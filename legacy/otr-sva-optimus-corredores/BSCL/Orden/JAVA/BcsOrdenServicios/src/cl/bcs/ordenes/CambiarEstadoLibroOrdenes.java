package cl.bcs.ordenes;

import java.sql.Connection;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class CambiarEstadoLibroOrdenes implements JavaCall {

    // --- INICIO: Atributos globales y logger ---
    private static final Logger logger = LogManager.getLogger(CambiarEstadoLibroOrdenes.class);
    private final String SQL_CHECK_EXISTE = loadResourceToString("cl/bcs/ordenes/sql/CambiarEstadoLibroOrdenes_Check_Existe.sql");
    private final String SQL_CHECK_LIBRO_DEF = loadResourceToString("cl/bcs/ordenes/sql/CambiarEstadoLibroOrdenes_Check_LibroPorDefecto.sql");
    private final String SQL_UPDATE_ACT = loadResourceToString("cl/bcs/ordenes/sql/CambiarEstadoLibroOrdenes_Update_ACT.sql");
    private final String SQL_UPDATE_INA = loadResourceToString("cl/bcs/ordenes/sql/CambiarEstadoLibroOrdenes_Update_INA.sql");
    private final String SQL_UPDATE_BORRADO = loadResourceToString("cl/bcs/ordenes/sql/CambiarEstadoLibroOrdenes_Update_BORRADO.sql");
    // --- FIN: Atributos globales y logger ---

    // POJOs para los parámetros de cada update
    public static class ParametrosUpdateACT {
        public String p_IdEstado;
        public String p_IdTipo;
        public String p_Fecha;
        public String p_Comentario;
        public Long p_IdLibroOrdenes;
        public Long p_IdEmpresa;
    }
    public static class ParametrosUpdateINA {
        public String p_IdEstado;
        public String p_IdTipo;
        public String p_Fecha;
        public String p_Comentario;
        public Long p_IdLibroOrdenes;
        public Long p_IdEmpresa;
    }
    public static class ParametrosUpdateBORRADO {
        public Long p_IdLibroOrdenes;
        public Long p_IdEmpresa;
    }

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;
        public Long p_IdLibroOrdenes;
        public String p_IdEstado;
        public String p_IdTipo;
        public String p_Fecha;
        public String p_Comentario;
    }

    public static class CountResult {
        public Long Contador;
    }

    public static class Salida {
        public java.util.List<MensajeSalida> P_OUT_MENSAJES;
        public Long P_OUT_ERRORESCONTADOR;

        public Salida() {
            this.P_OUT_MENSAJES = new java.util.ArrayList<>();
            this.P_OUT_ERRORESCONTADOR = 0L;
        }
    }

    @Override
    public JsonObject call(Connection conn, JsonObject jsonEntrada) throws Exception {
        Gson gson = Utils.getGsonBuilder();
        Entrada entrada = gson.fromJson(jsonEntrada.toString(), Entrada.class);
        Salida salida = logica(conn, entrada);
        return gson.fromJson(gson.toJson(salida), JsonObject.class);
    }

    public Salida logica(Connection conn, Entrada datosEntrada) throws Exception {
        Salida salida = new Salida();
        SQL sql = new SQL();

        Long p_IdEmpresa = datosEntrada.p_IdEmpresa;
        Long p_IdLibroOrdenes = datosEntrada.p_IdLibroOrdenes;
        String p_IdEstado = datosEntrada.p_IdEstado;
        String p_IdTipo = datosEntrada.p_IdTipo;
        String p_Fecha = datosEntrada.p_Fecha;
        String p_Comentario = datosEntrada.p_Comentario;

        // Validacion: existencia del Libro de Ordenes
        HashMap<String, Object> paramsExiste = new HashMap<>();
        paramsExiste.put("p_IdLibroOrdenes", p_IdLibroOrdenes);
        Long countLibro = sql.query(conn, SQL_CHECK_EXISTE, paramsExiste, CountResult.class)
                .stream()
                .findFirst()
                .map(r -> r.Contador)
                .orElse(0L);

        if (countLibro == null || countLibro == 0L) {
            MensajeSalida msg = new MensajeSalida();
            msg.tipo = "ERROR";
            msg.codigo = "LIBRO_ORDENES_NO_EXISTE";
            msg.descripcion = "";
            salida.P_OUT_MENSAJES.add(msg);
            salida.P_OUT_ERRORESCONTADOR++;
            return salida;
        }

        // Validacion: no eliminar LibroOrdenes marcado como LibroPorDefecto
        HashMap<String, Object> paramsDef = new HashMap<>();
        paramsDef.put("p_IdLibroOrdenes", p_IdLibroOrdenes);
        Long countDef = sql.query(conn, SQL_CHECK_LIBRO_DEF, paramsDef, CountResult.class)
                .stream()
                .findFirst()
                .map(r -> r.Contador)
                .orElse(0L);

        if (countDef != null && countDef > 0L) {
            MensajeSalida msg = new MensajeSalida();
            msg.tipo = "ERROR";
            msg.codigo = "LIBRO_ORDENES_ES_LIBRO_POR_DEFECTO";
            msg.descripcion = "";
            salida.P_OUT_MENSAJES.add(msg);
            salida.P_OUT_ERRORESCONTADOR++;
            return salida;
        }

        // Logica de negocio: actualizar estado segun p_IdEstado
        if ("ACT".equals(p_IdEstado)) {
            ParametrosUpdateACT paramsAct = new ParametrosUpdateACT();
            paramsAct.p_IdEstado = p_IdEstado;
            paramsAct.p_IdTipo = p_IdTipo;
            paramsAct.p_Fecha = p_Fecha;
            paramsAct.p_Comentario = p_Comentario;
            paramsAct.p_IdLibroOrdenes = p_IdLibroOrdenes;
            paramsAct.p_IdEmpresa = p_IdEmpresa;
            sql.batch(conn, SQL_UPDATE_ACT, java.util.Arrays.asList(paramsAct), ParametrosUpdateACT.class);
        } else if ("INA".equals(p_IdEstado)) {
            ParametrosUpdateINA paramsIna = new ParametrosUpdateINA();
            paramsIna.p_IdEstado = p_IdEstado;
            paramsIna.p_IdTipo = p_IdTipo;
            paramsIna.p_Fecha = p_Fecha;
            paramsIna.p_Comentario = p_Comentario;
            paramsIna.p_IdLibroOrdenes = p_IdLibroOrdenes;
            paramsIna.p_IdEmpresa = p_IdEmpresa;
            sql.batch(conn, SQL_UPDATE_INA, java.util.Arrays.asList(paramsIna), ParametrosUpdateINA.class);
        } else {
            ParametrosUpdateBORRADO paramsBor = new ParametrosUpdateBORRADO();
            paramsBor.p_IdLibroOrdenes = p_IdLibroOrdenes;
            paramsBor.p_IdEmpresa = p_IdEmpresa;
            sql.batch(conn, SQL_UPDATE_BORRADO, java.util.Arrays.asList(paramsBor), ParametrosUpdateBORRADO.class);
        }

        return salida;
    }

    public static void main(String[] args) throws Exception {
        try {
            Connection conn = PlataformaUtils.getPropertiesConnection("desa01","connections.properties");
            
            String json = "{\r\n"
                + "    \"p_IdEmpresa\": 1,\r\n"
                + "    \"p_IdUsuario\": 100,\r\n"
                + "    \"p_IdModulo\": \"ORD\",\r\n"
                + "    \"p_UsaReglasNegocio\": \"N\",\r\n"
                + "    \"p_IdLibroOrdenes\": 1,\r\n"
                + "    \"p_IdEstado\": \"ACT\",\r\n"
                + "    \"p_IdTipo\": \"TIPO1\",\r\n"
                + "    \"p_Fecha\": \"2026-01-21\",\r\n"
                + "    \"p_Comentario\": \"Comentario de prueba\"\r\n"
                + "}";

            Gson gson = new Gson();
            Entrada entrada = gson.fromJson(json, Entrada.class);

            CambiarEstadoLibroOrdenes servicio = new CambiarEstadoLibroOrdenes();
            System.out.println("ENTRADA: " + json);
            Salida salida = servicio.logica(conn, entrada);
            System.out.println(gson.toJson(salida));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
