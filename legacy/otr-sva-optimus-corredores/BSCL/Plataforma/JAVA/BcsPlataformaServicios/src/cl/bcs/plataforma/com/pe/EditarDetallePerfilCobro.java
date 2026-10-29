package cl.bcs.plataforma.com.pe;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.com.pe.CrearDetallePerfilCobro.CrearDetallePerfilCobro_Entrada;
import cl.bcs.plataforma.com.pe.CrearDetallePerfilCobro.CrearDetallePerfilCobro_Salida;
import cl.bcs.plataforma.com.pojo.DefinicionCobro_DB;
import cl.bcs.plataforma.com.pojo.DetallePerfilCobro_COM2;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class EditarDetallePerfilCobro implements JavaCall {

	
	private final String SQL_CONSULTAR_DETALLE_PERFIL_COBRO = loadResourceToString("cl/bcs/plataforma/com/sql/EliminarDetallePerfilCobro_ConsultarDetallePerfilCobro.sql");
	private final String SQL_UPDATE_DEFINICION_COBRO = loadResourceToString("cl/bcs/plataforma/com/sql/ModificarDetallePerfilCobro_UpdateDefinicionCobro.sql");
	private final String SQL_CONSULTAR_DEFINICION_COBRO = loadResourceToString("cl/bcs/plataforma/com/sql/ModificarDefinicionCobro_ConsultarDefinicionCobro.sql");
	
	public EditarDetallePerfilCobro_Salida logica(Connection conn,EditarDetallePerfilCobro_Entrada entrada) throws Exception {
		EditarDetallePerfilCobro_Salida salida = new EditarDetallePerfilCobro_Salida();
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida basicosSalida = basicos.logica(conn, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
		if (MensajeSalida.getNumeroErrores(basicosSalida.p_out_Mensajes) > 0) {
			salida.p_out_Mensajes.addAll(basicosSalida.p_out_Mensajes);
			return salida;
		}
		
		SQL sql = new SQL();
		HashMap<String,Object> params1 = new HashMap<>();
		HashMap<String,Object> params2 = new HashMap<>();
		ArrayList<Long> ids = new ArrayList<>();
		ArrayList<String> id2s = new ArrayList<>();
		for(DetallePerfilCobro_Entrada ent : entrada.p_DetallesPerfilCobro) {
			id2s.add(ent.CodigoDefinicionCobro);
			ids.add(ent.IdDetallePerfilCobros);
		}
		params1.put("IdEmpresa", entrada.p_IdEmpresa);
		params1.put("Datos", ids);
		params2.put("IdEmpresa", entrada.p_IdEmpresa);
		params2.put("CodigoDefinicionCobro", id2s);
		List<DetallePerfilCobro_COM2> det1 = sql.query(conn, SQL_CONSULTAR_DETALLE_PERFIL_COBRO, params1, DetallePerfilCobro_COM2.class);
		List<DetallePerfilCobro_COM2> det2 = sql.query(conn, SQL_CONSULTAR_DEFINICION_COBRO, params2, DetallePerfilCobro_COM2.class);
		
		HashMap<Long,Object> perfil =  Utils.List2Hash(det1,"IdDetallePerfilCobros");
		HashMap<Long,Object> cobros =  Utils.List2Hash(det2,"CodigoDefinicionCobro");
		
		ArrayList<DetallePerfilCobro_COM2> det3 = new ArrayList<>();
		for (DetallePerfilCobro_Entrada ent: entrada.p_DetallesPerfilCobro) {
			if (perfil.get(ent.IdDetallePerfilCobros) == null) {
				salida.p_out_Mensajes.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"DETALLE_PERFIL_COBRO_NO_EXISTE",""));
				return salida;
			}
			if (cobros.get(ent.CodigoDefinicionCobro) == null) {
				salida.p_out_Mensajes.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"DEFINICION_COBRO_NO_EXISTE",""));
				return salida;
			}
			DetallePerfilCobro_COM2 aux = new DetallePerfilCobro_COM2();
			aux.IdEmpresa						= entrada.p_IdEmpresa;
			aux.CodigoDefinicionCobro			= ent.CodigoDefinicionCobro;
			aux.MontoFijo_MontoCobro 			= "MONTOFIJO".equalsIgnoreCase(ent.IdTipoCobro)? ent.ValorCobro:BigDecimal.ZERO;
			aux.PorcentualFijo_ValorCobro		= "PORCENTAJEFIJO".equalsIgnoreCase(ent.IdTipoCobro)? ent.ValorCobro:BigDecimal.ZERO;
			aux.PorcentualTasaPlazo_ValorCobro	= "PORCENTAJETASAPLAZO".equalsIgnoreCase(ent.IdTipoCobro)? ent.ValorCobro:BigDecimal.ZERO;
			det3.add(aux);
		}
		sql.batch(conn, SQL_UPDATE_DEFINICION_COBRO, det3, DetallePerfilCobro_COM2.class);
		
		return salida;

	}
	
	
	
	
	public static class EditarDetallePerfilCobro_Entrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_IdPerfilCobros;
		public List<DetallePerfilCobro_Entrada> p_DetallesPerfilCobro;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DetallePerfilCobro_Entrada {
		public Long 		IdDetallePerfilCobros;
		public String 		CodigoDefinicionCobro;
		public String 		IdTipoCobro;
		public BigDecimal 	ValorCobro;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class EditarDetallePerfilCobro_Salida {
		public List<MensajeSalida> p_out_Mensajes;
		public Long P_OUT_ERRORESCONTADOR;
		
		public EditarDetallePerfilCobro_Salida() {
			p_out_Mensajes = new ArrayList<MensajeSalida>();
		}
		
	}
	
	public static void main (String[] args) throws Exception {
		
		try (Connection conn = PlataformaUtils.getDESAPEConnection()){
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json =   "{\"p_IdEmpresa\":1,\"p_IdUsuario\":69,\"p_IdModulo\":\"PLA\",\"p_IdPerfilCobros\":\"EXT_20262\",\"p_DetallesPerfilCobro\":[{\"IdDetallePerfilCobros\":580,\"CodigoDefinicionCobro\":\"CAVALI-EXT_20262_580\",\"IdTipoCobro\":\"PORCENTAJEFIJO\",\"ValorCobro\":29}]}";
			EditarDetallePerfilCobro_Entrada Objin = gson.fromJson(json, EditarDetallePerfilCobro_Entrada.class);
			EditarDetallePerfilCobro asdf = new EditarDetallePerfilCobro();
			EditarDetallePerfilCobro_Salida salida = asdf.logica(conn, Objin);
			conn.commit();

		}	
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = new Gson();		
		EditarDetallePerfilCobro_Entrada in=gson.fromJson(jin.toString() ,EditarDetallePerfilCobro_Entrada.class);
		EditarDetallePerfilCobro_Salida out=logica(conn,in);
		for (MensajeSalida msje : out.p_out_Mensajes) {
			msje.descripcion = Diccionario.getMensaje(conn,in.p_IdEmpresa, msje.codigo, new HashMap());
		}
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.p_out_Mensajes);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
}






