package cl.bcs.fondomutuo.consultas.custodia;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.cierre.FormatoMoneda;
import cl.bcs.fondomutuo.cierre.ListaHash;
import cl.bcs.fondomutuo.cierre.contrato.ParidadContrato;
import cl.bcs.fondomutuo.cuotas.CalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.fechas.ObtenerDiaSiguiente;
import cl.bcs.fondomutuo.kit.InformacionEmpresa;
import cl.bcs.fondomutuo.port.out.http.ParidadPort;
import cl.bcs.fondomutuo.port.out.kit.ConfiguracionSistemaPort;
import cl.bcs.fondomutuo.port.out.kit.EmpresaPort;

/**
 * Legacy {@code ConsultarCustodiaPortafolioFondoMutuo.logica} (FM-CPF-01..14, read only), bug for bug: the balances are a {@code HashMap<String,..>} of the DEFAULT capacity filled
 * with the solicitudes in transit FIRST and then the custody, and the output order is its iteration order (D-F3A-04); the solicitud ids start at 2 and are assigned in a
 * {@code parallelStream().forEach} as the legacy does (D-F3A-04: which row gets which id is not a rule); the guarantee is valued with {@code IdInterno * -2} and looked up with
 * {@code IdInterno}, so {@code MontoGarantia} is always 0 (D-F3A-03); the series of an instrument are NOT ordered and a repeated instrument is "key field is not unique"; the MLT comes
 * from the FIRST regime of the series (D-F3A-08); the company is queried even when the next-day parameter is off (D-F3A-07). The four valuations are
 * {@link CalcularMontoNumeroCuota} calls that happen even with no series. The custody calculation is a port (another piece). The next working day is
 * {@link ConsultarCustodiaPortafolioDiaSiguiente} (D-F3A-29). No state; the unit of work is the ambient read-only transaction.
 */
public class ConsultarCustodiaPortafolioService implements ConsultarCustodiaPortafolioPort {

    private static final String FECHA_DEFECTO = "0001-01-01";
    private static final String SI = "S";
    private static final String NO = "N";
    private static final String CALCULO_POR_CUOTA = "C";
    private static final String CALCULO_POR_MONTO = "M";
    private static final String TIPO_SOLICITUD_APORTE = "APO";
    private static final String TIPO_SOLICITUD_RESCATE = "RES";
    private static final String UF = "UF";
    private static final long FACTOR_APORTE = 1L;
    private static final long FACTOR_RESCATE = -1L;

    private final ConfiguracionSistemaPort configuracion;
    private final ConsultarCustodiaPortafolioDatos datos;
    private final ConsultarCustodiaPortafolioCustodiaPort custodia;
    private final CalcularMontoNumeroCuota cuotas;
    private final ConsultarCustodiaPortafolioDiaSiguiente diaSiguiente;
    private final EmpresaPort empresas;
    private final ParidadPort paridad;
    private final FormatoMoneda formato;

    public ConsultarCustodiaPortafolioService(ConfiguracionSistemaPort configuracion, ConsultarCustodiaPortafolioDatos datos, ConsultarCustodiaPortafolioCustodiaPort custodia,
            CalcularMontoNumeroCuota cuotas, ConsultarCustodiaPortafolioDiaSiguiente diaSiguiente, EmpresaPort empresas, ParidadPort paridad, FormatoMoneda formato) {
        this.configuracion = configuracion;
        this.datos = datos;
        this.custodia = custodia;
        this.cuotas = cuotas;
        this.diaSiguiente = diaSiguiente;
        this.empresas = empresas;
        this.paridad = paridad;
        this.formato = formato;
    }

    /** Saldo of a client / portfolio / instrument / regime (legacy {@code SaldoPortafolioRegimenInstrumento}). */
    private static final class Saldo {
        Long idInstrumento;
        Long idPortafolio;
        boolean tieneRescateTotal = false;
        ConsultarCustodiaPortafolioContrato.Solicitud solicitudesTransito;
        ConsultarCustodiaPortafolioContrato.CustodiaFila custodia;
    }

    /** Legacy {@code SalidaCustodiaTemp}: the row of the output plus the internal id and the system currency (the temp defaults of the transit amounts are 0). */
    private static final class Temp {
        Long idInterno;
        String idMonedaSistema;
        final ConsultarCustodiaPortafolioContrato.SalidaCustodia s = new ConsultarCustodiaPortafolioContrato.SalidaCustodia();

        Temp() {
            s.MontoTransSolicitudesAporte = BigDecimal.ZERO;
            s.MontoTransSolicitudesRescate = BigDecimal.ZERO;
        }
    }

