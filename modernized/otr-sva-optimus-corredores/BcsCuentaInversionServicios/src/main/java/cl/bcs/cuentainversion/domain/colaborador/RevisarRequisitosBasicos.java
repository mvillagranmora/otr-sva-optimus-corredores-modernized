package cl.bcs.cuentainversion.domain.colaborador;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import cl.bcs.cuentainversion.service.CtaJavaCallInfra.MensajeSalida;

/** JSON contract of {@code cl.bcs.plataforma.pla.RevisarRequisitosBasicos}: checks company, user and module requirements. */
public final class RevisarRequisitosBasicos {

    private RevisarRequisitosBasicos() {
    }

    public static final String CLASE = "cl.bcs.plataforma.pla.RevisarRequisitosBasicos";

    public static class Salida {
        public List<MensajeSalida> p_out_Mensajes = new ArrayList<>();
    }

    /** Input in the key order of the legacy call log. */
    public static Map<String, Object> entrada(Long idEmpresa, Long idUsuario, String idModulo) {
        Map<String, Object> e = new LinkedHashMap<>();
        e.put("p_IdEmpresa", idEmpresa);
        e.put("p_IdUsuario", idUsuario);
        e.put("p_IdModulo", idModulo);
        return e;
    }
}
