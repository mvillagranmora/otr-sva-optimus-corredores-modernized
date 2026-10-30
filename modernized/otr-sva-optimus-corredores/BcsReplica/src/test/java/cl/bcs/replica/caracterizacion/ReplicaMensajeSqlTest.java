package cl.bcs.replica.caracterizacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

/**
 * Pins the FULL text of the SQLException that the batch layer raises when an INSERT/UPSERT fails (the equivalence test only compares the class). Legacy cl.bcs.utils.SQL.rethrow builds
 * "<cause> Query: <sql> Parameters: " + Arrays.deepToString(rows), and every row is a POJO whose toString() is Gson JSON (serializeNulls, fields in POJO order, timestamps as
 * yyyy-MM-dd HH:mm:ss.SSS): {"Key":"","Table":"CARTERARV",...,"Stamp":null,...}. The expected tails below are the literal text recorded in the golden of the legacy.
 */
class ReplicaMensajeSqlTest {

    private static final String PARAMETERS = " Parameters: ";

    private static String cola(String texto) {
        int i = texto.lastIndexOf(PARAMETERS);
        assertTrue(i >= 0, "the exception text has no Parameters part: " + texto);
        return texto.substring(i + PARAMETERS.length());
    }

    private static void fija(String id, String esperadoLiteral) throws Exception {
        JsonObject golden = CasoReplica.golden(CasoReplica.id(id));
        JsonObject obtenido = CasoReplica.corrida(CasoReplica.id(id));
        assertTrue(obtenido.has("excepcion"), id + ": the unit must throw");
        assertEquals(esperadoLiteral, cola(golden.get("excepcion").getAsString()), id + ": literal expected vs legacy golden");
        assertEquals(esperadoLiteral, cola(obtenido.get("excepcion").getAsString()), id + ": Parameters text of the modernized exception");
        assertEquals(golden.get("excepcion").getAsString(), obtenido.get("excepcion").getAsString(), id + ": whole exception message");
    }

    @Test
    @DisplayName("Parameters of a failed upsert: null column is serialized as null, fields in POJO order (REP-POS-005)")
    void upsertConNulo() throws Exception {
        fija("REP-POS-005", "[{\"FechaSistema\":\"2026-03-06\",\"Tabla\":null,\"Fecha\":\"2026-03-09\"}]");
    }

    @Test
    @DisplayName("Parameters of a failed upsert: text values with a time part stay strings (REP-OPT-020)")
    void upsertConHora() throws Exception {
        fija("REP-OPT-020", "[{\"FechaSistema\":\"2026-03-06 10:00:00\",\"Tabla\":\"Custodia_CustodiaFechaOperacionHis\",\"Fecha\":\"2026-03-06 10:00:00\"}]");
    }

    @Test
    @DisplayName("Parameters of a failed workstack insert in IniciarReplicaFull: Long id as number, Timestamp as yyyy-MM-dd HH:mm:ss.SSS (REP-INI-013)")
    void workstackConIdYTimestamp() throws Exception {
        fija("REP-INI-013", "[{\"IdStack\":41,\"Key\":\"-1\",\"Table\":\"INICIAR_REPLICA_PAUTA\",\"Action\":\"F\",\"Status\":0,\"Stamp\":\"2026-03-02 14:30:00.000\",\"Parameters\":\"BCS_query:QueryReplica;Fecha:2026-03-04\"}]");
    }

    @Test
    @DisplayName("Parameters of a failed workstack insert in IniciarReplicaDelta (REP-INI-038)")
    void workstackDelta() throws Exception {
        fija("REP-INI-038", "[{\"IdStack\":41,\"Key\":\"-1\",\"Table\":\"INICIAR_REPLICA_PAUTA\",\"Action\":\"F\",\"Status\":0,\"Stamp\":\"2026-03-02 14:30:00.000\",\"Parameters\":\"BCS_query:QueryReplicaDelta;Fecha:2026-03-04\"}]");
    }

    @Test
    @DisplayName("Parameters of a failed workstack insert from EnviarTablaReplica: Status as 0, Stamp null, Key empty (REP-ENV-061)")
    void workstackEnvioConStampNulo() throws Exception {
        fija("REP-ENV-061", "[{\"Key\":\"\",\"Table\":\"CARTERARV\",\"Action\":\"M\",\"Status\":0,\"Stamp\":null,\"Parameters\":\"tables:Custodia_CarteraRV;BCS_query:QueryReplica;Fecha:2026-03-04 ;serialDelete:false;truncarDestino:true;serialLaunch:false\"}]");
    }

    @Test
    @DisplayName("Parameters of a failed workstack insert at FIN_PAUTA (REP-ENV-098)")
    void workstackFinPauta() throws Exception {
        fija("REP-ENV-098", "[{\"Key\":\"\",\"Table\":\"CUSTODIA DIA SIGUIENTE\",\"Action\":\"M\",\"Status\":0,\"Stamp\":null,\"Parameters\":\"tables:Custodia_CustodiaFechaLiquidacion,Custodia_CustodiaFechaOperacion;BCS_query:QueryReplicaDelta;Fecha:2026-03-06;serialDelete:false;truncarDestino:false;serialLaunch:false\"}]");
    }

    @Test
    @DisplayName("Parameters of a failed day-off copy: FechaOrigen / FechaDestino (REP-ENV-021)")
    void copiaDiaInhabil() throws Exception {
        fija("REP-ENV-021", "[{\"FechaOrigen\":\"2026-03-06\",\"FechaDestino\":\"2026-03-07\"}]");
    }

    @Test
    @DisplayName("every recorded exception that carries Parameters has the same whole message in the modernized run (all 25 cases)")
    void todasLasExcepcionesConParameters() throws Exception {
        List<String> distintos = new ArrayList<>();
        int comparados = 0;
        for (String id : CasoReplica.casos()) {
            JsonObject golden = CasoReplica.golden(id);
            if (!golden.has("excepcion") || !golden.get("excepcion").getAsString().contains(PARAMETERS)) continue;
            JsonObject obtenido = CasoReplica.corrida(id);
            comparados++;
            if (!obtenido.has("excepcion") || !golden.get("excepcion").getAsString().equals(obtenido.get("excepcion").getAsString())) distintos.add(id);
        }
        assertTrue(comparados >= 25, "cases with Parameters in the golden: " + comparados);
        assertTrue(distintos.isEmpty(), "different exception text: " + distintos);
    }
}
