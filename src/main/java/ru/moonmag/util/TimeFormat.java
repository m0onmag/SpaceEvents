package ru.moonmag.util;

import java.time.Duration;
import java.time.ZonedDateTime;

public final class TimeFormat {

    private TimeFormat() {
    }

    public static String remaining(ZonedDateTime from, ZonedDateTime to, Messages messages) {
        long total = Math.max(0, Duration.between(from, to).getSeconds());
        String d = String.valueOf(total / 86_400);
        String h = String.valueOf((total % 86_400) / 3_600);
        String m = String.valueOf((total % 3_600) / 60);
        String s = String.valueOf(total % 60);

        String key = total >= 86_400 ? "time-days" : total >= 3_600 ? "time-hours" : "time-minutes";
        return messages.plain(key, "d", d, "h", h, "m", m, "s", s);
    }
}