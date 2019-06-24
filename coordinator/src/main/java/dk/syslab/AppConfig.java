package dk.syslab;


import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

@Configuration
public class AppConfig {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

//    @Bean
//    public EmbeddedServletContainerFactory servletContainer(Environment env) {
//        UndertowEmbeddedServletContainerFactory undertow = new UndertowEmbeddedServletContainerFactory();
//        undertow.addBuilderCustomizers(new UndertowBuilderCustomizer() {
//            @Override
//            public void customize(Undertow.Builder builder) {
//                builder.addHttpListener(Integer.parseInt(env.getRequiredProperty("server.http.port")), "0.0.0.0");
//                builder.setIoThreads(1);
//                builder.setWorkerThreads(2);
//            }
//        });
//        undertow.addDeploymentInfoCustomizers(deploymentInfo -> {
//            deploymentInfo.addSecurityConstraint(new SecurityConstraint()
//                .addWebResourceCollection(new WebResourceCollection()
//                .addUrlPattern("/api/auth").addUrlPattern("/login"))
//                .setTransportGuaranteeType(TransportGuaranteeType.CONFIDENTIAL)
//                .setEmptyRoleSemantic(SecurityInfo.EmptyRoleSemantic.PERMIT))
//            .setConfidentialPortManager(exchange -> Integer.parseInt(env.getRequiredProperty("server.port")));
//        });
//        return undertow;
//    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(new SSLContextBuilder().loadTrustMaterial(null, new TrustAllStrategy()).build(), NoopHostnameVerifier.INSTANCE);
        CloseableHttpClient httpClient = HttpClients.custom()
            .setSSLSocketFactory(socketFactory)
            .build();
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        clientHttpRequestFactory.setBufferRequestBody(false);
        clientHttpRequestFactory.setHttpClient(httpClient);
        RestTemplate restTemplate = builder.build();
        restTemplate.setRequestFactory(clientHttpRequestFactory);
        return restTemplate;
    }
}
