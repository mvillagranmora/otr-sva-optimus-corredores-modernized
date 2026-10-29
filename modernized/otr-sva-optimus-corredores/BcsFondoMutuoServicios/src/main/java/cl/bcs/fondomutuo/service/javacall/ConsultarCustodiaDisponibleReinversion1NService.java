package cl.bcs.fondomutuo.service.javacall;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ConsultarCustodiaDisponibleReinversion1NService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarCustodiaDisponibleReinversion1NService.class);

    private final ConsultarCustodiaPortafolioFondoMutuoService consultar;

    public ConsultarCustodiaDisponibleReinversion1NService(ConsultarCustodiaPortafolioFondoMutuoService consultar) {
        this.consultar = consultar;
    }

    public JsonObject execute(JsonObject params) {
        log.info("ConsultarCustodiaDisponibleReinversion1N");
        // Delegates to ConsultarCustodiaPortafolioFondoMutuo to obtain available custodia for reinversion
        return consultar.execute(params);
    }
}
