package cl.bcs.tesoreria.javacall;

import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.tesoreria.javacall.ban.ActualizarSaldosCuentaBancoService;
import cl.bcs.tesoreria.javacall.ban.ActualizarSaldosCuentaBancoService.CuentaBancoEntrada;
import cl.bcs.tesoreria.javacall.kit.JsonLegacy;
import cl.bcs.tesoreria.javacall.kit.MensajeSalida;
import cl.bcs.tesoreria.javacall.kit.Plataforma;
import cl.bcs.tesoreria.javacall.kit.SqlNombrado;
import cl.bcs.tesoreria.port.out.DiccionarioMensajes;

/**
 * Legacy {@code cl.bcs.tesoreria.ActualizarSaldosCuentaBancoCorredora} (TES-B-211..213): refreshes the balances of EVERY bank account of the
 * brokerage by delegating to {@link ActualizarSaldosCuentaBancoService}, and reports success when there were no errors.
 */
@Service
public class ActualizarSaldosCuentaBancoCorredoraService {

    private static final String SQL_SELECT_CUENTAS_BANCARIAS = SqlNombrado.recurso("ActualizarSaldosCuentaBancoCorredora_SelectCuentasBancarias");

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;
        public String p_FechaMovimiento;
    }

    public static class Salida {
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR = 0L;
    }

    private final SqlNombrado sql;
    private final DiccionarioMensajes diccionario;
    private final ActualizarSaldosCuentaBancoService actualizarSaldos;

    public ActualizarSaldosCuentaBancoCorredoraService(NamedParameterJdbcTemplate jdbc, DiccionarioMensajes diccionario,
            ActualizarSaldosCuentaBancoService actualizarSaldos) {
        this.sql = new SqlNombrado(jdbc);
        this.diccionario = diccionario;
        this.actualizarSaldos = actualizarSaldos;
    }

    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject jin) throws Exception {
        Gson gson = JsonLegacy.gson();
        Entrada in = gson.fromJson(jin.toString(), Entrada.class);
        Salida out = logica(in);
        out.P_OUT_ERRORESCONTADOR = MensajeSalida.contarErrores(out.P_OUT_MENSAJES);
        return JsonParser.parseString(gson.toJson(out)).getAsJsonObject();
    }

    Salida logica(Entrada entrada) throws Exception {
        Salida out = new Salida();
        List<CuentaBancoEntrada> cuentas = sql.consultar(SQL_SELECT_CUENTAS_BANCARIAS, CuentaBancoEntrada.class);
        if (!cuentas.isEmpty()) {
            ActualizarSaldosCuentaBancoService.Entrada peticion = new ActualizarSaldosCuentaBancoService.Entrada();
            peticion.p_FechaMovimiento = entrada.p_FechaMovimiento;
            peticion.p_CuentaBanco = cuentas;
            ActualizarSaldosCuentaBancoService.Salida resultado = actualizarSaldos.logica(peticion);
            out.P_OUT_MENSAJES.addAll(resultado.P_OUT_MENSAJES);
            out.P_OUT_ERRORESCONTADOR = MensajeSalida.contarErrores(out.P_OUT_MENSAJES);
            if (out.P_OUT_ERRORESCONTADOR.compareTo(0L) == 0) {
                out.P_OUT_MENSAJES.add(Plataforma.mensaje(diccionario, entrada.p_IdEmpresa, 0L, MensajeSalida.MENSAJE_INFORMACION,
                        "SALDO_CUENTA_BANCO_CORREDORA_ACTUALIZADOS", new java.util.HashMap<>(1)));
            }
        }
        return out;
    }
}
