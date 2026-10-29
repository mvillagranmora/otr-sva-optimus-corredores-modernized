package cl.bcs.apv.domain.port.out.contratos;

import java.util.List;

import cl.bcs.apv.api.dto.modificarrelacionlaboral.RelacionLaboral;

/** Persistence port of {@code ModificarRelacionLaboral}: {@code AgregarRelacionLaboral_Insert.sql}. */
public interface RelacionLaboralRepositorio {

    /**
     * One insert per row (legacy {@code SQL.batch}). A null list is a {@code java.sql.SQLException} ("Null parameters"), a null row too
     * ("Map Object failed"): the legacy behaviours the goldens pin (APV-A-046).
     */
    void insertar(List<RelacionLaboral> relaciones);
}
