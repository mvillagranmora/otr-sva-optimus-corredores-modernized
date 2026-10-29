package cl.bcs.contabilidad.jobs.job;

import cl.bcs.contabilidad.domain.model.sap.JournalVoucher;
import cl.bcs.contabilidad.domain.model.sap.ResultadoAsientoSap;
import cl.bcs.contabilidad.domain.model.sap.SapRespuesta;
import cl.bcs.contabilidad.domain.model.sap.SapSesion;
import cl.bcs.contabilidad.domain.port.out.DiccionarioPort;
import cl.bcs.contabilidad.domain.port.out.InterfazContableRepository;
import cl.bcs.contabilidad.domain.port.out.PlataformaPort;
import cl.bcs.contabilidad.domain.port.out.SapGateway;
import cl.bcs.contabilidad.jobs.runtime.JobResultado;
import cl.bcs.contabilidad.jobs.runtime.JobResultado.Desenlace;
import cl.bcs.contabilidad.jobs.support.Archivos;
import cl.bcs.contabilidad.jobs.support.H2Jobs;
import cl.bcs.contabilidad.service.GenerarJsonAsientoSapService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Decision 2026-10-15, point 5 (iii), for legacy {@code BcsContabilidadJobs/src/cl/bcs/contabilidad/jobs/InterfazContable.java}:
 * the legacy appended each run-log line to {@code <PLA.RutaBaseArchivos>ITZ/<ts>_InterfazContable.log} as soon as it
 * was known ({@code FileUtils.writeStringToFile(file, ..., true)} at :108, :123, :160, :164, :169), so when the run
 * failed later the file held the lines written up to that point. Two later failure points propagate out of
 * {@code logica}: the HomologaRespuestaSap query (:116) and the batch that sets Estado 'E' (:175).
 *
 * <p>Wiring: the REAL {@code InterfazContableService} through the production factory
 * {@link InterfazContable#servicioConBitacora}; SAP gateway, voucher generator and repository are mocks. Every value
 * is fake; nothing connects to SAP or HANA.
 */
class InterfazContableFallaParcialTest {

    static final String CLASE = "cl.bcs.contabilidad.jobs.InterfazContable";
    static final Clock RELOJ = Clock.fixed(Instant.parse("2025-04-01T13:15:30Z"), ZoneId.of("America/Santiago"));
    static final String ARCHIVO = "ITZ/20250401101530_InterfazContable.log";

    @TempDir
    Path tmp;

    private String base;
    private final H2Jobs db = H2Jobs.nueva();
    private final PlataformaPort plataforma = mock(PlataformaPort.class);
    private final GenerarJsonAsientoSapService generador = mock(GenerarJsonAsientoSapService.class);
    private final SapGateway sap = mock(SapGateway.class);
    private final InterfazContableRepository repositorio = mock(InterfazContableRepository.class);
    /** Same deterministic dictionary as the legacy-oracle shim: "[CODIGO]{k=v}". */
    private final DiccionarioPort diccionario =
            (idEmpresa, codigo, valores) -> "[" + codigo + "]" + new TreeMap<>(valores == null ? Map.of() : valores);

    @BeforeEach
    void preparar() {
        base = tmp.resolve("base").toString() + File.separator;
        when(plataforma.obtenerParametroConfiguracion(anyLong(), eq("PLATAFORMA"), eq("PLA.RutaBaseArchivos"), any())).thenReturn(base);
        db.programa(77L, "ANTERIOR");
        when(generador.generar(anyLong(), anyList()))
                .thenReturn(new ResultadoAsientoSap(List.of(), List.of(new JournalVoucher(null))));
        doAnswer(inv -> { db.marcar("CON-" + inv.getArgument(2)); return null; })
                .when(repositorio).actualizarEstadoContabilizado(anyLong(), anyLong(), anyLong());
    }

    private JobResultado ejecutar(String secuencias) {
        InterfazContable job = new InterfazContable(plataforma,
                InterfazContable.servicioConBitacora(generador, sap, diccionario), repositorio, RELOJ);
        Map<String, String> p = Map.of("IdEmpresa", "1", "IdUsuario", "14", "Destino", "B",
                "SecCmbte", Base64.getEncoder().encodeToString(secuencias.getBytes(StandardCharsets.UTF_8)));
        return db.runner(job).ejecutar(CLASE, 77L, p);
    }

    @Test
    @DisplayName("CONT-J-79 (5.iii): the 'E' batch fails after the vouchers -> ERROR, but the log file already holds every line; voucher 100 stays committed")
    void contJ79_fallaLoteE_archivoConLineasEscritas() {
        // InterfazContable.java:123 + :169 (100 accepted), :244 (its commit), :160 + :169 (200 rejected), then :175
        // sql.batch(...'E'...) throws -> :179-181 rethrow -> BcsJob rollback (:140-144); :178 RutaSalida never runs.
        when(sap.login()).thenReturn(new SapSesion(List.of("B1SESSION=fake-session-0001")));
        when(sap.enviarAsiento(any(), any())).thenReturn(new SapRespuesta(204, ""), new SapRespuesta(500, "periodo cerrado"));
        when(repositorio.consultarHomologaRespuestaSap()).thenReturn(List.of());
        doAnswer(inv -> { db.marcar("E-" + inv.getArgument(0)); throw new IllegalStateException("HANA: lote Estado 'E' rechazado"); })
                .when(repositorio).marcarConError(anyList());

        JobResultado r = ejecutar("100;200");

        assertThat(r.desenlace()).isEqualTo(Desenlace.ERROR);
        assertThat(r.mensaje()).isEqualTo("HANA: lote Estado 'E' rechazado");
        assertThat(Archivos.leer(Path.of(base + ARCHIVO))).isEqualTo(
                "PRECOMPROBANTE ENVIADO EXISTOSAMENTE: 100\n"
                        + "ERROR AL ENVIAR PRECOMPROBANTE A SAP: 200, Mensaje: periodo cerrado\n");
        assertThat(db.marcas()).as("the per-voucher commit survives, the 'E' batch is rolled back").containsExactly("CON-100");
        assertThat(db.rutaSalida(77L)).isEqualTo("ANTERIOR");
    }

    @Test
    @DisplayName("CONT-J-79b (5.iii): SAP login fails, then the HomologaRespuestaSap query fails -> ERROR; the file holds the connection-error line")
    void contJ79b_loginFallaYHomologacionFalla_archivoConLineaDeConexion() {
        // InterfazContable.java:105-113: the login error line is written at :108 inside the catch; then :116 throws.
        when(sap.login()).thenThrow(new IllegalStateException("SAP B1: login rechazado"));
        when(repositorio.consultarHomologaRespuestaSap()).thenThrow(new IllegalStateException("HANA: HomologaRespuestaSap no disponible"));

        JobResultado r = ejecutar("100");

        assertThat(r.desenlace()).isEqualTo(Desenlace.ERROR);
        assertThat(r.mensaje()).isEqualTo("HANA: HomologaRespuestaSap no disponible");
        assertThat(Archivos.leer(Path.of(base + ARCHIVO)))
                .isEqualTo("ERROR AL ENVIAR PRECOMPROBANTE A SAP. MENSAJE: [UI_ERROR_CONEXION_SAP]{}\n");
        assertThat(db.rutaSalida(77L)).isEqualTo("ANTERIOR");
    }

    @Test
    @DisplayName("CONT-J-79c (5.iii): SAP login works, then the HomologaRespuestaSap query fails -> ERROR and no log file (nothing was written yet)")
    void contJ79c_homologacionFalla_sinArchivo() {
        // InterfazContable.java:106 login ok, nothing written before :116 throws; FileUtils creates the file only on write.
        when(sap.login()).thenReturn(new SapSesion(List.of("B1SESSION=fake-session-0001")));
        when(repositorio.consultarHomologaRespuestaSap()).thenThrow(new IllegalStateException("HANA: HomologaRespuestaSap no disponible"));

        JobResultado r = ejecutar("100");

        assertThat(r.desenlace()).isEqualTo(Desenlace.ERROR);
        assertThat(Path.of(base + "ITZ")).doesNotExist();
        assertThat(db.marcas()).isEmpty();
    }
}
