package cl.bcs.cuentainversion.infrastructure.adapter;

import cl.bcs.cuentainversion.port.out.Colaboradores;
import cl.bcs.cuentainversion.port.out.ColaboradoresReportes;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Production implementation of {@link ColaboradoresReportes}: the typed collaborator calls of the reporting services, expressed as
 * calls to the legacy JavaCall classes by name through the generic {@link Colaboradores} port (in process when this module owns the
 * class, otherwise the platform JavaCall router; URL from the environment, no credentials here).
 *
 * <p>JSON contracts (the JSON form of the legacy Entrada / Salida POJOs, field names unchanged):
 * <ul>
 *   <li>{@code ConsultarEmpresa}: input {@code {"valor": idEmpresa}} (the legacy entry point is not JSON; same convention as
 *       {@code domain.colaborador.ConsultarEmpresa}), output the company data; an empty answer is "Empresa no existe";</li>
 *   <li>{@code ConsultarSaldoCuentaInversion}, {@code CalcularParidad}, {@code CrearMovimientoComprobante},
 *       {@code EntregarPreciosTasas}: their own Entrada / Salida;</li>
 *   <li>cartola collaborators ({@code MovimientosTitulos}, {@code MovimientosCaja}, {@code DatosCartolaSocioNegocio}): input
 *       {@code BcsCartola.EntradaCartola}, output {@code SalidaCartola}; the rows are {@code P_OUT_SALIDA[<name>]}.</li>
 * </ul>
 * Pending outside this module: the three cartola classes are plain {@code BcsCartola} implementations in the legacy (not JavaCall) and
 * the normativa service does not expose them under their legacy names yet; until it does those three calls answer from the router
 * as "not migrated". Decision for the owners of BcsNormativaServicios.
 */
@Component
public class ColaboradoresReportesAdapter implements ColaboradoresReportes {

    static final String EMPRESA = "cl.bcs.plataforma.pla.utils.ConsultarEmpresa";
    static final String SALDO = "cl.bcs.cuentaInversion.ConsultarSaldoCuentaInversion";
    static final String PARIDAD = "cl.bcs.plataforma.mer.CalcularParidad";
    static final String COMPROBANTE = "cl.bcs.facturacion.CrearMovimientoComprobante";
    static final String PRECIOS_TASAS = "cl.bcs.plataforma.tas.EntregarPreciosTasas";
    static final String MOVIMIENTOS_TITULOS = "cl.bcs.normativa.cartola.MovimientosTitulos";
    static final String MOVIMIENTOS_CAJA = "cl.bcs.normativa.cartola.MovimientosCaja";
    static final String SOCIO_NEGOCIO = "cl.bcs.normativa.cartola.DatosCartolaSocioNegocio";

    private final Colaboradores colaboradores;

    public ColaboradoresReportesAdapter(Colaboradores colaboradores) {
        this.colaboradores = colaboradores;
    }

    @Override
    public JsonObject consultarEmpresa(long idEmpresa) {
        JsonObject entrada = new JsonObject();
        entrada.addProperty("valor", idEmpresa);
        JsonObject salida = colaboradores.invocar(EMPRESA, entrada);
        if (salida == null || salida.size() == 0) {
            throw new IllegalStateException("Empresa no existe");
        }
        return salida;
    }

    @Override
    public JsonObject consultarSaldo(JsonObject entrada) {
        return invocar(SALDO, entrada);
    }

    @Override
    public JsonObject calcularParidad(JsonObject entrada) {
        return invocar(PARIDAD, entrada);
    }

    @Override
    public JsonObject crearMovimientoComprobante(JsonObject entrada) {
        return invocar(COMPROBANTE, entrada);
    }

    @Override
    public List<JsonObject> movimientosTitulos(JsonObject entradaCartola) {
        return filas(MOVIMIENTOS_TITULOS, "movimientosTitulos", entradaCartola);
    }

    @Override
    public List<JsonObject> movimientosCaja(JsonObject entradaCartola) {
        return filas(MOVIMIENTOS_CAJA, "movimientosCaja", entradaCartola);
    }

    @Override
    public List<JsonObject> datosSocioNegocio(JsonObject entradaCartola) {
        return filas(SOCIO_NEGOCIO, "datosSocioNegocio", entradaCartola);
    }

    @Override
    public JsonObject entregarPreciosTasas(JsonObject entrada) {
        return invocar(PRECIOS_TASAS, entrada);
    }

    private JsonObject invocar(String servicio, JsonObject entrada) {
        JsonObject salida = colaboradores.invocar(servicio, entrada);
        if (salida == null) {
            throw new IllegalStateException("collaborator " + servicio + " answered nothing");
        }
        return salida;
    }

    private List<JsonObject> filas(String servicio, String clave, JsonObject entradaCartola) {
        JsonObject salida = invocar(servicio, entradaCartola);
        List<JsonObject> filas = new ArrayList<>();
        JsonObject datos = salida.has("P_OUT_SALIDA") && salida.get("P_OUT_SALIDA").isJsonObject() ? salida.getAsJsonObject("P_OUT_SALIDA") : null;
        JsonElement lista = datos == null ? null : datos.get(clave);
        if (lista instanceof JsonArray a) {
            a.forEach(e -> filas.add(e.getAsJsonObject()));
        }
        return filas;
    }
}
