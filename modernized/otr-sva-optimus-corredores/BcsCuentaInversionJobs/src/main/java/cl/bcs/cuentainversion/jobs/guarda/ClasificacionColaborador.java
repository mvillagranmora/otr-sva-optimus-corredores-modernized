package cl.bcs.cuentainversion.jobs.guarda;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Classification of the collaborator names this module sends to {@code Colaboradores.invocar} (inventory JOBS_H1_H4_INVENTORY.md A.2: groups
 * TW = TomEE with writes, TL = TomEE read-only, M = already migrated), by what a REPEAT of the call does. Only
 * {@link Categoria#NO_REPETIBLE} and {@link Categoria#MIGRADO_ESCRITOR} are guarded.
 *
 * <p>Unlike the other Jobs modules, the names here are the FULL legacy class names (router keys of A.2), because that is what the services of
 * bcs-cuenta-inversion-servicios send ({@code cl.bcs.plataforma.bat.InscribirProcesoBatch}, ...): the key is the exact string passed to the port.
 */
public final class ClasificacionColaborador {

    public enum Categoria {
        /** TW write that a repeat would duplicate or re-apply (programs, events, orders, requests, clients...): GUARDED. */
        NO_REPETIBLE(true),
        /** TW append-only audit log ({@code AgregarLog}): a duplicate line changes no business state: NOT guarded (see the notes). */
        APPEND_ONLY(false),
        /**
         * Group M writer of ANOTHER module (APV, Tesoreria): this module has no in-process dispatch for it, so it travels over the HTTP router
         * and commits apart from the job transaction: GUARDED.
         */
        MIGRADO_ESCRITOR(true),
        /**
         * CuentaInversion's own group M services ({@code cl.bcs.cuentaInversion.*}): {@code ColaboradoresJobs} answers them in process through
         * {@code JavaCallDispatcher}, inside the job transaction, and they never reach the HTTP router: nothing to guard.
         */
        MIGRADO_PROPIO(false),
        /** TL (read-only or staging tables): a repeat is harmless. */
        SOLO_LECTURA(false),
        /** {@code StoredProcedure} (JDBC in the job connection) and {@code Cloper} (not migrated: 503). */
        ESPECIAL(false);

        private final boolean guardada;

        Categoria(boolean guardada) {
            this.guardada = guardada;
        }

        public boolean guardada() {
            return guardada;
        }
    }

    private static final Map<String, Categoria> POR_NOMBRE = construir();

    private ClasificacionColaborador() {
    }

    private static Map<String, Categoria> construir() {
        Map<String, Categoria> m = new TreeMap<>();
        // TW (A.2 #16-32), all but the log. ValidaEjecucionProcesoRecPAC only sets a flag (repeatable by value) but is guarded anyway: a TomEE
        // write whose state this module cannot read back.
        for (String n : Set.of("cl.bcs.plataforma.bat.AgregarPrograma", "cl.bcs.plataforma.bat.InscribirProcesoBatch",
                "cl.bcs.plataforma.evt.AgregarEvento", "cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo",
                "cl.bcs.plataforma.aut.ModificarEstadoEventoAutorizacionMasivo", "cl.bcs.plataforma.pla.ValidaEjecucionProcesoRecPAC",
                "cl.bcs.ordenes.CrearOrdenes", "cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal", "cl.bcs.fondomutuo.AnulacionContratoCSC",
                "cl.bcs.fondomutuo.RevocacionContratoCSC", "cl.bcs.portafolio.ModificarPortafolio", "cl.bcs.socionegocio.cli.CrearCliente",
                "cl.bcs.socionegocio.per.CrearPersona", "cl.bcs.socionegocio.per.CrearTelefono",
                "cl.bcs.socionegocio.per.CrearDireccionElectronica", "cl.bcs.socionegocio.per.CrearPersonaCuentaBancaria",
                "cl.bcs.facturacion.CrearMovimientoComprobante")) {
            m.put(n, Categoria.NO_REPETIBLE);
        }
        m.put("cl.bcs.plataforma.pla.AgregarLog", Categoria.APPEND_ONLY);
        // Group M writers owned by APV and Tesoreria (A.2 #1-2, #3-10): for this module they travel over the router and commit apart.
        for (String n : Set.of("cl.bcs.APV.ValidarMandatosPAC", "cl.bcs.APV.CambiarEstadoContratosSAAPV", "cl.bcs.tesoreria.CrearIngresoEgreso",
                "cl.bcs.tesoreria.LiquidarComprobante", "cl.bcs.tesoreria.CrearComprobante", "cl.bcs.tesoreria.ban.ActualizarSaldosCuentaBanco",
                "cl.bcs.tesoreria.nor.AnularNominasTransferencias", "cl.bcs.tesoreria.AnulacionMandatoPAC",
                "cl.bcs.tesoreria.RevocacionMandatoPAC", "cl.bcs.tesoreria.IngresarMandatoPAC")) {
            m.put(n, Categoria.MIGRADO_ESCRITOR);
        }
        // CuentaInversion's own (A.2 #12-14): in process through JavaCallDispatcher (ConsultarSaldoCuentaInversion is read-only).
        for (String n : Set.of("cl.bcs.cuentaInversion.CrearCargoAbono", "cl.bcs.cuentaInversion.AnularCargoAbono",
                "cl.bcs.cuentaInversion.ConsultarSaldoCuentaInversion")) {
            m.put(n, Categoria.MIGRADO_PROPIO);
        }
        for (String n : Set.of("cl.bcs.tesoreria.liq.ConsultarClearingFinanciero", "cl.bcs.plataforma.pla.ConsultarEstadoModulo",
                "cl.bcs.plataforma.pla.ObtenerDiaAnterior", "cl.bcs.plataforma.pla.utils.ConsultarEmpresa", "cl.bcs.plataforma.pla.ConsultarFlujoCaja",
                "cl.bcs.plataforma.pla.SaldoFlujoCaja", "cl.bcs.plataforma.mer.CalcularParidad", "cl.bcs.plataforma.pla.utils.ConsultarImagen",
                "cl.bcs.plataforma.tas.EntregarPreciosTasas", "cl.bcs.plataforma.pla.RevisarRequisitosBasicos",
                "cl.bcs.plataforma.ins.ConsultarInstrumentoPrecioBolsa", "cl.bcs.ordenes.CalcularMontoYCobrosOrden",
                "cl.bcs.custodia.ConsultarCustodiaPortafolio", "cl.bcs.fondomutuo.ConsultarFlujoCustodiaFondoMutuo",
                "cl.bcs.fondomutuo.CalcularMontoNumeroCuota", "cl.bcs.fondomutuo.ConsultarCustodiaPortafolioFondoMutuo",
                "cl.bcs.socionegocio.cli.ObtenerPortafoliosEjecutivo", "cl.bcs.socionegocio.cli.ObtenerPortafoliosEjecutivo.restringeCartera",
                "cl.bcs.normativa.cartola.MovimientosTitulos", "cl.bcs.normativa.cartola.MovimientosCaja",
                "cl.bcs.normativa.cartola.DatosCartolaSocioNegocio")) {
            m.put(n, Categoria.SOLO_LECTURA);
        }
        m.put("StoredProcedure", Categoria.ESPECIAL);
        m.put("Cloper", Categoria.ESPECIAL);
        return Map.copyOf(m);
    }

    /** The category of a name, or {@code null} when this module does not know it (the decorator then passes it through). */
    public static Categoria de(String servicio) {
        return servicio == null ? null : POR_NOMBRE.get(servicio);
    }

    public static boolean guardada(String servicio) {
        Categoria c = de(servicio);
        return c != null && c.guardada();
    }

    /** Every classified name, for the tests and the notes. */
    public static Map<String, Categoria> todas() {
        return new TreeMap<>(POR_NOMBRE);
    }
}
