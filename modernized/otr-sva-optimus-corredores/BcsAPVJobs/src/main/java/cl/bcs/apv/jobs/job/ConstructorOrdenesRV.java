package cl.bcs.apv.jobs.job;

import java.math.BigDecimal;
import java.math.RoundingMode;

import cl.bcs.apv.jobs.job.FilasRV.Orden;
import cl.bcs.apv.jobs.job.contrato.AgregarLog;
import cl.bcs.apv.jobs.job.contrato.CalcularMontoYCobrosOrden;
import cl.bcs.apv.jobs.job.contrato.CrearOrdenes;

/**
 * Builders of the collaborator payloads of {@code IngresarOrdenRVBonificacionFiscal}, field by field as the legacy wrote them
 * (IngresarOrdenRVBonificacionFiscal.java:368-418 the amount request, :454-546 the order, :349-380 the cash-warning log).
 */
final class ConstructorOrdenesRV {

    private ConstructorOrdenesRV() {
    }

    /** Legacy :376-406 (the part common to both actions). */
    static CalcularMontoYCobrosOrden.Orden_Entrada calculo(Orden filaOrden, long contadorOrden, String fechaSistema, BigDecimal precioOrden) {
        CalcularMontoYCobrosOrden.Orden_Entrada c = new CalcularMontoYCobrosOrden.Orden_Entrada();
        c.PrecioOrden = precioOrden;
        c.IdInterno = contadorOrden;
        c.CantidadOrden = BigDecimal.ZERO;
        c.EsFungible = "N";
        c.IdBolsa = "BCS";
        c.IdCorredorCorresponsal = null;
        c.IdInstrumento = filaOrden.IdInstrumento;
        c.IdInstrumentoSubyacente = 0L;
        c.IdMonedaLiquidacionOrden = filaOrden.IdMonedaLiquidacionOrden;
        c.IdPortafolio = filaOrden.IdPortafolio; // BF portfolio
        c.IdTipoProducto = "INAC";
        c.IdTipoOperacion = filaOrden.IdTipoOperacion;
        c.IdTipoCanalIngreso = "AGE";
        c.FechaOperacion = fechaSistema;
        c.MontoOrden = BigDecimal.ZERO;
        c.TasaSimultanea = BigDecimal.ZERO;
        c.TasaRFIF = BigDecimal.ZERO;
        c.TasaVentaCorta = BigDecimal.ZERO;
        c.TipoCambio = BigDecimal.ZERO;
        c.ValorRescateOrden = BigDecimal.ZERO;
        c.PlazoSimultanea = 0L;
        c.PlazoVentaCorta = 0L;
        c.PlazoIF = 0L;
        c.EsAPlazo = "N";
        return c;
    }

