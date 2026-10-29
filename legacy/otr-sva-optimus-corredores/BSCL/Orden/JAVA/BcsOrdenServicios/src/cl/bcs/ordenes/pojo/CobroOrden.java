package cl.bcs.ordenes.pojo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import cl.bcs.plataforma.com.pe.ConsultarCobrosPeru.DefinicionRangoCobro;

public class CobroOrden {
	
	public Long 				IdCobroOrden;
	public Long 				IdGrupoCobro;
	public Long 				IdOrden;
	public String 				IdCobro;
	public BigDecimal 			ValorCobroPerfil;
	public BigDecimal 			ValorCobroFinal;
	public BigDecimal 			ValorCobroAplicado;
	public BigDecimal 			MontoCobroCalculado;
	public String 				FormaCobro;
	public String 				EsComprobante;
	public java.sql.Timestamp	FechaIngresoCobro;
	public BigDecimal 			MontoCobroCalculadoLocal;
	public BigDecimal 			MontoCobroCalculadoLiquidacion;
	public BigDecimal 			MontoMinimo;
	public String 				CodigoCobroClasificacion;
	public String 				CodigoCobroClasificacionBase;
	public String 				IdMoneda;
	public String 				IdOrigenCobro;
	public Long					IdAsociacion;
	public String				PermiteCambiarValor;
	
	public String			CheckCobroMonedaOperacion;
	public String			IdMonedaCobroOtraMoneda;
	public BigDecimal		MontoFijo_MontoCobro;
	public String			MontoFijo_CheckMontoOperacionMenor;
	public BigDecimal		MontoFijo_ValorMontoOperacionMenor;
	public String			MontoFijo_CheckSiNo;
	public BigDecimal		MontoFijo_ValorMontoSiNo;
	public BigDecimal		MontoFijoPorUnidad_MontoCobro;
	public String			MontoFijoPorUnidad_AllInCheckValor;
	public BigDecimal		PorcentualFijo_ValorCobro;
	public String			PorcentualFijo_CheckSobreMonto;
	public String			PorcentualFijo_CheckSobreCantidad;
	public String			PorcentualFijo_CheckMayorEntreDos;
	public String			PorcentualFijo_CheckMenorEntreDos;
	public String			PorcentualFijo_CheckMontoMinimo;
	public BigDecimal		PorcentualFijo_ValorMontoMinimo;
	public String			PorcentualFijo_CheckMontoMaximo;
	public BigDecimal		PorcentualFijo_ValorMontoMaximo;
	public String			PorcentualFijo_IdMonedaMinimoMaximo;
	public BigDecimal		PorcentualTasaPlazo_ValorCobro;
	public String			Otros_CheckAfectoImpuesto;
	public String			Otros_CheckPermiteAumentarValor;
	public String			Otros_CheckPermiteDisminuirValor;
	public String			Otros_CheckOperacionContado;
	public String			Otros_CheckOperacionPlazo;
	public String			Otros_CheckAllIn;
	public String			Otros_CheckInteresCompuesto;
	public String			Otros_CheckInteresSimple;
	public List<DefinicionRangoCobro> RangoCobros = new ArrayList();
	public String PorcentualRango_IdMonedaRangoMonto;
	public String PorcentualRango_IdTipoRangoCobro;
	
	public String toString() {
		return cl.bcs.utils.Utils.printJson(this,true);		
	}
}
