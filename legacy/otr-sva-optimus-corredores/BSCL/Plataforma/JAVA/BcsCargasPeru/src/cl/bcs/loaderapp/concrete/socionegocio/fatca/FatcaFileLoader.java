package cl.bcs.loaderapp.concrete.socionegocio.fatca;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import cl.bcs.loaderapp.App.EntradaCargaGeneral;
import cl.bcs.loaderapp.interfaces.FileLoader;
import cl.bcs.loaderapp.pojo.socionegocio.Fatca;

public class FatcaFileLoader implements FileLoader {

	@Override
	public List<Object> parse(EntradaCargaGeneral entrada) {
		List<Object> fatcas = new ArrayList<Object>();
		 
        try (Reader in = new FileReader(entrada.p_CsvFile);) {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.withDelimiter(';').withRecordSeparator('\n').withHeader().withIgnoreSurroundingSpaces(true).withNullString("NULL").parse(in);

            for (CSVRecord record : records) {
            	Fatca fatca = new Fatca();
            	fatca.setIUP( record.get("IUP") );
            	
            	fatca.setDocumentoIdentidad( fatca.getIUP());
            	fatca.setTipoDocumentoIdentidad( "IUP" );
            	
            	fatca.setIdClasificacionFatca(record.get("IDCLASIFICACIONFATCA"));
            	fatca.setIdClasificacionEmpresaFatca(record.get("IDCLASIFICACIONEMPRESAFATCA"));
            	fatca.setNumeroGIIN(record.get("NUMEROGIIN"));
            	fatca.setFechaFirmaFormulario(record.get("FECHAFIRMAFORMULARIO"));
            	fatca.setAutorizacionInformarOrganismo(record.get("AUTORIZACIONINFORMARORGANISMO"));
            	fatca.setTIN1(record.get("TIN1"));
            	fatca.setIdPaisNacionalidad2(record.get("IDPAISNACIONALIDAD2"));
            	fatca.setTIN2(record.get("TIN2"));
            	fatca.setIdPaisNacionalidad3(record.get("IDPAISNACIONALIDAD3"));
            	fatca.setTIN3(record.get("TIN3"));
            	fatca.setIdPaisNacionalidad4(record.get("IDPAISNACIONALIDAD4"));
            	fatca.setTIN4(record.get("TIN4"));
            	fatca.setIdPaisNacionalidad5(record.get("IDPAISNACIONALIDAD5"));
            	fatca.setTIN5(record.get("TIN5"));
            	fatca.setExcepcionResidente(record.get("EXCEPCIONRESIDENTE"));
            	fatca.setExcepcionNacimiento(record.get("EXCEPCIONNACIMIENTO"));
            	fatca.setExcepcionDomicilio(record.get("EXCEPCIONDOMICILIO"));
            	fatca.setExcepcionTelefono(record.get("EXCEPCIONTELEFONO"));
            	fatca.setExcepcionOrdenPermanente(record.get("EXCEPCIONORDENPERMANENTE"));
            	fatca.setExcepcionFirma(record.get("EXCEPCIONFIRMA"));
            	fatca.setParticipacionSociedadesUSA(record.get("PARTICIPACIONSOCIEDADESUSA"));
            	fatca.setOrdenPermanente(record.get("ORDENPERMANENTE"));
            	fatcas.add(fatca);
            }
        } catch (IOException e) {
			e.printStackTrace();
		}    
		return fatcas;
	}

}
