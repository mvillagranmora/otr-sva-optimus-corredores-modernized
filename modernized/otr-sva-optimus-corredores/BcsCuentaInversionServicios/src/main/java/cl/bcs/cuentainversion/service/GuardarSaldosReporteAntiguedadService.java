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
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Daily close of the aging-report balances (legacy {@code GuardarSaldosReporteAntiguedad}; CTA-B-63..68).
 *
 * <p>Days to (re)process: the {@code DIASREPROCESOSALDOREPORTEANTIGUEDAD} calendar days before the system date (CTA-B-68), the system
 * date itself and, when the next business day is not the next calendar day, the non-business days in between (CTA-B-64..66). Starting
 * from the balances of the day before the oldest one, the certified movements of each day are netted into the running G / I balances
 * and every day is UPSERTed (CTA-B-63, 67). No balances on the starting day -> ERROR_AL_OBTENER_SALDOS_REPORTE_ANTIGUEDAD.
 */
@Service
public class GuardarSaldosReporteAntiguedadService {

    private static final Logger log = LoggerFactory.getLogger(GuardarSaldosReporteAntiguedadService.class);

    private static final String SQL_SALDOS = SqlReportes.recurso("GuardarSaldosReporteAntiguedad_SelectSaldoReporteAntiguedad");
    private static final String SQL_MOVIMIENTOS = SqlReportes.recurso("GuardarSaldosReporteAntiguedad_SelectMovimientos");
    private static final String SQL_UPSERT = SqlReportes.recurso("GuardarSaldosReporteAntiguedad_UpsertSaldoReporteAntiguedad");

    private final SqlReportes sql;

    public GuardarSaldosReporteAntiguedadService(NamedParameterJdbcTemplate jdbc) {
        this.sql = new SqlReportes(jdbc);
    }

    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject params) throws Exception {
        return JsonReportes.escribir(guardar(JsonReportes.leer(params, Entrada.class)));
    }

    Salida guardar(Entrada in) throws Exception {
        log.info("INICIO::GuardarSaldosReporteAntiguedad");
        Salida salida = new Salida();
        ConfiguracionSistema config = new ConfiguracionSistema(sql);
        Mensajeria mensajes = new Mensajeria(sql, in.p_IdEmpresa);
        CalendarioReportes calendario = new CalendarioReportes(sql);

        Long idPais = config.entero(in.p_IdEmpresa, ConfiguracionSistema.PLATAFORMA, "PLA.PaisSistema");
        long diasPorReprocesar = config.entero(in.p_IdEmpresa, ConfiguracionSistema.CUENTAINVERSION, "DIASREPROCESOSALDOREPORTEANTIGUEDAD");
        LocalDate sistema = LocalDate.parse(in.p_FechaSistema);

        List<LocalDate> fechas = new ArrayList<>();
        for (long i = 1; i <= diasPorReprocesar; i++) {
            fechas.add(calendario.anterior(sistema, i, idPais, false, false).fecha());
        }
        // an empty reprocess window (days <= 0) fails here with IndexOutOfBoundsException, as the legacy did
        LocalDate fechaInicio = fechas.get(fechas.size() - 1);

        CalendarioReportes.Calculo siguienteHabil = calendario.siguiente(sistema, 1L, idPais, true, true, false);
        if (siguienteHabil.numeroDias() != 1L) {
            // the system date is followed by non business days: those days are loaded too (the day-count walked, exclusive)
            for (long i = 1; i < siguienteHabil.numeroDias(); i++) {
                fechas.add(calendario.siguiente(sistema, i, idPais, false, false, false).fecha());
            }
        }
        fechas.add(sistema);
        Collections.sort(fechas);

        LocalDate inicioReporte = calendario.anterior(fechaInicio, 1L, idPais, false, false).fecha();
        List<Saldo> saldos = new ArrayList<>(sql.consultar(SQL_SALDOS, Map.of("fechaConsulta", inicioReporte.toString()), Saldo.class));
        if (saldos.isEmpty()) {
            salida.error(mensajes.error("ERROR_AL_OBTENER_SALDOS_REPORTE_ANTIGUEDAD"));
            return salida;
        }

        List<Movimiento> movimientos = sql.consultar(SQL_MOVIMIENTOS, Map.of("FechaInicio", fechaInicio.toString()), Movimiento.class);
        List<Saldo> porGuardar = new ArrayList<>();
        for (LocalDate fecha : fechas) {
            List<Movimiento> delDia = movimientos.stream().filter(m -> m.dia().equals(fecha)).toList();
            if (!delDia.isEmpty()) {
                // LEGACY QUIRK kept: the accounts to update are taken from ALL the movements since fechaInicio, not only from the
                // ones of this day (an account without movements that day is netted with zero and may get new zero rows).
                Map<String, Movimiento> cuentas = new LinkedHashMap<>();
                movimientos.forEach(m -> cuentas.put(m.cuenta(), m));
                for (Movimiento cuenta : cuentas.values()) {
                    Neto neto = Neto.de(delDia.stream().filter(m -> m.esDe(cuenta)).toList());
                    List<Saldo> previos = saldos.stream().filter(s -> s.esDe(cuenta)).toList();
                    if (!previos.isEmpty()) {
                        SaldosAntiguedad.aplicar(previos, neto);
                    } else {
                        saldos.addAll(SaldosAntiguedad.nuevaCuenta(cuenta, sistema, neto));
                    }
                }
            }
            saldos.forEach(s -> porGuardar.add(s.para(fecha)));
        }

        if (!upsert(porGuardar)) {
            salida.error(mensajes.error("ERROR_AL_INSERTAR_LOS_SALDOS_REPORTE_ANTIGUEDAD"));
            return salida;
        }
        log.info("FIN::GuardarSaldosReporteAntiguedad");
        return salida;
    }

    /** The legacy reported a failed UPSERT as a message instead of an exception. */
    private boolean upsert(List<Saldo> saldos) {
        try {
            sql.lote(SQL_UPSERT, saldos);
            return !saldos.isEmpty();
        } catch (DataAccessException e) {
            log.error("GuardarSaldosReporteAntiguedad: UPSERT failed", e);
            return false;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_FechaSistema;
    }

    public static class Salida {
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR = 0L;

        void error(MensajeSalida m) {
            P_OUT_ERRORESCONTADOR++;
            P_OUT_MENSAJES.add(m);
        }
    }
}
