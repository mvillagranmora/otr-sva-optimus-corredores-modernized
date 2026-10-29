package cl.bcs.socionegocio.enr.pojo;

import cl.bcs.utils.BcsPojo;

public class ResultadoAdministrarBorradorEntidad extends BcsPojo {
	public Long IdInterno;
	public Long IdEntidad;

	public ResultadoAdministrarBorradorEntidad() {
		super();
	}

	public ResultadoAdministrarBorradorEntidad(Long idInterno, Long idEntidad) {
		super();
		IdInterno = idInterno;
		IdEntidad = idEntidad;
	}
}
