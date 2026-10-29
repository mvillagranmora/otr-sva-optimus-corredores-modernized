package cl.bcs.contabilidad.jobs.runtime;

import java.util.Optional;

/**
 * What a legacy {@code BcsJob} reached through {@code this} and its connection during one run (CONT-J-05..08).
 *
 * <p>Legacy: {@code BcsJob.getJobId()} (:163), {@code getParam} (:71-73), {@code getOptionalParam} (:75-77),
 * {@code getParamInteger} (:54-61), {@code con.commit()} / {@code con.rollback()} called by the jobs themselves, and
 * {@code updateRutaSalida(con, ruta, idPrograma)} (:171-185).
 */
public interface JobContext {

    /** Legacy {@code getJobId()}: the {@code BAT.Programa} IdPrograma of this run. */
    long idPrograma();

    /** The raw parameter value, or {@code null} when absent. Names are case-sensitive. */
    String getParam(String nombre);

    /** {@code Optional.ofNullable(getParam(nombre))}: an empty string is present. */
    Optional<String> getOptionalParam(String nombre);

    /** The parameter as an {@code Integer}; {@code null} when absent or not a number. */
    Integer getParamInteger(String nombre);

    /** Legacy {@code con.commit()} inside a job: commits the work so far; the job continues in a new transaction. */
    void commit();

    /** Legacy {@code con.rollback()} inside a job: discards the work since the last commit; the job continues. */
    void rollback();

    /** Legacy {@code BcsJob.updateRutaSalida}: sets {@code BAT.Programa.RutaSalida}, inside the job transaction. */
    void updateRutaSalida(String rutaSalida, Long idPrograma);
}
