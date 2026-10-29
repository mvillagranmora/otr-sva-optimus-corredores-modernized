package cl.bcs.contabilidad.interfaz;

import cl.bcs.contabilidad.domain.port.out.InterfazContadorSource;
import cl.bcs.contabilidad.domain.port.out.InterfazFileTransfer;
import cl.bcs.contabilidad.domain.port.out.PlataformaPort;
import cl.bcs.contabilidad.infrastructure.batch.CreaInterfazDiariaJob;
import cl.bcs.contabilidad.infrastructure.interfaz.BroadCuentasGenerator;
import cl.bcs.contabilidad.infrastructure.interfaz.BroadMovimientoGenerator;
import cl.bcs.contabilidad.infrastructure.interfaz.BroadSaldoGenerator;
import cl.bcs.contabilidad.infrastructure.interfaz.CuentasBpcGenerator;
import cl.bcs.contabilidad.infrastructure.interfaz.DescriptivasBroadGenerator;
import cl.bcs.contabilidad.infrastructure.interfaz.InterfazContext;
import cl.bcs.contabilidad.infrastructure.interfaz.InterfazGenerator;
import cl.bcs.contabilidad.infrastructure.interfaz.InterfazGenerators;
import cl.bcs.contabilidad.infrastructure.interfaz.MovimientoBpcGenerator;
import cl.bcs.contabilidad.interfaz.InterfazFixtures.Consulta;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static cl.bcs.contabilidad.interfaz.InterfazFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Characterization of legacy {@code CreaInterfazDiaria.logica()} orchestration.
 * Oracle: legacy/.../BcsContabilidadJobs/src/cl/bcs/contabilidad/jobs/CreaInterfazDiaria.java
 *
 * <p>Replaces the former tautological {@code cl.bcs.contabilidad.CreaInterfazDiariaJobTest}: every test here
 * executes {@link CreaInterfazDiariaJob} and observes what it does through fakes and the file system.
 */
class CreaInterfazDiariaJobTest {

    /** Legacy FI files in generation order (CreaInterfazDiaria.java:120-162). */
    static final List<String> FI = List.of(
            "B1_BROAD_CTAS.TXT", "B1_CTRL_BROAD_CTAS.TXT",
            "B1_BROAD_MOV_CONT.TXT", "B1_CTRL_BROAD_MOV_CONT.TXT",
            "B1_BROAD_SALD_CONT.TXT", "B1_CTRL_BROAD_SALD_CONT.TXT",
            "B1_DES_ENTIDAD.TXT", "B1_DES_TIPO_CTA.TXT", "B1_DES_CC_PROD.TXT");

    /** Legacy BPC files (CreaInterfazDiaria.java:173-186). */
    static final List<String> BPC = List.of(
            "B1_CUENTAS_BPC.TXT", "B1_CTRL_CUENTAS_BPC.TXT",
            "B1_BROAD_BPC.TXT", "B1_CTRL_BROAD_BPC.TXT");

    /** Files each SAP query feeds (used by the isolation matrix). */
    static final Map<Consulta, Set<String>> ARCHIVOS_POR_CONSULTA = Map.of(
            Consulta.CUENTAS_BROAD, Set.of("B1_BROAD_CTAS.TXT", "B1_CTRL_BROAD_CTAS.TXT"),
            Consulta.MOVIMIENTOS_BROAD, Set.of("B1_BROAD_MOV_CONT.TXT", "B1_CTRL_BROAD_MOV_CONT.TXT"),
            Consulta.SALDOS_BROAD, Set.of("B1_BROAD_SALD_CONT.TXT", "B1_CTRL_BROAD_SALD_CONT.TXT"),
            Consulta.ENTIDADES, Set.of("B1_DES_ENTIDAD.TXT"),
            Consulta.TIPOS_CUENTA, Set.of("B1_DES_TIPO_CTA.TXT"),
            Consulta.CENTROS_COSTO, Set.of("B1_DES_CC_PROD.TXT"),
            Consulta.CUENTAS_BPC, Set.of("B1_CUENTAS_BPC.TXT", "B1_CTRL_CUENTAS_BPC.TXT"),
            Consulta.MOVIMIENTOS_BPC, Set.of("B1_BROAD_BPC.TXT", "B1_CTRL_BROAD_BPC.TXT"));

