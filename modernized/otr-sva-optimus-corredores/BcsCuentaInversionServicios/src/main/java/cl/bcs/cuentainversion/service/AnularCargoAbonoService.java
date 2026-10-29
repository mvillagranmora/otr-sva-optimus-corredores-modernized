package cl.bcs.cuentainversion.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import cl.bcs.cuentainversion.domain.colaborador.AgregarLog;
import cl.bcs.cuentainversion.domain.colaborador.ConsultarEmpresa;
import cl.bcs.cuentainversion.domain.colaborador.CrearEventoAutorizacionMasivo;
import cl.bcs.cuentainversion.domain.colaborador.RevisarRequisitosBasicos;
import cl.bcs.cuentainversion.port.out.Colaboradores;
import cl.bcs.cuentainversion.port.out.DiccionarioMensajes;
import cl.bcs.cuentainversion.service.CtaJavaCallInfra.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.Utils;

/**
 * Voids certified cargo/abono movements (README CTA-A-32..47 and 108; legacy AnularCargoAbono).
 *
 * <p>With business rules ({@code p_UsaReglasNegocio = S}) each movement is validated by SQL (not found, already voided, voiding not
 * allowed, needs authorization, pending authorization, abono that is half of a transfer); movements that need authorization get an
 * authorization event instead of being voided. Then, if the company allows voiding, the movements become ANU, the inventory
 * book is restored and the balances are recalculated; otherwise a counter-movement is created through CrearCargoAbono.
 * Every outcome is logged through AgregarLog.
 */
@Service
public class AnularCargoAbonoService {

    private static final Logger log = LoggerFactory.getLogger(AnularCargoAbonoService.class);

    private static final String SQL = "cl/bcs/cuentainversion/sql/AnularCargoAbono_";
    private static final String SQL_DELETE_IN = CtaJavaCallInfra.loadSql(SQL + "Delete_In.sql");
    private static final String SQL_DELETE_SRV = CtaJavaCallInfra.loadSql(SQL + "Delete_Srv.sql");
    private static final String SQL_INSERT_IN = CtaJavaCallInfra.loadSql(SQL + "InsertCargoAbono.sql");
    private static final String SQL_INSERT_SRV = CtaJavaCallInfra.loadSql(SQL + "Select_Insert_CargoAbono.sql");
    private static final String SQL_INSERT_CUMPLE = CtaJavaCallInfra.loadSql(SQL + "CumpleRequisito_Insert.sql");
    private static final String SQL_SELECT_CUMPLE = CtaJavaCallInfra.loadSql(SQL + "CumpleRequisito_Select.sql");
    private static final String SQL_SELECT_REQUIERE_AUTORIZACION = CtaJavaCallInfra.loadSql(SQL + "Select_RequiereAutorizacion.sql");
    private static final String SQL_SELECT_EVENTO_AUTORIZACION = CtaJavaCallInfra.loadSql(SQL + "Select_EventoAutorizacion.sql");
    private static final String SQL_UPDATE_EVENTO = CtaJavaCallInfra.loadSql(SQL + "Update_EventoCargoAbono.sql");
    private static final String SQL_DELETE_RECHAZADOS = CtaJavaCallInfra.loadSql(SQL + "Delete_CargoAbono.sql");
    private static final String SQL_COUNT_SRV = CtaJavaCallInfra.loadSql(SQL + "Count_MovimientosAnular.sql");
    private static final String SQL_SELECT_CONFIGURACION = CtaJavaCallInfra.loadSql(SQL + "Select_ConfiguracionCuentaInversion.sql");
    private static final String SQL_UPDATE_ESTADO = CtaJavaCallInfra.loadSql(SQL + "Update_EstadoMovimiento.sql");
    private static final String SQL_UPDATE_MOVIMIENTO = CtaJavaCallInfra.loadSql(SQL + "Update_Movimiento.sql");
    private static final String SQL_UPDATE_LIBRO = CtaJavaCallInfra.loadSql(SQL + "Update_LibroInventarioSaldos.sql");
    private static final String SQL_SELECT_FECHA_MINIMA = CtaJavaCallInfra.loadSql(SQL + "Select_FechaMinima.sql");
    private static final String SQL_SELECT_CUENTAS = CtaJavaCallInfra.loadSql(SQL + "Select_SrvMovimientoAnular.sql");
    private static final String SQL_SELECT_RESULTADO_ANULAR = CtaJavaCallInfra.loadSql(SQL + "Select_ResultadoAnular.sql");
    private static final String SQL_SELECT_PLAZO_MASIVO = CtaJavaCallInfra.loadSql(SQL + "Select_ObtenerPlazoMasivo.sql");
    private static final String SQL_INSERT_PLAZO = CtaJavaCallInfra.loadSql(SQL + "Insert_ObtenerPlazoFechas.sql");
    private static final String SQL_SELECT_ENTRADA_CREAR = CtaJavaCallInfra.loadSql(SQL + "Select_EntradaCrearCargoAbono.sql");
    private static final String SQL_FERIADOS = CtaJavaCallInfra.loadSql("cl/bcs/cuentainversion/sql/Calendario_ConsultarFeriados.sql");

