package cl.bcs.fondomutuo.rescatereinversion;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

/**
 * The reinversion lock of {@code CrearSolicitudRescateReinversion} (D-F2-1, accepted by the user as NOT a database change: plan 12): the session-local
 * temporary table {@code #p_BloqueoProcesarReinversion} and the two AUTONOMOUS TRANSACTION procedures {@code CrearBloqueoProcesarReinversion} /
 * {@code EliminarBloqueoReinversion} (catalog: plan 13), on the connection of the unit of work (a local table exists only in its session), with the legacy SQL
 * files verbatim. Each method raises {@code AccesoDatosException} (cause {@code SQLException}) where the legacy raised {@code SQLException}. Unlike
 * Normal's lock (Step 6 H1) a failure of the INSERT or of the creating CALL drops the local table (DEV-6-H1, extended to this lock by the user on 2026-10-26; the
 * legacy left it, FM-REI-10). An exception raised LATER, after the lock was made, still leaves the lock and the table (D-F2-1, no catch in the legacy).
 */
public interface BloqueoReinversionPort {

    /** Legacy {@code CrearSolicitudRescateReinversion.BloqueoReinversion} (its own nested class, NOT the one of ProcesarReinversionRescate: C-W0A-4). */
    class Bloqueo {
        public String IdBloqueaReinversion;
        public Long IdPortafolio;
        public Long IdCliente;
        public String IdRegimenTributario;
        public String IdGrupoRegimen;
        public String IdSerieFondo;
        public BigDecimal Monto;
        public BigDecimal NumeroCuotas;
        public Date FechaSistema;
        public String Simula;

        public Bloqueo() {
            this.IdPortafolio = 0L;
            this.IdCliente = 0L;
            this.IdRegimenTributario = "";
            this.IdGrupoRegimen = "";
            this.Simula = "";
        }
    }

    /** {@code SelectReinversionRescate_Tabla_Bloqueo.sql}: the ids found, in the order the database returns them (no ORDER BY). */
    List<String> existentes(List<String> ids);

    /** {@code CREATE LOCAL TEMPORARY TABLE}, batch INSERT of the rows and {@code CALL CrearBloqueoProcesarReinversion} (commits on its own). */
    void crear(List<Bloqueo> bloqueos);

    /** {@code CALL EliminarBloqueoReinversion} (commits on its own) over the rows already in the local table. */
    void llamarEliminar();

    /** {@code DROP TABLE #p_BloqueoProcesarReinversion}. */
    void soltarTabla();
}
