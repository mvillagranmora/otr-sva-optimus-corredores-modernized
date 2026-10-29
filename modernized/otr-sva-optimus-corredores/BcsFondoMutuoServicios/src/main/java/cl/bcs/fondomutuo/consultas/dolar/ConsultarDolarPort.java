package cl.bcs.fondomutuo.consultas.dolar;

/** Legacy {@code ConsultarValorDolarFondoMutuo.logica(con, entrada)} without the connection. Single abstract method (the tests use lambdas). */
public interface ConsultarDolarPort {

    ConsultarDolarContrato.Salida logica(ConsultarDolarContrato.Entrada entrada) throws Exception;
}
