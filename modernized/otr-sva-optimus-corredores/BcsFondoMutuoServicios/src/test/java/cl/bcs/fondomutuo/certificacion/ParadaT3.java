package cl.bcs.fondomutuo.certificacion;

/**
 * The KNOWN stop of the T3 seam: the test double of {@link CertificarAporteRescateFasesPosteriores} runs the steps of F-C..F-H (see {@link ApiT3}) over the context it receives and then
 * throws this, so that the run ends right after Spot (Facturacion, reinversion, switch, UPDATE, notifications and logs are fronts G, I, J, T4 / T5).
 * It is the counterpart, for T3, of {@link ParadaT1}: it only marks "F-C..F-H finished without an error"; any other exception is what F-C..F-H themselves raised and must
 * come out of the facade untouched (the legacy {@code logica} has no try/catch).
 */
public final class ParadaT3 extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ParadaT3() {
        super("PARADA_T3");
    }
}
