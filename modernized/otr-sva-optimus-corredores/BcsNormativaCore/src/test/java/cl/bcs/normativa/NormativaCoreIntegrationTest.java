package cl.bcs.normativa;

import cl.bcs.normativa.adapter.out.*;
import cl.bcs.normativa.domain.*;
import cl.bcs.normativa.service.EsquemaService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for BcsNormativaCore against H2 in-memory DB.
 *
 * Metodologia: @SpringBootTest con H2, fixtures en data-normativa.sql.
 * Tests HANA (SPs, batch) marcados @Disabled("requires HANA").
 *
 * Business rules verified:
 *   B-NOR-INT-01: findDocumentoById devuelve documento existente
 *   B-NOR-INT-02: setEnProceso actualiza indicador 'S'/'N' y fecha inicio
 *   B-NOR-INT-03: findEsquemaByNombreVersion incluye lista de campos
 *   B-NOR-INT-04: findValidacionesActivas excluye inactivas
 *   B-NOR-INT-05: findEmpresaById devuelve empresa
 *   B-NOR-INT-06: findConfiguracion devuelve valor de parametro
 *   B-NOR-INT-07: findParametro devuelve valor de parametro de esquema
 *   B-NOR-INT-08: insertarResumenCarga asigna id y persiste
 *   B-NOR-INT-09: buscarClasificador devuelve clasificador existente
 *   B-NOR-INT-10: buscarCuadratura devuelve cuadratura existente
 *   B-NOR-INT-11: GET /api/normativa/documentos/{id} 200 OK
 *   B-NOR-INT-12: GET /api/normativa/esquemas/{nombre}/{version} 200 OK
 *   B-NOR-INT-13: documento inexistente -> 400 Bad Request
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("NormativaCore — Integration tests (H2)")
class NormativaCoreIntegrationTest {

    @Autowired
    private DocumentoJdbcAdapter documentoAdapter;

    @Autowired
    private ValidacionJdbcAdapter validacionAdapter;

    @Autowired
    private EmpresaJdbcAdapter empresaAdapter;

    @Autowired
    private ConfiguracionJdbcAdapter configuracionAdapter;

    @Autowired
    private ResumenJdbcAdapter resumenAdapter;

    @Autowired
    private ClasificadorJdbcAdapter clasificadorAdapter;

    @Autowired
    private CuadraturaJdbcAdapter cuadraturaAdapter;

    @Autowired
    private EsquemaService esquemaService;

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    // =========================================================================
    // B-NOR-INT-01
    // =========================================================================
    @Test
    @DisplayName("B-NOR-INT-01: findDocumentoById devuelve documento de fixtures")
    void findDocumentoById_existente_retornaDocumento() {
        Optional<Documento> opt = documentoAdapter.buscarPorId(100L);
        assertThat(opt).isPresent();
        Documento doc = opt.get();
        assertThat(doc.getId()).isEqualTo(100L);
        assertThat(doc.getNombreEsquema()).isEqualTo("DJ1937");
        assertThat(doc.getVersionEsquema()).isEqualTo("v1");
        assertThat(doc.getIdEmpresa()).isEqualTo(1001L);
        assertThat(doc.isEnProceso()).isFalse();
    }

    @Test
    @DisplayName("B-NOR-INT-01: documento inexistente retorna Optional.empty")
    void findDocumentoById_inexistente_retornaEmpty() {
        Optional<Documento> opt = documentoAdapter.buscarPorId(99999L);
        assertThat(opt).isEmpty();
    }

    // =========================================================================
    // B-NOR-INT-02
    // =========================================================================
    @Test
    @DisplayName("B-NOR-INT-02: setEnProceso=true actualiza indicador a 'S'")
    void setEnProceso_true_actualizaIndicador() {
        documentoAdapter.setEnProceso(100L, true);
        Optional<Documento> opt = documentoAdapter.buscarPorId(100L);
        assertThat(opt).isPresent();
        assertThat(opt.get().isEnProceso()).isTrue();
    }

