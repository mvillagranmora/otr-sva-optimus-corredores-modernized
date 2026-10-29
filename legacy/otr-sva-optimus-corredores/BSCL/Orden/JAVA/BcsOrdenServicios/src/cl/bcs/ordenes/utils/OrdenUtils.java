package cl.bcs.ordenes.utils;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;

import cl.bcs.ordenes.utils.OrdenUtils.AdvertenciaDetalle;
import cl.bcs.ordenes.utils.OrdenUtils.ErrorDetalle;
import cl.bcs.ordenes.utils.OrdenUtils.InformacionDetalle;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.SQL;


public class OrdenUtils {
	
	public static List<PlataformaUtils.MensajeSalida> convertirMensajes(List<ErrorDetalle> errDet, List<AdvertenciaDetalle> advDet, List<InformacionDetalle> infDet) {
		
		ArrayList<PlataformaUtils.MensajeSalida> salida = new ArrayList<>();
		
		for (AdvertenciaDetalle adv : advDet) {
			if (adv.RequiereAutorizacion.equalsIgnoreCase("S"))
				salida.add(new MensajeSalida(adv.IdInterno,MensajeSalida.MENSAJE_AUTORIZACION,adv.Codigo,adv.Descripcion));
			else
				salida.add(new MensajeSalida(adv.IdInterno,MensajeSalida.MENSAJE_ADVERTENCIA,adv.Codigo,adv.Descripcion));
		}
		
		for (ErrorDetalle err : errDet) {
			salida.add(new MensajeSalida(err.IdInterno,MensajeSalida.MENSAJE_ERROR,err.Codigo,err.Descripcion));
		}
		
		for (InformacionDetalle det : infDet) {
			salida.add(new MensajeSalida(det.IdInterno,MensajeSalida.MENSAJE_INFORMACION,det.Codigo,det.Descripcion));
		}
		
		return salida;
		
	}
	
	public static class ErrorDetalle extends PlataformaUtils.ErrorDetalle {
		public Long IdInterno;
		public Long IdOperacion;
		
		public String toString() {
			return cl.bcs.utils.Utils.printJson(this,true);
			
		}
	}
	
	public static class AdvertenciaDetalle extends PlataformaUtils.AdvertenciaDetalle {
		public Long IdInterno;
		public Long IdOperacion;
		
		public String toString() {
			return cl.bcs.utils.Utils.printJson(this,true);
			
		}
	}
	
	public static class InformacionDetalle extends PlataformaUtils.InformacionDetalle {
		public Long IdInterno;
		public Long IdOperacion;
		
		public String toString() {
			return cl.bcs.utils.Utils.printJson(this,true);
			
		}
	}

	
	
}