package cl.bcs.custodia.evc;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.custodia.evc.ProcesarEventoCapitalCambioDeNemos.InstrumentoNuevo;
import cl.bcs.custodia.pojo.EventoCapital;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.utils.BcsPojo;


public class CerrarEventoCapitalCambioDeNemo implements JavaCall{
	
	private static final Logger logger = LogManager.getLogger(CerrarEventoCapitalCambioDeNemo.class);

	
	private final String SQL_SELECT_EVENTO_CAPITAL					= loadResourceToString("cl/bcs/custodia/sql/CerrarEventoCapitalCambioDeNemo_SelectEventoCapital.sql");
	private final String SQL_UPDATE_EVENTO_CAPITAL 					= loadResourceToString("cl/bcs/custodia/sql/CerrarEventoCapitalCambioDeNemo_UpdateEventoCapital.sql");
	private final String SQL_UPDATE_EVENTO_CAPITAL_CLIENTE 			= loadResourceToString("cl/bcs/custodia/sql/CerrarEventoCapitalCambioDeNemo_UpdateEventoCapitalCliente.sql");
	private final String SQL_UPDATE_CARTERA 						= loadResourceToString("cl/bcs/custodia/sql/CambioNemos_UpdateCartera.sql");
	private final String SQL_UPDATE_CARTERA_NEGATIVA 				= loadResourceToString("cl/bcs/custodia/sql/CambioNemos_UpdateCarteraNegativa.sql");
	private final String SQL_UPDATE_GARANTIA 						= loadResourceToString("cl/bcs/custodia/sql/CambioNemos_UpdateGarantia.sql");
	private final String SQL_UPDATE_MOVIMIENTO_COMPROBANTE_SIMULTANEA 			= loadResourceToString("cl/bcs/custodia/sql/CambioNemos_UpdateMovimientoComprobanteSimultanea.sql");
	private final String SQL_UPDATE_SUBYACENTE 						= loadResourceToString("cl/bcs/custodia/sql/CambioNemos_UpdateSubyacente.sql");
	
	
	private final String SQL_UPDATE_CARTERA_DE_GARANTIA 			= loadResourceToString("cl/bcs/custodia/sql/CerrarEventoCapitalCambioDeNemo_UpdateCarteraDeGarantia.sql");
	private final String SQL_UPDATE_CARTERA_DE_SUBYACENTE			= loadResourceToString("cl/bcs/custodia/sql/CerrarEventoCapitalCambioDeNemo_UpdateCarteraDeSubyacente.sql");
	private final String SQL_UPDATE_INSTRUMENTO_ANTERIOR 			= loadResourceToString("cl/bcs/custodia/sql/CerrarEventoCapitalCambioDeNemo_UpdateInstrumentoAnterior.sql");

	public static class CerrarEventoCapitalCambioDeNemoEntrada extends BcsPojo {
		
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String	p_IdModulo;
		public Long 	p_IdEventoCapital;
		public String 	p_IdTipoEventoCapital;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	};

	public static class CerrarEventoCapitalCambioDeNemoSalida extends BcsPojo {
		
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		
		public CerrarEventoCapitalCambioDeNemoSalida (){
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}	
		
	};
	
	
	
	public CerrarEventoCapitalCambioDeNemoSalida logica(Connection con, CerrarEventoCapitalCambioDeNemoEntrada in) throws Exception {
		
		Long init = System.currentTimeMillis();
		logger.info("INICIO::CerrarEventoCapitalCambioDeNemo");
		
		SQL sql 								  	= new SQL();
		CerrarEventoCapitalCambioDeNemoSalida out 	= new CerrarEventoCapitalCambioDeNemoSalida();		
		HashMap<String,Object> params 				= new HashMap<String,Object>();
		RevisarRequisitosBasicos basicos 			= new RevisarRequisitosBasicos();		
		
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
		out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
	
		if ( MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0 ) {
			
			return out;
			
		}		
		
		params.put("IdEventoCapital", in.p_IdEventoCapital);	
		params.put("TipoEventoCapital", in.p_IdTipoEventoCapital);
		
		List<EventoCapital> eventoCapital = sql.query(con, SQL_SELECT_EVENTO_CAPITAL, params, EventoCapital.class);

		if ( eventoCapital.size() == 0 ) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_EVENTO_CAPITAL_NO_EXISTE", new HashMap<String,String>() , con, in.p_IdEmpresa));
			return out;
		}
								
