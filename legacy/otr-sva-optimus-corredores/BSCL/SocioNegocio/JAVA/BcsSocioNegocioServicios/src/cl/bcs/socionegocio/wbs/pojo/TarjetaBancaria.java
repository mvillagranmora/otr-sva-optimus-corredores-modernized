package cl.bcs.socionegocio.wbs.pojo;

import java.sql.Date;

import cl.bcs.utils.BcsPojo;

public class TarjetaBancaria extends BcsPojo {
	public String Accion;
	public Long IdPersona;
	public Long IdInterno;
	public Long IdTarjetaBancaria;
	public Long IdBanco;
	public String IdTipoTarjetaBancaria;
	public String NumeroTarjetaBancaria;
	public Date FechaVencimiento;
	public Integer CodigoVerificacion;
	public String IdEstado;
	public Long IdUsuarioCreador;
	public String IdEstadoAutorizacion;
	public String TipoEnrolamiento;
	public String CodigoFormulario;
	public Long IdCliente;
	public String Tipo;
	public String EsBorrador;
}