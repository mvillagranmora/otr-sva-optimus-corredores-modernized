package cl.bcs.cuentainversion.service;

import cl.bcs.cuentainversion.port.out.ColaboradoresReportes;
import cl.bcs.cuentainversion.reportes.AntiguedadComun;
import cl.bcs.cuentainversion.reportes.AntiguedadComun.ClientePortafolio;
import cl.bcs.cuentainversion.reportes.AntiguedadComun.CuentaPaso;
import cl.bcs.cuentainversion.reportes.AntiguedadComun.Moneda;
import cl.bcs.cuentainversion.reportes.ConfiguracionSistema;
import cl.bcs.cuentainversion.reportes.JsonReportes;
import cl.bcs.cuentainversion.reportes.MensajeSalida;
import cl.bcs.cuentainversion.reportes.Mensajeria;
import cl.bcs.cuentainversion.reportes.ParidadReportes;
import cl.bcs.cuentainversion.reportes.ParidadReportes.CalculoEntrada;
import cl.bcs.cuentainversion.reportes.ParidadReportes.CalculoSalida;
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
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Aging of the debt of the clients by age tranche, from the daily balance snapshots (legacy
 * {@code ConsultaAntiguedadDeudasCuentaInversion}, CTA-B-93..108).
 *
 * <p>Accounts with a negative balance are staged; for every one of the last 32 days the saved snapshots
 * ({@code CTA.SaldosReporteAntiguedad}, written by the Guardar/Actualizar services) are assigned to the tranche of their day and
 * the tranche amounts are then netted (a later payment reduces the older debt, CTA-B-98/99/103). With several currencies and
 * Consolidar = N everything is converted to CLP through the {@code CalcularParidad} collaborator (CTA-B-98).
 */
@Service
public class ConsultaAntiguedadDeudasCuentaInversionService {

    private static final Logger log = LoggerFactory.getLogger(ConsultaAntiguedadDeudasCuentaInversionService.class);

    private static final String SQL_INSERT_PASO = SqlReportes.recurso("ConsultaAntiguedadDeudasCuentaInversion_InsertCuentaInversionTablaDePaso");
    private static final String SQL_DELETE_PASO = SqlReportes.recurso("ConsultaAntiguedadDeudasCuentaInversion_DeleteCuentaInversionTablaDePaso");
    private static final String SQL_MONEDAS = SqlReportes.recurso("ConsultaAntiguedadDeudasCuentaInversion_SelectDistintasMonedasCuentasInversion");
    private static final String SQL_DATOS_CLIENTE = SqlReportes.recurso("ConsultaAntiguedadDeudasCuentaInversion_SelectDatosCliente");
    private static final String SQL_SALDOS = SqlReportes.recurso("ConsultaAntiguedadDeudasCuentaInversion_SelectSaldosReporteAntiguedad");
    private static final String SQL_FECHA_DEUDA = SqlReportes.recurso("ConsultaAntiguedadDeudasCuentaInversion_SelectFechaDeudaAntiguedad");

    private static final String TODAS = "TODAS";
    private static final DateTimeFormatter FORMATO_FECHA_DEUDA = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final SqlReportes sql;
    private final ColaboradoresReportes colaboradores;

