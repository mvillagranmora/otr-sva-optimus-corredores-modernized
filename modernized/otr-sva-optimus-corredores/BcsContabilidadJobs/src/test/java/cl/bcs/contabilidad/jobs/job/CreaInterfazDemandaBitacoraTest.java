package cl.bcs.contabilidad.jobs.job;

import cl.bcs.contabilidad.domain.port.out.InterfazContadorSource;
import cl.bcs.contabilidad.domain.port.out.InterfazFileTransfer;
import cl.bcs.contabilidad.domain.port.out.PlataformaPort;
import cl.bcs.contabilidad.infrastructure.sftp.SftpSettings;
import cl.bcs.contabilidad.jobs.runtime.JobResultado;
import cl.bcs.contabilidad.jobs.runtime.JobResultado.Desenlace;
import cl.bcs.contabilidad.jobs.support.Archivos;
import cl.bcs.contabilidad.jobs.support.FakeInterfazSapSource;
import cl.bcs.contabilidad.jobs.support.H2Jobs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static cl.bcs.contabilidad.jobs.job.CreaInterfazDiariaBitacoraTest.familia;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Decision 2026-10-15, point 5, for legacy {@code BcsContabilidadJobs/src/cl/bcs/contabilidad/jobs/CreaInterfazDemanda.java}:
 * <ul>
 *   <li>(i) the run log {@code <base>APPS/GAB1/logs/LOG_ServiceBROAD_<today>.log} holds the lines the legacy
 *       {@code Crea*} generators wrote into the same {@code Log} (:125-200), with the Demanda file names;</li>
 *   <li>(ii) when the SAP connection cannot be obtained (:112-114, inside the try that starts at :78), nothing is
 *       generated or uploaded; GAB1 was already emptied (:80-84); the outer catch logs the FIRST STACK FRAME, since
 *       the failure is not an IllegalArgumentException (:253-257); {@code transaction()} still returns true (:301-312).</li>
 * </ul>
 */
class CreaInterfazDemandaBitacoraTest {

    static final String CLASE = "cl.bcs.contabilidad.jobs.CreaInterfazDemanda";
    static final Clock RELOJ = Clock.fixed(Instant.parse("2026-03-16T14:00:00Z"), ZoneId.of("America/Santiago"));

    @TempDir
    Path tmp;

    private String gab1;
    private final H2Jobs db = H2Jobs.nueva();
    private final PlataformaPort plataforma = mock(PlataformaPort.class);
    private final InterfazContadorSource contador = mock(InterfazContadorSource.class);
    private final InterfazFileTransfer sftp = mock(InterfazFileTransfer.class);
    private final FakeInterfazSapSource sap = new FakeInterfazSapSource();
    private final Supplier<SftpSettings> settings =
            () -> new SftpSettings("sftp.invalid", 22, "fake-sftp-user", "fake-sftp-pass-0000", Path.of("known_hosts"));

    @BeforeEach
    void preparar() {
        String base = tmp.resolve("archivos").toString() + File.separator;
        gab1 = base + "APPS/GAB1/";
        when(plataforma.obtenerParametroConfiguracion(eq(1L), eq("PLATAFORMA"), eq("PLA.RutaBaseArchivos"), any())).thenReturn(base);
        when(contador.contadoresInterfazSiga(any())).thenReturn(List.of(4L));
    }

    private JobResultado ejecutar(ConexionSap conexion) {
        return db.runner(new CreaInterfazDemanda(plataforma, contador, sap, conexion, sftp, settings, RELOJ))
                .ejecutar(CLASE, 301L, Map.of("FechaEjecucion", "2026-03-15"));
    }

    private List<String> log() {
        String txt = Archivos.leer(Path.of(gab1 + "logs/LOG_ServiceBROAD_20260316.log"));
        return Arrays.stream(txt.split("\r\n")).filter(l -> !l.isEmpty()).toList();
    }

