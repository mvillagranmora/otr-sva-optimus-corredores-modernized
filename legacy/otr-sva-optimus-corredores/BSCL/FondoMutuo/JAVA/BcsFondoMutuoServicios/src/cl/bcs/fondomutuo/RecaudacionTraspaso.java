package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.cuentaInversion.CrearCargoAbono;
import cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoEntrada;
import cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoSalida;
import cl.bcs.cuentaInversion.CrearCargoAbono.ObjCargoAbonoAGenerar;
import cl.bcs.cuentaInversion.CrearCargoAbono.ObjCargoAbonoGenerado;
import cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal.EntradaCrearSolicitudAporteRescateNormal;
import cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal.SalidaCrearSolicitudAporteRescateNormal;
import cl.bcs.fondomutuo.ProcesarIngresoEgresoTesoreria.Empleado;
import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.fondomutuo.pojo.ResultadoSolicitud;
import cl.bcs.fondomutuo.pojo.Traspaso.InMovimientoGenerado;
import cl.bcs.fondomutuo.pojo.Traspaso.RegistroRecaudacion;
import cl.bcs.fondomutuo.pojo.Traspaso.RegistroTraspaso;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.tesoreria.CrearComprobante;
import cl.bcs.tesoreria.CrearIngresoEgreso;
import cl.bcs.tesoreria.LiquidarComprobante;
import cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Entrada;
import cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Salida;
import cl.bcs.tesoreria.CrearIngresoEgreso.FormaDePago_Entrada;
import cl.bcs.tesoreria.CrearIngresoEgreso.IngresoEgresoGenerado_Salida;
import cl.bcs.tesoreria.CrearIngresoEgreso.IngresoEgreso_Entrada;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;



public class RecaudacionTraspaso implements JavaCall   {

	private final Log log = new Log("FM.RecaudacionTraspaso");
	
	public String SQL_TRASPASO_SELECT					= loadResourceToString("cl/bcs/fondomutuo/sql/Recaudacion_SelectTraspaso.sql");
		
	public String SQL_TRASPASO_DETALLE_DESTINO_SELECT 	= loadResourceToString("cl/bcs/fondomutuo/sql/Recaudacion_SelectTraspasoDestino.sql");
	
	public String SQL_TRASPASO_DETALLE_DESTINO_UPDATE	= loadResourceToString("cl/bcs/fondomutuo/sql/Recaudacion_UpdateTraspasoDestino.sql");
	
	public String SQL_TRASPASO_UPDATE	 				= loadResourceToString("cl/bcs/fondomutuo/sql/Recaudacion_UpdateTraspaso.sql");
	
	
	public String SQL_RECAUDACION_INSERT			 		= loadResourceToString("cl/bcs/fondomutuo/sql/Recaudacion_InsertRecaudacion.sql");
	
	public String SQL_RECAUDACION_INSERTMOVIMIENTO			= loadResourceToString("cl/bcs/fondomutuo/sql/Recaudacion_InsertRecaudacionMovimiento.sql");
	
	private  String SQL_CtaBancaria 						= loadResourceToString("cl/bcs/fondomutuo/sql/CuentaBancariaTraspaso.sql");
		
	private final String SQL_DatosCorredor = "SELECT \"IdPersona\",\"IdCliente\",\"IdPortafolio\" FROM \"BSCL.Portafolio.BD.Vistas::ConsultarPortafoliosCuentaPropia\""
			+ "where \"IdEmpresa\" = :IdEmpresa AND \"EsPortafolioDefecto\"='S'";
	
	private final String SQL_SELECT_Empleado = "SELECT COALESCE(\"IdEntidad\",0) AS \"IdEmpleado\" FROM \"BSCL.Plataforma.BD::ACC.Usuario\" WHERE \"TipoEntidad\" = 'EMP' AND \"IdUsuario\" = :IdUsuario";

	private final String SQL_CtaDistribucion = "SELECT \"DestinoCtaBco\" FROM \"BSCL.APV.BD::APV.OrigenDestinoTransicion\" WHERE \"Operacion\"='DIS'AND \"IdEmpresa\"=:IdEmpresa AND \"IdEstado\"='ACT'";
	
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	public static class InRecaudacion{
		
		public Long 		IdTraspaso;
		public String 		FechaDeposito;
		public String 		IdMedioDePago;
		public Long 		IdBanco;
		public Long 		NumeroCuentaBancaria;
		public Long 		IdCuentaBanco;
		
		public String 		Sucursal;
		public BigDecimal 	MontoDeposito;
		public String 		Observacion;
		public String 		IdRegimenTributario="";	
		
		private String  EstadoProceso ="";
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	};
	

	
	public static class DetalleTraspasoDestino{
	
		public Long   	IdTraspaso;
		public String 	IdSerieFondo;
		public BigDecimal 	Cantidad;
		public String 	IdEstado;
		public Date 	FechaIngreso;
		public Date 	FechaModificacion;
		
		public String	IdTipoMovimientoTraspaso;
		public String 	Glosa;
		
		public Long   	IdCliente;
		public Long   	IdPortafolio;
		public Long 	IdSolicitudAporteRescate;
		public String	Certifica="";
		public String	EstadoProceso="";
		public String	Codigo="";
		
		public Date 	FechaAceptacion;
		
		public String	IdRegimenFondoMutuo;
		public String IndicadorMLT;
		public Long IdInterno;
		public Long IdDetalleTraspaso;
		public BigDecimal Monto;
		public BigDecimal Porcentaje;
		public String IdMonedaFondo;
	    public DetalleTraspasoDestino() {
	    	IdInterno = 0L;
	    	IdTraspaso = 0L;
	    	IdDetalleTraspaso = 0L;
	    	IdMonedaFondo = "CLP";
	    }
	    
	    public String toString() {
			return Utils.printJson(this, false);
		}
	    
	    
	};
	
	public static class DatosCorredor{
		
		public Long IdPersona;
		public Long IdCliente;
		public Long IdPortafolio;
	}
	
	public static class CtaDistribucion{
		public Long		DestinoCtaBco;
		public String   NumeroCuentaBancariaSocio;
		public String   IdTipoCuentaBancariaSocio;
		public Long     IdCuentaBancariaSocio;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	
	
	//  ENTRADA
	public static class EntradaRecaudacionTraspaso {
		
