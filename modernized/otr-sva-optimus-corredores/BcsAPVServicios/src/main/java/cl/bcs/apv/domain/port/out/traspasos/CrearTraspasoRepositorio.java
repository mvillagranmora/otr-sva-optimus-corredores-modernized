package cl.bcs.apv.domain.port.out.traspasos;

import java.sql.SQLException;
import java.util.List;

/**
 * Persistence port of {@code CrearTraspaso} (legacy {@code cl.bcs.APV.CrearTraspaso}). The row classes carry the legacy POJO fields: the INSERT binds
 * {@code :FechaSuscripcion} / {@code :CierreSolicitud} that the legacy {@code Traspaso} does not declare, so the INSERT always fails (README APV-B-15).
 */
public interface CrearTraspasoRepositorio {

    /** Legacy {@code CrearTraspaso.Traspaso} as the batch element: the public fields are the parameters the legacy class could supply. */
    final class TraspasoFila {
        public Long IdTraspaso;
        public Long IdContratoSAAPV;
        public Long IdEmpresa;
        public String TipoMovimiento;
        public String RazonRetiro;
        public String FechaIngreso;
        public String FechaEgreso;
        public String FechaModificacion;
        public String CodigoTipoTraspaso;
        public String Descripcion;
        public Long IdCliente;
        public Long IdPersona;
        public String CodigoTipoAhorro;
        public String NombreEntidad;
        public String DocumentoEntidad;
        public String Canal;
        public String IdEstado;
        public String CierreSolicitud;
    }

    /** Legacy {@code CrearTraspaso.DetalleOrigenTraspaso} as the batch element. */
    final class DetalleOrigenFila {
        public Long IdDetalleOrigenTraspaso;
        public Long IdTraspaso;
        public String CodigoTipoAhorro;
        public Long IdEntidadRecaudadora;
        public String NombreEntidad;
        public String DocumentoEntidad;
        public String Fondo;
        public double MontoTraspaso;
        public double UFTraspaso;
        public double PorcentajeTraspaso;
        public String IdEstado;
        public Long IdInstrumento;
        public Long IdPortafolio;
        public String CodigoRegimenTributario;
        public Long IdInterno = 0L;
    }

    /** Legacy {@code SQL.batch(Traspaso_Insert)}: rows affected per element; fails with the SQL layer message when the SQL binds a field the row lacks. */
    int[] insertarTraspaso(List<TraspasoFila> filas) throws SQLException;

    /** Legacy {@code SQL.batch(DetalleOrigenTraspaso_Insert)}. */
    int[] insertarDetallesOrigen(List<DetalleOrigenFila> filas) throws SQLException;

    /** Legacy {@code con.rollback()} of the catch block. */
    void revertir() throws SQLException;
}