    @TempDir
    Path tmp;

    // ------------------------------------------------------------------ fakes

    /** Shared, ordered event log across generators and transfer. */
    final List<String> eventos = Collections.synchronizedList(new ArrayList<>());

    /** Generator that records the context it received. */
    final class RecordingGenerator implements InterfazGenerator {
        final String nombre;
        final RuntimeException falla;
        InterfazContext recibido;

        RecordingGenerator(String nombre, RuntimeException falla) {
            this.nombre = nombre;
            this.falla = falla;
        }

        @Override
        public void generar(InterfazContext contexto) {
            recibido = contexto;
            eventos.add("GEN:" + nombre);
            if (falla != null) {
                throw falla;
            }
        }
    }

    /** Fake SFTP port: records calls and snapshots the local tree at removal time. */
    final class FakeTransfer implements InterfazFileTransfer {
        RuntimeException fallaRemove;
        IOException fallaUpload;
        final List<String> archivosAlRemover = new ArrayList<>();

        @Override
        public void removeRemoteCopy(String remoteBaseDirectory, Path localDirectory) {
            eventos.add("REMOVE:" + remoteBaseDirectory + "|" + localDirectory);
            try (Stream<Path> s = Files.exists(localDirectory) ? Files.walk(localDirectory) : Stream.empty()) {
                s.filter(Files::isRegularFile).forEach(p -> archivosAlRemover.add(p.getFileName().toString()));
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
            if (fallaRemove != null) {
                throw fallaRemove;
            }
        }

        @Override
        public void uploadDirectory(String remoteBaseDirectory, Path localDirectory) throws IOException {
            eventos.add("UPLOAD:" + remoteBaseDirectory + "|" + localDirectory);
            if (fallaUpload != null) {
                throw fallaUpload;
            }
        }
    }

    /** Fake contador port (ConsultaContadorInterfazSIGA.sql). */
    final class FakeContador implements InterfazContadorSource {
        List<Long> filas = List.of(CONTADOR);
        RuntimeException falla;
        final List<LocalDate> fechas = new ArrayList<>();

        @Override
        public List<Long> contadoresInterfazSiga(LocalDate fechaEjecucion) {
            fechas.add(fechaEjecucion);
            eventos.add("CONTADOR:" + fechaEjecucion);
            if (falla != null) {
                throw falla;
            }
            return filas;
        }
    }

    final FakeContador contador = new FakeContador();
    final FakeTransfer transfer = new FakeTransfer();

    /** Base path exactly as PLA.RutaBaseArchivos would hold it (with trailing separator). */
    String baseConfigurada() {
        return tmp.toString() + File.separator;
    }

    PlataformaPort plataforma(String rutaBase) {
        PlataformaPort p = mock(PlataformaPort.class);
        when(p.obtenerParametroConfiguracion(eq(1L), eq("PLATAFORMA"), eq("PLA.RutaBaseArchivos"), any()))
                .thenReturn(rutaBase);
        return p;
    }

    CreaInterfazDiariaJob job(List<InterfazGenerator> generadores, Clock clock) {
        return new CreaInterfazDiariaJob(plataforma(baseConfigurada()), contador, generadores, transfer, clock);
    }

    CreaInterfazDiariaJob jobReal(FakeSapSource sap) {
        return job(InterfazGenerators.enOrdenLegacy(sap, CLOCK), CLOCK);
    }

    Path fi() {
        return tmp.resolve("Transmisiones/Salida/FI");
    }

    Path bpcDir() {
        return tmp.resolve("Transmisiones/Salida/BPC");
    }

    static List<String> listar(Path dir) throws IOException {
        if (!Files.isDirectory(dir)) {
            return List.of();
        }
        try (Stream<Path> s = Files.list(dir)) {
            return s.map(p -> p.getFileName().toString()).sorted().toList();
        }
    }

    // ------------------------------------------------------------------ CONT-A-01 fechaEjecucion

