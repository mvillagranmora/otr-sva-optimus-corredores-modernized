package cl.bcs.contabilidad.generico;

import cl.bcs.contabilidad.domain.model.EntradaCentralizacion;
import cl.bcs.contabilidad.domain.model.ResultadoCentralizacion;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabDatoMovimientoRef;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabEventoRef;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabMovimientoRef;
import cl.bcs.contabilidad.domain.port.out.ContabilidadRepository;
import cl.bcs.contabilidad.domain.service.BcsCentralizacionService;
import cl.bcs.contabilidad.domain.service.DevengoCarteraPropiaLibreYGarantia;
import cl.bcs.contabilidad.domain.service.devengo.CarteraValorizada;
import cl.bcs.contabilidad.domain.service.devengo.DevengoCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static cl.bcs.contabilidad.generico.DevengoFixtures.FECHA_CONTABLE;
import static cl.bcs.contabilidad.generico.DevengoFixtures.ID_EMPRESA;
import static cl.bcs.contabilidad.generico.DevengoFixtures.ID_INFORME;
import static cl.bcs.contabilidad.generico.DevengoFixtures.TIPO_CARTERA_PROPIA;
import static cl.bcs.contabilidad.generico.DevengoFixtures.cartera;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Characterization tests for legacy cl.bcs.contabilidad.DevengoCarteraPropiaLibreYGarantia
 * (IdTipoCentralizacion 450, "Propia a Tasa Compra").
 *
 * Legacy flow: DevengoCarteraPropiaLibreYGarantia.java:67-131. 30 dato fields per row
 * (:96-125). Movement/event key is IdCartera, not IdValorizacion. Only IdMovimiento
 * goes through String.valueOf; IdCartera and Relacion are plain Strings here.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DevengoCarteraPropiaLibreYGarantiaServiceTest {

    static final String SQL_MOV   = "cl/bcs/contabilidad/sql/CentralizaDevengoCarteraPropiaLibreYGarantia_SelectCarteraMovimientos.sql";
    static final String SQL_EV    = "cl/bcs/contabilidad/sql/CentralizaDevengoCarteraPropiaLibreYGarantia_SelectCarteraEventos.sql";
    static final String SQL_DATOS = "cl/bcs/contabilidad/sql/CentralizaDevengoCarteraPropiaLibreYGarantia_SelectDatosMovimiento.sql";

    static final Long TIPO = TIPO_CARTERA_PROPIA;

    @Mock
    ContabilidadRepository repository;

    DevengoCarteraPropiaLibreYGarantia service;

    @BeforeEach
    void setUp() {
        service = new DevengoCarteraPropiaLibreYGarantia(repository, new DevengoCalculator());
    }

    private static EntradaCentralizacion entrada(String simula) {
        return new EntradaCentralizacion(ID_EMPRESA, 97L, "CON", TIPO, FECHA_CONTABLE, ID_INFORME, 2025L, simula);
    }

    private void stub(List<CarteraValorizada> mov, List<CarteraValorizada> ev, List<Map<String, Object>> datos) {
        when(repository.queryCarteraValorizada(eq(SQL_MOV), eq(ID_EMPRESA), eq(ID_INFORME))).thenReturn(mov);
        when(repository.queryCarteraValorizada(eq(SQL_EV), eq(ID_EMPRESA), eq(ID_INFORME))).thenReturn(ev);
        when(repository.queryDatosMovimientoRows(eq(SQL_DATOS), any())).thenReturn(datos);
    }

    /** SQL aliases of CentralizaDevengoCarteraPropiaLibreYGarantia_SelectDatosMovimiento.sql:42-76. */
    static Map<String, Object> datosRow() {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("IdMovimiento", 55501L);
        r.put("DocumentoIdentidad", "33.333.333-3");
        r.put("NombreCliente", "CORREDORA PRUEBA");
        r.put("Portafolio", "PROPIA-01");
        r.put("Moneda", "CLP");
        r.put("Producto", "RF");
        r.put("ClaseInstrumento", "RF");
        r.put("TipoInstrumento", "BB");
        r.put("Nemo", "BONO-PRUEBA-1");
        r.put("Boveda", "DCV");
        r.put("IdCartera", "55501");
        r.put("Emisor", "EMISOR-Z");
        r.put("TipoReajuste", "UF");
        r.put("CodigoIFRS", "CRV");
        r.put("TipoCartera", "LIBRE");
        r.put("ClaseEmisor", "BANCO");
        r.put("Agente", "");
        r.put("NombreAgente", "");
        r.put("SucursalAgente", "");
        r.put("NombreSucursalAgente", "");
        r.put("CentroCostoAgente", "");
        r.put("Relacion", "CP");
        r.put("Bolsa", "BCS");
        r.put("FechaLiquidacion", java.sql.Date.valueOf("2025-01-31"));
        r.put("TipoPersona", "J");
        r.put("FechaOperacion", java.sql.Date.valueOf("2025-01-30"));
        r.put("IdConcepto", "CONC-3");
        r.put("DocumentoIdentidadCAVALI", "33333333-3");
        r.put("TipoEmisor", "TE-3");
        r.put("Mesa", "MESA-RF");
        return r;
    }

    @Test
    @DisplayName("CONT-C-40: queries by IdEmpresa + IdInforme; datos params p_IdEmpresa, p_IdInforme")
    // Legacy: DevengoCarteraPropiaLibreYGarantia.java:74-92
    void contC40_queries() {
        stub(List.of(), List.of(), List.of());

        service.centralizar(entrada("S"));

        assertThat(service).isInstanceOf(BcsCentralizacionService.class);
        verify(repository).queryCarteraValorizada(SQL_MOV, ID_EMPRESA, ID_INFORME);
        verify(repository).queryCarteraValorizada(SQL_EV, ID_EMPRESA, ID_INFORME);
        ArgumentCaptor<MapSqlParameterSource> p = ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(repository).queryDatosMovimientoRows(eq(SQL_DATOS), p.capture());
        assertThat(p.getValue().getValue("p_IdEmpresa")).isEqualTo(ID_EMPRESA);
        assertThat(p.getValue().getValue("p_IdInforme")).isEqualTo(ID_INFORME);
    }

    @Test
    @DisplayName("CONT-C-41: only RF/IF rows produce CARTERARF movements and *_TASA_COMPRA events keyed by IdCartera")
    // Legacy: CentralizaDevengoCarteraPropiaLibreYGarantia_SelectMovimientos.sql:2-7,22; _SelectEventos.sql:4,17
    void contC41_movimientosYEventos() {
        CarteraValorizada rf = cartera().idCartera(55501L).folioCartera(33001L).clasificacion("RF").montoReajuste("-0.6").build();
        CarteraValorizada rv = cartera().idCartera(55502L).clasificacion("RV").build();
        stub(List.of(rf, rv), List.of(rf, rv), List.of());

        SalidaCentralizacion s = service.centralizar(entrada("S"));

        assertThat(s.tabMovimientos()).extracting(TabMovimientoRef::idMovimiento, TabMovimientoRef::idTipoMovimiento,
                        TabMovimientoRef::numero, TabMovimientoRef::linea)
                .containsExactly(tuple(55501L, "CARTERARF", 33001L, 33001L));
        assertThat(s.tabEventos()).extracting(TabEventoRef::idEvento, TabEventoRef::montoEvento)
                .containsExactlyInAnyOrder(
                        tuple("MONTO_COMPRA", new BigDecimal("1000001")),
                        tuple("MONTO_ACTUAL_TASA_COMPRA", new BigDecimal("1002501")),
                        tuple("MONTO_PROXIMO_TASA_COMPRA", new BigDecimal("1002667")),
                        tuple("INTERES_DIARIO_TASA_COMPRA", new BigDecimal("167")),
                        tuple("REAJUSTE_NEGATIVO_TASA_COMPRA", new BigDecimal("-1")));
        assertThat(s.tabEventos()).allMatch(e -> e.idMovimiento().equals(55501L));
    }

    @Test
    @DisplayName("CONT-C-42: each datos row -> exactly 30 TabDatoMovimiento in legacy order (Portafolio, IdConcepto, DocumentoIdentidadCAVALI present)")
    // Legacy: DevengoCarteraPropiaLibreYGarantia.java:96-125
    void contC42_datosOrder() {
        stub(List.of(), List.of(), List.of(datosRow()));

        SalidaCentralizacion s = service.centralizar(entrada("S"));

        assertThat(s.tabDatosMovimiento()).extracting(TabDatoMovimientoRef::tipoDato, TabDatoMovimientoRef::valorVarChar)
                .containsExactly(
                        tuple("IdMovimiento", "55501"),
                        tuple("DocumentoIdentidad", "33.333.333-3"),
                        tuple("NombreCliente", "CORREDORA PRUEBA"),
                        tuple("Portafolio", "PROPIA-01"),
                        tuple("Moneda", "CLP"),
                        tuple("Producto", "RF"),
                        tuple("ClaseInstrumento", "RF"),
                        tuple("TipoInstrumento", "BB"),
                        tuple("Nemo", "BONO-PRUEBA-1"),
                        tuple("Boveda", "DCV"),
                        tuple("IdCartera", "55501"),
                        tuple("Emisor", "EMISOR-Z"),
                        tuple("TipoReajuste", "UF"),
                        tuple("CodigoIFRS", "CRV"),
                        tuple("TipoCartera", "LIBRE"),
                        tuple("ClaseEmisor", "BANCO"),
                        tuple("Agente", ""),
                        tuple("NombreAgente", ""),
                        tuple("SucursalAgente", ""),
                        tuple("NombreSucursalAgente", ""),
                        tuple("CentroCostoAgente", ""),
                        tuple("Relacion", "CP"),
                        tuple("Bolsa", "BCS"),
                        tuple("FechaLiquidacion", "2025-01-31"),
                        tuple("TipoPersona", "J"),
                        tuple("FechaOperacion", "2025-01-30"),
                        tuple("IdConcepto", "CONC-3"),
                        tuple("DocumentoIdentidadCAVALI", "33333333-3"),
                        tuple("TipoEmisor", "TE-3"),
                        tuple("Mesa", "MESA-RF"));
        assertThat(s.tabDatosMovimiento()).allSatisfy(d -> {
            assertThat(d.idTipoCentralizacion()).isEqualTo(TIPO);
            assertThat(d.idMovimiento()).isEqualTo(55501L);
            assertThat(d.valorDecimal()).isEqualByComparingTo(BigDecimal.ZERO);
        });
    }

    @Test
    @DisplayName("CONT-C-43: null IdMovimiento -> \"null\" (String.valueOf); null IdCartera/Relacion/others -> \"\"")
    // Legacy: DevengoCarteraPropiaLibreYGarantia.java:96 (String.valueOf), :106 and :117 (plain String); TabDatosMovimiento.java:23
    void contC43_nullHandling() {
        Map<String, Object> row = datosRow();
        row.replaceAll((k, v) -> k.startsWith("Fecha") ? v : null);
        stub(List.of(), List.of(), List.of(row));

        SalidaCentralizacion s = service.centralizar(entrada("S"));

        assertThat(s.tabDatosMovimiento()).filteredOn(d -> d.tipoDato().equals("IdMovimiento"))
                .singleElement().extracting(TabDatoMovimientoRef::valorVarChar).isEqualTo("null");
        assertThat(s.tabDatosMovimiento())
                .filteredOn(d -> !d.tipoDato().equals("IdMovimiento") && !d.tipoDato().startsWith("Fecha"))
                .hasSize(27).allMatch(d -> d.valorVarChar().isEmpty());
    }

    @Test
    @DisplayName("CONT-C-44: null FechaLiquidacion -> NullPointerException")
    // Legacy: DevengoCarteraPropiaLibreYGarantia.java:119 -> Fecha.stringToSqlDate(null)
    void contC44_nullFechaAborts() {
        Map<String, Object> row = datosRow();
        row.put("FechaLiquidacion", null);
        stub(List.of(), List.of(), List.of(row));

        assertThatThrownBy(() -> service.centralizar(entrada("N"))).isInstanceOf(NullPointerException.class);
        verify(repository, never()).persistirCentralizacion(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("CONT-C-45: resultados mirror movements (IdMovimiento=IdCartera, Ejercicio String)")
    // Legacy: DevengoCarteraPropiaLibreYGarantia.java:129 -> BcsCentralizacion.java:243-254
    void contC45_resultados() {
        stub(List.of(cartera().idCartera(55501L).build()), List.of(), List.of());

        SalidaCentralizacion s = service.centralizar(entrada("S"));

        assertThat(s.resultados()).extracting(ResultadoCentralizacion::idMovimiento, ResultadoCentralizacion::ejercicio,
                        ResultadoCentralizacion::idTipoMovimiento, ResultadoCentralizacion::linea)
                .containsExactly(tuple(55501L, "2025", "CARTERARF", 33001L));
    }

    @Test
    @DisplayName("CONT-C-46: S -> no persist; N -> persist once; errores 0, mensajes empty")
    // Legacy: CentralizaMultiSeleccion.java:411-417
    void contC46_simulaVsReal() {
        stub(List.of(cartera().build()), List.of(cartera().build()), List.of(datosRow()));

        service.centralizar(entrada("S"));
        verify(repository, never()).persistirCentralizacion(any(), any(), any(), any(), any(), any(), any(), any(), any());

        SalidaCentralizacion real = service.centralizar(entrada("N"));
        verify(repository, times(1)).persistirCentralizacion(
                eq(TIPO), eq(FECHA_CONTABLE), eq(ID_EMPRESA), eq(2025L), eq("97"),
                eq(real.tabMovimientos()), eq(real.tabEventos()), eq(real.tabDatosMovimiento()), eq(List.of()));
        assertThat(real.erroresContador()).isZero();
        assertThat(real.mensajes()).isEmpty();
    }

    @Test
    @DisplayName("CONT-C-47: repository failure propagates and nothing is persisted")
    // Legacy: DevengoCarteraPropiaLibreYGarantia.java:67 (throws Exception)
    void contC47_errorPropaga() {
        when(repository.queryDatosMovimientoRows(anyString(), any())).thenThrow(new IllegalStateException("db down"));

        assertThatThrownBy(() -> service.centralizar(entrada("N"))).hasMessage("db down");
        verify(repository, never()).persistirCentralizacion(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }
}
