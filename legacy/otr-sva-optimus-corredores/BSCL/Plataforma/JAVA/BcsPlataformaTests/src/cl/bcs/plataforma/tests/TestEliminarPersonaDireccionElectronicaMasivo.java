package cl.bcs.plataforma.tests;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import org.junit.jupiter.api.Test;


import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.SalidaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.EliminarPersonaDireccionElectronicaMasivo;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.EntradaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.pojo.RegistroCambioMasivo;
import cl.bcs.utils.SQL;


class TestEliminarPersonaDireccionElectronicaMasivo {	
		
	String sql = "SELECT top 1 \"IdDireccionElectronica\" FROM \"BSCL.SocioNegocio.BD::PER.DireccionElectronica\" WHERE \"IdPersona\" IN (SELECT \"IdPersona\" FROM \"BSCL.SocioNegocio.BD::PER.DireccionElectronica\" WHERE \"Borrado\" LIKE 'N'AND \"Predeterminado\" LIKE 'N' GROUP BY \"IdPersona\" HAVING count(\"IdDireccionElectronica\") > 1)";
	
	public static class SelectIdDirElectronica{
		public Long IdDireccionElectronica;
	} 
	
	@Test
    void testEliminarDireccionElectronicaMasivo() throws Exception {
    	try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
    		
    		SQL _sql = new SQL();
    		List<SelectIdDirElectronica> c = _sql.query(conn, sql, new HashMap<>(), SelectIdDirElectronica.class);
    		
    		EliminarPersonaDireccionElectronicaMasivo demo = new EliminarPersonaDireccionElectronicaMasivo(); 
    		List<RegistroCambioMasivo> registrosCambio = new ArrayList<RegistroCambioMasivo>();
    		RegistroCambioMasivo registroCambio = new RegistroCambioMasivo();
    		registroCambio.CampoModificado = "Borrado";
    		registroCambio.ValorNuevo = "S";
    		registroCambio.IdEntidad = c.get(0).IdDireccionElectronica;
    		registrosCambio.add(registroCambio);
    		
    		EntradaEjecutarCambiosPersona entrada = new EntradaEjecutarCambiosPersona();
    		entrada.p_IdEmpresa = 1L;
    		entrada.p_IdUsuario = 0L;
    		entrada.p_IdPersona = 0L;
    		entrada.p_IdModulo = "PER";
    		entrada.p_UsaReglasNegocio = "S";
    		entrada.p_IdTipoCanal = "OPT";
    		entrada.p_EsCrearCliente = "N";
    		entrada.p_RegistroCambio = registrosCambio;
    		
    		
    		SalidaEjecutarCambiosPersona salida = demo.logica(conn, entrada);
					
    		assertTrue(salida.P_OUT_ERRORESCONTADOR == 0);
    		assertTrue(salida.P_OUT_REFID > 0);
			conn.rollback();
    	}
    }
    	
}