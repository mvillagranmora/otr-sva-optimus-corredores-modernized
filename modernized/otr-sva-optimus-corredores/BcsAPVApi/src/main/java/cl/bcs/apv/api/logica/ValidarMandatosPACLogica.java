package cl.bcs.apv.api.logica;

/**
 * TYPED entry point of the legacy {@code cl.bcs.APV.ValidarMandatosPAC.logica(Connection, SalidaRecaudacionTraspaso, Long, Long)}, which the Tesoreria job
 * {@code BcsJobProcesarDistribucion} calls in process with its own connection to store the outcome of the PAC distribution in
 * {@code BSCL.Tesoreria.BD::PAC.LogMandatoPAC}. It is NOT a JavaCall operation (the legacy {@code call()} of the class returns null and stays a null
 * adapter) and has no REST route; it is not part of {@link cl.bcs.apv.api.casouso.CasosUso#TODOS}.
 *
 * <p>The implementation runs on the connection and transaction of the caller (it never commits or rolls back by itself); the legacy declared
 * {@code throws Exception} and so does the behaviour: any exception reaches the caller unchanged.
 */
public interface ValidarMandatosPACLogica {

    void ejecutar(ValidarMandatosPACLogicaEntrada entrada);
}
