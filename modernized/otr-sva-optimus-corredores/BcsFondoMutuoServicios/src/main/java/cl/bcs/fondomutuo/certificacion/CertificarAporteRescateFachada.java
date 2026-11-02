package cl.bcs.fondomutuo.certificacion;

import java.util.function.Function;
import java.util.function.UnaryOperator;

import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.Entrada;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.Salida;
import cl.bcs.fondomutuo.kit.Compuertas;
import cl.bcs.fondomutuo.kit.ServicioNoServidoException;

/**
 * Legacy {@code CertificarSolicitudAporteRescate.logica}, the whole of it (T5): fronts F-A and F-B (the service), F-C..F-I (the units of T2..T4-B) and F-J (the closing), in the order of the
 * legacy, behind one port; and the recursion of the switch ({@link CertificacionHijaPort}: the facade certifies the aportes of switch through itself, on the same connection). No state:
 * everything a request needs lives in its {@link CertificarAporteRescateContexto}.
 *
 * <p>What the facade adds to the legacy (the only intended behaviour differences of T5, plan 6.3): (1) the gates G8, G3, G-CUS, G6, G1 and G2 are evaluated BEFORE the first write of the request
 * (the legacy has none), each with the predicate of its unit ("a request that may reach the port"), so a shut gate answers {@link ServicioNoServidoException} (503, the router repeats the request in
 * TomEE) with nothing written; an early exit (FM-CER-14, the rejections of F-A) notifies nothing and meets no gate; (2) the recursion FAILS CLOSED when its depth is greater than
 * {@value #PROFUNDIDAD_MAXIMA} (the legacy has no limit; in practice the child of a request never has aportes of switch of its own).
 *
 * <p>The host decision is NOT the facade's: counter 0 commits, anything else rolls back ({@code CallServlet}); the facade returns the {@link Salida} or lets the exception go up untouched.
 */
public final class CertificarAporteRescateFachada implements CertificarAporteRescatePort, CertificacionHijaPort {

    /** The child (depth 1) runs, a grandchild (depth 2) is refused (decision 2 of T5, review M2 of T4-B). */
    static final int PROFUNDIDAD_MAXIMA = 1;

    private final Function<CertificarAporteRescateFasesPosteriores, CertificarAporteRescatePort> servicio;
    private final Compuertas compuertas;
    private final CertificarAporteRescateAportes aportes;
    private final CertificarAporteRescateRescates rescates;
    private final CertificarAporteRescateCuentaInversion cuenta;
    private final CertificarAporteRescateTesoreria tesoreria;
    private final CertificarAporteRescateFacturacion facturacion;
    private final CertificarAporteRescateReinversion reinversion;
    private final CertificarAporteRescateSpot spot;
    private final CertificarAporteRescateCierre cierre;
    private final CertificarAporteRescateDatos datos;
    private final CertificarAporteRescateEscrituraMonto escrituraMonto;
    private final UnaryOperator<CertificacionHijaPort> envolverHija;

    CertificarAporteRescateFachada(Function<CertificarAporteRescateFasesPosteriores, CertificarAporteRescatePort> servicio, Compuertas compuertas, CertificarAporteRescateAportes aportes,
            CertificarAporteRescateRescates rescates, CertificarAporteRescateCuentaInversion cuenta, CertificarAporteRescateTesoreria tesoreria, CertificarAporteRescateFacturacion facturacion,
            CertificarAporteRescateReinversion reinversion, CertificarAporteRescateSpot spot, CertificarAporteRescateCierre cierre, CertificarAporteRescateDatos datos,
            CertificarAporteRescateEscrituraMonto escrituraMonto, UnaryOperator<CertificacionHijaPort> envolverHija) {
        this.servicio = servicio;
        this.compuertas = compuertas;
        this.aportes = aportes;
        this.rescates = rescates;
        this.cuenta = cuenta;
        this.tesoreria = tesoreria;
        this.facturacion = facturacion;
        this.reinversion = reinversion;
        this.spot = spot;
        this.cierre = cierre;
        this.datos = datos;
        this.escrituraMonto = escrituraMonto;
        this.envolverHija = envolverHija;
    }

