package cl.bcs.plataforma.adapter.in.javacall;

import java.sql.Connection;

import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.adapter.out.jdbc.ModuloJdbcRepository;
import cl.bcs.plataforma.cre.ConsultarEstadoModuloService;

/** JavaCall 600017. One service and one repository per call, as the legacy {@code PluginLoader} instantiated the class per call. */
@Component
public class ConsultarEstadoModuloHandler implements JavaCallHandler {

    public static final String FQN = "cl.bcs.plataforma.cre.ConsultarEstadoModulo";

    @Override
    public String fqn() {
        return FQN;
    }

    @Override
    public JsonObject call(Connection con, JsonObject jin) {
        return new ConsultarEstadoModuloService(new ModuloJdbcRepository(con)).call(jin);
    }
}
