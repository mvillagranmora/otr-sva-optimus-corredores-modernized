package cl.bcs.plataforma.tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


import org.junit.jupiter.api.Test;


import cl.bcs.plataforma.pla.utils.PlataformaUtils;

import cl.bcs.socionegocio.per.CrearPersona;
import cl.bcs.socionegocio.per.CrearPersona.EntradaServicioCrearPersona;
import cl.bcs.socionegocio.per.CrearPersona.SalidaServicioCrearPersona;

import cl.bcs.socionegocio.pojo.Enrolamiento;


class TestCrearPersona {
	private static final SimpleDateFormat FORMATO_FECHA = new SimpleDateFormat("yyyy-MM-dd");	
	
	@Test
    void testCrearPersona() throws Exception {
    	try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
    		CrearPersona demo = new CrearPersona(); 		
    		
    		List<Enrolamiento> consultarEnrolamiento = new ArrayList<Enrolamiento>();
    		Enrolamiento consultaEnrolamiento = new Enrolamiento();
    		consultaEnrolamiento.IdEnrolamiento = 0L;
    		consultaEnrolamiento.IdEmpresa = 1L;
    		consultaEnrolamiento.IdUsuario = 0L;
    		consultaEnrolamiento.IdTipoEntidad = "PERSONA";
    		consultaEnrolamiento.IdEntidadEnrolada = 0L;
    		consultaEnrolamiento.IdTipoDocumentoIdentidad = "R";
    		consultaEnrolamiento.IdTipoDocumentoIdentidadAsociado = "R";
    		consultaEnrolamiento.DocumentoIdentidad = "23897602-2";
    		consultaEnrolamiento.IdTipoDocumentoIdentidadAdicional = "";
    		consultaEnrolamiento.DocumentoIdentidadAdicional = "";
    		consultaEnrolamiento.Tipo = "N";
    		consultaEnrolamiento.Nombres = "PRUEBA NOMBRE";
    		consultaEnrolamiento.ApellidoPaterno = "PRUEBA APELLIDO PATERNO";
    		consultaEnrolamiento.ApellidoMaterno = "PRUEBA APELLIDO MATERNO";
    		consultaEnrolamiento.RazonSocial = "";
    		consultaEnrolamiento.IdPaisNacimiento = 223L;
    		consultaEnrolamiento.FechaNacimiento = new java.sql.Date(FORMATO_FECHA.parse("1986-11-08").getTime());
    		consultaEnrolamiento.Sexo = "M";
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
    		consultaEnrolamiento.CLI_IdCategoriaInversionista = "";
    		consultaEnrolamiento.CLI_IdTipoCliente = "";
    		consultaEnrolamiento.CLI_IdClaseCliente = "";
    		consultaEnrolamiento.CLI_IdEmpleadoEjecutivo = 0L;
    		consultaEnrolamiento.CLI_CodigoAdicionalHolding = "";
    		consultaEnrolamiento.POR_IdPerfilComercial = "";
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
    		consultarEnrolamiento.add(consultaEnrolamiento);
    		
    		EntradaServicioCrearPersona entradaCrearPersona = new EntradaServicioCrearPersona();  		
    		entradaCrearPersona.p_IdEmpresa = 1L;
    		entradaCrearPersona.p_IdUsuario = 0L;
    		entradaCrearPersona.p_IdModulo = "SOC";
    		entradaCrearPersona.p_UsaReglasNegocio = "S";
    		entradaCrearPersona.p_IdTipoCanal = "S";
    		entradaCrearPersona.p_ConsultarEnrolamiento = consultarEnrolamiento;
    		
    		SalidaServicioCrearPersona salida = demo.logica(conn, entradaCrearPersona);
					
			assertNotNull(salida.P_OUT_IDENROLAMIENTO);
			assertNotNull(salida.P_OUT_IDSOCIONEGOCIO);
			assertTrue(salida.P_OUT_REFID > 0);
    		conn.rollback();
    	}
    }
    	
}