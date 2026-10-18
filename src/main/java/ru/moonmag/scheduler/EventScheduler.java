package ru.moonmag.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import ru.moonmag.event.EventRegistry;
import ru.moonmag.event.GameEvent;
import ru.moonmag.util.Messages;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.logging.Level;

public final class EventScheduler {

    private final JavaPlugin plugin;
    private final EventRegistry registry;
    private final Messages messages;
    private BukkitTask task;
    private long lastMinute;
    private long maxCatchUpMinutes;

    public EventScheduler(JavaPlugin plugin, EventRegistry registry, Messages messages) {
        this.plugin = plugin;
        this.registry = registry;
        this.messages = messages;
    }

    public void start() {
        stop();
        maxCatchUpMinutes = plugin.getConfig().getLong("max-catch-up-minutes");
        lastMinute = currentMinute();
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 20L, 20L);
    }
    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private void tick() {
        long minute = currentMinute();
        if (minute == lastMinute) return;

        long from = Math.max(lastMinute + 1, minute - maxCatchUpMinutes + 1);
        lastMinute = minute;

        for (long m = from; m <= minute; m++) {
            ZonedDateTime time = Instant.ofEpochSecond(m * 60L).atZone(registry.getZone());
            for (GameEvent event : registry.getEvents()) {
                if (event.isEnabled() && event.matches(time)) run(event);
            }
        }
    }

    public void run(GameEvent event) {
        for (String command : event.getCommands()) {
            try {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            } catch (Exception e) {
                messages.log(Level.WARNING, "log-command-error",
                        "name", event.getName(),
                        "command", command,
                        "error", String.valueOf(e.getMessage()));
            }
        }
        messages.log(Level.INFO, "log-event-started", "name", event.getName());
    }

    private static long currentMinute() {
        return System.currentTimeMillis() / 60_000L;
    }
}