package cl.bcs.plataforma.tests;

import static org.junit.Assert.fail;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Test;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.EntradaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.SalidaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.ModificarPersonaTelefonoMasivo;
import cl.bcs.socionegocio.per.pojo.RegistroCambioMasivo;
import cl.bcs.utils.SQL;

import static org.junit.jupiter.api.Assertions.*;

public class TestModificarPersonaTelefonoMasivo {
	
	String SELECT_IDS_TELEFONOS = "SELECT TOP 2 \"IdTelefono\", \"Numero\", \"CodigoProposito\"  FROM \"BSCL.SocioNegocio.BD::PER.Telefono\" ORDER BY \"IdTelefono\" DESC";
	
	public static class IdTelefono {
		public long IdTelefono;
		public String Numero;
		public String CodigoProposito;
	}
	
	@Test
    void testModificarPersonaTelefonoMasivo_CambiosMultiplesTelefonos() throws Exception {
        try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
            // Crear la instancia del servicio
            ModificarPersonaTelefonoMasivo modificarPersonaTelefonoMasivo = new ModificarPersonaTelefonoMasivo();
            SQL sql = new SQL();
            
            
            List<IdTelefono> listaTelefonos = sql.query(conn, SELECT_IDS_TELEFONOS, new HashMap<String,Object>(), IdTelefono.class);
            
            if (listaTelefonos.size() < 2) {
                fail("No se encontraron suficientes teléfonos en la base de datos para la prueba masiva (se requieren al menos 2).");
            }
            
            long IdTelefono1 = listaTelefonos.get(0).IdTelefono;
            String NumeroObtenido1 =  listaTelefonos.get(0).Numero;
	        String PropositoObtenido1 =  listaTelefonos.get(0).CodigoProposito;
	        assertTrue(NumeroObtenido1 != "123456789", "El numero previo 1 es distinto al numero que se va a actualizar");
	        assertTrue(PropositoObtenido1 != "PERS", "El numero previo 1 es distinto al numero que se va a actualizar");
	        
            long IdTelefono2 = listaTelefonos.get(1).IdTelefono;
            String NumeroObtenido2 =  listaTelefonos.get(1).Numero;
	        String PropositoObtenido2 =  listaTelefonos.get(1).CodigoProposito;
	        assertTrue(NumeroObtenido2 != "987654321", "El numero previo 2 es distinto al numero que se va a actualizar");
	        assertTrue(PropositoObtenido2 != "EMP", "El numero previo 2 es distinto al numero que se va a actualizar");
            
            // Crear la entrada con datos de prueba
            EntradaEjecutarCambiosPersona entrada = new EntradaEjecutarCambiosPersona();
            entrada.p_IdEmpresa = 1L;
            entrada.p_IdUsuario = 0L;
            entrada.p_IdModulo = "PER";
            entrada.p_UsaReglasNegocio = "S";
            
            // Crear registros de cambio masivo para el primer teléfono
            RegistroCambioMasivo cambioNumeroTel1 = new RegistroCambioMasivo();
            cambioNumeroTel1.IdEntidad = IdTelefono1;
            cambioNumeroTel1.CampoModificado = "Numero";
            cambioNumeroTel1.ValorNuevo = "123456789";
            
            RegistroCambioMasivo cambioPropositoTel1 = new RegistroCambioMasivo();
            cambioPropositoTel1.IdEntidad = IdTelefono1;
            cambioPropositoTel1.CampoModificado = "CodigoProposito";
            cambioPropositoTel1.ValorNuevo = "PERS";

            // Crear registros de cambio masivo para el segundo teléfono
            RegistroCambioMasivo cambioNumeroTel2 = new RegistroCambioMasivo();
            cambioNumeroTel2.IdEntidad = IdTelefono2;
            cambioNumeroTel2.CampoModificado = "Numero";
            cambioNumeroTel2.ValorNuevo = "987654321";
            
            RegistroCambioMasivo cambioPropositoTel2 = new RegistroCambioMasivo();
            cambioPropositoTel2.IdEntidad = IdTelefono2;
            cambioPropositoTel2.CampoModificado = "CodigoProposito";
            cambioPropositoTel2.ValorNuevo = "EMP";

            // Agregar todos los cambios a la entrada
            entrada.p_RegistroCambio.add(cambioNumeroTel1);
            entrada.p_RegistroCambio.add(cambioPropositoTel1);
            entrada.p_RegistroCambio.add(cambioNumeroTel2);
            entrada.p_RegistroCambio.add(cambioPropositoTel2);

            // Llamar a la lógica del servicio para modificar teléfonos de manera masiva
            SalidaEjecutarCambiosPersona salida = modificarPersonaTelefonoMasivo.logica(conn, entrada);

            // Validar la salida
            assertNotNull(salida, "La salida no debería ser nula");
            // Si no hay errores, el contador debería ser 0
            assertEquals(0L, salida.P_OUT_ERRORESCONTADOR, "No deberían existir errores en la modificación masiva");
            
            // Validar que el ID de referencia sea válido
            assertNotNull(salida.P_OUT_REFID, "El ID de referencia no debería ser nulo");
            assertTrue(salida.P_OUT_REFID > 0, "El ID de referencia debería ser mayor a 0");
            
            // Revertir los cambios realizados en la BD (para no afectar el entorno)
            conn.rollback();
        }
    }
    
}
