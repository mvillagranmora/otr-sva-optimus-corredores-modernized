package cl.bcs.fondomutuo.certificacion;

import static cl.bcs.fondomutuo.certificacion.CerConstantes.APV_AN;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.APV_B;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.CALCULO_POR_TOTAL;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.CUENTA_INVERSION;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.DOLAR;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.ESTADO_ADVERTENCIA;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.ESTADO_RECHAZADO;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.NO;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.OK;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.PESOS;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.RESCATE_COMISION_NN;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.RESCATE_FONDO_MUTUO;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.SI;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateColaboradores.CalculoCustodiaEntrada;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateColaboradores.CalculoCustodiaSalida;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateColaboradores.CalculoCustodiaSolicitud;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateColaboradores.CalculoParidad;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateColaboradores.CustodiaSalida;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateColaboradores.MovimientoSalida;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateColaboradores.ParidadSalida;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateColaboradores.ResultadoCalculoCustodia;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.Entrada;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.SolicitudProceso;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.Traspaso;
import cl.bcs.fondomutuo.cierre.FechaLegacy;
import cl.bcs.fondomutuo.cierre.FormatoMoneda;
import cl.bcs.fondomutuo.cierre.ListaHash;
import cl.bcs.fondomutuo.cierre.contrato.ParidadContrato.CalcularParidadEntrada;
import cl.bcs.fondomutuo.cierre.contrato.ParidadContrato.CalculoEntrada;
import cl.bcs.fondomutuo.comisiones.CalcularComisiones;
import cl.bcs.fondomutuo.comisiones.CalcularComisiones.EntradaCalcularComisiones;
import cl.bcs.fondomutuo.comisiones.CalcularComisiones.InCustodia;
import cl.bcs.fondomutuo.comisiones.CalcularComisiones.ResultadoCalcularComision;
import cl.bcs.fondomutuo.comisiones.CalcularComisiones.SalidaCalcularComisiones;
import cl.bcs.fondomutuo.port.out.http.ParidadPort;
import cl.bcs.fondomutuo.port.out.kit.DiccionarioMensajesPort;

/**
 * Front F-D (rescates: custody and costs) of legacy {@code CertificarSolicitudAporteRescate.logica}, bug for bug: the custody available to each rescate, rejection, advice and
 * total rescate, Spot, traspaso, retention, commission and format (FM-CER-31..38, 23), and, after the entry of the aportes and the cuenta de inversion of the aportes (front E),
 * the retirement from custody and the cost UF, spread and real profitability (FM-CER-39..41). Two steps the facade calls in the order of the legacy. Each does nothing when there
 * are no rescates, works on the {@link CertificarAporteRescateContexto} and its ports only (no write, no gate, no state) and lets any exception go up untouched.
 *
 * <p>The legacy ran several of the loops over {@code parallelStream()}; here they are sequential (the result does not depend on the order: every iteration touches its own
 * solicitud). A rescate rejected by an earlier rule is still visited by the later loops that do not filter it (D-F3B-11, D-F3B-38).
 */
public final class CertificarAporteRescateRescates {

    private final CalculaCustodiaRescatePort calculaCustodia;
    private final ProcesarCustodiaFondoMutuoPort custodia;
    private final CalcularComisiones comisiones;
    private final ParidadPort paridad;
    private final FormatoMoneda formato;
    private final DiccionarioMensajesPort diccionario;

    public CertificarAporteRescateRescates(CalculaCustodiaRescatePort calculaCustodia, ProcesarCustodiaFondoMutuoPort custodia, CalcularComisiones comisiones, ParidadPort paridad,
            FormatoMoneda formato, DiccionarioMensajesPort diccionario) {
        this.calculaCustodia = calculaCustodia;
        this.custodia = custodia;
        this.comisiones = comisiones;
        this.paridad = paridad;
        this.formato = formato;
        this.diccionario = diccionario;
    }

