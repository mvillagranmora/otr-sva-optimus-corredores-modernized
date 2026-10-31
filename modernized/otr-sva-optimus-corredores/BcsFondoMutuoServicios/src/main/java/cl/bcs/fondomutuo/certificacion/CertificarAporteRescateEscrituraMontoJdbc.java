package cl.bcs.fondomutuo.certificacion;

import java.util.List;

import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.adapter.out.jdbc.SqlRecursos;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.SolicitudReinversion;
import cl.bcs.fondomutuo.requisitos.SqlLote;

/** {@link CertificarAporteRescateEscrituraMonto} over the legacy UPDATE, byte for byte (no bean until T5). */
public class CertificarAporteRescateEscrituraMontoJdbc implements CertificarAporteRescateEscrituraMonto {

    private static final String SQL_UPDATE_MONTO_MONEDA_SISTEMA = SqlRecursos.modulo("CertificarSolicitudAporteRescate_UpdateMontoMonedaSistemaSolicitudesReinversion.sql");

    private final ConexionUnidadDeTrabajo conexion;

    public CertificarAporteRescateEscrituraMontoJdbc(ConexionUnidadDeTrabajo conexion) {
        this.conexion = conexion;
    }

    @Override
    public void actualizarMontoMonedaSistema(List<SolicitudReinversion> filas) {
        if (filas.isEmpty()) {
            return;
        }
        conexion.usar(con -> SqlLote.ejecutar(con, SQL_UPDATE_MONTO_MONEDA_SISTEMA, filas));
    }
}
