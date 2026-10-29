package cl.bcs.normativa.esquema;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cl.bcs.normativa.data.Registro;

/**
 * Resumen de los resultados de la cuadratura.
 *
 * @author fpino@email.com
 *
 */
public class ResumenCuadratura implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * A&ntilde;o/mes del resumen.
     */
    private Date anoMes;

    /**
     * Cantidad de casos.
     */
    private long cantidadCasos;

    /**
     * Fecha/hora de la cuadratura.
     */
    private Date fechaCuadratura;

    /**
     * Glosa del resumen.
     */
    private String glosa;

    /**
     * Id del resumen.
     */
    private long id;

    /**
     * Lista de los id de los detalles.
     */
    private final List<Long> idDetalles = new ArrayList<>();

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
     * Lista de los registros que explican la diferencia.
     */
    private final List<Registro> registros = new ArrayList<>();

    /**
     * Tipo de clasificador.
     */
    private String tipoClasificador;

    /**
     * Tipo de cuadratura.
     */
    private String tipoCuadratura;

    /**
     * Tipo de diferencia.
     */
    private String tipoDiferencia;

    /**
     * Incrementa la cantidad de casos.
     */
    public void contarCasos() {
        cantidadCasos++;
    }

    /**
     * Obtiene el &ntilde;o/mes del resumen.
     *
     * @return El &ntilde;o/mes del resumen.
     */
    public Date getAnoMes() {
        return anoMes;
    }

    /**
     * Obtiene la cantidad de casos.
     *
     * @return La cantidad de casos.
     */
    public long getCantidadCasos() {
        return cantidadCasos;
    }

    /**
     * Obtiene la fecha de la cuadratura.
     *
     * @return La fecha de la cuadratura.
     */
    public Date getFechaCuadratura() {
        return fechaCuadratura;
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
     * Obtiene la lista de los id de los detalles.
     *
     * @return La lista de los id de los detalles.
     */
    public List<Long> getIdDetalles() {
        return idDetalles;
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
     * Obtiene la lista de los registros.
     *
     * @return La lista de los registros.
     */
    public List<Registro> getRegistros() {
        return registros;
    }

    /**
     * Obtiene el tipo de clasificador.
     *
     * @return El tipo de clasificador.
     */
    public String getTipoClasificador() {
        return tipoClasificador;
    }

    /**
     * Obtiene el tipo de cuadratura.
     *
     * @return El tipo de cuadratura.
     */
    public String getTipoCuadratura() {
        return tipoCuadratura;
    }

    /**
     * Obtiene el tipo de diferencia.
     *
     * @return El tipo de diferencia.
     */
    public String getTipoDiferencia() {
        return tipoDiferencia;
    }

    /**
     * Asigna el &ntilde;o/mes del resumen.
     *
     * @param anoMes
     *            El &ntilde;o/mes del resumen.
     */
    public void setAnoMes(final Date anoMes) {
        this.anoMes = anoMes;
    }

    /**
     * Asigna la cantidad de casos.
     *
     * @param cantidadCasos
     *            La cantidad de casos.
     */
    public void setCantidadCasos(final long cantidadCasos) {
        this.cantidadCasos = cantidadCasos;
    }

    /**
     * Asigna la fecha de la cuadratura.
     *
     * @param fechaCuadratura
     *            La fecha de la cuadratura.
     */
    public void setFechaCuadratura(final Date fechaCuadratura) {
        this.fechaCuadratura = fechaCuadratura;
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
     * Asigna el tipo de clasificador.
     *
     * @param tipoClasificador
     *            El tipo de clasificador.
     */
    public void setTipoClasificador(final String tipoClasificador) {
        this.tipoClasificador = tipoClasificador;
    }

    /**
     * Asigna el tipo de cuadratura.
     *
     * @param tipoCuadratura
     *            El tipo de cuadratura.
     */
    public void setTipoCuadratura(final String tipoCuadratura) {
        this.tipoCuadratura = tipoCuadratura;
    }

    /**
     * Asigna el tipo de diferencia.
     *
     * @param tipoDiferencia
     *            El tipo de diferencia.
     */
    public void setTipoDiferencia(final String tipoDiferencia) {
        this.tipoDiferencia = tipoDiferencia;
    }

    @Override
    public String toString() {
        return "ResumenCuadratura [anoMes=" + anoMes + ", cantidadCasos=" + cantidadCasos
                + ", fechaCuadratura=" + fechaCuadratura + ", glosa=" + glosa + ", id=" + id
                + ", idDocumento=" + idDocumento + ", idProceso=" + idProceso + ", idUsuario="
                + idUsuario + ", tipoClasificador=" + tipoClasificador + ", tipoCuadratura="
                + tipoCuadratura + ", tipoDiferencia=" + tipoDiferencia + "]";
    }

}
