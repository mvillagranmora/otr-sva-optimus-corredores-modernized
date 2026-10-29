package cl.bcs.fondomutuo.rescatereinversion;

import java.math.BigDecimal;

import cl.bcs.fondomutuo.cierre.FechaLegacy;
import cl.bcs.fondomutuo.cierre.SerieFondoSolicitudes;
import cl.bcs.fondomutuo.comisionnn.Pojos;
import cl.bcs.fondomutuo.comisionnn.ResultadoSolicitud;
import cl.bcs.fondomutuo.comisionnn.SolicitudAporteRescate;
import cl.bcs.fondomutuo.cuotas.CalcularMontoNumeroCuota.Resultado;
import cl.bcs.fondomutuo.fechas.CalcularFechaSolicitudAporteRescate.ResultadoFecha;
import cl.bcs.fondomutuo.requisitos.RequisitosDatos.ClientePortafolioSolicitudes;
import cl.bcs.fondomutuo.rescatereinversion.RescateReinversionContrato.InSolicitudesAporte;
import cl.bcs.fondomutuo.rescatereinversion.RescateReinversionContrato.InSolicitudesRescate;

/** The RESREINV row of one rescue (FM-REI-16/18, RR:728-831) and its output element (FM-REI-23, RR:1055-1085), statement by statement. */
final class RreEncabezado {

    private RreEncabezado() {
    }

    private static java.sql.Date sql(java.time.LocalDate f) {
        return f != null ? java.sql.Date.valueOf(f) : null;
    }

    static SolicitudAporteRescate solicitud(RreContexto c, InSolicitudesRescate solicitud, InSolicitudesAporte apor, ClientePortafolioSolicitudes cliente,
            SerieFondoSolicitudes serie, SerieFondoSolicitudes serieDestino, Resultado vc, ResultadoFecha fecha, RreDatos.RegimenSerieDeFondo rRescate) {
        SolicitudAporteRescate sol = new SolicitudAporteRescate();
        sol.IdInterno = solicitud.IdInterno;
        sol.IdSolicitudAporteRescate = c.secuencias.get(c.rescatesUnicosV2.size() - 1);   // D-F2-6: the rescues of one group share the id
        sol.IdCliente = solicitud.IdCliente;
        sol.IdPortafolio = solicitud.IdPortafolio;
        sol.TipoSolicitud = RreConstantes.TIPO_SOLICITUD_RESCATE;
        sol.IdSerieFondo = serie != null ? serie.IdSerieFondo : "";
        sol.IdTipoMovimientoFondoMutuo = RreConstantes.REINVERSION;
        sol.IdDividendo = 0L;
        sol.IdSerieFondoDestino = serieDestino.IdSerieFondo;
        sol.IdEmisorDestino = c.haportes.size() == 1 ? apor.IdEmisor : "";
        sol.IdClienteDestino = 0L;
        sol.IdPortafolioDestino = 0L;
        sol.IndicadorMLT = apor.IndicadorMLT;
        sol.Overnight = RreConstantes.NO;
        sol.IdSolicitudAporteRescateRelacionado = 0L;
        sol.IdTipoCartera = "";
        sol.CalculoPorMontoCuota = solicitud.CalculoPorMontoCuota;
        sol.FechaEmision = FechaLegacy.aFechaSql(c.fechaSistema);
        sol.FechaOriginalInversion = FechaLegacy.aFechaSql(RreConstantes.FECHA_DEFECTO);
        sol.FechaIngresoRegistro = c.fechaHoraUTC;
        sol.IdMoneda = solicitud.IdMoneda;
        sol.Estado = RreConstantes.ESTADO_INGRESADO;
        sol.IdMedioDePago = solicitud.IdMedioDePago;
        sol.Glosa = solicitud.Glosa;
        sol.IdEmpresa = c.entrada.p_IdEmpresa;
        sol.IdUsuario = c.entrada.p_IdUsuario;
        sol.IdModulo = c.entrada.p_IdModulo;
        sol.IdEmisorOrigen = serie != null ? serie.IdEmisor : "";
        sol.SolicitudBatch = solicitud.SolicitudBatch;
        sol.OrigenOperacion = solicitud.OrigenOperacion;
        sol.IdAutorizadoOrigen = solicitud.IdAutorizadoOrigen;
        sol.IdEmpleadoEjecutivo = cliente != null && cliente.IdEmpleadoEjecutivo != null ? cliente.IdEmpleadoEjecutivo : 0L;
        sol.IdRegimenTributario = solicitud.IdRegimenTributario;
        sol.IngresoAutomatico = solicitud.IngresoAutomatico;
        sol.IdTipoCanalIngreso = c.entrada.p_IdTipoCanalIngreso != null ? c.entrada.p_IdTipoCanalIngreso : RreConstantes.CANAL_AGENTE;
        sol.FolioContrato = solicitud.FolioContrato > 0 ? solicitud.FolioContrato : c.folioContratoSolicitudes.get(c.entrada.p_SolicitudesRescate.indexOf(solicitud));
        sol.IdPersona = cliente != null ? cliente.IdPersona : 0L;
        sol.IdTipoInstrumento = serie != null ? serie.IdTipoInstrumento : "";
        sol.IdInstrumento = serie != null ? serie.IdInstrumento : 0L;
        sol.EstadoProceso = RreConstantes.ESTADO_PENDIENTE;
        sol.Monto = BigDecimal.ZERO;
        sol.NumeroCuotas = BigDecimal.ZERO;
        sol.ValorCuota = BigDecimal.ZERO;
        sol.CostoUF = BigDecimal.ZERO;
        sol.CostoMonedaLocal = BigDecimal.ZERO;
        sol.ValorUFFechaInversion = BigDecimal.ZERO;
        sol.ParidadFechaInversion = BigDecimal.ZERO;
        sol.IdMonedaSistema = "";
        sol.MontoMonedaSistema = BigDecimal.ZERO;
        sol.MontoPagoLocal = BigDecimal.ZERO;
        sol.MontoPago = BigDecimal.ZERO;
        sol.MontoComision = BigDecimal.ZERO;
        sol.MontoComisionLocal = BigDecimal.ZERO;
        sol.IdAgrupador = c.secuenciasIdAgrupador.get(c.rescatesUnicosV2.size() - 1);
        sol.IdAgrupadorInterno = solicitud.IdAgrupador;
        sol.Switch = RreConstantes.NO;   // corrected with the destination rows (FM-REI-31)
        sol.Porcentaje = solicitud.Porcentaje;
        if (vc != null) {
            sol.Monto = vc.Monto;
            sol.NumeroCuotas = vc.NumeroCuotas;
            sol.ValorCuota = vc.ValorCuota;
            sol.CostoUF = vc.CostoEnFactorAjuste;
            sol.CostoMonedaLocal = vc.MontoDestino;
            sol.ValorUFFechaInversion = vc.ValorFactorAjuste;
            sol.ParidadFechaInversion = vc.ValorParidad;
            sol.IdMonedaSistema = vc.IdMonedaSistema;
            sol.MontoMonedaSistema = vc.MontoMonedaSistema;
            sol.MontoPagoLocal = vc.MontoDestino;
            sol.MontoPago = vc.Monto;
            sol.MontoInversion = vc.Monto;
            sol.CostoMonedaLocalInversion = vc.MontoDestino;
            sol.ValorCuotaInversion = vc.ValorCuota;
            sol.ParidadFechaCertificacion = vc.ValorParidad;
            sol.NumeroCuotasInversion = vc.NumeroCuotas;
        }
        if (fecha != null) {
            sol.FechaAceptacion = sql(fecha.FechaAceptacion);
            sol.FechaValorCuota = solicitud.FechaValorCuota != null ? solicitud.FechaValorCuota : sql(fecha.FechaValorCuota);
            sol.FechaCertificacion = sql(fecha.FechaCertificacion);
            sol.FechaLiquidacion = sql(fecha.FechaLiquidacion);
        }
        if (rRescate != null) {
            sol.Tributacion = RreReglas.tributacion(rRescate.GrupoRegimen);
        }
        return sol;
    }

