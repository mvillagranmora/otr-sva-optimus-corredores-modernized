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
 * Contract of the legacy service AnulacionContratoCSC as the PAC mandate services use it through the {@link Colaboradores} port.
 * The nested classes keep the legacy names and field names (they ARE the JSON contract); toString is the legacy JSON dump.
 */
public final class AnulacionContratoCSC {

    private AnulacionContratoCSC() {
    }

    public static class InContrato{
        public Long     IdContrato;
        public String     Estado="";
        
        
        public String toString() {
            return Utils.printJson(this, false);
        }
        
    }
    public static class EntradaAnulacionContrato {
        
        public     Long                        p_IdEmpresa;
        public    Long                        p_IdUsuario;
        public     String                        p_IdModulo;
        public    List<InContrato>            p_Contrato;
        public  String                      p_DesdePac;
        
        
        public EntradaAnulacionContrato(){
            p_Contrato                     = new ArrayList<InContrato>();
            p_DesdePac                  = "N";
        }
        
        public String toString() {
            return Utils.printJson(this, false);
        }
    }    
        public static class SalidaAnulacionContrato{
            public Long P_OUT_ERRORESCONTADOR;
            public List<MensajeSalida> P_OUT_MENSAJES;
            public List<Object> P_OUT_RESULTADO;
            
            public SalidaAnulacionContrato(){
                this.P_OUT_ERRORESCONTADOR=0L;
                this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
                this.P_OUT_RESULTADO = new ArrayList<>();
            }
            
            public String toString() {
                return Utils.printJson(this, false);
            }
        }


    /** Calls the collaborator; the legacy {@code logica(con, entrada)}. */
    public static SalidaAnulacionContrato invocar(Colaboradores colaboradores, EntradaAnulacionContrato entrada) {
        JsonObject r = colaboradores.invocar("AnulacionContratoCSC", JsonLegacy.aJson(entrada));
        return JsonLegacy.de(r, SalidaAnulacionContrato.class);
    }
}
