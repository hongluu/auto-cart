package server;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;

public class CallbackHttpServer {

    protected Server server;
    protected final Map<String, HttpRequestCallback> httpReqCallbacks = new HashMap<String, HttpRequestCallback>();

    public CallbackHttpServer() {
        // Default HTTP GET request callback: returns files in "test/fixture"
        setHttpHandler("GET", new GetFixtureHttpRequestCallback());
    }

    public HttpRequestCallback getHttpHandler(String httpMethod) {
        return httpReqCallbacks.get(httpMethod.toUpperCase());
    }

    public void setHttpHandler(String httpMethod, HttpRequestCallback getHandler) {
        httpReqCallbacks.put(httpMethod.toUpperCase(), getHandler);
    }

    public void start() throws Exception {
        server = new Server(0);
        Context context = new Context(server, "/");
        addServlets(context);
        server.start();
    }

    public void stop() throws Exception {
        server.stop();
    }

    protected void addServlets(Context context) {
        context.addServlet(new ServletHolder(new CallbackServlet(this)), "/*");
    }

    public int getPort() {
        return server.getConnectors()[0].getLocalPort();
    }

    public String getBaseUrl() {
        return format("http://localhost:%d/", getPort());
    }
}