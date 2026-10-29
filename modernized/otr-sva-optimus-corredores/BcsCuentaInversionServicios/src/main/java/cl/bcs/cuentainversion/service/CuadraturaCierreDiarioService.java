package cl.bcs.cuentainversion.service;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import cl.bcs.cuentainversion.domain.colaborador.AgregarLog;
import cl.bcs.cuentainversion.port.out.Colaboradores;
import cl.bcs.cuentainversion.port.out.ConfiguracionSistemaPort;
import cl.bcs.cuentainversion.port.out.DiccionarioMensajes;
import cl.bcs.cuentainversion.service.CtaJavaCallInfra.MensajeSalida;
import cl.bcs.utils.Utils;

/**
 * End-of-day reconciliation (CTA-A-146..158; legacy CuadraturaCierreDiario). Mode "Proceso" recalculates the requested
 * reconciliation rows (today only CUAMOVCERT: movements of the day still pending certification) and replaces them; mode
 * "Consulta" returns the stored JSON detail of the first requested process.
 */
@Service
public class CuadraturaCierreDiarioService {

    private static final Logger log = LoggerFactory.getLogger(CuadraturaCierreDiarioService.class);

    private static final String SQL = "cl/bcs/cuentainversion/sql/CuadraturaCierreDiario_";
    private static final String SQL_SELECT_CUADRATURA = CtaJavaCallInfra.loadSql(SQL + "SelectCuadraturaDiariaOperaciones.sql");
    private static final String SQL_SELECT_MOVIMIENTOS = CtaJavaCallInfra.loadSql(SQL + "SelectMovimientos.sql");
    private static final String SQL_DELETE_CUADRATURA = CtaJavaCallInfra.loadSql(SQL + "DeleteCuadraturaDiariaOperaciones.sql");
    private static final String SQL_INSERT_CUADRATURA = CtaJavaCallInfra.loadSql(SQL + "InsertCuadraturaDiariaOperaciones.sql");
    private static final String SQL_SELECT_DETALLE = CtaJavaCallInfra.loadSql(SQL + "SelectJSONCuadraturaDiariaOperaciones.sql");

    private static final String PROCESO_MOVIMIENTOS_CERTIFICADOS = "CUAMOVCERT";

    public static class Entrada {
        public Long p_IdUsuario;
        public Long p_IdEmpresa;
        public String p_IdModulo;
        public Date p_FechaInicio;
        public List<Proceso> p_IdProceso = new ArrayList<>();
        public String p_TipoLlamado;
    }

    public static class Proceso {
        public String IdCuadraturaDiariaOperaciones;
    }

    public static class Salida {
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR;
        public String P_OUT_DETALLE_CUADRATURA;
    }

    /** Stored reconciliation row. */
    public static class Resumen {
        public String IdCuadraturaDiariaOperaciones;
        public Long IdEmpresa;
        public String IdClasificacionTipoInstrumento;
        public String OperacionCuadraturaDiariaOperaciones;
        public Date FechaEjecucion;
        public Long TotalOperaciones;
        public Long OperacionesConProblemas;
        public String TipoOperacion;
        public String Resultado;
        public Long Orden;
        public String DetalleCuadraturaJSON;
        public Long IdUsuario;
    }

    public static class Movimiento {
        public Long IdCargoAbonoMovimiento;
        public String NombreCliente;
        public String Proposito;
        public String NombreConcepto;
        public String IdMoneda;
        public BigDecimal Monto;
        public String IdEstado;
        public String Descripcion;
    }

    public static class IdProceso {
        public String IdCuadraturaDiariaOperaciones;
    }

    public static class Detalle {
        public String DetalleCuadraturaJSON;
    }

    private final NamedParameterJdbcTemplate jdbc;
    private final ConfiguracionSistemaPort configuracion;
    private final DiccionarioMensajes diccionario;
    private final Colaboradores colaboradores;

    public CuadraturaCierreDiarioService(NamedParameterJdbcTemplate jdbc, ConfiguracionSistemaPort configuracion,
                                         DiccionarioMensajes diccionario, Colaboradores colaboradores) {
        this.jdbc = jdbc;
        this.configuracion = configuracion;
        this.diccionario = diccionario;
        this.colaboradores = colaboradores;
    }

    @Transactional
    public JsonObject execute(JsonObject params) throws Exception {
        Gson gson = Utils.getGsonBuilder();
        Salida salida = ejecutar(gson.fromJson(params, Entrada.class));
        // the legacy JavaCall entry point recounts the errors of the messages
        salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_MENSAJES.stream().filter(m -> MensajeSalida.ERROR.equals(m.tipo)).count();
        return gson.toJsonTree(salida).getAsJsonObject();
    }

