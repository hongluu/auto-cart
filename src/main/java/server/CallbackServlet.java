package server;

import java.io.IOException;

import javax.servlet.ServletException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CallbackServlet extends HttpServlet {
    /**
	 * 
	 */
	private static final long serialVersionUID = 6001313133709614181L;
	private CallbackHttpServer server;

    CallbackServlet(CallbackHttpServer server) {
        this.server = server;
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        if (server.getHttpHandler("GET") != null) {
            server.getHttpHandler("GET").call(req, res);
        } else {
            super.doGet(req, res);
        }
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        if (server.getHttpHandler("POST") != null) {
            server.getHttpHandler("POST").call(req, res);
        } else {
            super.doPost(req, res);
        }
    }
}