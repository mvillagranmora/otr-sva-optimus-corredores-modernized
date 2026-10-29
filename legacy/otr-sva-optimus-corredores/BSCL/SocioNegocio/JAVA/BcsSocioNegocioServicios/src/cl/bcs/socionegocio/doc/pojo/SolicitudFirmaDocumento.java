package cl.bcs.socionegocio.doc.pojo;

import cl.bcs.utils.BcsPojo;

public class SolicitudFirmaDocumento extends BcsPojo {
    public Long IdSolicitudFirmaAnexo;
    public Long IdPersonaFirmante;
    public Long IdPersonaSolicitante;
    public String IdTipoCanalIngresoSolicitudFirmaAnexo;

    public Long IdFirmaAnexo;
    public Long IdAnexo;
    public Long IdDocumento;
    public Long IdCliente;
    public Long IdPersonaAgente;
    public String IdEstadoFirma;
    public java.sql.Timestamp FechaFirma;
    public String IdTipoCanalIngresoFirmaAnexo;

    public java.sql.Timestamp FechaExpiracionToken;
    public String Token;
}
