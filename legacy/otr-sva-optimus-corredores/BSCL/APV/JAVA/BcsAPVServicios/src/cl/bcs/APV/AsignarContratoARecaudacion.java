package cl.bcs.APV;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class AsignarContratoARecaudacion implements JavaCall   {

	static final Logger logger = LogManager.getLogger(AsignarContratoARecaudacion.class);
	/*
	 * Queries que van a ser utilizadas por el servicio. 
	 */	
	public String SQL_SELECT_AsignacionContratoRecaudacion = loadResourceToString("cl/bcs/APV/sql/AsignacionContratoRecaudacion_Select.sql");
	public String SQL_INSERT_AsignacionContratoRecaudacion = loadResourceToString("cl/bcs/APV/sql/AsignacionContratoRecaudacion_Insert.sql");
	public String SQL_UPDATE_AsignacionContratoRecaudacion = loadResourceToString("cl/bcs/APV/sql/AsignacionContratoRecaudacion_Update.sql");
	
	public String SQL_SELECT_SelectContratosAPV	= loadResourceToString("cl/bcs/APV/sql/AsignacionContratoRecaudacion_SelectContratosAPVPorGrupoDeContratos.sql");
	public String SQL_UPDATE_RecaudacionCliente = loadResourceToString("cl/bcs/APV/sql/AsignacionContratoRecaudacion_UpdateRecaudacionCliente.sql");
	public String SQL_SELECT_DetalleTrapaso		= loadResourceToString("cl/bcs/APV/sql/AsignacionContratoRecaudacion_SelectDetalleTraspaso.sql");
	
	
	/********************
	 * Clases IN/OUT	*
	 ********************/
	
	public static class ParametrosAsignacionContratoRecaudacion{

		public Long IdRecaudacionCliente;
		public Long IdContratoSAAPV;
		public Long IdRecaudacion;
		public ParametrosAsignacionContratoRecaudacion() {
			this.IdContratoSAAPV = 0L;
			this.IdRecaudacionCliente = 0L;
			this.IdRecaudacion = 0L;
		}
	}

	public static class AsignacionContratoRecaudacion{

		public Long IdRecaudacionCliente;
		public Long IdContratoSAAPV;
		public Timestamp FechaCreacion;
		public Timestamp FechaModificacion;
				
	}	
	public static class ParamsAsignacion_Select{

		public Long IdRecaudacionCliente;
				
	}
	
	public static class ParamsAsignacion_Insert{

		public Long IdRecaudacionCliente;
		public Long IdContratoSAAPV;
		public Timestamp FechaCreacion;
		public Timestamp FechaModificacion;
				
	}
	public static class ParamsAsignacion_Update{

		public Long IdRecaudacionCliente;
		public Long IdContratoSAAPV;
		public Timestamp FechaModificacion;
				
	}
	
	public static class ContratoSAAPV{
		
		public Long IdCliente;
		public Long IdContratoSAAPV;
		public String TipoContrato;
		public String Regimen;
		public Long IdRecaudacionCliente;
		public Long IdRecaudacion;
		
		public ContratoSAAPV() {
			
			this.IdCliente = 0L;
			this.IdContratoSAAPV = 0L;
			this.TipoContrato = "";
			this.Regimen = "";
			this.IdRecaudacionCliente = 0L;
			this.IdRecaudacion = 0L;
		}
		
		public Long getIdContratoSAAPV() {
		
			return this.IdContratoSAAPV;
		
		}
		
		public String toString() {
			
			return Utils.printJson(this, false);
		}
	}
	
	//  SERVICIO ENTRADA
	public static class ParametrosEntradaServicio {
		
		public 	Long												p_IdEmpresa;
		public	Long												p_IdUsuario;
		public 	String												p_IdModulo;
		public	String												p_Origen;
		public 	String												p_Simulacion;
		public  List<ParametrosAsignacionContratoRecaudacion>		p_Parametros;
		public ParametrosEntradaServicio() {
			this.p_Simulacion ="N";
			this.p_Origen = "";
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	}			

	// SERVICIO SALIDA
	public static class ParametrosSalidaServicio {		
		public List<MensajeSalida>									P_OUT_MENSAJES;
		public Long													P_OUT_ERRORESCONTADOR;

		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public ParametrosSalidaServicio(){
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}		
	};

	public static class ContratoConsultado {
		public  Long		IdContratoSAAPV;
	    public 	String		IdEstado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DetalleTrapaso extends BcsPojo{
		public Long		IdContratoSAAPV;
		public Long		IdTraspaso;
		public String	TieneDetalle;
	}
	
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		ParametrosEntradaServicio jin = gson.fromJson(in.toString() ,ParametrosEntradaServicio.class);
		ParametrosSalidaServicio out = logica(conn,jin);		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	/************************
	 * Metodos Basicos	*	
	 ************************/
	
	public ParametrosSalidaServicio logica(Connection con,ParametrosEntradaServicio in)throws Exception {
		logger.info("INICIO::AsignarContratoARecaudacion");
		long inicioServicio = System.currentTimeMillis();
		ParametrosSalidaServicio out = new ParametrosSalidaServicio();

		SQL sql = new SQL();
		Long time = System.currentTimeMillis();
		java.sql.Timestamp FechaHora = new java.sql.Timestamp(time);
		LocalDate FechaLocal = FechaHora.toLocalDateTime().toLocalDate();
		int mesSystema = FechaLocal.getMonthValue();
		int anoSystema = FechaLocal.getYear();
		String tipoMensajesSalida = "";
		boolean tieneDetalleTraspaso = false;

		List<ParamsAsignacion_Insert> Insert_Asignacion = new ArrayList<ParamsAsignacion_Insert>();
		List<ParamsAsignacion_Update> Update_Asignacion = new ArrayList<ParamsAsignacion_Update>();
		
		List<Long> listadoIdsContratoSAAPV= new ArrayList<Long>();
		
		Long IdContratoSAAPV = 0L;
		for (ParametrosAsignacionContratoRecaudacion fila : in.p_Parametros){
			
			if(fila.IdContratoSAAPV == null) {
				fila.IdContratoSAAPV = 0L;
			}
			
			HashMap<String,Object> Select_Asignacion = new HashMap<String,Object>(1);
			Select_Asignacion.put("IdRecaudacionCliente", fila.IdRecaudacionCliente);

			List<AsignacionContratoRecaudacion> _AsignacionContratoRecaudacion = sql.query(con, SQL_SELECT_AsignacionContratoRecaudacion, Select_Asignacion, AsignacionContratoRecaudacion.class);
			if ("S".equals(in.p_Simulacion)
				&& _AsignacionContratoRecaudacion.size()>0)
			{
				for (AsignacionContratoRecaudacion actual : _AsignacionContratoRecaudacion){
			        LocalDate actualFechaModificacion = actual.FechaModificacion.toLocalDateTime().toLocalDate();
			        int MesActual = actualFechaModificacion.getMonthValue();
			        int anoActual = actualFechaModificacion.getYear();
					if( actual.IdContratoSAAPV.equals(fila.IdContratoSAAPV)
						&&	MesActual == mesSystema
						&&	anoActual == anoSystema) {
							tipoMensajesSalida = "ContratoAsignado";
						//si no hay contratos que cuplan la condicion no realizamos cambios a la logica principal
					}
				}
			}
			if(_AsignacionContratoRecaudacion.size()>0){
				ParamsAsignacion_Update Param_Asignacion = new ParamsAsignacion_Update();
				Param_Asignacion.IdRecaudacionCliente = fila.IdRecaudacionCliente;
				
				Param_Asignacion.IdContratoSAAPV = fila.IdContratoSAAPV;
				Param_Asignacion.FechaModificacion = FechaHora;
				Update_Asignacion.add(Param_Asignacion);
			}else{
				ParamsAsignacion_Insert Param_Asignacion = new ParamsAsignacion_Insert();
				Param_Asignacion.IdRecaudacionCliente = fila.IdRecaudacionCliente;
				Param_Asignacion.IdContratoSAAPV = fila.IdContratoSAAPV;
				Param_Asignacion.FechaCreacion = FechaHora; 
				Param_Asignacion.FechaModificacion = FechaHora;
				Insert_Asignacion.add(Param_Asignacion);	
			}
			
			IdContratoSAAPV = fila.IdContratoSAAPV;
			
			listadoIdsContratoSAAPV.add(IdContratoSAAPV);			
		}
		
		List<Long> listadoIdsContratoSAAPVSinRepeticiones= listadoIdsContratoSAAPV.parallelStream().distinct().map(idContratoSAAPV->idContratoSAAPV).collect(Collectors.toList());
		
		HashMap<String,Object> hashListadoIdsContratos = new HashMap<String,Object>(1);
		hashListadoIdsContratos.put("IdContratoSAAPV", listadoIdsContratoSAAPV);
		
		List <DetalleTrapaso> detalleTrapaso = new ArrayList<>();
		
		if("TRASPA".equalsIgnoreCase(in.p_Origen)){
			detalleTrapaso = obtenerDetalleTrapaso(con,sql,listadoIdsContratoSAAPVSinRepeticiones);
		}
		
		List<ContratoSAAPV> listadoContratosRatificarRegimen = sql.query(con, SQL_SELECT_SelectContratosAPV, hashListadoIdsContratos, ContratoSAAPV.class);
		
		if(!"S".equals(in.p_Simulacion) || "".equals(tipoMensajesSalida)) {
			
			if(Update_Asignacion.size()>0) {
				sql.batch(con,SQL_UPDATE_AsignacionContratoRecaudacion,Update_Asignacion,ParamsAsignacion_Update.class);
			}else if(Insert_Asignacion.size()>0) {
				sql.batch(con,SQL_INSERT_AsignacionContratoRecaudacion,Insert_Asignacion,ParamsAsignacion_Insert.class);
			}
			
			for(ContratoSAAPV contSAAPV : listadoContratosRatificarRegimen) {
				
				if(detalleTrapaso.size() > 0) {
					List<DetalleTrapaso> detalleTrapasoSAAPV = detalleTrapaso.stream().filter(contrato -> contrato.IdContratoSAAPV.compareTo(contSAAPV.IdContratoSAAPV) == 0)
							.collect(Collectors.toList());
					if(detalleTrapasoSAAPV.size()> 0) {
						tieneDetalleTraspaso = "S".equalsIgnoreCase(detalleTrapasoSAAPV.get(0).TieneDetalle) ? true:false; 
					}
				}
				
				if (!"TRASPA".equalsIgnoreCase(in.p_Origen) && !tieneDetalleTraspaso) {
					
					List<ParametrosAsignacionContratoRecaudacion> filtroContrato = in.p_Parametros.stream().filter(contrato -> contrato.IdContratoSAAPV.equals(contSAAPV.IdContratoSAAPV))
							.collect(Collectors.toList());
					contSAAPV.IdRecaudacionCliente = filtroContrato.get(0).IdRecaudacionCliente;
					contSAAPV.IdRecaudacion = filtroContrato.get(0).IdRecaudacion;
						
				}
					
			}
				
			if(listadoContratosRatificarRegimen.size() > 0) {
				
				Long datosPorActualizar = listadoContratosRatificarRegimen.stream().filter(contrato ->contrato.IdRecaudacionCliente.compareTo(0L) != 0).count();
				
				if(datosPorActualizar > 0) {
					
					listadoContratosRatificarRegimen = listadoContratosRatificarRegimen.stream().filter(contrato ->contrato.IdRecaudacionCliente.compareTo(0L) != 0).collect(Collectors.toList());
					
					sql.batch(con,SQL_UPDATE_RecaudacionCliente,listadoContratosRatificarRegimen,ContratoSAAPV.class);
						
				}
								
			}
			
		}
		
		List<MensajeSalida> mensajes = new ArrayList<MensajeSalida>();		
		for(ContratoSAAPV contSAAPV : listadoContratosRatificarRegimen) {
			if("ContratoAsignado".equals(tipoMensajesSalida)){
			mensajes.add(new MensajeSalida(contSAAPV.IdRecaudacionCliente, MensajeSalida.MENSAJE_INFORMACION, "UI_CONTRATO_YA_ASIGNADO",new HashMap<String,String>(0),con,in.p_IdEmpresa));
			}else {
				mensajes.add(new MensajeSalida(contSAAPV.IdRecaudacionCliente, MensajeSalida.MENSAJE_INFORMACION, "UI_ASIGNACION_EXITOSA",new HashMap<String,String>(0),con,in.p_IdEmpresa));
			}
		}
		out.P_OUT_MENSAJES.addAll(mensajes);
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::AsignarContratoARecaudacion", finServicio - inicioServicio);
		return out;
	}
	
	private List <DetalleTrapaso> obtenerDetalleTrapaso (Connection con, SQL sql, List<Long> listadoContratos) throws Exception{
		logger.debug("INICIO::AsignarContratoARecaudacion.obtenerDetalleTrapaso");
		long inicio = System.currentTimeMillis();
		List <DetalleTrapaso> detalleTraspasos = new ArrayList<>();
		HashMap<String,Object> listadoContratosSAAPV = new HashMap<>(1);
		
		listadoContratosSAAPV.put("IdContratoSAAPV",listadoContratos);
		detalleTraspasos = sql.query(con,SQL_SELECT_DetalleTrapaso, listadoContratosSAAPV,DetalleTrapaso.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AsignarContratoARecaudacion.obtenerDetalleTrapaso tiempo: " + (fin - inicio));
		return detalleTraspasos;
	}
	/********************
	 * Metodo Principal *
	 ********************/

	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {
				ParametrosEntradaServicio Objin = new ParametrosEntradaServicio();
				AsignarContratoARecaudacion demo = new AsignarContratoARecaudacion();	

				Objin.p_IdEmpresa = 1L;
				Objin.p_IdUsuario = 17L;
				Objin.p_IdModulo = "APV";
				Objin.p_Simulacion = "S";

				ParametrosAsignacionContratoRecaudacion Param = new ParametrosAsignacionContratoRecaudacion();
				List<ParametrosAsignacionContratoRecaudacion> ListParam = new ArrayList<ParametrosAsignacionContratoRecaudacion>();
				
				Param.IdRecaudacionCliente = 13L;
				Param.IdContratoSAAPV = null;
				
				ListParam.add(Param);				
				
				Objin.p_Parametros = ListParam;
				

				ParametrosEntradaServicio obj = new ParametrosEntradaServicio();
				obj.p_IdEmpresa = 1L;
				obj.p_IdUsuario = 9L;
				obj.p_IdModulo = "APV";
				obj.p_Simulacion = "S";
				ParametrosAsignacionContratoRecaudacion Par = new ParametrosAsignacionContratoRecaudacion();
				List<ParametrosAsignacionContratoRecaudacion> ListPar = new ArrayList<ParametrosAsignacionContratoRecaudacion>();
				
				Par.IdRecaudacionCliente = 141342L;
				Par.IdContratoSAAPV = 451789L;
				
				ListPar.add(Par);				
				
				obj.p_Parametros = ListPar;
								
	 			try { 
	 				ParametrosSalidaServicio ObjOut = demo.logica(con, obj);
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
