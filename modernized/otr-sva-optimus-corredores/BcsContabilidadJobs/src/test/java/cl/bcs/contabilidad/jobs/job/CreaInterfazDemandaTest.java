package cl.bcs.contabilidad.jobs.job;

import cl.bcs.contabilidad.domain.port.out.InterfazContadorSource;
import cl.bcs.contabilidad.domain.port.out.InterfazFileTransfer;
import cl.bcs.contabilidad.domain.port.out.PlataformaPort;
import cl.bcs.contabilidad.infrastructure.sftp.SftpConfigurationException;
import cl.bcs.contabilidad.infrastructure.sftp.SftpSettings;
import cl.bcs.contabilidad.infrastructure.sftp.SftpTransferException;
import cl.bcs.contabilidad.jobs.runtime.JobResultado;
import cl.bcs.contabilidad.jobs.runtime.JobResultado.Desenlace;
import cl.bcs.contabilidad.jobs.support.Archivos;
import cl.bcs.contabilidad.jobs.support.FakeInterfazSapSource;
import cl.bcs.contabilidad.jobs.support.H2Jobs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;

import java.io.File;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Job shell of legacy {@code BcsContabilidadJobs/src/cl/bcs/contabilidad/jobs/CreaInterfazDemanda.java}, pinned by its
 * DIFFERENCES with CreaInterfazDiaria, read from the source:
 * <ul>
 *   <li>working path = PLA.RutaBaseArchivos + "APPS/GAB1/" (:72-74); log and files live there, SFTP uploads it (:224-225);</li>
 *   <li>its previous contents are deleted first, the directory itself is kept (:80-84, BorrarDirectorio :266-278);</li>
 *   <li>the date is the FechaEjecucion job param, not yesterday (:88);</li>
 *   <li>five steps, no Descriptivas (:120-211); names: {@code B1_BROAD_CTAS.txt}/{@code B1_CTRL_BROAD_CTAS.txt}
 *       lower-case .txt (:122-123), {@code *_BPC_DM.TXT} (:179-180, :197-198);</li>
 *   <li>error lines are prefixed with the step label; IllegalArgumentException logs the message, any other exception
 *       logs the message only for "Broad Cuenta" and the first stack frame for the others (:127-211);</li>
 *   <li>two more INF lines, "Fin de generacion de archivos" (:213) and "Subiendo archivos al directorio destino" (:217);</li>
 *   <li>the outer catch logs the message for IllegalArgumentException and {@code getStackTrace()[0]} otherwise (:248-257).</li>
 * </ul>
 * Same as Diaria: CreaInterfazSIGA.properties (now the SFTP settings) missing -> false (:293-307); otherwise true.
 */
class CreaInterfazDemandaTest {

    static final String CLASE = "cl.bcs.contabilidad.jobs.CreaInterfazDemanda";
    static final Clock RELOJ = Clock.fixed(Instant.parse("2026-03-16T14:00:00Z"), ZoneId.of("America/Santiago")); // 2026-03-16 local

    static final List<String> FI = List.of(
            "B1_BROAD_CTAS.txt", "B1_BROAD_MOV_CONT.TXT", "B1_BROAD_SALD_CONT.TXT",
            "B1_CTRL_BROAD_CTAS.txt", "B1_CTRL_BROAD_MOV_CONT.TXT", "B1_CTRL_BROAD_SALD_CONT.TXT");
    static final List<String> BPC = List.of(
            "B1_BROAD_BPC_DM.TXT", "B1_CTRL_BROAD_BPC_DM.TXT", "B1_CTRL_CUENTAS_BPC_DM.TXT", "B1_CUENTAS_BPC_DM.TXT");

    static final List<String> LINEAS_OK = List.of(
            "INF - Estableciendo conexion a SAP", "INF - Iniciando generacion de archivos",
            "INF - Fin de generacion de archivos", "INF - Subiendo archivos al directorio destino");

    @TempDir
    Path tmp;

