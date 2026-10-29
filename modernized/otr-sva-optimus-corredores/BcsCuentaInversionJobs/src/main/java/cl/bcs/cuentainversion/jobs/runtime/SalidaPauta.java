package cl.bcs.cuentainversion.jobs.runtime;

import cl.bcs.cuentainversion.service.CtaJavaCallInfra.MensajeSalida;

import java.util.ArrayList;
import java.util.List;

/**
 * Legacy {@code BcsProcesoPauta.SalidaProcesoPauta} (BcsPlataformaJobs {@code BcsProcesoPauta.java:17-31}): public fields with the legacy
 * names, because the PAUTA runner and the goldens read them by name. {@code P_OUT_MENSAJE} is the summary (null unless the step sets it),
 * {@code P_OUT_ERRORESCONTADOR} starts at 0 and {@code P_OUT_MENSAJES} (the details) at an empty list.
 */
public class SalidaPauta {
    public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
    public String P_OUT_MENSAJE;
    public Long P_OUT_ERRORESCONTADOR = 0L;
}
