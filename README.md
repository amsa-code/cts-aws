# cts-aws
Scalable implementation of craft tracking system using AWS.

##Requirements
Given position reports with lat, long and time would like to make performant (see below) queries based on
* ids
* geographic bounding box
* time range
* time sampling
* any combination of the above (including all three)

For example might want all vessel positions for 
* vessel mmsi=123456789
* between (-50, 135) and (-20, 150) lat longs
* for the month of December
* sampled hourly

##Performance
* low latency queries (<1s would be excellent, <2s good)
* data streamed to client as quickly as available bandwidth allows

##Design
Some back of envelope calculations based on 10 million reports a day indicate that using DynamoDB (SSD scalable storage) might cost thousands of dollars per month. If a scalable solution using S3 that has acceptable performance characteristics can be found then the cost would probably be 1/100 of that.

Lets explore a design using S3. We can go for gold on using storage because S3 storage is so cheap ($0.03 per GB per month). This enables geospatial indexing using geohashes and multiple copies by identifier and time block can be made as indexes without great penalty in terms of cost.

The approach is:
* Accumulate reports on disk locally (remembering some reports have arrival latency like positions sent via satellite)
* Rollover report files every day
* On rollover send the file for the previous day (ending 24 hours ago) to AWS for processing
* Now build geospatial, time and identifier indexes in s3
* In AWS accumulate reports in memory maps in the following categories:
  * GeographicHash (lengths 0 to 10 (~1m<sup>2</sup>))
  * TimeBlock (1s, 30s, 1min, 5min, 15min, 30min, 1hr, 2hr, 4hr, 8hr, 12hr, 1d)
  * Id Key and Value
* write lists of reports to files in s3 as per below (search indexes!)

Note: at some point we will also be interested in TimeBlocks of 2d, 4d, 8d, 16d, 32d, 64d, 128d, 1y but this may well involve map reduce jobs to produce. The big limitation preventing us including them now is that you cannot append data to an object in S3, you must completely rewrite it. This is not something we will want to do often for some of the bigger files like `r/1s/2015-01-01T00:00:00`.

Given a lat long point (-42, 135) at 2016-01-20T03:22:07 UTC its full resolution geohash is `r081040h2081`.

The hashes to increasing accuracy are:
```
ALL
r
r0
r08
r081
r0810
r08104
r081040h
r081040h2
r081040h20
```
The time blocks are:
```
2016-01-20T03:22:07 1s
2016-01-20T03:22:00 30s
2016-01-20T03:22:00 1m
2016-01-20T03:20:00 5m
2016-01-20T03:15:00 15m
2016-01-20T03:00:00 30m
2016-01-20T03:00:00 1h
2016-01-20T02:00:00 2h
2016-01-20T00:00:00 4h
2016-01-20T00:00:00 8h
2016-01-20T00:00:00 12h
2016-01-20T00:00:00 1d
...
```

So we would want the position to appear in time order (?) in these files (200 of them!):

```
r/block-1s/sample-all/2016-01-20T03:22:07
r/block-30s/sample-all/2016-01-20T03:22:00
r/block-30s/sample-1s/2016-01-20T03:22:00
r/block-30s/sample-30s/2016-01-20T03:22:00
r/block-1m/sample-all/2016-01-20T03:22:00
r/block-1m/sample-1s/2016-01-20T03:22:00
r/block-1m/sample-30s/2016-01-20T03:22:00
r/block-1m/sample-1m/2016-01-20T03:22:00
...
r0/block-1s/sample-all/2016-01-20T03:22:07
r0/block-30s/sample-all/2016-01-20T03:22:00
r0/block-30s/sample-1s/2016-01-20T03:22:00
r0/block-30s/sample-30s/2016-01-20T03:22:00
...

```

The meaning of block-n/sample-m/T is 
* the reports are sampled by individual vessel to every m time units (max one report by identifier per m time units)
* the reports are for a time t such that  T &lte; t &lt; T + n time units

Note that it doesn't make sense to retain sampled copies for time periods less than the sample period.

Now make another copy of the reports including the identifier key and value in the s3 path (there may be multiple identifiers so might make a copy for each):

```
mmsi123456789/r/1s/2016-01-20T03:22:07
mmsi123456789/r/30s/2016-01-20T03:22:00
mmsi123456789/r/1m/2016-01-20T03:22:00
```
That makes 400 copies of each report are copied into various indexes (assuming a single identifier per report).

Using the binary format with mmsi from the [risky](https://github.com/amsa-code/risky) project would correspond to 35 bytes per report and zipping would reduce the file size by a factor of 8.

Thus a years data at 10 million positions a day (70% more than current rates in AMSA as of Jan 2016) would correspond to 10m x 35 /8 bytes * 400 * 365 = 17.5 GB a day = 6.4TB a year! 

The cost of storing data in S3 Sydney is roughly $US0.03 per GB per month.

Starting from zero the accumulated cost of storing this data would be 

`n(n+1)/2 * 17.5 * 365/12 * 0.03 = 16n(n+1)` in dollars after `n` months

Accumlating the cost of the system over time:

```
months $US
----------
 1m      2
 2m      6
 6m     42
12m    166
24m    600
36m   1332
48m   2352
```

A total storage cost of $2352 after 4 years seems quite reasonable given the db has 25.6TB of data! 
