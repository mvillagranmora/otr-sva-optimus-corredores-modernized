package cl.bcs.fondomutuo.adapter.out.hana;

import cl.bcs.fondomutuo.domain.ValorCuota;
import cl.bcs.fondomutuo.port.out.ValorCuotaPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class HanaValorCuotaAdapter implements ValorCuotaPort {

    private static final Logger log = LoggerFactory.getLogger(HanaValorCuotaAdapter.class);

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlValorCuota;
    private final String sqlValorCuotaFecha;

    public HanaValorCuotaAdapter(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlValorCuota = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/CalcularMontoNumeroCuota_SelectValorCuotaTipoCambio.sql").getInputStream(),
            StandardCharsets.UTF_8);
        // For date-based lookup use historical table
        this.sqlValorCuotaFecha = """
            SELECT
                "IdSerieFondo",
                "FechaCierre",
                "ValorCuota",
                "IdMoneda",
                '' AS "Observacion",
                "FechaCarga"
            FROM "BSCL.FondoMutuo.BD::FMM.ValorCuota"
            WHERE "IdEmpresa" = :IdEmpresa
              AND "IdSerieFondo" = :IdSerieFondo
              AND "FechaCierre" = :Fecha
            LIMIT 1
            """;
    }

    @Override
    public Optional<ValorCuota> findBySerieAndFecha(String idSerieFondo, LocalDate fecha, Long idEmpresa) {
        try {
            List<Map<String, Object>> rows = jdbc.queryForList(sqlValorCuotaFecha,
                new MapSqlParameterSource()
                    .addValue("IdEmpresa", idEmpresa)
                    .addValue("IdSerieFondo", idSerieFondo)
                    .addValue("Fecha", fecha != null ? Date.valueOf(fecha) : null));
            if (rows.isEmpty()) return Optional.empty();
            Map<String, Object> row = rows.get(0);
            return Optional.of(new ValorCuota(
                idSerieFondo,
                fecha,
                row.get("ValorCuota") instanceof BigDecimal v ? v : BigDecimal.ZERO,
                row.get("IdMoneda") != null ? String.valueOf(row.get("IdMoneda")) : "",
                "",
                fecha,
                idEmpresa
            ));
        } catch (Exception e) {
            log.warn("findBySerieAndFecha failed for serie={} fecha={}: {}", idSerieFondo, fecha, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public List<ValorCuota> findBySeries(List<String> idsSerieFondo, Long idEmpresa) {
        if (idsSerieFondo == null || idsSerieFondo.isEmpty()) return List.of();
        try {
            List<Map<String, Object>> rows = jdbc.queryForList(sqlValorCuota,
                new MapSqlParameterSource()
                    .addValue("IdEmpresa", idEmpresa)
                    .addValue("IdSerieFondo", idsSerieFondo));
            return rows.stream().map(row -> new ValorCuota(
                String.valueOf(row.get("IdSerieFondo")),
                LocalDate.now(),
                row.get("ValorCuota") instanceof BigDecimal v ? v : BigDecimal.ZERO,
                "",
                "",
                LocalDate.now(),
                idEmpresa
            )).toList();
        } catch (Exception e) {
            log.warn("findBySeries failed for {} series: {}", idsSerieFondo.size(), e.getMessage());
            return List.of();
        }
    }
}