    // ---------------------------------------------------------------------------------------------------------------------------------------------------------
    // FM-CER-31..38 (+23)
    // ---------------------------------------------------------------------------------------------------------------------------------------------------------
    /** Legacy C:582-596. */
    public void prepararRescates(CertificarAporteRescateContexto ctx) throws Exception {
        List<SolicitudProceso> rescates = ctx.rescates;
        if (rescates.size() > 0) {
            Map<Long, ResultadoCalculoCustodia> hcalculaCustodiaRescate = calculaCustodiaRescate(ctx.entrada.p_IdEmpresa, rescates);
            cambiarMontosRescatesPorCantidadCustodia(ctx.entrada.p_IdEmpresa, rescates, hcalculaCustodiaRescate, ctx.rescateGeneraVentaSpotDolar);
            // the rescate has a related traspaso
            marcarTraspasos(rescates, ctx.traspasos);
            marcarRescatesConRetencion(rescates, ctx.porcentajeRetencion);

            // commission only when the parameter is exactly "S"
            if (SI.equals(ctx.calcularComisionCertificacion)) {
                Map<Long, ResultadoCalcularComision> hcomisiones = calcularComisiones(rescates, ctx.entrada);
                cambiarMontosPorComisiones(rescates, hcomisiones);
            }
            CertificarAporteRescateReglas.formatearMontos(formato, rescates);
            esMontoRetencionValido(rescates);
        }
    }

    private Map<Long, ResultadoCalculoCustodia> calculaCustodiaRescate(Long idEmpresa, List<SolicitudProceso> solicitudes) throws Exception {
        CalculoCustodiaEntrada entrada = new CalculoCustodiaEntrada();
        entrada.p_IdEmpresa = idEmpresa;
        for (SolicitudProceso sol : solicitudes) {
            CalculoCustodiaSolicitud solCus = new CalculoCustodiaSolicitud();
            solCus.IdSolicitudAporteRescate = sol.IdSolicitudAporteRescate;
            solCus.IdSerieFondo = sol.IdSerieFondo;
            solCus.CalculoPorMontoCuota = sol.CalculoPorMontoCuota;
            solCus.IdMoneda = sol.IdMoneda;
            solCus.IdMonedaFondo = sol.IdMonedaFondo;
            solCus.Monto = sol.Monto;
            solCus.NumeroCuotas = sol.NumeroCuotas;
            solCus.ValorCuota = sol.ValorCuota;
            solCus.TipoSolicitud = sol.TipoSolicitud;
            solCus.IdInstrumento = sol.IdInstrumento;
            solCus.IdCliente = sol.IdCliente;
            solCus.IdPortafolio = sol.IdPortafolio;
            solCus.FechaValorCuota = sol.FechaValorCuota;
            solCus.IdRegimenTributario = sol.IdRegimenTributario;
            solCus.IdOmnibus = sol.IdOmnibus;
            entrada.p_Solicitudes.add(solCus);
        }
        CalculoCustodiaSalida salida = CertificarAporteRescateColaboradores.calcular(calculaCustodia, entrada);
        return ListaHash.aHash(salida.P_OUT_RESULTADO, r -> r.IdSolicitudAporteRescate);
    }

