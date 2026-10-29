package cl.bcs.loaderapp.concrete.socionegocio.cliente;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import cl.bcs.loaderapp.App.EntradaCargaGeneral;
import cl.bcs.loaderapp.interfaces.FileLoader;
import cl.bcs.loaderapp.pojo.socionegocio.Cliente;
import cl.bcs.socionegocio.utils.IUP;

public class ClienteFileLoader implements FileLoader {
	
	@Override
	public List<Object> parse(EntradaCargaGeneral entrada) {
		 List<Object> clientes = new ArrayList<Object>();

	        try (Reader in = new FileReader(entrada.p_CsvFile);) {
	            Iterable<CSVRecord> records = CSVFormat.DEFAULT.withDelimiter(';').withRecordSeparator('\n').withHeader().withIgnoreSurroundingSpaces(true).parse(in);
	            int count = 0;
	            for (CSVRecord record : records) {
	                Cliente cliente= new Cliente();
	                cliente.setLinea(count);
	                cliente.setTipoPersona(record.get("TipoPersona"));
	                //generar IUP
	                cliente.setTipoDocumentoIdentidadAsociado(record.get("TipoDocumentoIdentidad"));
	                cliente.setDocumentoIdentidadAsociado(record.get("DocumentoIdentidad"));
	                
	                cliente.setTipoDocumentoIdentidad("IUP");
	                cliente.setDocumentoIdentidad(IUP.generar(record.get("DocumentoIdentidad"), record.get("TipoDocumentoIdentidad"),record.get("TipoPersona")));
	                
	                cliente.setRUT(record.get("RUT"));
	                cliente.setNombres(record.get("Nombres"));
	                cliente.setApellidoPaterno(record.get("ApellidoPaterno"));
	                cliente.setApellidoMaterno(record.get("ApellidoMaterno"));
	                cliente.setRazonSocial(record.get("RazonSocial"));
	                cliente.setPaisNacimientoConstitucion(record.get("PaisNacimientoConstitucion"));
	                cliente.setPaisNacionalidadPrincipalCasaMatriz(record.get("PaisNacionalidadPrincipalCasaMatriz"));
	                cliente.setPaisNacionalidadSecundario(record.get("PaisNacionalidadSecundario"));
	                cliente.setPaisResidenciaPrincipal(record.get("PaisResidenciaPrincipal"));
	                cliente.setPaisResidenciaSecundario(record.get("PaisResidenciaSecundario"));
	                cliente.setFechaNacimientoConstitucion(record.get("FechaNacimientoConstitucion"));
	                cliente.setGenero(record.get("Genero"));
	                cliente.setTipoEstadoCivil(record.get("TipoEstadoCivil"));
	                cliente.setProfesionActividad(record.get("ProfesionActividad"));
	                cliente.setGradoInstruccion(record.get("GradoInstruccion"));
	                cliente.setCodigoGiro(record.get("CodigoGiro"));
	                cliente.setExtranjero(record.get("Extranjero"));
	                cliente.setFechaDefuncion(record.get("FechaDefuncion"));
	                cliente.setCodigoTipoCargoPersona(record.get("CodigoTipoCargoPersona"));
	                cliente.setAnioServicio(record.get("AnioServicio"));
	                cliente.setIngresoPromedioMensual(record.get("IngresoPromedioMensual"));
	                cliente.setCodigoRangoPatrimonio(record.get("CodigoRangoPatrimonio"));
	                //viene el ubigeo
	                //cliente.setCiudadDistrito(record.get("CiudadDistrito"));
	                //cliente.setDistrito(record.get("Distrito"));
	                cliente.setCiudadDistrito(record.get("Ubigeo"));
	                cliente.setDistrito("");
	                
	                cliente.setCalle(record.get("Calle"));
	                cliente.setNumero(record.get("Numero"));
	                cliente.setComplemento(record.get("Complemento"));
	                cliente.setTelefono(record.get("Telefono"));
	                cliente.setEmail(record.get("Email"));
	                cliente.setNombreCortoCategoriaInversionista(record.get("NombreCortoCategoriaInversionista"));
	                cliente.setTipoCliente(record.get("TipoCliente"));
	                
	                //viene iup del asesor
	                //cliente.setDocumentoIdentidadAsesor(record.get("DocumentoIdentidadAsesor"));
	                cliente.setDocumentoIdentidadAsesor(record.get("IUP_Ejecutivo"));
	                
	                cliente.setSucursalCliente(record.get("SucursalCliente"));
	                cliente.setEstadoCliente(record.get("EstadoCliente"));
	                cliente.setFechaBloqueo(record.get("FechaBloqueo"));
	                cliente.setCodigoTipoBloqueo(record.get("CodigoTipoBloqueo"));
	                cliente.setComentarioBloqueo(record.get("ComentarioBloqueo"));
	                cliente.setNombreBanco(record.get("NombreBanco"));
	                cliente.setTipoCuenta(record.get("TipoCuenta"));
	                cliente.setIdMoneda(record.get("IdMoneda"));
	                cliente.setNumeroCuentaBancaria(record.get("NumeroCuentaBancaria"));
	                cliente.setTipoIdentificadorCuenta(record.get("TipoIdentificadorCuenta"));
	                cliente.setIdentificadorCuenta(record.get("IdentificadorCuenta"));
	                clientes.add(cliente);
	                count++;
	            }
	            return clientes;
	        } catch (IOException e) {
	            e.printStackTrace();
	            return null;
	        }
	}
	
}
