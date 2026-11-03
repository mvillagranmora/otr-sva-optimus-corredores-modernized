package cl.bcs.plataforma.login.dialecto;

/**
 * The parts of the login SQL that differ between SAP HANA and Postgres. Bind parameters are always {@code :name} and are never touched here
 * ({@code SqlNombrado} turns them into {@code ?}). An object token is the part of a HANA name after {@code ::} (for example {@code ACC.Usuario}); a
 * column token adds the column ({@code ACC.Usuario.IdEmpresa}).
 */
public interface SqlDialecto {

    /** {@code hana} or {@code postgres}: the value of {@code plataforma.db.dialect}. */
    String nombre();

    /** Name of an object token as it is written in a statement: quoted full HANA name, or the flat Postgres name. */
    String objeto(String ficha);

    /** Name of a column token as it is written in a condition. */
    String columna(String ficha);

    /** A column in a select list; in Postgres it carries an alias to the HANA name ({@code id_empresa AS "IdEmpresa"}) so the result labels never change. */
    String columnaSeleccion(String ficha);

    /** Name of an object given by its full HANA name (a procedure, a sequence...), not quoted. */
    String nombreDe(String objetoHana);

    /** The current UTC timestamp. */
    String ahoraUtc();

    /** The select limited to its first {@code n} rows; {@code n} below 1 is refused. */
    String primeros(String select, int n);

    /** The next value of a sequence given by its full HANA name. */
    String siguienteValor(String secuenciaHana);

    /** A select of an expression with no table. */
    String seleccionarSinTabla(String expresion);

    /** The timestamp plus a number of seconds. */
    String sumarSegundos(String timestamp, String segundos);

    /** A date or timestamp as text with the given format. */
    String fechaATexto(String expresion, String formato);

    /** The expression as an integer. */
    String aEntero(String expresion);

    /** The text without leading and trailing blanks. */
    String recortar(String expresion);

    /**
     * Refuses, with {@link ObjetoNoDisponibleException}, an object that cannot be used in this dialect. In HANA nothing is ever refused: the
     * equivalence table only describes Postgres.
     */
    void exigirDisponible(String objetoHana);

    /**
     * Refuses, with {@link UnsupportedOperationException} (503), something that only exists in HANA (a stored procedure called by its HANA name, a view
     * named by a registry row) when the active dialect is not HANA, so no HANA text ever reaches another database.
     */
    default void exigirHana(String que) {
        if (!"hana".equals(nombre())) {
            throw new UnsupportedOperationException(que + " is available only in hana mode; the active dialect is " + nombre());
        }
    }
}
