package cl.bcs.tesoreria.jobs.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Statement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import cl.bcs.tesoreria.jobs.support.Caso;

/**
 * {@link CabecerasCargaJdbc} runs the SQL of the legacy {@code getCabeceras} (the Servicios file) against the case schema: a header
 * counts when its file name is {@code CDB_IngresoEgreso_<tipo>_<yyyyMMdd>.csv}, its code type {@code CARGA_MASIVA_INGRESO_EGRESO_<tipo>}
 * and its state the requested one.
 */
class CabecerasCargaJdbcTest {

    private CabecerasCargaJdbc cabeceras;

    @BeforeEach
    void preparar() throws Exception {
        Caso caso = Caso.cargar("CM-01");
        try (Statement st = caso.bd().setup().createStatement()) {
            st.execute("INSERT INTO \"BSCL.Plataforma.BD::FTP.Cabecera\" VALUES (1, 'CARGA_MASIVA_INGRESO_EGRESO_DAP', 'PEN', TIMESTAMP '2025-04-01 08:00:00', 21, 'CDB_IngresoEgreso_DAP_20250401.csv')");
            st.execute("INSERT INTO \"BSCL.Plataforma.BD::FTP.Cabecera\" VALUES (2, 'CARGA_MASIVA_INGRESO_EGRESO_DAP', 'PRO', TIMESTAMP '2025-03-31 08:00:00', 21, 'CDB_IngresoEgreso_DAP_20250331.csv')");
            st.execute("INSERT INTO \"BSCL.Plataforma.BD::FTP.Cabecera\" VALUES (3, 'CARGA_MASIVA_INGRESO_EGRESO_FFMM', 'PEN', TIMESTAMP '2025-04-01 08:00:00', 21, 'CDB_IngresoEgreso_FFMM_20250401.csv')");
        }
        cabeceras = new CabecerasCargaJdbc(new NamedParameterJdbcTemplate(caso.bd().jdbc()));
    }

    @Test
    void cuentaLaCabeceraDelTipoLaFechaYElEstado() throws Exception {
        assertThat(cabeceras.cantidad("DAP", "2025-04-01", "PEN")).isEqualTo(1);
        assertThat(cabeceras.cantidad("FFMM", "2025-04-01", "PEN")).isEqualTo(1);
        assertThat(cabeceras.cantidad("DAP", "2025-03-31", "PRO")).isEqualTo(1);
    }

    @Test
    void sinCoincidenciaNoHayCabeceras() throws Exception {
        assertThat(cabeceras.cantidad("DAP", "2025-04-01", "PRO")).isZero();
        assertThat(cabeceras.cantidad("DAP", "2025-04-02", "PEN")).isZero();
        assertThat(cabeceras.cantidad("OTRO", "2025-04-01", "PEN")).isZero();
    }
}
