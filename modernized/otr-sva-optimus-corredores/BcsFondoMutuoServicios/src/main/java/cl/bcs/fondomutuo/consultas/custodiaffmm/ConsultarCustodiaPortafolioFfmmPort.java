package cl.bcs.fondomutuo.consultas.custodiaffmm;

/** Legacy {@code ConsultarCustodiaPortafolioFFMM.logica(con, entrada)} without the connection and without checked exception. */
@FunctionalInterface
public interface ConsultarCustodiaPortafolioFfmmPort {

    ConsultarCustodiaPortafolioFfmmContrato.Salida logica(ConsultarCustodiaPortafolioFfmmContrato.Entrada entrada);
}
