package cl.bcs.contabilidad.jobs.support;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * COPY of BcsContabilidadServicios src/test/java/cl/bcs/contabilidad/envio/support/FakeSapServiceLayer.java (test code,
 * JDK only), so this module replays the same recorded SAP scripts. Keep both copies identical.
 *
 * Scriptable stand-in for the SAP Business One Service Layer ({@code /b1s/v1/...}) on a raw
 * {@link ServerSocket} (JDK only, HTTP/1.1, {@code Connection: close}).
 *
 * <p>Why not WireMock / com.sun.net.httpserver: WireMock 3.x pulls Jetty 11 while the Spring Boot 4
 * BOM manages Jetty 12; and the JDK HttpServer rewrites header names to "Set-cookie", which breaks
 * legacy {@code CallerWSJournalVoucher.java:90-92} (case-sensitive {@code get("Set-Cookie")}). Real
 * Service Layer sends "Set-Cookie", so this fake writes header names exactly as given.
 *
 * <p>The same class drives the legacy oracle (src/test/resources/envio/legacy-oracle), so requests
 * from legacy {@code CallerWSJournalVoucher} and from the modern {@code SapB1Gateway} are recorded the
 * same way.
 *
 * <p>Routes are keyed {@code "METHOD /decoded/path"}, e.g. {@code "GET /b1s/v1/BusinessPartners('CL1-9')"}.
 * Each route holds a queue of responses; the last one repeats. An unscripted route answers 599 so a
 * missing script is never mistaken for success. All session values used with it are fake.
 */
public final class FakeSapServiceLayer implements AutoCloseable {

    /** A scripted HTTP response. */
    public record Respuesta(int status, String body, List<String> setCookies) {
        public static Respuesta of(int status, String body) {
            return new Respuesta(status, body, List.of());
        }
    }

    /** A recorded request. {@code cookie} is every Cookie header value joined with "; " (null if none). */
    public record Peticion(String metodo, String ruta, String cookie, String contentType, String cuerpo) {
        public String clave() {
            return metodo + " " + ruta;
        }
    }

    private final ServerSocket server;
    private final Thread acceptor;
    private final Map<String, Deque<Respuesta>> guion = new LinkedHashMap<>();
    private final List<Peticion> peticiones = Collections.synchronizedList(new ArrayList<>());
    private volatile boolean abierto = true;

    public FakeSapServiceLayer() throws IOException {
        server = new ServerSocket(0, 50, InetAddress.getLoopbackAddress());
        acceptor = Thread.ofPlatform().daemon().name("fake-sap-sl").start(this::aceptar);
    }

    /** Base URL to configure as the Service Layer URL (no trailing slash). */
    public String url() {
        return "http://127.0.0.1:" + server.getLocalPort();
    }

    /** Appends responses for a route; the last appended response repeats forever. */
    public synchronized FakeSapServiceLayer cuando(String metodoYRuta, Respuesta... respuestas) {
        Deque<Respuesta> q = guion.computeIfAbsent(metodoYRuta, k -> new ArrayDeque<>());
        Collections.addAll(q, respuestas);
        return this;
    }

    public List<Peticion> peticiones() {
        synchronized (peticiones) {
            return List.copyOf(peticiones);
        }
    }

    public long contar(String metodoYRuta) {
        return peticiones().stream().filter(p -> p.clave().equals(metodoYRuta)).count();
    }

    private void aceptar() {
        while (abierto) {
            try {
                Socket s = server.accept();
                Thread.ofVirtual().start(() -> atender(s));
            } catch (IOException e) {
                if (abierto) throw new IllegalStateException("fake SAP accept failed", e);
            }
        }
    }

    private void atender(Socket s) {
        try (s; InputStream in = s.getInputStream(); OutputStream out = s.getOutputStream()) {
            String requestLine = linea(in);
            if (requestLine == null || requestLine.isBlank()) return;
            String[] rl = requestLine.split(" ");
            Map<String, List<String>> headers = new LinkedHashMap<>();
            String h;
            while ((h = linea(in)) != null && !h.isEmpty()) {
                int i = h.indexOf(':');
                headers.computeIfAbsent(h.substring(0, i).trim().toLowerCase(Locale.ROOT), k -> new ArrayList<>())
                        .add(h.substring(i + 1).trim());
            }
            byte[] body = leerCuerpo(in, headers);
            String ruta = URI.create(rl[1].replace("'", "%27")).getPath();
            List<String> cookies = headers.get("cookie");
            String cuerpo = new String(body, StandardCharsets.UTF_8);
            Peticion p = new Peticion(rl[0], ruta, cookies == null ? null : String.join("; ", cookies),
                    headers.containsKey("content-type") ? headers.get("content-type").get(0) : null,
                    cuerpo.isEmpty() ? null : cuerpo);
            peticiones.add(p);

            Respuesta r;
            synchronized (this) {
                Deque<Respuesta> q = guion.get(p.clave());
                if (q == null || q.isEmpty()) {
                    r = Respuesta.of(599, "{\"fake\":\"no script for " + p.clave().replace("\"", "'") + "\"}");
                } else {
                    r = q.size() > 1 ? q.pollFirst() : q.peekFirst();
                }
            }
            byte[] bytes = r.body() == null || r.status() == 204 ? new byte[0] : r.body().getBytes(StandardCharsets.UTF_8);
            StringBuilder resp = new StringBuilder("HTTP/1.1 ").append(r.status()).append(" Fake\r\n");
            for (String c : r.setCookies()) resp.append("Set-Cookie: ").append(c).append("\r\n");
            if (bytes.length > 0) resp.append("Content-Type: application/json\r\n");
            if (r.status() != 204) resp.append("Content-Length: ").append(bytes.length).append("\r\n");
            resp.append("Connection: close\r\n\r\n");
            out.write(resp.toString().getBytes(StandardCharsets.ISO_8859_1));
            out.write(bytes);
            out.flush();
        } catch (IOException e) {
            // client went away; nothing to record
        }
    }

    private static byte[] leerCuerpo(InputStream in, Map<String, List<String>> headers) throws IOException {
        if (headers.containsKey("content-length")) {
            int n = Integer.parseInt(headers.get("content-length").get(0));
            return in.readNBytes(n);
        }
        if (headers.containsKey("transfer-encoding") && headers.get("transfer-encoding").get(0).equalsIgnoreCase("chunked")) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            while (true) {
                String size = linea(in);
                int n = Integer.parseInt(size.split(";")[0].trim(), 16);
                if (n == 0) {
                    while (true) {
                        String t = linea(in);
                        if (t == null || t.isEmpty()) break;
                    }
                    return bos.toByteArray();
                }
                bos.write(in.readNBytes(n));
                linea(in);
            }
        }
        return new byte[0];
    }

    private static String linea(InputStream in) throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        int c;
        while ((c = in.read()) != -1) {
            if (c == '\n') break;
            if (c != '\r') b.write(c);
        }
        if (c == -1 && b.size() == 0) return null;
        return b.toString(StandardCharsets.ISO_8859_1);
    }

    @Override
    public void close() {
        abierto = false;
        try {
            server.close();
        } catch (IOException ignored) {
            // closing
        }
    }
}
