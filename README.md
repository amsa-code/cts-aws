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
* In AWS accumulate reports in local files in the following categories:
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


