package cl.bcs.socionegocio.por.pojo;

import cl.bcs.utils.BcsPojo;

public class CuentaCustodio extends BcsPojo {

    public Long IdInterno;

    public Long IdCuentaCustodioPortafolio;

    public Long IdPortafolio;
    public String IdBoveda;
    public String Codigo;

    public java.sql.Timestamp FechaIngreso;
    public java.sql.Timestamp FechaCreacion;
    public java.sql.Timestamp FechaModificacion;
    public Long IdUsuarioCreador;

    public String IdEstado;

    public String IdEstadoAutorizacion;

    public String IdTipoCuentaCustodia;
    public String IdTipoCustodia;
    public String IdModeloCustodia;
    public Long IdPersonaBancoCustodio;
    public Long IdPersonaBancoLiquidador;
    public String LiquidaDividendos;
    public String LiquidaCupones;
    public String LiquidaVencimientos;
    public String LiquidaPrepagos;

    public CuentaCustodio() {
        this.IdTipoCuentaCustodia = "";
        this.IdTipoCustodia = "";
        this.IdModeloCustodia = "";
        this.IdPersonaBancoCustodio = 0L;
        this.IdPersonaBancoLiquidador = 0L;
        this.LiquidaDividendos = "N";
        this.LiquidaCupones = "N";
        this.LiquidaVencimientos = "N";
        this.LiquidaPrepagos = "N";
    }
}
