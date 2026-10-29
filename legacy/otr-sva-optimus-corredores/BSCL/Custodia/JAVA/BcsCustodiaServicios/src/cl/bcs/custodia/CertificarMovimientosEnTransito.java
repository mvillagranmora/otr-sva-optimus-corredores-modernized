package cl.bcs.custodia;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.CertificarMovimientosEnTransito.CertificarMovimientosEnTransitoSalida.ObjMovimientosProcesados;
import cl.bcs.custodia.ValidaCantidadRetiroCustodia.EntradaValidacionCustodia;
import cl.bcs.custodia.utils.GestionMovimientoCarteraEnTransito;
import cl.bcs.custodia.utils.GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar;
import cl.bcs.custodia.utils.InformacionConfiguracionCustodia;
import cl.bcs.custodia.utils.LogCustodia;
import cl.bcs.facturacion.ProcesarFacturacionCustodia;
import cl.bcs.facturacion.ProcesarFacturacionCustodia.MovimientoCustodiaSalida;
import cl.bcs.facturacion.ProcesarFacturacionCustodia.ProcesarFacturacionCustodia_Entrada;
import cl.bcs.facturacion.ProcesarFacturacionCustodia.ProcesarFacturacionCustodia_Salida;
import cl.bcs.facturacion.ReversarFacturacionCustodia;
import cl.bcs.mesa.MRF.CrearOperacionMesaCustodia;
import cl.bcs.plataforma.evt.AgregarEvento;
import cl.bcs.plataforma.evt.AgregarEvento.ObjEVENTOS;
import cl.bcs.plataforma.evt.AgregarEvento.ObjIDDIRECCIONES;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarImagen;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.MonedaFormato.Resultado;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarImagen.ConsultarImagenEntrada;
import cl.bcs.plataforma.pla.utils.ConsultarImagen.ConsultarImagenSalida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
public class CertificarMovimientosEnTransito implements JavaCall{	

	private static final Logger logger = LogManager.getLogger(CertificarMovimientosEnTransito.class);
	
	private final String SQL_SELECT_CertificarMovimientos_SelectNotificacion = loadResourceToString("cl/bcs/custodia/sql/CertificarMovimientosEnTransito_SelectNotificacion.sql");
	
	private final String SQL_SELECT_COMPROBANTE_FACTURACION = loadResourceToString("cl/bcs/custodia/sql/CertificarMovimientosEnTransito_SelectComprobanteFacturacion.sql");
	private final String SQL_SELECT_MOVIMIENTOS_PENDIENTES = loadResourceToString("cl/bcs/custodia/sql/CertificarMovimientosEnTransito_SelectMovimientoEnTransitoPendientes.sql");
	private final String SQL_SELECT_INSTRUMENTOS = loadResourceToString("cl/bcs/custodia/sql/CertificarMovimientoEnTransito_SelectInfoInstrumento.sql");
	private final String SQL_UPDATE_ESTADO = loadResourceToString("cl/bcs/custodia/sql/CertificarMovimientoEnTransito_UpdateEstado.sql");
	private final String SQL_SELECT_MOVIMIENTO_EN_TRANSITO = loadResourceToString("cl/bcs/custodia/sql/CertificarMovimientosEnTransito_SelectMovimientoEnTransito.sql");
	private final String SQL_BLOQUEA_MOVIMIENTOS_EN_TRANSITO = loadResourceToString("cl/bcs/custodia/sql/CertificarMovimientosEnTransito_BloqueoMovimientoEnTransito.sql");
	
	public static class CertificarMovimientos_Notificacion extends BcsPojo {
		
		public String  		Instrumento;
		public String	 	NombreCompleto;
		public String 		TipoMoneda;
		public String 		DI;
		public String		TipoDoc;
		public String 		Operacion;
		public String 		NombreOperacion;
		public String		NumeroTel;
		public String		Corredora;
		public String 		Correo;
		public Long			IdPersona;
		//public String		IdPortafolio;
		public Long 		IdDireccionElectronicaPrincipal;
		public Long 		IdDireccionElectronicaPrincipalCLI;
		public Long 		IdMovimientoCarteraEnTransito;
		public Long 		IdPersonaCLI;
		public BigDecimal 	Cantidad;
		public BigDecimal 	PrecioCompra;
		public BigDecimal 	MontoCompra;
		public String		Agente;
		public String		CodigoPortafolio;
		public String		AtencionCliente;
		
	}
		
	public static class ObjMovimientos extends BcsPojo{
		public Long		IdMovimientoCarteraEnTransito;
		public String	IdTipoOperacion;
		public String   MovimientoGarantia;
		public String   Origen;
		public String   IdClasificacionTipoInstrumento;
		public BigDecimal Cantidad;
		public BigDecimal ValorRescate; 
		public Long IdOperacion;
		
		public ObjMovimientos () {
			this.MovimientoGarantia="N";
			this.Origen="CUS";
		}
			
	};

	public static class ObjUpdateEstado extends BcsPojo {
		public Long		IdMovimientoCarteraEnTransito;		
	};

	public static class CertificarMovimientosEnTransitoEntrada extends BcsPojo{
	    public Long					p_IdEmpresa;
	    public Long					p_IdUsuario;
	    public String				p_IdModulo;
	    public String				p_FechaOperacion;
	    public String				p_UsaReglasNegocio = "S";
	    public List<ObjMovimientos>	P_MOVIMIENTOSCERTIFICAR;

	};
	
	public static class MovimientoEnTransito {
		public Long			IdMovimientoCarteraEnTransito;
		public Long			IdCliente;
		public Long			IdPortafolio;
		public String		Nemotecnico;
		public String		IdTipoOperacion;
		public String		IdEstado;
		public BigDecimal	Cantidad;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class ComprobanteFacturacion extends BcsPojo{		
		public String							IdEstadoComprobante;
		public Long								IdComprobante;
		public Long	                            IdMovimientoCarteraEnTransito;
		public String                           EsVentaDescubierta;
				
	};
	
	public static class CertificarMovimientosEnTransitoSalida {		
		public List<MensajeSalida>				P_OUT_MENSAJES;
		public Long								P_OUT_ERRORESCONTADOR;
		public Long								P_OUT_TICKETATENCION;
		public List<ObjMovimientosProcesados>	P_OUT_MOVIMIENTOSPROCESADOS;
		
		
				
