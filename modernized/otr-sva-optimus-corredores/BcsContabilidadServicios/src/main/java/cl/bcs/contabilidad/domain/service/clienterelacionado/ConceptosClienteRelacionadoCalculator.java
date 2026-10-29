package cl.bcs.contabilidad.domain.service.clienterelacionado;

import cl.bcs.contabilidad.domain.model.clienterelacionado.Comprobante;
import cl.bcs.contabilidad.domain.model.clienterelacionado.CondicionConcepto;
import cl.bcs.contabilidad.domain.model.clienterelacionado.ConceptoNormativoConfig;
import cl.bcs.contabilidad.domain.model.clienterelacionado.FuentesOperaciones;
import cl.bcs.contabilidad.domain.model.clienterelacionado.OperacionClienteRelacionado;
import cl.bcs.contabilidad.domain.model.clienterelacionado.OperacionDolar;
import cl.bcs.contabilidad.domain.model.clienterelacionado.PactoTermino;
import cl.bcs.contabilidad.domain.model.clienterelacionado.ResultadoConsultaClienteRelacionado;
import cl.bcs.contabilidad.domain.model.clienterelacionado.TotalConcepto;
import cl.bcs.contabilidad.domain.model.clienterelacionado.ValorCondicion;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * In-memory part of legacy {@code ConsultarOperacionesClienteRelacionado.logica()} (COCR:183-350) and
 * {@code obtenerMontoDesdeComprobante()} (COCR:362-418): rules CONT-D-10..25.
 *
 * <ul>
 *   <li>An operation counts for a concept only when it satisfies every condition group. Group names compare
 *       case-insensitively, configured values exactly. Unknown groups never match. TipoDocumento is skipped for
 *       VENTER/COMTER/COMPAC/VENPAC. The "TipoDato" group selects comprobante amounts and is not a filter.</li>
 *   <li>A matched value with operator "-" subtracts the amount; the count still adds one.</li>
 *   <li>A FolioOperacion counts once per concept; a null folio is never deduplicated.</li>
 *   <li>Concepts are computed over comprobantes, prestamos, pactos, spots, forwards and terminos-cartera, in that
 *       order. The listed operations are comprobantes, spots, forwards, pactos and terminos; for negative document
 *       types, comprobante and dollar amounts are negated after the totals are accumulated.</li>
 * </ul>
 */
public final class ConceptosClienteRelacionadoCalculator {

    /** COCR:37: operations whose TipoDocumento group is not checked. */
    private static final Set<String> SIN_FILTRO_TIPO_DOCUMENTO = Set.of("VENTER", "COMTER", "COMPAC", "VENPAC");
    /** COCR:326. */
    private static final Set<String> DOCUMENTOS_NEGATIVOS = Set.of("NCE", "NCR", "NDR", "NDE", "NEE", "NEO");
    /** COCR:270: spot cancellations, whose amount depends on CompraVenta. */
    private static final Set<String> ANULACIONES_SPOT = Set.of("ANVSPT", "ANCSPT");
    /** COCR:261: exact key of the amounts group (a HashMap lookup in the legacy, so case-sensitive). */
    private static final String GRUPO_TIPO_DATO = "TipoDato";
    private static final String TOTAL = "Total";

    /** COCR:368-411: TipoDato key, whether it applies to the system currency, and the comprobante amount it reads. */
    private enum TipoMonto {
        MONTO_COBRO(true, Comprobante::montoCobro),
        MONTO_COMISION(true, Comprobante::totalComision),
        MONTO_DERECHO(true, Comprobante::totalDerecho),
        MONTO_GASTOS(true, Comprobante::totalGasto),
        MONTO_IVA(true, Comprobante::totalImpto),
        MONTO_OPERADO(true, Comprobante::montoOperado),
        MONTO_TOTAL(true, Comprobante::montoTotal),
        VALOR_COBRO_MX(false, Comprobante::montoCobro),
        MONTO_COMISION_MX(false, Comprobante::totalComision),
        VALOR_DERECHO_MX(false, Comprobante::totalDerecho),
        VALOR_GASTOS_MX(false, Comprobante::totalGasto),
        VALOR_IVA_MX(false, Comprobante::totalImpto),
        VALOR_OPERADO_MX(false, Comprobante::montoOperado),
        VALOR_TOTAL_MX(false, Comprobante::montoTotal);

