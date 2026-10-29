package cl.bcs.tesoreria.familia_b.support.colaboradores;

/** Port of the oracle fake of AnulacionContratoCSC (script key {@code anulacionContratoCSC}). */
public class AnulacionContratoCSCGuion extends ContratoScriptGuion {

    public AnulacionContratoCSCGuion() {
        super(false);
    }

    @Override
    public String clave() {
        return "anulacionContratoCSC";
    }
}
