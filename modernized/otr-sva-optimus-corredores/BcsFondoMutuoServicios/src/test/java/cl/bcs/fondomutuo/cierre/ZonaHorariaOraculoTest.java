package cl.bcs.fondomutuo.cierre;

import java.util.TimeZone;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import cl.bcs.fondomutuo.cierre.CasosOraculo.Caso;

/**
 * DEV-F2 / D-FEC-6: the new code does not depend on the JVM time zone. The goldens were recorded by the legacy under UTC; here EVERY case of the step
 * (dates, days between dates, day of week, holiday lookups, the quota-date arithmetic) runs with another default zone (with and without daylight saving)
 * and must give the same dump. The legacy itself used {@code java.sql.Date} and {@code Calendar} in the JVM zone (the container sets UTC by compose).
 */
class ZonaHorariaOraculoTest {

    @DisplayName("FM-FEC-06 / D-FEC-6: la zona horaria de la JVM no cambia ningun resultado")
    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = { "America/Santiago", "Asia/Tokyo", "Pacific/Auckland", "America/Los_Angeles" })
    void todosLosCasosIgualesEnCualquierZona(String zona) {
        TimeZone previa = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone(zona));
            org.h2.util.DateTimeUtils.resetCalendar();   // H2 (the test database, not the product) caches the default zone of its first use
            for (Caso c : CasosOraculo.cargar("FM-")) {
                CasosOraculo.comparar(c, CasosOraculo.ejecutar(c));
            }
        } finally {
            TimeZone.setDefault(previa);
            org.h2.util.DateTimeUtils.resetCalendar();
        }
    }
}
