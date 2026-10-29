package cl.bcs.plataforma.lim.pojo;

import cl.bcs.utils.BcsPojo;

import javax.sound.sampled.Port;
import java.util.HashSet;
import java.util.Set;

public class CuentaPropia extends BcsPojo {
	public Long IdCliente;
	public Long IdPersona;
	
	public Set<Long> PortafolioIds;
	
	public CuentaPropia() {
		IdCliente = 0L;
		IdPersona = 0L;
		PortafolioIds = new HashSet<>();
	}
}
