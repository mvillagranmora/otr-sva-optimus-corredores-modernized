package cl.bcs.fondomutuo.adapter.out.jdbc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import cl.bcs.fondomutuo.comisiones.ComisionesPort;
import cl.bcs.fondomutuo.comisiones.TramoComision;

/** {@link ComisionesPort} over {@code Comunes_SelectTramoComision.sql} (verbatim legacy SQL, SELECT only, step 3A). */
@Component
public class ComisionesJdbc implements ComisionesPort {

    private static final String SQL_SELECT_TRAMO_COMISION = SqlRecursos.modulo("Comunes_SelectTramoComision.sql");

    private final ConexionUnidadDeTrabajo conexion;

    public ComisionesJdbc(ConexionUnidadDeTrabajo conexion) {
        this.conexion = conexion;
    }

    @Override
    public List<TramoComision> tramos(Long idEmpresa, List<String> idSerieFondo) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdSerieFondo", idSerieFondo);
        params.put("IdEmpresa", idEmpresa);
        return conexion.usar(con -> SqlNombrado.consultar(con, SQL_SELECT_TRAMO_COMISION, params, TramoComision.class));
    }
}
