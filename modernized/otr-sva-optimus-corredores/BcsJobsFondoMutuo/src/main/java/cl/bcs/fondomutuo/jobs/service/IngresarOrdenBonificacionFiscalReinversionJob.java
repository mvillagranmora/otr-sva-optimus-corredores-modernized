package cl.bcs.fondomutuo.jobs.service;

import cl.bcs.fondomutuo.jobs.domain.*;
import cl.bcs.fondomutuo.jobs.port.out.FondoMutuoServicioPort;
import cl.bcs.fondomutuo.jobs.port.out.HanaFondoMutuoPort;
import cl.bcs.fondomutuo.jobs.port.out.ParametrosProgramaPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Modernized IngresarOrdenBonificacionFiscalReinversion.
 *
 * Legacy: cl.bcs.fondomutuo.jobs.IngresarOrdenBonificacionFiscalReinversion
 * Creates FFMM Bonificación Fiscal rescate + aporte reinversion solicitudes.
 *
 * Business rules implemented (pure static methods, tested):
 *   B-FMJ-17  porcentaje=0 → PorcentajeCalculado = NumeroCuotas*100 / CantidadCustodia (HALF_UP, scale=2)
 *   B-FMJ-18  TipoSolicitud=APORTE → CalculoPorMonto; Monto = MontoMercado * porcentaje/100
 *   B-FMJ-19  TipoSolicitud=RESCATE → CalculoPorCuota; cuotas = CantidadCustodia * porcentaje/100,
 *              scaled to DecimalesCuota HALF_DOWN
 *   B-FMJ-20  Custody overshoot cap: if CantidadCustodia < totalNumeroCuotasRescates and diff < 1 →
 *              adjust last NumeroCuotas to cap exactly at CantidadCustodia.
 *   B-FMJ-21  IdRegimenTributario hardcoded "BF".
 *   B-FMJ-22  Aporte Monto = MontoMercado * apor.Porcentaje / 100 (ReinversionSerieFondoDestino).
 *
 * D-FMJ-26: HANA queries behind HanaFondoMutuoPort and ParametrosProgramaPort.
 * D-FMJ-27: Service calls (ConsultarCustodiaPortafolio, CrearSolicitudRescateReinversion)
 *            behind FondoMutuoServicioPort.
 * D-FMJ-28: AgregarLogPrograma call behind HanaFondoMutuoPort.
 *
 * main() removed per RULE-076.
 */
@Service
public class IngresarOrdenBonificacionFiscalReinversionJob {

    private static final Logger log = LoggerFactory.getLogger(IngresarOrdenBonificacionFiscalReinversionJob.class);
    public static final String ID_TIPO_PROGRAMA = "INGRESAR_BONIFICACION_FISCAL_REINVERSION";

    // ConstantesFondoMutuo values inlined
    private static final String TIPO_SOLICITUD_APORTE  = "APO";
    private static final String TIPO_SOLICITUD_RESCATE = "RES";
    private static final String CALCULO_POR_MONTO      = "MONTO";
    private static final String CALCULO_POR_CUOTA      = "CUOTA";
    private static final String CUENTA_INVERSION       = "CI";
    private static final String REGIMEN_BF             = "BF";
    private static final String SI                     = "S";

    private final ParametrosProgramaPort parametrosPort;
    private final HanaFondoMutuoPort     hanaPort;
    private final FondoMutuoServicioPort servicioPort;

    public IngresarOrdenBonificacionFiscalReinversionJob(ParametrosProgramaPort parametrosPort,
            HanaFondoMutuoPort hanaPort, FondoMutuoServicioPort servicioPort) {
        this.parametrosPort = parametrosPort;
        this.hanaPort       = hanaPort;
        this.servicioPort   = servicioPort;
    }

    /**
     * Computes the PorcentajeCalculado when the source porcentaje is zero.
     *
     * B-FMJ-17: porcentaje=0 → PorcentajeCalculado = NumeroCuotas * 100 / CantidadCustodia
     *            scale=2, HALF_UP.
     *
     * @param numeroCuotas       source cuotas from aporte/rescate
     * @param cantidadCustodia   custody quantity from portfolio
     * @return porcentaje calculado (0–100 scale, 2 decimal places)
     */
    public static BigDecimal calcularPorcentaje(BigDecimal numeroCuotas, BigDecimal cantidadCustodia) {
        return numeroCuotas
                .multiply(BigDecimal.valueOf(100))
                .divide(cantidadCustodia, 2, RoundingMode.HALF_UP);
    }

