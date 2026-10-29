package cl.bcs.tesoreria.javacall;

import static cl.bcs.tesoreria.javacall.TesoreriaJdbcHelper.loadSql;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonObject;

import cl.bcs.tesoreria.javacall.comun.Colaborador;
import cl.bcs.tesoreria.javacall.comun.Consultas;
import cl.bcs.tesoreria.javacall.comun.Filas;
import cl.bcs.tesoreria.javacall.comun.JsonLegacy;
import cl.bcs.tesoreria.javacall.comun.MensajeSalida;
import cl.bcs.tesoreria.javacall.comun.Parametros;
import cl.bcs.tesoreria.javacall.contrato.AgregarLog;
import cl.bcs.tesoreria.port.out.Colaboradores;
import cl.bcs.tesoreria.port.out.ConfiguracionSistema;
import cl.bcs.tesoreria.port.out.DiccionarioMensajes;
import cl.bcs.utils.BcsPojo;

/**
 * Legacy {@code cl.bcs.tesoreria.CuadraturaCierreDiario}, behaviours TES-A-700..: end-of-day reconciliation of the treasury operations.
 * Mode {@code Proceso} recomputes the requested reconciliations (payment forms still to certify, ingresos/egresos without comprobante) and stores
 * the result; mode {@code Consulta} returns the stored detail JSON. The log entry is written through the {@code AgregarLog} collaborator and its
 * failure never fails the process.
 */
@Service
public class CuadraturaCierreDiarioService {

    private static final Logger log = LoggerFactory.getLogger(CuadraturaCierreDiarioService.class);

    private static final String SQL_CUADRATURA = loadSql("sql/CuadraturaCierreDiario_SelectCuadraturaDiariaOperaciones.sql");
    private static final String SQL_FORMAS_DE_PAGO = loadSql("sql/CuadraturaCierreDiario_SelectFormasDePago.sql");
    private static final String SQL_INGRESO_EGRESO = loadSql("sql/CuadraturaCierreDiario_SelectIngresoEgreso.sql");
    private static final String SQL_BORRAR = loadSql("sql/CuadraturaCierreDiario_DeleteCuadraturaDiariaOperaciones.sql");
    private static final String SQL_INSERTAR = loadSql("sql/CuadraturaCierreDiario_InsertCuadraturaDiariaOperaciones.sql");
    private static final String SQL_DETALLE = loadSql("sql/CuadraturaCierreDiario_SelectJSONCuadraturaDiariaOperaciones.sql");

    private static final String PROCESO_CERTIFICACION = "CUAMOVCERT";
    private static final String PROCESO_SIN_COMPROBANTE = "CUAMOVSINCOMP";

    // ------------------------------------------------------------------ JavaCall contract (legacy names)

    public static class CuadraturaDiariaObj extends BcsPojo {
        public String IdCuadraturaDiariaOperaciones;
    }

    public static class CuadraturaCierreDiarioEntrada extends BcsPojo {
        public Long p_IdUsuario;
        public Long p_IdEmpresa;
        public String p_IdModulo;
        public Date p_FechaInicio;
        public List<CuadraturaDiariaObj> p_IdProceso = new ArrayList<>();
        /** {@code Proceso} or {@code Consulta}. */
        public String p_TipoLlamado;
    }

    public static class DetalleResumenJSON extends BcsPojo {
        public String IdCuadraturaDiariaOperaciones;
        public String DetalleCuadraturaJSON;
    }

    public static class Resumen extends BcsPojo {
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

    public static class FormaDePago extends BcsPojo {
        public Long IdComprobante;
        public Long IdComprobanteFormaDePago;
        public Date FechaLiquidacion;
        public String NombreConcepto;
        public String NombreMedioDePago;
        public String NombreTipoMovimiento;
        public String IdMoneda;
        public BigDecimal Monto;
        public String IdEstado;
        public String Descripcion;
    }

    public static class IngresoEgreso extends BcsPojo {
        public Long IdIngresoEgreso;
        public Date FechaLiquidacion;
        public String NombreConcepto;
        public String NombreMedioDePago;
        public String NombreTipoMovimiento;
        public String IdMoneda;
        public BigDecimal Monto;
        public String IdEstado;
        public String Descripcion;
    }

    public static class CuadraturaCierreDiarioSalida extends BcsPojo {
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR;
        public String P_OUT_DETALLE_CUADRATURA;
    }

