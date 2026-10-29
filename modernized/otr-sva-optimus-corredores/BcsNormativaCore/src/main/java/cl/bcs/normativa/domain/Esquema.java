package cl.bcs.normativa.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Representa el esquema de un documento normativo (incluye especificacion de campos).
 * Migrado desde legacy cl.bcs.normativa.esquema.Esquema.
 */
public class Esquema implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String FORMATO_ORIGEN    = "BSCL.Normativa.BD.Vistas::%s.%s.Origen";
    private static final String PREFIJO_DESTINO   = "BSCL.Normativa.BD::%s.Datos%s";
    private static final String FORMATO_VISTA_DESTINO = "BSCL.Normativa.BD.Vistas::%s.%s.Datos";

    private List<Campo> campos;
    private int codigoVersionSII;
    private Date fechaPresentacionLimite;
    private String idEntidadReguladora;
    private String modulo;
    private String nombre;
    private String objetivo;
    private String periodicidad;
    private Date periodoInicio;
    private RangoFechas rangoFechas;
    private String version;

    // ---- lazy-cached field lists ----
    private List<Campo> camposClaves;
    private List<Campo> camposComunes;
    private List<Campo> camposDatos;
    private List<Campo> camposExportables;
    private String nombreCampoFecha;
    private String nombreCampoRutCliente;

    /** Busca el campo en posicion de carga externa. */
    public Campo buscarCampoCargaExterna(int n) {
        for (Campo c : getCampos()) {
            if (c.getOrdenCargaExterna() == n) return c;
        }
        return null;
    }

    /** Busca campo por codigo SII (lanza EsquemaException si no existe). */
    public Campo buscarCampoPorCodigoSII(String codigoSII) {
        Objects.requireNonNull(codigoSII, "codigoSII no puede ser null");
        for (Campo c : getCampos()) {
            if (codigoSII.equals(c.getCodigoSII())) return c;
        }
        throw new EsquemaException("No se encontro campo con codigo SII: " + codigoSII);
    }

    /** Busca campo por nombre (lanza EsquemaException si no existe). */
    public Campo buscarCampoPorNombre(String nombreCampo) {
        Objects.requireNonNull(nombreCampo, "nombreCampo no puede ser null");
        for (Campo c : getCampos()) {
            if (nombreCampo.equals(c.getNombre())) return c;
        }
        throw new EsquemaException("No esta especificado el campo: " + nombreCampo);
    }

    public List<Campo> getCampos() {
        if (campos == null || campos.isEmpty()) {
            throw new EsquemaException("No hay campos definidos para " + nombre + "-" + version);
        }
        return campos;
    }

    public synchronized List<Campo> getCamposClaves() {
        if (camposClaves == null) {
            camposClaves = new ArrayList<>();
            for (Campo c : getCamposDatos()) {
                if (c.isClave()) camposClaves.add(c);
            }
            if (camposClaves.isEmpty()) {
                throw new EsquemaException("No hay campos clave definidos para " + nombre + "-" + version);
            }
        }
        return camposClaves;
    }

    public synchronized List<Campo> getCamposComunes() {
        if (camposComunes == null) {
            camposComunes = new ArrayList<>();
            for (Campo c : getCampos()) {
                if (c.isComun()) camposComunes.add(c);
            }
            if (camposComunes.isEmpty()) {
                throw new EsquemaException("No hay campos comunes definidos para " + nombre + "-" + version);
            }
        }
        return camposComunes;
    }

    public synchronized List<Campo> getCamposDatos() {
        if (camposDatos == null) {
            camposDatos = new ArrayList<>();
            for (Campo c : getCampos()) {
                if (!c.isComun()) camposDatos.add(c);
            }
            if (camposDatos.isEmpty()) {
                throw new EsquemaException("No hay campos de datos definidos para " + nombre + "-" + version);
            }
        }
        return camposDatos;
    }

    public synchronized List<Campo> getCamposExportables() {
        if (camposExportables == null) {
            camposExportables = new ArrayList<>();
            for (Campo c : getCampos()) {
                if (c.isExportable()) camposExportables.add(c);
            }
            if (camposExportables.isEmpty()) {
                throw new EsquemaException("No hay campos exportables definidos para " + nombre + "-" + version);
            }
        }
        return camposExportables;
    }

    public synchronized String getNombreCampoFecha() {
        if (nombreCampoFecha == null) {
            for (Campo c : getCampos()) {
                if ("DATE_FILTER".equals(c.getTipoDato())) {
                    nombreCampoFecha = c.getNombre();
                    break;
                }
            }
            if (nombreCampoFecha == null) {
                throw new EsquemaException("No esta definido campo Fecha para " + nombre + "-" + version);
            }
        }
        return nombreCampoFecha;
    }

    public synchronized String getNombreCampoRutCliente() {
        if (nombreCampoRutCliente == null) {
            for (Campo c : getCampos()) {
                if ("RUT_CLIENTE".equals(c.getTipoDato())) {
                    nombreCampoRutCliente = c.getNombre();
                    break;
                }
            }
            if (nombreCampoRutCliente == null) {
                throw new EsquemaException("No esta definido campo RUT Cliente para " + nombre + "-" + version);
            }
        }
        return nombreCampoRutCliente;
    }

    public List<String> getNombresCamposClave() {
        List<String> nombres = new ArrayList<>();
        for (Campo c : getCamposClaves()) nombres.add(c.getNombre());
        return nombres;
    }

    public String getNombreTablaDestino() {
        return String.format(PREFIJO_DESTINO, modulo, nombre);
    }

    public String getNombreTablaOrigen() {
        return String.format(FORMATO_ORIGEN, modulo, nombre);
    }

    public String getNombreVistaDestino() {
        return String.format(FORMATO_VISTA_DESTINO, modulo, nombre);
    }

    // ---- getters / setters ----
    public int getCodigoVersionSII() { return codigoVersionSII; }
    public Date getFechaPresentacionLimite() { return fechaPresentacionLimite; }
    public String getIdEntidadReguladora() { return idEntidadReguladora; }
    public String getModulo() { return modulo; }
    public String getNombre() { return nombre; }
    public String getObjetivo() { return objetivo; }
    public String getPeriodicidad() { return periodicidad; }
    public Date getPeriodoInicio() { return periodoInicio; }
    public RangoFechas getRangoFechas() { return rangoFechas; }
    public String getVersion() { return version; }

    public void setCampos(List<Campo> campos) { this.campos = campos; }
    public void setCodigoVersionSII(int codigoVersionSII) { this.codigoVersionSII = codigoVersionSII; }
    public void setFechaPresentacionLimite(Date fechaPresentacionLimite) { this.fechaPresentacionLimite = fechaPresentacionLimite; }
    public void setIdEntidadReguladora(String idEntidadReguladora) { this.idEntidadReguladora = idEntidadReguladora; }
    public void setModulo(String modulo) { this.modulo = modulo; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setObjetivo(String objetivo) { this.objetivo = objetivo; }
    public void setPeriodicidad(String periodicidad) { this.periodicidad = periodicidad; }
    public void setPeriodoInicio(Date periodoInicio) { this.periodoInicio = periodoInicio; }
    public void setRangoFechas(RangoFechas rangoFechas) { this.rangoFechas = rangoFechas; }
    public void setVersion(String version) { this.version = version; }

    @Override
    public String toString() {
        return "Esquema[" + nombre + "-" + version + "]";
    }
}
