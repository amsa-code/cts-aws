package au.gov.amsa.cts2;

import com.github.davidmoten.geo.GeoHash;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

//Not thread safe
final class Accumulator {

    private final HashMultimap<Key, SampledReport> m = HashMultimap.create();

    void add(Report report) {
        putKeys(report);
    }

    private void putKeys(Report report) {
        String geohash = GeoHash.encodeHash(report.lat(), report.lon());
        for (int i = 0; i < geohash.length(); i++) {
            for (Sample block : Sample.values()) {
                for (Sample sample : Sample.values()) {
                    if (sample.ordinal() <= block.ordinal()) {
                        Key key = new Key(geohash.substring(0, i), block, sample,
                                block.snapTo(report.time()));
                        m.put(key, new SampledReport(report, key.sample));
                    }
                }
            }
        }
    }

    public Multimap<Key, SampledReport> map() {
        return m;
    }

}
