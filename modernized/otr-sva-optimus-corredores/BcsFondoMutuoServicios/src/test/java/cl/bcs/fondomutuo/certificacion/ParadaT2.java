package cl.bcs.fondomutuo.certificacion;

/**
 * The KNOWN stop of the T2 seam: the test double of {@link CertificarAporteRescateFasesPosteriores} runs the steps of F-C and F-D (see {@link ApiT2}) over the context it receives and then
 * throws this, so that the run ends right after F-D (the cuenta de inversion, Tesoreria, Facturacion, Spot, reinversion, UPDATE, notifications and logs are fronts E..J, T3..T5).
 * It is the counterpart, for T2, of {@link ParadaT1}: it only marks "F-C and F-D finished without an error"; any other exception is what F-C / F-D themselves raised and must
 * come out of the facade untouched (the legacy {@code logica} has no try/catch).
 */
public final class ParadaT2 extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ParadaT2() {
        super("PARADA_T2");
    }
}
