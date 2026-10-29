package cl.bcs.fondomutuo.comisiones;

import static cl.bcs.fondomutuo.cierre.ConstantesCierre.COMISION_ENTRADA;
import static cl.bcs.fondomutuo.cierre.ConstantesCierre.COMISION_SALIDA;
import static cl.bcs.fondomutuo.cierre.ConstantesCierre.NO;
import static cl.bcs.fondomutuo.cierre.ConstantesCierre.REINVERSION;
import static cl.bcs.fondomutuo.cierre.ConstantesCierre.TIPO_SOLICITUD_APORTE;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cl.bcs.fondomutuo.cierre.FechaLegacy;
import cl.bcs.fondomutuo.cierre.ListaHash;
import cl.bcs.fondomutuo.cierre.SerieFondoSolicitudes;
import cl.bcs.fondomutuo.cierre.SeriesFondoPort;
import cl.bcs.fondomutuo.cierre.contrato.FlujoCustodiaContrato;
import cl.bcs.fondomutuo.cierre.contrato.FlujoCustodiaContrato.FlujoCustodiaDetallado;
import cl.bcs.fondomutuo.cierre.contrato.FlujoCustodiaContrato.FlujoCustodiaEntrada;
import cl.bcs.fondomutuo.cierre.contrato.FlujoCustodiaContrato.FlujoCustodiaSalida;
import cl.bcs.fondomutuo.cierre.contrato.FlujoCustodiaContrato.FlujoEsAgrupadoPor;
import cl.bcs.fondomutuo.cierre.contrato.FlujoCustodiaContrato.FlujoFiltrosBusqueda;
import cl.bcs.fondomutuo.cierre.contrato.FlujoCustodiaContrato.FlujoUso;
import cl.bcs.fondomutuo.cierre.contrato.ParidadContrato;
import cl.bcs.fondomutuo.cierre.contrato.ParidadContrato.CalcularParidadEntrada;
import cl.bcs.fondomutuo.cierre.contrato.ParidadContrato.CalcularParidadSalida;
import cl.bcs.fondomutuo.cierre.contrato.ParidadContrato.CalculoEntrada;
import cl.bcs.fondomutuo.cierre.contrato.ParidadContrato.CalculoSalida;
import cl.bcs.fondomutuo.cierre.contrato.PreciosTasasContrato;
import cl.bcs.fondomutuo.cierre.contrato.PreciosTasasContrato.PreciosTasas;
import cl.bcs.fondomutuo.kit.InformacionEmpresa;
import cl.bcs.fondomutuo.kit.MensajeSalida;
import cl.bcs.fondomutuo.port.out.http.CustodiaPort;
import cl.bcs.fondomutuo.port.out.http.ParidadPort;
import cl.bcs.fondomutuo.port.out.http.PreciosTasasPort;
import cl.bcs.fondomutuo.port.out.kit.ConfiguracionSistemaPort;
import cl.bcs.fondomutuo.port.out.kit.EmpresaPort;

/**
 * Port of legacy {@code cl.bcs.fondomutuo.CalcularComisiones} (600 LOC; rules FM-COM-01..06), bug for bug. Plain Java over ports: configuration, company,
 * series and commission brackets (reads), and the H ports {@code CustodiaPort} (custody lots), {@code PreciosTasasPort} (quota value) and {@code ParidadPort}
 * (currency conversion). Kept as the legacy: {@code EsTraspaso} fixed to "N" (D-COM-1), {@code new BigDecimal(1.19)} and the 365-day base (D-COM-2),
 * the regressive {@code CantidadCarteras} counter (D-COM-3), {@code CantidadComision} of the last lot only while {@code Comision} is the sum (D-COM-4), a
 * missing conversion valuing 1 (D-COM-5), contributions consuming the custody lots and mutating {@code InCustodia.Cantidad} (D-COM-6), the day count by
 * millisecond division (here whole days between dates, identical under UTC). Logging removed.
 */
public class CalcularComisiones {

    private final ConfiguracionSistemaPort configuracion;
    private final EmpresaPort empresas;
    private final SeriesFondoPort seriesFondo;
    private final ComisionesPort comisiones;
    private final CustodiaPort custodia;
    private final PreciosTasasPort precios;
    private final ParidadPort paridad;

