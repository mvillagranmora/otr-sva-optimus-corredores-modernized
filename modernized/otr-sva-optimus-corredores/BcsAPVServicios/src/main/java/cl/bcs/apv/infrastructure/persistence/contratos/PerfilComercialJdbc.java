package cl.bcs.apv.infrastructure.persistence.contratos;

import java.util.HashMap;
import java.util.List;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import cl.bcs.apv.application.comun.Sneaky;
import cl.bcs.apv.domain.port.out.contratos.PerfilComercialRepositorio;
import cl.bcs.apv.infrastructure.jdbc.SqlNombrado;

@Repository
public class PerfilComercialJdbc implements PerfilComercialRepositorio {

    private static final String SQL_INSERT = SqlNombrado.recurso("AsociarPerfilesAPV_InsertPerfilComercialAPV");
    private static final String SQL_VALIDACIONES = SqlNombrado.recurso("AsociarPerfilesAPV_SelectValidaciones");

    private final SqlNombrado sql;

    public PerfilComercialJdbc(NamedParameterJdbcTemplate jdbc) {
        this.sql = new SqlNombrado(jdbc);
    }

    @Override
    public List<ValidacionFila> validaciones(Long idEmpresa, String idEstado, List<String> categorias, List<String> abiertos, List<String> activos) {
        HashMap<String, Object> p = new HashMap<>();
        p.put("IdEmpresa", idEmpresa);
        p.put("IdEstado", idEstado);
        p.put("IdCategoriaInversionista", categorias);
        p.put("IdPerfilComercialAbierto", abiertos);
        p.put("IdPerfilComercialActivo", activos);
        return Sneaky.sinChequear(() -> sql.consultar(SQL_VALIDACIONES, p, ValidacionFila.class));
    }

    @Override
    public void insertar(List<PerfilInsertar> perfiles) {
        Sneaky.sinChequear(() -> sql.lote(SQL_INSERT, perfiles));
    }
}
