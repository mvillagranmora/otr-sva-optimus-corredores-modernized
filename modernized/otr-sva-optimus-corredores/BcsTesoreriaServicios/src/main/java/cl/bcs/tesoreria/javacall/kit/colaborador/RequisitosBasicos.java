package cl.bcs.tesoreria.javacall.kit.colaborador;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;

import cl.bcs.tesoreria.javacall.kit.JsonLegacy;
import cl.bcs.tesoreria.javacall.kit.MensajeSalida;
import cl.bcs.tesoreria.port.out.Colaboradores;

/** Contract of the legacy {@code RevisarRequisitosBasicos.logica(con, p_IdEmpresa, p_IdUsuario, p_IdModulo)}: the messages of the basic requirements (company, user, module). */
public final class RequisitosBasicos {

    public static class Salida {
        public List<MensajeSalida> p_out_Mensajes = new ArrayList<>();
    }

    private RequisitosBasicos() {
    }

    public static Salida revisar(Colaboradores colaboradores, Long idEmpresa, Long idUsuario, String idModulo) {
        Map<String, Object> entrada = new LinkedHashMap<>();
        entrada.put("p_IdEmpresa", idEmpresa);
        entrada.put("p_IdUsuario", idUsuario);
        entrada.put("p_IdModulo", idModulo);
        JsonObject r = colaboradores.invocar("RevisarRequisitosBasicos", JsonLegacy.aJson(entrada));
        Salida salida = JsonLegacy.de(r, Salida.class);
        return salida == null ? new Salida() : salida;
    }
}
