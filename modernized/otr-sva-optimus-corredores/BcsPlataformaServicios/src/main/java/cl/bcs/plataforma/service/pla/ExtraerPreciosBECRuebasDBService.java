package cl.bcs.plataforma.service.pla;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExtraerPreciosBECRuebasDBService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ExtraerPreciosBECRuebasDBService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    @Value("${ruebas.db.driver:com.sap.db.jdbc.Driver}") private String ruebasDriver;
    @Value("${ruebas.db.url:}")  private String ruebasUrl;
    @Value("${ruebas.db.user:}") private String ruebasUser;
    @Value("${ruebas.db.pass:}") private String ruebasPass;

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_PRECIOS_BEC;
    private final String SQL_INSTRUMENTOS;
    private final String SQL_INSERT;

    public ExtraerPreciosBECRuebasDBService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_PRECIOS_BEC  = loadSql("cl/bcs/plataforma/pla/sql/ExtraerPreciosBECRuebasDB_SelectPreciosBECRuebas.sql");
        this.SQL_INSTRUMENTOS = loadSql("cl/bcs/plataforma/pla/sql/ExtraerPreciosBECRuebasDB_SelectInstrumentos.sql");
        this.SQL_INSERT        = loadSql("cl/bcs/plataforma/pla/sql/ExtraerPreciosBECRuebasDB_InsertPreciosBEC.sql");
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.pla.ExtraerPreciosBECRuebasDB"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("INICIO::ExtraerPreciosBECRuebasDB");
        Salida out = new Salida();
        try {
            String fechaSistema = params.has("p_Fecha") ? params.get("p_Fecha").getAsString() : "";

            if (ruebasUrl.isBlank()) {
                out.P_OUT_ERRORESCONTADOR = 1L;
                out.P_OUT_MENSAJE = "ERROR NO SE PUEDE LEER configuracion ruebas DB";
                return toJson(out);
            }

            List<Instrumento> instrumentos = jdbc.query(SQL_INSTRUMENTOS,
                new MapSqlParameterSource(), BeanPropertyRowMapper.newInstance(Instrumento.class));

            Map<String, Instrumento> hash = new HashMap<>();
            for (Instrumento i : instrumentos) hash.put(i.Nemotecnico.trim().toUpperCase(), i);

            DriverManagerDataSource ds = new DriverManagerDataSource(ruebasUrl, ruebasUser, ruebasPass);
            ds.setDriverClassName(ruebasDriver);
            NamedParameterJdbcTemplate ruebasJdbc = new NamedParameterJdbcTemplate(ds);

            List<InstrumentoBEC> preciosBEC = ruebasJdbc.query(SQL_PRECIOS_BEC,
                new MapSqlParameterSource().addValue("Bolsa", "BEC").addValue("Fecha", fechaSistema),
                BeanPropertyRowMapper.newInstance(InstrumentoBEC.class));

            List<BolsaInstrumentoPrecio> nuevos = new ArrayList<>();
            for (InstrumentoBEC bec : preciosBEC) {
                String nemo = bec.NEMO == null ? "" : bec.NEMO.trim().toUpperCase();
                if (!hash.containsKey(nemo)) { log.warn("Instrumento no en Optimus: {}", nemo); continue; }
                BolsaInstrumentoPrecio p = new BolsaInstrumentoPrecio();
                p.IdInstrumento = hash.get(nemo).IdInstrumento;
                p.Nemotecnico = nemo; p.IdBolsa = "BEC";
                p.Fecha = Date.valueOf(bec.FECHA.substring(0, 10));
                p.PrecioMayor = bec.MEJOR_PRECIO; p.PrecioMenor = bec.PEOR_PRECIO;
                p.PrecioApertura = BigDecimal.ZERO; p.PrecioCierre = BigDecimal.ZERO;
                p.PrecioMedio = BigDecimal.ZERO; p.UnidadesTransadas = BigDecimal.ZERO;
                p.MontoTransado = BigDecimal.ZERO; p.Variacion = BigDecimal.ZERO;
                p.HoraPrecioMaximo = parseTime(bec.MEJOR_HORA);
                p.HoraPrecioMinimo = parseTime(bec.PEOR_HORA);
                nuevos.add(p);
            }

            if (!nuevos.isEmpty()) {
                MapSqlParameterSource[] batch = nuevos.stream()
                    .map(p -> new MapSqlParameterSource()
                        .addValue("IdInstrumento", p.IdInstrumento)
                        .addValue("IdBolsa", p.IdBolsa)
                        .addValue("Nemotecnico", p.Nemotecnico)
                        .addValue("Fecha", p.Fecha)
                        .addValue("PrecioMayor", p.PrecioMayor)
                        .addValue("PrecioMenor", p.PrecioMenor)
                        .addValue("PrecioApertura", p.PrecioApertura)
                        .addValue("PrecioCierre", p.PrecioCierre)
                        .addValue("PrecioMedio", p.PrecioMedio)
                        .addValue("UnidadesTransadas", p.UnidadesTransadas)
                        .addValue("MontoTransado", p.MontoTransado)
                        .addValue("Variacion", p.Variacion)
                        .addValue("HoraPrecioMaximo", p.HoraPrecioMaximo)
                        .addValue("HoraPrecioMinimo", p.HoraPrecioMinimo))
                    .toArray(MapSqlParameterSource[]::new);
                jdbc.batchUpdate(SQL_INSERT, batch);
                log.info("Insertados {} precios BEC", nuevos.size());
            }
        } catch (Exception e) {
            log.error("Error en ExtraerPreciosBECRuebasDB: {}", e.getMessage(), e);
            out.P_OUT_ERRORESCONTADOR = 1L;
            out.P_OUT_MENSAJE = "ERROR: " + e.getMessage();
        }
        return toJson(out);
    }

    private Time parseTime(String t) {
        if (t == null || t.isBlank()) return Time.valueOf("00:00:00");
        return Time.valueOf(LocalTime.parse(t, DateTimeFormatter.ofPattern("HHmmss")));
    }

    private JsonObject toJson(Object o) {
        return JsonParser.parseString(GSON.toJson(o)).getAsJsonObject();
    }

    private String loadSql(String path) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) { throw new IllegalStateException("SQL not found: " + path, e); }
    }

    public static class InstrumentoBEC {
        public String FECHA; public String NEMO; public String MEJOR_HORA;
        public BigDecimal MEJOR_PRECIO; public String PEOR_HORA; public BigDecimal PEOR_PRECIO;
    }

    public static class Instrumento { public Long IdInstrumento; public String Nemotecnico; }

    public static class BolsaInstrumentoPrecio {
        public Long IdInstrumento; public String IdBolsa; public String Nemotecnico;
        public Date Fecha; public BigDecimal PrecioMayor; public BigDecimal PrecioMenor;
        public BigDecimal PrecioApertura; public BigDecimal PrecioCierre; public BigDecimal PrecioMedio;
        public BigDecimal UnidadesTransadas; public BigDecimal MontoTransado; public BigDecimal Variacion;
        public Time HoraPrecioMaximo; public Time HoraPrecioMinimo;
    }

    public static class Salida { public Long P_OUT_ERRORESCONTADOR = 0L; public String P_OUT_MENSAJE = ""; }
}
