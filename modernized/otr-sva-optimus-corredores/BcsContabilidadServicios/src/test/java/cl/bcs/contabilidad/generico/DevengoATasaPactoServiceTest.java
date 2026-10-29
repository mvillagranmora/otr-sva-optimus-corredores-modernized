package cl.bcs.contabilidad.generico;

import cl.bcs.contabilidad.domain.model.EntradaCentralizacion;
import cl.bcs.contabilidad.domain.model.ResultadoCentralizacion;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabDatoMovimientoRef;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabEventoRef;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabMovimientoRef;
import cl.bcs.contabilidad.domain.port.out.ContabilidadRepository;
import cl.bcs.contabilidad.domain.service.BcsCentralizacionService;
import cl.bcs.contabilidad.domain.service.DevengoATasaPacto;
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
import static cl.bcs.contabilidad.generico.DevengoFixtures.TIPO_TASA_PACTO;
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
 * Characterization tests for legacy cl.bcs.contabilidad.DevengoATasaPacto
 * (IdTipoCentralizacion 430, "Pacto a Tasa Pacto").
 *
 * Legacy flow: DevengoATasaPacto.java:65-131. 29 dato fields per row
 * (DevengoATasaPacto.java:96-124) — note Portafolio, Boveda, Bolsa and Mesa are
 * read by the SQL but NOT emitted; ClienteBancario is emitted only by this class.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DevengoATasaPactoServiceTest {

    static final String SQL_MOV   = "cl/bcs/contabilidad/sql/CentralizaDevengoATasaPacto_SelectCarteraMovimientos.sql";
    static final String SQL_EV    = "cl/bcs/contabilidad/sql/CentralizaDevengoATasaPacto_SelectCarteraEventos.sql";
    static final String SQL_DATOS = "cl/bcs/contabilidad/sql/CentralizaDevengoATasaPacto_SelectDatosMovimiento.sql";

    static final Long TIPO = TIPO_TASA_PACTO;

    @Mock
    ContabilidadRepository repository;

    DevengoATasaPacto service;

    @BeforeEach
    void setUp() {
        service = new DevengoATasaPacto(repository, new DevengoCalculator());
    }

    private static EntradaCentralizacion entrada(String simula) {
        return new EntradaCentralizacion(ID_EMPRESA, 97L, "CON", TIPO, FECHA_CONTABLE, ID_INFORME, 2025L, simula);
    }

    private void stub(List<CarteraValorizada> mov, List<CarteraValorizada> ev, List<Map<String, Object>> datos) {
        when(repository.queryCarteraValorizada(eq(SQL_MOV), eq(ID_EMPRESA), eq(ID_INFORME))).thenReturn(mov);
        when(repository.queryCarteraValorizada(eq(SQL_EV), eq(ID_EMPRESA), eq(ID_INFORME))).thenReturn(ev);
        when(repository.queryDatosMovimientoRows(eq(SQL_DATOS), any())).thenReturn(datos);
    }

    /** SQL aliases of CentralizaDevengoATasaPacto_SelectDatosMovimiento.sql:2-48. */
    static Map<String, Object> datosRow() {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("IdMovimiento", 70001L);
        r.put("DocumentoIdentidad", "22.222.222-2");
        r.put("CompraVenta", "V");
        r.put("NombreCliente", "CLIENTE PRUEBA DOS");
        r.put("Portafolio", "PORT-002");
        r.put("Moneda", "UF");
        r.put("Producto", "PACTO");
        r.put("ClaseInstrumento", "IF");
        r.put("TipoInstrumento", "PAC");
        r.put("Nemo", "NEMO-2");
        r.put("Boveda", "DCV");
        r.put("IdCartera", 0L); // COALESCE(IdCartera,0)
        r.put("Emisor", "EMISOR-Y");
        r.put("TipoReajuste", "UF");
        r.put("CodigoIFRS", "VRC-CRV");
        r.put("TipoCartera", "VRC-CRV");
        r.put("ClaseEmisor", "EMPRESA");
        r.put("Agente", "AG02");
        r.put("NombreAgente", "AGENTE DOS");
        r.put("SucursalAgente", "SUC02");
        r.put("NombreSucursalAgente", "SUCURSAL NORTE");
        r.put("CentroCostoAgente", "CC-200");
        r.put("Relacion", "RE");
        r.put("Bolsa", "BCS");
        r.put("FechaLiquidacion", java.sql.Date.valueOf("2024-12-31"));
        r.put("FechaOperacion", java.sql.Date.valueOf("2024-12-30"));
        r.put("Concepto", "CONC-2");
        r.put("TipoPersona", "N");
        r.put("Mesa", "MESA_RENTA_FIJA");
        r.put("FolioOperacion", 88002L);
        r.put("ClienteBancario", "NO BANCARIO");
        r.put("EsVencimiento", "SI");
        r.put("TipoReajusteCabecera", "UF");
        return r;
    }

    @Test
    @DisplayName("CONT-C-30: queries by IdEmpresa + IdInforme; datos params p_IdEmpresa, p_IdInforme, p_FechaContable")
    // Legacy: DevengoATasaPacto.java:74-93
    void contC30_queries() {
        stub(List.of(), List.of(), List.of());

        service.centralizar(entrada("S"));

        assertThat(service).isInstanceOf(BcsCentralizacionService.class);
        verify(repository).queryCarteraValorizada(SQL_MOV, ID_EMPRESA, ID_INFORME);
        verify(repository).queryCarteraValorizada(SQL_EV, ID_EMPRESA, ID_INFORME);
        ArgumentCaptor<MapSqlParameterSource> p = ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(repository).queryDatosMovimientoRows(eq(SQL_DATOS), p.capture());
        assertThat(p.getValue().getValue("p_IdEmpresa")).isEqualTo(ID_EMPRESA);
        assertThat(p.getValue().getValue("p_IdInforme")).isEqualTo(ID_INFORME);
        assertThat(p.getValue().getValue("p_FechaContable")).isEqualTo(FECHA_CONTABLE);
    }

    @Test
    @DisplayName("CONT-C-31: movements only for COMPAC/VENPAC (PACTOCOMP / PACTOVTA); events unconditional *_TP_PACTO_*")
    // Legacy: CentralizaDevengoATasaPacto_SelectMovimientos.sql:3-7,26; _SelectEventos.sql:19-33
    void contC31_movimientosYEventos() {
        CarteraValorizada venta = cartera().idValorizacion(70001L).idTipoOperacion("VENPAC").montoInteres("-3.5").build();
        CarteraValorizada noPacto = cartera().idValorizacion(70002L).idTipoOperacion("SIM").build();
        stub(List.of(venta, noPacto), List.of(venta, noPacto), List.of());

        SalidaCentralizacion s = service.centralizar(entrada("S"));

        assertThat(s.tabMovimientos()).extracting(TabMovimientoRef::idMovimiento, TabMovimientoRef::idTipoMovimiento,
                        TabMovimientoRef::numero, TabMovimientoRef::linea)
                .containsExactly(tuple(70001L, "PACTOVTA", 88001L, 0L));
        assertThat(s.tabEventos()).hasSize(7).allMatch(e -> e.idMovimiento().equals(70001L));
        assertThat(s.tabEventos()).extracting(TabEventoRef::idEvento, TabEventoRef::montoEvento)
                .contains(tuple("INTERES_DIARIO_TP_PACTO_VENTA", new BigDecimal("-4")));
    }

    @Test
    @DisplayName("CONT-C-32: each datos row -> exactly 29 TabDatoMovimiento in legacy order (no Portafolio/Boveda/Bolsa/Mesa)")
    // Legacy: DevengoATasaPacto.java:96-124
    void contC32_datosOrder() {
        stub(List.of(), List.of(), List.of(datosRow()));

        SalidaCentralizacion s = service.centralizar(entrada("S"));

        assertThat(s.tabDatosMovimiento()).extracting(TabDatoMovimientoRef::tipoDato, TabDatoMovimientoRef::valorVarChar)
                .containsExactly(
                        tuple("IdMovimiento", "70001"),
                        tuple("DocumentoIdentidad", "22.222.222-2"),
                        tuple("NombreCliente", "CLIENTE PRUEBA DOS"),
                        tuple("Moneda", "UF"),
                        tuple("Producto", "PACTO"),
                        tuple("ClaseInstrumento", "IF"),
                        tuple("TipoInstrumento", "PAC"),
                        tuple("Nemo", "NEMO-2"),
                        tuple("IdCartera", "0"),
                        tuple("Emisor", "EMISOR-Y"),
                        tuple("TipoReajuste", "UF"),
                        tuple("CodigoIFRS", "VRC-CRV"),
                        tuple("TipoCartera", "VRC-CRV"),
                        tuple("ClaseEmisor", "EMPRESA"),
                        tuple("Agente", "AG02"),
                        tuple("NombreAgente", "AGENTE DOS"),
                        tuple("SucursalAgente", "SUC02"),
                        tuple("NombreSucursalAgente", "SUCURSAL NORTE"),
                        tuple("CentroCostoAgente", "CC-200"),
                        tuple("Relacion", "RE"),
                        tuple("TipoPersona", "N"),
                        tuple("Concepto", "CONC-2"),
                        tuple("FechaLiquidacion", "2024-12-31"),
                        tuple("FechaOperacion", "2024-12-30"),
                        tuple("CompraVenta", "V"),
                        tuple("FolioOperacion", "88002"),
                        tuple("ClienteBancario", "NO BANCARIO"),
                        tuple("EsVencimiento", "SI"),
                        tuple("TipoReajusteCabecera", "UF"));
        assertThat(s.tabDatosMovimiento()).allSatisfy(d -> {
            assertThat(d.idTipoCentralizacion()).isEqualTo(TIPO);
            assertThat(d.idMovimiento()).isEqualTo(70001L);
            assertThat(d.valorDecimal()).isEqualByComparingTo(BigDecimal.ZERO);
        });
    }

    @Test
    @DisplayName("CONT-C-33: String.valueOf fields (IdMovimiento, IdCartera, Relacion, FolioOperacion) null -> \"null\"; other nulls -> \"\"")
    // Legacy: DevengoATasaPacto.java:96,104,115,121 (String.valueOf); TabDatosMovimiento.java:23
    void contC33_nullHandling() {
        Map<String, Object> row = datosRow();
        row.replaceAll((k, v) -> k.startsWith("Fecha") ? v : null);
        stub(List.of(), List.of(), List.of(row));

        SalidaCentralizacion s = service.centralizar(entrada("S"));

        List<String> valueOf = List.of("IdMovimiento", "IdCartera", "Relacion", "FolioOperacion");
        assertThat(s.tabDatosMovimiento()).filteredOn(d -> valueOf.contains(d.tipoDato()))
                .extracting(TabDatoMovimientoRef::valorVarChar).containsExactly("null", "null", "null", "null");
        assertThat(s.tabDatosMovimiento())
                .filteredOn(d -> !valueOf.contains(d.tipoDato()) && !d.tipoDato().startsWith("Fecha"))
                .hasSize(23).allMatch(d -> d.valorVarChar().isEmpty());
    }

    @Test
    @DisplayName("CONT-C-34: null FechaOperacion -> NullPointerException")
    // Legacy: DevengoATasaPacto.java:119 -> Fecha.stringToSqlDate(null)
    void contC34_nullFechaAborts() {
        Map<String, Object> row = datosRow();
        row.put("FechaOperacion", null);
        stub(List.of(), List.of(), List.of(row));

        assertThatThrownBy(() -> service.centralizar(entrada("N"))).isInstanceOf(NullPointerException.class);
        verify(repository, never()).persistirCentralizacion(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("CONT-C-35: resultados mirror movements with Ejercicio as String")
    // Legacy: DevengoATasaPacto.java:127 -> BcsCentralizacion.java:243-254
    void contC35_resultados() {
        stub(List.of(cartera().fechaOperacion(java.time.LocalDate.of(2024, 12, 31)).build()), List.of(), List.of());

        SalidaCentralizacion s = service.centralizar(entrada("S"));

        assertThat(s.resultados()).extracting(ResultadoCentralizacion::ejercicio, ResultadoCentralizacion::periodo,
                        ResultadoCentralizacion::idTipoMovimiento)
                .containsExactly(tuple("2024", "12-2024", "PACTOCOMP"));
    }

    @Test
    @DisplayName("CONT-C-36: S -> no persist; N -> persist once with produced tables; errores 0, mensajes empty")
    // Legacy: CentralizaMultiSeleccion.java:411-417; BcsCentralizacion.java:168
    void contC36_simulaVsReal() {
        stub(List.of(cartera().build()), List.of(cartera().build()), List.of(datosRow()));

        SalidaCentralizacion sim = service.centralizar(entrada("S"));
        verify(repository, never()).persistirCentralizacion(any(), any(), any(), any(), any(), any(), any(), any(), any());
        assertThat(sim.tabDatosMovimiento()).hasSize(29);

        SalidaCentralizacion real = service.centralizar(entrada("N"));
        verify(repository, times(1)).persistirCentralizacion(
                eq(TIPO), eq(FECHA_CONTABLE), eq(ID_EMPRESA), eq(2025L), eq("97"),
                eq(real.tabMovimientos()), eq(real.tabEventos()), eq(real.tabDatosMovimiento()), eq(List.of()));
        assertThat(real.erroresContador()).isZero();
        assertThat(real.mensajes()).isEmpty();
    }

    @Test
    @DisplayName("CONT-C-37: repository failure propagates and nothing is persisted")
    // Legacy: DevengoATasaPacto.java:65 (throws Exception)
    void contC37_errorPropaga() {
        when(repository.queryDatosMovimientoRows(anyString(), any())).thenThrow(new IllegalStateException("db down"));

        assertThatThrownBy(() -> service.centralizar(entrada("N"))).hasMessage("db down");
        verify(repository, never()).persistirCentralizacion(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }
}
