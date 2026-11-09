package dev.goldrush.anticheat.detection;

import dev.goldrush.anticheat.GoldRushAC;
import dev.goldrush.anticheat.config.DetectionSettings;
import dev.goldrush.anticheat.core.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.Locale;

public final class ViolationManager {
    private final GoldRushAC plugin;
    public ViolationManager(GoldRushAC plugin) { this.plugin = plugin; }
    public void flag(Player p, CheckType type, String evidence, double weight) {
        if (!plugin.configs().bool("anti-cheat.enabled", true) || p.hasPermission("goldrushac.bypass")) return;
        DetectionSettings settings = plugin.configs().det(type);
        if (settings == null || !settings.enabled()) return;
        PlayerData d = plugin.data(p); long now = System.currentTimeMillis();
        long previous = d.lastFlag.getOrDefault(type, now);
        double vl = d.vl(type);
        if (settings.decay() > 0) vl = Math.max(0, vl - Math.max(0, now - previous) / 1000.0 / settings.decay());
        vl += Math.max(0.1, weight); d.vl.put(type, vl); d.counts.merge(type, 1, Integer::sum); d.lastFlag.put(type, now);
        d.addRecent(new ViolationRecord(now,type,vl,evidence,p.getWorld().getName(),p.getLocation().getX(),p.getLocation().getY(),p.getLocation().getZ(),p.getPing()));
        String formatted = String.format(Locale.US,"%.1f",vl);
        if (settings.alert()) {
            long last = d.lastAlert.getOrDefault(type,0L);
            if (now-last >= plugin.configs().integer("anti-cheat.alerts.cooldown-ms",1500)) {
                d.lastAlert.put(type,now);
                for (Player staff : Bukkit.getOnlinePlayers()) if (staff.hasPermission("goldrushac.staff") && plugin.data(staff).alertsOn)
                    staff.sendMessage(plugin.configs().comp(plugin.configs().msg("flag","player",p.getName(),"check",type.display(),"detail",evidence,"vl",formatted)));
                plugin.getLogger().info("[FLAG] "+p.getName()+" "+type.display()+" VL "+formatted+" - "+evidence);
            }
        }
        if (d.debugTarget != null) {
            Player staff = Bukkit.getPlayer(d.debugTarget);
            if (staff != null) staff.sendMessage(plugin.configs().comp(plugin.configs().msg("debug","player",p.getName(),"check",type.display(),"detail",evidence)));
        }
        // No automatic punishments in this initial build; staff review is required.
    }
}