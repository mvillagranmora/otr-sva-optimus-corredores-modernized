package cl.bcs.loaderapp.concrete.posiciones.cartera;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.dbcp.BasicDataSource;

import cl.bcs.custodia.ProcesaCargaMasivaCustodia;
import cl.bcs.custodia.ProcesaCargaMasivaCustodia.CuadraturaDiariaSalida;
import cl.bcs.custodia.ProcesaCargaMasivaCustodia.ProcesarCargaMasivaCustodiaEntrada;
import cl.bcs.custodia.ProcesaCargaMasivaCustodiaReporte;
import cl.bcs.custodia.ProcesaCargaMasivaCustodiaReporte.ProcesarCargaMasivaCustodiaReporteEntrada;
import cl.bcs.custodia.ProcesaCargaMasivaCustodiaReporte.ProcesarCargaMasivaCustodiaReporteSalida;
import cl.bcs.loaderapp.App.EntradaCargaGeneral;
import cl.bcs.loaderapp.interfaces.DbLoader;
import cl.bcs.loaderapp.pojo.posiciones.Posicion;
import cl.bcs.plataforma.pla.utils.SalidaServicios.AdvertenciaDetalle;
import cl.bcs.plataforma.pla.utils.SalidaServicios.ErrorDetalle;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CarteraDbLoader implements DbLoader{
	
	public static final String SQL_DROP_TABLA_CARGA_MASIVA = "DROP TABLE #CARGA_MASIVA;";
	public static final String SQL_DROP_TABLA_CARGA_MASIVA_DETALLE = "DROP TABLE #CARGA_MASIVA_DETALLE;";
	
	public final String SQL_CREAR_TABLA_CARGA_MASIVA = loadResourceToString("cl/bcs/loaderapp/sql/posiciones/cartera/Cartera_CrearTablaCargaMasiva.sql");
	public final String SQL_CREAR_TABLA_CARGA_MASIVA_DETALLE = loadResourceToString("cl/bcs/loaderapp/sql/posiciones/cartera/Cartera_CrearTablaCargaMasivaDetalle.sql");
	public final String SQL_INSERT_TABLA_CARGA_MASIVA = loadResourceToString("cl/bcs/loaderapp/sql/posiciones/cartera/Cartera_InsertTablaCargaMasiva.sql");
	public final String SQL_INSERT_TABLA_CARGA_MASIVA_DETALLE = loadResourceToString("cl/bcs/loaderapp/sql/posiciones/cartera/Cartera_InsertTablaCargaMasivaDetalle.sql");
	public final String SQL_CALL_CREAR_CARGA_MASIVA = loadResourceToString("cl/bcs/loaderapp/sql/posiciones/cartera/Cartera_CallCrearCargaMasiva.sql");
	
	public static class EntidadCargaMasiva{
		public Long IdInterno;
		public Long IdTipoCargaMasiva;
		public String NombreArchivo;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}

	}
	public static class SalidaEntidadCargaMasiva{
		public Long IdInterno;
		public Long SecuenciaCargaMasiva;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	public static class EntidadLog{
		public String Resultado;
	}
	
	protected Long identificarTipoCarga (String entidad) {
		if(entidad.equalsIgnoreCase("posicion")) {
			return new Long(1);
		}else if(entidad.equalsIgnoreCase("reporte")) {
			return new Long(9);
		}else {
			//garantia
			return new Long(5);
		}
	}
		
	@Override
	public List<String> loadIntoDb(BasicDataSource jsJB, EntradaCargaGeneral in, List<Object> lines) {
		List<String> salida = new ArrayList<>();
		List<Posicion> posicionesCompletas = (List<Posicion>) (Object) lines;
		
		try(Connection conn = jsJB.getConnection()){
			
			try {
				int cantidad = 1000;//cantidad de posiciones por grupo
				int cantidadGrupos = (posicionesCompletas.size() + cantidad - 1) / cantidad;
				List<List<Posicion>> posicionesAgrupadas = IntStream.range(0, cantidadGrupos)
						.mapToObj(i -> posicionesCompletas.subList(cantidad * i, Math.min(cantidad * i + cantidad, posicionesCompletas.size())) )
						.collect(Collectors.toList());
				
				for(List<Posicion> posiciones : posicionesAgrupadas) {

					SQL sql = new SQL();
					SQL.exec(conn, SQL_CREAR_TABLA_CARGA_MASIVA);
					SQL.exec(conn, SQL_CREAR_TABLA_CARGA_MASIVA_DETALLE);
					
					String fechaSistema = Utils.getFechaSistema(conn, Utils.getSchema(), in.p_IdEmpresa);
					
					EntidadCargaMasiva cargaMasiva = new EntidadCargaMasiva();
					cargaMasiva.IdInterno = (long) 1;
					cargaMasiva.IdTipoCargaMasiva = identificarTipoCarga(in.p_Entidad);	
					cargaMasiva.NombreArchivo = in.p_Entidad;
								
					sql.batch(conn, SQL_INSERT_TABLA_CARGA_MASIVA, Arrays.asList(cargaMasiva), EntidadCargaMasiva.class);
					sql.batch(conn, SQL_INSERT_TABLA_CARGA_MASIVA_DETALLE, posiciones, Posicion.class);
					
					System.out.println("CARGA MASIVA INGRESADA A LA TABLA");
					
					HashMap<String, Object> paramsIn = new HashMap<>();
					paramsIn.put("p_IdEmpresa", in.p_IdEmpresa);
					paramsIn.put("p_IdUsuario", in.p_IdUsuario);
					paramsIn.put("p_IdModulo", in.p_IdModulo);
					paramsIn.put("p_UsaReglasNegocio", "N");
					
					paramsIn.put("p_FechaCarga", fechaSistema);
					HashMap<String, Class> paramsOut = new HashMap<>();
					paramsOut.put("p_out_SecuenciaCargaMasiva", SalidaEntidadCargaMasiva.class);
					paramsOut.put("p_out_AdvertenciasContador", Long.class);
					paramsOut.put("p_out_AdvertenciasDetalle", AdvertenciaDetalle.class);
					paramsOut.put("p_out_ErroresContador", Long.class);
					paramsOut.put("p_out_ErroresDetalle", ErrorDetalle.class);
					
					HashMap<String,Object> resultSP = sql.call(conn, SQL_CALL_CREAR_CARGA_MASIVA, paramsIn, paramsOut);
					
					Long errores = (Long) resultSP.get("p_out_ErroresContador");
					List<SalidaEntidadCargaMasiva> secuencias = (List<SalidaEntidadCargaMasiva>) resultSP.get("p_out_SecuenciaCargaMasiva");
					
					if(errores.equals( new Long(0) ) ){
						if(cargaMasiva.IdTipoCargaMasiva.equals(new Long(9))) {
							//se ingreso correctamente el archivo, ahora realizar el proceso (reportes).
							ProcesaCargaMasivaCustodiaReporte procesar = new ProcesaCargaMasivaCustodiaReporte();
							ProcesarCargaMasivaCustodiaReporteEntrada entradaProcesarCustodia = new ProcesarCargaMasivaCustodiaReporteEntrada();
							entradaProcesarCustodia.p_IdEmpresa = in.p_IdEmpresa;
							entradaProcesarCustodia.p_IdUsuario = in.p_IdUsuario;
							entradaProcesarCustodia.p_IdModulo = in.p_IdModulo;
							entradaProcesarCustodia.p_UsaReglasNegocio = "S";
							entradaProcesarCustodia.p_UsaCarterasSistema=  "S";
							entradaProcesarCustodia.p_FechaProceso = fechaSistema;
							entradaProcesarCustodia.p_CargaMasivaProcesar = secuencias.get(0).SecuenciaCargaMasiva;
							
							ProcesarCargaMasivaCustodiaReporteSalida salidaProcesar = procesar.logica(conn, entradaProcesarCustodia);
							
							//actualizar los reportes con el correlativoasociado en el idOperacion
							
							System.out.println(salidaProcesar);
							
						}else {
							//se ingreso correctamente el archivo, ahora realizar el proceso (titulo, garantia).
							ProcesaCargaMasivaCustodia procesar = new ProcesaCargaMasivaCustodia();
							ProcesarCargaMasivaCustodiaEntrada entradaProcesarCustodia = new ProcesarCargaMasivaCustodiaEntrada();
							entradaProcesarCustodia.p_IdEmpresa = in.p_IdEmpresa;
							entradaProcesarCustodia.p_IdUsuario = in.p_IdUsuario;
							entradaProcesarCustodia.p_IdModulo = in.p_IdModulo;
							entradaProcesarCustodia.p_UsaReglasNegocio = "N";
							entradaProcesarCustodia.p_UsaCarterasSistema=  "S";
							entradaProcesarCustodia.p_FechaProceso = fechaSistema;
							entradaProcesarCustodia.p_CargaMasivaProcesar = secuencias.get(0).SecuenciaCargaMasiva;
							
							CuadraturaDiariaSalida salidaProcesar = procesar.logica(conn, entradaProcesarCustodia);
							System.out.println(salidaProcesar);
						}
						HashMap<String, Object> params = new HashMap<>();
						params.put("IdCargaMasiva", secuencias.get(0).SecuenciaCargaMasiva);
						List<EntidadLog> logs = sql.query(conn, "SELECT (\"Glosa\" || ' ' || \"ObservacionValidacion\") AS \"Resultado\" FROM \"BSCL.Custodia.BD::CUS.CargaMasivaDetalle\" WHERE \"IdCargaMasiva\" = :IdCargaMasiva", params, EntidadLog.class);
						logs.stream().forEach(res -> salida.add(salida.size() + " - " + res.Resultado));
					}
					
					SQL.exec(conn, SQL_DROP_TABLA_CARGA_MASIVA_DETALLE);
					SQL.exec(conn, SQL_DROP_TABLA_CARGA_MASIVA);
					
					conn.commit();//TODO COMMIT
				}
			}catch(SQLException ex) {
				salida.add("CARGA MASIVA " + in.p_Entidad + " ERROR: " + ex.getMessage());
				ex.printStackTrace();
			}
			
			conn.rollback();						
		}catch(Exception e) {
			e.printStackTrace();
		}

		return salida;
	}
	
}
