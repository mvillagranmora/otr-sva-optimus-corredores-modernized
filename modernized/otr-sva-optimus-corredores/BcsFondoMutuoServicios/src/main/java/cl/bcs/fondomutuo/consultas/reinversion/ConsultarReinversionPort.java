package cl.bcs.fondomutuo.consultas.reinversion;

/** Legacy {@code ConsultarFondoMutuoReinversion.logica(con, entrada)} without the connection. Single abstract method (the tests use lambdas). */
public interface ConsultarReinversionPort {

    ConsultarReinversionContrato.Salida logica(ConsultarReinversionContrato.Entrada entrada) throws Exception;
}
