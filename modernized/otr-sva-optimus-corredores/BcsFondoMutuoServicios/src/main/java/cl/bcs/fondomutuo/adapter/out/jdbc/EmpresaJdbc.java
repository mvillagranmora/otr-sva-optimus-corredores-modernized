package cl.bcs.fondomutuo.adapter.out.jdbc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import cl.bcs.fondomutuo.kit.InformacionEmpresa;
import cl.bcs.fondomutuo.port.out.kit.EmpresaPort;

/** {@link EmpresaPort} over the legacy {@code ConsultarEmpresa_Select.sql} (verbatim). No row: {@code IllegalStateException("Empresa no existe")} (legacy {@code Exception}). */
@Component
public class EmpresaJdbc implements EmpresaPort {

    private static final String SQL = SqlRecursos.kit("ConsultarEmpresa_Select.sql");

    private final ConexionUnidadDeTrabajo conexion;

    public EmpresaJdbc(ConexionUnidadDeTrabajo conexion) {
        this.conexion = conexion;
    }

    @Override
    public InformacionEmpresa informacion(Long idEmpresa) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdEmpresa", idEmpresa);
        List<InformacionEmpresa> empresas = conexion.usar(con -> SqlNombrado.consultar(con, SQL, params, InformacionEmpresa.class));
        if (empresas.isEmpty()) {
            throw new IllegalStateException("Empresa no existe");
        }
        return empresas.get(0);
    }
}
