package cl.bcs.socionegocio.enr.pojo;

import cl.bcs.utils.BcsPojo;

public class TipoRelacion extends BcsPojo {
	public String IdTipoRelacion;
	public String EsIndefinida;
	public String UsaPorcentajeParticipacion;
	public String UsaFinalidadRelacion;
	public String UsaCondicionesRelacion;
	public String UsaGradoVinculacionParental;
	public String IdEstado;

	public TipoRelacion() {
		IdTipoRelacion = "";
		EsIndefinida = "S";
		UsaPorcentajeParticipacion = "N";
		UsaFinalidadRelacion = "N";
		UsaCondicionesRelacion = "N";
		UsaGradoVinculacionParental = "N";
		IdEstado = "";
	}
}
