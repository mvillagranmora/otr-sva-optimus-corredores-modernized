package cl.bcs.tesoreria.javacall;

import static cl.bcs.tesoreria.javacall.TesoreriaJdbcHelper.loadSql;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import cl.bcs.tesoreria.javacall.comun.Consultas;
import cl.bcs.tesoreria.javacall.comun.MensajeSalida;
import cl.bcs.utils.BcsPojo;

/**
 * Legacy {@code cl.bcs.tesoreria.ModificarComprobanteTesoreria}, behaviours TES-A-420..: field-by-field edit of ingresos/egresos, comprobantes and
 * their detail and payment forms. Each group of edits ({@code Id}, {@code Campo}, {@code Valor}) is staged in a session temporary table that the
 * group's UPDATE joins; the tables are dropped first if a previous run left them.
 */
@Service
public class ModificarComprobanteTesoreriaService {

    private static final Logger log = LoggerFactory.getLogger(ModificarComprobanteTesoreriaService.class);
    /** The legacy call used a plain Gson. */
    private static final Gson GSON = new Gson();

    private static final String SQL_EXISTE_TABLA = loadSql("sql/ModificarComprobanteTesoreria_ExisteTablaTemporal.sql");
    private static final String PLANTILLA_CREAR = loadSql("sql/ModificarComprobanteTesoreria_CreateTablaTemporal.sql");
    private static final String PLANTILLA_INSERTAR = loadSql("sql/ModificarComprobanteTesoreria_InsertTablaTemporal.sql");
    private static final String PLANTILLA_BORRAR = loadSql("sql/ModificarComprobanteTesoreria_DropTablaTemporal.sql");

    // ------------------------------------------------------------------ JavaCall contract (legacy names)

    public static class EntradaModificarComprobanteTesoreria extends BcsPojo {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public List<IngresoEgreso> p_IngresoEgreso;
        public List<IngresoEgresoFormaDePago> p_IngresoEgresoFormaDePago;
        public List<Comprobante> p_Comprobante;
        public List<ComprobanteDetalles> p_ComprobanteDetalle;
        public List<ComprobanteFormaDePago> p_ComprobanteFormaDePago;
    }

    public static class SalidaModificarComprobanteTesoreria extends BcsPojo {
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR;
        public Long P_OUT_ADVERTENCIASCONTADOR;
    }

    public static class IngresoEgreso extends BcsPojo {
        public Long IdIngresoEgreso;
        public String Campo;
        public String Valor;
    }

    public static class IngresoEgresoFormaDePago extends BcsPojo {
        public Long IdIngresoEgresoFormaDePago;
        public String Campo;
        public String Valor;
    }

    public static class Comprobante extends BcsPojo {
        public Long IdComprobante;
        public String Campo;
        public String Valor;
    }

    public static class ComprobanteDetalles extends BcsPojo {
        public Long IdComprobanteDetalle;
        public String Campo;
        public String Valor;
    }

    public static class ComprobanteFormaDePago extends BcsPojo {
        public Long IdComprobanteFormaDePago;
        public String Campo;
        public String Valor;
    }

    /** One group of edits: its staging table, the id column, the UPDATE that applies it and how to read a row of the input. */
    private record Grupo<T>(String tabla, String columnaId, String sqlActualizar, Function<T, Map<String, Object>> fila) {

        static <T> Grupo<T> de(String tabla, String columnaId, String recursoActualizar, Function<T, Long> id, Function<T, String> campo,
                Function<T, String> valor) {
            return new Grupo<>(tabla, columnaId, loadSql("sql/" + recursoActualizar),
                    t -> {
                        Map<String, Object> f = new java.util.HashMap<>();
                        f.put("Id", id.apply(t));
                        f.put("Campo", campo.apply(t));
                        f.put("Valor", valor.apply(t));
                        return f;
                    });
        }
    }

