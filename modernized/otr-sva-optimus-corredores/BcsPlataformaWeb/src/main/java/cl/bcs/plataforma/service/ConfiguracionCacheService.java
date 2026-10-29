package cl.bcs.plataforma.service;

import cl.bcs.plataforma.domain.RegistroConfiguracion;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Configuration cache — 1-minute TTL, lazy-loaded from HANA.
 * Source: RegistrosConfiguracionCache + ConfiguracionServlet
 *
 * All queries are HANA-specific (multi-schema UNION ALL).
 * Tests that exercise DB access use @Disabled("requires HANA").
 */
@Service
public class ConfiguracionCacheService {

    /**
     * Multi-schema UNION ALL query for all module configuration tables.
     * Source: ConfiguracionServlet.PLATAFORMA constant
     */
    static final String SELECT_ALL_CONFIGURACIONES =
        "SELECT \"IdEmpresa\",\"CodigoParametro\",\"Valor\",'PLATAFORMA' AS \"Modulo\" " +
        "FROM \"BSCL.Plataforma.BD::PLA.Configuracion\" " +
        "UNION ALL " +
        "SELECT \"IdEmpresa\",\"CodigoParametro\",\"Valor\",'APV' AS \"Modulo\" " +
        "FROM \"BSCL.APV.BD::PLA.ConfiguracionAPV\" " +
        "UNION ALL " +
        "SELECT \"IdEmpresa\",\"CodigoParametro\",\"Valor\",'CONTABILIDAD' AS \"Modulo\" " +
        "FROM \"BSCL.Contabilidad.BD::CON.ConfiguracionContabilidad\" " +
        "UNION ALL " +
        "SELECT \"IdEmpresa\",\"CodigoParametro\",\"Valor\",'CUENTAINVERSION' AS \"Modulo\" " +
        "FROM \"BSCL.CuentaInversion.BD::CTA.ConfiguracionCuentaInversion\" " +
        "UNION ALL " +
        "SELECT \"IdEmpresa\",\"CodigoParametro\",\"Valor\",'CUSTODIA' AS \"Modulo\" " +
        "FROM \"BSCL.Custodia.BD::CUS.Configuracion\" " +
        "UNION ALL " +
        "SELECT \"IdEmpresa\",\"CodigoParametro\",\"Valor\",'FACTURACION' AS \"Modulo\" " +
        "FROM \"BSCL.Facturacion.BD::FAC.ConfiguracionFacturacion\" " +
        "UNION ALL " +
        "SELECT \"IdEmpresa\",\"NombreTipoParametro\" AS \"CodigoParametro\",\"ValorParametro\" AS \"Valor\",'FONDOMUTUO' AS \"Modulo\" " +
        "FROM \"BSCL.FondoMutuo.BD::FMM.Parametro\" " +
        "UNION ALL " +
        "SELECT \"IdEmpresa\",\"CodigoParametro\",\"Valor\",'MESA' AS \"Modulo\" " +
        "FROM \"BSCL.Mesa.BD::MES.ConfiguracionMesa\" " +
        "UNION ALL " +
        "SELECT \"IdEmpresa\",\"CodigoParametro\",\"Valor\",'ORDENES' AS \"Modulo\" " +
        "FROM \"BSCL.Orden.BD::ORD.ConfiguracionOrdenes\" " +
        "UNION ALL " +
        "SELECT \"IdEmpresa\",\"CodigoParametro\",\"Valor\",'SOCIODENEGOCIO' AS \"Modulo\" " +
        "FROM \"BSCL.SocioDeNegocio.BD::SOC.ConfiguracionSocioNegocio\" " +
        "UNION ALL " +
        "SELECT \"IdEmpresa\",\"CodigoParametro\",\"Valor\",'TESORERIA' AS \"Modulo\" " +
        "FROM \"BSCL.Tesoreria.BD::TES.ConfiguracionTesoreria\"";

    private final Cache<String, RegistroConfiguracion> cache;
    private final NamedParameterJdbcTemplate           jdbc;

    public ConfiguracionCacheService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc  = Objects.requireNonNull(jdbc, "jdbc");
        this.cache = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .concurrencyLevel(4)
                .build();
    }

    /** Returns cached entries for the requested keys, loading from DB if needed. */
    public List<RegistroConfiguracion> getConfiguraciones(List<RegistroConfiguracion> keys) {
        Objects.requireNonNull(keys, "keys must not be null");
        if (cacheMiss(keys)) {
            loadCache();
        }
        return keys.stream()
                .map(k -> cache.getIfPresent(k.toString()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /** Force-reload the cache from the DB. */
    public void reloadCache() {
        loadCache();
    }

    public long cacheSize() {
        return cache.size();
    }

    // ─── Private ─────────────────────────────────────────────────────────────

    private boolean cacheMiss(List<RegistroConfiguracion> keys) {
        return keys.stream().anyMatch(k -> cache.getIfPresent(k.toString()) == null);
    }

    private void loadCache() {
        jdbc.query(SELECT_ALL_CONFIGURACIONES, Collections.emptyMap(), rs -> {
            RegistroConfiguracion r = new RegistroConfiguracion();
            r.setIdEmpresa(rs.getLong("IdEmpresa"));
            r.setCodigoParametro(rs.getString("CodigoParametro"));
            r.setValor(rs.getString("Valor"));
            r.setModulo(rs.getString("Modulo"));
            cache.put(r.toString(), r);
        });
    }
}
