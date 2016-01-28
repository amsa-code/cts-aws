package au.gov.amsa.cts2;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.time.ZoneOffset;

import com.github.davidmoten.geo.GeoHash;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import au.gov.amsa.cts2.proto.PositionProtos.Position;

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

    public void writeToFiles(File directory) {
        String d = File.separator;
        for (Key key : m.keySet()) {
            String dir = key.geohash + d + "block-" + key.block.shortName() + d + "sample-"
                    + key.sample.shortName();
            String filename = Instant.ofEpochMilli(key.time).atZone(ZoneOffset.UTC).toString();
            File fileDir = new File(directory, dir);
            fileDir.mkdirs();
            File file = new File(fileDir, filename);
            // append reports to the file in the asppropriate binary format
            try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file, true))) {
                for (Report report : m.get(key)) {
                    //use protobuffers to serialize position to binary format
                    Position p = Position.newBuilder().setIdentifierType(1)
                            .setValueInteger(Integer.valueOf(report.identifier()))
                            .setLatitude((float) report.lat()).setLongitude((float) report.lon())
                            .setTimeEpochMs(report.time()).build();
                    out.write(p.toByteArray());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public Multimap<Key, SampledReport> map() {
        return m;
    }

}
