package dev.goldrush.anticheat.listener;

import dev.goldrush.anticheat.GoldRushAC;
import dev.goldrush.anticheat.core.*;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.*;
import java.util.Locale;

public final class MonitorListener implements Listener {
    private final GoldRushAC plugin;
    public MonitorListener(GoldRushAC plugin) { this.plugin=plugin; }
    private boolean exempt(Player p) {
        return p.hasPermission("goldrushac.bypass") || p.getGameMode()==GameMode.CREATIVE
          || p.getGameMode()==GameMode.SPECTATOR || p.isFlying() || p.isInsideVehicle() || p.isGliding() || p.isSwimming();
    }
    @EventHandler public void join(PlayerJoinEvent e) { plugin.data(e.getPlayer()); }
    @EventHandler public void quit(PlayerQuitEvent e) { plugin.data(e.getPlayer()).lastQuitMs=System.currentTimeMillis(); }
    @EventHandler(priority=EventPriority.MONITOR,ignoreCancelled=true)
    public void teleport(PlayerTeleportEvent e) {
        PlayerData d=plugin.data(e.getPlayer()); long now=System.currentTimeMillis(); d.lastTeleportMs=now;
        d.exemptUntil=now+plugin.configs().integer("anti-cheat.movement.teleport-exempt-ms",1800);
        d.addTeleport(new TeleportInfo(now,e.getFrom().clone(),e.getTo()==null?null:e.getTo().clone(),e.getCause().name()));
    }
    @EventHandler(priority=EventPriority.MONITOR,ignoreCancelled=true)
    public void velocity(PlayerVelocityEvent e) {
        PlayerData d=plugin.data(e.getPlayer()); d.lastVelocityMs=System.currentTimeMillis();
        d.exemptUntil=Math.max(d.exemptUntil,d.lastVelocityMs+plugin.configs().integer("anti-cheat.movement.velocity-exempt-ms",1200));
    }
    @EventHandler(priority=EventPriority.MONITOR,ignoreCancelled=true)
    public void move(PlayerMoveEvent e) {
        Location from=e.getFrom(), to=e.getTo(); if(to==null || (from.getX()==to.getX()&&from.getY()==to.getY()&&from.getZ()==to.getZ())) return;
        Player p=e.getPlayer(); PlayerData d=plugin.data(p); long now=System.currentTimeMillis();
        if(exempt(p)||now<d.exemptUntil||from.getWorld()!=to.getWorld()) { d.lastMoveMs=now; d.lastLocation=to.clone(); return; }
        double dx=to.getX()-from.getX(), dz=to.getZ()-from.getZ(), h=Math.hypot(dx,dz), dy=to.getY()-from.getY();
        d.hSpeed=h; d.lastMoveMs=now; d.lastLocation=to.clone();
        if(p.isOnGround()||p.getLocation().getBlock().isLiquid()) return;
        double maxH=plugin.configs().number("anti-cheat.movement.max-horizontal-per-tick",0.82);
        if(d.buffer("speed",h>maxH,5,0.35)) plugin.violations().flag(p,CheckType.SPEED,"repeated horizontal movement sample "+String.format(Locale.US,"%.3f",h),0.7);
        double maxY=plugin.configs().number("anti-cheat.movement.max-vertical-per-tick",0.92);
        if(d.buffer("highjump",dy>maxY,3,0.2)) plugin.violations().flag(p,CheckType.HIGHJUMP,"repeated upward movement sample "+String.format(Locale.US,"%.3f",dy),0.5);
    }
    @EventHandler(priority=EventPriority.MONITOR,ignoreCancelled=true)
    public void breakBlock(BlockBreakEvent e) {
        Player p=e.getPlayer(); if(exempt(p)) return;
        double dist=p.getEyeLocation().distance(e.getBlock().getLocation().add(.5,.5,.5));
        if(dist>plugin.configs().number("anti-cheat.world.remote-block-distance",6.0)
          && plugin.data(p).buffer("remote-break",true,2,.1))
            plugin.violations().flag(p,CheckType.REMOTEBREAK,"block break distance "+String.format(Locale.US,"%.2f",dist),1.0);
    }
    @EventHandler(priority=EventPriority.MONITOR,ignoreCancelled=true)
    public void placeBlock(BlockPlaceEvent e) {
        Player p=e.getPlayer(); if(exempt(p)) return;
        double dist=p.getEyeLocation().distance(e.getBlockPlaced().getLocation().add(.5,.5,.5));
        if(dist>6.0 && plugin.data(p).buffer("place-reach",true,2,.1))
            plugin.violations().flag(p,CheckType.BADPLACE,"block placement distance "+String.format(Locale.US,"%.2f",dist),.8);
        plugin.data(p).statAdd("blocks-placed",1);
    }
    @EventHandler(priority=EventPriority.MONITOR,ignoreCancelled=true)
    public void interact(PlayerInteractEvent e) {
        if(e.getAction()!=Action.RIGHT_CLICK_BLOCK||e.getClickedBlock()==null||exempt(e.getPlayer())) return;
        Player p=e.getPlayer(); double dist=p.getEyeLocation().distance(e.getClickedBlock().getLocation().add(.5,.5,.5));
        if(dist>6.0&&plugin.data(p).buffer("interact-reach",true,2,.1))
            plugin.violations().flag(p,CheckType.INTERACTREACH,"block interaction distance "+String.format(Locale.US,"%.2f",dist),.8);
    }
    @EventHandler(priority=EventPriority.MONITOR,ignoreCancelled=true)
    public void attack(EntityDamageByEntityEvent e) {
        if(!(e.getDamager() instanceof Player p)||exempt(p)) return;
        double dist=p.getEyeLocation().distance(e.getEntity().getLocation().add(0,e.getEntity().getHeight()*.5,0));
        if(dist>4.2&&plugin.data(p).buffer("reach",true,2,.1))
            plugin.violations().flag(p,CheckType.REACH,"attack distance "+String.format(Locale.US,"%.2f",dist),.8);
        plugin.data(p).statAdd("attacks",1);
    }
}