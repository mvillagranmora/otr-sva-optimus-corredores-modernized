package cl.bcs.tesoreria.javacall.distribucion;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * State of ONE run of ProcesarDistribucion / ValidaPreRendicion: the request, the business date, the UF value, the response being built and the
 * contract folios collected while the requests are assembled. The legacy kept all of this in instance fields of the service object (one object per call);
 * here the services are singletons, so a run owns its own instance.
 */
public final class Corrida {

    public final EntradaRecaudacionTraspaso entrada;
    public final String fechaSistema;
    public final BigDecimal valorUF;
    public final SalidaRecaudacionTraspaso out = new SalidaRecaudacionTraspaso();

    /** Folios collected by {@link PreRendicionPac#registroDe}: CSC folios and SAAPV folios of every request assembled (an empty file row contributes ""). */
    public final List<String> foliosCSC = new ArrayList<>();
    public final List<String> foliosAPV = new ArrayList<>();

    public Corrida(EntradaRecaudacionTraspaso entrada, String fechaSistema, BigDecimal valorUF) {
        this.entrada = entrada;
        this.fechaSistema = fechaSistema;
        this.valorUF = valorUF;
    }
}
