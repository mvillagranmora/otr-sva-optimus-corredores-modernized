package cl.bcs.apv.infrastructure.persistence.contratos;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import cl.bcs.apv.api.dto.modificarrelacionlaboral.RelacionLaboral;
import cl.bcs.apv.application.comun.Sneaky;
import cl.bcs.apv.domain.port.out.contratos.RelacionLaboralRepositorio;
import cl.bcs.apv.infrastructure.jdbc.SqlNombrado;

/** JDBC adapter of {@link RelacionLaboralRepositorio}. */
@Repository
public class RelacionLaboralJdbc implements RelacionLaboralRepositorio {

    private static final String INSERTAR = SqlNombrado.recurso("AgregarRelacionLaboral_Insert");

    private final NamedParameterJdbcTemplate jdbc;

    public RelacionLaboralJdbc(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Parameters of the statement (legacy names). */
    private record Fila(Long IdPersona, Long IdEmpleador, String CodigoTipoCargoEmpleado, BigDecimal IngresoMensual, String FechaIngresoCargo,
            String FechaRetiroCargo, String IdEstado) {
    }

    @Override
    public void insertar(List<RelacionLaboral> relaciones) {
        Sneaky.sinChequear(() -> {
            if (relaciones == null) {
                throw new SQLException("Null parameters. If parameters aren't need, pass an empty");
            }
            List<Fila> filas = new ArrayList<>();
            for (RelacionLaboral r : relaciones) {
                if (r == null) {
                    throw new SQLException("Map Object failed");
                }
                filas.add(new Fila(r.idPersona(), r.idEmpleador(), r.codigoTipoCargoEmpleado(), r.ingresoMensual(), r.fechaIngresoCargo(),
                        r.fechaRetiroCargo(), r.idEstado()));
            }
            LoteContratoModificacion.ejecutar(jdbc, INSERTAR, filas);
        });
    }
}
