package cl.bcs.utils.equivalencia;

import java.math.BigDecimal;
import java.util.List;

/**
 * POJO shapes used (mirror of the legacy oracle's oracle/Pojos.java, same field names, types and order) as inputs for the reflection/JSON helpers. The modern test suite declares the SAME shapes (same
 * field names, types and declaration order) so both sides render identically.
 */
public final class Pojos {

    public static class Detalle {
        public String Codigo;
        public Long Valor;

        public Detalle() {
        }

        public Detalle(String c, Long v) {
            Codigo = c;
            Valor = v;
        }
    }

    public static class Muestra {
        public Long IdCliente;
        public String Nombre;
        public BigDecimal Monto;
        public Double Tasa;
        public Boolean Activo;
        public int Cantidad;
        public java.util.Date Fecha;
        public java.sql.Timestamp FechaHora;
        public java.sql.Date FechaSql;
        public List<String> Etiquetas;
        public String Nulo;
        public Detalle Detalle;
    }

    /** BcsPojo subclass: toString() is Utils.printJson(this, false). */
    public static class MuestraPojo extends cl.bcs.utils.BcsPojo {
        public Long Id;
        public String Texto;
        public java.sql.Timestamp FechaHora;
    }

    public static class Fila {
        public String Codigo;
        public Long Id;
        public String Valor;
        public static final String CONSTANTE = "K";

        public Fila() {
        }

        public Fila(String c, Long id, String v) {
            Codigo = c;
            Id = id;
            Valor = v;
        }
    }

    public static class OtraFila {
        public String Codigo;
    }

    public static class Origen {
        public Long Id;
        public Integer Num;
        public String Texto;
        public BigDecimal Monto;
        public java.sql.Timestamp FechaHora;
        public java.sql.Date Fecha;
        public java.sql.Time Hora;
        public List<Detalle> Detalles;
        public Double Tasa;
        public Boolean Activo;
        public int Primitivo;
        public String FechaTexto;
        public String FechaHoraTexto;
        public java.sql.Timestamp FechaHoraATexto;
        public java.sql.Date FechaATexto;
        public String SoloOrigen;
        public Long TipoDistinto;
    }

    public static class Destino {
        public Long Id;
        public Integer Num;
        public String Texto;
        public BigDecimal Monto;
        public java.sql.Timestamp FechaHora;
        public java.sql.Date Fecha;
        public java.sql.Time Hora;
        public List<Detalle> Detalles;
        public Double Tasa;
        public Boolean Activo;
        public int Primitivo;
        public java.sql.Date FechaTexto;
        public java.sql.Timestamp FechaHoraTexto;
        public String FechaHoraATexto;
        public String FechaATexto;
        public String SoloDestino = "intacto";
        public String TipoDistinto = "intacto";
    }

    private Pojos() {
    }
}
