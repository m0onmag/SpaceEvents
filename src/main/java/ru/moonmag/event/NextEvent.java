package ru.moonmag.event;

import java.time.ZonedDateTime;

public final class NextEvent {

    private final GameEvent event;
    private final ZonedDateTime time;

    public NextEvent(GameEvent event, ZonedDateTime time) {
        this.event = event;
        this.time = time;
    }

    public GameEvent getEvent() {
        return event;
    }

    public ZonedDateTime getTime() {
        return time;
    }
}