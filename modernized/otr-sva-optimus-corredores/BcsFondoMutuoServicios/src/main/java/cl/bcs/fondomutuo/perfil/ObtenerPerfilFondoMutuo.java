package cl.bcs.fondomutuo.perfil;

import java.util.ArrayList;
import java.util.List;

import cl.bcs.fondomutuo.kit.MensajeSalida;

/**
 * Port of legacy {@code cl.bcs.fondomutuo.ObtenerPerfilFondoMutuo} (74 LOC): the fund-module profile of a user ({@code PERFIL_COMERCIAL} or
 * {@code PERFIL_BACKOFFICE}), or no profile ({@code PerfilFondoMutuo} stays null, counter 0) when the user has none. One read through
 * {@link PerfilFondoMutuoPort}; no state. A null input is a NullPointerException, as in the legacy.
 */
public class ObtenerPerfilFondoMutuo {

    private final PerfilFondoMutuoPort perfiles;

    public ObtenerPerfilFondoMutuo(PerfilFondoMutuoPort perfiles) {
        this.perfiles = perfiles;
    }

    public static class EntradaObtenerPerfilFondoMutuo {
        public Long p_IdUsuario;
    }

    public static class SalidaObtenerPerfilFondoMutuo {
        public Long P_OUT_ERRORESCONTADOR;
        public List<MensajeSalida> P_OUT_MENSAJES;
        public String PerfilFondoMutuo;

        public SalidaObtenerPerfilFondoMutuo() {
            this.P_OUT_ERRORESCONTADOR = 0L;
            this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
        }
    }

    public SalidaObtenerPerfilFondoMutuo logica(EntradaObtenerPerfilFondoMutuo entrada) throws Exception {
        SalidaObtenerPerfilFondoMutuo out = new SalidaObtenerPerfilFondoMutuo();
        List<String> perfilesFondoMutuo = perfiles.perfiles(entrada.p_IdUsuario);
        if (perfilesFondoMutuo.size() > 0) {
            out.PerfilFondoMutuo = perfilesFondoMutuo.get(0);
        }
        return out;
    }
}
