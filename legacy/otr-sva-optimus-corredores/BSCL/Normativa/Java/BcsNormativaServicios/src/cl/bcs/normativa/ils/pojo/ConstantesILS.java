package cl.bcs.normativa.ils.pojo;

import java.util.ArrayList;
import java.util.List;

public class ConstantesILS {
	
	public final String TIPOITEM_CTACONTABLE = "CTB";
	public final String TIPOITEM_DIGITADO = "DIG";
	public final String TIPOITEM_PROGRAMADO = "PRG";
	public final String TIPOSALDO_DEBE = "D";
	public final String TIPOSALDO_HABER = "H";
	public final String TIPOSALDO_SALDO = "S";
	public final String SIGNO_MAS = "+";
	public final String SIGNO_MENOS = "-";
	public final String CONDICION_MENOR = "<";
	public final String CONDICION_MAYOR = ">";
	public final String CONDICION_IGUAL = "=";
	public final String POSICION_NUMERADOR = "N";
	public final String POSICION_DENOMINADOR = "D";
	public final String NOMBREFUNCION_DERECHORETROCOMPRA 		= "SPDerechoRetroCompra";
	public final String NOMBREFUNCION_DERECHOMONEDAEXTRANJERA 	= "SPDerechosMonedaExtranjera";
	public final String NOMBREFUNCION_OBLIGACIONESRETROVENTA 	= "SPObligacionRetroVenta";
	public final String NOMBREFUNCION_SIMULTANEADERECHO 		= "SPSimultaneasDerechos";
	public final String NOMBREFUNCION_SIMULTANEAGARANTIA		= "SPSimultaneasGarantias";
	public final String NOMBREFUNCION_SIMULTANEAOBLIGACION		= "SPSimultaneasObligaciones";
	public final String NOMBREFUNCION_CARTERAOBLIGACION			= "SPCarteraObligaciones";
	
	public final List<String> CATEGORIAS = new ArrayList<String>();
	
	public String toString() {
		return cl.bcs.utils.Utils.printJson(this,true);		
	}
	public ConstantesILS() {
		
		CATEGORIAS.add("A");
		CATEGORIAS.add("B");
		CATEGORIAS.add("C");
		CATEGORIAS.add("D TIPO 1");
		CATEGORIAS.add("D TIPO 2");
		CATEGORIAS.add("E TIPO 1");
		CATEGORIAS.add("E TIPO 2");
	}

	
}
