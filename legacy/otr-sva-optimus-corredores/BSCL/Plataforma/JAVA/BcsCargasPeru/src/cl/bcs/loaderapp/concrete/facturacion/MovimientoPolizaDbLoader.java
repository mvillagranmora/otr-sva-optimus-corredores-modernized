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
import cl.bcs.loaderapp.servicios.ConsultarDocumentoIdentidadCliente;
import cl.bcs.loaderapp.servicios.ConsultarDocumentoIdentidadCliente.SalidaSocioNegocio;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class MovimientoPolizaDbLoader implements DbLoader {
	
	public String SQL_INGRESAR_MOVIMIENTO_POLIZA = loadResourceToString("cl/bcs/loaderapp/sql/facturacion/MovimientoPoliza_Ingresar.sql");
	
	public String SQL_CONSULTA_PORTAFOLIO = "SELECT \"IdPortafolio\" FROM \"BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio\" CCP INNER JOIN \"BSCL.SocioNegocio.BD::PER.DocumentoIdentidad\" DI ON CCP.\"IdPersona\" = DI.\"IdPersona\" WHERE DI.\"DocumentoIdentidad\" = :DocumentoIdentidadCliente AND DI.\"IdTipoDocumentoIdentidad\" = :TipoDocumentoIdentidad AND CCP.\"CodigoPortafolio\" = :CodigoPortafolio;";
	public static class Portafolio{
		public Long IdPortafolio;
	}
	
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
	
	public String SQL_CONSULTA_BROKER = "SELECT \"IdPersona\" FROM BSCL.\"BSCL.Plataforma.BD::PLA.BrokerExtranjero\" WHERE \"Codigo\" = :Codigo;";
	public String SQL_CONSULTA_BANCO = "SELECT \"IdPersona\" FROM \"BSCL.Plataforma.BD::PLA.BancoCustodio\" WHERE \"Codigo\" = :Codigo;";
	public static class Persona{
		public Long IdPersona;
	}
	
	public String SQL_CONSULTA_CLIENTE = "SELECT CTC.\"IdTipoCliente\", CTC.\"Nombre\" AS \"NombreTipoCliente\"  \r\n" + 
			"FROM \"BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente\" CC\r\n" + 
			"INNER JOIN \"BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarTipoCliente\" CTC ON CC.\"IdTipoCliente\" = CTC.\"IdTipoCliente\" AND CTC.\"IdEmpresa\" = CC.\"IdEmpresa\" \r\n" + 
			"WHERE CC.\"IdEmpresa\" = :IdEmpresa AND CC.\"IdCliente\" = :IdCliente;";
	public static class Cliente{
		public String IdTipoCliente;
		public String NombreTipoCliente;
	}
	
	public String SQL_CONSULTA_INDICADOR_FACTURACION = "SELECT \"ValorIndicador\" FROM \"BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio\" WHERE \"GrupoIndicador\" = 'GRUPOFACTURACION' AND \"CodigoTipoIndicador\" = 'FAC_MODFAC' AND \"IdCliente\" = :IdCliente";
	public static class IndicadorFacturacion{
		public String ValorIndicador;
	}

	public static class EntidadMovimientoPoliza{
		public String IdConceptoMovimiento;
		public String SecuenciaAgrupacion;
		public String IdMoneda;
		public String IdTipoComprobante;
		public String IdComprobante;
		public String IdTipoOperacion;
		public String IdBoveda;
		public String IdCondicionLiquidacion;
		public String FechaLiquidacion;
		public String IdBolsa;
		public String IdTipoReajuste;
		public String IdTipoConvenio;
		public String IdOrden;
		public String IdAsignacion;
		public String IdTipoCanalIngreso;
		public String FechaOrden;
		public String IdTransaccion;
		public String FolioTransaccion;
		public String FechaTransaccion;
		public String FechaVencimiento;
		public String FechaParidad;
		public String ValorParidad;
		public String ValorCaptacionMonedaOrigen;
		public String FolioComprobante;
		public String Tasa;
		public String Cantidad;
		public String Precio;
		public String Monto;
		public String MontoDescuento;
		public String MontoInteres;
		public String Glosa;
		public String ValorRescate;
		public String TasaAgente;
		public String Spread;
		public String DiasPlazo;
		public String CodigoCanalIngreso;
		public String DocumentoIdentidad;
		public String TipoDocumentoIdentidad;
		public String TipoPersona;
		public String CodigoPortafolio;
		public String FolioCartera;
		public String FechaEmision;
		public String TasaEmision;
		public String EsComprobIndividual;
		public String EsCuentaPropia;
		public String EsPrecioPrima;
		public String EsEnRueda;
		public String EsRuteo;
		public String EsIngresoAutomatico;
		public String EsAPlazo;
		public String MontoAPlazo;
		public String PrecioAPlazo;
		public String IdMonedaSubyacente;
		public String IdMonedaLiquidacion;
		public String FechaTipoCambio;
		public String ValorTipoCambio;
		public String EsVentaCorta;
		public String ActualizarCustodia;
		public String MontoMovimientoComprobante;
		public String MontoLiquidacion;
		public String ActualizarCustodiaRelacionado;
		public String EsFungible;
		public String IdFormaCustodia;
		public String ActualizarCuentaInversion;
		public String ActualizarTesoreria;
		public String NemotecnicoRelacionado;
		public String IdAsignacionRef;
		public String IdComprobanteReferencia;
		public String FolioComprobanteReferencia;
		public String IdMovimientoRelacionado;
		public String IdMovimientoOdRelacionado;
		public String IdTipoComprobanteReferencia;
		public String FechaComprobanteReferencia;
		public String IdMesa;
		public String IndicadorOrdenDirecta;
		public String IdMonedaSpot;
		public String NemoSVS;
		public String EsClienteFinal;
		public String CodigoBroker;
		public String LiquidaBancoCustodio;
		public String CodigoBancoCustodio;
		public String CodigoBancoLiquidador;
		public String IdEstadoCavali;
		public String ReajustesVAC;
		public String Intereses;
		public String MercadoSMV;
		public String ModalidadSMV;
		public String CodigoAgenteDigitador;
		public String CodigoAgenteEjecutivo;
		
		//internos
		public Long IdMovimientoComprobante;
		public Long IdEmpresa;
		public Long IdCliente;
		public Long IdPortafolio;
		public Long IdInstrumento;
		public Long IdInstrumentoSubyacente;
		public String IdTipoInstrumento;
		public String IdTipoInstrumentoSubyacente;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoProducto;
		public String IdEstadoFacturacion;
		public String NombreLargoCliente;
		public String DireccionFacturacion;
		public String CodigoTipoProducto;
		public String CodigoTipoOperacion;
		public String CodigoBolsa;
		public String NombreTipoInstrumento;
		public String NombreTipoInstrumentoSubyacente;
		public String NombreClasificacionTipoInstrumento;
		public String NemotecnicoInstrumento;
		public String NemotecnicoSubyacente;
		public String RazonSocialEmisor;
		public String FechaIngresoRegistro;
		public String FechaSistema;
		public Long IdCarga;
		public Long SecMovimientoFacturarMigracion;
		public String FechaMovimientoComprobante;
		public String IdMonedaMovimientoComprobante;
		public String IdEmisor;
		public Long IdInstrumentoRelacionado;
		public String IdTipoInstrumentoRelacionado;
		public String IdClasificacionTipoInstrumentoRelacionado;
		public String NombreTipoInstrumentoRelacionado;
		public String NombreClasificacionTipoInstrumentoRelacionado;
		public String ModPrefFact;
		public Long IdSucursalEmpleadoDigitador;
		public String NombreSucursalEmpleadoEjecutivoPortafolio;
		public String NombreEmpleadoDigitador;
		public Long IdEmpleadoDigitador;
		public Long IdSucursalEmpleadoEjecutivoPortafolio;
		public Long IdEmpleadoEjecutivoPortafolio;
		public String NombreEmpleadoEjecutivoPortafolio;
		public String NombreSucursalEmpleadoDigitador;
		public String IdTipoCliente;
		public String NombreTipoCliente;
		public Long IdPersonaBroker;
		public Long IdPersonaBancoCustodio;
		public Long IdPersonaBancoLiquidador;
		public Long IdUsuario;

	}
	
	@Override
	public List<String> loadIntoDb(BasicDataSource jsJB, EntradaCargaGeneral in, List<Object> lines) {
		List<String> salida = new ArrayList<String>();
		SQL sql = new SQL();
		
		List<CSVRecord> detallesCsv = (List<CSVRecord>) (Object) lines;
		try(Connection conn = jsJB.getConnection()){
			int linea = 0;
			List<Long> idsMovimientoPolizas = Utils.getSecuencia(conn, "BSCL.Facturacion.BD.Secuencias::MovimientoComprobante", (long) lines.size());
		
			for(CSVRecord record : detallesCsv) {
				EntidadMovimientoPoliza movimientoPoliza = new EntidadMovimientoPoliza();
				movimientoPoliza.IdConceptoMovimiento = record.get("IdConceptoMovimiento");
				movimientoPoliza.SecuenciaAgrupacion = record.get("SecuenciaAgrupacion");
				movimientoPoliza.IdMoneda = record.get("IdMoneda");
				movimientoPoliza.IdTipoComprobante = record.get("IdTipoComprobante");
				movimientoPoliza.IdComprobante = record.get("IdComprobante");
				movimientoPoliza.IdTipoOperacion = record.get("IdTipoOperacion");
				movimientoPoliza.IdBoveda = record.get("IdBoveda");
				movimientoPoliza.IdCondicionLiquidacion = record.get("IdCondicionLiquidacion");
				movimientoPoliza.FechaLiquidacion = record.get("FechaLiquidacion");
				movimientoPoliza.IdBolsa = record.get("IdBolsa");
				movimientoPoliza.IdTipoReajuste = record.get("IdTipoReajuste");
				movimientoPoliza.IdTipoConvenio = record.get("IdTipoConvenio");
				movimientoPoliza.IdOrden = record.get("IdOrden");
				movimientoPoliza.IdAsignacion = record.get("IdAsignacion");
				movimientoPoliza.IdTipoCanalIngreso = record.get("IdTipoCanalIngreso");
				movimientoPoliza.FechaOrden = record.get("FechaOrden");
				movimientoPoliza.IdTransaccion = record.get("IdTransaccion");
				movimientoPoliza.FolioTransaccion = record.get("FolioTransaccion");
				movimientoPoliza.FechaTransaccion = record.get("FechaTransaccion");
				movimientoPoliza.FechaVencimiento = record.get("FechaVencimiento");
				movimientoPoliza.FechaParidad = record.get("FechaParidad");
				movimientoPoliza.ValorParidad = record.get("ValorParidad");
				movimientoPoliza.ValorCaptacionMonedaOrigen = record.get("ValorCaptacionMonedaOrigen");
				movimientoPoliza.FolioComprobante = record.get("FolioComprobante");
				movimientoPoliza.Tasa = record.get("Tasa");
				movimientoPoliza.Cantidad = record.get("Cantidad");
				movimientoPoliza.Precio = record.get("Precio");
				movimientoPoliza.Monto = record.get("Monto");
				movimientoPoliza.MontoDescuento = record.get("MontoDescuento");
				movimientoPoliza.MontoInteres = record.get("MontoInteres");
				movimientoPoliza.Glosa = record.get("Glosa");
				movimientoPoliza.ValorRescate = record.get("ValorRescate");
				movimientoPoliza.TasaAgente = record.get("TasaAgente");
				movimientoPoliza.Spread = record.get("Spread");
				movimientoPoliza.DiasPlazo = record.get("DiasPlazo");
				movimientoPoliza.CodigoCanalIngreso = record.get("CodigoCanalIngreso");
				movimientoPoliza.DocumentoIdentidad = record.get("DocumentoIdentidad");
				movimientoPoliza.TipoDocumentoIdentidad = record.get("TipoDocumentoIdentidad");
				movimientoPoliza.TipoPersona = record.get("TipoPersona");
				movimientoPoliza.CodigoPortafolio = record.get("CodigoPortafolio");
				movimientoPoliza.FolioCartera = record.get("FolioCartera");
				movimientoPoliza.FechaEmision = record.get("FechaEmision");
				movimientoPoliza.TasaEmision = record.get("TasaEmision");
				movimientoPoliza.EsComprobIndividual = record.get("EsComprobIndividual");
				movimientoPoliza.EsCuentaPropia = record.get("EsCuentaPropia");
				movimientoPoliza.EsPrecioPrima = record.get("EsPrecioPrima");
				movimientoPoliza.EsEnRueda = record.get("EsEnRueda");
				movimientoPoliza.EsRuteo = record.get("EsRuteo");
				movimientoPoliza.EsIngresoAutomatico = record.get("EsIngresoAutomatico");
				movimientoPoliza.EsAPlazo = record.get("EsAPlazo");
				movimientoPoliza.MontoAPlazo = record.get("MontoAPlazo");
				movimientoPoliza.PrecioAPlazo = record.get("PrecioAPlazo");
				movimientoPoliza.IdMonedaSubyacente = record.get("IdMonedaSubyacente");
				movimientoPoliza.IdMonedaLiquidacion = record.get("IdMonedaLiquidacion");
				movimientoPoliza.FechaTipoCambio = record.get("FechaTipoCambio");
				movimientoPoliza.ValorTipoCambio = record.get("ValorTipoCambio");
				movimientoPoliza.EsVentaCorta = record.get("EsVentaCorta");
				movimientoPoliza.ActualizarCustodia = record.get("ActualizarCustodia");
				movimientoPoliza.MontoMovimientoComprobante = record.get("MontoMovimientoComprobante");
				movimientoPoliza.MontoLiquidacion = record.get("MontoLiquidacion");
				movimientoPoliza.ActualizarCustodiaRelacionado = record.get("ActualizarCustodiaRelacionado");
				movimientoPoliza.EsFungible = record.get("EsFungible");
				movimientoPoliza.IdFormaCustodia = record.get("IdFormaCustodia");
				movimientoPoliza.ActualizarCuentaInversion = record.get("ActualizarCuentaInversion");
				movimientoPoliza.ActualizarTesoreria = record.get("ActualizarTesoreria");
				movimientoPoliza.NemotecnicoRelacionado = record.get("NemotecnicoRelacionado");
				movimientoPoliza.IdAsignacionRef = record.get("IdAsignacionRef");
				movimientoPoliza.IdComprobanteReferencia = record.get("IdComprobanteReferencia");
				movimientoPoliza.FolioComprobanteReferencia = record.get("FolioComprobanteReferencia");
				movimientoPoliza.IdMovimientoRelacionado = record.get("IdMovimientoRelacionado");
				movimientoPoliza.IdMovimientoOdRelacionado = record.get("IdMovimientoOdRelacionado");
				movimientoPoliza.IdTipoComprobanteReferencia = record.get("IdTipoComprobanteReferencia");
				movimientoPoliza.FechaComprobanteReferencia = record.get("FechaComprobanteReferencia");
				movimientoPoliza.IdMesa = record.get("IdMesa");
				movimientoPoliza.IndicadorOrdenDirecta = record.get("IndicadorOrdenDirecta");
				movimientoPoliza.IdMonedaSpot = record.get("IdMonedaSpot");
				movimientoPoliza.NemoSVS = record.get("NemoSVS");
				movimientoPoliza.EsClienteFinal = record.get("EsClienteFinal");
				movimientoPoliza.CodigoBroker = record.get("CodigoBroker");
				movimientoPoliza.LiquidaBancoCustodio = record.get("LiquidaBancoCustodio");
				movimientoPoliza.CodigoBancoCustodio = record.get("CodigoBancoCustodio");
				movimientoPoliza.CodigoBancoLiquidador = record.get("CodigoBancoLiquidador");
				movimientoPoliza.IdEstadoCavali = record.get("IdEstadoCavali");
				movimientoPoliza.ReajustesVAC = record.get("ReajustesVAC");
				movimientoPoliza.Intereses = record.get("Intereses");
				movimientoPoliza.MercadoSMV = record.get("MercadoSMV");
				movimientoPoliza.ModalidadSMV = record.get("ModalidadSMV");
				
				movimientoPoliza.CodigoAgenteDigitador = record.get("CodigoAgenteDigitador");
				movimientoPoliza.CodigoAgenteEjecutivo= record.get("CodigoAgenteEjecutivo");
				
				//INTERNOS
				movimientoPoliza.IdMovimientoComprobante = idsMovimientoPolizas.get(linea);
				movimientoPoliza.IdEmpresa = in.p_IdEmpresa;
				movimientoPoliza.IdUsuario = in.p_IdUsuario;
				
				//obtener id cliente
				ConsultarDocumentoIdentidadCliente consultarDocumentoIdentidad = new ConsultarDocumentoIdentidadCliente();
				SalidaSocioNegocio socioNegocio = consultarDocumentoIdentidad.logica(conn, movimientoPoliza.DocumentoIdentidad, movimientoPoliza.TipoDocumentoIdentidad, movimientoPoliza.TipoPersona, in.p_IdEmpresa);
				movimientoPoliza.IdCliente = socioNegocio.IdCliente;
				
				//obtener datos cliente
				HashMap<String, Object> params = new HashMap<>();
				if(movimientoPoliza.IdCliente!=null){
					params.put("IdCliente", movimientoPoliza.IdCliente);
					params.put("IdEmpresa", in.p_IdEmpresa);
					List<Cliente> clientes = sql.query(conn, SQL_CONSULTA_CLIENTE, params, Cliente.class);
					movimientoPoliza.IdTipoCliente = clientes.get(0).IdTipoCliente;
					movimientoPoliza.NombreTipoCliente = clientes.get(0).NombreTipoCliente;
				}
				
				//obtener datos cliente
				params = new HashMap<>();
				if(movimientoPoliza.IdCliente!=null){
					params.put("IdCliente", movimientoPoliza.IdCliente);
					List<IndicadorFacturacion> indicador = sql.query(conn, SQL_CONSULTA_INDICADOR_FACTURACION, params, IndicadorFacturacion.class);
					movimientoPoliza.ModPrefFact = indicador.get(0).ValorIndicador;//GRUPOFACTURACION//FAC_MODFAC
				}
				
				//obtener id portafolio
				params = new HashMap<>();
				params.put("DocumentoIdentidadCliente", movimientoPoliza.DocumentoIdentidad);
				params.put("TipoDocumentoIdentidad", movimientoPoliza.TipoDocumentoIdentidad);
				params.put("CodigoPortafolio", movimientoPoliza.CodigoPortafolio);
				List<Portafolio> portafolios = sql.query(conn, SQL_CONSULTA_PORTAFOLIO, params, Portafolio.class);
				movimientoPoliza.IdPortafolio = portafolios.get(0).IdPortafolio;
				
				//obtener IdInstrumento
				params = new HashMap<>();
				if(!record.get("NemotecnicoInstrumento").equals("")) {
					params.put("Nemotecnico", record.get("NemotecnicoInstrumento"));
					List<Instrumento> instrumentos = sql.query(conn, SQL_CONSULTA_INSTRUMENTO, params, Instrumento.class);
					movimientoPoliza.IdClasificacionTipoInstrumento = instrumentos.get(0).IdClasificacionTipoInstrumento;//obtener con el nemo
					movimientoPoliza.IdTipoInstrumento = instrumentos.get(0).IdTipoInstrumento;//obtener con el nemo
					movimientoPoliza.IdInstrumento = instrumentos.get(0).IdInstrumento;//obtener con el nemo
					movimientoPoliza.NemotecnicoInstrumento = record.get("NemotecnicoInstrumento");
					movimientoPoliza.IdTipoProducto = instrumentos.get(0).IdTipoProducto;
					movimientoPoliza.CodigoTipoProducto = instrumentos.get(0).CodigoTipoProducto;
					movimientoPoliza.CodigoTipoOperacion = instrumentos.get(0).CodigoTipoOperacion;
					movimientoPoliza.NombreTipoInstrumento = instrumentos.get(0).NombreTipoInstrumento;
					movimientoPoliza.NombreClasificacionTipoInstrumento = instrumentos.get(0).NombreClasificacionTipoInstrumento;
					movimientoPoliza.IdEmisor = instrumentos.get(0).IdEmisor;
				}
				//obtener IdInstrumentoSubyacente
				params = new HashMap<>();
				if(!record.get("NemotecnicoSubyacente").equals("")) {
					params.put("Nemotecnico", record.get("NemotecnicoSubyacente"));
					List<Instrumento> instrumentos = sql.query(conn, SQL_CONSULTA_INSTRUMENTO, params, Instrumento.class);
					movimientoPoliza.IdTipoInstrumentoSubyacente = instrumentos.get(0).IdTipoInstrumento;//obtener con el nemo
					movimientoPoliza.IdInstrumentoSubyacente = instrumentos.get(0).IdInstrumento;//obtener con el nemo
					movimientoPoliza.NemotecnicoSubyacente= record.get("NemotecnicoSubyacente");
					movimientoPoliza.NombreTipoInstrumentoSubyacente = instrumentos.get(0).NombreTipoInstrumento;
				}
				//id Estado facturacion??
				movimientoPoliza.IdEstadoFacturacion = "ING";
				movimientoPoliza.FechaIngresoRegistro = null;
				movimientoPoliza.FechaSistema= null;
				movimientoPoliza.IdCarga = (long) 999;
				movimientoPoliza.SecMovimientoFacturarMigracion = (long) 0;
				movimientoPoliza.FechaMovimientoComprobante = null;

				movimientoPoliza.IdMonedaMovimientoComprobante = movimientoPoliza.IdMoneda;
				
				//obtener IdInstrumentoRelacionado
				params = new HashMap<>();
				if(!record.get("NemotecnicoRelacionado").equals("")) {
					params.put("Nemotecnico", record.get("NemotecnicoRelacionado"));
					List<Instrumento> instrumentos = sql.query(conn, SQL_CONSULTA_INSTRUMENTO, params, Instrumento.class);
					movimientoPoliza.IdClasificacionTipoInstrumentoRelacionado = instrumentos.get(0).IdClasificacionTipoInstrumento;//obtener con el nemo
					movimientoPoliza.IdTipoInstrumentoRelacionado = instrumentos.get(0).IdTipoInstrumento;//obtener con el nemo
					movimientoPoliza.IdInstrumentoRelacionado = instrumentos.get(0).IdInstrumento;//obtener con el nemo
					movimientoPoliza.NemotecnicoRelacionado = record.get("NemotecnicoRelacionado");
					movimientoPoliza.NombreTipoInstrumentoRelacionado = instrumentos.get(0).NombreTipoInstrumento;
					movimientoPoliza.NombreClasificacionTipoInstrumentoRelacionado = instrumentos.get(0).NombreClasificacionTipoInstrumento;
				}

				//obtener IdEmpleadoDigitador
				params = new HashMap<>();
				if(!record.get("CodigoAgenteDigitador").equals("")) {
					params.put("CodigoAgente", record.get("CodigoAgenteDigitador"));
					List<Agente> agentes = sql.query(conn, SQL_CONSULTA_AGENTE, params, Agente.class);
					movimientoPoliza.IdSucursalEmpleadoDigitador = agentes.get(0).IdSucursal;
					movimientoPoliza.NombreSucursalEmpleadoDigitador = agentes.get(0).NombreSucursal;
					movimientoPoliza.NombreEmpleadoDigitador = agentes.get(0).NombreCompleto;
					movimientoPoliza.IdEmpleadoDigitador = agentes.get(0).IdEmpleadoEjecutivo;
				}
				
				//obtener IdEmpleadoDigitador
				params = new HashMap<>();
				if(!record.get("CodigoAgenteEjecutivo").equals("")) {
					params.put("CodigoAgente", record.get("CodigoAgenteEjecutivo"));
					List<Agente> agentes = sql.query(conn, SQL_CONSULTA_AGENTE, params, Agente.class);
					movimientoPoliza.NombreSucursalEmpleadoEjecutivoPortafolio = agentes.get(0).NombreSucursal;
					movimientoPoliza.IdSucursalEmpleadoEjecutivoPortafolio = agentes.get(0).IdSucursal;
					movimientoPoliza.IdEmpleadoEjecutivoPortafolio = agentes.get(0).IdEmpleadoEjecutivo;
					movimientoPoliza.NombreEmpleadoEjecutivoPortafolio = agentes.get(0).NombreCompleto;
				}

				//obtener CodigoBroker
				params = new HashMap<>();
				if(!record.get("CodigoBroker").equals("")) {
					params.put("Codigo", record.get("CodigoBroker"));
					List<Persona> brokers = sql.query(conn, SQL_CONSULTA_BROKER, params, Persona.class);
					movimientoPoliza.IdPersonaBroker = brokers.get(0).IdPersona;
				}
				
				//obtener IdPersonaBancoCustodio
				params = new HashMap<>();
				if(!record.get("CodigoBancoCustodio").equals("")) {
					params.put("Codigo", record.get("CodigoBancoCustodio"));
					List<Persona> brokers = sql.query(conn, SQL_CONSULTA_BANCO, params, Persona.class);
					movimientoPoliza.IdPersonaBancoCustodio = (brokers.size()>0) ? brokers.get(0).IdPersona : (long) 0;
				}
				
				//obtener IdPersonaBancoCustodio
				params = new HashMap<>();
				if(!record.get("CodigoBancoLiquidador").equals("")) {
					params.put("Codigo", record.get("CodigoBancoLiquidador"));
					List<Persona> brokers = sql.query(conn, SQL_CONSULTA_BANCO, params, Persona.class);
					movimientoPoliza.IdPersonaBancoLiquidador = (brokers.size()>0) ? brokers.get(0).IdPersona : (long) 0;
				}
				
				
				//Ingresar Cartera
				int[] res = sql.batch(conn, SQL_INGRESAR_MOVIMIENTO_POLIZA, Arrays.asList(movimientoPoliza), EntidadMovimientoPoliza.class);
				for (int i : res) {
					if(i==1){
						salida.add(linea + " - Movimiento Poliza A FOLIO: " + movimientoPoliza.FolioCartera + ", INGRESADA CORRECTAMENTE");
					}else{
						salida.add(linea + " - Movimiento Poliza A FOLIO: " + movimientoPoliza.FolioCartera + ", NO INGRESADA");
					}
				}
				
				
				
				linea++;
			}
		}catch (Exception e) {
			salida.add("ERROR AL OBTENER CONEXION");
			e.printStackTrace();
		}
		return salida;
	}

}
