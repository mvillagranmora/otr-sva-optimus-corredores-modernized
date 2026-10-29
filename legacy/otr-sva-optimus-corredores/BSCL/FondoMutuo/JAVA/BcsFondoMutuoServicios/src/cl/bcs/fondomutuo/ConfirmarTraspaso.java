package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal.EntradaCrearSolicitudAporteRescateNormal;
import cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal.SalidaCrearSolicitudAporteRescateNormal;
import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.fondomutuo.pojo.ResultadoSolicitud;
import cl.bcs.fondomutuo.pojo.Traspaso.RegistroTraspaso;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;



public class ConfirmarTraspaso implements JavaCall   {

	private final Log log = new Log("FM.ConfirmarTraspaso");
	
	public String SQL_TRASPASO_SELECT					= loadResourceToString("cl/bcs/fondomutuo/sql/Traspaso_Confirmar_SelectTraspaso.sql");
		
	public String SQL_TRASPASO_UPDATE	 				= loadResourceToString("cl/bcs/fondomutuo/sql/Traspaso_Confirmar_UpdateTraspaso.sql");
	
	public String SQL_TRASPASO_DETALLE_ORIGEN_SELECT 	= loadResourceToString("cl/bcs/fondomutuo/sql/Traspaso_Confirmar_SelectTraspasoOrigen.sql");
	
	public String SQL_TRASPASO_DETALLE_ORIGEN_UPDATE	= loadResourceToString("cl/bcs/fondomutuo/sql/Traspaso_Confirmar_UpdateTraspasoOrigen.sql");
	
	public String SQL_TRASPASO_DETALLE_DESTINO_SELECT 	= loadResourceToString("cl/bcs/fondomutuo/sql/Traspaso_Confirmar_SelectTraspasoDestino.sql");
	
	
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	public static class InTraspaso{
		public Long 	IdTraspaso;
		private String  EstadoProceso ="";
		public String	IdEmisor;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	};
	
	public static class DetalleTraspasoOrigen{
	
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
		public String IdRegimenFondoMutuo;
		public Date 	FechaAceptacion;
		
		
		public Long IdInterno;
		public Long IdDetalleTraspaso;
		public String IdMoneda;
	    public DetalleTraspasoOrigen() {
	    	IdInterno = 0L;
	    	IdTraspaso = 0L;
	    	IdDetalleTraspaso = 0L;
	    	IdMoneda = "CLP";
	    }
	    
	    public String toString() {
			return Utils.printJson(this, false);
		}
	    
	    
	};
	
	public static class DetalleTraspasoDestino{
		//public Long   	IdDetalleTraspaso;
		public Long   	IdTraspaso;
		public String 	IdSerieFondo;
		public BigDecimal 	Porcentaje;
		public String 	IdEstado;
		public Date 	FechaIngreso;
		public Date 	FechaModificacion;
		public String IdRegimenFondoMutuo;	
		public Long IdInterno;
		public Long IdDetalleTraspaso;
	    public DetalleTraspasoDestino() {
	    	IdInterno = 0L;
	    	IdTraspaso = 0L;
	    	IdDetalleTraspaso = 0L;
	    }
	    public String toString() {
			return Utils.printJson(this, false);
		}
	    
	};

	
	//  ENTRADA
	public static class EntradaConfirmarTraspaso {
		
