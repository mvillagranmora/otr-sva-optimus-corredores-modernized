package cl.bcs.contabilidad.jobs.guarda;

import java.util.Map;
import java.util.TreeMap;

/**
 * Classification of the collaborator names this module can send over HTTP to TomEE (inventory JOBS_H1_H4_INVENTORY.md A.2/A.3: groups TW = TomEE
 * with writes, TL = TomEE read-only, M = already migrated), by what a REPEAT of the call does. Only {@link Categoria#NO_REPETIBLE} and
 * {@link Categoria#MIGRADO_ESCRITOR} are guarded.
 *
 * <p>Unlike Tesoreria / APV, this module has no name-based collaborator port: it reaches TomEE through two typed ports
 * ({@code PlataformaPort}, {@code ProcesoBatchPort}) whose HTTP adapters post to {@code /api/<owner>/<Name>}. The names are those path segments;
 * {@link #PUERTOS} maps every method of both ports to its name, and a test fails on a method, or an HTTP path of the Servicios adapters, that is
 * not classified here.
 */
public final class ClasificacionColaborador {

    public enum Categoria {
        /** TW write that a repeat would duplicate or re-apply (batch programs, orders, events...): GUARDED. */
        NO_REPETIBLE(true),
        /** TW append-only audit log ({@code AgregarLog}): NOT guarded. This module does not send it; the category exists for the other modules. */
        APPEND_ONLY(false),
        /** Group M writer of another module: in process it joins the job transaction; if a call still reaches the HTTP port it commits apart: GUARDED there. */
        MIGRADO_ESCRITOR(true),
        /** Group M that only reads (Tesoreria {@code ConsultarClearingFinanciero}): served in process, and a repeat over HTTP is harmless: NOT guarded. */
        MIGRADO_LECTURA(false),
        /** TL (read-only): a repeat is harmless. */
        SOLO_LECTURA(false),
        /** Not an HTTP collaborator: JDBC on the caller's connection and transaction. */
        ESPECIAL(false);

        private final boolean guardada;

        Categoria(boolean guardada) {
            this.guardada = guardada;
        }

        public boolean guardada() {
            return guardada;
        }
    }

    /** Name used for the port methods that never leave the JVM (JDBC in the job connection). */
    public static final String EN_CONEXION = "JdbcEnConexion";
    public static final String INSCRIBIR_PROCESO_BATCH = "InscribirProcesoBatch";

    private static final Map<String, Categoria> POR_NOMBRE = construirNombres();
    private static final Map<String, String> PUERTOS = construirPuertos();

    private ClasificacionColaborador() {
    }

    private static Map<String, Categoria> construirNombres() {
        Map<String, Categoria> m = new TreeMap<>();
        m.put(INSCRIBIR_PROCESO_BATCH, Categoria.NO_REPETIBLE); // TW #17: registers a BAT program in TomEE
        m.put("ObtenerDiaAnterior", Categoria.SOLO_LECTURA); // TL #34
        m.put("ConsultarClearingFinanciero", Categoria.MIGRADO_LECTURA); // M #11: Tesoreria, in process when its dispatcher serves it
        m.put(EN_CONEXION, Categoria.ESPECIAL);
        return Map.copyOf(m);
    }

    /** {@code Port.method} -> the collaborator name that method sends over HTTP (or {@link #EN_CONEXION}). Every method of both ports must be here. */
    private static Map<String, String> construirPuertos() {
        Map<String, String> m = new TreeMap<>();
        m.put("PlataformaPort.consultarEmpresa", EN_CONEXION);
        m.put("PlataformaPort.obtenerDiaAnterior", "ObtenerDiaAnterior");
        m.put("PlataformaPort.obtenerFechaSistema", EN_CONEXION);
        m.put("PlataformaPort.obtenerLocalizacionSistema", EN_CONEXION);
        m.put("PlataformaPort.obtenerMonedaSistema", EN_CONEXION);
        m.put("PlataformaPort.obtenerParametroConfiguracion", EN_CONEXION);
        m.put("PlataformaPort.consultarClearingFinanciero", "ConsultarClearingFinanciero");
        m.put("ProcesoBatchPort.inscribir", INSCRIBIR_PROCESO_BATCH);
        return Map.copyOf(m);
    }

    /** The category of a name, or {@code null} when this module does not know it (the decorators then pass it through). */
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

    /** Every port method and the name it sends, for the tests and the notes. */
    public static Map<String, String> puertos() {
        return new TreeMap<>(PUERTOS);
    }
}
