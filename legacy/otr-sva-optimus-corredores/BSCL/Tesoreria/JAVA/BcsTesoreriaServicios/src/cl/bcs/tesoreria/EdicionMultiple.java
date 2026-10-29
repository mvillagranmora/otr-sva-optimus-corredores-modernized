package cl.bcs.tesoreria;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.tesoreria.LiquidarComprobante.ComprobanteLiquidar_Entrada;
import cl.bcs.tesoreria.LiquidarComprobante.FormaDePagoLiquidar_Entrada;
import cl.bcs.tesoreria.LiquidarComprobante.LiquidarComprobante_Entrada;
import cl.bcs.tesoreria.LiquidarComprobante.LiquidarComprobante_Salida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class EdicionMultiple implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(EdicionMultiple.class);
	
	private final String SQL_CALL_EDICION_MULTIPLE = loadResourceToString("cl/bcs/tesoreria/sql/EdicionMultiple_CallEdicionMultiple.sql");
	private final String SQL_CREATE_TABLE_FORMA_PAGO = loadResourceToString("cl/bcs/tesoreria/sql/EdicionMultiple_CreateTableFormaPago.sql");
	private final String SQL_CREATE_TABLE_REGISTRO = loadResourceToString("cl/bcs/tesoreria/sql/EdicionMultiple_CreateTableRegistro.sql");
	private final String SQL_DROP_TABLE_FORMA_PAGO = loadResourceToString("cl/bcs/tesoreria/sql/EdicionMultiple_DropTableFormaPago.sql");
	private final String SQL_DROP_TABLE_REGISTRO = loadResourceToString("cl/bcs/tesoreria/sql/EdicionMultiple_DropTableRegistro.sql");
	private final String SQL_INSERT_TABLE_FORMA_PAGO = loadResourceToString("cl/bcs/tesoreria/sql/EdicionMultiple_InsertTableFormaPago.sql");
	private final String SQL_INSERT_TABLE_REGISTRO = loadResourceToString("cl/bcs/tesoreria/sql/EdicionMultiple_InsertTableRegistro.sql");
	private final String SQL_SELECT_MENSAJE_SALIDA_EDICION_MULTIPLE = loadResourceToString("cl/bcs/tesoreria/sql/EdicionMultiple_SelectMensajeSalidaEdicionMultiple.sql");
	
	public static class EntradaEdicionMultiple extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_FechaMovimiento;
		public String p_Entidad;
		public String p_LiquidaComprobante;
		public List<FormaDePago> p_FormasDePago;
		public List<RegistroAProcesar> p_RegistrosAProcesar;
		
		public EntradaEdicionMultiple() {
			this.p_FormasDePago = new ArrayList<FormaDePago>();
			this.p_RegistrosAProcesar = new ArrayList<RegistroAProcesar>();
		}
	}
	
	public static class FormaDePago extends BcsPojo {
		public Long NumeroReferencia;
		public String IdMedioDePago;
		public String IdModalidadDePago;
		public String IdMoneda;
		public BigDecimal Monto;
		public Long IdCuentaBancariaCorredora;
		public Long IdTarjetaBancariaCorredora;
		public Long IdChequera;
		public Long NumeroDocumentoCorredora;
		public String FechaDocumentoPagoCorredora;
		public Long IdCuentaBancariaSocio;
		public Long IdBancoSocio;
		public String NombreCuentaBancariaSocio;
		public String NumeroCuentaBancariaSocio;
		public String IdTipoCuentaBancariaSocio;
		public String NombreTipoCuentaBancariaSocio;
		public Long IdTarjetaBancariaSocio;
		public String IdTipoTarjetaBancariaSocio;
		public String NombreTipoTarjetaBancariaSocio;
		public String NumeroTarjetaBancariaSocio;
		public String NumeroSerieSocio;
		public Long NumeroDocumentoSocio;
		public String FechaDocumentoPagoSocio;
	}
	
	public static class RegistroAProcesar extends BcsPojo {
		public Long Id;
		public String Entidad;
		public String Accion;
	}
	
	public static class SalidaEdicionMultiple extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<FormaDePagoComprobante> P_OUT_FORMADEPAGOCOMPROBANTE;
		
		public SalidaEdicionMultiple() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	}
	
	public static class FormaDePagoComprobante extends BcsPojo {
		public Long IdComprobante;
		public Long IdComprobanteFormaDePago;
		public String IdTipoMovimiento;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaEdicionMultiple jin = gson.fromJson(in.toString(), EntradaEdicionMultiple.class);
		SalidaEdicionMultiple out = logica(conn,jin);

		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaEdicionMultiple logica(Connection con, EntradaEdicionMultiple entrada) throws Exception {
		logger.info("INICIO::EdicionMultiple");
		SalidaEdicionMultiple out = new SalidaEdicionMultiple();
		
		SalidaEdicionMultiple salidaEdicionMultiple = editarMultiple(con, entrada);
		out.P_OUT_ERRORESCONTADOR += salidaEdicionMultiple.P_OUT_ERRORESCONTADOR;
		out.P_OUT_MENSAJES.addAll(salidaEdicionMultiple.P_OUT_MENSAJES);
		out.P_OUT_FORMADEPAGOCOMPROBANTE = salidaEdicionMultiple.P_OUT_FORMADEPAGOCOMPROBANTE;
		
		Long erroresYAdvertencias = salidaEdicionMultiple.P_OUT_MENSAJES.stream()
				.filter(mensaje-> MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo) 
						|| MensajeSalida.MENSAJE_ADVERTENCIA.equals(mensaje.tipo) 
						|| MensajeSalida.MENSAJE_AUTORIZACION.equals(mensaje.tipo))
				.count();
		
		if("S".equals(entrada.p_LiquidaComprobante) && erroresYAdvertencias.equals(0L)) {
			LiquidarComprobante_Salida salidaLiquidacionComprobante = liquidarComprobantes(con, entrada, salidaEdicionMultiple.P_OUT_FORMADEPAGOCOMPROBANTE);
			out.P_OUT_ERRORESCONTADOR += salidaLiquidacionComprobante.P_OUT_ERRORESCONTADOR != null ? salidaLiquidacionComprobante.P_OUT_ERRORESCONTADOR : MensajeSalida.getNumeroErrores(salidaLiquidacionComprobante.P_OUT_MENSAJES);
			out.P_OUT_MENSAJES.addAll(salidaLiquidacionComprobante.P_OUT_MENSAJES);
		}
		
		logger.info("FIN::EdicionMultiple");
		return out;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private SalidaEdicionMultiple editarMultiple(Connection con, EntradaEdicionMultiple entrada) throws Exception {
		logger.debug("INICIO::EdicionMultiple.editarMultiple");
		SalidaEdicionMultiple out = new SalidaEdicionMultiple();
		
		HashMap<String, Object> parametrosEntrada = new HashMap<String, Object>();
		parametrosEntrada.put("p_IdEmpresa", entrada.p_IdEmpresa);
		parametrosEntrada.put("p_IdUsuario", entrada.p_IdUsuario);
		parametrosEntrada.put("p_IdModulo", entrada.p_IdModulo);
		parametrosEntrada.put("p_UsaReglasNegocio", entrada.p_UsaReglasNegocio);
		parametrosEntrada.put("p_FechaMovimiento", entrada.p_FechaMovimiento);
		parametrosEntrada.put("p_Entidad", entrada.p_Entidad);
		parametrosEntrada.put("p_LiquidaComprobante", "N");
		
		HashMap<String, Class> parametrosSalida = new HashMap<String, Class>();
		parametrosSalida.put("p_out_InformacionContador", Long.class);
	    parametrosSalida.put("p_out_AdvertenciasContador", Long.class);
	    parametrosSalida.put("p_out_ErroresContador", Long.class);
	    parametrosSalida.put("p_out_FormaDePagoComprobante", FormaDePagoComprobante.class);
	    
	    SQL sql = new SQL();
	    try {
	    	SQL.exec(con, SQL_CREATE_TABLE_FORMA_PAGO);
	    	SQL.exec(con, SQL_CREATE_TABLE_REGISTRO);
	    	sql.batch(con, SQL_INSERT_TABLE_FORMA_PAGO, entrada.p_FormasDePago, FormaDePago.class);
	    	sql.batch(con, SQL_INSERT_TABLE_REGISTRO, entrada.p_RegistrosAProcesar, RegistroAProcesar.class);
	    	HashMap<String, Object> respuestaEdicionMultiple = sql.call(con, SQL_CALL_EDICION_MULTIPLE, parametrosEntrada, parametrosSalida);
	    	
	    	out.P_OUT_ERRORESCONTADOR += (Long) respuestaEdicionMultiple.get("p_out_ErroresContador");
		    out.P_OUT_FORMADEPAGOCOMPROBANTE = (List<FormaDePagoComprobante>) respuestaEdicionMultiple.get("p_out_FormaDePagoComprobante");
		    out.P_OUT_MENSAJES = sql.query(con, SQL_SELECT_MENSAJE_SALIDA_EDICION_MULTIPLE, new HashMap<String, Object>(1), MensajeSalida.class);
	    } catch(Exception e) {
	    	throw e;
	    } finally {
	    	SQL.exec(con, SQL_DROP_TABLE_FORMA_PAGO);
	    	SQL.exec(con, SQL_DROP_TABLE_REGISTRO);
	    }
	    
		logger.debug("FIN::EdicionMultiple.editarMultiple");
		return out;
	}
	
	private LiquidarComprobante_Salida liquidarComprobantes(Connection con, EntradaEdicionMultiple entrada, List<FormaDePagoComprobante> formasDePagoComprobante) throws Exception {
		logger.debug("INICIO::EdicionMultiple.liquidarComprobantes");
		LiquidarComprobante_Salida salidaLiquidarComprobante = new LiquidarComprobante_Salida();
		
		LiquidarComprobante_Entrada entradaLiquidarComprobante = new LiquidarComprobante_Entrada();
		entradaLiquidarComprobante.p_IdEmpresa = entrada.p_IdEmpresa;  				 
		entradaLiquidarComprobante.p_IdUsuario = entrada.p_IdUsuario; 				   
		entradaLiquidarComprobante.p_IdModulo = entrada.p_IdModulo; 				
		entradaLiquidarComprobante.p_SimularOperacion = "N"; 	 
		entradaLiquidarComprobante.p_UsaReglasNegocio = "S";			
		entradaLiquidarComprobante.p_FechaMovimiento = entrada.p_FechaMovimiento;
		entradaLiquidarComprobante.p_ComprobanteLiquidar = formasDePagoComprobante.stream().map(formaDePago->{
			ComprobanteLiquidar_Entrada entradaComprobanteLiquidar = new ComprobanteLiquidar_Entrada();
			entradaComprobanteLiquidar.IdComprobante = formaDePago.IdComprobante;
			entradaComprobanteLiquidar.IdTipoMovimiento = formaDePago.IdTipoMovimiento;
			entradaComprobanteLiquidar.AfectaCuentaInversion = "CLI";
			return entradaComprobanteLiquidar;
		}).collect(Collectors.toList());
		entradaLiquidarComprobante.p_FormaDePagoLiquidar = formasDePagoComprobante.stream().map(formaDePago->{
			FormaDePagoLiquidar_Entrada entradaFormaPagoLiquidar = new FormaDePagoLiquidar_Entrada();
			entradaFormaPagoLiquidar.IdComprobante = formaDePago.IdComprobante;
			entradaFormaPagoLiquidar.IdComprobanteFormaDePago = formaDePago.IdComprobanteFormaDePago;
			return entradaFormaPagoLiquidar;
		}).collect(Collectors.toList());
		
		LiquidarComprobante liquidarComprobante = new LiquidarComprobante();
		salidaLiquidarComprobante = liquidarComprobante.logica(con, entradaLiquidarComprobante);
		
		logger.debug("FIN::EdicionMultiple.liquidarComprobantes");
		return salidaLiquidarComprobante;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("becbpro1", "connections.properties")) {
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
					
		String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":165114,\"p_IdModulo\":\"TES\",\"p_UsaReglasNegocio\":\"S\",\"p_LiquidaComprobante\":\"S\",\"p_FechaMovimiento\":\"2026-09-11\",\"p_Entidad\":\"TES.Comprobante\",\"p_FormasDePago\":[{\"NumeroReferencia\":13660,\"IdMedioDePago\":\"TRA\",\"IdModalidadDePago\":\"MODDEP\",\"IdMoneda\":\"USD\",\"Monto\":366068.94,\"IdCuentaBancariaCorredora\":3204,\"IdTarjetaBancariaCorredora\":0,\"IdChequera\":0,\"NumeroDocumentoCorredora\":0,\"FechaDocumentoPagoCorredora\":\"2026-09-11\",\"IdCuentaBancariaSocio\":null,\"IdBancoSocio\":null,\"NombreCuentaBancariaSocio\":null,\"NumeroCuentaBancariaSocio\":\"\",\"IdTipoCuentaBancariaSocio\":null,\"NombreTipoCuentaBancariaSocio\":null,\"IdTarjetaBancariaSocio\":null,\"IdTipoTarjetaBancariaSocio\":null,\"NombreTipoTarjetaBancariaSocio\":null,\"NumeroTarjetaBancariaSocio\":null,\"NumeroSerieSocio\":null,\"NumeroDocumentoSocio\":null,\"FechaDocumentoPagoSocio\":\"2026-09-11\"}],\"p_RegistrosAProcesar\":[{\"Id\":13660,\"Entidad\":\"TES.Comprobante\",\"Accion\":\"AGN\"},{\"Id\":13660,\"Entidad\":\"TES.Comprobante\",\"Accion\":\"NTN\"}]}";

		EntradaEdicionMultiple Objin = gson.fromJson(json, EntradaEdicionMultiple.class);
		EdicionMultiple demo = new EdicionMultiple();				
		try {
			//SQL.exec(con,"UPDATE \"BSCL.Tesoreria.BD::TES.Comprobante\" SET \"IdEstado\" = 'ING' WHERE \"IdComprobante\" = 13660 ");
			SalidaEdicionMultiple ObjOut = demo.logica(con, Objin);
			if(ObjOut.P_OUT_ERRORESCONTADOR.equals(0L)) {
				con.rollback();
			}else {
				con.rollback();
			}
			
			con.rollback();
			System.out.println(ObjOut.toString());
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}
			con.rollback(); 			
			//con.commit();
    	}
		System.out.println("== END ==");
	}
	
}
