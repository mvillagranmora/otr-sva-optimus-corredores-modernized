package cl.bcs.plataforma.com.pojo;

import java.math.BigDecimal;
import java.util.List;

public class DefinicionCobro_DB {
	public Long					IdEmpresa;
	public String				IdClaseCobro; 
	public String				IdTipoCobro; 
	public String				CodigoDefinicionCobro;
	public String				CodigoDefinicionCobroBase;
	public String				IdEstado; 
	public String				Descripcion; 
	public String				CheckCobroMonedaOperacion; 
	public String				IdMonedaCobroOtraMoneda;
	public String 				IdTipoProducto;
	public BigDecimal			MontoFijo_MontoCobro; 
	public String				MontoFijo_CheckMontoOperacionMenor; 
	public BigDecimal			MontoFijo_ValorMontoOperacionMenor; 
	public String				MontoFijo_CheckSiNo; 
	public BigDecimal			MontoFijo_ValorMontoSiNo;
	public BigDecimal 			MontoFijoPorUnidad_MontoCobro;
	public String 				MontoFijoPorUnidad_AllInCheckValor;
	public BigDecimal			PorcentualFijo_ValorCobro; 
	public String				PorcentualFijo_CheckSobreMonto; 
	public String				PorcentualFijo_CheckSobreCantidad; 
	public String				PorcentualFijo_CheckMayorEntreDos; 
	public String				PorcentualFijo_CheckMenorEntreDos; 
	public String				PorcentualFijo_CheckMontoMinimo; 
	public BigDecimal			PorcentualFijo_ValorMontoMinimo; 
	public String				PorcentualFijo_CheckMontoMaximo; 
	public BigDecimal			PorcentualFijo_ValorMontoMaximo; 
	public String				PorcentualFijo_IdMonedaMinimoMaximo; 
	public String				Otros_CheckOperacionContado; 
	public String				Otros_CheckOperacionPlazo; 
	public String				Otros_CheckBaseTasa; 
	public Long					Otros_ValorBaseTasa; 
	public String				Otros_CheckInteresSimple; 
	public String				Otros_CheckInteresCompuesto; 
	public String				Otros_CheckAllIn; 
	public BigDecimal			Otros_ValorAllInMontoOperacionBase; 
	public String				Otros_CheckAfectoImpuesto; 
	public String				Otros_CheckAplicaPortafolio; 
	public String				Otros_CheckAplicaCorresponsal; 
	public String				Otros_CheckPermiteAumentarValor; 
	public String				Otros_CheckPermiteDisminuirValor; 
	public String				PorcentualRango_IdMonedaRangoMonto; 
	public String				PorcentualRango_IdTipoRangoCobro;
	public List<DefinicionCobroRango_DB>	Rangos;
	public List<DefinicionCobroEmisor_DB> 		Emisores;
	public String				Otros_IncluyeExcluyeEmisoresCheck;
	public String				Otros_IncluyeExcluyeEmisoresValor;
	public List<DefinicionCobroInstrumento_DB> 	Instrumentos;
	public String				Otros_IncluyeExcluyeInstrumentosCheck;
	public String				Otros_IncluyeExcluyeInstrumentosValor;
	public List<DefinicionCobroTipoDeCliente_DB> 	TiposCliente;
	public String				Otros_IncluyeExcluyeTiposDeClienteCheck;
	public String				Otros_IncluyeExcluyeTiposDeClienteValor;
	public List<DefinicionCobroPortafolio_DB> 	Clientes;
	public String				Otros_IncluyeExcluyeClientesCheck;
	public String				Otros_IncluyeExcluyeClientesValor;
	public List<DefinicionCobroCorresponsal_DB> 	Corresponsales;
	public String				Otros_IncluyeExcluyeCorresponsalesCheck;
	public String				Otros_IncluyeExcluyeCorresponsalesValor;
	public BigDecimal 			PorcentualTasaPlazo_ValorCobro;
	
	
	
}
