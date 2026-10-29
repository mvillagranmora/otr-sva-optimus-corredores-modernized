package cl.bcs.normativa.esquema;

import java.io.Serializable;
import java.util.Date;

/**
 * Resumen de los resultados de la generaci&oacute;n.
 *
 * @author fpino@email.com
 *
 */
public class ResumenGeneracion implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Fecha de la generaci&oacute;n.
     */
    private Date fecha;

    /**
     * Glosa del resultado.
     */
    private String glosa;

    /**
     * Nombre del archivo de resultado.
     */
    private String nombreArchivo;

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
     * Cantidad total de registros generados.
     */
    private long totalRegistrosGenerados;
    
    /**
     * Numero del ultimo documento creado.
     */
    private long ultimoCorrelativo;

    /**
     * Obtiene la fecha de la generaci&oacute;n.
     *
     * @return La fecha de la generaci&oacute;n.
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
     * Obtiene el nombre del archivo de resultado.
     *
     * @return El nombre del archivo de resultado.
     */
    public String getNombreArchivo() {
        return nombreArchivo;
    }

    /**
     * Obtiene la cantidad total de registros generados.
     *
     * @return La cantidad total de registros generados.
     */
    public long getTotalRegistrosGenerados() {
        return totalRegistrosGenerados;
    }
    
    /**
     * Obtiene el numero del ultimo documento creado.
     *
     * @return Numero del ultimo documento creado.
     */
    public long getUltimoCorrelativo() {
        return ultimoCorrelativo;
    }

    /**
     * Asigna la fecha de la generaci&oacute;n.
     *
     * @param fecha
     *            La fecha de la generaci&oacute;n.
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
     * Asigna el nombre del archivo de resultado.
     * 
     * @param nombreArchivo
     *            El nombre del archivo de resultado.
     */
    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    /**
     * Asigna la cantidad total de registros generados.
     *
     * @param totalRegistrosGenerados
     *            La cantidad total de registros generados.
     */
    public void setTotalRegistrosGenerados(final long totalRegistrosGenerados) {
        this.totalRegistrosGenerados = totalRegistrosGenerados;
    }
    
    /**
     * Asigna el numero del ultimo documento creado.
     *
     * @return Numero del ultimo documento creado.
     */
    public void setUltimoCorrelativo(final long ultimoCorrelativo) {
    	this.ultimoCorrelativo = ultimoCorrelativo;
    }

    @Override
    public String toString() {
        return "ResumenGeneracion [fecha=" + fecha + ", glosa=" + glosa + ", nombreArchivo="
                + nombreArchivo + ", id=" + id + ", idDocumento=" + idDocumento + ", idProceso="
                + idProceso + ", idUsuario=" + idUsuario + ", totalRegistrosGenerados="
                + totalRegistrosGenerados + ", ultimoCorrelativo=" + ultimoCorrelativo + "]";
    }

}
