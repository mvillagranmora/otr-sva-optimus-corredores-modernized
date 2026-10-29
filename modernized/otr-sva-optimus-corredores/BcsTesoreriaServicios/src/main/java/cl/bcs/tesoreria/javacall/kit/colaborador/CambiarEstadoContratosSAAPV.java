package cl.bcs.tesoreria.javacall.kit.colaborador;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import cl.bcs.tesoreria.javacall.kit.JsonLegacy;
import cl.bcs.tesoreria.javacall.kit.MensajeSalida;
import cl.bcs.tesoreria.port.out.Colaboradores;
import cl.bcs.utils.Utils;

/**
 * Contract of the legacy service CambiarEstadoContratosSAAPV as the PAC mandate services use it through the {@link Colaboradores} port.
 * The nested classes keep the legacy names and field names (they ARE the JSON contract); toString is the legacy JSON dump.
 */
public final class CambiarEstadoContratosSAAPV {

    private CambiarEstadoContratosSAAPV() {
    }

    public static class ContratosSAAPV {
        public Long                    IdPersona;
        public Long                    IdCliente;
        public Long                    IdEmpleador;
        public String                IdFolioContratoSAAPV;
        public String                FechaRevocacion; 
        public String                IdEstadoEnvioNotificacion;
        public String                IdEstadoSolicitud;
        public String                IdEstado;
        
        public Long                 IdGlosa;
        public String                 DescripcionGlosa;
        
        public ContratosSAAPV() {
            IdGlosa = 0L;
            DescripcionGlosa = "";
        }
        
        public String toString() {
            return Utils.printJson(this, false);
        }
    }
    public static class ContratosSAAPV_Entrada {
        public Long                p_IdEmpresa;
        public Long                p_IdUsuario;
        public String            p_IdModulo;
        public String            p_Opcion; // REV = Revocacion, CER = Cerrar, COM = Completar 
        public List<ContratosSAAPV>    p_ContratosSAAPV;
        public String            p_DesdePac;
        
        public ContratosSAAPV_Entrada(){
            p_ContratosSAAPV = new ArrayList<ContratosSAAPV>();
            p_DesdePac         = "N";
        }
       
        public String toString() {
            return Utils.printJson(this, false);
        }
    }
    public static class ContratosSAAPV_Salida {        
        public List<MensajeSalida>    P_OUT_MENSAJES;
        public Long                    P_OUT_ERRORESCONTADOR;
        public Long                    P_OUT_ADVERTENCIASCONTADOR;
        
        public String toString() {
            return Utils.printJson(this, false);
        }
        
        public ContratosSAAPV_Salida(){
            P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
            P_OUT_ERRORESCONTADOR = 0L;
            P_OUT_ADVERTENCIASCONTADOR = 0L;
        }        
    }


    /** Calls the collaborator; the legacy {@code logica(con, entrada)}. */
    public static ContratosSAAPV_Salida invocar(Colaboradores colaboradores, ContratosSAAPV_Entrada entrada) {
        JsonObject r = colaboradores.invocar("CambiarEstadoContratosSAAPV", JsonLegacy.aJson(entrada));
        return JsonLegacy.de(r, ContratosSAAPV_Salida.class);
    }
}
