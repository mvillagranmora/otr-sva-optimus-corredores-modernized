package cl.bcs.fondomutuo.consultas.solicitudes;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.adapter.out.jdbc.SqlNombrado;
import cl.bcs.fondomutuo.adapter.out.jdbc.SqlRecursos;

/**
 * The read of the legacy SELECT, SQL verbatim, on the connection of the unit of work. The columns are mapped by the UNDERLYING column NAME, not the label
 * (D-F3A-28: {@code "IdSerieFondo" as "IdSerieFondoOrigen"} fills {@code IdSerieFondo}, as the legacy {@code SQL.queryParallel}).
 */
@Component
public class ConsultarSolicitudesJdbc implements ConsultarSolicitudesDatos {

    private static final String SQL_SELECT_SOLICITUDES = SqlRecursos.modulo("ConsultarSolicitudesFFMM_SelectSolicitudes.sql");

    private final ConexionUnidadDeTrabajo conexion;

    public ConsultarSolicitudesJdbc(ConexionUnidadDeTrabajo conexion) {
        this.conexion = conexion;
    }

    @Override
    public List<ConsultarSolicitudesContrato.Solicitud> solicitudes(Map<String, Object> parametros) {
        return conexion.usar(con -> SqlNombrado.consultarPorNombreDeColumna(con, SQL_SELECT_SOLICITUDES, parametros, ConsultarSolicitudesContrato.Solicitud.class));
    }
}
