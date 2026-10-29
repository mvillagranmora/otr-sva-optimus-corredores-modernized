package cl.bcs.contabilidad.generico;

import cl.bcs.contabilidad.domain.model.EntradaCentralizacion;
import cl.bcs.contabilidad.domain.model.ResultadoCentralizacion;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabDatoMovimientoRef;
import cl.bcs.contabilidad.domain.port.out.ContabilidadRepository;
import cl.bcs.contabilidad.domain.service.BcsCentralizacionService;
import cl.bcs.contabilidad.domain.service.DevengoATasaCompraCarteraPactada;
import cl.bcs.contabilidad.domain.service.devengo.CarteraValorizada;
import cl.bcs.contabilidad.domain.service.devengo.DevengoCalculator;
import cl.bcs.contabilidad.domain.service.devengo.TipoDevengo;
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
import static cl.bcs.contabilidad.generico.DevengoFixtures.TIPO_TASA_COMPRA_PACTADA;
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
 * Characterization tests for legacy cl.bcs.contabilidad.DevengoATasaCompraCarteraPactada
 * (IdTipoCentralizacion 440, "Pacto a Tasa Compra").
 *
 * Legacy flow (DevengoATasaCompraCarteraPactada.java:64-130): three queries filtered by
 * IdEmpresa + IdInforme, 30 dato fields per datos row, llenarResultado. It has no
 * calculation of its own and never writes; persistence happens later in CentralizaGenerico,
 * which CentralizaMultiSeleccion.java:411 only invokes when p_SimulaOperacion = "N".
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DevengoATasaCompraCarteraPactadaServiceTest {

    static final String SQL_MOV   = "cl/bcs/contabilidad/sql/CentralizaDevengoATasaCompraCarteraPactada_SelectCarteraMovimientos.sql";
    static final String SQL_EV    = "cl/bcs/contabilidad/sql/CentralizaDevengoATasaCompraCarteraPactada_SelectCarteraEventos.sql";
    static final String SQL_DATOS = "cl/bcs/contabilidad/sql/CentralizaDevengoATasaCompraCarteraPactada_SelectDatosMovimiento.sql";

    static final Long TIPO = TIPO_TASA_COMPRA_PACTADA;

    @Mock
    ContabilidadRepository repository;

    DevengoCalculator calculator = new DevengoCalculator();
    DevengoATasaCompraCarteraPactada service;

    @BeforeEach
    void setUp() {
        service = new DevengoATasaCompraCarteraPactada(repository, calculator);
    }

    private static EntradaCentralizacion entrada(String simula) {
        return new EntradaCentralizacion(ID_EMPRESA, 97L, "CON", TIPO, FECHA_CONTABLE, ID_INFORME, 2025L, simula);
    }

    private void stub(List<CarteraValorizada> mov, List<CarteraValorizada> ev, List<Map<String, Object>> datos) {
        when(repository.queryCarteraValorizada(eq(SQL_MOV), eq(ID_EMPRESA), eq(ID_INFORME))).thenReturn(mov);
        when(repository.queryCarteraValorizada(eq(SQL_EV), eq(ID_EMPRESA), eq(ID_INFORME))).thenReturn(ev);
        when(repository.queryDatosMovimientoRows(eq(SQL_DATOS), any())).thenReturn(datos);
    }

    /** One datos row with the SQL aliases of CentralizaDevengoATasaCompraCarteraPactada_SelectDatosMovimiento.sql:3-57. */
    static Map<String, Object> datosRow() {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("IdMovimiento", 70001L);
        r.put("DocumentoIdentidad", "11.111.111-1");
        r.put("NombreCliente", "CLIENTE PRUEBA UNO");
        r.put("Portafolio", "PORT-001");
        r.put("Moneda", "CLP");
        r.put("CompraVenta", "C");
        r.put("Producto", "PACTO");
        r.put("ClaseInstrumento", "IF");
        r.put("TipoInstrumento", "PAC");
        r.put("Nemo", "NEMO-SUBY-1");
        r.put("Boveda", "DCV");
        r.put("IdSubyacente", "33001");
        r.put("Emisor", "EMISOR-X");
        r.put("TipoReajuste", "CLP");
        r.put("CodigoIFRS", "VRC");
        r.put("TipoCartera", "VRC");
        r.put("ClaseEmisor", "BANCO");
        r.put("TipoEmisor", "TE-1");
        r.put("Agente", "AG01");
        r.put("NombreAgente", "AGENTE UNO");
        r.put("SucursalAgente", "SUC01");
        r.put("NombreSucursalAgente", "CASA MATRIZ");
        r.put("CentroCostoAgente", "CC-100");
        r.put("Relacion", "NR");
        r.put("Bolsa", "BCS");
        r.put("TipoPersona", "J");
        r.put("Concepto", "CONC-1");
        r.put("FechaLiquidacion", java.sql.Date.valueOf("2025-03-12"));
        r.put("FechaOperacion", java.sql.Date.valueOf("2025-02-14"));
        r.put("Mesa", "MESA_RENTA_FIJA");
        r.put("FolioOperacion", 88001L);
        r.put("EsVencimiento", "NO");
        r.put("TipoReajusteCabecera", "UF");
        return r;
    }

    @Test
    @DisplayName("CONT-C-20: service is a BcsCentralizacionService and queries by IdEmpresa + IdInforme with the three SQL resources")
    // Legacy: DevengoATasaCompraCarteraPactada.java:71-89 (params p_IdEmpresa, p_IdInforme, p_FechaContable for datos)
    void contC20_queriesAndParameters() {
        stub(List.of(), List.of(), List.of());

        service.centralizar(entrada("S"));

        assertThat(service).isInstanceOf(BcsCentralizacionService.class);
        verify(repository).queryCarteraValorizada(SQL_MOV, ID_EMPRESA, ID_INFORME);
        verify(repository).queryCarteraValorizada(SQL_EV, ID_EMPRESA, ID_INFORME);
        ArgumentCaptor<MapSqlParameterSource> params = ArgumentCaptor.forClass(MapSqlParameterSource.class);
        verify(repository).queryDatosMovimientoRows(eq(SQL_DATOS), params.capture());
        assertThat(params.getValue().getValue("p_IdEmpresa")).isEqualTo(ID_EMPRESA);
        assertThat(params.getValue().getValue("p_IdInforme")).isEqualTo(ID_INFORME);
        assertThat(params.getValue().getValue("p_FechaContable")).isEqualTo(FECHA_CONTABLE);
    }

    @Test
    @DisplayName("CONT-C-21: tabMovimientos / tabEventos are the DevengoCalculator derivation of each row (non-pacto row: movement yes, events no)")
    // Legacy: CentralizaDevengoATasaCompraCarteraPactada_SelectMovimientos.sql (no IdTipoOperacion filter) vs _SelectEventos.sql:19
    void contC21_movimientosYEventosFromCalculator() {
        CarteraValorizada pacto = cartera().idValorizacion(70001L).idTipoOperacion("COMPAC").build();
        CarteraValorizada otro = cartera().idValorizacion(70002L).idTipoOperacion("COMPRA").build();
        stub(List.of(pacto, otro), List.of(pacto, otro), List.of());

        SalidaCentralizacion s = service.centralizar(entrada("S"));

        assertThat(s.tabMovimientos()).containsExactly(
                calculator.movimiento(TipoDevengo.TASA_COMPRA_CARTERA_PACTADA, TIPO, FECHA_CONTABLE, pacto).orElseThrow(),
                calculator.movimiento(TipoDevengo.TASA_COMPRA_CARTERA_PACTADA, TIPO, FECHA_CONTABLE, otro).orElseThrow());
        assertThat(s.tabMovimientos()).extracting(SalidaCentralizacion.TabMovimientoRef::idTipoMovimiento)
                .containsExactly("CARTPACCOM", "??????????");
        assertThat(s.tabEventos())
                .containsExactlyInAnyOrderElementsOf(
                        calculator.eventos(TipoDevengo.TASA_COMPRA_CARTERA_PACTADA, TIPO, pacto));
        assertThat(s.tabEventos()).hasSize(7).allMatch(e -> e.idMovimiento().equals(70001L));
    }

    @Test
    @DisplayName("CONT-C-22: each datos row -> exactly 30 TabDatoMovimiento in legacy order (no IdMovimiento/Portafolio/IdCartera entries)")
    // Legacy: DevengoATasaCompraCarteraPactada.java:94-123
    void contC22_thirtyDatosInOrder() {
        stub(List.of(), List.of(), List.of(datosRow()));

        SalidaCentralizacion s = service.centralizar(entrada("S"));

        assertThat(s.tabDatosMovimiento()).extracting(TabDatoMovimientoRef::tipoDato).containsExactly(
                "DocumentoIdentidad", "NombreCliente", "Moneda", "Producto", "ClaseInstrumento",
                "TipoInstrumento", "Nemo", "Boveda", "Emisor", "TipoReajuste", "CodigoIFRS",
                "TipoCartera", "ClaseEmisor", "Agente", "NombreAgente", "SucursalAgente",
                "NombreSucursalAgente", "CentroCostoAgente", "Relacion", "Bolsa", "TipoPersona",
                "Concepto", "FechaLiquidacion", "FechaOperacion", "TipoEmisor", "CompraVenta",
                "Mesa", "FolioOperacion", "EsVencimiento", "TipoReajusteCabecera");
    }

    @Test
    @DisplayName("CONT-C-23: datos values copied verbatim; dates formatted yyyy-MM-dd; Long via String.valueOf; ValorDecimal 0; keyed by tipo + IdMovimiento")
    // Legacy: DevengoATasaCompraCarteraPactada.java:94-123; Fecha.stringToSqlDate = SimpleDateFormat("yyyy-MM-dd") (BcsUtils Fecha.java:72-76)
    void contC23_datosValues() {
        stub(List.of(), List.of(), List.of(datosRow()));

        SalidaCentralizacion s = service.centralizar(entrada("S"));

        assertThat(s.tabDatosMovimiento()).extracting(TabDatoMovimientoRef::tipoDato, TabDatoMovimientoRef::valorVarChar)
                .containsExactly(
                        tuple("DocumentoIdentidad", "11.111.111-1"),
                        tuple("NombreCliente", "CLIENTE PRUEBA UNO"),
                        tuple("Moneda", "CLP"),
                        tuple("Producto", "PACTO"),
                        tuple("ClaseInstrumento", "IF"),
                        tuple("TipoInstrumento", "PAC"),
                        tuple("Nemo", "NEMO-SUBY-1"),
                        tuple("Boveda", "DCV"),
                        tuple("Emisor", "EMISOR-X"),
                        tuple("TipoReajuste", "CLP"),
                        tuple("CodigoIFRS", "VRC"),
                        tuple("TipoCartera", "VRC"),
                        tuple("ClaseEmisor", "BANCO"),
                        tuple("Agente", "AG01"),
                        tuple("NombreAgente", "AGENTE UNO"),
                        tuple("SucursalAgente", "SUC01"),
                        tuple("NombreSucursalAgente", "CASA MATRIZ"),
                        tuple("CentroCostoAgente", "CC-100"),
                        tuple("Relacion", "NR"),
                        tuple("Bolsa", "BCS"),
                        tuple("TipoPersona", "J"),
                        tuple("Concepto", "CONC-1"),
                        tuple("FechaLiquidacion", "2025-03-12"),
                        tuple("FechaOperacion", "2025-02-14"),
                        tuple("TipoEmisor", "TE-1"),
                        tuple("CompraVenta", "C"),
                        tuple("Mesa", "MESA_RENTA_FIJA"),
                        tuple("FolioOperacion", "88001"),
                        tuple("EsVencimiento", "NO"),
                        tuple("TipoReajusteCabecera", "UF"));
        assertThat(s.tabDatosMovimiento()).allSatisfy(d -> {
            assertThat(d.idTipoCentralizacion()).isEqualTo(TIPO);
            assertThat(d.idMovimiento()).isEqualTo(70001L);
            assertThat(d.valorDecimal()).isEqualByComparingTo(BigDecimal.ZERO);
        });
    }

    @Test
    @DisplayName("CONT-C-24: null String columns -> \"\"; but Relacion and FolioOperacion go through String.valueOf -> literal \"null\"")
    // Legacy: TabDatosMovimiento.java:23 (null -> ""), DevengoATasaCompraCarteraPactada.java:112 (String.valueOf(Relacion)), :121 (String.valueOf(FolioOperacion))
    void contC24_nullHandling() {
        Map<String, Object> row = datosRow();
        row.replaceAll((k, v) -> k.equals("IdMovimiento") || k.startsWith("Fecha") ? v : null);
        stub(List.of(), List.of(), List.of(row));

        SalidaCentralizacion s = service.centralizar(entrada("S"));

        assertThat(s.tabDatosMovimiento())
                .filteredOn(d -> d.tipoDato().equals("Relacion") || d.tipoDato().equals("FolioOperacion"))
                .extracting(TabDatoMovimientoRef::valorVarChar)
                .containsExactly("null", "null");
        assertThat(s.tabDatosMovimiento())
                .filteredOn(d -> !d.tipoDato().equals("Relacion") && !d.tipoDato().equals("FolioOperacion")
                        && !d.tipoDato().startsWith("Fecha"))
                .hasSize(26)
                .allMatch(d -> d.valorVarChar().isEmpty());
    }

    @Test
    @DisplayName("CONT-C-25: null FechaLiquidacion -> NullPointerException (SimpleDateFormat.format(null)); the run aborts")
    // Legacy: DevengoATasaCompraCarteraPactada.java:116 -> Fecha.stringToSqlDate(null) (BcsUtils Fecha.java:75)
    void contC25_nullFechaAborts() {
        Map<String, Object> row = datosRow();
        row.put("FechaLiquidacion", null);
        stub(List.of(), List.of(), List.of(row));

        assertThatThrownBy(() -> service.centralizar(entrada("S"))).isInstanceOf(NullPointerException.class);
        verify(repository, never()).persistirCentralizacion(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("CONT-C-26: resultados = one per movement, Ejercicio as String.valueOf(Long) (null -> \"null\")")
    // Legacy: DevengoATasaCompraCarteraPactada.java:126 -> BcsCentralizacion.java:243-254 (llenarResultado)
    void contC26_resultados() {
        CarteraValorizada conFecha = cartera().idValorizacion(70001L).build();
        CarteraValorizada sinFecha = cartera().idValorizacion(70002L).fechaOperacion(null).build();
        stub(List.of(conFecha, sinFecha), List.of(), List.of());

        SalidaCentralizacion s = service.centralizar(entrada("S"));

        assertThat(s.resultados()).extracting(ResultadoCentralizacion::idMovimiento, ResultadoCentralizacion::ejercicio,
                        ResultadoCentralizacion::idTipoMovimiento, ResultadoCentralizacion::monto)
                .containsExactly(
                        tuple(70001L, "2025", "CARTPACCOM", new BigDecimal("1002500.50")),
                        tuple(70002L, "null", "CARTPACCOM", new BigDecimal("1002500.50")));
    }

    @Test
    @DisplayName("CONT-C-27: simulaOperacion S -> nothing persisted, outputs still populated")
    // Legacy: CentralizaMultiSeleccion.java:411 (CentralizaGenerico only when "N")
    void contC27_simulacionNoPersiste() {
        stub(List.of(cartera().build()), List.of(cartera().build()), List.of(datosRow()));

        SalidaCentralizacion s = service.centralizar(entrada("S"));

        verify(repository, never()).persistirCentralizacion(any(), any(), any(), any(), any(), any(), any(), any(), any());
        assertThat(s.tabMovimientos()).hasSize(1);
        assertThat(s.tabEventos()).hasSize(7);
        assertThat(s.tabDatosMovimiento()).hasSize(30);
        assertThat(s.resultados()).hasSize(1);
    }

    @Test
    @DisplayName("CONT-C-27: simulaOperacion N -> persisted once with exactly the produced tables and CodigoUsuario = String.valueOf(idUsuario)")
    // Legacy: CentralizaMultiSeleccion.java:354-357 (tables), :411-417 (only "N"), :415 (String.valueOf(p_IdUsuario))
    void contC27_realPersiste() {
        stub(List.of(cartera().build()), List.of(cartera().build()), List.of(datosRow()));

        SalidaCentralizacion s = service.centralizar(entrada("N"));

        verify(repository, times(1)).persistirCentralizacion(
                eq(TIPO), eq(FECHA_CONTABLE), eq(ID_EMPRESA), eq(2025L), eq("97"),
                eq(s.tabMovimientos()), eq(s.tabEventos()), eq(s.tabDatosMovimiento()), eq(List.of()));
    }

    @Test
    @DisplayName("CONT-C-27: simulaOperacion null or other than \"N\" -> not persisted (legacy tests \"N\".equals)")
    // Legacy: CentralizaMultiSeleccion.java:411
    void contC27_nullSimulaNoPersiste() {
        stub(List.of(cartera().build()), List.of(), List.of());

        service.centralizar(entrada(null));
        service.centralizar(entrada("n"));

        verify(repository, never()).persistirCentralizacion(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("CONT-C-28: erroresContador 0, mensajes empty, tabDatosEvento empty")
    // Legacy: BcsCentralizacion.java:168,175-182 (defaults), class never touches them
    void contC28_sinErroresNiMensajes() {
        stub(List.of(cartera().build()), List.of(cartera().build()), List.of(datosRow()));

        SalidaCentralizacion s = service.centralizar(entrada("N"));

        assertThat(s.erroresContador()).isZero();
        assertThat(s.mensajes()).isEmpty();
        assertThat(s.tabDatosEvento()).isEmpty();
    }

    @Test
    @DisplayName("CONT-C-29: a repository failure propagates (legacy 'throws Exception'; caught one level up by CentralizaMultiSeleccion)")
    // Legacy: DevengoATasaCompraCarteraPactada.java:64; CentralizaMultiSeleccion.java:361-366
    void contC29_errorPropaga() {
        when(repository.queryDatosMovimientoRows(anyString(), any())).thenThrow(new IllegalStateException("db down"));

        assertThatThrownBy(() -> service.centralizar(entrada("N")))
                .isInstanceOf(IllegalStateException.class).hasMessage("db down");
        verify(repository, never()).persistirCentralizacion(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }
}
