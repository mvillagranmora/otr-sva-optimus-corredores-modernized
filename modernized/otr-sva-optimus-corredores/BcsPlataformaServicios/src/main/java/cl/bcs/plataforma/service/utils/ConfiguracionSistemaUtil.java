package cl.bcs.plataforma.service.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Spring-managed equivalent of legacy ConfiguracionSistema static utility.
 * Queries the PLA.Configuracion table for system parameters by module and code.
 */
@Component
public class ConfiguracionSistemaUtil {

    private static final Logger log = LoggerFactory.getLogger(ConfiguracionSistemaUtil.class);

    private final NamedParameterJdbcTemplate jdbc;
    private final Map<String, String> SQL_BY_MODULE;

    public ConfiguracionSistemaUtil(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_BY_MODULE = buildSqlMap();
    }

    public String getParametroString(Long idEmpresa, String modulo, String nombre) {
        return getParametro(idEmpresa, modulo, nombre);
    }

    public Long getParametroLong(Long idEmpresa, String modulo, String nombre) {
        return Long.parseLong(getParametro(idEmpresa, modulo, nombre));
    }

    public BigDecimal getParametroDecimal(Long idEmpresa, String modulo, String nombre) {
        return new BigDecimal(getParametro(idEmpresa, modulo, nombre));
    }

    public Set<String> getParametroSet(Long idEmpresa, String modulo, String nombre, String delimiter) {
        String val = getParametro(idEmpresa, modulo, nombre);
        return Arrays.stream(val.split(delimiter)).collect(Collectors.toSet());
    }

    private String getParametro(Long idEmpresa, String modulo, String nombre) {
        String sql = SQL_BY_MODULE.get(modulo.toUpperCase());
        if (sql == null) throw new IllegalArgumentException("Módulo de configuración no encontrado: " + modulo);

        List<RegistroConfiguracion> registros = jdbc.query(sql,
            Collections.emptyMap(), BeanPropertyRowMapper.newInstance(RegistroConfiguracion.class));

        return registros.stream()
            .filter(r -> Objects.equals(r.IdEmpresa, idEmpresa) && nombre.equals(r.CodigoParametro))
            .map(r -> r.Valor)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(
                "Parámetro no encontrado: empresa=" + idEmpresa + " módulo=" + modulo + " param=" + nombre));
    }

    public static class RegistroConfiguracion {
        public Long IdEmpresa;
        public String CodigoParametro;
        public String Valor;
    }

    private Map<String, String> buildSqlMap() {
        Map<String, String> map = new HashMap<>();
        map.put("PLATAFORMA",    loadSql("cl/bcs/plataforma/pla/sql/ConsultarConfiguracionPlataforma.sql"));
        map.put("ORDENES",       loadSql("cl/bcs/plataforma/pla/sql/ConsultarConfiguracionOrdenes.sql"));
        map.put("CUENTAINVERSION", loadSql("cl/bcs/plataforma/pla/sql/ConsultarConfiguracionCuentaInversion.sql"));
        map.put("TESORERIA",     loadSql("cl/bcs/plataforma/pla/sql/ConsultarConfiguracionTesoreria.sql"));
        map.put("FACTURACION",   loadSql("cl/bcs/plataforma/pla/sql/ConsultarConfiguracionFacturacion.sql"));
        map.put("SOCIODENEGOCIO", loadSql("cl/bcs/plataforma/pla/sql/ConsultarConfiguracionSocioDeNegocio.sql"));
        map.put("CUSTODIA",      loadSql("cl/bcs/plataforma/pla/sql/ConsultarConfiguracionCustodia.sql"));
        map.put("APV",           loadSql("cl/bcs/plataforma/pla/sql/ConsultarConfiguracionAPV.sql"));
        map.put("CONTABILIDAD",  loadSql("cl/bcs/plataforma/pla/sql/ConsultarConfiguracionContabilidad.sql"));
        map.put("FONDOMUTUO",    loadSql("cl/bcs/plataforma/pla/sql/ConsultarConfiguracionFondoMutuo.sql"));
        map.put("NORMATIVA",     loadSql("cl/bcs/plataforma/pla/sql/ConsultarConfiguracionNormativa.sql"));
        map.put("MESA",          loadSql("cl/bcs/plataforma/pla/sql/ConsultarConfiguracionMesa.sql"));
        map.put("PRODUCTO",      loadSql("cl/bcs/plataforma/pla/sql/ConsultarConfiguracionProducto.sql"));
        map.put("LIMITE",        loadSql("cl/bcs/plataforma/pla/sql/ConsultarConfiguracionLimite.sql"));
        return Collections.unmodifiableMap(map);
    }

    private static String loadSql(String path) {
        try (InputStream is = ConfiguracionSistemaUtil.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
