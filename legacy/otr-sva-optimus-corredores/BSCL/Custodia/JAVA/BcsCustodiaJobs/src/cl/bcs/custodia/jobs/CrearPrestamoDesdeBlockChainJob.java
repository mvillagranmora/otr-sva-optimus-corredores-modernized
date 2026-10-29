package cl.bcs.custodia.jobs;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import cl.bcs.custodia.prt.CrearPrestamoDesdeBlockChain;
import cl.bcs.custodia.prt.CrearPrestamoDesdeBlockChain.CrearPrestamoDesdeBlockChain_Entrada;
import cl.bcs.custodia.prt.CrearPrestamoDesdeBlockChain.CrearPrestamoDesdeBlockChain_Salida;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.socionegocio.bck.ConsultaListaPorFolioBlockChain;
import cl.bcs.socionegocio.bck.ConsultaListaPorFolioBlockChain.Folio;
import cl.bcs.socionegocio.bck.ConsultaListaPorFolioBlockChain.PrestamosBlockChain;
import cl.bcs.socionegocio.bck.ConsultaLogBlockChain.PrestamoCreado;
import cl.bcs.utils.Log;
import cl.bcs.utils.Utils;

public class CrearPrestamoDesdeBlockChainJob extends BcsJob {
	private final Log log = new Log("CUS.CrearPrestamoDesdeBlockChainJob");

	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		
        Long idEmpresa 				= Long.parseLong( this.getParam("IdEmpresa") );
        Long idUsuario 				= Long.parseLong( this.getParam("IdUsuario") );
        String strModulo 			= this.getParam( "IdModulo" );
        String FolioSistemaExterno 	= this.getParam( "FolioSistemaExterno" );
        String fechaSistema 		= Utils.getFechaSistema( con, Utils.getSchema(), idEmpresa );
		
		try{
			
			ConsultaListaPorFolioBlockChain consultaPrestamoPorLAID 												= new ConsultaListaPorFolioBlockChain();
			ConsultaListaPorFolioBlockChain.ConsultaListaPorFolioBlockChain_Entrada consultaPrestamoPorLAIDEntrada 	= new ConsultaListaPorFolioBlockChain.ConsultaListaPorFolioBlockChain_Entrada();
			
			List<Folio>	folios = new ArrayList<>();
			folios.add( new Folio ( FolioSistemaExterno ) );
			
			consultaPrestamoPorLAIDEntrada.p_IdEmpresa 		= idEmpresa;
			consultaPrestamoPorLAIDEntrada.p_FolioExterno	= folios;
			ConsultaListaPorFolioBlockChain.ConsultaListaPorFolioBlockChain_Salida consultaPrestamoPorLAIDSalida = consultaPrestamoPorLAID.logica(con, consultaPrestamoPorLAIDEntrada);
			
			List<PrestamosBlockChain> prestamoBlockChain 	= consultaPrestamoPorLAIDSalida.p_lista_prestamos.stream()
					.filter( prestamo_bck -> prestamo_bck.laDetails.LAID.equals( FolioSistemaExterno ) )
					.collect( Collectors.toList() );
			
			if ( prestamoBlockChain.size() == 0 ) {
				return true;
			}
			
			PrestamoCreado prestamoCreado = new PrestamoCreado();
			prestamoCreado.laDetails 		= prestamoBlockChain.get( 0 ).laDetails;
			prestamoCreado.borrowingClients = prestamoBlockChain.get( 0 ).borrowingClients;
			
			CrearPrestamoDesdeBlockChain objCrearPrestamoDesdeBlockChain 					= new CrearPrestamoDesdeBlockChain();
			CrearPrestamoDesdeBlockChain_Entrada objCrearPrestamoDesdeBlockChain_Entrada 	= new CrearPrestamoDesdeBlockChain_Entrada();
			
			objCrearPrestamoDesdeBlockChain_Entrada.p_IdEmpresa			= idEmpresa;
			objCrearPrestamoDesdeBlockChain_Entrada.p_IdUsuario			= idUsuario;
			objCrearPrestamoDesdeBlockChain_Entrada.p_IdModulo 			= strModulo;
			objCrearPrestamoDesdeBlockChain_Entrada.p_PrestamosCreado.add( prestamoCreado );
			
			CrearPrestamoDesdeBlockChain_Salida objCrearPrestamoDesdeBlockChain_Salida = objCrearPrestamoDesdeBlockChain.logica( con, objCrearPrestamoDesdeBlockChain_Entrada );
			
			if( objCrearPrestamoDesdeBlockChain_Salida.P_OUT_ERRORESCONTADOR.equals(0L) ) {
				con.commit();
			} else {
				con.rollback();
				//lleno tabla con registros de prestamos que deben ser procesados manualmente
				if( objCrearPrestamoDesdeBlockChain_Salida.P_OUT_ERRORESCONTADOR > 0 ) {
					objCrearPrestamoDesdeBlockChain.AgregaATablaPrestamosPendientesPorCargar( con, objCrearPrestamoDesdeBlockChain_Salida.P_OUT_PRESTAMOBLOCKCHAIN, "PRT", objCrearPrestamoDesdeBlockChain_Salida.P_OUT_MENSAJES, fechaSistema );
				}
			}
			
		return true;
		
		} catch ( Exception e ) {
			log.write(e);
			con.rollback();
			throw new Throwable(e);
		}
	}
	@Override
	public void onError(Throwable t) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}
	
	
}
