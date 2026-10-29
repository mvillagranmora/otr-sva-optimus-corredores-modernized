package cl.bcs.plataforma.pla;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pau.CambiarEstadoModuloMesaSpot;
import cl.bcs.plataforma.pau.CambiarEstadoModuloMesaSpot.EntradaAbrirModuloMesaSpot;
import cl.bcs.plataforma.pau.CambiarEstadoModuloMesaSpot.OutAdvertenciasDetalle;
import cl.bcs.plataforma.pau.CambiarEstadoModuloMesaSpot.OutErroresDetalle;
import cl.bcs.plataforma.pau.CambiarEstadoModuloMesaSpot.OutParidadesTipoCambio;
import cl.bcs.plataforma.pau.CambiarEstadoModuloMesaSpot.SalidaAbrirModuloMesaSpot;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
//import cl.bcs.utils.//statsD;
import cl.bcs.utils.Utils;


public class AperturaMesasModulos implements JavaCall {	
	private static final Logger logger 		= LogManager.getLogger(AperturaMesasModulos.class);
	public static final String PREFIX 		= "plataforma.pla.AperturaMesasModulos";	
	
	private String SQL_ABRIR_MESAS						= loadResourceToString("cl/bcs/plataforma/pla/sql/AperturaMesasModulosAperturaMesas.sql");
	private String SQL_ABRIR_MODULOS					= loadResourceToString("cl/bcs/plataforma/pla/sql/AperturaMesasModulosAperturaModulos.sql");	
	private String SQL_CONSULTAR_PARIDAD_TIPO_CAMBIO	= loadResourceToString("cl/bcs/plataforma/pla/sql/AperturaMesasModulosConsultarParidadTipoCambio.sql");	
	
	private String SQL_SELECT_MODULOS_APERTURA				= "SELECT \"Valor\" AS \"Modulos\" FROM \"BSCL.Plataforma.BD::PLA.Configuracion\" WHERE \"CodigoParametro\"  = 'PLA.AperturaDiariaModulos'";
	private String SQL_SELECT_MESA_SPOT						= "SELECT \"IdMesa\" , \"Nombre\"  FROM BSCL.\"BSCL.Mesa.BD::MES.Mesa\" WHERE \"Codigo\"  = 'MESA_SPOT'";
	private String SQL_CREATE_TABLA_TEMPORAL_APERTURA_MESA 	= "CREATE LOCAL TEMPORARY TABLE #AperturaMesa_p_DatosMesas(\"IdMesa\" BIGINT, \"NombreMesa\"    NVARCHAR(30), \"IdEstadoMesa\"  NVARCHAR(3));";
	private String SQL_CREATE_TEMPORARY_TABLE_ABRIR_MODULO  = "CREATE LOCAL TEMPORARY TABLE #AbrirModulo_p_Modulo (\"CodigoModulo\" NVARCHAR(40),\"Estado\" NVARCHAR(3),\"CodigoUsuario\" NVARCHAR(40));";
	private String SQL_INSERT_TABLA_TEMPORAL_APERTURA_MESA 	= "INSERT INTO #AperturaMesa_p_DatosMesas(\"IdMesa\",\"NombreMesa\",\"IdEstadoMesa\")VALUES(:IdMesa,:NombreMesa,'A');";
	private String SQL_INSERT_TEMPORARY_TABLE_ABRIR_MODULO  = "INSERT INTO #AbrirModulo_p_Modulo(\"CodigoModulo\",\"Estado\",\"CodigoUsuario\")VALUES(:CodigoModulo,:Estado,:CodigoUsuario)";

	
	public static class EntradaAperturaMesasModulos extends BcsPojo {
		public Long						p_IdEmpresa ;
		public Long						p_IdUsuario ;
		public String					p_IdModulo  ;
	}
	public static class ErrorDetalle{
		public String Codigo;
		public String Descripcion;
	}
	public static class FechasSistema {
        public String FechaSistema;
        public String FechaSistemaAnterior;
    }
	public static class ErrorDetalleModulo{
		public Long IdInterno;
		public String Codigo;
		public String Descripcion;
	}
	public static class ModulosApertura{
		public String Modulos;
	}
	public static class Mesa_Spot{
		public Long 	IdMesa;
		public String 	Nombre;
	}
	public static class Mesa{
		public Long IdMesa;
		public String NombreMesa;
	}	
	public static class Modulo{
		public String 	CodigoModulo; 
		public String 	Estado; 
		public String 	CodigoUsuario;
	}
	public static class ResultadoMesa{
		public Long IdMesa;
		public String Resultado;
	}
	public static class ResultadoModulo{
		public String CodigoModulo;
		public String IdEstado;
		public String NombreModulo;
	}
	public static class AdvertenciasDetalle{
		public String Codigo;
		public String RequiereAutorizacion;
		public String Descripcion;
	}
	public static class AdvertenciasDetalleModulo{
		public String IdInterno;
		public String Codigo;
		public String RequiereAutorizacion;
		public String Descripcion;
	}
	public static class ParametroEntrada {
		public String moduloParametro;
		public String nombreParametro;	
	}
	
