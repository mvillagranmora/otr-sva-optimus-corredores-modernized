package cl.bcs.ordenes;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.ordenes.NotificarEstadoOrden.NotificarEstadoOrden_Entrada;
import cl.bcs.plataforma.pla.AgregarMensajeWebsocket;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class CantarOrdenes implements JavaCall {

	private final String SQL_CREAR_ENTRADA_TEMPORAL = loadResourceToString("cl/bcs/ordenes/sql/CantarOrdenes_CrearEntrada.sql");
	private final String SQL_INSERT_ENTRADA_TEMPORAL = loadResourceToString("cl/bcs/ordenes/sql/CantarOrdenes_InsertarEntrada.sql");
	private final String SQL_ELIMINAR_ENTRADA_TEMPORAL = loadResourceToString("cl/bcs/ordenes/sql/CantarOrdenes_EliminarEntrada.sql");
	private final String SQL_UPDATE_ORDENES        	= loadResourceToString("cl/bcs/ordenes/sql/CantarOrdenes_UpdateOrden.sql");

	public String SQL_CANTAR_ORDENS_Call = loadResourceToString("cl/bcs/ordenes/sql/CambiarEstadoOrdenMasivo_Call.sql");
	
	Logger logger = LogManager.getLogger(CantarOrdenes.class);

	
	public static class CantarOrdenes_Entrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public List<CantarOrden> p_Ordenes;

		public CantarOrdenes_Entrada() {
			this.p_Ordenes = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CantarOrden {
		public Long IdInterno;
		public Long IdOrden;
		public String IdBolsa;

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Entrada_OrdenUpdate {
		public Long IdOrden;
		public String IdBolsa;
		public java.sql.Timestamp FechaOferta;
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}

		public Entrada_OrdenUpdate(Long idOrden, String idBolsa, Timestamp fechaOferta) {
			super();
			IdOrden = idOrden;
			IdBolsa = idBolsa;
			FechaOferta = fechaOferta;
		}

		public Entrada_OrdenUpdate() {
			super();
		}
		
	}


	public static class CantarOrdenes_Salida{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		
		public CantarOrdenes_Salida() {
			this.P_OUT_ERRORESCONTADOR=0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	
	public static class CantarOrdenesUpdate_Salida {

		public List<SalidaDetalle>	P_OUT_SALIDADETALLE;
		public List<SalidaResumen>	P_OUT_SALIDARESUMEN;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

		
	public static class SalidaDetalle {
		public Long Id;
		public Long IdOperacion;
		public String Resultado;
		public String CodigoMensaje;
		public String CodigoDato1;
		public String ValorDato1;
		public String CodigoDato2;
		public String ValorDato2;
	}

	
	public static class SalidaResumen {
		public Long IdInterno;
		public Long IdOperacion;
		public String Resultado;
		public Long OK;
		public Long ADVERTENCIA;
		public Long AUTORIZACION;
		public Long RECHAZO;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		CantarOrdenes_Entrada in = gson.fromJson(jin.toString(), CantarOrdenes_Entrada.class);
		CantarOrdenes_Salida out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	@SuppressWarnings("unchecked")
	@Trace
	public CantarOrdenes_Salida logica(Connection conn, CantarOrdenes_Entrada in) throws Exception {
		logger.info("INICIO::CantarOrdenes");
		Long init = System.currentTimeMillis();
		Long Id = 0L;
		SQL sql = new SQL();
		try {
		    String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
			logger.debug("INICIO:: Revisar Requisitos Basicos");
			RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(conn, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
			logger.debug("FIN:: Revisar Requisitos Basicos");
			CantarOrdenes_Salida out = new CantarOrdenes_Salida();
	
			if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
				for(MensajeSalida salida : salidaBasicos.p_out_Mensajes){
					out.P_OUT_ERRORESCONTADOR++;
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,  salida.codigo, salida.descripcion));
				}
				return out;
			}
			HashMap<String, Object> paramsIn = new HashMap<String, Object>();
			paramsIn.put("p_NuevoEstado", "OFE");

			SQL.exec(conn, SQL_CREAR_ENTRADA_TEMPORAL);
			sql.batch(conn, SQL_INSERT_ENTRADA_TEMPORAL, in.p_Ordenes, CantarOrden.class);
			
			@SuppressWarnings("rawtypes")
			HashMap<String, Class> paramsOut = new HashMap<String, Class>();
			paramsOut.put("p_out_SalidaDetalle", SalidaDetalle.class);
			paramsOut.put("p_out_SalidaResumen", SalidaResumen.class);
			
			HashMap<String, Object> salidaCall = sql.call(conn, SQL_CANTAR_ORDENS_Call, paramsIn, paramsOut);
			
			List<SalidaDetalle> P_OUT_SALIDADETALLE;
			List<Entrada_OrdenUpdate> ordenesUpd = new ArrayList<>();

			P_OUT_SALIDADETALLE = (List<SalidaDetalle>) salidaCall.get("p_out_SalidaDetalle");
			
			List<NotificarEstadoOrden.Orden> ordenesNotificacion = new ArrayList<NotificarEstadoOrden.Orden>();
			HashMap<String, String> mapMsg = new HashMap<>();

			
			for (SalidaDetalle salidaDetalle: P_OUT_SALIDADETALLE) {
				if ("OK".equalsIgnoreCase(salidaDetalle.Resultado)) {
					ordenesUpd.add(new Entrada_OrdenUpdate(salidaDetalle.IdOperacion,"",Fecha.toDateTime(FechaDB.ObtenerFechaHoraSistemaUTC(conn, in.p_IdEmpresa))));
					NotificarEstadoOrden.Orden ordenNotificar = new NotificarEstadoOrden.Orden();
					ordenNotificar.IdOrden = salidaDetalle.IdOperacion;					
					ordenesNotificacion.add(ordenNotificar);
					mapMsg.clear();
					mapMsg.put("IdOrden", salidaDetalle.IdOperacion.toString());
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_OK,"ORDEN_OFERTADA",mapMsg,conn,in.p_IdEmpresa));
					Id++;
					AgregarMensajeWebsocket.agregar(conn, in.p_IdEmpresa, "ORDEN_OFERTADA", "");
				}
			}

			sql.batch(conn, SQL_UPDATE_ORDENES, ordenesUpd, Entrada_OrdenUpdate.class);


			//NOTIFICACION ORDEN
			{
				NotificarEstadoOrden_Entrada entradaNotificarEstadoOrden = new NotificarEstadoOrden_Entrada();
				entradaNotificarEstadoOrden.p_IdEmpresa = in.p_IdEmpresa;
				entradaNotificarEstadoOrden.p_IdModulo = in.p_IdModulo;
				entradaNotificarEstadoOrden.p_IdUsuario = in.p_IdUsuario;
				entradaNotificarEstadoOrden.p_Ordenes = ordenesNotificacion;
				NotificarEstadoOrden notificarEstadoOrdenService = new NotificarEstadoOrden();
				notificarEstadoOrdenService.logicaAsync(conn, entradaNotificarEstadoOrden);
			}
			logger.info("FIN::CantarOrdenes, tiempo: " + (System.currentTimeMillis() - init));
			return out;
		} 
		
		catch (Exception e){
			logger.error("ERROR",e);
			throw e;
			
		} 
		finally 
		{ 
			SQL.exec(conn, SQL_ELIMINAR_ENTRADA_TEMPORAL);

		}
	}

	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			System.out.println("== BEGIN ==");
			Gson gson = Utils.getGsonBuilder();
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"ORD\",\"p_Ordenes\":[{\"IdInterno\":321938,\"IdOrden\":321938,\"IdBolsa\":\"\"}]}";
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"ORD\",\"p_Ordenes\":[{\"IdInterno\":321938,\"IdOrden\":321938,\"IdBolsa\":\"\"},{\"IdInterno\":310701,\"IdOrden\":310701,\"IdBolsa\":\"\"},{\"IdInterno\":666777,\"IdOrden\":666777,\"IdBolsa\":\"\"}]}";
			CantarOrdenes_Entrada Objin = gson.fromJson(json, CantarOrdenes_Entrada.class);
			
			CantarOrdenes demo = new CantarOrdenes();
			try {
				CantarOrdenes_Salida ObjOut = demo.logica(con, Objin);
				con.rollback();
				//con.commit();
				System.out.println(ObjOut.toString());
			} catch(Exception er) {
				er.printStackTrace();
				con.rollback();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}
