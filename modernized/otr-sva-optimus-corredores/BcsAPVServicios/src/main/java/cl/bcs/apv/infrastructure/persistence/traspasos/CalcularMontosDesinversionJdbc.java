package cl.bcs.apv.infrastructure.persistence.traspasos;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import cl.bcs.apv.application.comun.Sneaky;
import cl.bcs.apv.domain.port.out.traspasos.CalcularMontosDesinversionRepositorio;

/** JDBC adapter of {@link CalcularMontosDesinversionRepositorio}; the SQL files are the legacy ones, byte for byte. */
@Repository
public class CalcularMontosDesinversionJdbc implements CalcularMontosDesinversionRepositorio {

    private static final String SQL_DETALLE = SqlTraspasos.recurso("ObtenerDetalleTraspasoDesinversion");
    private static final String SQL_VALOR_CUOTA = SqlTraspasos.recurso("CalcularMontoNumeroCuota_SelectValorCuotaTipoCambio");

    private final SqlTraspasos sql;

    public CalcularMontosDesinversionJdbc(NamedParameterJdbcTemplate jdbc) {
        this.sql = new SqlTraspasos(jdbc);
    }

    @Override
    public List<DetalleTraspasoDesinversion> detalleTraspaso(Long idTraspaso) {
        Map<String, Object> p = new HashMap<>();
        p.put("IdTraspaso", idTraspaso);
        return Sneaky.sinChequear(() -> sql.consultar(SQL_DETALLE, p, DetalleTraspasoDesinversion.class));
    }

    @Override
    public List<ValorCuotaTipoParidad> valorCuotaTipoCambio(Long idEmpresa, String idSerieFondo) {
        Map<String, Object> p = new HashMap<>();
        p.put("IdEmpresa", idEmpresa);
        p.put("IdSerieFondo", idSerieFondo);
        return Sneaky.sinChequear(() -> sql.consultar(SQL_VALOR_CUOTA, p, ValorCuotaTipoParidad.class));
    }
}
