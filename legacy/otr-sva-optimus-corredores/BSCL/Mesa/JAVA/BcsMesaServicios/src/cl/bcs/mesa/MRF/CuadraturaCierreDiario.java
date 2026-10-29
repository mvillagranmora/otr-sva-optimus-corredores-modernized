package cl.bcs.mesa.MRF;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.contabilidad.CentralizaMultiSeleccion;
import cl.bcs.contabilidad.CentralizaMultiSeleccion.EntradaCentralizaMultiSeleccion;
import cl.bcs.contabilidad.CentralizaMultiSeleccion.InTiposCentralizacion;
import cl.bcs.contabilidad.CentralizaMultiSeleccion.SalidaCentralizaMultiSeleccion;
import cl.bcs.contabilidad.pojo.ResultadoCentralizacion;
import cl.bcs.mesa.MRF.CuadraturaCierreDiario.CuadraturaCierreDiarioEntrada;
import cl.bcs.mesa.MRF.CuadraturaCierreDiario.CuadraturaCierreDiarioSalida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;

public class CuadraturaCierreDiario implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(CuadraturaCierreDiario.class);
	
	private final String SQL_SELECT_CUADRATURADIARIAOPERACIONES		 	= loadResourceToString("cl/bcs/mesa/MRF/sql/CuadraturaCierreDiario_SelectCuadraturaDiariaOperaciones.sql");
	private final String SQL_SELECT_OPERACIONES 						= loadResourceToString("cl/bcs/mesa/MRF/sql/CuadraturaCierreDiario_SelectOperaciones.sql");
	private final String SQL_SELECT_INFORMES		 					= loadResourceToString("cl/bcs/mesa/MRF/sql/CuadraturaCierreDiario_SelectInformes.sql");
	private final String SQL_DELETE_CUADRATURADIARIAOPERACIONES 		= loadResourceToString("cl/bcs/mesa/MRF/sql/CuadraturaCierreDiario_DeleteCuadraturaDiariaOperaciones.sql");
	private final String SQL_INSERT_CUADRATURADIARIAOPERACIONES 		= loadResourceToString("cl/bcs/mesa/MRF/sql/CuadraturaCierreDiario_InsertCuadraturaDiariaOperaciones.sql");
	private final String SQL_SELECT_DETALLE_CUADRATURADIARIAOPERACIONES = loadResourceToString("cl/bcs/mesa/MRF/sql/CuadraturaCierreDiario_SelectJSONCuadraturaDiariaOperaciones.sql");
	

	private final String SQL_SELECT_PACTOS_VIGENTE 						= loadResourceToString("cl/bcs/mesa/MRF/sql/CuadraturaCierreDiario_SelectPactosVigentes.sql");//cualquiera -- CABECERA
	private final String SQL_SELECT_PACTOS_DETALLE_VIGENTES 			= loadResourceToString("cl/bcs/mesa/MRF/sql/CuadraturaCierreDiario_SelectPactosDetallesVigentes.sql"); //cualquiera --papeles
	private final String SQL_SELECT_PACTOS_ANTICIPO 					= loadResourceToString("cl/bcs/mesa/MRF/sql/CuadraturaCierreDiario_SelectPactosAnticipo.sql");  
	private final String SQL_SELECT_PACTOS_VENCIMIENTO 					= loadResourceToString("cl/bcs/mesa/MRF/sql/CuadraturaCierreDiario_SelectPactosVencimiento.sql");
	private final String SQL_SELECT_COMPRA_TERMINO 						= loadResourceToString("cl/bcs/mesa/MRF/sql/CuadraturaCierreDiario_SelectTerminoCompra.sql");  //dia - papeles
	private final String SQL_SELECT_VENTA_TERMINO 						= loadResourceToString("cl/bcs/mesa/MRF/sql/CuadraturaCierreDiario_SelectTerminoVenta.sql");    //dia -paples	
	private final String SQL_SELECT_MAYOR_MENOR_VALOR_RF 				= loadResourceToString("cl/bcs/mesa/MRF/sql/CuadraturaCierreDiario_SelectMenorMayorValorRF.sql");  // Cualquier - Carteras RF , IF
	private final String SQL_SELECT_PACTOS_COMPRA_VENTA 				= loadResourceToString("cl/bcs/mesa/MRF/sql/CuadraturaCierreDiario_SelectPactosCompraVenta.sql");    //dia -papeles- cabecera
	//private final String SQL_SELECT_TERMINO_VIGENTE 					= loadResourceToString("cl/bcs/mesa/MRF/sql/CuadraturaCierreDiario_SelectTerminoVigente.sql");
	
	private final String SQL_SELECT_CARTERAS		 					= loadResourceToString("cl/bcs/mesa/MRF/sql/CuadraturaCierreDiario_SelectCarteras.sql");
	private final String SQL_SELECT_CARTERASMRF		 					= loadResourceToString("cl/bcs/mesa/MRF/sql/CuadraturaCierreDiario_SelectCarterasMRF.sql");
	private final String SQL_SELECT_OPERACIONPACTO	 					= loadResourceToString("cl/bcs/mesa/MRF/sql/CuadraturaCierreDiario_SelectOperacionPacto.sql");
	private final String SQL_SELECT_MOVCTAINVERSION	 					= loadResourceToString("cl/bcs/mesa/MRF/sql/CuadraturaCierreDiario_SelectMovCtaInversion.sql");
	private final String SQL_SELECT_MOVCOMTESORERIA	 					= loadResourceToString("cl/bcs/mesa/MRF/sql/CuadraturaCierreDiario_SelectMovComTesoreria.sql");
	private final String SQL_SELECT_CARTERACONSOLIDADA 					= loadResourceToString("cl/bcs/mesa/MRF/sql/CuadraturaCierreDiario_SelectCarteraConsolidada.sql");
	
	
	
	
	
	public static class CuadraturaDiariaObj{
		public String IdCuadraturaDiariaOperaciones;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CuadraturaCierreDiarioEntrada {
		public Long p_IdUsuario;
		public Long p_IdEmpresa;
		public String p_IdModulo;
		public Date p_FechaInicio;
		public List<CuadraturaDiariaObj> p_IdProceso;
		public String p_TipoLlamado;
		
		public CuadraturaCierreDiarioEntrada() {
			this.p_IdProceso = new ArrayList<CuadraturaDiariaObj>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DetalleResumenJSON{
		public String IdCuadraturaDiariaOperaciones;
		public String DetalleCuadraturaJSON;
		
		public String toString() {
			return Utils.printJson(this, false);
		}		
	}
	
	public static class Resumen{
		public String IdCuadraturaDiariaOperaciones;
		public Long IdEmpresa;
		public String IdClasificacionTipoInstrumento;
		public String OperacionCuadraturaDiariaOperaciones;
		public Date FechaEjecucion;
		public Long TotalOperaciones;
		public Long OperacionesConProblemas;
		public String TipoOperacion;
		public String Resultado;
		public Long Orden;
		public String DetalleCuadraturaJSON;
		public Long IdUsuario;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Operaciones{
		public Long IdOperacion; 
		public Long FolioOperacion;     
		public String RazonSocial;     
		public BigDecimal MontoOperacion;
		public BigDecimal MontoOperacionMoneda;
		public String IdMoneda;
		public String IdMonedaLiquidacion;
		public Long IdMesaOrigen;
		public String IdEstado;
		public String Descripcion;
		public String TipoInformeGenerado="";
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Carteras{
		public String IdClasificacionTipoInstrumento;
		public BigDecimal INGCantidad;
		public BigDecimal INGValorRescate;
		public BigDecimal INGMontoATasaCompraActual;
		public BigDecimal INGMontoCompraActualMonedaInstrumento;
		public BigDecimal INGMontoATasaMercadoActual;
		public BigDecimal INGMontoMercadoActualMonedaInstrumento;
		public BigDecimal InteresCorridoMonedaInstrumento;
		public BigDecimal ReajusteCorridoMonedaInstrumento;
		public String IdEstado;
		public String Descripcion;
		
		public String RazonSocial; 
		public Long IdOperacion; 
		public Long FolioOperacion;
		public BigDecimal MontoOperacion;
		public BigDecimal MontoOperacionMoneda;
		public String IdMoneda;
		public String IdMonedaLiquidacion; 
		 
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CarterasMRF{
			
		public BigDecimal CorteMinimo;
		public BigDecimal MontoCompra;
		public BigDecimal ValorCompra;
		public BigDecimal MontoMercado;
		public BigDecimal ValorMercado;
		public BigDecimal InteresDiario;
		
		
		public String IdEstado;
		public String Descripcion;
		
		public String RazonSocial; 
		public Long IdOperacion; 
		public Long FolioOperacion;
		public BigDecimal MontoOperacion;
		public BigDecimal MontoOperacionMoneda;
		public String IdMoneda;
		public String IdMonedaLiquidacion; 
		 
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	public static class OperacionPacto{
		
		public BigDecimal ValorOperacion;
		public BigDecimal MontoCompromiso;
		public BigDecimal ValorCompromiso;
		public BigDecimal Intereses;
				
		public String IdEstado;
		public String Descripcion;
		
		public String RazonSocial; 
		public Long IdOperacion; 
		public Long FolioOperacion;
		public BigDecimal MontoOperacion;
		public BigDecimal MontoOperacionMoneda;
		public String IdMoneda;
		public String IdMonedaLiquidacion; 
		 
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class MovCtaInversion{
		
		public String IdEstadoInv;
		public String Validacion;
		
		
		public String IdEstado;
		public String Descripcion;
		
		public String RazonSocial; 
		public Long IdOperacion; 
		public Long FolioOperacion;
		public BigDecimal MontoOperacion;
		public BigDecimal MontoOperacionMoneda;
		public String IdMoneda;
		public String IdMonedaLiquidacion; 
		 
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class MovComTesoreria{
		
		public String IdEstadoTes;
		public String Validacion;
		
		
		public String IdEstado;
		public String Descripcion;
		
		public String RazonSocial; 
		public Long IdOperacion; 
		public Long FolioOperacion;
		public BigDecimal MontoOperacion;
		public BigDecimal MontoOperacionMoneda;
		public String IdMoneda;
		public String IdMonedaLiquidacion; 
		 
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CarteraConsolidada{
		
		public BigDecimal  MontoCompra;
		public BigDecimal ValorCompraHoy;
		public BigDecimal Interes;
		public BigDecimal InteresAcumulado;
		public BigDecimal ValorMercado;
		public BigDecimal InteresDevengado;
		
		public String IdEstado;
		public String Descripcion;
		
		public String RazonSocial; 
		public Long IdOperacion; 
		public Long FolioOperacion;
		public BigDecimal MontoOperacion;
		public BigDecimal MontoOperacionMoneda;
		public String IdMoneda;
		public String IdMonedaLiquidacion; 
		public String IdMesaOrigen; 
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	
	public static class objCuadraturaDiariaOperaciones{
		public Long IdEmpresa; 
		public String IdCuadraturaDiariaOperaciones;
		public Date FechaEjecucion;
		public Long TotalOperaciones; 
		public Long OperacionesConProblemas;
		public String Resultado;
		public String DetalleCuadraturaJson;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CuadraturaCierreDiarioSalida {
		public List<MensajeSalida>			P_OUT_MENSAJES;
		public Long							P_OUT_ERRORESCONTADOR;
		public String						P_OUT_DETALLE_CUADRATURA;
		
		public CuadraturaCierreDiarioSalida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_DETALLE_CUADRATURA="";
			this.P_OUT_ERRORESCONTADOR=0L;
		}
		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	/*
	 * Objeto de transporte
	 */
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		CuadraturaCierreDiarioEntrada in = gson.fromJson(jin.toString(), CuadraturaCierreDiarioEntrada.class);
		CuadraturaCierreDiarioSalida out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public CuadraturaCierreDiarioSalida logica(Connection conn, CuadraturaCierreDiarioEntrada in) throws Exception {
		SQL _sql = new SQL();
		CuadraturaCierreDiarioSalida out = new CuadraturaCierreDiarioSalida();
		out.P_OUT_ERRORESCONTADOR = 0L;
		logger.info("INICIO");
		
		try {
			//Modo Proceso
			if (in.p_TipoLlamado.equals("Proceso")){
				Long Id = 0L;
				HashMap<String,String> mapMsg = new HashMap<>();
				List<String> idOper = new ArrayList<>();
				for (CuadraturaDiariaObj obj: in.p_IdProceso){
					idOper.add(obj.IdCuadraturaDiariaOperaciones);
				}
				HashMap<String,Object> regResumen = new HashMap<>();
				regResumen.put("IdEmpresa", in.p_IdEmpresa);
				//regResumen.put("IdCuadraturaDiariaOperaciones", idOper);
				List<Resumen> rsResumen = _sql.query(conn, SQL_SELECT_CUADRATURADIARIAOPERACIONES, regResumen, Resumen.class);
				
				HashMap<String,Object> HashResumen = Utils.List2Hash(rsResumen, "IdCuadraturaDiariaOperaciones");
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
				
				
				List<Operaciones> certificacionesPendientes = new ArrayList<Operaciones>();
				List<Operaciones> informesPendientes = new ArrayList<Operaciones>();
				List<Carteras> carterasPendientes = new ArrayList<Carteras>();
				List<CarterasMRF> carterasMRFPendientes = new ArrayList<CarterasMRF>();
				List<OperacionPacto> OperacionPactoPendientes = new ArrayList<OperacionPacto>();
				List<MovCtaInversion> MovCtaInversionPendientes = new ArrayList<MovCtaInversion>();
				List<MovComTesoreria> MovComTesoreriaPendientes = new ArrayList<MovComTesoreria>();
				List<CarteraConsolidada> CarteraConsolidadaPendientes = new ArrayList<CarteraConsolidada>();
				
				
				
				
				List<objCuadraturaDiariaOperaciones> parametrosparaBorrar = new ArrayList<>();
				
				HashMap<String, Object> params = new HashMap<String, Object>();
				params.put("IdEmpresa", in.p_IdEmpresa);
				params.put("Fecha", in.p_FechaInicio);
	
				//Verificar si es necesario simular la centralizacion
				boolean requiereCentralizar = false;    
				for (CuadraturaDiariaObj objProc: in.p_IdProceso){
					
					
					if ( "CUAPACINTREA".equalsIgnoreCase(objProc.IdCuadraturaDiariaOperaciones) || "CUAPACINTREADET".equalsIgnoreCase(objProc.IdCuadraturaDiariaOperaciones)  || "CUAMAYMENVAL".equalsIgnoreCase(objProc.IdCuadraturaDiariaOperaciones)
					 ||  "CUATERVEN".equalsIgnoreCase(objProc.IdCuadraturaDiariaOperaciones)    || "CUATERCOM".equalsIgnoreCase(objProc.IdCuadraturaDiariaOperaciones)        || "CUAPACTOS".equalsIgnoreCase(objProc.IdCuadraturaDiariaOperaciones) 
					 ||  "CUAPACANT".equalsIgnoreCase(objProc.IdCuadraturaDiariaOperaciones)    || "CUAPACVEN".equalsIgnoreCase(objProc.IdCuadraturaDiariaOperaciones) 
						){requiereCentralizar = true;}
				}
				
				
				//Listas movimientos de la centralizacion				
				List<ResultadoCentralizacion> Lista240 = new ArrayList<>();
				List<ResultadoCentralizacion> Lista225 = new ArrayList<>();
				List<ResultadoCentralizacion> Lista250 = new ArrayList<>();
				List<ResultadoCentralizacion> Lista230 = new ArrayList<>();
				List<ResultadoCentralizacion> Lista235 = new ArrayList<>();
				List<ResultadoCentralizacion> Lista241 = new ArrayList<>();
				List<ResultadoCentralizacion> Lista242 = new ArrayList<>();
				List<ResultadoCentralizacion> Lista243 = new ArrayList<>();
				//List<ResultadoCentralizacion> Lista353 = new ArrayList<>();
				
				if(requiereCentralizar==true){
					
				
						//Simular Centralizaciones 
						CentralizaMultiSeleccion centralizar = new CentralizaMultiSeleccion();
						EntradaCentralizaMultiSeleccion InCentralizacion = new EntradaCentralizaMultiSeleccion();
						
						//Centralizaciones pedidas
						List<InTiposCentralizacion> TiposCentralizaciones = new ArrayList<>();
						TiposCentralizaciones.add(new InTiposCentralizacion(225L));
						TiposCentralizaciones.add(new InTiposCentralizacion(250L));
						TiposCentralizaciones.add(new InTiposCentralizacion(230L));
						TiposCentralizaciones.add(new InTiposCentralizacion(235L));
						TiposCentralizaciones.add(new InTiposCentralizacion(241L));
						TiposCentralizaciones.add(new InTiposCentralizacion(242L));
						TiposCentralizaciones.add(new InTiposCentralizacion(243L));				
						TiposCentralizaciones.add(new InTiposCentralizacion(240L));
		//				TiposCentralizaciones.add(new InTiposCentralizacion(353L));
						
						InCentralizacion.p_IdEmpresa=in.p_IdEmpresa;
						InCentralizacion.p_IdModulo=in.p_IdModulo;
						InCentralizacion.p_IdUsuario=in.p_IdUsuario;
						InCentralizacion.p_FechaContable = "2018-10-22";
						InCentralizacion.p_SimulaOperacion="S";
						InCentralizacion.p_Ejercicio=2018L;
						InCentralizacion.p_in_TiposCentralizacion= TiposCentralizaciones;				
						
						SalidaCentralizaMultiSeleccion  OutCentralizacion = centralizar.logica(conn, InCentralizacion) ;				
						List<ResultadoCentralizacion> ListaCentralizaciones = OutCentralizacion.P_OUT_RESULTADO;
						

						
						for(ResultadoCentralizacion obj : ListaCentralizaciones) {
							switch (obj.IdTipoCentralizacion.intValue()) {					
							case 240 :
								Lista240.add(obj);
								break;
							case 225 :
								Lista225.add(obj);
								break;
							case 250 :
								Lista250.add(obj);
								break;
							case 230 :
								Lista230.add(obj);
								break;
							case 235 :
								Lista235.add(obj);
								break;
							case 241 :
								Lista241.add(obj);
								break;
							case 242 :
								Lista242.add(obj);
								break;
							case 243 :
								Lista243.add(obj);
								break;
							/*	
							case 353 :
								Lista353.add(obj);
								break;		*/				
							}				
						}
				};
				
				List<Resumen> ResumenCuadratura = new ArrayList<>();
			
				for (CuadraturaDiariaObj objProc: in.p_IdProceso){
					Resumen ObjResumen = new Resumen();
					objCuadraturaDiariaOperaciones oDelete = new objCuadraturaDiariaOperaciones();
					String vResultado = new String();
					
					switch (objProc.IdCuadraturaDiariaOperaciones) {
						case "CUAMOVPEN":System.out.println("== Mov. Pendientes ==");
							params.clear();
							params.put("IdEmpresa", in.p_IdEmpresa);
							params.put("Fecha", in.p_FechaInicio);
							List<Operaciones> ope = _sql.query(conn, SQL_SELECT_OPERACIONES, params, Operaciones.class);
							certificacionesPendientes = buscarDescuadresCertificacion(ope);
							
							if (ope.size()==0){
								vResultado = "UI_SIN_OPERACIONES";
							}
							else if(certificacionesPendientes.size() == 0){
								vResultado = "UI_CUADRADO";
							}else {
								vResultado = "UI_DESCUADRADO";
							}
							
							Resumen temp =(Resumen)HashResumen.get(objProc.IdCuadraturaDiariaOperaciones);
							
							temp.FechaEjecucion = in.p_FechaInicio;
							temp.TotalOperaciones = (long)ope.size(); 
							temp.OperacionesConProblemas = (long)certificacionesPendientes.size();
							temp.Resultado = vResultado;
							temp.DetalleCuadraturaJSON = gson.toJson(certificacionesPendientes);						
							
							ObjResumen=temp;
							
							break;
						case "CUAINFOPEN":System.out.println("== Inf. Pendientes ==");
							params.clear();
							params.put("IdEmpresa", in.p_IdEmpresa);
							params.put("Fecha", in.p_FechaInicio);
							List<Operaciones> info = _sql.query(conn, SQL_SELECT_INFORMES, params, Operaciones.class);
							informesPendientes = buscarDescuadresMandatos(info);
							if (info.size()==0){
								vResultado = "UI_SIN_OPERACIONES";
							}
							else if(informesPendientes.size() == 0){
								vResultado = "UI_CUADRADO";
							}else {
								vResultado = "UI_DESCUADRADO";
							}
							Resumen temp2 =(Resumen)HashResumen.get(objProc.IdCuadraturaDiariaOperaciones);
							temp2.FechaEjecucion = in.p_FechaInicio;
							temp2.TotalOperaciones = (long)info.size(); 
							temp2.OperacionesConProblemas = (long)informesPendientes.size();
							temp2.Resultado = vResultado;
							temp2.DetalleCuadraturaJSON = gson.toJson(informesPendientes);						
							ObjResumen=temp2;
							break;
						case "CUACARCUS":System.out.println("== Valores. cartera ==");
							params.clear();
							params.put("IdEmpresa", in.p_IdEmpresa);
							params.put("Fecha", in.p_FechaInicio);
							List<Carteras> cart = _sql.query(conn, SQL_SELECT_CARTERAS, params, Carteras.class);
							carterasPendientes = buscarDescuadresCarteras(cart);
							if (cart.size()==0){
								vResultado = "UI_SIN_CARTERAS";
							}
							else if(carterasPendientes.size() == 0){
								vResultado = "UI_CUADRADO";
							}else {
								vResultado = "UI_DESCUADRADO";
							}
							Resumen temp3 =(Resumen)HashResumen.get(objProc.IdCuadraturaDiariaOperaciones);
							temp3.FechaEjecucion = in.p_FechaInicio;
							temp3.TotalOperaciones = (long)cart.size(); 
							temp3.OperacionesConProblemas = (long)carterasPendientes.size();
							temp3.Resultado = vResultado;
							temp3.DetalleCuadraturaJSON = gson.toJson(carterasPendientes);						
							ObjResumen=temp3;
							break;
							
						case "CUACARMRF":System.out.println("== Valores. cartera  MRF==");
							params.clear();
							params.put("IdEmpresa", in.p_IdEmpresa);
							params.put("Fecha", in.p_FechaInicio);
							List<CarterasMRF> cartRF = _sql.query(conn, SQL_SELECT_CARTERASMRF, params, CarterasMRF.class);
							carterasMRFPendientes = buscarDescuadresCarterasMRF(cartRF);
							if (cartRF.size()==0){
								vResultado = "UI_SIN_CARTERAS";
							}
							else if(carterasMRFPendientes.size() == 0){
								vResultado = "UI_CUADRADO";
							}else {
								vResultado = "UI_DESCUADRADO";
							}
							Resumen temp4 =(Resumen)HashResumen.get(objProc.IdCuadraturaDiariaOperaciones);
							temp4.FechaEjecucion = in.p_FechaInicio;
							temp4.TotalOperaciones = (long)cartRF.size(); 
							temp4.OperacionesConProblemas = (long)carterasMRFPendientes.size();
							temp4.Resultado = vResultado;
							temp4.DetalleCuadraturaJSON = gson.toJson(carterasMRFPendientes);						
							ObjResumen=temp4;
							break;								
								
						case "CUAOPEPAC":System.out.println("== Valores. oper. pactos diario ==");
							params.clear();
							params.put("IdEmpresa", in.p_IdEmpresa);
							params.put("Fecha", in.p_FechaInicio);
							List<OperacionPacto> operPac = _sql.query(conn, SQL_SELECT_OPERACIONPACTO, params, OperacionPacto.class);
							OperacionPactoPendientes = buscarDescuadresValorOperPacto(operPac);
							if (operPac.size()==0){
								vResultado = "UI_SIN_PACTOS";
							}
							else if(OperacionPactoPendientes.size() == 0){
								vResultado = "UI_CUADRADO";
							}else {
								vResultado = "UI_DESCUADRADO";
							}
							Resumen temp5 =(Resumen)HashResumen.get(objProc.IdCuadraturaDiariaOperaciones);
							temp5.FechaEjecucion = in.p_FechaInicio;
							temp5.TotalOperaciones = (long)operPac.size(); 
							temp5.OperacionesConProblemas = (long)OperacionPactoPendientes.size();
							temp5.Resultado = vResultado;
							temp5.DetalleCuadraturaJSON = gson.toJson(OperacionPactoPendientes);						
							ObjResumen=temp5;
							break;								
							
						case "CUACTAINV":System.out.println("== Cta. Inversion ==");
							params.clear();
							params.put("IdEmpresa", in.p_IdEmpresa);
							params.put("Fecha", in.p_FechaInicio);
							List<MovCtaInversion> ctaInv = _sql.query(conn, SQL_SELECT_MOVCTAINVERSION, params, MovCtaInversion.class);
							MovCtaInversionPendientes = buscarDescuadresEstadoCtaInversion(ctaInv);
							if (ctaInv.size()==0){
								vResultado = "UI_SIN_OPERACIONES";
							}
							else if(MovCtaInversionPendientes.size() == 0){
								vResultado = "UI_CUADRADO";
							}else {
								vResultado = "UI_DESCUADRADO";
							}
							Resumen temp6 =(Resumen)HashResumen.get(objProc.IdCuadraturaDiariaOperaciones);
							temp6.FechaEjecucion = in.p_FechaInicio;
							temp6.TotalOperaciones = (long)ctaInv.size(); 
							temp6.OperacionesConProblemas = (long)MovCtaInversionPendientes.size();
							temp6.Resultado = vResultado;
							temp6.DetalleCuadraturaJSON = gson.toJson(MovCtaInversionPendientes);						
							ObjResumen=temp6;
							break;		
							
						case "CUACOMTES":System.out.println("== Com. Tesoreria ==");
							params.clear();
							params.put("IdEmpresa", in.p_IdEmpresa);
							params.put("Fecha", in.p_FechaInicio);
							List<MovComTesoreria> comTes = _sql.query(conn, SQL_SELECT_MOVCOMTESORERIA, params, MovComTesoreria.class);
							MovComTesoreriaPendientes = buscarDescuadresEstadoComTesoreria(comTes);
							if (comTes.size()==0){
								vResultado = "UI_SIN_OPERACIONES";
							}
							else if(MovComTesoreriaPendientes.size() == 0){
								vResultado = "UI_CUADRADO";
							}else {
								vResultado = "UI_DESCUADRADO";
							}
							Resumen temp7 =(Resumen)HashResumen.get(objProc.IdCuadraturaDiariaOperaciones);
							temp7.FechaEjecucion = in.p_FechaInicio;
							temp7.TotalOperaciones = (long)comTes.size(); 
							temp7.OperacionesConProblemas = (long)MovComTesoreriaPendientes.size();
							temp7.Resultado = vResultado;
							temp7.DetalleCuadraturaJSON = gson.toJson(MovComTesoreriaPendientes);						
							ObjResumen=temp7;
							break;		
							
						case "CUACARCON":System.out.println("== Cartera Consolidada ==");
							params.clear();
							params.put("IdEmpresa", in.p_IdEmpresa);
							params.put("Fecha", in.p_FechaInicio);
							params.put("IdModulo", in.p_IdModulo);
							params.put("IdUsuario", in.p_IdUsuario);
							
							List<CarteraConsolidada> carCon = _sql.query(conn, SQL_SELECT_CARTERACONSOLIDADA, params, CarteraConsolidada.class);
							
							CarteraConsolidadaPendientes = buscarDescuadresCarteraConsolidada(carCon);
							if (carCon.size()==0){
								vResultado = "UI_SIN_CARTERAS";
							}
							else if(CarteraConsolidadaPendientes.size() == 0){
								vResultado = "UI_CUADRADO";
							}else {
								vResultado = "UI_DESCUADRADO";
							}
							Resumen temp8 =(Resumen)HashResumen.get(objProc.IdCuadraturaDiariaOperaciones);
							temp8.FechaEjecucion = in.p_FechaInicio;
							temp8.TotalOperaciones = (long)carCon.size(); 
							temp8.OperacionesConProblemas = (long)CarteraConsolidadaPendientes.size();
							temp8.Resultado = vResultado;
							temp8.DetalleCuadraturaJSON = gson.toJson(CarteraConsolidadaPendientes);						
							ObjResumen=temp8;
							break;			
							
						
						case "CUAPACINTREA":
							params.clear(); 
							params.put("IdEmpresa", in.p_IdEmpresa);
							List<Operaciones> opePactoVigentes = _sql.query(conn, SQL_SELECT_PACTOS_VIGENTE, params, Operaciones.class);  

							ObjResumen=resumenOperaciones(opePactoVigentes,Lista240,(Resumen)HashResumen.get(objProc.IdCuadraturaDiariaOperaciones),objProc.IdCuadraturaDiariaOperaciones,in.p_FechaInicio);						
							break;
						case "CUAPACINTREADET":
							params.clear();
							params.put("IdEmpresa", in.p_IdEmpresa);
							List<Operaciones> opePactoVigentesPapeles = _sql.query(conn, SQL_SELECT_PACTOS_DETALLE_VIGENTES, params, Operaciones.class);  
				
							ObjResumen=resumenOperaciones(opePactoVigentesPapeles,Lista225,(Resumen)HashResumen.get(objProc.IdCuadraturaDiariaOperaciones),objProc.IdCuadraturaDiariaOperaciones,in.p_FechaInicio);
							break;
						case "CUAPACANT":
							params.clear();
							params.put("IdEmpresa", in.p_IdEmpresa);
							params.put("FechaActual", in.p_FechaInicio);				
							List<Operaciones> opePactoAnticipo = _sql.query(conn, SQL_SELECT_PACTOS_ANTICIPO, params, Operaciones.class);  
					
							ObjResumen=resumenOperaciones(opePactoAnticipo,Lista242,(Resumen)HashResumen.get(objProc.IdCuadraturaDiariaOperaciones),objProc.IdCuadraturaDiariaOperaciones,in.p_FechaInicio);
							break;
						case "CUAPACVEN":
							params.clear();
							params.put("IdEmpresa", in.p_IdEmpresa);
							params.put("FechaActual", in.p_FechaInicio);
							List<Operaciones> opePactoVencimiento = _sql.query(conn, SQL_SELECT_PACTOS_VENCIMIENTO, params, Operaciones.class);  				
													
							ObjResumen=resumenOperaciones(opePactoVencimiento,Lista243,(Resumen)HashResumen.get(objProc.IdCuadraturaDiariaOperaciones),objProc.IdCuadraturaDiariaOperaciones,in.p_FechaInicio);
							break;
						case "CUATERCOM":
							params.clear();
							params.put("IdEmpresa", in.p_IdEmpresa);
							params.put("FechaActual", in.p_FechaInicio);				
							List<Operaciones> opeCompraTermino = _sql.query(conn, SQL_SELECT_COMPRA_TERMINO, params, Operaciones.class);  
												
							ObjResumen=resumenOperaciones(opeCompraTermino,Lista235,(Resumen)HashResumen.get(objProc.IdCuadraturaDiariaOperaciones),objProc.IdCuadraturaDiariaOperaciones,in.p_FechaInicio);
							break;
						case "CUATERVEN":
							params.clear();
							params.put("IdEmpresa", in.p_IdEmpresa);
							params.put("FechaActual", in.p_FechaInicio);
							List<Operaciones> opeVentaTermino = _sql.query(conn, SQL_SELECT_VENTA_TERMINO, params, Operaciones.class);  
											
							ObjResumen=resumenOperaciones(opeVentaTermino,Lista230,(Resumen)HashResumen.get(objProc.IdCuadraturaDiariaOperaciones),objProc.IdCuadraturaDiariaOperaciones,in.p_FechaInicio);
							break;
						case "CUAMAYMENVAL":  
							params.clear();
							params.put("IdEmpresa", in.p_IdEmpresa);
							List<Operaciones> opeMayoMenorValorRF = _sql.query(conn, SQL_SELECT_MAYOR_MENOR_VALOR_RF, params, Operaciones.class); 
												
							ObjResumen=resumenOperaciones(opeMayoMenorValorRF,Lista250,(Resumen)HashResumen.get(objProc.IdCuadraturaDiariaOperaciones),objProc.IdCuadraturaDiariaOperaciones,in.p_FechaInicio);
							break;
						case "CUAPACTOS":
							params.clear();
							params.put("IdEmpresa", in.p_IdEmpresa);
							params.put("FechaActual", in.p_FechaInicio);
							List<Operaciones> opePactoCompraVenta = _sql.query(conn, SQL_SELECT_PACTOS_COMPRA_VENTA, params, Operaciones.class); 
							ObjResumen=resumenOperaciones(opePactoCompraVenta,Lista241,(Resumen)HashResumen.get(objProc.IdCuadraturaDiariaOperaciones),objProc.IdCuadraturaDiariaOperaciones,in.p_FechaInicio);
							break;
						/*	
						case "CUATERINTREA":
							ObjResumen=resumenOperaciones(opeTerminoIntRea,Lista353,(Resumen)HashResumen.get(objProc.IdCuadraturaDiariaOperaciones),objProc.IdCuadraturaDiariaOperaciones,in.p_FechaInicio);
							break;*/
						default:
							break;
					}
					
					ObjResumen.IdUsuario = in.p_IdUsuario;
					ResumenCuadratura.add(ObjResumen);
					oDelete = new objCuadraturaDiariaOperaciones();
					oDelete.IdCuadraturaDiariaOperaciones = objProc.IdCuadraturaDiariaOperaciones;
					
					parametrosparaBorrar.add(oDelete);
				}
				if (parametrosparaBorrar.size() > 0){ 
					_sql.batch(conn, SQL_DELETE_CUADRATURADIARIAOPERACIONES, parametrosparaBorrar, objCuadraturaDiariaOperaciones.class);
					_sql.batch(conn, SQL_INSERT_CUADRATURADIARIAOPERACIONES, ResumenCuadratura, Resumen.class);
				}
				
				out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_OK,"UI_PROCESO_EJECUTADO_EXITO",mapMsg,conn,in.p_IdEmpresa));
				AgregarLog(conn, in);
				
			}else if (in.p_TipoLlamado.equals("Consulta")){
				//Modo consulta
				List<String> idOper = new ArrayList<>();
				for (CuadraturaDiariaObj obj: in.p_IdProceso){
					idOper.add(obj.IdCuadraturaDiariaOperaciones);
				}
				
				HashMap<String, Object> params = new HashMap<>();
				params.put("IdEmpresa", in.p_IdEmpresa);
				params.put("IdCuadraturaDiariaOperaciones", idOper);
				List<DetalleResumenJSON> rsJson = _sql.query(conn, SQL_SELECT_DETALLE_CUADRATURADIARIAOPERACIONES, params, DetalleResumenJSON.class);
				
				if (rsJson.size() != 0) {
					out.P_OUT_DETALLE_CUADRATURA = (rsJson).get(0).DetalleCuadraturaJSON;					
				}
			}
		}catch(Exception e){
			out.P_OUT_ERRORESCONTADOR = 1L;
			logger.error(e);
			throw e;
		}
		
		return out;
	}
	
	public Resumen resumenOperaciones (List<Operaciones> ListaOperaciones,List<ResultadoCentralizacion> ListaCentralizacion,Resumen HashResumen,String IdCuadraturaDiariaOperaciones,Date FechaInicio) {
		
		
		Resumen Objresumen = new Resumen();		
		String vResultado = new String ();
		List<Operaciones> DescuadresCentralizacion = new ArrayList<Operaciones>();
		
		if(IdCuadraturaDiariaOperaciones=="CUATERVEN"|| IdCuadraturaDiariaOperaciones=="CUATERCOM" ) {
			DescuadresCentralizacion = buscarDescuadresCentralizacionFolio(ListaOperaciones,ListaCentralizacion);
		}else if(IdCuadraturaDiariaOperaciones=="CUAPACINTREA"|| IdCuadraturaDiariaOperaciones=="CUAPACINTREADET" || IdCuadraturaDiariaOperaciones=="CUAPACANT"|| IdCuadraturaDiariaOperaciones=="CUAPACVEN" 
				|| IdCuadraturaDiariaOperaciones=="CUAPACVEN" || IdCuadraturaDiariaOperaciones=="CUAPACTOS" ) {
			DescuadresCentralizacion = buscarDescuadresCentralizacionOperacion(ListaOperaciones,ListaCentralizacion);
		}
		
		
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
		
		if(ListaOperaciones.size()==0)
		{ 
			vResultado = "UI_SIN_OPERACIONES";
		}else {
			if(DescuadresCentralizacion.size()>0){
				vResultado = "UI_DESCUADRADO" ;
			}else if(DescuadresCentralizacion.size()==0){
				vResultado = "UI_CUADRADO";
			}
		}
		
		
		Objresumen.IdEmpresa=HashResumen.IdEmpresa;
		Objresumen.IdCuadraturaDiariaOperaciones=HashResumen.IdCuadraturaDiariaOperaciones;
		Objresumen.IdClasificacionTipoInstrumento=HashResumen.IdClasificacionTipoInstrumento;
		Objresumen.OperacionCuadraturaDiariaOperaciones=HashResumen.OperacionCuadraturaDiariaOperaciones;
		Objresumen.TipoOperacion = HashResumen.TipoOperacion;
		Objresumen.Orden= HashResumen.Orden;
		Objresumen.FechaEjecucion = FechaInicio;
		Objresumen.TotalOperaciones = (long)ListaOperaciones.size(); 
		Objresumen.OperacionesConProblemas =(long)DescuadresCentralizacion.size();
		Objresumen.Resultado = vResultado;
		Objresumen.DetalleCuadraturaJSON = gson.toJson(DescuadresCentralizacion);
		
		return Objresumen;
	}
	
	private List<Operaciones> buscarDescuadresCentralizacionFolio(List<Operaciones> ope, List<ResultadoCentralizacion> ListaCentralizacion){
		//revisar como calcular descuadres
		
		List<Operaciones> descuadres = new ArrayList<Operaciones>();	
		
		for(Operaciones objOpe : ope){			
			for (ResultadoCentralizacion objCen : ListaCentralizacion) {
				if(objOpe.FolioOperacion.compareTo(objCen.Numero)!=0 ) {
					objOpe.Descripcion = "UI_ERROR";
					descuadres.add(objOpe);
				}
			}			
		}	
		
		for (ResultadoCentralizacion objCen : ListaCentralizacion) {
			for(Operaciones objOpe : ope){		
				if(objCen.Numero.compareTo(objOpe.FolioOperacion)!=0 ) {
					objOpe.Descripcion = "UI_ERROR";
					descuadres.add(objOpe);
				}
			}
		}
		
		
		return descuadres;
	}
	
	private List<Operaciones> buscarDescuadresCentralizacionOperacion(List<Operaciones> ope, List<ResultadoCentralizacion> ListaCentralizacion){
		//revisar como calcular descuadres
		
		List<Operaciones> descuadres = new ArrayList<Operaciones>();	
		
		for(Operaciones objOpe : ope){			
			for (ResultadoCentralizacion objCen : ListaCentralizacion) {
				if(objOpe.IdOperacion.compareTo(objCen.Numero)!=0 ) {
					objOpe.Descripcion = "UI_ERROR";
					descuadres.add(objOpe);
				}
			}			
		}
		
		for (ResultadoCentralizacion objCen : ListaCentralizacion) {
			for(Operaciones objOpe : ope){		
				if(objCen.Numero.compareTo(objOpe.IdOperacion)!=0 ) {
					objOpe.Descripcion = "UI_ERROR";
					descuadres.add(objOpe);
				}
			}
		}
		
		
		return descuadres;
	}
	
	
	private List<Operaciones> buscarDescuadresCertificacion(List<Operaciones> ope){
		List<Operaciones> descuadres = new ArrayList<Operaciones>();
		for(Operaciones operacion : ope){
			if("PEN".equalsIgnoreCase(operacion.IdEstado)){
				operacion.Descripcion = "UI_PENDIENTE";
				descuadres.add(operacion);
			}
		}
		return descuadres;
	}
	
	private List<Operaciones> buscarDescuadresMandatos(List<Operaciones> ope){
		List<Operaciones> descuadres = new ArrayList<Operaciones>();
		for(Operaciones operacion : ope){
			if("VIG".equalsIgnoreCase(operacion.IdEstado) &&   !"MANDATO".equalsIgnoreCase(operacion.TipoInformeGenerado) ){
				if(!"TODOS".equalsIgnoreCase(operacion.TipoInformeGenerado)) {
					operacion.Descripcion = "UI_PENDIENTE";
					descuadres.add(operacion);
				}
			}
		}
		return descuadres;
	}
	
	private List<Carteras> buscarDescuadresCarteras(List<Carteras> car){
		List<Carteras> descuadres = new ArrayList<Carteras>();
		for(Carteras cartera : car){
			
			List<String> descripcion = new ArrayList<String>();
			
			if (BigDecimal.ZERO.compareTo(cartera.INGCantidad)  == 0)
			{				
				descripcion.add("INGCantidad en 0");
			}
			
			if (BigDecimal.ZERO.compareTo(cartera.INGMontoATasaCompraActual)   == 0)
			{				
				descripcion.add("INGMontoATasaCompraActual en 0");
			}
			
			if (BigDecimal.ZERO.compareTo(cartera.INGMontoCompraActualMonedaInstrumento) == 0)
			{				
				descripcion.add("INGMontoCompraActualMonedaInstrumento en 0");
			}
			
			if (BigDecimal.ZERO.compareTo(cartera.INGMontoATasaMercadoActual) == 0)
			{				
				descripcion.add("INGMontoATasaMercadoActual en 0");
			}
			
			if (BigDecimal.ZERO.compareTo(cartera.INGMontoMercadoActualMonedaInstrumento) == 0)
			{				
				descripcion.add("INGMontoMercadoActualMonedaInstrumento en 0");
			}
			
			if (BigDecimal.ZERO.compareTo(cartera.INGValorRescate)  == 0 &&  !"RF".equalsIgnoreCase(cartera.IdClasificacionTipoInstrumento))
			{				
				descripcion.add("INGValorRescate en 0");
			}
			
			if(!descripcion.isEmpty())  
			{
				cartera.Descripcion = String.join(",", descripcion);				
				descuadres.add(cartera);
			}
		}
		return descuadres;
	}
	
	private List<CarterasMRF> buscarDescuadresCarterasMRF(List<CarterasMRF> car){
		List<CarterasMRF> descuadres = new ArrayList<CarterasMRF>();
		for(CarterasMRF cartera : car){
			
			List<String> descripcion = new ArrayList<String>();
			
			if (BigDecimal.ZERO.compareTo(cartera.CorteMinimo) 	== 0)
			{				
				descripcion.add("CorteMinimo en 0");
			}
			
			if(BigDecimal.ZERO.compareTo(cartera.MontoCompra) == 0) {
				descripcion.add("MontoCompra en 0");
			}
			
			if (BigDecimal.ZERO.compareTo(cartera.ValorCompra)  == 0)
			{
				descripcion.add("ValorCompra en 0");
			}
			
			if(BigDecimal.ZERO.compareTo(cartera.MontoMercado)	== 0) {
				descripcion.add("MontoMercado en 0");
			}
			
			if (BigDecimal.ZERO.compareTo(cartera.ValorMercado) 	== 0)
			{
				descripcion.add("ValorMercado en 0");
			}
			
			if(BigDecimal.ZERO.compareTo(cartera.InteresDiario) == 0) {
				descripcion.add("InteresDiario en 0");
			}
			
			if(!descripcion.isEmpty())  
			{
				cartera.Descripcion = String.join(",", descripcion);				
				descuadres.add(cartera);
			}
						
		}
		return descuadres;
	}
	
	private List<OperacionPacto> buscarDescuadresValorOperPacto(List<OperacionPacto> car){
		List<OperacionPacto> descuadres = new ArrayList<OperacionPacto>();
		
		for(OperacionPacto cartera : car){			
			
			List<String> descripcion = new ArrayList<String>();
			
			if (BigDecimal.ZERO.compareTo(cartera.MontoOperacion) == 0)
			{				
				descripcion.add("MontoOperacion en 0");
			}
			
			if (BigDecimal.ZERO.compareTo(cartera.ValorOperacion) 	== 0)
			{				
				descripcion.add("ValorOperacion en 0");
			}
			
			if (BigDecimal.ZERO.compareTo(cartera.MontoCompromiso) == 0)
			{				
				descripcion.add("MontoCompromiso en 0");
			}
			
			if (BigDecimal.ZERO.compareTo(cartera.ValorCompromiso)== 0)
			{				
				descripcion.add("ValorCompromiso en 0");
			}
			
			if (BigDecimal.ZERO.compareTo(cartera.Intereses) 		== 0)
			{				
				descripcion.add("Intereses en 0");
			}
			
			if(!descripcion.isEmpty())  
			{
				cartera.Descripcion = String.join(",", descripcion);				
				descuadres.add(cartera);
			}
		}
		return descuadres;
	}
	
	
	private List<MovCtaInversion> buscarDescuadresEstadoCtaInversion(List<MovCtaInversion> car){
		List<MovCtaInversion> descuadres = new ArrayList<MovCtaInversion>();
		
		for(MovCtaInversion operacion : car){
			if( 
				"NO_OK".equalsIgnoreCase(operacion.Validacion)
			  ) {
				operacion.Descripcion = "UI_PENDIENTE";
					descuadres.add(operacion);
				}
		}
		return descuadres;
	}
	
	private List<MovComTesoreria> buscarDescuadresEstadoComTesoreria(List<MovComTesoreria> car){
		List<MovComTesoreria> descuadres = new ArrayList<MovComTesoreria>();
		
		for(MovComTesoreria operacion : car){
			if( 
				"NO_OK".equalsIgnoreCase(operacion.Validacion)
			  ) {
				operacion.Descripcion = "UI_PENDIENTE";
					descuadres.add(operacion);
				}
		}
		return descuadres;
	}
	
	
	private List<CarteraConsolidada> buscarDescuadresCarteraConsolidada(List<CarteraConsolidada> car){
		List<CarteraConsolidada> descuadres = new ArrayList<CarteraConsolidada>();
		
		for(CarteraConsolidada cartera : car){			
			
			List<String> descripcion = new ArrayList<String>();
			
			if (BigDecimal.ZERO.compareTo(cartera.MontoCompra) == 0)
			{				
				descripcion.add("MontoCompra en 0");
			}
			
			if (BigDecimal.ZERO.compareTo(cartera.ValorCompraHoy) 	== 0)
			{				
				descripcion.add("ValorCompraHoy en 0");
			}
			
			if (BigDecimal.ZERO.compareTo(cartera.Interes) 	== 0)
			{				
				descripcion.add("Interes en 0");
			}
			
			if (BigDecimal.ZERO.compareTo(cartera.ValorMercado) == 0)
			{				
				descripcion.add("ValorMercado en 0");
			}
			
			if (BigDecimal.ZERO.compareTo(cartera.InteresDevengado)	== 0)
			{				
				descripcion.add("InteresDevengado en 0");
			}
			
			if ((BigDecimal.ZERO.compareTo(cartera.Interes) == 0) && (BigDecimal.ZERO.compareTo(cartera.InteresDevengado)	== 0))
			{				
				descripcion.add("Sin variaciones PH vs PM");
			}
			
			if(!descripcion.isEmpty())  
			{
				cartera.Descripcion = String.join(",", descripcion);				
				descuadres.add(cartera);
			}
		}
		return descuadres;
	}
	
	public void AgregarLog(Connection con, CuadraturaCierreDiarioEntrada in) {
		try {
			String FechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			Long IdCabeceraLog =  in.p_IdUsuario;		
			//CABECERA
			AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
				
			log.IdCabeceraLog = IdCabeceraLog;
			log.IdModulo = "MRF";
			log.IdUsuario = in.p_IdUsuario;
			log.IdEmpresa = in.p_IdEmpresa;
			log.FechaSistemaLog = FechaSistema;
			log.IdUsuarioAtendido = 0L;
			log.IdCliente = 0L;
			log.EstadoCliente = "";
			log.NombreCliente = "";
			log.IdTipoDocumentoIdentidad = "";
			log.IdDocumentoIdentidad = "";
			log.IdPortafolio = 0L;
			log.EstadoPortafolio = "";
			log.IdTipoRegistro = "CUADRATURACIERREDIARIO";
			log.EstadoRegistro = "";
			log.IdTipoOperacion = "";
			log.Folio = 0L;
			log.Cantidad = BigDecimal.ZERO;
			log.PrecioTasa = BigDecimal.ZERO;
			log.Monto = BigDecimal.ZERO;
			log.IdMonedaOrigen = "";
			log.IdMonedaLiquidacion = "";
			log.TipoCambio = "";
			log.Nemo = "";
			log.IdProceso = 0L;
			log.EstadoProceso = "ING";
			log.IdPersona = 0L;
			log.IdTipoLog = "INFORMATIVO";
			log.NombreProceso = "";
			//AÑADIR AL LOG
			entradaAgregarLog.p_Log.add(log);
			//REGISTRO
			AgregarLog agregarLogService = new AgregarLog();
			agregarLogService.logica(con, entradaAgregarLog);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}	
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdUsuario\":74252,\"p_IdEmpresa\":1,\"p_IdModulo\":\"MES\",\"p_FechaInicio\":\"2023-09-15\",\"p_IdProceso\":[{\"IdCuadraturaDiariaOperaciones\":\"CUACARMRF\"}],\"p_TipoLlamado\":\"Proceso\"}";
//			String json = "{\"p_IdUsuario\":21,\"p_IdEmpresa\":1,\"p_IdModulo\":\"MES\",\"p_FechaInicio\":\"2023-09-15\",\"p_IdProceso\":["
//					
//					+ "{\"IdCuadraturaDiariaOperaciones\":\"CUAMOVPEN\"},"
//					
//					+ "{\"IdCuadraturaDiariaOperaciones\":\"CUAPACINTREA\"},"
//					+ "{\"IdCuadraturaDiariaOperaciones\":\"CUAPACINTREADET\"},"
//					+ "{\"IdCuadraturaDiariaOperaciones\":\"CUAPACANT\"},"
//					+ "{\"IdCuadraturaDiariaOperaciones\":\"CUAPACVEN\"},"
//					+ "{\"IdCuadraturaDiariaOperaciones\":\"CUATERVEN\"},"
//					+ "{\"IdCuadraturaDiariaOperaciones\":\"CUATERCOM\"},"
//					+ "{\"IdCuadraturaDiariaOperaciones\":\"CUAMAYMENVAL\"},"
//					+ "{\"IdCuadraturaDiariaOperaciones\":\"CUAPACTOS\"},"	
//					
//					+ "{\"IdCuadraturaDiariaOperaciones\":\"CUAINFOPEN\"},"
//					+ "{\"IdCuadraturaDiariaOperaciones\":\"CUACARCUS\"},"
//					+ "{\"IdCuadraturaDiariaOperaciones\":\"CUACARMRF\"},"
//					+ "{\"IdCuadraturaDiariaOperaciones\":\"CUAOPEPAC\"},"
//					+ "{\"IdCuadraturaDiariaOperaciones\":\"CUACTAINV\"},"
//					+ "{\"IdCuadraturaDiariaOperaciones\":\"CUACOMTES\"},"
//					+ "{\"IdCuadraturaDiariaOperaciones\":\"CUACARCON\"}"
//					
//					
//					+ "],\"p_TipoLlamado\":\"Proceso\"}";
			
			CuadraturaCierreDiarioEntrada Objin = gson.fromJson(json, CuadraturaCierreDiarioEntrada.class);
			
			CuadraturaCierreDiario demo=new CuadraturaCierreDiario();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				CuadraturaCierreDiarioSalida ObjOut= demo.logica(con, Objin);
 				con.commit();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				//er.printStackTrace();	
				con.rollback();
			}
 			//con.commit();
 			
	    	}
		 	
		System.out.println("== END ==");
	}

}
