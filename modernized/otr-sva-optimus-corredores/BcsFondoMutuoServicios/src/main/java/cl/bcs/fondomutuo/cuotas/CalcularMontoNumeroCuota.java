package cl.bcs.fondomutuo.cuotas;

import static cl.bcs.fondomutuo.cierre.ConstantesCierre.CALCULO_POR_MONTO;
import static cl.bcs.fondomutuo.cierre.ConstantesCierre.DOLAR;
import static cl.bcs.fondomutuo.cierre.ConstantesCierre.NO;
import static cl.bcs.fondomutuo.cierre.ConstantesCierre.SI;
import static cl.bcs.fondomutuo.cierre.ConstantesCierre.TIPO_SOLICITUD_APORTE;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cl.bcs.fondomutuo.cierre.FechaLegacy;
import cl.bcs.fondomutuo.cierre.FormatoMoneda;
import cl.bcs.fondomutuo.cierre.ListaHash;
import cl.bcs.fondomutuo.cierre.SerieFondoSolicitudes;
import cl.bcs.fondomutuo.cierre.SeriesFondoPort;
import cl.bcs.fondomutuo.cierre.contrato.ParidadContrato;
import cl.bcs.fondomutuo.cierre.contrato.ParidadContrato.CalcularParidadEntrada;
import cl.bcs.fondomutuo.cierre.contrato.ParidadContrato.CalcularParidadSalida;
import cl.bcs.fondomutuo.cierre.contrato.ParidadContrato.CalculoEntrada;
import cl.bcs.fondomutuo.cierre.contrato.ParidadContrato.CalculoSalida;
import cl.bcs.fondomutuo.cierre.contrato.PreciosTasasContrato;
import cl.bcs.fondomutuo.cierre.contrato.PreciosTasasContrato.PreciosTasas;
import cl.bcs.fondomutuo.port.out.http.ParidadPort;
import cl.bcs.fondomutuo.port.out.http.PreciosTasasPort;
import cl.bcs.fondomutuo.port.out.kit.CalendarioPort;
import cl.bcs.fondomutuo.port.out.kit.ConfiguracionSistemaPort;

/**
 * Port of legacy {@code cl.bcs.fondomutuo.CalcularMontoNumeroCuota} (552 LOC; rules FM-CUO-01..10), bug for bug. Plain Java: reads and collaborators go
 * through ports ({@code ConfiguracionSistemaPort}, {@code SeriesFondoPort}, {@code CuotasPort}, {@code CalendarioPort} for the currency formats, and the H ports
 * {@code PreciosTasasPort} / {@code ParidadPort}); one instance holds only its ports (the currency-format table is read once per call, no static state).
 * Kept as the legacy: the scale-sensitive {@code equals(BigDecimal.ZERO)} (D-CUO-1), the NullPointerException of a null {@code NumeroCuotas} (D-CUO-2), the
 * mixed HALF_DOWN / HALF_UP rounding (D-CUO-3), a null price map (D-CUO-6) and duplicated keys (D-CUO-7). Logging of the legacy removed; the
 * {@code parallelStream} loops of the legacy (no side effects beyond the element) are sequential here.
 */
public class CalcularMontoNumeroCuota {

    private final ConfiguracionSistemaPort configuracion;
    private final SeriesFondoPort seriesFondo;
    private final CuotasPort cuotas;
    private final CalendarioPort calendario;
    private final PreciosTasasPort precios;
    private final ParidadPort paridad;

    public CalcularMontoNumeroCuota(ConfiguracionSistemaPort configuracion, SeriesFondoPort seriesFondo, CuotasPort cuotas, CalendarioPort calendario,
            PreciosTasasPort precios, ParidadPort paridad) {
        this.configuracion = configuracion;
        this.seriesFondo = seriesFondo;
        this.cuotas = cuotas;
        this.calendario = calendario;
        this.precios = precios;
        this.paridad = paridad;
    }

    public static class EntradaCalcularMontoNumeroCuota {
        public Long p_IdEmpresa;
        public List<Serie> p_Series;

        public EntradaCalcularMontoNumeroCuota() {
            this.p_Series = new ArrayList<Serie>();
        }
    }

