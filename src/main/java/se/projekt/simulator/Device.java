package se.projekt.simulator;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

/**
 * En fejk-enhet (t.ex. en switch på ubåten).
 * Varje mätvärde svänger mjukt (sinusvåg) plus lite slumpbrus,
 * så att det ser ut som riktig data.
 */
public class Device {

    private final String name;
    private final Random random;

    // Varje enhet får egna "personlighetsvärden" så att de inte beter sig likadant
    private final double cpuMid, cpuSwing, cpuSpeed;
    private final double memMid, memSwing, memSpeed;
    private final double tempMid, tempSwing, tempSpeed;

    // Om ett scenario är aktivt skriver det över det normala värdet
    private final Map<String, Double> overrides = new LinkedHashMap<>();

    public Device(String name, long seed) {
        this.name = name;
        this.random = new Random(seed);
        cpuMid = 30 + random.nextInt(25);   cpuSwing = 10 + random.nextInt(20);  cpuSpeed = 0.02 + random.nextDouble() * 0.1;
        memMid = 40 + random.nextInt(20);   memSwing = 5 + random.nextInt(10);   memSpeed = 0.005 + random.nextDouble() * 0.02;
        tempMid = 40 + random.nextInt(10);  tempSwing = 3 + random.nextInt(5);   tempSpeed = 0.01 + random.nextDouble() * 0.03;
    }

    public String getName() {
        return name;
    }

    /** Räknar ut alla mätvärden vid tiden t (sekunder sedan start). */
    public Map<String, String> readMetrics(double t) {
        Map<String, String> values = new LinkedHashMap<>();
        values.put(Metrics.CPU, format(value(Metrics.CPU, cpuMid, cpuSwing, cpuSpeed, t, 0, 100)));
        values.put(Metrics.MEMORY, format(value(Metrics.MEMORY, memMid, memSwing, memSpeed, t, 0, 100)));
        values.put(Metrics.TEMPERATURE, format(value(Metrics.TEMPERATURE, tempMid, tempSwing, tempSpeed, t, 0, 120)));
        return values;
    }

    private double value(String metric, double mid, double swing, double speed, double t, double min, double max) {
        if (overrides.containsKey(metric)) {
            return overrides.get(metric);
        }
        double v = mid + swing * Math.sin(t * speed) + random.nextGaussian() * 2; // brus ±2
        return Math.max(min, Math.min(max, v));
    }

    public void setOverride(String metric, double value) {
        overrides.put(metric, value);
    }

    public void clearOverride(String metric) {
        overrides.remove(metric);
    }

    private static String format(double v) {
        return String.valueOf(Math.round(v * 10) / 10.0); // en decimal, punkt som decimaltecken
    }
}
