package cl.bcs.tesoreria.javacall.contrato;

import cl.bcs.tesoreria.javacall.comun.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Utils;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON contract (legacy field names, P_OUT_*) of the collaborator service {@code cl.bcs.plataforma.aut.ModificarEstadoEventoAutorizacionMasivo} as the Tesoreria services call it
 * through the {@code Colaboradores} port. The nested classes are the legacy Entrada/Salida classes; their no-argument constructors set the same
 * defaults, so decoding an empty answer with Gson gives the legacy empty Salida.
 */
public final class ModificarEstadoEventoAutorizacionMasivo {

	private ModificarEstadoEventoAutorizacionMasivo() {
	}


	// legacy Plataforma/JAVA/BcsPlataformaServicios/src/cl/bcs/plataforma/aut/ModificarEstadoEventoAutorizacionMasivo.java:59-75 (verbatim)
    public static class ServicioEntrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_SimularOperacion;
        public String p_UsaReglasNegocio;
        public List<ServicioEntradaEvento> p_Eventos;
        public List<ServicioEntradaEtapa> p_Etapas;
        public String p_EventoEtapa;
        public String p_IdEstado;
        public String p_Observaciones;
        public String p_Comentario = "";

        public String toString() {
            return Utils.printJson(this, false);
        }
    }

	// legacy Plataforma/JAVA/BcsPlataformaServicios/src/cl/bcs/plataforma/aut/ModificarEstadoEventoAutorizacionMasivo.java:77-83 (verbatim)
    public static class ServicioEntradaEvento {
        public Long IdEvento;

        public String toString() {
            return Utils.printJson(this, false);
        }
    }

	// legacy Plataforma/JAVA/BcsPlataformaServicios/src/cl/bcs/plataforma/aut/ModificarEstadoEventoAutorizacionMasivo.java:85-92 (verbatim)
    public static class ServicioEntradaEtapa {
        public Long IdEvento;
        public Long IdEtapa;

        public String toString() {
            return Utils.printJson(this, false);
        }
    }

	// legacy Plataforma/JAVA/BcsPlataformaServicios/src/cl/bcs/plataforma/aut/ModificarEstadoEventoAutorizacionMasivo.java:94-103 (verbatim)
    public static class ServicioSalida {
        public Long P_OUT_ERRORESCONTADOR;
        public Long P_OUT_ADVERTENCIASCONTADOR;
        public List<ServicioSalidaAdvertencia> P_OUT_ADVERTENCIADETALLE;
        public List<ServicioSalidaError> P_OUT_ERRORESDETALLE;

        public String toString() {
            return Utils.printJson(this, false);
        }
    }

	// legacy Plataforma/JAVA/BcsPlataformaServicios/src/cl/bcs/plataforma/aut/ModificarEstadoEventoAutorizacionMasivo.java:105-121 (verbatim)
    public static class ServicioSalidaAdvertencia {
        public String Codigo;
        public String RequiereAutorizacion;
        public String Descripcion;
        
        public ServicioSalidaAdvertencia() {
        };

        public ServicioSalidaAdvertencia(String codigo, String mensaje) {
             this.Codigo = codigo;
             this.Descripcion = mensaje;
        }

        public String toString() {
            return Utils.printJson(this, false);
        }
    }

	// legacy Plataforma/JAVA/BcsPlataformaServicios/src/cl/bcs/plataforma/aut/ModificarEstadoEventoAutorizacionMasivo.java:123-139 (verbatim)
    public static class ServicioSalidaError {
        public String Codigo;
        public String RequiereAutorizacion;
        public String Descripcion;

        public ServicioSalidaError() {
        };

        public ServicioSalidaError(String codigo, String mensaje) {
             this.Codigo = codigo;
             this.Descripcion = mensaje;
        }

        public String toString() {
            return Utils.printJson(this, false);
        }
    }

	// legacy Plataforma/JAVA/BcsPlataformaServicios/src/cl/bcs/plataforma/aut/ModificarEstadoEventoAutorizacionMasivo.java:168-176 (verbatim)
    public static class EtapaEventoPrc extends EtapaEvento {
        // Procesados
        public boolean IsOK;
        public String MsgError;

        public String toString() {
            return Utils.printJson(this, false);
        }
    }

	// legacy Plataforma/JAVA/BcsPlataformaServicios/src/cl/bcs/plataforma/aut/ModificarEstadoEventoAutorizacionMasivo.java:188-197 (verbatim)
    public static class UpdateEventoPorEtapa {
        public Long IdEvento;
        public Long IdUsuarioCambioEstado;
        public Timestamp FechaHoraCambioEstado;
        public String Comentario;

        public String toString() {
            return Utils.printJson(this, false);
        }
    }

	// legacy Plataforma/JAVA/BcsPlataformaServicios/src/cl/bcs/plataforma/aut/ModificarEstadoEventoAutorizacionMasivo.java:199-208 (verbatim)
    public static class UpdateEtapaEvento {
        public Long IdEtapaEvento;
        public String IdEstado;
        public Long IdUsuarioCambioEstado;
        public Timestamp FechaHoraCambioEstado;

        public String toString() {
            return Utils.printJson(this, false);
        }
    }

	// legacy Plataforma/JAVA/BcsPlataformaServicios/src/cl/bcs/plataforma/aut/ModificarEstadoEventoAutorizacionMasivo.java:210-214 (verbatim)
    public static class InsertAutorizadoEtapaEvento extends AutorizadoEtapaEvento {
        public String toString() {
            return Utils.printJson(this, false);
        }
    }
}
