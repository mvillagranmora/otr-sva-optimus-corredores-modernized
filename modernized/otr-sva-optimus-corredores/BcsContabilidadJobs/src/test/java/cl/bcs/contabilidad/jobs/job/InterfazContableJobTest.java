package cl.bcs.contabilidad.jobs.job;

import cl.bcs.contabilidad.domain.model.sap.HomologaRespuestaSap;
import cl.bcs.contabilidad.domain.model.sap.ResultadoInterfazContable;
import cl.bcs.contabilidad.domain.port.out.InterfazContableRepository;
import cl.bcs.contabilidad.domain.port.out.PlataformaPort;
import cl.bcs.contabilidad.jobs.runtime.JobResultado;
import cl.bcs.contabilidad.jobs.runtime.JobResultado.Desenlace;
import cl.bcs.contabilidad.jobs.support.Archivos;
import cl.bcs.contabilidad.jobs.support.H2Jobs;
import cl.bcs.contabilidad.jobs.support.H2Jobs.Estados.Estado;
import cl.bcs.contabilidad.service.InterfazContableService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Job shell of legacy {@code BcsContabilidadJobs/src/cl/bcs/contabilidad/jobs/InterfazContable.java}:
 * {@code transaction()} (:46-54) and the parts of {@code logica()} (:80-186) that BcsContabilidadServicios'
 * {@code InterfazContableService.procesar} does not own: the params, the log file under PLA.RutaBaseArchivos, the
 * per-pre-voucher commit (:244), the RutaSalida update (:178) and the transaction outcome.
 *
 * <p>The service is mocked here through the job's factory {@code Function<InterfazContableRepository,
 * InterfazContableService>}; the end-to-end version against the recorded legacy goldens is
 * {@link InterfazContableJobGoldenTest}. Clock: 2025-04-01T13:15:30Z = 10:15:30 in America/Santiago.
 */
class InterfazContableJobTest {

    static final String CLASE = "cl.bcs.contabilidad.jobs.InterfazContable";
    static final Clock RELOJ = Clock.fixed(Instant.parse("2025-04-01T13:15:30Z"), ZoneId.of("America/Santiago"));
    static final String ARCHIVO = "ITZ/20250401101530_InterfazContable.log";

    @TempDir
    Path tmp;

    private String base;
    private final H2Jobs db = H2Jobs.nueva();
    private final PlataformaPort plataforma = mock(PlataformaPort.class);
    private final InterfazContableService servicio = mock(InterfazContableService.class);
    private final InterfazContableRepository repositorio = mock(InterfazContableRepository.class);
    private final AtomicReference<InterfazContableRepository> repoEntregado = new AtomicReference<>();

    @BeforeEach
    void preparar() {
        base = tmp.resolve("base").toString() + File.separator;
        when(plataforma.obtenerParametroConfiguracion(anyLong(), eq("PLATAFORMA"), eq("PLA.RutaBaseArchivos"), any())).thenReturn(base);
        db.programa(77L, "ANTERIOR").programa(78L, "OTRO");
    }

    private InterfazContable job() {
        return new InterfazContable(plataforma, repo -> { repoEntregado.set(repo); return servicio; }, repositorio, RELOJ);
    }

    private JobResultado ejecutar(Map<String, String> params) {
        return db.runner(job()).ejecutar(CLASE, 77L, params);
    }

    static String b64(String s) {
        return Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
    }

    static Map<String, String> params(String secuencias) {
        Map<String, String> p = new HashMap<>();
        p.put("IdEmpresa", "1");
        p.put("IdUsuario", "14");
        p.put("SecCmbte", b64(secuencias));
        p.put("Destino", "B");
        return p;
    }

    private void devolver(List<Long> ok, List<Long> err, String... lineas) {
        when(servicio.procesar(anyLong(), anyLong(), anyString(), anyList()))
                .thenReturn(new ResultadoInterfazContable(ok, err, List.of(lineas)));
    }

