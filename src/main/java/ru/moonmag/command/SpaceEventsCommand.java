package ru.moonmag.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import ru.moonmag.event.EventRegistry;
import ru.moonmag.event.GameEvent;
import ru.moonmag.event.NextEvent;
import ru.moonmag.event.ScheduleSlot;
import ru.moonmag.scheduler.EventScheduler;
import ru.moonmag.util.Messages;
import ru.moonmag.util.TimeFormat;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public final class SpaceEventsCommand implements CommandExecutor, TabCompleter {

    private static final String ADMIN = "spaceevents.admin";
    private static final String USE = "spaceevents.use";
    private static final List<String> SUBCOMMANDS = Arrays.asList("next", "list", "run", "reload");

    private final EventRegistry registry;
    private final EventScheduler scheduler;
    private final Messages messages;

    public SpaceEventsCommand(EventRegistry registry, EventScheduler scheduler, Messages messages) {
        this.registry = registry;
        this.scheduler = scheduler;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String sub = args.length == 0 ? "next" : args[0].toLowerCase(Locale.ROOT);

        switch (sub) {
            case "next":
                if (!check(sender, USE)) return true;
                showNext(sender);
                return true;
            case "list":
                if (!check(sender, ADMIN)) return true;
                showList(sender);
                return true;
            case "reload":
                if (!check(sender, ADMIN)) return true;
                int count = registry.load();
                sender.sendMessage(messages.prefixed("reloaded", "count", String.valueOf(count)));
                return true;
            case "run":
                if (!check(sender, ADMIN)) return true;
                runEvent(sender, args);
                return true;
            default:
                sender.sendMessage(messages.prefixed("usage", "label", label));
                return true;
        }
    }

    private void showNext(CommandSender sender) {
        NextEvent next = registry.findNext();
        if (next == null) {
            sender.sendMessage(messages.prefixed("no-next"));
            return;
        }
        ZonedDateTime now = ZonedDateTime.now(registry.getZone());
        sender.sendMessage(messages.prefixed("next",
                "name", next.getEvent().getName(),
                "time", TimeFormat.remaining(now, next.getTime(), messages)));
    }

    private void showList(CommandSender sender) {
        if (registry.getEvents().isEmpty()) {
            sender.sendMessage(messages.prefixed("list-empty"));
            return;
        }
        for (GameEvent event : registry.getEvents()) {
            String slots = event.getSlots().stream().map(ScheduleSlot::toString).collect(Collectors.joining(", "));
            sender.sendMessage(messages.prefixed("list-entry",
                    "name", event.getName(),
                    "state", messages.get(event.isEnabled() ? "state-on" : "state-off"),
                    "slots", slots.isEmpty() ? messages.get("no-schedule") : slots));
        }
    }

    private void runEvent(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(messages.prefixed("run-usage"));
            return;
        }
        GameEvent event = registry.find(args[1]);
        if (event == null) {
            sender.sendMessage(messages.prefixed("event-not-found", "name", args[1]));
            return;
        }
        scheduler.run(event);
        sender.sendMessage(messages.prefixed("event-started", "name", event.getName()));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission(ADMIN)) return Collections.emptyList();

        if (args.length == 1) return filter(SUBCOMMANDS, args[0]);
        if (args.length == 2 && args[0].equalsIgnoreCase("run")) {
            List<String> names = new ArrayList<>();
            for (GameEvent event : registry.getEvents()) names.add(event.getName());
            return filter(names, args[1]);
        }
        return Collections.emptyList();
    }

    private static List<String> filter(List<String> options, String prefix) {
        String lower = prefix.toLowerCase(Locale.ROOT);
        return options.stream()
                .filter(o -> o.toLowerCase(Locale.ROOT).startsWith(lower))
                .collect(Collectors.toList());
    }

    private boolean check(CommandSender sender, String permission) {
        if (sender.hasPermission(permission)) return true;
        sender.sendMessage(messages.prefixed("no-permission"));
        return false;
    }
}