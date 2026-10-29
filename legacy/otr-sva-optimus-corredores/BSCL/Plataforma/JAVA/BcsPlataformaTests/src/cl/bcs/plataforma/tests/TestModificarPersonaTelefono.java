package cl.bcs.plataforma.tests;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Test;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.per.ModificarPersonaTelefono;
import cl.bcs.utils.SQL;
import static org.junit.jupiter.api.Assertions.*;

public class TestModificarPersonaTelefono {

	public static String SELECT_ID_TELEFONO = "SELECT TOP 1 \"IdTelefono\", \"Numero\", \"CodigoProposito\"  FROM \"BSCL.SocioNegocio.BD::PER.Telefono\" ORDER BY \"IdTelefono\" DESC";
	
	public static class IdTelefono {
		public long IdTelefono;
		public String Numero;
		public String CodigoProposito;
	}
	
	
	@Test
	void testModificarPersonaTelefono_CambiosValidos() throws Exception {
	    try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
	        // Crear la instancia del servicio
	        ModificarPersonaTelefono modificarPersonaTelefono = new ModificarPersonaTelefono();
	        SQL sql = new SQL();
	       
	        List<IdTelefono> listaTelefonos = sql.query(conn, SELECT_ID_TELEFONO, new HashMap<String,Object>(), IdTelefono.class);
	        long IdTelefonoObtenido = listaTelefonos.get(0).IdTelefono;
	        String NumeroObtenido =  listaTelefonos.get(0).Numero;
	        String PropositoObtenido =  listaTelefonos.get(0).CodigoProposito;
	        
	        assertTrue(NumeroObtenido != "123456789", "El numero previo es distinto al numero que se va a actualizar");
	        assertTrue(PropositoObtenido != "PERS", "El numero previo es distinto al numero que se va a actualizar");
	        
	        // Crear la entrada con datos de prueba
	        ModificarPersonaTelefono.EntradaModificarPersonaTelefono entrada = new ModificarPersonaTelefono.EntradaModificarPersonaTelefono();
	        entrada.p_IdEmpresa = 1L;
	        entrada.p_IdUsuario = 0L;
	        entrada.p_IdModulo = "PER";
	        entrada.p_UsaReglasNegocio = "S";
	        entrada.p_IdTelefono = IdTelefonoObtenido;
	        
	        // Crear registros de cambio
	        ModificarPersonaTelefono.RegistroCambio cambioNumero = new ModificarPersonaTelefono.RegistroCambio();
	        cambioNumero.CampoModificado = "Numero";
	        cambioNumero.ValorNuevo = "123456789";

	        ModificarPersonaTelefono.RegistroCambio cambioProposito = new ModificarPersonaTelefono.RegistroCambio();
	        cambioProposito.CampoModificado = "CodigoProposito";
	        cambioProposito.ValorNuevo = "PERS";

	        // Agregar los cambios a la entrada
	        entrada.p_RegistroCambio.add(cambioNumero);
	        entrada.p_RegistroCambio.add(cambioProposito);

	        // Llamar a la lógica del servicio para modificar el teléfono
	        ModificarPersonaTelefono.SalidaModificarPersonaTelefono salida = modificarPersonaTelefono.logica(conn, entrada);

	        // Validar que no hubo errores
	        assertNotNull(salida, "La salida no debería ser nula");
	        assertEquals(0L, salida.P_OUT_ERRORESCONTADOR, "No deberían existir errores en la modificación");
	        
	        // Validar que el ID de referencia sea válido
	        assertNotNull(salida.P_OUT_REFID, "El ID de referencia no debería ser nulo");
	        assertTrue(salida.P_OUT_REFID > 0, "El ID de referencia debería ser mayor a 0");
	        
	        // Revertir los cambios realizados en la BD

	        conn.rollback();
	    }
	}
}
