package cl.bcs.plataforma.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import org.junit.jupiter.api.Test;


import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.per.CrearModificarPaisResidenciaFiscal;
import cl.bcs.socionegocio.per.CrearModificarPaisResidenciaFiscal.ObjPaisResidenciaFiscal;
import cl.bcs.socionegocio.per.CrearModificarPaisResidenciaFiscal.ObjPaisResidenciaFiscalIN;
import cl.bcs.socionegocio.per.CrearModificarPaisResidenciaFiscal.ObjPaisResidenciaFiscalOUT;
import cl.bcs.utils.SQL;


class TestCrearModificarPaisResidenciaFiscal {	
	
	private static final SimpleDateFormat FORMATO_FECHA = new SimpleDateFormat("yyyy-MM-dd");	
	String sql = "SELECT top 1 \"IdPersona\" FROM \"BSCL.SocioNegocio.BD::PER.DocumentoIdentidad\" WHERE \"IdPersona\" NOT IN (SELECT \"IdPersona\" FROM \"BSCL.SocioNegocio.BD::PER.FatcaPaisResidencia\")";
	
	public static class SelectIdPersona{
		public Long IdPersona;
	} 
	
	@Test
    void testCrearFatca() throws Exception {
    	try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
    		
    		SQL _sql = new SQL();
    		List<SelectIdPersona> c = _sql.query(conn, sql, new HashMap<>(), SelectIdPersona.class);
    		
    		CrearModificarPaisResidenciaFiscal demo = new CrearModificarPaisResidenciaFiscal(); 
    		ArrayList<ObjPaisResidenciaFiscal> objsPaisResidencia = new ArrayList<ObjPaisResidenciaFiscal>();
    		ObjPaisResidenciaFiscal objPaisResidencia = new ObjPaisResidenciaFiscal();
    		objPaisResidencia.IdPaisResidenciaFiscal = 44L;
    		objPaisResidencia.IdPersona = c.get(0).IdPersona;
    		objPaisResidencia.IdPais = 44L;
    		objPaisResidencia.FechaInicioTributacionPais = new java.sql.Date(FORMATO_FECHA.parse("0001-01-01 00:00:00.000").getTime());;
    		objPaisResidencia.IdEstado = "ACT";
    		objPaisResidencia.IdEmpresa = 0L;
    		objPaisResidencia.IdUsuarioCreador = 0L;
    		objPaisResidencia.IdUsuarioModificador = 0L ;
    		objsPaisResidencia.add(objPaisResidencia);
    		
    		ObjPaisResidenciaFiscalIN entradaCrearFacta = new ObjPaisResidenciaFiscalIN();
    		entradaCrearFacta.idEmpresa = 1L;
    		entradaCrearFacta.accion = "INSERTAR";
    		entradaCrearFacta.paisResidenciaFiscal = objsPaisResidencia;
    		
    		
    		ObjPaisResidenciaFiscalOUT salidaCrearFacta = demo.logica(conn, entradaCrearFacta);
					
			assertEquals("UI_PAIS_RESIDENCIA_FISCAL_CREADAS", salidaCrearFacta.P_OUT_MENSAJES.get(0).codigo);
    		conn.rollback();
    	}
    }
    	
}