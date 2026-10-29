package cl.bcs.ordenes;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ServicioCumpleRequisitosModificarTransaccion implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(ServicioCumpleRequisitosModificarTransaccion.class);
	
	private final String SQL_SELECT_TRANSACCIONES = loadResourceToString("cl/bcs/ordenes/sql/ServicioCumpleRequisitosModificarTransaccion_SelectTransacciones.sql");
	private final String SQL_SELECT_ASIGNACIONES = loadResourceToString("cl/bcs/ordenes/sql/ServicioCumpleRequisitosModificarTransaccion_SelectAsignacion.sql");
	
	public static class EntradaServicioCumpleRequisitosModificarTransaccion{
		public Long p_IdEmpresa;
		public List<InTransaccion> p_Transacciones;
		
		public EntradaServicioCumpleRequisitosModificarTransaccion() {
			this.p_Transacciones = new ArrayList<InTransaccion>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InTransaccion{
		public Long IdInterno;
		public Long IdTransaccion;
		public String IdEstado;
		public BigDecimal Cantidad;
		public BigDecimal Monto;
		public BigDecimal Precio;
		public String FechaEmision;
		public BigDecimal Tasa;
		public BigDecimal ValorRescate;
		public String AccionTipoOperacion;
		public BigDecimal TipoCambioCorresponsal;
		public Long IdCorredorCorresponsal;
		public BigDecimal MontoLiquidacionCorresponsal;
		public BigDecimal GastosCorresponsal;
		public String IdMonedaLiquidacionCorresponsal;
		
	
		public String EsBilateral;
	
		public String IdCamara;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class TransaccionReal{
		public Long IdTransaccion;
		
	
		public String IdClasificacionTipoInstrumento;
		public String IdCondicionDeLiquidacion;
		public String IdMoneda;
		public String IdBolsa;
		public String IdTipoInstrumento;
		public String IndicadorOrdenDirecta;
		

		public Long 	IdComprobanteTesoreria;
		public String 	EsBilateral;
		public String 	IdCamara;
		

		public String 	BolsaEsExtranjera;		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Asignacion{
		public Long IdTransaccion;
		public Long Count;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaServicioCumpleRequisitosModificarTransaccion{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaServicioCumpleRequisitosModificarTransaccion() {
			this.P_OUT_ERRORESCONTADOR=0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaServicioCumpleRequisitosModificarTransaccion jin = gson.fromJson(in.toString(), EntradaServicioCumpleRequisitosModificarTransaccion.class);
		SalidaServicioCumpleRequisitosModificarTransaccion out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaServicioCumpleRequisitosModificarTransaccion logica(Connection con, EntradaServicioCumpleRequisitosModificarTransaccion entrada) throws Exception {
		logger.info("INICIO::ServicioCumpleRequisitosModificarTransaccion");
		SalidaServicioCumpleRequisitosModificarTransaccion out = new SalidaServicioCumpleRequisitosModificarTransaccion();
		

		String localizacionSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.LocalizacionSistema");
		
		HashMap<Long,Object> transaccionesReales = obtenerTransacciones(con,entrada);
		HashMap<Long,Object> asignaciones = obtenerAsignaciones(con,entrada);
		
		for(InTransaccion transaccion : entrada.p_Transacciones) {
		
			out.P_OUT_MENSAJES.addAll(validarTransaccion(con,transaccion,transaccionesReales,asignaciones,entrada.p_IdEmpresa,localizacionSistema));
		}
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		logger.info("FIN::ServicioCumpleRequisitosModificarTransaccion");
		return out;
	}
	
	private HashMap<Long,Object> obtenerTransacciones(Connection con,EntradaServicioCumpleRequisitosModificarTransaccion entrada) throws Exception{
		logger.info("INICIO::ServicioCumpleRequisitosModificarTransaccion.obtenerTransacciones");
		SQL sql = new SQL();
		
		List<Long> idTransacciones = entrada.p_Transacciones.parallelStream().map(transaccion -> transaccion.IdTransaccion).collect(Collectors.toList());
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdTransaccion", idTransacciones);
		List<TransaccionReal> transaccionesReales = sql.query(con, SQL_SELECT_TRANSACCIONES, params, TransaccionReal.class);
		
		logger.info("FIN::ServicioCumpleRequisitosModificarTransaccion.obtenerTransacciones");
		return Utils.List2Hash(transaccionesReales, "IdTransaccion");
		
	}
	
	private HashMap<Long,Object> obtenerAsignaciones(Connection con,EntradaServicioCumpleRequisitosModificarTransaccion entrada) throws Exception{
		logger.info("INICIO::ServicioCumpleRequisitosModificarTransaccion.obtenerAsignaciones");
		SQL sql = new SQL();
		
		List<Long> idTransacciones = entrada.p_Transacciones.parallelStream().map(transaccion -> transaccion.IdTransaccion).collect(Collectors.toList());
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdTransaccion", idTransacciones);
		List<Asignacion> asignaciones = sql.query(con, SQL_SELECT_ASIGNACIONES, params, Asignacion.class);
		
		logger.info("FIN::ServicioCumpleRequisitosModificarTransaccion.obtenerAsignaciones");
		return Utils.List2Hash(asignaciones, "IdTransaccion");
		
	}
	
	private List<MensajeSalida> validarTransaccion(Connection con,InTransaccion transaccion,HashMap<Long,Object> transaccionesReales,HashMap<Long,Object> asignaciones,Long idEmpresa, String localizacionSistema) throws Exception{
		logger.info("INICIO::ServicioCumpleRequisitosModificarTransaccion.validarTransaccion");
		List<MensajeSalida> mensajes = new ArrayList<MensajeSalida>();
				
		if(transaccionesReales.get(transaccion.IdTransaccion) == null) {
			HashMap<String,String> params = new HashMap<String,String>();
			params.put("IdTransaccion", String.valueOf(transaccion.IdTransaccion));
			mensajes.add(new MensajeSalida(transaccion.IdTransaccion,MensajeSalida.MENSAJE_ERROR,"TRANSACCION_NO_EXISTE",params,con,idEmpresa));
		}
		
		if(transaccion.Cantidad != null && transaccion.Cantidad.compareTo(BigDecimal.ZERO) != 1) {
			HashMap<String,String> params = new HashMap<String,String>();
			params.put("IdTransaccion", String.valueOf(transaccion.IdTransaccion));
			mensajes.add(new MensajeSalida(transaccion.IdTransaccion,MensajeSalida.MENSAJE_ERROR,"CANTIDAD_MENOR_IGUAL_A_CERO",params,con,idEmpresa));
		}
		
		if("ANU".equals(transaccion.IdEstado)) {
			HashMap<String,String> params = new HashMap<String,String>();
			params.put("IdTransaccion", String.valueOf(transaccion.IdTransaccion));
			mensajes.add(new MensajeSalida(transaccion.IdTransaccion,MensajeSalida.MENSAJE_ERROR,"ERROR_MODIFICAR_TRANSACCION_ANULADA",params,con,idEmpresa));
		}
		
		if(transaccion.Precio != null && transaccion.Precio.compareTo(BigDecimal.ZERO) != 1) {
			HashMap<String,String> params = new HashMap<String,String>();
			params.put("IdTransaccion", String.valueOf(transaccion.IdTransaccion));
			mensajes.add(new MensajeSalida(transaccion.IdTransaccion,MensajeSalida.MENSAJE_ERROR,"PRECIO_MENOR_IGUAL_A_CERO",params,con,idEmpresa));
		}
		
		if(transaccion.Monto != null && transaccion.Monto.compareTo(BigDecimal.ZERO) != 1) {
			HashMap<String,String> params = new HashMap<String,String>();
			params.put("IdTransaccion", String.valueOf(transaccion.IdTransaccion));
			mensajes.add(new MensajeSalida(transaccion.IdTransaccion,MensajeSalida.MENSAJE_ERROR,"MONTO_MENOR_IGUAL_A_CERO",params,con,idEmpresa));
		}
		
		TransaccionReal infoTransaccionReal = (TransaccionReal) transaccionesReales.get(transaccion.IdTransaccion);
		//Si transaccion ya genero comprobante de tesoreria por procesamiento de clearing o liquidacion bilateral, no se puede editar Camara ni marca bilateral
		if (infoTransaccionReal.IdComprobanteTesoreria.compareTo(0L) != 0 && (!infoTransaccionReal.EsBilateral.equalsIgnoreCase(transaccion.EsBilateral)||!infoTransaccionReal.IdCamara.equalsIgnoreCase(transaccion.IdCamara)))
		{
			HashMap<String,String> params = new HashMap<String,String>();
			params.put("IdTransaccion", String.valueOf(transaccion.IdTransaccion));
			mensajes.add(new MensajeSalida(transaccion.IdTransaccion,MensajeSalida.MENSAJE_ERROR,"NO_SE_PUEDE_EDITAR_CAMARA_BILATERALIDAD_TRANSACCION_YA_PROCESADA",params,con,idEmpresa));	
		}
		

		
		//Validaciones para bilaterales. Las transacciones que cumplan para ser bilaterales denen tener la marca bilateral
		//Estas validaciones estan en CrearTransaccion.java y ServicioCumpleRequisitosModificarTransaccion.java (mantener en ambos lados igual)
		if (transaccion.EsBilateral.equalsIgnoreCase("N") && localizacionSistema.equalsIgnoreCase("CL"))
		{			
			
			
			//Transacciones de bolsas extranjeras se liquidan bilateralmente
			if (infoTransaccionReal.BolsaEsExtranjera.equalsIgnoreCase("S")){
				HashMap<String,String> params = new HashMap<String,String>();
				params.put("IdTransaccion", String.valueOf(transaccion.IdTransaccion));
				mensajes.add(new MensajeSalida(transaccion.IdTransaccion,MensajeSalida.MENSAJE_ERROR,"TRANSACCION_BOLSA_EXTRANJERA_DEBE_SER_BILATERAL",params,con,idEmpresa));
			}
			
			
			//Transacciones moneda extranjeras se liquidan bilateralmente
			if (!infoTransaccionReal.IdMoneda.equalsIgnoreCase("CLP")){
				HashMap<String,String> params = new HashMap<String,String>();
				params.put("IdTransaccion", String.valueOf(transaccion.IdTransaccion));
				mensajes.add(new MensajeSalida(transaccion.IdTransaccion,MensajeSalida.MENSAJE_ERROR,"TRANSACCION_MONEDA_EXTRANJERA_DEBE_SER_BILATERAL",params,con,idEmpresa));	
			}
			
			//Transacciones RV CLP, PH o PM no puede ser compensadas, debe ser bilaterales
			if (infoTransaccionReal.IdClasificacionTipoInstrumento.equalsIgnoreCase("RV") && 
					infoTransaccionReal.IdMoneda.equalsIgnoreCase("CLP") && 
					!infoTransaccionReal.IdTipoInstrumento.equalsIgnoreCase("SIM") && 
					infoTransaccionReal.IndicadorOrdenDirecta.equalsIgnoreCase("") &&				
					(infoTransaccionReal.IdCondicionDeLiquidacion.contentEquals("T0") ||
					infoTransaccionReal.IdCondicionDeLiquidacion.contentEquals("T1")))
			{
				HashMap<String,String> params = new HashMap<String,String>();
				params.put("IdTransaccion", String.valueOf(transaccion.IdTransaccion));
				mensajes.add(new MensajeSalida(transaccion.IdTransaccion,MensajeSalida.MENSAJE_ERROR,"TRANSACCION_DEBE_SER_BILATERAL",params,con,idEmpresa));	
			}

			
			//Transacciones de Simultanea PM no puede ser compensadas, debe ser bilaterales
			if (	infoTransaccionReal.IdClasificacionTipoInstrumento.equalsIgnoreCase("RV") && 
					infoTransaccionReal.IdMoneda.equalsIgnoreCase("CLP") && 
					infoTransaccionReal.IdTipoInstrumento.equalsIgnoreCase("SIM") && 
					infoTransaccionReal.IndicadorOrdenDirecta.equalsIgnoreCase("") &&	
					infoTransaccionReal.IdCondicionDeLiquidacion.contentEquals("T1"))
			{
				HashMap<String,String> params = new HashMap<String,String>();
				params.put("IdTransaccion", String.valueOf(transaccion.IdTransaccion));
				mensajes.add(new MensajeSalida(transaccion.IdTransaccion,MensajeSalida.MENSAJE_ERROR,"TRANSACCION_DEBE_SER_BILATERAL",params,con,idEmpresa));	
			}
			
			
			//Transacciones RF o IF CLP,  CN no puede ser compensadas, debe ser bilaterales
			if ((infoTransaccionReal.IdClasificacionTipoInstrumento.equalsIgnoreCase("RF") || infoTransaccionReal.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF")) &&				
					infoTransaccionReal.IdMoneda.equalsIgnoreCase("CLP") && 				
					infoTransaccionReal.IndicadorOrdenDirecta.equalsIgnoreCase("") &&
					infoTransaccionReal.IdCondicionDeLiquidacion.contentEquals("T2"))
			{
				HashMap<String,String> params = new HashMap<String,String>();
				params.put("IdTransaccion", String.valueOf(transaccion.IdTransaccion));
				mensajes.add(new MensajeSalida(transaccion.IdTransaccion,MensajeSalida.MENSAJE_ERROR,"TRANSACCION_DEBE_SER_BILATERAL",params,con,idEmpresa));	
			}
		} // fin validaciones para bilaterales
		

		
		if(transaccion.ValorRescate != null && transaccion.ValorRescate.compareTo(BigDecimal.ZERO) != 1) {
			HashMap<String,String> params = new HashMap<String,String>();
			params.put("IdTransaccion", String.valueOf(transaccion.IdTransaccion));
			mensajes.add(new MensajeSalida(transaccion.IdTransaccion,MensajeSalida.MENSAJE_ERROR,"VALOR_RESCATE_MENOR_IGUAL_A_CERO",params,con,idEmpresa));
		}
		
		Asignacion asignacion = (Asignacion) asignaciones.get(transaccion.IdTransaccion);
		
		if(asignacion != null && asignacion.Count > 0) {
			HashMap<String,String> params = new HashMap<String,String>();
			params.put("IdTransaccion", String.valueOf(transaccion.IdTransaccion));
			mensajes.add(new MensajeSalida(transaccion.IdTransaccion,MensajeSalida.MENSAJE_ERROR,"TRANSACCION_TIENE_ASIGNACIONES",params,con,idEmpresa));
		}
		
		if((transaccion.GastosCorresponsal.compareTo(BigDecimal.ZERO) != 0 && (transaccion.MontoLiquidacionCorresponsal.compareTo(BigDecimal.ZERO) == 0 || transaccion.TipoCambioCorresponsal.compareTo(BigDecimal.ZERO) == 0 || transaccion.IdCorredorCorresponsal.equals(0L) || "".equals(transaccion.IdMonedaLiquidacionCorresponsal)))
		   || (transaccion.MontoLiquidacionCorresponsal.compareTo(BigDecimal.ZERO) != 0 && (transaccion.GastosCorresponsal.compareTo(BigDecimal.ZERO) == 0 || transaccion.TipoCambioCorresponsal.compareTo(BigDecimal.ZERO) == 0 || transaccion.IdCorredorCorresponsal.equals(0L) || "".equals(transaccion.IdMonedaLiquidacionCorresponsal)))
		   || (transaccion.TipoCambioCorresponsal.compareTo(BigDecimal.ZERO) != 0 && (transaccion.GastosCorresponsal.compareTo(BigDecimal.ZERO) == 0 || transaccion.MontoLiquidacionCorresponsal.compareTo(BigDecimal.ZERO) == 0 || transaccion.IdCorredorCorresponsal.equals(0L) || "".equals(transaccion.IdMonedaLiquidacionCorresponsal)))
		 ) {
			HashMap<String,String> params = new HashMap<String,String>();
			params.put("IdTransaccion", String.valueOf(transaccion.IdTransaccion));
			mensajes.add(new MensajeSalida(transaccion.IdTransaccion,MensajeSalida.MENSAJE_ERROR,"DATOS_CORRESPONSAL_INCOMPLETOS",params,con,idEmpresa));
		}
		
		if(transaccion.TipoCambioCorresponsal.compareTo(BigDecimal.ZERO) != 0) {
			BigDecimal montoMonedaExtranjera = transaccion.Monto.divide(transaccion.TipoCambioCorresponsal, MathContext.DECIMAL128);
			BigDecimal epsilon = new BigDecimal("0.5");
			BigDecimal monto = "COMPRA".equals(transaccion.AccionTipoOperacion) ? transaccion.MontoLiquidacionCorresponsal.subtract(transaccion.GastosCorresponsal,MathContext.DECIMAL128) : transaccion.MontoLiquidacionCorresponsal.add(transaccion.GastosCorresponsal,MathContext.DECIMAL128);
			if(monto.subtract(montoMonedaExtranjera,MathContext.DECIMAL128).abs(MathContext.DECIMAL128).compareTo(epsilon) == 1) {
				HashMap<String,String> params = new HashMap<String,String>();
				params.put("MontoLiquidacionOk", monto.toPlainString());
				mensajes.add(new MensajeSalida(transaccion.IdTransaccion,MensajeSalida.MENSAJE_ERROR,"VALOR_LIQUIDACION_CORRESPONSAL_ERRONEO",params,con,idEmpresa));
			}
		}
		
		logger.info("FIN::ServicioCumpleRequisitosModificarTransaccion.validarTransaccion");
		return mensajes;
	}

}
