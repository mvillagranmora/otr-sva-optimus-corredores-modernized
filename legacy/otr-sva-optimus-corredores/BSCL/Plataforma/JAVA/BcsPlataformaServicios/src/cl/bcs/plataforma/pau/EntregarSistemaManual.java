package cl.bcs.plataforma.pau;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.plataforma.bat.AgregarPrograma;
import cl.bcs.plataforma.bat.AgregarPrograma.InAgregarPrograma;

public class EntregarSistemaManual implements JavaCall {	

	//QUERIES
	private final String SQL_ESTADO_MODULOS = loadResourceToString("cl/bcs/plataforma/pau/sql/EntregarSistemaManual_SelectEstadoModulos.sql");
	private final String SQL_SELECT_USUARIO = loadResourceToString("cl/bcs/plataforma/pau/sql/EntregarSistemaManual_SelectUsuario.sql");
	private final String SQL_UPDATE_MODULO_SISTEMA="update \"BSCL.Plataforma.BD::PAU.Modulo\" set \"IdEstado\"=:IdEstado, \"CodigoUsuario\" = :CodigoUsuario, \"FechaUltimaActualizacion\" = CURRENT_UTCTIMESTAMP where \"CodigoModulo\"=:IdModulo and \"IdEmpresa\"=:IdEmpresa";
	private final String SQL_PERMISO_ENTREGA = loadResourceToString("cl/bcs/plataforma/pau/sql/EntregarSistemaManual_SelectPermisoEntregaSistema.sql");
	
	public static class AdelantarPautaEntrada {
		public Long 			p_IdEmpresa;
		public Long 			p_IdUsuario;
		public String			p_IdModulo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class AdelantarPautaSalida {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public AdelantarPautaSalida() {
			P_OUT_MENSAJES = new ArrayList<>();
			P_OUT_ERRORESCONTADOR = 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	
	
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = getGsonBuilder();		
		AdelantarPautaEntrada in=gson.fromJson(jin.toString() ,AdelantarPautaEntrada.class);
		AdelantarPautaSalida out=logica(con,in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public static void main(String[] args) throws SQLException, Exception {
		
	    EntregarSistemaManual demo=new EntregarSistemaManual();		
		AdelantarPautaEntrada entrada = new AdelantarPautaEntrada();
		entrada.p_IdEmpresa = 1L;
		entrada.p_IdModulo = "PLA";
		entrada.p_IdUsuario = 1L;
		System.out.println(demo.logica(PlataformaUtils.getDESAConnection(),entrada ).toString());
	    
	}
	
	public static class EstadoModulo {
		public String IdModulo;
		public String NombreModulo;
		public String IdEstado;
		public String CodigoUsuario;
		public Long IdEmpresa;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class PermisoEntrega {
			public Long Cantidad;
	}
	
	
	public static class Usuario {
		public String CodigoUsuario;
		public Long IdUsuario;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public AdelantarPautaSalida logica(Connection conn, AdelantarPautaEntrada entrada) throws Exception {
		AdelantarPautaSalida out = new AdelantarPautaSalida();
		SQL _sql = new SQL();
		
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(conn, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			return out;
		}
		
		HashMap<String,Object> params = new HashMap<>();
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		
		List<PermisoEntrega> permiso = _sql.query(conn, SQL_PERMISO_ENTREGA, params , PermisoEntrega.class); 
		
		if (permiso.get(0).Cantidad > 0L) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_PUEDE_ENTREGAR_CON_ERRORES","No puede entregar Sistema si hay Errores."));
			return out;
		}
		
		
		
		List<EstadoModulo> modulos = _sql.query(conn, SQL_ESTADO_MODULOS, params , EstadoModulo.class); 
		
		List<EstadoModulo> modulosAbiertos = modulos.stream().filter(t -> !t.IdModulo.equalsIgnoreCase("SIS") && !t.IdEstado.equalsIgnoreCase("CER")).collect(Collectors.toList());
		List<EstadoModulo> moduloSistema = modulos.stream().filter(t -> t.IdModulo.equalsIgnoreCase("SIS")).collect(Collectors.toList());
		
		if (modulosAbiertos.size() > 0) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_EXISTEN_MODULOS_ABIERTOS","No se puede entregar sistema, existen modulos abiertos"));
			out.P_OUT_ERRORESCONTADOR++;
			return out;
		}
		
		if (moduloSistema.size() == 0) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_MODULO_SISTEMA_NO_DEFINIDO","El modulo Sistema no se encuentra definido"));
			out.P_OUT_ERRORESCONTADOR++;
			return out;
		}
		
		if (moduloSistema.get(0).IdEstado.equalsIgnoreCase("CER")) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_MODULO_SISTEMA_CERRADO","El modulo Sistema se encuentra cerrado. Debe finalizar la pauta para abrir el Sistema"));
			out.P_OUT_ERRORESCONTADOR++;
			return out;
		}
		else 
		{	
			params.clear();
			params.put("IdUsuario",entrada.p_IdUsuario);
			List<Usuario> usuario = _sql.query(conn, SQL_SELECT_USUARIO, params, Usuario.class);
			
			EstadoModulo e = new EstadoModulo();
			e.CodigoUsuario = usuario.get(0).CodigoUsuario;
			e.IdEmpresa = entrada.p_IdEmpresa;
			e.IdEstado = "CER";
			e.IdModulo = "SIS";
			
			List<EstadoModulo> l = new ArrayList<>();
			l.add(e);
			
			_sql.batch(conn, SQL_UPDATE_MODULO_SISTEMA, l, EstadoModulo.class);
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,"UI_MODULO_SISTEMA_CERRADO","Modulo Sistema Cerrado. Pauta Programada"));
		
			AgregarPrograma agregarPrograma = new AgregarPrograma();
			InAgregarPrograma inAgregarPrograma = new InAgregarPrograma();
			inAgregarPrograma.p_IdEmpresa = entrada.p_IdEmpresa;
			inAgregarPrograma.p_IdUsuario = entrada.p_IdUsuario;
			inAgregarPrograma.p_IdModulo = "PLA";
			inAgregarPrograma.p_IdTipoPrograma = "PAUTA_BASE";
			inAgregarPrograma.p_Segundos = 0L;
			
			InAgregarPrograma.Parametro parametro = new InAgregarPrograma.Parametro();
			parametro.NombreParametro = "IdEmpresa";
			parametro.Valor = entrada.p_IdEmpresa+"";
			inAgregarPrograma.p_Parametros.add(parametro);
			
			parametro = new InAgregarPrograma.Parametro();
			parametro.NombreParametro = "IdUsuario";
			parametro.Valor = entrada.p_IdUsuario+"";
			inAgregarPrograma.p_Parametros.add(parametro);
			
			
			AgregarPrograma.OutAgregarPrograma outAgregarPrograma = agregarPrograma.logica(conn, inAgregarPrograma);
			
			if (outAgregarPrograma.P_OUT_IDPROGRAMA <= 0L) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_INICIAR_PAUTA","Error al iniciar pauta de procesos"));
				return out;
			}
			
			return out;

		}	
		
	}
	
	
}
