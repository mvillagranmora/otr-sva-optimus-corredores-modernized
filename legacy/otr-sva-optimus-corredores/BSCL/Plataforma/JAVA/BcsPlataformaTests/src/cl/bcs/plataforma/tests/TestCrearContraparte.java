package cl.bcs.plataforma.tests;

import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.List;


import org.junit.jupiter.api.Test;


import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.cnt.CrearContraparte;
import cl.bcs.socionegocio.cnt.CrearContraparte.EntradaServicioCrearContraparte;
import cl.bcs.socionegocio.cnt.CrearContraparte.SalidaServicioCrearContraparte;

import cl.bcs.socionegocio.pojo.Enrolamiento;


class TestCrearContraparte {
	private static final SimpleDateFormat FORMATO_FECHA = new SimpleDateFormat("yyyy-MM-dd");
			
	@Test
    void testCrearContraparte() throws Exception {
    	try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
    		CrearContraparte demo = new CrearContraparte();		
    		
    		List<Enrolamiento> consultarEnrolamiento = new ArrayList<Enrolamiento>();
    		Enrolamiento consultaEnrolamiento = new Enrolamiento();    		
    		consultaEnrolamiento.IdEnrolamiento = 0L;
    		consultaEnrolamiento.IdEmpresa = 1L;
    		consultaEnrolamiento.IdUsuario = 0L;
    		consultaEnrolamiento.IdTipoEntidad = "CONTRAPARTE";
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
    		
    		EntradaServicioCrearContraparte entradaCrearContraparte = new EntradaServicioCrearContraparte();  		
    		entradaCrearContraparte.p_IdEmpresa = 1L;
    		entradaCrearContraparte.p_IdUsuario = 0L;
    		entradaCrearContraparte.p_IdModulo = "SOC";
    		entradaCrearContraparte.p_UsaReglasNegocio = "S";
    		entradaCrearContraparte.p_ConsultarEnrolamiento = consultarEnrolamiento;
    		
    		SalidaServicioCrearContraparte salida = demo.logica(conn, entradaCrearContraparte);
					
			assertTrue(salida.P_OUT_REFIDCONTRAPARTE > 0);
    		conn.rollback();
    	}
    }
}