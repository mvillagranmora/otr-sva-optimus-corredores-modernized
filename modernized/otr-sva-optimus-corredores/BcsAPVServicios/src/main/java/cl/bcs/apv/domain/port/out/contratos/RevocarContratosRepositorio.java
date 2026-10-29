package cl.bcs.apv.domain.port.out.contratos;

import java.util.List;

/** Persistence port of {@code RevocarContratos} (legacy {@code CambiarEstadoRevocacionSAAPV}). */
public interface RevocarContratosRepositorio {

    class ResultadoValidaciones {
        public Long IdContratoSAAPV;
        public Long IdEmpleador;
    }

    /** The (at most one) VIG contract of the folio. */
    List<ResultadoValidaciones> contratoVigente(String folioSAAPV);
}
