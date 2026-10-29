package cl.bcs.fondomutuo.adapter.out.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cl.bcs.fondomutuo.TestBd;

/** FM-KIT-01..08: {@link SqlNombrado} has the semantics of legacy {@code SQL.query} / {@code NamedParameterStatement} (BcsUtils/SQL.java:162-262, :495-523). */
class SqlNombradoTest {

    public static class Fila {
        public Long IdPais;
        public String Tipo;
    }

    DataSource ds;

    @BeforeEach
    void preparar() {
        ds = TestBd.nueva();
        TestBd.ejecutar(ds,
                "INSERT INTO \"BSCL.Plataforma.BD::REG.DiaFeriado\" VALUES (1,'N','2026-01-01',1,'a',2026,56,'ACT')",
                "INSERT INTO \"BSCL.Plataforma.BD::REG.DiaFeriado\" VALUES (2,'R','2026-05-01',1,'b',2026,56,'ACT')",
                "INSERT INTO \"BSCL.Plataforma.BD::REG.DiaFeriado\" VALUES (3,'N','2026-07-04',4,'c',2026,840,'ACT')",
                "INSERT INTO \"BSCL.Plataforma.BD::REG.DiaFeriado\" VALUES (4,'N','2026-07-05',5,'d',2026,32,'ACT')");
    }

    private <T> T con(DataSource d, ConexionUnidadDeTrabajo.Trabajo<T> t) {
        return new ConexionUnidadDeTrabajo(d).usar(t);
    }

    private static final String SEL = "SELECT \"IdPais\", \"Tipo\" FROM \"BSCL.Plataforma.BD::REG.DiaFeriado\" WHERE \"IdPais\" IN (:IdPais) ORDER BY \"IdFeriado\";";

    @Test
    void fmKit01_listaExpandeElIn_yElPuntoYComaFinalSeEnvia() {
        Map<String, Object> p = new HashMap<>();
        p.put("IdPais", new java.util.ArrayList<>(List.of(56L, 840L)));
        List<Fila> filas = con(ds, c -> SqlNombrado.consultar(c, SEL, p, Fila.class));
        assertThat(filas).extracting(f -> f.IdPais).containsExactly(56L, 56L, 840L);
        assertThat(filas).extracting(f -> f.Tipo).containsExactly("N", "R", "N");
    }

    @Test
    void fmKit02_listaVaciaNoDevuelveFilas_nullNoFalla() {
        Map<String, Object> p = new HashMap<>();
        p.put("IdPais", new java.util.ArrayList<Long>());
        List<Fila> filas = con(ds, c -> SqlNombrado.consultar(c, SEL, p, Fila.class));
        assertThat(filas).isEmpty();
    }

    @Test
    void fmKit03_parametroNombradoRepetidoYComentariosConDosPuntos() {
        String sql = "/* :NoEsParametro */ SELECT COUNT(*) AS \"N\" FROM \"BSCL.Plataforma.BD::REG.DiaFeriado\" WHERE \"IdPais\" = :P OR \"IdPais\" = :P -- :Otro\n";
        Map<String, Object> p = new HashMap<>();
        p.put("P", 56L);
        assertThat(con(ds, c -> SqlNombrado.filas(c, sql, p)).get(0).get("N")).isEqualTo(2L);
    }

    @Test
    void fmKit04_losNombresDeObjetoHanaConDosPuntosDobles_noSonParametros() {
        // "SCHEMA::TABLE" is inside double quotes: it is never read as :TABLE
        Map<String, Object> p = new HashMap<>();
        p.put("X", 1L);
        List<Map<String, Object>> filas = con(ds, c -> SqlNombrado.filas(c, "SELECT \"IdEmpresa\" FROM \"BSCL.Plataforma.BD::PLA.Empresa\" WHERE \"IdEmpresa\" = :X", p));
        assertThat(filas).isEmpty();
    }

    @Test
    void fmKit05_parametroDelMapaQueNoEstaEnElSql_falla_comoElLegacy() {
        Map<String, Object> p = new HashMap<>();
        p.put("NoExiste", 1L);
        assertThatThrownBy(() -> con(ds, c -> SqlNombrado.filas(c, "SELECT 1 AS \"A\" FROM DUMMY", p)))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("Parameter not found: NoExiste");
    }

    @Test
    void fmKit06_valorNull_seEnviaComoNullVarchar() {
        Map<String, Object> p = new HashMap<>();
        p.put("T", null);
        String sql = "SELECT COUNT(*) AS \"N\" FROM \"BSCL.Plataforma.BD::REG.DiaFeriado\" WHERE \"Tipo\" = :T";
        assertThat(con(ds, c -> SqlNombrado.filas(c, sql, p)).get(0).get("N")).isEqualTo(0L);
    }

    @Test
    void fmKit07_elMapeoEsPorCampoPublicoExacto_columnasSinCampoSeIgnoran() {
        Map<String, Object> p = new HashMap<>();
        List<Fila> filas = con(ds, c -> SqlNombrado.consultar(c,
                "SELECT \"IdPais\", \"Tipo\", \"DescripcionDia\" AS \"Otro\" FROM \"BSCL.Plataforma.BD::REG.DiaFeriado\" WHERE \"IdFeriado\" = 3", p, Fila.class));
        assertThat(filas).hasSize(1);
        assertThat(filas.get(0).IdPais).isEqualTo(840L);
    }

    @Test
    void fmKit08_actualizarDevuelveFilasAfectadas_yUsaLaConexionDeLaTransaccion() throws Exception {
        Map<String, Object> p = new HashMap<>();
        p.put("E", "XXX");
        int n = con(ds, c -> SqlNombrado.actualizar(c, "UPDATE \"BSCL.Plataforma.BD::REG.DiaFeriado\" SET \"IdEstado\" = :E WHERE \"IdPais\" = 56", p));
        assertThat(n).isEqualTo(2);
        try (Connection c = ds.getConnection()) {
            var rs = c.createStatement().executeQuery("SELECT COUNT(*) FROM \"BSCL.Plataforma.BD::REG.DiaFeriado\" WHERE \"IdEstado\" = 'XXX'");
            rs.next();
            assertThat(rs.getLong(1)).isEqualTo(2L);
        }
    }
}
