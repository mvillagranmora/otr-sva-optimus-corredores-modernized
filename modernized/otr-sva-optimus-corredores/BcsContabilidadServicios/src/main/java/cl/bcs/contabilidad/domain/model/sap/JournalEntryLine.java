package cl.bcs.contabilidad.domain.model.sap;

import java.math.BigDecimal;

/**
 * SAP B1 journal entry line (legacy pojo/JournalEntryLine.java). A {@code null} component is omitted from the wire
 * JSON, which is how legacy distinguishes Agrupado lines (base fields only) from Detallado ones.
 */
public record JournalEntryLine(
        String AccountCode, BigDecimal Debit, BigDecimal Credit, String LineMemo, Long BPLID, String DueDate,
        String ProjectCode, String Reference1, String Reference2, String AdditionalReference, BigDecimal FCDebit,
        BigDecimal FCCredit, String FCCurrency, String ShortName, String ReferenceDate1, String TaxDate,
        String CostingCode, String CostingCode2, String CostingCode3, String CostingCode4, String CostingCode5,
        String U_BCS_TipoPersona, String U_BCS_Rut, String U_BCS_Portafolio, String U_BCS_Nombre,
        String U_BCS_CtaDCV, String U_BCS_EmitidoA, String U_BCS_Comprobante, String U_BCS_Folio,
        String U_BCS_NDcto, String U_BCS_Concepto, String U_BCS_FecLiquid, String U_BCS_CV, String U_BCS_Nemo,
        String U_BCS_IFRSCtaPropia, String U_BCS_Producto, String U_BCS_Bolsa, String U_BCS_Relacion,
        String U_BCS_GlosaExt, String U_BCS_FolioDTE) {}
