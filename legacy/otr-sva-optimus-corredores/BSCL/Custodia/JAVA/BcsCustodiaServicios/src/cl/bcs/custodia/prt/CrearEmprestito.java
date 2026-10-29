package cl.bcs.custodia.prt;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.utils.GestionMovimientoCarteraEnTransito;
import cl.bcs.custodia.utils.IngresoCustodia;
import cl.bcs.custodia.utils.RetiroCustodia;
import cl.bcs.custodia.utils.GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar;
import cl.bcs.custodia.utils.IngresoCustodia.IngresoCustodiaSalida;
import cl.bcs.plataforma.bat.AgregarPrograma;
import cl.bcs.plataforma.bat.AgregarPrograma.InAgregarPrograma.Parametro;
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
import cl.bcs.socionegocio.bck.AsignaPrestamistaBlockchain;
import cl.bcs.socionegocio.bck.ConsultaDataCorredoraBlockChain;
import cl.bcs.socionegocio.bck.AsignaPrestamistaBlockchain.AsignaPrestamistaBlockchain_Entrada;
import cl.bcs.socionegocio.bck.AsignaPrestamistaBlockchain.AsignaPrestamistaBlockchain_Salida;
import cl.bcs.socionegocio.bck.ContratoEsBlockChainFirmado;
import cl.bcs.socionegocio.bck.ContratoEsBlockChainFirmado.ConsultaEstadoCntBC;
import cl.bcs.socionegocio.bck.ContratoEsBlockChainFirmado.ContratoEsBlockChainFirmado_Entrada;
import cl.bcs.socionegocio.bck.ContratoEsBlockChainFirmado.ContratoEsBlockChainFirmado_Salida;
import cl.bcs.socionegocio.pojo.ConstantesBCK;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearEmprestito implements JavaCall {
	// -------------------------------------------------
	// LOG
	// -------------------------------------------------
	private final Log log = new Log("CUS.CrearEmprestito");

	// -------------------------------------------------
	// QUERIES
	// -------------------------------------------------
	private final String SQL_SELECT_VALIDA_CONTRATO_EMPRESTITO 	= loadResourceToString("cl/bcs/custodia/sql/CrearEmprestito_SelectValidaContratoEmprestito.sql");
	private final String SQL_SELECT_CONSULTAR_INSTRUMENTO 		= loadResourceToString("cl/bcs/custodia/sql/CrearEmprestito_SelectInstrumento.sql");
	private final String SQL_SELECT_PRESTAMO 					= loadResourceToString("cl/bcs/custodia/sql/CrearEmprestito_SelectPrestamo.sql");
	private final String SQL_SELECT_INSTRUMENTO_EMPRESTITO 		= loadResourceToString("cl/bcs/custodia/sql/CrearEmprestito_SelectInstrumentoEmprestito.sql");
	private final String SQL_SELECT_EMPLEADO 					= loadResourceToString("cl/bcs/custodia/sql/CrearEmprestito_SelectEmpleado.sql");
	private final String SQL_SELECT_VALIDA_CONTRATO_CUSTODIA 	= loadResourceToString("cl/bcs/custodia/sql/CrearEmprestito_SelectValidaContratoCustodia.sql");
	private final String UPD_EXIGIR_GARANTIA 					= loadResourceToString("cl/bcs/custodia/sql/CrearEmprestito_UpdateExigirGarantia.sql");
	private final String SQL_INSERT_LOG_PROCESO_BLOCKCHAIN 		= loadResourceToString("cl/bcs/socionegocio/bck/sql/ProcesaLogBlockChain_InsertLogProcesoPrestamoBlockChain.sql");
	
	// -------------------------------------------------
	// CLASES DE ENTRADA
	// -------------------------------------------------
	public static class CrearEmprestito_Entrada {
		public Long p_IdEmpresa = 0L;
		public Long p_IdUsuario = 0L;
		public String p_IdModulo = "";
		public String p_UsaReglasNegocio = "N";
		public Long p_IdClientePrestamista = 0L;
		public Long p_IdPortafolioPrestamista = 0L;
		public String p_FechaMovimiento = "0001-01-01";
		public String p_FechaVencimiento = "0001-01-01";
		public String p_FechaLiquidacion = "0001-01-01";
		public String p_IdCondicionDeLiquidacion = "";
		public Long p_IdInstrumento = 0L; // IdInstrumento Subyacente
		public String p_Nemo = "";
		public BigDecimal p_Cantidad = new BigDecimal(0);
		public BigDecimal p_Precio = new BigDecimal(0);
		public BigDecimal p_TasaInstrumento = new BigDecimal(0);
		public BigDecimal p_Monto = new BigDecimal(0);
		public BigDecimal p_MontoPremio = new BigDecimal(0);
		public BigDecimal p_TasaPremio = new BigDecimal(0);
		public BigDecimal p_MontoAPlazo = new BigDecimal(0);
		public Long p_FolioCarteraPrestamo = 0L;
		public String p_PrestaTitulosNominales = "N";
		public Long p_IdAgente = 0L;
		public Long p_PlazoEnDias = 0L;
		public String p_IdTipoCanalIngreso = "";
		public String p_IdMonedaSistema = "";
		public String p_EsFungible = "N";
		public Long p_IdEmpleado = 0L;
		public String p_TipoCustodia = "";
		public String p_ExigirGarantia = "";
		public String p_VieneDesdeBlockChain = "N"; 
		public String p_EsPrestamistaExterno = "";
		public BigDecimal p_Cantidad_Asignar = new BigDecimal(0);
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// -------------------------------------------------
	// CLASES DE SALIDA
	// -------------------------------------------------
	public static class CrearEmprestito_Salida {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		public Long P_OUT_FOLIOCARTERA;
		public Long P_OUT_FOLIOSUBYACENTE;

		public CrearEmprestito_Salida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// -------------------------------------------------
	// CLASES UTILITARIAS
	// -------------------------------------------------
	public static class ContratoEmprestito {
		public String ValorIndicador;
	}
	public static class ContratoCustodia {
		public String ValorIndicador;
	}
	public static class PrestamistaSinCustodia {
		public Long IdPrestamista;
	}
	public static class Instrumento {
		public String IdMoneda = "CLP";
		public String IdTipoInstrumento = "";
		public String IdClasificacionTipoInstrumento = "";
		public String IdTipoReajuste = "";
		public String IdEmisor = "";
		public java.sql.Date FechaEmision;
		public java.sql.Date FechaVencimiento;
		public BigDecimal CorteMinimo = new BigDecimal(0);
		public String Nemotecnico = "";
		public Long IdInstrumento = 0L;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class Prestamo {
		public Long IdCliente = 0L;
		public Long IdPortafolio = 0L;
		public Long FolioComprobante = 0L;
		public String IdTipoComprobante = "";
		public java.sql.Date FechaComprobante;
		public String IdTipoPrestamo = "";
		public String UsaGarantiaEspecifica = "N";
		public String IdBolsa = "";
		public Long FolioPrestamo = 0L;
		public String FolioSistemaExterno = "";
		public String TipoCustodia = "";
		public String ExigirGarantia = "";
		public String IdBoveda;
		public String ConCustodiaPrestatario;
		public String IdTipoInstrumentoEmprestito;
		public String NeteaCarteraSobregiro;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class InstrumentoEmprestito {
		public Long IdInstrumento = 0L;
		public String Nemotecnico = "";
		public String IdEmisor = "";
		public String IdMoneda = "";
		public String IdMonedaEmision = "";
		public String IdTipoReajuste = "";
		public BigDecimal CorteMinimo = new BigDecimal(0);
		public java.sql.Date FechaEmisionInstrumento;
		public java.sql.Date FechaVencimiento;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class Empleado {
		public Long IdEmpleado = 0L;
		public Long IdSucursal = 0L;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class UpdateExigirGarantia
	{
		public String ExigirGarantia;
		public Long FolioCartera;
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

	public static class EntradaNeteo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_SimularOperacion;
		public List<ClienteNeteo> p_PortafoliosInstrumento;
	}
	public static class ClienteNeteo {
		public Long IdPortafolio;
		public Long IdInstrumento;
	}
	
	// -------------------------------------------------
	// LOGICA DEL SERVICIO
	// -------------------------------------------------
	public CrearEmprestito_Salida logica(Connection con, CrearEmprestito_Entrada entrada) throws Exception {
		SQL sql = new SQL();
		CrearEmprestito_Salida salida = new CrearEmprestito_Salida();
		
		String monedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");

		List<Long> TransitosPorCertificar = new ArrayList<>();
		List<ObjMovimientosAActualizar> transitosOK = new ArrayList<>();

		log.write("== INICIO EJECUCION CREAR EMPRESTITO ==");

		// ---------------------------------------------------------------------------------------------
		// VALIDA SI CUMPLE REQUISITOS BASICOS
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			log.write("NO CUMPLE REQUISITOS BASICOS");
			salida.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			return salida;
		}
		
		/*** INFORMACION CORREDORA ***/
		ConsultaDataCorredoraBlockChain consultaDataCorredora = new ConsultaDataCorredoraBlockChain();
		ConsultaDataCorredoraBlockChain.ConsultaDataCorredoraBlockChain_Entrada entradaConsultaDataCorredora = new ConsultaDataCorredoraBlockChain.ConsultaDataCorredoraBlockChain_Entrada();
		entradaConsultaDataCorredora.p_IdEmpresa = entrada.p_IdEmpresa;
		ConsultaDataCorredoraBlockChain.ConsultaDataCorredoraBlockChain_Salida salidaConsultaDataCorredora = consultaDataCorredora.logica(con, entradaConsultaDataCorredora);

		// si p_VieneDesdeBlockChain == S significa que se esta llamando este servicio desde proceso que leyo desde BlockChain los prestamistas
		// por lo que debe saltarse cualquier validacion BlockChain
		String EsBlockChain = null;
		String ContratoFirmado = null;
		
		// ---------------------------------------------------------------------------------------------
		// ---------------------------------------------------------------------------------------------
		// B L O C K C H A I N
		// ---------------------------------------------------------------------------------------------
		// ---------------------------------------------------------------------------------------------
		if (entrada.p_VieneDesdeBlockChain.equals("N")) {			
			// ---------------------------------------------------------------------------------------------
			// VALIDAR QUE EL CONTRATO ESTE CONFIGURADO COMO BLOCKCHAIN -------
			ContratoEsBlockChainFirmado ContratoEsBlockChain = new ContratoEsBlockChainFirmado();
			ContratoEsBlockChainFirmado_Entrada ContratoEsBlockChainEntrada = new ContratoEsBlockChainFirmado_Entrada();
			ContratoEsBlockChainEntrada.p_IdEmpresa 	= entrada.p_IdEmpresa;
			ContratoEsBlockChainEntrada.p_IdUsuario 	= entrada.p_IdUsuario;
			ContratoEsBlockChainEntrada.p_ClaveUsuario 	= "";
			
			ConsultaEstadoCntBC ConsultaEstadoCntBC = new ConsultaEstadoCntBC();
			ConsultaEstadoCntBC.p_IdTipoDocumento 				= ConstantesBCK.IDCONTRATOPRESTAMOANEXO43;
			ConsultaEstadoCntBC.p_IdCliente 					= entrada.p_IdClientePrestamista.toString();
			ConsultaEstadoCntBC.p_BrokerageRUT 					= salidaConsultaDataCorredora.p_Out_BrokerageRUT;
			ConsultaEstadoCntBC.p_IdContrato 					= ConstantesBCK.CONTRATOPRESTAMOANEXO43;
			ContratoEsBlockChainEntrada.p_ConsultaEstadoCntBC 	= ConsultaEstadoCntBC;
			ContratoEsBlockChainFirmado_Salida ContratoEsBlockChainFirmado_Salida = ContratoEsBlockChain.logica(con,ContratoEsBlockChainEntrada);

			for (ContratoEsBlockChainFirmado.Resultado ResultadoBC : ContratoEsBlockChainFirmado_Salida.P_OUT_RESULTADOPROCESO) {
				EsBlockChain = ResultadoBC.EsBlockChain;
				ContratoFirmado = ResultadoBC.ContratoFirmado;
				break;
			}

			if (EsBlockChain.equals(ConstantesBCK.SI) && ContratoFirmado.equals(ConstantesBCK.NO)) {
				String p_Mensaje = null;
				for (MensajeSalida ResultadoMSJ : ContratoEsBlockChainFirmado_Salida.P_OUT_MENSAJES) {
					p_Mensaje = ResultadoMSJ.descripcion;
					break;
				}
				// Cliente sin contrato de VC firmado
				log.write("NO TIENE CONTRATO FIRMADO");
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, p_Mensaje, null, con, entrada.p_IdEmpresa));
				return salida;
			}
			
			// ---------------------------------------------------------------------------------------------
			// VALIDAR CLIENTE TENGA CONTRATO EMPRESTITO
			if (EsBlockChain.equals("N")) {
				HashMap<String, Object> ParamValidar = new HashMap<String, Object>();
				ParamValidar.put("p_IdClientePrestamista", entrada.p_IdClientePrestamista);
				
				HashMap<String,String> valores = new HashMap<String,String>();
				List<ContratoEmprestito> ContratoEmprestito = sql.query(con, SQL_SELECT_VALIDA_CONTRATO_EMPRESTITO,	ParamValidar, ContratoEmprestito.class);

				if (ContratoEmprestito.get(0).ValorIndicador.equals("N")) {
					log.write("CLIENTE_SIN_CONTRATO_EMPRESTITOS");

					salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"CLIENTE_SIN_CONTRATO_EMPRESTITOS", valores, con, entrada.p_IdEmpresa));
					salida.P_OUT_ERRORESCONTADOR = 1L;
					return salida;
				}
				
				/*VALIDA QUE CLIENTE NO TENGA CONTRATO DE CUSTODIA SI ES PRESTAMISTA EXTERNO*/
				if(entrada.p_EsPrestamistaExterno.equals("S")) {
					List<ContratoCustodia> ContratoCustodia = sql.query(con, SQL_SELECT_VALIDA_CONTRATO_CUSTODIA,	ParamValidar, ContratoCustodia.class);

					if (ContratoCustodia.get(0).ValorIndicador.equals("S")) {
						log.write("CLIENTE_CON_CONTRATO_CUSTODIA");

						salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"CLIENTE_CON_CONTRATO_CUSTODIA", valores, con, entrada.p_IdEmpresa));
						salida.P_OUT_ERRORESCONTADOR = 1L;
						return salida;
					}
				}
			}
		} 
		// ---------------------------------------------------------------------------------------------
		// ---------------------------------------------------------------------------------------------
		// B L O C K C H A I N
		// ---------------------------------------------------------------------------------------------
		// ---------------------------------------------------------------------------------------------

		
		// OBTIENE DATOS DEL INSTRUMENTO
		HashMap<String, Object> paramsInstrumento = new HashMap<String, Object>();
		paramsInstrumento.put("p_IdInstrumento", entrada.p_IdInstrumento);
		List<Instrumento> Instrumento = sql.query(con, SQL_SELECT_CONSULTAR_INSTRUMENTO, paramsInstrumento, Instrumento.class);

		if (Instrumento.size() == 0) {
			log.write("NO SE ENCONTRARON DATOS PARA EL INSTRUMENTO");
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"NO_SE_ENCONTRARON_DATOS_PARA_INSTRUMENTO", null, con, entrada.p_IdEmpresa));
			salida.P_OUT_ERRORESCONTADOR = 1L;
			return salida;
		} else {
			log.write("DATOS INSTRUMENTO SUBYACENTE > " + Instrumento);
		}

		// ---------------------------------------------------------------------------------------------
		// OBTIENE DATOS DEL PRESTAMO ASOCIADO (busca el cliente que recibira los titulos en prestamo)
		HashMap<String, Object> paramsPrestamo = new HashMap<String, Object>();
		paramsPrestamo.put("p_FolioCarteraPrestamo", entrada.p_FolioCarteraPrestamo);
		String tipoPrestamo = "";
		List<Prestamo> Prestamo = sql.query(con, SQL_SELECT_PRESTAMO, paramsPrestamo, Prestamo.class);

		if (Prestamo.size() == 0) {
			log.write("NO SE ENCONTRARON DATOS DEL PRESTAMO ASOCIADO");
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "NO_SE_ENCONTRARON_DATOS_DEL_PRESTAMO_ASOCIADO", null, con, entrada.p_IdEmpresa));
			salida.P_OUT_ERRORESCONTADOR = 1L;
			return salida;
		} else {
			log.write("DATOS DEL PRESTAMO > " + Prestamo);
			tipoPrestamo = Prestamo.get(0).IdTipoPrestamo;
		}

		// ---------------------------------------------------------------------------------------------
		// OBTIENE DATOS INSTRUMENTO EMPRESTITO
		HashMap<String, Object> parametrosEmprestito = new HashMap<String, Object>();
		parametrosEmprestito.put( "p_IdTipoInstrumento", Prestamo.get(0).IdTipoInstrumentoEmprestito );
		
		List<InstrumentoEmprestito> InstrumentoEmprestito = sql.query(con, SQL_SELECT_INSTRUMENTO_EMPRESTITO, parametrosEmprestito, InstrumentoEmprestito.class);
		
		if (InstrumentoEmprestito.size() == 0) {
			log.write("NO SE ENCONTRARON DATOS DEL INSTRUMENTO EMPRESTITO");
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "NO_SE_ENCONTRARON_DATOS_PARA_INSTRUMENTO_EMPRESTITO", null, con, entrada.p_IdEmpresa));
			salida.P_OUT_ERRORESCONTADOR = 1L;
			return salida;
		} else {
			log.write("DATOS INSTRUMENTO EMPRESTITO > " + InstrumentoEmprestito);
		}

		// ---------------------------------------------------------------------------------------------
		// OBTIENE DATOS DEL EMPLEADO
		HashMap<String, Object> paramsEmp = new HashMap<String, Object>();
		paramsEmp.put("p_IdEmpleado", entrada.p_IdEmpleado);

		List<Empleado> Empleado = sql.query(con, SQL_SELECT_EMPLEADO, paramsEmp, Empleado.class);

		if (Empleado.size() == 0) {
			log.write("NO SE ENCONTRARON DATOS DEL EMPLEADO");
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "NO_SE_ENCONTRARON_DATOS_DEL_EMPLEADO", null, con, entrada.p_IdEmpresa));
			salida.P_OUT_ERRORESCONTADOR = 1L;
			return salida;
		} else {
			log.write("DATOS DEL EMPLEADO > " + Empleado);
		}

		// ---------------------------------------------------------------------------------------------
		// VALIDA QUE EL PRESTAMISTA SEA DISTINTO AL PRESTATARIO
		if (entrada.p_IdClientePrestamista.compareTo(Prestamo.get(0).IdCliente) == 0
				&& entrada.p_IdPortafolioPrestamista.compareTo(Prestamo.get(0).IdPortafolio) == 0) {
			log.write("EL PRESTAMISTA DEBE SER DISTINTO AL PRESTATARIO");
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "PRESTAMISTA_DEBE_SER_DISTINTO_A_PRESTATARIO", null, con, entrada.p_IdEmpresa));
			salida.P_OUT_ERRORESCONTADOR = 1L;
			return salida;
		}
		
		

		// CALCULO DE PARIDAD PARA MONEDAS DIFERENTES A SISTEMA
		CalcularParidad calculoParidad = new CalcularParidad();
		CalcularParidadEntrada calcularParidadEntrada = new CalcularParidadEntrada();
		calcularParidadEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
		calcularParidadEntrada.p_IdTrabajo = 0L;
		
		CalculoEntrada calculoEntrada = new CalculoEntrada();
		calculoEntrada.Fecha = entrada.p_FechaMovimiento;
		calculoEntrada.IdInterno = 1L;
		calculoEntrada.IdMonedaOrigen = Instrumento.get(0).IdMoneda;
		calculoEntrada.IdMonedaDestino = monedaSistema;
		calculoEntrada.IdTrabajo = 1L;
		calculoEntrada.IdUso = "";
		calculoEntrada.MontoOrigen = entrada.p_Monto;
		calculoEntrada.ValorCambioLocalModificado = BigDecimal.ZERO;
		
		CalculoEntrada calculoEntradaPlazo = new CalculoEntrada();
		calculoEntradaPlazo.Fecha = entrada.p_FechaMovimiento;
		calculoEntradaPlazo.IdInterno = 2L;
		calculoEntradaPlazo.IdMonedaOrigen = Instrumento.get(0).IdMoneda;
		calculoEntradaPlazo.IdMonedaDestino = monedaSistema;
		calculoEntradaPlazo.IdTrabajo = 2L;
		calculoEntradaPlazo.IdUso = "";
		calculoEntradaPlazo.MontoOrigen = entrada.p_MontoAPlazo;
		calculoEntradaPlazo.ValorCambioLocalModificado = BigDecimal.ZERO;
		
		calcularParidadEntrada.p_CalculosEntrada.add(calculoEntrada);
		calcularParidadEntrada.p_CalculosEntrada.add(calculoEntradaPlazo);
		
		CalcularParidadSalida calculoSalida = calculoParidad.logica(con,calcularParidadEntrada);
		HashMap<Long,Object> hCalculoSalida = Utils.List2Hash(calculoSalida.p_out_CalculosSalida, "IdInterno");
		
		BigDecimal montoLocal = ((CalcularParidad.CalculoSalida)hCalculoSalida.get(1L)).MontoLocal;
		BigDecimal montoAPlazoLocal = ((CalcularParidad.CalculoSalida)hCalculoSalida.get(2L)).MontoLocal;
		
		if (calculoSalida.P_OUT_ERRORESCONTADOR > 0) {
			salida.P_OUT_ERRORESCONTADOR = calculoSalida.P_OUT_ERRORESCONTADOR;
			return salida;
		}
		
		// ---------------------------------------------------------------------------------------------
		// CREA MOVIMIENTOS EN TRANSITO PARA CARTERA Y SUBYACENTE DE INGRESO
		List<IngresoCustodia.ObjDatosEntrada> DatosMovimientoTransito = new ArrayList<>();

		// ********** DATOS PARA RETIRO CARTERA PRESTAMISTA  (Retiro de titulo, prestamista con custodia)
		if("N".equalsIgnoreCase(entrada.p_EsPrestamistaExterno)) {
			IngresoCustodia.ObjDatosEntrada ObjRetiroTitulos = new IngresoCustodia.ObjDatosEntrada();		
			ObjRetiroTitulos.IdGenerarMovimientoCustodia 	= -100L;
			ObjRetiroTitulos.TablaAfectada 					= "TRANSITO";
			ObjRetiroTitulos.AccionOperacion 				= "RETIRO";	
			ObjRetiroTitulos.EsRegistroNuevo				= "S";		
			ObjRetiroTitulos.IdTipoOperacion				= "RETTIT";
			ObjRetiroTitulos.IdConcepto 					= "INGEMP";
			ObjRetiroTitulos.PermiteCertificacionPorUsuario = "S";
			ObjRetiroTitulos.EsPendienteAutorizacion 		= "S";
			ObjRetiroTitulos.Glosa 							= "EMPRESTITO " + entrada.p_Nemo;
			ObjRetiroTitulos.IdCliente 						= entrada.p_IdClientePrestamista;
			ObjRetiroTitulos.IdPortafolio 					= entrada.p_IdPortafolioPrestamista;
			ObjRetiroTitulos.FechaOperacion					= java.sql.Date.valueOf(entrada.p_FechaMovimiento);
			ObjRetiroTitulos.FechaLiquidacion               = java.sql.Date.valueOf(entrada.p_FechaMovimiento);
			
			ObjRetiroTitulos.EsNominal						= "N";
			ObjRetiroTitulos.IdInstrumento 					= entrada.p_IdInstrumento;
			ObjRetiroTitulos.IdBoveda 						= Prestamo.get(0).IdBoveda;
			ObjRetiroTitulos.Cantidad 						= entrada.p_Cantidad;
			ObjRetiroTitulos.ValorRescate 					= BigDecimal.ZERO;
			ObjRetiroTitulos.CorteMinimo 					= Instrumento.get(0).CorteMinimo;
			ObjRetiroTitulos.PrecioCompra 					= entrada.p_Precio;
			ObjRetiroTitulos.TasaCompra 					= entrada.p_TasaInstrumento;
			ObjRetiroTitulos.MontoCompra 					= montoLocal;
			ObjRetiroTitulos.MontoCompraMonedaInstrumento 	= entrada.p_Monto;
			ObjRetiroTitulos.IdMonedaEmision				= Instrumento.get(0).IdMoneda;
			ObjRetiroTitulos.IdSucursal 					= Empleado.get(0).IdSucursal;
			ObjRetiroTitulos.IdAgente 						= entrada.p_IdAgente;
			ObjRetiroTitulos.IdTipoCanalIngreso 			= entrada.p_IdTipoCanalIngreso;
			DatosMovimientoTransito.add(ObjRetiroTitulos);
		}
		
		
		// ********** DATOS PARA CARTERA PRESTAMISTA  (Cartera del emprestito)
		IngresoCustodia.ObjDatosEntrada ObjEntradaCartera = new IngresoCustodia.ObjDatosEntrada();		
		ObjEntradaCartera.IdGenerarMovimientoCustodia 	= 1L;
		ObjEntradaCartera.TablaAfectada 				= "TRANSITO";
		ObjEntradaCartera.AccionOperacion 				= "INGRESO";
		ObjEntradaCartera.FolioOperacionRelacionada 	= entrada.p_FolioCarteraPrestamo;
		ObjEntradaCartera.IdCliente 					= entrada.p_IdClientePrestamista;
		ObjEntradaCartera.IdPortafolio 					= entrada.p_IdPortafolioPrestamista;
		ObjEntradaCartera.IdClienteRelacionado 			= Prestamo.get(0).IdCliente;
		ObjEntradaCartera.IdPortafolioRelacionado 		= Prestamo.get(0).IdPortafolio;
		ObjEntradaCartera.IdTipoProducto 				= "PRES";
		ObjEntradaCartera.IdTipoCanalIngreso 			= entrada.p_IdTipoCanalIngreso;
		ObjEntradaCartera.IdConcepto 					= "INGEMP";
		ObjEntradaCartera.IdTipoOperacion 				= "EMPRES";
		ObjEntradaCartera.FechaOperacion 				= java.sql.Date.valueOf(entrada.p_FechaMovimiento);
		ObjEntradaCartera.FechaVencimientoOperacion 	= java.sql.Date.valueOf(entrada.p_FechaVencimiento);
		ObjEntradaCartera.PlazoEnDiasOperacion 			= entrada.p_PlazoEnDias;
		ObjEntradaCartera.FolioComprobante 				= Prestamo.get(0).FolioComprobante;
		ObjEntradaCartera.IdTipoComprobante 			= Prestamo.get(0).IdTipoComprobante;
		ObjEntradaCartera.FechaComprobante 				= Prestamo.get(0).FechaComprobante;
		ObjEntradaCartera.IdBolsa 						= Prestamo.get(0).IdBolsa;
		ObjEntradaCartera.IdSucursal 					= Empleado.get(0).IdSucursal;
		ObjEntradaCartera.FechaVencimientoInstrumento 	= Instrumento.get(0).FechaVencimiento;
		ObjEntradaCartera.FechaEmisionInstrumento 		= Instrumento.get(0).FechaEmision;
		ObjEntradaCartera.IdMonedaEmision 				= Instrumento.get(0).IdMoneda;
		ObjEntradaCartera.IdAgente 						= entrada.p_IdAgente;
		ObjEntradaCartera.IdCondicionLiquidacion 		= entrada.p_IdCondicionDeLiquidacion;
		ObjEntradaCartera.FechaLiquidacion 				= java.sql.Date.valueOf(entrada.p_FechaLiquidacion);
		ObjEntradaCartera.UsaGarantiaEspecifica 		= Prestamo.get(0).UsaGarantiaEspecifica;
		ObjEntradaCartera.CobraPrimaAlInicio 			= "N";
		ObjEntradaCartera.IdTipoPrestamo 				= Prestamo.get(0).IdTipoPrestamo;
		ObjEntradaCartera.IdInstrumento 				= InstrumentoEmprestito.get(0).IdInstrumento;
		ObjEntradaCartera.Cantidad 						= entrada.p_Cantidad;
		ObjEntradaCartera.CorteMinimo 					= Instrumento.get(0).CorteMinimo;
		ObjEntradaCartera.PrecioCompra 					= entrada.p_Precio;
		ObjEntradaCartera.TasaCompra 					= entrada.p_TasaPremio;
		ObjEntradaCartera.MontoCompra 					= montoLocal;
		ObjEntradaCartera.MontoCompraMonedaInstrumento 	= entrada.p_Monto;
		ObjEntradaCartera.PrecioAPlazo 					= entrada.p_MontoPremio;
		ObjEntradaCartera.MontoAPlazo 					= montoAPlazoLocal;
		ObjEntradaCartera.MontoAPlazoMonedaInstrumento 	= entrada.p_MontoAPlazo;
		ObjEntradaCartera.Nemotecnico 					= entrada.p_Nemo;
		ObjEntradaCartera.Nemotecnico 					= InstrumentoEmprestito.get(0).Nemotecnico;
		ObjEntradaCartera.EsFungible 					= entrada.p_EsFungible;
		ObjEntradaCartera.EsNominal 					= entrada.p_PrestaTitulosNominales;
		ObjEntradaCartera.IdMonedaInstrumento 			= Instrumento.get(0).IdMoneda;
		ObjEntradaCartera.IdMonedaEmision 				= Instrumento.get(0).IdMoneda;
		ObjEntradaCartera.IdEmisor 						= InstrumentoEmprestito.get(0).IdEmisor;
		ObjEntradaCartera.IdBoveda 						= Prestamo.get(0).IdBoveda;
		if(entrada.p_EsPrestamistaExterno.equals("S"))
			ObjEntradaCartera.EsCustodiaExterna=entrada.p_EsPrestamistaExterno;		
		ObjEntradaCartera.FolioCarteraOrigen 			= 0L;
		ObjEntradaCartera.EsRegistroNuevo 				= "S";
		ObjEntradaCartera.EsVentaCorta 					= "N";
		ObjEntradaCartera.Glosa 						= "EMPRESTITO " + entrada.p_Nemo;
		ObjEntradaCartera.ExigirGarantia 				= entrada.p_ExigirGarantia;
		ObjEntradaCartera.IdInstrumentoSubyacente       = entrada.p_IdInstrumento; 
		ObjEntradaCartera.NemotecnicoSubyacente 		= entrada.p_Nemo;
		ObjEntradaCartera.IdTipoInstrumentoSubyacente 	= Instrumento.get(0).IdTipoInstrumento;
		ObjEntradaCartera.TasaCompraInstrumento		    = entrada.p_TasaInstrumento;
		ObjEntradaCartera.IdTipoReajusteInstrumento     = Instrumento.get(0).IdTipoReajuste;
		DatosMovimientoTransito.add(ObjEntradaCartera);
		
		
		// ********** DATOS PARA CARTERA PRESTATARIO
		if(!(Prestamo.get(0).IdTipoPrestamo.equalsIgnoreCase("PRTVTC") || Prestamo.get(0).IdTipoPrestamo.equalsIgnoreCase("PRVCRF")) && "S".equals(Prestamo.get(0).ConCustodiaPrestatario)) {
			IngresoCustodia.ObjDatosEntrada ObjEntradaCarteraPrestatario = new IngresoCustodia.ObjDatosEntrada();			
			ObjEntradaCarteraPrestatario.IdGenerarMovimientoCustodia = 2L;
			ObjEntradaCarteraPrestatario.TablaAfectada = "TRANSITO";
			ObjEntradaCarteraPrestatario.AccionOperacion = "INGRESO";
			ObjEntradaCarteraPrestatario.FolioOperacionRelacionada = entrada.p_FolioCarteraPrestamo;
			ObjEntradaCarteraPrestatario.IdCliente = Prestamo.get(0).IdCliente;
			ObjEntradaCarteraPrestatario.IdPortafolio = Prestamo.get(0).IdPortafolio;
			ObjEntradaCarteraPrestatario.IdClienteRelacionado = entrada.p_IdClientePrestamista;
			ObjEntradaCarteraPrestatario.IdPortafolioRelacionado = entrada.p_IdPortafolioPrestamista;
			ObjEntradaCarteraPrestatario.IdTipoProducto = "CUST";
			ObjEntradaCarteraPrestatario.IdTipoCanalIngreso = entrada.p_IdTipoCanalIngreso;
			ObjEntradaCarteraPrestatario.IdConcepto = "INGEMP";
			ObjEntradaCarteraPrestatario.IdTipoOperacion = "EMPRES";
			ObjEntradaCarteraPrestatario.FechaOperacion = java.sql.Date.valueOf(entrada.p_FechaMovimiento);
			ObjEntradaCarteraPrestatario.FechaVencimientoOperacion = java.sql.Date.valueOf(entrada.p_FechaVencimiento);
			ObjEntradaCarteraPrestatario.PlazoEnDiasOperacion = entrada.p_PlazoEnDias;
			ObjEntradaCarteraPrestatario.FolioComprobante = Prestamo.get(0).FolioComprobante;
			ObjEntradaCarteraPrestatario.IdTipoComprobante = "CUS";
			ObjEntradaCarteraPrestatario.FechaComprobante = Prestamo.get(0).FechaComprobante;
			ObjEntradaCarteraPrestatario.IdBolsa = Prestamo.get(0).IdBolsa;
			ObjEntradaCarteraPrestatario.IdSucursal = Empleado.get(0).IdSucursal;
			ObjEntradaCarteraPrestatario.IdAgente = entrada.p_IdAgente;
			ObjEntradaCarteraPrestatario.IdCondicionLiquidacion = entrada.p_IdCondicionDeLiquidacion;
			ObjEntradaCarteraPrestatario.FechaLiquidacion = java.sql.Date.valueOf(entrada.p_FechaLiquidacion);
			ObjEntradaCarteraPrestatario.UsaGarantiaEspecifica = Prestamo.get(0).UsaGarantiaEspecifica;
			ObjEntradaCarteraPrestatario.CobraPrimaAlInicio = "";
			ObjEntradaCarteraPrestatario.IdTipoPrestamo = "";
			ObjEntradaCarteraPrestatario.IdInstrumento = Instrumento.get(0).IdInstrumento;
			ObjEntradaCarteraPrestatario.IdInstrumentoAnterior = Instrumento.get(0).IdInstrumento;
			ObjEntradaCarteraPrestatario.Cantidad = entrada.p_Cantidad;
			ObjEntradaCarteraPrestatario.CorteMinimo = Instrumento.get(0).CorteMinimo;
			ObjEntradaCarteraPrestatario.PrecioCompra = entrada.p_Precio;
			ObjEntradaCarteraPrestatario.TasaCompra = entrada.p_TasaInstrumento;
			ObjEntradaCarteraPrestatario.MontoCompra = montoLocal;
			ObjEntradaCarteraPrestatario.MontoCompraMonedaInstrumento = entrada.p_Monto;
			ObjEntradaCarteraPrestatario.PrecioAPlazo = entrada.p_MontoPremio;
			ObjEntradaCarteraPrestatario.MontoAPlazo = montoAPlazoLocal;
			ObjEntradaCarteraPrestatario.MontoAPlazoMonedaInstrumento = entrada.p_MontoAPlazo;
			ObjEntradaCarteraPrestatario.Nemotecnico = Instrumento.get(0).Nemotecnico;
			ObjEntradaCarteraPrestatario.EsFungible = entrada.p_EsFungible;
			ObjEntradaCarteraPrestatario.EsNominal = entrada.p_PrestaTitulosNominales;
			ObjEntradaCarteraPrestatario.IdMonedaInstrumento = Instrumento.get(0).IdMoneda;
			ObjEntradaCarteraPrestatario.IdMonedaEmision = "";
			ObjEntradaCarteraPrestatario.IdEmisor = Instrumento.get(0).IdEmisor;
			ObjEntradaCarteraPrestatario.IdBoveda = Prestamo.get(0).IdBoveda;
			ObjEntradaCarteraPrestatario.EsCustodiaExterna="N";			
			ObjEntradaCarteraPrestatario.FolioCarteraOrigen = 0L;
			ObjEntradaCarteraPrestatario.EsRegistroNuevo = "S";
			ObjEntradaCarteraPrestatario.EsVentaCorta = "N";
			ObjEntradaCarteraPrestatario.Glosa = "EMPRESTITO CARTERA PRESTATARIO";
			ObjEntradaCarteraPrestatario.ExigirGarantia = entrada.p_ExigirGarantia;
			DatosMovimientoTransito.add(ObjEntradaCarteraPrestatario);
		}
		
		// ********** DATOS PARA SUBYACENTE
		IngresoCustodia.ObjDatosEntrada ObjEntradaSubyacente = new IngresoCustodia.ObjDatosEntrada();
		ObjEntradaSubyacente.IdGenerarMovimientoCustodia 	= -1 * 1L;
		ObjEntradaSubyacente.TablaAfectada 					= "TRANSITO";
		ObjEntradaSubyacente.AccionOperacion 				= "INGRESO";
		ObjEntradaSubyacente.IdMovimientoCarteraEnTransitoRelacionado = 1L;
		ObjEntradaSubyacente.FolioOperacionRelacionada 		= entrada.p_FolioCarteraPrestamo;
		ObjEntradaSubyacente.IdCliente 						= entrada.p_IdClientePrestamista;
		ObjEntradaSubyacente.IdPortafolio 					= entrada.p_IdPortafolioPrestamista;
		ObjEntradaSubyacente.IdClienteRelacionado 			= Prestamo.get(0).IdCliente;
		ObjEntradaSubyacente.IdPortafolioRelacionado 		= Prestamo.get(0).IdPortafolio;
		ObjEntradaSubyacente.IdTipoProducto 				= "PRES";
		ObjEntradaSubyacente.IdTipoCanalIngreso 			= entrada.p_IdTipoCanalIngreso;
		ObjEntradaSubyacente.IdConcepto 					= "INGEMP";
		ObjEntradaSubyacente.IdTipoOperacion 				= "EMPRES";
		ObjEntradaSubyacente.FechaOperacion 				= java.sql.Date.valueOf(entrada.p_FechaMovimiento);
		ObjEntradaSubyacente.FechaVencimientoOperacion 		= java.sql.Date.valueOf(entrada.p_FechaVencimiento);
		ObjEntradaSubyacente.PlazoEnDiasOperacion 			= entrada.p_PlazoEnDias;
		ObjEntradaSubyacente.FolioComprobante 				= Prestamo.get(0).FolioComprobante;
		ObjEntradaSubyacente.IdTipoComprobante 				= Prestamo.get(0).IdTipoComprobante;
		ObjEntradaSubyacente.FechaComprobante 				= Prestamo.get(0).FechaComprobante;
		ObjEntradaSubyacente.IdBolsa 						= Prestamo.get(0).IdBolsa;
		ObjEntradaSubyacente.IdSucursal 					= Empleado.get(0).IdSucursal;
		ObjEntradaSubyacente.IdAgente 						= entrada.p_IdAgente;
		ObjEntradaSubyacente.IdCondicionLiquidacion 		= entrada.p_IdCondicionDeLiquidacion;
		ObjEntradaSubyacente.FechaLiquidacion 				= java.sql.Date.valueOf(entrada.p_FechaLiquidacion);
		ObjEntradaSubyacente.UsaGarantiaEspecifica 			= Prestamo.get(0).UsaGarantiaEspecifica;
		ObjEntradaSubyacente.CobraPrimaAlInicio 			= "N";
		ObjEntradaSubyacente.IdTipoPrestamo 				= Prestamo.get(0).IdTipoPrestamo;
		ObjEntradaSubyacente.IdInstrumento 					= entrada.p_IdInstrumento;
		ObjEntradaSubyacente.Cantidad 						= entrada.p_Cantidad;
		ObjEntradaSubyacente.ValorRescate 					= BigDecimal.ZERO;
		ObjEntradaSubyacente.CorteMinimo 					= Instrumento.get(0).CorteMinimo;
		ObjEntradaSubyacente.PrecioCompra 					= entrada.p_Precio;
		ObjEntradaSubyacente.TasaCompra 					= entrada.p_TasaPremio;
		ObjEntradaSubyacente.MontoCompra 					= montoLocal;
		ObjEntradaSubyacente.MontoCompraMonedaInstrumento 	= entrada.p_Monto;
		ObjEntradaSubyacente.PrecioAPlazo 					= entrada.p_MontoPremio;
		ObjEntradaSubyacente.MontoAPlazo 					= montoAPlazoLocal;
		ObjEntradaSubyacente.MontoAPlazoMonedaInstrumento 	= entrada.p_MontoAPlazo;
		ObjEntradaSubyacente.Nemotecnico 					= entrada.p_Nemo;
		ObjEntradaSubyacente.EsFungible 					= entrada.p_EsFungible;
		ObjEntradaSubyacente.EsNominal 						= entrada.p_PrestaTitulosNominales;
		ObjEntradaSubyacente.IdMonedaInstrumento 			= Instrumento.get(0).IdMoneda;
		ObjEntradaSubyacente.IdMonedaEmision 				= Instrumento.get(0).IdMoneda;
		ObjEntradaSubyacente.IdTipoReajusteInstrumento 		= Instrumento.get(0).IdTipoReajuste;
		ObjEntradaSubyacente.FechaVencimientoInstrumento 	= Instrumento.get(0).FechaVencimiento;
		ObjEntradaSubyacente.IdEmisor 						= Instrumento.get(0).IdEmisor;	
		ObjEntradaSubyacente.IdBoveda 						= Prestamo.get(0).IdBoveda;
		ObjEntradaSubyacente.FolioCarteraOrigen 			= 0L;
		ObjEntradaSubyacente.EsRegistroNuevo 				= "S";
		ObjEntradaSubyacente.EsVentaCorta 					= "N";
		ObjEntradaSubyacente.Glosa 							= "EMPRESTITO " + entrada.p_Nemo;
		DatosMovimientoTransito.add(ObjEntradaSubyacente);

		
		// ********** INGRESO MOVIMIENTOS TRANSITO
		IngresoCustodia IngresoCustodiaTransito = new IngresoCustodia();
		IngresoCustodia.IngresoCustodiaEntrada EntradaMovimientoTransito = new IngresoCustodia.IngresoCustodiaEntrada();
		EntradaMovimientoTransito.p_IdEmpresa = entrada.p_IdEmpresa;
		EntradaMovimientoTransito.p_IdModulo = entrada.p_IdModulo;
		EntradaMovimientoTransito.p_IdUsuario = entrada.p_IdUsuario;
		EntradaMovimientoTransito.p_SimularOperacion = "N";
		EntradaMovimientoTransito.p_TicketAtencion = 0L;
		EntradaMovimientoTransito.p_ValorizacionAutomatica = "S";
		EntradaMovimientoTransito.p_DatosEntrada = DatosMovimientoTransito;
		IngresoCustodia.IngresoCustodiaSalida salidaMovimientoTransito = IngresoCustodiaTransito.logica(con,EntradaMovimientoTransito);

		for (IngresoCustodia.IngresoCustodiaSalida.ObjDatosSalida DatosSalida : salidaMovimientoTransito.P_OUT_DATOSSALIDA) {
			if (DatosSalida.RegistroConProblemas.equalsIgnoreCase("S")) {
				log.write("ERROR_AL_PROCESAR_MOVIMIENTO");
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_AL_PROCESAR_MOVIMIENTO", null, con, entrada.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR = 1L;
				return salida;
			} else {
				TransitosPorCertificar.add(DatosSalida.IdCustodia);
			}
		}

		// ---------------------------------------------------------------------------------------------
		// INICIO PROCESAR MOVIMIENTOS EN TRANSITO
		Long FolioCustodia = 0L;
		boolean RetiroOK = false;
		boolean IngresoOK = false;
		List<GestionMovimientoCarteraEnTransito.ObjMovimientosEnTransito> MovimientosEnTransito = GestionMovimientoCarteraEnTransito.ObtenerMovimientoEnTransito(con, TransitosPorCertificar);

		for (GestionMovimientoCarteraEnTransito.ObjMovimientosEnTransito movTransito : MovimientosEnTransito) {
			
			// ********** CREAR INGRESO CARTERA EMPRESTITO
			if (movTransito.IdMovimientoCarteraEnTransitoRelacionado.compareTo(0L) == 0 && movTransito.IdEmisor.equalsIgnoreCase(InstrumentoEmprestito.get(0).IdEmisor)) {
				List<IngresoCustodia.ObjDatosEntrada> datosEntradaCartera = new ArrayList<>();
				IngresoCustodia.ObjDatosEntrada datosCartera = new IngresoCustodia.ObjDatosEntrada();
				datosCartera.IdGenerarMovimientoCustodia = 1L;
				datosCartera.TablaAfectada = "CARTERA";
				datosCartera.AccionOperacion = "INGRESO";
				datosCartera.EsRegistroNuevo = "S";
				datosCartera.IdMovimientoCarteraEnTransitoOrigen = movTransito.IdMovimientoCarteraEnTransito;
				datosCartera.IdCliente = movTransito.IdCliente;
				datosCartera.IdPortafolio = movTransito.IdPortafolio;
				datosCartera.IdCuentaCustodioPortafolio = movTransito.IdCuentaCustodioPortafolio;
				datosCartera.IdEmisor = movTransito.IdEmisor;
				datosCartera.IdTipoProducto = "PRES";
				datosCartera.FechaOperacion = movTransito.FechaOperacion;
				datosCartera.FechaVencimientoOperacion = movTransito.FechaVencimientoOperacion;
				datosCartera.FechaVencimientoInstrumento = movTransito.FechaVencimientoInstrumento;
				datosCartera.PlazoEnDiasOperacion = movTransito.PlazoEnDiasOperacion;
				datosCartera.IdTipoOperacion = movTransito.IdTipoOperacion;
				datosCartera.IdConcepto = movTransito.IdConcepto;
				datosCartera.IdCondicionLiquidacion = movTransito.IdCondicionLiquidacion;
				datosCartera.FechaLiquidacion = movTransito.FechaLiquidacion;
				datosCartera.IdTipoCanalIngreso = movTransito.IdTipoCanalIngreso;
				datosCartera.CodigoIFRS = movTransito.CodigoIFRS;
				datosCartera.NemoSVS = movTransito.NemoSVS;
				datosCartera.IdTipoComprobante = movTransito.IdTipoComprobante;
				datosCartera.FolioComprobante = movTransito.FolioComprobante;
				datosCartera.FolioExternoReferencia = movTransito.FolioExternoReferencia;
				datosCartera.IdBolsa = movTransito.IdBolsa;
				datosCartera.IdUsuario = movTransito.IdUsuario;
				datosCartera.IdSucursal = movTransito.IdSucursal;
				datosCartera.IdAgente = movTransito.IdAgente;
				datosCartera.Glosa = movTransito.Glosa;
				datosCartera.IdInstrumento = movTransito.IdInstrumento;
				datosCartera.IdMonedaInstrumento = movTransito.IdMonedaInstrumento;
				datosCartera.IdMonedaEmision = movTransito.IdMonedaEmision;
				datosCartera.IdTipoReajusteInstrumento = movTransito.IdTipoReajuste;
				datosCartera.EsFungible = movTransito.EsFungible;
				datosCartera.IdBoveda = movTransito.IdBoveda;
				datosCartera.EsNominal = movTransito.EsNominal;
				datosCartera.Cantidad = movTransito.Cantidad;
				datosCartera.CorteMinimo = movTransito.CorteMinimo;
				datosCartera.ValorRescate = movTransito.ValorRescate;
				datosCartera.PrecioCompra = movTransito.PrecioCompra;
				datosCartera.TasaCompra = movTransito.TasaCompra;
				datosCartera.MontoCompra = movTransito.MontoCompra;
				datosCartera.MontoCompraMonedaInstrumento = movTransito.MontoCompraMonedaInstrumento;
				datosCartera.PrecioAPlazo = movTransito.PrecioAPlazo;
				datosCartera.FechaEmisionInstrumento = movTransito.FechaEmisionInstrumento;
				datosCartera.UsaGarantiaEspecifica = Prestamo.get(0).UsaGarantiaEspecifica;
				datosCartera.CobraPrimaAlInicio = "N";
				datosCartera.MontoAPlazo = montoAPlazoLocal;
				datosCartera.MontoAPlazoMonedaInstrumento = entrada.p_MontoAPlazo;
				datosCartera.IdTipoPrestamo = Prestamo.get(0).IdTipoPrestamo;
				datosCartera.FolioCarteraRelacionada = entrada.p_FolioCarteraPrestamo;
				datosCartera.ExigirGarantia = entrada.p_ExigirGarantia;
				datosCartera.Glosa = "CREACION CARTERA EMPRESTITO";
				// DATOS PARA VALORIZACION DE CARTERA
				datosCartera.IdInstrumentoSubyacente               = entrada.p_IdInstrumento; 
				datosCartera.NemotecnicoSubyacente 				   = entrada.p_Nemo;
				datosCartera.IdTipoInstrumentoSubyacente 	       = Instrumento.get(0).IdTipoInstrumento;
				datosCartera.TasaCompraInstrumento				   = entrada.p_TasaInstrumento;
				
				if(entrada.p_EsPrestamistaExterno.equals("S"))
					datosCartera.EsCustodiaExterna=entrada.p_EsPrestamistaExterno;
				datosEntradaCartera.add(datosCartera);
				
				
				// ********** INGRESO CARTERA DEL EMPRESTITO 
				IngresoCustodia ingresoCustodia = new IngresoCustodia();
				IngresoCustodia.IngresoCustodiaEntrada entradaIngresoCustodia = new IngresoCustodia.IngresoCustodiaEntrada();
				entradaIngresoCustodia.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaIngresoCustodia.p_IdModulo = entrada.p_IdModulo;
				entradaIngresoCustodia.p_IdUsuario = entrada.p_IdUsuario;
				entradaIngresoCustodia.p_SimularOperacion = "N";
				entradaIngresoCustodia.p_TicketAtencion = 0L;
				entradaIngresoCustodia.p_ValorizacionAutomatica = "S";
				entradaIngresoCustodia.p_DatosEntrada = datosEntradaCartera;
				IngresoCustodia.IngresoCustodiaSalida salidaIngresoCustodia = ingresoCustodia.logica(con,entradaIngresoCustodia);
				
				if (MensajeSalida.getNumeroErrores(salidaIngresoCustodia.P_OUT_MENSAJES) > 0) {
					salida.P_OUT_MENSAJES.addAll(salidaIngresoCustodia.P_OUT_MENSAJES);
					salida.P_OUT_ERRORESCONTADOR = 1L;
					return salida;
				} else {
					IngresoOK = true;
					for (IngresoCustodiaSalida.ObjDatosSalida datoSalida : salidaIngresoCustodia.P_OUT_DATOSSALIDA) {
						FolioCustodia = datoSalida.FolioCustodia;
						salida.P_OUT_FOLIOCARTERA = datoSalida.FolioCustodia;
						
						String descripcion = Diccionario.getMensaje(con, entrada.p_IdEmpresa, "UI_CARTERA_CREADA_FOLIO").add("FolioCartera", datoSalida.FolioCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
						salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_INFORMACION, descripcion,	null, con, entrada.p_IdEmpresa));
					}
				}
			}
			
			// ********** CREAR INGRESO SUBYACENTE EMPRESTITO
			if (movTransito.IdMovimientoCarteraEnTransitoRelacionado.compareTo(0L) != 0) {			
				List<IngresoCustodia.ObjDatosEntrada> datosEntradaSubyacente = new ArrayList<>();
				IngresoCustodia.ObjDatosEntrada datosSubyacente = new IngresoCustodia.ObjDatosEntrada();

				datosSubyacente.IdGenerarMovimientoCustodia = 1L;
				datosSubyacente.TablaAfectada = "SUBYACENTE";
				datosSubyacente.AccionOperacion = "INGRESO";
				datosSubyacente.EsRegistroNuevo = "S";
				datosSubyacente.IdMovimientoCarteraEnTransitoOrigen = movTransito.IdMovimientoCarteraEnTransito;
				datosSubyacente.IdCarteraOrigen = 0L;
				datosSubyacente.FolioCarteraRelacionada = FolioCustodia;
				datosSubyacente.IdCliente = movTransito.IdCliente;
				datosSubyacente.IdPortafolio = movTransito.IdPortafolio;
				datosSubyacente.IdCuentaCustodioPortafolio = movTransito.IdCuentaCustodioPortafolio;
				datosSubyacente.FechaOperacion = movTransito.FechaOperacion;
				datosSubyacente.FechaVencimientoOperacion = movTransito.FechaVencimientoOperacion;
				datosSubyacente.FechaVencimientoInstrumento = movTransito.FechaVencimientoInstrumento;
				datosSubyacente.PlazoEnDiasOperacion = movTransito.PlazoEnDiasOperacion;
				datosSubyacente.IdTipoOperacion = movTransito.IdTipoOperacion;
				datosSubyacente.IdConcepto = movTransito.IdConcepto;
				datosSubyacente.IdCondicionLiquidacion = movTransito.IdCondicionLiquidacion;
				datosSubyacente.FechaLiquidacion = movTransito.FechaLiquidacion;
				datosSubyacente.IdTipoCanalIngreso = movTransito.IdTipoCanalIngreso;
				datosSubyacente.CodigoIFRS = movTransito.CodigoIFRS;
				datosSubyacente.NemoSVS = movTransito.NemoSVS;
				datosSubyacente.IdTipoComprobante = movTransito.IdTipoComprobante;
				datosSubyacente.FolioComprobante = movTransito.FolioComprobante;
				datosSubyacente.FolioExternoReferencia = movTransito.FolioExternoReferencia;
				datosSubyacente.IdBolsa = movTransito.IdBolsa;
				datosSubyacente.IdUsuario = movTransito.IdUsuario;
				datosSubyacente.IdSucursal = movTransito.IdSucursal;
				datosSubyacente.IdAgente = movTransito.IdAgente;
				datosSubyacente.Glosa = movTransito.Glosa;
				datosSubyacente.IdInstrumento = movTransito.IdInstrumento;
				datosSubyacente.IdMonedaInstrumento = movTransito.IdMonedaInstrumento;
				datosSubyacente.IdMonedaEmision = movTransito.IdMonedaEmision;
				datosSubyacente.IdTipoReajusteInstrumento = movTransito.IdTipoReajuste;
				datosSubyacente.IdEmisor = movTransito.IdEmisor;
				datosSubyacente.EsFungible = movTransito.EsFungible;
				datosSubyacente.IdBoveda = movTransito.IdBoveda;
				datosSubyacente.EsNominal = movTransito.EsNominal;
				datosSubyacente.Cantidad = movTransito.Cantidad;
				datosSubyacente.CorteMinimo = movTransito.CorteMinimo;
				datosSubyacente.ValorRescate = movTransito.ValorRescate;
				datosSubyacente.PrecioCompra = movTransito.PrecioCompra;
				datosSubyacente.TasaCompra = entrada.p_TasaInstrumento;
				datosSubyacente.MontoCompra = movTransito.MontoCompra;
				datosSubyacente.MontoCompraMonedaInstrumento = movTransito.MontoCompraMonedaInstrumento;
				datosSubyacente.PrecioAPlazo = movTransito.PrecioAPlazo;
				datosSubyacente.UsaGarantiaEspecifica = Prestamo.get(0).UsaGarantiaEspecifica;
				datosSubyacente.CobraPrimaAlInicio = "N";
				datosSubyacente.MontoAPlazo = montoAPlazoLocal;
				datosSubyacente.MontoAPlazoMonedaInstrumento = entrada.p_MontoAPlazo;
				datosSubyacente.IdTipoPrestamo = Prestamo.get(0).IdTipoPrestamo;
				datosEntradaSubyacente.add(datosSubyacente);
				
				// ********** INGRESO DE SUBYACENTE DEL EMPRESTITO
				IngresoCustodia ingresoCustodiaSubyacente = new IngresoCustodia();
				IngresoCustodia.IngresoCustodiaEntrada entradaIngresoSubyacente = new IngresoCustodia.IngresoCustodiaEntrada();
				entradaIngresoSubyacente.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaIngresoSubyacente.p_IdModulo = entrada.p_IdModulo;
				entradaIngresoSubyacente.p_IdUsuario = entrada.p_IdUsuario;
				entradaIngresoSubyacente.p_SimularOperacion = "N";
				entradaIngresoSubyacente.p_TicketAtencion = 0L;
				entradaIngresoSubyacente.p_ValorizacionAutomatica = "S";
				entradaIngresoSubyacente.p_DatosEntrada = datosEntradaSubyacente;
				IngresoCustodia.IngresoCustodiaSalida salidaIngresoSubyacente = ingresoCustodiaSubyacente.logica(con,entradaIngresoSubyacente);
				
				
				if (MensajeSalida.getNumeroErrores(salidaIngresoSubyacente.P_OUT_MENSAJES) > 0) {
					salida.P_OUT_MENSAJES.addAll(salidaIngresoSubyacente.P_OUT_MENSAJES);
					salida.P_OUT_ERRORESCONTADOR = 1L;
					return salida;
				} else {
					IngresoOK = true;
					for (IngresoCustodiaSalida.ObjDatosSalida datoSalida : salidaIngresoSubyacente.P_OUT_DATOSSALIDA) {
						salida.P_OUT_FOLIOSUBYACENTE = datoSalida.FolioCustodia;
						String descripcion = Diccionario.getMensaje(con, entrada.p_IdEmpresa, "UI_CARTERA_CREADA_FOLIO").add("FolioCartera", datoSalida.FolioCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
						salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_INFORMACION, descripcion,null, con, entrada.p_IdEmpresa));
					}
				}
			}
			
			// ********** CREAR INGRESO CARTERA PRESTATARIO
			if (Prestamo.get(0).IdCliente.equals(movTransito.IdCliente) &&  movTransito.IdEmisor.equalsIgnoreCase(Instrumento.get(0).IdEmisor)) {
				List<IngresoCustodia.ObjDatosEntrada> datosEntradaCarteraPrestatario = new ArrayList<>();
				IngresoCustodia.ObjDatosEntrada datosCarteraPrestatario = new IngresoCustodia.ObjDatosEntrada();

				datosCarteraPrestatario.IdGenerarMovimientoCustodia = 2L;
				datosCarteraPrestatario.TablaAfectada = "CARTERA";
				datosCarteraPrestatario.AccionOperacion = "INGRESO";
				datosCarteraPrestatario.EsRegistroNuevo = "S";
				datosCarteraPrestatario.IdMovimientoCarteraEnTransitoOrigen = movTransito.IdMovimientoCarteraEnTransito;
				datosCarteraPrestatario.IdCliente = movTransito.IdCliente;
				datosCarteraPrestatario.IdPortafolio = movTransito.IdPortafolio;
				datosCarteraPrestatario.IdCuentaCustodioPortafolio = movTransito.IdCuentaCustodioPortafolio;
				datosCarteraPrestatario.IdEmisor = movTransito.IdEmisor;
				datosCarteraPrestatario.IdTipoProducto = "CUST";
				datosCarteraPrestatario.FechaOperacion = movTransito.FechaOperacion;
				datosCarteraPrestatario.FechaVencimientoOperacion = movTransito.FechaVencimientoOperacion;
				datosCarteraPrestatario.FechaVencimientoInstrumento = movTransito.FechaVencimientoInstrumento;
				datosCarteraPrestatario.PlazoEnDiasOperacion = movTransito.PlazoEnDiasOperacion;
				datosCarteraPrestatario.IdTipoOperacion = movTransito.IdTipoOperacion;
				datosCarteraPrestatario.IdConcepto = movTransito.IdConcepto;
				datosCarteraPrestatario.IdCondicionLiquidacion = movTransito.IdCondicionLiquidacion;
				datosCarteraPrestatario.FechaLiquidacion = movTransito.FechaLiquidacion;
				datosCarteraPrestatario.IdTipoCanalIngreso = movTransito.IdTipoCanalIngreso;
				datosCarteraPrestatario.CodigoIFRS = movTransito.CodigoIFRS;
				datosCarteraPrestatario.NemoSVS = movTransito.NemoSVS;
				datosCarteraPrestatario.IdTipoComprobante = movTransito.IdTipoComprobante;
				datosCarteraPrestatario.FolioComprobante = movTransito.FolioComprobante;
				datosCarteraPrestatario.FolioExternoReferencia = movTransito.FolioExternoReferencia;
				datosCarteraPrestatario.IdBolsa = movTransito.IdBolsa;
				datosCarteraPrestatario.IdUsuario = movTransito.IdUsuario;
				datosCarteraPrestatario.IdSucursal = movTransito.IdSucursal;
				datosCarteraPrestatario.IdAgente = movTransito.IdAgente;
				datosCarteraPrestatario.Glosa = movTransito.Glosa;
				datosCarteraPrestatario.IdInstrumento = movTransito.IdInstrumento;
				datosCarteraPrestatario.IdMonedaInstrumento = movTransito.IdMonedaInstrumento;
				datosCarteraPrestatario.IdMonedaEmision = movTransito.IdMonedaEmision;
				datosCarteraPrestatario.IdTipoReajusteInstrumento = movTransito.IdTipoReajuste;
				datosCarteraPrestatario.EsFungible = movTransito.EsFungible;
				datosCarteraPrestatario.IdBoveda = movTransito.IdBoveda;
				datosCarteraPrestatario.EsNominal = movTransito.EsNominal;
				datosCarteraPrestatario.Cantidad = movTransito.Cantidad;
				datosCarteraPrestatario.CorteMinimo = movTransito.CorteMinimo;
				datosCarteraPrestatario.ValorRescate = movTransito.ValorRescate;
				datosCarteraPrestatario.PrecioCompra = movTransito.PrecioCompra;
				datosCarteraPrestatario.TasaCompra = movTransito.TasaCompra;
				datosCarteraPrestatario.MontoCompra = movTransito.MontoCompra;
				datosCarteraPrestatario.MontoCompraMonedaInstrumento = movTransito.MontoCompraMonedaInstrumento;
				datosCarteraPrestatario.PrecioAPlazo = movTransito.PrecioAPlazo;
				datosCarteraPrestatario.FechaEmisionInstrumento = movTransito.FechaEmisionInstrumento;
				datosCarteraPrestatario.UsaGarantiaEspecifica = Prestamo.get(0).UsaGarantiaEspecifica;
				datosCarteraPrestatario.CobraPrimaAlInicio = "";
				datosCarteraPrestatario.MontoAPlazo = montoAPlazoLocal;
				datosCarteraPrestatario.MontoAPlazoMonedaInstrumento = entrada.p_MontoAPlazo;
				datosCarteraPrestatario.IdTipoPrestamo = "";
				datosCarteraPrestatario.FolioCarteraRelacionada = entrada.p_FolioCarteraPrestamo;
				datosCarteraPrestatario.IdOperacion = FolioCustodia; //Para guardar relacion con emprestito
				datosCarteraPrestatario.ExigirGarantia = entrada.p_ExigirGarantia;
				datosEntradaCarteraPrestatario.add(datosCarteraPrestatario);
				
				// ********** INGRESO CARTERA PRESTATARIO
				IngresoCustodia ingresoCustodiaPrestatario = new IngresoCustodia();
				IngresoCustodia.IngresoCustodiaEntrada entradaIngresoCustodiaPrestatario = new IngresoCustodia.IngresoCustodiaEntrada();
				entradaIngresoCustodiaPrestatario.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaIngresoCustodiaPrestatario.p_IdModulo = entrada.p_IdModulo;
				entradaIngresoCustodiaPrestatario.p_IdUsuario = entrada.p_IdUsuario;
				entradaIngresoCustodiaPrestatario.p_SimularOperacion = "N";
				entradaIngresoCustodiaPrestatario.p_TicketAtencion = 0L;
				entradaIngresoCustodiaPrestatario.p_ValorizacionAutomatica = "S";
				entradaIngresoCustodiaPrestatario.p_DatosEntrada = datosEntradaCarteraPrestatario;
				IngresoCustodia.IngresoCustodiaSalida salidaIngresoCustodiaPrestatario = ingresoCustodiaPrestatario.logica(con,entradaIngresoCustodiaPrestatario);
				
				if (MensajeSalida.getNumeroErrores(salidaIngresoCustodiaPrestatario.P_OUT_MENSAJES) > 0) {
					salida.P_OUT_MENSAJES.addAll(salidaIngresoCustodiaPrestatario.P_OUT_MENSAJES);
					salida.P_OUT_ERRORESCONTADOR = 1L;
					return salida;
				} else {
					IngresoOK = true;
					for (IngresoCustodiaSalida.ObjDatosSalida datoSalida : salidaIngresoCustodiaPrestatario.P_OUT_DATOSSALIDA)  {
						salida.P_OUT_FOLIOCARTERA = datoSalida.FolioCustodia;
						
						String descripcion = Diccionario.getMensaje(con, entrada.p_IdEmpresa, "UI_CARTERA_CREADA_FOLIO").add("FolioCartera", datoSalida.FolioCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
						salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_INFORMACION, descripcion,null, con, entrada.p_IdEmpresa));
					}
				}
			} 
			
			// ********** RETIRO TITULOS PRESTAMISTA 
			if (movTransito.IdTipoOperacion.equalsIgnoreCase("RETTIT")) {
				List<RetiroCustodia.ObjDatosEntrada> lstRetirosCustodiaPrestamos = new ArrayList<>();
				RetiroCustodia.ObjDatosEntrada	RetiroSubyacenteCart = new RetiroCustodia.ObjDatosEntrada();
				RetiroSubyacenteCart.IdGenerarMovimientoCustodia 	= movTransito.IdMovimientoCarteraEnTransito;
				RetiroSubyacenteCart.TablaAfectada 				= "CARTERA";
				RetiroSubyacenteCart.EsRetiroNegocio 			= "S";
				RetiroSubyacenteCart.AccionOperacion			= "RETIRO";					
				RetiroSubyacenteCart.FechaComprobante			= Fecha.toDate(entrada.p_FechaMovimiento);
				RetiroSubyacenteCart.IdCliente					= movTransito.IdCliente;
				RetiroSubyacenteCart.IdPortafolio				= movTransito.IdPortafolio;
				RetiroSubyacenteCart.FechaOperacion				= Fecha.toDate(entrada.p_FechaMovimiento);
				RetiroSubyacenteCart.IdTipoOperacion			= movTransito.IdTipoOperacion;
				RetiroSubyacenteCart.IdOperacion				= 0L;
				RetiroSubyacenteCart.IdConcepto 				= movTransito.IdTipoOperacion;
				RetiroSubyacenteCart.IdCondicionLiquidacion 	= "T0";
				RetiroSubyacenteCart.IdTipoCanalIngreso			= movTransito.IdTipoCanalIngreso;
				RetiroSubyacenteCart.IdUsuario					= movTransito.IdUsuario;
				RetiroSubyacenteCart.IdModulo					= movTransito.IdModulo;
				RetiroSubyacenteCart.Glosa						= movTransito.Glosa;					
				RetiroSubyacenteCart.IdInstrumento				= movTransito.IdInstrumento;
				RetiroSubyacenteCart.IdMonedaInstrumento		= movTransito.IdMonedaInstrumento;
				RetiroSubyacenteCart.IdEmisor					= movTransito.IdEmisor;
				RetiroSubyacenteCart.IdBoveda					= movTransito.IdBoveda;
				RetiroSubyacenteCart.PrecioCompra				= movTransito.PrecioCompra;
				RetiroSubyacenteCart.TasaCompra					= movTransito.TasaCompra;	
				RetiroSubyacenteCart.Cantidad 					= movTransito.Cantidad;
				RetiroSubyacenteCart.ValorRescate 				= movTransito.ValorRescate;
				RetiroSubyacenteCart.MontoCompraMonedaInstrumento = movTransito.MontoCompraMonedaInstrumento;
				RetiroSubyacenteCart.IdMonedaEmision 			= movTransito.IdMonedaEmision;
				lstRetirosCustodiaPrestamos.add(RetiroSubyacenteCart);
				
				RetiroCustodia retirosCustodia = new RetiroCustodia();
				RetiroCustodia.RetiroCustodiaEntrada paramInSP = new RetiroCustodia.RetiroCustodiaEntrada();
				paramInSP = new RetiroCustodia.RetiroCustodiaEntrada();
				paramInSP.p_IdEmpresa				= entrada.p_IdEmpresa;
				paramInSP.p_IdModulo				= entrada.p_IdModulo;
				paramInSP.p_IdUsuario				= entrada.p_IdUsuario;
				paramInSP.p_ValorizacionAutomatica 	= "S";
				paramInSP.p_SimularOperacion		= "N";
				paramInSP.p_TicketAtencion 			= 0L;
				paramInSP.p_DatosEntrada			= lstRetirosCustodiaPrestamos;	
				RetiroCustodia.RetiroCustodiaSalida resultRetiros = retirosCustodia.logica(con, paramInSP);	
				
				if (MensajeSalida.getNumeroErrores(resultRetiros.P_OUT_MENSAJES) > 0) {
					salida.P_OUT_MENSAJES.addAll(resultRetiros.P_OUT_MENSAJES);
					salida.P_OUT_ERRORESCONTADOR = 1L;
					return salida;
				} else {
					RetiroOK = true;
					for (RetiroCustodia.RetiroCustodiaSalida.ObjDatosSalida datoSalida : resultRetiros.P_OUT_DATOSSALIDA) {
						if ("CARTERA".equalsIgnoreCase(datoSalida.TablaOrigen))
							salida.P_OUT_FOLIOCARTERA 		= datoSalida.FolioCustodia;
						else 
							salida.P_OUT_FOLIOSUBYACENTE 	= datoSalida.FolioCustodia;
						
						if (entrada.p_Cantidad.compareTo(entrada.p_Cantidad_Asignar) < 0 ) {
							String descripcion_adv = Diccionario.getMensaje(con, entrada.p_IdEmpresa, "UI_CANTIDAD_PRESTAMISTA_MENOR_A_CANTIDAD_PRESTAMO").msg();
							salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, descripcion_adv,null, con, entrada.p_IdEmpresa));
							
						}
						
						String descripcion = Diccionario.getMensaje(con, entrada.p_IdEmpresa, "UI_TIPO_CARTERA_RETIRADA_FOLIO").add("FolioCartera", datoSalida.FolioCustodia).add("TipoCartera", datoSalida.TablaOrigen).msg();
						salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_INFORMACION, descripcion,null, con, entrada.p_IdEmpresa));
					}
				}
			}
			
			// ********** MARCA LOS MOVIMIENTOS EN TRANSITO PROCESADOS (PARA CERTIFICARLOS)
			if (RetiroOK || IngresoOK) {
				GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar MovTraAct = new GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar();
				MovTraAct.IdMovimientoCarteraEnTransito = movTransito.IdMovimientoCarteraEnTransito;
				MovTraAct.EsCertificacion = "S";
				MovTraAct.IdUsuario = entrada.p_IdUsuario;
				transitosOK.add(MovTraAct);
			}
		}
		// FIN PROCESAR MOVIMIENTOS EN TRANSITO
		// ---------------------------------------------------------------------------------------------
		
		// CERTIFICAR MOVIMIENTOS EN TRANSITO
		if (transitosOK.size() > 0) {
			GestionMovimientoCarteraEnTransito.ActualizarMovimientoEnTransito(con, transitosOK);
		}
		
		
		//ACTUALIZACION ENCABEZADO POR GARANTIA
		if (entrada.p_ExigirGarantia.equalsIgnoreCase("S")) {
			List<UpdateExigirGarantia> paramsExigirGarantia = new ArrayList<UpdateExigirGarantia>();
			UpdateExigirGarantia EGActualizar = new UpdateExigirGarantia();
			EGActualizar.FolioCartera = entrada.p_FolioCarteraPrestamo;
			paramsExigirGarantia.add(EGActualizar);
			sql.batch(con, UPD_EXIGIR_GARANTIA, paramsExigirGarantia, UpdateExigirGarantia.class);
		}
		
		// ---------------------------------------------------------------------------------------------
		// ---------------------------------------------------------------------------------------------
		// B L O C K C H A I N
		// ---------------------------------------------------------------------------------------------
		// ---------------------------------------------------------------------------------------------

		// si p_VieneDesdeBlockChain == S significa que se esta llamando este servicio desde proceso que leyo desde BlockChain los prestamistas
		// por lo que debe saltarse el proceso de insercion en BlockChain de los prestamistas
		if (entrada.p_VieneDesdeBlockChain.equals("N")) {
			if (EsBlockChain.equals(ConstantesBCK.SI) && ContratoFirmado.equals(ConstantesBCK.SI)) {
				AsignaPrestamistaBlockchain AsignarPrestamista = new AsignaPrestamistaBlockchain();
				AsignaPrestamistaBlockchain_Entrada AsignarPrestamistaEntrada = new AsignaPrestamistaBlockchain_Entrada();
				AsignarPrestamistaEntrada.p_IdEmpresa 					= entrada.p_IdEmpresa;
				AsignarPrestamistaEntrada.p_TipoCustodia 				= Prestamo.get(0).TipoCustodia;
				AsignarPrestamistaEntrada.p_TipoGarantia 				= entrada.p_ExigirGarantia;
				AsignarPrestamistaEntrada.p_CodigoCorredorPrestamista 	= salidaConsultaDataCorredora.p_Out_BrokerageID;
				AsignarPrestamistaEntrada.p_Cantidad 					= entrada.p_Cantidad.toString();
				AsignarPrestamistaEntrada.p_IdClientePrestatario 		= Prestamo.get(0).IdCliente.toString();
				AsignarPrestamistaEntrada.p_IdClientePrestamista 		= entrada.p_IdClientePrestamista.toString();
				AsignarPrestamistaEntrada.p_CantidadPrestamista 		= "";
				AsignarPrestamistaEntrada.p_Folio 						= Prestamo.get(0).FolioSistemaExterno;
				AsignaPrestamistaBlockchain_Salida AsignarPrestamistaBlockChainFirmado_Salida = AsignarPrestamista.logica(con, AsignarPrestamistaEntrada);
									
				salida.P_OUT_MENSAJES.addAll(AsignarPrestamistaBlockChainFirmado_Salida.P_OUT_MENSAJES);
				
				if (AsignarPrestamistaBlockChainFirmado_Salida.p_Out_ContadorError > 0) {
					
					List<LogProcesoPrestamoBlockChain> logProcesoCrearEmprestitoBlockChain = new ArrayList<>();
					if (!AsignarPrestamistaBlockChainFirmado_Salida.p_Out_MensajeError.equals("")) {
						logProcesoCrearEmprestitoBlockChain.add(new LogProcesoPrestamoBlockChain(entrada.p_IdEmpresa, Prestamo.get(0).FolioPrestamo, entrada.p_FechaMovimiento, AsignarPrestamistaBlockChainFirmado_Salida.p_Out_MensajeError, "CrearEmprestito", "Logica", entrada.p_IdUsuario.toString())); 
					}
					if (!logProcesoCrearEmprestitoBlockChain.isEmpty()) {
						sql.batch(con, SQL_INSERT_LOG_PROCESO_BLOCKCHAIN, logProcesoCrearEmprestitoBlockChain, LogProcesoPrestamoBlockChain.class);	
					}
					if (AsignarPrestamistaBlockChainFirmado_Salida.p_Out_Status.equalsIgnoreCase("ERROR")) {
						salida.P_OUT_MENSAJES = salida.P_OUT_MENSAJES.stream()
								.filter(mensaje_salida -> !mensaje_salida.tipo.equals(MensajeSalida.MENSAJE_INFORMACION))
								.collect(Collectors.toList());
						return salida;
					}
					
				}
						
			}
		}
		// ---------------------------------------------------------------------------------------------
		// ---------------------------------------------------------------------------------------------
		// B L O C K C H A I N
		// ---------------------------------------------------------------------------------------------
		// ---------------------------------------------------------------------------------------------

		/*---------------
		  INICIO NETEO	
		---------------*/
		if(Prestamo.get(0).NeteaCarteraSobregiro.equals("S")){				
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");			
			EntradaNeteo eee = new EntradaNeteo();
			eee.p_IdEmpresa = entrada.p_IdEmpresa;
			eee.p_IdModulo = "CUS";
			eee.p_IdUsuario = entrada.p_IdUsuario;
			ClienteNeteo clienteNeteo = new ClienteNeteo();
			List<ClienteNeteo> portafoliosInstrumento = new ArrayList<>();
			clienteNeteo.IdPortafolio = Prestamo.get(0).IdPortafolio;
			clienteNeteo.IdInstrumento =  entrada.p_IdInstrumento;
			portafoliosInstrumento.add(clienteNeteo);
			eee.p_PortafoliosInstrumento = portafoliosInstrumento;
			eee.p_SimularOperacion = "N";
			eee.p_UsaReglasNegocio = "S";
			
			AgregarPrograma.InAgregarPrograma paramsIn = new AgregarPrograma.InAgregarPrograma(); 
			paramsIn.p_IdEmpresa		= entrada.p_IdEmpresa;
			paramsIn.p_IdModulo			= "CUS";
			paramsIn.p_IdUsuario		= entrada.p_IdUsuario;
			paramsIn.p_IdTipoPrograma	= "NeteoCarterasNegativas";
			paramsIn.p_Segundos			= 0L;
			paramsIn.p_FechaSistema		= fechaSistema;
			List<Parametro> paramsParametros = new ArrayList<>();
			Parametro ppp = new Parametro();
			ppp.NombreParametro = "Entrada";
			Gson gson = Utils.getGsonBuilder();
			ppp.Valor = "";
			ppp.ValorBlob = gson.toJson(eee);
			paramsParametros.add(ppp );
			paramsIn.p_Parametros		= paramsParametros ;
			AgregarPrograma AP = new AgregarPrograma();
			AP.logica(con, paramsIn);
		}
		/*---------------
		  FIN NETEO	
		---------------*/
		
		salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);

		log.write("PARAMETROS DE SALIDA : " + salida.toString());
		log.write("== TERMINO CREAR EMPRESTITO ==");

		return salida;
	}

	// -------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE - CALL
	// -------------------------------------------------------------------------------------------
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();
		CrearEmprestito_Entrada in = gson.fromJson(jin.toString(), CrearEmprestito_Entrada.class);
		CrearEmprestito_Salida out = logica(con, in);

		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	// -------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	// -------------------------------------------------
	public static void main(String[] args) throws Exception {
		System.out.println("== INICIO PRUEBA LOCAL ==");

		try (Connection con = PlataformaUtils.getPropertiesConnection("desa2")) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
					.serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"CUS\",\"p_UsaReglasNegocio\":\"S\",\"p_IdClientePrestamista\":11507,\"p_IdPortafolioPrestamista\":12454,\"p_FechaMovimiento\":\"2024-04-09\",\"p_FechaVencimiento\":\"2024-05-09\",\"p_IdCondicionDeLiquidacion\":\"T0\",\"p_FechaLiquidacion\":\"2024-04-09\",\"p_IdInstrumento\":260115,\"p_Nemo\":\"BCOCH-A\",\"p_Cantidad\":2000000000,\"p_Cantidad_Asignar\":2000000000,\"p_Precio\":139.5625,\"p_TasaInstrumento\":0.456,\"p_Monto\":7776565521,\"p_MontoPremio\":640322404.9991407,\"p_TasaPremio\":8.234,\"p_MontoAPlazo\":8416887925.999141,\"p_FolioCarteraPrestamo\":543573,\"p_PrestaTitulosNominales\":\"N\",\"p_IdAgente\":97,\"p_IdEmpleado\":108,\"p_PlazoEnDias\":30,\"p_IdTipoCanalIngreso\":\"AGE\",\"p_IdMonedaSistema\":\"CLP\",\"p_ExigirGarantia\":\"N\",\"p_EsPrestamistaExterno\":\"N\"}";
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"CUS\",\"p_UsaReglasNegocio\":\"S\",\"p_IdClientePrestamista\":11509,\"p_IdPortafolioPrestamista\":12469,\"p_FechaMovimiento\":\"2024-04-02\",\"p_FechaVencimiento\":\"2024-04-08\",\"p_IdCondicionDeLiquidacion\":\"T0\",\"p_FechaLiquidacion\":\"2024-04-02\",\"p_IdInstrumento\":70026,\"p_Nemo\":\"BACEN-A2\",\"p_Cantidad\":500,\"p_Cantidad_Asignar\":32000,\"p_Precio\":105.7561,\"p_TasaInstrumento\":1.19,\"p_Monto\":7273222,\"p_MontoPremio\":83932.98188000079,\"p_TasaPremio\":5.77,\"p_MontoAPlazo\":7357154.981880001,\"p_FolioCarteraPrestamo\":542700,\"p_PrestaTitulosNominales\":\"N\",\"p_IdAgente\":97,\"p_IdEmpleado\":108,\"p_PlazoEnDias\":6,\"p_IdTipoCanalIngreso\":\"AGE\",\"p_IdMonedaSistema\":\"CLP\",\"p_ExigirGarantia\":\"N\",\"p_EsPrestamistaExterno\":\"N\"}";
			
			CrearEmprestito_Entrada Objin = gson.fromJson(json, CrearEmprestito_Entrada.class);
			CrearEmprestito demo = new CrearEmprestito();

			System.out.println("ENTRADA:" + Objin.toString());

			try {
				CrearEmprestito_Salida ObjOut = demo.logica(con, Objin);
				System.out.println("SALIDA:" + ObjOut);
				con.commit();
				//con.rollback();
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
		}

		System.out.println("== FIN PRUEBA LOCAL ==");
	}
}
