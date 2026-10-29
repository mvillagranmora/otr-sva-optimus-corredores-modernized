package cl.bcs.ordenes;

import java.sql.Connection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

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

public class CrearLibroOrdenes implements JavaCall {
    @Override
    public com.google.gson.JsonObject call(java.sql.Connection conn, com.google.gson.JsonObject in) throws Exception {
        com.google.gson.Gson gson = cl.bcs.utils.Utils.getGsonBuilder();
        Entrada datosEntrada = gson.fromJson(in, Entrada.class);
        return gson.toJsonTree(logica(conn, datosEntrada)).getAsJsonObject();
    }

    private static final Logger logger = LogManager.getLogger(CrearLibroOrdenes.class);

    private final String SQL_CHECK = loadResourceToString("cl/bcs/ordenes/sql/CrearLibroOrdenes_CheckExiste.sql");
    private final String SQL_INSERT = loadResourceToString("cl/bcs/ordenes/sql/CrearLibroOrdenes_Insert.sql");
    // Clases POJO después de recursos
    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;
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

    public static class CountResult {
        public Long Contador;
    }

    public static class InsertParams {
        public Long p_out_RefId;
        public Long p_IdEmpresa;
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



    public Salida logica(Connection conn, Entrada datosEntrada) throws Exception {
        Salida out = new Salida();
        SQL sql = new SQL();
        Long p_IdEmpresa = datosEntrada.p_IdEmpresa;
        String p_NombreLibroOrdenes = datosEntrada.p_NombreLibroOrdenes;
        String p_IdTipoLibroOrden = datosEntrada.p_IdTipoLibroOrden;
        String p_IdEstado = datosEntrada.p_IdEstado;
        String p_AsignacionAutomatica = datosEntrada.p_AsignacionAutomatica;
        String p_IdTipoAsignacion = datosEntrada.p_IdTipoAsignacion;
        Long p_Prioridad = datosEntrada.p_Prioridad;
        java.math.BigDecimal p_Precio = datosEntrada.p_Precio;
        String p_Bolsa = datosEntrada.p_Bolsa;
        String p_TipoSubasta = datosEntrada.p_TipoSubasta;
        Long p_IdInstrumento = datosEntrada.p_IdInstrumento;
        Long p_IdClienteOfertante = datosEntrada.p_IdClienteOfertante;
        java.math.BigDecimal p_Cantidad = datosEntrada.p_Cantidad;
        String p_IdPlantillaCobros = datosEntrada.p_IdPlantillaCobros;
        String p_FechaApertura = datosEntrada.p_FechaApertura;
        String p_FechaCierre = datosEntrada.p_FechaCierre;

        HashMap<String, Object> paramsCheck = new HashMap<>();
        paramsCheck.put("p_IdEmpresa", p_IdEmpresa);
        paramsCheck.put("p_NombreLibroOrdenes", p_NombreLibroOrdenes);
        List<CountResult> result  = sql.query(conn, SQL_CHECK, paramsCheck, CountResult.class);
        
        if (result.get(0).Contador != null && result.get(0).Contador > 0) {
            MensajeSalida msg = new MensajeSalida();
            msg.tipo = "ERROR";
            msg.codigo = "LIBRO_ORDENES_YA_EXISTE";
            msg.descripcion = "";
            out.P_OUT_MENSAJES.add(msg);
            out.P_OUT_ERRORESCONTADOR++;
            return out;
        }

        LinkedList<Long> secuenciaLibro = new LinkedList<Long>(
            Utils.getSecuencia(conn, "BSCL.Orden.BD.Secuencias::LibroOrdenes", 1L));
        Long nuevoId = secuenciaLibro.peek();

        InsertParams ins = new InsertParams();
        ins.p_out_RefId = nuevoId;
        ins.p_IdEmpresa = p_IdEmpresa;
        ins.p_NombreLibroOrdenes = p_NombreLibroOrdenes;
        ins.p_IdTipoLibroOrden = p_IdTipoLibroOrden;
        ins.p_IdEstado = p_IdEstado;
        ins.p_AsignacionAutomatica = p_AsignacionAutomatica;
        ins.p_IdTipoAsignacion = p_IdTipoAsignacion;
        ins.p_Prioridad = p_Prioridad != null ? p_Prioridad : 0L;
        ins.p_IdInstrumento = p_IdInstrumento;
        ins.p_IdClienteOfertante = p_IdClienteOfertante;
        ins.p_Cantidad = p_Cantidad;
        ins.p_IdPlantillaCobros = p_IdPlantillaCobros;
        ins.p_FechaApertura = p_FechaApertura;
        ins.p_FechaCierre = p_FechaCierre;
        ins.p_Precio = p_Precio;
        ins.p_Bolsa = p_Bolsa;
        ins.p_TipoSubasta = p_TipoSubasta;

        sql.batch(conn, SQL_INSERT, Arrays.asList(ins), InsertParams.class);

        out.p_out_RefId = nuevoId;
        
        return out;
    }

    public static void main(String[] args) throws Exception {
    	try (Connection conn = cl.bcs.plataforma.pla.utils.PlataformaUtils.getPropertiesConnection("desa01","connections.properties")) {
            Gson gson = Utils.getGsonBuilder();

            String json = "{\r\n"
            		+ "    \"p_IdEmpresa\": 1,\r\n"
            		+ "    \"p_IdUsuario\": 76767,\r\n"
            		+ "    \"p_IdModulo\": \"ORD\",\r\n"
            		+ "    \"p_UsaReglasNegocio\": \"N\",\r\n"
            		+ "    \"p_NombreLibroOrdenes\": \"PRUEBA\",\r\n"
            		+ "    \"p_IdTipoLibroOrden\": \"NORMAL\",\r\n"
            		+ "    \"p_IdEstado\": \"ABT\",\r\n"
            		+ "    \"p_AsignacionAutomatica\": \"S\",\r\n"
            		+ "    \"p_IdTipoAsignacion\": \"ASIGNACION_PROPORCIONAL\",\r\n"
            		+ "    \"p_Bolsa\": \"BCS\",\r\n"
            		+ "    \"p_TipoSubasta\": \"\",\r\n"
            		+ "    \"p_IdInstrumento\": null,\r\n"
            		+ "    \"p_IdClienteOfertante\": null,\r\n"
            		+ "    \"p_Cantidad\": null,\r\n"
            		+ "    \"p_IdPlantillaCobros\": null,\r\n"
            		+ "    \"p_FechaApertura\": null,\r\n"
            		+ "    \"p_FechaCierre\": null\r\n"
            		+ "}";

            Entrada entrada = gson.fromJson(json, Entrada.class);
            CrearLibroOrdenes demo = new CrearLibroOrdenes();
            Salida out = demo.logica(conn, entrada);
            System.out.println(gson.toJson(out));
            conn.rollback();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
