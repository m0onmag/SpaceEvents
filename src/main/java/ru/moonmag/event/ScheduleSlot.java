package ru.moonmag.event;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public final class ScheduleSlot {

    private static final DateTimeFormatter DISPLAY = DateTimeFormatter.ofPattern("HH:mm");

    private final DayOfWeek day; // null = каждый день
    private final LocalTime time;

    public ScheduleSlot(DayOfWeek day, LocalTime time) {
        this.day = day;
        this.time = time;
    }

    public boolean matches(ZonedDateTime now) {
        return (day == null || day == now.getDayOfWeek())
                && time.getHour() == now.getHour()
                && time.getMinute() == now.getMinute();
    }

    public ZonedDateTime nextAfter(ZonedDateTime now) {
        LocalDate date = now.toLocalDate();
        for (int i = 0; i <= 7; i++) {
            LocalDate candidateDate = date.plusDays(i);
            if (day != null && candidateDate.getDayOfWeek() != day) continue;
            ZonedDateTime candidate = ZonedDateTime.of(candidateDate, time, now.getZone());
            if (candidate.isAfter(now)) return candidate;
        }
        return null;
    }

    @Override
    public String toString() {
        return (day == null ? "ALL" : day.name()) + " " + time.format(DISPLAY);
    }
}