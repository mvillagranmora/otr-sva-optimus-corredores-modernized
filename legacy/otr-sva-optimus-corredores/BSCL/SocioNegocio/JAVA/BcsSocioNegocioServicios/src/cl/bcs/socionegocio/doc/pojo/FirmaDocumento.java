package cl.bcs.socionegocio.doc.pojo;

import cl.bcs.utils.BcsPojo;

public class FirmaDocumento extends BcsPojo {
	public Long IdFirmaAnexo;
	public Long IdAnexo;
	public Long IdPersonaAgente;
	public Long IdSolicitudFirmaAnexo;
	public String IdEstado;
	public String IdTipoCanalIngreso;
	public String EsAutorizado;
	public java.sql.Timestamp FechaFirma;
	public Long IdUsuario;
}