package cl.bcs.plataforma.login.dialecto;

/**
 * Postgres: flat snake_case names resolved through the equivalence catalog, and Postgres spellings of the functions HANA writes differently. An object
 * that is not available is refused with {@link ObjetoNoDisponibleException} (503) and never replaced by its HANA name.
 */
public class PostgresDialecto implements SqlDialecto {

    private final CatalogoEquivalencias catalogo;

    public PostgresDialecto(CatalogoEquivalencias catalogo) {
        this.catalogo = catalogo;
    }

    @Override
    public String nombre() {
        return "postgres";
    }

    private String hanaDeFicha(String ficha) {
        try {
            return catalogo.hanaDeFicha(ficha);
        } catch (IllegalArgumentException e) {
            throw new ObjetoNoDisponibleException(ficha);
        }
    }

    @Override
    public String objeto(String ficha) {
        return catalogo.nombrePostgres(hanaDeFicha(ficha));
    }

    @Override
    public String columna(String ficha) {
        int i = ficha.lastIndexOf('.');
        if (i < 0) {
            throw new IllegalArgumentException("not a column token: " + ficha);
        }
        String tabla = hanaDeFicha(ficha.substring(0, i));
        try {
            return catalogo.columnaPostgres(tabla, ficha.substring(i + 1));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("unknown column token " + ficha, e);
        }
    }

    @Override
    public String columnaSeleccion(String ficha) {
        return columna(ficha) + " AS \"" + ficha.substring(ficha.lastIndexOf('.') + 1) + "\"";
    }

    @Override
    public String nombreDe(String objetoHana) {
        return catalogo.nombrePostgres(objetoHana);
    }

    @Override
    public String ahoraUtc() {
        return "(now() AT TIME ZONE 'UTC')";
    }

    @Override
    public String primeros(String select, int n) {
        HanaDialecto.exigirPositivo(n);
        return select.stripTrailing() + " LIMIT " + n;
    }

    @Override
    public String siguienteValor(String secuenciaHana) {
        return "nextval('" + catalogo.nombrePostgres(secuenciaHana) + "')";
    }

    @Override
    public String seleccionarSinTabla(String expresion) {
        return "SELECT " + expresion;
    }

    @Override
    public String sumarSegundos(String timestamp, String segundos) {
        return "(" + timestamp + " + (" + segundos + ") * INTERVAL '1' SECOND)";
    }

    @Override
    public String fechaATexto(String expresion, String formato) {
        return "to_char(" + expresion + ",'" + formato + "')";
    }

    @Override
    public String aEntero(String expresion) {
        return "CAST(" + expresion + " AS integer)";
    }

    @Override
    public String recortar(String expresion) {
        return "rtrim(ltrim(" + expresion + "))";
    }

    @Override
    public void exigirDisponible(String objetoHana) {
        catalogo.nombrePostgres(objetoHana);
    }
}
