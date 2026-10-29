package cl.bcs.socionegocio.jobs;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import cl.bcs.scheduler.BcsJob;
import cl.bcs.socionegocio.cli.GenerarReporteConsultaAvanzadaCliente;
import cl.bcs.socionegocio.cli.GenerarReporteConsultaAvanzadaCliente.InConsultaSocioNegocio;
import cl.bcs.socionegocio.cli.GenerarReporteConsultaAvanzadaCliente.OutConsultaSocioNegocio;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class GenerarReporteConsultaAvanzadaClienteJob extends BcsJob implements JavaCall{
	
private final Log log = new Log("SOC.GenerarReporteConsultaAvanzadaClienteJob"); 
	
	private final String SQL_UPDATE_ID_PROGRAMA = loadResourceToString("cl/bcs/socionegocio/jobs/sql/GenerarReporteConsultaAvanzadaCliente_Update.sql");
	
	public static class ActualizarPrograma{
		public String RutaSalida;
		public Long IdPrograma;
		
		public ActualizarPrograma(String RutaSalida,Long IdPrograma){
			this.RutaSalida = RutaSalida;
			this.IdPrograma = IdPrograma;
		}
	}
	

	
	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		
		GenerarReporteConsultaAvanzadaCliente consultaAvanzada = new GenerarReporteConsultaAvanzadaCliente();
		InConsultaSocioNegocio entrada = new InConsultaSocioNegocio();
		
		
		Long    parIdEmpresa=Long.parseLong(this.getParam("p_IdEmpresa"));
		Long    parIdUsuario=Long.parseLong(this.getParam("p_IdUsuario"));
		String  parIdModulo = this.getParam("p_IdModulo");
		Long    parIdEmpleadoConsulta=Long.parseLong(this.getParam("p_IdEmpleadoConsulta"));
		
		String  parNombre = this.getParam("p_Nombre");
		String  parDocumentoIdentidad = this.getParam("p_DocumentoIdentidad");
		String  parIdTipoPersona = this.getParam("p_IdTipoPersona");
		String  parIdTipoCliente = this.getParam("p_IdTipoCliente");
		Long    parIdSegmentoCliente=this.getParam("p_IdSegmentoCliente") != null ? Long.parseLong(this.getParam("p_IdSegmentoCliente")) : null;
		String  parIdEstadoCliente = this.getParam("p_IdEstadoCliente");
		String  parIdTipoBloqueoCliente = this.getParam("p_IdTipoBloqueoCliente");
		String  parIdTipoBloqueoPortafolio = this.getParam("p_IdTipoBloqueoPortafolio");
		Long    parIdEmpleadoFiltro=this.getParam("p_IdEmpleadoFiltro") != null ? Long.parseLong(this.getParam("p_IdEmpleadoFiltro")) : null;
		String  parIdCategoriaInversionista = this.getParam("p_IdCategoriaInversionista");
		String  parIdPropositoPortafolio = this.getParam("p_IdPropositoPortafolio");
		String  parIdEstadoPortafolio = this.getParam("p_IdEstadoPortafolio");
		String  parIdPerfilComercial = this.getParam("p_IdPerfilComercial");
		String  parIdPerfilProductos = this.getParam("p_IdPerfilProductos");
		String  parIdPerfilCobros = this.getParam("p_IdPerfilCobros");
		String  parFechaNacimientoDesde = this.getParam("p_FechaNacimientoDesde");
		String  parFechaNacimientoHasta = this.getParam("p_FechaNacimientoHasta");
		String  parFechaCreacionDesde = this.getParam("p_FechaCreacionDesde");
		String  parFechaCreacionHasta = this.getParam("p_FechaCreacionHasta");
		String  parFechaDefuncionDesde = this.getParam("p_FechaDefuncionDesde");
		String  parFechaDefuncionHasta = this.getParam("p_FechaDefuncionHasta");
		String  parAliasPortafolio = this.getParam("p_AliasPortafolio");
		Long    parIdPersonaBancoCustodio=this.getParam("p_IdPersonaBancoCustodio") != null ? Long.parseLong(this.getParam("p_IdPersonaBancoCustodio")) : null;
		Long    parIdPersonaBrokerExtranjero=this.getParam("p_IdPersonaBrokerExtranjero") != null ? Long.parseLong(this.getParam("p_IdPersonaBrokerExtranjero")) : null;
		String  parBuscaPorFlujoCaja = this.getParam("p_BuscaPorFlujoCaja");
		String  parUsoFlujoCaja = this.getParam("p_UsoFlujoCaja");
		Long    parSaldoFlujoCaja=this.getParam("p_SaldoFlujoCaja") != null ? Long.parseLong(this.getParam("p_SaldoFlujoCaja")) : null;
		String  parIdMonedaFlujoCaja = this.getParam("p_IdMonedaFlujoCaja");
		String  parCriterioFlujoCaja = this.getParam("p_CriterioFlujoCaja");
		String  parBuscaPorFlujoCustodia = this.getParam("p_BuscaPorFlujoCustodia");
		String  parTipoOperacionCustodia = this.getParam("p_TipoOperacionCustodia");
		Long    parSaldoFlujoCustodia=this.getParam("p_SaldoFlujoCustodia") != null ? Long.parseLong(this.getParam("p_SaldoFlujoCustodia")) : null;
		String  parCriterioFlujoCustodia = this.getParam("p_CriterioFlujoCustodia");
		Long    parIdInstrumento=this.getParam("p_SaldoFlujoCustodia") != null ? Long.parseLong(this.getParam("p_IdInstrumento")) :null;
				
		//entrada de parametros
		entrada.p_IdEmpresa				=	parIdEmpresa;
		entrada.p_IdUsuario				=	parIdUsuario;
		entrada.p_IdModulo				=	parIdModulo;
		entrada.p_IdEmpleadoConsulta	=	parIdEmpleadoConsulta;
		entrada.p_Nombre				=	parNombre;
		entrada.p_DocumentoIdentidad	=	parDocumentoIdentidad;
		entrada.p_IdTipoPersona			=	parIdTipoPersona;
		entrada.p_IdTipoCliente			=	parIdTipoCliente;
		entrada.p_IdSegmentoCliente		=	parIdSegmentoCliente;
		entrada.p_IdEstadoCliente		=	parIdEstadoCliente;
		entrada.p_IdTipoBloqueoCliente	=	parIdTipoBloqueoCliente;
		entrada.p_IdTipoBloqueoPortafolio	=	parIdTipoBloqueoPortafolio;
		entrada.p_IdEmpleadoFiltro			=	parIdEmpleadoFiltro;
		entrada.p_IdCategoriaInversionista  =	parIdCategoriaInversionista;
		entrada.p_IdPropositoPortafolio		=	parIdPropositoPortafolio;
		entrada.p_IdEstadoPortafolio		=	parIdEstadoPortafolio;
		entrada.p_IdPerfilComercial			=	parIdPerfilComercial;
		entrada.p_IdPerfilProductos			=	parIdPerfilProductos;
		entrada.p_IdPerfilCobros			=	parIdPerfilCobros;
		entrada.p_FechaNacimientoDesde		=	parFechaNacimientoDesde;
		entrada.p_FechaNacimientoHasta		=	parFechaNacimientoHasta;
		entrada.p_FechaCreacionDesde		=	parFechaCreacionDesde;
		entrada.p_FechaCreacionHasta		=	parFechaCreacionHasta;
		entrada.p_FechaDefuncionDesde		=	parFechaDefuncionDesde;
		entrada.p_FechaDefuncionHasta		=	parFechaDefuncionHasta;
		entrada.p_AliasPortafolio			=	parAliasPortafolio;
		entrada.p_IdPersonaBancoCustodio	=	parIdPersonaBancoCustodio;
		entrada.p_IdPersonaBrokerExtranjero =	parIdPersonaBrokerExtranjero;
		entrada.p_BuscaPorFlujoCaja			=	parBuscaPorFlujoCaja;
		entrada.p_UsoFlujoCaja				=	parUsoFlujoCaja;
		entrada.p_SaldoFlujoCaja			=	parSaldoFlujoCaja;
		entrada.p_IdMonedaFlujoCaja			=	parIdMonedaFlujoCaja	;
		entrada.p_CriterioFlujoCaja			=	parCriterioFlujoCaja	;
		entrada.p_BuscaPorFlujoCustodia		=	parBuscaPorFlujoCustodia;
		entrada.p_TipoOperacionCustodia		=	parTipoOperacionCustodia;
		entrada.p_SaldoFlujoCustodia		=	parSaldoFlujoCustodia;
		entrada.p_CriterioFlujoCustodia		=	parCriterioFlujoCustodia	;
		entrada.p_IdInstrumento				=	parIdInstrumento;
		entrada.p_Ruta						=	Utils.getConfiguracion(con, Utils.getSchema(), parIdEmpresa, "PLA.RutaBaseArchivos");
		
		
		
		try{
			OutConsultaSocioNegocio out=consultaAvanzada.logica(con,entrada);
			SQL sql=new SQL();
			if(new Long(0).compareTo(out.p_out_ErroresContador) == 0){
				List<ActualizarPrograma> actualizar = new ArrayList<ActualizarPrograma>();
				actualizar.add(new ActualizarPrograma(out.p_out_NombreArchivo,this.getJobId()));
				sql.batch(con,SQL_UPDATE_ID_PROGRAMA , actualizar,ActualizarPrograma.class);
				con.commit();
				return true;
			}else{
				return false;
			}
		}catch(Exception e){
			con.rollback();
			e.printStackTrace();
			throw e;
		}
	}

	@Override
	public void onError(Throwable t) {
		log.write("****** ERROR  *******");
        log.write(t);
        t.printStackTrace();
		
	}

	@Override
	public void pause() {
		
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		
		return null;
	}

	
}