    private String base;
    private String gab1;
    private final H2Jobs db = H2Jobs.nueva();
    private final PlataformaPort plataforma = mock(PlataformaPort.class);
    private final InterfazContadorSource contador = mock(InterfazContadorSource.class);
    private final InterfazFileTransfer sftp = mock(InterfazFileTransfer.class);
    private FakeInterfazSapSource sap;
    private Supplier<SftpSettings> settings;

    @BeforeEach
    void preparar() {
        base = tmp.resolve("archivos").toString() + File.separator;
        gab1 = base + "APPS/GAB1/";
        when(plataforma.obtenerParametroConfiguracion(eq(1L), eq("PLATAFORMA"), eq("PLA.RutaBaseArchivos"), any())).thenReturn(base);
        when(contador.contadoresInterfazSiga(any())).thenReturn(List.of(4L));
        sap = new FakeInterfazSapSource();
        // fake, same-shape SFTP settings (never used to connect: the transfer is a mock)
        settings = () -> new SftpSettings("sftp.invalid", 22, "fake-sftp-user", "fake-sftp-pass-0000", tmp.resolve("known_hosts"));
    }

    private JobResultado ejecutar(Map<String, String> params) {
        return db.runner(new CreaInterfazDemanda(plataforma, contador, sap, sftp, settings, RELOJ)).ejecutar(CLASE, 301L, params);
    }

    private JobResultado ejecutar() {
        return ejecutar(Map.of("FechaEjecucion", "2026-03-15"));
    }

    private Path log() {
        return Path.of(gab1 + "logs/LOG_ServiceBROAD_20260316.log");
    }

    private Path fi() {
        return Path.of(gab1 + "Transmisiones/Salida/FI/");
    }

    private Path bpc() {
        return Path.of(gab1 + "Transmisiones/Salida/BPC/");
    }

    @Test
    @DisplayName("CONT-J-40: the date is the FechaEjecucion param (2026-02-10), not today-1 (2026-03-15)")
    void contJ40_fechaDelParametro() {
        // CreaInterfazDemanda.java:88 this.getParam("FechaEjecucion")
        ejecutar(Map.of("FechaEjecucion", "2026-02-10"));

        verify(contador).contadoresInterfazSiga(LocalDate.of(2026, 2, 10));
        assertThat(sap.llamadas()).contains("movimientosBroad(2026-02-10)", "saldosBroad(2026-02-10)", "movimientosBpc(2026-02-10)");
    }

    @Test
    @DisplayName("CONT-J-41: files under <base>APPS/GAB1/Transmisiones/Salida/{FI,BPC}/ with the Demanda names; nothing directly under <base>Transmisiones")
    void contJ41_archivosYRutas() {
        // CreaInterfazDemanda.java:73-74, :116-117, :122-198
        assertThat(ejecutar().desenlace()).isEqualTo(Desenlace.COMMIT);

        assertThat(Archivos.nombres(fi())).isEqualTo(FI);
        assertThat(Archivos.nombres(bpc())).isEqualTo(BPC);
        assertThat(Archivos.arbol(Path.of(base))).allMatch(p -> p.startsWith("APPS/GAB1/"));
        assertThat(Path.of(base + "Transmisiones")).doesNotExist();
    }

    @Test
    @DisplayName("CONT-J-42: five steps in order, no Descriptivas: cuentas, movimiento, saldo, cuentas BPC, movimiento BPC")
    void contJ42_ordenSinDescriptivas() {
        // CreaInterfazDemanda.java:120-211 (no CreaDescriptivasBroad, unlike CreaInterfazDiaria.java:155-168)
        ejecutar();

        assertThat(sap.llamadas()).containsExactly("cuentasBroad", "movimientosBroad(2026-03-15)", "saldosBroad(2026-03-15)",
                "cuentasBpc", "movimientosBpc(2026-03-15)");
    }

