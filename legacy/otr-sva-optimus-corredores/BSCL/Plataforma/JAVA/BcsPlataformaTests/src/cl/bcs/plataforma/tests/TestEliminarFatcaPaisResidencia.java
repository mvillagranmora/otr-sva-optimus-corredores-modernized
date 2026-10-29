package cl.bcs.plataforma.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import org.junit.jupiter.api.Test;


import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.per.EliminarFatcaPaisResidencia;
import cl.bcs.socionegocio.per.EliminarFatcaPaisResidencia.FatcaPaisResidencia;
import cl.bcs.socionegocio.per.EliminarFatcaPaisResidencia.EntradaEliminarFatcaPaisResidencia;
import cl.bcs.socionegocio.per.EliminarFatcaPaisResidencia.SalidaEliminarFatcaPaisResidencia;
import cl.bcs.utils.SQL;


class TestEliminarFatcaPaisResidencia {	
		
	String sql = "SELECT top 1 \"IdPersona\", \"IdPaisResidencia\"  FROM \"BSCL.SocioNegocio.BD::PER.FatcaPaisResidencia\" WHERE \"IdPaisResidencia\" != 44";
	
	public static class SelectIdPersona{
		public Long IdPersona;
		public Long IdPaisResidencia;
	} 
	
	@Test
    void testEliminarFatcaPaisResidencia() throws Exception {
    	try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
    		
    		SQL _sql = new SQL();
    		List<SelectIdPersona> c = _sql.query(conn, sql, new HashMap<>(), SelectIdPersona.class);
    		
    		EliminarFatcaPaisResidencia demo = new EliminarFatcaPaisResidencia(); 
    		List<FatcaPaisResidencia> fatcasPaisResidencia = new ArrayList<FatcaPaisResidencia>();
    		FatcaPaisResidencia fatcaPaisResidencia = new FatcaPaisResidencia();
    		fatcaPaisResidencia.IdPersona = c.get(0).IdPersona;
    		fatcaPaisResidencia.IdPaisResidencia = c.get(0).IdPaisResidencia;
    		fatcasPaisResidencia.add(fatcaPaisResidencia);
    		
    		EntradaEliminarFatcaPaisResidencia entradaCrearFacta = new EntradaEliminarFatcaPaisResidencia();
    		entradaCrearFacta.p_IdEmpresa = 1L;
    		entradaCrearFacta.p_FatcaPaisResidencia = fatcasPaisResidencia;
    		
    		
    		SalidaEliminarFatcaPaisResidencia salidaCrearFacta = demo.logica(conn, entradaCrearFacta);
					
			assertEquals("UI_PROCESO_EJECUTADO_EXITO", salidaCrearFacta.P_OUT_MENSAJES.get(0).codigo);
    		assertTrue(salidaCrearFacta.P_OUT_ERRORESCONTADOR == 0);
			conn.rollback();
    	}
    }
    	
}