package cl.bcs.utils.clave;

import java.util.List;

/**
 * RULE-031..034: outcome of {@link ValidadorClave#validar} (legacy SalidaValidarNuevaClave: ClaveValida,
 * P_OUT_ERRORESCONTADOR, P_OUT_ERRORESDETALLE, P_OUT_ESTADOCRITERIOSNUEVACLAVE). The lists are immutable.
 */
public record ResultadoValidacion(boolean claveValida, long erroresContador, List<MensajeSalida> erroresDetalle,
                                  List<EstadoCriterio> estadoCriterios) {

    public ResultadoValidacion {
        erroresDetalle = List.copyOf(erroresDetalle);
        estadoCriterios = List.copyOf(estadoCriterios);
    }
}
