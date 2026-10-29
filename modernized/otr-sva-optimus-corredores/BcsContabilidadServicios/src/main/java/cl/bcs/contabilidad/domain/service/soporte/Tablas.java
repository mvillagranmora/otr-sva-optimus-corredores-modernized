package cl.bcs.contabilidad.domain.service.soporte;

import cl.bcs.contabilidad.domain.model.ResultadoCentralizacion;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabDatoMovimientoRef;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabEventoRef;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabMovimientoRef;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The tables one centralization type produces (legacy {@code BcsCentralizacion.SalidaCentralizacion}, all lists start
 * empty and the error counter at 0, BcsCentralizacion.java:25-39). {@link #salida()} adds {@code P_OUT_RESULTADO}
 * through {@code llenarResultado} (BcsCentralizacion.java:100-111) unless the type skipped it.
 */
public final class Tablas {

    private final List<TabMovimientoRef> movimientos = new ArrayList<>();
    private final List<TabEventoRef> eventos = new ArrayList<>();
    private final List<TabDatoMovimientoRef> datos = new ArrayList<>();
    private final List<String> mensajes = new ArrayList<>();
    private long errores;
    private boolean conResultados = true;

    public Tablas movimientos(List<TabMovimientoRef> filas) {
        movimientos.addAll(filas);
        return this;
    }

    public Tablas movimiento(TabMovimientoRef fila) {
        movimientos.add(fila);
        return this;
    }

    public Tablas eventos(List<TabEventoRef> filas) {
        eventos.addAll(filas);
        return this;
    }

    public Tablas evento(TabEventoRef fila) {
        eventos.add(fila);
        return this;
    }

    public Tablas datos(List<TabDatoMovimientoRef> filas) {
        datos.addAll(filas);
        return this;
    }

    public Tablas dato(TabDatoMovimientoRef fila) {
        datos.add(fila);
        return this;
    }

    public List<TabMovimientoRef> movimientos() { return movimientos; }
    public List<TabEventoRef> eventos() { return eventos; }
    public List<TabDatoMovimientoRef> datos() { return datos; }

    /** A controlled error of the type: P_OUT_ERRORESCONTADOR = 1 plus one ERROR message. */
    public Tablas error(String codigo, Map<String, String> valores) {
        errores = 1L;
        mensajes.add(mensaje(codigo, valores));
        return this;
    }

    /** The type ended before {@code llenarResultado}: P_OUT_RESULTADO stays empty. */
    public Tablas sinResultados() {
        conResultados = false;
        return this;
    }

    public SalidaCentralizacion salida() {
        List<ResultadoCentralizacion> resultados = conResultados
                ? movimientos.stream().map(Tablas::resultado).toList()
                : List.of();
        return new SalidaCentralizacion(errores, List.copyOf(mensajes), resultados,
                List.copyOf(movimientos), List.copyOf(eventos), List.copyOf(datos), List.of());
    }

    /** Message text shape shared with the orchestrator: {@code CODIGO:clave=valor,...}. */
    public static String mensaje(String codigo, Map<String, String> valores) {
        if (valores == null || valores.isEmpty()) return codigo;
        StringBuilder sb = new StringBuilder(codigo).append(':');
        new TreeMap<>(valores).forEach((k, v) -> sb.append(sb.charAt(sb.length() - 1) == ':' ? "" : ",")
                .append(k).append('=').append(v));
        return sb.toString();
    }

    /** Legacy llenarResultado: a copy of the movement with Ejercicio = String.valueOf(Long). */
    public static ResultadoCentralizacion resultado(TabMovimientoRef m) {
        return new ResultadoCentralizacion(m.idTipoCentralizacion(), m.idMovimiento(), m.idTipoMovimiento(),
                m.idEmpresa(), m.numero(), m.linea(), m.fecha(), m.documentoIdentidad(), m.idPortafolio(),
                String.valueOf(m.ejercicio()), m.periodo(), m.monto(), m.idMoneda(), m.estado());
    }
}
