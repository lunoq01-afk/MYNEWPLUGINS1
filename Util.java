package dev.goldrush.anticheat.core;

import org.bukkit.Location;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/** Small helpers shared by everything. */
public final class Util {

    private Util() { }

    public static int floor(double v) {
        return (int) Math.floor(v);
    }

    /** Wraps an angle difference into -180..180. */
    public static double wrap(double a) {
        a %= 360;
        if (a > 180) a -= 360;
        if (a < -180) a += 360;
        return a;
    }

    public static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    public static String fmt(double v, int decimals) {
        return String.format(Locale.US, "%." + decimals + "f", v);
    }

    /** 5s, 3m, 2h, 1d: how long ago something was. */
    public static String ago(long ms) {
        long s = Math.max(0, ms / 1000);
        if (s < 60) return s + "s";
        if (s < 3600) return (s / 60) + "m";
        if (s < 86400) return (s / 3600) + "h";
        return (s / 86400) + "d";
    }

    /** "2d 3h", "45m", "30s". */
    public static String dur(long ms) {
        long s = Math.max(0, ms / 1000);
        long d = s / 86400, h = (s % 86400) / 3600, m = (s % 3600) / 60;
        if (d > 0) return d + "d " + h + "h";
        if (h > 0) return h + "h " + m + "m";
        if (m > 0) return m + "m";
        return s + "s";
    }

    /** Milliseconds for 30m / 12h / 15d / 2w. 0 = permanent ("perm"). -1 = invalid. */
    public static long parseDuration(String s) {
        if (s == null) return -1;
        String v = s.toLowerCase(Locale.ROOT).trim();
        if (v.equals("perm") || v.equals("permanent") || v.equals("0")) return 0;
        if (v.length() < 2) return -1;
        try {
            long n = Long.parseLong(v.substring(0, v.length() - 1));
            if (n <= 0) return -1;
            return switch (v.charAt(v.length() - 1)) {
                case 's' -> n * 1000L;
                case 'm' -> n * 60_000L;
                case 'h' -> n * 3_600_000L;
                case 'd' -> n * 86_400_000L;
                case 'w' -> n * 7 * 86_400_000L;
                default -> -1;
            };
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    public static String where(Location l) {
        if (l == null || l.getWorld() == null) return "unknown";
        return l.getWorld().getName() + " " + l.getBlockX() + " " + l.getBlockY() + " " + l.getBlockZ();
    }

    /** First 16 hex characters of SHA-256. Used so IP addresses are never stored in plain text. */
    public static String sha(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] h = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 8; i++) sb.append(String.format("%02x", h[i]));
            return sb.toString();
        } catch (Exception ex) {
            return Integer.toHexString(s.hashCode());
        }
    }

    /** a=1.5,b=2 */
    public static String kv(Map<String, Double> m) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Double> e : m.entrySet()) {
            if (sb.length() > 0) sb.append(',');
            sb.append(e.getKey()).append('=').append(fmt(e.getValue(), 3));
        }
        return sb.toString();
    }

    public static Map<String, Double> parseKv(String s) {
        Map<String, Double> out = new LinkedHashMap<>();
        if (s == null || s.isBlank()) return out;
        for (String part : s.split(",")) {
            int i = part.indexOf('=');
            if (i <= 0) continue;
            try {
                out.put(part.substring(0, i), Double.parseDouble(part.substring(i + 1)));
            } catch (NumberFormatException ignored) {
                // skip a damaged entry
            }
        }
        return out;
    }
}