    public CalcularComisiones(ConfiguracionSistemaPort configuracion, EmpresaPort empresas, SeriesFondoPort seriesFondo, ComisionesPort comisiones,
            CustodiaPort custodia, PreciosTasasPort precios, ParidadPort paridad) {
        this.configuracion = configuracion;
        this.empresas = empresas;
        this.seriesFondo = seriesFondo;
        this.comisiones = comisiones;
        this.custodia = custodia;
        this.precios = precios;
        this.paridad = paridad;
    }

    public static class EntradaCalcularComisiones {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_SimularOperacion;
        public String p_UsaReglasNegocio;
        public List<InCustodia> p_DatosCustodia;

        public EntradaCalcularComisiones() {
            this.p_DatosCustodia = new ArrayList<InCustodia>();
        }
    }

    public static class InCustodia {
        public Long IdInterno;
        public String IdSerieFondo;
        public Long IdInstrumento;
        public Long IdCliente;
        public Long IdPortafolio;
        public BigDecimal Cantidad;
        public String IdMonedaFondo;
        public String IdMonedaOperacion;
        public String TipoSolicitud;
        public String IdTipoMovimientoFondoMutuo;
        public String FechaValorCuota;
        public String FechaCurse;
        public String EsTraspaso;
    }

    public static class SalidaCalcularComisiones {
        public Long P_OUT_ERRORESCONTADOR;
        public List<MensajeSalida> P_OUT_MENSAJES;
        public List<ResultadoCalcularComision> P_OUT_RESULTADO;

        public SalidaCalcularComisiones() {
            this.P_OUT_ERRORESCONTADOR = 0L;
            this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
            this.P_OUT_RESULTADO = new ArrayList<ResultadoCalcularComision>();
        }
    }

    public static class ResultadoCalcularComision {
        public Long IdInterno;
        public String IdSerieFondo;
        public Long IdCliente;
        public Long IdPortafolio;
        public BigDecimal Comision;
        public BigDecimal ComisionLocal;
        public BigDecimal ComisionMonedaSistema;
        public BigDecimal ValorPivoteLocalConversionLocal;
        public BigDecimal ValorPivoteSistemaConversionSistema;
        public String TipoSolicitud;
        public String IdTipoMovimientoFondoMutuo;
        public String EsAPV;
        public String EsTraspaso;
        public int Flujo;
        public BigDecimal CantidadComision;
        public BigDecimal MontoComisionMonedaLocal;
        public String TipoComision;
    }

    /** Lot of custody consumed by a request (legacy {@code CarterasProceso}). */
    public static class CarterasProceso {
        public Long IdInterno;
        public Long IdInstrumento;
        public String IdSerieFondo;
        public BigDecimal CantidadCartera;
        public Long IdCliente;
        public Long IdPortafolio;
        public LocalDate FechaIngresoCartera;
        public LocalDate FechaIngresoInversion;
        public String IdConcepto;
        public BigDecimal ValorCuota;
    }

