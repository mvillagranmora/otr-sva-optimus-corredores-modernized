package cl.bcs.plataforma.service.pla;

import cl.bcs.plataforma.service.JavaCallService;
import cl.bcs.plataforma.service.utils.ConfiguracionSistemaUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ConsultarParametrosService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarParametrosService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final ConfiguracionSistemaUtil config;

    public ConsultarParametrosService(ConfiguracionSistemaUtil config) {
        this.config = config;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.pla.ConsultarParametros"; }

    @Override
    public JsonObject execute(JsonObject params) {
        log.debug("INICIO::ConsultarParametros");
        EntradaConsultarParametro in = GSON.fromJson(params, EntradaConsultarParametro.class);
        SalidaConsultarParametro salida = new SalidaConsultarParametro();

        try {
            for (ParametroEntrada p : in.p_ListaParametros) {
                ParametroSalida ps = new ParametroSalida();
                ps.moduloParametro = p.moduloParametro;
                ps.nombreParametro = p.nombreParametro;
                try {
                    ps.valorParametro = config.getParametroString(in.p_IdEmpresa, p.moduloParametro, p.nombreParametro);
                } catch (Exception e) {
                    log.warn("Parámetro no encontrado: {}.{}", p.moduloParametro, p.nombreParametro);
                    ps.valorParametro = null;
                }
                salida.P_OUT_LISTA_PARAMETROS.add(ps);
            }
        } catch (Exception e) {
            log.error("Error en ConsultarParametros: {}", e.getMessage(), e);
            salida.P_OUT_ERRORESCONTADOR = 1L;
        }

        return JsonParser.parseString(GSON.toJson(salida)).getAsJsonObject();
    }

    public static class EntradaConsultarParametro {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public List<ParametroEntrada> p_ListaParametros = new ArrayList<>();
    }

    public static class ParametroEntrada {
        public String moduloParametro;
        public String nombreParametro;
    }

    public static class SalidaConsultarParametro {
        public List<Object> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<ParametroSalida> P_OUT_LISTA_PARAMETROS = new ArrayList<>();
    }

    public static class ParametroSalida {
        public String moduloParametro;
        public String nombreParametro;
        public String valorParametro;
    }
}
