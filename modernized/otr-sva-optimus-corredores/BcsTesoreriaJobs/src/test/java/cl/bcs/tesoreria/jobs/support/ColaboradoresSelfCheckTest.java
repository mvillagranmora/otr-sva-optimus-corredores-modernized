package cl.bcs.tesoreria.jobs.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

/**
 * Self-check of the scripted collaborators: the legacy call sequence of GenerarArchivoCargos, replayed BY HAND against the fakes, produces exactly
 * the trace, sequence consumption and answers the oracle recorded for case CG-03. If a fake changed shape, this fails before any job code is involved.
 */
class ColaboradoresSelfCheckTest {

    @Test
    @DisplayName("the fakes replay the recorded collaborator trace of CG-03 (format, procedure executed twice, mandates, sequences)")
    void trazaDeCg03() throws Exception {
        Caso caso = Caso.cargar("CG-03");
        Traza t = new Traza(caso.colaboradores());
        JsonObject in = new JsonObject();
        in.addProperty("IdFormatoArchivo", 8L);
        in.addProperty("IdEmpresa", 1L);
        in.addProperty("IdUsuario", 21L);
        var formatos = ServiciosDeCaso.formatos(t).consultar(in);
        assertThat(formatos).hasSize(1);
        assertThat(formatos.get(0).getAsJsonObject().get("IdUsuario").getAsLong()).isEqualTo(21L);

        var proc = Puertos.procedimiento(t);
        List<Object> params = List.of(1L, 8L, 5L, "2025-04-05");
        String llamada = "CALL BSCL.\"SP_CARGOS_PAC\"(?,?,?,?,?,?)";
        assertThat(proc.ejecutar(llamada, params)).hasSize(2);
        assertThat(proc.ejecutar(llamada, params)).hasSize(2);

        var sec = Puertos.secuencias(caso, t);
        assertThat(List.of(sec.siguiente("BSCL.Plataforma.BD.Secuencias::Detalle"), sec.siguiente("BSCL.Plataforma.BD.Secuencias::Detalle"),
                sec.siguiente("BSCL.Plataforma.BD.Secuencias::Detalle"), sec.siguiente("BSCL.Plataforma.BD.Secuencias::Cabecera"))).containsExactly(500L, 501L, 502L, 70L);

        ServiciosDeCaso.mandatos(t).logica(5L);

        assertThat(Canon.texto(t.llamadas())).isEqualTo(Canon.texto(caso.esperado().get("colaboradores")));
        assertThat(Canon.texto(t.secuencias())).isEqualTo(Canon.texto(caso.esperado().get("secuencias")));
    }

    @Test
    @DisplayName("the scripted Colaboradores and CabecerasCarga answer and trace like the oracle fakes (CM-01 head)")
    void trazaDeCm01() throws Exception {
        Caso caso = Caso.cargar("CM-01");
        Traza t = new Traza(caso.colaboradores());
        JsonObject q = new JsonObject();
        q.addProperty("p_IdEmpresa", 1L);
        q.addProperty("p_IdModulo", "TES");
        q.addProperty("p_IdUsuario", 21L);
        assertThat(new ColaboradoresDeCaso(t).invocar("ConsultarEstadoModulo", q).getAsJsonArray("p_out_ListaModulos")).hasSize(2);
        var cab = Puertos.cabeceras(t);
        assertThat(cab.cantidad("DAP", "2025-04-01", "PRO")).isZero();
        assertThat(cab.cantidad("DAP", "2025-04-01", "PEN")).isEqualTo(1);
        JsonObject esperada = caso.esperado().getAsJsonArray("colaboradores").get(0).getAsJsonObject();
        assertThat(Canon.texto(t.llamadas().get(0))).isEqualTo(Canon.texto(esperada));
        assertThat(t.llamadas()).hasSize(3);
    }
}