    private void cambiarMontosRescatesPorCantidadCustodia(Long idEmpresa, List<SolicitudProceso> rescates, Map<Long, ResultadoCalculoCustodia> hcalculaCustodiaRescate,
            String rescateGeneraVentaSpotDolar) {
        for (SolicitudProceso sol : rescates) {
            ResultadoCalculoCustodia custodiaCalculada = hcalculaCustodiaRescate.get(sol.IdSolicitudAporteRescate);
            HashMap<String, String> parametrosMensaje = new HashMap<String, String>();
            parametrosMensaje.put("DocumentoIdentidad", sol.DocumentoIdentidad);
            parametrosMensaje.put("CodigoPortafolio", sol.CodigoPortafolio);
            parametrosMensaje.put("Nemotecnico", sol.IdSerieFondo);
            if ((custodiaCalculada == null || ESTADO_RECHAZADO.equals(custodiaCalculada.Estado))
                    || (ESTADO_ADVERTENCIA.equals(custodiaCalculada.Estado) && esOmnibusProcesado(sol.IdOmnibus))) {
                sol.EstadoProceso = ESTADO_RECHAZADO;
                sol.Codigo = "UI_ERROR_FM_CLIENTE_SIN_CUSTODIA_CERTIFICACION";
                parametrosMensaje.put("CantidadRetiro", sol.NumeroCuotas.toPlainString());
                // D-F3B-05: without a result (custodiaCalculada null) or without CantidadDisponible this is a NullPointerException that aborts the whole lot
                parametrosMensaje.put("CantidadCustodia", custodiaCalculada.CantidadDisponible.toPlainString());
                sol.DescripcionCodigo = obtenerDescripcionMensajeSalida(idEmpresa, sol.Codigo, parametrosMensaje);
            } else {
                if (ESTADO_ADVERTENCIA.equals(custodiaCalculada.Estado)) {
                    sol.EstadoProceso = ESTADO_ADVERTENCIA;
                    sol.Codigo = "UI_ERROR_FM_SALDO_CUSTODIA_INSUFICIENTE_CERTIFICACION";
                    parametrosMensaje.put("CantidadRetiroInicial", sol.NumeroCuotas.toPlainString());
                    parametrosMensaje.put("CantidadRetiroFinal", custodiaCalculada.NumeroCuotas.toPlainString());
                    parametrosMensaje.put("CantidadCustodia", custodiaCalculada.CantidadDisponible.toPlainString());
                    sol.DescripcionCodigo = obtenerDescripcionMensajeSalida(idEmpresa, sol.Codigo, parametrosMensaje);
                }
                // D-F3B-39: any state other than RZD / ADV is taken as approved
                if (ESTADO_ADVERTENCIA.equals(custodiaCalculada.Estado) || (CALCULO_POR_TOTAL.equals(sol.CalculoPorMontoCuota) && esOmnibusPorProcesar(sol.IdOmnibus))) {
                    sol.CostoMonedaLocal = custodiaCalculada.MontoDestino;
                    sol.Monto = custodiaCalculada.Monto;
                    sol.ValorCuota = custodiaCalculada.ValorCuota;
                    sol.NumeroCuotas = custodiaCalculada.NumeroCuotas;
                    sol.ParidadFechaCertificacion = custodiaCalculada.ValorParidad;
                    sol.CostoUF = custodiaCalculada.CostoEnFactorAjuste;
                    sol.ValorUFFechaInversion = custodiaCalculada.ValorFactorAjuste;
                    sol.MontoMonedaSistema = custodiaCalculada.MontoMonedaSistema;
                    sol.MontoPago = custodiaCalculada.Monto;
                    sol.MontoPagoLocal = custodiaCalculada.MontoDestino;
                }

                // GeneraSpot is decided only here: a rescate rejected above keeps it NULL
                if (SI.equalsIgnoreCase(rescateGeneraVentaSpotDolar) && (RESCATE_COMISION_NN.equals(sol.IdTipoMovimientoFondoMutuo) || DOLAR.equalsIgnoreCase(sol.IdMoneda))
                        && PESOS.equalsIgnoreCase(sol.IdMonedaPago)) {
                    sol.GeneraSpot = SI;
                } else {
                    sol.GeneraSpot = NO;
                }
            }
        }
    }

    private static boolean esOmnibusProcesado(Long idOmnibus) {
        return idOmnibus != null && idOmnibus > 0;
    }

    private static boolean esOmnibusPorProcesar(Long idOmnibus) {
        return idOmnibus == null || idOmnibus.equals(0L);
    }

    private String obtenerDescripcionMensajeSalida(Long idEmpresa, String codigo, HashMap<String, String> parametrosMensaje) {
        String descripcion = "";
        try {
            descripcion = diccionario.mensaje(idEmpresa, codigo, parametrosMensaje);
        } catch (Exception e) {
            // legacy: the text stays empty
        }
        return descripcion;
    }

    /** D-F3B-38: ALL the rescates of the list, also the ones rejected by custody, are visited. */
    private static void marcarTraspasos(List<SolicitudProceso> rescates, Map<Long, Traspaso> hashTraspasos) {
        for (SolicitudProceso sol : rescates) {
            if (hashTraspasos.get(sol.IdSolicitudAporteRescate) != null || hashTraspasos.get(sol.IdSolicitudAporteRescateRelacionado) != null) {
                sol.RelacionTraspaso = SI;
            }
        }
    }