		public 	Long						p_IdEmpresa;
		public	Long						p_IdUsuario;
		public 	String						p_IdModulo;
		public	List<InRecaudacion>			p_Recaudacion;
		
		
		public EntradaRecaudacionTraspaso(){
			p_Recaudacion 					= new ArrayList<InRecaudacion>();
			
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	
	
	// SALIDA

	public static class SalidaRecaudacionTraspaso{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		public List <ResultadoSolicitud> P_OUT_RESULTADO;
		
		public SalidaRecaudacionTraspaso(){
			this.P_OUT_ERRORESCONTADOR=0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_RESULTADO = new ArrayList<ResultadoSolicitud>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaRecaudacionTraspaso jin = gson.fromJson(in.toString(), EntradaRecaudacionTraspaso.class);
		SalidaRecaudacionTraspaso out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaRecaudacionTraspaso logica(Connection con, EntradaRecaudacionTraspaso entrada) throws Exception{
	
		log.write("INICIO SalidaRecaudacionTraspaso");
		SalidaRecaudacionTraspaso out = new SalidaRecaudacionTraspaso();
		SQL sql=new SQL();
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		String monedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		
		HashMap<String,Object> ParamsDatosCorredor = new HashMap<String,Object>();
		ParamsDatosCorredor.put("IdEmpresa", entrada.p_IdEmpresa);
			
		List<DatosCorredor> _DatosCorredor = sql.query(con, SQL_DatosCorredor, ParamsDatosCorredor, DatosCorredor.class);
		
		Long IdEmpresa = entrada.p_IdEmpresa; 
		
		Long IdUsuario=entrada.p_IdUsuario;
		
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			return out;
		}
		
		
		if(entrada.p_Recaudacion.size() == 0){
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_DEPOSITOS_DE_TRASPASO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
			return out;
		}
		
		ArrayList<Long> correlativos = new ArrayList<Long>();
		for (InRecaudacion mov: entrada.p_Recaudacion) {
			if(mov.IdTraspaso.longValue() == 0L){
				out.P_OUT_ERRORESCONTADOR = 1L;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ID_INTERNO_NO_CERO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				return out;
			}
			if (correlativos.contains(mov.IdTraspaso)) {
				out.P_OUT_ERRORESCONTADOR = 1L;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"CORRELATIVO_INCORRECTO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				return out;
			} else {
				correlativos.add(mov.IdTraspaso);
			}
		}
		
		List<Long> idTraspaso = new ArrayList<Long>();
		for(InRecaudacion in : entrada.p_Recaudacion){
			if(!idTraspaso.contains(in.IdTraspaso)) idTraspaso.add(in.IdTraspaso);
		}
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdEmpresa", IdEmpresa);
		params.put("IdTraspaso", idTraspaso);
		
		List<RegistroTraspaso> traspasos = sql.query(con, SQL_TRASPASO_SELECT, params, RegistroTraspaso.class);
		List<DetalleTraspasoDestino> traspasosDetalleDestino = sql.query(con, SQL_TRASPASO_DETALLE_DESTINO_SELECT, params, DetalleTraspasoDestino.class);
		

		
		List<RegistroRecaudacion> solicitudProcesarFM = new ArrayList<RegistroRecaudacion>();
		List<InRecaudacion> solicitudInvalida = new ArrayList<InRecaudacion>();
		
	
		
		
		//validaciones del Traspaso
		for(InRecaudacion in : entrada.p_Recaudacion){
			in.EstadoProceso = _c.EstadoPendiente;
			
				//verificcion del estado 
				for(RegistroTraspaso solicitud : traspasos){
					if ((long)in.IdTraspaso== (long)solicitud.IdTraspaso)
					{
						if( !(_c.TraspasoEstadoIngresado.equalsIgnoreCase(solicitud.IdEstado))){
							in.EstadoProceso = _c.EstadoRechazado;
							out.P_OUT_ERRORESCONTADOR++;
							out.P_OUT_MENSAJES.add(new MensajeSalida(solicitud.IdTraspaso,MensajeSalida.MENSAJE_ERROR,"UI_ESTADO_TRASPASO_INVALIDO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
							continue;
						}
						
						
					}
				}
				
				int contDestino=0;
				for(DetalleTraspasoDestino DetalleOrigen : traspasosDetalleDestino){
					if ((long)DetalleOrigen.IdTraspaso==(long)in.IdTraspaso)
					{
						contDestino++;
					}
					
				}
			
				if(contDestino == 0){
					in.EstadoProceso = _c.EstadoRechazado;
					out.P_OUT_ERRORESCONTADOR++;
					out.P_OUT_MENSAJES.add(new MensajeSalida(in.IdTraspaso,MensajeSalida.MENSAJE_ERROR,"UI_TRASPASO_SIN_DESTINO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
					continue;
				}
				
				//registro recaudacion
				
				RegistroRecaudacion reg = new RegistroRecaudacion();
				reg.FechaDeposito=Fecha.toDate(in.FechaDeposito);
				reg.IdBanco=in.IdBanco;
				//reg.IdRegimenTributario=in.IdRegimenTributario;
				reg.IdMedioDePago=in.IdMedioDePago;
				reg.IdTraspaso=in.IdTraspaso;
				reg.MontoDeposito=in.MontoDeposito;
				reg.NumeroCuentaBancaria=in.NumeroCuentaBancaria;
				reg.IdCuentaBanco=in.IdCuentaBanco;
				
				reg.Observacion=in.Observacion;
				reg.Sucursal=in.Sucursal;
				reg.EstadoProceso=in.EstadoProceso;
				reg.IdEmpresa=IdEmpresa;
				reg.IdUsuario=IdUsuario;
				reg.IdEstado=_c.EstadoActivo;
				reg.FechaIngreso=Fecha.toDate(fechaSistema);
				reg.FechaAceptacion=Fecha.toDate(fechaSistema);
				
			
						for(RegistroTraspaso tra : traspasos){
							if ((long)in.IdTraspaso== (long)tra.IdTraspaso)
							{
								reg.IdCliente=tra.IdCliente;
								reg.IdPortafolio=tra.IdPortafolio;
								reg.IdPersonaEmisor=tra.IdPersonaEmisor;
								reg.IdPersonaEntidad = tra.IdPersonaEntidad;
								reg.IdPersonaCliente = tra.IdPersonaCliente;
								reg.FolioCSC = tra.FolioCSC;
							}
						}
						reg.IdMonedaFondo = traspasosDetalleDestino.stream().filter(detalleDestino -> in.IdTraspaso.equals(detalleDestino.IdTraspaso)).findFirst().orElse(new DetalleTraspasoDestino()).IdMonedaFondo;
				if (_c.EstadoRechazado.equalsIgnoreCase(in.EstadoProceso)){
					solicitudInvalida.add(in);
				}else{
					
					solicitudProcesarFM.add(reg);	
				};	
				
		}//fin Validaiones
		
		//procesar solicitudes
		if (solicitudProcesarFM.size() > 0){
			EntradaCrearSolicitudAporteRescateNormal entradaCrearSolicitudAporteRescateNormal = new EntradaCrearSolicitudAporteRescateNormal();
			
			
			//List<DetalleTraspasoDestino> solicitudOperacionTraspaso = new ArrayList<DetalleTraspasoDestino>();
			//Armar solicitudes de negocio
			//1 - APORTES Y RESCATES NORMALES
			
			for(RegistroRecaudacion procesar : solicitudProcesarFM){
					//DETALLE  DE APORTES
				for(DetalleTraspasoDestino sol : traspasosDetalleDestino){
					if ((long)sol.IdTraspaso==(long)procesar.IdTraspaso)
					{
						CrearSolicitudAporteRescateNormal.InSolicitudesAporteRescate s = new CrearSolicitudAporteRescateNormal.InSolicitudesAporteRescate();
								s.IdInterno = sol.IdDetalleTraspaso;
								s.IdCliente = sol.IdCliente;
								s.IdPortafolio = sol.IdPortafolio;
								s.TipoSolicitud = _c.TipoSolicitudAporte;
								s.IdSerieFondo = sol.IdSerieFondo;
								s.CalculoPorMontoCuota =  _c.CalculoPorMonto;
								s.EsPorMontoLiquido = _c.NO;
								s.Monto = sol.Monto.multiply(sol.Porcentaje.divide(new BigDecimal(100),MathContext.DECIMAL128),MathContext.DECIMAL128);
								s.NumeroCuotas =  BigDecimal.ZERO;
								s.IdMoneda = sol.IdMonedaFondo;
								s.IdMedioDePago = _c.CuentaInversion;
								s.IdPreferenciaPagoPortafolio = 0L;
								s.Visado = _c.NO;
								s.SolicitudBatch = _c.NO;
								s.OrigenOperacion = _c.OrigenManual;
								s.FechaOperacion = "0001-01-01";
								s.IdRegimenTributario = sol.IdRegimenFondoMutuo;
								s.Glosa = procesar.Observacion;
								s.IndicadorMLT = sol.IndicadorMLT.equals("S") ? _c.SI :_c.NO;
								s.IdSerieFondoDestino = "";
								s.IdEmisorDestino = "";
								s.IdAutorizadoOrigen = 0L;
								s.Porcentaje = BigDecimal.ZERO;
								s.FolioContrato = 0L;
								s.EsTraspaso = _c.SI;
								if(_c.REGIMEN_18Q.equals(sol.IdRegimenFondoMutuo)) {
									s.FolioContratoOperacion= String.valueOf(procesar.FolioCSC);
								}
								entradaCrearSolicitudAporteRescateNormal.p_SolicitudesAporteRescate.add(s);
					}
					
				}
				
			}// fin Armar 
		
			
			List<RegistroRecaudacion> solicitudProcesarCtaInv = new ArrayList<RegistroRecaudacion>();
			for(RegistroRecaudacion procesar : solicitudProcesarFM){
				procesar.EstadoProceso=_c.EstadoProcesado;
								
				if (_c.EstadoProcesado.equalsIgnoreCase(procesar.EstadoProceso)){
					RegistroRecaudacion solpro = new RegistroRecaudacion();
					solpro = Utils.copyPojo(procesar, solpro);
					solicitudProcesarCtaInv.add(solpro);
				};
								
			};
			//1 - CUENTA DE INVERSION
			//CUENTA INVERSION
			if (solicitudProcesarCtaInv.size() > 0){
				
				for( RegistroRecaudacion sol : solicitudProcesarCtaInv){
					//por cada traspaso se generan 3 mov en cta. inversion 
						CrearCargoAbonoSalida salidaCrearCargoAbono = mueveCuentaInversion(con, sol, entrada, fechaSistema,_DatosCorredor);
						out.P_OUT_ERRORESCONTADOR += salidaCrearCargoAbono.P_OUT_ERRORESCONTADOR;
						for(MensajeSalida mje : salidaCrearCargoAbono.P_OUT_MENSAJES){
							Long id = Math.abs(mje.id);
							if(MensajeSalida.MENSAJE_ERROR.equals(mje.tipo)){
									out.P_OUT_MENSAJES.add(new MensajeSalida(id, mje.tipo, mje.codigo, mje.descripcion));
									sol.EstadoProceso = _c.EstadoRechazado;
									sol.Codigo = mje.codigo;
									return out;
							}else{
								out.P_OUT_MENSAJES.add(new MensajeSalida(sol.IdTraspaso, MensajeSalida.MENSAJE_INFORMACION, mje.codigo, mje.descripcion));
							};
						}
						for(ObjCargoAbonoGenerado  gen : salidaCrearCargoAbono.P_OUT_CARGOABONOGENERADO){
								InMovimientoGenerado movimientoGenerado = new InMovimientoGenerado();
								movimientoGenerado.IdSecuenciaMovimiento= gen.IdCargoAbonoMovimiento;
								movimientoGenerado.IdEmpresa=sol.IdEmpresa;
								movimientoGenerado.IdUsuario=sol.IdUsuario;
								movimientoGenerado.IdModulo=_c.ModuloCtaInversion;
								movimientoGenerado.FechaIngreso=sol.FechaIngreso;
					
								sol.MovimientoGenerado.add(movimientoGenerado);
	
						}
				
				}	
				
				
			}
			
			
			//PROCESADOS CUENTA INVERSION
			List<RegistroRecaudacion> solicitudProcesarTes = new ArrayList<RegistroRecaudacion>();
			
			for(RegistroRecaudacion procta : solicitudProcesarCtaInv){
				if (_c.EstadoProcesado.equalsIgnoreCase(procta.EstadoProceso)){
					RegistroRecaudacion solpro = new RegistroRecaudacion();
					solpro = Utils.copyPojo(procta, solpro);
					solicitudProcesarTes.add(solpro);
				};
			};
			
			//INGRESO FFMM
			{	
				
				CrearSolicitudAporteRescateNormal crearSolicitudAporteRescateNormal = new CrearSolicitudAporteRescateNormal();
			
				
				if(entradaCrearSolicitudAporteRescateNormal.p_SolicitudesAporteRescate.size() > 0){
					entradaCrearSolicitudAporteRescateNormal.p_IdEmpresa = entrada.p_IdEmpresa;
					entradaCrearSolicitudAporteRescateNormal.p_IdUsuario = entrada.p_IdUsuario;
					entradaCrearSolicitudAporteRescateNormal.p_IdModulo = entrada.p_IdModulo;
					entradaCrearSolicitudAporteRescateNormal.p_SimularOperacion = _c.NO;
					entradaCrearSolicitudAporteRescateNormal.p_UsaReglasNegocio = _c.SI;
					entradaCrearSolicitudAporteRescateNormal.p_IdTipoCanalIngreso = _c.CanalAgente;
					entradaCrearSolicitudAporteRescateNormal.p_NumeroDividendo = 0L;
					SalidaCrearSolicitudAporteRescateNormal salidaCrearSolicitudAporteRescateNormal = crearSolicitudAporteRescateNormal.logica(con, entradaCrearSolicitudAporteRescateNormal);
					List <MensajeSalida> MensajesErrorSolicitud = new ArrayList<MensajeSalida>();
					Long ErroresSolicitud = 0L;
					for(MensajeSalida mje : salidaCrearSolicitudAporteRescateNormal.P_OUT_MENSAJES){
						if(MensajeSalida.MENSAJE_ERROR.equals(mje.tipo)){
							MensajesErrorSolicitud.add(mje);
							ErroresSolicitud++;
							Long id = Math.abs(mje.id);
							for(DetalleTraspasoDestino sol : traspasosDetalleDestino){
								if(sol.IdDetalleTraspaso.longValue() == id.longValue()){
									sol.EstadoProceso = _c.EstadoRechazado;
									sol.Codigo = mje.codigo;
									break;
								}
							}
						};
					}
					
					if(ErroresSolicitud.compareTo(0L) == 1){
						out.P_OUT_ERRORESCONTADOR = ErroresSolicitud;
						out.P_OUT_MENSAJES = MensajesErrorSolicitud;
						return out;
					}
					
					for(ResultadoSolicitud res : salidaCrearSolicitudAporteRescateNormal.P_OUT_RESULTADO){
						for(DetalleTraspasoDestino sol : traspasosDetalleDestino){
							if(sol.IdDetalleTraspaso.longValue() == res.IdInterno.longValue()){
								sol.IdSolicitudAporteRescate = res.IdSolicitudAporteRescate;
								sol.EstadoProceso = _c.EstadoProcesado;
								sol.FechaAceptacion= Fecha.toDate(fechaSistema);
								sol.Certifica = _c.SI;
								
								//ok ffmm
								String codigoMensajeExito = "";
								if (res.IdTipoMovimientoFondoMutuo.equalsIgnoreCase(_c.AporteFondoMutuo))
									codigoMensajeExito = "UI_SE_CREO_EL_APORTE_FONDO_MUTUO_N";
								else if(res.IdTipoMovimientoFondoMutuo.equalsIgnoreCase(_c.RescateFondoMutuo))
									codigoMensajeExito = "UI_SE_CREO_EL_RESCATE_FONDO_MUTUO_N";
								
								String mensajeExito = Diccionario.getMensaje(con, entrada.p_IdEmpresa, codigoMensajeExito).add("IdSolicitudAporteRescate", res.IdSolicitudAporteRescate).msg();
								out.P_OUT_MENSAJES.add(new MensajeSalida(sol.IdTraspaso,MensajeSalida.MENSAJE_INFORMACION,codigoMensajeExito,mensajeExito));
								break;
							}
						}
					}
				}
			}
			
			//3 - TESORERIA
			//TESORERIA
			if (solicitudProcesarTes.size() > 0){
				
				for( RegistroRecaudacion sol : solicitudProcesarTes){
					//por cada traspaso se generan 3 mov en cta. inversion 
					CrearIngresoEgreso_Salida salidaCrearIngresoEgreso = mueveTesoreria(con, sol, entrada, fechaSistema,_DatosCorredor);
					   if (salidaCrearIngresoEgreso.P_OUT_ERRORESCONTADOR != null){out.P_OUT_ERRORESCONTADOR += salidaCrearIngresoEgreso.P_OUT_ERRORESCONTADOR;};
						
						for(MensajeSalida mje : salidaCrearIngresoEgreso.P_OUT_MENSAJES){
							Long id = Math.abs(mje.id);
							
							if(MensajeSalida.MENSAJE_ERROR.equals(mje.tipo)){
									out.P_OUT_MENSAJES.add(new MensajeSalida(id, mje.tipo, mje.codigo, mje.descripcion));
									sol.EstadoProceso = _c.EstadoRechazado;
									sol.Codigo = mje.codigo;
									break;
							}
						}
						
						for(IngresoEgresoGenerado_Salida  gen : salidaCrearIngresoEgreso.P_OUT_INGRESOEGRESOGENERADO){
							InMovimientoGenerado movimientoGenerado = new InMovimientoGenerado();
							movimientoGenerado.IdSecuenciaMovimiento= gen.IdIngresoEgreso;
							movimientoGenerado.IdEmpresa=sol.IdEmpresa;
							movimientoGenerado.IdUsuario=sol.IdUsuario;
							movimientoGenerado.IdModulo=_c.ModuloTesoreria;
							movimientoGenerado.FechaIngreso=sol.FechaIngreso;
				
							sol.MovimientoGenerado.add(movimientoGenerado);
							String codigoMensajeExito = "";
							
							if (gen.IdTipoMovimiento.equalsIgnoreCase(_c.IngresoTesoreria))
								codigoMensajeExito = "UI_CREADO_MOVIMIENTO_INGRESO";
							else if(gen.IdTipoMovimiento.equalsIgnoreCase(_c.EgresoTesoreria))
								codigoMensajeExito = "UI_CREADO_MOVIMIENTO_EGRESO";
							
							String mensajeExito = Diccionario.getMensaje(con, sol.IdEmpresa, codigoMensajeExito).add("IdIngresoEgreso", gen.IdIngresoEgreso).msg();	
							
							out.P_OUT_MENSAJES.add(new MensajeSalida(sol.IdTraspaso,MensajeSalida.MENSAJE_INFORMACION,codigoMensajeExito,mensajeExito));
						}
						
						
						
				
				}	
				
				
			}
			
			//PROCESADOS TESORERIA
			List<RegistroRecaudacion> recaudacionProcesada = new ArrayList<RegistroRecaudacion>();
			int i = 0;
			List<Long> secuencias = Utils.getSecuencia(con, "BSCL.FondoMutuo.BD.Secuencias::RecaudacionMovimiento", new Long(solicitudProcesarTes.size()));
			
			for(RegistroRecaudacion protes : solicitudProcesarTes){
				if (_c.EstadoProcesado.equalsIgnoreCase(protes.EstadoProceso)){
					RegistroRecaudacion solpro = new RegistroRecaudacion();
					solpro = Utils.copyPojo(protes, solpro);
					solpro.IdRecaudacion=secuencias.get(i);
					recaudacionProcesada.add(solpro);
				};
				i++;	
			};
			
			
			//ACTUALIZACION TABLAS DE TRASPASO Y RECAUDACION
			
			//Detalle instrumentos de FFMM
			List<DetalleTraspasoDestino> detalleProcesada = new ArrayList<DetalleTraspasoDestino>();
			List<DetalleTraspasoDestino> detalleInvalida = new ArrayList<DetalleTraspasoDestino>();
			for(DetalleTraspasoDestino sol : traspasosDetalleDestino){
				if (_c.EstadoRechazado.equalsIgnoreCase(sol.EstadoProceso)){
					detalleInvalida.add(sol);
				}else{
					detalleProcesada.add(sol);	
				};
			}
			if(detalleProcesada.size() > 0){
				sql.batch(con, SQL_TRASPASO_DETALLE_DESTINO_UPDATE, detalleProcesada, DetalleTraspasoDestino.class);
			}
			if(recaudacionProcesada.size() > 0){
			
				sql.batch(con, SQL_TRASPASO_UPDATE, recaudacionProcesada, RegistroRecaudacion.class);
				
				sql.batch(con, SQL_RECAUDACION_INSERT, recaudacionProcesada, RegistroRecaudacion.class);
				
				for(RegistroRecaudacion sol : recaudacionProcesada){
					
					List<InMovimientoGenerado> recaudacionMovimiento = new ArrayList<InMovimientoGenerado>();
					for(InMovimientoGenerado mov : sol.MovimientoGenerado){
						mov.IdRecaudacion=sol.IdRecaudacion;
						recaudacionMovimiento.add(mov);
					}
					sql.batch(con, SQL_RECAUDACION_INSERTMOVIMIENTO, recaudacionMovimiento, InMovimientoGenerado.class);
				}
			}
				
		}//procesar
		
	
		
		
	

		return out;
	}
	
	private CrearIngresoEgreso_Salida mueveTesoreria(Connection con, RegistroRecaudacion sol, EntradaRecaudacionTraspaso entrada, String fechaSistema,List<DatosCorredor> _DatosCorredor) throws Exception{
		
		SQL sql = new SQL();
		Long IdEmpleadoDigitador = 0L;
		Long idCtaBancoCorredor = 0L;
		String IdTipoCuentaBancariaSocio ="";
		String NumeroCuentaBancariaSocio ="";
		Long IdCuentaBancariaSocio=0L;
		
		log.write("INICIO CrearIngresoEgreso_Salida");
		CrearIngresoEgreso crearIngresoEgreso = new CrearIngresoEgreso();
		CrearIngresoEgreso_Entrada entradacrearIngresoEgreso = new CrearIngresoEgreso_Entrada();
		List<CrearComprobante.FormaDePago_Entrada> paramsComprobanteFormaDePago_Entrada = new ArrayList<CrearComprobante.FormaDePago_Entrada>();
		CrearComprobante.FormaDePago_Entrada ComprobanteFormaDePago = new CrearComprobante.FormaDePago_Entrada();//ComprobanteFormaPago

	
		//Obtiene empleado digitador a partir de IdUsuario
		HashMap<String,Object> params = new HashMap<>();
		params.put("IdUsuario", entrada.p_IdUsuario);
		
		List<Empleado> empleadoDigitador = sql.query(con, SQL_SELECT_Empleado, params, Empleado.class);
		
		if (empleadoDigitador.size() > 0)
		{
			IdEmpleadoDigitador = empleadoDigitador.get(0).IdEmpleado;
		};
		
		Long IdPortafolioCorredor 	= _DatosCorredor.get(0).IdPortafolio;
		Long IdPersonaCorredor 		= _DatosCorredor.get(0).IdPersona;
		
		//cta Distribucion
		HashMap<String,Object> param = new HashMap<>();
		param.put("IdEmpresa", entrada.p_IdEmpresa);
		List<CtaDistribucion> CtaBancoCorredor = sql.query(con, SQL_CtaDistribucion, param, CtaDistribucion.class);
		if (CtaBancoCorredor.size() > 0)
		{
			idCtaBancoCorredor = CtaBancoCorredor.get(0).DestinoCtaBco;
		};
		
		//cta Bancaria
		List<CtaDistribucion> CuentaBancaria = sql.query(con, SQL_CtaBancaria, param, CtaDistribucion.class);
		if (CuentaBancaria.size() > 0)
		{
			IdTipoCuentaBancariaSocio = CuentaBancaria.get(0).IdTipoCuentaBancariaSocio;
			NumeroCuentaBancariaSocio = CuentaBancaria.get(0).NumeroCuentaBancariaSocio;
			IdCuentaBancariaSocio  = CuentaBancaria.get(0).IdCuentaBancariaSocio;
		};
		
		

		String monedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		
		Long i = 0L;

		IngresoEgreso_Entrada ie = new IngresoEgreso_Entrada();
		//CORREDOR : INGRESO CTA. Recaudacion
			ie.NumeroReferencia = i;
			ie.IdTipoMovimiento =  _c.IngresoTesoreria ;
			ie.IdTipoPersona = _c.TipoPersonaContraparte;
			ie.IdPersona = IdPersonaCorredor; 
			ie.IdPortafolio = 0L;
			ie.IdMoneda = sol.IdMonedaFondo;
			ie.Monto    = sol.MontoDeposito;
			ie.FechaLiquidacion = fechaSistema;
			ie.IdConcepto = _c.ConceptoTraspasoIngreso;
			ie.IdTipoPersonaTitularPago =  _c.TipoPersonaContraparte;
			ie.IdPersonaTitularPago = sol.IdPersonaEntidad; 
			ie.IdPortafolioTitularPago = 0L;
			ie.IdCaja = 0L;
			ie.SecuenciaAgrupacion = "";
			ie.SecuenciaRelacionada = 0L;
			ie.Glosa = "";
			ie.IdEmpleadoDigitador = IdEmpleadoDigitador;
			ie.IdSucursalEmpleadoDigitador = 0L;
			ie.IdTipoProducto = _c.TipoProducto;
			ie.IdTipoOperacion = _c.TipoOperacionAporteFondoMutuo;
			ie.IdTipoComprobante = _c.TipoComprobanteTesoreria;
			ie.IdComprobante = sol.IdTraspaso;
			ie.FechaComprobante = Fecha.stringToSqlDate(sol.FechaAceptacion);
			ie.FechaLiquidacionComprobante = "0001-01-01";
			ie.IdOperacion = sol.IdTraspaso;
			ie.FechaOperacion = fechaSistema;
			ie.IdCondicionLiquidacion = _c.T0;
			ie.IdBolsa = _c.BCS;
			ie.IdMonedaOperacion = sol.IdMonedaFondo;
			ie.MontoOperacion = sol.MontoDeposito;
			ie.IdTipoReajuste = "";
			ie.FechaParidad =  "0001-01-01";
			ie.ValorParidad = BigDecimal.valueOf(0);	
			ie.FolioCartera = 0L;
			ie.IdInstrumento = 0L;
			ie.IdTipoInstrumento = "";
			ie.IdClasificacionTipoInstrumento = "";
			ie.Bilateral = _c.NO;
			ie.GeneraComprobante = _c.NO;
			ie.AgruparEnComprobante = _c.NO;
			ie.MueveCuentaInversion = _c.SI;
			ie.TitularPago = _c.NO;
			
			FormaDePago_Entrada fp = new FormaDePago_Entrada();
			fp.NumeroReferencia = i;
			fp.NumeroReferenciaFormaDePago = i;
			fp.IdMedioDePago = sol.IdMedioDePago;
			fp.IdModalidadDePago = _c.MODDEP;
			fp.IdMoneda = sol.IdMonedaFondo;
			fp.Monto =  sol.MontoDeposito;
			fp.IdCuentaBancariaCorredora = sol.IdCuentaBanco;
			fp.IdTarjetaBancariaCorredora = 0L;
			fp.IdChequera = 0L;
			fp.NumeroDocumentoCorredora = 0L;
			fp.FechaDocumentoPagoCorredora = "0001-01-01";
			fp.IdCuentaBancariaSocio = IdCuentaBancariaSocio;
			fp.IdBancoSocio = 0L;
			fp.NombreCuentaBancariaSocio = "";
			fp.NumeroCuentaBancariaSocio = NumeroCuentaBancariaSocio;
			fp.IdTipoCuentaBancariaSocio = "";
			fp.NombreTipoCuentaBancariaSocio = "";
			fp.IdTarjetaBancariaSocio = 0L;
			fp.IdTipoTarjetaBancariaSocio = "";
			fp.NombreTipoTarjetaBancariaSocio = "";
			fp.NumeroTarjetaBancariaSocio = "";
			fp.NumeroSerieSocio = "";
			fp.NumeroDocumentoSocio = 0L;
			fp.FechaDocumentoPagoSocio = "0001-01-01";
			
			entradacrearIngresoEgreso.p_IngresoEgresoGenerar.add(ie);
			entradacrearIngresoEgreso.p_FormaDePago.add(fp);
		
		//comprobante
			ComprobanteFormaDePago.NumeroReferencia = i;
			ComprobanteFormaDePago.IdMedioDePago = sol.IdMedioDePago;
			ComprobanteFormaDePago.IdModalidadDePago ="MODDEP";
			ComprobanteFormaDePago.IdMoneda =sol.IdMonedaFondo;
			ComprobanteFormaDePago.Monto =sol.MontoDeposito;
			ComprobanteFormaDePago.IdCuentaBancariaCorredora = sol.IdCuentaBanco;
			ComprobanteFormaDePago.IdTarjetaBancariaCorredora =0L;
			ComprobanteFormaDePago.IdChequera =0L;
			ComprobanteFormaDePago.NumeroDocumentoCorredora =0L;
			ComprobanteFormaDePago.FechaDocumentoPagoCorredora ="0001-01-01";
			ComprobanteFormaDePago.IdCuentaBancariaSocio =0L;
			ComprobanteFormaDePago.IdBancoSocio =0L;
			ComprobanteFormaDePago.NombreCuentaBancariaSocio ="";
			ComprobanteFormaDePago.NumeroCuentaBancariaSocio ="";
			ComprobanteFormaDePago.IdTipoCuentaBancariaSocio =IdTipoCuentaBancariaSocio;
			ComprobanteFormaDePago.NombreTipoCuentaBancariaSocio ="";
			ComprobanteFormaDePago.IdTarjetaBancariaSocio =0L;
			ComprobanteFormaDePago.IdTipoTarjetaBancariaSocio ="";
			ComprobanteFormaDePago.NombreTipoTarjetaBancariaSocio ="";
			ComprobanteFormaDePago.NumeroTarjetaBancariaSocio ="";
			ComprobanteFormaDePago.NumeroSerieSocio ="";
			ComprobanteFormaDePago.NumeroDocumentoSocio =0L;
			ComprobanteFormaDePago.FechaDocumentoPagoSocio ="0001-01-01";
			paramsComprobanteFormaDePago_Entrada.add(ComprobanteFormaDePago);

			
		//CORREDOR : EGRESO	CTA. Recaudacion		
			i++;
			IngresoEgreso_Entrada ie2 = new IngresoEgreso_Entrada();
			ie2 = Utils.copyPojo(ie, ie2);
			ie2.IdTipoMovimiento =  _c.EgresoTesoreria;
			ie2.NumeroReferencia = i;		
			ie2.IdPersona = IdPersonaCorredor;
			ie2.IdPersonaTitularPago = IdPersonaCorredor;
			ie2.IdPortafolioTitularPago = IdPortafolioCorredor;
			
			FormaDePago_Entrada fp2 = new FormaDePago_Entrada();
			fp2 = Utils.copyPojo(fp, fp2);
			fp2.NumeroReferencia = i;
			fp2.NumeroReferenciaFormaDePago = i;
			
			entradacrearIngresoEgreso.p_IngresoEgresoGenerar.add(ie2);
			entradacrearIngresoEgreso.p_FormaDePago.add(fp2);
			
		//CORREDOR : INGRESO Cta. Disribucion			
			i++;
			IngresoEgreso_Entrada ie3 = new IngresoEgreso_Entrada();
			ie3 = Utils.copyPojo(ie, ie3);
			ie3.IdTipoMovimiento =  _c.IngresoTesoreria;
			ie3.NumeroReferencia = i;		
			ie3.IdPersona =  sol.IdPersonaCliente;
			ie3.IdPersonaTitularPago = IdPersonaCorredor;
			ie3.IdPortafolioTitularPago = IdPortafolioCorredor;
			
			FormaDePago_Entrada fp3 = new FormaDePago_Entrada();
			fp3 = Utils.copyPojo(fp, fp3);
			fp3.NumeroReferencia = i;
			fp3.NumeroReferenciaFormaDePago = i;
			fp3.IdCuentaBancariaCorredora = sol.IdCuentaBanco;
			entradacrearIngresoEgreso.p_IngresoEgresoGenerar.add(ie3);
			entradacrearIngresoEgreso.p_FormaDePago.add(fp3);
			

		entradacrearIngresoEgreso.p_IdEmpresa = entrada.p_IdEmpresa;
		entradacrearIngresoEgreso.p_IdUsuario = entrada.p_IdUsuario;
		entradacrearIngresoEgreso.p_IdModulo = entrada.p_IdModulo;
		entradacrearIngresoEgreso.p_SimularOperacion = "N";
		entradacrearIngresoEgreso.p_UsaReglasNegocio = "S";
		entradacrearIngresoEgreso.p_EsIngresoAutomatico =  "S";
		CrearIngresoEgreso_Salida salida = crearIngresoEgreso.logica(con, entradacrearIngresoEgreso);

		log.write("FIN CrearIngresoEgreso_Salida");
		List<CrearComprobante.IngresoEgresoGenerar_Entrada> paramsComprobanteIngresoEgreso_Entrada = new ArrayList<CrearComprobante.IngresoEgresoGenerar_Entrada>();
		   
		 //Se toma la salida del ingresoEgreso de tesoreria para crear comprobantes 
		for(CrearIngresoEgreso.IngresoEgresoGenerado_Salida outIngresoEgreso :salida.P_OUT_INGRESOEGRESOGENERADO){

			CrearComprobante.IngresoEgresoGenerar_Entrada ComprobanteIngresoEgreso_Entrada = new CrearComprobante.IngresoEgresoGenerar_Entrada();
			ComprobanteIngresoEgreso_Entrada.NumeroReferencia = outIngresoEgreso.NumeroReferencia;
			ComprobanteIngresoEgreso_Entrada.IdIngresoEgreso = outIngresoEgreso.IdIngresoEgreso;
			ComprobanteIngresoEgreso_Entrada.TipoComprobante = "";
			paramsComprobanteIngresoEgreso_Entrada.add(ComprobanteIngresoEgreso_Entrada);
		};
		
		 //COMPROBANTE
	    
		CrearComprobante.CrearComprobante_Entrada paramsComprobante_Entrada = new CrearComprobante.CrearComprobante_Entrada();
		List<CrearComprobante.ComprobanteGenerar_Entrada> paramsComprobanteGenerar_Entrada = new ArrayList<CrearComprobante.ComprobanteGenerar_Entrada>();
		
		paramsComprobante_Entrada.p_IdEmpresa = entrada.p_IdEmpresa;
		paramsComprobante_Entrada.p_IdUsuario = entrada.p_IdUsuario;
		paramsComprobante_Entrada.p_IdModulo = entrada.p_IdModulo;
		paramsComprobante_Entrada.p_UsaReglasNegocio = "S";
		paramsComprobante_Entrada.p_FechaMovimiento = fechaSistema.toString();
		paramsComprobante_Entrada.p_IdCaja = 1L;
		paramsComprobante_Entrada.p_GenerarComprobanteDesdeMovimientos = "S";
		paramsComprobante_Entrada.p_ComprobanteGenerar = paramsComprobanteGenerar_Entrada;
		paramsComprobante_Entrada.p_IngresoEgresoGenerar = paramsComprobanteIngresoEgreso_Entrada;
		paramsComprobante_Entrada.p_FormaDePago = paramsComprobanteFormaDePago_Entrada;
		
		CrearComprobante ProcesoCrearComprobante = new CrearComprobante();

		CrearComprobante.CrearComprobante_Salida OutComprobante = ProcesoCrearComprobante.logica(con, paramsComprobante_Entrada); 
	   // out.P_OUT_MENSAJES.addAll(OutComprobante.P_OUT_MENSAJES);
		List<LiquidarComprobante.ComprobanteLiquidar_Entrada> paramsComprobanteLiquidar_Entrada = new ArrayList<LiquidarComprobante.ComprobanteLiquidar_Entrada>();

		if (MensajeSalida.getNumeroErrores(OutComprobante.P_OUT_MENSAJES) > 0) 
		{
			
		}else{
			for(int z = 0; z < OutComprobante.P_OUT_RESULTADOPROCESO.size(); z++){
				if(OutComprobante.P_OUT_RESULTADOPROCESO.get(z).Campo.equals("IdComprobante")){
					LiquidarComprobante.ComprobanteLiquidar_Entrada ComprobanteLiquidar_Entrada = new LiquidarComprobante.ComprobanteLiquidar_Entrada();
					ComprobanteLiquidar_Entrada.IdComprobante = OutComprobante.P_OUT_RESULTADOPROCESO.get(z).Id;
					ComprobanteLiquidar_Entrada.IdTipoMovimiento = entradacrearIngresoEgreso.p_IngresoEgresoGenerar.get(z).IdTipoMovimiento;
					ComprobanteLiquidar_Entrada.AfectaCuentaInversion = "CLI";
					paramsComprobanteLiquidar_Entrada.add(ComprobanteLiquidar_Entrada);
				}
			}
		}
		
		LiquidarComprobante.LiquidarComprobante_Entrada paramsLiquidar_Entrada = new LiquidarComprobante.LiquidarComprobante_Entrada();
		
		paramsLiquidar_Entrada.p_IdEmpresa = entrada.p_IdEmpresa;
		paramsLiquidar_Entrada.p_IdUsuario = entrada.p_IdUsuario;
		paramsLiquidar_Entrada.p_IdModulo = entrada.p_IdModulo;
		paramsLiquidar_Entrada.p_UsaReglasNegocio = "S";
		paramsLiquidar_Entrada.p_FechaMovimiento = fechaSistema.toString();
		paramsLiquidar_Entrada.p_ComprobanteLiquidar = paramsComprobanteLiquidar_Entrada;
		
		LiquidarComprobante ProcesoLiquidar = new LiquidarComprobante();
		
		LiquidarComprobante.LiquidarComprobante_Salida OutLiquidar = ProcesoLiquidar.logica(con, paramsLiquidar_Entrada);
       // out.P_OUT_MENSAJES.addAll(OutLiquidar.P_OUT_MENSAJES);
	
		return salida;
	}
	
	private CrearCargoAbonoSalida mueveCuentaInversion(Connection con, RegistroRecaudacion sol, EntradaRecaudacionTraspaso entrada, String fechaSistema,List<DatosCorredor> _DatosCorredor) throws Exception{
		SQL sql = new SQL();
		Long IdEmpleadoDigitador = 0L;
		log.write("INICIO CrearCargoAbonoSalida");
		
		//Obtiene empleado digitador a partir de IdUsuario
		HashMap<String,Object> params = new HashMap<>();
		params.put("IdUsuario", entrada.p_IdUsuario);
		
		List<Empleado> empleadoDigitador = sql.query(con, SQL_SELECT_Empleado, params, Empleado.class);
		
		if (empleadoDigitador.size() > 0)
		{
			IdEmpleadoDigitador = empleadoDigitador.get(0).IdEmpleado;
		}		
				
		Long IdPortafolioCorredor 	= _DatosCorredor.get(0).IdPortafolio;
		
		String monedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		
		CrearCargoAbonoSalida salidaCrearCargoAbono = new CrearCargoAbonoSalida();
		salidaCrearCargoAbono.P_OUT_ERRORESCONTADOR = 0L;
		salidaCrearCargoAbono.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		CrearCargoAbonoEntrada entradaCrearCargoAbono = new CrearCargoAbonoEntrada();
		CrearCargoAbono crearCargoAbono = new CrearCargoAbono();
		Long i = 0L;
		HashMap<Long,Long> ids = new HashMap<Long,Long>();

					ObjCargoAbonoAGenerar obj = new ObjCargoAbonoAGenerar();
					//CARGO CORREDORA
					obj.NumeroCargoAbono = i;
					obj.IdPortafolio = IdPortafolioCorredor;
					obj.IdMoneda = sol.IdMonedaFondo;
					obj.IdTipoMovimiento = _c.CargoCuentaInversion;
					obj.Monto = sol.MontoDeposito;
					obj.IdMonedaOperacion = sol.IdMonedaFondo;
					obj.MontoOperacion = sol.MontoDeposito;
					obj.IdConcepto = _c.ConceptoTraspasoIngreso;
					obj.Descripcion = "APORTE_FONDO_MUTUO";
					obj.IdCondicionDeLiquidacion = "";
					obj.FechaCondicionDeLiquidacion = fechaSistema;
					obj.IdEjecutivo = IdEmpleadoDigitador;
					obj.IdSucursal = 0L;
					obj.IdTipoProducto = _c.TipoProducto;
					obj.IdTipoOperacion = _c.TipoOperacionAporteFondoMutuo;
					obj.IdOperacion = sol.IdTraspaso;
					obj.FechaOperacion = fechaSistema;
					obj.FechaLiquidacionOperacion = fechaSistema;
					obj.TipoCambioOperacion = new BigDecimal(0);
					obj.IdTipoComprobante = _c.TipoComprobanteCuentaInversion;
					obj.IdComprobante = sol.IdTraspaso;
					obj.FolioComprobante = sol.IdTraspaso.toString();
					obj.IdComprobanteDetalle = sol.IdTraspaso;
					obj.IdTipoMovimientoAsociado = "";
					obj.IdMovimientoAsociado = 0L;
					obj.FechaComprobante = fechaSistema;
					obj.IdInstrumento = 0L;
					obj.IdTipoInstrumento = "";
					obj.Nemo = "";
					obj.EsCargoAbonoPorTraspaso = _c.NO;
					obj.GeneroContraparteEnTesoreria = _c.NO;
					obj.CertificadoAutomatico = _c.SI;
					ids.put(i, sol.IdTraspaso);
					i++;
					//ABONO CORREDORA
						ObjCargoAbonoAGenerar cardif = new ObjCargoAbonoAGenerar();
						cardif = Utils.copyPojo(obj, cardif);
						cardif.NumeroCargoAbono = i;
						cardif.IdTipoMovimiento = _c.AbonoCuentaInversion;
						entradaCrearCargoAbono.p_CargoAbonoAGenerar.add(cardif);
						ids.put(i, sol.IdTraspaso);
						i++;
			
					
					//ABONO CLIENTE
				
						ObjCargoAbonoAGenerar abndif = new ObjCargoAbonoAGenerar();
						abndif = Utils.copyPojo(obj, abndif);
						abndif.NumeroCargoAbono = i;
						abndif.IdPortafolio = sol.IdPortafolio;
						abndif.IdTipoMovimiento = _c.AbonoCuentaInversion;
						entradaCrearCargoAbono.p_CargoAbonoAGenerar.add(abndif);
						ids.put(i, sol.IdTraspaso);
						i++;
						
					entradaCrearCargoAbono.p_CargoAbonoAGenerar.add(obj);
				

		if(entradaCrearCargoAbono.p_CargoAbonoAGenerar.size() > 0){
			entradaCrearCargoAbono.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaCrearCargoAbono.p_IdUsuario = entrada.p_IdUsuario;
			entradaCrearCargoAbono.p_IdModulo = entrada.p_IdModulo;
			entradaCrearCargoAbono.p_simularOperacion =  "N";
			entradaCrearCargoAbono.p_UsaReglasNegocio =  "S";
			entradaCrearCargoAbono.p_FechaSistema = fechaSistema;
			entradaCrearCargoAbono.p_EsIngresoManual	= "N";
			salidaCrearCargoAbono = crearCargoAbono.logica(con, entradaCrearCargoAbono);
			
			//Cambiamos el id interno por el id de la solicitud
			for(MensajeSalida mje : salidaCrearCargoAbono.P_OUT_MENSAJES){
				if(MensajeSalida.MENSAJE_ERROR.equals(mje.tipo)){
					Long id = ids.get(mje.id);
					mje.id = id;
				}
			}
		}
		log.write("FIN CrearCargoAbonoSalida");
		
		return salidaCrearCargoAbono;
	}
	
	

	
	
	/********************
	 * Metodo Principal *
	 ********************/

	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();	
				
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":15,\"p_IdModulo\":\"FMM\",\"p_Recaudacion\":[{\"IdTraspaso\":99953696,\"FechaDeposito\":\"2019-12-16\",\"IdMedioDePago\":\"TRA\",\"IdBanco\":17,\"NumeroCuentaBancaria\":\"66934853\",\"IdCuentaBanco\":1438,\"Sucursal\":\"\",\"MontoDeposito\":10000,\"Observacion\":\"TEST 2.0 EDU\"}]}";
				
				
				
				EntradaRecaudacionTraspaso Objin_json = gson.fromJson(json, EntradaRecaudacionTraspaso.class);
				
				RecaudacionTraspaso demo = new RecaudacionTraspaso();	
				
	 			System.out.println("ENTRADA");
	 			System.out.println(Objin_json.toString());
	 			System.out.println("SALIDA");

	 			try { 
	 				SalidaRecaudacionTraspaso ObjOut = demo.logica(con, Objin_json);
	 				//con.commit();
	 				con.rollback();
	 				System.out.println(ObjOut.toString());
	 			} 
	 			catch(Exception er) {
					er.printStackTrace();
					con.rollback();
				}
	 			
		} catch (Exception e) {

			e.printStackTrace();			
		}	
		System.out.println("== END ==");
		};
	}
}