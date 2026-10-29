package cl.bcs.plataforma.jobs;

import java.sql.Connection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import cl.bcs.plataforma.pla.EjecutarTareaJenkins;
import cl.bcs.plataforma.pla.EjecutarTareaJenkins.EntradaEjecutarTareaJenkins;
import cl.bcs.plataforma.pla.EjecutarTareaJenkins.ParametroJenkins;
import cl.bcs.plataforma.pla.EjecutarTareaJenkins.SalidaEjecutarTareaJenkins;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;

public final class BcsJobEjecutarTareaJenkins extends BcsJob {
	
	private static final Logger LOGGER = Logger.getLogger(BcsJobEjecutarTareaJenkins.class.getName());
	
    @Override
    public void onError(final Throwable t) {
        LOGGER.log(Level.SEVERE, "Error en proceso de carga", t);
    }

    @Override
    public void pause() {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean transaction(Connection con) throws InterruptedException, Throwable {
    	return logica(con);
    }
	
    public boolean logica(Connection con ) throws Exception {
    	LOGGER.info("INICIO::BcsJobEjecutarTareaJenkins");
    	
    	Long idEmpresa = Long.valueOf(getParam("IdEmpresa"));
    	String tipoTarea = getParam("TipoTarea");
    	String parametros = getParam("Parametros");
    	
		EntradaEjecutarTareaJenkins inJenkins = new EntradaEjecutarTareaJenkins();
		inJenkins.p_IdModulo = "PLA";
		inJenkins.p_IdUsuario = 0L;
		inJenkins.p_IdEmpresa = idEmpresa;
		inJenkins.p_TipoTarea = tipoTarea;
		
		List<String> params = Arrays.asList(parametros.split("&"));
		
		for (String p: params) {
			List<String> NombreValor = Arrays.asList(p.split("="));
			if (NombreValor.size()==2) {
				inJenkins.p_Parametros.add(new ParametroJenkins(NombreValor.get(0),NombreValor.get(1)));
			}
		}
		
		LOGGER.log(Level.INFO, "Entrada Jenkins "+inJenkins);
		
		EjecutarTareaJenkins jenkins = new EjecutarTareaJenkins();
		SalidaEjecutarTareaJenkins outJenkins = jenkins.logica(con, inJenkins);
		
		LOGGER.info("Salida Jenkins > "+outJenkins);
    	LOGGER.info("FIN::BcsJobJenkins");
    	
    	return true;
    }
    
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa02","connections.properties")){
			
			Map<String, String> parameters = new HashMap<String, String>();
			parameters.put("IdEmpresa", "1");
			parameters.put("TipoTarea", "ENVIAR_INTERFAZ_LIQUIDEZ_INTRADIA");
			parameters.put("Parametros", "&SecuenciaCartola=100&GeneraTxt=false");
			
			BcsJobEjecutarTareaJenkins j1 = new BcsJobEjecutarTareaJenkins();
			j1.setParameters(999L,parameters,1L,false,null,"**");
			j1.logica(con);

		} catch (Exception e) {
			e.printStackTrace();			
		}
	}
}