package cl.bcs.fondomutuo.certificacion;

/**
 * The KNOWN stop of the T1 seam: the test double of {@link CertificarAporteRescateFasesPosteriores} throws it right after RECORDING the context it received. It is the new code's
 * counterpart of the {@code "PARADA_T1"} error that the oracle's `parada` variant scripts into the first collaborator F-C / F-D reach (the golden then ends with
 * {@code Exception: PARADA_T1}: {@link CasoCer} writes this one as that very text). The facade must let it go up untouched (the legacy has no try/catch in {@code logica}).
 */
public final class ParadaT1 extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ParadaT1() {
        super("PARADA_T1");
    }
}
