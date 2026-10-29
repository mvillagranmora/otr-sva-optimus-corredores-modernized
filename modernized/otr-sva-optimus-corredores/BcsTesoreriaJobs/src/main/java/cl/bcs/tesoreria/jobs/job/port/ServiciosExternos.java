package cl.bcs.tesoreria.jobs.job.port;

/**
 * Names (legacy class simple names) the jobs pass to {@code cl.bcs.tesoreria.port.out.Colaboradores.invocar(servicio,
 * entrada)} for the legacy services that live OUTSIDE BcsTesoreriaServicios (Plataforma, APV). The {@code entrada} is the
 * legacy input object serialised with the legacy Gson (the same JSON the golden "colaboradores" trace holds). The
 * production {@code ColaboradoresJavaCall} does not route the first four: {@code adapter.ColaboradoresJobs} does (platform JavaCall router).
 */
public final class ServiciosExternos {

    /** {@code cl.bcs.APV.ValidarMandatosPAC.logica(con, salida, idEmpresa, idUsuario)}: entrada {@code {salida, idEmpresa, idUsuario}}. */
    public static final String VALIDAR_MANDATOS_PAC = "ValidarMandatosPAC";

    /** {@code cl.bcs.plataforma.pla.ValidaEjecucionProcesoRecPAC.logica}: entrada {@code {p_IdEmpresa, p_ValorNuevo}}. */
    public static final String VALIDA_EJECUCION_PROCESO_REC_PAC = "ValidaEjecucionProcesoRecPAC";

    /** {@code cl.bcs.plataforma.cre.ConsultarEstadoModulo.logica}: entrada {@code {p_IdEmpresa, p_IdModulo, p_IdUsuario}}. */
    public static final String CONSULTAR_ESTADO_MODULO = "ConsultarEstadoModulo";

    /** {@code cl.bcs.plataforma.pla.ObtenerDiaAnterior.logica}: entrada {@code {idPais, diaHabil, excluirConsultarDiaFeriado, fechas:[{fechaInicial, numeroDias}]}}. */
    public static final String OBTENER_DIA_ANTERIOR = "ObtenerDiaAnterior";

    /** {@code cl.bcs.plataforma.pla.AgregarLog.logica}: entrada {@code InAgregarLog}. */
    public static final String AGREGAR_LOG = "AgregarLog";

    private ServiciosExternos() {
    }
}
