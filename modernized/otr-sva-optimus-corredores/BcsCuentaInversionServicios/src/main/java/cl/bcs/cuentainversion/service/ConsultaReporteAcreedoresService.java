package cl.bcs.cuentainversion.service;

import cl.bcs.cuentainversion.port.out.ColaboradoresReportes;
import cl.bcs.cuentainversion.reportes.AntiguedadComun;
import cl.bcs.cuentainversion.reportes.AntiguedadComun.ClientePortafolio;
import cl.bcs.cuentainversion.reportes.AntiguedadComun.CuentaPaso;
import cl.bcs.cuentainversion.reportes.AntiguedadComun.Movimiento;
import cl.bcs.cuentainversion.reportes.ConfiguracionSistema;
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
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Aging of what the house owes its clients (legacy {@code ConsultaReporteAcreedores}, CTA-B-83..92): accounts with a POSITIVE balance
 * in one currency (the system currency unless asked otherwise) are staged, and the certified credits (MOVABN) up to the query date
 * are walked from the newest and assigned to their age tranche until the balance is explained. Consolidar = S (default) works per
 * client. Failures after the staging are reported as messages (and the working table is always cleaned), CTA-B-86..92.
 */
@Service
public class ConsultaReporteAcreedoresService {

    private static final Logger log = LoggerFactory.getLogger(ConsultaReporteAcreedoresService.class);

    private static final String SQL_INSERT_PASO = SqlReportes.recurso("ConsultaReporteAcreedores_InsertCuentaInversionTablaDePaso");
    private static final String SQL_DATOS_CLIENTE = SqlReportes.recurso("ConsultaReporteAcreedores_SelectDatosCliente");
    private static final String SQL_ABONOS = SqlReportes.recurso("ConsultaReporteAcreedores_SelectAbonos");
    private static final String SQL_DELETE_PASO = SqlReportes.recurso("ConsultaReporteAcreedores_DeleteCuentaInversionTablaDePaso");

    private static final String INTERMEDIACION = "I";

    private final SqlReportes sql;
    private final ColaboradoresReportes colaboradores;

