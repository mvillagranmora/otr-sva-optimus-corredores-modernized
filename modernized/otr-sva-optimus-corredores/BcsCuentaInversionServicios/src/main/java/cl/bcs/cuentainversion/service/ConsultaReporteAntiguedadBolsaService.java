package cl.bcs.cuentainversion.service;

import cl.bcs.cuentainversion.port.out.ColaboradoresReportes;
import cl.bcs.cuentainversion.reportes.AntiguedadComun;
import cl.bcs.cuentainversion.reportes.AntiguedadComun.ClientePortafolio;
import cl.bcs.cuentainversion.reportes.AntiguedadComun.CuentaPaso;
import cl.bcs.cuentainversion.reportes.AntiguedadComun.Moneda;
import cl.bcs.cuentainversion.reportes.AntiguedadComun.Movimiento;
import cl.bcs.cuentainversion.reportes.JsonReportes;
import cl.bcs.cuentainversion.reportes.MensajeSalida;
import cl.bcs.cuentainversion.reportes.Mensajeria;
import cl.bcs.cuentainversion.reportes.SaldosCuentaInversion;
import cl.bcs.cuentainversion.reportes.SaldosCuentaInversion.ClienteConsolidado;
import cl.bcs.cuentainversion.reportes.SaldosCuentaInversion.Saldo;
import cl.bcs.cuentainversion.reportes.SqlReportes;
import cl.bcs.cuentainversion.reportes.TramosAntiguedad;
import cl.bcs.cuentainversion.reportes.TramosAntiguedad.Tramo;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Aging of the debt of the clients with the brokerage house (legacy {@code ConsultaReporteAntiguedadBolsa}, CTA-B-69..82).
 *
 * <p>Accounts with a negative balance (CLP truncated, other currencies rounded to 2 decimals, CTA-B-75/76) are staged in a working
 * table; the certified MOVCAR charges up to the query date are then walked from the newest and assigned to the age tranche of their
 * certification date until the balance is covered (CTA-B-69, 78, 80). Intermediation charges ('I') and the rest ('G') are
 * reported in separate rows. Amounts: whole units for CLP or when several currencies are asked, 2 decimals otherwise (CTA-B-71).
 */
@Service
public class ConsultaReporteAntiguedadBolsaService {

    private static final Logger log = LoggerFactory.getLogger(ConsultaReporteAntiguedadBolsaService.class);

    private static final String SQL_MONEDAS = SqlReportes.recurso("ConsultaReporteAntiguedadBolsa_SelectDistintasMonedasCuentasInversion");
    private static final String SQL_INSERT_PASO = SqlReportes.recurso("ConsultaReporteAntiguedadBolsa_InsertCuentaInversionTablaDePaso");
    private static final String SQL_DELETE_PASO = SqlReportes.recurso("ConsultaReporteAntiguedadBolsa_DeleteCuentaInversionTablaDePaso");
    private static final String SQL_DATOS_CLIENTE = SqlReportes.recurso("ConsultaReporteAntiguedadBolsa_SelectDatosCliente");
    private static final String SQL_CARGOS = SqlReportes.recurso("ConsultaReporteAntiguedadBolsa_SelectCargos");

    private static final String TODAS = "TODAS";
    private static final String INTERMEDIACION = "I";

    private final SqlReportes sql;
    private final ColaboradoresReportes colaboradores;

