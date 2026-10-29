package cl.bcs.socionegocio.jobs;

import java.sql.Connection;

import cl.bcs.scheduler.BcsJob;
import cl.bcs.socionegocio.cli.NotificarCaducidadDatos;
import cl.bcs.socionegocio.cli.NotificarCaducidadDatos.SalidaNotificarCaducidad;

public class DatosProximosPorVencerJob extends BcsJob{

	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		
		NotificarCaducidadDatos notificar = new NotificarCaducidadDatos();
		
		SalidaNotificarCaducidad out = notificar.logica(con);
		
		if(out.P_OUT_ERRORESCONTADOR>0) {
			return false;
		}
		return true;
	}

	@Override
	public void onError(Throwable t) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}
	

}
