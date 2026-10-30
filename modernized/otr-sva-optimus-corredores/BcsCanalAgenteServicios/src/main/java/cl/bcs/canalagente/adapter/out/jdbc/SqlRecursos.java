package cl.bcs.canalagente.adapter.out.jdbc;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/** Loads the verbatim legacy SQL files from the classpath, under this module's own package. UTF-8 explicit; a missing resource FAILS (never an empty string). */
public final class SqlRecursos {

    public static final String SQL_MODULO = "cl/bcs/canalagente/sql/";
    public static final String BD_SERVICIOS = "cl/bcs/canalagente/bd/servicios/";

    private SqlRecursos() {
    }

    public static String modulo(String archivo) {
        return cargar(SQL_MODULO + archivo);
    }

    public static String cargar(String ruta) {
        try (InputStream in = SqlRecursos.class.getClassLoader().getResourceAsStream(ruta)) {
            if (in == null) {
                throw new IllegalStateException("SQL resource not found on the classpath: " + ruta);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("cannot read SQL resource " + ruta, e);
        }
    }
}
