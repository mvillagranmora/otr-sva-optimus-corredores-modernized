package cl.bcs.plataforma.tests;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Test;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.per.ModificarPersonaDatosGenerales.EntradaModificarPersonaDatosGenerales;
import cl.bcs.socionegocio.per.ModificarPersonaDatosGenerales.SalidaModificarPersonaDatosGenerales;
import cl.bcs.socionegocio.per.ModificarPersonaDatosGenerales;
import cl.bcs.socionegocio.per.pojo.RegistroCambio;
import cl.bcs.utils.SQL;


class TestModificarPersonaDatosGenerales {
	String sql = "SELECT top 1 \"IdPersona\" FROM \"BSCL.SocioNegocio.BD::PER.Persona\" WHERE \"IdPaisResidencia\" != 223";
	
	public static class SelectIdPersona{
		public Long IdPersona;
	} 
	
	@Test
    void testModificarPersonaDatosGenerales() throws Exception {
    	try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
    		SQL _sql = new SQL();
    		List<SelectIdPersona> c = _sql.query(conn, sql, new HashMap<>(), SelectIdPersona.class);
    		
    		ModificarPersonaDatosGenerales demo = new ModificarPersonaDatosGenerales();
    		
    		List<RegistroCambio> registrosCambio = new ArrayList<RegistroCambio>();
    		RegistroCambio registroCambio = new RegistroCambio();	
    		registroCambio.CampoModificado = "IdPaisResidencia";
    		registroCambio.ValorNuevo = "223";
    		registrosCambio.add(registroCambio);
    		
    		EntradaModificarPersonaDatosGenerales entradaCambiosPersona = new EntradaModificarPersonaDatosGenerales();
    		entradaCambiosPersona.p_IdEmpresa = 1L;
    		entradaCambiosPersona.p_IdUsuario = 0L;
    		entradaCambiosPersona.p_IdPersona = c.get(0).IdPersona;
    		entradaCambiosPersona.p_IdModulo = "SOC";
    		entradaCambiosPersona.p_UsaReglasNegocio = "S";
    		entradaCambiosPersona.p_RegistroCambio = registrosCambio;
    		
    		SalidaModificarPersonaDatosGenerales salida = demo.logica(conn, entradaCambiosPersona);
					
			assertTrue(salida.P_OUT_REFID > 0);
    		conn.rollback();
    	}
    }
}