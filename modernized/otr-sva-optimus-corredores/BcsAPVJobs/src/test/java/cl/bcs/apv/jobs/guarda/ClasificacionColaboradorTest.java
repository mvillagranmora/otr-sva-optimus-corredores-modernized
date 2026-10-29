package cl.bcs.apv.jobs.guarda;

import cl.bcs.apv.infrastructure.adapter.ColaboradoresJavaCall;
import cl.bcs.apv.jobs.adapter.ColaboradoresJobs;
import cl.bcs.apv.jobs.guarda.ClasificacionColaborador.Categoria;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/** Which collaborator names the guard protects (inventory JOBS_H1_H4_INVENTORY.md A.2, verified against what this module actually sends). */
class ClasificacionColaboradorTest {

    private static final Pattern LITERAL = Pattern.compile("colaboradores\\s*\\.\\s*invocar\\(\\s*\"([A-Za-z0-9_.]+)\"");
    private static final Pattern CUALQUIERA = Pattern.compile("colaboradores\\s*\\.\\s*invocar\\(");

    private static Set<String> de(Categoria c) {
        return ClasificacionColaborador.todas().entrySet().stream().filter(e -> e.getValue() == c)
                .map(java.util.Map.Entry::getKey).collect(Collectors.toCollection(TreeSet::new));
    }

    /** Every literal passed to {@code colaboradores.invocar(...)} in the production sources of this module; a call with a non-literal name fails. */
    private static Set<String> literalesDelModulo() throws IOException {
        Path raiz = Path.of(System.getProperty("basedir", System.getProperty("user.dir"))).toAbsolutePath().normalize().resolve("src/main/java");
        Set<String> nombres = new TreeSet<>();
        int llamadas = 0;
        int literales = 0;
        try (Stream<Path> s = Files.walk(raiz)) {
            List<Path> java = s.filter(p -> p.toString().endsWith(".java")).toList();
            assertThat(java).as("the scan must see the job sources").isNotEmpty();
            for (Path p : java) {
                String texto = Files.readString(p, StandardCharsets.UTF_8);
                Matcher m = LITERAL.matcher(texto);
                while (m.find()) {
                    nombres.add(m.group(1));
                    literales++;
                }
                llamadas += (int) CUALQUIERA.matcher(texto).results().count();
            }
        }
        assertThat(literales).as("invocar( literal call sites found").isGreaterThan(5);
        assertThat(llamadas).as("every invocar( call of the module names its collaborator with a literal").isEqualTo(literales);
        return nombres;
    }

    @Test
    @DisplayName("G6-C01: every name this module can send over the collaborator port is classified (no name escapes the guard by omission)")
    void todoNombreEstaClasificado() throws IOException {
        Set<String> enviados = new TreeSet<>(ColaboradoresJavaCall.nombresConocidos()); // what the service adapter routes (and the library services send)
        enviados.addAll(ColaboradoresJobs.nombresPropios()); // the two this module sends to the platform router itself
        enviados.addAll(literalesDelModulo()); // the literals of the four jobs
        assertThat(enviados).contains("CrearOrdenes", "CrearSolicitudAporteRescateNormal", "AgregarLog", "StoredProcedure", "ConsultarFlujoCaja",
                "ConsultarFlujoCustodiaFondoMutuo");

        assertThat(enviados).allSatisfy(n -> assertThat(ClasificacionColaborador.de(n)).as(n).isNotNull());
    }

    @Test
    @DisplayName("G6-C02: the non-repeatable TomEE writes of inventory A.2 (group TW) are guarded: orders, fund requests, events, programs, clients...")
    void escrituraNoRepetible() {
        assertThat(de(Categoria.NO_REPETIBLE)).containsExactlyInAnyOrder("AgregarPrograma", "InscribirProcesoBatch", "AgregarEvento",
                "CrearEventoAutorizacionMasivo", "ModificarEstadoEventoAutorizacionMasivo", "ValidaEjecucionProcesoRecPAC", "CrearOrdenes",
                "CrearSolicitudAporteRescateNormal", "AnulacionContratoCSC", "RevocacionContratoCSC", "ModificarPortafolio", "CrearCliente",
                "CrearPersona", "CrearTelefono", "CrearDireccionElectronica", "CrearPersonaCuentaBancaria", "CrearMovimientoComprobante");
        assertThat(de(Categoria.NO_REPETIBLE)).allSatisfy(n -> assertThat(ClasificacionColaborador.guardada(n)).isTrue());
    }

    @Test
    @DisplayName("G6-C03: AgregarLog is the only append-only write and is NOT guarded; group TW = 17 guarded + AgregarLog = 18 clases")
    void appendOnly() {
        assertThat(de(Categoria.APPEND_ONLY)).containsExactly("AgregarLog");
        assertThat(ClasificacionColaborador.guardada("AgregarLog")).isFalse();
        assertThat(de(Categoria.NO_REPETIBLE).size() + de(Categoria.APPEND_ONLY).size()).isEqualTo(18);
    }

    @Test
    @DisplayName("G6-C04: reads, APV's own services and the specials are not guarded; the group M writers of Tesoreria / CuentaInversion are (they commit apart)")
    void restoDeCategorias() {
        for (String n : Set.of("ConsultarEstadoModulo", "ObtenerDiaAnterior", "CalcularParidad", "ConsultarFlujoCaja", "SaldoFlujoCaja",
                "RevisarRequisitosBasicos", "CalcularMontoYCobrosOrden", "CalcularMontoNumeroCuota", "ConsultarInstrumentoPrecioBolsa",
                "EntregarPreciosTasas", "ConsultarEmpresa", "ConsultarCustodiaPortafolio", "ConsultarCustodiaPortafolioFondoMutuo",
                "ConsultarFlujoCustodiaFondoMutuo", "ConsultarSaldoCuentaInversion", "ValidarMandatosPAC", "CambiarEstadoContratosSAAPV",
                "StoredProcedure", "Cloper")) {
            assertThat(ClasificacionColaborador.guardada(n)).as(n).isFalse();
        }
        assertThat(de(Categoria.MIGRADO_ESCRITOR)).containsExactlyInAnyOrder("CrearIngresoEgreso", "LiquidarComprobante", "CrearComprobante",
                "ActualizarSaldosCuentaBanco", "AnularNominasTransferencias", "AnulacionMandatoPAC", "RevocacionMandatoPAC", "IngresarMandatoPAC",
                "CrearCargoAbono", "AnularCargoAbono");
        assertThat(de(Categoria.MIGRADO_ESCRITOR)).allSatisfy(n -> assertThat(ClasificacionColaborador.guardada(n)).as(n).isTrue());
    }

    @Test
    @DisplayName("G6-C05: what the jobs of this module send: the two non-repeatable writers are guarded, AgregarLog is append-only, the rest are reads")
    void loQueAlcanzaElModulo() throws IOException {
        // IngresarOrdenBonificacionFiscal / RV: CrearOrdenes; IngresarOrdenFM: CrearSolicitudAporteRescateNormal; Previred: only AgregarLog (library)
        Set<String> guardadas = literalesDelModulo().stream().filter(ClasificacionColaborador::guardada).collect(Collectors.toCollection(TreeSet::new));
        assertThat(guardadas).containsExactly("CrearOrdenes", "CrearSolicitudAporteRescateNormal");
        assertThat(ClasificacionColaborador.de("AgregarLog")).isEqualTo(Categoria.APPEND_ONLY);
        assertThat(ClasificacionColaborador.de("NombreQueNoExiste")).isNull();
        assertThat(ClasificacionColaborador.guardada(null)).isFalse();
    }
}
