package cl.bcs.fondomutuo.pojo;

import cl.bcs.utils.Utils;

public class TipoMovimientoFondoMutuo {
	
	 public String IdTipoMovimientoFondoMutuo;
     public String Descripcion;
     public String IndicadorAporteRescate;   
     public String IdTipoOperacion;
     public String TieneCarga;
     public String TieneIngresoManual;
     public String TieneCertificacion;
     public String IdEstado;
	
	public String toString() {
		return Utils.printJson(this, false);
	}
}
