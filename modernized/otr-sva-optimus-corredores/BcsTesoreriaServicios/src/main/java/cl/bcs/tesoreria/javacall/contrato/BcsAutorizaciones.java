package cl.bcs.tesoreria.javacall.contrato;

import java.util.ArrayList;
import java.util.List;

import cl.bcs.tesoreria.javacall.comun.MensajeSalida;
import cl.bcs.utils.BcsPojo;

/**
 * Input and output of the two hooks of the platform authorization engine (legacy {@code cl.bcs.plataforma.aut.BcsAutorizaciones}):
 * {@code autorizarEvento} / {@code rechazarEvento}, implemented by {@code AutorizacionLiquidacion} and
 * {@code AutorizacionAnularMovimientosTesoreria}. Field names are the legacy JSON names.
 */
public final class BcsAutorizaciones {

    public static final String EXITO_AUTORIZACION = "EXITO";
    public static final String ERROR_AUTORIZACION = "ERROR";

    private BcsAutorizaciones() {
    }

    public static class SalidaAutorizacion extends BcsPojo {
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<ResultadoAutorizacion> P_OUT_RESULTADO = new ArrayList<>();
    }

    public static class ResultadoAutorizacion {
        public Object evento;
        /** {@link #EXITO_AUTORIZACION} or {@link #ERROR_AUTORIZACION}. */
        public String resultado;
    }

    public static class EntradaAutorizacion extends BcsPojo {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;
        public List<Evento> p_Lista = new ArrayList<>();
    }
}
