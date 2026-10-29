package cl.bcs.plataforma.internal;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/** Local HTTP server that records every request and answers a fixed status/body (optionally after a delay). */
public final class Falso implements AutoCloseable {

    public record Pedido(String metodo, String uri, Map<String, List<String>> cabeceras, String cuerpo) {
        public String cabecera(String n) {
            for (var e : cabeceras.entrySet()) {
                if (e.getKey().equalsIgnoreCase(n)) return e.getValue().get(0);
            }
            return null;
        }
    }

    public final List<Pedido> pedidos = new CopyOnWriteArrayList<>();
    private final HttpServer server;
    public volatile int status = 200;
    public volatile String respuesta = "{}";
    public volatile long demoraMs = 0;

    public Falso() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", ex -> {
            String cuerpo = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            pedidos.add(new Pedido(ex.getRequestMethod(), ex.getRequestURI().toString(), new java.util.HashMap<>(ex.getRequestHeaders()), cuerpo));
            try {
                if (demoraMs > 0) Thread.sleep(demoraMs);
            } catch (InterruptedException ignorado) {
                Thread.currentThread().interrupt();
            }
            byte[] b = respuesta.getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().add("Content-Type", "application/json");
            ex.sendResponseHeaders(status, b.length == 0 ? -1 : b.length);
            if (b.length > 0) ex.getResponseBody().write(b);
            ex.close();
        });
        server.start();
    }

    public String url() {
        return "http://127.0.0.1:" + server.getAddress().getPort();
    }

    /** A URL where nothing listens: connection refused. */
    public static String urlCerrada() throws IOException {
        try (ServerSocket s = new ServerSocket(0, 1, java.net.InetAddress.getByName("127.0.0.1"))) {
            return "http://127.0.0.1:" + s.getLocalPort();
        }
    }

    public List<Pedido> copia() {
        return new ArrayList<>(pedidos);
    }

    @Override
    public void close() {
        server.stop(0);
    }
}
