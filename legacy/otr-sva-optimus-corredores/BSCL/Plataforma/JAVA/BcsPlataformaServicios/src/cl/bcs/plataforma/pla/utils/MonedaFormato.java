package cl.bcs.plataforma.pla.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;

import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Currency;

public class MonedaFormato {
	private static FileLoader loader = new FileLoader();
	private static String SQL_SELECT_MONEDA_FORMATO;
	private static List<ObjMonedaFormato> formatos = null;

	public static final String MONTO = "MONTO";
	public static final String CANTIDAD = "CANTIDAD";
	public static final String TASA = "TASA";
	public static final String PUNTOS = "PUNTOS";
	public static final String PRECIO = "PRECIO";
	public static final String TIPOCAMBIO = "TIPOCAMBIO";
	public static final String PARIDAD = "PARIDAD";
	
	public static Resultado formatear(Connection conn,String valor,String tipoFormato,String idMoneda) throws Exception {
		return formatear(conn,new BigDecimal(valor),tipoFormato,idMoneda,"","");
	}
	public static Resultado formatear(Connection conn,BigDecimal valor,String tipoFormato,String idMoneda) throws Exception {
		return formatear(conn,valor,tipoFormato,idMoneda,"","");
	}
	public static Resultado formatear(Connection conn,String valor,String tipoFormato,String idMoneda,String idTipoInstrumento,String idTipoOperacion) throws Exception {
		return formatear(conn,new BigDecimal(valor),tipoFormato,idMoneda,idTipoInstrumento,idTipoOperacion);
	}
	public static Resultado formatear(Connection conn,BigDecimal valor,String tipoFormato,String idMoneda,String idTipoInstrumento,String idTipoOperacion) throws Exception {
		
		if (formatos == null) {
			SQL_SELECT_MONEDA_FORMATO = loader.loadResourceToString("cl/bcs/plataforma/pla/sql/Formato_ConsultarMonedaFormato.sql");
			SQL _sql = new SQL();
			formatos = _sql.query(conn, SQL_SELECT_MONEDA_FORMATO, new HashMap<>(), ObjMonedaFormato.class);
		}
		
		String uso = "GEN"; //siempre GEN
		if (idMoneda == null)
			throw new Exception("debe especificar moneda");
		
		if (valor == null)
			valor = BigDecimal.ZERO;
		ObjMonedaFormato formato = null;
		for (ObjMonedaFormato f : formatos) {
			if (f.IdUsoMonedaFormato.equalsIgnoreCase(uso)
					&& f.IdMoneda.equalsIgnoreCase(idMoneda)
					&& f.IdTipoInstrumento.equalsIgnoreCase(idTipoInstrumento)
					&& f.IdTipoOperacion.equalsIgnoreCase(idTipoOperacion)) {
				formato = f;
				break;
			}
		}
		
		
		if (formato == null) {
			//si no encontramos el especifico
			for (ObjMonedaFormato f : formatos) {
				if (f.IdUsoMonedaFormato.equalsIgnoreCase(uso)
						&& f.IdMoneda.equalsIgnoreCase(idMoneda)
						&& f.IdTipoInstrumento.equalsIgnoreCase(idTipoInstrumento)) {
					formato = f;
					break;
				}
			}
		}
		
		if (formato == null) {
			//si no encontramos el especifico
			for (ObjMonedaFormato f : formatos) {
				if (f.IdUsoMonedaFormato.equalsIgnoreCase(uso)
						&& f.IdMoneda.equalsIgnoreCase(idMoneda)) {
					formato = f;
					break;
				}
			}
		}
		
		if (formato == null) {
			throw new Exception("formato no encontrado moneda " + idMoneda + " uso " + uso + " tipo instrumento " + idTipoInstrumento + " tipo operacion " + idTipoOperacion );
		}
		
		String truncaRedondea = null;
		Integer decimales = null;
		if (tipoFormato.equalsIgnoreCase(MONTO)) {
			truncaRedondea = formato.MontoTrucaRedondea;
			decimales = formato.MontoDecimales;
		}
		else if (tipoFormato.equalsIgnoreCase(CANTIDAD)) {
			truncaRedondea = formato.CantidadTrucaRedondea;
			decimales = formato.CantidadDecimales;
		}
		else if (tipoFormato.equalsIgnoreCase(PRECIO)) {
			truncaRedondea = formato.PrecioTrucaRedondea;
			decimales = formato.PrecioDecimales;
		}
		else if (tipoFormato.equalsIgnoreCase(TASA)) {
			truncaRedondea = formato.TasaTrucaRedondea;
			decimales = formato.TasaDecimales;
		}
		else if (tipoFormato.equalsIgnoreCase(PARIDAD)) {
			truncaRedondea = formato.ParidadTrucaRedondea;
			decimales = formato.ParidadDecimales;
		}
		else if (tipoFormato.equalsIgnoreCase(TIPOCAMBIO)) {
			truncaRedondea = formato.TipoCambioTrucaRedondea;
			decimales = formato.TipoCambioDecimales;
		}
		else if (tipoFormato.equalsIgnoreCase(PUNTOS)) {
			truncaRedondea = formato.PuntosTrucaRedondea;
			decimales = formato.PuntosDecimales;
		}
		else {
			throw new Exception("tipoFormato " + tipoFormato + " no valido");
		}
		
		BigDecimal valorFinal = BigDecimal.ZERO;
		if (truncaRedondea.equalsIgnoreCase("R")) {
			valorFinal = valor.setScale(decimales, RoundingMode.HALF_UP);
		}
		else if (truncaRedondea.equalsIgnoreCase("T")) {
			valorFinal = valor.setScale(decimales, RoundingMode.FLOOR);
		}
		else {
			throw new Exception("trunca o redondea invalido " + truncaRedondea + ".");
		}
		String valorFormato = "";
		NumberFormat formatoValor = NumberFormat.getCurrencyInstance(new Locale("es","CL"));
		formatoValor.setMinimumFractionDigits(decimales);
		formatoValor.setMaximumFractionDigits(decimales);

		try{
			valorFormato = formatoValor.format(valorFinal);
			valorFormato = valorFormato.replace("Ch$", "");
		}catch(Exception e){
			
		}
		Resultado r = new Resultado();
		r.decimales = decimales;
		r.idMoneda = formato.IdMoneda;
		r.idTipoInstrumento = formato.IdTipoInstrumento;
		r.idTipoOperacion = formato.IdTipoOperacion;
		r.truncaRedondea = truncaRedondea;
		r.uso = formato.IdUsoMonedaFormato;
		r.valor = valorFinal;
		r.valorConFormato = valorFormato;
		
		return r;
	}
	
