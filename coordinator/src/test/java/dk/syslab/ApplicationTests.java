package dk.syslab;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URL;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = App.class)
@AutoConfigureWebClient
public class ApplicationTests {

    @Test
    public void contextLoads() {
    }


    @Test
    @Ignore
    public void connectoxmlrpc() throws Exception {
        XmlRpcClientConfigImpl config;
        XmlRpcClient client;

        config = new XmlRpcClientConfigImpl();
        config.setBasicUserName("N/A");
        config.setBasicPassword("N/A");
        config.setServerURL(new URL("http", "syslab-03.syslab.dk", 9901, "/RPC2"));

        client = new XmlRpcClient();
//        client.setTransportFactory(new XmlRpcCommonsTransportFactory(client));
        client.setConfig(config);

        String[] params = new String[]{};
        Object obj = client.execute("supervisor.getAPIVersion", params);

        int i = 0;
    }

}
