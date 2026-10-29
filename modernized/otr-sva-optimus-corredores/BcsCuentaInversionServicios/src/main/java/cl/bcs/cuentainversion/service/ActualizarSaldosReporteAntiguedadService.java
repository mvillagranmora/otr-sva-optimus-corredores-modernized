package cl.bcs.cuentainversion.service;

import cl.bcs.cuentainversion.reportes.CalendarioReportes;
import cl.bcs.cuentainversion.reportes.ConfiguracionSistema;
import cl.bcs.cuentainversion.reportes.JsonReportes;
import cl.bcs.cuentainversion.reportes.MensajeSalida;
import cl.bcs.cuentainversion.reportes.Mensajeria;
import cl.bcs.cuentainversion.reportes.SaldosAntiguedad;
import cl.bcs.cuentainversion.reportes.SaldosAntiguedad.Movimiento;
import cl.bcs.cuentainversion.reportes.SaldosAntiguedad.Neto;
import cl.bcs.cuentainversion.reportes.SaldosAntiguedad.Saldo;
import cl.bcs.cuentainversion.reportes.SqlReportes;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Re-computes the aging-report balances of ONE account from a movement date up to the day before the system date (legacy
 * {@code ActualizarSaldosReporteAntiguedad}; CTA-B-47..57).
 *
 * <p>The balances of the day before {@code p_FechaMovimiento} are the starting point; for every calendar day up to the day before the
 * system date the certified movements of the account (cargo subtracts, abono adds; FAC + RV voucher is intermediation "I") are netted
 * into the running G / I balances and the result of each day is UPSERTed (CTA-B-47, 48, 56, 57). Errors: movement date not before the
 * system date (49-51), account not found (52), no balances on the starting day (54).
 */
@Service
public class ActualizarSaldosReporteAntiguedadService {

    private static final Logger log = LoggerFactory.getLogger(ActualizarSaldosReporteAntiguedadService.class);

    private static final String SQL_DATOS_CLIENTE = SqlReportes.recurso("ActualizarSaldosReporteAntiguedad_SelectDatosCliente");
    private static final String SQL_MOVIMIENTOS = SqlReportes.recurso("ActualizarSaldosReporteAntiguedad_SelectMovimientosCargoAbono");
    private static final String SQL_SALDOS = SqlReportes.recurso("ActualizarSaldosReporteAntiguedad_SelectSaldoReporteAntiguedad");
    private static final String SQL_UPSERT = SqlReportes.recurso("ActualizarSaldosReporteAntiguedad_UpsertSaldoReporteAntiguedad");

    private final SqlReportes sql;

    public ActualizarSaldosReporteAntiguedadService(NamedParameterJdbcTemplate jdbc) {
        this.sql = new SqlReportes(jdbc);
    }

    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject params) throws Exception {
        return JsonReportes.escribir(actualizar(JsonReportes.leer(params, Entrada.class)));
    }

    Salida actualizar(Entrada in) throws Exception {
        log.info("INICIO::ActualizarSaldosReporteAntiguedad");
        Salida salida = new Salida();
        ConfiguracionSistema config = new ConfiguracionSistema(sql);
        Mensajeria mensajes = new Mensajeria(sql, in.p_IdEmpresa);
        CalendarioReportes calendario = new CalendarioReportes(sql);

        Long idPais = config.entero(in.p_IdEmpresa, ConfiguracionSistema.PLATAFORMA, "PLA.PaisSistema");
        String fechaSistema = config.cadena(in.p_IdEmpresa, ConfiguracionSistema.PLATAFORMA, "PLA.FechaSistema");

        LocalDate inicio = calendario.anterior(LocalDate.parse(in.p_FechaMovimiento), 1L, idPais, false, false).fecha();
        LocalDate fin = calendario.anterior(LocalDate.parse(fechaSistema), 1L, idPais, false, false).fecha();
        long dias = ChronoUnit.DAYS.between(inicio, fin);
        if (dias <= 0) {
            salida.error(mensajes.error("ERROR_FECHA_DE_MOVIMIENTO_MAYOR_O_IGUAL_FECHA_SISTEMA"));
            return salida;
        }

        List<LocalDate> fechasPorCargar = new ArrayList<>();
        for (long i = 1; i <= dias; i++) {
            fechasPorCargar.add(calendario.siguiente(inicio, i, idPais, false, false, false).fecha());
        }

        Cliente cliente = datosCliente(in);
        if (cliente.IdCuentaInversion == null) {
            salida.error(mensajes.error("ERROR_NO_SE_ENCONTRO_LA_CUENTA_DEL_CLIENTE"));
            return salida;
        }
        List<Movimiento> movimientos = sql.consultar(SQL_MOVIMIENTOS, Map.of(
                "FechaInicio", in.p_FechaMovimiento, "FechaFin", fin.toString(), "IdCuentaInversion", cliente.IdCuentaInversion), Movimiento.class);
        List<Saldo> saldos = sql.consultar(SQL_SALDOS, Map.of(
                "IdCliente", cliente.IdCliente, "IdPortafolio", cliente.IdPortafolio, "IdMoneda", cliente.IdMoneda,
                "FechaInicio", inicio.toString()), Saldo.class);
        if (saldos.isEmpty()) {
            salida.error(mensajes.error("ERROR_AL_OBTENER_SALDOS_REPORTE_ANTIGUEDAD"));
            return salida;
        }

        List<Saldo> porActualizar = new ArrayList<>();
        for (LocalDate fecha : fechasPorCargar) {
            List<Movimiento> delDia = movimientos.stream().filter(m -> m.dia().equals(fecha)).toList();
            if (!delDia.isEmpty()) {
                // LEGACY DEFECT kept (see README CTA-B-55): exactly two balance rows (G and I) are assumed; with one row get(1) throws.
                SaldosAntiguedad.aplicar(saldos, Neto.de(delDia));
            }
            saldos.forEach(s -> porActualizar.add(s.para(fecha)));
        }

        if (!guardar(porActualizar)) {
            salida.error(mensajes.error("ERROR_AL_ACTUALIZAR_LOS_SALDOS_REPORTE_ANTIGUEDAD"));
            return salida;
        }
        log.info("FIN::ActualizarSaldosReporteAntiguedad");
        return salida;
    }

    private Cliente datosCliente(Entrada in) {
        Map<String, Object> p = new HashMap<>();
        p.put("Rut", in.p_Rut);
        p.put("CodigoPortafolio", in.p_CodigoPortafolio.toUpperCase());
        p.put("IdMoneda", in.p_Moneda.toUpperCase());
        List<Cliente> filas = sql.consultar(SQL_DATOS_CLIENTE, p, Cliente.class);
        return filas.isEmpty() ? new Cliente() : filas.get(0);
    }

    /** The legacy reported a failed UPSERT as a message instead of an exception. */
    private boolean guardar(List<Saldo> saldos) {
        try {
            sql.lote(SQL_UPSERT, saldos);
            return !saldos.isEmpty();
        } catch (DataAccessException e) {
            log.error("ActualizarSaldosReporteAntiguedad: UPSERT failed", e);
            return false;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_Rut;
        public String p_CodigoPortafolio;
        public String p_Moneda;
        public String p_FechaMovimiento;
    }

    public static class Salida {
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR = 0L;

        void error(MensajeSalida m) {
            P_OUT_ERRORESCONTADOR++;
            P_OUT_MENSAJES.add(m);
        }
    }

    public static class Cliente {
        public Long IdCuentaInversion;
        public Long IdCliente;
        public Long IdPortafolio;
        public String IdMoneda;
    }
}
