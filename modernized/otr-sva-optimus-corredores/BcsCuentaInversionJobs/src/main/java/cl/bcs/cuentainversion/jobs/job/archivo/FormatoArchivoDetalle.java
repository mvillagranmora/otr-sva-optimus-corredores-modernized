package cl.bcs.cuentainversion.jobs.job.archivo;

/**
 * One field of a file format (legacy {@code generarArchivo/FormatoArchivoDetalle.java}, row of
 * {@code ARC.ConsultarFormatoArchivoDetalle}). The wrapper types are kept: a null {@code Largo} or {@code Decimales} is unboxed by the
 * formatter exactly as the legacy did (CIJ-DEF-10).
 */
public final class FormatoArchivoDetalle {

    private Long idFormatoArchivoDetalle;
    /** Several formats can live in one file (1: header, 2: detail). */
    private Integer tipoRegistro;
    private String campo;
    /** CHAR, NUM, DEC or DATE. */
    private String tipoDato;
    private Integer largo;
    private Integer decimales;
    private String valorDefecto;
    private Integer orden;
    private String descripcion;
    /** For example the date format {@code yyyyMMdd}. */
    private String formato;

    public Long getIdFormatoArchivoDetalle() {
        return idFormatoArchivoDetalle;
    }

    public Integer getTipoRegistro() {
        return tipoRegistro;
    }

    public String getCampo() {
        return campo;
    }

    public String getTipoDato() {
        return tipoDato;
    }

    public Integer getLargo() {
        return largo;
    }

    public Integer getDecimales() {
        return decimales;
    }

    public String getValorDefecto() {
        return valorDefecto;
    }

    public Integer getOrden() {
        return orden;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getFormato() {
        return formato;
    }

    public void setIdFormatoArchivoDetalle(Long idFormatoArchivoDetalle) {
        this.idFormatoArchivoDetalle = idFormatoArchivoDetalle;
    }

    public void setTipoRegistro(Integer tipoRegistro) {
        this.tipoRegistro = tipoRegistro;
    }

    public void setCampo(String campo) {
        this.campo = campo;
    }

    public void setTipoDato(String tipoDato) {
        this.tipoDato = tipoDato;
    }

    public void setLargo(Integer largo) {
        this.largo = largo;
    }

    public void setDecimales(Integer decimales) {
        this.decimales = decimales;
    }

    public void setValorDefecto(String valorDefecto) {
        this.valorDefecto = valorDefecto;
    }

    public void setOrden(Integer orden) {
        this.orden = orden;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setFormato(String formato) {
        this.formato = formato;
    }
}
