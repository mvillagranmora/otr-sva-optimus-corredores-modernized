package cl.bcs.custodia.flujo.jdbc;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * Loads the verbatim legacy SQL files from the classpath: the module SQL under {@code cl/bcs/custodia/sql/} and the Plataforma reads of the slice under
 * {@code cl/bcs/custodia/flujo/kit/sql/}. UTF-8; a missing resource FAILS (never an empty string).
 */
public final class SqlRecursos {

    private static final String SQL_MODULO = "cl/bcs/custodia/sql/";
    private static final String SQL_KIT = "cl/bcs/custodia/flujo/kit/sql/";

    private SqlRecursos() {
    }

    public static String modulo(String archivo) {
        return cargar(SQL_MODULO + archivo);
    }

    public static String kit(String archivo) {
        return cargar(SQL_KIT + archivo);
    }

    private static String cargar(String ruta) {
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
