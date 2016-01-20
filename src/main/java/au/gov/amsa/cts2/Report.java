package au.gov.amsa.cts2;

import java.util.Objects;

public interface Report {

    String identifier();

    double lat();

    double lon();

    long time();

    default int hashCodeReport() {
        return Objects.hash(identifier(), lat(), lon(), time());
    }

    default boolean equalsReport(Object o) {
        if (o == null || !(o instanceof Report)) {
            return false;
        } else {
            Report r = (Report) o;
            return Objects.equals(this.identifier(), r.identifier()) && lat() == r.lat()
                    && lon() == r.lon() && time() == r.time();
        }
    }
}
