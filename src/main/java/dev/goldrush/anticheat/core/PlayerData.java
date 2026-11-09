package dev.goldrush.anticheat.core;

import org.bukkit.Location;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Everything GoldRushAC remembers about one player: violations, buffers, statistics, staff preferences.
 * Detection modules keep their own per-player working state; this holds what is shared or shown in /sus.
 * Not tied to a Player object, so it can also describe offline players loaded from the database.
 */
public final class PlayerData {

    public final UUID uuid;
    public volatile String name;
    public final long joinMs = System.currentTimeMillis();

    // violations
    public final EnumMap<CheckType, Double> vl = new EnumMap<>(CheckType.class);
    public final EnumMap<CheckType, Integer> counts = new EnumMap<>(CheckType.class);
    public final EnumMap<CheckType, Long> lastFlag = new EnumMap<>(CheckType.class);
    public final EnumMap<CheckType, Long> lastAlert = new EnumMap<>(CheckType.class);
    public final EnumMap<CheckType, Long> lastPunish = new EnumMap<>(CheckType.class);
    public final EnumMap<CheckType, Integer> stepApplied = new EnumMap<>(CheckType.class);
    public final Deque<ViolationRecord> recent = new ArrayDeque<>();
    public ViolationRecord last;

    // generic buffers and statistics
    private final Map<String, Double> buffers = new HashMap<>();
    private final Map<String, Double> stats = new HashMap<>();

    // shared state written by StateListener and the modules
    public final Deque<TeleportInfo> teleports = new ArrayDeque<>();
    public volatile long exemptUntil;        // movement/combat grace after teleports, velocity, respawn...
    public volatile long lastVelocityMs;
    public volatile long lastTeleportMs;
    public volatile long combatUntil;        // combat tag
    public volatile long lastCombatMs;
    public volatile double hSpeed;           // horizontal blocks/packet at the last movement packet
    public volatile long lastMoveMs;         // when the last position change arrived
    public Location lastLocation;

    // staff preferences (stored in the database)
    public boolean alertsOn = true;
    public boolean verbose = false;
    public boolean notifyOn = true;
    public boolean bypass = false;           // /ac bypass
    public UUID debugTarget;                 // staff member is debugging this player

    // client information
    public String brand;
    public final Set<String> channels = new HashSet<>();

    // relog information
    public int combatLogs;
    public long lastQuitMs;

    public PlayerData(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public double vl(CheckType t) {
        return vl.getOrDefault(t, 0.0);
    }

    public int count(CheckType t) {
        return counts.getOrDefault(t, 0);
    }

    /**
     * A leaky counter: a bad sample adds 1, a good sample removes 'decay'. Returns true when the counter reaches
     * the limit (and then drops back to half so a continuing cheat keeps being reported).
     */
    public boolean buffer(String key, boolean bad, double limit, double decay) {
        double b = buffers.getOrDefault(key, 0.0);
        b = bad ? b + 1 : Math.max(0, b - decay);
        boolean hit = b >= limit;
        buffers.put(key, hit ? limit / 2 : b);
        return hit;
    }

    public void clearBuffers() {
        buffers.clear();
    }

    public double stat(String key) {
        return stats.getOrDefault(key, 0.0);
    }

    public void stat(String key, double v) {
        stats.put(key, v);
    }

    public void statAdd(String key, double dv) {
        stats.merge(key, dv, Double::sum);
    }

    public void statMax(String key, double v) {
        stats.merge(key, v, Math::max);
    }

    public Map<String, Double> allStats() {
        return stats;
    }

    public void addRecent(ViolationRecord r) {
        recent.addLast(r);
        while (recent.size() > 60) recent.pollFirst();
        last = r;
    }

    public void addTeleport(TeleportInfo t) {
        teleports.addLast(t);
        while (teleports.size() > 8) teleports.pollFirst();
    }
}
