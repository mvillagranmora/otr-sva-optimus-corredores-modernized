package cl.bcs.custodia.prt;

import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.utils.GestionMovimientoCarteraEnTransito;
import cl.bcs.custodia.utils.GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar;
import cl.bcs.custodia.utils.IngresoCustodia;
import cl.bcs.custodia.utils.IngresoCustodia.IngresoCustodiaSalida;
import cl.bcs.facturacion.CrearMovimientoComprobante;
import cl.bcs.facturacion.CrearMovimientoComprobante.CrearMovimientoComprobante_Entrada;
import cl.bcs.facturacion.CrearMovimientoComprobante.CrearMovimientoComprobante_Salida;
import cl.bcs.facturacion.CrearMovimientoComprobante.MovimientoComprobante_Entrada;
import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.bck.ConsultaDataCorredoraBlockChain;
import cl.bcs.socionegocio.bck.ContratoEsBlockChainFirmado;
import cl.bcs.socionegocio.bck.ContratoEsBlockChainFirmado.ConsultaEstadoCntBC;
import cl.bcs.socionegocio.bck.ContratoEsBlockChainFirmado.ContratoEsBlockChainFirmado_Entrada;
import cl.bcs.socionegocio.bck.ContratoEsBlockChainFirmado.ContratoEsBlockChainFirmado_Salida;
import cl.bcs.socionegocio.bck.ContratoEsBlockChainFirmado.TipoDocumento;
import cl.bcs.socionegocio.bck.IngresoPrestamosBlockchain;
import cl.bcs.socionegocio.bck.IngresoPrestamosBlockchain.IngresoPrestamosBlockchain_Entrada;
import cl.bcs.socionegocio.bck.IngresoPrestamosBlockchain.IngresoPrestamosBlockchain_Salida;
import cl.bcs.socionegocio.pojo.ConstantesBCK;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearPrestamo implements JavaCall {

	// Log
	private final Log log = new Log("CUS.CrearPrestamo");

	// Queries
	private final String SQL_SELECT_TIPO_PRESTAMO 				= loadResourceToString("cl/bcs/custodia/sql/prt/CrearPrestamo_SelectTipoPrestamo.sql");
	private final String SQL_SELECT_INDICADORES_CLIENTE 		= loadResourceToString("cl/bcs/custodia/sql/prt/CrearPrestamo_SelectIndicadoresCliente.sql");
	private final String SQL_SELECT_CONSULTAR_INSTRUMENTO 		= loadResourceToString("cl/bcs/custodia/sql/prt/CrearPrestamo_SelectInstrumento.sql");
	private final String SQL_SELECT_CONDICION_DE_LIQUIDACION	= loadResourceToString("cl/bcs/custodia/sql/prt/CrearPrestamo_SelectCondicionDeLiquidacion.sql");
	private final String SQL_SELECT_ESCONTRATOBLOCKCHAIN 		= loadResourceToString("cl/bcs/socionegocio/bck/sql/ContratoEsBlockChainFirmado_SelectTipoDocumento.sql");
	private final String SQL_INSERT_LOG_PROCESO_BLOCKCHAIN 		= loadResourceToString("cl/bcs/socionegocio/bck/sql/ProcesaLogBlockChain_InsertLogProcesoPrestamoBlockChain.sql");
	
	// -------------------------------------------------
	// CLASES DE ENTRADA
	// -------------------------------------------------
	public static class CrearPrestamo_Entrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio = "N";
		public Long p_IdEmpleadoDigitador;
		public Long p_IdClienteSolicita;
		public Long p_IdPortafolioSolicita;
		public String p_IdBolsa;
		public String p_FechaMovimiento = "0001-01-01";
		public String p_FechaVencimiento = "0001-01-01";
		public Long p_IdInstrumentoPrestamo;
		public Long p_IdInstrumento; // IdInstrumento Subyacente
		public String p_Nemo;
		public BigDecimal p_Cantidad;
		public BigDecimal p_Precio;
		public BigDecimal p_TasaInstrumento;
		public BigDecimal p_Monto;
		public BigDecimal p_MontoPrima;
		public BigDecimal p_TasaPrima;
		public BigDecimal p_MontoAPlazo;
		public String p_ConGarantiaEspecifica = "N";
		public String p_FolioEntidad;
		public String p_FolioTransaccion;
		public String p_CobraPrimaAlInicio;
		public Long p_IdAgente;
		public Long p_IdSucursal;
		public Long p_PlazoEnDias;
		public String p_esNominal = "N"; // S: Es custodia externa N: Custodia propia
		public String p_IdTipoCanalIngreso; // Ej.: AGE
		public String p_IdMonedaSistema;
		public String p_IdMonedaInstrumento;
		public String p_IdMercado;
		public String p_EsFungible;
		public String p_TipoCustodia = "";
		public String p_TipoGarantia = "";
		public String p_TipoPrestamo_BChain;
		public String p_IdFolioSistemaExterno = ""; // si este valor viene, no hay que ir a BlockChain a crear Pedido

		public String           p_IdCondicionLiquidacion = "";
		public String           p_FechaLiquidacion = "";
		

		
		public String toString() {
			return Utils.printJson(this, false);
		}
	} 
	
	
	
	//-------------------------------------------------
	// CLASES DE SALIDA 
	//-------------------------------------------------	
	public static class CrearPrestamo_Salida {	
		public List<MensajeSalida>			P_OUT_MENSAJES;
		public Long							P_OUT_ERRORESCONTADOR;		
		public Long							P_OUT_IDCARTERA;
		public Long							P_OUT_FOLIOCARTERA;
		public Long							P_OUT_FOLIOSUBYACENTE;
		public String						P_OUT_COBRAPRIMAALINICIO;
		
		public CrearPrestamo_Salida() {
			this.P_OUT_MENSAJES	 =  new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	
	
	//-------------------------------------------------
	// CLASES UTILITARIAS 
	//-----------------------------------------------
	

	public static class TipoPrestamo {
		public String CobraPrimaAlInicio; // S/N
		public String IdTipoPrestamo; // PRTVTC /PRVCRF/ PRTADM / PRTFIN
		public String IdBovedaDefecto;
		public String IdEmisor;
		public String TipoPrestamoSistemaExterno;
		public String IdClasificacionTipoInstrumento;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class IndicadorCliente {
		public String CodigoTipoIndicador;
		public String NombreTipoIndicador;
		public String ValorIndicador;
		public String MensajeIndicador;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class Instrumento {
		public String IdMoneda;
		public String IdTipoInstrumento;
		public String IdTipoReajuste;
		public String IdEmisor;
		public java.sql.Date FechaEmision;
		public java.sql.Date FechaVencimiento;
		public BigDecimal CorteMinimo;
		public String Nemotecnico;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	

	public static class LogProcesoPrestamoBlockChain extends BcsPojo {
		public Long 	IdEmpresa;
		public Long 	FolioCartera;
		public String 	FechaProceso;
		public String 	Descripcion;
		public String 	OrigenProcesoClase;
		public String 	OrigenProcesoMetodo;
		public String 	CreadorParametro;
		
		public LogProcesoPrestamoBlockChain(Long idEmpresa, Long folioCartera, String fechaProceso, String descripcion, String origenProcesoClase, String origenProcesoMetodo, String creadoPor) {
			this.IdEmpresa 				= idEmpresa;
			this.FolioCartera 			= folioCartera;
			this.FechaProceso 			= fechaProceso;
			this.Descripcion 			= descripcion;
			this.OrigenProcesoClase 	= origenProcesoClase;
			this.OrigenProcesoMetodo 	= origenProcesoMetodo;
			this.CreadorParametro 		= creadoPor;
		}
	}
	
	public static class SelectCondicionDeLiquidacion extends BcsPojo {
		public String	IdCondicionDeLiquidacion;
		public String	Codigo;
		public String	Nombre;
		public Long		PlazoEnDias;
	}
	
	public static class InstrumentoRF {
		public Long IdInstrumento;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}


	// -------------------------------------------------
	// LOGICA DEL SERVICIO
	// -------------------------------------------------
	public CrearPrestamo_Salida logica(Connection con, CrearPrestamo_Entrada entrada) throws Exception {
		SQL sql 									= new SQL();
		CrearPrestamo_Salida salida 				= new CrearPrestamo_Salida();
		List<Long> transitosPorCertificar 			= new ArrayList<>();
		List<ObjMovimientosAActualizar> transitosOK = new ArrayList<>();
		SimpleDateFormat format 					= new SimpleDateFormat("yyyy-MM-dd");

		String fechaSistema 				= ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		String prestamosRentaFija			= ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "CUS.PrestamosRentaFija");
		String monedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		Long FolioComprobanteFacturacion 	= 0L;
		Long FolioCustodia 					= 0L;
		String EsBlockChain 				= "N";
		String ContratoFirmado 				= "N";
		
		ConsultaDataCorredoraBlockChain consultaDataCorredora 													= new ConsultaDataCorredoraBlockChain();
		ConsultaDataCorredoraBlockChain.ConsultaDataCorredoraBlockChain_Entrada entradaConsultaDataCorredora 	= new ConsultaDataCorredoraBlockChain.ConsultaDataCorredoraBlockChain_Entrada();
		entradaConsultaDataCorredora.p_IdEmpresa = entrada.p_IdEmpresa;
		ConsultaDataCorredoraBlockChain.ConsultaDataCorredoraBlockChain_Salida salidaConsultaDataCorredora = consultaDataCorredora.logica(con, entradaConsultaDataCorredora);
		
		log.write("== INICIO EJECUCION CREAR PRESTAMO ==");
		
		// ---------------------------------------------------------------------------------------------
		// OBTIENE DATOS TIPO PRESTAMO
		HashMap<String, Object> paramsTipPres = new HashMap<String, Object>();
		paramsTipPres.put("IdInstrumentoPrestamo", entrada.p_IdInstrumentoPrestamo);
		List<TipoPrestamo> tipoPrestamo = sql.query(con, SQL_SELECT_TIPO_PRESTAMO, paramsTipPres, TipoPrestamo.class);

		if (tipoPrestamo.size() == 0) {
			log.write("NO SE ENCONTRARON DATOS PARA TIPO PRESTAMO");
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "NO_SE_ENCONTRARON_DATOS_PARA_TIPO_PRESTAMO", null, con, entrada.p_IdEmpresa));
			return salida;
		} else {
			salida.P_OUT_COBRAPRIMAALINICIO = tipoPrestamo.get(0).CobraPrimaAlInicio;
			log.write("DATOS TIPO PRESTAMO > " + tipoPrestamo);
		}
		
		// ---------------------------------------------------------------------------------------------
		// VALIDA MERCADO RENTA FIJA HABILITADO PARA OPERAR
		
		if ( tipoPrestamo.get(0).IdClasificacionTipoInstrumento.equals("RF") && prestamosRentaFija.equals("N") ) {
			log.write("PRESTAMOS DE RENTA FIJA NO SE ENCUENTRAN HABILITADO");
			salida.P_OUT_MENSAJES.add( new MensajeSalida( 0L, MensajeSalida.MENSAJE_ERROR, "PRESTAMOS_MERCADO_RENTA_FIJA_DESHABILITADO", new HashMap<String, String>(), con, entrada.p_IdEmpresa ) );
			return salida;
		}

		// ---------------------------------------------------------------------------------------------
		// VALIDA SI CUMPLE REQUISITOS BASICOS
				
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, entrada.p_IdEmpresa, entrada.p_IdUsuario,entrada.p_IdModulo);
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			log.write("NO CUMPLE REQUISITOS BASICOS");
			salida.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			return salida;
		}

		// ---------------------------------------------------------------------------------------------
		// VALIDAR QUE EL CONTRATO ESTE CONFIGURADO COMO BLOCKCHAIN VALIDAR QUE TENGA FIRMADO CONTRATO
		if ( entrada.p_IdFolioSistemaExterno.equals("")
				|| ( entrada.p_IdFolioSistemaExterno.equals("") && tipoPrestamo.get( 0 ).IdTipoPrestamo.equalsIgnoreCase( ConstantesBCK.PRESTAMO_TIPO_VENTA_CORTA_RENTA_FIJA ) && prestamosRentaFija.equalsIgnoreCase( ConstantesBCK.SI ) ) ) {
			
			ContratoEsBlockChainFirmado ContratoEsBlockChain = new ContratoEsBlockChainFirmado();
			ContratoEsBlockChainFirmado_Entrada ContratoEsBlockChainEntrada = new ContratoEsBlockChainFirmado_Entrada();
			ContratoEsBlockChainEntrada.p_IdEmpresa 			= entrada.p_IdEmpresa;
			ContratoEsBlockChainEntrada.p_IdUsuario 			= entrada.p_IdUsuario;
			ContratoEsBlockChainEntrada.p_ClaveUsuario 			= "";
			
			ConsultaEstadoCntBC ConsultaEstadoCntBC = new ConsultaEstadoCntBC();
			ConsultaEstadoCntBC.p_IdTipoDocumento 				= (tipoPrestamo.get(0).IdTipoPrestamo.equals("PRTVTC") || tipoPrestamo.get(0).IdTipoPrestamo.equals("PRVCRF")) ? ConstantesBCK.IDCONTRATOPRESTAMOANEXO42 : ConstantesBCK.IDCONTRATOPRESTAMOANEXO48;			
			ConsultaEstadoCntBC.p_IdCliente 					= entrada.p_IdClienteSolicita.toString();
			ConsultaEstadoCntBC.p_BrokerageRUT	 				= salidaConsultaDataCorredora.p_Out_BrokerageRUT;
			ConsultaEstadoCntBC.p_IdContrato 					= (tipoPrestamo.get(0).IdTipoPrestamo.equals("PRTVTC") || tipoPrestamo.get(0).IdTipoPrestamo.equals("PRVCRF")) ? ConstantesBCK.CONTRATOPRESTAMOANEXO42 : ConstantesBCK.CONTRATOPRESTAMOANEXO48;

			ContratoEsBlockChainEntrada.p_ConsultaEstadoCntBC 	= ConsultaEstadoCntBC;
			
			ContratoEsBlockChainFirmado_Salida ContratoEsBlockChainFirmado_Salida = ContratoEsBlockChain.logica(con, ContratoEsBlockChainEntrada);
			HashMap<String, Object> parametroEntrada = new HashMap<String, Object>();
			parametroEntrada.put("p_IdTipoDocumento", ConsultaEstadoCntBC.p_IdContrato);
			List<TipoDocumento> arrTipoDocumento = sql.query(con, SQL_SELECT_ESCONTRATOBLOCKCHAIN, parametroEntrada, TipoDocumento.class);

			if (arrTipoDocumento.size() > 0) {
				for (ContratoEsBlockChainFirmado.Resultado ResultadoBC : ContratoEsBlockChainFirmado_Salida.P_OUT_RESULTADOPROCESO) {
					EsBlockChain 		= ResultadoBC.EsBlockChain;
					ContratoFirmado 	= ResultadoBC.ContratoFirmado;
					break;
				}

				if (EsBlockChain.equalsIgnoreCase(ConstantesBCK.SI) && ContratoFirmado.equalsIgnoreCase(ConstantesBCK.SI)) {
					log.write("SI TIENE CONTRATO FIRMADO");
				} else {
					String p_Mensaje = null;
					for (MensajeSalida ResultadoMSJ : ContratoEsBlockChainFirmado_Salida.P_OUT_MENSAJES) {
						p_Mensaje = ResultadoMSJ.descripcion;
						break;
					}
					// Cliente sin contrato de VC firmado
					log.write("NO TIENE CONTRATO FIRMADO");
					log.write("Es blockchain: " + EsBlockChain);
					log.write("Esta firmado: " + ContratoFirmado);
					salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, p_Mensaje, p_Mensaje));
					return salida;
				}
			}
			
		}
			
		
		// ---------------------------------------------------------------------------------------------
		// VALIDA QUE EL CLIENTE TENGA ACTIVO EL INDICADOR CONTRATO DE PRESTAMOS
		// si los tipos de contrato que intervien en este proceso estan "marcados" como BlockChain entonces no valida los mismos con validaciones Optimus pero ademas no tiene que venir de propio Blockchain
		// (lectura de Log de Block que entrega las acciones hechas con prestamos)
		if (EsBlockChain.equals("N") && entrada.p_IdFolioSistemaExterno.equals("")) {
			HashMap<String, Object> paramsIndCli = new HashMap<String, Object>();
			paramsIndCli.put("IdCliente", entrada.p_IdClienteSolicita);
			paramsIndCli.put("IdTipoPrestamo", tipoPrestamo.get(0).IdTipoPrestamo);
			
			HashMap<String, String> valores = new HashMap<String, String>();
			List<IndicadorCliente> indicadorCliente = sql.query(con, SQL_SELECT_INDICADORES_CLIENTE, paramsIndCli, IndicadorCliente.class);

			if (indicadorCliente.size() > 0) // Siempre va a entregar resultado
			{
				log.write("INDICADORES DEL CLIENTE > " + indicadorCliente);
				for (IndicadorCliente indCli : indicadorCliente) {
					if (indCli.ValorIndicador.equalsIgnoreCase("N")) {
						log.write("FALTA CONTRATO > " + indCli.MensajeIndicador);
						salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, indCli.MensajeIndicador, valores, con, entrada.p_IdEmpresa));
						salida.P_OUT_ERRORESCONTADOR = 1L;
						return salida;
					}
				}
			}
		}	
			
			
		//---------------------------------------------------------------------------------------------
		// OBTIENE DATOS INSTRUMENTO SUBYACENTE
		HashMap<String,Object> paramsInsSub = new HashMap<String,Object>();
		paramsInsSub.put("IdInstrumento", entrada.p_IdInstrumento);			
		List<Instrumento> instrumentoSubyacente = sql.query(con, SQL_SELECT_CONSULTAR_INSTRUMENTO, paramsInsSub, Instrumento.class);
					
		if (instrumentoSubyacente.size() == 0)
		{
			log.write("NO SE ENCONTRARON DATOS PARA EL INSTRUMENTO SUBYACENTE");
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "NO_SE_ENCONTRARON_DATOS_PARA_INSTRUMENTO_SUBYACENTE", null, con, entrada.p_IdEmpresa));				
			return salida;
		}
		else
		{
			log.write("DATOS INSTRUMENTO SUBYACENTE > " + instrumentoSubyacente);
		}


		//---------------------------------------------------------------------------------------------
		// GENERACION DEL MOVIMIENTO A FACTURAR (En caso que el prestamo tenga prima inicial)
			
	   if (entrada.p_CobraPrimaAlInicio.equalsIgnoreCase("S") && entrada.p_MontoPrima.compareTo(BigDecimal.ZERO) > 0)
	   {
		   
		   log.write("GENERANDO MOVIMIENTO FACTURACION POR COBRO PRIMA AL INICIO");				

			CrearMovimientoComprobante ComprobantePrestamo = new CrearMovimientoComprobante();				
			CrearMovimientoComprobante_Entrada entradaComprobantePrestamo = new CrearMovimientoComprobante_Entrada();
			
			entradaComprobantePrestamo.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaComprobantePrestamo.p_IdUsuario = entrada.p_IdUsuario;
			entradaComprobantePrestamo.p_IdModulo = entrada.p_IdModulo;
			entradaComprobantePrestamo.p_simularOperacion = "N";
			entradaComprobantePrestamo.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
			entradaComprobantePrestamo.p_EsIngresoAutomatico = "S";
			
			
			MovimientoComprobante_Entrada entradaMovimientoComprobante = new MovimientoComprobante_Entrada();
			
			entradaMovimientoComprobante.NumeroMvtoCmbte = 1L;
			entradaMovimientoComprobante.IdEmpresa = entrada.p_IdEmpresa;
			entradaMovimientoComprobante.IdConcepto = "COBPRI"; // Cobro Prima				
			entradaMovimientoComprobante.SecuenciaAgrupacion = "";				
			entradaMovimientoComprobante.IdCliente = entrada.p_IdClienteSolicita;
			entradaMovimientoComprobante.IdPortafolio = entrada.p_IdPortafolioSolicita;				
			entradaMovimientoComprobante.ModPrefFact = "";				
			entradaMovimientoComprobante.IdEmpleadoDigitador = new Long(entrada.p_IdEmpleadoDigitador);				
			entradaMovimientoComprobante.IdInstrumento = entrada.p_IdInstrumento;
			entradaMovimientoComprobante.IdInstrumentoRelacionado = 0L;				
			entradaMovimientoComprobante.IdInstrumentoSubyacente = 0L;
			entradaMovimientoComprobante.IdTipoProducto = "PRES"; // Prestamo
			entradaMovimientoComprobante.IdMoneda = entrada.p_IdMonedaSistema;				
			entradaMovimientoComprobante.IdMonedaSubyacente = "";
			entradaMovimientoComprobante.IdMonedaLiquidacion = entrada.p_IdMonedaSistema;
			entradaMovimientoComprobante.IdTipoComprobante = "FEE";     // Factura electronica exenta
			entradaMovimientoComprobante.IdTipoOperacion = "PRESTA";    // Prestamo
			entradaMovimientoComprobante.IdBoveda = tipoPrestamo.get(0).IdBovedaDefecto;
			entradaMovimientoComprobante.IdCorredoraContraparte = 0L;
						
			entradaMovimientoComprobante.IdCondicionLiquidacion = entrada.p_IdCondicionLiquidacion.equalsIgnoreCase("") ? "T0" : entrada.p_IdCondicionLiquidacion;				
			entradaMovimientoComprobante.FechaLiquidacion = entrada.p_FechaLiquidacion.equalsIgnoreCase("") ? fechaSistema : entrada.p_FechaLiquidacion;
		
			
			entradaMovimientoComprobante.IdBolsa = entrada.p_IdBolsa;
			entradaMovimientoComprobante.IdTipoReajuste = "";
			entradaMovimientoComprobante.IdTipoConvenio = "SCV"; // Sin convenio				
			entradaMovimientoComprobante.IdFormaCustodia = "";				
			entradaMovimientoComprobante.IdOrden = 0L;
			entradaMovimientoComprobante.IdAsignacion = 0L;
			entradaMovimientoComprobante.IdAsignacionRef = 0L;
			entradaMovimientoComprobante.IdTipoCanalIngreso = "";
			entradaMovimientoComprobante.FechaOrden = "0001-01-01";
			entradaMovimientoComprobante.IdTransaccion = 0L;
			entradaMovimientoComprobante.FolioTransaccion = "";				
			entradaMovimientoComprobante.FechaTransaccion = entrada.p_FechaMovimiento;
			entradaMovimientoComprobante.FechaVencimiento = entrada.p_FechaMovimiento;
			entradaMovimientoComprobante.PrecioAPlazo = BigDecimal.ZERO;
			entradaMovimientoComprobante.MontoAPlazo = entrada.p_MontoAPlazo;				
			entradaMovimientoComprobante.FechaParidad = "0001-01-01";
			entradaMovimientoComprobante.ValorParidad = BigDecimal.ZERO;
			entradaMovimientoComprobante.FechaTipoCambio = "0001-01-01"; 
			entradaMovimientoComprobante.ValorTipoCambio = BigDecimal.ZERO; 
			entradaMovimientoComprobante.ValorCaptacionMonedaOrigen = BigDecimal.ZERO;
			entradaMovimientoComprobante.TasaOperacion = entrada.p_TasaPrima; 
			entradaMovimientoComprobante.Cantidad = entrada.p_Cantidad; 
			entradaMovimientoComprobante.Precio = entrada.p_Precio;
			entradaMovimientoComprobante.Monto = entrada.p_MontoPrima;
			entradaMovimientoComprobante.MontoMovimientoComprobante = entrada.p_MontoPrima;
			entradaMovimientoComprobante.MontoLiquidacion = entrada.p_MontoPrima;
			entradaMovimientoComprobante.MontoDescuento = BigDecimal.ZERO; 
			entradaMovimientoComprobante.MontoInteres = BigDecimal.ZERO;
			entradaMovimientoComprobante.Glosa = "PRIMA POR PRESTAMO " + instrumentoSubyacente.get(0).Nemotecnico;
			entradaMovimientoComprobante.ValorRescate = BigDecimal.ZERO;
			entradaMovimientoComprobante.TasaAgente = BigDecimal.ZERO; 
			entradaMovimientoComprobante.Spread = BigDecimal.ZERO; 
			entradaMovimientoComprobante.DiasPlazo = BigDecimal.ZERO;
			entradaMovimientoComprobante.FechaMovimientoComprobante = entrada.p_FechaMovimiento; 				
			entradaMovimientoComprobante.FolioCartera = 0L;
			entradaMovimientoComprobante.FechaEmision = entrada.p_FechaMovimiento; 
			entradaMovimientoComprobante.TasaEmision = BigDecimal.ZERO; 
			entradaMovimientoComprobante.EsComprobIndividual = "N";
			entradaMovimientoComprobante.EsCuentaPropia = "N"; 
			entradaMovimientoComprobante.IndicadorOrdenDirecta = ""; 
			entradaMovimientoComprobante.EsVentaCorta = "N"; 
			entradaMovimientoComprobante.EsPrecioPrima = "S";				
			entradaMovimientoComprobante.EsEnRueda = "N"; 
			entradaMovimientoComprobante.EsRuteo = "N"; 
			entradaMovimientoComprobante.EsAPlazo = "N"; 
			entradaMovimientoComprobante.EsFungible = "N"; 
			entradaMovimientoComprobante.ActualizarCustodia = "N";
			entradaMovimientoComprobante.ActualizarCuentaInversion = "S"; 
			entradaMovimientoComprobante.ActualizarTesoreria = "S"; 
			entradaMovimientoComprobante.ActualizarCustodiaRelacionado = "N";
			entradaMovimientoComprobante.CrearComprobanteAutomatico = "N";
			entradaMovimientoComprobante.IdUsuario = entrada.p_IdUsuario;
			entradaMovimientoComprobante.Tasa = entrada.p_TasaInstrumento;
			
			entradaComprobantePrestamo.p_Movimientos.add(entradaMovimientoComprobante);
			
			
			CrearMovimientoComprobante_Salida salidaCrearMovimientoComprobante = ComprobantePrestamo.logica(con, entradaComprobantePrestamo);
			
			
			//Recorre y asigna mensajes de salida del crearMovimientoComprobante a la salida de CrearPrestamo
			for (MensajeSalida msj : salidaCrearMovimientoComprobante.P_OUT_MENSAJES) 
			{		
				salida.P_OUT_MENSAJES.add(msj);
			}
			
			if (MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES) > 0)
			{
				log.write("ERROR AL GENERAR MOVIMIENTO A FACTURAR");									
				return salida;					
			}
			
			
			
			//Obtiene Id del movimiento de facturación creado 
			for (cl.bcs.facturacion.CrearMovimientoComprobante.ResultadoProceso_Salida result : salidaCrearMovimientoComprobante.p_out_ResultadoProceso) 
			{
				if (result.Campo.equalsIgnoreCase("IdMovimientoComprobante") && result.Resultado.equalsIgnoreCase("ING"))
				{
					FolioComprobanteFacturacion = result.Id;  
				}
			}				
			
			log.write("MOVIMIENTO FACTURACION GENERADO > " + salidaCrearMovimientoComprobante.P_OUT_MENSAJES);
			
		   
	   } // FIN  -  if (entrada.p_CobraPrimaAlInicio == "S" && entrada.p_MontoPrima.compareTo(BigDecimal.ZERO) > 0) 
		

		
		// ---------------------------------------------------------------------------------------------------------------------
		// CREAR PRESTAMO EN BLOCKCHAIN
		// ---------------------------------------------------------------------------------------------------------------------
		
		List<LogProcesoPrestamoBlockChain> logInformarPrestamoBlockChain 	= new ArrayList<>();
		String folioSistemaExterno 											= entrada.p_IdFolioSistemaExterno;
		// si se paso esta valor es que NO se debe ir a sistema BlockChain
		if ( entrada.p_IdFolioSistemaExterno.equals( "" ) && ( !tipoPrestamo.get( 0 ).IdTipoPrestamo.equalsIgnoreCase( ConstantesBCK.PRESTAMO_TIPO_VENTA_CORTA )
				|| tipoPrestamo.get( 0 ).IdTipoPrestamo.equalsIgnoreCase( ConstantesBCK.PRESTAMO_TIPO_VENTA_CORTA_RENTA_FIJA ) && prestamosRentaFija.equalsIgnoreCase( ConstantesBCK.SI ) ) ) {
			if ( EsBlockChain.equalsIgnoreCase( ConstantesBCK.SI ) && ContratoFirmado.equalsIgnoreCase( ConstantesBCK.SI ) ) {
				String tipoPrestamoSistemaExterno = ( entrada.p_TipoCustodia.equals(ConstantesBCK.SI) ) ? "C" : tipoPrestamo.get(0).TipoPrestamoSistemaExterno;
				HashMap<String,Object> parametroEntrada = new HashMap<String,Object>();
				parametroEntrada.put("p_IdCondicionDeLiquidacion", entrada.p_IdCondicionLiquidacion);
				List<SelectCondicionDeLiquidacion> selectCondicionDeLiquidacion = sql.query(con, SQL_SELECT_CONDICION_DE_LIQUIDACION, parametroEntrada, SelectCondicionDeLiquidacion.class);
				
				IngresoPrestamosBlockchain IngresoPrestamo 							= new IngresoPrestamosBlockchain();
				IngresoPrestamosBlockchain_Entrada IngresoPrestamoBlockChainEntrada = new IngresoPrestamosBlockchain_Entrada();
				IngresoPrestamoBlockChainEntrada.p_IdEmpresa 					= entrada.p_IdEmpresa;
				IngresoPrestamoBlockChainEntrada.p_Tasa 						= entrada.p_TasaPrima.toString();
				IngresoPrestamoBlockChainEntrada.p_FechaOperacion 				= entrada.p_FechaMovimiento;
				IngresoPrestamoBlockChainEntrada.p_Nemo 						= instrumentoSubyacente.get(0).Nemotecnico;
				IngresoPrestamoBlockChainEntrada.p_Mercado 						= entrada.p_IdMercado; 
				IngresoPrestamoBlockChainEntrada.p_TipoPrestamo 				= tipoPrestamoSistemaExterno;
				IngresoPrestamoBlockChainEntrada.p_TipoGarantia 				= "T";
				IngresoPrestamoBlockChainEntrada.p_CondicionLiquidacion 		= selectCondicionDeLiquidacion.get(0).Codigo;
				IngresoPrestamoBlockChainEntrada.p_CodigoCorredorPrestamista 	= salidaConsultaDataCorredora.p_Out_BrokerageID;
				IngresoPrestamoBlockChainEntrada.p_Cantidad 					= entrada.p_Cantidad.toString();
				IngresoPrestamoBlockChainEntrada.p_FechaVencimiento 			= entrada.p_FechaVencimiento;
				IngresoPrestamoBlockChainEntrada.p_IdClientePrestatario 		= entrada.p_IdClienteSolicita.toString(); 
				IngresoPrestamoBlockChainEntrada.p_IdClientePrestamista 		= "";
				IngresoPrestamoBlockChainEntrada.p_CantidadPrestamista 			= "0";
				IngresoPrestamosBlockchain_Salida IngresoPrestamoBlockChainFirmado_Salida = IngresoPrestamo.logica(con, IngresoPrestamoBlockChainEntrada);
				
				salida.P_OUT_MENSAJES.addAll( IngresoPrestamoBlockChainFirmado_Salida.P_OUT_MENSAJES );
					
				if (IngresoPrestamoBlockChainFirmado_Salida.p_Out_ContadorError > 0) {
					logInformarPrestamoBlockChain.add(new LogProcesoPrestamoBlockChain(entrada.p_IdEmpresa, 0L, fechaSistema, IngresoPrestamoBlockChainFirmado_Salida.p_Out_Mensaje, "CrearPrestamo", "logica", entrada.p_IdUsuario.toString()));
				}
				else {
					folioSistemaExterno = IngresoPrestamoBlockChainFirmado_Salida.p_Out_FolioExterno;
				}
			}
		}
		
		// ------------------------------------------------------------------------------------
		// CALCULO DE PARIDAD PARA MONEDAS DIFERENTES A MONEDA SISTEMA
		log.write("CALCULAR MONTO EN MONEDA SISTEMA");
		CalcularParidad calculoParidad = new CalcularParidad();
		CalcularParidadEntrada calcularParidadEntrada = new CalcularParidadEntrada();
		calcularParidadEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
		calcularParidadEntrada.p_IdTrabajo = 0L;
		
		CalculoEntrada calculoEntrada = new CalculoEntrada();
		calculoEntrada.Fecha = entrada.p_FechaMovimiento;
		calculoEntrada.IdInterno = 1L;
		calculoEntrada.IdMonedaOrigen = instrumentoSubyacente.get(0).IdMoneda;
		calculoEntrada.IdMonedaDestino = monedaSistema;
		calculoEntrada.IdTrabajo = 1L;
		calculoEntrada.IdUso = "";
		calculoEntrada.MontoOrigen = entrada.p_Monto;
		calculoEntrada.ValorCambioLocalModificado = BigDecimal.ZERO;
		
		CalculoEntrada calculoEntradaMontoPlazo = new CalculoEntrada();
		calculoEntradaMontoPlazo.Fecha = entrada.p_FechaMovimiento;
		calculoEntradaMontoPlazo.IdInterno = 2L;
		calculoEntradaMontoPlazo.IdMonedaOrigen = instrumentoSubyacente.get(0).IdMoneda;
		calculoEntradaMontoPlazo.IdMonedaDestino = monedaSistema;
		calculoEntradaMontoPlazo.IdTrabajo = 2L;
		calculoEntradaMontoPlazo.IdUso = "";
		calculoEntradaMontoPlazo.MontoOrigen = entrada.p_MontoAPlazo;
		calculoEntradaMontoPlazo.ValorCambioLocalModificado = BigDecimal.ZERO;
		
		calcularParidadEntrada.p_CalculosEntrada.add(calculoEntrada);
		calcularParidadEntrada.p_CalculosEntrada.add(calculoEntradaMontoPlazo);
		
		CalcularParidadSalida calculoSalida = calculoParidad.logica(con,calcularParidadEntrada);
		HashMap<Long,Object> hCalculoSalida = Utils.List2Hash(calculoSalida.p_out_CalculosSalida, "IdInterno");
		
		BigDecimal montoLocal = ((CalcularParidad.CalculoSalida)hCalculoSalida.get(1L)).MontoLocal;
		BigDecimal montoAPlazoLocal = ((CalcularParidad.CalculoSalida)hCalculoSalida.get(2L)).MontoLocal;
		
		
		if (calculoSalida.P_OUT_ERRORESCONTADOR > 0) {
			salida.P_OUT_ERRORESCONTADOR = calculoSalida.P_OUT_ERRORESCONTADOR;
			return salida;
		}
		
		// ---------------------------------------------------------------------------------------------------------------------
		// CREA MOVIMIENTOS EN TRANSITO PARA CARTERA Y SUBYACENTE
		log.write("CREANDO MOVIMIENTOS EN TRANSITO PARA CARTERA Y SUBYACENTE");

		List<IngresoCustodia.ObjDatosEntrada> datosMovimientoTransito = new ArrayList<>();
		// Datos para movimiento en transito de cartera
		{

			IngresoCustodia.ObjDatosEntrada ObjEntrada = new IngresoCustodia.ObjDatosEntrada();

			ObjEntrada.IdGenerarMovimientoCustodia = 1L;
			ObjEntrada.TablaAfectada = "TRANSITO";
			ObjEntrada.AccionOperacion = "INGRESO";
			ObjEntrada.EsRegistroNuevo = "S";
			ObjEntrada.FolioCarteraOrigen = 0L;
			ObjEntrada.IdCliente = entrada.p_IdClienteSolicita;
			ObjEntrada.IdPortafolio = entrada.p_IdPortafolioSolicita;
			ObjEntrada.FechaOperacion = new java.sql.Date(format.parse(entrada.p_FechaMovimiento).getTime());
			ObjEntrada.FechaVencimientoOperacion = new java.sql.Date(format.parse(entrada.p_FechaVencimiento).getTime());
			ObjEntrada.FechaVencimientoInstrumento = instrumentoSubyacente.get(0).FechaVencimiento;
			ObjEntrada.FechaEmisionInstrumento = instrumentoSubyacente.get(0).FechaEmision;
			ObjEntrada.IdMonedaEmision = instrumentoSubyacente.get(0).IdMoneda;
			ObjEntrada.PlazoEnDiasOperacion = entrada.p_PlazoEnDias;
			ObjEntrada.IdTipoOperacion = "PRESTA";
			ObjEntrada.IdTipoProducto = "PRES";
			ObjEntrada.IdConcepto = "INGPRT"; // Ingreso Prestamo
			ObjEntrada.IdCondicionLiquidacion = entrada.p_IdCondicionLiquidacion.equalsIgnoreCase("") ? "T0": entrada.p_IdCondicionLiquidacion;
			ObjEntrada.FechaLiquidacion = new java.sql.Date(format
					.parse(entrada.p_FechaLiquidacion.equalsIgnoreCase("") ? fechaSistema : entrada.p_FechaLiquidacion)
					.getTime());

				
			ObjEntrada.IdTipoCanalIngreso				= entrada.p_IdTipoCanalIngreso;
			ObjEntrada.IdTipoComprobante				= "FEE";   // Factura electronica exenta 
			ObjEntrada.FolioComprobante					= FolioComprobanteFacturacion; 
			ObjEntrada.FolioExternoReferencia			= entrada.p_FolioEntidad;
			ObjEntrada.IdOperacion						= FolioComprobanteFacturacion;
			ObjEntrada.IdBolsa							= entrada.p_IdBolsa;	
			ObjEntrada.IdUsuario						= entrada.p_IdUsuario;
			ObjEntrada.IdSucursal						= entrada.p_IdSucursal; 
			ObjEntrada.IdAgente							= entrada.p_IdAgente;
			ObjEntrada.Glosa							= "PRESTAMO " + instrumentoSubyacente.get(0).Nemotecnico;
			ObjEntrada.IdInstrumento					= entrada.p_IdInstrumentoPrestamo;
			ObjEntrada.IdInstrumentoAnterior			= 0L;
			ObjEntrada.IdMonedaInstrumento				= instrumentoSubyacente.get(0).IdMoneda;
			ObjEntrada.IdTipoReajusteInstrumento		= instrumentoSubyacente.get(0).IdTipoReajuste;
			ObjEntrada.IdEmisor 						= tipoPrestamo.get(0).IdEmisor;
			ObjEntrada.EsFungible						= entrada.p_EsFungible;
			ObjEntrada.IdBoveda							= tipoPrestamo.get(0).IdBovedaDefecto; 
			ObjEntrada.EsNominal						= entrada.p_esNominal;
			ObjEntrada.Cantidad							= entrada.p_Cantidad;
			ObjEntrada.CorteMinimo						= instrumentoSubyacente.get(0).CorteMinimo; 
			ObjEntrada.ValorRescate						= BigDecimal.ZERO; 
			ObjEntrada.PrecioCompra						= entrada.p_Precio; 
			ObjEntrada.TasaCompra						= entrada.p_TasaPrima;
			ObjEntrada.MontoCompra						= montoLocal;
			ObjEntrada.MontoCompraMonedaInstrumento		= entrada.p_Monto;
			ObjEntrada.EsVentaCorta 					= (tipoPrestamo.get(0).IdTipoPrestamo.equals("PRTVTC") || tipoPrestamo.get(0).IdTipoPrestamo.equals("PRVCRF")) ? "S" : "N";
			ObjEntrada.FechaEmisionInstrumento 			= instrumentoSubyacente.get(0).FechaEmision;
			ObjEntrada.MontoAPlazo 						= montoAPlazoLocal;
			ObjEntrada.MontoAPlazoMonedaInstrumento 	= entrada.p_MontoAPlazo;
			ObjEntrada.PrecioAPlazo						= entrada.p_MontoPrima;			
			ObjEntrada.IdTipoPrestamo 					= tipoPrestamo.get(0).IdTipoPrestamo;
			ObjEntrada.CobraPrimaAlInicio               = entrada.p_CobraPrimaAlInicio;
			ObjEntrada.UsaGarantiaEspecifica 			= entrada.p_ConGarantiaEspecifica;
			
			datosMovimientoTransito.add(ObjEntrada);
		}
					
		//Datos para movimiento en transito de subyacente
		{
			IngresoCustodia.ObjDatosEntrada	ObjEntrada = new IngresoCustodia.ObjDatosEntrada();
			
			ObjEntrada.IdGenerarMovimientoCustodia		= -1*1L;
			ObjEntrada.IdMovimientoCarteraEnTransitoRelacionado = 1L;
			ObjEntrada.TablaAfectada					= "TRANSITO";
			ObjEntrada.AccionOperacion					= "INGRESO";
			ObjEntrada.EsRegistroNuevo					= "S";
			ObjEntrada.FolioCarteraOrigen				= 0L;	
			ObjEntrada.IdCliente						= entrada.p_IdClienteSolicita;  
			ObjEntrada.IdPortafolio						= entrada.p_IdPortafolioSolicita;
			ObjEntrada.FechaOperacion					= new java.sql.Date(format.parse(entrada.p_FechaMovimiento).getTime());			
			ObjEntrada.FechaVencimientoOperacion		= new java.sql.Date(format.parse(entrada.p_FechaVencimiento).getTime());
			ObjEntrada.FechaVencimientoInstrumento		= instrumentoSubyacente.get(0).FechaVencimiento;
			ObjEntrada.PlazoEnDiasOperacion				= entrada.p_PlazoEnDias;
			ObjEntrada.IdTipoOperacion					= "PRESTA";
			ObjEntrada.IdTipoProducto					= "PRES";
			ObjEntrada.IdConcepto						= "INGPRT";  // Ingreso Prestamo
			
	
			ObjEntrada.IdCondicionLiquidacion			= entrada.p_IdCondicionLiquidacion.equalsIgnoreCase("") ? "T0" : entrada.p_IdCondicionLiquidacion; 
			ObjEntrada.FechaLiquidacion					= new java.sql.Date(format.parse(entrada.p_FechaLiquidacion.equalsIgnoreCase("") ? fechaSistema : entrada.p_FechaLiquidacion).getTime());
				
			ObjEntrada.IdTipoCanalIngreso				= entrada.p_IdTipoCanalIngreso;
			ObjEntrada.IdTipoComprobante				= "FEE";   // Factura electronica exenta 
			ObjEntrada.FolioComprobante					= FolioComprobanteFacturacion; 
			ObjEntrada.FolioExternoReferencia			= entrada.p_FolioEntidad;
			ObjEntrada.IdOperacion						= FolioComprobanteFacturacion;
			ObjEntrada.IdBolsa							= entrada.p_IdBolsa;	
			ObjEntrada.IdUsuario						= entrada.p_IdUsuario;
			ObjEntrada.IdSucursal						= entrada.p_IdSucursal; 
			ObjEntrada.IdAgente							= entrada.p_IdAgente;
			ObjEntrada.Glosa							= "PRESTAMO " + instrumentoSubyacente.get(0).Nemotecnico;
			ObjEntrada.IdInstrumento					= entrada.p_IdInstrumento;
			ObjEntrada.IdInstrumentoAnterior			= 0L;
			ObjEntrada.IdMonedaInstrumento				= instrumentoSubyacente.get(0).IdMoneda;
			ObjEntrada.IdMonedaEmision 					= instrumentoSubyacente.get(0).IdMoneda;
			ObjEntrada.IdTipoReajusteInstrumento		= instrumentoSubyacente.get(0).IdTipoReajuste;
			ObjEntrada.IdEmisor							= instrumentoSubyacente.get(0).IdEmisor;
			ObjEntrada.EsFungible						= entrada.p_EsFungible;
			ObjEntrada.IdBoveda							= tipoPrestamo.get(0).IdBovedaDefecto;
			ObjEntrada.EsNominal						= entrada.p_esNominal;			
			ObjEntrada.Cantidad							= entrada.p_Cantidad; 
			ObjEntrada.CorteMinimo						= instrumentoSubyacente.get(0).CorteMinimo; 
			ObjEntrada.ValorRescate						= BigDecimal.ZERO; 
			ObjEntrada.PrecioCompra						= entrada.p_Precio; 
			ObjEntrada.TasaCompra						= entrada.p_TasaPrima;
			ObjEntrada.MontoCompra						= calculoSalida.p_out_CalculosSalida.get(0).MontoLocal;
			ObjEntrada.MontoCompraMonedaInstrumento		= entrada.p_Monto;
			ObjEntrada.EsVentaCorta						= "N";

			ObjEntrada.FechaEmisionInstrumento 			= instrumentoSubyacente.get(0).FechaEmision;
			ObjEntrada.MontoAPlazo 						= montoAPlazoLocal;
			ObjEntrada.MontoAPlazoMonedaInstrumento 	= entrada.p_MontoAPlazo;
			ObjEntrada.PrecioAPlazo						= entrada.p_MontoPrima;			
			ObjEntrada.IdTipoPrestamo 					= tipoPrestamo.get(0).IdTipoPrestamo;
			ObjEntrada.CobraPrimaAlInicio               = entrada.p_CobraPrimaAlInicio;
			ObjEntrada.UsaGarantiaEspecifica 			= entrada.p_ConGarantiaEspecifica;	
			
			datosMovimientoTransito.add(ObjEntrada);
		}
					

				
				
		//Ingreso de movimientos en transito(cartera y subyacente)	
		IngresoCustodia ingresoCustodiaTransito = new IngresoCustodia();
		IngresoCustodia.IngresoCustodiaEntrada entradaMovimientoTransito = new IngresoCustodia.IngresoCustodiaEntrada();
		
		entradaMovimientoTransito.p_IdEmpresa					= entrada.p_IdEmpresa;
		entradaMovimientoTransito.p_IdModulo					= entrada.p_IdModulo;
		entradaMovimientoTransito.p_IdUsuario					= entrada.p_IdUsuario;
		entradaMovimientoTransito.p_SimularOperacion			= "N";
		entradaMovimientoTransito.p_TicketAtencion 				= 0L;
		entradaMovimientoTransito.p_ValorizacionAutomatica		= "S";
		entradaMovimientoTransito.p_DatosEntrada				= datosMovimientoTransito;
		
		IngresoCustodia.IngresoCustodiaSalida salidaMovimientoTransito = ingresoCustodiaTransito.logica(con, entradaMovimientoTransito);
		

		for (IngresoCustodia.IngresoCustodiaSalida.ObjDatosSalida datosSalida : salidaMovimientoTransito.P_OUT_DATOSSALIDA) 
		{			
			if (datosSalida.RegistroConProblemas.equalsIgnoreCase("S")) 
			{
				String codigo = "UI_ERROR_AL_PROCESAR_MOVIMIENTO"; //Error al procesar movimiento ::IdMovimientoComprobante::
				String descripcion = Diccionario.getMensaje(con, entrada.p_IdEmpresa, codigo).add("IdMovimientoComprobante", datosSalida.IdGenerarMovimientoCustodia).msg();
				salida.P_OUT_MENSAJES.add(new MensajeSalida(datosSalida.IdGenerarMovimientoCustodia, MensajeSalida.MENSAJE_ERROR, codigo, descripcion));
			}
			else 
			{	
				transitosPorCertificar.add(datosSalida.IdCustodia);
			}	
		}
			

			
		//---------------------------------------------------------------------------------------------------------------------			
		// INICIO PROCESAR MOVIMIENTOS EN TRANSITO	
		//---------------------------------------------------------------------------------------------------------------------
		
		List<GestionMovimientoCarteraEnTransito.ObjMovimientosEnTransito> MovimientosEnTransito =  GestionMovimientoCarteraEnTransito.ObtenerMovimientoEnTransito(con, transitosPorCertificar);
		
		for (GestionMovimientoCarteraEnTransito.ObjMovimientosEnTransito movTransito : MovimientosEnTransito) 
		{
			
			
			//---------------------------------------------------------------------------------------------			
			// CREAR INGRESO CARTERA PRESTAMO (a partir de movimiento en transito)
			
			if (movTransito.IdMovimientoCarteraEnTransitoRelacionado.compareTo(0L) == 0 )
			{  
			    log.write("CREANDO CARTERA DEL PRESTAMO ");			   

			    //Prepara datos para el ingreso cartera del prestamo
				List<IngresoCustodia.ObjDatosEntrada> datosEntradaCartera = new ArrayList<>();
				IngresoCustodia.ObjDatosEntrada datosCartera = new IngresoCustodia.ObjDatosEntrada();
				
				datosCartera.IdGenerarMovimientoCustodia		 = 1L;
				datosCartera.TablaAfectada						 = "CARTERA";
				datosCartera.AccionOperacion					 = "INGRESO";
				datosCartera.EsRegistroNuevo					 = "S";
				datosCartera.IdMovimientoCarteraEnTransitoOrigen = movTransito.IdMovimientoCarteraEnTransito;
				datosCartera.IdCliente							 = movTransito.IdCliente; 
				datosCartera.IdPortafolio						 = movTransito.IdPortafolio;	
				datosCartera.IdCuentaCustodioPortafolio		     = movTransito.IdCuentaCustodioPortafolio;
				datosCartera.IdTipoProducto 					 = "PRES";
				datosCartera.FechaOperacion						 = movTransito.FechaOperacion;				
				datosCartera.FechaVencimientoOperacion			 = movTransito.FechaVencimientoOperacion;				
				datosCartera.FechaVencimientoInstrumento		 = movTransito.FechaVencimientoInstrumento;				
				datosCartera.PlazoEnDiasOperacion				 = movTransito.PlazoEnDiasOperacion;				
				datosCartera.IdTipoOperacion					 = movTransito.IdTipoOperacion;							
				datosCartera.IdConcepto							 = movTransito.IdConcepto;  
				datosCartera.IdCondicionLiquidacion				 = movTransito.IdCondicionLiquidacion;
				datosCartera.FechaLiquidacion					 = movTransito.FechaLiquidacion;				
				datosCartera.IdTipoCanalIngreso					 = movTransito.IdTipoCanalIngreso;
				datosCartera.CodigoIFRS							 = movTransito.CodigoIFRS;
				datosCartera.NemoSVS							 = movTransito.NemoSVS;
				datosCartera.IdTipoComprobante					 = movTransito.IdTipoComprobante; 
				datosCartera.FolioComprobante					 = movTransito.FolioComprobante;
				datosCartera.FolioExternoReferencia				 = movTransito.FolioExternoReferencia;				
				datosCartera.IdBolsa							 = movTransito.IdBolsa;	
				datosCartera.IdUsuario							 = movTransito.IdUsuario;
				datosCartera.IdSucursal							 = movTransito.IdSucursal;
				datosCartera.IdAgente							 = movTransito.IdAgente;
				datosCartera.Glosa								 = movTransito.Glosa;
				datosCartera.IdInstrumento						 = movTransito.IdInstrumento;				
				datosCartera.IdMonedaInstrumento				 = movTransito.IdMonedaInstrumento;
				datosCartera.IdMonedaEmision					 = movTransito.IdMonedaEmision;
				datosCartera.IdTipoReajusteInstrumento			 = movTransito.IdTipoReajuste;
				datosCartera.EsFungible							 = movTransito.EsFungible;
				datosCartera.IdBoveda							 = movTransito.IdBoveda;
				datosCartera.EsNominal							 = movTransito.EsNominal;				
				datosCartera.Cantidad							 = movTransito.Cantidad;				
				datosCartera.CorteMinimo						 = movTransito.CorteMinimo;
				datosCartera.ValorRescate						 = movTransito.ValorRescate;
				datosCartera.PrecioCompra						 = movTransito.PrecioCompra; 
				datosCartera.TasaCompra							 = movTransito.TasaCompra;
				datosCartera.MontoCompra						 = movTransito.MontoCompra;
				datosCartera.MontoCompraMonedaInstrumento		 = movTransito.MontoCompraMonedaInstrumento;				
				datosCartera.PrecioAPlazo						 = movTransito.PrecioAPlazo;				
				datosCartera.IdEmisor							 = movTransito.IdEmisor;
				datosCartera.FechaEmisionInstrumento 			 = movTransito.FechaEmisionInstrumento;
				datosCartera.UsaGarantiaEspecifica 				 = entrada.p_ConGarantiaEspecifica;				
				datosCartera.CobraPrimaAlInicio               	 = entrada.p_CobraPrimaAlInicio;
				datosCartera.MontoAPlazo 						 = montoAPlazoLocal;
				datosCartera.MontoAPlazoMonedaInstrumento 		 = entrada.p_MontoAPlazo;
				datosCartera.IdTipoPrestamo 					 = tipoPrestamo.get(0).IdTipoPrestamo;								
				datosCartera.FolioSistemaExterno 			     = folioSistemaExterno;
				datosCartera.TipoCustodia                        = entrada.p_TipoCustodia; 
				
				// DATOS PARA VALORIZACION DE CARTERA
				datosCartera.IdInstrumentoSubyacente             = entrada.p_IdInstrumento; 
				datosCartera.NemotecnicoSubyacente 				 = entrada.p_Nemo;
				datosCartera.IdTipoInstrumentoSubyacente 	     = instrumentoSubyacente.get(0).IdTipoInstrumento;
				datosCartera.TasaCompraInstrumento 	     		 = entrada.p_TasaInstrumento;
				datosEntradaCartera.add(datosCartera);
				
				//Ingreso de cartera de prestamos
				IngresoCustodia ingresoCustodia = new IngresoCustodia();
				IngresoCustodia.IngresoCustodiaEntrada entradaIngresoCustodia = new IngresoCustodia.IngresoCustodiaEntrada();
				
				entradaIngresoCustodia.p_IdEmpresa				= entrada.p_IdEmpresa;		
				entradaIngresoCustodia.p_IdModulo				= entrada.p_IdModulo;
				entradaIngresoCustodia.p_IdUsuario				= entrada.p_IdUsuario;
				entradaIngresoCustodia.p_SimularOperacion		= "N";
				entradaIngresoCustodia.p_TicketAtencion 		= 0L;
				entradaIngresoCustodia.p_ValorizacionAutomatica	= "S";
				entradaIngresoCustodia.p_DatosEntrada			= datosEntradaCartera;
				IngresoCustodia.IngresoCustodiaSalida salidaIngresoCustodia = ingresoCustodia.logica(con, entradaIngresoCustodia);

				
				//Recorre y asigna mensajes de salida del ingresoCustodia a la salida de CrearPrestamo
				for (MensajeSalida msj : salidaIngresoCustodia.P_OUT_MENSAJES) 
				{		
					salida.P_OUT_MENSAJES.add(msj);
				}
				
				
				for (IngresoCustodiaSalida.ObjDatosSalida datoSalida : salidaIngresoCustodia.P_OUT_DATOSSALIDA) 
				{
						if (datoSalida.RegistroConProblemas.equalsIgnoreCase("N")) 
						{
							FolioCustodia = datoSalida.FolioCustodia;								
							salida.P_OUT_FOLIOCARTERA = datoSalida.FolioCustodia;
							salida.P_OUT_IDCARTERA = datoSalida.IdCustodia;
							String codigo = "UI_CARTERA_CREADA_FOLIO"; //Cartera ::TipoCartera:: creada, Folio : ::FolioCartera::
							String descripcion = Diccionario.getMensaje(con, entrada.p_IdEmpresa, codigo).add("FolioCartera", datoSalida.FolioCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
							salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_INFORMACION, descripcion, null, con, entrada.p_IdEmpresa));
							if (!logInformarPrestamoBlockChain.isEmpty()) {
								List<LogProcesoPrestamoBlockChain> logProcesoPrestamoBlockChain = logInformarPrestamoBlockChain.stream()
										.map(log_proceso_prestamo_blockchain -> {
											return new LogProcesoPrestamoBlockChain(log_proceso_prestamo_blockchain.IdEmpresa, datoSalida.FolioCustodia, log_proceso_prestamo_blockchain.FechaProceso, log_proceso_prestamo_blockchain.Descripcion, log_proceso_prestamo_blockchain.OrigenProcesoClase, log_proceso_prestamo_blockchain.OrigenProcesoMetodo, log_proceso_prestamo_blockchain.CreadorParametro);
											})
										.collect(Collectors.toList());
								sql.batch(con, SQL_INSERT_LOG_PROCESO_BLOCKCHAIN, logProcesoPrestamoBlockChain, LogProcesoPrestamoBlockChain.class);	
							}
						}
						else 
						{	
							String codigo = "UI_ERROR_AL_CREAR_CARTERA_MOVIMIENTO"; //Error al crear Cartera de ::TipoCartera::, movimiento ::IdMovimientoComprobante::
							String descripcion = Diccionario.getMensaje(con, entrada.p_IdEmpresa, codigo).add("IdMovimientoComprobante", datoSalida.IdGenerarMovimientoCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();						
							log.write(descripcion);
							salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, descripcion, null, con, entrada.p_IdEmpresa));
						}
				}

				if (MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES) > 0)	
				{
					log.write("ERROR EN EL INGRESO DE CARTERA MOVIMIENTO");									
					return salida;					
				}

			} // FIN - if (movTransito.IdMovimientoCarteraEnTransitoRelacionado.compareTo(0L) == 0 )
			

			
			
			//---------------------------------------------------------------------------------------------			
			// CREANDO SUBYACENTE DEL PRESTAMO (a partir de movimiento en transito)				
			
			if (movTransito.IdMovimientoCarteraEnTransitoRelacionado.compareTo(0L) != 0 )
			{	
			    log.write("CREANDO CUSTODIA SUBYACENTE DEL PRESTAMO");			   

			    //Prepara datos para el ingreso de subyacente del prestamo
			    
				List<IngresoCustodia.ObjDatosEntrada> datosEntradaSubyacente = new ArrayList<>();
				IngresoCustodia.ObjDatosEntrada	datosSubyacente = new IngresoCustodia.ObjDatosEntrada();
				
				datosSubyacente.IdGenerarMovimientoCustodia			= 1L;
				datosSubyacente.TablaAfectada						= "SUBYACENTE";
				datosSubyacente.AccionOperacion						= "INGRESO";
				datosSubyacente.EsRegistroNuevo						= "S";
				datosSubyacente.IdMovimientoCarteraEnTransitoOrigen = movTransito.IdMovimientoCarteraEnTransito;
				datosSubyacente.FolioCarteraRelacionada				= FolioCustodia;
				datosSubyacente.IdCliente							= movTransito.IdCliente; 
				datosSubyacente.IdPortafolio						= movTransito.IdPortafolio;
				datosSubyacente.IdCuentaCustodioPortafolio		    = movTransito.IdCuentaCustodioPortafolio;				
				datosSubyacente.FechaOperacion						= movTransito.FechaOperacion;				
				datosSubyacente.FechaVencimientoOperacion			= movTransito.FechaVencimientoOperacion;
				datosSubyacente.FechaVencimientoInstrumento			= movTransito.FechaVencimientoInstrumento;				
				datosSubyacente.PlazoEnDiasOperacion				= movTransito.PlazoEnDiasOperacion;
				datosSubyacente.IdTipoOperacion						= movTransito.IdTipoOperacion;
				datosSubyacente.IdConcepto							= movTransito.IdConcepto;				
				datosSubyacente.IdCondicionLiquidacion				= movTransito.IdCondicionLiquidacion;				
				datosSubyacente.FechaLiquidacion					= movTransito.FechaLiquidacion;
				datosSubyacente.IdTipoCanalIngreso					= movTransito.IdTipoCanalIngreso; 		 		
				datosSubyacente.CodigoIFRS							= movTransito.CodigoIFRS;
				datosSubyacente.NemoSVS								= movTransito.NemoSVS;
				datosSubyacente.IdTipoComprobante					= movTransito.IdTipoComprobante;				
				datosSubyacente.FolioComprobante					= movTransito.FolioComprobante;
				datosSubyacente.FolioExternoReferencia				= movTransito.FolioExternoReferencia;				
				datosSubyacente.IdBolsa								= movTransito.IdBolsa;	
				datosSubyacente.IdUsuario							= movTransito.IdUsuario;
				datosSubyacente.IdSucursal							= movTransito.IdSucursal; 
				datosSubyacente.IdAgente							= movTransito.IdAgente;				
				datosSubyacente.Glosa								= movTransito.Glosa;
				datosSubyacente.IdInstrumento						= movTransito.IdInstrumento;
				datosSubyacente.IdMonedaInstrumento					= movTransito.IdMonedaInstrumento;
				datosSubyacente.IdMonedaEmision						= movTransito.IdMonedaEmision;
				datosSubyacente.IdTipoReajusteInstrumento			= movTransito.IdTipoReajuste;
				datosSubyacente.IdEmisor							= movTransito.IdEmisor;				
				datosSubyacente.EsFungible							= movTransito.EsFungible;
				datosSubyacente.IdBoveda							= movTransito.IdBoveda;
				datosSubyacente.EsNominal							= movTransito.EsNominal;		
				datosSubyacente.Cantidad							= movTransito.Cantidad;
				datosSubyacente.CorteMinimo							= movTransito.CorteMinimo; 
				datosSubyacente.ValorRescate						= movTransito.ValorRescate;
				datosSubyacente.PrecioCompra						= movTransito.PrecioCompra; 
				datosSubyacente.MontoCompra							= movTransito.MontoCompra;
				datosSubyacente.MontoCompraMonedaInstrumento		= movTransito.MontoCompraMonedaInstrumento;
				datosSubyacente.PrecioAPlazo						= movTransito.PrecioAPlazo;
				
				datosSubyacente.TasaCompra							= entrada.p_TasaInstrumento;
				datosSubyacente.TasaCompraOperacionRelacionada		= entrada.p_TasaPrima;
				datosSubyacente.UsaGarantiaEspecifica 				= entrada.p_ConGarantiaEspecifica;				
				datosSubyacente.CobraPrimaAlInicio               	= entrada.p_CobraPrimaAlInicio;
				datosSubyacente.MontoAPlazo 						= montoAPlazoLocal;
				datosSubyacente.MontoAPlazoMonedaInstrumento 		= entrada.p_MontoAPlazo;
				datosSubyacente.IdTipoPrestamo 					    = tipoPrestamo.get(0).IdTipoPrestamo;
				datosSubyacente.FolioSistemaExterno 				= folioSistemaExterno;
				datosEntradaSubyacente.add(datosSubyacente);
				
				
				
				
				//Ingreso del subyacente del prestamos
				IngresoCustodia ingresoCustodiaSubyacente = new IngresoCustodia();
				IngresoCustodia.IngresoCustodiaEntrada entradaIngresoSubyacente = new IngresoCustodia.IngresoCustodiaEntrada();
				
				entradaIngresoSubyacente.p_IdEmpresa				= entrada.p_IdEmpresa;		
				entradaIngresoSubyacente.p_IdModulo					= entrada.p_IdModulo;
				entradaIngresoSubyacente.p_IdUsuario				= entrada.p_IdUsuario;
				entradaIngresoSubyacente.p_SimularOperacion			= "N";
				entradaIngresoSubyacente.p_TicketAtencion 			= 0L;
				entradaIngresoSubyacente.p_ValorizacionAutomatica	= "S";
				entradaIngresoSubyacente.p_DatosEntrada				= datosEntradaSubyacente;
				IngresoCustodia.IngresoCustodiaSalida salidaIngresoSubyacente = ingresoCustodiaSubyacente.logica(con, entradaIngresoSubyacente);
				
				
				
				//Recorre y asigna mensajes de salida del ingresoCustodiaSubyacente a la salida de CrearPrestamo
				for (MensajeSalida msj : salidaIngresoSubyacente.P_OUT_MENSAJES) 
				{		
					salida.P_OUT_MENSAJES.add(msj);
				}
				
				
				for (IngresoCustodiaSalida.ObjDatosSalida datoSalida : salidaIngresoSubyacente.P_OUT_DATOSSALIDA) 
				{
						if (datoSalida.RegistroConProblemas.equalsIgnoreCase("N")) 
						{	
							salida.P_OUT_FOLIOSUBYACENTE = datoSalida.FolioCustodia;
							String codigo = "UI_CARTERA_CREADA_FOLIO"; //Cartera ::TipoCartera:: creada, Folio : ::FolioCartera::
							String descripcion = Diccionario.getMensaje(con, entrada.p_IdEmpresa, codigo).add("FolioCartera", datoSalida.FolioCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
							salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_INFORMACION, descripcion, null, con, entrada.p_IdEmpresa));
						}
						else 
						{	
							String codigo = "UI_ERROR_AL_CREAR_SUBYACENTE_MOVIMIENTO"; //Error al crear Cartera de ::TipoCartera::, movimiento ::IdMovimientoComprobante::
							String descripcion = Diccionario.getMensaje(con, entrada.p_IdEmpresa, codigo).add("IdMovimientoComprobante", datoSalida.IdGenerarMovimientoCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
							log.write(descripcion);
							salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, descripcion, null, con, entrada.p_IdEmpresa));
						}
				}

				if (MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES) > 0)	
				{
					log.write("ERROR EN EL INGRESO DEL SUBYACENTE");									
					return salida;					
				}		
							
				
			} // FIN - if (movTransito.IdMovimientoCarteraEnTransitoRelacionado.compareTo(0L) != 0 )
			
			
			
			
			//---------------------------------------------------------------------------------------------
			// MARCA LOS MOVIMIENTOS EN TRANSITO PROCESADOS (Para certificarlos)
			GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar MovTraAct = new GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar();
			MovTraAct.IdMovimientoCarteraEnTransito = movTransito.IdMovimientoCarteraEnTransito;
			MovTraAct.EsCertificacion = "S";
			MovTraAct.IdUsuario = entrada.p_IdUsuario;
			transitosOK.add(MovTraAct);

		}    // FIN - for (GestionMovimientoCarteraEnTransito.ObjMovimientosEnTransito mov : MovimientosEnTransito)

			
		//---------------------------------------------------------------------------------------------
		// CERTIFICAR MOVIMIENTOS EN TRANSITO
		
		if (transitosOK.size() > 0) 
		{
			log.write("CERTIFICANDO MOVIMIENTOS EN TRANSITO");
			GestionMovimientoCarteraEnTransito.ActualizarMovimientoEnTransito(con, transitosOK);				
		}

		
		salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
		
		log.write("PARAMETROS DE SALIDA : " + salida.toString());		
		log.write("== TERMINO CREAR PRESTAMO ==");
		
		return salida;
	}
	
	
	

	
	//-------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE - CALL
	//-------------------------------------------------------------------------------------------
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();
		CrearPrestamo_Entrada in = gson.fromJson(jin.toString(), CrearPrestamo_Entrada.class);
		CrearPrestamo_Salida out = logica(con,in);
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	
	
	//-------------------------------------------------
	// METODO PARA PRUEBAS LOCALES z
	//-------------------------------------------------
 
	public static void main(String[] args) throws Exception {
		System.out.println("== INICIO PRUEBA LOCAL ==");
		
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa2"))
		{
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();		
			
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdEmpleadoDigitador\":108,\"p_IdModulo\":\"CUS\",\"p_UsaReglasNegocio\":\"S\",\"p_IdClienteSolicita\":4846,\"p_IdPortafolioSolicita\":2224,\"p_IdBolsa\":\"BCS\",\"p_FechaMovimiento\":\"2024-08-21\",\"p_FechaVencimiento\":\"2024-08-28\",\"p_IdInstrumentoPrestamo\":558620,\"p_IdInstrumento\":70025,\"p_IdMercado\":\"RF\",\"p_Nemo\":\"BACEN-A1\",\"p_Cantidad\":10000,\"p_Precio\":106.2705,\"p_Monto\":123258718,\"p_MontoPrima\":1599076.4348533303,\"p_TasaPrima\":5.56,\"p_TasaInstrumento\":0.45,\"p_MontoAPlazo\":124857794.43485333,\"p_ConGarantiaEspecifica\":\"N\",\"p_FolioEntidad\":\"\",\"p_FolioTransaccion\":\"\",\"p_CobraPrimaAlInicio\":\"N\",\"p_TipoCustodia\":\"N\",\"p_IdAgente\":97,\"p_IdSucursal\":1,\"p_PlazoEnDias\":7,\"p_IdTipoCanalIngreso\":\"AGE\",\"p_IdMonedaSistema\":\"CLP\",\"p_esNominal\":\"N\",\"p_IdCondicionLiquidacion\":\"T0\",\"p_FechaLiquidacion\":\"2024-08-21\"}";
			
			// String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdEmpleadoDigitador\":108,\"p_IdModulo\":\"CUS\",\"p_UsaReglasNegocio\":\"S\",\"p_IdClienteSolicita\":4846,\"p_IdPortafolioSolicita\":2224,\"p_IdBolsa\":\"BCS\",\"p_FechaMovimiento\":\"2024-08-21\",\"p_FechaVencimiento\":\"2024-08-23\",\"p_IdInstrumentoPrestamo\":558620,\"p_IdInstrumento\":255932,\"p_IdMercado\":\"RF\",\"p_Nemo\":\"BAARA-A\",\"p_Cantidad\":2000,\"p_Precio\":125.2316,\"p_Monto\":86492985,\"p_MontoPrima\":135217.36654999852,\"p_TasaPrima\":2.345,\"p_TasaInstrumento\":0.5667,\"p_MontoAPlazo\":86628202.36655,\"p_ConGarantiaEspecifica\":\"N\",\"p_FolioEntidad\":\"\",\"p_FolioTransaccion\":\"\",\"p_CobraPrimaAlInicio\":\"N\",\"p_TipoCustodia\":\"N\",\"p_IdAgente\":97,\"p_IdSucursal\":1,\"p_PlazoEnDias\":2,\"p_IdTipoCanalIngreso\":\"AGE\",\"p_IdMonedaSistema\":\"CLP\",\"p_esNominal\":\"N\",\"p_IdCondicionLiquidacion\":\"T0\",\"p_FechaLiquidacion\":\"2024-08-21\"}";
			
			CrearPrestamo_Entrada Objin = gson.fromJson(json, CrearPrestamo_Entrada.class);
			CrearPrestamo demo = new CrearPrestamo();
 			
			System.out.println("ENTRADA:" + Objin.toString());
			
 			try
 			{
 				CrearPrestamo_Salida ObjOut = demo.logica(con, Objin);
 				System.out.println("SALIDA:" + ObjOut);
 				con.commit();
 				//con.rollback();
 			}
 			catch(Exception er) {
				er.printStackTrace();
				con.rollback();
			}

	    }

		System.out.println("== FIN PRUEBA LOCAL ==");
	}
	
	
	
}
