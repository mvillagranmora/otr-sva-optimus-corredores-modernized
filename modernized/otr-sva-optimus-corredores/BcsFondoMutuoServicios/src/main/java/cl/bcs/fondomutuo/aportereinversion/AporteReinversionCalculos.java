package cl.bcs.fondomutuo.aportereinversion;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import cl.bcs.fondomutuo.aportereinversion.AporteReinversionContrato.InSolicitudesAporte;
import cl.bcs.fondomutuo.cierre.FechaLegacy;
import cl.bcs.fondomutuo.cierre.SerieFondoSolicitudes;
import cl.bcs.fondomutuo.cierre.contrato.ParidadContrato;
import cl.bcs.fondomutuo.comisionnn.ComisionNnDatos.Moneda;
import cl.bcs.fondomutuo.cuotas.CalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.fechas.CalcularFechaSolicitudAporteRescate;
import cl.bcs.fondomutuo.fechas.CalcularFechaSolicitudAporteRescate.InFechas;
import cl.bcs.fondomutuo.fechas.CalcularFechaSolicitudAporteRescate.ResultadoFecha;
import cl.bcs.fondomutuo.port.out.http.ParidadPort;
import cl.bcs.fondomutuo.requisitos.Hashes;
import cl.bcs.fondomutuo.requisitos.SecuenciaPort;

/** Parity of the total investment, dates and quotas of one lot of {@code CrearSolicitudAporteReinversion} (legacy :569-717), with the 3A services. */
final class AporteReinversionCalculos {

    private final CalcularFechaSolicitudAporteRescate fechas;
    private final CalcularMontoNumeroCuota cuotas;
    private final ParidadPort paridad;
    private final SecuenciaPort secuencias;
    private final AporteReinversionDatos datos;

    AporteReinversionCalculos(CalcularFechaSolicitudAporteRescate fechas, CalcularMontoNumeroCuota cuotas, ParidadPort paridad, SecuenciaPort secuencias,
            AporteReinversionDatos datos) {
        this.fechas = fechas;
        this.cuotas = cuotas;
        this.paridad = paridad;
        this.secuencias = secuencias;
        this.datos = datos;
    }

    /** Legacy {@code Fecha.stringToSqlDate(Date)} on a 3A date: a null date is a NullPointerException. */
    private static String texto(java.time.LocalDate fecha) {
        return FechaLegacy.texto(java.sql.Date.valueOf(fecha));
    }

    /** Legacy :569-588: the total investment (or the amount) of every request in the system currency, at the system date. */
    void paridadDeLaInversion(AporteReinversionContexto c) throws Exception {
        ParidadContrato.CalcularParidadEntrada entrada = new ParidadContrato.CalcularParidadEntrada();
        entrada.p_IdEmpresa = c.entrada.p_IdEmpresa;
        entrada.p_IdTrabajo = 1L;
        for (InSolicitudesAporte solicitud : c.entrada.p_SolicitudesAporte) {
            ParidadContrato.CalculoEntrada calculoEntrada = new ParidadContrato.CalculoEntrada();
            calculoEntrada.IdTrabajo = 1L;
            calculoEntrada.IdInterno = solicitud.IdInterno;
            calculoEntrada.Fecha = c.fechaSistema;
            calculoEntrada.IdMonedaOrigen = solicitud.IdMoneda;
            calculoEntrada.IdMonedaDestino = c.idMonedaLocal;
            calculoEntrada.ValorCambioLocalModificado = BigDecimal.ZERO;
            calculoEntrada.MontoOrigen = BigDecimal.ZERO.compareTo(solicitud.MontoTotalInversion) != 0 ? solicitud.MontoTotalInversion : solicitud.Monto;
            calculoEntrada.IdUso = "";
            entrada.p_CalculosEntrada.add(calculoEntrada);
        }
        c.hashMontoTotalInversionMonedaSistema = Hashes.aHash(ParidadContrato.calcular(paridad, entrada).p_out_CalculosSalida, s -> s.IdInterno);
    }

