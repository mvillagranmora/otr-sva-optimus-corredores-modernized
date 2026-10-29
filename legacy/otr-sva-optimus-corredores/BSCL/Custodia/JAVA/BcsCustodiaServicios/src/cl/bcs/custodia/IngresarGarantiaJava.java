package cl.bcs.custodia;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.cuentaInversion.CrearCargoAbono;
import cl.bcs.custodia.utils.GestionMovimientoCarteraEnTransito;
import cl.bcs.custodia.utils.InformacionConceptosCustodia;
import cl.bcs.custodia.utils.IngresoCustodia;
import cl.bcs.custodia.utils.LogCustodia;
import cl.bcs.custodia.utils.RetiroCustodia;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.tesoreria.CrearIngresoEgreso;
import cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Entrada;
import cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Salida;
import cl.bcs.tesoreria.CrearIngresoEgreso.IngresoEgreso_Entrada;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;


public class IngresarGarantiaJava implements JavaCall{	
	
	private final Log log = new Log("IngresarGarantia");
	private final String SQL_Contrapartes = loadResourceToString("cl/bcs/custodia/sql/IngresarGarantia_SEL_Contrapartes.sql");
	
	private final String SQL_SELECT_Persona_Info_Cliente = loadResourceToString("cl/bcs/custodia/sql/IngresarGarantia_SelectDatosCliente.sql");
	
	private final String SQL_SELECT_IngresarGarantia_SelectDatosNot = loadResourceToString("cl/bcs/custodia/sql/IngresarGarantia_SelectDatosNotificacion.sql");
	
	private final String SQL_LotePadron = loadResourceToString("cl/bcs/custodia/sql/IngresarGarantia_SEL_LotePadron.sql");

	public static class IngresarGarantia_Notificacion {
		
		public String  		Instrumento;
		public String	 	NombreCompleto;
		public String 		TipoMoneda;
		public String 		DI;
		public String		TipoDoc;
		public String 		Operacion;
		public String 		NombreOperacion;
		public String		NumeroTel;
		public String 		Correo;
		public String 		Corredora;
		public Long			IdPersona;
		public Long 		IdDireccionElectronicaPrincipal;
		public Long 		IdDireccionElectronicaPrincipalCLI;
		public Long 		IdMovimientoCarteraEnTransito;
		public Long 		IdPersonaCLI;
		public BigDecimal 	Cantidad;
		public BigDecimal 	PrecioCompra;
		public BigDecimal 	MontoCompra;

		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	
	public static class IngresarGarantiaEntrada {
	    public Long								p_IdEmpresa;
	    public Long								p_IdUsuario;
	    public String							p_IdModulo;
	    public Long 							p_IdEmpleado;
	    public String							p_UsaReglasNegocio;
	    public String							p_SimulaOperacion;
	    public String							p_Certifica = "N";  //S: si proviene desde Certificacion de movimiento en transito
	    public String							p_FechaMovimiento;
	    public List<ObjGarantiaCustodia>		p_GarantiaCustodia;
	    public List<ObjGarantiaCuentaInversion>	p_GarantiaCuentaInversion;
	    public List<ObjGarantiaEspecie>			p_GarantiaEspecies;
	    public List<Long>						p_MovimientosEnTransito;
	    public String 							p_EsProcesoContrapartida = "N";
	    
