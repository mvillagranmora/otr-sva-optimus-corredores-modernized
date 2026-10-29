package cl.bcs.fondomutuo.normal;

import java.math.BigDecimal;

import cl.bcs.fondomutuo.cierre.FechaLegacy;
import cl.bcs.fondomutuo.cierre.SerieFondoSolicitudes;
import cl.bcs.fondomutuo.comisionnn.SolicitudAporteRescate;
import cl.bcs.fondomutuo.normal.NormalContrato.InSolicitud;
import cl.bcs.fondomutuo.normal.NormalDatos.RegimenSerieDeFondo;
import cl.bcs.fondomutuo.requisitos.RequisitosDatos.ClientePortafolioSolicitudes;

/** The fixed part of the row of one request (legacy :769-837): identifiers, defaults, zeroed amounts. */
final class NormalEncabezado {

    private NormalEncabezado() {
    }

    static void completar(NormalContexto c, InSolicitud solicitud, int i, String tm, String procedenciaCanal, ClientePortafolioSolicitudes cliente,
            SerieFondoSolicitudes serie, RegimenSerieDeFondo r, SolicitudAporteRescate sol) {
        NormalContrato.Entrada entrada = c.entrada;
        sol.IdInterno = solicitud.IdInterno;
        sol.IdSolicitudAporteRescate = c.secuencias.get(i);
        sol.IdCliente = solicitud.IdCliente;
        sol.IdPortafolio = solicitud.IdPortafolio;
        sol.TipoSolicitud = solicitud.TipoSolicitud;
        sol.IdSerieFondo = serie != null ? serie.IdSerieFondo : "";
        sol.IdTipoMovimientoFondoMutuo = tm;
        sol.IdDividendo = entrada.p_NumeroDividendo;
        sol.IdSerieFondoDestino = "";
        sol.IdEmisorDestino = solicitud.IdEmisorDestino;
        sol.IdClienteDestino = 0L;
        sol.IdPortafolioDestino = 0L;
        sol.IndicadorMLT = solicitud.IndicadorMLT;
        sol.Overnight = NormalConstantes.NO;
        sol.IdSolicitudAporteRescateRelacionado = solicitud.IdSolicitudAporteRescateRelacionado != null ? solicitud.IdSolicitudAporteRescateRelacionado : 0L;
        sol.TipoCarteraNormativa = NormalConstantes.APORTE_FONDO_MUTUO.equalsIgnoreCase(sol.IdTipoMovimientoFondoMutuo) && r != null ? r.TipoCarteraNormativa : "";
        sol.IdTipoCartera = "";
        sol.CalculoPorMontoCuota = solicitud.CalculoPorMontoCuota;
        sol.FechaEmision = FechaLegacy.aFechaSql(c.fechaSistema);
        sol.FechaOriginalInversion = FechaLegacy.aFechaSql("0001-01-01");
        sol.FechaIngresoRegistro = c.fechaHoraUTC;
        sol.IdMoneda = solicitud.IdMoneda;
        sol.Estado = NormalConstantes.ESTADO_INGRESADO;
        sol.IdMedioDePago = solicitud.IdMedioDePago;
        sol.Glosa = solicitud.Glosa;
        sol.IdEmpresa = entrada.p_IdEmpresa;
        sol.IdUsuario = entrada.p_IdUsuario;
        sol.IdModulo = entrada.p_IdModulo;
        sol.IdEmisorOrigen = serie != null ? serie.IdEmisor : "";
        sol.SolicitudBatch = solicitud.SolicitudBatch;
        sol.OrigenOperacion = solicitud.OrigenOperacion;
        sol.IdAutorizadoOrigen = solicitud.IdAutorizadoOrigen;
        sol.IdEmpleadoEjecutivo = cliente != null && cliente.IdEmpleadoEjecutivo != null ? cliente.IdEmpleadoEjecutivo : 0L;
        sol.IdRegimenTributario = solicitud.IdRegimenTributario;
        sol.IngresoAutomatico = solicitud.IngresoAutomatico;
        sol.EsPorMontoLiquido = solicitud.EsPorMontoLiquido;
        sol.Porcentaje = solicitud.Porcentaje;
        sol.IdTipoCanalIngreso = procedenciaCanal;
        sol.FolioContrato = solicitud.FolioContrato > 0 ? solicitud.FolioContrato
                : c.folioContratoSolicitudes.get(entrada.p_SolicitudesAporteRescate.indexOf(solicitud));
        sol.IdMonedaPago = solicitud.IdMonedaPago;
        sol.IdPersona = cliente != null ? cliente.IdPersona : 0L;
        sol.IdTipoInstrumento = serie != null ? serie.IdTipoInstrumento : "";
        sol.IdInstrumento = serie != null ? serie.IdInstrumento : 0L;
        sol.Visado = NormalConstantes.TIPO_SOLICITUD_RESCATE.equalsIgnoreCase(sol.TipoSolicitud)
                || (NormalConstantes.TIPO_SOLICITUD_APORTE.equalsIgnoreCase(sol.TipoSolicitud) && NormalConstantes.CUENTA_INVERSION.equalsIgnoreCase(sol.IdMedioDePago))
                ? NormalConstantes.NO : solicitud.Visado;
        sol.IdPreferenciaPagoPortafolio = solicitud.IdPreferenciaPagoPortafolio;
        sol.EstadoProceso = NormalConstantes.ESTADO_PENDIENTE;
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
        sol.MontoComisionMonedaSistema = BigDecimal.ZERO;
        sol.GeneraSpot = sol.IdMoneda.equalsIgnoreCase(sol.IdMonedaPago) ? NormalConstantes.NO : NormalConstantes.SI;
        sol.ValidaSpot = NormalConstantes.NO;
        sol.ForzarSpot = solicitud.ForzarSpot;
        sol.TipoContratoOperacion = solicitud.TipoContratoOperacion;
        sol.FolioContratoOperacion = solicitud.FolioContratoOperacion;
        sol.IdOrigenRecaCliente = solicitud.IdOrigenRecaCliente;
        sol.SimulaCambioPortafolioEjecutadoOk = solicitud.SimulaCambioPortafolioEjecutadoOk;
        sol.IngresoDesde = solicitud.IngresoDesde;
    }
}
