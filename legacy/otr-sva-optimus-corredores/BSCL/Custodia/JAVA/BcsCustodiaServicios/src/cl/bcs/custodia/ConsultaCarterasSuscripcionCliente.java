package cl.bcs.custodia;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.ConsultarFlujoCustodia.FlujoCustodiaDetallado;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultaCarterasSuscripcionCliente implements JavaCall {

	private final Log log = new Log("CUS.ConsultaCarterasSuscripcionCliente");
	private final String SQL_SELECT_CONSULTAR_INSTRUMENTO_RELACIONADO	= loadResourceToString("cl/bcs/custodia/sql/ConsultaCarterasSuscripcionCliente_SelectInstrumentoRelacionado.sql");
	private final String SQL_SELECT_CONSULTAR_SUSCRIPCION_CLIENTE		= loadResourceToString("cl/bcs/custodia/sql/ConsultaCarterasSuscripcionCliente_SelectSuscripcionCliente.sql");
	

	public static class ConsultaSuscripcionCliente{
		public Long			IdPortafolio;
		public BigDecimal	CantidadTramite;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ConsultaInstrumentoRelacionado{
		public Long			IdInstrumentoRelacionado;
		public String		NemoRelacionado;
		public String		IdTipoInstrumentoRelacionado;
		public String		IdClasificacionTipoInstrumentoRelacionado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}		
	};
	
	public static class ConsultaCarterasSuscripcionClientePagoEntrada {
		public Long					p_IdEmpresa; 
		public Long					p_IdUsuario;
		public String				p_IdModulo;
		public Long					p_IdCliente;
		public Long					p_IdPortafolio;
		public String 				p_IdClasificacionTipoInstrumento;
		public String 				p_IdTipoInstrumento;
		public Long 				p_IdInstrumento;
		public Long 				p_Secuencia;
		public Long 				p_IdSocioNegocio;		
		public Long 				p_IdPais;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class ConsultaCarterasSuscripcionClientePagoSalida {
		public List<MensajeSalida>					P_OUT_MENSAJES;
		public BigDecimal							P_OUT_CANTIDADGARANTIAS;
		public BigDecimal							P_OUT_CANTIDADDISPONIBLE;
		public BigDecimal							P_OUT_CANTIDADTRAMITE;
		public Long									P_OUT_ERRORESCONTADOR;
		
		public String toString() {
			return Utils.printJson(this, false);
		}	
	};
	
	public ConsultaCarterasSuscripcionClientePagoSalida logica(Connection con, ConsultaCarterasSuscripcionClientePagoEntrada in) throws Exception {		
		ConsultaCarterasSuscripcionClientePagoSalida out = new ConsultaCarterasSuscripcionClientePagoSalida();
		SQL sql=new SQL();
		try {			
			
			//******************************************************************************************************************
			//												VALIDACIONES INICIALES
			//******************************************************************************************************************			
//			RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();			
//			RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
//			List<listaInstrumentoProcesado> instrumentoProcesado = new ArrayList<listaInstrumentoProcesado>();
//			List<ObjTableCoberturaSimulacion> registroCalcCobertura = new ArrayList<ObjTableCoberturaSimulacion>();
//			
//			if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
//				out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
//			};
//		
//			if (out.P_OUT_MENSAJES.size() > 0){
//				return out;
//			};
			
			//******************************************************************************************************************
			//													LOGICA
			//******************************************************************************************************************			
			// obtener fecha del sistema
			String tFechaHoraSistema   = FechaDB.ObtenerFechaHoraSistemaUTC(con, in.p_IdEmpresa);
			// obtener valor del parámetro
			HashMap<String,Object> paramsConsIns = new HashMap<>();
			List<String> IdsClaIns = new ArrayList<String>();
			List<Long> IdIns = new ArrayList<Long>();
			IdsClaIns.add( in.p_IdClasificacionTipoInstrumento);
			IdIns.add( in.p_IdInstrumento);
			paramsConsIns.put("IdInstrumento", IdIns);
			List<ConsultaInstrumentoRelacionado> infoInstrumentoRelacionado = sql.query(con, SQL_SELECT_CONSULTAR_INSTRUMENTO_RELACIONADO, paramsConsIns, ConsultaInstrumentoRelacionado.class);
			
			//Llamado a Flujo de Custodia
			List<ConsultarFlujoCustodia.FlujoUso> dato_p_uso = new ArrayList<ConsultarFlujoCustodia.FlujoUso>();
			ConsultarFlujoCustodia.FlujoUso datosFlujoUso = new ConsultarFlujoCustodia.FlujoUso();
			datosFlujoUso.UsoTipoOperacion				= "SUSOSA";
			datosFlujoUso.IdCliente						= in.p_IdCliente;
			datosFlujoUso.IdPortafolio					= in.p_IdPortafolio;
			datosFlujoUso.BusquedaEspecifica			= "";
			datosFlujoUso.IdTipoInstrumentoEspecifico	= "";
			dato_p_uso.add(datosFlujoUso);
			
			List<ConsultarFlujoCustodia.FlujoFiltrosBusqueda> dato_p_FiltrosBusqueda = new ArrayList<ConsultarFlujoCustodia.FlujoFiltrosBusqueda>();
			ConsultarFlujoCustodia.FlujoFiltrosBusqueda datosFlujoFiltrosBusqueda = new ConsultarFlujoCustodia.FlujoFiltrosBusqueda();
			for (ConsultaInstrumentoRelacionado regRela :infoInstrumentoRelacionado){
				datosFlujoFiltrosBusqueda.IdClasificacionTipoInstrumento	= regRela.IdClasificacionTipoInstrumentoRelacionado;
				datosFlujoFiltrosBusqueda.IdTipoInstrumento					= regRela.IdTipoInstrumentoRelacionado;
				datosFlujoFiltrosBusqueda.IdInstrumento						= regRela.IdInstrumentoRelacionado;
			}
			datosFlujoFiltrosBusqueda.IdEmisor							= null;
			datosFlujoFiltrosBusqueda.FechaVencimiento					= null;
			datosFlujoFiltrosBusqueda.IdTipoReajuste					= null;
			datosFlujoFiltrosBusqueda.EsFungible						= null;
			datosFlujoFiltrosBusqueda.IdBoveda							= null;
			dato_p_FiltrosBusqueda.add(datosFlujoFiltrosBusqueda);
			
			List<ConsultarFlujoCustodia.FlujoEsAgrupadoPor> dato_p_EsAgrupadoPor = new ArrayList<ConsultarFlujoCustodia.FlujoEsAgrupadoPor>();
			ConsultarFlujoCustodia.FlujoEsAgrupadoPor datosFlujoEsAgrupadoPor = new ConsultarFlujoCustodia.FlujoEsAgrupadoPor();
			datosFlujoEsAgrupadoPor.ClasificacionTipoInstrumento	= "S";
			datosFlujoEsAgrupadoPor.TipoInstrumento					= "S";
			datosFlujoEsAgrupadoPor.Instrumento						= "S";
			datosFlujoEsAgrupadoPor.Emisor							= "N";
			datosFlujoEsAgrupadoPor.Nemotecnico						= "N";
			datosFlujoEsAgrupadoPor.NemoSVS							= "N";
			datosFlujoEsAgrupadoPor.FechaEmisionInstrumento			= "N";
			datosFlujoEsAgrupadoPor.FechaVencimientoInstrumento		= "N";
			datosFlujoEsAgrupadoPor.FechaLiquidacionInstrumento		= "N";
			datosFlujoEsAgrupadoPor.TipoReajuste					= "N";
			datosFlujoEsAgrupadoPor.EsFungible						= "N";
			datosFlujoEsAgrupadoPor.CodigoIFRS						= "N";
			datosFlujoEsAgrupadoPor.Boveda							= "N";
			datosFlujoEsAgrupadoPor.Bolsa							= "N";
			datosFlujoEsAgrupadoPor.TipoOperacion					= "N";
			datosFlujoEsAgrupadoPor.Mesa							= "N";
			datosFlujoEsAgrupadoPor.FolioRegistro					= "N";
			datosFlujoEsAgrupadoPor.ItemOrigen						= "N";
			dato_p_EsAgrupadoPor.add(datosFlujoEsAgrupadoPor);
			
			ConsultarFlujoCustodia llamadoFlujoCustodia = new ConsultarFlujoCustodia();
			ConsultarFlujoCustodia.FlujoCustodiaEntrada paramInSP = new ConsultarFlujoCustodia.FlujoCustodiaEntrada(); 
			paramInSP.p_IdEmpresa			= in.p_IdEmpresa;
			paramInSP.p_IdUsuario			= in.p_IdUsuario;
			paramInSP.p_IdModulo			= in.p_IdModulo;
			paramInSP.p_FechaSaldo			= tFechaHoraSistema.substring(0, 10) ;
			paramInSP.p_IdPais				= in.p_IdPais;
			paramInSP.p_DiasProyeccion		= 0;
			paramInSP.p_DiaHabil			= "S";
			paramInSP.p_ValorizarFlujo		= "S";
			paramInSP.p_Uso					= dato_p_uso;
			paramInSP.p_FiltrosBusqueda		= dato_p_FiltrosBusqueda;
			paramInSP.p_EsAgrupadoPor		= dato_p_EsAgrupadoPor;
			
			ConsultarFlujoCustodia.FlujoCustodiaSalida salidaFlujo = llamadoFlujoCustodia.logica(con, paramInSP);
			
			if (salidaFlujo.P_OUT_ERRORESCONTADOR == null || salidaFlujo.P_OUT_ERRORESCONTADOR.equals(0L) ){
				
				//CONSULTAMOS DATOS DEL CLIENTE/SOCIO
				HashMap<String,Object> paramsConsCli = new HashMap<>();
				paramsConsCli.put("Secuencia", in.p_Secuencia);
				paramsConsCli.put("IdSocioNegocio", in.p_IdSocioNegocio);
				paramsConsCli.put("IdPortafolio", in.p_IdPortafolio);
				for (ConsultaInstrumentoRelacionado regRela :infoInstrumentoRelacionado){
					paramsConsCli.put("IdInstrumento", regRela.IdInstrumentoRelacionado);
				}
				List<ConsultaSuscripcionCliente> infoClienteSocio = sql.query(con, SQL_SELECT_CONSULTAR_SUSCRIPCION_CLIENTE, paramsConsCli, ConsultaSuscripcionCliente.class);

				BigDecimal vCantidadGarantia = new BigDecimal(0);
				BigDecimal vCantidadDisponible = new BigDecimal(0);
				for(FlujoCustodiaDetallado recorreFlujoDetallado : salidaFlujo.p_out_FlujoDetallado){
					if (recorreFlujoDetallado.ItemOrigen.equals("GARANTIA")){
						vCantidadGarantia = vCantidadGarantia.add(recorreFlujoDetallado.Cantidad);
					}else{
						vCantidadDisponible = vCantidadDisponible.add(recorreFlujoDetallado.Cantidad);
					}
				};
				
				out.P_OUT_CANTIDADTRAMITE = new BigDecimal(0);
				out.P_OUT_ERRORESCONTADOR = 0L;
				out.P_OUT_CANTIDADDISPONIBLE = vCantidadDisponible;
				out.P_OUT_CANTIDADGARANTIAS = vCantidadGarantia;
				for (ConsultaSuscripcionCliente regSuscripcion: infoClienteSocio){
					out.P_OUT_CANTIDADTRAMITE = regSuscripcion.CantidadTramite;
				}
//				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"UI_OPERACION_PROCESAR_EXITO",null,con,in.p_IdEmpresa));					
			};	


			for (MensajeSalida  AdveError: salidaFlujo.P_OUT_MENSAJES){
				out.P_OUT_MENSAJES.add(AdveError);
				out.P_OUT_ERRORESCONTADOR 	= MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			};								
						
		}
		finally {
			//algo
		}
		// SALIDA
		return out;
	};  
    
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		ConsultaCarterasSuscripcionClientePagoEntrada jin=gson.fromJson(in.toString() ,ConsultaCarterasSuscripcionClientePagoEntrada.class);
		ConsultaCarterasSuscripcionClientePagoSalida out=logica(conn,jin);
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	// MAIN
	public static void main(String[] args) throws Exception {		
		try (Connection con = PlataformaUtils.getQAPEConnection()) {
		//try (Connection con = PlataformaUtils.getDESAPEConnection()) {
		//try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":69,\"p_IdModulo\":\"CUS\",\"p_IdClasificacionTipoInstrumento\":\"RV\",\"p_IdTipoInstrumento\":\"ACN\",\"p_IdInstrumento\":177797,\"p_IdCliente\":175210,\"p_IdPortafolio\":40917,\"p_Secuencia\":1,\"p_IdSocioNegocio\":94953,\"p_IdPais\":166}";
				ConsultaCarterasSuscripcionClientePagoEntrada Objin = gson.fromJson(json, ConsultaCarterasSuscripcionClientePagoEntrada.class);
								
				ConsultaCarterasSuscripcionCliente  kk = new ConsultaCarterasSuscripcionCliente();
				ConsultaCarterasSuscripcionClientePagoSalida Objout = kk.logica(con, Objin);
				con.commit();
				
		    	}catch(Exception er) {
					er.printStackTrace();	
					con.rollback();
				}
		} catch (Exception e) {
			e.printStackTrace();			
		}
	};
	
};