package cl.bcs.loaderapp.concrete.socionegocio.persona;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import cl.bcs.loaderapp.App.EntradaCargaGeneral;
import cl.bcs.loaderapp.interfaces.FileLoader;
import cl.bcs.loaderapp.pojo.socionegocio.Persona;

public class PersonaFileLoader implements FileLoader {

    @Override
    public List<Object> parse(EntradaCargaGeneral entrada) {

        List<Object> personas = new ArrayList<Object>();

        try (Reader in = new FileReader(entrada.p_CsvFile);) {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.withDelimiter(';').withRecordSeparator('\n').withHeader().withIgnoreSurroundingSpaces(true).parse(in);

            for (CSVRecord record : records) {
                Persona persona = new Persona();
                persona.setTipoPersona(record.get("TipoPersona"));
                persona.setIUP(record.get("IUP"));
                persona.setRUT(record.get("RUT"));
                persona.setNombres(record.get("Nombres"));
                persona.setApellidoPaterno(record.get("ApellidoPaterno"));
                persona.setApellidoMaterno(record.get("ApellidoMaterno"));
                persona.setRazonSocial(record.get("RazonSocial"));
                persona.setPaisNacimientoConstitucion(record.get("PaisNacimientoConstitucion"));
                persona.setPaisNacionalidadPrincipalCasaMatriz(record.get("PaisNacionalidadPrincipalCasaMatriz"));
                persona.setPaisNacionalidadSecundario(record.get("PaisNacionalidadSecundario"));
                persona.setPaisResidenciaPrincipal(record.get("PaisResidenciaPrincipal"));
                persona.setPaisResidenciaSecundario(record.get("PaisResidenciaSecundario"));
                persona.setFechaNacimientoConstitucion(record.get("FechaNacimientoConstitucion"));
                persona.setGenero(record.get("Genero"));
                persona.setTipoEstadoCivil(record.get("TipoEstadoCivil"));
                persona.setProfesionActividad(record.get("ProfesionActividad"));
                persona.setGradoInstruccion(record.get("GradoInstruccion"));
                persona.setCodigoGiro(record.get("CodigoGiro"));
                persona.setExtranjero(record.get("Extranjero"));
                persona.setFechaDefuncion(record.get("FechaDefuncion"));
                persona.setCodigoCargo(record.get("CodigoCargo"));
                persona.setAnioServicio(record.get("AnioServicio"));
                persona.setIngresoPromedioMensual(record.get("IngresoPromedioMensual"));
                persona.setIdRangoPatrimonio(record.get("IdRangoPatrimonio"));
                persona.setCiudadDistrito(record.get("CiudadDistrito"));
                persona.setCalle(record.get("Calle"));
                persona.setNumero(record.get("Numero"));
                persona.setComplemento(record.get("Complemento"));
                persona.setTelefono(record.get("Telefono"));
                persona.setEmail(record.get("Email"));
                persona.setNombreCortoCategoriaInversionista(record.get("NombreCortoCategoriaInversionista"));
                persona.setTipoCliente(record.get("TipoCliente"));
                persona.setSegmentoEconomico(record.get("SegmentoEconomico"));
                persona.setIUP_Ejecutivo(record.get("IUP_Ejecutivo"));
                persona.setIUPIntermediario(record.get("IUPIntermediario"));
                persona.setSucursalCliente(record.get("SucursalCliente"));
                persona.setEstadoCliente(record.get("EstadoCliente"));
                persona.setFechaBloqueo(record.get("FechaBloqueo"));
                persona.setCodigoTipoBloqueo(record.get("CodigoTipoBloqueo"));
                persona.setComentarioBloqueo(record.get("ComentarioBloqueo"));
                persona.setNombreBanco(record.get("NombreBanco"));
                persona.setTipoCuenta(record.get("TipoCuenta"));
                persona.setIdMoneda(record.get("IdMoneda"));
                persona.setNumeroCuentaBancaria(record.get("NumeroCuentaBancaria"));
                persona.setTipoIdentificadorCuenta(record.get("TipoIdentificadorCuenta"));
                persona.setIdentificadorCuenta(record.get("IdentificadorCuenta"));
                persona.setPlazaDistrito(record.get("PlazaDistrito"));
                persona.setCodigoPortafolio(record.get("CodigoPortafolio"));
                persona.setDescripcionPortafolio(record.get("DescripcionPortafolio"));
                persona.setCodigoProposito(record.get("CodigoProposito"));
                persona.setAliasPortafolio(record.get("AliasPortafolio"));
                persona.setEstadoPortafolio(record.get("EstadoPortafolio"));
                persona.setFechaBloqueoPortafolio(record.get("FechaBloqueoPortafolio"));
                persona.setCodigoTipoBloqueoPortafolio(record.get("CodigoTipoBloqueoPortafolio"));
                persona.setComentarioBloqueoPortafolio(record.get("ComentarioBloqueoPortafolio"));
                personas.add(persona);
            }
            return personas;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
