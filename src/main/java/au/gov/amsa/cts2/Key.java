package au.gov.amsa.cts2;

import java.time.Instant;
import java.time.ZoneOffset;

class Key {
    final Sample block;
    final Sample sample;
    final long time;
    final String geohash;

    public Key(String geohash, Sample block, Sample sample, long time) {
        this.geohash = geohash;
        this.block = block;
        this.sample = sample;
        this.time = time;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((block == null) ? 0 : block.hashCode());
        result = prime * result + ((geohash == null) ? 0 : geohash.hashCode());
        result = prime * result + ((sample == null) ? 0 : sample.hashCode());
        result = prime * result + (int) (time ^ (time >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Key other = (Key) obj;
        if (block != other.block)
            return false;
        if (geohash == null) {
            if (other.geohash != null)
                return false;
        } else if (!geohash.equals(other.geohash))
            return false;
        if (sample != other.sample)
            return false;
        if (time != other.time)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Key [block=" + block + ", sample=" + sample + ", time="
                + Instant.ofEpochMilli(time).atZone(ZoneOffset.UTC) + ", geohash=" + geohash + "]";
    }

}
