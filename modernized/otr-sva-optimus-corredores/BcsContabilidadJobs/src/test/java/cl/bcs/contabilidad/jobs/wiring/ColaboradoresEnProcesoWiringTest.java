package cl.bcs.contabilidad.jobs.wiring;

import cl.bcs.contabilidad.domain.port.out.PlataformaPort;
import cl.bcs.contabilidad.infrastructure.plataforma.TomEePlataformaClient;
import cl.bcs.contabilidad.jobs.adapter.DespachadorEnProceso;
import cl.bcs.contabilidad.jobs.adapter.PlataformaEnProceso;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.test.util.AopTestUtils;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Step 5 with the real application context on H2 (colaboradores-en-proceso=true): the wiring, the routing by owner, the real Tesoreria service reached
 * without HTTP, the configuration that must not leak from the library, and the absence of library cycles. The transaction proofs are in
 * {@link ColaboradoresEnProcesoTransaccionTest}.
 */
class ColaboradoresEnProcesoWiringTest extends EnProcesoBase {

    private static final String CLEARING = PlataformaEnProceso.CLEARING_FINANCIERO;
    private static final String EMPRESA = "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa";

    @Autowired
    ApplicationContext contexto;
    @Autowired
    PlataformaPort plataforma;
    @Autowired
    DespachadorEnProceso despachador;
    @Autowired
    Environment entorno;

    @BeforeEach
    void vistaDeEmpresasVacia() {
        // no company: Tesoreria's service answers UI_CORREDOR_NO_EXISTE (legacy TES-B-38) without needing any other table
        crearTabla(EMPRESA, String.join(", ", Stream.of("IdEmpresa", "Codigo", "Nombre", "IdEstado", "IdPais", "IdSocioDeNegocio", "NombreCorto",
                "IdContraparte", "IdPersona", "FechaIngreso", "FechaCreacion", "FechaModificacion", "IdDocumentoIdentidad", "DocumentoIdentidad",
                "TipoDocumentoIdentidad", "DocumentoIdentidadAdicional", "TipoDocumentoIdentidadAdicional", "RazonSocial", "CodigoContraparte",
                "EsCorredora", "DocumentoIdentidadRepresentante", "DireccionElectronicaPrincipal", "DireccionFisicaPrincipalCalle",
                "DireccionFisicaPrincipalNumero", "DireccionFisicaPrincipalDepartamento", "DireccionFisicaPrincipalSector",
                "DireccionFisicaPrincipalNombreCiudad", "DireccionFisicaPrincipalNombreDivisionPais", "DireccionFisicaPrincipalNombrePais",
                "TelefonoPrincipal").map(c -> "\"" + c + "\" VARCHAR(200)").toList()));
    }

    @Test
    @DisplayName("S5-W01: the context starts (no Spring cycle); the in-process port is the primary one and wraps TomEePlataformaClient")
    void elPuertoEnProcesoEsPrimarioYElHttpEsElDelegado() {
        assertThat(plataforma).isInstanceOf(PlataformaEnProceso.class);
        assertThat(contexto.getBeansOfType(PlataformaPort.class)).hasSize(2);
        assertThat(contexto.getBeansOfType(PlataformaEnProceso.class)).hasSize(1);
        assertThat(contexto.getBeansOfType(TomEePlataformaClient.class)).hasSize(1);
        assertThat(ReflectionTestUtils.getField(plataforma, "http")).isSameAs(contexto.getBean(TomEePlataformaClient.class));
    }

    @Test
    @DisplayName("S5-W02: the Contabilidad service that calls the clearing got the in-process port (the Servicios sources are not edited)")
    void elServicioDeContabilidadRecibeElPuertoEnProceso() {
        Object servicio = AopTestUtils.getUltimateTargetObject(contexto.getBean("centralizaClearingFinanciero"));
        assertThat(ReflectionTestUtils.getField(servicio, "plataforma")).isSameAs(plataforma);
    }

    @Test
    @DisplayName("S5-W03: only the 3 Tesoreria beans the clearing needs are scanned (no web layer, no router adapter, no other service)")
    void soloSeEscaneanLosBeansDeTesoreriaQueHacenFalta() {
        List<String> beans = new ArrayList<>();
        for (String nombre : contexto.getBeanDefinitionNames()) {
            if (nombre.startsWith("cl.bcs.tesoreria.")) {
                beans.add(nombre);
            }
        }
        assertThat(beans).containsExactlyInAnyOrder("cl.bcs.tesoreria.javacall.JavaCallDispatcher",
                "cl.bcs.tesoreria.javacall.liq.ConsultarClearingFinancieroService",
                "cl.bcs.tesoreria.infrastructure.adapter.ConfiguracionSistemaJdbc");
        assertThat(contexto.getBeansOfType(cl.bcs.tesoreria.port.out.Colaboradores.class)).isEmpty();
    }

