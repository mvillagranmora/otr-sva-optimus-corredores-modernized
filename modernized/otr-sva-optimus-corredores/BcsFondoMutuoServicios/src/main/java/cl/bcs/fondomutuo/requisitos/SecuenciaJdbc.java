package cl.bcs.fondomutuo.requisitos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.adapter.out.jdbc.SqlNombrado;

/**
 * {@link SecuenciaPort} for HANA: the legacy statement of {@code Utils.getSecuencia} (HANA branch) as is. It is a SELECT (reading a sequence is not a
 * schema change). H2 has no {@code SERIES_GENERATE_BIGINT}: the H2 tests replace this port by a stub that reads the H2 sequence of the same name.
 */
@Component
public class SecuenciaJdbc implements SecuenciaPort {

    /** Row of the statement. */
    public static class Secuencia {
        public Long Id;
    }

    private final ConexionUnidadDeTrabajo conexion;

    public SecuenciaJdbc(ConexionUnidadDeTrabajo conexion) {
        this.conexion = conexion;
    }

    @Override
    public List<Long> siguientes(String nombreSecuencia, Long cantidad) {
        List<Long> salida = new ArrayList<>();
        if (cantidad == null || cantidad <= 0) {
            return salida;
        }
        String query = "SELECT \"" + nombreSecuencia + "\".NEXTVAL AS \"Id\" FROM SERIES_GENERATE_BIGINT(1, 0, :cantidad)";
        Map<String, Object> params = new HashMap<>();
        params.put("cantidad", cantidad);
        List<Secuencia> filas = conexion.usar(con -> SqlNombrado.consultar(con, query, params, Secuencia.class));
        for (Secuencia s : filas) {
            if (s.Id != null) {
                salida.add(s.Id);
            }
        }
        return salida;
    }
}
