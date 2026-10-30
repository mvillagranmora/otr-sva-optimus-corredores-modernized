package cl.bcs.plataforma.pla.utils;

import java.sql.Connection;

import cl.bcs.oracle.Doubles;

/** ORACLE DOUBLE of the legacy cl.bcs.plataforma.pla.utils.Feriados.esDiaHabil (not part of the Replica module): the answer is scripted by the case. */
public class Feriados {
    public static boolean esDiaHabil(Connection con, String fechaConsulta, Long idPais) throws Exception {
        return Doubles.answer("Feriados", "esDiaHabil", new Object[] { fechaConsulta, idPais }).getAsBoolean();
    }
}
