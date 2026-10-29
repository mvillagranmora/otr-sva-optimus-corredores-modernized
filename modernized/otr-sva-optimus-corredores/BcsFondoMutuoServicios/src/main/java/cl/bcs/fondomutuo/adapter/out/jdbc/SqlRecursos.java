package cl.bcs.fondomutuo.adapter.out.jdbc;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * Loads the verbatim legacy SQL files from the classpath (C3): every path is under this module's own package
 * ({@code cl/bcs/fondomutuo/sql/} for the module SQL, {@code cl/bcs/fondomutuo/kit/sql/} for the Plataforma kit), so a same-named file of another jar
 * (Tesoreria keeps {@code sql/CrearIngresoEgreso_*.sql}) can never win silently. UTF-8 explicit; a missing resource FAILS (never an empty string).
 */
public final class SqlRecursos {

    public static final String SQL_MODULO = "cl/bcs/fondomutuo/sql/";
    public static final String SQL_KIT = "cl/bcs/fondomutuo/kit/sql/";

    private SqlRecursos() {
    }

    public static String modulo(String archivo) {
        return cargar(SQL_MODULO + archivo);
    }

    public static String kit(String archivo) {
        return cargar(SQL_KIT + archivo);
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
