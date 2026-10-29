package cl.bcs.cartolas.master.domain;

/**
 * Enumeración de reportes JasperReports que componen una Cartola Master.
 *
 * <p>Cada constante lleva su configuración completa:
 * nombre de archivo jrxml, nombre en base de datos, datasource, parámetros de
 * inclusión y jerarquía de reportes padre/consolidado.</p>
 *
 * <p>Equivalente modernizado de {@code config.ReporteMaster} legacy.</p>
 */
public enum ReporteMaster {

    InfoCarteraMasterPag0(
            "InfoCarteraMasterPag0",
            "InfoCarteraMasterPag0Jasper",
            "InfoCarteraMaster",
            null, null,
            "showCarteraConsolidada",
            "N", null, "S", "S", "N"),

    InfoCarteraMasterPag1(
            "InfoCarteraMasterPag1",
            "InfoCarteraMasterPag1Jasper",
            "InfoCarteraMaster",
            null, null, null,
            "N", null, "N", "S", "N"),

    InfoCarteraMasterPag4(
            "InfoCarteraMasterPag4",
            "InfoCarteraMasterPag4Jasper",
            "InfoCarteraMaster",
            null, null, null,
            "N", null, "N", "N", "S"),

    InfoCarteraMasterPag2(
            "InfoCarteraMasterPag2",
            "InfoCarteraMasterPag2Jasper",
            "InfoCarteraMaster",
            null, null,
            "VC_incCircular1770",
            "N", null, null, null, null),

    // ---- Sub-reportes cabecera ----
    ResumenInversionConsolidado(
            "itemResumenInversionConsolidado",
            "ResumenInversionConsolidadoJasper",
            "ResumenInversionConsolidado",
            "resumenInversionConsolidado",
            "DataResumenInversionConsolidado",
            null,
            "S", "InfoCarteraMasterPag0", null, "S", null),

    ResumenFlujosProyectadosConsolidado(
            "itemResumenFlujosProyectadosConsolidado",
            "ResumenFlujosProyectadosConsolidadoJasper",
            "ResumenFlujosProyectadosConsolidado",
            "resumenFlujosProyectadosConsolidado",
            "DataResumenFlujosProyectadosConsolidado",
            null,
            "S", "InfoCarteraMasterPag0", null, "S", null),

    ResumenInversion(
            "itemResInversionClasico",
            "ResumenInversionJasper",
            "ResumenInversion",
            "resumenInversion",
            "DataResumenInversion",
            null,
            "S", "InfoCarteraMasterPag1", null, "S", null),

    ResumenMovimientos(
            "resumenMovimientos",
            "ResumenMovimientosJasper",
            "ResumenMovimientos",
            "resumenMovimientos",
            "DataResumenMovimientos",
            null,
            "S", "InfoCarteraMasterPag1", null, "S", null),

    FlujoPatrimonial(
            "flujoPatrimonial",
            "FlujoPatrimonialJasper",
            "FlujoPatrimonial",
            "flujoPatrimonial",
            "DataFlujoPatrimonial",
            null,
            "S", "InfoCarteraMasterPag1", null, "S", null),

    FlujoProyectado(
            "flujoProyectado",
            "FlujoProyectadoJasper",
            "FlujoProyectado",
            "flujoProyectado",
            "DataFlujoProyectado",
            null,
            "S", "InfoCarteraMasterPag1", null, "S", null),

    PatrimonioValorizado(
            "patrimonioValorizado_H",
            "PatrimonioValorizadoJasper",
            "PatrimonioValorizado",
            "patrimonioVal",
            "DataPatrimonioVal",
            null,
            "S", "InfoCarteraMasterPag1", null, "S", null),

    CodigoPDF417(
            "graficoPDF417Cartola",
            null,
            "CodigoPDF417",
            "codigoPDF417",
            "DataCodigoPDF417",
            null,
            "S", "InfoCarteraMasterPag1", null, "S", null),

    ResumenDerivados(
            "itemDerivados",
            "ResumenDerivadosJasper",
            "ResumenDerivados",
            "resumenDerivados",
            "DataResumenDerivados",
            null,
            "S", null, null, null, null),

    ResumenInversionFM(
            "itemResumenInversionFFMM",
            null,
            "ResumenInversionFM",
            "resumenInversionFFMM",
            "DataResumenInversionFFMM",
            null,
            "S", null, null, null, null),

    // ---- Sub-reportes detalle ----
    CarteraRV(
            "carteraRv", "CarteraRVJasper", "CarteraRV",
            "subGrilla", "DataGrilla",
            "PRV_incCartera",
            "S", "InfoCarteraMasterPag4", null, "N", null),

    CarteraRF(
            "carteraRF", "CarteraRFJasper", "CarteraRF",
            "subGrilla", "DataGrilla",
            "IRF_incCartera",
            "S", "InfoCarteraMasterPag4", null, "N", null),

    CarteraIF(
            "carteraIIF", "CarteraIFJasper", "CarteraIF",
            "subGrilla", "DataGrilla",
            "IIF_incCartera",
            "S", null, null, "N", null),

    CarteraSimultaneas(
            "carteraSimultanea", "CarteraSimultaneasJasper", "CarteraSimultaneas",
            "subGrilla", "DataGrilla",
            "SIM_incCartera",
            "S", null, null, "N", null),

    MovimientosTitulos(
            "MovTitulos", "MovimientosTitulosJasper", "MovimientosTitulos",
            "subGrilla", "DataGrilla",
            "MTI_incCartera",
            "S", null, null, "N", null),

