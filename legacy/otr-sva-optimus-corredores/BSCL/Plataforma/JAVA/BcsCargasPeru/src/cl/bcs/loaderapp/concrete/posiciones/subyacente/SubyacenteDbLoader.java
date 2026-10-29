package cl.bcs.loaderapp.concrete.posiciones.subyacente;

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

public class SubyacenteDbLoader implements DbLoader {
	
	public String SQL_INSERT_SUBYACENTE = loadResourceToString("cl/bcs/loaderapp/sql/posiciones/subyacente/Subyacente_Ingresar.sql");

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
	
	public String SQL_CONSULTA_AGENTE = "SELECT \"IdEmpleadoEjecutivo\" FROM \"BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo\" WHERE \"CodigoEmpleado\" = :CodigoAgente;";
	public static class Agente{
		public Long IdEmpleadoEjecutivo;
	}

	public static class EntidadSubyacente {
		public Long IdSubyacente;
		
		public String FolioSubyacente;
		public String FolioCarteraRelacionada;
		public String FechaIngresoRegistro;
		public String IdCarteraRetirada;
		public String IdMovimientoCarteraEnTransito;
		public String PrecioCompraOperacionRelacionada;
		public String TasaCompraOperacionRelacionada;
		public String IdEstado;
		public String IdTipoCanalIngreso;
		
		public Long IdInstrumentoAnterior;
		public String NemotecnicoAnterior;
		
		public Long IdInstrumento;
		public String Nemotecnico;
		
		public String IdEmisor;
		public String NemoSVS;
		public String IdTipoReajuste;
		public String EsPagaderoEn;
		public String FechaEmisionInstrumento;
		public String FechaVencimientoInstrumento;
		public String EsFungible;
		public String TieneCorteDeCupon;
		public String IdMonedaInstrumento;
		public String ISIN;
		public String FechaFactorMonedaInstrumento;
		public String ValorFactorMonedaInstrumento;
		public String Cantidad;
		public String CorteMinimo;
		public String ValorRescate;
		public String PrecioSubyacente;
		public String TasaSubyacente;
		public String MontoSubyacente;
		public String MontoSubyacenteMonedaInstrumento;
		public String MontoSubyacenteMonedaAdicional;
		public String INGFechaOperacion;
		public String INGCantidad;
		public String INGValorRescate;
		public String INGPrecioSubyacente;
		public String INGTasaSubyacente;
		public String INGMontoATasaSubyacenteActual;
		public String INGMontoSubyacenteActualMonedaInstrumento;
		public String INGMontoSubyacenteActualMonedaAdicional;
		public String INGPrecioMercadoActual;
		public String INGTasaMercadoActual;
		public String INGMontoATasaMercadoActual;
		public String INGMontoMercadoActualMonedaInstrumento;
		public String INGMontoMercadoActualMonedaAdicional;
		public String INGMontoATasaCompraActual;
		public String INGMontoCompraActualMonedaInstrumento;
		public String INGMontoCompraActualMonedaAdicional;
		public String INGTasaCorredora;
		public String INGFechaTasaCorredora;
		public String INGMontoATasaCorredora;
		public String INGMontoATasaCorredoraMonedaInstrumento;
		public String INGMontoATasaCorredoraMonedaAdicional;
		public String INGFechaCorteCupon;
		public String INGNumeroCupon;
		public String INGIdMonedaPagoCupon;
		public String INGMontoCorteCupon;
		public String INGFechaPagoCorteCupon;
		public String EGRFechaOperacion;
		public String EGRIdTipoCanalIngreso;
		public String EGRIdCondicionLiquidacion;
		public String EGRFechaLiquidacion;
		public String EGRIdTipoComprobante;
		public String EGRFolioComprobante;
		public String EGRFechaComprobante;
		public String EGRFolioExternoReferencia;
		public String EGRIdTipoOperacion;
		public String EGRIdOperacion;
		public String EGRIdConcepto;
		public String EGRIdSucursal;
		public String CodigoAgente;
		public Long EGRIdAgente;
		public String EGRCantidad;
		public String EGRValorRescate;
		public String EGRPrecio;
		public String EGRTasa;
		public String EGRMonto;
		public String EGRMontoMonedaInstrumento;
		public String EGRMontoMonedaAdicional;
		public String EGRSpread;
		public String EGRFechaPrecioTasaMercado;
		public String EGRFuenteTasaPrecioMercado;
		public String EGRPrecioMercado;
		public String EGRTasaMercado;
		public String EGRMontoATasaMercado;
		public String EGRMontoMercadoMonedaInstrumento;
		public String EGRMontoMercadoMonedaAdicional;
		public String EGRSpreadMercado;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		
		
	}

