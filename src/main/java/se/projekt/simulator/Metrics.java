package se.projekt.simulator;

/**
 * Namnen på mätvärdena.
 * VIKTIGT: varje namn måste finnas som en DCI med källan "Push" på noden i NetXMS,
 * med exakt samma namn.
 */
public final class Metrics {
    public static final String CPU = "cpu.usage";            // procent 0–100
    public static final String MEMORY = "memory.usage";      // procent 0–100
    public static final String TEMPERATURE = "temperature";  // grader Celsius

    private Metrics() {
    }
}
