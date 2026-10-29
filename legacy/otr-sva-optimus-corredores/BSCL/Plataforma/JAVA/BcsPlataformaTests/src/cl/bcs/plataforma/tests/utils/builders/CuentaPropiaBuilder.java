package cl.bcs.plataforma.tests.utils.builders;

import java.sql.Connection;

import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.tests.utils.builders.CrearMesaRfBuilder.SalidaCrearMesaRfBuilder;
import cl.bcs.portafolio.CrearPortafolio;
import cl.bcs.portafolio.CrearPortafolio.EntradaCrearPortafolio;
import cl.bcs.portafolio.CrearPortafolio.SalidaCrearPortafolio;

public class CuentaPropiaBuilder {

	private Connection conn;
	private InformacionEmpresa empresa;
	private SalidaCrearPortafolio portafolioNuevo;
	private SalidaCrearMesaRfBuilder mesaNueva;
	
	public CuentaPropiaBuilder(Connection conn, Long idEmpresa) throws Exception {
		this.conn = conn;
		//1 - obtener empresa cuenta propia
		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
		this.empresa = consultarEmpresaService.logica(this.conn, idEmpresa);
	}

	public CuentaPropiaBuilder setPortafolioNuevo(String perfilComercial) throws Exception {
		//2 - crear un nuevo portafolio cuneta propia
		CrearPortafolio portafolio = new CrearPortafolio();
		EntradaCrearPortafolio entradaPortafolio = new EntradaCrearPortafolio();
		entradaPortafolio.p_IdEmpresa = this.empresa.IdEmpresa;
		entradaPortafolio.p_IdUsuario = 0L;
		entradaPortafolio.p_IdModulo = "PLA";
		
		entradaPortafolio.p_IdCliente = empresa.IdCliente;
		entradaPortafolio.p_IdPerfilComercialBase = perfilComercial;
		entradaPortafolio.p_IdPersonaBrokerExtranjero = 0L;
		this.portafolioNuevo = portafolio.logica(this.conn, entradaPortafolio);
		
		return this;
	}
	
	public CuentaPropiaBuilder setMesaNueva() {
		
		this.mesaNueva = new CrearMesaRfBuilder()
			.setIdEmpresa(empresa.IdEmpresa)
			.setIdCliente(empresa.IdCliente)
			.setIdPortafolio(portafolioNuevo.p_out_RefId)
			.build(conn);
		
		return this;
	}
	
	public CuentaPropiaBuilder build() {
		return this;
	}
	
	public InformacionEmpresa getEmpresa() {
		return empresa;
	}

	public SalidaCrearPortafolio getPortafolioNuevo() {
		return portafolioNuevo;
	}
	
	public SalidaCrearMesaRfBuilder getMesaNueva() {
		return mesaNueva;
	}
}
