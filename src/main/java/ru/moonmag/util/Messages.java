package ru.moonmag.util;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.logging.Level;

public final class Messages {

    private static final Pattern HEX = Pattern.compile(
            "&#([A-Fa-f0-9]{6})"
                    + "|<#([A-Fa-f0-9]{6})>"
                    + "|\\{#([A-Fa-f0-9]{6})\\}"
                    + "|#([A-Fa-f0-9]{6})"
                    + "|&#([A-Fa-f0-9]{3})(?![A-Fa-f0-9])"
                    + "|[&§]x((?:[&§][A-Fa-f0-9]){6})");

    private final JavaPlugin plugin;

    public Messages(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public String get(String key, String... pairs) {
        return color(plain(key, pairs));
    }

    public void log(Level level, String key, String... pairs) {
        if (!plugin.getConfig().getBoolean("log")) return;
        plugin.getLogger().log(level, plain(key, pairs));
    }

    public String plain(String key, String... pairs) {
        String text = plugin.getConfig().getString("messages." + key, key);
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            text = text.replace("{" + pairs[i] + "}", pairs[i + 1]);
        }
        return text;
    }

    public String prefixed(String key, String... pairs) {
        return color(plugin.getConfig().getString("messages.prefix")) + get(key, pairs);
    }

    private static String color(String text) {
        Matcher m = HEX.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String hex = null;
            for (int i = 1; i <= 4; i++) {
                if (m.group(i) != null) {
                    hex = m.group(i);
                    break;
                }
            }
            if (hex == null && m.group(5) != null) {
                StringBuilder full = new StringBuilder();
                for (char c : m.group(5).toCharArray()) full.append(c).append(c);
                hex = full.toString();
            }
            if (hex == null && m.group(6) != null) {
                hex = m.group(6).replaceAll("[&§]", "");
            }

            StringBuilder rep = new StringBuilder("§x");
            for (char c : hex.toCharArray()) rep.append('§').append(c);
            m.appendReplacement(sb, Matcher.quoteReplacement(rep.toString()));
        }
        m.appendTail(sb);
        return ChatColor.translateAlternateColorCodes('&', sb.toString());
    }
}