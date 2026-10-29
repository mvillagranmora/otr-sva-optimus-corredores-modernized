
package cl.bcs.contabilidad.pojo;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

import cl.bcs.utils.Utils;

public class JournalEntryLine {

    public String AccountCode;
    public BigDecimal Debit;
    public BigDecimal Credit;
    public String LineMemo;
    public Object BPLID;
    public String DueDate;
    public Object ProjectCode;
    //Agregar nuevos parametros

    public String Reference1;
    public String Reference2;
    public String AdditionalReference;

    public BigDecimal FCDebit;
    public BigDecimal FCCredit;
    public String FCCurrency;
    public String ShortName;
    public String ReferenceDate1;
    public String TaxDate;
   
    public String CostingCode;
    public String CostingCode2;
    public String CostingCode3;
    public String CostingCode4;
    public String CostingCode5;
    public String U_BCS_TipoPersona;
    public String U_BCS_Rut;
    public String U_BCS_Portafolio;
    public String U_BCS_Nombre;
    public String U_BCS_CtaDCV;
    public String U_BCS_EmitidoA;
    public String U_BCS_Comprobante;
    public String U_BCS_Folio;
    public String U_BCS_NDcto;
    public String U_BCS_Concepto;
    public String U_BCS_FecLiquid;
   
    public String U_BCS_CV;
    public String U_BCS_Nemo;
    public String U_BCS_IFRSCtaPropia;
    public String U_BCS_Producto;
    public String U_BCS_Bolsa;
    public String U_BCS_Relacion;
    public String U_BCS_GlosaExt;
    
    public String U_BCS_FolioDTE;
       
    public String toString() {
		return Utils.printJson(this, false);
	}

}
