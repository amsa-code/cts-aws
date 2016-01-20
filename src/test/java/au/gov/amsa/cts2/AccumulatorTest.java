package au.gov.amsa.cts2;

import org.joda.time.DateTime;
import org.junit.Test;

public final class AccumulatorTest {

    @Test
    public void test() throws Exception {
        Report report = createReport();
        Accumulator a = new Accumulator();
        a.add(report);
        for (Key key : a.map().keySet()) {
            System.out.print(key);
            System.out.println(" -->");
            System.out.print("  ");
            System.out.println(a.map().get(key));
        }
    }

    private static Report createReport() {
        return new Report() {

            @Override
            public String identifier() {
                return "mmsi123456789";
            }

            @Override
            public double lat() {
                return -42;
            }

            @Override
            public double lon() {
                return 135.0;
            }

            @Override
            public long time() {
                return DateTime.parse("2016-01-19T03:47:07").getMillis();
            }

        };
    }
}
