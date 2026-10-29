package cl.bcs.contabilidad;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import com.google.gson.Gson;

import cl.bcs.contabilidad.CentralizaMultiSeleccion.EntradaCentralizaMultiSeleccion;
import cl.bcs.contabilidad.CentralizaMultiSeleccion.SalidaCentralizaMultiSeleccion;
import cl.bcs.contabilidad.pojo.ResultadoCentralizacion;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Utils;

@TestInstance(Lifecycle.PER_CLASS)
public class CentralizaMultiSeleccionTest {

	private String entradaServicio = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"CON\",\"p_FechaContable\":\"2018-04-10\",\"p_Ejercicio\":\"2018\",\"p_SimulaOperacion\":\"N\",\"p_in_TiposCentralizacion\":[{\"IdTipoCentralizacion\":295},{\"IdTipoCentralizacion\":350}]}";
	private String salidaEsperadaServicio = "{\"P_OUT_ERRORESCONTADOR\":0,\"P_OUT_MENSAJES\":[{\"id\":295,\"tipo\":\"INFORMACION\",\"codigo\":\"CENTRALIZACIONES_GENERADA_CORRECTAMENTE\",\"descripcion\":\"Centralización 10 - KARDEX generada correctamente. Secuencia 1124\"},{\"id\":350,\"tipo\":\"INFORMACION\",\"codigo\":\"CENTRALIZACIONES_GENERADA_CORRECTAMENTE\",\"descripcion\":\"Centralización 13 - DEPÓSITOS Y AUTO DESEMBOLSOS generada correctamente. Secuencia 1125\"}],\"P_OUT_RESULTADO\":[{\"IdTipoCentralizacion\":295,\"IdMovimiento\":2589,\"IdTipoMovimiento\":\"PER\",\"IdEmpresa\":1,\"Numero\":89612,\"Linea\":0,\"Fecha\":\"2018-04-10\",\"DocumentoIdentidad\":\"00117138\",\"IdPortafolio\":205612,\"Ejercicio\":\"2018\",\"Periodo\":\"04-2018\",\"Monto\":1.00000000,\"IdMoneda\":\"PEN\",\"Estado\":\"VIG\"},{\"IdTipoCentralizacion\":295,\"IdMovimiento\":2590,\"IdTipoMovimiento\":\"PER\",\"IdEmpresa\":1,\"Numero\":89612,\"Linea\":0,\"Fecha\":\"2018-04-10\",\"DocumentoIdentidad\":\"00117138\",\"IdPortafolio\":205612,\"Ejercicio\":\"2018\",\"Periodo\":\"04-2018\",\"Monto\":2.00000000,\"IdMoneda\":\"PEN\",\"Estado\":\"VIG\"},{\"IdTipoCentralizacion\":295,\"IdMovimiento\":2591,\"IdTipoMovimiento\":\"PER\",\"IdEmpresa\":1,\"Numero\":89612,\"Linea\":0,\"Fecha\":\"2018-04-10\",\"DocumentoIdentidad\":\"00117138\",\"IdPortafolio\":205612,\"Ejercicio\":\"2018\",\"Periodo\":\"04-2018\",\"Monto\":3.00000000,\"IdMoneda\":\"PEN\",\"Estado\":\"VIG\"},{\"IdTipoCentralizacion\":295,\"IdMovimiento\":2515,\"IdTipoMovimiento\":\"PER\",\"IdEmpresa\":1,\"Numero\":89612,\"Linea\":0,\"Fecha\":\"2018-04-10\",\"DocumentoIdentidad\":\"00117138\",\"IdPortafolio\":205612,\"Ejercicio\":\"2018\",\"Periodo\":\"04-2018\",\"Monto\":4.00000000,\"IdMoneda\":\"PEN\",\"Estado\":\"VIG\"},{\"IdTipoCentralizacion\":295,\"IdMovimiento\":2516,\"IdTipoMovimiento\":\"PER\",\"IdEmpresa\":1,\"Numero\":89612,\"Linea\":0,\"Fecha\":\"2018-04-10\",\"DocumentoIdentidad\":\"00117138\",\"IdPortafolio\":205612,\"Ejercicio\":\"2018\",\"Periodo\":\"04-2018\",\"Monto\":5.00000000,\"IdMoneda\":\"PEN\",\"Estado\":\"VIG\"},{\"IdTipoCentralizacion\":295,\"IdMovimiento\":2517,\"IdTipoMovimiento\":\"PER\",\"IdEmpresa\":1,\"Numero\":89612,\"Linea\":0,\"Fecha\":\"2018-04-10\",\"DocumentoIdentidad\":\"00117138\",\"IdPortafolio\":205612,\"Ejercicio\":\"2018\",\"Periodo\":\"04-2018\",\"Monto\":6.00000000,\"IdMoneda\":\"PEN\",\"Estado\":\"VIG\"},{\"IdTipoCentralizacion\":295,\"IdMovimiento\":2540,\"IdTipoMovimiento\":\"PER\",\"IdEmpresa\":1,\"Numero\":13540,\"Linea\":0,\"Fecha\":\"2018-04-10\",\"DocumentoIdentidad\":null,\"IdPortafolio\":40768,\"Ejercicio\":\"2018\",\"Periodo\":\"04-2018\",\"Monto\":1267.60000000,\"IdMoneda\":\"PEN\",\"Estado\":\"VIG\"},{\"IdTipoCentralizacion\":350,\"IdMovimiento\":119776,\"IdTipoMovimiento\":\"TOMDE\",\"IdEmpresa\":1,\"Numero\":130565,\"Linea\":130565,\"Fecha\":\"2018-04-10\",\"DocumentoIdentidad\":null,\"IdPortafolio\":40768,\"Ejercicio\":\"2018\",\"Periodo\":\"04-2018\",\"Monto\":65433.00000000,\"IdMoneda\":\"PEN\",\"Estado\":\"VIG\"},{\"IdTipoCentralizacion\":350,\"IdMovimiento\":150345,\"IdTipoMovimiento\":\"AUTOD\",\"IdEmpresa\":1,\"Numero\":161132,\"Linea\":161132,\"Fecha\":\"2018-04-10\",\"DocumentoIdentidad\":null,\"IdPortafolio\":40768,\"Ejercicio\":\"2018\",\"Periodo\":\"04-2018\",\"Monto\":1006833.00000000,\"IdMoneda\":\"PEN\",\"Estado\":\"VIG\"},{\"IdTipoCentralizacion\":350,\"IdMovimiento\":150346,\"IdTipoMovimiento\":\"AUTOD\",\"IdEmpresa\":1,\"Numero\":161133,\"Linea\":161133,\"Fecha\":\"2018-04-10\",\"DocumentoIdentidad\":null,\"IdPortafolio\":40768,\"Ejercicio\":\"2018\",\"Periodo\":\"04-2018\",\"Monto\":807653.00000000,\"IdMoneda\":\"PEN\",\"Estado\":\"VIG\"},{\"IdTipoCentralizacion\":350,\"IdMovimiento\":150347,\"IdTipoMovimiento\":\"TOMDE\",\"IdEmpresa\":1,\"Numero\":161134,\"Linea\":161134,\"Fecha\":\"2018-04-10\",\"DocumentoIdentidad\":null,\"IdPortafolio\":40768,\"Ejercicio\":\"2018\",\"Periodo\":\"04-2018\",\"Monto\":682267.00000000,\"IdMoneda\":\"PEN\",\"Estado\":\"VIG\"},{\"IdTipoCentralizacion\":350,\"IdMovimiento\":150348,\"IdTipoMovimiento\":\"AUTOD\",\"IdEmpresa\":1,\"Numero\":161135,\"Linea\":161135,\"Fecha\":\"2018-04-10\",\"DocumentoIdentidad\":null,\"IdPortafolio\":40768,\"Ejercicio\":\"2018\",\"Periodo\":\"04-2018\",\"Monto\":408400.00000000,\"IdMoneda\":\"PEN\",\"Estado\":\"VIG\"},{\"IdTipoCentralizacion\":350,\"IdMovimiento\":150349,\"IdTipoMovimiento\":\"AUTOD\",\"IdEmpresa\":1,\"Numero\":161136,\"Linea\":161136,\"Fecha\":\"2018-04-10\",\"DocumentoIdentidad\":null,\"IdPortafolio\":40768,\"Ejercicio\":\"2018\",\"Periodo\":\"04-2018\",\"Monto\":514667.00000000,\"IdMoneda\":\"PEN\",\"Estado\":\"VIG\"},{\"IdTipoCentralizacion\":350,\"IdMovimiento\":150350,\"IdTipoMovimiento\":\"AUTOD\",\"IdEmpresa\":1,\"Numero\":161137,\"Linea\":161137,\"Fecha\":\"2018-04-10\",\"DocumentoIdentidad\":null,\"IdPortafolio\":40768,\"Ejercicio\":\"2018\",\"Periodo\":\"04-2018\",\"Monto\":787800.00000000,\"IdMoneda\":\"PEN\",\"Estado\":\"VIG\"},{\"IdTipoCentralizacion\":350,\"IdMovimiento\":150352,\"IdTipoMovimiento\":\"AUTOD\",\"IdEmpresa\":1,\"Numero\":161139,\"Linea\":161139,\"Fecha\":\"2018-04-10\",\"DocumentoIdentidad\":null,\"IdPortafolio\":40768,\"Ejercicio\":\"2018\",\"Periodo\":\"04-2018\",\"Monto\":342267.00000000,\"IdMoneda\":\"PEN\",\"Estado\":\"VIG\"},{\"IdTipoCentralizacion\":350,\"IdMovimiento\":150355,\"IdTipoMovimiento\":\"AUTOD\",\"IdEmpresa\":1,\"Numero\":161142,\"Linea\":161142,\"Fecha\":\"2018-04-10\",\"DocumentoIdentidad\":null,\"IdPortafolio\":40768,\"Ejercicio\":\"2018\",\"Periodo\":\"04-2018\",\"Monto\":894153.00000000,\"IdMoneda\":\"PEN\",\"Estado\":\"VIG\"},{\"IdTipoCentralizacion\":350,\"IdMovimiento\":150356,\"IdTipoMovimiento\":\"AUTOD\",\"IdEmpresa\":1,\"Numero\":161143,\"Linea\":161143,\"Fecha\":\"2018-04-10\",\"DocumentoIdentidad\":null,\"IdPortafolio\":40768,\"Ejercicio\":\"2018\",\"Periodo\":\"04-2018\",\"Monto\":300400.00000000,\"IdMoneda\":\"PEN\",\"Estado\":\"VIG\"},{\"IdTipoCentralizacion\":350,\"IdMovimiento\":150363,\"IdTipoMovimiento\":\"AUTOD\",\"IdEmpresa\":1,\"Numero\":161150,\"Linea\":161150,\"Fecha\":\"2018-04-10\",\"DocumentoIdentidad\":null,\"IdPortafolio\":40768,\"Ejercicio\":\"2018\",\"Periodo\":\"04-2018\",\"Monto\":493675.00000000,\"IdMoneda\":\"PEN\",\"Estado\":\"VIG\"},{\"IdTipoCentralizacion\":350,\"IdMovimiento\":150364,\"IdTipoMovimiento\":\"AUTOD\",\"IdEmpresa\":1,\"Numero\":161151,\"Linea\":161151,\"Fecha\":\"2018-04-10\",\"DocumentoIdentidad\":null,\"IdPortafolio\":40768,\"Ejercicio\":\"2018\",\"Periodo\":\"04-2018\",\"Monto\":453075.00000000,\"IdMoneda\":\"PEN\",\"Estado\":\"VIG\"}]}";
	@BeforeAll public void prepararAmbiente() {
		
	}
	/**
	 * Test basico, evalua una salida que en teoria esta correcta sea lo mismo que se encuentra retornando el servicio modificado... es como una prueba de impacto
	 * @throws Exception
	 */
	@Test public void centralizarMultiple() throws Exception {
		Gson gson = Utils.getGsonBuilder();
		SalidaCentralizaMultiSeleccion salidaEspedada = gson.fromJson(salidaEsperadaServicio, SalidaCentralizaMultiSeleccion.class);
		EntradaCentralizaMultiSeleccion entrada = gson.fromJson(entradaServicio, EntradaCentralizaMultiSeleccion.class);
		
		try(Connection conn = PlataformaUtils.getQAPEConnection()){
			CentralizaMultiSeleccion centralizar = new CentralizaMultiSeleccion();
			SalidaCentralizaMultiSeleccion salidaReal = centralizar.logica(conn, entrada);
			assertTrue(salidaEspedada.P_OUT_RESULTADO.size() == salidaReal.P_OUT_RESULTADO.size(), "CANTIDAD DE RESULTADOS DISTINTOS");
			
			Map<Long, Map<Long, List<ResultadoCentralizacion>>> grupoSalidaEsperada = salidaEspedada.P_OUT_RESULTADO.stream().collect(Collectors.groupingBy(res -> res.IdTipoCentralizacion, Collectors.groupingBy(res-> res.IdMovimiento)));
			Map<Long, Map<Long, List<ResultadoCentralizacion>>> grupoSalidaReal = salidaReal.P_OUT_RESULTADO.stream().collect(Collectors.groupingBy(res -> res.IdTipoCentralizacion, Collectors.groupingBy(res-> res.IdMovimiento)));
			
			grupoSalidaEsperada.keySet().forEach(key->{
				grupoSalidaEsperada.get(key).keySet().forEach(keyIdMovimiento -> {
					assertNotNull(grupoSalidaReal.get(key), "NO EXISTE ID TIPO CENTRALIZACION");
					assertNotNull(grupoSalidaReal.get(key).get(keyIdMovimiento), "NO EXISTE ID MOVIMIENTO");	
				});
			});
			
		}catch(SQLException e) {
			e.printStackTrace();
		}
		
		
	}
}
