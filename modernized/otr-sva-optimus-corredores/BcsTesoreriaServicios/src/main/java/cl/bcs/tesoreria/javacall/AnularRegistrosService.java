package cl.bcs.tesoreria.javacall;

import static cl.bcs.tesoreria.javacall.SqlSinComentarios.cargar;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonObject;

import cl.bcs.tesoreria.javacall.comun.Colaborador;
import cl.bcs.tesoreria.javacall.comun.Consultas;
import cl.bcs.tesoreria.javacall.comun.Filas;
import cl.bcs.tesoreria.javacall.comun.JsonLegacy;
import cl.bcs.tesoreria.javacall.comun.MensajeSalida;
import cl.bcs.tesoreria.javacall.contrato.AnularCargoAbono;
import cl.bcs.tesoreria.javacall.contrato.AnularNominasTransferencias;
import cl.bcs.tesoreria.javacall.contrato.CrearEventoAutorizacionMasivo;
import cl.bcs.tesoreria.javacall.contrato.ModificarEstadoEventoAutorizacionMasivo;
import cl.bcs.tesoreria.javacall.contrato.RevisarRequisitosBasicos;
import cl.bcs.tesoreria.port.out.Colaboradores;
import cl.bcs.tesoreria.port.out.DiccionarioMensajes;
import cl.bcs.utils.Utils;

/**
 * Legacy {@code cl.bcs.tesoreria.AnularRegistros}, behaviours TES-A-300..337: annuls movimientos (TES.IngresoEgreso) and comprobantes
 * (TES.Comprobante), releasing the originals they were generated from. With business rules on, it first validates the registers
 * (requirements per register, an authorization event for those that need it) and only the registers that pass are annulled.
 *
 * <p>Works on session temporary tables ({@code #AnularRegistros_*}), so it must run in one transaction/connection. Collaborators (through
 * {@link Colaboradores}): RevisarRequisitosBasicos, CrearEventoAutorizacionMasivo, ModificarEstadoEventoAutorizacionMasivo,
 * AnularNominasTransferencias, AnularCargoAbono.
 */
@Service
public class AnularRegistrosService {

    private static final Logger log = LoggerFactory.getLogger(AnularRegistrosService.class);

    static final String ENTIDAD_MOVIMIENTO = "TES.IngresoEgreso";
    static final String ENTIDAD_COMPROBANTE = "TES.Comprobante";

    private static final String DDL_REGISTROS = "CREATE LOCAL TEMPORARY TABLE #AnularRegistros_p_RegistrosAProcesar (\"Id\" BIGINT,\"Entidad\" NVARCHAR(300))";
    private static final String DDL_COMPROBANTES = "CREATE LOCAL TEMPORARY TABLE #AnularRegistros_ComprobantesAnular "
            + "(\"IdComprobante\" BIGINT, \"IdIngresoEgresoOrigen\" BIGINT, \"IdComprobanteOrigen\"  BIGINT)";
    private static final String SQL_INSERT_REGISTROS = "INSERT INTO #AnularRegistros_p_RegistrosAProcesar (\"Id\",\"Entidad\") VALUES (:Id,:Entidad)";
    private static final String SQL_DELETE_REGISTROS = "DELETE FROM #AnularRegistros_p_RegistrosAProcesar WHERE 1=1";
    private static final String SQL_DROP_REGISTROS = "DROP TABLE #AnularRegistros_p_RegistrosAProcesar";
    private static final String SQL_DROP_COMPROBANTES = "DROP TABLE #AnularRegistros_ComprobantesAnular";

