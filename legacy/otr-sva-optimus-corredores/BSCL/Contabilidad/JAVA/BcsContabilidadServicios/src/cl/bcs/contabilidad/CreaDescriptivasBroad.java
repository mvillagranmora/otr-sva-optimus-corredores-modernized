package cl.bcs.contabilidad;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;


import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CreaDescriptivasBroad implements JavaCall 
{
	private final String SQL_ENTIDAD = loadResourceToString("cl/bcs/contabilidad/sql/ConsultaDesEntidad.sql");
	private final String SQL_TIPOCUENTA = loadResourceToString("cl/bcs/contabilidad/sql/ConsultaDesTipoCuenta.sql");
	private final String SQL_CENTROCOSTO = loadResourceToString("cl/bcs/contabilidad/sql/ConsultaDesCentroCosto.sql");
	
	public static class Entidad {
		public String getCodEntidad() {
			return CodEntidad;
		}
		public void setCodEntidad(String codEntidad) {
			CodEntidad = codEntidad;
		}
		public String getDescripcion() {
			return Descripcion;
		}
		public void setDescripcion(String descripcion) {
			Descripcion = descripcion;
		}
		public String CodEntidad;
		public String Descripcion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class TipoCuenta {
		public String getTipoCuenta() {
			return TipoCuenta;
		}
		public void setTipoCuenta(String tipoCuenta) {
			TipoCuenta = tipoCuenta;
		}
		public String getDescripcion() {
			return Descripcion;
		}
		public void setDescripcion(String descripcion) {
			Descripcion = descripcion;
		}
		public String TipoCuenta;
		public String Descripcion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CentroCosto {
		public String getCodccPro() {
			return CodccPro;
		}
		public void setCodccPro(String codccPro) {
			CodccPro = codccPro;
		}
		public String getDescripcion() {
			return Descripcion;
		}
		public void setDescripcion(String descripcion) {
			Descripcion = descripcion;
		}
		public String getTipo() {
			return Tipo;
		}
		public void setTipo(String tipo) {
			Tipo = tipo;
		}
		public String CodccPro;
		public String Descripcion;
		public String Tipo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		//ConsultarInformacionAsignacionPropCrono_Entrada in = gson.fromJson(jin.toString(), ConsultarInformacionAsignacionPropCrono_Entrada.class);
		//logica(con);
		//JsonParser parser = new JsonParser();
		//JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return null;
	}

	
	public void logicaEntidad(Connection con, String rutaDescarga, String nombreArchivo,  cl.bcs.contabilidad.Log log) throws Exception {
		
		Entidad ent = new Entidad();
		FileWriter flwriter = null;
		String linea = "";
		String ruta  = "";
		//BufferedWriter bfwriter = null;
		List<Entidad> out = new ArrayList<Entidad>();
		HashMap<String, Object> parmsIN = null;
		SQL _sql = new SQL();
		File fileInfo = null;
		
		try {
			
			log.EscribirLog("INF", "Creando Archivo: "+ nombreArchivo);
			parmsIN = new HashMap<String, Object>();
			out = _sql.query(con, SQL_ENTIDAD, parmsIN , Entidad.class);
			out.toString();
			//Genera Entidad
			ruta = rutaDescarga ;
			fileInfo=new File(ruta + nombreArchivo);
			//flwriter = new FileWriter(ruta);
			//bfwriter = new BufferedWriter(flwriter);
			
			linea = linea + "COD-ENTIDAD ";
			linea  = linea + "DESCRIPCION ";
			linea  = linea + "\n";
			
			ListIterator<Entidad> iter = out.listIterator(out.size());
			while (iter.hasPrevious())
			{
				ent = new Entidad();
				ent = iter.previous();
				
				linea  = linea + String.format("%1$-20s",ent.CodEntidad);
				linea  = linea + String.format("%1$-100s",ent.Descripcion);
				linea  = linea + "\n";
			}

			
			//bfwriter.write(linea);
			//bfwriter.close();
			FileUtils.writeStringToFile(fileInfo,linea, "ISO-8859-1", true);
			linea = "";
			log.EscribirLog("INF", "Fin de creacion de Archivo: " + nombreArchivo);				
			
		} catch (Exception e) 
		{
			log.EscribirLog("ERR", e.getMessage());
		}
		
		
	}
	
	public void logicaTipoCuenta(Connection con, String rutaDescarga, String nombreArchivo,  cl.bcs.contabilidad.Log log) throws Exception {
		

		TipoCuenta tp = new TipoCuenta();
		FileWriter flwriter = null;
		String linea = "";
		List<TipoCuenta> out = new ArrayList<TipoCuenta>();
		String ruta = "";
		SQL _sql = new SQL();
		//BufferedWriter bfwriter = null;
		HashMap<String, Object> parmsIN = null;
		File fileInfo = null;
		try {
			
			log.EscribirLog("INF", "Creando Archivo: "+ nombreArchivo);
			//Genera Tipo Cuenta
			parmsIN = new HashMap<String, Object>();
			out = _sql.query(con, SQL_TIPOCUENTA, parmsIN , TipoCuenta.class);
			out.toString();
			
			ruta = rutaDescarga;
			//flwriter = new FileWriter(ruta);
			//bfwriter = new BufferedWriter(flwriter);
			fileInfo=new File(ruta + nombreArchivo);
			linea  = linea + "TIPO-CUENTA ";
			linea  = linea + "DESCRIPCION ";
			linea  = linea + "\n";
			
			ListIterator<TipoCuenta> iter = out.listIterator(out.size());
			while (iter.hasPrevious())
			{
				tp = new TipoCuenta();
				tp = iter.previous();
				
				linea  = linea + String.format("%1$-20s",tp.TipoCuenta);
				linea  = linea + String.format("%1$-100s",tp.Descripcion);
				linea  = linea + "\n";
			}
			//bfwriter.write(linea);
			//bfwriter.close();
			FileUtils.writeStringToFile(fileInfo,linea, "ISO-8859-1", true);
			linea = "";
			log.EscribirLog("INF", "Fin de creacion de Archivo: " + nombreArchivo);
			
		} catch (Exception e) {
			log.EscribirLog("ERR", e.getMessage());
		}
		
		
	}
	
	public void logicaCentroCosto(Connection con, String rutaDescarga, String nombreArchivo,  cl.bcs.contabilidad.Log log) throws Exception {
		

		CentroCosto cc = new CentroCosto();
		FileWriter flwriter = null;
		String linea = "";
		String ruta = "";
		List<CentroCosto> out = new ArrayList<CentroCosto>();
		SQL _sql = new SQL();
		//BufferedWriter bfwriter = null;
		HashMap<String, Object> parmsIN = null;
		File fileInfo = null;
		try {
			log.EscribirLog("INF", "Creando Archivo: "+ nombreArchivo);
			
			//Genera Centro de Costo
			parmsIN = new HashMap<String, Object>();
			out = _sql.query(con, SQL_CENTROCOSTO, parmsIN , CentroCosto.class);
			out.toString();
			//flwriter = new FileWriter(ruta);
			//bfwriter = new BufferedWriter(flwriter);
			ruta = rutaDescarga;
			fileInfo=new File(ruta + nombreArchivo);
			linea  = linea + "COD-CC-PRD ";
			linea  = linea + "DESCRIPCION ";
			linea  = linea + "TIPO ";
			linea  = linea + "\n";
			
			ListIterator<CentroCosto> iter2 = out.listIterator(out.size());
			while (iter2.hasPrevious())
			{
				cc = new CentroCosto();
				cc = iter2.previous();
				
				linea  = linea + String.format("%1$-8s",cc.CodccPro);
				linea  = linea + String.format("%1$-100s",cc.Descripcion);
				linea  = linea + String.format("%1$-3s",cc.Tipo);
				linea  = linea + "\n";
			}
			//bfwriter.write(linea);
			//bfwriter.close();
			FileUtils.writeStringToFile(fileInfo,linea,true);
			linea = "";
			log.EscribirLog("INF", "Fin de creacion de Archivo: " + nombreArchivo);
			
		} catch (Exception e) {
			log.EscribirLog("ERR", e.getMessage());
		}
		
		
	}
	
	public static void main(String[] args) throws Exception {
		
		BasicDataSource dsb1 = PlataformaUtils.getDESAB1Datasource();
		Connection con = dsb1.getConnection();
		CreaDescriptivasBroad demo = new CreaDescriptivasBroad();
		//demo.logica(con);
		/*try  
		{
			
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdPortafolio\":343710,\"p_IdTipoOperacion\":\"COMACN\",\"p_IdMonedaLiquidacionOrden\":\"CLP\"}";
			ConsultarInformacionAsignacionPropCrono_Entrada adf = new  ConsultarInformacionAsignacionPropCrono_Entrada();
			adf.p_HoraHasta = "21:48:36";
			adf.p_IdEmpresa = 1L;
			CreaBroadCuentas demo = new CreaBroadCuentas();
			demo.logica(con);
 			//con.commit();
 			
	    	}
		*/ 	
		System.out.println("== END ==");
	}
}
