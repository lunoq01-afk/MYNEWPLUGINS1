package dev.goldrush.anticheat.listener;

import dev.goldrush.anticheat.GoldRushAC;
import dev.goldrush.anticheat.core.CheckType;
import dev.goldrush.anticheat.core.PlayerData;
import dev.goldrush.anticheat.core.TeleportInfo;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerAnimationEvent;

public final class MonitorListener implements Listener {
    private final GoldRushAC plugin;
    public MonitorListener(GoldRushAC plugin) { this.plugin = plugin; }

    private boolean exempt(Player p) {
        return p.hasPermission("goldrushac.bypass") || p.getGameMode() == GameMode.CREATIVE
                || p.getGameMode() == GameMode.SPECTATOR || p.isFlying() || p.isInsideVehicle()
                || p.isGliding() || p.isSwimming();
    }

    @EventHandler public void join(PlayerJoinEvent e) { plugin.data(e.getPlayer()); }

    @EventHandler public void quit(PlayerQuitEvent e) {
        PlayerData d = plugin.data(e.getPlayer());
        d.lastQuitMs = System.currentTimeMillis();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void teleport(PlayerTeleportEvent e) {
        PlayerData d = plugin.data(e.getPlayer());
        long now = System.currentTimeMillis();
        d.lastTeleportMs = now;
        d.exemptUntil = now + plugin.configs().integer("anti-cheat.movement.teleport-exempt-ms", 1800);
        d.addTeleport(new TeleportInfo(now, e.getFrom().clone(), e.getTo() == null ? null : e.getTo().clone(),
                e.getCause().name()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void velocity(PlayerVelocityEvent e) {
        PlayerData d = plugin.data(e.getPlayer());
        d.lastVelocityMs = System.currentTimeMillis();
        d.exemptUntil = Math.max(d.exemptUntil,
                d.lastVelocityMs + plugin.configs().integer("anti-cheat.movement.velocity-exempt-ms", 1200));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void move(PlayerMoveEvent e) {
        if (e.getTo() == null || (e.getFrom().getX() == e.getTo().getX()
                && e.getFrom().getY() == e.getTo().getY() && e.getFrom().getZ() == e.getTo().getZ())) return;
        Player p = e.getPlayer();
        PlayerData d = plugin.data(p);
        long now = System.currentTimeMillis();
        Location from = e.getFrom(), to = e.getTo();
        if (d.lastMoveMs > 0 && now < d.exemptUntil && !exempt(p)) {
            d.lastLocation = to.clone(); d.lastMoveMs = now; return;
        }
        double dx = to.getX() - from.getX(), dz = to.getZ() - from.getZ();
        double horizontal = Math.hypot(dx, dz);
        double dy = to.getY() - from.getY();
        d.hSpeed = horizontal; d.lastMoveMs = now; d.lastLocation = to.clone();
        if (exempt(p) || now < d.exemptUntil || from.getWorld() != to.getWorld()) return;
        if (p.isOnGround() || p.getLocation().getBlock().isLiquid()) return;
        double maxH = plugin.configs().number("anti-cheat.movement.max-horizontal-per-tick", 0.82);
        if (d.buffer("speed", horizontal > maxH, 5, 0.35)) {
            plugin.violations().flag(p, CheckType.SPEED, "sustained horizontal movement " +
                    String.format(java.util.Locale.US, "%.3f", horizontal), 0.7);
        }
        if (dy > plugin.configs().number("anti-cheat.movement.max-vertical-per-tick", 0.92)
                && d.buffer("highjump", true, 3, 0.2)) {
            plugin.violations().flag(p, CheckType.HIGHJUMP, "unexpected upward movement " +
                    String.format(java.util.Locale.US, "%.3f", dy), 0.5);
        } else {
            d.buffer("highjump", false, 3, 0.2);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void breakBlock(BlockBreakEvent e) {
        Player p = e.getPlayer();
        if (exempt(p)) return;
        Block b = e.getBlock();
        double distance = p.getEyeLocation().distance(b.getLocation().add(0.5, 0.5, 0.5));
        double max = plugin.configs().number("anti-cheat.world.remote-block-distance", 6.0);
        if (distance > max && plugin.data(p).buffer("remote-break", true, 2, 0.1)) {
            plugin.violations().flag(p, CheckType.REMOTEBREAK, "block break at " +
                    String.format(java.util.Locale.US, "%.2f", distance) + " blocks", 1.0);
        } else if (distance <= max) plugin.data(p).buffer("remote-break", false, 2, 0.1);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void placeBlock(BlockPlaceEvent e) {
        if (exempt(e.getPlayer())) return;
        Player p = e.getPlayer();
        long now = System.currentTimeMillis();
        PlayerData d = plugin.data(p);
        double distance = p.getEyeLocation().distance(e.getBlockPlaced().getLocation().add(0.5,0.5,0.5));
        if (distance > 6.0 && d.buffer("place-reach", true, 2, 0.1)) {
            plugin.violations().flag(p, CheckType.BADPLACE, "place distance " +
                    String.format(java.util.Locale.US, "%.2f", distance), 0.8);
        } else if (distance <= 6.0) d.buffer("place-reach", false, 2, 0.1);
        d.statAdd("blocks-placed", 1);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void interact(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getClickedBlock() == null || exempt(e.getPlayer())) return;
        Player p = e.getPlayer();
        double distance = p.getEyeLocation().distance(e.getClickedBlock().getLocation().add(0.5,0.5,0.5));
        if (distance > 6.0 && plugin.data(p).buffer("interact-reach", true, 2, 0.1)) {
            plugin.violations().flag(p, CheckType.INTERACTREACH, "block interaction distance " +
                    String.format(java.util.Locale.US, "%.2f", distance), 0.8);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void attack(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player p) || exempt(p)) return;
        Entity target = e.getEntity();
        double distance = p.getEyeLocation().distance(target.getLocation().add(0, target.getHeight() * 0.5, 0));
        if (distance > 4.2 && plugin.data(p).buffer("reach", true, 2, 0.1)) {
            plugin.violations().flag(p, CheckType.REACH, "attack distance " +
                    String.format(java.util.Locale.US, "%.2f", distance), 0.8);
        } else if (distance <= 4.2) plugin.data(p).buffer("reach", false, 2, 0.1);
        plugin.data(p).statAdd("attacks", 1);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void fallDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player p && e.getCause() == EntityDamageEvent.DamageCause.FALL
                && !exempt(p)) plugin.data(p).statAdd("fall-damage-events", 1);
    }

    @EventHandler public void inventory(InventoryClickEvent e) {
        if (e.getWhoClicked() instanceof Player p && p.isSprinting()) {
            // Inventory movement is recorded for telemetry only; vanilla/plugin inventory rules vary.
            plugin.data(p).statAdd("inventory-clicks-while-moving", 1);
        }
    }
}