package cl.bcs.custodia.evc;

import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.evc.NotificarEventoCapital.NotificarEventoCapital_Entrada;
import cl.bcs.custodia.evc.NotificarEventoCapital.NotificarEventoCapital_Salida;
import cl.bcs.custodia.evc.NotificarEventoCapital.ObjEventoCapitalCliente_Entrada;
import cl.bcs.custodia.evc.NotificarEventoCapital.ObjEventoCapital_Entrada;
import cl.bcs.custodia.pojo.EventoCapital;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ProcesarEventoCapitalCanje implements JavaCall {

	private final Log log = new Log("CUS.ProcesarEventoCapitalCanje");
	private final String SQL_UPDATE_EVENTO_CAPITAL						= loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapitalCanje_UpdateEventoCapital.sql");
	private final String SQL_SELECT_SIMULTANEAS_PRESTAMOS_ORDENES		= loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapitalCanje_SelectSimultaneasPrestamosOrdenes.sql");
	private final String SQL_INSERT_EVENTO_CAPITAL_CLIENTE				= loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapitalCanje_InsertEventoCapitalCliente.sql");	
	private final String SQL_INSERT_EVENTO_CAPITAL_CLIENTE_NEGATIVO		= loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapitalCanje_InsertEventoCapitalClienteNegativo.sql");	
	
	public static class actualizaEventoCapital{
		public Long		IdEventoCapital;
		public String	IdEstado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}		
	};

	public static class InsertEventoCapital{
		public Long		IdEventoCapital;
		public Long		IdUsuario;
		public Long		IdEmpresa;
		public Long		IdInstrumento;
		public Date		FechaLimite;
		
		public String toString() {
			return Utils.printJson(this, false);
		}		
	};
	
	//TODO: Clase de entrada
	public static class ProcesarEventoCapitalCanjeEntrada {
		public Long							p_IdEmpresa; 
		public Long							p_IdUsuario;
		public String						p_IdModulo;
		public List<EventoCapital>			p_EventoCapital;
		
		public ProcesarEventoCapitalCanjeEntrada (){
			p_EventoCapital 			= new ArrayList<EventoCapital>();
		}
			
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class ProcesarEventoCapitalCanjeSalida {
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		//public List<Long>			P_OUT_SECUENCIA;
		
		public ProcesarEventoCapitalCanjeSalida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
			//this.P_OUT_SECUENCIA = new ArrayList<Long>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DatosEjecutivo {
		public Long		IdPortafolio;
		public Long		IdEmpleadoEjecutivoPortafolio;
		public String	CodigoEmpleado;
		public String	NombreCompletoEjecutivo;
		public String	CodigoSucursal;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DatosCuentaCustodio{
		public Long		IdPortafolio;
		public String	Codigo;
		public String	IdTipoCuentaCustodia;
		public String	LiquidaDividendos;
		public String	IdBoveda;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DatosSocioDeNegocio {
		public Long		IdSocioDeNegocio;
		public Long		IdPersona;
		public Long		IdEmpresa;
		public Long		IdCliente;
		public Long		IdEmpleado;
		public Long		IdOperador;
		public Long		IdProveedor;
		public Long		IdContraparte;
		public String	Borrado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DatosOperaciones{
		public Long		CantidadOrdenes;
		public Long		CantidadSimultaneas;
		public Long		CantidadPrestamos;
		public Long		CantidadGarantias;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	//TODO: Logica
	public ProcesarEventoCapitalCanjeSalida logica(Connection con, ProcesarEventoCapitalCanjeEntrada in) throws Exception {
		ProcesarEventoCapitalCanjeSalida out = new ProcesarEventoCapitalCanjeSalida();
		SQL sql=new SQL();
//		Long SecPrecioTasas = Utils.getSecuencia(con, "BSCL.Plataforma.BD.Secuencias::TAS.IdServicioEntregarPreciosTasas", 1L).get(0);
//		CalcularCoberturaWrapper calculoCobertura = new CalcularCoberturaWrapper();
		
		//try {			
		//******************************************************************************************************************
		//												VALIDACIONES INICIALES
		//******************************************************************************************************************			
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		ProcesarEventoCapital ev = new ProcesarEventoCapital();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
		out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
	
		if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0){
			return out;
		}
		
		
		//******************************************************************************************************************
		//													LOGICA
		//******************************************************************************************************************			
		
		// obtener valor del parámetro
//			String BolsaSistema			= ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.BolsaDefectoSistema");
//			String localizacionSistema	= ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.LocalizacionSistema");
		
		// Consulta empresa
		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresaService.logica(con, in.p_IdEmpresa);
		List<ObjEventoCapital_Entrada> ListaIds = new ArrayList<ObjEventoCapital_Entrada>();
		List<ObjEventoCapitalCliente_Entrada> ListaCliIds = new ArrayList<ObjEventoCapitalCliente_Entrada>();
		
		for (EventoCapital recorreEventoCapital: in.p_EventoCapital){
			
			//Validaciones de negocio: En caso que existan Simultáneas, Prestamos, órdenes vigentes o Garantías, se debe abortar el proceso. 
			HashMap<String,Object> paramDatosOperaciones = new HashMap<String,Object>();
			
			paramDatosOperaciones.put("IdInstrumento", recorreEventoCapital.IdInstrumento);
			paramDatosOperaciones.put("FechaLimite", recorreEventoCapital.FechaLimite);
			
			List<DatosOperaciones> datosOperaciones = sql.query(con, SQL_SELECT_SIMULTANEAS_PRESTAMOS_ORDENES, paramDatosOperaciones, DatosOperaciones.class);
			Long vContadorOperaciones = 0L;
			for(DatosOperaciones recorredatosOperaciones : datosOperaciones){
				if (recorredatosOperaciones.CantidadOrdenes>0){
					vContadorOperaciones++;
					HashMap<String,String> params = new HashMap<String,String>();
					params.put("Cantidad", recorredatosOperaciones.CantidadOrdenes.toString());
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_CANJE_ORDENES_VIGENTES",params,con,in.p_IdEmpresa));
				}
				if (recorredatosOperaciones.CantidadSimultaneas>0){
					vContadorOperaciones++;
					HashMap<String,String> params = new HashMap<String,String>();
					params.put("Cantidad", recorredatosOperaciones.CantidadSimultaneas.toString());
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_CANJE_SIMULTANEAS_VIGENTES",params,con,in.p_IdEmpresa));
				}
				if (recorredatosOperaciones.CantidadPrestamos>0){
					vContadorOperaciones++;
					HashMap<String,String> params = new HashMap<String,String>();
					params.put("Cantidad", recorredatosOperaciones.CantidadPrestamos.toString());
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_CANJE_PRESTAMOS_VIGENTES",params,con,in.p_IdEmpresa));
				}
				if (recorredatosOperaciones.CantidadGarantias>0){
					vContadorOperaciones++;
					HashMap<String,String> params = new HashMap<String,String>();
					params.put("Cantidad", recorredatosOperaciones.CantidadGarantias.toString());
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_CANJE_GARANTIAS_VIGENTES",params,con,in.p_IdEmpresa));
				}
			};
			
			if (vContadorOperaciones.equals(0L)){ // NO hay operaciones, podemos continuar con el proceso
				List<InsertEventoCapital> insertEventoClienteFull = new ArrayList<InsertEventoCapital>();
				
				InsertEventoCapital vInsert = new InsertEventoCapital();
				vInsert.IdEventoCapital = recorreEventoCapital.IdEventoCapital;
				vInsert.IdUsuario		= in.p_IdUsuario;
				vInsert.IdEmpresa		= in.p_IdEmpresa;
				vInsert.IdInstrumento	= recorreEventoCapital.IdInstrumento;
				vInsert.FechaLimite		= recorreEventoCapital.FechaLimite;
				insertEventoClienteFull.add(vInsert);
				try {
					sql.batch(con, SQL_INSERT_EVENTO_CAPITAL_CLIENTE, insertEventoClienteFull , InsertEventoCapital.class);
					sql.batch(con, SQL_INSERT_EVENTO_CAPITAL_CLIENTE_NEGATIVO, insertEventoClienteFull , InsertEventoCapital.class);
					ProcesarEventoCapital.agregarLogProcesarEventoCapital(con, recorreEventoCapital.IdEventoCapital, in.p_IdModulo, in.p_IdUsuario, in.p_IdEmpresa,
																   "PRO", recorreEventoCapital.IdTipoEventoCapital, insertEventoClienteFull.size(), recorreEventoCapital.Nemo);
				}catch(Exception e) {
					out.P_OUT_ERRORESCONTADOR++;
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_INSERT_CLIENTE_EVENTO_CAPITAL_CANJE",null,con,in.p_IdEmpresa));
					return out;
				}
				log.write("ProcesarEventoCapitalCanje - Fin Insert Eventos Capital Cliente");
				
				List<actualizaEventoCapital> idEventoCapital = new ArrayList<actualizaEventoCapital>();
				actualizaEventoCapital idEven = new actualizaEventoCapital();
				idEven.IdEventoCapital = recorreEventoCapital.IdEventoCapital;
				idEven.IdEstado = "PRO";
				idEventoCapital.add(idEven);
				
				sql.batch(con, SQL_UPDATE_EVENTO_CAPITAL, idEventoCapital, actualizaEventoCapital.class);
				log.write("ProcesarEventoCapitalCanje - Fin Update Eventos Capital");
				
				//Envío de Notificación
				ObjEventoCapital_Entrada NotifIN = new ObjEventoCapital_Entrada();
				NotifIN.p_IdEventoCapital = recorreEventoCapital.IdEventoCapital;
				ListaIds.add(NotifIN);
				
				NotificarEventoCapital_Entrada paramsNotif = new NotificarEventoCapital_Entrada();
				paramsNotif.p_IdEmpresa = in.p_IdEmpresa;
				paramsNotif.p_IdModulo = in.p_IdModulo;
				paramsNotif.p_IdUsuario = in.p_IdUsuario;
				paramsNotif.p_IdsEventoCapital = ListaIds;
				paramsNotif.p_IdsEventoCapitalCliente = ListaCliIds; 
				
				NotificarEventoCapital CreaNotificacion = new NotificarEventoCapital();
				NotificarEventoCapital_Salida resulNotif  = CreaNotificacion.logica(con, paramsNotif);
				out.P_OUT_MENSAJES.addAll(resulNotif.P_OUT_MENSAJES);
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"UI_CANJE_PROCESADO_OK",null,con,in.p_IdEmpresa));
			}
		}
		return out;
	};
    
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		ProcesarEventoCapitalCanjeEntrada jin=gson.fromJson(in.toString() ,ProcesarEventoCapitalCanjeEntrada.class);
		ProcesarEventoCapitalCanjeSalida out=logica(conn,jin);
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	// MAIN
	public static void main(String[] args) throws Exception {		
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
				String json = "{\"p_IdEmpresa\": 1,\"p_IdUsuario\": 9,\"p_IdModulo\": \"CUS\",\"p_EventoCapital\": [{\"IdEventoCapital\": 364858,\"IdEmpresa\": 1,\"IdTipoEventoCapital\": \"DA\",\"Secuencia\": 1,\"Descripcion\": \"PRUEBA JPZ INGEVEC\",\"EsInscrito\": \"S\",\"IdClasificacionTipoInstrumento\": \"RV\",\"IdTipoInstrumento\": \"ACN\",\"IdInstrumento\": 1203,\"Nemo\": \"INGEVEC\",\"Factor\": 0.0846546,\"Valor\": 0.0,\"IdMoneda\": \"\",\"IdInstrumentoNuevo\": 0,\"NemoNuevo\": \"0\",\"FactorNuevo\": 1.0,\"FechaActualizacion\": \"2018-07-31 00:00:00.000\",\"FechaLimite\": \"2018-07-31 00:00:00.000\",\"FechaPago\": \"2018-08-07 00:00:00.000\",\"NumeroDividendo\": 5520101,\"IdTipoDividendo\": \"P\",\"PrecioAnterior\": 0.0,\"FechaHoraIngresoRegistro\": \"2018-07-31 00:00:00.000\",\"FechaHoraUltimaActualizacion\": \"2018-08-07 00:00:00.000\",\"TipoIngreso\": \"C\",\"IdUsuario\": 16,\"IdEstado\": \"VIG\",\"CuentaDepositanteSorteoLetras\": 0,\"MonedaSorteoLetras\": 0,\"GlosaMonedaSorteoLetras\": \"\",\"EmisorPagadorSorteoLetras\": 0,\"EmisorSorteoLetras\": 0,\"InstrumentoSorteoLetras\": 0,\"GlosaInstrumentoSorteoLetras\": \"\",\"BloqueoSorteoLetras\": 0,\"SorteadoSorteoLetras\": 0.0,\"APagarSorteoLetras\": 0.0,\"APagarPesosSorteoLetras\": 0,\"Origen\": \"MAN\",\"IdEmisor\": \"INGEVEC\",\"IdMonedaLiquidacion\": \"\",\"Paridad\": 1.0,\"IdEmisorNuevo\": \"\",\"EstadoNotificacion\": \"N\",\"IdInstrumentoAnterior\": 0,\"Indice\": 0,\"Impuesto\": 0.0,\"CantidadCuotas\": null,\"Monto\": null}]}";
				ProcesarEventoCapitalCanjeEntrada Objin = gson.fromJson(json, ProcesarEventoCapitalCanjeEntrada.class);
								
				ProcesarEventoCapitalCanje  kk = new ProcesarEventoCapitalCanje();
				ProcesarEventoCapitalCanjeSalida Objout = kk.logica(con, Objin);
				con.commit();
				
		    	}catch(Exception er) {
					er.printStackTrace();	
					con.rollback();
				}
		} catch (Exception e) {
			e.printStackTrace();			
		}
	};
	
};