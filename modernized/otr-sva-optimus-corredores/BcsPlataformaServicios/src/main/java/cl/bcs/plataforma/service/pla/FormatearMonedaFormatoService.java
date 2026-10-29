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

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class FormatearMonedaFormatoService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(FormatearMonedaFormatoService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_SELECT_FORMATO;

    public FormatearMonedaFormatoService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_SELECT_FORMATO = loadSql("cl/bcs/plataforma/pla/sql/Formato_ConsultarMonedaFormato.sql");
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.pla.FormatearMonedaFormato"; }

    @Override
    public JsonObject execute(JsonObject params) {
        log.debug("INICIO::FormatearMonedaFormato");
        Entrada in = GSON.fromJson(params, Entrada.class);
        Salida out = new Salida();

        try {
            List<MonedaFormato> formatos = jdbc.query(SQL_SELECT_FORMATO,
                    new MapSqlParameterSource(),
                    BeanPropertyRowMapper.newInstance(MonedaFormato.class));

            for (InItem item : in.ListaMonedasFormatos) {
                Resultado r = formatear(formatos, item.valor, item.tipoFormato,
                        item.idMoneda, item.idTipoInstrumento, item.idTipoOperacion);
                if (r.valor == null) {
                    out.P_OUT_ERRORESCONTADOR++;
                    out.P_OUT_MENSAJES.add("ERROR: Formato no encontrado para " + item.idMoneda);
                } else {
                    out.P_OUT_RESULTADO.add(r);
                }
            }
        } catch (Exception e) {
            log.error("Error en FormatearMonedaFormato: {}", e.getMessage(), e);
            out.P_OUT_ERRORESCONTADOR++;
        }

        return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
    }

    private Resultado formatear(List<MonedaFormato> formatos, String valorStr, String tipoFormato,
                                String idMoneda, String idTipoInstrumento, String idTipoOperacion) {
        Resultado r = new Resultado();
        if (idMoneda == null) return r;

        BigDecimal valor;
        try {
            valor = valorStr != null ? new BigDecimal(valorStr) : BigDecimal.ZERO;
        } catch (NumberFormatException e) {
            valor = BigDecimal.ZERO;
        }

        String uso = "GEN";
        String ti = idTipoInstrumento != null ? idTipoInstrumento : "";
        String to = idTipoOperacion != null ? idTipoOperacion : "";

        MonedaFormato formato = formatos.stream()
                .filter(f -> uso.equalsIgnoreCase(f.IdUsoMonedaFormato) && idMoneda.equalsIgnoreCase(f.IdMoneda)
                        && ti.equalsIgnoreCase(f.IdTipoInstrumento) && to.equalsIgnoreCase(f.IdTipoOperacion))
                .findFirst()
                .or(() -> formatos.stream().filter(f -> uso.equalsIgnoreCase(f.IdUsoMonedaFormato)
                        && idMoneda.equalsIgnoreCase(f.IdMoneda) && ti.equalsIgnoreCase(f.IdTipoInstrumento))
                        .findFirst())
                .or(() -> formatos.stream().filter(f -> uso.equalsIgnoreCase(f.IdUsoMonedaFormato)
                        && idMoneda.equalsIgnoreCase(f.IdMoneda))
                        .findFirst())
                .orElse(null);

        if (formato == null) return r;

        String truncaRedondea;
        int decimales;
        switch (tipoFormato != null ? tipoFormato.toUpperCase() : "") {
            case "MONTO"      -> { truncaRedondea = formato.MontoTrucaRedondea; decimales = formato.MontoDecimales != null ? formato.MontoDecimales : 0; }
            case "CANTIDAD"   -> { truncaRedondea = formato.CantidadTrucaRedondea; decimales = formato.CantidadDecimales != null ? formato.CantidadDecimales : 0; }
            case "PRECIO"     -> { truncaRedondea = formato.PrecioTrucaRedondea; decimales = formato.PrecioDecimales != null ? formato.PrecioDecimales : 0; }
            case "TASA"       -> { truncaRedondea = formato.TasaTrucaRedondea; decimales = formato.TasaDecimales != null ? formato.TasaDecimales : 0; }
            case "PARIDAD"    -> { truncaRedondea = formato.ParidadTrucaRedondea; decimales = formato.ParidadDecimales != null ? formato.ParidadDecimales : 0; }
            case "TIPOCAMBIO" -> { truncaRedondea = formato.TipoCambioTrucaRedondea; decimales = formato.TipoCambioDecimales != null ? formato.TipoCambioDecimales : 0; }
            case "PUNTOS"     -> { truncaRedondea = formato.PuntosTrucaRedondea; decimales = formato.PuntosDecimales != null ? formato.PuntosDecimales : 0; }
            default -> { return r; }
        }

        BigDecimal valorFinal = "T".equalsIgnoreCase(truncaRedondea)
                ? valor.setScale(decimales, RoundingMode.FLOOR)
                : valor.setScale(decimales, RoundingMode.HALF_UP);

        String valorFormateado;
        try {
            NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("es", "CL"));
            valorFormateado = nf.format(valorFinal).replace("Ch$", "");
        } catch (Exception e) {
            valorFormateado = valorFinal.toPlainString();
        }

        r.valor = valorFinal;
        r.uso = formato.IdUsoMonedaFormato;
        r.decimales = decimales;
        r.truncaRedondea = truncaRedondea;
        r.idMoneda = formato.IdMoneda;
        r.idTipoInstrumento = formato.IdTipoInstrumento;
        r.idTipoOperacion = formato.IdTipoOperacion;
        r.valorConFormato = valorFormateado;
        return r;
    }

    public static class Entrada {
        public Long IdEmpresa;
        public List<InItem> ListaMonedasFormatos = new ArrayList<>();
    }

    public static class InItem {
        public String valor;
        public String tipoFormato = "GEN";
        public String idMoneda = "CLP";
        public String idTipoInstrumento;
        public String idTipoOperacion;
    }

    public static class MonedaFormato {
        public String IdUsoMonedaFormato;
        public String IdMoneda;
        public String IdTipoInstrumento;
        public String IdTipoOperacion;
        public String MontoTrucaRedondea;
        public Integer MontoDecimales;
        public String CantidadTrucaRedondea;
        public Integer CantidadDecimales;
        public String PrecioTrucaRedondea;
        public Integer PrecioDecimales;
        public String TasaTrucaRedondea;
        public Integer TasaDecimales;
        public String ParidadTrucaRedondea;
        public Integer ParidadDecimales;
        public String TipoCambioTrucaRedondea;
        public Integer TipoCambioDecimales;
        public String PuntosTrucaRedondea;
        public Integer PuntosDecimales;
    }

    public static class Resultado {
        public BigDecimal valor;
        public String uso;
        public Integer decimales;
        public String truncaRedondea;
        public String idTipoInstrumento;
        public String idMoneda;
        public String idTipoOperacion;
        public String valorConFormato;
    }

    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<String> P_OUT_MENSAJES = new ArrayList<>();
        public List<Resultado> P_OUT_RESULTADO = new ArrayList<>();
    }

    private static String loadSql(String path) {
        try (InputStream is = FormatearMonedaFormatoService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
