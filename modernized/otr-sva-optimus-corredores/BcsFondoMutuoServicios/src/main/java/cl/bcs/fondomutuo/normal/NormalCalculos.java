package cl.bcs.fondomutuo.normal;

import java.math.BigDecimal;

import cl.bcs.fondomutuo.cierre.FechaLegacy;
import cl.bcs.fondomutuo.cierre.SerieFondoSolicitudes;
import cl.bcs.fondomutuo.cierre.contrato.ParidadContrato;
import cl.bcs.fondomutuo.comisiones.CalcularComisiones;
import cl.bcs.fondomutuo.comisiones.CalcularComisiones.EntradaCalcularComisiones;
import cl.bcs.fondomutuo.comisiones.CalcularComisiones.SalidaCalcularComisiones;
import cl.bcs.fondomutuo.cuotas.CalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.fechas.CalcularFechaSolicitudAporteRescate;
import cl.bcs.fondomutuo.fechas.CalcularFechaSolicitudAporteRescate.ResultadoFecha;
import cl.bcs.fondomutuo.normal.NormalContrato.InSolicitud;
import cl.bcs.fondomutuo.normal.NormalDatos.TraspasoAPV;
import cl.bcs.fondomutuo.port.out.http.ParidadPort;
import cl.bcs.fondomutuo.requisitos.Hashes;
import cl.bcs.fondomutuo.requisitos.SecuenciaPort;

/** Dates, quotas, payment-currency parity and commissions of {@code CrearSolicitudAporteRescateNormal} (legacy :533-716), with the 3A services. */
final class NormalCalculos {

    private final CalcularFechaSolicitudAporteRescate fechas;
    private final CalcularMontoNumeroCuota cuotas;
    private final CalcularComisiones comisiones;
    private final ParidadPort paridad;
    private final SecuenciaPort secuencias;

    NormalCalculos(CalcularFechaSolicitudAporteRescate fechas, CalcularMontoNumeroCuota cuotas, CalcularComisiones comisiones, ParidadPort paridad,
            SecuenciaPort secuencias) {
        this.fechas = fechas;
        this.cuotas = cuotas;
        this.comisiones = comisiones;
        this.paridad = paridad;
        this.secuencias = secuencias;
    }

    /** Legacy {@code Fecha.stringToSqlDate(Date)} on a 3A date: a null date is a NullPointerException. */
    static String texto(java.time.LocalDate fecha) {
        return FechaLegacy.texto(java.sql.Date.valueOf(fecha));
    }

    static java.sql.Date sql(java.time.LocalDate fecha) {
        return fecha != null ? java.sql.Date.valueOf(fecha) : null;
    }

    void calcular(NormalContexto c) throws Exception {
        calcularFechas(c);
        String fechaValor = null;
        CalcularMontoNumeroCuota.EntradaCalcularMontoNumeroCuota entradaCalculo = new CalcularMontoNumeroCuota.EntradaCalcularMontoNumeroCuota();
        entradaCalculo.p_IdEmpresa = c.entrada.p_IdEmpresa;
        for (InSolicitud solicitud : c.entrada.p_SolicitudesAporteRescate) {
            CalcularMontoNumeroCuota.Serie serie = new CalcularMontoNumeroCuota.Serie();
            SerieFondoSolicitudes serieFondo = c.hseries.get(solicitud.IdSerieFondo);
            ResultadoFecha fecha = c.hfechas.get(solicitud.IdInterno);
            serie.IdInterno = solicitud.IdInterno;
            serie.Fecha = fechaDe(c, solicitud, fecha);
            serie.IdSerieFondo = solicitud.IdSerieFondo;
            serie.CalculoPorMontoCuota = solicitud.CalculoPorMontoCuota;
            serie.Monto = solicitud.Monto;
            serie.NumeroCuotas = solicitud.NumeroCuotas;
            serie.IdMonedaFondo = serieFondo != null ? serieFondo.IdMoneda : "";
            serie.IdMonedaInversion = solicitud.IdMoneda;
            serie.TipoSolicitud = solicitud.TipoSolicitud;
            serie.TipoFactorAjuste = NormalConstantes.UF;
            serie.IdEmpresa = c.entrada.p_IdEmpresa;
            entradaCalculo.p_Series.add(serie);
        }
        CalcularMontoNumeroCuota.SalidaCalcularMontoNumeroCuota salidaCalculo = cuotas.logica(entradaCalculo);
        c.secuencias = secuencias.siguientes(NormalConstantes.SECUENCIA_SOLICITUD, Long.valueOf(c.entrada.p_SolicitudesAporteRescate.size()));
        c.valoresCuota = Hashes.aHash(salidaCalculo.P_OUT_RESULTADO, r -> r.IdInterno);
        calcularParidad(c);
        calcularComisiones(c);
    }

    /** The date of the quota value: the one of the request, else the acceptance date, else the system date (legacy :626-632 and :661-667). */
    private static String fechaDe(NormalContexto c, InSolicitud solicitud, ResultadoFecha fecha) {
        if (solicitud.FechaValorCuota != null) {
            return FechaLegacy.texto(solicitud.FechaValorCuota);
        } else if (fecha != null) {
            return texto(fecha.FechaAceptacion);
        }
        return c.fechaSistema;
    }

