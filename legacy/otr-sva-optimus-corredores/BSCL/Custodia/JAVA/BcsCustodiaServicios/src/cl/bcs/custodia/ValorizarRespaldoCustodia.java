package cl.bcs.custodia;

import java.sql.Connection;
import java.util.Stack;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

import cl.bcs.plataforma.pau.ValorizarCarteraPactos;
import cl.bcs.plataforma.pau.ValorizarGarantiasRVRFIF;

public class ValorizarRespaldoCustodia implements JavaCall {

    private static final Logger logger = LogManager.getLogger(ValorizarRespaldoCustodia.class);
    private static final String[] CARTERAS = {"RF", "IF", "Cupones"};
    private static final String PACKAGE_NAME = "cl.bcs.plataforma.pau";
    private static final Stack<String> stackErr = new Stack<>();

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_FechaSistema;
        public Long p_IdPais;
        public String p_SimularOperacion;
        public Long p_IdPortafolio = 0L;
        public Long p_IdCliente = 0L;
    }

    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR;
        public String P_OUT_MENSAJE;

        public Salida() {
            this.P_OUT_ERRORESCONTADOR = 0L;
            this.P_OUT_MENSAJE = "";
        }
    }

    @Override
    public JsonObject call(Connection con, JsonObject jin) throws Exception {
        logger.info("*** Inicio de la ejecucion ***");
        Gson gson = new Gson();
        Entrada in = gson.fromJson(jin.toString(), Entrada.class);
        Salida out = logica(con, in);
        JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
        logger.info("*** Fin de la ejecucion ***");
        return dataout;
    }

    private <T> T createParams(Class<T> paramsClass, Entrada entrada) throws Exception {
        T params = paramsClass.getDeclaredConstructor().newInstance();

        paramsClass.getField("p_FechaSistema").set(params, entrada.p_FechaSistema);
        paramsClass.getField("p_IdCliente").set(params, entrada.p_IdCliente);
        paramsClass.getField("p_IdPortafolio").set(params, entrada.p_IdPortafolio);
        paramsClass.getField("p_IdEmpresa").set(params, entrada.p_IdEmpresa);
        paramsClass.getField("p_IdPais").set(params, entrada.p_IdPais);
        paramsClass.getField("p_IdUsuario").set(params, entrada.p_IdUsuario);
        paramsClass.getField("p_SimularOperacion").set(params, entrada.p_SimularOperacion);

        return params;
    }

    public Salida logica(Connection con, Entrada entrada) throws Exception {
        Salida out = new Salida();

        try {
            logger.info("Inicia ejecucion de valorizar custodia: " + entrada);
            
            for (String situacion : CARTERAS) {
            	try {
            		String className = "Cartera" + situacion;
                	String classPath = PACKAGE_NAME + ".Valorizar" + className;
                	
                	logger.info("Valorizando " + className);
                    
                    Class<?> clase = Class.forName(classPath);

                    JavaCall instance = (JavaCall) clase.getDeclaredConstructor().newInstance();

                    Class<?> entradaClass = Class.forName(classPath + "$ValorizarCartera" + situacion + "Entrada");
                    Object params = createParams(entradaClass, entrada);

                    Gson gson = new Gson();
                    JsonObject jsonParams = new JsonParser().parse(gson.toJson(params)).getAsJsonObject();

                    JsonObject result = instance.call(con, jsonParams);

                    Salida valorizarSalida = gson.fromJson(result, Salida.class);
                    logger.info(situacion + ": " + valorizarSalida.P_OUT_MENSAJE);

                    if (valorizarSalida.P_OUT_ERRORESCONTADOR > 0) {
                        out.P_OUT_ERRORESCONTADOR += valorizarSalida.P_OUT_ERRORESCONTADOR;
                    }
            		
            	} catch (Exception e) {
            		logger.error("Error para valorizar " + situacion + ". " + e);
            		stackErr.push(situacion);
            	}
            }
            
            logger.info("Valorizando Pactos");
            try {
            	ValorizarCarteraPactos.ValorizarCarteraPactosEntrada paramsPactos = new ValorizarCarteraPactos.ValorizarCarteraPactosEntrada();
                paramsPactos.p_FechaSistema = entrada.p_FechaSistema;
                paramsPactos.p_IdCliente = entrada.p_IdCliente;
                paramsPactos.p_IdPortafolio = entrada.p_IdPortafolio;
                paramsPactos.p_IdEmpresa = entrada.p_IdEmpresa;
    			paramsPactos.p_IdPais = entrada.p_IdPais;
    			paramsPactos.p_IdUsuario = entrada.p_IdUsuario;
    			paramsPactos.p_SimularOperacion = entrada.p_SimularOperacion;
    			
    			ValorizarCarteraPactos valorizarPactos = new ValorizarCarteraPactos();
    			ValorizarCarteraPactos.ValorizarCarteraPactosSalida valorizarPactosSalida = new ValorizarCarteraPactos.ValorizarCarteraPactosSalida();
    			valorizarPactosSalida = valorizarPactos.logica(con, paramsPactos);
            } catch (Exception e) {
            	logger.error("Error para valorizar Pactos. " + e);
        		stackErr.push("Pactos");
            }
            
            logger.info("Valorizando Garantias");
            try {
            	ValorizarGarantiasRVRFIF.ValorizarCarteraRVRFIFEntrada paramsGaratia = new ValorizarGarantiasRVRFIF.ValorizarCarteraRVRFIFEntrada();
            	paramsGaratia.p_FechaSistema = entrada.p_FechaSistema;
            	paramsGaratia.p_IdCliente = entrada.p_IdCliente;
            	paramsGaratia.p_IdPortafolio = entrada.p_IdPortafolio;
            	paramsGaratia.p_IdEmpresa = entrada.p_IdEmpresa;
            	paramsGaratia.p_IdPais = entrada.p_IdPais;
            	paramsGaratia.p_IdUsuario = entrada.p_IdUsuario;
            	paramsGaratia.p_SimularOperacion = entrada.p_SimularOperacion;
    			
    			ValorizarGarantiasRVRFIF valorizarGarantias = new ValorizarGarantiasRVRFIF();
    			ValorizarGarantiasRVRFIF.ValorizarCarteraRVRFIFSalida valorizarGarantiasSalida = new ValorizarGarantiasRVRFIF.ValorizarCarteraRVRFIFSalida();
    			valorizarGarantiasSalida = valorizarGarantias.logica(con, paramsGaratia);
            } catch (Exception e) {
            	logger.error("Error para valorizar Garantias. " + e);
        		stackErr.push("Garantias");
            }
            
            String outMsg = stackErr.size() > 0 ? "Problemas para valorizar situaciones de tipo" + stackErr.toString() 
			: "Valorizacion Respaldo Custodia completado";
            
            out.P_OUT_ERRORESCONTADOR = (long) stackErr.size();
            out.P_OUT_MENSAJE = outMsg;

            logger.info("Fin de la ejecucion respaldo de custodia. " + "(" + outMsg + ")");

        } catch (Exception e) {
            logger.info("Error en proceso de valorizar custodia: " + e.toString());
            e.printStackTrace();
            throw e;
        }

        return out;
    }

    public static void main(String[] args) {
    	System.out.println("== TEST ==");
		try {
			Connection con = PlataformaUtils.getDESAConnection();
			Entrada entrada = new Entrada();
			entrada.p_FechaSistema = "2024-05-14";
			entrada.p_IdCliente = 4846L;
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdPais = 44L;
			entrada.p_IdUsuario = 0L;
			entrada.p_SimularOperacion = "N";
			
			Gson gson = Utils.getGsonBuilder();
 			JsonParser parser = new JsonParser();
	        JsonObject dataJsonIn = parser.parse(gson.toJson(entrada)).getAsJsonObject();
	        ValorizarRespaldoCustodia demo = new ValorizarRespaldoCustodia();			
	        Salida resp = gson.fromJson(demo.call(con, dataJsonIn), Salida.class);
	    	System.out.println(resp);	

//	    	con.commit();
//	    	con.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
        System.out.println("== END ==");
	}

}
