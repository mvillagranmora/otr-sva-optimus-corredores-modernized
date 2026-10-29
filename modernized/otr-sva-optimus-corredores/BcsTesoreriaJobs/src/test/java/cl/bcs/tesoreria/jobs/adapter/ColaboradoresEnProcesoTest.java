package cl.bcs.tesoreria.jobs.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.tesoreria.infrastructure.adapter.ColaboradoresJavaCall;
import cl.bcs.tesoreria.jobs.job.port.ServiciosExternos;
import cl.bcs.tesoreria.port.out.Colaboradores;

/**
 * Step 5, unit level (no Spring, no database): which collaborators ColaboradoresEnProceso answers in process, which ones it leaves to the HTTP port,
 * and how the Tesoreria &lt;-&gt; APV &lt;-&gt; CuentaInversion calls are routed between owners without any library depending on another.
 */
class ColaboradoresEnProcesoTest {

    /** Owner stub that records the calls and answers {@code {"propietario": ..., "clase": ...}}. */
    private static final class Propietario implements EntradaEnProceso {
        final String nombre;
        final Set<String> clases;
        final List<String> llamadas = new ArrayList<>();
        final List<JsonObject> entradas = new ArrayList<>();
        Exception fallo;

        Propietario(String nombre, String... clases) {
            this.nombre = nombre;
            this.clases = Set.of(clases);
        }

        @Override
        public String propietario() {
            return nombre;
        }

        @Override
        public boolean maneja(String claseLegacy) {
            return clases.contains(claseLegacy);
        }

        @Override
        public JsonObject despachar(String claseLegacy, JsonObject entrada) throws Exception {
            llamadas.add(claseLegacy);
            entradas.add(entrada);
            if (fallo != null) {
                throw fallo;
            }
            JsonObject salida = new JsonObject();
            salida.addProperty("propietario", nombre);
            salida.addProperty("clase", claseLegacy);
            return salida;
        }
    }

    /** The HTTP port: records the names it receives. */
    private static final class Http implements Colaboradores {
        final List<String> nombres = new ArrayList<>();

        @Override
        public JsonObject invocar(String servicio, JsonObject entrada) {
            nombres.add(servicio);
            JsonObject salida = new JsonObject();
            salida.addProperty("via", "http");
            return salida;
        }
    }

    private static JsonObject json(String texto) {
        return JsonParser.parseString(texto).getAsJsonObject();
    }

    private final Propietario apv = new Propietario("apv", "cl.bcs.APV.ValidarMandatosPAC.logica", "cl.bcs.APV.CambiarEstadoContratosSAAPV");
    private final Propietario cuentaInversion = new Propietario("cuenta-inversion", "cl.bcs.cuentaInversion.CrearCargoAbono",
            "cl.bcs.cuentaInversion.AnularCargoAbono");
    private final Http http = new Http();
    private final ColaboradoresEnProceso colaboradores = new ColaboradoresEnProceso(http, new DespachadorEnProceso(List.of(apv, cuentaInversion)));

    @Test
    void cadaNombreDelGrupoMSeResuelveEnProcesoSinHttp() {
        for (Map.Entry<String, String> m : ColaboradoresEnProceso.GRUPO_M.entrySet()) {
            JsonObject salida = colaboradores.invocar(m.getKey(), json("{\"x\":1}"));
            assertThat(salida.get("clase").getAsString()).as(m.getKey()).isEqualTo(m.getValue());
        }
        assertThat(http.nombres).as("no group M collaborator travels over HTTP").isEmpty();
        assertThat(apv.llamadas).containsExactlyInAnyOrder("cl.bcs.APV.ValidarMandatosPAC.logica", "cl.bcs.APV.CambiarEstadoContratosSAAPV");
        assertThat(cuentaInversion.llamadas).containsExactlyInAnyOrder("cl.bcs.cuentaInversion.CrearCargoAbono", "cl.bcs.cuentaInversion.AnularCargoAbono");
    }

    @Test
    void elGrupoMSonExactamenteLosCuatroNombresQueEsteModuloEnviabaPorHttp() {
        assertThat(ColaboradoresEnProceso.GRUPO_M.keySet())
                .containsExactlyInAnyOrder("ValidarMandatosPAC", "CambiarEstadoContratosSAAPV", "CrearCargoAbono", "AnularCargoAbono");
    }

    @Test
    void laTablaDeClasesCoincideConLasQueUsabaElAdaptadorHttp() {
        // ValidarMandatosPAC in process is the TYPED operation (class of the JavaCall that ColaboradoresJobs sends over HTTP + ".logica"); the other
        // three are the ones tesoreria-service's adapter sends
        assertThat(ColaboradoresEnProceso.GRUPO_M.get(ServiciosExternos.VALIDAR_MANDATOS_PAC))
                .isEqualTo(ColaboradoresJobs.CLASES.get(ServiciosExternos.VALIDAR_MANDATOS_PAC) + ".logica");
        for (String nombre : List.of("CambiarEstadoContratosSAAPV", "CrearCargoAbono", "AnularCargoAbono")) {
            assertThat(ColaboradoresEnProceso.GRUPO_M.get(nombre)).as(nombre).isEqualTo(ColaboradoresJavaCall.clase(nombre));
        }
    }