    public ConsultaReporteAntiguedadBolsaService(NamedParameterJdbcTemplate jdbc, ColaboradoresReportes colaboradores) {
        this.sql = new SqlReportes(jdbc);
        this.colaboradores = colaboradores;
    }

    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject params) throws Exception {
        return JsonReportes.escribir(consultar(JsonReportes.leer(params, Entrada.class)));
    }

    Salida consultar(Entrada in) throws Exception {
        log.info("INICIO::ConsultaReporteAntiguedadBolsa");
        Salida out = new Salida();
        Mensajeria mensajes = new Mensajeria(sql, in.p_IdEmpresa);
        long idTrabajo = (long) (Math.random() * 10000000000L);

        // The company must exist (its country only fed the holiday calendar, which the calendar-day tranches do not use).
        JsonReportes.empresa(colaboradores, in.p_IdEmpresa);

        List<Moneda> monedas = new ArrayList<>();
        if (in.p_IdMoneda != null && !in.p_IdMoneda.isEmpty() && !TODAS.equals(in.p_IdMoneda)) {
            Moneda m = new Moneda();
            m.IdMoneda = in.p_IdMoneda;
            monedas.add(m);
        } else {
            monedas = sql.consultar(SQL_MONEDAS, Map.of(), Moneda.class);
        }

        TramosAntiguedad tramos = TramosAntiguedad.desde(in.p_FechaConsulta);

        boolean consolidado = in.p_Consolidar.equalsIgnoreCase("S") && monedas.size() == 1;
        List<ClienteConsolidado> clientesConsolidados = new ArrayList<>();
        List<Saldo> cuentas = consolidado
                ? cuentasConsolidadas(in, monedas.get(0).IdMoneda, clientesConsolidados)
                : cuentasNegativas(in, monedas);
        if (cuentas.isEmpty()) {
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_MENSAJES.add(mensajes.error("SIN_SALDO_CUENTAS_DE_INVERSION_NEGATIVO"));
            return out;
        }

        List<CuentaPaso> paso = AntiguedadComun.prepararSaldoFinal(cuentas, in.p_Consolidar.equalsIgnoreCase("N"));

        boolean eliminada = true;
        try {
            sql.ejecutar(SQL_DELETE_PASO);
        } catch (DataAccessException e) {
            eliminada = false;
        }
        try {
            boolean insertada = AntiguedadComun.insertarPaso(sql, SQL_INSERT_PASO, paso, idTrabajo);
            if (!eliminada && !insertada) {
                out.P_OUT_ERRORESCONTADOR++;
            }
        } catch (RuntimeException e) {
            out.P_OUT_ERRORESCONTADOR++;
        }
        if (out.P_OUT_ERRORESCONTADOR > 0) {
            out.P_OUT_MENSAJES.add(mensajes.error("ERROR_AL_GENERAR_REPORTE"));
            return out;
        }

        List<ClientePortafolio> clientes = sql.consultar(SQL_DATOS_CLIENTE, Map.of("IdTrabajo", idTrabajo), ClientePortafolio.class);
        Map<Long, ClientePortafolio> porPortafolio = AntiguedadComun.porPortafolio(clientes);

        List<Movimiento> cargos = sql.consultar(SQL_CARGOS, Map.of("IdTrabajo", idTrabajo, "FechaBusqueda", in.p_FechaConsulta), Movimiento.class);
        if (cargos.isEmpty()) {
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_MENSAJES.add(mensajes.error("ERROR_AL_OBTENER_LOS_CARGOS"));
            return out;
        }

        boolean sinDecimales = monedas.get(0).IdMoneda.equalsIgnoreCase("CLP") || monedas.size() > 1;
        boolean separa = in.p_SeparaIntermediacion.equalsIgnoreCase("S");
        List<Fila> filas = new ArrayList<>();

        if (consolidado) {
            for (ClienteConsolidado cliente : clientesConsolidados) {
                ClientePortafolio datos = porPortafolio.get(cliente.idPortafolios.get(0));
                Fila intermediacion = fila(datos, null, cliente.saldoFinal, "", "Cuenta de Intermediacion");
                Fila general = fila(datos, null, cliente.saldoFinal, "", clasificacion(datos));
                List<Movimiento> deCliente = cargos.stream().filter(c -> c.IdCliente.compareTo(cliente.idCliente) == 0).sorted(AntiguedadComun.MAS_RECIENTE_PRIMERO).toList();
                cubrirDeuda(cliente.saldoFinal, deCliente, tramos, intermediacion, general);
                cerrar(intermediacion, general, sinDecimales, separa, filas);
            }
        } else {
            for (CuentaPaso cuenta : paso) {
                ClientePortafolio datos = porPortafolio.get(cuenta.IdPortafolio);
                String observacion = cuenta.Observacion == null ? "" : cuenta.Observacion;
                Fila intermediacion = fila(datos, cuenta.IdPortafolio, cuenta.SaldoFinal, observacion, "Cuenta de Intermediacion");
                Fila general = fila(datos, cuenta.IdPortafolio, cuenta.SaldoFinal, observacion, clasificacion(datos));
                List<Movimiento> deCuenta = cargos.stream().filter(c -> c.IdCuentaInversion.compareTo(cuenta.IdCuentaInversion) == 0).sorted(AntiguedadComun.MAS_RECIENTE_PRIMERO).toList();
                cubrirDeuda(cuenta.SaldoFinal, deCuenta, tramos, intermediacion, general);
                cerrar(intermediacion, general, sinDecimales, separa, filas);
            }
        }
        // LEGACY DEFECT kept (see README CTA-B-08): with SeparaIntermediacion = N nothing is ever added, the table comes back empty.
        out.P_OUT_TABLA.addAll(filas);
        log.info("FIN::ConsultaReporteAntiguedadBolsa");
        return out;
    }

    /** "SI" is what the data query answers for a related client, the legacy compares it with "S": nobody is ever related (CTA-B-09). */
    private static String clasificacion(ClientePortafolio datos) {
        // LEGACY DEFECT kept (see README CTA-B-09): 'SI' vs 'S' related-client comparison.
        return datos.Relacionado.equals("S") ? "Cuentas con Personas y Empresas Relacionados" : "Cuentas por Cobrar y Cuentas de Documentos";
    }

    private static Fila fila(ClientePortafolio datos, Long idPortafolio, BigDecimal saldo, String observacion, String clasificacion) {
        Fila f = new Fila();
        f.IdPortafolio = idPortafolio;
        f.DocumentoIndentidad = datos.DocumentoIdentidad;
        f.NombreCliente = datos.NombreCompleto;
        f.ClasificacionContable = clasificacion;
        f.Relacionado = datos.Relacionado;
        f.CampoBD = saldo;
        f.CodigoPortafolio = idPortafolio == null ? null : datos.CodigoPortafolio;
        f.Observacion = observacion;
        return f;
    }

    /** Walks the charges from the newest, adding them to their tranche until the (negative) balance is covered. */
    private static void cubrirDeuda(BigDecimal saldoInicial, List<Movimiento> cargos, TramosAntiguedad tramos, Fila intermediacion, Fila general) {
        BigDecimal saldo = saldoInicial;
        for (Movimiento mov : cargos) {
            BigDecimal restante = AntiguedadComun.sumar(saldo, mov.Monto);
            Tramo tramo = tramos.de(mov.FechaMovimiento.toLocalDate());
            Fila destino = INTERMEDIACION.equalsIgnoreCase(mov.TipoOperacion) ? intermediacion : general;
            int signo = restante.compareTo(BigDecimal.ZERO);
            if (signo < 0) {
                destino.sumar(tramo, mov.Monto);
            } else if (signo == 0) {
                destino.sumar(tramo, mov.Monto);
                return;
            } else {
                destino.sumar(tramo, mov.Monto.subtract(restante, java.math.MathContext.DECIMAL128));
                return;
            }
            saldo = restante;
        }
    }

    private static void cerrar(Fila intermediacion, Fila general, boolean sinDecimales, boolean separa, List<Fila> filas) {
        for (Fila f : List.of(general, intermediacion)) {
            f.formatear(sinDecimales);
        }
        general.totalizar();
        intermediacion.totalizar();
        if (separa) {
            if (intermediacion.MontoPorCobrarTotal.compareTo(BigDecimal.ZERO) > 0) {
                filas.add(intermediacion);
            }
            if (general.MontoPorCobrarTotal.compareTo(BigDecimal.ZERO) > 0) {
                filas.add(general);
            }
        }
    }

    private List<Saldo> cuentasNegativas(Entrada in, List<Moneda> monedas) {
        List<Saldo> negativas = new ArrayList<>();
        SaldosCuentaInversion.Entrada solicitud = SaldosCuentaInversion.solicitud(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, in.p_FechaConsulta, in.p_IdEjecutivo, null);
        SaldosCuentaInversion.Salida saldos = SaldosCuentaInversion.consultar(colaboradores, solicitud);
        if (SaldosCuentaInversion.utilizable(saldos)) {
            for (Moneda moneda : monedas) {
                List<Saldo> deMoneda = saldos.P_OUT_SALDOPORFECHAEFECTIVA.stream().filter(s -> s.IdMoneda.equalsIgnoreCase(moneda.IdMoneda)).toList();
                negativas.addAll(SaldosCuentaInversion.conSigno(deMoneda, moneda.IdMoneda, signo -> signo < 0));
            }
        }
        return negativas;
    }

    private List<Saldo> cuentasConsolidadas(Entrada in, String moneda, List<ClienteConsolidado> clientes) {
        List<Saldo> cuentas = new ArrayList<>();
        SaldosCuentaInversion.Entrada solicitud = SaldosCuentaInversion.solicitud(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, in.p_FechaConsulta, in.p_IdEjecutivo, moneda);
        solicitud.p_IdMoneda = moneda;
        SaldosCuentaInversion.Salida saldos = SaldosCuentaInversion.consultar(colaboradores, solicitud);
        if (SaldosCuentaInversion.utilizable(saldos)) {
            clientes.addAll(SaldosCuentaInversion.consolidar(saldos.P_OUT_SALDOPORFECHAEFECTIVA, moneda, signo -> signo < 0, cuentas));
        }
        return cuentas;
    }

    // ---------------------------------------------------------------------------------------------- JSON contract

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_FechaConsulta;
        public String p_IdMoneda;
        public Long p_IdEjecutivo = 0L;
        public String p_SeparaIntermediacion = "N";
        public String p_AgrupaPortafolios = "N";
        public String p_Consolidar = "N";
    }

    public static class Salida {
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_INFORMACIONCONTADOR;
        public Long P_OUT_ADVERTENCIASCONTADOR;
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<Fila> P_OUT_TABLA = new ArrayList<>();
    }

    /** Row of the report: amount per age tranche (A current, B 3-10 days, C 11-30, D older than 30). */
    public static class Fila {
        public Long IdPortafolio;
        public String CodigoPortafolio;
        public String DocumentoIndentidad;
        public String NombreCliente;
        public BigDecimal MontoPorCobrarTotal = BigDecimal.ZERO;
        public BigDecimal TramoA = BigDecimal.ZERO;
        public BigDecimal TramoB = BigDecimal.ZERO;
        public BigDecimal TramoC = BigDecimal.ZERO;
        public BigDecimal TramoD = BigDecimal.ZERO;
        public String Observacion;
        public String ClasificacionContable;
        public String Relacionado;
        public BigDecimal CampoBD;
        public BigDecimal Matematica;

        void sumar(Tramo tramo, BigDecimal monto) {
            switch (tramo) {
                case A -> TramoA = AntiguedadComun.sumar(TramoA, monto);
                case B -> TramoB = AntiguedadComun.sumar(TramoB, monto);
                case C -> TramoC = AntiguedadComun.sumar(TramoC, monto);
                case D -> TramoD = AntiguedadComun.sumar(TramoD, monto);
                case NINGUNO -> {
                }
            }
        }

        void formatear(boolean sinDecimales) {
            TramoA = AntiguedadComun.formatear(TramoA, sinDecimales);
            TramoB = AntiguedadComun.formatear(TramoB, sinDecimales);
            TramoC = AntiguedadComun.formatear(TramoC, sinDecimales);
            TramoD = AntiguedadComun.formatear(TramoD, sinDecimales);
            MontoPorCobrarTotal = AntiguedadComun.formatear(MontoPorCobrarTotal, sinDecimales);
        }

        void totalizar() {
            MontoPorCobrarTotal = AntiguedadComun.sumar(AntiguedadComun.sumar(AntiguedadComun.sumar(AntiguedadComun.sumar(MontoPorCobrarTotal, TramoA), TramoB), TramoC), TramoD);
        }
    }
}