    static ResultadoSolicitud salida(SolicitudAporteRescate sol, SerieFondoSolicitudes serie, ClientePortafolioSolicitudes cliente, Resultado vcApor,
            InSolicitudesAporte apor) {
        ResultadoSolicitud s = new ResultadoSolicitud();
        s = Pojos.copiar(sol, s);
        if (serie != null) {
            s.IdFondo = serie.IdFondo;
            s.Nemotecnico = serie.Nemotecnico;
            s.NombreFondo = serie.NombreFondo;
            s.NombreCorto = serie.NombreCortoTipoFondo;
            s.NombreSerieFondo = serie.NombreSerieFondo;
            s.CodigoSerie = serie.CodigoSerie;
            s.IdEmisorOrigen = serie.IdEmisor;
        }
        if (cliente != null) {
            s.DocumentoIdentidad = cliente.DocumentoIdentidad;
            s.CodigoPortafolio = cliente.CodigoPortafolio;
            s.NombreCliente = cliente.NombreCompleto;
        }
        s.MontoTransferencia = sol.Monto;
        s.NumeroCuotaTransferencia = sol.NumeroCuotas;
        s.FechaAceptacion = FechaLegacy.texto(sol.FechaAceptacion);
        s.FechaOriginalInversion = FechaLegacy.texto(sol.FechaOriginalInversion);
        s.FechaIngresoRegistro = sol.FechaIngresoRegistro.toLocalDateTime().format(FechaLegacy.TS);
        s.FechaLiquidacion = FechaLegacy.texto(sol.FechaLiquidacion);
        s.FechaCertificacion = FechaLegacy.texto(sol.FechaCertificacion);
        s.FechaValorCuota = FechaLegacy.texto(sol.FechaValorCuota);
        if (vcApor != null) {
            s.MontoAporte = vcApor.Monto;
            s.NumeroCuotasAporte = vcApor.NumeroCuotas;
        }
        s.IdEmisorAporte = apor.IdEmisor;
        return s;
    }
}
