package cl.bcs.tesoreria.familia_b.support.colaboradores;

/** Port of the oracle fake of CambiarEstadoContratosSAAPV (script key {@code cambiarEstadoContratosSAAPV}). */
public class CambiarEstadoContratosSAAPVGuion extends ContratoScriptGuion {

    public CambiarEstadoContratosSAAPVGuion() {
        super(true);
    }

    @Override
    public String clave() {
        return "cambiarEstadoContratosSAAPV";
    }
}
