package cl.bcs.loaderapp.concrete.facturacion;

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

public class DetallePolizaDbLoader implements DbLoader {
	
	public String SQL_INGRESAR_DETALLE_POLIZA = loadResourceToString("cl/bcs/loaderapp/sql/facturacion/DetallePoliza_Ingresar.sql");
	
	public String SQL_CONSULTA_INSTRUMENTO = "SELECT I.\"IdInstrumento\", I.\"Nemotecnico\", I.\"IdTipoInstrumento\", I.\"IdMoneda\", TI.\"IdClasificacionTipoInstrumento\", TI.\"Nombre\" \r\n"
			+ "FROM \"BSCL.Plataforma.BD::INS.Instrumento\" I\r\n"
			+ "INNER JOIN \"BSCL.Plataforma.BD::INS.TipoInstrumento\" TI ON  TI.\"IdTipoInstrumento\" = I.\"IdTipoInstrumento\"\r\n"
			+ "WHERE I.\"Nemotecnico\" = :Nemotecnico;";
	public static class Instrumento {
		public Long IdInstrumento;
		public String Nemotecnico;
		public String IdMoneda;
		public String IdTipoInstrumento;
		public String IdClasificacionTipoInstrumento;
		public String Nombre;
	}
	
	public String SQL_CONSULTA_EJECUTIVO = "SELECT COALESCE(P.\"IdEmpleadoEjecutivo\",0) AS \"IdEmpleadoEjecutivo\" FROM \"BSCL.Facturacion.BD::FAC.Comprobante\" C\r\n" + 
			"INNER JOIN \"BSCL.Facturacion.BD::FAC.ComprobanteDetalle\" CD ON C.\"IdComprobante\" = CD.\"IdComprobante\"\r\n" + 
			"INNER JOIN \"BSCL.Portafolio.BD::POR.Portafolio\" P ON C.\"IdPortafolio\" = P.\"IdPortafolio\"\r\n" + 
			"WHERE C.\"IdComprobante\" = :IdComprobante";
	public static class Ejecutivo{
		public Long IdEmpleadoEjecutivo;
	}
	
	public static class EntidadDetallePoliza{
		public String IdComprobante;
		public String NroLinea;
		public String FechaLiquidacion;
		public String NemotecnicoInstrumento;
		public String IdTipoOperacion;
		public String IdTipoProducto;
		public String Cantidad;
		public String Precio;
		public String Monto;
		public String IdBolsa;
		public String IdEstadoComprobanteDetalle;
		public String FechaIngresoRegistro;
		public String IdMoneda;
		public String IdMonedaComprobanteDetalle;
		public String IdMonedaLiquidacion;
		public String MontoComprobanteDetalle;
		public String MontoLiquidacion;
		public String IdTipoInstrumentoSubyacente;
		public String NombreClasificacionTipoInstrumentoRelacionado;
		public String NemotecnicoSubyacente;
		public String IdClasificacionTipoInstrumentoRelacionado;
		public String NombreTipoInstrumentoRelacionado;
		public String IdTipoInstrumentoRelacionado;
		public String NombreClasificacionTipoInstrumento;
		public String NemotecnicoRelacionado;
		public String SucursalEmpleadoEjecutivoPortafolio;
		public String IdConcepto;
		public String ModPrefFact;
		public String IdInstrumentoNuevo;
		public String IdMesa;
		public String EsVentaCorta;
		public String IndicadorOrdenDirecta;
		//internos
		public Long IdComprobanteDetalle;
		public Long IdInstrumento;
		public Long IdInstrumentoSubyacente;
		public String IdTipoInstrumento;
		public String NombreTipoInstrumento;
		public String IdClasificacionTipoInstrumento;
		public Long IdInstrumentoRelacionado;
		public String IdMonedaSubyacente;
		public String NombreTipoInstrumentoSubyacente;
		public Long IdEmpleadoEjecutivoPortafolio;
	}

