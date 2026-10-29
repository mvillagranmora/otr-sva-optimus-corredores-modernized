package cl.bcs.cartolas.domain;

/**
 * Datos de cliente leídos desde la vista NOR.ConsultarCliente.
 */
public class Cliente {

    private long   id;
    private String nombre;
    private String documentoIdentidad;
    private String email;

    /**
     * Construye el nombre completo a partir de los campos del legacy.
     * Es pure-logic: sin efectos secundarios.
     */
    public static String buildNombre(final String razonSocial,
                                      final String nombres,
                                      final String paterno,
                                      final String materno) {
        if (razonSocial != null && !razonSocial.isBlank()) {
            return razonSocial;
        }
        final String n = nombres  != null ? nombres  : "";
        final String p = paterno  != null ? paterno  : "";
        final String m = materno  != null ? materno  : "";
        return (n + " " + p + " " + m).trim();
    }

    public long getId()                          { return id; }
    public void setId(final long v)               { this.id = v; }

    public String getNombre()                    { return nombre; }
    public void setNombre(final String v)        { this.nombre = v; }

    public String getDocumentoIdentidad()        { return documentoIdentidad; }
    public void setDocumentoIdentidad(final String v){ this.documentoIdentidad = v; }

    public String getEmail()                     { return email; }
    public void setEmail(final String v)         { this.email = v; }
}
