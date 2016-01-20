package au.gov.amsa.cts2;

import java.util.concurrent.TimeUnit;

public enum Sample {

    SECONDS_1(1, TimeUnit.SECONDS), SECONDS_30(30, TimeUnit.SECONDS), MINUTES_1(1,
            TimeUnit.MINUTES), MINUTES_5(5, TimeUnit.MINUTES), MINUTES_15(15,
                    TimeUnit.MINUTES), MINUTES_30(30, TimeUnit.MINUTES), HOURS_1(1,
                            TimeUnit.HOURS), HOURS_2(2, TimeUnit.HOURS), HOURS_4(4,
                                    TimeUnit.HOURS), HOURS_8(8, TimeUnit.HOURS), HOURS_12(12,
                                            TimeUnit.HOURS), DAYS_1(1, TimeUnit.DAYS);

    private final long duration;
    private final TimeUnit unit;

    private Sample(long duration, TimeUnit unit) {
        this.duration = duration;
        this.unit = unit;
    }

    public long duration() {
        return duration;
    }

    public TimeUnit unit() {
        return unit;
    }

    public String description() {
        return duration + " " + unit.name();
    }

    public long snapTo(long time) {
        long remainder = time % unit.toMillis(duration);
        return time - remainder;
    }

    public String shortName() {
        return duration + "-" + unit.name();
    }

}
