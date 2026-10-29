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
import cl.bcs.socionegocio.per.ModificarPersonaDireccionElectronicaMasivo;
import cl.bcs.socionegocio.per.pojo.RegistroCambioMasivo;
import cl.bcs.socionegocio.per.ModificarPersonaDireccionFisicaMasivo;
import cl.bcs.utils.SQL;


class TestModificarPersonaDireccionElectronicaMasivo {
	String sql = "SELECT top 1 \"IdDireccionElectronica\" FROM \"BSCL.SocioNegocio.BD::PER.DireccionElectronica\";";
	
	public static class SelectIdDireccionElectronica{
		public Long IdDireccionElectronica;
	} 
	
	@Test
    void testModificarDireccionElectronicaMasivo() throws Exception {
    	try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
    		SQL _sql = new SQL();
    		List<SelectIdDireccionElectronica> c = _sql.query(conn, sql, new HashMap<>(), SelectIdDireccionElectronica.class);
    		   		
    		ModificarPersonaDireccionElectronicaMasivo demo = new ModificarPersonaDireccionElectronicaMasivo();
    		
    		List<RegistroCambioMasivo> registrosCambios = new ArrayList<RegistroCambioMasivo>();
    		RegistroCambioMasivo registroCambios = new RegistroCambioMasivo();
    		registroCambios.CampoModificado = "Direccion";
    		registroCambios.ValorNuevo = "CAMBIOPRUEBA@GMAIL.COM";
    		registroCambios.IdEntidad = c.get(0).IdDireccionElectronica;
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