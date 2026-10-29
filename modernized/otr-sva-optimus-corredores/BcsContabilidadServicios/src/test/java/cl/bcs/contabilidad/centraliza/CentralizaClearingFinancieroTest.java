package cl.bcs.contabilidad.centraliza;

import cl.bcs.contabilidad.domain.model.EmpresaInfo;
import cl.bcs.contabilidad.domain.model.EntradaCentralizacion;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabDatoMovimientoRef;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabEventoRef;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabMovimientoRef;
import cl.bcs.contabilidad.domain.port.out.ContabilidadRepository;
import cl.bcs.contabilidad.domain.port.out.PlataformaPort;
import cl.bcs.contabilidad.domain.service.CentralizaClearingFinanciero;
import cl.bcs.contabilidad.domain.port.out.PlataformaPort.ClearingFinancieroDetalle;
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
 * Characterization tests for CentralizaClearingFinanciero (work package WP-E).
 *
 * <p>Oracle: legacy CentralizaClearingFinanciero.java (BSCL/Contabilidad/JAVA/BcsContabilidadServicios/src/cl/bcs/contabilidad/), NOT the
 * modern class. Each test cites the legacy lines it pins. A failing test is a modern-vs-legacy discrepancy.
 *
 * <p>SQL evidence is read from every repository invocation that carries a String argument naming the legacy
 * SQL file (any port method), so the tests do not depend on which port method the implementation uses.
 * All fixture values are synthetic; no legacy credential or real RUT is used.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CentralizaClearingFinancieroTest {

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
    CentralizaClearingFinanciero service;

    @BeforeEach
    void setUp() {
        service = new CentralizaClearingFinanciero(plataforma, repository);
        when(plataforma.consultarEmpresa(any())).thenReturn(new EmpresaInfo(ID_EMPRESA, ID_PAIS, DOC_CORREDORA, "CORREDORA DE PRUEBA"));
        when(plataforma.obtenerDiaAnterior(any(), any())).thenReturn(FECHA_ANTERIOR);
        when(plataforma.obtenerLocalizacionSistema(any())).thenReturn(LOC);
        when(plataforma.obtenerMonedaSistema(any())).thenReturn(MONEDA_SISTEMA);
        // Unconfigured parameters answer with the default the caller passes (port contract for "absent").
        when(plataforma.obtenerParametroConfiguracion(any(), any(), any(), any())).thenAnswer(inv -> inv.getArgument(3));
        fechaSistema(FECHA);
        when(repository.queryClasificacionTipoInstrumento(any())).thenReturn(CLASIF);
        when(repository.queryEventosOperacion(any(), any(), any())).thenReturn(List.of(EV));
        when(repository.queryEventosSubyacente(any(), any(), any(), any())).thenReturn(List.of(EV_EXTRA));
    }

    @Test
    @DisplayName("CONT-E-141: simulaOperacion=S never writes")
    void contE141_simulationNeverWrites() {
        // Legacy: CentralizaMultiSeleccion.java:411 persists (CentralizaGenerico.logica) only when p_SimulaOperacion is "N";
        // CentralizaClearingFinanciero.java itself never writes.
        when(plataforma.consultarClearingFinanciero(any(), any(), any(), any()))
                .thenReturn(List.of(det(9001L, 5L, "4455", new BigDecimal("1000.00"), BigDecimal.ZERO)));
        service.centralizar(entrada("S"));
        verify(repository, never()).persistirCentralizacion(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("CONT-E-142: simulaOperacion=N persists exactly the produced tables once (usuario as String.valueOf(idUsuario))")
    void contE142_realRunPersistsProducedTables() {
        // Legacy: CentralizaMultiSeleccion.java:347-357 (salida tables appended to entradaCentralizaGenerico) and
        // :411-417 (p_CodigoUsuario = String.valueOf(p_IdUsuario), p_Ejercicio, p_FechaContable, p_IdEmpresa).
        when(plataforma.consultarClearingFinanciero(any(), any(), any(), any()))
                .thenReturn(List.of(det(9001L, 5L, "4455", new BigDecimal("1000.00"), BigDecimal.ZERO)));
        SalidaCentralizacion salida = service.centralizar(entrada("N"));
        verify(repository, times(1)).persistirCentralizacion(eq(ID_TIPO), eq(FECHA), eq(ID_EMPRESA), eq(EJERCICIO), eq("97"),
                eq(salida.tabMovimientos()), eq(salida.tabEventos()), eq(salida.tabDatosMovimiento()), eq(salida.tabDatosEvento()));
    }

    @Test
    @DisplayName("CONT-E-143: data source is ConsultarClearingFinanciero(idEmpresa, idUsuario, idModulo, fechaLiquidacion = fechaContable); no SQL is run")
    void contE143_sourceIsConsultarClearingFinanciero() {
        // Legacy: CentralizaClearingFinanciero.java:58-68 (also p_UsaReglasNegocio = "S", camaras PM,RV,SM,PH,HS - not expressible in the port)
        clearing();
        service.centralizar(entrada("S"));
        verify(plataforma).consultarClearingFinanciero(ID_EMPRESA, ID_USUARIO, ID_MODULO, FECHA);
        assertThat(issuedSql()).isEmpty();
        verify(repository, never()).queryMovimientos(any(), any(), any());
        verify(repository, never()).queryEventosOperacion(any(), any(), any());
    }

    @Test
    @DisplayName("CONT-E-144: only details with IdComprobanteTesoreria > 0 are centralized")
    void contE144_onlyPositiveComprobante() {
        // Legacy: CentralizaClearingFinanciero.java:71,92,114 (filter detalle.IdComprobanteTesoreria > 0)
        clearing(det(1L, 0L, "1", new BigDecimal("5.00"), BigDecimal.ZERO), det(2L, 7L, "2", new BigDecimal("6.00"), BigDecimal.ZERO));
        SalidaCentralizacion salida = service.centralizar(entrada("S"));
        assertThat(salida.tabMovimientos()).extracting(TabMovimientoRef::idMovimiento).containsExactly(2L);
        assertThat(salida.tabEventos()).extracting(TabEventoRef::idMovimiento).containsExactly(2L, 2L);
        assertThat(salida.tabDatosMovimiento()).extracting(TabDatoMovimientoRef::idMovimiento).containsOnly(2L).hasSize(10);
    }

    @Test
    @DisplayName("CONT-E-145: movement row: CLEARING, Numero = folio, Linea 0, IdPortafolio 0, Ejercicio = year of fechaContable (not entrada), Periodo MM-yy, Estado empty")
    void contE145_movementBuiltFromDetail() {
        // Legacy: CentralizaClearingFinanciero.java:72-87
        clearing(det(9001L, 5L, "4455", new BigDecimal("1000.00"), new BigDecimal("3.00")));
        EntradaCentralizacion in = new EntradaCentralizacion(ID_EMPRESA, ID_USUARIO, ID_MODULO, ID_TIPO, FECHA, null, 1999L, "S");
        SalidaCentralizacion salida = service.centralizar(in);
        assertThat(salida.tabMovimientos()).containsExactly(new TabMovimientoRef(ID_TIPO, 9001L, "CLEARING", ID_EMPRESA, 4455L, 0L,
                FECHA, "22222222-2", 0L, 2025L, "03-25", new BigDecimal("1000.00"), "USD", ""));
        assertThat(salida.resultados()).singleElement().satisfies(r -> {
            assertThat(r.ejercicio()).isEqualTo("2025");
            assertThat(r.periodo()).isEqualTo("03-25");
        });
    }

    @Test
    @DisplayName("CONT-E-146: Monto = TotalVenta when > 0, otherwise TotalCompra; null folio -> Numero 0")
    void contE146_montoVentaElseCompraAndNullFolio() {
        // Legacy: CentralizaClearingFinanciero.java:77 (Optional.ofNullable(folio).orElse("0")), :85
        clearing(det(1L, 5L, null, BigDecimal.ZERO, new BigDecimal("77.70")), det(2L, 5L, "8", new BigDecimal("-1.00"), new BigDecimal("4.00")));
        SalidaCentralizacion salida = service.centralizar(entrada("S"));
        assertThat(salida.tabMovimientos()).extracting(TabMovimientoRef::numero, TabMovimientoRef::monto)
                .containsExactly(tuple(0L, new BigDecimal("77.70")), tuple(8L, new BigDecimal("4.00")));
    }

    @Test
    @DisplayName("CONT-E-147: events TOTAL_VENTA then TOTAL_COMPRA per detail, null total -> 0, moneda of the transaction")
    void contE147_totalVentaCompraEvents() {
        // Legacy: CentralizaClearingFinanciero.java:92-110
        clearing(det(9001L, 5L, "4455", new BigDecimal("1000.00"), null));
        SalidaCentralizacion salida = service.centralizar(entrada("S"));
        assertThat(salida.tabEventos()).containsExactly(
                new TabEventoRef(ID_TIPO, 9001L, "TOTAL_VENTA", "Total Venta", new BigDecimal("1000.00"), "USD"),
                new TabEventoRef(ID_TIPO, 9001L, "TOTAL_COMPRA", "Total Compra", BigDecimal.ZERO, "USD"));
    }

    @Test
    @DisplayName("CONT-E-148: null TotalVenta aborts (TotalVenta.compareTo on null)")
    void contE148_nullTotalVentaAborts() {
        // Legacy: CentralizaClearingFinanciero.java:85 detalle.TotalVenta.compareTo(...) -> NPE before the events are built
        clearing(det(9001L, 5L, "4455", null, new BigDecimal("3.00")));
        assertThatThrownBy(() -> service.centralizar(entrada("S"))).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("CONT-E-149: 10 datos-movimiento in order; FechaDocumento = fechaContable; EsContraparte = SI")
    void contE149_datosMovimientoFields() {
        // Legacy: CentralizaClearingFinanciero.java:113-194
        clearing(det(9001L, 5L, "4455", new BigDecimal("1000.00"), BigDecimal.ZERO));
        SalidaCentralizacion salida = service.centralizar(entrada("S"));
        assertThat(salida.tabDatosMovimiento())
                .extracting(TabDatoMovimientoRef::tipoDato, TabDatoMovimientoRef::valorVarChar, TabDatoMovimientoRef::valorDecimal)
                .containsExactly(
                        tuple("Folio", "4455", BigDecimal.ZERO),
                        tuple("FechaDocumento", FECHA, BigDecimal.ZERO),
                        tuple("CompraVenta", "C", BigDecimal.ZERO),
                        tuple("ClaseInstrumento", "RF", BigDecimal.ZERO),
                        tuple("Camara", "CAMARA-PM", BigDecimal.ZERO),
                        tuple("CondicionLiquidacion", "PH", BigDecimal.ZERO),
                        tuple("Bolsa", "BCS", BigDecimal.ZERO),
                        tuple("DocumentoIdentidad", "22222222-2", BigDecimal.ZERO),
                        tuple("NombreCliente", "CONTRAPARTE SA", BigDecimal.ZERO),
                        tuple("EsContraparte", "SI", BigDecimal.ZERO));
        assertIds(salida.tabDatosMovimiento(), 9001L);
    }

    @Test
    @DisplayName("CONT-E-150: null detail strings -> empty, except Folio which stays null (assigned directly, no ctor coalescing)")
    void contE150_datosMovimientoNulls() {
        // Legacy: CentralizaClearingFinanciero.java:119 (mov.ValorVarChar = detalle.FolioTransaccion, no-arg ctor),
        // :135-183 Optional.ofNullable(..).orElse("")
        clearing(new ClearingFinancieroDetalle(9001L, 5L, null, new BigDecimal("1.00"), BigDecimal.ZERO, null, null, null, null, null, null, null, null, null));
        SalidaCentralizacion salida = service.centralizar(entrada("S"));
        assertThat(salida.tabDatosMovimiento())
                .extracting(TabDatoMovimientoRef::tipoDato, TabDatoMovimientoRef::valorVarChar)
                .containsExactly(
                        tuple("Folio", null), tuple("FechaDocumento", FECHA), tuple("CompraVenta", ""), tuple("ClaseInstrumento", ""),
                        tuple("Camara", ""), tuple("CondicionLiquidacion", ""), tuple("Bolsa", ""), tuple("DocumentoIdentidad", ""),
                        tuple("NombreCliente", ""), tuple("EsContraparte", "SI"));
    }

    @Test
    @DisplayName("CONT-E-151: 0 errors, no messages, no datos-evento")
    void contE151_noErrors() {
        // Legacy: BcsCentralizacion.java:25-39; CentralizaClearingFinanciero.java never touches P_OUT_* counters
        clearing(det(9001L, 5L, "4455", new BigDecimal("1000.00"), BigDecimal.ZERO));
        SalidaCentralizacion salida = service.centralizar(entrada("S"));
        assertThat(salida.erroresContador()).isZero();
        assertThat(salida.mensajes()).isEmpty();
        assertThat(salida.tabDatosEvento()).isEmpty();
    }

    // ------------------------------------------------------------------ fixtures

    static ClearingFinancieroDetalle det(Long idTransaccion, Long idComprobante, String folio, BigDecimal venta, BigDecimal compra) {
        return new ClearingFinancieroDetalle(idTransaccion, idComprobante, folio, venta, compra, "USD", "22222222-2",
                "CONTRAPARTE SA", "C", "RF", "CAMARA-PM", "PH", "BCS", FECHA);
    }

    void clearing(ClearingFinancieroDetalle... d) {
        when(plataforma.consultarClearingFinanciero(any(), any(), any(), any())).thenReturn(List.of(d));
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
