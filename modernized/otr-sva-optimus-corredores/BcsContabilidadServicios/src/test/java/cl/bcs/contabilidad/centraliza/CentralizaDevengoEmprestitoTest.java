package cl.bcs.contabilidad.centraliza;

import cl.bcs.contabilidad.domain.model.EmpresaInfo;
import cl.bcs.contabilidad.domain.model.EntradaCentralizacion;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabDatoMovimientoRef;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabEventoRef;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabMovimientoRef;
import cl.bcs.contabilidad.domain.port.out.ContabilidadRepository;
import cl.bcs.contabilidad.domain.port.out.PlataformaPort;
import cl.bcs.contabilidad.domain.service.CentralizaDevengoEmprestito;
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
 * Characterization tests for CentralizaDevengoEmprestito (work package WP-E).
 *
 * <p>Oracle: legacy CentralizaDevengoEmprestito.java (BSCL/Contabilidad/JAVA/BcsContabilidadServicios/src/cl/bcs/contabilidad/), NOT the
 * modern class. Each test cites the legacy lines it pins. A failing test is a modern-vs-legacy discrepancy.
 *
 * <p>SQL evidence is read from every repository invocation that carries a String argument naming the legacy
 * SQL file (any port method), so the tests do not depend on which port method the implementation uses.
 * All fixture values are synthetic; no legacy credential or real RUT is used.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CentralizaDevengoEmprestitoTest {

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
    CentralizaDevengoEmprestito service;

    @BeforeEach
    void setUp() {
        service = new CentralizaDevengoEmprestito(plataforma, repository);
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
        rows("CentralizaDevengoEmprestito_SelectMovimientoTipoCentralizacionTipo.sql", List.of(row("IdTipoMovimiento", "DEVEMPRV")));
    }

    @Test
    @DisplayName("CONT-E-217: simulaOperacion=S never writes")
    void contE217_simulationNeverWrites() {
        // Legacy: CentralizaMultiSeleccion.java:411 persists (CentralizaGenerico.logica) only when p_SimulaOperacion is "N";
        // CentralizaDevengoEmprestito.java itself never writes.
        rows("CentralizaDevengoEmprestito_SelectMovimiento.sql", List.of(devRow("NO", 501L)));
        service.centralizar(entrada("S"));
        verify(repository, never()).persistirCentralizacion(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("CONT-E-218: simulaOperacion=N persists exactly the produced tables once (usuario as String.valueOf(idUsuario))")
    void contE218_realRunPersistsProducedTables() {
        // Legacy: CentralizaMultiSeleccion.java:347-357 (salida tables appended to entradaCentralizaGenerico) and
        // :411-417 (p_CodigoUsuario = String.valueOf(p_IdUsuario), p_Ejercicio, p_FechaContable, p_IdEmpresa).
        rows("CentralizaDevengoEmprestito_SelectMovimiento.sql", List.of(devRow("NO", 501L)));
        SalidaCentralizacion salida = service.centralizar(entrada("N"));
        verify(repository, times(1)).persistirCentralizacion(eq(ID_TIPO), eq(FECHA), eq(ID_EMPRESA), eq(EJERCICIO), eq("97"),
                eq(salida.tabMovimientos()), eq(salida.tabEventos()), eq(salida.tabDatosMovimiento()), eq(salida.tabDatosEvento()));
    }

    @Test
    @DisplayName("CONT-E-219: tipo de movimiento query runs CentralizaDevengoEmprestito_SelectMovimientoTipoCentralizacionTipo.sql with params {IdTipoCentralizacion}")
    void contE219_tipoCentralizacionQuery() {
        // Legacy: CentralizaDevengoEmprestito.java:107-110
        try { service.centralizar(entrada("S")); } catch (RuntimeException ignored) { }
        assertThat(paramsOf("CentralizaDevengoEmprestito_SelectMovimientoTipoCentralizacionTipo.sql")).isEqualTo(Map.of("IdTipoCentralizacion", ID_TIPO));
    }

    @Test
    @DisplayName("CONT-E-220: fechaContable = PLA.FechaSistema -> CentralizaDevengoEmprestito_SelectMovimiento.sql with {FechaContable, IdTipoInstrumento=EMP for DEVEMPRV, IdEmpresa}")
    void contE220_movimientoQueryCurrent() {
        // Legacy: CentralizaDevengoEmprestito.java:102-104 (fechaSistema from ConfiguracionSistema PLATAFORMA/PLA.FechaSistema), 112-122
        try { service.centralizar(entrada("S")); } catch (RuntimeException ignored) { }
        assertThat(paramsOf("CentralizaDevengoEmprestito_SelectMovimiento.sql")).isEqualTo(Map.of("FechaContable", FECHA, "IdTipoInstrumento", "EMP", "IdEmpresa", ID_EMPRESA));
        assertNotIssued("CentralizaDevengoEmprestito_SelectMovimientoHistorico.sql");
    }

    @Test
    @DisplayName("CONT-E-221: fechaContable != PLA.FechaSistema -> CentralizaDevengoEmprestito_SelectMovimientoHistorico.sql; IdTipoMovimiento other than DEVEMPRV -> IdTipoInstrumento ERF")
    void contE221_movimientoQueryHistoricoAndRf() {
        // Legacy: CentralizaDevengoEmprestito.java:114,118-121
        fechaSistema(FECHA_HIST);
        rows("CentralizaDevengoEmprestito_SelectMovimientoTipoCentralizacionTipo.sql", List.of(row("IdTipoMovimiento", "DEVEMPRF")));
        try { service.centralizar(entrada("S")); } catch (RuntimeException ignored) { }
        assertThat(paramsOf("CentralizaDevengoEmprestito_SelectMovimientoHistorico.sql")).isEqualTo(Map.of("FechaContable", FECHA, "IdTipoInstrumento", "ERF", "IdEmpresa", ID_EMPRESA));
        assertNotIssued("CentralizaDevengoEmprestito_SelectMovimiento.sql");
    }

    @Test
    @DisplayName("CONT-E-222: row with EsCuentaPropia NO -> one movement: IdMovimiento/Numero/Linea = FolioOperacion, Periodo yyyy-MM, Monto = MontoCompraActual, moneda sistema")
    void contE222_movementBuiltFromRow() {
        // Legacy: CentralizaDevengoEmprestito.java:140-157
        rows("CentralizaDevengoEmprestito_SelectMovimiento.sql", List.of(devRow("NO", 501L)));
        SalidaCentralizacion salida = service.centralizar(entrada("S"));
        assertThat(salida.tabMovimientos()).containsExactly(new TabMovimientoRef(ID_TIPO, 501L, "DEVEMPRV", ID_EMPRESA, 501L, 501L,
                FECHA, "33333333-3", 300L, EJERCICIO, "2025-03", new BigDecimal("99.40"), "CLP", "V"));
        assertThat(salida.resultados()).singleElement().satisfies(r -> assertThat(r.ejercicio()).isEqualTo("2025"));
    }

    @Test
    @DisplayName("CONT-E-223: IdTipoMovimiento of the movement is DEVEMPRF whenever the configured type is not DEVEMPRV")
    void contE223_movementTypeRf() {
        // Legacy: CentralizaDevengoEmprestito.java:145
        rows("CentralizaDevengoEmprestito_SelectMovimientoTipoCentralizacionTipo.sql", List.of(row("IdTipoMovimiento", "OTRO")));
        rows("CentralizaDevengoEmprestito_SelectMovimiento.sql", List.of(devRow("NO", 501L)));
        SalidaCentralizacion salida = service.centralizar(entrada("S"));
        assertThat(salida.tabMovimientos()).extracting(TabMovimientoRef::idTipoMovimiento).containsExactly("DEVEMPRF");
    }

    @Test
    @DisplayName("CONT-E-224: rows whose EsCuentaPropia is not NO produce nothing")
    void contE224_cuentaPropiaSkipped() {
        // Legacy: CentralizaDevengoEmprestito.java:140-157 if ("NO".equals(data.EsCuentaPropia))
        rows("CentralizaDevengoEmprestito_SelectMovimiento.sql", List.of(devRow("SI", 501L), devRow(null, 502L)));
        SalidaCentralizacion salida = service.centralizar(entrada("S"));
        assertThat(salida.tabMovimientos()).isEmpty();
        assertThat(salida.tabEventos()).isEmpty();
        assertThat(salida.tabDatosMovimiento()).isEmpty();
        assertThat(salida.resultados()).isEmpty();
    }

    @Test
    @DisplayName("CONT-E-225: 16 datos-movimiento per movement in legacy order (Premio carries TasaCompra as decimal)")
    void contE225_datosMovimientoFields() {
        // Legacy: CentralizaDevengoEmprestito.java:161-176
        rows("CentralizaDevengoEmprestito_SelectMovimiento.sql", List.of(devRow("NO", 501L)));
        SalidaCentralizacion salida = service.centralizar(entrada("S"));
        assertThat(salida.tabDatosMovimiento())
                .extracting(TabDatoMovimientoRef::tipoDato, TabDatoMovimientoRef::valorVarChar, TabDatoMovimientoRef::valorDecimal)
                .containsExactly(
                        tuple("DocumentoIdentidad", "33333333-3", BigDecimal.ZERO),
                        tuple("Portafolio", "PORT-1", BigDecimal.ZERO),
                        tuple("EsCuentaPropia", "NO", BigDecimal.ZERO),
                        tuple("EsRelacionado", "null", BigDecimal.ZERO),
                        tuple("FechaOperacion", "2025-03-03", BigDecimal.ZERO),
                        tuple("FechaLiquidacion", "2025-03-05", BigDecimal.ZERO),
                        tuple("FolioOperacion", "501", BigDecimal.ZERO),
                        tuple("ClaseInstrumento", "RV", BigDecimal.ZERO),
                        tuple("TipoInstrumento", "ACC", BigDecimal.ZERO),
                        tuple("Nemotecnico", "NEMO1", BigDecimal.ZERO),
                        tuple("Boveda", "DCV", BigDecimal.ZERO),
                        tuple("Emisor", "EMI1", BigDecimal.ZERO),
                        tuple("TipoReajuste", "$", BigDecimal.ZERO),
                        tuple("CodigoIFRS", "", BigDecimal.ZERO),
                        tuple("Moneda", "USD", BigDecimal.ZERO),
                        tuple("Premio", "", new BigDecimal("0.0450")));
        assertIds(salida.tabDatosMovimiento(), 501L);
    }

    @Test
    @DisplayName("CONT-E-226: 7 events per movement in legacy order, moneda = IdMonedaSistema of the row, null monto kept null")
    void contE226_events() {
        // Legacy: CentralizaDevengoEmprestito.java:180-186 ; TabEventos ctor does not coalesce MontoEvento (pojo/TabEventos.java:17-24)
        rows("CentralizaDevengoEmprestito_SelectMovimiento.sql", List.of(devRow("NO", 501L)));
        SalidaCentralizacion salida = service.centralizar(entrada("S"));
        assertThat(salida.tabEventos()).containsExactly(
                new TabEventoRef(ID_TIPO, 501L, "MONTO_MERCADO_ANTERIOR", "Monto a Tasa Mercado Anterior", null, "CLP"),
                new TabEventoRef(ID_TIPO, 501L, "MONTO_MERCADO_ACTUAL", "Monto a Tasa Mercado Actual", new BigDecimal("101.20"), "CLP"),
                new TabEventoRef(ID_TIPO, 501L, "MONTO_COMPRA_ANTERIOR", "Monto a Tasa Compra Anterior", new BigDecimal("99.30"), "CLP"),
                new TabEventoRef(ID_TIPO, 501L, "MONTO_COMPRA_ACTUAL", "Monto a Tasa Compra Actual", new BigDecimal("99.40"), "CLP"),
                new TabEventoRef(ID_TIPO, 501L, "MONTO_COMPRA_PROXIMO", "Monto a Tasa Compra Proximo", new BigDecimal("99.50"), "CLP"),
                new TabEventoRef(ID_TIPO, 501L, "INTERES_COMPRA_ANTERIOR", "Interes Compra Anterior", new BigDecimal("0.60"), "CLP"),
                new TabEventoRef(ID_TIPO, 501L, "INTERES_COMPRA_PROXIMO", "Interes Compra Proximo", new BigDecimal("0.70"), "CLP"));
    }

    @Test
    @DisplayName("CONT-E-227: null FechaOperacion aborts (data.FechaOperacion.toString())")
    void contE227_nullFechaOperacionAborts() {
        // Legacy: CentralizaDevengoEmprestito.java:165
        Map<String, Object> r = devRow("NO", 501L);
        r.put("FechaOperacion", null);
        rows("CentralizaDevengoEmprestito_SelectMovimiento.sql", List.of(r));
        assertThatThrownBy(() -> service.centralizar(entrada("S"))).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("CONT-E-228: CUS.UsaFechaLiquidacionEnVencimiento is read but does not change the output")
    void contE228_usaFechaLiquidacionHasNoEffect() {
        // Legacy: CentralizaDevengoEmprestito.java:127 stores it in conf.usaFechaLiquidacion; dataCentralizacion (:139-189) never reads it
        rows("CentralizaDevengoEmprestito_SelectMovimiento.sql", List.of(devRow("NO", 501L)));
        config("PLATAFORMA", "CUS.UsaFechaLiquidacionEnVencimiento", "S");
        SalidaCentralizacion conS = service.centralizar(entrada("S"));
        config("PLATAFORMA", "CUS.UsaFechaLiquidacionEnVencimiento", "N");
        SalidaCentralizacion conN = service.centralizar(entrada("S"));
        assertThat(conS).isEqualTo(conN);
        assertThat(conS.tabMovimientos()).hasSize(1);
    }

    @Test
    @DisplayName("CONT-E-229: 0 errors, no messages, no datos-evento")
    void contE229_noErrors() {
        // Legacy: BcsCentralizacion.java:25-39
        rows("CentralizaDevengoEmprestito_SelectMovimiento.sql", List.of(devRow("NO", 501L)));
        SalidaCentralizacion salida = service.centralizar(entrada("S"));
        assertThat(salida.erroresContador()).isZero();
        assertThat(salida.mensajes()).isEmpty();
        assertThat(salida.tabDatosEvento()).isEmpty();
    }

    // ------------------------------------------------------------------ fixtures

    /** Row of CentralizaDevengoEmprestito_SelectMovimiento.sql (legacy CentralizaDevengoEmprestito.Movimientos). */
    static Map<String, Object> devRow(String esCuentaPropia, Long folio) {
        return row("DocumentoIdentidad", "33333333-3", "IdPortafolio", 300L, "CodigoPortafolio", "PORT-1",
                "EsCuentaPropia", esCuentaPropia, "EsRelacionado", null,
                "FechaOperacion", java.sql.Date.valueOf("2025-03-03"), "FechaVencimientoOperacion", java.sql.Date.valueOf("2025-04-03"),
                "FechaLiquidacion", java.sql.Date.valueOf("2025-03-05"), "IdOperacion", 88L, "FolioOperacion", folio,
                "IdClasificacionTipoInstrumento", "RV", "IdTipoInstrumento", "ACC", "IdInstrumento", 9L, "Nemotecnico", "NEMO1",
                "IdEmisor", "EMI1", "IdTipoReajuste", "$", "IdBoveda", "DCV", "CodigoIFRS", null,
                "IdMonedaSistema", "CLP", "IdMonedaInstrumento", "USD", "TasaCompra", new BigDecimal("0.0450"),
                "MontoMercadoAnterior", null, "MontoMercadoActual", new BigDecimal("101.20"),
                "MontoCompraAnterior", new BigDecimal("99.30"), "MontoCompraActual", new BigDecimal("99.40"),
                "MontoCompraProximo", new BigDecimal("99.50"), "InteresMontoCompraAnterior", new BigDecimal("0.60"),
                "InteresMontoCompraProximo", new BigDecimal("0.70"), "IdEmpresa", ID_EMPRESA, "IdEstado", "V");
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
