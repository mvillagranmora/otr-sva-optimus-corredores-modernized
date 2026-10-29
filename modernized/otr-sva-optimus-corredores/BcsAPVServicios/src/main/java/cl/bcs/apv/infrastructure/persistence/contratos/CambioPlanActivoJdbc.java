package cl.bcs.apv.infrastructure.persistence.contratos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import cl.bcs.apv.application.comun.Sneaky;
import cl.bcs.apv.domain.port.out.contratos.CambioPlanActivoRepositorio;
import cl.bcs.apv.infrastructure.jdbc.SqlNombrado;

/**
 * JDBC adapter of {@link CambioPlanActivoRepositorio}. The statement of the plan update is the legacy HANA text verbatim (the H2 test dialect keys its
 * rewrite on it).
 */
@Repository
public class CambioPlanActivoJdbc implements CambioPlanActivoRepositorio {

    private static final String PARAMETROS_CONTRATO = SqlNombrado.recurso("CambiarAPlanActivoContratoSAAPV_SelectParametrosContrato");
    private static final String DISTRIBUCION_INVERSION = SqlNombrado.recurso("CambiarAPlanActivoContratoSAAPV_SelectDistribucionInversion");
    private static final String PORTAFOLIOS_MODIFICADOS = SqlNombrado.recurso("CambiarAPlanActivoContratoSAAPV_SelectPortafoliosModificados");
    private static final String DETALLE_ORIGEN_TRASPASO = SqlNombrado.recurso("CambiarAPlanActivoContratoSAAPV_SelectDetalleOrigenTraspaso");
    private static final String INSERTAR_GLOBAL_PORTAFOLIOS = SqlNombrado.recurso("CambiarAPlanActivoContratoSAAPV_InsertGlobalPortafolios");
    private static final String MODIFICAR_PLAN_PORTAFOLIOS = SqlNombrado.recurso("CambiarAPlanActivoContratoSAAPV_UpdatePlanPortafolios");

    private final NamedParameterJdbcTemplate jdbc;
    private final SqlNombrado sql;

    public CambioPlanActivoJdbc(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.sql = new SqlNombrado(jdbc);
    }

    private record Portafolio(Long IdPortafolio) {
    }

    private static Map<String, Object> parametros(Long idEmpresa, int idPlan, List<Long> idsContrato) {
        Map<String, Object> p = new HashMap<>();
        p.put("IdEmpresa", idEmpresa);
        p.put("IdPlan", idPlan);
        p.put("IdContratoSAAPV", idsContrato);
        return p;
    }

    @Override
    public List<DatosContratoSAAPV> consultarParametrosContrato(Long idEmpresa, int idPlan, List<Long> idsContrato) {
        return Sneaky.sinChequear(() -> sql.consultar(PARAMETROS_CONTRATO, parametros(idEmpresa, idPlan, idsContrato), DatosContratoSAAPV.class));
    }

    @Override
    public List<DatosDistribucionInversion> consultarDistribucionInversion(Long idEmpresa, int idPlan, List<Long> idsContrato) {
        return Sneaky.sinChequear(() -> sql.consultar(DISTRIBUCION_INVERSION, parametros(idEmpresa, idPlan, idsContrato), DatosDistribucionInversion.class));
    }

    @Override
    public List<DatosPortafoliosModificados> consultarPortafoliosModificados(Long idEmpresa, int idPlan, List<Long> idsContrato) {
        return Sneaky.sinChequear(() -> sql.consultar(PORTAFOLIOS_MODIFICADOS, parametros(idEmpresa, idPlan, idsContrato), DatosPortafoliosModificados.class));
    }

    @Override
    public List<DatosDetalleOrigenTraspaso> consultarDetalleOrigenTraspaso(Long idEmpresa, int idPlan, List<Long> idsContrato) {
        return Sneaky.sinChequear(() -> sql.consultar(DETALLE_ORIGEN_TRASPASO, parametros(idEmpresa, idPlan, idsContrato), DatosDetalleOrigenTraspaso.class));
    }

    private static List<Portafolio> filas(List<Long> ids) {
        List<Portafolio> filas = new ArrayList<>();
        for (Long id : ids) {
            filas.add(new Portafolio(id));
        }
        return filas;
    }

    @Override
    public void registrarPortafoliosCambioPlan(List<Long> idsPortafolio) {
        Sneaky.sinChequear(() -> LoteContratoModificacion.ejecutar(jdbc, INSERTAR_GLOBAL_PORTAFOLIOS, filas(idsPortafolio)));
    }

    @Override
    public int modificarPlanPortafolios(List<Long> idsPortafolio) {
        return Sneaky.sinChequear(() -> LoteContratoModificacion.ejecutar(jdbc, MODIFICAR_PLAN_PORTAFOLIOS, filas(idsPortafolio)).length);
    }
}
