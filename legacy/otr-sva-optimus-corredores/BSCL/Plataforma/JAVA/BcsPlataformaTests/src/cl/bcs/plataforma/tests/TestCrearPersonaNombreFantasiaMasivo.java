package cl.bcs.plataforma.tests;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Test;
import cl.bcs.socionegocio.per.pojo.NombreFantasia;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.per.CrearPersonaNombreFantasiaMasivo;
import cl.bcs.utils.SQL;

import static org.junit.jupiter.api.Assertions.*;

public class TestCrearPersonaNombreFantasiaMasivo {
	
	String SELECT_ID_PERSONA = "SELECT TOP 1 \"IdPersona\" FROM BSCL.\"BSCL.SocioNegocio.BD::PER.Persona\" ORDER BY \"IdPersona\" DESC";

    public static class IdPersona {
        public Long IdPersona;
    }

    @Test
    void testCrearPersonaNombreFantasiaMasivo_CasoBasico() throws Exception {
        try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
            // Instancia del servicio
            CrearPersonaNombreFantasiaMasivo servicio = new CrearPersonaNombreFantasiaMasivo();
            SQL sql = new SQL();

            List<IdPersona> listaPersonas = sql.query(conn, SELECT_ID_PERSONA, new HashMap<>(), IdPersona.class);
            Long idPersona = listaPersonas.get(0).IdPersona;

            // Configurar entrada
            CrearPersonaNombreFantasiaMasivo.EntradaCrearPersonaNombreFantasiaMasivo entrada = new CrearPersonaNombreFantasiaMasivo.EntradaCrearPersonaNombreFantasiaMasivo();
            entrada.p_IdEmpresa = 1L;
            entrada.p_IdUsuario = 0L;
            entrada.p_IdModulo = "PER";
            entrada.p_UsaReglasNegocio = "S";

            NombreFantasia nombre1 = new NombreFantasia();
            nombre1.IdInterno = 0L;
            nombre1.IdPersona = idPersona;
            nombre1.DescripcionNombreFantasia = "NombreFantasiaPrueba1";

            NombreFantasia nombre2 = new NombreFantasia();
            nombre2.IdInterno = 1L;
            nombre2.IdPersona = idPersona;
            nombre2.DescripcionNombreFantasia = "NombreFantasiaPrueba2";

            entrada.p_NombresFantasia.add(nombre1);
            entrada.p_NombresFantasia.add(nombre2);

            // Ejecución del servicio
            CrearPersonaNombreFantasiaMasivo.SalidaCrearPersonaNombreFantasiaMasivo salida = servicio.logica(conn, entrada);

            // Validaciones
            assertNotNull(salida, "La salida no debe ser nula");
            assertEquals(0L, salida.P_OUT_ERRORESCONTADOR, "No deben existir errores en la operación");
            assertNotNull(salida.P_OUT_REFID, "La lista de referencias no debe ser nula");
            assertFalse(salida.P_OUT_REFID.isEmpty(), "Debe contener al menos una referencia");

            // Rollback para no afectar la base de datos
            conn.rollback();
        }
    }
}
