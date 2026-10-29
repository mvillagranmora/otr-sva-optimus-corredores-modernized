package cl.bcs.contabilidad.envio;

import cl.bcs.contabilidad.domain.model.sap.ResultadoAsientoSap;
import cl.bcs.contabilidad.domain.service.AsientoSapMapper;
import cl.bcs.contabilidad.envio.support.CasoEnvio;
import cl.bcs.contabilidad.envio.support.Fakes;
import cl.bcs.contabilidad.envio.support.JsonCanon;
import cl.bcs.contabilidad.service.GenerarJsonAsientoSapService;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Characterization of legacy {@code cl.bcs.contabilidad.GenerarJsonAsientoSap} (logica(), lines 150-347):
 * reads the staging written by EnvioContabilidad (InterfazContable_SelectPreComprobante.sql,
 * InterfazContable_SelectPrecomprobanteAsiento.sql) plus the SAP chart of accounts (OACT) and returns the
 * BusinessPartners and JournalVouchers that InterfazContable posts to the Service Layer.
 *
 * <p>Goldens hold the exact wire JSON legacy produces with {@code new Gson()} (CallerWSJournalVoucher.java:168,
 * :234): nulls omitted. Comparison is by JSON value; JournalEntryLines and partners are compared without
 * order because the staging query has no ORDER BY.
 */
class GenerarJsonAsientoSapServiceTest {

    static final List<String> CASOS = List.of(
            "CONT-B-20_sap-voucher-detallado-cl",
            "CONT-B-21_sap-voucher-agrupado-pe",
            "CONT-B-22_sap-orden-faltantes-duplicados",
            "CONT-B-23_sap-vacio");

    private static final AtomicInteger EJECUTADOS = new AtomicInteger();

    static GenerarJsonAsientoSapService servicio(CasoEnvio caso) {
        return new GenerarJsonAsientoSapService(caso.jdbc(), Fakes.planCuentas(caso.planCuentas()), Fakes.plataforma(caso.config()));
    }

    static List<Long> ids(CasoEnvio caso) {
        List<Long> ids = new ArrayList<>();
        caso.entrada().getAsJsonArray("p_IdPreComprobantes").forEach(e -> ids.add(e.getAsLong()));
        return ids;
    }

    static ResultadoAsientoSap ejecutarYComparar(String nombre) {
        CasoEnvio caso = CasoEnvio.cargar(nombre);
        ResultadoAsientoSap r = servicio(caso).generar(caso.entrada().get("p_IdEmpresa").getAsLong(), ids(caso));

        JsonObject esperado = new JsonObject();
        esperado.add("socios", caso.esperado().get("socios"));
        esperado.add("vouchers", caso.esperado().get("vouchers"));
        JsonObject actual = new JsonObject();
        actual.add("socios", JsonCanon.parse(AsientoSapMapper.toSapJson(r.socios())));
        actual.add("vouchers", JsonCanon.parse(AsientoSapMapper.toSapJson(r.vouchers())));
        JsonCanon.assertEquivalente(nombre, esperado, actual, "socios", "JournalEntryLines");
        // Wire JSON omits nulls (legacy new Gson()): no key may carry an explicit null.
        assertThat(AsientoSapMapper.toSapJson(r.vouchers())).as("SAP body must not contain nulls").doesNotContain(":null");
        EJECUTADOS.incrementAndGet();
        return r;
    }

    private static JsonObject entry(ResultadoAsientoSap r, int i) {
        return JsonCanon.parse(AsientoSapMapper.toSapJson(r.vouchers().get(i))).getAsJsonObject()
                .getAsJsonObject("JournalVoucher").getAsJsonObject("JournalEntry");
    }

    private static JsonObject linea(JsonObject entry, String account, String memo) {
        for (JsonElement l : entry.getAsJsonArray("JournalEntryLines")) {
            JsonObject o = l.getAsJsonObject();
            if (account.equals(o.get("AccountCode").getAsString()) && memo.equals(o.get("LineMemo").getAsString())) {
                return o;
            }
        }
        throw new AssertionError("no line " + account + " / " + memo + " in " + entry);
    }

