package au.gov.amsa.cts2;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Objects;

public class SampledReport implements Report {

    private final Report report;
    private final Sample sample;

    public SampledReport(Report report, Sample sample) {
        this.report = report;
        this.sample = sample;
    }

    @Override
    public String identifier() {
        return report.identifier();
    }

    @Override
    public double lat() {
        return report.lat();
    }

    @Override
    public double lon() {
        return report.lon();
    }

    @Override
    public long time() {
        return report.time();
    }

    public long snappedTime() {
        return sample.snapTo(report.time());
    }

    @Override
    public int hashCode() {
        // hashCode and equals based only on the snapped-to time and the
        // identifier and sample
        return Objects.hash(report.identifier(), sample, snappedTime());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (!(o instanceof SampledReport)) {
            return false;
        } else {
            SampledReport s = (SampledReport) o;
            return Objects.equals(report.identifier(), s.identifier())
                    && Objects.equals(this.sample, s.sample) && snappedTime() == s.snappedTime();
        }
    }

    @Override
    public String toString() {
        return "SampledReport [time="
                + Instant.ofEpochMilli(report.time()).atZone(ZoneOffset.UTC).toString() + ", snap="
                + Instant.ofEpochMilli(snappedTime()).atZone(ZoneOffset.UTC).toString()
                + ", sample=" + sample + "]";
    }

}
