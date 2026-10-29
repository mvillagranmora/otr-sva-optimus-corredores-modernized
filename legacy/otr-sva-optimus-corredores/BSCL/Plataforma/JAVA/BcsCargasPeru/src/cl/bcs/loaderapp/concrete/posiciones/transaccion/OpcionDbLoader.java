package cl.bcs.loaderapp.concrete.posiciones.transaccion;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.dbcp.BasicDataSource;

import cl.bcs.loaderapp.App.EntradaCargaGeneral;
import cl.bcs.loaderapp.interfaces.DbLoader;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class OpcionDbLoader implements DbLoader {
	
	public String SQL_INSERT_TRANSACCION = loadResourceToString("cl/bcs/loaderapp/sql/posiciones/transaccion/Opciones_Ingresar.sql");
	
	public String SQL_CONSULTA_INSTRUMENTO = "SELECT I.\"IdInstrumento\", I.\"Nemotecnico\", I.\"IdTipoInstrumento\", TI.\"IdClasificacionTipoInstrumento\" \r\n"
			+ "FROM \"BSCL.Plataforma.BD::INS.Instrumento\" I\r\n"
			+ "INNER JOIN \"BSCL.Plataforma.BD::INS.TipoInstrumento\" TI ON  TI.\"IdTipoInstrumento\" = I.\"IdTipoInstrumento\"\r\n"
			+ "WHERE I.\"Nemotecnico\" = :Nemotecnico;";
	public static class Instrumento {
		public Long IdInstrumento;
		public String Nemotecnico;
		public String IdTipoInstrumento;
		public String IdClasificacionTipoInstrumento;
	}
	
	public String SQL_ConsultarIdEmpleadoUsuario = "SELECT \"IdEntidad\" FROM \"BSCL.Plataforma.BD::ACC.Usuario\" WHERE \"IdUsuario\" = :IdUsuario AND \"IdEmpresa\" = :IdEmpresa;";
	public static class EntidadEmpleadoUsuario{
		public Long IdEntidad;

		@Override
		public String toString() {
			return "EntidadEmpleadoUsuario [IdEntidad=" + IdEntidad + "]";
		}
	}
	
	public static class EntidadTransaccion{
		public String Folio;
		public String FechaSistemaCreacion;
		public String HoraSistemaCreacion;
		public String FechaLiquidacion;
		public String FechaVencimiento;
		public String Cantidad;
		public String Monto;
		public String Precio;
		public String Tasa;
		public String Nemotecnico;
		public String IdEstado;
		public String IdTipoOperacion;
		public String IdCondicionDeLiquidacion;
		public String IdMoneda;
		public String IdEmpresa;
		public String IdBolsa;
		public String EsVentaCorta;
		public Long Plazo;
		public String CodigoOperadorContraparte;
		public String IdLibroOrdenes;
		public String IdCorredorLocal;
		public String EsBilateral;
		public String TipoCambioCorresponsal;
		public String IdCorredorCorresponsal;
		public String MontoLiquidacionCorresponsal;
		public String IdMonedaLiquidacionCorresponsal;
		public String IdCorredorContraparte;
		public String CodigoOperadorCorredor;
		public String FormaNegociacion;
		
		public Long IdTransaccion;
		public String IdTipoInstrumento;
		public String IdClasificacionTipoInstrumento;
		public Long IdInstrumento;
		public String FechaIngresoRegistro;
		public String NemotecnicoSVS;
		public Long IdEmpleado;
		public String NemotecnicoSubyacente;
		public Long IdInstrumentoSubyacente;
		public String FechaIngresoOferta;
		public String FechaOperacion;
	}

	@Override
	public List<String> loadIntoDb(BasicDataSource jsJB, EntradaCargaGeneral in, List<Object> lines) {
		List<String> salida = new ArrayList<String>();

		Long idUsuarioDefecto = in.p_IdUsuario;
		Long idEmpresaDefecto = in.p_IdEmpresa;
		
		SQL sql = new SQL();
		try(Connection conn = jsJB.getConnection()){
			int linea = 0;
			List<CSVRecord> transaccionesCSV = (List<CSVRecord>) (Object) lines;
			for (CSVRecord record : transaccionesCSV) {
				try {
					List<Long> idsTransaccion= Utils.getSecuencia(conn, "BSCL.Orden.BD.Secuencias::Transaccion", (long) 2);
					
					EntidadTransaccion transaccion = new EntidadTransaccion();
					transaccion.IdTransaccion = idsTransaccion.get(0);
									
					transaccion.FechaSistemaCreacion = record.get("FechaSistemaCreacion");
					transaccion.HoraSistemaCreacion = record.get("HoraSistemaCreacion");
					transaccion.FechaLiquidacion = record.get("FechaLiquidacion");
					transaccion.FechaVencimiento = record.get("FechaVencimiento");
					transaccion.Cantidad = record.get("Cantidad");
					transaccion.Monto = record.get("Monto");
					transaccion.Precio = record.get("Precio");
					transaccion.Tasa = record.get("Tasa");
					
					transaccion.IdEstado = record.get("IdEstado");
					transaccion.IdTipoOperacion = record.get("IdTipoOperacion");
					transaccion.IdCondicionDeLiquidacion = record.get("IdCondicionDeLiquidacion");
					transaccion.IdMoneda = record.get("IdMoneda");
					transaccion.IdEmpresa = record.get("IdEmpresa");
					transaccion.IdBolsa = record.get("IdBolsa");
					transaccion.EsVentaCorta = record.get("EsVentaCorta");
					transaccion.Plazo = Long.parseLong(record.get("Plazo"));
					transaccion.CodigoOperadorContraparte = record.get("CodigoOperadorContraparte");
					transaccion.IdLibroOrdenes = record.get("IdLibroOrdenes");
					transaccion.IdCorredorLocal = record.get("IdCorredorLocal");
					transaccion.EsBilateral = record.get("EsBilateral");
					transaccion.TipoCambioCorresponsal = record.get("TipoCambioCorresponsal");
					transaccion.IdCorredorCorresponsal = record.get("IdCorredorCorresponsal");
					transaccion.MontoLiquidacionCorresponsal = record.get("MontoLiquidacionCorresponsal");
					transaccion.IdMonedaLiquidacionCorresponsal = record.get("IdMonedaLiquidacionCorresponsal");
					transaccion.IdCorredorContraparte = record.get("IdCorredorContraparte");
					transaccion.CodigoOperadorCorredor = record.get("CodigoOperadorCorredor");
					transaccion.FormaNegociacion = record.get("FormaNegociacion");
										
					HashMap<String, Object> params = new HashMap<>();
					if(!record.get("Nemotecnico").equals("")) {
						params.put("Nemotecnico", record.get("Nemotecnico"));
						List<Instrumento> instrumentos = sql.query(conn, SQL_CONSULTA_INSTRUMENTO, params, Instrumento.class);
						transaccion.IdClasificacionTipoInstrumento = instrumentos.get(0).IdClasificacionTipoInstrumento;
						transaccion.IdTipoInstrumento = instrumentos.get(0).IdTipoInstrumento;
						transaccion.IdInstrumento = instrumentos.get(0).IdInstrumento;
						transaccion.Nemotecnico = record.get("Nemotecnico");
					}
					
					//obtener id empleado
					params = new HashMap<>();
					params.put("IdUsuario", idUsuarioDefecto);
					params.put("IdEmpresa", idEmpresaDefecto);
					List<EntidadEmpleadoUsuario> empleadoUsuario = sql.query(conn, SQL_ConsultarIdEmpleadoUsuario, params, EntidadEmpleadoUsuario.class);
					if(empleadoUsuario!=null && empleadoUsuario.size()>0) {
						transaccion.IdEmpleado = empleadoUsuario.get(0).IdEntidad;
					}else {
						transaccion.IdEmpleado = new Long(0);	
					}
					
					transaccion.Folio = record.get("Folio");
					transaccion.FechaIngresoOferta = transaccion.FechaSistemaCreacion + " " + transaccion.HoraSistemaCreacion;
					transaccion.FechaIngresoRegistro = transaccion.FechaIngresoOferta;
					transaccion.FechaOperacion = transaccion.FechaSistemaCreacion;
					
					transaccion.NemotecnicoSVS = transaccion.Nemotecnico;
					transaccion.NemotecnicoSubyacente = transaccion.Nemotecnico;
					transaccion.IdInstrumentoSubyacente = transaccion.IdInstrumento;
					
					int[] res = sql.batch(conn, SQL_INSERT_TRANSACCION, Arrays.asList(transaccion), EntidadTransaccion.class);
					for (int i : res) {
						if(i==1){
							salida.add(linea + " - OPCION CON FOLIO: " + transaccion.Folio + ", INGRESADA CORRECTAMENTE");
						}else{
							salida.add(linea + " - OPCION CON FOLIO: " + transaccion.Folio + ", NO INGRESADA");
						}
					}
						
				} catch (Exception e) {
					e.printStackTrace();
					salida.add(linea + " - OPCION CON FOLIO: " + record.get("Folio") + ", NO INGRESADA");
				}
				linea++;
			}
		}catch (Exception e) {
			salida.add("ERROR AL OBTENER CONEXION");
		}
		return salida;
	}

}
