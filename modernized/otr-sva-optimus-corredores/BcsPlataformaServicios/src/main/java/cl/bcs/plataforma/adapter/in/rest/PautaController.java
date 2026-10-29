package cl.bcs.plataforma.adapter.in.rest;

import org.springframework.web.bind.annotation.*;

/**
 * Stub controller for valuation schedule (PAU / Pauta) operations.
 * Legacy package: cl.bcs.plataforma.pau (35 classes)
 *
 * Covers:
 *   - ValorizarCarteraSubPrestEmpres, ValorizarCarteraPactos
 *   - ConsultarLogValorizadorPauta, AgregarControlPauta
 *   - EnviarNotificacionFolioDocumento
 *   (and additional pauta/valuation services)
 *
 * TODO: wire when PLA.Servicio DDL + SELECT * export is provided.
 */
@RestController
@RequestMapping("/api/plataforma/pauta")
public class PautaController {

    @PostMapping("/cartera/valorizar-sub-prest")
    public Object valorizarCarteraSubPrestEmpres(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — PAU.Cartera not yet wired");
    }

    @PostMapping("/cartera/valorizar-pactos")
    public Object valorizarCarteraPactos(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — PAU.Pactos not yet wired");
    }

    @PostMapping("/log/consultar-valorizador")
    public Object consultarLogValorizadorPauta(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — PAU.LogValorizador not yet wired");
    }

    @PostMapping("/control/agregar")
    public Object agregarControlPauta(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — PAU.Control not yet wired");
    }

    @PostMapping("/folio-documento/notificar")
    public Object enviarNotificacionFolioDocumento(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires external notification service not yet wired");
    }
}
