package dev.goldrush.anticheat;

import dev.goldrush.anticheat.command.StaffCommand;
import dev.goldrush.anticheat.config.ConfigManager;
import dev.goldrush.anticheat.core.PlayerData;
import dev.goldrush.anticheat.detection.ViolationManager;
import dev.goldrush.anticheat.listener.MonitorListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class GoldRushAC extends JavaPlugin {
    private final Map<UUID, PlayerData> players = new ConcurrentHashMap<>();
    private ConfigManager configs;
    private ViolationManager violations;
    @Override public void onEnable() {
        saveDefaultConfig();
        configs = new ConfigManager(this);
        configs.load();
        violations = new ViolationManager(this);
        Bukkit.getPluginManager().registerEvents(new MonitorListener(this), this);
        StaffCommand command = new StaffCommand(this);
        getCommand("ac").setExecutor(command); getCommand("ac").setTabCompleter(command);
        getCommand("sus").setExecutor(command); getCommand("alerts").setExecutor(command);
        for (Player p : Bukkit.getOnlinePlayers()) data(p);
        getLogger().info("GoldRushAC enabled; automatic punishments remain disabled.");
    }
    @Override public void onDisable() { players.clear(); }
    public PlayerData data(Player p) { return players.computeIfAbsent(p.getUniqueId(), id -> new PlayerData(id, p.getName())); }
    public Map<UUID, PlayerData> players() { return players; }
    public ConfigManager configs() { return configs; }
    public ViolationManager violations() { return violations; }
}