	//clases de Salida
	public static class SalidaAperturaMesasModulos{
		public List<MensajeSalida>			P_OUT_MENSAJES 			= new ArrayList<>();
		public Long							P_OUT_ERRORESCONTADOR	= 0L;
		
		public String toString() {
			return Utils.printJson(this, false);
		};

	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaAperturaMesasModulos in = gson.fromJson(jin.toString(), EntradaAperturaMesasModulos.class);
		SalidaAperturaMesasModulos out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	public static FechasSistema fechas = new FechasSistema();
	public static void establecerFechas(String hoy, String anterior) {
        fechas.FechaSistema = hoy;
        fechas.FechaSistemaAnterior = anterior;
    }

	public boolean verificarMonedas (Connection con,SQL sql, EntradaAperturaMesasModulos entrada) throws Exception{
		String FechaSistema = fechas.FechaSistema;
		HashMap<String,Object> parmsInTipoCambio =new HashMap<String,Object>();
		parmsInTipoCambio.put("p_IdEmpresa"	, entrada.p_IdEmpresa);
		parmsInTipoCambio.put("p_IdUsuario"	, entrada.p_IdUsuario);
		parmsInTipoCambio.put("p_Fecha"		, Fecha.toDate(FechaSistema));
		
		HashMap<String,Class> parmsOutTipoCambio =new HashMap<String,Class>();
		parmsOutTipoCambio.put("p_out_ParidadesYTipoCambio"	, OutParidadesTipoCambio.class);
		parmsOutTipoCambio.put("p_out_AdvertenciasContador"	, Long.class);
		parmsOutTipoCambio.put("p_out_ErroresContador"		, Long.class);
		parmsOutTipoCambio.put("p_out_AdvertenciasDetalle"	, OutAdvertenciasDetalle.class);
		parmsOutTipoCambio.put("p_out_ErroresDetalle"		, OutErroresDetalle.class);
		
		HashMap<String, Object> outConsultarParidadTipoCambio = sql.call(con, SQL_CONSULTAR_PARIDAD_TIPO_CAMBIO, parmsInTipoCambio, parmsOutTipoCambio);
		List<OutParidadesTipoCambio> outParidadTipoCambio = (List<OutParidadesTipoCambio>) outConsultarParidadTipoCambio.get("p_out_ParidadesYTipoCambio");

		boolean existeTipoCambiousd = false , existeTipoCambiouf  = false;
		for(OutParidadesTipoCambio tipoCambio : outParidadTipoCambio) {
			if("DO".equalsIgnoreCase(tipoCambio.IdMoneda)) {
				if(tipoCambio.TipoCambioMercado.compareTo(BigDecimal.ZERO) != 0)
					existeTipoCambiousd = true;
				
			}
			if("UF".equalsIgnoreCase(tipoCambio.IdMoneda)) {
				if(tipoCambio.TipoCambioMercado.compareTo(BigDecimal.ZERO) != 0)
					existeTipoCambiouf = true;
				
			}
		}
		if (existeTipoCambiouf && existeTipoCambiousd) {
			return true;
		}
		logger.info("Valores para D0 : "+ existeTipoCambiousd + " - Valores para UF: " + existeTipoCambiouf );
		return false;
	}
	
