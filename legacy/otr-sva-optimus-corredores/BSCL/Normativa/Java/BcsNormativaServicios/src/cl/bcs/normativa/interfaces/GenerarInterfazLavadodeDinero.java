package cl.bcs.normativa.interfaces;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class GenerarInterfazLavadodeDinero implements JavaCall{
	
	private final Log log =new Log("GenerarInterfazLavadodeDinero");
	private final String extencion = ".TXT";
	private final String SQL_SELECT_ARCHIVO_PLDPRD = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazLavadodeDinero_Select_Archivo_PLDPRD.sql");
	private final String SQL_SELECT_ARCHIVO_PLDTRS = loadResourceToString("cl/bcs/normativa/interfaces/sql/GenerarInterfazLavadodeDinero_Select_Archivo_PLDTRS.sql");
		
	public static class EntradaGenerarInterfazLavadodeDinero{
		public Long 	p_IdEmpresa;
		public String	p_Fecha;
		public String	p_Ruta;
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaGenerarInterfazLavadodeDinero{
		public List<String> p_out_Archivos;
		public Long 	p_out_ErroresContador;
		public String 	p_out_Comentario;
		public String 	p_out_nombre_archivo_log;
		public String 	p_out_ruta_archivo_log;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ComprobantesPLDPRD{
		public String CodigoTransaccion;
		public String MedioPago;
		public String FechaTransaccion;
		public String FechaCierre;
		public Long Folio;
		public String DocumentoIdentidad;
		public String MontoOriginal;
		public String CodigoMoneda;
		public String CodogoSubProducto;

		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ComprobantesPLDTRS{
		public String CodigoTransaccion;
		public String MedioPago;
		public String FechaTransaccion;
		public Long Folio;
		public String DocumentoIdentidad;
		public String MontoOriginal;
		public String CodigoMoneda;
		public String CodogoSubProducto;
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();	
		EntradaGenerarInterfazLavadodeDinero in=gson.fromJson(jin.toString() , EntradaGenerarInterfazLavadodeDinero.class);
		SalidaGenerarInterfazLavadodeDinero out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	} 
	
	public  SalidaGenerarInterfazLavadodeDinero logica(Connection con, EntradaGenerarInterfazLavadodeDinero entrada) throws Exception {
		SalidaGenerarInterfazLavadodeDinero out = new SalidaGenerarInterfazLavadodeDinero(); 
		List<String> listArchivos = new ArrayList<>();
		String nombreArchivoPLDPRD = "PLDPRD" + entrada.p_Fecha.replace("-", "").substring(2,8) + extencion;
		String rutaArchivoPLDPRD = entrada.p_Ruta + "/" + nombreArchivoPLDPRD;
		listArchivos.add(rutaArchivoPLDPRD);
		String nombreArchivoPLDTRS = "PLDTRS" + entrada.p_Fecha.replace("-", "").substring(2,8) + extencion;
		String rutaArchivoPLDTRS = entrada.p_Ruta+"/" + nombreArchivoPLDTRS;
		listArchivos.add(rutaArchivoPLDTRS);
		String nombreArchivoLog = "PLDTRSLOG" + entrada.p_Fecha.replace("-", "").substring(2,8) + ".log";
		String rutaArchivoLog= entrada.p_Ruta+"/" + nombreArchivoLog;

		File filePLDPRD = new File(rutaArchivoPLDPRD);
		File filePLDTRS = new File(rutaArchivoPLDTRS);
		File archivoLog = new File(rutaArchivoLog);
		
		
		BufferedWriter bwArchivoPLDPRD = new BufferedWriter(new FileWriter(filePLDPRD));
		BufferedWriter bwArchivoPLDTRS = new BufferedWriter(new FileWriter(filePLDTRS));
		BufferedWriter bwArchivoLog = new BufferedWriter(new FileWriter(archivoLog));
		try{
			SQL sql=new SQL();
			//ARCHIVO PLDPRD
			HashMap<String,Object> params = new HashMap<String,Object>();
			params.put("Fecha", entrada.p_Fecha);
			params.put("IdEmpresa", entrada.p_IdEmpresa);
			List<ComprobantesPLDPRD> listDatosPLDPRD = sql.query(con, SQL_SELECT_ARCHIVO_PLDPRD, params, ComprobantesPLDPRD.class);
			List<ComprobantesPLDTRS> listDatosPLDTRS = sql.query(con, SQL_SELECT_ARCHIVO_PLDTRS, params, ComprobantesPLDTRS.class);
						
			//ARCHIVO PLDPRD
			for(ComprobantesPLDPRD comprobante: listDatosPLDPRD) {				
				StringBuilder pldprd = new StringBuilder(); 
				String CF3 = StringUtils.leftPad(";", 4, ' ') + StringUtils.leftPad(";", 11, '0') + "0;0;";			 	
				pldprd.append("COR ;");// COD_SISTEMA
				pldprd.append(StringUtils.rightPad(  StringUtils.leftPad(comprobante.Folio.toString(), 8, '0'),30, ' '   )  );//NUM_OPERACION
				pldprd.append(";");// COD_SISTEMA
				if(comprobante.DocumentoIdentidad.contains("-")) {
					String[] documentoIdentidad = comprobante.DocumentoIdentidad.split("-");
					pldprd.append(StringUtils.leftPad(documentoIdentidad[0], 10, '0') + ";");//RUT_CLIENTE
				 	pldprd.append(documentoIdentidad[1] + ";");//DIG_VERIF
				}else {
					pldprd.append(StringUtils.leftPad(comprobante.DocumentoIdentidad, 10, '0') + ";");//RUT_CLIENTE
				 	pldprd.append("0;");//DIG_VERIF
				}				
			 	pldprd.append("1;");//COD_ORIGEN
			 	pldprd.append(comprobante.CodogoSubProducto);//COD_SUBPROD
			 	pldprd.append(comprobante.FechaTransaccion.replace("-", "") + ";");//FEC_APERTURA -- corresponde a la fecha de OPERACION
			 	pldprd.append("01;");//ESTADO
			 	pldprd.append("1;");//IND_CUENTA
			 	//Si el comprobante esta en la tabla MovimientoComprobante, entonces que muestre la fecha liquidacion de esa tabla, sino la  fecha de consulta
			 	pldprd.append(comprobante.FechaCierre.replace("-", "") + ";");//FEC_CIERRE
			 	pldprd.append(StringUtils.leftPad(";", 18, '0'));//CF1
			 	pldprd.append("00000000000000000;0000000;");//CF2
				pldprd.append(StringUtils.repeat(CF3, 10));//CF3
				pldprd.append(StringUtils.leftPad("", 16, ' '));//CF4
			 	
				bwArchivoPLDPRD.write(pldprd.toString());
				bwArchivoPLDPRD.write("\r\n");
				
			}
			
			//ARCHIVO PLDTRS
			for(ComprobantesPLDTRS comprobante: listDatosPLDTRS) {
				StringBuilder pldtrs = new StringBuilder(); 
				String CF2 = StringUtils.leftPad(";", 141, ' ') + StringUtils.leftPad(";", 16, ' ') + StringUtils.leftPad(";", 76, ' ') + StringUtils.leftPad(";", 6, ' ');
				pldtrs.append(comprobante.CodigoTransaccion);//COD_TRANS
				pldtrs.append(comprobante.MedioPago);//MEDIO_PAGO
				pldtrs.append(comprobante.FechaTransaccion.replace("-", "") + ";");//FEC_TRANS
				pldtrs.append("COR ;");//COD_SISTEMA
				pldtrs.append(StringUtils.leftPad(comprobante.Folio.toString(), 8, '0'));//NUM_OPERACION
				pldtrs.append(StringUtils.leftPad(";", 23, ' '));//formato NUM_OPERACION
				pldtrs.append(StringUtils.leftPad(";", 31, ' '));//NUM_REFERENCIA
				if(comprobante.DocumentoIdentidad.contains("-")) {
					String[] documentoIdentidad = comprobante.DocumentoIdentidad.split("-");
					pldtrs.append(StringUtils.leftPad(documentoIdentidad[0], 10, '0') + ";");//RUT_CLIENTE
					pldtrs.append(documentoIdentidad[1] + ";");//DIG_VERIF
				}else {
					pldtrs.append(StringUtils.leftPad(comprobante.DocumentoIdentidad, 10, '0') + ";");//RUT_CLIENTE
					pldtrs.append("0;");//DIG_VERIF
				}
				pldtrs.append("1;");//COD_ORIGEN
				pldtrs.append("0963;");//CF1
				pldtrs.append("0963;");//CF1
				pldtrs.append(StringUtils.leftPad(comprobante.MontoOriginal.replace(".", ""), 19, '0') + ";");//MONTO_ORIGINAL
				pldtrs.append(comprobante.CodigoMoneda);//COD_MONEDA
				pldtrs.append("SINDATO  ;");//CANAL
				pldtrs.append(comprobante.CodogoSubProducto);//COD_SUBPROD
				pldtrs.append(StringUtils.repeat(CF2, 2));//CF2
				pldtrs.append(StringUtils.leftPad(";", 17, ' '));//CF3
				pldtrs.append(StringUtils.leftPad("", 19, '0'));//CF4
				
				bwArchivoPLDTRS.write(pldtrs.toString());
				bwArchivoPLDTRS.write("\r\n");
			
			}
		}catch(Exception e){
			log.write(e);
			 out.p_out_Archivos = listArchivos;
			 out.p_out_Comentario = "Archivos no pudieron ser Generados";
				bwArchivoLog.write( out.p_out_Comentario.toString());
			 out.p_out_ErroresContador = 1L;
		}finally{ 
		
			 out.p_out_Archivos = listArchivos;
			 out.p_out_Comentario = "Archivos generados exitosamente";
			 bwArchivoLog.write( out.p_out_Comentario.toString());
			 out.p_out_ErroresContador = 0L;
			 out.p_out_nombre_archivo_log = nombreArchivoLog;
			 out.p_out_ruta_archivo_log = rutaArchivoLog;
			 log.writeAsJson("SALIDA",out);
			 if(bwArchivoPLDPRD !=null){
				 bwArchivoPLDPRD.close();
				 bwArchivoLog.close();
			 }
			 if(bwArchivoPLDTRS != null){
				 bwArchivoPLDTRS.close();
				 bwArchivoLog.close();
			 }
		}	
		return out;
	}
	
	public static void main(String[] args) {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			
			EntradaGenerarInterfazLavadodeDinero entrada= new EntradaGenerarInterfazLavadodeDinero();
				entrada.p_IdEmpresa		= new Long(1);
				entrada.p_Fecha			= "2022-05-31";
				entrada.p_Ruta			= "C:\\BCS\\logs";
				JsonParser parser 		= new JsonParser();
				JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
				GenerarInterfazLavadodeDinero demo= new GenerarInterfazLavadodeDinero();
			try {
				System.out.println(demo.call(con, dataJsonIn).toString());
				con.commit();
				System.out.println("SALIDA");
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
			con.rollback();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}
