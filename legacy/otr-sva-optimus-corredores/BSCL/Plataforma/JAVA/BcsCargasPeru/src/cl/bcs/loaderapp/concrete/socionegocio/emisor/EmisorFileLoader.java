package cl.bcs.loaderapp.concrete.socionegocio.emisor;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import cl.bcs.loaderapp.App.EntradaCargaGeneral;
import cl.bcs.loaderapp.interfaces.FileLoader;
import cl.bcs.loaderapp.pojo.socionegocio.Emisor;
import cl.bcs.loaderapp.utils.LoaderUtils;
import cl.bcs.socionegocio.utils.IUP;

public class EmisorFileLoader implements FileLoader{

	@Override
	public List<Object> parse(EntradaCargaGeneral entrada) {
		 List<Object> emisores = new ArrayList<Object>();
		 
        try (Reader in = new FileReader(entrada.p_CsvFile);) {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.withDelimiter(';').withRecordSeparator('\n').withHeader().withIgnoreSurroundingSpaces(true).parse(in);
            int linea = 1;
            for (CSVRecord record : records) {
            	Emisor emisor = new Emisor();
            	emisor.setLinea(linea);
            	emisor.setIdEmisor(record.get("IdEmisor"));
        		emisor.setCodigoEmisor(record.get("CodigoEmisor"));
        		emisor.setCodigoRegulador(record.get("CodigoRegulador"));
        		emisor.setEmiteIRF(record.get("EmiteIRF"));
        		emisor.setIdEstado(record.get("IdEstado"));
        		emisor.setTipoPersona(record.get("TipoPersona"));
        		
        		emisor.setTipoDocumentoIdentidadAsociado(record.get("TipoDocumentoIdentidad"));
        		emisor.setDocumentoIdentidadAsociado(record.get("DocumentoIdentidad"));
                
        		emisor.setTipoDocumentoIdentidad("IUP");
        		emisor.setDocumentoIdentidad(IUP.generar(record.get("DocumentoIdentidad"), record.get("TipoDocumentoIdentidad"),record.get("TipoPersona")));
                
        		emisor.setRazonSocial(record.get("RazonSocial"));
        		emisor.setPais(record.get("Pais"));
        		emisor.setCiudad(record.get("Ciudad"));
        		emisores.add(emisor);
        		linea++;
            }
        } catch (IOException e) {
			e.printStackTrace();
		}    
		return emisores;
	}

}
