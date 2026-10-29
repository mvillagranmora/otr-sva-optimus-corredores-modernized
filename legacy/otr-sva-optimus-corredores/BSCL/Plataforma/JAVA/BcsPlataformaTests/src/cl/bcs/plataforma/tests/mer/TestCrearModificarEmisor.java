package cl.bcs.plataforma.tests.mer;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;

import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import cl.bcs.plataforma.mer.CrearModificarEmisor;
import cl.bcs.plataforma.mer.CrearModificarEmisor.CrearModificarEmisor_Entrada;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Utils;

public class TestCrearModificarEmisor {

    @Test
    void modificaEmisorDummy() throws Exception {
        try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
            CrearModificarEmisor svc = new CrearModificarEmisor();
            CrearModificarEmisor.Entrada entrada = new CrearModificarEmisor.Entrada();
            entrada.p_IdEmpresa = 1L;
            entrada.p_IdUsuario = 1L;
            entrada.p_IdModulo = "PLA";
            entrada.p_simularOperacion = "N";
            entrada.p_UsaReglasNegocio = "N";
            entrada.p_Modo = "MODIFICAR";
            entrada.p_IdPersona = 0L;
            entrada.p_IdEmisor = "TEST_EMISOR";
            entrada.p_CodigoEmisor = "";
            entrada.p_CodigoRegulador = "";
            entrada.p_Regulador = "";
            entrada.p_EmiteIIF = "N";
            entrada.p_EmiteIRF = "N";
            entrada.p_EmiteFungible = "N";
            entrada.p_ValorClasificacionRiesgoIIF = "";
            entrada.p_IndicadorAGF = "N";
            entrada.p_IdEstado = "ACT";
            entrada.p_IdEntidadClasificadoraRiesgo = "";
            entrada.p_TipoEntidad = 0;

            Gson gson = Utils.getGsonBuilder();
            JsonObject in = gson.toJsonTree(entrada).getAsJsonObject();

            JsonObject out = svc.call(conn, in);
            assertNotNull(out);

            // La UI espera el campo legacy P_OUT_* además del formato nuevo p_out_*
            assertTrue(out.has("P_OUT_ERRORESCONTADOR"));
            assertTrue(out.has("P_OUT_ERRORESDETALLE"));
            assertTrue(out.has("P_OUT_ADVERTENCIASCONTADOR"));
            assertTrue(out.has("P_OUT_ADVERTENCIASDETALLE"));
        }
    }
}
