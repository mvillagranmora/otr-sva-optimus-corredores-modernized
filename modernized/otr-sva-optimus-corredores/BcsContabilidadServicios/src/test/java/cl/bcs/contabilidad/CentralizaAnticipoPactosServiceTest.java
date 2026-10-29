package cl.bcs.contabilidad;

import cl.bcs.contabilidad.domain.model.EmpresaInfo;
import cl.bcs.contabilidad.domain.model.EntradaCentralizacion;
import cl.bcs.contabilidad.domain.model.ResultadoCentralizacion;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabDatoMovimientoRef;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabMovimientoRef;
import cl.bcs.contabilidad.domain.port.out.ContabilidadRepository;
import cl.bcs.contabilidad.domain.port.out.ContabilidadRepository.AnticipoPactosDatoMovimiento;
import cl.bcs.contabilidad.domain.port.out.PlataformaPort;
import cl.bcs.contabilidad.domain.service.CentralizaAnticipoPactos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Characterization tests for CentralizaAnticipoPactos service.
 *
 * Behaviour pinned:
 *   B-04  centralizar() calls consultarEmpresa(idEmpresa) to get IdPais, then
 *         calls obtenerDiaAnterior(idPais, fechaContable), then queries
 *         movements/events/datos from the repository, and maps each datos-row
 *         to exactly 26 TabDatoMovimientoRef entries with specific field names.
 *         ResultadoCentralizacion.ejercicio is String.valueOf(Long) — Long→String.
 *   B-02  When simulaOperacion="S" the repository persistirCentralizacion method
 *         is never called, but resultados are still populated.
 *
 * Unmocked static helpers (mapDatosMovimiento, toResultado) are tested directly
 * because they encode field-name-level oracle behaviour that must be exact.
 */
@ExtendWith(MockitoExtension.class)
class CentralizaAnticipoPactosServiceTest {

    private static final long   ID_EMPRESA          = 1L;
    private static final long   ID_PAIS             = 56L;   // Chile
    private static final long   ID_TIPO_CENTRAL     = 200L;
    private static final String FECHA_CONTABLE      = "2025-03-10";
    private static final String FECHA_ANTERIOR      = "2025-03-09";
    private static final long   ID_MOVIMIENTO       = 7001L;
    private static final long   EJERCICIO_LONG      = 2025L;
    private static final String EJERCICIO_STRING    = "2025"; // oracle: String.valueOf(Long)

    @Mock
    PlataformaPort plataforma;

    @Mock
    ContabilidadRepository repository;

    CentralizaAnticipoPactos service;

    @BeforeEach
    void setUp() {
        service = new CentralizaAnticipoPactos(plataforma, repository);
    }

    // -----------------------------------------------------------------------
    // Static helper tests — these run immediately (no @Disabled needed because
    // the helpers are already implemented in the stub class).
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("B-04: mapDatosMovimiento produces exactly 26 entries per movement row")
    void rule_b04_mapDatosMovimientoProduces26Entries() {
        AnticipoPactosDatoMovimiento dato = buildDatoMovimiento(ID_MOVIMIENTO);

        List<TabDatoMovimientoRef> result =
                CentralizaAnticipoPactos.mapDatosMovimiento(ID_TIPO_CENTRAL, dato);

        // Oracle: legacy CentralizaAnticipoPactos lines 111-138 add exactly 26 rows
        assertThat(result)
                .as("B-04: exactly 26 TabDatoMovimientoRef entries per movement")
                .hasSize(26);
    }

    @Test
    @DisplayName("B-04: mapDatosMovimiento — all entries carry ValorDecimal = BigDecimal.ZERO")
    void rule_b04_allDatoEntriesHaveValorDecimalZero() {
        AnticipoPactosDatoMovimiento dato = buildDatoMovimiento(ID_MOVIMIENTO);

        List<TabDatoMovimientoRef> result =
                CentralizaAnticipoPactos.mapDatosMovimiento(ID_TIPO_CENTRAL, dato);

        // Legacy new TabDatosMovimiento(..., BigDecimal.ZERO) for every field
        assertThat(result)
                .as("B-04: every dato entry must have ValorDecimal == ZERO")
                .allMatch(r -> BigDecimal.ZERO.compareTo(r.valorDecimal()) == 0);
    }

