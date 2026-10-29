package cl.bcs.loaderapp.concrete.portafolio;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import cl.bcs.loaderapp.App.EntradaCargaGeneral;
import cl.bcs.loaderapp.interfaces.FileLoader;
import cl.bcs.loaderapp.pojo.portafolio.Portafolio;

public class PortafolioFileLoader implements FileLoader {

	@Override
	public List<Object> parse(EntradaCargaGeneral entrada) {
		
		List<Object> portafolios = new ArrayList<Object>();
		 
        try (Reader in = new FileReader(entrada.p_CsvFile);) {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.withDelimiter(';').withRecordSeparator('\n').withHeader().withIgnoreSurroundingSpaces(true).parse(in);

            for (CSVRecord record : records) {
            	Portafolio portafolio= new Portafolio();
            	portafolio.setCodigoPortafolio(record.get("CodigoPortafolio"));
            	portafolio.setProposito(record.get("Proposito"));
            	portafolio.setTipoDocumentoIdentidadCliente(record.get("TipoDocumentoIdentidadCliente"));
            	portafolio.setDocumentoIdentidadCliente(record.get("DocumentoIdentidadCliente"));
            	portafolio.setIdEstado(record.get("IdEstado"));
            	portafolio.setFechaBloqueo(record.get("FechaBloqueo"));
            	portafolio.setComentarioBloqueo(record.get("ComentarioBloqueo"));
            	portafolio.setIdTipoBloqueo(record.get("IdTipoBloqueo"));
            	portafolio.setIdPerfilComercialBaseOrigen(record.get("IdPerfilComercialBaseOrigen"));
            	portafolio.setIdPerfilProductos(record.get("IdPerfilProductos"));
            	portafolio.setIdPerfilCobros(record.get("IdPerfilCobros"));
            	portafolio.setDescripcionPortafolio(record.get("DescripcionPortafolio"));
            	portafolio.setIdPerfilLimites(record.get("IdPerfilLimites"));
            	portafolio.setNumeroFicha(record.get("NumeroFicha"));
            	portafolio.setIdNombreFantasia(record.get("IdNombreFantasia"));
            	portafolio.setAlias(record.get("Alias"));
            	portafolio.setTipoDocumentoIdentidadBrokerExtranjero(record.get("TipoDocumentoIdentidadBrokerExtranjero"));
            	portafolio.setDocumentoIdentidadBrokerExtranjero(record.get("DocumentoIdentidadBrokerExtranjero"));
            	portafolios.add(portafolio);
            }
            
        } catch (IOException e) {
			e.printStackTrace();
		}    
		return portafolios;
	}

}