    @Test
    @DisplayName("CONT-J-60: IdEmpresa, IdUsuario, Destino and the SecCmbte ids reach InterfazContableService.procesar(1, 14, \"B\", [100, 200])")
    void contJ60_parametrosAlServicio() {
        // InterfazContable.java:48-53, :89-90, :122 (destino forwarded per pre-voucher)
        devolver(List.of(100L, 200L), List.of(), "PRECOMPROBANTE ENVIADO EXISTOSAMENTE: 100", "PRECOMPROBANTE ENVIADO EXISTOSAMENTE: 200");

        JobResultado r = ejecutar(params("100;200"));

        assertThat(r.desenlace()).isEqualTo(Desenlace.COMMIT);
        verify(servicio).procesar(1L, 14L, "B", List.of(100L, 200L));
    }

    @ParameterizedTest(name = "CONT-J-61: SecCmbte = base64(\"{0}\") -> ids {1}")
    @CsvSource(delimiter = '|', value = {
            "35981|35981",
            "100;200|100,200",
            "100;200;|100,200",      // String.split drops the trailing empty token
            "200;100;200|200,100,200"})
    void contJ61_secCmbteBase64(String texto, String ids) {
        // InterfazContable.java:89 new String(Base64.getDecoder().decode(comprobante), "UTF-8"); :90 split(";") + Long.parseLong
        devolver(List.of(), List.of(), "x");

        assertThat(ejecutar(params(texto)).desenlace()).isEqualTo(Desenlace.COMMIT);

        List<Long> esperado = new ArrayList<>();
        for (String s : ids.split(",")) esperado.add(Long.parseLong(s));
        verify(servicio).procesar(1L, 14L, "B", esperado);
    }

    @ParameterizedTest(name = "CONT-J-62: SecCmbte \"{0}\" cannot be decoded -> ERROR, service never called, RutaSalida untouched")
    @ValueSource(strings = {"%%%", "MTAwOzIwMA=x"})
    void contJ62_base64Invalido(String secCmbte) {
        // InterfazContable.java:89 IllegalArgumentException -> catch :179-181 rethrow -> BcsJob rollback + status error
        Map<String, String> p = params("100");
        p.put("SecCmbte", secCmbte);

        JobResultado r = ejecutar(p);

        assertThat(r.desenlace()).isEqualTo(Desenlace.ERROR);
        assertThat(r.error()).isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(servicio);
        assertThat(db.rutaSalida(77L)).isEqualTo("ANTERIOR");
        assertThat(Path.of(base + "ITZ")).doesNotExist();
    }

    @ParameterizedTest(name = "CONT-J-63: SecCmbte = base64(\"{0}\") -> NumberFormatException, ERROR, service never called")
    @ValueSource(strings = {"", "100;abc", " 100", "100;;200", "100,200"})
    void contJ63_idsInvalidos(String texto) {
        // InterfazContable.java:90 Long.parseLong on each token, no trim
        JobResultado r = ejecutar(params(texto));

        assertThat(r.desenlace()).isEqualTo(Desenlace.ERROR);
        assertThat(r.error()).isInstanceOf(NumberFormatException.class);
        verifyNoInteractions(servicio);
    }

    @ParameterizedTest(name = "CONT-J-64: {0} = {1} -> NumberFormatException, ERROR, nothing read or sent")
    @CsvSource(delimiter = '|', nullValues = "NULL", value = {"IdEmpresa|NULL", "IdEmpresa|uno", "IdUsuario|NULL", "IdUsuario|1.5"})
    void contJ64_numericosObligatorios(String param, String valor) {
        // InterfazContable.java:48-49 Long.parseLong(getParam(..))
        Map<String, String> p = params("100");
        if (valor == null) p.remove(param); else p.put(param, valor);

        JobResultado r = ejecutar(p);

        assertThat(r.desenlace()).isEqualTo(Desenlace.ERROR);
        assertThat(r.error()).isInstanceOf(NumberFormatException.class);
        verifyNoInteractions(servicio, plataforma);
        assertThat(db.estados().llamadas()).singleElement().extracting(Estado::error).isEqualTo(true);
    }

