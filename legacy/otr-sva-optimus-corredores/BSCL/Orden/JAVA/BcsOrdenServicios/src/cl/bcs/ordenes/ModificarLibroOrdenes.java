
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

public class ModificarLibroOrdenes implements JavaCall {
    private static final Logger logger = LogManager.getLogger(ModificarLibroOrdenes.class);

    private final String SQL_EXISTE = loadResourceToString("cl/bcs/ordenes/sql/ModificarLibroOrdenes_CheckExiste.sql");
    private final String SQL_DEF = loadResourceToString("cl/bcs/ordenes/sql/ModificarLibroOrdenes_CheckPorDefecto.sql");
    private final String SQL_UPD = loadResourceToString("cl/bcs/ordenes/sql/ModificarLibroOrdenes_Update.sql");

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdLibroOrdenes;
        public String p_NombreLibroOrdenes;
        public String p_IdTipoLibroOrden;
        public String p_IdEstado;
        public String p_AsignacionAutomatica;
        public String p_IdTipoAsignacion;
        public Long p_Prioridad;
        public java.math.BigDecimal p_Precio;
        public String p_Bolsa;
        public String p_TipoSubasta;
        public Long p_IdInstrumento;
        public Long p_IdClienteOfertante;
        public java.math.BigDecimal p_Cantidad;
        public String p_IdPlantillaCobros;
        public String p_FechaApertura;
        public String p_FechaCierre;
    }

    public static class CountResult { public Long Contador; }
    public static class ParametrosUpdate {
        public String p_NombreLibroOrdenes;
        public String p_IdTipoLibroOrden;
        public String p_IdEstado;
        public String p_AsignacionAutomatica;
        public String p_IdTipoAsignacion;
        public Long p_Prioridad;
        public Long p_IdInstrumento;
        public Long p_IdClienteOfertante;
        public java.math.BigDecimal p_Cantidad;
        public String p_IdPlantillaCobros;
        public String p_FechaApertura;
        public String p_FechaCierre;
        public java.math.BigDecimal p_Precio;
        public String p_Bolsa;
        public String p_TipoSubasta;
        public Long p_IdLibroOrdenes;
    }
    
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

    public Salida logica(Connection conn, Entrada datosEntrada) throws Exception {
        Salida out = new Salida();
        SQL sql = new SQL();
        Long p_IdEmpresa = datosEntrada.p_IdEmpresa;
        Long p_IdLibroOrdenes = datosEntrada.p_IdLibroOrdenes;
        String p_NombreLibroOrdenes = datosEntrada.p_NombreLibroOrdenes;
        String p_IdTipoLibroOrden = datosEntrada.p_IdTipoLibroOrden;
        String p_IdEstado = datosEntrada.p_IdEstado;
        String p_AsignacionAutomatica = datosEntrada.p_AsignacionAutomatica;
        String p_IdTipoAsignacion = (datosEntrada.p_IdTipoAsignacion != null && !datosEntrada.p_IdTipoAsignacion.trim().isEmpty()) ? datosEntrada.p_IdTipoAsignacion : null;
        Long p_Prioridad = datosEntrada.p_Prioridad;
        java.math.BigDecimal p_Precio = datosEntrada.p_Precio;
        String p_Bolsa = (datosEntrada.p_Bolsa != null && !datosEntrada.p_Bolsa.trim().isEmpty()) ? datosEntrada.p_Bolsa : null;
        String p_TipoSubasta = (datosEntrada.p_TipoSubasta != null && !datosEntrada.p_TipoSubasta.trim().isEmpty()) ? datosEntrada.p_TipoSubasta : null;
        Long p_IdInstrumento = datosEntrada.p_IdInstrumento;
        Long p_IdClienteOfertante = datosEntrada.p_IdClienteOfertante;
        java.math.BigDecimal p_Cantidad = datosEntrada.p_Cantidad;
        String p_IdPlantillaCobros = (datosEntrada.p_IdPlantillaCobros != null && !datosEntrada.p_IdPlantillaCobros.trim().isEmpty()) ? datosEntrada.p_IdPlantillaCobros : null;
        String p_FechaApertura = (datosEntrada.p_FechaApertura != null && !datosEntrada.p_FechaApertura.trim().isEmpty()) ? datosEntrada.p_FechaApertura : null;
        String p_FechaCierre = (datosEntrada.p_FechaCierre != null && !datosEntrada.p_FechaCierre.trim().isEmpty()) ? datosEntrada.p_FechaCierre : null;

        // Validación 1: Verifica que exista Libro de Ordenes
        HashMap<String, Object> p0 = new HashMap<>();
        p0.put("p_IdLibroOrdenes", p_IdLibroOrdenes);
        Long existe = sql.query(conn, SQL_EXISTE, p0, CountResult.class).stream().findFirst().map(r -> r.Contador).orElse(0L);
        if (existe == null || existe == 0L) {
            MensajeSalida msg = new MensajeSalida();
            msg.tipo = "ERROR";
            msg.codigo = "LIBRO_ORDENES_NO_EXISTE";
            msg.descripcion = "";
            out.P_OUT_MENSAJES.add(msg);
            out.P_OUT_ERRORESCONTADOR++;
            return out;
        }

        // Validación 2: Libro por defecto no se puede cerrar
        HashMap<String, Object> p1 = new HashMap<>();
        p1.put("p_IdEmpresa", p_IdEmpresa);
        p1.put("p_IdLibroOrdenes", p_IdLibroOrdenes);
        Long esPorDefecto = sql.query(conn, SQL_DEF, p1, CountResult.class).stream().findFirst().map(r -> r.Contador).orElse(0L);
        if (esPorDefecto != null && esPorDefecto > 0) {
            MensajeSalida msg = new MensajeSalida();
            msg.tipo = "ERROR";
            msg.codigo = "LIBRO_ORDENES_POR_DEFECTO_NO_SE_PUEDE_CERRAR";
            msg.descripcion = "";
            out.P_OUT_MENSAJES.add(msg);
            out.P_OUT_ERRORESCONTADOR++;
            return out;
        }

        // Update de Libro de Ordenes
        ParametrosUpdate p = new ParametrosUpdate();
        p.p_NombreLibroOrdenes = p_NombreLibroOrdenes;
        p.p_IdTipoLibroOrden = p_IdTipoLibroOrden;
        p.p_IdEstado = p_IdEstado;
        p.p_AsignacionAutomatica = p_AsignacionAutomatica;
        p.p_IdTipoAsignacion = p_IdTipoAsignacion;
        p.p_Prioridad = p_Prioridad;
        p.p_IdInstrumento = p_IdInstrumento;
        p.p_IdClienteOfertante = p_IdClienteOfertante;
        p.p_Cantidad = p_Cantidad;
        p.p_IdPlantillaCobros = p_IdPlantillaCobros;
        p.p_FechaApertura = p_FechaApertura;
        p.p_FechaCierre = p_FechaCierre;
        p.p_Precio = p_Precio;
        p.p_Bolsa = p_Bolsa;
        p.p_TipoSubasta = p_TipoSubasta;
        p.p_IdLibroOrdenes = p_IdLibroOrdenes;

        sql.batch(conn, SQL_UPD, java.util.Arrays.asList(p), ParametrosUpdate.class);

        out.p_out_RefId = p_IdLibroOrdenes;
        return out;
    }


    public static void main(String[] args) throws Exception {
        try {
            Connection conn = PlataformaUtils.getPropertiesConnection("desa01","connections.properties");
            
            String json = "{\r\n"
                    + "    \"p_IdEmpresa\": 1,\r\n"
                    + "    \"p_IdLibroOrdenes\": 0,\r\n"
                    + "    \"p_NombreLibroOrdenes\": \"LIBRO_DEMO_MODIFICAR\",\r\n" 
                    + "    \"p_IdTipoLibroOrden\": \"RF\",\r\n" 
                    + "    \"p_IdEstado\": \"ACT\",\r\n" 
                    + "    \"p_AsignacionAutomatica\": \"N\",\r\n" 
                    + "    \"p_IdTipoAsignacion\": \"\",\r\n" 
                    + "    \"p_Prioridad\": 0,\r\n" 
                    + "    \"p_Precio\": 0,\r\n" 
                    + "    \"p_Bolsa\": \"\",\r\n" 
                    + "    \"p_TipoSubasta\": \"\",\r\n" 
                    + "    \"p_IdInstrumento\": 0,\r\n" 
                    + "    \"p_IdClienteOfertante\": 0,\r\n" 
                    + "    \"p_Cantidad\": 0,\r\n" 
                    + "    \"p_IdPlantillaCobros\": \"\",\r\n" 
                    + "    \"p_FechaApertura\": \"0001-01-01\",\r\n" 
                    + "    \"p_FechaCierre\": \"0001-01-01\"\r\n" 
                    + "}";

            Gson gson = new Gson();
            Entrada entrada = gson.fromJson(json, Entrada.class);

            ModificarLibroOrdenes servicio = new ModificarLibroOrdenes();
            System.out.println("ENTRADA: " + json);
            Salida salida = servicio.logica(conn, entrada);
            System.out.println(gson.toJson(salida));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