    @ParameterizedTest(name = "now={0} zone={1} -> fechaEjecucion={2}")
    @CsvSource({
            "2026-10-13T18:04:05Z, America/Santiago, 2026-10-12",
            "2026-03-01T15:00:00Z, America/Santiago, 2026-02-28",
            "2024-03-01T15:00:00Z, America/Santiago, 2024-02-29",
            "2026-01-01T15:00:00Z, America/Santiago, 2025-12-31",
            // 23:30 local on 2026-10-12 -> local date governs, not UTC date
            "2026-10-13T02:30:00Z, America/Santiago, 2026-10-11",
            "2026-10-13T02:30:00Z, UTC,              2026-10-12"
    })
    @DisplayName("CONT-A-01: fechaEjecucion = current local date minus exactly 1 calendar day")
    void cont_a_01_fechaEjecucionIsTodayMinusOneCalendarDay(String now, String zone, String esperada) {
        // CreaInterfazDiaria.java:79-90 new Date() -> Calendar.add(DAY_OF_YEAR, -1) in JVM default zone
        RecordingGenerator g = new RecordingGenerator("g", null);
        job(List.of(g), Clock.fixed(Instant.parse(now), ZoneId.of(zone))).ejecutar();
        assertThat(g.recibido).isNotNull();
        assertThat(g.recibido.fechaEjecucion()).isEqualTo(LocalDate.parse(esperada));
        assertThat(contador.fechas).containsExactly(LocalDate.parse(esperada));
    }

    @Test
    @DisplayName("CONT-A-01: explicit fecha overload also subtracts one calendar day (Monday -> Sunday)")
    void cont_a_01_explicitFechaSubtractsOneCalendarDay() {
        // CreaInterfazDiaria.java:88 calendar day, NOT business day: 2026-10-12 (Mon) -> 2026-10-11 (Sun)
        RecordingGenerator g = new RecordingGenerator("g", null);
        job(List.of(g), CLOCK).ejecutar(LocalDate.of(2026, 10, 12));
        assertThat(g.recibido.fechaEjecucion()).isEqualTo(LocalDate.of(2026, 10, 11));
    }

    // ------------------------------------------------------------------ CONT-A-02 / 03 contador

    @Test
    @DisplayName("CONT-A-02: fechaEjecucion reaches the contador, movimiento, saldo and movimiento-BPC queries only")
    void cont_a_02_fechaEjecucionReachesOnlyDatedQueries() {
        // CreaInterfazDiaria.java:94-95,123-188; dated SQL: ConsultaContadorInterfazSIGA, ConsultaBroadMovimiento,
        // ConsultaBroadSaldo, ConsultaMovimientoBPC (:FechaEjecucion); the rest take no parameter
        FakeSapSource sap = new FakeSapSource();
        jobReal(sap).ejecutar();
        assertThat(contador.fechas).containsExactly(FECHA_EJECUCION);
        assertThat(sap.llamadas).containsExactly(
                "CUENTAS_BROAD",
                "MOVIMIENTOS_BROAD@2026-10-12",
                "SALDOS_BROAD@2026-10-12",
                "ENTIDADES", "TIPOS_CUENTA", "CENTROS_COSTO",
                "CUENTAS_BPC",
                "MOVIMIENTOS_BPC@2026-10-12");
    }

    @Test
    @DisplayName("CONT-A-02: fechaEjecucion never appears in file names (names are fixed)")
    void cont_a_02_fileNamesDoNotCarryTheDate() throws Exception {
        // CreaInterfazDiaria.java:120-186 literal names
        jobReal(new FakeSapSource()).ejecutar();
        assertThat(listar(fi())).noneMatch(n -> n.contains("2026") || n.contains("20261012"));
        assertThat(listar(bpcDir())).noneMatch(n -> n.contains("2026"));
    }

    @ParameterizedTest(name = "contador rows {0} -> {1}")
    @CsvSource({
            "'7',     7",
            "'7;9',   7",
            "'',      0"
    })
    @DisplayName("CONT-A-03: contador = first row of ConsultaContadorInterfazSIGA, 0 when it returns no rows")
    void cont_a_03_contadorIsFirstRowOrZero(String filas, long esperado) {
        // CreaInterfazDiaria.java:73 (default 0) and 97-103 (reverse iteration -> last assignment is index 0)
        contador.filas = filas.isEmpty() ? List.of()
                : Stream.of(filas.split(";")).map(Long::valueOf).toList();
        RecordingGenerator g = new RecordingGenerator("g", null);
        job(List.of(g), CLOCK).ejecutar();
        assertThat(g.recibido.contador()).isEqualTo(esperado);
    }

