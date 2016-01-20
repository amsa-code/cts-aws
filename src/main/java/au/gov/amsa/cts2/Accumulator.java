package au.gov.amsa.cts2;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import com.github.davidmoten.geo.GeoHash;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

public final class Accumulator<T extends Report> {

    private final Multimap<String, T> m = ArrayListMultimap.create();

    void add(T report) {
        getKeys(report).stream().forEach(key -> m.put(key, report));
    }

    static List<String> getKeys(Report r) {
        List<String> list = Lists.newArrayList();
        String geohash = GeoHash.encodeHash(r.lat(), r.lon());

        for (int i = 0; i < geohash.length(); i++) {
            for (Sample block : Sample.values()) {
                for (Sample sample : Sample.values()) {
                    if (sample.ordinal() <= block.ordinal()) {
                        String dateTime = Instant.ofEpochMilli(block.snapTo(r.time()))
                                .atZone(ZoneId.of("UTC")).toString().replace("[UTC]", "");
                        StringBuilder b = new StringBuilder();
                        b.append(geohash.substring(0, i));
                        b.append('/');
                        b.append("block-");
                        b.append(block.shortName());
                        b.append("/");
                        b.append("sample-");
                        b.append(sample.shortName());
                        b.append("/");
                        b.append(dateTime);
                        list.add(b.toString());
                    }
                }
            }
        }
        return list;
    }
}