    @ParameterizedTest(name = "CONT-J-43: control file {0} names its data file {1}")
    @CsvSource({
            "FI,  B1_CTRL_BROAD_CTAS.txt,     B1_BROAD_CTAS.txt",
            "FI,  B1_CTRL_BROAD_MOV_CONT.TXT, B1_BROAD_MOV_CONT.TXT",
            "BPC, B1_CTRL_CUENTAS_BPC_DM.TXT, B1_CUENTAS_BPC_DM.TXT",
            "BPC, B1_CTRL_BROAD_BPC_DM.TXT,   B1_BROAD_BPC_DM.TXT"})
    void contJ43_controlNombraArchivoDemanda(String dir, String control, String datos) {
        // CreaBroadCuentas.java:175 / CreaCuentasBPC.java:174 / ...: ac.setDesEstructura(String.format("%1$-30s", nombreArchivo))
        ejecutar();

        Path d = dir.equals("FI") ? fi() : bpc();
        String txt = Archivos.leerLatin1(d.resolve(control));
        assertThat(txt).contains(String.format(Locale.ROOT, "%-30s", datos));
        if (!datos.equals(datos.toUpperCase(Locale.ROOT)) || datos.contains("_DM")) {
            String nombreDiaria = datos.replace("_DM", "").replace(".txt", ".TXT");
            assertThat(txt).doesNotContain(String.format(Locale.ROOT, "%-30s", nombreDiaria));
        }
    }

    @Test
    @DisplayName("CONT-J-44: previous contents of APPS/GAB1/ (files, sub-folders, today's log) are deleted; GAB1 and its siblings stay")
    void contJ44_borraDirectorioPrevio() {
        // CreaInterfazDemanda.java:80-84 + BorrarDirectorio :266-278 (deletes children recursively, not the folder)
        Archivos.escribir(Path.of(gab1 + "viejo/anidado/resto.txt"), "x");
        Archivos.escribir(Path.of(gab1 + "suelto.TXT"), "x");
        Archivos.escribir(log(), "INF - corrida anterior\r\n");
        Archivos.escribir(Path.of(base + "APPS/OTRO/conservar.txt"), "x");
        Archivos.escribir(Path.of(base + "conservar.txt"), "x");

        ejecutar();

        assertThat(Path.of(gab1 + "viejo")).doesNotExist();
        assertThat(Path.of(gab1 + "suelto.TXT")).doesNotExist();
        assertThat(Archivos.lineasJob(log())).isEqualTo(LINEAS_OK);
        assertThat(Path.of(base + "APPS/OTRO/conservar.txt")).exists();
        assertThat(Path.of(base + "conservar.txt")).exists();
    }

    @Test
    @DisplayName("CONT-J-45: job log <base>APPS/GAB1/logs/LOG_ServiceBROAD_<today>.log has the four INF lines of a clean run")
    void contJ45_lineasDeLog() {
        // CreaInterfazDemanda.java:75 (Log(path) with the GAB1 path), :110, :118, :213, :217
        ejecutar();

        assertThat(Archivos.lineasJob(log())).isEqualTo(LINEAS_OK);
        assertThat(Path.of(base + "logs")).doesNotExist();
    }

    @ParameterizedTest(name = "CONT-J-46: IllegalArgumentException in {0} -> 'ERR - {1}: dato invalido', other steps still run")
    @CsvSource(delimiter = '|', value = {
            "cuentasBroad|Broad Cuenta",
            "movimientosBroad|Broad Movimiento Contable",
            "saldosBroad|Broad Saldo Contable",
            "cuentasBpc|Cuenta BPC",
            "movimientosBpc|Movimiento BCP"})
    void contJ46_etiquetaConMensajeParaIAE(String metodo, String etiqueta) throws Exception {
        // CreaInterfazDemanda.java:127-131, :146-150, :165-169, :184-188, :202-206 ("Movimiento BCP" typo kept)
        sap.fallar(metodo, new IllegalArgumentException("dato invalido"));

        assertThat(ejecutar().desenlace()).isEqualTo(Desenlace.COMMIT);

        assertThat(Archivos.lineasJob(log())).containsExactly(LINEAS_OK.get(0), LINEAS_OK.get(1),
                "ERR - " + etiqueta + ": dato invalido", LINEAS_OK.get(2), LINEAS_OK.get(3));
        assertThat(sap.llamadas()).hasSize(5);
        verify(sftp).uploadDirectory(anyString(), any());
    }

