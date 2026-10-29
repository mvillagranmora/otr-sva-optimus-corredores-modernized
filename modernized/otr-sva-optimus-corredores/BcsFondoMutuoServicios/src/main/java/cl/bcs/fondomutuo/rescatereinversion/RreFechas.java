package cl.bcs.fondomutuo.rescatereinversion;

import java.util.HashMap;

import cl.bcs.fondomutuo.cierre.FechaLegacy;
import cl.bcs.fondomutuo.cierre.ListaHash;
import cl.bcs.fondomutuo.cierre.SerieFondoSolicitudes;
import cl.bcs.fondomutuo.comisionnn.ComisionNnDatos.Moneda;
import cl.bcs.fondomutuo.fechas.CalcularFechaSolicitudAporteRescate;
import cl.bcs.fondomutuo.fechas.CalcularFechaSolicitudAporteRescate.EntradaCalcularFechaSolicitudAporteRescate;
import cl.bcs.fondomutuo.fechas.CalcularFechaSolicitudAporteRescate.InFechas;
import cl.bcs.fondomutuo.fechas.CalcularFechaSolicitudAporteRescate.ResultadoFecha;
import cl.bcs.fondomutuo.port.out.kit.ConfiguracionSistemaPort;
import cl.bcs.fondomutuo.rescatereinversion.RescateReinversionContrato.InSolicitudesAporte;
import cl.bcs.fondomutuo.rescatereinversion.RescateReinversionContrato.InSolicitudesRescate;

/** The two date requests of {@code CrearSolicitudRescateReinversion}: the rescue (FM-REI-08, RR:470-510) and {@code obtenerFechaAporte} (FM-REI-12, RR:1423-1510). */
final class RreFechas {

    private final CalcularFechaSolicitudAporteRescate fechas;
    private final ConfiguracionSistemaPort configuracion;

    RreFechas(CalcularFechaSolicitudAporteRescate fechas, ConfiguracionSistemaPort configuracion) {
        this.fechas = fechas;
        this.configuracion = configuracion;
    }

    /** Legacy {@code Fecha.stringToSqlDate(Date)} of a 3A date: a null date is a NullPointerException. */
    static String texto(java.time.LocalDate fecha) {
        return FechaLegacy.texto(java.sql.Date.valueOf(fecha));
    }

    /** FM-REI-08: the date request of one rescue whose series exists. */
    static InFechas entradaRescate(RreContexto c, InSolicitudesRescate solicitud, SerieFondoSolicitudes serie) {
        boolean comercial = RreConstantes.PERFIL_COMERCIAL.equalsIgnoreCase(c.perfilFondoMutuo);
        InFechas fecha = new InFechas();
        fecha.IdInterno = solicitud.IdInterno;
        fecha.IdSerieFondo = solicitud.IdSerieFondo;
        fecha.FechaEmision = FechaLegacy.texto(serie.FechaFondo);
        if (RreConstantes.SI.equalsIgnoreCase(solicitud.SolicitudBatch) && RreConstantes.NO.equals(c.respetaValutasCargaMasiva)) {
            fecha.EstadoFondo = RreConstantes.ABIERTO;
            fecha.EstadoLiquidacion = RreConstantes.ABIERTO;
            fecha.NumeroDiasRetencion = 0L;
            fecha.NumeroValutaValorCuota = 0L;
            fecha.NumeroValutaCertificacion = 0L;
            fecha.NumeroValutaCurse = 0L;
            fecha.SolicitudBatch = RreConstantes.SI;
        } else {
            fecha.EstadoFondo = comercial ? serie.EstadoRescateComercial : serie.EstadoRescateBackoffice;
            fecha.NumeroDiasRetencion = RreReglas.retencion(solicitud.TipoReinversion, serie.DiasRetencionesRescate);
            fecha.NumeroValutaValorCuota = RreReglas.switchOInternacional(solicitud.TipoReinversion) ? 1 : serie.DiasValutaValorCuotaRescate;
            fecha.NumeroValutaCertificacion = RreReglas.switchOInternacional(solicitud.TipoReinversion) ? 2 : serie.DiasValutaCertificacionRescate;
            fecha.NumeroValutaCurse = serie.DiasValutaCurseRescate;
            fecha.EstadoLiquidacion = comercial ? serie.EstadoLiquidezComercial : serie.EstadoLiquidezBackoffice;
            if (RreConstantes.SI.equals(solicitud.SolicitudBatch)) {   // legacy: the fund is always open for batch
                fecha.EstadoFondo = RreConstantes.ABIERTO;
                fecha.EstadoLiquidacion = RreConstantes.ABIERTO;
            }
            fecha.SolicitudBatch = RreConstantes.NO;
        }
        fecha.TipoSolicitud = RreConstantes.TIPO_SOLICITUD_RESCATE;
        fecha.FondoExtranjero = serie.FondoExtranjero;
        fecha.IdPaisFondo = serie.IdPaisOrigen;
        fecha.IdPaisEmisor = serie.IdPaisEmisor;
        Moneda moneda = c.hmonedasFondo.get(serie.IdMoneda);
        fecha.IdPaisMoneda = moneda != null ? moneda.IdPais : 0L;
        if (RreConstantes.SI.equalsIgnoreCase(c.rescateGeneraVentaSpotDolar) && RreConstantes.SI.equalsIgnoreCase(c.hseriesMonedasDiferentes.get(solicitud.IdInterno))) {
            fecha.RescateGeneraSpotCertificacion = RreConstantes.SI;
        }
        fecha.FondoNacionalDolar = serie.FondoNacionalDolar;
        fecha.IdMercadoFondoMutuo = serie.IdMercadoFondoMutuo;
        return fecha;
    }

