package cl.bcs.socionegocio.soc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.opencsv.CSVWriter;

import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class GenerarReporteSocioNegocio implements JavaCall{
	private final Log log = new Log("SOC.GenerarReporteSocioNegocio");
	private final String SQL_CONSULTA_SOCIO_NEGOCIO = loadResourceToString("cl/bcs/socionegocio/soc/sql/GenerarReporteSocioNegocio_ConsultaSocioNegocio.sql");
	
	/*******************
	 * clases in/out
	 *******************/
	
	public static class InGeneraReporteSocioNegocio{
		public Long    p_IdEmpresa;
		public Long	   p_IdUsuario;
		public String  p_IdModulo;
		public String  p_EsCliente;
		public String  p_EsEmpleado;
		public String  p_EsProveedor;
		public String  p_EsContraparte;
		public String  p_IdTipoDocumentoIdentidadDefecto;
		public String  p_ListaDocumentosIdentidad;
		public String  p_FechaHasta;
		public String  p_FechaDesde;
		public String  p_Alias;
		public String  p_NombreCompleto;
		public String  p_Ruta;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class OutGeneraReporteSocioNegocio{
		
		public Long 	  P_OUT_ERRORESCONTADOR;
		public String 	  P_OUT_NOMBRE_ARCHIVO;
		public String	  P_OUT_RUTA_NOMBRE_ARCHIVO;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class SocioNegocio{
		public String FechaIngreso;
		public String NombreTipoDocumentoIdentidadDefecto;
		public String DocumentoIdentidadDefecto;
		public String TipoPersona;
		public String NombreCompleto;
		public String NombrePaisResidencia;
		public String EsCliente;
		public String EsEmpleado;
		public String EsProveedor;
		public String EsContraparte;
		public String DescripcionSegmento;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	/*******************
	 * método principal
	 *******************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = new Gson();		
		InGeneraReporteSocioNegocio in=gson.fromJson(jin.toString() ,InGeneraReporteSocioNegocio.class);
		OutGeneraReporteSocioNegocio out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	/**********************
	 * lógica del programa
	 **********************/
	
	public OutGeneraReporteSocioNegocio logica(Connection con, InGeneraReporteSocioNegocio entrada) throws Exception {
		
		OutGeneraReporteSocioNegocio salida= new OutGeneraReporteSocioNegocio();
		salida.P_OUT_ERRORESCONTADOR = new Long(1);
		
		SQL sql=new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("EsCliente", entrada.p_EsCliente.toUpperCase());
		params.put("EsEmpleado", entrada.p_EsEmpleado.toUpperCase());
		params.put("EsProveedor", entrada.p_EsProveedor.toUpperCase());
		params.put("EsContraparte", entrada.p_EsContraparte.toUpperCase());
		params.put("IdTipoDocumentoIdentidadDefecto", entrada.p_IdTipoDocumentoIdentidadDefecto);
		params.put("ListaDocumentosIdentidad", entrada.p_ListaDocumentosIdentidad.toUpperCase());
		params.put("FechaHasta", entrada.p_FechaHasta);
		params.put("FechaDesde", entrada.p_FechaDesde);
		params.put("Alias", entrada.p_Alias.toUpperCase());
		params.put("NombreCompleto", entrada.p_NombreCompleto.toUpperCase());
		List<SocioNegocio> sociosNegocio = sql.query(con,SQL_CONSULTA_SOCIO_NEGOCIO,params,SocioNegocio.class);
	    salida.P_OUT_NOMBRE_ARCHIVO = "SOC/ReporteSocioNegocio_" + Fecha.toFormat(FechaDB.ObtenerFechaHoraTs(con, entrada.p_IdEmpresa),"yyyyMMddHHmmss") + ".csv";
		salida.P_OUT_RUTA_NOMBRE_ARCHIVO = entrada.p_Ruta + "/" + salida.P_OUT_NOMBRE_ARCHIVO;
		//se crea carpeta 
		File folder = new File(entrada.p_Ruta+"/SOC");
		if(!folder.exists()){
			folder.mkdirs();
		}
		FileOutputStream os = new FileOutputStream(salida.P_OUT_RUTA_NOMBRE_ARCHIVO);
		os.write(0xef);
		os.write(0xbb);
		os.write(0xbf);
		CSVWriter EscribeCSV=new CSVWriter(new OutputStreamWriter(os,"UTF-8"),';');

		try{
			String [] FilaEscribeSocioNegocio= new String[]{
					"Fecha Ingreso",
					"Tipo Documento Identidad",
					"Documento Identidad",
					"Tipo Persona",
					"Nombre",
					"País Residencia",
					"Cliente",
					"Empleado",
					"Proveedor",
					"Contraparte",
					"Segmento Cliente"
					};
			
			EscribeCSV.writeNext(FilaEscribeSocioNegocio);
			log.write("Numero Socio Negocios",sociosNegocio.size());
			int  SegundosZonaHoraria=FechaDB.ObtenerSegundosZonaHoraria(con,entrada.p_IdEmpresa);
			Fecha.setLocalOffSet(SegundosZonaHoraria);
			for(SocioNegocio socioNegocio : sociosNegocio){
				
				FilaEscribeSocioNegocio = new String[]{
						socioNegocio.FechaIngreso,
						socioNegocio.NombreTipoDocumentoIdentidadDefecto,
						socioNegocio.DocumentoIdentidadDefecto,
						socioNegocio.TipoPersona,
						socioNegocio.NombreCompleto,
						socioNegocio.NombrePaisResidencia,
						socioNegocio.EsCliente,
						socioNegocio.EsEmpleado,
						socioNegocio.EsProveedor,
						socioNegocio.EsContraparte,
						socioNegocio.DescripcionSegmento		
				};
				EscribeCSV.writeNext(FilaEscribeSocioNegocio);
			}
		}
		catch(Exception e){
			log.write("Error",e);
		}
		finally{
			try{
				EscribeCSV.close();
			}
			catch(Exception e){
				log.write("Error",e);
			}
			
		}
		
		salida.P_OUT_ERRORESCONTADOR = new Long(0);
		log.writeAsJson("SALIDA:",salida);
		return salida;
		
	}
	
	
}
