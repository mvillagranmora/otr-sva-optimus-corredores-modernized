package cl.bcs.plataforma.service.cre;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Migrated from cl.bcs.plataforma.cre.EntregarSistema (JavaCall).
 * Triggers system delivery via HANA stored procedure
 * "BSCL.Plataforma.BD.Servicios::CRE.EntregaSistema".
 * Also optionally advances pauta via pau.* services.
 * Depends on pla.* pau.AdelantarPauta, pau.AgregarControlPauta, pau.ConsultarPautaEjecutar.
 */
@Service
public class EntregarSistemaService implements JavaCallService {

    private static final Gson GSON = new Gson();
    private final NamedParameterJdbcTemplate jdbc;

    public EntregarSistemaService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() {
        return "cl.bcs.plataforma.cre.EntregarSistema";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        try {
            Long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : null;
            Long idUsuario = params.has("p_IdUsuario") ? params.get("p_IdUsuario").getAsLong() : null;
            String idModulo = params.has("p_IdModulo") ? params.get("p_IdModulo").getAsString() : null;
            String simular = params.has("p_simularOperacion") ? params.get("p_simularOperacion").getAsString() : "N";
            String usaReglasNegocio = params.has("p_UsaReglasNegocio") ? params.get("p_UsaReglasNegocio").getAsString() : "S";

            // TODO: requires pla.* utility — complete after pla subpackage migration
            // HANA stored procedure call via SimpleJdbcCall:
            //   withSchemaName("BSCL").withProcedureName("Plataforma.BD.Servicios::CRE.EntregaSistema")
            // pau.ConsultarPautaEjecutar / AdelantarPauta for optional pauta advance.

            result.add("p_out_Validaciones", new JsonArray());
            result.add("p_out_AdvertenciasDetalle", new JsonArray());
            result.add("p_out_ErroresDetalle", new JsonArray());
            result.addProperty("P_OUT_ADVERTENCIASCONTADOR", 0);
            result.addProperty("P_OUT_ERRORESCONTADOR", 0);
            result.addProperty("p_out_SistemaCerrado", "N");
            result.addProperty("p_out_TipoProgramaAdelantado", "N");
        } catch (Exception e) {
            result.addProperty("P_OUT_ERRORESCONTADOR", 1);
            result.addProperty("error", e.getMessage());
        }
        return result;
    }
}
