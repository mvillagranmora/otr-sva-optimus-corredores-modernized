package cl.bcs.fondomutuo.pojo;

import cl.bcs.utils.Utils;

public class ConfiguracionFechasFondoMutuo {
	
	 public Long IdConfiguracionFechasFondoMutuo;
	 public Long IdEmpresa;
	 public String IdModalidad;
	 public String IdTipoFecha;
	 public String TipoSolicitud;
	 public String EstadoFondo;
	 public String FondoExtranjero;
	 public String Descripcion;
	 public Long CantidadDias;
	 public String EsHabil;
	 public String IdTipoFechaOrigen;
	 public String DebeSerHabilFechaOrigen;
	 public String ValutaCurse;
	 public String ValutaValorCuota;
	 public String ValutaCertificacion;
	 public String ValutaRetencion;
	 public String CalendarioLocal;
	 public String CalendarioEmisor;
	 public String CalendarioFondo;
	 public String CalendarioMoneda;
	 public String IdEstado;
	
	public String toString() {
		return Utils.printJson(this, false);
	}

}
