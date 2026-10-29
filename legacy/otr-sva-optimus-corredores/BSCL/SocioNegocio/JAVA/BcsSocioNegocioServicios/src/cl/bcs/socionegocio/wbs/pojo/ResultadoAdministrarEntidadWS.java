package cl.bcs.socionegocio.wbs.pojo;

import cl.bcs.utils.BcsPojo;

public class ResultadoAdministrarEntidadWS extends BcsPojo {
	public Long IdInterno;
	public Long IdEntidad;

	public ResultadoAdministrarEntidadWS() {
		super();
	}

	public ResultadoAdministrarEntidadWS(Long idInterno, Long idEntidad) {
		super();
		IdInterno = idInterno;
		IdEntidad = idEntidad;
	}
}