    private final NamedParameterJdbcTemplate jdbc;
    private final ConfiguracionSistema configuracion;
    private final DiccionarioMensajes diccionario;
    private final Colaboradores colaboradores;

    public CuadraturaCierreDiarioService(NamedParameterJdbcTemplate jdbc, ConfiguracionSistema configuracion, DiccionarioMensajes diccionario,
            Colaboradores colaboradores) {
        this.jdbc = jdbc;
        this.configuracion = configuracion;
        this.diccionario = diccionario;
        this.colaboradores = colaboradores;
    }

    /** Legacy {@code call}: the error counter is recounted from the messages. */
    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject entrada) throws Exception {
        CuadraturaCierreDiarioEntrada in = JsonLegacy.PLATAFORMA.fromJson(entrada, CuadraturaCierreDiarioEntrada.class);
        CuadraturaCierreDiarioSalida out = logica(in);
        out.P_OUT_ERRORESCONTADOR = MensajeSalida.numeroErrores(out.P_OUT_MENSAJES);
        return JsonLegacy.PLATAFORMA.toJsonTree(out).getAsJsonObject();
    }

    /** Legacy {@code logica}; any failure propagates (the transaction is rolled back by the caller). */
    @Transactional(rollbackFor = Exception.class)
    public CuadraturaCierreDiarioSalida logica(CuadraturaCierreDiarioEntrada in) throws Exception {
        CuadraturaCierreDiarioSalida out = new CuadraturaCierreDiarioSalida();
        out.P_OUT_ERRORESCONTADOR = 0L;
        if (in.p_TipoLlamado.equals("Proceso")) {
            procesar(in, out);
        } else if (in.p_TipoLlamado.equals("Consulta")) {
            consultar(in, out);
        }
        return out;
    }

