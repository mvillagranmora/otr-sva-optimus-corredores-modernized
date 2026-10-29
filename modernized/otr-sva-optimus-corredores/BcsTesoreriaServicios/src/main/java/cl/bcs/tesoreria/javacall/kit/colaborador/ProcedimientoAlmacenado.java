package cl.bcs.tesoreria.javacall.kit.colaborador;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonObject;

import cl.bcs.tesoreria.javacall.kit.JsonLegacy;
import cl.bcs.tesoreria.port.out.Colaboradores;

/**
 * HANA stored procedures ({@code CALL "BSCL...::Proc"(...)}, legacy {@code SQL.call}) cannot run on the test database, so they go through the
 * {@link Colaboradores} port under the service name {@code StoredProcedure} with {@code {procedimiento, entrada}} (scalar inputs, keys sorted).
 * The production implementation of that service name (executing the CALL on the connection) belongs to the Colaboradores adapter: DECISION NEEDED
 * (see the family-B report); the procedures' SQLScript is not in the repository.
 */
public final class ProcedimientoAlmacenado {

    private static final Pattern NOMBRE = Pattern.compile("(?i)call\s+\"([^\"]+)\"");

    private ProcedimientoAlmacenado() {
    }

    public static void llamar(Colaboradores colaboradores, String sqlCall, Map<String, Object> entrada) {
        Matcher m = NOMBRE.matcher(sqlCall);
        Map<String, Object> reg = new LinkedHashMap<>();
        reg.put("procedimiento", m.find() ? m.group(1) : sqlCall);
        reg.put("entrada", new TreeMap<>(entrada));
        JsonObject json = JsonLegacy.aJson(reg);
        colaboradores.invocar("StoredProcedure", json);
    }
}
