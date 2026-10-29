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
import cl.bcs.contabilidad.jobs.support.H2Jobs.Estados.Estado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InOrder;

import java.io.File;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Job shell of legacy {@code BcsContabilidadJobs/src/cl/bcs/contabilidad/jobs/CreaInterfazDiaria.java}.
 *
 * <p>Legacy flow: {@code transaction()} loads {@code CreaInterfazSIGA.properties} from the working directory and returns
 * false (rollback) when it cannot (:258-269), now the SFTP settings supplier; {@code logica()} reads
 * PLA.RutaBaseArchivos (empresa 1, PLATAFORMA) outside its try (:74), opens {@code <base>logs/LOG_ServiceBROAD_yyyyMMdd.log}
 * (Log.java:25), takes fechaEjecucion = today - 1 day (:79-90, the FechaEjecucion param is NOT read, :82), reads the
 * contador (first row, :93-103), generates the files each in its own try/catch (:118-193), then removes the remote copy
 * and uploads the base path over SFTP (:198-226). Everything inside logica's try is logged and swallowed (:229-232).
 *
 * <p>The generators, SAP queries and SFTP client are BcsContabilidadServicios ({@code InterfazGenerators},
 * {@code InterfazSapSource}, {@code InterfazFileTransfer}); they are faked at that boundary. Clock: 2026-03-01T02:30Z
 * is 2026-02-28 23:30 in America/Santiago, so "today" is 2026-02-28 (not the UTC date) and fechaEjecucion 2026-02-27.
 */
class CreaInterfazDiariaTest {

    static final String CLASE = "cl.bcs.contabilidad.jobs.CreaInterfazDiaria";
    static final ZoneId SANTIAGO = ZoneId.of("America/Santiago");
    static final Clock RELOJ = Clock.fixed(Instant.parse("2026-03-01T02:30:00Z"), SANTIAGO);

    static final List<String> FI = List.of(
            "B1_BROAD_CTAS.TXT", "B1_BROAD_MOV_CONT.TXT", "B1_BROAD_SALD_CONT.TXT",
            "B1_CTRL_BROAD_CTAS.TXT", "B1_CTRL_BROAD_MOV_CONT.TXT", "B1_CTRL_BROAD_SALD_CONT.TXT",
            "B1_DES_CC_PROD.TXT", "B1_DES_ENTIDAD.TXT", "B1_DES_TIPO_CTA.TXT");
    static final List<String> BPC = List.of(
            "B1_BROAD_BPC.TXT", "B1_CTRL_BROAD_BPC.TXT", "B1_CTRL_CUENTAS_BPC.TXT", "B1_CUENTAS_BPC.TXT");

    @TempDir
    Path tmp;

    private String base;
    private final H2Jobs db = H2Jobs.nueva();
    private final PlataformaPort plataforma = mock(PlataformaPort.class);
    private final InterfazContadorSource contador = mock(InterfazContadorSource.class);
    private final InterfazFileTransfer sftp = mock(InterfazFileTransfer.class);
    private FakeInterfazSapSource sap;
    private Supplier<SftpSettings> settings;

    @BeforeEach
    void preparar() {
        base = tmp.resolve("archivos").toString() + File.separator;
        when(plataforma.obtenerParametroConfiguracion(eq(1L), eq("PLATAFORMA"), eq("PLA.RutaBaseArchivos"), any())).thenReturn(base);
        when(contador.contadoresInterfazSiga(any())).thenReturn(List.of(7L));
        sap = new FakeInterfazSapSource();
        // fake, same-shape SFTP settings (never used to connect: the transfer is a mock)
        settings = () -> new SftpSettings("sftp.invalid", 22, "fake-sftp-user", "fake-sftp-pass-0000", tmp.resolve("known_hosts"));
    }

    private JobResultado ejecutar(Clock reloj, Map<String, String> params) {
        CreaInterfazDiaria job = new CreaInterfazDiaria(plataforma, contador, sap, sftp, settings, reloj);
        return db.runner(job).ejecutar(CLASE, 300L, params);
    }

