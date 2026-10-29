package cl.bcs.custodia.prt;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

import cl.bcs.custodia.prt.CrearPrestamo.CrearPrestamo_Entrada;
import cl.bcs.custodia.prt.CrearPrestamo.CrearPrestamo_Salida;
import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo.ObjADVERTENCIASDETALLE;
import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo.ObjERRORESDETALLE;
import cl.bcs.plataforma.ins.ConsultarInstrumentoPrecioBolsa;
import cl.bcs.plataforma.ins.ConsultarInstrumentoPrecioBolsa.ConsultarInstrumentoPrecioBolsa_Entrada;
import cl.bcs.plataforma.ins.ConsultarInstrumentoPrecioBolsa.ConsultarInstrumentoPrecioBolsa_Salida;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.bck.ConsultaListaPorFolioBlockChain.PrestamosBlockChain;
import cl.bcs.socionegocio.bck.ConsultaLogBlockChain.PrestamoCreado;
import cl.bcs.socionegocio.bck.ProcesaLogBlockChain.GrupoIndicador;
import cl.bcs.socionegocio.bck.ProcesaLogBlockChain.Indicador;
import cl.bcs.socionegocio.bck.ProcesaLogBlockChain.Instrumento;
import cl.bcs.socionegocio.pojo.ConstantesBCK;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearPrestamoDesdeBlockChain implements JavaCall
{
	//-------------------------------------------------
	// QUERIES
	//-------------------------------------------------
	private final String SQL_SELECT_IDCLIENTE	 				= loadResourceToString("cl/bcs/custodia/sql/CrearPrestamoDesdeBlockChain_SelectPersonaCliente.sql");
	private final String SQL_SELECT_IDINSTRUMENTO				= loadResourceToString("cl/bcs/custodia/sql/CrearPrestamoDesdeBlockChain_SelectIdInstrumento.sql");
	private final String SQL_SELECT_TIPOPRESTAMO				= loadResourceToString("cl/bcs/custodia/sql/CrearPrestamoDesdeBlockChain_SelectTipoPrestamo.sql");
	private final String SQL_SELECT_PRTPENDIENTEEXISTE			= loadResourceToString("cl/bcs/custodia/sql/CrearPrestamoDesdeBlockChain_SelectRegistroYaExiste.sql");
	private final String SQL_SELECT_INSERTPRTPENDIENTE			= loadResourceToString("cl/bcs/custodia/sql/CrearPrestamoDesdeBlockChain_InsertPrestamosPendientes.sql");
	private final String SQL_SELECT_UPDATEPRTPENDIENTE			= loadResourceToString("cl/bcs/custodia/sql/CrearPrestamoDesdeBlockChain_UpdatePrestamosPendientes.sql");
	private final String SQL_SELECT_SUCURSAL					= loadResourceToString("cl/bcs/custodia/sql/CrearPrestamoDesdeBlockChain_SelectSucursal.sql");
	private final String SQL_SELECT_PRESTAMO					= loadResourceToString("cl/bcs/custodia/sql/CrearPrestamoDesdeBlockChain_SelectPrestamo.sql");
	private final String SQL_SELECT_CONDICIONES_LIQUIDACION		= loadResourceToString("cl/bcs/custodia/sql/CrearPrestamoDesdeBlockChain_SelectCondicionesLiquidacion.sql");														
	
	//-------------------------------------------------
	// CLASES DE ENTRADA 
	//-------------------------------------------------

	public static class CrearPrestamoDesdeBlockChain_Entrada extends BcsPojo {
		public Long						p_IdEmpresa;
		public Long						p_IdUsuario;
		public String					p_IdModulo;
		public List<PrestamoCreado> 	p_PrestamosCreado;
		
		public CrearPrestamoDesdeBlockChain_Entrada()
		{
			p_PrestamosCreado = new ArrayList<>();
		}
	}
	//-------------------------------------------------
	// CLASES DE SALIDA 
	//-------------------------------------------------
	public static class CrearPrestamoDesdeBlockChain_Salida extends BcsPojo {
		public List<MensajeSalida>			P_OUT_MENSAJES					;
		public Long							P_OUT_ERRORESCONTADOR			;
		public List<String>					P_OUT_RESULTADO					; //retorna los FoliosBlockChain que se pudieron crear en Optimus
		public List<PrestamosBlockChain>	P_OUT_PRESTAMOBLOCKCHAIN		; //retorna un objeto con los datos del prestamo BlockChain que no se pudocrear
		//public String						P_OUT_PRESTAMOCONPRESTAMISTAS	; //retorna el Folios BlockChain (prestamo) por que tiene prestamista
		
		public CrearPrestamoDesdeBlockChain_Salida()
		{	
			this.P_OUT_MENSAJES 				= new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR			= 0L;
			this.P_OUT_RESULTADO 				= new ArrayList<>();
			this.P_OUT_PRESTAMOBLOCKCHAIN		= new ArrayList<>();
			//this.P_OUT_PRESTAMOCONPRESTAMISTAS 	= "";
		}
	}
	
	//-------------------------------------------------
	// CLASES UTILITARIAS 
	//-------------------------------------------------
	public static class FolioBlockChain extends BcsPojo {
		public String FolioSistemaExterno;
	}
	
	public static class PrestamoPendiente extends BcsPojo {
		public Long		IdPrestamosPendientes;
		public String	Nemotecnico;
		public String	Mercado;
		public String	DocumentoIdentidad;
		public String	TipoDocIdentidad;
		public String	Cantidad;
		public String	Tasa;
		public String	CondicionLiquidacion;
		public String	FechaLiquidacion;
		public String	CodigoCorredora;
		public String	FolioSistemaExterno;
		public String	Origen;
		public String	Mensaje;
		public String	Estado;
		public String	TipoRegistro;
		public String	FechaIngreso;
	}
	
	public static class Sucursal extends BcsPojo{
		public Long IdSucursal = 0L;
	}
	
	public static class Prestamo extends BcsPojo {
		public Long			FolioPrestamo;
		public String		FolioSistemaExterno;
	}
	
	public static class TipoPrestamo extends BcsPojo {
		public Long 	IdInstrumento = 0L;
		public String 	IdTipoPrestamo = "";
		public String 	TipoPrestamoSistemaExterno;
		public String 	CobraPrimaAlInicio;
		public String	EsDiaHabilPlazoPrestamo;
		public String	IdClasificacionTipoInstrumento;
	}
	
	public static class Cliente extends BcsPojo {
		public String	DocumentoIdentidad;
		public Long 	IdCliente;
	}
	
	public static class CondicionLiquidacion extends BcsPojo {
		public String IdCondicionDeLiquidacion;
		public String Codigo;
	}
	
	//-------------------------------------------------------------------------------------------
	// CREACION DE PRESTAMOS EN OPTIMUS
	//-------------------------------------------------------------------------------------------
	public CrearPrestamoDesdeBlockChain_Salida logica(Connection conn, CrearPrestamoDesdeBlockChain_Entrada in) throws Exception {
		
		CrearPrestamoDesdeBlockChain_Salida salida 	= new CrearPrestamoDesdeBlockChain_Salida();
		
		if ( in.p_PrestamosCreado.size() == 0 ) {
			return salida;
		}
		
		SQL sql 										= new SQL();
		HashMap<String,Object> parametrosQuery 			= new HashMap<String,Object>();
		HashMap<String,String> mapMsg 					= new HashMap<String,String>();
		List<TipoPrestamo> tiposPrestamo 				= new ArrayList<>();
		HashMap<String, Object> hPrestamosOptimus 		= new HashMap<String, Object>();
		HashMap<String, Object> hClientesOptimus 		= new HashMap<String, Object>();
		HashMap<String, Object> hCondicionesLiquidacion = new HashMap<String, Object>();
		String strEsCustodia	= "N";
		
		tiposPrestamo = sql.query(conn, SQL_SELECT_TIPOPRESTAMO, parametrosQuery, TipoPrestamo.class);
		parametrosQuery.clear();
		
		parametrosQuery.put("p_IdEmpresa", in.p_IdEmpresa);
		parametrosQuery.put("p_IdUsuario", in.p_IdUsuario);
		List<Sucursal> sucursales = sql.query(conn, SQL_SELECT_SUCURSAL, parametrosQuery, Sucursal.class);
		parametrosQuery.clear();
		
		if ( sucursales.size() == 0 ) {
			return salida;
		}
		
		String usaFechaLiquidacionEnVencimiento = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "CUS.UsaFechaLiquidacionEnVencimiento");
		
		List<CondicionLiquidacion> condicionesLiquidacion = sql.query( conn, SQL_SELECT_CONDICIONES_LIQUIDACION, parametrosQuery, CondicionLiquidacion.class );
		parametrosQuery.clear();
		hCondicionesLiquidacion = Utils.List2Hash( condicionesLiquidacion, "Codigo" );
		
		List<Prestamo> prestamosOptimus = ObtienePrestamos( conn, in.p_IdEmpresa, in.p_PrestamosCreado.stream().map( prestamo_creado -> prestamo_creado.laDetails.LAID ).collect( Collectors.toList() ) );
		hPrestamosOptimus = Utils.List2Hash(prestamosOptimus, "FolioSistemaExterno");
		
		parametrosQuery.put("p_DocumentoIdentidad"	, in.p_PrestamosCreado.stream().map( prestamo_creado -> prestamo_creado.borrowingClients.BorrowingClientRUT ).collect( Collectors.toList() ) );
		parametrosQuery.put("p_IdEmpresa"			, in.p_IdEmpresa);
		List<Cliente> clientes = sql.query(conn, SQL_SELECT_IDCLIENTE, parametrosQuery, Cliente.class);
		parametrosQuery.clear();
		hClientesOptimus = Utils.List2Hash(clientes, "DocumentoIdentidad");
		
		List<Instrumento> instrumentosPrestamo = ObtieneInstrumentos( conn, in.p_PrestamosCreado.stream().map( prestamo_creado -> prestamo_creado.laDetails.Symbol ).collect( Collectors.toList() ) );
		
		ConsultarEmpresa consultarEmpresaService 	= new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresaService.logica(conn, in.p_IdEmpresa);
		Long IdPaisEmpresa = empresa.IdPais;
		
		for ( PrestamoCreado prestamoCreado : in.p_PrestamosCreado ) {
			
			PrestamosBlockChain prestamoBlockChainPendientePorCargar = new PrestamosBlockChain();
			
			Prestamo prestamo = ( Prestamo ) hPrestamosOptimus.get( prestamoCreado.laDetails.LAID );
			
			if ( prestamo != null ) continue;
			
			Cliente cliente = ( Cliente ) hClientesOptimus.get( prestamoCreado.borrowingClients.BorrowingClientRUT );
			
			if ( cliente == null ) {
				mapMsg.put( "DocumentoIdentidad", prestamoCreado.borrowingClients.BorrowingClientRUT );
				salida.P_OUT_MENSAJES.add( new MensajeSalida( Long.valueOf( prestamoCreado.laDetails.LegacyLAID ), MensajeSalida.MENSAJE_ERROR, "BC_CLIENTE_NO_EXISTE", mapMsg, conn, in.p_IdEmpresa ) );
				mapMsg.clear();
				salida.P_OUT_ERRORESCONTADOR += 1L;
				prestamoBlockChainPendientePorCargar.laDetails 			= prestamoCreado.laDetails;
				prestamoBlockChainPendientePorCargar.borrowingClients 	= prestamoCreado.borrowingClients;
				salida.P_OUT_PRESTAMOBLOCKCHAIN.add( prestamoBlockChainPendientePorCargar );
				continue;
			}
			
			List<Instrumento> instrumento = instrumentosPrestamo.stream()
					.filter( intrumento_prestamo -> intrumento_prestamo.Nemotecnico.trim().equals( prestamoCreado.laDetails.Symbol ) )
					.collect( Collectors.toList() );
			
			if ( instrumento.size() == 0 ) {
				mapMsg.put( "Nemotecnico", prestamoCreado.laDetails.Symbol );
				salida.P_OUT_MENSAJES.add( new MensajeSalida( Long.valueOf( prestamoCreado.laDetails.LegacyLAID ), MensajeSalida.MENSAJE_ERROR, "BC_INSTRUMENTO_NO_ENCONTRADO", mapMsg, conn, in.p_IdEmpresa ) );
				mapMsg.clear();
				salida.P_OUT_ERRORESCONTADOR += 1L;
				prestamoBlockChainPendientePorCargar.laDetails 			= prestamoCreado.laDetails;
				prestamoBlockChainPendientePorCargar.borrowingClients 	= prestamoCreado.borrowingClients;
				salida.P_OUT_PRESTAMOBLOCKCHAIN.add( prestamoBlockChainPendientePorCargar );
				continue;
			}
			
			List<TipoPrestamo> tipoPrestamo = tiposPrestamo.stream()
			.filter( tipo_prestamo -> tipo_prestamo.TipoPrestamoSistemaExterno.equals( prestamoCreado.laDetails.LendingAgreementType )
					&& tipo_prestamo.IdClasificacionTipoInstrumento.equals( prestamoCreado.laDetails.SecurityType ) )
			.collect( Collectors.toList() );
			
			if ( tipoPrestamo.size() == 0 ) {
				strEsCustodia = "S";
				tipoPrestamo = tiposPrestamo.stream()
						.filter( tipo_prestamo -> tipo_prestamo.TipoPrestamoSistemaExterno.equals( "O" )
								&& tipo_prestamo.IdClasificacionTipoInstrumento.equals( prestamoCreado.laDetails.SecurityType ) )
						.collect( Collectors.toList() );
			}
			
			
			Long lngPortafolio = ConsultaPortafolio(conn, in.p_IdEmpresa, cliente.IdCliente );
			
			if ( lngPortafolio == 0 ) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida( Long.valueOf( prestamoCreado.laDetails.LegacyLAID ), MensajeSalida.MENSAJE_ERROR, "BC_CLIENTE_PREFERENCIA_NEGOCIO_SIN_CUENTA_BLOCKCHAIN", new HashMap<>(), conn, in.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR += 1L;
				prestamoBlockChainPendientePorCargar.laDetails 			= prestamoCreado.laDetails;
				prestamoBlockChainPendientePorCargar.borrowingClients 	= prestamoCreado.borrowingClients;
				salida.P_OUT_PRESTAMOBLOCKCHAIN.add( prestamoBlockChainPendientePorCargar );
				continue;
			}
			
			CondicionLiquidacion condicionLiquidacion = ( CondicionLiquidacion ) hCondicionesLiquidacion.get( prestamoCreado.laDetails.SettlType );
			
			if ( condicionLiquidacion == null ) {
				salida.P_OUT_MENSAJES.add( new MensajeSalida( Long.valueOf( prestamoCreado.laDetails.LegacyLAID ), MensajeSalida.MENSAJE_ERROR, "BC_CONDICION_LIQUIDACION_NO_ENCONTRADA", new HashMap<>(), conn, in.p_IdEmpresa ) );
				salida.P_OUT_ERRORESCONTADOR += 1L;
				prestamoBlockChainPendientePorCargar.laDetails 			= prestamoCreado.laDetails;
				prestamoBlockChainPendientePorCargar.borrowingClients 	= prestamoCreado.borrowingClients;
				salida.P_OUT_PRESTAMOBLOCKCHAIN.add( prestamoBlockChainPendientePorCargar );
				continue;
			}
			
			BigDecimal precioInstrumento					= Precio(conn, in.p_IdEmpresa, in.p_IdUsuario, instrumento.get( 0 ).IdInstrumento, prestamoCreado.laDetails.LADate , "BCS"  );
			
			if ( precioInstrumento.compareTo( new BigDecimal( "0" ) ) == 0 ) {
				salida.P_OUT_MENSAJES.add( new MensajeSalida( Long.valueOf( prestamoCreado.laDetails.LegacyLAID ), MensajeSalida.MENSAJE_ERROR, "BC_INSTRUMENTO_SIN_PRECIO", new HashMap<>(), conn, in.p_IdEmpresa ) );
				salida.P_OUT_ERRORESCONTADOR += 1L;
				prestamoBlockChainPendientePorCargar.laDetails 			= prestamoCreado.laDetails;
				prestamoBlockChainPendientePorCargar.borrowingClients 	= prestamoCreado.borrowingClients;
				salida.P_OUT_PRESTAMOBLOCKCHAIN.add( prestamoBlockChainPendientePorCargar );
				continue;
			}
			
			CrearPrestamo objCrearPrestamo 					= new CrearPrestamo();
			CrearPrestamo_Entrada objCrearPrestamo_Entrada 	= new CrearPrestamo_Entrada();
			
			objCrearPrestamo_Entrada.p_Cantidad 					= new BigDecimal( prestamoCreado.laDetails.LendingQty );
			objCrearPrestamo_Entrada.p_CobraPrimaAlInicio 			= tipoPrestamo.get( 0 ).CobraPrimaAlInicio;
			objCrearPrestamo_Entrada.p_ConGarantiaEspecifica 		= "N";
			objCrearPrestamo_Entrada.p_FechaMovimiento 				= prestamoCreado.laDetails.LADate;
			objCrearPrestamo_Entrada.p_FechaVencimiento 			= prestamoCreado.laDetails.MaturityDate;
			objCrearPrestamo_Entrada.p_FolioEntidad 				= "";
			objCrearPrestamo_Entrada.p_FolioTransaccion 			= "";
			objCrearPrestamo_Entrada.p_IdAgente 					= in.p_IdUsuario; //54;
			objCrearPrestamo_Entrada.p_IdBolsa 						= ConstantesBCK.ID_BOLSA_POR_DEFECTO;  
			objCrearPrestamo_Entrada.p_IdClienteSolicita 			= cliente.IdCliente;
			objCrearPrestamo_Entrada.p_IdEmpleadoDigitador 			= 0L;// Id de Usuario Automatico //11404L; //in.p_IdUsuario; //null; //11404L;
			objCrearPrestamo_Entrada.p_IdEmpresa 					= in.p_IdEmpresa;
			objCrearPrestamo_Entrada.p_IdInstrumento 				= instrumento.get( 0 ).IdInstrumento; //502;
			objCrearPrestamo_Entrada.p_IdInstrumentoPrestamo 		= tipoPrestamo.get( 0 ).IdInstrumento; //10011;
			objCrearPrestamo_Entrada.p_IdMercado 					= ConstantesBCK.MERCADO_PRESTAMOS_BLOCKCHAIN; 
			objCrearPrestamo_Entrada.p_IdModulo 					= in.p_IdModulo; // "CUS"; //asume que es del modulo de Custodia
			objCrearPrestamo_Entrada.p_IdMonedaSistema 				= ConstantesBCK.MONEDA; //TODO: ver si esto siempre es en pesos
			objCrearPrestamo_Entrada.p_IdPortafolioSolicita 		= lngPortafolio; // 
			objCrearPrestamo_Entrada.p_IdSucursal					= sucursales.get( 0 ).IdSucursal; 
			objCrearPrestamo_Entrada.p_IdTipoCanalIngreso			= "AGE"; //TODO: este valor es en duro siempre?
			objCrearPrestamo_Entrada.p_IdUsuario 					= in.p_IdUsuario; //54;
			objCrearPrestamo_Entrada.p_Precio 						= precioInstrumento;
			objCrearPrestamo_Entrada.p_Monto 						= Monto(conn, in.p_IdEmpresa, in.p_IdUsuario, new BigDecimal( prestamoCreado.laDetails.LendingQty ) , "ACN", objCrearPrestamo_Entrada.p_Precio );
			objCrearPrestamo_Entrada.p_IdCondicionLiquidacion		= condicionLiquidacion.IdCondicionDeLiquidacion;
			objCrearPrestamo_Entrada.p_FechaLiquidacion				= FechaLiquidacion( conn, in.p_IdEmpresa, in.p_IdUsuario, prestamoCreado.laDetails.LADate, "PRESTA", IdPaisEmpresa, condicionLiquidacion.IdCondicionDeLiquidacion, "S" );
			
			String strPlazo = Plazo( conn, in.p_IdEmpresa, in.p_IdUsuario, usaFechaLiquidacionEnVencimiento.equalsIgnoreCase( ConstantesBCK.SI ) ? objCrearPrestamo_Entrada.p_FechaLiquidacion : prestamoCreado.laDetails.LADate, prestamoCreado.laDetails.MaturityDate, IdPaisEmpresa, condicionLiquidacion.IdCondicionDeLiquidacion, "S", tipoPrestamo.get( 0 ).EsDiaHabilPlazoPrestamo );
			
			objCrearPrestamo_Entrada.p_MontoAPlazo 					= calcularMontoPlazo( objCrearPrestamo_Entrada.p_Monto, strPlazo , prestamoCreado.laDetails.Yield );
			objCrearPrestamo_Entrada.p_MontoPrima 					= objCrearPrestamo_Entrada.p_MontoAPlazo.subtract( objCrearPrestamo_Entrada.p_Monto ); 
			objCrearPrestamo_Entrada.p_Nemo							= prestamoCreado.laDetails.Symbol; 
			objCrearPrestamo_Entrada.p_PlazoEnDias					= Long.valueOf( strPlazo ); 
			objCrearPrestamo_Entrada.p_TasaInstrumento				= new BigDecimal( "0" );
			objCrearPrestamo_Entrada.p_TasaPrima					= new BigDecimal( prestamoCreado.laDetails.Yield ); 
			objCrearPrestamo_Entrada.p_TipoCustodia					= strEsCustodia;
			objCrearPrestamo_Entrada.p_UsaReglasNegocio				= "S";
			objCrearPrestamo_Entrada.p_esNominal					= "N";
			objCrearPrestamo_Entrada.p_IdFolioSistemaExterno		= prestamoCreado.laDetails.LAID;
			
			CrearPrestamo_Salida objCrearPrestamo_Salida = objCrearPrestamo.logica(conn, objCrearPrestamo_Entrada);
			
			Long lngError = objCrearPrestamo_Salida.P_OUT_ERRORESCONTADOR;

			if( lngError != null && lngError.equals( 0L ) ) {
				salida.P_OUT_RESULTADO.add( prestamoCreado.laDetails.LAID );						
			} else {
				salida.P_OUT_MENSAJES.addAll( objCrearPrestamo_Salida.P_OUT_MENSAJES.stream()
						.map( mensaje_salida -> new MensajeSalida( Long.valueOf( prestamoCreado.laDetails.LegacyLAID ), mensaje_salida.tipo, mensaje_salida.codigo, mensaje_salida.descripcion) )
						.collect( Collectors.toList() ) );
				salida.P_OUT_ERRORESCONTADOR += 1L;
				prestamoBlockChainPendientePorCargar.laDetails 			= prestamoCreado.laDetails;
				prestamoBlockChainPendientePorCargar.borrowingClients 	= prestamoCreado.borrowingClients;
				salida.P_OUT_PRESTAMOBLOCKCHAIN.add( prestamoBlockChainPendientePorCargar );

			}
			
		}
		
		return salida;
		
	}
	//-------------------------------------------------------------------------------------------
	// DEVUELVE DATOS DE PRESTAMO
	//-------------------------------------------------------------------------------------------
	private List<Prestamo> ObtienePrestamos(Connection conn, Long p_IdEmpresa, List<String> p_strFoliosSistemaExterno) throws SQLException {
		
		SQL sql 								= new SQL();
		List<Prestamo> prestamosPorFolioExterno = new ArrayList<>();
		HashMap<String,Object> hparam 			= new HashMap<String,Object>();
		
		hparam.put("p_IdEmpresa"			, p_IdEmpresa );
		hparam.put("p_FolioSistemaExterno"	, p_strFoliosSistemaExterno );
		prestamosPorFolioExterno = sql.query(conn, SQL_SELECT_PRESTAMO, hparam, Prestamo.class);
		
		return prestamosPorFolioExterno;
	}

	//-------------------------------------------------------------------------------------------
	// VA EN BUSCA DEL ID DE TIPO DE INSTRUMENTO
	//-------------------------------------------------------------------------------------------
	public List<Instrumento> ObtieneInstrumentos(Connection conn, List<String> nemotecnicos) throws SQLException 
	{
		SQL sql 								= new SQL();
		List<Instrumento> instrumentosPrestamo	= new ArrayList<Instrumento>();
		HashMap<String,Object> hparam 			= new HashMap<String,Object>();
		
		hparam.put("p_Nemo", nemotecnicos);
		instrumentosPrestamo = sql.query(conn, SQL_SELECT_IDINSTRUMENTO, hparam, Instrumento.class);
		
		return instrumentosPrestamo;
	}
	//va a consultar los indicadores y obtener el de preferencia de negocio referente al Id del Portafolio al cual sera asociado un prestado
	public Long ConsultaPortafolio(Connection conn, Long p_lngIdEmpresa, Long p_lngCliente) throws SQLException 
	{
		Long lngIdPortafolio 					= 0L;
		List<Indicador> lstIndicadores 			= new ArrayList<>();
		List<GrupoIndicador> lstGruposIndicador = new ArrayList<>();
		SQL sql 								= new SQL();
		
		// ------------------------------
		// Ejecuta el SP
		HashMap<String, Object> parmsIN = new HashMap<String, Object>();

		parmsIN.put("P_IDEMPRESA", p_lngIdEmpresa);
		parmsIN.put("P_IDENTIDAD", p_lngCliente);
		parmsIN.put("P_IDTIPOENTIDAD", "CLIENTE");
		parmsIN.put("P_CODIGOTIPOINDICADOR", "BLOCKCHAIN_PORTAFOLIO_PRESTAMOS");
		parmsIN.put("P_GRUPOINDICADOR", "");
		parmsIN.put("P_MODELOCLIENTE", "");
		parmsIN.put("P_IDINDICADOR", 0L); 

		HashMap<String, Class> parmsOUT = new HashMap<String, Class>();

		parmsOUT.put("P_OUT_INDICADORESCONTADOR", Long.class);
		parmsOUT.put("P_OUT_INDICADORES", Indicador.class);
		parmsOUT.put("P_OUT_GRUPOINDICADORESCONTADOR", Long.class);
		parmsOUT.put("P_OUT_GRUPOINDICADORES", GrupoIndicador.class);

		String queryCall = "call \"BSCL.Plataforma.BD.Servicios::IND.ConsultarPreferencia\" (:P_IDEMPRESA,:P_IDENTIDAD,:P_IDTIPOENTIDAD,:P_CODIGOTIPOINDICADOR,:P_GRUPOINDICADOR,:P_MODELOCLIENTE,:P_IDINDICADOR,@P_OUT_INDICADORESCONTADOR,$P_OUT_INDICADORES,@P_OUT_GRUPOINDICADORESCONTADOR,$P_OUT_GRUPOINDICADORES)";
		HashMap<String, Object> rpt = sql.call(conn, queryCall, parmsIN, parmsOUT);

		lstIndicadores = (List<Indicador>) rpt.get("P_OUT_INDICADORES");
		lstGruposIndicador = (List<GrupoIndicador>) rpt.get("P_OUT_GRUPOINDICADORES");
		
		if(lstIndicadores.size()>0)
		{
			if(isInteger(lstIndicadores.get(0).ValorIndicador) && !lstIndicadores.get(0).ValorIndicador.equals("N")) {
				lngIdPortafolio = new Long(lstIndicadores.get(0).ValorIndicador);
			}
		}
		
		return lngIdPortafolio;
	}
	
	public BigDecimal Precio(Connection conn, Long p_lngIdEmpresa, Long p_lngIdUsuario, Long p_lngIdInstrumento, String p_strFecha, String p_strIdBolsa) 
	{
		BigDecimal bdeRetorno = new BigDecimal("0");
		
		ConsultarInstrumentoPrecioBolsa_Entrada objConsultaPrecio_Entrada = new ConsultarInstrumentoPrecioBolsa_Entrada();
		ConsultarInstrumentoPrecioBolsa objConsultaPrecio = new ConsultarInstrumentoPrecioBolsa();
		
		ConsultarInstrumentoPrecioBolsa.Instrumento objInstrumento = new ConsultarInstrumentoPrecioBolsa.Instrumento();
		
		objInstrumento.Fecha = p_strFecha;
		objInstrumento.IdBolsa = p_strIdBolsa;
		objInstrumento.IdCondicionDeLiquidacion="PH";
		objInstrumento.IdInstrumento = p_lngIdInstrumento;
		objInstrumento.IdEmpresa = p_lngIdEmpresa;
		objConsultaPrecio_Entrada.p_Instrumentos.add(objInstrumento);
		
		ConsultarInstrumentoPrecioBolsa_Salida objConsultaPrecio_Salida;
		try {
			objConsultaPrecio_Salida = objConsultaPrecio.logica(conn, objConsultaPrecio_Entrada);
		
			for (ConsultarInstrumentoPrecioBolsa.DatosInstrumentosPrecioBolsa_Salida objItem : objConsultaPrecio_Salida.P_OUT_INSTRUMENTO )
			{
				//el servicio retorna mas de una bolsa por lo tanto busco la bolsa que pase por paramtro
				if(objItem.IdBolsa.equals(p_strIdBolsa)) 
				{
					bdeRetorno = objItem.PrecioMercado; //objItem.PrecioApertura;
					break;
				}
				
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return bdeRetorno;
	}
	public BigDecimal Monto(Connection conn, Long p_lngIdEmpresa, Long p_lngIdUsuario, BigDecimal p_bdeCantidad, String p_IdTipoInstrumento, BigDecimal p_bdePrecio) throws SQLException
	{
		BigDecimal bdeRetorno = new BigDecimal("0");

		SQL sql = new SQL();
		
		// ------------------------------
		// Ejecuta el SP
		HashMap<String, Object> parmsIN = new HashMap<String, Object>();

		parmsIN.put("P_IDEMPRESA", p_lngIdEmpresa);
		parmsIN.put("P_IDUSUARIO", p_lngIdUsuario);
		parmsIN.put("P_IDMODULO", "CUS");
		parmsIN.put("P_USAREGLASNEGOCIO", "N");
		parmsIN.put("P_CANTIDAD", p_bdeCantidad);
		parmsIN.put("P_PRECIO", p_bdePrecio); // "7000");
		parmsIN.put("P_TAMANO", 1); 
		parmsIN.put("P_IDTIPOINSTRUMENTO", p_IdTipoInstrumento); 
		parmsIN.put("P_APLICAVALIDACIONES", "N"); 

		HashMap<String, Class> parmsOUT = new HashMap<String, Class>();

		parmsOUT.put("P_OUT_MONTO", BigDecimal.class);
		parmsOUT.put("P_OUT_ADVERTENCIASCONTADOR", Long.class);
		parmsOUT.put("P_OUT_ADVERTENCIASDETALLE", ObjADVERTENCIASDETALLE.class);
		parmsOUT.put("P_OUT_ERRORESCONTADOR", Long.class);
		parmsOUT.put("P_OUT_ERRORESDETALLE", ObjERRORESDETALLE.class);

		String queryCall = "call \"BSCL.Plataforma.BD.Servicios::CalcularMontoPorTipoInstrumento\" (:P_IDEMPRESA,:P_IDUSUARIO,:P_IDMODULO,:P_USAREGLASNEGOCIO,:P_CANTIDAD,:P_PRECIO,:P_TAMANO,:P_IDTIPOINSTRUMENTO,:P_APLICAVALIDACIONES,@P_OUT_MONTO,@P_OUT_ADVERTENCIASCONTADOR,$P_OUT_ADVERTENCIASDETALLE,@P_OUT_ERRORESCONTADOR,$P_OUT_ERRORESDETALLE)";
		HashMap<String, Object> rpt = sql.call(conn, queryCall, parmsIN, parmsOUT);

		// ------------------------------
		// Lee scalar

		//out.P_OUT_ADVERTENCIASCONTADOR = (Long) rpt.get("P_OUT_ADVERTENCIASCONTADOR");
		//out.P_OUT_ERRORESCONTADOR = (Long) rpt.get("P_OUT_ERRORESCONTADOR");

		bdeRetorno = (BigDecimal) rpt.get("P_OUT_MONTO");
		
		return bdeRetorno;
	}
	//calcula MontoPlazo segun la prima y el monto
	//Number(monto * ( 1 + (tasa/100) * (plazo /30) ))
	public BigDecimal calcularMontoPlazo(BigDecimal monto, String plazo, String ptasa){
		BigDecimal bdeRetorno = new BigDecimal("0");
		BigDecimal treita = new BigDecimal("30");
		BigDecimal uno = new BigDecimal("1");
		BigDecimal tasa = new BigDecimal(ptasa);
		
		BigDecimal tasadiv = tasa.divide(new BigDecimal("100"),3, BigDecimal.ROUND_HALF_UP);
		BigDecimal plazodiv = new BigDecimal(plazo).divide(treita,10, BigDecimal.ROUND_HALF_UP);
		
		//Number(monto * ( 1 + (tasa/100) * (plazo /30) ))
		bdeRetorno = monto.multiply(( uno.add(tasadiv.multiply( plazodiv ))));
		
		return bdeRetorno.setScale(0, RoundingMode.HALF_UP);
	};
	
	//consultar plazo
	public String Plazo(Connection conn, Long p_lngIdEmpresa, Long p_lngIdUsuario, String p_FechaDesde, String p_FechaHasta, Long p_IdPais, String p_IdCondicionDeLiquidacion, String p_UsaReglasNegocio, String p_DiasHabiles) throws SQLException
	{
		String strRetorno = "";

		SQL sql = new SQL();
		
		// ------------------------------
		// Ejecuta el SP
		HashMap<String, Object> parmsIN = new HashMap<String, Object>();

		parmsIN.put("p_IdEmpresa", p_lngIdEmpresa);
		parmsIN.put("p_IdUsuario", p_lngIdUsuario);
		parmsIN.put("p_IdModulo", "CUS");
		parmsIN.put("p_UsaReglasNegocio", p_UsaReglasNegocio);
		parmsIN.put("p_FechaDesde", p_FechaDesde);
		parmsIN.put("p_FechaHasta", p_FechaHasta); 
		parmsIN.put("p_DiasHabiles", p_DiasHabiles); 
		parmsIN.put("p_IdCondicionDeLiquidacion", p_IdCondicionDeLiquidacion); 
		parmsIN.put("p_IdPais", p_IdPais); 

		HashMap<String, Class> parmsOUT = new HashMap<String, Class>();

		parmsOUT.put("p_out_PlazoDias", Long.class);
		parmsOUT.put("p_out_AdvertenciasContador", Long.class);
		parmsOUT.put("p_out_AdvertenciasDetalle", ObjADVERTENCIASDETALLE.class);
		parmsOUT.put("p_out_ErroresContador", Long.class);
		parmsOUT.put("p_out_ErroresDetalle", ObjERRORESDETALLE.class);

		String queryCall = "CALL \"BSCL.Plataforma.BD.Servicios::ConsultarPlazo\" (:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:p_UsaReglasNegocio,:p_FechaDesde,:p_FechaHasta,:p_DiasHabiles,:p_IdCondicionDeLiquidacion,:p_IdPais,@p_out_PlazoDias,@p_out_AdvertenciasContador,$p_out_AdvertenciasDetalle,@p_out_ErroresContador,$p_out_ErroresDetalle)";
		HashMap<String, Object> rpt = sql.call(conn, queryCall, parmsIN, parmsOUT);
		
		strRetorno = rpt.get("p_out_PlazoDias").toString();
		
		return strRetorno;
	}
	
	public String FechaLiquidacion(Connection conn, Long p_lngIdEmpresa, Long p_lngIdUsuario, String p_FechaIngreso, String p_IdTipoOperacion, Long p_IdPais, String p_IdCondicionDeLiquidacion, String p_UsaReglasNegocio) throws SQLException
	{
		String strRetorno = "";

		SQL sql = new SQL();
		
		// ------------------------------
		// Ejecuta el SP
		HashMap<String, Object> parmsIN = new HashMap<String, Object>();

		parmsIN.put("p_IdEmpresa", p_lngIdEmpresa);
		parmsIN.put("p_IdUsuario", p_lngIdUsuario);
		parmsIN.put("p_IdModulo", "PLA");
		parmsIN.put("p_UsaReglasNegocio", p_UsaReglasNegocio);
		parmsIN.put("p_FechaIngreso", p_FechaIngreso);
		parmsIN.put("p_IdTipoOperacion", p_IdTipoOperacion); 
		parmsIN.put("P_IdCondicionDeLiquidacion", p_IdCondicionDeLiquidacion);
		parmsIN.put("p_IdPais", p_IdPais); 

		HashMap<String, Class> parmsOUT = new HashMap<String, Class>();

		parmsOUT.put("p_out_FechaLiquidacion", String.class);
		parmsOUT.put("p_out_AdvertenciasContador", Long.class);
		parmsOUT.put("p_out_AdvertenciasDetalle", ObjADVERTENCIASDETALLE.class);
		parmsOUT.put("p_out_ErroresContador", Long.class);
		parmsOUT.put("p_out_ErroresDetalle", ObjERRORESDETALLE.class);

		String queryCall = "CALL \"BSCL.Plataforma.BD.Servicios::ConsultarFechaLiquidacion\" (:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:p_UsaReglasNegocio,:p_FechaIngreso,:p_IdTipoOperacion,:P_IdCondicionDeLiquidacion,:p_IdPais,@p_out_FechaLiquidacion,@p_out_AdvertenciasContador,$p_out_AdvertenciasDetalle,@p_out_ErroresContador,$p_out_ErroresDetalle)";
		HashMap<String, Object> rpt = sql.call(conn, queryCall, parmsIN, parmsOUT);
		
		strRetorno = (String) rpt.get("p_out_FechaLiquidacion");
		
		return strRetorno;
	}
	
	public static boolean isInteger(String source) {

	    if (source == null || source.isEmpty())
	        return false;

	    int index = 0;
	    while (index < source.length() && 
	            Character.isDigit(source.charAt(index))) {
	        index++;
	    }
	    return index == source.length();
	}
	
	//-----------------------------------------------------
	//Registra en tabla los registros de prestamos BlockChain que no se pudieron crear en Optimus
	//-----------------------------------------------------
	public void AgregaATablaPrestamosPendientesPorCargar(Connection conn, List<PrestamosBlockChain> p_PrestamoBC, String p_TipoRegistro, List<MensajeSalida> p_Mensajes, String p_FechaProceso) throws Exception {
		
		SQL sql 												= new SQL();
		List<PrestamoPendiente> prestamosPendientesIngresar 	= new ArrayList<>(); 
		List<PrestamoPendiente> prestamosPendientesActualizar 	= new ArrayList<>();
		HashMap<String, Object> hPrestamosExistente				= new HashMap<String, Object>();
		String strMensaje 										= "";
			
		HashMap<String,Object> hparam = new HashMap<String,Object>();
		hparam.put("p_FolioSistemaExterno"	, p_PrestamoBC.stream().map( prestamo_pdt_cargar -> prestamo_pdt_cargar.laDetails.LAID ).collect( Collectors.toList() ) );
		hparam.put("p_TipoRegistro"			, p_TipoRegistro);
		List<PrestamoPendiente> prestamosPendienteExistente = sql.query(conn, SQL_SELECT_PRTPENDIENTEEXISTE, hparam, PrestamoPendiente.class);
		hPrestamosExistente = Utils.List2Hash( prestamosPendienteExistente, "FolioSistemaExterno" );
		
		for ( PrestamosBlockChain prestamoCrear : p_PrestamoBC ) {
			
			strMensaje 	= "";
			strMensaje 	= p_Mensajes.stream().filter( mensaje -> mensaje.id.equals( Long.valueOf( prestamoCrear.laDetails.LegacyLAID ) ) )
			.map( mensaje -> mensaje.descripcion )
			.collect( Collectors.joining(";") );
			
			PrestamoPendiente prestamoPendiente = ( PrestamoPendiente ) hPrestamosExistente.get( prestamoCrear.laDetails.LAID );
			
			if ( prestamoPendiente == null ) {
				
				PrestamoPendiente objPrestamoPendiente = new PrestamoPendiente();
				objPrestamoPendiente.Nemotecnico			= prestamoCrear.laDetails.Symbol;
				objPrestamoPendiente.Mercado 				= prestamoCrear.laDetails.SecurityType;
				objPrestamoPendiente.DocumentoIdentidad		= prestamoCrear.borrowingClients.BorrowingClientRUT;
				objPrestamoPendiente.TipoDocIdentidad		= "RUT";
				objPrestamoPendiente.Cantidad			  	= prestamoCrear.laDetails.LendingQty;
				objPrestamoPendiente.Tasa				  	= prestamoCrear.laDetails.Yield;
				objPrestamoPendiente.CondicionLiquidacion	= prestamoCrear.laDetails.SettlType;
				objPrestamoPendiente.FechaLiquidacion		= prestamoCrear.laDetails.MaturityDate;
				objPrestamoPendiente.CodigoCorredora		= prestamoCrear.laDetails.BorrowingBrokerageHouseID;
				objPrestamoPendiente.FolioSistemaExterno	= prestamoCrear.laDetails.LAID;
				objPrestamoPendiente.Origen					= "BLOCKCHAIN";
				objPrestamoPendiente.Mensaje				= strMensaje.length() >= 1000 ? strMensaje.substring(0,1000) : strMensaje;
				objPrestamoPendiente.Estado					= "ACTIVO";
				objPrestamoPendiente.TipoRegistro			= p_TipoRegistro;
				objPrestamoPendiente.FechaIngreso			= p_FechaProceso;
				
				prestamosPendientesIngresar.add( objPrestamoPendiente );
				
			} else {
				
				PrestamoPendiente objPrestamoPendiente = new PrestamoPendiente();
				objPrestamoPendiente.IdPrestamosPendientes	=	prestamoPendiente.IdPrestamosPendientes;
				objPrestamoPendiente.Mensaje				=	strMensaje.length() >= 1000 ? strMensaje.substring(0,1000) : strMensaje;
				objPrestamoPendiente.FechaIngreso			=	p_FechaProceso;
				prestamosPendientesActualizar.add( objPrestamoPendiente );
			}
			
		}
		
		if ( prestamosPendientesIngresar.size() > 0 ) {
			sql.batch(conn, SQL_SELECT_INSERTPRTPENDIENTE, prestamosPendientesIngresar, PrestamoPendiente.class);
		}
		
		if ( prestamosPendientesActualizar.size() > 0 ) {
			sql.batch(conn, SQL_SELECT_UPDATEPRTPENDIENTE, prestamosPendientesActualizar, PrestamoPendiente.class);
		}
		
	}
	
	//-------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE - CALL
	//-------------------------------------------------------------------------------------------
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();
		CrearPrestamoDesdeBlockChain_Entrada in = gson.fromJson(jin.toString(), CrearPrestamoDesdeBlockChain_Entrada.class);
		CrearPrestamoDesdeBlockChain_Salida out = logica(con,in);
		
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

			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"CUS\",\"p_PrestamosCreado\":[{\"laDetails\":{\"LAID\":\"61-0000000071084\",\"Status\":\"ACTIVE\",\"Symbol\":\"CINTAC\",\"LADate\":\"2023-04-21\",\"SecurityType\":\"RV\",\"LendingAgreementType\":\"O\",\"SettlType\":\"PH\",\"BorrowingBrokerageHouseID\":\"61\",\"LendingBrokerageHouseID\":\"61\",\"LendingQty\":\"205\",\"MaturityDate\":\"2023-04-26\",\"Yield\":\"1.27\",\"LegacyLAID\":\"71084\"},\"borrowingClients\":{\"LAID\":\"61-0000000071084\",\"BorrowingClientRUT\":\"21087838-6\"}},{\"laDetails\":{\"LAID\":\"61-0000000071085\",\"Status\":\"CREATED\",\"Symbol\":\"CINTAC\",\"LADate\":\"2023-04-21\",\"SecurityType\":\"RV\",\"LendingAgreementType\":\"O\",\"SettlType\":\"PM\",\"BorrowingBrokerageHouseID\":\"61\",\"LendingBrokerageHouseID\":\"61\",\"LendingQty\":\"205\",\"MaturityDate\":\"2023-04-26\",\"Yield\":\"1.02\",\"LegacyLAID\":\"71085\"},\"borrowingClients\":{\"LAID\":\"61-0000000071085\",\"BorrowingClientRUT\":\"11454397-7\"}},{\"laDetails\":{\"LAID\":\"61-0000000071086\",\"Status\":\"ACTIVE\",\"Symbol\":\"HITES\",\"LADate\":\"2023-04-21\",\"SecurityType\":\"RV\",\"LendingAgreementType\":\"O\",\"SettlType\":\"PH\",\"BorrowingBrokerageHouseID\":\"61\",\"LendingBrokerageHouseID\":\"61\",\"LendingQty\":\"206\",\"MaturityDate\":\"2023-04-26\",\"Yield\":\"10.52\",\"LegacyLAID\":\"71086\"},\"borrowingClients\":{\"LAID\":\"61-0000000071086\",\"BorrowingClientRUT\":\"10510689-0\"}},{\"laDetails\":{\"LAID\":\"61-0000000071087\",\"Status\":\"ACTIVE\",\"Symbol\":\"CINTAC\",\"LADate\":\"2023-04-21\",\"SecurityType\":\"RV\",\"LendingAgreementType\":\"O\",\"SettlType\":\"PH\",\"BorrowingBrokerageHouseID\":\"61\",\"LendingBrokerageHouseID\":\"61\",\"LendingQty\":\"301\",\"MaturityDate\":\"2023-04-26\",\"Yield\":\"2.72\",\"LegacyLAID\":\"71087\"},\"borrowingClients\":{\"LAID\":\"61-0000000071087\",\"BorrowingClientRUT\":\"10510689-0\"}},{\"laDetails\":{\"LAID\":\"61-0000000071088\",\"Status\":\"ACTIVE\",\"Symbol\":\"ANTARCHILE\",\"LADate\":\"2023-04-21\",\"SecurityType\":\"RV\",\"LendingAgreementType\":\"T\",\"SettlType\":\"CN\",\"BorrowingBrokerageHouseID\":\"61\",\"LendingBrokerageHouseID\":\"61\",\"LendingQty\":\"300\",\"MaturityDate\":\"2023-04-25\",\"Yield\":\"0.6000\",\"LegacyLAID\":\"71088\"},\"borrowingClients\":{\"LAID\":\"61-0000000071088\",\"BorrowingClientRUT\":\"160-0\"}},{\"laDetails\":{\"LAID\":\"61-0000000071089\",\"Status\":\"ACTIVE\",\"Symbol\":\"CENCOSUD\",\"LADate\":\"2023-04-21\",\"SecurityType\":\"RV\",\"LendingAgreementType\":\"T\",\"SettlType\":\"CN\",\"BorrowingBrokerageHouseID\":\"61\",\"LendingBrokerageHouseID\":\"61\",\"LendingQty\":\"300\",\"MaturityDate\":\"2023-04-25\",\"Yield\":\"0.6000\",\"LegacyLAID\":\"71089\"},\"borrowingClients\":{\"LAID\":\"61-0000000071089\",\"BorrowingClientRUT\":\"160-0\"}},{\"laDetails\":{\"LAID\":\"61-0000000071090\",\"Status\":\"ACTIVE\",\"Symbol\":\"COPEC\",\"LADate\":\"2023-04-21\",\"SecurityType\":\"RV\",\"LendingAgreementType\":\"T\",\"SettlType\":\"CN\",\"BorrowingBrokerageHouseID\":\"61\",\"LendingBrokerageHouseID\":\"61\",\"LendingQty\":\"300\",\"MaturityDate\":\"2023-04-25\",\"Yield\":\"0.6000\",\"LegacyLAID\":\"71090\"},\"borrowingClients\":{\"LAID\":\"61-0000000071090\",\"BorrowingClientRUT\":\"160-0\"}}]}";
			CrearPrestamoDesdeBlockChain_Entrada Objin = gson.fromJson(json, CrearPrestamoDesdeBlockChain_Entrada.class);
			CrearPrestamoDesdeBlockChain demo = new CrearPrestamoDesdeBlockChain();
 			
			System.out.println("ENTRADA:" + Objin.toString());
			
 			try {
 				CrearPrestamoDesdeBlockChain_Salida ObjOut = demo.logica(con, Objin);
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
