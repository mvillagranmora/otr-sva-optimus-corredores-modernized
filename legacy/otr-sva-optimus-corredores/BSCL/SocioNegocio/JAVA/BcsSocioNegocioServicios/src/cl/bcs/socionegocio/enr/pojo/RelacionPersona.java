package cl.bcs.socionegocio.enr.pojo;

import java.math.BigDecimal;

import cl.bcs.utils.BcsPojo;

public class RelacionPersona extends BcsPojo{
	public Long IdInterno;
	public Long IdRelacionPersona;
	public Long IdEnrolamientoOrigen;
	public String IdTipoRelacion;
	public Long IdPersonaDestino;
	public String FechaInicioRelacion;
	public String FechaFinRelacion;
	public BigDecimal PorcentajeParticipacion;
	public String RetiraDocumentos;
	public String FinalidadRelacion;
	public String CondicionesRelacion;
	public Long IdGradoVinculacionParental;
	public Long IdCiudad;
	public String Calle;
	public String Numero;
	public String Email;
	public String Telefono;
	public String Complemento;
	public String Ciudad;
	public Long IdRelacionPersonaPersona;
	public String TipoRelacionUsaPorcentajeParticipacion;
	public String TipoRelacionEsIndefinida;
	public String TipoRelacionUsaFinalidadRelacion;
	public String TipoRelacionUsaCondicionesRelacion;
	public String TipoRelacionUsaGradoVinculacionParental;
	public String EstaInsertada;
	//wbs
	public String NombresPersonaRelacion;
	public String ApellidoPaternoPersonaRelacion;
	public String ApellidoMaternoPersonaRelacion;
	public String DocumentoIdentidadRelacion;
	public String TipoDocumentoIdentidadRelacion;
	public RelacionPersona() {
		this.IdRelacionPersonaPersona = 0L;
		this.TipoRelacionUsaPorcentajeParticipacion = "";
		this.TipoRelacionEsIndefinida = "";
		this.TipoRelacionUsaFinalidadRelacion = "";
		this.TipoRelacionUsaCondicionesRelacion = "";
		this.TipoRelacionUsaGradoVinculacionParental = "";
		this.EstaInsertada = "";
	}
}
