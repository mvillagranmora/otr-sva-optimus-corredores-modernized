package cl.bcs.cuentainversion.jobs.job.archivo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/** Review M-2: the lenient path rule (same as BcsTesoreriaJobs, own copy). */
class RutaSeguraTest {

    @Test
    void nombreSinSeparadoresNiPuntosPuntos() {
        assertThat(RutaSegura.nombre("ARCHIVO_X_20260102_030405.TXT")).isEqualTo("ARCHIVO_X_20260102_030405.TXT");
        assertThat(RutaSegura.nombre("Saldos de Cuentas Ñandú.CSV")).isNotNull();
        assertThat(RutaSegura.nombre(null)).isNull();
        for (String malo : new String[] {"../x", "a/b", "a\\b", "x..y"}) {
            assertThatThrownBy(() -> RutaSegura.nombre(malo)).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    void rutaBajoLaCarpeta() {
        assertThatCode(() -> RutaSegura.bajoCarpeta("/data/out/", "/data/out/f.txt")).doesNotThrowAnyException();
        assertThatThrownBy(() -> RutaSegura.bajoCarpeta("/data/out/", "/data/out/../f.txt")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> RutaSegura.bajoCarpeta("/data/out/", "/data/otro/f.txt")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void carpetaSinSeparadorFinalEsLenient() {
        // legacy plain concatenation "/data/out" + "f.txt" = "/data/outf.txt": accepted, its parent is the limit
        assertThatCode(() -> RutaSegura.bajoCarpeta("/data/out", "/data/outf.txt")).doesNotThrowAnyException();
        assertThatThrownBy(() -> RutaSegura.bajoCarpeta("/data/out", "/otro/f.txt")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nulosYRutasNoInterpretablesNoSeJuzgan() {
        assertThatCode(() -> RutaSegura.bajoCarpeta(null, "x")).doesNotThrowAnyException();
        assertThatCode(() -> RutaSegura.bajoBase("/a", null)).doesNotThrowAnyException();
        assertThatCode(() -> RutaSegura.bajoBase("a\0b", "/x")).doesNotThrowAnyException();
    }
}
