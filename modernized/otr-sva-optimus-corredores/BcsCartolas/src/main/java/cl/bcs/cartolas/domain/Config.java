package cl.bcs.cartolas.domain;

/**
 * Parámetro de configuración por empresa (tabla CAR.Config).
 */
public class Config {

    private long   id;
    private long   idEmpresa;
    private String descripcion;
    private String tipo;
    private String nombre;
    private String valor;

    public long getId()                   { return id; }
    public void setId(final long v)        { this.id = v; }

    public long getIdEmpresa()            { return idEmpresa; }
    public void setIdEmpresa(final long v) { this.idEmpresa = v; }

    public String getDescripcion()        { return descripcion; }
    public void setDescripcion(final String v){ this.descripcion = v; }

    public String getTipo()               { return tipo; }
    public void setTipo(final String v)   { this.tipo = v; }

    public String getNombre()             { return nombre; }
    public void setNombre(final String v) { this.nombre = v; }

    public String getValor()              { return valor; }
    public void setValor(final String v)  { this.valor = v; }
}