	public static class ObjMovimientosProcesados extends BcsPojo {
		public Long		IdMovimientoCarteraEnTransito;
		public String	IdEstado;
		public String   IdClasificacionTipoInstrumento;
		public String   IdConcepto;
		public String   IdTipoOperacion;
				
		
	};
	
		
	public CertificarMovimientosEnTransitoSalida (){
		P_OUT_MENSAJES 				= new ArrayList<MensajeSalida>();
		P_OUT_MOVIMIENTOSPROCESADOS	= new ArrayList<ObjMovimientosProcesados>();
		this.P_OUT_ERRORESCONTADOR=0L;
		
	};
	
	
	};
	
	public static class ObjInstrumento extends BcsPojo{
		//VARIABLES DEL PROCESO OBLIGATORIAS
		public Long						IdGenerarMovimientoCustodia = 0L;	//Registro secuencial unico
		public String					FechaOperacion;						//debe venir en formato AAAA-MM-DD
		public String					FechaOperacionOriginal;				//debe venir en formato AAAA-MM-DD
		public String					FechaVencimiento;					//debe venir en formato AAAA-MM-DD
		public String					FechaLiquidacion;					//debe venir en formato AAAA-MM-DD
		public String					IdCondicionLiquidacion;				//si no usara Condicion de Liq, enviar un valor <= 0
		public String					IdConcepto;
		public String					IdBolsa;
		public String					IdTipoOperacion;
		public String					IdTipoProducto = "";
		public Long						IdCliente;
		public Long						IdPortafolio;
		public Long						IdClienteDestino;
		public Long						IdPortafolioDestino;
		public String					IdBoveda;
		public String					CuentaBoveda;			
		public Long						IdCuentaCustodioPortafolio;				//--------------------------------------------------Campo nuevo de peru				
		public String					IdBovedaRelacionada;
		public String					CuentaBovedaRelacionada;
		public Long						IdCuentaCustodioPortafolioRelacionado;	//--------------------------------------------------Campo nuevo de peru
		public String					esTraspasoCliente = "N";
		public String					esTraspasoPortafolio = "N";		
		public String					esTraspasoBoveda = "N";		
		public String					DebeCertificar;
		public String					EsGarantia = "N";
		public Long						PlazoEnDias = 0L; 
		public Long						IdCartera = 0L;					//cartera a la cual puede tener referencia el movimiento
		public Long						FolioCartera = 0L;			
		public String					FolioExternoReferencia = "";	//comprobante de referencia
		public String					Glosa = "";
		public String					IndicadorNominal = "N"; 
		public Long						IdAgente;
		public Long						IdSucursal;			
		public String					IdTipoCanalIngreso;		
		public String					IdClasificacionTipoInstrumento;
		public String					IdTipoInstrumento;
		public Long						IdInstrumento;
		public String					Nemotecnico;
		public String					IdEmisor;
		public String					IdMonedaInstrumento = "";
		public String					IdMonedaEmision = "";
		public String					IdTipoReajuste = "";
		public String					IdMonedaAdicional = null;
		public String					FechaEmisionInstrumento = "0001-01-01";  //public java.sql.Date FechaEmisionInstrumento = Fecha.toDate("0001-01-01");
		public String					FechaVencimientoInstrumento = "0001-01-01"; 
		public String					Fungible = "N";
		public String					NemoSVS = "";		
		public BigDecimal				Precio = BigDecimal.ZERO;
		public BigDecimal				Cantidad = BigDecimal.ZERO;
		public BigDecimal				ValorRescate = BigDecimal.ZERO;
		public BigDecimal				CorteMinimo = BigDecimal.ONE;	
		public BigDecimal				Monto	= BigDecimal.ZERO;
		public BigDecimal				MontoEnMonedaInstrumento = BigDecimal.ZERO;
		public BigDecimal				Tasa = BigDecimal.ZERO;
		public BigDecimal				TasaAgente = BigDecimal.ZERO;
		public String 					CodigoIFRS = "";
		public String 					EsCtaPropia = "N";
		public String 					IdRegimenFondoMutuo = "";
		public String					Procesar = "S";//Por defecto: S=Procesar; N=No Procesar
		
	}
	

		
	public CertificarMovimientosEnTransitoSalida logica(Connection con, CertificarMovimientosEnTransitoEntrada in) throws Exception {
		CertificarMovimientosEnTransitoSalida out = new CertificarMovimientosEnTransitoSalida();
		out.P_OUT_ERRORESCONTADOR = 0L;
		HashMap<String,Object> params = new HashMap<>();
		HashMap<String,Object> paramsMvtsPendientes = new HashMap<>();
		SQL _sql = new SQL();
		
		//******************************************************************************************************************
		//												VALIDACIONES INICIALES
		//******************************************************************************************************************			
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();			
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			out.P_OUT_ERRORESCONTADOR+=MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes);
			return out;
		};
				