    private static final String SQL_VALIDAR_MOVIMIENTOS = cargar("sql/AnularRegistros_SelectValidarMovimientos.sql");
    private static final String SQL_VALIDAR_COMPROBANTES = cargar("sql/AnularRegistros_SelectValidarComprobante.sql");
    private static final String SQL_EVENTOS_MOVIMIENTOS = cargar("sql/AnularRegistros_SelectEventosAutorizarMovimientos.sql");
    private static final String SQL_EVENTOS_COMPROBANTES = cargar("sql/AnularRegistros_SelectEventosAutorizarComprobantes.sql");
    private static final String SQL_UPDATE_EVENTO_COMPROBANTE = cargar("sql/AnularRegistros_UpdateComprobanteEventosAutorizar.sql");
    private static final String SQL_UPDATE_EVENTO_MOVIMIENTO = cargar("sql/AnularRegistros_UpdateMovimientoEventosAutorizar.sql");
    private static final String SQL_LIQUIDACION_PENDIENTE = cargar("sql/AnularRegistros_SelectComprobanteLiquidacionPendienteDeAutorizacion.sql");
    private static final String SQL_COMPROBANTES_ANULAR = cargar("sql/AnularRegistros_SelectComprobantesAnular.sql");
    private static final String SQL_ANULAR_COMPROBANTE = cargar("sql/AnularRegistros_UpdateAnularComprobante.sql");
    private static final String SQL_ANULAR_FORMA_PAGO_COMPROBANTE = cargar("sql/AnularRegistros_UpdateAnularFormasDePagoComprobante.sql");
    private static final String SQL_ANULAR_DETALLE_COMPROBANTE = cargar("sql/AnularRegistros_UpdateAnularDetalleComprobate.sql");
    private static final String SQL_OBTENER_MOVIMIENTOS = cargar("sql/AnularRegistros_ObtenerMovimientos.sql");
    private static final String SQL_LIBERAR_MOVIMIENTOS = cargar("sql/AnularRegistros_UpdateLiberarMovimientosOriginales.sql");
    private static final String SQL_LIBERAR_FORMA_PAGO_MOVIMIENTOS = cargar("sql/AnularRegistros_UpdateLiberarFormaDePagoMovimientosOriginales.sql");
    private static final String SQL_LIBERAR_COMPROBANTES = cargar("sql/AnularRegistros_UpdateLiberarComprobantesOriginales.sql");
    private static final String SQL_LIBERAR_FORMA_PAGO_COMPROBANTES = cargar("sql/AnularRegistros_UpdateLiberarFormaDePagoComprobantesOriginales.sql");
    private static final String SQL_ANULAR_MOVIMIENTOS = cargar("sql/AnularRegistros_UpdateAnulaMovimientosIngresoEgreso.sql");
    private static final String SQL_ANULAR_FORMA_PAGO_MOVIMIENTOS = cargar("sql/AnularRegistros_UpdateAnulaFormaDePagoMovimientos.sql");
    private static final String SQL_ANULAR_DEPOSITOS = cargar("sql/AnularRegistros_UpdateAnulacionDepositos.sql");
    private static final String SQL_NOMINAS_TRANSFERENCIA = cargar("sql/AnularRegistros_SelectNominasTransferenciaAnular.sql");
    private static final String SQL_CARGOS_ABONO = cargar("sql/AnularRegistros_SelectAnularCargosAbono.sql");

    // ------------------------------------------------------------------ JavaCall contract (legacy names)

    public static class Registro_Entrada {
        public Long Id;
        public String Entidad;

        public Long getId() {
            return Id;
        }

        public String getEntidad() {
            return Entidad;
        }
    }

