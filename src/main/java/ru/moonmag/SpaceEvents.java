package ru.moonmag;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import ru.moonmag.command.SpaceEventsCommand;
import ru.moonmag.event.EventRegistry;
import ru.moonmag.placeholder.SpaceEventsExpansion;
import ru.moonmag.scheduler.EventScheduler;
import ru.moonmag.util.Messages;
import ru.moonmag.util.Metrics;
import ru.moonmag.util.UpdateChecker;

public final class SpaceEvents extends JavaPlugin {

    private static final String LINK = "§x§0§0§f§5§f§fh§x§0§0§f§4§f§ft§x§0§0§f§3§f§ft§x§0§0§f§2§f§fp§x§0§0§f§1§f§fs"
            + "§x§0§0§e§f§f§f:§x§0§0§e§e§f§f/§x§0§0§e§d§f§f/§x§0§0§e§c§f§ft§x§0§0§e§b§f§f.§x§0§0§e§a§f§fm"
            + "§x§0§0§e§9§f§fe§x§0§0§e§8§f§f/§x§0§0§e§7§f§fs§x§0§0§e§5§f§fp§x§0§0§e§4§f§fa§x§0§0§e§3§f§fc"
            + "§x§0§0§e§2§f§fe§x§0§0§e§1§f§fs§x§0§0§e§0§f§ft§x§0§0§d§f§f§fu§x§0§0§d§e§f§fd§x§0§0§d§c§f§fi"
            + "§x§0§0§d§b§f§fo§x§0§0§d§a§f§fm§x§0§0§d§9§f§fc";

    private EventScheduler scheduler;
    private SpaceEventsExpansion expansion;
    private Metrics metrics;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        Messages messages = new Messages(this);

        EventRegistry registry = new EventRegistry(this, messages);
        int loaded = registry.load();

        scheduler = new EventScheduler(this, registry, messages);
        scheduler.start();

        PluginCommand command = getCommand("spaceevents");
        if (command != null) {
            SpaceEventsCommand handler = new SpaceEventsCommand(registry, scheduler, messages);
            command.setExecutor(handler);
            command.setTabCompleter(handler);
        }

        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            expansion = new SpaceEventsExpansion(this, registry, messages);
            expansion.register();
        }

        if (getConfig().getBoolean("update-checker")) {
            new UpdateChecker(this).checkForUpdates();
        }

        if (getConfig().getBoolean("metrics")) {
            metrics = new Metrics(this, 34804);
        }

        banner("Запуск плагина...", "§x§0§0§f§f§1§7Плагин запустился!", null);
    }

    @Override
    public void onDisable() {
        if (scheduler != null) scheduler.stop();
        if (expansion != null) expansion.unregister();
        if (metrics != null) metrics.shutdown();

        banner("Отключение плагина...", "§x§f§f§0§0§0§0Плагин отключился!", null);
    }

    private void banner(String title, String status, String extra) {
        String border = "§x§f§f§7§c§0§0";
        getLogger().info(border + "╔");
        getLogger().info(border + "║ §f" + title);
        getLogger().info(border + "║ " + status + " §fАвтор: §x§f§f§6§e§0§0MoonMag");
        if (extra != null) getLogger().info(border + "║ §f" + extra);
        getLogger().info(border + "║ " + LINK);
        getLogger().info(border + "╚");
    }
}