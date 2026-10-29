package cl.bcs.contabilidad;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.contabilidad.pojo.HomologaCuenta;
import cl.bcs.plataforma.pla.InsertarLogServicio;
import cl.bcs.plataforma.pla.InsertarLogServicio.RegistroLog;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.plataforma.sap.pojo.OACT;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class HomologarCuentasContables implements JavaCall {
	
	private final Log log = new Log("CON.HomologarCuentasContables");
	static final String CUENTA_ACTIVO = "A";
	static final String CUENTA_PASIVO = "P";
	
	public static class HomologarCuentasContables_Entrada {
		public Long p_IdEmpresa;
		public String p_IdModulo;
		public Long p_IdUsuario;
		public String p_FechaContable;
		
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
	
	private final String SQL_SELECT_CUENTAS_CONTABLES = loadResourceToString("cl/bcs/contabilidad/sql/HomologarCuentasContables_SelectCuentasContables.sql");
	private final String SQL_DELETE_CUENTAS_CONTABLES = loadResourceToString("cl/bcs/contabilidad/sql/HomologarCuentasContables_DeleteCuentasContables.sql");
	private final String SQL_INSERT_CUENTAS_CONTABLES = loadResourceToString("cl/bcs/contabilidad/sql/HomologarCuentasContables_InsertCuentasContables.sql");
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = new Gson();		
		HomologarCuentasContables_Entrada in=gson.fromJson(jin.toString() ,HomologarCuentasContables_Entrada.class);
		HomologarCuentasContables_Salida out=logica(con,in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	/**********************
	 * logica del programa
	 **********************/
	public HomologarCuentasContables_Salida logica(Connection con, HomologarCuentasContables_Entrada entrada) throws Exception {
		log.write("INICIO::HomologarCuentasContables");
		HomologarCuentasContables_Salida out = new HomologarCuentasContables_Salida();
		Properties prop = new Properties();
		out.P_OUT_ERRORESCONTADOR = 0L;
        try {
        	String rutaArchivosConfiguracion = Utils.getConfiguracion(con, Utils.getSchema(), entrada.p_IdEmpresa, "PLA.RutaArchivosConfiguracion");
        	InputStream input = new FileInputStream(rutaArchivosConfiguracion + "jobm/interfazSAP.properties");
			prop.load(input);
		} catch (FileNotFoundException e1) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ARCHIVO_CONFIGURACION_NO_ENCONTRADO","Archivo de configuracion no encontrado"));
			e1.printStackTrace();
			return out;
		} catch (IOException e) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ARCHIVO_CONFIGURACION_PROBLEMA_LECTURA","Problemas al leer archivo de configuracion no encontrado"));
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
        
        //UPSERT CUENTAS
        List<HomologaCuenta> upsertList = new ArrayList<>();
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
/*
	Cuentas de Activo (*)
	Cuentas de Pasivo (-)	 
		1 ACTivo *
		2 pasivo -
		3 patrimonio -
		4 Ingresos operacionales -
		5 costos OPERACIONALES *
		6 gastos OPERACIONALES *
		7 otros ingresos  -
		8 otros egresos * 
*/
			switch (oact.GroupMask) {
			case 1:
			case 5:
			case 6:
			case 8:
				cuentaOptimus.TipoCuenta = CUENTA_ACTIVO;
				break;
			case 2:
			case 3:
			case 4:
			case 7:
				cuentaOptimus.TipoCuenta = CUENTA_PASIVO;
				break;
			default:
				cuentaOptimus.TipoCuenta = "";
				break;
			}
			
			for(int i = 0;i < cuentas.size(); i++){
				if(oact.AcctCode.equals(cuentas.get(i).FatherNum)){
					cuentaOptimus.Imputable = "N";	
					break;
				}
				else{
					cuentaOptimus.Imputable = "S";
				}
			}
			
			upsertList.add(cuentaOptimus);
			
		}
		
		InsertarLogServicio insertarLogService = new InsertarLogServicio();
		List<RegistroLog> lstDatosLog = new ArrayList<RegistroLog>();
		
		String Glosa = "Homologación de cuentas contables actualizadas.";
		String Valor1 = entrada.p_FechaContable;
		String Valor2 = "HomologarCuentasContables";
		
		if(out.P_OUT_ERRORESCONTADOR.equals(0L)) {
			lstDatosLog.add(insertarLogService.armaRegistroLog("NORMATIVA",entrada.p_IdEmpresa,"INFORMACION",Glosa,Valor1,Valor2));
		}
				
		if(lstDatosLog.size() >= 1) {
			insertarLogService.logica(con,lstDatosLog);
		}else {
			String GlosaError = "Error actualizando la homologación las cuentas contables.";
			String Valor1Error = entrada.p_FechaContable;
			String Valor2Error = "HomologarCuentasContables";
			lstDatosLog.add(insertarLogService.armaRegistroLog("NORMATIVA",entrada.p_IdEmpresa,"ERROR",GlosaError,Valor1Error,Valor2Error));
			insertarLogService.logica(con,lstDatosLog);
		}	
		
		_sql.batch(con, SQL_DELETE_CUENTAS_CONTABLES, upsertList, HomologaCuenta.class);	
		
		_sql.batch(con, SQL_INSERT_CUENTAS_CONTABLES, upsertList, HomologaCuenta.class);
		
		out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,"CUENTAS_CONTABLES_ACTUALIZADAS","Cuentas contables actualizadas"));
		
		log.write("FIN::HomologarCuentasContables");
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
  				//con.commit();
  				
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
