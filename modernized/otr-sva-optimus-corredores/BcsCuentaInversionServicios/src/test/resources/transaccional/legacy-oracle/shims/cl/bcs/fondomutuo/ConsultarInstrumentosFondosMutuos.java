package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;
import java.sql.Date;
import java.sql.Timestamp;
import cl.bcs.utils.Utils;

/**
 * ORACLE SHIM: only the InFondos POJO of ConsultarInstrumentosFondosMutuos (verbatim); imported (never used) by ProcesaCargaSeleccionada.
 */
public class ConsultarInstrumentosFondosMutuos {

	// legacy cl/bcs/fondomutuo/ConsultarInstrumentosFondosMutuos.java:46-80 (verbatim)
	public static class InFondos
	{
		public String IdSerieFondo;
		public String IdEmisor;
		public String IdFamiliaFondo;
		public List<String> IdRegimenFondoMutuo;
		public String IdSerieFondoOrigen; // Cuando tiene valor se obtienen los instrumentos candidatos a un aporte por reinversion
		public String IdCategoriaPerfilInversionista;
		public String IdEstado;
		public String IdMoneda;
		public String FondoExtranjero;
	    public String InstrumentoOWP;
	    public String EsAPV;
	    public String OcultarBloqueados;
	    
	    public InFondos() {
	    	this.IdSerieFondo = "";
	    	this.IdEmisor = "";
	    	this.IdFamiliaFondo = "";
	    	this.IdRegimenFondoMutuo = new ArrayList<String>();
	    	this.IdSerieFondoOrigen = "";
	    	this.IdCategoriaPerfilInversionista = "";
	    	this.IdEstado = "";
	    	this.IdMoneda = "";
	    	this.FondoExtranjero = "";
	    	this.InstrumentoOWP = "";
	    	this.EsAPV = "";
	    	this.OcultarBloqueados = "N";
	    }
	    
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}


}