    @Test
    @DisplayName("CONT-J-56 (5.ii): the SAP connection cannot be obtained -> GAB1 emptied, 'Estableciendo...', 'ERR - <first stack frame>'; no file, no SFTP; COMMIT")
    void contJ56_sinConexionSap_abortaGeneracion() {
        // CreaInterfazDemanda.java:80-84 (BorrarDirectorio), :91 (contador), :110 (INF line), :112-114 getConnection()
        // throws (a SQLException in the legacy, not an IllegalArgumentException) -> :253-257 e.getStackTrace()[0];
        // "Fin de generacion" (:213), "Subiendo" (:217) and the SFTP block (:219-245) are skipped.
        Archivos.escribir(Path.of(gab1 + "previo/viejo.TXT"), "x");
        IllegalStateException caida = new IllegalStateException("SAP B1: conexion rechazada");

        JobResultado r = ejecutar(() -> { throw caida; });

        assertThat(r.desenlace()).isEqualTo(Desenlace.COMMIT);
        assertThat(log()).containsExactly("INF - Estableciendo conexion a SAP", "ERR - " + caida.getStackTrace()[0]);
        verify(contador).contadoresInterfazSiga(LocalDate.of(2026, 3, 15));
        assertThat(sap.llamadas()).isEmpty();
        verifyNoInteractions(sftp);
        assertThat(Archivos.arbol(Path.of(gab1))).containsExactly("logs/LOG_ServiceBROAD_20260316.log");
    }

    @Test
    @DisplayName("CONT-J-57 (5.i): clean run -> job lines and generator lines with the Demanda names, then 'Fin de generacion' and 'Subiendo'")
    void contJ57_lineasDeLosGeneradores() {
        // CreaInterfazDemanda.java:110, :118, :120-211 (names :122-123, :141-142, :160-161, :179-180, :197-198), :213, :217;
        // generator lines as in CONT-J-39 (CreaBroadCuentas.java:83,:130,:153,:201 ... CreaMovimientoBPC.java:247).
        assertThat(ejecutar(ConexionSap.IMPLICITA).desenlace()).isEqualTo(Desenlace.COMMIT);

        List<String> esperado = new ArrayList<>(List.of(
                "INF - Estableciendo conexion a SAP", "INF - Iniciando generacion de archivos"));
        esperado.addAll(familia("B1_BROAD_CTAS.txt", "B1_CTRL_BROAD_CTAS.txt"));
        esperado.addAll(familia("B1_BROAD_MOV_CONT.TXT", "B1_CTRL_BROAD_MOV_CONT.TXT"));
        esperado.addAll(familia("B1_BROAD_SALD_CONT.TXT", "B1_CTRL_BROAD_SALD_CONT.TXT"));
        esperado.addAll(familia("B1_CUENTAS_BPC_DM.TXT", "B1_CTRL_CUENTAS_BPC_DM.TXT"));
        esperado.addAll(List.of(
                "INF - Creando Archivo: B1_BROAD_BPC_DM.TXT",
                "INF - Fin de creacion de Archivo: B1_BROAD_BPC_DM.TXT",
                "INF - Creando Archivo: B1_CTRL_BROAD_BPC_DM.TXT",
                "INF - Fin de creacion de Archivo: B1_CTRL_BROAD_BPC_DM.TXT",
                "INF - Fin de generacion de archivos",
                "INF - Subiendo archivos al directorio destino"));
        assertThat(log()).containsExactlyElementsOf(esperado);
    }

    @Test
    @DisplayName("CONT-J-57b (5.i): a failing step's 'ERR - <label>: ...' sits between the previous and the next family's lines")
    void contJ57b_errorDeEtiquetaEnSuLugar() {
        // CreaInterfazDemanda.java:146-150 (IllegalArgumentException -> label + message); CreaBroadMovimiento.java:96
        // queries before its "Creando Archivo" (:102), so the failing family logs nothing itself.
        sap.fallar("movimientosBroad", new IllegalArgumentException("fecha fuera de rango"));

        ejecutar(ConexionSap.IMPLICITA);

        List<String> l = log();
        int i = l.indexOf("INF - Fin de creacion de ArchivoB1_CTRL_BROAD_CTAS.txt");
        assertThat(l.subList(i, i + 3)).containsExactly(
                "INF - Fin de creacion de ArchivoB1_CTRL_BROAD_CTAS.txt",
                "ERR - Broad Movimiento Contable: fecha fuera de rango",
                "INF - Creando Archivo: B1_BROAD_SALD_CONT.TXT");
        assertThat(l).noneMatch(s -> s.contains("B1_BROAD_MOV_CONT") || s.contains("B1_CTRL_BROAD_MOV_CONT"));
    }
}
