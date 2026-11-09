package dev.goldrush.anticheat.command;

import dev.goldrush.anticheat.GoldRushAC;
import dev.goldrush.anticheat.core.*;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import java.util.Locale;
import java.util.Map;

public final class StaffCommand implements CommandExecutor, TabCompleter {
    private final GoldRushAC plugin;
    public StaffCommand(GoldRushAC plugin){this.plugin=plugin;}
    private void send(CommandSender s,String key,Object... args){s.sendMessage(plugin.configs().comp(plugin.configs().msg(key,args)));}
    @Override public boolean onCommand(CommandSender s,Command c,String label,String[] a){
        String cmd=c.getName().toLowerCase(Locale.ROOT);
        if(cmd.equals("sus")){
            if(!s.hasPermission("goldrushac.sus")){send(s,"no-permission");return true;}
            if(a.length!=1){send(s,"usage-sus");return true;}
            Player t=Bukkit.getPlayerExact(a[0]);if(t==null){send(s,"player-not-found");return true;}
            PlayerData d=plugin.data(t);double total=d.vl.values().stream().mapToDouble(Double::doubleValue).sum();
            int count=d.counts.values().stream().mapToInt(Integer::intValue).sum();
            send(s,"sus-header","player",t.getName());send(s,"sus-summary","vl",Util.fmt(total,1),"flags",count);
            var entries=d.vl.entrySet().stream().filter(x->x.getValue()>0).sorted(Map.Entry.<CheckType,Double>comparingByValue().reversed()).limit(12).toList();
            if(entries.isEmpty())send(s,"sus-none");
            for(var e:entries){CheckType type=e.getKey();long last=d.lastFlag.getOrDefault(type,System.currentTimeMillis());
                send(s,"sus-check","check",type.display(),"vl",Util.fmt(e.getValue(),1),"flags",d.count(type),"ago",Util.ago(System.currentTimeMillis()-last));}
            if(d.last!=null)s.sendMessage("§8Latest evidence: §7"+d.last.type().display()+" §8» §f"+d.last.evidence());
            return true;
        }
        if(cmd.equals("alerts")){
            if(!(s instanceof Player p)||!s.hasPermission("goldrushac.alerts")){send(s,"no-permission");return true;}
            PlayerData d=plugin.data(p);d.alertsOn=!d.alertsOn;send(s,d.alertsOn?"alerts-on":"alerts-off");return true;
        }
        if(!s.hasPermission("goldrushac.staff")){send(s,"no-permission");return true;}
        if(a.length==0){send(s,"usage-ac");return true;}
        switch(a[0].toLowerCase(Locale.ROOT)){
            case "alerts"->{if(!(s instanceof Player p)){send(s,"player-only");break;}PlayerData d=plugin.data(p);d.alertsOn=!d.alertsOn;send(s,d.alertsOn?"alerts-on":"alerts-off");}
            case "debug"->{if(!(s instanceof Player staff)){send(s,"player-only");break;}if(a.length<2){send(s,"usage-ac");break;}Player target=Bukkit.getPlayerExact(a[1]);if(target==null){send(s,"player-not-found");break;}PlayerData d=plugin.data(target);if(staff.getUniqueId().equals(d.debugTarget)){d.debugTarget=null;staff.sendMessage("§eDebug disabled.");}else{d.debugTarget=staff.getUniqueId();staff.sendMessage("§aDebug enabled for "+target.getName());}}
            case "reload"->{if(!s.hasPermission("goldrushac.reload")){send(s,"no-permission");break;}plugin.reloadConfig();plugin.configs().load();send(s,"reload");}
            case "info"->send(s,"info","version",plugin.getDescription().getVersion(),"tracked",plugin.players().size(),"checks",CheckType.values().length);
            default->send(s,"usage-ac");
        }return true;
    }
    @Override public java.util.List<String> onTabComplete(CommandSender s,Command c,String alias,String[] a){
        if(c.getName().equalsIgnoreCase("sus")||(c.getName().equalsIgnoreCase("ac")&&a.length==2&&a[0].equalsIgnoreCase("debug")))
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(n->n.toLowerCase(Locale.ROOT).startsWith(a[a.length-1].toLowerCase(Locale.ROOT))).toList();
        if(c.getName().equalsIgnoreCase("ac")&&a.length==1)return java.util.List.of("alerts","debug","reload","info").stream().filter(x->x.startsWith(a[0].toLowerCase(Locale.ROOT))).toList();
        return java.util.List.of();
    }
}