package cl.bcs.contabilidad.centralizagz;

import cl.bcs.contabilidad.domain.model.ResultadoCentralizacion;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabDatoMovimientoRef;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabEventoRef;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabMovimientoRef;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cl.bcs.contabilidad.centralizagz.Fx.*;

/**
 * Deterministic fixture rows (shaped like the legacy SQL result columns, i.e. the
 * legacy POJO field names) and the expected output derived from the legacy rules.
 */
final class Filas {

    // ------------------------------------------------------------------ movements / events

    /** Row shaped like pojo/TabMovimientos.java (the SQL aliases of every *_SelectMovimientos.sql). */
    static Map<String, Object> movimiento(long idMovimiento, Long ejercicio, String idMoneda, String monto) {
        Map<String, Object> r = new HashMap<>();
        r.put("IdTipoCentralizacion", ID_TIPO);
        r.put("IdMovimiento", idMovimiento);
        r.put("IdTipoMovimiento", "TM-" + idMovimiento);
        r.put("IdEmpresa", ID_EMPRESA);
        r.put("Numero", 300L + idMovimiento % 100);
        r.put("Linea", 1L);
        r.put("Fecha", FECHA_CONTABLE);
        r.put("DocumentoIdentidad", "11111111-1");
        r.put("IdPortafolio", 4000L + idMovimiento % 100);
        r.put("Ejercicio", ejercicio);
        r.put("Periodo", "03");
        r.put("Monto", new BigDecimal(monto));
        r.put("IdMoneda", idMoneda);
        r.put("Estado", "VIG");
        return r;
    }

    static TabMovimientoRef movimientoRef(Map<String, Object> r) {
        return new TabMovimientoRef(
                (Long) r.get("IdTipoCentralizacion"), (Long) r.get("IdMovimiento"),
                (String) r.get("IdTipoMovimiento"), (Long) r.get("IdEmpresa"),
                (Long) r.get("Numero"), (Long) r.get("Linea"), (String) r.get("Fecha"),
                (String) r.get("DocumentoIdentidad"), (Long) r.get("IdPortafolio"),
                (Long) r.get("Ejercicio"), (String) r.get("Periodo"), (BigDecimal) r.get("Monto"),
                (String) r.get("IdMoneda"), (String) r.get("Estado"));
    }

    /** BcsCentralizacion.llenarResultado (BcsCentralizacion.java:100-111): copy + Ejercicio = String.valueOf(Long). */
    static ResultadoCentralizacion resultado(Map<String, Object> r) {
        return new ResultadoCentralizacion(
                (Long) r.get("IdTipoCentralizacion"), (Long) r.get("IdMovimiento"),
                (String) r.get("IdTipoMovimiento"), (Long) r.get("IdEmpresa"),
                (Long) r.get("Numero"), (Long) r.get("Linea"), (String) r.get("Fecha"),
                (String) r.get("DocumentoIdentidad"), (Long) r.get("IdPortafolio"),
                String.valueOf(r.get("Ejercicio")), (String) r.get("Periodo"),
                (BigDecimal) r.get("Monto"), (String) r.get("IdMoneda"), (String) r.get("Estado"));
    }

    /** Row shaped like pojo/TabEventos.java. */
    static Map<String, Object> evento(long idMovimiento, String idEvento, String monto, String moneda) {
        Map<String, Object> r = new HashMap<>();
        r.put("IdTipoCentralizacion", ID_TIPO);
        r.put("IdMovimiento", idMovimiento);
        r.put("IdEvento", idEvento);
        r.put("Nombre", "Nombre " + idEvento);
        r.put("MontoEvento", new BigDecimal(monto));
        r.put("CodigoMoneda", moneda);
        return r;
    }

    static TabEventoRef eventoRef(Map<String, Object> r) {
        return new TabEventoRef((Long) r.get("IdTipoCentralizacion"), (Long) r.get("IdMovimiento"),
                (String) r.get("IdEvento"), (String) r.get("Nombre"),
                (BigDecimal) r.get("MontoEvento"), (String) r.get("CodigoMoneda"));
    }

    // ------------------------------------------------------------------ datos movimiento

    /**
     * Value for column {@code colIdx} (index of the first Campo that reads that column) in row {@code fila}.
     * Strings: "Columna-<fila+1>"; Longs: 90000 + fila*1000 + colIdx; Dates: 2025-02-01 + fila*30 + colIdx days.
     */
    static Object valor(Spec.Campo c, int fila, int colIdx) {
        return switch (c.formato()) {
            case STR, VALUEOF_STR, TOSTRING_STR, CONST -> c.columna() + "-" + (fila + 1);
            case VALUEOF_LONG -> 90000L + fila * 1000L + colIdx;
            case DATE, TOSTRING_DATE -> java.sql.Date.valueOf(LocalDate.of(2025, 2, 1).plusDays(fila * 30L + colIdx));
            case DECIMAL -> new BigDecimal("1000.1234").add(BigDecimal.valueOf(fila * 100L + colIdx));
            case TS_O_DEFECTO, DATETIME -> Timestamp.valueOf(LocalDateTime.of(2025, 3, 10, 14, 5, 9, 123_000_000).plusHours(fila));
        };
    }

    /**
     * Two data rows for a datos query. {@code nulos=true} leaves every column null except the ones whose
     * legacy formatting throws on null (those keep a value so the run completes).
     */
    static List<Map<String, Object>> datos(Spec.Tipo tipo, int idxConsulta, boolean nulos) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (int fila = 0; fila < 2; fila++) {
            Map<String, Object> r = new HashMap<>();
            r.put("IdMovimiento", 7001L + idxConsulta * 10L + fila);
            List<Spec.Campo> campos = tipo.campos();
            for (int i = 0; i < campos.size(); i++) {
                Spec.Campo c = campos.get(i);
                if (c.columna() == null || c.columna().equals("IdMovimiento") || r.containsKey(c.columna())) {
                    continue;
                }
                r.put(c.columna(), nulos && !c.formato().lanzaConNulo() ? null : valor(c, fila, i));
            }
            out.add(r);
        }
        return out;
    }

    static List<TabDatoMovimientoRef> esperado(Spec.Tipo tipo, List<Map<String, Object>> rows) {
        List<TabDatoMovimientoRef> out = new ArrayList<>();
        for (var r : rows) {
            Long idMov = (Long) r.get("IdMovimiento");
            for (Spec.Campo c : tipo.campos()) {
                Object v = c.columna() == null ? null : r.get(c.columna());
                out.add(new TabDatoMovimientoRef(ID_TIPO, idMov, c.tipoDato(), varchar(c, v), decimal(c, v)));
            }
        }
        return out;
    }

    static String varchar(Spec.Campo c, Object v) {
        return switch (c.formato()) {
            case STR -> v == null ? "" : (String) v;
            case VALUEOF_LONG, VALUEOF_STR -> String.valueOf(v);
            case DATE -> new SimpleDateFormat("yyyy-MM-dd").format((java.util.Date) v);
            case TOSTRING_DATE, TOSTRING_STR -> v.toString();
            case CONST, DECIMAL -> "";
            case TS_O_DEFECTO -> v == null ? "0001-01-01 00:00:00"
                    : ((Timestamp) v).toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
            case DATETIME -> ((Timestamp) v).toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        };
    }

    static BigDecimal decimal(Spec.Campo c, Object v) {
        if (c.formato() == Spec.Formato.DECIMAL && v != null) {
            return (BigDecimal) v;
        }
        return BigDecimal.ZERO;
    }

    private Filas() {}
}
