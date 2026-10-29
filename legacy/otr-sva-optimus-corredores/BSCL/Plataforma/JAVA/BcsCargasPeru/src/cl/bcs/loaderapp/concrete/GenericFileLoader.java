package cl.bcs.loaderapp.concrete;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import cl.bcs.loaderapp.App.EntradaCargaGeneral;
import cl.bcs.loaderapp.interfaces.FileLoader;

public class GenericFileLoader implements FileLoader {

	@Override
	public List<Object> parse(EntradaCargaGeneral entrada) {

		List<Object> lineas = new ArrayList<Object>();
		 
        try (Reader in = new FileReader(entrada.p_CsvFile);) {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.withDelimiter(';').withRecordSeparator('\n').withHeader().withIgnoreSurroundingSpaces(true).parse(in);
            for (CSVRecord record : records) {
            	lineas.add(record);
            }
        } catch (IOException e) {
			e.printStackTrace();
		}    
		return lineas;

	}

}
