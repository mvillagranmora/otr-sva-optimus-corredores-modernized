package cl.bcs.portafolio.javacall;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cl.bcs.portafolio.javacall.JavaCallInfra.*;
import static cl.bcs.portafolio.javacall.JavaCallInfra.MensajeSalida.*;

/**
 * Migrates cl.bcs.portafolio.ws.ModificarEstadoPortafolioWS.
 *
 * Validates format/catalog/business rules locally, then delegates to
 * SP "BSCL.Portafolio.BD.Servicios::ModificarEstadoPortafolio" via SimpleJdbcCall.
 */
@Service
public class ModificarEstadoPortafolioService {

    private static final Logger log = LoggerFactory.getLogger(ModificarEstadoPortafolioService.class);
    private static final Gson GSON = new Gson();

    private static final String SQL_ESTADO = loadSql("cl/bcs/portafolio/ws/sql/ModificarEstadoPortafolioWS_SelectEstadoPortafolio.sql");
    private static final String SQL_BLOQUEO = loadSql("cl/bcs/portafolio/ws/sql/ModificarEstadoPortafolioWS_SelectTiposBloqueos.sql");
    private static final String SQL_BLOQUEO_TODOS = loadSql("cl/bcs/portafolio/ws/sql/ModificarEstadoPortafolioWS_SelectTiposBloqueosTodos.sql");

    private final DataSource dataSource;
    private final HanaUtilService hanaUtil;

    public ModificarEstadoPortafolioService(DataSource dataSource, HanaUtilService hanaUtil) {
        this.dataSource = dataSource;
        this.hanaUtil = hanaUtil;
    }

