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
import cl.bcs.loaderapp.servicios.ConsultarDocumentoIdentidadCliente;
import cl.bcs.loaderapp.servicios.ConsultarDocumentoIdentidadCliente.SalidaSocioNegocio;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class PolizaDbLoader implements DbLoader {
	
	public String SQL_CONSULTA_PORTAFOLIO = "SELECT \"IdPortafolio\" FROM \"BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio\" CCP INNER JOIN \"BSCL.SocioNegocio.BD::PER.DocumentoIdentidad\" DI ON CCP.\"IdPersona\" = DI.\"IdPersona\" WHERE DI.\"DocumentoIdentidad\" = :DocumentoIdentidadCliente AND DI.\"IdTipoDocumentoIdentidad\" = :TipoDocumentoIdentidad AND CCP.\"CodigoPortafolio\" = :CodigoPortafolio;";
	public static class Portafolio{
		public Long IdPortafolio;
	}
	
	public String SQL_CONSULTA_CLIENTE = "SELECT \"NombreCompleto\" FROM \"BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente\" WHERE \"IdCliente\" = :IdCliente";
	public static class EntidadCliente{
		public String NombreCompleto;
	}
	
	public String SQL_INGRESAR_POLIZA = loadResourceToString("cl/bcs/loaderapp/sql/facturacion/Poliza_Ingresar.sql");

	public static class EntidadPoliza{
		public String IdEmpresa;
		public String CodigoPortafolio;
		public String TipoPersona;
		public String DocumentoIdentidad;
		public String TipoDocumentoIdentidad;
		public String DireccionFacturacion;
		public String IdTipoComprobante;
		public String IdTipoOperacion;
		public String IdTipoProducto;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public String Monto;
		public String MontoCobro;
		public String MontoFinal;
		public String IdCondicionLiquidacion;
		public String FechaLiquidacion;
		public String IdEstadoComprobante;
		public String Glosa;
		public String EsExento;
		public String FechaIngresoRegistro;
		public String FechaSistema;
		public String IdMoneda;
		public String IdMonedaLiquidacion;
		public String MontoLiquidacion;
		public String IdMonedaComprobante;
		public String MontoComprobante;
		public String ModPrefFact;
		public String IdConcepto;
		public String CodigoAgente;
		public String IdSucursalEmpleadoEjecutivoPortafolio;
		public String IdBolsa;
		public String IdComprobanteReferencia;
		public String FolioComprobanteReferencia;
		public String FechaComprobanteReferencia;
		public String IdTipoComprobanteReferencia;
		public String Cantidad;
		public String FechaComprobante;
		public String MontoComprobanteFinal;
		public String MontoLiquidacionFinal;
		public String MontoComprobanteCobro;
		public String MontoLiquidacionCobro;
		public String EsVentaCorta;
		public String IdFormaCustodia;
		
		//internos
		public Long IdComprobante;
		public Long IdPortafolio;
		public Long IdCliente;
		public String NombreLargoCliente;
		public Long FolioComprobante;
		public String FechaEmision;
		public Long IdUsuario;
		public Long IdEmpleadoEjecutivoPortafolio;
		
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
			List<Long> idsPolizas= Utils.getSecuencia(conn, "BSCL.Facturacion.BD.Secuencias::Comprobante", (long) lines.size());
			for (CSVRecord record : polizasCSV) {
				try {
					EntidadPoliza poliza = new EntidadPoliza();
					poliza.IdEmpresa = record.get("IdEmpresa");
					poliza.CodigoPortafolio = record.get("CodigoPortafolio");
					poliza.TipoPersona = record.get("TipoPersona");
					poliza.DocumentoIdentidad = record.get("DocumentoIdentidad");
					poliza.TipoDocumentoIdentidad = record.get("TipoDocumentoIdentidad");
					poliza.DireccionFacturacion = record.get("DireccionFacturacion");
					poliza.IdTipoComprobante = record.get("IdTipoComprobante");
					poliza.IdTipoOperacion = record.get("IdTipoOperacion");
					poliza.IdTipoProducto = record.get("IdTipoProducto");
					poliza.IdClasificacionTipoInstrumento = record.get("IdClasificacionTipoInstrumento");
					poliza.IdTipoInstrumento = record.get("IdTipoInstrumento");
					poliza.Monto = record.get("Monto");
					poliza.MontoCobro = record.get("MontoCobro");
					poliza.MontoFinal = record.get("MontoFinal");
					poliza.IdCondicionLiquidacion = record.get("IdCondicionLiquidacion");
					poliza.FechaLiquidacion = record.get("FechaLiquidacion");
					poliza.IdEstadoComprobante = record.get("IdEstadoComprobante");
					poliza.Glosa = record.get("Glosa");
					poliza.EsExento = record.get("EsExento");
					poliza.FechaIngresoRegistro = record.get("FechaIngresoRegistro");
					poliza.FechaSistema = record.get("FechaSistema");
					poliza.IdMoneda = record.get("IdMoneda");
					poliza.IdMonedaLiquidacion = record.get("IdMonedaLiquidacion");
					poliza.MontoLiquidacion = record.get("MontoLiquidacion");
					poliza.IdMonedaComprobante = record.get("IdMonedaComprobante");
					poliza.MontoComprobante = record.get("MontoComprobante");
					poliza.ModPrefFact = record.get("ModPrefFact");
					poliza.IdConcepto = record.get("IdConcepto");
					poliza.CodigoAgente = record.get("CodigoAgente");
					poliza.IdSucursalEmpleadoEjecutivoPortafolio = record.get("IdSucursalEmpleadoEjecutivoPortafolio");
					poliza.IdBolsa = record.get("IdBolsa");
					poliza.IdComprobanteReferencia = record.get("IdComprobanteReferencia");
					poliza.FolioComprobanteReferencia = record.get("FolioComprobanteReferencia");
					poliza.FechaComprobanteReferencia = record.get("FechaComprobanteReferencia");
					poliza.IdTipoComprobanteReferencia = record.get("IdTipoComprobanteReferencia");
					poliza.Cantidad = record.get("Cantidad");
					poliza.FechaComprobante = record.get("FechaComprobante");
					poliza.MontoComprobanteFinal = record.get("MontoComprobanteFinal");
					poliza.MontoLiquidacionFinal = record.get("MontoLiquidacionFinal");
					poliza.MontoComprobanteCobro = record.get("MontoComprobanteCobro");
					poliza.MontoLiquidacionCobro = record.get("MontoLiquidacionCobro");
					poliza.EsVentaCorta = record.get("EsVentaCorta");
					poliza.IdFormaCustodia = record.get("IdFormaCustodia");
					
					//obtener id comprobante
					poliza.IdComprobante = idsPolizas.get(linea);
					
					//obtener id portafolio
					HashMap<String, Object> params = new HashMap<>();
					params.put("DocumentoIdentidadCliente", poliza.DocumentoIdentidad);
					params.put("TipoDocumentoIdentidad", poliza.TipoDocumentoIdentidad);
					params.put("CodigoPortafolio", poliza.CodigoPortafolio);
					List<Portafolio> portafolios = sql.query(conn, SQL_CONSULTA_PORTAFOLIO, params, Portafolio.class);
					poliza.IdPortafolio = portafolios.get(0).IdPortafolio;
					
					//obtener id cliente
					ConsultarDocumentoIdentidadCliente consultarDocumentoIdentidad = new ConsultarDocumentoIdentidadCliente();
					SalidaSocioNegocio salidaSocioNegocio = consultarDocumentoIdentidad.logica(conn, poliza.DocumentoIdentidad, poliza.TipoDocumentoIdentidad, poliza.TipoPersona, idEmpresaDefecto);
					poliza.IdCliente = salidaSocioNegocio.IdCliente;//obtener con el documento identidad
					
					//obtener nombre cliente?
					params = new HashMap<>();
					params.put("IdCliente", poliza.IdCliente);
					List<EntidadCliente> clientes = sql.query(conn, SQL_CONSULTA_CLIENTE, params, EntidadCliente.class);
					poliza.NombreLargoCliente = clientes.get(0).NombreCompleto;
					
					//folio comprobante es asignado luego de pasar por el DTE.
					poliza.FolioComprobante = new Long(0);
					
					//obtener fecha emision
					poliza.FechaEmision = "2017-01-04";
					
					// id usuario defecto
					poliza.IdUsuario = idUsuarioDefecto;
					
					int[] res = sql.batch(conn, SQL_INGRESAR_POLIZA, Arrays.asList(poliza), EntidadPoliza.class);
					for (int i : res) {
						if(i==1){
							salida.add(linea + " - POLIZA CON DOCUMENTO IDENTIDAD: " + poliza.DocumentoIdentidad + ", INGRESADA CORRECTAMENTE");
						}else{
							salida.add(linea + " - POLIZA CON DOCUMENTO IDENTIDAD: " + poliza.DocumentoIdentidad + ", NO INGRESADA");
						}
					}
					
				} catch (Exception e) {
					e.printStackTrace();
					salida.add(linea + " - POLIZA CON DOCUMENTO IDENTIDAD: " + record.get("DocumentoIdentidad") + ", NO INGRESADA");
				}
				linea++;
			}
		} catch (SQLException e1) {
			salida.add("ERROR AL OBTENER CONEXION");
			e1.printStackTrace();
		}
		return salida;
	}
	
}
