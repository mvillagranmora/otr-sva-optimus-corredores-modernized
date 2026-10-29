package cl.bcs.apv.jobs.support;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import cl.bcs.apv.jobs.job.BcsJobCargaMasivaPrevired;
import cl.bcs.apv.jobs.job.IngresarOrdenBonificacionFiscal;
import cl.bcs.apv.jobs.job.IngresarOrdenFMBonificacionFiscal;
import cl.bcs.apv.jobs.job.IngresarOrdenRVBonificacionFiscal;
import cl.bcs.apv.jobs.runtime.ApvJob;
import cl.bcs.apv.jobs.runtime.JobResultado;
import cl.bcs.apv.jobs.runtime.JobResultado.Desenlace;
import cl.bcs.apv.jobs.runtime.JobRunner;

/**
 * Runs a case through the NEW code: the job built with the case doubles, executed by {@link JobRunner} on the case H2 (one transaction per run), and
 * describes the run in the same shape the oracle recorded ({@code resultado}, tables, collaborator trace, files).
 */
public final class EjecucionJob {

    /** What a run produced, in the shape of expected.json. */
    public record Corrida(JsonObject resultado, JsonObject tablas, List<JsonObject> colaboradores, JsonObject archivos, boolean marcaDeTiempoDelReloj) {
    }

    private EjecucionJob() {
    }

    public static Corrida ejecutar(CasoJob caso) {
        ColaboradoresDeCaso colab = new ColaboradoresDeCaso(caso);
        DoblesJob.CargaPreviredDeCaso carga = new DoblesJob.CargaPreviredDeCaso(caso);
        ApvJob job = crear(caso, colab, carga);
        List<JsonObject> estados = new ArrayList<>();
        JobRunner runner = new JobRunner(caso.transactionManager(), caso.jdbcTemplate(), List.of(job), (id, error, mensaje) -> {
            JsonObject o = new JsonObject();
            o.addProperty("error", error);
            if (caso.pinMensaje()) {
                o.addProperty("mensaje", limpiar(mensaje, caso));
            }
            estados.add(o);
        });
        JobResultado r = runner.ejecutar(job.clase(), caso.idPrograma(), caso.parametros());

        JsonObject res = new JsonObject();
        res.addProperty("desenlace", r.desenlace().name());
        List<String> eventos = new ArrayList<>(caso.transactionManager().eventos());
        boolean esException = r.error() == null || r.error() instanceof Exception;
        if (r.desenlace() == Desenlace.ERROR && !esException && !eventos.isEmpty() && eventos.get(eventos.size() - 1).equals("rollback")) {
            // legacy: an Error is not caught by BcsJob.run's rollback block (the connection is only closed); JobRunner rolls back explicitly
            eventos.remove(eventos.size() - 1);
        }
        JsonArray ev = new JsonArray();
        eventos.forEach(ev::add);
        res.add("eventos", ev);
        JsonArray es = new JsonArray();
        estados.forEach(es::add);
        res.add("estados", es);
        switch (r.desenlace()) {
            case COMMIT -> res.addProperty("retorno", true);
            case ROLLBACK -> res.addProperty("retorno", false);
            default -> res.add("retorno", JsonNull.INSTANCE);
        }
        if (r.error() != null) {
            res.addProperty("excepcion", r.error().getClass().getSimpleName());
            res.addProperty("esException", esException);
            if (caso.pinMensaje()) {
                res.addProperty("excepcionMensaje", limpiar(r.error().getMessage(), caso));
            }
        }
        List<JsonObject> traza = new ArrayList<>(carga.traza());
        traza.addAll(colab.traza());
        return archivos(caso, res, traza);
    }

    private static Corrida archivos(CasoJob caso, JsonObject res, List<JsonObject> traza) {
        JsonObject archivos = new JsonObject();
        boolean[] marca = {true};
        Path base = caso.baseDir();
        try (var paths = Files.walk(base)) {
            for (Path f : paths.filter(Files::isRegularFile).sorted().toList()) {
                String rel = base.relativize(f).toString().replace('\\', '/');
                Matcher m = Dump.MARCA_14.matcher(rel);
                if (m.find()) {
                    marca[0] &= m.group().equals(DoblesJob.MARCA);
                }
                archivos.addProperty(Dump.enmascararInstante(rel), limpiar(Files.readString(f, StandardCharsets.UTF_8), caso));
            }
        } catch (java.io.IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
        return new Corrida(res, caso.tablas(), traza, archivos, marca[0]);
    }

    static String limpiar(String s, CasoJob caso) {
        if (s == null) {
            return null;
        }
        String base = caso.baseDir().toAbsolutePath().toString();
        String r = s.replace(base.replace('\\', '/'), "<BASE>").replace(base, "<BASE>");
        return Dump.NPE_MSG.matcher(r).replaceAll(Dump.NPE_TOKEN);
    }

    private static ApvJob crear(CasoJob caso, ColaboradoresDeCaso colab, DoblesJob.CargaPreviredDeCaso carga) {
        return switch (caso.job()) {
            case "BcsJobCargaMasivaPrevired" -> new BcsJobCargaMasivaPrevired(carga, DoblesJob.configuracion(caso), DoblesJob.reloj());
            case "IngresarOrdenBonificacionFiscal" -> new IngresarOrdenBonificacionFiscal(caso.jdbc(), colab);
            case "IngresarOrdenFMBonificacionFiscal" -> new IngresarOrdenFMBonificacionFiscal(caso.jdbc(), DoblesJob.configuracion(caso), colab);
            case "IngresarOrdenRVBonificacionFiscal" -> new IngresarOrdenRVBonificacionFiscal(caso.jdbc(), DoblesJob.configuracion(caso), colab);
            default -> throw new AssertionError("unknown job in case.json: " + caso.job());
        };
    }
}