	    public IngresarGarantiaEntrada (){
	    	p_GarantiaCustodia 			= new ArrayList<ObjGarantiaCustodia>();
	    	p_GarantiaCuentaInversion	= new ArrayList<ObjGarantiaCuentaInversion>();
	    	p_GarantiaEspecies			= new ArrayList<ObjGarantiaEspecie>();
	    	p_MovimientosEnTransito		= new ArrayList<Long>();
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	public static class IngresarGarantiaSalida {				
		public Long					P_OUT_ERRORESCONTADOR;
		public Long					P_OUT_TICKETATENCION;
		public List<ObjResultado>	P_OUT_RESULTADOPROCESO;
		public List<MensajeSalida>	P_OUT_MENSAJES;
				
		public static class ObjResultado {
			public Long		IdGenerarMovimientoCustodia;
			public String	TipoAccion;
			public String	TablaOrigen;
			public Long		IdCustodia;
			public Long		FolioCustodia;			
			public Long		IdOperacion;			//Si viene del negocio
			public Long		FolioComprobante;		//Si viene de facturaciÃ³n
			public Long		IdInstrumento;
		};
		public IngresarGarantiaSalida (){
			P_OUT_MENSAJES 			= new ArrayList<MensajeSalida>();
			P_OUT_RESULTADOPROCESO	= new ArrayList<ObjResultado>();
		};
		public String toString() {
			return Utils.printJson(this, false);
		};
	};


	public static class ObjGarantiaCustodia {
		public Long					IdInstrumento; 
		public String				IdMoneda; 
		public String				IdMonedaEmisionInstrumento;
		public BigDecimal			Cantidad;
		public BigDecimal			ValorRescate;
		public BigDecimal			MontoMonedaInstrumento;
		public BigDecimal			MontoMonedaSistema; 
		public String				IdClasificacionTipoInstrumento; 
		public String				IdTipoReajuste; 
		public String				NemoSVS; 
		public String				FechaEmisionInstrumento; 
		public String				FechaVencimientoInstrumento; 
		public String				IdEmisor; 
		public Long					PlazoEnDias; 
		public String				EsFungible; 
		public Long					IdCliente; 
		public Long					IdPortafolio;
		public Long 				IdAgente;
		public String				IdTipoInstrumento; 
		public String				IdBolsa; 
		public Long					IdInstrumentoGarantizado; 
		public String				IdConcepto; 
		public Long					IdContraparteGarantia;
		public BigDecimal			Precio; 
		public BigDecimal			Tasa;
		public Long					FolioCarteraGarantizada;
		public BigDecimal			CorteMinimo;
		//Para la entrada del retiro masico de custodia
		public Long 				FolioCarteraRetiroMasivo;
		//Si viene del negocio
		public String				IdTipoOperacion; 
		public Long					IdOperacion; 		
		//Si viene de facturaciÃ³n
		public String				IdTipoComprobante; 
		public Long					FolioComprobante; 
		public java.sql.Date		FechaComprobante; 
		public Long 				IdGarantiaVigente = 0L;
		public BigDecimal			CantidadVigente = BigDecimal.ZERO;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	public static class ObjGarantiaCuentaInversion {
		public String				IdMoneda; 
		public BigDecimal			MontoMonedaInstrumento;
		public BigDecimal			MontoMonedaSistema; 
		public Long					IdCliente; 
		public Long					IdPortafolio;
		public Long 				IdAgente;
		public String				IdTipoInstrumento; 
		public String				IdBolsa; 
		public Long					IdInstrumentoGarantizado; 
		public String				IdConcepto; 
		public Long					IdContraparteGarantia; 
		
		//Si viene del negocio
		public String				IdTipoOperacion; 
		public Long					IdOperacion;		
		//Si viene de facturaciÃ³n
		public String				IdTipoComprobante; 
		public Long					FolioComprobante; 
		public java.sql.Date		FechaComprobante;
		public Long 				IdGarantiaVigente = 0L;
		public BigDecimal			CantidadVigente = BigDecimal.ZERO;
		public BigDecimal			INGMontoATasaGarantiaActualVigente = BigDecimal.ZERO;
		public BigDecimal			INGMontoGarantiaActualMonedaInstrumentoVigente = BigDecimal.ZERO;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class ObjLotePadron {
		public Long IdInstrumento;
		public Long LotePadron;
		public String Nemotecnico;

		public String toString() {
			return Utils.printJson(this, false);
		}		
	};
	
	public static class ObjGarantiaEspecie {
		public Long					IdEspecie; 
		public String				IdMoneda; 
		public BigDecimal			MontoMonedaSistema; 
		public String				Glosa; 
		public Long					IdCliente; 
		public Long					IdPortafolio;
		public Long 				IdAgente;
		public String				IdTipoInstrumento; 
		public String				IdBolsa; 
		public Long					IdInstrumentoGarantizado; 
		public String				IdConcepto; 
		public Long					IdContraparteGarantia; 
		public String				IdTipoOperacion; 
		public Long					IdOperacion;
		public Long 				IdGarantiaVigente = 0L;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	
	public static class ObjContraparte {
		public Long			IdContraparte; 
		public String		RazonSocial;
	
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	public static class ObjDatosCliente {
		public String NombreCompleto;
		public String DocumentoIdentidad;
		public String DocumentoIdentidadAdicional;
		public Long IdCliente;
		public Long IdEmpresa;
		public Long IdPersona;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	public static class ObjGenerarMovimientoCustodia extends BcsPojo{
		public Long IdGenerarMovimientoCustodia;
		public Long IdGarantiaVigente;
		public BigDecimal CantidadVigente;
		public BigDecimal INGMontoATasaGarantiaActualVigente;
		public BigDecimal INGMontoGarantiaActualMonedaInstrumentoVigente;
		
		public ObjGenerarMovimientoCustodia () {
			this.IdGenerarMovimientoCustodia = 0L;
			this.IdGarantiaVigente = 0L;
			this.CantidadVigente = BigDecimal.ZERO;
			this.INGMontoATasaGarantiaActualVigente = BigDecimal.ZERO;
			this.INGMontoGarantiaActualMonedaInstrumentoVigente = BigDecimal.ZERO;
		}

		public ObjGenerarMovimientoCustodia (Long idGenerarMovimientoCustodia, Long idGarantiaVigente, BigDecimal cantidad, BigDecimal ingMontoATasa, BigDecimal ingMontoMonedaInstrumento) {
			this.IdGenerarMovimientoCustodia = idGenerarMovimientoCustodia;
			this.IdGarantiaVigente = idGarantiaVigente;
			this.CantidadVigente = cantidad;
			this.INGMontoATasaGarantiaActualVigente = ingMontoATasa;
			this.INGMontoGarantiaActualMonedaInstrumentoVigente = ingMontoMonedaInstrumento;
		}
		
		public ObjGenerarMovimientoCustodia (Long idGarantiaVigente, BigDecimal cantidad, BigDecimal ingMontoATasa, BigDecimal ingMontoMonedaInstrumento) {
			this.IdGenerarMovimientoCustodia = 0L;
			this.IdGarantiaVigente = idGarantiaVigente;
			this.CantidadVigente = cantidad.compareTo(BigDecimal.ZERO) > 0L ? cantidad : BigDecimal.ZERO;
			this.INGMontoATasaGarantiaActualVigente = ingMontoATasa.compareTo(BigDecimal.ZERO) > 0L ? ingMontoATasa : BigDecimal.ZERO;
			this.INGMontoGarantiaActualMonedaInstrumentoVigente = ingMontoMonedaInstrumento.compareTo(BigDecimal.ZERO) > 0L ? ingMontoMonedaInstrumento : BigDecimal.ZERO;
		}
	}
	private static final Logger logger = LogManager.getLogger(LiquidacionOpciones.class);
	
	@Trace
	public IngresarGarantiaSalida logica(Connection con, IngresarGarantiaEntrada in) throws Exception {
		IngresarGarantiaSalida out = new IngresarGarantiaSalida();
		out.P_OUT_ERRORESCONTADOR = 0L;
		SQL sql = new SQL();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		HashMap<String, String> mapMensageSalida 	= new HashMap<>();
		
		//******************************************************************************************************************
		//												VALIDACIONES INICIALES
		//******************************************************************************************************************
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
		};
	
		if (out.P_OUT_MENSAJES.size() > 0){
			return out;
		};
		
		//******************************************************************************************************************
		//													LOGICA
		//******************************************************************************************************************
		List<InformacionConceptosCustodia.ObjDatosEntrada> IdConceptos = new ArrayList<InformacionConceptosCustodia.ObjDatosEntrada>(); 
		
		Long TicketAtencion = LogCustodia.logIngresoGarantia(con, in);
		if (TicketAtencion.equals(0L)){
			//TODO : incorporar mensajes de error
			out.P_OUT_ERRORESCONTADOR = 1L;				
			return out; 
		};
		
		//01 - CREAR U OBTENER MOVIMIENTO EN TRANSITO
		List<Long> IdMovimientosEnTransito = new ArrayList<Long>();
		HashMap<Long,Object> hGenerarMovimientoCustodia = new HashMap<Long,Object>();
		
		if (in.p_Certifica.equals("N")) {
			Set<String> IdContraparte = new HashSet<>();
			Set<String> IdCliente = new HashSet<>();
			in.p_GarantiaCustodia.stream().forEach( garCus -> IdContraparte.add(garCus.IdContraparteGarantia.toString()) );
			
			for (ObjGarantiaCuentaInversion garCTA: in.p_GarantiaCuentaInversion  ) {
				IdContraparte.add(garCTA.IdContraparteGarantia.toString());
				IdCliente.add(garCTA.IdCliente.toString());
			};
			for (ObjGarantiaEspecie garESP: in.p_GarantiaEspecies  ) {
				IdContraparte.add(garESP.IdContraparteGarantia.toString());
			};
			HashMap<String,Object> paramsContrapartes = new HashMap<>();
			paramsContrapartes.put("IdContraparte", IdContraparte.stream().collect(Collectors.toList()));
			List<ObjContraparte> Contrapartes = sql.query(con, SQL_Contrapartes, paramsContrapartes, ObjContraparte.class);			
			HashMap<String,Object> InformacionContraparte = Utils.List2Hash(Contrapartes, "IdContraparte");
			
			logger.debug("INICIO OBTENER DATOS CLIENTES CUENTA INVERSION");
			
			HashMap<String,Object> paramsCliente = new HashMap<>();
			//in.p_IdEmpresa, aux.IdCliente
			paramsCliente.put("IdCliente", IdCliente.stream().collect(Collectors.toList()));
			paramsCliente.put("IdEmpresa", in.p_IdEmpresa);
			List<ObjDatosCliente> clientes = sql.query(con, SQL_SELECT_Persona_Info_Cliente, paramsCliente, ObjDatosCliente.class);	
			logger.debug("FIN OBTENER DATOS CLIENTES CUENTA INVERSION", clientes);
			
			// GARANTIAS EN CUSTODIA
			// DERIVADOS ACCIONES SOBRE OPCIONES
			
			List<Long> idInstrumentoLote = new ArrayList<Long>();
			for(ObjGarantiaCustodia dato : in.p_GarantiaCustodia){
				if (dato.IdTipoInstrumento.equals("IOA")) {
					idInstrumentoLote.add(dato.IdInstrumentoGarantizado);
				}
			}
			
			HashMap<String, Object> paramLote = new HashMap<>();
			paramLote.clear();
			paramLote.put("IdInstrumento", idInstrumentoLote);

			List<ObjLotePadron> lotePadron = new ArrayList<ObjLotePadron>();
			lotePadron = sql.query(con,SQL_LotePadron,paramLote,ObjLotePadron.class);
			logger.debug("LOTE PADRON",lotePadron);
			
			HashMap<String,String> valores = new HashMap<String,String>();
			

			
			if(!lotePadron.isEmpty()){
				for (ObjGarantiaCustodia datosEntrada: in.p_GarantiaCustodia) {
				    for(ObjLotePadron listaLote :  lotePadron) {					
				    	if (listaLote.IdInstrumento.equals(datosEntrada.IdInstrumentoGarantizado)) {
				    		if (datosEntrada.Cantidad.longValue()%listaLote.LotePadron.longValue()!=0) {
				    			out.P_OUT_ERRORESCONTADOR = 1L;
				    			valores.put("Nemotecnico", listaLote.Nemotecnico);
				    			valores.put("LotePadron", listaLote.LotePadron.toString());
				    			valores.put("Cantidad",datosEntrada.Cantidad.toString());				    			
				    			out.P_OUT_MENSAJES.add(new MensajeSalida(listaLote.IdInstrumento ,MensajeSalida.MENSAJE_ERROR, "SRV_DER_ERROR_MULTIPLO_LOTE_PADRON", valores ,con, in.p_IdEmpresa));				    			
				    		}				    		
				    	}
				    }
				}
				if (out.P_OUT_ERRORESCONTADOR > 0) {
					return out;	
				}
				
			}
			// DERIVADOS ACCIONES SOBRE OPCIONES		
			Long IdGenerarMovimientoCustodia  = 0L;
			List<IngresoCustodia.ObjDatosEntrada> p_Datos = new ArrayList<IngresoCustodia.ObjDatosEntrada>();			
			for (ObjGarantiaCustodia garCustodia: in.p_GarantiaCustodia) {
				ObjContraparte InfoContraparte = (ObjContraparte) InformacionContraparte.get(garCustodia.IdContraparteGarantia);					
				if (InfoContraparte == null) continue; //TODO indicar mensaje de error
				
				IngresoCustodia.ObjDatosEntrada aux = new IngresoCustodia.ObjDatosEntrada();
				aux.IdGenerarMovimientoCustodia			= IdGenerarMovimientoCustodia++;				
				aux.IdInstrumentoCarteraGarantizada		= garCustodia.IdInstrumentoGarantizado;
				aux.IdContraparteGarantia				= garCustodia.IdContraparteGarantia;
				aux.RazonSocialContraparteGarantia		= InfoContraparte.RazonSocial;
				aux.IdOrigenGarantia					= "CUS";
				aux.IdCliente							= garCustodia.IdCliente;
				aux.IdPortafolio						= garCustodia.IdPortafolio;
				aux.IdAgente 							= garCustodia.IdAgente;
				aux.IdConcepto							= garCustodia.IdConcepto;
				aux.IdTipoOperacionGestora				= garCustodia.IdTipoOperacion;
				aux.IdTipoOperacion						= garCustodia.IdTipoOperacion;
				aux.IdOperacion							= garCustodia.IdOperacion;
				aux.FechaOperacion						= Fecha.toDate(in.p_FechaMovimiento);
				aux.IdTipoComprobante					= garCustodia.IdTipoComprobante;
				aux.FolioComprobante					= garCustodia.FolioComprobante;
				aux.FechaComprobante					= garCustodia.FechaComprobante;
				aux.IdBolsa								= garCustodia.IdBolsa;
				aux.IdCondicionLiquidacion				= "T0";
				aux.FechaLiquidacion					= Fecha.toDate(in.p_FechaMovimiento);
				aux.IdInstrumento						= garCustodia.IdInstrumento;
				aux.IdEmisor							= garCustodia.IdEmisor;
				aux.IdTipoReajusteInstrumento			= garCustodia.IdTipoReajuste;
				aux.PlazoEnDiasOperacion 				= garCustodia.PlazoEnDias;
//					if (garCustodia.IdClasificacionTipoInstrumento.equalsIgnoreCase("RF") || garCustodia.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF")){
					aux.FechaVencimientoInstrumento 		= Fecha.toDate(garCustodia.FechaVencimientoInstrumento);
					aux.PlazoEnDiasOperacion 				= (garCustodia.PlazoEnDias==null ? 0 : garCustodia.PlazoEnDias);
//					}
				aux.IdMonedaInstrumento					= garCustodia.IdMoneda;				
				aux.IdMonedaEmision						= garCustodia.IdMonedaEmisionInstrumento;
				aux.EsFungible							= garCustodia.EsFungible == null ? "N" : garCustodia.EsFungible;
				aux.NemoSVS 							= (garCustodia.NemoSVS == null || !garCustodia.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF") ? "" : garCustodia.NemoSVS); 
				
				if (garCustodia.IdClasificacionTipoInstrumento!=null && garCustodia.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF")){
					if(garCustodia.EsFungible.equals("N")) {
						aux.Cantidad						= garCustodia.Cantidad;
					}else {
						aux.Cantidad						= BigDecimal.ONE;
					}
					
					if(garCustodia.ValorRescate.remainder(garCustodia.CorteMinimo).compareTo(BigDecimal.ZERO)==0) {
						aux.ValorRescate					= garCustodia.ValorRescate;
					}else {
						out.P_OUT_ERRORESCONTADOR = out.P_OUT_ERRORESCONTADOR + 1L;
						HashMap<String, String> mapMsg = new HashMap<>();
						
						mapMsg.put("Nemotecnico", garCustodia.NemoSVS);
						mapMsg.put("IdEmisor", garCustodia.IdEmisor);
						mapMsg.put("tipoValor", garCustodia.IdTipoReajuste);
						mapMsg.put("Valor", garCustodia.ValorRescate.toString());
						mapMsg.put("CorteMinimo", garCustodia.CorteMinimo.toString());

						out.P_OUT_MENSAJES.add(new MensajeSalida(1L, "ERROR",
								"UI_CARGA_INSTRUCCIONES_NOES_MULTIPLO_CORTE", mapMsg, con, in.p_IdEmpresa));
						
						return out;
					}

				} else {
					aux.Cantidad						= garCustodia.Cantidad;
					aux.ValorRescate					= BigDecimal.ZERO;
				};
				
				aux.PrecioCompra						= garCustodia.Precio;
				aux.TasaCompra							= garCustodia.Tasa;
				aux.MontoCompra							= garCustodia.MontoMonedaSistema;
				aux.EsEspecifica						= (garCustodia.IdOperacion != null ? (garCustodia.IdOperacion.compareTo(0L) != 0 ? "S" : "N") : "N");
				aux.MontoCompraMonedaInstrumento		= garCustodia.MontoMonedaInstrumento;
				aux.FolioCarteraGarantizada				= garCustodia.FolioCarteraGarantizada;
				aux.CorteMinimo							= garCustodia.CorteMinimo;
				aux.TablaAfectada						= "TRANSITO";
				aux.EsRegistroNuevo						= "S";
				aux.AccionOperacion						= "INGRESO";
				aux.IdUsuario							= in.p_IdUsuario;
				p_Datos.add(aux);
				hGenerarMovimientoCustodia.put(aux.IdGenerarMovimientoCustodia, new ObjGenerarMovimientoCustodia(garCustodia.IdGarantiaVigente, garCustodia.CantidadVigente, BigDecimal.ZERO, BigDecimal.ZERO));
				
				InformacionConceptosCustodia.ObjDatosEntrada obj = new InformacionConceptosCustodia.ObjDatosEntrada();
				Utils.copyPojo(aux, obj);
				obj.IdInstrumento = garCustodia.IdInstrumentoGarantizado;
				obj.IdNombreEntidad = "CUS.GARANTIA";
				IdConceptos.add(obj);
			};
			
			
			//AGREGAR LOG
			{
				AgregarLog agregarLogService = new AgregarLog();
				for (ObjGarantiaCustodia gar: in.p_GarantiaCustodia) {
					AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
					AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
					entradaAgregarLog.p_IdTrabajo = 1L;
					log.IdTrabajo = entradaAgregarLog.p_IdTrabajo;
					log.IdCabeceraLog = gar.FolioCarteraGarantizada;
					log.IdTipoLog = "INGRESO";
					log.IdModulo = "CUS";
					log.IdUsuario = in.p_IdUsuario;
					log.IdEmpresa = in.p_IdEmpresa;
					log.FechaSistemaLog = fechaSistema;
					log.IdUsuarioAtendido = in.p_IdUsuario;
					log.IdCliente = gar.IdCliente;
					log.EstadoCliente = "";
					log.NombreCliente = "";
					log.IdTipoDocumentoIdentidad = "";
					log.IdDocumentoIdentidad = "";
					log.IdPortafolio = gar.IdPortafolio;
					log.EstadoPortafolio = "";
					log.IdTipoRegistro = "CUSTODIA";
					log.EstadoRegistro = "";
					log.IdTipoOperacion = gar.IdTipoOperacion;
					log.Folio = gar.FolioComprobante;
					log.Cantidad = gar.Cantidad;
					log.PrecioTasa = gar.Precio;
					log.Monto = gar.MontoMonedaInstrumento;
					log.IdMonedaOrigen = gar.IdMoneda;
					log.IdMonedaLiquidacion = "";
					log.TipoCambio = "";
					log.Nemo = "";
					log.IdProceso = 0L;
					log.EstadoProceso = "";
					log.NombreProceso = "";						
					entradaAgregarLog.p_Log.add(log);
					agregarLogService.logica(con, entradaAgregarLog);
				}
			}
			
			
			
			
			//GARANTIAS EN ESPECIE
			for (ObjGarantiaEspecie garEspecie: in.p_GarantiaEspecies) {
				ObjContraparte InfoContraparte = (ObjContraparte) InformacionContraparte.get(garEspecie.IdContraparteGarantia);
				if (InfoContraparte == null) continue; //TODO indicar mensaje de error
				
				IngresoCustodia.ObjDatosEntrada aux = new IngresoCustodia.ObjDatosEntrada();
				aux.IdGenerarMovimientoCustodia			= IdGenerarMovimientoCustodia++;
				aux.IdInstrumentoCarteraGarantizada		= garEspecie.IdInstrumentoGarantizado;
				aux.IdContraparteGarantia				= garEspecie.IdContraparteGarantia;
				aux.RazonSocialContraparteGarantia		= InfoContraparte.RazonSocial;
				aux.IdOrigenGarantia					= "ESP";
				aux.IdEspecieGarantia					= garEspecie.IdEspecie;
				aux.IdCliente							= garEspecie.IdCliente;
				aux.IdPortafolio						= garEspecie.IdPortafolio;
				aux.IdAgente 							= garEspecie.IdAgente;
				aux.IdConcepto							= garEspecie.IdConcepto;
				aux.IdTipoOperacionGestora				= garEspecie.IdTipoOperacion;
				aux.IdTipoOperacion						= garEspecie.IdTipoOperacion;
				aux.FechaOperacion						= Fecha.toDate(in.p_FechaMovimiento);
				aux.Glosa								= garEspecie.Glosa;
				aux.IdBolsa								= garEspecie.IdBolsa;
				aux.IdCondicionLiquidacion				= "T0";
				aux.FechaLiquidacion					= Fecha.toDate(in.p_FechaMovimiento);		
				aux.IdMonedaInstrumento					= garEspecie.IdMoneda;
				aux.Cantidad							= BigDecimal.ONE;
				aux.MontoCompra							= garEspecie.MontoMonedaSistema;
				aux.MontoCompraMonedaInstrumento		= garEspecie.MontoMonedaSistema;
				aux.EsEspecifica						= (garEspecie.IdOperacion != null ? (garEspecie.IdOperacion.compareTo(0L) != 0 ? "S" : "N") : "N");
				aux.TablaAfectada						= "TRANSITO";
				aux.EsRegistroNuevo						= "S";
				aux.AccionOperacion						= "INGRESO";
				aux.IdUsuario							= in.p_IdUsuario;
				p_Datos.add(aux);
				
				hGenerarMovimientoCustodia.put(aux.IdGenerarMovimientoCustodia, new ObjGenerarMovimientoCustodia(garEspecie.IdGarantiaVigente, garEspecie.MontoMonedaSistema, BigDecimal.ZERO, BigDecimal.ZERO));
				
				InformacionConceptosCustodia.ObjDatosEntrada obj = new InformacionConceptosCustodia.ObjDatosEntrada();
				Utils.copyPojo(aux, obj);
				obj.IdInstrumento = garEspecie.IdInstrumentoGarantizado;
				obj.IdNombreEntidad = "CUS.GARANTIA";
				IdConceptos.add(obj);
			};
			
			//AGREGAR LOG
			{
				AgregarLog agregarLogService = new AgregarLog();
				for (ObjGarantiaEspecie gar: in.p_GarantiaEspecies) {
					AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
					AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
					entradaAgregarLog.p_IdTrabajo = 1L;
					log.IdTrabajo = entradaAgregarLog.p_IdTrabajo;
					log.IdCabeceraLog = gar.IdPortafolio;
					log.IdTipoLog = "INGRESO";
					log.IdModulo = "CUS";
					log.IdUsuario = in.p_IdUsuario;
					log.IdEmpresa = in.p_IdEmpresa;
					log.FechaSistemaLog = fechaSistema;
					log.IdUsuarioAtendido = in.p_IdUsuario;
					log.IdCliente = gar.IdCliente;
					log.EstadoCliente = "";
					log.NombreCliente = "";
					log.IdTipoDocumentoIdentidad = "";
					log.IdDocumentoIdentidad = "";
					log.IdPortafolio = gar.IdPortafolio;
					log.EstadoPortafolio = "";
					log.IdTipoRegistro = "CUSTODIA";
					log.EstadoRegistro = "";
					log.IdTipoOperacion = gar.IdTipoOperacion;
					log.Folio = 0L;
					log.Cantidad =BigDecimal.ZERO;
					log.PrecioTasa = BigDecimal.ZERO;
					log.Monto = BigDecimal.ZERO;
					log.IdMonedaOrigen = gar.IdMoneda;
					log.IdMonedaLiquidacion = "";
					log.TipoCambio = "";
					log.Nemo = "";
					log.IdProceso = 0L;
					log.EstadoProceso = "";
					log.NombreProceso = "";						
					entradaAgregarLog.p_Log.add(log);
					agregarLogService.logica(con, entradaAgregarLog);
				}
			}
			
			List<IngresoEgreso_Entrada> listaIngresoEgresoTes = new ArrayList<CrearIngresoEgreso.IngresoEgreso_Entrada>();

			//GARANTIAS EN EFECTIVO (Cuenta inversion)			
			for (ObjGarantiaCuentaInversion garCuentaInversion: in.p_GarantiaCuentaInversion){
				ObjContraparte InfoContraparte = (ObjContraparte) InformacionContraparte.get(garCuentaInversion.IdContraparteGarantia);
				if (InfoContraparte == null) continue; //TODO indicar mensaje de error
				
				IngresoCustodia.ObjDatosEntrada aux = new IngresoCustodia.ObjDatosEntrada();					
				aux.IdGenerarMovimientoCustodia			= IdGenerarMovimientoCustodia++;
				aux.IdInstrumentoCarteraGarantizada		= garCuentaInversion.IdInstrumentoGarantizado;
				aux.IdContraparteGarantia				= garCuentaInversion.IdContraparteGarantia;
				aux.RazonSocialContraparteGarantia		= InfoContraparte.RazonSocial;
				aux.IdOrigenGarantia					= "CTA";
				aux.IdCliente							= garCuentaInversion.IdCliente;
				aux.IdPortafolio						= garCuentaInversion.IdPortafolio;
				aux.IdAgente 							= garCuentaInversion.IdAgente;
				aux.IdConcepto							= garCuentaInversion.IdConcepto;
				aux.IdTipoOperacionGestora				= garCuentaInversion.IdTipoOperacion;
				aux.IdTipoOperacion						= garCuentaInversion.IdTipoOperacion;
				aux.IdOperacion							= garCuentaInversion.IdOperacion;
				aux.FechaOperacion						= Fecha.toDate(in.p_FechaMovimiento);
				aux.IdTipoComprobante					= garCuentaInversion.IdTipoComprobante;
				aux.FolioComprobante					= garCuentaInversion.FolioComprobante;
				aux.FechaComprobante					= garCuentaInversion.FechaComprobante;
				aux.IdBolsa								= garCuentaInversion.IdBolsa;
				aux.EsEspecifica						= (garCuentaInversion.IdOperacion != null ? (garCuentaInversion.IdOperacion.compareTo(0L) != 0 ? "S" : "N") : "N");
				aux.IdCondicionLiquidacion				= "T0";
				aux.FechaLiquidacion					= Fecha.toDate(in.p_FechaMovimiento);				
				aux.IdMonedaInstrumento					= garCuentaInversion.IdMoneda;
				aux.Cantidad							= garCuentaInversion.MontoMonedaInstrumento;
				aux.MontoCompra							= garCuentaInversion.MontoMonedaSistema;
				aux.MontoCompraMonedaInstrumento		= garCuentaInversion.MontoMonedaInstrumento;
				aux.TablaAfectada						= "TRANSITO";
				aux.EsRegistroNuevo						= "S";
				aux.AccionOperacion						= "INGRESO";
				aux.IdUsuario							= in.p_IdUsuario;
				p_Datos.add(aux);
				hGenerarMovimientoCustodia.put(aux.IdGenerarMovimientoCustodia, new ObjGenerarMovimientoCustodia(garCuentaInversion.IdGarantiaVigente,garCuentaInversion.CantidadVigente,garCuentaInversion.INGMontoATasaGarantiaActualVigente,garCuentaInversion.INGMontoGarantiaActualMonedaInstrumentoVigente));
				
				
				InformacionConceptosCustodia.ObjDatosEntrada obj = new InformacionConceptosCustodia.ObjDatosEntrada();

				Utils.copyPojo(aux, obj);
				obj.IdInstrumento = garCuentaInversion.IdInstrumentoGarantizado;
				obj.IdNombreEntidad = "CUS.GARANTIA";
				IdConceptos.add(obj);
				
			};
						
			
			if (listaIngresoEgresoTes.size() > 0) {
				logger.debug("INICIO GENERAR MOVIMIENTO TESORERIA");
	
				CrearIngresoEgreso serviceCrearIngresoEgreso = new CrearIngresoEgreso();
				CrearIngresoEgreso_Entrada paramsInTes = new CrearIngresoEgreso_Entrada();
				paramsInTes.p_EsIngresoAutomatico = "S";
				
				
				paramsInTes.p_IdEmpresa = in.p_IdEmpresa;
				paramsInTes.p_IdModulo = "FAC";
				paramsInTes.p_IdUsuario = in.p_IdUsuario;
				
				paramsInTes.p_IngresoEgresoGenerar = listaIngresoEgresoTes;
				paramsInTes.p_SimularOperacion = "N";
				
				paramsInTes.p_FechaMovimiento = ((IngresoEgreso_Entrada) listaIngresoEgresoTes.get(0)).FechaComprobante;
				 
				paramsInTes.p_UsaReglasNegocio = "S";
				CrearIngresoEgreso_Salida salidaCrearIngresoEgreso = serviceCrearIngresoEgreso.logica(con, paramsInTes);
				out.P_OUT_MENSAJES.addAll(salidaCrearIngresoEgreso.P_OUT_MENSAJES);
				
				logger.debug("FIN GENERAR MOVIMIENTO TESORERIA");
			}	
	
			IngresoCustodia ingresoCustodia = new IngresoCustodia();
			IngresoCustodia.IngresoCustodiaEntrada paramInSP = new IngresoCustodia.IngresoCustodiaEntrada();
			paramInSP.p_IdEmpresa			= in.p_IdEmpresa;			
			paramInSP.p_IdModulo			= in.p_IdModulo;
			paramInSP.p_SimularOperacion	= in.p_SimulaOperacion;
			paramInSP.p_TicketAtencion		= TicketAtencion;
			paramInSP.p_DatosEntrada		= p_Datos;
			IngresoCustodia.IngresoCustodiaSalida resultIngresoTransito = ingresoCustodia.logica(con, paramInSP);
			
			for (IngresoCustodia.IngresoCustodiaSalida.ObjDatosSalida result : resultIngresoTransito.P_OUT_DATOSSALIDA){
				if (result.RegistroConProblemas.equalsIgnoreCase("S")){
					//TODO : incorporar mensajes de error
					out.P_OUT_ERRORESCONTADOR = 1L;
					continue;
			    };				
				IdMovimientosEnTransito.add( result.IdCustodia);
				ObjGenerarMovimientoCustodia gMovCustodia = (ObjGenerarMovimientoCustodia) hGenerarMovimientoCustodia.get(result.IdGenerarMovimientoCustodia);
				if(gMovCustodia != null) {
					hGenerarMovimientoCustodia.put(result.IdCustodia, new ObjGenerarMovimientoCustodia(result.IdCustodia, gMovCustodia.IdGarantiaVigente, gMovCustodia.CantidadVigente, gMovCustodia.INGMontoATasaGarantiaActualVigente, gMovCustodia.INGMontoGarantiaActualMonedaInstrumentoVigente));
					hGenerarMovimientoCustodia.remove(result.IdGenerarMovimientoCustodia);
				}
			};					
		} else {
			for (Long M : in.p_MovimientosEnTransito){
				IdMovimientosEnTransito.add(M);
			};				
		};
		
		//AGREGAR LOG
		{
			AgregarLog agregarLogService = new AgregarLog();
			for (ObjGarantiaCuentaInversion gar: in.p_GarantiaCuentaInversion) {
				AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
				AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
				entradaAgregarLog.p_IdTrabajo = 1L;
				log.IdTrabajo = entradaAgregarLog.p_IdTrabajo;
				log.IdCabeceraLog = gar.FolioComprobante;
				log.IdTipoLog = "INGRESO";
				log.IdModulo = "CUS";
				log.IdUsuario = in.p_IdUsuario;
				log.IdEmpresa = in.p_IdEmpresa;
				log.FechaSistemaLog = fechaSistema;
				log.IdUsuarioAtendido = in.p_IdUsuario;
				log.IdCliente = gar.IdCliente;
				log.EstadoCliente = "";
				log.NombreCliente = "";
				log.IdTipoDocumentoIdentidad = "";
				log.IdDocumentoIdentidad = "";
				log.IdPortafolio = gar.IdPortafolio;
				log.EstadoPortafolio = "";
				log.IdTipoRegistro = "CUSTODIA";
				log.EstadoRegistro = "";
				log.IdTipoOperacion = gar.IdTipoOperacion;
				log.Folio = gar.FolioComprobante;
				log.Cantidad = BigDecimal.ZERO;
				log.PrecioTasa = BigDecimal.ZERO;
				log.Monto = gar.MontoMonedaInstrumento;
				log.IdMonedaOrigen = gar.IdMoneda;
				log.IdMonedaLiquidacion = "";
				log.TipoCambio = "";
				log.Nemo = "";
				log.IdProceso = 0L;
				log.EstadoProceso = "";
				log.NombreProceso = "";						
				entradaAgregarLog.p_Log.add(log);
				agregarLogService.logica(con, entradaAgregarLog);
			}
		}
		
		//02 - PROCESAR MOVIMIENTO EN TRANSITO 
		List<GestionMovimientoCarteraEnTransito.ObjMovimientosEnTransito> MovimientosEnTransito =  GestionMovimientoCarteraEnTransito.ObtenerMovimientoEnTransito(con, IdMovimientosEnTransito); 
		List<CrearCargoAbono.ObjCargoAbonoAGenerar> CargosAbonosAGenerar = new ArrayList<CrearCargoAbono.ObjCargoAbonoAGenerar>();			
		if ( MovimientosEnTransito.size() > 0 ){
			List<IngresoCustodia.ObjDatosEntrada> p_IngresoGarantia = new ArrayList<IngresoCustodia.ObjDatosEntrada>();
			List<RetiroCustodia.ObjDatosEntrada> p_RetiroCartera = new ArrayList<RetiroCustodia.ObjDatosEntrada>();
			List<GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar> p_Transitos = new ArrayList<GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar>();				
			List<InformacionConceptosCustodia.ObjDatosSalida> Concep = InformacionConceptosCustodia.informacionConcepto(con, IdConceptos);				
			HashMap<String,Object> Conceptos = Utils.List2Hash(Concep, "IdConcepto");
			
			for (GestionMovimientoCarteraEnTransito.ObjMovimientosEnTransito Mov : MovimientosEnTransito) {	
				if (in.p_Certifica.equals("N")){
					InformacionConceptosCustodia.ObjDatosSalida ConceptoCustodia = (InformacionConceptosCustodia.ObjDatosSalida) Conceptos.get(Mov.IdConcepto);
					if (ConceptoCustodia==null || !ConceptoCustodia.IdConcepto.equalsIgnoreCase(Mov.IdConcepto)) continue;  //TODO indicar mensaje de error (error de configuracion concepto)
					if (ConceptoCustodia.ConCertificadoAutomatico.equals("N")){
						HashMap<String,String> valores = new HashMap<String,String>();
						valores.put("Folio", Mov.IdMovimientoCarteraEnTransito.toString());
						out.P_OUT_MENSAJES.add(new MensajeSalida(Mov.IdMovimientoCarteraEnTransito, MensajeSalida.MENSAJE_ADVERTENCIA, "UI_INGRESO_TITULO_TRANSITO_OK", valores ,con,in.p_IdEmpresa));
						continue;
					};
				};
				
				IngresoCustodia.ObjDatosEntrada Ing = new IngresoCustodia.ObjDatosEntrada();
				RetiroCustodia.ObjDatosEntrada Ret = new RetiroCustodia.ObjDatosEntrada();
								
				if (!Mov.IdOrigenGarantia.equals("CUS")){
					ObjGenerarMovimientoCustodia gMovCustodia = (ObjGenerarMovimientoCustodia) hGenerarMovimientoCustodia.get(Mov.IdMovimientoCarteraEnTransito);
					
					Utils.copyPojo(Mov, Ing);
					Ing.IdGenerarMovimientoCustodia = Mov.IdMovimientoCarteraEnTransito; 
					Ing.TablaAfectada 	= "GARANTIA";
					Ing.AccionOperacion = "INGRESO";
					Ing.IdMovimientoCarteraEnTransitoOrigen	= Mov.IdMovimientoCarteraEnTransito;
					Ing.EsRegistroNuevo	= "S";
					Ing.NemoSVS = Mov.NemoSVS;
					if(gMovCustodia != null && "S".equals(in.p_EsProcesoContrapartida)) {
						Ing.EsRegistroNuevo = "N";
						Ing.IdCustodia = gMovCustodia.IdGarantiaVigente;
						Ing.Cantidad = Mov.Cantidad.add(gMovCustodia.CantidadVigente);
						Ing.MontoCompra = Mov.MontoCompra.add(gMovCustodia.INGMontoATasaGarantiaActualVigente);
						Ing.MontoCompraMonedaInstrumento = Mov.MontoCompraMonedaInstrumento.add(gMovCustodia.INGMontoGarantiaActualMonedaInstrumentoVigente);
					}
					p_IngresoGarantia.add(Ing);
				} else {
					Utils.copyPojo(Mov, Ret);
					Ret.IdGenerarMovimientoCustodia = Mov.IdMovimientoCarteraEnTransito;
					Ret.TablaAfectada 	= "CARTERA";
					Ret.AccionOperacion = "RETIRO";
					Ret.IdMovimientoCarteraEnTransitoOrigen	= Mov.IdMovimientoCarteraEnTransito;
					Ret.NemoSVS = Mov.NemoSVS;
					Ret.IdTipoReajusteInstrumento = Mov.IdTipoReajuste;
					p_RetiroCartera.add(Ret);
				};
				
				GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar MOV = new GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar();
				MOV.IdMovimientoCarteraEnTransito = Mov.IdMovimientoCarteraEnTransito;
				MOV.EsCertificacion = "S";
				MOV.IdUsuario = in.p_IdUsuario;
				p_Transitos.add(MOV);
			};
			
			//03 - REALIZAR INGRESOS
			if ( p_IngresoGarantia.size() > 0 ){
				IngresoCustodia.IngresoCustodiaEntrada paramInGarantia = new IngresoCustodia.IngresoCustodiaEntrada();
				paramInGarantia.p_IdEmpresa			= in.p_IdEmpresa;		
				paramInGarantia.p_IdModulo			= in.p_IdModulo;
				paramInGarantia.p_SimularOperacion	= in.p_SimulaOperacion;
				paramInGarantia.p_TicketAtencion	= TicketAtencion;
				paramInGarantia.p_DatosEntrada		= p_IngresoGarantia;
				IngresoCustodia ingresoCustodia = new IngresoCustodia();
				IngresoCustodia.IngresoCustodiaSalida resultIngresos = ingresoCustodia.logica(con, paramInGarantia);				

				HashMap<String,Object> MovimientosTransito = Utils.List2Hash(MovimientosEnTransito, "IdMovimientoCarteraEnTransito");
				for (IngresoCustodia.IngresoCustodiaSalida.ObjDatosSalida result : resultIngresos.P_OUT_DATOSSALIDA){
					HashMap<String,String> valores = new HashMap<String,String>();
					valores.put("Folio", result.FolioCustodia.toString());
					if ( result.RegistroConProblemas.equalsIgnoreCase("S") ) {
						out.P_OUT_ERRORESCONTADOR = 1L;
						out.P_OUT_MENSAJES.add(new MensajeSalida(result.FolioCustodia ,MensajeSalida.MENSAJE_ERROR, "UI_INGRESO_GARANTIA_ERROR", valores ,con, in.p_IdEmpresa));
						continue;
					} else {							
						out.P_OUT_MENSAJES.add(new MensajeSalida(result.FolioCustodia ,MensajeSalida.MENSAJE_INFORMACION, "UI_INGRESO_GARANTIA_OK", valores ,con, in.p_IdEmpresa));
					};
					
					IngresarGarantiaSalida.ObjResultado resultadoProceso = new IngresarGarantiaSalida.ObjResultado(); 
					Utils.copyPojo(result, resultadoProceso);
					resultadoProceso.TipoAccion = "INGRESO";
					out.P_OUT_RESULTADOPROCESO.add(resultadoProceso);
					
					GestionMovimientoCarteraEnTransito.ObjMovimientosEnTransito MovimientoTransito = (GestionMovimientoCarteraEnTransito.ObjMovimientosEnTransito) MovimientosTransito.get( result.IdGenerarMovimientoCustodia );
					if ( MovimientoTransito.IdOrigenGarantia.equals("CTA") ){
						CrearCargoAbono.ObjCargoAbonoAGenerar CargoAbono = new CrearCargoAbono.ObjCargoAbonoAGenerar();
						CargoAbono.NumeroCargoAbono				= result.IdGenerarMovimientoCustodia;
						CargoAbono.IdPortafolio					= MovimientoTransito.IdPortafolio; 
						CargoAbono.IdMoneda						= MovimientoTransito.IdMonedaInstrumento; 
						CargoAbono.IdTipoMovimiento				= "MOVCAR"; 
						CargoAbono.IdConcepto					= MovimientoTransito.IdConcepto; 
						CargoAbono.Descripcion					= ""; 
						CargoAbono.IdCondicionDeLiquidacion		= "T0"; 
						CargoAbono.FechaCondicionDeLiquidacion	= in.p_FechaMovimiento; 
						CargoAbono.Monto						= MovimientoTransito.MontoCompraMonedaInstrumento; 
						//CargoAbono.IdEjecutivo					= MovimientoTransito.IdAgente;
						CargoAbono.IdEjecutivo 					= (in.p_IdEmpleado != null ? in.p_IdEmpleado : MovimientoTransito.IdAgente);
						CargoAbono.IdSucursal					= 0L; 
						CargoAbono.IdTipoProducto				= "";
						CargoAbono.IdTipoOperacion				= MovimientoTransito.IdTipoOperacion; 
						CargoAbono.IdOperacion					= result.FolioCustodia;
						CargoAbono.FechaOperacion				= in.p_FechaMovimiento;
						CargoAbono.FechaLiquidacionOperacion	= in.p_FechaMovimiento; 
						CargoAbono.FechaVencimientoOperacion	= "0001-01-01"; 
						CargoAbono.IdMonedaOperacion			= ""; 
						CargoAbono.MontoOperacion				= new BigDecimal(0); 
						CargoAbono.TipoCambioOperacion			= new BigDecimal(0); 
						CargoAbono.IdContraparte				= MovimientoTransito.IdContraparteGarantia; 
						CargoAbono.IdTipoComprobante			= ""; 
						CargoAbono.IdComprobante				= result.IdCustodia; 
						CargoAbono.FolioComprobante				= result.FolioCustodia.toString();
						CargoAbono.IdComprobanteDetalle			= 0L; 
						CargoAbono.IdComprobanteFormaDePago		= 0L; 
						CargoAbono.IdTipoMovimientoAsociado		= ""; 
						CargoAbono.IdMovimientoAsociado			= 0L; 
						CargoAbono.FechaComprobante				= in.p_FechaMovimiento;
						CargoAbono.IdPersonaPagoCobro			= 0L;
						CargoAbono.IdInstrumento				= MovimientoTransito.IdInstrumentoCarteraGarantizada; 
						CargoAbono.IdTipoInstrumento			= MovimientoTransito.IdTipoInstrumento; 
						CargoAbono.Nemo							= ""; 
						CargoAbono.EsIndicadorBilateral			= ""; 
						CargoAbono.EsCargoAbonoPorTraspaso		= "N"; 
						CargoAbono.IdPortafolioTraspaso			= 0L; 
						CargoAbono.GeneroContraparteEnTesoreria	= "N"; 
						CargoAbono.CertificadoAutomatico		= "S";
						if("S".equals(in.p_EsProcesoContrapartida)) {
							CargoAbono.IdMovimientoAsociado = MovimientoTransito.IdMovimientoCarteraEnTransito;
						}
						CargosAbonosAGenerar.add(CargoAbono);
					};
				};
			};
			
			//04 - REALIZAR RETIROS
			if ( p_RetiroCartera.size() > 0 ){
				RetiroCustodia retiroCustodia = new RetiroCustodia();
				RetiroCustodia.RetiroCustodiaEntrada paramRetCartera = new RetiroCustodia.RetiroCustodiaEntrada();
				paramRetCartera.p_IdEmpresa			= in.p_IdEmpresa;
				paramRetCartera.p_SimularOperacion	= in.p_SimulaOperacion;
				paramRetCartera.p_TicketAtencion	= TicketAtencion;
				paramRetCartera.p_DatosEntrada		= p_RetiroCartera;
				RetiroCustodia.RetiroCustodiaSalida resultRetiros = retiroCustodia.logica(con, paramRetCartera);					
				if (!resultRetiros.P_OUT_ERRORESCONTADOR.equals(0L)){
					for (RetiroCustodia.RetiroCustodiaSalida.ObjDatosSalida msg : resultRetiros.P_OUT_DATOSSALIDA){
						if (msg.RegistroConProblemas.equals("N")) continue;
						out.P_OUT_MENSAJES.add(new MensajeSalida(msg.IdGenerarMovimientoCustodia ,MensajeSalida.MENSAJE_ERROR, msg.Mensaje, new HashMap<>() ,con,in.p_IdEmpresa));
					};
					out.P_OUT_ERRORESCONTADOR = 1L;
					return out;												
				};
				List<IngresoCustodia.ObjDatosEntrada> p_IngresoGarantia2 = new ArrayList<IngresoCustodia.ObjDatosEntrada>();
				for (GestionMovimientoCarteraEnTransito.ObjMovimientosEnTransito Mov : MovimientosEnTransito) {
					for (RetiroCustodia.RetiroCustodiaSalida.ObjDatosSalida result : resultRetiros.P_OUT_DATOSSALIDA){
						if (!Mov.IdMovimientoCarteraEnTransito.equals(result.IdGenerarMovimientoCustodia)) continue;
						if ( result.RegistroConProblemas.equalsIgnoreCase("S") ){
							//TODO : incorporar mensajes de error
							out.P_OUT_ERRORESCONTADOR = 1L;
							continue;
						};
						
						IngresarGarantiaSalida.ObjResultado resultadoProceso = new IngresarGarantiaSalida.ObjResultado(); 
						Utils.copyPojo(result, resultadoProceso);
						resultadoProceso.TipoAccion = "RETIRO";
						out.P_OUT_RESULTADOPROCESO.add(resultadoProceso);
						ObjGenerarMovimientoCustodia gMovCustodia = (ObjGenerarMovimientoCustodia) hGenerarMovimientoCustodia.get(result.IdGenerarMovimientoCustodia);
					
						IngresoCustodia.ObjDatosEntrada p_paramsInGar = new IngresoCustodia.ObjDatosEntrada();
						Utils.copyPojo(Mov, p_paramsInGar);
						p_paramsInGar.IdGenerarMovimientoCustodia = result.IdCustodia; 
						p_paramsInGar.TablaAfectada 		= "GARANTIA";
						p_paramsInGar.AccionOperacion 		= "INGRESO";
						p_paramsInGar.IdMovimientoCarteraEnTransitoOrigen	= Mov.IdMovimientoCarteraEnTransito;
						p_paramsInGar.EsRegistroNuevo		= "S";
						p_paramsInGar.IdCarteraOrigen		= result.IdCustodia;
						p_paramsInGar.FolioCarteraOrigen 	= result.FolioCustodia;
						p_paramsInGar.Cantidad				= result.Cantidad;
						p_paramsInGar.ValorRescate			= result.ValorRescate;
						p_paramsInGar.IdTipoReajusteInstrumento = Mov.IdTipoReajuste;
						if(gMovCustodia != null && "S".equals(in.p_EsProcesoContrapartida)) {
							p_paramsInGar.EsRegistroNuevo = "N";
							p_paramsInGar.IdCustodia = gMovCustodia.IdGarantiaVigente;
							p_paramsInGar.Cantidad = result.Cantidad.add(gMovCustodia.CantidadVigente);
						}
						p_IngresoGarantia2.add(p_paramsInGar);
					};
				};
				if (p_IngresoGarantia2.size() > 0){
					IngresoCustodia.IngresoCustodiaEntrada paramInGarantia = new IngresoCustodia.IngresoCustodiaEntrada();	
					paramInGarantia.p_IdEmpresa			= in.p_IdEmpresa;
					paramInGarantia.p_IdModulo			= in.p_IdModulo;
					paramInGarantia.p_SimularOperacion	= in.p_SimulaOperacion;
					paramInGarantia.p_TicketAtencion	= TicketAtencion;
					paramInGarantia.p_DatosEntrada		= p_IngresoGarantia2;
					IngresoCustodia ingresoCustodia = new IngresoCustodia();
					IngresoCustodia.IngresoCustodiaSalida resultIngresos = ingresoCustodia.logica(con, paramInGarantia);

					for (IngresoCustodia.IngresoCustodiaSalida.ObjDatosSalida result : resultIngresos.P_OUT_DATOSSALIDA){
						HashMap<String,String> valores = new HashMap<String,String>();
						valores.put("Folio", result.FolioCustodia.toString());
						
						if ( result.RegistroConProblemas.equalsIgnoreCase("S") ) {
							out.P_OUT_ERRORESCONTADOR = 1L;
							out.P_OUT_MENSAJES.add(new MensajeSalida(result.FolioCustodia ,MensajeSalida.MENSAJE_ERROR, "UI_INGRESO_GARANTIA_ERROR", valores ,con, in.p_IdEmpresa));
							continue;
						} else {								
							IngresarGarantiaSalida.ObjResultado resultadoProceso = new IngresarGarantiaSalida.ObjResultado(); 
							Utils.copyPojo(result, resultadoProceso);
							resultadoProceso.TipoAccion = "RETIRO";
							out.P_OUT_RESULTADOPROCESO.add(resultadoProceso);
							
							out.P_OUT_MENSAJES.add(new MensajeSalida(result.FolioCustodia ,MensajeSalida.MENSAJE_INFORMACION, "UI_INGRESO_GARANTIA_OK", valores ,con, in.p_IdEmpresa));
						};
					};
				};
			};
									
			if (out.P_OUT_ERRORESCONTADOR.equals(0L)){
				//05 - REALIZAR ACTUALIZACION MOVIMIENTO EN TRANSITO
				GestionMovimientoCarteraEnTransito.ActualizarMovimientoEnTransito(con, p_Transitos);
				
				//06 - GARANTIAS EN EFECTIVO
				if ( CargosAbonosAGenerar.size() > 0 ){
					CrearCargoAbono.CrearCargoAbonoEntrada paramsCrearCargoAbono = new CrearCargoAbono.CrearCargoAbonoEntrada();
					paramsCrearCargoAbono.p_IdEmpresa			= in.p_IdEmpresa;
					paramsCrearCargoAbono.p_IdUsuario			= in.p_IdUsuario;
					paramsCrearCargoAbono.p_IdModulo			= in.p_IdModulo;
					paramsCrearCargoAbono.p_simularOperacion	= "N";
					paramsCrearCargoAbono.p_UsaReglasNegocio	= "S";
					paramsCrearCargoAbono.p_FechaSistema		= in.p_FechaMovimiento;
					paramsCrearCargoAbono.p_EsIngresoManual		= "N";
					paramsCrearCargoAbono.p_CargoAbonoAGenerar 	= CargosAbonosAGenerar;
					
					CrearCargoAbono CrearCargoAbonoSP= new CrearCargoAbono();
					CrearCargoAbono.CrearCargoAbonoSalida resultCrearCargoAbono = CrearCargoAbonoSP.logica(con, paramsCrearCargoAbono);
					for (MensajeSalida  AdveError: resultCrearCargoAbono.P_OUT_MENSAJES){
						out.P_OUT_MENSAJES.add(AdveError);							
					};
					out.P_OUT_ERRORESCONTADOR = resultCrearCargoAbono.P_OUT_ERRORESCONTADOR;
					
				};
				
				GestionMovimientoCarteraEnTransito.ActualizarMovimientoEnTransito(con, p_Transitos);
				
			};
		};
			
		return out;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		IngresarGarantiaEntrada jin=gson.fromJson(in.toString() ,IngresarGarantiaEntrada.class);
		IngresarGarantiaSalida out=logica(conn,jin);		
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	public static void main(String[] args) throws Exception {		
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa02")) {
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();				
				String jsonDESA = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"CUS\",\"p_IdEmpleado\":7889,\"p_UsaReglasNegocio\":\"S\",\"p_SimulaOperacion\":\"N\",\"p_Certifica\":\"N\",\"p_FechaMovimiento\":\"2022-01-14\",\"p_GarantiaCustodia\":[],\"p_GarantiaCuentaInversion\":[{\"IdMoneda\":\"CLP\",\"MontoMonedaInstrumento\":9000000,\"MontoMonedaSistema\":9000000,\"IdCliente\":1424283,\"IdPortafolio\":1451997,\"IdAgente\":7905,\"IdTipoInstrumento\":\"PRT\",\"IdBolsa\":\"BCS\",\"IdInstrumentoGarantizado\":10012,\"IdConcepto\":\"GAPRAD\",\"IdContraparteGarantia\":561820,\"IdTipoOperacion\":\"INGGTA\",\"IdOperacion\":0}],\"p_GarantiaEspecies\":[]}";
//				String jsonDESA = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"CUS\",\"p_IdEmpleado\":7889,\"p_UsaReglasNegocio\":\"S\",\"p_SimulaOperacion\":\"N\",\"p_Certifica\":\"N\",\"p_FechaMovimiento\":\"2022-01-14\",\"p_GarantiaCustodia\":[],\"p_GarantiaCuentaInversion\":[{\"IdMoneda\":\"CLP\",\"MontoMonedaInstrumento\":500000,\"MontoMonedaSistema\":500000,\"IdCliente\":1424283,\"IdPortafolio\":1451997,\"IdAgente\":7905,\"IdTipoInstrumento\":\"PRT\",\"IdBolsa\":\"BCS\",\"IdInstrumentoGarantizado\":10011,\"IdConcepto\":\"GAPRFG\",\"IdContraparteGarantia\":561820,\"IdTipoOperacion\":\"INGGTA\",\"IdOperacion\":0}],\"p_GarantiaEspecies\":[]}";
				String jsonCUS = "{\r\n"
						+ "    \"p_IdEmpresa\": 1,\r\n"
						+ "    \"p_IdUsuario\": 97,\r\n"
						+ "    \"p_IdModulo\": \"CUS\",\r\n"
						+ "    \"p_IdEmpleado\": 108,\r\n"
						+ "    \"p_UsaReglasNegocio\": \"S\",\r\n"
						+ "    \"p_SimulaOperacion\": \"N\",\r\n"
						+ "    \"p_Certifica\": \"N\",\r\n"
						+ "    \"p_FechaMovimiento\": \"2024-10-21\",\r\n"
						+ "    \"p_GarantiaCustodia\": [\r\n"
						+ "        {\r\n"
						+ "            \"IdInstrumento\": 502,\r\n"
						+ "            \"IdMoneda\": \"CLP\",\r\n"
						+ "            \"IdMonedaEmisionInstrumento\": \"\",\r\n"
						+ "            \"Cantidad\": 500,\r\n"
						+ "            \"ValorRescate\": 500,\r\n"
						+ "            \"MontoMonedaInstrumento\": 2812663.3499999996,\r\n"
						+ "            \"MontoMonedaSistema\": 2812663.3499999996,\r\n"
						+ "            \"IdClasificacionTipoInstrumento\": \"RV\",\r\n"
						+ "            \"IdTipoReajuste\": \"\",\r\n"
						+ "            \"NemoSVS\": \"\",\r\n"
						+ "            \"FechaEmisionInstrumento\": \"1935-10-02\",\r\n"
						+ "            \"FechaVencimientoInstrumento\": \"0001-01-01\",\r\n"
						+ "            \"IdEmisor\": \"COPEC\",\r\n"
						+ "            \"PlazoEnDias\": 0,\r\n"
						+ "            \"EsFungible\": \"N\",\r\n"
						+ "            \"IdCliente\": 4843,\r\n"
						+ "            \"IdPortafolio\": 1285,\r\n"
						+ "            \"IdAgente\": 31,\r\n"
						+ "            \"IdTipoInstrumento\": \"\",\r\n"
						+ "            \"IdBolsa\": \"BCS\",\r\n"
						+ "            \"IdInstrumentoGarantizado\": 0,\r\n"
						+ "            \"IdConcepto\": \"IGRV85\",\r\n"
						+ "            \"IdContraparteGarantia\": 17,\r\n"
						+ "            \"Precio\": 6228,\r\n"
						+ "            \"IdTipoOperacion\": \"INGGTA\",\r\n"
						+ "            \"IdOperacion\": 0,\r\n"
						+ "            \"CorteMinimo\": 0\r\n"
						+ "        }\r\n"
						+ "    ],\r\n"
						+ "    \"p_GarantiaCuentaInversion\": [],\r\n"
						+ "    \"p_GarantiaEspecies\": []\r\n"
						+ "}";
				String jsonCTA = "{\r\n"
						+ "    \"p_IdEmpresa\": 1,\r\n"
						+ "    \"p_IdUsuario\": 97,\r\n"
						+ "    \"p_IdModulo\": \"CUS\",\r\n"
						+ "    \"p_IdEmpleado\": 108,\r\n"
						+ "    \"p_UsaReglasNegocio\": \"S\",\r\n"
						+ "    \"p_SimulaOperacion\": \"N\",\r\n"
						+ "    \"p_Certifica\": \"N\",\r\n"
						+ "    \"p_FechaMovimiento\": \"2024-09-09\",\r\n"
						+ "    \"p_GarantiaCustodia\": [],\r\n"
						+ "    \"p_GarantiaCuentaInversion\": [\r\n"
						+ "        {\r\n"
						+ "            \"IdMoneda\": \"CLP\",\r\n"
						+ "            \"MontoMonedaInstrumento\": 100000,\r\n"
						+ "            \"MontoMonedaSistema\": 100000,\r\n"
						+ "            \"IdCliente\": 11828,\r\n"
						+ "            \"IdPortafolio\": 12851,\r\n"
						+ "            \"IdAgente\": 89,\r\n"
						+ "            \"IdTipoInstrumento\": \"\",\r\n"
						+ "            \"IdBolsa\": \"BCS\",\r\n"
						+ "            \"IdInstrumentoGarantizado\": 0,\r\n"
						+ "            \"IdConcepto\": \"IGRV85\",\r\n"
						+ "            \"IdContraparteGarantia\": 17,\r\n"
						+ "            \"IdTipoOperacion\": \"INGGTA\",\r\n"
						+ "            \"IdOperacion\": 0\r\n"
						+ "        }\r\n"
						+ "    ],\r\n"
						+ "    \"p_GarantiaEspecies\": []\r\n"
						+ "}";
				String esp = "{\r\n"
						+ "    \"p_IdEmpresa\": 1,\r\n"
						+ "    \"p_IdUsuario\": 97,\r\n"
						+ "    \"p_IdModulo\": \"CUS\",\r\n"
						+ "    \"p_IdEmpleado\": 108,\r\n"
						+ "    \"p_UsaReglasNegocio\": \"S\",\r\n"
						+ "    \"p_SimulaOperacion\": \"N\",\r\n"
						+ "    \"p_Certifica\": \"N\",\r\n"
						+ "    \"p_FechaMovimiento\": \"2024-10-21\",\r\n"
						+ "    \"p_GarantiaCustodia\": [],\r\n"
						+ "    \"p_GarantiaCuentaInversion\": [],\r\n"
						+ "    \"p_GarantiaEspecies\": [\r\n"
						+ "        {\r\n"
						+ "            \"IdEspecie\": 3,\r\n"
						+ "            \"IdMoneda\": \"CLP\",\r\n"
						+ "            \"MontoMonedaSistema\": 30000,\r\n"
						+ "            \"Monto\": 30000,\r\n"
						+ "            \"Glosa\": \"\",\r\n"
						+ "            \"IdCliente\": 4298,\r\n"
						+ "            \"IdPortafolio\": 226,\r\n"
						+ "            \"IdAgente\": 67,\r\n"
						+ "            \"IdTipoInstrumento\": \"\",\r\n"
						+ "            \"IdBolsa\": \"BCS\",\r\n"
						+ "            \"IdInstrumentoGarantizado\": 0,\r\n"
						+ "            \"IdConcepto\": \"IGRV85\",\r\n"
						+ "            \"IdContraparteGarantia\": 17,\r\n"
						+ "            \"IdTipoOperacion\": \"INGGTA\",\r\n"
						+ "            \"IdOperacion\": 0\r\n"
						+ "        }\r\n"
						+ "    ]\r\n"
						+ "}";
				
				IngresarGarantiaEntrada Objin = gson.fromJson(jsonCUS, IngresarGarantiaEntrada.class);
								
				IngresarGarantiaJava  kk = new IngresarGarantiaJava();
				System.out.println(kk.logica(con, Objin));
				con.rollback();
//				con.commit();
				
		    	}catch(Exception er) {
					er.printStackTrace();
					con.rollback();
				}
		} catch (Exception e) {
			e.printStackTrace();			
		}
	};
}
