package cl.bcs.plataforma.pla;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class RevisarRequisitosBasicos implements JavaCall {
	
	private final String SQL_CONSULTAR_EMPRESA_SOCIO = loadResourceToString("cl/bcs/plataforma/pla/sql/RevisarRequisitosBasicos_ConsultarSocioDeNegocio.sql");
	private final String SQL_CONSULTAR_USUARIO = loadResourceToString("cl/bcs/plataforma/pla/sql/RevisarRequisitosBasicos_ConsultarUsuario.sql");
	private final String SQL_CONSULTAR_MODULO = loadResourceToString("cl/bcs/plataforma/pla/sql/RevisarRequisitosBasicos_ConsultarModulo.sql");
	
	@Trace
	public RevisarRequisitosBasicosSalida logica(Connection conn,Long p_IdEmpresa,Long p_IdUsuario,String p_IdModulo) throws Exception {
		RevisarRequisitosBasicosSalida salida = new RevisarRequisitosBasicosSalida();
		
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<>();
		
		if (p_IdEmpresa == null) {
			String codigo = "EMPRESA_VACIA";
			String desc = "Empresa vacia";
			salida.p_out_Mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, codigo, desc));
			return salida;
		}
		else {
			params.put("IdEmpresa", p_IdEmpresa);
			List<EmpresaSocio> datosEmpresa = sql.query(conn, SQL_CONSULTAR_EMPRESA_SOCIO, params, EmpresaSocio.class);
			if (datosEmpresa.size() == 0) {
				String codigo = "EMPRESA_NO_EXISTE";
				String desc = Diccionario.getMensaje(conn, p_IdEmpresa,codigo).msg();
				salida.p_out_Mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, codigo, desc));
			}
			else {
				if (datosEmpresa.get(0).IdSocioDeNegocioEmpresa == null || datosEmpresa.get(0).IdSocioDeNegocioEmpresa == 0) {
					
					String codigo = "SIN_DEFINIR_SOCIO_DE_NEGOCIO_PARA_EMPRESA";
					String desc = Diccionario.getMensaje(conn, p_IdEmpresa,codigo).msg();
					salida.p_out_Mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, codigo, desc));
				}
				if (datosEmpresa.get(0).IdSocioDeNegocio == null || datosEmpresa.get(0).IdSocioDeNegocio == 0) {
					String codigo = "SOCIO_DE_NEGOCIO_DE_EMPRESA_NO_EXISTE";
					String desc = Diccionario.getMensaje(conn, p_IdEmpresa,codigo).msg();
					salida.p_out_Mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, codigo, desc));
					
				}
				/*if (datosEmpresa.get(0).IdContraparte == null || datosEmpresa.get(0).IdContraparte == 0) {
					salida.p_out_Mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "EMPRESA_NO_CONFIGURADA_COMO_CONTRAPARTE", ""));
				}*/
				if (datosEmpresa.get(0).IdCliente == null || datosEmpresa.get(0).IdCliente == 0) {
					String codigo = "EMPRESA_NO_CONFIGURADA_COMO_CLIENTE";
					String desc = Diccionario.getMensaje(conn, p_IdEmpresa,codigo).msg();
					salida.p_out_Mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, codigo, desc));
					
				}
			}
		}
		
		if (p_IdUsuario == null) {
			String codigo = "USUARIO_VACIO";
			String desc = Diccionario.getMensaje(conn,p_IdEmpresa, codigo).msg();
			salida.p_out_Mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, codigo, desc));
		}
		else {
			params.clear();
			params.put("IdUsuario", p_IdUsuario);
			params.put("IdEmpresa", p_IdEmpresa);
			List<Usuario> datosUsuario = sql.query(conn, SQL_CONSULTAR_USUARIO, params, Usuario.class);
			if (datosUsuario.size() == 0) {
				String codigo = "USUARIO_NO_EXISTE";
				String desc = Diccionario.getMensaje(conn, p_IdEmpresa,codigo).msg();
				salida.p_out_Mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, codigo, desc));
			}
			else if (!datosUsuario.get(0).IdEstado.equalsIgnoreCase("ACT")) {
				String codigo = "USUARIO_NO_VIGENTE";
				String desc = Diccionario.getMensaje(conn, p_IdEmpresa,codigo).msg();
				salida.p_out_Mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, codigo, desc));
			}
		}
		

		if (p_IdModulo == null) {
			String codigo = "MODULO_VACIO";
			String desc = Diccionario.getMensaje(conn,p_IdEmpresa, codigo).msg();
			salida.p_out_Mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, codigo, desc));
		}
		else {
			params.clear();
			params.put("IdModulo", p_IdModulo);
			List<Modulo> datosModulo = sql.query(conn, SQL_CONSULTAR_MODULO, params, Modulo.class);
			if (datosModulo.size() == 0) {
				String codigo = "MODULO_NO_EXISTE";
				String desc = Diccionario.getMensaje(conn, p_IdEmpresa,codigo).msg();
				salida.p_out_Mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, codigo, desc));
			}
		}
		
		
		
		return salida;
	}
	
	public static class EmpresaSocio {
		public Long IdEmpresa;
		public Long IdSocioDeNegocioEmpresa;
		public Long IdSocioDeNegocio;
		public Long IdContraparte;
		public Long IdCliente;
	}
	
	public static class Modulo {
		public String IdModulo;
	}
	
	public static class Usuario {
		public Long IdUsuario;
		public String IdEstado;
	}

	public static class RevisarRequisitosBasicosSalida {
		public List<MensajeSalida> p_out_Mensajes;
		
		public RevisarRequisitosBasicosSalida() {
			this.p_out_Mensajes = new ArrayList<MensajeSalida>();
		}

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	

}