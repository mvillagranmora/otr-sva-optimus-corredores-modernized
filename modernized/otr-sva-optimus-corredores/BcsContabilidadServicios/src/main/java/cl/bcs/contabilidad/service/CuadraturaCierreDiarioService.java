package cl.bcs.contabilidad.service;

import cl.bcs.contabilidad.domain.model.MensajeSalida;
import cl.bcs.contabilidad.infrastructure.persistence.SqlParams;
import cl.bcs.contabilidad.infrastructure.persistence.SqlResources;
import cl.bcs.contabilidad.infrastructure.persistence.SqlRow;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Daily closing reconciliation (cuadratura) of accounting operations.
 *
 * <p>Legacy {@code cl.bcs.contabilidad.CuadraturaCierreDiario}. Implements CONT-G-120..129.
 * <ul>
 *   <li>{@code p_TipoLlamado = Proceso}: recomputes each requested summary row. {@code CUACOMCON} counts the vouchers
 *       of the day still in Estado V ("No Contabilizado"); a centralization row compares the current movements with a
 *       simulated re-centralization ({@link CentralizaMultiSeleccionService}, {@code p_SimulaOperacion = S}). The rows
 *       are deleted by id and inserted again.</li>
 *   <li>{@code Consulta}: returns the stored JSON detail of the first row.</li>
 *   <li>any other value: nothing; a missing value is an error (legacy NPE, rethrown).</li>
 * </ul>
 * Database failures propagate. Not ported: the AgregarLog audit entry (legacy L316, L419-465, platform module).
 */
@Service
public class CuadraturaCierreDiarioService {

    private static final String CUADRATURA_COMPROBANTES = "CUACOMCON";

    private final String SQL_SELECT_CUADRATURA = SqlResources.load("CuadraturaCierreDiario_SelectCuadraturaDiariaOperaciones.sql");
    private final String SQL_SELECT_MOVIMIENTOS = SqlResources.load("CuadraturaCierreDiario_SelectContabilidad.sql");
    private final String SQL_SELECT_COMPROBANTES = SqlResources.load("CuadraturaCierreDiario_SelectComprobantes.sql");
    private final String SQL_DELETE_CUADRATURA = SqlResources.load("CuadraturaCierreDiario_DeleteCuadraturaDiariaOperaciones.sql");
    private final String SQL_INSERT_CUADRATURA = SqlResources.load("CuadraturaCierreDiario_InsertCuadraturaDiariaOperaciones.sql");
    private final String SQL_SELECT_DETALLE = SqlResources.load("CuadraturaCierreDiario_SelectJSONCuadraturaDiariaOperaciones.sql");

    private final NamedParameterJdbcTemplate jdbc;
    private final CentralizaMultiSeleccionService centralizaMultiSeleccion;

    @Autowired
    public CuadraturaCierreDiarioService(NamedParameterJdbcTemplate jdbc, CentralizaMultiSeleccionService centralizaMultiSeleccion) {
        this.jdbc = jdbc;
        this.centralizaMultiSeleccion = centralizaMultiSeleccion;
    }

    public CuadraturaCierreDiarioService(NamedParameterJdbcTemplate jdbc) {
        this(jdbc, new CentralizaMultiSeleccionService(jdbc));
    }

    record Proceso(@SerializedName("IdCuadraturaDiariaOperaciones") String idCuadraturaDiariaOperaciones) {}

    record Entrada(@SerializedName("p_IdUsuario") Long idUsuario,
                   @SerializedName("p_IdEmpresa") Long idEmpresa,
                   @SerializedName("p_IdModulo") String idModulo,
                   @SerializedName("p_FechaInicio") String fechaInicio,
                   @SerializedName("p_IdProceso") List<Proceso> procesos,
                   @SerializedName("p_TipoLlamado") String tipoLlamado) {
        List<String> ids() {
            return (procesos == null ? List.<Proceso>of() : procesos).stream().map(Proceso::idCuadraturaDiariaOperaciones).toList();
        }
    }

    record Salida(@SerializedName("P_OUT_MENSAJES") List<MensajeSalida> mensajes,
                  @SerializedName("P_OUT_ERRORESCONTADOR") Long erroresContador,
                  @SerializedName("P_OUT_DETALLE_CUADRATURA") String detalleCuadratura) {}