    @Test
    @DisplayName("CONT-A-04: the contador is written in NUM-REGISTRO of MOV_CONT and SALD_CONT in a full run")
    void cont_a_04_contadorFlowsIntoFiles() throws Exception {
        // CreaInterfazDiaria.java:135,148 pass contador; CreaBroadMovimiento.java:153; CreaBroadSaldo.java:134
        contador.filas = List.of(58L);
        jobReal(new FakeSapSource()).ejecutar();
        assertThat(lineas(fi().resolve("B1_BROAD_MOV_CONT.TXT")).get(1).substring(46, 56)).isEqualTo("58        ");
        assertThat(lineas(fi().resolve("B1_BROAD_SALD_CONT.TXT")).get(1).substring(18, 28)).isEqualTo("58        ");
    }

    // ------------------------------------------------------------------ CONT-A-05 paths

    @Test
    @DisplayName("CONT-A-05: outputs go to <PLA.RutaBaseArchivos>Transmisiones/Salida/FI/ and .../BPC/ (created)")
    void cont_a_05_outputDirectoriesUnderConfiguredBasePath() throws Exception {
        // CreaInterfazDiaria.java:74 (PLATAFORMA / PLA.RutaBaseArchivos, empresa 1), 115-116
        RecordingGenerator g = new RecordingGenerator("g", null);
        job(List.of(g), CLOCK).ejecutar();
        assertThat(g.recibido.directorioFi()).isEqualTo(Path.of(baseConfigurada() + "Transmisiones/Salida/FI/"));
        assertThat(g.recibido.directorioBpc()).isEqualTo(Path.of(baseConfigurada() + "Transmisiones/Salida/BPC/"));

        jobReal(new FakeSapSource()).ejecutar();
        assertThat(fi()).isDirectory();
        assertThat(bpcDir()).isDirectory();
    }

    @Test
    @DisplayName("CONT-A-05: base path is joined by plain string concatenation (no separator is inserted)")
    void cont_a_05_basePathWithoutTrailingSeparatorIsConcatenatedVerbatim() {
        // CreaInterfazDiaria.java:115 path + "Transmisiones/Salida/FI/" — a missing trailing '/' yields a sibling dir
        String sinSlash = tmp.resolve("archivos").toString();
        RecordingGenerator g = new RecordingGenerator("g", null);
        new CreaInterfazDiariaJob(plataforma(sinSlash), contador, List.of(g), transfer, CLOCK).ejecutar();
        assertThat(g.recibido.directorioFi()).isEqualTo(Path.of(sinSlash + "Transmisiones/Salida/FI/"));
        assertThat(g.recibido.directorioFi().toString()).contains("archivosTransmisiones");
    }

    // ------------------------------------------------------------------ CONT-A-06 full run

    @Test
    @DisplayName("CONT-A-06: legacy generator order is Cuentas, Movimiento, Saldo, Descriptivas, CuentasBPC, MovimientoBPC")
    void cont_a_06_generatorsInLegacyOrder() {
        // CreaInterfazDiaria.java:118-193
        List<InterfazGenerator> gs = InterfazGenerators.enOrdenLegacy(new FakeSapSource(), CLOCK);
        assertThat(gs).extracting(Object::getClass).containsExactly(
                BroadCuentasGenerator.class, BroadMovimientoGenerator.class, BroadSaldoGenerator.class,
                DescriptivasBroadGenerator.class, CuentasBpcGenerator.class, MovimientoBpcGenerator.class);
    }

