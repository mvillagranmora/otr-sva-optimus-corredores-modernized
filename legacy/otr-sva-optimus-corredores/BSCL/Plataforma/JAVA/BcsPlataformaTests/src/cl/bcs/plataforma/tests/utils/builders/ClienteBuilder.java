package cl.bcs.plataforma.tests.utils.builders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.util.Arrays;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.cli.CrearCliente;
import cl.bcs.socionegocio.cli.CrearCliente.EntradaServicioCrearCliente;
import cl.bcs.socionegocio.cli.CrearCliente.SalidaServicioCrearCliente;
import cl.bcs.socionegocio.pojo.Enrolamiento;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ClienteBuilder {
	@Disabled
	@Test
	void validarCrearCliente() throws Exception{
		try(Connection conn = PlataformaUtils.getQAConnection()){
			SalidaServicioCrearCliente builder = new ClienteBuilder().setTipoPersona("N").build(conn);
			conn.rollback();
			assertNotNull(builder);
			assertEquals(builder.P_OUT_ERRORESDETALLE.size(), 0, builder.P_OUT_ERRORESDETALLE.toString());
			assertTrue(builder.P_OUT_REFIDCLIENTE!=0);
			assertTrue(builder.P_OUT_REFIDPORTAFOLIO!=0);
		}
	}

	private Enrolamiento cliente;
	public ClienteBuilder() {
		Gson gson = Utils.getGsonBuilder();
		String clienteDefecto = "{\"IdEnrolamiento\":0,\"IdEmpresa\":1,\"IdUsuario\":3,\"IdTipoEntidad\":\"CLIENTE\",\"IdEntidadEnrolada\":0,\"IdTipoDocumentoIdentidad\":\"R\",\"IdTipoDocumentoIdentidadAsociado\":\"R\",\"DocumentoIdentidad\":\"6-K\",\"IdTipoDocumentoIdentidadAdicional\":\"R\",\"DocumentoIdentidadAdicional\":\"\",\"Tipo\":\"N\",\"Nombres\":\"CLIENTE\",\"ApellidoPaterno\":\"PRUEBA\",\"ApellidoMaterno\":\"UNITARIA\",\"RazonSocial\":\"\",\"IdPaisNacimiento\":1,\"FechaNacimiento\":\"2010-01-01 03:00:00.000Z\",\"Sexo\":\"M\",\"EstadoCivil\":\"SO\",\"IdCiudad\":173,\"IdDivisionPais\":343,\"IdTipoTelefono\":\"FIJO\",\"Telefono\":\"+56225485952\",\"Email\":\"CHERNANDEZ@BOLSADESANTIAGO.COM\",\"DireccionCalle\":\"ASD\",\"DireccionNumero\":\"123\",\"DireccionDepartamento\":\"\",\"FechaCreacion\":\"2018-11-06 17:21:10.190Z\",\"FechaModificacion\":\"2018-11-06 17:21:10.190Z\",\"IdPersonaConyuge\":0,\"RegimenConyugal\":\"\",\"CLI_IdCategoriaInversionista\":\"ARRIESGADO\",\"CLI_IdTipoCliente\":\"FAMILYOFFICE\",\"CLI_IdClaseCliente\":\"\",\"CLI_IdEmpleadoEjecutivo\":192,\"CLI_CodigoAdicionalHolding\":\"\",\"CLI_ClienteFinal\":\"N\",\"POR_IdPerfilComercial\":\"PRUEBA MODELO ARRIESGADO\",\"POR_IdPersonaBrokerExtranjero\":0,\"EMP_CodigoEmpleado\":\"\",\"EMP_CodigoTipoCargoEmpleado\":\"\",\"EMP_IdEmpleadoSupervisor\":0,\"EMP_IdSucursal\":0,\"EMP_FechaInicioCargo\":\"0001-01-01\",\"EMP_FechaTerminoCargo\":\"0001-01-01\",\"CNT_CodigoInscripcion\":\"\",\"CNT_Emisor\":\"\",\"CNT_Depositos\":\"\",\"CNT_Bolsa\":\"\",\"CNT_Corredora\":\"\",\"CNT_CamaraDeCompensacion\":\"\",\"CNT_Banco\":\"\",\"CNT_Boveda\":\"\",\"CNT_Otros\":\"\",\"PRO_IdAreaProveedor\":0,\"EMP_CargoIndefinido\":\"\",\"EsRutInterno\":\"N\"}";
		cliente = gson.fromJson(clienteDefecto, Enrolamiento.class);
	}
	
	public ClienteBuilder setIdEmpresa(Long idEmpresa) {
		cliente.IdEmpresa = idEmpresa;
		return this;
	}
	
	public ClienteBuilder setRazonSocial(String razonSocial) {
		cliente.RazonSocial = razonSocial;
		cliente.Nombres = "";
		cliente.ApellidoMaterno = "";
		cliente.ApellidoPaterno = "";
		return this;
	}
	
	public ClienteBuilder setIdTipoDocumentoIdentidad(String idTipoDocumentoIdentidad) {
		cliente.IdTipoDocumentoIdentidad = idTipoDocumentoIdentidad;
		return this;
	}
	public ClienteBuilder setDocumentoIdentidad(String documentoIdentidad) {
		cliente.DocumentoIdentidad = documentoIdentidad;
		return this;
	}
	
	public ClienteBuilder setIdTipoDocumentoIdentidadAdicional(String idTipoDocumentoIdentidadAdicional) {
		cliente.IdTipoDocumentoIdentidadAdicional = idTipoDocumentoIdentidadAdicional;
		return this;
	}
	public ClienteBuilder setDocumentoIdentidadAdicional(String documentoIdentidadAdicional) {
		cliente.DocumentoIdentidadAdicional = documentoIdentidadAdicional;
		return this;
	}
	
	public ClienteBuilder setTipoPersona(String tipoPersona) {
		cliente.Tipo = tipoPersona;
		return this;
	}
	
	public ClienteBuilder setIdCategoriaInversionista(String IdCategoriaInversionista) {
		cliente.CLI_IdCategoriaInversionista = IdCategoriaInversionista;
		return this;
	}
	public ClienteBuilder setIdPerfilComercial(String IdPerfilComercial) {
		cliente.POR_IdPerfilComercial = IdPerfilComercial;
		return this;
	}
	
	public ClienteBuilder setIdTipoCliente(String IdTipoCliente) {
		cliente.CLI_IdTipoCliente = IdTipoCliente;
		return this;
	}
	public ClienteBuilder setIdClaseCliente(String IdClaseCliente) {
		cliente.CLI_IdClaseCliente = IdClaseCliente;
		return this;
	}
	public ClienteBuilder seIdPerfilComercial(String IdPerfilComercial) {
		cliente.POR_IdPerfilComercial = IdPerfilComercial;
		return this;
	}
	public ClienteBuilder setEmail(String email) {
		cliente.Email = email;
		return this;
	}
	
	public ClienteBuilder setIdEmpleadoEjecutivo(Long idEmpleadoEjecutivo) {
		cliente.CLI_IdEmpleadoEjecutivo = idEmpleadoEjecutivo;
		return this;
	}
	
	public SalidaServicioCrearCliente build(Connection conn) throws Exception {
		SQL sql = new SQL();
		CrearCliente crearCliente = new CrearCliente();
		EntradaServicioCrearCliente in = new EntradaServicioCrearCliente();
		in.p_IdEmpresa = 1L;
		in.p_IdUsuario = 0L;
		in.p_IdModulo = "PLA";
		in.p_ConsultarEnrolamiento = Arrays.asList(cliente);
		//deshabilitar requerimiento de autorizacion del nuevo cliente
		sql.exec(conn, "UPDATE \"BSCL.Plataforma.BD::PLA.RequiereAutorizacion\" SET \"RequiereAutorizacion\" = 'N' WHERE \"IdConcepto\" IN('ENROLC', 'NUEPOR')");
		sql.exec(conn, "UPDATE \"BSCL.SocioDeNegocio.BD::SOC.ConfiguracionSocioNegocio\" SET \"Valor\" = 'N' WHERE \"CodigoParametro\" = 'CLI.BloqueoPorDefecto'");
		return crearCliente.logica(conn, in);
	}
		
	
	
}
