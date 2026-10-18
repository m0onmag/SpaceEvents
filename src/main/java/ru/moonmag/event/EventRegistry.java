package ru.moonmag.event;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import ru.moonmag.util.Messages;
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

public final class EventRegistry {

    private static final DateTimeFormatter TIME_PARSER = DateTimeFormatter.ofPattern("H:mm");

    private final JavaPlugin plugin;
    private final Messages messages;
    private volatile Snapshot snapshot;

    public EventRegistry(JavaPlugin plugin, Messages messages) {
        this.plugin = plugin;
        this.messages = messages;
    }

    public int load() {
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        ZoneId zone = parseZone(config.getString("timezone"));
        Map<String, GameEvent> events = new LinkedHashMap<>();

        ConfigurationSection root = config.getConfigurationSection("events");
        if (root != null) {
            for (String name : root.getKeys(false)) {
                GameEvent event = parseEvent(name, root.getConfigurationSection(name));
                if (event != null) events.put(name.toLowerCase(Locale.ROOT), event);
            }
        }

        snapshot = new Snapshot(zone, events);
        return events.size();
    }

    public ZoneId getZone() {
        return snapshot.zone;
    }

    public Collection<GameEvent> getEvents() {
        return snapshot.events.values();
    }

    public GameEvent find(String name) {
        return name == null ? null : snapshot.events.get(name.toLowerCase(Locale.ROOT));
    }

    public NextEvent findNext() {
        Snapshot snap = snapshot;
        ZonedDateTime now = ZonedDateTime.now(snap.zone);

        NextEvent cached = snap.cachedNext;
        if (cached != null && cached.getTime().isAfter(now)) return cached;

        NextEvent best = null;
        for (GameEvent event : snap.events.values()) {
            if (!event.isEnabled()) continue;
            ZonedDateTime time = event.nextAfter(now);
            if (time != null && (best == null || time.isBefore(best.getTime()))) {
                best = new NextEvent(event, time);
            }
        }
        snap.cachedNext = best;
        return best;
    }

    private ZoneId parseZone(String raw) {
        try {
            if (raw == null) throw new DateTimeException("null");
            return ZoneId.of(raw.trim());
        } catch (DateTimeException e) {
            throw new IllegalStateException(messages.plain("log-bad-zone", "zone", String.valueOf(raw)));
        }
    }

    private GameEvent parseEvent(String name, ConfigurationSection section) {
        if (section == null) {
            messages.log(Level.WARNING, "log-bad-section", "name", name);
            return null;
        }

        List<String> commands = new ArrayList<>();
        for (String raw : section.getStringList("commands")) addCommand(commands, raw);
        addCommand(commands, section.getString("command"));
        if (commands.isEmpty()) {
            messages.log(Level.WARNING, "log-no-commands", "name", name);
            return null;
        }

        List<ScheduleSlot> slots = new ArrayList<>();
        ConfigurationSection schedule = section.getConfigurationSection("schedule");
        if (schedule != null) {
            for (String dayKey : schedule.getKeys(false)) {
                DayOfWeek day = parseDay(dayKey);
                if (day == null && !dayKey.equalsIgnoreCase("ALL")) {
                    messages.log(Level.WARNING, "log-bad-day", "name", name, "day", dayKey);
                    continue;
                }
                for (String rawTime : schedule.getStringList(dayKey)) {
                    try {
                        slots.add(new ScheduleSlot(day, LocalTime.parse(rawTime.trim(), TIME_PARSER)));
                    } catch (DateTimeParseException e) {
                        messages.log(Level.WARNING, "log-bad-time", "name", name, "time", rawTime);
                    }
                }
            }
        }
        if (slots.isEmpty()) {
            messages.log(Level.INFO, "log-no-schedule", "name", name);
        }

        return new GameEvent(name, section.getBoolean("enabled"), commands, slots);
    }

    private static void addCommand(List<String> target, String raw) {
        if (raw == null) return;
        String cmd = raw.trim();
        if (cmd.startsWith("/")) cmd = cmd.substring(1).trim();
        if (!cmd.isEmpty()) target.add(cmd);
    }

    private static DayOfWeek parseDay(String key) {
        try {
            return DayOfWeek.valueOf(key.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static final class Snapshot {
        final ZoneId zone;
        final Map<String, GameEvent> events;
        volatile NextEvent cachedNext;

        Snapshot(ZoneId zone, Map<String, GameEvent> events) {
            this.zone = zone;
            this.events = Collections.unmodifiableMap(events);
        }
    }
}