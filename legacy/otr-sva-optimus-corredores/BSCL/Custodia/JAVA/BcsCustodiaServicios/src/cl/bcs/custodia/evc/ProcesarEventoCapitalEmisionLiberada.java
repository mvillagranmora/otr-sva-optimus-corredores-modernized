package cl.bcs.custodia.evc;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.evc.ProcesarEventoCapital.SalidaClientesConDerecho;
import cl.bcs.custodia.pojo.EventoCapital;
import cl.bcs.custodia.pojo.EventoCapitalCliente;
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

public class ProcesarEventoCapitalEmisionLiberada implements JavaCall {

	private final Log log = new Log("CUS.ProcesarEventoCapitalEmisionLiberada");
	                                                                                                                
	private final String SQL_INSERT_EVENTO_CAPITAL_CLIENTE				= loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapitalEmisionLiberada_InsertEventoCapitalCliente.sql");
	private final String SQL_UPDATE_EVENTO_CAPITAL						= loadResourceToString("cl/bcs/custodia/sql/ProcesarEventoCapitalEmisionLiberada_UpdateEventoCapital.sql");
	
	public static class actualizaEventoCapital{
		public Long		IdEventoCapital;
		public String	IdEstado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}		
	};
	
	//Clases de entrada
	public static class ProcesarEventoCapitalEmisionLiberadaEntrada {
		public Long							p_IdEmpresa; 
		public Long							p_IdUsuario;
		public String						p_IdModulo;
		public List<EventoCapital>			p_EventoCapital;
		
		public ProcesarEventoCapitalEmisionLiberadaEntrada (){
			p_EventoCapital 			= new ArrayList<EventoCapital>();
		}
			
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	//Clases de salida
	public static class ProcesarEventoCapitalEmisionLiberadaSalida {
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		
		public ProcesarEventoCapitalEmisionLiberadaSalida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	//Clases Complementarias
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
	
	public static class DatosCliente{
		public Long		IdCliente;
		public String	TipoDocumentoIdentidad;
		public String	DocumentoIdentidad;
		public String	NombreCompleto;
		public Long		IdPaisResidencia;
		public String	IdTipoPersona;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	//Logica
	public ProcesarEventoCapitalEmisionLiberadaSalida logica(Connection con, ProcesarEventoCapitalEmisionLiberadaEntrada in) throws Exception {
		ProcesarEventoCapitalEmisionLiberadaSalida out = new ProcesarEventoCapitalEmisionLiberadaSalida();
		SQL sql=new SQL();

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
			
			// Consulta empresa
			ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
			InformacionEmpresa empresa = consultarEmpresaService.logica(con, in.p_IdEmpresa);
			for (EventoCapital recorreEventoCapital: in.p_EventoCapital){
				String vIdEstado = new String();
				vIdEstado = "PRO";
				List<actualizaEventoCapital> idEventoCapital = new ArrayList<actualizaEventoCapital>();
				SalidaClientesConDerecho CCD = ev.getClientesConDerecho(con, recorreEventoCapital, in.p_IdModulo, empresa);
				out.P_OUT_MENSAJES.addAll(CCD.P_OUT_MENSAJES);
				
				if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0){
					return out;
				}
				
				//INSERT EVENTO CAPITAL CLIENTE
				List<EventoCapitalCliente> insertEventoClienteFull = unionLists(CCD.P_OUT_CLIENTES, CCD.P_OUT_CONTRAPARTES);
				
				if (insertEventoClienteFull.size() == 0)
				{	
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_EVENTO_CAPITAL_SIN_CLIENTES_CON_DERECHO",new HashMap<String,String>(),con,in.p_IdEmpresa)); 
					return out;
				}
				List<EventoCapitalCliente> insertEventoClienteFullFinales = new ArrayList<EventoCapitalCliente>();
				
				List<Long> secuenciaEventoCapitalCliente= Utils.getSecuencia(con, "BSCL.Custodia.BD.Secuencias::EventoCapitalCliente", new Long(insertEventoClienteFull.size()));
				if (insertEventoClienteFull.size()>0 ){
					
					for (EventoCapitalCliente recorreInsert: insertEventoClienteFull){
						recorreInsert.IdEventoCapitalCliente = secuenciaEventoCapitalCliente.get(insertEventoClienteFull.indexOf(recorreInsert));
												
						if (recorreInsert.IdSocioNegocio == 0 || recorreInsert.TipoSocioNegocio  == null)
						{	//Descarta los registros invalidos 
							continue;
						}
						
						if (recorreInsert.IdEstado.equals("PEN")){
							vIdEstado = "PEN";
						}
						
						insertEventoClienteFullFinales.add(recorreInsert);
						
					}
					
					sql.batch(con, SQL_INSERT_EVENTO_CAPITAL_CLIENTE, insertEventoClienteFullFinales , EventoCapitalCliente.class);
					log.write("Fin Insert Eventos Capital Cliente");
					ProcesarEventoCapital.agregarLogProcesarEventoCapital(con, recorreEventoCapital.IdEventoCapital, in.p_IdModulo, in.p_IdUsuario, in.p_IdEmpresa,
																	vIdEstado, recorreEventoCapital.IdTipoEventoCapital, insertEventoClienteFullFinales.size(), recorreEventoCapital.Nemo);
					
				}
				actualizaEventoCapital idEven = new actualizaEventoCapital();
				idEven.IdEventoCapital = recorreEventoCapital.IdEventoCapital;
				idEven.IdEstado = vIdEstado;
				idEventoCapital.add(idEven);
				
				sql.batch(con, SQL_UPDATE_EVENTO_CAPITAL, idEventoCapital, actualizaEventoCapital.class);
				log.write("Fin Update Eventos Capital");	
				
				
			}
			
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"UI_OPERACION_PROCESAR_EXITO",new HashMap<String,String>(0),con,in.p_IdEmpresa));

		return out;
	};		
			
	
	public <EventoCapitalCliente> List<EventoCapitalCliente> unionLists (	List<EventoCapitalCliente> list1, 
			List<EventoCapitalCliente> list2) {
		
		Set<EventoCapitalCliente> setNuevaLista = new HashSet<EventoCapitalCliente>();
		
		setNuevaLista.addAll(list1);
		setNuevaLista.addAll(list2);

		return new ArrayList<EventoCapitalCliente>(setNuevaLista);
	};

    
    
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		ProcesarEventoCapitalEmisionLiberadaEntrada jin=gson.fromJson(in.toString() ,ProcesarEventoCapitalEmisionLiberadaEntrada.class);
		ProcesarEventoCapitalEmisionLiberadaSalida out=logica(conn,jin);
		
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
				ProcesarEventoCapitalEmisionLiberadaEntrada Objin = gson.fromJson(json, ProcesarEventoCapitalEmisionLiberadaEntrada.class);
								
				ProcesarEventoCapitalEmisionLiberada  kk = new ProcesarEventoCapitalEmisionLiberada();
				ProcesarEventoCapitalEmisionLiberadaSalida Objout = kk.logica(con, Objin);
				//con.commit();
				
		    	}catch(Exception er) {
					er.printStackTrace();	
					con.rollback();
				}
		} catch (Exception e) {
			e.printStackTrace();			
		}
	};
	
};