package cl.bcs.loaderapp.concrete.socionegocio.relacionpersona;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import cl.bcs.loaderapp.App.EntradaCargaGeneral;
import cl.bcs.loaderapp.interfaces.FileLoader;
import cl.bcs.loaderapp.pojo.socionegocio.RelacionPersona;

public class RelacionPersonaFileLoader implements FileLoader {

	@Override
	public List<Object> parse(EntradaCargaGeneral entrada) {

		List<Object> relaciones = new ArrayList<Object>();

		try (Reader in = new FileReader(entrada.p_CsvFile);){
			Iterable<CSVRecord> records = CSVFormat.DEFAULT.withDelimiter(';').withRecordSeparator('\n').withHeader().withIgnoreSurroundingSpaces(true).parse(in);

			for (CSVRecord record : records) {
				RelacionPersona relacion= new RelacionPersona();
				relacion.setRelacion(record.get("Relacion"));
				relacion.setIupCliente(record.get("IupCliente"));
				relacion.setIupRelacion(record.get("IupRelacion"));
				relacion.setTipoPersona(record.get("TipoPersona"));
				relacion.setNombres(record.get("Nombres"));
				relacion.setApellidoPaterno(record.get("ApellidoPaterno"));
				relacion.setApellidoMaterno(record.get("ApellidoMaterno"));
				relacion.setRazonSocial(record.get("RazonSocial"));
				relacion.setPaisNacimientoConstitucion(record.get("PaisNacimientoConstitucion"));
				relacion.setPaisNacionalidadPrincipalCasaMatriz(record.get("PaisNacionalidadPrincipalCasaMatriz"));
				relacion.setPaisNacionalidadSecundario(record.get("PaisNacionalidadSecundario"));
				relacion.setPaisResidenciaPrincipal(record.get("PaisResidenciaPrincipal"));
				relacion.setPaisResidenciaSecundario(record.get("PaisResidenciaSecundario"));
				relacion.setFechaNacimientoConstitucion(record.get("FechaNacimientoConstitucion"));
				relacion.setGenero(record.get("Genero"));
				relacion.setTipoEstadoCivil(record.get("TipoEstadoCivil"));
				relacion.setProfesionActividad(record.get("ProfesionActividad"));
				relacion.setDependenciaLaboral(record.get("DependenciaLaboral"));
				relacion.setGradoInstruccion(record.get("GradoInstruccion"));
				relacion.setCodigoGiro(record.get("CodigoGiro"));
				relacion.setExtranjero(record.get("Extranjero"));
				relacion.setFechaDefuncion(record.get("FechaDefuncion"));
				relacion.setCodigoCargo(record.get("CodigoCargo"));
				relacion.setAniosServicio(record.get("AniosServicio"));
				relacion.setIngresoPromedioMensual(record.get("IngresoPromedioMensual"));
				relacion.setIdRangoPatrimonio(record.get("IdRangoPatrimonio"));
				relacion.setCiudadDistrito(record.get("CiudadDistrito"));
				relacion.setCalle(record.get("Calle"));
				relacion.setNumero(record.get("Numero"));
				relacion.setComplemento(record.get("Complemento"));
				relacion.setTelefono(record.get("Telefono"));
				relacion.setEmail(record.get("Email"));
				relacion.setDatosDeRegistrosPublicos(record.get("DatosDeRegistrosPublicos"));
				relacion.setAsiento(record.get("Asiento"));
				relacion.setFichaoPartida(record.get("FichaoPartida"));
				relaciones.add(relacion);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return relaciones;
	}

}