    public static class AnularRegistros_Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_simularOperacion = "N";
        public String p_UsaReglasNegocio;
        public String p_EjecutaAutorizacion = "S";
        public String p_FechaMovimiento;
        public List<Registro_Entrada> p_RegistrosAProcesar = new ArrayList<>();
    }

    public static class ResultadoProceso {
        public String Campo;
        public Long Id;
        public String Servicio;
        public String Resultado;

        public ResultadoProceso() {
        }

        public ResultadoProceso(String campo, Long id, String servicio, String resultado) {
            this.Campo = campo;
            this.Id = id;
            this.Servicio = servicio;
            this.Resultado = resultado;
        }
    }

    public static class AnularRegistros_Salida {
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public List<ResultadoProceso> P_OUT_RESULTADOPROCESO = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR;
    }

    /** Row of the requirement queries (one row per check of a register). */
    public static class AnularRegistros_TablaRequisitos {
        public Integer Prioridad;
        public Integer Cumple;
        public String CodigoError;
        public String CodigoDato;
        public String ValorDato;
        public String Entidad;
        public Long Id;
        public String RequiereAutorizacion;
    }

    public static class Eventos {
        public Long IdEvento;
        public Long IdEtapa;
    }

    private final NamedParameterJdbcTemplate jdbc;
    private final DiccionarioMensajes diccionario;
    private final Colaboradores colaboradores;

    public AnularRegistrosService(NamedParameterJdbcTemplate jdbc, DiccionarioMensajes diccionario, Colaboradores colaboradores) {
        this.jdbc = jdbc;
        this.diccionario = diccionario;
        this.colaboradores = colaboradores;
    }

    /** Legacy {@code call}: as {@link #logica}; the error counter is recounted from the messages (TES-A-337). */
    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject entrada) throws Exception {
        AnularRegistros_Entrada in = JsonLegacy.PLATAFORMA.fromJson(entrada.toString(), AnularRegistros_Entrada.class);
        AnularRegistros_Salida out = logica(in);
        out.P_OUT_ERRORESCONTADOR = MensajeSalida.numeroErrores(out.P_OUT_MENSAJES);
        return JsonLegacy.PLATAFORMA.toJsonTree(out).getAsJsonObject();
    }

    /** Legacy typed {@code logica}. */
    @Transactional(rollbackFor = Exception.class)
    public AnularRegistros_Salida logica(AnularRegistros_Entrada in) throws Exception {
        log.info("INICIO ANULAR REGISTROS");
        AnularRegistros_Salida out = new AnularRegistros_Salida();
        out.P_OUT_ERRORESCONTADOR = 0L;

        jdbc.getJdbcOperations().execute(DDL_REGISTROS);
        insertarRegistros(in.p_RegistrosAProcesar);

        Set<Long> registrosAProcesar = new HashSet<>(in.p_RegistrosAProcesar.stream().map(Registro_Entrada::getId).collect(Collectors.toList()));

        if (in.p_UsaReglasNegocio.equals("S")) {
            if (!validarYSolicitarAutorizaciones(in, out, registrosAProcesar)) {
                liberarTablasTemporales();
                return out;
            }
            jdbc.getJdbcOperations().execute(SQL_DELETE_REGISTROS);
            insertarRegistros(in.p_RegistrosAProcesar.stream().filter(reg -> registrosAProcesar.contains(reg.Id)).collect(Collectors.toList()));
        }

        MensajeSalida msj = null;
        if (!registrosAProcesar.isEmpty()) {
            msj = anular(in, out);
        }

        construirResultado(in, out, registrosAProcesar);
        construirMensajes(in, out, msj);

        liberarTablasTemporales();
        out.P_OUT_ERRORESCONTADOR = MensajeSalida.numeroErrores(out.P_OUT_MENSAJES);
        log.info("FIN ANULAR REGISTROS");
        return out;
    }

    // ------------------------------------------------------------------ validation and authorization events

    /**
     * Business rules on: basic requirements, per-register requirements, authorization events. Returns false when the process must stop
     * (the output already holds the messages).
     */
    private boolean validarYSolicitarAutorizaciones(AnularRegistros_Entrada in, AnularRegistros_Salida out, Set<Long> registrosAProcesar) {
        RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida basicos = Colaborador.invocar(colaboradores, "RevisarRequisitosBasicos",
                requisitosBasicos(in), RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida.class);
        if (!basicos.p_out_Mensajes.isEmpty()) {
            out.P_OUT_ERRORESCONTADOR = (long) basicos.p_out_Mensajes.size();
            out.P_OUT_MENSAJES.addAll(basicos.p_out_Mensajes);
            return false;
        }
        List<MensajeSalida> requisitos = cumpleRequisitos(in);
        if (!requisitos.isEmpty()) {
            out.P_OUT_MENSAJES.addAll(requisitos);
            long errores = MensajeSalida.numeroErrores(requisitos);
            if (errores > 0) {
                out.P_OUT_ERRORESCONTADOR = errores;
                out.P_OUT_MENSAJES = requisitos.stream().filter(m -> m.tipo().equals(MensajeSalida.MENSAJE_ERROR)).collect(Collectors.toList());
                return false;
            }
        }
        // registers with a pending requirement (authorization) are not annulled now
        requisitos.forEach(req -> registrosAProcesar.remove(req.id()));

        List<Long> porAutorizar = out.P_OUT_MENSAJES.stream().filter(m -> m.tipo().equals(MensajeSalida.MENSAJE_AUTORIZACION)).map(MensajeSalida::id)
                .collect(Collectors.toList());
        return solicitarAutorizacion(in, out, ENTIDAD_MOVIMIENTO, porAutorizar, SQL_EVENTOS_MOVIMIENTOS, SQL_UPDATE_EVENTO_MOVIMIENTO)
                && solicitarAutorizacion(in, out, ENTIDAD_COMPROBANTE, porAutorizar, SQL_EVENTOS_COMPROBANTES, SQL_UPDATE_EVENTO_COMPROBANTE);
    }

    private static Map<String, Object> requisitosBasicos(AnularRegistros_Entrada in) {
        Map<String, Object> entrada = new LinkedHashMap<>();
        entrada.put("p_IdEmpresa", in.p_IdEmpresa);
        entrada.put("p_IdUsuario", in.p_IdUsuario);
        entrada.put("p_IdModulo", in.p_IdModulo);
        return entrada;
    }

    /** Creates the authorization events of the registers of one entity that need it and links each event to its register. */
    private boolean solicitarAutorizacion(AnularRegistros_Entrada in, AnularRegistros_Salida out, String entidad, List<Long> porAutorizar, String sqlEventos,
            String sqlActualizar) {
        List<Long> ids = in.p_RegistrosAProcesar.stream().filter(e -> e.Entidad.equals(entidad) && porAutorizar.contains(e.Id)).map(e -> e.Id)
                .collect(Collectors.toList());
        if (ids.isEmpty()) {
            return true;
        }
        CrearEventoAutorizacionMasivo.ServiceEntrada entrada = new CrearEventoAutorizacionMasivo.ServiceEntrada();
        entrada.P_IDEMPRESA = in.p_IdEmpresa;
        entrada.P_IDMODULO = in.p_IdModulo;
        entrada.P_SIMULAROPERACION = in.p_simularOperacion;
        entrada.P_USAREGLASNEGOCIO = in.p_UsaReglasNegocio;
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("p_IdModulo", in.p_IdModulo).addValue("p_IdUsuario", in.p_IdUsuario)
                .addValue("p_FechaMovimiento", in.p_FechaMovimiento).addValue("Id", ids);
        entrada.P_TABLAEVENTOS = jdbc.query(sqlEventos, params, Filas.de(CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS.class));
        if (entrada.P_TABLAEVENTOS.isEmpty()) {
            return true;
        }
        CrearEventoAutorizacionMasivo.ServiceSalida salida = Colaborador.invocar(colaboradores, "CrearEventoAutorizacionMasivo", entrada,
                CrearEventoAutorizacionMasivo.ServiceSalida.class);
        if (salida.P_OUT_ERRORESCONTADOR > 0) {
            out.P_OUT_ERRORESCONTADOR += salida.P_OUT_ERRORESCONTADOR;
            out.P_OUT_MENSAJES = salida.P_OUT_ERRORESDETALLE.stream()
                    .map(e -> new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, e.Codigo, e.Descripcion)).collect(Collectors.toList());
            return false;
        }
        Consultas.lote(jdbc, sqlActualizar, salida.P_OUT_IDEVENTO.stream().map(e -> {
            Map<String, Object> fila = new HashMap<>();
            fila.put("IdEvento", e.IdEvento);
            fila.put("NumeroOperacionConcepto", e.NumeroOperacionConcepto);
            return fila;
        }).collect(Collectors.toList()));
        return true;
    }

    /** Basic checks (date, registers present and valid) followed by the per-register business rules, in the order of the legacy. */
    private List<MensajeSalida> cumpleRequisitos(AnularRegistros_Entrada in) {
        List<MensajeSalida> out = new ArrayList<>();
        try {
            LocalDate.parse(in.p_FechaMovimiento, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (DateTimeParseException ex) {
            out.add(error(in, "FECHA_SISTEMA_INVALIDA", new HashMap<>()));
        }
        if (in.p_RegistrosAProcesar.isEmpty()) {
            out.add(error(in, "NO_VIENEN_MOVIMIENTOS", new HashMap<>()));
        }
        in.p_RegistrosAProcesar.stream().filter(reg -> Optional.ofNullable(reg.Id).orElse(0L).equals(0L)).forEach(reg -> {
            boolean movimiento = reg.Entidad.equals(ENTIDAD_MOVIMIENTO);
            Map<String, String> param = new HashMap<>();
            param.put(movimiento ? "IdIngresoEgreso" : "IdComprobante", String.valueOf(reg.Id));
            out.add(error(in, movimiento ? "MOVIMIENTO_TESORERIA_INVALIDO" : "COMPROBANTE_TESORERIA_IVALIDO", param));
        });
        List<String> entidadesValidas = List.of(ENTIDAD_MOVIMIENTO, ENTIDAD_COMPROBANTE);
        in.p_RegistrosAProcesar.stream().filter(reg -> !entidadesValidas.contains(reg.Entidad))
                .forEach(reg -> out.add(error(in, "ENTIDAD_TESORERIA_INVALIDA", new HashMap<>())));

        agregarRequisitos(in, out, SQL_VALIDAR_MOVIMIENTOS);
        agregarRequisitos(in, out, SQL_VALIDAR_COMPROBANTES);
        return out;
    }

    private void agregarRequisitos(AnularRegistros_Entrada in, List<MensajeSalida> out, String sql) {
        jdbc.query(sql, Map.of("p_IdModulo", in.p_IdModulo), Filas.de(AnularRegistros_TablaRequisitos.class)).stream().filter(req -> req.Cumple.equals(0))
                .forEach(req -> {
                    String tipo = req.RequiereAutorizacion.equals("S") ? MensajeSalida.MENSAJE_AUTORIZACION : MensajeSalida.MENSAJE_ERROR;
                    Map<String, String> param = new HashMap<>();
                    param.put(req.CodigoDato, req.ValorDato);
                    out.add(new MensajeSalida(req.Id, tipo, req.CodigoError, diccionario.mensaje(in.p_IdEmpresa, req.CodigoError, param)));
                });
    }

    private MensajeSalida error(AnularRegistros_Entrada in, String codigo, Map<String, String> valores) {
        return new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, codigo, diccionario.mensaje(in.p_IdEmpresa, codigo, valores));
    }

    // ------------------------------------------------------------------ annulment

    /** Annuls comprobantes, movimientos, deposits, nominas and the cuenta-inversion movements; returns the message left in the legacy local variable. */
    private MensajeSalida anular(AnularRegistros_Entrada in, AnularRegistros_Salida out) throws SQLException {
        MensajeSalida msj = null;
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("p_IdUsuario", in.p_IdUsuario).addValue("p_IdModulo", in.p_IdModulo);

        // rejects the pending authorization of liquidation comprobantes (state PDA)
        List<Long> ids = new ArrayList<>();
        for (Registro_Entrada registro : in.p_RegistrosAProcesar) {
            ids.add(registro.Id);
        }
        List<Eventos> eventos = jdbc.query(SQL_LIQUIDACION_PENDIENTE, new MapSqlParameterSource("IdsComprobante", Consultas.enLista(ids)),
                Filas.de(Eventos.class));
        rechazarAutorizaciones(eventos, in);

        // comprobantes
        jdbc.getJdbcOperations().execute(DDL_COMPROBANTES);
        jdbc.update(SQL_COMPROBANTES_ANULAR, params);
        jdbc.update(SQL_ANULAR_COMPROBANTE, params);
        jdbc.update(SQL_ANULAR_FORMA_PAGO_COMPROBANTE, params);
        jdbc.update(SQL_ANULAR_DETALLE_COMPROBANTE, params);
        if (!in.p_IdModulo.equals("TES")) {
            List<Registro_Entrada> movimientos = jdbc.query(SQL_OBTENER_MOVIMIENTOS, new MapSqlParameterSource("p_comprobante", Consultas.enLista(ids)),
                    Filas.de(Registro_Entrada.class));
            insertarRegistros(movimientos);
            for (Registro_Entrada movimiento : movimientos) {
                // LEGACY DEFECT kept (see README, AnularRegistros): INGRESO_EGRESO_ANULADO is built but never added to the output for non-TES modules
                msj = new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "INGRESO_EGRESO_ANULADO", "Ingreso/Egreso " + movimiento.Id + " anulado");
            }
        } else {
            // releases the movimientos the comprobantes were generated from
            jdbc.update(SQL_LIBERAR_MOVIMIENTOS, params);
            jdbc.update(SQL_LIBERAR_FORMA_PAGO_MOVIMIENTOS, params);
        }
        // releases the comprobantes the comprobantes were generated from
        jdbc.update(SQL_LIBERAR_COMPROBANTES, params);
        jdbc.update(SQL_LIBERAR_FORMA_PAGO_COMPROBANTES, params);

        // movimientos, their formas de pago and the deposits
        jdbc.update(SQL_ANULAR_MOVIMIENTOS, params);
        jdbc.update(SQL_ANULAR_FORMA_PAGO_MOVIMIENTOS, params);
        jdbc.update(SQL_ANULAR_DEPOSITOS, params);

        // only whether there is any nomina detail to annul matters (the legacy mapped the rows onto a class that took none of the columns)
        if (!jdbc.queryForList(SQL_NOMINAS_TRANSFERENCIA, Map.of()).isEmpty()) {
            anularNominas(in, out);
        }

        // reverses the cuenta-inversion movements
        List<AnularCargoAbono.CargoAbono_Entrada> cargosAbono = jdbc.query(SQL_CARGOS_ABONO, Map.of(), Filas.de(AnularCargoAbono.CargoAbono_Entrada.class));
        if (!cargosAbono.isEmpty()) {
            anularCargosAbono(in, out, cargosAbono);
        }
        return msj;
    }

    /** Rejects (state RZD) the open authorization stages; the legacy swallowed any failure of the collaborator. */
    private void rechazarAutorizaciones(List<Eventos> eventos, AnularRegistros_Entrada in) {
        ModificarEstadoEventoAutorizacionMasivo.ServicioEntrada entrada = new ModificarEstadoEventoAutorizacionMasivo.ServicioEntrada();
        entrada.p_IdEmpresa = in.p_IdEmpresa;
        entrada.p_IdModulo = "AUT";
        entrada.p_IdUsuario = in.p_IdUsuario;
        entrada.p_SimularOperacion = "N";
        entrada.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
        entrada.p_IdEstado = "RZD";
        entrada.p_EventoEtapa = "ETAPA";
        entrada.p_Observaciones = " ";
        entrada.p_Etapas = eventos.stream().map(e -> {
            ModificarEstadoEventoAutorizacionMasivo.ServicioEntradaEtapa etapa = new ModificarEstadoEventoAutorizacionMasivo.ServicioEntradaEtapa();
            etapa.IdEvento = e.IdEvento;
            etapa.IdEtapa = e.IdEtapa;
            return etapa;
        }).collect(Collectors.toList());
        ModificarEstadoEventoAutorizacionMasivo.ServicioEntradaEvento evento = new ModificarEstadoEventoAutorizacionMasivo.ServicioEntradaEvento();
        evento.IdEvento = 0L;
        entrada.p_Eventos = new ArrayList<>(Collections.singletonList(evento));
        try {
            Colaborador.invocar(colaboradores, "ModificarEstadoEventoAutorizacionMasivo", entrada, ModificarEstadoEventoAutorizacionMasivo.ServicioSalida.class);
        } catch (RuntimeException e) {
            // LEGACY DEFECT kept (see README, AnularRegistros): a failure of the authorization service is only printed, the annulment goes on
            log.warn("RechazarAutorizaciones failed", e);
        }
    }

    private void anularNominas(AnularRegistros_Entrada in, AnularRegistros_Salida out) {
        AnularNominasTransferencias.EntradaAnularNominasTransferencias entrada = new AnularNominasTransferencias.EntradaAnularNominasTransferencias();
        entrada.p_IdEmpresa = in.p_IdEmpresa;
        entrada.p_IdModulo = in.p_IdModulo;
        entrada.p_IdUsuario = in.p_IdUsuario;
        entrada.p_FechaMovimiento = in.p_FechaMovimiento;
        entrada.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
        entrada.p_RegistrosAAnular = in.p_RegistrosAProcesar.stream()
                .map(reg -> new AnularNominasTransferencias.RegistrosAnularNominasTransferencias(reg.Id, reg.Entidad)).collect(Collectors.toList());
        AnularNominasTransferencias.SalidaAnularNominasTransferencias salida = Colaborador.invocar(colaboradores, "AnularNominasTransferencias", entrada,
                AnularNominasTransferencias.SalidaAnularNominasTransferencias.class);
        out.P_OUT_RESULTADOPROCESO.addAll(salida.p_out_ResultadoProceso.stream()
                .map(res -> new ResultadoProceso(res.Campo, res.Id, res.Servicio, res.Resultado)).collect(Collectors.toList()));
    }

    private void anularCargosAbono(AnularRegistros_Entrada in, AnularRegistros_Salida out, List<AnularCargoAbono.CargoAbono_Entrada> cargosAbono) {
        AnularCargoAbono.AnularCargoAbono_Entrada entrada = new AnularCargoAbono.AnularCargoAbono_Entrada();
        entrada.p_IdEmpresa = in.p_IdEmpresa;
        entrada.p_IdUsuario = in.p_IdUsuario;
        entrada.p_IdModulo = in.p_IdModulo;
        entrada.p_FechaMovimiento = in.p_FechaMovimiento;
        entrada.p_simularOperacion = "N";
        entrada.p_UsaReglasNegocio = "S";
        entrada.p_EjecutaAutorizacion = "N";
        entrada.p_OmiteRegistroSinAutorizar = "N";
        entrada.p_EsAnulacionManual = "N";
        entrada.p_CargoAbonoAAnular = cargosAbono;
        AnularCargoAbono.AnularCargoAbono_Salida salida = Colaborador.invocar(colaboradores, "AnularCargoAbono", entrada,
                AnularCargoAbono.AnularCargoAbono_Salida.class);
        out.P_OUT_MENSAJES.addAll(salida.P_OUT_MENSAJES);
        out.P_OUT_ERRORESCONTADOR += salida.P_OUT_ERRORESCONTADOR;
    }

    // ------------------------------------------------------------------ output

    /** One result per register that was annulled; the iteration order is that of a {@link HashSet} of ids, which the goldens pin. */
    private void construirResultado(AnularRegistros_Entrada in, AnularRegistros_Salida out, Set<Long> registrosAProcesar) throws Exception {
        Map<Long, Object> registros = Utils.List2Hash(in.p_RegistrosAProcesar, "Id");
        for (Long id : registrosAProcesar) {
            Registro_Entrada reg = (Registro_Entrada) registros.get(id);
            if (reg != null) {
                ResultadoProceso res = new ResultadoProceso();
                res.Campo = reg.Entidad.equals(ENTIDAD_MOVIMIENTO) ? "IdIngresoEgreso" : "IdComprobante";
                res.Id = id;
                res.Servicio = "BSCL.Tesoreria.BD.Servicios::AnularRegistros";
                res.Resultado = "ANU";
                out.P_OUT_RESULTADOPROCESO.add(res);
            }
        }
    }

    /**
     * One message per result. LEGACY DEFECT kept (see README, AnularRegistros): a result that matches no rule re-adds the previous message (or null,
     * which then fails the error recount), because the message variable is not reset between results.
     */
    private void construirMensajes(AnularRegistros_Entrada in, AnularRegistros_Salida out, MensajeSalida anterior) {
        MensajeSalida msj = anterior;
        for (ResultadoProceso res : out.P_OUT_RESULTADOPROCESO) {
            boolean anulado = res.Resultado.equalsIgnoreCase("ANU");
            boolean rechazado = res.Resultado.equalsIgnoreCase("RECH");
            if (anulado && res.Campo.equalsIgnoreCase("IdComprobante")) {
                msj = new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "COMPROBANTE_ANULADO", "Comprobante " + res.Id + " anulado");
            } else if (rechazado && res.Campo.equalsIgnoreCase("IdComprobante")) {
                msj = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "COMPROBANTE_RECHAZADO", "Comprobante " + res.Id + " rechazado");
            } else if (anulado && res.Campo.equalsIgnoreCase("IdIngresoEgreso")) {
                Map<String, String> valores = new HashMap<>();
                valores.put("IdMovimientoComprobante", String.valueOf(res.Id));
                msj = new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "UI_MOVIMIENTO_COMPROBANTE_ANULADO",
                        diccionario.mensaje(in.p_IdEmpresa, "UI_MOVIMIENTO_COMPROBANTE_ANULADO", valores));
            } else if (rechazado && res.Campo.equalsIgnoreCase("IdIngresoEgreso")) {
                msj = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "INGRESO_EGRESO_RECHAZADO", "Ingreso/Egreso " + res.Id + " rechazado");
            } else if (anulado && res.Campo.equalsIgnoreCase("IdNominaTransferencia")) {
                msj = new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "NOMINA_TRANSFERENCIA_ANULADO", "Nomina Transferencia " + res.Id + " anulado");
            } else if (rechazado && res.Campo.equalsIgnoreCase("IdNominaTransferencia")) {
                msj = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "NOMINA_TRANSFERENCIA_RECHAZADO", "Nomina Transferencia " + res.Id + " rechazado");
            }
            out.P_OUT_MENSAJES.add(msj);
        }
    }

    // ------------------------------------------------------------------ temporary tables

    /** Legacy {@code SQL.batch}: a null list is an error, an empty one writes nothing. */
    private void insertarRegistros(List<Registro_Entrada> registros) throws SQLException {
        if (registros == null) {
            throw new SQLException("Null parameters. If parameters aren't need, pass an empty");
        }
        Consultas.lote(jdbc, SQL_INSERT_REGISTROS, registros.stream().map(r -> {
            Map<String, Object> fila = new HashMap<>();
            fila.put("Id", r.Id);
            fila.put("Entidad", r.Entidad);
            return fila;
        }).collect(Collectors.toList()));
    }

    /** The legacy dropped them only at the normal end; here also when the process stops early so a pooled connection does not keep them. */
    private void liberarTablasTemporales() {
        try {
            jdbc.getJdbcOperations().execute(SQL_DROP_REGISTROS);
            jdbc.getJdbcOperations().execute(SQL_DROP_COMPROBANTES);
        } catch (RuntimeException er) {
            log.debug("temporary tables were not dropped", er);
        }
    }
}
