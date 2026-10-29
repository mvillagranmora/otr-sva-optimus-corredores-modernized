package cl.bcs.normativa.domain;

import java.io.Serializable;

/**
 * Datos relevantes de una empresa.
 * Migrado desde legacy cl.bcs.normativa.esquema.Empresa.
 */
public class Empresa implements Serializable {

    private static final long serialVersionUID = 1L;

    private String codigo;
    private boolean corredora;
    private Direccion direccionPrincipal;
    private String emailPrincipal;
    private String fonoPrincipal;
    private long id;
    private String nombre;
    private String razonSocial;
    private String rut;
    private String rutRepresentante;

    public String getCodigo() { return codigo; }
    public Direccion getDireccionPrincipal() { return direccionPrincipal; }
    public String getEmailPrincipal() { return emailPrincipal; }
    public String getFonoPrincipal() { return fonoPrincipal; }
    public long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getRazonSocial() { return razonSocial; }
    public String getRut() { return rut; }
    public String getRutRepresentante() { return rutRepresentante; }
    public boolean isCorredora() { return corredora; }

    public void setCodigo(String codigo) { this.codigo = codigo; }
    public void setCorredora(boolean corredora) { this.corredora = corredora; }
    public void setDireccionPrincipal(Direccion direccionPrincipal) { this.direccionPrincipal = direccionPrincipal; }
    public void setEmailPrincipal(String emailPrincipal) { this.emailPrincipal = emailPrincipal; }
    public void setFonoPrincipal(String fonoPrincipal) { this.fonoPrincipal = fonoPrincipal; }
    public void setId(long id) { this.id = id; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }
    public void setRut(String rut) { this.rut = rut; }
    public void setRutRepresentante(String rutRepresentante) { this.rutRepresentante = rutRepresentante; }

    @Override
    public String toString() {
        return "Empresa[id=" + id + ", rut=" + rut + "]";
    }
}
