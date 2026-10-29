package cl.bcs.tesoreria.javacall.liq;

import cl.bcs.utils.Utils;

/** Clearing chamber requested by the caller / returned by the chambers query (legacy {@code pojo.Camara}); the field names are the JavaCall contract. */
public class Camara {
    public String Camara;
    public String NombreGrupoCamara;
    public String IdEstado;
    public String IdCamaraDeCompensacion;
    public String Descripcion;
    public String CodigoGrupo;

    public Camara() {
        this.Camara = "";
        this.NombreGrupoCamara = "";
        this.IdEstado = "";
        this.IdCamaraDeCompensacion = "";
        this.Descripcion = "";
        this.CodigoGrupo = "";
    }

    public Camara(String camara) {
        this.Camara = camara;
    }

    @Override
    public String toString() {
        return Utils.printJson(this, false);
    }
}
