package cl.bcs.contabilidad.envio;

import cl.bcs.contabilidad.domain.port.out.PlanCuentasSapPort;
import cl.bcs.contabilidad.envio.support.CasoEnvio;
import cl.bcs.contabilidad.envio.support.Fakes;
import cl.bcs.contabilidad.envio.support.JsonCanon;
import cl.bcs.contabilidad.service.EnvioContabilidadService;
import cl.bcs.contabilidad.service.ReversaPreComprobanteService;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Transform driver (scratch copy, never in modernized/): runs the NEW services on the development CONT-B cases exactly as
 * EnvioContabilidadServiceTest / ReversaPreComprobanteServiceTest do, and writes both the new output and the
 * legacy oracle's expected.json through the same canonical form (JsonCanon: key order, null keys dropped,
 * numbers by value, SQL-unordered arrays sorted; table rows sorted). No assertion: compare.py judges.
 */
class DevCasesDriverTest {

    static final Set<String> SIN_ORDEN = Set.of("P_OUT_ASIENTOS");
    static final String[] TABLAS_REVERSA = {"PreComprobante", "PreComprobanteAsiento", "EventoPreComprobanteAsiento"};

    @Test
    void escribirSalidas() throws Exception {
        Path out = Path.of(System.getProperty("dev.out"));
        for (String nombre : CasoEnvio.todos()) {
            if (!nombre.startsWith("CONT-B-")) {
                continue;
            }
            String id = "B" + nombre.substring(7, 9);
            CasoEnvio caso = CasoEnvio.cargar(nombre);
            String servicio = caso.caso().get("servicio").getAsString();
            if (!"EnvioContabilidad".equals(servicio) && !"ReversaPreComprobante".equals(servicio)) {
                continue;
            }
            JsonObject salida = new JsonObject();
            JsonObject tablas;
            try {
                salida = "EnvioContabilidad".equals(servicio) ? envio(caso) : reversa(caso);
            } catch (Exception e) {
                salida.addProperty("excepcion", e.getClass().getSimpleName());
            }
            tablas = caso.tablasEnvio();
            if ("ReversaPreComprobante".equals(servicio)) {
                tablas.add("PreComprobante", caso.tabla("BSCL.Contabilidad.BD::ITZ.PreComprobante", Set.of(), "\"IdPreComprobante\""));
                tablas.add("PreComprobanteAsiento", caso.tabla("BSCL.Contabilidad.BD::ITZ.PreComprobanteAsiento", Set.of(), "\"IdPreComprobanteAsiento\""));
                tablas.add("EventoPreComprobanteAsiento", caso.tabla("BSCL.Contabilidad.BD::ITZ.EventoPreComprobanteAsiento", Set.of(),
                        "\"IdPreComprobanteAsiento\", \"IdEvento\""));
            }
            escribir(out.resolve("new").resolve(id), salida, tablas);

            JsonObject esp = caso.esperado();
            JsonObject legSalida = esp.has("salida") ? esp.getAsJsonObject("salida") : new JsonObject();
            if (esp.has("excepcion")) {
                legSalida.addProperty("excepcion", esp.get("excepcion").getAsString().split(":")[0]);
            }
            escribir(out.resolve("legacy").resolve(id), legSalida, esp.getAsJsonObject("tablas"));
            System.out.println("fresh driver wrote " + id + " (" + servicio + ")");
        }
    }

    static JsonObject envio(CasoEnvio caso) {
        PlanCuentasSapPort plan = caso.sinArchivoConfiguracion()
                ? Fakes.planCuentasSinConfiguracion() : Fakes.planCuentas(caso.planCuentas());
        return new EnvioContabilidadService(caso.jdbc(), plan, Fakes.plataforma(caso.config()), Fakes.diccionario())
                .execute(caso.entrada().deepCopy());
    }

    static JsonObject reversa(CasoEnvio caso) {
        EnvioContabilidadService envio = new EnvioContabilidadService(caso.jdbc(), Fakes.planCuentas(caso.planCuentas()),
                Fakes.plataforma(caso.config()), Fakes.diccionario());
        return new ReversaPreComprobanteService(caso.jdbc(), new Fakes.Secuencias(), Fakes.reloj(), Fakes.diccionario(), envio)
                .execute(caso.entrada().deepCopy());
    }

    static void escribir(Path dir, JsonObject salida, JsonObject tablas) throws Exception {
        Files.createDirectories(dir);
        Files.writeString(dir.resolve("salida.json"), JsonCanon.pretty(JsonCanon.canon(salida, SIN_ORDEN)) + "\n", StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder();
        List<String> nombres = new ArrayList<>(tablas.keySet());
        nombres.sort(String::compareTo);
        for (String t : nombres) {
            sb.append("## ").append(t).append('\n');
            List<String> filas = new ArrayList<>();
            JsonArray rows = tablas.getAsJsonArray(t);
            for (JsonElement r : rows) {
                filas.add(JsonCanon.canon(r, Set.of()).toString());
            }
            filas.sort(String::compareTo);
            filas.forEach(f -> sb.append(f).append('\n'));
        }
        Files.writeString(dir.resolve("tablas.txt"), sb.toString(), StandardCharsets.UTF_8);
    }
}
