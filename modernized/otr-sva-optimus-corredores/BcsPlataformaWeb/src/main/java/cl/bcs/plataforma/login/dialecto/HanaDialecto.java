package cl.bcs.plataforma.login.dialecto;

/** SAP HANA: the text the login has always sent. */
public class HanaDialecto implements SqlDialecto {

    public HanaDialecto() {
    }

    @Override
    public String nombre() {
        return "hana";
    }

    @Override
    public String siguienteValor(String secuenciaHana) {
        return "\"" + secuenciaHana + "\".NEXTVAL";
    }

    @Override
    public String seleccionarSinTabla(String expresion) {
        return "SELECT " + expresion + " FROM DUMMY";
    }
}
