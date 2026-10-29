package cl.bcs.tesoreria.javacall.kit.colaborador;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import cl.bcs.tesoreria.javacall.kit.JsonLegacy;
import cl.bcs.tesoreria.javacall.kit.MensajeSalida;
import cl.bcs.tesoreria.port.out.Colaboradores;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Utils;

/**
 * Contract of the legacy service AgregarPrograma as the Tesoreria family-B services use it through the {@link Colaboradores} port.
 * The nested classes keep the legacy names and field names (they ARE the JSON contract); toString is the legacy JSON dump.
 */
public final class AgregarPrograma {

    private AgregarPrograma() {
    }

    public static class InAgregarPrograma {
        public Long             p_IdEmpresa;
        public Long             p_IdUsuario;
        public String            p_IdModulo;
        public String            p_IdTipoPrograma;
        public Long                p_Segundos;
        public String            p_FechaSistema;
        public List<Parametro>    p_Parametros;
        public String            p_Comentario;
        public static class Parametro{
            public String    NombreParametro;
            public String    Valor;
            public String    ValorBlob;
        }
        public InAgregarPrograma(){
            this.p_Parametros = new ArrayList<Parametro>();
            this.p_Comentario = "";
        }
    }
    public static class OutAgregarPrograma {
        public Long P_OUT_ERRORESCONTADOR;
        public Long P_OUT_IDPROGRAMA;
        public Long P_OUT_IDGRUPOPARAMETRO;
        public String P_OUT_MENSAJE;
    }


    /** Calls the collaborator; the legacy {@code logica(con, entrada)}. */
    public static OutAgregarPrograma invocar(Colaboradores colaboradores, InAgregarPrograma entrada) {
        JsonObject r = colaboradores.invocar("AgregarPrograma", JsonLegacy.aJson(entrada));
        return JsonLegacy.de(r, OutAgregarPrograma.class);
    }
}
