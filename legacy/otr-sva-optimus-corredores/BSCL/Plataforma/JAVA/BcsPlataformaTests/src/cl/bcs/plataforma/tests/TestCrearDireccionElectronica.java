package cl.bcs.plataforma.tests;

import static org.junit.Assert.assertTrue;

import java.sql.Connection;

import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Test;


import cl.bcs.plataforma.pla.utils.PlataformaUtils;

import cl.bcs.socionegocio.per.CrearDireccionElectronica;
import cl.bcs.socionegocio.per.CrearDireccionElectronica.EntradaCrearDireccionElectronica;
import cl.bcs.socionegocio.per.CrearDireccionElectronica.SalidaCrearDireccionElectronica;
import cl.bcs.utils.SQL;


class TestCrearDireccionElectronica {
	String sql = "SELECT top 1 \"IdPersona\" FROM \"BSCL.SocioNegocio.BD::PER.Persona\";";
	
	public static class SelectIdPersona{
		public Long IdPersona;
	} 
	
	@Test
    void testCrearDireccionElectronica() throws Exception {
    	try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
    		SQL _sql = new SQL();
    		List<SelectIdPersona> c = _sql.query(conn, sql, new HashMap<>(), SelectIdPersona.class);
    		
    		
    		CrearDireccionElectronica demo = new CrearDireccionElectronica();
    		
    		EntradaCrearDireccionElectronica entradaDireccionElectronica = new EntradaCrearDireccionElectronica();  		
    		entradaDireccionElectronica.p_IdEmpresa = 1L;
    		entradaDireccionElectronica.p_IdUsuario = 0L;
    		entradaDireccionElectronica.p_IdModulo = "PER";
    		entradaDireccionElectronica.p_UsaReglasNegocio = "S";
    		entradaDireccionElectronica.p_Direccion = "PRUEBA@GMAIL.COM";
    		entradaDireccionElectronica.p_CodigoProposito = "PART";
    		entradaDireccionElectronica.p_Comentario = "";
    		entradaDireccionElectronica.p_IdEstado = "ACT";
    		entradaDireccionElectronica.p_Tipo = "";
    		entradaDireccionElectronica.p_IdPersona = c.get(0).IdPersona;
    		entradaDireccionElectronica.p_Predeterminado  = "S";
    		
    		SalidaCrearDireccionElectronica salida = demo.logica(conn, entradaDireccionElectronica);
					
			assertTrue(salida.p_out_RefId > 0);
    		conn.rollback();
    	}
    }
}