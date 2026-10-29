package cl.bcs.fondomutuo.normal;

import java.math.BigDecimal;
import java.util.List;

/**
 * The anti-duplicate lock of {@code CrearSolicitudAporteRescateNormal} (plan 6.5, accepted by the user as NOT a database change): the session-local
 * temporary table {@code #p_BloqueoSolicitudAporteRescate} and the two AUTONOMOUS TRANSACTION procedures {@code CrearBloqueoSolicitudAporteRescate} /
 * {@code EliminarBloqueoSolicitudAporteRescate}, everything on the connection of the unit of work (a local table exists only in its session). Each
 * method raises {@code AccesoDatosException} (cause {@code SQLException}) on a database failure, where the legacy raised {@code SQLException}.
 */
public interface BloqueoSolicitudPort {

    /** Legacy {@code CrearSolicitudAporteRescateNormal.BloqueoSolicitudAporteRescate}: field names are the SQL parameters. */
    class Bloqueo {
        public String IdBloqueoSolicitudAporteRescate;
        public Long IdPortafolio;
        public String IdSerieFondo;
        public String IdRegimenTributario;
        public String TipoSolicitud;
        public BigDecimal Monto;
        public BigDecimal NumeroCuotas;
        public String FechaSistema;
    }

    /** {@code SELECT ... WHERE id IN (...)}: the ids that exist, in the order the database returns them (no ORDER BY, D-NOR-2). */
    List<String> existentes(List<String> ids);

    /** {@code CREATE LOCAL TEMPORARY TABLE}, batch INSERT of the locks and {@code CALL CrearBloqueoSolicitudAporteRescate} (commits on its own). */
    void crear(List<Bloqueo> bloqueos);

    /** {@code CALL EliminarBloqueoSolicitudAporteRescate} (commits on its own) with the rows already in the local table. */
    void llamarEliminar(List<Bloqueo> bloqueos);

    /** {@code DROP TABLE #p_BloqueoSolicitudAporteRescate}. */
    void soltarTabla();
}