    @Override
    public ConsultarCustodiaPortafolioContrato.Salida logica(ConsultarCustodiaPortafolioContrato.Entrada entrada) throws Exception {
        ConsultarCustodiaPortafolioContrato.Salida out = new ConsultarCustodiaPortafolioContrato.Salida();

        entrada.p_FechaConsulta = obtenerFechaConsultaReal(entrada);

        ConsultarCustodiaPortafolioDatos.Parametros parametros = obtenerParametrosConsultasSolicitudes(entrada);
        List<ConsultarCustodiaPortafolioContrato.Solicitud> solicitudes = obtenerSolicitudesTransitoValorizadas(entrada, parametros);
        Map<String, ConsultarCustodiaPortafolioContrato.SolicitudTotal> rescatesTotales = obtenerSolicitudesTransitoTotales(parametros);
        List<ConsultarCustodiaPortafolioContrato.CustodiaFila> custodias = obtenerCustodiaPortafolio(entrada);

        HashMap<String, Saldo> saldos = obtenerSaldoInstrumentosAgrupadosPorPortafolioRegimen(solicitudes, custodias, rescatesTotales);

        List<Temp> salidaPreliminar = obtenerSalidaPreliminar(entrada, saldos);
        valorizarCustodiaSalidaPreliminar(entrada, salidaPreliminar);
        valorizarGarantiaSalidaPreliminar(entrada, salidaPreliminar);
        valorizarTransitoSalidaPreliminar(entrada, salidaPreliminar);

        Map<Long, ParidadContrato.CalculoSalida> valoresCuotaMonedaSistema = obtenerValoresCuotasMonedaSistema(entrada, salidaPreliminar);

        out.P_OUT_TBL = obtenerSalidaCustodia(entrada.p_IdEmpresa, salidaPreliminar, valoresCuotaMonedaSistema);
        return out;
    }

    // ---- date of the query (FM-CPF-02)