    /**
     * Computes rescate NumeroCuotas for a RESCATE solicitud.
     *
     * B-FMJ-19: cuotas = CantidadCustodia * effectivePorcentaje / 100, scaled to
     *            decimalesCuota HALF_DOWN.
     *
     * @param cantidadCustodia    custody quantity
     * @param effectivePorcentaje effective percentage (may be porcentajeCalculado if porcentaje=0)
     * @param decimalesCuota      decimal places for the series
     * @return rounded NumeroCuotas
     */
    public static BigDecimal calcularNumeroCuotasRescate(
            BigDecimal cantidadCustodia, BigDecimal effectivePorcentaje, int decimalesCuota) {
        return cantidadCustodia
                .multiply(effectivePorcentaje)
                .divide(BigDecimal.valueOf(100))
                .setScale(decimalesCuota, RoundingMode.HALF_DOWN);
    }

    /**
     * Applies the custody overshoot cap.
     *
     * B-FMJ-20: if cantidadCustodia < totalNumeroCuotasRescates and the difference
     *            is less than 1 unit → adjust NumeroCuotas by adding the negative delta.
     *
     * @param numeroCuotas             already-computed cuotas for this solicitud
     * @param cantidadCustodia         total custody quantity
     * @param totalNumeroCuotasRescates running sum including this solicitud
     * @return adjusted NumeroCuotas
     */
    public static BigDecimal aplicarCapCustodia(BigDecimal numeroCuotas,
            BigDecimal cantidadCustodia, BigDecimal totalNumeroCuotasRescates) {
        if (cantidadCustodia.compareTo(totalNumeroCuotasRescates) < 0) {
            BigDecimal diff = cantidadCustodia.subtract(totalNumeroCuotasRescates).abs();
            if (diff.compareTo(BigDecimal.ONE) < 1) {
                // cap: adjust by the overshoot (negative delta)
                return numeroCuotas.add(cantidadCustodia.subtract(totalNumeroCuotasRescates));
            }
        }
        return numeroCuotas;
    }

