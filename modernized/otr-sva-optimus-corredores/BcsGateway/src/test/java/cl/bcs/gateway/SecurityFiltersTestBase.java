package cl.bcs.gateway;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import com.sun.net.httpserver.HttpServer;

/** A fake TomEE shared by the security tests: records the X-Forwarded-For it receives and answers with its own (hostile) X-Frame-Options. */
abstract class SecurityFiltersTestBase {

    static final AtomicReference<String> XFF = new AtomicReference<>();
    static final AtomicReference<String> PATH = new AtomicReference<>();
    static final HttpServer TOMEE;

    static {
        try {
            TOMEE = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            TOMEE.setExecutor(Executors.newCachedThreadPool());
            TOMEE.createContext("/", ex -> {
                var v = ex.getRequestHeaders().get("X-Forwarded-For");
                XFF.set(v == null ? "" : String.join(" | ", v));
                PATH.set(ex.getRequestURI().getRawPath());
                ex.getRequestBody().readAllBytes();
                byte[] b = "{\"from\":\"tomee\"}".getBytes(StandardCharsets.UTF_8);
                ex.getResponseHeaders().add("X-Frame-Options", "DENY");
                ex.sendResponseHeaders(200, b.length);
                ex.getResponseBody().write(b);
                ex.close();
            });
            TOMEE.start();
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    static String tomeeUrl() {
        return "http://127.0.0.1:" + TOMEE.getAddress().getPort();
    }
}
