package cl.bcs.contabilidad.service;

import cl.bcs.contabilidad.domain.model.clienterelacionado.Comprobante;
import cl.bcs.contabilidad.domain.model.clienterelacionado.ConceptoNormativoConfig;
import cl.bcs.contabilidad.domain.model.clienterelacionado.FuentesOperaciones;
import cl.bcs.contabilidad.domain.model.clienterelacionado.OperacionClienteRelacionado;
import cl.bcs.contabilidad.domain.model.clienterelacionado.OperacionDolar;
import cl.bcs.contabilidad.domain.model.clienterelacionado.PactoTermino;
import cl.bcs.contabilidad.domain.model.clienterelacionado.ResultadoConsultaClienteRelacionado;
import cl.bcs.contabilidad.domain.model.clienterelacionado.TotalConcepto;
import cl.bcs.contabilidad.domain.port.out.ClienteRelacionadoRepository;
import cl.bcs.contabilidad.domain.port.out.PlataformaPort;
import cl.bcs.contabilidad.domain.service.clienterelacionado.ConceptosClienteRelacionadoCalculator;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Related-client operations query: legacy {@code cl.bcs.contabilidad.ConsultarOperacionesClienteRelacionado}
 * (ConsultarOperacionesClienteRelacionado.java:119-356), rules CONT-D-01..07. The totals are computed by
 * {@link ConceptosClienteRelacionadoCalculator}.
 *
 * <p>Order of checks, as in the legacy: system currency (PLA.MonedaSistema), date format, a range of at most three
 * months ({@code FECHA_FUERA_RANGO}), loading the normative concepts, start not after end, then the seven queries.
 * Every failure gives P_OUT_ERRORESCONTADOR 1, with the message or the exception text in P_OUT_MENSAJE and empty
 * {@code conceptos}/{@code operaciones}. The JSON keeps the legacy field names; nulls are omitted (plain Gson).
 */
@Service
public class ConsultarOperacionesClienteRelacionadoService {

    static final String FECHA_FUERA_RANGO = "FECHA_FUERA_RANGO";
    static final String INICIO_POSTERIOR_A_FIN = "La fecha de inicio no puede ser posterior a la fecha de fin.";
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final ClienteRelacionadoRepository repository;
    private final PlataformaPort plataforma;

    public ConsultarOperacionesClienteRelacionadoService(ClienteRelacionadoRepository repository,
                                                         PlataformaPort plataforma) {
        this.repository = repository;
        this.plataforma = plataforma;
    }

    /** Result of {@link #consultar}: {@code resultado} is null when {@code errores > 0}. */
    public record Consulta(long errores, String mensaje, ResultadoConsultaClienteRelacionado resultado) {

        static Consulta error(String mensaje) {
            return new Consulta(1L, mensaje, null);
        }
    }

    public JsonObject execute(JsonObject params) {
        Long idEmpresa = texto(params, "p_IdEmpresa") == null ? null : params.get("p_IdEmpresa").getAsLong();
        return aJson(consultar(idEmpresa, texto(params, "p_FechaInicio"), texto(params, "p_FechaFin")));
    }

    public Consulta consultar(Long idEmpresa, String fechaInicio, String fechaFin) {
        try {
            String monedaSistema = plataforma.obtenerMonedaSistema(idEmpresa);

            LocalDate inicio = LocalDate.parse(fechaInicio, FORMATO_FECHA);
            LocalDate fin = LocalDate.parse(fechaFin, FORMATO_FECHA);
            if (fueraDeRango(inicio, fin)) return Consulta.error(FECHA_FUERA_RANGO);

            List<ConceptoNormativoConfig> conceptos = repository.consultarConceptosNormativos();
            if (inicio.isAfter(fin)) return Consulta.error(INICIO_POSTERIOR_A_FIN);

            FuentesOperaciones fuentes = repository.consultarOperaciones(fechaInicio, fechaFin);
            return new Consulta(0L, null, ConceptosClienteRelacionadoCalculator.calcular(monedaSistema, conceptos, fuentes));
        } catch (RuntimeException e) {
            return Consulta.error(e.getMessage());
        }
    }

