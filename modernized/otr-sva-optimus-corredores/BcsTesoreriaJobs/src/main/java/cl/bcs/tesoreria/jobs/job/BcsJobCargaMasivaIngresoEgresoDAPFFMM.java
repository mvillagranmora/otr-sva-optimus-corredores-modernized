package cl.bcs.tesoreria.jobs.job;

import cl.bcs.tesoreria.javacall.CargaMasivaIngresoEgresoDAPFFMMService;
import cl.bcs.tesoreria.javacall.CargaMasivaIngresoEgresoDAPFFMMService.EntradaCargaMasivaIngresoEgresoDAPFFMM;
import cl.bcs.tesoreria.javacall.CargaMasivaIngresoEgresoDAPFFMMService.SalidaCargaMasivaIngresoEgresoDAPFFMM;
import cl.bcs.tesoreria.javacall.kit.JsonLegacy;
import cl.bcs.tesoreria.javacall.kit.MensajeSalida;
import cl.bcs.tesoreria.javacall.kit.SqlNombrado;
import cl.bcs.tesoreria.jobs.job.port.CabecerasCarga;
import cl.bcs.tesoreria.jobs.runtime.JobContext;
import cl.bcs.tesoreria.jobs.runtime.TesoreriaJob;
import cl.bcs.tesoreria.port.out.Colaboradores;
import cl.bcs.tesoreria.port.out.ConfiguracionSistema;
import cl.bcs.tesoreria.port.out.DiccionarioMensajes;
import cl.bcs.tesoreria.port.out.Reloj;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Legacy {@code cl.bcs.tesoreria.cargaMasivaIngresoEgresoDAPFFMM.BcsJobCargaMasivaIngresoEgresoDAPFFMM} (447 lines): the
 * scheduled DAP / FFMM income-expense mass load. Six guards (module closed, file already loaded, no file to load, previous
 * business day without PAU.Ejecucion, no BAT.RangoHorario, outside the time window) each write a one-line CSV
 * {@code CDB_Errores_IngresoEgreso_<tipo>_<yyyyMMdd>_<HHmmss>.csv} below
 * {@code PLA.RutaBaseArchivos + TES.RutaSalidaArchivosCargaMasiva}, set {@code BAT.Programa.RutaSalida} and return true;
 * otherwise the proven {@link CargaMasivaIngresoEgresoDAPFFMMService} runs and its outcome goes to the run output and to an
 * AgregarLog record. Rules: RULE-076 (the debug {@code logica(con, true)} and {@code main()} are not ported).
 *
 * <p>Legacy defects kept bug for bug (README): TES-J-DEF-01 (the window excludes both ends), 16 (missing folder: empty
 * RutaSalida), 17 (CURRENT_DATE of the database against the local time; a null type returns false, reported as success).
 */
public final class BcsJobCargaMasivaIngresoEgresoDAPFFMM implements TesoreriaJob {

    public static final String CLASE =
            "cl.bcs.tesoreria.cargaMasivaIngresoEgresoDAPFFMM.BcsJobCargaMasivaIngresoEgresoDAPFFMM";

    private static final Logger logger = LoggerFactory.getLogger(BcsJobCargaMasivaIngresoEgresoDAPFFMM.class);
    private static final String ID_TIPO_PROGRAMA = "CARGA_MASIVA_INGRESO_EGRESO_DAP_FFMM";
    private static final String MODULO_ESTADO_CERRADO = "CER";
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    private static final String SQL_RANGO_HORARIO = SqlNombrado.recurso("jobs/BcsJobCargaMasivaIngresoEgresoDAPFFMM_SelectRangoHorario");
    private static final String SQL_FECHA_ACTUAL = SqlNombrado.recurso("jobs/BcsJobCargaMasivaIngresoEgresoDAPFFMM_ObtenerFechaActual");
    private static final String SQL_ESTADO_PAUTA = SqlNombrado.recurso("jobs/BcsJobCargaMasivaIngresoEgresoDAPFFMM_ObtenerEstadoEjecucionPauta");

