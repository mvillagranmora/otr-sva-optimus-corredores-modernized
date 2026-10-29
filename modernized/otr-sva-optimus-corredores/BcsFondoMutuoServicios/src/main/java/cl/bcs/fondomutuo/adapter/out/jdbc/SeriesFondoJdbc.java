package cl.bcs.fondomutuo.adapter.out.jdbc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import cl.bcs.fondomutuo.cierre.SerieFondoSolicitudes;
import cl.bcs.fondomutuo.cierre.SeriesFondoPort;

/** {@link SeriesFondoPort} over {@code ComunesSolicitudes_SelectSerieFondo.sql} (verbatim legacy SQL, SELECT only, step 3A). */
@Component
public class SeriesFondoJdbc implements SeriesFondoPort {

    private static final String SQL_SELECT_SERIE_FONDO = SqlRecursos.modulo("ComunesSolicitudes_SelectSerieFondo.sql");

    private final ConexionUnidadDeTrabajo conexion;

    public SeriesFondoJdbc(ConexionUnidadDeTrabajo conexion) {
        this.conexion = conexion;
    }

    @Override
    public List<SerieFondoSolicitudes> series(Long idEmpresa, List<String> idSerieFondo) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdSerieFondo", idSerieFondo);
        params.put("IdEmpresa", idEmpresa);
        return conexion.usar(con -> SqlNombrado.consultar(con, SQL_SELECT_SERIE_FONDO, params, SerieFondoSolicitudes.class));
    }
}
