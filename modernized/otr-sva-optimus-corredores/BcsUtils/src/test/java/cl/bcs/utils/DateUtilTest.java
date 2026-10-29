package cl.bcs.utils;

import cl.bcs.utils.equivalencia.Golden;
import cl.bcs.utils.equivalencia.Golden.Caso;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import static cl.bcs.utils.equivalencia.Golden.d;
import static cl.bcs.utils.equivalencia.Golden.map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * DateUtil (legacy DateUtil.java:34-239) and Utils.diferenciaEnDias (Utils.java:1379-1383), in the JVM default
 * zone: America/Santiago (DST edges: 2024-09-08 has no local midnight, 2024-04-06 23:xx happens twice) and UTC.
 * UTIL-30 parsing, UTIL-31 start/end of day/month and getters, UTIL-32 addDays, UTIL-33 diferenciaEnDias.
 * API kept as in legacy (BcsNormativaCore calls DateUtil.getDate and catches DateFormatException).
 */
class DateUtilTest {

    static final String AREA = "dateutil";

    static Date at(String iso) {
        return iso == null ? null : Date.from(Instant.parse(iso));
    }

    /** "yyyy-MM-dd" strings go through DateUtil.getDate (as the oracle did); ISO instants through Instant.parse. */
    static Date fecha(String s) {
        return s == null ? null : s.contains("T") ? at(s) : DateUtil.getDate(s);
    }

    Callable<Object> moderno(Caso c) {
        String fn = c.s("fn");
        return switch (fn) {
            case "getDate(String)" -> () -> d(DateUtil.getDate(c.s("val")));
            case "getAnoMes" -> () -> d(DateUtil.getAnoMes(c.s("val")));
            case "getDate(int,int,int)" -> () -> d(DateUtil.getDate((int) c.l("year"), (int) c.l("month"), (int) c.l("day")));
            case "getStartOfDay" -> () -> d(DateUtil.getStartOfDay(at(c.s("instant"))));
            case "getEndOfDay" -> () -> d(DateUtil.getEndOfDay(at(c.s("instant"))));
            case "getStartOfMonth" -> () -> d(DateUtil.getStartOfMonth(at(c.s("instant"))));
            case "getDay/getMonth/getYear" -> () -> map("day", DateUtil.getDay(at(c.s("instant"))), "month", DateUtil.getMonth(at(c.s("instant"))), "year", DateUtil.getYear(at(c.s("instant"))));
            case "getDay" -> () -> DateUtil.getDay(at(c.s("instant")));
            case "addDays" -> () -> d(DateUtil.addDays(at(c.s("instant")), (int) c.l("days")));
            case "getCalendar" -> () -> {
                Calendar k = DateUtil.getCalendar(at(c.s("instant")));
                return map("timeZone", k.getTimeZone().getID(), "class", k.getClass().getName(), "ms", k.getTimeInMillis(),
                        "firstDayOfWeek", k.getFirstDayOfWeek(), "lenient", k.isLenient());
            };
            case "diferenciaEnDias" -> () -> Utils.diferenciaEnDias(fecha(c.s("mayor")), fecha(c.s("menor")));
            default -> throw new AssertionError("unmapped golden fn " + fn + " in " + c.id());
        };
    }

    @TestFactory
    @DisplayName("UTIL-30..33 equivalence per zone: DateUtil + diferenciaEnDias == legacy")
    Stream<DynamicContainer> util30a33_equivalencia() {
        return Golden.ZONAS.entrySet().stream().map(z -> DynamicContainer.dynamicContainer(z.getValue(),
                Golden.casos(z.getKey(), AREA).stream().map(c -> DynamicTest.dynamicTest(c.nombre(),
                        () -> Golden.enZona(z.getValue(), () -> Golden.igual(AREA, c, moderno(c)))))));
    }

    @AfterAll
    static void reporte() {
        System.out.println("equivalence cases executed [" + AREA + "]: " + Golden.ejecutados(AREA));
    }

    @Test
    @DisplayName("UTIL-30: four accepted shapes, strict (non-lenient) calendar, DateFormatException otherwise; null/\"\" -> null")
    void util30_formatos() throws Exception {
        Golden.enZona("UTC", () -> {
            assertThat(DateUtil.getDate("15-03-2024").getTime()).isEqualTo(1710460800000L);
            assertThat(DateUtil.getDate("2024/3/15").getTime()).isEqualTo(1710460800000L);
            assertThat(DateUtil.getDate("")).isNull();
            assertThatThrownBy(() -> DateUtil.getDate("29-02-2023")).isInstanceOf(DateFormatException.class);
            assertThatThrownBy(() -> DateUtil.getDate("2024-03-15 ")).isInstanceOf(DateFormatException.class);
            assertThatThrownBy(() -> DateUtil.getAnoMes(null)).isInstanceOf(DateFormatException.class);
            assertThat(DateFormatException.class).hasSuperclass(RuntimeException.class);
        });
    }

    @Test
    @DisplayName("UTIL-30/31 America/Santiago DST: 2024-09-08 has no 00:00, getDate and getStartOfDay return 01:00 -03:00")
    void util31_santiagoSinMedianoche() throws Exception {
        Golden.enZona("America/Santiago", () -> {
            assertThat(DateUtil.getDate("2024-09-08").getTime()).isEqualTo(Instant.parse("2024-09-08T04:00:00Z").toEpochMilli());
            assertThat(DateUtil.getStartOfDay(at("2024-09-08T12:00:00Z")).getTime()).isEqualTo(Instant.parse("2024-09-08T04:00:00Z").toEpochMilli());
        });
    }

    @Test
    @DisplayName("UTIL-33 legacy defect pinned: diferenciaEnDias truncates elapsed ms / 86 400 000, so across the September DST jump 2 calendar days count as 1 in Santiago")
    void util33_diferenciaDst() throws Exception {
        Golden.enZona("America/Santiago", () -> assertThat(Utils.diferenciaEnDias(DateUtil.getDate("2024-09-09"), DateUtil.getDate("2024-09-07"))).isEqualTo(1));
        Golden.enZona("UTC", () -> assertThat(Utils.diferenciaEnDias(DateUtil.getDate("2024-09-09"), DateUtil.getDate("2024-09-07"))).isEqualTo(2));
    }

    @Test
    @DisplayName("UTIL-30: getDate(y, m, d) is lenient and 0-based in month (month 12 -> January next year)")
    void util30_enteros() throws Exception {
        Golden.enZona("UTC", () -> {
            assertThat(DateUtil.getDate(2024, 12, 1).getTime()).isEqualTo(Instant.parse("2025-01-01T00:00:00Z").toEpochMilli());
            assertThat(DateUtil.getDate(2023, 1, 29).getTime()).isEqualTo(Instant.parse("2023-03-01T00:00:00Z").toEpochMilli());
        });
    }
}
