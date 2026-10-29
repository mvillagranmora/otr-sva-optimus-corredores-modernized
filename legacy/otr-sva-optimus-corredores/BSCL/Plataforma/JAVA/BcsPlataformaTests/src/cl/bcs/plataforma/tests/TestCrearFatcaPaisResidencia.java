package cl.bcs.plataforma.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import org.junit.jupiter.api.Test;


import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.per.CrearFatcaPaisResidencia;
import cl.bcs.socionegocio.per.CrearFatcaPaisResidencia.EntradaCrearFatcaPaisResidencia;
import cl.bcs.socionegocio.per.CrearFatcaPaisResidencia.SalidaCrearFatcaPaisResidencia;
import cl.bcs.socionegocio.per.pojo.FatcaPaisResidencia;
import cl.bcs.utils.SQL;


class TestCrearFatcaPaisResidencia {	
		
	String sql = "SELECT top 1 \"IdPersona\" FROM \"BSCL.SocioNegocio.BD::PER.DocumentoIdentidad\" WHERE \"IdPersona\" NOT IN (SELECT \"IdPersona\" FROM \"BSCL.SocioNegocio.BD::PER.FatcaPaisResidencia\")";
	
	public static class SelectIdPersona{
		public Long IdPersona;
	} 
	
	@Test
    void testCrearFatcaPaisResidencia() throws Exception {
    	try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
    		
    		SQL _sql = new SQL();
    		List<SelectIdPersona> c = _sql.query(conn, sql, new HashMap<>(), SelectIdPersona.class);
    		
    		CrearFatcaPaisResidencia demo = new CrearFatcaPaisResidencia(); 
    		List<FatcaPaisResidencia> fatcasPaisResidencia = new ArrayList<FatcaPaisResidencia>();
    		FatcaPaisResidencia fatcaPaisResidencia = new FatcaPaisResidencia();
    		fatcaPaisResidencia.IdPersona = c.get(0).IdPersona;
    		fatcaPaisResidencia.IdPaisResidencia = 44L;
    		fatcaPaisResidencia.TIN = "";
    		fatcaPaisResidencia.IdCausaNoTin = "OTRA";
    		fatcaPaisResidencia.Observacion = "Pendiente de ingreso";
    		fatcaPaisResidencia.PaisResidenciaPrincipal = "S";
    		fatcasPaisResidencia.add(fatcaPaisResidencia);
    		
    		EntradaCrearFatcaPaisResidencia entradaCrearFacta = new EntradaCrearFatcaPaisResidencia();
    		entradaCrearFacta.p_IdEmpresa = 1L;
    		entradaCrearFacta.p_IdUsuario = 0L;
    		entradaCrearFacta.p_FatcaPaisResidencia = fatcasPaisResidencia;
    		
    		
    		SalidaCrearFatcaPaisResidencia salidaCrearFacta = demo.logica(conn, entradaCrearFacta);
					
			assertEquals("UI_PROCESO_EJECUTADO_EXITO", salidaCrearFacta.P_OUT_MENSAJES.get(0).codigo);
    		conn.rollback();
    	}
    }
    	
}