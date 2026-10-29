package cl.bcs.plataforma.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import org.junit.jupiter.api.Test;


import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.per.ModificarFatcaPaisResidencia;
import cl.bcs.socionegocio.per.ModificarFatcaPaisResidencia.EntradaModificarFatcaPaisResidencia;
import cl.bcs.socionegocio.per.ModificarFatcaPaisResidencia.SalidaModificarFatcaPaisResidencia;
import cl.bcs.socionegocio.per.pojo.FatcaPaisResidencia;
import cl.bcs.utils.SQL;


class TestModificarFatcaPaisResidencia {	
		
	String sql = "SELECT top 1 \"IdPersona\" FROM \"BSCL.SocioNegocio.BD::PER.FatcaPaisResidencia\" WHERE \"IdPaisResidencia\" != 44";
	
	public static class SelectFatcaPaisResidencia{
		public Long IdPersona;
	} 
	
	@Test
    void testModificarFatcaPaisResidencia() throws Exception {
    	try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
    		
    		SQL _sql = new SQL();
    		List<SelectFatcaPaisResidencia> c = _sql.query(conn, sql, new HashMap<>(), SelectFatcaPaisResidencia.class);
    		
    		ModificarFatcaPaisResidencia demo = new ModificarFatcaPaisResidencia(); 
    		List<FatcaPaisResidencia> fatcas = new ArrayList<FatcaPaisResidencia>();
    		FatcaPaisResidencia fatca = new FatcaPaisResidencia();
    		fatca.IdPersona = c.get(0).IdPersona;
    		fatca.IdPaisResidencia = 44L;
    		fatca.TIN = "";
    		fatca.IdCausaNoTin = "OTRA";
    		fatca.Observacion = "Pendiente de ingreso";
    		fatca.PaisResidenciaPrincipal = "S";
    		fatcas.add(fatca);
    		
    		EntradaModificarFatcaPaisResidencia entrada = new EntradaModificarFatcaPaisResidencia();
    		entrada.p_IdEmpresa = 1L;
    		entrada.p_IdUsuario = 0L;
    		entrada.p_FatcaPaisResidencia = fatcas;
    			
    		SalidaModificarFatcaPaisResidencia salida = demo.logica(conn, entrada);
					
    		assertEquals("UI_PROCESO_EJECUTADO_EXITO", salida.P_OUT_MENSAJES.get(0).codigo);

			conn.rollback();
    	}
    }
    	
}