package cl.bcs.plataforma.doc.pojo;

import cl.bcs.plataforma.ind.pojo.Indicador;
import cl.bcs.utils.BcsPojo;
import java.util.ArrayList;
import java.util.List;

public class TipoDocumento extends BcsPojo {
    public Long IdEmpresa;
    public String IdTipoDocumento;
    public String NombreDocumento;
    public Long NumeroFirmantesMinimo;
    public Long NumeroFirmantesMaximo;
    public String Glosa;
    public String ValidaFechaVigencia;
    public String IdTipoPersona;
    public String SolicitarFechaFirma;
    public Long IdClase;
    public String TipoDocumentoNormativa;
    public String FechaFirma;
    public String CodigoArchivo;
    public String ValidaClienteFull;
    public String RequiereFirmaCliente;
    public String BloquearAlVencer;
    public String FirmaAutomaticaDocumento;
    public List<Indicador> Requisitos;

    public TipoDocumento() {
        TipoDocumentoNormativa = "";
        FechaFirma = "";
        ValidaClienteFull = "";
        RequiereFirmaCliente ="";
        BloquearAlVencer = "";
        FirmaAutomaticaDocumento = "N";
        Requisitos = new ArrayList<>();
    }
}
