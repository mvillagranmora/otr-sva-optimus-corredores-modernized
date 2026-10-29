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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.StringUtils;
import cl.bcs.utils.Utils;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import cl.bcs.cuentaInversion.ConsultaAntiguedadDeudasCuentaInversion.SalidaAntiguedadDeudaCuentaInversion;

public class GenerarReporteAntiguedadDeudaCTA implements JavaCall{
	
	private final static Logger logger = LogManager.getLogger(GenerarReporteAntiguedadDeudaCTA.class);

	private final String SQL_SELECT_CODIGOCORREDORA		= loadResourceToString("cl/bcs/cuentaInversion/sql/GenerarReporteAntiguedadDeudaCTA_SelectCodigoCorredora.sql");
	private final String SQL_SELECT_RAIZ_ARCHIVO 		= loadResourceToString("cl/bcs/cuentaInversion/sql/GenerarReporteAntiguedadDeudaCTA_SelectConfiguracionArchivo.sql");
	private final String SQL_ARCHIVO_FORMATO_ESTRUCTURA = loadResourceToString("cl/bcs/cuentaInversion/sql/GenerarReporteAntiguedadDeudaCTA_SelectArchivoFormatoConfiguracion.sql");
	// ----------------------------------------------------
	// CLASES DE ENTRADA
	// ----------------------------------------------------
	public static class GenerarReporteAntiguedadDeudaCTA_Entrada{
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;					
    	public String	p_FechaSolicitud;    	
    	public String 	p_IdMoneda;
    	public String 	p_SeparaIntermediacion;
    	public String 	p_AgrupaPortafolios;
    	public String	p_Consolidar;
    	public String 	p_NombreInternoArchivo;
    	
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	
	// ----------------------------------------------------
	// CLASES DE PROCESO
	// ----------------------------------------------------
	public static class Corredora{
		public String IdEmpresa;
		public String CodigoCorredora;
	}
	
	public static class RaizArchivo extends BcsPojo {
		public String RutaArchivo;
		public String RutaModulo;
		public RaizArchivo() {
			this.RutaModulo = "";
			this.RutaArchivo = "";
		}
	}
	
	public static class CamposArchivo{
		public String CodigoCampo;
		public String CampoNombreCompleto;
		public int Posicion;
	}
	
	public static class ClienteDeuda{
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

	// ----------------------------------------------------
	// CLASES DE SALIDA
	// ----------------------------------------------------
	public static class GenerarReporteAntiguedadDeudaCTA_Salida  {

		public List<MensajeSalida>			P_OUT_MENSAJES;
		public Long							P_OUT_ERRORESCONTADOR;
		public String 						P_OUT_RUTAARCHIVO;
		public String 						P_OUT_CODIGOCORREDOR;
		
		public GenerarReporteAntiguedadDeudaCTA_Salida(){	
			this.P_OUT_MENSAJES  		= new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR 	= 0L;	
			this.P_OUT_RUTAARCHIVO 		= "";
			this.P_OUT_CODIGOCORREDOR	= "";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	// ----------------------------------------------------
	// CALL
	// ----------------------------------------------------
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();	
		GenerarReporteAntiguedadDeudaCTA_Entrada in = gson.fromJson(jin.toString(),GenerarReporteAntiguedadDeudaCTA_Entrada.class);
		GenerarReporteAntiguedadDeudaCTA_Salida out = logica(con,in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES); 
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}

	public  GenerarReporteAntiguedadDeudaCTA_Salida logica(Connection con, GenerarReporteAntiguedadDeudaCTA_Entrada in) throws Exception {
		logger.info("INICIO::GenerarReporteAntiguedadDeudaCTA");
		long inicio = System.currentTimeMillis();
		GenerarReporteAntiguedadDeudaCTA_Salida out = new GenerarReporteAntiguedadDeudaCTA_Salida();
		SQL sql = new SQL();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		RaizArchivo raizArchivo = obtenerRaizArchivo(con, in.p_IdEmpresa);
		if (StringUtils.isNullOrBlank(raizArchivo.RutaArchivo) || StringUtils.isNullOrBlank(raizArchivo.RutaModulo)) {
			out.P_OUT_ERRORESCONTADOR++;
			out.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR,
					"FALTA_CONFIGURACION_RUTA_ARCHIVO", new HashMap<>(), con, in.p_IdEmpresa));
			return out;
		}
		
