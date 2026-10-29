package cl.bcs.plataforma.internal;

import org.junit.jupiter.api.Test;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Contract: every JavaCall class of the registry export (PLA.Servicio, HANA QA) that belongs to an already migrated owner resolves
 * to a documented destination or to a documented "not routable yet" marker, and every group-M collaborator of the inventory (A.2) too.
 * Markers: NO_RUTEABLE_AUN (APV: no typed route in the registry) and DUENO_503 (the owner does not serve the class yet: it answers
 * 503 and the router falls back to TomEE).
 */
class ContratoDestinosTest {

    private enum Resolucion { RUTA_APV, RUTA_DUENO, NO_RUTEABLE_AUN, DUENO_503 }

    private static final List<String> PAQUETES = List.of("cl.bcs.APV.", "cl.bcs.tesoreria.", "cl.bcs.cuentaInversion.", "cl.bcs.contabilidad.");

    private static List<String> recurso(String nombre) throws Exception {
        try (InputStream in = ContratoDestinosTest.class.getResourceAsStream("/javacall/" + nombre)) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8).lines().filter(l -> !l.isBlank()).toList();
        }
    }

    private static RegistroServicios registro() throws Exception {
        try (InputStream in = ContratoDestinosTest.class.getResourceAsStream("/javacall/pla_servicio_trim.csv")) {
            return RegistroServicios.desde(in);
        }
    }

    /** The destinations exactly as shipped in application.properties (nested placeholders resolved with their defaults). */
    private static List<Destino> destinosPorDefecto() throws Exception {
        Properties p = new Properties();
        try (InputStream in = ContratoDestinosTest.class.getResourceAsStream("/application.properties")) {
            p.load(new java.io.InputStreamReader(in, StandardCharsets.UTF_8));
        }
        StandardEnvironment env = new StandardEnvironment();
        env.getPropertySources().addFirst(new PropertiesPropertySource("app", p));
        return Destino.parsear(env.getProperty("plataforma.javacall.destinos"));
    }

    private static Resolucion resolver(String clase, List<Destino> destinos) throws Exception {
        Destino d = Destino.buscar(destinos, clase);
        assertThat(d).as("destination of " + clase).isNotNull();
        if (d.estrategia() == Destino.Estrategia.APV_REST) {
            return ApvRestEstrategia.operacion(clase) != null ? Resolucion.RUTA_APV : Resolucion.NO_RUTEABLE_AUN;
        }
        String serv = d.prefijo().contains("tesoreria") ? "tesoreria" : d.prefijo().contains("cuentaInversion") ? "cuentainversion" : "contabilidad";
        return Set.copyOf(recurso("servidas-" + serv + ".txt")).contains(clase) ? Resolucion.RUTA_DUENO : Resolucion.DUENO_503;
    }

    @Test
    void exportDelRegistro_conteosPorTipo() throws Exception {
        RegistroServicios r = registro();
        assertThat(r.filas("Q")).isEqualTo(1347);
        assertThat(r.filas("J")).isEqualTo(981);
        assertThat(r.filas("P")).isEqualTo(826);
        assertThat(r.filas("X")).isEqualTo(6);
        assertThat(r.filas("O")).isEqualTo(2);
        assertThat(r.filas("A")).isEqualTo(1);
        assertThat(r.filas("Q") + r.filas("J") + r.filas("P") + r.filas("X") + r.filas("O") + r.filas("A")).isEqualTo(3163);
    }

    @Test
    void destinosPorDefecto_cuatroDuenos() throws Exception {
        List<Destino> d = destinosPorDefecto();
        assertThat(d).extracting(Destino::prefijo).containsExactlyInAnyOrderElementsOf(PAQUETES);
        assertThat(d).filteredOn(x -> x.prefijo().equals("cl.bcs.APV.")).extracting(Destino::estrategia).containsExactly(Destino.Estrategia.APV_REST);
        assertThat(d).filteredOn(x -> !x.prefijo().equals("cl.bcs.APV.")).extracting(Destino::estrategia).containsOnly(Destino.Estrategia.JAVACALL);
    }

    @Test
    void todaClaseJavaDeUnDuenoMigrado_resuelveADestinoOMarcador() throws Exception {
        List<Destino> destinos = destinosPorDefecto();
        TreeMap<Resolucion, Set<String>> por = new TreeMap<>();
        int[] total = { 0 };
        registro().clasesJava(c -> {
            if (PAQUETES.stream().noneMatch(c::startsWith)) return;
            total[0]++;
            try {
                por.computeIfAbsent(resolver(c, destinos), k -> new TreeSet<>()).add(c);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });
        por.forEach((k, v) -> System.out.println("CONTRATO " + k + " = " + v.size() + (k.ordinal() >= 2 ? " " + v : "")));
        assertThat(total[0]).isEqualTo(139);
        assertThat(por.values().stream().mapToInt(Set::size).sum()).isEqualTo(139);
        // recorded markers: a change in the registry, in an owner's class table or in the export must be a conscious edit of these files
        assertThat(por.getOrDefault(Resolucion.NO_RUTEABLE_AUN, Set.of())).containsExactlyInAnyOrderElementsOf(recurso("apv-no-ruteables-aun.txt"));
        assertThat(por.getOrDefault(Resolucion.DUENO_503, Set.of())).containsExactlyInAnyOrderElementsOf(recurso("dueno-503-aun.txt"));
    }

    @Test
    void grupoM_losCatorceColaboradores_resuelven() throws Exception {
        List<String> m = recurso("grupo-m.txt");
        assertThat(m).hasSize(14).doesNotHaveDuplicates();
        List<Destino> destinos = destinosPorDefecto();
        for (String c : m) {
            Resolucion r = resolver(c, destinos);
            if (c.equals("cl.bcs.APV.ValidarMandatosPAC")) {
                assertThat(r).as(c + " (call() returns null in the legacy: no typed route)").isEqualTo(Resolucion.NO_RUTEABLE_AUN);
            } else {
                assertThat(r).as(c).isIn(Resolucion.RUTA_APV, Resolucion.RUTA_DUENO);
            }
        }
    }

    @Test
    void validarMandatosPACLogica_esSoloEnProceso() {
        assertThat(ApvRestEstrategia.soloEnProceso("cl.bcs.APV.ValidarMandatosPAC.logica")).isTrue();
        assertThat(ApvRestEstrategia.soloEnProceso("cl.bcs.APV.ValidarMandatosPAC")).isFalse();
    }
}
