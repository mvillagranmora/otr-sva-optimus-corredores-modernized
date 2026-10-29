package cl.bcs.tesoreria.jobs.guarda;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Classification of the collaborator names this module sends to {@code Colaboradores.invocar} (inventory JOBS_H1_H4_INVENTORY.md A.2: groups
 * TW = TomEE with writes, TL = TomEE read-only, M = already migrated), by what a REPEAT of the call does. Only
 * {@link Categoria#NO_REPETIBLE} and {@link Categoria#MIGRADO_ESCRITOR} are guarded.
 */
public final class ClasificacionColaborador {

    public enum Categoria {
        /** TW write that a repeat would duplicate or re-apply (orders, requests, events, programs, clients...): GUARDED. */
        NO_REPETIBLE(true),
        /** TW append-only audit log ({@code AgregarLog}): a duplicate line changes no business state: NOT guarded (see the notes). */
        APPEND_ONLY(false),
        /** Group M writer of ANOTHER module (APV, CuentaInversion): in process it joins the job transaction; if a call still reaches the HTTP port it commits apart: GUARDED there. */
        MIGRADO_ESCRITOR(true),
        /** Tesoreria's own services, always served in process inside the job transaction: nothing to guard. */
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
        // TW (A.2 #15-32), all but the log. ValidaEjecucionProcesoRecPAC only sets a flag (repeatable by value) but is guarded anyway: a TomEE
        // write whose state this module cannot read back.
        for (String n : Set.of("AgregarPrograma", "InscribirProcesoBatch", "AgregarEvento", "CrearEventoAutorizacionMasivo",
                "ModificarEstadoEventoAutorizacionMasivo", "ValidaEjecucionProcesoRecPAC", "CrearOrdenes", "CrearSolicitudAporteRescateNormal",
                "AnulacionContratoCSC", "RevocacionContratoCSC", "ModificarPortafolio", "CrearCliente", "CrearPersona", "CrearTelefono",
                "CrearDireccionElectronica", "CrearPersonaCuentaBancaria", "CrearMovimientoComprobante")) {
            m.put(n, Categoria.NO_REPETIBLE);
        }
        m.put("AgregarLog", Categoria.APPEND_ONLY);
        for (String n : Set.of("ValidarMandatosPAC", "CambiarEstadoContratosSAAPV", "CrearCargoAbono", "AnularCargoAbono")) {
            m.put(n, Categoria.MIGRADO_ESCRITOR);
        }
        for (String n : Set.of("CrearIngresoEgreso", "LiquidarComprobante", "CrearComprobante", "ActualizarSaldosCuentaBanco",
                "AnularNominasTransferencias")) {
            m.put(n, Categoria.MIGRADO_PROPIO);
        }
        for (String n : Set.of("ConsultarEstadoModulo", "ObtenerDiaAnterior", "RevisarRequisitosBasicos", "ConsultarFlujoCaja", "SaldoFlujoCaja",
                "CalcularParidad", "ConsultarInstrumentoPrecioBolsa", "CalcularMontoNumeroCuota", "CalcularMontoYCobrosOrden",
                "ObtenerPortafoliosEjecutivo", "ObtenerPortafoliosEjecutivo.restringeCartera", "ConsultarEmpresa", "ConsultarCustodiaPortafolio", "ConsultarFlujoCustodiaFondoMutuo", "ConsultarSaldoCuentaInversion")) {
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
