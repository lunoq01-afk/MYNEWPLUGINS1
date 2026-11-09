package dev.goldrush.anticheat.detection;

import dev.goldrush.anticheat.GoldRushAC;
import dev.goldrush.anticheat.config.DetectionSettings;
import dev.goldrush.anticheat.core.CheckType;
import dev.goldrush.anticheat.core.PlayerData;
import dev.goldrush.anticheat.core.Util;
import dev.goldrush.anticheat.core.ViolationRecord;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Locale;

public final class ViolationManager {
    private final GoldRushAC plugin;
    public ViolationManager(GoldRushAC plugin) { this.plugin = plugin; }

    public void flag(Player player, CheckType type, String evidence, double weight) {
        if (!plugin.configs().bool("anti-cheat.enabled", true) || player.hasPermission("goldrushac.bypass")) return;
        DetectionSettings settings = plugin.configs().det(type);
        if (settings == null || !settings.enabled()) return;
        PlayerData data = plugin.data(player);
        long now = System.currentTimeMillis();
        long previous = data.lastFlag.getOrDefault(type, now);
        double current = data.vl(type);
        if (settings.decay() > 0 && previous < now) {
            current = Math.max(0, current - ((now - previous) / 1000.0) / settings.decay());
        }
        current += Math.max(0.1, weight);
        data.vl.put(type, current);
        data.counts.merge(type, 1, Integer::sum);
        data.lastFlag.put(type, now);
        int ping = player.getPing();
        ViolationRecord record = new ViolationRecord(now, type, current, evidence,
                player.getWorld().getName(), player.getLocation().getX(), player.getLocation().getY(),
                player.getLocation().getZ(), ping);
        data.addRecent(record);

        String detail = evidence == null ? "no evidence" : evidence;
        String line = plugin.configs().raw("flag", "player", player.getName(), "check", type.display(),
                "detail", detail, "vl", String.format(Locale.US, "%.1f", current));
        if (settings.alert()) {
            long last = data.lastAlert.getOrDefault(type, 0L);
            long cooldown = plugin.configs().integer("anti-cheat.alerts.cooldown-ms", 1500);
            if (now - last >= cooldown) {
                data.lastAlert.put(type, now);
                for (Player staff : Bukkit.getOnlinePlayers()) {
                    if (staff.hasPermission("goldrushac.staff") && plugin.data(staff).alertsOn) {
                        staff.sendMessage(plugin.configs().comp(plugin.configs().msg("flag",
                                "player", player.getName(), "check", type.display(), "detail", detail,
                                "vl", String.format(Locale.US, "%.1f", current))));
                    }
                }
                plugin.getLogger().info("[FLAG] " + player.getName() + " " + type.display() + " " +
                        Util.fmt(current, 2) + " - " + detail);
            }
        }
        if (data.debugTarget != null) {
            Player debugger = Bukkit.getPlayer(data.debugTarget);
            if (debugger != null && debugger.isOnline()) {
                debugger.sendMessage(plugin.configs().comp(plugin.configs().msg("debug",
                        "player", player.getName(), "check", type.display(), "detail", detail)));
            }
        }
        // Punishment execution is intentionally not automatic in this initial safe build.
        // Escalation settings are retained for a later reviewed punishment module.
    }
}