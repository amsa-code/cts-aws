package au.gov.amsa.cts2;

import org.junit.Test;

import au.gov.amsa.cts2.proto.PositionProtos.Position;

public class PositionProtosTest {

    @Test
    public void test() {
        {
            Position p = Position.newBuilder().setIdentifierType(1).setValueInteger(123456789L)
                    .setLatitude(-35f).setLongitude(142f).setTimeEpochMs(System.currentTimeMillis())
                    .setHeadingDegrees(27).setCourseDegrees(25).setSpeedKnots(12.0f).build();
            System.out.println(p.toByteArray().length);
        }
        {
            Position p = Position.newBuilder().setIdentifierType(1).setValueInteger(123456789L)
                    .setLatitude(-35f).setLongitude(142f).setTimeEpochMs(System.currentTimeMillis())
                    .build();
            System.out.println(p.toByteArray().length);
        }
    }

}
