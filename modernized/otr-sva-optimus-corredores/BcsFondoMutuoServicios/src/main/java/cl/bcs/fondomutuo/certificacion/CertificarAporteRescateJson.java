package cl.bcs.fondomutuo.certificacion;

import com.google.gson.Gson;

/** The Gson of legacy {@code CertificarSolicitudAporteRescate.call}: the plain {@code new Gson()}, a NEW one per call. No state. */
public final class CertificarAporteRescateJson {

    private CertificarAporteRescateJson() {
    }

    public static Gson gson() {
        return new Gson();
    }
}