    @Test
    @DisplayName("B-04: mapDatosMovimiento — first entry has TipoDato='IdMovimiento' and value = String.valueOf(idMovimiento)")
    void rule_b04_firstEntryIsIdMovimiento() {
        AnticipoPactosDatoMovimiento dato = buildDatoMovimiento(ID_MOVIMIENTO);

        List<TabDatoMovimientoRef> result =
                CentralizaAnticipoPactos.mapDatosMovimiento(ID_TIPO_CENTRAL, dato);

        TabDatoMovimientoRef first = result.get(0);
        assertThat(first.tipoDato())
                .as("B-04: first tipoDato must be 'IdMovimiento'")
                .isEqualTo("IdMovimiento");
        assertThat(first.valorVarChar())
                .as("B-04: IdMovimiento value = String.valueOf(idMovimiento)")
                .isEqualTo(String.valueOf(ID_MOVIMIENTO));
    }

    @Test
    @DisplayName("B-04: mapDatosMovimiento — 'PortafolioCliente' key (not 'Portafolio') matches legacy line 114")
    void rule_b04_portafolioFieldNameIsPortafolioCliente() {
        // Legacy line 114: new TabDatosMovimiento(..., "PortafolioCliente", mov.Portafolio, ...)
        AnticipoPactosDatoMovimiento dato = buildDatoMovimiento(ID_MOVIMIENTO);

        List<TabDatoMovimientoRef> result =
                CentralizaAnticipoPactos.mapDatosMovimiento(ID_TIPO_CENTRAL, dato);

        assertThat(result)
                .as("B-04: field name must be 'PortafolioCliente' (not 'Portafolio')")
                .anyMatch(r -> "PortafolioCliente".equals(r.tipoDato())
                               && "PORT-001".equals(r.valorVarChar()));
    }

    @Test
    @DisplayName("B-04: mapDatosMovimiento — 'IdTipoReajuste' key (not 'TipoReajuste') matches legacy line 124")
    void rule_b04_tipoReajusteFieldNameIsIdTipoReajuste() {
        // Legacy line 124: new TabDatosMovimiento(..., "IdTipoReajuste", mov.TipoReajuste, ...)
        AnticipoPactosDatoMovimiento dato = buildDatoMovimiento(ID_MOVIMIENTO);

        List<TabDatoMovimientoRef> result =
                CentralizaAnticipoPactos.mapDatosMovimiento(ID_TIPO_CENTRAL, dato);

        assertThat(result)
                .as("B-04: field name must be 'IdTipoReajuste' (not 'TipoReajuste')")
                .anyMatch(r -> "IdTipoReajuste".equals(r.tipoDato())
                               && "REAJ-UF".equals(r.valorVarChar()));
    }

    @Test
    @DisplayName("B-04: mapDatosMovimiento — 'FolioOperacion' value is String.valueOf(Long)")
    void rule_b04_folioOperacionValueIsStringOfLong() {
        // Legacy line 137: String.valueOf(mov.FolioOperacion)
        AnticipoPactosDatoMovimiento dato = buildDatoMovimiento(ID_MOVIMIENTO);

        List<TabDatoMovimientoRef> result =
                CentralizaAnticipoPactos.mapDatosMovimiento(ID_TIPO_CENTRAL, dato);

        assertThat(result)
                .as("B-04: FolioOperacion tipoDato must exist with String value")
                .anyMatch(r -> "FolioOperacion".equals(r.tipoDato())
                               && "88001".equals(r.valorVarChar()));
    }

    @Test
    @DisplayName("B-04: mapDatosMovimiento — null String fields become empty string (not literal 'null')")
    void rule_b04_nullStringFieldsBecomeEmptyString() {
        // Legacy TabDatosMovimiento constructor:
        //   this.ValorVarChar = ValorVarChar == null ? "" : ValorVarChar;
        // This applies when String.valueOf() is NOT called; for pure String fields
        // (documentoIdentidad, nombreCliente, …) that are passed as null directly.
        //
        // NOTE: Long fields (idMovimiento, folioOperacion) go through String.valueOf(),
        // which produces "null" for null Longs — that is the legacy oracle for those fields
        // and is tested separately.  We use a valid folioOperacion here to isolate
        // the String null-coalescing behaviour.
        AnticipoPactosDatoMovimiento datoWithNullStrings = new AnticipoPactosDatoMovimiento(
                ID_MOVIMIENTO,
                null,   // documentoIdentidad
                null,   // nombreCliente
                null,   // portafolio
                null,   // moneda
                null,   // producto
                null,   // claseInstrumento
                null,   // tipoInstrumento
                null,   // nemo
                null,   // boveda
                null,   // idCartera
                null,   // emisor
                null,   // tipoReajuste
                null,   // tipoEmisor
                null,   // codigoIFRS
                null,   // tipoCartera
                null,   // claseEmisor
                null,   // agente
                null,   // nombreAgente
                null,   // sucursalAgente
                null,   // nombreSucursalAgente
                null,   // centroCostoAgente
                null,   // relacion
                null,   // compraVenta
                null,   // mesa
                0L      // folioOperacion — use 0 (not null) to isolate String-field behaviour
        );

        List<TabDatoMovimientoRef> result =
                CentralizaAnticipoPactos.mapDatosMovimiento(ID_TIPO_CENTRAL, datoWithNullStrings);

        // All non-Long fields that were null must become "" (never literal "null")
        // FolioOperacion = "0" (String.valueOf(0L)), IdMovimiento = String.valueOf(ID_MOVIMIENTO)
        assertThat(result)
                .as("B-04: pure String fields that are null must produce empty string, not 'null'")
                .filteredOn(r -> !r.tipoDato().equals("FolioOperacion")
                              && !r.tipoDato().equals("IdMovimiento"))
                .noneMatch(r -> "null".equals(r.valorVarChar()));
    }

