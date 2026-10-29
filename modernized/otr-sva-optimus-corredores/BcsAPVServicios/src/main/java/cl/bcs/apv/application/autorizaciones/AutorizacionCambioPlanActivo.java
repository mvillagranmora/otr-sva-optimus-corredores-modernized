package cl.bcs.apv.application.autorizaciones;

/**
 * Typed port of the legacy hook {@code cl.bcs.APV.AutorizacionCambiarAPlanActivoContratoSAAPV} ({@code BcsAutorizaciones}): the authorization engine calls it
 * when the approvers accepted ({@link #autorizarEvento}) or rejected ({@link #rechazarEvento}) the events "MOPLAC" that
 * {@code SolicitarCambioAPlanActivo} creates when {@code APV.CambioPlanRequiereAutorizacion} is "S". It is NOT a {@code JavaCall} operation and has no entry in
 * the JavaCall registry: its REST entry point is {@code AutorizacionesController}.
 *
 * <p>Like the legacy methods, neither declares an exception, yet a malformed event (see the implementation) makes them throw a runtime exception.
 */
public interface AutorizacionCambioPlanActivo {

    SalidaAutorizacion autorizarEvento(EntradaAutorizacion entrada);

    SalidaAutorizacion rechazarEvento(EntradaAutorizacion entrada);
}
