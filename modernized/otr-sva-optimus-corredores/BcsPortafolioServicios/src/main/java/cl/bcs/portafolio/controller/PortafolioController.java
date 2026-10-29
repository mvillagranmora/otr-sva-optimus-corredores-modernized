package cl.bcs.portafolio.controller;

import cl.bcs.portafolio.domain.autorizacion.AutorizacionPortafolioEntrada;
import cl.bcs.portafolio.domain.autorizacion.AutorizacionPortafolioResultado;
import cl.bcs.portafolio.domain.consumo.*;
import cl.bcs.portafolio.domain.custodio.*;
import cl.bcs.portafolio.domain.matriz.*;
import cl.bcs.portafolio.domain.portafolio.*;
import cl.bcs.portafolio.domain.preferencia.*;
import cl.bcs.portafolio.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/portafolio")
public class PortafolioController {

    private final PortafolioService portafolioService;
    private final AutorizacionService autorizacionService;
    private final ConsumoService consumoService;
    private final PreferenciaService preferenciaService;
    private final CustodioService custodioService;
    private final MatrizService matrizService;

    public PortafolioController(PortafolioService portafolioService,
                                AutorizacionService autorizacionService,
                                ConsumoService consumoService,
                                PreferenciaService preferenciaService,
                                CustodioService custodioService,
                                MatrizService matrizService) {
        this.portafolioService = portafolioService;
        this.autorizacionService = autorizacionService;
        this.consumoService = consumoService;
        this.preferenciaService = preferenciaService;
        this.custodioService = custodioService;
        this.matrizService = matrizService;
    }

    @PostMapping("/crear")
    public ResponseEntity<CrearPortafolioResultado> crearPortafolio(@RequestBody CrearPortafolioEntrada entrada) {
        CrearPortafolioResultado resultado = portafolioService.crearPortafolio(entrada);
        if (resultado.mensaje() != null && !resultado.mensaje().equals("OK")) {
            return ResponseEntity.unprocessableEntity().body(resultado);
        }
        return ResponseEntity.ok(resultado);
    }

    @PostMapping("/crear-con-replica")
    public ResponseEntity<CrearPortafolioConReplicaResultado> crearPortafolioConReplica(@RequestBody CrearPortafolioConReplicaEntrada entrada) {
        return ResponseEntity.ok(portafolioService.crearPortafolioConReplica(entrada));
    }

    @PostMapping("/eliminar-sin-transacciones")
    public ResponseEntity<PortafolioResultado> eliminarSinTransacciones(@RequestBody EliminarPortafolioEntrada entrada) {
        return ResponseEntity.ok(portafolioService.eliminarSinTransacciones(entrada));
    }

    @PostMapping("/modificar")
    public ResponseEntity<PortafolioResultado> modificarPortafolio(@RequestBody ModificarPortafolioEntrada entrada) {
        return ResponseEntity.ok(portafolioService.modificarPortafolio(entrada));
    }

    @PostMapping("/modificar-estado")
    public ResponseEntity<PortafolioResultado> modificarEstado(@RequestBody ModificarEstadoPortafolioEntrada entrada) {
        return ResponseEntity.ok(portafolioService.modificarEstadoPortafolio(entrada));
    }

    @PostMapping("/autorizar-masivo")
    public ResponseEntity<List<AutorizacionPortafolioResultado>> autorizarMasivo(@RequestBody List<AutorizacionPortafolioEntrada> entradas) {
        return ResponseEntity.ok(autorizacionService.autorizarMasivo(entradas));
    }

    @PostMapping("/rechazar-masivo")
    public ResponseEntity<List<AutorizacionPortafolioResultado>> rechazarMasivo(@RequestBody List<AutorizacionPortafolioEntrada> entradas) {
        return ResponseEntity.ok(autorizacionService.rechazarMasivo(entradas));
    }

    @PostMapping("/consumo/en-linea")
    public ResponseEntity<ConsumoEnLineaResultado> consumoEnLinea(@RequestBody ConsumoEnLineaEntrada entrada) {
        return ResponseEntity.ok(consumoService.consultarConsumoEnLinea(entrada));
    }

    @PostMapping("/consumo/limite")
    public ResponseEntity<ConsumoLimiteResultado> consumoLimite(@RequestBody ConsumoLimiteEntrada entrada) {
        return ResponseEntity.ok(consumoService.consultarLimite(entrada));
    }

    @PostMapping("/preferencia/crear")
    public ResponseEntity<PreferenciaPagoResultado> crearPreferencia(@RequestBody PreferenciaPagoEntrada entrada) {
        return ResponseEntity.ok(preferenciaService.crear(entrada));
    }

    @PutMapping("/preferencia/actualizar")
    public ResponseEntity<Void> actualizarPreferencia(@RequestBody PreferenciaPagoEntrada entrada) {
        preferenciaService.actualizar(entrada);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/custodio/consultar")
    public ResponseEntity<CuentaCustodioResultado> consultarCustodio(@RequestBody CuentaCustodioEntrada entrada) {
        return ResponseEntity.ok(custodioService.consultar(entrada));
    }

    @PutMapping("/custodio/actualizar")
    public ResponseEntity<Void> actualizarCustodio(@RequestBody CuentaCustodioEntrada entrada) {
        custodioService.actualizar(entrada);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/matriz/consultar")
    public ResponseEntity<List<MatrizAgrupadorResultado>> consultarMatriz(@RequestBody MatrizAgrupadorEntrada entrada) {
        return ResponseEntity.ok(matrizService.consultarMatriz(entrada));
    }
}
