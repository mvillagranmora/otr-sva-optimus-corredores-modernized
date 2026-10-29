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


public class CreaMovimientoBPC implements JavaCall 
{
	private final String SQL_CONSULTA = loadResourceToString("cl/bcs/contabilidad/sql/ConsultaMovimientoBPC.sql");
	
	public static class MovimientoBPC {
		public String CodigoSociedad;
		public String CuentaMayor;
		public String VarianteDeEjercicio;
		public String EjercicioPeriodo;
		public String NroDocumentoContable;
		public String NroApunteContable;
		public String Segmento;
		public String Ledger;
		public String SocGlAsociada;
		public String Moneda;
		public BigDecimal TotalContabDebe;
		public BigDecimal TotalContaHaber;
		public String ClaseDeRegistro;
		public String IndicadorAnulacion;
		
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
		List<MovimientoBPC> out = new ArrayList<MovimientoBPC>();
		String linea = "";
		SQL _sql = new SQL();
		Date _dateDesde = new Date();
		Date _dateHasta = new Date();
		MovimientoBPC bc = null;
		Long ms = null;
		String ruta = "";
		//Generacion de Archivo Broad Contable
		BigDecimal totaldebe = new BigDecimal(0);
		BigDecimal totalhaber = new BigDecimal(0);
		File fileInfo = null;
		
		HashMap<String, Object> parmsIN = new HashMap<String, Object>();
		parmsIN.put("FechaEjecucion", fecha);
		out = _sql.query(con, SQL_CONSULTA, parmsIN , MovimientoBPC.class);
		out.toString();
		
