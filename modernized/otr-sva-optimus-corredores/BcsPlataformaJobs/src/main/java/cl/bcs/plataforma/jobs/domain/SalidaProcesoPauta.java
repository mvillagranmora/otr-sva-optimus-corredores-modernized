package cl.bcs.plataforma.jobs.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Output record for a pauta process step.
 * Legacy: cl.bcs.plataforma.jobs.BcsProcesoPauta.SalidaProcesoPauta
 */
public class SalidaProcesoPauta {
    public List<String> mensajes = new ArrayList<>();
    public String mensaje = "";
    public Long erroresContador = 0L;

    @Override
    public String toString() {
        return "SalidaProcesoPauta{errores=" + erroresContador + ", mensaje=" + mensaje + "}";
    }
}