		List<InstrumentoNuevo> updateInstrumentos = new ArrayList<>();
		for ( EventoCapital ec: eventoCapital ) {
			
			InstrumentoNuevo nuevoInstrumento = new InstrumentoNuevo();
			nuevoInstrumento.p_IdInstrumento 		= ec.IdInstrumento;
			nuevoInstrumento.p_IdInstrumentoNuevo 	= ec.IdInstrumentoNuevo;
			nuevoInstrumento.p_NemoNuevo			= ec.NemoNuevo;
			nuevoInstrumento.p_Emisor				= ec.IdEmisorNuevo;
			nuevoInstrumento.p_ISIN					= ec.ISIN;
			updateInstrumentos.add(nuevoInstrumento);
			
		}
		
		actulizaMovimientoComprobanteSimultanea(updateInstrumentos, con);
		actulizaCarteras(updateInstrumentos, con);
		actulizaCarterasNegativas(updateInstrumentos, con);
		actulizaSubyacente(updateInstrumentos, con);
		actulizaGarantia(updateInstrumentos, con);
		actulizaEventoCapital(eventoCapital, con);
		actulizaEventoCapitalClientes(eventoCapital, con);
		
		String DesactivaInstrumento = "N";
		try {
			DesactivaInstrumento = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "CUSTODIA", "DesactivaInstrumentoCambioNemo");
		}
		catch(Exception ex)
		{
			DesactivaInstrumento    	= "N";
		}
		
		if("S".equalsIgnoreCase(DesactivaInstrumento))
		{
			actulizaInstrumentoAnterior(updateInstrumentos, con);
		}
		
		out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"UI_OPERACION_CERRAR_EXITO",new HashMap<String,String>(), con, in.p_IdEmpresa));

		logger.info("FIN::CerrarEventoCapitalCambioDeNemo, " + (System.currentTimeMillis() - init));
		return out;
		
	}
	
	public void actulizaCarteras ( List<InstrumentoNuevo> intrumento, Connection con ) throws Exception {
		
		Long init = System.currentTimeMillis();
		
		logger.debug("INICIO::CerrarEventoCapitalCambioDeNemo.actulizaCarteras");
		SQL sql = new SQL();
		sql.batch(con, SQL_UPDATE_CARTERA, intrumento, InstrumentoNuevo.class);
		logger.debug("FIN::CerrarEventoCapitalCambioDeNemo.actulizaCarteras, " + (System.currentTimeMillis() - init));
		
	}
	
	public void actulizaCarterasNegativas ( List<InstrumentoNuevo> intrumento, Connection con ) throws Exception {
		Long init = System.currentTimeMillis();
		logger.debug("INICIO::CerrarEventoCapitalCambioDeNemo.actulizaCarterasNegativas");
		SQL sql = new SQL();
		sql.batch(con, SQL_UPDATE_CARTERA_NEGATIVA, intrumento, InstrumentoNuevo.class);
		logger.debug("FIN::CerrarEventoCapitalCambioDeNemo.actulizaCarterasNegativas, " + (System.currentTimeMillis() - init));
		
	}

	public void actulizaSubyacente ( List<InstrumentoNuevo> intrumento, Connection con ) throws Exception {
		
		Long init = System.currentTimeMillis();
		logger.debug("INICIO::CerrarEventoCapitalCambioDeNemo.actulizaSubyacente");
		SQL sql = new SQL();
		sql.batch(con, SQL_UPDATE_CARTERA_DE_SUBYACENTE, intrumento, InstrumentoNuevo.class);
		sql.batch(con, SQL_UPDATE_SUBYACENTE, intrumento, InstrumentoNuevo.class);
		logger.debug("FIN::CerrarEventoCapitalCambioDeNemo.actulizaSubyacente, " + (System.currentTimeMillis() - init));

		
	}
	
	public void actulizaMovimientoComprobanteSimultanea ( List<InstrumentoNuevo> intrumento, Connection con ) throws Exception {
		
		Long init = System.currentTimeMillis();
		logger.debug("INICIO::CerrarEventoCapitalCambioDeNemo.actulizaMovimientoComprobanteSimultanea");
		SQL sql = new SQL();
		sql.batch(con, SQL_UPDATE_MOVIMIENTO_COMPROBANTE_SIMULTANEA, intrumento, InstrumentoNuevo.class);
		logger.debug("FIN::CerrarEventoCapitalCambioDeNemo.actulizaMovimientoComprobanteSimultanea, " + (System.currentTimeMillis() - init));
		
	}
	
	public void actulizaGarantia ( List<InstrumentoNuevo> intrumento, Connection con ) throws Exception {
		
		Long init = System.currentTimeMillis();
		logger.debug("INICIO::CerrarEventoCapitalCambioDeNemo.actulizaGarantia");
		SQL sql = new SQL();
		sql.batch(con, SQL_UPDATE_CARTERA_DE_GARANTIA, intrumento, InstrumentoNuevo.class);
		sql.batch(con, SQL_UPDATE_GARANTIA, intrumento, InstrumentoNuevo.class);
		logger.debug("FIN::CerrarEventoCapitalCambioDeNemo.actulizaGarantia, " + (System.currentTimeMillis() - init));

	}
	
	public void actulizaEventoCapital ( List<EventoCapital> eventoCapital, Connection con ) throws Exception {
		
		Long init = System.currentTimeMillis();
		logger.debug("INICIO::CerrarEventoCapitalCambioDeNemo.actulizaEventoCapital");
		SQL sql = new SQL();
		sql.batch(con, SQL_UPDATE_EVENTO_CAPITAL, eventoCapital, EventoCapital.class);	
		logger.debug("FIN::CerrarEventoCapitalCambioDeNemo.actulizaEventoCapital, " + (System.currentTimeMillis() - init));

	}
	
	public void actulizaEventoCapitalClientes ( List<EventoCapital> eventoCapital, Connection con ) throws Exception {
		
		Long init = System.currentTimeMillis();
		logger.debug("INICIO::CerrarEventoCapitalCambioDeNemo.actulizaEventoCapitalClientes");
		SQL sql = new SQL();
		sql.batch(con, SQL_UPDATE_EVENTO_CAPITAL_CLIENTE, eventoCapital, EventoCapital.class);
		logger.debug("FIN::CerrarEventoCapitalCambioDeNemo.actulizaEventoCapitalClientes, " + (System.currentTimeMillis() - init));
		
	}
	
	public void actulizaInstrumentoAnterior ( List<InstrumentoNuevo> intrumento, Connection con ) throws Exception {
		
		Long init = System.currentTimeMillis();		
		logger.debug("INICIO::CerrarEventoCapitalCambioDeNemo.actulizaInstrumentoAnterior");
		SQL sql = new SQL();
		sql.batch(con, SQL_UPDATE_INSTRUMENTO_ANTERIOR, intrumento, InstrumentoNuevo.class);
		logger.debug("FIN::CerrarEventoCapitalCambioDeNemo.actulizaInstrumentoAnterior, " + (System.currentTimeMillis() - init));
		
	}
	

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		
		CerrarEventoCapitalCambioDeNemoEntrada jin = gson.fromJson(in.toString(), CerrarEventoCapitalCambioDeNemoEntrada.class);		
		CerrarEventoCapitalCambioDeNemoSalida out = logica(conn,jin);
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	

	public static void main(String[] args) throws Exception 
	{
		System.out.println("== INICIO PRUEBA LOCAL ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection()) 
		{
			try 
			{	
				Gson gson = Utils.getGsonBuilder();
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"CUS\",\"p_IdEventoCapital\":32493,\"p_IdTipoEventoCapital\":\"CN\"}";
				CerrarEventoCapitalCambioDeNemoEntrada Objin = gson.fromJson(json, CerrarEventoCapitalCambioDeNemoEntrada.class);
				System.out.println(Objin.toString());
				
				CerrarEventoCapitalCambioDeNemo  demo = new CerrarEventoCapitalCambioDeNemo();
				CerrarEventoCapitalCambioDeNemoSalida ObjOut = demo.logica(con, Objin);
				ObjOut.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(ObjOut.P_OUT_MENSAJES);
				System.out.println(ObjOut.toString());
				
//				con.commit();
				con.rollback();
				
		    }
			catch(Exception er) 
			{
				er.printStackTrace();	
				con.rollback();
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();			
		}
		
		System.out.println("== FIN PRUEBA LOCAL ==");
	};
	

	
}