    private void procesar(CuadraturaCierreDiarioEntrada in, CuadraturaCierreDiarioSalida out) {
        List<Resumen> resumenes = jdbc.query(SQL_CUADRATURA,
                Map.of("IdEmpresa", in.p_IdEmpresa, "IdCuadraturaDiariaOperaciones", Consultas.enLista(idsDeProceso(in))), Filas.de(Resumen.class));

        List<Map<String, Object>> aBorrar = new ArrayList<>();
        for (CuadraturaDiariaObj proceso : in.p_IdProceso) {
            switch (proceso.IdCuadraturaDiariaOperaciones) {
                case PROCESO_CERTIFICACION -> {
                    List<FormaDePago> formas = jdbc.query(SQL_FORMAS_DE_PAGO, Map.of("FechaLiquidacion", in.p_FechaInicio), Filas.de(FormaDePago.class));
                    List<FormaDePago> pendientes = formas.stream().filter(f -> "ING".equalsIgnoreCase(f.IdEstado)).peek(f -> f.Descripcion = "NO CERTIFICADO").toList();
                    registrar(in, proceso, resumenes, formas.size(), pendientes.size(), pendientes);
                    aBorrar.add(Map.of("IdCuadraturaDiariaOperaciones", proceso.IdCuadraturaDiariaOperaciones));
                }
                case PROCESO_SIN_COMPROBANTE -> {
                    List<IngresoEgreso> movimientos = jdbc.query(SQL_INGRESO_EGRESO, Map.of("FechaLiquidacion", in.p_FechaInicio), Filas.de(IngresoEgreso.class));
                    List<IngresoEgreso> pendientes = movimientos.stream().filter(m -> "ING".equalsIgnoreCase(m.IdEstado)).peek(m -> m.Descripcion = "SIN COMPROBANTE").toList();
                    registrar(in, proceso, resumenes, movimientos.size(), pendientes.size(), pendientes);
                    aBorrar.add(Map.of("IdCuadraturaDiariaOperaciones", proceso.IdCuadraturaDiariaOperaciones));
                }
                default -> {
                    // an unknown reconciliation is ignored
                }
            }
        }

        if (!aBorrar.isEmpty()) {
            // LEGACY DEFECT kept (see README CuadraturaCierreDiario): the delete runs once per requested process id, and ALL the rows read are
            // inserted back, also those of reconciliations that were not processed (they were not deleted)
            Consultas.lote(jdbc, SQL_BORRAR, aBorrar);
            Consultas.lote(jdbc, SQL_INSERTAR, resumenes.stream().map(CuadraturaCierreDiarioService::fila).toList());
        }
        out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "UI_PROCESO_EJECUTADO_EXITO",
                diccionario.mensaje(in.p_IdEmpresa, "UI_PROCESO_EJECUTADO_EXITO", new HashMap<>())));
        agregarLog(in);
    }

    /** Stores the outcome of one reconciliation in the summary row that was read for it. */
    private static void registrar(CuadraturaCierreDiarioEntrada in, CuadraturaDiariaObj proceso, List<Resumen> resumenes, int total, int conProblemas,
            List<?> detalle) {
        String resultado = total == 0 ? "SIN OPERACIONES" : conProblemas == 0 ? "CUADRADO" : "DESCUADRADO";
        for (Resumen r : resumenes) {
            if (r.IdCuadraturaDiariaOperaciones.equals(proceso.IdCuadraturaDiariaOperaciones)) {
                r.FechaEjecucion = in.p_FechaInicio;
                r.TotalOperaciones = (long) total;
                r.OperacionesConProblemas = (long) conProblemas;
                r.Resultado = resultado;
                r.DetalleCuadraturaJSON = JsonLegacy.ENTRADA_SALIDA.toJson(detalle);
                r.IdUsuario = in.p_IdUsuario;
            }
        }
    }

    private static Map<String, Object> fila(Resumen r) {
        Map<String, Object> f = new LinkedHashMap<>();
        f.put("IdCuadraturaDiariaOperaciones", r.IdCuadraturaDiariaOperaciones);
        f.put("IdEmpresa", r.IdEmpresa);
        f.put("OperacionCuadraturaDiariaOperaciones", r.OperacionCuadraturaDiariaOperaciones);
        f.put("FechaEjecucion", r.FechaEjecucion);
        f.put("TotalOperaciones", r.TotalOperaciones);
        f.put("OperacionesConProblemas", r.OperacionesConProblemas);
        f.put("TipoOperacion", r.TipoOperacion);
        f.put("Resultado", r.Resultado);
        f.put("Orden", r.Orden);
        f.put("DetalleCuadraturaJSON", r.DetalleCuadraturaJSON);
        f.put("IdUsuario", r.IdUsuario);
        return f;
    }

    private void consultar(CuadraturaCierreDiarioEntrada in, CuadraturaCierreDiarioSalida out) {
        List<DetalleResumenJSON> detalles = jdbc.query(SQL_DETALLE,
                Map.of("IdEmpresa", in.p_IdEmpresa, "IdCuadraturaDiariaOperaciones", Consultas.enLista(idsDeProceso(in))), Filas.de(DetalleResumenJSON.class));
        if (!detalles.isEmpty()) {
            out.P_OUT_DETALLE_CUADRATURA = detalles.get(0).DetalleCuadraturaJSON;
        }
    }

    private static List<String> idsDeProceso(CuadraturaCierreDiarioEntrada in) {
        List<String> ids = new ArrayList<>();
        for (CuadraturaDiariaObj p : in.p_IdProceso) {
            ids.add(p.IdCuadraturaDiariaOperaciones);
        }
        return ids;
    }

    /** Writes the informative log entry; a failure (including a missing system date) is only logged, as in the legacy. */
    private void agregarLog(CuadraturaCierreDiarioEntrada in) {
        try {
            String fechaSistema = Parametros.texto(configuracion, in.p_IdEmpresa, Parametros.PLATAFORMA, "PLA.FechaSistema");
            AgregarLog.InAgregarLog entrada = new AgregarLog.InAgregarLog();
            AgregarLog.InAgregarLog.Log l = new AgregarLog.InAgregarLog.Log();
            l.IdCabeceraLog = in.p_IdUsuario;
            l.IdModulo = "TES";
            l.IdUsuario = in.p_IdUsuario;
            l.IdEmpresa = in.p_IdEmpresa;
            l.FechaSistemaLog = fechaSistema;
            l.IdUsuarioAtendido = 0L;
            l.IdTipoRegistro = "CUADRATURACIERREDIARIO";
            l.EstadoProceso = "ING";
            l.IdTipoLog = "INFORMATIVO";
            entrada.p_Log.add(l);
            Colaborador.invocar(colaboradores, "AgregarLog", entrada, AgregarLog.OutAgregarLog.class);
        } catch (Exception e) {
            log.warn("CuadraturaCierreDiario: the log entry could not be written", e);
        }
    }
}
