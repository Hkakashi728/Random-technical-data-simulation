package se.projekt.simulator;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/** Testläge: skriver ut värdena i stället för att skicka dem. Kräver ingen NetXMS-server. */
public class ConsoleSender implements DataSender {

    @Override
    public void open() {
        System.out.println("TESTLÄGE: inget skickas till NetXMS, värdena skrivs bara ut.");
    }

    @Override
    public void send(List<Value> values) {
        System.out.println("--- " + LocalTime.now().truncatedTo(ChronoUnit.SECONDS));
        for (Value v : values) {
            System.out.printf("  %-10s %-14s %s%n", v.deviceName(), v.metric(), v.value());
        }
    }

    @Override
    public void close() {
    }
}
