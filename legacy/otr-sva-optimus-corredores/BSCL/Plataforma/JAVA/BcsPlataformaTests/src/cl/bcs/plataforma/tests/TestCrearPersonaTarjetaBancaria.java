package cl.bcs.plataforma.tests;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Test;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.per.CrearPersonaTarjetaBancaria;
import cl.bcs.utils.SQL;

import static org.junit.jupiter.api.Assertions.*;

public class TestCrearPersonaTarjetaBancaria {
	
	String SELECT_ID_PERSONA = "SELECT TOP 1 \"IdPersona\" FROM BSCL.\"BSCL.SocioNegocio.BD::PER.Persona\" ORDER BY \"IdPersona\" DESC";
    String SELECT_DATA_TARJETA = "SELECT TOP 1 \"IdBanco\" FROM \"BSCL.SocioNegocio.BD::PER.CuentaBancaria\" ORDER BY \"IdCuentaBancaria\" DESC";

    public static class DataTarjeta {
        public long IdBanco;
    }

    public static class IdPersona {
        public Long IdPersona;
    }

    @Test
    void testCrearPersonaTarjetaBancaria_CasoUnitario() throws Exception {
        try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
            // Crear la instancia del servicio
            CrearPersonaTarjetaBancaria servicio = new CrearPersonaTarjetaBancaria();
            SQL sql = new SQL();

            // Consultar un ID de persona de prueba desde la base de datos
            List<IdPersona> listaPersonas = sql.query(conn, SELECT_ID_PERSONA, new HashMap<>(), IdPersona.class);
            long IdPersonaObtenido = listaPersonas.get(0).IdPersona;

            // Consultar datos de una tarjeta bancaria de prueba
            List<DataTarjeta> listaDataTarjeta = sql.query(conn, SELECT_DATA_TARJETA, new HashMap<>(), DataTarjeta.class);
            long IdBancoObtenido = listaDataTarjeta.get(0).IdBanco;

            // Crear la entrada con datos de prueba
            CrearPersonaTarjetaBancaria.EntradaCrearPersonaTarjetaBancaria entrada =
                new CrearPersonaTarjetaBancaria.EntradaCrearPersonaTarjetaBancaria();
            entrada.p_IdEmpresa = 1L;
            entrada.p_IdUsuario = 0L;
            entrada.p_IdModulo = "PER";
            entrada.p_UsaReglasNegocio = "N";
            entrada.p_IdBanco = IdBancoObtenido;
            entrada.p_IdTipoTarjetaBancaria = "";
            entrada.p_NumeroTarjetaBancaria = "";
            entrada.p_IdPersona = IdPersonaObtenido;
            entrada.p_FechaVencimiento = "2030-12-30";
            entrada.p_IdEstado = "ACT";
            entrada.p_CodigoVerificacion = 0L;

            // Llamar a la lógica del servicio
            CrearPersonaTarjetaBancaria.SalidaCrearPersonaTarjetaBancaria salida = servicio.logica(conn, entrada);

            // Validar salida
            assertNotNull(salida, "La salida no debería ser nula");

            // Validar contador de errores
            assertEquals(0L, salida.P_OUT_ERRORESCONTADOR, "No deberían existir errores en la creación de la tarjeta bancaria");

            // Validar que se generó referencia (P_OUT_REFID)
            assertNotNull(salida.P_OUT_REFID, "El ID de referencia no debería ser nulo");
            assertTrue(salida.P_OUT_REFID > 0, "El ID de referencia debería ser mayor a 0");

            // Validar los mensajes de salida
            assertNotNull(salida.P_OUT_MENSAJES, "La lista de mensajes no debería ser nula");
            assertTrue(salida.P_OUT_MENSAJES.isEmpty(), "No deberían existir mensajes de error en la salida");

            // Revertir cambios
            conn.rollback();
        }
    }
	
}
