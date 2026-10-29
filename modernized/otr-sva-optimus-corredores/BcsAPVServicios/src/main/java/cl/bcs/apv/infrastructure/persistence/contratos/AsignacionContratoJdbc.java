package cl.bcs.apv.infrastructure.persistence.contratos;

import java.util.HashMap;
import java.util.List;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import cl.bcs.apv.application.comun.Sneaky;
import cl.bcs.apv.domain.port.out.contratos.AsignacionContratoRepositorio;
import cl.bcs.apv.infrastructure.jdbc.SqlNombrado;

@Repository
public class AsignacionContratoJdbc implements AsignacionContratoRepositorio {

    private static final String SQL_SELECT = SqlNombrado.recurso("AsignacionContratoRecaudacion_Select");
    private static final String SQL_INSERT = SqlNombrado.recurso("AsignacionContratoRecaudacion_Insert");
    private static final String SQL_UPDATE = SqlNombrado.recurso("AsignacionContratoRecaudacion_Update");
    private static final String SQL_CONTRATOS = SqlNombrado.recurso("AsignacionContratoRecaudacion_SelectContratosAPVPorGrupoDeContratos");
    private static final String SQL_UPDATE_REGIMEN = SqlNombrado.recurso("AsignacionContratoRecaudacion_UpdateRecaudacionCliente");
    private static final String SQL_DETALLE = SqlNombrado.recurso("AsignacionContratoRecaudacion_SelectDetalleTraspaso");

    private final SqlNombrado sql;

    public AsignacionContratoJdbc(NamedParameterJdbcTemplate jdbc) {
        this.sql = new SqlNombrado(jdbc);
    }

    @Override
    public List<AsignacionFila> asignacionesDe(Long idRecaudacionCliente) {
        HashMap<String, Object> p = new HashMap<>(1);
        p.put("IdRecaudacionCliente", idRecaudacionCliente);
        return Sneaky.sinChequear(() -> sql.consultar(SQL_SELECT, p, AsignacionFila.class));
    }

    @Override
    public List<DetalleTraspasoFila> detalleTraspaso(List<Long> idsContrato) {
        HashMap<String, Object> p = new HashMap<>(1);
        p.put("IdContratoSAAPV", idsContrato);
        return Sneaky.sinChequear(() -> sql.consultar(SQL_DETALLE, p, DetalleTraspasoFila.class));
    }

    @Override
    public List<ContratoRatificar> contratosARatificar(List<Long> idsContrato) {
        HashMap<String, Object> p = new HashMap<>(1);
        p.put("IdContratoSAAPV", idsContrato);
        return Sneaky.sinChequear(() -> sql.consultar(SQL_CONTRATOS, p, ContratoRatificar.class));
    }

    @Override
    public void actualizarAsignaciones(List<AsignacionActualizar> filas) {
        Sneaky.sinChequear(() -> sql.lote(SQL_UPDATE, filas));
    }

    @Override
    public void insertarAsignaciones(List<AsignacionInsertar> filas) {
        Sneaky.sinChequear(() -> sql.lote(SQL_INSERT, filas));
    }

    @Override
    public void actualizarRegimen(List<ContratoRatificar> contratos) {
        Sneaky.sinChequear(() -> sql.lote(SQL_UPDATE_REGIMEN, contratos));
    }
}
