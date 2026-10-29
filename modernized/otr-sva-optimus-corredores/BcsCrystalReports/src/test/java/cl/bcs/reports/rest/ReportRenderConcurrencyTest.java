package cl.bcs.reports.rest;

import cl.bcs.reports.equiv.EquivCase;
import cl.bcs.reports.report.RenderLock;
import cl.bcs.reports.equiv.H2Shim;
import cl.bcs.reports.equiv.RestRenderer;
import cl.bcs.reports.support.CrystalTestEnvironment;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * H5: ListMapResultSet keeps its column metadata in a static field and JRC has global state, so two renders with
 * different row shapes at the same time may mix data. The sidecar serializes the render critical section (RenderLock);
 * the concurrent outputs must equal the sequential ones. TXT is used because it is plain text and deterministic.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:restconc;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "services.internal-token=" + RestRenderer.TOKEN
})
@AutoConfigureMockMvc
class ReportRenderConcurrencyTest {

    private static final java.util.Map<String, String> BODIES = new java.util.concurrent.ConcurrentHashMap<>();
    private static final int ROUNDS = 4;
    private static final int REPLICAS = 300;   // 3 rows x 300 per render

    @Autowired MockMvc mvc;
    @Autowired DataSource ds;
    private EquivCase a;   // Custodia_Anexo2
    private EquivCase b;   // Tesoreria_DetalleComprobanteTesoreria (different columns / row shape)

    @BeforeAll
    static void environment() {
        CrystalTestEnvironment.ensure();
    }

    @BeforeEach
    void data() throws Exception {
        H2Shim.init(ds);
        a = load("Custodia_Anexo2");
        b = load("Tesoreria_DetalleComprobanteTesoreria");
    }

    private EquivCase load(String name) throws Exception {
        EquivCase c = new EquivCase(Paths.get("src/test/resources/crystal/equiv", name));
        H2Shim.putTemplate(ds, c.name, Paths.get(c.template));
        return c;
    }

    /**
     * The race lives inside ListMapResultSet.populate (static metadata replaced while the other populate is looping),
     * a window of microseconds with 3 rows; the rows are replicated so the window is wide enough to be hit reliably.
     */
    private static String bigBody(EquivCase c) {
        JsonObject body = RestRenderer.body(c, "txt");
        JsonObject main = body.getAsJsonObject("configuration").getAsJsonObject("mainReport");
        JsonArray rows = main.getAsJsonArray("data");
        JsonArray big = new JsonArray();
        for (int i = 0; i < REPLICAS; i++) {
            rows.forEach(big::add);
        }
        main.add("data", big);
        return new Gson().toJson(body);
    }

    private byte[] render(EquivCase c) throws Exception {
        byte[] out = mvc.perform(post("/report").header("X-Internal-Token", RestRenderer.TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).content(BODIES.computeIfAbsent(c.name, k -> bigBody(c))))
                .andReturn().getResponse().getContentAsByteArray();
        assertEquals(true, out.length > 100, "render of " + c.name + " returned " + out.length + " bytes");
        return out;
    }

    @Test
    void concurrentRendersOfDifferentRowShapesEqualTheirSequentialOutputs() throws Exception {
        BODIES.clear();
        BODIES.put(a.name, bigBody(a));
        BODIES.put(b.name, bigBody(b));
        byte[] seqA = render(a);
        byte[] seqB = render(b);
        assertArrayEquals(seqA, render(a), "precondition: TXT of " + a.name + " is deterministic");
        assertArrayEquals(seqB, render(b), "precondition: TXT of " + b.name + " is deterministic");

        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            for (int round = 0; round < ROUNDS; round++) {
                CyclicBarrier start = new CyclicBarrier(2);
                List<Callable<byte[]>> jobs = new ArrayList<>();
                jobs.add(() -> {
                    start.await();
                    return render(a);
                });
                jobs.add(() -> {
                    start.await();
                    return render(b);
                });
                List<Future<byte[]>> res = pool.invokeAll(jobs);
                assertArrayEquals(seqA, res.get(0).get(), "round " + round + ": " + a.name + " differs from its sequential output");
                assertArrayEquals(seqB, res.get(1).get(), "round " + round + ": " + b.name + " differs from its sequential output");
            }
        } finally {
            pool.shutdownNow();
        }
    }

    /**
     * Deterministic proof that the render critical section is serialized: while the test holds the lock, neither the
     * /report render, the /reports/generate render nor a template upload can finish; once released they complete.
     * (The row-shape race above has a microsecond window, so this is what fails reliably if the lock is removed.)
     */
    @Test
    void renderGenerateAndUploadWaitForTheLock() throws Exception {
        byte[] expected = render(a);
        ExecutorService pool = Executors.newFixedThreadPool(3);
        try {
            RenderLock.lock();
            Future<byte[]> report;
            Future<Integer> generate;
            Future<Integer> upload;
            try {
                report = pool.submit(() -> render(a));
                generate = pool.submit(() -> mvc.perform(post("/reports/generate").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"templateId\":\"" + a.name + "\",\"format\":\"txt\"}")).andReturn().getResponse().getStatus());
                upload = pool.submit(() -> mvc.perform(multipart("/report").file(
                                new MockMultipartFile("data", "x.rpt", "application/octet-stream", "not a report".getBytes()))
                        .param("upload", "").param("reportName", "n").param("reportVersion", "1").param("companyId", "1")
                        .header("X-Internal-Token", RestRenderer.TOKEN)).andReturn().getResponse().getStatus());
                Thread.sleep(2500);
                assertFalse(report.isDone(), "/report finished while the render lock was held");
                assertFalse(generate.isDone(), "/reports/generate finished while the render lock was held");
                assertFalse(upload.isDone(), "upload finished while the render lock was held");
            } finally {
                RenderLock.unlock();
            }
            assertArrayEquals(expected, report.get(120, TimeUnit.SECONDS));
            generate.get(120, TimeUnit.SECONDS);   // any status (no data given: it answers 500 like before); it must just complete
            upload.get(120, TimeUnit.SECONDS);
        } finally {
            pool.shutdownNow();
        }
    }
}