	public SalidaAperturaMesasModulos AbrirMesas (Connection con,SQL sql, EntradaAperturaMesasModulos entrada , SalidaAperturaMesasModulos Salida) throws SQLException {
		HashMap<String, Object> parametrosEntrada = new HashMap<String, Object>(3);
		parametrosEntrada.put("IdEmpresa", entrada.p_IdEmpresa);
		parametrosEntrada.put("IdUsuario", entrada.p_IdUsuario);
		
		HashMap<String, Class> parametrosSalida = new HashMap<String, Class>(6);
		parametrosSalida.put("p_out_ResultadoProceso"		, ResultadoMesa.class		);
		parametrosSalida.put("p_out_AdvertenciasContador"	, Long.class				);
	    parametrosSalida.put("p_out_AdvertenciasDetalle"	, AdvertenciasDetalle.class	); 
	    parametrosSalida.put("p_out_ErroresContador"		, Long.class				);
	    parametrosSalida.put("p_out_ErroresDetalle"			, ErrorDetalle.class		);
	    
	    HashMap<String, Object> respuestaAperturaMesa = new HashMap<String, Object>();
	    
	    try {
			SQL.exec(con, "DROP TABLE #AperturaMesa_p_DatosMesas");
		} catch(Exception e) {
			logger.info("Tabla #AperturaMesa_p_DatosMesas no existe");
		}
		SQL.exec(con, SQL_CREATE_TABLA_TEMPORAL_APERTURA_MESA);
		
		
		
		List<Mesa> mesas = new ArrayList<Mesa>();
		Mesa_Spot MesaSpot = sql.query(con, SQL_SELECT_MESA_SPOT, new HashMap<>(0) , Mesa_Spot.class).get(0);
		Mesa newMesa = new Mesa();
		newMesa.IdMesa 		= MesaSpot.IdMesa;  
		newMesa.NombreMesa 	= MesaSpot.Nombre;
		mesas.add(newMesa);
		
		sql.batch(con, SQL_INSERT_TABLA_TEMPORAL_APERTURA_MESA, mesas, Mesa.class);		
		respuestaAperturaMesa = sql.call(con, SQL_ABRIR_MESAS, parametrosEntrada, parametrosSalida);
		
		Long ErroresApertura = (Long) respuestaAperturaMesa.get("p_out_ErroresContador");
		return Salida;
	}
	
	public static SalidaAperturaMesasModulos AbrirSpot(Connection con, EntradaAperturaMesasModulos entrada ) {
		SalidaAperturaMesasModulos salida = new SalidaAperturaMesasModulos();
    	try {
			String fechaSistema = fechas.FechaSistema;
			String fechaSistemaAnterior = fechas.FechaSistemaAnterior;
			
			CambiarEstadoModuloMesaSpot cestspt = new CambiarEstadoModuloMesaSpot();
			EntradaAbrirModuloMesaSpot incestspt = new EntradaAbrirModuloMesaSpot();
			incestspt.p_IdEmpresa = entrada.p_IdEmpresa;
			incestspt.p_IdUsuario = entrada.p_IdUsuario;
			incestspt.p_IdModulo = "PLA";
			incestspt.p_simularOperacion = "N";
			incestspt.p_UsaReglasNegocio = "N";
			incestspt.p_FechaSistema = fechaSistema;
			incestspt.p_FechaSistemaAnterior = fechaSistemaAnterior;
			
			SalidaAbrirModuloMesaSpot outcestspt = cestspt.logica(con, incestspt);
			
			List<MensajeSalida> Mensajes = outcestspt.p_out_Mensajes;
			salida.P_OUT_ERRORESCONTADOR = outcestspt.p_out_ErroresContador;
			if(salida.P_OUT_ERRORESCONTADOR > 0) {
				salida.P_OUT_MENSAJES = Mensajes;
			}			
			logger.info("Errores: " + salida.P_OUT_ERRORESCONTADOR);
		} catch (Exception e) {
    		logger.info(e);
    		logger.info("***** Error al AbrirModuloMesaSpot *****");
    	}
		return salida;
    }
	
