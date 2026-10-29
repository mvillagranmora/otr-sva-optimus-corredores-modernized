package cl.bcs.plataforma.com.pe;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.com.pojo.DetallePerfilCobro_COM2;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class EliminarDetallePerfilCobro implements JavaCall {

	private final String SQL_UPDATE_DETALLE_PERFIL_COBRO = loadResourceToString("cl/bcs/plataforma/com/sql/EliminarDetallePerfilCobro_UpdateDetallePerfilCobro.sql");
	private final String SQL_CONSULTAR_DETALLE_PERFIL_COBRO = loadResourceToString("cl/bcs/plataforma/com/sql/EliminarDetallePerfilCobro_ConsultarDetallePerfilCobro.sql");
	private final String SQL_UPDATE_DEFINICION_COBRO = loadResourceToString("cl/bcs/plataforma/com/sql/EliminarDetallePerfilCobro_UpdateDefinicionCobro.sql");
	
	public EliminarDetallePerfilCobro_Salida logica(Connection conn,EliminarDetallePerfilCobro_Entrada entrada) throws Exception {
		EliminarDetallePerfilCobro_Salida salida = new EliminarDetallePerfilCobro_Salida();
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida basicosSalida = basicos.logica(conn, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
		if (MensajeSalida.getNumeroErrores(basicosSalida.p_out_Mensajes) > 0) {
			salida.p_out_Mensajes.addAll(basicosSalida.p_out_Mensajes);
			return salida;
		}
		
		SQL sql = new SQL();
		HashMap<String,Object> params1 = new HashMap<>();
		ArrayList<Long> ids = new ArrayList<>();
		for(DetallePerfilCobro_Entrada ent : entrada.p_DetallesPerfilCobro) {
			ids.add(ent.IdDetallePerfilCobros);
		}
		params1.put("IdEmpresa", entrada.p_IdEmpresa);
		params1.put("Datos", ids);
		List<DetallePerfilCobro_COM2> det1 = sql.query(conn, SQL_CONSULTAR_DETALLE_PERFIL_COBRO, params1, DetallePerfilCobro_COM2.class);
		
		HashMap<Long,Object> hola =  Utils.List2Hash(det1,"IdDetallePerfilCobros");
		
		ArrayList<DetallePerfilCobro_COM2> det2 = new ArrayList<>();
		ArrayList<DetallePerfilCobro_COM2> detUpdate = new ArrayList<>();
		
		for (DetallePerfilCobro_Entrada ent: entrada.p_DetallesPerfilCobro) {
			DetallePerfilCobro_COM2 det = (DetallePerfilCobro_COM2)hola.get(ent.IdDetallePerfilCobros);
			if (det == null) {
				salida.p_out_Mensajes.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"DETALLE_PERFIL_COBRO_NO_EXISTE",new HashMap<>(),conn,entrada.p_IdEmpresa));
				continue;
			}
			DetallePerfilCobro_COM2 aux = new DetallePerfilCobro_COM2();
			aux.IdDetallePerfilCobros = ent.IdDetallePerfilCobros;
			aux.IdUsuarioBorra = entrada.p_IdUsuario;
			aux.Borrado = "S";
			if (det.IdPerfilCobros.contains("EXT")) {
				DetallePerfilCobro_COM2 arg0 = new DetallePerfilCobro_COM2();
				arg0.IdDetallePerfilCobros = ent.IdDetallePerfilCobros;
				arg0.IdEstado = "INA";
				aux.IdEmpresa = entrada.p_IdEmpresa;
				detUpdate.add(arg0 );
			}
			det2.add(aux);
			salida.p_out_Mensajes.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,"PERFIL_COMERCIAL_ACTUALIZADO",new HashMap<>(),conn,entrada.p_IdEmpresa));
			
		}
		
		sql.batch(conn, SQL_UPDATE_DETALLE_PERFIL_COBRO, det2, DetallePerfilCobro_COM2.class);
		sql.batch(conn, SQL_UPDATE_DEFINICION_COBRO, detUpdate, DetallePerfilCobro_COM2.class);
		//ModificarDefinicionCobro
		
		return salida;

	}
	
	
	
	
	public static class EliminarDetallePerfilCobro_Entrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		
		public List<DetallePerfilCobro_Entrada> p_DetallesPerfilCobro;
		
		public EliminarDetallePerfilCobro_Entrada() {
			p_DetallesPerfilCobro = new ArrayList<>();
		}
		
		
	}
	
	public static class DetallePerfilCobro_Entrada {
		public Long IdDetallePerfilCobros;
		public String CodigoDefinicionCobro;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class EliminarDetallePerfilCobro_Salida {
		public List<MensajeSalida> p_out_Mensajes;
		public Long P_OUT_ERRORESCONTADOR;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public EliminarDetallePerfilCobro_Salida() {
			p_out_Mensajes = new ArrayList<MensajeSalida>();
		}
		
	}
	
	public static void main (String[] args) throws Exception {
		
		try (Connection conn = PlataformaUtils.getMBPEConnection()){
			
			
			
			Gson gson = Utils.getGsonBuilder();
			String json =   "{\"p_IdEmpresa\":1,\"p_IdUsuario\":69,\"p_IdModulo\":\"PLA\",\"p_DetallesPerfilCobro\":[{\"IdDetallePerfilCobros\":831,\"CodigoDefinicionCobro\":\"COMISION SMV RF-EXT_20262\"},{\"IdDetallePerfilCobros\":456,\"CodigoDefinicionCobro\":\"COBRO_BASE_TASA-EXT_20262\"},{\"IdDetallePerfilCobros\":454,\"CodigoDefinicionCobro\":\"IGV_CONTADO-EXT_20262\"}]}";
			EliminarDetallePerfilCobro_Entrada Objin = gson.fromJson(json, EliminarDetallePerfilCobro_Entrada.class);
			EliminarDetallePerfilCobro asdf = new EliminarDetallePerfilCobro();
			EliminarDetallePerfilCobro_Salida salida = asdf.logica(conn, Objin);
			System.out.println(salida.toString());

		}	
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EliminarDetallePerfilCobro_Entrada in=gson.fromJson(jin.toString() ,EliminarDetallePerfilCobro_Entrada.class);
		EliminarDetallePerfilCobro_Salida out=logica(conn,in);
		for (MensajeSalida msje : out.p_out_Mensajes) {
			msje.descripcion = Diccionario.getMensaje(conn,in.p_IdEmpresa, msje.codigo, new HashMap());
		}
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.p_out_Mensajes);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
}






