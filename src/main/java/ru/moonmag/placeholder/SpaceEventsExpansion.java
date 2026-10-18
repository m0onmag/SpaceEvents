package ru.moonmag.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import ru.moonmag.event.EventRegistry;
import ru.moonmag.event.NextEvent;
import ru.moonmag.util.Messages;
import ru.moonmag.util.TimeFormat;
import java.time.ZonedDateTime;

public final class SpaceEventsExpansion extends PlaceholderExpansion {

    private final JavaPlugin plugin;
    private final EventRegistry registry;
    private final Messages messages;

    public SpaceEventsExpansion(JavaPlugin plugin, EventRegistry registry, Messages messages) {
        this.plugin = plugin;
        this.registry = registry;
        this.messages = messages;
    }

    @Override
    public String getIdentifier() {
        return "spaceevents";
    }

    @Override
    public String getAuthor() {
        return "m0onmag";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        NextEvent next = registry.findNext();
        if (next == null) return messages.plain("placeholder-none");

        switch (params.toLowerCase()) {
            case "next_event":
                return TimeFormat.remaining(ZonedDateTime.now(registry.getZone()), next.getTime(), messages);
            case "next_event_name":
                return next.getEvent().getName();
            case "next_event_time":
                return String.format("%02d:%02d", next.getTime().getHour(), next.getTime().getMinute());
            default:
                return null;
        }
    }
}