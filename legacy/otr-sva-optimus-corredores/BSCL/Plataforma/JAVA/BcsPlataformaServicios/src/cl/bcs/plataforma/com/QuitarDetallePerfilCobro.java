package cl.bcs.plataforma.com;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.com.pojo.DetallePerfilCobros;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

//QUITAR DETALLE COBRO PARA CHILE
public class QuitarDetallePerfilCobro implements JavaCall {

	private final String SQL_UPDATE_DETALLE_PERFIL_COBRO = loadResourceToString("cl/bcs/plataforma/com/sql/QuitarDetallePerfilCobro_UpdateDetallePerfilCobro.sql");
	private final String SQL_CONSULTAR_DETALLE_PERFIL_COBRO = loadResourceToString("cl/bcs/plataforma/com/sql/QuitarDetallePerfilCobro_ConsultarDetallePerfilCobro.sql");
	
	public QuitarDetallePerfilCobro_Salida logica(Connection conn,QuitarDetallePerfilCobro_Entrada entrada) throws Exception {
		QuitarDetallePerfilCobro_Salida salida = new QuitarDetallePerfilCobro_Salida();
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
		params1.put("Datos", ids);
		List<DetallePerfilCobros> det1 = sql.query(conn, SQL_CONSULTAR_DETALLE_PERFIL_COBRO, params1, DetallePerfilCobros.class);
		
		HashMap<Long,Object> hola =  Utils.List2Hash(det1,"IdDetallePerfilCobros");
		
		ArrayList<DetallePerfilCobros> det2 = new ArrayList<>();
		for (DetallePerfilCobro_Entrada ent: entrada.p_DetallesPerfilCobro) {
			if (hola.get(ent.IdDetallePerfilCobros) == null) {
				salida.p_out_Mensajes.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"DETALLE_PERFIL_COBRO_NO_EXISTE",""));
				return salida;
			}
			DetallePerfilCobros aux = new DetallePerfilCobros();
			aux.IdDetallePerfilCobros = ent.IdDetallePerfilCobros;
			aux.IdUsuarioBorra = entrada.p_IdUsuario;
			aux.Borrado = "S";
			det2.add(aux);
		}
		
		sql.batch(conn, SQL_UPDATE_DETALLE_PERFIL_COBRO, det2, DetallePerfilCobros.class);
		return salida;

	}
	
	
	
	
	public static class QuitarDetallePerfilCobro_Entrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		
		public List<DetallePerfilCobro_Entrada> p_DetallesPerfilCobro;
		
		
	}
	
	public static class DetallePerfilCobro_Entrada {
		public Long IdDetallePerfilCobros;
		
	}
	
	public static class QuitarDetallePerfilCobro_Salida {
		public List<MensajeSalida> p_out_Mensajes;
		public Long P_OUT_ERRORESCONTADOR;
		
		public QuitarDetallePerfilCobro_Salida() {
			p_out_Mensajes = new ArrayList<MensajeSalida>();
		}
		
	}
	
	public static void main (String[] args) throws Exception {
		try (Connection conn =  PlataformaUtils.getDESAConnection()){
			QuitarDetallePerfilCobro_Entrada ent = new QuitarDetallePerfilCobro_Entrada();
			//{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"PLA\",\"p_DetallesPerfilCobro\":[{\"IdDetallePerfilCobros\":1965}]}
			ent.p_IdEmpresa = 1L;
			ent.p_IdUsuario = 4L;
			ent.p_IdModulo = "ORD";
			ent.p_DetallesPerfilCobro = new ArrayList<DetallePerfilCobro_Entrada>();
			DetallePerfilCobro_Entrada aux = new DetallePerfilCobro_Entrada();
			aux.IdDetallePerfilCobros = 1965L;
			
			ent.p_DetallesPerfilCobro.add(aux);
			
			
			QuitarDetallePerfilCobro asdf = new QuitarDetallePerfilCobro();
			QuitarDetallePerfilCobro_Salida salida = asdf.logica(conn, ent);
			

		}	
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = new Gson();		
		QuitarDetallePerfilCobro_Entrada in=gson.fromJson(jin.toString() ,QuitarDetallePerfilCobro_Entrada.class);
		QuitarDetallePerfilCobro_Salida out=logica(conn,in);
		for (MensajeSalida msje : out.p_out_Mensajes) {
			msje.descripcion = Diccionario.getMensaje(conn, in.p_IdEmpresa,msje.codigo, new HashMap());
		}
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.p_out_Mensajes);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
}






