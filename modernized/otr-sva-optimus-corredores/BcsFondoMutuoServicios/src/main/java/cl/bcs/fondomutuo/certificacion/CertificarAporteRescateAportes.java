package cl.bcs.fondomutuo.certificacion;

import static cl.bcs.fondomutuo.certificacion.CerConstantes.APORTE_REINVERSION;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.ESTADO_RECHAZADO;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.NO;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.ORIGEN_MANUAL;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.REGIMEN_18Q;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.SI;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.TIPO_SOLICITUD_RESCATE;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.USO_FLUJO_CAJA_CERTIFICACION;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.USO_FLUJO_CAJA_CERTIFICACION_PAC;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateColaboradores.CustodiaSalida;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateColaboradores.FlujoCaja;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateColaboradores.FlujoCajaEntrada;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateColaboradores.FlujoCajaSalida;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateColaboradores.MovimientoSalida;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateColaboradores.ParidadSalida;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateColaboradores.PortafolioAConsultar;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.IndicadorPortafolio;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.SolicitudProceso;
import cl.bcs.fondomutuo.cierre.FechaLegacy;
import cl.bcs.fondomutuo.cierre.FormatoMoneda;
import cl.bcs.fondomutuo.cierre.contrato.ParidadContrato.CalcularParidadEntrada;
import cl.bcs.fondomutuo.cierre.contrato.ParidadContrato.CalculoEntrada;
import cl.bcs.fondomutuo.port.out.http.FlujoCajaPort;
import cl.bcs.fondomutuo.port.out.http.ParidadPort;

/**
 * Front F-C (aportes) of legacy {@code CertificarSolicitudAporteRescate.logica}, bug for bug: the balance of the cuenta de inversion (FM-CER-21, 22, and the format of the
 * amounts, FM-CER-23), the cost UF of the reinversion aportes (FM-CER-24..26) and the entry of the aportes to custody (FM-CER-27, 28). Three steps the facade calls in the
 * order of the legacy (the cuenta de inversion of the aportes, front E, runs after {@link #ingresarCustodia}). Each step does nothing when there are no aportes, works on the
 * {@link CertificarAporteRescateContexto} (the legacy mutates the same objects in place) and its ports only: no write, no gate (the host evaluates G3 / G-CUS before the first
 * write), no state. Exceptions go up untouched; only {@link #recalcularCostoUF} catches, as the legacy does (D-NOR-11).
 */
public final class CertificarAporteRescateAportes {

    private final FlujoCajaPort flujoCaja;
    private final ParidadPort paridad;
    private final ProcesarCustodiaFondoMutuoPort custodia;
    private final CertificarAporteRescateDatos datos;
    private final FormatoMoneda formato;

    public CertificarAporteRescateAportes(FlujoCajaPort flujoCaja, ParidadPort paridad, ProcesarCustodiaFondoMutuoPort custodia, CertificarAporteRescateDatos datos,
            FormatoMoneda formato) {
        this.flujoCaja = flujoCaja;
        this.paridad = paridad;
        this.custodia = custodia;
        this.datos = datos;
        this.formato = formato;
    }

    // ---------------------------------------------------------------------------------------------------------------------------------------------------------
    // FM-CER-21, 22, 23: balance of the cuenta de inversion
    // ---------------------------------------------------------------------------------------------------------------------------------------------------------
    /** Legacy {@code validarSaldoCuentaInversion}, {@code validarSaldoCuentaInversionPAC} and {@code formatearMontos(aportes)}: first the aportes that are not PAC, then the PAC ones, then the format. */
    public void validarSaldo(CertificarAporteRescateContexto ctx) throws Exception {
        if (ctx.aportes.size() > 0) {
            validarSaldoCuentaInversion(ctx, false);
            validarSaldoCuentaInversion(ctx, true);
            CertificarAporteRescateReglas.formatearMontos(formato, ctx.aportes);
        }
    }