	@Override
	public List<String> loadIntoDb(BasicDataSource jsJB, EntradaCargaGeneral in, List<Object> lines) {
		List<String> salida = new ArrayList<String>();

		Long idUsuarioDefecto = in.p_IdUsuario;
		Long idEmpresaDefecto = in.p_IdEmpresa;
		SQL sql = new SQL();

		try (Connection conn = jsJB.getConnection()){
			//IdentificarLlaves identificarLlaves = new IdentificarLlavesBuilder(conn).setCiudades().setPaises().build();
			
			List<Long> ids = Utils.getSecuencia(conn, "BSCL.Custodia.BD.Secuencias::Subyacente", (long) lines.size());
			long linea = 0;
			List<CSVRecord> subyacenteCSV = (List<CSVRecord>) (Object) lines;
			for (CSVRecord record : subyacenteCSV) {
				
				try {
					
				
					EntidadSubyacente subyacente = new EntidadSubyacente();
					subyacente.IdSubyacente = ids.get((int) linea);
						
					subyacente.FolioSubyacente = record.get("FolioSubyacente");
					subyacente.FolioCarteraRelacionada = record.get("FolioCarteraRelacionada");
					subyacente.FechaIngresoRegistro = record.get("FechaIngresoRegistro");
					subyacente.IdCarteraRetirada = record.get("IdCarteraRetirada");
					subyacente.IdMovimientoCarteraEnTransito = record.get("IdMovimientoCarteraEnTransito");
					subyacente.PrecioCompraOperacionRelacionada = record.get("PrecioCompraOperacionRelacionada");
					subyacente.TasaCompraOperacionRelacionada = record.get("TasaCompraOperacionRelacionada");
					subyacente.IdEstado = record.get("IdEstado");
					subyacente.IdTipoCanalIngreso = record.get("IdTipoCanalIngreso");
					
					HashMap<String, Object> params = new HashMap<>();
					List<Instrumento> instrumentos = null;
					//obtener instrumento con el nemo
					if(!record.get("NemotecnicoAnterior").equals("")) {
						params.put("Nemotecnico", record.get("NemotecnicoAnterior"));
						instrumentos = sql.query(conn, SQL_CONSULTA_INSTRUMENTO, params, Instrumento.class);
						subyacente.IdInstrumentoAnterior = instrumentos.get(0).IdInstrumento;//obtener con el nemo
					}else {
						subyacente.IdInstrumentoAnterior = (long) 0 ; 
					}
					
					
					params = new HashMap<>();
					if(!record.get("Nemotecnico").equals("")) {
						params.put("Nemotecnico", record.get("Nemotecnico"));
						instrumentos = sql.query(conn, SQL_CONSULTA_INSTRUMENTO, params, Instrumento.class);
						subyacente.IdClasificacionTipoInstrumento = instrumentos.get(0).IdClasificacionTipoInstrumento;//obtener con el nemo
						subyacente.IdTipoInstrumento = instrumentos.get(0).IdTipoInstrumento;//obtener con el nemo
						subyacente.IdInstrumento = instrumentos.get(0).IdInstrumento;//obtener con el nemo
						subyacente.Nemotecnico = record.get("Nemotecnico");
					}
					
					
					
					subyacente.IdEmisor = record.get("IdEmisor");
					subyacente.NemoSVS = record.get("NemoSVS");
					subyacente.IdTipoReajuste = record.get("IdTipoReajuste");
					subyacente.EsPagaderoEn = record.get("EsPagaderoEn");
					subyacente.FechaEmisionInstrumento = record.get("FechaEmisionInstrumento");
					subyacente.FechaVencimientoInstrumento = record.get("FechaVencimientoInstrumento");
					subyacente.EsFungible = record.get("EsFungible");
					subyacente.TieneCorteDeCupon = record.get("TieneCorteDeCupon");
					subyacente.IdMonedaInstrumento = record.get("IdMonedaInstrumento");
					subyacente.ISIN = record.get("ISIN");
					subyacente.FechaFactorMonedaInstrumento = record.get("FechaFactorMonedaInstrumento");
					subyacente.ValorFactorMonedaInstrumento = record.get("ValorFactorMonedaInstrumento");
					subyacente.Cantidad = record.get("Cantidad");
					subyacente.CorteMinimo = record.get("CorteMinimo");
					subyacente.ValorRescate = record.get("ValorRescate");
					subyacente.PrecioSubyacente = record.get("PrecioSubyacente");
					subyacente.TasaSubyacente = record.get("TasaSubyacente");
					subyacente.MontoSubyacente = record.get("MontoSubyacente");
					subyacente.MontoSubyacenteMonedaInstrumento = record.get("MontoSubyacenteMonedaInstrumento");
					subyacente.MontoSubyacenteMonedaAdicional = record.get("MontoSubyacenteMonedaAdicional");
					subyacente.INGFechaOperacion = record.get("INGFechaOperacion");
					subyacente.INGCantidad = record.get("INGCantidad");
					subyacente.INGValorRescate = record.get("INGValorRescate");
					subyacente.INGPrecioSubyacente = record.get("INGPrecioSubyacente");
					subyacente.INGTasaSubyacente = record.get("INGTasaSubyacente");
					subyacente.INGMontoATasaSubyacenteActual = record.get("INGMontoATasaSubyacenteActual");
					subyacente.INGMontoSubyacenteActualMonedaInstrumento = record
							.get("INGMontoSubyacenteActualMonedaInstrumento");
					subyacente.INGMontoSubyacenteActualMonedaAdicional = record
							.get("INGMontoSubyacenteActualMonedaAdicional");
					subyacente.INGPrecioMercadoActual = record.get("INGPrecioMercadoActual");
					subyacente.INGTasaMercadoActual = record.get("INGTasaMercadoActual");
					subyacente.INGMontoATasaMercadoActual = record.get("INGMontoATasaMercadoActual");
					subyacente.INGMontoMercadoActualMonedaInstrumento = record
							.get("INGMontoMercadoActualMonedaInstrumento");
					subyacente.INGMontoMercadoActualMonedaAdicional = record.get("INGMontoMercadoActualMonedaAdicional");
					subyacente.INGMontoATasaCompraActual = record.get("INGMontoATasaCompraActual");
					subyacente.INGMontoCompraActualMonedaInstrumento = record.get("INGMontoCompraActualMonedaInstrumento");
					subyacente.INGMontoCompraActualMonedaAdicional = record.get("INGMontoCompraActualMonedaAdicional");
					subyacente.INGTasaCorredora = record.get("INGTasaCorredora");
					subyacente.INGFechaTasaCorredora = record.get("INGFechaTasaCorredora");
					subyacente.INGMontoATasaCorredora = record.get("INGMontoATasaCorredora");
					subyacente.INGMontoATasaCorredoraMonedaInstrumento = record
							.get("INGMontoATasaCorredoraMonedaInstrumento");
					subyacente.INGMontoATasaCorredoraMonedaAdicional = record.get("INGMontoATasaCorredoraMonedaAdicional");
					subyacente.INGFechaCorteCupon = record.get("INGFechaCorteCupon");
					subyacente.INGNumeroCupon = record.get("INGNumeroCupon");
					subyacente.INGIdMonedaPagoCupon = record.get("INGIdMonedaPagoCupon");
					subyacente.INGMontoCorteCupon = record.get("INGMontoCorteCupon");
					subyacente.INGFechaPagoCorteCupon = record.get("INGFechaPagoCorteCupon");
					subyacente.EGRFechaOperacion = record.get("EGRFechaOperacion");
					subyacente.EGRIdTipoCanalIngreso = record.get("EGRIdTipoCanalIngreso");
					subyacente.EGRIdCondicionLiquidacion = record.get("EGRIdCondicionLiquidacion");
					subyacente.EGRFechaLiquidacion = record.get("EGRFechaLiquidacion");
					subyacente.EGRIdTipoComprobante = record.get("EGRIdTipoComprobante");
					subyacente.EGRFolioComprobante = record.get("EGRFolioComprobante");
					subyacente.EGRFechaComprobante = record.get("EGRFechaComprobante");
					subyacente.EGRFolioExternoReferencia = record.get("EGRFolioExternoReferencia");
					subyacente.EGRIdTipoOperacion = record.get("EGRIdTipoOperacion");
					subyacente.EGRIdOperacion = record.get("EGRIdOperacion");
					subyacente.EGRIdConcepto = record.get("EGRIdConcepto");
					subyacente.EGRIdSucursal = record.get("EGRIdSucursal");
					
					
					params = new HashMap<>();
					if(!record.get("CodigoAgente").equals("")) {
						params.put("CodigoAgente", record.get("CodigoAgente"));
						List<Agente> agentes = sql.query(conn, SQL_CONSULTA_AGENTE, params, Agente.class);
						subyacente.EGRIdAgente = agentes.get(0).IdEmpleadoEjecutivo;
							
					}else {
						subyacente.EGRIdAgente = (long) 0;
					}
					subyacente.CodigoAgente = record.get("CodigoAgente");
					
					
					subyacente.EGRCantidad = record.get("EGRCantidad");
					subyacente.EGRValorRescate = record.get("EGRValorRescate");
					subyacente.EGRPrecio = record.get("EGRPrecio");
					subyacente.EGRTasa = record.get("EGRTasa");
					subyacente.EGRMonto = record.get("EGRMonto");
					subyacente.EGRMontoMonedaInstrumento = record.get("EGRMontoMonedaInstrumento");
					subyacente.EGRMontoMonedaAdicional = record.get("EGRMontoMonedaAdicional");
					subyacente.EGRSpread = record.get("EGRSpread");
					subyacente.EGRFechaPrecioTasaMercado = record.get("EGRFechaPrecioTasaMercado");
					subyacente.EGRFuenteTasaPrecioMercado = record.get("EGRFuenteTasaPrecioMercado");
					subyacente.EGRPrecioMercado = record.get("EGRPrecioMercado");
					subyacente.EGRTasaMercado = record.get("EGRTasaMercado");
					subyacente.EGRMontoATasaMercado = record.get("EGRMontoATasaMercado");
					subyacente.EGRMontoMercadoMonedaInstrumento = record.get("EGRMontoMercadoMonedaInstrumento");
					subyacente.EGRMontoMercadoMonedaAdicional = record.get("EGRMontoMercadoMonedaAdicional");
					subyacente.EGRSpreadMercado = record.get("EGRSpreadMercado");
					
					int[] res = sql.batch(conn, SQL_INSERT_SUBYACENTE, Arrays.asList(subyacente), EntidadSubyacente.class);
					for (int i : res) {
						if(i==1){
							salida.add(linea + " - SUBYACENTE A FOLIO: " + subyacente.FolioSubyacente + ", INGRESADA CORRECTAMENTE");
						}else{
							salida.add(linea + " - SUBYACENTE A FOLIO: " + subyacente.FolioSubyacente + ", NO INGRESADA");
						}
					}
				
				} catch (Exception e) {
					e.printStackTrace();
					salida.add(linea + " - SUBYACENTE A FOLIO: " + record.get("FolioSubyacente") + ", NO INGRESADA");
				}
				linea++;
			}

		} catch (SQLException e) {

			e.printStackTrace();
		}

		return salida;
	}

}
