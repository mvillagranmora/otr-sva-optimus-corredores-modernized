package cl.bcs.utils;

import cl.bcs.utils.equivalencia.Golden;
import cl.bcs.utils.equivalencia.Golden.Caso;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static cl.bcs.utils.equivalencia.Golden.map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * UTIL-36: Timer (legacy Timer.java:18-127). Target API = legacy fluent API plus {@code Timer.createTimer(LongSupplier
 * clockMillis)} so elapsed times are deterministic, and {@code setLogger(org.slf4j.Logger)} (log4j replaced by SLF4J:
 * endMetric -> trace, printMetrics -> debug, as legacy). Golden: golden/any/timer.json.
 */
class TimerTest {

    static final String AREA = "timer";
    /** Golden cases not reachable through the public API (the oracle read private state by reflection). */
    static final Set<String> NO_APLICA = Set.of("TIMER-064");

    static LongSupplier reloj(long... valores) {
        Deque<Long> q = new ArrayDeque<>();
        for (long v : valores) q.add(v);
        long ultimo = valores.length == 0 ? 0 : valores[valores.length - 1];
        return () -> q.isEmpty() ? ultimo : q.poll();
    }

    static String stdout(Callable<?> r) throws Exception {
        PrintStream old = System.out;
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        System.setOut(new PrintStream(bo, true, StandardCharsets.UTF_8));
        try {
            r.call();
        } finally {
            System.setOut(old);
        }
        return bo.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
    }

    static org.slf4j.Logger grabador(List<String> sink) {
        return (org.slf4j.Logger) Proxy.newProxyInstance(TimerTest.class.getClassLoader(), new Class<?>[]{org.slf4j.Logger.class}, (p, m, a) -> {
            if (a != null && a.length == 1 && a[0] instanceof String && Set.of("trace", "debug", "info").contains(m.getName()))
                sink.add(m.getName() + ":" + a[0]);
            if (m.getReturnType() == boolean.class) return false;
            if (m.getName().equals("getName")) return "grabador";
            return null;
        });
    }

    static final Pattern TARDO = Pattern.compile("^m, tardo: (-?\\d+)\\((\\w+)\\)\n$");

    Callable<Object> moderno(Caso c) {
        String fn = c.s("fn");
        return switch (fn) {
            case "calculateMetric" -> () -> {
                TimeUnit u = TimeUnit.valueOf(c.s("unit"));
                Timer t = Timer.createTimer(reloj(c.l("init"), c.l("end"))).setTimeUnit(u);
                String s = stdout(() -> t.addMetric("m").endMetric());
                Matcher m = TARDO.matcher(s);
                assertThat(m.matches()).as("message shape: " + s).isTrue();
                assertThat(m.group(2)).isEqualTo(u.name());
                return Long.parseLong(m.group(1));
            };
            case "printMetrics(stdout)" -> () -> {
                if (c.es("unit", "SECONDS at addMetric, MILLISECONDS at print")) {
                    Timer t = Timer.createTimer(reloj(0, 5000));
                    stdout(() -> t.addMetric("A").endMetric());
                    t.setTimeUnit(TimeUnit.MILLISECONDS);
                    return stdout(() -> { t.printMetrics(); return null; });
                }
                Timer t = Timer.createTimer(reloj(1000, 3500, 0, 61000, 10, 10)).setTimeUnit(TimeUnit.valueOf(c.s("unit")));
                stdout(() -> t.addMetric("INICIO PROCESO").endMetric().addMetric("100% listo").endMetric().addMetric(null).endMetric());
                return stdout(() -> { t.printMetrics(); t.printMetrics(); return null; });
            };
            case "printMetrics(logger)" -> () -> {
                List<String> sink = new ArrayList<>();
                Timer t = Timer.createTimer(reloj(0, 2500)).setLogger(grabador(sink));
                t.addMetric("X").endMetric();
                sink.clear();
                String so = stdout(() -> { t.printMetrics(); return null; });
                return map("stdout", so, "logger", sink);
            };
            case "endMetric(stdout)" -> () -> stdout(() -> Timer.createTimer(reloj(7)).addMetric("E").endMetric());
            case "endMetric(logger)" -> () -> {
                List<String> sink = new ArrayList<>();
                String so = stdout(() -> Timer.createTimer(reloj(7)).setLogger(grabador(sink)).addMetric("E").endMetric());
                return map("stdout", so, "logger", sink);
            };
            case "addMetric x3 (stdout)" -> () -> stdout(() -> Timer.createTimer(reloj(7)).addMetric("P1").addMetric("P2").addMetric("P3"));
            case "addMetric after endMetric (stdout)" -> () -> stdout(() -> Timer.createTimer(reloj(7)).addMetric("P1").endMetric().addMetric("P2"));
            case "endMetric on empty" -> () -> stdout(() -> Timer.createTimer(reloj(7)).endMetric());
            case "printMetrics on empty" -> () -> stdout(() -> { Timer.createTimer(reloj(7)).printMetrics(); return null; });
            case "clearMetrics then printMetrics" -> () -> stdout(() -> { Timer.createTimer(reloj(7)).addMetric("C").clearMetrics().printMetrics(); return null; });
            case "fluent API" -> () -> {
                Timer t = Timer.createTimer();
                return map("setTimeUnitReturnsSame", t.setTimeUnit(TimeUnit.SECONDS) == t, "setLoggerReturnsSame", t.setLogger(null) == t,
                        "addMetricReturnsSame", t.addMetric("a") == t, "clearReturnsSame", t.clearMetrics() == t,
                        "distinctInstances", Timer.createTimer() != Timer.createTimer());
            };
            default -> throw new AssertionError("unmapped golden fn " + fn + " in " + c.id());
        };
    }

