package cl.bcs.loaderapp.concrete.socionegocio.origenfondos;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import cl.bcs.loaderapp.App.EntradaCargaGeneral;
import cl.bcs.loaderapp.interfaces.FileLoader;
import cl.bcs.loaderapp.pojo.socionegocio.OrigenFondo;

public class OrigenFondoFileLoader implements FileLoader {

	@Override
	public List<Object> parse(EntradaCargaGeneral entrada) {
		List<Object> origenes = new ArrayList<Object>();
		 
        try (Reader in = new FileReader(entrada.p_CsvFile);) {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.withDelimiter(';').withRecordSeparator('\n').withHeader().withIgnoreSurroundingSpaces(true).parse(in);

            for (CSVRecord record : records) {
            	OrigenFondo origenFondo = new OrigenFondo();
            	/*
            	origenFondo.setDocumentoIdentidad(record.get("DocumentoIdentidad"));
            	origenFondo.setTipoDocumentoIdentidad(record.get("TipoDocumentoIdentidad"));
            	origenFondo.setTipoPersona(record.get("TipoPersona"));
            	*/
            	origenFondo.setDocumentoIdentidad(record.get("IUP"));
            	origenFondo.setTipoDocumentoIdentidad("IUP");
            	
            	origenFondo.setIdTipoOperacionOrigenFondo(record.get("IdTipoOperacionOrigenFondo"));
            	origenFondo.setFechaDeclaracion(record.get("FechaDeclaracion"));
            	origenFondo.setComentario(record.get("Comentario"));
            	origenFondo.setRemuneraciones(record.get("Remuneraciones"));
            	origenFondo.setAhorros(record.get("Ahorros"));
            	origenFondo.setDesembolsoPrestamo(record.get("DesembolsoPrestamo"));
            	origenFondo.setUtilidadesDividendosEmpresa(record.get("UtilidadesDividendosEmpresa"));
            	origenFondo.setVentaMisAcciones(record.get("VentaMisAcciones"));
            	origenFondo.setVentaInmueble(record.get("VentaInmueble"));
            	origenFondo.setDescripcionOtros(record.get("DescripcionOtros"));
            	origenFondo.setOtros(record.get("Otros"));
            	
            	origenes.add(origenFondo);
            }
        } catch (IOException e) {
			e.printStackTrace();
		}    
		return origenes;
	}

}