    /** A summary row of CuadraturaDiariaOperaciones. */
    private record Resumen(String id, Long idEmpresa, Long idTipoCentralizacion, String operacion, Object fechaEjecucion,
                           Long totalOperaciones, Long operacionesConProblemas, String tipoOperacion, String resultado,
                           Long orden, String detalle) {
        Resumen recalculado(LocalDate fecha, long total, long problemas, String resultado, String detalle) {
            return new Resumen(id, idEmpresa, idTipoCentralizacion, operacion, java.sql.Date.valueOf(fecha), total,
                    problemas, tipoOperacion, resultado, orden, detalle);
        }

        Resumen conFecha(LocalDate fecha) {
            return new Resumen(id, idEmpresa, idTipoCentralizacion, operacion, java.sql.Date.valueOf(fecha),
                    totalOperaciones, operacionesConProblemas, tipoOperacion, resultado, orden, detalle);
        }

        MapSqlParameterSource params(Long idUsuario) {
            return new MapSqlParameterSource()
                    .addValue("IdCuadraturaDiariaOperaciones", id)
                    .addValue("IdEmpresa", idEmpresa)
                    .addValue("IdTipoCentralizacion", idTipoCentralizacion)
                    .addValue("OperacionCuadraturaDiariaOperaciones", operacion)
                    .addValue("FechaEjecucion", fechaEjecucion)
                    .addValue("TotalOperaciones", totalOperaciones)
                    .addValue("OperacionesConProblemas", operacionesConProblemas)
                    .addValue("TipoOperacion", tipoOperacion)
                    .addValue("Resultado", resultado)
                    .addValue("Orden", orden)
                    .addValue("DetalleCuadraturaJSON", detalle)
                    .addValue("IdUsuario", idUsuario);
        }
    }

    /** Voucher row of the JSON detail, legacy {@code CuadraturaCierreDiario.Comprobantes}. */
    record Comprobante(@SerializedName("IdPreComprobante") Long idPreComprobante,
                       @SerializedName("NombreEmpresa") String nombreEmpresa,
                       @SerializedName("Ejercicio") Long ejercicio,
                       @SerializedName("FechaContable") String fechaContable,
                       @SerializedName("FechaProceso") String fechaProceso,
                       @SerializedName("HoraProceso") String horaProceso,
                       @SerializedName("FolioContabilidad") Long folioContabilidad,
                       @SerializedName("CantidadAsientos") Long cantidadAsientos,
                       @SerializedName("Estado") String estado,
                       @SerializedName("NombreEstado") String nombreEstado,
                       @SerializedName("Glosa") String glosa,
                       @SerializedName("IdReversa") Long idReversa,
                       @SerializedName("Tipo") String tipo,
                       @SerializedName("CodigoUsuario") String codigoUsuario,
                       @SerializedName("IdTipoContabilidad") Long idTipoContabilidad,
                       @SerializedName("NombreTipoContabilidad") String nombreTipoContabilidad,
                       @SerializedName("IdEmpresa") Long idEmpresa,
                       @SerializedName("Debe") BigDecimal debe,
                       @SerializedName("Haber") BigDecimal haber,
                       @SerializedName("IdMoneda") String idMoneda,
                       @SerializedName("IdEstadoPrograma") String idEstadoPrograma,
                       @SerializedName("Descripcion") String descripcion) {
        static Comprobante of(SqlRow r) {
            return new Comprobante(r.lng("IdPreComprobante"), r.str("NombreEmpresa"), r.lng("Ejercicio"), r.str("FechaContable"),
                    r.str("FechaProceso"), r.str("HoraProceso"), r.lng("FolioContabilidad"), r.lng("CantidadAsientos"),
                    r.str("Estado"), r.str("NombreEstado"), r.str("Glosa"), r.lng("IdReversa"), r.str("Tipo"),
                    r.str("CodigoUsuario"), r.lng("IdTipoContabilidad"), r.str("NombreTipoContabilidad"), r.lng("IdEmpresa"),
                    r.dec("Debe"), r.dec("Haber"), r.str("IdMoneda"), r.str("IdEstadoPrograma"), r.str("Descripcion"));
        }

        Comprobante conDescripcion(String d) {
            return new Comprobante(idPreComprobante, nombreEmpresa, ejercicio, fechaContable, fechaProceso, horaProceso,
                    folioContabilidad, cantidadAsientos, estado, nombreEstado, glosa, idReversa, tipo, codigoUsuario,
                    idTipoContabilidad, nombreTipoContabilidad, idEmpresa, debe, haber, idMoneda, idEstadoPrograma, d);
        }
    }

