package cl.bcs.normativa.ils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.GenerarDataReporteTRXI;
import cl.bcs.normativa.ils.pojo.HomologaCuenta;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.plataforma.sap.pojo.OACT;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import java.util.Properties;

public class HomologarCuentasContables implements JavaCall {
	
	public static class HomologarCuentasContables_Entrada {
		public Long p_IdEmpresa;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class HomologarCuentasContables_Salida {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public HomologarCuentasContables_Salida() {
			P_OUT_MENSAJES = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	private final String SQL_UPDATE_ITEM_CONFIGURACION = loadResourceToString("cl/bcs/normativa/ils/sql/HomologarCuentasContables_UpdateItemConfiguracion.sql");
	private final String SQL_UPSERT_CUENTAS_CONTABLES = loadResourceToString("cl/bcs/normativa/ils/sql/HomologarCuentasContables_UpsertCuentasContables.sql");
	private final String SQL_SELECT_CUENTAS_CONTABLES = loadResourceToString("cl/bcs/normativa/ils/sql/HomologarCuentasContables_SelectCuentasContables.sql");
	private final String SQL_SELECT_CUENTAS_CONTABLES_ACTUALES = loadResourceToString("cl/bcs/normativa/ils/sql/HomologarCuentasContables_SelectCuentasContablesActuales.sql");
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = new Gson();		
		HomologarCuentasContables_Entrada in=gson.fromJson(jin.toString() ,HomologarCuentasContables_Entrada.class);
		HomologarCuentasContables_Salida out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	/**********************
	 * lógica del programa
	 **********************/
	public HomologarCuentasContables_Salida logica(Connection con, HomologarCuentasContables_Entrada entrada) throws Exception {
		HomologarCuentasContables_Salida out = new HomologarCuentasContables_Salida();
		String rutaArchivosConfiguracion = Utils.getConfiguracion(con, Utils.getSchema(), entrada.p_IdEmpresa, "PLA.RutaArchivosConfiguracion");
		Properties prop = new Properties();
		out.P_OUT_ERRORESCONTADOR = 0L;
        try { 										 
			InputStream input = new FileInputStream(rutaArchivosConfiguracion + "interfazSAP.properties");
			prop.load(input);
		} catch (FileNotFoundException e1) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ARCHIVO_CONFIGURACION_NO_ENCONTRADO","Archivo de configuraci�n no encontrado"));
			e1.printStackTrace();
			return out;
		} catch (IOException e) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ARCHIVO_CONFIGURACION_PROBLEMA_LECTURA","Problemas al leer archivo de configuraci�n no encontrado"));
			e.printStackTrace();
			return out;
		}
        String dbIp  = (String) prop.get("HANA_SERVER");
        String dbPort  = (String) prop.get("HANA_PORT");
        String dbCompany = (String) prop.get("B1_COMPANY");
        String user   = (String) prop.get("HANA_USER");
        String pass   = (String) prop.get("HANA_PASS");

        Connection sapConn = Utils.getSAPConnection(dbIp,dbPort,dbCompany,user,pass);
        SQL _sql = new SQL();
        List<OACT> cuentas = _sql.query(sapConn, SQL_SELECT_CUENTAS_CONTABLES.replace(":EMPRESA", dbCompany), new HashMap<>(), OACT.class);
        List<HomologaCuenta> cuentasOptimus = _sql.query(con, SQL_SELECT_CUENTAS_CONTABLES_ACTUALES,new HashMap<>(),HomologaCuenta.class);
		//System.out.println(cuentas);
		//System.out.println(cuentasOptimus);
        
        //UPSERT CUENTAS
        List<HomologaCuenta> upsertList = new ArrayList<>();
        List<HomologaCuenta> updateList = new ArrayList<>();
		for (OACT oact : cuentas) {
			HomologaCuenta cuentaOptimus = new HomologaCuenta();
			cuentaOptimus.CodigoContabilidad = oact.AcctCode;
			if (oact.Segment_0 == null || oact.Segment_0.trim().length() == 0) {
				cuentaOptimus.CuentaOptimus = oact.AcctCode;
				cuentaOptimus.CuentaContabilidad = oact.AcctCode;
			}
			else {
				cuentaOptimus.CuentaOptimus = oact.Segment_0;
				cuentaOptimus.CuentaContabilidad = oact.Segment_0;
			}
			cuentaOptimus.DetalleCuenta = oact.AcctName;
			if (cuentaOptimus.DetalleCuenta != null && cuentaOptimus.DetalleCuenta.length() > 70) {
				cuentaOptimus.DetalleCuenta  = cuentaOptimus.DetalleCuenta.substring(0,70);
			}
			if (cuentaOptimus.DetalleCuenta == null) {
				cuentaOptimus.DetalleCuenta = "CUENTA SIN DETALLE";
			}
			cuentaOptimus.NombreCuenta = oact.AcctName;
			if (cuentaOptimus.NombreCuenta != null && cuentaOptimus.NombreCuenta.length() > 70) {
				cuentaOptimus.NombreCuenta  = cuentaOptimus.NombreCuenta.substring(0,70);
			}
			if (cuentaOptimus.NombreCuenta == null) {
				cuentaOptimus.NombreCuenta = "CUENTA SIN NOMBRE";
			}
			cuentaOptimus.CuentaProveedor = "";
			cuentaOptimus.CuentaEEFF = "";
			cuentaOptimus.CentroCosto = "";
			
			upsertList.add(cuentaOptimus);
			
		}
		
		for (HomologaCuenta coptNueva : upsertList) {
			boolean found = false;
			for (HomologaCuenta copt : cuentasOptimus) {
				if (coptNueva.CuentaOptimus.equalsIgnoreCase(copt.CuentaOptimus)) {
					found = true;
					break;
				}
			}
			if (!found) {
				updateList.add(coptNueva);
			}
		}
		
		System.out.println(updateList);
		
		_sql.batch(con, SQL_UPSERT_CUENTAS_CONTABLES, updateList, HomologaCuenta.class);
		//_sql.batch(con, SQL_UPDATE_ITEM_CONFIGURACION, updateList, HomologaCuenta.class);
		
		out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"CUENTAS_CONTABLES_ACTUALIZADAS","Cuentas contables actualizadas"));
			
		return out;
		 
	}
	
	public static void main (String [] args) {
		
        //prueba de la clase
  		try {
  			Connection con = PlataformaUtils.getDESAConnection();
  			try{
  				
  				HomologarCuentasContables_Entrada entrada = new HomologarCuentasContables_Entrada();
  				entrada.p_IdEmpresa		= new Long(1);
  				HomologarCuentasContables demo = new HomologarCuentasContables();
  				System.out.println(demo.logica(con, entrada));
  				con.commit();
  				
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