    @Test
    void losNombresQueNoSonDelGrupoMSiguenPorElPuertoHttp() {
        List<String> otros = List.of(ServiciosExternos.VALIDA_EJECUCION_PROCESO_REC_PAC, ServiciosExternos.CONSULTAR_ESTADO_MODULO,
                ServiciosExternos.OBTENER_DIA_ANTERIOR, ServiciosExternos.AGREGAR_LOG, "CrearOrdenes", "CrearSolicitudAporteRescateNormal",
                "CalcularMontoYCobrosOrden", "CalcularMontoNumeroCuota", "ConsultarInstrumentoPrecioBolsa",
                // Tesoreria's own services: tesoreria-service's adapter already runs them in process behind the HTTP port
                "CrearIngresoEgreso", "LiquidarComprobante", "StoredProcedure", "Cloper");
        for (String nombre : otros) {
            assertThat(colaboradores.invocar(nombre, new JsonObject()).get("via").getAsString()).as(nombre).isEqualTo("http");
        }
        assertThat(http.nombres).containsExactlyElementsOf(otros);
        assertThat(apv.llamadas).isEmpty();
        assertThat(cuentaInversion.llamadas).isEmpty();
    }

    @Test
    void siElPropietarioNoLaSirveLaLlamadaVaPorHttp() {
        ColaboradoresEnProceso sinApv = new ColaboradoresEnProceso(http, new DespachadorEnProceso(List.of(cuentaInversion)));
        assertThat(sinApv.invocar("ValidarMandatosPAC", new JsonObject()).get("via").getAsString()).isEqualTo("http");
        assertThat(http.nombres).containsExactly("ValidarMandatosPAC");
    }

    @Test
    void laEntradaLlegaSinAlterarseAlPropietario() {
        JsonObject entrada = json("{\"p_IdEmpresa\":1,\"p_CargoAbonoAGenerar\":[{\"NumeroCargoAbono\":1,\"Monto\":50}],\"nulo\":null}");
        colaboradores.invocar("CrearCargoAbono", entrada);
        assertThat(cuentaInversion.entradas).hasSize(1);
        assertThat(cuentaInversion.entradas.get(0)).isSameAs(entrada);
    }

    @Test
    void unaExcepcionDeEjecucionDelServicioSePropagaSinCambios() {
        RuntimeException original = new IllegalArgumentException("rule violated");
        cuentaInversion.fallo = original;
        assertThatThrownBy(() -> colaboradores.invocar("CrearCargoAbono", new JsonObject())).isSameAs(original);
    }

    @Test
    void unaExcepcionVerificadaSeEnvuelveYNoSePierdeLaCausa() {
        Exception original = new IOException("checked");
        apv.fallo = original;
        assertThatThrownBy(() -> colaboradores.invocar("CambiarEstadoContratosSAAPV", new JsonObject()))
                .isInstanceOf(IllegalStateException.class).hasCause(original).hasMessageContaining("apv");
    }

    @Test
    void elPuertoDeApvResuelveAlPropietarioSinCiclosDeLibrerias() {
        Propietario tesoreria = new Propietario("tesoreria", "cl.bcs.tesoreria.CrearIngresoEgreso", "cl.bcs.tesoreria.AnulacionMandatoPAC",
                "cl.bcs.tesoreria.RevocacionMandatoPAC", "cl.bcs.tesoreria.IngresarMandatoPAC", "cl.bcs.tesoreria.LiquidarComprobante",
                "cl.bcs.tesoreria.CrearComprobante");
        DespachadorEnProceso despachador = new DespachadorEnProceso(List.of(tesoreria, apv, cuentaInversion));
        List<String> viaHttp = new ArrayList<>();
        cl.bcs.apv.domain.port.out.Colaboradores httpApv = (servicio, entrada) -> {
            viaHttp.add(servicio);
            return new JsonObject();
        };
        ColaboradoresApvEnProceso puertoApv = new ColaboradoresApvEnProceso(despachador, httpApv);

        // APV -> Tesoreria and APV -> CuentaInversion: in process, to the owner
        for (String nombre : List.of("CrearIngresoEgreso", "AnulacionMandatoPAC", "RevocacionMandatoPAC", "IngresarMandatoPAC", "LiquidarComprobante",
                "CrearComprobante")) {
            assertThat(puertoApv.invocar(nombre, new JsonObject()).get("propietario").getAsString()).as(nombre).isEqualTo("tesoreria");
        }
        assertThat(puertoApv.invocar("CrearCargoAbono", new JsonObject()).get("propietario").getAsString()).isEqualTo("cuenta-inversion");
        assertThat(viaHttp).isEmpty();

        // Plataforma / Orden / StoredProcedure: through the APV library adapter
        for (String nombre : List.of("AgregarLog", "CrearOrdenes", "StoredProcedure", "CrearCliente")) {
            puertoApv.invocar(nombre, new JsonObject());
        }
        assertThat(viaHttp).containsExactly("AgregarLog", "CrearOrdenes", "StoredProcedure", "CrearCliente");

        // an unregistered name still fails closed instead of travelling bare
        assertThatThrownBy(() -> puertoApv.invocar("NoRegistrado", new JsonObject())).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void elPuertoDeCuentaInversionResuelveAlPropietarioYElRestoVaAlRouter() {
        Propietario tesoreria = new Propietario("tesoreria", "cl.bcs.tesoreria.CrearIngresoEgreso");
        ColaboradoresCuentaInversionEnProceso puerto = new ColaboradoresCuentaInversionEnProceso(
                new DespachadorEnProceso(List.of(tesoreria, cuentaInversion)), (org.springframework.web.client.RestClient) null, "");
        assertThat(puerto.invocar("cl.bcs.tesoreria.CrearIngresoEgreso", new JsonObject()).get("propietario").getAsString()).isEqualTo("tesoreria");
        // no router configured: a platform call fails when it is made, it is never silently dropped
        assertThatThrownBy(() -> puerto.invocar("cl.bcs.plataforma.pla.SaldoFlujoCaja", new JsonObject()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("JAVACALL_URL");
    }
}
