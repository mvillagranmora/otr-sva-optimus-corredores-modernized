package cl.bcs.plataforma.tests;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.per.ModificarPersonaNombreFantasiaMasivo;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.EntradaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.SalidaEjecutarCambiosPersona;

import cl.bcs.utils.SQL;

import static org.junit.jupiter.api.Assertions.*;

public class TestModificarPersonaNombreFantasiaMasivo {
	
	public static final String SELECT_NOMBRES_FANTASIA = "SELECT TOP 1 \"IdNombreFantasia\", \"DescripcionNombreFantasia\"  FROM \"BSCL.SocioNegocio.BD::PER.NombreFantasia\" ORDER BY \"IdNombreFantasia\" DESC";

    public static class NombreFantasia {
        public long IdNombreFantasia;
        public String DescripcionNombreFantasia;
    }

    @Test
    void testModificarPersonaNombreFantasiaMasivo_CambiosValidos() throws Exception {
        try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
            // Crear la instancia del servicio
            ModificarPersonaNombreFantasiaMasivo servicio = new ModificarPersonaNombreFantasiaMasivo();

            SQL sql = new SQL();

            // Obtener datos de prueba desde la base de datos
            List<NombreFantasia> listaNombresFantasia = sql.query(conn, SELECT_NOMBRES_FANTASIA, new HashMap<>(), NombreFantasia.class);
            long idEntidadObtenido = listaNombresFantasia.get(0).IdNombreFantasia;
            String IdEntidadStr = Long.toString(idEntidadObtenido);
            String nombreFantasiaPrevio = listaNombresFantasia.get(0).DescripcionNombreFantasia;

            assertNotNull(nombreFantasiaPrevio, "El nombre de fantasía previo no debería ser nulo");
            assertTrue(!nombreFantasiaPrevio.equals("NuevoNombreFantasia"), "El nombre de fantasía previo es distinto al nombre que se va a actualizar");
            
            
            Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\r\n"
					+ "  \"p_IdEmpresa\": 1,\r\n"
					+ "  \"p_IdUsuario\": 0,\r\n"
					+ "  \"p_IdModulo\": \"PER\",\r\n"
					+ "  \"p_UsaReglasNegocio\": \"S\",\r\n"
					+ "  \"p_RegistroCambio\": [\r\n"
					+ "    { \"CampoModificado\": \"DescripcionNombreFantasia\", \"ValorNuevo\": \"NuevoNombreFantasia\", \"IdEntidad\": " + IdEntidadStr +" }\r\n"
					+ "  ]\r\n"
					+ "}\r\n"
					+ "";
			EntradaEjecutarCambiosPersona Objin = gson.fromJson(json, EntradaEjecutarCambiosPersona.class);
            
            // Crear la entrada con datos de prueba
            //ModificarPersonaNombreFantasiaMasivo.EntradaModificarPersonaNombreFantasiaMasivo entrada = new ModificarPersonaNombreFantasiaMasivo.EntradaModificarPersonaNombreFantasiaMasivo();
            //entrada.p_IdEmpresa = 1L;
            //entrada.p_IdUsuario = 0L;
            //entrada.p_IdModulo = "PER";
            //entrada.p_UsaReglasNegocio = "S";

            // Crear registros de cambio
            //RegistroCambioMasivo cambioNombre = new RegistroCambioMasivo();
            //cambioNombre.IdEntidad = idEntidadObtenido;
            //cambioNombre.CampoModificado = "DescripcionNombreFantasia";
            //cambioNombre.ValorNuevo = "NuevoNombreFantasia";

            // Agregar los cambios a la entrada
            //entrada.p_NombresFantasia.add(cambioNombre);

            // Llamar a la lógica del servicio para modificar el nombre de fantasía;
			SalidaEjecutarCambiosPersona salida = servicio.logica(conn, Objin);

            // Validar que no hubo errores
            assertNotNull(salida, "La salida no debería ser nula");
             assertEquals(0L, salida.P_OUT_ERRORESCONTADOR, "No deberían existir errores en la modificación");

            // Validar que el ID de referencia sea válido
            assertNotNull(salida.P_OUT_REFID, "El ID de referencia no debería ser nulo");
            assertTrue(salida.P_OUT_REFID > 0, "El ID de referencia debería ser mayor a 0");

            // Validar que el cambio se realizó en la base de datos
            List<NombreFantasia> listaNombresPostCambio = sql.query(conn, SELECT_NOMBRES_FANTASIA, new HashMap<>(), NombreFantasia.class);
            String nombreFantasiaModificado = listaNombresPostCambio.get(0).DescripcionNombreFantasia;

            assertEquals("NuevoNombreFantasia", nombreFantasiaModificado, "El nombre de fantasía debería haberse actualizado");

            // Revertir los cambios realizados en la base de datos
            conn.rollback();
        }
    }

    
	
}