    @ParameterizedTest(name = "CONT-J-65: missing {0} -> ERROR, service never called")
    @ValueSource(strings = {"SecCmbte", "Destino"})
    void contJ65_textosObligatorios(String param) {
        // InterfazContable.java:51 getParam("Destino").toString() (NPE); :89 decode(null) (NPE)
        Map<String, String> p = params("100");
        p.remove(param);

        JobResultado r = ejecutar(p);

        assertThat(r.desenlace()).isEqualTo(Desenlace.ERROR);
        verifyNoInteractions(servicio);
        assertThat(db.rutaSalida(77L)).isEqualTo("ANTERIOR");
    }

    @Test
    @DisplayName("CONT-J-66: Destino is forwarded as given (\"S\", \"\")")
    void contJ66_destinoTalCual() {
        devolver(List.of(), List.of(), "x");
        Map<String, String> p = params("100");
        p.put("Destino", "S");
        ejecutar(p);
        verify(servicio).procesar(1L, 14L, "S", List.of(100L));

        p.put("Destino", "");
        ejecutar(p);
        verify(servicio).procesar(1L, 14L, "", List.of(100L));
    }

    @Test
    @DisplayName("CONT-J-67: log file = <PLA.RutaBaseArchivos>ITZ/<yyyyMMddHHmmss local>_InterfazContable.log, one service line per '\\n', UTF-8")
    void contJ67_archivoLog() {
        // InterfazContable.java:84-86 (name, base + archivo), :108, :123, :160, :164 (UTF-8 appends), :169 ("\n" after each id)
        devolver(List.of(100L), List.of(200L),
                "PRECOMPROBANTE ENVIADO EXISTOSAMENTE: 100",
                "ERROR AL ENVIAR PRECOMPROBANTE A SAP: 200, Mensaje: Cuenta contable año 2025 cerrada");

        ejecutar(params("100;200"));

        assertThat(Archivos.arbol(Path.of(base))).containsExactly(ARCHIVO);
        assertThat(Archivos.leer(Path.of(base + ARCHIVO))).isEqualTo(
                "PRECOMPROBANTE ENVIADO EXISTOSAMENTE: 100\n"
                        + "ERROR AL ENVIAR PRECOMPROBANTE A SAP: 200, Mensaje: Cuenta contable año 2025 cerrada\n");
    }

    @Test
    @DisplayName("CONT-J-68: RutaSalida of the job's IdPrograma = the RELATIVE name 'ITZ/<ts>_InterfazContable.log'; other rows untouched; COMMIT")
    void contJ68_rutaSalidaRelativa() {
        // InterfazContable.java:178 updateRutaSalida(conn, archivo, this.getJobId()) — archivo, not rutaArchivo
        devolver(List.of(100L), List.of(), "PRECOMPROBANTE ENVIADO EXISTOSAMENTE: 100");

        JobResultado r = ejecutar(params("100"));

        assertThat(r.desenlace()).isEqualTo(Desenlace.COMMIT);
        assertThat(db.rutaSalida(77L)).isEqualTo(ARCHIVO);
        assertThat(db.rutaSalida(78L)).isEqualTo("OTRO");
        assertThat(db.estados().llamadas()).containsExactly(new Estado(77L, false, ""));
    }

