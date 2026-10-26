package se.projekt.simulator;

import java.util.List;

import org.netxms.client.NXCSession;
import org.netxms.client.datacollection.DciPushData;

/**
 * Skickar mätvärden till NetXMS-servern med NetXMS Java-bibliotek (push).
 * Servern hittar rätt nod och DCI via namnen, så noderna och DCI:erna
 * måste redan finnas i NetXMS.
 */
public class NetxmsSender implements DataSender {

    private final String host;
    private final int port;
    private final String user;
    private final String password;
    private NXCSession session;

    public NetxmsSender(String host, int port, String user, String password) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
    }

    @Override
    public void open() throws Exception {
        session = new NXCSession(host, port);
        session.connect();
        session.login(user, password);
        System.out.println("Inloggad på NetXMS " + host + ":" + port + " som " + user);
    }

    @Override
    public void send(List<Value> values) throws Exception {
        DciPushData[] data = new DciPushData[values.size()];
        for (int i = 0; i < values.size(); i++) {
            Value v = values.get(i);
            data[i] = new DciPushData(v.deviceName(), v.metric(), v.value());
        }
        session.pushDciData(data); // allt skickas i ett enda meddelande
    }

    @Override
    public void close() {
        if (session != null) {
            session.disconnect();
        }
    }
}
