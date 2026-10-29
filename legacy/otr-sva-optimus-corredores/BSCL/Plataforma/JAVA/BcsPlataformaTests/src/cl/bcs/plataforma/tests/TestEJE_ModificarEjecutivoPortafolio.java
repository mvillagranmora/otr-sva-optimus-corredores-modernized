package cl.bcs.plataforma.tests;

import static org.junit.Assert.assertNotNull;
import java.sql.Connection;
import org.junit.Test;
import com.google.gson.JsonObject;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.eje.ModificarEjecutivoPortafolio;
import cl.bcs.utils.Utils;

public class TestEJE_ModificarEjecutivoPortafolio {
    @Test
    public void ejecutarDemo() throws Exception {
        try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01","connections.properties")) {
            String json = "{\n" +
                "  \"p_IdEmpresa\": 1,\n" +
                "  \"p_IdUsuario\": 1,\n" +
                "  \"p_IdModulo\": \"SOC\",\n" +
                "  \"p_UsaReglasNegocio\": \"N\",\n" +
                "  \"p_IdCliente\": 1000,\n" +
                "  \"p_IdPortafolio\": 2000,\n" +
                "  \"p_IdEmpleadoEjecutivo\": 300,\n" +
                "  \"p_EsPrincipal\": \"S\",\n" +
                "  \"p_ClientePrivado\": \"N\"\n" +
                "}";
            ModificarEjecutivoPortafolio.Entrada entrada = Utils.getGsonBuilder().fromJson(json, ModificarEjecutivoPortafolio.Entrada.class);
            JsonObject out = new ModificarEjecutivoPortafolio().call(conn, Utils.getGsonBuilder().toJsonTree(entrada).getAsJsonObject());
            assertNotNull(out);
            conn.rollback();
        }
    }
}
