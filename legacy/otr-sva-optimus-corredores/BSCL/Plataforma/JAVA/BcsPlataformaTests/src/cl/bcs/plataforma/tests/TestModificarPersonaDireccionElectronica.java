package cl.bcs.plataforma.tests;

import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Test;


import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.per.ModificarPersonaDireccionElectronica;
import cl.bcs.socionegocio.per.ModificarPersonaDireccionElectronica.SalidaModificarPersonaDireccionElectronica;
import cl.bcs.socionegocio.per.ModificarPersonaDireccionElectronica.EntradaModificarPersonaDireccionElectronica;
import cl.bcs.socionegocio.per.ModificarPersonaDireccionElectronica.RegistroCambio;
import cl.bcs.utils.SQL;


class TestModificarPersonaDireccionElectronica {
	String sql = "SELECT top 1 \"IdDireccionElectronica\" FROM \"BSCL.SocioNegocio.BD::PER.DireccionElectronica\";";
	
	public static class SelectIdDireccionElectronica{
		public Long IdDireccionElectronica;
	} 
	
	@Test
    void testModificarDireccionElectronica() throws Exception {
    	try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
    		SQL _sql = new SQL();
    		List<SelectIdDireccionElectronica> c = _sql.query(conn, sql, new HashMap<>(), SelectIdDireccionElectronica.class);
    		   		
    		ModificarPersonaDireccionElectronica demo = new ModificarPersonaDireccionElectronica();
    		
    		List<RegistroCambio> registrosCambio = new ArrayList<RegistroCambio>();
    		RegistroCambio registroCambio = new RegistroCambio();
    		registroCambio.CampoModificado = "p_Direccion";
    		registroCambio.ValorNuevo = "CAMBIOPRUEBA@GMAIL.COM";
    		registrosCambio.add(registroCambio);
    		
    		EntradaModificarPersonaDireccionElectronica entradaDireccionElectronica = new EntradaModificarPersonaDireccionElectronica();  		
    		entradaDireccionElectronica.p_IdEmpresa = 1L;
    		entradaDireccionElectronica.p_IdUsuario = 0L;
    		entradaDireccionElectronica.p_IdModulo = "PER";
    		entradaDireccionElectronica.p_UsaReglasNegocio = "S";
    		entradaDireccionElectronica.p_IdDireccionElectronica = c.get(0).IdDireccionElectronica;
    		entradaDireccionElectronica.p_RegistroCambio = registrosCambio;
    		
    		SalidaModificarPersonaDireccionElectronica salida = demo.logica(conn, entradaDireccionElectronica);
					
			assertTrue(salida.P_OUT_REFID > 0);
    		conn.rollback();
    	}
    }
}