package cl.bcs.normativa.ils;



import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.commons.dbcp2.BasicDataSource;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ObtenerSaldoCuentaContable implements JavaCall {
	
	private final Log log = new Log("ILS.ObtenerSaldoCuentaContable");
	

	public static class SaldosCuentasContables{		
		public 	String CCL1;
		public 	String CCL2;
		public 	String CCL3;
		public 	String CCL4;
		public 	String CCL5;
		public  BigDecimal DEBIT = BigDecimal.ZERO;
		public  BigDecimal CREDIT = BigDecimal.ZERO;		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CuentaContable{
		public String IdCuenta;
		public String IdCuentaPadre;
		public int Nivel;
		public  BigDecimal Debe = BigDecimal.ZERO;
		public  BigDecimal Haber = BigDecimal.ZERO;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	

	

	
	
	public static class UpdateSaldosCuentasContables {
		public String IdCodigoContabilidad;
		public BigDecimal MontoDebe;
		public BigDecimal MontoHaber;
		public String IdCodigoContabilidadPadre;
		public int Nivel;
	}
	
	public static class CodigosCuentasContables {
		public String CuentaOptimus;
		public String CuentaContabilidad;
		public String CodigoContabilidad;
	}
	
	public static class ObtenerSaldoCuentaContableEntrada {
		public Long p_IdEmpresa;
		public String p_fechaConsulta;
	}
	
	public static class ObtenerSaldoCuentaContableSalida {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public ObtenerSaldoCuentaContableSalida() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES	 =  new ArrayList<MensajeSalida>();
		}
	}
	
	private final String SQL_UPSERT_CUENTAS_CONTABLES = loadResourceToString("cl/bcs/normativa/ils/sql/UpdateSaldosContables.sql");
	private final String SQL_SELECT_CUENTAS_CONTABLES_BASE = loadResourceToString("cl/bcs/normativa/ils/sql/SelectHomologaCuentasContables.sql");
	private final String SQL_DELETE_CUENTAS_CONTABLES_BASE = "DELETE FROM \"BSCL.Normativa.BD::ILS.p_SaldoCuentaContable\"";
		
// -----------------------------------------------------------------------
// CALL
// -----------------------------------------------------------------------

	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = new Gson();
		ObtenerSaldoCuentaContableEntrada in = gson.fromJson(jin.toString(), ObtenerSaldoCuentaContableEntrada.class);
		ObtenerSaldoCuentaContableSalida out = logica(con,in);
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	
// -----------------------------------------------------------------------
// LOGICA
// -----------------------------------------------------------------------

	public ObtenerSaldoCuentaContableSalida logica(Connection con, ObtenerSaldoCuentaContableEntrada entrada) throws Exception {
		ObtenerSaldoCuentaContableSalida out = new ObtenerSaldoCuentaContableSalida();
		
		//1. *** OBTIENE DE CONEXION A SAP B1 DESDE ARCHIVO PROPERTIES ***
		String rutaArchivosConfiguracion = Utils.getConfiguracion(con, Utils.getSchema(), entrada.p_IdEmpresa, "PLA.RutaArchivosConfiguracion");
		//String rutaArchivosConfiguracion = "C:\\_SVN\\BSCL\\Properties\\\\"; //para pruebas locales
		
		
		Properties prop = new Properties();
        try { 										 
			InputStream input = new FileInputStream(rutaArchivosConfiguracion + "interfazSaldosContables.properties");
        	//InputStream input = new FileInputStream("C:/BCS/PROYECTOS/OPTIMUS/SVN_DESA/BSCL/Properties/interfazSaldosContables.properties");
			prop.load(input);
		} catch (FileNotFoundException e1) {
			//out.P_OUT_MENSAJE = "Archivo de configuracion no encontrado";
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ARCHIVO_CONFIGURACION_NO_ENCONTRADO","Archivo de configuracion no encontrado"));
			e1.printStackTrace();
			return out;
		} catch (IOException e) {
			//out.P_OUT_MENSAJE = "Problemas al leer archivo de configuracion no encontrado";
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ARCHIVO_CONFIGURACION_PROBLEMA_LECTURA","Problemas al leer archivo de configuracion no encontrado"));
			e.printStackTrace();
			return out;
		}
        String dbURL  = (String) prop.get("DBURL");
        String dbprop = "currentSchema="+(String) prop.get("DBESQUEMA");
        String dbdrv  = (String) prop.get("DBDRIVER");
        String dbqry  = (String) prop.get("DBQUERY");
        String user   = (String) prop.get("DBUSER");
        String pass   = (String) prop.get("DBPASS");
        String bbdd   = (String) prop.get("DBDATABASE");

        
		//2. *** SE CONECTA A BD SAP B1 ***
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(dbdrv);
        ds.setUsername(user);
        ds.setPassword(pass);
        ds.setUrl(dbURL);
        ds.setValidationQuery(dbqry);
        ds.setConnectionProperties(dbprop);
        ds.setDefaultAutoCommit(false);
        
        try {
        	//3. *** OBTIENE DATOS DE LAS CUENTAS Y SALDOS ***
	        String fechaConsulta = entrada.p_fechaConsulta;	        
	        String fechaConsultaInicio = entrada.p_fechaConsulta.substring(0,4) + "-01-01";  // Fecha de inicio de anno respecto a la fecha consulta

	        SQL sql = new SQL();
	        String homologaCuenta = Utils.getConfiguracion(con,Utils.getSchema(),entrada.p_IdEmpresa, "ILS.HomologaCuentasContables");
	        	        
	    	Connection conn = ds.getConnection();
			HashMap<String,Object> hs = new HashMap<String,Object>();
	
			

			List<SaldosCuentasContables> lstSaldosCuentasContables = sql.query(conn, "CALL \"_SYS_BIC\".\"sap." + bbdd  + "/PROC_BALANCE_COA\" ('"+ fechaConsultaInicio +"','" + fechaConsulta + "')", hs, SaldosCuentasContables.class);
			List<CuentaContable> lstCuentasContables = new ArrayList<CuentaContable>();
			
			
			CuentaContable cuentaContable;
			Boolean newRegistro = true;
			
			//4. *** ARMA ESTRUCTURA DE NIVELES :: DEBE INSERTARSE DEDE EL NIVEL 5 EN FORMA DECRECIENTE PARA IR ACUMULANDO MONTOS DE NIVELES INFERIORES(PADRES) ***
			
			// SALDOS CUENTA NIVEL 5
			for(SaldosCuentasContables saldoCuenta : lstSaldosCuentasContables) 
			{					
				//if (!saldoCuenta.CCL1.equalsIgnoreCase("300000000000000") )  continue;
				
				cuentaContable  = new CuentaContable();
				cuentaContable.IdCuenta 		= saldoCuenta.CCL5;
				cuentaContable.IdCuentaPadre 	= saldoCuenta.CCL4;
				cuentaContable.Nivel 			= 5;
				cuentaContable.Debe 			= saldoCuenta.DEBIT;
				cuentaContable.Haber 			= saldoCuenta.CREDIT;
				
				lstCuentasContables.add(cuentaContable);				
			}
			
			
			// SALDOS CUENTA NIVEL 4
			for(SaldosCuentasContables saldoCuenta : lstSaldosCuentasContables) 
			{	
				newRegistro = true;				
				cuentaContable  = new CuentaContable();       
				cuentaContable.IdCuenta 		= saldoCuenta.CCL4;
				cuentaContable.IdCuentaPadre 	= saldoCuenta.CCL3;
				cuentaContable.Nivel 			= 4;
				cuentaContable.Debe 			= BigDecimal.ZERO;
				cuentaContable.Haber 			= BigDecimal.ZERO;
				
				for (CuentaContable ctaCbl : lstCuentasContables)
				{
					if (cuentaContable.IdCuenta.equalsIgnoreCase(ctaCbl.IdCuenta) && cuentaContable.IdCuentaPadre.equalsIgnoreCase(ctaCbl.IdCuentaPadre))					
						newRegistro = false;
					
					if (ctaCbl.Nivel == 5 && ctaCbl.IdCuentaPadre.equalsIgnoreCase(cuentaContable.IdCuenta))
					{	//Acumula los montos del nivel hijo
						cuentaContable.Debe 			= cuentaContable.Debe.add(ctaCbl.Debe);
						cuentaContable.Haber 			= cuentaContable.Haber.add(ctaCbl.Haber);
					}
				}		
				
				if (newRegistro == true)
					lstCuentasContables.add(cuentaContable);				
			}
			
			
			
			// SALDOS CUENTA NIVEL 3
			for(SaldosCuentasContables saldoCuenta : lstSaldosCuentasContables) 
			{					
				newRegistro = true;
				cuentaContable  = new CuentaContable();
       
				cuentaContable.IdCuenta 		= saldoCuenta.CCL3;
				cuentaContable.IdCuentaPadre 	= saldoCuenta.CCL2;
				cuentaContable.Nivel 			= 3;
				cuentaContable.Debe 			= BigDecimal.ZERO;
				cuentaContable.Haber 			= BigDecimal.ZERO;
				
				for (CuentaContable ctaCbl : lstCuentasContables)
				{
					if (cuentaContable.IdCuenta.equalsIgnoreCase(ctaCbl.IdCuenta) && cuentaContable.IdCuentaPadre.equalsIgnoreCase(ctaCbl.IdCuentaPadre))					
						newRegistro = false;					
						
					if (ctaCbl.Nivel == 4 && ctaCbl.IdCuentaPadre.equalsIgnoreCase(cuentaContable.IdCuenta)) 
					{   //Acumula los montos del nivel hijo
						cuentaContable.Debe 			= cuentaContable.Debe.add(ctaCbl.Debe);
						cuentaContable.Haber 			= cuentaContable.Haber.add(ctaCbl.Haber);
					}
				}		
	
				if (newRegistro == true)
					lstCuentasContables.add(cuentaContable);				
			}
			
			
			// SALDOS CUENTA NIVEL 2
			for(SaldosCuentasContables saldoCuenta : lstSaldosCuentasContables) 
			{	
				newRegistro = true;				
				cuentaContable  = new CuentaContable();
       
				cuentaContable.IdCuenta 		= saldoCuenta.CCL2;
				cuentaContable.IdCuentaPadre 	= saldoCuenta.CCL1;
				cuentaContable.Nivel 			= 2;
				cuentaContable.Debe 			= BigDecimal.ZERO;
				cuentaContable.Haber 			= BigDecimal.ZERO;
				
				for (CuentaContable ctaCbl : lstCuentasContables)
				{
					
					if (cuentaContable.IdCuenta.equalsIgnoreCase(ctaCbl.IdCuenta) && cuentaContable.IdCuentaPadre.equalsIgnoreCase(ctaCbl.IdCuentaPadre))					
						newRegistro = false;
					
					if (ctaCbl.Nivel == 3 && ctaCbl.IdCuentaPadre.equalsIgnoreCase(cuentaContable.IdCuenta)) 
					{	//Acumula los montos del nivel hijo					
						cuentaContable.Debe 			= cuentaContable.Debe.add(ctaCbl.Debe);
						cuentaContable.Haber 			= cuentaContable.Haber.add(ctaCbl.Haber);
					}
				}				
	
				if (newRegistro == true)
					lstCuentasContables.add(cuentaContable);				
			}
			
			
			// SALDOS CUENTA NIVEL 1
			for(SaldosCuentasContables saldoCuenta : lstSaldosCuentasContables) 
			{	
				newRegistro = true;				
				cuentaContable  = new CuentaContable();
       
				cuentaContable.IdCuenta 		= saldoCuenta.CCL1;
				cuentaContable.IdCuentaPadre 	= null;
				cuentaContable.Nivel 			= 1;
				cuentaContable.Debe 			= BigDecimal.ZERO;
				cuentaContable.Haber 			= BigDecimal.ZERO;
				
				for (CuentaContable ctaCbl : lstCuentasContables)
				{
					
					if (ctaCbl.Nivel != 1 && cuentaContable.IdCuenta.equalsIgnoreCase(ctaCbl.IdCuenta) && cuentaContable.IdCuentaPadre.equalsIgnoreCase(ctaCbl.IdCuentaPadre))					
						newRegistro = false;
					
					if (ctaCbl.Nivel == 2 && ctaCbl.IdCuentaPadre.equalsIgnoreCase(cuentaContable.IdCuenta)) 
					{   //Acumula los montos del nivel hijo
						cuentaContable.Debe 			= cuentaContable.Debe.add(ctaCbl.Debe);
						cuentaContable.Haber 			= cuentaContable.Haber.add(ctaCbl.Haber);
					}
				}				
	
				if (newRegistro == true)
					lstCuentasContables.add(cuentaContable);				
			}
			

			
			conn.close();
	        


			
			List<UpdateSaldosCuentasContables> listadoActualizar = new ArrayList<>();
			UpdateSaldosCuentasContables actualizar;
			

			if (homologaCuenta.equalsIgnoreCase("S")){
				List<CodigosCuentasContables> lstCodigosCuentasContables = sql.query(con, SQL_SELECT_CUENTAS_CONTABLES_BASE, new HashMap<String,Object>(), CodigosCuentasContables.class);
				for(CuentaContable ctaCon : lstCuentasContables){ 
					for (CodigosCuentasContables codigo : lstCodigosCuentasContables){
						if (codigo.CodigoContabilidad.equalsIgnoreCase(ctaCon.IdCuenta)){
							actualizar = new UpdateSaldosCuentasContables();
							actualizar.IdCodigoContabilidad 		= codigo.CuentaContabilidad;
							actualizar.IdCodigoContabilidadPadre 	= ctaCon.IdCuentaPadre;
							actualizar.MontoDebe 					= ctaCon.Debe.setScale(2, RoundingMode.HALF_UP);
							actualizar.MontoHaber 					= ctaCon.Haber.setScale(2, RoundingMode.HALF_UP);
							actualizar.Nivel 						= ctaCon.Nivel;
							listadoActualizar.add(actualizar);							
							
							break;
						}
					}
				}
			}else{
				for(CuentaContable ctaCon : lstCuentasContables)
				{
					actualizar = new UpdateSaldosCuentasContables();
					actualizar.IdCodigoContabilidad 		= ctaCon.IdCuenta;
					actualizar.IdCodigoContabilidadPadre 	= ctaCon.IdCuentaPadre;
					actualizar.MontoDebe 					= ctaCon.Debe.setScale(2, RoundingMode.HALF_UP);
					actualizar.MontoHaber 					= ctaCon.Haber.setScale(2, RoundingMode.HALF_UP);
					actualizar.Nivel 						= ctaCon.Nivel;
					listadoActualizar.add(actualizar);					
				}
			}
			


				
			System.out.println("Termina todo...");
			sql.exec(con, SQL_DELETE_CUENTAS_CONTABLES_BASE);
				
			sql.batch(con, SQL_UPSERT_CUENTAS_CONTABLES, listadoActualizar, UpdateSaldosCuentasContables.class);
	        
        } catch (SQLException e) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ARCHIVO_CONFIGURACION_PROBLEMA_LECTURA","Problemas al leer archivo de configuracion no encontrado"));
			//out.P_OUT_MENSAJE = "Problemas al leer archivo de configuracion no encontrado";
			e.printStackTrace();
		}  
		return out;	 
	}
	
	
	// -----------------------------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	// -----------------------------------------------------------------------
	public static void main (String [] args) {
  		try {
  			//--Connection con = PlataformaUtils.getMBIPREConnection();
  			//Connection con = PlataformaUtils.getPro1BCOESTADOConnection();
  			Connection con = PlataformaUtils.getMASTER01Connection();
  			try{
  				ObtenerSaldoCuentaContableEntrada entrada = new ObtenerSaldoCuentaContableEntrada();
  				entrada.p_IdEmpresa		= new Long(1);
  				entrada.p_fechaConsulta	= "2025-05-29T04:00:00.000Z";
  				JsonParser parser 		= new JsonParser();
  				JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
  				ObtenerSaldoCuentaContable demo = new ObtenerSaldoCuentaContable();
  				System.out.println(demo.call(con, dataJsonIn).toString());
  				//con.commit();
  				
  				con.rollback();
  				
  			}catch(Exception e){
  				con.rollback();
  				e.printStackTrace();
  			}finally{
  				con.close();
  			}
  		}catch (Exception e) {
  			e.printStackTrace();
  		}		
        System.out.println("== END ==");
	}
}
