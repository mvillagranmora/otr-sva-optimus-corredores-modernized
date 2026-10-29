package cl.bcs.cuentaInversion;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.cuentaInversion.ConsultarCobroComisionRegistros.Tabla_Salida;

import cl.bcs.cuentaInversion.ConsultarCobroComisionRegistros.EntradaCobroComisionConsultarRegistros;
import cl.bcs.cuentaInversion.ConsultarCobroComisionRegistros.SalidaCobroComisionEditarRegistro;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

public class GenerarInformeResumenCobro implements JavaCall {
    private static final Logger log = LogManager.getLogger(GenerarInformeResumenCobro.class);
    
    public static class EntradaGenerarInformeResumenCobro extends BcsPojo{
        public Long     p_IdEmpresa;
        public Long     p_IdUsuario;
        public String   p_IdModulo;
        public String   p_Ruta;
        public Long     p_IdCliente;
        public Long     p_IdPortafolio;
        public String   p_IdEstado;
        public String   p_FechaDesde;
        public String   p_FechaHasta;
        public Long     p_IdProductoAgrupador;
        public Long     p_Periodo;
        
        
    }
    
    public static class SalidaGenerarInformeResumenCobro extends BcsPojo{
        public String       p_out_NombreArchivoLog;
        public String       p_out_RutaArchivoLog;
        public String       p_out_NombreArchivo;
        public String       p_out_RutaArchivo;
        public Long         p_out_ErroresContador;
        public String       p_out_Comentario;
        public List<String> p_out_ArchivoSecundario;

    }
    
    public static class DataInforme {
        public String NombreCliente;
        public String RutCliente;
        public Long ComisionAcumulada;
        public Long Cuenta;

        public String toString() {
            return Utils.printJson(this, false);
        }
    }
    
    
            
