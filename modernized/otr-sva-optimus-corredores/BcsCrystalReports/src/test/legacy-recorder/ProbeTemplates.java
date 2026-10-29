import com.crystaldecisions.sdk.occa.report.application.ReportClientDocument;
import com.crystaldecisions.sdk.occa.report.data.*;
import com.crystaldecisions.sdk.occa.report.definition.*;
import com.crystaldecisions.sdk.occa.report.lib.ReportObjectKind;
import com.google.gson.*;

import java.nio.file.*;
import java.util.*;

/**
 * Dumps, for every .rpt given (args: outJson rpt...), what the Crystal API says about it: tables (alias, fields:type),
 * subreports (with tables), parameter fields, picture objects, sections count. Used to DEDUCE the data shape of a case
 * when the sibling .sql does not map clearly. Opens the template only (no data, no connection).
 */
public class ProbeTemplates {
    public static void main(String[] a) throws Exception {
        JsonArray all = new JsonArray();
        for (int i = 1; i < a.length; i++) {
            JsonObject o = new JsonObject();
            o.addProperty("rpt", a[i].replace('\\', '/'));
            try {
                probe(a[i], o);
            } catch (Throwable t) {
                o.addProperty("error", String.valueOf(t));
            }
            all.add(o);
        }
        Files.write(Paths.get(a[0]), new GsonBuilder().setPrettyPrinting().create().toJson(all).getBytes("UTF-8"));
        System.exit(0);
    }

    static void probe(String file, JsonObject o) throws Exception {
        ReportClientDocument d = new ReportClientDocument();
        d.setLocale(Locale.ENGLISH);
        d.open(file, 0);
        o.add("tables", tables(d.getDatabaseController().getDatabase().getTables()));
        JsonObject subs = new JsonObject();
        for (String s : d.getSubreportController().getSubreportNames()) {
            subs.add(s, tables(d.getSubreportController().getSubreportDatabase(s).getTables()));
        }
        o.add("subreports", subs);
        JsonArray params = new JsonArray();
        var pf = d.getDataDefinition().getParameterFields();
        for (int i = 0; i < pf.size(); i++) {
            params.add(((IField) pf.get(i)).getName());
        }
        o.add("parameters", params);
        JsonObject formulas = new JsonObject();
        var ff = d.getDataDefinition().getFormulaFields();
        for (int i = 0; i < ff.size(); i++) {
            IFormulaField f = (IFormulaField) ff.get(i);
            formulas.addProperty(f.getName(), f.getText());
        }
        o.add("formulas", formulas);
        o.addProperty("recordFilter", String.valueOf(d.getDataDefinition().getRecordFilter().getFreeEditingText()));
        JsonArray pics = new JsonArray();
        var ros = d.getReportDefController().getReportObjectController().getReportObjectsByKind(ReportObjectKind.picture);
        for (int i = 0; i < ros.size(); i++) {
            pics.add(((IReportObject) ros.get(i)).getName());
        }
        o.add("pictures", pics);
        d.close();
    }

    static JsonArray tables(Tables ts) {
        JsonArray arr = new JsonArray();
        for (int i = 0; i < ts.size(); i++) {
            ITable t = ts.getTable(i);
            JsonObject to = new JsonObject();
            to.addProperty("alias", t.getAlias());
            to.addProperty("kind", t.getClass().getSimpleName());
            if (t instanceof ICommandTable) {
                to.addProperty("command", ((ICommandTable) t).getCommandText());
            }
            JsonObject fs = new JsonObject();
            for (int j = 0; j < t.getDataFields().size(); j++) {
                IField f = t.getDataFields().getField(j);
                fs.addProperty(f.getName(), String.valueOf(f.getType()));
            }
            to.add("fields", fs);
            arr.add(to);
        }
        return arr;
    }
}
