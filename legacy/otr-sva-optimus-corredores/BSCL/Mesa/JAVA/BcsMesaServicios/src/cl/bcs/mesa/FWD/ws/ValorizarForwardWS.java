package cl.bcs.mesa.FWD.ws;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.mesa.FWD.ValorizacionForward;
import cl.bcs.mesa.FWD.ValorizacionForward.ValorizacionForwardEntrada;
import cl.bcs.mesa.FWD.ValorizacionForward.ValorizacionForwardSalida;
import cl.bcs.plataforma.bat.AgregarPrograma;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

public class ValorizarForwardWS implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(ValorizarForwardWS.class);
	
	public static class EntradaValorizarForwardWS extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_Fecha;
	}
	
	public static class SalidaValorizarForwardWS extends BcsPojo{
		public Long P_OUT_ID_PROGRAMA;
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaValorizarForwardWS() {
			this.P_OUT_ID_PROGRAMA = 0L;
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaValorizarForwardWS jin = gson.fromJson(in.toString(), EntradaValorizarForwardWS.class);
		SalidaValorizarForwardWS out = logica(conn,jin);

		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaValorizarForwardWS logica(Connection con, EntradaValorizarForwardWS entrada) throws Exception {
		logger.info("INICIO::ValorizarForwardWS");
		long inicioServicio = System.currentTimeMillis();
		SalidaValorizarForwardWS out = new SalidaValorizarForwardWS();
		
		String ejecutaValorizadorEnLineaOBatch = "";
		try {
			ejecutaValorizadorEnLineaOBatch = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "MESA", "WS_VALORIZAR_FWD_EN_LINEA_O_BATCH");
		}catch (Exception ex) {
			HashMap<String,String> msjParam = new HashMap<String,String>(1);
			msjParam.put("CodigoParametro", "WS_VALORIZAR_FWD_EN_LINEA_O_BATCH");
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,"UI_INFO_PARAMETRO_NO_EXISTE",msjParam,con,entrada.p_IdEmpresa));
			ejecutaValorizadorEnLineaOBatch = "B";
			msjParam.clear();
		}
		
		if("L".equals(ejecutaValorizadorEnLineaOBatch)) {
			Long idPaisSistema = ConfiguracionSistema.getParametroConfiguracionLong(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.PaisSistema");
			ValorizacionForwardEntrada entradaValorizacionForward = new ValorizacionForwardEntrada();
			entradaValorizacionForward.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaValorizacionForward.p_IdPais = idPaisSistema;
			entradaValorizacionForward.p_IdUsuario = entrada.p_IdUsuario;
			entradaValorizacionForward.p_IdModulo = "MFW";
			entradaValorizacionForward.p_UsaReglasNegocio = "S";
			entradaValorizacionForward.p_FechaProceso = entrada.p_Fecha;
			entradaValorizacionForward.p_IdTipoCanalIngreso = "WBS";
			
			ValorizacionForward valorizacionForward = new ValorizacionForward();
			ValorizacionForwardSalida resultadoValorizacion = valorizacionForward.logica(con, entradaValorizacionForward);
			
			out.P_OUT_MENSAJES.addAll(resultadoValorizacion.P_OUT_MENSAJES);
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			if(out.P_OUT_ERRORESCONTADOR == 0L) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"UI_VALORIZACION_FORWARD_OK",new HashMap<String,String>(0),con,entrada.p_IdEmpresa));
			}
				
		} else {
			AgregarPrograma.OutAgregarPrograma salidaAgendarValorizacion = agendarValorizacion(con, entrada);
		
			if(salidaAgendarValorizacion.P_OUT_ERRORESCONTADOR > 0) {
				out.P_OUT_ERRORESCONTADOR = salidaAgendarValorizacion.P_OUT_ERRORESCONTADOR;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_AGENDAR_VALORIZACION_FORWARD", salidaAgendarValorizacion.P_OUT_MENSAJE));
				return out;
			}
			
			out.P_OUT_ID_PROGRAMA = salidaAgendarValorizacion.P_OUT_IDPROGRAMA;
			HashMap<String,String> parametros = new HashMap<String,String>();
			parametros.put("IdPrograma", String.valueOf(out.P_OUT_ID_PROGRAMA));
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"CARGA_VALORIZACION_FORWARD_CORRECTAMENTE",parametros,con,entrada.p_IdEmpresa));
		}
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ValorizarForwardWS", finServicio - inicioServicio);
		return out;
	}
	
	private AgregarPrograma.OutAgregarPrograma agendarValorizacion(Connection con, EntradaValorizarForwardWS entrada) throws Exception {
		logger.trace("INICIO::ValorizarForwardWS.agendarValorizacion");
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		List<AgregarPrograma.InAgregarPrograma.Parametro> paramsParametros = new ArrayList<AgregarPrograma.InAgregarPrograma.Parametro>();
		AgregarPrograma.InAgregarPrograma.Parametro	paramIdEmpresa = new AgregarPrograma.InAgregarPrograma.Parametro();
		AgregarPrograma.InAgregarPrograma.Parametro	paramIdUsuario = new AgregarPrograma.InAgregarPrograma.Parametro();
		AgregarPrograma.InAgregarPrograma.Parametro	paramFecha = new AgregarPrograma.InAgregarPrograma.Parametro();
		AgregarPrograma.InAgregarPrograma.Parametro	paramCanalIngreso = new AgregarPrograma.InAgregarPrograma.Parametro();
		
		paramIdEmpresa.NombreParametro = "IdEmpresa";
		paramIdEmpresa.Valor = entrada.p_IdEmpresa.toString();
		paramsParametros.add(paramIdEmpresa);
		
		paramIdUsuario.NombreParametro = "IdUsuario";
		paramIdUsuario.Valor = entrada.p_IdUsuario.toString();
		paramsParametros.add(paramIdUsuario);
		
		paramFecha.NombreParametro = "Fecha";
		paramFecha.Valor = entrada.p_Fecha.toString();
		paramsParametros.add(paramFecha);

		paramCanalIngreso.NombreParametro = "IdTipoCanalIngreso";
		paramCanalIngreso.Valor = "WBS";
		paramsParametros.add(paramCanalIngreso);
		
		AgregarPrograma.InAgregarPrograma paramsAgregarPrograma = new AgregarPrograma.InAgregarPrograma(); 
		paramsAgregarPrograma.p_IdEmpresa = entrada.p_IdEmpresa;
		paramsAgregarPrograma.p_IdModulo = "MFW";
		paramsAgregarPrograma.p_IdUsuario = entrada.p_IdUsuario;
		paramsAgregarPrograma.p_IdTipoPrograma	= "VALORIZAR_FORWARD";
		paramsAgregarPrograma.p_Segundos = 0L;
		paramsAgregarPrograma.p_FechaSistema = fechaSistema;
		paramsAgregarPrograma.p_Parametros = paramsParametros;
		AgregarPrograma agregarPrograma = new AgregarPrograma();
		AgregarPrograma.OutAgregarPrograma salidaAgregarPrograma = agregarPrograma.logica(con, paramsAgregarPrograma);
		
		logger.trace("FIN::ValorizarForwardWS.agendarValorizacion");
		return salidaAgregarPrograma;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");		                  
		try (Connection con = PlataformaUtils.getPropertiesConnection("qa02")){
			Gson gson = Utils.getGsonBuilder();			
			
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":\"9576\",\"p_Fecha\":\"2026-05-28\"}";
			EntradaValorizarForwardWS Objin = gson.fromJson(json, EntradaValorizarForwardWS.class);
		ValorizarForwardWS demo = new ValorizarForwardWS();				
		try {
			SalidaValorizarForwardWS ObjOut = demo.logica(con, Objin);
			con.rollback();
			System.out.println(ObjOut.toString());
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}
						
    	};
		System.out.println("== END ==");
	}

}
