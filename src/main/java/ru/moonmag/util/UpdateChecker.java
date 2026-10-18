package ru.moonmag.util;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class UpdateChecker {

    private static final String REPO = "m0onmag/SpaceEvents";

    private final JavaPlugin plugin;
    private final Logger logger;

    public UpdateChecker(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public void checkForUpdates() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL("https://api.github.com/repos/" + REPO + "/releases/latest");
                conn = (HttpURLConnection) url.openConnection();
                conn.addRequestProperty("User-Agent", "Mozilla/5.0");

                int code = conn.getResponseCode();
                if (code != 200) {
                    logger.warning("Не удалось проверить обновление. Код: " + code);
                    return;
                }

                JSONObject json = (JSONObject) new JSONParser().parse(new InputStreamReader(conn.getInputStream()));
                String latestVersion = (String) json.get("tag_name");
                String currentVersion = plugin.getDescription().getVersion();

                if (!normalize(currentVersion).equalsIgnoreCase(normalize(latestVersion))) {
                    logger.warning("Доступна новая версия плагина: " + latestVersion + " (текущая: " + currentVersion + ")");
                    logger.warning("Скачать: https://github.com/" + REPO + "/releases/tag/" + latestVersion);
                } else {
                    logger.info("Вы используете последнюю версию плагина (" + currentVersion + ")");
                }
            } catch (Exception e) {
                logger.warning("Ошибка при проверке обновлений: " + e.getMessage());
            } finally {
                if (conn != null) conn.disconnect();
            }
        });
    }

    private static String normalize(String version) {
        String v = version.trim();
        return v.startsWith("v") || v.startsWith("V") ? v.substring(1) : v;
    }
}