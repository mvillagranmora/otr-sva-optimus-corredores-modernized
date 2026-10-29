package cl.bcs.apv.domain.port.out.traspasos;

import java.util.List;

/** Persistence port of {@code GrabarRecaudacionTraspaso} (legacy {@code cl.bcs.APV.GrabarRecaudacionTraspaso}): the two batch updates that close the traspaso. */
public interface GrabarRecaudacionTraspasoRepositorio {

    /** Legacy {@code CambiarEstadoTraspaso} (batch element of {@code CambiarEstadoTraspasoAPV.sql}). */
    final class EstadoTraspaso {
        public String IdEstado;
        public Long IdEmpresa;
        public Long IdTraspaso;
        public Long IdContratoSAAPV;
        public String CierreSolicitud;
    }

    /** Legacy {@code CambiarEstadoContrato} (batch element of {@code CambiarEstadoContratosSAAPV.sql}). */
    final class EstadoContrato {
        public String IdEstado;
        public String IdEstadoEnvioNotificacion;
        public String IdEstadoSolicitud;
        public String IdFolioContratoSAAPV;
        public Long IdPersona;
        public Long IdEmpleador;
    }

    /** Per-element counts of the legacy batch (an empty list gives an empty array). */
    int[] cambiarEstadoTraspasos(List<EstadoTraspaso> traspasos);

    int[] cambiarEstadoContratos(List<EstadoContrato> contratos);
}