    private void calcularFechas(NormalContexto c) throws Exception {
        CalcularFechaSolicitudAporteRescate.EntradaCalcularFechaSolicitudAporteRescate entradaFechas =
                new CalcularFechaSolicitudAporteRescate.EntradaCalcularFechaSolicitudAporteRescate();
        entradaFechas.p_IdEmpresa = c.entrada.p_IdEmpresa;
        entradaFechas.p_IdPais = c.idPaisEmpresa;
        for (InSolicitud solicitud : c.entrada.p_SolicitudesAporteRescate) {
            if (solicitud.FechaOperacion == null && "APV".equalsIgnoreCase(c.entrada.p_IdModulo)) {
                solicitud.FechaOperacion = c.fechaSistema;
            }
            SerieFondoSolicitudes serie = c.hseries.get(solicitud.IdSerieFondo);
            if (serie != null) {
                entradaFechas.p_SolicitudAporteRescate.add(NormalFechas.armar(c, solicitud, serie));
            }
        }
        c.hfechas = Hashes.aHash(fechas.logica(entradaFechas).P_FECHASAPORTERESCATE, f -> f.IdInterno);
    }

    private void calcularParidad(NormalContexto c) throws Exception {
        ParidadContrato.CalcularParidadEntrada entrada = new ParidadContrato.CalcularParidadEntrada();
        entrada.p_IdEmpresa = c.entrada.p_IdEmpresa;
        entrada.p_IdTrabajo = 1L;
        for (InSolicitud solicitud : c.entrada.p_SolicitudesAporteRescate) {
            ResultadoFecha fecha = c.hfechas.get(solicitud.IdInterno);
            CalcularMontoNumeroCuota.Resultado vc = c.valoresCuota.get(solicitud.IdInterno);
            ParidadContrato.CalculoEntrada calculoEntrada = new ParidadContrato.CalculoEntrada();
            calculoEntrada.IdTrabajo = 1L;
            calculoEntrada.IdInterno = solicitud.IdInterno;
            calculoEntrada.Fecha = fechaDe(c, solicitud, fecha);
            calculoEntrada.IdMonedaOrigen = solicitud.IdMoneda;
            calculoEntrada.IdMonedaDestino = solicitud.IdMonedaPago;
            calculoEntrada.ValorCambioLocalModificado = BigDecimal.ZERO;
            calculoEntrada.MontoOrigen = vc != null ? vc.MontoDestino : solicitud.Monto;
            calculoEntrada.IdUso = "";
            entrada.p_CalculosEntrada.add(calculoEntrada);
        }
        c.hashMontoMonedaPago = Hashes.aHash(ParidadContrato.calcular(paridad, entrada).p_out_CalculosSalida, s -> s.IdInterno);
    }

    private void calcularComisiones(NormalContexto c) throws Exception {
        EntradaCalcularComisiones entrada = new EntradaCalcularComisiones();
        entrada.p_IdEmpresa = c.entrada.p_IdEmpresa;
        entrada.p_IdUsuario = c.entrada.p_IdUsuario;
        entrada.p_IdModulo = c.entrada.p_IdModulo;
        entrada.p_SimularOperacion = c.entrada.p_SimularOperacion;
        entrada.p_UsaReglasNegocio = c.entrada.p_UsaReglasNegocio;
        for (InSolicitud solicitud : c.entrada.p_SolicitudesAporteRescate) {
            TraspasoAPV traspaso = c.hTraspasoAPV.get(solicitud.FolioContrato);
            CalcularComisiones.InCustodia inCustodia = new CalcularComisiones.InCustodia();
            SerieFondoSolicitudes serieFondo = c.hseries.get(solicitud.IdSerieFondo);
            CalcularMontoNumeroCuota.Resultado vc = c.valoresCuota.get(solicitud.IdInterno);
            ResultadoFecha fechas = c.hfechas.get(solicitud.IdInterno);
            inCustodia.IdInterno = solicitud.IdInterno;
            inCustodia.IdSerieFondo = solicitud.IdSerieFondo;
            inCustodia.IdInstrumento = serieFondo != null ? serieFondo.IdInstrumento : 0L;
            inCustodia.IdCliente = solicitud.IdCliente;
            inCustodia.IdPortafolio = solicitud.IdPortafolio;
            inCustodia.Cantidad = vc != null ? vc.NumeroCuotas : BigDecimal.ZERO;
            inCustodia.IdMonedaFondo = serieFondo != null ? serieFondo.IdMoneda : "";
            inCustodia.IdMonedaOperacion = solicitud.IdMoneda;
            inCustodia.TipoSolicitud = solicitud.TipoSolicitud;
            inCustodia.IdTipoMovimientoFondoMutuo = NormalConstantes.TIPO_SOLICITUD_APORTE.equals(solicitud.TipoSolicitud)
                    ? NormalConstantes.APORTE_FONDO_MUTUO : NormalConstantes.RESCATE_FONDO_MUTUO;
            inCustodia.FechaValorCuota = solicitud.FechaValorCuota != null ? FechaLegacy.aTexto(solicitud.FechaValorCuota.toLocalDate())
                    : FechaLegacy.aTexto(fechas.FechaValorCuota);
            inCustodia.FechaCurse = FechaLegacy.aTexto(fechas.FechaAceptacion);
            inCustodia.EsTraspaso = traspaso != null ? NormalConstantes.SI : solicitud.EsTraspaso;
            entrada.p_DatosCustodia.add(inCustodia);
        }
        SalidaCalcularComisiones salida = new SalidaCalcularComisiones();
        if (NormalConstantes.SI.equalsIgnoreCase(c.calcularComisionCertificacion)) {
            salida = comisiones.logica(entrada);
        }
        c.hcomisiones = Hashes.aHash(salida.P_OUT_RESULTADO, r -> r.IdInterno);
    }
}
