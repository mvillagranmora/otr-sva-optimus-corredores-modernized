package cl.bcs.apv.infrastructure.persistence.contratos;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import cl.bcs.apv.domain.port.out.contratos.EventoAutorizacionRepositorio;
import cl.bcs.apv.infrastructure.jdbc.SqlNombrado;

/** JDBC adapter of {@link EventoAutorizacionRepositorio}. The statement is the legacy text verbatim (it was a literal of the hook). */
@Repository
public class EventoAutorizacionJdbc implements EventoAutorizacionRepositorio {

    private static final String SELECT_DETALLE_EVENTO = "SELECT EvtDet.\"IdEvento\" AS \"IdUnion\",EvtDet.\"NombreCampo\",EvtDet.\"TipoDato\",EvtDet.\"Valor\",EvtDet.\"Clasificacion\" FROM \"BSCL.Plataforma.BD::AUT.EventoDetalle\" AS EvtDet WHERE EvtDet.\"IdEvento\" = :idsEvento";

    private final SqlNombrado sql;

    public EventoAutorizacionJdbc(NamedParameterJdbcTemplate jdbc) {
        this.sql = new SqlNombrado(jdbc);
    }

    @Override
    public List<DetalleEventoAutorizacion> consultarDetalleEventos(List<Long> idsEvento) throws SQLException {
        Map<String, Object> params = new HashMap<>();
        params.put("idsEvento", idsEvento);
        return sql.consultar(SELECT_DETALLE_EVENTO, params, DetalleEventoAutorizacion.class);
    }
}
