package cl.bcs.plataforma.internal;

/**
 * Owners that expose their own {@code POST /internal/java-call} with {@code {"class","params"}}: BcsContabilidadServicios,
 * BcsTesoreriaServicios and BcsCuentaInversionServicios (each answers 503 for a class it has not migrated). Body and answer
 * go through unchanged.
 */
public class JavaCallDirectaEstrategia implements EstrategiaDueno {

    @Override
    public Respuesta invocar(ClienteInterno http, String urlBase, SolicitudJavaCall s) {
        return http.post(urlBase + "/internal/java-call", s.cuerpoParaDueno(), true, null);
    }
}
