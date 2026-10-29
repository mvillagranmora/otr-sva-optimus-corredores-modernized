package cl.bcs.fondomutuo.consultas.custodia;

import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.kit.ServicioNoServidoException;

/**
 * The operational switch of the custody calculation: it FAILS CLOSED. It is NOT a component: {@code ConsultarCustodiaPortafolioFfmmConfiguracion} declares it as the bean of
 * {@link ConsultarCustodiaPortafolioCustodiaPort} only when {@code fondomutuo.custodia.ffmm.cerrada=true} (the real adapter is the default). Every call is answered with
 * {@link ServicioNoServidoException} gate {@code G-CUS} (the same gate and exception {@code RescateReinversion} raises when the custody is closed): the controller answers 503 and the
 * router repeats the whole request in TomEE. It never returns a made-up custody. CPF is read only: nothing was written before it is reached. The start-up guard of
 * {@code ListasLecturaArranque} refuses to open CPF while this is the effective bean.
 */
public class ConsultarCustodiaPortafolioCustodiaCerrada implements ConsultarCustodiaPortafolioCustodiaPort {

    @Override
    public JsonObject consultar(JsonObject entrada) {
        throw new ServicioNoServidoException("G-CUS", "the custody calculation (ConsultarCustodiaPortafolioFFMM) is not served by this module");
    }
}
