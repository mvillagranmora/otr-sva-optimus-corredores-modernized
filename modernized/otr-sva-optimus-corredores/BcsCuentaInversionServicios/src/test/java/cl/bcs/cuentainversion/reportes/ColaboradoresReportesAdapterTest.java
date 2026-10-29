package cl.bcs.cuentainversion.reportes;

import cl.bcs.cuentainversion.infrastructure.adapter.AuditoriaReportesAdapter;
import cl.bcs.cuentainversion.infrastructure.adapter.ColaboradoresReportesAdapter;
import cl.bcs.cuentainversion.port.out.Colaboradores;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** The production collaborator adapters speak the legacy JavaCall names and JSON contracts through the generic port. */
class ColaboradoresReportesAdapterTest {

    private final List<String> llamadas = new ArrayList<>();
    private JsonObject respuesta = new JsonObject();
    private final Colaboradores generico = (servicio, entrada) -> {
        llamadas.add(servicio + " " + entrada);
        return respuesta;
    };

    @Test
    void empresaVaciaEsEmpresaNoExiste() {
        ColaboradoresReportesAdapter adapter = new ColaboradoresReportesAdapter(generico);
        assertThatThrownBy(() -> adapter.consultarEmpresa(7L)).isInstanceOf(IllegalStateException.class).hasMessage("Empresa no existe");
        assertThat(llamadas).containsExactly("cl.bcs.plataforma.pla.utils.ConsultarEmpresa {\"valor\":7}");
    }

    @Test
    void lasFilasDeCartolaSalenDeP_OUT_SALIDA() {
        respuesta = JsonParser.parseString("{\"P_OUT_SALIDA\":{\"movimientosCaja\":[{\"Moneda\":\"CLP\"},{\"Moneda\":\"USD\"}]}}").getAsJsonObject();
        List<JsonObject> filas = new ColaboradoresReportesAdapter(generico).movimientosCaja(new JsonObject());
        assertThat(filas).hasSize(2);
        assertThat(filas.get(1).get("Moneda").getAsString()).isEqualTo("USD");
        assertThat(llamadas.get(0)).startsWith("cl.bcs.normativa.cartola.MovimientosCaja ");
    }

    @Test
    void laAuditoriaSinFilasNoSeEnvia_yUnFalloSeTraga() {
        AuditoriaReportesAdapter vacia = new AuditoriaReportesAdapter(generico);
        vacia.agregarLog(new AgregarLogEntrada());
        assertThat(llamadas).isEmpty();

        AuditoriaReportesAdapter caida = new AuditoriaReportesAdapter((s, e) -> {
            throw new IllegalStateException("down");
        });
        AgregarLogEntrada entrada = new AgregarLogEntrada();
        entrada.p_Log.add(AgregarLogEntrada.Log.cta("INGRESO", "X", 1L, 1L, 1L, 1L, "2025-03-14", "p"));
        caida.agregarLog(entrada);
    }
}
