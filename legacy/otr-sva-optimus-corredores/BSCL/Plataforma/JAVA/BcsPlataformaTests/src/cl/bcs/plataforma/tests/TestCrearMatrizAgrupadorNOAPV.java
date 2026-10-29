package cl.bcs.plataforma.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;


import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.portafolio.CrearMatrizAgrupadorNOAPV;
import cl.bcs.portafolio.CrearMatrizAgrupadorNOAPV.EntradaCrearMatrizAgrupadorNOAPV;
import cl.bcs.portafolio.CrearMatrizAgrupadorNOAPV.SalidaCrearMatrizAgrupadorNOAPV;
import cl.bcs.portafolio.pojo.MatrizAgrupadorNOAPV;
import cl.bcs.utils.SQL;



class TestCrearMatrizAgrupadorNOAPV {
	
	String sql = "SELECT \"CodigoPortafolio\" FROM \"BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV\";";
	
	public static class SelectCodigoPortafolio{
		public String CodigoPortafolio;
	} 
	
	@Test
    void testCrearMatriz() throws Exception {
    	try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
    		CrearMatrizAgrupadorNOAPV demo = new CrearMatrizAgrupadorNOAPV();   		
    		
    		String codigoPortafolioUnico = CodigoPortafolioNuevo(conn);
    		
    		List<MatrizAgrupadorNOAPV> matrices = new ArrayList<MatrizAgrupadorNOAPV>();
    		MatrizAgrupadorNOAPV matriz = new MatrizAgrupadorNOAPV();
    		matriz.IdMatrizAgrupadorNOAPV = 100L;
    		matriz.CodigoPortafolio = codigoPortafolioUnico;
    		matriz.EsADC = "S";
    		matriz.IdEstado = "VIG";
    		matrices.add(matriz);
    		
    		EntradaCrearMatrizAgrupadorNOAPV entradaCrearMatriz = new EntradaCrearMatrizAgrupadorNOAPV();  		
    		entradaCrearMatriz.p_IdEmpresa = 1L;
    		entradaCrearMatriz.p_IdUsuario = 0L;
    		entradaCrearMatriz.p_IdModulo = "POR";
    		entradaCrearMatriz.p_SimularOperacion = "N";
    		entradaCrearMatriz.p_UsaReglasNegocio = "S";
    		entradaCrearMatriz.p_MatrizAgrupadorNOAPV = matrices;
    		
    		SalidaCrearMatrizAgrupadorNOAPV salida = demo.logica(conn, entradaCrearMatriz);
					
			assertEquals("MATRIZ_CREADA_EXISTOSA", salida.P_OUT_MENSAJES.get(0).codigo);
			
    		conn.rollback();
    	}
    }
    
	public String CodigoPortafolioNuevo(Connection conn) throws Exception {
		Random random = new Random();
		int primerNumero = random.nextInt(201);
		SQL _sql = new SQL();
		List<SelectCodigoPortafolio> c = _sql.query(conn, sql, new HashMap<>(), SelectCodigoPortafolio.class);
		
		if(c.contains(primerNumero+"")) {
			return CodigoPortafolioNuevo(conn);
		}else {
			return primerNumero + "";
		}
		
		
	}
	
}