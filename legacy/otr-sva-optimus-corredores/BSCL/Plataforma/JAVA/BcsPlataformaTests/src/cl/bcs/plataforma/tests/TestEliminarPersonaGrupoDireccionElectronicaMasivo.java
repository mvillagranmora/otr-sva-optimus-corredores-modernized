package cl.bcs.plataforma.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Test;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;

import cl.bcs.socionegocio.per.EliminarPersonaGrupoDireccionElectronicaMasivo;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.EntradaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.SalidaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.pojo.RegistroCambioMasivo;
import cl.bcs.utils.SQL;

public class TestEliminarPersonaGrupoDireccionElectronicaMasivo {

	public static String SELECT_ID_GRUPO_DIRECCION =  "SELECT TOP 3 \"IdGrupoDireccion\" FROM \"BSCL.SocioNegocio.BD::PER.GrupoDireccion\" ORDER BY \"IdGrupoDireccion\" DESC";
	String SELECT_ID_PERSONA = "SELECT TOP 1 \"IdPersona\" FROM BSCL.\"BSCL.SocioNegocio.BD::PER.Persona\" ORDER BY \"IdPersona\" DESC";
		
	public static class IdPersona {
        public Long IdPersona;
    }

	public static class IdGrupoDireccion {
	   public long IdGrupoDireccion;
	}

	    @Test
	    void testEliminarPersonaGrupoDireccionElectronicaMasivo_CambiosValidos() throws Exception {
	        try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
	            // Crear la instancia del servicio
	        	EliminarPersonaGrupoDireccionElectronicaMasivo eliminarPersonaGrupoDireccionElectronicoMasivo = new EliminarPersonaGrupoDireccionElectronicaMasivo();
	            SQL sql = new SQL();

	            // Obtener los datos de prueba desde la base de datos
	            List<IdGrupoDireccion> listaGruposDireccion = sql.query(conn, SELECT_ID_GRUPO_DIRECCION, new HashMap<>(), IdGrupoDireccion.class);
	            List<IdPersona> listaPersonas = sql.query(conn, SELECT_ID_PERSONA, new HashMap<>(), IdPersona.class);
	            long IdPersonaObtenido = listaPersonas.get(0).IdPersona;
	            
	            
	            assertTrue(listaGruposDireccion.size() >= 3, "Debe haber al menos 3 Grupos para ejecutar la prueba masiva");

	            // Crear la entrada con datos de prueba
	            EntradaEjecutarCambiosPersona entrada = new EntradaEjecutarCambiosPersona();
	            entrada.p_IdEmpresa = 1L;
	            entrada.p_IdUsuario = 0L;
	            entrada.p_IdModulo = "PER";
	            entrada.p_UsaReglasNegocio = "S";
	            entrada.p_IdPersona = IdPersonaObtenido;
	            // Crear múltiples registros de cambio
	            for (int i = 0; i < 3; i++) {
	                RegistroCambioMasivo cambioBorradoGrupo = new RegistroCambioMasivo();
	                IdGrupoDireccion grupos = listaGruposDireccion.get(i);
	                
	                
	                cambioBorradoGrupo.IdEntidad = grupos.IdGrupoDireccion;
	                cambioBorradoGrupo.CampoModificado = "Borrado";
	                cambioBorradoGrupo.ValorNuevo = "S";

	                entrada.p_RegistroCambio.add(cambioBorradoGrupo);
	            }

	            // Llamar a la lógica del servicio
	            SalidaEjecutarCambiosPersona salida = eliminarPersonaGrupoDireccionElectronicoMasivo.logica(conn, entrada);

	            // Validar que no hubo errores
	            assertNotNull(salida, "La salida no debería ser nula");
	            assertEquals(0L, salida.P_OUT_ERRORESCONTADOR, "No deberían existir errores en la eliminación masiva");

	            // Validar que los ID de referencia sean válidos
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
