package cl.bcs.plataforma.login.dialecto;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * HANA to Postgres equivalences, loaded from two CSV resources generated from the table the database team delivered
 * ({@code resumen_exportacion.xlsx}): {@code /equivalencias/equivalencias-objetos.csv} (type, HANA name, Postgres name) and
 * {@code /equivalencias/equivalencias-columnas.csv} (table and column, both names). An object whose {@code nombre_postgres} is the literal
 * {@code NO_DISPONIBLE}, is empty or is absent is not available: asking for its Postgres name raises {@link ObjetoNoDisponibleException}. Names are
 * matched exactly (case, spaces, quotes). Immutable once built.
 */
public final class CatalogoEquivalencias {

    static final String NO_DISPONIBLE = "NO_DISPONIBLE";
    static final String RECURSO_OBJETOS = "/equivalencias/equivalencias-objetos.csv";
    static final String RECURSO_COLUMNAS = "/equivalencias/equivalencias-columnas.csv";

    private record Objeto(String tipo, String hana, String postgres) {
        boolean disponible() {
            return !postgres.isEmpty() && !postgres.equals(NO_DISPONIBLE);
        }
    }

    private final Map<String, Objeto> objetos = new LinkedHashMap<>();
    private final Map<String, Map<String, String>> columnas = new LinkedHashMap<>();
    private final Map<String, List<String>> porFicha = new LinkedHashMap<>();

    /** The catalog the application uses, from the resources of this module. */
    public static CatalogoEquivalencias desdeRecursos() {
        return desdeCsv(recurso(RECURSO_OBJETOS), recurso(RECURSO_COLUMNAS));
    }

    /** A catalog from two CSV texts; the readers are closed. */
    public static CatalogoEquivalencias desdeCsv(Reader objetos, Reader columnas) {
        return new CatalogoEquivalencias(LectorCsv.filas(objetos, List.of("tipo_objeto", "nombre_hana", "nombre_postgres")),
                LectorCsv.filas(columnas, List.of("nombre_tabla_hana", "nombre_tabla_postgresql", "nombre_columna_hana", "nombre_columna_postgresql")));
    }

    private static Reader recurso(String ruta) {
        InputStream in = CatalogoEquivalencias.class.getResourceAsStream(ruta);
        if (in == null) {
            throw new IllegalStateException("missing resource " + ruta);
        }
        return new InputStreamReader(in, StandardCharsets.UTF_8);
    }

    private CatalogoEquivalencias(List<Map<String, String>> filasObjetos, List<Map<String, String>> filasColumnas) {
        Map<String, String> hanaDePostgres = new LinkedHashMap<>();
        for (Map<String, String> fila : filasObjetos) {
            Objeto objeto = new Objeto(fila.get("tipo_objeto"), fila.get("nombre_hana"), fila.get("nombre_postgres"));
            Objeto previo = objetos.get(objeto.hana());
            if (previo != null) {
                if (!previo.equals(objeto)) {
                    throw new IllegalStateException("Duplicate key with different values in the objects table: " + objeto.hana());
                }
                continue;
            }
            if (objeto.disponible()) {
                String otro = hanaDePostgres.put(objeto.postgres(), objeto.hana());
                if (otro != null) {
                    throw new IllegalStateException("Duplicate Postgres name " + objeto.postgres() + " for " + otro + " and " + objeto.hana());
                }
            }
            objetos.put(objeto.hana(), objeto);
            int i = objeto.hana().indexOf("::");
            if (i >= 0) {
                porFicha.computeIfAbsent(objeto.hana().substring(i + 2), k -> new ArrayList<>()).add(objeto.hana());
            }
        }
        for (Map<String, String> fila : filasColumnas) {
            Map<String, String> deLaTabla = columnas.computeIfAbsent(fila.get("nombre_tabla_hana"), k -> new LinkedHashMap<>());
            String hana = fila.get("nombre_columna_hana");
            String postgres = fila.get("nombre_columna_postgresql");
            String previo = deLaTabla.put(hana, postgres);
            if (previo != null && !previo.equals(postgres)) {
                throw new IllegalStateException("Duplicate column key with different values: " + fila.get("nombre_tabla_hana") + "." + hana);
            }
        }
    }

    /** True when the object has a usable Postgres name. */
    public boolean disponible(String hana) {
        Objeto o = objetos.get(hana);
        return o != null && o.disponible();
    }

    /** The Postgres name of a HANA object; {@link ObjetoNoDisponibleException} when it is unavailable or absent. */
    public String nombrePostgres(String hana) {
        if (!disponible(hana)) {
            throw new ObjetoNoDisponibleException(hana);
        }
        return objetos.get(hana).postgres();
    }

    /** The type of the object in the table ({@code tabla}, {@code vista}, {@code procedimiento}...); {@link ObjetoNoDisponibleException} when absent. */
    public String tipo(String hana) {
        Objeto o = objetos.get(hana);
        if (o == null) {
            throw new ObjetoNoDisponibleException(hana);
        }
        return o.tipo();
    }

    /**
     * The Postgres column of a column of a HANA table. A table that is not available raises {@link ObjetoNoDisponibleException}; a table or column the
     * catalog does not know raises {@link IllegalArgumentException} naming both.
     */
    public String columnaPostgres(String tablaHana, String columnaHana) {
        nombrePostgres(tablaHana);
        Map<String, String> deLaTabla = columnas.get(tablaHana);
        if (deLaTabla == null) {
            throw new IllegalArgumentException("no columns in the catalog for " + tablaHana + " (looking for " + columnaHana + ")");
        }
        String postgres = deLaTabla.get(columnaHana);
        if (postgres == null) {
            throw new IllegalArgumentException("unknown column " + columnaHana + " of " + tablaHana);
        }
        return postgres;
    }

    /** True when the catalog lists the column for the table, whatever the availability of the table in Postgres. */
    public boolean tieneColumna(String tablaHana, String columnaHana) {
        Map<String, String> deLaTabla = columnas.get(tablaHana);
        return deLaTabla != null && deLaTabla.containsKey(columnaHana);
    }

    /** The full HANA name of an object token ({@code ACC.Usuario}); it must match exactly one object. */
    public String hanaDeFicha(String ficha) {
        List<String> candidatos = porFicha.get(ficha);
        if (candidatos == null) {
            throw new IllegalArgumentException("unknown object token " + ficha);
        }
        if (candidatos.size() > 1) {
            throw new IllegalArgumentException("ambiguous object token " + ficha + ": " + candidatos);
        }
        return candidatos.get(0);
    }

    /** Every HANA object name in the table. */
    public Set<String> objetosHana() {
        return Set.copyOf(objetos.keySet());
    }
}
