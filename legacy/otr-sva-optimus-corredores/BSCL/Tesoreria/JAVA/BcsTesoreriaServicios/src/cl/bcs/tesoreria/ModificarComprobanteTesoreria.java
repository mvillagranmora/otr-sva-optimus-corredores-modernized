package cl.bcs.tesoreria;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class ModificarComprobanteTesoreria implements JavaCall{
	
		static final Logger logger = LogManager.getLogger(ModificarComprobanteTesoreria.class);
		private final String SQL_UPDATE_INGRESOEGRESO 				= loadResourceToString("cl/bcs/tesoreria/sql/ModificarComprobanteTesoreria_UpdateIngresoEgreso.sql");
		private final String SQL_UPDATE_INGRESOEGRESOFORMADEPAGO	= loadResourceToString("cl/bcs/tesoreria/sql/ModificarComprobanteTesoreria_UpdateIngresoEgresoFormaDePago.sql");
		private final String SQL_UPDATE_COMPROBANTE 				= loadResourceToString("cl/bcs/tesoreria/sql/ModificarComprobanteTesoreria_UpdateComprobante.sql");
		private final String SQL_UPDATE_COMPROBANTEDETALLE			= loadResourceToString("cl/bcs/tesoreria/sql/ModificarComprobanteTesoreria_UpdateComprobanteDetalle.sql");
		private final String SQL_UPDATE_COMPROBANTEFORMADEPAGO 		= loadResourceToString("cl/bcs/tesoreria/sql/ModificarComprobanteTesoreria_UpdateComprobanteFormaDePago.sql");
		
		private final String SQL_EXIST_TABLA_INGRESOEGRESO   				= "select 1 AS \"Tab\" from m_temporary_tables where table_name = '#TES_MOD_COMPROBANTE_INGRESOEGRESO'";
		private final String SQL_CREATE_TABLA_INGRESOEGRESO  				= "CREATE LOCAL TEMPORARY TABLE #TES_MOD_COMPROBANTE_INGRESOEGRESO (\"IdIngresoEgreso\" BIGINT,\"Campo\" NVARCHAR(40),\"Valor\" NVARCHAR(40))";													
		private final String SQL_INSERT_TABLA_INGRESOEGRESO 				= "INSERT INTO #TES_MOD_COMPROBANTE_INGRESOEGRESO (\"IdIngresoEgreso\",\"Campo\",\"Valor\") VALUES (:IdIngresoEgreso,:Campo,:Valor)";		
		private final String SQL_DROP_TABLA_INGRESOEGRESO  					= "DROP TABLE #TES_MOD_COMPROBANTE_INGRESOEGRESO";
		
		private final String SQL_EXIST_TABLA_INGRESOEGRESOFORMADEPAGO   	= "select 1 AS \"Tab\" from m_temporary_tables where table_name = '#TES_MOD_COMPROBANTE_INGRESOEGRESOFORMADEPAGO'";
		private final String SQL_CREATE_TABLA_INGRESOEGRESOFORMADEPAGO  	= "CREATE LOCAL TEMPORARY TABLE #TES_MOD_COMPROBANTE_INGRESOEGRESOFORMADEPAGO (\"IdIngresoEgresoFormaDePago\" BIGINT,\"Campo\" NVARCHAR(40),\"Valor\" NVARCHAR(40))";													
		private final String SQL_INSERT_TABLA_INGRESOEGRESOFORMADEPAGO 		= "INSERT INTO #TES_MOD_COMPROBANTE_INGRESOEGRESOFORMADEPAGO (\"IdIngresoEgresoFormaDePago\",\"Campo\",\"Valor\") VALUES (:IdIngresoEgresoFormaDePago,:Campo,:Valor)";		
		private final String SQL_DROP_TABLA_INGRESOEGRESOFORMADEPAGO  		= "DROP TABLE #TES_MOD_COMPROBANTE_INGRESOEGRESOFORMADEPAGO";
		
		private final String SQL_EXIST_TABLA_COMPROBANTE   					= "select 1 AS \"Tab\" from m_temporary_tables where table_name = '#TES_MOD_COMPROBANTE_COMPROBANTE'";
		private final String SQL_CREATE_TABLA_COMPROBANTE  					= "CREATE LOCAL TEMPORARY TABLE #TES_MOD_COMPROBANTE_COMPROBANTE (\"IdComprobante\" BIGINT,\"Campo\" NVARCHAR(40),\"Valor\" NVARCHAR(40))";													
		private final String SQL_INSERT_TABLA_COMPROBANTE 					= "INSERT INTO #TES_MOD_COMPROBANTE_COMPROBANTE (\"IdComprobante\",\"Campo\",\"Valor\") VALUES (:IdComprobante,:Campo,:Valor)";		
		private final String SQL_DROP_TABLA_COMPROBANTE  					= "DROP TABLE #TES_MOD_COMPROBANTE_COMPROBANTE";
	
		private final String SQL_EXIST_TABLA_COMPROBANTEDETALLE   			= "select 1 AS \"Tab\" from m_temporary_tables where table_name = '#TES_MOD_COMPROBANTE_COMPROBANTEDETALLE'";
		private final String SQL_CREATE_TABLA_COMPROBANTEDETALLE  			= "CREATE LOCAL TEMPORARY TABLE #TES_MOD_COMPROBANTE_COMPROBANTEDETALLE (\"IdComprobanteDetalle\" BIGINT,\"Campo\" NVARCHAR(40),\"Valor\" NVARCHAR(40))";													
		private final String SQL_INSERT_TABLA_COMPROBANTEDETALLE 			= "INSERT INTO #TES_MOD_COMPROBANTE_COMPROBANTEDETALLE (\"IdComprobanteDetalle\",\"Campo\",\"Valor\") VALUES (:IdComprobanteDetalle,:Campo,:Valor)";		
		private final String SQL_DROP_TABLA_COMPROBANTEDETALLE  			= "DROP TABLE #TES_MOD_COMPROBANTE_COMPROBANTEDETALLE";
	
		private final String SQL_EXIST_TABLA_COMPROBANTEFORMADEPAGO   		= "select 1 AS \"Tab\" from m_temporary_tables where table_name = '#TES_MOD_COMPROBANTE_COMPROBANTEFORMADEPAGO'";
		private final String SQL_CREATE_TABLA_COMPROBANTEFORMADEPAGO  		= "CREATE LOCAL TEMPORARY TABLE #TES_MOD_COMPROBANTE_COMPROBANTEFORMADEPAGO (\"IdComprobanteFormaDePago\" BIGINT,\"Campo\" NVARCHAR(40),\"Valor\" NVARCHAR(40))";													
		private final String SQL_INSERT_TABLA_COMPROBANTEFORMADEPAGO 		= "INSERT INTO #TES_MOD_COMPROBANTE_COMPROBANTEFORMADEPAGO (\"IdComprobanteFormaDePago\",\"Campo\",\"Valor\") VALUES (:IdComprobanteFormaDePago,:Campo,:Valor)";		
		private final String SQL_DROP_TABLA_COMPROBANTEFORMADEPAGO  		= "DROP TABLE #TES_MOD_COMPROBANTE_COMPROBANTEFORMADEPAGO";
	
		
		
		
		
	public static class EntradaModificarComprobanteTesoreria{
		public Long 							p_IdEmpresa;
		public Long								p_IdUsuario;
		public String							p_IdModulo;
		public List<IngresoEgreso> 				p_IngresoEgreso;
		public List<IngresoEgresoFormaDePago> 	p_IngresoEgresoFormaDePago;
		public List<Comprobante> 				p_Comprobante;
		public List<ComprobanteDetalles> 		p_ComprobanteDetalle;
		public List<ComprobanteFormaDePago> 	p_ComprobanteFormaDePago;
		
	
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaModificarComprobanteTesoreria {
		public List<MensajeSalida>		P_OUT_MENSAJES;
		public Long						P_OUT_ERRORESCONTADOR;
		public Long						P_OUT_ADVERTENCIASCONTADOR;
		
		public SalidaModificarComprobanteTesoreria() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class IngresoEgreso extends BcsPojo{
		public Long 				IdIngresoEgreso;
		public String				Campo; 
		public String				Valor;  
	}
	
	public static class IngresoEgresoFormaDePago extends BcsPojo{
		public Long 				IdIngresoEgresoFormaDePago;
		public String				Campo; 
		public String				Valor;  
	}
	
	public static class Comprobante extends BcsPojo{
		public Long 				IdComprobante;
		public String				Campo; 
		public String				Valor;  
	}
	public static class ComprobanteDetalles extends BcsPojo{
		public Long 				IdComprobanteDetalle;
		public String				Campo; 
		public String				Valor;  
	}
	
	public static class ComprobanteFormaDePago extends BcsPojo{
		public Long 				IdComprobanteFormaDePago;
		public String				Campo; 
		public String				Valor;  
	}
	
	public static class ExisteTab extends BcsPojo{
		public Long count;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaModificarComprobanteTesoreria jin = gson.fromJson(in.toString(), EntradaModificarComprobanteTesoreria.class);
		
		SalidaModificarComprobanteTesoreria out = logica(conn,jin);

		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	@Trace
	public  SalidaModificarComprobanteTesoreria logica(Connection con, EntradaModificarComprobanteTesoreria entrada) throws Exception{
		logger.info("INICIO::ModificarComprobanteTesoreria");
		SalidaModificarComprobanteTesoreria out = new SalidaModificarComprobanteTesoreria();
		//String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		SQL _sql = new SQL();
		
		HashMap<String, Object> params = new HashMap<>(2);
		List<ExisteTab> TabINGRESOEGRESO =new ArrayList<ExisteTab>();
		TabINGRESOEGRESO = new SQL().query(con, SQL_EXIST_TABLA_INGRESOEGRESO, params, ExisteTab.class);
		if(TabINGRESOEGRESO.size()>0) {
			try {				
				SQL.exec(con, SQL_DROP_TABLA_INGRESOEGRESO);				
			} catch (Exception er) { 
				er.printStackTrace();
			}
			
		};
		
		List<ExisteTab> TabINGRESOEGRESOFORMADEPAGO =new ArrayList<ExisteTab>();
		TabINGRESOEGRESOFORMADEPAGO = new SQL().query(con, SQL_EXIST_TABLA_INGRESOEGRESOFORMADEPAGO, params, ExisteTab.class);
		if(TabINGRESOEGRESOFORMADEPAGO.size()>0) {
			try {				
				SQL.exec(con, SQL_DROP_TABLA_INGRESOEGRESOFORMADEPAGO);				
			} catch (Exception er) { 
				er.printStackTrace();
			}
			
		};
		
		List<ExisteTab> TabCOMPROBANTE =new ArrayList<ExisteTab>();
		TabCOMPROBANTE = new SQL().query(con, SQL_EXIST_TABLA_COMPROBANTE, params, ExisteTab.class);
		if(TabCOMPROBANTE.size()>0) {
			try {				
				SQL.exec(con, SQL_DROP_TABLA_COMPROBANTE);				
			} catch (Exception er) { 
				er.printStackTrace();
			}
			
		};

		List<ExisteTab> TabCOMPROBANTEDETALLE =new ArrayList<ExisteTab>();
		TabCOMPROBANTEDETALLE = new SQL().query(con, SQL_EXIST_TABLA_COMPROBANTEDETALLE, params, ExisteTab.class);
		if(TabCOMPROBANTEDETALLE.size()>0) {
			try {				
				SQL.exec(con, SQL_DROP_TABLA_COMPROBANTEDETALLE);				
			} catch (Exception er) { 
				er.printStackTrace();
			}
			
		};		
		
		List<ExisteTab> TabCOMPROBANTEFORMADEPAGO =new ArrayList<ExisteTab>();
		TabCOMPROBANTEFORMADEPAGO = new SQL().query(con, SQL_EXIST_TABLA_COMPROBANTEFORMADEPAGO, params, ExisteTab.class);
		if(TabCOMPROBANTEFORMADEPAGO.size()>0) {
			try {				
				SQL.exec(con, SQL_DROP_TABLA_COMPROBANTEFORMADEPAGO);				
			} catch (Exception er) { 
				er.printStackTrace();
			}
			
		};		
				
		if (entrada.p_IngresoEgreso.size() != 0) {
	 		 SQL.exec(con, SQL_CREATE_TABLA_INGRESOEGRESO);			
			_sql.batch(con,SQL_INSERT_TABLA_INGRESOEGRESO ,entrada.p_IngresoEgreso, IngresoEgreso.class);
			_sql.batch(con, SQL_UPDATE_INGRESOEGRESO, entrada.p_IngresoEgreso, IngresoEgreso.class);
		};	
		if (entrada.p_IngresoEgresoFormaDePago.size() != 0) {
	 		 SQL.exec(con, SQL_CREATE_TABLA_INGRESOEGRESOFORMADEPAGO);			
			_sql.batch(con,SQL_INSERT_TABLA_INGRESOEGRESOFORMADEPAGO ,entrada.p_IngresoEgresoFormaDePago, IngresoEgresoFormaDePago.class);
			_sql.batch(con, SQL_UPDATE_INGRESOEGRESOFORMADEPAGO, entrada.p_IngresoEgresoFormaDePago, IngresoEgresoFormaDePago.class);
		};			
		if (entrada.p_Comprobante.size() != 0) {
	 		 SQL.exec(con, SQL_CREATE_TABLA_COMPROBANTE);			
			_sql.batch(con,SQL_INSERT_TABLA_COMPROBANTE ,entrada.p_Comprobante, Comprobante.class);
			_sql.batch(con, SQL_UPDATE_COMPROBANTE, entrada.p_Comprobante, Comprobante.class);
		};			
		if (entrada.p_ComprobanteDetalle.size() != 0) {	
	 		 SQL.exec(con, SQL_CREATE_TABLA_COMPROBANTEDETALLE);			
			_sql.batch(con,SQL_INSERT_TABLA_COMPROBANTEDETALLE ,entrada.p_ComprobanteDetalle, ComprobanteDetalles.class);
			_sql.batch(con, SQL_UPDATE_COMPROBANTEDETALLE, entrada.p_ComprobanteDetalle, ComprobanteDetalles.class);
		};			
		if (entrada.p_ComprobanteFormaDePago.size() != 0) {
	 		 SQL.exec(con, SQL_CREATE_TABLA_COMPROBANTEFORMADEPAGO);			
			_sql.batch(con,SQL_INSERT_TABLA_COMPROBANTEFORMADEPAGO ,entrada.p_ComprobanteFormaDePago, ComprobanteFormaDePago.class);
			_sql.batch(con, SQL_UPDATE_COMPROBANTEFORMADEPAGO, entrada.p_ComprobanteFormaDePago, ComprobanteFormaDePago.class);
		};			
			
		
		logger.info("FIN::ModificarComprobanteTesoreria");
		return out;
	}
	

	
	

	
	
}