	public static class Resultado {
		public BigDecimal valor;
		public String uso;
		public Integer decimales;
		public String truncaRedondea;
		public String idTipoInstrumento;
		public String idMoneda;
		public String idTipoOperacion;
		public String valorConFormato;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static void main(String[] args) throws Exception {
		
		Connection con = PlataformaUtils.getDESAConnection();
		Resultado R = MonedaFormato.formatear(con,"0.0001","MONTO", "USD", null, null);
		Resultado R2 = MonedaFormato.formatear(con,"0.123","MONTO", "USD", null, null);
		Resultado R3 = MonedaFormato.formatear(con,"123584653.123","MONTO", "CLP", null, null);
		Resultado R4 = MonedaFormato.formatear(con,"123584653.123","MONTO", "USD", null, null);
		System.out.println("asdñfadf");
		
	}
	
	public static class ObjMonedaFormato {
		public Long IdMonedaFormato;
		public String IdMoneda;
		public String IdUsoMonedaFormato;
		public String IdTipoInstrumento;
		public String IdTipoOperacion;
		public Integer PrecioDecimales;
		public String PrecioTrucaRedondea;
		public Integer TasaDecimales;
		public String TasaTrucaRedondea;
		public Integer CantidadDecimales;
		public String CantidadTrucaRedondea;
		public Integer MontoDecimales;
		public String MontoTrucaRedondea;
		public Integer TipoCambioDecimales;
		public String TipoCambioTrucaRedondea;
		public Integer ParidadDecimales;
		public String ParidadTrucaRedondea;
		public Integer PuntosDecimales;
		public String PuntosTrucaRedondea;
		public String IdEstado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	
}


