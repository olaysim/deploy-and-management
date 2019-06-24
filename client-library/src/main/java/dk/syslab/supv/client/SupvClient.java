package dk.syslab.supv.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import dk.syslab.supv.dto.RestError;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;

import java.io.IOException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;

/**
 * The SupvClient is the basic REST client that the SupvApi makes use of.<br />
 * This class has a convenience method getApi() that returns an instance of the SupvApi with an instantiated SupvClient.<br />
 * This class offers generic methods to GET, POST, DELETE and return the results as generic types.<br />
 * The class also offers methods that can return a Map&lt;String, T&gt;, which can normally be a little more tricky to parse,
 * to do this you need to supply a TypeReference describing the type as argument.
 */
@SuppressWarnings("Duplicates")
public class SupvClient {
    private CloseableHttpClient client;
    private ObjectMapper objectMapper;

    public SupvClient() {
        objectMapper = new ObjectMapper();
    }

    /**
     * Convenience method to create a SupvAPI object using the SupvClient
     * @return SupvAPI
     */
    public static SupvApi getApi() {
        SupvClient client = new SupvClient();
        SupvApi api = new SupvApi(client);
        return api;
    }

    /**
     * Create an Apache HttpClient (which accepts invalid SSL certificates)
     * @return a CloseableHttpClient that can be used to connect to endpoints using self-signed certificates and invalid hostnames in certificate
     */
    public CloseableHttpClient createHttpClient() {
        SSLConnectionSocketFactory socketFactory = null;
        try {
            socketFactory = new SSLConnectionSocketFactory(new SSLContextBuilder().loadTrustMaterial(null, new TrustAllStrategy()).build(), NoopHostnameVerifier.INSTANCE);
            return HttpClients.custom()
                    .setSSLSocketFactory(socketFactory)
                    .build();
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            throw new SecurityException("Unable to create HttpClient", e);
        }
    }

    private CloseableHttpClient getHttpClientInstance() {
        if (this.client == null) {
            this.client = createHttpClient();
        }
        return this.client;
    }

    /**
     * Performs a GET REST call to the specified URI and returns a result of the generic type. This method is a shorthand version without headers.
     * @param uri contains host and parameters
     * @param cls the class to use and return
     * @param <T> the type to map the JSON response to
     * @return the results of the JSON response from the server as the generic type
     * @throws IOException
     */
    public <T> T get(URI uri, Class<T> cls) throws IOException {
        return get(uri, cls, null);
    }
    /**
     * Performs a GET REST call to the specified URI and returns a result of the generic type.
     * @param uri contains host and parameters
     * @param cls the class to use and return
     * @param headers optional. headers for the GET call
     * @param <T> the type to map the JSON response to
     * @return the results of the JSON response from the server as the generic type
     * @throws IOException
     */
    public <T> T get(URI uri, Class<T> cls, List<Header> headers) throws IOException {
        CloseableHttpClient client = getHttpClientInstance();
        HttpGet httpGet = new HttpGet(uri);
        if (headers != null) {
            Header[] headerarr = new Header[headers.size()];
            headerarr = headers.toArray(headerarr);
            httpGet.setHeaders(headerarr);
        }
        CloseableHttpResponse response = client.execute(httpGet);
        try {
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return objectMapper.readValue(response.getEntity().getContent(), cls);
            } else {
                if (response.getEntity() != null) {
                    try {
                        RestError error = objectMapper.readValue(response.getEntity().getContent(), RestError.class);
                        throw new CommException(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase(), error.getMessage());
                    } catch (Exception e) {
                        // ignore: unable to map rest error, jut thow the more general version of the comm exception
                        if (e instanceof CommException)
                            throw e;
                    }
                }
                throw new CommException(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
            }
        } finally {
            response.close();
        }
    }

