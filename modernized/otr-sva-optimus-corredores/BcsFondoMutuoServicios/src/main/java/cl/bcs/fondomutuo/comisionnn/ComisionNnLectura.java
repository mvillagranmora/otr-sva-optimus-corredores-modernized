package cl.bcs.fondomutuo.comisionnn;

import java.util.ArrayList;
import java.util.List;

import cl.bcs.fondomutuo.comisionnn.ComisionNnArmado.Datos;
import cl.bcs.fondomutuo.comisionnn.ComisionNnContrato.Entrada;
import cl.bcs.fondomutuo.comisionnn.ComisionNnContrato.InSolicitudesRescate;
import cl.bcs.fondomutuo.comisionnn.ComisionNnDatos.Moneda;
import cl.bcs.fondomutuo.cierre.SerieFondoSolicitudes;
import cl.bcs.fondomutuo.requisitos.Constantes3B;
import cl.bcs.fondomutuo.requisitos.Hashes;
import cl.bcs.fondomutuo.requisitos.RequisitosDatos;
import cl.bcs.fondomutuo.requisitos.contrato.CuotaContrato;
import cl.bcs.fondomutuo.requisitos.contrato.FechaContrato;

/** The read phase of {@code CrearSolicitudRescateComisionNN}: support reads, dates and quotas (collaborators 3A), in the legacy order. */
final class ComisionNnLectura {

    private ComisionNnLectura() {
    }

    static void leerApoyo(Entrada entrada, Datos d, ComisionNnDatos datos, RequisitosDatos datosRequisitos) throws Exception {
        List<String> series = new ArrayList<>();
        List<Long> portafolios = new ArrayList<>();
        List<String> monedas = new ArrayList<>();
        List<String> regimen = new ArrayList<>();
        List<String> idTipoMovimientos = new ArrayList<>();
        idTipoMovimientos.add(Constantes3B.RESCATE_COMISION_NN);
        for (InSolicitudesRescate mov : entrada.p_SolicitudesRescate) {
            if (!series.contains(mov.IdSerieFondo)) series.add(mov.IdSerieFondo);
            if (!portafolios.contains(mov.IdPortafolio)) portafolios.add(mov.IdPortafolio);
            if (!regimen.contains(mov.IdRegimenTributario)) regimen.add(mov.IdRegimenTributario);
        }
        List<SerieFondoSolicitudes> seriesSolicitudes = datos.series(entrada.p_IdEmpresa, series);
        var clientesSolicitudes = datosRequisitos.clientesPortafolio(portafolios);
        var tipoMovimientos = datos.tiposMovimiento(idTipoMovimientos);
        var regimenesSeries = datos.regimenes(entrada.p_IdEmpresa, series, regimen);
        for (SerieFondoSolicitudes serie : seriesSolicitudes) {
            if (!monedas.contains(serie.IdMoneda)) monedas.add(serie.IdMoneda);
        }
        List<Moneda> monedasFondo = datos.monedas(monedas);

        d.hseries = Hashes.aHash(seriesSolicitudes, s -> s.IdSerieFondo);
        d.hclientes = Hashes.aHash(clientesSolicitudes, c -> c.IdPortafolio);
        d.htipoMovimiento = Hashes.aHash(tipoMovimientos, t -> t.IdTipoMovimientoFondoMutuo);
        d.hmonedas = Hashes.aHash(monedasFondo, m -> m.IdMoneda);
        d.hseriesRegimen = Hashes.aHash(regimenesSeries, r -> r.IdRegimenFondoMutuoSerieFondo);
    }

