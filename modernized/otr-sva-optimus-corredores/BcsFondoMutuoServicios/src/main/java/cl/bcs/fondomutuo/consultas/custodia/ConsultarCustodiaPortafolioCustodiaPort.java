package cl.bcs.fondomutuo.consultas.custodia;

import com.google.gson.JsonObject;

/**
 * NEW read port: legacy {@code ConsultarCustodiaPortafolioFFMM.logica} with its JSON contract ({@link ConsultarCustodiaPortafolioContrato.CustodiaEntrada} in,
 * {@link ConsultarCustodiaPortafolioContrato.CustodiaSalida} out). The default adapter is {@code ConsultarCustodiaPortafolioFfmmAdaptador}; with {@code fondomutuo.custodia.ffmm.cerrada=true} the bean is the one that FAILS CLOSED ({@link ConsultarCustodiaPortafolioCustodiaCerrada}).
 */
@FunctionalInterface
public interface ConsultarCustodiaPortafolioCustodiaPort {

    JsonObject consultar(JsonObject entrada);
}
