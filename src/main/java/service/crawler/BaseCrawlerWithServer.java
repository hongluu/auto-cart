package service.crawler;

import org.junit.After;
import org.junit.Before;

import server.CallbackHttpServer;

abstract public class BaseCrawlerWithServer extends BaseCrawler {
    protected static CallbackHttpServer server;

    @Before
    public static void startServer() throws Exception {
        server = new CallbackHttpServer();
        server.start();
    }

    @After
    public void stopServer() throws Exception {
        server.stop();
    }
}