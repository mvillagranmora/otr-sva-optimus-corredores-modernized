package cl.bcs.facturacion.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AnularMovimientoComprobanteWrapperService {

    private static final Logger log = LoggerFactory.getLogger(AnularMovimientoComprobanteWrapperService.class);

    private final AnularMovimientoComprobanteService delegate;

    public AnularMovimientoComprobanteWrapperService(AnularMovimientoComprobanteService delegate) {
        this.delegate = delegate;
    }

    public JsonObject execute(JsonObject params) {
        log.info("INICIO::AnularMovimientoComprobanteWrapper — delegating to AnularMovimientoComprobante");
        return delegate.execute(params);
    }
}
