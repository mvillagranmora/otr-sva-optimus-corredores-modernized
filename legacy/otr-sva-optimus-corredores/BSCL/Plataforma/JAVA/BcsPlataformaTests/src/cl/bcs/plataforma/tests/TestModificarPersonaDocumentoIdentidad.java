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
import cl.bcs.socionegocio.per.ModificarPersonaDocumentoIdentidadMasivo;
import cl.bcs.utils.SQL;


class TestModificarPersonaDocumentoIdentidad {	
	
String sql = "SELECT top 1 \"IdPersona\" FROM \"BSCL.SocioNegocio.BD::PER.DocumentoIdentidad\"";
	
	public static class SelectIdPersona{
		public Long IdPersona;
	} 
	
	@Test
    void testModificarPesonaDocumentoIdentidad() throws Exception {
    	try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
    		
    		SQL _sql = new SQL();
    		List<SelectIdPersona> c = _sql.query(conn, sql, new HashMap<>(), SelectIdPersona.class);
    		
    		ModificarPersonaDocumentoIdentidadMasivo demo = new ModificarPersonaDocumentoIdentidadMasivo(); 		   		
    		
    		List<RegistroCambioMasivo> registrosCambio = new ArrayList<RegistroCambioMasivo>();
    		RegistroCambioMasivo registroCambio = new RegistroCambioMasivo();	
    		registroCambio.CampoModificado = "FechaVencimiento";
    		registroCambio.ValorNuevo = "2043-01-01";
    		registroCambio.IdEntidad = c.get(0).IdPersona;
    		registrosCambio.add(registroCambio);
    		
    		EntradaEjecutarCambiosPersona entradaCambiosPersona = new EntradaEjecutarCambiosPersona();
    		entradaCambiosPersona.p_IdEmpresa = 1L;
    		entradaCambiosPersona.p_IdUsuario = 0L;
    		entradaCambiosPersona.p_IdPersona = 0L;
    		entradaCambiosPersona.p_IdModulo = "SOC";
    		entradaCambiosPersona.p_UsaReglasNegocio = "S";
    		entradaCambiosPersona.p_IdTipoCanal = "OPT";
    		entradaCambiosPersona.p_EsCrearCliente = "N";
    		entradaCambiosPersona.p_RegistroCambio = registrosCambio;
    		
    		SalidaEjecutarCambiosPersona salidaDocIdentidad = demo.logica(conn, entradaCambiosPersona);
					
			assertTrue(salidaDocIdentidad.P_OUT_REFID > 0);
    		conn.rollback();
    	}
    }
    	
}