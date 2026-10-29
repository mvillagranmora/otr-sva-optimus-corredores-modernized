package cl.bcs.fondomutuo.rescatereinversion;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;

import cl.bcs.fondomutuo.cierre.FechaLegacy;
import cl.bcs.fondomutuo.cierre.ListaHash;
import cl.bcs.fondomutuo.cierre.SerieFondoSolicitudes;
import cl.bcs.fondomutuo.cuotas.CalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.cuotas.CalcularMontoNumeroCuota.EntradaCalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.cuotas.CalcularMontoNumeroCuota.Resultado;
import cl.bcs.fondomutuo.cuotas.CalcularMontoNumeroCuota.SalidaCalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.fechas.CalcularFechaSolicitudAporteRescate.ResultadoFecha;
import cl.bcs.fondomutuo.kit.MensajeSalida;
import cl.bcs.fondomutuo.port.out.kit.DiccionarioMensajesPort;
import cl.bcs.fondomutuo.rescatereinversion.RescateReinversionContrato.InSolicitudesAporte;
import cl.bcs.fondomutuo.rescatereinversion.RescateReinversionContrato.InSolicitudesRescate;

/**
 * Quota and amount of the rescues and of the contributions derived from them (FM-REI-13..15, RR:551-642), with the 3A {@code CalcularMontoNumeroCuota}.
 * The two early exits are answered to the caller, which releases the lock as the legacy does at those points (RR:602, :640).
 */
final class RreCuotas {

    private final CalcularMontoNumeroCuota cuotas;
    private final DiccionarioMensajesPort diccionario;

    RreCuotas(CalcularMontoNumeroCuota cuotas, DiccionarioMensajesPort diccionario) {
        this.cuotas = cuotas;
        this.diccionario = diccionario;
    }

    /** @return false when the legacy releases the lock and returns (ERROR_VALOR_CUOTA of the first contribution, or a failed second calculation). */
    boolean calcular(RreContexto c) throws Exception {
        EntradaCalcularMontoNumeroCuota entradaCalculo = new EntradaCalcularMontoNumeroCuota();
        entradaCalculo.p_IdEmpresa = c.entrada.p_IdEmpresa;
        for (InSolicitudesRescate solicitud : c.entrada.p_SolicitudesRescate) {
            CalcularMontoNumeroCuota.Serie serie = new CalcularMontoNumeroCuota.Serie();
            SerieFondoSolicitudes serieFondo = c.hseries.get(solicitud.IdSerieFondo);
            ResultadoFecha fecha = c.hfechas.get(solicitud.IdInterno);
            serie.IdInterno = solicitud.IdInterno;
            if (solicitud.FechaValorCuota != null) {
                serie.Fecha = FechaLegacy.texto(solicitud.FechaValorCuota);
            } else if (fecha != null) {
                serie.Fecha = RreFechas.texto(fecha.FechaValorCuota);
            } else {
                serie.Fecha = c.fechaSistema;
            }
            serie.IdSerieFondo = solicitud.IdSerieFondo;
            serie.CalculoPorMontoCuota = solicitud.CalculoPorMontoCuota;
            serie.Monto = solicitud.Monto;
            serie.NumeroCuotas = solicitud.NumeroCuotas;
            serie.IdMonedaFondo = serieFondo != null ? serieFondo.IdMoneda : "";
            serie.IdMonedaInversion = solicitud.IdMoneda;
            serie.TipoSolicitud = RreConstantes.TIPO_SOLICITUD_RESCATE;
            serie.TipoFactorAjuste = RreConstantes.UF;
            serie.IdEmpresa = c.entrada.p_IdEmpresa;
            serie.IdTipoMovimientoFondoMutuo = RreConstantes.REINVERSION;
            entradaCalculo.p_Series.add(serie);
            if (!c.rescatesUnicos.containsKey(solicitud.IdAgrupador)) {
                c.rescatesUnicos.put(solicitud.IdAgrupador, solicitud);
                c.pIdSerieFondo = solicitud.IdSerieFondo;
            }
        }
        SalidaCalcularMontoNumeroCuota salidaCalculo = cuotas.logica(entradaCalculo);
        c.valoresCuota = ListaHash.aHash(salidaCalculo.P_OUT_RESULTADO, r -> r.IdInterno);
        entradaCalculo.p_Series.clear();
        for (InSolicitudesAporte solicitud : c.entrada.p_SolicitudesAporte) {
            CalcularMontoNumeroCuota.Serie serie = new CalcularMontoNumeroCuota.Serie();
            Resultado montoRescate = c.valoresCuota.get(solicitud.IdInterno);
            if (!"OK".equalsIgnoreCase(montoRescate.CodigoMensaje)) {   // D-F2-7: the series of the message is the one of the last new group
                c.out.P_OUT_ERRORESCONTADOR++;
                HashMap<String, String> datosMensaje = new HashMap<>();
                datosMensaje.put("IdSerieFondo", c.pIdSerieFondo);
                c.out.P_OUT_MENSAJES.add(MensajeSalida.conTexto(solicitud.IdInterno, MensajeSalida.MENSAJE_ERROR, "ERROR_VALOR_CUOTA", datosMensaje, diccionario,
                        c.entrada.p_IdEmpresa));
                return false;
            }
            SerieFondoSolicitudes serieFondo = c.hseries.get(solicitud.IdSerieFondo);
            InSolicitudesRescate res = c.hrescates.get(solicitud.IdInterno);
            serie.IdInterno = solicitud.IdInterno * -1;
            serie.Fecha = serieFondo != null ? FechaLegacy.texto(serieFondo.FechaFondo) : c.fechaSistema;
            serie.IdSerieFondo = solicitud.IdSerieFondo;
            serie.CalculoPorMontoCuota = RreConstantes.CALCULO_POR_MONTO;
            serie.Monto = montoRescate.Monto.multiply(solicitud.Porcentaje, MathContext.DECIMAL128).divide(new BigDecimal(100), MathContext.DECIMAL128);
            serie.NumeroCuotas = BigDecimal.ZERO;
            serie.IdMonedaFondo = serieFondo != null ? serieFondo.IdMoneda : "";
            serie.IdMonedaInversion = res.IdMoneda;
            serie.TipoSolicitud = RreConstantes.TIPO_SOLICITUD_APORTE;
            serie.TipoFactorAjuste = RreConstantes.UF;
            serie.IdEmpresa = c.entrada.p_IdEmpresa;
            serie.Porcentaje = solicitud.Porcentaje;
            entradaCalculo.p_Series.add(serie);
        }
        salidaCalculo = cuotas.logica(entradaCalculo);
        c.valoresCuota.putAll(ListaHash.aHash(salidaCalculo.P_OUT_RESULTADO, r -> r.IdInterno));
        boolean hasVC = true;
        for (Resultado sc : salidaCalculo.P_OUT_RESULTADO) {
            if (!salidaCalculo.P_OUT_RESULTADO.get(0).CodigoMensaje.equals("OK")) {   // D-F2-7: always the FIRST result is checked; all are reported
                c.out.P_OUT_ERRORESCONTADOR++;
                hasVC = false;
                c.out.P_OUT_MENSAJES.add(new MensajeSalida(sc.IdInterno, "ERROR", sc.CodigoMensaje, sc.Resultado));
            }
        }
        return hasVC;
    }
}
