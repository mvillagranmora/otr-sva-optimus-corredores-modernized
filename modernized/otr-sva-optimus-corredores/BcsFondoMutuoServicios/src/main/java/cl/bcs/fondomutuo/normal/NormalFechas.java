package cl.bcs.fondomutuo.normal;

import cl.bcs.fondomutuo.cierre.FechaLegacy;
import cl.bcs.fondomutuo.cierre.SerieFondoSolicitudes;
import cl.bcs.fondomutuo.comisionnn.ComisionNnDatos.Moneda;
import cl.bcs.fondomutuo.fechas.CalcularFechaSolicitudAporteRescate.InFechas;
import cl.bcs.fondomutuo.normal.NormalContrato.InSolicitud;

/** The request of one series to {@code CalcularFechaSolicitudAporteRescate} (legacy :545-610): state of the fund by profile, valutas and countries. */
final class NormalFechas {

    private NormalFechas() {
    }

    static InFechas armar(NormalContexto c, InSolicitud solicitud, SerieFondoSolicitudes serie) {
        boolean comercial = NormalConstantes.PERFIL_COMERCIAL.equalsIgnoreCase(c.perfilFondoMutuo);
        InFechas fecha = new InFechas();
        fecha.IdInterno = solicitud.IdInterno;
        fecha.IdSerieFondo = solicitud.IdSerieFondo;
        if (NormalConstantes.SI.equalsIgnoreCase(solicitud.SolicitudBatch) && NormalConstantes.NO.equals(c.respetaValutasCargaMasiva)) {
            fecha.EstadoFondo = NormalConstantes.ABIERTO;
            fecha.EstadoLiquidacion = NormalConstantes.ABIERTO;
            fecha.NumeroDiasRetencion = 0L;
            fecha.NumeroValutaValorCuota = 0L;
            fecha.NumeroValutaCertificacion = 0L;
            fecha.NumeroValutaCurse = 0L;
            fecha.SolicitudBatch = NormalConstantes.SI;
        } else {
            if (NormalConstantes.TIPO_SOLICITUD_APORTE.equalsIgnoreCase(solicitud.TipoSolicitud)) {
                fecha.EstadoFondo = comercial ? serie.EstadoAporteComercial : serie.EstadoAporteBackoffice;
                fecha.NumeroDiasRetencion = serie.DiasRetencionesAporte;
                fecha.NumeroValutaValorCuota = serie.DiasValutaValorCuotaAporte;
                fecha.NumeroValutaCertificacion = serie.DiasValutaCertificacionAporte;
                fecha.NumeroValutaCurse = serie.DiasValutaCurseAporte;
            } else if (NormalConstantes.TIPO_SOLICITUD_RESCATE.equalsIgnoreCase(solicitud.TipoSolicitud)) {
                fecha.EstadoFondo = comercial ? serie.EstadoRescateComercial : serie.EstadoRescateBackoffice;
                fecha.NumeroDiasRetencion = serie.DiasRetencionesRescate;
                fecha.NumeroValutaValorCuota = serie.DiasValutaValorCuotaRescate;
                fecha.NumeroValutaCertificacion = serie.DiasValutaCertificacionRescate;
                fecha.NumeroValutaCurse = serie.DiasValutaCurseRescate;
            } else {
                fecha.EstadoFondo = NormalConstantes.CERRADO;
                fecha.NumeroDiasRetencion = 0L;
                fecha.NumeroValutaValorCuota = 0L;
                fecha.NumeroValutaCertificacion = 0L;
                fecha.NumeroValutaCurse = 0L;
            }
            fecha.EstadoLiquidacion = comercial ? serie.EstadoLiquidezComercial : serie.EstadoLiquidezBackoffice;
            if (NormalConstantes.SI.equals(solicitud.SolicitudBatch)) {   // legacy: the fund is always open for batch
                fecha.EstadoFondo = NormalConstantes.ABIERTO;
                fecha.EstadoLiquidacion = NormalConstantes.ABIERTO;
            }
            fecha.SolicitudBatch = NormalConstantes.NO;
        }
        fecha.FechaEmision = NormalConstantes.FECHA_DEFECTO.equalsIgnoreCase(solicitud.FechaOperacion) ? FechaLegacy.texto(serie.FechaFondo)
                : solicitud.FechaOperacion;
        if (NormalConstantes.FECHA_DEFECTO.equalsIgnoreCase(solicitud.FechaOperacion)) {
            if (NormalConstantes.TIPO_SOLICITUD_APORTE.equalsIgnoreCase(solicitud.TipoSolicitud)) {
                String estado = comercial ? serie.EstadoAporteComercial : serie.EstadoAporteBackoffice;
                fecha.FechaEmision = NormalConstantes.ABIERTO.equalsIgnoreCase(estado) ? FechaLegacy.texto(serie.FechaFondo) : c.fechaSistema;
            }
            if (NormalConstantes.TIPO_SOLICITUD_RESCATE.equalsIgnoreCase(solicitud.TipoSolicitud)) {
                String estado = comercial ? serie.EstadoRescateComercial : serie.EstadoRescateBackoffice;
                fecha.FechaEmision = NormalConstantes.ABIERTO.equalsIgnoreCase(estado) ? FechaLegacy.texto(serie.FechaFondo) : c.fechaSistema;
            }
        }
        fecha.TipoSolicitud = solicitud.TipoSolicitud;
        fecha.FondoExtranjero = serie.FondoExtranjero;
        fecha.IdPaisFondo = serie.IdPaisOrigen;
        fecha.IdPaisEmisor = serie.IdPaisEmisor;
        Moneda moneda = c.hmonedasFondo.get(serie.IdMoneda);
        fecha.IdPaisMoneda = moneda != null ? moneda.IdPais : 0L;
        if (NormalConstantes.SI.equalsIgnoreCase(c.rescateGeneraSpotDolar) && NormalConstantes.DOLAR.equalsIgnoreCase(solicitud.IdMoneda)
                && NormalConstantes.PESOS.equalsIgnoreCase(solicitud.IdMonedaPago) && NormalConstantes.TIPO_SOLICITUD_RESCATE.equalsIgnoreCase(solicitud.TipoSolicitud)) {
            fecha.RescateGeneraSpotCertificacion = NormalConstantes.SI;
        }
        fecha.FondoNacionalDolar = serie.FondoNacionalDolar;
        fecha.IdMercadoFondoMutuo = serie.IdMercadoFondoMutuo;
        return fecha;
    }
}
