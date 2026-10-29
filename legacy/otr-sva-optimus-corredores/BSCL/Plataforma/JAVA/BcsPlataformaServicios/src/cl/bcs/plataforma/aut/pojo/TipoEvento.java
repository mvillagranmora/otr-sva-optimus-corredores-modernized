package cl.bcs.plataforma.aut.pojo;

import java.math.BigDecimal;

import cl.bcs.utils.BcsPojo;

public class TipoEvento extends BcsPojo {
	public String IdTipoEvento;
	public Long IdEmpresa;
	public String IdModulo;
	public String Codigo;
	public String Nombre;
	public BigDecimal HorasVigencia;	
	public String EtapasSecuenciales;
	public String IdTipoProducto;
	public String IdTipoOperacion;	
	public Long IdUsuarioSuperAutorizador;
	public Long IdRolSuperAutorizador;
	public String IdEstado;
	public String SPConfirma;
	public String SPCampoAutorizacion;
	public String SPCampoNegocio;
	public String SPRechaza;
	public Integer SPFormato;
	public String JavaAutorizacion;
}