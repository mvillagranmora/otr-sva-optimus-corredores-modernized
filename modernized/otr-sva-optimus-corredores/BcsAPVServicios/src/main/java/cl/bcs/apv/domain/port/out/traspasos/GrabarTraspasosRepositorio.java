package cl.bcs.apv.domain.port.out.traspasos;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/** Persistence port of {@code GrabarTraspasos} (legacy {@code cl.bcs.APV.GrabarTraspasos}). */
public interface GrabarTraspasosRepositorio {

    /** State and identity document of the clients (one row per client with a default identity document). */
    List<EstadoCliente> estadoClientes(List<Long> idsCliente) throws SQLException;

    /** Portafolios APV of a client (view ConsultarPortafolio, purpose PROPOSITOAPV). */
    List<VistaInfoPort> portafoliosDelCliente(Long idCliente) throws SQLException;

    /** Legacy {@code ModificarPortafolioTraspaso.ModificarEstadoPortafolioTraspaso}: ACT sets it active, any other state sets INA (always AUT). */
    boolean modificarEstadoPortafolio(Long idPortafolio, String estado) throws SQLException;

    /** Batch insert of TRA.DetalleTraspaso: the per-row counts of the batch. */
    int[] insertarDetallesTraspaso(List<DetalleTraspaso> detalles) throws SQLException;

    /** Batch insert of SCS.PortafolioContratoSAAPV: the per-row counts of the batch. */
    int[] insertarPortafoliosContrato(List<PortafolioContratoSAAPV> filas) throws SQLException;

    /** Batch update of the state of TRA.Traspaso: the per-row counts of the batch. */
    int[] modificarEstadoTraspaso(List<TraspasoModificar> filas) throws SQLException;

    class EstadoCliente {
        public Long IdCliente;
        public String IdEstado;
        public String DocumentoIdentidad;
    }

    class VistaInfoPort {
        public Long IdPortafolio;
        public String CodigoPortafolio;
        public String Proposito;
        public Long IdCliente;
        public String IdEstado;
    }

    class DetalleTraspaso {
        public Long IdTraspaso;
        public Long IdDetalleTraspaso;
        public Long IdCliente;
        public Long IdPortafolio;
        public String FechaIngreso;
        public String FechaModificacion;
        public Long IdPlanTipoInversion;
        public Long IdProductoAgrupador;
        public Long IdRegimenTributario;
        public BigDecimal MontoPeso;
        public BigDecimal MontoUF;
        public BigDecimal Porcentaje;
        public String IdEstado;
        public String EsReapertura;
    }

    class PortafolioContratoSAAPV {
        public Long IdContratoSAAPV;
        public Long IdPortafolio;
        public String OrigenAhorro;
        public Double MontoPesos;
        public Double MontoUF;
        public Double PorcentajeRegimenImponible;
        public String Pago;
        public String PeriodoInicio;
        public String PeriodoTermino;
        public String IdEstado;
    }

    class TraspasoModificar {
        public Long IdTraspaso;
        public String IdEstado;
    }
}
