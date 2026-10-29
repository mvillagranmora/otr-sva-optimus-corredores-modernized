package cl.bcs.cuentaInversion;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
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

import cl.bcs.cuentaInversion.ConsultaReporteAntiguedadBolsa.EntradaConsultaReporteAntiguedadBolsa;
import cl.bcs.cuentaInversion.ConsultaReporteAntiguedadBolsa.SalidaConsultaReporteAntiguedadBolsa;
import cl.bcs.cuentaInversion.ConsultaReporteAntiguedadBolsa.SalidaReporteAntiguedadBolsa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.StringUtils;
import cl.bcs.utils.Utils;

public class GenerarReporteAntiguedadDeudaBolsaCTA implements JavaCall{
	
	private final static Logger logger = LogManager.getLogger(GenerarReporteAntiguedadDeudaBolsaCTA.class);
	
	private final String SQL_SELECT_RAIZ_ARCHIVO 		= loadResourceToString("cl/bcs/cuentaInversion/sql/GenerarReporteAntiguedadDeudaBolsaCTA_SelectConfiguracionArchivo.sql");
	private final String SQL_SELECT_CODIGOCORREDORA		= loadResourceToString("cl/bcs/cuentaInversion/sql/GenerarReporteAntiguedadDeudaBolsaCTA_SelectCodigoCorredora.sql");
	private final String SQL_ARCHIVO_FORMATO_ESTRUCTURA = loadResourceToString("cl/bcs/cuentaInversion/sql/GenerarReporteAntiguedadDeudaBolsaCTA_SelectArchivoFormatoConfiguracion.sql");
	
	public static class EntradaGenerarReporteAntiguedadDeudaBolsaCTA extends BcsPojo{
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;					
    	public String	p_FechaSolicitud;    	
    	public String 	p_IdMoneda;
    	public String 	p_SeparaIntermediacion;
    	public String 	p_AgrupaPortafolios;
    	public String	p_Consolidar;
    	public String 	p_NombreInternoArchivo;
	}
	
	public static class SalidaGenerarReporteAntiguedadDeudaBolsaCTA extends BcsPojo{
		public List<MensajeSalida>			P_OUT_MENSAJES;
		public Long							P_OUT_ERRORESCONTADOR;
		public String 						P_OUT_RUTAARCHIVO;
		public String 						P_OUT_CODIGOCORREDOR;
		
