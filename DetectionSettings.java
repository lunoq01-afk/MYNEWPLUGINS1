package dev.goldrush.anticheat.config;

import java.util.List;

/** The settings of one detection, read from detections.yml. */
public record DetectionSettings(boolean enabled, double threshold, double decay, boolean alert, boolean debug,
                                int minFlags, List<Step> steps) {

    /** One punishment escalation step: at violation level vl, do action. */
    public record Step(double vl, String action, long durationMs) { }
}
