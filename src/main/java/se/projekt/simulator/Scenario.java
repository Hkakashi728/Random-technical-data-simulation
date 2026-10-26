package se.projekt.simulator;

/**
 * En händelse som tvingar fram ett visst värde under en tid,
 * t.ex. "CPU på switch3 går upp till 95 % mellan sekund 60 och 120".
 * Används för att testa larm och för demo.
 *
 * Värdet går linjärt från 'from' till 'to' under perioden
 * (sätt from == to för ett konstant värde).
 */
public record Scenario(String deviceName, String metric,
                       int startSec, int endSec, double from, double to) {

    public boolean isActive(double t) {
        return t >= startSec && t < endSec;
    }

    public double valueAt(double t) {
        double progress = (t - startSec) / (endSec - startSec);
        return from + (to - from) * progress;
    }

    public String describe() {
        return String.format("%s: %s %.0f -> %.0f mellan %ds och %ds",
                deviceName, metric, from, to, startSec, endSec);
    }
}
