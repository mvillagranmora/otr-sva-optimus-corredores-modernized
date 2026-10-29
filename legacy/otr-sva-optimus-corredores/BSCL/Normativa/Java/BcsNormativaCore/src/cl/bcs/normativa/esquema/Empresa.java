package cl.bcs.normativa.esquema;

import java.io.Serializable;

/**
 * Datos relevantes de una empresa.
 *
 * @author fpino@email.com
 *
 */
public class Empresa implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * C&oacute;digo de la empresa.
     */
    private String codigo;

    /**
     * Indica si es corredora.
     */
    private boolean corredora;

    /**
     * Direcci&oacute;n principal.
     */
    private Direccion direccionPrincipal;

    /**
     * Email principal.
     */
    private String emailPrincipal;

    /**
     * Fono principal.
     */
    private String fonoPrincipal;

    /**
     * Id de la empresa.
     */
    private long id;

    /**
     * Nombre de la empresa.
     */
    private String nombre;

    /**
     * Raz&oacute;n social de la empresa.
     */
    private String razonSocial;

    /**
     * RUT de la empresa.
     */
    private String rut;

    /**
     * RUT del representante.
     */
    private String rutRepresentante;

    /**
     * Obtiene el c&oacute;digo de la empresa.
     *
     * @return El c&oacute;digo de la empresa.
     */
    public String getCodigo() {
        return codigo;
    }

    /**
     * Obtiene la direcci&oacute;n principal.
     *
     * @return La direcci&oacute;n principal.
     */
    public Direccion getDireccionPrincipal() {
        return direccionPrincipal;
    }

    /**
     * Obtiene el email principal.
     *
     * @return El email principal.
     */
    public String getEmailPrincipal() {
        return emailPrincipal;
    }

    /**
     * Obtiene el fono principal.
     *
     * @return El fono principal.
     */
    public String getFonoPrincipal() {
        return fonoPrincipal;
    }

    /**
     * Obtiene el id de la empresa.
     *
     * @return El id de la empresa.
     */
    public long getId() {
        return id;
    }

    /**
     * Obtiene el nombre de la empresa.
     *
     * @return El nombre de la empresa.
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Obtiene la raz&oacute;n social de la empresa.
     *
     * @return La raz&oacute;n social de la empresa.
     */
    public String getRazonSocial() {
        return razonSocial;
    }

    /**
     * Obtiene el RUT de la empresa.
     *
     * @return El RUT de la empresa.
     */
    public String getRut() {
        return rut;
    }

    /**
     * Obtiene el RUT del representante legal de la empresa.
     *
     * @return El RUT del representante legal de la empresa.
     */
    public String getRutRepresentante() {
        return rutRepresentante;
    }

    /**
     * Indica si es corredora.
     *
     * @return <code>true</code> si es corredora, <code>false</code> en caso
     *         contrario.
     */
    public boolean isCorredora() {
        return corredora;
    }

    /**
     * Asigna el c&oacute;digo de la empresa.
     *
     * @param codigo
     *            El c&oacute;digo de la empresa.
     */
    public void setCodigo(final String codigo) {
        this.codigo = codigo;
    }

    /**
     * Asigna si es corredora.
     *
     * @param corredora
     *            Si es corredora.
     */
    public void setCorredora(final boolean corredora) {
        this.corredora = corredora;
    }

    /**
     * Asigna la direcci&oacute;n principal.
     *
     * @param direccionPrincipal
     *            La direcci&oacute;n principal.
     */
    public void setDireccionPrincipal(final Direccion direccionPrincipal) {
        this.direccionPrincipal = direccionPrincipal;
    }

    /**
     * Asigna el email principal.
     *
     * @param emailPrincipal
     *            El email principal.
     */
    public void setEmailPrincipal(final String emailPrincipal) {
        this.emailPrincipal = emailPrincipal;
    }

    /**
     * Asigna el fono principal.
     *
     * @param fonoPrincipal
     *            El fono principal.
     */
    public void setFonoPrincipal(final String fonoPrincipal) {
        this.fonoPrincipal = fonoPrincipal;
    }

    /**
     * Asigna el id de la empresa.
     *
     * @param id
     *            El id de la empresa.
     */
    public void setId(final long id) {
        this.id = id;
    }

    /**
     * Asigna el nombre de la empresa.
     *
     * @param nombre
     *            El nombre de la empresa.
     */
    public void setNombre(final String nombre) {
        this.nombre = nombre;
    }

    /**
     * Asigna la raz&oacute;n social de la empresa.
     *
     * @param razonSocial
     *            La raz&oacute;n social de la empresa.
     */
    public void setRazonSocial(final String razonSocial) {
        this.razonSocial = razonSocial;
    }

    /**
     * Asigna el RUT de la empresa.
     *
     * @param rut
     *            El RUT de la empresa.
     */
    public void setRut(final String rut) {
        this.rut = rut;

    }

    /**
     * Asigna el RUT del representante legal de la empresa.
     *
     * @param rutRepresentante
     *            El RUT del representante legal de la empresa.
     */
    public void setRutRepresentante(final String rutRepresentante) {
        this.rutRepresentante = rutRepresentante;

    }

    @Override
    public String toString() {
        return "Empresa [codigo=" + codigo + ", corredora=" + corredora + ", direccionPrincipal="
                + direccionPrincipal + ", emailPrincipal=" + emailPrincipal + ", fonoPrincipal="
                + fonoPrincipal + ", id=" + id + ", nombre=" + nombre + ", razonSocial="
                + razonSocial + ", rut=" + rut + ", rutRepresentante=" + rutRepresentante + "]";
    }

}
