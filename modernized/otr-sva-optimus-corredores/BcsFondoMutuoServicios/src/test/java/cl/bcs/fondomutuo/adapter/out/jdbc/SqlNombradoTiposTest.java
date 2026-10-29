package cl.bcs.fondomutuo.adapter.out.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;

import cl.bcs.fondomutuo.TestBd;
import cl.bcs.fondomutuo.normal.CasoNormal;

/**
 * M4 (architecture review): the row mapping is {@code Field.set} with NO conversion, so a HANA column whose driver class differs from the pojo field fails
 * at run time. (1) the failure names the field and the classes, never a value; (2) CONTRACT: every column mapped by the SQL that the 87 Normal cases run on H2
 * is compared, by the class name the driver declares ({@code ResultSetMetaData.getColumnClassName}), with the type of the pojo field. The list of
 * (pojo.field, field type, column class, precision, scale) is written to {@code target/hana-smoke-tipos.txt}: it is the checklist of the HANA smoke test,
 * which is PENDING (H2 says nothing about what the SAP driver returns: e.g. the lock key is built from {@code String.valueOf(BigDecimal)}, so the
 * scale of Monto / NumeroCuotas matters).
 */
class SqlNombradoTiposTest {

    public static class FilaTexto {
        public String IdPais;
    }

    @Test
    void elMensajeDelDesajusteDeTipoNombraCampoYClases_sinValores() {
        DataSource ds = TestBd.nueva();
        TestBd.ejecutar(ds, "INSERT INTO \"BSCL.Plataforma.BD::REG.DiaFeriado\" VALUES (1,'N','2026-01-01',1,'a',2026,56,'ACT')");
        Map<String, Object> p = new HashMap<>();
        assertThatThrownBy(() -> new ConexionUnidadDeTrabajo(ds).usar(c -> SqlNombrado.consultar(c,
                "SELECT \"IdPais\" FROM \"BSCL.Plataforma.BD::REG.DiaFeriado\"", p, FilaTexto.class)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("FilaTexto.IdPais").hasMessageContaining("java.lang.String").hasMessageContaining("java.lang.Long")
                .satisfies(e -> assertThat(e.getMessage()).doesNotContain("56"))
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void contratoDeTipos_columnaDelDriverVsCampoDelPojo_enTodoElSqlQueCorrenLosCasosNormal() throws Exception {
        TreeSet<String> lista = new TreeSet<>();
        TreeSet<String> violaciones = new TreeSet<>();
        SqlNombrado.observador = (pojo, campo, etiqueta, claseColumna, precision, escala) -> {
            String linea = pojo.getName() + "." + campo.getName() + " | field " + campo.getType().getName() + " | column " + claseColumna + "(" + precision + "," + escala + ")";
            lista.add(linea);
            try {
                if (!campo.getType().isAssignableFrom(Class.forName(claseColumna))) violaciones.add(linea);
            } catch (ClassNotFoundException e) {
                violaciones.add(linea + " | unknown column class");
            }
        };
        try {
            for (String id : CasoNormal.casos()) {
                CasoNormal.ejecutar(id, CasoNormal.Perfil.ABIERTAS);
            }
        } finally {
            SqlNombrado.observador = null;
        }
        Path destino = Path.of("target", "hana-smoke-tipos.txt");
        Files.createDirectories(destino.getParent());
        Files.writeString(destino, "# smoke list for the HANA test (types/scales the SAP driver must return): pojo.field | java field type | H2 column class(precision,scale)\n"
                + String.join("\n", lista) + "\n", StandardCharsets.UTF_8);
        assertThat(lista).as("the cases map columns").isNotEmpty();
        assertThat(violaciones).as("column class vs pojo field").isEmpty();
    }
}
