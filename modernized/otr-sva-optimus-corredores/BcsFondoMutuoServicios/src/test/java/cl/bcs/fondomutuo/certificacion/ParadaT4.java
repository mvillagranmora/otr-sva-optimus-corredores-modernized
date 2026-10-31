package cl.bcs.fondomutuo.certificacion;

/**
 * The KNOWN stop of the T4 seam: the test double of {@link CertificarAporteRescateFasesPosteriores} runs the steps of F-C..F-I (see {@link ApiT4}) over the context it receives and then throws
 * this, so that the run ends right after FM-CER-64 (the UPDATE to CRT, the notifications, the log and the final output are front J, T5). It only marks "F-C..F-I finished without an error";
 * any other exception is what F-C..F-I themselves raised and must come out of the facade untouched (the legacy {@code logica} has no try/catch).
 */
public final class ParadaT4 extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ParadaT4() {
        super("PARADA_T4");
    }
}
