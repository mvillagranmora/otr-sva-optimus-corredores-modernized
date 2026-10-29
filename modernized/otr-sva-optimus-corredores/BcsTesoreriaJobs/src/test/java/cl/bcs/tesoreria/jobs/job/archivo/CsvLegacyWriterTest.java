package cl.bcs.tesoreria.jobs.job.archivo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.io.StringWriter;

import org.junit.jupiter.api.Test;

/**
 * The opencsv 3.7 rules the legacy relied on (the writer is a reproduction, not the jar; the goldens CM-41, CG-02, CG-35, PG-05 and
 * PG-40 pin them through the jobs): quote all or only the special fields, no quote character, a separator alone is not special, a
 * null field writes nothing, and closing twice does not throw.
 */
class CsvLegacyWriterTest {

    private static String escribir(CsvLegacyWriter w, StringWriter sw, boolean todos, String... campos) {
        w.escribirLinea(campos, todos);
        w.flush();
        return sw.toString();
    }

    @Test
    void conComillasATodosCitaCadaCampoYDuplicaLasComillas() {
        StringWriter sw = new StringWriter();
        CsvLegacyWriter w = new CsvLegacyWriter(sw, ';');
        assertThat(escribir(w, sw, true, "a", "b\"c", "d;e")).isEqualTo("\"a\";\"b\"\"c\";\"d;e\"\n");
    }

    @Test
    void sinComillasATodosSoloCitaLosEspeciales() {
        StringWriter sw = new StringWriter();
        CsvLegacyWriter w = new CsvLegacyWriter(sw, ';');
        // a separator alone is NOT special (opencsv 5 would quote it); a quote or a line break is
        assertThat(escribir(w, sw, false, "a;b", "x\"y", "l1\nl2", null)).isEqualTo("a;b;\"x\"\"y\";\"l1\nl2\";\n");
    }

    @Test
    void sinCaracterDeComillaElSeparadorYElSaltoDeLineaSalenTalCual() {
        StringWriter sw = new StringWriter();
        CsvLegacyWriter w = new CsvLegacyWriter(sw, ';', CsvLegacyWriter.SIN_COMILLA);
        assertThat(escribir(w, sw, true, "0", "0", "Rechazado", "a;b")).isEqualTo("0;0;Rechazado;a;b\n");
    }

    @Test
    void cerrarDosVecesNoLanzaExcepcion() {
        StringWriter sw = new StringWriter();
        CsvLegacyWriter w = new CsvLegacyWriter(sw, ';');
        assertThatCode(() -> {
            w.close();
            w.close();
        }).doesNotThrowAnyException();
    }
}
