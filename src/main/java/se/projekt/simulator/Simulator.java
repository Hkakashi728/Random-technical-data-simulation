package se.projekt.simulator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
  Simulerar ett antal enheter och skickar deras mätvärden till NetXMS med jämna mellanrum.

  Exempel:
    Testläge (ingen server behövs): --dry-run
    Mot NetXMS: --server 127.0.0.1 --user admin
                                     (lösenordet läses från miljövariabeln NETXMS_PASSWORD)

  Alla flaggor:
    --server <ip>       NetXMS-serverns adress (standard 127.0.0.1)
    --port <nr>         NetXMS klientport (standard 4701)
    --user <namn>       NetXMS-användare (standard admin)
    --devices <antal>   antal fejk-enheter (standard 5)
    --interval <sek>    sekunder mellan varje utskick (standard 5)
    --dry-run           skriv bara ut värdena
 */
public class Simulator {

    /** Scenarierna upprepas med denna period, så att demon kan rulla hur länge som helst. */
    private static final int CYCLE_SEC = 360;

    public static void main(String[] args) throws Exception {
        Map<String, String> opt = parseArgs(args);
        String server = opt.getOrDefault("server", "127.0.0.1");
        int port = Integer.parseInt(opt.getOrDefault("port", "4701"));
        String user = opt.getOrDefault("user", "admin");
        int deviceCount = Integer.parseInt(opt.getOrDefault("devices", "5"));
        int intervalSec = Integer.parseInt(opt.getOrDefault("interval", "5"));
        boolean dryRun = opt.containsKey("dry-run");

        // 1. Skapa enheterna: switch1, switch2, ...
        Map<String, Device> devices = new LinkedHashMap<>();
        for (int i = 1; i <= deviceCount; i++) {
            String name = "switch" + i;
            devices.put(name, new Device(name, i)); // samma frö = samma beteende varje körning
        }

        // 2. Scenarier som triggar larm (ändra fritt!)
        List<Scenario> scenarios = List.of(
                new Scenario("switch2", Metrics.CPU, 60, 120, 95, 95),             // CPU-topp
                new Scenario("switch3", Metrics.MEMORY, 120, 300, 60, 98),         // minnesläcka, stiger sakta
                new Scenario("switch1", Metrics.TEMPERATURE, 200, 260, 85, 85));   // överhettning
        System.out.println("Scenarier (upprepas var " + CYCLE_SEC + ":e sekund):");
        scenarios.forEach(s -> System.out.println("  " + s.describe()));

        // 3. Välj mottagare: testläge eller riktig NetXMS
        DataSender sender;
        if (dryRun) {
            sender = new ConsoleSender();
        } else {
            String password = System.getenv("NETXMS_PASSWORD");
            if (password == null) {
                System.err.println("Sätt lösenordet i miljövariabeln NETXMS_PASSWORD (eller kör med --dry-run).");
                return;
            }
            sender = new NetxmsSender(server, port, user, password);
        }

        // 4. Huvudloopen
        try (sender) {
            sender.open();
            long start = System.currentTimeMillis();
            while (true) {
                double t = (System.currentTimeMillis() - start) / 1000.0;
                applyScenarios(scenarios, devices, t % CYCLE_SEC);

                List<DataSender.Value> values = new ArrayList<>();
                for (Device d : devices.values()) {
                    d.readMetrics(t).forEach((metric, value) ->
                            values.add(new DataSender.Value(d.getName(), metric, value)));
                }

                try {
                    sender.send(values);
                } catch (Exception e) {
                    // T.ex. om en nod eller DCI saknas i NetXMS. Fortsätt ändå.
                    System.err.println("Kunde inte skicka: " + e.getMessage());
                }
                Thread.sleep(intervalSec * 1000L);
            }
        }
    }

    /** Nollställ alla scenarier och lägg sedan på de som är aktiva just nu. */
    private static void applyScenarios(List<Scenario> scenarios, Map<String, Device> devices, double t) {
        for (Scenario s : scenarios) {
            Device d = devices.get(s.deviceName());
            if (d != null) {
                d.clearOverride(s.metric());
            }
        }
        for (Scenario s : scenarios) {
            Device d = devices.get(s.deviceName());
            if (d != null && s.isActive(t)) {
                d.setOverride(s.metric(), s.valueAt(t));
            }
        }
    }

    private static Map<String, String> parseArgs(String[] args) {
        Map<String, String> opt = new LinkedHashMap<>();
        for (int i = 0; i < args.length; i++) {
            if (!args[i].startsWith("--")) {
                continue;
            }
            String key = args[i].substring(2);
            boolean hasValue = i + 1 < args.length && !args[i + 1].startsWith("--");
            opt.put(key, hasValue ? args[++i] : "true");
        }
        return opt;
    }
}
