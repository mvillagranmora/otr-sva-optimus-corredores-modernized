package cl.bcs.tesoreria.javacall.carga;

import com.google.gson.JsonObject;

/**
 * Seam of {@code CrearCargaMasivaPacRen} to the pre-settlement validation of legacy {@code ProcesarDistribucion.ValidaPreRendicion}
 * (TES-B-309..318). The request is the {@code EntradaRecaudacionTraspaso} JSON (p_IdEmpresa, p_IdModulo, p_IdUsuario, p_simular), the answer the
 * {@code SalidaRecaudacionTraspaso} JSON. {@code ProcesarDistribucionService} implements it.
 */
public interface ValidadorPreRendicion {

    JsonObject validaPreRendicion(JsonObject entrada) throws Exception;
}