    @Test
    @DisplayName("B-NOR-INT-02: setEnProceso=false actualiza indicador a 'N'")
    void setEnProceso_false_actualizaIndicador() {
        documentoAdapter.setEnProceso(100L, false);
        Optional<Documento> opt = documentoAdapter.buscarPorId(100L);
        assertThat(opt).isPresent();
        assertThat(opt.get().isEnProceso()).isFalse();
    }

    // =========================================================================
    // B-NOR-INT-03
    // =========================================================================
    @Test
    @DisplayName("B-NOR-INT-03: findEsquema incluye 3 campos")
    void findEsquema_incluyeCampos() {
        Esquema esquema = esquemaService.findByNombreVersion("DJ1937", "v1");
        assertThat(esquema).isNotNull();
        assertThat(esquema.getNombre()).isEqualTo("DJ1937");
        assertThat(esquema.getCampos()).hasSize(3);
    }

    @Test
    @DisplayName("B-NOR-INT-03: campos ordenados por Orden ASC")
    void findEsquema_camposOrdenadosPorOrden() {
        Esquema esquema = esquemaService.findByNombreVersion("DJ1937", "v1");
        List<Campo> campos = esquema.getCampos();
        assertThat(campos.get(0).getNombre()).isEqualTo("Periodo");
        assertThat(campos.get(1).getNombre()).isEqualTo("RutCliente");
        assertThat(campos.get(2).getNombre()).isEqualTo("Monto");
    }

    @Test
    @DisplayName("B-NOR-INT-03: campo Periodo es comun=true")
    void findEsquema_campoPeriodo_esComun() {
        Esquema esquema = esquemaService.findByNombreVersion("DJ1937", "v1");
        Campo periodo = esquema.buscarCampoPorNombre("Periodo");
        assertThat(periodo.isComun()).isTrue();
    }