    @Test
    @DisplayName("CONT-B-20: Detallado voucher - envelope {JournalVoucher:{JournalEntry}}, '<' '>' stripped, zero line skipped, FC-only line has no Debit/Credit, negative debit drops both")
    void contB20_voucherDetallado_cl() {
        // GenerarJsonAsientoSap.java:192-219 (envelope), :271-276 (all-zero skip), :284-292 (Debit/Credit vs FC split),
        // :294 (strip < >), :301 equalsIgnoreCase("Detallado"), :312-318 costing only for ActType E/I,
        // :333-337 renames U_BCS_CodigoProducto->U_BCS_Producto, U_glosa->U_BCS_GlosaExt, DueDate->U_BCS_FecLiquid.
        ResultadoAsientoSap r = ejecutarYComparar("CONT-B-20_sap-voucher-detallado-cl");
        JsonObject je = entry(r, 0);
        assertThat(je.get("JdtNum").getAsLong()).isEqualTo(100L);
        assertThat(je.get("U_BCS_IDPreComp").getAsString()).isEqualTo("100");
        assertThat(je.get("U_BCS_TipoCen").getAsString()).isEqualTo("200");
        assertThat(je.getAsJsonArray("JournalEntryLines")).hasSize(5);          // all-zero SAP-2101 line skipped

        JsonObject l1 = linea(je, "SAP-1201", "Venta RV pactada & folio123");
        assertThat(l1.get("BPLID").getAsLong()).isEqualTo(4L);
        assertThat(l1.get("U_BCS_Producto").getAsString()).isEqualTo("RV");
        assertThat(l1.get("U_BCS_GlosaExt").getAsString()).isEqualTo("Venta cartera propia");
        assertThat(l1.get("U_BCS_FecLiquid").getAsString()).isEqualTo("20250312");
        assertThat(l1.has("CostingCode")).as("SAP-1201 is ActType N").isFalse();
        assertThat(l1.has("U_RelacionCodigoSap")).as("never mapped to SAP").isFalse();
        assertThat(l1.has("FCDebit")).as("FC amounts zero -> not sent").isFalse();

        JsonObject fc = linea(je, "SAP-5101", "Solo moneda extranjera");       // Formato 'DETALLADO' (upper case)
        assertThat(fc.has("Debit")).isFalse();
        assertThat(fc.has("Credit")).isFalse();
        assertThat(fc.get("FCDebit").getAsBigDecimal()).isEqualByComparingTo("100");
        assertThat(fc.get("FCCurrency").getAsString()).isEqualTo("USD");
        assertThat(fc.get("CostingCode").getAsString()).isEqualTo("CC-X");      // ActType 'e' matched ignoring case

        JsonObject neg = linea(je, "SAP-1101", "Debe negativo");
        assertThat(neg.has("Debit")).isFalse();
        assertThat(neg.has("Credit")).isFalse();

        assertThat(r.socios()).hasSize(3);                                      // partner of the SKIPPED zero line is still created
    }

    @Test
    @DisplayName("CONT-B-21: Agrupado voucher sends only base fields (+ShortName on analytic accounts); PE -> FederalTaxID '0' for CLIENTE/CONTRAPARTE, PROVEEDOR keeps RUT; GroupCode 100/101")
    void contB21_voucherAgrupado_pe() {
        // GenerarJsonAsientoSap.java:235-264 (partners, localisation compared ignoring case), BusinessPartner.java:11-15,40-42.
        ResultadoAsientoSap r = ejecutarYComparar("CONT-B-21_sap-voucher-agrupado-pe");
        JsonObject je = entry(r, 0);
        JsonObject l = linea(je, "SAP-1201", "Clientes por cobrar");
        assertThat(l.keySet()).containsExactlyInAnyOrder("AccountCode", "Debit", "Credit", "LineMemo", "BPLID", "DueDate", "ProjectCode", "ShortName");
        JsonArray socios = JsonCanon.parse(AsientoSapMapper.toSapJson(r.socios())).getAsJsonArray();
        List<String> resumen = new ArrayList<>();
        socios.forEach(s -> {
            JsonObject o = s.getAsJsonObject();
            resumen.add(o.get("CardCode").getAsString() + "|" + o.get("CardType").getAsString() + "|" + o.get("FederalTaxID").getAsString()
                    + "|" + o.get("GroupCode").getAsString() + "|" + o.get("Address").getAsString() + "|" + o.get("Currency").getAsString());
        });
        assertThat(resumen).containsExactlyInAnyOrder(
                "CP11111111-1|cCustomer|0|100|Adress|##",
                "CL76123456-7|cCustomer|0|100|Adress|##",
                "PR22222222-2|cSupplier|22222222-2|101|Adress|##");
    }

