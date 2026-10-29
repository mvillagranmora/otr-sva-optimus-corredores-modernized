package cl.bcs.contabilidad.centraliza;

import cl.bcs.contabilidad.domain.model.EmpresaInfo;
import cl.bcs.contabilidad.domain.model.EntradaCentralizacion;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabDatoMovimientoRef;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabEventoRef;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabMovimientoRef;
import cl.bcs.contabilidad.domain.port.out.ContabilidadRepository;
import cl.bcs.contabilidad.domain.port.out.PlataformaPort;
import cl.bcs.contabilidad.domain.service.CentralizaCorteCuponRF;
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
 * Characterization tests for CentralizaCorteCuponRF (work package WP-E).
 *
 * <p>Oracle: legacy CentralizaCorteCuponRF.java (BSCL/Contabilidad/JAVA/BcsContabilidadServicios/src/cl/bcs/contabilidad/), NOT the
 * modern class. Each test cites the legacy lines it pins. A failing test is a modern-vs-legacy discrepancy.
 *
 * <p>SQL evidence is read from every repository invocation that carries a String argument naming the legacy
 * SQL file (any port method), so the tests do not depend on which port method the implementation uses.
 * All fixture values are synthetic; no legacy credential or real RUT is used.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CentralizaCorteCuponRFTest {

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
    CentralizaCorteCuponRF service;

    @BeforeEach
    void setUp() {
        service = new CentralizaCorteCuponRF(plataforma, repository);
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
        rows("CentralizaCorteCuponRF_SelectMovimientos.sql", List.of(movRow(MOV)));
        rows("CentralizaCorteCuponRF_SelectEventos.sql", List.of(evRow(EV)));
    }

    @Test
    @DisplayName("CONT-E-164: movimientos query runs CentralizaCorteCuponRF_SelectMovimientos.sql with params {p_IdEmpresa, p_FechaContable, p_IdTipoCentralizacion} (legacy CentralizaCorteCuponRF.java:85-89)")
    void contE164_movimientosQuery() {
        // Legacy: CentralizaCorteCuponRF.java:85-89
        try { service.centralizar(entrada("S")); } catch (RuntimeException ignored) { /* query evidence is checked below */ }
        assertThat(paramsOf("CentralizaCorteCuponRF_SelectMovimientos.sql")).isEqualTo(Map.ofEntries(Map.entry("p_IdEmpresa", ID_EMPRESA), Map.entry("p_FechaContable", FECHA), Map.entry("p_IdTipoCentralizacion", ID_TIPO)));
    }

    @Test
    @DisplayName("CONT-E-165: eventos query runs CentralizaCorteCuponRF_SelectEventos.sql with params {p_IdEmpresa, p_FechaContable, p_IdTipoCentralizacion} (legacy CentralizaCorteCuponRF.java:93-96)")
    void contE165_eventosQuery() {
        // Legacy: CentralizaCorteCuponRF.java:93-96
        try { service.centralizar(entrada("S")); } catch (RuntimeException ignored) { /* query evidence is checked below */ }
        assertThat(paramsOf("CentralizaCorteCuponRF_SelectEventos.sql")).isEqualTo(Map.ofEntries(Map.entry("p_IdEmpresa", ID_EMPRESA), Map.entry("p_FechaContable", FECHA), Map.entry("p_IdTipoCentralizacion", ID_TIPO)));
    }

    @Test
    @DisplayName("CONT-E-166: datosMovimiento query runs CentralizaCorteCuponRF_SelectDatosMovimiento.sql with params {p_IdEmpresa, p_FechaContable, LocSistema} (legacy CentralizaCorteCuponRF.java:100-104)")
    void contE166_datosMovimientoQuery() {
        // Legacy: CentralizaCorteCuponRF.java:100-104
        try { service.centralizar(entrada("S")); } catch (RuntimeException ignored) { /* query evidence is checked below */ }
        assertThat(paramsOf("CentralizaCorteCuponRF_SelectDatosMovimiento.sql")).isEqualTo(Map.ofEntries(Map.entry("p_IdEmpresa", ID_EMPRESA), Map.entry("p_FechaContable", FECHA), Map.entry("LocSistema", LOC)));
    }

    @Test
    @DisplayName("CONT-E-167: datos-movimiento: exact field names, order and formatting (39 entries per row, legacy CentralizaCorteCuponRF.java:107-149)")
    void contE167_datosMovimientoFieldsOrderAndFormat() {
        // Legacy: CentralizaCorteCuponRF.java:107-149 ; TabDatosMovimiento ctor (pojo/TabDatosMovimiento.java:19-25)
        rows("CentralizaCorteCuponRF_SelectDatosMovimiento.sql", List.of(fullRow()));
        SalidaCentralizacion salida = service.centralizar(entrada("S"));
        assertThat(salida.tabDatosMovimiento())
                .extracting(TabDatoMovimientoRef::tipoDato, TabDatoMovimientoRef::valorVarChar, TabDatoMovimientoRef::valorDecimal)
                .containsExactly(
                        tuple("IdMovimiento", "7001", BigDecimal.ZERO),
                        tuple("NombreCuentaCorriente", "vNombreCuentaCorriente", BigDecimal.ZERO),
                        tuple("TipoDocumento", "vTipoDocumento", BigDecimal.ZERO),
                        tuple("Folio", "5002", BigDecimal.ZERO),
                        tuple("FechaDocumento", "vFechaDocumento", BigDecimal.ZERO),
                        tuple("NumeroCuentaCorriente", "vNumeroCuentaCorriente", BigDecimal.ZERO),
                        tuple("MedioDePago", "vMedioDePago", BigDecimal.ZERO),
                        tuple("NumeroDocumentoPago", "vNumeroDocumentoPago", BigDecimal.ZERO),
                        tuple("FechaLiquidacion", "2025-02-08", BigDecimal.ZERO),
                        tuple("SerieDocumento", "vSerieDocumento", BigDecimal.ZERO),
                        tuple("Concepto", "vConcepto", BigDecimal.ZERO),
                        tuple("DocumentoIdentidad", "vDocumentoIdentidad", BigDecimal.ZERO),
                        tuple("NombreCliente", "vNombreCliente", BigDecimal.ZERO),
                        tuple("PortafolioCliente", "vPortafolio", BigDecimal.ZERO),
                        tuple("Moneda", "vMoneda", BigDecimal.ZERO),
                        tuple("CodigoCaja", "5014", BigDecimal.ZERO),
                        tuple("EmitidoA", "vEmitidoA", BigDecimal.ZERO),
                        tuple("EsBilateral", "vEsBilateral", BigDecimal.ZERO),
                        tuple("TipoPersona", "vTipoPersona", BigDecimal.ZERO),
                        tuple("Producto", "vProducto", BigDecimal.ZERO),
                        tuple("Relacion", "vRelacion", BigDecimal.ZERO),
                        tuple("Bolsa", "vBolsa", BigDecimal.ZERO),
                        tuple("CentroCostoAgente", "vCentroCostoAgente", BigDecimal.ZERO),
                        tuple("ComprobanteTipo", "vComprobanteTipo", BigDecimal.ZERO),
                        tuple("Nemotecnico", "vNemotecnico", BigDecimal.ZERO),
                        tuple("CostingCode", "vCentroCostoAgente", BigDecimal.ZERO),
                        tuple("CostingCode2", "vProducto", BigDecimal.ZERO),
                        tuple("CostingCode3", "vRelacion", BigDecimal.ZERO),
                        tuple("CostingCode4", "vBolsa", BigDecimal.ZERO),
                        tuple("CostingCode5", "vOpCentros", BigDecimal.ZERO),
                        tuple("TipoEmisor", "vTipoEmisor", BigDecimal.ZERO),
                        tuple("Mesa", "vMesa", BigDecimal.ZERO),
                        tuple("TipoCartera", "vTipoCartera", BigDecimal.ZERO),
                        tuple("Emisor", "vEmisor", BigDecimal.ZERO),
                        tuple("EsCuentaPropia", "vEsCuentaPropia", BigDecimal.ZERO),
                        tuple("ClienteBancario", "vClienteBancario", BigDecimal.ZERO),
                        tuple("IdTipoReajuste", "vIdTipoReajuste", BigDecimal.ZERO),
                        tuple("ClaseEmisor", "vClaseEmisor", BigDecimal.ZERO),
                        tuple("EsCuentaTercero", "vEsCuentaTercero", BigDecimal.ZERO));
        assertIds(salida.tabDatosMovimiento(), ID_MOV);
    }

    @Test
    @DisplayName("CONT-E-168: datos-movimiento null handling: null String -> \"\", String.valueOf(null) -> \"null\", toStringDate(null) -> \"\", null decimal -> 0 (legacy CentralizaCorteCuponRF.java:107-149)")
    void contE168_datosMovimientoNullHandling() {
        // Legacy: CentralizaCorteCuponRF.java:107-149 ; TabDatosMovimiento ctor coalesces ValorVarChar null -> "" and ValorDecimal null -> ZERO,
        // but String.valueOf(null) has already produced the 4-char string "null" before the ctor sees it.
        rows("CentralizaCorteCuponRF_SelectDatosMovimiento.sql", List.of(nullRow()));
        SalidaCentralizacion salida = service.centralizar(entrada("S"));
        assertThat(salida.tabDatosMovimiento())
                .extracting(TabDatoMovimientoRef::tipoDato, TabDatoMovimientoRef::valorVarChar, TabDatoMovimientoRef::valorDecimal)
                .containsExactly(
                        tuple("IdMovimiento", "7001", BigDecimal.ZERO),
                        tuple("NombreCuentaCorriente", "", BigDecimal.ZERO),
                        tuple("TipoDocumento", "", BigDecimal.ZERO),
                        tuple("Folio", "null", BigDecimal.ZERO),
                        tuple("FechaDocumento", "", BigDecimal.ZERO),
                        tuple("NumeroCuentaCorriente", "", BigDecimal.ZERO),
                        tuple("MedioDePago", "", BigDecimal.ZERO),
                        tuple("NumeroDocumentoPago", "", BigDecimal.ZERO),
                        tuple("FechaLiquidacion", "2025-02-08", BigDecimal.ZERO),
                        tuple("SerieDocumento", "", BigDecimal.ZERO),
                        tuple("Concepto", "", BigDecimal.ZERO),
                        tuple("DocumentoIdentidad", "", BigDecimal.ZERO),
                        tuple("NombreCliente", "", BigDecimal.ZERO),
                        tuple("PortafolioCliente", "", BigDecimal.ZERO),
                        tuple("Moneda", "", BigDecimal.ZERO),
                        tuple("CodigoCaja", "null", BigDecimal.ZERO),
                        tuple("EmitidoA", "", BigDecimal.ZERO),
                        tuple("EsBilateral", "", BigDecimal.ZERO),
                        tuple("TipoPersona", "", BigDecimal.ZERO),
                        tuple("Producto", "", BigDecimal.ZERO),
                        tuple("Relacion", "", BigDecimal.ZERO),
                        tuple("Bolsa", "", BigDecimal.ZERO),
                        tuple("CentroCostoAgente", "", BigDecimal.ZERO),
                        tuple("ComprobanteTipo", "", BigDecimal.ZERO),
                        tuple("Nemotecnico", "", BigDecimal.ZERO),
                        tuple("CostingCode", "", BigDecimal.ZERO),
                        tuple("CostingCode2", "", BigDecimal.ZERO),
                        tuple("CostingCode3", "", BigDecimal.ZERO),
                        tuple("CostingCode4", "", BigDecimal.ZERO),
                        tuple("CostingCode5", "", BigDecimal.ZERO),
                        tuple("TipoEmisor", "", BigDecimal.ZERO),
                        tuple("Mesa", "", BigDecimal.ZERO),
                        tuple("TipoCartera", "", BigDecimal.ZERO),
                        tuple("Emisor", "", BigDecimal.ZERO),
                        tuple("EsCuentaPropia", "", BigDecimal.ZERO),
                        tuple("ClienteBancario", "", BigDecimal.ZERO),
                        tuple("IdTipoReajuste", "", BigDecimal.ZERO),
                        tuple("ClaseEmisor", "", BigDecimal.ZERO),
                        tuple("EsCuentaTercero", "", BigDecimal.ZERO));
    }

    @Test
    @DisplayName("CONT-E-169: datos-movimiento: null FechaLiquidacion aborts the centralization (Fecha.stringToSqlDate(null) -> SimpleDateFormat.format(null) throws NPE; legacy CentralizaCorteCuponRF.java:107-149)")
    void contE169_nullFechaLiquidacionAborts() {
        // Legacy: CentralizaCorteCuponRF.java:107-149 field "FechaLiquidacion": Fecha.stringToSqlDate(null) -> SimpleDateFormat.format(null) throws NPE.
        // The exception propagates out of centralizar(); CentralizaMultiSeleccion.java:361-366 catches it and
        // only records the type in the error header. No partial output is produced.
        rows("CentralizaCorteCuponRF_SelectDatosMovimiento.sql", List.of(npeRow()));
        assertThatThrownBy(() -> service.centralizar(entrada("S")))
                .as("legacy throws when FechaLiquidacion is null").isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("CONT-E-170: movimientos/eventos are the query rows unchanged; resultados via llenarResultado (Ejercicio as String); 0 errors, no messages, no datos-evento")
    void contE170_passthroughResultadosAndNoErrors() {
        // Legacy: CentralizaCorteCuponRF.java:89,96,151 (P_TABMOVIMIENTOS / P_TABEVENTOS = query rows); BcsCentralizacion.java:100-111 llenarResultado;
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
    @DisplayName("CONT-E-171: simulaOperacion=S never writes")
    void contE171_simulationNeverWrites() {
        // Legacy: CentralizaMultiSeleccion.java:411 persists (CentralizaGenerico.logica) only when p_SimulaOperacion is "N";
        // CentralizaCorteCuponRF.java itself never writes.
        rows("CentralizaCorteCuponRF_SelectDatosMovimiento.sql", List.of(fullRow()));
        service.centralizar(entrada("S"));
        verify(repository, never()).persistirCentralizacion(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("CONT-E-172: simulaOperacion=N persists exactly the produced tables once (usuario as String.valueOf(idUsuario))")
    void contE172_realRunPersistsProducedTables() {
        // Legacy: CentralizaMultiSeleccion.java:347-357 (salida tables appended to entradaCentralizaGenerico) and
        // :411-417 (p_CodigoUsuario = String.valueOf(p_IdUsuario), p_Ejercicio, p_FechaContable, p_IdEmpresa).
        rows("CentralizaCorteCuponRF_SelectDatosMovimiento.sql", List.of(fullRow()));
        SalidaCentralizacion salida = service.centralizar(entrada("N"));
        verify(repository, times(1)).persistirCentralizacion(eq(ID_TIPO), eq(FECHA), eq(ID_EMPRESA), eq(EJERCICIO), eq("97"),
                eq(salida.tabMovimientos()), eq(salida.tabEventos()), eq(salida.tabDatosMovimiento()), eq(salida.tabDatosEvento()));
    }

    // ------------------------------------------------------------------ fixtures

    /** Row as the legacy TabDatosMovimientoPaso POJO would receive it (one value per column). */
    static Map<String, Object> fullRow() {
        return row("IdMovimiento", ID_MOV,
                "NombreCuentaCorriente", "vNombreCuentaCorriente",
                "TipoDocumento", "vTipoDocumento",
                "Folio", 5002L,
                "FechaDocumento", "vFechaDocumento",
                "NumeroCuentaCorriente", "vNumeroCuentaCorriente",
                "MedioDePago", "vMedioDePago",
                "NumeroDocumentoPago", "vNumeroDocumentoPago",
                "FechaLiquidacion", java.sql.Date.valueOf("2025-02-08"),
                "SerieDocumento", "vSerieDocumento",
                "Concepto", "vConcepto",
                "DocumentoIdentidad", "vDocumentoIdentidad",
                "NombreCliente", "vNombreCliente",
                "Portafolio", "vPortafolio",
                "Moneda", "vMoneda",
                "CodigoCaja", 5014L,
                "EmitidoA", "vEmitidoA",
                "EsBilateral", "vEsBilateral",
                "TipoPersona", "vTipoPersona",
                "Producto", "vProducto",
                "Relacion", "vRelacion",
                "Bolsa", "vBolsa",
                "CentroCostoAgente", "vCentroCostoAgente",
                "ComprobanteTipo", "vComprobanteTipo",
                "Nemotecnico", "vNemotecnico",
                "OpCentros", "vOpCentros",
                "TipoEmisor", "vTipoEmisor",
                "Mesa", "vMesa",
                "TipoCartera", "vTipoCartera",
                "Emisor", "vEmisor",
                "EsCuentaPropia", "vEsCuentaPropia",
                "ClienteBancario", "vClienteBancario",
                "IdTipoReajuste", "vIdTipoReajuste",
                "ClaseEmisor", "vClaseEmisor",
                "EsCuentaTercero", "vEsCuentaTercero");
    }

    /** Same row with every column null except IdMovimiento and the columns whose null makes legacy throw. */
    static Map<String, Object> nullRow() {
        return row("IdMovimiento", ID_MOV,
                "NombreCuentaCorriente", null,
                "TipoDocumento", null,
                "Folio", null,
                "FechaDocumento", null,
                "NumeroCuentaCorriente", null,
                "MedioDePago", null,
                "NumeroDocumentoPago", null,
                "FechaLiquidacion", java.sql.Date.valueOf("2025-02-08"),
                "SerieDocumento", null,
                "Concepto", null,
                "DocumentoIdentidad", null,
                "NombreCliente", null,
                "Portafolio", null,
                "Moneda", null,
                "CodigoCaja", null,
                "EmitidoA", null,
                "EsBilateral", null,
                "TipoPersona", null,
                "Producto", null,
                "Relacion", null,
                "Bolsa", null,
                "CentroCostoAgente", null,
                "ComprobanteTipo", null,
                "Nemotecnico", null,
                "OpCentros", null,
                "TipoEmisor", null,
                "Mesa", null,
                "TipoCartera", null,
                "Emisor", null,
                "EsCuentaPropia", null,
                "ClienteBancario", null,
                "IdTipoReajuste", null,
                "ClaseEmisor", null,
                "EsCuentaTercero", null);
    }

    static Map<String, Object> npeRow() {
        return row("IdMovimiento", ID_MOV,
                "NombreCuentaCorriente", "vNombreCuentaCorriente",
                "TipoDocumento", "vTipoDocumento",
                "Folio", 5002L,
                "FechaDocumento", "vFechaDocumento",
                "NumeroCuentaCorriente", "vNumeroCuentaCorriente",
                "MedioDePago", "vMedioDePago",
                "NumeroDocumentoPago", "vNumeroDocumentoPago",
                "FechaLiquidacion", null,
                "SerieDocumento", "vSerieDocumento",
                "Concepto", "vConcepto",
                "DocumentoIdentidad", "vDocumentoIdentidad",
                "NombreCliente", "vNombreCliente",
                "Portafolio", "vPortafolio",
                "Moneda", "vMoneda",
                "CodigoCaja", 5014L,
                "EmitidoA", "vEmitidoA",
                "EsBilateral", "vEsBilateral",
                "TipoPersona", "vTipoPersona",
                "Producto", "vProducto",
                "Relacion", "vRelacion",
                "Bolsa", "vBolsa",
                "CentroCostoAgente", "vCentroCostoAgente",
                "ComprobanteTipo", "vComprobanteTipo",
                "Nemotecnico", "vNemotecnico",
                "OpCentros", "vOpCentros",
                "TipoEmisor", "vTipoEmisor",
                "Mesa", "vMesa",
                "TipoCartera", "vTipoCartera",
                "Emisor", "vEmisor",
                "EsCuentaPropia", "vEsCuentaPropia",
                "ClienteBancario", "vClienteBancario",
                "IdTipoReajuste", "vIdTipoReajuste",
                "ClaseEmisor", "vClaseEmisor",
                "EsCuentaTercero", "vEsCuentaTercero");
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