    @Test
    @DisplayName("B-04: mapDatosMovimiento — null Long folioOperacion becomes literal string 'null' (legacy oracle)")
    void rule_b04_nullLongFolioOperacionBecomesStringNull() {
        // Legacy line 137: String.valueOf(mov.FolioOperacion)
        // String.valueOf((Long)null) returns "null" — this is the legacy oracle.
        // The TabDatosMovimiento constructor null-check applies AFTER String.valueOf,
        // so the "null" String is not coalesced to "".
        AnticipoPactosDatoMovimiento datoNullFolio = new AnticipoPactosDatoMovimiento(
                ID_MOVIMIENTO,
                "12345678-9", "CLIENTE", "PORT", "CLP", "PROD",
                "CLASE", "TIPO", "NEMO", "BOV", "CART",
                "EMISOR", "REAJ", "TEMIS", "IFRS", "TCART",
                "CEMIS", "AGT", "NAGENTE", "SUC", "NSUC",
                "CC", "REL", "C", "MESA",
                null   // folioOperacion = null Long
        );

        List<TabDatoMovimientoRef> result =
                CentralizaAnticipoPactos.mapDatosMovimiento(ID_TIPO_CENTRAL, datoNullFolio);

        assertThat(result)
                .as("B-04: null Long FolioOperacion must produce literal string 'null' (String.valueOf oracle)")
                .filteredOn(r -> "FolioOperacion".equals(r.tipoDato()))
                .singleElement()
                .extracting(TabDatoMovimientoRef::valorVarChar)
                .isEqualTo("null");
    }

    @Test
    @DisplayName("B-04: toResultado — Ejercicio is String.valueOf(Long) — Long converted to String")
    void rule_b04_toResultadoConvertsEjercicioLongToString() {
        // Legacy BcsCentralizacion.llenarResultado():
        //   resultado.Ejercicio = String.valueOf(mov.Ejercicio);
        TabMovimientoRef mov = buildTabMovimientoRef(EJERCICIO_LONG);

        ResultadoCentralizacion r = CentralizaAnticipoPactos.toResultado(mov);

        assertThat(r.ejercicio())
                .as("B-04: ejercicio must be String '2025', not Long 2025")
                .isEqualTo(EJERCICIO_STRING);
    }

    @Test
    @DisplayName("B-04: toResultado — all other fields are copied unchanged from TabMovimientoRef")
    void rule_b04_toResultadoCopiesAllFieldsFromTabMovimiento() {
        TabMovimientoRef mov = buildTabMovimientoRef(EJERCICIO_LONG);

        ResultadoCentralizacion r = CentralizaAnticipoPactos.toResultado(mov);

        assertThat(r.idTipoCentralizacion()).isEqualTo(ID_TIPO_CENTRAL);
        assertThat(r.idMovimiento()).isEqualTo(ID_MOVIMIENTO);
        assertThat(r.idTipoMovimiento()).isEqualTo("MANTPAC");
        assertThat(r.idEmpresa()).isEqualTo(ID_EMPRESA);
        assertThat(r.numero()).isEqualTo(ID_MOVIMIENTO);
        assertThat(r.documentoIdentidad()).isEqualTo("12345678-9");
        assertThat(r.idPortafolio()).isEqualTo(300L);
        assertThat(r.periodo()).isEqualTo("03");
        assertThat(r.monto()).isEqualByComparingTo(BigDecimal.valueOf(500_000));
        assertThat(r.idMoneda()).isEqualTo("CLP");
        assertThat(r.estado()).isEqualTo("V");
    }

