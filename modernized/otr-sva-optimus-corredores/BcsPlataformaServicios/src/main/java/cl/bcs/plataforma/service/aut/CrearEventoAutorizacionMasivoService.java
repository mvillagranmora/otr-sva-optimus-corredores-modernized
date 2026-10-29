package cl.bcs.plataforma.service.aut;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CrearEventoAutorizacionMasivoService implements JavaCallService {

    private final NamedParameterJdbcTemplate jdbc;

    public CrearEventoAutorizacionMasivoService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo"; }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        try {
            // TODO: requires pla.* utility — complete after pla subpackage migration
            // Delegates to SAP HANA stored procedure:
            //   CALL "BSCL.Plataforma.BD.Servicios::AUT.CrearEventoAutorizacionMasivo" (...)
            // Creates two local temporary tables:
            //   #CrearEventoAutorizacionMasivo_P_TABLAEVENTOS
            //   #CrearEventoAutorizacionMasivo_P_TABLADETALLE
            // populated from P_TABLAEVENTOS and P_TABLADETALLE input arrays, then
            // the SP is called; tables are dropped in a finally block.
            // Full implementation requires StoredProcedure support via JDBC CallableStatement
            // with HANA-specific output table handling.

            Long idEmpresa = params.has("P_IDEMPRESA") && !params.get("P_IDEMPRESA").isJsonNull() ? params.get("P_IDEMPRESA").getAsLong() : null;

            try {
                jdbc.getJdbcTemplate().execute(
                        "CREATE LOCAL TEMPORARY TABLE #CrearEventoAutorizacionMasivo_P_TABLAEVENTOS (" +
                        "\"IdTipoEvento\" NVARCHAR(6),\"IdModuloSolicita\" NVARCHAR(3),\"IdUsuarioSolicita\" BIGINT," +
                        "\"Fecha\" NVARCHAR(10),\"IdTipoOperacion\" NVARCHAR(6),\"IdConcepto\" NVARCHAR(6)," +
                        "\"NumeroOperacionConcepto\" NVARCHAR(40),\"FechaOperacionConcepto\" NVARCHAR(10)," +
                        "\"IdCliente\" BIGINT,\"IdFuncionario\" BIGINT,\"IdProveedor\" BIGINT," +
                        "\"IdTipoProducto\" NVARCHAR(4),\"IdClasificacionTipoInstrumento\" NVARCHAR(2)," +
                        "\"IdTipoInstrumento\" NVARCHAR(3),\"IdInstrumento\" BIGINT,\"Nemo\" NVARCHAR(40)," +
                        "\"Cantidad\" BIGINT,\"Monto\" DECIMAL(26,8),\"Precio\" DECIMAL(26,8)," +
                        "\"Tasa\" DECIMAL(26,8),\"IdMoneda\" NVARCHAR(3),\"Glosa\" NVARCHAR(500),\"IdUnion\" BIGINT)");
                jdbc.getJdbcTemplate().execute(
                        "CREATE LOCAL TEMPORARY TABLE #CrearEventoAutorizacionMasivo_P_TABLADETALLE (" +
                        "\"IdUnion\" BIGINT,\"NombreCampo\" NVARCHAR(100),\"TipoDato\" NVARCHAR(50)," +
                        "\"Valor\" NVARCHAR(300),\"Clasificacion\" NVARCHAR(50))");

                if (params.has("P_TABLAEVENTOS") && params.get("P_TABLAEVENTOS").isJsonArray()) {
                    for (var el : params.getAsJsonArray("P_TABLAEVENTOS")) {
                        JsonObject ev = el.getAsJsonObject();
                        MapSqlParameterSource ps = new MapSqlParameterSource()
                                .addValue("IdTipoEvento", ev.has("IdTipoEvento") && !ev.get("IdTipoEvento").isJsonNull() ? ev.get("IdTipoEvento").getAsString() : null)
                                .addValue("IdModuloSolicita", ev.has("IdModuloSolicita") && !ev.get("IdModuloSolicita").isJsonNull() ? ev.get("IdModuloSolicita").getAsString() : null)
                                .addValue("IdUsuarioSolicita", ev.has("IdUsuarioSolicita") && !ev.get("IdUsuarioSolicita").isJsonNull() ? ev.get("IdUsuarioSolicita").getAsLong() : null)
                                .addValue("Fecha", ev.has("Fecha") && !ev.get("Fecha").isJsonNull() ? ev.get("Fecha").getAsString() : null)
                                .addValue("IdTipoOperacion", ev.has("IdTipoOperacion") && !ev.get("IdTipoOperacion").isJsonNull() ? ev.get("IdTipoOperacion").getAsString() : null)
                                .addValue("IdConcepto", ev.has("IdConcepto") && !ev.get("IdConcepto").isJsonNull() ? ev.get("IdConcepto").getAsString() : null)
                                .addValue("NumeroOperacionConcepto", ev.has("NumeroOperacionConcepto") && !ev.get("NumeroOperacionConcepto").isJsonNull() ? ev.get("NumeroOperacionConcepto").getAsString() : null)
                                .addValue("FechaOperacionConcepto", ev.has("FechaOperacionConcepto") && !ev.get("FechaOperacionConcepto").isJsonNull() ? ev.get("FechaOperacionConcepto").getAsString() : null)
                                .addValue("IdCliente", ev.has("IdCliente") && !ev.get("IdCliente").isJsonNull() ? ev.get("IdCliente").getAsLong() : 0L)
                                .addValue("IdFuncionario", ev.has("IdFuncionario") && !ev.get("IdFuncionario").isJsonNull() ? ev.get("IdFuncionario").getAsLong() : 0L)
                                .addValue("IdProveedor", ev.has("IdProveedor") && !ev.get("IdProveedor").isJsonNull() ? ev.get("IdProveedor").getAsLong() : 0L)
                                .addValue("IdTipoProducto", ev.has("IdTipoProducto") && !ev.get("IdTipoProducto").isJsonNull() ? ev.get("IdTipoProducto").getAsString() : null)
                                .addValue("IdClasificacionTipoInstrumento", ev.has("IdClasificacionTipoInstrumento") && !ev.get("IdClasificacionTipoInstrumento").isJsonNull() ? ev.get("IdClasificacionTipoInstrumento").getAsString() : null)
                                .addValue("IdTipoInstrumento", ev.has("IdTipoInstrumento") && !ev.get("IdTipoInstrumento").isJsonNull() ? ev.get("IdTipoInstrumento").getAsString() : null)
                                .addValue("IdInstrumento", ev.has("IdInstrumento") && !ev.get("IdInstrumento").isJsonNull() ? ev.get("IdInstrumento").getAsLong() : 0L)
                                .addValue("Nemo", ev.has("Nemo") && !ev.get("Nemo").isJsonNull() ? ev.get("Nemo").getAsString() : null)
                                .addValue("Cantidad", ev.has("Cantidad") && !ev.get("Cantidad").isJsonNull() ? ev.get("Cantidad").getAsLong() : 0L)
                                .addValue("Monto", ev.has("Monto") && !ev.get("Monto").isJsonNull() ? ev.get("Monto").getAsBigDecimal() : BigDecimal.ZERO)
                                .addValue("Precio", ev.has("Precio") && !ev.get("Precio").isJsonNull() ? ev.get("Precio").getAsBigDecimal() : BigDecimal.ZERO)
                                .addValue("Tasa", ev.has("Tasa") && !ev.get("Tasa").isJsonNull() ? ev.get("Tasa").getAsBigDecimal() : BigDecimal.ZERO)
                                .addValue("IdMoneda", ev.has("IdMoneda") && !ev.get("IdMoneda").isJsonNull() ? ev.get("IdMoneda").getAsString() : null)
                                .addValue("Glosa", ev.has("Glosa") && !ev.get("Glosa").isJsonNull() ? ev.get("Glosa").getAsString() : null)
                                .addValue("IdUnion", ev.has("IdUnion") && !ev.get("IdUnion").isJsonNull() ? ev.get("IdUnion").getAsLong() : 0L);
                        jdbc.update(
                                "INSERT INTO #CrearEventoAutorizacionMasivo_P_TABLAEVENTOS " +
                                "(\"IdTipoEvento\",\"IdModuloSolicita\",\"IdUsuarioSolicita\",\"Fecha\",\"IdTipoOperacion\"," +
                                "\"IdConcepto\",\"NumeroOperacionConcepto\",\"FechaOperacionConcepto\",\"IdCliente\"," +
                                "\"IdFuncionario\",\"IdProveedor\",\"IdTipoProducto\",\"IdClasificacionTipoInstrumento\"," +
                                "\"IdTipoInstrumento\",\"IdInstrumento\",\"Nemo\",\"Cantidad\",\"Monto\",\"Precio\"," +
                                "\"Tasa\",\"IdMoneda\",\"Glosa\",\"IdUnion\") VALUES " +
                                "(:IdTipoEvento,:IdModuloSolicita,:IdUsuarioSolicita,:Fecha,:IdTipoOperacion," +
                                ":IdConcepto,:NumeroOperacionConcepto,:FechaOperacionConcepto,:IdCliente," +
                                ":IdFuncionario,:IdProveedor,:IdTipoProducto,:IdClasificacionTipoInstrumento," +
                                ":IdTipoInstrumento,:IdInstrumento,:Nemo,:Cantidad,:Monto,:Precio," +
                                ":Tasa,:IdMoneda,:Glosa,:IdUnion)", ps);
                    }
                }

                // TODO: requires pla.* utility — complete after pla subpackage migration
                // Invoke SP: CALL "BSCL.Plataforma.BD.Servicios::AUT.CrearEventoAutorizacionMasivo" via CallableStatement
                result.add("P_OUT_IDEVENTO", new JsonArray());
                result.addProperty("P_OUT_ADVERTENCIASCONTADOR", 0L);
                result.add("P_OUT_ADVERTENCIASDETALLE", new JsonArray());
                result.addProperty("P_OUT_ERRORESCONTADOR", 0L);
                result.add("P_OUT_ERRORESDETALLE", new JsonArray());
            } finally {
                try { jdbc.getJdbcTemplate().execute("DROP TABLE #CrearEventoAutorizacionMasivo_P_TABLAEVENTOS"); } catch (Exception ignore) {}
                try { jdbc.getJdbcTemplate().execute("DROP TABLE #CrearEventoAutorizacionMasivo_P_TABLADETALLE"); } catch (Exception ignore) {}
            }
        } catch (Exception e) {
            result.addProperty("P_OUT_ERRORESCONTADOR", 1L);
            JsonArray errs = new JsonArray(); JsonObject err = new JsonObject(); err.addProperty("Codigo", "ERROR"); err.addProperty("Descripcion", e.getMessage()); errs.add(err); result.add("P_OUT_ERRORESDETALLE", errs);
        }
        return result;
    }
}
