package cl.bcs.plataforma.tests;

import static org.junit.Assert.assertNotNull;
import java.sql.Connection;
import org.junit.Test;
import com.google.gson.JsonObject;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.eje.CrearEjecutivo;
import cl.bcs.utils.Utils;

public class TestEJE_CrearEjecutivo {
    @Test
    public void ejecutarDemo() throws Exception {
        try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01","connections.properties")) {
            String json = "{\n" +
                "  \"p_IdEmpresa\": 1,\n" +
                "  \"p_IdUsuario\": 1,\n" +
                "  \"p_IdModulo\": \"SOC\",\n" +
                "  \"p_UsaReglasNegocio\": \"N\",\n" +
                "  \"p_IdEmpleadoSupervisor\": 10,\n" +
                "  \"p_IdEmpleadoEjecutivo\": 300,\n" +
                "  \"p_IdEstado\": \"ACT\"\n" +
                "}";
            CrearEjecutivo.Entrada entrada = Utils.getGsonBuilder().fromJson(json, CrearEjecutivo.Entrada.class);
            JsonObject out = new CrearEjecutivo().ejecutar(conn, Utils.getGsonBuilder().toJsonTree(entrada).getAsJsonObject());
            assertNotNull(out);
            conn.rollback();
        }
    }
}
