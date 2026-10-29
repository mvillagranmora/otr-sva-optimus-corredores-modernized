package cl.bcs.ordenes;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.ordenes.AnularOrdenMasiva.EntradaAnularOrdenMasiva;
import cl.bcs.ordenes.AnularOrdenMasiva.SalidaAnularOrdenMasiva;
import cl.bcs.ordenes.utils.OrdenUtils.AdvertenciaDetalle;
import cl.bcs.ordenes.utils.OrdenUtils.ErrorDetalle;
import cl.bcs.ordenes.utils.OrdenUtils.InformacionDetalle;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AprobarRechazarSolicitudAnulacionOrdenes implements JavaCall {
	
	private final String SQL_CREAR_ENTRADA_TEMPORAL = "create local temporary table #entrada_anular_ordenes_solicitud (\"IdOrden\" BIGINT , \"MotivoAnulacion\" nvarchar(5000))";
	private final String SQL_INSERT_ENTRADA_TEMPORAL = "insert into #entrada_anular_ordenes_solicitud values(:IdOrden,:MotivoAnulacion);";
	private final String SQL_CREAR_ENTRADA_RETIRO_OFERTA_TEMPORAL = "create local temporary table #entrada_anular_ordenes_retiro_solicitud (\"IdOrden\" BIGINT)";
	private final String SQL_INSERT_ENTRADA_RETIRO_OFERTA_TEMPORAL = "insert into #entrada_anular_ordenes_retiro_solicitud values(:IdOrden);";
	private final String SQL_ELIMINAR_ENTRADA_TEMPORAL = "DROP TABLE #entrada_anular_ordenes_solicitud;";
	private final String SQL_ELIMINAR_ENTRADA_RETIRO_TEMPORAL = "DROP TABLE #entrada_anular_ordenes_retiro_solicitud;";
	public String SQL_RETIRAR_OFERTA_ORDENS_Call = loadResourceToString("cl/bcs/ordenes/sql/RetirarOfertaOrdenMasiva_Call.sql");
	private final String SQL_SELECT_ORDENES_RETIRAR_OFERTA = loadResourceToString("cl/bcs/ordenes/sql/RetirarOferta_SelectOrdenes.sql");
	//private final String SQL_UPDATE_ESTADO_SOLICITUD_ORDEN = loadResourceToString("cl/bcs/ordenes/sql/UpdateOrdenes_Rechazadas.sql");
	public String SQL_UPDATE_ESTADO_SOLICITUD_ORDEN = "UPDATE \"BSCL.Orden.BD::ORD.Orden\" SET \"EnAnulacion\" = 'N' Where \"IdOrden\" IN (:IdOrden);";
	public static class EntradaAprobarRechazarSolicitudAnulacionOrdenes {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_Opcion;
		public String p_RequiereNotificacion = "S";
		public List<AnularOrden> p_ordenes;
		
		public EntradaAprobarRechazarSolicitudAnulacionOrdenes() {
			this.p_ordenes = new ArrayList<>();
		}

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class EntradaRetirarOferta{
		public List<RetirarOferta> p_ordenes;
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class AnularOrden {
		public Long IdOrden;
		public String MotivoAnulacion;

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class RetirarOferta {
		public Long IdOrden;

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaAprobarRechazarSolicitudAnulacionOrdenes {
		public Long P_OUT_ERRORESCONTADOR;
		public Long P_OUT_INFORMACIONCONTADOR;
		public Long P_OUT_ADVERTENCIASCONTADOR;
		public List<ErrorDetalle> P_OUT_ERRORESDETALLE;
		public List<InformacionDetalle> P_OUT_INFORMACIONDETALLE;
		public List<AdvertenciaDetalle> P_OUT_ADVERTENCIASDETALLE;
		
		public SalidaAprobarRechazarSolicitudAnulacionOrdenes() {
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_INFORMACIONCONTADOR = 0L;
			P_OUT_ADVERTENCIASCONTADOR = 0L;
			P_OUT_ERRORESDETALLE = new ArrayList<>();
			P_OUT_INFORMACIONDETALLE = new ArrayList<>();
			P_OUT_ADVERTENCIASDETALLE = new ArrayList<>();
		}
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public SalidaAprobarRechazarSolicitudAnulacionOrdenes logica(Connection con, EntradaAprobarRechazarSolicitudAnulacionOrdenes in) throws Exception {
		SalidaAprobarRechazarSolicitudAnulacionOrdenes salida = new SalidaAprobarRechazarSolicitudAnulacionOrdenes();
		List<AnularOrden> OrdenesParaModificar = new ArrayList<>();
		List<MensajeSalida> salidaRechazo = new ArrayList<>();
		SQL sql = new SQL();
		SQL.exec(con, SQL_CREAR_ENTRADA_TEMPORAL);
		SQL.exec(con,SQL_CREAR_ENTRADA_RETIRO_OFERTA_TEMPORAL);
			if(in.p_Opcion.equalsIgnoreCase("APROBAR")){
			List<Long> IdOrdenes = new ArrayList<>();
			HashMap<String,Object> entradaOrdenes = new HashMap<String,Object>();
			for(AnularOrden retirarOrden : in.p_ordenes){
				//RetirarOferta ordeneParaRetirar = new RetirarOferta();
				//entradaOrdenes.putIfAbsent("IdOrden", retirarOrden.IdOrden);
				//ordeneParaRetirar.IdOrden = retirarOrden.IdOrden;
				IdOrdenes.add(retirarOrden.IdOrden);
			}
			entradaOrdenes.put("IdOrden", IdOrdenes);
			List<RetirarOferta> retirarOfertasOrdenes = sql.query(con, SQL_SELECT_ORDENES_RETIRAR_OFERTA, entradaOrdenes, RetirarOferta.class);
			if(retirarOfertasOrdenes.size()>0){
				HashMap<String,Object> retirarOfertaIn = new HashMap<String,Object>();
				retirarOfertaIn.put("p_IdEmpresa", in.p_IdEmpresa);
				retirarOfertaIn.put("p_IdUsuario", in.p_IdUsuario);
				retirarOfertaIn.put("p_IdModulo", in.p_IdModulo);
				sql.batch(con, SQL_INSERT_ENTRADA_RETIRO_OFERTA_TEMPORAL, retirarOfertasOrdenes, RetirarOferta.class);
				
				//agregar ultimos datos
				HashMap<String,Class> retirarOrfertaOut = new HashMap<String,Class>();
				retirarOrfertaOut.put("p_out_InformacionContador", Long.class);
				retirarOrfertaOut.put("p_out_InformacionDetalle", InformacionDetalle.class);
				retirarOrfertaOut.put("p_out_AdvertenciasContador", Long.class);
				retirarOrfertaOut.put("p_out_AdvertenciasDetalle", AdvertenciaDetalle.class);
				retirarOrfertaOut.put("p_out_ErroresContador", Long.class);
				retirarOrfertaOut.put("p_out_ErroresDetalle", ErrorDetalle.class);
				HashMap<String, Object> salidaCall = sql.call(con, SQL_RETIRAR_OFERTA_ORDENS_Call, retirarOfertaIn, retirarOrfertaOut);
				salida.P_OUT_ADVERTENCIASDETALLE = (List<AdvertenciaDetalle>) salidaCall.get("p_out_AdvertenciasDetalle");
				salida.P_OUT_ERRORESDETALLE = (List<ErrorDetalle>) salidaCall.get("p_out_ErroresDetalle");
				salida.P_OUT_INFORMACIONDETALLE = (List<InformacionDetalle>) salidaCall.get("p_out_InformacionDetalle");
				salida.P_OUT_INFORMACIONCONTADOR = (long)salida.P_OUT_INFORMACIONDETALLE.size();
				salida.P_OUT_ADVERTENCIASCONTADOR = (long)salida.P_OUT_ADVERTENCIASDETALLE.size();
				salida.P_OUT_ERRORESCONTADOR = (long)salida.P_OUT_ERRORESDETALLE.size();
			}
			//Como ya se retiraron las ofertas, ahora podemos anular sin problema.
			//Todos los registros deben ser anulados, no llegan registros "invalidos" (Filtrados por UI)
			AnularOrdenMasiva anularOrden = new AnularOrdenMasiva();
			EntradaAnularOrdenMasiva anularOrdenIn = new EntradaAnularOrdenMasiva();
			anularOrdenIn.p_IdEmpresa = in.p_IdEmpresa;
			anularOrdenIn.p_IdUsuario = in.p_IdUsuario;
			anularOrdenIn.p_IdModulo = in.p_IdModulo;
			anularOrdenIn.p_RequiereNotificacion = in.p_RequiereNotificacion;
			for(AnularOrden ordenParaAnular : in.p_ordenes){
				AnularOrdenMasiva.AnularOrden objAnularOrden = new AnularOrdenMasiva.AnularOrden();
				objAnularOrden.IdOrden = ordenParaAnular.IdOrden;
				objAnularOrden.MotivoAnulacion = ordenParaAnular.MotivoAnulacion;
				anularOrdenIn.p_ordenes.add(objAnularOrden);
			}
			SalidaAnularOrdenMasiva salidaBasicos = anularOrden.logica(con, anularOrdenIn);
			salida.P_OUT_ADVERTENCIASDETALLE.addAll(salidaBasicos.P_OUT_ADVERTENCIASDETALLE);
			salida.P_OUT_ERRORESDETALLE.addAll(salidaBasicos.P_OUT_ERRORESDETALLE);
			salida.P_OUT_INFORMACIONDETALLE.addAll(salidaBasicos.P_OUT_INFORMACIONDETALLE);
			salida.P_OUT_ERRORESCONTADOR = (long)salida.P_OUT_ERRORESDETALLE.size();
			salida.P_OUT_INFORMACIONCONTADOR = (long)salida.P_OUT_INFORMACIONDETALLE.size();
			salida.P_OUT_ADVERTENCIASCONTADOR = (long)salida.P_OUT_ADVERTENCIASDETALLE.size();
			//Se cambian estados por proceso
			if(!(salida.P_OUT_ERRORESCONTADOR>0)){
				for(AnularOrden OrdenParaModificar : in.p_ordenes){
					OrdenesParaModificar.add(OrdenParaModificar);
				}
				if(OrdenesParaModificar.size()>0){
					sql.batch(con, SQL_UPDATE_ESTADO_SOLICITUD_ORDEN, OrdenesParaModificar, AnularOrden.class);
				}
			}
		}else{
			//Desmarcar Estado
			//HashMap<String,Object> OrdenesParaModificarEstado = new HashMap<String,Object>();
			for(AnularOrden OrdenParaModificar : in.p_ordenes){
				OrdenesParaModificar.add(OrdenParaModificar);
			}
			if(OrdenesParaModificar.size()>0){
				sql.batch(con, SQL_UPDATE_ESTADO_SOLICITUD_ORDEN, OrdenesParaModificar, AnularOrden.class);
			}
			InformacionDetalle rechazo = new InformacionDetalle();
			rechazo.Codigo="UI_SOLICITUD_RECHAZADA";
			rechazo.Descripcion="Se ha rechazado la solicitud de Anulacion";
			rechazo.IdInterno = 0L;
			rechazo.IdOperacion = 0L;
			salida.P_OUT_INFORMACIONDETALLE.add(rechazo);
			salida.P_OUT_INFORMACIONCONTADOR = (long)salida.P_OUT_INFORMACIONDETALLE.size();
			salida.P_OUT_ADVERTENCIASCONTADOR = (long)salida.P_OUT_ADVERTENCIASDETALLE.size();
			
			
			
		}
			
		SQL.exec(con, SQL_ELIMINAR_ENTRADA_TEMPORAL);
		SQL.exec(con, SQL_ELIMINAR_ENTRADA_RETIRO_TEMPORAL);
		return salida;
		
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaAprobarRechazarSolicitudAnulacionOrdenes in = gson.fromJson(jin.toString(), EntradaAprobarRechazarSolicitudAnulacionOrdenes.class);
		SalidaAprobarRechazarSolicitudAnulacionOrdenes out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public static void main(String[] args) {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getQAPEConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();

			String json2 = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"ORD\",\"p_Opcion\":\"RECHAZO\",\"p_ordenes\":[{\"IdOrden\":41587,\"MotivoAnulacion\":\"Anulacion por Solicitud\"}]}";
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"ORD\",\"p_Opcion\":\"APROBAR\",\"p_ordenes\":[{\"IdOrden\":41952,\"MotivoAnulacion\":\"TEST\"},{\"IdOrden\":41950,\"MotivoAnulacion\":\"TEST\"}]}";
			EntradaAprobarRechazarSolicitudAnulacionOrdenes Objin = gson.fromJson(json, EntradaAprobarRechazarSolicitudAnulacionOrdenes.class);

			AprobarRechazarSolicitudAnulacionOrdenes demo = new AprobarRechazarSolicitudAnulacionOrdenes();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			
			try {
				SalidaAprobarRechazarSolicitudAnulacionOrdenes ObjOut = demo.logica(con, Objin);
				con.rollback();
				System.out.println(ObjOut.toString());
				System.out.println("SALIDA");
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
			con.rollback();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}

}
