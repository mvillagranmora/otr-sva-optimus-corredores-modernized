package cl.bcs.normativa.esquema;

import java.io.Serializable;
import java.util.Date;

/**
 * Registro de resumen de carga.
 *
 * @author fpino@email.com
 *
 */
public class ResumenCarga implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Fecha/hora del resumen.
     */
    private Date fecha;

    /**
     * Glosa del resumen.
     */
    private String glosa;

    /**
     * Id del resumen.
     */
    private long id;

    /**
     * Id del documento relacionado.
     */
    private long idDocumento;

    /**
     * Id del proceso relacionado.
     */
    private long idProceso;

    /**
     * Id del uduario relacionado.
     */
    private long idUsuario;

    /**
     * Nombre del archivo de la carga (opcional).
     */
    private String nombreArchivo;

    /**
     * Cantidad total de registros cargados.
     */
    private long totalRegistrosCargados;

    /**
     * Cantidad total de registros err&oacute;neos.
     */
    private long totalRegistrosErroneos;

    /**
     * Cantidad total de registros leidos.
     */
    private long totalRegistrosLeidos;

    /**
     * Incrementa en uno la cuenta de registros err&oacute;neos.
     */
    public void contarRegistrosErroneos() {
        this.totalRegistrosErroneos++;
    }

    /**
     * Obtiene la fecha del resumen.
     *
     * @return La fecha del resumen.
     */
    public Date getFecha() {
        return fecha;
    }

    /**
     * Obtiene la glosa del resumen.
     *
     * @return La glosa del resumen.
     */
    public String getGlosa() {
        return glosa;
    }

    /**
     * Obtiene el id del resumen.
     *
     * @return El id del resumen.
     */
    public long getId() {
        return id;
    }

    /**
     * Obtiene el id del documento.
     *
     * @return El id del documento.
     */
    public long getIdDocumento() {
        return idDocumento;
    }

    /**
     * Obtiene el id del proceso.
     *
     * @return El id del proceso.
     */
    public long getIdProceso() {
        return idProceso;
    }

    /**
     * Obtiene el id del usuario.
     *
     * @return El id del usuario.
     */
    public long getIdUsuario() {
        return idUsuario;
    }

    /**
     * Obtiene el nombre del archivo.
     *
     * @return El nombre del archivo.
     */
    public String getNombreArchivo() {
        return nombreArchivo;
    }

    /**
     * Obtiene la cantidad total de registros cargados.
     *
     * @return La cantidad total de registros cargados.
     */
    public long getTotalRegistrosCargados() {
        return totalRegistrosCargados;
    }

    /**
     * Obtiene la cantidad total de registros err&oacute;neos.
     *
     * @return La cantidad total de registros err&oacute;neos.
     */
    public long getTotalRegistrosErroneos() {
        return totalRegistrosErroneos;
    }

    /**
     * Obtiene la cantidad total de registros leidos.
     *
     * @return La cantidad total de registros leidos.
     */
    public long getTotalRegistrosLeidos() {
        return totalRegistrosLeidos;
    }

    /**
     * Asigna la fecha del resumen.
     *
     * @param fecha
     *            La fecha del resumen.
     */
    public void setFecha(final Date fecha) {
        this.fecha = fecha;
    }

    /**
     * Asigna la glosa del resumen.
     *
     * @param glosa
     *            La glosa del resumen.
     */
    public void setGlosa(final String glosa) {
        this.glosa = glosa;
    }

    /**
     * Asigna el id del resumen.
     *
     * @param id
     *            El id del resumen.
     */
    public void setId(final long id) {
        this.id = id;
    }

    /**
     * Asigna el id del documento.
     *
     * @param idDocumento
     *            El id del documento.
     */
    public void setIdDocumento(final long idDocumento) {
        this.idDocumento = idDocumento;
    }

    /**
     * Asigna el id del proceso.
     *
     * @param idProceso
     *            El id del proceso.
     */
    public void setIdProceso(final long idProceso) {
        this.idProceso = idProceso;
    }

    /**
     * Asigna el id del usuario.
     *
     * @param idUsuario
     *            El id del usuario.
     */
    public void setIdUsuario(final long idUsuario) {
        this.idUsuario = idUsuario;
    }

    /**
     * Asigna el nombre del archivo.
     *
     * @param nombreArchivo
     *            El nombre del archivo.
     */
    public void setNombreArchivo(final String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    /**
     * Asigna la cantidad total de registros cargados.
     *
     * @param totalRegistrosCargados
     *            La cantidad total de registros cargados.
     */
    public void setTotalRegistrosCargados(final long totalRegistrosCargados) {
        this.totalRegistrosCargados = totalRegistrosCargados;
    }

    /**
     * Asigna la cantidad total de registros err&oacute;neos.
     *
     * @param totalRegistrosErroneos
     *            La cantidad total de registros err&oacute;neos.
     */
    public void setTotalRegistrosErroneos(final long totalRegistrosErroneos) {
        this.totalRegistrosErroneos = totalRegistrosErroneos;
    }

    /**
     * Asigna la cantidad total de registros leidos.
     *
     * @param totalRegistrosLeidos
     *            La cantidad total de registros leidos.
     */
    public void setTotalRegistrosLeidos(final long totalRegistrosLeidos) {
        this.totalRegistrosLeidos = totalRegistrosLeidos;
    }

    @Override
    public String toString() {
        return "ResumenCarga [fecha=" + fecha + ", glosa=" + glosa + ", id=" + id + ", idDocumento="
                + idDocumento + ", idProceso=" + idProceso + ", idUsuario=" + idUsuario
                + ", nombreArchivo=" + nombreArchivo + ", totalRegistrosCargados="
                + totalRegistrosCargados + ", totalRegistrosErroneos=" + totalRegistrosErroneos
                + ", totalRegistrosLeidos=" + totalRegistrosLeidos + "]";
    }

}
