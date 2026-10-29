package cl.bcs.plataforma.tests;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Test;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.per.CrearPersonaCuentaBancaria;
import cl.bcs.utils.SQL;

import static org.junit.jupiter.api.Assertions.*;

public class TestCrearPersonaCuentaBancaria {
	String SELECT_ID_PERSONA = "SELECT TOP 1 \"IdPersona\" FROM BSCL.\"BSCL.SocioNegocio.BD::PER.Persona\" ORDER BY \"IdPersona\" DESC";
	
	String SELECT_DATA_BANCO = "SELECT TOP 1 \"IdBanco\", \"IdTipoCuentaBancaria\", \"IdMoneda\", \"IdPlaza\"  FROM \"BSCL.SocioNegocio.BD::PER.CuentaBancaria\" ORDER BY \"IdCuentaBancaria\" DESC";
	
	public static class DataBanco{
		public long IdBanco;
		public String IdTipoCuentaBancaria;
		public String IdMoneda;
		public long IdPlaza;
	}
	
    public static class IdPersona {
        public Long IdPersona;
    }

    @Test
    void testCrearPersonaCuentaBancaria_CasoBasico() throws Exception {
        try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
            // Crear la instancia del servicio
            CrearPersonaCuentaBancaria crearPersonaCuentaBancaria = new CrearPersonaCuentaBancaria();
            SQL sql = new SQL();

            // Consultar un ID de persona de prueba desde la base de datos
            List<IdPersona> listaPersonas = sql.query(conn, SELECT_ID_PERSONA, new HashMap<String, Object>(), IdPersona.class);
            long IdPersonaObtenido = listaPersonas.get(0).IdPersona;
            
            List<DataBanco> listaDataBanco = sql.query(conn, SELECT_DATA_BANCO, new HashMap<String, Object>(), DataBanco.class);
            long IdBancoObtenido = listaDataBanco.get(0).IdBanco;
            String IdTipoCuentaBancariaObtenido = listaDataBanco.get(0).IdTipoCuentaBancaria;
            String IdMonedaObtenido = listaDataBanco.get(0).IdMoneda;
            long IdPlazaObtenido = listaDataBanco.get(0).IdPlaza;
            
            // Crear la entrada con datos de prueba
            CrearPersonaCuentaBancaria.EntradaCrearPersonaCuentaBancaria entrada = new CrearPersonaCuentaBancaria.EntradaCrearPersonaCuentaBancaria();
            entrada.p_IdEmpresa = 1L;
            entrada.p_IdUsuario = 0L;
            entrada.p_IdModulo = "PER";
            entrada.p_UsaReglasNegocio = "N"; // No requiere autorización en este caso
            
            CrearPersonaCuentaBancaria.InCuentaBancaria cuentaBancaria = new CrearPersonaCuentaBancaria.InCuentaBancaria();
            cuentaBancaria.IdBanco = IdBancoObtenido; // Id del banco de prueba
            cuentaBancaria.IdTipoCuentaBancaria = IdTipoCuentaBancariaObtenido;
            cuentaBancaria.IdMoneda = IdMonedaObtenido;
            cuentaBancaria.NumeroCuentaBancaria = "123456789";
            cuentaBancaria.IdPersona = IdPersonaObtenido;
            cuentaBancaria.IdEstado = "ACT";
            cuentaBancaria.IdPlaza = IdPlazaObtenido;
            cuentaBancaria.CrearPreferenciaPago = "N";
            cuentaBancaria.CodigoCuentaInterbancario = "";
            cuentaBancaria.EsPac = "";
            cuentaBancaria.CodigoAba = "";
            cuentaBancaria.OtroCodigoBancoExtranjero = "";
            cuentaBancaria.CodigoIban = "";
            cuentaBancaria.OtroCodigoCuentaExtranjero = "";
            
            entrada.p_InCuentaBancaria.add(cuentaBancaria);

            // Llamar a la lógica del servicio para crear la cuenta bancaria
            CrearPersonaCuentaBancaria.SalidaCrearPersonaCuentaBancaria salida = crearPersonaCuentaBancaria.logica(conn, entrada);

            // Validar la salida
            assertNotNull(salida, "La salida no debería ser nula");
            assertEquals(0L, salida.P_OUT_ERRORESCONTADOR, "No deberían existir errores en la creación de la cuenta bancaria");

            // Validar que se creó al menos una cuenta bancaria
            assertNotNull(salida.P_OUT_CUENTABANCARIA, "La lista de cuentas bancarias no debería ser nula");
            assertFalse(salida.P_OUT_CUENTABANCARIA.isEmpty(), "La lista de cuentas bancarias no debería estar vacía");

            // Validar los datos de la cuenta bancaria creada
            CrearPersonaCuentaBancaria.OutCuentaBancaria cuentaCreada = salida.P_OUT_CUENTABANCARIA.get(0);
            assertEquals(cuentaBancaria.IdMoneda, cuentaCreada.IdMoneda, "La moneda de la cuenta creada debería coincidir");
            assertEquals(cuentaBancaria.IdPersona, cuentaCreada.IdPersona, "El ID de persona debería coincidir");

            // Revertir cambios
            conn.rollback();
        }
    }
}
