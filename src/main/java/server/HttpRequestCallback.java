package server;



import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface HttpRequestCallback {
    public void call(HttpServletRequest req, HttpServletResponse res) throws IOException;
}