    public static class Serie {
        public Long IdInterno;
        public String Fecha;
        public String IdSerieFondo;
        public String CalculoPorMontoCuota;
        public BigDecimal Monto;
        public BigDecimal NumeroCuotas;
        public String IdMonedaFondo;
        public String IdMonedaInversion;
        public String TipoSolicitud;
        public String TipoFactorAjuste;
        public Long IdEmpresa;
        public BigDecimal Porcentaje;
        public String IdTipoMovimientoFondoMutuo;

        public Serie() {
            this.IdTipoMovimientoFondoMutuo = "";
        }
    }

    public static class SalidaCalcularMontoNumeroCuota {
        public List<Resultado> P_OUT_RESULTADO;

        public SalidaCalcularMontoNumeroCuota() {
            this.P_OUT_RESULTADO = new ArrayList<Resultado>();
        }
    }

    /** Row of {@code SelectValorCuotaTipoCambio.sql} (filled by column label; {@code ValorCuota} is not a column: it is set from the price). */
    public static class ValorCuotaTipoParidad {
        public String IdSerieFondo;
        public BigDecimal ValorCuota;
        public BigDecimal ValorDolarAporte;
        public BigDecimal ValorDolarRescate;
        public Long DecimalesCuota;
        public Long DecimalesValorCuota;
    }

    public static class SeriesValorCuotaTipoParidad {
        public Long IdInterno;
        public String Fecha;
        public String IdSerieFondo;
        public String CalculoPorMontoCuota;
        public BigDecimal Monto;
        public BigDecimal NumeroCuotas;
        public String IdMonedaFondo;
        public String IdMonedaInversion;
        public String TipoSolicitud;
        public String TipoFactorAjuste;
        public Long IdEmpresa;
        public BigDecimal MontoPorCuota;
        public BigDecimal ValorCuota;
        public BigDecimal TipoCambio;
        public Long DecimalesCuota;
        public Long DecimalesValorCuota;
        public String Diferido;
        public BigDecimal ValorParidad;
        public BigDecimal MontoDestino;
        public BigDecimal MontoMonedaSistema;
        public BigDecimal Porcentaje;
        public String FechaValorCuota;

        public SeriesValorCuotaTipoParidad() {
            this.ValorCuota = BigDecimal.ZERO;
            this.TipoCambio = BigDecimal.ZERO;
            this.ValorParidad = new BigDecimal(-1);
            this.MontoDestino = BigDecimal.ZERO;
            this.Porcentaje = BigDecimal.ZERO;
            this.DecimalesCuota = 0L;
            this.FechaValorCuota = "0001-01-01";
        }
    }

    public static class Resultado {
        public Long IdInterno;
        public String TipoSolicitud;
        public BigDecimal Monto;
        public BigDecimal NumeroCuotas;
        public BigDecimal ValorCuota;
        public BigDecimal MontoDestino;
        public BigDecimal ValorParidad;
        public BigDecimal ValorFactorAjuste;
        public BigDecimal CostoEnFactorAjuste;
        public String IdMonedaSistema;
        public BigDecimal MontoMonedaSistema;
        public String Resultado;
        public String CodigoMensaje;
        public BigDecimal Porcentaje;
        public Long DecimalesValorCuota;
        public Long DecimalesNumeroCuotas;
        public String FechaValorCuota;
    }

