package cl.bcs.fondomutuo.agf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.adapter.out.jdbc.SqlNombrado;
import cl.bcs.fondomutuo.adapter.out.jdbc.SqlRecursos;
import cl.bcs.fondomutuo.requisitos.SqlLote;

/** {@link AgfDatos} over the legacy SQL, verbatim (the two INSERT into {@code FMM.InterfazAgf} are the accepted writes of this class). */
@Component
public class AgfJdbc implements AgfDatos {

    private static final String SQL_SELECT_SOLICITUD = SqlRecursos.modulo("CrearSolicitudAGF_SelectSolicitud.sql");
    private static final String SQL_SELECT_BENEFICIARIO = SqlRecursos.modulo("CrearSolicitudAGF_SelectBeneficiario.sql");
    private static final String SQL_INSERT_BENEFICIARIO = SqlRecursos.modulo("CrearSolicitudAGF_InsertBeneficiario.sql");
    private static final String SQL_INSERT_SOLICITUD = SqlRecursos.modulo("CrearSolicitudAGF_InsertSolicitud.sql");

    private final ConexionUnidadDeTrabajo conexion;

    public AgfJdbc(ConexionUnidadDeTrabajo conexion) {
        this.conexion = conexion;
    }

    private static Map<String, Object> ids(List<Long> idsSolicitud) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdSolicitudAporteRescate", idsSolicitud);
        return params;
    }

    @Override
    public List<SelectBeneficiario> beneficiariosNuevos(List<Long> idsSolicitud) {
        return conexion.usar(con -> SqlNombrado.consultar(con, SQL_SELECT_BENEFICIARIO, ids(idsSolicitud), SelectBeneficiario.class));
    }

    @Override
    public List<SelectSolicitud> solicitudes(List<Long> idsSolicitud) {
        return conexion.usar(con -> SqlNombrado.consultar(con, SQL_SELECT_SOLICITUD, ids(idsSolicitud), SelectSolicitud.class));
    }

    @Override
    public void insertarBeneficiarios(List<InsertBeneficiario> filas) {
        cl.bcs.fondomutuo.kit.EscrituraConfirmada.escrituraLocalPendiente();   // M3: the lock DDL commits it on HANA
        conexion.usar(con -> SqlLote.ejecutar(con, SQL_INSERT_BENEFICIARIO, filas));
    }

    @Override
    public void insertarSolicitudes(List<InsertSolicitud> filas) {
        cl.bcs.fondomutuo.kit.EscrituraConfirmada.escrituraLocalPendiente();   // M3: the lock DDL commits it on HANA
        conexion.usar(con -> SqlLote.ejecutar(con, SQL_INSERT_SOLICITUD, filas));
    }
}