    // ── POJOs ───────────────────────────────────────────────────────────────

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;
        public String p_DocumentoIdentidad;
        public String p_CodigoPortafolio;
        public Long p_IdPortafolio;
        public String p_IdEstado;
        public String p_IdTipo;
        public String p_Fecha;
        public String p_FechaHasta;
        public String p_Comentario;
    }

    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
    }

    public static class EstadoRow { public String IdEstado; public String NombreEntidad; }
    public static class TipoBloqueoRow { public String IdTipoBloqueo; public String IdEstado; public String IdAccion; }
    public static class ErrorDetalle { public String Codigo; public String Descripcion; }
    public static class AdvertenciaDetalle { public String Codigo; public String RequiereAutorizacion; public String Descripcion; }

    // ── Entry point ──────────────────────────────────────────────────────────

    public JsonObject modificarEstadoPortafolio(Connection conn, JsonObject in) throws Exception {
        Entrada entrada = GSON.fromJson(in.toString(), Entrada.class);
        Salida salida = new Salida();

        salida.P_OUT_MENSAJES.addAll(validarFormato(entrada));
        salida.P_OUT_ERRORESCONTADOR = countErrors(salida.P_OUT_MENSAJES);
        if (salida.P_OUT_ERRORESCONTADOR > 0) return toJson(salida);

        salida.P_OUT_MENSAJES.addAll(validarCatalogos(conn, entrada));
        salida.P_OUT_ERRORESCONTADOR = countErrors(salida.P_OUT_MENSAJES);
        if (salida.P_OUT_ERRORESCONTADOR > 0) return toJson(salida);

        salida.P_OUT_MENSAJES.addAll(validarReglasNegocio(conn, entrada));
        salida.P_OUT_ERRORESCONTADOR = countErrors(salida.P_OUT_MENSAJES);
        if (salida.P_OUT_ERRORESCONTADOR > 0) return toJson(salida);

        salida = ejecutarSP(conn, entrada);
        return toJson(salida);
    }

    // ── Validaciones ─────────────────────────────────────────────────────────

    private List<MensajeSalida> validarFormato(Entrada e) {
        List<MensajeSalida> msgs = new ArrayList<>();

        if (e.p_IdEmpresa == null)
            msgs.add(error("UI_ERROR_IDEMPRESA_NULO"));
        if (e.p_DocumentoIdentidad == null || e.p_DocumentoIdentidad.isBlank())
            msgs.add(error("UI_ERROR_DOCUMENTOIDENTIDAD_VACIO"));
        else if (!e.p_DocumentoIdentidad.trim().matches("^\\d{1,8}-[\\dkK]$"))
            msgs.add(error("UI_ERROR_DOCUMENTOIDENTIDAD_FORMATO_INVALIDO"));
        if (e.p_CodigoPortafolio == null || e.p_CodigoPortafolio.isBlank())
            msgs.add(error("UI_ERROR_CODIGOPORTAFOLIO_VACIO"));
        else if (!e.p_CodigoPortafolio.trim().matches("^[a-zA-Z0-9-]+$"))
            msgs.add(error("UI_ERROR_CODIGOPORTAFOLIO_CARACTERES_INVALIDOS"));
        if (e.p_IdEstado == null || e.p_IdEstado.isBlank())
            msgs.add(error("UI_ERROR_IDESTADO_VACIO"));
        if (e.p_IdTipo == null || e.p_IdTipo.isBlank())
            msgs.add(error("UI_ERROR_IDTIPO_VACIO"));
        else if (!e.p_IdTipo.trim().matches("^[a-zA-Z0-9]+$"))
            msgs.add(error("UI_ERROR_IDTIPO_CARACTERES_INVALIDOS"));
        if (e.p_Fecha != null && !e.p_Fecha.isBlank()
                && !e.p_Fecha.trim().matches("^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$"))
            msgs.add(error("UI_ERROR_FECHA_FORMATO_INVALIDO"));
        if (e.p_FechaHasta != null && !e.p_FechaHasta.isBlank()
                && !e.p_FechaHasta.trim().matches("^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$"))
            msgs.add(error("UI_ERROR_FECHAHASTA_FORMATO_INVALIDO"));
        if (e.p_Fecha != null && e.p_FechaHasta != null
                && !e.p_Fecha.isBlank() && !e.p_FechaHasta.isBlank()) {
            try {
                if (LocalDate.parse(e.p_FechaHasta).isBefore(LocalDate.parse(e.p_Fecha)))
                    msgs.add(error("UI_ERROR_RANGO_FECHAS_INVALIDO"));
            } catch (Exception ex) {
                msgs.add(error("UI_ERROR_RANGO_FECHAS_INVALIDO_EXCEPCION"));
            }
        }
        if (e.p_Comentario == null || e.p_Comentario.isBlank())
            msgs.add(error("UI_ERROR_COMENTARIO_VACIO"));
        else {
            e.p_Comentario = e.p_Comentario.trim();
            if (e.p_Comentario.length() > 255) e.p_Comentario = e.p_Comentario.substring(0, 255);
            if (!e.p_Comentario.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ0-9\\s\\.,\\-\\(\\)]+$"))
                msgs.add(error("UI_ERROR_COMENTARIO_CARACTERES_INVALIDOS"));
        }
        return msgs;
    }

    private List<MensajeSalida> validarCatalogos(Connection conn, Entrada e) {
        List<MensajeSalida> msgs = new ArrayList<>();

        List<EstadoRow> estados = query(conn,
            SQL_ESTADO,
            Map.of("IdEstado", e.p_IdEstado),
            fieldMapper(EstadoRow.class));
        if (estados.isEmpty()) {
            msgs.add(error("UI_ERROR_IDESTADO_NO_EXISTE", "IdEstado=" + e.p_IdEstado));
            return msgs;
        }

        List<TipoBloqueoRow> tipos = query(conn,
            SQL_BLOQUEO,
            Map.of("IdTipo", e.p_IdTipo),
            fieldMapper(TipoBloqueoRow.class));
        if (tipos.isEmpty()) {
            List<TipoBloqueoRow> todos = query(conn, SQL_BLOQUEO_TODOS, Map.of(), fieldMapper(TipoBloqueoRow.class));
            String validos = todos.stream().map(t -> t.IdTipoBloqueo).collect(Collectors.joining(", "));
            msgs.add(error("UI_ERROR_IDTIPO_NO_EXISTE", "IdTipo=" + e.p_IdTipo + " válidos: " + validos));
            return msgs;
        }

        boolean coincide = tipos.stream().anyMatch(t -> e.p_IdEstado.equalsIgnoreCase(t.IdAccion));
        if (!coincide)
            msgs.add(error("UI_ERROR_IDTIPO_NO_CORRESPONDE_IDESTADO", "IdTipo=" + e.p_IdTipo + " IdEstado=" + e.p_IdEstado));

        return msgs;
    }

    private List<MensajeSalida> validarReglasNegocio(Connection conn, Entrada e) {
        List<MensajeSalida> msgs = new ArrayList<>();
        String idEstado = e.p_IdEstado != null ? e.p_IdEstado.trim().toUpperCase() : "";
        String idTipo = e.p_IdTipo != null ? e.p_IdTipo.trim().toUpperCase() : "";
        boolean permiteFechaLibre = "BLO".equals(idEstado) && "BMUERT".equals(idTipo);

        String fechaSistema = hanaUtil.getFechaSistema(conn, e.p_IdEmpresa);
        if (fechaSistema != null && fechaSistema.length() >= 10) fechaSistema = fechaSistema.substring(0, 10);

        if (e.p_Fecha == null || e.p_Fecha.isBlank()) {
            msgs.add(error("UI_ERROR_FECHA_OBLIGATORIA"));
            return msgs;
        }
        if (!permiteFechaLibre && !e.p_Fecha.trim().equals(fechaSistema)) {
            msgs.add(error("UI_ERROR_FECHA_NO_CORRESPONDE"));
            return msgs;
        }

        boolean tieneFechaHasta = e.p_FechaHasta != null && !e.p_FechaHasta.isBlank();
        if (Arrays.asList("BTEMPO", "DESBTEMPORAL").contains(idTipo)) {
            if (!tieneFechaHasta) {
                msgs.add(error("UI_ERROR_FECHAHASTA_OBLIGATORIA", "IdTipo=" + idTipo));
                return msgs;
            }
        } else if (tieneFechaHasta) {
            msgs.add(error("UI_ERROR_FECHAHASTA_NO_PERMITIDA", "IdTipo=" + idTipo));
            return msgs;
        }
        return msgs;
    }

    // ── SP execution ─────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private Salida ejecutarSP(Connection conn, Entrada e) {
        Salida salida = new Salida();

        boolean permiteFechaLibre = "BLO".equalsIgnoreCase(e.p_IdEstado) && "BMUERT".equalsIgnoreCase(e.p_IdTipo);
        String fechaDesde = permiteFechaLibre ? e.p_Fecha : hanaUtil.getFechaSistema(conn, e.p_IdEmpresa);
        if (fechaDesde != null && fechaDesde.length() >= 10) fechaDesde = fechaDesde.substring(0, 10);

        String fechaHasta = (e.p_FechaHasta != null && !e.p_FechaHasta.isBlank()) ? e.p_FechaHasta : null;

        SimpleJdbcCall call = new SimpleJdbcCall(dataSource)
            .withSchemaName("BSCL")
            .withProcedureName("Portafolio.BD.Servicios::ModificarEstadoPortafolio")
            .withoutProcedureColumnMetaDataAccess();

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("p_IdEmpresa", e.p_IdEmpresa)
            .addValue("p_IdUsuario", e.p_IdUsuario)
            .addValue("p_IdModulo", e.p_IdModulo)
            .addValue("p_UsaReglasNegocio", e.p_UsaReglasNegocio)
            .addValue("p_IdPortafolio", e.p_IdPortafolio)
            .addValue("p_IdEstado", e.p_IdEstado)
            .addValue("p_IdTipo", e.p_IdTipo)
            .addValue("p_Fecha", fechaDesde)
            .addValue("p_FechaHasta", fechaHasta)
            .addValue("p_Comentario", e.p_Comentario);

        try {
            Map<String, Object> result = call.execute(params);

            List<?> errores = (List<?>) result.get("p_out_ErroresDetalle");
            List<?> advertencias = (List<?>) result.get("p_out_AdvertenciasDetalle");

            if (errores != null) {
                for (Object o : errores) {
                    if (o instanceof Map<?,?> m) {
                        salida.P_OUT_MENSAJES.add(new MensajeSalida(
                            0L, ERROR,
                            String.valueOf(m.get("Codigo")),
                            String.valueOf(m.get("Descripcion"))));
                    }
                }
            }
            if (advertencias != null) {
                for (Object o : advertencias) {
                    if (o instanceof Map<?,?> m) {
                        salida.P_OUT_MENSAJES.add(new MensajeSalida(
                            0L, ADVERTENCIA,
                            String.valueOf(m.get("Codigo")),
                            String.valueOf(m.get("Descripcion"))));
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Error ejecutando SP ModificarEstadoPortafolio", ex);
            salida.P_OUT_MENSAJES.add(error("ERROR_SP_MODIFICAR_ESTADO_PORTAFOLIO", ex.getMessage()));
        }
        salida.P_OUT_ERRORESCONTADOR = countErrors(salida.P_OUT_MENSAJES);
        return salida;
    }

    private static JsonObject toJson(Object o) {
        return new Gson().fromJson(new Gson().toJson(o), JsonObject.class);
    }
}
