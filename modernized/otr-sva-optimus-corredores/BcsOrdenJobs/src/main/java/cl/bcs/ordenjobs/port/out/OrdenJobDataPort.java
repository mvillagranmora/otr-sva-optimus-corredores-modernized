package cl.bcs.ordenjobs.port.out;

public interface OrdenJobDataPort {

    void ejecutarAsignacionAutomatica(long idEmpresa, long idUsuario);

    void cargarArchivoOrdenesTransacciones(long idEmpresa, long idUsuario);

    void cargarTransaccionesBolsaElectronica(long idEmpresa, long idUsuario);

    void controlMercadoCargaModificacionRuebas(long idEmpresa, long idUsuario);

    void controlMercadoFix(long idEmpresa, long idUsuario);

    void controlMercadoFixMME(long idEmpresa, long idUsuario);

    void controlMercadoMarketDataPalosBEC(long idEmpresa, long idUsuario);

    void controlMercadoMarketDataPalosIF(long idEmpresa, long idUsuario);

    void controlMercadoMarketDataPalosOP(long idEmpresa, long idUsuario);

    void controlMercadoMarketDataPalosRF(long idEmpresa, long idUsuario);

    void controlMercadoMarketDataPalosRV(long idEmpresa, long idUsuario);

    void controlMercadoMarketDataPalosSIM(long idEmpresa, long idUsuario);

    void controlMercadoModificacionPalosRuebas(long idEmpresa, long idUsuario);

    void controlMercadoPalosRVMME(long idEmpresa, long idUsuario);

    void controlMercadoTransaccionesBolsaElectronica(long idEmpresa, long idUsuario);

    void crearOrdenesPostreras(long idEmpresa, long idUsuario);

    void dropCopyRvMarcaNegociacion(long idEmpresa, long idUsuario);

    void generarInformeEstadisticas(long idEmpresa, long idUsuario);

    void marketDataOpInstrumentos(long idEmpresa, long idUsuario);

    void marketDataOpPrecioInstrumentos(long idEmpresa, long idUsuario);

    void marketDataOpPuntas(long idEmpresa, long idUsuario);

    void marketDataRvInstrumentos(long idEmpresa, long idUsuario);

    void marketDataRvInstrumentosMME(long idEmpresa, long idUsuario);

    void marketDataRvPuntas(long idEmpresa, long idUsuario);

    void marketDataRvPuntasMME(long idEmpresa, long idUsuario);

    void procesamientoTransaccionesOperadores(long idEmpresa, long idUsuario);

    void refacturarMovimiento(long idEmpresa, long idUsuario);
}