		List<ObjMovimientosProcesados> lstMvtsEnTransito = new ArrayList<ObjMovimientosProcesados>();
		if (in.P_MOVIMIENTOSCERTIFICAR == null ||in.P_MOVIMIENTOSCERTIFICAR.isEmpty() ){
			HashMap<String,String> valores = new HashMap<String,String>();
			valores.put("", "No hay movimientos para procesar");
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_VIENEN_MOVIMIENTOS",valores,con,in.p_IdEmpresa));
			out.P_OUT_ERRORESCONTADOR++;
		}
		else {
			//Valida si es que algun movimiento seleccionado fue certificado previo a ejecutar la certificacion
			HashMap<Long,ObjMovimientos> HashmovientosGarantia = new HashMap<Long,ObjMovimientos>();
			
			List<Long> idsMovimientoEnTransito = new ArrayList<Long>();	
			for (ObjMovimientos MvtoTrans :in.P_MOVIMIENTOSCERTIFICAR) {			
				idsMovimientoEnTransito.add(MvtoTrans.IdMovimientoCarteraEnTransito);
				if("S".equals(MvtoTrans.MovimientoGarantia)) {
					HashmovientosGarantia.put(MvtoTrans.IdMovimientoCarteraEnTransito, MvtoTrans);
				}
			}
			
			
			
			paramsMvtsPendientes.put("idsMovimientoCarteraEnTransito", idsMovimientoEnTransito);
			
			/// BLOQUEO DE LA TRANSACCION
			try {
				_sql.query(con, SQL_BLOQUEA_MOVIMIENTOS_EN_TRANSITO, paramsMvtsPendientes, ObjMovimientosProcesados.class);
			}catch(SQLException sqle) {
				if(sqle.getErrorCode()==131) {
					out.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR, "UI_TRANSACCION_BLOQUEADA_EN_PROCESO", "UI_TRANSACCION_BLOQUEADA_EN_PROCESO"));
					return out;
				}else {
					throw sqle;
				}
			}
			lstMvtsEnTransito = _sql.query(con, SQL_SELECT_MOVIMIENTOS_PENDIENTES, paramsMvtsPendientes , ObjMovimientosProcesados.class);
			if(!lstMvtsEnTransito.isEmpty()) {
				for(ObjMovimientosProcesados mov: lstMvtsEnTransito) {
					HashMap<String,String> valoresAut = new HashMap<String,String>();
					valoresAut.put("Movimiento", mov.IdMovimientoCarteraEnTransito.toString());
					if(mov.IdEstado.equals("PDA")) {
						out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_AUTORIZACION,"MOVIMIENTO_EN_TRANSITO_POR_AUTORIZAR",valoresAut,con,in.p_IdEmpresa));
						//out.P_OUT_ERRORESCONTADOR++;
					}	
					else if (("PEN").equals(mov.IdEstado) && ("IF".equals(mov.IdClasificacionTipoInstrumento) || "RF".equals(mov.IdClasificacionTipoInstrumento))) {
						this.validarMovimientoGarantia(mov,HashmovientosGarantia,out);
						
					}					
					
				}
			}
			if(lstMvtsEnTransito.isEmpty()) {
				HashMap<String,String> valores = new HashMap<String,String>();
				valores.put("", "Los movimientos ya se encuentran certificados");
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"MOVIMIENTO_EN_TRANSITO_CERTIFICADO",valores,con,in.p_IdEmpresa));
				out.P_OUT_ERRORESCONTADOR++;
			}
		}
	
		if (out.P_OUT_MENSAJES.size() > 0){
			return out;
		};
		
		//Informa los movimientos ya certificados
		if(lstMvtsEnTransito.size() > 0) {
			List<Long> idsMvtsEnTransito = lstMvtsEnTransito.stream().map(mvtoEnTransito -> mvtoEnTransito.IdMovimientoCarteraEnTransito).collect(Collectors.toList());
			List<ObjMovimientos> MvtosADescartar = new ArrayList<ObjMovimientos>();	
			for (ObjMovimientos MvtoTrans :in.P_MOVIMIENTOSCERTIFICAR) {
					if(!(idsMvtsEnTransito.contains(MvtoTrans.IdMovimientoCarteraEnTransito))) {
						HashMap<String,String> valores = new HashMap<String,String>();
						valores.put("Movimiento", MvtoTrans.IdMovimientoCarteraEnTransito.toString());
						out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,"MOVIMIENTOS_EN_TRANSITO_CERTIFICADOS",valores,con,in.p_IdEmpresa));
						MvtosADescartar.add(MvtoTrans);
					}
			}
			if(MvtosADescartar.size() > 0)
				in.P_MOVIMIENTOSCERTIFICAR.removeAll(MvtosADescartar);
		}
		List<Long> idsMovimientoCarteraEnTransito = new ArrayList<Long>();		
		
		if(lstMvtsEnTransito.size() > 0 ) {
			for (ObjMovimientosProcesados MvtoTrans : lstMvtsEnTransito) {			
				idsMovimientoCarteraEnTransito.add(MvtoTrans.IdMovimientoCarteraEnTransito); 			
			}
		}
		else {
			for (ObjMovimientos MvtoTrans :in.P_MOVIMIENTOSCERTIFICAR) {			
				idsMovimientoCarteraEnTransito.add(MvtoTrans.IdMovimientoCarteraEnTransito); 			
			}
		}
		
		params.put("idsMovimientoCarteraEnTransito", idsMovimientoCarteraEnTransito);		
		
		List<ComprobanteFacturacion> lstCmbteFac = _sql.query(con, SQL_SELECT_COMPROBANTE_FACTURACION, params , ComprobanteFacturacion.class);	
		HashMap<String,Object> hashCmbteFac = Utils.List2Hash(lstCmbteFac, "IdMovimientoCarteraEnTransito");
					
		
		//******************************************************************************************************************
		//													LOGICA
		//******************************************************************************************************************
		//00 - Informacion de Sistema
		List<InformacionConfiguracionCustodia.ObjConfiguracionMovimientoCustodia> ConfiguracionCUS = InformacionConfiguracionCustodia.informacionConfiguracion(con, in.p_IdEmpresa);
		HashMap<String,Object> ConfigCUS = Utils.List2Hash(ConfiguracionCUS, "IdTipoOperacion");
		
		//01 - LOG (Requerido)
		Long TicketAtencion = LogCustodia.logCertificarMovimientosEnTransito(con, in);
		if (TicketAtencion.equals(0L)){
			//TODO : incorporar mensajes de error
			out.P_OUT_ERRORESCONTADOR = 1L;				
			return out; 
		};
		
		
		//02 - SEPARAR MOVIMIENTOS TRANSITO SEGUN NEGOCIO 
		List<GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar> movimientos = new ArrayList<GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar>();
		List<ObjMovimientosAActualizar> transitosSinOperacionMigrada = new ArrayList<>();
				
		//negocios
		IngresoRetiroTitulosJava.IngresoRetiroTitulosEntrada IngresoRetiroTitulos = new IngresoRetiroTitulosJava.IngresoRetiroTitulosEntrada();
		IngresarGarantiaJava.IngresarGarantiaEntrada IngresoGarantia = new IngresarGarantiaJava.IngresarGarantiaEntrada();
		RetirarGarantiaJava.RetirarGarantiaEntrada RetiroGarantia = new RetirarGarantiaJava.RetirarGarantiaEntrada();
		ProcesarFacturacionCustodia_Entrada procesarFacturacionCustodiaEntrada = new ProcesarFacturacionCustodia_Entrada();
		CrearOperacionMesaCustodia.CrearOperacionMesaCustodia_IN CrearOperacionMesaRF = new CrearOperacionMesaCustodia.CrearOperacionMesaCustodia_IN();
		ReversarFacturacionCustodia.ReversarFacturacionCustodia_Entrada reversarFacturacionCustodiaEntrada = new ReversarFacturacionCustodia.ReversarFacturacionCustodia_Entrada();

		for (ObjMovimientos mov: in.P_MOVIMIENTOSCERTIFICAR){
			InformacionConfiguracionCustodia.ObjConfiguracionMovimientoCustodia ConfiguracionCustodia = (InformacionConfiguracionCustodia.ObjConfiguracionMovimientoCustodia) ConfigCUS.get(mov.IdTipoOperacion);
            
			ComprobanteFacturacion infoCmbteFac = (ComprobanteFacturacion) hashCmbteFac.get(mov.IdMovimientoCarteraEnTransito);
			
			if (ConfiguracionCustodia.OperacionMigrada.equalsIgnoreCase("S")){
				//Negocios migrados
				switch (ConfiguracionCustodia.CertificaEn) {
					case "IngresoRetiroTitulos": 
						if (infoCmbteFac != null) {
							if (("N").equalsIgnoreCase(infoCmbteFac.EsVentaDescubierta)) {
								IngresoRetiroTitulos.p_MovimientosEnTransito.add(mov.IdMovimientoCarteraEnTransito);								
							}else {
	   							     if (!infoCmbteFac.IdEstadoComprobante.equalsIgnoreCase("ENV")) { 
								       HashMap<String,String> valores = new HashMap<String,String>();
								       valores.put("IdMovimientoCarteraEnTransito", mov.IdMovimientoCarteraEnTransito.toString());
								       out.P_OUT_MENSAJES.add(new MensajeSalida(mov.IdMovimientoCarteraEnTransito,MensajeSalida.MENSAJE_ERROR,"UI_COMPROBANTE_FACTURACION_NO_ENVIADO_DTE",valores,con,in.p_IdEmpresa));	
	   							     } else {
	   							    	       IngresoRetiroTitulos.p_MovimientosEnTransito.add(mov.IdMovimientoCarteraEnTransito);
	   							            }
							      }
						}else {
							     IngresoRetiroTitulos.p_MovimientosEnTransito.add(mov.IdMovimientoCarteraEnTransito);
  						      }					
						
						break;
					case "IngresoGarantia":
						IngresoGarantia.p_MovimientosEnTransito.add(mov.IdMovimientoCarteraEnTransito); 
						break;
					case "RetiroGarantia":
						RetiroGarantia.p_MovimientosEnTransito.add(mov.IdMovimientoCarteraEnTransito);
						RetiroGarantia.p_IdTipoOperacion = mov.IdTipoOperacion;
						break;
					case "Facturacion":
						procesarFacturacionCustodiaEntrada.p_MovimientosEnTransito.add(mov.IdMovimientoCarteraEnTransito); 
						break;

					case "CrearOperacionMesaCustodia":
						CrearOperacionMesaRF.p_MovimientoEnTransito.add(mov.IdMovimientoCarteraEnTransito); 
						break;
					case "ReversarFacturacionCustodia":
						reversarFacturacionCustodiaEntrada.p_MovimientosEnTransito.add(mov.IdMovimientoCarteraEnTransito); 
						break;
					default : throw new Exception("Operacion migrada, pero no indica donde certificar");
				};
				
				GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar transito = new GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar();
				transito.IdMovimientoCarteraEnTransito = mov.IdMovimientoCarteraEnTransito;
				transito.EsCertificacion = "S";
				transito.IdUsuario = in.p_IdUsuario;
				movimientos.add(transito);
				
			} else {
				
				GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar MOV = new GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar();
				MOV.IdMovimientoCarteraEnTransito = mov.IdMovimientoCarteraEnTransito;
				MOV.EsCertificacion = "S";
				MOV.IdUsuario = in.p_IdUsuario;
				transitosSinOperacionMigrada.add(MOV);
				
			};
		};
		
		
		//03 - PROCESAR MOVIMIENTO EN TRANSITO (negocios migrados)
		if (movimientos.size()>0){
			
			if (IngresoRetiroTitulos.p_MovimientosEnTransito.size() > 0){
				IngresoRetiroTitulos.p_IdEmpresa		= in.p_IdEmpresa;
				IngresoRetiroTitulos.p_IdUsuario		= in.p_IdUsuario;
				IngresoRetiroTitulos.p_IdModulo			= in.p_IdModulo;
				IngresoRetiroTitulos.p_simularOperacion	= "N";
				IngresoRetiroTitulos.p_UsaReglasNegocio	= in.p_UsaReglasNegocio;
				IngresoRetiroTitulos.p_Certifica 		= "S";
				
				ValidaCantidadRetiroCustodia validarRetiros = new ValidaCantidadRetiroCustodia();
				EntradaValidacionCustodia entradaValidarRetiros = new EntradaValidacionCustodia();
				entradaValidarRetiros.p_IdEmpresa = in.p_IdEmpresa;
				entradaValidarRetiros.p_IdUsuario = in.p_IdUsuario;
				entradaValidarRetiros.p_IdModulo = in.p_IdModulo;
				entradaValidarRetiros.p_IdCanalIngreso = "OPT";
				entradaValidarRetiros.p_MovimientosEnTransito = IngresoRetiroTitulos.p_MovimientosEnTransito;
				
				ValidaCantidadRetiroCustodia.SalidaValidacionCustodia retirosValidados = validarRetiros.logica(con, entradaValidarRetiros);
				boolean movimientoAutorizacion=false;
				HashMap<Long,Long> movimientosNoValidos = new HashMap <Long, Long>();
				
				for (MensajeSalida mensajeValidado : retirosValidados.P_OUT_MENSAJES) {
					if(mensajeValidado.tipo.equals("ERROR")) {
						movimientosNoValidos.put(mensajeValidado.id, mensajeValidado.id);
						out.P_OUT_MENSAJES.add(mensajeValidado);
						out.P_OUT_ERRORESCONTADOR = 1L;
						
					}else if(mensajeValidado.tipo.equals("AUTORIZACION")) {
						movimientoAutorizacion=true;
					}
				}
				if(!lstMvtsEnTransito.isEmpty() && movimientoAutorizacion) {
					
					List<Long> movimientosentransito = new ArrayList<Long>();
					ObjUpdateEstado UpdateEst = new ObjUpdateEstado();
					List<ObjUpdateEstado> list = new ArrayList<ObjUpdateEstado>();
					for(ObjMovimientosProcesados mov: lstMvtsEnTransito) {
						if(mov.IdEstado.equals("PEN")) {
							UpdateEst.IdMovimientoCarteraEnTransito=mov.IdMovimientoCarteraEnTransito;
							movimientosentransito.add(mov.IdMovimientoCarteraEnTransito);
							IngresoRetiroTitulos.p_ExisteMovEnTransito=true;
							list.add(UpdateEst);
						}
					}
					if(IngresoRetiroTitulos.p_ExisteMovEnTransito) {
						
					HashMap<String,String> paramsInst = new HashMap<>();
					paramsInst.put("idsMovimientoCarteraEnTransito",movimientosentransito.toString());
					List<cl.bcs.custodia.IngresoRetiroTitulosJava.IngresoRetiroTitulosEntrada.ObjInstrumento> datosInstrumentos = _sql.query(con, SQL_SELECT_INSTRUMENTOS, params , cl.bcs.custodia.IngresoRetiroTitulosJava.IngresoRetiroTitulosEntrada.ObjInstrumento.class);	
					
					_sql.batch(con, SQL_UPDATE_ESTADO,list, ObjUpdateEstado.class);
					 IngresoRetiroTitulos.P_INSTRUMENTOS=datosInstrumentos;
					 IngresoRetiroTitulos.p_Certifica = "N";
					}
				}
				if(!movimientosNoValidos.isEmpty()) {
					IngresoRetiroTitulos.p_MovimientosEnTransito.removeIf(n -> (movimientosNoValidos.containsKey(n)) );
				}
				
				IngresoRetiroTitulosJava IngRetTitulos = new IngresoRetiroTitulosJava();
				IngresoRetiroTitulosJava.IngresoRetiroTitulosSalida resultIngRetTitulos = IngRetTitulos.logica(con, IngresoRetiroTitulos);
			
				for (MensajeSalida result : resultIngRetTitulos.P_OUT_MENSAJES){
					out.P_OUT_MENSAJES.add(result);
				};
				for (IngresoRetiroTitulosJava.IngresoRetiroTitulosSalida.ObjResultado result : resultIngRetTitulos.P_OUT_RESULTADO){
					CertificarMovimientosEnTransitoSalida.ObjMovimientosProcesados a = new CertificarMovimientosEnTransitoSalida.ObjMovimientosProcesados();  
					a.IdMovimientoCarteraEnTransito = result.IdGenerarMovimientoCustodia;
					out.P_OUT_MOVIMIENTOSPROCESADOS.add(a);
				};
				if ( resultIngRetTitulos.P_OUT_ERRORESCONTADOR.equals(1L) )
					out.P_OUT_ERRORESCONTADOR = 1L;
				
			};
			
			if (procesarFacturacionCustodiaEntrada.p_MovimientosEnTransito.size() > 0){
				procesarFacturacionCustodiaEntrada.p_IdEmpresa		= in.p_IdEmpresa;
				procesarFacturacionCustodiaEntrada.p_IdUsuario		= in.p_IdUsuario;
				procesarFacturacionCustodiaEntrada.p_IdModulo			= in.p_IdModulo;
				procesarFacturacionCustodiaEntrada.p_simularOperacion	= "N";
				procesarFacturacionCustodiaEntrada.p_UsaReglasNegocio	= "S";
				
				ProcesarFacturacionCustodia procesarFacturacionService = new ProcesarFacturacionCustodia();
				ProcesarFacturacionCustodia_Salida resultProcesarFacturacionCustodia = procesarFacturacionService.logica(con, procesarFacturacionCustodiaEntrada);
				
				out.P_OUT_MENSAJES.addAll(resultProcesarFacturacionCustodia.P_OUT_MENSAJES);
				
				for (MovimientoCustodiaSalida mov : resultProcesarFacturacionCustodia.P_OUT_MOVIMIENTOS) {
					CertificarMovimientosEnTransitoSalida.ObjMovimientosProcesados a = new CertificarMovimientosEnTransitoSalida.ObjMovimientosProcesados();  
					a.IdMovimientoCarteraEnTransito = mov.IdMovimiento;
					out.P_OUT_MOVIMIENTOSPROCESADOS.add(a);
				}
			};	
			
			if (reversarFacturacionCustodiaEntrada.p_MovimientosEnTransito.size() > 0){
				reversarFacturacionCustodiaEntrada.p_IdEmpresa		= in.p_IdEmpresa;
				reversarFacturacionCustodiaEntrada.p_IdUsuario		= in.p_IdUsuario;
				reversarFacturacionCustodiaEntrada.p_IdModulo			= in.p_IdModulo;
				reversarFacturacionCustodiaEntrada.p_simularOperacion	= "N";
				
				ReversarFacturacionCustodia procesarReversa = new ReversarFacturacionCustodia();
				ReversarFacturacionCustodia.ReversarFacturacionCustodia_Salida resultReversa = procesarReversa.logica(con, reversarFacturacionCustodiaEntrada);
				
				out.P_OUT_MENSAJES.addAll(resultReversa.P_OUT_MENSAJES);
				
				for (MovimientoCustodiaSalida mov : resultReversa.P_OUT_MOVIMIENTOS) {
					CertificarMovimientosEnTransitoSalida.ObjMovimientosProcesados a = new CertificarMovimientosEnTransitoSalida.ObjMovimientosProcesados();  
					a.IdMovimientoCarteraEnTransito = mov.IdMovimiento;
					out.P_OUT_MOVIMIENTOSPROCESADOS.add(a);
				}
			};	
			
			if (IngresoGarantia.p_MovimientosEnTransito.size() > 0){
				IngresoGarantia.p_IdEmpresa			= in.p_IdEmpresa;
				IngresoGarantia.p_IdUsuario			= in.p_IdUsuario;
				IngresoGarantia.p_IdModulo			= in.p_IdModulo;
				IngresoGarantia.p_UsaReglasNegocio	= "S";
				IngresoGarantia.p_SimulaOperacion	= "N";
				IngresoGarantia.p_Certifica 		= "S";
				IngresoGarantia.p_FechaMovimiento	= in.p_FechaOperacion;
				
				IngresarGarantiaJava IngGarantia = new IngresarGarantiaJava();
				IngresarGarantiaJava.IngresarGarantiaSalida resultIngGarantia = IngGarantia.logica(con, IngresoGarantia);
				
				for (MensajeSalida result : resultIngGarantia.P_OUT_MENSAJES){
					out.P_OUT_MENSAJES.add(result);
				};
				for (IngresarGarantiaJava.IngresarGarantiaSalida.ObjResultado result : resultIngGarantia.P_OUT_RESULTADOPROCESO){
					CertificarMovimientosEnTransitoSalida.ObjMovimientosProcesados a = new CertificarMovimientosEnTransitoSalida.ObjMovimientosProcesados();  
					a.IdMovimientoCarteraEnTransito = result.IdGenerarMovimientoCustodia;
					out.P_OUT_MOVIMIENTOSPROCESADOS.add(a);
				};
				if ( resultIngGarantia.P_OUT_ERRORESCONTADOR.equals(1L) )
					out.P_OUT_ERRORESCONTADOR = 1L;
			};
			
			if (RetiroGarantia.p_MovimientosEnTransito.size() > 0){
				RetiroGarantia.p_IdEmpresa			= in.p_IdEmpresa;
				RetiroGarantia.p_IdUsuario			= in.p_IdUsuario;
				RetiroGarantia.p_IdModulo			= in.p_IdModulo;
				RetiroGarantia.p_UsaReglasNegocio	= "S";
				RetiroGarantia.p_SimulaOperacion	= "N";	
				RetiroGarantia.p_FechaMovimiento	= in.p_FechaOperacion;
//				RetiroGarantia.p_Certifica 			= "N";
				
				RetirarGarantiaJava RetGarantia = new RetirarGarantiaJava();
				RetirarGarantiaJava.RetirarGarantiaSalida resultRetGarantia = RetGarantia.logica(con, RetiroGarantia);
				
				for (MensajeSalida result : resultRetGarantia.P_OUT_MENSAJES){
					out.P_OUT_MENSAJES.add(result);
				};
				for (RetirarGarantiaJava.RetirarGarantiaSalida.ObjResultado result : resultRetGarantia.P_OUT_RESULTADOPROCESO){
					CertificarMovimientosEnTransitoSalida.ObjMovimientosProcesados a = new CertificarMovimientosEnTransitoSalida.ObjMovimientosProcesados();  
					a.IdMovimientoCarteraEnTransito = result.IdGenerarMovimientoCustodia;
					out.P_OUT_MOVIMIENTOSPROCESADOS.add(a);
				};
				if ( resultRetGarantia.P_OUT_ERRORESCONTADOR.equals(1L) )
					out.P_OUT_ERRORESCONTADOR = 1L;
			};
			
			if (CrearOperacionMesaRF.p_MovimientoEnTransito.size() > 0){
				CrearOperacionMesaRF.p_IdEmpresa			= in.p_IdEmpresa;
				CrearOperacionMesaRF.p_IdUsuario			= in.p_IdUsuario;
				CrearOperacionMesaRF.p_IdModulo				= in.p_IdModulo;
				CrearOperacionMesaRF.p_UsaReglasNegocio		= "S";
				//CrearOperacionMesaRF.p_SimulaOperacion	= "N";
				//CrearOperacionMesaRF.p_FechaMovimiento	= in.p_FechaOperacion;
				
				CrearOperacionMesaCustodia operacionMesaRF = new CrearOperacionMesaCustodia();
				CrearOperacionMesaCustodia.CrearOperacionMesaCustodia_OUT resultOperacionMesaRF = operacionMesaRF.logica(con, CrearOperacionMesaRF);
				
				for (MensajeSalida result : resultOperacionMesaRF.P_OUT_MENSAJES){
					out.P_OUT_MENSAJES.add(result);
				};
				//LEER LOS MOV EN TRANSITO PROCESADOS y MOSTRAR
				if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0){
					out.P_OUT_ERRORESCONTADOR = 1L;
			        return out;
			     }
			};
		};
		
		
		//04 - PROCESAR MOVIMIENTO EN TRANSITO (negocios no migrados)
		if (transitosSinOperacionMigrada.size() > 0) {
			
			GestionMovimientoCarteraEnTransito.ActualizarMovimientoEnTransito(con, transitosSinOperacionMigrada);
			HashMap<String,String> valores = new HashMap<String,String>();
			
			for (ObjMovimientosAActualizar mov: transitosSinOperacionMigrada) {
				valores.put("IdMovimientoEnTransito", mov.IdMovimientoCarteraEnTransito.toString() );
				out.P_OUT_MENSAJES.add(new MensajeSalida(mov.IdMovimientoCarteraEnTransito,MensajeSalida.MENSAJE_INFORMACION,
						"MOVIMIENTO_EN_TRANSITO_CERTIFICADO_OK",valores,con,in.p_IdEmpresa));
				valores.clear();
			}
		};
		
		//notificacion		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");

		//Agrega notificacion
		AgregarEvento.ServiceEntrada notifIn = new AgregarEvento.ServiceEntrada();
		notifIn.P_IDEMPRESA = in.p_IdEmpresa;
		notifIn.P_IDUSUARIO = in.p_IdUsuario;
		notifIn.P_IDMODULO = in.p_IdModulo;

		List<ObjEVENTOS> eventosNotificacion = new ArrayList<>();
		List<ObjIDDIRECCIONES> direccionesNotificacion = new ArrayList<>();
		List<CertificarMovimientos_Notificacion> ntfc = new ArrayList<>();
		List<Long> idsMovs = new ArrayList<>();
		
        for (ObjMovimientosProcesados movs: out.P_OUT_MOVIMIENTOSPROCESADOS)
		{
        	if (!idsMovs.contains(movs.IdMovimientoCarteraEnTransito)) {
        		idsMovs.add(movs.IdMovimientoCarteraEnTransito);
        	}
		}
        
        if (idsMovs.size() > 0)
        {
        	params.clear();
        	
		    params.put("IdUsuario", in.p_IdUsuario);
		    params.put("idsMovs", idsMovs);
		    params.put("IdEmpresa", in.p_IdEmpresa);
		    ntfc = _sql.query(con, SQL_SELECT_CertificarMovimientos_SelectNotificacion, params, CertificarMovimientos_Notificacion.class);
		    
	        String rutaLogoCorredora = "";
	        ConsultarImagenEntrada imagenCorredoraIn = new ConsultarImagenEntrada();
	        imagenCorredoraIn.p_IdEmpresa = in.p_IdEmpresa;
	        imagenCorredoraIn.p_imagenNombres.add("LOGO_CORREDOR");
	        ConsultarImagenSalida imagenCorredoraOut = new ConsultarImagen().logica(con, imagenCorredoraIn);
	        if(imagenCorredoraOut.OUT_IMAGENES.size() > 0) {
	        	rutaLogoCorredora = imagenCorredoraOut.OUT_IMAGENES.get(0).RutaPublica;        	
	        }
		    
		    for (CertificarMovimientos_Notificacion movs: ntfc){
				
				ObjEVENTOS evento = new ObjEVENTOS();
				evento.CodigoTipoEvento = "MOV_TITULOS";
				evento.FechaCreacionSistema = fechaSistema;
				evento.IdPersonaDestinatario = movs.IdPersonaCLI;
				evento.IdPersonaRemitente = movs.IdPersona;
				evento.IdPortafolio = 0L;
				evento.IdSecuencia = movs.IdMovimientoCarteraEnTransito;
				eventosNotificacion.add(evento);
				
				Resultado montoCompra = MonedaFormato.formatear(con, movs.MontoCompra, MonedaFormato.MONTO, movs.TipoMoneda, null, null);
				Resultado cantidadCompra = MonedaFormato.formatear(con,  movs.Cantidad, MonedaFormato.CANTIDAD, movs.TipoMoneda, null, null);
				Resultado precioCompra = MonedaFormato.formatear(con, movs.PrecioCompra, MonedaFormato.PRECIO, movs.TipoMoneda, null, null);
				
			
				// Valores generales				
				notifIn.addValor(movs.IdMovimientoCarteraEnTransito, "IDINTERNO", movs.IdMovimientoCarteraEnTransito+"")
					   .addValor(movs.IdMovimientoCarteraEnTransito, "FECHA", Fecha.toFormat(Fecha.toDate(fechaSistema),"dd-MM-yyyy"))
					   .addValor(movs.IdMovimientoCarteraEnTransito, "PORTAFOLIO", movs.CodigoPortafolio)
					   .addValor(movs.IdMovimientoCarteraEnTransito, "OPERACION", movs.NombreOperacion)
					   .addValor(movs.IdMovimientoCarteraEnTransito, "CANTIDADCOMPRA",cantidadCompra.valorConFormato)
					   .addValor(movs.IdMovimientoCarteraEnTransito, "MONTOCOMPRA", montoCompra.valorConFormato)
					   .addValor(movs.IdMovimientoCarteraEnTransito, "PRECIOCOMPRA", precioCompra.valorConFormato)
					   .addValor(movs.IdMovimientoCarteraEnTransito, "DOCUMENTOIDENTIDAD", movs.DI)
					   .addValor(movs.IdMovimientoCarteraEnTransito, "TIPODOCUMENTOIDENTIDAD", movs.TipoDoc)
					   .addValor(movs.IdMovimientoCarteraEnTransito, "TELEFONO", movs.NumeroTel)
					   .addValor(movs.IdMovimientoCarteraEnTransito, "CORREDORA", movs.Corredora)
					   .addValor(movs.IdMovimientoCarteraEnTransito, "ATENCIONCLIENTE", movs.AtencionCliente)
					   .addValor(movs.IdMovimientoCarteraEnTransito, "CORREO", movs.Correo)
					   .addValor(movs.IdMovimientoCarteraEnTransito, "MONEDA", movs.TipoMoneda)
					   .addValor(movs.IdMovimientoCarteraEnTransito, "CLIENTE",movs.NombreCompleto)
					   .addValor(movs.IdMovimientoCarteraEnTransito, "NEMO",movs.Instrumento)
					   .addValor(movs.IdMovimientoCarteraEnTransito, "AGENTE", movs.Agente)
					   .addValor(movs.IdMovimientoCarteraEnTransito, "LOGO_CORREDORA", rutaLogoCorredora);

				
				
				ObjIDDIRECCIONES dir1 = new ObjIDDIRECCIONES();
				dir1.IdDireccion = movs.IdDireccionElectronicaPrincipal;
				
				dir1.IdSecuencia = movs.IdMovimientoCarteraEnTransito;
				dir1.TipoDestinatario = "CC";
				
				
				ObjIDDIRECCIONES dir2 = new ObjIDDIRECCIONES();
				dir2.IdDireccion = movs.IdDireccionElectronicaPrincipalCLI;
				dir2.IdSecuencia = movs.IdMovimientoCarteraEnTransito;
				dir2.TipoDestinatario = "TO";
				
				direccionesNotificacion.add(dir1);
				direccionesNotificacion.add(dir2);
			}

			notifIn.P_EVENTOS = eventosNotificacion;
			notifIn.P_IDDIRECCIONES = direccionesNotificacion;


			if (notifIn.P_EVENTOS.size() > 0) {
			    AgregarEvento notif = new AgregarEvento();
			    AgregarEvento.ServiceSalida notifOut = notif.logica(con, notifIn);
			}
		}
        
        Set<Long> idsCertificados = new HashSet<>();
		if(out.P_OUT_MOVIMIENTOSPROCESADOS.size() > 0) {
			agregarLog(con, in, out.P_OUT_MOVIMIENTOSPROCESADOS);
			idsCertificados = out.P_OUT_MOVIMIENTOSPROCESADOS.stream().map(t -> Long.valueOf(t.IdMovimientoCarteraEnTransito)).collect(Collectors.toSet());
		}
		if(movimientos.size() > 0 && out.P_OUT_ERRORESCONTADOR == 0) {
			List<ObjMovimientosProcesados>	procesados = new ArrayList<ObjMovimientosProcesados>();
			for (GestionMovimientoCarteraEnTransito.ObjMovimientosAActualizar mov : movimientos){
				if (!idsCertificados.contains(mov.IdMovimientoCarteraEnTransito)) {
					CertificarMovimientosEnTransitoSalida.ObjMovimientosProcesados a = new CertificarMovimientosEnTransitoSalida.ObjMovimientosProcesados();  
					a.IdMovimientoCarteraEnTransito = mov.IdMovimientoCarteraEnTransito;
					procesados.add(a);
				}
			};
			agregarLog(con, in, procesados);
		}
		
		logger.debug("Fin:" + out.toString());
		return out;
	}

	private void validarMovimientoGarantia(ObjMovimientosProcesados mov,HashMap<Long, ObjMovimientos> hashmovientosGarantia, CertificarMovimientosEnTransitoSalida out) {
		logger.trace("INICIO::CertificarMovimientosEnTransito.validarMovimientoGarantia");
		long inicioMetodo = System.currentTimeMillis();		
		
		if(!hashmovientosGarantia.isEmpty()) {
			ObjMovimientos movimientoGarantia = hashmovientosGarantia.get(mov.IdMovimientoCarteraEnTransito);
			
			if ("VCDTIT".equals(mov.IdConcepto)  && "RETTIT".equals(mov.IdTipoOperacion) && "S".equals(movimientoGarantia.MovimientoGarantia) && "CUS".equals(movimientoGarantia.Origen)) {										
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, "MOVIMIENTO_ASOCIADO_GARANTIA","Movimiento asociado a una garantia"));
					
			}else if ( "VCDTIT".equals(mov.IdConcepto)  && "RETTIT".equals(mov.IdTipoOperacion) && "S".equals(movimientoGarantia) && "CUS".equals(movimientoGarantia.Origen)) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, "MOVIMIENTO_ASOCIADO_GARANTIA","Movimiento asociado a una garantia"));
			}
		}

		
		
		long finMetodo = System.currentTimeMillis();
		logger.trace("FIN::CertificarMovimientosEnTransito.validarMovimientoGarantia :" , finMetodo-inicioMetodo);		
	}
	
	private void agregarLog(Connection con, CertificarMovimientosEnTransitoEntrada in, List<ObjMovimientosProcesados> listaMovimientosProcesados) {
		try {
			logger.debug("INICIO::CertificarMovimientosEnTransito.agregarLog");
			long inicio = System.currentTimeMillis();
			
			SQL _sql = new SQL();
			String FechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
			List<AgregarLog.InAgregarLog.Log> listaLog = new ArrayList<>();
			List<Long> idsMovimientosCarteraEnTransito = new ArrayList<Long>();
			
			for (ObjMovimientosProcesados movimiento : listaMovimientosProcesados) {
				idsMovimientosCarteraEnTransito.add(movimiento.IdMovimientoCarteraEnTransito);
			}
			
			HashMap<String, Object> params = new HashMap<>();
			params.put("p_IdMovimientoCarteraEnTransito", idsMovimientosCarteraEnTransito);
			List<MovimientoEnTransito> listaDatosMovimiento = _sql.query(con, SQL_SELECT_MOVIMIENTO_EN_TRANSITO, params, MovimientoEnTransito.class);
			HashMap<Long, Object> hDatosMovimiento = Utils.List2Hash(listaDatosMovimiento, "IdMovimientoCarteraEnTransito");
			
			for (ObjMovimientosProcesados movimiento : listaMovimientosProcesados) {
				//CABECERA
				AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
				MovimientoEnTransito datosMovimiento = (MovimientoEnTransito) hDatosMovimiento.get(movimiento.IdMovimientoCarteraEnTransito);
				
				log.IdTrabajo = 1L; 
				log.IdCabeceraLog = datosMovimiento.IdMovimientoCarteraEnTransito; //
				log.IdTipoLog = "CERTIFICACION";
				log.IdModulo = in.p_IdModulo;
				log.IdUsuario = in.p_IdUsuario;
				log.IdEmpresa = in.p_IdEmpresa;
				log.FechaSistemaLog = FechaSistema;
				log.IdUsuarioAtendido = in.p_IdUsuario;
				log.IdCliente = datosMovimiento.IdCliente; //
				log.EstadoCliente = "";
				log.NombreCliente = "";
				log.IdTipoDocumentoIdentidad = "";
				log.IdDocumentoIdentidad = "";
				log.IdPortafolio = datosMovimiento.IdPortafolio;
				log.EstadoPortafolio = "";
				log.IdTipoRegistro = "CERTIFICARMOVIMIENTOENTRANSITO";
				log.IdRegistro = datosMovimiento.IdMovimientoCarteraEnTransito.toString();
				log.EstadoRegistro = datosMovimiento.IdEstado;
				log.IdTipoOperacion = datosMovimiento.IdTipoOperacion;
				log.Folio = 0L;
				log.Cantidad = datosMovimiento.Cantidad;
				log.PrecioTasa = BigDecimal.ZERO;
				log.Monto = BigDecimal.ZERO;
				log.IdMonedaOrigen = "";
				log.IdMonedaLiquidacion = "";
				log.TipoCambio = "";
				log.Nemo = datosMovimiento.Nemotecnico; //
				log.IdProceso = 0L;
				log.EstadoProceso = "";
				log.NombreProceso = "cl.bcs.custodia.CertificarMovimientosEnTransito";
						
				listaLog.add(log);
			}
			
			//REGISTRO
			AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
			entradaAgregarLog.p_IdTrabajo = 1L;		
			entradaAgregarLog.p_Log = listaLog;	
			AgregarLog agregarLogService = new AgregarLog();
			agregarLogService.logica(con, entradaAgregarLog);
				
			long fin = System.currentTimeMillis();
			logger.debug("FIN::CertificarMovimientosEnTransito.agregarLog:" , fin - inicio);
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();		
		CertificarMovimientosEnTransitoEntrada jin=gson.fromJson(in.toString() ,CertificarMovimientosEnTransitoEntrada.class);
		CertificarMovimientosEnTransitoSalida out=logica(conn,jin);		
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	public static void main(String[] args) throws Exception {
		//try (Connection con = PlataformaUtils.getQAPEConnection()) {
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
		//try (Connection con = PlataformaUtils.getDESAPEConnection()) {
			try {
				Gson gson = Utils.getGsonBuilder();
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"CUS\",\"P_MOVIMIENTOSCERTIFICAR\":[{\"IdMovimientoCarteraEnTransito\":4971338,\"IdTipoOperacion\":\"INGTIT\",\"MovimientoGarantia\":\"N\"}],\"p_FechaOperacion\":\"2024-07-11\"}";
				CertificarMovimientosEnTransitoEntrada Objin = gson.fromJson(json, CertificarMovimientosEnTransitoEntrada.class);
								
				CertificarMovimientosEnTransito  kk = new CertificarMovimientosEnTransito();
				CertificarMovimientosEnTransitoSalida asdf = kk.logica(con, Objin);
				con.rollback();
				//con.commit();
				
		    	}catch(Exception er) {
					er.printStackTrace();	
					con.rollback();
				}
		} catch (Exception e) {
			e.printStackTrace();			
		}
	};
}