    /** Legacy :59-71 row types. */
    public static final class RangoHorario {
        public String HoraInicio;
        public String HoraFin;
    }

    public static final class FechaActual {
        public String Fecha;
    }

    public static final class EstadoPauta {
        public String EstadoPauta = "PEN";
    }

    private final CargaMasivaIngresoEgresoDAPFFMMService carga;
    private final CabecerasCarga cabeceras;
    private final Colaboradores colaboradores;
    private final ConfiguracionSistema configuracion;
    private final DiccionarioMensajes diccionario;
    private final Reloj reloj;
    private final SqlNombrado sql;
    private final CargaMasivaSalida salidas;

    public BcsJobCargaMasivaIngresoEgresoDAPFFMM(CargaMasivaIngresoEgresoDAPFFMMService carga, CabecerasCarga cabeceras,
                                                 Colaboradores colaboradores, ConfiguracionSistema configuracion,
                                                 DiccionarioMensajes diccionario, Reloj reloj, JdbcTemplate jdbc) {
        this.carga = Objects.requireNonNull(carga, "carga");
        this.cabeceras = Objects.requireNonNull(cabeceras, "cabeceras");
        this.colaboradores = Objects.requireNonNull(colaboradores, "colaboradores");
        this.configuracion = Objects.requireNonNull(configuracion, "configuracion");
        this.diccionario = Objects.requireNonNull(diccionario, "diccionario");
        this.reloj = Objects.requireNonNull(reloj, "reloj");
        this.sql = new SqlNombrado(new NamedParameterJdbcTemplate(Objects.requireNonNull(jdbc, "jdbc")));
        this.salidas = new CargaMasivaSalida(colaboradores, configuracion);
    }

    @Override
    public String clase() {
        return CLASE;
    }

