package cl.bcs.tesoreria.javacall.dap;

import java.sql.Timestamp;

import cl.bcs.utils.Fecha;

/** Rows of the platform file tables FTP.Cabecera / FTP.Detalle (legacy {@code FileManager.Cabecera / Detalle}) as the mass load reads and writes them. */
public final class ArchivoFtp {

    private ArchivoFtp() {
    }

    /** Header of a file: the field names are the column / parameter names of the SQL files. */
    public static class Cabecera {
        public Long IdCabecera = 0L;
        public String IdCodigoTipo = "";
        public String IdEstado = "";
        public Timestamp FechaCreacion = Fecha.toDateTime("0001-01-01");
        public Long IdUsuario = 0L;
        public String NombreArchivo = "";
    }

    /** One line of a file; {@code IdTipoEntidad / IdEntidad} point to the entity the line produced (a comprobante). */
    public static class Detalle {
        public Long IdDetalle = 0L;
        public Long IdCabecera = 0L;
        public String Linea = "";
        public String IdTipoEntidad = "";
        public Long IdEntidad = 0L;
    }
}
