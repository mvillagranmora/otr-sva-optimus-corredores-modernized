package cl.bcs.normativa.circulares;

import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.normativa.circulares.pojo.Campo;
import cl.bcs.plataforma.pla.utils.FileLoader;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class GenerarInformeForwardsVigentesEBox implements JavaCall{	
    private static final FileLoader loader = new FileLoader();
	private static final Logger LOGGER = LogManager.getLogger(GenerarInformeForwardsVigentesEBox.class);
	
	private static String SQL_SELECT_FORWARDS_VIGENTES = loader.loadResourceToString("cl/bcs/normativa/circulares/sql/GenerarInformeForwardsVigentesEBox_SelectForwardsVigentes.sql");
	private static String SQL_SELECT_CAMPO_ESQUEMA =  loader.loadResourceToString("cl/bcs/normativa/circulares/sql/Circulares_SelectCampoEsquema.sql");
	private static final String ESQUEMA = "FWDVIGEBOX";
	private static final String VERSION = "V1";	
    private final Map<Class<?>, Map<String, Field>> cacheFields = new HashMap<>();
	
    public static class ObjTablaSalida extends BcsPojo{
		public Date FechaOperacion;
		public Long NumeroContrato;
		public Long NumeroContratoCalce;
		public String RutCliente;
		public String NombreCliente;
		public String ClienteRelacionado;
		public String TipoOperacion;
		public Date FechaVencimiento;
		public String ModalidadLiquidacion;
		public String MonedaTasa;
		public BigDecimal ValorNominal;
		public BigDecimal TipoCambioPactado;
		public BigDecimal MontoPactado;
		public BigDecimal TipoCambioMercado;
		public BigDecimal ValorizacionMercado;
		public BigDecimal ResultadoOperacion;
	}
	
	public static class GenerarInformeForwardsVigentesEBox_Entrada extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_FechaSistema;
		public String p_FechaInicio;
		public String p_FechaFin;
		public String p_NombreArchivo;
		public String p_Ruta;
		public Long p_SecuenciaSolicitud;
	}
		
	public static class GenerarInformeForwardsVigentesEBox_Salida extends BcsPojo{
		public String P_OUT_NOMBRE_ARCHIVO;
		public Long P_OUT_NUMERO_REGISTROS;
		public Long P_OUT_ERRORESCONTADOR;
		public String P_OUT_MENSAJE;
	}
	
	@Trace
	public GenerarInformeForwardsVigentesEBox_Salida logica(Connection con, GenerarInformeForwardsVigentesEBox_Entrada in) throws Exception {
		LOGGER.info("Inicio GenerarInformeForwardsVigentesEBox");
		LOGGER.info(in.toString());
				
		GenerarInformeForwardsVigentesEBox_Salida out = new GenerarInformeForwardsVigentesEBox_Salida();
		out.P_OUT_ERRORESCONTADOR = 0L;
        SQL sql = new SQL();

        HashMap<String, Object> paramsCampo = new HashMap<>(2);
        paramsCampo.put("Esquema", ESQUEMA);
        paramsCampo.put("Version", VERSION);
        
        LOGGER.info("INICIO::Select Campos Esquema");
        List<Campo> campos = sql.query(con, SQL_SELECT_CAMPO_ESQUEMA, paramsCampo, Campo.class);
		LOGGER.info("FIN::Select Campos Esquema");

        if (campos == null || campos.isEmpty()) {
            throw new Exception("No se encontraron campos para el esquema " + ESQUEMA);
        }

        Map<String, Campo> mapCampos = new LinkedHashMap<>();
        for (Campo c : campos) {
            mapCampos.put(c.Nombre, c);
        }

        HashMap<String, Object> paramsdetalle = new HashMap<>(2);
        paramsdetalle.put("SecuenciaSolicitud", in.p_SecuenciaSolicitud);

        LOGGER.info("INICIO::Select Forwards Vigentes");
        List<ObjTablaSalida> lstForwards = sql.query(con, SQL_SELECT_FORWARDS_VIGENTES, paramsdetalle, ObjTablaSalida.class);
        LOGGER.info("FIN::Select Forwards Vigentes");

        if (lstForwards == null || lstForwards.isEmpty()) {
            LOGGER.warn("No se encontraron forwards vigentes");
        }

        out.P_OUT_NOMBRE_ARCHIVO = in.p_NombreArchivo.concat(".xlsx");
        out.P_OUT_NUMERO_REGISTROS = Long.valueOf(lstForwards.size());
        
        try (FileOutputStream outputStream = new FileOutputStream(in.p_Ruta.concat(out.P_OUT_NOMBRE_ARCHIVO));
             Workbook wb = new XSSFWorkbook()) {

            Sheet hoja = wb.createSheet();
            // -------------------------
            // Estilos
            // -------------------------
            DataFormat format = wb.createDataFormat();

            Map<String, XSSFCellStyle> estilos = new HashMap<>(5);

            XSSFCellStyle estiloDecimal = (XSSFCellStyle) wb.createCellStyle();
            estiloDecimal.setDataFormat(format.getFormat("#,##0.00;(#,##0.00);-"));
            estiloDecimal.setVerticalAlignment(VerticalAlignment.CENTER);
            estilos.put("decimal", estiloDecimal);

            XSSFCellStyle estiloAbsoluto = (XSSFCellStyle) wb.createCellStyle();
            estiloAbsoluto.setDataFormat(format.getFormat("#,##0"));
            estiloAbsoluto.setVerticalAlignment(VerticalAlignment.CENTER);
            estilos.put("absoluto", estiloAbsoluto);

            XSSFCellStyle estiloFechaTexto = (XSSFCellStyle) wb.createCellStyle();
            estiloFechaTexto.setDataFormat(wb.createDataFormat().getFormat("@"));
            estiloFechaTexto.setAlignment(HorizontalAlignment.GENERAL);
            estiloFechaTexto.setVerticalAlignment(VerticalAlignment.BOTTOM);
            estilos.put("fechaTexto", estiloFechaTexto);

            XSSFCellStyle estiloTexto = (XSSFCellStyle) wb.createCellStyle();
            estiloTexto.setDataFormat(wb.createDataFormat().getFormat("@"));
            estiloTexto.setVerticalAlignment(VerticalAlignment.CENTER);
            estilos.put("texto", estiloTexto);
            
            XSSFCellStyle estiloNumero = (XSSFCellStyle) wb.createCellStyle();
            estiloNumero.setAlignment(HorizontalAlignment.RIGHT);
            estiloNumero.setVerticalAlignment(VerticalAlignment.CENTER);
            estilos.put("numero", estiloNumero);

            XSSFCellStyle estiloHeader = (XSSFCellStyle) wb.createCellStyle();
            estiloHeader.setAlignment(HorizontalAlignment.LEFT);
            estiloHeader.setVerticalAlignment(VerticalAlignment.CENTER);

            // -------------------------
            // Crear encabezado
            // -------------------------
            int rowCount = 0;
            Row filaEncabezado = hoja.createRow(rowCount++);
            int col = 0;

            for (Campo campo : mapCampos.values()) {
                Cell celda = filaEncabezado.createCell(col++);
                celda.setCellValue(campo.Titulo);
                celda.setCellStyle(estiloHeader);
            }

            // Congelar encabezado
            hoja.createFreezePane(0, 1);

            // -------------------------
            // Crear detalle
            // -------------------------
            Map<Integer, XSSFCellStyle> estilosDecimalCache = new HashMap<>();
            
            XSSFCellStyle estiloResultadoOperacion = null;
            
            for (ObjTablaSalida filaData : lstForwards) {

                Row fila = hoja.createRow(rowCount++);
                col = 0;

                for (Campo campo : mapCampos.values()) {
                    Cell celda = fila.createCell(col++);
                    Object valor = obtenerValor(filaData, campo.Nombre);

                    XSSFCellStyle estiloCelda;
                    
                    //Formato especial de ResultadoOperacion
                    if ("ResultadoOperacion".equalsIgnoreCase(campo.Nombre)) {

                        // Crear solo una vez
                        if (estiloResultadoOperacion == null) {
                            estiloResultadoOperacion = (XSSFCellStyle) wb.createCellStyle();

                            String formatoEspecial = "_(* #,##0_);_(* (#,##0);_(* 0_);_(@_)";
                            
                            estiloResultadoOperacion.setDataFormat(format.getFormat(formatoEspecial));
                            estiloResultadoOperacion.setAlignment(HorizontalAlignment.RIGHT);
                            estiloResultadoOperacion.setVerticalAlignment(VerticalAlignment.CENTER);
                        }

                        estiloCelda = estiloResultadoOperacion;

					} else if ("DECIMAL".equalsIgnoreCase(campo.TipoDato)) {
						int decimales = campo.CantidadDecimales != null ? campo.CantidadDecimales : 2;
						if (estilosDecimalCache.containsKey(decimales)) {
							estiloCelda = estilosDecimalCache.get(decimales);
						} else {
							estiloCelda = (XSSFCellStyle) wb.createCellStyle();
							StringBuilder formato = new StringBuilder("#,##0");
							if (decimales > 0) {
								formato.append(".");
								for (int d = 0; d < decimales; d++) {
									formato.append("0");
								}
							}
							estiloCelda.setDataFormat(format.getFormat(formato.toString()));
							estiloCelda.setAlignment(HorizontalAlignment.RIGHT);
							estiloCelda.setVerticalAlignment(VerticalAlignment.CENTER);
							estilosDecimalCache.put(decimales, estiloCelda);
						}
					} else {
						estiloCelda = obtenerEstiloPorTipoDato(campo.TipoDato, estilos);
					}
                    
                    celda.setCellStyle(estiloCelda);

                    if (valor == null) {
                        celda.setCellValue("");
                    } else if (valor instanceof BigDecimal) {
                        celda.setCellValue(((BigDecimal) valor).doubleValue());
                    } else if (valor instanceof Number) {
                        celda.setCellValue(((Number) valor).doubleValue());
                    } else if (valor instanceof java.util.Date) {
                        celda.setCellValue(new SimpleDateFormat("dd-MM-yyyy").format((Date) valor));
                    } else {
                        celda.setCellValue(valor.toString());
                    }
                }
            }

            int maxWidth = 10000;
            
            for (int i = 0; i < mapCampos.size(); i++) {
                hoja.autoSizeColumn(i);
                if (hoja.getColumnWidth(i) > maxWidth) {
                    hoja.setColumnWidth(i, maxWidth);
                }
            }

            wb.write(outputStream);

        } catch (Exception e) {
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_MENSAJE = e.getMessage();
            LOGGER.error("Error generando Excel", e);
        }

        return out;
    }

    private Object obtenerValor(Object obj, String nombreAttr) {
        try {
            Class<?> clazz = obj.getClass();

            cacheFields.putIfAbsent(clazz, new HashMap<>());
            Map<String, Field> map = cacheFields.get(clazz);

            if (!map.containsKey(nombreAttr)) {
                Field f = clazz.getDeclaredField(nombreAttr);
                f.setAccessible(true);
                map.put(nombreAttr, f);
            }

            return map.get(nombreAttr).get(obj);

        } catch (Exception e) {
            return null;
        }
    }

    private XSSFCellStyle obtenerEstiloPorTipoDato(String tipoDato, Map<String, XSSFCellStyle> estilos) {
        if (tipoDato == null)
            return estilos.get("texto");

        switch (tipoDato.toUpperCase()) {
            case "DATE": return estilos.get("fechaTexto");
            case "DECIMAL": return estilos.get("decimal");
            case "QTY": return estilos.get("absoluto");
            case "ID": return estilos.get("numero");
            default: return estilos.get("texto");
        }
    }

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		GenerarInformeForwardsVigentesEBox_Entrada jin=gson.fromJson(in.toString() ,GenerarInformeForwardsVigentesEBox_Entrada.class);
		GenerarInformeForwardsVigentesEBox_Salida out=logica(conn,jin);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

    public static void main(String[] args) throws Exception {

        try (Connection con = PlataformaUtils.getPropertiesConnection("desa2", "connections.properties")) {

            GenerarInformeForwardsVigentesEBox_Entrada entrada = new GenerarInformeForwardsVigentesEBox_Entrada();
            entrada.p_IdEmpresa = 1L;
            entrada.p_IdModulo = "NOR";
            entrada.p_IdUsuario = 1L;
            entrada.p_FechaSistema = "2021-11-27";
            entrada.p_FechaInicio = "2021-11-27";
            entrada.p_FechaFin = "2021-11-27";
            entrada.p_Ruta = "C:/mnt/optimus/des/empresa1/nor/";
            entrada.p_SecuenciaSolicitud = 1103L;
            entrada.p_NombreArchivo = "ForwardsPrueba";

            GenerarInformeForwardsVigentesEBox sp = new GenerarInformeForwardsVigentesEBox();
            GenerarInformeForwardsVigentesEBox_Salida salida = sp.logica(con, entrada);

            System.out.println(salida);

            con.rollback();

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("== END ==");
    }
}