    HashMap<Long, ResultadoFecha> calcular(EntradaCalcularFechaSolicitudAporteRescate entrada) throws Exception {
        return ListaHash.aHash(fechas.logica(entrada).P_FECHASAPORTERESCATE, f -> f.IdInterno);
    }

    /** FM-REI-12 ({@code obtenerFechaAporte}): it mutates the contributions ({@code TipoReinversion}, {@code SolicitudBatch}) as the legacy. */
    HashMap<Long, ResultadoFecha> obtenerFechaAporte(RreContexto c) throws Exception {
        String idMonedaSistema = configuracion.texto(c.entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
        String idMonedaSistemaAdicional = configuracion.texto(c.entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaAdicional");
        EntradaCalcularFechaSolicitudAporteRescate entradaFechas = new EntradaCalcularFechaSolicitudAporteRescate();
        entradaFechas.p_IdEmpresa = c.entrada.p_IdEmpresa;
        entradaFechas.p_IdPais = c.idPaisEmpresa;
        for (InSolicitudesAporte solicitud : c.entrada.p_SolicitudesAporte) {
            RreReglas.tipoReinversionAporte(solicitud, c.hseries.get(solicitud.IdSerieFondoOrigen), c.hseries.get(solicitud.IdSerieFondo));
        }
        boolean comercial = RreConstantes.PERFIL_COMERCIAL.equalsIgnoreCase(c.perfilFondoMutuo);
        for (InSolicitudesAporte solicitud : c.entrada.p_SolicitudesAporte) {
            SerieFondoSolicitudes serie = c.hseries.get(solicitud.IdSerieFondo);
            InFechas fecha = new InFechas();
            InSolicitudesRescate res = c.hrescates.get(solicitud.IdInterno);
            ResultadoFecha fechaRescate = c.hfechas.get(solicitud.IdInterno);
            solicitud.SolicitudBatch = c.solicitudesBatches.get(solicitud.IdInterno);
            if (serie != null) {
                fecha.IdInterno = solicitud.IdInterno;
                fecha.IdSerieFondo = solicitud.IdSerieFondo;
                if (solicitud.IdMoneda.equalsIgnoreCase(res.IdMoneda)) {
                    fecha.FechaEmision = idMonedaSistema.equalsIgnoreCase(serie.IdMoneda) || idMonedaSistemaAdicional.equalsIgnoreCase(serie.IdMoneda)
                            ? texto(fechaRescate.FechaLiquidacion) : texto(fechaRescate.FechaCertificacion);
                } else {
                    fecha.FechaEmision = texto(fechaRescate.FechaLiquidacion);
                }
                if (RreConstantes.SI.equalsIgnoreCase(solicitud.SolicitudBatch) && RreConstantes.NO.equals(c.respetaValutasCargaMasiva)) {
                    fecha.EstadoFondo = RreConstantes.ABIERTO;
                    fecha.EstadoLiquidacion = RreConstantes.ABIERTO;
                    fecha.NumeroDiasRetencion = 0L;
                    fecha.NumeroValutaValorCuota = 0L;
                    fecha.NumeroValutaCertificacion = 0L;
                    fecha.NumeroValutaCurse = 0L;
                    fecha.SolicitudBatch = RreConstantes.SI;
                } else {
                    fecha.EstadoFondo = RreConstantes.ABIERTO;
                    fecha.NumeroDiasRetencion = RreReglas.retencion(solicitud.TipoReinversion, serie.DiasRetencionesAporte);
                    fecha.NumeroValutaValorCuota = RreReglas.switchOInternacional(solicitud.TipoReinversion) ? 1 : serie.DiasValutaValorCuotaAporte;
                    fecha.NumeroValutaCertificacion = RreReglas.switchOInternacional(solicitud.TipoReinversion) ? 2 : serie.DiasValutaCertificacionAporte;
                    fecha.NumeroValutaCurse = RreReglas.switchOInternacional(solicitud.TipoReinversion) ? 0 : serie.DiasValutaCurseAporte;
                    fecha.EstadoLiquidacion = comercial ? serie.EstadoLiquidezComercial : serie.EstadoLiquidezBackoffice;
                    fecha.Switch = solicitud.TipoReinversion.equalsIgnoreCase(RreConstantes.SWITCH) ? RreConstantes.SI : RreConstantes.NO;
                    fecha.FechaCertificacionRescateOrigen = texto(fechaRescate.FechaCertificacion);
                    fecha.FechaLiquidacionRescateOrigen = texto(fechaRescate.FechaLiquidacion);
                    fecha.FechaValorCuotaRescateOrigen = texto(fechaRescate.FechaValorCuota);
                    if (RreConstantes.SI.equals(solicitud.SolicitudBatch)) {
                        fecha.EstadoFondo = RreConstantes.ABIERTO;
                        fecha.EstadoLiquidacion = RreConstantes.ABIERTO;
                    }
                    fecha.SolicitudBatch = RreConstantes.NO;
                }
                fecha.TipoSolicitud = RreConstantes.TIPO_SOLICITUD_APORTE;
                fecha.FondoExtranjero = serie.FondoExtranjero;
                fecha.IdPaisFondo = serie.IdPaisOrigen;
                fecha.IdPaisEmisor = serie.IdPaisEmisor;
                Moneda moneda = c.hmonedasFondo.get(serie.IdMoneda);
                fecha.IdPaisMoneda = moneda != null ? moneda.IdPais : 0L;
                fecha.FondoNacionalDolar = serie.FondoNacionalDolar;
                fecha.IdMercadoFondoMutuo = serie.IdMercadoFondoMutuo;
                entradaFechas.p_SolicitudAporteRescate.add(fecha);
            }
        }
        return calcular(entradaFechas);
    }
}
