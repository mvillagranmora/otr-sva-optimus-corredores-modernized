package cl.bcs.cartolas.domain;

/**
 * Registro de datos en CSV por cartola y nombre de datasource (tabla CAR.Datos).
 */
public class Datos {

    private long   id;
    private long   idCartola;
    private String nombre;
    private String formato;
    private String valor;

    // -------------------------------------------------------------------------
    // Factory
    // -------------------------------------------------------------------------

    public static Datos of(final long idCartola,
                            final String nombre,
                            final String formato,
                            final String valor) {
        final Datos d = new Datos();
        d.idCartola = idCartola;
        d.nombre    = nombre;
        d.formato   = formato;
        d.valor     = valor;
        return d;
    }

    // -------------------------------------------------------------------------
    // Getters / Setters
    // -------------------------------------------------------------------------

    public long getId()                    { return id; }
    public void setId(final long v)         { this.id = v; }

    public long getIdCartola()             { return idCartola; }
    public void setIdCartola(final long v)  { this.idCartola = v; }

    public String getNombre()              { return nombre; }
    public void setNombre(final String v)  { this.nombre = v; }

    public String getFormato()             { return formato; }
    public void setFormato(final String v) { this.formato = v; }

    public String getValor()               { return valor; }
    public void setValor(final String v)   { this.valor = v; }

    @Override
    public String toString() {
        return "Datos[id=" + id + ", idCartola=" + idCartola
                + ", nombre=" + nombre + ", formato=" + formato + "]";
    }
}
