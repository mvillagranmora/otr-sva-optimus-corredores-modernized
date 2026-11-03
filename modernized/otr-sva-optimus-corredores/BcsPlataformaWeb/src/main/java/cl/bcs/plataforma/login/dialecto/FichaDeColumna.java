package cl.bcs.plataforma.login.dialecto;

/** A column token such as {@code ACC.Usuario.IdEmpresa}, split into the full HANA name of its table and the HANA column name. */
record FichaDeColumna(String tablaHana, String columna) {

    static FichaDeColumna de(String ficha, CatalogoEquivalencias catalogo) {
        int i = ficha.lastIndexOf('.');
        if (i < 0) {
            throw new IllegalArgumentException("not a column token: " + ficha);
        }
        return new FichaDeColumna(catalogo.hanaDeFicha(ficha.substring(0, i)), ficha.substring(i + 1));
    }
}