    /**
     * Executes the BF reinversion job using the given program's parameters.
     *
     * @param jobId program ID (required)
     */
    public void ejecutar(Long jobId) {
        Objects.requireNonNull(jobId, "jobId is required");

        log.info("INICIO JOB {}", ID_TIPO_PROGRAMA);

        // Read parameters from HANA
        List<ParametrosPrograma> rawParams = parametrosPort.consultarParametros(jobId);

        Long idEmpresa = null;
        Long idUsuario = null;
        List<Long> idsAporteRescate = new ArrayList<>();

        for (ParametrosPrograma p : rawParams) {
            switch (p.nombreParametro()) {
                case "IdEmpresa" -> idEmpresa = Long.parseLong(p.valor());
                case "IdUsuario" -> idUsuario = Long.parseLong(p.valor());
                case "IdSolicitudAporteRescate" -> {
                    for (String id : p.valor().split(";")) {
                        idsAporteRescate.add(Long.parseLong(id.trim()));
                    }
                }
            }
        }

        Objects.requireNonNull(idEmpresa, "IdEmpresa parameter missing");
        Objects.requireNonNull(idUsuario, "IdUsuario parameter missing");

        // Get system date from configuration
        String fechaSistema = hanaPort.consultarParametroConfiguracion(idEmpresa, "PLA.FechaSistema");

        // Query AporteRescate records eligible for BF
        List<ApoResFFMM> aporteRescates = hanaPort.consultarAportesRescateBF(idsAporteRescate);

        if (aporteRescates.isEmpty()) {
            hanaPort.agregarLogPrograma(jobId, ID_TIPO_PROGRAMA, "NO CUENTA CON PORTAFOLIO BF");
            log.info("FIN JOB {} — sin solicitudes elegibles", ID_TIPO_PROGRAMA);
            return;
        }

        // Query custody portfolio for the client
        Long idCliente = aporteRescates.get(0).idCliente();
        List<CustodiaPortafolio> custodia = servicioPort.consultarCustodiaPortafolio(
                idEmpresa, idUsuario, "FMM", idCliente, fechaSistema);

        // Query reinversion destinations
        List<ReinversionSerieFondoDestino> reinversionDestinos =
                hanaPort.consultarReinversionSerieFondoDestino(idsAporteRescate);

        // Query series with DecimalesCuota
        List<String> series = aporteRescates.stream()
                .map(ApoResFFMM::idSerieFondo)
                .distinct()
                .collect(Collectors.toList());
        List<SerieFondoSolicitudes> seriesFondo = hanaPort.consultarSeriesFondo(idEmpresa, series);
        Map<String, SerieFondoSolicitudes> seriesMap = seriesFondo.stream()
                .collect(Collectors.toMap(SerieFondoSolicitudes::idSerieFondo, s -> s));

        // Build solicitudes
        List<Map<String, Object>> solicitudesRescate = new ArrayList<>();
        List<Map<String, Object>> solicitudesAporte  = new ArrayList<>();
        long contadorFFMM = 1L;
        long contadorFF   = 1L;
        long idAgrupador  = 1L;
        Set<String> hashSolicitudes = new HashSet<>();

        for (CustodiaPortafolio out : custodia) {
            BigDecimal totalNumeroCuotasRescates = BigDecimal.ZERO;

            for (ApoResFFMM sol : aporteRescates) {
                if (!out.nemotecnico().equals(sol.idSerieFondo())
                        || !out.idPortafolio().equals(sol.idPortafolio())) {
                    continue;
                }

                SerieFondoSolicitudes serie = seriesMap.get(sol.idSerieFondo());
                int decimalesCuota = serie != null ? serie.decimalesCuota().intValue() : 8;

                // B-FMJ-17: compute effective porcentaje
                BigDecimal effectivePorcentaje = sol.porcentaje();
                if (effectivePorcentaje.compareTo(BigDecimal.ZERO) == 0) {
                    effectivePorcentaje = calcularPorcentaje(sol.numeroCuotas(), out.cantidadCustodia());
                }

                List<ReinversionSerieFondoDestino> destinosFiltrados = reinversionDestinos.stream()
                        .filter(r -> r.idSolicitudAporteRescate().equals(sol.idSolicitudAporteRescate()))
                        .collect(Collectors.toList());

                for (ReinversionSerieFondoDestino apor : destinosFiltrados) {
                    // --- Rescate ---
                    BigDecimal montoRescate;
                    BigDecimal cuotasRescate;

                    if (TIPO_SOLICITUD_APORTE.equals(sol.tipoSolicitud())) {
                        // B-FMJ-18: APORTE branch
                        montoRescate  = out.montoMercadoCustodia()
                                           .multiply(sol.porcentaje())
                                           .divide(BigDecimal.valueOf(100));
                        cuotasRescate = out.cantidadCustodia();
                    } else if (TIPO_SOLICITUD_RESCATE.equals(sol.tipoSolicitud())) {
                        // B-FMJ-19: RESCATE branch
                        montoRescate  = out.montoMercadoCustodia()
                                           .multiply(effectivePorcentaje)
                                           .divide(BigDecimal.valueOf(100));
                        cuotasRescate = calcularNumeroCuotasRescate(
                                out.cantidadCustodia(), effectivePorcentaje, decimalesCuota);

                        String hashKey = sol.idCliente() + "_" + sol.idPortafolio() + "_"
                                + sol.idSerieFondo() + "_" + sol.calculoPorMontoCuota() + "_"
                                + montoRescate + "_" + cuotasRescate + "_" + idAgrupador;
                        if (hashSolicitudes.add(hashKey)) {
                            totalNumeroCuotasRescates = totalNumeroCuotasRescates.add(cuotasRescate);
                            // B-FMJ-20: overshoot cap
                            cuotasRescate = aplicarCapCustodia(
                                    cuotasRescate, out.cantidadCustodia(), totalNumeroCuotasRescates);
                        }
                    } else {
                        montoRescate  = BigDecimal.ZERO;
                        cuotasRescate = BigDecimal.ZERO;
                    }

                    // Build rescate map
                    Map<String, Object> rescateMap = new LinkedHashMap<>();
                    rescateMap.put("IdInterno",            contadorFFMM);
                    rescateMap.put("IdAgrupador",          idAgrupador);
                    rescateMap.put("IdCliente",            sol.idCliente());
                    rescateMap.put("IdPortafolio",         sol.idPortafolio());
                    rescateMap.put("IdSerieFondo",         sol.idSerieFondo());
                    rescateMap.put("CalculoPorMontoCuota", TIPO_SOLICITUD_APORTE.equals(sol.tipoSolicitud())
                            ? CALCULO_POR_MONTO : CALCULO_POR_CUOTA);
                    rescateMap.put("Monto",                montoRescate);
                    rescateMap.put("NumeroCuotas",         cuotasRescate);
                    rescateMap.put("IdMoneda",             sol.idMoneda());
                    rescateMap.put("IdMedioDePago",        CUENTA_INVERSION);
                    rescateMap.put("SolicitudBatch",       sol.solicitudBatch());
                    rescateMap.put("OrigenOperacion",      sol.origenOperacion());
                    rescateMap.put("IdRegimenTributario",  REGIMEN_BF); // B-FMJ-21
                    rescateMap.put("Glosa",                "");
                    rescateMap.put("IdAutorizadoOrigen",   0L);
                    rescateMap.put("FolioContrato",        0L);
                    rescateMap.put("Porcentaje",           sol.porcentaje());
                    rescateMap.put("IngresoAutomatico",    SI);
                    solicitudesRescate.add(rescateMap);

                    // --- Aporte (B-FMJ-22) ---
                    Map<String, Object> aporteMap = new LinkedHashMap<>();
                    aporteMap.put("IdInterno",           contadorFF);
                    aporteMap.put("IdAgrupador",         idAgrupador);
                    aporteMap.put("IdCliente",           sol.idCliente());
                    aporteMap.put("IdPortafolio",        sol.idPortafolio());
                    aporteMap.put("IdEmisor",            sol.idEmisorDestino());
                    aporteMap.put("IdMoneda",            sol.idMoneda());
                    aporteMap.put("IdMedioDePago",       CUENTA_INVERSION);
                    aporteMap.put("IdRegimenTributario", REGIMEN_BF);
                    aporteMap.put("Glosa",               "");
                    aporteMap.put("IndicadorMLT",        "N");
                    aporteMap.put("IdSerieFondo",        apor.idSerieFondoDestino());
                    aporteMap.put("Porcentaje",          apor.porcentaje());
                    // B-FMJ-22: Monto = MontoMercado * apor.Porcentaje / 100
                    aporteMap.put("Monto",               out.montoMercadoCustodia()
                            .multiply(apor.porcentaje()).divide(BigDecimal.valueOf(100)));
                    aporteMap.put("NumeroCuotas",        out.cantidadCustodia()
                            .multiply(apor.porcentaje()).divide(BigDecimal.valueOf(100)));
                    aporteMap.put("FolioContrato",       0L);
                    solicitudesAporte.add(aporteMap);

                    contadorFF++;
                    contadorFFMM++;
                }
                idAgrupador++;
            }
        }

        if (solicitudesRescate.isEmpty()) {
            hanaPort.agregarLogPrograma(jobId, ID_TIPO_PROGRAMA, "NO CUENTA CON PORTAFOLIO BF");
            log.info("FIN JOB {} — sin solicitudes a crear", ID_TIPO_PROGRAMA);
            return;
        }

        // Call service to create solicitudes
        Map<String, Object> rpt = servicioPort.crearSolicitudRescateReinversion(
                idEmpresa, idUsuario, "FMM", solicitudesRescate, solicitudesAporte);

        // Log messages
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> mensajes = (List<Map<String, Object>>) rpt.getOrDefault("P_OUT_MENSAJES", List.of());
        for (Map<String, Object> mje : mensajes) {
            String tipo = (String) mje.get("tipo");
            String desc = (String) mje.get("descripcion");
            if (MensajeSalida.MENSAJE_ADVERTENCIA.equals(tipo)) {
                hanaPort.agregarLogPrograma(jobId, ID_TIPO_PROGRAMA, "ADVERTENCIAS FFMM : " + desc);
            } else if (MensajeSalida.MENSAJE_ERROR.equals(tipo)) {
                hanaPort.agregarLogPrograma(jobId, ID_TIPO_PROGRAMA, "ERRORES FFMM : " + desc);
            }
        }

        log.info("FIN JOB {}", ID_TIPO_PROGRAMA);
    }
}
