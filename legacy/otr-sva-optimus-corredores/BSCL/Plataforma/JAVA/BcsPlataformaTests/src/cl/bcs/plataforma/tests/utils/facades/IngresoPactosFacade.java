package cl.bcs.plataforma.tests.utils.facades;

import java.sql.Connection;
import java.time.LocalDate;

import cl.bcs.mesa.MRF.CrearOperacionMesa.CrearOperacionMesa_OUT;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.tests.utils.builders.ClienteBuilder;
import cl.bcs.plataforma.tests.utils.builders.CuentaPropiaBuilder;
import cl.bcs.plataforma.tests.utils.builders.IngresarPactoBuilder;
import cl.bcs.portafolio.CrearPortafolio.SalidaCrearPortafolio;
import cl.bcs.socionegocio.cli.CrearCliente.SalidaServicioCrearCliente;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class IngresoPactosFacade {
	
	protected Connection conn;
	protected Long idEmpresa;
	
	public Long getIdEmpresa() {
		return idEmpresa;
	}
	public IngresoPactosFacade(Connection conn, Long idEmpresa){
		this.conn = conn;
		this.idEmpresa = idEmpresa;
	}

	public CrearOperacionMesa_OUT ingresarPactoCompra(String perfilComercial, Long idInstrumento) throws Exception {
		//0 - Obtener cuenta propia
		CuentaPropiaBuilder cuentaPropia = new CuentaPropiaBuilder(this.conn, idEmpresa)
				.setPortafolioNuevo(perfilComercial)
				.setMesaNueva()
				.build();
		
		//1 - obtener empresa cuenta propia
		InformacionEmpresa empresa = cuentaPropia.getEmpresa();
		//2 - crear un nuevo portafolio cuneta propia
		SalidaCrearPortafolio salidaPortafolio = cuentaPropia.getPortafolioNuevo();
		
		//3 - Crear cliente
		SalidaServicioCrearCliente cliente = new ClienteBuilder()
				.setIdEmpresa(idEmpresa)
				.setIdPerfilComercial(perfilComercial)
				.build(conn);
				
		//4 - eliminar todos los pactos de la cuenta propia desde las carteras.
		SQL.exec(conn, "DELETE FROM \"BSCL.Custodia.BD::CUS.Cartera\" WHERE \"IdTipoInstrumento\" = 'PAC'");
				
		//5 - ingresar pacto compra
		String fechaSistema = Utils.getFechaSistema(conn, Utils.getSchema(), empresa.IdEmpresa);
		LocalDate ld = LocalDate.parse(fechaSistema, Fecha.YYYYMMDD);
		
		return new IngresarPactoBuilder()
				.setIdEmpresa(empresa.IdEmpresa)
				.setIdCliente(cliente.P_OUT_REFIDCLIENTE)
				.setIdPortafolio(cliente.P_OUT_REFIDPORTAFOLIO)
				.setIdClienteCorredor(empresa.IdCliente)
				.setIdPortafolioCorredor(salidaPortafolio.p_out_RefId)
				.setIdTipoOperacion("COMPAC")
				.setFechaOperacion(fechaSistema)
				.setFechaLiquidacion(ld.plusDays(7).format(Fecha.YYYYMMDD))
				.setIdInstrumento(idInstrumento)
				.build(conn);
	}
	
	public CrearOperacionMesa_OUT ingresarPactoVenta(String perfilComercial, Long idInstrumento) throws Exception {
		//TODO IMPLEMENTAR
		return null;
	}
}