    /** Legacy :455-466 + :472-531: the order sent to CrearOrdenes, before CalcularMontoYCobrosOrden replaces the quantity. */
    static CrearOrdenes.ObjOrdenesEntrada orden(Orden fila, long contadorOrden, BigDecimal montoCompra, BigDecimal cantidadVenta) {
        CrearOrdenes.ObjOrdenesEntrada o = new CrearOrdenes.ObjOrdenesEntrada();
        o.IdInterno = contadorOrden;
        o.FechaRecepcion = "0001-01-01 00:00:00";
        o.FechaVigencia = fila.FechaVigencia;
        o.FechaEmision = "0001-01-01";
        o.FechaOperacion = "0001-01-01";
        o.IdCondicionDeLiquidacion = fila.IdCondicionDeLiquidacion;
        o.IdTipoOperacion = fila.IdTipoOperacion;
        o.IdTipoVigencia = fila.IdTipoVigencia;
        o.Glosa = fila.Glosa;
        if ("COMPRA".equals(fila.AccionOrden)) {
            o.MontoNeto = montoCompra;
            o.EsPorMonto = "S";
            o.Cantidad = BigDecimal.ZERO;
        } else if ("VENTA".equals(fila.AccionOrden)) {
            o.EsPorMonto = "N";
            o.MontoNeto = BigDecimal.ZERO;
            o.Cantidad = cantidadVenta;
        }
        o.ValorRescate = fila.ValorRescate;
        o.IdBoveda = fila.IdBoveda;
        o.IdFormaCustodia = fila.IdFormaCustodia;
        o.IdPorcentajeVisibilidad = fila.IdPorcentajeVisibilidad;
        o.PrecioLimite = fila.PrecioLimite;
        o.EsFacturaInmediata = fila.EsFacturaInmediata;
        o.IdTipoPrecio = fila.IdTipoPrecio;
        o.IdPortafolio = fila.IdPortafolio;
        o.IdTipoCanalIngreso = fila.IdTipoCanalIngreso;
        o.IdTipoReajuste = fila.IdTipoReajuste;
        o.IdEmisor = fila.IdEmisor;
        o.IdBolsa = fila.IdBolsa;
        o.EspecificaBolsa = fila.EspecificaBolsa;
        o.IdMonedaLiquidacionOrden = fila.IdMonedaLiquidacionOrden;
        o.TipoCambio = fila.TipoCambio;
        o.IdInstrumento = fila.IdInstrumento;
        o.IdInstrumentoSubyacente = fila.IdInstrumentoSubyacente;
        o.IdCondicionDeLiquidacionPlazo = fila.IdCondicionDeLiquidacionPlazo;
        o.TasaVentaCorta = fila.TasaVentaCorta;
        o.PlazoVentaCorta = fila.PlazoVentaCorta;
        o.EsVentaCorta = fila.EsVentaCorta;
        o.TasaSimultanea = fila.TasaSimultanea;
        o.PlazoSimultanea = fila.PlazoSimultanea;
        o.PlazoIF = fila.PlazoIF;
        o.TasaRFIF = fila.TasaRFIF;
        o.EsOD = fila.EsOD;
        o.EsFungible = fila.EsFungible;
        o.IdTipoLibroOrden = "NORMAL";
        o.IdLibroOrdenes = null;
        o.IdCuentaCustodioPortafolio = fila.IdCuentaCustodioPortafolio;
        o.IdCorredorCorresponsal = fila.IdCorredorCorresponsal;
        o.IdPersonaAutorizadoDarOrdenes = fila.IdPersonaAutorizadoDarOrdenes;
        o.IdTipoCanalDistribucion = fila.IdTipoCanalDistribucion;
        o.EsOrdenEspecial = fila.EsOrdenEspecial;
        o.PagoCobroCuentaInversion = "S";
        o.IdPreferenciaPagoPortafolio = fila.IdPortafolio;
        o.BaseTasa = fila.BaseTasa;
        o.TipoInteres = fila.TipoInteres;
        o.IdMecanismoCaptacion = fila.IdMecanismoCaptacion;
        return o;
    }

    /** Legacy :423-469 (the rounding of the bonus is {@code RoundingMode.UP} at the scale of the base amount, RVB-002). */
    static BigDecimal porcentaje(BigDecimal base, long porcentaje) {
        return base.multiply(BigDecimal.valueOf(porcentaje)).divide(BigDecimal.valueOf(100), RoundingMode.UP);
    }

    /** Legacy :439-478: the log of the platform for an order that cannot be placed for lack of cash. */
    static AgregarLog.InAgregarLog logSaldoInsuficiente(Orden fila, Long idPrograma, Long idUsuario, Long idEmpresa, String fechaSistema, BigDecimal monto, String mensaje) {
        AgregarLog.InAgregarLog entrada = new AgregarLog.InAgregarLog();
        AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
        log.IdCabeceraLog = idPrograma;
        log.IdTipoLog = "INGRESO";
        log.IdModulo = "APV";
        log.IdUsuario = idUsuario;
        log.IdEmpresa = idEmpresa;
        log.FechaSistemaLog = fechaSistema;
        log.IdUsuarioAtendido = idUsuario;
        log.IdCliente = fila.IdCliente;
        log.EstadoCliente = "";
        log.NombreCliente = "";
        log.IdTipoDocumentoIdentidad = "";
        log.IdDocumentoIdentidad = "";
        log.IdPortafolio = fila.IdPortafolio;
        log.EstadoPortafolio = "";
        log.IdTipoRegistro = "ORDEN";
        log.EstadoRegistro = "";
        log.IdTipoOperacion = fila.IdTipoOperacion;
        log.Folio = 0L;
        log.Cantidad = fila.Cantidad;
        log.PrecioTasa = BigDecimal.ZERO;
        log.Monto = monto;
        log.IdMonedaOrigen = fila.IdMonedaOrden;
        log.IdMonedaLiquidacion = fila.IdMonedaLiquidacionOrden;
        log.TipoCambio = fila.TipoCambio.doubleValue() + ""; // NullPointerException when the order has no exchange rate (RVB-047)
        log.Nemo = "";
        log.IdProceso = 0L;
        log.EstadoProceso = "";
        log.NombreProceso = "";
        entrada.p_Log.add(log);
        AgregarLog.InAgregarLog.DetalleLog det = new AgregarLog.InAgregarLog.DetalleLog();
        det.IdCabeceraLog = idPrograma;
        det.IdTrabajo = 0L;
        det.SubRegistro = "ADVERTENCIA ";
        det.CampoRegistro = "SALDO CAJA";
        det.ValorAntiguo = "";
        det.ValorNuevo = mensaje;
        entrada.p_LogDetalle.add(det);
        return entrada;
    }
}
