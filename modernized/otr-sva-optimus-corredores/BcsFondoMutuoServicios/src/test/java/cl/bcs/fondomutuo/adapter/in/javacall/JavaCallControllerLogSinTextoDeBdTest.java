package cl.bcs.fondomutuo.adapter.in.javacall;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;

import cl.bcs.fondomutuo.kit.AccesoDatosException;

/** Architecture review F3 W1 (M3): what the controller logs for a failure caused by the database carries SQLState and ErrorCode, never the text that HANA builds with values. */
class JavaCallControllerLogSinTextoDeBdTest {

    private static String traza(Throwable t) {
        StringWriter w = new StringWriter();
        t.printStackTrace(new PrintWriter(w));
        return w.toString();
    }

    @Test
    void unaCausaSql_seLoguea_sinSuTexto_perConSqlStateErrorCodeYLaMismaPila() {
        SQLException sql = new SQLException("invalid value 'SECRETO-12345' for column X", "22018", 307);
        Throwable e = new AccesoDatosException("SQL failure: SQLState=22018 ErrorCode=307", sql);
        Throwable logueada = JavaCallController.sinTextoDeBd(e);
        String texto = traza(logueada);
        assertThat(texto).doesNotContain("SECRETO-12345").contains("SQLState=22018").contains("ErrorCode=307").contains("AccesoDatosException");
        assertThat(logueada.getStackTrace()).isEqualTo(e.getStackTrace());
        assertThat(logueada.getCause()).isNull();
    }

    @Test
    void unaExcepcionSinCausaSql_sePasaTalCual() {
        Throwable e = new IllegalStateException("otra cosa", new RuntimeException("causa"));
        assertThat(JavaCallController.sinTextoDeBd(e)).isSameAs(e);
    }

    @Test
    void l1_unMensajeConSaltosDeLinea_seLoguea_enUnaCopiaLimpia_conLaMismaPila() {
        Throwable e = new IllegalArgumentException("bad value\nFAKE LOG LINE\r\nINFO forged", new RuntimeException("causa\nforjada"));
        Throwable logueada = JavaCallController.sinTextoDeBd(e);
        assertThat(logueada).isNotSameAs(e);
        assertThat(logueada.getMessage()).doesNotContain("\n").doesNotContain("\r").contains("IllegalArgumentException").contains("RuntimeException");
        assertThat(traza(logueada).lines().filter(l -> l.startsWith("FAKE") || l.startsWith("INFO") || l.startsWith("forjada"))).isEmpty();
        assertThat(logueada.getStackTrace()).isEqualTo(e.getStackTrace());
    }
}
