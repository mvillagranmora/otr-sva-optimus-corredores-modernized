package cl.bcs.loaderapp.concrete.socionegocio.documento;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import cl.bcs.loaderapp.App.EntradaCargaGeneral;
import cl.bcs.loaderapp.interfaces.FileLoader;
import cl.bcs.loaderapp.pojo.socionegocio.Documento;

public class DocumentoFileLoader implements FileLoader{

	@Override
	public List<Object> parse(EntradaCargaGeneral entrada) {
		 List<Object> documentos = new ArrayList<Object>();
		 
	        try (Reader in = new FileReader(entrada.p_CsvFile);) {
	            Iterable<CSVRecord> records = CSVFormat.DEFAULT.withDelimiter(';').withRecordSeparator('\n').withHeader().withIgnoreSurroundingSpaces(true).parse(in);

	            for (CSVRecord record : records) {
	            	Documento documento = new Documento();
	            	documento.setTipoDocumento(record.get("TipoDocumento"));
	            	documento.setNombreDocumento(record.get("NombreDocumento"));
	            	documento.setNumeroFirmantesEmpresa(record.get("NumeroFirmantesEmpresa"));
	            	documento.setNumeroFirmantesCliente(record.get("NumeroFirmantesCliente"));
	            	documento.setClasificacion(record.get("Clasificacion"));
	            	documento.setValidarFechaVigencia(record.get("ValidarFechaVigencia"));
	            	documento.setCodigoTipoIndicador(record.get("CodigoTipoIndicador"));
	            	documento.setTipoPersona(record.get("TipoPersona"));
	        		documentos.add(documento);
	            }
	        } catch (IOException e) {
				e.printStackTrace();
			}    
			return documentos;
	}

}
