package cl.bcs.contabilidad.envio;

import cl.bcs.contabilidad.domain.model.sap.AsientoEnvio;
import cl.bcs.contabilidad.domain.model.sap.CuentaSap;
import cl.bcs.contabilidad.domain.model.sap.PreComprobanteEnvio;
import cl.bcs.contabilidad.domain.model.sap.ResultadoAsientoSap;
import cl.bcs.contabilidad.domain.service.AsientoSapMapper;
import cl.bcs.contabilidad.envio.support.JsonCanon;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pure rules of the SAP JournalVoucher mapping (legacy GenerarJsonAsientoSap.CrearDetalle, lines 225-347) that
 * the H2 goldens cannot pin precisely: literal number formatting on the wire, line order, and OACT lookups.
 * Inputs are written as JSON with the legacy column names (the record components carry those names).
 */
class AsientoSapMapperTest {

    private static final Gson GSON = new Gson();

    /** A staging row as the legacy defaults leave it (EnvioContabilidad.java:182-218), overridden by {@code json}. */
    private static AsientoEnvio asiento(String json) {
        JsonObject base = JsonParser.parseString("""
                {"IdTipoContabilidad":10,"ParentKey":100,"Debit":0,"Credit":0,"U_glosa":"","LineMemo":"m","ProfitCode":"",
                 "BPLId":4,"AccountCode":"SAP-1101","DueDate":"","Formato":"Agrupado","TieneProject":"","Project":"",
                 "ProductoMovimiento":"","RelacionCorredora":"","CodSecuencia":"","IdMovimiento":0,"Reference1":"",
                 "Reference2":"","AdditionalReference":"","FCDebit":0,"FCCredit":0,"FCCurrency":"","ShortName":"",
                 "TipoSocioNegocio":"","ReferenceDate1":"","TaxDate":"","CostingCode":"","CostingCode2":"","CostingCode3":"",
                 "CostingCode4":"","CostingCode5":"","U_BCS_TipoPersona":"","U_BCS_Rut":"","U_BCS_Portafolio":"",
                 "U_BCS_Nombre":"","U_BCS_CtaDCV":"","U_BCS_EmitidoA":"","U_BCS_Comprobante":"","U_BCS_Folio":"",
                 "U_BCS_NDcto":"","U_BCS_Concepto":"","U_BCS_CV":"","U_BCS_Nemo":"","U_BCS_IFRSCtaPropia":"",
                 "U_BCS_CodigoProducto":"","U_BCS_Bolsa":"","U_BCS_Relacion":"","U_RelacionCodigoSap":"","U_BCS_FolioDTE":""}
                """).getAsJsonObject();
        JsonParser.parseString(json).getAsJsonObject().entrySet().forEach(e -> base.add(e.getKey(), e.getValue()));
        return GSON.fromJson(base, AsientoEnvio.class);
    }

    private static PreComprobanteEnvio cabecera(long jdtNum) {
        return GSON.fromJson("{\"JdtNum\":" + jdtNum + ",\"ReferenceDate\":\"20250310\",\"Memo\":\"M-" + jdtNum
                + "\",\"DueDate\":\"20250310\",\"TaxDate\":\"20250310\",\"U_BCS_IDPreComp\":" + jdtNum + ",\"U_BCS_TipoCen\":200}",
                PreComprobanteEnvio.class);
    }

    private static CuentaSap cuenta(String code, String locManTran, String actType) {
        return new CuentaSap(code, locManTran, actType, (short) 1);
    }

    private static JsonArray lineas(ResultadoAsientoSap r) {
        return JsonParser.parseString(AsientoSapMapper.toSapJson(r.vouchers().get(0))).getAsJsonObject()
                .getAsJsonObject("JournalVoucher").getAsJsonObject("JournalEntry").getAsJsonArray("JournalEntryLines");
    }

    @Test
    @DisplayName("CONT-B-25: amounts go on the wire as BigDecimal.toString() of the stored value (1500.50 stays 1500.50; a scale-16 zero is 0E-16)")
    void contB25_importesSeSerializanComoBigDecimalRecibido() {
        // CallerWSJournalVoucher.java:234-236 new Gson().toJson -> BigDecimal written verbatim; the legacy batch
        // (BcsUtils SQL.java:588-590) stores amounts with setScale(16), so 0 can come back as 0E-16.
        ResultadoAsientoSap r = AsientoSapMapper.generar(List.of(100L), List.of(cabecera(100)),
                List.of(asiento("{\"Debit\":1500.50,\"Credit\":0E-16}")), List.of(), "CL");
        String json = AsientoSapMapper.toSapJson(r.vouchers().get(0));
        assertThat(json).contains("\"Debit\":1500.50").contains("\"Credit\":0E-16");
    }