	@Override
	public List<String> loadIntoDb(BasicDataSource jsJB, EntradaCargaGeneral in, List<Object> lines) {
		List<String> salida = new ArrayList<String>();
		
		SQL sql = new SQL();
		
		List<CSVRecord> detallesCsv = (List<CSVRecord>) (Object) lines;
		try(Connection conn = jsJB.getConnection()){
			int linea = 0;
			List<Long> idsDetallePolizas = Utils.getSecuencia(conn, "BSCL.Facturacion.BD.Secuencias::ComprobanteDetalle", (long) lines.size());
		
			for(CSVRecord record : detallesCsv) {
				EntidadDetallePoliza detallePoliza = new EntidadDetallePoliza();
				detallePoliza.IdComprobante = record.get("IdComprobante");
				detallePoliza.NroLinea = record.get("NroLinea");
				detallePoliza.FechaLiquidacion = record.get("FechaLiquidacion");
				
				detallePoliza.IdTipoOperacion = record.get("IdTipoOperacion");
				detallePoliza.IdTipoProducto = record.get("IdTipoProducto");
				detallePoliza.Cantidad = record.get("Cantidad");
				detallePoliza.Precio = record.get("Precio");
				detallePoliza.Monto = record.get("Monto");
				detallePoliza.IdBolsa = record.get("IdBolsa");
				detallePoliza.IdEstadoComprobanteDetalle = record.get("IdEstadoComprobanteDetalle");
				detallePoliza.FechaIngresoRegistro = record.get("FechaIngresoRegistro");
				detallePoliza.IdMoneda = record.get("IdMoneda");
				detallePoliza.IdMonedaComprobanteDetalle = record.get("IdMonedaComprobanteDetalle");
				detallePoliza.IdMonedaLiquidacion = record.get("IdMonedaLiquidacion");
				detallePoliza.MontoComprobanteDetalle = record.get("MontoComprobanteDetalle");
				detallePoliza.MontoLiquidacion = record.get("MontoLiquidacion");
				detallePoliza.IdTipoInstrumentoSubyacente = record.get("IdTipoInstrumentoSubyacente");
				detallePoliza.NombreClasificacionTipoInstrumentoRelacionado = record.get("NombreClasificacionTipoInstrumentoRelacionado");
				
				detallePoliza.IdClasificacionTipoInstrumentoRelacionado = record.get("IdClasificacionTipoInstrumentoRelacionado");
				detallePoliza.NombreTipoInstrumentoRelacionado = record.get("NombreTipoInstrumentoRelacionado");
				detallePoliza.IdTipoInstrumentoRelacionado = record.get("IdTipoInstrumentoRelacionado");
				detallePoliza.NombreClasificacionTipoInstrumento = record.get("NombreClasificacionTipoInstrumento");
				
				detallePoliza.SucursalEmpleadoEjecutivoPortafolio = record.get("SucursalEmpleadoEjecutivoPortafolio");
				detallePoliza.IdConcepto = record.get("IdConcepto");
				detallePoliza.ModPrefFact = record.get("ModPrefFact");
				detallePoliza.IdInstrumentoNuevo = record.get("IdInstrumentoNuevo");
				detallePoliza.IdMesa = record.get("IdMesa");
				detallePoliza.EsVentaCorta = record.get("EsVentaCorta");
				detallePoliza.IndicadorOrdenDirecta = record.get("IndicadorOrdenDirecta");
				
				//internos
				detallePoliza.IdComprobanteDetalle = idsDetallePolizas.get(linea);
				
				//obtener info nemotecnico
				HashMap<String, Object> params = new HashMap<>();
				if(!record.get("NemotecnicoInstrumento").equals("")) {
					params.put("Nemotecnico", record.get("NemotecnicoInstrumento"));
					List<Instrumento> instrumentos = sql.query(conn, SQL_CONSULTA_INSTRUMENTO, params, Instrumento.class);
					detallePoliza.IdClasificacionTipoInstrumento = instrumentos.get(0).IdClasificacionTipoInstrumento;
					detallePoliza.IdTipoInstrumento = instrumentos.get(0).IdTipoInstrumento;
					detallePoliza.IdInstrumento = instrumentos.get(0).IdInstrumento;
					detallePoliza.NombreTipoInstrumento = instrumentos.get(0).Nombre;
					detallePoliza.NemotecnicoInstrumento = record.get("NemotecnicoInstrumento");
				}
				//obtener subyacente
				params = new HashMap<>();
				if(!record.get("NemotecnicoSubyacente").equals("")) {
					params.put("Nemotecnico", record.get("NemotecnicoSubyacente"));
					List<Instrumento> instrumentos = sql.query(conn, SQL_CONSULTA_INSTRUMENTO, params, Instrumento.class);
					detallePoliza.IdTipoInstrumentoSubyacente = instrumentos.get(0).IdTipoInstrumento;
					detallePoliza.IdInstrumentoSubyacente = instrumentos.get(0).IdInstrumento;
					detallePoliza.IdMonedaSubyacente = instrumentos.get(0).IdMoneda;
					detallePoliza.NombreTipoInstrumentoSubyacente = instrumentos.get(0).Nombre;
					detallePoliza.NemotecnicoSubyacente = record.get("NemotecnicoSubyacente");
				}
				
				//obtener relacionado
				params = new HashMap<>();
				if(!record.get("NemotecnicoRelacionado").equals("")) {
					params.put("Nemotecnico", record.get("NemotecnicoRelacionado"));
					List<Instrumento> instrumentos = sql.query(conn, SQL_CONSULTA_INSTRUMENTO, params, Instrumento.class);
					detallePoliza.IdInstrumentoRelacionado = instrumentos.get(0).IdInstrumento;
					detallePoliza.NemotecnicoRelacionado = record.get("NemotecnicoRelacionado");
				}
				
				//obtener instrumento relacionado
				params = new HashMap<>();
				if(!record.get("IdComprobante").equals("")) {
					params.put("IdComprobante", record.get("IdComprobante"));
					List<Ejecutivo> ejecutivos = sql.query(conn, SQL_CONSULTA_EJECUTIVO, params, Ejecutivo.class);
					detallePoliza.IdEmpleadoEjecutivoPortafolio = ejecutivos.get(0).IdEmpleadoEjecutivo;
				}
				
				

				int[] res = sql.batch(conn, SQL_INGRESAR_DETALLE_POLIZA, Arrays.asList(detallePoliza), EntidadDetallePoliza.class);
				for (int i : res) {
					if(i==1){
						salida.add(linea + " - OPCION CON ID COMPROBANTE: " + detallePoliza.IdComprobante + ", INGRESADA CORRECTAMENTE");
					}else{
						salida.add(linea + " - OPCION CON ID COMPROBANTE: " + detallePoliza.IdComprobante + ", NO INGRESADA");
					}
				}
				
				
				/*
				detallePoliza.IdEmpleadoEjecutivoPortafolio
				*/
			}
		}catch (Exception e) {
			salida.add("ERROR AL OBTENER CONEXION");
			e.printStackTrace();
		}
		return salida;
	}

}
