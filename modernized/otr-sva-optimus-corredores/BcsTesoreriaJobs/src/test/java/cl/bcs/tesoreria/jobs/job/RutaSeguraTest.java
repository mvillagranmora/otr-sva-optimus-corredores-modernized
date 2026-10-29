package cl.bcs.tesoreria.jobs.job;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import cl.bcs.tesoreria.jobs.job.archivo.ArchivoPago;
import cl.bcs.tesoreria.jobs.job.archivo.RutaSegura;
import cl.bcs.tesoreria.port.out.Colaboradores;
import cl.bcs.tesoreria.port.out.ConfiguracionSistema;

/** Review M1: request-controlled values cannot escape the base directory. */
class RutaSeguraTest {

    @ParameterizedTest
    @ValueSource(strings = {"../x", "..", "a/b", "a\\b", "/etc/passwd", "a b", "x..y", "a;b", "%2e%2e", "á"})
    @DisplayName("M1: a file-name value with separators, '..' or odd characters is rejected")
    void nombreInvalido(String valor) {
        assertThatThrownBy(() -> RutaSegura.nombre("TipoArchivoCarga", valor)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("M1: a name longer than 60 characters is rejected")
    void nombreLargo() {
        assertThatThrownBy(() -> RutaSegura.nombre("p", "A".repeat(61))).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"DAP", "FFMM", "Dap_1-2.v3"})
    @DisplayName("M1: normal values are accepted and returned unchanged")
    void nombreValido(String valor) {
        assertThat(RutaSegura.nombre("TipoArchivoCarga", valor)).isEqualTo(valor);
    }

    @Test
    @DisplayName("M1: null and empty (legacy builds the name with them) are not rejected")
    void nuloYVacio() {
        assertThat(RutaSegura.nombre("p", null)).isNull();
        assertThat(RutaSegura.nombre("p", "")).isEmpty();
    }

    @Test
    @DisplayName("M1: bajoBase accepts paths below the base and rejects the ones that normalize outside")
    void bajoBase(@TempDir Path base) {
        String b = base.toString() + "/";
        assertThatCode(() -> RutaSegura.bajoBase(b, b + "tes/../tes/a.csv")).doesNotThrowAnyException();
        assertThatThrownBy(() -> RutaSegura.bajoBase(b, b + "../fuera.csv")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> RutaSegura.bajoBase(b, b + "tes/../../fuera.csv")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("M1: the mass-load error file with TipoArchivoCarga '../../x' is rejected; 'DAP' is written under the base")
    void cargaMasivaSalida(@TempDir Path base) throws Exception {
        Files.createDirectories(base.resolve("carga"));
        ConfiguracionSistema cfg = mock(ConfiguracionSistema.class);
        when(cfg.parametro(1L, "PLATAFORMA", "PLA.RutaBaseArchivos")).thenReturn(base.toString() + "/");
        when(cfg.parametro(1L, "TESORERIA", "TES.RutaSalidaArchivosCargaMasiva")).thenReturn("carga/");
        CargaMasivaSalida salida = new CargaMasivaSalida(mock(Colaboradores.class), cfg);

        assertThatThrownBy(() -> salida.generarArchivoSalidaErrores("2025-01-02", "10:20:30", "m", "../../x", 1L))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(salida.generarArchivoSalidaErrores("2025-01-02", "10:20:30", "m", "DAP", 1L))
                .isEqualTo("carga/CDB_Errores_IngresoEgreso_DAP_20250102_102030.csv");
        assertThat(base.resolve("carga/CDB_Errores_IngresoEgreso_DAP_20250102_102030.csv")).exists();
    }

    @Test
    @DisplayName("M1: ArchivoPago refuses a file name that leaves its folder, and writes a normal one")
    void archivoPago(@TempDir Path base) {
        String carpeta = base.toString() + "/";
        assertThatThrownBy(() -> new ArchivoPago("../fuera.csv", "CSV", carpeta).crear()).isInstanceOf(IllegalArgumentException.class);
        ArchivoPago ok = new ArchivoPago("ok.csv", "CSV", carpeta);
        ok.crear();
        ok.cerrar();
        assertThat(base.resolve("ok.csv")).exists();
        assertThat(base.getParent().resolve("fuera.csv")).doesNotExist();
    }
}