    /** Legacy :590-674: the dates of every request whose series was found, by the profile and the type of reinversion. */
    void calcularFechas(AporteReinversionContexto c) throws Exception {
        CalcularFechaSolicitudAporteRescate.EntradaCalcularFechaSolicitudAporteRescate entradaFechas =
                new CalcularFechaSolicitudAporteRescate.EntradaCalcularFechaSolicitudAporteRescate();
        entradaFechas.p_IdEmpresa = c.entrada.p_IdEmpresa;
        entradaFechas.p_IdPais = c.idPaisEmpresa;
        boolean comercial = AporteReinversionConstantes.PERFIL_COMERCIAL.equalsIgnoreCase(c.perfilFondoMutuo);
        for (InSolicitudesAporte solicitud : c.entrada.p_SolicitudesAporte) {
            SerieFondoSolicitudes serie = c.hseries.get(solicitud.IdSerieFondo);
            InFechas fecha = new InFechas();
            if (serie != null) {
                fecha.IdInterno = solicitud.IdInterno;
                fecha.IdSerieFondo = solicitud.IdSerieFondo;
                fecha.FechaEmision = solicitud.FechaEmision == null ? FechaLegacy.texto(serie.FechaFondo) : solicitud.FechaEmision;
                if (AporteReinversionConstantes.SI.equalsIgnoreCase(solicitud.SolicitudBatch) && AporteReinversionConstantes.NO.equals(c.respetaValutasCargaMasiva)) {
                    fecha.EstadoFondo = AporteReinversionConstantes.ABIERTO;
                    fecha.EstadoLiquidacion = AporteReinversionConstantes.ABIERTO;
                    fecha.NumeroDiasRetencion = 0L;
                    fecha.NumeroValutaValorCuota = 0L;
                    fecha.NumeroValutaCertificacion = 0L;
                    fecha.NumeroValutaCurse = 0L;
                    fecha.SolicitudBatch = AporteReinversionConstantes.SI;
                } else {
                    boolean switchOInternacional = solicitud.TipoReinversion.equalsIgnoreCase("SWITCH") || solicitud.TipoReinversion.equalsIgnoreCase("INTERNACIONAL");
                    fecha.EstadoFondo = comercial ? serie.EstadoAporteComercial : serie.EstadoAporteBackoffice;
                    // the unboxing of the series values is the legacy one: a null day count of the series raises a NullPointerException on the branch that uses it
                    fecha.NumeroDiasRetencion = solicitud.TipoReinversion.equalsIgnoreCase("SWITCH") ? 2
                            : (solicitud.TipoReinversion.equalsIgnoreCase("INTERNACIONAL") ? 4 : serie.DiasRetencionesAporte);
                    fecha.NumeroValutaValorCuota = switchOInternacional ? 1 : serie.DiasValutaValorCuotaAporte;
                    fecha.NumeroValutaCertificacion = switchOInternacional ? 2 : serie.DiasValutaCertificacionAporte;
                    fecha.NumeroValutaCurse = solicitud.TipoReinversion.equalsIgnoreCase("VALUTASNACUSD") ? serie.DiasValutaCurseAporte
                            : (switchOInternacional ? 0 : serie.DiasValutaCurseAporte);
                    fecha.EstadoLiquidacion = comercial ? serie.EstadoLiquidezComercial : serie.EstadoLiquidezBackoffice;
                    fecha.Switch = solicitud.Switch;
                    fecha.FechaCertificacionRescateOrigen = solicitud.FechaCertificacionRescateOrigen;
                    fecha.FechaLiquidacionRescateOrigen = solicitud.FechaLiquidacionRescateOrigen;
                    fecha.FechaValorCuotaRescateOrigen = solicitud.FechaValorCuotaRescateOrigen;
                    if (AporteReinversionConstantes.SI.equals(solicitud.SolicitudBatch)) {   // the fund is always open for batch
                        fecha.EstadoFondo = AporteReinversionConstantes.ABIERTO;
                        fecha.EstadoLiquidacion = AporteReinversionConstantes.ABIERTO;
                    }
                    fecha.SolicitudBatch = AporteReinversionConstantes.NO;
                }
                fecha.TipoSolicitud = AporteReinversionConstantes.TIPO_SOLICITUD_APORTE;
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
        c.hfechas = Hashes.aHash(fechas.logica(entradaFechas).P_FECHASAPORTERESCATE, f -> f.IdInterno);
    }

    /** Legacy :676-717: the quotas, the regimes of the rescues being reinvested, and the sequences of the new requests (in that order). */
    void calcularCuotas(AporteReinversionContexto c) throws Exception {
        CalcularMontoNumeroCuota.EntradaCalcularMontoNumeroCuota entradaCalculo = new CalcularMontoNumeroCuota.EntradaCalcularMontoNumeroCuota();
        entradaCalculo.p_IdEmpresa = c.entrada.p_IdEmpresa;
        List<Long> idsRescates = new ArrayList<>();
        for (InSolicitudesAporte solicitud : c.entrada.p_SolicitudesAporte) {
            CalcularMontoNumeroCuota.Serie serie = new CalcularMontoNumeroCuota.Serie();
            SerieFondoSolicitudes serieFondo = c.hseries.get(solicitud.IdSerieFondo);
            ResultadoFecha fecha = c.hfechas.get(solicitud.IdInterno);
            serie.IdInterno = solicitud.IdInterno;
            if (solicitud.FechaValorCuota != null) {
                serie.Fecha = FechaLegacy.texto(solicitud.FechaValorCuota);
            } else if (fecha != null) {
                serie.Fecha = texto(fecha.FechaValorCuota);
            } else {
                serie.Fecha = c.fechaSistema;
            }
            serie.IdSerieFondo = solicitud.IdSerieFondo;
            serie.CalculoPorMontoCuota = AporteReinversionConstantes.CALCULO_POR_MONTO;
            serie.Monto = solicitud.Monto;
            serie.NumeroCuotas = solicitud.NumeroCuotas;
            serie.IdMonedaFondo = serieFondo != null ? serieFondo.IdMoneda : "";
            serie.IdMonedaInversion = solicitud.IdMoneda;
            serie.TipoSolicitud = AporteReinversionConstantes.TIPO_SOLICITUD_APORTE;
            serie.TipoFactorAjuste = AporteReinversionConstantes.UF;
            serie.IdEmpresa = c.entrada.p_IdEmpresa;
            entradaCalculo.p_Series.add(serie);
            if (!idsRescates.contains(solicitud.IdSolicitudAporteRescateRelacionado)) idsRescates.add(solicitud.IdSolicitudAporteRescateRelacionado);
        }
        c.regimenSolicitudReinversion = datos.regimenesDeLosRescates(idsRescates);
        CalcularMontoNumeroCuota.SalidaCalcularMontoNumeroCuota salidaCalculo = cuotas.logica(entradaCalculo);
        c.secuencias = secuencias.siguientes(AporteReinversionConstantes.SECUENCIA_SOLICITUD, Long.valueOf(c.entrada.p_SolicitudesAporte.size()));
        c.valoresCuota = Hashes.aHash(salidaCalculo.P_OUT_RESULTADO, r -> r.IdInterno);
        c.listaEntrada = Hashes.aHash(c.entrada.p_SolicitudesAporte, s -> s.IdInterno);
    }
}
