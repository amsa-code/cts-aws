# cts-aws
Scalable implementation of craft tracking system using AWS.

#Requirements
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

#Design
Some back of envelope calculations based on 10 million reports a day indicate that using DynamoDB (SSD scalable storage) might cost thousands of dollars per month. If a scalable solution using S3 that has acceptable performance characteristics can be found then the cost would probably be 1/100 of that.

##Option 1
* Accumulate reports over 24 hours (remembering some reports have arrival latency like positions sent via satellite)
* Send accumulated file to AWS for processing
* In AWS accumulate reports in memory maps in the following categories:
  * GeographicHash (lengths 0 to 10 (~1m2))
  * TimeBlock (1s, 30s, 1min, 5min, 15min, 30min, 1hr, 2hr, 4hr, 8hr, 12hr, 1d, 2d, 4d, 8d, 16d, 32d)
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
`

So we would want the position to appear in time order (?) in these files:

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











