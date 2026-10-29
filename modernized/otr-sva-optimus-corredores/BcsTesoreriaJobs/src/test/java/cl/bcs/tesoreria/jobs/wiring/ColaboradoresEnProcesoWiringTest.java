package cl.bcs.tesoreria.jobs.wiring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.tesoreria.jobs.adapter.ColaboradoresEnProceso;
import cl.bcs.tesoreria.jobs.adapter.ColaboradoresJobs;
import cl.bcs.tesoreria.jobs.adapter.DespachadorEnProceso;
import cl.bcs.tesoreria.port.out.Colaboradores;

/**
 * Step 5 with the real application context on H2 (colaboradores-en-proceso=true): the wiring, the routing by owner, the equivalence of the
 * in-process answer with the recorded legacy JSON, and the absence of library cycles. The transaction proofs are in
 * {@link ColaboradoresEnProcesoTransaccionTest}.
 */
class ColaboradoresEnProcesoWiringTest extends EnProcesoBase {

    @Autowired
    ApplicationContext contexto;
    @Autowired
    Colaboradores colaboradores;
    @Autowired
    DespachadorEnProceso despachador;

    @Test
    void elContextoArrancaConElPuertoEnProcesoComoPrimarioYElHttpComoDelegado() {
        // reaching this point is the "no Spring context cycle" proof: Spring Boot forbids circular references by default
        assertThat(colaboradores).isInstanceOf(ColaboradoresEnProceso.class);
        assertThat(contexto.getBeansOfType(ColaboradoresJobs.class)).hasSize(1).containsKey("colaboradoresJobsHttp");
        assertThat(contexto.getBeansOfType(ColaboradoresEnProceso.class)).hasSize(1);
        assertThat(contexto.getBeansOfType(cl.bcs.cuentainversion.port.out.Colaboradores.class)).hasSize(1);
        assertThat(contexto.getBeansOfType(cl.bcs.cuentainversion.port.out.ConfiguracionSistemaPort.class)).hasSize(1);
    }

    @Test
    void soloSeEscaneanLosBeansDeCuentaInversionQueHacenFalta() {
        List<String> beansCuentaInversion = new ArrayList<>();
        for (String nombre : contexto.getBeanDefinitionNames()) {
            if (nombre.startsWith("cl.bcs.cuentainversion.")) {
                beansCuentaInversion.add(nombre);
            }
        }
        assertThat(beansCuentaInversion).containsExactlyInAnyOrder("cl.bcs.cuentainversion.service.CrearCargoAbonoService",
                "cl.bcs.cuentainversion.service.AnularCargoAbonoService", "cl.bcs.cuentainversion.service.ActualizarSaldoCargoAbonoService",
                "cl.bcs.cuentainversion.service.CalendarioHabil", "cl.bcs.cuentainversion.service.FormatoMoneda",
                "cl.bcs.cuentainversion.infrastructure.adapter.DiccionarioJdbc", "cl.bcs.cuentainversion.service.JavaCallDispatcher");
        // neither its REST clients nor its web layer, nor APV (its use cases are built on demand, not scanned)
        assertThat(contexto.getBeanDefinitionNames()).noneMatch(n -> n.startsWith("cl.bcs.apv.") || n.contains("Controller") && n.contains("uentaInversion"));
    }

    @Test
    void cadaPropietarioServeSusClasesDelGrupoM() {
        for (String clase : ColaboradoresEnProceso.GRUPO_M.values()) {
            assertThat(despachador.maneja(clase)).as(clase).isTrue();
        }
        // APV -> Tesoreria and Tesoreria -> APV go through the same dispatcher (cycle resolved in the job application)
        assertThat(despachador.propietarioDe("cl.bcs.tesoreria.CrearIngresoEgreso").orElseThrow().propietario()).isEqualTo("tesoreria");
        assertThat(despachador.propietarioDe("cl.bcs.tesoreria.AnulacionMandatoPAC").orElseThrow().propietario()).isEqualTo("tesoreria");
        assertThat(despachador.propietarioDe("cl.bcs.APV.CambiarEstadoContratosSAAPV").orElseThrow().propietario()).isEqualTo("apv");
        assertThat(despachador.propietarioDe("cl.bcs.cuentaInversion.CrearCargoAbono").orElseThrow().propietario()).isEqualTo("cuenta-inversion");
        // not claimed: the dispatcher of CuentaInversion knows the name but its bean is not part of this application; not migrated ones
        assertThat(despachador.maneja("cl.bcs.cuentaInversion.ConsultarSaldoCuentaInversion")).isFalse();
        assertThat(despachador.maneja("cl.bcs.plataforma.pla.AgregarLog")).isFalse();
        assertThat(despachador.maneja("cl.bcs.ordenes.CrearOrdenes")).isFalse();
    }

    @Test
    void validarMandatosPacRespondeEnProcesoLoQueRespondeElLegacy() {
        JsonObject entrada = JsonParser.parseString("{\"salida\":{\"P_OUT_MENSAJES\":[]},\"idEmpresa\":1,\"idUsuario\":21}").getAsJsonObject();
        // in process the job's call is the typed logica (void: null, no HTTP; it writes LogMandatoPAC, see ValidarMandatosPacEnProcesoTest; here the
        // lists are empty so nothing is written). The HTTP path below can only reach the JavaCall call(), null by golden APV-C-411
        assertThat(colaboradores.invocar("ValidarMandatosPAC", entrada)).isNull();
        assertThat(PETICIONES).isEmpty();
        // the HTTP adapter (the delegate, used when the property is false) does send this call to the router
        contexto.getBean("colaboradoresJobsHttp", ColaboradoresJobs.class).invocar("ValidarMandatosPAC", entrada);
        assertThat(PETICIONES).hasSize(1);
        assertThat(PETICIONES.get(0).get("class").getAsString()).isEqualTo("cl.bcs.APV.ValidarMandatosPAC");
        assertThat(PETICIONES.get(0).getAsJsonObject("params")).isEqualTo(entrada);
    }

