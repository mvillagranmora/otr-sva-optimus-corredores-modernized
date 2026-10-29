package cl.bcs.plataforma.tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Test;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.portafolio.CrearPortafolio;
import cl.bcs.portafolio.CrearPortafolio.EntradaCrearPortafolio;
import cl.bcs.portafolio.CrearPortafolio.SalidaCrearPortafolio;
import cl.bcs.utils.SQL;

class TestCrearPortafolio {
	String sql = "SELECT TOP 1 \"IdCliente\"  FROM  \"BSCL.Portafolio.BD::POR.Portafolio\" WHERE \"IdCliente\" NOT IN (SELECT \"IdCliente\" FROM \"BSCL.Portafolio.BD::POR.Portafolio\" WHERE \"CodigoPortafolio\" like 96);";
	
	public static class SelectCliente{
		public Long IdCliente;
	} 
	
    @Test
    void testCrearPortafolio() throws Exception {
    	try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
    		SQL _sql = new SQL();
    		List<SelectCliente> c = _sql.query(conn, sql, new HashMap<>(), SelectCliente.class);
    		
    		CrearPortafolio demo = new CrearPortafolio();
			
    		EntradaCrearPortafolio crearPortafolioEntrada = new EntradaCrearPortafolio();
    		crearPortafolioEntrada.p_IdEmpresa = 1L;
    		crearPortafolioEntrada.p_IdUsuario = 0L;
    		crearPortafolioEntrada.p_IdModulo = "SOC";
    		crearPortafolioEntrada.p_UsaReglasNegocio = "S";
    		crearPortafolioEntrada.p_CodigoPortafolio = "96";
    		crearPortafolioEntrada.p_DescripcionPortafolio = "TEST NEWPOR";
    		crearPortafolioEntrada.p_PropositoPortafolio = "PROPGENERALES";
    		crearPortafolioEntrada.p_DesdeEnrolamiento = "N";
    		crearPortafolioEntrada.p_CodigoRegimen = "";
    		crearPortafolioEntrada.p_NombrePlan = "";
    		crearPortafolioEntrada.p_CodigoAgrupador = "";
    		crearPortafolioEntrada.p_IdCliente = c.get(0).IdCliente;
    		crearPortafolioEntrada.p_IdPerfilComercialBase = "ARRIESGADO";
    		
    		SalidaCrearPortafolio salida = demo.logica(conn, crearPortafolioEntrada);
					
			assertNotNull(salida.p_out_RefId);
			assertTrue(salida.p_out_RefId > 0);
			
    		conn.rollback();
    	}
    }
    
    void testCrearPortafolioDesdeEnrolamiento() throws Exception {
    	try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
    		SQL _sql = new SQL();
    		List<SelectCliente> c = _sql.query(conn, sql, new HashMap<>(), SelectCliente.class);
    		
    		CrearPortafolio demo = new CrearPortafolio();
			
    		EntradaCrearPortafolio crearPortafolioEntrada = new EntradaCrearPortafolio();
    		crearPortafolioEntrada.p_IdEmpresa = 1L;
    		crearPortafolioEntrada.p_IdUsuario = 0L;
    		crearPortafolioEntrada.p_IdModulo = "SOC";
    		crearPortafolioEntrada.p_UsaReglasNegocio = "S";
    		crearPortafolioEntrada.p_CodigoPortafolio = "96";
    		crearPortafolioEntrada.p_DescripcionPortafolio = "TEST NEWPOR";
    		crearPortafolioEntrada.p_PropositoPortafolio = "PROPGENERALES";
    		crearPortafolioEntrada.p_DesdeEnrolamiento = "S";
    		crearPortafolioEntrada.p_CodigoRegimen = "";
    		crearPortafolioEntrada.p_NombrePlan = "";
    		crearPortafolioEntrada.p_CodigoAgrupador = "";
    		crearPortafolioEntrada.p_IdCliente = c.get(0).IdCliente;
    		crearPortafolioEntrada.p_IdPerfilComercialBase = "ARRIESGADO";
    		
    		SalidaCrearPortafolio salida = demo.logica(conn, crearPortafolioEntrada);
					
			assertNotNull(salida.p_out_RefId);
			assertTrue(salida.p_out_RefId > 0);
			
    		conn.rollback();
    	}
    }
    
}