	public SalidaAperturaMesasModulos AbrirModulos(Connection con,SQL sql, EntradaAperturaMesasModulos entrada , SalidaAperturaMesasModulos Salida) throws SQLException  {

		ModulosApertura ModuloConfiguracion = sql.query(con, SQL_SELECT_MODULOS_APERTURA, new HashMap<>(0) , ModulosApertura.class).get(0);		
		//ModuloConfiguracion.Modulos = "MSP;FAC;CUS"; // debug seteo de modulos manualmente para mo tocar el parametro 
	    List<String> resultList = new ArrayList<>(Arrays.asList(ModuloConfiguracion.Modulos.split(";")));
	    logger.info("modulos a abrir : " + resultList);
	    
	    if (resultList.remove("MSP") ) {
	    	SalidaAperturaMesasModulos salidaAperturaSpot = new SalidaAperturaMesasModulos();
	    	salidaAperturaSpot = AbrirSpot(con , entrada); // modulo spot se abre de manera diferente
	    	
        }
	    try {
			SQL.exec(con, "DROP TABLE #AbrirModulo_p_Modulo");
		} catch(Exception e) {
			logger.info("Tabla #AbrirModulo_p_Modulo no existe");
		}
	    SQL.exec(con, SQL_CREATE_TEMPORARY_TABLE_ABRIR_MODULO);
		
	    List<Modulo> modulos = new ArrayList<>();
	    for (String codigoModulo : resultList) {
	        Modulo modulo = new Modulo();
	        modulo.CodigoModulo 	= codigoModulo;
	        modulo.Estado 			= "ABT";
	        modulo.CodigoUsuario 	= "SISTEMA";	        
	        modulos.add(modulo);
	    }
		
	    sql.batch(con, SQL_INSERT_TEMPORARY_TABLE_ABRIR_MODULO, modulos, Modulo.class);
		HashMap<String,Object> parmsInModulo =new HashMap<String,Object>();
		parmsInModulo.put("p_IdEmpresa"	, entrada.p_IdEmpresa);
		parmsInModulo.put("p_IdUsuario"	, entrada.p_IdUsuario);
		parmsInModulo.put("p_IdModulo"	, entrada.p_IdModulo);
		parmsInModulo.put("p_Modulo"	, modulos);
		parmsInModulo.put("p_MesaAbierta", 'N');
		
	    HashMap<String, Class> parametrosSalidaModulo = new HashMap<String, Class>(6);
	    parametrosSalidaModulo.put("p_out_Resultado"			, ResultadoModulo.class		);
	    parametrosSalidaModulo.put("p_out_AdvertenciasContador"	, Long.class				);
	    parametrosSalidaModulo.put("p_out_AdvertenciasDetalle"	, AdvertenciasDetalleModulo.class	);
	    parametrosSalidaModulo.put("p_out_ErroresContador"		, Long.class				);
	    parametrosSalidaModulo.put("p_out_ErroresDetalle"		, ErrorDetalleModulo.class	);
	    
	    HashMap<String, Object> respuestaAperturaModulo = new HashMap<String, Object>();
	    respuestaAperturaModulo = sql.call(con, SQL_ABRIR_MODULOS, parmsInModulo, parametrosSalidaModulo);	
			
	    Long ErroresModulos = (Long) respuestaAperturaModulo.get("p_out_ErroresContador");
	    
	    return Salida;	    
	    //String message = "Modulos abiertos :" + ModuloConfiguracion.Modulos + "\n";
	    //message = message + "MEsas abiertas :" + ModuloConfiguracion.Modulos + "\n";
	    ////statsD.reportEventSuccess(PREFIX , message, "");		
	}
	public SalidaAperturaMesasModulos logica(Connection con, EntradaAperturaMesasModulos entrada) 
	{	
	long inicioServicio = System.currentTimeMillis();
	logger.trace("INICIO::AperturaMesasModulos");
	SalidaAperturaMesasModulos Salida = new SalidaAperturaMesasModulos();
	
	Boolean ExitoAperturaMesas 		= false;
	Boolean ExitoAperturaModulos 	= false;
	
	  try{
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con,(Long)entrada.p_IdEmpresa,"PLATAFORMA","PLA.FechaSistema");
		String fechaSistemaAnterior = ConfiguracionSistema.getParametroConfiguracionString(con,(Long)entrada.p_IdEmpresa,"PLATAFORMA","PLA.FechaSistemaAnterior");
		//fechaSistema = "2023-12-05";
		establecerFechas(fechaSistema,fechaSistemaAnterior);
  		SQL sql = new SQL();
  		if ( !verificarMonedas(con,sql,entrada) ) {
  			MensajeSalida FalloValidacion = new MensajeSalida();
  			FalloValidacion.tipo = "ADVERTENCIA";
  			FalloValidacion.descripcion = "No hay Valores UF o USD para el dia";
  			Salida.P_OUT_MENSAJES.add(FalloValidacion);
  			Salida.P_OUT_ERRORESCONTADOR = Salida.P_OUT_ERRORESCONTADOR + 1L;
  			return Salida;
  		}
  		
  		SalidaAperturaMesasModulos SalidaModulos 	= AbrirModulos(con, sql, entrada, Salida);
  		SalidaAperturaMesasModulos SalidaMesas		= AbrirMesas(con, sql, entrada, Salida);
  		
  		
  		
  		long finServicio = System.currentTimeMillis();  		
	    
  		logger.info("FIN::AperturaMesasModulos", finServicio - inicioServicio);
  		//statsD.reportExecutionTimeMilis(PREFIX, finServicio - inicioServicio, "");
	  }catch (Exception e){
		  Salida.P_OUT_ERRORESCONTADOR = Salida.P_OUT_ERRORESCONTADOR + 1L;
		  MensajeSalida Errorejecucion = new MensajeSalida();
		  Errorejecucion.tipo = "Error";
		  Errorejecucion.descripcion = e.toString();
		  Salida.P_OUT_MENSAJES.add(Errorejecucion);		  
		  //statsD.reportException("Error : ", e, "");
		  logger.error(e);
		  e.printStackTrace();		  
	  }
	  return Salida;
	}

	public static void main(String[] args) throws Exception {
	  try (Connection con = PlataformaUtils.getDESAConnection()) {
	  	SalidaAperturaMesasModulos salida = new SalidaAperturaMesasModulos();
	  	try {
	  	  Gson gson = Utils.getGsonBuilder();	
	  	  String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":\"0\", \"p_IdModulo\":\"CUS\"}";
	  	  EntradaAperturaMesasModulos Objin = gson.fromJson(json, EntradaAperturaMesasModulos.class);  
	  	  AperturaMesasModulos  kk = new AperturaMesasModulos();
	  	  salida = kk.logica(con, Objin);
	  	  System.out.println(salida);
	  	  //Thread.sleep(10000);		//para que //statsD pueda reportar en debug
	  	  con.rollback();
	  	  con.close();
	    }catch(Exception er) {
		  System.out.println(er);
		  System.out.println(salida);
	  	  er.printStackTrace();	
	  	  con.rollback();
	  	}
	  } catch (Exception e) {
	  	e.printStackTrace();			
	  }	
	  System.out.println("== END MAIN ==");
	};
}