    /**
     * Performs a GET REST call to the specified URI and returns a result that contains a Map&lt;String, T&gt;.
     * The method handles Maps with a string as key and any type as value. The type is defined by a TypeReference that is supplied as an argument to the method.
     * @param uri contains host and parameters
     * @param typeRef the generic type that is the value part of the Map
     * @param headers optional. headers for the GET call
     * @param <T> the type to map the JSON response to
     * @return a Map containing the results of the JSON response from the server mapped to a Map&lt;String, T&gt;
     * @throws IOException
     */
    public <T> HashMap<String, T> getMap(URI uri, TypeReference<HashMap<String, T>> typeRef, List<Header> headers) throws IOException {
        CloseableHttpClient client = getHttpClientInstance();
        HttpGet httpGet = new HttpGet(uri);
        if (headers != null) {
            Header[] headerarr = new Header[headers.size()];
            headerarr = headers.toArray(headerarr);
            httpGet.setHeaders(headerarr);
        }
        CloseableHttpResponse response = client.execute(httpGet);
        try {
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HashMap<String, T> map = objectMapper.readValue(response.getEntity().getContent(), typeRef);
                return map;
            } else {
                if (response.getEntity() != null) {
                    try {
                        RestError error = objectMapper.readValue(response.getEntity().getContent(), RestError.class);
                        throw new CommException(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase(), error.getMessage());
                    } catch (Exception e) {
                        // ignore: unable to map rest error, jut thow the more general version of the comm exception
                        if (e instanceof CommException)
                            throw e;
                    }
                }
                throw new CommException(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
            }
        } catch (MismatchedInputException ex) {
            // most likely no data on server so it returns a [], which can't be parsed..
            // going to return an empty result here, this might turn out to be bad, we'll see
            return new HashMap<>();
        } finally {
            response.close();
        }
    }

    /**
     * Performs a POST REST call to the specified URI and returns a result of the generic type. This method is a shorthand version without headers.
     * @param uri contains host and parameters
     * @param cls the class to use and return
     * @param entity the request-body of the REST call, primarily contains the authentication token, but may contain more data as well
     * @param <T> the type to map the JSON response to
     * @return the results of the JSON response from the server as the generic type
     * @throws IOException
     */
    public <T> T post(URI uri, Class<T> cls, HttpEntity entity) throws IOException {
        return post(uri, cls, entity, null);
    }
    /**
     * Performs a POST REST call to the specified URI and returns a result of the generic type.
     * @param uri contains host and parameters
     * @param cls the class to use and return
     * @param entity the request-body of the REST call, primarily contains the authentication token, but may contain more data as well
     * @param headers optional. headers for the POST call
     * @param <T> the type to map the JSON response to
     * @return the results of the JSON response from the server as the generic type
     * @throws IOException
     */
    public <T> T post(URI uri, Class<T> cls, HttpEntity entity, List<Header> headers) throws IOException {
        CloseableHttpClient client = getHttpClientInstance();
        HttpPost httpPost = new HttpPost(uri);
        if (headers != null) {
            Header[] headerarr = new Header[headers.size()];
            headerarr = headers.toArray(headerarr);
            httpPost.setHeaders(headerarr);
        }
        if (entity != null) httpPost.setEntity(entity);
        CloseableHttpResponse response = client.execute(httpPost);
        try {
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return objectMapper.readValue(response.getEntity().getContent(), cls);
            } else {
                if (response.getEntity() != null) {
                    try {
                        RestError error = objectMapper.readValue(response.getEntity().getContent(), RestError.class);
                        throw new CommException(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase(), error.getMessage());
                    } catch (Exception e) {
                        // ignore: unable to map rest error, jut thow the more general version of the comm exception
                        if (e instanceof CommException)
                            throw e;
                    }
                }
                throw new CommException(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
            }
        } finally {
            response.close();
        }
    }

    /**
     * Performs a POST REST call to the specified URI and returns a result that contains a Map&lt;String, T&gt;.
     * The method handles Maps with a string as key and any type as value. The type is defined by a TypeReference that is supplied as an argument to the method.
     * @param uri contains host and parameters
     * @param typeRef the generic type that is the value part of the Map
     * @param entity the request-body of the REST call, primarily contains the authentication token, but may contain more data as well
     * @param headers optional. headers for the POST call
     * @param <T> the type to map the JSON response to
     * @return a Map containing the results of the JSON response from the server mapped to a Map&lt;String, T&gt;
     * @throws IOException
     */
    public <T> HashMap<String, T> postMap(URI uri, TypeReference<HashMap<String, T>> typeRef, HttpEntity entity, List<Header> headers) throws IOException {
        CloseableHttpClient client = getHttpClientInstance();
        HttpPost httpPost = new HttpPost(uri);
        if (headers != null) {
            Header[] headerarr = new Header[headers.size()];
            headerarr = headers.toArray(headerarr);
            httpPost.setHeaders(headerarr);
        }
        if (entity != null) httpPost.setEntity(entity);
        CloseableHttpResponse response = client.execute(httpPost);
        try {
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HashMap<String, T> map = objectMapper.readValue(response.getEntity().getContent(), typeRef);
                return map;
            } else {
                if (response.getEntity() != null) {
                    try {
                        RestError error = objectMapper.readValue(response.getEntity().getContent(), RestError.class);
                        throw new CommException(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase(), error.getMessage());
                    } catch (Exception e) {
                        // ignore: unable to map rest error, jut thow the more general version of the comm exception
                        if (e instanceof CommException)
                            throw e;
                    }
                }
                throw new CommException(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
            }
        } finally {
            response.close();
        }
    }