        final boolean monedaSistema;
        final Function<Comprobante, BigDecimal> monto;

        TipoMonto(boolean monedaSistema, Function<Comprobante, BigDecimal> monto) {
            this.monedaSistema = monedaSistema;
            this.monto = monto;
        }

        static TipoMonto de(String clave) {
            if (clave == null) return null;
            try {
                return valueOf(clave.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException desconocida) {
                return null;
            }
        }
    }

    private ConceptosClienteRelacionadoCalculator() {}

    public static ResultadoConsultaClienteRelacionado calcular(String monedaSistema, List<ConceptoNormativoConfig> conceptos,
                                                               FuentesOperaciones fuentes) {
        List<OperacionClienteRelacionado> base = concatenar(fuentes.comprobantes(), fuentes.prestamos(),
                fuentes.pactos(), fuentes.spots(), fuentes.forwards(), fuentes.terminosCartera());

        // COCR:186, 289, 324: one entry per IdConceptoNormativo; a repeated id keeps the last one. The legacy HashMap
        // happened to iterate small ids in ascending order; the modern contract is ascending id (CONT-D-23).
        Map<Long, TotalConcepto> porId = new TreeMap<>(Comparator.nullsFirst(Comparator.naturalOrder()));
        for (ConceptoNormativoConfig concepto : conceptos == null ? List.<ConceptoNormativoConfig>of() : conceptos) {
            porId.put(concepto.idConceptoNormativo(), acumular(concepto, base, monedaSistema));
        }
        List<TotalConcepto> totales = List.copyOf(porId.values());

        List<OperacionClienteRelacionado> operaciones = concatenar(fuentes.comprobantes(), fuentes.spots(),
                fuentes.forwards(), fuentes.pactos(), fuentes.terminos())
                .stream().map(ConceptosClienteRelacionadoCalculator::conSigno).toList();

        return new ResultadoConsultaClienteRelacionado(totales, totalizar(totales), operaciones);
    }

    // ------------------------------------------------------------------ concept totals

    private static TotalConcepto acumular(ConceptoNormativoConfig concepto, List<OperacionClienteRelacionado> base,
                                          String monedaSistema) {
        List<CondicionConcepto> condiciones = concepto.condiciones();
        if (condiciones == null || condiciones.isEmpty()) return TotalConcepto.enCero(concepto.conceptoNormativo());

        // COCR:209-219: values per group name (exact key); a repeated value keeps the last operator.
        Map<String, Map<String, String>> valoresPorGrupo = new LinkedHashMap<>();
        for (CondicionConcepto grupo : condiciones) {
            Map<String, String> valores = valoresPorGrupo.computeIfAbsent(grupo.idTipoDatoEvento(), k -> new LinkedHashMap<>());
            for (ValorCondicion v : grupo.valores()) valores.put(v.valorVarchar(), v.operadorFuncion());
        }
        List<TipoMonto> tiposMonto = valoresPorGrupo.getOrDefault(GRUPO_TIPO_DATO, Map.of()).keySet().stream()
                .map(TipoMonto::de).filter(t -> t != null).toList();

        BigDecimal cantidad = BigDecimal.ZERO;
        BigDecimal monto = BigDecimal.ZERO;
        Set<Long> folios = new HashSet<>();
        for (OperacionClienteRelacionado op : base) {
            if (op.folioOperacion() != null && folios.contains(op.folioOperacion())) continue;
            Coincidencia c = evaluar(op, valoresPorGrupo);
            if (!c.cumple()) continue;

            BigDecimal valor = montoDe(op, tiposMonto, monedaSistema);
            cantidad = cantidad.add(BigDecimal.ONE);
            monto = monto.add(c.restar() ? valor.negate() : valor);
            if (op.folioOperacion() != null) folios.add(op.folioOperacion());
        }
        return new TotalConcepto(concepto.conceptoNormativo(), cantidad, monto, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO);
    }

    private record Coincidencia(boolean cumple, boolean restar) {}