    // -----------------------------------------------------------------------
    // Integration-level tests — disabled pending full service implementation
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("B-04: centralizar() calls consultarEmpresa then obtenerDiaAnterior in that order")
    void rule_b04_callOrderConsultarEmpresaThenObtenerDiaAnterior() {
        // Arrange
        when(plataforma.consultarEmpresa(ID_EMPRESA))
                .thenReturn(new EmpresaInfo(ID_EMPRESA, ID_PAIS, "76000001-1", "BROKER TEST"));
        when(plataforma.obtenerDiaAnterior(ID_PAIS, FECHA_CONTABLE))
                .thenReturn(FECHA_ANTERIOR);
        when(repository.queryMovimientos(ID_EMPRESA, FECHA_CONTABLE, ID_TIPO_CENTRAL))
                .thenReturn(List.of());
        when(repository.queryEventosOperacion(ID_EMPRESA, FECHA_CONTABLE, ID_TIPO_CENTRAL))
                .thenReturn(List.of());
        when(repository.queryEventosSubyacente(ID_EMPRESA, FECHA_CONTABLE,
                ID_TIPO_CENTRAL, FECHA_ANTERIOR)).thenReturn(List.of());
        when(repository.queryDatosMovimientoAnticipo(ID_EMPRESA, FECHA_CONTABLE))
                .thenReturn(List.of());

        EntradaCentralizacion entrada = EntradaCentralizacion.realRun(
                ID_EMPRESA, 97L, "CON", ID_TIPO_CENTRAL,
                FECHA_CONTABLE, null, EJERCICIO_LONG);

        // Act
        service.centralizar(entrada);

        // Assert: call order — consultarEmpresa MUST precede obtenerDiaAnterior
        InOrder order = inOrder(plataforma);
        order.verify(plataforma).consultarEmpresa(ID_EMPRESA);
        order.verify(plataforma).obtenerDiaAnterior(ID_PAIS, FECHA_CONTABLE);
    }

    @Test
    @DisplayName("B-04: obtenerDiaAnterior is called with idPais from empresa (not hardcoded)")
    void rule_b04_obtenerDiaAnteriorUsesIdPaisFromEmpresa() {
        // EmpresaInfo.idPais is the oracle; nothing is hardcoded in the service
        long idPaisArgentina = 54L; // different from Chile (56)
        when(plataforma.consultarEmpresa(ID_EMPRESA))
                .thenReturn(new EmpresaInfo(ID_EMPRESA, idPaisArgentina, "30-71034...0", "BROKER AR"));
        when(plataforma.obtenerDiaAnterior(idPaisArgentina, FECHA_CONTABLE))
                .thenReturn(FECHA_ANTERIOR);
        when(repository.queryMovimientos(any(), any(), any())).thenReturn(List.of());
        when(repository.queryEventosOperacion(any(), any(), any())).thenReturn(List.of());
        when(repository.queryEventosSubyacente(any(), any(), any(), any())).thenReturn(List.of());
        when(repository.queryDatosMovimientoAnticipo(any(), any())).thenReturn(List.of());

        EntradaCentralizacion entrada = EntradaCentralizacion.realRun(
                ID_EMPRESA, 97L, "CON", ID_TIPO_CENTRAL,
                FECHA_CONTABLE, null, EJERCICIO_LONG);

        service.centralizar(entrada);

        // idPaisArgentina, not 56 (Chile)
        verify(plataforma).obtenerDiaAnterior(idPaisArgentina, FECHA_CONTABLE);
    }

    @Test
    @DisplayName("B-04: movimientos are queried with IdEmpresa, FechaContable, IdTipoCentralizacion from entrada")
    void rule_b04_movimientosQueriedWithCorrectParams() {
        when(plataforma.consultarEmpresa(ID_EMPRESA))
                .thenReturn(new EmpresaInfo(ID_EMPRESA, ID_PAIS, "76000001-1", "BROKER TEST"));
        when(plataforma.obtenerDiaAnterior(ID_PAIS, FECHA_CONTABLE)).thenReturn(FECHA_ANTERIOR);
        when(repository.queryMovimientos(ID_EMPRESA, FECHA_CONTABLE, ID_TIPO_CENTRAL))
                .thenReturn(List.of());
        when(repository.queryEventosOperacion(any(), any(), any())).thenReturn(List.of());
        when(repository.queryEventosSubyacente(any(), any(), any(), any())).thenReturn(List.of());
        when(repository.queryDatosMovimientoAnticipo(any(), any())).thenReturn(List.of());

        EntradaCentralizacion entrada = EntradaCentralizacion.realRun(
                ID_EMPRESA, 97L, "CON", ID_TIPO_CENTRAL,
                FECHA_CONTABLE, null, EJERCICIO_LONG);

        service.centralizar(entrada);

        verify(repository).queryMovimientos(ID_EMPRESA, FECHA_CONTABLE, ID_TIPO_CENTRAL);
    }