    @Test
    @DisplayName("CONT-J-69: an existing log file of the same second is appended to")
    void contJ69_logSeAnexa() {
        // FileUtils.writeStringToFile(file, ..., true)
        Archivos.escribir(Path.of(base + ARCHIVO), "PRECOMPROBANTE ENVIADO EXISTOSAMENTE: 1\n");
        devolver(List.of(2L), List.of(), "PRECOMPROBANTE ENVIADO EXISTOSAMENTE: 2");

        ejecutar(params("2"));

        assertThat(Archivos.leer(Path.of(base + ARCHIVO)))
                .isEqualTo("PRECOMPROBANTE ENVIADO EXISTOSAMENTE: 1\nPRECOMPROBANTE ENVIADO EXISTOSAMENTE: 2\n");
    }

    @Test
    @DisplayName("CONT-J-70: PLA.RutaBaseArchivos is read for the IdEmpresa param (3); a failing lookup -> ERROR, service never called")
    void contJ70_rutaBasePorEmpresa() {
        // InterfazContable.java:85 Utils.getConfiguracion(conn, schema, idEmpresa, "PLA.RutaBaseArchivos") — before the decode (:89)
        devolver(List.of(), List.of(), "x");
        Map<String, String> p = params("100");
        p.put("IdEmpresa", "3");
        ejecutar(p);
        verify(plataforma).obtenerParametroConfiguracion(eq(3L), eq("PLATAFORMA"), eq("PLA.RutaBaseArchivos"), any());

        PlataformaPort caida = mock(PlataformaPort.class);
        when(caida.obtenerParametroConfiguracion(anyLong(), anyString(), anyString(), any())).thenThrow(new IllegalStateException("ObtenerConfiguracion fallo"));
        InterfazContableService otro = mock(InterfazContableService.class);
        JobResultado r = db.runner(new InterfazContable(caida, repo -> otro, repositorio, RELOJ)).ejecutar(CLASE, 77L, params("100"));
        assertThat(r.desenlace()).isEqualTo(Desenlace.ERROR);
        assertThat(r.mensaje()).isEqualTo("ObtenerConfiguracion fallo");
        verifyNoInteractions(otro);
    }

    @Test
    @DisplayName("CONT-J-71: PLA.RutaBaseArchivos not configured (null) -> ERROR, service never called (legacy JsonNull.getAsString threw)")
    void contJ71_rutaBaseNula() {
        // Utils.java:137 output.get("P_OUT_VALOR").getAsString() on a null value throws
        PlataformaPort sinRuta = mock(PlataformaPort.class);
        JobResultado r = db.runner(new InterfazContable(sinRuta, repo -> servicio, repositorio, RELOJ)).ejecutar(CLASE, 77L, params("100"));

        assertThat(r.desenlace()).isEqualTo(Desenlace.ERROR);
        verifyNoInteractions(servicio);
        assertThat(db.rutaSalida(77L)).isEqualTo("ANTERIOR");
    }

    @Test
    @DisplayName("CONT-J-72: the service throws -> ERROR with its message, RutaSalida rolled back (not updated)")
    void contJ72_servicioFalla() {
        // InterfazContable.java:179-181 rethrow -> BcsJob.java:140-159
        when(servicio.procesar(anyLong(), anyLong(), anyString(), anyList())).thenThrow(new IllegalStateException("HomologaRespuestaSap no disponible"));

        JobResultado r = ejecutar(params("100"));

        assertThat(r.desenlace()).isEqualTo(Desenlace.ERROR);
        assertThat(db.estados().llamadas()).containsExactly(new Estado(77L, true, "HomologaRespuestaSap no disponible"));
        assertThat(db.rutaSalida(77L)).isEqualTo("ANTERIOR");
    }

