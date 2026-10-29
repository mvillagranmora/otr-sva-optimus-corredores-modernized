package cl.bcs.loaderapp.concrete.portafolio.cuentacustodio;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import cl.bcs.loaderapp.App.EntradaCargaGeneral;
import cl.bcs.loaderapp.interfaces.FileLoader;
import cl.bcs.loaderapp.pojo.portafolio.CuentaCustodio;

public class CuentaCustodioFileLoader implements FileLoader {

	@Override
	public List<Object> parse(EntradaCargaGeneral entrada) {
		List<Object> cuentascustodio = new ArrayList<Object>();
		 
        try (Reader in = new FileReader(entrada.p_CsvFile);) {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.withDelimiter(';').withRecordSeparator('\n').withHeader().withIgnoreSurroundingSpaces(true).parse(in);

            for (CSVRecord record : records) {
            	CuentaCustodio cuentacustodio= new CuentaCustodio();
            	cuentacustodio.setTipoDocumentoIdentidad(record.get("TipoDocumentoIdentidad"));
            	cuentacustodio.setDocumentoIdentidadCliente(record.get("DocumentoIdentidadCliente"));
            	cuentacustodio.setCodigoPortafolio(record.get("CodigoPortafolio"));
            	cuentacustodio.setIdBoveda(record.get("IdBoveda"));
            	cuentacustodio.setCodigo(record.get("Codigo"));
            	cuentacustodio.setIdEstado(record.get("IdEstado"));
            	cuentacustodio.setIdModeloCustodia(record.get("IdModeloCustodia"));
            	cuentacustodio.setIdTipoCustodia(record.get("IdTipoCustodia"));
            	cuentacustodio.setIdTipoCuentaCustodia(record.get("IdTipoCuentaCustodia"));
            	cuentacustodio.setDocumentoIdentidadPersonaBancoCustodio(record.get("DocumentoIdentidadPersonaBancoCustodio"));
            	cuentacustodio.setDocumentoIdentidadPersonaBancoLiquidador(record.get("DocumentoIdentidadPersonaBancoLiquidador"));
            	cuentascustodio.add(cuentacustodio);
            }
            
        } catch (IOException e) {
			e.printStackTrace();
		}    
		return cuentascustodio;
	}

}
