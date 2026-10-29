package cl.bcs.custodia.prt;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.prt.ConsultarPrestamistasDisponibles.ConsultarPrestamistasDisponibles_IN;
import cl.bcs.custodia.prt.ConsultarPrestamistasDisponibles.ConsultarPrestamistasDisponibles_OUT;
import cl.bcs.custodia.prt.ConsultarPrestamistasDisponibles.RegistroSalida;
import cl.bcs.custodia.prt.CrearEmprestito.CrearEmprestito_Entrada;
import cl.bcs.custodia.prt.CrearEmprestito.CrearEmprestito_Salida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.bck.ConsultaListaPorFolioBlockChain.PrestamosBlockChain;
import cl.bcs.socionegocio.bck.ConsultaListaPorFolioBlockChain.lendingClients;
import cl.bcs.socionegocio.bck.ConsultaLogBlockChain.PrestamoAsignado;
import cl.bcs.socionegocio.bck.ProcesaLogBlockChain.Instrumento;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearEmprestitoDesdeBlockChain implements JavaCall
{
	//-------------------------------------------------
	// QUERIES
	//-------------------------------------------------
	private final String SQL_SELECT_EMPRESTITO			= loadResourceToString("cl/bcs/custodia/sql/CrearEmprestitoDesdeBlockChain_SelectEmprestito.sql");
	private final String SQL_SELECT_IDCLIENTE			= loadResourceToString("cl/bcs/custodia/sql/CrearEmprestitoDesdeBlockChain_SelectPersonaCliente.sql");
	private final String SQL_SELECT_PRESTAMO			= loadResourceToString("cl/bcs/custodia/sql/CrearEmprestitoDesdeBlockChain_SelectPrestamo.sql");
	private final String SQL_SELECT_USUARIO				= loadResourceToString("cl/bcs/custodia/sql/CrearEmprestitoDesdeBlockChain_SelectUsuario.sql");
	private final String MERCADO_PRESTAMOS_BLOCKCHAIN	= "RV"; //TODO: segun CELZORRO-284 solo se traen desde BLOCKCHAIN los prestamos cuyo mercado sea "RV" (renta variable)
	
	//-------------------------------------------------
	// CLASES DE ENTRADA 
	//-------------------------------------------------

	public static class CrearEmprestitoDesdeBlockChain_Entrada extends BcsPojo {
		public Long							p_IdEmpresa				;
		public Long							p_IdUsuario				;
		public String						p_IdModulo				;
		public List<PrestamoAsignado>		p_FoliosBlockChain		;
	}
	//-------------------------------------------------
	// CLASES DE SALIDA 
	//-------------------------------------------------
	public static class CrearEmprestitoDesdeBlockChain_Salida
	{
		public List<MensajeSalida>			P_OUT_MENSAJES					;
		public Long							P_OUT_ERRORESCONTADOR			;
		public List<String>					P_OUT_RESULTADO					; 
		public List<PrestamosBlockChain>	P_OUT_PRESTAMOBLOCKCHAIN		; //retorna un objeto con los datos del prestamo BlockChain que no se pudocrear
		
		public CrearEmprestitoDesdeBlockChain_Salida()
		{	
			this.P_OUT_MENSAJES 			= new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR		= 0L;
			this.P_OUT_RESULTADO 			= new ArrayList<>();
			this.P_OUT_PRESTAMOBLOCKCHAIN	= new ArrayList<>();
		}
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}

	}

	//-------------------------------------------------
	// CLASES UTILITARIAS 
	//-------------------------------------------------
	
	public static class Prestamo {
		public Long			FolioPrestamo;
		public Long			FolioComprobante;
		public String		NombreEstado;
		public String		IdEstado;
		public String		IdTipoPrestamo;
		public String		NombreTipoPrestamo;
		public String		DocumentoIdentidad;
		public String		CodigoPortafolio;
		public String		RazonSocial;
		public String		IdBolsa;
		public String		NombreBolsa;
		public String		NemoSubyacente;
		public String		FechaPrestamo;
		public String		FechaLiquidacion;
		public String		FechaVencimientoPrestamo;
		public Long			PlazoEnDiasOperacion;
		public BigDecimal	INGCantidad;
		public BigDecimal	INGPrecioInstrumento;
		public BigDecimal	INGMontoInstrumento;
		public BigDecimal	INGPrecioMercadoInstrumento;
		public BigDecimal	INGMontoMercadoInstrumento;
		public BigDecimal	INGTasaPrestamo;
		public BigDecimal	MontoPrimaOriginal;
		public BigDecimal	INGMontoPrima;
		public BigDecimal	INGMontoTotalPrestamo;
		public BigDecimal	CantidadAsignada;
		public BigDecimal	CantidadPorAsignar;
		public String		INGFechaOperacion;
		public String		IdClasificacionTipoInstrumentoSubyacente;
		public BigDecimal	INGMontoInteresCorridoCompraActual;
		public BigDecimal	INGMontoInteresCompraProximo;
		public String		EstadoAnticipo;
		public String		EstadoLiquidacion;
		public String		IdCondicionLiquidacion;
		public Long			IdInstrumentoSubyacente;
		public BigDecimal	INGTasaInstrumento;
		public Long			IdAgente;
		public Long			IdInstrumento;
		public String		NemotecnicoPrestamo;
		public String		IdTipoInstrumento;
		public String 		IdTipoCanalIngreso;
		public String		IdMonedaSistema;
		public String		TipoCustodia;
		public String		ExigirGarantia;
		public String		IdTipoInstrumentoSubyacente;
		public String		FolioSistemaExterno;
	}
	
	
	public static class Emprestito
	{
		public Long 		FolioEmprestito						;
		public Long 		FolioPrestamo						;
		public String 		FolioSistemaExterno					;
		public String 		ExigirGarantia						;
		public String 		IdEstado							;
		public String 		NombreEstado						;
		public String 		DocumentoIdentidad					;
		public String 		NemoSubyacente						;
		public Long 		CodigoPortafolio					;
		public String 		RazonSocial							;
		public Long 		PlazoEnDiasOperacion				;
		public String 		FechaLiquidacion					;
		public String 		FechaEmprestito						;
		public String 		INGFechaOperacion					;
		public String 		FechaVencimientoEmprestito			;
		public String 		FechaInicioEmprestito				;
		public BigDecimal 	INGCantidad							;
		public BigDecimal 	INGPrecioInstrumento				;
		public BigDecimal 	INGMontoInstrumento					;
		public BigDecimal 	INGMontoTotalEmprestito				;
		public BigDecimal 	INGTasaEmprestito					;
		public BigDecimal 	INGMontoPremio						;					
		public BigDecimal 	INGMontoInteresCorridoCompraActual	;
		public BigDecimal 	INGMontoInteresCompraProximo		;
		
	}
	
	public static class PrestamistasDisponibles
	{
		public List<RegistroSalida> lstPrestamistasDisponibles = new ArrayList<>();
	}

	public static class Usuario
	{
		public Long IdEntidad	= 0L;
	}
	
	public static class Cliente extends BcsPojo {
		public String	DocumentoIdentidad;
		public Long 	IdCliente;
	}
	
	//-------------------------------------------------------------------------------------------
	// Procesa uno a uno los prestamistas (emprestitos) de un prestamo BlockChain
	//-------------------------------------------------------------------------------------------
	public CrearEmprestitoDesdeBlockChain_Salida logica(Connection conn, CrearEmprestitoDesdeBlockChain_Entrada in) throws Exception {
		
		CrearEmprestitoDesdeBlockChain_Salida salida		= new CrearEmprestitoDesdeBlockChain_Salida();
		
		if ( in.p_FoliosBlockChain.size() == 0 ) {
			return salida;
		}
		
		SQL sql 											= new SQL();
		PrestamistasDisponibles objPrestamistasDisponibles	= new PrestamistasDisponibles();
		Long lngIdClientePrestamo							= 0L;
		Long lngIdEmpleado									= 0L;
		
		HashMap<String, Object> hparam 					= new HashMap<String, Object>();
		HashMap<String, Object> hClientesPrestatario 	= new HashMap<String, Object>();
		HashMap<String, Object> hClientesPrestamista 	= new HashMap<String, Object>();
		HashMap<String, Object> hPrestamos 				= new HashMap<String, Object>();
		HashMap<String, Object> hEmprestitos			= new HashMap<String, Object>();
		
		hparam.put( "p_IdEmpresa"			, in.p_IdEmpresa );
		hparam.put( "p_DocumentoIdentidad"	, in.p_FoliosBlockChain.stream().map( prestamo_asignacion -> prestamo_asignacion.borrowingClients.BorrowingClientRUT ).distinct().collect( Collectors.toList() ) );
		List<Cliente> clientesPrestatario = sql.query( conn, SQL_SELECT_IDCLIENTE, hparam, Cliente.class );
		hparam.clear();
		hClientesPrestatario = Utils.List2Hash( clientesPrestatario, "DocumentoIdentidad" );
		
		hparam.put( "p_IdEmpresa", in.p_IdEmpresa );
		hparam.put( "p_IdUsuario", in.p_IdUsuario );
		List<Usuario> usuarios = sql.query( conn, SQL_SELECT_USUARIO, hparam, Usuario.class );
		hparam.clear();
		lngIdEmpleado = usuarios.get(0).IdEntidad;
		
		List<String> prestamistasDocumentoIdentidad = new ArrayList<>();
		in.p_FoliosBlockChain.stream()
		.forEach( prestamo -> {
			prestamo.lendingClients.forEach( lending_client -> { 
				if ( !prestamistasDocumentoIdentidad.contains( lending_client.LendingClientRUT ) ) {
					prestamistasDocumentoIdentidad.add( lending_client.LendingClientRUT );
				}
			} );
		} );
		hparam.put( "p_IdEmpresa"			, in.p_IdEmpresa );
		hparam.put( "p_DocumentoIdentidad"	, prestamistasDocumentoIdentidad );
		List<Cliente> clientesPrestamista = sql.query( conn, SQL_SELECT_IDCLIENTE, hparam, Cliente.class );
		hparam.clear();
		hClientesPrestamista = Utils.List2Hash( clientesPrestamista, "DocumentoIdentidad" );
		
		CrearPrestamoDesdeBlockChain crearPrestamoDesdeBlockChain = new CrearPrestamoDesdeBlockChain();	
		List<Instrumento> instrumentos = crearPrestamoDesdeBlockChain.ObtieneInstrumentos( conn, in.p_FoliosBlockChain.stream().map( prestamo_asignacion -> prestamo_asignacion.laDetails.Symbol ).distinct().collect( Collectors.toList() ) );
		
		ConsultarEmpresa consultarEmpresaService 	= new ConsultarEmpresa();
		InformacionEmpresa empresa 					= consultarEmpresaService.logica( conn, in.p_IdEmpresa );
		Long IdPaisEmpresa 							= empresa.IdPais;
		
		List<Prestamo> prestamos = ObtienePrestamos( conn, in.p_IdEmpresa, in.p_FoliosBlockChain.stream().map( prestamo_asignado -> prestamo_asignado.laDetails.LAID ).collect( Collectors.toList() ) );
		hPrestamos = Utils.List2Hash( prestamos, "FolioSistemaExterno" );
		
		for ( PrestamoAsignado prestamoAsignado : in.p_FoliosBlockChain ) {
			
			hEmprestitos.clear();
			PrestamosBlockChain prestamoBlockChainPendientePorCargar = new PrestamosBlockChain();
			
			Prestamo prestamoEnOptimus 	= ( Prestamo ) hPrestamos.get( prestamoAsignado.laDetails.LAID );
			Cliente clientePrestatario 	= ( Cliente ) hClientesPrestatario.get( prestamoAsignado.borrowingClients.BorrowingClientRUT );
			
			if ( prestamoEnOptimus == null ) {
				String msg = Diccionario.getMensaje( conn, in.p_IdEmpresa, "BC_PRESTAMO_BLOCKCHAIN_NO_ESTA_EN_OPTIMUS" )
					      .add( "FolioSistemaExterno", prestamoAsignado.laDetails.LAID )
					      .msg();
				salida.P_OUT_MENSAJES.add( new MensajeSalida( Long.valueOf( prestamoAsignado.laDetails.LegacyLAID ), MensajeSalida.MENSAJE_ERROR, "BC_PRESTAMO_BLOCKCHAIN_NO_ESTA_EN_OPTIMUS", msg ) );
				prestamoBlockChainPendientePorCargar.laDetails 			= prestamoAsignado.laDetails;
				prestamoBlockChainPendientePorCargar.borrowingClients 	= prestamoAsignado.borrowingClients;
				salida.P_OUT_PRESTAMOBLOCKCHAIN.add( prestamoBlockChainPendientePorCargar );
				continue;
			}
			
			if ( clientePrestatario == null ) {
				String msg = Diccionario.getMensaje( conn, in.p_IdEmpresa, "BC_CLIENTE_NO_EXISTE" )
					      .add( "DocumentoIdentidad", prestamoAsignado.borrowingClients.BorrowingClientRUT )
					      .msg();
				salida.P_OUT_MENSAJES.add( new MensajeSalida( Long.valueOf( prestamoAsignado.laDetails.LegacyLAID ), MensajeSalida.MENSAJE_ERROR, "BC_CLIENTE_NO_EXISTE", msg ) );
				prestamoBlockChainPendientePorCargar.laDetails 			= prestamoAsignado.laDetails;
				prestamoBlockChainPendientePorCargar.borrowingClients 	= prestamoAsignado.borrowingClients;
				salida.P_OUT_PRESTAMOBLOCKCHAIN.add( prestamoBlockChainPendientePorCargar );
				continue;
			}
			lngIdClientePrestamo		= clientePrestatario.IdCliente;
			
			List<Instrumento> instrumento = instrumentos.stream()
					.filter( intrumento_prestamo -> intrumento_prestamo.Nemotecnico.trim().equals( prestamoAsignado.laDetails.Symbol.trim() ) )
					.collect( Collectors.toList() );
			
			if ( instrumento.size() == 0 ) {
				String msg=Diccionario.getMensaje(conn, in.p_IdEmpresa, "BC_INSTRUMENTO_NO_ENCONTRADO")
					      .add("IdInstrumento", prestamoAsignado.laDetails.Symbol)
						  .msg();
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"INSTRUMENTO_NO_EXISTE",msg));
				prestamoBlockChainPendientePorCargar.laDetails 			= prestamoAsignado.laDetails;
				prestamoBlockChainPendientePorCargar.borrowingClients 	= prestamoAsignado.borrowingClients;
				salida.P_OUT_PRESTAMOBLOCKCHAIN.add( prestamoBlockChainPendientePorCargar );
				continue;
			}
			
			if ( lngIdEmpleado == 0L ) {
				salida.P_OUT_MENSAJES.add( new MensajeSalida( 0L, MensajeSalida.MENSAJE_ERROR, "BC_EMPLEADO_NO_EXISTE", new HashMap<String, String>(), conn, in.p_IdEmpresa) );
				prestamoBlockChainPendientePorCargar.laDetails 			= prestamoAsignado.laDetails;
				prestamoBlockChainPendientePorCargar.borrowingClients 	= prestamoAsignado.borrowingClients;
				salida.P_OUT_PRESTAMOBLOCKCHAIN.add( prestamoBlockChainPendientePorCargar );
				continue;
			}
			
			hparam.put("p_IdEmpresa"			, in.p_IdEmpresa );
			hparam.put("p_FolioSistemaExterno"	, prestamoAsignado.laDetails.LAID );
			hparam.put("p_DocumentoIdentidad"	, prestamoAsignado.lendingClients.stream().map( lending_client -> lending_client.LendingClientRUT ).collect( Collectors.toList() ) );
			List<Emprestito> emprestitosPrestamo = sql.query(conn, SQL_SELECT_EMPRESTITO, hparam, Emprestito.class);
			hparam.clear();
			hEmprestitos = Utils.List2Hash( emprestitosPrestamo, "DocumentoIdentidad" );
			
			for ( lendingClients lendingClient : prestamoAsignado.lendingClients ) {
				
				int intIndexPrestamistasDisp 	= -1;
				Cliente clientePrestamista 		= ( Cliente ) hClientesPrestamista.get( lendingClient.LendingClientRUT );
				Emprestito emprestitoPrestamo	= ( Emprestito ) hEmprestitos.get( lendingClient.LendingClientRUT );
				
				if ( emprestitoPrestamo != null ) continue;
				
				if ( clientePrestamista == null ) {
					String msg = Diccionario.getMensaje( conn, in.p_IdEmpresa, "BC_CLIENTE_NO_EXISTE" )
						      .add( "DocumentoIdentidad", lendingClient.LendingClientRUT )
						      .msg();
					salida.P_OUT_MENSAJES.add( new MensajeSalida( 0L, MensajeSalida.MENSAJE_ERROR, "BC_CLIENTE_NO_EXISTE", msg ) );
					prestamoBlockChainPendientePorCargar.laDetails 			= prestamoAsignado.laDetails;
					prestamoBlockChainPendientePorCargar.borrowingClients 	= prestamoAsignado.borrowingClients;
					salida.P_OUT_PRESTAMOBLOCKCHAIN.add( prestamoBlockChainPendientePorCargar );
					continue;
				}
				
				intIndexPrestamistasDisp = intPrestamistaEstaDisponible( conn, in.p_IdEmpresa, in.p_IdUsuario, IdPaisEmpresa, instrumento.get( 0 ), lngIdClientePrestamo, lendingClient.LendingClientRUT, objPrestamistasDisponibles );
				if ( intIndexPrestamistasDisp == -1 ) {
					
					String msg=Diccionario.getMensaje( conn, in.p_IdEmpresa,"BC_PRESTAMISTA_BLOCKCHAIN_NO_DISPONIBLES_EN_OPTIMUS" )
						      .add( "DocumentoIdentidad", lendingClient.LendingClientRUT )
						      .add( "Nemo"				, prestamoAsignado.laDetails.Symbol )
							  .msg();
					salida.P_OUT_MENSAJES.add( new MensajeSalida( 0L, MensajeSalida.MENSAJE_ERROR, "BC_PRESTAMISTA_BLOCKCHAIN_NO_DISPONIBLES_EN_OPTIMUS", msg ) );
					prestamoBlockChainPendientePorCargar.laDetails 			= prestamoAsignado.laDetails;
					prestamoBlockChainPendientePorCargar.borrowingClients 	= prestamoAsignado.borrowingClients;
					salida.P_OUT_PRESTAMOBLOCKCHAIN.add( prestamoBlockChainPendientePorCargar );
					continue;
				}
				
				if ( !CrearEmprestitoUnoAUno( conn, in, lendingClient, prestamoEnOptimus, objPrestamistasDisponibles.lstPrestamistasDisponibles.get( intIndexPrestamistasDisp ), lngIdEmpleado, salida ) ) {
					prestamoBlockChainPendientePorCargar.laDetails 			= prestamoAsignado.laDetails;
					prestamoBlockChainPendientePorCargar.borrowingClients 	= prestamoAsignado.borrowingClients;
					salida.P_OUT_PRESTAMOBLOCKCHAIN.add( prestamoBlockChainPendientePorCargar );
				} else {
					salida.P_OUT_RESULTADO.add( prestamoAsignado.laDetails.LAID );
				}
				
			}
			
		}
		
	
		return salida;
	}	
	//-------------------------------------------------------------------------------------------
	// VALIDA PRESTAMISTA BLOCKCHAIN SI ES DE LOS DISPONIBLES EN OPTIMUS PARA EL INSTRUMENTO INGRESADO
	//-------------------------------------------------------------------------------------------
	public int intPrestamistaEstaDisponible(Connection conn, Long p_IdEmpresa, Long p_IdUsuario, Long p_IdPais, Instrumento p_objInstrumento, Long p_lngIdClientePrestamo, String p_strRutBC,  PrestamistasDisponibles p_objPrestamistasDisponibles ) throws Exception {
		int intRetorno = -1;
		
		ConsultarPrestamistasDisponibles objConsultarPrestamista = new ConsultarPrestamistasDisponibles();
		
		ConsultarPrestamistasDisponibles_IN objConsultarPrestamista_in = new  ConsultarPrestamistasDisponibles_IN();
		
		//viene con datos ya llenos de los prestamistas disponibles en Optimus, si es asi no llama al servicio de nuevo
		if(p_objPrestamistasDisponibles.lstPrestamistasDisponibles.size()==0)
		{
			objConsultarPrestamista_in.p_IdEmpresa						= p_IdEmpresa;
			objConsultarPrestamista_in.p_IdUsuario						= p_IdUsuario;
			objConsultarPrestamista_in.p_IdModulo						= "CUS";
			objConsultarPrestamista_in.p_IdPais							= p_IdPais;
			objConsultarPrestamista_in.p_IdClasificacionTipoInstrumento	= MERCADO_PRESTAMOS_BLOCKCHAIN;
			objConsultarPrestamista_in.p_IdTipoInstrumento				= p_objInstrumento.IdTipoInstrumento; // "ACN";
			objConsultarPrestamista_in.p_IdInstrumento					= p_objInstrumento.IdInstrumento;
			objConsultarPrestamista_in.p_CantidadMinima 				= new BigDecimal(1);
			objConsultarPrestamista_in.p_FechaConsulta					= ConfiguracionSistema.getParametroConfiguracionString(conn, p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			objConsultarPrestamista_in.p_IdClienteExcluido				= p_lngIdClientePrestamo;
			
			ConsultarPrestamistasDisponibles_OUT objConsultarPrestamista_out = objConsultarPrestamista.logica(conn, objConsultarPrestamista_in);
			
			p_objPrestamistasDisponibles.lstPrestamistasDisponibles = objConsultarPrestamista_out.P_OUT_DISPONIBLES;
		}
		
		//no puedo pasar la lista de prestamistas disponibles a Hash por que por temas de Portafolio puede estar mas de una vez el RUT
		//asi que lo recorro elemento a elemento
		int intIndex = -1;
		for(RegistroSalida objItem : p_objPrestamistasDisponibles.lstPrestamistasDisponibles)
		{
			++intIndex;
			if(objItem.DocumentoIdentidad.equals(p_strRutBC))
			{
				intRetorno = intIndex;
				break;
			}
		}
		return intRetorno;
		
	}
	
	
	//-------------------------------------------------------------------------------------------
	// CREACION DE EMPRESTITOS EN OPTIMUS
	//-------------------------------------------------------------------------------------------
	public boolean CrearEmprestitoUnoAUno(Connection conn, CrearEmprestitoDesdeBlockChain_Entrada in, lendingClients p_objPrestamistaBC, Prestamo p_objPrestamo, RegistroSalida p_objPrestamistaDisponible, Long p_lngIdEmpleado, CrearEmprestitoDesdeBlockChain_Salida salida) throws Exception
	{
		
		boolean blnRetorno = false;
				
		CrearPrestamoDesdeBlockChain objPrestamoDesdeBC = new CrearPrestamoDesdeBlockChain();
		
		//hacer calculos previos antes de crear el prestamo, esto depende de la cantidad que aporta el prestamista
		
		//calula Monto
		BigDecimal bdcMonto = objPrestamoDesdeBC.Monto(conn, in.p_IdEmpresa, in.p_IdUsuario, new BigDecimal(p_objPrestamistaBC.LendingClientQty), p_objPrestamo.IdTipoInstrumentoSubyacente, p_objPrestamo.INGPrecioInstrumento);
		BigDecimal bdcMontoAPlazo = objPrestamoDesdeBC.calcularMontoPlazo(bdcMonto, p_objPrestamo.PlazoEnDiasOperacion.toString(), p_objPrestamo.INGTasaPrestamo.toString());
		BigDecimal bdcMontoPremio = bdcMontoAPlazo.subtract(bdcMonto);
		
		CrearEmprestito objAsignarPrestamista = new CrearEmprestito();
					
		CrearEmprestito_Entrada objAsignarPrestamista_Entrada = new CrearEmprestito_Entrada();
		
		objAsignarPrestamista_Entrada.p_IdEmpresa 					= in.p_IdEmpresa;
		objAsignarPrestamista_Entrada.p_IdUsuario					= in.p_IdUsuario;
		objAsignarPrestamista_Entrada.p_IdModulo					= in.p_IdModulo;
		objAsignarPrestamista_Entrada.p_UsaReglasNegocio			= "N";
		objAsignarPrestamista_Entrada.p_IdClientePrestamista		= p_objPrestamistaDisponible.IdCliente;
		objAsignarPrestamista_Entrada.p_IdPortafolioPrestamista		= p_objPrestamistaDisponible.IdPortafolio;
		objAsignarPrestamista_Entrada.p_FechaMovimiento				= p_objPrestamo.FechaPrestamo;
		objAsignarPrestamista_Entrada.p_FechaVencimiento			= p_objPrestamo.FechaVencimientoPrestamo;
		objAsignarPrestamista_Entrada.p_FechaLiquidacion			= p_objPrestamo.FechaLiquidacion;
		objAsignarPrestamista_Entrada.p_IdCondicionDeLiquidacion	= p_objPrestamo.IdCondicionLiquidacion; // "PH"; //p_Prestamo.p_;
		objAsignarPrestamista_Entrada.p_IdInstrumento				= p_objPrestamo.IdInstrumentoSubyacente; // 0L; //IdInstrumento Subyacente
		objAsignarPrestamista_Entrada.p_Nemo						= p_objPrestamo.NemoSubyacente;
		objAsignarPrestamista_Entrada.p_Cantidad					= new BigDecimal(p_objPrestamistaBC.LendingClientQty);
		objAsignarPrestamista_Entrada.p_Precio						= p_objPrestamo.INGPrecioInstrumento;
		objAsignarPrestamista_Entrada.p_TasaInstrumento				= p_objPrestamo.INGTasaInstrumento;
		objAsignarPrestamista_Entrada.p_Monto						= bdcMonto;
		objAsignarPrestamista_Entrada.p_MontoPremio					= bdcMontoPremio;
		objAsignarPrestamista_Entrada.p_TasaPremio					= p_objPrestamo.INGTasaPrestamo;
		objAsignarPrestamista_Entrada.p_MontoAPlazo					= bdcMontoAPlazo;
		objAsignarPrestamista_Entrada.p_FolioCarteraPrestamo		= p_objPrestamo.FolioPrestamo;
		objAsignarPrestamista_Entrada.p_PrestaTitulosNominales		= "N";
		objAsignarPrestamista_Entrada.p_IdAgente					= p_objPrestamo.IdAgente;
		objAsignarPrestamista_Entrada.p_PlazoEnDias					= p_objPrestamo.PlazoEnDiasOperacion;
		objAsignarPrestamista_Entrada.p_IdTipoCanalIngreso			= p_objPrestamo.IdTipoCanalIngreso;
		objAsignarPrestamista_Entrada.p_IdMonedaSistema				= p_objPrestamo.IdMonedaSistema;
		objAsignarPrestamista_Entrada.p_EsFungible					= "N";
		objAsignarPrestamista_Entrada.p_IdEmpleado					= p_lngIdEmpleado; // p_objPrestamo.IdEmpleadoDigitador;
		objAsignarPrestamista_Entrada.p_TipoCustodia            	= p_objPrestamo.TipoCustodia;
		objAsignarPrestamista_Entrada.p_ExigirGarantia		    	= (p_objPrestamistaBC.MandatoryCollateral.equalsIgnoreCase("TRUE") ? "S" : "N"); // p_objPrestamo.ExigirGarantia;
		objAsignarPrestamista_Entrada.p_VieneDesdeBlockChain		= "S";
			
		CrearEmprestito_Salida objAsignarPrestamista_Salida = objAsignarPrestamista.logica(conn, objAsignarPrestamista_Entrada);
		blnRetorno = true; //asumo que es retorno verdadero
		
			//devolvio error el servicio de agregar prestamitas
		if(MensajeSalida.getNumeroErrores(objAsignarPrestamista_Salida.P_OUT_MENSAJES) > 0 || (objAsignarPrestamista_Salida.P_OUT_ERRORESCONTADOR != null && objAsignarPrestamista_Salida.P_OUT_ERRORESCONTADOR>0))
		{
			//debo registrar el error en tabla de reproceso 
			//si no puede agregar prestamistas no continua con el proceso, deberia hacer rollback de todos los agregados antes
			
			salida.P_OUT_ERRORESCONTADOR = objAsignarPrestamista_Salida.P_OUT_ERRORESCONTADOR == null ? 1L: objAsignarPrestamista_Salida.P_OUT_ERRORESCONTADOR;
			salida.P_OUT_MENSAJES.addAll(objAsignarPrestamista_Salida.P_OUT_MENSAJES);
			blnRetorno = false;
		}
	
		return blnRetorno;
	}
	//-------------------------------------------------------------------------------------------
	// DEVUELVE DATOS DE PRESTAMO
	//-------------------------------------------------------------------------------------------
	private List<Prestamo> ObtienePrestamos( Connection conn, Long p_IdEmpresa, List<String> p_strFoliosSistemaExterno ) throws SQLException {
		List<Prestamo> prestamos 		= new ArrayList<>();
		SQL sql 						= new SQL();
		HashMap<String,Object> hparam 	= new HashMap<String,Object>();
		
		hparam.put("p_IdEmpresa"			, p_IdEmpresa );
		hparam.put("p_FolioSistemaExterno"	, p_strFoliosSistemaExterno );
		prestamos = sql.query(conn, SQL_SELECT_PRESTAMO, hparam, Prestamo.class);
		
		return prestamos;
	}
	//-------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE - CALL
	//-------------------------------------------------------------------------------------------
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();
		CrearEmprestitoDesdeBlockChain_Entrada in = gson.fromJson(jin.toString(), CrearEmprestitoDesdeBlockChain_Entrada.class);
		CrearEmprestitoDesdeBlockChain_Salida out = logica(con,in);
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	//-------------------------------------------------
	// METODO PARA PRUEBAS LOCALES 
	//-------------------------------------------------
 
	public static void main(String[] args) throws Exception {
		System.out.println("== INICIO PRUEBA LOCAL ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();		

			//String json = "{\"p_IdEmpresa\":1,\"p_Bloque\":\"2999\"}";
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"CUS\",\"p_FoliosBlockChain\":[{\"laDetails\":{\"LAID\":\"61-0000000071084\",\"Status\":\"ACTIVE\",\"Symbol\":\"CINTAC\",\"LADate\":\"2023-04-21\",\"SecurityType\":\"RV\",\"LendingAgreementType\":\"O\",\"SettlType\":\"PH\",\"BorrowingBrokerageHouseID\":\"61\",\"LendingBrokerageHouseID\":\"61\",\"LendingQty\":\"205\",\"MaturityDate\":\"2023-04-26\",\"Yield\":\"1.27\",\"LegacyLAID\":\"71084\"},\"borrowingClients\":{\"LAID\":\"61-0000000071084\",\"BorrowingClientRUT\":\"21087838-6\"},\"lendingClients\":[{\"LAID\":\"61-0000000071084\",\"LendingClientRUT\":\"10487346-4\",\"LendingClientFund\":\"\",\"LendingClientQty\":\"205\",\"CollateralType\":\"G\",\"MandatoryCollateral\":\"TRUE\",\"LCStatus\":\"\",\"SignatureDate\":\"\",\"SignatureUser\":\"\"}]},{\"laDetails\":{\"LAID\":\"61-0000000071086\",\"Status\":\"ACTIVE\",\"Symbol\":\"HITES\",\"LADate\":\"2023-04-21\",\"SecurityType\":\"RV\",\"LendingAgreementType\":\"O\",\"SettlType\":\"PH\",\"BorrowingBrokerageHouseID\":\"61\",\"LendingBrokerageHouseID\":\"61\",\"LendingQty\":\"206\",\"MaturityDate\":\"2023-04-26\",\"Yield\":\"10.52\",\"LegacyLAID\":\"71086\"},\"borrowingClients\":{\"LAID\":\"61-0000000071086\",\"BorrowingClientRUT\":\"10510689-0\"},\"lendingClients\":[{\"LAID\":\"61-0000000071086\",\"LendingClientRUT\":\"10401925-0\",\"LendingClientFund\":\"\",\"LendingClientQty\":\"206\",\"CollateralType\":\"G\",\"MandatoryCollateral\":\"TRUE\",\"LCStatus\":\"\",\"SignatureDate\":\"\",\"SignatureUser\":\"\"}]},{\"laDetails\":{\"LAID\":\"61-0000000071087\",\"Status\":\"ACTIVE\",\"Symbol\":\"CINTAC\",\"LADate\":\"2023-04-21\",\"SecurityType\":\"RV\",\"LendingAgreementType\":\"O\",\"SettlType\":\"PH\",\"BorrowingBrokerageHouseID\":\"61\",\"LendingBrokerageHouseID\":\"61\",\"LendingQty\":\"301\",\"MaturityDate\":\"2023-04-26\",\"Yield\":\"2.72\",\"LegacyLAID\":\"71087\"},\"borrowingClients\":{\"LAID\":\"61-0000000071087\",\"BorrowingClientRUT\":\"10510689-0\"},\"lendingClients\":[{\"LAID\":\"61-0000000071087\",\"LendingClientRUT\":\"10401925-0\",\"LendingClientFund\":\"\",\"LendingClientQty\":\"206\",\"CollateralType\":\"G\",\"MandatoryCollateral\":\"TRUE\",\"LCStatus\":\"\",\"SignatureDate\":\"\",\"SignatureUser\":\"\"},{\"LAID\":\"61-0000000071087\",\"LendingClientRUT\":\"10487346-4\",\"LendingClientFund\":\"\",\"LendingClientQty\":\"95\",\"CollateralType\":\"G\",\"MandatoryCollateral\":\"TRUE\",\"LCStatus\":\"\",\"SignatureDate\":\"\",\"SignatureUser\":\"\"}]},{\"laDetails\":{\"LAID\":\"61-0000000071088\",\"Status\":\"ACTIVE\",\"Symbol\":\"ANTARCHILE\",\"LADate\":\"2023-04-21\",\"SecurityType\":\"RV\",\"LendingAgreementType\":\"T\",\"SettlType\":\"CN\",\"BorrowingBrokerageHouseID\":\"61\",\"LendingBrokerageHouseID\":\"61\",\"LendingQty\":\"300\",\"MaturityDate\":\"2023-04-25\",\"Yield\":\"0.6000\",\"LegacyLAID\":\"71088\"},\"borrowingClients\":{\"LAID\":\"61-0000000071088\",\"BorrowingClientRUT\":\"160-0\"},\"lendingClients\":[{\"LAID\":\"61-0000000071088\",\"LendingClientRUT\":\"80993900-6\",\"LendingClientFund\":\"\",\"LendingClientQty\":\"300\",\"CollateralType\":\"G\",\"MandatoryCollateral\":\"TRUE\",\"LCStatus\":\"\",\"SignatureDate\":\"\",\"SignatureUser\":\"\"}]},{\"laDetails\":{\"LAID\":\"61-0000000071089\",\"Status\":\"ACTIVE\",\"Symbol\":\"CENCOSUD\",\"LADate\":\"2023-04-21\",\"SecurityType\":\"RV\",\"LendingAgreementType\":\"T\",\"SettlType\":\"CN\",\"BorrowingBrokerageHouseID\":\"61\",\"LendingBrokerageHouseID\":\"61\",\"LendingQty\":\"300\",\"MaturityDate\":\"2023-04-25\",\"Yield\":\"0.6000\",\"LegacyLAID\":\"71089\"},\"borrowingClients\":{\"LAID\":\"61-0000000071089\",\"BorrowingClientRUT\":\"160-0\"},\"lendingClients\":[{\"LAID\":\"61-0000000071089\",\"LendingClientRUT\":\"80993900-6\",\"LendingClientFund\":\"\",\"LendingClientQty\":\"300\",\"CollateralType\":\"G\",\"MandatoryCollateral\":\"TRUE\",\"LCStatus\":\"\",\"SignatureDate\":\"\",\"SignatureUser\":\"\"}]},{\"laDetails\":{\"LAID\":\"61-0000000071090\",\"Status\":\"ACTIVE\",\"Symbol\":\"COPEC\",\"LADate\":\"2023-04-21\",\"SecurityType\":\"RV\",\"LendingAgreementType\":\"T\",\"SettlType\":\"CN\",\"BorrowingBrokerageHouseID\":\"61\",\"LendingBrokerageHouseID\":\"61\",\"LendingQty\":\"300\",\"MaturityDate\":\"2023-04-25\",\"Yield\":\"0.6000\",\"LegacyLAID\":\"71090\"},\"borrowingClients\":{\"LAID\":\"61-0000000071090\",\"BorrowingClientRUT\":\"160-0\"},\"lendingClients\":[{\"LAID\":\"61-0000000071090\",\"LendingClientRUT\":\"80993900-6\",\"LendingClientFund\":\"\",\"LendingClientQty\":\"300\",\"CollateralType\":\"G\",\"MandatoryCollateral\":\"TRUE\",\"LCStatus\":\"\",\"SignatureDate\":\"\",\"SignatureUser\":\"\"}]}]}";
			CrearEmprestitoDesdeBlockChain_Entrada Objin = gson.fromJson(json, CrearEmprestitoDesdeBlockChain_Entrada.class);
			CrearEmprestitoDesdeBlockChain demo = new CrearEmprestitoDesdeBlockChain();
 			
			System.out.println("ENTRADA:" + Objin.toString());
			
 			try {
 				CrearEmprestitoDesdeBlockChain_Salida ObjOut = demo.logica(con, Objin);
 				System.out.println("SALIDA:" + ObjOut);
 				con.commit();
 				//con.rollback();
 				
 			} catch(Exception er) {
				er.printStackTrace();
				con.rollback();
			}

	    }

		System.out.println("== FIN PRUEBA LOCAL ==");
	}
	
	
}
