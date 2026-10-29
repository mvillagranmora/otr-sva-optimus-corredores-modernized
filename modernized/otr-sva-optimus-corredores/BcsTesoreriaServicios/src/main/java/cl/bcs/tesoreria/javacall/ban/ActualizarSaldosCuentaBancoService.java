package cl.bcs.tesoreria.javacall.ban;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import cl.bcs.tesoreria.javacall.kit.SqlNombrado;

/**
 * Legacy {@code cl.bcs.tesoreria.ban.ActualizarSaldosCuentaBanco} (TES-B-204..210): recomputes, for the given bank accounts, the running available
 * balance (opening balance of the previous day + movements) and the accounting balance (pending balance + movements) of every payment form that
 * affects the balance from the given date on, and stores them on {@code TES.ComprobanteFormaDePago}.
 *
 * <p>The account list is staged in the work table of the legacy statement set; it is emptied when the call ends, whether it succeeded or not.
 */
@Service
public class ActualizarSaldosCuentaBancoService {

    private static final Logger log = LoggerFactory.getLogger(ActualizarSaldosCuentaBancoService.class);

    private static final String SQL_SELECT_SALDO_DISPONIBLE_INICIAL = SqlNombrado.recurso("ActualizarSaldosCuentaBanco_SelectSaldoDisponibleInicial");
    private static final String SQL_INSERT_TABLA_TEMPORAL = SqlNombrado.recurso("ActualizarSaldosCuentaBanco_InsertTablaTemporal");
    private static final String SQL_SELECT_SALDO_DISPONIBLE_MOVIMIENTOS = SqlNombrado.recurso("ActualizarSaldosCuentaBanco_SelectSaldoDisponibleMovimientos");
    private static final String SQL_SELECT_SALDO_PENDIENTE = SqlNombrado.recurso("ActualizarSaldosCuentaBanco_SelectSaldoPendiente");
    private static final String SQL_UPDATE_SALDOS = SqlNombrado.recurso("ActualizarSaldosCuentaBanco_UpdateSaldoContable");
    private static final String SQL_DELETE_TABLA_TEMPORAL = SqlNombrado.recurso("ActualizarSaldosCuentaBanco_DeleteTablaTemporal");

    // ---- JSON contract (legacy names) and statement row types

    public static class Entrada {
        public String p_FechaMovimiento;
        public List<CuentaBancoEntrada> p_CuentaBanco = new ArrayList<>();
    }

    /** Account to refresh; also the parameter object of the staging INSERT. */
    public static class CuentaBancoEntrada {
        public Long IdCuentaBanco;
    }

    public static class Salida {
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR;
    }

    public static class SaldoDisponible {
        public Long IdCuentaBanco;
        public Long IdComprobanteFormaDePago;
        public String FechaAfectaSaldoAnterior;
        public BigDecimal SaldoAnterior;
    }

    /** Movement of a payment form; also the parameter object of the UPDATE (SaldoDisponible, SaldoPendiente, IdComprobanteFormaDePago). */
    public static class Movimientos {
        public Long IdComprobanteFormaDePago;
        public Long IdCuentaBanco;
        public String FechaHoraAfectaSaldo;
        public String FechaHoraLiquidacion;
        public BigDecimal Monto;
        public BigDecimal SaldoDisponible;
        public BigDecimal SaldoPendiente;

        Timestamp fechaHoraAfectaSaldo() {
            return Timestamp.valueOf(this.FechaHoraAfectaSaldo);
        }
    }

    public static class SaldoPendiente {
        public Long IdCuentaBanco;
        public BigDecimal SaldoPendiente;
    }

    private final SqlNombrado sql;

    public ActualizarSaldosCuentaBancoService(NamedParameterJdbcTemplate jdbc) {
        this.sql = new SqlNombrado(jdbc);
    }

    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject jin) throws Exception {
        Gson gson = JsonLegacy.gson();
        Entrada in = gson.fromJson(jin.toString(), Entrada.class);
        Salida out = logica(in);
        return JsonParser.parseString(gson.toJson(out)).getAsJsonObject();
    }

    /** Typed entry (also used in process by {@code ActualizarSaldosCuentaBancoCorredoraService}). */
    @Transactional(rollbackFor = Exception.class)
    public Salida logica(Entrada in) throws Exception {
        Salida out = new Salida();
        log.info("== INICIO ACTUALIZAR SALDOS CUENTA BANCO ==");
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("p_FechaMovimiento", in.p_FechaMovimiento);

            sql.lote(SQL_INSERT_TABLA_TEMPORAL, in.p_CuentaBanco);
            List<SaldoDisponible> saldosIniciales = sql.consultar(SQL_SELECT_SALDO_DISPONIBLE_INICIAL, parametros, SaldoDisponible.class);
            List<Movimientos> movimientos = sql.consultar(SQL_SELECT_SALDO_DISPONIBLE_MOVIMIENTOS, parametros, Movimientos.class);
            List<SaldoPendiente> saldosPendientes = sql.consultar(SQL_SELECT_SALDO_PENDIENTE, parametros, SaldoPendiente.class);

            List<Movimientos> porActualizar = new ArrayList<>();
            for (CuentaBancoEntrada cuenta : in.p_CuentaBanco) {
                BigDecimal saldoAnterior = saldosIniciales.stream()
                        .filter(s -> s.IdCuentaBanco.compareTo(cuenta.IdCuentaBanco) == 0)
                        .map(s -> s.SaldoAnterior).findFirst().orElse(BigDecimal.ZERO);
                BigDecimal pendienteAnterior = saldosPendientes.stream()
                        .filter(s -> s.IdCuentaBanco.compareTo(cuenta.IdCuentaBanco) == 0)
                        .map(s -> s.SaldoPendiente).findFirst().orElse(BigDecimal.ZERO);
                List<Movimientos> deLaCuenta = movimientos.stream()
                        .filter(m -> m.IdCuentaBanco.compareTo(cuenta.IdCuentaBanco) == 0)
                        .collect(Collectors.toList());
                if (!deLaCuenta.isEmpty()) {
                    BigDecimal disponible = saldoAnterior;
                    BigDecimal pendiente = pendienteAnterior;
                    deLaCuenta.sort(Comparator.comparing(Movimientos::fechaHoraAfectaSaldo).thenComparing(m -> m.IdComprobanteFormaDePago));
                    for (Movimientos mov : deLaCuenta) {
                        mov.SaldoDisponible = mov.Monto.add(disponible);
                        disponible = mov.SaldoDisponible;
                        mov.SaldoPendiente = mov.Monto.add(pendiente);
                        pendiente = mov.SaldoPendiente;
                        porActualizar.add(mov);
                    }
                }
            }
            if (!porActualizar.isEmpty()) {
                sql.lote(SQL_UPDATE_SALDOS, porActualizar);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_ACTUALIZAR_SALDO_CUENTA_BANCO", "ERROR AL ACTUALIZAR SALDO CUENTA BANCO"));
            throw e;
        } finally {
            sql.ejecutar(SQL_DELETE_TABLA_TEMPORAL);
        }
        out.P_OUT_ERRORESCONTADOR = 0L;
        log.info("== TERMINO ACTUALIZAR SALDOS CUENTA BANCO ==");
        return out;
    }
}
