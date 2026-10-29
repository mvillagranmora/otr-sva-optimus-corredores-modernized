package cl.bcs.cartolas.domain;

/**
 * Parámetro de una nómina (tabla CAR.Parametro).
 */
public class Parametro {

    private long   id;
    private long   idNomina;
    private String nombre;
    private String valor;

    public long getId()                    { return id; }
    public void setId(final long v)         { this.id = v; }

    public long getIdNomina()              { return idNomina; }
    public void setIdNomina(final long v)   { this.idNomina = v; }

    public String getNombre()              { return nombre; }
    public void setNombre(final String v)  { this.nombre = v; }

    public String getValor()               { return valor; }
    public void setValor(final String v)   { this.valor = v; }
}
