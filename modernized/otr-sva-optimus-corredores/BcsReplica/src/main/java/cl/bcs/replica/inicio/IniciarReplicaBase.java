package cl.bcs.replica.inicio;

import java.sql.Connection;
import java.sql.Timestamp;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import cl.bcs.replica.kit.Sql;
import cl.bcs.replica.port.out.FechaSistemaPort;
import cl.bcs.replica.port.out.SecuenciaPort;

abstract class IniciarReplicaBase {
    static class Entrada {
        Long p_IdEmpresa;
        Long p_IdUsuario;
    }

    private final SecuenciaPort secuencias;
    private final FechaSistemaPort fechas;
    private final Clock reloj;
    private final String consulta;

    IniciarReplicaBase(SecuenciaPort secuencias, FechaSistemaPort fechas, Clock reloj, String consulta) {
        this.secuencias = secuencias;
        this.fechas = fechas;
        this.reloj = reloj;
        this.consulta = consulta;
    }

    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        Entrada e = new Gson().fromJson(in.toString(), Entrada.class);
        JsonObject out = logica(conn, e.p_IdEmpresa, e.p_IdUsuario);
        long errores = 0;
        for (var m : out.getAsJsonArray("P_OUT_MENSAJES")) {
            if ("ERROR".equals(m.getAsJsonObject().get("tipo").getAsString())) errores++;
        }
        out.addProperty("P_OUT_ERRORESCONTADOR", errores);
        return out;
    }

    public JsonObject logica(Connection conn, Long idEmpresa, Long idUsuario) throws Exception {
        Long idStack = secuencias.siguientes(conn, "BSCL.Plataforma.BD.Secuencias::WorkStack", 1L).get(0);
        Timestamp stamp = new Timestamp(reloj.millis());
        String parametros = "BCS_query:" + consulta + ";Fecha:" + fechas.fechaSistema(conn, "BSCL", idEmpresa);
        List<Map<String, Object>> filas = new ArrayList<>();
        filas.add(Sql.fila("IdStack", idStack, "Key", "-1", "Table", "INICIAR_REPLICA_PAUTA", "Action", "F", "Status", 0, "Stamp", stamp, "Parameters", parametros));
        Sql.batch(conn, Sql.recurso("IniciarReplica_InsertWorkstack.sql"), filas);
        JsonObject m = new JsonObject();
        m.addProperty("id", 0L);
        m.addProperty("tipo", "OK");
        m.addProperty("codigo", "INICIAR_REPLICA_PAUTA");
        m.addProperty("descripcion", "Se inicia Replica desde Pauta");
        JsonArray mensajes = new JsonArray();
        mensajes.add(m);
        JsonObject salida = new JsonObject();
        salida.add("P_OUT_MENSAJES", mensajes);
        salida.addProperty("P_OUT_ERRORESCONTADOR", 0L);
        return salida;
    }
}
