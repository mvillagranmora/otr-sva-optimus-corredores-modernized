package cl.bcs.ordenjobs.infrastructure.persistence;

import cl.bcs.ordenjobs.port.out.OrdenJobDataPort;
import org.springframework.stereotype.Repository;

@Repository
public class OrdenJobRepository implements OrdenJobDataPort {

    @Override
    public void ejecutarAsignacionAutomatica(long idEmpresa, long idUsuario) {
        throw new UnsupportedOperationException(
            "requires HANA: BSCL.Orden.BD::AsignacionAutomaticaJob");
    }

    @Override
    public void cargarArchivoOrdenesTransacciones(long idEmpresa, long idUsuario) {
        throw new UnsupportedOperationException(
            "requires HANA: BSCL.Orden.BD::CargarArchivoOrdenesTransacciones");
    }

    @Override
    public void cargarTransaccionesBolsaElectronica(long idEmpresa, long idUsuario) {
        throw new UnsupportedOperationException(
            "requires HANA: BSCL.Orden.BD::CargarTransaccionesBolsaElectronica");
    }

    @Override
    public void controlMercadoCargaModificacionRuebas(long idEmpresa, long idUsuario) {
        throw new UnsupportedOperationException(
            "requires HANA: BSCL.Orden.BD::ControlMercadoCargaModificacionRuebas");
    }

    @Override
    public void controlMercadoFix(long idEmpresa, long idUsuario) {
        throw new UnsupportedOperationException(
            "requires HANA: BSCL.Orden.BD::DMA.TBRESPUESTAS + BSCL.Orden.BD::DMA.TBRESPUESTAS_2");
    }

    @Override
    public void controlMercadoFixMME(long idEmpresa, long idUsuario) {
        throw new UnsupportedOperationException(
            "requires HANA: BSCL.Orden.BD::ControlMercadoFixMME");
    }

    @Override
    public void controlMercadoMarketDataPalosBEC(long idEmpresa, long idUsuario) {
        throw new UnsupportedOperationException(
            "requires HANA: BSCL.Orden.BD::ControlMercadoMarketDataPalosBEC");
    }

    @Override
    public void controlMercadoMarketDataPalosIF(long idEmpresa, long idUsuario) {
        throw new UnsupportedOperationException(
            "requires HANA: BSCL.Orden.BD::ControlMercadoMarketDataPalosIF");
    }

    @Override
    public void controlMercadoMarketDataPalosOP(long idEmpresa, long idUsuario) {
        throw new UnsupportedOperationException(
            "requires HANA: BSCL.Orden.BD::ControlMercadoMarketDataPalosOP");
    }

    @Override
    public void controlMercadoMarketDataPalosRF(long idEmpresa, long idUsuario) {
        throw new UnsupportedOperationException(
            "requires HANA: BSCL.Orden.BD::ControlMercadoMarketDataPalosRF");
    }

    @Override
    public void controlMercadoMarketDataPalosRV(long idEmpresa, long idUsuario) {
        throw new UnsupportedOperationException(
            "requires HANA: BSCL.Orden.BD::ControlMercadoMarketDataPalosRV");
    }

    @Override
    public void controlMercadoMarketDataPalosSIM(long idEmpresa, long idUsuario) {
        throw new UnsupportedOperationException(
            "requires HANA: BSCL.Orden.BD::ControlMercadoMarketDataPalosSIM");
    }

    @Override
    public void controlMercadoModificacionPalosRuebas(long idEmpresa, long idUsuario) {
        throw new UnsupportedOperationException(
            "requires HANA: BSCL.Orden.BD::ControlMercadoModificacionPalosRuebas");
    }

    @Override
    public void controlMercadoPalosRVMME(long idEmpresa, long idUsuario) {
        throw new UnsupportedOperationException(
            "requires HANA: BSCL.Orden.BD::ControlMercadoPalosRVMME");
    }

    @Override
    public void controlMercadoTransaccionesBolsaElectronica(long idEmpresa, long idUsuario) {
        throw new UnsupportedOperationException(
            "requires HANA: BSCL.Orden.BD::ControlMercadoTransaccionesBolsaElectronica ruebas.properties");
    }

    @Override
    public void crearOrdenesPostreras(long idEmpresa, long idUsuario) {
        throw new UnsupportedOperationException(
            "requires HANA: BSCL.Orden.BD::CrearOrdenesPostrerasJob");
    }

    @Override
    public void dropCopyRvMarcaNegociacion(long idEmpresa, long idUsuario) {
        throw new UnsupportedOperationException(
            "requires HANA: BSCL.Orden.BD::DropCopyRvMarcaNegociacion");
    }

    @Override
    public void generarInformeEstadisticas(long idEmpresa, long idUsuario) {
        throw new UnsupportedOperationException(
            "requires HANA: BSCL.Orden.BD::GenerarInformeEstadisticasJob");
    }

    @Override
    public void marketDataOpInstrumentos(long idEmpresa, long idUsuario) {
        throw new UnsupportedOperationException(
            "requires HANA: BSCL.Orden.BD::MarketDataOpInstrumentos");
    }

    @Override
    public void marketDataOpPrecioInstrumentos(long idEmpresa, long idUsuario) {
        throw new UnsupportedOperationException(
            "requires HANA: BSCL.Orden.BD::MarketDataOpPrecioInstrumentos");
    }

    @Override
    public void marketDataOpPuntas(long idEmpresa, long idUsuario) {
        throw new UnsupportedOperationException(
            "requires HANA: BSCL.Orden.BD::MarketDataOpPuntas");
    }

    @Override
    public void marketDataRvInstrumentos(long idEmpresa, long idUsuario) {
        throw new UnsupportedOperationException(
            "requires HANA: BSCL.Orden.BD::MarketDataRvInstrumentos");
    }

    @Override
    public void marketDataRvInstrumentosMME(long idEmpresa, long idUsuario) {
        throw new UnsupportedOperationException(
            "requires HANA: BSCL.Orden.BD::MarketDataRvInstrumentosMME");
    }

    @Override
    public void marketDataRvPuntas(long idEmpresa, long idUsuario) {
        throw new UnsupportedOperationException(
            "requires HANA: BSCL.Orden.BD::MarketDataRvPuntas");
    }

    @Override
    public void marketDataRvPuntasMME(long idEmpresa, long idUsuario) {
        throw new UnsupportedOperationException(
            "requires HANA: BSCL.Orden.BD::MarketDataRvPuntasMME");
    }

    @Override
    public void procesamientoTransaccionesOperadores(long idEmpresa, long idUsuario) {
        throw new UnsupportedOperationException(
            "requires HANA: ProcesamientoTransaccionesOperadores (ruebas.properties + BSCL.Orden.BD)");
    }

    @Override
    public void refacturarMovimiento(long idEmpresa, long idUsuario) {
        throw new UnsupportedOperationException(
            "requires HANA: BSCL.Orden.BD::RefacturarMovimientoJob");
    }
}
