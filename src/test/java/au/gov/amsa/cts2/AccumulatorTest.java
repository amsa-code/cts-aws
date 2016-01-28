package au.gov.amsa.cts2;

import java.io.File;

import org.joda.time.DateTime;
import org.junit.Test;

public final class AccumulatorTest {

    @Test
    public void test() throws Exception {
        Accumulator a = new Accumulator();
        a.add(createReport("2016-01-19T03:47:07"));
        a.add(createReport("2016-01-19T03:47:57"));
        a.add(createReport("2016-01-19T03:48:04"));
        a.add(createReport("2016-01-19T04:48:04"));
        for (Key key : a.map().keySet()) {
            System.out.print(key);
            System.out.println(" -->");
            System.out.print("  ");
            System.out.println(a.map().get(key));
        }
        System.out.println("keys " + a.map().keySet().size());
        System.out.println("reports " + a.map().size());
        a.writeToFiles(new File("target"));
    }

    private static Report createReport(String dateTime) {
        return new Report() {
            @Override
            public int identifierType() {
                return 1;//mmsi
            }
            
            @Override
            public String identifier() {
                return "123456789";
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
                return DateTime.parse(dateTime).getMillis();
            }

        };
    }
}
