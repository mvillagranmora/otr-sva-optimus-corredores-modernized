package cl.bcs.ordenes;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.evt.AgregarEvento;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class NotificarCambioTransaccion implements JavaCall {

	static final Logger logger = LogManager.getLogger(ModificarTransaccion.class);
	
	private final String SQL_SELECT_TRANSACCION = loadResourceToString("cl/bcs/ordenes/sql/NotificarCambioTransaccion_SelectTransacciones.sql");	

	public static class EntradaModificarTransaccion{
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;
		public String 	p_Accion;
		public List<InTransaccion> p_InTransacciones;
		
		public EntradaModificarTransaccion() {
			this.p_InTransacciones = new ArrayList<InTransaccion>();
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InTransaccion{
		public Long IdTransaccion;
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ObjInfo {
		//basicos
		public Long 	  IdTransaccion;
		public Long 	  IdCliente;
		public Long 	  IDCLIENTEPERSONA;
		public Long 	  IdPortafolioCliente;
		public Long 	  IdEmpleadoDigitador;
		public Long 	  IDEMPLEADOPERSONA;
		public Long		  IDCORREOEMPLEADO;
		//tags notificaciones
		public String 	  Bolsa;
		public String 	  Num_Transaccion;
		public String 	  Producto;
		public String	  Tipo_Operacion;
		public String 	  Operacion;
		public String 	  Codigo_Operacion;
		public String 	  Instrumento;	
		public String 	  Instrumento_Simultanea;
		public String 	  Liquidacion;
		public String 	  Tipo_Transaccion;		
		public String 	  Cantidad;
		public String 	  Precio;
		public BigDecimal Tasa;
		public String	  Monto_Contado;
		public String	  Monto_Compromiso;
		public Date 	  Fecha_Vencimiento;
		public String 	  Operador_Externo;
		public String 	  TIPO_OPERACION;	
		public String 	  ATENCIONCLIENTE;
		public String 	  FECHA;
		public String 	  HORA;
		public String 	  Corredora;
		public String 	  Atencion_Cliente;
	}	
	
	public static class ObjDirs {
		public Long 	IdPortafolio;
		public Integer 	IdDireccion;
		public String 	TipoDestinatario;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}	
	}
	
	public static class NotificarCambioTransaccion_Salida {
		 public Long P_OUT_ERRORESCONTADOR;
	}

	public void logicaAsync(Connection con,EntradaModificarTransaccion in) throws Exception {
		Gson gson 				= getGsonBuilder();	
		JsonParser parser 		= new JsonParser();
        JsonObject dataObj		= parser.parse(gson.toJson(in)).getAsJsonObject();		
        String CallBatchURL 	= ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.CallBatchURL");
        String CallBatchToken 	= ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.CallBatchToken");
		
        Utils.CallBatchAsync(CallBatchURL, CallBatchToken, "PLUGIN_ORD.jar@cl.bcs.ordenes.NotificarCambioTransaccion", dataObj);
	}

	public NotificarCambioTransaccion_Salida logica(Connection con, EntradaModificarTransaccion entrada) throws Exception {
		logger.info("INICIO::NotificarCambioTransaccion");
		NotificarCambioTransaccion_Salida out = new NotificarCambioTransaccion_Salida();
		
		SQL sql = new SQL();
		
		int 		SegundosZonaHoraria = FechaDB.ObtenerSegundosZonaHoraria(con, entrada.p_IdEmpresa);        
		String 		tFechaHoraSistema	= FechaDB.ObtenerFechaHoraSistemaUTC(con, entrada.p_IdEmpresa);
	    Timestamp 	vFechaHoraSistema 	= Fecha.toTimeStamp(tFechaHoraSistema);
	    String 		Accion				= entrada.p_Accion;
	    
        Fecha.setLocalOffSet(SegundosZonaHoraria);
	    
		List<Long> transacciones = Utils.getColumn(entrada.p_InTransacciones, "IdTransaccion", false);
		
		if (transacciones.size()==0) {return out;}
		
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("listaTransacciones"	,transacciones);
		params.put("IdEmpresa"			,entrada.p_IdEmpresa);	
		params.put("SegundosZonaHoraria",SegundosZonaHoraria);
		params.put("SegundosZonaHoraria",SegundosZonaHoraria);
		
		List<ObjInfo> listaInfo = sql.query(con, SQL_SELECT_TRANSACCION, params, ObjInfo.class);
		
		AgregarEvento.ServiceEntrada notifIn = new AgregarEvento.ServiceEntrada();
		notifIn.P_IDEMPRESA 		= entrada.p_IdEmpresa;
        notifIn.P_IDUSUARIO			= entrada.p_IdUsuario;
        notifIn.P_IDMODULO			= entrada.p_IdModulo;
        notifIn.P_EVENTOS 			= new ArrayList<>();
        notifIn.P_VALORES 			= new ArrayList<>();
        notifIn.P_ARCHIVOSADJUNTOS	= new ArrayList<>();
        notifIn.P_IDDIRECCIONES 	= new ArrayList<>();
        
        for (ObjInfo tran : listaInfo) {
        	// Evento de notificacion
            AgregarEvento.ObjEVENTOS evt = new AgregarEvento.ObjEVENTOS();
            
            evt.CodigoTipoEvento 		= (tran.Codigo_Operacion.equals("VENSIM") || tran.Codigo_Operacion.equals("COMSIM"))?"NOTIFICACION_TRANSACCIONES_ORDENES_S":"NOTIFICACION_TRANSACCIONES_ORDENES";
            evt.IdSecuencia 			= tran.IdTransaccion;
            evt.FechaCreacionSistema 	= Fecha.toStringDate(vFechaHoraSistema);            
            evt.IdPersonaRemitente 		= tran.IDEMPLEADOPERSONA;
            evt.IdPersonaDestinatario 	= tran.IDEMPLEADOPERSONA;            
            evt.IdPortafolio 			= (long) 0;
            
            notifIn.P_EVENTOS.add(evt);
            
            AgregarEvento.ObjIDDIRECCIONES ndir = new AgregarEvento.ObjIDDIRECCIONES();
            ndir.IdSecuencia 		= tran.IdTransaccion;
            ndir.TipoDestinatario 	= "TO";
            ndir.IdDireccion 		= new Long(tran.IDCORREOEMPLEADO);
            
            notifIn.P_IDDIRECCIONES.add(ndir);
            
            String Fecha_Vencimiento = (tran.Fecha_Vencimiento.toString().equals("0001-01-01"))	?"N/A":tran.Fecha_Vencimiento.toString();
            String Tasa				 = (tran.Tasa.compareTo(BigDecimal.ZERO)==0)				?"N/A":tran.Tasa.toString() + "%";
            
        	notifIn.addValor(tran.IdTransaccion, "BOLSA"				,tran.Bolsa 			+ "")
        			.addValor(tran.IdTransaccion, "ACCION" 				,Accion 				+ "")
        			.addValor(tran.IdTransaccion, "NUM_TRANSACCION"		,tran.Num_Transaccion 	+ "")
        			.addValor(tran.IdTransaccion, "PRODUCTO"			,tran.Producto 			+ "") 
        			.addValor(tran.IdTransaccion, "OPERACION"			,tran.Operacion)  
		            .addValor(tran.IdTransaccion, "TASA"				,Tasa					+ "")
		            .addValor(tran.IdTransaccion, "CANTIDAD"			,tran.Cantidad			+ "")
		            .addValor(tran.IdTransaccion, "CORREDORA"			,tran.Corredora			+ "")
		            .addValor(tran.IdTransaccion, "FECHA"				,tran.FECHA				+ "")
		            .addValor(tran.IdTransaccion, "HORA"				,tran.HORA				+ "")
		            .addValor(tran.IdTransaccion, "FECHA_VENCIMIENTO"	,Fecha_Vencimiento 		+ "")
		            .addValor(tran.IdTransaccion, "ATENCION_CLIENTE"	,tran.Atencion_Cliente	+ "");
  
            
            if(tran.Codigo_Operacion.equals("VENSIM") || tran.Codigo_Operacion.equals("COMSIM")) {
         
            	notifIn.addValor(tran.IdTransaccion, "INSTRUMENTO"			,tran.Instrumento_Simultanea 	+ "")
            			.addValor(tran.IdTransaccion, "MONTO_CONT"			,"$ " + tran.Monto_Contado 		+ " CLP")
            			.addValor(tran.IdTransaccion, "MONTO_COMP"			,"$ " + tran.Monto_Compromiso 	+ " CLP");
            	
            }else {
            	
            	notifIn.addValor(tran.IdTransaccion, "LIQUIDACION"			,tran.Liquidacion 		+ "")
					   .addValor(tran.IdTransaccion, "INSTRUMENTO"			,tran.Instrumento 		+ "")
		               .addValor(tran.IdTransaccion, "TIPO_TRANSACCION"		,tran.Tipo_Transaccion) 
		               .addValor(tran.IdTransaccion, "PRECIO"				,"$" + tran.Precio		+ "");
            }	
        }
        
        if (notifIn.P_EVENTOS.size() > 0) {
            AgregarEvento notif = new AgregarEvento();
            AgregarEvento.ServiceSalida notifOut = notif.logica(con, notifIn);
            logger.info("ID EVENTO::",notifOut.P_OUT_IDEVENTO);
        }
        
        logger.info("FIN::NotificarCambioTransaccion");
		
		return out;
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = getGsonBuilder();	
		
		EntradaModificarTransaccion in	= gson.fromJson(jin.toString() ,EntradaModificarTransaccion.class);
		NotificarCambioTransaccion_Salida out 	= logica(con,in);		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}	

	public static void main(String[] args){
		
		System.out.println("== TEST ==");
		Long idEmpresa = 1L;
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			NotificarCambioTransaccion sp = new NotificarCambioTransaccion(); 
			EntradaModificarTransaccion in = new EntradaModificarTransaccion();
			in.p_IdEmpresa = idEmpresa;
			in.p_IdModulo = "ORD";
			in.p_IdUsuario = Long.parseLong("21");
			in.p_Accion	 = "MODIFICADA";
			in.p_InTransacciones = new ArrayList<>();
			InTransaccion trans = new InTransaccion();
			trans.IdTransaccion = Long.parseLong("70289776");
			in.p_InTransacciones.add(trans);
			sp.logica(con, in); 
			con.commit();
			//con.rollback();
		} catch (Exception e) {
			e.printStackTrace();	
		}	
		System.out.println("== END ==");
	}

}