    /**
     * Performs an extended DELETE REST call to the specified URI and returns a result of the generic type. A normal DELETE call does not have a request-body, but the standard allows to a request-body to be present. This method includes a request-body which is used to contain the authentication token. This method is a shorthand version without headers.
     * @param uri contains host and parameters
     * @param cls the class to use and return
     * @param entity the request-body of the REST call, primarily contains the authentication token, but may contain more data as well
     * @param <T> the type to map the JSON response to
     * @return the results of the JSON response from the server as the generic type
     * @throws IOException
     */
    public <T> T delete(URI uri, Class<T> cls, HttpEntity entity) throws IOException {
        return delete(uri, cls, entity, null);
    }
    /**
     * Performs an extended DELETE REST call to the specified URI and returns a result of the generic type. A normal DELETE call does not have a request-body, but the standard allows to a request-body to be present. This method includes a request-body which is used to contain the authentication token.
     * @param uri contains host and parameters
     * @param cls the class to use and return
     * @param entity the request-body of the REST call, primarily contains the authentication token, but may contain more data as well
     * @param headers optional. headers for the POST call
     * @param <T> the type to map the JSON response to
     * @return the results of the JSON response from the server as the generic type
     * @throws IOException
     */
    public <T> T delete(URI uri, Class<T> cls, HttpEntity entity, List<Header> headers) throws IOException {
        CloseableHttpClient client = getHttpClientInstance();
        HttpDeleteWithBody httpDelete = new HttpDeleteWithBody(uri);
        if (headers != null) {
            Header[] headerarr = new Header[headers.size()];
            headerarr = headers.toArray(headerarr);
            httpDelete.setHeaders(headerarr);
        }
        if (entity != null) httpDelete.setEntity(entity);
        CloseableHttpResponse response = client.execute(httpDelete);
        try {
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return objectMapper.readValue(response.getEntity().getContent(), cls);
            } else {
                if (response.getEntity() != null) {
                    try {
                        RestError error = objectMapper.readValue(response.getEntity().getContent(), RestError.class);
                        throw new CommException(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase(), error.getMessage());
                    } catch (Exception e) {
                        // ignore: unable to map rest error, jut thow the more general version of the comm exception
                        if (e instanceof CommException)
                            throw e;
                    }
                }
                throw new CommException(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
            }
        } finally {
            response.close();
        }
    }

    /**
     * Performs an extended DELETE REST call to the specified URI and returns a result that contains a Map&lt;String, T&gt;.
     * The method handles Maps with a string as key and any type as value. The type is defined by a TypeReference that is supplied as an argument to the method.<br />
     * A normal DELETE call does not have a request-body, but the standard allows to a request-body to be present. This method includes a request-body which is used to contain the authentication token.
     * @param uri contains host and parameters
     * @param typeRef the generic type that is the value part of the Map
     * @param entity the request-body of the REST call, primarily contains the authentication token, but may contain more data as well
     * @param headers optional. headers for the DELETE call
     * @param <T> the type to map the JSON response to
     * @return a Map containing the results of the JSON response from the server mapped to a Map&lt;String, T&gt;
     * @throws IOException
     */
    public <T> HashMap<String, T> deleteMap(URI uri, TypeReference<HashMap<String, T>> typeRef, HttpEntity entity, List<Header> headers) throws IOException {
        CloseableHttpClient client = getHttpClientInstance();
        HttpDeleteWithBody httpDelete = new HttpDeleteWithBody(uri);
        if (headers != null) {
            Header[] headerarr = new Header[headers.size()];
            headerarr = headers.toArray(headerarr);
            httpDelete.setHeaders(headerarr);
        }
        if (entity != null) httpDelete.setEntity(entity);
        CloseableHttpResponse response = client.execute(httpDelete);
        try {
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HashMap<String, T> map = objectMapper.readValue(response.getEntity().getContent(), typeRef);
                return map;
            } else {
                if (response.getEntity() != null) {
                    try {
                        RestError error = objectMapper.readValue(response.getEntity().getContent(), RestError.class);
                        throw new CommException(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase(), error.getMessage());
                    } catch (Exception e) {
                        // ignore: unable to map rest error, jut thow the more general version of the comm exception
                        if (e instanceof CommException)
                            throw e;
                    }
                }
                throw new CommException(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
            }
        } finally {
            response.close();
        }
    }
}
