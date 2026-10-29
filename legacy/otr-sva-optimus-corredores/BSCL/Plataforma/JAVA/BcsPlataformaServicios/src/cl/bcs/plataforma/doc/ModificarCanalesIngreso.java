package cl.bcs.plataforma.doc;


import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarCanalesIngreso implements JavaCall {
    static final Logger logger = LogManager.getLogger(ModificarCanalesIngreso.class);
    
    private final String SQL_Insert_CanalesIngreso = loadResourceToString("cl/bcs/plataforma/doc/sql/ModificarCanalesIngreso_InsertCanalesIngreso.sql");
    private final String SQL_Update_CanalesIngreso = loadResourceToString("cl/bcs/plataforma/doc/sql/ModificarCanalesIngreso_UpdateCanalesIngreso.sql");
    private final String SQL_Delete_CanalesIngreso = loadResourceToString("cl/bcs/plataforma/doc/sql/ModificarCanalesIngreso_DeleteCanalesIngreso.sql");

    public static class EntradaModificarCanalesIngreso  {
        public Long 	p_IdEmpresa;
        public Long 	p_IdUsuario;
        public String 	p_IdModulo ;
        public String   p_IdTipoDocumento;
        
        public List<EntidadCanaldeIngreso> p_CanalesDeIngreso;

        public EntradaModificarCanalesIngreso() {
        	p_CanalesDeIngreso = new ArrayList<>();
        }           
    }
    
    public  class EntidadCanaldeIngreso {
    	  	
    	public Long   Codigo ;
    	public String IdEstado ;
    	public String IdTipoCanalIngreso ;
    	public String Nombre ;
    	public String PermiteEnrolamiento ;
    	public String PermiteFirmaDocumento ;
    	
    }
    
    public  class EntidadCanalesDocumento {
    
    	public String IdTipoCanalIngreso ;
    	public String IdTipoDocumento ;
   	
    }
    
    public static class SalidaModificarCanalesIngreso{
        public Long P_OUT_ERRORESCONTADOR;
        public List<PlataformaUtils.MensajeSalida> P_OUT_MENSAJES;
        
        public SalidaModificarCanalesIngreso() {
            P_OUT_MENSAJES = new ArrayList<>();
            P_OUT_ERRORESCONTADOR = 0L;
        }
    }
    
    private void InsertCanalesIngreso(Connection conn, List<EntidadCanaldeIngreso> CanalesDeIngreso ,String  IdTipoDocumento )  throws SQLException {
    	logger.info("INICIO::ModificarCanalesIngreso.InsertCanalesIngreso");
        SQL sql = new SQL();

        if (CanalesDeIngreso.size() > 0 ) {
        List<EntidadCanalesDocumento> datos = new ArrayList<>();

        for ( EntidadCanaldeIngreso canal : CanalesDeIngreso){
        	EntidadCanalesDocumento dato = new EntidadCanalesDocumento();
                
            dato.IdTipoCanalIngreso = canal.IdTipoCanalIngreso;
            dato.IdTipoDocumento    = IdTipoDocumento;          
                        
            datos.add(dato);
                
        	logger.info("Eliminando");
            sql.batch(conn, SQL_Delete_CanalesIngreso, datos, EntidadCanalesDocumento.class);
            logger.info("Eliminando");
            
           	logger.info("Insertando");
                sql.batch(conn, SQL_Insert_CanalesIngreso, datos, EntidadCanalesDocumento.class);
             logger.info("Insertando");
                
           
            
        }
        }

		logger.info("FIN::ModificarCanalesIngreso.InsertCanalesIngres");
    }
    
    
    
    private SalidaModificarCanalesIngreso logica(Connection conn, EntradaModificarCanalesIngreso entrada) throws Exception {
        logger.info("INICIO::AgregarPlantillaContratoDocumento");
        long inicio = System.currentTimeMillis();

        
        SalidaModificarCanalesIngreso salida = new SalidaModificarCanalesIngreso();

        
        if(entrada.p_IdTipoDocumento != null) {
        	InsertCanalesIngreso(conn, entrada.p_CanalesDeIngreso, entrada.p_IdTipoDocumento);
        	salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"INSERCION_EXITOSA",new HashMap<String,String>(),conn,entrada.p_IdEmpresa));
        }else {
        	salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"INSERCION_NO_EXITOSA",new HashMap<String,String>(),conn,entrada.p_IdEmpresa));
        }

        return salida;
    }

    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        Gson gson = Utils.getGsonBuilder();
        EntradaModificarCanalesIngreso entrada = gson.fromJson(in, EntradaModificarCanalesIngreso.class);
    
        SalidaModificarCanalesIngreso salida = logica(conn, entrada);
        JsonParser jsonParser = new JsonParser();

        return jsonParser.parse(gson.toJson(salida)).getAsJsonObject();
    }


    public static void main(String[] args) {
        System.out.println("== INICIO TEST ==");

     
        String jsonRequest= "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"DOC\",\"p_UsaReglasNegocio\":\"N\",\"p_IdTipoDocumento\":\"WWWE\",\"p_CanalesDeIngreso\":[{\"IdTipoCanalIngreso\":\"OPT\",\"Codigo\":\"12\",\"Nombre\":\"OPTIMUS\",\"IdEstado\":\"ACT\",\"PermiteEnrolamiento\":\"S\",\"PermiteFirmaDocumento\":\"S\"}]}";
        
        
        try (Connection conn = PlataformaUtils.getDESAConnection()) {
        	System.out.println("== conexion ==" + conn);
            Gson gson = Utils.getGsonBuilder();
            EntradaModificarCanalesIngreso entradaModificarCanalesIngreso = gson.fromJson(jsonRequest, EntradaModificarCanalesIngreso.class);
            System.out.println("Entrada:");
            System.out.println(entradaModificarCanalesIngreso);

            ModificarCanalesIngreso modificarCanalesIngreso = new ModificarCanalesIngreso();
            try {
                SalidaModificarCanalesIngreso salidaModificarCanalesIngreso = modificarCanalesIngreso.logica(conn, entradaModificarCanalesIngreso);
                System.out.println("Salida:");
                System.out.println(salidaModificarCanalesIngreso);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
           // 	conn.commit();
               conn.rollback();
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        System.out.println("== FIN TEST ==");
    }
}
