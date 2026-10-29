package cl.bcs.tesoreria.javacall.liq;

import cl.bcs.utils.Utils;

/** Clearing house (entity that settles the chambers), as read by the settlement-house query (legacy {@code pojo.CamaraCompensacion}). */
public class CamaraCompensacion {
    public String IdCamaraDeCompensacion;
    public String Codigo;
    public String Nombre;
    public Long IdPersona;
    public Long IdPaisCamara;
    public String IdEstadoCamara;
    public String DocumentoIdentidad;
    public String RazonSocial;

    @Override
    public String toString() {
        return Utils.printJson(this, false);
    }
}