    MovimientosCaja(
            "movimientosCaja", "MovimientosCajaJasper", "MovimientosCaja",
            "subGrilla", "DataGrilla",
            "MCJ_incCartera",
            "S", "InfoCarteraMasterPag4", null, "N", null),

    PortadaDerecha(
            "columnRight_PG1_V", "PortadaDerechaJasper", "PortadaDerecha",
            "rightColumn", "DataRightColumn",
            null,
            "S", null, null, null, null),

    PortadaIzquierda(
            "columnLeft_PG1_H", null, null,
            "leftColumn", "DataLeftColumn",
            null,
            "S", null, null, null, null),

    CarteraFM(
            "carteraFM", null, "CarteraFM",
            null, null,
            "FM_incCartera",
            "S", null, null, null, null),

    CarteraFU(
            "carteraFU", null, "CarteraFU",
            null, null,
            "FU_incCartera",
            "S", null, null, null, null),

    CarteraOP(
            "carteraOP", null, "CarteraOP",
            null, null,
            "OP_incCartera",
            "S", null, null, null, null),

    CarteraES(
            "carteraES", null, "CarteraES",
            null, null,
            "ES_incCartera",
            "S", null, null, null, null),

    CarteraForward(
            "carteraForward", null, "CarteraForward",
            null, null,
            "FWD_incCartera",
            "S", null, null, null, null),

    CarteraFuturos(
            "carteraFU", null, "CarteraFuturos",
            null, null,
            "PFU_incCartera",
            "S", null, null, null, null),

    CarteraFwd(
            "carteraFwd", null, "CarteraFwd",
            null, null,
            "FOW_incCartera",
            "S", null, null, null, null),

    CarteraPactos(
            "carteraPc", null, "CarteraPactos",
            null, null,
            "PAC_incCartera",
            "S", null, null, null, null),

    CarteraPrestamos(
            "carteraVC", null, "CarteraPrestamos",
            null, null,
            "PVC_incCartera",
            "S", null, null, null, null),

    InfoCarteraFM(
            "infoCarteraFFMM", null, "InfoCarteraFM",
            null, null,
            "FMG_incCartera",
            "S", null, null, null, null),

    ItemDerivados(
            "itemDerivados", null, "ItemDerivados",
            "resumenDerivados", "dataResumenDerivados",
            "PFU_showReport",
            "S", null, null, null, null),

    ItemDerivadosForward(
            "itemDerivadosFWD", null, "ItemDerivadosForward",
            "resumenForward", "dataResumenForward",
            null,
            "S", null, null, null, null),

    MovimientosTitulosForward(
            "MovTitulosFWD", null, "MovimientosTitulosForward",
            null, null,
            "MTF_incCartera",
            "S", null, null, null, null),

    ObservacionesAnotacionesCliente(
            "ObservacionesAnotacionesCliente", null, "ObservacionesAnotacionesCliente",
            null, null, null,
            "S", null, null, null, null),

    ProximosEventosCapital(
            "ProximosEventosCapital", null, "ProximosEventosCapital",
            null, null, null,
            "S", null, null, null, null);

    // ---- Campos ----
    private final String nombreArchivo;
    private final String nombreReporteBaseDato;
    private final String nombreDataSource;
    private final String nombreParametroReporte;
    private final String nombreParametroDataSource;
    private final String nombreParametroInclusion;
    private final String tipoSubReport;
    private final String reportePadre;
    private final String reporteConsolidado;
    private final String poseeSubReporteCabecera;
    private final String poseeSubReporteDetalle;

    ReporteMaster(
            final String nombreArchivo,
            final String nombreReporteBaseDato,
            final String nombreDataSource,
            final String nombreParametroReporte,
            final String nombreParametroDataSource,
            final String nombreParametroInclusion,
            final String tipoSubReport,
            final String reportePadre,
            final String reporteConsolidado,
            final String poseeSubReporteCabecera,
            final String poseeSubReporteDetalle) {
        this.nombreArchivo = nombreArchivo;
        this.nombreReporteBaseDato = nombreReporteBaseDato;
        this.nombreDataSource = nombreDataSource;
        this.nombreParametroReporte = nombreParametroReporte;
        this.nombreParametroDataSource = nombreParametroDataSource;
        this.nombreParametroInclusion = nombreParametroInclusion;
        this.tipoSubReport = tipoSubReport;
        this.reportePadre = reportePadre;
        this.reporteConsolidado = reporteConsolidado;
        this.poseeSubReporteCabecera = poseeSubReporteCabecera;
        this.poseeSubReporteDetalle = poseeSubReporteDetalle;
    }

    public String getNombreArchivo() { return nombreArchivo; }
    public String getNombreReporteBaseDato() { return nombreReporteBaseDato; }
    public String getNombreDataSource() { return nombreDataSource; }
    public String getNombreParametroReporte() { return nombreParametroReporte; }
    public String getNombreParametroDataSource() { return nombreParametroDataSource; }
    public String getNombreParametroInclusion() { return nombreParametroInclusion; }
    public String getTipoSubReport() { return tipoSubReport; }
    public String getReportePadre() { return reportePadre; }
    public String getReporteConsolidado() { return reporteConsolidado; }
    public String getPoseeSubReporteCabecera() { return poseeSubReporteCabecera; }
    public String getPoseeSubReporteDetalle() { return poseeSubReporteDetalle; }

    /** Retorna true si el reporte tiene un parámetro de inclusión configurado. */
    public boolean tieneParametroInclusion() {
        return nombreParametroInclusion != null;
    }
}
