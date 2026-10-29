package cl.bcs.apv.infrastructure.persistence.contratos;

import java.util.HashMap;
import java.util.List;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import cl.bcs.apv.application.comun.Sneaky;
import cl.bcs.apv.domain.port.out.contratos.PreferenciaPagoRepositorio;
import cl.bcs.apv.infrastructure.jdbc.SqlNombrado;

@Repository
public class PreferenciaPagoJdbc implements PreferenciaPagoRepositorio {

    private static final String SQL = SqlNombrado.recurso("ConsultarPreferenciasPagoPortafolioCliente");

    private final SqlNombrado sql;

    public PreferenciaPagoJdbc(NamedParameterJdbcTemplate jdbc) {
        this.sql = new SqlNombrado(jdbc);
    }

    @Override
    public List<PreferenciaFila> preferenciasDelCliente(Long idCliente) {
        HashMap<String, Object> p = new HashMap<>();
        p.put("IdCliente", idCliente);
        return Sneaky.sinChequear(() -> sql.consultar(SQL, p, PreferenciaFila.class));
    }
}
