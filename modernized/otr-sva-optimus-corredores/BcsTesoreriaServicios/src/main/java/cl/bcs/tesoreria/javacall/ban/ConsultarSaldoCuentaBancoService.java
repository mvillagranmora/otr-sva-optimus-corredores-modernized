package cl.bcs.tesoreria.javacall.ban;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.tesoreria.javacall.kit.JsonLegacy;
import cl.bcs.tesoreria.javacall.kit.MensajeSalida;
import cl.bcs.tesoreria.javacall.kit.Plataforma;
import cl.bcs.tesoreria.javacall.kit.SqlNombrado;
import cl.bcs.tesoreria.javacall.kit.colaborador.RequisitosBasicos;
import cl.bcs.tesoreria.port.out.Colaboradores;
import cl.bcs.tesoreria.port.out.ConfiguracionSistema;
import cl.bcs.tesoreria.port.out.DiccionarioMensajes;

/**
 * Legacy {@code cl.bcs.tesoreria.ban.ConsultarSaldoCuentaBanco} (TES-B-01..08): balances (available T0, pending, accounting, projected T1..T3) of the
 * brokerage bank accounts for each day of a date range.
 *
 * <p>The accounts to consult and the date ranges are staged in the work tables of the legacy statement set. The three optional switches
 * (pending / accounting / future balances) fall back to the TESORERIA configuration parameters when the request leaves them null.
 */