    public SalidaCalcularMontoNumeroCuota logica(EntradaCalcularMontoNumeroCuota entrada) throws Exception {
        SalidaCalcularMontoNumeroCuota out = new SalidaCalcularMontoNumeroCuota();
        FormatoMoneda formato = new FormatoMoneda(calendario);

        String usaDolarObservado = configuracion.texto(entrada.p_IdEmpresa, "FONDOMUTUO", "UsaDolarObservado");
        String idMoneda = configuracion.texto(entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");

        List<String> series = new ArrayList<String>();
        for (Serie serie : entrada.p_Series) {
            if (!series.contains(serie.IdSerieFondo)) {
                series.add(serie.IdSerieFondo);
            }
        }
        Map<Long, PreciosTasas> hashVCTP = obtenerPrecios(entrada);
        List<ValorCuotaTipoParidad> valorCuotaTipoParidad = cuotas.valorCuotaTipoCambio(entrada.p_IdEmpresa, series);

        if (valorCuotaTipoParidad.size() == 0) {

            for (Serie serie : entrada.p_Series) {
                Resultado resultadoErroneo = new Resultado();
                resultadoErroneo.IdInterno = serie.IdInterno;
                resultadoErroneo.CodigoMensaje = "ERROR_VALORCUOTA";
                resultadoErroneo.Resultado = "Error al obtener Valor Cuota: " + serie.IdSerieFondo;
                out.P_OUT_RESULTADO.add(resultadoErroneo);
            }
            return out;
        }

        HashMap<String, ValorCuotaTipoParidad> hashVCTPDecimales = ListaHash.aHash(valorCuotaTipoParidad, v -> v.IdSerieFondo);

        if (hashVCTP.size() == 0 || hashVCTPDecimales.size() == 0) {

            for (String serie : series) {
                Resultado resultadoErroneo = new Resultado();
                resultadoErroneo.CodigoMensaje = "ERROR_VALORCUOTA";
                resultadoErroneo.Resultado = "Error al obtener Valor Cuota: " + serie;
                out.P_OUT_RESULTADO.add(resultadoErroneo);
            }
            return out;
        }

        List<SeriesValorCuotaTipoParidad> seriesValorCuotaTipoParidad = new ArrayList<SeriesValorCuotaTipoParidad>();

        HashMap<Long, CalculoSalida> paridades = new HashMap<Long, CalculoSalida>();
        HashMap<Long, CalculoSalida> montosParidad = new HashMap<Long, CalculoSalida>();

        CalcularParidadEntrada calcularParidadEntrada = new CalcularParidadEntrada();
        calcularParidadEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
        calcularParidadEntrada.p_IdTrabajo = 1L;
        for (Serie serie : entrada.p_Series) {
            SeriesValorCuotaTipoParidad serieValorCuotaTipoParidad = new SeriesValorCuotaTipoParidad();
            copiarSerie(serie, serieValorCuotaTipoParidad);
            PreciosTasas VCTPValorCuota = hashVCTP.get(serie.IdInterno);
            ValorCuotaTipoParidad valorCuota = hashVCTPDecimales.get(serie.IdSerieFondo);
            if (valorCuota != null && VCTPValorCuota != null) {
                valorCuota.IdSerieFondo = serie.IdSerieFondo;
                valorCuota.ValorCuota = VCTPValorCuota.Precio;
                serieValorCuotaTipoParidad.FechaValorCuota = VCTPValorCuota.FechaFuente != null ? VCTPValorCuota.FechaFuente.substring(0, 10) : "0001-01-01";
                serieValorCuotaTipoParidad.ValorCuota = valorCuota.ValorCuota != null ? valorCuota.ValorCuota : BigDecimal.ZERO;
                serieValorCuotaTipoParidad.DecimalesCuota = valorCuota.DecimalesCuota;
                serieValorCuotaTipoParidad.DecimalesValorCuota = valorCuota.DecimalesValorCuota;
                serieValorCuotaTipoParidad.TipoCambio = TIPO_SOLICITUD_APORTE.equalsIgnoreCase(serieValorCuotaTipoParidad.TipoSolicitud) ? valorCuota.ValorDolarAporte : valorCuota.ValorDolarRescate;
            }
            serieValorCuotaTipoParidad.Diferido = NO;

            if (serieValorCuotaTipoParidad.NumeroCuotas != null) {
                serieValorCuotaTipoParidad.MontoPorCuota = serieValorCuotaTipoParidad.ValorCuota.multiply(serieValorCuotaTipoParidad.NumeroCuotas, MathContext.DECIMAL128);
            } else {
                serieValorCuotaTipoParidad.MontoPorCuota = serie.Monto;
            }

            serieValorCuotaTipoParidad.Porcentaje = serie.Porcentaje;
            if (!serieValorCuotaTipoParidad.IdMonedaInversion.equalsIgnoreCase(serieValorCuotaTipoParidad.IdMonedaFondo)) {
                serieValorCuotaTipoParidad.Diferido = SI;
                CalculoEntrada calculoEntrada = new CalculoEntrada();
                calculoEntrada.IdTrabajo = 1L;
                calculoEntrada.IdInterno = serieValorCuotaTipoParidad.IdInterno;
                calculoEntrada.Fecha = serieValorCuotaTipoParidad.Fecha;
                calculoEntrada.IdMonedaOrigen = CALCULO_POR_MONTO.equalsIgnoreCase(serieValorCuotaTipoParidad.CalculoPorMontoCuota) ? serieValorCuotaTipoParidad.IdMonedaInversion : serieValorCuotaTipoParidad.IdMonedaFondo;
                calculoEntrada.IdMonedaDestino = CALCULO_POR_MONTO.equalsIgnoreCase(serieValorCuotaTipoParidad.CalculoPorMontoCuota) ? serieValorCuotaTipoParidad.IdMonedaFondo : serieValorCuotaTipoParidad.IdMonedaInversion;
                calculoEntrada.ValorCambioLocalModificado = BigDecimal.ZERO;
                calculoEntrada.MontoOrigen = CALCULO_POR_MONTO.equalsIgnoreCase(serieValorCuotaTipoParidad.CalculoPorMontoCuota) ? serieValorCuotaTipoParidad.Monto : serieValorCuotaTipoParidad.MontoPorCuota;
                calculoEntrada.IdUso = "";
                calcularParidadEntrada.p_CalculosEntrada.add(calculoEntrada);
            }
            seriesValorCuotaTipoParidad.add(serieValorCuotaTipoParidad);
        }

        if (calcularParidadEntrada.p_CalculosEntrada.size() > 0) {
            CalcularParidadSalida calculoSalida = ParidadContrato.calcular(paridad, calcularParidadEntrada);
            paridades = ListaHash.aHash(calculoSalida.p_out_CalculosSalida, c -> c.IdInterno);
        }

        // CALCULO MONEDA SISTEMA
        calcularParidadEntrada = new CalcularParidadEntrada();
        calcularParidadEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
        calcularParidadEntrada.p_IdTrabajo = 2L;

        for (SeriesValorCuotaTipoParidad serie : seriesValorCuotaTipoParidad) {
            if (serie.ValorCuota.equals(BigDecimal.ZERO)) {
                continue;
            }

            if (SI.equalsIgnoreCase(serie.Diferido)) {
                CalculoSalida paridadSerie = paridades.get(serie.IdInterno);
                BigDecimal montoDestino = BigDecimal.ZERO;
                if (NO.equalsIgnoreCase(usaDolarObservado)) {
                    if (DOLAR.equalsIgnoreCase(serie.IdMonedaFondo)) {
                        serie.ValorParidad = serie.TipoCambio;
                        montoDestino = CALCULO_POR_MONTO.equalsIgnoreCase(serie.CalculoPorMontoCuota) ? serie.Monto.divide(serie.ValorParidad, MathContext.DECIMAL128) : serie.MontoPorCuota.multiply(serie.ValorParidad, MathContext.DECIMAL128);
                    } else if (idMoneda.equalsIgnoreCase(serie.IdMonedaFondo) && DOLAR.equalsIgnoreCase(serie.IdMonedaInversion)) {
                        serie.ValorParidad = serie.TipoCambio;
                        montoDestino = CALCULO_POR_MONTO.equalsIgnoreCase(serie.CalculoPorMontoCuota) ? serie.Monto.multiply(serie.ValorParidad, MathContext.DECIMAL128) : serie.MontoPorCuota.divide(serie.ValorParidad, MathContext.DECIMAL128);
                    } else if (paridadSerie != null) {
                        serie.ValorParidad = paridadSerie.FactorCambioLocal;
                        montoDestino = paridadSerie.MontoDestino;
                    }
                } else if (paridadSerie != null) {
                    serie.ValorParidad = paridadSerie.FactorCambioLocal;
                    montoDestino = paridadSerie.MontoDestino;
                }

                if (CALCULO_POR_MONTO.equalsIgnoreCase(serie.CalculoPorMontoCuota)) {
                    serie.NumeroCuotas = montoDestino.divide(serie.ValorCuota, MathContext.DECIMAL128).setScale(serie.DecimalesCuota.intValue(), RoundingMode.HALF_DOWN);
                    BigDecimal monto = serie.Monto;
                    serie.Monto = formato.formatear(montoDestino, FormatoMoneda.MONTO, serie.IdMonedaFondo);
                    serie.MontoDestino = monto;

                } else {
                    serie.Monto = formato.formatear(serie.MontoPorCuota, FormatoMoneda.MONTO, serie.IdMonedaFondo);
                    serie.MontoDestino = montoDestino;
                }
            } else {
                serie.ValorParidad = BigDecimal.ONE;
                if (CALCULO_POR_MONTO.equalsIgnoreCase(serie.CalculoPorMontoCuota)) {
                    serie.NumeroCuotas = serie.Monto.divide(serie.ValorCuota, MathContext.DECIMAL128).setScale(serie.DecimalesCuota.intValue(), RoundingMode.HALF_DOWN);
                    serie.Monto = formato.formatear(serie.Monto, FormatoMoneda.MONTO, serie.IdMonedaFondo);
                } else {
                    if (serie.MontoPorCuota.compareTo(BigDecimal.ZERO) == 0 && serie.ValorCuota.compareTo(BigDecimal.ZERO) != 0) {
                        serie.NumeroCuotas = serie.Monto.divide(serie.ValorCuota, MathContext.DECIMAL128).setScale(serie.DecimalesCuota.intValue(), RoundingMode.HALF_DOWN);
                        serie.Monto = formato.formatear(serie.Monto, FormatoMoneda.MONTO, serie.IdMonedaFondo);

                    } else {
                        serie.Monto = formato.formatear(serie.MontoPorCuota, FormatoMoneda.MONTO, serie.IdMonedaFondo);
                    }

                }
                serie.MontoDestino = serie.Monto;
                serie.MontoMonedaSistema = serie.Monto;
            }

            int s = serie.IdInterno > 0 ? 1 : -1;
            if (idMoneda.equalsIgnoreCase(serie.IdMonedaFondo)) {
                serie.MontoMonedaSistema = serie.Monto;
            } else {
                CalculoEntrada calculoEntrada = new CalculoEntrada();
                calculoEntrada.IdTrabajo = 2L;
                calculoEntrada.IdInterno = (serie.IdInterno * 4) + (1 * s);
                calculoEntrada.Fecha = serie.Fecha;
                calculoEntrada.IdMonedaOrigen = serie.IdMonedaFondo;
                calculoEntrada.IdMonedaDestino = idMoneda;
                calculoEntrada.ValorCambioLocalModificado = BigDecimal.ZERO;
                calculoEntrada.MontoOrigen = serie.Monto;
                calculoEntrada.IdUso = "";
                calcularParidadEntrada.p_CalculosEntrada.add(calculoEntrada);
            }

            CalculoEntrada calculoEntrada = new CalculoEntrada();
            calculoEntrada.IdTrabajo = 2L;
            calculoEntrada.IdInterno = (serie.IdInterno * 4) + (2 * s);
            calculoEntrada.Fecha = serie.Fecha;
            calculoEntrada.IdMonedaOrigen = serie.IdMonedaFondo;
            calculoEntrada.IdMonedaDestino = serie.TipoFactorAjuste;
            calculoEntrada.ValorCambioLocalModificado = BigDecimal.ZERO;
            calculoEntrada.MontoOrigen = serie.Monto;
            calculoEntrada.IdUso = "";
            calcularParidadEntrada.p_CalculosEntrada.add(calculoEntrada);

            CalculoEntrada calculoEntradaUF = new CalculoEntrada();
            calculoEntradaUF.IdTrabajo = 2L;
            calculoEntradaUF.IdInterno = (serie.IdInterno * 4) + (3 * s);
            calculoEntradaUF.Fecha = serie.Fecha;
            calculoEntradaUF.IdMonedaOrigen = serie.TipoFactorAjuste;
            calculoEntradaUF.IdMonedaDestino = idMoneda;
            calculoEntradaUF.ValorCambioLocalModificado = BigDecimal.ZERO;
            calculoEntradaUF.MontoOrigen = BigDecimal.ONE;
            calculoEntradaUF.IdUso = "";
            calcularParidadEntrada.p_CalculosEntrada.add(calculoEntradaUF);

        }

        CalcularParidadSalida calculoSalida = ParidadContrato.calcular(paridad, calcularParidadEntrada);

        if (calcularParidadEntrada.p_CalculosEntrada.size() > 0) {
            montosParidad = ListaHash.aHash(calculoSalida.p_out_CalculosSalida, c -> c.IdInterno);
        }

        for (SeriesValorCuotaTipoParidad serie : seriesValorCuotaTipoParidad) {
            int s = serie.IdInterno > 0 ? 1 : -1;
            CalculoSalida montoMonedaSistema = montosParidad.get((serie.IdInterno * 4) + (1 * s));
            CalculoSalida montoEnUF = montosParidad.get((serie.IdInterno * 4) + (2 * s));
            CalculoSalida uf = montosParidad.get((serie.IdInterno * 4) + (3 * s));
            Resultado resultado = new Resultado();
            resultado.IdInterno = serie.IdInterno;
            resultado.TipoSolicitud = serie.TipoSolicitud;
            // EN CASO DE NO DEFINIRSE EL NUMERO DE DE CUOTAS SE PROCEDE A SU CALCULO
            resultado.Monto = formato.formatear(serie.Monto, FormatoMoneda.MONTO, serie.IdMonedaFondo);
            resultado.DecimalesNumeroCuotas = serie.DecimalesCuota != null ? serie.DecimalesCuota : 0L;
            resultado.DecimalesValorCuota = serie.DecimalesValorCuota != null ? serie.DecimalesValorCuota : 0L;
            if (serie.NumeroCuotas != null) {
                resultado.NumeroCuotas = serie.NumeroCuotas.setScale(resultado.DecimalesNumeroCuotas.intValue(), RoundingMode.HALF_DOWN);
            } else {
                resultado.NumeroCuotas = serie.Monto.divide(serie.ValorCuota, MathContext.DECIMAL128).setScale(resultado.DecimalesNumeroCuotas.intValue(), RoundingMode.HALF_DOWN);
            }
            if (serie.NumeroCuotas.compareTo(BigDecimal.ZERO) == 0) {
                resultado.NumeroCuotas = serie.Monto.divide(serie.ValorCuota, MathContext.DECIMAL128).setScale(serie.DecimalesCuota.intValue() + 1, RoundingMode.HALF_UP);
            }
            for (Serie serieEntrada : entrada.p_Series) {
                if ("RESREINV".equalsIgnoreCase(serieEntrada.IdTipoMovimientoFondoMutuo) && BigDecimal.ZERO.compareTo(serie.NumeroCuotas) == 0) {
                    if (serieEntrada.IdInterno.equals(serie.IdInterno)) {
                        resultado.NumeroCuotas = serie.Monto.divide(serie.ValorCuota, MathContext.DECIMAL128).setScale(serie.DecimalesCuota.intValue() + 1, RoundingMode.HALF_UP);
                    }
                }
            }
            resultado.ValorCuota = serie.ValorCuota;
            resultado.MontoDestino = formato.formatear(serie.MontoDestino, FormatoMoneda.MONTO, serie.IdMonedaInversion);
            resultado.ValorParidad = serie.ValorParidad;
            resultado.ValorFactorAjuste = uf != null && uf.FactorOrigenDestino != null ? uf.FactorOrigenDestino : BigDecimal.ZERO;
            resultado.CostoEnFactorAjuste = montoEnUF != null && montoEnUF.MontoDestino != null ? montoEnUF.MontoDestino : BigDecimal.ZERO;
            resultado.IdMonedaSistema = idMoneda;
            resultado.MontoMonedaSistema = idMoneda.equalsIgnoreCase(serie.IdMonedaFondo) ? serie.Monto : montoMonedaSistema != null ? formato.formatear(montoMonedaSistema.MontoDestino, FormatoMoneda.MONTO, idMoneda) : BigDecimal.ZERO;
            resultado.Porcentaje = serie.Porcentaje;
            resultado.FechaValorCuota = serie.FechaValorCuota;
            resultado.Resultado = "OK";
            resultado.CodigoMensaje = "OK";
            if (resultado.ValorCuota.compareTo(BigDecimal.ZERO) == 0) {
                resultado.CodigoMensaje = "ERROR_VALOR_CUOTA";
                resultado.Resultado = "Error al obtener Valor Cuota : " + serie.IdSerieFondo;
            }
            out.P_OUT_RESULTADO.add(resultado);
        }

        return out;
    }

    /** {@code Utils.copyPojo(serie, destino)}: the fields of the same name and type (the objects are immutable values: the reference is the copy). */
    private static void copiarSerie(Serie origen, SeriesValorCuotaTipoParidad destino) {
        destino.IdInterno = origen.IdInterno;
        destino.Fecha = origen.Fecha;
        destino.IdSerieFondo = origen.IdSerieFondo;
        destino.CalculoPorMontoCuota = origen.CalculoPorMontoCuota;
        destino.Monto = origen.Monto;
        destino.NumeroCuotas = origen.NumeroCuotas;
        destino.IdMonedaFondo = origen.IdMonedaFondo;
        destino.IdMonedaInversion = origen.IdMonedaInversion;
        destino.TipoSolicitud = origen.TipoSolicitud;
        destino.TipoFactorAjuste = origen.TipoFactorAjuste;
        destino.IdEmpresa = origen.IdEmpresa;
        destino.Porcentaje = origen.Porcentaje;
    }

    /** Prices of the instruments (legacy {@code obtenerPrecios}): null when the series SELECT returns nothing (the caller then fails with NullPointerException, D-CUO-6). */
    private Map<Long, PreciosTasas> obtenerPrecios(EntradaCalcularMontoNumeroCuota entrada) throws Exception {
        List<String> series = new ArrayList<String>();
        for (Serie mov : entrada.p_Series) {
            if (!series.contains(mov.IdSerieFondo)) series.add(mov.IdSerieFondo);
        }
        List<SerieFondoSolicitudes> seriesSolicitudes = seriesFondo.series(entrada.p_IdEmpresa, series);
        if (seriesSolicitudes.size() == 0)
            return null;
        HashMap<String, SerieFondoSolicitudes> hseries = ListaHash.aHash(seriesSolicitudes, s -> s.IdSerieFondo);
        PreciosTasasContrato.Entrada paramsPrecioTasa = new PreciosTasasContrato.Entrada();
        paramsPrecioTasa.p_IdEmpresa = entrada.p_IdEmpresa;
        paramsPrecioTasa.p_IdModulo = null;
        paramsPrecioTasa.p_IdPais = obtenerPaisSistema(entrada.p_IdEmpresa);
        paramsPrecioTasa.p_IdUsuario = null;
        paramsPrecioTasa.p_DatosInstrumentos = entrada.p_Series.stream()
                .map(dato -> {
                    SerieFondoSolicitudes serie = hseries.get(dato.IdSerieFondo);
                    PreciosTasasContrato.DatosInstrumentos paramsRegIn = new PreciosTasasContrato.DatosInstrumentos();
                    paramsRegIn.FechaConsulta = PreciosTasasContrato.fechaConsulta(FechaLegacy.aTexto(FechaLegacy.aFecha(dato.Fecha)));
                    paramsRegIn.IdClasificacionTipoInstrumento = "FM";
                    paramsRegIn.IdInstrumento = serie.IdInstrumento;
                    paramsRegIn.IdMoneda = dato.IdMonedaFondo;
                    if (dato.IdInterno != 0 && dato.IdInterno != null)
                        paramsRegIn.IdRegistro = dato.IdInterno;
                    else
                        paramsRegIn.IdRegistro = 0L;
                    paramsRegIn.IdTipoCriterioTasa = "0";
                    paramsRegIn.IdTipoInstrumento = serie.IdTipoInstrumento;
                    paramsRegIn.IdUso = "INGGTA".equals(dato.IdTipoMovimientoFondoMutuo) ? "GARANTIA" : "CARTERA";
                    paramsRegIn.Nemotecnico = dato.IdSerieFondo;
                    paramsRegIn.PlazoResidual = 0L;
                    paramsRegIn.TipoCartera = "0";
                    return paramsRegIn;
                })
                .collect(Collectors.toList());
        PreciosTasasContrato.Salida salidaPrecioTasa = PreciosTasasContrato.entregar(precios, paramsPrecioTasa);
        Map<Long, PreciosTasas> mapa = salidaPrecioTasa.P_OUT_PRECIOSTASAS.stream().filter(precio -> precio.IdInstrumento != null)
                .collect(Collectors.toMap(instrumento -> instrumento.IdRegistro, precio -> precio));
        return mapa;
    }

    private Long obtenerPaisSistema(Long idEmpresa) throws Exception {
        Long paisSistema = configuracion.entero(idEmpresa, "PLATAFORMA", "PLA.PaisSistema");
        return paisSistema;
    }
}
