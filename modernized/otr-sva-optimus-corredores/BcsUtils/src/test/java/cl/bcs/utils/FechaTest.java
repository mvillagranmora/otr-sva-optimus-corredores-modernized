package cl.bcs.utils;

import cl.bcs.utils.equivalencia.Golden;
import cl.bcs.utils.equivalencia.Golden.Caso;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import static cl.bcs.utils.equivalencia.Golden.d;
import static cl.bcs.utils.equivalencia.Golden.map;
import static cl.bcs.utils.equivalencia.Golden.ts;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Fecha (legacy Fecha.java:12-335; 658 legacy call sites, API kept, main() removed per RULE-076), per default zone.
 * UTIL-34 formatting/parsing/offset conversions, UTIL-35 arithmetic. "fresh JVM" cases load Fecha in a new class
 * loader so the JVM-wide offset (Fecha.java:13, null until setLocalOffSet) has never been set.
 */
class FechaTest {

    static final String AREA = "fecha";

    static Object fresco(String metodo, Class<?>[] tipos, Object... args) throws Exception {
        URL clases = Fecha.class.getProtectionDomain().getCodeSource().getLocation();
        try (URLClassLoader cl = new URLClassLoader(new URL[]{clases}, ClassLoader.getPlatformClassLoader())) {
            Class<?> f = cl.loadClass("cl.bcs.utils.Fecha");
            assertThat(f).isNotSameAs(Fecha.class);
            return f.getMethod(metodo, tipos).invoke(null, args);
        }
    }

    Callable<Object> moderno(Caso c) {
        String fn = c.s("fn");
        if (c.es("state", "fresh JVM")) {
            return switch (fn) {
                case "getLocalOffSet" -> () -> fresco("getLocalOffSet", new Class<?>[]{});
                case "toLocalTimeStamp" -> () -> d((Date) fresco("toLocalTimeStamp", new Class<?>[]{Timestamp.class}, ts(0, 0)));
                case "toUTCTimeStamp" -> () -> d((Date) fresco("toUTCTimeStamp", new Class<?>[]{Timestamp.class}, ts(0, 0)));
                case "toLocalFormat" -> () -> fresco("toLocalFormat", new Class<?>[]{Timestamp.class, String.class}, ts(0, 0), "yyyy");
                case "toLocalTime" -> () -> d((Date) fresco("toLocalTime", new Class<?>[]{Time.class}, new Time(0)));
                default -> throw new AssertionError(fn);
            };
        }
        if (c.tiene("offset")) {
            int off = (int) c.l("offset");
            return () -> {
                Fecha.setLocalOffSet(off);
                return switch (fn) {
                    case "getLocalOffSet" -> Fecha.getLocalOffSet();
                    case "toLocalTimeStamp" -> d(Fecha.toLocalTimeStamp(ts(c.l("ms"), (int) c.l("nanos"))));
                    case "toUTCTimeStamp" -> d(Fecha.toUTCTimeStamp(ts(c.l("ms"), (int) c.l("nanos"))));
                    case "toLocalFormat" -> Fecha.toLocalFormat(ts(c.l("ms"), (int) c.l("nanos")), c.s("format"));
                    case "toLocalTime" -> d(Fecha.toLocalTime(Time.valueOf(c.s("time"))));
                    case "fechaSistemaHoraLocal_fechaHoraUTC" -> d(Fecha.fechaSistemaHoraLocal_fechaHoraUTC(c.s("fechaSistema"), c.s("hora")));
                    case "fechaSistemaHoraUTC_fechaHoraUTC" -> d(Fecha.fechaSistemaHoraUTC_fechaHoraUTC(c.s("fechaSistema"), Time.valueOf(c.s("hora"))));
                    default -> throw new AssertionError(fn);
                };
            };
        }
        if (fn.equals("constants")) {
            return () -> map("YYYYMMDD", Fecha.YYYYMMDD.format(LocalDate.of(2024, 3, 5)), "YYYYMM", Fecha.YYYYMM.format(LocalDate.of(2024, 3, 5)),
                    "YYYY", Fecha.YYYY.format(LocalDate.of(2024, 3, 5)), "DEFAULT_DATE_TIME", Fecha.DEFAULT_DATE_TIME, "DEFAULT_TIME", Fecha.DEFAULT_TIME);
        }
        if (c.tiene("s")) {
            String s = c.s("s");
            return switch (fn) {
                case "toDate" -> () -> d(Fecha.toDate(s));
                case "toDateTime" -> () -> d(Fecha.toDateTime(s));
                case "toTimeStamp" -> () -> d(Fecha.toTimeStamp(s));
                case "toTime" -> () -> d(Fecha.toTime(s));
                default -> throw new AssertionError(fn);
            };
        }
        if (c.input().get("ms").isJsonNull()) {
            return switch (fn) {
                case "toStringDate(sql.Date)" -> () -> Fecha.toStringDate((java.sql.Date) null);
                case "toStringDate(sql.Date,String)" -> () -> Fecha.toStringDate((java.sql.Date) null, c.s("default"));
                case "toStringDate(Timestamp)" -> () -> Fecha.toStringDate((Timestamp) null);
                default -> throw new AssertionError(fn);
            };
        }
        if (fn.equals("toStringDate(sql.Date,String)")) return () -> Fecha.toStringDate(new java.sql.Date(c.l("ms")), c.s("default"));
        Timestamp x = ts(c.l("ms"), (int) c.l("nanos"));
        int n = c.tiene("n") ? (int) c.l("n") : 0;
        return switch (fn) {
            case "toStringTimeStamp" -> () -> Fecha.toStringTimeStamp(x);
            case "toStringDatetime" -> () -> Fecha.toStringDatetime(x);
            case "toStringDate(Timestamp)" -> () -> Fecha.toStringDate(x);
            case "toStringTime(Timestamp)" -> () -> Fecha.toStringTime(x);
            case "toFormat(Timestamp)" -> () -> Fecha.toFormat(x, c.s("format"));
            case "toFormat(sql.Date)" -> () -> Fecha.toFormat(new java.sql.Date(x.getTime()), c.s("format"));
            case "stringToSqlDate" -> () -> Fecha.stringToSqlDate(x);
            case "utilDateToSqlDate" -> () -> d(Fecha.utilDateToSqlDate(x));
            case "toStringDate(sql.Date)" -> () -> Fecha.toStringDate(new java.sql.Date(x.getTime()));
            case "toStringTime(Time)" -> () -> Fecha.toStringTime(new Time(x.getTime()));
            case "addSeconds" -> () -> d(Fecha.addSeconds(x, n));
            case "substractSeconds" -> () -> d(Fecha.substractSeconds(x, n));
            case "addHours" -> () -> d(Fecha.addHours(x, n));
            case "addMilliseconds" -> () -> d(Fecha.addMilliseconds(x, n));
            case "addDays(Timestamp)" -> () -> d(Fecha.addDays(x, n));
            case "addDays(sql.Date)" -> () -> d(Fecha.addDays(new java.sql.Date(x.getTime()), n));
            case "substractDays(Timestamp)" -> () -> d(Fecha.substractDays(x, n));
            case "substractDays(sql.Date)" -> () -> d(Fecha.substractDays(new java.sql.Date(x.getTime()), n));
            default -> throw new AssertionError("unmapped golden fn " + fn + " in " + c.id());
        };
    }

