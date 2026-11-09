package dev.goldrush.anticheat.core;

/** One recorded detection hit. */
public record ViolationRecord(long time, CheckType type, double vlAfter, String evidence, String world,
                              double x, double y, double z, int ping) { }