    /** The two legacy methods are the same code with the channel filter reversed and another {@code P_USO}: separate totals and separate calls (PAC and not PAC are never added together). */
    private void validarSaldoCuentaInversion(CertificarAporteRescateContexto ctx, boolean pac) {
        List<SolicitudProceso> solicitudes = ctx.aportes;
        List<Long> portFlujo = new ArrayList<Long>();
        List<String> monedaFlujo = new ArrayList<String>();
        HashMap<String, BigDecimal> portTotalesMontos = new HashMap<String, BigDecimal>();
        FlujoCajaEntrada entrada = new FlujoCajaEntrada();
        entrada.P_IDEMPRESA = ctx.entrada.p_IdEmpresa;
        entrada.P_FECHADESDE = FechaLegacy.texto(ctx.fechaCertificacion);
        entrada.P_FECHAHASTA = FechaLegacy.texto(ctx.fechaCertificacion);
        entrada.P_USO = pac ? USO_FLUJO_CAJA_CERTIFICACION_PAC : USO_FLUJO_CAJA_CERTIFICACION;

        for (SolicitudProceso sol : solicitudes) {
            if (ESTADO_RECHAZADO.equals(sol.EstadoProceso)) continue;
            if (pac == "PAC".equals(sol.IdTipoCanalIngreso)) {
                if (mueveCuentaSinSobregiro(ctx, sol)) {
                    String id = sol.IdPortafolio.toString() + "_" + sol.IdMoneda;
                    BigDecimal montoAcum = portTotalesMontos.get(id) != null ? portTotalesMontos.get(id) : BigDecimal.ZERO;
                    portTotalesMontos.put(id, montoAcum.add(sol.CostoMonedaLocal, MathContext.DECIMAL128));
                    // D-NOR-16: the pair is omitted when the portfolio AND the currency were both seen before (two independent lists)
                    if (!portFlujo.contains(sol.IdPortafolio) || !monedaFlujo.contains(sol.IdMoneda)) {
                        PortafolioAConsultar saldo = new PortafolioAConsultar();
                        saldo.IdPortafolio = sol.IdPortafolio;
                        saldo.IdMoneda = sol.IdMoneda;
                        entrada.P_PORTAFOLIOACONSULTAR.add(saldo);
                        if (!portFlujo.contains(sol.IdPortafolio)) portFlujo.add(sol.IdPortafolio);
                        if (!monedaFlujo.contains(sol.IdMoneda)) monedaFlujo.add(sol.IdMoneda);
                    }
                }
            }
        }

        FlujoCajaSalida salida = new FlujoCajaSalida();
        if (entrada.P_PORTAFOLIOACONSULTAR.size() > 0) {
            salida = CertificarAporteRescateColaboradores.saldo(flujoCaja, entrada);
        }

        for (SolicitudProceso sol : solicitudes) {
            if (ESTADO_RECHAZADO.equals(sol.EstadoProceso)) continue;
            if (pac == "PAC".equals(sol.IdTipoCanalIngreso)) {
                if (mueveCuentaSinSobregiro(ctx, sol)) {
                    String id = sol.IdPortafolio.toString() + "_" + sol.IdMoneda;
                    BigDecimal saldo = BigDecimal.ZERO;
                    BigDecimal total = portTotalesMontos.get(id) != null ? portTotalesMontos.get(id) : BigDecimal.ZERO;
                    String fecha = sol.FechaCertificacion.compareTo(FechaLegacy.aFechaSql(ctx.fechaSistema)) < 0 ? ctx.fechaSistema : FechaLegacy.texto(sol.FechaCertificacion);
                    // the LAST row of the same portfolio, currency and date is the balance
                    for (FlujoCaja f : salida.P_OUT_FLUJOCAJA) {
                        if (f.IdPortafolio.longValue() == sol.IdPortafolio && f.IdMoneda.equals(sol.IdMoneda) && f.FechaSaldo.equals(fecha)) {
                            saldo = f.Saldo;
                        }
                    }
                    if (NO.equalsIgnoreCase(sol.OperaSaldoNegativo)) {
                        if (total.compareTo(saldo) == 1) {
                            // the total of the pair rejects EVERY aporte of the pair, not only the last
                            sol.EstadoProceso = ESTADO_RECHAZADO;
                            sol.Codigo = "UI_SALDO_INSUFICIENTE_FLUJO_CAJA";
                            continue;
                        }
                    }
                }
            }
        }
    }

    private static boolean mueveCuentaSinSobregiro(CertificarAporteRescateContexto ctx, SolicitudProceso sol) {
        IndicadorPortafolio ind = ctx.indicadoresPortafolio.get(sol.IdPortafolio);
        String sobregiro = ind != null ? ind.OperaLineaSobregiro : NO;
        return SI.equals(sol.MueveCta) && ORIGEN_MANUAL.equals(sol.OrigenOperacion) && NO.equals(sobregiro);
    }

