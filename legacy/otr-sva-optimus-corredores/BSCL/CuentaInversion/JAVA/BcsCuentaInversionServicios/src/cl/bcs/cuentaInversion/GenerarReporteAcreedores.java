package cl.bcs.cuentaInversion;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.cuentaInversion.ConsultaReporteAcreedores.EntradaConsultaReporteAcreedores;
import cl.bcs.cuentaInversion.ConsultaReporteAcreedores.SalidaConsultaReporteAcreedores;
import cl.bcs.cuentaInversion.ConsultaReporteAcreedores.SalidaReporteAcreedores;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.StringUtils;
import cl.bcs.utils.Utils;

public class GenerarReporteAcreedores implements JavaCall{

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		// TODO Auto-generated method stub
		Gson gson = new Gson();	
		EntradaGenerarReporteAcreedores in = gson.fromJson(jin.toString(),EntradaGenerarReporteAcreedores.class);
		SalidaGenerarReporteAcreedores out = logica(conn,in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES); 
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	private final static Logger logger = LogManager.getLogger(GenerarReporteAcreedores.class);
	
	private final String SQL_SELECT_RAIZ_ARCHIVO 		= loadResourceToString("cl/bcs/cuentaInversion/sql/GenerarReporteAcreedores_SelectConfiguracionArchivo.sql");
	private final String SQL_SELECT_CODIGOCORREDORA		= loadResourceToString("cl/bcs/cuentaInversion/sql/GenerarReporteAcreedores_SelectCodigoCorredora.sql");
	private final String SQL_ARCHIVO_FORMATO_ESTRUCTURA = loadResourceToString("cl/bcs/cuentaInversion/sql/GenerarReporteAcreedores_SelectArchivoFormatoConfiguracion.sql");
	
	public static class EntradaGenerarReporteAcreedores extends BcsPojo{
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;					
    	public String	p_FechaSolicitud;    	
    	public String 	p_IdMoneda;
    	public String	p_Consolidar;
    	public String 	p_NombreInternoArchivo;
	}
	
	public static class SalidaGenerarReporteAcreedores extends BcsPojo{
		public List<MensajeSalida>			P_OUT_MENSAJES;
		public Long							P_OUT_ERRORESCONTADOR;
		public String 						P_OUT_RUTAARCHIVO;
		public String 						P_OUT_CODIGOCORREDOR;
		
		public SalidaGenerarReporteAcreedores() {
			this.P_OUT_MENSAJES  		= new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR 	= 0L;	
			this.P_OUT_RUTAARCHIVO 		= "";
			this.P_OUT_CODIGOCORREDOR	= "";
		}
	}
	
	public static class Corredora extends BcsPojo{
		public String IdEmpresa;
		public String CodigoCorredora;
	}
	
	public static class RaizArchivo extends BcsPojo{
		public String RutaArchivo;
		public String RutaModulo;
		public RaizArchivo() {
			this.RutaModulo = "";
			this.RutaArchivo = "";
		}
	}
	
	public static class CamposArchivo extends BcsPojo{
		public String CodigoCampo;
		public String CampoNombreCompleto;
		public int Posicion;
	}
	
