package cl.bcs.cartolas.master.domain;

/**
 * Enumeración de los gráficos embebidos en la Cartola Master.
 *
 * <p>Equivalente modernizado de {@code config.GraficoMaster} legacy.</p>
 */
public enum GraficoMaster {

    GraficoConsolidadoPorProducto(
            "imgGraficoConsolidado",
            "GraficoConsolidadoPorProducto",
            "InfoCarteraMasterPag0"),

    GraficoActivoPorProducto(
            "imgGrafico1",
            "GraficoActivoPorProducto",
            "InfoCarteraMasterPag1"),

    GraficoFM(
            "FMG_imagen",
            null,
            null);

    private final String nombreParametroReporte;
    private final String nombreDataSource;
    private final String reportePadre;

    GraficoMaster(
            final String nombreParametroReporte,
            final String nombreDataSource,
            final String reportePadre) {
        this.nombreParametroReporte = nombreParametroReporte;
        this.nombreDataSource = nombreDataSource;
        this.reportePadre = reportePadre;
    }

    public String getNombreParametroReporte() { return nombreParametroReporte; }
    public String getNombreDataSource() { return nombreDataSource; }
    public String getReportePadre() { return reportePadre; }
}