@Service
public class ConsultarSaldoCuentaBancoService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarSaldoCuentaBancoService.class);

    private static final String SQL_INSERT_CUENTAS_A_CONSULTAR = SqlNombrado.recurso("ConsultarSaldoCuentaBanco_InsertCuentasAConsultar");
    private static final String SQL_SELECT_CUENTA_BANCO = SqlNombrado.recurso("ConsultarSaldoCuentaBanco_SelectCuentaBanco");
    private static final String SQL_INSERT_RANGO_FECHA_CUENTA_BANCO = SqlNombrado.recurso("ConsultarSaldoCuentaBanco_RangoFechaCuentaBanco");
    private static final String SQL_SELECT_CALCULA_SALDO_DISPONIBLE = SqlNombrado.recurso("ConsultarSaldoCuentaBanco_CalculaSaldoDisponible");
    private static final String SQL_SELECT_CALCULA_SALDO_PENDIENTE = SqlNombrado.recurso("ConsultarSaldoCuentaBanco_CalculaSaldoPendiente");
    private static final String SQL_SELECT_CALCULA_SALDO_PROYECTADO = SqlNombrado.recurso("ConsultarSaldoCuentaBanco_CalculaSaldoProyectado");

    // ---- JSON contract (legacy names)

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;
        public String p_FechaDesde;
        public String p_FechaHasta;
        /** S|N|null; null: the configuration parameter decides. */
        public String p_IncluyeSaldosPendientes;
        public String p_IncluyeSaldosContables;
        public String p_IncluyeSaldosFuturo;
        public List<CuentaAConsultar> p_CuentasAConsultar = new ArrayList<>();
    }

    /** Account filter; also the parameter object of the staging INSERT. */
    public static class CuentaAConsultar {
        public Long IdCuentaBanco;
        public Long IdBanco;
        public String IdMoneda;
    }

    public static class Salida {
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR;
        public List<SaldoCuentaBanco> P_OUT_SALDOCUENTABANCO = new ArrayList<>();
    }

    public static class SaldoCuentaBanco {
        public Long IdCuentaBanco;
        public String NombreCuentaBanco;
        public Long IdBanco;
        public String NombreBanco;
        public String NumeroCuentaBancaria;
        public String IdMoneda;
        public String NombreMoneda;
        public String FechaSaldo;
        public BigDecimal SaldoDisponibleInicial;
        public BigDecimal SaldoDisponibleIngreso;
        public BigDecimal SaldoDisponibleEgreso;
        public BigDecimal SaldoDisponible;
        public BigDecimal SaldoPendienteIngreso;
        public BigDecimal SaldoPendienteEgreso;
        public BigDecimal SaldoPendiente;
        public BigDecimal SaldoContableIngreso;
        public BigDecimal SaldoContableEgreso;
        public BigDecimal SaldoContable;
        public String FechaT1;
        public BigDecimal SaldoIngresoT1;
        public BigDecimal SaldoEgresoT1;
        public BigDecimal SaldoT1;
        public String FechaT2;
        public BigDecimal SaldoIngresoT2;
        public BigDecimal SaldoEgresoT2;
        public BigDecimal SaldoT2;
        public String FechaT3;
        public BigDecimal SaldoIngresoT3;
        public BigDecimal SaldoEgresoT3;
        public BigDecimal SaldoT3;
    }

    // ---- statement rows

    public static class CuentaBanco {
        public Long IdCuentaBanco;
        public Long IdBanco;
        public String NombreBanco;
        public String NumeroCuentaBancaria;
        public String NombreCuentaBanco;
        public String IdMoneda;
        public String NombreMoneda;
    }

    /** Day of an account to compute (also the parameter object of the staging INSERT). */
    public record RangoFechaCuentaBanco(Long IdCuentaBanco, String FechaSaldo, Integer Orden) {
    }

    public record SaldoDisponible(Long IdCuentaBanco, String FechaSaldo, BigDecimal SaldoDisponibleInicial, BigDecimal SaldoDisponibleEgreso,
            BigDecimal SaldoDisponibleIngreso, BigDecimal SaldoDisponible) {
    }

    public record SaldoPendiente(Long IdCuentaBanco, String FechaSaldo, BigDecimal SaldoPendienteEgreso, BigDecimal SaldoPendienteIngreso,
            BigDecimal SaldoPendiente) {
    }

    public record SaldoContable(Long IdCuentaBanco, String FechaSaldo, BigDecimal SaldoContableEgreso, BigDecimal SaldoContableIngreso,
            BigDecimal SaldoContable) {
    }

    public record SaldoProyectado(Long IdCuentaBanco, String FechaSaldo, String FechaT1, BigDecimal SaldoEgresoT1, BigDecimal SaldoIngresoT1,
            BigDecimal SaldoT1, String FechaT2, BigDecimal SaldoEgresoT2, BigDecimal SaldoIngresoT2, BigDecimal SaldoT2, String FechaT3,
            BigDecimal SaldoEgresoT3, BigDecimal SaldoIngresoT3, BigDecimal SaldoT3) {
    }

    /** Key of the per-account, per-day balance maps. */
    private record CuentaDia(Long idCuentaBanco, String fechaSaldo) {
    }

    private final SqlNombrado sql;
    private final DiccionarioMensajes diccionario;
    private final ConfiguracionSistema configuracion;
    private final Colaboradores colaboradores;

    public ConsultarSaldoCuentaBancoService(NamedParameterJdbcTemplate jdbc, DiccionarioMensajes diccionario, ConfiguracionSistema configuracion,
            Colaboradores colaboradores) {
        this.sql = new SqlNombrado(jdbc);
        this.diccionario = diccionario;
        this.configuracion = configuracion;
        this.colaboradores = colaboradores;
    }

    /** Writes only the work tables of the query; a transaction keeps them together with the shared connection of the call. */
    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject jin) throws Exception {
        Gson gson = JsonLegacy.gson();
        Entrada in = gson.fromJson(jin.toString(), Entrada.class);
        Salida out = logica(in);
        out.P_OUT_ERRORESCONTADOR = MensajeSalida.contarErrores(out.P_OUT_MENSAJES);
        return JsonParser.parseString(gson.toJson(out)).getAsJsonObject();
    }

    Salida logica(Entrada in) throws Exception {
        Salida out = new Salida();
        log.debug("== INICIO CONSULTA SALDO CUENTA BANCO ==");

        // LEGACY DEFECT kept (see README TES-B-01): the dates are parsed with a lenient SimpleDateFormat (2025-02-31 becomes 2025-03-03)
        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");
        Date fechaDesde = formato.parse(in.p_FechaDesde);
        Date fechaHasta = formato.parse(in.p_FechaHasta);

        RequisitosBasicos.Salida basicos = RequisitosBasicos.revisar(colaboradores, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
        if (MensajeSalida.contarErrores(basicos.p_out_Mensajes) > 0) {
            out.P_OUT_MENSAJES.addAll(basicos.p_out_Mensajes);
            return out;
        }

        String incluyePendientes = in.p_IncluyeSaldosPendientes == null
                ? Plataforma.parametro(configuracion, in.p_IdEmpresa, "TESORERIA", "CONSULTASALDOBANCOINCLUYESALDOSPENDIENTE") : in.p_IncluyeSaldosPendientes;
        String incluyeContables = in.p_IncluyeSaldosContables == null
                ? Plataforma.parametro(configuracion, in.p_IdEmpresa, "TESORERIA", "CONSULTASALDOBANCOINCLUYESALDOSCONTABLES") : in.p_IncluyeSaldosContables;
        String incluyeFuturos = in.p_IncluyeSaldosFuturo == null
                ? Plataforma.parametro(configuracion, in.p_IdEmpresa, "TESORERIA", "CONSULTASALDOBANCOINCLUYESALDOSFUTUROS") : in.p_IncluyeSaldosFuturo;

        sql.lote(SQL_INSERT_CUENTAS_A_CONSULTAR, in.p_CuentasAConsultar);
        Map<String, Object> params = new HashMap<>();
        params.put("IdEmpresa", in.p_IdEmpresa);
        List<CuentaBanco> cuentas = sql.consultar(SQL_SELECT_CUENTA_BANCO, params, CuentaBanco.class);
        if (cuentas.isEmpty()) {
            out.P_OUT_MENSAJES.add(Plataforma.mensaje(diccionario, in.p_IdEmpresa, 0L, MensajeSalida.MENSAJE_ERROR, "NO_EXISTEN_CUENTAS_BANCARIAS", null));
            return out;
        }
        Map<Long, CuentaBanco> cuentaPorId = new HashMap<>();
        for (CuentaBanco c : cuentas) {
            if (cuentaPorId.containsKey(c.IdCuentaBanco)) {
                throw new Exception("key field is not unique");
            }
            cuentaPorId.put(c.IdCuentaBanco, c);
        }

        // days to compute for every account
        List<RangoFechaCuentaBanco> rango = new ArrayList<>();
        List<Date> dias = generarRangoFechas(fechaDesde, fechaHasta);
        for (CuentaBanco cuenta : cuentas) {
            for (Date dia : dias) {
                rango.add(new RangoFechaCuentaBanco(cuenta.IdCuentaBanco, formato.format(dia), 0));
            }
        }
        sql.lote(SQL_INSERT_RANGO_FECHA_CUENTA_BANCO, rango);

        // available balance (T0)
        List<Long> idsCuentas = new ArrayList<>();
        for (CuentaBanco cuenta : cuentas) {
            idsCuentas.add(cuenta.IdCuentaBanco);
        }
        params.clear();
        params.put("IdCuentaBanco", idsCuentas);
        params.put("FechaDesde", in.p_FechaDesde);
        List<SaldoDisponible> disponibles = sql.consultar(SQL_SELECT_CALCULA_SALDO_DISPONIBLE, params, SaldoDisponible.class);

        // pending balance
        Map<CuentaDia, SaldoPendiente> pendientes = new HashMap<>();
        if (incluyePendientes.equals("S")) {
            for (SaldoPendiente s : sql.consultar(SQL_SELECT_CALCULA_SALDO_PENDIENTE, Map.of(), SaldoPendiente.class)) {
                pendientes.put(new CuentaDia(s.IdCuentaBanco(), s.FechaSaldo()), s);
            }
        }

        // accounting balance = available + pending
        Map<CuentaDia, SaldoContable> contables = new HashMap<>();
        if (incluyeContables.equals("S")) {
            List<SaldoContable> lista = new ArrayList<>();
            for (SaldoDisponible disp : disponibles) {
                // LEGACY DEFECT kept (see README TES-B-03): with pending balances switched off the map is empty and this dereference is a NullPointerException
                SaldoPendiente pen = pendientes.get(new CuentaDia(disp.IdCuentaBanco(), disp.FechaSaldo()));
                lista.add(new SaldoContable(disp.IdCuentaBanco(), disp.FechaSaldo(),
                        disp.SaldoDisponibleEgreso().add(pen.SaldoPendienteEgreso()),
                        disp.SaldoDisponibleIngreso().add(pen.SaldoPendienteIngreso()),
                        disp.SaldoDisponible().add(pen.SaldoPendiente())));
            }
            for (SaldoContable s : lista) {
                contables.put(new CuentaDia(s.IdCuentaBanco(), s.FechaSaldo()), s);
            }
        }

        // projected balances T1..T3
        Map<CuentaDia, SaldoProyectado> proyectados = new HashMap<>();
        if (incluyeFuturos.equals("S")) {
            List<Date> diasProyeccion = generarRangoFechas(agregarDias(fechaDesde, 1), agregarDias(fechaHasta, 5));
            List<RangoFechaCuentaBanco> rangoProyeccion = new ArrayList<>();
            for (CuentaBanco cuenta : cuentas) {
                int orden = 0;
                for (Date dia : diasProyeccion) {
                    orden++;
                    rangoProyeccion.add(new RangoFechaCuentaBanco(cuenta.IdCuentaBanco, formato.format(dia), orden));
                }
            }
            sql.lote(SQL_INSERT_RANGO_FECHA_CUENTA_BANCO, rangoProyeccion);
            for (SaldoProyectado s : sql.consultar(SQL_SELECT_CALCULA_SALDO_PROYECTADO, Map.of(), SaldoProyectado.class)) {
                proyectados.put(new CuentaDia(s.IdCuentaBanco(), s.FechaSaldo()), s);
            }
        }

        // output: one row per available-balance row
        List<SaldoCuentaBanco> salida = new ArrayList<>();
        for (SaldoDisponible disp : disponibles) {
            CuentaBanco cuenta = cuentaPorId.get(disp.IdCuentaBanco());
            CuentaDia clave = new CuentaDia(disp.IdCuentaBanco(), disp.FechaSaldo());
            SaldoPendiente pen = pendientes.get(clave);
            SaldoContable con = contables.get(clave);
            SaldoProyectado pro = proyectados.get(clave);
            SaldoCuentaBanco s = new SaldoCuentaBanco();
            s.IdCuentaBanco = cuenta.IdCuentaBanco;
            s.NombreCuentaBanco = cuenta.NombreCuentaBanco;
            s.IdBanco = cuenta.IdBanco;
            s.NombreBanco = cuenta.NombreBanco;
            s.NumeroCuentaBancaria = cuenta.NumeroCuentaBancaria;
            s.IdMoneda = cuenta.IdMoneda;
            s.NombreMoneda = cuenta.NombreMoneda;
            s.FechaSaldo = disp.FechaSaldo();
            s.SaldoDisponibleInicial = disp.SaldoDisponibleInicial();
            s.SaldoDisponibleIngreso = disp.SaldoDisponibleIngreso();
            s.SaldoDisponibleEgreso = disp.SaldoDisponibleEgreso();
            s.SaldoDisponible = disp.SaldoDisponible();
            s.SaldoPendienteIngreso = pen == null ? BigDecimal.ZERO : pen.SaldoPendienteIngreso();
            s.SaldoPendienteEgreso = pen == null ? BigDecimal.ZERO : pen.SaldoPendienteEgreso();
            s.SaldoPendiente = pen == null ? BigDecimal.ZERO : pen.SaldoPendiente();
            s.SaldoContableIngreso = con == null ? BigDecimal.ZERO : con.SaldoContableIngreso();
            s.SaldoContableEgreso = con == null ? BigDecimal.ZERO : con.SaldoContableEgreso();
            s.SaldoContable = con == null ? BigDecimal.ZERO : con.SaldoContable();
            s.FechaT1 = pro == null ? "0001-01-01" : pro.FechaT1();
            s.SaldoIngresoT1 = pro == null ? BigDecimal.ZERO : pro.SaldoIngresoT1();
            s.SaldoEgresoT1 = pro == null ? BigDecimal.ZERO : pro.SaldoEgresoT1();
            s.SaldoT1 = pro == null ? BigDecimal.ZERO : pro.SaldoT1();
            s.FechaT2 = pro == null ? "0001-01-01" : pro.FechaT2();
            s.SaldoIngresoT2 = pro == null ? BigDecimal.ZERO : pro.SaldoIngresoT2();
            s.SaldoEgresoT2 = pro == null ? BigDecimal.ZERO : pro.SaldoEgresoT2();
            s.SaldoT2 = pro == null ? BigDecimal.ZERO : pro.SaldoT2();
            s.FechaT3 = pro == null ? "0001-01-01" : pro.FechaT3();
            s.SaldoIngresoT3 = pro == null ? BigDecimal.ZERO : pro.SaldoIngresoT3();
            s.SaldoEgresoT3 = pro == null ? BigDecimal.ZERO : pro.SaldoEgresoT3();
            s.SaldoT3 = pro == null ? BigDecimal.ZERO : pro.SaldoT3();
            salida.add(s);
        }
        out.P_OUT_SALDOCUENTABANCO = salida;
        log.debug("== TERMINO CONSULTA SALDO CUENTA BANCO ==");
        return out;
    }

    /** Every day from {@code inicio} to {@code fin}, both included. */
    static List<Date> generarRangoFechas(Date inicio, Date fin) {
        Calendar c1 = Calendar.getInstance();
        c1.setTime(inicio);
        Calendar c2 = Calendar.getInstance();
        c2.setTime(fin);
        List<Date> rango = new ArrayList<>();
        while (!c1.after(c2)) {
            rango.add(c1.getTime());
            c1.add(Calendar.DAY_OF_MONTH, 1);
        }
        return rango;
    }

    static Date agregarDias(Date fecha, int dias) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(fecha);
        cal.add(Calendar.DATE, dias);
        return cal.getTime();
    }
}
