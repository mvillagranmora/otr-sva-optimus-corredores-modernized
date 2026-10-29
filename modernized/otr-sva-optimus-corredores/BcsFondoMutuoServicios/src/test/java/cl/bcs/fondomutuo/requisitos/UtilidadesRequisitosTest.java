package cl.bcs.fondomutuo.requisitos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import cl.bcs.fondomutuo.kit.MensajeSalida;

/** The small legacy helpers that the 3B classes share (counter by reference, List2Hash, Fecha.toDate, the named-parameter reader of the batch). */
class UtilidadesRequisitosTest {

    @Test
    @DisplayName("D-REQ-11: the error counter compares the type BY REFERENCE (a text that is equal but not the constant is not counted)")
    void conteoDeErroresPorReferencia() {
        List<MensajeSalida> m = new ArrayList<>();
        m.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR, "A", "a"));
        m.add(new MensajeSalida(2L, MensajeSalida.MENSAJE_ADVERTENCIA, "B", "b"));
        m.add(new MensajeSalida(3L, new String("ERROR"), "C", "c"));   // e.g. a message read back from JSON
        m.add(new MensajeSalida(4L, MensajeSalida.MENSAJE_ERROR, "D", "d"));
        assertEquals(2L, ConteoErrores.errores(m));
        assertEquals(0L, ConteoErrores.errores(new ArrayList<>()));
    }

    @Test
    @DisplayName("Utils.List2Hash: unique keys map, a repeated key or a null list raise the legacy Exception, an empty list is an empty map")
    void hashes() throws Exception {
        assertEquals(0, Hashes.aHash(new ArrayList<String>(), s -> s).size());
        assertEquals("a", Hashes.aHash(List.of("a", "bb"), String::trim).get("a"));
        Exception repetida = assertThrows(Exception.class, () -> Hashes.aHash(List.of("a", "a"), s -> s));
        assertEquals("key field is not unique", repetida.getMessage());
        Exception nula = assertThrows(Exception.class, () -> Hashes.<String, String>aHash(null, s -> s));
        assertEquals("List must be not null", nula.getMessage());
    }

    @Test
    @DisplayName("Fecha.toDate: 19 characters are a date-time (its date), otherwise yyyy-MM-dd")
    void fechaToDate() {
        assertEquals(java.sql.Date.valueOf("2026-03-02"), FechasLegacy.toDate("2026-03-02"));
        assertEquals(java.sql.Date.valueOf("2026-03-02"), FechasLegacy.toDate("2026-03-02 23:59:59"));
        assertThrows(java.time.format.DateTimeParseException.class, () -> FechasLegacy.toDate("02/03/2026"));
    }

    @Test
    @DisplayName("SQL.batch: parameters are the :names outside quotes and comments, in order of appearance, without duplicates")
    void nombresDeParametros() {
        String sql = "INSERT INTO \"A::B\" (\"x\", \"y\") VALUES (:Uno, ':no'); -- :comentado\n/* :otro */ SELECT :Dos, :Uno, \"S::T\".NEXTVAL";
        assertEquals(Set.of("Uno", "Dos"), SqlLote.nombres(sql));
        assertEquals(List.of("Uno", "Dos"), new ArrayList<>(SqlLote.nombres(sql)));
        assertEquals(0, SqlLote.nombres("SELECT 1").size());
    }

}
