package dk.syslab.documentation;

import dk.syslab.supv.rpc.model.xmlrpc.ProcessStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;

@Component
public class Util {

    @Autowired
    private Environment env;

    private PrivateKey privateKey;

    public MockMvc setUp(WebApplicationContext context, JUnitRestDocumentation restDocumentation) {
        String privateKeyStr = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDJsvcQJXnqC1iwoAVg/sgVoAOT7zd+ayg5dGZJyYHTTFuIe4Nw+pigAYfKJ27r13EUcl/bvZbh1wDJ6uUzf0iA2uIyostgiZyyPh9JjsII3K41GRIbIQCeqIMoFCBHxq5Aj4T9KeOrXqRCpDzTSllr7r6OhGuNCXom3J3ObtPNTHhaHZUi0uqW+bD/lcWQV/D7rmtYbJoaJidEHa17pWtWu3L3wPQ7cKYF4yzc8MHXmP8JPD46Rs46iPkggxqb3B98jrYTsdSwYf6NVjPmsJxB3Xrn4erX/N9w5gVhrwwl7Q+TDzbjerjFEiZVFv7MLd4NDSpoWD5NOjA4pYJJOrOHAgMBAAECggEBAL8HZwO92seV03wFMtL4u5vvnzPfHtn2t3QjEDXWScVLzm1nPcglL6RrjqYhzuZDd+/7m1pNJ7fhn/edJ1MsvAh+RjnbAW//JY+TUqKiOJeYGs5jXkifayzZQM8WsLQKMkMnKrdzkxNdYYlUb99VzXdx+LyWIh7D2RMxY6x+Ps9ybCrBoOV0VW5HVoRAVZ5vAqwSvPHefTlF/lvp3iQyAuwN092FbnJEyAmacBKg3hggXa4Mfg4J290w/vhNApI8VAWVp0qm1lTOM3xTvHl+kjLCOQ8JprXOVH6Z1xCzSTacQk9WQ3LSUbtj/Pr5FUHuVwl/VKGOAqCnIS8+DXMq3cECgYEA8wjd9LJS+e+j2do4fclQUp5VjnBvBL9xPPMClORVvbKZRhSRS4RcoodDXLthwpXl+SRzDo8DUV4UkHZq/wQ5xhNkdrc9E2xjHPSmdre0z5zA+be5WpUhp/tyHTS1paNYacFv+BopqWMLHXmvgSnnDgqV3ke/dqrdt21nMBW63PECgYEA1HWTrtQ9FIy4v3cp6nsdKOf8f1irvBuhuNMfKZdq+AL5fA2rjjA7FBbrRANuJchPDIhALIx85kJlwiZYXsX0mLQuNid/oYi9EDEC2aDeqoOvh/i9i5YbTuXdiJhc/weo8bFI2lQgqZHc9uVC4cDIO0CYFevPdcVYXVlKQy7z9/cCgYAPFtzZBX1bYUKWExNw09/10TjUqCxsagp400m0vtyH85/KlxpSSvyJZsaK6mNudmoZ5uZx3/U/xwyVz5RjEvsXTjrtmADlxNP+hz4SnuNOBJiCqKKMejfluxnhsFUa3KJvpn/yMoWlq62T/eskpNpgp82YSHIdxwwPCG7gsGiysQKBgGiWcNsRs/fXxmLKwKGjdZlbilsORYxWdehMVfzdg1UP8cz11cwrRgn/tlVG8wQ6dV+P+zdy+VFIwlsvETWmcWBy4oTaMOROMfQ0jFih7rs2FET1WCnM/Sh70/CYQL9y/+HrChggbevL2GDaP3BuvKMeiz/PU9OAEJ3zhLFJ/ePFAoGBANa1u0LQU49PO1by1plzCGqyrMQrGBP1c2SdIbvNDmFO7nGsJvSzZ/P7Q1dYeUfjdMDj/I5Y+avyb539X3rELWvTFDjAeJ/bJqOE2TiKcPLeljM4wgutioRHVvAF9Q3GUpV0t5k7K733nL5QJnDxaV82FCD7b/a0hSLCXAJcFt1w";
        try {
            byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyStr);
            PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            privateKey = kf.generatePrivate(pkcs8EncodedKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            System.out.println("FATAL: " + e.getMessage());
            System.exit(-1);
        }

        int port = env.getRequiredProperty("server.test.port", int.class);
        return MockMvcBuilders.webAppContextSetup(context)
            .apply(documentationConfiguration(restDocumentation).uris()
                .withScheme("https")
                .withHost("syslab-xx")
                .withPort(port))
            .alwaysDo(document("{method-name}",
                preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint())))
            .build();
    }

    public Map<String, Object> getToken() {
        Map<String, Object> body = new HashMap<>();
        body.put("token", "<auth token>");
        return body;
    }

    public List<ProcessStatus> getProcessStatus() {
        List<ProcessStatus> list = new ArrayList<>();
        ProcessStatus ps1 = new ProcessStatus();
        ps1.setGroup("group");
        ps1.setStatus(1);
        ps1.setDescription("This is a description");
        ps1.setName("Program 1");
        ProcessStatus ps2 = new ProcessStatus();
        ps2.setGroup("group");
        ps2.setStatus(1);
        ps2.setDescription("This is a description");
        ps2.setName("Program 2");
        list.add(ps1);
        list.add(ps2);
        return list;
    }

    public Key getSigningKey() {
        return privateKey;
    }
}
