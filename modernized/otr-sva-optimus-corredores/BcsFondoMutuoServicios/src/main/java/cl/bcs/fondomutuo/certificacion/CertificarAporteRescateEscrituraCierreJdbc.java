package cl.bcs.fondomutuo.certificacion;

import java.util.List;

import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.adapter.out.jdbc.SqlRecursos;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.SolicitudProceso;
import cl.bcs.fondomutuo.requisitos.SqlLote;

/**
 * {@link CertificarAporteRescateEscrituraCierre} over the legacy UPDATE ({@code CertificarSolicitudAporteRescate_UpdateSolicitudes.sql}), byte for byte: no guard of state in the WHERE
 * (D-F3B-16), the clock is the database's {@code CURRENT_UTCTIMESTAMP}. The legacy runs the batch only for a non-empty list.
 */
public class CertificarAporteRescateEscrituraCierreJdbc implements CertificarAporteRescateEscrituraCierre {

    private static final String SQL_UPDATE_SOLICITUDES = SqlRecursos.modulo("CertificarSolicitudAporteRescate_UpdateSolicitudes.sql");

    private final ConexionUnidadDeTrabajo conexion;

    public CertificarAporteRescateEscrituraCierreJdbc(ConexionUnidadDeTrabajo conexion) {
        this.conexion = conexion;
    }

    @Override
    public void actualizarSolicitudes(List<SolicitudProceso> solicitudes) {
        if (solicitudes.isEmpty()) {
            return;
        }
        conexion.usar(con -> SqlLote.ejecutar(con, SQL_UPDATE_SOLICITUDES, solicitudes));
    }
}
