package cl.bcs.facturacion.pojo;

import java.util.List;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;

public abstract class SalidaCargaMasivaRefacturacion {
	
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<SalidaSimulacion> P_OUT_SIMASIGNACION;
		public List<SalidaCobrosSimulacion> P_OUT_COBROSASIGNACION;
		public List<Long> P_OUT_COMPROBANTES;
	
}