	public SalidaGenerarReporteAcreedores logica (Connection con, EntradaGenerarReporteAcreedores in) throws Exception {
		logger.info("INICIO::GenerarReporteAcreedores");
		long inicio = System.currentTimeMillis();
		
		SalidaGenerarReporteAcreedores out = new SalidaGenerarReporteAcreedores();
		
		SQL sql = new SQL();
		
		RaizArchivo raizArchivo = obtenerRaizArchivo(con, in.p_IdEmpresa);
		
		if (StringUtils.isNullOrBlank(raizArchivo.RutaArchivo) || StringUtils.isNullOrBlank(raizArchivo.RutaModulo)) {
			out.P_OUT_ERRORESCONTADOR++;
			out.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR,"FALTA_CONFIGURACION_RUTA_ARCHIVO", new HashMap<>(), con, in.p_IdEmpresa));
			return out;
		}
		
		String codigoCorredora = obtenerCodigoCorredora(con, sql, in.p_IdEmpresa);
		
		if(codigoCorredora.trim().length()==0) {
			out.P_OUT_ERRORESCONTADOR++;
			out.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR,"CODIGO_CORREDOR_NO_EXISTE", new HashMap<>(), con, in.p_IdEmpresa));
			return out;
		}
		
		String nombreArchivo = in.p_NombreInternoArchivo+".xlsx";
		String rutaReportefinal = creacionReportefinal(con, sql, raizArchivo, nombreArchivo, codigoCorredora,in, out);
		
		if(rutaReportefinal.trim().length() == 0) {
			out.P_OUT_ERRORESCONTADOR++;
			out.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR,"UI_ERROR_AL_GENERAR_REPORTE", new HashMap<>(), con, in.p_IdEmpresa));
			return out;
		}
		
		out.P_OUT_RUTAARCHIVO = rutaReportefinal;
		out.P_OUT_CODIGOCORREDOR = "CODIGOCORREDOR_"+codigoCorredora;
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::GenerarReporteAcreedores tiempo: " + (fin - inicio));
		
		return out;
	}
	
	private RaizArchivo obtenerRaizArchivo(Connection conn, Long idEmpresa) throws SQLException {
		logger.debug("INICIO::GenerarReporteAcreedores.obtenerRaizArchivo");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> params = new HashMap<>(1);
		params.put("IdEmpresa", idEmpresa);
		RaizArchivo raiz = new SQL().query(conn, SQL_SELECT_RAIZ_ARCHIVO, params, RaizArchivo.class).stream()
																									.findFirst().orElse(new RaizArchivo());
		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarReporteAcreedores.obtenerRaizArchivo tiempo: " + (fin - inicio));
		return raiz;
	}
	
	private String obtenerCodigoCorredora(Connection con, SQL sql, Long IdEmpresa) throws Exception{
		logger.debug("INICIO::GenerarReporteAcreedores.obtenerCodigoCorredora");
		long inicio = System.currentTimeMillis();
		
		String codigoCorredora = "";
		HashMap<String, Object> params = new HashMap<>();
		params.put("IdEmpresa", IdEmpresa);
		List<Corredora> codigoCorresdora = sql.query(con, SQL_SELECT_CODIGOCORREDORA, params, Corredora.class);
		if(codigoCorresdora.size() == 1) {
			Corredora corred = codigoCorresdora.get(0);
			codigoCorredora = corred.CodigoCorredora;
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarReporteAcreedores.obtenerCodigoCorredora tiempo: " + (fin - inicio));
		return codigoCorredora;
	}
	
	private String creacionReportefinal(Connection con, SQL sql, RaizArchivo rutaRaiz, String nombreArchivo, String codigoCorredora, EntradaGenerarReporteAcreedores entrada, SalidaGenerarReporteAcreedores salida) throws Exception {
		logger.trace("INICIO::GenerarReporteAcreedores.creacionReportefinal");
		long inicio = System.currentTimeMillis();
		
		String rutaSalida = "";
        HashMap<String, Object> hashCampos = obtenerCabeceraYPosicionesTituloArchivo(con, sql, entrada.p_IdEmpresa, entrada.p_Consolidar);
        
        if(hashCampos.isEmpty()) {
        	salida.P_OUT_ERRORESCONTADOR ++;
        	salida.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR,"FALTA_CONFIGURACION_ARCHIVO_FORMATO", new HashMap<>(), con, entrada.p_IdEmpresa));
        	return rutaSalida;
        }
        
        File archivo = new File(nombreArchivo);
        Workbook workbook = new XSSFWorkbook();
        Sheet pagina = workbook.createSheet(obtenerNombreHoja(codigoCorredora, entrada));
        
        confeccionarCabeceraArchivo(workbook, pagina, hashCampos, entrada.p_Consolidar);
        confeccionarCuerpoArchivo(con, entrada, salida, pagina, hashCampos);
        
        try {
        	String rutaCompleta = rutaRaiz.RutaArchivo+archivo;
            FileOutputStream archivoSalida = new FileOutputStream(rutaCompleta);
            workbook.write(archivoSalida);
            workbook.close();
        	rutaSalida = rutaRaiz.RutaModulo+archivo;
        } catch (FileNotFoundException ex) {
        	ex.printStackTrace();
        } catch (IOException exIO) {
        	exIO.printStackTrace();
        }
        
		long fin = System.currentTimeMillis();
		logger.trace("FIN::GenerarReporteAcreedores.creacionReportefinal tiempo: " + (fin - inicio));
		return rutaSalida;
	}
	
	private HashMap<String, Object> obtenerCabeceraYPosicionesTituloArchivo(Connection con, SQL sql, Long IdEmpresa, String Consolidado) throws Exception {
		logger.debug("INICIO::GenerarReporteAcreedores.obtenerCabeceraYPosicionesTituloArchivo");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> hashCampos = new HashMap<>();
		HashMap<String, Object> params = new HashMap<>(2);
		params.put("IdEmpresa", IdEmpresa);
		params.put("Consolidado", Consolidado);
		List<CamposArchivo> camposArchivo = sql.query(con, SQL_ARCHIVO_FORMATO_ESTRUCTURA, params, CamposArchivo.class);
		if(!camposArchivo.isEmpty()) {
			hashCampos = Utils.List2Hash(camposArchivo,"CodigoCampo");
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarReporteAcreedores.obtenerCabeceraYPosicionesTituloArchivo tiempo: " + (fin - inicio));
		return hashCampos;
	}
	
	private List<SalidaReporteAcreedores> obtenerHaberesCuentaInversion(Connection con, EntradaGenerarReporteAcreedores entrada, SalidaGenerarReporteAcreedores salida){
		logger.trace("INICIO::GenerarReporteAcreedores.obtenerHaberesCuentaInversion");
		long inicio = System.currentTimeMillis();
		
		ConsultaReporteAcreedores consultaReporteAcreedores =  new ConsultaReporteAcreedores();
		EntradaConsultaReporteAcreedores entradaConsultaReporteAcreedores = new EntradaConsultaReporteAcreedores();
		entradaConsultaReporteAcreedores.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaConsultaReporteAcreedores.p_IdUsuario = entrada.p_IdUsuario;
		entradaConsultaReporteAcreedores.p_IdModulo = entrada.p_IdModulo;
		entradaConsultaReporteAcreedores.p_IdEjecutivo =  0L;
		entradaConsultaReporteAcreedores.p_FechaConsulta = entrada.p_FechaSolicitud;
		entradaConsultaReporteAcreedores.p_IdMoneda = entrada.p_IdMoneda;

		entradaConsultaReporteAcreedores.p_Consolidar= entrada.p_Consolidar;
		SalidaConsultaReporteAcreedores salidaConsultaReporteAcreedores = new SalidaConsultaReporteAcreedores();
		
		try {
			salidaConsultaReporteAcreedores = consultaReporteAcreedores.logica(con, entradaConsultaReporteAcreedores);
			if(salidaConsultaReporteAcreedores.P_OUT_ERRORESCONTADOR.compareTo(0L) == 1  || !(salidaConsultaReporteAcreedores.P_OUT_MENSAJES.isEmpty())) {
				salida.P_OUT_ERRORESCONTADOR ++;
				salida.P_OUT_MENSAJES = salidaConsultaReporteAcreedores.P_OUT_MENSAJES;			
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		List<SalidaReporteAcreedores> listadoClienteHaberes = new ArrayList<>();
		if(salidaConsultaReporteAcreedores.P_OUT_ERRORESCONTADOR.compareTo(0L) == 0) {
			listadoClienteHaberes = salidaConsultaReporteAcreedores.P_OUT_TABLA;
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::GenerarReporteAcreedores.obtenerHaberesCuentaInversion tiempo: " + (fin - inicio));
		return listadoClienteHaberes;
	}
	
	private String obtenerNombreHoja(String codigoCorredora, EntradaGenerarReporteAcreedores entrada) {
		logger.trace("INICIO::GenerarReporteAcreedores.obtenerNombreHoja");
		long inicio = System.currentTimeMillis();
		
		String [] fechaArray = entrada.p_FechaSolicitud.split("-");
		String fecha = fechaArray[2]+fechaArray[1]+fechaArray[0];
		String nombreHoja = codigoCorredora+"_"+fecha+"_cuentas_por_cobrar";
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::GenerarReporteAcreedores.obtenerNombreHoja tiempo: " + (fin - inicio));
		return nombreHoja;
	}
	
	private void confeccionarCabeceraArchivo(Workbook workbook, Sheet pagina, HashMap<String, Object> hashCampos, String Consolidado) {
		logger.trace("INICIO::GenerarReporteAcreedores.confeccionarCabeceraArchivo");
		long inicio = System.currentTimeMillis();
		
		CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
        style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        style.setBorderLeft(CellStyle.BORDER_THIN);
        style.setBorderRight(CellStyle.BORDER_THIN);
        style.setBorderBottom(CellStyle.BORDER_THIN);
        
        Row fila = pagina.createRow(0);
        short altoFila = 1000;
        fila.setHeight(altoFila);
        int anchoColumna = 6000;
        
        for (String key : hashCampos.keySet()) {
        	CamposArchivo campoArchivo = (CamposArchivo) hashCampos.get(key);
        	//Campo Sec.
        	if(campoArchivo.Posicion == 0) {
        		pagina.setColumnWidth(campoArchivo.Posicion, 3000);
        	}
        	//Campos grup. de haberes
        	else if(campoArchivo.Posicion >= 5 && campoArchivo.Posicion <= 7 && "S".equalsIgnoreCase(Consolidado) ) {
        		pagina.setColumnWidth(campoArchivo.Posicion, 8000);
        	}
        	else if(campoArchivo.Posicion >= 6 && campoArchivo.Posicion <= 8 && "N".equalsIgnoreCase(Consolidado) ) {
        		pagina.setColumnWidth(campoArchivo.Posicion, 8000);
        	}
        	else {
        		pagina.setColumnWidth(campoArchivo.Posicion, anchoColumna);
        	}
            Cell celda = fila.createCell(campoArchivo.Posicion);
            celda.setCellStyle(style);
            celda.setCellValue(campoArchivo.CampoNombreCompleto);
        }

		long fin = System.currentTimeMillis();
		logger.trace("FIN::GenerarReporteAcreedores.confeccionarCabeceraArchivo tiempo: " + (fin - inicio));
	}
	
	private void confeccionarCuerpoArchivo(Connection con, EntradaGenerarReporteAcreedores entrada, SalidaGenerarReporteAcreedores salida, Sheet pagina, HashMap<String, Object> hashCampos) throws Exception {
		logger.trace("INICIO::GenerarReporteAcreedores.confeccionarCuerpoArchivo");
		long inicio = System.currentTimeMillis();
		Integer rowCuerpo = 1;
		
		List<SalidaReporteAcreedores> datosClienteHaberes = obtenerHaberesCuentaInversion(con, entrada, salida);
		if((salida.P_OUT_ERRORESCONTADOR.compareTo(0L) == 0) && !datosClienteHaberes.isEmpty()) {
			HashMap<String, Integer> HashCodigoPosicion = obtenerPosicionesParaCuerpoArchivo(hashCampos);
			Long cont = 1L;
			for(SalidaReporteAcreedores clienteHaber : datosClienteHaberes) {
				
				Row fila = pagina.createRow(rowCuerpo);
				Cell celdaSec = fila.createCell(HashCodigoPosicion.get("RACREEDORES_SECUENCIA"));
				celdaSec.setCellValue(cont);
				//Rut
	            Cell celdaRut = fila.createCell(HashCodigoPosicion.get("RACREEDORES_RUTCLIENTE"));
	            celdaRut.setCellValue(clienteHaber.DocumentoIndentidad);
				//Nombre
	            Cell celdaNombre = fila.createCell(HashCodigoPosicion.get("RACREEDORES_NOMBRECLIENTE"));
	            celdaNombre.setCellValue(clienteHaber.NombreCliente);
	            
	            if("N".equalsIgnoreCase(entrada.p_Consolidar)) {
	            	Cell celdaCodigoPortafolio = fila.createCell(HashCodigoPosicion.get("RACREEDORES_CODIGOPORTAFOLIO"));
		            celdaCodigoPortafolio.setCellValue(clienteHaber.CodigoPortafolio);
	            }
	            
	            //MontoTotalPorCobrar
	            String MontoTotalPorPagar	= clienteHaber.MontoPorPagarTotal.toPlainString();
	            String HaberesVigentes		= clienteHaber.TramoA.toPlainString();
	            String HaberesTramoB		= clienteHaber.TramoB.toPlainString();
	            String HaberesTramoC		= clienteHaber.TramoC.toPlainString();
	            String HaberesTramoD		= clienteHaber.TramoD.toPlainString();

	            Cell celdaMontoPorPagar = fila.createCell(HashCodigoPosicion.get("RACREEDORES_MONTOTOTALPORPAGAR"));
	            celdaMontoPorPagar.setCellValue(MontoTotalPorPagar); 
	            //Haber Vigentes
	            Cell celdaHaberesVigentes = fila.createCell(HashCodigoPosicion.get("RACREEDORES_HABERESVIGENTES"));
	            celdaHaberesVigentes.setCellValue(HaberesVigentes);
	            //Haber de 3 a 10 dias 
	            Cell celdaHaberesDe3A10Dias = fila.createCell(HashCodigoPosicion.get("RACREEDORES_HABERESDE3A10DIAS"));
	            celdaHaberesDe3A10Dias.setCellValue(HaberesTramoB);
	            //Haber de 11 a 30 dias 
	            Cell celdaHaberesDe11A30Dias = fila.createCell(HashCodigoPosicion.get("RACREEDORES_HABERESDE11A30DIAS"));
	            celdaHaberesDe11A30Dias.setCellValue(HaberesTramoC);
	            //Haber Mas de 30 dias 
	            Cell celdaHaberesMasde30Dias = fila.createCell(HashCodigoPosicion.get("RACREEDORES_HABERESMASDE30DIAS"));
	            celdaHaberesMasde30Dias.setCellValue(HaberesTramoD);
	            //Observaciones 
	            Cell celdaObservaciones = fila.createCell(HashCodigoPosicion.get("RACREEDORES_OBSERVACIONES"));
	            celdaObservaciones.setCellValue(clienteHaber.Observacion);
	            //Clasifiacion contable  
	            Cell celdaClasifiacionContable = fila.createCell(HashCodigoPosicion.get("RACREEDORES_CLASIFICACIONCONTABLE"));
	            celdaClasifiacionContable.setCellValue(clienteHaber.ClasificacionContable);
	            //Relacionado
	            Cell celdaRelacionado = fila.createCell(HashCodigoPosicion.get("RACREEDORES_RELACIONADO"));
	            celdaRelacionado.setCellValue(clienteHaber.Relacionado);
	            
	            cont ++;
	            rowCuerpo ++;
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarReporteAcreedores.confeccionarCuerpoArchivo tiempo: " + (fin - inicio));
	}
	
	private HashMap<String, Integer> obtenerPosicionesParaCuerpoArchivo(HashMap<String, Object> hashCampos) {
		logger.trace("INICIO::GenerarReporteAcreedores.obtenerPosicionesParaCuerpoArchivo");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Integer> HashCodigoPosicion = new HashMap<>();
		HashSet<String> hasHCodigo = new HashSet<>(); 
		for (Map.Entry<String, Object> entry : hashCampos.entrySet()) {
			if(hasHCodigo.add(entry.getKey())==true) {
				CamposArchivo campoArchivo = (CamposArchivo) entry.getValue();
				HashCodigoPosicion.put(entry.getKey(), campoArchivo.Posicion);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::GenerarReporteAcreedores.obtenerPosicionesParaCuerpoArchivo", fin - inicio);
		return HashCodigoPosicion;
	}

}
