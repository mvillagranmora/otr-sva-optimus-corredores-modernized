package cl.bcs.jobmanager.port.out;

import cl.bcs.jobmanager.domain.Agenda;

import java.util.List;
import java.util.Map;

/**
 * Output port for Agenda (scheduler diary) persistence operations.
 */
public interface AgendaPort {

    /**
     * Reads all enabled agenda entries.
     *
     * @return list of enabled agendas
     */
    List<Agenda> readEnabledAgendas();

    /**
     * Reads parameters for an agenda entry.
     *
     * @param idAgenda agenda identifier
     * @return parameter map (name -> value)
     */
    Map<String, String> readParams(long idAgenda);

    /**
     * Updates the next execution date for an agenda entry.
     * This is a HANA stored-procedure call — disabled in H2 tests.
     *
     * @param idAgenda agenda identifier
     * @param segundos next execution offset in seconds
     */
    void updateNextExecutionDate(long idAgenda, int segundos);

    /**
     * Adds a new Programa (program instance) for an agenda.
     * This is a HANA stored-procedure call — disabled in H2 tests.
     *
     * @param idAgenda    agenda identifier
     * @param idTipoTarea task type
     * @param date        scheduled date offset
     * @param parameters  job parameters
     * @return new program id, or -1 on failure
     */
    int addPrograma(int idAgenda, String idTipoTarea, int date, Map<String, String> parameters);

    /**
     * Runs first-execution correction (fixes stuck daemons on startup).
     * This is a HANA stored-procedure call — disabled in H2 tests.
     *
     * @return true if correction was successful
     */
    boolean firstExecution();

    /**
     * Gets the next scheduled execution date offset in seconds for an agenda.
     * This is a HANA stored-procedure call — disabled in H2 tests.
     *
     * @param idAgenda agenda identifier
     * @return seconds until next execution, or -1 if not applicable
     */
    int getDateForPrograma(long idAgenda);

    /**
     * Compares two date strings (yyyy-MM-dd HH:mm:ss) and returns true if
     * fecha1 is before or equal to fechaActual.
     *
     * @param fecha1      the "next execution" timestamp
     * @param fechaActual the "last execution" timestamp
     * @return true if the schedule should trigger
     */
    boolean compareDates(String fecha1, String fechaActual);
}
