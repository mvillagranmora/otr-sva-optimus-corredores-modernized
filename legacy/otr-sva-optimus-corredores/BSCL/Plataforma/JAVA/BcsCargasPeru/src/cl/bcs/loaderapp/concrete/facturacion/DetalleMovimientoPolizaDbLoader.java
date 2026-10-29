package cl.bcs.loaderapp.concrete.facturacion;

import java.sql.Connection;
import java.sql.SQLException;
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

public class DetalleMovimientoPolizaDbLoader implements DbLoader {
	
	public String SQL_INGRESAR_DETALLE_MOVIMIENTO_POLIZA = loadResourceToString("cl/bcs/loaderapp/sql/facturacion/DetalleMovimientoPoliza_Ingresar.sql");
	
	public String SQL_CONSULTA_INSTRUMENTO = "SELECT I.\"IdInstrumento\", I.\"Nemotecnico\", I.\"IdTipoInstrumento\", I.\"IdClasificacionTipoInstrumento\", I.\"IdMoneda\", TI.\"IdTipoProducto\", TI.\"Codigo\" AS \"CodigoTipoProducto\" ,I.\"IdTipoOperacion\" AS \"CodigoTipoOperacion\", I.\"NombreTipoInstrumento\", I.\"NombreClasificacionTipoInstrumento\", I.\"IdEmisor\" \r\n"
			+ "FROM \"BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento\" I \r\n"
			+ "LEFT JOIN BSCL.\"BSCL.Plataforma.BD::INS.TipoOperacion\" TI ON I.\"IdTipoOperacion\" = TI.\"IdTipoOperacion\" \r\n"
			+ "WHERE I.\"Nemotecnico\" = :Nemotecnico;";
	public static class Instrumento {
		public Long IdInstrumento;
		public String Nemotecnico;
		public String IdTipoInstrumento;
		public String IdClasificacionTipoInstrumento;
		public String IdMoneda;
		public String IdTipoProducto;
		public String CodigoTipoProducto;
		public String CodigoTipoOperacion;
		public String NombreTipoInstrumento;
		public String NombreClasificacionTipoInstrumento;
		public String IdEmisor;
	}
	
	public String SQL_CONSULTA_AGENTE = "SELECT \"IdSucursal\", \"NombreSucursal\", \"NombreCompleto\", \"IdEmpleadoEjecutivo\" FROM \"BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo\" WHERE \"CodigoEmpleado\" = :CodigoAgente;";
	public static class Agente{
		public Long IdSucursal;
		public Long IdEmpleadoEjecutivo;
		public String NombreSucursal;
		public String NombreCompleto;
	}
	
	public static class EntidadDetalleMovimientoPoliza{
		public String IdComprobanteDetalle;
		public String IdMovimientoComprobante;
		public String IdTransaccion;
		public String NemotecnicoInstrumento;
		public String IdConceptoMovimiento;
		public String Cantidad;
		public String Precio;
		public String PrecioPromedio;
		public String Monto;
		public String Tasa;
		public String TasaEmision;
		public String ValorRescate;
		public String ValorParidad;
		public String ValorCaptacionMonedaOrigen;
		public String SecuenciaAgrupacion;
		public String IdEstadoMovimientoComprobanteDetalle;
		public String FechaIngresoRegistro;
		public String MontoMovimientoComprobanteDetalle;
		public String MontoLiquidacion;
		public String IdMoneda;
		public String IdMonedaMovimientoComprobanteDetalle;
		public String IdMonedaLiquidacion;
		public String NemotecnicoSubyacente;
		public String NemotecnicoRelacionado;
		public String CodigoAgenteEjecutivoPortafolio;
		public String ModPrefFact;
		public String CodigoIFRS;
		public String EsEnRueda;
		public String IdMesa;
		public String EsVentaCorta;
		public String IndicadorOrdenDirecta;
		public String NemotecnicoNuevo;
		
		//internos
		public Long IdMovimientoComprobanteDetalle;
		public Long IdInstrumento;
		public String IdTipoInstrumentoSubyacente;
		public String NombreClasificacionTipoInstrumentoRelacionado;
		public String IdClasificacionTipoInstrumentoRelacionado;
		public String NombreTipoInstrumentoRelacionado;
		public Long IdInstrumentoSubyacente;
		public String IdTipoInstrumento;
		public String NombreTipoInstrumento;
		public String IdTipoInstrumentoRelacionado;
		public String NombreClasificacionTipoInstrumento;
		public String IdClasificacionTipoInstrumento;
		public Long IdInstrumentoRelacionado;
		public String IdMonedaSubyacente;
		public String NombreTipoInstrumentoSubyacente;
		public Long IdSucursalEmpleadoEjecutivoPortafolio;
		public Long IdEmpleadoEjecutivoPortafolio;
		public Long IdInstrumentoNuevo;
	}

