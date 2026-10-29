package cl.bcs.plataforma.doc.pojo;

import java.util.Date;

import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Utils;

public class PlantillaContratoDocumento extends BcsPojo {
    
    public Long IdPlantillaContratoDocumento;
    public String PlantillaContrato;
    public String CodigoArchivo;
    public String Estado;
    public Date FechaVigencia;
    public Date FechaVencimiento;
    public String IdTipoDocumento;
	


    public String toString() {
		return Utils.printJson(this, false);
	}
}

