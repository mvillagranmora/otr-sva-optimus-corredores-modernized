package cl.bcs.facturacion;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.google.gson.JsonObject;

import cl.bcs.facturacion.GenerarInformesComprobanteComisionesDiarias.EntradaGenerarInformesComprobanteComisionesDiarias;
import cl.bcs.facturacion.pojo.ComprobanteComision;
import cl.bcs.facturacion.pojo.ReporteMatrizComisionesDiarias;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;

public class GenerarInformeMatrizComisionesDiarias  implements JavaCall {
	private final static Logger logger = LogManager.getLogger(GenerarInformeMatrizComisiones.class);
	private final String SQL_SELECT_COMPROBANTES_COMISIONES = loadResourceToString("cl/bcs/facturacion/sql/GenerarInformeMatrizComisionesDiarias_SelectComprobantesComisiones.sql");
	private final String SQL_SELECT_COMISIONES = loadResourceToString("cl/bcs/facturacion/sql/GenerarInformeMatrizComisionesDiarias_SelectComisionesMinimasComprobante.sql");
	
	public void crearInformeMatrizComisiones(Connection con, EntradaGenerarInformesComprobanteComisionesDiarias entrada, Workbook archivoExcel) throws Exception{
		
		logger.info("INICIO::GenerarInformeMatrizComisiones.crearInformeComisionClienteDetallado");
		Long inicioServicio = System.currentTimeMillis();
	
		List<ReporteMatrizComisionesDiarias> datosMatrizComisiones = obtenerDatosMatrizComisiones(con, entrada);
		ReporteMatrizComisionesDiarias cabecerasComision = new ReporteMatrizComisionesDiarias();
		Field[] cabeceras = cabecerasComision.getClass().getDeclaredFields();
		
		if (datosMatrizComisiones.isEmpty()) {
			String mensaje = Diccionario.getMensaje(con, entrada.p_IdEmpresa, "UI_NO_EXISTE_INFORMACION_BD").msg();
			return;
		}else {
			
			try {
	            Sheet sheet = archivoExcel.createSheet("Informe de Comisiones");

	            Row headerRow = sheet.createRow(0);
	            for (int i = 0; i < cabeceras.length; i++) {
	                headerRow.createCell(i).setCellValue(cabeceras[i].getName());
	            }

	            int rowIdx = 1;
	            for (ReporteMatrizComisionesDiarias objeto : datosMatrizComisiones) {
	                Row row = sheet.createRow(rowIdx++);
	                
	                for (int colIdx = 0; colIdx < cabeceras.length; colIdx++) {
	                    Field campo = cabeceras[colIdx];

	                    Object valor = campo.get(objeto);
	                    Cell celda = row.createCell(colIdx);

	                    if (valor != null) {
	                        if (valor instanceof Number) {
	                            celda.setCellValue(((Number) valor).doubleValue());
	                        } else if (valor instanceof Boolean) {
	                            celda.setCellValue((Boolean) valor);
	                        } else {
	                            celda.setCellValue(valor.toString());
	                        }
	                    }
	                }
	            }

	            for (int i = 0; i < cabeceras.length; i++) {
	                sheet.autoSizeColumn(i);
	            }

	            try (FileOutputStream fileOut = new FileOutputStream(entrada.p_Ruta)) {
	            	
	            	archivoExcel.write(fileOut);
	            }

	        } catch (IllegalAccessException e) {
	            e.printStackTrace();
	        }
		}
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::GenerarInformeMatrizComisionesDiariasRV.crearInformeMatrizComisiones", finServicio - inicioServicio);
	}
	
	
	
	private List<ReporteMatrizComisionesDiarias> obtenerDatosMatrizComisiones(Connection con, EntradaGenerarInformesComprobanteComisionesDiarias entrada) throws Exception {
		
		logger.info("INICIO::GenerarInformeMatrizComisiones.obtenerDatosMatrizComisiones");
		Long inicioServicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		HashMap<String, Object> params = new HashMap<>();
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("Fecha", entrada.p_Fecha);
		params.put("IdPortafolio", entrada.p_IdPortafolio);
		params.put("IdCliente", entrada.p_IdCliente);
		
		List<ReporteMatrizComisionesDiarias> listaMatrizComisiones = new ArrayList<ReporteMatrizComisionesDiarias>();
		listaMatrizComisiones = sql.query(con, SQL_SELECT_COMPROBANTES_COMISIONES, params, ReporteMatrizComisionesDiarias.class);
		
		
		if(listaMatrizComisiones.size() > 0 ) {
			List<Long> IdsComprobantes = new ArrayList<Long>();
			for(ReporteMatrizComisionesDiarias ac : listaMatrizComisiones) {
				IdsComprobantes.add(ac.IdComprobante);
				
			}
			
			params.clear();
			params.put("IdsComprobantes",IdsComprobantes);
			List<ComprobanteComision> comprobanteComision = new ArrayList<ComprobanteComision>(); 
			comprobanteComision = sql.query(con, SQL_SELECT_COMISIONES, params, ComprobanteComision.class);
			
			Map<Long, BigDecimal> mapaComisiones = comprobanteComision.stream()
				    .collect(Collectors.toMap(
				        c -> c.IdComprobante, 
				        c -> c.ComisionMinima
				    ));
			for(ReporteMatrizComisionesDiarias ac : listaMatrizComisiones) {
				BigDecimal comisionMin = mapaComisiones.get(ac.IdComprobante);
				if(comisionMin == null) {
					comisionMin = BigDecimal.ZERO;
				}
				
				ac.ComisionMinima = comisionMin;
			};
		}else {
			listaMatrizComisiones.add(new ReporteMatrizComisionesDiarias());
		}
		
		
		
			
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::GenerarInformeMatrizComisiones.obtenerDatosMatrizComisiones", finServicio - inicioServicio);
		
		return listaMatrizComisiones;
		
	}
	
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		return null;
	}
}
