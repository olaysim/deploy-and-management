package dk.syslab.supv.client;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

import java.net.URI;

/**
 * A normal DELETE call does not have a request-body, but the standard allows to a request-body to be present.
 * This method includes a request-body which is primarily used to contain the authentication token.
 */
public class HttpDeleteWithBody extends HttpEntityEnclosingRequestBase {
    public final static String METHOD_NAME = "DELETE";

    public HttpDeleteWithBody() {
        super();
    }

    public HttpDeleteWithBody(final URI uri) {
        super();
        setURI(uri);
    }

    /**
     * @throws IllegalArgumentException if the uri is invalid.
     */
    public HttpDeleteWithBody(final String uri) {
        super();
        setURI(URI.create(uri));
    }

    @Override
    public String getMethod() {
        return METHOD_NAME;
    }
}
