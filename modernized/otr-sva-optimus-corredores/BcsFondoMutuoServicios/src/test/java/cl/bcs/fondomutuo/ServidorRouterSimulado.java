package cl.bcs.fondomutuo;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;

/**
 * Simulated platform router for the tests of the H ports: a JDK {@link HttpServer} on a free loopback port that answers
 * {@code POST /internal/java-call} with a scripted reply per legacy class and records every request (path, token header, class, params).
 * Nothing real is called: no network beyond 127.0.0.1.
 */
public final class ServidorRouterSimulado implements AutoCloseable {

    public record Respuesta(int estado, String cuerpo, long demoraMs) {
        public static Respuesta ok(String json) {
            return new Respuesta(200, json, 0);
        }

        public static Respuesta estado(int estado) {
            return new Respuesta(estado, "{\"error\":\"x\"}", 0);
        }
    }

    public record Peticion(String metodo, String ruta, String token, String clase, JsonObject params, String cuerpoCrudo) {
    }

    private final HttpServer servidor;
    private final Map<String, Respuesta> guion = new ConcurrentHashMap<>();
    private final List<Peticion> recibidas = new CopyOnWriteArrayList<>();

    public ServidorRouterSimulado() {
        try {
            servidor = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        servidor.createContext("/", ex -> {
            String cuerpo = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String clase = null;
            JsonObject params = null;
            try {
                JsonObject o = JsonParser.parseString(cuerpo).getAsJsonObject();
                clase = o.get("class").getAsString();
                params = o.getAsJsonObject("params");
            } catch (RuntimeException ignorado) {
                // recorded with a null class: the test decides
            }
            recibidas.add(new Peticion(ex.getRequestMethod(), ex.getRequestURI().getPath(), ex.getRequestHeaders().getFirst("X-Internal-Token"), clase, params, cuerpo));
            Respuesta r = clase == null ? Respuesta.estado(400) : guion.getOrDefault(clase, Respuesta.estado(503));
            try {
                if (r.demoraMs() > 0) Thread.sleep(r.demoraMs());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            byte[] bytes = r.cuerpo().getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().add("Content-Type", "application/json");
            try {
                ex.sendResponseHeaders(r.estado(), bytes.length);
                ex.getResponseBody().write(bytes);
            } catch (IOException clienteSeFue) {
                // the client timed out and closed: expected in the timeout test
            } finally {
                ex.close();
            }
        });
        servidor.start();
    }

    public ServidorRouterSimulado responde(String clase, Respuesta r) {
        guion.put(clase, r);
        return this;
    }

    public String url() {
        return "http://127.0.0.1:" + servidor.getAddress().getPort();
    }

    public int puerto() {
        return servidor.getAddress().getPort();
    }

    public List<Peticion> recibidas() {
        return recibidas;
    }

    @Override
    public void close() {
        servidor.stop(0);
    }
}
