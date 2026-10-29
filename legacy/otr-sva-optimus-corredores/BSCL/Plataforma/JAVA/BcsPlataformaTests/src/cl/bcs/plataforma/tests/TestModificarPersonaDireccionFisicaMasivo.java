package cl.bcs.plataforma.tests;

import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Test;


import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.EntradaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.SalidaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.pojo.RegistroCambioMasivo;
import cl.bcs.socionegocio.per.ModificarPersonaDireccionFisicaMasivo;
import cl.bcs.utils.SQL;


class TestModificarPersonaDireccionFisicaMasivo {
	String sql = "SELECT top 1 \"IdDireccionFisica\" FROM \"BSCL.SocioNegocio.BD::PER.DireccionFisica\";";
	
	public static class SelectIdDireccionFisica{
		public Long IdDireccionFisica;
	} 
	
	@Test
    void testModificarDireccionFisica() throws Exception {
    	try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
    		SQL _sql = new SQL();
    		List<SelectIdDireccionFisica> c = _sql.query(conn, sql, new HashMap<>(), SelectIdDireccionFisica.class);
    		   		
    		ModificarPersonaDireccionFisicaMasivo demo = new ModificarPersonaDireccionFisicaMasivo();
    		
    		List<RegistroCambioMasivo> registrosCambios = new ArrayList<RegistroCambioMasivo>();
    		RegistroCambioMasivo registroCambios = new RegistroCambioMasivo();
    		registroCambios.CampoModificado = "Predeterminado";
    		registroCambios.ValorNuevo = "N";
    		registroCambios.IdEntidad = c.get(0).IdDireccionFisica;
    		registrosCambios.add(registroCambios);
    		
    		EntradaEjecutarCambiosPersona entradaDireccionFisica = new EntradaEjecutarCambiosPersona();  		
    		entradaDireccionFisica.p_IdEmpresa = 1L;
    		entradaDireccionFisica.p_IdUsuario = 0L;
    		entradaDireccionFisica.p_IdModulo = "PER";
    		entradaDireccionFisica.p_UsaReglasNegocio = "S";
    		entradaDireccionFisica.p_IdTipoCanal = "OPT";
    		entradaDireccionFisica.p_EsCrearCliente = "N";
    		entradaDireccionFisica.p_RegistroCambio = registrosCambios;    		 		
    	
    		SalidaEjecutarCambiosPersona salida = demo.logica(conn, entradaDireccionFisica);
					
			assertTrue(salida.P_OUT_REFID > 0);
    		conn.rollback();
    	}
    }
}