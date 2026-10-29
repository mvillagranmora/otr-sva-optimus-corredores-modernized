package cl.bcs.plataforma.tests;

import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Test;


import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.per.CrearDireccionFisica;
import cl.bcs.socionegocio.per.CrearDireccionFisica.EntradaCrearDireccionFisica;
import cl.bcs.socionegocio.per.CrearDireccionFisica.SalidaCrearDireccionFisica;
import cl.bcs.utils.SQL;


class TestCrearDireccionFisica {
	String sql = "SELECT top 1 \"IdPersona\" FROM \"BSCL.SocioNegocio.BD::PER.Persona\";";
	
	public static class SelectIdPersona{
		public Long IdPersona;
	} 
	
	@Test
    void testCrearDireccionFisica() throws Exception {
    	try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
    		SQL _sql = new SQL();
    		List<SelectIdPersona> c = _sql.query(conn, sql, new HashMap<>(), SelectIdPersona.class);
    		
    		
    		CrearDireccionFisica demo = new CrearDireccionFisica();
    		
    		EntradaCrearDireccionFisica entradaDireccionFisica = new EntradaCrearDireccionFisica();  		
    		entradaDireccionFisica.p_IdEmpresa = 1L;
    		entradaDireccionFisica.p_IdUsuario = 0L;
    		entradaDireccionFisica.p_IdModulo = "PER";
    		entradaDireccionFisica.p_UsaReglasNegocio = "S";
    		entradaDireccionFisica.p_Calle = "APOQUINDO";
    		entradaDireccionFisica.p_Numero = "123";
    		entradaDireccionFisica.p_Departamento = "";
    		entradaDireccionFisica.p_Sector = "";
    		entradaDireccionFisica.p_CodigoPostal = "";
    		entradaDireccionFisica.p_IdCiudad = 2931L;
    		entradaDireccionFisica.p_IdDivisionPais  = 299L;
    		entradaDireccionFisica.p_CodigoProposito = "PART";
    		entradaDireccionFisica.p_Comentario = "";
    		entradaDireccionFisica.p_IdEstado = "ACT";
    		entradaDireccionFisica.p_IdPersona = c.get(0).IdPersona;
    		entradaDireccionFisica.p_CodigoCompuesto = "";
    		entradaDireccionFisica.p_Tipo = "";
    		entradaDireccionFisica.p_Predeterminado = "S";
    		
    		SalidaCrearDireccionFisica salida = demo.logica(conn, entradaDireccionFisica);
					
			assertTrue(salida.p_out_RefId > 0);
    		conn.rollback();
    	}
    }
}