    /** COCR:227-255. */
    private static Coincidencia evaluar(OperacionClienteRelacionado op, Map<String, Map<String, String>> valoresPorGrupo) {
        boolean restar = false;
        for (Map.Entry<String, Map<String, String>> grupo : valoresPorGrupo.entrySet()) {
            String nombre = grupo.getKey() == null ? "" : grupo.getKey().toUpperCase(Locale.ROOT);
            if (nombre.equals("TIPODATO")) continue;
            if (nombre.equals("TIPODOCUMENTO") && op.idTipoOperacion() != null
                    && SIN_FILTRO_TIPO_DOCUMENTO.contains(op.idTipoOperacion())) continue;

            String valorOperacion = switch (nombre) {
                case "TIPOINSTRUMENTO" -> op.idTipoInstrumento();
                case "TIPODOCUMENTO" -> op.tipoDocumento();
                case "COMPRAVENTA" -> op.compraVenta();
                default -> null;
            };
            if (valorOperacion == null || !grupo.getValue().containsKey(valorOperacion)) {
                return new Coincidencia(false, false);
            }
            if ("-".equals(grupo.getValue().get(valorOperacion))) restar = true;
        }
        return new Coincidencia(true, restar);
    }

    /** COCR:258-277. */
    private static BigDecimal montoDe(OperacionClienteRelacionado op, List<TipoMonto> tiposMonto, String monedaSistema) {
        return switch (op) {
            case Comprobante c -> montoComprobante(c, tiposMonto, monedaSistema);
            case PactoTermino p -> cero(p.montoInicial());
            case OperacionDolar d -> {
                boolean compra = "C".equalsIgnoreCase(d.compraVenta());
                boolean anulacion = d.idTipoOperacion() != null && ANULACIONES_SPOT.contains(d.idTipoOperacion());
                BigDecimal mc = !anulacion || compra ? cero(d.montoCompra()) : BigDecimal.ZERO;
                BigDecimal mv = !anulacion || "V".equalsIgnoreCase(d.compraVenta()) ? cero(d.montoVenta()) : BigDecimal.ZERO;
                yield compra ? mc : mv;
            }
        };
    }

    /** COCR:362-418: sum of the selected amounts; zero falls back to MontoOperado. */
    private static BigDecimal montoComprobante(Comprobante c, List<TipoMonto> tiposMonto, String monedaSistema) {
        boolean mismaMoneda = monedaSistema != null && monedaSistema.equalsIgnoreCase(c.idMoneda());
        BigDecimal total = BigDecimal.ZERO;
        for (TipoMonto tipo : tiposMonto) {
            if (tipo.monedaSistema == mismaMoneda) total = total.add(cero(tipo.monto.apply(c)));
        }
        return total.signum() == 0 ? cero(c.montoOperado()) : total;
    }

    /** COCR:292-322. */
    private static TotalConcepto totalizar(List<TotalConcepto> totales) {
        BigDecimal cantidad = BigDecimal.ZERO, monto = BigDecimal.ZERO, resultado = BigDecimal.ZERO,
                activos = BigDecimal.ZERO, pasivos = BigDecimal.ZERO;
        for (TotalConcepto t : totales) {
            cantidad = cantidad.add(cero(t.cantidad()));
            monto = monto.add(cero(t.monto()));
            resultado = resultado.add(cero(t.resultado()));
            activos = activos.add(cero(t.activos()));
            pasivos = pasivos.add(cero(t.pasivos()));
        }
        return new TotalConcepto(TOTAL, cantidad, monto, resultado, activos, pasivos);
    }

    // ------------------------------------------------------------------ listed operations

    /** COCR:326-350. */
    private static OperacionClienteRelacionado conSigno(OperacionClienteRelacionado op) {
        if (op.tipoDocumento() == null || !DOCUMENTOS_NEGATIVOS.contains(op.tipoDocumento())) return op;
        return switch (op) {
            case Comprobante c -> c.negado();
            case OperacionDolar d -> d.negado();
            case PactoTermino p -> p;
        };
    }

    @SafeVarargs
    private static List<OperacionClienteRelacionado> concatenar(List<? extends OperacionClienteRelacionado>... fuentes) {
        List<OperacionClienteRelacionado> todas = new ArrayList<>();
        Stream.of(fuentes).forEach(todas::addAll);
        return todas;
    }

    private static BigDecimal cero(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
