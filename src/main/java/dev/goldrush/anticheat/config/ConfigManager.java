package dev.goldrush.anticheat.config;

import dev.goldrush.anticheat.GoldRushAC;
import dev.goldrush.anticheat.core.CheckType;
import dev.goldrush.anticheat.core.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Loads config.yml, messages.yml, detections.yml and permissions.yml. */
public final class ConfigManager {

    private static final String[] FILES = {"config.yml", "messages.yml", "detections.yml", "permissions.yml"};

    private final GoldRushAC plugin;
    private FileConfiguration config;
    private FileConfiguration messages;
    private FileConfiguration detections;
    private FileConfiguration permissionsFile;
    private final Map<CheckType, DetectionSettings> cache = new EnumMap<>(CheckType.class);

    public ConfigManager(GoldRushAC plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.getDataFolder().mkdirs();
        for (String f : FILES) {
            if (!new File(plugin.getDataFolder(), f).exists()) plugin.saveResource(f, false);
        }
        config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "config.yml"));
        messages = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "messages.yml"));
        detections = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "detections.yml"));
        permissionsFile = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "permissions.yml"));
        cache.clear();
        for (CheckType t : CheckType.values()) cache.put(t, read(t));
    }

    private DetectionSettings read(CheckType t) {
        ConfigurationSection s = detections.getConfigurationSection(t.path());
        if (s == null) {
            return new DetectionSettings(true, t.defaultThreshold(), t.defaultDecaySeconds(), true, false, 5, List.of());
        }
        List<DetectionSettings.Step> steps = new ArrayList<>();
        for (Map<?, ?> m : s.getMapList("punishment.steps")) {
            Object vl = m.get("vl");
            Object action = m.get("action");
            Object duration = m.get("duration");
            if (!(vl instanceof Number n) || action == null) continue;
            long ms = duration == null ? 0 : Math.max(0, Util.parseDuration(String.valueOf(duration)));
            steps.add(new DetectionSettings.Step(n.doubleValue(), String.valueOf(action).toUpperCase(), ms));
        }
        steps.sort((a, b) -> Double.compare(a.vl(), b.vl()));
        return new DetectionSettings(
                s.getBoolean("enabled", true),
                s.getDouble("threshold", t.defaultThreshold()),
                s.getDouble("decay", t.defaultDecaySeconds()),
                s.getBoolean("alert", true),
                s.getBoolean("debug", false),
                s.getInt("min-flags", 5),
                steps);
    }

    public DetectionSettings det(CheckType t) {
        return cache.get(t);
    }

    public FileConfiguration config() { return config; }
    public FileConfiguration messages() { return messages; }
    public FileConfiguration detections() { return detections; }

    // ---- config.yml shortcuts (paths are under "anti-cheat." unless they start with "root:") ----

    public boolean bool(String path, boolean def) { return config.getBoolean(path, def); }
    public int integer(String path, int def) { return config.getInt(path, def); }
    public double number(String path, double def) { return config.getDouble(path, def); }
    public String string(String path, String def) { return config.getString(path, def); }
    public List<String> list(String path) { return config.getStringList(path); }

    // ---- messages ----

    /** The messages.yml text with {placeholders} filled in. Colour codes are still '&' codes. */
    public String raw(String key, Object... kv) {
        String s = messages.getString(key);
        if (s == null) s = "&c[missing message: " + key + "]";
        for (int i = 0; i + 1 < kv.length; i += 2) {
            s = s.replace("{" + kv[i] + "}", String.valueOf(kv[i + 1]));
        }
        return s;
    }

    /** raw() with the plugin prefix in front. */
    public String msg(String key, Object... kv) {
        return messages.getString("prefix", "&8[&6GoldRushAC&8] ") + raw(key, kv);
    }

    public Component comp(String legacy) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(legacy);
    }

    public Component msgc(String key, Object... kv) {
        return comp(msg(key, kv));
    }

    /** A permission node from permissions.yml (falls back to the built-in default). */
    public String perm(String key) {
        return permissionsFile.getString("permissions." + key, "goldrushac." + key);
    }
}
