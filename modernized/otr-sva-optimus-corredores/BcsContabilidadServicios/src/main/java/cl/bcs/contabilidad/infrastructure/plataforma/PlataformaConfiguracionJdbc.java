package cl.bcs.contabilidad.infrastructure.plataforma;

import cl.bcs.contabilidad.domain.model.EmpresaInfo;
import cl.bcs.contabilidad.infrastructure.persistence.SqlResources;
import cl.bcs.contabilidad.infrastructure.persistence.SqlRow;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Reads the platform configuration and company data straight from HANA, on the caller's connection and
 * transaction, as the legacy did.
 *
 * <p>Legacy {@code cl.bcs.plataforma.pla.utils.ConfiguracionSistema}: one verbatim SQL per module, each returning
 * {@code IdEmpresa, CodigoParametro, Valor}, filtered in memory by company. The legacy parameter cache is disabled
 * ({@code if (true)}), so every read queries again. An unknown module fails, as the legacy did. The company comes
 * from the legacy {@code ConsultarEmpresa_Select.sql}.
 */
final class PlataformaConfiguracionJdbc {

    private static final Map<String, String> SQL_POR_MODULO = Map.ofEntries(
            Map.entry("PLATAFORMA", "cl/bcs/contabilidad/sql/ConsultarConfiguracionPlataforma.sql"),
            Map.entry("ORDENES", "cl/bcs/contabilidad/sql/ConsultarConfiguracionOrdenes.sql"),
            Map.entry("CUENTAINVERSION", "cl/bcs/contabilidad/sql/ConsultarConfiguracionCuentaInversion.sql"),
            Map.entry("TESORERIA", "cl/bcs/contabilidad/sql/ConsultarConfiguracionTesoreria.sql"),
            Map.entry("FACTURACION", "cl/bcs/contabilidad/sql/ConsultarConfiguracionFacturacion.sql"),
            Map.entry("SOCIODENEGOCIO", "cl/bcs/contabilidad/sql/ConsultarConfiguracionSocioDeNegocio.sql"),
            Map.entry("CUSTODIA", "cl/bcs/contabilidad/sql/ConsultarConfiguracionCustodia.sql"),
            Map.entry("APV", "cl/bcs/contabilidad/sql/ConsultarConfiguracionAPV.sql"),
            Map.entry("CONTABILIDAD", "cl/bcs/contabilidad/sql/ConsultarConfiguracionContabilidad.sql"),
            Map.entry("FONDOMUTUO", "cl/bcs/contabilidad/sql/ConsultarConfiguracionFondoMutuo.sql"),
            Map.entry("NORMATIVA", "cl/bcs/contabilidad/sql/ConsultarConfiguracionNormativa.sql"),
            Map.entry("MESA", "cl/bcs/contabilidad/sql/ConsultarConfiguracionMesa.sql"),
            Map.entry("PRODUCTO", "cl/bcs/contabilidad/sql/ConsultarConfiguracionProducto.sql"),
            Map.entry("LIMITE", "cl/bcs/contabilidad/sql/ConsultarConfiguracionLimite.sql"));

    private static final String SQL_EMPRESA = "cl/bcs/contabilidad/sql/ConsultarEmpresa_Select.sql";

    private record Registro(Long idEmpresa, String codigoParametro, String valor) {}

    private final NamedParameterJdbcTemplate jdbc;

    PlataformaConfiguracionJdbc(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Empty when the module has no such parameter for the company; database errors propagate. */
    Optional<String> parametro(Long idEmpresa, String modulo, String parametro) {
        String sql = SQL_POR_MODULO.get(modulo);
        if (sql == null) {
            throw new IllegalArgumentException("Configuracion modulo " + modulo + " no encontrada");
        }
        List<Registro> registros = jdbc.query(sinPuntoYComa(SqlResources.load(sql)), new MapSqlParameterSource(),
                SqlRow.mapper(r -> new Registro(r.lng("IdEmpresa"), r.str("CodigoParametro"), r.str("Valor"))));
        // Legacy keeps the LAST row per (module, company, parameter) (HashMap.put in getConfiguracionModulo)
        String valor = null;
        boolean encontrado = false;
        for (Registro r : registros) {
            if (Objects.equals(r.idEmpresa(), idEmpresa) && Objects.equals(r.codigoParametro(), parametro)) {
                valor = r.valor();
                encontrado = true;
            }
        }
        return encontrado ? Optional.ofNullable(valor) : Optional.empty();
    }

    /** Legacy getParametro: a missing parameter is an error. */
    String parametroRequerido(Long idEmpresa, String modulo, String parametro) {
        return parametro(idEmpresa, modulo, parametro).orElseThrow(() -> new IllegalStateException(
                "Parametro " + parametro + " en modulo " + modulo + " no encontrado"));
    }

    EmpresaInfo empresa(Long idEmpresa) {
        List<EmpresaInfo> filas = jdbc.query(sinPuntoYComa(SqlResources.load(SQL_EMPRESA)),
                new MapSqlParameterSource("IdEmpresa", idEmpresa),
                SqlRow.mapper(r -> new EmpresaInfo(r.lng("IdEmpresa"), r.lng("IdPais"), r.str("DocumentoIdentidad"),
                        r.str("Nombre"))));
        if (filas.isEmpty()) {
            throw new IllegalStateException("Empresa " + idEmpresa + " no encontrada");
        }
        return filas.getFirst();
    }

    /** The legacy files end with ';', which the HANA driver accepts in a prepared statement but H2 does not. */
    private static String sinPuntoYComa(String sql) {
        String s = sql.strip();
        return s.endsWith(";") ? s.substring(0, s.length() - 1) : s;
    }
}
