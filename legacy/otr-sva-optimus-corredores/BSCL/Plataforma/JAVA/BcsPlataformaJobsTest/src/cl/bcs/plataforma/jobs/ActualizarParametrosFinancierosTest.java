package cl.bcs.plataforma.jobs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import cl.bcs.plataforma.jobs.ActualizarParametrosFinancieros.ParametroFinanciero;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.CallRestService;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

class ActualizarParametrosFinancierosTest {
	
	public final String SQL_SELECT_PARAMETROS_FINANCIEROS = "SELECT \"IdParametroFinanciero\", \"Fecha\", \"AgnoMes\", \"Agno\", \"Valor\" FROM \"BSCL.Plataforma.BD::MER.ParametroFinanciero\" WHERE \"Fecha\" = :fechaSistema;";
	
	private static Connection conn;
	
	private static String fechaSistema;
	private static String servidor;
	private static String url;
	private static String user;
	private static String passwd;
	private static boolean auth;
	private static boolean debug;
	
	private static String salidaServicio;
	
	@BeforeAll
	static void prepararAmbiente() {
		
		System.out.println("BEFORE ALL");
		try {
			conn = PlataformaUtils.getDESAConnection();
			//obtener configuraciones
			HashMap<String, HashMap<String, String>> configuraciones = Utils.getConfiguraciones(conn, Utils.getSchema(), 1);

			fechaSistema = configuraciones.get("PLATAFORMA").get("PLA.FechaSistema");
			servidor = configuraciones.get("PLATAFORMA").get("MER.ServidorTec");
			url = configuraciones.get("PLATAFORMA").get("MER.UrlServicioParidades");
			user = configuraciones.get("PLATAFORMA").get("MER.UsuarioServidorTec");
			passwd = configuraciones.get("PLATAFORMA").get("MER.ClaveServidorTec");
			auth = true;
			debug = true;
		} catch (SQLException e) {
			fail("No se puede obtener la conexion a la base de datos");
		}
		
	}
	
	@AfterAll
	static void terminarAmbiente() {
		try {
			conn.rollback();
			conn.close();
			System.out.println("AFTER ALL OK");
		}catch(Exception e) {
			fail("Error al cerrar la conexion: " + e.getMessage());
		}
	}
	

	@Test
	void probarConexionWebService() {
		try {

			CallRestService call = new CallRestService(servidor, url, user, passwd, auth, debug);
			salidaServicio = call.callRestPost("[{\"id\":0,\"fechaini\":\""+fechaSistema+"\",\"fechafin\":\""+fechaSistema+"\",\"periodo\":\"DI\",\"codigo\":\"?\"}]");
			assertTrue( (salidaServicio!=null && !salidaServicio.equals("")) );
		} catch (Exception e) {
			e.printStackTrace();
			fail("Error al obtener la conexion con el servicio web: " + e.getMessage());
			
		}			
	}
	
	@Test
	void ejecutarServicio() {
		ActualizarParametrosFinancieros job = new ActualizarParametrosFinancieros();
		try {
			assertTrue(job.go(conn, true));
		} catch (Throwable e) {
			fail("Error grave al ejecutar ActualizarParametrosFinancieros: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	@Test
	void evaluarRegistrosEnBaseDeDatos(){
		SQL sql = new SQL();
		ActualizarParametrosFinancieros job = new ActualizarParametrosFinancieros();
		WrapServicio[]  parsed = Utils.getGsonBuilder().fromJson(salidaServicio, WrapServicio[].class);
		
		if(parsed!=null && parsed.length>0) {
			WrapServicio parametroServicio = parsed[0];
			WrapServicio.Cabecera cabecera = parametroServicio.cabecera;
			
			HashMap<String, Object> params = new HashMap<>();
			params.put("fechaSistema", fechaSistema);
			try {
				job.go(conn, true);
				HashMap<String, Object> definicionParametrosFinancieros = Utils.List2Hash( job.obtenerListaParametrosFinancieros(conn, sql), "IdParametroFinanciero");
				
				List<ParametroFinanciero> parametros = sql.query(conn, SQL_SELECT_PARAMETROS_FINANCIEROS, params, ParametroFinanciero.class);
				System.out.println(parametros);
				HashMap<String, Object> parametrosBD = Utils.List2Hash(parametros, "IdParametroFinanciero");
				
				cabecera.consultatbiepfResult.forEach(resultServicio ->{
					if(definicionParametrosFinancieros.containsKey(resultServicio.codigo)) {
						ParametroFinanciero parametroBD = (ParametroFinanciero) parametrosBD.get(resultServicio.codigo);
						assertEquals(resultServicio.codigo, parametroBD.IdParametroFinanciero);
						assertEquals(resultServicio.valor.doubleValue(), parametroBD.Valor.doubleValue(),0.1);	
					}
				});

			} catch (SQLException e) {
				e.printStackTrace();
				fail("Error al obtener los parametros financieros: " + e.getMessage());
			} catch (Exception e) {
				e.printStackTrace();
				fail("Parametros financieros repetidos: " + e.getMessage());
			} catch (Throwable e) {
				e.printStackTrace();
				fail("Error al obtener definicion de los parametros financieros: " + e.getMessage());
			}
		}
	
	}
	
	public class RespuestaServicio{
		List<WrapServicio> resultados;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public class WrapServicio{
		public Long id;
		public Cabecera cabecera;
		
		public class Cabecera{
			List<ConsultatbiepfResult> consultatbiepfResult;
			@Override
			public String toString() {
				return Utils.printJson(this, false);
			}
		}
		public class ConsultatbiepfResult {
			public String codigo;
			public String fecha;
			public BigDecimal valor;
			@Override
			public String toString() {
				return Utils.printJson(this, false);
			}
		}
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

}