    @Test
    @DisplayName("CONT-A-06: a full run writes exactly the 9 FI + 4 BPC files, each byte-identical to its legacy golden")
    void cont_a_06_fullRunMatchesAllThirteenGoldens() throws Exception {
        // CreaInterfazDiaria.java:115-193; equivalence cases executed: 13 files
        jobReal(new FakeSapSource()).ejecutar();
        assertThat(listar(fi())).containsExactlyInAnyOrderElementsOf(FI);
        assertThat(listar(bpcDir())).containsExactlyInAnyOrderElementsOf(BPC);
        for (String f : FI) {
            assertThat(leer(fi().resolve(f))).as(f).isEqualTo(golden("FI/" + f));
        }
        for (String f : BPC) {
            assertThat(leer(bpcDir().resolve(f))).as(f).isEqualTo(golden("BPC/" + f));
        }
    }

    @Test
    @DisplayName("CONT-A-07: generators run in list order and each receives the same context")
    void cont_a_07_generatorsInvokedInOrderWithSameContext() {
        // CreaInterfazDiaria.java:118-193 sequential try blocks
        RecordingGenerator a = new RecordingGenerator("a", null);
        RecordingGenerator b = new RecordingGenerator("b", null);
        job(List.of(a, b), CLOCK).ejecutar();
        assertThat(eventos).startsWith("CONTADOR:2026-10-12", "GEN:a", "GEN:b");
        assertThat(a.recibido).isEqualTo(b.recibido);
    }

    // ------------------------------------------------------------------ CONT-A-07 isolation

    @Test
    @DisplayName("CONT-A-07: a throwing generator does not stop the following ones nor the SFTP step")
    void cont_a_07_failingGeneratorIsIsolated() {
        // CreaInterfazDiaria.java:125-128 (and every other step): catch (Exception) -> log, continue
        RecordingGenerator a = new RecordingGenerator("a", new IllegalStateException("boom"));
        RecordingGenerator b = new RecordingGenerator("b", null);
        RecordingGenerator c = new RecordingGenerator("c", new RuntimeException("boom2"));
        assertThatCode(() -> job(List.of(a, b, c), CLOCK).ejecutar()).doesNotThrowAnyException();
        assertThat(eventos).containsSubsequence("GEN:a", "GEN:b", "GEN:c", "REMOVE:/home/interfaz-bpc/|" + Path.of(baseConfigurada()));
    }

    @ParameterizedTest(name = "SAP query {0} fails")
    @EnumSource(Consulta.class)
    @DisplayName("CONT-A-07: when one SAP query fails only its own files are missing; all others match goldens")
    void cont_a_07_isolationMatrix(Consulta falla) throws Exception {
        // CreaInterfazDiaria.java:118-193 per-step try/catch; CreaDescriptivasBroad.java:171,223,275 per-file catch
        FakeSapSource sap = new FakeSapSource();
        sap.fallar.add(falla);
        jobReal(sap).ejecutar();
        Set<String> ausentes = ARCHIVOS_POR_CONSULTA.get(falla);
        for (String f : FI) {
            if (ausentes.contains(f)) {
                assertThat(fi().resolve(f)).as(f).doesNotExist();
            } else {
                assertThat(leer(fi().resolve(f))).as(f).isEqualTo(golden("FI/" + f));
            }
        }
        for (String f : BPC) {
            if (ausentes.contains(f)) {
                assertThat(bpcDir().resolve(f)).as(f).doesNotExist();
            } else {
                assertThat(leer(bpcDir().resolve(f))).as(f).isEqualTo(golden("BPC/" + f));
            }
        }
        assertThat(eventos).anyMatch(e -> e.startsWith("UPLOAD:"));
    }

    // ------------------------------------------------------------------ CONT-A-08 / 09 fatal paths

    @Test
    @DisplayName("CONT-A-08: contador query failure aborts the run silently: no generator, no SFTP, no exception")
    void cont_a_08_contadorFailureAbortsEverything() throws Exception {
        // CreaInterfazDiaria.java:95 inside the outer try; caught at 229-232 (log only)
        contador.falla = new IllegalStateException("HANA no disponible (fake)");
        FakeSapSource sap = new FakeSapSource();
        assertThatCode(() -> jobReal(sap).ejecutar()).doesNotThrowAnyException();
        assertThat(sap.llamadas).isEmpty();
        assertThat(eventos).containsExactly("CONTADOR:2026-10-12");
        assertThat(listar(fi())).isEmpty();
        assertThat(listar(bpcDir())).isEmpty();
    }

