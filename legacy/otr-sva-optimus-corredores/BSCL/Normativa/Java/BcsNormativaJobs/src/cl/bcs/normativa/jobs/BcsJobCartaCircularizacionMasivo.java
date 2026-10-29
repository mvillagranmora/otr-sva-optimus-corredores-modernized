package cl.bcs.normativa.jobs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;


import com.google.gson.JsonArray;

import com.google.gson.JsonParser;


import cl.bcs.normativa.dal.LocalizadorArchivo;

import cl.bcs.plataforma.report.api.ReportDocument;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public final class BcsJobCartaCircularizacionMasivo extends BcsJob {

    private static final Logger LOGGER = Logger
            .getLogger(BcsJobCartaCircularizacionMasivo.class.getName());

    private String ActualizarSolicitud(
            final long p_IdDocumento,
            String p_NombreArchivo,
            String p_RutaArchivo,
            final long p_NumRegistros,
            final Connection conn,
            final long jobId) {

        String sqlCall = "";

        final String PROCEDURE_NAME_ACTUALIZAR = "BSCL.Normativa.BD.Servicios.NOR::ModificarEstadoSolicitudCircular";
        p_NombreArchivo = "'" + p_NombreArchivo + "'";
        p_RutaArchivo = "'" + p_RutaArchivo + "'";
        sqlCall = String.format("{CALL %s.%s(%s,%s,%s,%s,%s)}", getQuotedString("BSCL"),
                getQuotedString(PROCEDURE_NAME_ACTUALIZAR), p_IdDocumento, p_NombreArchivo,
                p_RutaArchivo, p_NumRegistros, jobId);
        System.out.println(sqlCall);

        try (CallableStatement cstmt = conn.prepareCall(sqlCall)) {
            conn.setAutoCommit(false);

            if (cstmt.execute()) {
                conn.commit();

                try (final ResultSet rs = cstmt.getResultSet()) {
                    if (rs.next()) {
                        LOGGER.log(Level.SEVERE, "Proceso de Actualizacion de Solicitud exitoso");
                        return "OK";
                    } else {
                        LOGGER.log(Level.SEVERE,
                                "Error durante proceso de Actualizacion de Solicitud, no RS");
                        return "NOK";

                    }

                } catch (final Exception ex) {

                    LOGGER.log(Level.SEVERE, "Error durante proceso de Actualizacion de Solicitud",
                            ex);
                    return "NOK";
                }

            } else {

                LOGGER.log(Level.SEVERE,
                        "Error no se pudo ejecutar servicio actualizaciÃ¯Â¿Â½n");
                return "NOK";
            }

        } catch (final Exception ex) {

            LOGGER.log(Level.SEVERE, "Error durante proceso de Actualizacion de Solicitud", ex);
            return "NOK";
        }

    }

    private String getQuotedString(
            final String name) {
        return "\"" + name + "\"";
    }
    public static class ResumenGeneracion extends BcsPojo{
		public Long 	IdSolicitudCircular;
		public String 	NombreArchivo;
		
		public ResumenGeneracion(Long IdSolicitudCircular, String NombreArchivo)
		{
			this.IdSolicitudCircular = IdSolicitudCircular;
			this.NombreArchivo = NombreArchivo;		
			
		}
	}
    
    @Override
    public void onError(
            final Throwable t) {
        System.out.println(":GENERAR CARTAS CIRCULARIZACION MASIVO:  ****** ERROR  *******");
        t.printStackTrace();
    }

    @Override
    public void pause() {

    }

    @Override
    public boolean transaction(
            final Connection conn)
                    throws InterruptedException,
                    Throwable {
        System.out.println(":GENERAR CARTAS CIRCULARIZACION MASIVO:  ****** INICIO  *******");
        final long idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
        final long secuenciaSolicitud = Long.parseLong(this.getParam("IdSolicitudCircular"));
        final String periodo = this.getParam("Periodo");
        final String tipoDocumento = this.getParam("idTipoDocumento");
        SQL sql = new SQL();
        // obtener la ruta de archivos para normativo
        final String ruta = LocalizadorArchivo.buscarRutaRaiz(conn, Utils.getSchema(), idEmpresa)
                + File.separator;

        // obtener los datos para la generacion de cada certificado

        final String selectCertificados = "SELECT CER.*,NOM.\"DocumentoIdentidad\",NOM.\"CodigoPortafolio\", NOM.\"NombreCliente\" from \"BSCL.Normativa.BD.Vistas::CIR.ConsultarNomina\"  AS NOM"
                + " INNER JOIN \"BSCL.Normativa.BD.Vistas::CER.ConsultarCertificadoCliente\" CER ON  NOM.\"IdPortafolio\"=CER.\"IdPortafolio\" AND NOM.\"Periodo\"=CER.\"Periodo\""
                + " WHERE   NOM.\"Notificar\"='SI' AND NOM.\"Periodo\"= ? AND CER.\"IdTipoDocumento\"=? ORDER BY CER.\"IdCliente\",CER.\"IdPortafolio\"";
        
        final String countCertificados = "SELECT COUNT(1) AS \"Total\" from \"BSCL.Normativa.BD.Vistas::CIR.ConsultarNomina\"  AS NOM"
                + " INNER JOIN \"BSCL.Normativa.BD.Vistas::CER.ConsultarCertificadoCliente\" CER ON  NOM.\"IdPortafolio\"=CER.\"IdPortafolio\" AND NOM.\"Periodo\"=CER.\"Periodo\""
                + " WHERE   NOM.\"Notificar\"='SI' AND NOM.\"Periodo\"= ? AND CER.\"IdTipoDocumento\"=?";
        
        final String SQL_UPDATE_SOLICITUD = "UPDATE BSCL.\"BSCL.Normativa.BD::NOR.SolicitudCircular\" SET \"EstadoSolicitud\"='',\"NombreArchivo\"=:NombreArchivo,\"IdEstado\"='EPR'  WHERE \"IdSolicitudCircular\"=:IdSolicitudCircular";
        
		final String SQL_SELECT_FIRMA_CORREDORA = "SELECT TOP 1 Imagen.\"Binario\" AS \"Contenido\" FROM \"BSCL.Plataforma.BD::PLA.Imagenes\" Imagen WHERE Imagen.\"NombreImagen\" = 'FIRMA_CORREDORA' AND Imagen.\"IdEmpresa\" = ? ORDER BY Imagen.\"Version\" DESC;";
        
        String tot="";
        try (PreparedStatement pstct = conn.prepareStatement(countCertificados)) {
        	pstct.setString(1, periodo);
        	pstct.setString(2, tipoDocumento);
        	try (ResultSet rsCount = pstct.executeQuery()) {
        		
        		 while (rsCount.next()) {
        			 tot=rsCount.getString("Total");
        		 }    
        	}
        	
        }
        
        try (PreparedStatement pstmt = conn.prepareStatement(selectCertificados)) {
            pstmt.setString(1, periodo);
            pstmt.setString(2, tipoDocumento);

            Date fechaActual = new Date();			
			DateFormat formatoFecha = new SimpleDateFormat("yyyyMMdd_HHmm");
			String fechaHora = formatoFecha.format(fechaActual);	
			String NombrePDF="";
			
            try (ResultSet rs = pstmt.executeQuery()) {
                final List<String> archivos = new ArrayList<String>();
                long contadorArchivos = 0,ContLog=0,contresumen=0;
                
               List<ResumenGeneracion> programa = new ArrayList<ResumenGeneracion>();
                while (rs.next()) {
                    contadorArchivos++;ContLog++;contresumen++;
                    final String reportId = rs.getString("IdArchivo");
                     
                    final ReportDocument report = new ReportDocument(Utils.getSchema(),
                            super.getDataSource());
                    if (!report.readDocument(reportId)) {
                        return false;
                    }

                    try {
                    	String jsonData = rs.getString("JsonData");
                        JsonParser parser = new JsonParser();
                        JsonArray jsonArrayData = parser.parse(jsonData).getAsJsonArray();
                        Map<String, String> schema = new HashMap<String, String> ();
                        schema.put("LogoCorredora", "blob");
                        schema.put("FirmaCorredora", "blob");
                        schema.put("Cantidad", "number");
                        schema.put("Tasa", "number");
                        schema.put("ValorPresente", "number");
                        schema.put("PrecioCierre", "number");
                        
                        final String codigoPDF417 = jsonArrayData.get(0).getAsJsonObject().get("NombreDestinatario").getAsString();
                        byte[] firmaCorredoraBase64 = null;
                        String firmaCorredoraBase64String = "";
                        
                        try (PreparedStatement pstmtss = conn.prepareStatement(SQL_SELECT_FIRMA_CORREDORA)) {
                        	pstmtss.setLong(1, idEmpresa);
                        	try (ResultSet rsCount = pstmtss.executeQuery()) {
                        		rsCount.next();
                        		firmaCorredoraBase64 =  rsCount.getBytes("Contenido");
                        		firmaCorredoraBase64String = Base64.getEncoder().encodeToString(firmaCorredoraBase64);

                        	}
                        }      
                        
                        report.setDocumentBarcodeImage("pdf417", codigoPDF417, "pdf417");
                        report.setDocumentImage(firmaCorredoraBase64String, "firmacorredora");
                        report.setDocumentResultSet(schema, jsonArrayData);

                        // exportar el reporte en formato pdf, se guarda
                        try (InputStream inStreamRpt = report
                                .exportDocument(rs.getString("TipoArchivo"))) {
                        	NombrePDF="CIR_"+ rs.getString("DocumentoIdentidad")+"_"+ rs.getString("CodigoPortafolio") + "_" + fechaHora +  "_" +  contadorArchivos  + ".pdf";
                            final File fileTemp = new File(ruta + NombrePDF);//+ rs.getString("NombreArchivo")
                                   // + contadorArchivos + ".pdf");
                            FileUtils.copyInputStreamToFile(inStreamRpt, fileTemp);
                            if (ContLog ==20  ||   Integer.parseInt(tot)  <= 1000){
                            	System.out.println(tot + "->file: " + NombrePDF);
                            };
                            if (contresumen==100){
        						String Estado="< " + contadorArchivos + " de "+ tot + " >";
        						programa.add(new ResumenGeneracion(secuenciaSolicitud, Estado));
        			        	sql.batch(conn, SQL_UPDATE_SOLICITUD, programa,ResumenGeneracion.class);
        			        	conn.commit();
        			        	contresumen=0;
        					};
                            archivos.add(ruta + NombrePDF);
                        }

                    } catch (final Exception e) {
                        e.printStackTrace();
                    }
                }
                // Nombre y ruta ZIP
                final String zipName = "CIRCULARIZA_"
                        + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".zip";
                final String zip = ruta + zipName;
                //System.out.println("ZIPFILE: " + zip);
                // Crear ZIP
                try (OutputStream os = new FileOutputStream(zip)) {
                    try (ZipOutputStream zos = new ZipOutputStream(os)) {
                    	System.out.println("INICIO COMPRESSFILE: ");
                        for (final String archivo : archivos) {
                            // obtener cada archivo a zipear
                            final String fileName = FilenameUtils.getName(archivo);
                            //System.out.println("COMPRESSFILE: " + fileName);

                            try (InputStream fis = new FileInputStream(archivo)) {
                                zos.putNextEntry(new ZipEntry(fileName));
                                // escribie un archivo en el zip
                                int length;
                                final byte[] buffer = new byte[1024];
                                while ((length = fis.read(buffer)) > 0) {
                                    zos.write(buffer, 0, length);
                                }
                                zos.closeEntry();
                               // System.out.println("COMPRESSFILE: " + archivo + " DONE");
                            }
                            FileUtils.forceDelete(new File(archivo));//Borrar cada arhivo incluido en el zip al terminar.
                        }
                        System.out.println("FIN COMPRESSFILE: ");
                    }
                }
                // actualiza la ruta del archivo y el estado de la solicitud
                ActualizarSolicitud(secuenciaSolicitud, zipName, ruta, contadorArchivos, conn,
                        getJobId());
            }

        }

        System.out.println(":GENERAR CARTAS CIRCULARIZACION MASIVO:  ****** FIN  *******");
        return true;
    }
}