    @Test
    @DisplayName("CONT-J-73: each successful pre-voucher is committed right after its state update (:244): it survives a later failure; the 'E' batch does not")
    void contJ73_commitPorPrecomprobante() {
        // InterfazContable.java:243-244 spActualizarEstado.execute(); dbConnection.commit();  :175 batch 'E' is NOT committed by the job
        when(servicio.procesar(anyLong(), anyLong(), anyString(), anyList())).thenAnswer(inv -> {
            InterfazContableRepository repo = repoEntregado.get();
            repo.actualizarEstadoContabilizado(1L, 14L, 100L);
            repo.marcarConError(List.of(200L));
            throw new IllegalStateException("caida despues del envio");
        });
        org.mockito.Mockito.doAnswer(inv -> { db.marcar("CON-" + inv.getArgument(2)); return null; })
                .when(repositorio).actualizarEstadoContabilizado(anyLong(), anyLong(), anyLong());
        org.mockito.Mockito.doAnswer(inv -> { db.marcar("E-" + inv.<List<Long>>getArgument(0)); return null; })
                .when(repositorio).marcarConError(anyList());

        JobResultado r = ejecutar(params("100;200"));

        assertThat(r.desenlace()).isEqualTo(Desenlace.ERROR);
        assertThat(db.marcas()).containsExactly("CON-100");
        verify(repositorio).actualizarEstadoContabilizado(1L, 14L, 100L);
        verify(repositorio).marcarConError(List.of(200L));
    }

    @Test
    @DisplayName("CONT-J-74: on a clean run the state update, the 'E' batch and RutaSalida all persist; homologation rows come from the job's repository")
    void contJ74_repositorioDelegado() {
        List<HomologaRespuestaSap> homologa = List.of(new HomologaRespuestaSap(-5002L, "Invalid account", "La cuenta {cuentaContable} no es valida"));
        when(repositorio.consultarHomologaRespuestaSap()).thenReturn(homologa);
        when(servicio.procesar(anyLong(), anyLong(), anyString(), anyList())).thenAnswer(inv -> {
            InterfazContableRepository repo = repoEntregado.get();
            assertThat(repo.consultarHomologaRespuestaSap()).isEqualTo(homologa);
            repo.actualizarEstadoContabilizado(1L, 14L, 100L);
            repo.marcarConError(List.of(200L));
            return new ResultadoInterfazContable(List.of(100L), List.of(200L), List.of("a", "b"));
        });
        org.mockito.Mockito.doAnswer(inv -> { db.marcar("CON-" + inv.getArgument(2)); return null; })
                .when(repositorio).actualizarEstadoContabilizado(anyLong(), anyLong(), anyLong());
        org.mockito.Mockito.doAnswer(inv -> { db.marcar("E-" + inv.<List<Long>>getArgument(0)); return null; })
                .when(repositorio).marcarConError(anyList());

        JobResultado r = ejecutar(params("100;200"));

        assertThat(r.desenlace()).isEqualTo(Desenlace.COMMIT);
        assertThat(db.marcas()).containsExactly("CON-100", "E-[200]");
        assertThat(db.rutaSalida(77L)).isEqualTo(ARCHIVO);
    }

    @Test
    @DisplayName("CONT-J-75: SAP login failure (every id in error) is still COMMIT with RutaSalida set: the job never decides on SAP results")
    void contJ75_siempreTrue() {
        // InterfazContable.java:185 return true regardless of secuenciasError
        devolver(List.of(), List.of(100L), "ERROR AL ENVIAR PRECOMPROBANTE A SAP. MENSAJE: [UI_ERROR_CONEXION_SAP]{}");

        JobResultado r = ejecutar(params("100"));

        assertThat(r.desenlace()).isEqualTo(Desenlace.COMMIT);
        assertThat(db.rutaSalida(77L)).isEqualTo(ARCHIVO);
        assertThat(Archivos.leer(Path.of(base + ARCHIVO))).isEqualTo("ERROR AL ENVIAR PRECOMPROBANTE A SAP. MENSAJE: [UI_ERROR_CONEXION_SAP]{}\n");
    }

    @Test
    @DisplayName("CONT-J-76: registered under the legacy class name cl.bcs.contabilidad.jobs.InterfazContable")
    void contJ76_clase() {
        assertThat(job().clase()).isEqualTo(CLASE);
    }
}