    @ParameterizedTest(name = "CONT-J-47: IllegalStateException in {0} -> 'ERR - {1}: ' + {2}")
    @CsvSource(delimiter = '|', value = {
            "cuentasBroad|Broad Cuenta|mensaje",
            "movimientosBroad|Broad Movimiento Contable|frame",
            "saldosBroad|Broad Saldo Contable|frame",
            "cuentasBpc|Cuenta BPC|frame",
            "movimientosBpc|Movimiento BCP|frame"})
    void contJ47_otrasExcepciones(String metodo, String etiqueta, String forma) {
        // CreaInterfazDemanda.java:132-136 (message) vs :151-155, :170-174, :189-193, :207-211 (e.getStackTrace()[0])
        IllegalStateException e = new IllegalStateException("conexion SAP perdida");
        sap.fallar(metodo, e);

        ejecutar();

        String esperado = "ERR - " + etiqueta + ": " + (forma.equals("mensaje") ? "conexion SAP perdida" : e.getStackTrace()[0].toString());
        assertThat(Archivos.lineasJob(log())).contains(esperado);
    }

    @Test
    @DisplayName("CONT-J-48: SFTP removes and uploads the GAB1 folder ('/home/interfaz-bpc/', <base>APPS/GAB1/), after the 'Subiendo' line")
    void contJ48_sftpDeGab1() throws Exception {
        // CreaInterfazDemanda.java:217-226
        ejecutar();

        InOrder o = inOrder(sftp);
        o.verify(sftp).removeRemoteCopy("/home/interfaz-bpc/", Path.of(gab1));
        o.verify(sftp).uploadDirectory("/home/interfaz-bpc/", Path.of(gab1));
        o.verifyNoMoreInteractions();
    }

    @Test
    @DisplayName("CONT-J-49: SFTP IOException -> 'ERR - <message>' after 'Subiendo...', upload skipped when removal failed; still COMMIT")
    void contJ49_sftpIOException() throws Exception {
        // CreaInterfazDemanda.java:227-245
        doThrow(new SftpTransferException("sftp: host key rechazada")).when(sftp).removeRemoteCopy(anyString(), any());

        assertThat(ejecutar().desenlace()).isEqualTo(Desenlace.COMMIT);
        verify(sftp, never()).uploadDirectory(anyString(), any());
        List<String> l = Archivos.lineasJob(log());
        assertThat(l.subList(l.size() - 2, l.size()))
                .containsExactly("INF - Subiendo archivos al directorio destino", "ERR - sftp: host key rechazada");
    }

    @Test
    @DisplayName("CONT-J-50: a RuntimeException from the transfer reaches the outer catch: 'ERR - ' + its first stack frame")
    void contJ50_sftpRuntime() throws Exception {
        // CreaInterfazDemanda.java:253-257 e.getStackTrace()[0].toString()
        SftpConfigurationException e = new SftpConfigurationException("SFTP_PORT is not a number");
        doThrow(e).when(sftp).removeRemoteCopy(anyString(), any());

        assertThat(ejecutar().desenlace()).isEqualTo(Desenlace.COMMIT);
        assertThat(Archivos.lineasJob(log())).endsWith("ERR - " + e.getStackTrace()[0]);
    }

