package dev.goldrush.anticheat.core;

import org.bukkit.Location;

/** A recent teleport, kept for /sus and for exempting movement checks. */
public record TeleportInfo(long time, Location from, Location to, String cause) { }