    @Test
    @DisplayName("CONT-A-09: failure reading PLA.RutaBaseArchivos propagates (it is outside the legacy try)")
    void cont_a_09_basePathFailurePropagates() {
        // CreaInterfazDiaria.java:74 precedes the try at 76 -> logica() throws -> transaction() returns false
        PlataformaPort p = mock(PlataformaPort.class);
        when(p.obtenerParametroConfiguracion(anyLong(), anyString(), anyString(), any()))
                .thenThrow(new IllegalStateException("config no disponible (fake)"));
        RecordingGenerator g = new RecordingGenerator("g", null);
        CreaInterfazDiariaJob job = new CreaInterfazDiariaJob(p, contador, List.of(g), transfer, CLOCK);
        assertThatThrownBy(job::ejecutar).isInstanceOf(IllegalStateException.class);
        assertThat(eventos).isEmpty();
    }

    // ------------------------------------------------------------------ CONT-A-30 / 31 SFTP

    @Test
    @DisplayName("CONT-A-30: after all generators: removeRemoteCopy then uploadDirectory of the whole base path to /home/interfaz-bpc/")
    void cont_a_30_sftpRemoveThenUploadAfterGeneration() {
        // CreaInterfazDiaria.java:198-205 removeDir("/home/interfaz-bpc/", path); downloadDir("/home/interfaz-bpc/", path)
        // SFTPConnector.java:136-195: despite its name, downloadDir PUTs local files to the server (upload).
        jobReal(new FakeSapSource()).ejecutar();
        Path local = Path.of(baseConfigurada());
        List<String> sftp = eventos.stream().filter(e -> e.startsWith("REMOVE:") || e.startsWith("UPLOAD:")).toList();
        assertThat(sftp).containsExactly(
                "REMOVE:/home/interfaz-bpc/|" + local,
                "UPLOAD:/home/interfaz-bpc/|" + local);
        assertThat(transfer.archivosAlRemover).containsExactlyInAnyOrderElementsOf(
                Stream.concat(FI.stream(), BPC.stream()).toList());
        assertThat(InterfazFileTransfer.REMOTE_BASE_DIRECTORY).isEqualTo("/home/interfaz-bpc/");
    }

    @Test
    @DisplayName("CONT-A-30: SFTP step still runs when every generator failed")
    void cont_a_30_sftpRunsEvenIfAllGeneratorsFail() {
        // CreaInterfazDiaria.java:198 is reached regardless of step outcomes
        RecordingGenerator a = new RecordingGenerator("a", new IllegalStateException("x"));
        job(List.of(a), CLOCK).ejecutar();
        assertThat(eventos).containsSubsequence("GEN:a", "REMOVE:/home/interfaz-bpc/|" + Path.of(baseConfigurada()),
                "UPLOAD:/home/interfaz-bpc/|" + Path.of(baseConfigurada()));
    }

    @Test
    @DisplayName("CONT-A-31: a failing remote removal skips the upload and is swallowed")
    void cont_a_31_removeFailureSkipsUploadAndIsSwallowed() {
        // CreaInterfazDiaria.java:198-226 remove and upload share one try; exceptions only logged
        transfer.fallaRemove = new IllegalStateException("sftp caido (fake)");
        assertThatCode(() -> job(List.of(new RecordingGenerator("g", null)), CLOCK).ejecutar())
                .doesNotThrowAnyException();
        assertThat(eventos).anyMatch(e -> e.startsWith("REMOVE:")).noneMatch(e -> e.startsWith("UPLOAD:"));
    }

    @Test
    @DisplayName("CONT-A-31: a failing upload is swallowed")
    void cont_a_31_uploadFailureIsSwallowed() {
        // CreaInterfazDiaria.java:216-220 catch (IOException) -> log
        transfer.fallaUpload = new IOException("sftp caido (fake)");
        assertThatCode(() -> job(List.of(new RecordingGenerator("g", null)), CLOCK).ejecutar())
                .doesNotThrowAnyException();
        assertThat(eventos).anyMatch(e -> e.startsWith("UPLOAD:"));
    }
}
