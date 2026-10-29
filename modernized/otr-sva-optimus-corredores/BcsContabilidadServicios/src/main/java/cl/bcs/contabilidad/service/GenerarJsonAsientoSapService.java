package cl.bcs.contabilidad.service;

import cl.bcs.contabilidad.domain.model.sap.AsientoEnvio;
import cl.bcs.contabilidad.domain.model.sap.CuentaSap;
import cl.bcs.contabilidad.domain.model.sap.PreComprobanteEnvio;
import cl.bcs.contabilidad.domain.model.sap.ResultadoAsientoSap;
import cl.bcs.contabilidad.domain.port.out.PlanCuentasSapPort;
import cl.bcs.contabilidad.domain.port.out.PlataformaPort;
import cl.bcs.contabilidad.domain.service.AsientoSapMapper;
import cl.bcs.contabilidad.infrastructure.persistence.SqlResources;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * Reads the staged pre-vouchers and builds the SAP B1 JournalVouchers and BusinessPartners
 * ({@link AsientoSapMapper}). Legacy {@code cl.bcs.contabilidad.GenerarJsonAsientoSap.logica}
 * (GenerarJsonAsientoSap.java:150-223), CONT-B-20..24.
 *
 * <p>Deliberate deviation (CONT-B-24): legacy {@code call()} returned {@code null}; {@link #execute} returns
 * {@code P_OUT_SOCIOS} / {@code P_OUT_VOUCHERS} with the wire JSON of the result.
 */
@Service
public class GenerarJsonAsientoSapService {

    private final String SQL_SELECT_PRECOMPROBANTE = SqlResources.load("cl/bcs/contabilidad/sql/InterfazContable_SelectPreComprobante.sql");
    private final String SQL_SELECT_PRECOMPROBANTE_ASIENTO = SqlResources.load("cl/bcs/contabilidad/sql/InterfazContable_SelectPrecomprobanteAsiento.sql");

    private final NamedParameterJdbcTemplate jdbc;
    private final PlanCuentasSapPort planCuentas;
    private final PlataformaPort plataforma;

    public GenerarJsonAsientoSapService(NamedParameterJdbcTemplate jdbc, PlanCuentasSapPort planCuentas, PlataformaPort plataforma) {
        this.jdbc = jdbc;
        this.planCuentas = planCuentas;
        this.plataforma = plataforma;
    }

    record Entrada(@SerializedName("p_IdPreComprobantes") List<Long> idPreComprobantes,
                   @SerializedName("p_IdEmpresa") Long idEmpresa,
                   @SerializedName("p_IdUsuario") Long idUsuario,
                   @SerializedName("p_Destino") String destino) {}

    public JsonObject execute(JsonObject params) {
        Entrada e = JavaCallJson.read(JavaCallJson.legacy(), params, Entrada.class);
        ResultadoAsientoSap r = generar(e.idEmpresa(), e.idPreComprobantes() == null ? List.of() : e.idPreComprobantes());
        JsonObject out = new JsonObject();
        out.add("P_OUT_MENSAJES", new JsonArray());
        out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
        out.add("P_OUT_SOCIOS", JsonParser.parseString(AsientoSapMapper.toSapJson(r.socios())));
        out.add("P_OUT_VOUCHERS", JsonParser.parseString(AsientoSapMapper.toSapJson(r.vouchers())));
        return out;
    }

    /** Vouchers for {@code idPreComprobantes}, in that order (duplicates repeated, ids without header skipped). */
    public ResultadoAsientoSap generar(Long idEmpresa, List<Long> idPreComprobantes) {
        String localizacion = plataforma.obtenerLocalizacionSistema(idEmpresa);
        List<CuentaSap> cuentas = planCuentas.consultarPlanCuentas();
        List<Long> ids = idPreComprobantes.stream().filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) return ResultadoAsientoSap.vacio(); // legacy IN (NULL): nothing matches
        MapSqlParameterSource params = new MapSqlParameterSource("IdPrecomprobante", ids);
        List<PreComprobanteEnvio> cabeceras = jdbc.query(SQL_SELECT_PRECOMPROBANTE, params, new DataClassRowMapper<>(PreComprobanteEnvio.class));
        List<AsientoEnvio> asientos = jdbc.query(SQL_SELECT_PRECOMPROBANTE_ASIENTO, params, new DataClassRowMapper<>(AsientoEnvio.class));
        return AsientoSapMapper.generar(idPreComprobantes, cabeceras, asientos, cuentas, localizacion);
    }
}