    @Test
    @DisplayName("CONT-B-22: vouchers follow the requested id order, ids without header skipped silently, duplicates repeated, null U_BCS_IDPreComp sent as \"null\", header without lines -> []")
    void contB22_ordenFaltantesYDuplicados() {
        // GenerarJsonAsientoSap.java:177-190 (order/skip), :199 (Long + "" -> "null"), JournalEntry.java:20-22 (empty list).
        ResultadoAsientoSap r = ejecutarYComparar("CONT-B-22_sap-orden-faltantes-duplicados");
        assertThat(r.vouchers()).hasSize(3);
        assertThat(entry(r, 0).get("JdtNum").getAsLong()).isEqualTo(300L);
        assertThat(entry(r, 1).get("JdtNum").getAsLong()).isEqualTo(100L);
        assertThat(entry(r, 2).get("JdtNum").getAsLong()).isEqualTo(300L);
        assertThat(entry(r, 0).get("U_BCS_IDPreComp").getAsString()).isEqualTo("null");
        assertThat(entry(r, 1).getAsJsonArray("JournalEntryLines")).isEmpty();
    }

    @Test
    @DisplayName("CONT-B-23: empty id list -> no vouchers, no partners")
    void contB23_vacio() {
        ResultadoAsientoSap r = ejecutarYComparar("CONT-B-23_sap-vacio");
        assertThat(r.vouchers()).isEmpty();
        assertThat(r.socios()).isEmpty();
    }

    @Test
    @DisplayName("CONT-B-24: execute(JsonObject) exposes generar() as P_OUT_SOCIOS/P_OUT_VOUCHERS (legacy call() returns null - deliberate, flagged deviation)")
    void contB24_executeExponeElResultadoDeGenerar() {
        // GenerarJsonAsientoSap.java:351-355 returns null; the golden records javaCallCallDevuelveNull=true.
        CasoEnvio caso = CasoEnvio.cargar("CONT-B-20_sap-voucher-detallado-cl");
        assertThat(caso.esperado().get("javaCallCallDevuelveNull").getAsBoolean()).isTrue();
        JsonObject out = servicio(caso).execute(caso.entrada().deepCopy());
        assertThat(out.get("P_OUT_ERRORESCONTADOR").getAsLong()).isZero();
        assertThat(out.getAsJsonArray("P_OUT_MENSAJES")).isEmpty();
        JsonObject esperado = new JsonObject();
        esperado.add("P_OUT_SOCIOS", caso.esperado().get("socios"));
        esperado.add("P_OUT_VOUCHERS", caso.esperado().get("vouchers"));
        JsonObject actual = new JsonObject();
        actual.add("P_OUT_SOCIOS", out.get("P_OUT_SOCIOS"));
        actual.add("P_OUT_VOUCHERS", out.get("P_OUT_VOUCHERS"));
        JsonCanon.assertEquivalente("execute", esperado, actual, "P_OUT_SOCIOS", "JournalEntryLines");
    }

    @AfterAll
    static void reportarEjecutados() {
        System.out.println("equivalence cases executed (GenerarJsonAsientoSap): " + EJECUTADOS.get() + "/" + CASOS.size());
        assertThat(EJECUTADOS.get()).as("equivalence cases executed; zero means NOT PROVEN").isPositive();
    }
}
