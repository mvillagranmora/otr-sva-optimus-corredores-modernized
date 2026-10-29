package cl.bcs.tesoreria.jobs.programa;

/**
 * The SQL of job pattern H4. Only EXISTING objects and columns of {@code BAT} (no database change). Names are qualified with the
 * schema {@code BSCL} because the repository uses its own connection, which does not get the {@code SET SCHEMA BSCL} of the job
 * transaction (BcsJob.java:130-132). Statements are portable between HANA and H2 on purpose: the HANA-only constructs of the legacy
 * are written in an equivalent portable form (see each constant), except {@code CURRENT_UTCTIMESTAMP}, which the H2 tests translate.
 */
final class ProgramaSql {

    private ProgramaSql() {
    }

    static final String PROGRAMA = "BSCL.\"BSCL.Plataforma.BD::BAT.Programa\"";
    static final String TIPO = "BSCL.\"BSCL.Plataforma.BD::BAT.TipoPrograma\"";
    static final String AGENDA = "BSCL.\"BSCL.Plataforma.BD::BAT.Agenda\"";
    static final String VISTA_PARAMETROS = "BSCL.\"BSCL.Plataforma.BD.Vistas::BAT.ConsultarParametrosProgramas\"";

    /** Legacy readProgramm columns (TransactionManager.java:41-47) for ONE program, LEFT JOIN so a type without row is reported, not hidden. */
    static final String CARGAR = "SELECT P.\"IdPrograma\", P.\"idEstado\", P.\"IdTipoPrograma\", P.\"Intento\", P.\"Bloqueo\", P.\"Nodo\", "
            + "P.\"IdAgenda\", TP.\"NombreClasePrograma\", TP.\"MaximoIntentos\" FROM " + PROGRAMA + " P LEFT JOIN " + TIPO
            + " TP ON P.\"IdTipoPrograma\" = TP.\"IdTipoPrograma\" WHERE P.\"IdPrograma\" = ?";

    /** TransactionManager.java:476 verbatim (view and columns), no ORDER BY as the legacy. */
    static final String PARAMETROS = "SELECT \"NombreParametro\", \"Valor\", \"ValorBlob\" FROM " + VISTA_PARAMETROS
            + " WHERE \"IdPrograma\" = ?";

    /**
     * The claim: legacy updateIntent (Intento = observed + 1, :281-300) + updateProgramm(id, 0, "Programa Encolado") (:221-224:
     * ASI, fechaFin 0001-01-01 as a SQL literal, so no driver calendar conversion, Resultado) + the Bloqueo 'S' of :68-73, in ONE statement,
     * conditioned on the row still being as it was read (Bloqueo 'N', PEN, same Intento). Parameters: Intento+1, idPrograma, Intento read.
     */
    static final String RECLAMAR = "UPDATE " + PROGRAMA + " SET \"Bloqueo\" = 'S', \"idEstado\" = 'ASI', \"Intento\" = ?, "
            + "\"Resultado\" = '" + ProgramaRepositorio.RESULTADO_ENCOLADO + "', \"fechaFin\" = TIMESTAMP '0001-01-01 00:00:00' "
            + "WHERE \"IdPrograma\" = ? AND \"Bloqueo\" = 'N' AND \"idEstado\" = 'PEN' AND \"Intento\" = ?";

    /** TransactionManager.java:226-229 (TER / ERR). Parameters: idEstado, Resultado, idPrograma. */
    static final String ESTADO_FINAL = "UPDATE " + PROGRAMA + " SET \"idEstado\" = ?, \"fechaFin\" = CURRENT_UTCTIMESTAMP, "
            + "\"Resultado\" = ? WHERE \"IdPrograma\" = ?";

    /**
     * TransactionManager.java:240-250 ({@code update ag ... from Programa pg INNER JOIN Agenda ag}, HANA-only) as a portable
     * sub-select; same rows. Parameter: idPrograma.
     */
    static final String AGENDA_ULTIMA = "UPDATE " + AGENDA + " SET \"UltimaFechaDeEjecucion\" = CURRENT_UTCTIMESTAMP "
            + "WHERE \"IdAgenda\" IN (SELECT \"IdAgenda\" FROM " + PROGRAMA + " WHERE \"IdPrograma\" = ?)";

    /** State "requires review". Parameters: Intento (= MaximoIntentos), Resultado, idPrograma. */
    static final String REVISION = "UPDATE " + PROGRAMA + " SET \"idEstado\" = 'ERR', \"Intento\" = ?, \"Bloqueo\" = 'S', "
            + "\"fechaFin\" = CURRENT_UTCTIMESTAMP, \"Resultado\" = ? WHERE \"IdPrograma\" = ?";
}