    public ConsultaReporteAcreedoresService(NamedParameterJdbcTemplate jdbc, ColaboradoresReportes colaboradores) {
        this.sql = new SqlReportes(jdbc);
        this.colaboradores = colaboradores;
    }

    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject params) throws Exception {
        return JsonReportes.escribir(consultar(JsonReportes.leer(params, Entrada.class)));
    }

    Salida consultar(Entrada in) throws Exception {
        log.info("INICIO::ConsultaReporteAcreedores");
        Salida out = new Salida();
        Mensajeria mensajes = new Mensajeria(sql, in.p_IdEmpresa);
        long idTrabajo = (long) (Math.random() * 10000000000L);

        JsonReportes.empresa(colaboradores, in.p_IdEmpresa); // must exist; the country only fed the (unused) holiday calendar
        String monedaSistema = new ConfiguracionSistema(sql).cadena(in.p_IdEmpresa, ConfiguracionSistema.PLATAFORMA, "PLA.MonedaSistema");
        if (in.p_IdMoneda == null || in.p_IdMoneda.isEmpty()) {
            in.p_IdMoneda = monedaSistema;
        }
        TramosAntiguedad tramos = TramosAntiguedad.desde(in.p_FechaConsulta);

        boolean consolidar = "S".equalsIgnoreCase(in.p_Consolidar);
        List<ClienteConsolidado> clientesConsolidados = new ArrayList<>();
        List<Saldo> cuentas = consolidar
                ? cuentasConsolidadas(in, clientesConsolidados)
                : cuentasPositivas(in);
        if (cuentas.isEmpty()) {
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_MENSAJES.add(mensajes.error("SIN_CUENTAS_DE_INVERSION_SALDO_POSITIVO"));
            return out;
        }

        List<CuentaPaso> paso = AntiguedadComun.prepararSaldoFinal(cuentas, "N".equalsIgnoreCase(in.p_Consolidar));
        // LEGACY DEFECT kept (see README CTA-B-10): amounts are formatted with the SYSTEM currency even when another one was asked.
        boolean sinDecimales = "CLP".equalsIgnoreCase(monedaSistema);

        try {
            if (AntiguedadComun.insertarPaso(sql, SQL_INSERT_PASO, paso, idTrabajo)) {
                List<ClientePortafolio> clientes = sql.consultar(SQL_DATOS_CLIENTE, Map.of("IdTrabajo", idTrabajo), ClientePortafolio.class);
                if (!clientes.isEmpty()) {
                    Map<Long, ClientePortafolio> porPortafolio = AntiguedadComun.porPortafolio(clientes);
                    List<Movimiento> abonos = sql.consultar(SQL_ABONOS, Map.of("IdTrabajo", idTrabajo, "FechaBusqueda", in.p_FechaConsulta), Movimiento.class);
                    if (!abonos.isEmpty()) {
                        out.P_OUT_TABLA.addAll(consolidar
                                ? filasPorCliente(clientesConsolidados, porPortafolio, abonos, tramos, sinDecimales)
                                : filasPorCuenta(paso, porPortafolio, abonos, tramos, sinDecimales));
                    } else {
                        out.P_OUT_ERRORESCONTADOR++;
                        out.P_OUT_MENSAJES.add(mensajes.error("ERROR_AL_OBTENER_ABONOS"));
                    }
                } else {
                    out.P_OUT_ERRORESCONTADOR++;
                    out.P_OUT_MENSAJES.add(mensajes.error("ERROR_AL_OBTENER_DATOS_CLIENTE"));
                }
            } else {
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_MENSAJES.add(mensajes.error("ERROR_AL_INSERTAR_TABLA_TEMPORAL"));
            }
        } catch (Exception e) {
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_MENSAJES.add(mensajes.error("ERROR_AL_GENERAR_REPORTE"));
            log.error(e.getMessage());
        } finally {
            if (!limpiarPaso(idTrabajo)) {
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_MENSAJES.add(mensajes.error("ERROR_AL_LIMPIAR_TABLA_TEMPORAL"));
            }
        }
        log.info("FIN::ConsultaReporteAcreedores");
        return out;
    }

    private boolean limpiarPaso(long idTrabajo) {
        try {
            sql.lote(SQL_DELETE_PASO, List.of(new LimpiarPaso(idTrabajo)));
            return true;
        } catch (DataAccessException e) {
            log.error(e.getMessage());
            return false;
        }
    }

    public static class LimpiarPaso {
        public Long IdTrabajo;

        LimpiarPaso(Long idTrabajo) {
            this.IdTrabajo = idTrabajo;
        }
    }

    private List<Fila> filasPorCliente(List<ClienteConsolidado> clientes, Map<Long, ClientePortafolio> porPortafolio, List<Movimiento> abonos,
                                       TramosAntiguedad tramos, boolean sinDecimales) {
        List<Fila> filas = new ArrayList<>();
        for (ClienteConsolidado cliente : clientes) {
            ClientePortafolio datos = porPortafolio.get(cliente.idPortafolios.get(0));
            // LEGACY DEFECT kept (see README CTA-B-09/90): a client unknown to the DB dereferences null here, reported as ERROR_AL_GENERAR_REPORTE.
            Fila intermediacion = fila(datos, null, cliente.saldoFinal, "", "Cuenta de Intermediacion");
            Fila general = fila(datos, null, cliente.saldoFinal, "", clasificacion(datos));
            List<Movimiento> deCliente = abonos.stream().filter(a -> a.IdCliente.compareTo(cliente.idCliente) == 0).sorted(AntiguedadComun.MAS_RECIENTE_PRIMERO).toList();
            if (deCliente.isEmpty()) {
                continue;
            }
            explicarSaldo(cliente.saldoFinal, deCliente, tramos, intermediacion, general);
            cerrar(intermediacion, general, sinDecimales, filas);
        }
        return filas;
    }

    private List<Fila> filasPorCuenta(List<CuentaPaso> cuentas, Map<Long, ClientePortafolio> porPortafolio, List<Movimiento> abonos,
                                      TramosAntiguedad tramos, boolean sinDecimales) {
        List<Fila> filas = new ArrayList<>();
        for (CuentaPaso cuenta : cuentas) {
            ClientePortafolio datos = porPortafolio.get(cuenta.IdPortafolio);
            String observacion = cuenta.Observacion == null ? "" : cuenta.Observacion;
            Fila intermediacion = fila(datos, cuenta.IdPortafolio, cuenta.SaldoFinal, observacion, "Cuenta de Intermediacion");
            Fila general = fila(datos, cuenta.IdPortafolio, cuenta.SaldoFinal, observacion, clasificacion(datos));
            List<Movimiento> deCuenta = abonos.stream().filter(a -> a.IdCuentaInversion.compareTo(cuenta.IdCuentaInversion) == 0).sorted(AntiguedadComun.MAS_RECIENTE_PRIMERO).toList();
            if (deCuenta.isEmpty()) {
                continue;
            }
            explicarSaldo(cuenta.SaldoFinal, deCuenta, tramos, intermediacion, general);
            cerrar(intermediacion, general, sinDecimales, filas);
        }
        return filas;
    }

    private static String clasificacion(ClientePortafolio datos) {
        // LEGACY DEFECT kept (see README CTA-B-09): the data query answers 'SI' for a related client, the legacy compares it with 'S'.
        return "S".equals(datos.Relacionado) ? "Cuentas con Personas y Empresas Relacionados" : "Cuentas por Cobrar y Cuentas de Documentos";
    }

    private static Fila fila(ClientePortafolio datos, Long idPortafolio, BigDecimal saldo, String observacion, String clasificacion) {
        Fila f = new Fila();
        f.IdPortafolio = idPortafolio;
        f.DocumentoIndentidad = datos.DocumentoIdentidad;
        f.NombreCliente = datos.NombreCompleto;
        f.Relacionado = datos.Relacionado;
        f.CampoBD = saldo;
        f.Observacion = observacion;
        f.ClasificacionContable = clasificacion;
        // LEGACY DEFECT kept (see README CTA-B-10): CodigoPortafolio is never filled.
        return f;
    }

    /** Walks the credits from the newest, adding them to their tranche until the (positive) balance is explained. */
    private static void explicarSaldo(BigDecimal saldoInicial, List<Movimiento> abonos, TramosAntiguedad tramos, Fila intermediacion, Fila general) {
        BigDecimal saldo = saldoInicial;
        for (Movimiento mov : abonos) {
            BigDecimal restante = saldo.subtract(mov.Monto, MathContext.DECIMAL128);
            Tramo tramo = tramos.de(mov.FechaMovimiento.toLocalDate());
            Fila destino = INTERMEDIACION.equalsIgnoreCase(mov.TipoOperacion) ? intermediacion : general;
            int signo = restante.compareTo(BigDecimal.ZERO);
            if (signo > 0) {
                destino.sumar(tramo, mov.Monto);
            } else if (signo == 0) {
                destino.sumar(tramo, mov.Monto);
                return;
            } else {
                destino.sumar(tramo, mov.Monto.add(restante, MathContext.DECIMAL128));
                return;
            }
            saldo = restante;
        }
    }

    private static void cerrar(Fila intermediacion, Fila general, boolean sinDecimales, List<Fila> filas) {
        general.formatear(sinDecimales);
        intermediacion.formatear(sinDecimales);
        general.totalizar();
        intermediacion.totalizar();
        if (intermediacion.MontoPorPagarTotal.compareTo(BigDecimal.ZERO) > 0) {
            filas.add(intermediacion);
        }
        if (general.MontoPorPagarTotal.compareTo(BigDecimal.ZERO) > 0) {
            filas.add(general);
        }
    }

    private List<Saldo> cuentasPositivas(Entrada in) {
        SaldosCuentaInversion.Entrada solicitud = SaldosCuentaInversion.solicitud(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, in.p_FechaConsulta, in.p_IdEjecutivo, in.p_IdMoneda);
        solicitud.p_IdMoneda = in.p_IdMoneda;
        SaldosCuentaInversion.Salida saldos = SaldosCuentaInversion.consultar(colaboradores, solicitud);
        return SaldosCuentaInversion.utilizable(saldos)
                ? new ArrayList<>(SaldosCuentaInversion.conSigno(saldos.P_OUT_SALDOPORFECHAEFECTIVA, in.p_IdMoneda, signo -> signo > 0))
                : new ArrayList<>();
    }

    private List<Saldo> cuentasConsolidadas(Entrada in, List<ClienteConsolidado> clientes) {
        List<Saldo> cuentas = new ArrayList<>();
        SaldosCuentaInversion.Entrada solicitud = SaldosCuentaInversion.solicitud(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, in.p_FechaConsulta, in.p_IdEjecutivo, in.p_IdMoneda);
        solicitud.p_IdMoneda = in.p_IdMoneda;
        SaldosCuentaInversion.Salida saldos = SaldosCuentaInversion.consultar(colaboradores, solicitud);
        if (SaldosCuentaInversion.utilizable(saldos)) {
            clientes.addAll(SaldosCuentaInversion.consolidar(saldos.P_OUT_SALDOPORFECHAEFECTIVA, in.p_IdMoneda, signo -> signo > 0, cuentas));
        }
        return cuentas;
    }

    // ---------------------------------------------------------------------------------------------- JSON contract

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_FechaConsulta;
        public String p_IdMoneda = "";
        public Long p_IdEjecutivo = 0L;
        public String p_Consolidar = "S";
    }

    public static class Salida {
        public Long P_OUT_INFORMACIONCONTADOR;
        public Long P_OUT_ADVERTENCIASCONTADOR;
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public List<Fila> P_OUT_TABLA = new ArrayList<>();
    }

    /** Row of the report: amount per age tranche (A current, B 3-10 days, C 11-30, D older than 30). */
    public static class Fila {
        public Long IdPortafolio;
        public String CodigoPortafolio;
        public String DocumentoIndentidad;
        public String NombreCliente;
        public BigDecimal MontoPorPagarTotal = BigDecimal.ZERO;
        public BigDecimal TramoA = BigDecimal.ZERO;
        public BigDecimal TramoB = BigDecimal.ZERO;
        public BigDecimal TramoC = BigDecimal.ZERO;
        public BigDecimal TramoD = BigDecimal.ZERO;
        public String Observacion;
        public String ClasificacionContable;
        public String Relacionado;
        public BigDecimal CampoBD;

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
            MontoPorPagarTotal = AntiguedadComun.formatear(MontoPorPagarTotal, sinDecimales);
        }

        void totalizar() {
            MontoPorPagarTotal = AntiguedadComun.sumar(AntiguedadComun.sumar(AntiguedadComun.sumar(AntiguedadComun.sumar(MontoPorPagarTotal, TramoA), TramoB), TramoC), TramoD);
        }
    }
}
