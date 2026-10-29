package cl.bcs.apv.infrastructure.persistence.contratos;

import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import cl.bcs.apv.application.comun.Sneaky;
import cl.bcs.apv.domain.port.out.contratos.CambiarEstadoNotificacionContratosRepositorio;
import cl.bcs.apv.infrastructure.jdbc.SqlNombrado;

/** JDBC adapter of {@link CambiarEstadoNotificacionContratosRepositorio}; the SQL file is the legacy one, byte for byte. */
@Repository
public class CambiarEstadoNotificacionContratosJdbc implements CambiarEstadoNotificacionContratosRepositorio {

    private static final String SQL_UPDATE = SqlNombrado.recurso("CambiarEstadoEnvioNotificacionContratoSAAPV");

    private final SqlNombrado sql;

    public CambiarEstadoNotificacionContratosJdbc(NamedParameterJdbcTemplate jdbc) {
        this.sql = new SqlNombrado(jdbc);
    }

    /** Batch parameters: the legacy {@code Params} class (public fields named as the SQL parameters). */
    public static class Params {
        public String FolioContratoSAAPV;
        public String IdEstadoEnvioNotificacion;
    }

    @Override
    public void actualizarEstadoEnvio(String idEstadoEnvioNotificacion, List<String> folios) {
        List<Params> lote = new ArrayList<>();
        for (String folio : folios) {
            Params p = new Params();
            p.IdEstadoEnvioNotificacion = idEstadoEnvioNotificacion;
            p.FolioContratoSAAPV = folio;
            lote.add(p);
        }
        Sneaky.sinChequear(() -> sql.lote(SQL_UPDATE, lote));
    }
}
