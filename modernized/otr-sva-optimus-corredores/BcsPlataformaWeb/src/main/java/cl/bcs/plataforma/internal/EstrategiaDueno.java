package cl.bcs.plataforma.internal;

/** How one owner is called. The implementation either answers or says the class cannot be routed to that owner yet. */
public interface EstrategiaDueno {

    /** Raised when the owner has no route for the class (documented "not routable yet"): the router falls back to TomEE. */
    final class NoRuteableAun extends RuntimeException {
        public NoRuteableAun(String motivo) {
            super(motivo, null, false, false);
        }
    }

    Respuesta invocar(ClienteInterno http, String urlBase, SolicitudJavaCall solicitud);
}
