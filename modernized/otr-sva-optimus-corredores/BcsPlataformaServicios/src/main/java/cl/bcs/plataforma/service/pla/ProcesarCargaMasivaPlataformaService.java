package cl.bcs.plataforma.service.pla;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProcesarCargaMasivaPlataformaService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ProcesarCargaMasivaPlataformaService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_SELECT_DETALLE_PE;
    private final String SQL_ACTUALIZA_ESTADO;
    private final String SQL_ACTUALIZA_ESTADO_DETALLE;
    private final String SQL_INSERT_VALOR_RIESGO_RV;
    private static final String SQL_SELECT_LOCALIZACION = "SELECT \"Valor\" FROM \"BSCL\".\"BSCL.Plataforma.BD::PLA.Configuracion\" WHERE \"CodigoParametro\" = 'PLA.LocalizacionSistema'";
    private static final String SQL_SELECT_INSTRUMENTOS_RV = "SELECT \"IdInstrumento\" FROM \"BSCL\".\"BSCL.Plataforma.BD::INS.Instrumento\" WHERE \"Nemotecnico\" = :Nemotecnico";
    private static final String SQL_SELECT_INS_CLASIFICACION = "SELECT 1 AS \"IdEmpresa\", INS.\"Nemotecnico\",IVC.\"IdInstrumento\",IVC.\"IdEntidadClasificadoraRiesgo\" FROM \"BSCL\".\"BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo\" AS IVC INNER JOIN \"BSCL\".\"BSCL.Plataforma.BD::INS.Instrumento\" AS INS ON INS.\"IdInstrumento\" = IVC.\"IdInstrumento\" WHERE INS.\"Nemotecnico\" = :Nemotecnico AND IVC.\"IdEntidadClasificadoraRiesgo\" = :IdEntidadClasificadoraRiesgo";
    private static final String SQL_UPDATE_RF = "UPDATE \"BSCL\".\"BSCL.Plataforma.BD::INS.InstrumentoPorcentajeDescuento\" SET \"PorcentajeDescuento\" = :PorcentajeDescuento WHERE \"IdInstrumento\" = :IdInstrumento";

    public ProcesarCargaMasivaPlataformaService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_SELECT_DETALLE_PE        = loadSql("cl/bcs/plataforma/pla/sql/ProcesarCargaMasivaPlataforma_SelectCargaMasivaDetallePeru.sql");
        this.SQL_ACTUALIZA_ESTADO         = loadSql("cl/bcs/plataforma/pla/sql/ProcesarCargaMasivaPlataforma_ActualizaEstadoCargaMasiva.sql");
        this.SQL_ACTUALIZA_ESTADO_DETALLE = loadSql("cl/bcs/plataforma/pla/sql/ProcesarCargaMasivaPlataforma_ActualizaEstadoCargaMasivaDetalle.sql");
        this.SQL_INSERT_VALOR_RIESGO_RV   = loadSql("cl/bcs/plataforma/ins/sql/CargaArchivoValorRiesgoRV_InsertValorRiesgo.sql");
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.pla.ProcesarCargaMasivaPlataforma"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("INICIO::ProcesarCargaMasivaPlataforma");
        Salida out = new Salida();
        try {
            Long idEmpresa       = params.has("p_IdEmpresa")          ? params.get("p_IdEmpresa").getAsLong()          : 1L;
            Long idUsuario       = params.has("p_IdUsuario")          ? params.get("p_IdUsuario").getAsLong()          : 0L;
            String idModulo      = params.has("p_IdModulo")           ? params.get("p_IdModulo").getAsString()         : "PLA";
            String fechaProceso  = params.has("p_FechaProceso")       ? params.get("p_FechaProceso").getAsString()     : "";
            Long idCargaMasiva   = params.has("p_CargaMasivaProcesar") ? params.get("p_CargaMasivaProcesar").getAsLong() : 0L;

            // Determine localization
            List<String> locRows = jdbc.queryForList(SQL_SELECT_LOCALIZACION, new MapSqlParameterSource(), String.class);
            String localizacion = locRows.isEmpty() ? "" : locRows.get(0);

            List<DetalleCarga> detalles = new ArrayList<>();
            if ("PE".equals(localizacion)) {
                detalles = jdbc.query(SQL_SELECT_DETALLE_PE,
                    new MapSqlParameterSource().addValue("IdEmpresa", idEmpresa).addValue("IdCargaMasiva", idCargaMasiva),
                    BeanPropertyRowMapper.newInstance(DetalleCarga.class));
            }

            List<ValorRiesgoRV> valoresRV = new ArrayList<>();

            for (DetalleCarga det : detalles) {
                int tipo = det.IdTipoCargaMasiva != null ? det.IdTipoCargaMasiva.intValue() : 0;
                boolean ok = false;
                switch (tipo) {
                    case 1 -> { // RF - PorcentajeDescuento
                        // Inline RF update: look up instrument and update percentage
                        List<Long> idInst = jdbc.queryForList(SQL_SELECT_INSTRUMENTOS_RV,
                            new MapSqlParameterSource("Nemotecnico", det.Nemotecnico), Long.class);
                        if (!idInst.isEmpty()) {
                            jdbc.update(SQL_UPDATE_RF, new MapSqlParameterSource()
                                .addValue("IdInstrumento", idInst.get(0))
                                .addValue("PorcentajeDescuento", det.RF_PorcentajeDescuento));
                            ok = true;
                        }
                    }
                    case 2 -> { // RV - ValorRiesgo
                        if ("PE".equals(localizacion)) {
                            List<Long> existing = jdbc.queryForList(SQL_SELECT_INS_CLASIFICACION,
                                new MapSqlParameterSource()
                                    .addValue("Nemotecnico", det.Nemotecnico)
                                    .addValue("IdEntidadClasificadoraRiesgo", det.RV_IdEntidadClasificadoraRiesgo),
                                Long.class);
                            if (existing.isEmpty()) {
                                List<Long> idInst = jdbc.queryForList(SQL_SELECT_INSTRUMENTOS_RV,
                                    new MapSqlParameterSource("Nemotecnico", det.Nemotecnico), Long.class);
                                if (!idInst.isEmpty()) {
                                    ValorRiesgoRV vr = new ValorRiesgoRV();
                                    vr.IdInstrumento = idInst.get(0);
                                    vr.Entidad = det.RV_IdEntidadClasificadoraRiesgo;
                                    vr.Valor = det.RV_Valor;
                                    valoresRV.add(vr);
                                }
                            }
                        }
                        ok = true;
                    }
                }
                if (ok) {
                    jdbc.update(SQL_ACTUALIZA_ESTADO, new MapSqlParameterSource()
                        .addValue("IdCargaMasiva", idCargaMasiva).addValue("Fecha", fechaProceso));
                    jdbc.update(SQL_ACTUALIZA_ESTADO_DETALLE, new MapSqlParameterSource()
                        .addValue("IdCargaMasiva", idCargaMasiva));
                }
            }

            if (!valoresRV.isEmpty()) {
                MapSqlParameterSource[] batch = valoresRV.stream()
                    .map(vr -> new MapSqlParameterSource()
                        .addValue("IdInstrumento", vr.IdInstrumento)
                        .addValue("Entidad", vr.Entidad)
                        .addValue("Valor", vr.Valor))
                    .toArray(MapSqlParameterSource[]::new);
                jdbc.batchUpdate(SQL_INSERT_VALOR_RIESGO_RV, batch);
            }

            addMsg(out, "OK", "PROCESADO", "Carga masiva " + idCargaMasiva + " procesada");
        } catch (Exception e) {
            log.error("Error en ProcesarCargaMasivaPlataforma: {}", e.getMessage(), e);
            addMsg(out, "ERROR", "ERROR_INTERNO", e.getMessage());
            out.P_OUT_ERRORESCONTADOR = 1L;
        }
        return toJson(out);
    }

    private void addMsg(Salida out, String tipo, String codigo, String desc) {
        Mensaje m = new Mensaje(); m.tipo = tipo; m.codigo = codigo; m.descripcion = desc;
        out.P_OUT_MENSAJES.add(m);
    }

    private JsonObject toJson(Object o) { return JsonParser.parseString(GSON.toJson(o)).getAsJsonObject(); }

    private String loadSql(String path) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) { throw new IllegalStateException("SQL not found: " + path, e); }
    }

    public static class DetalleCarga {
        public Long IdTipoCargaMasiva; public Long IdCargaMasiva; public Long IdCargaMasivaDetalle;
        public String Nemotecnico; public String RV_IdEntidadClasificadoraRiesgo;
        public String RV_Valor; public BigDecimal RF_PorcentajeDescuento;
    }

    public static class ValorRiesgoRV { public Long IdInstrumento; public String Entidad; public String Valor; }

    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<Mensaje> P_OUT_MENSAJES = new ArrayList<>();
    }

    public static class Mensaje { public String tipo; public String codigo; public String descripcion; }
}
