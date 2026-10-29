package cl.bcs.tesoreria.familia_a;

import java.util.List;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import cl.bcs.tesoreria.familia_a.support.Caso;
import cl.bcs.tesoreria.familia_a.support.Dobles;
import cl.bcs.tesoreria.familia_a.support.Verifica;
import cl.bcs.tesoreria.familia_a.support.ZonaUtc;
import cl.bcs.tesoreria.javacall.AnularRegistrosService;
import cl.bcs.tesoreria.javacall.ConsultarComprobantesWSService;
import cl.bcs.tesoreria.javacall.CrearComprobanteService;
import cl.bcs.tesoreria.javacall.CrearIngresoEgresoService;
import cl.bcs.tesoreria.javacall.CrearTraspasoService;
import cl.bcs.tesoreria.javacall.LiquidarComprobanteService;
import cl.bcs.tesoreria.javacall.PreLiquidarComprobanteService;
import cl.bcs.tesoreria.javacall.SaldoPendienteDeLiquidarService;
import cl.bcs.tesoreria.javacall.ValidarTransferenciaDVPService;

/**
 * Fresh-input driver (independent verifier), family A: runs the modern code on the TES-A-F* cases with the SAME wiring
 * as the development tests (copied from each *ServiceTest.verificar). Every output facet is compared strictly: the
 * "sin orden" lists of the development tests are NOT used here (an order difference would be reported as a finding).
 */
@ExtendWith(ZonaUtc.class)
class FreshATest {

    static List<String> casos() {
        return Caso.todos().stream().filter(n -> n.startsWith("TES-A-F")).toList();
    }

    private static final String[] SIN_ORDEN = {};

    @ParameterizedTest
    @MethodSource("casos")
    void fresco(String nombre) throws Exception {
        Caso caso = Caso.cargar(nombre);
        Dobles d = Dobles.de(caso);
        String servicio = caso.caso().get("servicio").getAsString();
        Verifica.Ejecucion r;
        switch (servicio) {
            case "ValidarTransferenciaDVP" -> {
                ValidarTransferenciaDVPService s = new ValidarTransferenciaDVPService(caso.jdbc(), d.diccionario());
                r = Verifica.Ejecucion.de(caso, () -> s.execute(caso.entrada().deepCopy()));
            }
            case "CrearIngresoEgreso" -> {
                try (var ctx = CieContexto.de(caso, d, CrearIngresoEgresoService.class, CrearComprobanteService.class, LiquidarComprobanteService.class)) {
                    CrearIngresoEgresoService s = ctx.getBean(CrearIngresoEgresoService.class);
                    r = Verifica.Ejecucion.de(caso, () -> s.execute(caso.entrada().deepCopy()));
                    Verifica.comparar(caso, r, d.colaboradores().traza(), SIN_ORDEN);
                }
                return;
            }
            case "CrearTraspaso" -> {
                try (var ctx = CieContexto.de(caso, d, CrearTraspasoService.class, CrearIngresoEgresoService.class, CrearComprobanteService.class, LiquidarComprobanteService.class)) {
                    CrearTraspasoService s = ctx.getBean(CrearTraspasoService.class);
                    r = Verifica.Ejecucion.de(caso, () -> s.execute(caso.entrada().deepCopy()));
                    Verifica.comparar(caso, r, d.colaboradores().traza(), SIN_ORDEN);
                }
                return;
            }
            case "SaldoPendienteDeLiquidar" -> {
                SaldoPendienteDeLiquidarService s = new SaldoPendienteDeLiquidarService(caso.jdbc());
                r = Verifica.Ejecucion.de(caso, () -> s.execute(caso.entrada().deepCopy()));
            }
            case "ConsultarComprobantesWS" -> {
                ConsultarComprobantesWSService s = new ConsultarComprobantesWSService(caso.jdbc(), d.configuracion(), d.diccionario());
                r = Verifica.Ejecucion.de(caso, () -> s.execute(caso.entrada().deepCopy()));
            }
            case "AnularRegistros" -> {
                AnularRegistrosService s = new AnularRegistrosService(caso.jdbc(), d.diccionario(), d.colaboradores());
                r = Verifica.Ejecucion.de(caso, () -> s.execute(caso.entrada().deepCopy()));
            }
            case "PreLiquidarComprobante" -> {
                PreLiquidarComprobanteService s = new PreLiquidarComprobanteService(caso.jdbc(), d.configuracion(), d.diccionario(), d.colaboradores());
                r = Verifica.Ejecucion.de(caso, () -> s.execute(caso.entrada().deepCopy()));
            }
            case "LiquidarComprobante" -> {
                LiquidarComprobanteService s = new LiquidarComprobanteService(caso.jdbc(), d.configuracion(), d.diccionario(), d.colaboradores());
                r = Verifica.Ejecucion.de(caso, () -> s.execute(caso.entrada().deepCopy()));
            }
            default -> throw new AssertionError("servicio sin conductor: " + servicio);
        }
        Verifica.comparar(caso, r, d.colaboradores().traza(), SIN_ORDEN);
    }
}
