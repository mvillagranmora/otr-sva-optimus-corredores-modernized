package cl.bcs.fondomutuo.requisitos;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.adapter.out.jdbc.SqlNombrado;
import cl.bcs.fondomutuo.adapter.out.jdbc.SqlRecursos;

/** {@link RequisitosDatos} over the legacy SQL, verbatim. Read only. */
@Component
public class RequisitosJdbc implements RequisitosDatos {

    private static final String SQL_SELECT_PORTAFOLIO = SqlRecursos.modulo("ComunesSolicitudes_SelectPortafolio.sql");
    private static final String SQL_SELECT_EMPLEADO_USUARIO = SqlRecursos
            .modulo("ServicioCumpleRequisitosCrearSolicitudAporteRescate_SelectEmpleadoUsuario.sql");

    private final ConexionUnidadDeTrabajo conexion;

    public RequisitosJdbc(ConexionUnidadDeTrabajo conexion) {
        this.conexion = conexion;
    }

    @Override
    public List<ClientePortafolioSolicitudes> clientesPortafolio(List<Long> idsPortafolio) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdPortafolio", idsPortafolio);
        return conexion.usar(con -> SqlNombrado.consultar(con, SQL_SELECT_PORTAFOLIO, params, ClientePortafolioSolicitudes.class));
    }

    @Override
    public List<ObjValidaProducto> empleadoUsuario(Long idUsuario) {
        Map<String, Object> params = new HashMap<>();
        params.put("p_IdUsuario", idUsuario);
        return conexion.usar(con -> SqlNombrado.consultar(con, SQL_SELECT_EMPLEADO_USUARIO, params, ObjValidaProducto.class));
    }
}
