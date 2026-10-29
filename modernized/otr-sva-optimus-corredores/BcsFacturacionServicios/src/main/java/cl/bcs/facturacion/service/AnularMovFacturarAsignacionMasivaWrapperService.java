package cl.bcs.facturacion.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AnularMovFacturarAsignacionMasivaWrapperService {

    private static final Logger log = LoggerFactory.getLogger(AnularMovFacturarAsignacionMasivaWrapperService.class);

    private final AnularMovimientosPorAsignacionMasivaService delegate;

    public AnularMovFacturarAsignacionMasivaWrapperService(AnularMovimientosPorAsignacionMasivaService delegate) {
        this.delegate = delegate;
    }

    public JsonObject execute(JsonObject params) {
        log.info("INICIO::AnularMovFacturarAsignacionMasivaWrapper — delegating to AnularMovimientosPorAsignacionMasiva");
        // TODO: additional wrapper logic (e.g. AnularMovFacturarAsignacion cross-domain to BcsOrdenServicios)
        return delegate.execute(params);
    }
}