    @Test
    @DisplayName("S5-W04: the clearing is owned by Tesoreria; a Tesoreria name that has no bean here and the other owners' names are not claimed")
    void soloElClearingSeReclama() {
        assertThat(despachador.propietarioDe(CLEARING)).hasValueSatisfying(p -> assertThat(p.propietario()).isEqualTo("tesoreria"));
        assertThat(despachador.maneja("cl.bcs.tesoreria.CrearIngresoEgreso")).isFalse();
        assertThat(despachador.maneja("cl.bcs.tesoreria.liq.ProcesarClearingFinanciero")).isFalse();
        assertThat(despachador.maneja("cl.bcs.APV.ValidarMandatosPAC")).isFalse();
        assertThat(despachador.maneja("cl.bcs.plataforma.pla.ObtenerDiaAnterior")).isFalse();
    }

    @Test
    @DisplayName("S5-W05: the port reaches the REAL Tesoreria service without HTTP (empty company view -> legacy UI_CORREDOR_NO_EXISTE, no rows)")
    void elServicioRealDeTesoreriaSeAlcanzaSinHttp() {
        assertThat(plataforma.consultarClearingFinanciero(1L, 5L, "CON", "2025-04-01")).isEmpty();

        JsonObject entrada = new JsonObject();
        entrada.addProperty("p_IdEmpresa", 1L);
        entrada.addProperty("p_FechaLiquidacion", "2025-04-01");
        entrada.add("p_Camaras", new com.google.gson.JsonArray());
        JsonObject salida = despachador.despachar(CLEARING, entrada);
        assertThat(salida.getAsJsonArray("P_OUT_MENSAJES").get(0).getAsJsonObject().get("codigo").getAsString())
                .isEqualTo("UI_CORREDOR_NO_EXISTE");
        assertThat(PETICIONES).as("nothing went to TomEE").isEmpty();
    }

    @Test
    @DisplayName("S5-W06: what is not group M still goes over HTTP: ObtenerDiaAnterior reaches the fake TomEE through the delegate")
    void loQueNoEsGrupoMSigueEnHttp() {
        // the fake answers [] (not a fechaCalculada object): the unchanged TomEePlataformaClient fails parsing it, but the request was made
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> plataforma.obtenerDiaAnterior(56L, "2025-04-01"))
                .isInstanceOf(RuntimeException.class);
        assertThat(PETICIONES).containsExactly("/api/plataforma/ObtenerDiaAnterior");
    }

    @Test
    @DisplayName("S5-W07: the library's application.properties does not leak into the application (name, no router URL), Tesoreria's included")
    void laConfiguracionDeLasLibreriasNoSeFiltra() {
        assertThat(entorno.getProperty("spring.application.name")).isEqualTo("bcs-contabilidad-jobs");
        assertThat(entorno.getProperty("services.javacall.url")).isNull();
        assertThat(entorno.getProperty("server.port")).isEqualTo("8082");
    }

    @Test
    @DisplayName("S5-W08: neither library depends on the other at class-file level (the link is made in this application)")
    void ningunaLibreriaDependeDeOtraEnTiempoDeCompilacion() throws Exception {
        assertThat(referencias("cl.bcs.tesoreria.javacall.JavaCallDispatcher", "cl/bcs/contabilidad/")).isEmpty();
        assertThat(referencias("cl.bcs.contabilidad.infrastructure.plataforma.TomEePlataformaClient", "cl/bcs/tesoreria/")).isEmpty();
        assertThat(AopUtils.isAopProxy(contexto.getBean("cl.bcs.tesoreria.javacall.liq.ConsultarClearingFinancieroService")))
                .as("transactional proxy: the service joins the transaction of the caller").isTrue();
    }

    /** Class files of the library (jar or, in the reactor build, the classes directory) that holds {@code clase} and mention a prefix. */
    private static List<String> referencias(String clase, String prefijo) throws IOException, ClassNotFoundException {
        var origen = Class.forName(clase).getProtectionDomain().getCodeSource().getLocation();
        File f = new File(java.net.URI.create(origen.toString().replace("jar:", "").replaceAll("!/.*", "")));
        List<String> encontrados = new ArrayList<>();
        if (f.isDirectory()) {
            try (Stream<java.nio.file.Path> archivos = Files.walk(f.toPath())) {
                for (java.nio.file.Path p : archivos.filter(a -> a.toString().endsWith(".class")).toList()) {
                    if (new String(Files.readAllBytes(p), StandardCharsets.ISO_8859_1).contains(prefijo)) {
                        encontrados.add(p + " -> " + prefijo);
                    }
                }
            }
            return encontrados;
        }
        try (ZipFile zip = new ZipFile(f)) {
            for (var it = zip.entries().asIterator(); it.hasNext(); ) {
                ZipEntry e = it.next();
                if (e.getName().endsWith(".class")
                        && new String(zip.getInputStream(e).readAllBytes(), StandardCharsets.ISO_8859_1).contains(prefijo)) {
                    encontrados.add(e.getName() + " -> " + prefijo);
                }
            }
        }
        return encontrados;
    }
}
