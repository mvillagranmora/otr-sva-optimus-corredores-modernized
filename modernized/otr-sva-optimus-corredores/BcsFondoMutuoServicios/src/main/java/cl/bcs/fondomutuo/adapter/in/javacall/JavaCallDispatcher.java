package cl.bcs.fondomutuo.adapter.in.javacall;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The allow-list of the JavaCall classes this module may serve (D-4 / DEV-F1): EXACTLY twelve legacy FQN (the Manejador, Normal, RescateReinversion, the eight F3 W1 UI reads and the F3 W2 CargarValorCuota), taken from the router registry
 * (ids 601190 and 357; F2 W1 adds 1436 CrearSolicitudRescateReinversion, disabled by configuration) and the legacy package. Everything else (the ~130 legacy stubs included, which are no longer in the Spring context) is "not
 * served" and the controller answers 503 so the router falls back to TomEE. A whitelisted class with no handler registered yet is ALSO not served: in
 * this step (kit) both are unserved; the handlers arrive in steps 3-4. A {@link JavaCallHandler} for any other FQN stops the start-up: the list cannot
 * grow by accident.
 */
@Component
public class JavaCallDispatcher {

    public static final String MANEJADOR = "cl.bcs.fondomutuo.ManejadorSolicitudAporteRescate";
    public static final String CREAR_SOLICITUD_NORMAL = "cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal";
    /** F2 W1 front 2 (registry id 1436, the only F2 class with direct use, plan 6.3): its handler answers 503 unless its list is configured as served. */
    public static final String CREAR_SOLICITUD_RESCATE_REINVERSION = "cl.bcs.fondomutuo.CrearSolicitudRescateReinversion";
    /** F3 W1 front 1: read-only entry of the UI (registry id 3022), served only when its list is open. */
    public static final String CONSULTAR_FECHA_SOLICITUD_APORTE_RESCATE = "cl.bcs.fondomutuo.ConsultarFechaSolicitudAporteRescate";
    /** F3 W1 reads (SFM, SFM-WS, FMR, IFM, VDO, DET, CPF): read-only entries, each served only when its own list is open; all are in {@link #LISTA_BLANCA}. */
    public static final String CONSULTAR_SOLICITUDES_FFMM = "cl.bcs.fondomutuo.ConsultarSolicitudesFFMM";
    public static final String CONSULTAR_SOLICITUDES_FFMM_WS = "cl.bcs.fondomutuo.ConsultarSolicitudesFFMMWS";
    public static final String CONSULTAR_FONDO_MUTUO_REINVERSION = "cl.bcs.fondomutuo.ConsultarFondoMutuoReinversion";
    public static final String CONSULTAR_INSTRUMENTOS_FONDOS_MUTUOS = "cl.bcs.fondomutuo.ConsultarInstrumentosFondosMutuos";
    public static final String CONSULTAR_VALOR_DOLAR_FONDO_MUTUO = "cl.bcs.fondomutuo.ConsultarValorDolarFondoMutuo";
    public static final String CONSULTAR_DETALLE_SOLICITUD = "cl.bcs.fondomutuo.ConsultarDetalleSolicitud";
    public static final String CONSULTAR_CUSTODIA_PORTAFOLIO_FONDO_MUTUO = "cl.bcs.fondomutuo.ConsultarCustodiaPortafolioFondoMutuo";
    /** F3 W2: the only UI class that WRITES (registry id 3035); in {@link #LISTA_BLANCA} and NOT in {@link #CLASES_LECTURA} (its unit of work is read-write); served only when its list is open. */
    public static final String CARGAR_VALOR_CUOTA = "cl.bcs.fondomutuo.CargarValorCuota";

    /**
     * The eight UI reads (architecture review F3 W1, H2): {@link JavaCallUnitOfWorkFilter} opens their transaction READ ONLY. Writes and the Manejador are never in it.
     */
    public static final Set<String> CLASES_LECTURA = Set.of(CONSULTAR_FECHA_SOLICITUD_APORTE_RESCATE, CONSULTAR_FONDO_MUTUO_REINVERSION,
            CONSULTAR_INSTRUMENTOS_FONDOS_MUTUOS, CONSULTAR_VALOR_DOLAR_FONDO_MUTUO, CONSULTAR_SOLICITUDES_FFMM, CONSULTAR_SOLICITUDES_FFMM_WS,
            CONSULTAR_DETALLE_SOLICITUD, CONSULTAR_CUSTODIA_PORTAFOLIO_FONDO_MUTUO);

    /** The whole allow-list. Adding a class here is a decision of the plan (F2 waves), never a side effect of adding a bean. */
    public static final Set<String> LISTA_BLANCA = Set.of(MANEJADOR, CREAR_SOLICITUD_NORMAL, CREAR_SOLICITUD_RESCATE_REINVERSION,
            CONSULTAR_FECHA_SOLICITUD_APORTE_RESCATE, CONSULTAR_FONDO_MUTUO_REINVERSION, CONSULTAR_INSTRUMENTOS_FONDOS_MUTUOS, CONSULTAR_VALOR_DOLAR_FONDO_MUTUO,
            CONSULTAR_SOLICITUDES_FFMM, CONSULTAR_SOLICITUDES_FFMM_WS, CONSULTAR_DETALLE_SOLICITUD, CONSULTAR_CUSTODIA_PORTAFOLIO_FONDO_MUTUO, CARGAR_VALOR_CUOTA);

    private final Map<String, JavaCallHandler> porFqn;

    /** Spring constructor: there may be NO handler yet (kit step), which a plain {@code List} injection would reject. */
    @Autowired
    public JavaCallDispatcher(ObjectProvider<JavaCallHandler> handlers) {
        this(handlers.orderedStream().toList());
    }

    public JavaCallDispatcher(List<JavaCallHandler> handlers) {
        for (JavaCallHandler h : handlers) {
            if (!LISTA_BLANCA.contains(h.fqn())) {
                throw new IllegalStateException("JavaCallHandler for a class outside the allow-list: " + h.fqn());
            }
        }
        this.porFqn = handlers.stream().collect(Collectors.toUnmodifiableMap(JavaCallHandler::fqn, Function.identity()));
    }

    /** The handler to run, or empty when the class is not on the allow-list or has no handler yet (both: 503). */
    public Optional<JavaCallHandler> buscar(String fqn) {
        return Optional.ofNullable(fqn == null ? null : porFqn.get(fqn));
    }

    /** FQN with a handler registered (a subset of {@link #LISTA_BLANCA}). */
    public Set<String> servidas() {
        return porFqn.keySet();
    }
}
