package cl.bcs.normativa.svc.service.circulares;

import cl.bcs.normativa.svc.service.GenerarNCG380Service;
import cl.bcs.normativa.svc.service.fatca.GenerarDatosFatcaService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CircularesService {

    private static final Gson GSON = new Gson();

    private final GenerarArchivoCircular1720Service circular1720Svc;
    private final GenerarNCG380CustodiaService ncg380CustodiaSvc;
    private final GenerarNCG380Service ncg380Svc;
    private final NotificarCIRCULAR1985Service circular1985Svc;
    private final GenerarAnexo1Circular20Service anexo1Circular20Svc;
    private final NotificarNCG365FFMMService ncg365FfmmSvc;
    private final ConsolidarClientesService consolidarClientesSvc;
    private final GenerarInformeMensualInversionistasRV13419Service inversionistasRV13419Svc;
    private final GenerarDatosFatcaService datosFatcaSvc;

    public CircularesService(
            GenerarArchivoCircular1720Service circular1720Svc,
            GenerarNCG380CustodiaService ncg380CustodiaSvc,
            GenerarNCG380Service ncg380Svc,
            NotificarCIRCULAR1985Service circular1985Svc,
            GenerarAnexo1Circular20Service anexo1Circular20Svc,
            NotificarNCG365FFMMService ncg365FfmmSvc,
            ConsolidarClientesService consolidarClientesSvc,
            GenerarInformeMensualInversionistasRV13419Service inversionistasRV13419Svc,
            GenerarDatosFatcaService datosFatcaSvc) {
        this.circular1720Svc          = circular1720Svc;
        this.ncg380CustodiaSvc        = ncg380CustodiaSvc;
        this.ncg380Svc                = ncg380Svc;
        this.circular1985Svc          = circular1985Svc;
        this.anexo1Circular20Svc      = anexo1Circular20Svc;
        this.ncg365FfmmSvc            = ncg365FfmmSvc;
        this.consolidarClientesSvc    = consolidarClientesSvc;
        this.inversionistasRV13419Svc = inversionistasRV13419Svc;
        this.datosFatcaSvc            = datosFatcaSvc;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> exec(JsonObject result) {
        return GSON.fromJson(result, Map.class);
    }

    private JsonObject toJson(Map<String, Object> p) {
        return GSON.toJsonTree(p).getAsJsonObject();
    }

    public Map<String, Object> generarArchivoCircular1720(Map<String, Object> parametros) {
        return exec(circular1720Svc.execute(toJson(parametros)));
    }

    public Map<String, Object> generarNCG380Custodia(Map<String, Object> parametros) {
        return exec(ncg380CustodiaSvc.execute(toJson(parametros)));
    }

    public Map<String, Object> generarNCG380(Map<String, Object> parametros) {
        return exec(ncg380Svc.execute(toJson(parametros)));
    }

    public Map<String, Object> notificarCircular1985(Map<String, Object> parametros) {
        return exec(circular1985Svc.execute(toJson(parametros)));
    }

    public Map<String, Object> generarAnexo1Circular20(Map<String, Object> parametros) {
        return exec(anexo1Circular20Svc.execute(toJson(parametros)));
    }

    public Map<String, Object> notificarNCG365FFMM(Map<String, Object> parametros) {
        return exec(ncg365FfmmSvc.execute(toJson(parametros)));
    }

    public Map<String, Object> consolidarClientes(Map<String, Object> parametros) {
        return exec(consolidarClientesSvc.execute(toJson(parametros)));
    }

    public Map<String, Object> generarInformeMensualInversionistasRV13419(Map<String, Object> parametros) {
        return exec(inversionistasRV13419Svc.execute(toJson(parametros)));
    }

    public Map<String, Object> generarDatosFatca(Map<String, Object> parametros) {
        return exec(datosFatcaSvc.execute(toJson(parametros)));
    }
}
