package cl.bcs.apv.infrastructure.persistence.contratos;

import java.util.HashMap;
import java.util.List;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import cl.bcs.apv.application.comun.Sneaky;
import cl.bcs.apv.domain.port.out.contratos.BusquedaContratoRepositorio;
import cl.bcs.apv.infrastructure.jdbc.SqlNombrado;

@Repository
public class BusquedaContratoJdbc implements BusquedaContratoRepositorio {

    private static final String SQL_CONTRATOS = SqlNombrado.recurso("BusquedaContratoSAAPVRecaudacion_SelectContratos");
    private static final String SQL_RECAUDACIONES = SqlNombrado.recurso("BusquedaContratoSAAPVRecaudacion_SelectRecaudaciones");

    private final SqlNombrado sql;

    public BusquedaContratoJdbc(NamedParameterJdbcTemplate jdbc) {
        this.sql = new SqlNombrado(jdbc);
    }

    @Override
    public List<RecaudacionFila> recaudaciones(List<Long> idsRecaudacion) {
        HashMap<String, Object> p = new HashMap<>();
        p.put("IdsRecaudacion", idsRecaudacion);
        return Sneaky.sinChequear(() -> sql.consultar(SQL_RECAUDACIONES, p, RecaudacionFila.class));
    }

    @Override
    public List<ContratoFila> contratos(List<Long> idsCliente) {
        HashMap<String, Object> p = new HashMap<>();
        p.put("IdsCliente", idsCliente);
        return Sneaky.sinChequear(() -> sql.consultar(SQL_CONTRATOS, p, ContratoFila.class));
    }
}
