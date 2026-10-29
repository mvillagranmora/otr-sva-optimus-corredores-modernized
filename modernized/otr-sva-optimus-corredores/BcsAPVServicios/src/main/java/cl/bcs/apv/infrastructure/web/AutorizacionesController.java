package cl.bcs.apv.infrastructure.web;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.bcs.apv.application.autorizaciones.AutorizacionCambioPlanActivo;

/**
 * REST entry point of the authorization hook of the plan change (legacy {@code cl.bcs.APV.AutorizacionCambiarAPlanActivoContratoSAAPV}, interface
 * {@code BcsAutorizaciones}). The legacy platform instantiated the class named in the event type ({@code JavaAutorizacion}) and called
 * {@code autorizarEvento} / {@code rechazarEvento} in process on the same connection; the new platform calls these two routes instead, with the LEGACY
 * JSON ({@code EntradaAutorizacion} {@code p_IdEmpresa, p_IdUsuario, p_IdModulo, p_UsaReglasNegocio, p_Lista[Evento]} in, {@code SalidaAutorizacion}
 * {@code P_OUT_MENSAJES, P_OUT_ERRORESCONTADOR, P_OUT_RESULTADO} out, see {@link AutorizacionJsonLegacy}), so the platform router needs no refactor; the use case stays typed.
 * It is not an operation of the JavaCall registry. Both paths are protected by {@code TokenInternoFilter} (allow-list, only the health probes are public);
 * errors are mapped in one place by {@code ApvExceptionHandler}.
 */
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class AutorizacionesController {

    private final AutorizacionCambioPlanActivo autorizacionCambioPlanActivo;

    public AutorizacionesController(AutorizacionCambioPlanActivo autorizacionCambioPlanActivo) {
        this.autorizacionCambioPlanActivo = autorizacionCambioPlanActivo;
    }

    /** Legacy {@code AutorizacionCambiarAPlanActivoContratoSAAPV.autorizarEvento}. */
    @PostMapping("/apv/autorizaciones/cambio-plan-activo/autorizacion")
    public String autorizarCambioPlanActivo(@RequestBody String cuerpo) {
        return AutorizacionJsonLegacy.salidaATexto(autorizacionCambioPlanActivo.autorizarEvento(AutorizacionJsonLegacy.entradaDesdeTexto(cuerpo)));
    }

    /** Legacy {@code AutorizacionCambiarAPlanActivoContratoSAAPV.rechazarEvento} (no effect). */
    @PostMapping("/apv/autorizaciones/cambio-plan-activo/rechazo")
    public String rechazarCambioPlanActivo(@RequestBody String cuerpo) {
        return AutorizacionJsonLegacy.salidaATexto(autorizacionCambioPlanActivo.rechazarEvento(AutorizacionJsonLegacy.entradaDesdeTexto(cuerpo)));
    }
}