    private String obtenerFechaConsultaReal(ConsultarCustodiaPortafolioContrato.Entrada entrada) {
        String fechaSistema = configuracion.texto(entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
        if (FECHA_DEFECTO.equals(entrada.p_FechaConsulta) || entrada.p_FechaConsulta == null) {
            return fechaSistema;
        }
        return entrada.p_FechaConsulta;
    }

    private ConsultarCustodiaPortafolioDatos.Parametros obtenerParametrosConsultasSolicitudes(ConsultarCustodiaPortafolioContrato.Entrada entrada) {
        String filtraSoloSolicitudesNoAutomaticas = configuracion.texto(entrada.p_IdEmpresa, "FONDOMUTUO", "WS_SOLICITUDES_MUESTRA_SOLO_SOLICITUDES_NO_AUTOMATICAS");
        ConsultarCustodiaPortafolioDatos.Parametros parametros = new ConsultarCustodiaPortafolioDatos.Parametros();
        if (SI.equals(filtraSoloSolicitudesNoAutomaticas)) {
            parametros.IngresoAutomatico = NO;
            parametros.FiltraIngresoAutomatico = SI;
        } else {
            parametros.IngresoAutomatico = "";
            parametros.FiltraIngresoAutomatico = NO;
        }
        parametros.IdEmpresa = entrada.p_IdEmpresa;
        parametros.IdCliente = entrada.p_IdCliente;
        parametros.IdPortafolio = entrada.p_IdPortafolio;
        parametros.IdInstrumento = entrada.p_IdInstrumento;
        parametros.IdProductoAgrupador = entrada.p_IdProductoAgrupador;
        parametros.IdEmisor = entrada.p_IdEmisor;
        parametros.IdFamiliaFondo = entrada.p_IdFamiliaFondo;
        return parametros;
    }

    // ---- solicitudes in transit (FM-CPF-03/04)

    private List<ConsultarCustodiaPortafolioContrato.Solicitud> obtenerSolicitudesTransitoValorizadas(ConsultarCustodiaPortafolioContrato.Entrada entrada,
            ConsultarCustodiaPortafolioDatos.Parametros parametros) throws Exception {
        List<ConsultarCustodiaPortafolioContrato.Solicitud> solicitudes = datos.solicitudes(parametros);
        solicitudes = asignarIdInternoSolicitudes(solicitudes);
        return valorizarSolicitudes(entrada, solicitudes);
    }

    private Map<String, ConsultarCustodiaPortafolioContrato.SolicitudTotal> obtenerSolicitudesTransitoTotales(ConsultarCustodiaPortafolioDatos.Parametros parametros) {
        List<ConsultarCustodiaPortafolioContrato.SolicitudTotal> totales = datos.solicitudesTotales(parametros);
        return totales.stream().collect(Collectors.toMap(s -> s.IdPortafolio + "_" + s.IdInstrumento, s -> s));
    }

    /** D-F3A-04: ids 2, 3, ... assigned in a {@code parallelStream().forEach}, as the legacy does. */
    private static List<ConsultarCustodiaPortafolioContrato.Solicitud> asignarIdInternoSolicitudes(List<ConsultarCustodiaPortafolioContrato.Solicitud> solicitudes) {
        AtomicLong idInterno = new AtomicLong(1L);
        solicitudes.parallelStream().forEach(solicitud -> {
            solicitud.IdInterno = idInterno.incrementAndGet();
        });
        return solicitudes;
    }

    private List<ConsultarCustodiaPortafolioContrato.Solicitud> valorizarSolicitudes(ConsultarCustodiaPortafolioContrato.Entrada entrada,
            List<ConsultarCustodiaPortafolioContrato.Solicitud> solicitudes) throws Exception {
        CalcularMontoNumeroCuota.EntradaCalcularMontoNumeroCuota entradaCalculo = new CalcularMontoNumeroCuota.EntradaCalcularMontoNumeroCuota();
        entradaCalculo.p_IdEmpresa = entrada.p_IdEmpresa;

        List<CalcularMontoNumeroCuota.Serie> aportes = solicitudes.stream().filter(s -> s.CantidadAporte.compareTo(BigDecimal.ZERO) == 1).map(s -> {
            CalcularMontoNumeroCuota.Serie serie = serie(entrada, s.IdInterno * FACTOR_APORTE, s.IdSerieFondo, s.CalculoPorMontoCuota, s.CantidadMontoAporte,
                    s.CantidadAporte, s.IdMonedaFondo, TIPO_SOLICITUD_APORTE);
            serie.IdTipoMovimientoFondoMutuo = (s.IdTipoMovimientoFondoMutuo == null) ? "" : s.IdTipoMovimientoFondoMutuo;
            return serie;
        }).collect(Collectors.toList());
        List<CalcularMontoNumeroCuota.Serie> rescates = solicitudes.stream().filter(s -> s.CantidadRescate.compareTo(BigDecimal.ZERO) == 1).map(s -> {
            CalcularMontoNumeroCuota.Serie serie = serie(entrada, s.IdInterno * FACTOR_RESCATE, s.IdSerieFondo, s.CalculoPorMontoCuota, s.CantidadMontoRescate,
                    s.CantidadRescate, s.IdMonedaFondo, TIPO_SOLICITUD_RESCATE);
            serie.IdTipoMovimientoFondoMutuo = (s.IdTipoMovimientoFondoMutuo == null) ? "" : s.IdTipoMovimientoFondoMutuo;
            return serie;
        }).collect(Collectors.toList());

        entradaCalculo.p_Series.addAll(aportes);
        entradaCalculo.p_Series.addAll(rescates);

        CalcularMontoNumeroCuota.SalidaCalcularMontoNumeroCuota salida = cuotas.logica(entradaCalculo);
        Map<Long, CalcularMontoNumeroCuota.Resultado> montosValorizados = salida.P_OUT_RESULTADO.stream().collect(Collectors.toMap(r -> r.IdInterno, r -> r));

        for (ConsultarCustodiaPortafolioContrato.Solicitud solicitud : solicitudes) {
            CalcularMontoNumeroCuota.Resultado aporte = montosValorizados.get(solicitud.IdInterno * FACTOR_APORTE);
            if (aporte != null) {
                solicitud.CantidadAporte = aporte.NumeroCuotas;
                solicitud.CantidadMontoAporte = CALCULO_POR_MONTO.equals(solicitud.CalculoPorMontoCuota) ? solicitud.MontoMonedaSistemaAporte : aporte.MontoMonedaSistema;
            }
            CalcularMontoNumeroCuota.Resultado rescate = montosValorizados.get(solicitud.IdInterno * FACTOR_RESCATE);
            if (rescate != null) {
                solicitud.CantidadRescate = rescate.NumeroCuotas;
                solicitud.CantidadMontoRescate = rescate.MontoMonedaSistema;
            }
        }
        return solicitudes;
    }

    /** One {@code Serie} of the valuation: date of the query, currency of the fund as investment currency, factor UF. */
    private static CalcularMontoNumeroCuota.Serie serie(ConsultarCustodiaPortafolioContrato.Entrada entrada, Long idInterno, String idSerieFondo, String calculoPorMontoCuota,
            BigDecimal monto, BigDecimal numeroCuotas, String idMonedaFondo, String tipoSolicitud) {
        CalcularMontoNumeroCuota.Serie serie = new CalcularMontoNumeroCuota.Serie();
        serie.IdInterno = idInterno;
        serie.Fecha = entrada.p_FechaConsulta;
        serie.IdSerieFondo = idSerieFondo;
        serie.CalculoPorMontoCuota = calculoPorMontoCuota;
        serie.Monto = monto;
        serie.NumeroCuotas = numeroCuotas;
        serie.IdMonedaFondo = idMonedaFondo;
        serie.IdMonedaInversion = idMonedaFondo;
        serie.TipoSolicitud = tipoSolicitud;
        serie.TipoFactorAjuste = UF;
        serie.IdEmpresa = entrada.p_IdEmpresa;
        return serie;
    }

    // ---- custody (the other piece)

    private List<ConsultarCustodiaPortafolioContrato.CustodiaFila> obtenerCustodiaPortafolio(ConsultarCustodiaPortafolioContrato.Entrada entrada) {
        ConsultarCustodiaPortafolioContrato.CustodiaEntrada e = new ConsultarCustodiaPortafolioContrato.CustodiaEntrada();
        e.p_IdEmpresa = entrada.p_IdEmpresa;
        e.p_IdUsuario = entrada.p_IdUsuario;
        e.p_IdModulo = entrada.p_IdModulo;
        e.p_IdCliente = entrada.p_IdCliente;
        e.p_IdPortafolio = entrada.p_IdPortafolio;
        e.p_IdProductoAgrupador = entrada.p_IdProductoAgrupador;
        e.p_IdEmisor = entrada.p_IdEmisor;
        e.p_IdInstrumento = entrada.p_IdInstrumento;
        e.p_FechaConsulta = entrada.p_FechaConsulta;
        e.p_IdFamiliaFondo = entrada.p_IdFamiliaFondo;
        Gson gson = ConsultarCustodiaPortafolioJson.gson();
        JsonObject respuesta = custodia.consultar(gson.toJsonTree(e).getAsJsonObject());
        return gson.fromJson(respuesta, ConsultarCustodiaPortafolioContrato.CustodiaSalida.class).p_out_tbl;
    }

    // ---- balances (FM-CPF-05/07/09, D-F3A-04)

    private static HashMap<String, Saldo> obtenerSaldoInstrumentosAgrupadosPorPortafolioRegimen(List<ConsultarCustodiaPortafolioContrato.Solicitud> solicitudes,
            List<ConsultarCustodiaPortafolioContrato.CustodiaFila> custodias, Map<String, ConsultarCustodiaPortafolioContrato.SolicitudTotal> rescateTotales) {
        HashMap<String, Saldo> saldos = new HashMap<String, Saldo>();   // DEFAULT capacity: its iteration order is the output order
        for (ConsultarCustodiaPortafolioContrato.Solicitud solicitud : solicitudes) {
            String key = solicitud.IdCliente + "_" + solicitud.IdPortafolio + "_" + solicitud.IdInstrumento + "_" + solicitud.IdRegimenFondoMutuo;
            Saldo instrumento = saldos.get(key);
            if (instrumento == null) {
                instrumento = new Saldo();
                instrumento.idPortafolio = solicitud.IdPortafolio;
                instrumento.idInstrumento = solicitud.IdInstrumento;
                instrumento.solicitudesTransito = solicitud;
            } else {
                ConsultarCustodiaPortafolioContrato.Solicitud acumulada = instrumento.solicitudesTransito;
                acumulada.CantidadAporte = solicitud.CantidadAporte.add(acumulada.CantidadAporte, MathContext.DECIMAL128);
                acumulada.CantidadMontoAporte = solicitud.CantidadMontoAporte.add(acumulada.CantidadMontoAporte, MathContext.DECIMAL128);
                acumulada.CantidadRescate = solicitud.CantidadRescate.add(acumulada.CantidadRescate, MathContext.DECIMAL128);
                acumulada.CantidadMontoRescate = solicitud.CantidadMontoRescate.add(acumulada.CantidadMontoRescate, MathContext.DECIMAL128);
            }
            saldos.put(key, instrumento);
        }

        for (ConsultarCustodiaPortafolioContrato.CustodiaFila custodia : custodias) {
            String key = custodia.IdCliente + "_" + custodia.IdPortafolio + "_" + custodia.IdInstrumento + "_" + custodia.IdRegimenFondoMutuo;
            Saldo instrumento = saldos.get(key);
            if (instrumento == null) {
                instrumento = new Saldo();
                instrumento.idPortafolio = custodia.IdPortafolio;
                instrumento.idInstrumento = custodia.IdInstrumento;
            }
            instrumento.custodia = custodia;   // the LAST custody row of a key wins
            saldos.put(key, instrumento);
        }

        for (Map.Entry<String, Saldo> entry : saldos.entrySet()) {
            Saldo saldo = entry.getValue();
            if (rescateTotales.get(saldo.idPortafolio + "_" + saldo.idInstrumento) != null) {
                saldo.tieneRescateTotal = true;
            }
        }
        return saldos;
    }

    // ---- preliminary output (FM-CPF-08/10)

    private List<Temp> obtenerSalidaPreliminar(ConsultarCustodiaPortafolioContrato.Entrada entrada, HashMap<String, Saldo> saldos) throws Exception {
        List<Temp> salidaPreliminar = new ArrayList<Temp>();

        String idMonedaSistema = configuracion.texto(entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
        List<Long> idsInstrumentos = saldos.entrySet().stream().map(s -> s.getValue().idInstrumento).distinct().collect(Collectors.toList());
        List<ConsultarCustodiaPortafolioContrato.SerieFondo> seriesFondo = datos.series(idsInstrumentos);
        HashMap<Long, ConsultarCustodiaPortafolioContrato.SerieFondo> seriesPorInstrumento = ListaHash.aHash(seriesFondo, s -> s.IdInstrumento);
        List<String> idSerieFondos = seriesFondo.stream().map(s -> s.IdSerieFondo).distinct().collect(Collectors.toList());
        List<ConsultarCustodiaPortafolioContrato.Regimen> regimenes = datos.regimenes(idSerieFondos);

        AtomicLong idInterno = new AtomicLong(1L);
        for (Map.Entry<String, Saldo> obj : saldos.entrySet()) {
            Saldo saldo = obj.getValue();
            ConsultarCustodiaPortafolioContrato.SerieFondo serie = seriesPorInstrumento.get(saldo.idInstrumento);
            if (serie == null) {
                continue;   // a balance whose instrument has no series is omitted
            }
            Temp temp = new Temp();
            ConsultarCustodiaPortafolioContrato.SalidaCustodia salida = temp.s;
            temp.idInterno = idInterno.getAndIncrement();
            temp.idMonedaSistema = idMonedaSistema;
            salida.IdClasificacionTipoInstrumento = serie.IdClasificacionTipoInstrumento;
            salida.NombreClasificacionTipoInstrumento = serie.NombreClasificacionTipoInstrumento;
            salida.IdTipoInstrumento = serie.IdTipoInstrumento;
            salida.NombreTipoInstrumento = serie.NombreTipoInstrumento;
            salida.IdInstrumento = saldo.idInstrumento;
            salida.Nemotecnico = serie.IdSerieFondo;   // D-F3A-06
            salida.IdMoneda = serie.IdMoneda;
            salida.DecimalesValorCuota = serie.DecimalesValorCuota;
            salida.DecimalesCuota = serie.DecimalesCuota;
            salida.IdMonedaPrincipal = serie.IdMoneda;
            salida.IdMonedaFondo = serie.IdMoneda;
            salida.IdEmisor = serie.IdEmisor;
            salida.IdSerieFondo = serie.IdSerieFondo;
            salida.IdEmpresa = entrada.p_IdEmpresa;
            salida.IdFamiliaFondo = serie.IdFamiliaFondo;
            salida.FondoExtranjero = serie.FondoExtranjero;
            salida.Fondo = serie.IdFondo;
            salida.NombreFondo = serie.NombreFondo;
            salida.NombreSerieFondo = serie.NombreSerieFondo;
            salida.FechaValorCuota = FECHA_DEFECTO;
            salida.CodigoSerie = serie.CodigoSerie;
            salida.DocumentoIdentidadAdministradora = serie.DocumentoIdentidadAdministradora;
            salida.NombreAdministradora = serie.IdEmisor;
            salida.RestringeRescate = serie.RestringeRescate;
            if (saldo.custodia == null) {
                ConsultarCustodiaPortafolioContrato.Solicitud t = saldo.solicitudesTransito;
                salida.IdSocioDeNegocio = t.IdSocioDeNegocio;
                salida.IdCliente = t.IdCliente;
                salida.IdPortafolio = t.IdPortafolio;
                salida.DocumentoIdentidad = t.DocumentoIdentidad;
                salida.RazonSocial = t.RazonSocial;
                salida.CodigoPortafolio = t.CodigoPortafolio;
                salida.IdBoveda = "COR";
                salida.Relacion = "";
                salida.Producto = t.Producto;
                salida.IdRegimenFondoMutuo = t.IdRegimenFondoMutuo;
                salida.EsADC = t.EsADC;
                salida.IdMatrizAgrupador = t.IdMatrizAgrupador;
                salida.RegimenTributario = t.RegimenTributario;
            } else {
                ConsultarCustodiaPortafolioContrato.CustodiaFila c = saldo.custodia;
                salida.IdSocioDeNegocio = c.IdSocioDeNegocio;
                salida.IdCliente = c.IdCliente;
                salida.IdPortafolio = c.IdPortafolio;
                salida.DocumentoIdentidad = c.DocumentoIdentidad;
                salida.RazonSocial = c.RazonSocial;
                salida.CodigoPortafolio = c.CodigoPortafolio;
                salida.IdBoveda = c.IdBoveda;
                salida.Relacion = c.Relacion;
                salida.Producto = c.Producto;
                salida.IdRegimenFondoMutuo = c.IdRegimenFondoMutuo;
                salida.EsADC = c.EsADC;
                salida.IdMatrizAgrupador = c.IdMatrizAgrupador;
                salida.RegimenTributario = c.RegimenTributario;
            }

            if (saldo.custodia != null) {
                salida.CantidadCustodia = saldo.custodia.CantidadCustodia;
                salida.CantidadGarantia = saldo.custodia.CantidadGarantia;
                salida.INGPrecioPromedioCompra = saldo.custodia.INGPrecioPromedioCompra;
                salida.INGMontoPrecioPromedioCompra = saldo.custodia.INGMontoPrecioPromedioCompra;
                salida.INGMontoPrecioPromedioMonedaInstrumento = saldo.custodia.INGMontoPrecioPromedioMonedaInstrumento;
                salida.PPPCustodia = saldo.custodia.PPPCustodia != null ? saldo.custodia.PPPCustodia : BigDecimal.ZERO;
            }

            if (saldo.solicitudesTransito != null) {
                salida.CantidadTransSolicitudesAporte = saldo.solicitudesTransito.CantidadAporte;
                salida.CantidadTransSolicitudesRescate = saldo.solicitudesTransito.CantidadRescate;
                salida.MontoTransSolicitudesAporte = saldo.solicitudesTransito.CantidadMontoAporte;
                salida.MontoTransSolicitudesRescate = saldo.solicitudesTransito.CantidadMontoRescate;
            }

            if (saldo.tieneRescateTotal) {
                salida.CantidadTransSolicitudesRescate = salida.CantidadCustodia.add(salida.CantidadTransSolicitudesAporte, MathContext.DECIMAL128);
            }

            if (SI.equals(entrada.p_BuscaMLT)) {
                for (ConsultarCustodiaPortafolioContrato.Regimen regimen : regimenes) {
                    if (regimen.IdSerieFondo.equals(serie.IdSerieFondo)) {
                        salida.MLT = (SI.equals(regimen.RescateConMandato) || SI.equals(regimen.AceptaMandatoEnReinversion)) ? "S" : "N";   // the FIRST regime decides: D-F3A-08
                        break;
                    }
                }
            } else {
                salida.MLT = "N";
            }
            salidaPreliminar.add(temp);
        }
        return salidaPreliminar;
    }

    // ---- the three valuations (FM-CPF-11/12)

    private void valorizarCustodiaSalidaPreliminar(ConsultarCustodiaPortafolioContrato.Entrada entrada, List<Temp> salidaPreliminar) throws Exception {
        CalcularMontoNumeroCuota.EntradaCalcularMontoNumeroCuota entradaCalculo = new CalcularMontoNumeroCuota.EntradaCalcularMontoNumeroCuota();
        entradaCalculo.p_IdEmpresa = entrada.p_IdEmpresa;
        entradaCalculo.p_Series = salidaPreliminar.stream()
                .map(t -> serie(entrada, t.idInterno, t.s.IdSerieFondo, CALCULO_POR_CUOTA, BigDecimal.ZERO, t.s.CantidadCustodia, t.s.IdMonedaFondo, TIPO_SOLICITUD_APORTE))
                .collect(Collectors.toList());

        Map<Long, CalcularMontoNumeroCuota.Resultado> hash = cuotas.logica(entradaCalculo).P_OUT_RESULTADO.stream().collect(Collectors.toMap(r -> r.IdInterno, r -> r));

        for (Temp t : salidaPreliminar) {
            CalcularMontoNumeroCuota.Resultado resultado = hash.get(t.idInterno);
            if (resultado != null) {
                t.s.FechaValorCuota = resultado.FechaValorCuota;
                t.s.Monto = resultado.Monto;
                t.s.ValorCuota = resultado.ValorCuota;
                t.s.MontoMonedaSistema = resultado.MontoMonedaSistema;
            }
        }
    }

    /** D-F3A-03: asked with {@code IdInterno * -2}, looked up with {@code IdInterno}: the guarantee amount is never filled (always 0). */
    private void valorizarGarantiaSalidaPreliminar(ConsultarCustodiaPortafolioContrato.Entrada entrada, List<Temp> salidaPreliminar) throws Exception {
        CalcularMontoNumeroCuota.EntradaCalcularMontoNumeroCuota entradaCalculo = new CalcularMontoNumeroCuota.EntradaCalcularMontoNumeroCuota();
        entradaCalculo.p_IdEmpresa = entrada.p_IdEmpresa;
        entradaCalculo.p_Series = salidaPreliminar.stream().filter(t -> BigDecimal.ZERO.compareTo(t.s.CantidadGarantia) != 0)
                .map(t -> serie(entrada, t.idInterno * -2L, t.s.IdSerieFondo, CALCULO_POR_CUOTA, BigDecimal.ZERO, t.s.CantidadGarantia, t.s.IdMonedaFondo, TIPO_SOLICITUD_APORTE))
                .collect(Collectors.toList());

        Map<Long, CalcularMontoNumeroCuota.Resultado> hash = cuotas.logica(entradaCalculo).P_OUT_RESULTADO.stream().collect(Collectors.toMap(r -> r.IdInterno, r -> r));

        for (Temp t : salidaPreliminar) {
            CalcularMontoNumeroCuota.Resultado resultado = hash.get(t.idInterno);
            if (resultado != null) {
                t.s.MontoGarantia = resultado.Monto;
                t.s.MontoGarantiaMonedaSistema = resultado.MontoMonedaSistema;
            }
        }
    }

    /** D-F3A-05: the rescate in transit is valued as an APORTE (type "APO"). */
    private void valorizarTransitoSalidaPreliminar(ConsultarCustodiaPortafolioContrato.Entrada entrada, List<Temp> salidaPreliminar) throws Exception {
        CalcularMontoNumeroCuota.EntradaCalcularMontoNumeroCuota entradaCalculo = new CalcularMontoNumeroCuota.EntradaCalcularMontoNumeroCuota();
        entradaCalculo.p_IdEmpresa = entrada.p_IdEmpresa;
        entradaCalculo.p_Series = salidaPreliminar.stream().filter(t -> BigDecimal.ZERO.compareTo(t.s.CantidadTransSolicitudesRescate) != 0)
                .map(t -> serie(entrada, t.idInterno * FACTOR_RESCATE, t.s.IdSerieFondo, CALCULO_POR_CUOTA, BigDecimal.ZERO, t.s.CantidadTransSolicitudesRescate,
                        t.s.IdMonedaFondo, TIPO_SOLICITUD_APORTE))
                .collect(Collectors.toList());

        Map<Long, CalcularMontoNumeroCuota.Resultado> hash = cuotas.logica(entradaCalculo).P_OUT_RESULTADO.stream().collect(Collectors.toMap(r -> r.IdInterno, r -> r));

        for (Temp t : salidaPreliminar) {
            CalcularMontoNumeroCuota.Resultado resultado = hash.get(t.idInterno * FACTOR_RESCATE);
            if (resultado != null) {
                t.s.MontoTransSolicitudesRescate = resultado.MontoMonedaSistema;
            }
        }
    }

    // ---- value of the quota in system currency (FM-CPF-13)

    private Map<Long, ParidadContrato.CalculoSalida> obtenerValoresCuotasMonedaSistema(ConsultarCustodiaPortafolioContrato.Entrada entrada, List<Temp> salidaPreliminar)
            throws Exception {
        ParidadContrato.CalcularParidadEntrada calcularParidadEntrada = new ParidadContrato.CalcularParidadEntrada();
        calcularParidadEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
        calcularParidadEntrada.p_IdTrabajo = 2L;

        String calculaValorCuotaDiaSiguiente = configuracion.texto(entrada.p_IdEmpresa, "PLATAFORMA", "PLA.CalculaValorCuotaDiaSiguiente");

        Map<String, String> fechasValorCuotaDiaSiguiente = obtenerDiasSiguiente(entrada.p_IdEmpresa, salidaPreliminar);

        boolean usaDiaSiguiente = "S".equals(calculaValorCuotaDiaSiguiente);
        calcularParidadEntrada.p_CalculosEntrada = salidaPreliminar.stream().map(t -> {
            ParidadContrato.CalculoEntrada calculoEntrada = new ParidadContrato.CalculoEntrada();
            calculoEntrada.IdTrabajo = 2L;
            calculoEntrada.IdInterno = t.idInterno;
            if (usaDiaSiguiente) {
                String fechaValorCuotaDiaSiguiente = fechasValorCuotaDiaSiguiente.get(t.s.FechaValorCuota);
                calculoEntrada.Fecha = fechaValorCuotaDiaSiguiente != null ? fechaValorCuotaDiaSiguiente : entrada.p_FechaConsulta;
            } else {
                calculoEntrada.Fecha = entrada.p_FechaConsulta;
            }
            calculoEntrada.IdMonedaOrigen = t.s.IdMonedaFondo;
            calculoEntrada.IdMonedaDestino = t.idMonedaSistema;
            calculoEntrada.ValorCambioLocalModificado = BigDecimal.ZERO;
            calculoEntrada.MontoOrigen = t.s.ValorCuota;
            calculoEntrada.IdUso = "";
            return calculoEntrada;
        }).collect(Collectors.toList());

        ParidadContrato.CalcularParidadSalida calculoSalida = ParidadContrato.calcular(paridad, calcularParidadEntrada);
        return calculoSalida.p_out_CalculosSalida.stream().collect(Collectors.toMap(r -> r.IdInterno, r -> r));
    }

    /** The company is ALWAYS queried (D-F3A-07), then the next working day of every distinct {@code FechaValorCuota} (D-F3A-29: the legacy Calendar arithmetic). */
    private Map<String, String> obtenerDiasSiguiente(Long idEmpresa, List<Temp> salidaPreliminar) throws Exception {
        List<String> fechas = salidaPreliminar.stream().map(t -> t.s.FechaValorCuota).distinct().collect(Collectors.toList());

        InformacionEmpresa empresa = empresas.informacion(idEmpresa);
        Long idPaisEmpresa = empresa.IdPais;

        ObtenerDiaSiguiente.InObtenerDiaSiguiente in = new ObtenerDiaSiguiente.InObtenerDiaSiguiente();
        in.p_diaHabil = SI;
        in.p_soloCaeDiaHabil = SI;
        in.p_fechas = fechas.stream().map(fecha -> {
            ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn f = new ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn();
            f.fechaInicial = fecha;
            f.numeroDias = 1L;
            f.pais = idPaisEmpresa;
            return f;
        }).collect(Collectors.toList());
        ObtenerDiaSiguiente.OutObtenerDiaSiguiente out = diaSiguiente.logica(in);

        return out.p_out_fechas.stream().collect(Collectors.toMap(f -> f.fechaInicial, f -> f.fechaCalculada));
    }

    // ---- output

    private List<ConsultarCustodiaPortafolioContrato.SalidaCustodia> obtenerSalidaCustodia(Long idEmpresa, List<Temp> salidaPreliminar,
            Map<Long, ParidadContrato.CalculoSalida> valoresCuotaMonedaSistema) {
        String monedaSistema = configuracion.texto(idEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
        return salidaPreliminar.stream().map(temp -> {
            ParidadContrato.CalculoSalida valorCuotaMonedaSistema = valoresCuotaMonedaSistema.get(temp.idInterno);
            ConsultarCustodiaPortafolioContrato.SalidaCustodia salida = temp.s;
            salida.ValorCuotaMonedaSistema = valorCuotaMonedaSistema != null && valorCuotaMonedaSistema.MontoDestino != null ? valorCuotaMonedaSistema.MontoDestino : BigDecimal.ZERO;
            try {
                salida.MontoMonedaSistema = formato.formatear(salida.CantidadCustodia.multiply(salida.ValorCuotaMonedaSistema), FormatoMoneda.MONTO, monedaSistema);
                salida.MontoGarantiaMonedaSistema = formato.formatear(salida.CantidadGarantia.multiply(salida.ValorCuotaMonedaSistema), FormatoMoneda.MONTO, monedaSistema);
            } catch (Exception e) {
                salida.MontoMonedaSistema = salida.CantidadCustodia.multiply(salida.ValorCuotaMonedaSistema);
                salida.MontoGarantiaMonedaSistema = salida.CantidadGarantia.multiply(salida.ValorCuotaMonedaSistema);
            }
            return salida;
        }).collect(Collectors.toList());
    }
}
