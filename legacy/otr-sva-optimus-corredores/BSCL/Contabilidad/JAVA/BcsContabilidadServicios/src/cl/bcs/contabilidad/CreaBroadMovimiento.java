package cl.bcs.contabilidad;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
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

public class CreaBroadMovimiento implements JavaCall 
{
	private final String SQL_CONSULTA = loadResourceToString("cl/bcs/contabilidad/sql/ConsultaBroadMovimiento.sql");
	
	public static class BroadMovimiento {
		
		public String CodInstitucion;
		public String CodAplicativo;
		public String CodEntidad;
		public Timestamp FecDia;
		public Short HraTransaccion;
		public String NumOperacion;
		public String NumPosOperacion;
		public String NumRegistro;
		public String CodCtaContable;
		public String CodTipoCuenta;
		public String CodProductoContable;
		public String CodCentroCosto;
		public Timestamp FecDocumento;
		public String NroSerieDoc;
		public String NroDoc;
		public String CodAfiliada;
		public String TipCargoAbono;
		public String CodMonedaContable;
		public BigDecimal MontoTransaccionContable;
		public String IndAnulado;
		public String IndOpeAfiliada;
		public String Filler01;
		public String Filler02;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {

		return null;
	}

	
	public void logica(Connection con, String fecha, String rutaDescarga, String nombreArchivo, String nombreArchivoCtr, Long contador, cl.bcs.contabilidad.Log log) throws Exception {
		List<BroadMovimiento> out = new ArrayList<BroadMovimiento>();
		String linea = "";
		String ruta = null;
		SQL _sql = new SQL();
		String dateFormat  = "";
		BroadMovimiento bc = null;
		Long ms = null;
		BigDecimal totalMtoSumCampo = new BigDecimal(0);
		File fileInfo = null;
		//Generacion de Archivo Broad Movimiento Contable
		
		HashMap<String, Object> parmsIN = new HashMap<String, Object>();
		parmsIN.put("FechaEjecucion", fecha);
		out = _sql.query(con, SQL_CONSULTA, parmsIN , BroadMovimiento.class);
		out.toString();
		
		FileWriter flwriter = null;
		try {
			
			log.EscribirLog("INF", "Creando Archivo: "+ nombreArchivo);
			ruta = rutaDescarga ;
			
			fileInfo=new File(ruta + nombreArchivo);
			
			//flwriter = new FileWriter(ruta + nombreArchivo);
			
			//crea el flujo para escribir en el archivo
			//crea un buffer o flujo intermedio antes de escribir directamente en el archivo
			//BufferedWriter bfwriter = new BufferedWriter(flwriter);
			
			linea  = linea + "COD-INSTITUCION ";
			linea  = linea + "COD-APLICATIVO ";
			linea  = linea + "COD-ENTIDAD ";
			linea  = linea + "FEC-DIA ";
			linea  = linea + "HRA-TRASACCION ";
			linea  = linea + "NUM-OPERACION ";
			linea  = linea + "NUM-POS_OPERACION ";
			linea  = linea + "NUM-REGISTRO ";
			linea  = linea + "COD-CTA-CONTABLE ";
			linea  = linea + "COD-TIPO-CUENTA ";
			linea  = linea + "COD-PRODUCTO-CONTABLE ";
			linea  = linea + "COD-CENTRO-COSTO ";
			linea  = linea + "FEC-DOCUMENTO ";
			linea  = linea + "NRO-SERIE-DOC ";
			linea  = linea + "NRO-DOC ";
			linea  = linea + "COD-AFILIADA ";
			linea  = linea + "TIP-CARGO-ABONO ";
			linea  = linea + "COD-MONEDA-CONTABLE ";
			linea  = linea + "MTO-TRANSACCION-CONTABLE ";
			linea  = linea + "IND-ANULADO ";
			linea  = linea + "IND-OPE-AFILIADA ";
			linea  = linea + "FILLER-01 ";
			linea  = linea + "FILLER-02";
			linea  = linea + "\n";
			
			ListIterator<BroadMovimiento> iter = out.listIterator(out.size());
			while (iter.hasPrevious())
			{
				bc = new BroadMovimiento();
				bc = iter.previous();
				
				linea  = linea + String.format("%1$-1s",bc.CodInstitucion);
				linea  = linea + String.format("%1$-2s",bc.CodAplicativo);
				linea  = linea + String.format("%1$-7s",bc.CodEntidad);
				//Date fechaNueva = format.parse(bc.FecDia.toString());
				dateFormat = new SimpleDateFormat("yyyyMMdd").format(bc.FecDia);
				linea  = linea + String.format("%1$-8s", dateFormat);
				linea  = linea + String.format("%1$-8s",bc.HraTransaccion);
				linea  = linea + String.format("%1$-10s",bc.NumOperacion);
				linea  = linea + String.format("%1$-10s",bc.NumPosOperacion);
				linea  = linea + String.format("%1$-10s",contador);
				linea  = linea + String.format("%1$-20s",bc.CodCtaContable);
				linea  = linea + String.format("%1$-20s",bc.CodTipoCuenta);
				linea  = linea + String.format("%1$-8s",bc.CodProductoContable);
				linea  = linea + String.format("%1$-8s",bc.CodCentroCosto);
				dateFormat = new SimpleDateFormat("yyyyMMdd").format(bc.FecDocumento);
				linea  = linea + String.format("%1$-8s", dateFormat);
				linea  = linea + String.format("%1$-10s",bc.NroSerieDoc);
				linea  = linea + String.format("%1$-11s",bc.NroDoc);
				linea  = linea + String.format("%1$-4s",bc.CodAfiliada);
				linea  = linea + String.format("%1$-1s",bc.TipCargoAbono);
				linea  = linea + String.format("%1$-4s",bc.CodMonedaContable);
				linea  = linea + String.format("%1$-20s",bc.MontoTransaccionContable);
				linea  = linea + String.format("%1$-1s",bc.IndAnulado);
				linea  = linea + String.format("%1$-1s",bc.IndOpeAfiliada);
				linea  = linea + String.format("%1$-4s",bc.Filler01);
				linea  = linea + String.format("%1$-4s",bc.Filler02);
				linea  = linea + "\n";
					
				totalMtoSumCampo = totalMtoSumCampo.add(bc.MontoTransaccionContable) ;
			}
			//cierra el buffer intermedio
			//bfwriter.write(linea);
			//bfwriter.close();
			FileUtils.writeStringToFile(fileInfo,linea, "ISO-8859-1", true);
			linea = "";			
			log.EscribirLog("INF", "Fin de creacion de Archivo: " + nombreArchivo);
			
			
			
 
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
		
		
		//Generacion de archivo de control del Broad Contable
		try {
			
			log.EscribirLog("INF", "Creando Archivo: "+ nombreArchivoCtr);
			linea  = linea + "TIP-RUTINA ";
			linea  = linea + "DES-RUTINA ";
			linea  = linea + "DES-SUBRUTINA ";
			linea  = linea + "DES-ESTRUCTURA ";
			linea  = linea + "DES-CAMPOVALIDAR ";
			linea  = linea + "POS-INICIAL ";
			linea  = linea + "POS-FINAL ";
			linea  = linea + "MTO-SUMCAMPO ";
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
			ac.setDesCampoValidar("MTO-TRANSACCION-CONTABLE");
			ac.setPosInicial("151");
			ac.setPosFinal("170");
			ac.setMtoSumcampo(totalMtoSumCampo.toString());
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
			linea  = linea + ac.getDesCampoValidar();
			linea  = linea + ac.getPosInicial();
			linea  = linea + ac.getPosFinal();
			linea  = linea + ac.getMtoSumcampo();
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
		System.out.println("== END ==");
	}
}