    private JobResultado ejecutar() {
        return ejecutar(RELOJ, Map.of());
    }

    private Path log() {
        return Path.of(base + "logs/LOG_ServiceBROAD_20260228.log");
    }

    private Path fi() {
        return Path.of(base + "Transmisiones/Salida/FI/");
    }

    private Path bpc() {
        return Path.of(base + "Transmisiones/Salida/BPC/");
    }

    @ParameterizedTest(name = "CONT-J-20: now {0} in America/Santiago -> fechaEjecucion {1}")
    @CsvSource({
            "2026-03-01T02:30:00Z, 2026-02-27",   // local date 2026-02-28 (UTC date would give 2026-02-28)
            "2024-03-01T15:00:00Z, 2024-02-29",   // leap day
            "2026-01-01T12:00:00Z, 2025-12-31"})  // year boundary
    void contJ20_fechaEsAyerEnZonaDelReloj(String ahora, String fecha) {
        // CreaInterfazDiaria.java:79-90 new Date() -> Calendar.add(DAY_OF_YEAR, -1)
        ejecutar(Clock.fixed(Instant.parse(ahora), SANTIAGO), Map.of());

        LocalDate f = LocalDate.parse(fecha);
        verify(contador).contadoresInterfazSiga(f);
        assertThat(sap.llamadas()).contains("movimientosBroad(" + f + ")", "saldosBroad(" + f + ")", "movimientosBpc(" + f + ")");
    }

    @Test
    @DisplayName("CONT-J-21: the FechaEjecucion job param is ignored (legacy reads it only in a commented-out line)")
    void contJ21_ignoraParametroFechaEjecucion() {
        // CreaInterfazDiaria.java:82 //String _fechaEjecucion = this.getParam("fechaEjecucion");
        ejecutar(RELOJ, Map.of("FechaEjecucion", "2020-01-01", "fechaEjecucion", "2020-01-01"));

        verify(contador).contadoresInterfazSiga(LocalDate.of(2026, 2, 27));
        assertThat(String.join(",", sap.llamadas())).doesNotContain("2020-01-01");
    }

    @Test
    @DisplayName("CONT-J-22: files: 9 under <base>Transmisiones/Salida/FI/ and 4 under .../BPC/, upper-case .TXT, nothing else besides the log")
    void contJ22_archivosYRutas() {
        // CreaInterfazDiaria.java:115-116 (paths), :120-188 (names)
        assertThat(ejecutar().desenlace()).isEqualTo(Desenlace.COMMIT);

        assertThat(Archivos.nombres(fi())).isEqualTo(FI);
        assertThat(Archivos.nombres(bpc())).isEqualTo(BPC);
        List<String> esperado = new ArrayList<>();
        BPC.forEach(n -> esperado.add("Transmisiones/Salida/BPC/" + n));
        FI.forEach(n -> esperado.add("Transmisiones/Salida/FI/" + n));
        esperado.add("logs/LOG_ServiceBROAD_20260228.log");
        assertThat(Archivos.arbol(Path.of(base))).containsExactlyInAnyOrderElementsOf(esperado);
    }

    @Test
    @DisplayName("CONT-J-23: generation order: cuentas, movimiento, saldo, descriptivas (entidad, tipo cuenta, centro costo), cuentas BPC, movimiento BPC")
    void contJ23_ordenDeGeneracion() {
        // CreaInterfazDiaria.java:118-193
        ejecutar();

        assertThat(sap.llamadas()).containsExactly(
                "cuentasBroad", "movimientosBroad(2026-02-27)", "saldosBroad(2026-02-27)",
                "entidades", "tiposCuenta", "centrosCosto",
                "cuentasBpc", "movimientosBpc(2026-02-27)");
    }

    @Test
    @DisplayName("CONT-J-24: job log lines on a clean run: 'INF - Estableciendo conexion a SAP', 'INF - Iniciando generacion de archivos', CRLF")
    void contJ24_lineasDeLog() {
        // CreaInterfazDiaria.java:109, :117; Log.java:25 (<base>logs/LOG_ServiceBROAD_yyyyMMdd.log), :33 ("TIPO - msg\r\n")
        ejecutar();

        assertThat(Archivos.lineasJob(log())).containsExactly(
                "INF - Estableciendo conexion a SAP", "INF - Iniciando generacion de archivos");
    }

