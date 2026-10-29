package cl.bcs.plataforma.tests;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Test;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.per.CrearPersonaTarjetaBancariaMasivo;
import cl.bcs.socionegocio.per.CrearPersonaTarjetaBancariaMasivo.SalidaCrearPersonaTarjetaBancariaMasivo;
import cl.bcs.socionegocio.per.CrearPersonaTarjetaBancariaMasivo.TarjetaBancaria;
import cl.bcs.utils.SQL;

import static org.junit.jupiter.api.Assertions.*;

public class TestCrearPersonaTarjetaBancariaMasivo {
	
	String SELECT_ID_PERSONA = "SELECT TOP 1 \"IdPersona\" FROM BSCL.\"BSCL.SocioNegocio.BD::PER.Persona\" ORDER BY \"IdPersona\" DESC";
	String SELECT_DATA_TARJETA = "SELECT TOP 1 \"IdBanco\" FROM \"BSCL.SocioNegocio.BD::PER.CuentaBancaria\" ORDER BY \"IdCuentaBancaria\" DESC";

    public static class DataTarjeta {
    	public long IdBanco;

    }

    public static class IdPersona {
        public Long IdPersona;
    }

    @Test
    void testCrearPersonaTarjetaBancariaMasivo_CasoMultiple() throws Exception {
        try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
            // Crear la instancia del servicio
            CrearPersonaTarjetaBancariaMasivo servicio = new CrearPersonaTarjetaBancariaMasivo();
            SQL sql = new SQL();

            // Consultar un ID de persona de prueba desde la base de datos
            List<IdPersona> listaPersonas = sql.query(conn, SELECT_ID_PERSONA, new HashMap<>(), IdPersona.class);
            long IdPersonaObtenido = listaPersonas.get(0).IdPersona;

            // Consultar datos de una tarjeta bancaria de prueba
            List<DataTarjeta> listaDataTarjeta = sql.query(conn, SELECT_DATA_TARJETA, new HashMap<>(), DataTarjeta.class);
            long IdBancoObtenido = listaDataTarjeta.get(0).IdBanco;

            // Crear la entrada con datos de prueba para múltiples tarjetas
            CrearPersonaTarjetaBancariaMasivo.EntradaCrearPersonaTarjetaBancariaMasivo entrada = 
                new CrearPersonaTarjetaBancariaMasivo.EntradaCrearPersonaTarjetaBancariaMasivo();
            entrada.p_IdEmpresa = 1L;
            entrada.p_IdUsuario = 0L;
            entrada.p_IdModulo = "PER";
            entrada.p_UsaReglasNegocio = "N";

            // Tarjeta 1
            TarjetaBancaria tarjeta1 = 
                new TarjetaBancaria();
            tarjeta1.IdBanco = IdBancoObtenido;
            tarjeta1.IdTipoTarjetaBancaria = "";
            tarjeta1.NumeroTarjetaBancaria = "987654321";
            tarjeta1.IdPersona = IdPersonaObtenido;
            tarjeta1.FechaVencimiento = "2030-12-30";
            tarjeta1.IdEstado = "ACT";
            tarjeta1.CodigoVerificacion = 0L;
            
            entrada.p_TarjetasBancarias.add(tarjeta1);
            

            // Tarjeta 2
            TarjetaBancaria tarjeta2 = 
                new TarjetaBancaria();
            tarjeta2.IdBanco = IdBancoObtenido;
            tarjeta2.IdTipoTarjetaBancaria = "";
            tarjeta2.NumeroTarjetaBancaria = "987654322";
            tarjeta2.IdPersona = IdPersonaObtenido;
            tarjeta2.FechaVencimiento = "2030-12-30";
            tarjeta2.IdEstado = "ACT";
            tarjeta2.CodigoVerificacion = 0L;
            
            entrada.p_TarjetasBancarias.add(tarjeta2);

            // Tarjeta 3
            TarjetaBancaria tarjeta3 = 
                new TarjetaBancaria();
            tarjeta3.IdBanco = IdBancoObtenido;
            tarjeta3.IdTipoTarjetaBancaria = "";
            tarjeta3.NumeroTarjetaBancaria = "987654323";
            tarjeta3.IdPersona = IdPersonaObtenido;
            tarjeta3.FechaVencimiento = "2030-12-30";
            tarjeta3.IdEstado = "ACT";
            tarjeta3.CodigoVerificacion = 0L;
            
            entrada.p_TarjetasBancarias.add(tarjeta3);

            // Llamar a la lógica del servicio
            SalidaCrearPersonaTarjetaBancariaMasivo salida = servicio.logica(conn, entrada);

            assertNotNull(salida, "La salida no debería ser nula");

            // Validar contador de errores
            assertEquals(0L, salida.P_OUT_ERRORESCONTADOR, "No deberían existir errores en la creación de las tarjetas bancarias");

            // Validar que se generaron referencias (P_OUT_REFID)
            assertNotNull(salida.P_OUT_REFID, "La lista de referencias creadas no debería ser nula");
            assertEquals(3, salida.P_OUT_REFID.size(), "Deberían haberse creado tres referencias de entidades");

            // Validar los mensajes de salida
            assertNotNull(salida.P_OUT_MENSAJES, "La lista de mensajes no debería ser nula");
            assertTrue(salida.P_OUT_MENSAJES.isEmpty(), "No deberían existir mensajes de error en la salida");


            // Revertir cambios
            conn.rollback();
        }
    }
	
}