    // ---------------------------------------------------------------------------------------------------------------------------------------------------------
    // FM-CER-24..26: cost UF of the reinversion aportes
    // ---------------------------------------------------------------------------------------------------------------------------------------------------------
    /**
     * Legacy {@code reCalcularCostoUFAportes}: the whole body is in {@code try/catch (Exception)} and nothing is reported (D-NOR-11; the legacy only printed the stack): a missing
     * rescate of origin (D-F3B-10: the rescate of origin is READ FROM THE DATABASE, {@code findFirst().get()}), a zero divisor or a failed parity leave this aporte and every
     * following one as they were. Never throws an {@link Exception}.
     */
    public void recalcularCostoUF(CertificarAporteRescateContexto ctx) {
        if (ctx.aportes.size() > 0) {
            reCalcularCostoUFAportes(ctx.aportes, ctx.entrada.p_IdEmpresa);
        }
    }

    private void reCalcularCostoUFAportes(List<SolicitudProceso> aportes, Long idEmpresa) {
        try {
            List<Long> idSolicitudes = aportes.stream().map(aporte -> aporte.IdSolicitudAporteRescateRelacionado).distinct().collect(Collectors.toList());

            List<SolicitudProceso> solicitudRescateOrigen = datos.solicitudes(idEmpresa, idSolicitudes);

            CalcularParidadEntrada calcularParidadEntrada = new CalcularParidadEntrada();
            calcularParidadEntrada.p_IdEmpresa = idEmpresa;
            calcularParidadEntrada.p_IdTrabajo = 1L;

            // D-F3B-10: ONE entry object, added once per reinversion aporte and rewritten each time: the call carries it as many times as it was added, with its last values
            CalculoEntrada calculoEntradaUF = new CalculoEntrada();
            Long idInterno = 1L;

            for (SolicitudProceso aporte : aportes) {
                if (APORTE_REINVERSION.equalsIgnoreCase(aporte.IdTipoMovimientoFondoMutuo)) {

                    calculoEntradaUF.IdTrabajo = 1L;
                    calculoEntradaUF.IdInterno = idInterno;
                    calculoEntradaUF.Fecha = FechaLegacy.texto(aporte.FechaValorCuota);
                    calculoEntradaUF.IdMonedaOrigen = "UF";
                    calculoEntradaUF.IdMonedaDestino = "CLP";
                    calculoEntradaUF.ValorCambioLocalModificado = BigDecimal.ZERO;
                    calculoEntradaUF.MontoOrigen = BigDecimal.ONE;
                    calculoEntradaUF.IdUso = "";
                    calcularParidadEntrada.p_CalculosEntrada.add(calculoEntradaUF);
                    idInterno++;

                    SolicitudProceso rescateOrigen = solicitudRescateOrigen.stream().filter(sol -> sol.IdSolicitudAporteRescate.equals(aporte.IdSolicitudAporteRescateRelacionado))
                            .findFirst().get();

                    if (rescateOrigen != null && rescateOrigen.ValorUFFechaInversion != null && rescateOrigen.CostoUF != null
                            && rescateOrigen.ValorUFFechaInversion.compareTo(BigDecimal.ZERO) > 0 && rescateOrigen.CostoUF.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal valorUFRescate = BigDecimal.ZERO;

                        // both the rescate and the reinversion aportes must be of regime 18Q to inherit the cost UF
                        boolean rescateEs18Q = REGIMEN_18Q.equalsIgnoreCase(rescateOrigen.IdRegimenTributario);
                        boolean todosSon18Q = REGIMEN_18Q.equalsIgnoreCase(aporte.IdRegimenTributario) && rescateEs18Q;

                        if (!aporte.IdMonedaSistema.equalsIgnoreCase(aporte.IdMoneda) && !rescateOrigen.IdMonedaSistema.equalsIgnoreCase(rescateOrigen.IdMoneda)
                                && rescateOrigen.IdMoneda.equalsIgnoreCase(aporte.IdMoneda)) {
                            List<BigDecimal> montosRelacionados = aportes.stream()
                                    .filter(solicitud -> !ESTADO_RECHAZADO.equals(solicitud.EstadoProceso) && APORTE_REINVERSION.equals(solicitud.IdTipoMovimientoFondoMutuo)
                                            && solicitud.IdSolicitudAporteRescateRelacionado > 0 && !solicitud.IdMonedaSistema.equals(solicitud.IdMoneda)
                                            && !SI.equals(solicitud.GeneraSpot))
                                    .map(solicitud -> solicitud.MontoMonedaSistema).distinct().collect(Collectors.toList());
                            Optional<BigDecimal> suma = montosRelacionados.stream().reduce(BigDecimal::add);

                            BigDecimal sumaTotalMontoMonedaSistema = suma.orElse(BigDecimal.ZERO);

                            valorUFRescate = sumaTotalMontoMonedaSistema.divide(rescateOrigen.CostoUF, MathContext.DECIMAL64);
                        } else {
                            valorUFRescate = rescateOrigen.MontoMonedaSistema.divide(rescateOrigen.CostoUF, MathContext.DECIMAL64);
                        }

                        if ("APV-A".equalsIgnoreCase(aporte.IdRegimenTributario) || "BF".equalsIgnoreCase(aporte.IdRegimenTributario)
                                || "DC-DL".equalsIgnoreCase(aporte.IdRegimenTributario) || "DC-AL".equalsIgnoreCase(aporte.IdRegimenTributario)
                                || "DC-R".equalsIgnoreCase(aporte.IdRegimenTributario)) {
                            aporte.CostoUF = aporte.MontoMonedaSistema.divide(valorUFRescate, MathContext.DECIMAL64);
                            aporte.ValorUFFechaInversion = rescateOrigen.ValorUFFechaInversion;

                        } else if ("S".equalsIgnoreCase(aporte.IndicadorMLT) && todosSon18Q) {
                            aporte.CostoUF = aporte.MontoMonedaSistema.divide(valorUFRescate, MathContext.DECIMAL64);
                            aporte.ValorUFFechaInversion = rescateOrigen.ValorUFFechaInversion;

                        } else {
                            ParidadSalida calculoSalida = CertificarAporteRescateColaboradores.calcularParidad(paridad, calcularParidadEntrada);
                            BigDecimal ufValorCuotaAporte = calculoSalida.p_out_CalculosSalida.stream().findAny().get().MontoLocal;
                            // the legacy compares by REFERENCE with BigDecimal.ZERO (never true for a parsed number): a zero divides and the error is swallowed; a null MontoLocal (D-F3B-40)
                            // falls to the other branch and leaves ValorUFFechaInversion null
                            aporte.CostoUF = ufValorCuotaAporte != null && ufValorCuotaAporte != BigDecimal.ZERO
                                    ? aporte.MontoMonedaSistema.divide(ufValorCuotaAporte, MathContext.DECIMAL64)
                                    : aporte.MontoMonedaSistema.divide(aporte.ValorUFFechaInversion, MathContext.DECIMAL64);
                            aporte.ValorUFFechaInversion = ufValorCuotaAporte;
                        }
                    }
                }
            }
        } catch (Exception e) {
            // legacy: e.printStackTrace() and nothing else (D-NOR-11, Q-T2-2): the aportes keep what they had
        }
    }

