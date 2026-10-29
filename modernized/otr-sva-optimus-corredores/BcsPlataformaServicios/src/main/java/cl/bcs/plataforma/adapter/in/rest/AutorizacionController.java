package cl.bcs.plataforma.adapter.in.rest;

import org.springframework.web.bind.annotation.*;

/**
 * Stub controller for authorization workflow (AUT) operations.
 * Legacy package: cl.bcs.plataforma.aut (19 classes)
 *
 * Covers:
 *   - BcsAutorizaciones, AutorizacionEjemplo
 *   - CrearEventoAutorizacion, CrearEventoAutorizacionMasivo
 *   - CrearEtapaAutorizacion, ModificarEtapaAutorizacion
 *   - CrearAutorizadoEtapa, ModificarAutorizadoEtapa
 *   - CambiarEstadoEventoMantenedor
 *   - ModificarEstadoEventoAutorizacionMasivo
 *   - RequiereEventoAutorizacion
 *   - NotificarAutorizaciones, VencerEventosAutorizacion
 *
 * TODO: wire when PLA.Servicio DDL + SELECT * export is provided.
 */
@RestController
@RequestMapping("/api/plataforma/autorizacion")
public class AutorizacionController {

    @PostMapping("/evento/crear")
    public Object crearEventoAutorizacion(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — AUT.Evento not yet wired");
    }

    @PostMapping("/evento/crear-masivo")
    public Object crearEventoAutorizacionMasivo(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — AUT.EventoMasivo not yet wired");
    }

    @PostMapping("/evento/cambiar-estado-mantenedor")
    public Object cambiarEstadoEventoMantenedor(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — AUT.EventoMantenedor not yet wired");
    }

    @PostMapping("/evento/modificar-estado-masivo")
    public Object modificarEstadoEventoAutorizacionMasivo(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — AUT.EventoMasivo not yet wired");
    }

    @PostMapping("/evento/requiere")
    public Object requiereEventoAutorizacion(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — AUT.Evento not yet wired");
    }

    @PostMapping("/evento/vencer")
    public Object vencerEventosAutorizacion(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — AUT.Evento not yet wired");
    }

    @PostMapping("/etapa/crear")
    public Object crearEtapaAutorizacion(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — AUT.Etapa not yet wired");
    }

    @PostMapping("/etapa/modificar")
    public Object modificarEtapaAutorizacion(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — AUT.Etapa not yet wired");
    }

    @PostMapping("/autorizado-etapa/crear")
    public Object crearAutorizadoEtapa(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — AUT.AutorizadoEtapa not yet wired");
    }

    @PostMapping("/autorizado-etapa/modificar")
    public Object modificarAutorizadoEtapa(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — AUT.AutorizadoEtapa not yet wired");
    }

    @PostMapping("/notificar")
    public Object notificarAutorizaciones(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — AUT.Notificacion not yet wired");
    }
}