		String codigoCorredora = obtenerCodigoCorredora(con, sql, in.p_IdEmpresa);
		if(codigoCorredora.trim().length()==0) {
			out.P_OUT_ERRORESCONTADOR++;
			out.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR,
					"CODIGO_CORREDOR_NO_EXISTE", new HashMap<>(), con, in.p_IdEmpresa));
			return out;
		}
		
		String nombreArchivo = in.p_NombreInternoArchivo+".xlsx";
		String rutaReportefinal = creacionReportefinal(con, sql, raizArchivo, nombreArchivo, codigoCorredora,in, out);
		
		if(rutaReportefinal.trim().length() == 0) {
			out.P_OUT_ERRORESCONTADOR++;
			out.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR,
					"UI_ERROR_AL_GENERAR_REPORTE", new HashMap<>(), con, in.p_IdEmpresa));
			return out;
		}
		
		out.P_OUT_RUTAARCHIVO = rutaReportefinal;
		out.P_OUT_CODIGOCORREDOR = "CODIGOCORREDOR_"+codigoCorredora;

		long fin = System.currentTimeMillis();
		logger.info("FIN::GenerarReporteAntiguedadDeudaCTA",  fin - inicio);
		return out;
	}
	
	private RaizArchivo obtenerRaizArchivo(Connection conn, Long idEmpresa) throws SQLException {
		logger.debug("INICIO::GenerarReporteAntiguedadDeudaCTA.obtenerRaizArchivo");
		long inicio = System.currentTimeMillis();
		HashMap<String, Object> params = new HashMap<>(2);
		params.put("IdEmpresa", idEmpresa);
		RaizArchivo raiz = new SQL().query(conn, SQL_SELECT_RAIZ_ARCHIVO, params, RaizArchivo.class).stream()
																									.findFirst().orElse(new RaizArchivo());
		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarReporteAntiguedadDeudaCTA.obtenerRaizArchivo", fin - inicio);
		return raiz;
	}

	private String obtenerCodigoCorredora(Connection con, SQL sql, Long IdEmpresa) throws Exception{
		logger.info("INICIO::GenerarReporteAntiguedadDeudaCTA.obtenerCodigoCorredora");
		long inicio = System.currentTimeMillis();
		String codigoCorredora = "";
		HashMap<String, Object> params = new HashMap<>();
		params.put("IdEmpresa", IdEmpresa);
		List<Corredora> codigoCorresdora = sql.query(con, SQL_SELECT_CODIGOCORREDORA, params, Corredora.class);
		// Solo debemos de obtener 1 solo codigo de corredora.
		if(codigoCorresdora.size() == 1) {
			Corredora corred = codigoCorresdora.get(0);
			return corred.CodigoCorredora;
		}
		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarReporteAntiguedadDeudaCTA.obtenerCodigoCorredora", fin - inicio);
		return codigoCorredora;
	}
	
	private String creacionReportefinal(Connection con, SQL sql, RaizArchivo rutaRaiz, String nombreArchivo, String codigoCorredora, GenerarReporteAntiguedadDeudaCTA_Entrada entrada, GenerarReporteAntiguedadDeudaCTA_Salida out) throws Exception {	
		logger.info("INICIO::GenerarReporteAntiguedadDeudaCTA.creacionReportefinal");
		long inicio = System.currentTimeMillis();
		String rutaSalida = "";
        File archivo = new File(nombreArchivo);
        Workbook workbook = new XSSFWorkbook();
        Sheet pagina = workbook.createSheet(obtenerNombreHoja(codigoCorredora, entrada));
        
        HashMap<String, Object> hashCampos = obtenerCabeceraYPosicionesTituloArchivo(con, sql, entrada.p_IdEmpresa, entrada.p_Consolidar);
        if(hashCampos.isEmpty()) {
        	out.P_OUT_ERRORESCONTADOR ++;
        	out.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR,
					"FALTA_CONFIGURACION_ARCHIVO_FORMATO", new HashMap<>(), con, entrada.p_IdEmpresa));
        	return rutaSalida;
        }
        
        confeccionarCabeceraArchivo(workbook, pagina, hashCampos, entrada.p_Consolidar);
        
        confeccionarCuerpoArchivo(con, entrada, workbook, pagina, hashCampos, out);

        try {
        	String rutaCompleta = rutaRaiz.RutaArchivo+archivo;
            FileOutputStream salida = new FileOutputStream(rutaCompleta);
            workbook.write(salida);
            workbook.close();
        	rutaSalida = rutaRaiz.RutaModulo+archivo;
        } catch (FileNotFoundException ex) {
        	ex.printStackTrace();
        } catch (IOException exIO) {
        	exIO.printStackTrace();
        }
        
        long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarReporteAntiguedadDeudaCTA.creacionReportefinal", fin - inicio);
		
        return rutaSalida;
    }

	private void confeccionarCabeceraArchivo(Workbook workbook, Sheet pagina, HashMap<String, Object> hashCampos, String Consolidado) {
		logger.info("INICIO::GenerarReporteAntiguedadDeudaCTA.confeccionarCabeceraArchivo");
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
		logger.debug("FIN::GenerarReporteAntiguedadDeudaCTA.confeccionarCabeceraArchivo", fin - inicio);
	}
	
	private HashMap<String, Object> obtenerCabeceraYPosicionesTituloArchivo(Connection con, SQL sql, Long IdEmpresa, String Consolidado) throws Exception {
		logger.info("INICIO::GenerarReporteAntiguedadDeudaCTA.obtenerCabeceraYPosicionesTituloArchivo");
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
		logger.debug("FIN::GenerarReporteAntiguedadDeudaCTA.obtenerCabeceraYPosicionesTituloArchivo", fin - inicio);
		return hashCampos;
	}
	
	private String obtenerNombreHoja(String codigoCorredora, GenerarReporteAntiguedadDeudaCTA_Entrada entrada) {
		logger.info("INICIO::GenerarReporteAntiguedadDeudaCTA.obtenerNombreHoja");
		long inicio = System.currentTimeMillis();
		String [] fechaArray = entrada.p_FechaSolicitud.split("-");
		String fecha = fechaArray[2]+fechaArray[1]+fechaArray[0];
		String nombreHoja = codigoCorredora+"_"+fecha+"_cuentas_por_cobrar";
		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarReporteAntiguedadDeudaCTA.obtenerNombreHoja", fin - inicio);
		return nombreHoja;
	}
	
	private void confeccionarCuerpoArchivo(Connection con, GenerarReporteAntiguedadDeudaCTA_Entrada entrada, Workbook workbook, Sheet pagina, HashMap<String, Object> hashCampos,
											GenerarReporteAntiguedadDeudaCTA_Salida out) throws Exception {
		logger.info("INICIO::GenerarReporteAntiguedadDeudaCTA.confeccionarCuerpoArchivo");
		long inicio = System.currentTimeMillis();
		int rowCuerpo = 1;
		int secuencia = 0;
		
		List<SalidaAntiguedadDeudaCuentaInversion> datosClienteDeuda = obtenerDeudasCuentaInversion(con, entrada, out);
		if((out.P_OUT_ERRORESCONTADOR.compareTo(0L) == 0) && !datosClienteDeuda.isEmpty()) {
			HashMap<String, Integer> HashCodigoPosicion = obtenerPosicionesParaCuerpoArchivo(hashCampos);
			//se trabaja con las codigos de la BD para asignar posicion al archivo
			Long cont = 1L;
			for (SalidaAntiguedadDeudaCuentaInversion clienteDeuda : datosClienteDeuda) {
				
				if(entrada.p_Consolidar.equalsIgnoreCase("N")) {
					Row fila = pagina.createRow(rowCuerpo);
					Cell celdaSec = fila.createCell(HashCodigoPosicion.get("SECUENCIA"));
					celdaSec.setCellValue(cont);
					//Rut
		            Cell celdaRut = fila.createCell(HashCodigoPosicion.get("RUTCLIENTE"));
		            celdaRut.setCellValue(clienteDeuda.DocumentoIndentidad);
					//Nombre
		            Cell celdaNombre = fila.createCell(HashCodigoPosicion.get("NOMBRECLIENTE"));
		            celdaNombre.setCellValue(clienteDeuda.NombreCliente);
		            
		            Cell celdaCodigoPortafolio = fila.createCell(HashCodigoPosicion.get("CODIGOPORTAFOLIO"));
		            celdaCodigoPortafolio.setCellValue(clienteDeuda.CodigoPortafolio);
			            
		            //MontoTotalPorCobrar
		            String MontoTotalPorCobrar = clienteDeuda.MontoPorCobrarTotal.toPlainString();
		            String DeudasVigentes = clienteDeuda.TramoA.toPlainString();
		            String DeudasTramoB = clienteDeuda.TramoB.toPlainString();
		            String DeudasTramoC = clienteDeuda.TramoC.toPlainString();
		            String DeudasTramoD = clienteDeuda.TramoD.toPlainString();

		            Cell celdaMontoPorCobrar = fila.createCell(HashCodigoPosicion.get("MONTOTOTALPORCOBRAR"));
		            celdaMontoPorCobrar.setCellValue(MontoTotalPorCobrar); 
		            //Deuda Vigentes
		            Cell celdaDeudasVigentes = fila.createCell(HashCodigoPosicion.get("DEUDASVIGENTES"));
		            celdaDeudasVigentes.setCellValue(DeudasVigentes);
		            //Deuda de 3 a 10 dias 
		            Cell celdaDeudaVenciadaDe3A10Dias = fila.createCell(HashCodigoPosicion.get("DEUDASVENCIDASDE3A10DIAS"));
		            celdaDeudaVenciadaDe3A10Dias.setCellValue(DeudasTramoB);
		            //Deuda de 11 a 30 dias 
		            Cell celdaDeudaVenciadaDe11A30Dias = fila.createCell(HashCodigoPosicion.get("DEUDASVENCIDASDE11A30DIAS"));
		            celdaDeudaVenciadaDe11A30Dias.setCellValue(DeudasTramoC);
		            //Deuda Mas de 30 dias 
		            Cell celdaDeudaVenciadaMasde30Dias = fila.createCell(HashCodigoPosicion.get("DEUDASVENCIDAMASDE30DIAS"));
		            celdaDeudaVenciadaMasde30Dias.setCellValue(DeudasTramoD);
		            //Observaciones 
		            Cell celdaObservaciones = fila.createCell(HashCodigoPosicion.get("OBSERVACIONES"));
		            celdaObservaciones.setCellValue(clienteDeuda.Observacion);
		            //Relacionado
		            Cell celdaRelacionado = fila.createCell(HashCodigoPosicion.get("RELACIONADO"));
		            celdaRelacionado.setCellValue(clienteDeuda.Relacionado);
		            //Fecha Deuda
		            Cell celdaFechaDeuda = fila.createCell(HashCodigoPosicion.get("FECHADEUDA"));
		            clienteDeuda.FechaDeuda = clienteDeuda.MontoPorCobrarTotal.compareTo(BigDecimal.ZERO) == 0 ? null : clienteDeuda.FechaDeuda;
		            celdaFechaDeuda.setCellValue(clienteDeuda.FechaDeuda);
		            
		            cont ++;
		            rowCuerpo ++;
				}else {
					Row fila = pagina.createRow(rowCuerpo);
					Cell celdaSec = fila.createCell(HashCodigoPosicion.get("SECUENCIA"));
					celdaSec.setCellValue(cont);
					//Rut
		            Cell celdaRut = fila.createCell(HashCodigoPosicion.get("RUTCLIENTE"));
		            celdaRut.setCellValue(clienteDeuda.DocumentoIndentidad);
					//Nombre
		            Cell celdaNombre = fila.createCell(HashCodigoPosicion.get("NOMBRECLIENTE"));
		            celdaNombre.setCellValue(clienteDeuda.NombreCliente);
		            			            
		            //MontoTotalPorCobrar
		            String MontoTotalPorCobrar = clienteDeuda.MontoPorCobrarTotal.toPlainString();
		            String DeudasVigentes = clienteDeuda.TramoA.toPlainString();
		            String DeudasTramoB = clienteDeuda.TramoB.toPlainString();
		            String DeudasTramoC = clienteDeuda.TramoC.toPlainString();
		            String DeudasTramoD = clienteDeuda.TramoD.toPlainString();

		            Cell celdaMontoPorCobrar = fila.createCell(HashCodigoPosicion.get("MONTOTOTALPORCOBRAR"));
		            celdaMontoPorCobrar.setCellValue(MontoTotalPorCobrar); 
		            //Deuda Vigentes
		            Cell celdaDeudasVigentes = fila.createCell(HashCodigoPosicion.get("DEUDASVIGENTES"));
		            celdaDeudasVigentes.setCellValue(DeudasVigentes);
		            //Deuda de 3 a 10 dias 
		            Cell celdaDeudaVenciadaDe3A10Dias = fila.createCell(HashCodigoPosicion.get("DEUDASVENCIDASDE3A10DIAS"));
		            celdaDeudaVenciadaDe3A10Dias.setCellValue(DeudasTramoB);
		            //Deuda de 11 a 30 dias 
		            Cell celdaDeudaVenciadaDe11A30Dias = fila.createCell(HashCodigoPosicion.get("DEUDASVENCIDASDE11A30DIAS"));
		            celdaDeudaVenciadaDe11A30Dias.setCellValue(DeudasTramoC);
		            //Deuda Mas de 30 dias 
		            Cell celdaDeudaVenciadaMasde30Dias = fila.createCell(HashCodigoPosicion.get("DEUDASVENCIDAMASDE30DIAS"));
		            celdaDeudaVenciadaMasde30Dias.setCellValue(DeudasTramoD);
		            //Observaciones 
		            Cell celdaObservaciones = fila.createCell(HashCodigoPosicion.get("OBSERVACIONES"));
		            celdaObservaciones.setCellValue(clienteDeuda.Observacion);

		            //Relacionado
		            Cell celdaRelacionado = fila.createCell(HashCodigoPosicion.get("RELACIONADO"));
		            celdaRelacionado.setCellValue(clienteDeuda.Relacionado);
		            //Fecha Deuda
		            Cell celdaFechaDeuda = fila.createCell(HashCodigoPosicion.get("FECHADEUDA"));
		            clienteDeuda.FechaDeuda = clienteDeuda.MontoPorCobrarTotal.compareTo(BigDecimal.ZERO) == 0 ? null : clienteDeuda.FechaDeuda;
		            celdaFechaDeuda.setCellValue(clienteDeuda.FechaDeuda);
		            
		            cont ++;
		            rowCuerpo ++;
				}

	        }
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarReporteAntiguedadDeudaCTA.confeccionarCuerpoArchivo", fin - inicio);
	}
	
	private HashMap<String, Integer> obtenerPosicionesParaCuerpoArchivo(HashMap<String, Object> hashCampos) {
		logger.info("INICIO::GenerarReporteAntiguedadDeudaCTA.obtenerPosicionesParaCuerpoArchivo");
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
		logger.debug("FIN::GenerarReporteAntiguedadDeudaCTA.obtenerPosicionesParaCuerpoArchivo", fin - inicio);
		return HashCodigoPosicion;
	}
	
	private List<SalidaAntiguedadDeudaCuentaInversion> obtenerDeudasCuentaInversion(Connection con, GenerarReporteAntiguedadDeudaCTA_Entrada entrada, GenerarReporteAntiguedadDeudaCTA_Salida out) throws Exception {
		logger.info("INICIO::GenerarReporteAntiguedadDeudaCTA.obtenerDeudasCuentaInversion");
		long inicio = System.currentTimeMillis();
		ConsultaAntiguedadDeudasCuentaInversion consultaAntiguedadDeudaCuentaInversion = new ConsultaAntiguedadDeudasCuentaInversion();
		ConsultaAntiguedadDeudasCuentaInversion.EntradaConsultaAntiguedadDeudasCuentaInversion entradaConsultaAntiguedadCuentaInversion = new ConsultaAntiguedadDeudasCuentaInversion.EntradaConsultaAntiguedadDeudasCuentaInversion();
		entradaConsultaAntiguedadCuentaInversion.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaConsultaAntiguedadCuentaInversion.p_IdUsuario = entrada.p_IdUsuario;
		entradaConsultaAntiguedadCuentaInversion.p_IdModulo = entrada.p_IdModulo;
		entradaConsultaAntiguedadCuentaInversion.p_IdEjecutivo =  0L;
		entradaConsultaAntiguedadCuentaInversion.p_FechaConsulta = entrada.p_FechaSolicitud;
		entradaConsultaAntiguedadCuentaInversion.p_IdMoneda = entrada.p_IdMoneda;
		entradaConsultaAntiguedadCuentaInversion.p_SeparaIntermediacion = entrada.p_SeparaIntermediacion;
		entradaConsultaAntiguedadCuentaInversion.p_AgrupaPortafolios = entrada.p_AgrupaPortafolios;
		entradaConsultaAntiguedadCuentaInversion.p_Consolidar= entrada.p_Consolidar;
		ConsultaAntiguedadDeudasCuentaInversion.SalidaConsultaAntiguedadDeudasCuentaInversion salidaConsultaAntiguedadDeudasCuentaInversion = new ConsultaAntiguedadDeudasCuentaInversion.SalidaConsultaAntiguedadDeudasCuentaInversion();
		try {
			salidaConsultaAntiguedadDeudasCuentaInversion = consultaAntiguedadDeudaCuentaInversion.logica(con, entradaConsultaAntiguedadCuentaInversion);
			if(salidaConsultaAntiguedadDeudasCuentaInversion.P_OUT_ERRORESCONTADOR.compareTo(0L) == 1 
				|| !(salidaConsultaAntiguedadDeudasCuentaInversion.P_OUT_MENSAJES.isEmpty())) {
				out.P_OUT_ERRORESCONTADOR ++;
				out.P_OUT_MENSAJES = salidaConsultaAntiguedadDeudasCuentaInversion.P_OUT_MENSAJES;			
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		List<SalidaAntiguedadDeudaCuentaInversion> listadoClienteDeuda = new ArrayList<>();
		if(salidaConsultaAntiguedadDeudasCuentaInversion.P_OUT_ERRORESCONTADOR.compareTo(0L) == 0) {
			listadoClienteDeuda = salidaConsultaAntiguedadDeudasCuentaInversion.P_OUT_TABLA;
		}
		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarReporteAntiguedadDeudaCTA.obtenerDeudasCuentaInversion", fin - inicio);
		return listadoClienteDeuda;
	}

}
