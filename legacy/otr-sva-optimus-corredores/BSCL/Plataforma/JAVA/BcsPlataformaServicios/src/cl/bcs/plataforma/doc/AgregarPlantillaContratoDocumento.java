package cl.bcs.plataforma.doc;

import cl.bcs.nuam.StoredProcedureException;
import cl.bcs.plataforma.doc.pojo.PlantillaContratoDocumento;
import cl.bcs.plataforma.doc.pojo.TipoDocumento;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.portafolio.pojo.PortafolioMantenedor;
import cl.bcs.socionegocio.doc.DocumentoUtils;
import cl.bcs.socionegocio.doc.pojo.TipoDocumentoIndicador;
import cl.bcs.socionegocio.pojo.Validacion;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.icu.text.SimpleDateFormat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class AgregarPlantillaContratoDocumento implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(AgregarPlantillaContratoDocumento.class);
	
	private final String SQL_INSERTAR_PLANTILLA_CONTRATO_DOCUMENTO = loadResourceToString("cl/bcs/plataforma/doc/sql/AgregarPlantillaContratoDocumento_InsertPlantillaContratoDocumento.sql");
	private final String SQL_MODIFICAR_PLANTILLA_CONTRATO_DOCUMENTO = loadResourceToString("cl/bcs/plataforma/doc/sql/AgregarPlantillaContratoDocumento_UpdatePlantillaContratoDocumento.sql");
    private final String SQL_SELECT_PLANTILLA_CONTRATO_DOCUMENTO = loadResourceToString("cl/bcs/plataforma/doc/sql/AgregarPlantillaContratoDocumento_SelectPlantillaContratoDocumento.sql");
    
	
	public static class EntradaAgregarPlantillaContratoDocumento extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;

        // Parametros PlantillaContratoDocumento
        public String p_CodigoArchivo;
        public String p_IdTipoDocumento;
    
	}
	    

    public static class SalidaAgregarPlantillaContratoDocumento extends BcsPojo {
        public Long P_OUT_ERRORESCONTADOR;
        public List<PlataformaUtils.MensajeSalida> P_OUT_MENSAJES;

        public SalidaAgregarPlantillaContratoDocumento() {
            P_OUT_MENSAJES = new ArrayList<>();
            P_OUT_ERRORESCONTADOR = 0L;
        }
    }

    

    private SalidaAgregarPlantillaContratoDocumento logica(Connection conn, EntradaAgregarPlantillaContratoDocumento entrada) throws Exception {
        logger.info("INICIO::AgregarPlantillaContratoDocumento");
        long inicio = System.currentTimeMillis();

        List<PlantillaContratoDocumento> versionPlantillas = obtenerPlantillasContrato(conn);
        SalidaAgregarPlantillaContratoDocumento salida = new SalidaAgregarPlantillaContratoDocumento();
        ingresarVersionPlantilla(conn, versionPlantillas, entrada);

        
        salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"INSERCION_PLANTILLA_EXITOSA",new HashMap<String,String>(),conn,entrada.p_IdEmpresa));
        

        return salida;
    }

    private void ingresarVersionPlantilla(Connection conn, List<PlantillaContratoDocumento> versionPlantillas, EntradaAgregarPlantillaContratoDocumento entrada)  throws SQLException, StoredProcedureException, ParseException {
    	logger.info("INICIO::AgregarPlantillaContratoDocumento.ingresarVersionPlantilla");
        SQL sql = new SQL();
        Long version = 1L;
        Boolean notData = true;
        List<Long> secuenciaPlantillaContratoDocumento = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::DOC.PlantillaContratoDocumento", Long.valueOf(versionPlantillas.size()));
        Long idVersionPlantillaDocumento = 0L;
        String schema = Utils.getSchema();
        String strFechaHoy = Utils.getFechaSistema(conn, schema, entrada.p_IdEmpresa);
        
        
        // Armar fecha con la fecha del Sistema
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, Integer.parseInt(strFechaHoy.split("-")[0]));
        c.set(Calendar.MONTH, Integer.parseInt(strFechaHoy.split("-")[1])-1);
        c.set(Calendar.DAY_OF_MONTH, Integer.parseInt(strFechaHoy.split("-")[2]));      
        
        Date fechaHoy = c.getTime();
        
        List<PlantillaContratoDocumento> pcdInsert = new ArrayList<>();
        List<PlantillaContratoDocumento> pcdUpdate = new ArrayList<>();
        
        HashMap<String, Object> paramsInsert = new HashMap<String,Object>();
        for ( PlantillaContratoDocumento pcd : versionPlantillas){
        	notData = false;
            // Deja como vencido las plantillas anteriores
            if(pcd.IdTipoDocumento.equals(entrada.p_IdTipoDocumento)){
                
  
                HashMap<String, Object> paramsUpdate = new HashMap<String,Object>();

               
             
                pcd.Estado = "VENCIDO";
                pcd.FechaVencimiento = fechaHoy;
                
                pcdUpdate.add(pcd);
                
            	logger.info("Modificando versiones anteriores");
                sql.batch(conn, SQL_MODIFICAR_PLANTILLA_CONTRATO_DOCUMENTO, pcdUpdate, PlantillaContratoDocumento.class);
                logger.info("Modificando versiones anteriores");
                
                if(pcd.CodigoArchivo.equals(entrada.p_CodigoArchivo))
                	version += 1L;
            }
        }
        
        if(!notData) 
        	idVersionPlantillaDocumento = secuenciaPlantillaContratoDocumento.get(secuenciaPlantillaContratoDocumento.size() - 1) + 1;   
        
        PlantillaContratoDocumento pcdEntradaInsert = new PlantillaContratoDocumento();
        
        pcdEntradaInsert.IdPlantillaContratoDocumento = idVersionPlantillaDocumento;
        pcdEntradaInsert.PlantillaContrato = entrada.p_CodigoArchivo + ".v" + version;
        pcdEntradaInsert.CodigoArchivo = entrada.p_CodigoArchivo;
        pcdEntradaInsert.Estado = "VIGENTE";
        pcdEntradaInsert.FechaVigencia = fechaHoy;
        pcdEntradaInsert.FechaVencimiento = null;
        pcdEntradaInsert.IdTipoDocumento = entrada.p_IdTipoDocumento;
        

        pcdInsert.add(pcdEntradaInsert);
        
        
        try {
            logger.info("INSERTANDO PLANTILLA CONTRATO DOCUMENTO: " + pcdEntradaInsert.IdPlantillaContratoDocumento);
            //sql.query(conn, SQL_INSERTAR_PLANTILLA_CONTRATO_DOCUMENTO, paramsInsert, PlantillaContratoDocumento.class);

    		sql.batch(conn, SQL_INSERTAR_PLANTILLA_CONTRATO_DOCUMENTO, pcdInsert, PlantillaContratoDocumento.class);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	
		logger.info("FIN::AgregarPlantillaContratoDocumento.ingresarVersionPlantilla");
    }

    private List<PlantillaContratoDocumento> obtenerPlantillasContrato(Connection conn)  throws SQLException {
        long inicio = System.currentTimeMillis();
        SQL sql = new SQL();
        
         HashMap<String, Object> params = new HashMap<String,Object>();
    
         
		List<PlantillaContratoDocumento> versionPlantillas = sql.query(conn, SQL_SELECT_PLANTILLA_CONTRATO_DOCUMENTO, params, PlantillaContratoDocumento.class);
        
        return versionPlantillas;
    }







    public static void main(String[] args) {
        System.out.println("== INICIO TEST ==");

        String jsonRequest = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"PLA\",\"p_UsaReglasNegocio\":\"N\",\"p_CodigoArchivo\":\"CONTRATO MDI\",\"p_IdTipoDocumento\":\"CONTRATOMERCANTIL\"}";

        try (Connection conn = PlataformaUtils.getDESAConnection()) {
            Gson gson = Utils.getGsonBuilder();
            EntradaAgregarPlantillaContratoDocumento entradaAgregarPlantillaContratoDocumento = gson.fromJson(jsonRequest, EntradaAgregarPlantillaContratoDocumento.class);
            System.out.println("Entrada:");
            System.out.println(entradaAgregarPlantillaContratoDocumento);

            AgregarPlantillaContratoDocumento agregarPlantillaContratoDocumento = new AgregarPlantillaContratoDocumento();
            try {
                SalidaAgregarPlantillaContratoDocumento salidaAgregarPlantillaContratoDocumento = agregarPlantillaContratoDocumento.logica(conn, entradaAgregarPlantillaContratoDocumento);
                System.out.println("Salida:");
                System.out.println(salidaAgregarPlantillaContratoDocumento);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                conn.commit();
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        System.out.println("== FIN TEST ==");
    }
 

	@Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        Gson gson = Utils.getGsonBuilder();
        EntradaAgregarPlantillaContratoDocumento entrada = gson.fromJson(in, EntradaAgregarPlantillaContratoDocumento.class);
        SalidaAgregarPlantillaContratoDocumento salida = logica(conn, entrada);
        JsonParser jsonParser = new JsonParser();

        return jsonParser.parse(gson.toJson(salida)).getAsJsonObject();
    }

}
