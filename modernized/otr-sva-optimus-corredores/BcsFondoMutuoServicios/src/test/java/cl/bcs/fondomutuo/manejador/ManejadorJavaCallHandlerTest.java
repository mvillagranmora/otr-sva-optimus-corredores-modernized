package cl.bcs.fondomutuo.manejador;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.adapter.in.javacall.JavaCallDispatcher;
import cl.bcs.fondomutuo.kit.ServicioNoServidoException;

/** FM-MAN-13..: the JavaCall JSON in / out exactly as the legacy (plain Gson, counter always present, null keys omitted, D-MAN-2 through the JSON). */
class ManejadorJavaCallHandlerTest {

    private static JsonObject json(String s) {
        return JsonParser.parseString(s).getAsJsonObject();
    }

    private ManejadorJavaCallHandler handler(Banco b, Set<String> servidas) {
        return new ManejadorJavaCallHandler(b.servicio(servidas));
    }

    @Test
    void fmMan13_elHandlerEsElFqnDelLegacy_yEstaEnLaListaBlanca() {
        assertThat(handler(new Banco(), Banco.TODAS).fqn()).isEqualTo("cl.bcs.fondomutuo.ManejadorSolicitudAporteRescate");
        assertThat(JavaCallDispatcher.LISTA_BLANCA).contains(handler(new Banco(), Banco.TODAS).fqn());
        new JavaCallDispatcher(List.of(handler(new Banco(), Banco.TODAS)));   // does not stop the start-up
    }

    @Test
    void fmMan14_jsonDeSalida_conLasClavesDelLegacy_enOrden_sinNulos_yElContadorSiempre() throws Exception {
        Banco b = new Banco();
        JsonObject out = handler(b, Set.of("NORMAL")).call(null, json("{\"p_IdEmpresa\":1,\"p_IdUsuario\":7,\"p_IdModulo\":\"FMM\",\"p_IdOrigenProceso\":\"PAC\"}"));
        assertThat(out.toString()).isEqualTo("{\"P_OUT_ERRORESCONTADOR\":0,\"P_OUT_MENSAJES\":[],\"P_OUT_RESULTADO\":[]}");
        assertThat(b.log).isEmpty();
    }

    @Test
    void fmMan15_laListaNormalSeLeeConGsonPlano_yElResultadoSeSerializaSinNulos() throws Exception {
        Banco b = new Banco();
        JsonObject out = handler(b, Set.of("NORMAL")).call(null, json("{\"p_IdEmpresa\":1,\"p_FormaPagoSpot\":[],\"p_SolicitudesAporteRescateNormal\":"
                + "[{\"IdInterno\":5,\"IdCliente\":100,\"Monto\":12.50,\"NumeroCuotas\":10.0000}],\"p_SimularOperacion\":\"S\"}"));
        assertThat(b.log).containsExactly("NORMAL:S[5]", "NORMAL:N[5]");
        assertThat(out.toString()).isEqualTo("{\"P_OUT_ERRORESCONTADOR\":0,\"P_OUT_MENSAJES\":[],\"P_OUT_RESULTADO\":[{\"IdInterno\":5}]}");
        assertThat(b.entradasNormal.get(0).p_SolicitudesAporteRescate.get(0).Monto.toPlainString()).isEqualTo("12.50");
        assertThat(b.entradasNormal.get(0).p_SolicitudesAporteRescate.get(0).NumeroCuotas.toPlainString()).as("scale kept").isEqualTo("10.0000");
    }

    @Test
    void fmMan16_sinLaClaveFormaPagoSpot_esNpe_DMAN2_tambienPorElJson() {
        Banco b = new Banco();
        assertThatThrownBy(() -> handler(b, Set.of("NORMAL")).call(null, json("{\"p_SolicitudesAporteRescateNormal\":[{\"IdInterno\":1}]}")))
                .isInstanceOf(NullPointerException.class);
        assertThat(b.log).containsExactly("NORMAL:S[1]");
        assertThatThrownBy(() -> handler(b, Set.of("NORMAL")).call(null, json("{\"p_FormaPagoSpot\":null,\"p_SolicitudesAporteRescateNormal\":[{\"IdInterno\":1}]}")))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void fmMan17_g0PorJson_otraListaConFilas_esNoServido_conFormaPagoSpotPresente() {
        Banco b = new Banco();
        assertThatThrownBy(() -> handler(b, Set.of("NORMAL")).call(null, json("{\"p_FormaPagoSpot\":[],\"p_SolicitudesAporteRescateNormal\":[{\"IdInterno\":1}],"
                + "\"p_SolicitudRescateTraspaso\":[{\"IdInterno\":2}]}"))).isInstanceOf(ServicioNoServidoException.class);
        assertThat(b.log).isEmpty();
    }

    @Test
    void fmMan18_jsonMalFormado_lanzaComoElLegacy_unaClaveDesconocidaSeIgnora() throws Exception {
        assertThatThrownBy(() -> handler(new Banco(), Banco.TODAS).call(null, json("{\"p_IdEmpresa\":\"no es numero\"}"))).isInstanceOf(RuntimeException.class);
        assertThat(handler(new Banco(), Banco.TODAS).call(null, json("{\"clave_desconocida\":1}")).get("P_OUT_ERRORESCONTADOR").getAsLong()).isZero();
    }
}
