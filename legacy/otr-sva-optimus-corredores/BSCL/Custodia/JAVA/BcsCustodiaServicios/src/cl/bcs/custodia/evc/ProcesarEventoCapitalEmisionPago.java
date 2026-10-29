package cl.bcs.custodia.evc;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.evc.ProcesarEventoCapital.SalidaClientesConDerecho;
import cl.bcs.custodia.evc.ProcesarEventoCapitalDividendoEnAcciones.actualizaEventoCapital;
import cl.bcs.custodia.pojo.EventoCapital;
import cl.bcs.custodia.pojo.EventoCapitalCliente;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ProcesarEventoCapitalEmisionPago implements JavaCall {

	private final Log log = new Log("CUS.ProcesarEventoCapitalEmisionPago");
	private static final Logger logger = LogManager.getLogger(ProcesarEventoCapitalEmisionPago.class);
	private final String SQL_INSERT_EVENTO_CAPITAL_CLIENTE			= loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapitalEmisionPago_InsertEventoCapitalCliente.sql");
	private final String SQL_UPDATE_EVENTO_CAPITAL					= loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapitalEmisionPago_ActualizaEventoCapital.sql");
	private final String SQL_SELECT_CONSULTAR_INSTRUMENTO_RELACIONADO = loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapitalEmisionPago_SelectConsultarInstrumentoRelacionado.sql");
	
	//TODO : Parámetros de entrada.
	public static class ProcesarEventoCapitalEmisionPagoEntrada {
		public Long							p_IdEmpresa; 
		public Long							p_IdUsuario;
		public String						p_IdModulo;
		public List<EventoCapital>			p_EventoCapital;
		
		public ProcesarEventoCapitalEmisionPagoEntrada (){
			p_EventoCapital 			= new ArrayList<EventoCapital>();
		}
			
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class ProcesarEventoCapitalEmisionPagoSalida {
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		
		public ProcesarEventoCapitalEmisionPagoSalida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class InstrumentoRelacionado{
		public Long IdInstrumento;
		public String Nemotecnico;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	//TODO: Logica
	public ProcesarEventoCapitalEmisionPagoSalida logica(Connection con, ProcesarEventoCapitalEmisionPagoEntrada in) throws Exception {		
		ProcesarEventoCapitalEmisionPagoSalida out = new ProcesarEventoCapitalEmisionPagoSalida();
		SQL sql=new SQL();
		{			
			//******************************************************************************************************************
			//												VALIDACIONES INICIALES
			//******************************************************************************************************************			
			RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
			RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
			out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
		
			if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0){
				return out;
			}
			
			//******************************************************************************************************************
			//													LOGICA
			//******************************************************************************************************************			
			// obtener fecha del sistema
			String tFechaHoraSistema   = FechaDB.ObtenerFechaHoraSistemaUTC(con, in.p_IdEmpresa);
			// obtener valor del parámetro
			String BolsaSistema			= ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.BolsaDefectoSistema");
			String localizacionSistema	= ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.LocalizacionSistema");
			
			ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
			InformacionEmpresa empresa = consultarEmpresaService.logica(con, in.p_IdEmpresa);
			
			ProcesarEventoCapital ev = new ProcesarEventoCapital();
			
			for (EventoCapital recorreEventoCapital : in.p_EventoCapital){
				String vIdEstado = new String();
				vIdEstado = "PRO";
				List<actualizaEventoCapital> idEventoCapital = new ArrayList<actualizaEventoCapital>();
				HashMap<String,Object> params = new HashMap<String,Object>();
				params.put("IdInstrumento", recorreEventoCapital.IdInstrumento);
				List<InstrumentoRelacionado> instrumentoRelacionado = sql.query(con, SQL_SELECT_CONSULTAR_INSTRUMENTO_RELACIONADO, params, InstrumentoRelacionado.class);
				
				if(instrumentoRelacionado.size() == 0){
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_INSTRUMENTO_RELACIONADO_NO_EXISTE",new HashMap<String,String>(),con,in.p_IdEmpresa));
					return out;
				}
				
				//Validación de Valor y FactorNuevo (Valor Nuevo)
				boolean esValidoValor = validarValorEventoCapital(recorreEventoCapital);
				if (!esValidoValor) { 
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_EVENTO_CAPITAL_VALOR_FACTOR_INVALIDO", new HashMap<String,String>(), con, in.p_IdEmpresa));
					out.P_OUT_ERRORESCONTADOR ++;
					continue;
				}
				
				// Consultamos clientes con derecho.
				SalidaClientesConDerecho CCD = ev.getClientesConDerecho(con, recorreEventoCapital, in.p_IdModulo, empresa);
				out.P_OUT_MENSAJES.addAll(CCD.P_OUT_MENSAJES);
				
				if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0){
					return out;
				}
				
				// GCBSXNEC-1 jmedina_ext
				if(!recorreEventoCapital.SuscripcionComplementaria.equalsIgnoreCase("S") && recorreEventoCapital.SecuenciaRelacionada.equalsIgnoreCase("")){
					for(EventoCapitalCliente _datosEvCapCli : CCD.P_OUT_CLIENTES){					
						BigDecimal CantidadNuevaNew			= new BigDecimal (_datosEvCapCli.CantidadDerecho.multiply(recorreEventoCapital.Factor).doubleValue());
						if (CantidadNuevaNew.compareTo(_datosEvCapCli.CantidadNueva) != 0) {
						_datosEvCapCli.CantidadNueva = CantidadNuevaNew.setScale(0, RoundingMode.DOWN);
						}					
					}
				
					for(EventoCapitalCliente _datosEvCapCon : CCD.P_OUT_CONTRAPARTES){					
						BigDecimal CantidadNuevaNew			= new BigDecimal (_datosEvCapCon.CantidadDerecho.multiply(recorreEventoCapital.Factor).doubleValue());
						if (CantidadNuevaNew.compareTo(_datosEvCapCon.CantidadNueva) != 0) {
							_datosEvCapCon.CantidadNueva = CantidadNuevaNew.setScale(0, RoundingMode.DOWN);
						}
					}
				}
								
				//TODO: INSERT EVENTO CAPITAL CLIENTE.
				List<EventoCapitalCliente> insertEventoClienteFull = unionLists(CCD.P_OUT_CLIENTES, CCD.P_OUT_CONTRAPARTES);
				//insertEventoClienteFull.size();
				List<Long> secuenciaEventoCapitalCliente= Utils.getSecuencia(con, "BSCL.Custodia.BD.Secuencias::EventoCapitalCliente", new Long(insertEventoClienteFull.size()));
				if (insertEventoClienteFull.size()>0 ){
					
					for (EventoCapitalCliente recorreInsert: insertEventoClienteFull){
						recorreInsert.IdEventoCapitalCliente = secuenciaEventoCapitalCliente.get(insertEventoClienteFull.indexOf(recorreInsert));
						if (recorreInsert.IdEstado.equals("PEN")){
							vIdEstado = "PEN";
						}
					}
					
					sql.batch(con, SQL_INSERT_EVENTO_CAPITAL_CLIENTE, insertEventoClienteFull , EventoCapitalCliente.class);
					log.write("ProcesarEventoCapitalDividendoEnAcciones - Fin Insert Eventos Capital Cliente");
					ProcesarEventoCapital.agregarLogProcesarEventoCapital(con, recorreEventoCapital.IdEventoCapital, in.p_IdModulo, in.p_IdUsuario, in.p_IdEmpresa,
																	vIdEstado, recorreEventoCapital.IdTipoEventoCapital, insertEventoClienteFull.size(), recorreEventoCapital.Nemo);
							
						
				}
				actualizaEventoCapital idEven = new actualizaEventoCapital();
				idEven.IdEventoCapital = recorreEventoCapital.IdEventoCapital;
				idEven.IdEstado = vIdEstado;
				idEventoCapital.add(idEven);
				
				sql.batch(con, SQL_UPDATE_EVENTO_CAPITAL, idEventoCapital, actualizaEventoCapital.class);
				log.write("ProcesarEventoCapitalDividendoEnAcciones - Fin Update Eventos Capital");	
				
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"UI_OPERACION_PROCESAR_EXITO",new HashMap<String,String>(),con,in.p_IdEmpresa));
				
			};
			
		}
		return out;
	};


	public <EventoCapitalCliente> List<EventoCapitalCliente> unionLists (	List<EventoCapitalCliente> list1, 
			List<EventoCapitalCliente> list2) {
		
		Set<EventoCapitalCliente> setNuevaLista = new HashSet<EventoCapitalCliente>();
		
		setNuevaLista.addAll(list1);
		setNuevaLista.addAll(list2);

		return new ArrayList<EventoCapitalCliente>(setNuevaLista);
	};
	
	//Validación de Valor y FactorNuevo (Valor Nuevo)
	private boolean validarValorEventoCapital(EventoCapital eventoCapital ){
		logger.trace("INICIO::ProcesarEventoCapitalEmisionPago.validarValorEventoCapital");
		long inicio = System.currentTimeMillis();
		
		boolean valorValido = true;
		if (eventoCapital.Valor != null && eventoCapital.Valor.compareTo(BigDecimal.ZERO) == 0 
			&& eventoCapital.FactorNuevo != null && eventoCapital.FactorNuevo.compareTo(BigDecimal.ZERO) == 0){
			valorValido = false;
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ProcesarEventoCapitalEmisionPago.validarValorEventoCapital" + " - Tiempo: " + (fin - inicio) + " ms");
		return valorValido;
	}

    
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		ProcesarEventoCapitalEmisionPagoEntrada jin=gson.fromJson(in.toString() ,ProcesarEventoCapitalEmisionPagoEntrada.class);
		ProcesarEventoCapitalEmisionPagoSalida out=logica(conn,jin);
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	// MAIN
	public static void main(String[] args) throws Exception {		
		//try (Connection con = PlataformaUtils.getQAPEConnection()) {
		//try (Connection con = PlataformaUtils.getDESAPEConnection()) {
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":16,\"p_IdModulo\":\"CUS\",\"p_IdEventoCapital\":364779,\"p_IdTipoEventoCapital\":\"EP\",\"p_Secuencia\":1,\"p_NumeroDividendo\":120120,\"p_IdClasificacionTipoInstrumento\":\"RV\",\"p_IdTipoInstrumento\":\"ACN\",\"p_Nemo\":\"RIPLEY\",\"p_Factor\":0.0564654,\"p_Paridad\":0,\"p_FechaLimite\":\"2018-06-12\",\"p_FechaPago\":\"2018-06-19\",\"p_IdMoneda\":\"CLP\",\"p_IdInstrumento\":1845,\"p_IdInstrumentoNuevo\":0,\"p_NemoNuevo\":\"0\",\"p_FactorNuevo\":1,\"p_IdEstado\":\"VIG\",\"p_Indice\":0}";
				ProcesarEventoCapitalEmisionPagoEntrada Objin = gson.fromJson(json, ProcesarEventoCapitalEmisionPagoEntrada.class);
								
				ProcesarEventoCapitalEmisionPago  kk = new ProcesarEventoCapitalEmisionPago();
				ProcesarEventoCapitalEmisionPagoSalida Objout = kk.logica(con, Objin);
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