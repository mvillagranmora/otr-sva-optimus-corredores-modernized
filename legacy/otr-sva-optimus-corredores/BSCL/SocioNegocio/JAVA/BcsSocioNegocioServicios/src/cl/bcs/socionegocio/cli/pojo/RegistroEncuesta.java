package cl.bcs.socionegocio.cli.pojo;

import cl.bcs.utils.Utils;

public class RegistroEncuesta {
	
	public Long IdEmpresa;
	public Long IdCliente;
	public Long IdPortafolio;
	public Long IdRegistroEncuesta;
	public String IdCategoriaInversionista;
	public String NombreEncuesta;
	public Long Puntaje;
	public java.sql.Timestamp FechaIngresoRegistro;
	public java.sql.Timestamp FechaEncuesta;
	public Long IdUsuarioResponde;
	public String AsignadoPorEncuesta;
	public String IdCanal;
	public String IdPerfilProductos;
	
	public RegistroEncuesta () {		
		this.IdPortafolio= 0L;
		this.IdPerfilProductos ="";		
	}
	
	public String toString() {
		return Utils.printJson(this, false);
	}

	public RegistroEncuesta paraPortafolio(Long idPortafolio, String idPerfilProductos) {
		RegistroEncuesta registroEncuesta = new RegistroEncuesta();
		registroEncuesta.AsignadoPorEncuesta = this.AsignadoPorEncuesta;
		registroEncuesta.FechaEncuesta = this.FechaEncuesta;
		registroEncuesta.FechaIngresoRegistro = this.FechaIngresoRegistro;
		registroEncuesta.IdRegistroEncuesta = this.IdRegistroEncuesta;
		registroEncuesta.IdCanal = this.IdCanal;
		registroEncuesta.IdCategoriaInversionista = this.IdCategoriaInversionista;
		registroEncuesta.IdCliente = this.IdCliente;
		registroEncuesta.IdEmpresa = this.IdEmpresa;
		registroEncuesta.IdPerfilProductos = idPerfilProductos;
		registroEncuesta.IdPortafolio = idPortafolio;
		registroEncuesta.IdUsuarioResponde = this.IdUsuarioResponde;
		registroEncuesta.NombreEncuesta = this.NombreEncuesta;
		registroEncuesta.Puntaje = this.Puntaje;

		return registroEncuesta;
	}
}
