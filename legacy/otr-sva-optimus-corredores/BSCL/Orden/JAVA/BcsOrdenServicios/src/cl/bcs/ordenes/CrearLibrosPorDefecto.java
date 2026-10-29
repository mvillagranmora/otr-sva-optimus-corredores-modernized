
package cl.bcs.ordenes;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CrearLibrosPorDefecto implements JavaCall {
    private static final Logger logger = LogManager.getLogger(CrearLibrosPorDefecto.class);
    private final String SQL_SELECT_EXISTE = loadResourceToString("cl/bcs/ordenes/sql/CrearLibrosPorDefecto_SelectExiste.sql");
    private final String SQL_SELECT_CERRADO = loadResourceToString("cl/bcs/ordenes/sql/CrearLibrosPorDefecto_SelectCerrado.sql");
    private final String SQL_DESMARCAR = loadResourceToString("cl/bcs/ordenes/sql/CrearLibrosPorDefecto_DesmarcarTodos.sql");
    private final String SQL_MARCAR = loadResourceToString("cl/bcs/ordenes/sql/CrearLibrosPorDefecto_MarcarSeleccionado.sql");

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;
        public Long p_IdLibroOrdenes;
    }

    public static class Contador { public Long Contador; }
    public static class ParamDesmarcar { public Long p_IdEmpresa; }
    public static class ParamMarcar { public Long p_IdEmpresa; public Long p_IdLibroOrdenes; }
    
    public static class Salida {
        public java.util.List<MensajeSalida> P_OUT_MENSAJES;
        public Long P_OUT_ERRORESCONTADOR;
        public Long p_out_RefId;

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

    public Salida logica(Connection conn, Entrada in) throws Exception {
        Salida out = new Salida();
        SQL sql = new SQL();

        // Validar que el libro existe
        HashMap<String, Object> p1 = new HashMap<>();
        p1.put("IdLibroOrdenes", in.p_IdLibroOrdenes);
        p1.put("IdEmpresa", in.p_IdEmpresa);
        List<Contador> c1 = sql.query(conn, SQL_SELECT_EXISTE, p1, Contador.class);
        long existe = (c1.isEmpty() || c1.get(0).Contador == null) ? 0L : c1.get(0).Contador;
        if (existe == 0) {
            MensajeSalida msg = new MensajeSalida();
            msg.tipo = "ERROR";
            msg.codigo = "LIBRO_ORDENES_NO_EXISTE";
            msg.descripcion = "";
            out.P_OUT_MENSAJES.add(msg);
            out.P_OUT_ERRORESCONTADOR++;
            return out;
        }

        // Validar que el libro no esté cerrado (IdEstado='INA')
        List<Contador> c2 = sql.query(conn, SQL_SELECT_CERRADO, p1, Contador.class);
        long cerrado = (c2.isEmpty() || c2.get(0).Contador == null) ? 0L : c2.get(0).Contador;
        if (cerrado > 0) {
            MensajeSalida msg = new MensajeSalida();
            msg.tipo = "ERROR";
            msg.codigo = "LIBRO_ORDENES_CERRADO";
            msg.descripcion = "";
            out.P_OUT_MENSAJES.add(msg);
            out.P_OUT_ERRORESCONTADOR++;
            return out;
        }

        // Desmarcar todos los libros por defecto de la empresa
        ParamDesmarcar paramDesmarcar = new ParamDesmarcar();
        paramDesmarcar.p_IdEmpresa = in.p_IdEmpresa;
        sql.batch(conn, SQL_DESMARCAR, java.util.Arrays.asList(paramDesmarcar), ParamDesmarcar.class);

        // Marcar el libro indicado como por defecto
        ParamMarcar paramMarcar = new ParamMarcar();
        paramMarcar.p_IdEmpresa = in.p_IdEmpresa;
        paramMarcar.p_IdLibroOrdenes = in.p_IdLibroOrdenes;
        sql.batch(conn, SQL_MARCAR, java.util.Arrays.asList(paramMarcar), ParamMarcar.class);

        out.p_out_RefId = in.p_IdLibroOrdenes;
        return out;
    }

    public static void main(String[] args) throws Exception {
        try {
            Connection conn = PlataformaUtils.getPropertiesConnection("desa01","connections.properties");
            
            String json = "{\r\n"
                    + "    \"p_IdEmpresa\": 1,\r\n"
                    + "    \"p_IdUsuario\": 100,\r\n"
                    + "    \"p_IdModulo\": \"ORD\",\r\n"
                    + "    \"p_UsaReglasNegocio\": \"N\",\r\n"
                    + "    \"p_IdLibroOrdenes\": 123\r\n"
                    + "}";

            Gson gson = new Gson();
            Entrada entrada = gson.fromJson(json, Entrada.class);

            CrearLibrosPorDefecto servicio = new CrearLibrosPorDefecto();
            System.out.println("ENTRADA: " + json);
            Salida salida = servicio.logica(conn, entrada);
            System.out.println(gson.toJson(salida));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