    public ConsultaAntiguedadDeudasCuentaInversionService(NamedParameterJdbcTemplate jdbc, ColaboradoresReportes colaboradores) {
        this.sql = new SqlReportes(jdbc);
        this.colaboradores = colaboradores;
    }

    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject params) throws Exception {
        return JsonReportes.escribir(consultar(JsonReportes.leer(params, Entrada.class)));
    }

    Salida consultar(Entrada in) throws Exception {
        log.info("INICIO::ConsultaAntiguedadDeudasCuentaInversion");
        Salida out = new Salida();
        Mensajeria mensajes = new Mensajeria(sql, in.p_IdEmpresa);
        long idTrabajo = (long) (Math.random() * 10000000000L);
        new ConfiguracionSistema(sql).cadena(in.p_IdEmpresa, ConfiguracionSistema.PLATAFORMA, "PLA.FechaSistema");

        JsonReportes.empresa(colaboradores, in.p_IdEmpresa); // must exist; the country only fed the (unused) holiday calendar

        List<Moneda> monedas = new ArrayList<>();
        if (in.p_IdMoneda != null && !in.p_IdMoneda.isEmpty() && !TODAS.equals(in.p_IdMoneda)) {
            Moneda m = new Moneda();
            m.IdMoneda = in.p_IdMoneda;
            monedas.add(m);
        } else {
            monedas = sql.consultar(SQL_MONEDAS, Map.of(), Moneda.class);
        }
        Map<String, Moneda> porMoneda = monedas.stream().collect(Collectors.toMap(m -> m.IdMoneda, Function.identity()));
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

        boolean eliminada = vaciarPaso();
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

        List<SaldoReporte> saldosReporte = sql.consultar(SQL_SALDOS, Map.of(
                "IdTrabajo", idTrabajo,
                "FechaInicio", tramos.rango(Tramo.D).fin().toString(),
                "FechaFin", tramos.rango(Tramo.A).fin().toString()), SaldoReporte.class);

        // With several currencies (and no consolidation) everything is expressed in CLP.
        if (porMoneda.size() > 1 && in.p_Consolidar.equalsIgnoreCase("N")) {
            List<CuentaPaso> enClp = new ArrayList<>(paso.stream().filter(c -> c.IdMoneda.equals("CLP")).toList());
            enClp.addAll(convertirCuentas(in, paso.stream().filter(c -> !c.IdMoneda.equals("CLP")).toList()));
            paso = enClp;

            List<SaldoReporte> enClpSaldos = new ArrayList<>(saldosReporte.stream().filter(s -> s.IdMoneda.equalsIgnoreCase("CLP")).toList());
            enClpSaldos.addAll(convertirSaldos(in, saldosReporte.stream().filter(s -> !s.IdMoneda.equalsIgnoreCase("CLP")).toList()));
            saldosReporte = enClpSaldos;
        }

        boolean sinDecimales = monedas.get(0).IdMoneda.equalsIgnoreCase("CLP") || porMoneda.size() > 1;
        boolean separa = in.p_SeparaIntermediacion.equalsIgnoreCase("S");
        List<Fila> filas = new ArrayList<>();

        if (consolidado) {
            for (ClienteConsolidado cliente : clientesConsolidados) {
                ClientePortafolio datos = porPortafolio.get(cliente.idPortafolios.get(0));
                Fila intermediacion = fila(datos, null, cliente.saldoFinal, "", "Cuenta de Intermediacion");
                Fila general = fila(datos, null, cliente.saldoFinal, "", clasificacion(datos));
                List<SaldoReporte> deCliente = saldosReporte.stream().filter(s -> s.IdCliente.equals(cliente.idCliente))
                        .sorted(Comparator.comparing(s -> s.FechaSistema)).toList();
                fechasDeuda(separa, datos.IdCliente, cliente.idPortafolios, monedas.get(0).IdMoneda, general, intermediacion);
                procesar(separa, deCliente, intermediacion, general, tramos, sinDecimales, filas);
            }
            out.P_OUT_TABLA.addAll(filas);
        } else {
            for (CuentaPaso cuenta : paso) {
                ClientePortafolio datos = porPortafolio.get(cuenta.IdPortafolio);
                String observacion = cuenta.Observacion == null ? "" : cuenta.Observacion;
                Fila intermediacion = fila(datos, cuenta.IdPortafolio, cuenta.SaldoFinal, observacion, "Cuenta de Intermediacion");
                intermediacion.CodigoPortafolio = datos.CodigoPortafolio;
                Fila general = fila(datos, cuenta.IdPortafolio, cuenta.SaldoFinal, observacion, clasificacion(datos));
                general.CodigoPortafolio = datos.CodigoPortafolio;
                List<SaldoReporte> deCuenta = saldosReporte.stream()
                        .filter(s -> s.IdCliente.equals(cuenta.IdCliente) && s.IdPortafolio.equals(cuenta.IdPortafolio) && s.IdMoneda.equalsIgnoreCase(cuenta.IdMoneda))
                        .sorted(Comparator.comparing(s -> s.FechaSistema)).toList();
                fechasDeuda(separa, datos.IdCliente, List.of(datos.IdPortafolio), "", general, intermediacion);
                procesar(separa, deCuenta, intermediacion, general, tramos, sinDecimales, filas);
            }
            if (in.p_AgrupaPortafolios.equalsIgnoreCase("S")) {
                out.P_OUT_TABLA.addAll(agruparPorPortafolio(porPortafolio, filas));
            } else {
                out.P_OUT_TABLA.addAll(filas);
            }
        }

        if (!vaciarPaso()) {
            out.P_OUT_ERRORESCONTADOR++;
            out.P_OUT_MENSAJES.add(mensajes.error("ERROR_AL_GENERAR_REPORTE"));
            return out;
        }
        log.info("FIN::ConsultaAntiguedadDeudasCuentaInversion");
        return out;
    }

    /** True unless the DELETE failed (the legacy swallowed the SQL exception). */
    private boolean vaciarPaso() {
        try {
            sql.ejecutar(SQL_DELETE_PASO);
            return true;
        } catch (DataAccessException e) {
            return false;
        }
    }

    private static String clasificacion(ClientePortafolio datos) {
        // LEGACY DEFECT kept (see README CTA-B-09): the data query answers 'SI' for a related client, the legacy compares it with 'S'.
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
        f.Observacion = observacion;
        return f;
    }

    /** Oldest day with a negative saved balance (per type when the intermediation is reported apart). */
    private void fechasDeuda(boolean separa, Long idCliente, List<Long> idPortafolios, String idMoneda, Fila general, Fila intermediacion) {
        if (separa) {
            general.FechaDeuda = fechaDeuda(idCliente, idPortafolios, "G", idMoneda);
            intermediacion.FechaDeuda = fechaDeuda(idCliente, idPortafolios, "I", idMoneda);
        } else {
            general.FechaDeuda = fechaDeuda(idCliente, idPortafolios, "", idMoneda);
        }
    }

    private String fechaDeuda(Long idCliente, List<Long> idPortafolios, String tipoSaldo, String idMoneda) {
        Map<String, Object> p = new HashMap<>();
        p.put("IdCliente", idCliente);
        p.put("IdPortafolio", idPortafolios);
        p.put("TipoSaldo", tipoSaldo);
        p.put("IdMoneda", idMoneda);
        List<FechaDeuda> r = sql.consultar(SQL_FECHA_DEUDA, p, FechaDeuda.class);
        return !r.isEmpty() && r.get(0).FechaSistema != null ? r.get(0).FechaSistema.toLocalDate().format(FORMATO_FECHA_DEUDA) : null;
    }

    private static void procesar(boolean separa, List<SaldoReporte> saldos, Fila intermediacion, Fila general, TramosAntiguedad tramos,
                                 boolean sinDecimales, List<Fila> filas) {
        asignarSaldosPorTramo(separa, saldos, intermediacion, general, tramos);
        intermediacion.repasar();
        general.repasar();
        general.formatear(sinDecimales);
        intermediacion.formatear(sinDecimales);
        general.totalizar();
        intermediacion.totalizar();
        if (separa) {
            filas.add(general);
            if (intermediacion.MontoPorCobrarTotal.compareTo(BigDecimal.ZERO) > 0) {
                filas.add(intermediacion);
            }
        } else {
            // the total above was computed before this merge, so it does not include the intermediation tranches
            general.TramoA = AntiguedadComun.sumar(general.TramoA, intermediacion.TramoA);
            general.TramoB = AntiguedadComun.sumar(general.TramoB, intermediacion.TramoB);
            general.TramoC = AntiguedadComun.sumar(general.TramoC, intermediacion.TramoC);
            general.TramoD = AntiguedadComun.sumar(general.TramoD, intermediacion.TramoD);
            filas.add(general);
        }
    }

    /** One row per portfolio of the client query, adding up the rows of its accounts (totals by portfolio). */
    private List<Fila> agruparPorPortafolio(Map<Long, ClientePortafolio> porPortafolio, List<Fila> filas) {
        List<Fila> agrupadas = new ArrayList<>();
        for (Map.Entry<Long, ClientePortafolio> e : porPortafolio.entrySet()) {
            ClientePortafolio datos = e.getValue();
            Fila total = fila(datos, datos.IdPortafolio, null, null, clasificacion(datos));
            total.CampoBD = null;
            total.FechaDeuda = fechaDeuda(datos.IdCliente, List.of(datos.IdPortafolio), "", "");
            for (Fila previa : filas.stream().filter(f -> f.IdPortafolio.equals(e.getKey())).toList()) {
                total.MontoPorCobrarTotal = AntiguedadComun.sumar(total.MontoPorCobrarTotal, previa.MontoPorCobrarTotal);
                total.TramoA = AntiguedadComun.sumar(total.TramoA, previa.TramoA);
                total.TramoB = AntiguedadComun.sumar(total.TramoB, previa.TramoB);
                total.TramoC = AntiguedadComun.sumar(total.TramoC, previa.TramoC);
                total.TramoD = AntiguedadComun.sumar(total.TramoD, previa.TramoD);
            }
            agrupadas.add(total);
        }
        return agrupadas;
    }

    /**
     * For each day, oldest first, the saved snapshot of the day becomes the amount of its tranche. A day that nets to zero or to a
     * credit clears the older tranches. A day is used only when it has BOTH intermediation and general rows (CTA-B-97).
     */
    private static void asignarSaldosPorTramo(boolean separa, List<SaldoReporte> saldos, Fila intermediacion, Fila general, TramosAntiguedad tramos) {
        for (LocalDate fecha : tramos.fechas()) {
            Tramo tramo = tramos.de(fecha);
            List<SaldoReporte> delDia = saldos.stream().filter(s -> s.FechaSistema.toLocalDate().equals(fecha)).toList();
            if (delDia.isEmpty()) {
                continue;
            }
            BigDecimal totalDia = delDia.stream().map(s -> s.Saldo).reduce(BigDecimal.ZERO, BigDecimal::add);
            if (totalDia.compareTo(BigDecimal.ZERO) >= 0) {
                intermediacion.borrarDeudaAnterior(tramo);
                general.borrarDeudaAnterior(tramo);
            }
            List<SaldoReporte> deIntermediacion = delDia.stream().filter(s -> s.TipoSaldo.equalsIgnoreCase("I")).toList();
            List<SaldoReporte> deGeneral = delDia.stream().filter(s -> s.TipoSaldo.equalsIgnoreCase("G")).toList();
            // LEGACY DEFECT kept (see README CTA-B-11): days with only G or only I rows are ignored.
            if (!deIntermediacion.isEmpty() && !deGeneral.isEmpty()) {
                BigDecimal saldoI = deIntermediacion.stream().map(s -> s.Saldo).reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal saldoG = deGeneral.stream().map(s -> s.Saldo).reduce(BigDecimal.ZERO, BigDecimal::add);
                if (separa) {
                    intermediacion.fijar(tramo, saldoI);
                    general.fijar(tramo, saldoG);
                } else {
                    general.fijar(tramo, AntiguedadComun.sumar(saldoG, saldoI));
                }
            }
        }
    }

    private List<CuentaPaso> convertirCuentas(Entrada in, List<CuentaPaso> enMonedaExtranjera) throws Exception {
        List<CalculoEntrada> calculos = new ArrayList<>();
        for (CuentaPaso c : enMonedaExtranjera) {
            calculos.add(CalculoEntrada.de(c.IdCuentaInversion, in.p_FechaConsulta, c.IdMoneda, "CLP", c.SaldoFinal));
        }
        Map<Long, CalculoSalida> convertidos = indexar(ParidadReportes.calcular(colaboradores, in.p_IdEmpresa, calculos));
        List<CuentaPaso> salida = new ArrayList<>();
        for (CuentaPaso c : enMonedaExtranjera) {
            CuentaPaso n = new CuentaPaso();
            n.IdCuentaInversion = c.IdCuentaInversion;
            n.IdCliente = c.IdCliente;
            n.IdMoneda = c.IdMoneda;
            n.IdPortafolio = c.IdPortafolio;
            CalculoSalida calculo = convertidos.get(c.IdCuentaInversion);
            if (calculo != null) {
                n.SaldoFinal = calculo.MontoDestino == null ? BigDecimal.ZERO : calculo.MontoDestino;
                n.Observacion = calculo.MontoDestino == null ? "Error en la paridad" : "";
            }
            salida.add(n);
        }
        return salida;
    }

    private List<SaldoReporte> convertirSaldos(Entrada in, List<SaldoReporte> enMonedaExtranjera) throws Exception {
        List<CalculoEntrada> calculos = new ArrayList<>();
        for (SaldoReporte s : enMonedaExtranjera) {
            calculos.add(CalculoEntrada.de(s.NumeroRegistro, in.p_FechaConsulta, s.IdMoneda, "CLP", s.Saldo));
        }
        Map<Long, CalculoSalida> convertidos = indexar(ParidadReportes.calcular(colaboradores, in.p_IdEmpresa, calculos));
        List<SaldoReporte> salida = new ArrayList<>();
        for (SaldoReporte s : enMonedaExtranjera) {
            SaldoReporte n = new SaldoReporte();
            n.FechaSistema = s.FechaSistema;
            n.IdCliente = s.IdCliente;
            n.IdPortafolio = s.IdPortafolio;
            n.IdMoneda = s.IdMoneda;
            n.TipoSaldo = s.TipoSaldo;
            n.NumeroRegistro = s.NumeroRegistro;
            CalculoSalida calculo = convertidos.get(s.NumeroRegistro);
            n.Saldo = calculo == null || calculo.MontoDestino == null ? BigDecimal.ZERO : calculo.MontoDestino;
            salida.add(n);
        }
        return salida;
    }

    /** Legacy {@code List2Hash} by {@code IdInterno}: a repeated key is an error. */
    private static Map<Long, CalculoSalida> indexar(List<CalculoSalida> calculos) throws Exception {
        Map<Long, CalculoSalida> m = new HashMap<>();
        for (CalculoSalida c : calculos) {
            if (m.put(c.IdInterno, c) != null) {
                throw new Exception("key field is not unique");
            }
        }
        return m;
    }

    private List<Saldo> cuentasNegativas(Entrada in, List<Moneda> monedas) {
        List<Saldo> negativas = new ArrayList<>();
        for (Moneda moneda : monedas) {
            SaldosCuentaInversion.Entrada solicitud = SaldosCuentaInversion.solicitud(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, in.p_FechaConsulta, in.p_IdEjecutivo, moneda.IdMoneda);
            solicitud.p_IdMoneda = moneda.IdMoneda;
            SaldosCuentaInversion.Salida saldos = SaldosCuentaInversion.consultar(colaboradores, solicitud);
            if (SaldosCuentaInversion.utilizable(saldos)) {
                negativas.addAll(SaldosCuentaInversion.conSigno(saldos.P_OUT_SALDOPORFECHAEFECTIVA, moneda.IdMoneda, signo -> signo < 0));
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

    /** Saved daily snapshot read by the SQL (and, after conversion, in CLP). */
    public static class SaldoReporte {
        public Long NumeroRegistro;
        public Date FechaSistema;
        public Long IdCliente;
        public Long IdPortafolio;
        public String IdMoneda;
        public String TipoSaldo;
        public BigDecimal Saldo;
    }

    public static class FechaDeuda {
        public Date FechaSistema;
    }

    /** Row of the report: debt per age tranche (A current, B 3-10 days, C 11-30, D older than 30). */
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
        public String FechaDeuda;
        public BigDecimal CampoBD;
        public BigDecimal Matematica;

        void fijar(Tramo tramo, BigDecimal monto) {
            switch (tramo) {
                case A -> TramoA = monto;
                case B -> TramoB = monto;
                case C -> TramoC = monto;
                case D -> TramoD = monto;
                case NINGUNO -> {
                }
            }
        }

        /** A day with no debt left clears the tranches older than its own. */
        void borrarDeudaAnterior(Tramo tramo) {
            switch (tramo) {
                case A -> TramoB = TramoC = TramoD = BigDecimal.ZERO;
                case B -> TramoC = TramoD = BigDecimal.ZERO;
                case C -> TramoD = BigDecimal.ZERO;
                default -> {
                }
            }
        }

        /**
         * Turns the cumulative snapshot amounts into the debt of each tranche: positive amounts are ignored, each tranche keeps only the
         * increase over the next older one, and a positive remainder is netted against the older debt (CTA-B-98, 99, 103).
         */
        void repasar() {
            BigDecimal d = TramoD.signum() > 0 ? BigDecimal.ZERO : TramoD;
            BigDecimal c = TramoC.signum() > 0 ? BigDecimal.ZERO : TramoC;
            BigDecimal b = TramoB.signum() > 0 ? BigDecimal.ZERO : TramoB;
            BigDecimal a = TramoA.signum() > 0 ? BigDecimal.ZERO : TramoA;
            BigDecimal[] t = new BigDecimal[4];
            t[3] = d;
            t[2] = d.compareTo(c) == 0 ? BigDecimal.ZERO : c.subtract(d, java.math.MathContext.DECIMAL128);
            t[1] = c.compareTo(b) == 0 ? BigDecimal.ZERO : b.subtract(c, java.math.MathContext.DECIMAL128);
            t[0] = b.compareTo(a) == 0 ? BigDecimal.ZERO : a.subtract(b, java.math.MathContext.DECIMAL128);
            // LEGACY DEFECT kept (see README CTA-B-11): empty intermediate tranches net older debt into tranche A.
            if (t[2].signum() > 0) {
                t[3] = AntiguedadComun.sumar(t[3], t[2]);
                t[2] = BigDecimal.ZERO;
            }
            if (t[1].signum() > 0) {
                if (AntiguedadComun.sumar(t[2], t[1]).signum() <= 0) {
                    t[2] = AntiguedadComun.sumar(t[2], t[1]);
                    t[1] = BigDecimal.ZERO;
                } else {
                    arrastrar(t, 2);
                }
            }
            if (t[0].signum() > 0) {
                if (AntiguedadComun.sumar(t[1], t[0]).signum() <= 0) {
                    t[1] = AntiguedadComun.sumar(t[0], t[1]);
                    t[0] = BigDecimal.ZERO;
                } else {
                    arrastrar(t, 1);
                }
            }
            TramoD = t[3];
            TramoC = t[2];
            TramoB = t[1];
            TramoA = t[0];
        }

        /** Moves the amount of each tranche into the next older one from {@code desde} on, until the accumulated amount is not a debt. */
        private static void arrastrar(BigDecimal[] t, int desde) {
            for (int i = desde; i < 4; i++) {
                BigDecimal acumulado = AntiguedadComun.sumar(t[i], t[i - 1]);
                t[i] = acumulado;
                t[i - 1] = BigDecimal.ZERO;
                if (acumulado.signum() <= 0) {
                    break;
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
