package cl.bcs.custodia.evc;

import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.evc.NotificarEventoCapital.NotificarEventoCapital_Entrada;
import cl.bcs.custodia.evc.NotificarEventoCapital.NotificarEventoCapital_Salida;
import cl.bcs.custodia.evc.NotificarEventoCapital.ObjEventoCapitalCliente_Entrada;
import cl.bcs.custodia.evc.NotificarEventoCapital.ObjEventoCapital_Entrada;
import cl.bcs.custodia.pojo.EventoCapital;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ProcesarEventoCapitalDivisionSociedad implements JavaCall {

	private final Log log = new Log("CUS.ProcesarEventoCapitalDivisionSociedad");
	private final String SQL_UPDATE_EVENTO_CAPITAL = loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapitalDivisionSociedad_UpdateEventoCapital.sql");
	private final String SQL_SELECT_REPORTES = loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapitalDivisionSociedad_SelectOrdenesReportes.sql");
	private final String SQL_INSERT_EVENTO_CAPITAL_CLIENTE = loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapitalDivisionSociedad_InsertEventoCapitalCliente.sql");

	public static class ActualizaEventoCapital {
		public Long IdEventoCapital;
		public String IdEstado;

		public String toString() {
			return Utils.printJson(this, false);
		}
	};

	public static class InsertaEventoCapital {
		public Long IdEventoCapital;
		public Long IdUsuario;
		public Long IdEmpresa;
		public Long IdInstrumento;
		public Date FechaLimite;

		public String toString() {
			return Utils.printJson(this, false);
		}
	};

	public static class ProcesarEventoCapitalDivisionSociedadEntrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public List<EventoCapital> p_EventoCapital;

		public ProcesarEventoCapitalDivisionSociedadEntrada() {
			p_EventoCapital = new ArrayList<EventoCapital>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	};

	public static class ProcesarEventoCapitalDivisionSociedadSalida {
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		
		public ProcesarEventoCapitalDivisionSociedadSalida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DatosOperaciones{
		public Long		Cantidad;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public ProcesarEventoCapitalDivisionSociedadSalida logica(Connection con, ProcesarEventoCapitalDivisionSociedadEntrada in) throws Exception {
		ProcesarEventoCapitalDivisionSociedadSalida out = new ProcesarEventoCapitalDivisionSociedadSalida();
		SQL sql=new SQL();
		//Validaciones iniciales.
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
		out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
	
		if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0){
			return out;
		}
		
		List<ObjEventoCapital_Entrada> ListaIds = new ArrayList<ObjEventoCapital_Entrada>();
		List<ObjEventoCapitalCliente_Entrada> ListaCliIds = new ArrayList<ObjEventoCapitalCliente_Entrada>();
		for (EventoCapital recorreEventoCapital: in.p_EventoCapital){
			HashMap<String,Object> paramDatosOperaciones = new HashMap<String,Object>();
			paramDatosOperaciones.put("IdInstrumento", recorreEventoCapital.IdInstrumento);
			paramDatosOperaciones.put("FechaLimite", recorreEventoCapital.FechaLimite);
			
			List<DatosOperaciones> datosOperaciones = sql.query(con, SQL_SELECT_REPORTES, paramDatosOperaciones, DatosOperaciones.class);
			Long contadorOperaciones = datosOperaciones.stream().filter(s-> s.Cantidad > 0).count();
			if (contadorOperaciones.equals(0L)) {
				List<InsertaEventoCapital> insertEventoClienteFull = new ArrayList<InsertaEventoCapital>();
				
				InsertaEventoCapital vInsert = new InsertaEventoCapital();
				vInsert.IdEventoCapital = recorreEventoCapital.IdEventoCapital;
				vInsert.IdUsuario		= in.p_IdUsuario;
				vInsert.IdEmpresa		= in.p_IdEmpresa;
				vInsert.IdInstrumento	= recorreEventoCapital.IdInstrumento;
				vInsert.FechaLimite		= recorreEventoCapital.FechaLimite;
				insertEventoClienteFull.add(vInsert);
				
				//Inserta evento capital cliente
				sql.batch(con, SQL_INSERT_EVENTO_CAPITAL_CLIENTE, insertEventoClienteFull , InsertaEventoCapital.class);
				log.write("ProcesarEventoCapitalDivisionSociedad - Fin Insert Eventos Capital Cliente");
				ProcesarEventoCapital.agregarLogProcesarEventoCapital(con, recorreEventoCapital.IdEventoCapital, in.p_IdModulo, in.p_IdUsuario, in.p_IdEmpresa,
						   										"PRO", recorreEventoCapital.IdTipoEventoCapital, insertEventoClienteFull.size(), recorreEventoCapital.Nemo);
				List<ActualizaEventoCapital> idEventoCapital = new ArrayList<ActualizaEventoCapital>();
				ActualizaEventoCapital idEven = new ActualizaEventoCapital();
				idEven.IdEventoCapital = recorreEventoCapital.IdEventoCapital;
				idEven.IdEstado = "PRO";
				idEventoCapital.add(idEven);
				
				//Actualiza evento capital
				sql.batch(con, SQL_UPDATE_EVENTO_CAPITAL, idEventoCapital, ActualizaEventoCapital.class);
				log.write("ProcesarEventoCapitalDivisionSociedad - Fin Update Eventos Capital");
				
				//Envio de notificacion
				ObjEventoCapital_Entrada NotifIN = new ObjEventoCapital_Entrada();
				NotifIN.p_IdEventoCapital = recorreEventoCapital.IdEventoCapital;
				ListaIds.add(NotifIN);
				
				NotificarEventoCapital_Entrada paramsNotif = new NotificarEventoCapital_Entrada();
				paramsNotif.p_IdEmpresa = in.p_IdEmpresa;
				paramsNotif.p_IdModulo = in.p_IdModulo;
				paramsNotif.p_IdUsuario = in.p_IdUsuario;
				paramsNotif.p_IdsEventoCapital = ListaIds;
				paramsNotif.p_IdsEventoCapitalCliente = ListaCliIds; 
				
				NotificarEventoCapital CreaNotificacion = new NotificarEventoCapital();
				NotificarEventoCapital_Salida resulNotif  = CreaNotificacion.logica(con, paramsNotif);
				out.P_OUT_MENSAJES.addAll(resulNotif.P_OUT_MENSAJES);
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"UI_DIVISION_SOCIEDAD_PROCESADO_OK",null,con,in.p_IdEmpresa));
			} else {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_DIVISION_SOCIEDAD_EXISTEN_OPERACIONES",null,con,in.p_IdEmpresa));
			}
		}
		return out;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		ProcesarEventoCapitalDivisionSociedadEntrada jin=gson.fromJson(in.toString() ,ProcesarEventoCapitalDivisionSociedadEntrada.class);
		ProcesarEventoCapitalDivisionSociedadSalida out=logica(conn,jin);
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public static void main(String[] args) throws Exception {		
		try (Connection con = PlataformaUtils.getDESAPEConnection()) {
			try {
				Gson gson = Utils.getGsonBuilder();
				String json = "";
				ProcesarEventoCapitalDivisionSociedadEntrada Objin = gson.fromJson(json, ProcesarEventoCapitalDivisionSociedadEntrada.class);
								
				ProcesarEventoCapitalDivisionSociedad  kk = new ProcesarEventoCapitalDivisionSociedad();
				ProcesarEventoCapitalDivisionSociedadSalida Objout = kk.logica(con, Objin);
				con.rollback();
				
		    	}catch(Exception er) {
					er.printStackTrace();	
					con.rollback();
				}
		} catch (Exception e) {
			e.printStackTrace();			
		}
	};
}
