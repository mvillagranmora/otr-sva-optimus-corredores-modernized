package cl.bcs.loaderapp.concrete.posiciones.cartera;

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
import cl.bcs.plataforma.pla.utils.IdentificarLlaves;
import cl.bcs.plataforma.pla.utils.IdentificarLlavesBuilder;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class MovimientoCarteraDbLoader implements DbLoader {
	
	public String SQL_INSERT_MOVIMIENTO_CARTERA = loadResourceToString("cl/bcs/loaderapp/sql/posiciones/cartera/MovimientoCartera_Ingresar.sql");
	
	public String SQL_CONSULTA_PORTAFOLIO = "SELECT \"IdPortafolio\" FROM \"BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio\" CCP INNER JOIN \"BSCL.SocioNegocio.BD::PER.DocumentoIdentidad\" DI ON CCP.\"IdPersona\" = DI.\"IdPersona\" WHERE DI.\"DocumentoIdentidad\" = :DocumentoIdentidadCliente AND DI.\"IdTipoDocumentoIdentidad\" = :TipoDocumentoIdentidad AND CCP.\"CodigoPortafolio\" = :CodigoPortafolio;";
	public static class Portafolio{
		public Long IdPortafolio;
		@Override
		public String toString() { return String.format("Portafolio [IdPortafolio=%s]", IdPortafolio); }
	}
	
	public String SQL_CONSULTA_AGENTE = "SELECT \"IdEmpleadoEjecutivo\" FROM \"BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo\" WHERE \"CodigoEmpleado\" = :CodigoAgente;";
	public static class Agente{
		public Long IdEmpleadoEjecutivo;
	}
	
	public String SQL_CONSULTA_INSTRUMENTO = "SELECT I.\"IdInstrumento\", I.\"Nemotecnico\", I.\"IdTipoInstrumento\", TI.\"IdClasificacionTipoInstrumento\", I.\"IdMoneda\" \r\n"
			+ "FROM \"BSCL.Plataforma.BD::INS.Instrumento\" I\r\n"
			+ "INNER JOIN \"BSCL.Plataforma.BD::INS.TipoInstrumento\" TI ON  TI.\"IdTipoInstrumento\" = I.\"IdTipoInstrumento\"\r\n"
			+ "WHERE I.\"Nemotecnico\" = :Nemotecnico;";
	public static class Instrumento {
		public Long IdInstrumento;
		public String Nemotecnico;
		public String IdTipoInstrumento;
		public String IdClasificacionTipoInstrumento;
		public String IdMoneda;
	}
	
	
	public class EntidadMovimientoCartera{
		public String IdMovimientoCarteraEnTransitoRelacionado;
		public String FechaIngresoRegistro;
		public String FechaCertificacionRegistro;
		public String FechaAnulacionRegistro;
		public String Usuario;
		public String FechaOperacion;
		public String FechaVencimientoOperacion;
		public String PlazoEnDiasOperacion;
		public String FolioCustodia;
		public String IdTipoComprobante;
		public String FolioComprobante;
		public String MueveCustodia;
		public String OrigenCustodia;
		public String IdCustodiaOrigen;
		public String IdTipoOperacionOrigen;
		public String FolioCarteraGarantizada;
		public String IdInstrumentoCarteraGarantizada;
		public String EsEspecifica;
		public String IdContraparteGarantia;
		public String CuentaContraparteGarantia;
		public String IdOrigenGarantia;
		public String IdEspecieGarantia;
		public String IdEmpresa;
		public String CodigoPais;
		public String EsNominal;
		public String FolioExternoReferencia;
		public String TipoDocumentoIdentidad;
		public String TipoPersona;
		public String DocumentoIdentidadCliente;
		public String CodigoPortafolio;
		public String IdCuentaCustodioPortafolio;
		public String IdEstado;
		public String IdTipoCanalIngreso;
		public String IdMonedaSistema;
		public String IdMonedaAdicional;
		public String FechaFactorMonedaAdicional;
		public String ValorFactorMonedaAdicional;
		public String CodigoIFRS;
		public String IdConcepto;
		public String IdTipoOperacion;
		public String IdBolsa;
		public String IdSucursal;
		public String CodigoAgente;
		public String IdCondicionLiquidacion;
		public String FechaLiquidacion;
		public String EsIntraday;
		public String UsaGarantiaEspecifica;
		public String Glosa;
		public String NemotecnicoAnterior;
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
		public String Tamano;
		public String IdBoveda;
		public String CuentaBoveda;
		public String ISIN;
		public String FechaFactorMonedaInstrumento;
		public String ValorFactorMonedaInstrumento;
		public String Cantidad;
		public String CorteMinimo;
		public String ValorRescate;
		public String PrecioCompra;
		public String TasaCompra;
		public String MontoCompra;
		public String MontoCompraMonedaInstrumento;
		public String MontoCompraMonedaAdicional;
		
		public Long IdMovimientoCarteraEnTransito;
		public Long IdUsuarioIngreso;
		public Long IdPais;
		public Long IdCliente;
		public Long IdPortafolio;
		public Long IdAgente;
		public Long IdUsuario;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public Long IdInstrumento;
		public Long IdInstrumentoAnterior;
	}

	@Override
	public List<String> loadIntoDb(BasicDataSource jsJB, EntradaCargaGeneral in, List<Object> lines) {
		List<String> salida = new ArrayList<String>();

		Long idUsuarioDefecto = in.p_IdUsuario;
		Long idEmpresaDefecto = in.p_IdEmpresa;
		SQL sql = new SQL();

		try (Connection conn = jsJB.getConnection()){
			IdentificarLlaves identificarLlaves = new IdentificarLlavesBuilder(conn).setPaises().build();
			
			List<Long> idsMovimiento= Utils.getSecuencia(conn, "BSCL.Custodia.BD.Secuencias::IdMovimientoCarteraEnTransito", (long) lines.size());
			int linea = 0;
			List<CSVRecord> carterasCSV = (List<CSVRecord>) (Object) lines;
			for (CSVRecord record : carterasCSV) {
				try {
					EntidadMovimientoCartera movimiento = new EntidadMovimientoCartera();
					movimiento.IdMovimientoCarteraEnTransitoRelacionado = record.get("IdMovimientoCarteraEnTransitoRelacionado");
					movimiento.FechaIngresoRegistro = record.get("FechaIngresoRegistro");
					movimiento.FechaCertificacionRegistro = record.get("FechaCertificacionRegistro");
					movimiento.FechaAnulacionRegistro = record.get("FechaAnulacionRegistro");
					movimiento.Usuario = record.get("Usuario");
					movimiento.FechaOperacion = record.get("FechaOperacion");
					movimiento.FechaVencimientoOperacion = record.get("FechaVencimientoOperacion");
					movimiento.PlazoEnDiasOperacion = record.get("PlazoEnDiasOperacion");
					movimiento.FolioCustodia = record.get("FolioCustodia");
					movimiento.IdTipoComprobante = record.get("IdTipoComprobante");
					movimiento.FolioComprobante = record.get("FolioComprobante");
					movimiento.MueveCustodia = record.get("MueveCustodia");
					movimiento.OrigenCustodia = record.get("OrigenCustodia");
					movimiento.IdCustodiaOrigen = record.get("IdCustodiaOrigen");
					movimiento.IdTipoOperacionOrigen = record.get("IdTipoOperacionOrigen");
					movimiento.FolioCarteraGarantizada = record.get("FolioCarteraGarantizada");
					movimiento.IdInstrumentoCarteraGarantizada = record.get("IdInstrumentoCarteraGarantizada");
					movimiento.EsEspecifica = record.get("EsEspecifica");
					movimiento.IdContraparteGarantia = record.get("IdContraparteGarantia");
					movimiento.CuentaContraparteGarantia = record.get("CuentaContraparteGarantia");
					movimiento.IdOrigenGarantia = record.get("IdOrigenGarantia");
					movimiento.IdEspecieGarantia = record.get("IdEspecieGarantia");
					movimiento.IdEmpresa = record.get("IdEmpresa");
					movimiento.CodigoPais = record.get("CodigoPais");
					movimiento.EsNominal = record.get("EsNominal");
					movimiento.FolioExternoReferencia = record.get("FolioExternoReferencia");
					movimiento.TipoDocumentoIdentidad = record.get("TipoDocumentoIdentidad");
					movimiento.TipoPersona = record.get("TipoPersona");
					movimiento.DocumentoIdentidadCliente = record.get("DocumentoIdentidadCliente");
					movimiento.CodigoPortafolio = record.get("CodigoPortafolio");
					movimiento.IdCuentaCustodioPortafolio = record.get("IdCuentaCustodioPortafolio");
					movimiento.IdEstado = record.get("IdEstado");
					movimiento.IdTipoCanalIngreso = record.get("IdTipoCanalIngreso");
					movimiento.IdMonedaSistema = record.get("IdMonedaSistema");
					movimiento.IdMonedaAdicional = record.get("IdMonedaAdicional");
					movimiento.FechaFactorMonedaAdicional = record.get("FechaFactorMonedaAdicional");
					movimiento.ValorFactorMonedaAdicional = record.get("ValorFactorMonedaAdicional");
					movimiento.CodigoIFRS = record.get("CodigoIFRS");
					movimiento.IdConcepto = record.get("IdConcepto");
					movimiento.IdTipoOperacion = record.get("IdTipoOperacion");
					movimiento.IdBolsa = record.get("IdBolsa");
					movimiento.IdSucursal = record.get("IdSucursal");
					movimiento.CodigoAgente = record.get("CodigoAgente");
					movimiento.IdCondicionLiquidacion = record.get("IdCondicionLiquidacion");
					movimiento.FechaLiquidacion = record.get("FechaLiquidacion");
					movimiento.EsIntraday = record.get("EsIntraday");
					movimiento.UsaGarantiaEspecifica = record.get("UsaGarantiaEspecifica");
					movimiento.Glosa = record.get("Glosa");
					movimiento.NemotecnicoAnterior = record.get("NemotecnicoAnterior");
					movimiento.Nemotecnico = record.get("Nemotecnico");
					movimiento.IdEmisor = record.get("IdEmisor");
					movimiento.NemoSVS = record.get("NemoSVS");
					movimiento.IdTipoReajuste = record.get("IdTipoReajuste");
					movimiento.EsPagaderoEn = record.get("EsPagaderoEn");
					movimiento.FechaEmisionInstrumento = record.get("FechaEmisionInstrumento");
					movimiento.FechaVencimientoInstrumento = record.get("FechaVencimientoInstrumento");
					movimiento.EsFungible = record.get("EsFungible");
					movimiento.TieneCorteDeCupon = record.get("TieneCorteDeCupon");
					movimiento.IdMonedaInstrumento = record.get("IdMonedaInstrumento");
					movimiento.Tamano = record.get("Tamano");
					movimiento.IdBoveda = record.get("IdBoveda");
					movimiento.CuentaBoveda = record.get("CuentaBoveda");
					movimiento.ISIN = record.get("ISIN");
					movimiento.FechaFactorMonedaInstrumento = record.get("FechaFactorMonedaInstrumento");
					movimiento.ValorFactorMonedaInstrumento = record.get("ValorFactorMonedaInstrumento");
					movimiento.Cantidad = record.get("Cantidad");
					movimiento.CorteMinimo = record.get("CorteMinimo");
					movimiento.ValorRescate = record.get("ValorRescate");
					movimiento.PrecioCompra = record.get("PrecioCompra");
					movimiento.TasaCompra = record.get("TasaCompra");
					movimiento.MontoCompra = record.get("MontoCompra");
					movimiento.MontoCompraMonedaInstrumento = record.get("MontoCompraMonedaInstrumento");
					movimiento.MontoCompraMonedaAdicional = record.get("MontoCompraMonedaAdicional");
					
					//OBTENER CAMPOS INTERNOS
					movimiento.IdMovimientoCarteraEnTransito = idsMovimiento.get(linea);
					
					movimiento.IdUsuarioIngreso = idUsuarioDefecto;
					movimiento.IdPais = identificarLlaves.getPais(record.get("CodigoPais"));
					
					//obtener id cliente
					ConsultarDocumentoIdentidadCliente consDocCli = new ConsultarDocumentoIdentidadCliente();
					SalidaSocioNegocio idsSocioNegocio = consDocCli.logica(conn, movimiento.DocumentoIdentidadCliente, movimiento.TipoDocumentoIdentidad, "", idEmpresaDefecto);
					movimiento.IdCliente = idsSocioNegocio.IdCliente;
					
					//obtener id portafolio
					HashMap<String, Object> params = new HashMap<>();
					params.put("DocumentoIdentidadCliente", movimiento.DocumentoIdentidadCliente);
					params.put("TipoDocumentoIdentidad", movimiento.TipoDocumentoIdentidad);
					params.put("CodigoPortafolio", movimiento.CodigoPortafolio);
					List<Portafolio> portafolios = sql.query(conn, SQL_CONSULTA_PORTAFOLIO, params, Portafolio.class);
					movimiento.IdPortafolio = portafolios.get(0).IdPortafolio;
					
					//obtener codigo agente
					params = new HashMap<>();
					if(!record.get("CodigoAgente").equals("")) {
						params.put("CodigoAgente", record.get("CodigoAgente"));
						List<Agente> agentes = sql.query(conn, SQL_CONSULTA_AGENTE, params, Agente.class);
						movimiento.IdAgente = agentes.get(0).IdEmpleadoEjecutivo;
							
					}else {
						movimiento.IdAgente = (long) 0;
					} 

					//asignar id usuario
					movimiento.IdUsuario = idUsuarioDefecto;
					
					//obtener id instrumento
					params = new HashMap<>();
					if(!record.get("Nemotecnico").equals("")) {
						params.put("Nemotecnico", record.get("Nemotecnico"));
						List<Instrumento> instrumentos = sql.query(conn, SQL_CONSULTA_INSTRUMENTO, params, Instrumento.class);
						movimiento.IdClasificacionTipoInstrumento = instrumentos.get(0).IdClasificacionTipoInstrumento;//obtener con el nemo
						movimiento.IdTipoInstrumento = instrumentos.get(0).IdTipoInstrumento;//obtener con el nemo
						movimiento.IdInstrumento = instrumentos.get(0).IdInstrumento;//obtener con el nemo
						movimiento.Nemotecnico = record.get("Nemotecnico");
						movimiento.IdMonedaInstrumento = instrumentos.get(0).IdMoneda;
					}

					
					//obtener id instrumento anterior
					params = new HashMap<>();
					if(!record.get("NemotecnicoAnterior").equals("")) {
						params.put("Nemotecnico", record.get("NemotecnicoAnterior"));
						List<Instrumento> instrumentos = sql.query(conn, SQL_CONSULTA_INSTRUMENTO, params, Instrumento.class);
						movimiento.IdInstrumentoAnterior = instrumentos.get(0).IdInstrumento;
						movimiento.NemotecnicoAnterior = record.get("NemotecnicoAnterior");
					}else {
						movimiento.IdInstrumentoAnterior = new Long(0);
					}
					
					
					//Ingresar Cartera
					int[] res = sql.batch(conn, SQL_INSERT_MOVIMIENTO_CARTERA, Arrays.asList(movimiento), EntidadMovimientoCartera.class);
					for (int i : res) {
						if(i==1){
							salida.add(linea + " - MOVIMIENTO CARTERA A DOCUMENTO: " + movimiento.DocumentoIdentidadCliente+ ", INGRESADA CORRECTAMENTE");
						}else{
							salida.add(linea + " - MOVIMIENTO CARTERA A DOCUMENTO: " + movimiento.DocumentoIdentidadCliente + ", NO INGRESADA");
						}
					}
					
				} catch (Exception e) {
					e.printStackTrace();
					salida.add(linea + " - CARTERA A FOLIO: " + record.get("FolioSubyacente") + ", NO INGRESADA");
				}
				linea++;
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

	return salida;
	}

}
