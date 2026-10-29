package cl.bcs.normativa.svc.service.declaracion;

import cl.bcs.normativa.svc.domain.declaracion.DetalleIPCDJ1871;
import cl.bcs.normativa.svc.domain.declaracion.ValorIPC;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Servicio de reajuste de valores por IPC para declaraciones juradas.
 *
 * <p>Implementa la lógica pura portada de:
 * {@code cl.bcs.normativa.declaraciones.DJ1871.ReajustarValorIPC} (legacy).
 *
 * <p>Todos los métodos son {@code static} para testear sin Spring.
 *
 * <p>El acceso a HANA (consulta de factores IPC y persistencia) se realiza via
 * los puertos de salida — esta clase solo contiene la lógica de cálculo.
 */
@Service
public class ReajusteIPCService {

    /**
     * Calcula la variación porcentual IPC entre un mes de referencia y el mes de un detalle.
     *
     * <p>Fórmula:
     * <pre>
     *   variacion% = (ipcMesActual / ipcMesDetalle) * 100 - 100
     *   valorActualizado = valor * (1 + variacion% / 100)
     * </pre>
     *
     * @param ipcMesActual  valor IPC del mes de referencia (mes del esquema)
     * @param ipcMesDetalle valor IPC del mes del detalle
     * @param valorOriginal monto original a reajustar
     * @return monto reajustado, redondeado a entero (HALF_UP)
     */
    public static BigDecimal reajustar(BigDecimal ipcMesActual,
                                       BigDecimal ipcMesDetalle,
                                       BigDecimal valorOriginal) {
        if (ipcMesDetalle == null || ipcMesDetalle.compareTo(BigDecimal.ZERO) == 0) {
            return valorOriginal;
        }
        if (valorOriginal == null) return BigDecimal.ZERO;

        BigDecimal variacionPct = ipcMesActual
                .divide(ipcMesDetalle, MathContext.DECIMAL128)
                .multiply(new BigDecimal(100))
                .subtract(new BigDecimal(100));

        return valorOriginal
                .multiply(variacionPct, MathContext.DECIMAL128)
                .divide(new BigDecimal(100))
                .add(valorOriginal)
                .setScale(0, RoundingMode.HALF_UP);
    }

    /**
     * Aplica el reajuste IPC a todos los detalles de una DJ1871.
     *
     * @param detalles        lista de detalles a reajustar (modificados in-place)
     * @param valoresIPC      mapa agnoMes → ValorIPC (construido desde BD)
     * @param ipcMesReferencia valor IPC del mes de cierre/referencia
     */
    public static void aplicarReajusteDJ1871(List<DetalleIPCDJ1871> detalles,
                                              Map<String, ValorIPC> valoresIPC,
                                              BigDecimal ipcMesReferencia) {
        for (DetalleIPCDJ1871 detalle : detalles) {
            ValorIPC ipcMes = valoresIPC.get(detalle.agnoMes);
            if (ipcMes == null) continue;

            BigDecimal ipc = ipcMes.valorIpc;

            detalle.ahorrosTrabajadorActualizado     = reajustar(ipcMesReferencia, ipc, detalle.ahorrosTrabajador);
            detalle.ahorrosDepositoActualizado       = reajustar(ipcMesReferencia, ipc, detalle.ahorrosDeposito);
            detalle.ahorrosCotizacionesActualizado   = reajustar(ipcMesReferencia, ipc, detalle.ahorrosCotizaciones);
            detalle.retirosTrabajadorActualizado     = reajustar(ipcMesReferencia, ipc, detalle.retirosTrabajador);
            detalle.retirosDepositoActualizado       = reajustar(ipcMesReferencia, ipc, detalle.retirosDeposito);
            detalle.retirosCotizacionesActualizado   = reajustar(ipcMesReferencia, ipc, detalle.retirosCotizaciones);
        }
    }

    /**
     * Construye un mapa agnoMes → ValorIPC desde una lista.
     */
    public static Map<String, ValorIPC> indexarIPC(List<ValorIPC> lista) {
        return lista.stream().collect(Collectors.toMap(
                v -> v.agnoMes,
                Function.identity(),
                (a, b) -> a));
    }
}