    @Test
    @DisplayName("CONT-J-25: the day's log is appended to, not truncated")
    void contJ25_logSeAnexa() {
        // Log.java:34 FileUtils.writeStringToFile(file, msg, true)
        Archivos.escribir(log(), "INF - corrida anterior\r\n");
        ejecutar();

        assertThat(Archivos.lineasJob(log())).containsExactly("INF - corrida anterior",
                "INF - Estableciendo conexion a SAP", "INF - Iniciando generacion de archivos");
    }

    @ParameterizedTest(name = "CONT-J-26: {0} fails -> 'ERR - <message>', the other steps still run")
    @CsvSource(delimiter = '|', value = {
            "cuentasBroad|B1_BROAD_CTAS.TXT,B1_CTRL_BROAD_CTAS.TXT",
            "movimientosBroad|B1_BROAD_MOV_CONT.TXT,B1_CTRL_BROAD_MOV_CONT.TXT",
            "saldosBroad|B1_BROAD_SALD_CONT.TXT,B1_CTRL_BROAD_SALD_CONT.TXT",
            "cuentasBpc|B1_CUENTAS_BPC.TXT,B1_CTRL_CUENTAS_BPC.TXT",
            "movimientosBpc|B1_BROAD_BPC.TXT,B1_CTRL_BROAD_BPC.TXT"})
    void contJ26_tryCatchPorArchivo(String metodo, String faltantes) throws Exception {
        // CreaInterfazDiaria.java:118-193: each file family in its own try { } catch (Exception e) { log ERR e.getMessage() }
        sap.fallar(metodo, new IllegalStateException("SAP B1 no responde (" + metodo + ")"));

        assertThat(ejecutar().desenlace()).isEqualTo(Desenlace.COMMIT);

        assertThat(Archivos.lineasJob(log())).containsExactly(
                "INF - Estableciendo conexion a SAP", "INF - Iniciando generacion de archivos",
                "ERR - SAP B1 no responde (" + metodo + ")");
        List<String> generados = new ArrayList<>(Archivos.nombres(fi()));
        generados.addAll(Archivos.nombres(bpc()));
        List<String> todos = new ArrayList<>(FI);
        todos.addAll(BPC);
        todos.removeAll(List.of(faltantes.split(",")));
        assertThat(generados).containsExactlyInAnyOrderElementsOf(todos);
        assertThat(sap.llamadas()).hasSize(8);
        verify(sftp).uploadDirectory(anyString(), any());
    }

    @Test
    @DisplayName("CONT-J-27: a failure without message is logged as 'ERR - null'")
    void contJ27_mensajeNulo() {
        // CreaInterfazDiaria.java:127 log.EscribirLog("ERR", e.getMessage()) -> "ERR - " + null
        sap.fallar("cuentasBroad", new IllegalStateException());
        ejecutar();

        assertThat(Archivos.lineasJob(log())).contains("ERR - null");
    }

    @Test
    @DisplayName("CONT-J-28: SFTP: removeRemoteCopy then uploadDirectory('/home/interfaz-bpc/', <base>) after every file is written")
    void contJ28_sftpDespuesDeGenerar() throws Exception {
        // CreaInterfazDiaria.java:202-205: connect, removeDir("/home/interfaz-bpc/", path), downloadDir(...) (= upload), disconnect
        AtomicReference<List<String>> alSubir = new AtomicReference<>();
        doAnswer(inv -> { alSubir.set(Archivos.arbol(Path.of(base))); return null; }).when(sftp).uploadDirectory(anyString(), any());

        ejecutar();

        InOrder o = inOrder(sftp);
        o.verify(sftp).removeRemoteCopy("/home/interfaz-bpc/", Path.of(base));
        o.verify(sftp).uploadDirectory("/home/interfaz-bpc/", Path.of(base));
        o.verifyNoMoreInteractions();
        assertThat(alSubir.get()).hasSize(FI.size() + BPC.size() + 1);
    }