    private static final String RESULTADO_ANULADO = "ANU";
    private static final String RESULTADO_RECHAZADO = "RECH";
    private static final String RESULTADO_REQUIERE_AUTORIZACION = "RAUT";

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_simularOperacion;
        public String p_UsaReglasNegocio;
        public String p_EjecutaAutorizacion = "S";
        public String p_OmiteRegistroSinAutorizar = "S";
        public String p_FechaMovimiento;
        public String p_EsAnulacionManual = "N";
        public List<CargoAbonoAAnular> p_CargoAbonoAAnular = new ArrayList<>();
    }

    public static class CargoAbonoAAnular {
        public Long IdCargoAbonoMovimiento;
        public String GlosaAnulacion;
    }

    public static class Salida {
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR;
    }

    /** Row of the requirement check; {@code Resultado} is OK, ADVE, RAUT or RECH. */
    public static class ResultadoRequisito {
        public Long IdInterno;
        public String Codigo;
        public String Resultado;
        public String IdTipoMovimiento;
    }

    /** Outcome per voided (or counter) movement. */
    public static class ResultadoAnulacion {
        public Long IdCargoAbonoMovimiento;
        public String Resultado;
        public String IdTipoMovimiento;
        public String IdTipoOperacion;
        public Long IdCliente;
        public Long IdPortafolio;
        public String IdMoneda;
        public BigDecimal Monto;
    }

    public static class PermiteAnular {
        public String PermiteAnularMovimiento;
    }

    public static class RequiereAutorizacion {
        public String IdConcepto;
        public String RequiereAutorizacion;
    }

    public static class CuentaInversion {
        public Long IdCuentaInversion;
    }

    public static class FechaPlazo {
        public Long IdInterno;
        public String FechaDesde;
        public String FechaHasta;
        public String SoloDiasHabiles;
        public Long Plazo;
    }

    private final NamedParameterJdbcTemplate jdbc;
    private final ActualizarSaldoCargoAbonoService actualizarSaldo;
    private final CrearCargoAbonoService crearCargoAbono;
    private final DiccionarioMensajes diccionario;
    private final Colaboradores colaboradores;

    public AnularCargoAbonoService(NamedParameterJdbcTemplate jdbc, ActualizarSaldoCargoAbonoService actualizarSaldo,
                                   CrearCargoAbonoService crearCargoAbono, DiccionarioMensajes diccionario, Colaboradores colaboradores) {
        this.jdbc = jdbc;
        this.actualizarSaldo = actualizarSaldo;
        this.crearCargoAbono = crearCargoAbono;
        this.diccionario = diccionario;
        this.colaboradores = colaboradores;
    }

    /** JavaCall entry point: the error counter is recomputed from the messages (legacy call()). */
    @Transactional
    public JsonObject execute(JsonObject params) throws Exception {
        Gson gson = Utils.getGsonBuilder();
        Salida salida = anular(gson.fromJson(params, Entrada.class));
        salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_MENSAJES.stream().filter(m -> MensajeSalida.ERROR.equals(m.tipo)).count();
        return gson.toJsonTree(salida).getAsJsonObject();
    }

    /** Legacy logica(): the error counter is only set by the early-return cases (execute recomputes it). */
    public Salida anular(Entrada in) throws Exception {
        log.info("INICIO::AnularCargoAbono");
        Salida out = new Salida();
        out.P_OUT_ERRORESCONTADOR = 0L;

        jdbc.getJdbcTemplate().execute(SQL_DELETE_IN);
        jdbc.getJdbcTemplate().execute(SQL_DELETE_SRV);
        jdbc.batchUpdate(SQL_INSERT_IN, in.p_CargoAbonoAAnular.stream()
                .map(c -> (SqlParameterSource) new MapSqlParameterSource()
                        .addValue("IdCargoAbonoMovimiento", c.IdCargoAbonoMovimiento)
                        .addValue("GlosaAnulacion", c.GlosaAnulacion))
                .toArray(SqlParameterSource[]::new));
        MapSqlParameterSource empresa = new MapSqlParameterSource("p_IdEmpresa", in.p_IdEmpresa);
        jdbc.update(SQL_INSERT_SRV, empresa);

        // LEGACY DEFECT kept (see README CTA-A-47): a null p_UsaReglasNegocio throws NullPointerException
        if (in.p_UsaReglasNegocio.equals("S")) {
            RevisarRequisitosBasicos.Salida requisitos = colaboradores.invocar(RevisarRequisitosBasicos.CLASE,
                    RevisarRequisitosBasicos.entrada(in.p_IdEmpresa, in.p_IdUsuario, "TES"), RevisarRequisitosBasicos.Salida.class);
            if (!requisitos.p_out_Mensajes.isEmpty()) {
                out.P_OUT_ERRORESCONTADOR = (long) requisitos.p_out_Mensajes.size();
                requisitos.p_out_Mensajes.forEach(m -> out.P_OUT_MENSAJES.add(new MensajeSalida(m.id, m.tipo, m.codigo, m.descripcion)));
                return out;
            }
            if (in.p_CargoAbonoAAnular.stream().noneMatch(c -> c.IdCargoAbonoMovimiento > 0)) {
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.ERROR, "NO_VIENEN_MOVIMIENTOS", "NO_VIENEN_MOVIMIENTOS"));
                return out;
            }

            jdbc.getJdbcTemplate().execute(SQL_INSERT_CUMPLE);
            List<ResultadoRequisito> requisitosMovimiento = jdbc.query(SQL_SELECT_CUMPLE, new MapSqlParameterSource(),
                    CtaJavaCallInfra.fieldMapper(ResultadoRequisito.class));
            aplicarMantenedorDeAutorizacion(requisitosMovimiento);

            // LEGACY DEFECT kept (see README CTA-A-43): p_simularOperacion = S skips this block and still annuls
            // LEGACY DEFECT kept (see README CTA-A-44): p_EjecutaAutorizacion = N annuls without asking for authorization
            if (!requisitosMovimiento.isEmpty() && in.p_simularOperacion.equals("N") && in.p_EjecutaAutorizacion.equals("S")) {
                solicitarAutorizaciones(in, requisitosMovimiento);
                quitarRechazadosYPorAutorizar(in, requisitosMovimiento);
                informarRechazosYAutorizaciones(in, requisitosMovimiento, out);
            }

            Long pendientes = jdbc.getJdbcTemplate().queryForObject(SQL_COUNT_SRV, Long.class);
            if (pendientes == null || pendientes == 0L) {
                return out;
            }
        }

        List<ResultadoAnulacion> resultados;
        List<PermiteAnular> permite = jdbc.query(SQL_SELECT_CONFIGURACION, new MapSqlParameterSource("IdEmpresa", in.p_IdEmpresa),
                CtaJavaCallInfra.fieldMapper(PermiteAnular.class));
        if (!permite.isEmpty() && permite.get(0).PermiteAnularMovimiento.equals("S")) {
            jdbc.update(SQL_UPDATE_ESTADO, empresa);
            jdbc.update(SQL_UPDATE_MOVIMIENTO, new MapSqlParameterSource());
            jdbc.update(SQL_UPDATE_LIBRO, new MapSqlParameterSource());
            recalcularSaldos(in);
            resultados = jdbc.query(SQL_SELECT_RESULTADO_ANULAR, new MapSqlParameterSource(), CtaJavaCallInfra.fieldMapper(ResultadoAnulacion.class));
        } else {
            resultados = crearContrapartidas(in);
        }

        informarYRegistrar(in, resultados, out);
        jdbc.getJdbcTemplate().execute(SQL_DELETE_IN);
        log.info("FIN::AnularCargoAbono");
        return out;
    }

    /** A concept whose authorization is switched off in the maintainer (PLA.RequiereAutorizacion) turns RAUT into OK. */
    private void aplicarMantenedorDeAutorizacion(List<ResultadoRequisito> requisitos) {
        Map<String, String> mantenedor = new HashMap<>();
        jdbc.query(SQL_SELECT_REQUIERE_AUTORIZACION, new MapSqlParameterSource(), CtaJavaCallInfra.fieldMapper(RequiereAutorizacion.class))
                .forEach(r -> {
                    if (r.IdConcepto.equals("ANUCAR") || r.IdConcepto.equals("ANUABO")) {
                        mantenedor.put(r.IdConcepto, r.RequiereAutorizacion);
                    }
                });
        for (ResultadoRequisito r : requisitos) {
            if ("ANULACION_REQUIERE_AUTORIZACION".equals(r.Codigo)) {
                String clave = "MOVCAR".equals(r.IdTipoMovimiento) ? "ANUCAR" : "ANUABO";
                if (!"S".equals(mantenedor.getOrDefault(clave, ""))) {
                    r.Resultado = "OK";
                }
            }
        }
    }

    private void solicitarAutorizaciones(Entrada in, List<ResultadoRequisito> requisitos) {
        List<Long> porAutorizar = requisitos.stream().filter(r -> r.Resultado.equals(RESULTADO_REQUIERE_AUTORIZACION))
                .map(r -> r.IdInterno).toList();
        if (porAutorizar.isEmpty()) {
            return;
        }
        CrearEventoAutorizacionMasivo.ServiceEntrada evento = new CrearEventoAutorizacionMasivo.ServiceEntrada();
        evento.P_IDEMPRESA = in.p_IdEmpresa;
        evento.P_IDMODULO = in.p_IdModulo;
        evento.P_IDUSUARIO = in.p_IdUsuario;
        evento.P_SIMULAROPERACION = in.p_simularOperacion;
        evento.P_TABLAEVENTOS = jdbc.query(SQL_SELECT_EVENTO_AUTORIZACION,
                new MapSqlParameterSource().addValue("p_IdModulo", in.p_IdModulo).addValue("p_IdUsuario", in.p_IdUsuario)
                        .addValue("IdInterno", porAutorizar),
                CtaJavaCallInfra.fieldMapper(CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS.class));
        evento.P_USAREGLASNEGOCIO = in.p_UsaReglasNegocio;
        CrearEventoAutorizacionMasivo.ServiceSalida creados = colaboradores.invocar(CrearEventoAutorizacionMasivo.CLASE, evento,
                CrearEventoAutorizacionMasivo.ServiceSalida.class);

        jdbc.batchUpdate(SQL_UPDATE_EVENTO, creados.P_OUT_IDEVENTO.stream()
                .map(e -> (SqlParameterSource) new MapSqlParameterSource().addValue("IdEvento", e.IdEvento)
                        .addValue("NumeroOperacionConcepto", e.NumeroOperacionConcepto).addValue("p_IdEmpresa", in.p_IdEmpresa))
                .toArray(SqlParameterSource[]::new));
    }

    /** Movements that wait for authorization or were rejected leave the working table (they are not voided). */
    private void quitarRechazadosYPorAutorizar(Entrada in, List<ResultadoRequisito> requisitos) {
        List<Object[]> lotes = requisitos.stream()
                .map(r -> new Object[] {r.IdInterno, r.Resultado, in.p_EjecutaAutorizacion, in.p_OmiteRegistroSinAutorizar,
                        in.p_EjecutaAutorizacion, in.p_OmiteRegistroSinAutorizar})
                .toList();
        jdbc.getJdbcTemplate().batchUpdate(SQL_DELETE_RECHAZADOS, lotes);
    }

    private void informarRechazosYAutorizaciones(Entrada in, List<ResultadoRequisito> requisitos, Salida out) {
        requisitos.stream().filter(r -> r.Resultado.equals(RESULTADO_RECHAZADO) || r.Resultado.equals(RESULTADO_REQUIERE_AUTORIZACION))
                .forEach(r -> {
                    String tipo = r.Resultado.equals(RESULTADO_RECHAZADO) ? MensajeSalida.ERROR : MensajeSalida.AUTORIZACION;
                    Map<String, String> valores = new HashMap<>();
                    valores.put("IdCargoAbonoMovimiento", String.valueOf(r.IdInterno));
                    valores.put("Id", String.valueOf(r.IdInterno));
                    out.P_OUT_MENSAJES.add(new MensajeSalida(r.IdInterno, tipo, r.Codigo, diccionario.traducir(in.p_IdEmpresa, r.Codigo, valores)));
                });
    }

    private void recalcularSaldos(Entrada in) {
        List<Map<String, Object>> fila = jdbc.queryForList(SQL_SELECT_FECHA_MINIMA, new MapSqlParameterSource());
        // LEGACY DEFECT kept (see README CTA-A-45): no movement found -> MIN(FechaSistema) is null and the date format throws NullPointerException
        String fecha = fila.isEmpty() ? "" : Fecha.stringToSqlDate((java.util.Date) fila.get(0).get("FechaSistema"));
        if (fecha.isEmpty()) {
            return;
        }
        ActualizarSaldoCargoAbonoService.Entrada entrada = new ActualizarSaldoCargoAbonoService.Entrada();
        entrada.p_IdEmpresa = in.p_IdEmpresa;
        entrada.p_IdUsuario = in.p_IdUsuario;
        entrada.p_FechaMovimiento = fecha;
        jdbc.query(SQL_SELECT_CUENTAS, new MapSqlParameterSource(), CtaJavaCallInfra.fieldMapper(CuentaInversion.class))
                .forEach(c -> {
                    ActualizarSaldoCargoAbonoService.CuentaInversion cuenta = new ActualizarSaldoCargoAbonoService.CuentaInversion();
                    cuenta.IdCuentaInversion = c.IdCuentaInversion;
                    entrada.p_CuentaInversion.add(cuenta);
                });
        actualizarSaldo.actualizar(entrada);
    }

    /** The company does not allow voiding: a counter-movement (cargo <-> abono) is created for each movement. */
    private List<ResultadoAnulacion> crearContrapartidas(Entrada in) throws Exception {
        ConsultarEmpresa.InformacionEmpresa empresa = Utils.getGsonBuilder().fromJson(
                colaboradores.invocar(ConsultarEmpresa.CLASE, Colaboradores.valor(in.p_IdEmpresa)), ConsultarEmpresa.InformacionEmpresa.class);
        MapSqlParameterSource params = new MapSqlParameterSource("IdEmpresa", in.p_IdEmpresa).addValue("FechaMovimiento", in.p_FechaMovimiento);
        registrarPlazos(jdbc.query(SQL_SELECT_PLAZO_MASIVO, params, CtaJavaCallInfra.fieldMapper(FechaPlazo.class)), empresa.IdPais);

        CrearCargoAbonoService.Entrada entrada = new CrearCargoAbonoService.Entrada();
        entrada.p_IdEmpresa = in.p_IdEmpresa;
        entrada.p_IdModulo = in.p_IdModulo;
        entrada.p_IdUsuario = in.p_IdUsuario;
        entrada.p_simularOperacion = "N";
        entrada.p_UsaReglasNegocio = "N";
        entrada.p_FechaSistema = in.p_FechaMovimiento;
        // LEGACY DEFECT kept (see README CTA-A-108): p_EsIngresoManual is taken from p_UsaReglasNegocio
        entrada.p_EsIngresoManual = in.p_UsaReglasNegocio;
        entrada.p_CargoAbonoAGenerar = jdbc.query(SQL_SELECT_ENTRADA_CREAR, params,
                CtaJavaCallInfra.fieldMapper(CrearCargoAbonoService.CargoAbonoAGenerar.class));
        CrearCargoAbonoService.Salida creados = crearCargoAbono.crear(entrada);

        List<ResultadoAnulacion> resultados = new ArrayList<>();
        for (CrearCargoAbonoService.CargoAbonoGenerado c : creados.P_OUT_CARGOABONOGENERADO) {
            ResultadoAnulacion r = new ResultadoAnulacion();
            r.IdCargoAbonoMovimiento = c.IdCargoAbonoMovimiento;
            r.Resultado = "CRT";
            r.IdTipoMovimiento = c.IdTipoMovimiento;
            resultados.add(r);
        }
        return resultados;
    }

    private void informarYRegistrar(Entrada in, List<ResultadoAnulacion> resultados, Salida out) {
        AgregarLog.InAgregarLog registro = new AgregarLog.InAgregarLog();
        for (ResultadoAnulacion r : resultados) {
            String codigo = "";
            if (r.IdTipoMovimiento.equals("MOVCAR")) {
                codigo = r.Resultado.equals(RESULTADO_ANULADO) ? "UI_CARGO_ANULADO" : "UI_CARGO_INGRESADO";
            } else if (r.IdTipoMovimiento.equals("MOVABN")) {
                codigo = r.Resultado.equals(RESULTADO_ANULADO) ? "UI_ABONO_ANULADO" : "UI_ABONO_INGRESADO";
            }
            // LEGACY DEFECT kept (see README CTA-A-108): the counter-movement result (CRT) has no message type, so it renders with an empty tipo
            String tipo = switch (r.Resultado) {
                case "OK", RESULTADO_ANULADO -> MensajeSalida.INFORMACION;
                case "ADVE" -> MensajeSalida.ADVERTENCIA;
                case RESULTADO_REQUIERE_AUTORIZACION -> MensajeSalida.AUTORIZACION;
                case RESULTADO_RECHAZADO -> MensajeSalida.ERROR;
                default -> "";
            };
            Map<String, String> valores = new HashMap<>();
            valores.put("IdCargoAbonoMovimiento", String.valueOf(r.IdCargoAbonoMovimiento));
            out.P_OUT_MENSAJES.add(new MensajeSalida(r.IdCargoAbonoMovimiento, tipo, codigo, diccionario.traducir(in.p_IdEmpresa, codigo, valores)));

            String tipoRegistro = r.IdTipoMovimiento != null ? r.IdTipoMovimiento : "CargoAbonoMovimiento";
            AgregarLog.InAgregarLog.Log cabecera = new AgregarLog.InAgregarLog.Log();
            cabecera.IdEmpresa = in.p_IdEmpresa;
            cabecera.IdModulo = in.p_IdModulo;
            cabecera.IdUsuario = in.p_IdUsuario;
            cabecera.IdTipoLog = r.Resultado.equals(RESULTADO_ANULADO) ? "ANULACION" : "EDICION";
            cabecera.IdCabeceraLog = r.IdCargoAbonoMovimiento;
            cabecera.FechaSistemaLog = in.p_FechaMovimiento;
            cabecera.IdUsuarioAtendido = in.p_IdUsuario;
            cabecera.IdTipoRegistro = tipoRegistro;
            cabecera.Folio = r.IdCargoAbonoMovimiento;
            cabecera.IdCliente = r.IdCliente != null ? r.IdCliente : 0L;
            cabecera.IdPortafolio = r.IdPortafolio != null ? r.IdPortafolio : 0L;
            cabecera.Monto = r.Monto != null ? r.Monto : BigDecimal.ZERO;
            cabecera.IdTipoOperacion = r.IdTipoOperacion != null ? r.IdTipoOperacion : "";
            registro.p_Log.add(cabecera);

            AgregarLog.InAgregarLog.DetalleLog detalle = new AgregarLog.InAgregarLog.DetalleLog();
            detalle.IdCabeceraLog = r.IdCargoAbonoMovimiento;
            detalle.SubRegistro = tipoRegistro;
            detalle.CampoRegistro = "IdEstado";
            detalle.ValorAntiguo = "CRT";
            detalle.ValorNuevo = "ANU";
            registro.p_LogDetalle.add(detalle);
        }
        colaboradores.invocar(AgregarLog.CLASE, registro, AgregarLog.OutAgregarLog.class);
    }

    /**
     * Business days between the dates (legacy platform ObtenerPlazo): walks back from FechaHasta to FechaDesde, skipping non business
     * days when SoloDiasHabiles = S, and stores the result in the plazo table that the counter-movement query joins.
     */
    private void registrarPlazos(List<FechaPlazo> fechas, Long idPais) {
        Set<LocalDate> fechasFeriado = new HashSet<>();
        Set<Integer> diasInhabiles = new HashSet<>();
        for (Map<String, Object> f : jdbc.queryForList(SQL_FERIADOS, new MapSqlParameterSource("IdPais", idPais))) {
            String tipo = String.valueOf(f.get("Tipo"));
            if ("FERIADO".equalsIgnoreCase(tipo) && f.get("Fecha") != null) {
                fechasFeriado.add(((java.sql.Date) f.get("Fecha")).toLocalDate());
            } else if ("INHABIL".equalsIgnoreCase(tipo) && f.get("Dia") != null) {
                diasInhabiles.add(((Number) f.get("Dia")).intValue());
            }
        }
        for (FechaPlazo f : fechas) {
            LocalDate desde = Fecha.toDate(f.FechaDesde).toLocalDate();
            LocalDate hasta = Fecha.toDate(f.FechaHasta).toLocalDate();
            long dias = 0;
            LocalDate pivote = hasta;
            boolean buscar = desde.isBefore(hasta);
            while (buscar) {
                pivote = pivote.minusDays(1);
                dias++;
                if (f.SoloDiasHabiles.equals("S")) {
                    // Optimus week days: Monday = 0 .. Sunday = 6
                    while (fechasFeriado.contains(pivote) || diasInhabiles.contains(pivote.getDayOfWeek().getValue() - 1)) {
                        pivote = pivote.minusDays(1);
                    }
                }
                if (!desde.isBefore(pivote)) {
                    break;
                }
            }
            f.Plazo = dias;
        }
        jdbc.batchUpdate(SQL_INSERT_PLAZO, fechas.stream()
                .map(f -> (SqlParameterSource) new MapSqlParameterSource().addValue("IdInterno", f.IdInterno)
                        .addValue("FechaDesde", f.FechaDesde).addValue("FechaHasta", f.FechaHasta)
                        .addValue("SoloDiasHabiles", f.SoloDiasHabiles).addValue("Plazo", f.Plazo))
                .toArray(SqlParameterSource[]::new));
    }
}