    static void calcularFechasYCuotas(Entrada entrada, Datos d, String respetaValutas, Long idPaisEmpresa, FechaContrato.Puerto fechas,
            CuotaContrato.Puerto cuotas) throws Exception {
        FechaContrato.Entrada entradaFechas = new FechaContrato.Entrada();
        entradaFechas.p_IdEmpresa = entrada.p_IdEmpresa;
        entradaFechas.p_IdPais = idPaisEmpresa;
        for (InSolicitudesRescate solicitud : entrada.p_SolicitudesRescate) {
            SerieFondoSolicitudes serie = d.hseries.get(solicitud.IdSerieFondo);
            FechaContrato.InFechas fecha = new FechaContrato.InFechas();
            if (serie != null) {
                fecha.IdInterno = solicitud.IdInterno;
                fecha.IdSerieFondo = solicitud.IdSerieFondo;
                fecha.FechaEmision = d.fechaSistema;
                fecha.EstadoFondo = Constantes3B.ABIERTO;
                fecha.EstadoLiquidacion = Constantes3B.ABIERTO;
                if (Constantes3B.NO.equals(respetaValutas)) {
                    fecha.NumeroDiasRetencion = 0L;
                    fecha.NumeroValutaValorCuota = 0L;
                    fecha.NumeroValutaCertificacion = 0L;
                    fecha.NumeroValutaCurse = 0L;
                    fecha.SolicitudBatch = Constantes3B.SI;
                } else {
                    fecha.NumeroDiasRetencion = serie.DiasRetencionesRescate;
                    fecha.NumeroValutaValorCuota = serie.DiasValutaValorCuotaRescate;
                    fecha.NumeroValutaCertificacion = serie.DiasValutaCertificacionRescate;
                    fecha.NumeroValutaCurse = serie.DiasValutaCurseRescate;
                    fecha.SolicitudBatch = Constantes3B.NO;
                }
                fecha.TipoSolicitud = Constantes3B.TIPO_SOLICITUD_RESCATE;
                fecha.FondoExtranjero = serie.FondoExtranjero;
                fecha.IdPaisFondo = serie.IdPaisOrigen;
                fecha.IdPaisEmisor = serie.IdPaisEmisor;
                Moneda moneda = d.hmonedas.get(serie.IdMoneda);
                fecha.IdPaisMoneda = moneda != null ? moneda.IdPais : 0L;
                entradaFechas.p_SolicitudAporteRescate.add(fecha);
            }
        }
        FechaContrato.Salida salidaFechas = fechas.calcular(entradaFechas);
        d.hfechas = Hashes.aHash(salidaFechas.P_FECHASAPORTERESCATE, f -> f.IdInterno);

        CuotaContrato.Entrada entradaCalculo = new CuotaContrato.Entrada();
        entradaCalculo.p_IdEmpresa = entrada.p_IdEmpresa;
        for (InSolicitudesRescate solicitud : entrada.p_SolicitudesRescate) {
            CuotaContrato.Serie serie = new CuotaContrato.Serie();
            SerieFondoSolicitudes serieFondo = d.hseries.get(solicitud.IdSerieFondo);
            FechaContrato.ResultadoFecha fecha = d.hfechas.get(solicitud.IdInterno);
            serie.IdInterno = solicitud.IdInterno;
            if (solicitud.FechaValorCuota != null) {
                serie.Fecha = ComisionNnArmado.texto(solicitud.FechaValorCuota);
            } else if (fecha != null) {
                serie.Fecha = ComisionNnArmado.texto(fecha.FechaAceptacion);
            } else {
                serie.Fecha = d.fechaSistema;
            }
            serie.IdSerieFondo = solicitud.IdSerieFondo;
            serie.CalculoPorMontoCuota = Constantes3B.CALCULO_POR_CUOTA;
            serie.Monto = solicitud.Monto;
            serie.NumeroCuotas = solicitud.NumeroCuotas;
            serie.IdMonedaFondo = serieFondo != null ? serieFondo.IdMoneda : "";
            serie.IdMonedaInversion = solicitud.IdMoneda;
            serie.TipoSolicitud = Constantes3B.TIPO_SOLICITUD_RESCATE;
            serie.TipoFactorAjuste = Constantes3B.UF;
            serie.IdEmpresa = entrada.p_IdEmpresa;
            entradaCalculo.p_Series.add(serie);
        }
        d.salidaCuotas = cuotas.calcular(entradaCalculo);
    }

    /** Legacy: {@code Utils.List2Hash(salidaCalculo.P_OUT_RESULTADO, "IdInterno")}, done after the sequences were requested. */
    static void hashCuotas(Datos d) throws Exception {
        d.valoresCuota = Hashes.aHash(d.salidaCuotas.P_OUT_RESULTADO, r -> r.IdInterno);
    }
}
