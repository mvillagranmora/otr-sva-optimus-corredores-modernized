package cl.bcs.apv.api.logica;

import java.util.ArrayList;
import java.util.List;

/**
 * Typed arguments of the legacy {@code ValidarMandatosPAC.logica(Connection con, SalidaRecaudacionTraspaso entrada, Long IdEmpresa, Long IdUsuario)}.
 * HAND WRITTEN (not generated). Only the two lists of {@code SalidaRecaudacionTraspaso} the method reads are modelled; its counter, messages, result and
 * {@code P_OUT_SOLICITUDES_PROCESADAS} lists are never used by the legacy.
 *
 * <p>Like the legacy object, a list may be null (the legacy then throws NullPointerException) and an element may be null; when the list is absent
 * from the JSON the legacy constructor default applies (empty list). The legacy deals with {@code idEmpresa} only as the key of the
 * {@code PLATAFORMA/PLA.FechaSistema} lookup and with {@code idUsuario} only as the text of {@code LogMandatoPAC.Usuario} (null with something to write: NPE).
 *
 * @param idEmpresa              legacy {@code IdEmpresa}
 * @param idUsuario              legacy {@code IdUsuario}
 * @param solicitudesAProcesar   legacy {@code P_OUT_SOLICITUDES_A_PROCESAR}
 * @param solicitudesNoProcesadas legacy {@code P_OUT_SOLICITUDES_NO_PROCESADAS}
 */
public record ValidarMandatosPACLogicaEntrada(
        Long idEmpresa,
        Long idUsuario,
        List<RegistroMandatoPAC> solicitudesAProcesar,
        List<RegistroMandatoPAC> solicitudesNoProcesadas) {

    public static ValidarMandatosPACLogicaEntrada porDefecto() {
        return new ValidarMandatosPACLogicaEntrada(null, null, new ArrayList<>(), new ArrayList<>());
    }
}
