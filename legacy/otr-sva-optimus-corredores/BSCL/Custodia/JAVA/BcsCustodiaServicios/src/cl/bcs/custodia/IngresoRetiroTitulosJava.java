package cl.bcs.custodia;

import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.IngresoRetiroTitulosJava.IngresoRetiroTitulosEntrada.ObjInstrumento;
import cl.bcs.custodia.IngresoRetiroTitulosJava.IngresoRetiroTitulosSalida.ObjResultado;
import cl.bcs.custodia.ValidaCantidadRetiroCustodia.EntradaValidacionCustodia;
import cl.bcs.custodia.ValidaCantidadRetiroCustodia.SalidaValidacionCustodia;
import cl.bcs.custodia.utils.CacheValorizacion;
import cl.bcs.custodia.utils.GestionMovimientoCarteraEnTransito;
import cl.bcs.custodia.utils.GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar;
import cl.bcs.custodia.utils.InformacionConceptosCustodia;
import cl.bcs.custodia.utils.IngresoCustodia;
import cl.bcs.custodia.utils.IngresoCustodia.ObjDatosEntrada;
import cl.bcs.facturacion.ProcesarFacturacionCustodia.ClienteNeteo;
import cl.bcs.facturacion.ProcesarFacturacionCustodia.EntradaNeteo;
import cl.bcs.custodia.utils.LogCustodia;
import cl.bcs.custodia.utils.RetiroCustodia;
import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo;
import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS;
import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo.ServiceEntrada;
import cl.bcs.plataforma.bat.AgregarPrograma;
import cl.bcs.plataforma.bat.AgregarPrograma.InAgregarPrograma.Parametro;
import cl.bcs.plataforma.cre.ConsultarEstadoModulo;
import cl.bcs.plataforma.cre.ConsultarEstadoModulo.InConsultarEstadoModulo;
import cl.bcs.plataforma.cre.ConsultarEstadoModulo.Modulo;
import cl.bcs.plataforma.cre.ConsultarEstadoModulo.OutConsultarEstadoModulo;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class IngresoRetiroTitulosJava implements JavaCall {	
	private static final Logger logger = LogManager.getLogger(IngresoRetiroTitulosJava.class);
	
	private final String SQL_SELECT_MESAS = loadResourceToString("cl/bcs/custodia/sql/IngresoCustodia_SEL_Mesas.sql");
	private final String SQL_SELECT_CONCEPTOS = loadResourceToString("cl/bcs/custodia/sql/IngresoRetiroTitulosJava_SelectConceptos.sql");
	private final String SQL_SELECT_CLIENTE = loadResourceToString("cl/bcs/custodia/sql/IngresoRetiroTitulosJava_SelectClientes.sql");
	private final String SQL_SELECT_REQUISITO_OPERATIVO = loadResourceToString("cl/bcs/custodia/sql/IngresoRetiroTitulosJava_SelectRequisitoOperativo.sql");

	private static final String MODULO_ESTADO_CERRADO = "CER";

	public static class IngresoRetiroTitulosEntrada {
		public Long						p_IdEmpresa;
	    public Long						p_IdUsuario;
	    public String					p_IdModulo;
	    public String					p_simularOperacion;			//S/N 	se usa para simular operacion (algunos casos) 
		public String					p_UsaReglasNegocio;			//S/N 	se usa en caso de servicios anidados.		
		public List<ObjInstrumento>		P_INSTRUMENTOS;
		public String					p_Certifica = "N";  		//S: 	si proviene desde Certificacion de movimiento en transito
		public List<Long>				p_MovimientosEnTransito;	//si proviene desde Certificacion de movimiento en transito
		public String					p_CanalIngreso = "OPT";
		public boolean 					p_ExisteMovEnTransito;
		
		public IngresoRetiroTitulosEntrada () {
			P_INSTRUMENTOS			= new ArrayList<ObjInstrumento>();
			p_MovimientosEnTransito	= new ArrayList<Long>();
		}
		
		public static class ObjInstrumento {
			//VARIABLES DEL PROCESO OBLIGATORIAS
			public Long					IdGenerarMovimientoCustodia = 0L;	//Registro secuencial unico
			public String				FechaOperacion;						//debe venir en formato AAAA-MM-DD
			public String				FechaOperacionOriginal;				//debe venir en formato AAAA-MM-DD
			public String				FechaVencimiento;					//debe venir en formato AAAA-MM-DD
			public String				FechaLiquidacion;					//debe venir en formato AAAA-MM-DD
			public String				IdCondicionLiquidacion;				//si no usara Condicion de Liq, enviar un valor <= 0
			public String				IdConcepto;
			public String				IdBolsa;
			public String				IdTipoOperacion;
			public String				IdTipoProducto = "";
			public Long					IdCliente;
			public Long					IdPortafolio;
			public Long					IdClienteDestino;
			public Long					IdPortafolioDestino;
			public String				IdBoveda;
			public String				CuentaBoveda;			
			public Long					IdCuentaCustodioPortafolio;				//--------------------------------------------------Campo nuevo de peru				
			public String				IdBovedaRelacionada;
			public String				CuentaBovedaRelacionada;
			public Long					IdCuentaCustodioPortafolioRelacionado;	//--------------------------------------------------Campo nuevo de peru
			public String				esTraspasoCliente = "N";
			public String				esTraspasoPortafolio = "N";		
			public String				esTraspasoBoveda = "N";		
			public String				DebeCertificar;
			public String				EsGarantia = "N";
			public Long					PlazoEnDias = 0L; 
			public Long					IdCartera = 0L;					//cartera a la cual puede tener referencia el movimiento
			public Long					FolioCartera = 0L;			
			public String				FolioExternoReferencia = "";	//comprobante de referencia
			public String				Glosa = "";
			public String				IndicadorNominal = "N"; 
			public Long					IdAgente;
			public Long					IdSucursal;			
			public String				IdTipoCanalIngreso;		
			public String				IdClasificacionTipoInstrumento;
			public String				IdTipoInstrumento;
			public Long					IdInstrumento;
			public String				Nemotecnico;
			public String				IdEmisor;
			public String				IdMonedaInstrumento = "";
			public String				IdMonedaEmision = "";
			public String				IdTipoReajuste = "";
			public String				IdMonedaAdicional = null;
			public String				FechaEmisionInstrumento = "0001-01-01";  //public java.sql.Date FechaEmisionInstrumento = Fecha.toDate("0001-01-01");
			public String				FechaVencimientoInstrumento = "0001-01-01"; 
			public String				Fungible = "N";
			public String				NemoSVS = "";		
			public BigDecimal			Precio = BigDecimal.ZERO;
			public BigDecimal			Cantidad = BigDecimal.ZERO;
			public BigDecimal			ValorRescate = BigDecimal.ZERO;
			public BigDecimal			CorteMinimo = BigDecimal.ONE;	
			public BigDecimal			Monto	= BigDecimal.ZERO;
			public BigDecimal			MontoEnMonedaInstrumento = BigDecimal.ZERO;
			public BigDecimal			Tasa = BigDecimal.ZERO;
			public BigDecimal			TasaAgente = BigDecimal.ZERO;
			public String 				CodigoIFRS = "";
			public String 				EsCtaPropia = "N";
			public String 				IdRegimenFondoMutuo = "";
			public String				Procesar = "S";//Por defecto: S=Procesar; N=No Procesar
			public Long                 IdCorredora;  //Campo nuevo Entidad Origen para concepto TRACOR
			public String				EsInstrumentoCalificado = "N";
			
			//Para validaciones
			public long 				errores = 0;
			public long 				advertencias = 0;
			public long 				autorizaciones = 0;
			public long 				ok = 0;		
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class IngresoRetiroTitulosSalida {				
		public Long					P_OUT_ERRORESCONTADOR;
		public Long					P_OUT_TICKETATENCION;
		public List<ObjResultado>	P_OUT_RESULTADO;
		public List<MensajeSalida>	P_OUT_MENSAJES;

		public static class ObjResultado {
			public Long		IdGenerarMovimientoCustodia;
			public String	TablaOrigen;
			public Long		IdCustodia;
			public Long		FolioCustodia;
		}
		
		public IngresoRetiroTitulosSalida (){
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_MENSAJES 	= new ArrayList<MensajeSalida>();
			P_OUT_RESULTADO	= new ArrayList<ObjResultado>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public class ValidaAutorizacion extends BcsPojo{
		public Long		Id;
		public String	EsEnviadoAutorizar; 
		public String	Autorizado;
		public String 	Error;
	}
	
	public static class objMesas {
		public Long		IdMesa;
		public Long		IdPortafolio;
	}
	
	public static class Conceptos extends BcsPojo{
		public Long		IdMovimientoCarteraEnTransito;
		public String	IdConcepto;
		public String 	ConCertificadoAutomatico;
	}
	
	public static class Cliente extends BcsPojo{
		public Long		IdCliente;
		public String 	DocumentoIdentidad;
		public String	NombreCliente;
	    public String 	BloqueadoPorCumplimiento = "N";
	    public String   InversionistaCalificado = "N";
	}
	
	public static class RequisitosOperativosIngresoTitulos extends BcsPojo{
		public String IdCondicionOperativa;
		public String IdTipoEtapa;
		public String IdTipoCanalIngreso;
		public String IdTipoComportamiento;
		public String IdEstado;
		public String KeyRequisitos;
	}
	
	public static class MovimientoEnTransito{
		public Long IdMovimientoCarteraEnTransito;
	}
	
	private boolean fueEnviadoAutorizar = false;
	
	public IngresoRetiroTitulosSalida logica(Connection con, IngresoRetiroTitulosEntrada in) throws Exception {
		logger.info("INICIO::IngresoRetiroTitulosJava");
		SQL sql = new SQL();
		IngresoRetiroTitulosSalida out = new IngresoRetiroTitulosSalida();
		out.P_OUT_ERRORESCONTADOR = 0L;
	
		try {
			String conceptoNoValidaFlujoCustodia = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "CUSTODIA", "ConceptoNoValidaFlujoCustodia");
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");

			logger.debug("VALIDACIONES INICIALES");
			RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
			RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
			if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0)
				out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
				
			String estadoModulo = ConsultarEstadoModulo(in.p_IdEmpresa, in.p_IdUsuario, con);
			if (MODULO_ESTADO_CERRADO.equalsIgnoreCase(estadoModulo) && ("S").equals(in.p_UsaReglasNegocio)){
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"MODULO_CERRADO",new HashMap<String, String>(0),con,in.p_IdEmpresa));
				out.P_OUT_ERRORESCONTADOR++;
				return out;
			}
			
			List<ObjInstrumento> instrumentos = in.P_INSTRUMENTOS;
			List<ObjInstrumento> instrumentosConErrores = new ArrayList<>();
			
			List<Long> idsClientes = new ArrayList<Long>();
			List<Cliente> lstCliente = new ArrayList<Cliente>();
			HashMap<Long, Object> hashCliente = new HashMap<Long, Object>();
			
			for (ObjInstrumento inst : instrumentos){				
				if (!idsClientes.contains(inst.IdCliente)){
					idsClientes.add(inst.IdCliente);
				}
				if(("S").equals(inst.esTraspasoCliente)) {
					idsClientes.add(inst.IdClienteDestino);
				}
			}
			
			if (!idsClientes.isEmpty()){
				HashMap<String,Object> paramsClientes = new HashMap<>(2);
				paramsClientes.put("IdEmpresa", in.p_IdEmpresa);
				paramsClientes.put("IdsClientes", idsClientes);
				lstCliente = sql.query(con, SQL_SELECT_CLIENTE, paramsClientes, Cliente.class);
				hashCliente =  Utils.List2Hash(lstCliente, "IdCliente");
			}			
			
			//Busca Requisito Operativo 
			HashMap<String,Object> paramsRequisitoOperativo = new HashMap<>(1);
			paramsRequisitoOperativo.put("IdEmpresa", in.p_IdEmpresa);
			
			List<RequisitosOperativosIngresoTitulos> requisitosOperativos = sql.query(con, SQL_SELECT_REQUISITO_OPERATIVO, paramsRequisitoOperativo, RequisitosOperativosIngresoTitulos.class);
			HashMap<String, Object> hashRequisitosOperativos = Utils.List2Hash(requisitosOperativos, "KeyRequisitos");
			
			for (ObjInstrumento inst : instrumentos){			
				Cliente infoCliente = (Cliente) hashCliente.get(inst.IdCliente);
				Cliente infoClienteDestino = (Cliente) hashCliente.get(inst.IdClienteDestino);
				
				ProcesaRequisito(con, in.p_IdEmpresa,(inst.IdInstrumento == null),"INSTRUMENTO_NO_EXISTE", MensajeSalida.MENSAJE_ERROR, inst, hashRequisitosOperativos, out.P_OUT_MENSAJES);
				ProcesaRequisito(con, in.p_IdEmpresa,(inst.IdClasificacionTipoInstrumento == null),"DEBE_INDICAR_CLASIFICACION_INSTRUMENTO", MensajeSalida.MENSAJE_ERROR, inst, hashRequisitosOperativos, out.P_OUT_MENSAJES);
				ProcesaRequisito(con, in.p_IdEmpresa,(("IF").equalsIgnoreCase(inst.IdClasificacionTipoInstrumento) && (inst.IdEmisor == null || inst.IdEmisor.isEmpty())),"DEBE_INGRESAR_EMISOR_INSTRUMENTO", MensajeSalida.MENSAJE_ERROR, inst, hashRequisitosOperativos, out.P_OUT_MENSAJES);
				ProcesaRequisito(con, in.p_IdEmpresa,(("IF").equalsIgnoreCase(inst.IdClasificacionTipoInstrumento) && (inst.IdTipoReajuste == null || inst.IdTipoReajuste.isEmpty())),"DEBE_INGRESAR_TREAJUSTE_INSTRUMENTO", MensajeSalida.MENSAJE_ERROR, inst, hashRequisitosOperativos, out.P_OUT_MENSAJES);
				ProcesaRequisito(con, in.p_IdEmpresa,(("IF").equalsIgnoreCase(inst.IdClasificacionTipoInstrumento) && (("0001-01-01").equals(inst.FechaVencimientoInstrumento) || inst.FechaVencimientoInstrumento.isEmpty())),"DEBE_INGRESAR_FECVENCIMI_INSTRUMENTO", MensajeSalida.MENSAJE_ERROR, inst, hashRequisitosOperativos, out.P_OUT_MENSAJES);						
				ProcesaRequisito(con, in.p_IdEmpresa,(inst.Monto.compareTo(BigDecimal.ZERO) == 0 || inst.MontoEnMonedaInstrumento.compareTo(BigDecimal.ZERO) == 0),"MONTO_INGRESO_IGUAL_CERO_VALORIZAR_DESPUES", MensajeSalida.MENSAJE_ADVERTENCIA, inst, hashRequisitosOperativos, out.P_OUT_MENSAJES);
				ProcesaRequisito(con, in.p_IdEmpresa,(infoCliente != null && ("S").equalsIgnoreCase(infoCliente.BloqueadoPorCumplimiento) && ("INGTIT").equalsIgnoreCase(inst.IdTipoOperacion)),"CLIENTE_BLOQUEADO_POR_CUMPLIMIENTO", MensajeSalida.MENSAJE_ERROR, inst, hashRequisitosOperativos, out.P_OUT_MENSAJES);
				
				if(inst.errores == 0 && ("RV").equals(inst.IdClasificacionTipoInstrumento)) {
					if(("INGTIT").equalsIgnoreCase(inst.IdTipoOperacion)) {
						ProcesaRequisito(con,in.p_IdEmpresa,(("N").equals(infoCliente.InversionistaCalificado) && ("S").equals(inst.EsInstrumentoCalificado)),"CLIENTE_NO_OPERA_INSTRUMENTO_CALIFICADO","", inst, hashRequisitosOperativos, out.P_OUT_MENSAJES);
					}
					
					if(("S").equals(inst.esTraspasoCliente) && ("RETTIT").equalsIgnoreCase(inst.IdTipoOperacion)) {
						ProcesaRequisito(con,in.p_IdEmpresa,(("N").equals(infoClienteDestino.InversionistaCalificado) && ("S").equals(inst.EsInstrumentoCalificado)),"CLIENTE_NO_OPERA_INSTRUMENTO_CALIFICADO","", inst, hashRequisitosOperativos, out.P_OUT_MENSAJES);
					}
				}
			}
			
			if(in.p_MovimientosEnTransito.isEmpty()) {
				limpiarInstrumentos(instrumentos, instrumentosConErrores);
				if (instrumentos.size() == 0) {
					out.P_OUT_ERRORESCONTADOR += MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
					return out;
				}
			}
			
			logger.debug("Validar Cuenta Inversion para Retiro de titulos");		
			ValidaCantidadRetiroCustodia validacionRetiroCustodia = new ValidaCantidadRetiroCustodia();
			EntradaValidacionCustodia entradaValidacionRetiroCustodia = new EntradaValidacionCustodia();
			HashMap<Long, ValidaAutorizacion> hashValidacionRetiroCustodia = new HashMap<>();
			boolean dobleCheckCta = false;
			
			List<ObjInstrumento> instrumentosRetiroTitulo = instrumentos.stream().filter(inst -> ("RETTIT").equalsIgnoreCase(inst.IdTipoOperacion)).collect(Collectors.toList());
			
			if(!instrumentosRetiroTitulo.isEmpty()) {
				entradaValidacionRetiroCustodia.p_IdEmpresa = in.p_IdEmpresa;
				entradaValidacionRetiroCustodia.p_IdUsuario = in.p_IdUsuario;
				entradaValidacionRetiroCustodia.p_IdModulo = in.p_IdModulo;
				entradaValidacionRetiroCustodia.p_IdCanalIngreso = in.p_CanalIngreso;
				entradaValidacionRetiroCustodia.p_Instrumentos = instrumentosRetiroTitulo;
					
				SalidaValidacionCustodia salidaValidacionRetiroCustodia = validacionRetiroCustodia.logica(con, entradaValidacionRetiroCustodia);
				
				for (MensajeSalida mensajeValidado : salidaValidacionRetiroCustodia.P_OUT_MENSAJES) {
					
					ValidaAutorizacion resultadoValidacionRetiroCustodia = new ValidaAutorizacion();
				    
					switch (mensajeValidado.tipo) {
				        case "ERROR":
				        	resultadoValidacionRetiroCustodia = configurarValidaAutorizacion(mensajeValidado, "N", "N", "S");
				        	out.P_OUT_MENSAJES.add(mensajeValidado);
				        	out.P_OUT_ERRORESCONTADOR += 1L;
				            break;
				        case "AUTORIZACION":
				        	resultadoValidacionRetiroCustodia = configurarValidaAutorizacion(mensajeValidado, "N", "S", "N");
				        	out.P_OUT_MENSAJES.add(mensajeValidado);
				            break;
				        case "OK":
				        	resultadoValidacionRetiroCustodia = configurarValidaAutorizacion(mensajeValidado, "N", "N", "N");
				            break;
				       
				    }

				    hashValidacionRetiroCustodia.put(resultadoValidacionRetiroCustodia.Id, resultadoValidacionRetiroCustodia);
				}
				
				//Valida si tiene el check de Sobregiro Cuenta Inversion y usa cuenta inversion en el indicador y si el saldo es negativo. 
				if(("CERTIFICACION").equals(salidaValidacionRetiroCustodia.P_OUT_TIPOCOMPORTAMIENTO)) {
					dobleCheckCta = true;
					instrumentos.forEach(c -> c.DebeCertificar ="N");
				}
			}
			
			//******************************************************************************************************************
			//													LOGICA
			//******************************************************************************************************************
			logger.debug("01 - LOG (Requerido)");
			out.P_OUT_TICKETATENCION = LogCustodia.logIngresoRetiroTitulos(con, in);
			if (out.P_OUT_TICKETATENCION.equals(0L)){
				//TODO : incorporar mensajes de error
				out.P_OUT_ERRORESCONTADOR++;				
				return out; 
			}
			
			//IMPMBIOPT-617 se obtiene mesa de la corredora (por defecto o de menor id) sin verificar si Cliente es Cuenta Propia
			List<objMesas> mesas = new ArrayList<objMesas>();
			List<Long> idsPortafolio = null;

			if (instrumentos.stream().anyMatch(i -> ("IF").equals(i.IdClasificacionTipoInstrumento) || ("RF").equals(i.IdClasificacionTipoInstrumento))) {
				//IMPTANNOPT-271 se incorpora IdPortafolio al buscar la Mesa
				idsPortafolio = instrumentos.parallelStream().map(i -> i.IdPortafolio).distinct().collect(Collectors.toList());
				
				HashMap<String,Object> paramsMesas = new HashMap<>(3);
				paramsMesas.put("IdEmpresa", in.p_IdEmpresa);
				paramsMesas.put("IdPortafolio", idsPortafolio);
				paramsMesas.put("filtroIdPortafolio", 1);
				mesas = sql.query(con, SQL_SELECT_MESAS, paramsMesas, objMesas.class);
				
				if (mesas.isEmpty()) {
					//Si el Portafolio no tiene mesa, se busca la mesa por defecto o la primera
					paramsMesas.put("filtroIdPortafolio", 0);
					mesas = sql.query(con, SQL_SELECT_MESAS, paramsMesas, objMesas.class);
				}
			}
			
			logger.debug("02 - CREAR U OBTENER MOVIMIENTO EN TRANSITO");
			List<Long> idMovimientosEnTransito = new ArrayList<Long>();
			List<InformacionConceptosCustodia.ObjDatosEntrada> idConceptos = new ArrayList<InformacionConceptosCustodia.ObjDatosEntrada>();
			List<ObjTABLAEVENTOS> listEventos = new ArrayList<ObjTABLAEVENTOS>();
			HashMap<Long,String> hashConcepto = new HashMap<>();
			List<Conceptos> conceptos = new ArrayList<>();
			
			if (("N").equals(in.p_Certifica)) {
				List<ObjDatosEntrada> movimientosTransito  = new ArrayList<ObjDatosEntrada>();
				for (ObjInstrumento inst : instrumentos ){
					
					ValidaAutorizacion resultadoValidacionRetiroCustodia = hashValidacionRetiroCustodia.get(inst.IdGenerarMovimientoCustodia);
				    boolean tieneErroresRequisitos = inst.errores > 0;
				    boolean tieneErrorRetiroCustodia = resultadoValidacionRetiroCustodia != null && ("S").equals(resultadoValidacionRetiroCustodia.Error);
				    
					if(tieneErroresRequisitos || tieneErrorRetiroCustodia) {
						inst.Procesar = "N";
					}else if (in.p_ExisteMovEnTransito) { // ZORRO 851
						inst.Procesar = "N";						
						Long moventrans = in.p_MovimientosEnTransito.get(0);
						listEventos = agregarEventoAutorizacion(inst, in, listEventos, moventrans);
						enviarEventoAutorizacion(in, con, listEventos);
						fueEnviadoAutorizar = true;
					}
					
					if(inst.Procesar != null && ("N").equals(inst.Procesar)) continue;
					
					ObjDatosEntrada arg0 = new ObjDatosEntrada();
					arg0.IdGenerarMovimientoCustodia		= inst.IdGenerarMovimientoCustodia;																					
					arg0.TablaAfectada						= "TRANSITO";																																			//zorro 851
					arg0.AccionOperacion					= ("S").equalsIgnoreCase(inst.esTraspasoBoveda) || ("S").equalsIgnoreCase(inst.esTraspasoCliente) || ("S").equalsIgnoreCase(inst.esTraspasoPortafolio)|| ("RETTIT").equalsIgnoreCase(inst.IdTipoOperacion)  ? "RETIRO" : "INGRESO";
					arg0.EsRegistroNuevo					= "S";
					//arg0.FolioCarteraOrigen				= inst.FolioCartera;	//Para caso de retiro desde cartera					
					arg0.FolioCustodia						= inst.FolioCartera;	//Para caso de retiro desde cartera
					arg0.IdCliente							= inst.IdCliente; 
					arg0.IdPortafolio						= inst.IdPortafolio;
					arg0.IdCuentaCustodioPortafolio			= inst.IdCuentaCustodioPortafolio;
					arg0.IdClienteRelacionado				= ("S").equalsIgnoreCase(inst.esTraspasoCliente) ? inst.IdClienteDestino : null;
					arg0.IdPortafolioRelacionado			= ("S").equalsIgnoreCase(inst.esTraspasoPortafolio) ? inst.IdPortafolioDestino : null;
					arg0.FechaOperacion						= Fecha.toDate(inst.FechaOperacion);
					arg0.FechaOperacionOriginal				= Fecha.toDate(inst.FechaOperacionOriginal); // kkk
					arg0.FechaVencimientoOperacion			= Fecha.toDate(inst.FechaVencimiento);
					arg0.PlazoEnDiasOperacion				= inst.PlazoEnDias;
					arg0.IdTipoOperacion					= inst.IdTipoOperacion;		//debiera cambiar si es traspaso?
					arg0.IdTipoProducto						= inst.IdTipoProducto;
					arg0.IdConcepto							= inst.IdConcepto;
					arg0.IdCondicionLiquidacion				= inst.IdCondicionLiquidacion; 
					arg0.FechaLiquidacion					= Fecha.toDate(inst.FechaLiquidacion);
					arg0.IdTipoCanalIngreso					= inst.IdTipoCanalIngreso;
					arg0.FolioExternoReferencia				= inst.FolioExternoReferencia;
					arg0.IdBolsa							= inst.IdBolsa;	
					arg0.IdUsuario							= in.p_IdUsuario;
					arg0.IdSucursal							= inst.IdSucursal; 
					arg0.IdAgente							= inst.IdAgente; 
					arg0.Glosa								= inst.Glosa;
					arg0.IdInstrumento						= inst.IdInstrumento;
					arg0.Nemotecnico						= inst.Nemotecnico;
					arg0.IdMonedaInstrumento				= inst.IdMonedaInstrumento;
					arg0.IdMonedaEmision					= inst.IdMonedaEmision;
					arg0.IdTipoReajusteInstrumento			= inst.IdTipoReajuste;
					arg0.IdEmisor							= inst.IdEmisor;
					arg0.EsFungible							= inst.Fungible;
					arg0.IdBoveda							= inst.IdBoveda;
					arg0.CuentaBoveda						= inst.CuentaBoveda;
					arg0.EsNominal							= inst.IndicadorNominal;			
					arg0.Cantidad							= inst.Cantidad; 
					arg0.CorteMinimo						= ("RV").equalsIgnoreCase(inst.IdClasificacionTipoInstrumento) ? null : inst.CorteMinimo; 
					arg0.ValorRescate						= ("IF").equalsIgnoreCase(inst.IdClasificacionTipoInstrumento) ? inst.ValorRescate : null; 
					arg0.PrecioCompra						= inst.Precio; 
					arg0.TasaCompra							= inst.Tasa;
					arg0.MontoCompra						= inst.Monto; 
					arg0.MontoCompraMonedaInstrumento		= inst.MontoEnMonedaInstrumento;
					arg0.FechaVencimientoInstrumento		= Fecha.toDate(inst.FechaVencimientoInstrumento);
					arg0.FechaEmisionInstrumento			= Fecha.toDate(inst.FechaEmisionInstrumento);
					arg0.CodigoIFRS 						= inst.CodigoIFRS;
					arg0.IdMonedaAdicional					= inst.IdMonedaAdicional;
					arg0.IdRegimenFondoMutuo                = inst.IdRegimenFondoMutuo;
					arg0.IdCorredora                        = inst.IdCorredora;
					arg0.EsPendienteAutorizacion 			= "N";
					
					//IMPMBIOPT-617 se obtiene mesa de la corredora (por defecto o de menor id) sin verificar si Cliente es Cuenta Propia				
					arg0.IdMesa = (("IF").equalsIgnoreCase(inst.IdClasificacionTipoInstrumento) || ("RF").equalsIgnoreCase(inst.IdClasificacionTipoInstrumento)) && !mesas.isEmpty() ? mesas.get(0).IdMesa : 0L;
					
					arg0.MueveCarteraEnTransito = conceptoNoValidaFlujoCustodia.contains(inst.IdConcepto) ? "N" : "S";
					
					if (("S").equalsIgnoreCase(inst.esTraspasoBoveda) || ("S").equalsIgnoreCase(inst.esTraspasoCliente) || ("S").equalsIgnoreCase(inst.esTraspasoPortafolio)){
						ObjDatosEntrada arg1 = new ObjDatosEntrada();
						arg1.IdGenerarMovimientoCustodia		= -1*inst.IdGenerarMovimientoCustodia;
						arg1.IdMovimientoCarteraEnTransitoRelacionado	= inst.IdGenerarMovimientoCustodia;
						arg1.TablaAfectada						= "TRANSITO";
						arg1.AccionOperacion					= "INGRESO";
						arg1.EsRegistroNuevo					= "S";
						//arg1.FolioCarteraOrigen				= inst.FolioCartera;	//Para caso de retiro desde cartera
						arg1.FolioCustodia						= inst.FolioCartera;	//Para caso de retiro desde cartera
						arg1.IdCliente 							= ("S").equalsIgnoreCase(inst.esTraspasoCliente) ? inst.IdClienteDestino : inst.IdCliente;
						arg1.IdPortafolio 						= (("S").equalsIgnoreCase(inst.esTraspasoPortafolio) || ("S").equalsIgnoreCase(inst.esTraspasoCliente)) ? inst.IdPortafolioDestino : inst.IdPortafolio;						
						arg1.IdCuentaCustodioPortafolio 		= ("S").equalsIgnoreCase(inst.esTraspasoBoveda) ? inst.IdCuentaCustodioPortafolioRelacionado : inst.IdCuentaCustodioPortafolio;
						arg1.IdClienteRelacionado 				= ("S").equalsIgnoreCase(inst.esTraspasoCliente) ? inst.IdCliente : null;
						arg1.IdPortafolioRelacionado 			= (("S").equalsIgnoreCase(inst.esTraspasoPortafolio) || ("S").equalsIgnoreCase(inst.esTraspasoCliente)) ? inst.IdPortafolio : null;		
						arg1.FechaOperacion						= Fecha.toDate(inst.FechaOperacion);
						arg1.FechaOperacionOriginal				= Fecha.toDate(inst.FechaOperacionOriginal); // kkk
						arg1.FechaVencimientoOperacion			= Fecha.toDate(inst.FechaVencimiento);
						arg1.PlazoEnDiasOperacion				= inst.PlazoEnDias;
						arg1.IdTipoOperacion					= "INGTIT";		//debiera cambiar si es traspaso?
						arg0.IdTipoProducto						= inst.IdTipoProducto;
						arg1.IdConcepto							= inst.IdConcepto;
						arg1.IdCondicionLiquidacion				= inst.IdCondicionLiquidacion; 
						arg1.FechaLiquidacion					= Fecha.toDate(inst.FechaLiquidacion);
						arg1.IdTipoCanalIngreso					= inst.IdTipoCanalIngreso;
						arg1.FolioExternoReferencia				= inst.FolioExternoReferencia;
						arg1.IdBolsa							= inst.IdBolsa;	
						arg1.IdUsuario							= in.p_IdUsuario;
						arg1.IdSucursal							= inst.IdSucursal; 
						arg1.IdAgente							= inst.IdAgente; 
						arg1.Glosa								= inst.Glosa;
						arg1.IdInstrumento						= inst.IdInstrumento;
						arg1.Nemotecnico						= inst.Nemotecnico;
						arg1.IdMonedaInstrumento				= inst.IdMonedaInstrumento;
						arg1.IdMonedaEmision					= inst.IdMonedaEmision;
						arg1.IdTipoReajusteInstrumento			= inst.IdTipoReajuste;
						arg1.IdEmisor							= inst.IdEmisor;
						arg1.EsFungible							= inst.Fungible;
						arg1.IdBoveda							= ("S").equals(inst.esTraspasoBoveda) ? inst.IdBovedaRelacionada : inst.IdBoveda;
						arg1.CuentaBoveda						= ("S").equals(inst.esTraspasoBoveda) ? inst.CuentaBovedaRelacionada : inst.CuentaBoveda;
						arg1.EsNominal							= inst.IndicadorNominal;			
						arg1.Cantidad							= inst.Cantidad; 
						arg1.CorteMinimo						= ("RV").equalsIgnoreCase(inst.IdClasificacionTipoInstrumento) ? null : inst.CorteMinimo; 
						arg1.ValorRescate						= ("IF").equalsIgnoreCase(inst.IdClasificacionTipoInstrumento) ? inst.ValorRescate : null; 
						arg1.PrecioCompra						= inst.Precio; 
						arg1.TasaCompra							= inst.Tasa;
						arg1.MontoCompra						= inst.Monto; 
						arg1.MontoCompraMonedaInstrumento		= inst.MontoEnMonedaInstrumento;						
						arg1.FechaVencimientoInstrumento		= Fecha.toDate(inst.FechaVencimientoInstrumento);
						arg1.FechaEmisionInstrumento			= Fecha.toDate(inst.FechaEmisionInstrumento);
						arg1.PermiteCertificacionPorUsuario		= "N";
						arg1.CodigoIFRS 						= inst.CodigoIFRS;
						arg1.IdMonedaAdicional					= inst.IdMonedaAdicional;
						arg1.IdRegimenFondoMutuo                = inst.IdRegimenFondoMutuo;
						arg1.EsPendienteAutorizacion 			= "N";
						//IMPMBIOPT-617 se obtiene mesa de la corredora (por defecto o de menor id) sin verificar si Cliente es Cuenta Propia
						arg1.IdMesa = (("IF").equalsIgnoreCase(inst.IdClasificacionTipoInstrumento) || ("RF").equalsIgnoreCase(inst.IdClasificacionTipoInstrumento)) && !mesas.isEmpty() ? mesas.get(0).IdMesa : 0L;
						
						if(("S").equalsIgnoreCase(inst.esTraspasoCliente) && inst.autorizaciones > 0  && ("N").equals(in.p_simularOperacion)) {
							arg1.EsPendienteAutorizacion = "S";
						}
						
						movimientosTransito.add(arg1);
					}
							
					//Envia Movimiento en transito como Pendiente por Autorizar y crea evento
					boolean esRetiroConAutorizacion = ("RETTIT").equalsIgnoreCase(inst.IdTipoOperacion) && resultadoValidacionRetiroCustodia != null && ("S").equals(resultadoValidacionRetiroCustodia.EsEnviadoAutorizar) && ("N").equals(in.p_simularOperacion);
					boolean esIngresoConAutorizacion = ("INGTIT").equalsIgnoreCase(inst.IdTipoOperacion) && inst.autorizaciones > 0 && ("N").equals(in.p_simularOperacion);					
					boolean esTraspasoClienteConAutorizacion = ("S").equals(inst.esTraspasoCliente) && inst.autorizaciones > 0 && ("N").equals(in.p_simularOperacion);
					
					if (esRetiroConAutorizacion || esIngresoConAutorizacion || esTraspasoClienteConAutorizacion) {
						arg0.EsPendienteAutorizacion = "S";
						listEventos = agregarEventoAutorizacion(inst, in, listEventos, 0L);						    
					}
					
					movimientosTransito.add(arg0);
					
					InformacionConceptosCustodia.ObjDatosEntrada obj = new InformacionConceptosCustodia.ObjDatosEntrada();
					Utils.copyPojo(arg0, obj);
					obj.IdInstrumento = null;
					obj.IdNombreEntidad = "CUS.INGRESORETIROTITULO";
					idConceptos.add(obj);
					
				}
				
				if(fueEnviadoAutorizar == false) {
					IngresoCustodia ingresoCustodia = new IngresoCustodia();
					IngresoCustodia.IngresoCustodiaEntrada paramInSP = new IngresoCustodia.IngresoCustodiaEntrada();
					paramInSP.p_IdEmpresa			= in.p_IdEmpresa;			
					paramInSP.p_IdModulo			= in.p_IdModulo;
					paramInSP.p_SimularOperacion	= in.p_simularOperacion;
					paramInSP.p_TicketAtencion		= out.P_OUT_TICKETATENCION;
					paramInSP.p_DatosEntrada		= movimientosTransito;		
					paramInSP.p_ValorizacionAutomatica = "N";
					IngresoCustodia.IngresoCustodiaSalida resultIngresoTransito = ingresoCustodia.logica(con, paramInSP);
						
					for (IngresoCustodia.IngresoCustodiaSalida.ObjDatosSalida result : resultIngresoTransito.P_OUT_DATOSSALIDA){
						for(ObjTABLAEVENTOS evento : listEventos) {
							if(evento.IdUnion.equals(result.IdGenerarMovimientoCustodia)) {
								evento.Glosa = "Movimiento en Transito Numero: " + result.IdCustodia.toString();
								evento.NumeroOperacionConcepto = result.IdCustodia.toString();
							}
						}
	
						hashValidacionRetiroCustodia.put(result.IdCustodia, hashValidacionRetiroCustodia.get(result.IdGenerarMovimientoCustodia));
						hashValidacionRetiroCustodia.remove(result.IdGenerarMovimientoCustodia);
	
						if (("S").equalsIgnoreCase(result.RegistroConProblemas)){
							//TODO : incorporar mensajes de error
							out.P_OUT_ERRORESCONTADOR++;
							continue;
						}
						idMovimientosEnTransito.add(result.IdCustodia);
					}
				}		
			} else {
				idMovimientosEnTransito.addAll(in.p_MovimientosEnTransito);
				
				//busco conceptos de los movimientos
				HashMap<String,Object> params = new HashMap<>(1);
				params.put("IdMovimientoCarteraEnTransito", idMovimientosEnTransito);
				
				conceptos = sql.query(con, SQL_SELECT_CONCEPTOS, params, Conceptos.class);
				
				if(conceptos.isEmpty()) {
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "CONCEPTO_NO_EXISTE", new HashMap<String, String>(0), con, in.p_IdEmpresa));
					out.P_OUT_ERRORESCONTADOR++;
					return out;
				}
				
				// Construir el mapa de conceptos
				conceptos.forEach(concepto -> hashConcepto.put(concepto.IdMovimientoCarteraEnTransito, concepto.ConCertificadoAutomatico));
			}
			
			//envia todos los eventos
			if (!listEventos.isEmpty() && fueEnviadoAutorizar == false) {
				enviarEventoAutorizacion(in, con, listEventos);
			}
						
			logger.debug("03 - PROCESAR MOVIMIENTO EN TRANSITO"); 
			List<GestionMovimientoCarteraEnTransito.ObjMovimientosEnTransito> movimientosEnTransito =  GestionMovimientoCarteraEnTransito.ObtenerMovimientoEnTransito(con, idMovimientosEnTransito);
			List<GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar> p_Transitos = new ArrayList<GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar>();
			List<IngresoCustodia.ObjDatosEntrada> p_IngresoCartera = new ArrayList<IngresoCustodia.ObjDatosEntrada>();
			List<RetiroCustodia.ObjDatosEntrada> p_RetiroCartera = new ArrayList<RetiroCustodia.ObjDatosEntrada>();
			
			
			if(!instrumentos.isEmpty()) {
				//obtengo retiros de titulo asociados a ingresos de titulo (traspasos)
				Set<Long> idsRelacionadosTraspasoClientes = movimientosEnTransito.stream()
				        .filter(mov -> ("INGTIT").equals(mov.IdTipoOperacion) && ("RETTRC").equals(mov.IdConcepto) && ("PDA").equals(mov.IdEstado) && mov.IdMovimientoCarteraEnTransitoRelacionado != 0)
				        .map(mov -> mov.IdMovimientoCarteraEnTransitoRelacionado)
				        .collect(Collectors.toSet());
				
				//Filtrar y quitar pendientes de autorizar ingresos de título y retiros por trasapasos  (PDA)
				movimientosEnTransito = movimientosEnTransito.stream().filter(mov -> !(("PDA").equals(mov.IdEstado) && ("INGTIT").equals(mov.IdTipoOperacion)) &&
							!(("RETTIT").equals(mov.IdTipoOperacion) && idsRelacionadosTraspasoClientes.contains(mov.IdMovimientoCarteraEnTransito))).collect(Collectors.toList());
			}
			
			if (movimientosEnTransito.size() > 0){
				List<InformacionConceptosCustodia.ObjDatosSalida> conceptosCustodia = InformacionConceptosCustodia.informacionConcepto(con, idConceptos);
				//verifica si tiene el doble check de sobregiro cuenta inversion y tiene saldo negativo cta inversion, de ser asi, cualquier concepto con cert automatico tiene que ser certificado
				if(dobleCheckCta) {
					conceptosCustodia.forEach(c -> c.ConCertificadoAutomatico = "N");
				}
			
				HashMap<String,Object> hashConceptosCustodia = Utils.List2Hash(conceptosCustodia, "IdConcepto");
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				
				for (GestionMovimientoCarteraEnTransito.ObjMovimientosEnTransito movimientoEnTransito : movimientosEnTransito) {
					if (("N").equals(in.p_Certifica)){
						InformacionConceptosCustodia.ObjDatosSalida conceptoCustodia = (InformacionConceptosCustodia.ObjDatosSalida) hashConceptosCustodia.get(movimientoEnTransito.IdConcepto);
						
						if (!conceptoCustodia.IdConcepto.equalsIgnoreCase(movimientoEnTransito.IdConcepto)) continue;  //TODO indicar mensaje de error (error de configuracion concepto)
						
						if (("N").equals(conceptoCustodia.ConCertificadoAutomatico)){
							if (!movimientoEnTransito.IdMovimientoCarteraEnTransitoRelacionado.equals(0L)) continue; //No se muestra en UI							
							
							HashMap<String,String> valores = new HashMap<String,String>(1);
							valores.put("Folio", movimientoEnTransito.IdMovimientoCarteraEnTransito.toString());
							String mensaje = ("INGTIT").equals(movimientoEnTransito.IdTipoOperacion) ? "UI_INGRESO_TITULO_TRANSITO_OK" : "UI_EGRESO_TITULO_TRANSITO_OK";
							out.P_OUT_MENSAJES.add(new MensajeSalida(movimientoEnTransito.IdMovimientoCarteraEnTransito, MensajeSalida.MENSAJE_ADVERTENCIA, mensaje, valores ,con,in.p_IdEmpresa));
							continue;
						}else {
							hashConcepto.put(movimientoEnTransito.IdMovimientoCarteraEnTransito, conceptoCustodia.ConCertificadoAutomatico);
						}
					}
					
					logger.debug("04 - Construir ingresos de titulo");
					if (("INGTIT").equalsIgnoreCase(movimientoEnTransito.IdTipoOperacion) && ("S").equalsIgnoreCase(movimientoEnTransito.MueveCustodia)){
						IngresoCustodia.ObjDatosEntrada	ingreso = new IngresoCustodia.ObjDatosEntrada();
						ingreso.IdGenerarMovimientoCustodia			= movimientoEnTransito.IdMovimientoCarteraEnTransito;
						ingreso.TablaAfectada 						= "CARTERA";
						ingreso.EsRegistroNuevo						= "S";						
						ingreso.IdMovimientoCarteraEnTransitoOrigen	= movimientoEnTransito.IdMovimientoCarteraEnTransito;
						ingreso.AccionOperacion						= "INGRESO";		
						ingreso.IdCliente							= movimientoEnTransito.IdCliente;
						ingreso.IdPortafolio						= movimientoEnTransito.IdPortafolio; 
						ingreso.IdCuentaCustodioPortafolio			= movimientoEnTransito.IdCuentaCustodioPortafolio;
						
						if (("0001-01-01").equals(sdf.format(movimientoEnTransito.FechaOperacionOriginal))){
							ingreso.FechaOperacion					= movimientoEnTransito.FechaOperacion;
							ingreso.FechaComprobante				= movimientoEnTransito.FechaOperacion;
						}else{
							ingreso.FechaOperacion					= movimientoEnTransito.FechaOperacionOriginal;
							ingreso.FechaComprobante				= movimientoEnTransito.FechaOperacion;
						}
						
						ingreso.FechaVencimientoOperacion			= movimientoEnTransito.FechaVencimientoOperacion;
						ingreso.PlazoEnDiasOperacion				= movimientoEnTransito.PlazoEnDiasOperacion;
						ingreso.IdTipoOperacion						= movimientoEnTransito.IdTipoOperacion;	
						ingreso.IdTipoProducto						= movimientoEnTransito.IdTipoProducto;		
						ingreso.IdConcepto							= movimientoEnTransito.IdConcepto;
						ingreso.IdCondicionLiquidacion				= movimientoEnTransito.IdCondicionLiquidacion; 
						ingreso.FechaLiquidacion					= movimientoEnTransito.FechaLiquidacion;
						ingreso.IdTipoCanalIngreso					= movimientoEnTransito.IdTipoCanalIngreso; 		
						ingreso.CodigoIFRS							= movimientoEnTransito.CodigoIFRS;		 		 
						ingreso.IdTipoComprobante					= movimientoEnTransito.IdTipoComprobante; 
						ingreso.FolioComprobante					= movimientoEnTransito.FolioComprobante; 
						ingreso.IdBolsa								= movimientoEnTransito.IdBolsa;
						ingreso.EsIntraday							= movimientoEnTransito.EsIntraday; 
						ingreso.TipoCarteraCorredor					= movimientoEnTransito.TipoCarteraCorredor;
						ingreso.IdUsuario							= movimientoEnTransito.IdUsuario;
						ingreso.IdSucursal							= movimientoEnTransito.IdSucursal; 
						ingreso.IdAgente							= movimientoEnTransito.IdAgente;
						ingreso.Glosa								= movimientoEnTransito.Glosa;
						ingreso.IdInstrumento						= movimientoEnTransito.IdInstrumento;
						ingreso.Nemotecnico							= movimientoEnTransito.Nemotecnico;
						ingreso.IdMonedaInstrumento					= movimientoEnTransito.IdMonedaInstrumento;
						ingreso.IdTipoReajusteInstrumento			= movimientoEnTransito.IdTipoReajuste;
						ingreso.IdEmisor							= movimientoEnTransito.IdEmisor;
						ingreso.EsFungible							= movimientoEnTransito.EsFungible;
						ingreso.IdBoveda							= movimientoEnTransito.IdBoveda;
						ingreso.CuentaBoveda						= movimientoEnTransito.CuentaBoveda;
						ingreso.EsNominal							= movimientoEnTransito.EsNominal;
						ingreso.IdMonedaEmision						= movimientoEnTransito.IdMonedaEmision; 
						ingreso.Cantidad							= movimientoEnTransito.Cantidad; 
						ingreso.CorteMinimo							= movimientoEnTransito.CorteMinimo; 
						ingreso.ValorRescate						= movimientoEnTransito.ValorRescate; 
						ingreso.PrecioCompra						= movimientoEnTransito.PrecioCompra; 
						ingreso.TasaCompra							= movimientoEnTransito.TasaCompra;
						ingreso.MontoCompra							= movimientoEnTransito.MontoCompra;
						ingreso.MontoCompraMonedaInstrumento		= movimientoEnTransito.MontoCompraMonedaInstrumento;
						ingreso.FechaVencimientoInstrumento			= movimientoEnTransito.FechaVencimientoInstrumento;
						ingreso.FechaEmisionInstrumento				= movimientoEnTransito.FechaEmisionInstrumento;
						ingreso.IdMonedaAdicional					= movimientoEnTransito.IdMonedaAdicional;
						ingreso.IdOperacion							= movimientoEnTransito.IdMovimientoCarteraEnTransito;
						ingreso.IdMesa 								= movimientoEnTransito.IdMesa;
						ingreso.IdRegimenFondoMutuo                  = movimientoEnTransito.IdRegimenFondoMutuo;
					
						p_IngresoCartera.add(ingreso);
						
						ObjMovimientosAActualizar movimientoAActualizar = new ObjMovimientosAActualizar();
						movimientoAActualizar.IdMovimientoCarteraEnTransito = movimientoEnTransito.IdMovimientoCarteraEnTransito;
						movimientoAActualizar.EsCertificacion = "S";
						movimientoAActualizar.IdUsuario = in.p_IdUsuario;
						p_Transitos.add(movimientoAActualizar);
						
					} // END-IF INGTIT
										
					ValidaAutorizacion resultadoValidacionRetiroCustodia = hashValidacionRetiroCustodia.get(movimientoEnTransito.IdMovimientoCarteraEnTransito);
					String conceptoCertificadoAutomatico = hashConcepto.get(movimientoEnTransito.IdMovimientoCarteraEnTransito);
					
					logger.debug("05 - Construir retiros de titulo");
					if ((("RETTIT").equalsIgnoreCase(movimientoEnTransito.IdTipoOperacion) && ("S").equalsIgnoreCase(movimientoEnTransito.MueveCustodia))
						&&(   
						//no viene de movimiento en transito y no es enviado a autorizar pero es certificado automatico
							(in.p_MovimientosEnTransito.isEmpty() 
							&& resultadoValidacionRetiroCustodia != null
							&& ("N").equals(resultadoValidacionRetiroCustodia.Autorizado)
							&& ("N").equals(resultadoValidacionRetiroCustodia.EsEnviadoAutorizar)
							&& conceptoCertificadoAutomatico != null
							&& ("S").equals(conceptoCertificadoAutomatico)
						) 
							//viene de autorizaciones o certificacion con movimimiento en transito con autorizacion, autorizado
						|| (("S").equalsIgnoreCase(movimientoEnTransito.MueveCustodia)
							&& !in.p_MovimientosEnTransito.isEmpty()
							&& ("S").equals(in.p_Certifica)
						)) 
						&&(!conceptoNoValidaFlujoCustodia.contains(movimientoEnTransito.IdConcepto))) {
							String fecha = movimientoEnTransito.FechaLiquidacion.compareTo(Fecha.toDate(fechaSistema)) == -1 ? fechaSistema : Fecha.toStringDate(movimientoEnTransito.FechaLiquidacion);
							
							RetiroCustodia.ObjDatosEntrada	retiro = new RetiroCustodia.ObjDatosEntrada(); 
							retiro.IdGenerarMovimientoCustodia			= movimientoEnTransito.IdMovimientoCarteraEnTransito;
							retiro.TablaAfectada 						= "CARTERA";
							retiro.IdMovimientoCarteraEnTransitoOrigen	= movimientoEnTransito.IdMovimientoCarteraEnTransito;
							retiro.IdTipoComprobanteBusqueda			= ("CUS").equalsIgnoreCase(movimientoEnTransito.IdTipoComprobante) ? null : movimientoEnTransito.IdTipoComprobante;
							retiro.FolioComprobanteBusqueda				= ("CUS").equalsIgnoreCase(movimientoEnTransito.IdTipoComprobante) ? null : movimientoEnTransito.FolioComprobante;
							retiro.FolioCustodia						= movimientoEnTransito.FolioCustodia == 0 ? null : movimientoEnTransito.FolioCustodia;
							retiro.AccionOperacion						= ("VCDTIT").equals(movimientoEnTransito.IdConcepto) ? "VENCIMIENTO" : "RETIRO";
							retiro.IdCliente							= movimientoEnTransito.IdCliente;
							retiro.IdPortafolio							= movimientoEnTransito.IdPortafolio;	
							
							if (sdf.format(movimientoEnTransito.FechaOperacionOriginal).equals("0001-01-01")){
								retiro.FechaOperacion					= movimientoEnTransito.FechaOperacion;
								retiro.FechaComprobante					= movimientoEnTransito.FechaOperacion;
							}else{
								retiro.FechaOperacion					= movimientoEnTransito.FechaOperacionOriginal;
								retiro.FechaComprobante					= movimientoEnTransito.FechaOperacion;
							}
							
							retiro.IdTipoOperacion						= ("VCDTIT").equals(movimientoEnTransito.IdConcepto) ? "VCDTIT" : movimientoEnTransito.IdTipoOperacion;
							retiro.IdConcepto							= movimientoEnTransito.IdConcepto;
							retiro.IdCondicionLiquidacion				= movimientoEnTransito.IdCondicionLiquidacion; 
							retiro.FechaLiquidacion						= Fecha.toDate(fecha);
							retiro.IdTipoCanalIngreso					= movimientoEnTransito.IdTipoCanalIngreso; 		 
							retiro.IdTipoComprobante					= movimientoEnTransito.IdTipoComprobante; 
							retiro.IdTipoReajusteInstrumento			= movimientoEnTransito.IdTipoReajuste;
							retiro.FolioComprobante						= movimientoEnTransito.FolioComprobante;
							retiro.IdUsuario							= movimientoEnTransito.IdUsuario;
							retiro.IdSucursal							= movimientoEnTransito.IdSucursal; 
							retiro.IdAgente								= movimientoEnTransito.IdAgente;
							retiro.Glosa								= movimientoEnTransito.Glosa;
							retiro.IdInstrumento						= movimientoEnTransito.IdInstrumento;
							retiro.FechaVencimientoInstrumento			= movimientoEnTransito.FechaVencimientoInstrumento;
							retiro.IdBoveda								= movimientoEnTransito.IdBoveda;
							retiro.CuentaBoveda							= movimientoEnTransito.CuentaBoveda;
							retiro.IdEmisor								= movimientoEnTransito.IdEmisor;
							retiro.EsFungible							= movimientoEnTransito.EsFungible;
							retiro.IdMonedaEmision						= movimientoEnTransito.IdMonedaEmision; 
							retiro.Cantidad								= movimientoEnTransito.Cantidad; 
							retiro.CorteMinimo							= movimientoEnTransito.CorteMinimo; 
							retiro.ValorRescate							= movimientoEnTransito.ValorRescate; 
							retiro.PrecioCompra							= movimientoEnTransito.PrecioCompra; 
							retiro.TasaCompra							= movimientoEnTransito.TasaCompra;
							retiro.MontoCompraMonedaInstrumento			= movimientoEnTransito.MontoCompraMonedaInstrumento;
							retiro.IdMonedaAdicional					= movimientoEnTransito.IdMonedaAdicional;
							retiro.IdOperacion							= movimientoEnTransito.IdMovimientoCarteraEnTransito;
							retiro.IdClasificacionTipoInstrumento		= movimientoEnTransito.IdClasificacionTipoInstrumento;						
							retiro.IdMonedaInstrumento                  = movimientoEnTransito.IdMonedaInstrumento;
							
							if(("VCTSIC").equalsIgnoreCase(movimientoEnTransito.IdConcepto) && ("INSI").equalsIgnoreCase(movimientoEnTransito.IdTipoProducto) && ("RETTIT").equalsIgnoreCase(movimientoEnTransito.IdTipoOperacion)) {
								if(movimientoEnTransito.PrecioAPlazo != null) {
									retiro.SIMCarteraPrestadaAlFinancista = "S";
									retiro.PrecioAPlazo = movimientoEnTransito.PrecioAPlazo;
								}								
							}
							
							p_RetiroCartera.add(retiro);
					} // END-IF RETTIT
					
					boolean esCondicionValida = Stream.of(
						// Condicion 1: Retiro cartera no vacío
						!p_RetiroCartera.isEmpty(),
						// Condicion 2: Concepto no valida custodia
						conceptoNoValidaFlujoCustodia.contains(movimientoEnTransito.IdConcepto),
						// Condicion 3: Concepto ANTPRT con RETTIT y MueveCustodia "N"
						("ANTPRT").equalsIgnoreCase(movimientoEnTransito.IdConcepto) && ("RETTIT").equalsIgnoreCase(movimientoEnTransito.IdTipoOperacion) && ("N").equalsIgnoreCase(movimientoEnTransito.MueveCustodia),
						// Condicion 4: Concepto VCDTIT con RETTIT, IF/RF y MueveCustodia "N"
						("VCDTIT").equalsIgnoreCase(movimientoEnTransito.IdConcepto) && ("RETTIT").equalsIgnoreCase(movimientoEnTransito.IdTipoOperacion) && ("N").equalsIgnoreCase(movimientoEnTransito.MueveCustodia) 
						&& (("IF").equals(movimientoEnTransito.IdClasificacionTipoInstrumento) || ("RF").equals(movimientoEnTransito.IdClasificacionTipoInstrumento)),
						// Condicion 5: Concepto VCDTIT con RETTIT, IF/RF y MueveCustodia "S"
						("VCDTIT").equalsIgnoreCase(movimientoEnTransito.IdConcepto) && ("RETTIT").equalsIgnoreCase(movimientoEnTransito.IdTipoOperacion) && ("S").equalsIgnoreCase(movimientoEnTransito.MueveCustodia) 
						&& (("IF").equals(movimientoEnTransito.IdClasificacionTipoInstrumento) || ("RF").equals(movimientoEnTransito.IdClasificacionTipoInstrumento))
					).anyMatch(Boolean::booleanValue);

					if (esCondicionValida) {
					    ObjMovimientosAActualizar movimientoAActualizar = new ObjMovimientosAActualizar();
					    movimientoAActualizar.IdMovimientoCarteraEnTransito = movimientoEnTransito.IdMovimientoCarteraEnTransito;
					    movimientoAActualizar.EsCertificacion = "S";
					    movimientoAActualizar.IdUsuario = in.p_IdUsuario;
					    p_Transitos.add(movimientoAActualizar);
					}
				}
				
				logger.debug("06 - REALIZAR INGRESOS");
				if ( !p_IngresoCartera.isEmpty() ){
					IngresoCustodia ingresoCustodia = new IngresoCustodia();
					IngresoCustodia.IngresoCustodiaEntrada paramInSP = new IngresoCustodia.IngresoCustodiaEntrada();
					paramInSP.p_IdEmpresa			= in.p_IdEmpresa;		
					paramInSP.p_IdModulo			= in.p_IdModulo;
					paramInSP.p_SimularOperacion	= in.p_simularOperacion;
					paramInSP.p_TicketAtencion		= out.P_OUT_TICKETATENCION;
					paramInSP.p_DatosEntrada		= p_IngresoCartera;
					paramInSP.p_ValorizacionAutomatica = "N";
					IngresoCustodia.IngresoCustodiaSalida resultIngresos = ingresoCustodia.logica(con, paramInSP);				

					out.P_OUT_MENSAJES.addAll(resultIngresos.P_OUT_MENSAJES);
					
					if (resultIngresos.P_OUT_ERRORESCONTADOR.compareTo(0L) == 1){
						out.P_OUT_ERRORESCONTADOR+=resultIngresos.P_OUT_ERRORESCONTADOR;
						out.P_OUT_MENSAJES = out.P_OUT_MENSAJES.stream().filter(t -> t.tipo.equalsIgnoreCase(MensajeSalida.MENSAJE_ERROR)).collect(Collectors.toList());
						return out;
					}
					
					for (IngresoCustodia.IngresoCustodiaSalida.ObjDatosSalida result : resultIngresos.P_OUT_DATOSSALIDA){
						HashMap<String,String> valores = new HashMap<String,String>(1);
						valores.put("Folio", result.FolioCustodia.toString());
						
						ObjResultado resultadoProceso = new ObjResultado(); 
						Utils.copyPojo(result, resultadoProceso);
						out.P_OUT_RESULTADO.add(resultadoProceso);
						
						out.P_OUT_MENSAJES.add(new MensajeSalida(result.IdGenerarMovimientoCustodia ,MensajeSalida.MENSAJE_INFORMACION, "UI_INGRESO_TITULO_OK", valores ,con,in.p_IdEmpresa));
						
					}
					
					List<ClienteNeteo> clientesANetear = new ArrayList<>();
					for(IngresoCustodia.ObjDatosEntrada ingresoCartera : p_IngresoCartera) {
						ClienteNeteo clienteNeteo = new ClienteNeteo();
						clienteNeteo.IdInstrumento = ingresoCartera.IdInstrumento;
						clienteNeteo.IdPortafolio = ingresoCartera.IdPortafolio;
						clientesANetear.add(clienteNeteo);
					}
					
					if(clientesANetear.size() > 0) {
						EntradaNeteo entradaNeteo = new EntradaNeteo();
						entradaNeteo.p_IdEmpresa = in.p_IdEmpresa;
						entradaNeteo.p_IdModulo = "CUS";
						entradaNeteo.p_IdUsuario = in.p_IdUsuario;
						entradaNeteo.p_PortafoliosInstrumento = clientesANetear;
						entradaNeteo.p_SimularOperacion = "N";
						entradaNeteo.p_UsaReglasNegocio = "S";
						
						AgregarPrograma.InAgregarPrograma paramsIn = new AgregarPrograma.InAgregarPrograma(); 
						paramsIn.p_IdEmpresa		= in.p_IdEmpresa;
						paramsIn.p_IdModulo			= "CUS";
						paramsIn.p_IdUsuario		= in.p_IdUsuario;
						paramsIn.p_IdTipoPrograma	= "NeteoCarterasNegativas";
						paramsIn.p_Segundos			= 0L;
						paramsIn.p_FechaSistema		= fechaSistema;
						List<Parametro> paramsParametros = new ArrayList<>();
						Parametro parametro = new Parametro();
						parametro.NombreParametro = "Entrada";
						Gson gson = Utils.getGsonBuilder();
						parametro.Valor = "";
						parametro.ValorBlob = gson.toJson(entradaNeteo);
						paramsParametros.add(parametro);
						paramsIn.p_Parametros = paramsParametros ;
						AgregarPrograma AP = new AgregarPrograma();
						AP.logica(con, paramsIn);
					}
					
				}
				
				logger.debug("07 - REALIZAR RETIROS");
				if ( !p_RetiroCartera.isEmpty()){
					RetiroCustodia retiroCustodia = new RetiroCustodia();
					RetiroCustodia.RetiroCustodiaEntrada paramInSP = new RetiroCustodia.RetiroCustodiaEntrada();
					paramInSP.p_IdEmpresa			= in.p_IdEmpresa;
					paramInSP.p_SimularOperacion	= in.p_simularOperacion;
					paramInSP.p_TicketAtencion		= out.P_OUT_TICKETATENCION;
					paramInSP.p_DatosEntrada		= p_RetiroCartera;
					paramInSP.p_ValorizacionAutomatica = "N";
					RetiroCustodia.RetiroCustodiaSalida resultRetiros = retiroCustodia.logica(con, paramInSP);	
					
					out.P_OUT_MENSAJES.addAll(resultRetiros.P_OUT_MENSAJES);
					
					if (resultRetiros.P_OUT_ERRORESCONTADOR.compareTo(0L) == 1){
						out.P_OUT_ERRORESCONTADOR+=resultRetiros.P_OUT_ERRORESCONTADOR;
						out.P_OUT_MENSAJES = out.P_OUT_MENSAJES.stream().filter(t -> t.tipo.equalsIgnoreCase(MensajeSalida.MENSAJE_ERROR)).collect(Collectors.toList());
						return out;
					}
					String idMonedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
					
					for (RetiroCustodia.RetiroCustodiaSalida.ObjDatosSalida result : resultRetiros.P_OUT_DATOSSALIDA){
						for (RetiroCustodia.ObjDatosEntrada ret : p_RetiroCartera){
							if (!ret.IdGenerarMovimientoCustodia.equals(result.IdGenerarMovimientoCustodia)) continue;
							
							ObjResultado resultadoProceso = new ObjResultado(); 
							Utils.copyPojo(result, resultadoProceso);
							out.P_OUT_RESULTADO.add(resultadoProceso);
							
							HashMap<String,String> valores = new HashMap<String,String>(2);
							if(("IF").equalsIgnoreCase(ret.IdClasificacionTipoInstrumento)) {
								valores.put("Cantidad", ret.ValorRescate.toString());
							}else {
								//valores.put("Cantidad", ret.Cantidad.toString());
								String Cantidad=MonedaFormato.formatear(con, ret.Cantidad, MonedaFormato.CANTIDAD, idMonedaSistema).valor.toString();
								valores.put("Cantidad", Cantidad);			
							}
							
							valores.put("Folio", result.IdGenerarMovimientoCustodia.toString());
							
							out.P_OUT_MENSAJES.add(new MensajeSalida(result.IdGenerarMovimientoCustodia ,MensajeSalida.MENSAJE_INFORMACION, "UI_RETIRO_TITULO_OK", valores ,con,in.p_IdEmpresa));
							
						}						
					}
				}
				
				logger.debug("08 - ACTUALIZAR MOVIMIENTOS EN TRANSITO");
				if (out.P_OUT_ERRORESCONTADOR.equals(0L)){
					GestionMovimientoCarteraEnTransito.ActualizarMovimientoEnTransito(con, p_Transitos);
				}
			}
		}
		catch(Exception e) {
			throw e;
		}
		
		for (MensajeSalida resultMensajes : out.P_OUT_MENSAJES){
			if(("ERROR").equalsIgnoreCase(resultMensajes.tipo)) {
				List<MensajeSalida> filteredMensajes = out.P_OUT_MENSAJES.stream() .filter(mje -> mje.tipo.equals(MensajeSalida.MENSAJE_ERROR)).collect(Collectors.toList());
				out.P_OUT_MENSAJES.clear();
				out.P_OUT_MENSAJES.addAll(filteredMensajes);
				return out;
			}	
		}
		
		logger.debug("09 - VALORIZAR");
		CacheValorizacion.valorizar(con);
		if(out.P_OUT_ERRORESCONTADOR == 0 && out.P_OUT_MENSAJES.size() > 0) {
			agregarLog(con, in);
		}
		logger.info("TERMINO");
		System.out.println(in);
		return out;
	}

	private ValidaAutorizacion  configurarValidaAutorizacion(MensajeSalida mensaje, String autorizado, String enviadoAutorizar, String error) {
		ValidaAutorizacion validaAutorizacion = new ValidaAutorizacion();
	    validaAutorizacion.Id = mensaje.id;
	    validaAutorizacion.Autorizado = autorizado;
	    validaAutorizacion.EsEnviadoAutorizar = enviadoAutorizar;
	    validaAutorizacion.Error = error;
	    return validaAutorizacion; 
	}
	
	private List<ObjTABLAEVENTOS> agregarEventoAutorizacion(ObjInstrumento instrumento, IngresoRetiroTitulosEntrada in, List<ObjTABLAEVENTOS> listEventos, Long idMovimientoEnTransito ){
		ObjTABLAEVENTOS nuevoEvento = new ObjTABLAEVENTOS();
		nuevoEvento.IdUnion = instrumento.IdGenerarMovimientoCustodia;
		nuevoEvento.IdTipoEvento = instrumento.IdTipoOperacion;
		nuevoEvento.IdModuloSolicita = "CUS";
		nuevoEvento.IdUsuarioSolicita = in.p_IdUsuario;
		nuevoEvento.Fecha = instrumento.FechaOperacion.toString();
		nuevoEvento.IdTipoOperacion = instrumento.IdTipoOperacion;
		nuevoEvento.IdConcepto = instrumento.IdConcepto;
		nuevoEvento.IdCliente = instrumento.IdCliente;
		nuevoEvento.IdFuncionario = 0L;
		nuevoEvento.IdProveedor = 0L;
		nuevoEvento.IdTipoProducto = instrumento.IdTipoProducto;
		nuevoEvento.IdClasificacionTipoInstrumento = instrumento.IdClasificacionTipoInstrumento;
		nuevoEvento.IdTipoInstrumento = instrumento.IdTipoInstrumento;
		nuevoEvento.IdInstrumento = instrumento.IdInstrumento;
		nuevoEvento.Glosa = idMovimientoEnTransito != 0 ? "Movimiento en Transito Numero: " + idMovimientoEnTransito : "";
		nuevoEvento.NumeroOperacionConcepto = idMovimientoEnTransito != 0 ? idMovimientoEnTransito.toString() : "";
		
		listEventos.add(nuevoEvento);
		
		return listEventos;
	}
	
	private void enviarEventoAutorizacion(IngresoRetiroTitulosEntrada in, Connection con, List<ObjTABLAEVENTOS> listEventos) {
		if (!listEventos.isEmpty()) {
			CrearEventoAutorizacionMasivo crearAutorizacionService = new CrearEventoAutorizacionMasivo();
			ServiceEntrada entradaAutorizacionService = new ServiceEntrada();
			entradaAutorizacionService.P_IDEMPRESA = in.p_IdEmpresa;
			entradaAutorizacionService.P_IDUSUARIO = in.p_IdUsuario;
			entradaAutorizacionService.P_IDMODULO = in.p_IdModulo;
			entradaAutorizacionService.P_SIMULAROPERACION = "N";
			entradaAutorizacionService.P_USAREGLASNEGOCIO = "N";
			entradaAutorizacionService.P_TABLAEVENTOS = listEventos; 
		
			try {
				crearAutorizacionService.logica(con, entradaAutorizacionService);
			} catch (Exception e) {
				e.printStackTrace();
			}	
		}	
	}
	
	private void agregarLog(Connection con, IngresoRetiroTitulosEntrada in) {
		try {
			logger.debug("INICIO::IngresoRetiroTitulosJava.agregarLog");
			Long inicio = System.currentTimeMillis();
				
			List<AgregarLog.InAgregarLog.Log> listaLog = new ArrayList<>();
			String FechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			for (ObjInstrumento ins : in.P_INSTRUMENTOS) {
				//CABECERA
				AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
				
				log.IdTrabajo = 1L; 
				log.IdCabeceraLog = ins.IdGenerarMovimientoCustodia;
				log.IdTipoLog = ("INGTIT").equalsIgnoreCase(ins.IdTipoOperacion) ? "INGRESO" : "RETIRO";
				log.IdModulo = in.p_IdModulo;
				log.IdUsuario = in.p_IdUsuario;
				log.IdEmpresa = in.p_IdEmpresa;
				log.FechaSistemaLog = FechaSistema;
				log.IdUsuarioAtendido = in.p_IdUsuario;
				log.IdCliente = ins.IdCliente;
				log.EstadoCliente = "";
				log.NombreCliente = "";
				log.IdTipoDocumentoIdentidad = "";
				log.IdDocumentoIdentidad = "";
				log.IdPortafolio = ins.IdPortafolio;
				log.EstadoPortafolio = "";
				log.IdTipoRegistro = ("INGTIT").equalsIgnoreCase(ins.IdTipoOperacion) ? "INGRESOTITULO" : "RETIROTITULO";;
				log.IdRegistro = "";
				log.EstadoRegistro = "";
				log.IdTipoOperacion = ins.IdTipoOperacion;
				log.Folio = 0L;
				log.Cantidad = ins.Cantidad;
				log.PrecioTasa = ins.Precio;
				log.Monto = ("IF").equalsIgnoreCase(ins.IdClasificacionTipoInstrumento) ? ins.ValorRescate : ins.Monto;
				log.IdMonedaOrigen = ins.IdMonedaInstrumento;
				log.IdMonedaLiquidacion = "";
				log.TipoCambio = "";
				log.Nemo = ins.Nemotecnico;
				log.IdProceso = 0L;
				log.EstadoProceso = "";
				log.NombreProceso = "cl.bcs.custodia.IngresoRetiroTitulosJava.java";
						
				listaLog.add(log);
			}
			
			//REGISTRO
			AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
			entradaAgregarLog.p_IdTrabajo = 1L;		
			entradaAgregarLog.p_Log = listaLog;	
			AgregarLog agregarLogService = new AgregarLog();
			agregarLogService.logica(con, entradaAgregarLog);
			
			Long fin = System.currentTimeMillis();
			logger.debug("FIN::IngresoRetiroTitulosJava.agregarLog:" , fin - inicio);
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	//Procesa validaciones de negocio
	private void ProcesaRequisito(Connection con, Long idEmpresa, boolean esError, String codMsg, String CodRequisito, ObjInstrumento instrumento, HashMap<String, Object> requisitos, List<MensajeSalida> P_OUT_MENSAJES)
			throws Exception {
		if (esError) {
			String msg=Diccionario.getMensaje(con, idEmpresa, codMsg)
					.add("IdGenerarMovimientoCustodia", instrumento.IdGenerarMovimientoCustodia)
					.msg();
				 
			if (CodRequisito.equals(MensajeSalida.MENSAJE_ERROR)) {
				P_OUT_MENSAJES.add(new MensajeSalida(instrumento.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_ERROR,codMsg,msg));
				instrumento.errores++;
			} else if (CodRequisito.equals(MensajeSalida.MENSAJE_AUTORIZACION)) {
				P_OUT_MENSAJES.add(new MensajeSalida(instrumento.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_AUTORIZACION,codMsg,msg));
				instrumento.autorizaciones++;						
			} else if (CodRequisito.equals(MensajeSalida.MENSAJE_ADVERTENCIA)) {
				P_OUT_MENSAJES.add(new MensajeSalida(instrumento.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_ADVERTENCIA,codMsg,msg));
				instrumento.advertencias++;
			} else if (CodRequisito.equals(MensajeSalida.MENSAJE_INFORMACION)) {
				P_OUT_MENSAJES.add(new MensajeSalida(instrumento.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_INFORMACION,codMsg,msg));
				instrumento.advertencias++;
			} else {
				if(!requisitos.isEmpty()) {
					//si el comportamiento viene de requisitos operativos
					String tipoOperacion = ("S").equals(instrumento.esTraspasoCliente) ? "TRASPASOTITULOSRV" : "INGRESOTITULOSRV";
					String key = instrumento.IdTipoCanalIngreso + tipoOperacion;
					RequisitosOperativosIngresoTitulos requisito = (RequisitosOperativosIngresoTitulos) requisitos.get(key);
					
					if (requisito != null) {
						if (requisito.IdTipoComportamiento.equals(MensajeSalida.MENSAJE_ERROR) || requisito.IdTipoComportamiento.equals(MensajeSalida.MENSAJE_RECHAZO)) { //TODO ELIMINAR MENSAJE RECHAZO
							P_OUT_MENSAJES.add(new MensajeSalida(instrumento.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_ERROR,codMsg,msg));
							instrumento.errores++;
						} else if (requisito.IdTipoComportamiento.equals(MensajeSalida.MENSAJE_AUTORIZACION)) {
							P_OUT_MENSAJES.add(new MensajeSalida(instrumento.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_AUTORIZACION,codMsg,msg));
							instrumento.autorizaciones++;
						} else if (requisito.IdTipoComportamiento.equals(MensajeSalida.MENSAJE_ADVERTENCIA)) {
							P_OUT_MENSAJES.add(new MensajeSalida(instrumento.IdGenerarMovimientoCustodia,MensajeSalida.MENSAJE_ADVERTENCIA,codMsg,msg));
							instrumento.advertencias++;
						} else {
							instrumento.ok++;
						}
					}
				}
			}
		}else {
			instrumento.ok++;
		}		
	}
		
	private void limpiarInstrumentos(List<ObjInstrumento> instrumentos, List<ObjInstrumento> instrumentosConErrores) {
		for (ObjInstrumento ins : instrumentos) {
			if (ins.errores > 0) {
				instrumentosConErrores.add(ins);
			}
		}
			
		for (ObjInstrumento ins : instrumentosConErrores) {
			if (instrumentos.contains(ins))
				instrumentos.remove(ins);
		}
	}
	
	private String ConsultarEstadoModulo(Long idEmpresa, Long idUsuario, Connection conn){
		String estado = "";
		
		InConsultarEstadoModulo entrada = new InConsultarEstadoModulo();
		entrada.p_IdEmpresa = idEmpresa;
		entrada.p_IdUsuario = idUsuario;
		entrada.p_IdModulo = "CUS";
		ConsultarEstadoModulo objConsulta 	= new ConsultarEstadoModulo();
		
		try{
			OutConsultarEstadoModulo salida = objConsulta.logica(conn, entrada);
			for (Modulo modulo : salida.p_out_ListaModulos) {
				if (entrada.p_IdModulo.equals(modulo.NombreModulo)) {
					estado = modulo.IdEstadoModulo;
					break;
				}
			}
		}catch (Exception e){
			estado = "";
			e.printStackTrace();
		}
		
		return estado;
	}
	
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();		
		IngresoRetiroTitulosEntrada jin=gson.fromJson(in.toString() ,IngresoRetiroTitulosEntrada.class);
		IngresoRetiroTitulosSalida out=logica(conn,jin);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
        return dataout;
	}
		
	
	//-------------------------------------------------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	//-------------------------------------------------------------------------------------------
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getPropertiesConnection("qa2", "connections.properties")) {
			try {
				Gson gson = Utils.getGsonBuilder();
				
				String jsonIngreso = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"CUS\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_Certifica\":\"N\",\"P_INSTRUMENTOS\":[{\"IdGenerarMovimientoCustodia\":1,\"FechaOperacion\":\"2025-01-31\",\"FechaOperacionOriginal\":\"0001-01-01\",\"FechaVencimiento\":\"0001-01-01\",\"FechaLiquidacion\":\"2025-01-31\",\"IdConcepto\":\"IIRV04\",\"IdBolsa\":\"BCS\",\"IdTipoOperacion\":\"INGTIT\",\"IdTipoProducto\":\"CUST\",\"IdCliente\":12659,\"IdPortafolio\":13974,\"IdBoveda\":\"DCV\",\"CuentaBoveda\":\"\",\"IdCuentaCustodioPortafolio\":0,\"IdClienteDestino\":0,\"IdPortafolioDestino\":0,\"IdBovedaRelacionada\":\"\",\"CuentaBovedaRelacionada\":\"\",\"IdCuentaCustodioPortafolioRelacionado\":0,\"esTraspasoBoveda\":\"N\",\"esTraspasoPortafolio\":\"N\",\"esTraspasoCliente\":\"N\",\"DebeCertificar\":\"S\",\"EsGarantia\":\"N\",\"PlazoEnDias\":0,\"IdCartera\":0,\"FolioCartera\":0,\"IdCondicionLiquidacion\":\"T0\",\"FolioExternoReferencia\":\"\",\"Glosa\":\"\",\"IndicadorNominal\":\"N\",\"IdAgente\":97,\"IdSucursal\":0,\"IdMonedaSistema\":\"CLP\",\"IdTipoCanalIngreso\":\"AGE\",\"IdClasificacionTipoInstrumento\":\"RV\",\"IdTipoInstrumento\":\"ACN\",\"IdInstrumento\":502,\"Nemotecnico\":\"COPEC\",\"IdEmisor\":\"COPEC\",\"IdMonedaInstrumento\":\"CLP\",\"IdMonedaEmision\":\"\",\"IdTipoReajuste\":\"CLP\",\"IdMonedaAdicional\":\"CLP\",\"FechaEmisionInstrumento\":\"0001-01-01\",\"FechaVencimientoInstrumento\":\"0001-01-01\",\"Fungible\":\"N\",\"NemoSVS\":\"\",\"Precio\":6620,\"Cantidad\":3000,\"ValorRescate\":0,\"CorteMinimo\":1,\"Monto\":19860000,\"MontoEnMonedaInstrumento\":19860000,\"MontoEnMonedaAdicional\":0,\"Tasa\":0,\"TasaAgente\":0,\"CodigoIFRS\":\"\",\"EsCtaPropia\":\"N\",\"IdCorredora\":0,\"EsInstrumentoCalificado\":\"S\"}]}";
				String jsonTraspaso = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"CUS\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_Certifica\":\"N\",\"P_INSTRUMENTOS\":[{\"IdGenerarMovimientoCustodia\":1,\"FechaOperacion\":\"2025-01-13\",\"FechaOperacionOriginal\":\"0001-01-01\",\"FechaVencimiento\":\"0001-01-01\",\"FechaLiquidacion\":\"2025-01-13\",\"IdConcepto\":\"RETTRC\",\"IdBolsa\":\"BCS\",\"IdTipoOperacion\":\"RETTIT\",\"IdTipoProducto\":\"CUST\",\"IdCliente\":12566,\"IdPortafolio\":13839,\"IdBoveda\":\"DCV\",\"CuentaBoveda\":\"\",\"IdCuentaCustodioPortafolio\":0,\"IdClienteDestino\":2154,\"IdPortafolioDestino\":5172,\"IdBovedaRelacionada\":\"\",\"CuentaBovedaRelacionada\":\"\",\"IdCuentaCustodioPortafolioRelacionado\":0,\"esTraspasoBoveda\":\"N\",\"esTraspasoPortafolio\":\"N\",\"esTraspasoCliente\":\"S\",\"DebeCertificar\":\"N\",\"EsGarantia\":\"N\",\"PlazoEnDias\":0,\"IdCartera\":0,\"FolioCartera\":0,\"IdCondicionLiquidacion\":\"T0\",\"FolioExternoReferencia\":\"\",\"Glosa\":\"\",\"IndicadorNominal\":\"N\",\"IdAgente\":97,\"IdSucursal\":0,\"IdMonedaSistema\":\"CLP\",\"IdTipoCanalIngreso\":\"AGE\",\"IdClasificacionTipoInstrumento\":\"RV\",\"IdTipoInstrumento\":\"ACN\",\"IdInstrumento\":502,\"Nemotecnico\":\"COPEC\",\"IdEmisor\":\"COPEC\",\"IdMonedaInstrumento\":\"CLP\",\"IdMonedaEmision\":\"\",\"IdTipoReajuste\":\"CLP\",\"IdMonedaAdicional\":\"CLP\",\"FechaEmisionInstrumento\":\"0001-01-01\",\"FechaVencimientoInstrumento\":\"0001-01-01\",\"Fungible\":\"N\",\"NemoSVS\":\"\",\"Precio\":5940,\"Cantidad\":100,\"ValorRescate\":0,\"CorteMinimo\":1,\"Monto\":594000,\"MontoEnMonedaInstrumento\":594000,\"MontoEnMonedaAdicional\":0,\"Tasa\":0,\"TasaAgente\":0,\"CodigoIFRS\":\"\",\"EsCtaPropia\":\"N\",\"IdCorredora\":0,\"EsInstrumentoCalificado\":\"S\"}]}";
				String jsonRetiro = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"CUS\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_Certifica\":\"N\",\"P_INSTRUMENTOS\":[{\"IdGenerarMovimientoCustodia\":1,\"FechaOperacion\":\"2025-01-21\",\"FechaOperacionOriginal\":\"0001-01-01\",\"FechaVencimiento\":\"0001-01-01\",\"FechaLiquidacion\":\"2025-01-21\",\"IdConcepto\":\"RETTRC\",\"IdBolsa\":\"BCS\",\"IdTipoOperacion\":\"RETTIT\",\"IdTipoProducto\":\"CUST\",\"IdCliente\":11470,\"IdPortafolio\":12411,\"IdBoveda\":\"DCV\",\"CuentaBoveda\":\"\",\"IdCuentaCustodioPortafolio\":0,\"IdClienteDestino\":12659,\"IdPortafolioDestino\":13974,\"IdBovedaRelacionada\":\"\",\"CuentaBovedaRelacionada\":\"\",\"IdCuentaCustodioPortafolioRelacionado\":0,\"esTraspasoBoveda\":\"N\",\"esTraspasoPortafolio\":\"N\",\"esTraspasoCliente\":\"S\",\"DebeCertificar\":\"N\",\"EsGarantia\":\"N\",\"PlazoEnDias\":0,\"IdCartera\":0,\"FolioCartera\":0,\"IdCondicionLiquidacion\":\"T0\",\"FolioExternoReferencia\":\"\",\"Glosa\":\"\",\"IndicadorNominal\":\"N\",\"IdAgente\":97,\"IdSucursal\":0,\"IdMonedaSistema\":\"CLP\",\"IdTipoCanalIngreso\":\"AGE\",\"IdClasificacionTipoInstrumento\":\"RV\",\"IdTipoInstrumento\":\"ACN\",\"IdInstrumento\":1368,\"Nemotecnico\":\"LAN\",\"IdEmisor\":\"CRUZBLANCA\",\"IdMonedaInstrumento\":\"CLP\",\"IdMonedaEmision\":\"\",\"IdTipoReajuste\":\"CLP\",\"IdMonedaAdicional\":\"CLP\",\"FechaEmisionInstrumento\":\"0001-01-01\",\"FechaVencimientoInstrumento\":\"0001-01-01\",\"Fungible\":\"N\",\"NemoSVS\":\"\",\"Precio\":6839.1,\"Cantidad\":100,\"ValorRescate\":0,\"CorteMinimo\":1,\"Monto\":683910,\"MontoEnMonedaInstrumento\":683910,\"MontoEnMonedaAdicional\":0,\"Tasa\":0,\"TasaAgente\":0,\"CodigoIFRS\":\"\",\"EsCtaPropia\":\"N\",\"IdCorredora\":0,\"EsInstrumentoCalificado\":\"S\"}]}";
				
				IngresoRetiroTitulosEntrada Objin = gson.fromJson(jsonIngreso, IngresoRetiroTitulosEntrada.class);
								
				IngresoRetiroTitulosJava  kk = new IngresoRetiroTitulosJava();
				System.out.println(kk.logica(con, Objin)); 
				
				con.rollback();
		    	}catch(Exception er) {
					er.printStackTrace();	
					con.rollback();
				}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
		
}
