package cl.bcs.contabilidad.domain.port.out;

import cl.bcs.contabilidad.domain.model.sap.HomologaRespuestaSap;

import java.util.List;

/** Database side of the InterfazContable job (legacy jobs/InterfazContable.java:43-44, :116, :173-176, :228-245). */
public interface InterfazContableRepository {

    /** Every row of {@code ITZ.HomologaRespuestaSap}. */
    List<HomologaRespuestaSap> consultarHomologaRespuestaSap();

    /** {@code ITZ.ActualizarEstadoPreComprobante(idEmpresa, idUsuario, 'CON', 'N', id, ...)}, committed on its own. */
    void actualizarEstadoContabilizado(long idEmpresa, long idUsuario, long idPreComprobante);

    /** {@code Estado = 'E'} for every id, in one batch. */
    void marcarConError(List<Long> idsPreComprobante);
}