    @Test
    void cambiarEstadoContratosEnProcesoDaElJsonYLasTablasDelGoldenDelLegacy() {
        JsonObject caso = caso();
        JsonObject esperado = esperado();
        JsonObject salida = colaboradores.invocar("CambiarEstadoContratosSAAPV", caso.getAsJsonObject("entrada"));

        assertThat(salida).isEqualTo(esperado.getAsJsonObject("salida"));
        for (String tabla : esperado.getAsJsonObject("tablas").keySet()) {
            assertThat(volcar(tabla)).as(tabla).isEqualTo(esperado.getAsJsonObject("tablas").getAsJsonArray(tabla));
        }
        // the use case itself called AgregarLog (Plataforma, still in TomEE): the only request, with the input of the golden trace; no group M name went out
        JsonObject traza = esperado.getAsJsonArray("colaboradores").get(0).getAsJsonObject();
        assertThat(PETICIONES).hasSize(1);
        assertThat(PETICIONES.get(0).get("class").getAsString()).isEqualTo("cl.bcs.plataforma.pla.AgregarLog");
        assertThat(traza.get("servicio").getAsString()).isEqualTo("AgregarLog");
        assertThat(PETICIONES.get(0).getAsJsonObject("params")).isEqualTo(traza.getAsJsonObject("entrada"));
    }

    @Test
    void unaClaseQueNoEsDelGrupoMSigueIgualPorHttp() {
        JsonObject entrada = JsonParser.parseString("{\"p_IdEmpresa\":1,\"p_ValorNuevo\":\"N\"}").getAsJsonObject();
        JsonObject salida = colaboradores.invocar("ValidaEjecucionProcesoRecPAC", entrada);
        assertThat(salida).isNotNull();
        assertThat(PETICIONES).hasSize(1);
        assertThat(PETICIONES.get(0).get("class").getAsString()).isEqualTo("cl.bcs.plataforma.pla.ValidaEjecucionProcesoRecPAC");
        assertThat(PETICIONES.get(0).getAsJsonObject("params")).isEqualTo(entrada);
    }

    @Test
    void crearCargoAbonoLlegaAlServicioRealDeCuentaInversionSinHttp() {
        // company 99 has no PLA.FechaSistema: the REAL CrearCargoAbonoService fails reading it through the JDBC configuration port of this module
        JsonObject entrada = JsonParser.parseString("{\"p_IdEmpresa\":99,\"p_IdUsuario\":1,\"p_IdModulo\":\"TES\",\"p_CargoAbonoAGenerar\":[]}")
                .getAsJsonObject();
        assertThatThrownBy(() -> colaboradores.invocar("CrearCargoAbono", entrada))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("cl.bcs.cuentaInversion.CrearCargoAbono")
                .hasRootCauseMessage("Parametro PLA.FechaSistema en modulo PLATAFORMA no encontrado");
        assertThat(PETICIONES).isEmpty();
    }

    @Test
    void ningunaLibreriaDependeDeOtraEnTiempoDeCompilacion() throws Exception {
        // class files of each library: no reference to the packages of the others (the cycle is resolved in this application, not in the libraries)
        assertThat(referencias("cl.bcs.tesoreria.javacall.JavaCallDispatcher", "cl/bcs/apv/", "cl/bcs/cuentainversion/")).isEmpty();
        assertThat(referencias("cl.bcs.apv.application.ApvCasosDeUso", "cl/bcs/tesoreria/", "cl/bcs/cuentainversion/")).isEmpty();
        assertThat(referencias("cl.bcs.cuentainversion.service.JavaCallDispatcher", "cl/bcs/tesoreria/", "cl/bcs/apv/")).isEmpty();
    }

    /** Class files of the jar that holds {@code clase} that mention one of the prefixes (as class references, written with slashes). */
    private static List<String> referencias(String clase, String... prefijos) throws IOException, ClassNotFoundException {
        var origen = Class.forName(clase).getProtectionDomain().getCodeSource().getLocation();
        List<String> encontrados = new ArrayList<>();
        java.io.File f = new java.io.File(java.net.URI.create(origen.toString().replace("jar:", "").replaceAll("!/.*", "")));
        if (f.isDirectory()) {
            throw new AssertionError("expected a library jar for " + clase + " but found a directory: " + f);
        }
        try (ZipFile zip = new ZipFile(f)) {
            for (var it = zip.entries().asIterator(); it.hasNext(); ) {
                ZipEntry e = it.next();
                if (!e.getName().endsWith(".class")) {
                    continue;
                }
                String bytes = new String(zip.getInputStream(e).readAllBytes(), StandardCharsets.ISO_8859_1);
                for (String p : prefijos) {
                    if (bytes.contains(p)) {
                        encontrados.add(e.getName() + " -> " + p);
                    }
                }
            }
        }
        return encontrados;
    }
}
