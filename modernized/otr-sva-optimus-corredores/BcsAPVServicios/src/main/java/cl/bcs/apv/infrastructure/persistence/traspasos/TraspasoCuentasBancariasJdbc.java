package cl.bcs.apv.infrastructure.persistence.traspasos;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import cl.bcs.apv.application.comun.Sneaky;
import cl.bcs.apv.domain.port.out.traspasos.TraspasoCuentasBancariasRepositorio;

/** JDBC adapter of {@link TraspasoCuentasBancariasRepositorio}; the SQL files are the legacy ones, byte for byte. */
@Repository
public class TraspasoCuentasBancariasJdbc implements TraspasoCuentasBancariasRepositorio {

    private static final String SQL_TRANSICION = SqlTraspasos.recurso("ConsultaOrigenDestinoTransicion");
    private static final String SQL_ORIGEN_RECAUDACION = SqlTraspasos.recurso("ConsultaOrigenRecaudacion");
    private static final String SQL_PERSONA_CLIENTE = SqlTraspasos.recurso("ConsultarPersonaClienteRecaudacion");

    private final SqlTraspasos sql;

    public TraspasoCuentasBancariasJdbc(NamedParameterJdbcTemplate jdbc) {
        this.sql = new SqlTraspasos(jdbc);
    }

    @Override
    public List<OrigenDestinoTransicion> transicion(Long idEmpresa, String operacion) {
        Map<String, Object> p = new HashMap<>();
        p.put("IdEmpresa", idEmpresa);
        p.put("Operacion", operacion);
        return Sneaky.sinChequear(() -> sql.consultar(SQL_TRANSICION, p, OrigenDestinoTransicion.class));
    }

    @Override
    public List<RecaudacionPersona> personaClienteRecaudacion(List<Long> idsRecaudacionCliente) {
        Map<String, Object> p = new HashMap<>();
        p.put("IdsRecaudacionCliente", idsRecaudacionCliente);
        return Sneaky.sinChequear(() -> sql.consultar(SQL_PERSONA_CLIENTE, p, RecaudacionPersona.class));
    }

    @Override
    public List<OrigenDestinoTransicion> origenRecaudacion(Long idEmpresa, String operacion, List<Long> idsRecaudacionCliente) {
        Map<String, Object> p = new HashMap<>();
        p.put("IdEmpresa", idEmpresa);
        p.put("Operacion", operacion);
        p.put("IdsRecaudacionCliente", idsRecaudacionCliente);
        return Sneaky.sinChequear(() -> sql.consultar(SQL_ORIGEN_RECAUDACION, p, OrigenDestinoTransicion.class));
    }
}
