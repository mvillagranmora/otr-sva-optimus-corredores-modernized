package cl.bcs.normativa.cartolas.master.config;

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

    private final String descripcion;
    private final String grupo;
    private final String nombreArchivo;
    private final String nombreParametroReporte;

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

    public String getDescripcion() {
        return descripcion;
    }

    public String getGrupo() {
        return grupo;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public String getNombreParametroReporte() {
        return nombreParametroReporte;
    }

}