    private static final Grupo<IngresoEgreso> INGRESO_EGRESO = Grupo.de("#TES_MOD_COMPROBANTE_INGRESOEGRESO", "IdIngresoEgreso",
            "ModificarComprobanteTesoreria_UpdateIngresoEgreso.sql", i -> i.IdIngresoEgreso, i -> i.Campo, i -> i.Valor);
    private static final Grupo<IngresoEgresoFormaDePago> INGRESO_EGRESO_FORMA_DE_PAGO = Grupo.de("#TES_MOD_COMPROBANTE_INGRESOEGRESOFORMADEPAGO",
            "IdIngresoEgresoFormaDePago", "ModificarComprobanteTesoreria_UpdateIngresoEgresoFormaDePago.sql", i -> i.IdIngresoEgresoFormaDePago,
            i -> i.Campo, i -> i.Valor);
    private static final Grupo<Comprobante> COMPROBANTE = Grupo.de("#TES_MOD_COMPROBANTE_COMPROBANTE", "IdComprobante",
            "ModificarComprobanteTesoreria_UpdateComprobante.sql", c -> c.IdComprobante, c -> c.Campo, c -> c.Valor);
    private static final Grupo<ComprobanteDetalles> COMPROBANTE_DETALLE = Grupo.de("#TES_MOD_COMPROBANTE_COMPROBANTEDETALLE", "IdComprobanteDetalle",
            "ModificarComprobanteTesoreria_UpdateComprobanteDetalle.sql", c -> c.IdComprobanteDetalle, c -> c.Campo, c -> c.Valor);
    private static final Grupo<ComprobanteFormaDePago> COMPROBANTE_FORMA_DE_PAGO = Grupo.de("#TES_MOD_COMPROBANTE_COMPROBANTEFORMADEPAGO",
            "IdComprobanteFormaDePago", "ModificarComprobanteTesoreria_UpdateComprobanteFormaDePago.sql", c -> c.IdComprobanteFormaDePago,
            c -> c.Campo, c -> c.Valor);

    private final NamedParameterJdbcTemplate jdbc;

    public ModificarComprobanteTesoreriaService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Legacy {@code call}: the error counter is recounted from the messages. */
    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject entrada) throws Exception {
        EntradaModificarComprobanteTesoreria in = GSON.fromJson(entrada, EntradaModificarComprobanteTesoreria.class);
        SalidaModificarComprobanteTesoreria out = logica(in);
        out.P_OUT_ERRORESCONTADOR = MensajeSalida.numeroErrores(out.P_OUT_MENSAJES);
        return GSON.toJsonTree(out).getAsJsonObject();
    }

    /** Legacy {@code logica}. A missing list is a NullPointerException, as in the legacy. */
    @Transactional(rollbackFor = Exception.class)
    public SalidaModificarComprobanteTesoreria logica(EntradaModificarComprobanteTesoreria entrada) throws Exception {
        List<Grupo<?>> grupos = List.of(INGRESO_EGRESO, INGRESO_EGRESO_FORMA_DE_PAGO, COMPROBANTE, COMPROBANTE_DETALLE, COMPROBANTE_FORMA_DE_PAGO);
        for (Grupo<?> g : grupos) {
            soltarTablaPrevia(g);
        }
        aplicar(INGRESO_EGRESO, entrada.p_IngresoEgreso);
        aplicar(INGRESO_EGRESO_FORMA_DE_PAGO, entrada.p_IngresoEgresoFormaDePago);
        aplicar(COMPROBANTE, entrada.p_Comprobante);
        aplicar(COMPROBANTE_DETALLE, entrada.p_ComprobanteDetalle);
        aplicar(COMPROBANTE_FORMA_DE_PAGO, entrada.p_ComprobanteFormaDePago);
        return new SalidaModificarComprobanteTesoreria();
    }

    /** Drops the staging table when a previous run of the session left it; a failure to drop is only logged. */
    private void soltarTablaPrevia(Grupo<?> g) {
        if (!jdbc.queryForList(SQL_EXISTE_TABLA, Map.of("Tabla", g.tabla())).isEmpty()) {
            try {
                jdbc.getJdbcOperations().execute(plantilla(PLANTILLA_BORRAR, g));
            } catch (RuntimeException e) {
                log.warn("ModificarComprobanteTesoreria: cannot drop {}", g.tabla(), e);
            }
        }
    }

    private <T> void aplicar(Grupo<T> g, List<T> ediciones) {
        if (ediciones.isEmpty()) {
            return;
        }
        jdbc.getJdbcOperations().execute(plantilla(PLANTILLA_CREAR, g));
        Consultas.lote(jdbc, plantilla(PLANTILLA_INSERTAR, g), ediciones.stream().map(g.fila()).toList());
        // the legacy ran the UPDATE once per edit row (a batch without parameters); it is idempotent, so it runs once here
        jdbc.getJdbcOperations().update(g.sqlActualizar());
    }

    private static String plantilla(String plantilla, Grupo<?> g) {
        return plantilla.replace("${TABLA}", g.tabla()).replace("${ID}", g.columnaId());
    }
}
