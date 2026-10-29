package cl.bcs.plataforma.pla.pojo;

import java.util.ArrayList;

import java.util.List;
import cl.bcs.utils.BcsPojo;

public class FechasIn extends BcsPojo{
	public Long idInterno;
	public String fechaInicial;
	public Long numeroDias;
	public List<Long> paises;
	
	public FechasIn() {
		this.idInterno =0L;
		this.fechaInicial="0001-01-01";
		this.numeroDias=0L;
		this.paises=new ArrayList<Long>();
	}

	public FechasIn(Long idInterno, String fechaInicial, Long numeroDias, List<Long> idPaises) {
		this.idInterno = idInterno;
		this.fechaInicial = fechaInicial;
		this.numeroDias = numeroDias;
		this.paises = idPaises;
	}
}