    Salida ejecutar(Entrada in) throws Exception {
        Salida out = new Salida();
        out.P_OUT_ERRORESCONTADOR = 0L;
        try {
            // LEGACY DEFECT kept (see README CTA-A-157): a null p_TipoLlamado throws NullPointerException
            if (in.p_TipoLlamado.equals("Proceso")) {
                procesar(in);
                out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.OK, "UI_PROCESO_EJECUTADO_EXITO",
                        diccionario.traducir(in.p_IdEmpresa, "UI_PROCESO_EJECUTADO_EXITO", new HashMap<>())));
                agregarLog(in);
            } else if (in.p_TipoLlamado.equals("Consulta")) {
                List<Detalle> detalle = jdbc.query(SQL_SELECT_DETALLE, filtro(in), CtaJavaCallInfra.fieldMapper(Detalle.class));
                if (!detalle.isEmpty()) {
                    out.P_OUT_DETALLE_CUADRATURA = detalle.get(0).DetalleCuadraturaJSON;
                }
            }
        } catch (DataAccessException e) {
            // the callers (and the goldens) see the driver's SQLException, not Spring's translation of it
            log.error("CuadraturaCierreDiario", e);
            if (e.getCause() instanceof SQLException sql) {
                throw new SQLException(sql.getMessage(), sql.getSQLState(), sql.getErrorCode(), sql);
            }
            throw e;
        } catch (Exception e) {
            log.error("CuadraturaCierreDiario", e);
            throw e;
        }
        return out;
    }

    private void procesar(Entrada in) {
        List<Resumen> resumenes = jdbc.query(SQL_SELECT_CUADRATURA, filtro(in), CtaJavaCallInfra.fieldMapper(Resumen.class));
        Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
        List<IdProceso> aBorrar = new ArrayList<>();
        for (Proceso proceso : in.p_IdProceso) {
            if (PROCESO_MOVIMIENTOS_CERTIFICADOS.equals(proceso.IdCuadraturaDiariaOperaciones)) {
                List<Movimiento> movimientos = jdbc.query(SQL_SELECT_MOVIMIENTOS,
                        new MapSqlParameterSource("FechaLiquidacion", in.p_FechaInicio), CtaJavaCallInfra.fieldMapper(Movimiento.class));
                List<Movimiento> pendientes = pendientesDeCertificar(movimientos);
                String resultado = movimientos.isEmpty() ? "UI_SIN_OPERACIONES" : pendientes.isEmpty() ? "UI_CUADRADO" : "UI_DESCUADRADO";
                for (Resumen r : resumenes) {
                    if (r.IdCuadraturaDiariaOperaciones.equals(proceso.IdCuadraturaDiariaOperaciones)) {
                        r.FechaEjecucion = in.p_FechaInicio;
                        r.TotalOperaciones = (long) movimientos.size();
                        r.OperacionesConProblemas = (long) pendientes.size();
                        r.Resultado = resultado;
                        r.DetalleCuadraturaJSON = gson.toJson(pendientes);
                        r.IdUsuario = in.p_IdUsuario;
                    }
                }
                IdProceso id = new IdProceso();
                id.IdCuadraturaDiariaOperaciones = proceso.IdCuadraturaDiariaOperaciones;
                aBorrar.add(id);
            }
        }
        if (!aBorrar.isEmpty()) {
            // LEGACY DEFECT kept (see README CTA-A-151/152): the delete ignores IdEmpresa while the select filters it, and rows of
            // unknown processes that were selected are re-inserted without being deleted (primary key violation)
            jdbc.batchUpdate(SQL_DELETE_CUADRATURA, aBorrar.stream().map(CtaJavaCallInfra::fieldSource).toArray(SqlParameterSource[]::new));
            jdbc.batchUpdate(SQL_INSERT_CUADRATURA, resumenes.stream().map(CtaJavaCallInfra::fieldSource).toArray(SqlParameterSource[]::new));
        }
    }

    private static List<Movimiento> pendientesDeCertificar(List<Movimiento> movimientos) {
        List<Movimiento> pendientes = new ArrayList<>();
        for (Movimiento m : movimientos) {
            if ("PEN".equalsIgnoreCase(m.IdEstado)) {
                m.Descripcion = "UI_NO_CERTIFICADO";
                pendientes.add(m);
            }
        }
        return pendientes;
    }

    private static MapSqlParameterSource filtro(Entrada in) {
        List<String> ids = new ArrayList<>();
        for (Proceso p : in.p_IdProceso) {
            ids.add(p.IdCuadraturaDiariaOperaciones);
        }
        // an empty IN list is one NULL parameter (legacy SQL.query)
        return new MapSqlParameterSource("IdEmpresa", in.p_IdEmpresa)
                .addValue("IdCuadraturaDiariaOperaciones", ids.isEmpty() ? Collections.singletonList(null) : ids);
    }

    /** Audit log of the run; a failure writing it never fails the reconciliation (the legacy swallowed it). */
    private void agregarLog(Entrada in) {
        try {
            String fechaSistema = CtaJavaCallInfra.parametro(configuracion, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
            AgregarLog.InAgregarLog entrada = new AgregarLog.InAgregarLog();
            AgregarLog.InAgregarLog.Log l = new AgregarLog.InAgregarLog.Log();
            l.IdCabeceraLog = in.p_IdUsuario;
            l.IdModulo = "CTA";
            l.IdUsuarioAtendido = 0L;
            l.IdUsuario = in.p_IdUsuario;
            l.IdEmpresa = in.p_IdEmpresa;
            l.FechaSistemaLog = fechaSistema;
            l.IdTipoRegistro = "CUADRATURACIERREDIARIO";
            l.EstadoProceso = "ING";
            l.IdTipoLog = "INFORMATIVO";
            entrada.p_Log.add(l);
            colaboradores.invocar(AgregarLog.CLASE, entrada, AgregarLog.OutAgregarLog.class);
        } catch (Exception e) {
            log.error("CuadraturaCierreDiario: audit log failed", e);
        }
    }
}