    /** D-F3B-09: the retention is over the GROSS amount (before the commission) with a plain {@code divide(100)}; D-F3B-38 for the rejected ones. */
    private static void marcarRescatesConRetencion(List<SolicitudProceso> rescates, Long porcentajeRetencion) {
        for (SolicitudProceso sol : rescates) {
            if (RESCATE_FONDO_MUTUO.equals(sol.IdTipoMovimientoFondoMutuo) && (APV_AN.equals(sol.IdRegimenTributarioAPV) || APV_B.equals(sol.IdRegimenTributarioAPV))
                    && !CUENTA_INVERSION.equals(sol.IdMedioDePago) && !SI.equals(sol.RelacionTraspaso) && !SI.equals(sol.GeneraSpot) && porcentajeRetencion.compareTo(0L) != 0) {
                sol.TieneMontoRetencion = SI;
                sol.MontoRetencion = sol.Monto.multiply(BigDecimal.valueOf(porcentajeRetencion)).divide(BigDecimal.valueOf(100));
                sol.MontoRetencionLocal = sol.CostoMonedaLocal.multiply(BigDecimal.valueOf(porcentajeRetencion)).divide(BigDecimal.valueOf(100));
            }
        }
    }

    private Map<Long, ResultadoCalcularComision> calcularComisiones(List<SolicitudProceso> solicitudes, Entrada entrada) throws Exception {
        Map<Long, ResultadoCalcularComision> hcomisiones = new HashMap<Long, ResultadoCalcularComision>();
        EntradaCalcularComisiones entradaComisiones = new EntradaCalcularComisiones();
        entradaComisiones.p_IdEmpresa = entrada.p_IdEmpresa;
        entradaComisiones.p_IdUsuario = entrada.p_IdUsuario;
        entradaComisiones.p_IdModulo = entrada.p_IdModulo;
        entradaComisiones.p_SimularOperacion = NO;
        entradaComisiones.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
        entradaComisiones.p_DatosCustodia = solicitudes.stream().filter(solicitud -> !ESTADO_RECHAZADO.equals(solicitud.EstadoProceso)).map(solicitud -> {
            InCustodia inCustodia = new InCustodia();
            inCustodia.IdInterno = solicitud.IdSolicitudAporteRescate;
            inCustodia.IdSerieFondo = solicitud.IdSerieFondo;
            inCustodia.IdInstrumento = solicitud.IdInstrumento;
            inCustodia.IdCliente = solicitud.IdCliente;
            inCustodia.IdPortafolio = solicitud.IdPortafolio;
            inCustodia.Cantidad = solicitud.NumeroCuotas;
            inCustodia.IdMonedaFondo = solicitud.IdMonedaFondo;
            inCustodia.IdMonedaOperacion = solicitud.IdMoneda;
            inCustodia.TipoSolicitud = solicitud.TipoSolicitud;
            inCustodia.IdTipoMovimientoFondoMutuo = solicitud.IdTipoMovimientoFondoMutuo;
            inCustodia.FechaValorCuota = textoFecha(solicitud.FechaValorCuota);
            inCustodia.FechaCurse = textoFecha(solicitud.FechaAceptacion);
            inCustodia.EsTraspaso = solicitud.RelacionTraspaso;
            return inCustodia;
        }).collect(Collectors.toList());

        if (entradaComisiones.p_DatosCustodia.size() > 0) {
            SalidaCalcularComisiones salida = comisiones.logica(entradaComisiones);
            hcomisiones = ListaHash.aHash(salida.P_OUT_RESULTADO, r -> r.IdInterno);
        }
        return hcomisiones;
    }

    /** Legacy {@code Fecha.toStringDate(java.sql.Date)}: {@code yyyy-MM-dd}, the empty text for null. */
    private static String textoFecha(java.sql.Date fecha) {
        return FechaLegacy.aTexto(fecha == null ? null : fecha.toLocalDate());
    }

