package cl.bcs.contabilidad.domain.model.sap;

import java.math.BigDecimal;

/**
 * One staged SAP line: a row of {@code ITZ.PreComprobanteAsientoEnvioContabilidad} and an element of the
 * EnvioContabilidad output {@code P_OUT_ASIENTOS}.
 *
 * <p>Legacy {@code EnvioContabilidad.PreComprobanteAsientoSalida} (EnvioContabilidad.java:130-227) and
 * {@code GenerarJsonAsientoSap.PreComprobanteAsiento_Proceso}. Components are the column names (see package docs).
 */
public record AsientoEnvio(
        Long IdTipoContabilidad, Long ParentKey, BigDecimal Debit, BigDecimal Credit, String U_glosa, String LineMemo,
        String ProfitCode, Long BPLId, String AccountCode, String DueDate, String Formato, String TieneProject,
        String Project, String ProductoMovimiento, String RelacionCorredora, String CodSecuencia, Long IdMovimiento,
        String Reference1, String Reference2, String AdditionalReference, BigDecimal FCDebit, BigDecimal FCCredit,
        String FCCurrency, String ShortName, String TipoSocioNegocio, String ReferenceDate1, String TaxDate,
        String CostingCode, String CostingCode2, String CostingCode3, String CostingCode4, String CostingCode5,
        String U_BCS_TipoPersona, String U_BCS_Rut, String U_BCS_Portafolio, String U_BCS_Nombre,
        String U_BCS_CtaDCV, String U_BCS_EmitidoA, String U_BCS_Comprobante, String U_BCS_Folio,
        String U_BCS_NDcto, String U_BCS_Concepto, String U_BCS_CV, String U_BCS_Nemo,
        String U_BCS_IFRSCtaPropia, String U_BCS_CodigoProducto, String U_BCS_Bolsa, String U_BCS_Relacion,
        String U_RelacionCodigoSap, String U_BCS_FolioDTE) {

    public static final String FORMATO_DETALLADO = "Detallado";
    public static final String FORMATO_AGRUPADO = "Agrupado";

    /** Legacy compares the format ignoring case (GenerarJsonAsientoSap.java:301). */
    public boolean esDetallado() {
        return FORMATO_DETALLADO.equalsIgnoreCase(Formato);
    }
}
