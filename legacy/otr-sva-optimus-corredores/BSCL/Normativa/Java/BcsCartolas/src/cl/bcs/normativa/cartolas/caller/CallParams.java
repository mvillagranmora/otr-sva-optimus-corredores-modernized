package cl.bcs.normativa.cartolas.caller;

import java.util.List;

import cl.bcs.normativa.cartolas.entidades.Cartola;
import cl.bcs.normativa.cartolas.entidades.Nomina;

public class CallParams {

    private final List<Cartola> cartolas;
    private final int indiceCartolaDesde;
    private final int indiceCartolaHasta;
    private final Nomina nomina;

    public CallParams(
            final Nomina nomina,
            final List<Cartola> cartolas,
            final int indiceCartolaDesde,
            final int indiceCartolaHasta) {
        this.nomina = nomina;
        this.cartolas = cartolas;
        this.indiceCartolaDesde = indiceCartolaDesde;
        this.indiceCartolaHasta = indiceCartolaHasta;
    }

    public List<Cartola> getCartolas() {
        return cartolas;
    }

    public int getIndiceCartolaDesde() {
        return indiceCartolaDesde;
    }

    public int getIndiceCartolaHasta() {
        return indiceCartolaHasta;
    }

    public Nomina getNomina() {
        return nomina;
    }

}