		public SalidaGenerarReporteAntiguedadDeudaBolsaCTA() {
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
	
	public static class ClienteDeuda extends BcsPojo{
		public String Rut;
		public String Nombre;
		public BigDecimal MontoTotalCobrar;
		public BigDecimal MontoDeudaVigente;
		public BigDecimal MontoDeudaVencida3A10Dias;
		public BigDecimal MontoDeudaVencida11A30Dias;
		public BigDecimal MontoDeudaVencidaMasDe30Dias;
		public String Observaciones;
		public String ClasificacionContable;
		public String  Relacionado;
		
		public ClienteDeuda(String rut, String nombre, BigDecimal MontoTotal, BigDecimal montoVigente, BigDecimal MontoPrimeraDeuda, BigDecimal MontoSegundaDeuda,
							BigDecimal MontoTerceraDeuda, String Observa, String Clasificacion, String relacionado) {
			this.Rut = rut;
			this.Nombre = nombre;
			this.MontoTotalCobrar = MontoTotal;
			this.MontoDeudaVigente = montoVigente; 
			this.MontoDeudaVencida3A10Dias = MontoPrimeraDeuda;
			this.MontoDeudaVencida11A30Dias = MontoSegundaDeuda;
			this.MontoDeudaVencidaMasDe30Dias = MontoTerceraDeuda;
			this.Observaciones = Observa;
			this.ClasificacionContable = Clasificacion;
			this.Relacionado = relacionado;
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = new Gson();	
		EntradaGenerarReporteAntiguedadDeudaBolsaCTA in = gson.fromJson(jin.toString(),EntradaGenerarReporteAntiguedadDeudaBolsaCTA.class);
		SalidaGenerarReporteAntiguedadDeudaBolsaCTA out = logica(conn,in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES); 
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaGenerarReporteAntiguedadDeudaBolsaCTA logica(Connection con, EntradaGenerarReporteAntiguedadDeudaBolsaCTA in) throws Exception{
		logger.info("INICIO::GenerarReporteAntiguedadDeudaBolsaCTA");
		long inicio = System.currentTimeMillis();
		SalidaGenerarReporteAntiguedadDeudaBolsaCTA out = new SalidaGenerarReporteAntiguedadDeudaBolsaCTA();
		
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
		logger.info("FIN::GenerarReporteAntiguedadDeudaBolsaCTA tiempo: " + (fin - inicio));
		return out;
		
	}
	
	private RaizArchivo obtenerRaizArchivo(Connection conn, Long idEmpresa) throws SQLException {
		logger.debug("INICIO::GenerarReporteAntiguedadDeudaBolsaCTA.obtenerRaizArchivo");
		long inicio = System.currentTimeMillis();
		HashMap<String, Object> params = new HashMap<>(1);
		params.put("IdEmpresa", idEmpresa);
		RaizArchivo raiz = new SQL().query(conn, SQL_SELECT_RAIZ_ARCHIVO, params, RaizArchivo.class).stream()
																									.findFirst().orElse(new RaizArchivo());
		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarReporteAntiguedadDeudaBolsaCTA.obtenerRaizArchivo tiempo: " + (fin - inicio));
		return raiz;
	}
	
	private String obtenerCodigoCorredora(Connection con, SQL sql, Long IdEmpresa) throws Exception{
		logger.debug("INICIO::GenerarReporteAntiguedadDeudaBolsaCTA.obtenerCodigoCorredora");
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
		logger.debug("FIN::GenerarReporteAntiguedadDeudaBolsaCTA.obtenerCodigoCorredora tiempo: " + (fin - inicio));
		return codigoCorredora;
	}
	
	private String creacionReportefinal(Connection con, SQL sql, RaizArchivo rutaRaiz, String nombreArchivo, String codigoCorredora, EntradaGenerarReporteAntiguedadDeudaBolsaCTA entrada, SalidaGenerarReporteAntiguedadDeudaBolsaCTA salida) throws Exception {
		logger.trace("INICIO::GenerarReporteAntiguedadDeudaBolsaCTA.creacionReportefinal");
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
        confeccionarCuerpoArchivo(con, entrada, salida, workbook, pagina, hashCampos);
        
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
		logger.trace("FIN::GenerarReporteAntiguedadDeudaBolsaCTA.creacionReportefinal tiempo: " + (fin - inicio));
		return rutaSalida;
	}
	
	private String obtenerNombreHoja(String codigoCorredora, EntradaGenerarReporteAntiguedadDeudaBolsaCTA entrada) {
		logger.trace("INICIO::GenerarReporteAntiguedadDeudaBolsaCTA.obtenerNombreHoja");
		long inicio = System.currentTimeMillis();
		String [] fechaArray = entrada.p_FechaSolicitud.split("-");
		String fecha = fechaArray[2]+fechaArray[1]+fechaArray[0];
		String nombreHoja = codigoCorredora+"_"+fecha+"_cuentas_por_cobrar";
		long fin = System.currentTimeMillis();
		logger.trace("FIN::GenerarReporteAntiguedadDeudaBolsaCTA.obtenerNombreHoja tiempo: " + (fin - inicio));
		return nombreHoja;
	}
	
	private HashMap<String, Object> obtenerCabeceraYPosicionesTituloArchivo(Connection con, SQL sql, Long IdEmpresa, String Consolidado) throws Exception {
		logger.debug("INICIO::GenerarReporteAntiguedadDeudaBolsaCTA.obtenerCabeceraYPosicionesTituloArchivo");
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
		logger.debug("FIN::GenerarReporteAntiguedadDeudaBolsaCTA.obtenerCabeceraYPosicionesTituloArchivo tiempo: " + (fin - inicio));
		return hashCampos;
	}
	
	private void confeccionarCabeceraArchivo(Workbook workbook, Sheet pagina, HashMap<String, Object> hashCampos, String Consolidado) {
		logger.trace("INICIO::GenerarReporteAntiguedadDeudaBolsaCTA.confeccionarCabeceraArchivo");
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
        	//Campos grup. de deudas
        	else if(campoArchivo.Posicion >= 5 && campoArchivo.Posicion <= 7 && Consolidado.equalsIgnoreCase("S") ) {
        		pagina.setColumnWidth(campoArchivo.Posicion, 8000);
        	}
        	else if(campoArchivo.Posicion >= 6 && campoArchivo.Posicion <= 8 && Consolidado.equalsIgnoreCase("N") ) {
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
		logger.trace("FIN::GenerarReporteAntiguedadDeudaBolsaCTA.confeccionarCabeceraArchivo tiempo: " + (fin - inicio));
	}
	
	private void confeccionarCuerpoArchivo(Connection con, EntradaGenerarReporteAntiguedadDeudaBolsaCTA entrada, SalidaGenerarReporteAntiguedadDeudaBolsaCTA salida, Workbook workbook, Sheet pagina, HashMap<String, Object> hashCampos) throws Exception {
		logger.trace("INICIO::GenerarReporteAntiguedadDeudaBolsaCTA.confeccionarCuerpoArchivo");
		long inicio = System.currentTimeMillis();
		Integer rowCuerpo = 1;
		
		List<SalidaReporteAntiguedadBolsa> datosClienteDeuda = obtenerDeudasCuentaInversion(con, entrada, salida);
		if((salida.P_OUT_ERRORESCONTADOR.compareTo(0L) == 0) && !datosClienteDeuda.isEmpty()) {
			HashMap<String, Integer> HashCodigoPosicion = obtenerPosicionesParaCuerpoArchivo(hashCampos);
			Long cont = 1L;
			for(SalidaReporteAntiguedadBolsa clienteDeuda : datosClienteDeuda) {
				if(entrada.p_Consolidar.equalsIgnoreCase("N")) {
					Row fila = pagina.createRow(rowCuerpo);
					Cell celdaSec = fila.createCell(HashCodigoPosicion.get("RADB_SECUENCIA"));
					celdaSec.setCellValue(cont);
					//Rut
		            Cell celdaRut = fila.createCell(HashCodigoPosicion.get("RADB_RUTCLIENTE"));
		            celdaRut.setCellValue(clienteDeuda.DocumentoIndentidad);
					//Nombre
		            Cell celdaNombre = fila.createCell(HashCodigoPosicion.get("RADB_NOMBRECLIENTE"));
		            celdaNombre.setCellValue(clienteDeuda.NombreCliente);
		            
		            Cell celdaCodigoPortafolio = fila.createCell(HashCodigoPosicion.get("RADB_CODIGOPORTAFOLIO"));
		            celdaCodigoPortafolio.setCellValue(clienteDeuda.CodigoPortafolio);
			            
		            //MontoTotalPorCobrar
		            String MontoTotalPorCobrar = clienteDeuda.MontoPorCobrarTotal.toPlainString();
		            String DeudasVigentes = clienteDeuda.TramoA.toPlainString();
		            String DeudasTramoB = clienteDeuda.TramoB.toPlainString();
		            String DeudasTramoC = clienteDeuda.TramoC.toPlainString();
		            String DeudasTramoD = clienteDeuda.TramoD.toPlainString();

		            Cell celdaMontoPorCobrar = fila.createCell(HashCodigoPosicion.get("RADB_MONTOTOTALPORCOBRAR"));
		            celdaMontoPorCobrar.setCellValue(MontoTotalPorCobrar); 
		            //Deuda Vigentes
		            Cell celdaDeudasVigentes = fila.createCell(HashCodigoPosicion.get("RADB_DEUDASVIGENTES"));
		            celdaDeudasVigentes.setCellValue(DeudasVigentes);
		            //Deuda de 3 a 10 dias 
		            Cell celdaDeudaVenciadaDe3A10Dias = fila.createCell(HashCodigoPosicion.get("RADB_DEUDASVENCIDASDE3A10DIAS"));
		            celdaDeudaVenciadaDe3A10Dias.setCellValue(DeudasTramoB);
		            //Deuda de 11 a 30 dias 
		            Cell celdaDeudaVenciadaDe11A30Dias = fila.createCell(HashCodigoPosicion.get("RADB_DEUDASVENCIDASDE11A30DIAS"));
		            celdaDeudaVenciadaDe11A30Dias.setCellValue(DeudasTramoC);
		            //Deuda Mas de 30 dias 
		            Cell celdaDeudaVenciadaMasde30Dias = fila.createCell(HashCodigoPosicion.get("RADB_DEUDASVENCIDAMASDE30DIAS"));
		            celdaDeudaVenciadaMasde30Dias.setCellValue(DeudasTramoD);
		            //Observaciones 
		            Cell celdaObservaciones = fila.createCell(HashCodigoPosicion.get("RADB_OBSERVACIONES"));
		            celdaObservaciones.setCellValue(clienteDeuda.Observacion);
		            //Clasifiacion contable  
		            Cell celdaClasifiacionContable = fila.createCell(HashCodigoPosicion.get("RADB_CLASIFICACIONCONTABLE"));
		            celdaClasifiacionContable.setCellValue(clienteDeuda.ClasificacionContable);
		            //Relacionado
		            Cell celdaRelacionado = fila.createCell(HashCodigoPosicion.get("RADB_RELACIONADO"));
		            celdaRelacionado.setCellValue(clienteDeuda.Relacionado);
		            
		            cont ++;
		            rowCuerpo ++;
				}else {
					Row fila = pagina.createRow(rowCuerpo);
					Cell celdaSec = fila.createCell(HashCodigoPosicion.get("RADB_SECUENCIA"));
					celdaSec.setCellValue(cont);
					//Rut
		            Cell celdaRut = fila.createCell(HashCodigoPosicion.get("RADB_RUTCLIENTE"));
		            celdaRut.setCellValue(clienteDeuda.DocumentoIndentidad);
					//Nombre
		            Cell celdaNombre = fila.createCell(HashCodigoPosicion.get("RADB_NOMBRECLIENTE"));
		            celdaNombre.setCellValue(clienteDeuda.NombreCliente);
		            			            
		            //MontoTotalPorCobrar
		            String MontoTotalPorCobrar = clienteDeuda.MontoPorCobrarTotal.toPlainString();
		            String DeudasVigentes = clienteDeuda.TramoA.toPlainString();
		            String DeudasTramoB = clienteDeuda.TramoB.toPlainString();
		            String DeudasTramoC = clienteDeuda.TramoC.toPlainString();
		            String DeudasTramoD = clienteDeuda.TramoD.toPlainString();

		            Cell celdaMontoPorCobrar = fila.createCell(HashCodigoPosicion.get("RADB_MONTOTOTALPORCOBRAR"));
		            celdaMontoPorCobrar.setCellValue(MontoTotalPorCobrar); 
		            //Deuda Vigentes
		            Cell celdaDeudasVigentes = fila.createCell(HashCodigoPosicion.get("RADB_DEUDASVIGENTES"));
		            celdaDeudasVigentes.setCellValue(DeudasVigentes);
		            //Deuda de 3 a 10 dias 
		            Cell celdaDeudaVenciadaDe3A10Dias = fila.createCell(HashCodigoPosicion.get("RADB_DEUDASVENCIDASDE3A10DIAS"));
		            celdaDeudaVenciadaDe3A10Dias.setCellValue(DeudasTramoB);
		            //Deuda de 11 a 30 dias 
		            Cell celdaDeudaVenciadaDe11A30Dias = fila.createCell(HashCodigoPosicion.get("RADB_DEUDASVENCIDASDE11A30DIAS"));
		            celdaDeudaVenciadaDe11A30Dias.setCellValue(DeudasTramoC);
		            //Deuda Mas de 30 dias 
		            Cell celdaDeudaVenciadaMasde30Dias = fila.createCell(HashCodigoPosicion.get("RADB_DEUDASVENCIDAMASDE30DIAS"));
		            celdaDeudaVenciadaMasde30Dias.setCellValue(DeudasTramoD);
		            //Observaciones 
		            Cell celdaObservaciones = fila.createCell(HashCodigoPosicion.get("RADB_OBSERVACIONES"));
		            celdaObservaciones.setCellValue(clienteDeuda.Observacion);
		            //Clasifiacion contable  
		            Cell celdaClasifiacionContable = fila.createCell(HashCodigoPosicion.get("RADB_CLASIFICACIONCONTABLE"));
		            celdaClasifiacionContable.setCellValue(clienteDeuda.ClasificacionContable);
		            //Relacionado
		            Cell celdaRelacionado = fila.createCell(HashCodigoPosicion.get("RADB_RELACIONADO"));
		            celdaRelacionado.setCellValue(clienteDeuda.Relacionado);

		            cont ++;
		            rowCuerpo ++;
				}
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarReporteAntiguedadDeudaBolsaCTA.confeccionarCuerpoArchivo tiempo: " + (fin - inicio));
	}
	
	private List<SalidaReporteAntiguedadBolsa> obtenerDeudasCuentaInversion(Connection con,EntradaGenerarReporteAntiguedadDeudaBolsaCTA entrada, SalidaGenerarReporteAntiguedadDeudaBolsaCTA salida){
		logger.trace("INICIO::GenerarReporteAntiguedadDeudaBolsaCTA.obtenerDeudasCuentaInversion");
		long inicio = System.currentTimeMillis();
		
		ConsultaReporteAntiguedadBolsa consultaReporteAntiguedadBolsa =  new ConsultaReporteAntiguedadBolsa();
		EntradaConsultaReporteAntiguedadBolsa entradaConsultaReporteAntiguedadBolsa = new EntradaConsultaReporteAntiguedadBolsa();
		entradaConsultaReporteAntiguedadBolsa.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaConsultaReporteAntiguedadBolsa.p_IdUsuario = entrada.p_IdUsuario;
		entradaConsultaReporteAntiguedadBolsa.p_IdModulo = entrada.p_IdModulo;
		entradaConsultaReporteAntiguedadBolsa.p_IdEjecutivo =  0L;
		entradaConsultaReporteAntiguedadBolsa.p_FechaConsulta = entrada.p_FechaSolicitud;
		entradaConsultaReporteAntiguedadBolsa.p_IdMoneda = entrada.p_IdMoneda;
		entradaConsultaReporteAntiguedadBolsa.p_SeparaIntermediacion = entrada.p_SeparaIntermediacion;
		entradaConsultaReporteAntiguedadBolsa.p_AgrupaPortafolios = entrada.p_AgrupaPortafolios;
		entradaConsultaReporteAntiguedadBolsa.p_Consolidar= entrada.p_Consolidar;
		SalidaConsultaReporteAntiguedadBolsa salidaConsultaReporteAntiguedadBolsa = new SalidaConsultaReporteAntiguedadBolsa();
		
		try {
			salidaConsultaReporteAntiguedadBolsa = consultaReporteAntiguedadBolsa.logica(con, entradaConsultaReporteAntiguedadBolsa);
			if(salidaConsultaReporteAntiguedadBolsa.P_OUT_ERRORESCONTADOR.compareTo(0L) == 1  || !(salidaConsultaReporteAntiguedadBolsa.P_OUT_MENSAJES.isEmpty())) {
				salida.P_OUT_ERRORESCONTADOR ++;
				salida.P_OUT_MENSAJES = salidaConsultaReporteAntiguedadBolsa.P_OUT_MENSAJES;			
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		List<SalidaReporteAntiguedadBolsa> listadoClienteDeuda = new ArrayList<>();
		if(salidaConsultaReporteAntiguedadBolsa.P_OUT_ERRORESCONTADOR.compareTo(0L) == 0) {
			listadoClienteDeuda = salidaConsultaReporteAntiguedadBolsa.P_OUT_TABLA;
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::GenerarReporteAntiguedadDeudaBolsaCTA.obtenerDeudasCuentaInversion tiempo: " + (fin - inicio));
		return listadoClienteDeuda;
	}
	
	private HashMap<String, Integer> obtenerPosicionesParaCuerpoArchivo(HashMap<String, Object> hashCampos) {
		logger.trace("INICIO::GenerarReporteAntiguedadDeudaBolsaCTA.obtenerPosicionesParaCuerpoArchivo");
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
		logger.trace("FIN::GenerarReporteAntiguedadDeudaBolsaCTA.obtenerPosicionesParaCuerpoArchivo", fin - inicio);
		return HashCodigoPosicion;
	}
	

}
