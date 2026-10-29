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
import cl.bcs.contabilidad.jobs.support.H2Jobs.Estados.Estado;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Decision 2026-10-15, point 5, for legacy {@code BcsContabilidadJobs/src/cl/bcs/contabilidad/jobs/CreaInterfazDiaria.java}:
 * <ul>
 *   <li>(i) the run log holds, besides the job's own lines, the lines the legacy {@code Crea*} generators of
 *       BcsContabilidadServicios wrote into the same {@code cl.bcs.contabilidad.Log} (they received it as their last
 *       argument, :123-188), with their exact text and order;</li>
 *   <li>(ii) when the SAP connection cannot be obtained (:111-113, inside {@code logica}'s try that starts at :76),
 *       nothing is generated and nothing is uploaded: the outer catch logs the message (:229-232) and
 *       {@code transaction()} still returns true (:264-274).</li>
 * </ul>
 * The full log is read raw here (unlike {@link Archivos#lineasJob}, which hides the generator lines). Same clock as
 * CreaInterfazDiariaTest: today 2026-02-28 in America/Santiago, fechaEjecucion 2026-02-27.
 */
class CreaInterfazDiariaBitacoraTest {

    static final String CLASE = "cl.bcs.contabilidad.jobs.CreaInterfazDiaria";
    static final Clock RELOJ = Clock.fixed(Instant.parse("2026-03-01T02:30:00Z"), ZoneId.of("America/Santiago"));

    /** One data file + control family of CreaBroadCuentas / CreaBroadMovimiento / CreaBroadSaldo / CreaCuentasBPC. */
    static List<String> familia(String datos, String control) {
        return List.of(
                "INF - Creando Archivo: " + datos,                 // e.g. CreaBroadCuentas.java:83
                "INF - Fin de creacion de Archivo: " + datos,      // :130
                "INF - Creando Archivo: " + control,               // :153
                "INF - Fin de creacion de Archivo" + control);     // :201 (no ": " in the legacy text)
    }

    /** One CreaDescriptivasBroad file (no control file). */
    static List<String> descriptiva(String nombre) {
        return List.of("INF - Creando Archivo: " + nombre,           // CreaDescriptivasBroad.java:139 / :193 / :243
                "INF - Fin de creacion de Archivo: " + nombre);    // :169 / :221 / :273
    }

    @TempDir
    Path tmp;

    private String base;
    private final H2Jobs db = H2Jobs.nueva();
    private final PlataformaPort plataforma = mock(PlataformaPort.class);
    private final InterfazContadorSource contador = mock(InterfazContadorSource.class);
    private final InterfazFileTransfer sftp = mock(InterfazFileTransfer.class);
    private final FakeInterfazSapSource sap = new FakeInterfazSapSource();
    private final Supplier<SftpSettings> settings =
            () -> new SftpSettings("sftp.invalid", 22, "fake-sftp-user", "fake-sftp-pass-0000", Path.of("known_hosts"));

    @BeforeEach
    void preparar() {
        base = tmp.resolve("archivos").toString() + File.separator;
        when(plataforma.obtenerParametroConfiguracion(eq(1L), eq("PLATAFORMA"), eq("PLA.RutaBaseArchivos"), any())).thenReturn(base);
        when(contador.contadoresInterfazSiga(any())).thenReturn(List.of(7L));
    }

    private JobResultado ejecutar(ConexionSap conexion) {
        return db.runner(new CreaInterfazDiaria(plataforma, contador, sap, conexion, sftp, settings, RELOJ))
                .ejecutar(CLASE, 300L, Map.of());
    }

    private List<String> log() {
        String txt = Archivos.leer(Path.of(base + "logs/LOG_ServiceBROAD_20260228.log"));
        return Arrays.stream(txt.split("\r\n")).filter(l -> !l.isEmpty()).toList();
    }

    @Test
    @DisplayName("CONT-J-38 (5.ii): the SAP connection cannot be obtained -> 'INF - Estableciendo conexion a SAP', 'ERR - <message>'; no file, no SFTP; COMMIT")
    void contJ38_sinConexionSap_abortaGeneracion() {
        // CreaInterfazDiaria.java:95 (contador first), :109 (INF line), :111-113 getConnection() throws,
        // :229-232 outer catch logs e.getMessage(); the SFTP block (:198-226) is inside the same try, so it is skipped;
        // logica returns normally and transaction() returns true (:264-274).
        JobResultado r = ejecutar(() -> { throw new IllegalStateException("SAP B1: conexion rechazada"); });

        assertThat(r.desenlace()).isEqualTo(Desenlace.COMMIT);
        assertThat(db.estados().llamadas()).containsExactly(new Estado(300L, false, ""));
        assertThat(log()).containsExactly("INF - Estableciendo conexion a SAP", "ERR - SAP B1: conexion rechazada");
        verify(contador).contadoresInterfazSiga(LocalDate.of(2026, 2, 27));
        assertThat(sap.llamadas()).isEmpty();
        verifyNoInteractions(sftp);
        assertThat(Archivos.arbol(Path.of(base))).containsExactly("logs/LOG_ServiceBROAD_20260228.log");
    }

    @Test
    @DisplayName("CONT-J-39 (5.i): clean run -> the job lines and every generator line, in the legacy text and order")
    void contJ39_lineasDeLosGeneradores() {
        // CreaInterfazDiaria.java:109, :117, then :118-193 in order; per generator: CreaBroadCuentas.java:83,:130,:153,:201;
        // CreaBroadMovimiento.java:102,:179,:201,:259; CreaBroadSaldo.java:97,:151,:172,:229;
        // CreaDescriptivasBroad.java:139,:169,:193,:221,:243,:273; CreaCuentasBPC.java:86,:132,:154,:199;
        // CreaMovimientoBPC.java:98,:156,:177,:247 (the only control "Fin" line WITH ": ").
        assertThat(ejecutar(ConexionSap.IMPLICITA).desenlace()).isEqualTo(Desenlace.COMMIT);

        List<String> esperado = new ArrayList<>(List.of(
                "INF - Estableciendo conexion a SAP", "INF - Iniciando generacion de archivos"));
        esperado.addAll(familia("B1_BROAD_CTAS.TXT", "B1_CTRL_BROAD_CTAS.TXT"));
        esperado.addAll(familia("B1_BROAD_MOV_CONT.TXT", "B1_CTRL_BROAD_MOV_CONT.TXT"));
        esperado.addAll(familia("B1_BROAD_SALD_CONT.TXT", "B1_CTRL_BROAD_SALD_CONT.TXT"));
        esperado.addAll(descriptiva("B1_DES_ENTIDAD.TXT"));
        esperado.addAll(descriptiva("B1_DES_TIPO_CTA.TXT"));
        esperado.addAll(descriptiva("B1_DES_CC_PROD.TXT"));
        esperado.addAll(familia("B1_CUENTAS_BPC.TXT", "B1_CTRL_CUENTAS_BPC.TXT"));
        esperado.addAll(List.of(
                "INF - Creando Archivo: B1_BROAD_BPC.TXT",
                "INF - Fin de creacion de Archivo: B1_BROAD_BPC.TXT",
                "INF - Creando Archivo: B1_CTRL_BROAD_BPC.TXT",
                "INF - Fin de creacion de Archivo: B1_CTRL_BROAD_BPC.TXT"));
        assertThat(log()).containsExactlyElementsOf(esperado);
    }

    @Test
    @DisplayName("CONT-J-39b (5.i): a Descriptivas query fails -> 'Creando Archivo' THEN 'ERR - <message>' (its own catch); the next file is still made")
    void contJ39b_descriptivaFallaDentroDelGenerador() {
        // CreaDescriptivasBroad.java:137-174: "Creando Archivo" (:139) BEFORE the query (:141); catch (Exception) logs
        // e.getMessage() (:171-173) and swallows it, so the job's own catch (CreaInterfazDiaria.java:165-168) never fires.
        sap.fallar("entidades", new IllegalStateException("SAP B1: vista OADM no disponible"));

        ejecutar(ConexionSap.IMPLICITA);

        List<String> l = log();
        int i = l.indexOf("INF - Creando Archivo: B1_DES_ENTIDAD.TXT");
        assertThat(i).isPositive();
        assertThat(l.subList(i, i + 3)).containsExactly(
                "INF - Creando Archivo: B1_DES_ENTIDAD.TXT",
                "ERR - SAP B1: vista OADM no disponible",
                "INF - Creando Archivo: B1_DES_TIPO_CTA.TXT");
        assertThat(l).filteredOn(s -> s.startsWith("ERR")).containsExactly("ERR - SAP B1: vista OADM no disponible");
        assertThat(Archivos.nombres(Path.of(base + "Transmisiones/Salida/FI/")))
                .doesNotContain("B1_DES_ENTIDAD.TXT").contains("B1_DES_TIPO_CTA.TXT", "B1_DES_CC_PROD.TXT");
    }

    @Test
    @DisplayName("CONT-J-39c (5.i): a Broad query fails -> no 'Creando Archivo' line for that family, only the job's 'ERR - <message>'")
    void contJ39c_consultaFallaAntesDeCrear() {
        // CreaBroadCuentas.java:76 runs the query BEFORE the try that logs "Creando Archivo" (:80-83); the exception
        // reaches the job's catch (CreaInterfazDiaria.java:125-128).
        sap.fallar("cuentasBroad", new IllegalStateException("SAP B1: OACT no responde"));

        ejecutar(ConexionSap.IMPLICITA);

        List<String> l = log();
        assertThat(l.subList(0, 4)).containsExactly(
                "INF - Estableciendo conexion a SAP",
                "INF - Iniciando generacion de archivos",
                "ERR - SAP B1: OACT no responde",
                "INF - Creando Archivo: B1_BROAD_MOV_CONT.TXT");
        assertThat(l).noneMatch(s -> s.contains("B1_BROAD_CTAS") || s.contains("B1_CTRL_BROAD_CTAS"));
    }
}
