package cl.bcs.fondomutuo.consultas.dolar;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import cl.bcs.fondomutuo.cierre.ListaHash;
import cl.bcs.fondomutuo.cierre.contrato.ParidadContrato;
import cl.bcs.fondomutuo.cierre.contrato.ParidadContrato.CalcularParidadEntrada;
import cl.bcs.fondomutuo.cierre.contrato.ParidadContrato.CalcularParidadSalida;
import cl.bcs.fondomutuo.cierre.contrato.ParidadContrato.CalculoEntrada;
import cl.bcs.fondomutuo.cierre.contrato.ParidadContrato.CalculoSalida;
import cl.bcs.fondomutuo.domain.ConstantesFondoMutuo;
import cl.bcs.fondomutuo.port.out.http.ParidadPort;
import cl.bcs.fondomutuo.port.out.kit.ConfiguracionSistemaPort;

/**
 * Legacy {@code ConsultarValorDolarFondoMutuo.logica} (FM-VDO-01..03, read only): the dollar value of each series of the request, from Plataforma
 * {@code CalcularParidad} (parameter {@code UsaDolarObservado} = {@code S}) or from the table {@code FMM.ValorTipoCambio}. Bug for bug: both parameters are
 * read before the list is looked at (a null list is a NullPointerException), {@code IdInterno} is a counter that is never incremented (D-F3A-14, so two
 * distinct dates make the key not unique), every type other than {@code APO} takes the rescue rate and the date is ignored (D-F3A-15), a missing value is
 * zero. No state; the unit of work is the ambient read-only transaction.
 */
public class ConsultarDolarService implements ConsultarDolarPort {

    private final ConfiguracionSistemaPort configuracion;
    private final ParidadPort paridad;
    private final ConsultarDolarDatos datos;

    public ConsultarDolarService(ConfiguracionSistemaPort configuracion, ParidadPort paridad, ConsultarDolarDatos datos) {
        this.configuracion = configuracion;
        this.paridad = paridad;
        this.datos = datos;
    }

    @Override
    public ConsultarDolarContrato.Salida logica(ConsultarDolarContrato.Entrada entrada) throws Exception {
        ConsultarDolarContrato.Salida out = new ConsultarDolarContrato.Salida();

        String usaDolarObservado = configuracion.texto(entrada.p_IdEmpresa, "FONDOMUTUO", "UsaDolarObservado");
        String idMoneda = configuracion.texto(entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");

        if (entrada.p_Series.size() > 0) {   // a null list is a NullPointerException, as the legacy
            if (ConstantesFondoMutuo.SI.equals(usaDolarObservado)) {
                HashMap<String, Boolean> fechasRepetidas = new HashMap<String, Boolean>();
                HashMap<String, Long> fechasIdInterno = new HashMap<String, Long>();
                CalcularParidadEntrada calcularParidadEntrada = new CalcularParidadEntrada();
                Long i = 0L;   // never incremented (D-F3A-14)
                calcularParidadEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
                calcularParidadEntrada.p_IdTrabajo = 1L;
                // The side effect of this filter (fechasRepetidas.put on a HashMap) is correct ONLY with a SEQUENTIAL stream: it keeps the first series of each date,
                // in order, as the legacy did. Never make it parallel (HashMap is not thread safe and "first" would stop being deterministic).
                calcularParidadEntrada.p_CalculosEntrada = entrada.p_Series.stream().filter(serie -> {
                    if (fechasRepetidas.get(serie.Fecha) != null) {
                        return false;
                    } else {
                        fechasRepetidas.put(serie.Fecha, true);
                        return true;
                    }
                }).map(serie -> {
                    CalculoEntrada calculoEntrada = new CalculoEntrada();
                    calculoEntrada.IdTrabajo = 1L;
                    calculoEntrada.IdInterno = i;
                    calculoEntrada.Fecha = serie.Fecha;
                    calculoEntrada.IdMonedaOrigen = "USD";
                    calculoEntrada.IdMonedaDestino = idMoneda;
                    calculoEntrada.ValorCambioLocalModificado = BigDecimal.ZERO;
                    calculoEntrada.MontoOrigen = BigDecimal.ONE;
                    calculoEntrada.IdUso = "";
                    fechasIdInterno.put(serie.Fecha, i);
                    return calculoEntrada;
                }).collect(Collectors.toList());

                CalcularParidadSalida calculoSalida = ParidadContrato.calcular(paridad, calcularParidadEntrada);
                HashMap<Long, CalculoSalida> paridades = ListaHash.aHash(calculoSalida.p_out_CalculosSalida, s -> s.IdInterno);

                out.P_OUT_RESULTADO = entrada.p_Series.stream().map(serie -> {
                    ConsultarDolarContrato.ResultadoValorDolarFondoMutuo resultado = new ConsultarDolarContrato.ResultadoValorDolarFondoMutuo();
                    resultado.IdSerieFondo = serie.IdSerieFondo;
                    resultado.Fecha = serie.Fecha;
                    resultado.IdTipoSolicitud = serie.IdTipoSolicitud;
                    Long id = fechasIdInterno.get(serie.Fecha);
                    CalculoSalida calcularParidadSalida = paridades.get(id);
                    resultado.ValorParidad = (calcularParidadSalida != null) ? calcularParidadSalida.MontoDestino : BigDecimal.ZERO;
                    return resultado;
                }).collect(Collectors.toList());

            } else {
                List<String> series = entrada.p_Series.stream().map(serie -> serie.IdSerieFondo).collect(Collectors.toList());
                List<ConsultarDolarContrato.ValorCuotaTipoParidad> valorCuotaTipoParidad = datos.valoresTipoCambio(series, entrada.p_IdEmpresa);
                HashMap<String, ConsultarDolarContrato.ValorCuotaTipoParidad> paridades = ListaHash.aHash(valorCuotaTipoParidad, v -> v.IdSerieFondo);
                out.P_OUT_RESULTADO = entrada.p_Series.stream().map(serie -> {
                    ConsultarDolarContrato.ResultadoValorDolarFondoMutuo resultado = new ConsultarDolarContrato.ResultadoValorDolarFondoMutuo();
                    resultado.IdSerieFondo = serie.IdSerieFondo;
                    resultado.Fecha = serie.Fecha;
                    resultado.IdTipoSolicitud = serie.IdTipoSolicitud;
                    ConsultarDolarContrato.ValorCuotaTipoParidad fila = paridades.get(serie.IdSerieFondo);
                    resultado.ValorParidad = (fila != null)
                            ? ConstantesFondoMutuo.TIPO_SOLICITUD_APORTE.equals(serie.IdTipoSolicitud) ? fila.ValorDolarAporte : fila.ValorDolarRescate
                            : BigDecimal.ZERO;
                    return resultado;
                }).collect(Collectors.toList());
            }
        }
        return out;
    }
}
