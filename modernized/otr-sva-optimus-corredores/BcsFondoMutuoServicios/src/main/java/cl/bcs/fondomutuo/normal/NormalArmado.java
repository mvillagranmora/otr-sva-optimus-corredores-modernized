package cl.bcs.fondomutuo.normal;

import java.math.BigDecimal;
import java.math.MathContext;

import cl.bcs.fondomutuo.cierre.FechaLegacy;
import cl.bcs.fondomutuo.cierre.FormatoMoneda;
import cl.bcs.fondomutuo.cierre.SerieFondoSolicitudes;
import cl.bcs.fondomutuo.cierre.contrato.ParidadContrato.CalculoSalida;
import cl.bcs.fondomutuo.comisiones.CalcularComisiones.ResultadoCalcularComision;
import cl.bcs.fondomutuo.comisionnn.SolicitudAporteRescate;
import cl.bcs.fondomutuo.cuotas.CalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.fechas.CalcularFechaSolicitudAporteRescate.ResultadoFecha;
import cl.bcs.fondomutuo.normal.NormalContrato.InSolicitud;
import cl.bcs.fondomutuo.normal.NormalDatos.RegimenSerieDeFondo;
import cl.bcs.fondomutuo.requisitos.RequisitosDatos.ClientePortafolioSolicitudes;

/** Builds the row of {@code FMM.SolicitudAporteRescate} of one request (legacy :742-917): amounts, commission, dates, taxation. */
final class NormalArmado {

    private final FormatoMoneda formato;

    NormalArmado(FormatoMoneda formato) {
        this.formato = formato;
    }

    static String tipoMovimiento(InSolicitud s) {
        if (NormalConstantes.TIPO_SOLICITUD_APORTE.equalsIgnoreCase(s.TipoSolicitud)) {
            return NormalConstantes.SI.equalsIgnoreCase(s.EsTraspaso) ? NormalConstantes.APORTE_TRASPASO : NormalConstantes.APORTE_FONDO_MUTUO;
        }
        if (NormalConstantes.TIPO_SOLICITUD_RESCATE.equalsIgnoreCase(s.TipoSolicitud)) {
            return NormalConstantes.SI.equalsIgnoreCase(s.EsTraspaso) || NormalConstantes.RAZON_RETIRO_TRASPASO.equalsIgnoreCase(s.EsTraspaso)
                    ? NormalConstantes.RESCATE_TRASPASO : NormalConstantes.RESCATE_FONDO_MUTUO;
        }
        return "";
    }

    SolicitudAporteRescate solicitud(NormalContexto c, InSolicitud solicitud, int i, String tm, String procedenciaCanal) throws Exception {
        ClientePortafolioSolicitudes cliente = c.hclientes.get(solicitud.IdPortafolio);
        SerieFondoSolicitudes serie = c.hseries.get(solicitud.IdSerieFondo);
        CalcularMontoNumeroCuota.Resultado vc = c.valoresCuota.get(solicitud.IdInterno);
        CalculoSalida montoMonedaPago = c.hashMontoMonedaPago.get(solicitud.IdInterno);
        RegimenSerieDeFondo r = c.hseriesRegimen.get(solicitud.IdSerieFondo + "_" + solicitud.IdRegimenTributario);
        ResultadoFecha fecha = c.hfechas.get(solicitud.IdInterno);
        ResultadoCalcularComision comision = c.hcomisiones.get(solicitud.IdInterno);
        SolicitudAporteRescate sol = new SolicitudAporteRescate();
        encabezado(c, solicitud, i, tm, procedenciaCanal, cliente, serie, r, sol);
        if (vc != null && vc.ValorCuota != null) {
            montos(solicitud, vc, comision, sol);
        }
        if (montoMonedaPago != null) {
            sol.MontoMonedaPago = formato.formatear(montoMonedaPago.MontoDestino, FormatoMoneda.MONTO, sol.IdMonedaPago);
        }
        if (fecha != null) {
            sol.FechaAceptacion = NormalCalculos.sql(fecha.FechaAceptacion);
            sol.FechaValorCuota = solicitud.FechaValorCuota != null ? solicitud.FechaValorCuota : NormalCalculos.sql(fecha.FechaValorCuota);
            sol.FechaCertificacion = NormalCalculos.sql(fecha.FechaCertificacion);
            sol.FechaLiquidacion = NormalCalculos.sql(fecha.FechaLiquidacion);
        }
        if (r != null) {
            sol.Tributacion = tributacion(r.GrupoRegimen);
        }
        return sol;
    }

