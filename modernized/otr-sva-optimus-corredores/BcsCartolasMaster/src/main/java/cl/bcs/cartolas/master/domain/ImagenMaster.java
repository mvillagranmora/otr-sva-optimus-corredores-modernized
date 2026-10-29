package cl.bcs.cartolas.master.domain;

/**
 * Enumeración de imágenes utilizadas en la Cartola Master.
 *
 * <p>Equivalente modernizado de {@code config.ImagenMaster} legacy.</p>
 */
public enum ImagenMaster {

    Banner(
            "GENERAL",
            "BAN_imagen",
            "Banner de la portada",
            "banner.png"),

    Logo(
            "GENERAL",
            "STI_logotipo",
            "Logotipo del encabezado",
            "logo.gif");

    private final String grupo;
    private final String nombreParametroReporte;
    private final String descripcion;
    private final String nombreArchivo;

    ImagenMaster(
            final String grupo,
            final String nombreParametroReporte,
            final String descripcion,
            final String nombreArchivo) {
        this.grupo = grupo;
        this.nombreParametroReporte = nombreParametroReporte;
        this.descripcion = descripcion;
        this.nombreArchivo = nombreArchivo;
    }

    public String getGrupo() { return grupo; }
    public String getNombreParametroReporte() { return nombreParametroReporte; }
    public String getDescripcion() { return descripcion; }
    public String getNombreArchivo() { return nombreArchivo; }
}
