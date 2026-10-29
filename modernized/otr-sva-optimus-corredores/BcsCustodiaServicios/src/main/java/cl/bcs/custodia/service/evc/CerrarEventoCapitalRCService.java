package cl.bcs.custodia.service.evc;

import cl.bcs.custodia.service.JavaCallService;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Migrated stub for legacy cl.bcs.custodia.evc.CerrarEventoCapitalRC (JavaCall).
 * DEFERRED: cross-service dependencies: cl.bcs.cuentaInversion.CrearCargoAbono, cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoEntrada, cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoSalida, cl.bcs.cuentaInversion.CrearCargoAbono.ObjCargoAbonoAGenerar, cl.bcs.ordenes.pojo.IndicadoresPortafolio, cl.bcs.plataforma.pla.ConsultarFlujoCaja, cl.bcs.plataforma.pla.ConsultarFlujoCaja.EntradaConsultarFlujoCaja, cl.bcs.plataforma.pla.ConsultarFlujoCaja.PortafolioAConsultar, cl.bcs.plataforma.pla.ConsultarFlujoCaja.SalidaConsultarFlujoCaja, cl.bcs.plataforma.pla.SaldoFlujoCaja.ObjFLUJOCAJA, cl.bcs.tesoreria.CrearIngresoEgreso, cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Entrada, cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Salida, cl.bcs.tesoreria.CrearIngresoEgreso.FormaDePago_Entrada, cl.bcs.tesoreria.CrearIngresoEgreso.IngresoEgreso_Entrada.
 * TODO: implement after cross-service HTTP clients are available.
 */
@Service
public class CerrarEventoCapitalRCService implements JavaCallService {
    private static final Logger log = LoggerFactory.getLogger(CerrarEventoCapitalRCService.class);

    @Override
    public String getFqn() {
        return "cl.bcs.custodia.evc.CerrarEventoCapitalRC";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        log.warn("CerrarEventoCapitalRCService.execute called but class is deferred - returning 503");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
            "cl.bcs.custodia.evc.CerrarEventoCapitalRC is deferred: cl.bcs.cuentaInversion.CrearCargoAbono, cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoEntrada, cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoSalida, cl.bcs.cuentaInversion.CrearCargoAbono.ObjCargoAbonoAGenerar, cl.bcs.ordenes.pojo.IndicadoresPortafolio, cl.bcs.plataforma.pla.ConsultarFlujoCaja, cl.bcs.plataforma.pla.ConsultarFlujoCaja.EntradaConsultarFlujoCaja, cl.bcs.plataforma.pla.ConsultarFlujoCaja.PortafolioAConsultar, cl.bcs.plataforma.pla.ConsultarFlujoCaja.SalidaConsultarFlujoCaja, cl.bcs.plataforma.pla.SaldoFlujoCaja.ObjFLUJOCAJA, cl.bcs.tesoreria.CrearIngresoEgreso, cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Entrada, cl.bcs.tesoreria.CrearIngresoEgreso.CrearIngresoEgreso_Salida, cl.bcs.tesoreria.CrearIngresoEgreso.FormaDePago_Entrada, cl.bcs.tesoreria.CrearIngresoEgreso.IngresoEgreso_Entrada");
    }
}