    @Test
    @DisplayName("CONT-J-29: removeRemoteCopy fails -> upload skipped, 'ERR - <message>', still COMMIT")
    void contJ29_falloAlBorrarRemoto() throws Exception {
        // CreaInterfazDiaria.java:198-226: remove and upload share one try; catch (IOException/SftpException) logs ex.getMessage()
        doThrow(new SftpTransferException("sftp: conexion rechazada")).when(sftp).removeRemoteCopy(anyString(), any());

        JobResultado r = ejecutar();

        assertThat(r.desenlace()).isEqualTo(Desenlace.COMMIT);
        verify(sftp, never()).uploadDirectory(anyString(), any());
        assertThat(Archivos.lineasJob(log())).endsWith("ERR - sftp: conexion rechazada");
    }

    @Test
    @DisplayName("CONT-J-30: uploadDirectory fails with an IOException -> 'ERR - <message>', still COMMIT")
    void contJ30_falloAlSubir() throws Exception {
        doThrow(new SftpTransferException("sftp: disco remoto lleno")).when(sftp).uploadDirectory(anyString(), any());

        assertThat(ejecutar().desenlace()).isEqualTo(Desenlace.COMMIT);
        assertThat(Archivos.lineasJob(log())).endsWith("ERR - sftp: disco remoto lleno");
    }

    @Test
    @DisplayName("CONT-J-31: a RuntimeException from the transfer is caught by logica's outer catch: 'ERR - <message>', still COMMIT")
    void contJ31_runtimeEnSftp() throws Exception {
        // legacy: Integer.parseInt(PORT) NumberFormatException is not in the SFTP catch list -> outer catch :229-232
        doThrow(new SftpConfigurationException("SFTP_PORT is not a number")).when(sftp).removeRemoteCopy(anyString(), any());

        assertThat(ejecutar().desenlace()).isEqualTo(Desenlace.COMMIT);
        assertThat(Archivos.lineasJob(log())).endsWith("ERR - SFTP_PORT is not a number");
    }

    @Test
    @DisplayName("CONT-J-32: contador = first row of the query (rows 7, 3 -> 7) and is written in the movimiento file")
    void contJ32_contadorPrimeraFila() {
        // CreaInterfazDiaria.java:97-103 (backwards iteration keeping the last assignment = first row)
        when(contador.contadoresInterfazSiga(any())).thenReturn(List.of(7L, 3L));
        sap.conMovimientos(List.of(FakeInterfazSapSource.movimientoSintetico()));

        ejecutar();

        String mov = Archivos.leerLatin1(fi().resolve("B1_BROAD_MOV_CONT.TXT"));
        assertThat(mov.split("\n")[1]).startsWith("1AP" + "ENT0001" + "20260227" + "101500  " + "OP1       " + "P1        " + "7         ");
    }

    @Test
    @DisplayName("CONT-J-33: no contador rows -> contador 0")
    void contJ33_contadorSinFilas() {
        // CreaInterfazDiaria.java:73 Long contador = new Long(0)
        when(contador.contadoresInterfazSiga(any())).thenReturn(List.of());
        sap.conMovimientos(List.of(FakeInterfazSapSource.movimientoSintetico()));

        ejecutar();

        String mov = Archivos.leerLatin1(fi().resolve("B1_BROAD_MOV_CONT.TXT"));
        assertThat(mov.split("\n")[1]).contains("P1        " + "0         " + "1101001");
    }

    @Test
    @DisplayName("CONT-J-34: the contador query fails -> 'ERR - <message>' only: no SAP query, no file, no SFTP; still COMMIT")
    void contJ34_falloContador() {
        // CreaInterfazDiaria.java:95 inside logica's try; the outer catch :229-232 logs and returns normally
        when(contador.contadoresInterfazSiga(any())).thenThrow(new IllegalStateException("HANA: tabla SIGA no disponible"));

        JobResultado r = ejecutar();

        assertThat(r.desenlace()).isEqualTo(Desenlace.COMMIT);
        assertThat(sap.llamadas()).isEmpty();
        verifyNoInteractions(sftp);
        assertThat(Archivos.lineasJob(log())).containsExactly("ERR - HANA: tabla SIGA no disponible");
        assertThat(Archivos.arbol(Path.of(base))).containsExactly("logs/LOG_ServiceBROAD_20260228.log");
    }