    @Test
    @DisplayName("CONT-B-26: lines keep the order of the staging rows and only rows whose ParentKey equals JdtNum are used")
    void contB26_ordenDeLineasYFiltroParentKey() {
        // GenerarJsonAsientoSap.java:204-208, :229-343.
        ResultadoAsientoSap r = AsientoSapMapper.generar(List.of(100L), List.of(cabecera(100)), List.of(
                asiento("{\"LineMemo\":\"tercera\",\"Debit\":3}"),
                asiento("{\"LineMemo\":\"ajena\",\"Debit\":9,\"ParentKey\":101}"),
                asiento("{\"LineMemo\":\"primera\",\"Credit\":1}"),
                asiento("{\"LineMemo\":\"segunda\",\"Credit\":2}")), List.of(), "CL");
        assertThat(lineas(r)).extracting(l -> l.getAsJsonObject().get("LineMemo").getAsString())
                .containsExactly("tercera", "primera", "segunda");
    }

    @Test
    @DisplayName("CONT-B-27: analytic account means OACT LocManTran exactly \"Y\" (lower-case 'y' is NOT analytic: no ShortName, no partner); unknown account is neither analytic nor cost-centre")
    void contB27_cuentaAnaliticaSoloConYMayuscula() {
        // GenerarJsonAsientoSap.java:232-234 ("Y".equals, ActType equalsIgnoreCase).
        ResultadoAsientoSap r = AsientoSapMapper.generar(List.of(100L), List.of(cabecera(100)), List.of(
                asiento("{\"AccountCode\":\"A-Y\",\"Debit\":1,\"ShortName\":\"CL1-9\",\"TipoSocioNegocio\":\"CLIENTE\",\"U_BCS_Rut\":\"1-9\"}"),
                asiento("{\"AccountCode\":\"A-y\",\"Debit\":1,\"ShortName\":\"CL2-7\",\"TipoSocioNegocio\":\"CLIENTE\",\"U_BCS_Rut\":\"2-7\"}"),
                asiento("{\"AccountCode\":\"NO-EXISTE\",\"Formato\":\"Detallado\",\"Debit\":1,\"ShortName\":\"CL3-5\",\"CostingCode\":\"CC\"}")),
                List.of(cuenta("A-Y", "Y", "N"), cuenta("A-y", "y", "N")), "CL");
        JsonArray l = lineas(r);
        assertThat(l.get(0).getAsJsonObject().get("ShortName").getAsString()).isEqualTo("CL1-9");
        assertThat(l.get(1).getAsJsonObject().has("ShortName")).isFalse();
        assertThat(l.get(2).getAsJsonObject().has("ShortName")).isFalse();
        assertThat(l.get(2).getAsJsonObject().has("CostingCode")).isFalse();
        assertThat(r.socios()).extracting(s -> JsonParser.parseString(AsientoSapMapper.toSapJson(s)).getAsJsonObject().get("CardCode").getAsString())
                .containsExactly("CL1-9");
    }

    @Test
    @DisplayName("CONT-B-28: when OACT has the same AcctCode twice, the LAST row wins (HashMap.put)")
    void contB28_acctCodeDuplicadoGanaElUltimo() {
        // GenerarJsonAsientoSap.java:164-167. (EnvioContabilidad.java:376 uses Collectors.toMap and would THROW instead.)
        ResultadoAsientoSap r = AsientoSapMapper.generar(List.of(100L), List.of(cabecera(100)),
                List.of(asiento("{\"AccountCode\":\"DUP\",\"Debit\":1,\"ShortName\":\"CL1-9\",\"TipoSocioNegocio\":\"CLIENTE\"}")),
                List.of(cuenta("DUP", "N", "N"), cuenta("DUP", "Y", "N")), "CL");
        assertThat(lineas(r).get(0).getAsJsonObject().get("ShortName").getAsString()).isEqualTo("CL1-9");
        assertThat(r.socios()).hasSize(1);
    }

    @Test
    @DisplayName("CONT-B-29: one partner per analytic line, duplicates included (no de-duplication by CardCode)")
    void contB29_unSocioPorLineaAnalitica() {
        // GenerarJsonAsientoSap.java:235-264 adds to the list on every analytic line.
        ResultadoAsientoSap r = AsientoSapMapper.generar(List.of(100L), List.of(cabecera(100)), List.of(
                asiento("{\"AccountCode\":\"A-Y\",\"Debit\":1,\"ShortName\":\"CL1-9\",\"TipoSocioNegocio\":\"cliente\"}"),
                asiento("{\"AccountCode\":\"A-Y\",\"Credit\":1,\"ShortName\":\"CL1-9\",\"TipoSocioNegocio\":\"CLIENTE\"}")),
                List.of(cuenta("A-Y", "Y", "N")), "CL");
        assertThat(r.socios()).hasSize(2);
        JsonCanon.assertEquivalente("partners", JsonCanon.parse(AsientoSapMapper.toSapJson(r.socios().get(0))),
                JsonCanon.parse(AsientoSapMapper.toSapJson(r.socios().get(1))));
    }
}
