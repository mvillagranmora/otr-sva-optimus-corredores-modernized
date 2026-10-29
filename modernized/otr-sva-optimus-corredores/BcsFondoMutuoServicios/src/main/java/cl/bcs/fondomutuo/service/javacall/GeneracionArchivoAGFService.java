package cl.bcs.fondomutuo.service.javacall;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class GeneracionArchivoAGFService {

    private static final Logger log = LoggerFactory.getLogger(GeneracionArchivoAGFService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelectParametros;
    private final String sqlSelectFacturaCompras;
    private final String sqlSelectFacturaVentas;
    private final String sqlSelectCompraTerminoFR;
    private final String sqlSelectVentaTerminoFR;
    private final String sqlSelectCompraPactos;
    private final String sqlSelectVentaPactos;
    private final String sqlSelectCompraSimultanea;
    private final String sqlSelectVentaSimultanea;
    private final String sqlSelectNotaCreditoCompra;
    private final String sqlSelectNotaCreditoVenta;

    public GeneracionArchivoAGFService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlSelectParametros = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/GeneracionArchivoAGF_SelectParametros.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectFacturaCompras = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/GeneracionArchivoAGF_SelectFacturaCompras.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectFacturaVentas = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/GeneracionArchivoAGF_SelectFacturaVentas.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectCompraTerminoFR = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/GeneracionArchivoAGF_SelectCompraTerminoFR.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectVentaTerminoFR = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/GeneracionArchivoAGF_SelectVentaTerminoFR.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectCompraPactos = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/GeneracionArchivoAGF_SelectCompraPactos.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectVentaPactos = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/GeneracionArchivoAGF_SelectVentaPactos.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectCompraSimultanea = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/GeneracionArchivoAGF_SelectCompraSimultanea.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectVentaSimultanea = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/GeneracionArchivoAGF_SelectVentaSimultanea.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectNotaCreditoCompra = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/GeneracionArchivoAGF_SelectNotaCreditoCompraSimultanea.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectNotaCreditoVenta = StreamUtils.copyToString(new ClassPathResource("cl/bcs/fondomutuo/sql/GeneracionArchivoAGF_SelectNotaCreditoVentaSimultanea.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    public JsonObject execute(JsonObject params) {
        log.info("GeneracionArchivoAGF");
        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;
        String fechaDesde = params.has("p_FechaDesde") ? params.get("p_FechaDesde").getAsString() : "";
        String fechaHasta = params.has("p_FechaHasta") ? params.get("p_FechaHasta").getAsString() : "";

        MapSqlParameterSource ps = new MapSqlParameterSource()
            .addValue("IdEmpresa", idEmpresa)
            .addValue("FechaDesde", fechaDesde)
            .addValue("FechaHasta", fechaHasta);

        List<Map<String, Object>> parametros = jdbc.queryForList(sqlSelectParametros, ps);
        List<Map<String, Object>> factCompras = jdbc.queryForList(sqlSelectFacturaCompras, ps);
        List<Map<String, Object>> factVentas = jdbc.queryForList(sqlSelectFacturaVentas, ps);
        List<Map<String, Object>> compraFR = jdbc.queryForList(sqlSelectCompraTerminoFR, ps);
        List<Map<String, Object>> ventaFR = jdbc.queryForList(sqlSelectVentaTerminoFR, ps);
        List<Map<String, Object>> compraPactos = jdbc.queryForList(sqlSelectCompraPactos, ps);
        List<Map<String, Object>> ventaPactos = jdbc.queryForList(sqlSelectVentaPactos, ps);
        List<Map<String, Object>> compraSimult = jdbc.queryForList(sqlSelectCompraSimultanea, ps);
        List<Map<String, Object>> ventaSimult = jdbc.queryForList(sqlSelectVentaSimultanea, ps);
        List<Map<String, Object>> ncCompra = jdbc.queryForList(sqlSelectNotaCreditoCompra, ps);
        List<Map<String, Object>> ncVenta = jdbc.queryForList(sqlSelectNotaCreditoVenta, ps);

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        JsonArray arrParam = new JsonArray();
        for (Map<String, Object> row : parametros) arrParam.add(GSON.toJsonTree(row));
        out.add("P_OUT_PARAMETROS", arrParam);
        addArray(out, "P_OUT_FACTURA_COMPRAS", factCompras);
        addArray(out, "P_OUT_FACTURA_VENTAS", factVentas);
        addArray(out, "P_OUT_COMPRA_TERMINO_FR", compraFR);
        addArray(out, "P_OUT_VENTA_TERMINO_FR", ventaFR);
        addArray(out, "P_OUT_COMPRA_PACTOS", compraPactos);
        addArray(out, "P_OUT_VENTA_PACTOS", ventaPactos);
        addArray(out, "P_OUT_COMPRA_SIMULTANEA", compraSimult);
        addArray(out, "P_OUT_VENTA_SIMULTANEA", ventaSimult);
        addArray(out, "P_OUT_NC_COMPRA_SIMULTANEA", ncCompra);
        addArray(out, "P_OUT_NC_VENTA_SIMULTANEA", ncVenta);
        return out;
    }

    private void addArray(JsonObject out, String key, List<Map<String, Object>> rows) {
        JsonArray arr = new JsonArray();
        for (Map<String, Object> row : rows) arr.add(GSON.toJsonTree(row));
        out.add(key, arr);
    }
}