    @Test
    @DisplayName("B-NOR-INT-03: esquema inexistente lanza IllegalArgumentException")
    void findEsquema_inexistente_lanzaException() {
        assertThatThrownBy(() -> esquemaService.findByNombreVersion("XXXXX", "v0"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // =========================================================================
    // B-NOR-INT-04
    // =========================================================================
    @Test
    @DisplayName("B-NOR-INT-04: validaciones activas retorna solo activas")
    void findValidaciones_soloActivas() {
        List<Validacion> validaciones = validacionAdapter.buscarActivasPorEsquema("DJ1937", "v1");
        assertThat(validaciones).hasSize(1);
        assertThat(validaciones.get(0).getCodigo()).isEqualTo("V001");
        assertThat(validaciones.get(0).isActiva()).isTrue();
    }

    // =========================================================================
    // B-NOR-INT-05
    // =========================================================================
    @Test
    @DisplayName("B-NOR-INT-05: findEmpresaById retorna empresa")
    void findEmpresaById_existente_retornaEmpresa() {
        Optional<Empresa> opt = empresaAdapter.buscarPorId(1001L);
        assertThat(opt).isPresent();
        Empresa empresa = opt.get();
        assertThat(empresa.getId()).isEqualTo(1001L);
        assertThat(empresa.getRut()).isEqualTo("12345678-9");
        assertThat(empresa.isCorredora()).isTrue();
    }

    // =========================================================================
    // B-NOR-INT-06
    // =========================================================================
    @Test
    @DisplayName("B-NOR-INT-06: findConfiguracion retorna valor")
    void findConfiguracion_existente_retornaValor() {
        Optional<String> opt = configuracionAdapter.buscarValor(1001L, "RUTA_ARCHIVOS");
        assertThat(opt).isPresent().contains("/data/normativa/dj1937");
    }

    @Test
    @DisplayName("B-NOR-INT-06: configuracion inexistente retorna empty")
    void findConfiguracion_inexistente_retornaEmpty() {
        Optional<String> opt = configuracionAdapter.buscarValor(1001L, "NO_EXISTE");
        assertThat(opt).isEmpty();
    }

    // =========================================================================
    // B-NOR-INT-07
    // =========================================================================
    @Test
    @DisplayName("B-NOR-INT-07: findParametro retorna valor")
    void findParametro_existente_retornaValor() {
        Optional<String> opt = configuracionAdapter.buscarValor("DJ1937", "v1", "FORMATO_FECHA");
        assertThat(opt).isPresent().contains("dd/MM/yyyy");
    }

    // =========================================================================
    // B-NOR-INT-08
    // =========================================================================
    @Test
    @DisplayName("B-NOR-INT-08: insertarResumenCarga asigna id y persiste")
    void insertarResumenCarga_asignaIdYPersiste() {
        var resumen = new ResumenCarga();
        resumen.setIdDocumento(100L);
        resumen.setIdUsuario(10L);
        resumen.setGlosa("Test carga");
        resumen.setNombreArchivo("test.csv");
        resumen.setTotalRegistrosLeidos(100);
        resumen.setTotalRegistrosCargados(95);
        resumen.setTotalRegistrosErroneos(5);

        resumenAdapter.insertar(resumen);
        assertThat(resumen.getId()).isGreaterThan(0L);
    }

    // =========================================================================
    // B-NOR-INT-09
    // =========================================================================
    @Test
    @DisplayName("B-NOR-INT-09: buscarClasificador devuelve clasificador existente")
    void buscarClasificador_existente_retornaClasificador() {
        Optional<Clasificador> opt = clasificadorAdapter.buscarPorClave("DJ1937", "v1", "TIPO_RENTA");
        assertThat(opt).isPresent();
        assertThat(opt.get().getTipo()).isEqualTo("TIPO_RENTA");
    }

    // =========================================================================
    // B-NOR-INT-10
    // =========================================================================
    @Test
    @DisplayName("B-NOR-INT-10: buscarCuadratura devuelve cuadratura existente")
    void buscarCuadratura_existente_retornaCuadratura() {
        Optional<Cuadratura> opt = cuadraturaAdapter.buscarPorClave("DJ1937", "v1", "RENTA");
        assertThat(opt).isPresent();
        assertThat(opt.get().getTipo()).isEqualTo("RENTA");
    }

    // =========================================================================
    // B-NOR-INT-11: REST controller
    // =========================================================================
    @Test
    @DisplayName("B-NOR-INT-11: GET /api/normativa/documentos/100 returns 200")
    void getDocumento_existente_returns200() throws Exception {
        mockMvc.perform(get("/api/normativa/documentos/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.nombreEsquema").value("DJ1937"));
    }

    @Test
    @DisplayName("B-NOR-INT-13: GET /api/normativa/documentos/99999 returns 400")
    void getDocumento_inexistente_returns400() throws Exception {
        mockMvc.perform(get("/api/normativa/documentos/99999"))
                .andExpect(status().isBadRequest());
    }

    // =========================================================================
    // B-NOR-INT-12: REST controller esquema
    // =========================================================================
    @Test
    @DisplayName("B-NOR-INT-12: GET /api/normativa/esquemas/DJ1937/v1 returns 200")
    void getEsquema_existente_returns200() throws Exception {
        mockMvc.perform(get("/api/normativa/esquemas/DJ1937/v1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("DJ1937"))
                .andExpect(jsonPath("$.version").value("v1"));
    }

    // =========================================================================
    // HANA-specific tests (disabled)
    // =========================================================================

    @Test
    @Disabled("requires HANA stored procedure: BAT.AgregarPrograma")
    @DisplayName("scheduleProcess requires HANA stored procedure")
    void scheduleProcess_requiresHana() {
        // Batch scheduling uses HANA-specific stored procedures
        // Cannot be tested with H2
    }
}
