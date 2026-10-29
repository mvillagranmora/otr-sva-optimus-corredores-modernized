package cl.bcs.tesoreria.javacall.ban;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import cl.bcs.tesoreria.javacall.kit.colaborador.RequisitosBasicos;
import cl.bcs.tesoreria.port.out.Colaboradores;

/**
 * Legacy {@code cl.bcs.tesoreria.ban.ConsultarLibretaBanco} (TES-B-09..22): the "libreta" of a bank account (movements that affected its balance on
 * a day) with a simulated running balance and, for foreign-currency movements, the closing exchange rate and the CLP equivalent. Account id
 * {@value #TODAS_LAS_CUENTAS} means every account of the bank (CSV route, answered in {@code P_OUT_MOVIMIENTOSLIBRETABANCOCSVTODOS}).
 *
 * <p>Exchange rate rules (same for both routes): a movement of a spot operation uses that operation (closing rate, or observed dollar for the
 * arbitrage nemotecnicos); a CLP movement has rate 1; any other movement that is neither spot nor forward uses the observed dollar of the day.
 */
@Service
public class ConsultarLibretaBancoService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarLibretaBancoService.class);

    private static final long TODAS_LAS_CUENTAS = -41L;

    private static final String SQL_CALCULA_SALDO_SIMULADO = SqlNombrado.recurso("ConsultarLibretaBanco_CalculaSaldoSimulado");
    private static final String SQL_CALCULA_SALDO_SIMULADO_CSV = SqlNombrado.recurso("ConsultarLibretaBanco_CalculaSaldoSimuladoCSV");
    private static final String SQL_CONCEPTOS_SPOT_FWD = SqlNombrado.recurso("ConsultarLibretaBanco_ConceptosSpotFwd");
    private static final String SQL_OPERACIONES_SPOT = SqlNombrado.recurso("ConsultarLibretaBanco_OperacionesSpot");
    private static final String SQL_DOLAR_OBSERVADO = SqlNombrado.recurso("ConsultarLibretaBanco_DolarObservado");

    // ---- JSON contract (legacy names)

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;
        public Long p_IdCuentaBancariaCorredora;
        public String p_IdMoneda;
        public Long p_IdBancoCorredora;
        public String p_FechaConsulta;
        public BigDecimal p_SaldoInicialSimulado;
    }

    public static class Salida {
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR;
        public List<MovimientoLibreta> P_OUT_MOVIMIENTOSLIBRETABANCO = new ArrayList<>();
        public List<SaldoSimulado> P_OUT_MOVIMIENTOSLIBRETABANCOCSVTODOS = new ArrayList<>();
    }

    /** Row of the statements and element of the CSV answer. */
    public static class SaldoSimulado {
        public Long IdComprobante;
        public Long IdComprobanteFormaDePago;
        public String NombreSocio;
        public String DocumentoIdentidadSocio;
        public String IdConcepto;
        public String NombreConcepto;
        public String NombreMedioDePago;
        public String IdMoneda;
        public BigDecimal MontoIngreso;
        public BigDecimal MontoEgreso;
        public BigDecimal SaldoDisponible;
        public BigDecimal SaldoContable;
        public BigDecimal SaldoSimulado;
        public java.sql.Date FechaAfectaSaldo;
        public java.sql.Date FechaLiquidacion;
        public String IdEstado;
        public String NombreEstado;
        public Long NumeroDocumentoCorredora;
        public String TipoSocioNegocio;
        public Long IdPortafolio;
        public String CodigoPortafolio;
        public String Alias;
        public String Comentario;
        public BigDecimal TcCierre = BigDecimal.ZERO;
        public BigDecimal MontoEquivalente = BigDecimal.ZERO;
    }

    /** Element of the normal answer: the statement row plus the final simulated balance. */
    public static class MovimientoLibreta extends SaldoSimulado {
        public BigDecimal SaldoFinalSimulado;

        static MovimientoLibreta de(SaldoSimulado origen, BigDecimal saldoFinalSimulado) {
            MovimientoLibreta m = new MovimientoLibreta();
            m.SaldoFinalSimulado = saldoFinalSimulado;
            for (Field campo : SaldoSimulado.class.getFields()) {
                try {
                    campo.set(m, campo.get(origen));
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException(e);
                }
            }
            return m;
        }
    }

    public static class ConceptoSpotFwd {
        public String IdConcepto;
        public String Origen;
    }

    public static class ComprobanteSpotFwd {
        public Long IdComprobante;
        public Long IdOperacionSpot;
        public String IdMonedaPrincipal;
        public String IdMonedaSecundaria;
        public BigDecimal MontoMonedaPrincipal;
        public BigDecimal MontoMonedaSecundaria;
        public BigDecimal TipoCambioCierre;
        public BigDecimal ParidadCierre;
        public String Nemotecnico;
        public BigDecimal DolarObservado;
    }

    public static class DolarObservadoLibreta {
        public String IdParametroFinanciero;
        public Date Fecha;
        public BigDecimal Valor;
    }

    private final SqlNombrado sql;
    private final FormatoMoneda formatoMoneda;
    private final Colaboradores colaboradores;

    public ConsultarLibretaBancoService(NamedParameterJdbcTemplate jdbc, FormatoMoneda formatoMoneda, Colaboradores colaboradores) {
        this.sql = new SqlNombrado(jdbc);
        this.formatoMoneda = formatoMoneda;
        this.colaboradores = colaboradores;
    }

    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public JsonObject execute(JsonObject jin) throws Exception {
        Gson gson = JsonLegacy.gson();
        Entrada in = gson.fromJson(jin.toString(), Entrada.class);
        Salida out = logica(in);
        out.P_OUT_ERRORESCONTADOR = MensajeSalida.contarErrores(out.P_OUT_MENSAJES);
        return JsonParser.parseString(gson.toJson(out)).getAsJsonObject();
    }

    Salida logica(Entrada in) throws Exception {
        Salida out = new Salida();
        log.debug("== INICIO CONSULTA LIBRETA BANCO ==");

        RequisitosBasicos.Salida basicos = RequisitosBasicos.revisar(colaboradores, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
        if (MensajeSalida.contarErrores(basicos.p_out_Mensajes) > 0) {
            out.P_OUT_MENSAJES.addAll(basicos.p_out_Mensajes);
            return out;
        }

        Map<String, Object> params = new HashMap<>();
        params.put("IdBancoCorredora", in.p_IdBancoCorredora);
        params.put("IdEmpresa", in.p_IdEmpresa);
        params.put("FechaConsultaDesde", in.p_FechaConsulta + " 00:00:00");
        params.put("FechaConsultaHasta", in.p_FechaConsulta + " 23:59:59");

        List<ConceptoSpotFwd> conceptos = sql.consultar(SQL_CONCEPTOS_SPOT_FWD, ConceptoSpotFwd.class);
        Set<String> conceptosSpot = conceptos.stream().filter(b -> "MSP".equalsIgnoreCase(b.Origen)).map(b -> b.IdConcepto).collect(Collectors.toSet());
        Set<String> conceptosFwd = conceptos.stream().filter(b -> "MFW".equalsIgnoreCase(b.Origen)).map(b -> b.IdConcepto).collect(Collectors.toSet());

        Map<String, Object> paramsDolar = new HashMap<>();
        paramsDolar.put("Fecha", in.p_FechaConsulta);
        List<DolarObservadoLibreta> dolares = sql.consultar(SQL_DOLAR_OBSERVADO, paramsDolar, DolarObservadoLibreta.class);
        BigDecimal valorDO = dolares.stream().filter(x -> "DO".equals(x.IdParametroFinanciero)).map(x -> x.Valor).findFirst().orElse(BigDecimal.ZERO);
        BigDecimal dolarObservado = formatoMoneda.formatear(valorDO, FormatoMoneda.MONTO, "USD", "", "");

        List<SaldoSimulado> saldos = new ArrayList<>();
        if (in.p_IdCuentaBancariaCorredora == TODAS_LAS_CUENTAS) {
            List<SaldoSimulado> todos = sql.consultar(SQL_CALCULA_SALDO_SIMULADO_CSV, params, SaldoSimulado.class);
            for (SaldoSimulado s : todos) {
                // LEGACY DEFECT kept (see README TES-B-22): a null p_SaldoInicialSimulado is a NullPointerException here
                s.SaldoSimulado = s.SaldoSimulado.add(in.p_SaldoInicialSimulado);
            }
            completarEquivalentes(todos, conceptosSpot, conceptosFwd, dolares, dolarObservado);
            out.P_OUT_MOVIMIENTOSLIBRETABANCOCSVTODOS = todos;
        } else {
            params.put("IdCuentaBancariaCorredora", in.p_IdCuentaBancariaCorredora);
            params.put("IdMoneda", in.p_IdMoneda);
            saldos = sql.consultar(SQL_CALCULA_SALDO_SIMULADO, params, SaldoSimulado.class);
            for (SaldoSimulado s : saldos) {
                // LEGACY DEFECT kept (see README TES-B-21): a null p_SaldoInicialSimulado is a NullPointerException here
                s.SaldoSimulado = s.SaldoSimulado.add(in.p_SaldoInicialSimulado);
            }
        }

        // the final simulated balance is that of the last row of the normal route (the CSV route leaves `saldos` empty: LEGACY, balance 0)
        BigDecimal saldoFinal = saldos.isEmpty() ? BigDecimal.ZERO : saldos.get(saldos.size() - 1).SaldoSimulado;
        List<MovimientoLibreta> movimientos = new ArrayList<>();
        for (SaldoSimulado s : saldos) {
            movimientos.add(MovimientoLibreta.de(s, saldoFinal));
        }
        completarEquivalentes(movimientos, conceptosSpot, conceptosFwd, dolares, dolarObservado);
        out.P_OUT_MOVIMIENTOSLIBRETABANCO = movimientos;
        log.debug("== TERMINO CONSULTA LIBRETA BANCO ==");
        return out;
    }

    /**
     * Fills TcCierre / MontoEquivalente of each movement: spot operations first, then forwards in CLP, then everything else that is still
     * unconverted. A failure on one movement is logged and the movement is left as it was (legacy).
     */
    private void completarEquivalentes(List<? extends SaldoSimulado> movimientos, Set<String> conceptosSpot, Set<String> conceptosFwd,
            List<DolarObservadoLibreta> dolares, BigDecimal dolarObservado) throws Exception {
        List<Long> comprobantesSpot = movimientos.stream().filter(a -> conceptosSpot.contains(a.IdConcepto)).map(a -> a.IdComprobante).collect(Collectors.toList());
        List<Long> comprobantesFwd = movimientos.stream().filter(a -> conceptosFwd.contains(a.IdConcepto)).map(a -> a.IdComprobante).collect(Collectors.toList());

        if (!comprobantesSpot.isEmpty()) {
            Map<String, Object> paramSpot = new HashMap<>();
            paramSpot.put("idsComprobante", comprobantesSpot);
            List<ComprobanteSpotFwd> operaciones = sql.consultar(SQL_OPERACIONES_SPOT, paramSpot, ComprobanteSpotFwd.class);
            Map<Long, List<ComprobanteSpotFwd>> porComprobante = operaciones.stream().collect(Collectors.groupingBy(c -> c.IdComprobante));
            // LEGACY DEFECT kept (see README TES-B-18): a comprobante that resolves to two spot operations is ignored (only groups of one are used)
            Map<Long, ComprobanteSpotFwd> operacionPorComprobante = porComprobante.entrySet().stream().filter(e -> e.getValue().size() == 1)
                    .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().get(0)));
            for (SaldoSimulado u : movimientos) {
                try {
                    ComprobanteSpotFwd operacion = operacionPorComprobante.get(u.IdComprobante);
                    if (operacion == null) {
                        u.TcCierre = BigDecimal.ZERO;
                        u.MontoEquivalente = BigDecimal.ZERO;
                        continue;
                    }
                    BigDecimal montoBase = u.MontoEgreso.compareTo(BigDecimal.ZERO) != 0 ? u.MontoEgreso : u.MontoIngreso;
                    if ("CLP".equalsIgnoreCase(u.IdMoneda)) {
                        u.TcCierre = BigDecimal.ONE;
                        u.MontoEquivalente = montoBase;
                        continue;
                    }
                    boolean esArbitraje = "ARBINT".equalsIgnoreCase(operacion.Nemotecnico) || "ARBDIS".equalsIgnoreCase(operacion.Nemotecnico);
                    if (!esArbitraje) {
                        u.TcCierre = operacion.TipoCambioCierre;
                        u.MontoEquivalente = formatoMoneda.formatear(montoBase.multiply(operacion.TipoCambioCierre).setScale(2, RoundingMode.HALF_UP),
                                FormatoMoneda.MONTO, "CLP", "SPT", "");
                        continue;
                    }
                    BigDecimal tc;
                    if ("USD".equalsIgnoreCase(u.IdMoneda)) {
                        tc = formatoMoneda.formatear(operacion.DolarObservado, FormatoMoneda.TIPOCAMBIO, u.IdMoneda, "SPT", "");
                    } else {
                        tc = formatoMoneda.formatear(operacion.DolarObservado.multiply(operacion.ParidadCierre), FormatoMoneda.TIPOCAMBIO, u.IdMoneda, "SPT", "");
                    }
                    u.TcCierre = tc;
                    u.MontoEquivalente = formatoMoneda.formatear(montoBase.multiply(u.TcCierre).setScale(2, RoundingMode.HALF_UP), FormatoMoneda.MONTO, "CLP",
                            "SPT", "");
                } catch (Exception e) {
                    log.error("Error en comprobante {}", u.IdComprobante, e);
                }
            }
        }

        if (!comprobantesFwd.isEmpty()) {
            Set<Long> forwards = new HashSet<>(comprobantesFwd);
            for (SaldoSimulado mov : movimientos) {
                // LEGACY DEFECT kept (see README TES-B-17): the guard already requires CLP, so the foreign-currency branch of the legacy is
                // unreachable and a USD forward keeps TcCierre 0 / MontoEquivalente 0
                if (forwards.contains(mov.IdComprobante) && "CLP".equalsIgnoreCase(mov.IdMoneda)) {
                    mov.TcCierre = BigDecimal.ONE;
                    mov.MontoEquivalente = mov.MontoEgreso.compareTo(BigDecimal.ZERO) != 0 ? mov.MontoEgreso : mov.MontoIngreso;
                }
            }
        }

        for (SaldoSimulado u : movimientos) {
            try {
                if ("CLP".equalsIgnoreCase(u.IdMoneda)) {
                    u.TcCierre = BigDecimal.ONE;
                    u.MontoEquivalente = u.MontoEgreso.compareTo(BigDecimal.ZERO) != 0 ? u.MontoEgreso : u.MontoIngreso;
                } else if (u.TcCierre.equals(BigDecimal.ZERO) && u.MontoEquivalente.equals(BigDecimal.ZERO)) {
                    boolean esSpot = comprobantesSpot.contains(u.IdComprobante);
                    boolean esFwd = comprobantesFwd.contains(u.IdComprobante);
                    BigDecimal montoBase = u.MontoEgreso.compareTo(BigDecimal.ZERO) != 0 ? u.MontoEgreso : u.MontoIngreso;
                    if (!esSpot && !esFwd) {
                        if ("USD".equalsIgnoreCase(u.IdMoneda)) {
                            u.TcCierre = dolarObservado;
                            u.MontoEquivalente = formatoMoneda.formatear(montoBase.multiply(u.TcCierre).setScale(2, RoundingMode.HALF_UP), FormatoMoneda.MONTO,
                                    "CLP", "", "");
                        } else {
                            BigDecimal valorPorMoneda = dolares.stream().filter(x -> u.IdMoneda.equals(x.IdParametroFinanciero)).map(x -> x.Valor).findFirst()
                                    .orElse(BigDecimal.ZERO);
                            u.TcCierre = formatoMoneda.formatear(dolarObservado.multiply(valorPorMoneda), FormatoMoneda.TIPOCAMBIO, u.IdMoneda, "SPT", "");
                            u.MontoEquivalente = formatoMoneda.formatear(montoBase.multiply(u.TcCierre).setScale(2, RoundingMode.HALF_UP), FormatoMoneda.MONTO,
                                    "CLP", "", "");
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error en comprobante {}", u.IdComprobante, e);
            }
        }
    }
}
