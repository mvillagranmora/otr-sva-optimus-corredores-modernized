package cl.bcs.fondomutuo.requisitos;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cl.bcs.fondomutuo.kit.MensajeSalida;
import cl.bcs.fondomutuo.port.out.kit.ConfiguracionSistemaPort;
import cl.bcs.fondomutuo.port.out.kit.DiccionarioMensajesPort;

/**
 * Legacy {@code cl.bcs.fondomutuo.ValidaMontoInversion} (344 LOC), rules FM-REQ-22 and D-REQ-6. Plain Java, no state: all data of the request is
 * local, the ports are the only collaborators. Behaviour kept bug-for-bug: a missing/unreadable limit is 0 (so a maximum of 0 rejects every positive
 * amount), the groups are visited in {@link HashMap} order (the order of the messages depends on it), a null {@code FolioContrato} raises
 * {@link NullPointerException}.
 */
public class ValidaMontoInversionService {

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public List<Solicitud> p_Solicitudes = new ArrayList<>();
    }

    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
    }

    public static class Solicitud {
        public Long IdInterno;
        public Long FolioContrato;
        public String AportesAltos;
        public BigDecimal Monto;
        public String ValidaMontoMinimo = "S";
    }

    private static class ValidacionMonto {
        Long FolioContrato = 0L;
        BigDecimal MontoTotalInversion = BigDecimal.ZERO;
        BigDecimal MontoTotalAportesAltos = BigDecimal.ZERO;
        List<Solicitud> Solicitudes = new ArrayList<>();
    }

    private final ConfiguracionSistemaPort configuracion;
    private final DiccionarioMensajesPort diccionario;

    public ValidaMontoInversionService(ConfiguracionSistemaPort configuracion, DiccionarioMensajesPort diccionario) {
        this.configuracion = configuracion;
        this.diccionario = diccionario;
    }

    public Salida logica(Entrada entrada) {
        Salida out = new Salida();
        List<Solicitud> conFolio = entrada.p_Solicitudes.stream().filter(s -> !s.FolioContrato.equals(0L)).collect(Collectors.toList());
        List<Solicitud> sinFolio = entrada.p_Solicitudes.stream().filter(s -> s.FolioContrato.equals(0L)).collect(Collectors.toList());
        Map<Long, List<Solicitud>> agrupadas = conFolio.stream().collect(Collectors.groupingBy(s -> s.FolioContrato));

        List<ValidacionMonto> montos = new ArrayList<>();
        for (Map.Entry<Long, List<Solicitud>> grupo : agrupadas.entrySet()) {
            ValidacionMonto monto = new ValidacionMonto();
            monto.FolioContrato = grupo.getKey();
            for (Solicitud s : grupo.getValue()) {
                acumular(monto, s);
            }
            montos.add(monto);
        }
        for (Solicitud s : sinFolio) {
            ValidacionMonto monto = new ValidacionMonto();
            acumular(monto, s);
            montos.add(monto);
        }

        out.P_OUT_MENSAJES = errores(entrada.p_IdEmpresa, montos);
        out.P_OUT_ERRORESCONTADOR = ConteoErrores.errores(out.P_OUT_MENSAJES);
        return out;
    }

    private static void acumular(ValidacionMonto monto, Solicitud s) {
        if (Constantes3B.SI.equals(s.AportesAltos)) {
            monto.MontoTotalAportesAltos = monto.MontoTotalAportesAltos.add(s.Monto, MathContext.DECIMAL128);
        }
        monto.MontoTotalInversion = monto.MontoTotalInversion.add(s.Monto, MathContext.DECIMAL128);
        monto.Solicitudes.add(s);
    }

    /** Legacy {@code new BigDecimal(getParametroConfiguracionString(..))} inside try/catch: any failure (absent parameter, bad number) is 0. */
    private BigDecimal limite(Long idEmpresa, String parametro) {
        try {
            return new BigDecimal(configuracion.texto(idEmpresa, "FONDOMUTUO", parametro));
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private List<MensajeSalida> errores(Long idEmpresa, List<ValidacionMonto> montos) {
        BigDecimal minTotal = limite(idEmpresa, "MontoMinimoTotalInversion");
        BigDecimal maxTotal = limite(idEmpresa, "MontoMaximoTotalInversion");
        BigDecimal minAltos = limite(idEmpresa, "MontoMinimoAportesAltos");
        BigDecimal maxAltos = limite(idEmpresa, "MontoMaximoAportesAltos");

        List<MensajeSalida> errores = new ArrayList<>();
        for (ValidacionMonto monto : montos) {
            for (Solicitud s : monto.Solicitudes) {
                if (Constantes3B.SI.equals(s.AportesAltos)) {
                    HashMap<String, String> p = new HashMap<>();
                    p.put("Monto", monto.MontoTotalAportesAltos.toPlainString());
                    p.put("Minimo", minAltos.toPlainString());
                    p.put("Maximo", maxAltos.toPlainString());
                    if (errorMinimo(monto.MontoTotalAportesAltos, minAltos, s.ValidaMontoMinimo) || errorMaximo(monto.MontoTotalAportesAltos, maxAltos)) {
                        errores.add(MensajeSalida.conTexto(s.IdInterno, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_ALTOS_APORTE_FFMM", p, diccionario,
                                idEmpresa));
                    }
                }
                HashMap<String, String> p = new HashMap<>();
                p.put("Monto", monto.MontoTotalInversion.toPlainString());
                p.put("Minimo", minTotal.toPlainString());
                p.put("Maximo", maxTotal.toPlainString());
                if (errorMinimo(monto.MontoTotalInversion, minTotal, s.ValidaMontoMinimo) || errorMaximo(monto.MontoTotalInversion, maxTotal)) {
                    errores.add(MensajeSalida.conTexto(s.IdInterno, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_MONTO_TOTAL_INVERSION_FFMM", p, diccionario,
                            idEmpresa));
                }
            }
        }
        return errores;
    }

    private static boolean errorMinimo(BigDecimal monto, BigDecimal minimo, String validaMontoMinimo) {
        return Constantes3B.SI.equals(validaMontoMinimo) && monto.compareTo(minimo) == -1;
    }

    private static boolean errorMaximo(BigDecimal monto, BigDecimal maximo) {
        return monto.compareTo(maximo) == 1;
    }
}
