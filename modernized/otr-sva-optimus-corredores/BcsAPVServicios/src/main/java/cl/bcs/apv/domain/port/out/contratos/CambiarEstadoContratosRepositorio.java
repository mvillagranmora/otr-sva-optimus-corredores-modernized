package cl.bcs.apv.domain.port.out.contratos;

import java.util.List;

import cl.bcs.apv.api.dto.cambiarestadocontratos.ContratosSAAPV;

/**
 * Persistence port of the use case {@code CambiarEstadoContratos} (legacy {@code CambiarEstadoContratosSAAPV}). The row classes are the legacy ones:
 * public fields mapped by column label (a column the query does not return keeps the legacy default of the field). Failures surface as the
 * {@code SQLException} the legacy threw.
 */
public interface CambiarEstadoContratosRepositorio {

    class DatosPersona {
        public String DocumentoIdentidad;
        public String Nombres;
    }

    /** Legacy {@code ConsultaContratoEstado}: contract state with its INGRESO traspaso and its PAC mandate. */
    class ContratoEstado {
        public Long IdContratoSAAPV;
        public String IdEstado;
        public Long IdTraspaso;
        public String FolioContratoSAAPV;
        public String EstadoPAC = "";
        public Long IdMandatoPAC = 0L;
    }

    class MandatoPACRelacionado {
        public Long IdMandatoPAC;
        public Long IdContratoSAAPV;
        public String FolioContratoSAAPV;
        public String Estado = "";
    }

    /** Legacy {@code ConsultarEstadoGlosa}: the stored values of a contract (also used to carry the old values of its traspaso). */
    class EstadoGlosa {
        public String IdEstadoEnvioNotificacion;
        public String IdEstadoSolicitud;
        public String IdEstado;
        public Long IdGlosa;
        public String DescripcionGlosa;
    }

    /** Legacy {@code CambiarEstadoTraspaso} (state change of a traspaso). */
    record TraspasoACambiar(Long idContratoSAAPV, Long idEmpresa, Long idTraspaso, String idEstado, String cierreSolicitud) {
    }

    List<DatosPersona> datosPersona(Long idPersona);

    /** Contracts with their INGRESO traspaso, for the folios and persons given (without the PAC mandate columns). */
    List<ContratoEstado> contratosConTraspaso(List<String> folios, List<Long> personas);

    /** As {@link #contratosConTraspaso} plus the PAC mandate (IdMandatoPAC, EstadoPAC). */
    List<ContratoEstado> contratosConTraspasoYMandato(List<String> folios, List<Long> personas);

    List<EstadoGlosa> estadoGlosa(String folio);

    /** Traspaso of the contract of the folio: IdContratoSAAPV, IdTraspaso, IdEstado and (as IdMandatoPAC) the stored glosa id. */
    List<ContratoEstado> traspasoDelContrato(String folio);

    List<MandatoPACRelacionado> mandatosRelacionados(List<String> folios);

    /** Batch UPDATE of IdEstado, IdEstadoEnvioNotificacion and IdEstadoSolicitud, one execution per contract. */
    void actualizarEstados(List<ContratosSAAPV> contratos);

    /** Batch UPDATE of IdGlosa and DescripcionGlosa, one execution per contract. */
    void actualizarGlosas(List<ContratosSAAPV> contratos);

    void cambiarEstadoTraspasos(List<TraspasoACambiar> traspasos);

    /** Batch UPDATE of the traspaso IdEstado (IdEstado, IdTraspaso of each row). */
    void actualizarEstadoTraspaso(List<ContratoEstado> filas);
}