    @Test
    @DisplayName("CONT-J-35: SFTP settings unavailable (legacy CreaInterfazSIGA.properties missing) -> ROLLBACK, nothing read or written, status(id,false,\"\")")
    void contJ35_sinConfiguracionSftp_rollback() {
        // CreaInterfazDiaria.java:258-269: FileInputStream("CreaInterfazSIGA.properties") fails -> return false before logica
        settings = () -> { throw new SftpConfigurationException("Missing required environment variable SFTP_HOST"); };

        JobResultado r = ejecutar();

        assertThat(r.desenlace()).isEqualTo(Desenlace.ROLLBACK);
        assertThat(db.estados().llamadas()).containsExactly(new Estado(300L, false, ""));
        verifyNoInteractions(plataforma, contador, sftp);
        assertThat(sap.llamadas()).isEmpty();
        assertThat(Path.of(base)).doesNotExist();
    }

    @Test
    @DisplayName("CONT-J-35b (approved deviation): SFTP_KNOWN_HOSTS missing makes the settings unavailable -> same ROLLBACK, nothing generated")
    void contJ35b_sinKnownHosts_rollback() {
        // Approved deviation: the SFTP host key is verified against known_hosts; legacy disabled the check.
        Map<String, String> env = Map.of("SFTP_HOST", "sftp.invalid", "SFTP_PORT", "22", "SFTP_USER", "fake-sftp-user",
                "SFTP_PASSWORD", "fake-sftp-pass-0000");
        settings = () -> SftpSettings.fromEnvironment(env);

        assertThat(ejecutar().desenlace()).isEqualTo(Desenlace.ROLLBACK);
        verifyNoInteractions(sftp);
        assertThat(sap.llamadas()).isEmpty();
    }

    @Test
    @DisplayName("CONT-J-36: the PLA.RutaBaseArchivos lookup fails -> ROLLBACK (legacy reads it outside logica's try)")
    void contJ36_falloRutaBase_rollback() {
        // CreaInterfazDiaria.java:74 (before the try at :76) -> exception -> transaction() catch -> false
        when(plataforma.obtenerParametroConfiguracion(eq(1L), eq("PLATAFORMA"), eq("PLA.RutaBaseArchivos"), any()))
                .thenThrow(new IllegalStateException("HANA caida"));

        assertThat(ejecutar().desenlace()).isEqualTo(Desenlace.ROLLBACK);
        assertThat(sap.llamadas()).isEmpty();
        verifyNoInteractions(sftp, contador);
    }

    @Test
    @DisplayName("CONT-J-36b (DEV-J-02): PLA.RutaBaseArchivos empty -> ROLLBACK, nothing written (legacy wrote under a literal 'null' prefix)")
    void contJ36b_rutaBaseVacia_rollback() {
        when(plataforma.obtenerParametroConfiguracion(eq(1L), eq("PLATAFORMA"), eq("PLA.RutaBaseArchivos"), any())).thenReturn(null);

        assertThat(ejecutar().desenlace()).isEqualTo(Desenlace.ROLLBACK);
        assertThat(sap.llamadas()).isEmpty();
        verifyNoInteractions(sftp);
        assertThat(Path.of("nulllogs")).doesNotExist();
        assertThat(Path.of("nullTransmisiones")).doesNotExist();
    }

    @Test
    @DisplayName("CONT-J-37: registered under the legacy class name cl.bcs.contabilidad.jobs.CreaInterfazDiaria")
    void contJ37_clase() {
        assertThat(new CreaInterfazDiaria(plataforma, contador, sap, sftp, settings, RELOJ).clase()).isEqualTo(CLASE);
    }
}
