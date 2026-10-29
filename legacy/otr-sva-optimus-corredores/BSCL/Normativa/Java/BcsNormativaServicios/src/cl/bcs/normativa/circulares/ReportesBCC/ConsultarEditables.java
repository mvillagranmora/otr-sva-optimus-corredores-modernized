package cl.bcs.normativa.circulares.ReportesBCC;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.time.LocalDate;
import java.time.format.ResolverStyle;
import java.time.format.DateTimeFormatter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import datadog.trace.api.Trace;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarEditables implements JavaCall {

    static final Logger logger = LogManager.getLogger(ConsultarEditables.class);

    /**
     * Llamadas SQL
     */
    private final String SQL_SelectEditables = loadResourceToString(
            "cl/bcs/normativa/circulares/ReportesBCC/sql/Consultar_editables.sql");
            
    public static class EntradaConsultaEditables extends BcsPojo {
        public Long IdEmpresa;
        public String IdUsuario;
        public String ClaveReporte;
        public String Fecha;
    }

    public static class ResultadoConsultaEditables extends BcsPojo {
        public List<Datos> Resultado;
        public int codigoRes;
        public String mensajeRes;
    }

    public static class Datos extends BcsPojo {
        public String Nombre;
    }
    

    @Trace
    public ResultadoConsultaEditables consultaEditables(Connection con, EntradaConsultaEditables in) throws Exception {
        ResultadoConsultaEditables out = new ResultadoConsultaEditables();
        out.Resultado = new ArrayList<>();
        out.codigoRes = 200;
        SQL sql = new SQL();

        final DateTimeFormatter DF_IN  = DateTimeFormatter.ofPattern("uuuu-MM-dd")
            .withResolverStyle(ResolverStyle.STRICT);
        final DateTimeFormatter DF_VER = DateTimeFormatter.ofPattern("uuuu/MM");

        final String clave = (in.ClaveReporte == null) ? null : in.ClaveReporte.trim();
        final String fecha = (in.Fecha == null) ? null : in.Fecha.trim();
        
        if (clave == null || 
           !( "2.2".equals(clave) || "2.1".equals(clave) || "3.1".equals(clave))) {
            out.Resultado = Collections.emptyList();
            out.codigoRes = 400;
            out.mensajeRes = "ClaveReporte invalida (use 2.2, 2.1 o 3.1).";
            return out;
        }
        
        final String esquema = "REPO" + clave.replace(".", "P"); 

        final String version;
        try {
            if (fecha == null || fecha.isEmpty()) throw new IllegalArgumentException("vacía");
            LocalDate f = LocalDate.parse(fecha, DF_IN);
            version = f.format(DF_VER);
        } catch (Exception e) {
            logger.error("Fecha invalida (esperado YYYY-MM-DD): '{}'", fecha);
            out.Resultado = Collections.emptyList();
            out.codigoRes = 400;
            out.mensajeRes = "Fecha invalida, formato debe ser YYYY-MM-DD.";
            return out;
        }

        HashMap<String, Object> parametros = new HashMap<>();
        parametros.put("Esquema", esquema);
        parametros.put("Version", version);

        List<Datos> rows = sql.query(
            con,
            SQL_SelectEditables,
            parametros,
            Datos.class
        );

        if (!rows.isEmpty()) {
            logger.info("Registros encontrados: {}", rows.size());
            out.Resultado = rows;
            out.mensajeRes = "OK";
        } else {
            logger.info("No se encontraron datos editables.");
            out.Resultado = Collections.emptyList();
            out.mensajeRes = "NOOK";
        }

        return out;
    }

    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        Gson gson = new GsonBuilder().create();
        EntradaConsultaEditables entrada = gson.fromJson(in, EntradaConsultaEditables.class);
        ResultadoConsultaEditables out = consultaEditables(conn, entrada);

        JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
        return dataout;
    }

    /**
     * Metodo principal para probar la consulta de reportes.
     * 
     * @param args Argumentos de linea de comandos (no se utilizan).
     */
    public static void main(String[] args) {
        try (Connection con = PlataformaUtils.getPropertiesConnection("desa01",
                "C:\\Users\\vaguilera_ext\\Documents\\GIT_OPTIMUS\\optimus-corredores\\connections.properties")) {
            try {
                Gson gson = Utils.getGsonBuilder();
                String json = "{\n" +        		
                        "    \"ClaveReporte\": \"3.1\",\n" +
                        "    \"Fecha\": \"2025-08-31\"\n" +
                        "}";
                EntradaConsultaEditables Objin = gson.fromJson(json, EntradaConsultaEditables.class);

                ConsultarEditables service = new ConsultarEditables();
                
                service.consultaEditables(con, Objin);
                con.commit();
            } catch (Exception e) {
                logger.error("Error al consultar los datos editables.", e);
                con.rollback();
            }
        } catch (Exception e) {
            logger.error("Error al conectar a la base de datos", e);
        }
    }
}

