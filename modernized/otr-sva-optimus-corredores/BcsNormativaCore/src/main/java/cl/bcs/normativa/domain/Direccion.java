package cl.bcs.normativa.domain;

import java.io.Serializable;

/**
 * Direccion fisica.
 * Migrado desde legacy cl.bcs.normativa.esquema.Direccion.
 */
public class Direccion implements Serializable {

    private static final long serialVersionUID = 1L;

    private String calle;
    private String ciudad;
    private String comuna;
    private String departamento;
    private String numero;
    private String sector;

    public String getCalle() { return calle; }
    public String getCiudad() { return ciudad; }
    public String getComuna() { return comuna; }
    public String getDepartamento() { return departamento; }
    public String getNumero() { return numero; }
    public String getSector() { return sector; }

    public void setCalle(String calle) { this.calle = calle; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }
    public void setComuna(String comuna) { this.comuna = comuna; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }
    public void setNumero(String numero) { this.numero = numero; }
    public void setSector(String sector) { this.sector = sector; }

    @Override
    public String toString() {
        return "Direccion[" + calle + " " + numero + ", " + comuna + "]";
    }
}
