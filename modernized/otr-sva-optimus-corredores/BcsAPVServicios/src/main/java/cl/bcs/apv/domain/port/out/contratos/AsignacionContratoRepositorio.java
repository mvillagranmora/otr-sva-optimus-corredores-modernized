package cl.bcs.apv.domain.port.out.contratos;

import java.sql.Timestamp;
import java.util.List;

/** Persistence of the contract assignment of a collection (legacy AsignarContratoARecaudacion SQL). */
public interface AsignacionContratoRepositorio {

    /** Row of {@code REC.AsignacionContratoRecaudacion}. */
    record AsignacionFila(Long IdRecaudacionCliente, Long IdContratoSAAPV, Timestamp FechaCreacion, Timestamp FechaModificacion) {
    }

    /** Parameters of the insert and the update (the component names are the SQL parameter names). */
    record AsignacionInsertar(Long IdRecaudacionCliente, Long IdContratoSAAPV, Timestamp FechaCreacion, Timestamp FechaModificacion) {
    }

    record AsignacionActualizar(Long IdRecaudacionCliente, Long IdContratoSAAPV, Timestamp FechaModificacion) {
    }

    /** Row of the detail query: whether the contract has a transfer detail. */
    record DetalleTraspasoFila(Long IdContratoSAAPV, Long IdTraspaso, String TieneDetalle) {
    }

    /** Contract whose tax regime is ratified; mutable and with the legacy defaults, as the legacy POJO (public fields = columns / SQL parameters). */
    class ContratoRatificar {
        public Long IdCliente = 0L;
        public Long IdContratoSAAPV = 0L;
        public String TipoContrato = "";
        public String Regimen = "";
        public Long IdRecaudacionCliente = 0L;
        public Long IdRecaudacion = 0L;
    }

    List<AsignacionFila> asignacionesDe(Long idRecaudacionCliente);

    List<DetalleTraspasoFila> detalleTraspaso(List<Long> idsContrato);

    List<ContratoRatificar> contratosARatificar(List<Long> idsContrato);

    void actualizarAsignaciones(List<AsignacionActualizar> filas);

    void insertarAsignaciones(List<AsignacionInsertar> filas);

    void actualizarRegimen(List<ContratoRatificar> contratos);
}
