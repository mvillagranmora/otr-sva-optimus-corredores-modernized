package cl.bcs.plataforma.tests;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Test;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.EntradaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.SalidaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.ModificarPersonaDatosGeneralesMasivo;
import cl.bcs.socionegocio.per.pojo.RegistroCambioMasivo;
import cl.bcs.utils.SQL;


class TestModificarPersonaDatosGeneralesMasivo {
	String sql = "SELECT top 1 \"IdPersona\" FROM \"BSCL.SocioNegocio.BD::PER.Persona\" WHERE \"IdPaisResidencia\" != 223";
	
	public static class SelectIdPersona{
		public Long IdPersona;
	} 
	
	@Test
    void testModificarPersonaDatosGeneralesMasivo() throws Exception {
    	try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
    		SQL _sql = new SQL();
    		List<SelectIdPersona> c = _sql.query(conn, sql, new HashMap<>(), SelectIdPersona.class);
    		
    		ModificarPersonaDatosGeneralesMasivo demo = new ModificarPersonaDatosGeneralesMasivo();
    		
    		List<RegistroCambioMasivo> registrosCambios = new ArrayList<RegistroCambioMasivo>();
    		RegistroCambioMasivo registroCambios = new RegistroCambioMasivo();
    		registroCambios.CampoModificado = "IdPaisResidencia";
    		registroCambios.ValorNuevo = "223";
    		registroCambios.IdEntidad = c.get(0).IdPersona;
    		registrosCambios.add(registroCambios);
    		
    		EntradaEjecutarCambiosPersona entradaCambiosPersona = new EntradaEjecutarCambiosPersona();
    		entradaCambiosPersona.p_IdEmpresa = 1L;
    		entradaCambiosPersona.p_IdUsuario = 0L;
    		entradaCambiosPersona.p_IdPersona = c.get(0).IdPersona;
    		entradaCambiosPersona.p_IdModulo = "SOC";
    		entradaCambiosPersona.p_UsaReglasNegocio = "S";
    		entradaCambiosPersona.p_RegistroCambio = registrosCambios;
    		
    		SalidaEjecutarCambiosPersona salida = demo.logica(conn, entradaCambiosPersona);
					
			assertTrue(salida.P_OUT_REFID > 0);
    		conn.rollback();
    	}
    }
}