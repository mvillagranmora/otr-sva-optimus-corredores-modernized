package cl.bcs.socionegocio.cli.pojo;


import java.sql.Timestamp;

import cl.bcs.utils.BcsPojo;

public class DatosClienteDatosGenerales extends BcsPojo {

	public String TipoEnrolamiento;

	public Long IdCliente;
	public String NombreCompleto;
	public String DocumentoIdentidad;
	public String IdEstado;
	public Long IdSegmentoCliente;
	public String IdCategoriaInversionista;
	public Long IdPersonaEjecutivo;
	public String IdTipoBloqueo;
	public Timestamp FechaBloqueo;
	public String ComentarioBloqueo;
	public String IdTipoCliente;
	public String CodigoAdicionalHolding;
	public Long IdSucursalPreferencia;
	public Long IdPersonaBrokerExtranjero;
	public String IdClaseCliente;
	public String EsClienteFinal;
	public String CodigoEjecutivoExterno;
	public String AreaCliente;
	public String EsBorrador;
	public String IdPerfilComercial;

	public DatosClienteDatosGenerales() {
		EsBorrador = "N";
	}
}
