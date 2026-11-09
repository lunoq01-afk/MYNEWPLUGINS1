package dev.goldrush.anticheat.command;

import dev.goldrush.anticheat.GoldRushAC;
import dev.goldrush.anticheat.core.CheckType;
import dev.goldrush.anticheat.core.PlayerData;
import dev.goldrush.anticheat.core.Util;
import dev.goldrush.anticheat.core.ViolationRecord;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public final class StaffCommand implements CommandExecutor, TabCompleter {
    private final GoldRushAC plugin;
    public StaffCommand(GoldRushAC plugin) { this.plugin = plugin; }

    private void send(CommandSender sender, String key, Object... args) {
        sender.sendMessage(plugin.configs().comp(plugin.configs().msg(key, args)));
    }
    private Player find(String name) {
        Player p = Bukkit.getPlayerExact(name);
        return p;
    }
    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmd = command.getName().toLowerCase(Locale.ROOT);
        if (cmd.equals("sus")) {
            if (!sender.hasPermission("goldrushac.sus")) { send(sender, "no-permission"); return true; }
            if (args.length != 1) { send(sender, "usage-sus"); return true; }
            Player target = find(args[0]);
            if (target == null) { send(sender, "player-not-found"); return true; }
            showSus(sender, target);
            return true;
        }
        if (cmd.equals("alerts")) {
            if (!sender.hasPermission("goldrushac.alerts") || !(sender instanceof Player p)) {
                send(sender, "no-permission"); return true;
            }
            PlayerData d = plugin.data(p); d.alertsOn = !d.alertsOn;
            send(sender, d.alertsOn ? "alerts-on" : "alerts-off"); return true;
        }
        if (!sender.hasPermission("goldrushac.staff")) { send(sender, "no-permission"); return true; }
        if (args.length == 0) { send(sender, "usage-ac"); return true; }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "alerts" -> {
                if (!(sender instanceof Player p)) { send(sender, "player-only"); return true; }
                PlayerData d = plugin.data(p); d.alertsOn = !d.alertsOn;
                send(sender, d.alertsOn ? "alerts-on" : "alerts-off");
            }
            case "debug" -> {
                if (!(sender instanceof Player staff)) { send(sender, "player-only"); return true; }
                if (args.length < 2) { send(sender, "usage-ac"); return true; }
                Player target = find(args[1]);
                if (target == null) { send(sender, "player-not-found"); return true; }
                PlayerData td = plugin.data(target);
                if (staff.getUniqueId().equals(td.debugTarget)) {
                    td.debugTarget = null; staff.sendMessage("§eDebug disabled for " + target.getName());
                } else {
                    td.debugTarget = staff.getUniqueId(); staff.sendMessage("§aDebug enabled for " + target.getName());
                }
            }
            case "reload" -> {
                if (!sender.hasPermission("goldrushac.reload")) { send(sender, "no-permission"); return true; }
                plugin.reloadConfig(); plugin.configs().load(); send(sender, "reload");
            }
            case "info" -> send(sender, "info", "version", plugin.getDescription().getVersion(),
                    "tracked", plugin.players().size(), "checks", CheckType.values().length);
            default -> send(sender, "usage-ac");
        }
        return true;
    }

    private void showSus(CommandSender sender, Player target) {
        PlayerData d = plugin.data(target);
        double total = d.vl.values().stream().mapToDouble(Double::doubleValue).sum();
        int flags = d.counts.values().stream().mapToInt(Integer::intValue).sum();
        send(sender, "sus-header", "player", target.getName());
        send(sender, "sus-summary", "vl", Util.fmt(total, 1), "flags", flags);
        var sorted = d.vl.entrySet().stream().filter(e -> e.getValue() > 0)
                .sorted(Map.Entry.<CheckType, Double>comparingByValue().reversed()).limit(12).toList();
        if (sorted.isEmpty()) { send(sender, "sus-none"); return; }
        for (var entry : sorted) {
            CheckType type = entry.getKey();
            long last = d.lastFlag.getOrDefault(type, System.currentTimeMillis());
            sender.sendMessage(plugin.configs().comp(plugin.configs().msg("sus-check",
                    "check", type.display(), "vl", Util.fmt(entry.getValue(), 1),
                    "flags", d.count(type), "ago", Util.ago(System.currentTimeMillis() - last))));
        }
        ViolationRecord latest = d.last;
        if (latest != null) sender.sendMessage("§8Latest evidence: §7" + latest.type().display() +
                " §8» §f" + latest.evidence() + " §8(" + latest.world() + " " +
                (int) latest.x() + " " + (int) latest.y() + " " + (int) latest.z() + ")");
    }

    @Override public java.util.List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("sus") || (command.getName().equalsIgnoreCase("ac")
                && args.length == 2 && args[0].equalsIgnoreCase("debug"))) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                    .filter(n -> args[args.length - 1].isEmpty() || n.toLowerCase(Locale.ROOT)
                            .startsWith(args[args.length - 1].toLowerCase(Locale.ROOT))).toList();
        }
        if (command.getName().equalsIgnoreCase("ac") && args.length == 1)
            return java.util.List.of("alerts", "debug", "reload", "info").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase(Locale.ROOT))).toList();
        return java.util.List.of();
    }
}