    // ---------------------------------------------------------------------------------------------------------------------------------------------------------
    // FM-CER-27, 28: custody of the aportes
    // ---------------------------------------------------------------------------------------------------------------------------------------------------------
    /** Legacy C:602-620: the entry of the aportes (one call), the rejections by the error messages and the real profitability and cartera. */
    public void ingresarCustodia(CertificarAporteRescateContexto ctx) throws Exception {
        if (ctx.aportes.size() > 0) {
            CustodiaSalida salidaCustodia = CertificarAporteRescateCustodia.ingresoRetiro(custodia, ctx.aportes, ctx.entrada);
            ctx.salida.P_OUT_ERRORESCONTADOR += salidaCustodia.P_OUT_ERRORESCONTADOR;
            CertificarAporteRescateCustodia.rechazarPorMensajes(ctx.salida, salidaCustodia.P_OUT_MENSAJES, ctx.aportes);
            calcularRentabilidadRealAportes(ctx.aportes, salidaCustodia);
        }
    }

    /** D-F3B-37: an aporte without movement in the answer keeps NULL {@code RentabilidadReal} (the column read by {@code SelectSolicitudes} does not include it). */
    private static void calcularRentabilidadRealAportes(List<SolicitudProceso> aportes, CustodiaSalida salidaCustodia) {
        for (SolicitudProceso sol : aportes) {
            if (ESTADO_RECHAZADO.equalsIgnoreCase(sol.EstadoProceso) || TIPO_SOLICITUD_RESCATE.equals(sol.TipoSolicitud)) continue;
            MovimientoSalida cartera = salidaCustodia.P_OUT_MOVIMIENTOS.stream()
                    .filter(c -> c.IdGenerarMovimientoCustodia.longValue() == sol.IdSolicitudAporteRescate.longValue()).findFirst().orElse(null);
            if (cartera != null) {
                sol.IdCartera = cartera.IdCustodia;
                sol.RentabilidadReal = cartera.MontoCompraFactorOriginal;
            }
        }
    }
}
