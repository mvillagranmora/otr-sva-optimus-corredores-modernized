package cl.bcs.plataforma.tests;

import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import org.junit.jupiter.api.Test;


import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.per.CrearDocumentoIdentidad;
import cl.bcs.socionegocio.per.CrearDocumentoIdentidad.SalidaCrearDocumentoIdentidad;
import cl.bcs.utils.SQL;
import cl.bcs.socionegocio.per.CrearDocumentoIdentidad.EntradaCrearDocumentoIdentidad;


class TestCrearDocumentoIdentidad {	
	
String sql = "SELECT top 1 \"IdPersona\" FROM \"BSCL.SocioNegocio.BD::PER.DocumentoIdentidad\" WHERE \"IdPersona\" NOT IN (SELECT \"IdPersona\" FROM \"BSCL.SocioNegocio.BD::PER.DocumentoIdentidad\" WHERE \"IdTipoDocumentoIdentidad\" LIKE 'P')";
	
	public static class SelectIdPersona{
		public Long IdPersona;
	} 
	
	@Test
    void testCrearDocumentoIdentidad() throws Exception {
    	try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
    		
    		SQL _sql = new SQL();
    		List<SelectIdPersona> c = _sql.query(conn, sql, new HashMap<>(), SelectIdPersona.class);
    		
    		CrearDocumentoIdentidad demoDocIdentidad = new CrearDocumentoIdentidad(); 		   		
    		
    		EntradaCrearDocumentoIdentidad entradaCrearDocumentoIdentidad = new EntradaCrearDocumentoIdentidad();
    		entradaCrearDocumentoIdentidad.p_IdEmpresa = 1L;
    		entradaCrearDocumentoIdentidad.p_IdUsuario = 0L;
    		entradaCrearDocumentoIdentidad.p_IdModulo = "PER";
    		entradaCrearDocumentoIdentidad.p_UsaReglasNegocio = "S";
    		entradaCrearDocumentoIdentidad.p_IdPersona = c.get(0).IdPersona;
    		entradaCrearDocumentoIdentidad.p_IdTipoDocumentoIdentidad = "P";
    		entradaCrearDocumentoIdentidad.p_DocumentoIdentidad = "8984817-2";
    		entradaCrearDocumentoIdentidad.p_NumeroSerie = "";
    		entradaCrearDocumentoIdentidad.p_FechaEmision = "0001-01-01";
    		entradaCrearDocumentoIdentidad.p_FechaVencimiento = "0001-01-01";
    		entradaCrearDocumentoIdentidad.p_IdTipoDocumentoIdentidadAsociado = "";
    		entradaCrearDocumentoIdentidad.p_TipoPersona = "N";
    		
    		SalidaCrearDocumentoIdentidad salidaDocIdentidad = demoDocIdentidad.logica(conn, entradaCrearDocumentoIdentidad);
					
			assertTrue(salidaDocIdentidad.p_out_RefId > 0);
    		conn.rollback();
    	}
    }
    	
}