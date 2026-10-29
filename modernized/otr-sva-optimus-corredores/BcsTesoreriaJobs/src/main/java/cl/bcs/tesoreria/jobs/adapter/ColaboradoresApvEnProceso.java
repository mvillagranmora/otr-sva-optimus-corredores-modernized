package cl.bcs.tesoreria.jobs.adapter;

import cl.bcs.apv.domain.port.out.Colaboradores;
import cl.bcs.apv.infrastructure.adapter.ColaboradoresJavaCall;
import com.google.gson.JsonObject;

import java.util.Objects;

/**
 * The {@link Colaboradores} port of the APV use cases that this job application runs in process (step 5). It is where the Tesoreria &lt;-&gt; APV
 * cycle is resolved: APV calls Tesoreria ({@code CrearIngresoEgreso}, {@code LiquidarComprobante}, {@code CrearComprobante},
 * {@code AnulacionMandatoPAC}, {@code RevocacionMandatoPAC}, {@code IngresarMandatoPAC}) and CuentaInversion ({@code CrearCargoAbono},
 * {@code ConsultarSaldoCuentaInversion}); Tesoreria calls APV ({@code CambiarEstadoContratosSAAPV}, {@code ValidarMandatosPAC}). apv-service does not
 * depend on tesoreria-service or the reverse: this class, which lives in the job application, hands the call to {@link DespachadorEnProceso}.
 *
 * <p>The name is resolved to the legacy class with the registry of the library adapter ({@link ColaboradoresJavaCall#clase}), so an
 * unregistered name still fails instead of travelling bare. A class served in process runs inside the transaction of the job; any other
 * (Plataforma, Orden, FondoMutuo, ...) and {@code StoredProcedure} go through the APV library adapter exactly as in apv-service
 * (platform router with bounded waits; the stored procedure runs on the same connection).
 */
public class ColaboradoresApvEnProceso implements Colaboradores {

    private final DespachadorEnProceso despachador;
    private final Colaboradores http;

    /** @param http the production adapter of apv-service ({@link ColaboradoresJavaCall}), used for everything the owners do not serve */
    public ColaboradoresApvEnProceso(DespachadorEnProceso despachador, Colaboradores http) {
        this.despachador = Objects.requireNonNull(despachador, "despachador");
        this.http = Objects.requireNonNull(http, "http");
    }

    @Override
    public JsonObject invocar(String servicio, JsonObject entrada) {
        if (!"StoredProcedure".equals(servicio)) {
            String clase = ColaboradoresJavaCall.clase(servicio);
            if (despachador.maneja(clase)) {
                return despachador.despachar(clase, entrada);
            }
        }
        return http.invocar(servicio, entrada);
    }
}