    private static void cambiarMontosPorComisiones(List<SolicitudProceso> rescates, Map<Long, ResultadoCalcularComision> hcomisiones) {
        for (SolicitudProceso solicitud : rescates) {
            ResultadoCalcularComision comision = hcomisiones.get(solicitud.IdSolicitudAporteRescate);
            // the commission is calculated also when a related NN commission exists, but it is applied only when there is none (Q-T2-7)
            if (comision != null && NO.equals(solicitud.TieneComisionNNRelacionada)) {
                solicitud.MontoComision = comision.Comision;
                solicitud.MontoComisionLocal = comision.ComisionLocal;
                solicitud.MontoPagoLocal = solicitud.CostoMonedaLocal.subtract(solicitud.MontoComisionLocal, MathContext.DECIMAL128);
                solicitud.MontoPago = solicitud.Monto.subtract(solicitud.MontoComision, MathContext.DECIMAL128);
            }
        }
    }

    private static void esMontoRetencionValido(List<SolicitudProceso> rescates) {
        for (SolicitudProceso rescate : rescates) {
            if (SI.equals(rescate.TieneMontoRetencion) && rescate.MontoRetencion.compareTo(BigDecimal.ZERO) == 0) {
                rescate.TieneMontoRetencion = NO;
            }
        }
    }

    // ---------------------------------------------------------------------------------------------------------------------------------------------------------
    // FM-CER-39..41
    // ---------------------------------------------------------------------------------------------------------------------------------------------------------
    /** Legacy C:644-676 up to the cost UF (the cuenta de inversion of the rescates, front E, runs after this step). */
    public void retirarCustodiaYCostear(CertificarAporteRescateContexto ctx) throws Exception {
        List<SolicitudProceso> rescates = ctx.rescates;
        if (rescates.size() > 0) {
            List<SolicitudProceso> rescatesConRegimen = rescates.stream().filter(rescate -> !ESTADO_RECHAZADO.equals(rescate.EstadoProceso) && !rescate.IdRegimenTributario.isEmpty())
                    .collect(Collectors.toList());
            List<SolicitudProceso> rescatesSinRegimen = rescates.stream().filter(rescate -> !ESTADO_RECHAZADO.equals(rescate.EstadoProceso) && rescate.IdRegimenTributario.isEmpty())
                    .collect(Collectors.toList());
            CustodiaSalida salidaConRegimen = CertificarAporteRescateCustodia.ingresoRetiro(custodia, rescatesConRegimen, ctx.entrada);
            ctx.salida.P_OUT_ERRORESCONTADOR += salidaConRegimen.P_OUT_ERRORESCONTADOR;

            CustodiaSalida salidaSinRegimen = CertificarAporteRescateCustodia.ingresoRetiro(custodia, rescatesSinRegimen, ctx.entrada);
            ctx.salida.P_OUT_ERRORESCONTADOR += salidaSinRegimen.P_OUT_ERRORESCONTADOR;

            List<cl.bcs.fondomutuo.kit.MensajeSalida> mensajes = new ArrayList<>();
            mensajes.addAll(salidaConRegimen.P_OUT_MENSAJES);
            mensajes.addAll(salidaSinRegimen.P_OUT_MENSAJES);
            CertificarAporteRescateCustodia.rechazarPorMensajes(ctx.salida, mensajes, rescates);

            List<MovimientoSalida> carterasRetiradas = new ArrayList<>();
            carterasRetiradas.addAll(salidaConRegimen.P_OUT_MOVIMIENTOS);
            carterasRetiradas.addAll(salidaSinRegimen.P_OUT_MOVIMIENTOS);

            ctx.carterasRetiradas = carterasRetiradas;   // the reinversion (F-I) reads them

            validarRetiroCarteras(rescates, carterasRetiradas);
            calcularCostoUFRentabilidadRescates(rescates, carterasRetiradas, ctx.entrada.p_IdEmpresa);
        }
    }

    /** A rescate that is not rejected and has no movement of its id in the retirements is rejected. */
    private static void validarRetiroCarteras(List<SolicitudProceso> rescates, List<MovimientoSalida> carterasRetiradas) {
        for (SolicitudProceso rescate : rescates) {
            if (ESTADO_RECHAZADO.equals(rescate.EstadoProceso)) continue;
            int cantidadCarterasRetiradas = carterasRetiradas.stream().filter(cartera -> cartera.IdGenerarMovimientoCustodia.equals(rescate.IdSolicitudAporteRescate))
                    .collect(Collectors.toList()).size();
            if (cantidadCarterasRetiradas == 0) {
                rescate.EstadoProceso = ESTADO_RECHAZADO;
                rescate.Codigo = "UI_ERROR_RETIRO_CUSTODIA_CERTIFICACION_FONDO_MUTUO";
            }
        }
    }

