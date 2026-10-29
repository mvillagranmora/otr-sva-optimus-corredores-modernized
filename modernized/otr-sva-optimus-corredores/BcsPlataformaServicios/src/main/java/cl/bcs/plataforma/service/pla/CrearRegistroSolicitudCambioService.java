package cl.bcs.plataforma.service.pla;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CrearRegistroSolicitudCambioService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(CrearRegistroSolicitudCambioService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_INSERT;
    private final String SQL_SELECT_PORTAFOLIO_APV;
    private final String SQL_SELECT_PORTAFOLIOS_APV_ASOCIADOS;
    private static final String SQL_NEXT_SEQ =
        "SELECT \"BSCL.Plataforma.BD.Secuencias\".\"PLA.IdSolicitudCambio\".NEXTVAL FROM DUMMY";

    public CrearRegistroSolicitudCambioService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_INSERT = loadSql("cl/bcs/plataforma/pla/sql/CrearRegistroSolicitudCambio_InsertSolicitudCambio.sql");
        this.SQL_SELECT_PORTAFOLIO_APV = loadSql("cl/bcs/plataforma/pla/sql/CrearRegistroSolicitudCambio_SelectInformacionPortafolioAPV.sql");
        this.SQL_SELECT_PORTAFOLIOS_APV_ASOCIADOS = loadSql("cl/bcs/plataforma/pla/sql/CrearRegistroSolicitudCambio_SelectPortafoliosAPVAsociados.sql");
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.pla.CrearRegistroSolicitudCambio"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("INICIO::CrearRegistroSolicitudCambio");
        Entrada in = GSON.fromJson(params, Entrada.class);
        Salida out = new Salida();

        try {
            RegistroSolicitudCambio portafolioReg = in.p_RegistroSolicitudCambio.stream()
                    .filter(r -> "PORTAFOLIO".equals(r.IdTipoEntidad))
                    .findFirst().orElse(null);

            List<RegistroSolicitudCambio> replicasAPV = new ArrayList<>();
            if (portafolioReg != null) {
                List<Portafolio> portafoliosAPV = jdbc.query(SQL_SELECT_PORTAFOLIO_APV,
                        new MapSqlParameterSource("IdPortafolio", portafolioReg.IdEntidadOrigen),
                        BeanPropertyRowMapper.newInstance(Portafolio.class));

                if (!portafoliosAPV.isEmpty()) {
                    Portafolio pAPV = portafoliosAPV.get(0);
                    List<Portafolio> asociados = jdbc.query(SQL_SELECT_PORTAFOLIOS_APV_ASOCIADOS,
                            new MapSqlParameterSource()
                                .addValue("IdCliente", pAPV.IdCliente)
                                .addValue("IdPortafolio", pAPV.IdPortafolio)
                                .addValue("IdProductoAgrupador", pAPV.IdProductoAgrupador),
                            BeanPropertyRowMapper.newInstance(Portafolio.class));

                    RegistroSolicitudCambio finalPortafolioReg = portafolioReg;
                    replicasAPV = asociados.stream().map(p -> {
                        RegistroSolicitudCambio r = new RegistroSolicitudCambio();
                        r.IdSolicitudCambio = finalPortafolioReg.IdSolicitudCambio;
                        r.IdTipoEntidad = finalPortafolioReg.IdTipoEntidad;
                        r.IdEntidadOrigen = String.valueOf(p.IdPortafolio);
                        r.CampoModificado = finalPortafolioReg.CampoModificado;
                        r.TipoDato = finalPortafolioReg.TipoDato;
                        r.ValorAntiguo = finalPortafolioReg.ValorAntiguo;
                        r.ValorNuevo = finalPortafolioReg.ValorNuevo;
                        r.IdEstadoAutorizacion = finalPortafolioReg.IdEstadoAutorizacion;
                        return r;
                    }).collect(Collectors.toList());
                }
            }

            List<RegistroSolicitudCambio> toInsert = new ArrayList<>(in.p_RegistroSolicitudCambio);
            toInsert.addAll(replicasAPV);

            if (!toInsert.isEmpty()) {
                SqlParameterSource[] batch = toInsert.stream()
                        .map(BeanPropertySqlParameterSource::new)
                        .toArray(SqlParameterSource[]::new);
                jdbc.batchUpdate(SQL_INSERT, batch);
            }

        } catch (Exception e) {
            log.error("Error en CrearRegistroSolicitudCambio: {}", e.getMessage(), e);
        }

        return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
    }

    public static Long obtenerIdSolicitudCambio(NamedParameterJdbcTemplate jdbc) {
        return jdbc.queryForObject(SQL_NEXT_SEQ, new MapSqlParameterSource(), Long.class);
    }

    public static class Entrada {
        public List<RegistroSolicitudCambio> p_RegistroSolicitudCambio = new ArrayList<>();
    }

    public static class RegistroSolicitudCambio {
        public Long IdSolicitudCambio;
        public String IdTipoEntidad;
        public String IdEntidadOrigen;
        public String CampoModificado;
        public String TipoDato;
        public String ValorAntiguo;
        public String ValorNuevo;
        public String IdEstadoAutorizacion;
    }

    public static class Portafolio {
        public Long IdPortafolio;
        public Long IdMatrizAgrupadorAPV;
        public Long IdCliente;
        public Integer IdProductoAgrupador;
    }

    public static class Salida {
        public List<Secuencia> P_OUT_IDSECUENCIAS = new ArrayList<>();
    }

    public static class Secuencia {
        public Long IdSecuencia;
    }

    private static String loadSql(String path) {
        try (InputStream is = CrearRegistroSolicitudCambioService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
