package cl.bcs.contabilidad.envio;

import cl.bcs.contabilidad.domain.port.out.PlanCuentasSapPort;
import cl.bcs.contabilidad.envio.support.CasoEnvio;
import cl.bcs.contabilidad.envio.support.Fakes;
import cl.bcs.contabilidad.envio.support.JsonCanon;
import cl.bcs.contabilidad.service.EnvioContabilidadService;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Characterization of legacy {@code cl.bcs.contabilidad.EnvioContabilidad} (BcsContabilidadServicios/src/
 * cl/bcs/contabilidad/EnvioContabilidad.java) as {@link EnvioContabilidadService#execute(JsonObject)}.
 *
 * <p>EnvioContabilidad does NOT call SAP over HTTP: it validates pre-vouchers, builds the SAP-shaped lines and
 * writes three staging tables that GenerarJsonAsientoSap later turns into JournalVouchers. Each test runs
 * the modern service on H2 with the case seed and compares (a) the JavaCall output and (b) the staging
 * tables with {@code expected.json}, which was recorded by running the unmodified legacy class on the same
 * data (see src/test/resources/envio/README.md). Explicit assertions restate each rule so a test documents
 * the behaviour on its own.
 *
 * <p>Ids CONT-B-NN are local to work package WP-B (BUSINESS_RULES.md has no Contabilidad rules).
 */
class EnvioContabilidadServiceTest {

    static final List<String> CASOS = List.of(
            "CONT-B-01_envio-detallado-clp",
            "CONT-B-02_envio-estados-y-resumido",
            "CONT-B-03_envio-estado-E-detallado-sin-lineas",
            "CONT-B-05_envio-lote-mixto-duplica-lineas",
            "CONT-B-06_envio-moneda-extranjera-y-paridad-210",
            "CONT-B-07_envio-socios-shortname",
            "CONT-B-08_envio-folio-dte",
            "CONT-B-09_envio-cuentas-sys-sin-centro-costo",
            "CONT-B-10_envio-vacio",
            "CONT-B-11_envio-duplicados-en-entrada",
            "CONT-B-12_envio-sin-archivo-configuracion");

    private static final AtomicInteger EJECUTADOS = new AtomicInteger();

    /** Runs the modern service on the case and asserts equivalence with the legacy golden. Returns the output. */
    static JsonObject ejecutarYComparar(String nombre) {
        CasoEnvio caso = CasoEnvio.cargar(nombre);
        PlanCuentasSapPort plan = caso.sinArchivoConfiguracion()
                ? Fakes.planCuentasSinConfiguracion() : Fakes.planCuentas(caso.planCuentas());
        EnvioContabilidadService service = new EnvioContabilidadService(
                caso.jdbc(), plan, Fakes.plataforma(caso.config()), Fakes.diccionario());

        JsonObject salida = service.execute(caso.entrada().deepCopy());

        JsonCanon.assertEquivalente(nombre + " salida", caso.esperado().get("salida"), salida, "P_OUT_ASIENTOS");
        JsonObject esperadas = caso.esperado().getAsJsonObject("tablas");
        JsonObject actuales = caso.tablasEnvio();
        for (String t : esperadas.keySet()) {
            assertThat(sorted(actuales.getAsJsonArray(t))).as(nombre + " rows of " + t)
                    .isEqualTo(sorted(esperadas.getAsJsonArray(t)));
        }
        EJECUTADOS.incrementAndGet();
        return salida;
    }

    private static List<String> sorted(JsonArray rows) {
        List<String> out = new ArrayList<>();
        rows.forEach(r -> out.add(JsonCanon.canon(r, Set.of()).toString()));
        out.sort(String::compareTo);
        return out;
    }

    private static List<JsonObject> asientos(JsonObject salida) {
        List<JsonObject> out = new ArrayList<>();
        salida.getAsJsonArray("P_OUT_ASIENTOS").forEach(e -> out.add(e.getAsJsonObject()));
        return out;
    }

    private static JsonObject asiento(JsonObject salida, String accountCode, String formato) {
        return asientos(salida).stream()
                .filter(a -> accountCode.equals(a.get("AccountCode").getAsString()) && formato.equals(a.get("Formato").getAsString()))
                .findFirst().orElseThrow(() -> new AssertionError("no line " + accountCode + "/" + formato + " in " + salida));
    }

    private static BigDecimal num(JsonObject o, String k) {
        return o.get(k).getAsBigDecimal();
    }

    private static String str(JsonObject o, String k) {
        JsonElement e = o.get(k);
        return e == null || e.isJsonNull() ? null : e.getAsString();
    }

    @Test
    @DisplayName("CONT-B-01: detailed CLP line = |Evento.Monto|, LineMemo cut at 50, ShortName prefix+rut, BPLId 4, Reference1/2, homologated String fields only")
    void contB01_detalladoClp_construyeLineasYCabecera() {
        // EnvioContabilidad.java:449-624 (Detallado), :465 LineMemo 50, :467 AccountCode, :469 BPLId=4,
        // :470-472 Reference1/2/ReferenceDate1, :484-506 homologation (String targets only), :528 PrefijoClienteSAP,
        // :559-581 CostingCode only if UsaCentroCosto=S and ActType E/I, :591-605 Debit = |Monto|.
        JsonObject s = ejecutarYComparar("CONT-B-01_envio-detallado-clp");

        JsonObject cliente = asiento(s, "SAP-1201", "Detallado");
        assertThat(num(cliente, "Debit")).isEqualByComparingTo("1500.50");
        assertThat(num(cliente, "Credit")).isEqualByComparingTo("0");
        assertThat(str(cliente, "LineMemo")).isEqualTo("Venta pactada cliente 76123456-7 folio 123 con glo").hasSize(50);
        assertThat(str(cliente, "ShortName")).isEqualTo("CL76123456-7");
        assertThat(str(cliente, "TipoSocioNegocio")).isEqualTo("CLIENTE");
        assertThat(num(cliente, "BPLId")).isEqualByComparingTo("4");          // CampoSAP BPLId (Long) is NOT homologated
        assertThat(str(cliente, "DueDate")).isEqualTo("20250312");             // homologated from dato FechaLiq
        assertThat(str(cliente, "U_BCS_Nemo")).isEqualTo("BCS-ACC");
        assertThat(str(cliente, "AdditionalReference")).isEqualTo("REF-1");
        assertThat(str(cliente, "Reference1")).isEqualTo("76123456-7");
        assertThat(str(cliente, "Reference2")).isEqualTo("4711");
        assertThat(str(cliente, "ProfitCode")).isEqualTo("CC-10");
        assertThat(str(cliente, "CostingCode")).isEmpty();                     // SAP-1201 ActType N: no cost centre
        assertThat(str(cliente, "Project")).isEmpty();                         // ConRut is always '' in the SQL

        JsonObject comision = asiento(s, "SAP-4101", "Detallado");
        assertThat(num(comision, "Credit")).isEqualByComparingTo("1500.50");
        assertThat(str(comision, "CostingCode")).isEqualTo("CC-A");
        assertThat(str(comision, "CostingCode2")).isEqualTo("CC-B");
        assertThat(str(comision, "ShortName")).isEmpty();                      // non-analytic account
        assertThat(str(comision, "TipoSocioNegocio")).isEqualTo("CLIENTE");    // ...but the partner type is still set

        JsonObject cab = s.getAsJsonArray("P_OUT_PRECOMPROBANTE").get(0).getAsJsonObject();
        assertThat(str(cab, "Memo")).isEqualTo("PROPIA-VENTA PACT-100");       // SelectPrecomprobanteCreacion.sql:4
        assertThat(str(cab, "ReferenceDate")).isEqualTo("20250310");
        assertThat(s.getAsJsonArray("P_OUT_TIPOCENTRALIZACION").toString()).isEqualTo("[200]");
    }

    @Test
    @DisplayName("CONT-B-02: states C and A -> ADVERTENCIA PRE_COMPROBANTE_CONTABILIZADO_O_ANULADO; missing -> NO_EXISTE_PRE_COMPROBANTE; D and E are processed; counter stays 0")
    void contB02_estados_rechazaCyA_procesaDyE_yLineasAgrupadas() {
        // EnvioContabilidad.java:399-416 (validation), :407 'D' branch is dead code (only reached for C/A),
        // :629-659 Agrupado lines; SelectAsientoProcesoResumido.sql:6-7,18-19.
        JsonObject s = ejecutarYComparar("CONT-B-02_envio-estados-y-resumido");

        assertThat(s.get("P_OUT_ERRORESCONTADOR").getAsLong()).isZero();
        JsonArray m = s.getAsJsonArray("P_OUT_MENSAJES");
        assertThat(m).hasSize(3);
        assertThat(m.get(0).getAsJsonObject().get("codigo").getAsString()).isEqualTo("PRE_COMPROBANTE_CONTABILIZADO_O_ANULADO");
        assertThat(m.get(0).getAsJsonObject().get("tipo").getAsString()).isEqualTo("ADVERTENCIA");
        assertThat(m.get(0).getAsJsonObject().get("id").getAsLong()).isEqualTo(300L);
        assertThat(m.get(1).getAsJsonObject().get("codigo").getAsString()).isEqualTo("NO_EXISTE_PRE_COMPROBANTE");
        assertThat(m.get(1).getAsJsonObject().get("descripcion").getAsString()).isEqualTo("[NO_EXISTE_PRE_COMPROBANTE]{NumeroPreComprobante=999}");
        assertThat(m.get(2).getAsJsonObject().get("id").getAsLong()).isEqualTo(301L);
        // types of REJECTED pre-vouchers are still collected (line 395 runs before the state check)
        assertThat(s.getAsJsonArray("P_OUT_TIPOCENTRALIZACION").toString()).isEqualTo("[230,200,210]");

        JsonObject ingreso = asiento(s, "SAP-4101", "Agrupado");
        assertThat(str(ingreso, "LineMemo")).isEqualTo("Ingresos por comisiones de intermediacion de valor"); // SQL substring 1..50
        assertThat(str(ingreso, "DueDate")).isEqualTo("2025-03-11");           // FechaContable with dashes
        assertThat(str(ingreso, "TieneProject")).isEqualTo("N");
        assertThat(num(ingreso, "IdMovimiento")).isEqualByComparingTo("0");
        assertThat(str(asiento(s, "SAP-1101", "Agrupado"), "ProfitCode")).isEmpty(); // CentroCosto '0' -> ''
        assertThat(asientos(s)).hasSize(4);                                     // 0/0 line and unhomologated account dropped
    }

    @Test
    @DisplayName("CONT-B-03: estado E pre-voucher in detailed format writes header and type row but no line (detail SQL filters V,D)")
    void contB03_estadoE_detallado_sinLineas() {
        // EnvioContabilidad.java:404 (only C/A rejected); SelectAsientoProcesoDetallado.sql:30 Estado IN ('V','D').
        JsonObject s = ejecutarYComparar("CONT-B-03_envio-estado-E-detallado-sin-lineas");
        assertThat(s.getAsJsonArray("P_OUT_ASIENTOS")).isEmpty();
        assertThat(s.getAsJsonArray("P_OUT_PRECOMPROBANTE")).hasSize(1);
    }

    @Test
    @DisplayName("CONT-B-05: a batch mixing detailed and summarised pre-vouchers emits the detailed one TWICE (also as Agrupado) - legacy defect pinned")
    void contB05_loteMixto_duplicaLineasDelDetalladoComoAgrupado() {
        // EnvioContabilidad.java:438 and :629 only test that SOME definition of each format exists;
        // SelectAsientoProcesoResumido.sql has no format filter, so pre-voucher 500 is staged as Detallado AND Agrupado.
        JsonObject s = ejecutarYComparar("CONT-B-05_envio-lote-mixto-duplica-lineas");
        long lineas500 = asientos(s).stream().filter(a -> a.get("ParentKey").getAsLong() == 500L).count();
        assertThat(lineas500).isEqualTo(4);
        assertThat(asientos(s)).hasSize(6);
    }

    @Test
    @DisplayName("CONT-B-06: foreign currency -> FCDebit/FCCredit = |Monto|, Debit/Credit = |MontoMonedaSistema|; type 210 with CalculaParidad=S books in local currency")
    void contB06_monedaExtranjera_yParidadTesoreria210() {
        // EnvioContabilidad.java:490-493 (MontoMonedaSistema, abs), :591-621, :593/:609 (paridad && IdTipoCentralizacion==210),
        // :477-478 ActType compared ignoring case ('e').
        JsonObject s = ejecutarYComparar("CONT-B-06_envio-moneda-extranjera-y-paridad-210");
        JsonObject usd = asientos(s).stream().filter(a -> a.get("ParentKey").getAsLong() == 600L && "SAP-1101".equals(str(a, "AccountCode")))
                .findFirst().orElseThrow();
        assertThat(num(usd, "FCDebit")).isEqualByComparingTo("1000");
        assertThat(num(usd, "Debit")).isEqualByComparingTo("950123.45");
        assertThat(str(usd, "FCCurrency")).isEqualTo("USD");
        JsonObject gasto = asientos(s).stream().filter(a -> a.get("ParentKey").getAsLong() == 600L && "SAP-5101".equals(str(a, "AccountCode")))
                .findFirst().orElseThrow();
        assertThat(str(gasto, "CostingCode")).isEqualTo("CC-X");
        JsonObject teso = asientos(s).stream().filter(a -> a.get("ParentKey").getAsLong() == 610L && "SAP-1101".equals(str(a, "AccountCode")))
                .findFirst().orElseThrow();
        assertThat(num(teso, "Debit")).isEqualByComparingTo("500");
        assertThat(num(teso, "FCDebit")).isEqualByComparingTo("0");
        assertThat(str(teso, "FCCurrency")).isEmpty();
    }

    @Test
    @DisplayName("CONT-B-07: ShortName - EsContraparte SI -> CP+rut, EsProveedor SI -> PR+rut, default on analytic account -> 'C'+rut (hardcoded), non-analytic -> ''")
    void contB07_shortNameYTipoSocio() {
        // EnvioContabilidad.java:522-540 (EsContraparte / EsProveedor), :585-588 default "C" + DocumentoIdentidad.
        JsonObject s = ejecutarYComparar("CONT-B-07_envio-socios-shortname");
        List<String> shortNames = asientos(s).stream().map(a -> str(a, "ShortName") + "|" + str(a, "TipoSocioNegocio")).sorted().toList();
        assertThat(shortNames).containsExactly(
                "C33333333-3|CLIENTE", "CP11111111-1|CONTRAPARTE", "PR22222222-2|PROVEEDOR", "|CONTRAPARTE");
    }

    @Test
    @DisplayName("CONT-B-08: FolioDTE != '0' appends '/folio' keeping 50 chars; a movement with two events appends it TWICE (duplicated dato rows) - legacy defect pinned")
    void contB08_folioDte_sufijoEnLineMemo() {
        // EnvioContabilidad.java:541-555; duplication comes from SelectMovimientoDato.sql (no DISTINCT, one row per asiento x evento).
        JsonObject s = ejecutarYComparar("CONT-B-08_envio-folio-dte");
        assertThat(str(asiento(s, "SAP-1201", "Detallado"), "LineMemo")).isEqualTo("Factura servicios custodia/45678/45678");
        assertThat(str(asiento(s, "SAP-1101", "Detallado"), "LineMemo")).isEqualTo("Pago sin folio");
        assertThat(str(asiento(s, "SAP-1101", "Detallado"), "U_BCS_FolioDTE")).isEqualTo("0");
        assertThat(str(asiento(s, "SAP-2101", "Detallado"), "LineMemo"))
                .isEqualTo("Glosa de mas de cincuenta caracteres para proba/99").hasSize(50);
    }

    @Test
    @DisplayName("CONT-B-09: UsaCuentasSYS=S uses CodigoContabilidad; UsaCentroCosto and CalculaParidad missing -> default N (no CostingCode)")
    void contB09_cuentasSys_yDefaultsDeConfiguracion() {
        // EnvioContabilidad.java:336-351 (defaults on missing parameter), :467 / :648 (UsaCuentasSYS).
        JsonObject s = ejecutarYComparar("CONT-B-09_envio-cuentas-sys-sin-centro-costo");
        assertThat(asientos(s)).extracting(a -> str(a, "AccountCode")).containsExactlyInAnyOrder("SYS-1201", "SYS-4101");
        assertThat(str(asiento(s, "SYS-4101", "Detallado"), "CostingCode")).isEmpty();
        assertThat(str(asiento(s, "SYS-1201", "Detallado"), "ShortName")).isEqualTo("C90000000-9");
    }

    @Test
    @DisplayName("CONT-B-10: empty p_SecCmbte -> no message, counter 0, empty outputs, nothing written (must not send IN () to HANA)")
    void contB10_entradaVacia() {
        // Legacy SQL util turns an empty list into IN (NULL) (BcsUtils SQL.java:509-513), so nothing matches.
        JsonObject s = ejecutarYComparar("CONT-B-10_envio-vacio");
        assertThat(s.getAsJsonArray("P_OUT_MENSAJES")).isEmpty();
        assertThat(s.getAsJsonArray("P_OUT_ASIENTOS")).isEmpty();
        assertThat(s.get("P_OUT_ERRORESCONTADOR").getAsLong()).isZero();
    }

    @Test
    @DisplayName("CONT-B-11: duplicated ids -> header once, PreComprobanteCentralizacionTipo row per occurrence, warning per occurrence")
    void contB11_idsDuplicadosEnEntrada() {
        // EnvioContabilidad.java:380-382 dedups only the query list; :390-417 iterates the raw input.
        JsonObject s = ejecutarYComparar("CONT-B-11_envio-duplicados-en-entrada");
        assertThat(s.getAsJsonArray("P_OUT_MENSAJES")).hasSize(2);
        assertThat(s.getAsJsonArray("P_OUT_PRECOMPROBANTE")).hasSize(1);
    }

    @Test
    @DisplayName("CONT-B-12: SAP configuration not found -> ERROR ARCHIVO_CONFIGURACION_NO_ENCONTRADO (literal U+FFFD), counter 1, nothing written")
    void contB12_sinConfiguracionSap() {
        // EnvioContabilidad.java:359-363; the legacy literal contains U+FFFD ("configuraci�n").
        JsonObject s = ejecutarYComparar("CONT-B-12_envio-sin-archivo-configuracion");
        assertThat(s.get("P_OUT_ERRORESCONTADOR").getAsLong()).isEqualTo(1L);
        JsonObject m = s.getAsJsonArray("P_OUT_MENSAJES").get(0).getAsJsonObject();
        assertThat(m.get("id").getAsLong()).isZero();
        assertThat(m.get("tipo").getAsString()).isEqualTo("ERROR");
        assertThat(m.get("codigo").getAsString()).isEqualTo("ARCHIVO_CONFIGURACION_NO_ENCONTRADO");
        assertThat(m.get("descripcion").getAsString()).isEqualTo("Archivo de configuraci�n no encontrado");
    }

    @AfterAll
    static void reportarEjecutados() {
        System.out.println("equivalence cases executed (EnvioContabilidad): " + EJECUTADOS.get() + "/" + CASOS.size());
        assertThat(EJECUTADOS.get()).as("equivalence cases executed; zero means NOT PROVEN").isPositive();
    }
}
