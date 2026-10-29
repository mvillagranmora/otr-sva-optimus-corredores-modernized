package cl.bcs.plataforma.tests;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.ejb.BeforeCompletion;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cl.bcs.plataforma.acc.CrearUsuario;
import cl.bcs.plataforma.acc.CrearUsuario.EntradaCrearUsuario;
import cl.bcs.plataforma.acc.CrearUsuario.SalidaCrearUsuario;
import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.SQL;

class TestCrearUsuario {

	
	public static class Usuario {
		public Long IdUsuario;
		public Long IdEntidad;
	}
	
	public static String SELECT_USUARIO_SEGURIDAD = 
			"SELECT TOP 1 usu.\"IdUsuario\" FROM \"BSCL.Plataforma.BD::ACC.Usuario\" usu "
			+ "LEFT JOIN \"BSCL.Plataforma.BD::ACC.RolUsuario\" rolusu on usu.\"IdUsuario\" = rolusu.\"IdUsuario\" "
			+ "LEFT JOIN \"BSCL.Plataforma.BD::ACC.Rol\" rol on rolusu.\"IdRol\" = rol.\"IdRol\" "
			+ "WHERE rol.\"Nombre\" = 'SEGURIDAD' ";
	
	public static String SELECT_SOCIO_SISTEMA = 
			"SELECT usu.\"IdEntidad\" FROM \"BSCL.Plataforma.BD::ACC.Usuario\" usu "
			+ "WHERE usu.\"CodigoUsuario\" = 'SISTEMA' ";
	
	public static String SELECT_SOCIO_CLIENTE = 
			"SELECT TOP 1 usu.\"IdEntidad\" FROM \"BSCL.Plataforma.BD::ACC.Usuario\" usu "
			+ "WHERE usu.\"TipoEntidad\" = 'CLI'";
	
	@Test
    void CrearUsuarioCliente() throws Exception {
    	try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01","connections.properties")) {
    		
    		CrearUsuario crearUsuario = new CrearUsuario();
    		EntradaCrearUsuario entradaCrearUsuario = new EntradaCrearUsuario();
    		
    		SQL sql = new SQL();
    		List<Usuario> usuarioSeguridad = sql.query(conn, SELECT_USUARIO_SEGURIDAD, new HashMap<String,Object>(), Usuario.class);
    		//obtener un usuario con rol seguridad
    		assertTrue(usuarioSeguridad.get(0).IdUsuario > 0);
    		
    		//obtener la entidad del usuario sistema
    		List<Usuario> entidadSistema = sql.query(conn, SELECT_SOCIO_CLIENTE, new HashMap<String,Object>(), Usuario.class);
    		assertTrue(entidadSistema.get(0).IdEntidad > 0);
    		
    		entradaCrearUsuario.p_IdUsuario = usuarioSeguridad.get(0).IdUsuario;
    		entradaCrearUsuario.p_Canal = "AGE";
    		entradaCrearUsuario.p_IdEmpresa = 1L;
    		entradaCrearUsuario.p_Clave = "";
    		entradaCrearUsuario.p_CodigoUsuario = "TEST";
    		entradaCrearUsuario.p_HeredarRolesUsuario = 0L;
    		entradaCrearUsuario.p_IdEntidad = entidadSistema.get(0).IdEntidad;
    		entradaCrearUsuario.p_TipoEntidad = "CLI";
    		entradaCrearUsuario.p_FechaIngreso = "0001-01-01";
    		entradaCrearUsuario.p_FechaVigencia = "0001-01-01";
    		entradaCrearUsuario.p_Conectado = "S";
    		entradaCrearUsuario.p_Terminal = "";
    		entradaCrearUsuario.p_IdEstado = "ACT";
    		entradaCrearUsuario.p_IntentosConexion = 0;
    		
			SalidaCrearUsuario salidaCrearUsuario = crearUsuario.logica(conn, entradaCrearUsuario);
			
    		assertTrue(salidaCrearUsuario.P_OUT_NEW_USER_ID > 0);
    	}
    }
	
    @Test
    void CrearUsuarioEmpleado() throws Exception {
    	try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01","connections.properties")) {
    		
    		CrearUsuario crearUsuario = new CrearUsuario();
    		EntradaCrearUsuario entradaCrearUsuario = new EntradaCrearUsuario();
    		
    		SQL sql = new SQL();
    		List<Usuario> usuarioSeguridad = sql.query(conn, SELECT_USUARIO_SEGURIDAD, new HashMap<String,Object>(), Usuario.class);
    		//obtener un usuario con rol seguridad
    		assertTrue(usuarioSeguridad.get(0).IdUsuario > 0);
    		
    		//obtener la entidad del usuario sistema
    		List<Usuario> entidadSistema = sql.query(conn, SELECT_SOCIO_SISTEMA, new HashMap<String,Object>(), Usuario.class);
    		assertTrue(entidadSistema.get(0).IdEntidad > 0);
    		
    		entradaCrearUsuario.p_IdUsuario = usuarioSeguridad.get(0).IdUsuario;
    		entradaCrearUsuario.p_Canal = "AGE";
    		entradaCrearUsuario.p_IdEmpresa = 1L;
    		entradaCrearUsuario.p_Clave = "";
    		entradaCrearUsuario.p_CodigoUsuario = "TEST";
    		entradaCrearUsuario.p_HeredarRolesUsuario = 0L;
    		entradaCrearUsuario.p_IdEntidad = entidadSistema.get(0).IdEntidad;
    		entradaCrearUsuario.p_TipoEntidad = "EMP";
    		entradaCrearUsuario.p_FechaIngreso = "0001-01-01";
    		entradaCrearUsuario.p_FechaVigencia = "0001-01-01";
    		entradaCrearUsuario.p_Conectado = "S";
    		entradaCrearUsuario.p_Terminal = "";
    		entradaCrearUsuario.p_IdEstado = "ACT";
    		entradaCrearUsuario.p_IntentosConexion = 0;
    		
			SalidaCrearUsuario salidaCrearUsuario = crearUsuario.logica(conn, entradaCrearUsuario);
			
    		assertTrue(salidaCrearUsuario.P_OUT_NEW_USER_ID > 0);
    	}
    }
    
    

}