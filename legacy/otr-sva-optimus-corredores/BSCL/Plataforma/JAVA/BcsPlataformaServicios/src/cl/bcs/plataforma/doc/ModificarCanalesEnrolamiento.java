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

public class ModificarCanalesEnrolamiento implements JavaCall {
    static final Logger logger = LogManager.getLogger(ModificarCanalesEnrolamiento.class);
    
    private final String SQL_Insert_CanalesEnrolamiento = loadResourceToString("cl/bcs/plataforma/doc/sql/ModificarCanalesEnrolamiento_InsertCanales.sql");
    private final String SQL_Delete_CanalesEnrolamiento = loadResourceToString("cl/bcs/plataforma/doc/sql/ModificarCanalesEnrolamiento_DeleteCanales.sql");

    public static class EntradaModificarCanalesEnrolamiento  {
        public Long 	p_IdEmpresa;
        public Long 	p_IdUsuario;
        public String 	p_IdModulo ;
        public String   p_IdTipoDocumento;
        
        public List<EntidadCanaldeEnrolamiento> p_CanalesDeEnrolamiento;

        public EntradaModificarCanalesEnrolamiento() {
        	p_CanalesDeEnrolamiento = new ArrayList<>();
        }           
    }
    
    public  class EntidadCanaldeEnrolamiento {
    	  	
    	public Long   Codigo ;
    	public String IdEstado ;
    	public String IdTipoCanal ;
    	public String Nombre ;
    	public String PermiteEnrolamiento ;
    	public String PermiteFirmaDocumento ;
    	
    }
    
    public  class EntidadCanalesDocumento {
    
    	public String IdTipoCanal ;
    	public String IdTipoDocumento ;
   	
    }
    
    public static class SalidaModificarCanalesEnrolamiento{
        public Long P_OUT_ERRORESCONTADOR;
        public List<PlataformaUtils.MensajeSalida> P_OUT_MENSAJES;
        
        public SalidaModificarCanalesEnrolamiento() {
            P_OUT_MENSAJES = new ArrayList<>();
            P_OUT_ERRORESCONTADOR = 0L;
        }
    }
    
    private void InsertCanalesEnrolamiento(Connection conn, List<EntidadCanaldeEnrolamiento> CanalesDeEnrolamiento ,String  IdTipoDocumento )  throws SQLException {
    	logger.info("INICIO::ModificarCanalesEnrolamiento.InsertCanalesEnrolamiento");
        SQL sql = new SQL();

        if (CanalesDeEnrolamiento.size() > 0 ) {
        List<EntidadCanalesDocumento> datos = new ArrayList<>();

        for ( EntidadCanaldeEnrolamiento canal : CanalesDeEnrolamiento){
        	EntidadCanalesDocumento dato = new EntidadCanalesDocumento();
                
            dato.IdTipoCanal = canal.IdTipoCanal;
            dato.IdTipoDocumento    = IdTipoDocumento;          
                        
            datos.add(dato);
                
        	logger.info("Eliminando");
            sql.batch(conn, SQL_Delete_CanalesEnrolamiento, datos, EntidadCanalesDocumento.class);
            logger.info("Eliminando");
            
           	logger.info("Insertando");
                sql.batch(conn, SQL_Insert_CanalesEnrolamiento, datos, EntidadCanalesDocumento.class);
             logger.info("Insertando");
                
           
            
        }
        }

		logger.info("FIN::ModificarCanalesEnrolamiento.InsertCanalesEnrolamiento");
    }
    
    
    
    private SalidaModificarCanalesEnrolamiento logica(Connection conn, EntradaModificarCanalesEnrolamiento entrada) throws Exception {
        logger.info("INICIO::AgregarPlantillaContratoDocumento");
        long inicio = System.currentTimeMillis();

        
        SalidaModificarCanalesEnrolamiento salida = new SalidaModificarCanalesEnrolamiento();

        
        if(entrada.p_IdTipoDocumento != null) {
        	InsertCanalesEnrolamiento(conn, entrada.p_CanalesDeEnrolamiento, entrada.p_IdTipoDocumento);
        	salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"INSERCION_EXITOSA",new HashMap<String,String>(),conn,entrada.p_IdEmpresa));
        }else {
        	salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"INSERCION_NO_EXITOSA",new HashMap<String,String>(),conn,entrada.p_IdEmpresa));
        }

        return salida;
    }

    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        Gson gson = Utils.getGsonBuilder();
        EntradaModificarCanalesEnrolamiento entrada = gson.fromJson(in, EntradaModificarCanalesEnrolamiento.class);
    
        SalidaModificarCanalesEnrolamiento salida = logica(conn, entrada);
        JsonParser jsonParser = new JsonParser();

        return jsonParser.parse(gson.toJson(salida)).getAsJsonObject();
    }


    public static void main(String[] args) {
        System.out.println("== INICIO TEST ==");

     
        String jsonRequest= "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"DOC\",\"p_UsaReglasNegocio\":\"N\",\"p_IdTipoDocumento\":\"WWWE\",\"p_CanalesDeEnrolamiento\":[{\"IdTipoCanal\":\"OPT\",\"Codigo\":\"12\",\"Nombre\":\"OPTIMUS\",\"IdEstado\":\"ACT\",\"PermiteEnrolamiento\":\"S\",\"PermiteFirmaDocumento\":\"S\"}]}";
        
        
        try (Connection conn = PlataformaUtils.getDESAConnection()) {
        	System.out.println("== conexion ==" + conn);
            Gson gson = Utils.getGsonBuilder();
            EntradaModificarCanalesEnrolamiento entradaModificarCanalesEnrolamiento = gson.fromJson(jsonRequest, EntradaModificarCanalesEnrolamiento.class);
            System.out.println("Entrada:");
            System.out.println(entradaModificarCanalesEnrolamiento);

            ModificarCanalesEnrolamiento modificarCanalesEnrolamiento = new ModificarCanalesEnrolamiento();
            try {
                SalidaModificarCanalesEnrolamiento salidaModificarCanalesEnrolamiento = modificarCanalesEnrolamiento.logica(conn, entradaModificarCanalesEnrolamiento);
                System.out.println("Salida:");
                System.out.println(salidaModificarCanalesEnrolamiento);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
            	//conn.commit();
            	conn.rollback();
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        System.out.println("== FIN TEST ==");
    }
}
