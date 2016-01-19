# cts-aws
Scalable implementation of craft tracking system using AWS.

##Requirements
Given position reports with lat, long and time need to quickly return results of queries based on
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

##Design
Some back of envelope calculations based on 10 million reports a day indicate that using DynamoDB (SSD scalable storage) might cost thousands of dollars per month. If a scalable solution using S3 that has acceptable performance characteristics can be found then the cost would probably be 1/100 of that.

###Option 1
* Accumulate reports on disk locally (remembering some reports have arrival latency like positions sent via satellite)
* Rollover report files every day
* On rollover send the file for the previous day (ending 24 hours ago) to AWS for processing
* In AWS accumulate reports in memory maps in the following categories:
  * GeographicHash (lengths 0 to 10 (~1m<sup>2</sup>))
  * TimeBlock (1s, 30s, 1min, 5min, 15min, 30min, 1hr, 2hr, 4hr, 8hr, 12hr, 1d, 2d, 4d, 8d, 16d, 32d, 64d, 128d, 1y)
  * Id Key and Value

Given a lat long point (-42, 135) at 2016-01-20T03:22:07 UTC its full resolution geohash is `r081040h2081`.

The hashes to increasing accuracy are:
```
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
2016-01-20T00:00:00 2d
2016-01-20T00:00:00 4d
2016-01-16T00:00:00 8d
...
```

So we would want the position to appear in time order (?) in these files (200 of them!):

```
r/1s/2016-01-20T03:22:07
r/30s/2016-01-20T03:22:00
r/1m/2016-01-20T03:22:00
...
r0/1s/2016-01-20T03:22:07
r0/30s/2016-01-20T03:22:00
r0/1m/2016-01-20T03:22:00
...

```

Using the binary format with mmsi from the [risky](https://github.com/amsa-code/risky) project would correspond to 35 bytes per report and zipping would reduce the file size by a factor of 8.

Thus a years data at 10 million positions a day (70% more than current rates in AMSA as of Jan 2016) would correspond to 10m x 35 /8 bytes * 200 * 365 = 8.75 GB a day = 3.2TB a year! 

The cost of storing data in S3 Sydney is roughly $0.03 per GB perc month.

Starting from zero the accumulated cost of storing this data would be 

`n(n+1)/2 * 8.75 * 365/12 * 0.03 = 8n(n+1)` in dollars after `n` months

Accumlating the cost of the system over time:

```
months   $
----------
 1m      1
 2m      3
 6m     21
12m     78
24m    300
36m    666
48m   1176
```











