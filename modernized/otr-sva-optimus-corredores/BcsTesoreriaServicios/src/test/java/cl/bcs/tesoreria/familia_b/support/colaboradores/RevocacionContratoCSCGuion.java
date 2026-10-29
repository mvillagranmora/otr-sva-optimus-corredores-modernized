package cl.bcs.tesoreria.familia_b.support.colaboradores;

/** Port of the oracle fake of RevocacionContratoCSC (script key {@code revocacionContratoCSC}). */
public class RevocacionContratoCSCGuion extends ContratoScriptGuion {

    public RevocacionContratoCSCGuion() {
        super(false);
    }

    @Override
    public String clave() {
        return "revocacionContratoCSC";
    }
}