	@Override
	public List<String> loadIntoDb(BasicDataSource jsJB, EntradaCargaGeneral in, List<Object> lines) {

		List<String> salida = new ArrayList<String>();

		Long idUsuarioDefecto = in.p_IdUsuario;
		Long idEmpresaDefecto = in.p_IdEmpresa;
		
		SQL sql = new SQL();

		try(Connection conn = jsJB.getConnection()){
			int linea = 0;
			List<CSVRecord> polizasCSV = (List<CSVRecord>) (Object) lines;
			List<Long> idsMovimientoDetallePolizas = Utils.getSecuencia(conn, "BSCL.Facturacion.BD.Secuencias::MovimientoComprobanteDetalle", (long) lines.size());
			for (CSVRecord record : polizasCSV) {
				try {
					EntidadDetalleMovimientoPoliza detalleMovimientoPoliza = new EntidadDetalleMovimientoPoliza();
					detalleMovimientoPoliza.IdComprobanteDetalle = record.get("IdComprobanteDetalle");
					detalleMovimientoPoliza.IdMovimientoComprobante = record.get("IdMovimientoComprobante");
					detalleMovimientoPoliza.IdTransaccion = record.get("IdTransaccion");
					detalleMovimientoPoliza.NemotecnicoInstrumento = record.get("NemotecnicoInstrumento");
					detalleMovimientoPoliza.IdConceptoMovimiento = record.get("IdConceptoMovimiento");
					detalleMovimientoPoliza.Cantidad = record.get("Cantidad");
					detalleMovimientoPoliza.Precio = record.get("Precio");
					detalleMovimientoPoliza.PrecioPromedio = record.get("PrecioPromedio");
					detalleMovimientoPoliza.Monto = record.get("Monto");
					detalleMovimientoPoliza.Tasa = record.get("Tasa");
					detalleMovimientoPoliza.TasaEmision = record.get("TasaEmision");
					detalleMovimientoPoliza.ValorRescate = record.get("ValorRescate");
					detalleMovimientoPoliza.ValorParidad = record.get("ValorParidad");
					detalleMovimientoPoliza.ValorCaptacionMonedaOrigen = record.get("ValorCaptacionMonedaOrigen");
					detalleMovimientoPoliza.SecuenciaAgrupacion = record.get("SecuenciaAgrupacion");
					detalleMovimientoPoliza.IdEstadoMovimientoComprobanteDetalle = record.get("IdEstadoMovimientoComprobanteDetalle");
					detalleMovimientoPoliza.FechaIngresoRegistro = record.get("FechaIngresoRegistro");
					detalleMovimientoPoliza.MontoMovimientoComprobanteDetalle = record.get("MontoMovimientoComprobanteDetalle");
					detalleMovimientoPoliza.MontoLiquidacion = record.get("MontoLiquidacion");
					detalleMovimientoPoliza.IdMoneda = record.get("IdMoneda");
					detalleMovimientoPoliza.IdMonedaMovimientoComprobanteDetalle = record.get("IdMonedaMovimientoComprobanteDetalle");
					detalleMovimientoPoliza.IdMonedaLiquidacion = record.get("IdMonedaLiquidacion");
					detalleMovimientoPoliza.NemotecnicoSubyacente = record.get("NemotecnicoSubyacente");
					detalleMovimientoPoliza.NemotecnicoRelacionado = record.get("NemotecnicoRelacionado");
					detalleMovimientoPoliza.CodigoAgenteEjecutivoPortafolio = record.get("CodigoAgenteEjecutivoPortafolio");
					detalleMovimientoPoliza.ModPrefFact = record.get("ModPrefFact");
					detalleMovimientoPoliza.CodigoIFRS = record.get("CodigoIFRS");
					detalleMovimientoPoliza.EsEnRueda = record.get("EsEnRueda");
					detalleMovimientoPoliza.IdMesa = record.get("IdMesa");
					detalleMovimientoPoliza.EsVentaCorta = record.get("EsVentaCorta");
					detalleMovimientoPoliza.IndicadorOrdenDirecta = record.get("IndicadorOrdenDirecta");
					detalleMovimientoPoliza.NemotecnicoNuevo = record.get("NemotecnicoNuevo");
					
					//obtener datos internos.
					detalleMovimientoPoliza.IdMovimientoComprobanteDetalle = idsMovimientoDetallePolizas.get(0);
					
					//obtener IdInstrumento
					HashMap<String, Object> params = new HashMap<>();
					if(!record.get("NemotecnicoInstrumento").equals("")) {
						params.put("Nemotecnico", record.get("NemotecnicoInstrumento"));
						List<Instrumento> instrumentos = sql.query(conn, SQL_CONSULTA_INSTRUMENTO, params, Instrumento.class);
						detalleMovimientoPoliza.IdClasificacionTipoInstrumento = instrumentos.get(0).IdClasificacionTipoInstrumento;//obtener con el nemo
						detalleMovimientoPoliza.IdTipoInstrumento = instrumentos.get(0).IdTipoInstrumento;//obtener con el nemo
						detalleMovimientoPoliza.IdInstrumento = instrumentos.get(0).IdInstrumento;//obtener con el nemo
						detalleMovimientoPoliza.NombreTipoInstrumento = instrumentos.get(0).NombreTipoInstrumento;
						detalleMovimientoPoliza.NombreClasificacionTipoInstrumento = instrumentos.get(0).NombreClasificacionTipoInstrumento;
					}
					
					//obtener IdInstrumentoSubyacente
					params = new HashMap<>();
					if(!record.get("NemotecnicoSubyacente").equals("")) {
						params.put("Nemotecnico", record.get("NemotecnicoSubyacente"));
						List<Instrumento> instrumentos = sql.query(conn, SQL_CONSULTA_INSTRUMENTO, params, Instrumento.class);
						detalleMovimientoPoliza.IdTipoInstrumentoSubyacente = instrumentos.get(0).IdTipoInstrumento;//obtener con el nemo
						detalleMovimientoPoliza.IdInstrumentoSubyacente = instrumentos.get(0).IdInstrumento;//obtener con el nemo
						detalleMovimientoPoliza.IdMonedaSubyacente = instrumentos.get(0).IdMoneda;//obtener con el nemo
						detalleMovimientoPoliza.NombreTipoInstrumentoSubyacente = instrumentos.get(0).NombreTipoInstrumento;
					}
					
					//obtener IdInstrumentoRelacionado
					params = new HashMap<>();
					if(!record.get("NemotecnicoRelacionado").equals("")) {
						params.put("Nemotecnico", record.get("NemotecnicoRelacionado"));
						List<Instrumento> instrumentos = sql.query(conn, SQL_CONSULTA_INSTRUMENTO, params, Instrumento.class);
						detalleMovimientoPoliza.IdClasificacionTipoInstrumentoRelacionado = instrumentos.get(0).IdClasificacionTipoInstrumento;//obtener con el nemo
						detalleMovimientoPoliza.IdTipoInstrumentoRelacionado = instrumentos.get(0).IdTipoInstrumento;//obtener con el nemo
						detalleMovimientoPoliza.IdInstrumentoRelacionado = instrumentos.get(0).IdInstrumento;//obtener con el nemo
						detalleMovimientoPoliza.NombreTipoInstrumentoRelacionado = instrumentos.get(0).NombreTipoInstrumento;
						detalleMovimientoPoliza.NombreClasificacionTipoInstrumentoRelacionado = instrumentos.get(0).NombreClasificacionTipoInstrumento;
					}
					
					//obtener IdInstrumentoNuevo
					params = new HashMap<>();
					if(!record.get("NemotecnicoNuevo").equals("")) {
						params.put("Nemotecnico", record.get("NemotecnicoNuevo"));
						List<Instrumento> instrumentos = sql.query(conn, SQL_CONSULTA_INSTRUMENTO, params, Instrumento.class);
						detalleMovimientoPoliza.IdInstrumentoNuevo = instrumentos.get(0).IdInstrumento;//obtener con el nemo
					}
					
					//obtener IdEmpleadoEjecutivo
					params = new HashMap<>();
					if(!record.get("CodigoAgenteEjecutivoPortafolio").equals("")) {
						params.put("CodigoAgente", record.get("CodigoAgenteEjecutivoPortafolio"));
						List<Agente> agentes = sql.query(conn, SQL_CONSULTA_AGENTE, params, Agente.class);
						detalleMovimientoPoliza.IdSucursalEmpleadoEjecutivoPortafolio = agentes.get(0).IdSucursal;
						detalleMovimientoPoliza.IdEmpleadoEjecutivoPortafolio = agentes.get(0).IdEmpleadoEjecutivo;
					}
					
					//ingresar
					int[] res = sql.batch(conn, SQL_INGRESAR_DETALLE_MOVIMIENTO_POLIZA, Arrays.asList(detalleMovimientoPoliza), EntidadDetalleMovimientoPoliza.class);
					for (int i : res) {
						if(i==1){
							salida.add(linea + " - DETALLE MOVIMIENTO POLIZA CON ID: " + detalleMovimientoPoliza.IdComprobanteDetalle + ", INGRESADA CORRECTAMENTE");
						}else{
							salida.add(linea + " - DETALLE MOVIMIENTO POLIZA CON ID: " + detalleMovimientoPoliza.IdComprobanteDetalle + ", NO INGRESADA");
						}
					}
					
					
				} catch (Exception e) {
					e.printStackTrace();
					salida.add(linea + " - DETALLE MOVIMIENTO POLIZA CON ID: " + record.get("IdComprobanteDetalle") + ", NO INGRESADA");
				}
				linea++;
			}
		} catch (SQLException e1) {
			salida.add("ERROR AL OBTENER CONEXION");
		}
		return salida;
	}

}
