package ru.moonmag.event;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

public final class GameEvent {

    private final String name;
    private final boolean enabled;
    private final List<String> commands;
    private final List<ScheduleSlot> slots;

    public GameEvent(String name, boolean enabled, List<String> commands, List<ScheduleSlot> slots) {
        this.name = name;
        this.enabled = enabled;
        this.commands = Collections.unmodifiableList(commands);
        this.slots = Collections.unmodifiableList(slots);
    }

    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public List<String> getCommands() {
        return commands;
    }

    public List<ScheduleSlot> getSlots() {
        return slots;
    }

    public boolean matches(ZonedDateTime now) {
        for (ScheduleSlot slot : slots) {
            if (slot.matches(now)) return true;
        }
        return false;
    }

    public ZonedDateTime nextAfter(ZonedDateTime now) {
        ZonedDateTime best = null;
        for (ScheduleSlot slot : slots) {
            ZonedDateTime candidate = slot.nextAfter(now);
            if (candidate != null && (best == null || candidate.isBefore(best))) best = candidate;
        }
        return best;
    }
}