    @Test
    @DisplayName("B-04: eventosSubyacente query uses fechaAnterior returned by obtenerDiaAnterior")
    void rule_b04_eventosSubyacenteUseFechaAnteriorFromPlatforma() {
        // Legacy line 100: params.put("FechaAnterior", fechaAnterior)
        when(plataforma.consultarEmpresa(ID_EMPRESA))
                .thenReturn(new EmpresaInfo(ID_EMPRESA, ID_PAIS, "76000001-1", "BROKER TEST"));
        when(plataforma.obtenerDiaAnterior(ID_PAIS, FECHA_CONTABLE)).thenReturn(FECHA_ANTERIOR);
        when(repository.queryMovimientos(any(), any(), any())).thenReturn(List.of());
        when(repository.queryEventosOperacion(any(), any(), any())).thenReturn(List.of());
        when(repository.queryEventosSubyacente(
                eq(ID_EMPRESA), eq(FECHA_CONTABLE), eq(ID_TIPO_CENTRAL), eq(FECHA_ANTERIOR)))
                .thenReturn(List.of());
        when(repository.queryDatosMovimientoAnticipo(any(), any())).thenReturn(List.of());

        EntradaCentralizacion entrada = EntradaCentralizacion.realRun(
                ID_EMPRESA, 97L, "CON", ID_TIPO_CENTRAL,
                FECHA_CONTABLE, null, EJERCICIO_LONG);

        service.centralizar(entrada);

        verify(repository).queryEventosSubyacente(
                ID_EMPRESA, FECHA_CONTABLE, ID_TIPO_CENTRAL, FECHA_ANTERIOR);
    }

    @Test
    @DisplayName("B-04: one movement row produces P_OUT_RESULTADO with one ResultadoCentralizacion entry")
    void rule_b04_singleMovimientoProducesOneResultadoEntry() {
        TabMovimientoRef mov = buildTabMovimientoRef(EJERCICIO_LONG);
        when(plataforma.consultarEmpresa(ID_EMPRESA))
                .thenReturn(new EmpresaInfo(ID_EMPRESA, ID_PAIS, "76000001-1", "BROKER TEST"));
        when(plataforma.obtenerDiaAnterior(ID_PAIS, FECHA_CONTABLE)).thenReturn(FECHA_ANTERIOR);
        when(repository.queryMovimientos(any(), any(), any())).thenReturn(List.of(mov));
        when(repository.queryEventosOperacion(any(), any(), any())).thenReturn(List.of());
        when(repository.queryEventosSubyacente(any(), any(), any(), any())).thenReturn(List.of());
        when(repository.queryDatosMovimientoAnticipo(any(), any())).thenReturn(List.of());

        EntradaCentralizacion entrada = EntradaCentralizacion.realRun(
                ID_EMPRESA, 97L, "CON", ID_TIPO_CENTRAL,
                FECHA_CONTABLE, null, EJERCICIO_LONG);

        SalidaCentralizacion salida = service.centralizar(entrada);

        assertThat(salida.resultados())
                .as("B-04: one movement row must produce exactly one ResultadoCentralizacion")
                .hasSize(1);
        assertThat(salida.resultados().get(0).ejercicio())
                .as("B-04: ejercicio must be String '2025' (Long -> String conversion)")
                .isEqualTo(EJERCICIO_STRING);
    }

