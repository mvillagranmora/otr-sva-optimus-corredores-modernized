package cl.bcs.ordenes;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.facturacion.GenerarInformeComprobanteComisionesMensualesUtils.EstiloCelda;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class GenerarInformeEstadistica implements JavaCall{
	
	
	private final String SQL_SELECT_OBTENER_OPERACIONES = loadResourceToString("cl/bcs/ordenes/sql/GenerarInformeEstadisticas_SelectDetalleOperaciones.sql");
	private final String SQL_CREATE_TEMP_TABLE_OPERACIONES = "CREATE LOCAL TEMPORARY COLUMN TABLE #Salida_Libro_Operaciones(\"TipoOperacion\" NVARCHAR(100), \"AccionOperacion\" NVARCHAR(30), \"FechaMovimiento\" DATETIME, \"MontoCompra\" DECIMAL(26,8), \"MontoVenta\"	DECIMAL(26,8), \"IdTipoOperacion\" NVARCHAR(30), \"AreaCliente\" NVARCHAR(41));";
	private final String SQL_INSERT_TEMP_TABLE_OPERACIONES = "INSERT INTO #Salida_Libro_Operaciones VALUES(:TipoOperacion,:AccionOperacion,:FechaMovimiento,:MontoCompra,:MontoVenta,:IdTipoOperacion, :AreaCliente);";
	private final String SQL_DROP_TEMP_TABLE_OPERACIONES = "DROP TABLE #Salida_Libro_Operaciones;";
	private final String SQL_SELECT_INFORMACION_ORDENES = loadResourceToString("cl/bcs/ordenes/sql/GenerarInformeEstadisticas_SelectResumenOrden.sql");
	private final String SQL_SELECT_INFORMACION_ORDENES_POR_AREA_CLIENTE = loadResourceToString("cl/bcs/ordenes/sql/GenerarInformeEstadisticas_SelectResumenOrdenPorAreaCliente.sql");
		
	private final static Logger logger = LogManager.getLogger(GenerarInformeEstadistica.class);
	
	private static Integer columnaActual = 0;
	private static Integer filaActual	  = 0;
	

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		GenerarInformeEstadistica_Entrada in = gson.fromJson(jin.toString(), GenerarInformeEstadistica_Entrada.class);
		GenerarInformeEstadistica_Salida out = logica(conn, in);
		out.P_OUT_ERRORESCONTADOR = 0L;
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;	
	}
	
	public static class GenerarInformeEstadistica_Entrada{
		//Parametros Fijos
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String	p_Ruta;
		
		//Parametros de ventana		
		public String 	p_FechaOperacionOrdenI;
		public String 	p_FechaOperacionOrdenF;	
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
		
	public static class GenerarInformeEstadistica_Salida{
		public List<String> 		P_OUT_ARCHIVOS;
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long                 P_OUT_ERRORESCONTADOR;

		public String 				P_OUT_NOMBRE_ARCHIVO_LOG;
		public String 	 			P_OUT_RUTA_ARCHIVO_LOG;
		public String				P_OUT_NOMBRE_ARCHIVO;
		public String				P_OUT_RUTA_ARCHIVO; 
		public String 				P_OUT_COMENTARIO;
		
		public GenerarInformeEstadistica_Salida() {
			P_OUT_ERRORESCONTADOR = 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DetalleOrdenes{
		
		public String 		NombreTipoOperacion;
		public String 		AccionTipoOperacion;
		public Long 		CantidadOperaciones;
		public BigDecimal 	MontoOperacion;
		public Timestamp 	MaximaFecha;
		public Timestamp 	MinimaFecha;
		public String 		IdEstadoFactura;
		public String 		IdTipoInstrumento;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DetalleOrdenPorAreaCliente{
		
		public String 		AreaCliente;
		public Long 		CantidadOperaciones;
		public BigDecimal 	MontoOperacion;
		public String 		NombreTipoOperacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	 public static class DetalleOperaciones {
		 	public String		AreaCliente;
			public String 		TipoOperacion;
			public String 		AccionOperacion;
			public Timestamp 	FechaMovimiento;
			public BigDecimal 	MontoCompra;
			public BigDecimal 	MontoVenta;			
			public String 		IdTipoOperacion;
			public String 		IdEstadoFactura;
			public String 		IdTipoInstrumento;

			public String toString() {
				return Utils.printJson(this, false);
			}
		}
	
	public GenerarInformeEstadistica_Salida logica(Connection conn, GenerarInformeEstadistica_Entrada entrada) throws Exception{
		
		logger.info("Inicio de servicio GenerarInformeEstadistica");
		String mensajeGeneracionArchivo = "ARCHIVO_GENERADO_EXITOSAMENTE";
		GenerarInformeEstadistica_Salida salida = new GenerarInformeEstadistica_Salida();
		SQL _sql = new SQL();
				
		HashMap<String,Object> params =  new HashMap<>();
		params.put("p_IdEmpresa", entrada.p_IdEmpresa);
		params.put("p_FechaOperacionOrdenI", entrada.p_FechaOperacionOrdenI);
		params.put("p_FechaOperacionOrdenF", entrada.p_FechaOperacionOrdenF);
		
		HashMap<String,Class> paramOut = new HashMap<>();
		paramOut.put("p_out_LibroOperaciones", DetalleOperaciones.class);
		
		logger.debug("INICIO - Procesar Operaciones");
		HashMap<String, Object> resultOperaciones = _sql.call(conn, SQL_SELECT_OBTENER_OPERACIONES, params, paramOut);
		List<DetalleOperaciones> listaResultOperacionesFilter = (List<DetalleOperaciones>) resultOperaciones.get("p_out_LibroOperaciones");
		
		
		List<DetalleOperaciones> listDetalleOperaciones = listaResultOperacionesFilter.stream()
																						.filter(orden -> !(orden.IdTipoInstrumento.equals("SPT") && !orden.IdEstadoFactura.equals("VIG")))
																						.collect(Collectors.toList());
	
		SQL.exec(conn, SQL_CREATE_TEMP_TABLE_OPERACIONES);
		logger.debug("Inicio SQL_INSERT_TEMP_TABLE");
		_sql.batch(conn, SQL_INSERT_TEMP_TABLE_OPERACIONES, listDetalleOperaciones, DetalleOperaciones.class);
		
		params =  new HashMap<>();
		List<DetalleOrdenes> ordenes = _sql.query(conn, SQL_SELECT_INFORMACION_ORDENES, params, DetalleOrdenes.class);
		
		// Configuracion para salida de archivo 
		String fechaSistema = Utils.getFechaHoraSistema(conn, "BSCL", entrada.p_IdEmpresa);
		Date fechaActual = Fecha.toDate(fechaSistema);
		Date pFecha = new SimpleDateFormat("yyyy-MM-dd").parse(fechaActual.toString());	
		SimpleDateFormat sdfyyyyMMdd = new SimpleDateFormat("yyyyMMdd"); 
		String fechayyyymmdd = sdfyyyyMMdd.format(pFecha);		
		
		String nombreArchivo = "InformeEstadisticas";
				
		salida.P_OUT_NOMBRE_ARCHIVO = nombreArchivo + fechayyyymmdd + ".xls";
		salida.P_OUT_NOMBRE_ARCHIVO_LOG = nombreArchivo + fechayyyymmdd + "DET.log";		
		salida.P_OUT_RUTA_ARCHIVO = entrada.p_Ruta + "/" + salida.P_OUT_NOMBRE_ARCHIVO;
		salida.P_OUT_RUTA_ARCHIVO_LOG = entrada.p_Ruta + "/" + salida.P_OUT_NOMBRE_ARCHIVO_LOG;
		
		if (!generarArchivoInformeEstadisticas(conn, entrada, salida.P_OUT_RUTA_ARCHIVO, ordenes)) {
			mensajeGeneracionArchivo = "ERROR_ARCHIVO_GENERADO";
		}
	
		salida.P_OUT_COMENTARIO =  Diccionario.getMensaje(conn, entrada.p_IdEmpresa, mensajeGeneracionArchivo).msg();
		File archivoLog = new File(salida.P_OUT_RUTA_ARCHIVO_LOG);
		
		try(BufferedWriter bwAchivoLog = new BufferedWriter(new FileWriter(archivoLog))){
			bwAchivoLog.write(salida.P_OUT_COMENTARIO);
			bwAchivoLog.newLine();
		}
		
		SQL.exec(conn, SQL_DROP_TEMP_TABLE_OPERACIONES);
		logger.debug("FIN - Select Obtener informacion ordenes"); 
		logger.info("Fin de servicio ConsultarOrdenes");
		return salida;
	}
	
	
	public boolean generarArchivoInformeEstadisticas(Connection conn, GenerarInformeEstadistica_Entrada entrada, String pOutNombreArchivo, List<DetalleOrdenes> ordenes) 
			throws FileNotFoundException, IOException, EncryptedDocumentException, InvalidFormatException {
		try {
					    
		    try(FileOutputStream outputStream = new FileOutputStream(pOutNombreArchivo)){
		    		
    			Workbook archivoExcel = new HSSFWorkbook();
    			Sheet hoja = archivoExcel.createSheet();
    			
    			Date fechaDesde = Fecha.toDate(entrada.p_FechaOperacionOrdenI.substring(0,10));
    			Date fechaHasta = Fecha.toDate(entrada.p_FechaOperacionOrdenF.substring(0,10));
    			DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    			
    			GenerarInformeEstadisticaUtils.fijarEstilos(archivoExcel);
    			GenerarInformeEstadisticaUtils.fijarAnchoColumnas(hoja);
    			
    			Row fila = hoja.createRow(filaActual);
    			Cell celda = fila.createCell(columnaActual);
    			celda.setCellValue("INFORME ESTADISTICA");
    			celda.setCellStyle(GenerarInformeEstadisticaUtils.estiloTitulo);
    			hoja.addMergedRegion(new CellRangeAddress(0,0,0,5));
    			
    			filaActual = 1;
    			fila = hoja.createRow(filaActual);
    			celda = fila.createCell(columnaActual);
    			celda.setCellValue("DESDE: " + dateFormat.format(fechaDesde) + " HASTA EL: " + dateFormat.format(fechaHasta));
    			celda.setCellStyle(GenerarInformeEstadisticaUtils.estiloTitulo);
    			hoja.addMergedRegion(new CellRangeAddress(1, 1, 0, 5));
    			    			
    			generarEncabezado(hoja);
			
				if (!ordenes.isEmpty()) generarDetalleOrden(conn, hoja, ordenes);
					    				
				archivoExcel.write(outputStream);			    			
		    		
		    	}
		    return true;   
		
		}catch(Exception e) {
			logger.error("Error en la creaion de Informe Estadisticas");
			logger.error(e.getMessage());
			return false;
		}
	}
	

	private void generarEncabezado(Sheet hoja) {
		
		logger.trace("INICIO::GenerarInformeEstadistica.generarEncabezado");
		Long inicioServicio = System.currentTimeMillis();
		
		filaActual = 4;
		columnaActual = 0;
		Row fila = hoja.createRow(filaActual);
		
		Cell celda = fila.createCell(columnaActual);
		celda.setCellValue("Tipo Operacion");
		celda.setCellStyle(GenerarInformeEstadisticaUtils.estiloEncabezado);
	
		celda = fila.createCell(++columnaActual);
		celda.setCellValue("Operacion");
		celda.setCellStyle(GenerarInformeEstadisticaUtils.estiloEncabezado);
		
		celda = fila.createCell(++columnaActual);
		celda.setCellValue("Cantidad de Operaciones");				
		celda.setCellStyle(GenerarInformeEstadisticaUtils.estiloEncabezado);
		
		celda = fila.createCell(++columnaActual);
		celda.setCellValue("Monto");
		celda.setCellStyle(GenerarInformeEstadisticaUtils.estiloEncabezado);
		
		celda = fila.createCell(++columnaActual);
		celda.setCellValue("Fecha Mayor");
		celda.setCellStyle(GenerarInformeEstadisticaUtils.estiloEncabezado);
		
		celda = fila.createCell(++columnaActual);
		celda.setCellValue("Fecha Menor");
		celda.setCellStyle(GenerarInformeEstadisticaUtils.estiloEncabezado);
		
		Long finServicio = System.currentTimeMillis();
		logger.trace("FIN::GenerarInformeEstadistica.generarEncabezado", finServicio - inicioServicio);
		
	}
	
	private void generarDetalleOrden(Connection conn, Sheet hoja, List<DetalleOrdenes> ordenes) throws Exception{
		
		logger.trace("INICIO::GenerarInformeEstadistica.generarDetalleOrden");
		Long inicioServicio = System.currentTimeMillis();
		
		filaActual = 5;
		Row fila = hoja.createRow(filaActual);
		Cell celda = fila.createCell(columnaActual);
		
		List<DetalleOrdenPorAreaCliente> ordenesAgrupadas = new ArrayList<DetalleOrdenPorAreaCliente>();
		Map<String, List<DetalleOrdenPorAreaCliente>> OrdenesAgrupadasPorAreaCliente = obtenerOrdenesAgrupadasPorAreaCliente(conn);
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		
		for(DetalleOrdenes detalleOrdenes : ordenes) {
			
			columnaActual = 0;
			fila = hoja.createRow(filaActual);
			
			celda = fila.createCell(columnaActual);
			celda.setCellValue(detalleOrdenes.NombreTipoOperacion);
			celda.setCellStyle(GenerarInformeEstadisticaUtils.estiloDetallePrincipal);
			    				
			celda = fila.createCell(++columnaActual);
			celda.setCellValue(detalleOrdenes.AccionTipoOperacion);
			celda.setCellStyle(GenerarInformeEstadisticaUtils.estiloDetallePrincipal);
			
			celda = fila.createCell(++columnaActual);
			celda.setCellType(Cell.CELL_TYPE_NUMERIC);
			celda.setCellValue(detalleOrdenes.CantidadOperaciones);
			celda.setCellStyle(GenerarInformeEstadisticaUtils.estiloDetallePrincipal);
			
			celda = fila.createCell(++columnaActual);
			celda.setCellType(Cell.CELL_TYPE_NUMERIC);
			celda.setCellValue(detalleOrdenes.MontoOperacion.doubleValue());
			celda.setCellStyle(GenerarInformeEstadisticaUtils.estiloMontoTotal);
			
			celda = fila.createCell(++columnaActual);
			celda.setCellValue(dateFormat.format(detalleOrdenes.MaximaFecha));
			celda.setCellStyle(GenerarInformeEstadisticaUtils.estiloDetallePrincipal);
			
			celda = fila.createCell(++columnaActual);
			celda.setCellValue(dateFormat.format(detalleOrdenes.MinimaFecha));
			celda.setCellStyle(GenerarInformeEstadisticaUtils.estiloDetallePrincipal);
			
			ordenesAgrupadas = OrdenesAgrupadasPorAreaCliente.get(detalleOrdenes.NombreTipoOperacion);
			generarDesgloseOrden(hoja, ordenesAgrupadas);
			
			fila = hoja.createRow(++filaActual);
		}
		
		Long finServicio = System.currentTimeMillis();
		logger.trace("FIN::GenerarInformeEstadistica.generarDetalleOrden", finServicio - inicioServicio);
	}
	
	private Map<String, List<DetalleOrdenPorAreaCliente>> obtenerOrdenesAgrupadasPorAreaCliente(Connection conn) throws Exception {
		
		logger.info("INICIO::GenerarInformeEstadisticas.obtenerOrdenesAgrupadasPorAreaCliente");
		Long inicioServicio = System.currentTimeMillis();
		
		SQL _sql = new SQL();
		HashMap<String,Object> params =  new HashMap<>();
		
		List<DetalleOrdenPorAreaCliente> ordenesPorAreaCliente = _sql.query(conn, SQL_SELECT_INFORMACION_ORDENES_POR_AREA_CLIENTE, params, DetalleOrdenPorAreaCliente.class);
		
		Map<String, List<DetalleOrdenPorAreaCliente>> ordenesAgrupadasPorAreaCliente = ordenesPorAreaCliente.stream().collect(Collectors.groupingBy(orden -> orden.NombreTipoOperacion));
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::GenerarInformeEstadisticas.obtenerOrdenesAgrupadasPorAreaCliente", finServicio - inicioServicio);
		return ordenesAgrupadasPorAreaCliente;
		
	}
	
	private static void generarDesgloseOrden(Sheet hoja, List<DetalleOrdenPorAreaCliente> ordenes) throws Exception {
		
		logger.trace("INICIO::GenerarInformeEstadistica.generarDesgloseOrden");
		Long inicioServicio = System.currentTimeMillis();
		
		Integer column = 0;
		Row fila = hoja.createRow(++filaActual);
		Cell celda = fila.createCell(column);
		
		for(DetalleOrdenPorAreaCliente orden : ordenes) {
			column = 0;
			fila = hoja.createRow(filaActual); 
			celda = fila.createCell(column);
			celda.setCellValue(orden.AreaCliente.toUpperCase());
			celda.setCellStyle(GenerarInformeEstadisticaUtils.estiloDetalle);
			    				
			celda = fila.createCell(++column);
			celda.setCellValue("");
			celda.setCellStyle(GenerarInformeEstadisticaUtils.estiloDetalle);
			
			celda = fila.createCell(++column);
			celda.setCellType(Cell.CELL_TYPE_NUMERIC);
			celda.setCellValue(orden.CantidadOperaciones);
			celda.setCellStyle(GenerarInformeEstadisticaUtils.estiloDetalle);
			
			celda = fila.createCell(++column);
			celda.setCellType(Cell.CELL_TYPE_NUMERIC);
			celda.setCellValue(orden.MontoOperacion.doubleValue());
			celda.setCellStyle(GenerarInformeEstadisticaUtils.estiloMoneda);
			
			celda = fila.createCell(++column);
			celda.setCellValue("");
			celda.setCellStyle(GenerarInformeEstadisticaUtils.estiloDetalle);
			
			celda = fila.createCell(++column);
			celda.setCellValue("");
			celda.setCellStyle(GenerarInformeEstadisticaUtils.estiloDetalle);
			++filaActual;
		}
		
		Long finServicio = System.currentTimeMillis();
		logger.trace("FIN::GenerarInformeEstadistica.generarDesgloseOrden", finServicio - inicioServicio);
		
	}
	
	
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAConnection())
		{
			System.out.println("============INICIO============");
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			
			String json = "{\"p_IdEmpresa\":1,\"p_Ruta\":\"C:\\\\BSC\",\"p_IdUsuario\":21,\"p_IdModulo\":\"ORD\",\"p_FechaOperacionOrdenI\":\"2020-08-21\",\"p_FechaOperacionOrdenF\":\"2020-08-21\"}";
			GenerarInformeEstadistica_Entrada Objin = gson.fromJson(json, GenerarInformeEstadistica_Entrada.class);
			GenerarInformeEstadistica demo = new GenerarInformeEstadistica();
			try 
			{
				GenerarInformeEstadistica_Salida ObjOut = demo.logica(con, Objin);
				System.out.println(ObjOut.P_OUT_COMENTARIO);
				
				System.out.println("============FIN============");
			}catch(Exception er) {
				er.printStackTrace();
				logger.error(er.getMessage());
				con.rollback();
			}
		}	
    }
		
}
