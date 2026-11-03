package cl.bcs.plataforma.login.dialecto;

/**
 * SAP HANA: the text the login has always sent. The catalog is used only to turn an object token into its full HANA name and to check that a column
 * token exists; it never makes HANA refuse an object (its Postgres marks do not apply here).
 */
public class HanaDialecto implements SqlDialecto {

    private final CatalogoEquivalencias catalogo;

    public HanaDialecto(CatalogoEquivalencias catalogo) {
        this.catalogo = catalogo;
    }

    @Override
    public String nombre() {
        return "hana";
    }

    @Override
    public String objeto(String ficha) {
        return "\"" + catalogo.hanaDeFicha(ficha) + "\"";
    }

    @Override
    public String columna(String ficha) {
        FichaDeColumna f;
        try {
            f = FichaDeColumna.de(ficha, catalogo);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("unknown column token " + ficha, e);
        }
        if (!catalogo.tieneColumna(f.tablaHana(), f.columna())) {
            throw new IllegalArgumentException("unknown column token " + ficha);
        }
        return "\"" + f.columna() + "\"";
    }

    @Override
    public String columnaSeleccion(String ficha) {
        return columna(ficha);
    }

    @Override
    public String nombreDe(String objetoHana) {
        return objetoHana;
    }

    @Override
    public String ahoraUtc() {
        return "CURRENT_UTCTIMESTAMP";
    }

    @Override
    public String primeros(String select, int n) {
        exigirPositivo(n);
        return select.replaceFirst("(?i)^(\\s*)SELECT\\s+", "$1SELECT TOP " + n + " ");
    }

    @Override
    public String siguienteValor(String secuenciaHana) {
        return "\"" + secuenciaHana + "\".NEXTVAL";
    }

    @Override
    public String seleccionarSinTabla(String expresion) {
        return "SELECT " + expresion + " FROM DUMMY";
    }

    @Override
    public String sumarSegundos(String timestamp, String segundos) {
        return "ADD_SECONDS (" + timestamp + ", " + segundos + ")";
    }

    @Override
    public String fechaATexto(String expresion, String formato) {
        return "TO_VARCHAR (" + expresion + ",'" + formato + "')";
    }

    @Override
    public String aEntero(String expresion) {
        return "to_int(" + expresion + ")";
    }

    @Override
    public String recortar(String expresion) {
        return "rtrim(ltrim(" + expresion + "))";
    }

    @Override
    public void exigirDisponible(String objetoHana) {
        // HANA is the source of the names: everything the login names exists there
    }

    static void exigirPositivo(int n) {
        if (n < 1) {
            throw new IllegalArgumentException("first-N needs n >= 1, got " + n);
        }
    }
}
