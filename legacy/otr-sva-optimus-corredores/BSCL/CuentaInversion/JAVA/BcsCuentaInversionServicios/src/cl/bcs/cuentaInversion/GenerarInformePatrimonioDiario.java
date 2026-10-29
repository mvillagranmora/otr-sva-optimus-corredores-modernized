package cl.bcs.cuentaInversion;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class GenerarInformePatrimonioDiario implements JavaCall{
    private final String SQL_SELECT_PATRIMONIO_DIARIO = loadResourceToString("cl/bcs/cuentaInversion/sql/ConsultaInformePatrimonioDario_SelectDetallado.sql");
    private static final Logger log = LogManager.getLogger(GenerarInformePatrimonioDiario.class);
    
    public static class EntradaGenerarInformePatrimonioDiario extends BcsPojo{
        public Long     p_IdEmpresa;
        public Long     p_IdUsuario;
        public String   p_IdModulo;
        public String   p_Ruta;
        public Long     p_IdCliente;
        public Long     p_IdPortafolio;
        public String   p_IdEstado;
        public Long     p_IdProductoAgrupador;
        public String   p_FechaDesde;
        public String   p_FechaHasta;
        public Long     p_Periodo;
        
        
    }
    
    public static class SalidaGenerarInformePatrimonioDiario extends BcsPojo{
        public String       p_out_NombreArchivoLog;
        public String       p_out_RutaArchivoLog;
        public String       p_out_NombreArchivo;
        public String       p_out_RutaArchivo;
        public Long         p_out_ErroresContador;
        public String       p_out_Comentario;
        public List<String> p_out_ArchivoSecundario;

    }
    
    public static class DataInforme extends BcsPojo{
        public Date Fecha;
        public String RutCliente;
        public String Portafolio;
        public BigDecimal FactorComision;
        public BigDecimal MontoComision;
        public BigDecimal MontoValorizado;
        public BigDecimal CantidadCustodia;
        public BigDecimal CantidadGarantia;
        public BigDecimal PrecioMercado;
        public String Nemo;
        public String Mercado;
        public String FactorNemo;

    }
    
    
            
    public SalidaGenerarInformePatrimonioDiario logica(Connection con, EntradaGenerarInformePatrimonioDiario entrada) throws Exception {

        log.info("INICIO::GenerarInformePatrimonioDiario ");
        SQL _sql = new SQL();
        SalidaGenerarInformePatrimonioDiario out = new SalidaGenerarInformePatrimonioDiario();
        String fechaSistema = Utils.getConfiguracion(con, Utils.getSchema(), entrada.p_IdEmpresa, "PLA.FechaSistema");
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date fechaInforme           = dateFormat.parse(fechaSistema);
        String nombreArchivo        = "InformePatrimonioDiario_"+ new SimpleDateFormat("ddMMyyyy").format(fechaInforme); 
        fechaSistema= fechaSistema.replace("-", "");
        
        out.p_out_ErroresContador = 0L;
        out.p_out_NombreArchivo = nombreArchivo + ".xlsx";
        out.p_out_NombreArchivoLog = nombreArchivo + ".log";
        out.p_out_RutaArchivo =entrada.p_Ruta + "/" + out.p_out_NombreArchivo;
        out.p_out_RutaArchivoLog =entrada.p_Ruta + "/" + out.p_out_NombreArchivoLog;
        out.p_out_Comentario =  Diccionario.getMensaje(con, entrada.p_IdEmpresa, "ARCHIVO_GENERADO_EXITOSAMENTE").msg();
        
        
        HashMap<String,Object> params =  new HashMap<>(6);
        params.put("p_Cliente", entrada.p_IdCliente);
        params.put("p_Portafolio", entrada.p_IdPortafolio);
        params.put("p_FechaDesde", entrada.p_FechaDesde);
        params.put("p_FechaHasta", entrada.p_FechaHasta);
        params.put("p_EstadoComision", entrada.p_IdEstado);
        params.put("p_ProductoAgrupador", entrada.p_IdProductoAgrupador);
        params.put("p_Periodo", entrada.p_Periodo);
        
        log.info("INICIO - Select Obtener informacion");
        List<DataInforme> infos = _sql.query(con, SQL_SELECT_PATRIMONIO_DIARIO,params, DataInforme.class);
        log.info("FIN - Select Obtener informacion"); 

    
        
        try (FileOutputStream outputStream = new FileOutputStream(out.p_out_RutaArchivo)) {
            Workbook archivoExcel = new XSSFWorkbook();
            Sheet hoja = archivoExcel.createSheet();
            
            archivoExcel.setSheetName(0, "Patrimonio Diario");
            
            /************************************/
            /** Formatos **/
            /************************************/

            hoja.setColumnWidth(0, 4000);
            hoja.setColumnWidth(1, 4000);
            hoja.setColumnWidth(2, 8000);
            hoja.setColumnWidth(3, 4000);
            hoja.setColumnWidth(4, 4000);
            hoja.setColumnWidth(5, 8000);
            hoja.setColumnWidth(6, 4000);
            hoja.setColumnWidth(7, 4000);
            hoja.setColumnWidth(8, 4000);
            hoja.setColumnWidth(9, 4000);
            hoja.setColumnWidth(10, 4000);
            hoja.setColumnWidth(11, 4000);

            CellStyle estiloMoneda = archivoExcel.createCellStyle();
            DataFormat df = archivoExcel.createDataFormat();
            estiloMoneda.setDataFormat(df.getFormat("_ $* #,##0_ ;_ $* -#,##0_ ;_ $* \"-\"??_ ;_ @_ "));
            estiloMoneda.setBorderBottom(CellStyle.BORDER_THIN);
            estiloMoneda.setBorderLeft(CellStyle.BORDER_THIN);
            estiloMoneda.setBorderRight(CellStyle.BORDER_THIN);
            estiloMoneda.setBorderTop(CellStyle.BORDER_THIN);
            
            CellStyle estiloMonedaPrecio = archivoExcel.createCellStyle();
            estiloMonedaPrecio.setDataFormat(df.getFormat("_($* #,##0.00_);_($* (#,##0.00);_($* \"-\"??_);_(@_)"));
            estiloMonedaPrecio.setBorderBottom(CellStyle.BORDER_THIN);
            estiloMonedaPrecio.setBorderLeft(CellStyle.BORDER_THIN);
            estiloMonedaPrecio.setBorderRight(CellStyle.BORDER_THIN);
            estiloMonedaPrecio.setBorderTop(CellStyle.BORDER_THIN);

            
            CellStyle estiloNumeroCantidad = archivoExcel.createCellStyle();
            estiloNumeroCantidad.setDataFormat(df.getFormat("#,##0"));
            estiloNumeroCantidad.setBorderBottom(CellStyle.BORDER_THIN);
            estiloNumeroCantidad.setBorderLeft(CellStyle.BORDER_THIN);
            estiloNumeroCantidad.setBorderRight(CellStyle.BORDER_THIN);
            estiloNumeroCantidad.setBorderTop(CellStyle.BORDER_THIN);
            

            CellStyle estiloNumero = archivoExcel.createCellStyle();
            estiloNumero.setDataFormat(df.getFormat("#,##0.00%"));
            estiloNumero.setBorderBottom(CellStyle.BORDER_THIN);
            estiloNumero.setBorderLeft(CellStyle.BORDER_THIN);
            estiloNumero.setBorderRight(CellStyle.BORDER_THIN);
            estiloNumero.setBorderTop(CellStyle.BORDER_THIN);

            CellStyle estiloLetra = archivoExcel.createCellStyle();
            estiloLetra.setBorderBottom(CellStyle.BORDER_THIN);
            estiloLetra.setBorderLeft(CellStyle.BORDER_THIN);
            estiloLetra.setBorderRight(CellStyle.BORDER_THIN);
            estiloLetra.setBorderTop(CellStyle.BORDER_THIN);
            
            CellStyle estiloFecha = archivoExcel.createCellStyle();
            estiloFecha.setDataFormat(df.getFormat("yyyyMMdd"));
            estiloFecha.setBorderBottom(CellStyle.BORDER_THIN);
            estiloFecha.setBorderLeft(CellStyle.BORDER_THIN);
            estiloFecha.setBorderRight(CellStyle.BORDER_THIN);
            estiloFecha.setBorderTop(CellStyle.BORDER_THIN);


            CellStyle estiloEncabezado = archivoExcel.createCellStyle();
            Font fuenteEncabezado = archivoExcel.createFont();
            fuenteEncabezado.setBold(true);
            estiloEncabezado.setFont(fuenteEncabezado);
            estiloEncabezado.setAlignment(CellStyle.ALIGN_CENTER);
            estiloEncabezado.setWrapText(true);
            estiloEncabezado.setBorderBottom(CellStyle.BORDER_THIN);
            estiloEncabezado.setBorderLeft(CellStyle.BORDER_THIN);
            estiloEncabezado.setBorderRight(CellStyle.BORDER_THIN);
            estiloEncabezado.setBorderTop(CellStyle.BORDER_THIN);

            CellStyle estiloTipoOperacion = archivoExcel.createCellStyle();
            estiloTipoOperacion.setWrapText(true);

            /************************************/
            /** Encabezado **/
            /************************************/
            int rowCount = 0;
            Row fila = hoja.createRow(rowCount);
            int columnCount = 0;
            
            Cell celda = fila.createCell(columnCount);
            celda.setCellValue("Fecha");
            celda.setCellStyle(estiloEncabezado);
            
            celda = fila.createCell(++columnCount);
            celda.setCellValue("Rut Cliente");
            celda.setCellStyle(estiloEncabezado);

            celda = fila.createCell(++columnCount);
            celda.setCellValue("Portafolio");
            celda.setCellStyle(estiloEncabezado);

            celda = fila.createCell(++columnCount);
            celda.setCellValue("Mercado");
            celda.setCellStyle(estiloEncabezado);

            celda = fila.createCell(++columnCount);
            celda.setCellValue("Nemo");
            celda.setCellStyle(estiloEncabezado);
            
            celda = fila.createCell(++columnCount);
            celda.setCellValue("Cantidad Custodia");
            celda.setCellStyle(estiloEncabezado);
            
            celda = fila.createCell(++columnCount);
            celda.setCellValue("Cantidad Garantia");
            celda.setCellStyle(estiloEncabezado);
            
            celda = fila.createCell(++columnCount);
            celda.setCellValue("Precio Mercado");
            celda.setCellStyle(estiloEncabezado);
            
            celda = fila.createCell(++columnCount);
            celda.setCellValue("Monto valorización");
            celda.setCellStyle(estiloEncabezado);
            
            celda = fila.createCell(++columnCount);
            celda.setCellValue("Factor comisión");
            celda.setCellStyle(estiloEncabezado);
            
            celda = fila.createCell(++columnCount);
            celda.setCellValue("Factor nemo");
            celda.setCellStyle(estiloEncabezado);
            
            celda = fila.createCell(++columnCount);
            celda.setCellValue("Comisión diaria acumulada");
            celda.setCellStyle(estiloEncabezado);


            /************************************/
            /** DETALLE DIFI **/
            /************************************/

            for (DataInforme data : infos) {
                columnCount = 0;
                fila = hoja.createRow(++rowCount); // fila en donde se escribe

                celda = fila.createCell(columnCount);
                celda.setCellType(Cell.CELL_TYPE_NUMERIC);
                celda.setCellValue(data.Fecha);
                celda.setCellStyle(estiloFecha);
                
                celda = fila.createCell(++columnCount);
                celda.setCellValue(data.RutCliente);
                celda.setCellStyle(estiloLetra);

                celda = fila.createCell(++columnCount);
                celda.setCellValue(data.Portafolio);
                celda.setCellStyle(estiloLetra);

                celda = fila.createCell(++columnCount);
                celda.setCellValue(data.Mercado);
                celda.setCellStyle(estiloLetra);
                
                celda = fila.createCell(++columnCount);
                celda.setCellValue(data.Nemo);
                celda.setCellStyle(estiloLetra);
                
                celda = fila.createCell(++columnCount);
                celda.setCellType(Cell.CELL_TYPE_NUMERIC);
                celda.setCellValue(data.CantidadCustodia.doubleValue());
                celda.setCellStyle(estiloNumeroCantidad);
                
                celda = fila.createCell(++columnCount);
                celda.setCellType(Cell.CELL_TYPE_NUMERIC);
                celda.setCellValue(data.CantidadGarantia.doubleValue());
                celda.setCellStyle(estiloNumeroCantidad);
                
                celda = fila.createCell(++columnCount);
                celda.setCellType(Cell.CELL_TYPE_NUMERIC);
                celda.setCellValue(data.PrecioMercado.doubleValue());
                celda.setCellStyle(estiloMonedaPrecio);
                
                celda = fila.createCell(++columnCount);
                celda.setCellType(Cell.CELL_TYPE_NUMERIC);
                celda.setCellValue(data.MontoValorizado.doubleValue());
                celda.setCellStyle(estiloMoneda);
                
                celda = fila.createCell(++columnCount);
                celda.setCellType(Cell.CELL_TYPE_NUMERIC);
                celda.setCellValue(data.FactorComision.doubleValue());
                celda.setCellStyle(estiloNumero);
                
                celda = fila.createCell(++columnCount);
                if(data.FactorNemo.equals("N/A")) {
                    celda.setCellValue(data.FactorNemo);
                    celda.setCellStyle(estiloLetra);
                }else {
                    celda.setCellType(Cell.CELL_TYPE_NUMERIC);
                    celda.setCellValue(Integer.valueOf(data.FactorNemo));
                    celda.setCellStyle(estiloNumero);
                }
                
                
                celda = fila.createCell(++columnCount);
                celda.setCellType(Cell.CELL_TYPE_NUMERIC);
                celda.setCellValue(data.MontoComision.doubleValue());
                celda.setCellStyle(estiloMoneda);

                
            }

            archivoExcel.write(outputStream);

            archivoExcel.close();
        } catch (Exception e) {
            out.p_out_ErroresContador++;
            out.p_out_Comentario = e.getMessage();
        }
        
        File fileRF = new File(out.p_out_RutaArchivoLog);
        try (BufferedWriter achivoLog = new BufferedWriter(new FileWriter(fileRF));) {
            achivoLog.write(out.p_out_Comentario);
            achivoLog.newLine();
        }
        log.info("FIN::GenerarInformePatrimonioDiario ");

        return out;
    }
    
    
    @Override
    public JsonObject call(Connection con, JsonObject jin) throws Exception {

        log.info("=============== Inicio Llamada GenerarInformePatrimonioDiario ===============");
        Gson gson = Utils.getGsonBuilder();
        EntradaGenerarInformePatrimonioDiario in = gson.fromJson(jin.toString(), EntradaGenerarInformePatrimonioDiario.class);
        SalidaGenerarInformePatrimonioDiario out = logica(con, in);
        JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
        log.info("=============== Fin Llamada GenerarInformePatrimonioDiario  ===============");
        
        return dataout;
    }
    
    public static void main(String[] args) throws Exception{
        System.out.println("== TEST ==");
        try (Connection con = PlataformaUtils.getPropertiesConnection("Master01","connections.properties")) {
            Gson gson = Utils.getGsonBuilder(); 

            String json = "{\r\n"
                    + "  \"p_IdEmpresa\": 1,\r\n"
                    + "  \"p_Ruta\":\"C:/__test\",\r\n"
                    + "  \"p_IdUsuario\": 97,\r\n"
                    + "  \"p_IdModulo\": \"CUS\",\r\n"
                    + "  \"p_IdCliente\": 0,\r\n"
                    + "  \"p_IdPortafolio\": 0,\r\n"
                    + "  \"p_FechaDesde\": \"2024-01-01\",\r\n"
                    + "  \"p_FechaHasta\": \"2024-09-30\",\r\n"
                    + "  \"p_IdEstado\": \"\",\r\n"
                    + "  \"p_Periodo\": 1,\r\n"
                    + "  \"p_IdProductoAgrupador\": 0\r\n"
                    + "}";


            EntradaGenerarInformePatrimonioDiario Objin = gson.fromJson(json, EntradaGenerarInformePatrimonioDiario.class);
            GenerarInformePatrimonioDiario demo = new GenerarInformePatrimonioDiario();
            try {
                SalidaGenerarInformePatrimonioDiario salida = demo.logica(con, Objin);
                System.out.println(salida.toString());
                //con.commit();
                con.rollback();
            }
            catch(Exception er) {
                er.printStackTrace();   
                con.rollback();
            }
        }
        System.out.println("== END ==");
    }

}