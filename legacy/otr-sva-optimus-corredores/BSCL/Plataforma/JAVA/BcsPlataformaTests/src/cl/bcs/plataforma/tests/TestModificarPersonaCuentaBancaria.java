package cl.bcs.plataforma.tests;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Test;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.per.ModificarPersonaCuentaBancaria;
import cl.bcs.utils.SQL;
import static org.junit.jupiter.api.Assertions.*;

public class TestModificarPersonaCuentaBancaria {
	
public static String SELECT_ID_CUENTA_BANCARIA = "SELECT TOP 1 \"IdCuentaBancaria\", \"NumeroCuentaBancaria\" FROM \"BSCL.SocioNegocio.BD::PER.CuentaBancaria\" ORDER BY \"IdCuentaBancaria\" DESC";
    
    public static class IdCuentaBancaria {
        public long IdCuentaBancaria;
        public String NumeroCuentaBancaria;
    }

    @Test
    void testModificarPersonaCuentaBancaria_CambiosValidos() throws Exception {
        try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
            // Crear la instancia del servicio
            ModificarPersonaCuentaBancaria modificarPersonaCuentaBancaria = new ModificarPersonaCuentaBancaria();
            SQL sql = new SQL();
           
            // Obtener los datos de prueba desde la base de datos
            List<IdCuentaBancaria> listaCuentasBancarias = sql.query(conn, SELECT_ID_CUENTA_BANCARIA, new HashMap<>(), IdCuentaBancaria.class);
            long IdCuentaBancariaObtenido = listaCuentasBancarias.get(0).IdCuentaBancaria;
            String NumeroCuentaObtenido = listaCuentasBancarias.get(0).NumeroCuentaBancaria;

            // Validar que el número de cuenta es distinto al que se va a actualizar
            assertTrue(!"1234567890".equals(NumeroCuentaObtenido), "El número previo es distinto al número que se va a actualizar");

            // Crear la entrada con datos de prueba
            ModificarPersonaCuentaBancaria.EntradaModificarPersonaCuentaBancaria entrada = new ModificarPersonaCuentaBancaria.EntradaModificarPersonaCuentaBancaria();
            entrada.p_IdEmpresa = 1L;
            entrada.p_IdUsuario = 0L;
            entrada.p_IdModulo = "PER";
            entrada.p_UsaReglasNegocio = "S";
            entrada.p_IdCuentaBancaria = IdCuentaBancariaObtenido;

            // Crear registros de cambio
            ModificarPersonaCuentaBancaria.RegistroCambio cambioNumeroCuenta = new ModificarPersonaCuentaBancaria.RegistroCambio();
            cambioNumeroCuenta.CampoModificado = "NumeroCuentaBancaria";
            cambioNumeroCuenta.ValorNuevo = "1234567890";

            // Agregar los cambios a la entrada
            entrada.p_RegistroCambio.add(cambioNumeroCuenta);

            // Llamar a la lógica del servicio
            ModificarPersonaCuentaBancaria.SalidaModificarPersonaCuentaBancaria salida = modificarPersonaCuentaBancaria.logica(conn, entrada);

            // Validar que no hubo errores
            assertNotNull(salida, "La salida no debería ser nula");
            assertEquals(0L, salida.P_OUT_ERRORESCONTADOR, "No deberían existir errores en la modificación");
            
            // Validar que el ID de referencia sea válido
            assertNotNull(salida.P_OUT_REFID, "El ID de referencia no debería ser nulo");
            assertTrue(salida.P_OUT_REFID > 0, "El ID de referencia debería ser mayor a 0");

            // Validar los mensajes de salida
            assertNotNull(salida.P_OUT_MENSAJES, "La lista de mensajes no debería ser nula");
            assertTrue(salida.P_OUT_MENSAJES.isEmpty(), "No deberían existir mensajes de error en la salida");

            // Revertir los cambios realizados en la BD
            conn.rollback();
        }
    }
	
}