    /**
     * COCR:142-145: more than three whole months, or exactly three with a later day of month. Legacy quirk kept: the
     * day comparison lets ranges of almost four months through (2025-01-17..2025-05-16 is P3M29D, allowed).
     */
    private static boolean fueraDeRango(LocalDate inicio, LocalDate fin) {
        Period periodo = Period.between(inicio, fin);
        int meses = periodo.getYears() * 12 + periodo.getMonths();
        return meses > 3 || (meses == 3 && fin.getDayOfMonth() > inicio.getDayOfMonth());
    }

    // ------------------------------------------------------------------ JSON (legacy field names)

    static JsonObject aJson(Consulta c) {
        JsonObject out = new JsonObject();
        prop(out, "P_OUT_MENSAJE", c.mensaje());
        out.addProperty("P_OUT_ERRORESCONTADOR", c.errores());
        JsonArray conceptos = new JsonArray();
        JsonArray operaciones = new JsonArray();
        if (c.resultado() != null) {
            c.resultado().conceptos().forEach(t -> conceptos.add(concepto(t)));
            c.resultado().operaciones().forEach(o -> operaciones.add(operacion(o)));
        }
        out.add("conceptos", conceptos);
        if (c.resultado() != null && c.resultado().totalizado() != null) {
            out.add("totalizado", concepto(c.resultado().totalizado()));
        }
        out.add("operaciones", operaciones);
        return out;
    }

    private static JsonObject concepto(TotalConcepto t) {
        JsonObject total = new JsonObject();
        prop(total, "Cantidad", t.cantidad());
        prop(total, "Monto", t.monto());
        prop(total, "Resultado", t.resultado());
        JsonObject saldo = new JsonObject();
        prop(saldo, "Activos", t.activos());
        prop(saldo, "Pasivos", t.pasivos());
        JsonObject o = new JsonObject();
        prop(o, "Concepto", t.concepto());
        o.add("totalTransaccion", total);
        o.add("saldo", saldo);
        return o;
    }

    private static JsonElement operacion(OperacionClienteRelacionado op) {
        JsonObject o = new JsonObject();
        switch (op) {
            case Comprobante c -> {
                prop(o, "IdMoneda", c.idMoneda());
                prop(o, "MontoOperado", c.montoOperado());
                prop(o, "MontoTotal", c.montoTotal());
                prop(o, "MontoCobro", c.montoCobro());
                prop(o, "TotalComision", c.totalComision());
                prop(o, "TotalGasto", c.totalGasto());
                prop(o, "TotalImpto", c.totalImpto());
                prop(o, "TotalRetencion", c.totalRetencion());
                prop(o, "TotalDerecho", c.totalDerecho());
            }
            case OperacionDolar d -> {
                prop(o, "MontoCompra", d.montoCompra());
                prop(o, "MontoVenta", d.montoVenta());
            }
            case PactoTermino p -> {
                prop(o, "FechaInicio", p.fechaInicio());
                prop(o, "FechaTermino", p.fechaTermino());
                prop(o, "MontoFinal", p.montoFinal());
                prop(o, "MontoInicial", p.montoInicial());
                prop(o, "Perdida", p.perdida());
                prop(o, "Ganancia", p.ganancia());
            }
        }
        // Gson wrote the subclass fields first, then the base class ones (OperacionesClienteRelacionadoBase)
        if (op.folioOperacion() != null) o.addProperty("FolioOperacion", op.folioOperacion());
        prop(o, "FechaOperacion", op.fechaOperacion());
        prop(o, "Nombre", op.nombre());
        prop(o, "Portafolio", op.portafolio());
        prop(o, "DocumentoIdentidad", op.documentoIdentidad());
        prop(o, "IdTipoInstrumento", op.idTipoInstrumento());
        prop(o, "TipoDocumento", op.tipoDocumento());
        prop(o, "CompraVenta", op.compraVenta());
        prop(o, "IdTipoOperacion", op.idTipoOperacion());
        prop(o, "IdClasificacionTipoInstrumento", op.idClasificacionTipoInstrumento());
        return o;
    }

    private static void prop(JsonObject o, String nombre, String valor) {
        if (valor != null) o.addProperty(nombre, valor);
    }

    private static void prop(JsonObject o, String nombre, BigDecimal valor) {
        if (valor != null) o.addProperty(nombre, valor);
    }

    private static String texto(JsonObject params, String nombre) {
        JsonElement e = params.get(nombre);
        return e == null || e.isJsonNull() ? null : e.getAsString();
    }
}
