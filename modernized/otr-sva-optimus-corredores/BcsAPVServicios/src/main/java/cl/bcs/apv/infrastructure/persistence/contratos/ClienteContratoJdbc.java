package cl.bcs.apv.infrastructure.persistence.contratos;

import java.util.HashMap;
import java.util.List;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import cl.bcs.apv.application.comun.Sneaky;
import cl.bcs.apv.domain.port.out.contratos.ClienteContratoRepositorio;
import cl.bcs.apv.infrastructure.jdbc.SqlNombrado;

@Repository
public class ClienteContratoJdbc implements ClienteContratoRepositorio {

    private static final String SQL_CLIENTE = SqlNombrado.recurso("ConsultarClienteContratoSAAPV_SelectClienteContrato");
    private static final String SQL_CONTRATO = SqlNombrado.recurso("ConsultarClienteContratoSAAPV_SelectContrato");
    private static final String SQL_MODAL = SqlNombrado.recurso("ConsultarClienteContratoSAAPV_SelectContratoModal");

    private final SqlNombrado sql;

    public ClienteContratoJdbc(NamedParameterJdbcTemplate jdbc) {
        this.sql = new SqlNombrado(jdbc);
    }

    private static HashMap<String, Object> base(Long idEmpresa, int idPlan, int filtroIdPersona, List<Long> idPersona) {
        HashMap<String, Object> p = new HashMap<>();
        p.put("IdEmpresa", idEmpresa);
        p.put("IdPlan", idPlan);
        p.put("FiltroIdPersona", filtroIdPersona);
        p.put("IdPersona", idPersona);
        return p;
    }

    @Override
    public List<ClienteFila> clientes(Long idEmpresa, int idPlan, int filtroIdPersona, List<Long> idPersona) {
        HashMap<String, Object> p = base(idEmpresa, idPlan, filtroIdPersona, idPersona);
        return Sneaky.sinChequear(() -> sql.consultar(SQL_CLIENTE, p, ClienteFila.class));
    }

    @Override
    public List<ContratoFila> contratos(Long idEmpresa, int idPlan, int filtroIdPersona, List<Long> idPersona, int filtroIdPortafolio, List<Long> idPortafolio) {
        HashMap<String, Object> p = base(idEmpresa, idPlan, filtroIdPersona, idPersona);
        p.put("FiltroIdPortafolio", filtroIdPortafolio);
        p.put("IdPortafolio", idPortafolio);
        return Sneaky.sinChequear(() -> sql.consultar(SQL_CONTRATO, p, ContratoFila.class));
    }

    @Override
    public List<ModalFila> modal(Long idEmpresa, int idPlan, int filtroIdPersona, List<Long> idPersona, int filtroIdPortafolio, List<Long> idPortafolio) {
        HashMap<String, Object> p = base(idEmpresa, idPlan, filtroIdPersona, idPersona);
        p.put("FiltroIdPortafolio", filtroIdPortafolio);
        p.put("IdPortafolio", idPortafolio);
        return Sneaky.sinChequear(() -> sql.consultar(SQL_MODAL, p, ModalFila.class));
    }
}