    private static String tributacion(String grupoRegimen) {
        switch (grupoRegimen) {
        case "APV":
            return NormalConstantes.TRIBUTACION_APV;
        case "ART108":
            return NormalConstantes.TRIBUTACION_ART108;
        case "ART107":
            return NormalConstantes.TRIBUTACION_ART107;
        case "57BIS":
            return NormalConstantes.TRIBUTACION_57BIS;
        default:
            return NormalConstantes.TRIBUTACION_DEFECTO;
        }
    }

    /** The commission and the amounts of the quota calculation (legacy :838-889); the trimestral commission is taken out of the amount of a rescue. */
    private static void montos(InSolicitud solicitud, CalcularMontoNumeroCuota.Resultado vc, ResultadoCalcularComision comision, SolicitudAporteRescate sol) {
        sol.Monto = vc.Monto;
        sol.NumeroCuotas = vc.NumeroCuotas;
        sol.ValorCuota = vc.ValorCuota;
        sol.CostoUF = vc.CostoEnFactorAjuste;
        sol.CostoMonedaLocal = vc.MontoDestino;
        sol.ValorUFFechaInversion = vc.ValorFactorAjuste;
        sol.ParidadFechaInversion = vc.ValorParidad;
        sol.IdMonedaSistema = vc.IdMonedaSistema;
        sol.MontoMonedaSistema = vc.MontoMonedaSistema;
        BigDecimal comisionPER = BigDecimal.ZERO;
        if (comision == null || (comision.TipoComision != null && comision.TipoComision.equalsIgnoreCase(NormalConstantes.TIPO_COMISION_TRIMESTRAL)
                && NormalConstantes.TIPO_SOLICITUD_APORTE.equals(sol.TipoSolicitud))) {
            sol.MontoComision = BigDecimal.ZERO;
            sol.MontoComisionLocal = BigDecimal.ZERO;
            sol.MontoComisionMonedaSistema = BigDecimal.ZERO;
            sol.ValorPivoteLocalConversionLocal = BigDecimal.ZERO;
            sol.ValorPivoteSistemaConversionSistema = BigDecimal.ZERO;
        } else if (comision.TipoComision != null && comision.TipoComision.equalsIgnoreCase(NormalConstantes.TIPO_COMISION_TRIMESTRAL)) {
            sol.MontoComisionLocal = comision.ComisionLocal;
            sol.MontoComisionMonedaSistema = comision.ComisionMonedaSistema;
            sol.ValorPivoteLocalConversionLocal = comision.ValorPivoteLocalConversionLocal;
            sol.ValorPivoteSistemaConversionSistema = comision.ValorPivoteSistemaConversionSistema;
            sol.Monto = vc.Monto.subtract(comision.Comision);
            sol.NumeroCuotas = vc.NumeroCuotas.subtract(comision.CantidadComision);
            sol.MontoInversion = sol.Monto;
            sol.NumeroCuotasInversion = sol.NumeroCuotas;
            sol.CostoMonedaLocal = NormalConstantes.TIPO_COMISION_TRIMESTRAL.equals(comision.TipoComision)
                    ? sol.CostoMonedaLocal.subtract(comision.Comision, MathContext.DECIMAL128) : sol.CostoMonedaLocal;
            comisionPER = comision.Comision;
        } else {
            sol.MontoComision = comision.Comision;
            sol.MontoComisionLocal = comision.ComisionLocal;
            sol.MontoComisionMonedaSistema = comision.ComisionMonedaSistema;
            sol.ValorPivoteLocalConversionLocal = comision.ValorPivoteLocalConversionLocal;
            sol.ValorPivoteSistemaConversionSistema = comision.ValorPivoteSistemaConversionSistema;
        }
        sol.MontoPagoLocal = vc.MontoDestino != null ? vc.MontoDestino.subtract(sol.MontoComisionLocal, MathContext.DECIMAL128) : BigDecimal.ZERO;
        if (comisionPER.compareTo(BigDecimal.ZERO) == 1) {
            sol.MontoPago = sol.MontoPago.subtract(comisionPER, MathContext.DECIMAL128);
        } else {
            sol.MontoPago = vc.Monto != null ? vc.Monto.subtract(sol.MontoComision, MathContext.DECIMAL128) : BigDecimal.ZERO;
        }
        sol.CostoMonedaLocalInversion = vc.MontoDestino;
        sol.ValorCuotaInversion = vc.ValorCuota;
        sol.ParidadFechaCertificacion = vc.ValorParidad;
    }

    private static void encabezado(NormalContexto c, InSolicitud solicitud, int i, String tm, String procedenciaCanal, ClientePortafolioSolicitudes cliente,
            SerieFondoSolicitudes serie, RegimenSerieDeFondo r, SolicitudAporteRescate sol) {
        NormalEncabezado.completar(c, solicitud, i, tm, procedenciaCanal, cliente, serie, r, sol);
    }
}
