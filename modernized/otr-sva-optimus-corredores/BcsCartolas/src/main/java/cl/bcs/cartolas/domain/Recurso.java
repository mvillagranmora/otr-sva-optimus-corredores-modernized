package cl.bcs.cartolas.domain;

/**
 * Recurso de una plantilla: imagen, reporte Jasper, SP, texto, etc.
 * (tabla CAR.Recurso).
 */
public class Recurso {

    private long   id;
    private long   idPlantilla;
    private String editable = Opcion.NO;
    private String descripcion;
    private String grupo;
    private String nombre;
    private String tipo;
    private String valor;

    // -------------------------------------------------------------------------
    // Pure logic — getValorAsObject equivalente al legacy
    // -------------------------------------------------------------------------

    public static Object toObject(final String tipo, final String valor) {
        if (valor == null) {
            return null;
        }
        if (TipoRecurso.OPCION.equals(tipo)) {
            return Opcion.SI.equals(valor);
        }
        if (TipoRecurso.NUMERO.equals(tipo)) {
            return Integer.parseInt(valor);
        }
        // IMAGEN y otros: retornamos el valor raw (Base64 string)
        return valor;
    }

    public Object getValorAsObject() {
        return toObject(tipo, valor);
    }

    // -------------------------------------------------------------------------
    // Getters / Setters
    // -------------------------------------------------------------------------

    public long getId()                   { return id; }
    public void setId(final long v)        { this.id = v; }

    public long getIdPlantilla()           { return idPlantilla; }
    public void setIdPlantilla(final long v){ this.idPlantilla = v; }

    public String getEditable()            { return editable; }
    public void setEditable(final String v){ this.editable = v; }

    public String getDescripcion()         { return descripcion; }
    public void setDescripcion(final String v){ this.descripcion = v; }

    public String getGrupo()               { return grupo; }
    public void setGrupo(final String v)   { this.grupo = v; }

    public String getNombre()              { return nombre; }
    public void setNombre(final String v)  { this.nombre = v; }

    public String getTipo()                { return tipo; }
    public void setTipo(final String v)    { this.tipo = v; }

    public String getValor()               { return valor; }
    public void setValor(final String v)   { this.valor = v; }
}
