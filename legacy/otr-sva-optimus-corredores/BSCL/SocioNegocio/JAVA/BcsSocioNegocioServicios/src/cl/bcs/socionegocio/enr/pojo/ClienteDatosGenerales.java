package cl.bcs.socionegocio.enr.pojo;

import cl.bcs.utils.BcsPojo;

public class ClienteDatosGenerales extends BcsPojo {
    public Long IdInterno;
    public Long IdEnrolamiento;
    public String IdTipoCliente;
    public String IdCategoriaInversionista;
    public Long IdSegmentoCliente;
    public Long IdEmpleadoEjecutivo;
    public String CodigoEjecutivoExterno;
    public Long IdSucursalPreferencia;
    public String EsClienteFinal;
    public String CodigoArea;
    public transient Long CLI_IdEmpleadoEjecutivo;
    public transient Long CLI_IdSegmentoCliente;
    public transient String CLI_IdTipoCliente;
    public transient String CLI_IdCategoriaInversionista;
    public transient String CLI_AreaCliente;
    public transient String EsBorrador;
}
