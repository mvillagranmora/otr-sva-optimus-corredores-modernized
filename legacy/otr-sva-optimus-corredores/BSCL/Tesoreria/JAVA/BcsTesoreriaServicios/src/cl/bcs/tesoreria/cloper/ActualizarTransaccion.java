package cl.bcs.tesoreria.cloper;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.tesoreria.cloper.CloperWsBSC.ParametrosConexionCloperWS;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ActualizarTransaccion implements JavaCall {

	// DEFINICION DE CONSTANTES
	private static final Logger logger = LogManager.getLogger(ActualizarTransaccion.class);

	//QUERIES
	private final String SELECT_TRANSACCION			= loadResourceToString("cl/bcs/tesoreria/sql/ActualizarTransaccion_SelectTransaccion.sql");
	private final String UPDATE_TRANSACCION			= loadResourceToString("cl/bcs/tesoreria/sql/ActualizarTransaccion_UpdateTransaccion.sql");
	private final String SELECT_CODIGO_CORREDORA	= loadResourceToString("cl/bcs/tesoreria/sql/ActualizarTransaccion_SelectCodigoCorredora.sql");

	//-----------------------------------------------------
	// DEFINICION DE CLASES DE ENTRADA
	//------------------------------------------------------
	public static class ActualizarTransaccion_Entrada extends BcsPojo{
		public Long 		p_IdEmpresa;		
		public String 		p_FechaTransaccion;		
		public String 	    p_FolioTransaccion;   //Puede ser null para consultar solo por fecha
				
	}
	
	
	//-----------------------------------------------------
	// DEFINICION DE CLASES DE SALIDA
	//------------------------------------------------------
	
	public static class ActualizarTransaccion_Salida extends BcsPojo{
		public List<MensajeSalida>		P_OUT_MENSAJES;
		public Long 		     		P_OUT_ERRORESCONTADOR;
		
		public ActualizarTransaccion_Salida(){
			P_OUT_ERRORESCONTADOR = new Long(0);			
			P_OUT_MENSAJES 		  = new ArrayList<MensajeSalida>();
		}		
	}
	
	//-----------------------------------------------
	// CLASES UTILITARIAS 
	//-----------------------------------------------
	
	public static class Transaccion extends BcsPojo{
		public Long					IdTransaccion; 
		public String				Folio;
		public String               Nemotecnico;
		public BigDecimal			Cantidad;
		public BigDecimal			Precio;		
		public BigDecimal			Monto;
		public java.sql.Date		FechaSistemaCreacion; //Fecha de sistema en que se ingresa la transacion. Es igual a FechaOperacion pero sin hora
		public java.sql.Date		FechaLiquidacion;
		public String				IdCamara;
		public String				EsBilateral;
		
	}

	public static class CodigoCorredora extends BcsPojo{
		public String	NombreCorto;
		public Long	 	IdCorredora;
		public String	CodigoCorredora;
		public String	IdBolsa;
		
		public CodigoCorredora(){
			this.NombreCorto = "";
			this.IdCorredora = 0L;
			this.CodigoCorredora = "";
			this.IdBolsa = "";
		}
	}
	
	/*******************
	 * método principal
	 * @throws Exception 
	 *******************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception{				
		Gson gson = new Gson();
		
		ActualizarTransaccion_Entrada in = gson.fromJson(jin.toString() ,ActualizarTransaccion_Entrada.class);
		ActualizarTransaccion_Salida out = logica(con,in);
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	/**
	 * Consulta el WS cloper para obtener data de las transacciones de negociación de acuerdo a los parámetros de consulta   
	 * @param entrada: parámetros de entrada al servicio, codigoCorredora: codigo de la corredora, parametrosConexionWS: parametros de conexion cloper Ws 
	 * @return: lista de transaciones de cloper 
	 * @throws Exception
	 */
	
	public List<Cloper> obtenerDataCloper(ActualizarTransaccion_Entrada entrada, String codigoCorredora, ParametrosConexionCloperWS parametrosConexionWS) throws Exception {
		logger.trace("INICIO::ActualizarTransaccion.obtenerDataCloper");
		long inicio = System.currentTimeMillis();
		
		List<Cloper> arrCloper = null;
		
		try {			

			JsonArray arrEntradaCloperWS = new JsonArray();
			JsonObject objEntradaCloperWS = new JsonObject();

			objEntradaCloperWS.addProperty("id", "1");
			objEntradaCloperWS.addProperty("fecha", entrada.p_FechaTransaccion);
			if (entrada.p_FolioTransaccion != null && !entrada.p_FolioTransaccion.equals("0") && !entrada.p_FolioTransaccion.equals("?")) {
				objEntradaCloperWS.addProperty("folio", entrada.p_FolioTransaccion);
			}else {
				objEntradaCloperWS.addProperty("folio", "0");
			}
			
			objEntradaCloperWS.addProperty("usuario", codigoCorredora);
			arrEntradaCloperWS.add(objEntradaCloperWS);
			String bodyCloperWS = arrEntradaCloperWS.toString();
			
			Cloper cloper = new Cloper();
			arrCloper = cloper.obtenerDataCloper(bodyCloperWS, parametrosConexionWS);
		}
		catch(Exception e) {
			logger.debug(e.getMessage());
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ActualizarTransaccion.obtenerDataCloper", fin - inicio);
		return arrCloper;		
	}
	
	private CodigoCorredora obtenerCodigoCorredora(Connection con) throws SQLException{
		logger.debug("INICIO::ActualizarTransaccion.obtenerCodigoCorredora");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		
		CodigoCorredora codigoCorredora = new CodigoCorredora();
		
		List<CodigoCorredora> listaCodigosCorredoras = sql.query(con, SELECT_CODIGO_CORREDORA, new HashMap<>(), CodigoCorredora.class);
		
		if(listaCodigosCorredoras.size() > 0) {
			codigoCorredora = listaCodigosCorredoras.get(0);
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ActualizarTransaccion.obtenerCodigoCorredora", fin - inicio);
		return codigoCorredora;
	}
	
	private ParametrosConexionCloperWS definirParametrosCloper (Connection con, Long idEmpresa, ActualizarTransaccion_Salida salida){
		logger.debug("INICIO::ActualizarTransaccion.definirParametrosCloper");
		long inicio = System.currentTimeMillis();
		
		ParametrosConexionCloperWS parametrosWS = new ParametrosConexionCloperWS();
		
		try {
			String password = ConfiguracionSistema.getParametroConfiguracionString(con,idEmpresa,"PLATAFORMA","PLA.CloperWsBSC.Password");
			String profile = ConfiguracionSistema.getParametroConfiguracionString(con,idEmpresa,"PLATAFORMA","PLA.CloperWsBSC.Profile");
			String wsdl = ConfiguracionSistema.getParametroConfiguracionString(con,idEmpresa,"PLATAFORMA","PLA.CloperWsBSC.Wsdl");
						
			parametrosWS.password = password;
			parametrosWS.profile = profile;
			parametrosWS.wsdl = wsdl;
			
		} catch (Exception e) {
			logger.error(e.getMessage());
			salida.P_OUT_ERRORESCONTADOR++;
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ActualizarTransaccion.definirParametrosCloper", fin - inicio);
		return parametrosWS;
	}
	
	private List<Transaccion> consultarTransacciones(Connection con, SQL sql, HashMap<String,Object> parmsInTransacciones) throws SQLException {
		logger.debug("INICIO::ActualizarTransaccion.consultarTransacciones");
		long inicio = System.currentTimeMillis();
		
		List<Transaccion> listaTransacciones = new ArrayList<>();
		listaTransacciones = sql.query(con, SELECT_TRANSACCION,parmsInTransacciones,Transaccion.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ActualizarTransaccion.consultarTransacciones", fin - inicio);
		return listaTransacciones;
	}
	
	private void actualizarTransacciones(Connection con, SQL sql, List<Transaccion> listaUpdateTransaccion,ActualizarTransaccion_Salida salida, Long idEmpresa) throws Exception {
		logger.debug("INICIO::ActualizarTransaccion.actualizarTransacciones");
		long inicio = System.currentTimeMillis();
		
		try {
			sql.batch(con, UPDATE_TRANSACCION, listaUpdateTransaccion, Transaccion.class);
		} catch (SQLException e) {
			logger.error(e.getMessage());
			salida.P_OUT_ERRORESCONTADOR++;
			salida.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR,"ERROR_ACTUALIZAR_TRANSACCION", new HashMap<>(), con, idEmpresa));
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ActualizarTransaccion.actualizarTransacciones", fin - inicio);
	}
	
	//-----------------------------------------------
	// LOGICA 
	//-----------------------------------------------	
	public ActualizarTransaccion_Salida logica(Connection con, ActualizarTransaccion_Entrada entrada) throws Exception {
		logger.info("INICIO::ActualizarTransaccion");
		//Setea parámeteros de salida 
		ActualizarTransaccion_Salida salida = new ActualizarTransaccion_Salida();		
		SQL sql = new SQL();
		
		logger.trace("PARAMETROS DE ENTRADA : " + entrada.toString());
		
		//========= INICIO OBTENER TRANSACCIONES OPTIMUS =========
		logger.trace("INICIO LECTURA TRANSACCIONES OPTIMUS");
		
        //Setea parametros de consulta de transacciones
		HashMap<String,Object> parmsInTransacciones = new HashMap<String,Object>(3);		
		parmsInTransacciones.put("IdEmpresa", entrada.p_IdEmpresa);
		parmsInTransacciones.put("FechaSistemaCreacion", entrada.p_FechaTransaccion);
		
		if (entrada.p_FolioTransaccion != null && !entrada.p_FolioTransaccion.equals("0") && !entrada.p_FolioTransaccion.equals("?")){
			parmsInTransacciones.put("FolioTransaccion", entrada.p_FolioTransaccion);  
		}else {
			parmsInTransacciones.put("FolioTransaccion", "");
		}
		
		List<Transaccion> listaTransacciones = consultarTransacciones(con, sql, parmsInTransacciones);
		
		if (listaTransacciones.size() == 0){	
			logger.info("NO SE ENCONTRARON TRANSACCIONES PARA LA FECHA INDICADA");
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,"NO_SE_ENCONTRARON_TRANSACCIONES_PARA_FECHA_INDICADA",null,con , entrada.p_IdEmpresa));
			return salida;
		}
		
		logger.trace("SE ENCONTRARON "+ listaTransacciones.size() +" TRANSACCION(ES) PARA LA FECHA INDICADA");
		
		logger.trace("FIN LECTURA TRANSACCIONES OPTIMUS");
		//========= FIN OBTENER TRANSACCIONES OPTIMUS =========
		
		//========= INICIO OBTENER PALOS CLOPER(SCL) =========
		CodigoCorredora codigoCorredora = obtenerCodigoCorredora(con);
		String codigoCorr  = codigoCorredora != null ? codigoCorredora.CodigoCorredora : "0";
		
		ParametrosConexionCloperWS parametrosWS = new ParametrosConexionCloperWS();
		parametrosWS = definirParametrosCloper(con, entrada.p_IdEmpresa, salida);
		
		if(salida.P_OUT_ERRORESCONTADOR > 0L) {
			salida.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR,"PARAMETROS_CLOPER_WS_NO_ENCONTRADO", new HashMap<>(), con, entrada.p_IdEmpresa));
			return salida;
		}
		
		List<Cloper> arrCloper = new ArrayList<>();
		arrCloper = obtenerDataCloper(entrada,codigoCorr, parametrosWS);
		
		if (arrCloper == null){
			logger.error("ERROR AL CONSULTAR CLOPER WS");
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ERROR_AL_CONSULTAR_CLOPER_WS",new HashMap<>(),con , entrada.p_IdEmpresa));
			return salida;
		}
		if(arrCloper.isEmpty()) {
			logger.info("NO SE ENCONTRARON PALOS EN LA CCLV PARA PROCESAR");
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,"NO_SE_ENCONTRARON_PALOS_EN_LA_CCLV_PARA_PROCESAR",new HashMap<>(),con , entrada.p_IdEmpresa));
			return salida;
		}
		
		logger.trace("SE ENCONTRARON "+ arrCloper.size() +" PALOS EN EL SISTEMA DE COMPENSACION Y LIQUIDACION");

		//========= FIN OBTENER PALOS CLOPER(SCL) =========
		
		//========= INICIO ACTUALIZACION DE TRANSACCIONES  =========		
		
		/*********************************************************
		//NO BORRAR: usar estas condiciones para calzar trasacciones optimus con palos cloper   
		v_OperacionesCamara	= 
		SELECT 
		     TRA."IdTransaccion"
			,CAM."CAMARA"
			,(CASE
				WHEN (CAM."TIPO_LIQUIDA" = 'B') THEN 'S'
				WHEN (CAM."TIPO_LIQUIDA" = 'M' AND (CAM."ESTADO" = 'RZ' OR CAM."ESTADO" = 'E')) THEN 'S'
				ELSE 'N'
			END) AS "ESBILATERAL"
		FROM "RUEBAS"."TBCLOPER" AS CAM
		INNER JOIN :v_TransaccionesActualizar AS TRA ON(
							        CAM.FOLIO    = TRA."FolioTransaccion"
						        AND CAM.NEMO     = TRA."InstrumentoUI" 
						        AND CAM.FECHA    = TRA."FechaSistemaCreacion"
						        AND CAM.CANTIDAD = (CASE WHEN (TRA."IdClasificacionTipoInstrumento" <> 'IF') THEN TRA."CantidadTransaccion" ELSE CAM."CANTIDAD" END)  -- Para no IF (join por cantidad)
						        AND CAM.MONTO    = (CASE WHEN (TRA."IdClasificacionTipoInstrumento" = 'IF')  THEN TRA."MontoTransaccion"    ELSE CAM."MONTO" END)     -- Para IF    (join por monto)	
							)
		WHERE CAM."HORA_SCL" <> ''
		AND   CAM."ESTADO"     <> 'P'
		AND   CAM."CAMARA" IN ('RV','PM','SM','PH');
		**********************************************************/	
		
		logger.trace("INICIO CALCES Y ACTUALIZACION DE TRANSACCIONES");
		
		List<Transaccion> listaUpdateTransaccion = new ArrayList<Transaccion>();
		for(Transaccion transaccion : listaTransacciones){
			
			Cloper transaccionCCLV = arrCloper.stream()
					.filter(cloper -> transaccion.Folio.equalsIgnoreCase(cloper.folio.toString().trim()) && transaccion.Nemotecnico.equalsIgnoreCase(cloper.nemo.toString().trim()) &&
							transaccion.FechaSistemaCreacion.toString().equalsIgnoreCase(cloper.fecha.toString().trim()) && transaccion.Cantidad.compareTo(cloper.cantidad) == 0 && 
							transaccion.Monto.compareTo(cloper.monto) == 0)
					.findFirst()
					.orElse(null);
			
			if(transaccionCCLV != null) {
				//Marcar camara para edición
				transaccion.IdCamara =  transaccionCCLV.camara.trim();
				
				//Marcar EsBilateral para edición
				if ("B".equalsIgnoreCase(transaccionCCLV.tipo_liquida.toString().trim()) || ( "M".equalsIgnoreCase(transaccionCCLV.tipo_liquida.toString().trim()) && 
						("RZ".equalsIgnoreCase(transaccionCCLV.estado.toString().trim()) || "E".equalsIgnoreCase(transaccionCCLV.estado.toString().trim())))) {
					transaccion.EsBilateral =  "S";
				} else {
					transaccion.EsBilateral =  "N";
				}
				listaUpdateTransaccion.add(transaccion);
			}
		}

		//Valida que existan transacciones para actualizar
		if (listaUpdateTransaccion.size() == 0)	{
			logger.trace("NO SE ENCONTRARON CALCES ENTRE LAS TRANSACCIONES Y LOS PALOS DEL CCLV");
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,"NO_SE_ENCONTRARON_CALCES_ENTRE_LAS_TRANSACCIONES_Y_LOS_PALOS_DEL_CCLV",new HashMap<>(),con ,entrada.p_IdEmpresa));
			return salida;
		}else {
			actualizarTransacciones(con, sql, listaUpdateTransaccion, salida, entrada.p_IdEmpresa);
		}
		
		if(salida.P_OUT_ERRORESCONTADOR == 0) {
			logger.trace(listaUpdateTransaccion.size() + " TRANSACCIONES SE CALZARON CON LOS PALOS DEL CCLV Y SERAN ACTUALIZADAS");
			String codigo = "TRANSACCIONES_ACTUALIZADAS_CON_CALCE_EN_LA_CCLV";
			String descripcion = Diccionario.getMensaje(con, entrada.p_IdEmpresa, codigo).add("Cantidad", listaUpdateTransaccion.size()).msg();
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,codigo,descripcion));
		}
		
		logger.trace("FIN CALCES Y ACTUALIZACION DE TRANSACCIONES");
		
		logger.info("FIN::ActualizarTransaccion");
		return salida;
	}
	
	//-----------------------------------------------
	// FIN LOGICA 
	//-----------------------------------------------	
	
	//-----------------------------------------------
	// METODO PARA PRUEBAS LOCALES 
	//-----------------------------------------------	
	
	public static void main(String[] args)  throws Exception
	{		
		System.out.println("== INICIO PRUEBA LOCAL ==");
 		     
		try (Connection con = PlataformaUtils.getQAConnection())
		{	
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();

			String test = "{\"p_IdEmpresa\":1,\"p_FechaTransaccion\":\"2018-06-27\",\"p_FolioTransaccion\":\"?\"}";
			
			ActualizarTransaccion_Entrada  Objin = gson.fromJson(test, ActualizarTransaccion_Entrada.class);
			ActualizarTransaccion demo = new ActualizarTransaccion();
 		        
 			System.out.println("ENTRADA:");
 			System.out.println(Objin.toString());
 			
 			try 
 			{	
 				ActualizarTransaccion_Salida  ObjOut = demo.logica(con, Objin);
 				System.out.println(ObjOut.toString());
 				//con.commit();
 			}
 			catch(Exception er) 
 			{		
 				//er.printStackTrace();
 				logger.error(er.getMessage());
				con.rollback();
			}
 			
			con.close();
		}

		System.out.println("== FIN PRUEBA LOCAL ==");
	}
	
}

