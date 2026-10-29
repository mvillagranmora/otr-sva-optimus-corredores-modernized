package cl.bcs.loaderapp.pojo.socionegocio;

import cl.bcs.utils.Utils;

public class Proveedor {
	public int Linea;
	public String IDESTADO; 
	public String AREAPROVEEDOR; 
	public String IDTIPOBLOQUEO; 
	public String TIPOPERSONA; 
	
	public String IUP;
	public String TIPODOCUMENTOIDENTIDAD; 
	public String DOCUMENTOIDENTIDAD;
	
	public String TIPODOCUMENTOIDENTIDADASOCIADO;
	public String DOCUMENTOIDENTIDADASOCIADO;
	
	public String NOMBRES; 
	public String APELLIDOPATERNO; 
	public String APELLIDOMATERNO; 
	public String RAZONSOCIAL; 
	public String FECHANACIMIENTOCONSTITUCION; 
	public String TIPOESTADOCIVIL; 
	public String GENERO; 
	public String PAISNACIMIENTOCONSTITUCION; 
	public String CIUDAD; 
	public String CALLE; 
	public String NUMERO; 
	public String COMPLEMENTO; 
	public String TELEFONO; 
	public String EMAIL;
	@Override
	public String toString() {
		return Utils.printJson(this, false);
	} 
}
