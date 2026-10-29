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
 * Contract of the legacy service RevocacionContratoCSC as the PAC mandate services use it through the {@link Colaboradores} port.
 * The nested classes keep the legacy names and field names (they ARE the JSON contract); toString is the legacy JSON dump.
 */
public final class RevocacionContratoCSC {

    private RevocacionContratoCSC() {
    }

    public static class InContrato{
        public Long     IdContrato;
        public String     Estado="";
        public String   FolioCSC;
        
        public String toString() {
            return Utils.printJson(this, false);
        }
        
    }
    public static class EntradaRevocacionContrato {        
        public     Long                        p_IdEmpresa;
        public    Long                        p_IdUsuario;
        public     String                        p_IdModulo;
        public    List<InContrato>            p_Contrato;    
        public  String                        p_DesdePac;
        
        public EntradaRevocacionContrato(){
            p_Contrato                     = new ArrayList<InContrato>();
            p_DesdePac                    = "N";
        }    
        public String toString() {
            return Utils.printJson(this, false);
        }
    }    
    public static class ResultadoRevocacionCSC{
        public Long IdFolioCSC;
    }
    public static class SalidaRevocacionContrato{
        public Long P_OUT_ERRORESCONTADOR;
        public List<MensajeSalida> P_OUT_MENSAJES;
        public List <ResultadoRevocacionCSC> P_OUT_RESULTADO;
            
        public SalidaRevocacionContrato(){
            this.P_OUT_ERRORESCONTADOR=0L;
            this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
            this.P_OUT_RESULTADO = new ArrayList<ResultadoRevocacionCSC>();
        }
            
        public String toString() {
            return Utils.printJson(this, false);
        }
    }


    /** Calls the collaborator; the legacy {@code logica(con, entrada)}. */
    public static SalidaRevocacionContrato invocar(Colaboradores colaboradores, EntradaRevocacionContrato entrada) {
        JsonObject r = colaboradores.invocar("RevocacionContratoCSC", JsonLegacy.aJson(entrada));
        return JsonLegacy.de(r, SalidaRevocacionContrato.class);
    }
}