    /** Movement row of the JSON detail, legacy {@code CuadraturaCierreDiario.Movimientos}. */
    record Movimiento(@SerializedName("IdTipoCentralizacion") Long idTipoCentralizacion,
                      @SerializedName("IdMovimiento") Long idMovimiento,
                      @SerializedName("IdTipoMovimiento") String idTipoMovimiento,
                      @SerializedName("IdEmpresa") Long idEmpresa,
                      @SerializedName("Numero") Long numero,
                      @SerializedName("Linea") Long linea,
                      @SerializedName("Fecha") String fecha,
                      @SerializedName("DocumentoIdentidad") String documentoIdentidad,
                      @SerializedName("IdPortafolio") Long idPortafolio,
                      @SerializedName("Ejercicio") String ejercicio,
                      @SerializedName("Periodo") String periodo,
                      @SerializedName("Monto") BigDecimal monto,
                      @SerializedName("IdMoneda") String idMoneda,
                      @SerializedName("Estado") String estado,
                      @SerializedName("Descripcion") String descripcion) {
        static Movimiento of(SqlRow r) {
            return new Movimiento(r.lng("IdTipoCentralizacion"), r.lng("IdMovimiento"), r.str("IdTipoMovimiento"),
                    r.lng("IdEmpresa"), r.lng("Numero"), r.lng("Linea"), r.str("Fecha"), r.str("DocumentoIdentidad"),
                    r.lng("IdPortafolio"), r.str("Ejercicio"), r.str("Periodo"), r.dec("Monto"), r.str("IdMoneda"),
                    r.str("Estado"), null);
        }

        static Movimiento of(CentralizaMultiSeleccionService.Resultado r, String descripcion) {
            return new Movimiento(r.idTipoCentralizacion(), r.idMovimiento(), r.idTipoMovimiento(), r.idEmpresa(),
                    r.numero(), r.linea(), r.fecha(), r.documentoIdentidad(), r.idPortafolio(), r.ejercicio(),
                    r.periodo(), r.monto(), r.idMoneda(), r.estado(), descripcion);
        }

        Movimiento conDescripcion(String d) {
            return new Movimiento(idTipoCentralizacion, idMovimiento, idTipoMovimiento, idEmpresa, numero, linea, fecha,
                    documentoIdentidad, idPortafolio, ejercicio, periodo, monto, idMoneda, estado, d);
        }
    }

    public JsonObject execute(JsonObject params) {
        Entrada entrada = JavaCallJson.read(JavaCallJson.legacy(), params, Entrada.class);
        return JavaCallJson.write(JavaCallJson.legacy(), ejecutar(entrada));
    }

    Salida ejecutar(Entrada entrada) {
        if (entrada.tipoLlamado() == null) throw new IllegalArgumentException("p_TipoLlamado is required");
        return switch (entrada.tipoLlamado()) {
            case "Proceso" -> procesar(entrada);
            case "Consulta" -> consultar(entrada);
            default -> new Salida(new ArrayList<>(), 0L, null);
        };
    }

    private Salida consultar(Entrada entrada) {
        List<String> detalles = jdbc.query(SQL_SELECT_DETALLE, porIds(entrada), SqlRow.mapper(r -> r.str("DetalleCuadraturaJSON")));
        return new Salida(new ArrayList<>(), 0L, detalles.isEmpty() ? null : detalles.get(0));
    }

    private Salida procesar(Entrada entrada) {
        List<Resumen> resumenes = jdbc.query(SQL_SELECT_CUADRATURA, porIds(entrada), SqlRow.mapper(r -> new Resumen(
                r.str("IdCuadraturaDiariaOperaciones"), r.lng("IdEmpresa"), r.lng("IdTipoCentralizacion"),
                r.str("OperacionCuadraturaDiariaOperaciones"), r.obj("FechaEjecucion"), r.lng("TotalOperaciones"),
                r.lng("OperacionesConProblemas"), r.str("TipoOperacion"), r.str("Resultado"), r.lng("Orden"),
                r.str("DetalleCuadraturaJSON"))));

        List<Long> centralizaciones = resumenes.stream().map(Resumen::idTipoCentralizacion)
                .filter(id -> id != null && id != 0L).toList();
        List<Movimiento> movimientos = List.of();
        List<CentralizaMultiSeleccionService.Resultado> simulados = List.of();
        if (!centralizaciones.isEmpty()) {
            movimientos = jdbc.query(SQL_SELECT_MOVIMIENTOS, new MapSqlParameterSource()
                    .addValue("IdEmpresa", entrada.idEmpresa())
                    .addValue("FechaCentralizacion", entrada.fechaInicio())
                    .addValue("IdCentralizaciones", centralizaciones), SqlRow.mapper(Movimiento::of));
            simulados = simular(entrada, centralizaciones);
        }

        LocalDate fecha = LocalDate.parse(entrada.fechaInicio());
        List<Resumen> recalculados = new ArrayList<>();
        for (Resumen r : resumenes) {
            if (r.idTipoCentralizacion() == null || r.idTipoCentralizacion() == 0L) {
                recalculados.add(CUADRATURA_COMPROBANTES.equals(r.id()) ? cuadraComprobantes(entrada, r, fecha) : r.conFecha(fecha));
            } else {
                recalculados.add(cuadraCentralizacion(r, movimientos, simulados, fecha));
            }
        }
        if (!recalculados.isEmpty()) {
            jdbc.batchUpdate(SQL_DELETE_CUADRATURA, SqlParams.batch(recalculados,
                    r -> new MapSqlParameterSource("IdCuadraturaDiariaOperaciones", r.id())));
            jdbc.batchUpdate(SQL_INSERT_CUADRATURA, SqlParams.batch(recalculados, r -> r.params(entrada.idUsuario())));
        }
        List<MensajeSalida> mensajes = new ArrayList<>();
        mensajes.add(MensajeSalida.of(0L, MensajeSalida.OK, "UI_PROCESO_EJECUTADO_EXITO"));
        return new Salida(mensajes, MensajeSalida.contarErrores(mensajes), null);
    }

