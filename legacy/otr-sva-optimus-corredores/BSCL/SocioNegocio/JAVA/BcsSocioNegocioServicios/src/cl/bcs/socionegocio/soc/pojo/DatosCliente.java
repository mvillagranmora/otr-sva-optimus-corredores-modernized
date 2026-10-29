package cl.bcs.socionegocio.soc.pojo;

import java.util.ArrayList;
import java.util.List;

import cl.bcs.utils.BcsPojo;

public class DatosCliente extends BcsPojo{
	
	
	public String 	ClienteFinal 				= "";
	public Long		CodigoEjecutivo				= 0L;
	public String	CodigoEjecutivoExterno		= "";
	public String	IdCategoriaInversionista	= "";
    public String	IdTipoCliente				= "";
    public Long		SegmentoCliente				= 0L;
    public Long		Sucursal					= 0L;
    
    public List<IndicadorPersonaCliente>	Indicador;
    public List<Requisito>			Requisito;
    public List<PortafolioCliente> 	Portafolio;
    
    public DatosCliente() {
    	Indicador = new ArrayList<IndicadorPersonaCliente>();
    	Requisito = new ArrayList<Requisito>();
    	Portafolio = new ArrayList<PortafolioCliente>();
    }
    
    
    
}