    @Test
    @DisplayName("B-02: simulation mode — persistirCentralizacion is never called")
    void rule_b02_simulationModeSkipsRepositoryWrites() {
        TabMovimientoRef mov = buildTabMovimientoRef(EJERCICIO_LONG);
        when(plataforma.consultarEmpresa(ID_EMPRESA))
                .thenReturn(new EmpresaInfo(ID_EMPRESA, ID_PAIS, "76000001-1", "BROKER TEST"));
        when(plataforma.obtenerDiaAnterior(ID_PAIS, FECHA_CONTABLE)).thenReturn(FECHA_ANTERIOR);
        when(repository.queryMovimientos(any(), any(), any())).thenReturn(List.of(mov));
        when(repository.queryEventosOperacion(any(), any(), any())).thenReturn(List.of());
        when(repository.queryEventosSubyacente(any(), any(), any(), any())).thenReturn(List.of());
        when(repository.queryDatosMovimientoAnticipo(any(), any())).thenReturn(List.of());

        EntradaCentralizacion entrada = EntradaCentralizacion.simulation(
                ID_EMPRESA, 97L, "CON", ID_TIPO_CENTRAL,
                FECHA_CONTABLE, null, EJERCICIO_LONG); // simulaOperacion = "S"

        SalidaCentralizacion salida = service.centralizar(entrada);

        // B-02: write method must never be called in simulation
        verify(repository, never())
                .persistirCentralizacion(any(), any(), any(), any(), any(),
                        any(), any(), any(), any());

        // B-02: resultados still computed even in simulation
        assertThat(salida.resultados())
                .as("B-02: simulation still returns computed resultados")
                .isNotEmpty();
    }

    @Test
    @DisplayName("B-02: real run (simulaOperacion=N) calls persistirCentralizacion once")
    void rule_b02_realRunCallsPersistir() {
        TabMovimientoRef mov = buildTabMovimientoRef(EJERCICIO_LONG);
        when(plataforma.consultarEmpresa(ID_EMPRESA))
                .thenReturn(new EmpresaInfo(ID_EMPRESA, ID_PAIS, "76000001-1", "BROKER TEST"));
        when(plataforma.obtenerDiaAnterior(ID_PAIS, FECHA_CONTABLE)).thenReturn(FECHA_ANTERIOR);
        when(repository.queryMovimientos(any(), any(), any())).thenReturn(List.of(mov));
        when(repository.queryEventosOperacion(any(), any(), any())).thenReturn(List.of());
        when(repository.queryEventosSubyacente(any(), any(), any(), any())).thenReturn(List.of());
        when(repository.queryDatosMovimientoAnticipo(any(), any())).thenReturn(List.of());

        EntradaCentralizacion entrada = EntradaCentralizacion.realRun(
                ID_EMPRESA, 97L, "CON", ID_TIPO_CENTRAL,
                FECHA_CONTABLE, null, EJERCICIO_LONG); // simulaOperacion = "N"

        service.centralizar(entrada);

        // B-02: real run must call persist exactly once
        verify(repository, times(1))
                .persistirCentralizacion(
                        eq(ID_TIPO_CENTRAL), eq(FECHA_CONTABLE),
                        eq(ID_EMPRESA), eq(EJERCICIO_LONG), any(),
                        any(), any(), any(), any());
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static AnticipoPactosDatoMovimiento buildDatoMovimiento(long idMov) {
        return new AnticipoPactosDatoMovimiento(
                idMov,
                "12345678-9",   // documentoIdentidad
                "CLIENTE TEST", // nombreCliente
                "PORT-001",     // portafolio  -> PortafolioCliente
                "CLP",          // moneda
                "PACTOS",       // producto
                "RENTA FIJA",   // claseInstrumento
                "BONO",         // tipoInstrumento
                "BONO001",      // nemo
                "BCN",          // boveda
                "CART-1",       // idCartera
                "EMISOR SA",    // emisor
                "REAJ-UF",      // tipoReajuste -> IdTipoReajuste
                "BANCO",        // tipoEmisor
                "CL-IFRS",      // codigoIFRS
                "DISPONIBLE",   // tipoCartera
                "INST FINANC",  // claseEmisor
                "AGT001",       // agente
                "AGENTE SA",    // nombreAgente
                "SUC-1",        // sucursalAgente
                "SUCURSAL CENTRO", // nombreSucursalAgente
                "CC-001",       // centroCostoAgente
                "RELACIONADO",  // relacion
                "C",            // compraVenta
                "MESA-RF",      // mesa
                88001L          // folioOperacion
        );
    }

    private static TabMovimientoRef buildTabMovimientoRef(long ejercicio) {
        return new TabMovimientoRef(
                ID_TIPO_CENTRAL, ID_MOVIMIENTO, "MANTPAC",
                ID_EMPRESA, ID_MOVIMIENTO, 1L,
                FECHA_CONTABLE, "12345678-9", 300L,
                ejercicio, "03",
                BigDecimal.valueOf(500_000), "CLP", "V");
    }
}
