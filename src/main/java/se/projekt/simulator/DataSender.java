package se.projekt.simulator;

import java.util.List;

/**
 * Något som kan ta emot mätvärden.
 * Två versioner finns: en som skickar till NetXMS och en som bara skriver ut (testläge).
 */
public interface DataSender extends AutoCloseable {

    /** Ett mätvärde för en enhet. */
    record Value(String deviceName, String metric, String value) {
    }

    void open() throws Exception;

    void send(List<Value> values) throws Exception;

    @Override
    void close();
}
