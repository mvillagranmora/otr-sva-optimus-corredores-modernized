package cl.bcs.custodia.prt;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.custodia.prt.CrearEmprestitoDesdeBlockChain.Usuario;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.bck.ConsultaLogBlockChain.PrestamoPrestamistaDesasignado;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class AnularEmprestitoDesdeBlockChain implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(AnularEmprestitoDesdeBlockChain.class);
	
	private final String SQL_SELECT_EMPRESTITO			= loadResourceToString("cl/bcs/custodia/sql/AnularEmprestitoDesdeBlockChain_SelectEmprestito.sql");
	private final String SQL_SELECT_USUARIO				= loadResourceToString("cl/bcs/custodia/sql/CrearEmprestitoDesdeBlockChain_SelectUsuario.sql");
	
	public static class AnularEmprestitoDesdeBlockChainEntrada extends BcsPojo {
		public Long									p_IdEmpresa;
		public Long									p_IdUsuario;
		public List<PrestamoPrestamistaDesasignado>	p_PrestamistasDesasignado; 
		public String 								p_FechaAnulacion;
	}
	
	public static class AnularEmprestitoDesdeBlockChainSalida extends BcsPojo {
		public Long 				P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		
		public AnularEmprestitoDesdeBlockChainSalida() {
			this.P_OUT_ERRORESCONTADOR 	= 0L;
			this.P_OUT_MENSAJES 		= new ArrayList<>();
		}
	}
	
	public static class Emprestito extends BcsPojo {
		public Long		FolioEmprestito;
		public String	FolioSistemaExterno;
		public String	DocumentoIdentidad;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public AnularEmprestitoDesdeBlockChainSalida logica( Connection conn, AnularEmprestitoDesdeBlockChainEntrada in ) throws Exception {
		
		AnularEmprestitoDesdeBlockChainSalida salida = new AnularEmprestitoDesdeBlockChainSalida();
		
		if ( in.p_PrestamistasDesasignado.size() == 0 ) return salida;
		
		SQL sql 									= new SQL();
		HashMap<String,Object> parametrosQuery 		= new HashMap<String,Object>();
		
		parametrosQuery.put( "p_IdEmpresa"				, in.p_IdEmpresa);
		parametrosQuery.put( "p_FoliosSistemaExterno"	, in.p_PrestamistasDesasignado.stream().map( prestamo_prestamista -> prestamo_prestamista.LAID ).collect( Collectors.toList() ) );
		List<Emprestito> emprestitos = sql.query( conn, SQL_SELECT_EMPRESTITO, parametrosQuery, Emprestito.class);
		parametrosQuery.clear();
		
		parametrosQuery.put( "p_IdEmpresa", in.p_IdEmpresa );
		parametrosQuery.put( "p_IdUsuario", in.p_IdUsuario );
		List<Usuario> usuarios = sql.query( conn, SQL_SELECT_USUARIO, parametrosQuery, Usuario.class );
		parametrosQuery.clear();
		Long idEmpleado = usuarios.get(0).IdEntidad;
		
		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresaService.logica(conn, in.p_IdEmpresa);
		Long IdPaisEmpresa = empresa.IdPais;
		
		for ( Emprestito emprestito : emprestitos ) {
			
			if ( in.p_PrestamistasDesasignado.stream()
			.anyMatch( prestamo_prestamista -> prestamo_prestamista.LAID.equals( emprestito.FolioSistemaExterno ) &&
					prestamo_prestamista.lendingClients.stream().anyMatch( lending_client -> lending_client.LendingClientRUT.equals( emprestito.DocumentoIdentidad ) ) ) ) {
				continue;
			}
			
			AnularEmprestito anularEmprestito 					= new AnularEmprestito();
			AnularEmprestito.Entrada anularEmprestitoEntrada 	= new AnularEmprestito.Entrada();
			AnularEmprestito.Salida anularEmprestitoSalida 		= new AnularEmprestito.Salida();
			
			anularEmprestitoEntrada.p_IdEmpresa 			= in.p_IdEmpresa;
			anularEmprestitoEntrada.p_IdUsuario				= in.p_IdUsuario;
			anularEmprestitoEntrada.p_IdModulo				= "CUS";
			anularEmprestitoEntrada.p_IdEmpleadoDigitador	= idEmpleado;
			anularEmprestitoEntrada.p_IdPais				= IdPaisEmpresa;
			anularEmprestitoEntrada.p_FechaAnulacion		= in.p_FechaAnulacion;
			anularEmprestitoEntrada.p_FolioEmprestito		= emprestito.FolioEmprestito;
			anularEmprestitoEntrada.p_VieneDesdeBlockChain	= "S";
			
			anularEmprestitoSalida = anularEmprestito.logica( conn, anularEmprestitoEntrada );
			
			salida.P_OUT_ERRORESCONTADOR += anularEmprestitoSalida.P_OUT_ERRORESCONTADOR;
			salida.P_OUT_MENSAJES.addAll( anularEmprestitoSalida.P_OUT_MENSAJES.stream()
					.map( mensaje_salida -> new MensajeSalida( emprestito.FolioEmprestito, mensaje_salida.tipo, mensaje_salida.codigo, mensaje_salida.descripcion ) )
					.collect( Collectors.toList() ) );
			
		}
		
		return salida;
	}
	
}
