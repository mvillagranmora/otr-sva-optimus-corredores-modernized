package cl.bcs.sura.domain;

public class IdsEntidadYPerfiles {
    public Long idPortafolio;
    public Long idCliente;
    public String documentoIdentidad;
    public Long idIndicador;
    public String idFormaCobro;
    public String idCategoriaInversionista;
    public String idPerfilComercial;
    public String idPerfilCobros;
    public String idPerfilProductos;
    public String proposito;
    public String actualizarFormaCobro;

    public IdsEntidadYPerfiles() {
        this.idPortafolio = 0L;
        this.idCliente = 0L;
        this.documentoIdentidad = "";
        this.idIndicador = 0L;
        this.idFormaCobro = "";
        this.idCategoriaInversionista = "";
        this.idPerfilCobros = "";
        this.idPerfilComercial = "";
        this.idPerfilProductos = "";
        this.proposito = "";
        this.actualizarFormaCobro = "S";
    }
}