    public SalidaCalcularComisiones logica(EntradaCalcularComisiones entrada) throws Exception {
        SalidaCalcularComisiones out = new SalidaCalcularComisiones();
        String fechaSistema = configuracion.texto(entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
        String usoValorCuotaActual;
        try {
            usoValorCuotaActual = configuracion.texto(entrada.p_IdEmpresa, "FONDOMUTUO", "CALCULAR_COMISION_VALOR_CUOTA_ACTUAL");
        } catch (Exception e) {
            usoValorCuotaActual = "S";
        }

        LocalDate dFechaSistema = FechaLegacy.aFecha(fechaSistema);
        InformacionEmpresa empresa = empresas.informacion(entrada.p_IdEmpresa);
        Long idPaisEmpresa = empresa.IdPais;
        FlujoCustodiaEntrada flujoCustodiaEntrada = new FlujoCustodiaEntrada();
        flujoCustodiaEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
        flujoCustodiaEntrada.p_IdUsuario = entrada.p_IdUsuario;
        flujoCustodiaEntrada.p_IdModulo = entrada.p_IdModulo;
        flujoCustodiaEntrada.p_FechaSaldo = fechaSistema;
        flujoCustodiaEntrada.p_IdPais = idPaisEmpresa;
        flujoCustodiaEntrada.p_DiasProyeccion = 0;
        flujoCustodiaEntrada.p_DiaHabil = "S";
        flujoCustodiaEntrada.p_ValorizarFlujo = "N";

        List<Long> clientes = new ArrayList<Long>();
        List<Long> portafolios = new ArrayList<Long>();
        List<Long> instrumentos = new ArrayList<Long>();
        List<String> series = new ArrayList<String>();

        for (InCustodia cartera : entrada.p_DatosCustodia) {
            FlujoUso uso = new FlujoUso();
            uso.UsoTipoOperacion = "RESCFM";
            uso.IdCliente = cartera.IdCliente;
            uso.IdPortafolio = cartera.IdPortafolio;
            uso.BusquedaEspecifica = "";
            uso.IdTipoInstrumentoEspecifico = "";
            FlujoFiltrosBusqueda busqueda = new FlujoFiltrosBusqueda();
            busqueda.IdClasificacionTipoInstrumento = "FM";
            busqueda.IdInstrumento = cartera.IdInstrumento.intValue() == 0 ? null : cartera.IdInstrumento;
            flujoCustodiaEntrada.p_FiltrosBusqueda.add(busqueda);
            flujoCustodiaEntrada.p_Uso.add(uso);
            if (cartera.IdInstrumento != null && !instrumentos.contains(cartera.IdInstrumento)) {
                instrumentos.add(cartera.IdInstrumento);
            }
            if (!clientes.contains(cartera.IdCliente)) {
                clientes.add(cartera.IdCliente);
            }
            if (!portafolios.contains(cartera.IdPortafolio)) {
                portafolios.add(cartera.IdPortafolio);
            }
            if (!series.contains(cartera.IdSerieFondo)) {
                series.add(cartera.IdSerieFondo);
            }
        }

        FlujoEsAgrupadoPor agrupador = new FlujoEsAgrupadoPor();
        agrupador.ClasificacionTipoInstrumento = "S";
        agrupador.TipoInstrumento = "S";
        agrupador.Instrumento = "S";
        agrupador.Emisor = "N";
        agrupador.Nemotecnico = "N";
        agrupador.NemoSVS = "N";
        agrupador.FechaEmisionInstrumento = "N";
        agrupador.FechaVencimientoInstrumento = "N";
        agrupador.FechaLiquidacionInstrumento = "N";
        agrupador.TipoReajuste = "N";
        agrupador.EsFungible = "N";
        agrupador.CodigoIFRS = "N";
        agrupador.Boveda = "N";
        agrupador.Bolsa = "N";
        agrupador.TipoOperacion = "N";
        agrupador.Mesa = "N";
        agrupador.FolioRegistro = "N";
        agrupador.ItemOrigen = "N";
        flujoCustodiaEntrada.p_EsAgrupadoPor.add(agrupador);

        FlujoCustodiaSalida salida = FlujoCustodiaContrato.consultar(custodia, flujoCustodiaEntrada);

        salida.p_out_FlujoDetallado.sort(new Comparator<FlujoCustodiaDetallado>() {
            @Override
            public int compare(FlujoCustodiaDetallado mov1, FlujoCustodiaDetallado mov2) {
                int c = Integer.signum(mov1.FechaOrigenCompra.compareTo(mov2.FechaOrigenCompra));
                if (c == 0) {
                    return 0;
                }
                return c == 1 ? 1 : -1;
            }
        });

        List<CarterasProceso> carteras = new ArrayList<CarterasProceso>();
        for (InCustodia cartera : entrada.p_DatosCustodia) {
            for (FlujoCustodiaDetallado detalle : salida.p_out_FlujoDetallado) {
                if (cartera.IdCliente.longValue() == detalle.IdCliente.longValue()
                        && cartera.IdPortafolio.longValue() == detalle.IdPortafolio.longValue()
                        && cartera.IdInstrumento.longValue() == detalle.IdInstrumento.longValue()) {
                    BigDecimal cuotaRetirar = BigDecimal.ZERO;
                    if (cartera.Cantidad.compareTo(detalle.Cantidad) != -1) {
                        cuotaRetirar = detalle.Cantidad;
                        cartera.Cantidad = cartera.Cantidad.subtract(detalle.Cantidad, MathContext.DECIMAL128);
                    } else if (cartera.Cantidad.compareTo(detalle.Cantidad) == -1) {
                        cuotaRetirar = cartera.Cantidad;
                        cartera.Cantidad = BigDecimal.ZERO;
                    }
                    CarterasProceso cc = new CarterasProceso();
                    cc.IdInterno = cartera.IdInterno;
                    cc.IdCliente = cartera.IdCliente;
                    cc.IdPortafolio = cartera.IdPortafolio;
                    cc.IdInstrumento = cartera.IdInstrumento;
                    cc.IdSerieFondo = cartera.IdSerieFondo;
                    cc.FechaIngresoCartera = detalle.FechaOrigenCompra;
                    cc.FechaIngresoInversion = detalle.FechaOperacion;
                    cc.IdConcepto = detalle.IdConcepto;
                    cc.CantidadCartera = cuotaRetirar;
                    cc.ValorCuota = detalle.PrecioCompra;
                    carteras.add(cc);
                    if (cartera.Cantidad.compareTo(BigDecimal.ZERO) == -1 || cartera.Cantidad.compareTo(BigDecimal.ZERO) == 0) {
                        break;
                    }
                }
            }
        }

        List<TramoComision> tramosComisiones = comisiones.tramos(entrada.p_IdEmpresa, series);
        List<SerieFondoSolicitudes> listaSeries = seriesFondo.series(entrada.p_IdEmpresa, series);
        Map<String, SerieFondoSolicitudes> informacionSeries = listaSeries.stream().collect(Collectors.toMap(serie -> serie.IdSerieFondo, serie -> serie));

        Map<Long, PreciosTasas> valoresCuota = obtenerValorCuota(entrada, informacionSeries);

        String idMonedaSistema = configuracion.texto(entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
        CalcularParidadEntrada calcularParidadEntrada = new CalcularParidadEntrada();

        // Cantidad de mapeos + 1 (paridad local, paridad sistema, tipo de cambio fondo-local relativo a local y tipo de cambio fondo-sistema relativo sistema)
        final int pendiente = 5;
        for (InCustodia custodia : entrada.p_DatosCustodia) {
            SerieFondoSolicitudes serie = informacionSeries.get(custodia.IdSerieFondo);
            ResultadoCalcularComision comision = new ResultadoCalcularComision();
            comision.IdInterno = custodia.IdInterno;
            comision.IdSerieFondo = custodia.IdSerieFondo;
            comision.IdCliente = custodia.IdCliente;
            comision.IdPortafolio = custodia.IdPortafolio;
            comision.Comision = BigDecimal.ZERO;
            comision.ComisionLocal = BigDecimal.ZERO;
            comision.ComisionMonedaSistema = BigDecimal.ZERO;
            comision.TipoSolicitud = custodia.TipoSolicitud;
            comision.IdTipoMovimientoFondoMutuo = custodia.IdTipoMovimientoFondoMutuo;
            comision.EsAPV = serie != null ? serie.EsAPV : NO;
            comision.EsTraspaso = NO;
            PreciosTasas precioTasas = valoresCuota.get(custodia.IdInterno);
            BigDecimal valorCuota = precioTasas != null && precioTasas.Precio != null ? precioTasas.Precio : BigDecimal.ZERO;
            String tipoComision = TIPO_SOLICITUD_APORTE.equalsIgnoreCase(comision.TipoSolicitud) ? COMISION_ENTRADA : COMISION_SALIDA;
            if (!REINVERSION.equals(comision.IdTipoMovimientoFondoMutuo) && NO.equals(comision.EsAPV) && NO.equals(comision.EsTraspaso)) {
                int CantidadCarteras = carteras.size();
                BigDecimal CantidadCarteraTotal = BigDecimal.ZERO;
                for (CarterasProceso cartera : carteras) {
                    if (cartera.IdCliente.longValue() == custodia.IdCliente.longValue()
                            && cartera.IdPortafolio.longValue() == custodia.IdPortafolio.longValue()
                            && cartera.IdInstrumento.longValue() == custodia.IdInstrumento.longValue()
                            && cartera.IdInterno.longValue() == custodia.IdInterno.longValue()) {

                        if (NO.equalsIgnoreCase(usoValorCuotaActual) && custodia.TipoSolicitud.equalsIgnoreCase("RES")) {
                            valorCuota = cartera.ValorCuota;
                        }
                        for (TramoComision tramo : tramosComisiones) {
                            if (tramo.TipoComision.equalsIgnoreCase(tipoComision)
                                    && tramo.IdSerieFondo.equalsIgnoreCase(comision.IdSerieFondo)
                                    && !tramo.TipoComision.equalsIgnoreCase("PER")) {
                                int dias = 0;
                                if (cartera.IdConcepto.equals("APOREI")) {
                                    dias = numeroDiasEntreDosFechas(cartera.FechaIngresoCartera, FechaLegacy.fechaSql(custodia.FechaCurse));
                                } else {
                                    if (custodia.FechaCurse != null && !custodia.FechaCurse.isEmpty()) {
                                        dias = numeroDiasEntreDosFechas(cartera.FechaIngresoInversion, FechaLegacy.fechaSql(custodia.FechaCurse));
                                    } else {
                                        dias = numeroDiasEntreDosFechas(cartera.FechaIngresoCartera, dFechaSistema);
                                    }
                                }
                                comision.Flujo = dias;
                                if (dias >= tramo.DiasDesde && dias <= tramo.DiasHasta) {
                                    BigDecimal porcentaje = tramo.PorcentajeComision.divide(new BigDecimal(100), MathContext.DECIMAL128);
                                    BigDecimal montoComision = cartera.CantidadCartera
                                            .multiply(valorCuota, MathContext.DECIMAL128)
                                            .multiply(porcentaje, MathContext.DECIMAL128)
                                            .setScale(8, RoundingMode.HALF_DOWN);
                                    comision.Comision = comision.Comision.add(montoComision, MathContext.DECIMAL128);
                                    comision.ComisionLocal = comision.Comision;
                                }
                            } else if (tramo.IdSerieFondo.equalsIgnoreCase(comision.IdSerieFondo)
                                    && tramo.TipoComision.equalsIgnoreCase("PER")) {
                                CantidadCarteras = CantidadCarteras - 1;
                                BigDecimal porcentaje = tramo.PorcentajeComision.divide(new BigDecimal(100), MathContext.DECIMAL128);

                                BigDecimal comisionDiaria = porcentaje
                                        .divide(new BigDecimal(365), MathContext.DECIMAL128)
                                        .multiply(new BigDecimal(1.19), MathContext.DECIMAL128)
                                        .setScale(8, RoundingMode.HALF_DOWN);

                                int TrimestreActual = trimestreSegunFecha(dFechaSistema);
                                int TrimestreCartera = trimestreSegunFecha(cartera.FechaIngresoCartera);
                                int diasPermanencia = 0;
                                comision.TipoComision = tramo.TipoComision;

                                if (TrimestreActual != TrimestreCartera && dFechaSistema.compareTo(cartera.FechaIngresoCartera) > 0) {
                                    diasPermanencia = duracionTrimestre(cartera.FechaIngresoCartera);
                                } else {
                                    diasPermanencia = numeroDiasEntreDosFechas(cartera.FechaIngresoCartera, dFechaSistema);
                                }

                                comision.CantidadComision = cartera.CantidadCartera
                                        .multiply(comisionDiaria)
                                        .multiply(new BigDecimal(diasPermanencia), MathContext.DECIMAL128)
                                        .setScale(8, RoundingMode.HALF_DOWN);

                                CantidadCarteraTotal = CantidadCarteraTotal.add(comision.CantidadComision);
                                if (CantidadCarteras == 0) {
                                    comision.Comision = comision.Comision.add(CantidadCarteraTotal
                                            .multiply(valorCuota, MathContext.DECIMAL128), MathContext.DECIMAL128);

                                    comision.ComisionLocal = comision.Comision;
                                }
                            }
                        }
                    }
                }
            }

            CalculoEntrada calculoEntrada = new CalculoEntrada();
            calculoEntrada.IdTrabajo = 1L;
            calculoEntrada.IdInterno = (custodia.IdInterno * pendiente) + 1L;
            calculoEntrada.Fecha = fechaSistema;
            calculoEntrada.IdMonedaOrigen = custodia.IdMonedaFondo;
            calculoEntrada.IdMonedaDestino = custodia.IdMonedaOperacion;
            calculoEntrada.ValorCambioLocalModificado = BigDecimal.ZERO;
            calculoEntrada.MontoOrigen = comision.Comision;
            calculoEntrada.IdUso = "";
            calcularParidadEntrada.p_CalculosEntrada.add(calculoEntrada);

            CalculoEntrada calculoComisionMonedaSistema = new CalculoEntrada();
            calculoComisionMonedaSistema.IdTrabajo = 1L;
            calculoComisionMonedaSistema.IdInterno = (custodia.IdInterno * pendiente) + 2L;
            calculoComisionMonedaSistema.Fecha = fechaSistema;
            calculoComisionMonedaSistema.IdMonedaOrigen = custodia.IdMonedaFondo;
            calculoComisionMonedaSistema.IdMonedaDestino = idMonedaSistema;
            calculoComisionMonedaSistema.ValorCambioLocalModificado = BigDecimal.ZERO;
            calculoComisionMonedaSistema.MontoOrigen = comision.Comision;
            calculoComisionMonedaSistema.IdUso = "";
            calcularParidadEntrada.p_CalculosEntrada.add(calculoComisionMonedaSistema);

            CalculoEntrada tipoCambioFondoLocalRespectoLocal = new CalculoEntrada();
            tipoCambioFondoLocalRespectoLocal.IdTrabajo = 1L;
            tipoCambioFondoLocalRespectoLocal.IdInterno = (custodia.IdInterno * pendiente) + 3L;
            tipoCambioFondoLocalRespectoLocal.Fecha = fechaSistema;
            tipoCambioFondoLocalRespectoLocal.IdMonedaOrigen = custodia.IdMonedaOperacion;
            tipoCambioFondoLocalRespectoLocal.IdMonedaDestino = custodia.IdMonedaFondo;
            tipoCambioFondoLocalRespectoLocal.ValorCambioLocalModificado = BigDecimal.ZERO;
            tipoCambioFondoLocalRespectoLocal.MontoOrigen = BigDecimal.ONE;
            tipoCambioFondoLocalRespectoLocal.IdUso = "";
            calcularParidadEntrada.p_CalculosEntrada.add(tipoCambioFondoLocalRespectoLocal);

            CalculoEntrada tipoCambioFondoSistemaRespectoSistema = new CalculoEntrada();
            tipoCambioFondoSistemaRespectoSistema.IdTrabajo = 1L;
            tipoCambioFondoSistemaRespectoSistema.IdInterno = (custodia.IdInterno * pendiente) + 4L;
            tipoCambioFondoSistemaRespectoSistema.Fecha = fechaSistema;
            tipoCambioFondoSistemaRespectoSistema.IdMonedaOrigen = custodia.IdMonedaFondo;
            tipoCambioFondoSistemaRespectoSistema.IdMonedaDestino = idMonedaSistema;
            tipoCambioFondoSistemaRespectoSistema.ValorCambioLocalModificado = BigDecimal.ZERO;
            tipoCambioFondoSistemaRespectoSistema.MontoOrigen = BigDecimal.ONE;
            tipoCambioFondoSistemaRespectoSistema.IdUso = "";
            calcularParidadEntrada.p_CalculosEntrada.add(tipoCambioFondoSistemaRespectoSistema);

            out.P_OUT_RESULTADO.add(comision);
        }

        HashMap<Long, CalculoSalida> hcalculo = new HashMap<Long, CalculoSalida>();
        if (calcularParidadEntrada.p_CalculosEntrada.size() > 0) {
            calcularParidadEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
            calcularParidadEntrada.p_IdTrabajo = 1L;
            CalcularParidadSalida calculoSalida = ParidadContrato.calcular(paridad, calcularParidadEntrada);
            hcalculo = ListaHash.aHash(calculoSalida.p_out_CalculosSalida, c -> c.IdInterno);
            for (ResultadoCalcularComision comision : out.P_OUT_RESULTADO) {
                CalculoSalida calculoFondoLocal = hcalculo.get((comision.IdInterno * pendiente) + 1L);
                comision.ComisionLocal = calculoFondoLocal == null ? BigDecimal.ONE : calculoFondoLocal.MontoDestino;

                CalculoSalida calculoFondoSistema = hcalculo.get((comision.IdInterno * pendiente) + 2L);
                comision.ComisionMonedaSistema = calculoFondoSistema == null ? BigDecimal.ONE : calculoFondoSistema.MontoDestino;

                CalculoSalida calculoTipoCambioFondoLocalRespectoLocal = hcalculo.get((comision.IdInterno * pendiente) + 3L);
                comision.ValorPivoteLocalConversionLocal = calculoTipoCambioFondoLocalRespectoLocal == null ? BigDecimal.ONE : calculoTipoCambioFondoLocalRespectoLocal.MontoDestino;

                CalculoSalida calculoTipoCambioFondoSistemaRespectoSistema = hcalculo.get((comision.IdInterno) * pendiente + 4L);
                comision.ValorPivoteSistemaConversionSistema = calculoTipoCambioFondoSistemaRespectoSistema == null ? BigDecimal.ONE : calculoTipoCambioFondoSistemaRespectoSistema.MontoDestino;
            }
        }

        return out;
    }

    /** Whole days between two dates (legacy: the millisecond difference divided by 86 400 000, truncated toward zero; the same under UTC). */
    private int numeroDiasEntreDosFechas(LocalDate fecha1, LocalDate fecha2) {
        return (int) ChronoUnit.DAYS.between(fecha1, fecha2);
    }

    private int trimestreSegunFecha(LocalDate fecha) {
        int mes = fecha.getMonthValue();
        int trimestre = (mes + 2) / 3;
        return (int) trimestre;
    }

    /** Days from the first to the last day of the quarter of the date (day-of-year difference, as the legacy). */
    private int duracionTrimestre(LocalDate date) {
        int mesInicio = (date.getMonthValue() - 1) / 3 * 3;
        LocalDate calInicio = LocalDate.of(date.getYear(), mesInicio + 1, 1);
        LocalDate calFinal = LocalDate.of(date.getYear(), mesInicio + 3, 1);
        calFinal = calFinal.withDayOfMonth(calFinal.lengthOfMonth());
        int diaInicio = calInicio.getDayOfYear();
        int diaFinal = calFinal.getDayOfYear();
        int dias = diaFinal - diaInicio;
        return dias;
    }

    private Map<Long, PreciosTasas> obtenerValorCuota(EntradaCalcularComisiones entrada, Map<String, SerieFondoSolicitudes> informacionSeries) throws Exception {
        PreciosTasasContrato.Entrada entradaPrecioTasa = new PreciosTasasContrato.Entrada();

        Long paisSistema = configuracion.entero(entrada.p_IdEmpresa, "PLATAFORMA", "PLA.PaisSistema");

        entradaPrecioTasa.p_IdEmpresa = entrada.p_IdEmpresa;
        entradaPrecioTasa.p_IdModulo = entrada.p_IdModulo;
        entradaPrecioTasa.p_IdPais = paisSistema;
        entradaPrecioTasa.p_IdUsuario = entrada.p_IdUsuario;
        entradaPrecioTasa.p_DatosInstrumentos = entrada.p_DatosCustodia.stream()
                .map(sol -> {
                    SerieFondoSolicitudes serie = informacionSeries.get(sol.IdSerieFondo);
                    PreciosTasasContrato.DatosInstrumentos paramsRegIn = new PreciosTasasContrato.DatosInstrumentos();
                    paramsRegIn.FechaConsulta = PreciosTasasContrato.fechaConsulta(FechaLegacy.aTexto(FechaLegacy.aFecha(sol.FechaValorCuota)));
                    paramsRegIn.IdClasificacionTipoInstrumento = "FM";
                    paramsRegIn.IdInstrumento = sol.IdInstrumento;
                    paramsRegIn.IdMoneda = sol.IdMonedaFondo;
                    paramsRegIn.IdRegistro = sol.IdInterno;
                    paramsRegIn.IdTipoCriterioTasa = "0";
                    paramsRegIn.IdTipoInstrumento = serie != null ? serie.IdTipoInstrumento : "";
                    paramsRegIn.IdUso = "CARTERA";
                    paramsRegIn.Nemotecnico = sol.IdSerieFondo;
                    paramsRegIn.PlazoResidual = 0L;
                    paramsRegIn.TipoCartera = "0";
                    return paramsRegIn;
                })
                .collect(Collectors.toList());

        PreciosTasasContrato.Salida salidaPrecioTasa = PreciosTasasContrato.entregar(precios, entradaPrecioTasa);
        Map<Long, PreciosTasas> valoresCuota = salidaPrecioTasa.P_OUT_PRECIOSTASAS.stream().filter(precio -> precio.IdInstrumento != null)
                .collect(Collectors.toMap(instrumento -> instrumento.IdRegistro, precio -> precio));
        return valoresCuota;
    }
}
