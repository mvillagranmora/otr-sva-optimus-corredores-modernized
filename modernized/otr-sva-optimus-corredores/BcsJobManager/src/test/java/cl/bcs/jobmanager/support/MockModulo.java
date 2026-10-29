package cl.bcs.jobmanager.support;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/** Local HTTP server standing in for a job module's POST /internal/job-call. */
public final class MockModulo implements AutoCloseable {

    /** One request received. */
    public record Peticion(String metodo, String ruta, String token, String cuerpo) { }

    private final HttpServer server;
    private final List<Peticion> peticiones = new CopyOnWriteArrayList<>();
    private final AtomicInteger activas = new AtomicInteger();
    private final AtomicInteger maxActivas = new AtomicInteger();
    private volatile int status = 200;
    private volatile long demoraMs = 0;
    private volatile CountDownLatch compuerta;

    public MockModulo() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.setExecutor(Executors.newCachedThreadPool());
        server.createContext("/", ex -> {
            int n = activas.incrementAndGet();
            maxActivas.accumulateAndGet(n, Math::max);
            try {
                String cuerpo = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                peticiones.add(new Peticion(ex.getRequestMethod(), ex.getRequestURI().getPath(),
                        ex.getRequestHeaders().getFirst("X-Internal-Token"), cuerpo));
                CountDownLatch c = compuerta;
                if (c != null) {
                    c.await(10, TimeUnit.SECONDS);
                }
                if (demoraMs > 0) {
                    Thread.sleep(demoraMs);
                }
                ex.sendResponseHeaders(status, -1);
            } catch (InterruptedException | IOException e) {
                // the client gave up (read timeout); nothing to answer
            } finally {
                activas.decrementAndGet();
                ex.close();
            }
        });
        server.start();
    }

    public String url() { return "http://127.0.0.1:" + server.getAddress().getPort(); }
    public int puerto() { return server.getAddress().getPort(); }
    public MockModulo responde(int status) { this.status = status; return this; }
    public MockModulo demora(long ms) { this.demoraMs = ms; return this; }
    public MockModulo retiene(CountDownLatch compuerta) { this.compuerta = compuerta; return this; }
    public List<Peticion> peticiones() { return peticiones; }
    public int maxSimultaneas() { return maxActivas.get(); }

    @Override
    public void close() { server.stop(0); }
}
