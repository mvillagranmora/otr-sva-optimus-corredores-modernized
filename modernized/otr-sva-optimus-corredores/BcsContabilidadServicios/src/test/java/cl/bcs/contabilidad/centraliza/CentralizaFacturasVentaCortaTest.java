package cl.bcs.contabilidad.centraliza;

import cl.bcs.contabilidad.domain.model.EmpresaInfo;
import cl.bcs.contabilidad.domain.model.EntradaCentralizacion;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabDatoMovimientoRef;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabEventoRef;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabMovimientoRef;
import cl.bcs.contabilidad.domain.port.out.ContabilidadRepository;
import cl.bcs.contabilidad.domain.port.out.PlataformaPort;
import cl.bcs.contabilidad.domain.service.CentralizaFacturasVentaCorta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.invocation.Invocation;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.util.LinkedCaseInsensitiveMap;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Characterization tests for CentralizaFacturasVentaCorta (work package WP-E).
 *
 * <p>Oracle: legacy CentralizaFacturasVentaCorta.java (BSCL/Contabilidad/JAVA/BcsContabilidadServicios/src/cl/bcs/contabilidad/), NOT the
 * modern class. Each test cites the legacy lines it pins. A failing test is a modern-vs-legacy discrepancy.
 *
 * <p>SQL evidence is read from every repository invocation that carries a String argument naming the legacy
 * SQL file (any port method), so the tests do not depend on which port method the implementation uses.
 * All fixture values are synthetic; no legacy credential or real RUT is used.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CentralizaFacturasVentaCortaTest {

    // Fixture constants: synthetic values only (the RUT-shaped strings are fake).
    static final Long   ID_EMPRESA     = 1L;
    static final Long   ID_USUARIO     = 97L;
    static final String ID_MODULO      = "CON";
    static final Long   ID_TIPO        = 406L;
    static final String FECHA          = "2025-03-10";
    static final String FECHA_ANTERIOR = "2025-03-07";
    static final String FECHA_HIST     = "2025-03-11"; // system date different from the accounting date
    static final Long   EJERCICIO      = 2025L;
    static final Long   ID_PAIS        = 56L;
    static final String DOC_CORREDORA  = "76000000-0";
    static final String LOC            = "CL";
    static final String MONEDA_SISTEMA = "CLP";
    static final String CLASIF         = "ACC";
    static final String CFG_DOC        = "99999999-9";
    static final String CFG_NOM        = "CONTRAPARTE DE PRUEBA";
    static final Long   ID_MOV         = 7001L;

    static final TabMovimientoRef MOV = new TabMovimientoRef(ID_TIPO, ID_MOV, "TIPOMOV", ID_EMPRESA, 7001L, 1L,
            FECHA, "11111111-1", 300L, EJERCICIO, "03", new BigDecimal("1500.25"), "CLP", "V");
    static final TabEventoRef EV = new TabEventoRef(ID_TIPO, ID_MOV, "MONTO", "Monto", new BigDecimal("1500.25"), "CLP");
    static final TabEventoRef EV_SUB = new TabEventoRef(ID_TIPO, ID_MOV, "SUBYACENTE", "Evento subyacente", new BigDecimal("10.00"), "CLP");
    /** Returned by a port query the legacy type never runs; it must not leak into the output. */
    static final TabEventoRef EV_EXTRA = new TabEventoRef(ID_TIPO, ID_MOV, "EXTRA_NO_LEGACY", "No existe en legacy", BigDecimal.ONE, "CLP");

    @Mock PlataformaPort plataforma;
    @Mock ContabilidadRepository repository;
    CentralizaFacturasVentaCorta service;

    @BeforeEach
    void setUp() {
        service = new CentralizaFacturasVentaCorta(plataforma, repository);
        when(plataforma.consultarEmpresa(any())).thenReturn(new EmpresaInfo(ID_EMPRESA, ID_PAIS, DOC_CORREDORA, "CORREDORA DE PRUEBA"));
        when(plataforma.obtenerDiaAnterior(any(), any())).thenReturn(FECHA_ANTERIOR);
        when(plataforma.obtenerLocalizacionSistema(any())).thenReturn(LOC);
        when(plataforma.obtenerMonedaSistema(any())).thenReturn(MONEDA_SISTEMA);
        // Unconfigured parameters answer with the default the caller passes (port contract for "absent").
        when(plataforma.obtenerParametroConfiguracion(any(), any(), any(), any())).thenAnswer(inv -> inv.getArgument(3));
        fechaSistema(FECHA);
        when(repository.queryClasificacionTipoInstrumento(any())).thenReturn(CLASIF);
        when(repository.queryMovimientos(any(), any(), any())).thenReturn(List.of(MOV));
        when(repository.queryEventosOperacion(any(), any(), any())).thenReturn(List.of(EV));
        when(repository.queryEventosSubyacente(any(), any(), any(), any())).thenReturn(List.of(EV_EXTRA));
        rows("CentralizaFacturaVentaCorta_SelectMovimientos.sql", List.of(movRow(MOV)));
        rows("CentralizaFacturaVentaCorta_SelectEventos.sql", List.of(evRow(EV)));
        config("PLATAFORMA", "CON.DocumentoIdentidadContraparte", CFG_DOC);
        config("PLATAFORMA", "CON.NombreContraparte", CFG_NOM);
        rows("CentralizaFacturaVentaCorta_SelectMercadoCentralizacion.sql", List.of(row("ClasificacionTipoInstrumento", CLASIF)));
    }

    @Test
    @DisplayName("CONT-E-301: clasificacionTipoInstrumento query runs CentralizaFacturaVentaCorta_SelectMercadoCentralizacion.sql with params {IdTipoCentralizacion} (legacy CentralizaFacturasVentaCorta.java:29,126-128)")
    void contE301_clasificacionTipoInstrumentoQuery() {
        // Legacy: CentralizaFacturasVentaCorta.java:29,126-128
        try { service.centralizar(entrada("S")); } catch (RuntimeException ignored) { /* query evidence is checked below */ }
        assertThat(paramsOf("CentralizaFacturaVentaCorta_SelectMercadoCentralizacion.sql")).isEqualTo(Map.ofEntries(Map.entry("IdTipoCentralizacion", ID_TIPO)));
    }

    @Test
    @DisplayName("CONT-E-302: movimientos query runs CentralizaFacturaVentaCorta_SelectMovimientos.sql with params {FechaContable, IdTipoCentralizacion, DocumentoIdentidad, IdClasificacionTipoInstrumento} (legacy CentralizaFacturasVentaCorta.java:26,131-136)")
    void contE302_movimientosQuery() {
        // Legacy: CentralizaFacturasVentaCorta.java:26,131-136
        try { service.centralizar(entrada("S")); } catch (RuntimeException ignored) { /* query evidence is checked below */ }
        assertThat(paramsOf("CentralizaFacturaVentaCorta_SelectMovimientos.sql")).isEqualTo(Map.ofEntries(Map.entry("FechaContable", FECHA), Map.entry("IdTipoCentralizacion", ID_TIPO), Map.entry("DocumentoIdentidad", DOC_CORREDORA), Map.entry("IdClasificacionTipoInstrumento", CLASIF)));
    }

    @Test
    @DisplayName("CONT-E-303: eventos query runs CentralizaFacturaVentaCorta_SelectEventos.sql with params {FechaContable, IdTipoCentralizacion, DocumentoIdentidad, IdClasificacionTipoInstrumento} (legacy CentralizaFacturasVentaCorta.java:27,131-137)")
    void contE303_eventosQuery() {
        // Legacy: CentralizaFacturasVentaCorta.java:27,131-137
        try { service.centralizar(entrada("S")); } catch (RuntimeException ignored) { /* query evidence is checked below */ }
        assertThat(paramsOf("CentralizaFacturaVentaCorta_SelectEventos.sql")).isEqualTo(Map.ofEntries(Map.entry("FechaContable", FECHA), Map.entry("IdTipoCentralizacion", ID_TIPO), Map.entry("DocumentoIdentidad", DOC_CORREDORA), Map.entry("IdClasificacionTipoInstrumento", CLASIF)));
    }

    @Test
    @DisplayName("CONT-E-304: datosMovimiento query runs CentralizaFacturaVentaCorta_SelectMovimientosDatos.sql with params {FechaContable, DocumentoIdentidad, IdClasificacionTipoInstrumento, IdEmpresa} (legacy CentralizaFacturasVentaCorta.java:28,138-140)")
    void contE304_datosMovimientoQuery() {
        // Legacy: CentralizaFacturasVentaCorta.java:28,138-140
        try { service.centralizar(entrada("S")); } catch (RuntimeException ignored) { /* query evidence is checked below */ }
        assertThat(paramsOf("CentralizaFacturaVentaCorta_SelectMovimientosDatos.sql")).isEqualTo(Map.ofEntries(Map.entry("FechaContable", FECHA), Map.entry("DocumentoIdentidad", DOC_CORREDORA), Map.entry("IdClasificacionTipoInstrumento", CLASIF), Map.entry("IdEmpresa", ID_EMPRESA)));
    }

    @Test
    @DisplayName("CONT-E-305: datos-movimiento: exact field names, order and formatting (40 entries per row, legacy CentralizaFacturasVentaCorta.java:117-120,142-183)")
    void contE305_datosMovimientoFieldsOrderAndFormat() {
        // Legacy: CentralizaFacturasVentaCorta.java:117-120,142-183 ; TabDatosMovimiento ctor (pojo/TabDatosMovimiento.java:19-25)
        rows("CentralizaFacturaVentaCorta_SelectMovimientosDatos.sql", List.of(fullRow()));
        SalidaCentralizacion salida = service.centralizar(entrada("S"));
        assertThat(salida.tabDatosMovimiento())
                .extracting(TabDatoMovimientoRef::tipoDato, TabDatoMovimientoRef::valorVarChar, TabDatoMovimientoRef::valorDecimal)
                .containsExactly(
                        tuple("TipoDocumento", "vTipoDocumento", BigDecimal.ZERO),
                        tuple("FechaDocumento", "vFechaDocumento", BigDecimal.ZERO),
                        tuple("CompraVenta", "vCompraVenta", BigDecimal.ZERO),
                        tuple("Operacion", "vOperacion", BigDecimal.ZERO),
                        tuple("Producto", "vProducto", BigDecimal.ZERO),
                        tuple("ClaseInstrumento", "vClaseInstrumento", BigDecimal.ZERO),
                        tuple("TipoInstrumento", "vTipoInstrumento", BigDecimal.ZERO),
                        tuple("CondicionLiquidacion", "vCondicionLiquidacion", BigDecimal.ZERO),
                        tuple("MonedaComprobante", "vMonedaComprobante", BigDecimal.ZERO),
                        tuple("Bolsa", "vBolsa", BigDecimal.ZERO),
                        tuple("DocumentoIdentidad", "vDocumentoIdentidad", BigDecimal.ZERO),
                        tuple("NombreCliente", "vNombreCliente", BigDecimal.ZERO),
                        tuple("Portafolio", "vPortafolio", BigDecimal.ZERO),
                        tuple("Concepto", "vConcepto", BigDecimal.ZERO),
                        tuple("NombreConcepto", "vNombreConcepto", BigDecimal.ZERO),
                        tuple("Agente", "vAgente", BigDecimal.ZERO),
                        tuple("NombreAgente", "vNombreAgente", BigDecimal.ZERO),
                        tuple("SucursalAgente", "vSucursalAgente", BigDecimal.ZERO),
                        tuple("NombreSucursalAgente", "vNombreSucursalAgente", BigDecimal.ZERO),
                        tuple("FolioPoliza", "5019", BigDecimal.ZERO),
                        tuple("Folio", "5020", BigDecimal.ZERO),
                        tuple("EsExento", "vEsExento", BigDecimal.ZERO),
                        tuple("Moneda", "vMoneda", BigDecimal.ZERO),
                        tuple("MonedaLiquidacion", "vMonedaLiquidacion", BigDecimal.ZERO),
                        tuple("EsContraparte", "vEsContraparte", BigDecimal.ZERO),
                        tuple("Relacion", "vRelacion", BigDecimal.ZERO),
                        tuple("Emisor", "vEmisor", BigDecimal.ZERO),
                        tuple("Nemotecnico", "vNemotecnico", BigDecimal.ZERO),
                        tuple("TipoPersona", "vTipoPersona", BigDecimal.ZERO),
                        tuple("CodigoTipoComprobante", "vCodigoTipoComprobante", BigDecimal.ZERO),
                        tuple("FechaLiquidacion", "2025-02-04", BigDecimal.ZERO),
                        tuple("FechaComprobante", "2025-03-10 14:35:07", BigDecimal.ZERO),
                        tuple("EstadoComprobante", "vEstadoComprobante", BigDecimal.ZERO),
                        tuple("EsOtrosConceptos", "vEsOtrosConceptos", BigDecimal.ZERO),
                        tuple("EsAPlazo", "vEsAPlazo", BigDecimal.ZERO),
                        tuple("EsVentaCorta", "vEsVentaCorta", BigDecimal.ZERO),
                        tuple("EsArbitraje", "vEsArbitraje", BigDecimal.ZERO),
                        tuple("DocumentoIdentidadContraparte", CFG_DOC, BigDecimal.ZERO),
                        tuple("NombreContraparte", CFG_NOM, BigDecimal.ZERO),
                        tuple("EstadoSII", "vEstadoSII", BigDecimal.ZERO));
        assertIds(salida.tabDatosMovimiento(), ID_MOV);
    }

    @Test
    @DisplayName("CONT-E-306: datos-movimiento null handling: null String -> \"\", String.valueOf(null) -> \"null\", toStringDate(null) -> \"\", null decimal -> 0 (legacy CentralizaFacturasVentaCorta.java:117-120,142-183)")
    void contE306_datosMovimientoNullHandling() {
        // Legacy: CentralizaFacturasVentaCorta.java:117-120,142-183 ; TabDatosMovimiento ctor coalesces ValorVarChar null -> "" and ValorDecimal null -> ZERO,
        // but String.valueOf(null) has already produced the 4-char string "null" before the ctor sees it.
        rows("CentralizaFacturaVentaCorta_SelectMovimientosDatos.sql", List.of(nullRow()));
        SalidaCentralizacion salida = service.centralizar(entrada("S"));
        assertThat(salida.tabDatosMovimiento())
                .extracting(TabDatoMovimientoRef::tipoDato, TabDatoMovimientoRef::valorVarChar, TabDatoMovimientoRef::valorDecimal)
                .containsExactly(
                        tuple("TipoDocumento", "", BigDecimal.ZERO),
                        tuple("FechaDocumento", "", BigDecimal.ZERO),
                        tuple("CompraVenta", "", BigDecimal.ZERO),
                        tuple("Operacion", "", BigDecimal.ZERO),
                        tuple("Producto", "", BigDecimal.ZERO),
                        tuple("ClaseInstrumento", "", BigDecimal.ZERO),
                        tuple("TipoInstrumento", "", BigDecimal.ZERO),
                        tuple("CondicionLiquidacion", "", BigDecimal.ZERO),
                        tuple("MonedaComprobante", "", BigDecimal.ZERO),
                        tuple("Bolsa", "", BigDecimal.ZERO),
                        tuple("DocumentoIdentidad", "", BigDecimal.ZERO),
                        tuple("NombreCliente", "", BigDecimal.ZERO),
                        tuple("Portafolio", "", BigDecimal.ZERO),
                        tuple("Concepto", "", BigDecimal.ZERO),
                        tuple("NombreConcepto", "", BigDecimal.ZERO),
                        tuple("Agente", "", BigDecimal.ZERO),
                        tuple("NombreAgente", "", BigDecimal.ZERO),
                        tuple("SucursalAgente", "", BigDecimal.ZERO),
                        tuple("NombreSucursalAgente", "", BigDecimal.ZERO),
                        tuple("FolioPoliza", "null", BigDecimal.ZERO),
                        tuple("Folio", "null", BigDecimal.ZERO),
                        tuple("EsExento", "", BigDecimal.ZERO),
                        tuple("Moneda", "", BigDecimal.ZERO),
                        tuple("MonedaLiquidacion", "", BigDecimal.ZERO),
                        tuple("EsContraparte", "", BigDecimal.ZERO),
                        tuple("Relacion", "", BigDecimal.ZERO),
                        tuple("Emisor", "", BigDecimal.ZERO),
                        tuple("Nemotecnico", "", BigDecimal.ZERO),
                        tuple("TipoPersona", "", BigDecimal.ZERO),
                        tuple("CodigoTipoComprobante", "", BigDecimal.ZERO),
                        tuple("FechaLiquidacion", "2025-02-04", BigDecimal.ZERO),
                        tuple("FechaComprobante", "2025-03-10 14:35:07", BigDecimal.ZERO),
                        tuple("EstadoComprobante", "", BigDecimal.ZERO),
                        tuple("EsOtrosConceptos", "", BigDecimal.ZERO),
                        tuple("EsAPlazo", "", BigDecimal.ZERO),
                        tuple("EsVentaCorta", "", BigDecimal.ZERO),
                        tuple("EsArbitraje", "", BigDecimal.ZERO),
                        tuple("DocumentoIdentidadContraparte", CFG_DOC, BigDecimal.ZERO),
                        tuple("NombreContraparte", CFG_NOM, BigDecimal.ZERO),
                        tuple("EstadoSII", "", BigDecimal.ZERO));
    }

    @Test
    @DisplayName("CONT-E-307: datos-movimiento: null FechaLiquidacion aborts the centralization (Fecha.stringToSqlDate(null) -> SimpleDateFormat.format(null) throws NPE; legacy CentralizaFacturasVentaCorta.java:117-120,142-183)")
    void contE307_nullFechaLiquidacionAborts() {
        // Legacy: CentralizaFacturasVentaCorta.java:117-120,142-183 field "FechaLiquidacion": Fecha.stringToSqlDate(null) -> SimpleDateFormat.format(null) throws NPE.
        // The exception propagates out of centralizar(); CentralizaMultiSeleccion.java:361-366 catches it and
        // only records the type in the error header. No partial output is produced.
        rows("CentralizaFacturaVentaCorta_SelectMovimientosDatos.sql", List.of(npeRow()));
        assertThatThrownBy(() -> service.centralizar(entrada("S")))
                .as("legacy throws when FechaLiquidacion is null").isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("CONT-E-308: movimientos/eventos are the query rows unchanged; resultados via llenarResultado (Ejercicio as String); 0 errors, no messages, no datos-evento")
    void contE308_passthroughResultadosAndNoErrors() {
        // Legacy: CentralizaFacturasVentaCorta.java:136,137,185 (P_TABMOVIMIENTOS / P_TABEVENTOS = query rows); BcsCentralizacion.java:100-111 llenarResultado;
        // BcsCentralizacion.java:25-39 SalidaCentralizacion(): P_OUT_ERRORESCONTADOR = 0, all lists empty.
        SalidaCentralizacion salida = service.centralizar(entrada("S"));
        assertThat(salida.tabMovimientos()).containsExactly(MOV);
        assertThat(salida.tabEventos()).containsExactlyElementsOf(List.of(EV));
        assertThat(salida.resultados()).singleElement().satisfies(r -> {
            assertThat(r.ejercicio()).isEqualTo("2025");
            assertThat(r.idMovimiento()).isEqualTo(ID_MOV);
            assertThat(r.idTipoMovimiento()).isEqualTo("TIPOMOV");
            assertThat(r.monto()).isEqualTo(new BigDecimal("1500.25"));
            assertThat(r.periodo()).isEqualTo("03");
            assertThat(r.estado()).isEqualTo("V");
        });
        assertThat(salida.erroresContador()).isZero();
        assertThat(salida.mensajes()).isEmpty();
        assertThat(salida.tabDatosEvento()).isEmpty();
    }

    @Test
    @DisplayName("CONT-E-309: simulaOperacion=S never writes")
    void contE309_simulationNeverWrites() {
        // Legacy: CentralizaMultiSeleccion.java:411 persists (CentralizaGenerico.logica) only when p_SimulaOperacion is "N";
        // CentralizaFacturasVentaCorta.java itself never writes.
        rows("CentralizaFacturaVentaCorta_SelectMovimientosDatos.sql", List.of(fullRow()));
        service.centralizar(entrada("S"));
        verify(repository, never()).persistirCentralizacion(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("CONT-E-310: simulaOperacion=N persists exactly the produced tables once (usuario as String.valueOf(idUsuario))")
    void contE310_realRunPersistsProducedTables() {
        // Legacy: CentralizaMultiSeleccion.java:347-357 (salida tables appended to entradaCentralizaGenerico) and
        // :411-417 (p_CodigoUsuario = String.valueOf(p_IdUsuario), p_Ejercicio, p_FechaContable, p_IdEmpresa).
        rows("CentralizaFacturaVentaCorta_SelectMovimientosDatos.sql", List.of(fullRow()));
        SalidaCentralizacion salida = service.centralizar(entrada("N"));
        verify(repository, times(1)).persistirCentralizacion(eq(ID_TIPO), eq(FECHA), eq(ID_EMPRESA), eq(EJERCICIO), eq("97"),
                eq(salida.tabMovimientos()), eq(salida.tabEventos()), eq(salida.tabDatosMovimiento()), eq(salida.tabDatosEvento()));
    }

    @Test
    @DisplayName("CONT-E-311: CON.DocumentoIdentidadContraparte / CON.NombreContraparte absent -> both datos values empty, not an error")
    void contE311_contraparteConfigDefaultsToEmpty() {
        // Legacy: CentralizaFacturasVentaCorta.java:114-124,180-181 (getParametroConfiguracionString in try/catch, both start as "")
        config("PLATAFORMA", "CON.DocumentoIdentidadContraparte", "");
        config("PLATAFORMA", "CON.NombreContraparte", "");
        rows("CentralizaFacturaVentaCorta_SelectMovimientosDatos.sql", List.of(fullRow()));
        SalidaCentralizacion salida = service.centralizar(entrada("S"));
        verify(plataforma, org.mockito.Mockito.atLeastOnce()).obtenerParametroConfiguracion(eq(ID_EMPRESA), eq("PLATAFORMA"), eq("CON.DocumentoIdentidadContraparte"), eq(""));
        verify(plataforma, org.mockito.Mockito.atLeastOnce()).obtenerParametroConfiguracion(eq(ID_EMPRESA), eq("PLATAFORMA"), eq("CON.NombreContraparte"), eq(""));
        assertThat(salida.tabDatosMovimiento())
                .filteredOn(d -> d.tipoDato().equals("DocumentoIdentidadContraparte") || d.tipoDato().equals("NombreContraparte"))
                .extracting(TabDatoMovimientoRef::valorVarChar).containsExactly("", "");
        assertThat(salida.erroresContador()).isZero();
    }

    // ------------------------------------------------------------------ fixtures

    /** Row as the legacy TabDatosMovimientoPaso POJO would receive it (one value per column). */
    static Map<String, Object> fullRow() {
        return row("IdMovimiento", ID_MOV,
                "TipoDocumento", "vTipoDocumento",
                "FechaDocumento", "vFechaDocumento",
                "CompraVenta", "vCompraVenta",
                "Operacion", "vOperacion",
                "Producto", "vProducto",
                "ClaseInstrumento", "vClaseInstrumento",
                "TipoInstrumento", "vTipoInstrumento",
                "CondicionLiquidacion", "vCondicionLiquidacion",
                "MonedaComprobante", "vMonedaComprobante",
                "Bolsa", "vBolsa",
                "DocumentoIdentidad", "vDocumentoIdentidad",
                "NombreCliente", "vNombreCliente",
                "Portafolio", "vPortafolio",
                "Concepto", "vConcepto",
                "NombreConcepto", "vNombreConcepto",
                "Agente", "vAgente",
                "NombreAgente", "vNombreAgente",
                "SucursalAgente", "vSucursalAgente",
                "NombreSucursalAgente", "vNombreSucursalAgente",
                "FolioPoliza", 5019L,
                "Folio", 5020L,
                "EsExento", "vEsExento",
                "Moneda", "vMoneda",
                "MonedaLiquidacion", "vMonedaLiquidacion",
                "EsContraparte", "vEsContraparte",
                "Relacion", "vRelacion",
                "Emisor", "vEmisor",
                "Nemotecnico", "vNemotecnico",
                "TipoPersona", "vTipoPersona",
                "CodigoTipoComprobante", "vCodigoTipoComprobante",
                "FechaLiquidacion", java.sql.Date.valueOf("2025-02-04"),
                "FechaComprobante", java.sql.Timestamp.valueOf("2025-03-10 14:35:07.123"),
                "EstadoComprobante", "vEstadoComprobante",
                "EsOtrosConceptos", "vEsOtrosConceptos",
                "EsAPlazo", "vEsAPlazo",
                "EsVentaCorta", "vEsVentaCorta",
                "EsArbitraje", "vEsArbitraje",
                "EstadoSII", "vEstadoSII");
    }

    /** Same row with every column null except IdMovimiento and the columns whose null makes legacy throw. */
    static Map<String, Object> nullRow() {
        return row("IdMovimiento", ID_MOV,
                "TipoDocumento", null,
                "FechaDocumento", null,
                "CompraVenta", null,
                "Operacion", null,
                "Producto", null,
                "ClaseInstrumento", null,
                "TipoInstrumento", null,
                "CondicionLiquidacion", null,
                "MonedaComprobante", null,
                "Bolsa", null,
                "DocumentoIdentidad", null,
                "NombreCliente", null,
                "Portafolio", null,
                "Concepto", null,
                "NombreConcepto", null,
                "Agente", null,
                "NombreAgente", null,
                "SucursalAgente", null,
                "NombreSucursalAgente", null,
                "FolioPoliza", null,
                "Folio", null,
                "EsExento", null,
                "Moneda", null,
                "MonedaLiquidacion", null,
                "EsContraparte", null,
                "Relacion", null,
                "Emisor", null,
                "Nemotecnico", null,
                "TipoPersona", null,
                "CodigoTipoComprobante", null,
                "FechaLiquidacion", java.sql.Date.valueOf("2025-02-04"),
                "FechaComprobante", java.sql.Timestamp.valueOf("2025-03-10 14:35:07.123"),
                "EstadoComprobante", null,
                "EsOtrosConceptos", null,
                "EsAPlazo", null,
                "EsVentaCorta", null,
                "EsArbitraje", null,
                "EstadoSII", null);
    }

    static Map<String, Object> npeRow() {
        return row("IdMovimiento", ID_MOV,
                "TipoDocumento", "vTipoDocumento",
                "FechaDocumento", "vFechaDocumento",
                "CompraVenta", "vCompraVenta",
                "Operacion", "vOperacion",
                "Producto", "vProducto",
                "ClaseInstrumento", "vClaseInstrumento",
                "TipoInstrumento", "vTipoInstrumento",
                "CondicionLiquidacion", "vCondicionLiquidacion",
                "MonedaComprobante", "vMonedaComprobante",
                "Bolsa", "vBolsa",
                "DocumentoIdentidad", "vDocumentoIdentidad",
                "NombreCliente", "vNombreCliente",
                "Portafolio", "vPortafolio",
                "Concepto", "vConcepto",
                "NombreConcepto", "vNombreConcepto",
                "Agente", "vAgente",
                "NombreAgente", "vNombreAgente",
                "SucursalAgente", "vSucursalAgente",
                "NombreSucursalAgente", "vNombreSucursalAgente",
                "FolioPoliza", 5019L,
                "Folio", 5020L,
                "EsExento", "vEsExento",
                "Moneda", "vMoneda",
                "MonedaLiquidacion", "vMonedaLiquidacion",
                "EsContraparte", "vEsContraparte",
                "Relacion", "vRelacion",
                "Emisor", "vEmisor",
                "Nemotecnico", "vNemotecnico",
                "TipoPersona", "vTipoPersona",
                "CodigoTipoComprobante", "vCodigoTipoComprobante",
                "FechaLiquidacion", null,
                "FechaComprobante", java.sql.Timestamp.valueOf("2025-03-10 14:35:07.123"),
                "EstadoComprobante", "vEstadoComprobante",
                "EsOtrosConceptos", "vEsOtrosConceptos",
                "EsAPlazo", "vEsAPlazo",
                "EsVentaCorta", "vEsVentaCorta",
                "EsArbitraje", "vEsArbitraje",
                "EstadoSII", "vEstadoSII");
    }

    // ------------------------------------------------------------------ helpers

    EntradaCentralizacion entrada(String simula) {
        return new EntradaCentralizacion(ID_EMPRESA, ID_USUARIO, ID_MODULO, ID_TIPO, FECHA, null, EJERCICIO, simula);
    }

    /** Legacy reads PLA.FechaSistema (MultiSeleccion infoAdicional or ConfiguracionSistema); both port routes agree. */
    void fechaSistema(String f) {
        when(plataforma.obtenerFechaSistema(any())).thenReturn(f);
        when(plataforma.obtenerParametroConfiguracion(any(), eq("PLATAFORMA"), eq("PLA.FechaSistema"), any())).thenReturn(f);
    }

    void localizacion(String loc) {
        when(plataforma.obtenerLocalizacionSistema(any())).thenReturn(loc);
    }

    void config(String modulo, String parametro, String valor) {
        when(plataforma.obtenerParametroConfiguracion(any(), eq(modulo), eq(parametro), any())).thenReturn(valor);
    }

    /** MOV as the row its legacy movimientos SQL returns (columns = pojo/TabMovimientos.java fields). */
    static Map<String, Object> movRow(TabMovimientoRef m) {
        return row("IdTipoCentralizacion", m.idTipoCentralizacion(), "IdMovimiento", m.idMovimiento(),
                "IdTipoMovimiento", m.idTipoMovimiento(), "IdEmpresa", m.idEmpresa(), "Numero", m.numero(),
                "Linea", m.linea(), "Fecha", m.fecha(), "DocumentoIdentidad", m.documentoIdentidad(),
                "IdPortafolio", m.idPortafolio(), "Ejercicio", m.ejercicio(), "Periodo", m.periodo(),
                "Monto", m.monto(), "IdMoneda", m.idMoneda(), "Estado", m.estado());
    }

    /** An event as the row its legacy eventos SQL returns (columns = pojo/TabEventos.java fields). */
    static Map<String, Object> evRow(TabEventoRef e) {
        return row("IdTipoCentralizacion", e.idTipoCentralizacion(), "IdMovimiento", e.idMovimiento(),
                "IdEvento", e.idEvento(), "Nombre", e.nombre(), "MontoEvento", e.montoEvento(),
                "CodigoMoneda", e.codigoMoneda());
    }

    void rows(String sqlFile, List<Map<String, Object>> rows) {
        when(repository.queryDatosMovimientoRows(argThat(n -> isSql(n, sqlFile)), any())).thenReturn(rows);
    }

    static boolean isSql(Object n, String f) {
        return n instanceof String s && (s.equals(f) || s.endsWith("/" + f));
    }

    /** Case-insensitive like the maps JdbcTemplate.queryForList returns. */
    static Map<String, Object> row(Object... kv) {
        Map<String, Object> m = new LinkedCaseInsensitiveMap<>();
        for (int i = 0; i < kv.length; i += 2) m.put((String) kv[i], kv[i + 1]);
        return m;
    }

    List<String> issuedSql() {
        List<String> out = new ArrayList<>();
        for (Invocation inv : mockingDetails(repository).getInvocations())
            for (Object a : inv.getArguments())
                if (a instanceof String s && s.endsWith(".sql")) out.add(s);
        return out;
    }

    /** Parameters of the single repository invocation that named the given SQL file; fails if 0 or more than 1. */
    @SuppressWarnings("unchecked")
    Map<String, Object> paramsOf(String sqlFile) {
        List<Map<String, Object>> found = new ArrayList<>();
        for (Invocation inv : mockingDetails(repository).getInvocations()) {
            Object[] args = inv.getArguments();
            if (Arrays.stream(args).noneMatch(a -> isSql(a, sqlFile))) continue;
            Map<String, Object> p = new HashMap<>();
            for (Object a : args) {
                if (a instanceof MapSqlParameterSource ps) p = new HashMap<>(ps.getValues());
                else if (a instanceof Map<?, ?> mp) p = new HashMap<>((Map<String, Object>) mp);
            }
            found.add(p);
        }
        assertThat(found)
                .as("legacy runs %s exactly once; modern issued only these SQL resources: %s", sqlFile, issuedSql())
                .hasSize(1);
        return found.get(0);
    }

    void assertNotIssued(String sqlFile) {
        assertThat(issuedSql()).as("legacy does not run %s on this path", sqlFile).noneMatch(n -> isSql(n, sqlFile));
    }

    static void assertIds(List<TabDatoMovimientoRef> datos, Long idMov) {
        assertThat(datos).isNotEmpty().allSatisfy(d -> {
            assertThat(d.idTipoCentralizacion()).isEqualTo(ID_TIPO);
            assertThat(d.idMovimiento()).isEqualTo(idMov);
        });
    }
}
