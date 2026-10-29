package cl.bcs.cuentainversion.jobs.guarda;

/**
 * Port to the guard marks, stored in the EXISTING table {@code BAT.LogPrograma} (no database change). Like
 * {@code ProgramaRepositorio}, every write COMMITS BY ITSELF in its own connection, never in the transaction of the job: a mark must
 * survive a rollback of the job (it records a TomEE write the rollback cannot undo) and must be visible before the HTTP call goes out.
 */
public interface MarcasRepositorio {

    /**
     * Inserts the mark and commits it. A failure is a {@link GuardaTomeeException}: the caller must not make the TomEE call it was about to
     * announce.
     *
     * @param idTipoPrograma {@code BAT.Programa.IdTipoPrograma} of the program (NVARCHAR(100) in {@code LogPrograma}; null is stored as empty)
     */
    void registrar(MarcaGuarda marca, String idTipoPrograma);

    /** The marks of the program over all its attempts (see {@link EstadoMarcas}); a database error is a {@link GuardaTomeeException}. */
    EstadoMarcas estado(long idPrograma);
}
