package cl.bcs.cuentaInversion;

import cl.bcs.utils.BcsPojo;

/**
 * ORACLE DOUBLE (hand written, reduced) of the legacy cl.bcs.cuentaInversion.ConsultarSaldoCuentaInversion: the only thing the CanalAgente closure needs from it is the nested type
 * {@code ErroresDetalle} (the type of {@code SalidaConsultarCartolaDeMovimiento.P_OUT_ERRORESDETALLE}); it is copied verbatim from the legacy class (line 287-290). The service itself is not
 * part of the CanalAgente module and is never called.
 */
public class ConsultarSaldoCuentaInversion {
    public static class ErroresDetalle extends BcsPojo {
        public String Codigo;
        public String Descripcion;
    }
}
