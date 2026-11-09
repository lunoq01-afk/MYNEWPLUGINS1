package dev.goldrush.anticheat.core;

/** Every detection GoldRushAC knows. Generated from one table so config, code and docs always agree. */
public enum CheckType {
    KILLAURA("combat", "killaura", "KillAura", 5.0, 60.0, 10.0, 0.9),
    AIMASSIST("combat", "aimassist", "AimAssist", 5.0, 60.0, 8.0, 0.7),
    AUTOCLICKER("combat", "autoclicker", "AutoClicker", 5.0, 45.0, 6.0, 0.7),
    REACH("combat", "reach", "Reach", 4.0, 60.0, 10.0, 0.9),
    HITBOX("combat", "hitbox", "Hitbox", 5.0, 60.0, 7.0, 0.7),
    CRITICALS("combat", "criticals", "Criticals", 5.0, 60.0, 7.0, 0.7),
    ANTIKNOCKBACK("combat", "antiknockback", "AntiKnockback", 4.0, 90.0, 9.0, 0.8),
    NOSWING("combat", "noswing", "NoSwing", 4.0, 60.0, 6.0, 0.7),
    MULTIAURA("combat", "multiaura", "MultiAura", 3.0, 60.0, 9.0, 0.8),
    ATTACKRATE("combat", "attackrate", "AttackRate", 4.0, 60.0, 6.0, 0.8),
    TARGETSWITCH("combat", "targetswitch", "TargetSwitch", 4.0, 60.0, 6.0, 0.5),
    FLY("movement", "fly", "Fly", 5.0, 60.0, 10.0, 0.9),
    SPEED("movement", "speed", "Speed", 5.0, 60.0, 8.0, 0.8),
    JESUS("movement", "jesus", "Jesus", 5.0, 60.0, 7.0, 0.8),
    SPIDER("movement", "spider", "Spider", 5.0, 60.0, 8.0, 0.8),
    NOFALL("movement", "nofall", "NoFall", 4.0, 90.0, 8.0, 0.8),
    LONGJUMP("movement", "longjump", "LongJump", 4.0, 60.0, 7.0, 0.7),
    HIGHJUMP("movement", "highjump", "HighJump", 4.0, 60.0, 7.0, 0.8),
    AIRJUMP("movement", "airjump", "AirJump", 4.0, 60.0, 8.0, 0.8),
    PHASE("movement", "phase", "Phase", 4.0, 90.0, 9.0, 0.6),
    STEP("movement", "step", "Step", 5.0, 60.0, 6.0, 0.7),
    TIMER("movement", "timer", "Timer", 5.0, 60.0, 8.0, 0.7),
    BADPACKETS("movement", "badpackets", "BadPackets", 2.0, 120.0, 10.0, 1.0),
    GROUNDSPOOF("movement", "groundspoof", "GroundSpoof", 5.0, 60.0, 6.0, 0.7),
    ACCELERATION("movement", "acceleration", "Acceleration", 6.0, 60.0, 4.0, 0.5),
    XRAY("world", "xray", "Xray", 3.0, 300.0, 6.0, 0.4),
    NUKER("world", "nuker", "Nuker", 4.0, 60.0, 9.0, 0.8),
    FASTBREAK("world", "fastbreak", "FastBreak", 5.0, 60.0, 7.0, 0.7),
    FASTPLACE("world", "fastplace", "FastPlace", 5.0, 60.0, 6.0, 0.7),
    SCAFFOLD("world", "scaffold", "Scaffold", 5.0, 60.0, 7.0, 0.6),
    BADPLACE("world", "badplace", "BadPlace", 3.0, 90.0, 8.0, 0.9),
    INTERACTREACH("world", "interactreach", "InteractReach", 4.0, 60.0, 8.0, 0.8),
    REMOTEBREAK("world", "remotebreak", "RemoteBreak", 3.0, 90.0, 9.0, 0.8),
    CHESTREACH("world", "chestreach", "ContainerReach", 3.0, 90.0, 7.0, 0.8),
    CHESTINTERACT("world", "chestinteract", "ChestInteraction", 4.0, 90.0, 7.0, 0.7),
    BADDIG("world", "baddig", "InvalidDigging", 5.0, 60.0, 6.0, 0.6),
    PROTECTEDBLOCK("world", "protectedblock", "ProtectedBlock", 1.0, 300.0, 10.0, 1.0),
    AUTOTOTEM("behavior", "autototem", "AutoTotem", 3.0, 300.0, 7.0, 0.6),
    AUTOARMOR("behavior", "autoarmor", "AutoArmor", 4.0, 120.0, 5.0, 0.6),
    INVENTORYMOVE("behavior", "inventorymove", "InventoryMove", 5.0, 60.0, 6.0, 0.7),
    FASTINVENTORY("behavior", "fastinventory", "FastInventory", 5.0, 60.0, 5.0, 0.7),
    CHESTSTEALER("behavior", "cheststealer", "ChestStealer", 4.0, 90.0, 6.0, 0.7),
    AUTOSOUP("behavior", "autosoup", "AutoSoup", 3.0, 300.0, 4.0, 0.4),
    PACKETSPAM("behavior", "packetspam", "PacketSpam", 3.0, 60.0, 6.0, 0.8),
    BOT("behavior", "bot", "Bot", 3.0, 300.0, 5.0, 0.5),
    FREECAM("freecam", "freecam", "Freecam", 3.0, 300.0, 9.0, 0.8),
    COMBATLOG("relog", "combatlog", "CombatLog", 3.0, 600.0, 7.0, 1.0),
    RELOGABUSE("relog", "relogabuse", "RelogAbuse", 3.0, 900.0, 8.0, 0.7),
    FREEZELOG("relog", "freezelog", "FreezeLog", 1.0, 1800.0, 10.0, 1.0),
    CHEATCLIENT("client", "cheatclient", "CheatClient", 1.0, 0.0, 50.0, 1.0);

    private final String module;
    private final String id;
    private final String display;
    private final double defaultThreshold;
    private final double defaultDecaySeconds;
    private final double severity;
    private final double reliability;

    CheckType(String module, String id, String display, double defaultThreshold, double defaultDecaySeconds,
              double severity, double reliability) {
        this.module = module;
        this.id = id;
        this.display = display;
        this.defaultThreshold = defaultThreshold;
        this.defaultDecaySeconds = defaultDecaySeconds;
        this.severity = severity;
        this.reliability = reliability;
    }

    public String module() { return module; }
    public String id() { return id; }
    public String display() { return display; }
    public double defaultThreshold() { return defaultThreshold; }
    public double defaultDecaySeconds() { return defaultDecaySeconds; }
    /** How much one full threshold of this detection weighs in the suspicion score. */
    public double severity() { return severity; }
    /** 0..1: how trustworthy a single observation of this detection is. */
    public double reliability() { return reliability; }
    /** Path inside detections.yml. */
    public String path() { return "detections." + module + "." + id; }

    public static CheckType byName(String s) {
        if (s == null) return null;
        for (CheckType t : values()) {
            if (t.name().equalsIgnoreCase(s) || t.display.equalsIgnoreCase(s)) return t;
        }
        return null;
    }
}