    /**
     * D-F3B-11: cost UF, spread and real profitability of EVERY rescate of the list, also the rejected ones (a null {@code CostoUF} or {@code MontoMonedaSistema} aborts the lot).
     * The UTM entry is ONE object added once per distinct date (all the PEN / ADV solicitudes share the date, so in practice one entry).
     */
    private void calcularCostoUFRentabilidadRescates(List<SolicitudProceso> rescates, List<MovimientoSalida> carterasRetiradas, Long idEmpresa) throws Exception {
        List<java.sql.Date> fechaCertificaciones = rescates.stream().map(solicitud -> solicitud.FechaCertificacion).distinct().collect(Collectors.toList());
        Map<Long, List<MovimientoSalida>> movimientosAgrupadosPorSolicitud = carterasRetiradas.stream().collect(Collectors.groupingBy(solicitud -> solicitud.IdGenerarMovimientoCustodia));
        HashMap<java.sql.Date, Long> mapeoIdInternoFecha = new HashMap<java.sql.Date, Long>();

        CalcularParidadEntrada calcularParidadEntrada = new CalcularParidadEntrada();
        calcularParidadEntrada.p_IdEmpresa = idEmpresa;
        calcularParidadEntrada.p_IdTrabajo = 1L;

        CalculoEntrada calculoEntradaUTM = new CalculoEntrada();
        Long idInterno = 1L;

        for (java.sql.Date fechaCertificacion : fechaCertificaciones) {
            calculoEntradaUTM.IdTrabajo = 1L;
            calculoEntradaUTM.IdInterno = idInterno;
            calculoEntradaUTM.Fecha = FechaLegacy.texto(fechaCertificacion);
            calculoEntradaUTM.IdMonedaOrigen = "UTM";
            calculoEntradaUTM.IdMonedaDestino = "CLP";
            calculoEntradaUTM.ValorCambioLocalModificado = BigDecimal.ZERO;
            calculoEntradaUTM.MontoOrigen = BigDecimal.ONE;
            calculoEntradaUTM.IdUso = "";
            calcularParidadEntrada.p_CalculosEntrada.add(calculoEntradaUTM);
            mapeoIdInternoFecha.put(fechaCertificacion, idInterno);
            idInterno++;
        }

        ParidadSalida calculoSalida = CertificarAporteRescateColaboradores.calcularParidad(paridad, calcularParidadEntrada);
        Map<Long, CalculoParidad> hCalculo = ListaHash.aHash(calculoSalida.p_out_CalculosSalida, c -> c.IdInterno);

        for (SolicitudProceso sol : rescates) {
            BigDecimal costoUF = BigDecimal.ZERO;
            BigDecimal rentabilidad = BigDecimal.ZERO;

            List<MovimientoSalida> movimientosCustodia = movimientosAgrupadosPorSolicitud.get(sol.IdSolicitudAporteRescate);
            if (movimientosCustodia != null) {
                for (MovimientoSalida movimientoCustodia : movimientosCustodia) {
                    costoUF = costoUF.add(movimientoCustodia.CostoInversionEnFactorAjusteARetirar, MathContext.DECIMAL128);
                    rentabilidad = rentabilidad.add(movimientoCustodia.MontoCompraFactorARetirar, MathContext.DECIMAL128);
                }
            }

            Long id = mapeoIdInternoFecha.get(sol.FechaCertificacion);
            CalculoParidad calculoValorUTM = hCalculo.get(id);
            BigDecimal valorUTM = calculoValorUTM != null && OK.equals(calculoValorUTM.Resultado) ? calculoValorUTM.MontoDestino : BigDecimal.ONE;
            sol.RentabilidadReal = sol.MontoMonedaSistema.divide(valorUTM, MathContext.DECIMAL128).subtract(rentabilidad, MathContext.DECIMAL128);
            sol.Spread = sol.CostoUF.subtract(costoUF, MathContext.DECIMAL128);
            sol.CostoUF = costoUF;
        }
    }
}