    @TestFactory
    @DisplayName("UTIL-36 equivalence: Timer messages, units and sinks == legacy")
    Stream<DynamicTest> util36_equivalencia() {
        List<Caso> casos = Golden.casos("any", AREA);
        assertThat(casos).extracting(Caso::id).containsAll(NO_APLICA);
        return casos.stream().filter(c -> !NO_APLICA.contains(c.id()))
                .map(c -> DynamicTest.dynamicTest(c.nombre(), () -> Golden.igual(AREA, c, moderno(c))));
    }

    @AfterAll
    static void reporte() {
        System.out.println("equivalence cases executed [" + AREA + "]: " + Golden.ejecutados(AREA) + " (not applicable: " + NO_APLICA + ")");
    }

    @Test
    @DisplayName("UTIL-36 legacy defect pinned: only SECONDS divides; any other unit prints raw milliseconds labelled with that unit")
    void util36_unidadesNoConvertidas() throws Exception {
        assertThat(stdout(() -> Timer.createTimer(reloj(0, 120000)).setTimeUnit(TimeUnit.MINUTES).addMetric("m").endMetric()))
                .isEqualTo("m, tardo: 120000(MINUTES)\n");
        assertThat(stdout(() -> Timer.createTimer(reloj(0, 1999)).addMetric("m").endMetric())).isEqualTo("m, tardo: 1(SECONDS)\n");
        assertThat(stdout(() -> Timer.createTimer(reloj(1500, 0)).addMetric("m").endMetric())).isEqualTo("m, tardo: -1(SECONDS)\n");
    }

    @Test
    @DisplayName("UTIL-36: printMetrics always writes a blank line to stdout first (even with a logger), then clears the metrics")
    void util36_lineaEnBlanco() throws Exception {
        List<String> sink = new ArrayList<>();
        Timer t = Timer.createTimer(reloj(0, 2500)).setLogger(grabador(sink));
        t.addMetric("X").endMetric();
        assertThat(sink).containsExactly("trace:X, tardo: 2(SECONDS)");
        sink.clear();
        assertThat(stdout(() -> { t.printMetrics(); return null; })).isEqualTo("\n");
        assertThat(sink).containsExactly("debug:X, tardo: 2(SECONDS)");
        sink.clear();
        stdout(() -> { t.printMetrics(); return null; });
        assertThat(sink).isEmpty();
    }

    @Test
    @DisplayName("UTIL-36: endMetric without metrics throws NoSuchElementException")
    void util36_endSinMetricas() {
        assertThatThrownBy(() -> Timer.createTimer().endMetric()).isInstanceOf(java.util.NoSuchElementException.class);
    }
}
