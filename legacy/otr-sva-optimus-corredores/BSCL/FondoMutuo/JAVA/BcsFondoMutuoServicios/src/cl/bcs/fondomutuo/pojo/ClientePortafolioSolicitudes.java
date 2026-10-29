package cl.bcs.fondomutuo.pojo;

import cl.bcs.utils.Utils;

public class ClientePortafolioSolicitudes {
	
	public String NombreCompleto;
	public String CodigoPortafolio;
	public Long IdPortafolio;
	public Long IdCliente;
	public String IdEstadoCliente;
	public String IdEstadoPortafolio;
	public String DocumentoIdentidad;
	public Long IdEmpleadoEjecutivo;
	public String TipoDocumentoIdentidad;
	public String Proposito;
	public Long IdPersona;
	public String EsClienteNuevoProspecto;
	
	public String BloqueadoPorCumplimiento = "N";
	
	public String toString() {
		return Utils.printJson(this, false);
	}

}
