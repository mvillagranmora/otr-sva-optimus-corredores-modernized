package cl.bcs.cartolas.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Plantilla de generación de cartola (tabla CAR.Plantilla).
 * Contiene el reporte Jasper compilado (Base64) y los recursos asociados.
 */
public class Plantilla {

    private long       id;
    private long       idEmpresa;
    private String     nombre;
    private LocalDate  vigenteDesde;
    private boolean    vigente;
    private List<Recurso> recursos;

    // -------------------------------------------------------------------------
    // Pure logic
    // -------------------------------------------------------------------------

    /**
     * Busca un recurso por nombre y tipo.
     * Lanza {@link IllegalArgumentException} si no existe.
     */
    public Recurso buscarRecurso(final String nombre, final String tipo) {
        Objects.requireNonNull(nombre, "nombre es requerido");
        Objects.requireNonNull(tipo,   "tipo es requerido");
        if (recursos != null) {
            for (final Recurso r : recursos) {
                if (tipo.equals(r.getTipo()) && nombre.equals(r.getNombre())) {
                    return r;
                }
            }
        }
        throw new IllegalArgumentException(
                String.format("No existe recurso: tipo=%s, nombre=%s", tipo, nombre));
    }

    // -------------------------------------------------------------------------
    // Getters / Setters
    // -------------------------------------------------------------------------

    public long getId()                       { return id; }
    public void setId(final long v)            { this.id = v; }

    public long getIdEmpresa()                { return idEmpresa; }
    public void setIdEmpresa(final long v)     { this.idEmpresa = v; }

    public String getNombre()                 { return nombre; }
    public void setNombre(final String v)     { this.nombre = v; }

    public LocalDate getVigenteDesde()        { return vigenteDesde; }
    public void setVigenteDesde(final LocalDate v){ this.vigenteDesde = v; }

    public boolean isVigente()                { return vigente; }
    public void setVigente(final boolean v)   { this.vigente = v; }

    public List<Recurso> getRecursos()        { return recursos; }
    public void setRecursos(final List<Recurso> v){ this.recursos = v; }

    @Override
    public String toString() {
        return "Plantilla[id=" + id + ", nombre=" + nombre
                + ", vigenteDesde=" + vigenteDesde + "]";
    }
}