    @ParameterizedTest(name = "CONT-J-51 (DEV-J-03): FechaEjecucion \"{0}\" -> one 'ERR - ' line, no SAP query, no SFTP; GAB1 still emptied; COMMIT")
    @NullSource
    @ValueSource(strings = {"", "15/03/2026", "2026-02-30"})
    void contJ51_fechaInvalida(String fecha) {
        // Legacy passed the raw string to the SQL and the generators; here an unparseable date is a controlled error.
        Archivos.escribir(Path.of(gab1 + "viejo.txt"), "x");
        Map<String, String> p = new HashMap<>();
        if (fecha != null) p.put("FechaEjecucion", fecha);

        JobResultado r = ejecutar(p);

        assertThat(r.desenlace()).isEqualTo(Desenlace.COMMIT);
        assertThat(sap.llamadas()).isEmpty();
        verifyNoInteractions(sftp, contador);
        assertThat(Path.of(gab1 + "viejo.txt")).doesNotExist();
        assertThat(Archivos.lineasJob(log())).singleElement().asString().startsWith("ERR - ");
    }

    @Test
    @DisplayName("CONT-J-52: contador query IllegalStateException -> 'ERR - ' + first stack frame; IllegalArgumentException -> 'ERR - ' + message")
    void contJ52_falloContador() {
        // CreaInterfazDemanda.java:91 inside the outer try; :248-257
        IllegalStateException ise = new IllegalStateException("HANA no disponible");
        when(contador.contadoresInterfazSiga(any())).thenThrow(ise);
        ejecutar();
        assertThat(Archivos.lineasJob(log())).containsExactly("ERR - " + ise.getStackTrace()[0]);
        assertThat(sap.llamadas()).isEmpty();
        verifyNoInteractions(sftp);

        doThrow(new IllegalArgumentException("FechaEjecucion fuera de rango")).when(contador).contadoresInterfazSiga(any());
        ejecutar();
        assertThat(Archivos.lineasJob(log())).containsExactly("ERR - FechaEjecucion fuera de rango");
    }

    @Test
    @DisplayName("CONT-J-53: SFTP settings unavailable -> ROLLBACK before anything: GAB1 is NOT emptied, no log, no query")
    void contJ53_sinConfiguracionSftp_rollback() {
        // CreaInterfazDemanda.java:297-306 (properties loaded before logica)
        Archivos.escribir(Path.of(gab1 + "viejo.txt"), "x");
        settings = () -> { throw new SftpConfigurationException("Missing required environment variable SFTP_KNOWN_HOSTS"); };

        assertThat(ejecutar().desenlace()).isEqualTo(Desenlace.ROLLBACK);
        assertThat(Path.of(gab1 + "viejo.txt")).exists();
        assertThat(log()).doesNotExist();
        verifyNoInteractions(plataforma, contador, sftp);
    }

    @Test
    @DisplayName("CONT-J-54: the PLA.RutaBaseArchivos lookup fails -> ROLLBACK; empty -> ROLLBACK (DEV-J-02)")
    void contJ54_rutaBase() {
        // CreaInterfazDemanda.java:72 outside the try
        when(plataforma.obtenerParametroConfiguracion(eq(1L), eq("PLATAFORMA"), eq("PLA.RutaBaseArchivos"), any()))
                .thenThrow(new IllegalStateException("HANA caida"));
        assertThat(ejecutar().desenlace()).isEqualTo(Desenlace.ROLLBACK);

        doReturn("").when(plataforma).obtenerParametroConfiguracion(eq(1L), eq("PLATAFORMA"), eq("PLA.RutaBaseArchivos"), any());
        assertThat(ejecutar().desenlace()).isEqualTo(Desenlace.ROLLBACK);
        assertThat(sap.llamadas()).isEmpty();
        verifyNoInteractions(sftp);
    }

    @Test
    @DisplayName("CONT-J-55: registered under the legacy class name cl.bcs.contabilidad.jobs.CreaInterfazDemanda")
    void contJ55_clase() {
        assertThat(new CreaInterfazDemanda(plataforma, contador, sap, sftp, settings, RELOJ).clase()).isEqualTo(CLASE);
    }
}
