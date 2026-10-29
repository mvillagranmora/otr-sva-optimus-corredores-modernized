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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Initial load of the aging-report balances (legacy {@code CargaInicialSaldosReporteAntiguedad}; CTA-B-58..62): starting from the
 * newest day already stored, every following calendar day up to the day before {@code p_FechaSistema} is inserted for ALL accounts,
 * netting the certified movements of the day into the running balances (accounts without a previous row get a G and an I row).
 * Nothing to load (the newest day is not before the previous day of the system date) -> ERROR_CARGA_INICIAL_YA_REALIZADA.
 */
@Service
public class CargaInicialSaldosReporteAntiguedadService {

    private static final Logger log = LoggerFactory.getLogger(CargaInicialSaldosReporteAntiguedadService.class);

    private static final String SQL_SALDOS = SqlReportes.recurso("CargaInicialSaldosReporteAntiguedad_SelectSaldoReporteAntiguedad");
    private static final String SQL_ULTIMA_FECHA = SqlReportes.recurso("CargaInicialSaldosReporteAntiguedad_SelectFechaUltimoSaldoReporteAntiguedad");
    private static final String SQL_MOVIMIENTOS = SqlReportes.recurso("CargaInicialSaldosReporteAntiguedad_SelectMovimientosCargoAbono");
    private static final String SQL_INSERT = SqlReportes.recurso("CargaInicialSaldosReporteAntiguedad_InsertSaldoReporteAntiguedad");

    private final SqlReportes sql;

    public CargaInicialSaldosReporteAntiguedadService(NamedParameterJdbcTemplate jdbc) {
        this.sql = new SqlReportes(jdbc);
    }

    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject params) throws Exception {
        return JsonReportes.escribir(cargar(JsonReportes.leer(params, Entrada.class)));
    }

    Salida cargar(Entrada in) throws Exception {
        log.info("INICIO::CargaInicialSaldosReporteAntiguedad");
        Salida salida = new Salida();
        Mensajeria mensajes = new Mensajeria(sql, in.p_IdEmpresa);
        CalendarioReportes calendario = new CalendarioReportes(sql);
        Long idPais = new ConfiguracionSistema(sql).entero(in.p_IdEmpresa, ConfiguracionSistema.PLATAFORMA, "PLA.PaisSistema");

        LocalDate fin = calendario.anterior(LocalDate.parse(in.p_FechaSistema), 1L, idPais, false, false).fecha();
        // LEGACY DEFECT kept (see README CTA-B-61): MAX() over an empty balance table answers NULL -> NullPointerException.
        LocalDate ultimo = sql.consultar(SQL_ULTIMA_FECHA, UltimoRegistro.class).get(0).FechaRegistro.toLocalDate();
        long dias = ChronoUnit.DAYS.between(ultimo, fin);
        if (dias <= 0) {
            salida.error(mensajes.error("ERROR_CARGA_INICIAL_YA_REALIZADA"));
            return salida;
        }

        List<LocalDate> fechasPorCargar = new ArrayList<>();
        for (long i = 1; i <= dias; i++) {
            fechasPorCargar.add(calendario.siguiente(ultimo, i, idPais, false, false, false).fecha());
        }
        List<Movimiento> movimientos = sql.consultar(SQL_MOVIMIENTOS, Map.of("FechaInicio", ultimo.toString(), "FechaFin", fin.toString()), Movimiento.class);
        List<Saldo> saldos = new ArrayList<>(sql.consultar(SQL_SALDOS, Map.of("fechaConsulta", ultimo.toString()), Saldo.class));
        if (saldos.isEmpty()) {
            salida.error(mensajes.error("ERROR_AL_OBTENER_SALDOS_REPORTE_ANTIGUEDAD"));
            return salida;
        }

        List<Saldo> porInsertar = new ArrayList<>();
        for (LocalDate fecha : fechasPorCargar) {
            List<Movimiento> delDia = movimientos.stream().filter(m -> m.dia().equals(fecha)).toList();
            Map<String, Movimiento> cuentas = new LinkedHashMap<>();
            delDia.forEach(m -> cuentas.put(m.cuenta(), m));
            for (Movimiento cuenta : cuentas.values()) {
                Neto neto = Neto.de(delDia.stream().filter(m -> m.esDe(cuenta)).toList());
                List<Saldo> previos = saldos.stream().filter(s -> s.esDe(cuenta)).toList();
                if (!previos.isEmpty()) {
                    // LEGACY DEFECT kept (see README CTA-B-55 family): two rows (G and I) per account are assumed.
                    SaldosAntiguedad.aplicar(previos, neto);
                } else {
                    saldos.addAll(SaldosAntiguedad.nuevaCuenta(cuenta, fecha, neto));
                }
            }
            saldos.forEach(s -> porInsertar.add(s.para(fecha)));
        }

        if (!insertar(porInsertar)) {
            salida.error(mensajes.error("ERROR_AL_INSERTAR_LOS_SALDOS_CARGA_INICIAL"));
            return salida;
        }
        log.info("FIN::CargaInicialSaldosReporteAntiguedad");
        return salida;
    }

    /** The legacy reported a failed INSERT as a message instead of an exception. */
    private boolean insertar(List<Saldo> saldos) {
        try {
            sql.lote(SQL_INSERT, saldos);
            return !saldos.isEmpty();
        } catch (DataAccessException e) {
            log.error("CargaInicialSaldosReporteAntiguedad: INSERT failed", e);
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

    public static class UltimoRegistro {
        public java.sql.Date FechaRegistro;
    }
}
