package cl.bcs.loaderapp.concrete.socionegocio.anotacion;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import cl.bcs.loaderapp.App.EntradaCargaGeneral;
import cl.bcs.loaderapp.interfaces.FileLoader;
import cl.bcs.loaderapp.pojo.socionegocio.Anotacion;

public class AnotacionFileLoader implements FileLoader{

	@Override
	public List<Object> parse(EntradaCargaGeneral entrada) {
		List<Object> anotaciones = new ArrayList<Object>();
		 
        try (Reader in = new FileReader(entrada.p_CsvFile);) {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.withDelimiter(';').withRecordSeparator('\n').withHeader().withIgnoreSurroundingSpaces(true).parse(in);

            for (CSVRecord record : records) {
            	Anotacion anotacion = new Anotacion();
            	anotacion.setDocumentoIdentidad(record.get("DocumentoIdentidad"));
            	anotacion.setTipoDocumentoIdentidad(record.get("TipoDocumentoIdentidad"));
            	anotacion.setTipoPersona(record.get("TipoPersona"));
            	anotacion.setTitulo(record.get("Titulo"));
            	anotacion.setDetalle(record.get("Detalle"));
            	anotacion.setFechaAnotacion(record.get("FechaAnotacion"));
            	anotacion.setIdMotivo(record.get("IdMotivo"));
            	anotacion.setIdAmbito(record.get("IdAmbito"));
            	anotacion.setIdRelevancia(record.get("IdRelevancia"));
            	anotacion.setFechaDesde(record.get("FechaDesde"));
            	anotacion.setFechaHasta(record.get("FechaHasta"));
            	anotaciones.add(anotacion);
            }
        } catch (IOException e) {
			e.printStackTrace();
		}    
		return anotaciones;
	}

}
