package cl.bcs.contabilidad;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CreaBroadCuentas implements JavaCall
{

	private final String SQL_CONSULTA = loadResourceToString("cl/bcs/contabilidad/sql/ConsultaBroadCuenta.sql");
	
	public static class  BroadCuenta {
		
		public String CodEntidad;
		public String CodCtaContable;
		public String Descripcion;
		public String TipoCuenta;
		public String CuentaGrupo;
		public String CodMonedaContable;
		public String IndCtaConfidencial;
		public String IndCtaAsociada;
		public String IndCtaMonetaria;
		public String IndRelevanteFlujoCaja;
		public String IndCtaAfiliada;
		
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

	
	public void logica(Connection con, String fecha, String rutaDescarga, String nombreArchivo, String nombreArchivoCtr, cl.bcs.contabilidad.Log log) throws Exception {
		List<BroadCuenta> out = new ArrayList<BroadCuenta>();
		String linea = "";
		SQL _sql = new SQL();
		BroadCuenta bc = null;
		Long ms = null;
		String ruta = null;
		File fileInfo = null;
		//Generacion de Archivo Broad Contable
		
		
		
		HashMap<String, Object> parmsIN = new HashMap<String, Object>();
		out = _sql.query(con, SQL_CONSULTA, parmsIN , BroadCuenta.class);
		out.toString();
		
		FileWriter flwriter = null;
		try 
		{
			
			log.EscribirLog("INF", "Creando Archivo: "+ nombreArchivo);
			
			ruta = rutaDescarga ;
			fileInfo=new File(ruta + nombreArchivo);
			//crea el flujo para escribir en el archivo
			//flwriter = new FileWriter(ruta + nombreArchivo);
			//crea un buffer o flujo intermedio antes de escribir directamente en el archivo
			//BufferedWriter bfwriter = new BufferedWriter(flwriter);
			
			linea  = linea + "COD-ENTIDAD ";
			linea  = linea + "COD-CTA-CONTABLE ";
			linea  = linea + "DESCRIPCION ";
			linea  = linea + "TIPO-CUENTA ";
			linea  = linea + "CUENTA-GRUPO ";
			linea  = linea + "COD-CUENTA-CONTABLE ";
			linea  = linea + "IND_CTA_CONFIDENCIAL ";
			linea  = linea + "IND_CTA-ASOCIADA ";
			linea  = linea + "IND-CTA-MONETARIA ";
			linea  = linea + "IND_RELEVANTE_FLUJO-CAJA ";
			linea  = linea + "IND-CTA-AFILIADA";
			linea  = linea + "\n";
			
			ListIterator<BroadCuenta> iter = out.listIterator(out.size());
			while (iter.hasPrevious())
			{
				bc = new BroadCuenta();
				bc = iter.previous();
				
				linea  = linea + String.format("%1$-7s",bc.CodEntidad);
				linea  = linea + String.format("%1$-15s",bc.CodCtaContable);
				linea  = linea + String.format("%1$-100s",bc.Descripcion);
				linea  = linea + String.format("%1$-20s",bc.TipoCuenta);
				linea  = linea + String.format("%1$-10s",bc.CuentaGrupo);
				linea  = linea + String.format("%1$-4s",bc.CodMonedaContable);
				linea  = linea + String.format("%1$-1s",bc.IndCtaConfidencial);
				linea  = linea + String.format("%1$-1s",bc.IndCtaAsociada);
				linea  = linea + String.format("%1$-1s",bc.IndCtaMonetaria);
				linea  = linea + String.format("%1$-1s",bc.IndRelevanteFlujoCaja);
				linea  = linea + String.format("%1$-1s",bc.IndCtaAfiliada);
				linea  = linea + "\n";
				
			}
			//cierra el buffer intermedio
			//bfwriter.write(linea);
			//bfwriter.close();
			FileUtils.writeStringToFile(fileInfo,linea, "ISO-8859-1", true);
			linea = "";
			log.EscribirLog("INF", "Fin de creacion de Archivo: " + nombreArchivo);
			
			
			
			
 
		} catch (IOException e) 
		{
			log.EscribirLog("ERR", e.getMessage());
			e.printStackTrace();
		} finally {
			if (flwriter != null) {
				try {//cierra el flujo principal
					flwriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		
		//Generacion de archivo de control del Broad Contable
		try {
			log.EscribirLog("INF", "Creando Archivo: "+ nombreArchivoCtr);
			
			linea  = linea + "TIP-RUTINA ";
			linea  = linea + "DES-RUTINA ";
			linea  = linea + "DES-SUBRUTINA ";
			linea  = linea + "DES-ESTRUCUTRA ";
			linea  = linea + "MTO-SUMCTAREG ";
			linea  = linea + "FEC-PROCESO ";
			linea  = linea + "HRA-PROCESO ";
			linea  = linea + "\n";
			
			
			//crea el flujo para escribir en el archivo
			ruta = rutaDescarga + nombreArchivoCtr;
			fileInfo=new File(ruta);
			//flwriter = new FileWriter(ruta);
			//crea un buffer o flujo intermedio antes de escribir directamente en el archivo
			//BufferedWriter bfwriter = new BufferedWriter(flwriter);
			ArchivoControl ac = new ArchivoControl();
			ac.setTipRutina(String.format("%1$-1s", "C"));
			ac.setDesRutina(String.format("%1$-30s","SCRIPT"));
			ac.setDesSubRutina(String.format("%1$-30s",""));
			ac.setDesEstructura(String.format("%1$-30s",nombreArchivo));
			ac.setMtoSumctareg(String.format("%1$-15s",String.valueOf(out.size())));
			

			File file = new File(ruta + nombreArchivo);
			ms = file.lastModified();
			Date d = new Date(ms);
			Calendar c = Calendar.getInstance(); 
			c.setTime(d);
			//SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMdd");
			ac.setFecProceso(String.format("%1$-8s",c.get(Calendar.YEAR) + (c.get(Calendar.MONTH) +1) + c.get(Calendar.DAY_OF_MONTH)));
			ac.setHraProceso(String.format("%1$-10s",c.get(Calendar.HOUR) + ":" + c.get(Calendar.MINUTE) + ":" + c.get(Calendar.SECOND)));
			
			linea  = linea + ac.getTipRutina();
			linea  = linea + ac.getDesRutina();
			linea  = linea + ac.getDesSubRutina();
			linea  = linea + ac.getDesEstructura();
			linea  = linea + ac.getMtoSumctareg();
			linea  = linea + ac.getFecProceso();
			linea  = linea + ac.getHraProceso();
			
			//cierra el buffer intermedio
			//bfwriter.write(linea);
			//bfwriter.close();
			FileUtils.writeStringToFile(fileInfo,linea, "ISO-8859-1", true);
			linea = "";			
			log.EscribirLog("INF", "Fin de creacion de Archivo" + nombreArchivoCtr);
 
		} catch (IOException e) {
			log.EscribirLog("ERR", e.getMessage());
			e.printStackTrace();
		} finally {
			if (flwriter != null) {
				try {//cierra el flujo principal
					flwriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	public static void main(String[] args) throws Exception {
		
		BasicDataSource dsb1 = PlataformaUtils.getDESAB1Datasource();
		Connection con = dsb1.getConnection();
		CreaBroadCuentas demo = new CreaBroadCuentas();
		String fecha = "";
		String ruta = "C:\\archivos\\";	
		//demo.logica(con, fecha, ruta);
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