    /** Simulated re-centralization of the types (legacy L231-243). */
    private List<CentralizaMultiSeleccionService.Resultado> simular(Entrada entrada, List<Long> centralizaciones) {
        // Legacy passes the day of p_FechaInicio as p_Ejercicio (split("-")[2]); kept as is.
        Long ejercicio = Long.valueOf(entrada.fechaInicio().split("-")[2]);
        CentralizaMultiSeleccionService.Entrada simulacion = new CentralizaMultiSeleccionService.Entrada(entrada.idEmpresa(),
                entrada.idUsuario(), entrada.idModulo(), entrada.fechaInicio(), null, ejercicio, "S",
                centralizaciones.stream().map(CentralizaMultiSeleccionService.TipoSeleccionado::new).toList());
        return centralizaMultiSeleccion.centralizar(simulacion).resultado();
    }

    private Resumen cuadraComprobantes(Entrada entrada, Resumen r, LocalDate fecha) {
        List<Comprobante> comprobantes = jdbc.query(SQL_SELECT_COMPROBANTES, new MapSqlParameterSource()
                .addValue("FechaCentralizacion", entrada.fechaInicio())
                .addValue("IdEmpresa", entrada.idEmpresa()), SqlRow.mapper(Comprobante::of));
        List<Comprobante> descuadres = comprobantes.stream().filter(c -> "V".equalsIgnoreCase(c.estado()))
                .map(c -> c.conDescripcion("No Contabilizado")).toList();
        return r.recalculado(fecha, comprobantes.size(), descuadres.size(), resultado(comprobantes.size(), descuadres.size()),
                JavaCallJson.legacy().toJson(descuadres));
    }

    private Resumen cuadraCentralizacion(Resumen r, List<Movimiento> movimientos,
                                         List<CentralizaMultiSeleccionService.Resultado> simulados, LocalDate fecha) {
        Long tipo = r.idTipoCentralizacion();
        List<Movimiento> actuales = movimientos.stream().filter(m -> tipo.equals(m.idTipoCentralizacion())).toList();
        List<Movimiento> descuadres = new ArrayList<>();
        // Simulated but not currently centralized.
        for (CentralizaMultiSeleccionService.Resultado s : simulados) {
            if (!tipo.equals(s.idTipoCentralizacion())) continue;
            boolean existe = actuales.stream().anyMatch(m -> Objects.equals(m.numero(), s.numero()));
            if (!existe) descuadres.add(Movimiento.of(s, "Movimiento no Centralizado"));
        }
        // Currently centralized but no longer produced by the simulation.
        for (Movimiento m : actuales) {
            boolean existe = simulados.stream().anyMatch(s -> tipo.equals(s.idTipoCentralizacion()) && Objects.equals(m.numero(), s.numero()));
            if (!existe) descuadres.add(m.conDescripcion("Movimiento ya no es Contabilizable"));
        }
        return r.recalculado(fecha, actuales.size(), descuadres.size(), resultado(actuales.size(), descuadres.size()),
                JavaCallJson.legacy().toJson(descuadres));
    }

    private static String resultado(int total, int descuadres) {
        if (total == 0 && descuadres == 0) return "UI_SIN_OPERACIONES";
        return descuadres == 0 ? "UI_CUADRADO" : "UI_DESCUADRADO";
    }

    private static MapSqlParameterSource porIds(Entrada entrada) {
        return new MapSqlParameterSource().addValue("IdEmpresa", entrada.idEmpresa())
                .addValue("IdCuadraturaDiariaOperaciones", entrada.ids());
    }
}
