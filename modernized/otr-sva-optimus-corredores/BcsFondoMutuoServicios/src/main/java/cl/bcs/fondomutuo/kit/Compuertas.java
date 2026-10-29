package cl.bcs.fondomutuo.kit;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Gates G0..G5 of the F1 closure as CONFIGURATION (skeleton: no service uses them yet; steps 3-4 call them). FAIL CLOSED: a property that is missing or
 * false keeps the gate shut. A gate opens with any value that the {@code @Value} boolean conversion of Spring reads as true: {@code true}, {@code yes}, {@code on} or
 * {@code 1} (case-insensitive); a value that is none of the boolean spellings makes the start-up fail. Always configure exactly {@code true} or {@code false}. A
 * shut gate raises {@link ServicioNoServidoException} (503) so the router repeats the whole request in
 * TomEE. Callers must evaluate them AFTER all validations and BEFORE the first write, and release the anti-duplicate lock before the exception leaves
 * (a leftover lock would make TomEE answer "duplicate request"). G5 (a write collaborator fails or the router answers 503) is not a property: it is the
 * behaviour of {@code RouterClient}. The state is immutable and read once at start-up (no state between requests).
 */
@Component
public class Compuertas {

    public static final String LISTA_NORMAL = "NORMAL";

    private final Set<String> listasServidas;
    private final boolean tesoreria;
    private final boolean spot;
    private final boolean flujoCaja;
    private final boolean owp;
    private final boolean custodia;
    private final boolean custodiaNormal;
    private final boolean comisionesEnProceso;

    /** Whether G-CUS is open (the custody movements are enabled by configuration): the in-process custody for RescateReinversion and the in-process adapter. */
    public boolean custodiaHabilitada() {
        return custodia;
    }

    /** Whether Normal may call the custody flow ({@code fondomutuo.custodia.normal-habilitada}; with the 6-argument constructor it follows {@link #custodiaHabilitada()}). */
    public boolean custodiaNormalHabilitada() {
        return custodiaNormal;
    }

    /** Whether {@code CalcularComisiones} uses the in-process custody adapter ({@code fondomutuo.custodia.comisiones-en-proceso}); false = the HTTP port as before. */
    public boolean comisionesEnProceso() {
        return comisionesEnProceso;
    }

    /**
     * Compatibility constructor (harnesses and tests): Normal follows the custody gate ({@code custodiaNormal = custodia}) and the commissions stay on the HTTP
     * port ({@code comisionesEnProceso = false}).
     */
    public Compuertas(String listasServidas, boolean tesoreria, boolean spot, boolean flujoCaja, boolean owp, boolean custodia) {
        this(listasServidas, tesoreria, spot, flujoCaja, owp, custodia, custodia, false);
    }

    @Autowired
    public Compuertas(@Value("${fondomutuo.solicitud.listas-servidas:}") String listasServidas,
            @Value("${fondomutuo.tesoreria.habilitada:false}") boolean tesoreria,
            @Value("${fondomutuo.spot.habilitado:false}") boolean spot,
            @Value("${fondomutuo.flujo-caja.habilitado:false}") boolean flujoCaja,
            @Value("${fondomutuo.owp.habilitado:false}") boolean owp,
            @Value("${fondomutuo.custodia.habilitada:false}") boolean custodia,
            @Value("${fondomutuo.custodia.normal-habilitada:false}") boolean custodiaNormal,
            @Value("${fondomutuo.custodia.comisiones-en-proceso:false}") boolean comisionesEnProceso) {
        this.listasServidas = Arrays.stream(listasServidas.split(",")).map(s -> s.trim().toUpperCase(Locale.ROOT)).filter(s -> !s.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
        this.tesoreria = tesoreria;
        this.spot = spot;
        this.flujoCaja = flujoCaja;
        this.owp = owp;
        this.custodia = custodia;
        this.custodiaNormal = custodiaNormal;
        this.comisionesEnProceso = comisionesEnProceso;
    }

    public Set<String> listasServidas() {
        return listasServidas;
    }

    /** G0: every list of the Manejador that has rows must be served; evaluated before simulating or writing anything (F2 doc 6.1). */
    public void g0ListasConFilas(Set<String> listasConFilas) {
        for (String lista : listasConFilas) {
            if (!listasServidas.contains(lista)) {
                throw new ServicioNoServidoException("G0", "list " + lista + " is not served by this module");
            }
        }
    }

    /** G1: contribution with a payment medium other than CIN (needs CrearIngresoEgreso). */
    public void g1MedioDistintoDeCin() {
        cerrada(tesoreria, "G1", "non-CIN contribution needs Tesoreria CrearIngresoEgreso");
    }

    /** G2: GeneraSpot=S or p_FormaPagoSpot with rows (needs Mesa CrearOperacionSpot). */
    public void g2Spot() {
        cerrada(spot, "G2", "Spot operation needs Mesa CrearOperacionSpot");
    }

    /** G3: manual-origin CIN contribution (needs SaldoFlujoCaja as JavaCall). */
    public void g3SaldoFlujoCaja() {
        cerrada(flujoCaja, "G3", "needs Plataforma SaldoFlujoCaja as JavaCall");
    }

    /** G4: channel OWP with PLA.NotificarEstadoOrdenOWP=S. */
    public void g4Owp() {
        cerrada(owp, "G4", "needs NotificarEstadoOrdenesOWP");
    }

    /** G-CUS (F2): custody through HTTP breaks atomicity; open (property true) only with the in-process adapter, see {@code custodia.CustodiaEnProcesoArranque}. */
    public void gCustodia() {
        cerrada(custodia, "G-CUS", "custody movements are not atomic by HTTP");
    }

    /**
     * G-CUS for NORMAL ({@code fondomutuo.custodia.normal-habilitada}, default false; the 6-argument constructor makes it follow G-CUS). Evaluated before the custody flow
     * of Normal is called; same exception and text as {@link #gCustodia()}.
     */
    public void gCustodiaNormal() {
        cerrada(custodiaNormal, "G-CUS", "custody movements are not atomic by HTTP");
    }

    private static void cerrada(boolean abierta, String compuerta, String motivo) {
        if (!abierta) {
            throw new ServicioNoServidoException(compuerta, motivo);
        }
    }
}
