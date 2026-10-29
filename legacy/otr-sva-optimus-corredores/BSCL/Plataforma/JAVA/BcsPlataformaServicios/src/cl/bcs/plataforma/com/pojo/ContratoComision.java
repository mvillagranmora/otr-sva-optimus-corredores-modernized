package cl.bcs.plataforma.com.pojo;

import java.sql.Timestamp;

import cl.bcs.utils.BcsPojo;

public class ContratoComision extends BcsPojo{
	
	public Long 		IdInterno;
	public Long 		IdContratoComision;
	public String 		Nombre;
	public String 		Descripcion;
	public String		IdTipoContrato; 
	public String 		CodigoArea;
	public String		IdFormatoCobro; 
	public String		IdClasificacionTipoInstrumento;  
	public String		IdMoneda;
	public Long			IdSegmentoCliente;
	public Long			IdPortafolio;
	public Long 		IdCliente;
	public Long 		IdEmpresa;
	public String 		IdEstado;
	public Timestamp 	FechaIngresoRegistro;

}
