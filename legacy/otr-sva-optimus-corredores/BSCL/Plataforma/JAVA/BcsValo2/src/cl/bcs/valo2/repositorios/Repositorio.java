package cl.bcs.valo2.repositorios;

import cl.bcs.valo2.ValorizadorContexto;

public abstract class Repositorio {

    private final ValorizadorContexto contexto;

    public Repositorio(
            final ValorizadorContexto contexto) {
        this.contexto = contexto;
    }

    public abstract void cleanUpCache();

    public ValorizadorContexto getContexto() {
        return contexto;
    }

}
