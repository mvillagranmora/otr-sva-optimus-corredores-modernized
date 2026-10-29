package cl.bcs.fondomutuo.adapter.out.jdbc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import cl.bcs.fondomutuo.perfil.PerfilFondoMutuoPort;

/** {@link PerfilFondoMutuoPort} over {@code Comunes_SelectPerfilFondoMutuo.sql} (verbatim legacy SQL, SELECT only, step 3A). */
@Component
public class PerfilFondoMutuoJdbc implements PerfilFondoMutuoPort {

    /** Legacy {@code pojo.PerfilFondoMutuo}. */
    public static class PerfilFondoMutuo {
        public String Perfil;
    }

    private static final String SQL_PERFIL_FONDO_MUTUO = SqlRecursos.modulo("Comunes_SelectPerfilFondoMutuo.sql");

    private final ConexionUnidadDeTrabajo conexion;

    public PerfilFondoMutuoJdbc(ConexionUnidadDeTrabajo conexion) {
        this.conexion = conexion;
    }

    @Override
    public List<String> perfiles(Long idUsuario) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdUsuario", idUsuario);
        List<PerfilFondoMutuo> filas = conexion.usar(con -> SqlNombrado.consultar(con, SQL_PERFIL_FONDO_MUTUO, params, PerfilFondoMutuo.class));
        List<String> salida = new ArrayList<>();
        for (PerfilFondoMutuo fila : filas) {
            salida.add(fila.Perfil);
        }
        return salida;
    }
}
