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
  * TimeBlock (1s, 30s, 1min, 5min, 15min, 30min, 1hr, 2hr, 4hr, 8hr, 12hr, 1d, 2d, 4d, 7d, 14d, 28d)
  * Id Key and Value




