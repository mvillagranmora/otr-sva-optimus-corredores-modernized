package cl.bcs.plataforma.pla;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

import java.text.NumberFormat;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;

import cl.bcs.plataforma.pla.utils.FileLoader;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.plataforma.pla.utils.MonedaFormato.ObjMonedaFormato;

public class FormatearMonedaFormato implements JavaCall {
	private static final Logger logger = LogManager.getLogger(FormatearMonedaFormato.class);
	public static class InFormatearMonedaFormato{
		public  String valor;
		public String tipoFormato;
		public String idMoneda;
		public String idTipoInstrumento;
		public String idTipoOperacion;

		public InFormatearMonedaFormato(){
			valor = new String();
			tipoFormato = "GEN";
			idMoneda = "CLP";
			idTipoInstrumento = null;
			idTipoOperacion = null;
		}
	}
	public static class EntradaFormatearMonedaFormato{
		public List<InFormatearMonedaFormato> ListaMonedasFormatos;
		public Long IdEmpresa;
		public EntradaFormatearMonedaFormato(){
			IdEmpresa = 1L;
			ListaMonedasFormatos = new ArrayList<InFormatearMonedaFormato>();
			}
	}

	public static class OutFormatearMonedaFormato extends BcsPojo {

        public Long                                 P_OUT_ERRORESCONTADOR;
        public List<MensajeSalida>                  P_OUT_MENSAJES;
        public List<Resultado> 						P_OUT_RESULTADO;             
        
        public OutFormatearMonedaFormato() {
            this.P_OUT_MENSAJES = new ArrayList<>();
            this.P_OUT_ERRORESCONTADOR = 0L;
            this.P_OUT_RESULTADO = new ArrayList<Resultado>();
        }
        
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
//		public String msg;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
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
	public static String msg = null;
	
	public static Resultado formatear(Connection conn,String valor,String tipoFormato,String idMoneda) throws Exception {
		return formatear(conn,new BigDecimal(valor),tipoFormato,idMoneda,"","");
	}
	public static Resultado formatear(Connection conn,BigDecimal valor,String tipoFormato,String idMoneda) throws Exception {
		return formatear(conn,valor,tipoFormato,idMoneda,"","");
	}
	public static Resultado formatear(Connection conn,String valor,String tipoFormato,String idMoneda,String idTipoInstrumento,String idTipoOperacion ) throws Exception {
		return formatear(conn,new BigDecimal(valor),tipoFormato,idMoneda,idTipoInstrumento,idTipoOperacion);
	}
	public static Resultado formatear(Connection conn,BigDecimal valor,String tipoFormato,String idMoneda,String idTipoInstrumento,String idTipoOperacion) throws Exception {
		Resultado r = new Resultado();
		if (formatos == null) {
			SQL_SELECT_MONEDA_FORMATO = loader.loadResourceToString("cl/bcs/plataforma/pla/sql/Formato_ConsultarMonedaFormato.sql");
			SQL _sql = new SQL();
			formatos = _sql.query(conn, SQL_SELECT_MONEDA_FORMATO, new HashMap<>(), ObjMonedaFormato.class);
		}
		
		String uso = "GEN"; //siempre GEN
		if (idMoneda == null) {
			msg ="Debe especificar moneda";
			return r;
			}
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
			msg=("Formato no encontrado moneda " + idMoneda + " uso " + uso + " tipo instrumento " + idTipoInstrumento + " tipo operacion " + idTipoOperacion );
			return r;
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
//			r.msg=("tipoFormato " + tipoFormato + " no valido");
			return r;
		}
		
		BigDecimal valorFinal = BigDecimal.ZERO;
		if (truncaRedondea.equalsIgnoreCase("R")) {
			valorFinal = valor.setScale(decimales, RoundingMode.HALF_UP);
		}
		else if (truncaRedondea.equalsIgnoreCase("T")) {
			valorFinal = valor.setScale(decimales, RoundingMode.FLOOR);
		}
		else {
			msg = ("trunca o redondea invalido " + truncaRedondea + ".");
		}
		String valorFormato = "";
		NumberFormat formatoValor = NumberFormat.getCurrencyInstance(new Locale("es","CL"));
		
		try{
			valorFormato = formatoValor.format(valorFinal);
			valorFormato = valorFormato.replace("Ch$", "");
		}catch(Exception e){
			
		}
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
	
	public OutFormatearMonedaFormato logica(Connection con, EntradaFormatearMonedaFormato entrada){
		Long init = System.currentTimeMillis();
		OutFormatearMonedaFormato salida = new OutFormatearMonedaFormato();
		List<Resultado> resultado = new ArrayList<Resultado>();
		try {
			for(InFormatearMonedaFormato f : entrada.ListaMonedasFormatos) {
				Resultado Moneda_formateada = FormatearMonedaFormato.formatear(con,f.valor,f.tipoFormato , f.idMoneda, f.idTipoInstrumento, f.idTipoOperacion);
				if(Moneda_formateada.valor == null) {
					salida.P_OUT_ERRORESCONTADOR++;
					salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, msg, new HashMap<>(), con, entrada.IdEmpresa));
					salida.P_OUT_RESULTADO = null;
				}else {
					msg="Formato Moneda Exitoso";
					salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_INFORMACION, msg, new HashMap<>(), con, entrada.IdEmpresa));
					resultado.add(Moneda_formateada);
				}	
			}
			if(entrada.ListaMonedasFormatos.size()!= resultado.size()) {
				msg="Salida Con Errores";
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_INFORMACION, msg, new HashMap<>(), con, entrada.IdEmpresa));
			}
			salida.P_OUT_RESULTADO = resultado;

		}catch(Exception e){
			e.printStackTrace();
	
		}
		
		return salida;
		
	}
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = new Gson();		
		EntradaFormatearMonedaFormato in=gson.fromJson(jin.toString() ,EntradaFormatearMonedaFormato.class);
		OutFormatearMonedaFormato out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	public static void main(String[] args){
		FormatearMonedaFormato o = new FormatearMonedaFormato();
		try {
			Connection con = PlataformaUtils.getMASTER01Connection();
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdModulo\":\"NOR\",\"p_IdUsuario\":109,\"p_AccionNomina\":\"ONLINE\",\"p_TipoProceso\":\"ONLINE\",\"p_FechaDesde\":\"2019-08-01\",\"p_FechaHasta\":\"2019-08-23\",\"p_Clientes\":[{\"IdCliente\":395174,\"IdPortafolio\":343710}]}";
			String json2 = "{\"ListaMonedasFormatos\":[{\"valor\":\"0\",\"tipoFormato\":\"GEN\",\"idMoneda\":\"EUR\",\"idTipoInstrumento\":null,\"idTipoOperacion\":null},{\"valor\":\"0\",\"tipoFormato\":\"MONTO\",\"idMoneda\":\"EUR\",\"idTipoInstrumento\":null,\"idTipoOperacion\":null},{\"valor\":\"0\",\"tipoFormato\":\"MONTO\",\"idMoneda\":\"EUR\",\"idTipoInstrumento\":null,\"idTipoOperacion\":null}],\"IdEmpresa\":1}";
			EntradaFormatearMonedaFormato Objin = gson.fromJson(json2, EntradaFormatearMonedaFormato.class);		
			FormatearMonedaFormato  kk = new FormatearMonedaFormato();
			System.out.println(kk.logica(con, Objin));
		}
		catch(Exception e){
			e.printStackTrace();
		}		
	}
}


