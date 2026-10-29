package cl.bcs.loaderapp.concrete.socionegocio.proveedor;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import cl.bcs.loaderapp.App.EntradaCargaGeneral;
import cl.bcs.loaderapp.interfaces.FileLoader;
import cl.bcs.loaderapp.pojo.socionegocio.Proveedor;
import cl.bcs.socionegocio.utils.IUP;

public class ProveedorFileLoader implements FileLoader {

	@Override
	public List<Object> parse(EntradaCargaGeneral entrada) {
		List<Object> proveedores = new ArrayList<Object>();

        try (Reader in = new FileReader(entrada.p_CsvFile);) {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.withDelimiter(';').withRecordSeparator('\n').withHeader().withIgnoreSurroundingSpaces(true).withNullString("NULL").parse(in);
            int linea =0;
            for (CSVRecord record : records) {
            	Proveedor pro = new Proveedor();
				pro.Linea = linea;
				pro.IDESTADO = record.get("IDESTADO"); 
				pro.AREAPROVEEDOR = record.get("AREAPROVEEDOR"); 
				pro.IDTIPOBLOQUEO = record.get("IDTIPOBLOQUEO"); 
				pro.TIPOPERSONA = record.get("TIPOPERSONA"); 
				
				pro.TIPODOCUMENTOIDENTIDADASOCIADO = record.get("TIPODOCUMENTOIDENTIDAD"); 
				pro.DOCUMENTOIDENTIDADASOCIADO = record.get("DOCUMENTOIDENTIDAD");
				
				pro.TIPODOCUMENTOIDENTIDAD = "IUP";
				pro.DOCUMENTOIDENTIDAD = IUP.generar(record.get("DOCUMENTOIDENTIDAD"), record.get("TIPODOCUMENTOIDENTIDAD"),record.get("TIPOPERSONA"));
                 
				pro.NOMBRES = record.get("NOMBRES"); 
				pro.APELLIDOPATERNO = record.get("APELLIDOPATERNO"); 
				pro.APELLIDOMATERNO = record.get("APELLIDOMATERNO"); 
				pro.RAZONSOCIAL = record.get("RAZONSOCIAL"); 
				pro.FECHANACIMIENTOCONSTITUCION = record.get("FECHANACIMIENTOCONSTITUCION"); 
				pro.TIPOESTADOCIVIL = record.get("TIPOESTADOCIVIL"); 
				pro.GENERO = record.get("GENERO"); 
				pro.PAISNACIMIENTOCONSTITUCION = record.get("PAISNACIMIENTOCONSTITUCION"); 
				pro.CIUDAD = record.get("CIUDAD"); 
				pro.CALLE = record.get("CALLE"); 
				pro.NUMERO = record.get("NUMERO"); 
				pro.COMPLEMENTO = record.get("COMPLEMENTO"); 
				pro.TELEFONO = record.get("TELEFONO"); 
				pro.EMAIL = record.get("EMAIL"); 
				proveedores.add(pro);
				linea++;
				
            }
            return proveedores;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
	}

}