    /** Legacy :83-263 {@code transaction} / {@code logica(con, false)}. */
    @Override
    public boolean transaction(JobContext ctx) throws Exception {
        logger.info("INICIO :: BcsJobCargaMasivaIngresoEgresoDAPFFMM");
        Long idPrograma = ctx.idPrograma();
        Long idEmpresa = Long.parseLong(ctx.getParam("IdEmpresa"));
        Long idUsuario = Long.parseLong(ctx.getParam("IdUsuario"));
        String tipoArchivoCarga = ctx.getParam("TipoArchivoCarga");
        String fechaSistema = ConfiguracionLegacy.parametro(configuracion, idEmpresa, "PLATAFORMA", "PLA.FechaSistema");
        Long idPaisSistema = Long.parseLong(ConfiguracionLegacy.parametro(configuracion, idEmpresa, "PLATAFORMA", "PLA.PaisSistema"));
        String horaProceso = horaSistemaLocal(idEmpresa);

        String fechaCarga = sql.consultar(SQL_FECHA_ACTUAL, FechaActual.class).get(0).Fecha;

        String validacionesEstadoModulo = consultarEstadoModulos(idEmpresa, idUsuario, "TES");
        if (!validacionesEstadoModulo.isEmpty()) {
            return rechazar(ctx, idPrograma, fechaCarga, horaProceso, validacionesEstadoModulo, tipoArchivoCarga, idEmpresa, null);
        }

        if (cabeceras.cantidad(tipoArchivoCarga, fechaCarga, "PRO") != 0) {
            String mensaje = diccionario.mensaje(idEmpresa, "UI_CARGA_MASIVA_DAPFFMM_YA_CARGADO", new HashMap<>(0));
            return rechazar(ctx, idPrograma, fechaCarga, horaProceso, mensaje, tipoArchivoCarga, idEmpresa, null);
        }
        if (cabeceras.cantidad(tipoArchivoCarga, fechaCarga, "PEN") == 0) {
            String mensaje = diccionario.mensaje(idEmpresa, "UI_CARGA_MASIVA_DAPFFMM_SIN_ARCHIVO_PARA_CARGAR", new HashMap<>(0));
            return rechazar(ctx, idPrograma, fechaCarga, horaProceso, mensaje, tipoArchivoCarga, idEmpresa, null);
        }

        String diaHabilAnterior = diaHabilAnterior(fechaCarga, idPaisSistema);
        Map<String, Object> params = new HashMap<>();
        params.put("FechaEjecucion", diaHabilAnterior);
        params.put("IdEmpresa", idEmpresa);
        EstadoPauta estadoPauta = sql.consultar(SQL_ESTADO_PAUTA, params, EstadoPauta.class).stream().findFirst()
                .orElse(new EstadoPauta());
        if ("PEN".equals(estadoPauta.EstadoPauta)) {
            String mensaje = "No se ha procesado la Pauta para el dia " + diaHabilAnterior;
            return rechazar(ctx, idPrograma, fechaCarga, horaProceso, mensaje, tipoArchivoCarga, idEmpresa,
                    new Registro(idUsuario, fechaSistema));
        }

        List<RangoHorario> rangos = sql.consultar(SQL_RANGO_HORARIO, Map.of("TipoProposito", ID_TIPO_PROGRAMA), RangoHorario.class);
        if (rangos.size() < 1) {
            String mensaje = "No esta configurado el Horario de ejecucion";
            return rechazar(ctx, idPrograma, fechaCarga, horaProceso, mensaje, tipoArchivoCarga, idEmpresa,
                    new Registro(idUsuario, fechaSistema));
        }
        RangoHorario rango = rangos.get(0);
        LocalTime ahora = LocalTime.parse(horaProceso, HORA);
        // TES-J-DEF-01: both ends are excluded
        if (!(ahora.isAfter(LocalTime.parse(rango.HoraInicio, HORA)) && ahora.isBefore(LocalTime.parse(rango.HoraFin, HORA)))) {
            return rechazar(ctx, idPrograma, fechaCarga, horaProceso, "El proceso se encuentra fuera de Horario ",
                    tipoArchivoCarga, idEmpresa, new Registro(idUsuario, fechaSistema));
        }

        EntradaCargaMasivaIngresoEgresoDAPFFMM entrada = new EntradaCargaMasivaIngresoEgresoDAPFFMM();
        entrada.p_IdEmpresa = idEmpresa;
        entrada.p_IdUsuario = idUsuario;
        entrada.p_TipoArchivoCarga = tipoArchivoCarga;
        entrada.p_EnviarArchivoACasilla = ctx.getParam("EnviarArchivoACasilla");
        entrada.p_FechaCarga = fechaCarga;
        entrada.p_IdModulo = "TES";
        entrada.p_SimularOperacion = "N";
        entrada.p_UsaReglasNegocio = "S";

        if (entrada.p_TipoArchivoCarga == null || "".equals(entrada.p_TipoArchivoCarga)) {
            logger.info("INFO :: No se ha ingresado el Tipo Archivo Carga");
            return false; // TES-J-DEF-17 / 19: the framework reports it as success
        }
        SalidaCargaMasivaIngresoEgresoDAPFFMM salida = JsonLegacy.de(carga.execute(JsonLegacy.aJson(entrada)),
                SalidaCargaMasivaIngresoEgresoDAPFFMM.class);

        if (salida.P_OUT_ERRORESCONTADOR > 0L && salida.P_OUT_REGISTROSINGRESADOSCONTADOR == 0L) {
            String mensajes = "";
            for (MensajeSalida mensaje : salida.P_OUT_MENSAJES) {
                if (MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)) {
                    mensajes += mensaje.descripcion + ". ";
                }
            }
            return rechazar(ctx, idPrograma, fechaCarga, horaProceso, mensajes, tipoArchivoCarga, idEmpresa,
                    new Registro(idUsuario, fechaSistema));
        }
        if (!"".equals(salida.P_OUT_RUTA_ARCHIVO)) {
            String mensaje = "Proceso de Carga Masiva realizado correctamente, se han ingresado un total de : "
                    + salida.P_OUT_REGISTROSINGRESADOSCONTADOR + " registros";
            CargaMasivaSalida.actualizarRutaSalida(ctx, salida.P_OUT_RUTA_ARCHIVO, idPrograma);
            salidas.agregarLog(idEmpresa, idUsuario, "S", fechaSistema, tipoArchivoCarga, mensaje);
            return true;
        }
        logger.info("INFO :: No se ha encontrado la Ruta para el Archivo de Salida");
        return false;
    }

    /** AgregarLog data of a rejection ({@code null}: the guard writes no log). */
    private record Registro(Long idUsuario, String fechaSistema) {
    }

    /** A guard: error CSV, RutaSalida, optional AgregarLog "N", and the run ends with a commit (legacy returns true). */
    private boolean rechazar(JobContext ctx, Long idPrograma, String fechaCarga, String horaProceso, String mensaje,
                             String tipoArchivoCarga, Long idEmpresa, Registro log) throws Exception {
        logger.info("INFO :: Se han encontrado errores: {}", mensaje);
        String ruta = salidas.generarArchivoSalidaErrores(fechaCarga, horaProceso, mensaje, tipoArchivoCarga, idEmpresa);
        CargaMasivaSalida.actualizarRutaSalida(ctx, ruta, idPrograma);
        if (log != null) {
            salidas.agregarLog(idEmpresa, log.idUsuario(), "N", log.fechaSistema(), tipoArchivoCarga, mensaje);
        }
        return true;
    }

    /** Legacy {@code FechaDB.ObtenerHoraSistemaLocal} (FechaDB.java:100-121): UTC time plus PLA.SegundosZonaHoraria. */
    private String horaSistemaLocal(Long idEmpresa) throws Exception {
        ConfiguracionLegacy.parametro(configuracion, idEmpresa, "PLATAFORMA", "PLA.FechaSistema");
        int desplazamiento = ConfiguracionLegacy.segundosZonaHoraria(configuracion, idEmpresa);
        return reloj.ahora().atOffset(ZoneOffset.UTC).plusSeconds(desplazamiento).toLocalTime().format(HORA);
    }

    /** Legacy :265-289. */
    private String consultarEstadoModulos(Long idEmpresa, Long idUsuario, String idModulo) {
        ContratosPlataforma.ConsultarEstadoModulo.In entrada = new ContratosPlataforma.ConsultarEstadoModulo.In();
        entrada.p_IdEmpresa = idEmpresa;
        entrada.p_IdUsuario = idUsuario;
        entrada.p_IdModulo = idModulo;
        String mensajes = "";
        ContratosPlataforma.ConsultarEstadoModulo.Out salida = ContratosPlataforma.ConsultarEstadoModulo.logica(colaboradores, entrada);
        for (ContratosPlataforma.ConsultarEstadoModulo.Modulo modulo : salida.p_out_ListaModulos) {
            if (("TES".equals(modulo.NombreModulo) || "CTA".equals(modulo.NombreModulo))
                    && MODULO_ESTADO_CERRADO.equalsIgnoreCase(modulo.IdEstadoModulo)) {
                Map<String, String> valores = new HashMap<>(1);
                valores.put("NombreModulo", "TES".equals(modulo.NombreModulo) ? "TESORERIA" : "CUENTA INVERSION");
                mensajes += diccionario.mensaje(idEmpresa, "UI_ERROR_MODULO_CERRADO", valores) + ". ";
            }
        }
        return mensajes;
    }

    /** Legacy :144-155: the previous business day of the load date. */
    private String diaHabilAnterior(String fechaCarga, Long idPaisSistema) {
        ContratosPlataforma.ObtenerDiaAnterior.In entrada = new ContratosPlataforma.ObtenerDiaAnterior.In();
        ContratosPlataforma.ObtenerDiaAnterior.FechasIn fechas = new ContratosPlataforma.ObtenerDiaAnterior.FechasIn();
        fechas.fechaInicial = fechaCarga;
        fechas.numeroDias = 1L;
        entrada.fechas.add(fechas);
        entrada.idPais = idPaisSistema;
        entrada.diaHabil = "S";
        return ContratosPlataforma.ObtenerDiaAnterior.logica(colaboradores, entrada).fechas.get(0).fechaCalculada;
    }
}
