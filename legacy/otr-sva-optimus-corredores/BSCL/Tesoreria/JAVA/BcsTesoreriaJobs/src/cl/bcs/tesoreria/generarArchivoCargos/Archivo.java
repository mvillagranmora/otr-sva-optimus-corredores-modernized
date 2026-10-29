package cl.bcs.tesoreria.generarArchivoCargos;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.opencsv.CSVWriter;

import cl.bcs.tesoreria.ConsultarFormatoArchivo.SalidaFormatoArchivo;


public class Archivo {
	private String 			filename;
	private String 			extension;
	private String 			path;
	private String 			fullpath;
	private String			fieldSeparator;
	private File			file;
	private List<String> 	lines;
	private Long			details;
	private FileWriter 		fileWriter;
	private CSVWriter		csvWriter;
	
	
	public List<String> getLines() {
		return this.lines;
	}
	
	public Long getDetails() {
		return this.details;
	}
	
	public String getFilename() {
		return this.filename;
	}
	
	public String getFullpath() {
		return this.fullpath;
	}
	
	public String setFilename(String filename) {		
		String outputFilename = filename;
		DateFormat formatDate = new SimpleDateFormat("yyyyMMdd_HHmmss");
		String now = formatDate.format(new Date());
		return outputFilename + "_" + now + "." + this.extension;	
	}
	
	public Archivo(SalidaFormatoArchivo formatoArchivo) throws IOException {		
		this.extension = formatoArchivo.FormatoSalida;
		this.filename = this.setFilename(formatoArchivo.NombreArchivoSalida);		
		this.path = formatoArchivo.RutaCompleta;
		this.fullpath = this.path + this.filename;
		this.fieldSeparator = formatoArchivo.SeparadorCampos;
		this.lines = new ArrayList<String>();
		this.details = 0L;
	}
	
	public void open() throws IOException {
		this.file = new File(this.fullpath);
		this.fileWriter = new FileWriter(this.file);
		if(this.extension.equals("CSV"))
			this.csvWriter = new CSVWriter(this.fileWriter, ';');
	}
	
	public void close() throws IOException {
		this.fileWriter.flush();
		this.fileWriter.close();
		if(this.extension.equals("CSV")) 
			this.csvWriter.close();		
	}
	
	public void addLine(List<String> valuesFormatted) throws IOException {
		if(this.extension.equals("TXT"))
			this.TXT(valuesFormatted);				
		else if(this.extension.equals("CSV"))
			this.CSV(valuesFormatted);
		else
			throw new IOException("FORMATO DE SALIDA NO CONFIGURADO/COMPATIBLE."); 
		this.details++;
	}
	
	private void TXT(List<String> valuesFormatted) throws IOException {
		String line = "";
		for (String valueFormatted : valuesFormatted)
		    line = line + valueFormatted + this.fieldSeparator;
		this.lines.add(line);
		this.fileWriter.write(line);
		this.fileWriter.write("\r\n");		
	}
	
	private void CSV(List<String> valuesFormatted) throws IOException {		
		Integer length = valuesFormatted.size() + 1;		
		String[] lines = new String[length];
		String line = "";
		for(int i=0; i<length-1; i++)		
			lines[i] = valuesFormatted.get(i);		
		this.csvWriter.writeNext(lines, false);
		for (String valueFormatted : valuesFormatted)
		    line = line + valueFormatted + this.fieldSeparator;		
		this.lines.add(line);
	}
	
	
}
