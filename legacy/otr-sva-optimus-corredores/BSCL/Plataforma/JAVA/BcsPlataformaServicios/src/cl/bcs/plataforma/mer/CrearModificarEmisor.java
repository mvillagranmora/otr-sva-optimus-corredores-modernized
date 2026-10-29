package cl.bcs.plataforma.mer;

import java.sql.Connection;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearModificarEmisor implements JavaCall {
    private static final Logger logger = LogManager.getLogger(CrearModificarEmisor.class);
    private final String SQL_EXISTE = loadResourceToString("cl/bcs/plataforma/mer/sql/CrearModificarEmisor_Select_Existe.sql");
    private final String SQL_INSERT = loadResourceToString("cl/bcs/plataforma/mer/sql/CrearModificarEmisor_Insert.sql");
    private final String SQL_UPDATE = loadResourceToString("cl/bcs/plataforma/mer/sql/CrearModificarEmisor_Update.sql");
        // Clase interna para resultado de existencia
        public static class ExisteResult {
            public Long Contador;
        }

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_simularOperacion;
        public String p_UsaReglasNegocio;
        public Long p_IdPersona;
        public String p_IdEmisor;
        public String p_CodigoEmisor;
        public String p_CodigoRegulador;
        public String p_Regulador;
        public String p_EmiteIIF;
        public String p_EmiteIRF;
        public String p_EmiteFungible;
        public String p_ValorClasificacionRiesgoIIF;
        public String p_IndicadorAGF;
        public String p_IdEstado;
        public String p_IdEntidadClasificadoraRiesgo;
        public String p_Modo = "MODIFICAR";
        public Short p_TipoEntidad;
    }

    public static class Salida {
        public Integer P_OUT_ADVERTENCIASCONTADOR;
        public List<AdvertenciaDetalle> P_OUT_ADVERTENCIASDETALLE;
        public Integer P_OUT_ERRORESCONTADOR;
        public List<ErrorDetalle> P_OUT_ERRORESDETALLE;
        
        public Salida() {
            this.P_OUT_ADVERTENCIASCONTADOR = 0;
            this.P_OUT_ADVERTENCIASDETALLE = new java.util.ArrayList<>();
            this.P_OUT_ERRORESCONTADOR = 0;
            this.P_OUT_ERRORESDETALLE = new java.util.ArrayList<>();
        }
    }

    public static class AdvertenciaDetalle {
        public Long IdInterno;
        public String Codigo;
        public String RequiereAutorizacion;
        public String Descripcion;
    }

    public static class ErrorDetalle {
        public Long IdInterno;
        public String Codigo;
        public String Descripcion;
    }

    public Salida logica(Connection conn, Entrada entrada) throws Exception {
        Salida salida = new Salida();
        SQL sql = new SQL();
        
        if ("INGRESAR".equalsIgnoreCase(entrada.p_Modo)) {
            java.util.HashMap<String, Object> paramsExiste = new java.util.HashMap<>();
            paramsExiste.put("p_IdEmisor", entrada.p_IdEmisor);
            List<ExisteResult> existe = sql.query(conn, SQL_EXISTE, paramsExiste, ExisteResult.class);
            Long count = existe.size() > 0 ? existe.get(0).Contador : 0L;
            
            if (count == 0) {
                // Emisor no existe, se puede insertar
                sql.batch(conn, SQL_INSERT, java.util.Arrays.asList(entrada), Entrada.class);
            } else {
                // Emisor ya existe, error
                ErrorDetalle error = new ErrorDetalle();
                error.IdInterno = 0L;
                error.Codigo = "UI_EMISOR_YA_AGREGADO";
                error.Descripcion = "Emisor ya agregado";
                salida.P_OUT_ERRORESDETALLE.add(error);
                salida.P_OUT_ERRORESCONTADOR++;
                return salida;
            }
        } else {
            sql.batch(conn, SQL_UPDATE, java.util.Arrays.asList(entrada), Entrada.class);
        }
        return salida;
    }

    @Override
    public JsonObject call(Connection conn, JsonObject jin) throws Exception {
        Gson gson = Utils.getGsonBuilder();
        Entrada entrada = gson.fromJson(jin.toString(), Entrada.class);
        Salida salida = logica(conn, entrada);
        return gson.fromJson(gson.toJson(salida), JsonObject.class);
    }

    public static void main(String[] args) throws Exception {
        try (Connection conn = cl.bcs.plataforma.pla.utils.PlataformaUtils.getPropertiesConnection("desa01","connections.properties")) {
            Gson gson = Utils.getGsonBuilder();
            String json = "{\r\n"
            		+ "    \"p_Modo\": \"INGRESAR\",\r\n"
            		+ "    \"p_IdEmpresa\": 1,\r\n"
            		+ "    \"p_IdUsuario\": 76767,\r\n"
            		+ "    \"p_IdModulo\": \"PLA\",\r\n"
            		+ "    \"p_simularOperacion\": \"N\",\r\n"
            		+ "    \"p_UsaReglasNegocio\": \"N\",\r\n"
            		+ "    \"p_IdPersona\": 27333,\r\n"
            		+ "    \"p_IdEmisor\": \"AFPC\",\r\n"
            		+ "    \"p_CodigoEmisor\": \"69\",\r\n"
            		+ "    \"p_CodigoRegulador\": \"AFPCA\",\r\n"
            		+ "    \"p_Regulador\": \"SVS\",\r\n"
            		+ "    \"p_EmiteIIF\": \"N\",\r\n"
            		+ "    \"p_EmiteIRF\": \"S\",\r\n"
            		+ "    \"p_EmiteFungible\": \"N\",\r\n"
            		+ "    \"p_ValorClasificacionRiesgoIIF\": \"\",\r\n"
            		+ "    \"p_IndicadorAGF\": \"N\",\r\n"
            		+ "    \"p_IdEstado\": \"ACT\",\r\n"
            		+ "    \"p_IdEntidadClasificadoraRiesgo\": \"\",\r\n"
            		+ "    \"p_TipoEntidad\": 3\r\n"
            		+ "}";
            
            System.out.println("ENTRADA: " + json);
            Entrada entrada = gson.fromJson(json, Entrada.class);
            CrearModificarEmisor servicio = new CrearModificarEmisor();
            Salida salida = servicio.logica(conn, entrada);
            System.out.println(gson.toJson(salida));
            conn.rollback();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
