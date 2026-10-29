package cl.bcs.scheduler.notifications.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;

import com.itextpdf.text.Document;
import com.itextpdf.text.html.simpleparser.HTMLWorker;
import com.itextpdf.text.pdf.PdfWriter;

import cl.bcs.scheduler.notifications.pojo.ArchivoAdjunto;

 

public class FileGenerator {
	
	/**
	 * @param body
	 * @param fileName
	 * @param passwordUser
	 * @param passwordOwner
	 * @return
	 */
	public static ArchivoAdjunto pdfGenerateFromHtml(final String body,String fileName, final String passwordUser, final String passwordOwner)
	{
		try {
 			String filenameWE = removeExtension(fileName);
			String  x = StringEscapeUtils.unescapeJavaScript(body);
			System.out.println(body);
			
			fileName = filenameWE.concat(".pdf");

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		    Document document = new Document();
		    PdfWriter writer = PdfWriter.getInstance(document, outputStream);
 		    writer.setEncryption(passwordUser.getBytes(), passwordOwner.getBytes(),
            PdfWriter.ALLOW_PRINTING, PdfWriter.ENCRYPTION_AES_128);
 		    
		    writer.close();
		    document.open();
		    HTMLWorker htmlWorker = new HTMLWorker(document);
		    htmlWorker.parse(new StringReader(x));
		    document.close();
		    byte[] bytes = outputStream.toByteArray();
		    ArchivoAdjunto pdf = new ArchivoAdjunto(); 
		    pdf.setNombre(fileName);
		    pdf.setArchivo(bytes);
		    
		    return pdf;
		} catch (Exception e) {
			e.printStackTrace();
		    return null;
		}
	}
	
	/**
	 * @param path
	 * @param fileName
	 * @param file
	 * @return
	 */
	public static String fileCreateByBlob(final String path, final String fileName,final Blob file)
	{
		try {

			final InputStream in = file.getBinaryStream();
			try (final OutputStream out = new FileOutputStream(fileName)) {
 				final byte[] buff = new byte[4096]; // how much of the blob to read/write at a time
				int len = 0;
				while ((len = in.read(buff)) != -1) {
					out.write(buff, 0, len);
				}
				
			}
			return fileName;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * @param filename
	 * @return
	 */
	public static String removeExtension(String filename)
	{
 		String[] filenameaux = filename.replace(".", "-").split("-");
 		return filenameaux[0];
	}
	 
	//Crea directorios
	/**
	 * @param dir
	 * @return
	 */
	public static String mkdirs(String dir)
	{
		File fil = new File(dir);
		 if (!fil.exists()) {
	            if (fil.mkdir()) {
	                System.out.println("Directory is created!");
	            } else {
	                System.out.println("Failed to create directory!");
	            }
	        }
		return fil.getAbsolutePath()+"\\";
	}

}
