package cl.bcs.normativa.cartolas.nombres;

import cl.bcs.normativa.cartolas.entidades.Cartola;
import cl.bcs.normativa.cartolas.procesos.ContextoCartolas;

public final class ResolvedorNombreEmpresa extends ResolvedorNombre {

    @Override
    public String resolver(
            final ContextoCartolas contexto,
            final Cartola cartola) {
        return contexto.getEmpresa().getNombre();
    }

}
