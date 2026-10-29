package cl.bcs.plataforma.domain;

/**
 * Plataforma configuration parameter.
 * Source: CallServlet.Configuracion (inner class)
 */
public class Configuracion {

    public String CodigoParametro;
    public String Valor;

    public Configuracion() {}

    public Configuracion(String codigoParametro, String valor) {
        this.CodigoParametro = codigoParametro;
        this.Valor = valor;
    }
}
