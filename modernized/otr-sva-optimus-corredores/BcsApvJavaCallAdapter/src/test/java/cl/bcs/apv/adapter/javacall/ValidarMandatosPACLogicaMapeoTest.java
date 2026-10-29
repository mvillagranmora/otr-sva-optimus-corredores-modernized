package cl.bcs.apv.adapter.javacall;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import cl.bcs.apv.api.logica.RegistroMandatoPAC;
import cl.bcs.apv.api.logica.ValidarMandatosPACLogica;
import cl.bcs.apv.api.logica.ValidarMandatosPACLogicaEntrada;

/** JSON shapes of the typed in-process operation {@code cl.bcs.APV.ValidarMandatosPAC.logica}; it is not in the registry. */
class ValidarMandatosPACLogicaMapeoTest {

    private static JsonObject json(String t) {
        return JsonParser.parseString(t).getAsJsonObject();
    }

    private static final String LISTAS = "\"P_OUT_SOLICITUDES_A_PROCESAR\":[{\"IdMandatoPAC\":1,\"IdentServicio\":\"S1\",\"EstadoRendicion\":\"E\",\"Otro\":5}],"
            + "\"P_OUT_SOLICITUDES_NO_PROCESADAS\":[{},{\"IdMandatoPAC\":3,\"EstadoRendicion\":null}],\"P_OUT_ERRORESCONTADOR\":2";

    @Test
    void formaEntradaSalidaYPlanaDanLoMismo() {
        ValidarMandatosPACLogicaEntrada esperado = new ValidarMandatosPACLogicaEntrada(1L, 21L,
                List.of(new RegistroMandatoPAC(1L, "S1", "E")),
                List.of(new RegistroMandatoPAC(0L, "", ""), new RegistroMandatoPAC(3L, "", null)));
        assertThat(ValidarMandatosPACLogicaMapeo.entradaDesdeJson(json("{\"entrada\":{" + LISTAS + "},\"idEmpresa\":1,\"idUsuario\":21}"))).isEqualTo(esperado);
        assertThat(ValidarMandatosPACLogicaMapeo.entradaDesdeJson(json("{\"salida\":{" + LISTAS + "},\"idEmpresa\":1,\"idUsuario\":21}"))).isEqualTo(esperado);
        assertThat(ValidarMandatosPACLogicaMapeo.entradaDesdeJson(json("{" + LISTAS + ",\"idEmpresa\":1,\"idUsuario\":21}"))).isEqualTo(esperado);
    }

    @Test
    void listasAusentesSonVaciasYNulasExplicitasSiguenNulas() {
        ValidarMandatosPACLogicaEntrada vacia = ValidarMandatosPACLogicaMapeo.entradaDesdeJson(json("{\"entrada\":{}}"));
        assertThat(vacia.idEmpresa()).isNull();
        assertThat(vacia.idUsuario()).isNull();
        assertThat(vacia.solicitudesAProcesar()).isEmpty();
        assertThat(vacia.solicitudesNoProcesadas()).isEmpty();
        ValidarMandatosPACLogicaEntrada nula = ValidarMandatosPACLogicaMapeo.entradaDesdeJson(
                json("{\"entrada\":{\"P_OUT_SOLICITUDES_A_PROCESAR\":null},\"idEmpresa\":1,\"idUsuario\":2}"));
        assertThat(nula.solicitudesAProcesar()).isNull();
        assertThat(nula.solicitudesNoProcesadas()).isEmpty();
        assertThat(ValidarMandatosPACLogicaMapeo.entradaDesdeJson(json("{\"entrada\":null}")).solicitudesNoProcesadas()).isNull();
    }

    @Test
    void idNoNumericoFallaComoElGsonDelLegacy() {
        assertThatThrownBy(() -> ValidarMandatosPACLogicaMapeo.entradaDesdeJson(
                json("{\"entrada\":{\"P_OUT_SOLICITUDES_A_PROCESAR\":[{\"IdMandatoPAC\":\"abc\"}]}}"))).isInstanceOf(JsonSyntaxException.class);
    }

    @Test
    void laOperacionNoEstaEnElRegistroYSeEnrutaAparte() throws Exception {
        assertThat(new ApvJavaCallRegistro().buscar(ValidarMandatosPACLogicaMapeo.OPERACION)).isEmpty();
        assertThat(ApvJavaCallPipelineEnProceso.esOperacionTipada(ValidarMandatosPACLogicaMapeo.OPERACION)).isTrue();
        assertThat(ApvJavaCallPipelineEnProceso.esOperacionTipada("cl.bcs.APV.ValidarMandatosPAC")).isFalse();
        // the typed operation reaches the use case and answers null (void)
        ValidarMandatosPACLogicaEntrada[] recibida = new ValidarMandatosPACLogicaEntrada[1];
        ValidarMandatosPACLogica caso = e -> recibida[0] = e;
        ApvJavaCallPipelineEnProceso p = new ApvJavaCallPipelineEnProceso(new ApvJavaCallRegistro(), Map.of(ValidarMandatosPACLogica.class, caso));
        assertThat(p.invocar(ValidarMandatosPACLogicaMapeo.OPERACION, json("{\"entrada\":{" + LISTAS + "},\"idEmpresa\":1,\"idUsuario\":21}"))).isNull();
        assertThat(recibida[0].idUsuario()).isEqualTo(21L);
        // the JavaCall class of the same name keeps answering null without any use case
        assertThat(p.invocar("cl.bcs.APV.ValidarMandatosPAC", json("{}"))).isNull();
    }
}
