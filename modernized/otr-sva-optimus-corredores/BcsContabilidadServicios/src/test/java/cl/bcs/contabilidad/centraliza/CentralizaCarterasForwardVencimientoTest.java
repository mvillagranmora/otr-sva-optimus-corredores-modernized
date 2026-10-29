package cl.bcs.contabilidad.centraliza;

import cl.bcs.contabilidad.domain.model.EmpresaInfo;
import cl.bcs.contabilidad.domain.model.EntradaCentralizacion;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabDatoMovimientoRef;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabEventoRef;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabMovimientoRef;
import cl.bcs.contabilidad.domain.port.out.ContabilidadRepository;
import cl.bcs.contabilidad.domain.port.out.PlataformaPort;
import cl.bcs.contabilidad.domain.service.CentralizaCarterasForwardVencimiento;
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
 * Characterization tests for CentralizaCarterasForwardVencimiento (work package WP-E).
 *
 * <p>Oracle: legacy CentralizaCarterasForwardVencimiento.java (BSCL/Contabilidad/JAVA/BcsContabilidadServicios/src/cl/bcs/contabilidad/), NOT the
 * modern class. Each test cites the legacy lines it pins. A failing test is a modern-vs-legacy discrepancy.
 *
 * <p>SQL evidence is read from every repository invocation that carries a String argument naming the legacy
 * SQL file (any port method), so the tests do not depend on which port method the implementation uses.
 * All fixture values are synthetic; no legacy credential or real RUT is used.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CentralizaCarterasForwardVencimientoTest {

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
    CentralizaCarterasForwardVencimiento service;

    @BeforeEach
    void setUp() {
        service = new CentralizaCarterasForwardVencimiento(plataforma, repository);
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
        rows("CentralizaCarterasForwardVencimiento_SelectMovimientos.sql", List.of(movRow(MOV)));
        rows("CentralizaCarterasForwardVencimiento_SelectEventos.sql", List.of(evRow(EV)));
    }

    @Test
    @DisplayName("CONT-E-098: movimientos query runs CentralizaCarterasForwardVencimiento_SelectMovimientos.sql with params {IdEmpresa, IdTipoCentralizacion, FechaContable} (legacy CentralizaCarterasForwardVencimiento.java:102-107)")
    void contE098_movimientosQuery() {
        // Legacy: CentralizaCarterasForwardVencimiento.java:102-107
        try { service.centralizar(entrada("S")); } catch (RuntimeException ignored) { /* query evidence is checked below */ }
        assertThat(paramsOf("CentralizaCarterasForwardVencimiento_SelectMovimientos.sql")).isEqualTo(Map.ofEntries(Map.entry("IdEmpresa", ID_EMPRESA), Map.entry("IdTipoCentralizacion", ID_TIPO), Map.entry("FechaContable", FECHA)));
    }

    @Test
    @DisplayName("CONT-E-099: eventos query runs CentralizaCarterasForwardVencimiento_SelectEventos.sql with params {IdEmpresa, IdTipoCentralizacion, FechaContable} (legacy CentralizaCarterasForwardVencimiento.java:110-113)")
    void contE099_eventosQuery() {
        // Legacy: CentralizaCarterasForwardVencimiento.java:110-113
        try { service.centralizar(entrada("S")); } catch (RuntimeException ignored) { /* query evidence is checked below */ }
        assertThat(paramsOf("CentralizaCarterasForwardVencimiento_SelectEventos.sql")).isEqualTo(Map.ofEntries(Map.entry("IdEmpresa", ID_EMPRESA), Map.entry("IdTipoCentralizacion", ID_TIPO), Map.entry("FechaContable", FECHA)));
    }

    @Test
    @DisplayName("CONT-E-100: montoEventos query runs CentralizaCarterasForwardVencimiento_SelectMontoEventos.sql with params {IdEmpresa, FechaContable} (legacy CentralizaCarterasForwardVencimiento.java:116-118)")
    void contE100_montoEventosQuery() {
        // Legacy: CentralizaCarterasForwardVencimiento.java:116-118
        try { service.centralizar(entrada("S")); } catch (RuntimeException ignored) { /* query evidence is checked below */ }
        assertThat(paramsOf("CentralizaCarterasForwardVencimiento_SelectMontoEventos.sql")).isEqualTo(Map.ofEntries(Map.entry("IdEmpresa", ID_EMPRESA), Map.entry("FechaContable", FECHA)));
    }

    @Test
    @DisplayName("CONT-E-101: montoEventosHis query runs CentralizaCarterasForwardVencimiento_SelectMontoEventosHis.sql with params {IdEmpresa, FechaContable, FechaAnteriorSistema} (legacy CentralizaCarterasForwardVencimiento.java:89-100,121-124)")
    void contE101_montoEventosHisQuery() {
        // Legacy: CentralizaCarterasForwardVencimiento.java:89-100,121-124
        try { service.centralizar(entrada("S")); } catch (RuntimeException ignored) { /* query evidence is checked below */ }
        assertThat(paramsOf("CentralizaCarterasForwardVencimiento_SelectMontoEventosHis.sql")).isEqualTo(Map.ofEntries(Map.entry("IdEmpresa", ID_EMPRESA), Map.entry("FechaContable", FECHA), Map.entry("FechaAnteriorSistema", FECHA_ANTERIOR)));
    }

    @Test
    @DisplayName("CONT-E-102: datosMovimiento query runs CentralizaCarterasForwardVencimiento_SelectDatosMovimiento.sql with params {IdEmpresa, FechaContable} (legacy CentralizaCarterasForwardVencimiento.java:127-129)")
    void contE102_datosMovimientoQuery() {
        // Legacy: CentralizaCarterasForwardVencimiento.java:127-129
        try { service.centralizar(entrada("S")); } catch (RuntimeException ignored) { /* query evidence is checked below */ }
        assertThat(paramsOf("CentralizaCarterasForwardVencimiento_SelectDatosMovimiento.sql")).isEqualTo(Map.ofEntries(Map.entry("IdEmpresa", ID_EMPRESA), Map.entry("FechaContable", FECHA)));
    }

    @Test
    @DisplayName("CONT-E-103: datos-movimiento: exact field names, order and formatting (29 entries per row, legacy CentralizaCarterasForwardVencimiento.java:153-185)")
    void contE103_datosMovimientoFieldsOrderAndFormat() {
        // Legacy: CentralizaCarterasForwardVencimiento.java:153-185 ; TabDatosMovimiento ctor (pojo/TabDatosMovimiento.java:19-25)
        rows("CentralizaCarterasForwardVencimiento_SelectDatosMovimiento.sql", List.of(fullRow()));
        SalidaCentralizacion salida = service.centralizar(entrada("S"));
        assertThat(salida.tabDatosMovimiento())
                .extracting(TabDatoMovimientoRef::tipoDato, TabDatoMovimientoRef::valorVarChar, TabDatoMovimientoRef::valorDecimal)
                .containsExactly(
                        tuple("DocumentoIdentidad", "vDocumentoIdentidad", BigDecimal.ZERO),
                        tuple("NombreCliente", "vNombreCliente", BigDecimal.ZERO),
                        tuple("PortafolioCliente", "vPortafolio", BigDecimal.ZERO),
                        tuple("MonedaPrincipal", "vMonedaPrincipal", BigDecimal.ZERO),
                        tuple("MonedaSecundaria", "vMonedaSecundaria", BigDecimal.ZERO),
                        tuple("Modalidad", "vModalidaPago", BigDecimal.ZERO),
                        tuple("Producto", "vProducto", BigDecimal.ZERO),
                        tuple("ClaseInstrumento", "vClaseInstrumento", BigDecimal.ZERO),
                        tuple("TipoInstrumento", "vTipoInstrumento", BigDecimal.ZERO),
                        tuple("TipoOperacion", "vTipoOperacion", BigDecimal.ZERO),
                        tuple("Nemo", "vNemo", BigDecimal.ZERO),
                        tuple("Agente", "vAgente", BigDecimal.ZERO),
                        tuple("NombreAgente", "vNombreAgente", BigDecimal.ZERO),
                        tuple("SucursalAgente", "vSucursalAgente", BigDecimal.ZERO),
                        tuple("NombreSucursalAgente", "vNombreSucursalAgente", BigDecimal.ZERO),
                        tuple("CentroCostoAgente", "vCentroCostoAgente", BigDecimal.ZERO),
                        tuple("Relacion", "vRelacion", BigDecimal.ZERO),
                        tuple("Bolsa", "vBolsa", BigDecimal.ZERO),
                        tuple("TasaCambio", "30.3400", BigDecimal.ZERO),
                        tuple("TipoPersona", "vTipoPersona", BigDecimal.ZERO),
                        tuple("Concepto", "vConcepto", BigDecimal.ZERO),
                        tuple("FechaLiquidacion", "2025-02-22", BigDecimal.ZERO),
                        tuple("FechaOperacion", "2025-02-23", BigDecimal.ZERO),
                        tuple("CompraVenta", "vCompraVenta", BigDecimal.ZERO),
                        tuple("CostingCode", "vCentroCostoAgente", BigDecimal.ZERO),
                        tuple("CostingCode2", "vProducto", BigDecimal.ZERO),
                        tuple("CostingCode3", "vRelacion", BigDecimal.ZERO),
                        tuple("CostingCode4", "vBolsa", BigDecimal.ZERO),
                        tuple("CostingCode5", "", BigDecimal.ZERO));
        assertIds(salida.tabDatosMovimiento(), ID_MOV);
    }

    @Test
    @DisplayName("CONT-E-104: datos-movimiento null handling: null String -> \"\", String.valueOf(null) -> \"null\", toStringDate(null) -> \"\", null decimal -> 0 (legacy CentralizaCarterasForwardVencimiento.java:153-185)")
    void contE104_datosMovimientoNullHandling() {
        // Legacy: CentralizaCarterasForwardVencimiento.java:153-185 ; TabDatosMovimiento ctor coalesces ValorVarChar null -> "" and ValorDecimal null -> ZERO,
        // but String.valueOf(null) has already produced the 4-char string "null" before the ctor sees it.
        rows("CentralizaCarterasForwardVencimiento_SelectDatosMovimiento.sql", List.of(nullRow()));
        SalidaCentralizacion salida = service.centralizar(entrada("S"));
        assertThat(salida.tabDatosMovimiento())
                .extracting(TabDatoMovimientoRef::tipoDato, TabDatoMovimientoRef::valorVarChar, TabDatoMovimientoRef::valorDecimal)
                .containsExactly(
                        tuple("DocumentoIdentidad", "", BigDecimal.ZERO),
                        tuple("NombreCliente", "", BigDecimal.ZERO),
                        tuple("PortafolioCliente", "", BigDecimal.ZERO),
                        tuple("MonedaPrincipal", "", BigDecimal.ZERO),
                        tuple("MonedaSecundaria", "", BigDecimal.ZERO),
                        tuple("Modalidad", "null", BigDecimal.ZERO),
                        tuple("Producto", "", BigDecimal.ZERO),
                        tuple("ClaseInstrumento", "", BigDecimal.ZERO),
                        tuple("TipoInstrumento", "", BigDecimal.ZERO),
                        tuple("TipoOperacion", "", BigDecimal.ZERO),
                        tuple("Nemo", "", BigDecimal.ZERO),
                        tuple("Agente", "", BigDecimal.ZERO),
                        tuple("NombreAgente", "", BigDecimal.ZERO),
                        tuple("SucursalAgente", "", BigDecimal.ZERO),
                        tuple("NombreSucursalAgente", "", BigDecimal.ZERO),
                        tuple("CentroCostoAgente", "", BigDecimal.ZERO),
                        tuple("Relacion", "", BigDecimal.ZERO),
                        tuple("Bolsa", "", BigDecimal.ZERO),
                        tuple("TasaCambio", "30.3400", BigDecimal.ZERO),
                        tuple("TipoPersona", "", BigDecimal.ZERO),
                        tuple("Concepto", "", BigDecimal.ZERO),
                        tuple("FechaLiquidacion", "", BigDecimal.ZERO),
                        tuple("FechaOperacion", "", BigDecimal.ZERO),
                        tuple("CompraVenta", "", BigDecimal.ZERO),
                        tuple("CostingCode", "", BigDecimal.ZERO),
                        tuple("CostingCode2", "", BigDecimal.ZERO),
                        tuple("CostingCode3", "", BigDecimal.ZERO),
                        tuple("CostingCode4", "", BigDecimal.ZERO),
                        tuple("CostingCode5", "", BigDecimal.ZERO));
    }

    @Test
    @DisplayName("CONT-E-105: datos-movimiento: null TasaCambio aborts the centralization (null.toPlainString() throws NPE; legacy CentralizaCarterasForwardVencimiento.java:153-185)")
    void contE105_nullTasaCambioAborts() {
        // Legacy: CentralizaCarterasForwardVencimiento.java:153-185 field "TasaCambio": null.toPlainString() throws NPE.
        // The exception propagates out of centralizar(); CentralizaMultiSeleccion.java:361-366 catches it and
        // only records the type in the error header. No partial output is produced.
        rows("CentralizaCarterasForwardVencimiento_SelectDatosMovimiento.sql", List.of(npeRow()));
        assertThatThrownBy(() -> service.centralizar(entrada("S")))
                .as("legacy throws when TasaCambio is null").isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("CONT-E-106: movimientos/eventos are the query rows unchanged; resultados via llenarResultado (Ejercicio as String); 0 errors, no messages, no datos-evento")
    void contE106_passthroughResultadosAndNoErrors() {
        // Legacy: CentralizaCarterasForwardVencimiento.java:107,113,186 (P_TABMOVIMIENTOS / P_TABEVENTOS = query rows); BcsCentralizacion.java:100-111 llenarResultado;
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
    @DisplayName("CONT-E-107: simulaOperacion=S never writes")
    void contE107_simulationNeverWrites() {
        // Legacy: CentralizaMultiSeleccion.java:411 persists (CentralizaGenerico.logica) only when p_SimulaOperacion is "N";
        // CentralizaCarterasForwardVencimiento.java itself never writes.
        rows("CentralizaCarterasForwardVencimiento_SelectDatosMovimiento.sql", List.of(fullRow()));
        service.centralizar(entrada("S"));
        verify(repository, never()).persistirCentralizacion(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("CONT-E-108: simulaOperacion=N persists exactly the produced tables once (usuario as String.valueOf(idUsuario))")
    void contE108_realRunPersistsProducedTables() {
        // Legacy: CentralizaMultiSeleccion.java:347-357 (salida tables appended to entradaCentralizaGenerico) and
        // :411-417 (p_CodigoUsuario = String.valueOf(p_IdUsuario), p_Ejercicio, p_FechaContable, p_IdEmpresa).
        rows("CentralizaCarterasForwardVencimiento_SelectDatosMovimiento.sql", List.of(fullRow()));
        SalidaCentralizacion salida = service.centralizar(entrada("N"));
        verify(repository, times(1)).persistirCentralizacion(eq(ID_TIPO), eq(FECHA), eq(ID_EMPRESA), eq(EJERCICIO), eq("97"),
                eq(salida.tabMovimientos()), eq(salida.tabEventos()), eq(salida.tabDatosMovimiento()), eq(salida.tabDatosEvento()));
    }

    @Test
    @DisplayName("CONT-E-109: VALOR_RAZONABLE_* events appended after the eventos rows: neto<0 PERDIDA |neto|, neto>0 UTILIDAD |neto|, neto=0 neither; *_ANTERIOR from the His rows; moneda = MonedaSistema, IdMovimiento = IdCartera")
    void contE109_valorRazonableEventsFromMontoRows() {
        // Legacy: CentralizaCarterasForwardVencimiento.java:131-151
        rows("CentralizaCarterasForwardVencimiento_SelectMontoEventos.sql", List.of(
                row("IdCartera", 11L, "IdMonedaPrincipal", "USD", "RazonableActivo", new BigDecimal("100.50"), "RazonablePasivo", new BigDecimal("160.50"), "RazonableNeto", new BigDecimal("-60.00")),
                row("IdCartera", 12L, "IdMonedaPrincipal", "USD", "RazonableActivo", new BigDecimal("50.10"), "RazonablePasivo", new BigDecimal("25.00"), "RazonableNeto", new BigDecimal("25.10")),
                row("IdCartera", 13L, "IdMonedaPrincipal", "USD", "RazonableActivo", new BigDecimal("10.00"), "RazonablePasivo", new BigDecimal("10.00"), "RazonableNeto", new BigDecimal("0.00"))));
        rows("CentralizaCarterasForwardVencimiento_SelectMontoEventosHis.sql", List.of(
                row("IdCartera", 21L, "IdMonedaPrincipal", "USD", "RazonableActivo", new BigDecimal("1.00"), "RazonablePasivo", new BigDecimal("6.50"), "RazonableNeto", new BigDecimal("-5.50")),
                row("IdCartera", 22L, "IdMonedaPrincipal", "USD", "RazonableActivo", new BigDecimal("9.00"), "RazonablePasivo", new BigDecimal("2.00"), "RazonableNeto", new BigDecimal("7.00"))));
        SalidaCentralizacion salida = service.centralizar(entrada("S"));
        assertThat(salida.tabEventos()).containsExactly(
                EV,
                new TabEventoRef(ID_TIPO, 11L, "VALOR_RAZONABLE_ACTIVO", "Valor a Razonable Activo", new BigDecimal("100.50"), MONEDA_SISTEMA),
                new TabEventoRef(ID_TIPO, 11L, "VALOR_RAZONABLE_PASIVO", "Valor a Razonable Pasivo", new BigDecimal("160.50"), MONEDA_SISTEMA),
                new TabEventoRef(ID_TIPO, 11L, "VALOR_RAZONABLE_PERDIDA", "Valor a Razonable Perdida", new BigDecimal("60.00"), MONEDA_SISTEMA),
                new TabEventoRef(ID_TIPO, 12L, "VALOR_RAZONABLE_ACTIVO", "Valor a Razonable Activo", new BigDecimal("50.10"), MONEDA_SISTEMA),
                new TabEventoRef(ID_TIPO, 12L, "VALOR_RAZONABLE_PASIVO", "Valor a Razonable Pasivo", new BigDecimal("25.00"), MONEDA_SISTEMA),
                new TabEventoRef(ID_TIPO, 12L, "VALOR_RAZONABLE_UTILIDAD", "Valor a Razonable Utilidad", new BigDecimal("25.10"), MONEDA_SISTEMA),
                new TabEventoRef(ID_TIPO, 13L, "VALOR_RAZONABLE_ACTIVO", "Valor a Razonable Activo", new BigDecimal("10.00"), MONEDA_SISTEMA),
                new TabEventoRef(ID_TIPO, 13L, "VALOR_RAZONABLE_PASIVO", "Valor a Razonable Pasivo", new BigDecimal("10.00"), MONEDA_SISTEMA),
                new TabEventoRef(ID_TIPO, 21L, "VALOR_RAZONABLE_ACTIVO_ANTERIOR", "Valor a Razonable Activo Anterior", new BigDecimal("1.00"), MONEDA_SISTEMA),
                new TabEventoRef(ID_TIPO, 21L, "VALOR_RAZONABLE_PASIVO_ANTERIOR", "Valor a Razonable Pasivo Anterior", new BigDecimal("6.50"), MONEDA_SISTEMA),
                new TabEventoRef(ID_TIPO, 21L, "VALOR_RAZONABLE_PERDIDA_ANTERIOR", "Valor a Razonable Perdida Anterior", new BigDecimal("5.50"), MONEDA_SISTEMA),
                new TabEventoRef(ID_TIPO, 22L, "VALOR_RAZONABLE_ACTIVO_ANTERIOR", "Valor a Razonable Activo Anterior", new BigDecimal("9.00"), MONEDA_SISTEMA),
                new TabEventoRef(ID_TIPO, 22L, "VALOR_RAZONABLE_PASIVO_ANTERIOR", "Valor a Razonable Pasivo Anterior", new BigDecimal("2.00"), MONEDA_SISTEMA),
                new TabEventoRef(ID_TIPO, 22L, "VALOR_RAZONABLE_UTILIDAD_ANTERIOR", "Valor a Razonable Utilidad Anterior", new BigDecimal("7.00"), MONEDA_SISTEMA));
    }
    @Test
    @DisplayName("CONT-E-110: FechaAnteriorSistema = ObtenerDiaAnterior(idPais of the company, fechaContable, 1 business day)")
    void contE110_fechaAnteriorFromCompanyCountry() {
        // Legacy: CentralizaCarterasForwardVencimiento.java:86-100,123 (inObtenerDiaAnterior.idPais = empresa.IdPais, numeroDias = 1, diaHabil = "S")
        when(plataforma.consultarEmpresa(any())).thenReturn(new EmpresaInfo(ID_EMPRESA, 51L, DOC_CORREDORA, "CORREDORA PE"));
        when(plataforma.obtenerDiaAnterior(eq(51L), eq(FECHA))).thenReturn("2025-03-06");
        service.centralizar(entrada("S"));
        verify(plataforma).obtenerDiaAnterior(51L, FECHA);
        assertThat(paramsOf("CentralizaCarterasForwardVencimiento_SelectMontoEventosHis.sql")).containsEntry("FechaAnteriorSistema", "2025-03-06");
    }

    // ------------------------------------------------------------------ fixtures

    /** Row as the legacy TabDatosMovimientoPaso POJO would receive it (one value per column). */
    static Map<String, Object> fullRow() {
        return row("IdMovimiento", ID_MOV,
                "DocumentoIdentidad", "vDocumentoIdentidad",
                "NombreCliente", "vNombreCliente",
                "Portafolio", "vPortafolio",
                "MonedaPrincipal", "vMonedaPrincipal",
                "MonedaSecundaria", "vMonedaSecundaria",
                "ModalidaPago", "vModalidaPago",
                "Producto", "vProducto",
                "ClaseInstrumento", "vClaseInstrumento",
                "TipoInstrumento", "vTipoInstrumento",
                "TipoOperacion", "vTipoOperacion",
                "Nemo", "vNemo",
                "Agente", "vAgente",
                "NombreAgente", "vNombreAgente",
                "SucursalAgente", "vSucursalAgente",
                "NombreSucursalAgente", "vNombreSucursalAgente",
                "CentroCostoAgente", "vCentroCostoAgente",
                "Relacion", "vRelacion",
                "Bolsa", "vBolsa",
                "TasaCambio", new BigDecimal("30.3400"),
                "TipoPersona", "vTipoPersona",
                "Concepto", "vConcepto",
                "FechaLiquidacion", java.sql.Date.valueOf("2025-02-22"),
                "FechaOperacion", java.sql.Date.valueOf("2025-02-23"),
                "CompraVenta", "vCompraVenta");
    }

    /** Same row with every column null except IdMovimiento and the columns whose null makes legacy throw. */
    static Map<String, Object> nullRow() {
        return row("IdMovimiento", ID_MOV,
                "DocumentoIdentidad", null,
                "NombreCliente", null,
                "Portafolio", null,
                "MonedaPrincipal", null,
                "MonedaSecundaria", null,
                "ModalidaPago", null,
                "Producto", null,
                "ClaseInstrumento", null,
                "TipoInstrumento", null,
                "TipoOperacion", null,
                "Nemo", null,
                "Agente", null,
                "NombreAgente", null,
                "SucursalAgente", null,
                "NombreSucursalAgente", null,
                "CentroCostoAgente", null,
                "Relacion", null,
                "Bolsa", null,
                "TasaCambio", new BigDecimal("30.3400"),
                "TipoPersona", null,
                "Concepto", null,
                "FechaLiquidacion", null,
                "FechaOperacion", null,
                "CompraVenta", null);
    }

    static Map<String, Object> npeRow() {
        return row("IdMovimiento", ID_MOV,
                "DocumentoIdentidad", "vDocumentoIdentidad",
                "NombreCliente", "vNombreCliente",
                "Portafolio", "vPortafolio",
                "MonedaPrincipal", "vMonedaPrincipal",
                "MonedaSecundaria", "vMonedaSecundaria",
                "ModalidaPago", "vModalidaPago",
                "Producto", "vProducto",
                "ClaseInstrumento", "vClaseInstrumento",
                "TipoInstrumento", "vTipoInstrumento",
                "TipoOperacion", "vTipoOperacion",
                "Nemo", "vNemo",
                "Agente", "vAgente",
                "NombreAgente", "vNombreAgente",
                "SucursalAgente", "vSucursalAgente",
                "NombreSucursalAgente", "vNombreSucursalAgente",
                "CentroCostoAgente", "vCentroCostoAgente",
                "Relacion", "vRelacion",
                "Bolsa", "vBolsa",
                "TasaCambio", null,
                "TipoPersona", "vTipoPersona",
                "Concepto", "vConcepto",
                "FechaLiquidacion", java.sql.Date.valueOf("2025-02-22"),
                "FechaOperacion", java.sql.Date.valueOf("2025-02-23"),
                "CompraVenta", "vCompraVenta");
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
