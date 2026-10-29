package cl.bcs.fondomutuo.consultas.custodia;

/** Legacy {@code ConsultarCustodiaPortafolioFondoMutuo.logica(con, entrada)} without the connection. Functional interface (the tests use lambdas). */
@FunctionalInterface
public interface ConsultarCustodiaPortafolioPort {

    ConsultarCustodiaPortafolioContrato.Salida logica(ConsultarCustodiaPortafolioContrato.Entrada entrada) throws Exception;
}
