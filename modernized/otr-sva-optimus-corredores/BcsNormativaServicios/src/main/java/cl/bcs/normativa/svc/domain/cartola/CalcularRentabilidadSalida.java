package cl.bcs.normativa.svc.domain.cartola;

import java.util.ArrayList;
import java.util.List;

/**
 * Resultado del cálculo de rentabilidad de cartola.
 */
public class CalcularRentabilidadSalida {

    public Long erroresContador = 0L;
    public String mensaje = "";
    public List<Rentabilidad> salida = new ArrayList<>();

    public CalcularRentabilidadSalida() {}
}
