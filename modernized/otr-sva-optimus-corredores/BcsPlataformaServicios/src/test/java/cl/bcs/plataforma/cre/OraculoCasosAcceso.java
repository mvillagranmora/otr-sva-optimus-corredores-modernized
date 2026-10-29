package cl.bcs.plataforma.cre;

import java.nio.file.Path;
import java.sql.Connection;

/** Public door of {@link OraculoCasos} for tests of other packages: the PAU.Modulo schema of the oracle (single source of the DDL). */
public final class OraculoCasosAcceso {

    private OraculoCasosAcceso() { }

    public static Path esquema() {
        return OraculoCasos.base().resolve("schema-plataforma-esm.sql");
    }

    public static void script(Connection con) throws Exception {
        OraculoCasos.script(con, esquema());
    }
}
