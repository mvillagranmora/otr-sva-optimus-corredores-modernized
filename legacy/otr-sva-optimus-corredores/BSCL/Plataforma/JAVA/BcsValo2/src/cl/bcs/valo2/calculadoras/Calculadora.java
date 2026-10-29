package cl.bcs.valo2.calculadoras;

import cl.bcs.valo2.ValorizadorBonosParams;
import cl.bcs.valo2.entidades.Bono;

interface Calculadora {

    void calcular(
            final ValorizadorBonosParams params,
            final Bono bono);

}