    public SalidaGenerarInformeResumenCobro logica(Connection con, EntradaGenerarInformeResumenCobro entrada) throws Exception {

        log.info("INICIO::GenerarInformeResumenCobro ");

        SalidaGenerarInformeResumenCobro out = new SalidaGenerarInformeResumenCobro();
        String fechaSistema = Utils.getConfiguracion(con, Utils.getSchema(), entrada.p_IdEmpresa, "PLA.FechaSistema");
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date fechaInforme           = dateFormat.parse(fechaSistema);
        String nombreArchivo        = "InformeResumenCobro_"+ new SimpleDateFormat("ddMMyyyy").format(fechaInforme); 
        
        out.p_out_ErroresContador = 0L;
        out.p_out_NombreArchivo = nombreArchivo + ".xls";
        out.p_out_NombreArchivoLog = nombreArchivo + ".log";
        out.p_out_RutaArchivo =entrada.p_Ruta + "/" + out.p_out_NombreArchivo;
        out.p_out_RutaArchivoLog =entrada.p_Ruta + "/" + out.p_out_NombreArchivoLog;
        out.p_out_Comentario =  Diccionario.getMensaje(con, entrada.p_IdEmpresa, "ARCHIVO_GENERADO_EXITOSAMENTE").msg();
        
        
        EntradaCobroComisionConsultarRegistros entradaConsulta= new EntradaCobroComisionConsultarRegistros();
        entradaConsulta.p_IdEmpresa=entrada.p_IdEmpresa;
        entradaConsulta.p_IdUsuario=entrada.p_IdUsuario;
        entradaConsulta.p_IdModulo="CUS";
        entradaConsulta.p_UsaReglasNegocio="N";
        entradaConsulta.p_Cliente=entrada.p_IdCliente;
        entradaConsulta.p_Portafolio=entrada.p_IdPortafolio;
        entradaConsulta.p_FechaDesde=entrada.p_FechaDesde;
        entradaConsulta.p_FechaHasta=entrada.p_FechaHasta;
        entradaConsulta.p_IdProductoAgrupador=entrada.p_IdProductoAgrupador;
        entradaConsulta.p_IdEstado=entrada.p_IdEstado;
        entradaConsulta.p_Periodo=entrada.p_Periodo;
        
        ConsultarCobroComisionRegistros cobroComisionRegistros = new ConsultarCobroComisionRegistros();                 
        SalidaCobroComisionEditarRegistro registros = cobroComisionRegistros.logica(con, entradaConsulta);
        
        List<Tabla_Salida> datosOri = registros.P_OUT_TABLA;
        
        try (FileOutputStream outputStream = new FileOutputStream(out.p_out_RutaArchivo)) {
            Workbook archivoExcel = new HSSFWorkbook();
            Sheet hoja = archivoExcel.createSheet();
            
            archivoExcel.setSheetName(0, "Resumen Cobro");
            
            /************************************/
            /** Formatos **/
            /************************************/

            hoja.setColumnWidth(0, 4000);
            hoja.setColumnWidth(1, 8000);
            hoja.setColumnWidth(2, 4000);
            hoja.setColumnWidth(3, 15000);
            

            CellStyle estiloMoneda = archivoExcel.createCellStyle();
            DataFormat df = archivoExcel.createDataFormat();
            estiloMoneda.setDataFormat(df.getFormat("_ $* #,##0_ ;_ $* -#,##0_ ;_ $* \"-\"??_ ;_ @_ "));
            estiloMoneda.setBorderBottom(CellStyle.BORDER_THIN);
            estiloMoneda.setBorderLeft(CellStyle.BORDER_THIN);
            estiloMoneda.setBorderRight(CellStyle.BORDER_THIN);
            estiloMoneda.setBorderTop(CellStyle.BORDER_THIN);

            CellStyle estiloNumero = archivoExcel.createCellStyle();
            estiloNumero.setDataFormat(df.getFormat("#,##0"));
            estiloNumero.setBorderBottom(CellStyle.BORDER_THIN);
            estiloNumero.setBorderLeft(CellStyle.BORDER_THIN);
            estiloNumero.setBorderRight(CellStyle.BORDER_THIN);
            estiloNumero.setBorderTop(CellStyle.BORDER_THIN);

            CellStyle estiloLetra = archivoExcel.createCellStyle();
            estiloLetra.setBorderBottom(CellStyle.BORDER_THIN);
            estiloLetra.setBorderLeft(CellStyle.BORDER_THIN);
            estiloLetra.setBorderRight(CellStyle.BORDER_THIN);
            estiloLetra.setBorderTop(CellStyle.BORDER_THIN);



            CellStyle estiloEncabezado = archivoExcel.createCellStyle();
            Font fuenteEncabezado = archivoExcel.createFont();
            fuenteEncabezado.setBold(true);
            estiloEncabezado.setFont(fuenteEncabezado);
            //estiloEncabezado.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            //estiloEncabezado.setFillPattern(CellStyle.SOLID_FOREGROUND);
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
            celda.setCellValue("Rut Cliente");
            celda.setCellStyle(estiloEncabezado);

            celda = fila.createCell(++columnCount);
            celda.setCellValue("Portafolio");
            celda.setCellStyle(estiloEncabezado);

            celda = fila.createCell(++columnCount);
            celda.setCellValue("Comisión Diaria Acumulada");
            celda.setCellStyle(estiloEncabezado);

            celda = fila.createCell(++columnCount);
            celda.setCellValue("Nombre Cliente");
            celda.setCellStyle(estiloEncabezado);


            /************************************/
            /** DETALLE DIFI **/
            /************************************/

            for (Tabla_Salida data : datosOri) {
                columnCount = 0;
                fila = hoja.createRow(++rowCount); // fila en donde se escribe

                celda = fila.createCell(columnCount);
                celda.setCellValue(data.RutCliente);
                celda.setCellStyle(estiloLetra);

                celda = fila.createCell(++columnCount);
                celda.setCellValue(data.Portafolio);
                celda.setCellStyle(estiloLetra);

                celda = fila.createCell(++columnCount);
                celda.setCellType(Cell.CELL_TYPE_NUMERIC);
                celda.setCellValue(data.MontoComision.doubleValue());
                celda.setCellStyle(estiloMoneda);

                celda = fila.createCell(++columnCount);
                celda.setCellValue(data.Nombre);
                celda.setCellStyle(estiloLetra);

                
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
        log.info("FIN::GenerarInformeResumenCobro ");

        return out;
    }
    
    
    @Override
    public JsonObject call(Connection con, JsonObject jin) throws Exception {

        log.info("=============== Inicio Llamada GenerarInformeResumenCobro ===============");
        Gson gson = Utils.getGsonBuilder();
        EntradaGenerarInformeResumenCobro in = gson.fromJson(jin.toString(), EntradaGenerarInformeResumenCobro.class);
        SalidaGenerarInformeResumenCobro out = logica(con, in);
        JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
        log.info("=============== Fin Llamada GenerarInformeResumenCobro  ===============");
        
        return dataout;
    }
    
    public static void main(String[] args) throws Exception{
        System.out.println("== TEST ==");
        try (Connection con = PlataformaUtils.getPropertiesConnection("desa01","connections.properties")) {
            Gson gson = Utils.getGsonBuilder(); 

            String json = "{\r\n"
                    + "  \"p_IdEmpresa\": 1,\r\n"
                    + "  \"p_Ruta\":\"\",\r\n"
                    + "  \"p_IdUsuario\": 0,\r\n"
                    + "  \"p_IdModulo\": \"CUS\",\r\n"
                    + "  \"p_IdCliente\": 0,\r\n"
                    + "  \"p_IdPortafolio\": 0,\r\n"
                    + "  \"p_IdProductoAgrupador\": 0,\r\n"
                    + "  \"p_Periodo\": 1,\r\n"
                    + "  \"p_IdEstado\": \"\",\r\n"
                    + "  \"p_FechaDesde\": \"2024-01-01\",\r\n"
                    + "  \"p_FechaHasta\": \"2024-03-31\"\r\n"
                    + "}";

            EntradaGenerarInformeResumenCobro Objin = gson.fromJson(json, EntradaGenerarInformeResumenCobro.class);
            GenerarInformeResumenCobro demo = new GenerarInformeResumenCobro();
            try {
                SalidaGenerarInformeResumenCobro salida = demo.logica(con, Objin);
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