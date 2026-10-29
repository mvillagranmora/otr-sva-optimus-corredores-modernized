package cl.bcs.fondomutuo.consultas.instrumentos;

/** Legacy {@code ConsultarInstrumentosFondosMutuos.logica(con, entrada)} without the connection. Single abstract method (the tests use lambdas). */
public interface ConsultarInstrumentosPort {

    ConsultarInstrumentosContrato.Salida logica(ConsultarInstrumentosContrato.Entrada entrada) throws Exception;
}
