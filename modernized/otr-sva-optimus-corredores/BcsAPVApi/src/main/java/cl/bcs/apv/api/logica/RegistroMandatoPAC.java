package cl.bcs.apv.api.logica;

/**
 * The three fields of the legacy {@code cl.bcs.tesoreria.pojo.Traspaso.RegistroRecaudacion} that {@code ValidarMandatosPAC.logica} reads (the other fields of
 * the legacy record are ignored). HAND WRITTEN (not generated): typed entry point of the in-process callers, not a JavaCall operation.
 *
 * <p>Legacy defaults when the field is absent from the JSON: {@code IdMandatoPAC = 0}, {@code IdentServicio = ""}, {@code EstadoRendicion = ""};
 * an explicit JSON null stays null.
 *
 * @param idMandatoPAC    legacy {@code IdMandatoPAC}: key of the LogMandatoPAC row
 * @param identServicio   legacy {@code IdentServicio}: copied by the legacy into its log object, never written
 * @param estadoRendicion legacy {@code EstadoRendicion}: becomes the {@code Glosa} of a request that was not processed
 */
public record RegistroMandatoPAC(Long idMandatoPAC, String identServicio, String estadoRendicion) {

    public static RegistroMandatoPAC porDefecto() {
        return new RegistroMandatoPAC(0L, "", "");
    }
}