		FileWriter flwriter = null;
		try {
			
			log.EscribirLog("INF", "Creando Archivo: "+ nombreArchivo);
			
			//crea el flujo para escribir en el archivo
			//flwriter = new FileWriter(ruta + nombreArchivo);
			//crea un buffer o flujo intermedio antes de escribir directamente en el archivo
			//BufferedWriter bfwriter = new BufferedWriter(flwriter);
			ruta = rutaDescarga;
			fileInfo=new File(ruta + nombreArchivo);
			
			linea  = linea + "SOCIEDAD ";
			linea  = linea + "CUENTA-DE-MAYOR ";
			linea  = linea + "VARIANTE-DE-EJERCICIO ";
			linea  = linea + "EJERCICIO-PERIODO ";
			linea  = linea + "NRO-DOCUMENTO-CONTABLE ";
			linea  = linea + "NRO-APUNTE-CONTABLE ";
			linea  = linea + "SEGMENTO ";
			linea  = linea + "LEDGER ";
			linea  = linea + "SOC-GL-ASOCIADA ";
			linea  = linea + "TOTAL-CONTAB-DEBE ";
			linea  = linea + "TOTAL-CONTB-HABER ";
			linea  = linea + "CUENTA-OPERATIVA ";
			linea  = linea + "CLASE-DE-REGISTRO ";
			linea  = linea + "TIPO-CUENTA ";
			linea  = linea + "INDICADOR-ANULACION ";
			linea  = linea + "\n";
			
			ListIterator<MovimientoBPC> iter = out.listIterator(out.size());
			while (iter.hasPrevious())
			{
				bc = new MovimientoBPC();
				bc = iter.previous();
				
				linea  = linea + String.format("%1$-4s",bc.CodigoSociedad);
				linea  = linea + String.format("%1$-10s",bc.CuentaMayor);
				linea  = linea + String.format("%1$-2s",bc.VarianteDeEjercicio);
				linea  = linea + String.format("%1$-7s",bc.EjercicioPeriodo);
				linea  = linea + String.format("%1$-10s",bc.NroDocumentoContable);
				linea  = linea + String.format("%1$-3s",bc.NroApunteContable);
				linea  = linea + String.format("%1$-2s",bc.Segmento);
				linea  = linea + String.format("%1$-2s",bc.Ledger);
				linea  = linea + String.format("%1$-4s",bc.SocGlAsociada);
				linea  = linea + String.format("%1$-5s",bc.Moneda);
				linea  = linea + String.format("%1$-20s",bc.TotalContabDebe);
				linea  = linea + String.format("%1$-20s",bc.TotalContaHaber);
				linea  = linea + String.format("%1$-1s",bc.ClaseDeRegistro);
				linea  = linea + String.format("%1$-1s",bc.IndicadorAnulacion);

				totaldebe = totaldebe.add(bc.TotalContabDebe);
				totalhaber = totalhaber.add(bc.TotalContaHaber);
				
				linea  = linea + "\n";
				
			}
			//cierra el buffer intermedio
			//bfwriter.write(linea);
			//bfwriter.close();
			FileUtils.writeStringToFile(fileInfo,linea, "ISO-8859-1", true);
			linea = "";			
			log.EscribirLog("INF", "Fin de creacion de Archivo: " + nombreArchivo);
			
			
			
 
		} catch (IOException e) {
			e.printStackTrace();
			log.EscribirLog("ERR", e.getMessage());
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
			linea  = linea + "DES-CAMPOVALIDAR-1 ";
			linea  = linea + "POS-INICIAL-1 ";
			linea  = linea + "POS-FINAL-1 ";
			linea  = linea + "MTO-SUMCAMPO-1 ";
			linea  = linea + "DES-CAMPOVALIDAR-2 ";
			linea  = linea + "POS-INICIAL-2 ";
			linea  = linea + "POS-FINAL-2 ";
			linea  = linea + "MTO-SUMCAMPO-2 ";
			linea  = linea + "MTO-SUMCTAREG ";
			linea  = linea + "FEC-PROCESO ";
			linea  = linea + "HRA-PROCESO ";
			linea  = linea + "\n";
			
			
			//crea el flujo para escribir en el archivo
			//flwriter = new FileWriter(ruta + nombreArchivoCtr);
			fileInfo=new File(ruta + nombreArchivoCtr);
			//crea un buffer o flujo intermedio antes de escribir directamente en el archivo
			//BufferedWriter bfwriter = new BufferedWriter(flwriter);
			ArchivoControl ac = new ArchivoControl();
			ac.setTipRutina(String.format("%1$-1s", "C"));
			ac.setDesRutina(String.format("%1$-30s","SCRIPT"));
			ac.setDesSubRutina(String.format("%1$-30s",""));
			ac.setDesEstructura(String.format("%1$-30s",nombreArchivo));
			ac.setDesCampoValidar(String.format("%1$-20s", "TOTAL-CONTAB-DEBE"));
			ac.setDesCampoValidar2(String.format("%1$-20s", "TOTAL-CONTAB-HABER"));
			ac.setPosInicial(String.format("%1$-3s",""));
			ac.setPosInicial2(String.format("%1$-3s",""));
			ac.setPosFinal(String.format("%1$-3s",""));
			ac.setPosFinal2(String.format("%1$-3s",""));
			ac.setMtoSumcampo(String.format("%1$-20s",totaldebe.toString()));
			ac.setMtoSumcampo2(String.format("%1$-20s",totalhaber.toString()));
			ac.setMtoSumctareg(String.format("%1$-15s",String.valueOf(out.size())));
			
			File file = new File(ruta + nombreArchivo);
			ms = file.lastModified();
			Date d = new Date(ms);
			Calendar c = Calendar.getInstance();
			
			c.setTime(d);
			ac.setFecProceso(String.format("%1$-8s",c.get(Calendar.YEAR) + (c.get(Calendar.MONTH) +1) + c.get(Calendar.DAY_OF_MONTH)));
			ac.setHraProceso(String.format("%1$-10s",c.get(Calendar.HOUR) + ":" + c.get(Calendar.MINUTE) + ":" + c.get(Calendar.SECOND)));
			
			linea  = linea + ac.getTipRutina();
			linea  = linea + ac.getDesRutina();
			linea  = linea + ac.getDesSubRutina();
			linea  = linea + ac.getDesEstructura();
			linea  = linea + ac.getDesCampoValidar();
			linea  = linea + ac.getPosInicial();
			linea  = linea + ac.getPosFinal();
			linea  = linea + ac.getMtoSumcampo();
			linea  = linea + ac.getDesCampoValidar2();
			linea  = linea + ac.getPosInicial2();
			linea  = linea + ac.getPosFinal2();
			linea  = linea + ac.getMtoSumcampo2();
			linea  = linea + ac.getMtoSumctareg();
			linea  = linea + ac.getFecProceso();
			linea  = linea + ac.getHraProceso();
			
			//cierra el buffer intermedio
			//bfwriter.write(linea);
			//bfwriter.close();
			FileUtils.writeStringToFile(fileInfo,linea, "ISO-8859-1", true);
			linea = "";
			log.EscribirLog("INF", "Fin de creacion de Archivo: " + nombreArchivoCtr);
 
		} catch (IOException e) {
			e.printStackTrace();		
			log.EscribirLog("ERR", e.getMessage());
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
		CreaMovimientoBPC demo = new CreaMovimientoBPC();
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
