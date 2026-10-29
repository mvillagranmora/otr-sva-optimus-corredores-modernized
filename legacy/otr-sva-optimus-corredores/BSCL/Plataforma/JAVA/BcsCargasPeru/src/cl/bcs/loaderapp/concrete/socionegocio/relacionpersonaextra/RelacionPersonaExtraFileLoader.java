package cl.bcs.loaderapp.concrete.socionegocio.relacionpersonaextra;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import cl.bcs.loaderapp.App.EntradaCargaGeneral;
import cl.bcs.loaderapp.interfaces.FileLoader;
import cl.bcs.loaderapp.pojo.socionegocio.RelacionPersonaExtra;

public class RelacionPersonaExtraFileLoader implements FileLoader {

	@Override
	public List<Object> parse(EntradaCargaGeneral entrada) {

		List<Object> relacionesExtra = new ArrayList<Object>();

		try (Reader in = new FileReader(entrada.p_CsvFile);){
			Iterable<CSVRecord> records = CSVFormat.DEFAULT.withDelimiter(';').withRecordSeparator('\n').withHeader().withIgnoreSurroundingSpaces(true).parse(in);

			for (CSVRecord record : records) {
				RelacionPersonaExtra relacionExtra= new RelacionPersonaExtra();
				relacionExtra.setRelacion(record.get("Relacion"));
				relacionExtra.setIupCliente(record.get("IupCliente"));
				relacionExtra.setIupRelacion(record.get("IupRelacion"));
				relacionExtra.setFechainicio(record.get("Fechainicio"));
				relacionExtra.setFechaTermino(record.get("FechaTermino"));
				relacionExtra.setPorcentajeParticipacion(record.get("PorcentajeParticipacion"));
				relacionExtra.setFinalidadRelacion(record.get("FinalidadRelacion"));
				relacionExtra.setTipoPersona(record.get("TipoPersona"));
				relacionExtra.setNombres(record.get("Nombres"));
				relacionExtra.setApellidoPaterno(record.get("ApellidoPaterno"));
				relacionExtra.setApellidoMaterno(record.get("ApellidoMaterno"));
				relacionExtra.setRazonSocial(record.get("RazonSocial"));
				relacionExtra.setPaisNacimientoConstitucion(record.get("PaisNacimientoConstitucion"));
				relacionExtra.setPaisNacionalidadPrincipalCasaMatriz(record.get("PaisNacionalidadPrincipalCasaMatriz"));
				relacionExtra.setPaisNacionalidadSecundario(record.get("PaisNacionalidadSecundario"));
				relacionExtra.setPaisResidenciaPrincipal(record.get("PaisResidenciaPrincipal"));
				relacionExtra.setPaisResidenciaSecundario(record.get("PaisResidenciaSecundario"));
				relacionExtra.setFechaNacimientoConstituciOn(record.get("FechaNacimientoConstituciOn"));
				relacionExtra.setGenero(record.get("Genero"));
				relacionExtra.setTipoEstadoCivil(record.get("TipoEstadoCivil"));
				relacionExtra.setProfesionActividad(record.get("ProfesionActividad"));
				relacionExtra.setGradoInstruccion(record.get("GradoInstruccion"));
				relacionExtra.setCodigoGiro(record.get("CodigoGiro"));
				relacionExtra.setExtranjero(record.get("Extranjero"));
				relacionExtra.setFechaDefuncion(record.get("FechaDefuncion"));
				relacionExtra.setCodigoCargo(record.get("CodigoCargo"));
				relacionExtra.setAnioosServicio(record.get("AnioosServicio"));
				relacionExtra.setIngresoPromedioMensual(record.get("IngresoPromedioMensual"));
				relacionExtra.setIdRangoPatrimonio(record.get("IdRangoPatrimonio"));
				relacionExtra.setCiudadDistrito(record.get("CiudadDistrito"));
				relacionExtra.setCalle(record.get("Calle"));
				relacionExtra.setNumero(record.get("Numero"));
				relacionExtra.setComplemento(record.get("Complemento"));
				relacionExtra.setTelefono(record.get("Telefono"));
				relacionExtra.setEmail(record.get("Email"));
				relacionesExtra.add(relacionExtra);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return relacionesExtra;
	}

}
