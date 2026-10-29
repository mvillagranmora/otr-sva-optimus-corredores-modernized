package cl.bcs.plataforma.tests;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;


import cl.bcs.plataforma.pla.utils.PlataformaUtils;

import cl.bcs.socionegocio.cli.CrearCliente.EntradaServicioCrearCliente;
import cl.bcs.socionegocio.cli.CrearCliente.SalidaServicioCrearCliente;
import cl.bcs.socionegocio.cli.CrearCliente;
import cl.bcs.socionegocio.pojo.Enrolamiento;


class TestCrearCliente {
	private static final SimpleDateFormat FORMATO_FECHA = new SimpleDateFormat("yyyy-MM-dd");	
	
	@Test
    void testCrearCliente() throws Exception {
    	try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
    		CrearCliente demo = new CrearCliente(); 		
    		
    		List<Enrolamiento> consultarEnrolamiento = new ArrayList<Enrolamiento>();
    		Enrolamiento consultaEnrolamiento = new Enrolamiento();    		
    		consultaEnrolamiento.IdEnrolamiento = 0L;
    		consultaEnrolamiento.IdEmpresa = 1L;
    		consultaEnrolamiento.IdUsuario = 0L;
    		consultaEnrolamiento.IdTipoEntidad = "CLIENTE";
    		consultaEnrolamiento.IdEntidadEnrolada = 0L;
    		consultaEnrolamiento.IdTipoDocumentoIdentidad = "R";
    		consultaEnrolamiento.IdTipoDocumentoIdentidadAsociado = "R";
    		consultaEnrolamiento.DocumentoIdentidad = "8984817-2";
    		consultaEnrolamiento.IdTipoDocumentoIdentidadAdicional = "R";
    		consultaEnrolamiento.DocumentoIdentidadAdicional = "";
    		consultaEnrolamiento.Tipo = "N";
    		consultaEnrolamiento.Nombres = "PRUEBA NOMBRE";
    		consultaEnrolamiento.ApellidoPaterno = "PRUEBA APELLIDO PATERNO";
    		consultaEnrolamiento.ApellidoMaterno = "PRUEBA APELLIDO MATERNO";
    		consultaEnrolamiento.RazonSocial = "";
    		consultaEnrolamiento.IdPaisResidencia = 44L;
    		consultaEnrolamiento.IdPaisNacimiento = 223L;
    		consultaEnrolamiento.FechaNacimiento = new java.sql.Date(FORMATO_FECHA.parse("1986-11-08").getTime());
    		consultaEnrolamiento.Sexo = "F";
    		consultaEnrolamiento.EstadoCivil = "SO";
    		consultaEnrolamiento.IdCiudad = 54L;
    		consultaEnrolamiento.IdDivisionPais = 0L;
    		consultaEnrolamiento.IdTipoTelefono = "FIJO";
    		consultaEnrolamiento.Telefono = "+56982136987";
    		consultaEnrolamiento.Email = "PRUEBA@GMAIL.COM";
    		consultaEnrolamiento.DireccionCalle = "CALLE PRUEBA";
    		consultaEnrolamiento.DireccionNumero = "123";
    		consultaEnrolamiento.DireccionDepartamento = "";
    		consultaEnrolamiento.FechaCreacion = new java.sql.Timestamp(FORMATO_FECHA.parse("2021-02-03T15:15:09.397Z").getTime());
    		consultaEnrolamiento.FechaModificacion = new java.sql.Timestamp(FORMATO_FECHA.parse("2021-02-03T15:15:09.397Z").getTime());
    		consultaEnrolamiento.IdPersonaConyuge = 0L;
    		consultaEnrolamiento.RegimenConyugal = "";
    		consultaEnrolamiento.CLI_IdCategoriaInversionista = "BALANCEADO";
    		consultaEnrolamiento.CLI_IdTipoCliente = "EXPRESS_TIPO CLIENTE";
    		consultaEnrolamiento.CLI_IdTipoClienteCMF = 36L;
    		consultaEnrolamiento.CLI_IdClaseCliente = "";
    		consultaEnrolamiento.CLI_IdEmpleadoEjecutivo = 1751L;
    		consultaEnrolamiento.CLI_CodigoAdicionalHolding = "";
    		consultaEnrolamiento.CLI_ClienteFinal = "N";
    		consultaEnrolamiento.POR_IdPerfilComercial = "BALANCEADO";
    		consultaEnrolamiento.POR_IdPersonaBrokerExtranjero = 0L;
    		consultaEnrolamiento.EMP_CodigoEmpleado = "";
    		consultaEnrolamiento.EMP_CodigoTipoCargoEmpleado = "";
    		consultaEnrolamiento.EMP_IdEmpleadoSupervisor = 0L;
    		consultaEnrolamiento.EMP_IdSucursal = 0L;
    		consultaEnrolamiento.EMP_FechaInicioCargo = new java.sql.Date(FORMATO_FECHA.parse("0001-01-01").getTime());
    		consultaEnrolamiento.EMP_FechaTerminoCargo = new java.sql.Date(FORMATO_FECHA.parse("0001-01-01").getTime());
    		consultaEnrolamiento.CNT_CodigoInscripcion = "";
    		consultaEnrolamiento.CNT_Emisor = "";
    		consultaEnrolamiento.CNT_Depositos = "";
    		consultaEnrolamiento.CNT_Bolsa = "";
    		consultaEnrolamiento.CNT_Corredora = "";
    		consultaEnrolamiento.CNT_CamaraDeCompensacion = "";
    		consultaEnrolamiento.CNT_Banco = "";
    		consultaEnrolamiento.CNT_Boveda = "";
    		consultaEnrolamiento.CNT_Otros = "";
    		consultaEnrolamiento.PRO_IdAreaProveedor = 0L;
    		consultaEnrolamiento.EMP_CargoIndefinido = "";
    		consultaEnrolamiento.EsRutInterno = "N";
    		consultaEnrolamiento.TipoEnrolamiento = "EXPRESS";
    		consultaEnrolamiento.CLI_AreaCliente = "POR DEFINIR";
    		consultaEnrolamiento.UsaDivisionPais = "N";
    		consultarEnrolamiento.add(consultaEnrolamiento);
    		
    		EntradaServicioCrearCliente entradaCrearCliente = new EntradaServicioCrearCliente();  		
    		entradaCrearCliente.p_IdEmpresa = 1L;
    		entradaCrearCliente.p_IdUsuario = 0L;
    		entradaCrearCliente.p_IdModulo = "SOC";
    		entradaCrearCliente.p_CodigoFormulario = "INGRESAR_CLIENTE_EXPRESS_NATURAL";
    		entradaCrearCliente.p_IdTipoCanal = "OPT";
    		entradaCrearCliente.p_UsaReglasNegocio = "S";
    		entradaCrearCliente.p_ConsultarEnrolamiento = consultarEnrolamiento;
    		
    		SalidaServicioCrearCliente salida = demo.logica(conn, entradaCrearCliente);
					
			assertTrue(salida.P_OUT_REFIDCLIENTE > 0);
    		conn.rollback();
    	}
    }
}