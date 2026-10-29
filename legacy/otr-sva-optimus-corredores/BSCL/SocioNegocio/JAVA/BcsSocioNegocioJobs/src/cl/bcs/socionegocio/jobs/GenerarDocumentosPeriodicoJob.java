package cl.bcs.socionegocio.jobs;

import java.sql.Connection;
import java.sql.SQLException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.socionegocio.utils.RegenerarDocumentosCliente;
import cl.bcs.socionegocio.utils.RegenerarDocumentosCliente.EntradaRegenerarDocumentosCliente;
import cl.bcs.socionegocio.utils.RegenerarDocumentosCliente.SalidaRegenerarDocumentosCliente;



public class GenerarDocumentosPeriodicoJob extends BcsJob {

	static final Logger logger	 = LogManager.getLogger(GenerarDocumentosPeriodicoJob.class);	
	
	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		logger.info("INICIO::SalidaRegenerarDocumentosCliente");
		long inicioServicio = System.currentTimeMillis();
		RegenerarDocumentosCliente regeneracionDocumentos = new RegenerarDocumentosCliente();
		EntradaRegenerarDocumentosCliente entrada = new EntradaRegenerarDocumentosCliente();
		
		try {
			Long idEmpresa = Long.parseLong(this.getParam("p_IdEmpresa"));
			Long idUsuario = Long.parseLong(this.getParam("p_IdUsuario"));
			String idModulo = this.getParam("p_IdModulo");
			
			entrada.p_IdEmpresa = idEmpresa;
			entrada.p_IdUsuario = idUsuario;
			entrada.p_IdModulo = idModulo;			
			
			SalidaRegenerarDocumentosCliente salida= regeneracionDocumentos.logica(con,entrada);
			
	        logger.info( "*SalidaRegenerarDocumentosCliente* salida.P_OUT_ERRORESCONTADOR",salida.P_OUT_ERRORESCONTADOR);
	        if(salida.P_OUT_ERRORESCONTADOR.compareTo(0L) != 0) {
	        	salida.P_OUT_MENSAJES.forEach(msj ->{
	        		logger.info("*SalidaRegenerarDocumentosCliente* Mensajes :",msj.tipo,msj.descripcion);
	        	});
	        }
			
		}catch(Exception e) {
			logger.error("Error", e);
			throw e;
		}
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::SalidaRegenerarDocumentosCliente :" , finServicio- inicioServicio);		
		return false;
	}

	@Override
	public void onError(Throwable t) {
		// TODO Auto-generated method stub
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
	}
	
	 public static void main(String[] args) throws SQLException {
	    	Connection con = PlataformaUtils.getDESAConnection();
	    	GenerarDocumentosPeriodicoJob p = new GenerarDocumentosPeriodicoJob();
	        try {
	            p.transaction(con);
	            con.rollback();
	        } catch (InterruptedException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        } catch (Throwable e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        }

	    }

}
