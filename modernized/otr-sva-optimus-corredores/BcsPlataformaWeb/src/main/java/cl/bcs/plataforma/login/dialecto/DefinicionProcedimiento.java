package cl.bcs.plataforma.login.dialecto;

import java.util.List;
import java.util.Map;

/**
 * What the module knows about one stored procedure (or table function) of the login in Postgres: its Postgres name, its parameters in the order of the
 * signature, the labels HANA gave to the columns a function returns, and the attributes of the composite types its array parameters receive. It is written
 * in {@link DefinicionesProcedimientos}.
 */
record DefinicionProcedimiento(String nombreHana, String nombrePostgres, boolean esFuncion, List<Parametro> parametros, Map<String, String> etiquetaHanaPorColumna,
        Map<String, List<Atributo>> atributosPorParametro, String claveDeLaTabla) {

    /** A parameter: {@code modo} is IN, OUT or INOUT; {@code nombreHana} is the key of the input or the legacy OUT name of the answer. */
    record Parametro(int posicion, String modo, String nombreHana, String nombrePostgres, String tipoPostgres) {

        boolean esEntrada() {
            return "IN".equals(modo);
        }

        boolean esArreglo() {
            return tipoPostgres.endsWith("[]");
        }

        boolean esCursor() {
            return "refcursor".equals(tipoPostgres);
        }

        /** The composite type of an array parameter, without the brackets and unqualified. */
        String tipoDelElemento() {
            return tipoPostgres.substring(0, tipoPostgres.length() - 2);
        }
    }

    /** An attribute of a composite type; {@code posicion} is its place in the type. */
    record Atributo(int posicion, String nombre, String tipoPostgres, String nombreHana) {
    }
}
