package cl.bcs.normativa.esquema;

/**
 * Direcci&oacute;n f&iacute;sica.
 *
 * @author fpino@email.com
 *
 */
public class Direccion {

    /**
     * Calle.
     */
    private String calle;

    /**
     * Ciudad.
     */
    private String ciudad;

    /**
     * Comuna.
     */
    private String comuna;

    /**
     * Departamento.
     */
    private String departamento;

    /**
     * N&uacute;mero.
     */
    private String numero;

    /**
     * Sector.
     */
    private String sector;

    /**
     * Obtiene la calle.
     *
     * @return La calle.
     */
    public String getCalle() {
        return calle;
    }

    /**
     * Obtiene la ciudad.
     *
     * @return La ciudad.
     */
    public String getCiudad() {
        return ciudad;
    }

    /**
     * Obtiene la comuna.
     *
     * @return La comuna.
     */
    public String getComuna() {
        return comuna;
    }

    /**
     * Obtiene el departamento.
     *
     * @return El departamento.
     */
    public String getDepartamento() {
        return departamento;
    }

    /**
     * Obtiene el n&uacute;mero.
     *
     * @return El n&uacute;mero.
     */
    public String getNumero() {
        return numero;
    }

    /**
     * Obtiene el sector.
     *
     * @return El sector.
     */
    public String getSector() {
        return sector;
    }

    /**
     * Asigna la calle.
     *
     * @param calle
     *            La calle.
     */
    public void setCalle(final String calle) {
        this.calle = calle;
    }

    /**
     * Asigna la ciudad.
     *
     * @param ciudad
     *            La ciudad.
     */
    public void setCiudad(final String ciudad) {
        this.ciudad = ciudad;
    }

    /**
     * Asigna la comuna.
     *
     * @param comuna
     *            La comuna.
     */
    public void setComuna(final String comuna) {
        this.comuna = comuna;

    }

    /**
     * Asigna el departamento.
     *
     * @param departamento
     *            El departamento.
     */
    public void setDepartamento(final String departamento) {
        this.departamento = departamento;

    }

    /**
     * Asigna el n&uacute;mero.
     *
     * @param numero
     *            El n&uacute;mero.
     */
    public void setNumero(final String numero) {
        this.numero = numero;

    }

    /**
     * Asigna el sector.
     *
     * @param sector
     *            El sector.
     */
    public void setSector(final String sector) {
        this.sector = sector;

    }

    @Override
    public String toString() {
        return "Direccion [calle=" + calle + ", ciudad=" + ciudad + ", comuna=" + comuna
                + ", departamento=" + departamento + ", numero=" + numero + ", sector=" + sector
                + "]";
    }

}