		public 	Long						p_IdEmpresa;
		public	Long						p_IdUsuario;
		public 	String						p_IdModulo;
		public	List<InTraspaso>			p_Traspaso;
		
		
		public EntradaConfirmarTraspaso(){
			p_Traspaso 					= new ArrayList<InTraspaso>();
			
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	
	
	// SALIDA

	public static class SalidaConfirmarTraspaso{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		public List <ResultadoSolicitud> P_OUT_RESULTADO;
		
		public SalidaConfirmarTraspaso(){
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
		EntradaConfirmarTraspaso jin = gson.fromJson(in.toString(), EntradaConfirmarTraspaso.class);
		SalidaConfirmarTraspaso out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaConfirmarTraspaso logica(Connection con, EntradaConfirmarTraspaso entrada) throws Exception{
	
		log.write("INICIO SalidaConfirmarTraspaso");
		SalidaConfirmarTraspaso out = new SalidaConfirmarTraspaso();
		SQL sql=new SQL();
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		String monedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			return out;
		}
		
		
		if(entrada.p_Traspaso.size() == 0){
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_CONFIRMACIONES_DE_TRASPASO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
			return out;
		}
		
		ArrayList<Long> correlativos = new ArrayList<Long>();
		for (InTraspaso mov: entrada.p_Traspaso) {
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
		for(InTraspaso in : entrada.p_Traspaso){
			if(!idTraspaso.contains(in.IdTraspaso)) idTraspaso.add(in.IdTraspaso);
		}
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("IdTraspaso", idTraspaso);
		
		List<RegistroTraspaso> traspasos = sql.query(con, SQL_TRASPASO_SELECT, params, RegistroTraspaso.class);
		List<DetalleTraspasoOrigen> traspasosDetalleOrigen = sql.query(con, SQL_TRASPASO_DETALLE_ORIGEN_SELECT, params, DetalleTraspasoOrigen.class);
		
		List<DetalleTraspasoDestino> traspasosDetalleDestino = sql.query(con, SQL_TRASPASO_DETALLE_DESTINO_SELECT, params, DetalleTraspasoDestino.class);
		
		

		
		List<InTraspaso> solicitudProcesar = new ArrayList<InTraspaso>();
		List<InTraspaso> solicitudInvalida = new ArrayList<InTraspaso>();
		
	
		
		
		//validaciones del Traspaso
		for(InTraspaso in : entrada.p_Traspaso){
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
						in.IdEmisor=solicitud.IdEmisor;
					}
				}
				//verificacion Origen
				int contOrigen=0;
				for(DetalleTraspasoOrigen DetalleOrigen : traspasosDetalleOrigen){
					if ((long)DetalleOrigen.IdTraspaso==(long)in.IdTraspaso)
					{
						contOrigen++;
					}
					
				}
			
				if(contOrigen == 0){
					in.EstadoProceso = _c.EstadoRechazado;
					out.P_OUT_ERRORESCONTADOR++;
					out.P_OUT_MENSAJES.add(new MensajeSalida(in.IdTraspaso,MensajeSalida.MENSAJE_ERROR,"UI_TRASPASO_SIN_ORIGEN",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
					continue;
				}
				
				//verificacion Destino
				int contDestino=0;
				BigDecimal sumaPorcentajeDestino=BigDecimal.ZERO;
				for(DetalleTraspasoDestino DetalleDestino : traspasosDetalleDestino){
					if ((long)DetalleDestino.IdTraspaso==(long)in.IdTraspaso)
					{
						contDestino++;
						sumaPorcentajeDestino=sumaPorcentajeDestino.add(DetalleDestino.Porcentaje);
					}
					
				}
				
				if(contDestino == 0){
					in.EstadoProceso = _c.EstadoRechazado;
					out.P_OUT_ERRORESCONTADOR++;
					out.P_OUT_MENSAJES.add(new MensajeSalida(in.IdTraspaso,MensajeSalida.MENSAJE_ERROR,"UI_TRASPASO_SIN_DESTINO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
					continue;
				}
				
				if(sumaPorcentajeDestino.intValue()!=100){
					in.EstadoProceso = _c.EstadoRechazado;
					out.P_OUT_ERRORESCONTADOR++;
					out.P_OUT_MENSAJES.add(new MensajeSalida(in.IdTraspaso,MensajeSalida.MENSAJE_ERROR,"UI_PORCENTAJE_INCORRECTO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
					continue;
				}
			
				
				if (_c.EstadoRechazado.equalsIgnoreCase(in.EstadoProceso)){
					solicitudInvalida.add(in);
				}else{
					
					solicitudProcesar.add(in);	
				};	
				
		}//fin Validaiones
		
		
		//procesar solicitudes
		if (solicitudProcesar.size() > 0){
			EntradaCrearSolicitudAporteRescateNormal entradaCrearSolicitudAporteRescateNormal = new EntradaCrearSolicitudAporteRescateNormal();
			
			
			for(InTraspaso procesar : solicitudProcesar){
				
				for(DetalleTraspasoOrigen sol : traspasosDetalleOrigen){
					if ((long)sol.IdTraspaso==(long)procesar.IdTraspaso)
					{
					
						CrearSolicitudAporteRescateNormal.InSolicitudesAporteRescate s = new CrearSolicitudAporteRescateNormal.InSolicitudesAporteRescate();
								s.IdInterno = sol.IdDetalleTraspaso;
								s.IdCliente = sol.IdCliente;
								s.IdPortafolio = sol.IdPortafolio;
								s.TipoSolicitud = _c.TraspasoMovimientoEgreso.equals(sol.IdTipoMovimientoTraspaso) ? _c.TipoSolicitudRescate : _c.TipoSolicitudAporte;
								s.IdSerieFondo = sol.IdSerieFondo;
								s.CalculoPorMontoCuota =  _c.CalculoPorCuota;
								s.EsPorMontoLiquido = _c.NO;
								s.Monto =  BigDecimal.ZERO;
								s.NumeroCuotas = sol.Cantidad;
								s.IdMoneda = sol.IdMoneda;
								s.IdMedioDePago = _c.MedioPagoTransferencia;
								s.IdPreferenciaPagoPortafolio = 0L;
								s.Visado = _c.NO;
								s.SolicitudBatch = _c.NO;
								s.OrigenOperacion = _c.OrigenManual;
								s.FechaOperacion = "0001-01-01";
								s.IdRegimenTributario = sol.IdRegimenFondoMutuo;
								s.Glosa = sol.Glosa;
								s.IdAutorizadoOrigen = 0L;
								s.Porcentaje = BigDecimal.ZERO;
								s.FolioContrato = 0L;
								s.IndicadorMLT=_c.SI;
								s.IdEmisorDestino=procesar.IdEmisor;
								s.IdSerieFondoDestino=""; // desde la gestion se obtiene la suma total del monto desde los fonfos origen repartido a N destinos,  no es 1 a 1 
								s.IngresoAutomatico = _c.SI;
								s.EsTraspaso = _c.SI;
								entradaCrearSolicitudAporteRescateNormal.p_SolicitudesAporteRescate.add(s);
							
						
					}
					
				}
				
			}
			
			
			//INGRESO DE OPERACIONES
			{	
				//1 - APORTES Y RESCATES NORMALES
				CrearSolicitudAporteRescateNormal crearSolicitudAporteRescateNormal = new CrearSolicitudAporteRescateNormal();
			
				
				if(entradaCrearSolicitudAporteRescateNormal.p_SolicitudesAporteRescate.size() > 0){
					entradaCrearSolicitudAporteRescateNormal.p_IdEmpresa = entrada.p_IdEmpresa;
					entradaCrearSolicitudAporteRescateNormal.p_IdUsuario = entrada.p_IdUsuario;
					entradaCrearSolicitudAporteRescateNormal.p_IdModulo = entrada.p_IdModulo;
					entradaCrearSolicitudAporteRescateNormal.p_SimularOperacion = _c.SI;
					entradaCrearSolicitudAporteRescateNormal.p_UsaReglasNegocio = _c.SI;
					entradaCrearSolicitudAporteRescateNormal.p_IdTipoCanalIngreso = _c.CanalAgente;
					entradaCrearSolicitudAporteRescateNormal.p_NumeroDividendo = 0L;
					
					SalidaCrearSolicitudAporteRescateNormal salidaSimularSolicitudAporteRescateNormal = crearSolicitudAporteRescateNormal.logica(con, entradaCrearSolicitudAporteRescateNormal);
					for(MensajeSalida mje : salidaSimularSolicitudAporteRescateNormal.P_OUT_MENSAJES){
						if(MensajeSalida.MENSAJE_ERROR.equals(mje.tipo)){
							out.P_OUT_MENSAJES.add(mje);
							Long id = Math.abs(mje.id);
							 if (salidaSimularSolicitudAporteRescateNormal.P_OUT_ERRORESCONTADOR != null){out.P_OUT_ERRORESCONTADOR += salidaSimularSolicitudAporteRescateNormal.P_OUT_ERRORESCONTADOR;};
							for(DetalleTraspasoOrigen sol : traspasosDetalleOrigen){
								if(sol.IdDetalleTraspaso.longValue() == id.longValue()){
									sol.EstadoProceso = _c.EstadoRechazado;
									sol.Codigo = mje.codigo;
									continue;
								}
							}
							
						}
					}
					if (out.P_OUT_ERRORESCONTADOR <=0){
						
							EntradaCrearSolicitudAporteRescateNormal entradaFFMM = new EntradaCrearSolicitudAporteRescateNormal();
							entradaFFMM = Utils.copyPojo(entradaCrearSolicitudAporteRescateNormal, entradaFFMM);
							entradaFFMM.p_SimularOperacion=_c.NO;
							SalidaCrearSolicitudAporteRescateNormal salidaCrearSolicitudAporteRescateNormal = crearSolicitudAporteRescateNormal.logica(con, entradaFFMM);
							for(MensajeSalida mje : salidaCrearSolicitudAporteRescateNormal.P_OUT_MENSAJES){
								if(MensajeSalida.MENSAJE_ERROR.equals(mje.tipo)){
									out.P_OUT_MENSAJES.add(mje);
									Long id = Math.abs(mje.id);
									 if (salidaCrearSolicitudAporteRescateNormal.P_OUT_ERRORESCONTADOR != null){out.P_OUT_ERRORESCONTADOR += salidaCrearSolicitudAporteRescateNormal.P_OUT_ERRORESCONTADOR;};
									for(DetalleTraspasoOrigen sol : traspasosDetalleOrigen){
										if(sol.IdDetalleTraspaso.longValue() == id.longValue()){
											sol.EstadoProceso = _c.EstadoRechazado;
											sol.Codigo = mje.codigo;
											break;
										}
									}
								}
							}
							
							for(ResultadoSolicitud res : salidaCrearSolicitudAporteRescateNormal.P_OUT_RESULTADO){
								for(DetalleTraspasoOrigen sol : traspasosDetalleOrigen){
									if(sol.IdDetalleTraspaso.longValue() == res.IdInterno.longValue()){
										sol.IdSolicitudAporteRescate = res.IdSolicitudAporteRescate;
										sol.EstadoProceso = _c.EstadoProcesado;
										sol.FechaAceptacion= Fecha.toDate(fechaSistema);
										sol.Certifica = _c.SI;
										break;
									}
								}
							}
							//actualizar tabla de traspasos
							List<DetalleTraspasoOrigen> detalleProcesada = new ArrayList<DetalleTraspasoOrigen>();
							List<DetalleTraspasoOrigen> detalleInvalida = new ArrayList<DetalleTraspasoOrigen>();
							for(DetalleTraspasoOrigen sol : traspasosDetalleOrigen){
								
								if (_c.EstadoRechazado.equalsIgnoreCase(sol.EstadoProceso)){
									detalleInvalida.add(sol);
									
								}else{
									
									detalleProcesada.add(sol);	
								};
							}
							
							
							if(detalleProcesada.size() > 0){
								sql.batch(con, SQL_TRASPASO_DETALLE_ORIGEN_UPDATE, detalleProcesada, DetalleTraspasoOrigen.class);
								sql.batch(con, SQL_TRASPASO_UPDATE, detalleProcesada, DetalleTraspasoOrigen.class);
							}
					}	
					
				}
				

				
				
				
			}
		
			
		
			
		}//procesar
		
	
		
		
	

		return out;
	}
	/********************
	 * Metodo Principal *
	 ********************/

	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();	
				
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":6,\"p_IdModulo\":\"FMM\",\"p_Traspaso\":[{\"IdTraspaso\":223}]}";
				
				EntradaConfirmarTraspaso Objin_json = gson.fromJson(json, EntradaConfirmarTraspaso.class);
				
				ConfirmarTraspaso demo = new ConfirmarTraspaso();	
				
	 			System.out.println("ENTRADA");
	 			System.out.println(Objin_json.toString());
	 			System.out.println("SALIDA");

	 			try { 
	 				SalidaConfirmarTraspaso ObjOut = demo.logica(con, Objin_json);
	 				con.commit();
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