    @TestFactory
    @DisplayName("UTIL-34/35 equivalence per zone: Fecha == legacy")
    Stream<DynamicContainer> util34y35_equivalencia() {
        return Golden.ZONAS.entrySet().stream().map(z -> DynamicContainer.dynamicContainer(z.getValue(),
                Golden.casos(z.getKey(), AREA).stream().map(c -> DynamicTest.dynamicTest(c.nombre(),
                        () -> Golden.enZona(z.getValue(), () -> Golden.igual(AREA, c, moderno(c)))))));
    }

    @AfterEach
    void sinOffsetGlobalResidual() {
        Fecha.setLocalOffSet(0); // legacy API cannot un-set the offset; leave a neutral value for other tests
    }

    @AfterAll
    static void reporte() {
        System.out.println("equivalence cases executed [" + AREA + "]: " + Golden.ejecutados(AREA));
    }

    @Test
    @DisplayName("UTIL-34 legacy defect pinned: until setLocalOffSet is called, every offset conversion throws NullPointerException")
    void util34_offsetNoInicializado() throws Exception {
        assertThat(fresco("getLocalOffSet", new Class<?>[]{})).isNull();
        assertThatThrownBy(() -> fresco("toLocalTimeStamp", new Class<?>[]{Timestamp.class}, ts(0, 0)))
                .hasCauseInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("UTIL-34: toStringTimeStamp truncates nanos to millis; arithmetic drops sub-millisecond nanos")
    void util34_nanos() throws Exception {
        Golden.enZona("UTC", () -> {
            Timestamp t = ts(1710512430123L, 123456789);
            assertThat(Fecha.toStringTimeStamp(t)).isEqualTo("2024-03-15 14:20:30.123");
            assertThat(Fecha.addSeconds(t, 1).getNanos()).isEqualTo(123000000);
        });
    }

    @Test
    @DisplayName("UTIL-34: toLocalTime wraps around midnight (LocalTime arithmetic) with the global offset")
    void util34_horaLocal() throws Exception {
        Golden.enZona("UTC", () -> {
            Fecha.setLocalOffSet(-14400);
            assertThat(Fecha.toLocalTime(Time.valueOf("02:30:00"))).hasToString("22:30:00");
            assertThat(Fecha.toUTCTimeStamp(Timestamp.valueOf("2024-09-07 23:30:00"))).hasToString("2024-09-08 03:30:00.0");
        });
    }
}
