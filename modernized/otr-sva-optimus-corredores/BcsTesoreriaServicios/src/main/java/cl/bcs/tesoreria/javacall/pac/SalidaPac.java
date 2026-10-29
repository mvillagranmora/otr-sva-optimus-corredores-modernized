package cl.bcs.tesoreria.javacall.pac;

import java.util.ArrayList;
import java.util.List;

import cl.bcs.tesoreria.javacall.kit.MensajeSalida;

/** Response of IngresarMandatoPAC / ModificarPAC (legacy {@code SalidaCrearIngresoPAC}); the field names are the JSON contract. */
public class SalidaPac {

    public Long P_OUT_ERRORESCONTADOR = 0L;
    public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
    public List<ResultadoPac> P_OUT_RESULTADO = new ArrayList<>();

    /** {@code FolioPAC} is filled by ModificarPAC only (null is not serialised by the plain Gson of the entry points). */
    public static class ResultadoPac {
        public Long IdMandatoPAC;
        public Long FolioPAC;
    }
}