    /** The request (depth 0): the only entry of the controller. */
    @Override
    public Salida logica(Entrada entrada) throws Exception {
        return ejecutar(entrada, 0);   // depth 0
    }

    /**
     * The recursion of the switch as an entry: it runs at depth 1 (the child of a request), so its own aportes of switch, if any, are refused (a grandchild). The recursion that the facade makes
     * itself goes through {@code hijaDe}, which carries the real depth. The guard cannot fire earlier than the switch step: whether a request has aportes of switch is known only after
     * {@code CrearSolicitudAporteReinversion} answers (front I), that is, after writes; the host rolls everything back, only a notification already sent cannot be recalled (G8 shut by default).
     */
    @Override
    public Salida certificar(Entrada entrada) throws Exception {
        return ejecutar(entrada, 1);
    }

    private Salida ejecutar(Entrada entrada, int profundidad) throws Exception {
        if (profundidad > PROFUNDIDAD_MAXIMA) {
            throw new ServicioNoServidoException("PROFUNDIDAD", "the certification of the aportes of switch recurses deeper than " + PROFUNDIDAD_MAXIMA);
        }
        CertificarAporteRescateFasesPosteriores posteriores = ctx -> continuar(ctx, profundidad);
        return servicio.apply(posteriores).logica(entrada);
    }

    /** The phases after F-B, in the order of the legacy {@code logica} (C:575-864); the gates first, then every front, the closing last. */
    private Salida continuar(CertificarAporteRescateContexto ctx, int profundidad) throws Exception {
        // gates that need only what F-A and F-B left: every run that gets here notifies (G8); the cash flow of the aportes (G3); custody (G-CUS: the custody calculation of the rescates and the
        // movements of both lists are called whenever a list is not empty, whatever the state of its requests). A predicate reads only the context and evaluates the SAME expressions as the step it
        // announces: when the data make it throw, the step would throw too in the legacy, so the exception goes up as it is (never a retryable 503 for data that can never succeed).
        compuertas.g8Notificaciones();
        if (aportes.consultaElFlujoDeCaja(ctx)) compuertas.g3SaldoFlujoCaja();
        if (!ctx.aportes.isEmpty() || !ctx.rescates.isEmpty()) compuertas.gCustodia();

        aportes.validarSaldo(ctx);
        rescates.prepararRescates(ctx);
        aportes.recalcularCostoUF(ctx);

        // gates that need the commission and the Spot flag that F-D leaves, still before the first write (the custody of the aportes)
        if (facturacion.facturaAlgunRescate(ctx)) compuertas.g6Facturacion();
        if (tesoreria.enviaATesoreria(ctx)) compuertas.g1MedioDistintoDeCin();
        if (spot.enviaASpot(ctx)) compuertas.g2Spot();

        aportes.ingresarCustodia(ctx);
        cuenta.moverAportes(ctx);
        rescates.retirarCustodiaYCostear(ctx);
        cuenta.moverRescates(ctx);
        tesoreria.moverTesoreria(ctx);
        facturacion.facturar(ctx);
        reinversion.reinvertir(ctx);
        spot.moverSpot(ctx);

        // front J, with the recursion and FM-CER-64 between its first and its second step (C:835-850)
        CertificarAporteRescateSwitch conmutador = new CertificarAporteRescateSwitch(hijaDe(profundidad), datos, escrituraMonto);
        cierre.actualizarYNotificar(ctx);
        conmutador.certificarAportesSwitch(ctx);
        conmutador.cambiarMontoMonedaSistema(ctx);
        cierre.notificarOwp(ctx);
        cierre.cerrar(ctx);
        return ctx.salida;
    }

    /** The port of the recursion that THIS level hands to the switch unit: one level deeper, guarded, and observed by {@code envolverHija} when the harness asks for it (null in production). */
    private CertificacionHijaPort hijaDe(int profundidad) {
        CertificacionHijaPort guardada = entrada -> ejecutar(entrada, profundidad + 1);
        return envolverHija != null ? envolverHija.apply(guardada) : guardada;
    }
}
