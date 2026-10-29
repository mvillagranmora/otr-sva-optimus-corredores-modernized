package cl.bcs.plataforma.tests;

import static org.junit.Assert.assertNotNull;
import java.sql.Connection;
import org.junit.Test;
import com.google.gson.JsonObject;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.eje.ModificarEstadoEjecutivo;
import cl.bcs.utils.Utils;

public class TestEJE_ModificarEstadoEjecutivo {
    @Test
    public void ejecutarDemo() throws Exception {
        try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01","connections.properties")) {
            String json = "{\n" +
                "  \"p_IdEmpresa\": 1,\n" +
                "  \"p_IdUsuario\": 1,\n" +
                "  \"p_IdModulo\": \"SOC\",\n" +
                "  \"p_UsaReglasNegocio\": \"N\",\n" +
                "  \"p_IdEmpleadoEjecutivo\": 300,\n" +
                "  \"p_IdEstado\": \"ACT\"\n" +
                "}";
            ModificarEstadoEjecutivo.Entrada entrada = Utils.getGsonBuilder().fromJson(json, ModificarEstadoEjecutivo.Entrada.class);
            JsonObject out = new ModificarEstadoEjecutivo().ejecutar(conn, Utils.getGsonBuilder().toJsonTree(entrada).getAsJsonObject());
            assertNotNull(out);
            conn.rollback();
        }
    }
}
