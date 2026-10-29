package cl.bcs.apv.domain.port.out.traspasos;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/** Persistence port of {@code IngresarRecaudacionTraspaso} (legacy {@code cl.bcs.APV.IngresoRecaudacionTraspaso}). */
public interface IngresarRecaudacionTraspasoRepositorio {

    /** Rows of the view ConsultarTraspasosAPV for the ids (the legacy passes ONE id, possibly null). */
    List<Traspaso> traspasos(List<Long> idsTraspaso) throws SQLException;

    /** Detail rows of the traspasos (type I). */
    List<DetalleTraspaso> detalleTraspasos(List<Long> idsTraspaso) throws SQLException;

    /** Detail rows of ONE detalle of a reapertura (type R): the caller takes the first row. */
    List<DetalleTraspaso> detalleTraspasoReapertura(Long idTraspaso, Long idDetalleTraspaso) throws SQLException;

    /** {@code SELECT COUNT(1)} over the spot work table {@code SPT.CrearOperacionSpot_Proceso} (legacy {@code countQuery}). */
    Long contarOperacionesSpotEnProceso() throws SQLException;

    /** {@code DELETE FROM GBL.SaldoFlujoCaja_Proceso_Paso}. */
    void eliminarSaldoFlujoCajaProceso() throws SQLException;

    /** {@code TRUNCATE TABLE SPT.CrearOperacionSpot_Proceso}. */
    void eliminarOperacionSpotProceso() throws SQLException;

    /** The legacy {@code con.rollback()}: reverts EVERYTHING the transaction of the call wrote so far (also what the peers wrote). */
    void revertir() throws SQLException;

    class Traspaso {
        public Long IdTraspaso;
        public Long IdPersona;
        public Long IdEmpleador;
        public String FolioContratoSAAPV;
        public BigDecimal MontoPesoDetalle;
        public String Trabajador;
        public String IdEstado;
        public String NombreEntidad;
    }

    class DetalleTraspaso {
        public Long IdTraspaso;
        public Long IdDetalleTraspaso;
        public Long IdContratoSAAPV;
        public Long IdRegimenTributario;
        public String Regimen;
        public BigDecimal MontoPeso;
        public BigDecimal MontoUF;
        public BigDecimal Porcentaje;
        public Long IdCliente;
    }
}
