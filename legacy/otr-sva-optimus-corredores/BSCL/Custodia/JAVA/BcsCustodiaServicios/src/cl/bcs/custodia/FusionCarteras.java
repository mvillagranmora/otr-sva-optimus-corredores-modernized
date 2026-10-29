package cl.bcs.custodia;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.custodia.ConsultarEjercicioBloqueo.Carteras;
import cl.bcs.custodia.IngresoRetiroTitulosJava.IngresoRetiroTitulosEntrada;
import cl.bcs.custodia.IngresoRetiroTitulosJava.IngresoRetiroTitulosEntrada.ObjInstrumento;
import cl.bcs.custodia.IngresoRetiroTitulosJava.IngresoRetiroTitulosSalida;
import cl.bcs.custodia.IngresoRetiroTitulosJava.IngresoRetiroTitulosSalida.ObjResultado;
import cl.bcs.custodia.pojo.Cartera;
import cl.bcs.custodia.utils.RetiroCustodia;
import cl.bcs.custodia.utils.ValorizarInstrumento;
import cl.bcs.custodia.utils.RetiroCustodia.ObjDatosEntrada;
import cl.bcs.custodia.utils.ValorizarInstrumento.ObjEntradaInstrumentos;
import cl.bcs.custodia.utils.ValorizarInstrumento.ObjResultadoValorizacion;
import cl.bcs.custodia.utils.ValorizarInstrumento.ValorizarInstrumentoEntrada;
import cl.bcs.custodia.utils.ValorizarInstrumento.ValorizarInstrumentoSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;




public class FusionCarteras implements JavaCall{
	
	static final Logger logger 					= LogManager.getLogger(FusionCarteras.class);
	private final String SQL_SEL_Careras		= loadResourceToString("cl/bcs/custodia/sql/FusionCarteras_SEL_Masiva.sql"				);
	private final String SQL_ConsultaMesas		= loadResourceToString("cl/bcs/custodia/sql/FusionCarteras_ConsultaMesas.sql"			);
	private final String SQL_ConsultarModulos	= "SELECT * FROM BSCL.\"BSCL.Plataforma.BD::PAU.Modulo\" WHERE \"CodigoModulo\" IN ( \'FAC\',\'CUS\' ) ;" ;
	private final String SQL_ConsultarFlag		= "SELECT \"Valor\" FROM  \"BSCL.Custodia.BD::CUS.Configuracion\" WHERE \"CodigoParametro\"  = 'EjecutandoFusionCarteras';" ;
	private final String SQL_UpdaterFlag		= "UPDATE \"BSCL.Custodia.BD::CUS.Configuracion\" SET \"Valor\" = :Valor  WHERE \"CodigoParametro\"  = 'EjecutandoFusionCarteras';" ;
	private final String SQL_AddFusionAux		= loadResourceToString("cl/bcs/custodia/sql/FusionCarteras_AddFusionAux.sql"			);
	private final String SQL_AddHistorico 		= loadResourceToString("cl/bcs/custodia/sql/FusionCarteras_AddHistorico.sql"			);

	public static class FusionCarterasEntrada {
		public Long						p_IdEmpresa ;
		public Long						p_IdUsuario ;
		public String					p_IdModulo  ;	
		public Long 					p_IdPais;
		public String toString() {
			return Utils.printJson(this, false);
		};

	};
	public static class FusionCarterasSalida{
		public Long					P_OUT_ERRORESCONTADOR 	= 0L;
		public List<MensajeSalida>	P_OUT_MENSAJES 			= new ArrayList<MensajeSalida>();
	}
	public static class ObjMesas
	{

		public long			IdMesa; 
		public long			IdCliente;
		public long			IdPortafolio;
		public String 		IdEstadoMesa;
	};
	public static class Modulo{
		public String 	CodigoModulo;
		public String	IdEstado	;
	}
	public static class IngresoHISFusion{
		Date FFusion;
		Long IdModulo;
		Long IdE;
		Long Usuario;
		Long FOld;
		Long FolioNew;
	}
	public static class AddHISFusion{
		public String FechaFusion;
		public BigDecimal CantidadFusion;
		public BigDecimal TasaFusion;
		public BigDecimal ValorRescateFusion;
		public String IdClasificacionTipoInstrumento;
		public BigDecimal MontoFusion;
		public BigDecimal MontoValorizado;
		public Long IdCarteraResultante;
		public String IdModulo;
		public String IdUsuario;
		public String IdEmpresa;
		
		public String toString() {
			return Utils.printJson(this, false);
		};

	};	
	public static class AddFusionAux{
		public Long IdCartera;
		public Long FolioCustodia;
		public BigDecimal Cantidad;
		public BigDecimal Tasa;
		public BigDecimal ValorRescate;
		public BigDecimal CorteMinimo;
		public String EsFungible;
		public String toString() {
			return Utils.printJson(this, false);
		};

	};
	private class Valorizaciones{
		public Integer 		PosValorizacion;
		public Long			IdGeneracionMovimiento;
		public Boolean 		ValorizacionOK	= false;
		public BigDecimal	MontoSuma;
		public BigDecimal 	Tasa;
		public List<Long>	Folios = new ArrayList<Long>();
	}
	public static class FlagFusion {
		public String Valor;
	}
	static Boolean Debug = true;
	Boolean Flag = false;
	public List<List<Long>> ListaPortafoliosFusion = new ArrayList<>(); 
	String fechaSistema = "";	
	FusionCarterasSalida out = new FusionCarterasSalida();	
	
	List <ObjInstrumento> 		listasalida 			= new ArrayList<ObjInstrumento>		()	;	// Listado Carteras ingreso/retiro 
	List <List<ObjInstrumento>> CarterasRetiro 			= new ArrayList<>					()	;	// === pero para los retiro
	List <ObjInstrumento> 		CarterasIngreso 		= new ArrayList<ObjInstrumento>		()	;	// === pero para losingresos

	
	List<Valorizaciones> ValorizacionesFinal	= new ArrayList<Valorizaciones>()	;	// Listado para la verificacion de las valorizaciones y qui 	
	
	public FusionCarterasSalida logica(Connection con ,FusionCarterasEntrada entrada ) throws SQLException {	
		logger.info("Inicio Procedimiento de Fusion de carteras") ; 
	
		out.P_OUT_ERRORESCONTADOR = 0L;
		SQL 	sql	=	new SQL();	
		
		List	<Long> IdPortafolioMesasFusionables 	= ValidacionesEejcucion(sql, con, entrada.p_IdEmpresa);
		List	<List<Cartera>> ListaListasCarteras 	= new ArrayList<>();
		List	<Cartera> 		TablaCartera;		
		HashMap	<String,Object> paramsIN 				= new HashMap<>();	
		//fechaSistema = "2023-02-11";	// para pruebas manuales
		
		if (out.P_OUT_ERRORESCONTADOR == 0L) {
			paramsIN.put("Portafolios", IdPortafolioMesasFusionables );
			paramsIN.put("Instrumento", "RF");
			paramsIN.put("Orden", "\"IdMesa\", \"IdEmisor\" ,\"IdMonedaSistema\", \"IdMonedaInstrumento\" ,\"IdTipoReajuste\" ,\"IdBoveda\" ,\"Nemotecnico\" , \"FechaVencimientoInstrumento\", \"CodigoIFRS\" ");
			paramsIN.put("FSistema",fechaSistema );
			String msg;
			try 
			{
				TablaCartera = sql.query(con, SQL_SEL_Careras, paramsIN, Cartera.class);
				
				if (TablaCartera.size() > 1) {
					ListaListasCarteras.addAll( ListadoRF(TablaCartera) );
				}else {msg = "Sin carteras Candidatas Fusion RF";  		logger.info(msg) ; 		MensajeSalida("INFO",msg);}
				
				paramsIN.remove("Instrumento");	paramsIN.put("Instrumento", "IF");
				paramsIN.remove("Orden");paramsIN.put("Orden","\"IdMesa\", \"IdEmisor\" ,\"IdMonedaSistema\", \"IdMonedaInstrumento\" ,\"IdTipoReajuste\" ,\"IdBoveda\" ,\"Nemotecnico\" , \"FechaVencimientoInstrumento\", \"CodigoIFRS\" , \"CorteMinimo\"  , \"ValorRescate\" ,\"EsFungible\" ");
				
				TablaCartera = sql.query(con, SQL_SEL_Careras, paramsIN, Cartera.class);
				
				if (TablaCartera.size() > 1) {
					ListaListasCarteras.addAll(ListadoIF(TablaCartera));
					for ( List<Cartera> listCarteras : ListaListasCarteras ) {
						for (Cartera Carteras: listCarteras) {
							System.out.println( Carteras.IdCartera )	;	
						}
						System.out.println("-")	;	
					}
				}else {msg = "Sin carteras Candidatas Fusion IF" ;   	logger.info(msg) ;		MensajeSalida("INFO",msg); }
				
				if ( ListaListasCarteras.size() > 0) {
				LLamadoIngresoRetiro ( ListaListasCarteras,con , entrada);
				}else {msg = "Sin Fusiones RF / IF" ;  					logger.info(msg) ;		MensajeSalida("INFO",msg); }
				
				
				if (out.P_OUT_ERRORESCONTADOR == 0L) {
					msg = "Ejecucion exitosa";
					MensajeSalida("OK",msg);
				}else {
					con.rollback();		
					msg = "Hubo uno o mas errores en la ejecucion del proceso de fusion, no se hicieron fusiones";
					logger.info(msg) ;	logger.info(out) ;
					MensajeSalida("INFO",msg);
					
				}	
				
			}
			catch (Exception e) { 
				System.out.println(e);
				con.rollback();		
				msg = "Hubo uno o mas errores en la ejecucion del proceso de fusion, no se hicieron fusiones";
				logger.info(msg) ;	logger.info(out) ;
				MensajeSalida("INFO",msg);
				}
		}


	
	if (Debug) { System.out.println(out.P_OUT_MENSAJES)	;	 }
	if (Flag) {
		HashMap	<String,Object> params 	= new HashMap<>();	
		params.put("Valor", "N" );
		sql.call(con, SQL_UpdaterFlag, params , new HashMap<>() );	
		con.commit();
	}
	return out;
		
	}
	
	private List<Long> ValidacionesEejcucion(SQL sql,Connection con,Long IdEmpresa) { //Validaciones necesarias para la ejecucion del proceso de fusion 
		// Modulo de facturacion cerrado, Modulo de custodia abierto (requerimiento del ingresoretirotitulos, y mesas que que aceptan fusion cerradas)
		List<ObjMesas> 	Mesas 			= new ArrayList<ObjMesas>();
		List<Modulo> 	Modulos 		= new ArrayList<Modulo>();	
		List<Long> 		IdPortafolioMesasFusionables = new ArrayList<Long>();
		String 			FusionActiva 	= "S" ;
		try {
			Mesas 			= sql.query(con, SQL_ConsultaMesas, new HashMap<>(), ObjMesas.class)					;
			Modulos 		= sql.query(con, SQL_ConsultarModulos, new HashMap<>(), Modulo.class )					;			 
			FusionActiva 	= sql.query(con, SQL_ConsultarFlag,new HashMap<>() , FlagFusion.class).get(0).Valor  	;
			fechaSistema	= ConfiguracionSistema.getParametroConfiguracionString(con, IdEmpresa , "PLATAFORMA", "PLA.FechaSistema");
			
			if (FusionActiva.equalsIgnoreCase("S")) {
				MensajeSalida ("ERROR","La fusion ya se encuentra ejecutandose");					
			}else {
				HashMap	<String,Object> params 	= new HashMap<>();	
				params.put("Valor", "S" );
				sql.call(con, SQL_UpdaterFlag, params , new HashMap<>() );	
				con.commit();
				Flag = true;	// boolean para saber si esta instancia especifica fue la que seteo el flag de ejecucion en S

			}
			
			for (Modulo M : Modulos )
			{
				if ( M.CodigoModulo.equalsIgnoreCase("FAC") && M.IdEstado.equalsIgnoreCase("ABT")  ) {
					MensajeSalida ("ERROR","Modulo de facturacion Abierto, se requiere cerrar modulo para fusionar");
				}
				if ( M.CodigoModulo.equalsIgnoreCase("CUS") && M.IdEstado.equalsIgnoreCase("CER")  ) {
					MensajeSalida ("ERROR","Modulo de Custodia Cerrado, se requiere abrir modulo para fusionar.");
				}				
			}		
			for ( ObjMesas Mesa :Mesas ) {		
				if (Mesa.IdEstadoMesa.equals("A")) {	// Valida que las mesas RF que aplica fusion se encuentren cerradas
					String msg=Diccionario.getMensaje(con,IdEmpresa,"MESA_ABIERTA_CERRAR_PARA_FUSIONAR").add("IdMesa", Mesa.IdMesa).msg();
					MensajeSalida ("ERROR",msg);
				}
				if ( !IdPortafolioMesasFusionables.contains( Mesa.IdPortafolio) ) {
					IdPortafolioMesasFusionables.add( Mesa.IdPortafolio  );
				}
			}
		} catch (Exception e) {
			logger.info(e)	;
			MensajeSalida("ERROR","Error en la Fusion");
		}	
		return IdPortafolioMesasFusionables;
	}

	private void MensajeSalida (String Tipo, String Desc) {
		logger.info(Desc) ;
		MensajeSalida msg = new MensajeSalida();
		if (Tipo.equalsIgnoreCase("error")) {
			out.P_OUT_ERRORESCONTADOR++; 
		}
		msg.tipo = Tipo.toUpperCase();
		msg.descripcion = Desc;
		out.P_OUT_MENSAJES.add(msg);
	}
	
	private List<List<Cartera>> ListadoRF(List<Cartera> TablaCartera ) {	
		
		List<Cartera> 		NuevaListaCarteraAFusionar 	= new ArrayList<Cartera>();
		List<List<Cartera>> ListaListaCarterasFusion 	= new ArrayList<>(); 
		int NCartIN = TablaCartera.size();
		List<Long> IdFolio = new ArrayList<>();
		Cartera PrimerItem = TablaCartera.remove(0);
		
		
		while (TablaCartera.size() > 0) {
			for (int i = 0 ; i<TablaCartera.size() ;i++) {	

				if ( 	PrimerItem.IdPortafolio.equals(TablaCartera.get(i).IdPortafolio) 					&& PrimerItem.IdClasificacionTipoInstrumento.equals(TablaCartera.get(i).IdClasificacionTipoInstrumento)  &&
						PrimerItem.IdEmisor.equals( TablaCartera.get(i).IdEmisor ) 							&&
						PrimerItem.IdMonedaInstrumento.equals( TablaCartera.get(i).IdMonedaInstrumento )  	&& PrimerItem.IdTipoReajuste.equals( TablaCartera.get(i).IdTipoReajuste )   				&&
						PrimerItem.IdBoveda.equals( TablaCartera.get(i).IdBoveda )  						&& PrimerItem.Nemotecnico.equals(TablaCartera.get(i).Nemotecnico) 		 					&&
						PrimerItem.CodigoIFRS.equals(TablaCartera.get(i).CodigoIFRS)  						&& PrimerItem.FechaVencimientoInstrumento.equals( TablaCartera.get(i).FechaVencimientoInstrumento)  )  
				{
						NuevaListaCarteraAFusionar.add( TablaCartera.get(i));
						IdFolio.add(TablaCartera.get(i).FolioCartera);
						TablaCartera.remove(i);
						i--;
				}
			}
			if (NuevaListaCarteraAFusionar.size() > 0) {
	
				 NuevaListaCarteraAFusionar.add(PrimerItem);
				 IdFolio.add(PrimerItem.FolioCartera);
				 ListaPortafoliosFusion.add(IdFolio);
				 IdFolio = new ArrayList<>();
				 ListaListaCarterasFusion.add(NuevaListaCarteraAFusionar);
				 NuevaListaCarteraAFusionar = new ArrayList<Cartera>();
			}
			if (TablaCartera.size() > 0) { PrimerItem = TablaCartera.remove(0);	}
		}
		logger.info("Carteras IN RF  = "+  NCartIN  + " ;  Fusiones :" + ListaListaCarterasFusion.size() );
		return ListaListaCarterasFusion;
	}

	private List<List<Cartera>> ListadoIF(List<Cartera> TablaCartera ) {
		List<Cartera> 		NuevaListaCarteraAFusionar 	= new ArrayList<Cartera>();
		List<List<Cartera>> ListaListaCarterasFusion 	= new ArrayList<>(); 
		int NCartIN =  TablaCartera.size() ;
		List<Long> IdPortafolio = new ArrayList<>();
		Cartera PrimerItem = TablaCartera.remove(0);
				
		while (TablaCartera.size() > 0) {
			for (int i = 0 ; i<TablaCartera.size() ;i++) {
				if ( 	PrimerItem.IdPortafolio.equals(TablaCartera.get(i).IdPortafolio) 					&& PrimerItem.IdClasificacionTipoInstrumento.equals(TablaCartera.get(i).IdClasificacionTipoInstrumento)  &&
						PrimerItem.IdEmisor.equals( TablaCartera.get(i).IdEmisor ) 							&& PrimerItem.EsFungible.equals(TablaCartera.get(i).EsFungible)&&
						PrimerItem.IdMonedaInstrumento.equals( TablaCartera.get(i).IdMonedaInstrumento )  	&& PrimerItem.IdTipoReajuste.equals( TablaCartera.get(i).IdTipoReajuste )   	&&
						PrimerItem.IdBoveda.equals( TablaCartera.get(i).IdBoveda )  						&& PrimerItem.NemoSVS.equals(TablaCartera.get(i).NemoSVS) 		&&
						PrimerItem.CodigoIFRS.equals(TablaCartera.get(i).CodigoIFRS)  						&& PrimerItem.FechaVencimientoInstrumento.equals( TablaCartera.get(i).FechaVencimientoInstrumento )  &&
						PrimerItem.CorteMinimo.equals(TablaCartera.get(i).CorteMinimo )	
					)  
				{
					NuevaListaCarteraAFusionar.add( TablaCartera.get(i));
					IdPortafolio.add(TablaCartera.get(i).IdPortafolio);
					TablaCartera.remove(i);
					
					i--;
				}

			}
			if (NuevaListaCarteraAFusionar.size() > 0) {
				 NuevaListaCarteraAFusionar.add(PrimerItem);
				 IdPortafolio.add(PrimerItem.IdPortafolio);
				 ListaPortafoliosFusion.add(IdPortafolio);
				 IdPortafolio = new ArrayList<>();
				 ListaListaCarterasFusion.add(NuevaListaCarteraAFusionar);
				 NuevaListaCarteraAFusionar = new ArrayList<Cartera>();
			}
			if (TablaCartera.size() > 0) { PrimerItem = TablaCartera.remove(0);	}
		}

		logger.info("Carteras IN IF  = "+  NCartIN  + " ;  Fusiones :" + ListaListaCarterasFusion.size() );
		return  ListaListaCarterasFusion;
		
	}
	
	private void LLamadoIngresoRetiro(List<List<Cartera>> ListadoCarterasIN , Connection con ,FusionCarterasEntrada in) throws Exception  {
		IngresoRetiroTitulosEntrada 			EntradaIngresoRetiro	= new IngresoRetiroTitulosEntrada	()  ;
		RetiroCustodia.RetiroCustodiaEntrada	EntradaRetiroCustodia 	= new RetiroCustodia.RetiroCustodiaEntrada();
		IngresoRetiroTitulosJava IngresoRetiro 				= new IngresoRetiroTitulosJava		() 	;

		ObjInstrumento Instrumentos = new ObjInstrumento(); 
		
		ObjDatosEntrada CarteraRetiro = new RetiroCustodia.ObjDatosEntrada(); 
		
		if (Debug) {	
			for (List<Cartera> ListadoFusiondebug :ListadoCarterasIN) {
				System.out.println( "  --  " );
				for(Cartera A :ListadoFusiondebug) {	System.out.println("|| Carteras Fusionables || Folio: "+ A.FolioCartera + " Tasa: " + A.TasaCompra +" Cantidad: " +  A.Cantidad);}	
				}	
			}
		
		Long IdGen = 1L;	int IndexCartera = 0;
		for ( List<Cartera> Lista : ListadoCarterasIN ) 
			{
			BigDecimal Cantidad = BigDecimal.ZERO,Monto= BigDecimal.ZERO,valorRescate= BigDecimal.ZERO;			
			Valorizaciones CarteraIngresoIdGenMovCust = new Valorizaciones();			
			Cartera CarteraFusionada = new Cartera();	
			CarteraFusionada = ListadoCarterasIN.get(IndexCartera).get(0);	
			ArrayList<ObjInstrumento> ListaTemporalCarterasRetiro = new ArrayList<ObjInstrumento>();
			
			for (Cartera CarteraActual : Lista) {
					if (CarteraActual.IdClasificacionTipoInstrumento.equals("RF")) {
						Cantidad 		= 	Cantidad.add(CarteraActual.INGCantidad);
				}else {
					if (CarteraActual.EsFungible.equals("N")) {
						Cantidad 		= 	Cantidad.add(CarteraActual.INGCantidad);
					}else {		Cantidad = BigDecimal.valueOf(1); 	}
				}
				Monto			= 	Monto.add(CarteraActual.INGMontoCompraActualMonedaInstrumento);
				valorRescate	=	valorRescate.add(CarteraActual.INGValorRescate);	
				CarteraIngresoIdGenMovCust.Folios.add(CarteraActual.FolioCartera);
			}

			CarterasRetiro.add( ListaTemporalCarterasRetiro );
			//   Construccion de los Ingresos de titulo 
			
			Instrumentos = new ObjInstrumento();
			CarteraIngresoIdGenMovCust.IdGeneracionMovimiento = IdGen;
			CarteraIngresoIdGenMovCust.MontoSuma = Monto;
			ValorizacionesFinal.add(CarteraIngresoIdGenMovCust);
			
			Instrumentos.IdGenerarMovimientoCustodia 	= 	IdGen;													//Registro secuencial unico proceso
			Instrumentos.FechaOperacion 				= 	fechaSistema ;											//debe venir en formato AAAA-MM-DD
			Instrumentos.FechaOperacionOriginal 		= 	fechaSistema;											//debe venir en formato AAAA-MM-DD
			Instrumentos.FechaVencimiento    			= 	CarteraFusionada.FechaVencimientoInstrumento.toString();		//debe venir en formato AAAA-MM-DD				
			Instrumentos.FechaLiquidacion 				= 	CarteraFusionada.FechaLiquidacion.toString() ;					//debe venir en formato AAAA-MM-DD
			Instrumentos.IdCondicionLiquidacion 		= 	CarteraFusionada.IdCondicionLiquidacion  ;				//si no usara Condicion de Liq, enviar un valor <= 0
			Instrumentos.IdConcepto 					= 	"INGFUS";												// Ingreso por fusion - INGFUS
			Instrumentos.IdBolsa 						= 	CarteraFusionada.IdBolsa  ;
			Instrumentos.IdTipoOperacion 				= 	"INGTIT" ;												// INGRESO
			Instrumentos.IdTipoProducto 				= 	CarteraFusionada.IdTipoProducto ;
			Instrumentos.IdCliente 						= 	CarteraFusionada.IdCliente  ;
			Instrumentos.IdPortafolio 					= 	CarteraFusionada.IdPortafolio ;
			Instrumentos.IdClienteDestino 			 	= 	0L;
			Instrumentos.IdPortafolioDestino 			= 	0L;
			Instrumentos.IdBoveda 						=  	CarteraFusionada.IdBoveda ;
			Instrumentos.CuentaBoveda 					= 	CarteraFusionada.CuentaBoveda  ;			
			Instrumentos.IdCuentaCustodioPortafolio  	= 	CarteraFusionada.IdCuentaCustodioPortafolio  ;			// Campo nuevo de peru				
			Instrumentos.IdBovedaRelacionada 			= 	"" 	;
			Instrumentos.CuentaBovedaRelacionada 		= 	""	;
			Instrumentos.IdCuentaCustodioPortafolioRelacionado = 0L;												// Campo nuevo de peru
			Instrumentos.esTraspasoCliente 				= 	"N";
			Instrumentos.esTraspasoPortafolio 			= 	"N";		
			Instrumentos.esTraspasoBoveda 				= 	"N";		
			Instrumentos.DebeCertificar 				= 	"S";
			Instrumentos.EsGarantia 					= 	"N";
			Instrumentos.PlazoEnDias 					= 	CarteraFusionada.PlazoEnDiasOperacion; 
			//Instrumentos.IdCartera 					= 	0L; // CarteraFusionada.IdCartera  ;					//Cartera nueva no tiene idcartera
			Instrumentos.FolioCartera 					= 	CarteraFusionada.FolioCartera;			
			Instrumentos.FolioExternoReferencia 		= 	CarteraFusionada.FolioExternoReferencia;				//comprobante de referencia
			Instrumentos.Glosa 							= 	CarteraFusionada.Glosa;
			Instrumentos.IndicadorNominal 				= 	"N"; 
			Instrumentos.IdAgente 						= 	CarteraFusionada.IdAgente;
			Instrumentos.IdSucursal 					= 	CarteraFusionada.IdSucursal;			
			Instrumentos.IdTipoCanalIngreso 			= 	CarteraFusionada.IdTipoCanalIngreso;		
			Instrumentos.IdClasificacionTipoInstrumento = 	CarteraFusionada.IdClasificacionTipoInstrumento;
			Instrumentos.IdTipoInstrumento 				= 	CarteraFusionada.IdTipoInstrumento;
			Instrumentos.IdInstrumento 					= 	CarteraFusionada.IdInstrumento ;
			Instrumentos.Nemotecnico 					= 	CarteraFusionada.Nemotecnico;
			Instrumentos.IdEmisor 						=	CarteraFusionada.IdEmisor;
			Instrumentos.IdMonedaInstrumento 			= 	CarteraFusionada.IdMonedaInstrumento;
			Instrumentos.IdMonedaEmision 				= 	CarteraFusionada.IdMonedaEmision;
			Instrumentos.IdTipoReajuste 				= 	CarteraFusionada.IdTipoReajuste;
			Instrumentos.IdMonedaAdicional 				= 	CarteraFusionada.IdMonedaAdicional;
			Instrumentos.FechaEmisionInstrumento 		= 	CarteraFusionada.FechaEmisionInstrumento.toString();
			Instrumentos.FechaVencimientoInstrumento 	= 	CarteraFusionada.FechaVencimientoInstrumento.toString()	;
			Instrumentos.Fungible 						= 	CarteraFusionada.EsFungible;
			Instrumentos.NemoSVS 						= 	CarteraFusionada.NemoSVS;		
			Instrumentos.Precio 						= 	CarteraFusionada.PrecioCompra;
			Instrumentos.Cantidad 						= 	Cantidad;		
			Instrumentos.ValorRescate 					= 	valorRescate;
			Instrumentos.CorteMinimo 					= 	CarteraFusionada.CorteMinimo;	
			Instrumentos.Monto							= 	Monto;
			Instrumentos.MontoEnMonedaInstrumento 		= 	CarteraFusionada.MontoCompraMonedaInstrumento;
			Instrumentos.Tasa 							= 	CarteraFusionada.TasaCompra;
			Instrumentos.TasaAgente 					= 	CarteraFusionada.TasaAgente;
			Instrumentos.CodigoIFRS 					= 	CarteraFusionada.CodigoIFRS;
			Instrumentos.IdRegimenFondoMutuo 			= 	CarteraFusionada.IdRegimenFondoMutuo;
			Instrumentos.Procesar 						= 	"S";//Por defecto: S=Procesar; N=No Procesar
			CarterasIngreso.add(Instrumentos);
			IdGen++;
		IndexCartera++;
		}

		ValorizarInstrumentoSalida ResultadoValorizador = new ValorizarInstrumentoSalida();	
		ValorizacionInstrumentos( con, in);
		//ToDo: agregar excepcion de valorizador
		int i = 0;
		List<ObjDatosEntrada> DatosEntrada_RetiroCustodia 		= new ArrayList<ObjDatosEntrada>();
	
		for ( List<Cartera> Lista : ListadoCarterasIN ) 
		{	
		  if (ValorizacionesFinal.get(i).ValorizacionOK) 
		  {
		  	for (Cartera CarteraActual : Lista) 
		  	{
		  	CarteraRetiro = new RetiroCustodia.ObjDatosEntrada(); 
		  	CarteraRetiro.IdGenerarMovimientoCustodia	  			= IdGen		;
		  	CarteraRetiro.TablaAfectada 				  			= "CARTERA" ;
		  	CarteraRetiro.IdTipoOperacion				  			= "RETTIT" 	;
		  	CarteraRetiro.IdConcepto					  			= "RETFUS"	;
		  	CarteraRetiro.AccionOperacion				  			= "RETIRO"	;
		  	CarteraRetiro.EsRetiroPorFusion							= "S"		;
		  	CarteraRetiro.IdCustodia								= CarteraActual.IdCartera;
		  	CarteraRetiro.FechaLiquidacion				  			= CarteraActual.FechaLiquidacion ;  // fechaSistema?
		  	CarteraRetiro.IdCarteraOrigen							= CarteraActual.IdCartera;
		  	CarteraRetiro.FolioComprobanteBusqueda		  			= CarteraActual.FolioComprobante ;
		  	CarteraRetiro.FolioCustodia				  				= CarteraActual.FolioCartera ;
		  	CarteraRetiro.IdCliente					  				= CarteraActual.IdCliente ;
		  	CarteraRetiro.IdPortafolio					  			= CarteraActual.IdPortafolio ;					
		  	CarteraRetiro.FechaOperacion				  			= CarteraActual.FechaOperacion ;
		  	CarteraRetiro.FechaComprobante				  			= CarteraActual.FechaOperacion ;
		  	CarteraRetiro.IdCondicionLiquidacion		  			= CarteraActual.IdCondicionLiquidacion ;					
		  	CarteraRetiro.IdTipoCanalIngreso			  			= CarteraActual.IdTipoCanalIngreso ;
		  	CarteraRetiro.IdTipoComprobante			  				= CarteraActual.IdTipoComprobante ;
		  	CarteraRetiro.IdTipoReajusteInstrumento	  				= CarteraActual.IdTipoReajuste;
		  	CarteraRetiro.FolioComprobante				  			= CarteraActual.FolioComprobante ;
		  	CarteraRetiro.IdUsuario					  				= CarteraActual.IdUsuario ;
		  	CarteraRetiro.IdSucursal					  			= CarteraActual.IdSucursal ;
		  	CarteraRetiro.IdAgente						  			= CarteraActual.IdAgente ;
		  	CarteraRetiro.Glosa						  				= CarteraActual.Glosa ;
		  	CarteraRetiro.IdInstrumento				  				= CarteraActual.IdInstrumento;
		  	CarteraRetiro.FechaVencimientoInstrumento	  			= CarteraActual.FechaVencimientoInstrumento ;
		  	CarteraRetiro.IdBoveda						  			= CarteraActual.IdBoveda ;
		  	CarteraRetiro.CuentaBoveda					  			= CarteraActual.CuentaBoveda ;
		  	CarteraRetiro.IdEmisor						  			= CarteraActual.IdEmisor ;
		  	CarteraRetiro.EsFungible					  			= CarteraActual.EsFungible ;
		  	CarteraRetiro.IdMonedaAdicional			  				= CarteraActual.IdMonedaAdicional ;
		  	CarteraRetiro.IdMonedaEmision				  			= CarteraActual.IdMonedaEmision ;					
		  	CarteraRetiro.MontoCompraMonedaInstrumento	  			= CarteraActual.MontoCompraMonedaInstrumento ;
		  	CarteraRetiro.Cantidad						  			= CarteraActual.Cantidad ;
		  	CarteraRetiro.CorteMinimo					  			= CarteraActual.CorteMinimo ;
		  	CarteraRetiro.ValorRescate					  			= CarteraActual.ValorRescate;
		  	CarteraRetiro.PrecioCompra					  			= CarteraActual.PrecioCompra ;
		  	CarteraRetiro.TasaCompra					  			= CarteraActual.TasaCompra ;			
		  	CarteraRetiro.IdOperacion					  			= CarteraActual.IdOperacion ;
		  	CarteraRetiro.IdClasificacionTipoInstrumento  			= CarteraActual.IdClasificacionTipoInstrumento ;
		  	CarteraRetiro.NemoSVS									= CarteraActual.NemoSVS;
		  	CarteraRetiro.Nemotecnico								= CarteraActual.Nemotecnico;
		  	
		  	IdGen++;
		  	DatosEntrada_RetiroCustodia.add(CarteraRetiro);	
		  	} // for Cartera CarteraActual : Lista
		  }// IF
		  i++;
		}// for List<Cartera> Lista : ListadoCarterasIN
		
		
		
		
		
		EntradaRetiroCustodia.p_DatosEntrada			= DatosEntrada_RetiroCustodia	;
		EntradaRetiroCustodia.p_IdEmpresa				= in.p_IdEmpresa;
		EntradaRetiroCustodia.p_IdUsuario 				= in.p_IdUsuario;
		EntradaRetiroCustodia.p_IdModulo 				= in.p_IdModulo	;
		EntradaRetiroCustodia.p_TicketAtencion			=	1L	;
		EntradaRetiroCustodia.p_SimularOperacion		=	"N"	;
		EntradaRetiroCustodia.p_ValorizacionAutomatica 	= 	"N"	;
		
		RetiroCustodia retiroCustodia = new RetiroCustodia();
		RetiroCustodia.RetiroCustodiaSalida resultRetiros = retiroCustodia.logica(con, EntradaRetiroCustodia);	
		
		if ( !(resultRetiros.P_OUT_ERRORESCONTADOR.compareTo(0L) == 0) ) {
			MensajeSalida ("ERROR","Errores en el retiro de titulos");
		}
		
		out.P_OUT_MENSAJES.addAll(resultRetiros.P_OUT_MENSAJES);
		
		for (List<ObjInstrumento> ListCarterasRetiro: CarterasRetiro) {
			listasalida.addAll(ListCarterasRetiro);
		}
		listasalida.addAll(CarterasIngreso);
		
		EntradaIngresoRetiro.p_IdUsuario 		= 	in.p_IdUsuario;		
		EntradaIngresoRetiro.p_IdModulo 		=	in.p_IdModulo;
		EntradaIngresoRetiro.p_simularOperacion =	"N"	;
		EntradaIngresoRetiro.p_UsaReglasNegocio	= 	"S"	;
		EntradaIngresoRetiro.P_INSTRUMENTOS 	= 	listasalida;
		EntradaIngresoRetiro.p_Certifica 		= 	"N"; 
		EntradaIngresoRetiro.p_IdEmpresa 		= 	in.p_IdEmpresa;
		
		IngresoRetiroTitulosSalida resultadoIngresoRetiro = IngresoRetiro.logica(con,EntradaIngresoRetiro) ;
		
		if (resultadoIngresoRetiro.P_OUT_ERRORESCONTADOR == 0L ) {
			LLenarHistorico(resultadoIngresoRetiro,  ListadoCarterasIN , con, in);
			System.out.println(resultadoIngresoRetiro);
		}else{		
			System.out.println(resultadoIngresoRetiro);
			MensajeSalida ("ERROR","Errores en el ingreso de titulos");
		}
	}
	
	private ValorizarInstrumentoSalida ValorizacionInstrumentos( Connection con, FusionCarterasEntrada in ) throws Exception {
		List<ObjEntradaInstrumentos> ListaAValorizar1 	= new ArrayList<ObjEntradaInstrumentos>		()	;
		List<ObjEntradaInstrumentos> ListaAValorizar2 	= new ArrayList<ObjEntradaInstrumentos>		()	;
		ValorizarInstrumento valorizador = new ValorizarInstrumento();
		ValorizarInstrumentoEntrada ValIn = new ValorizarInstrumentoEntrada();
		ValIn.p_IdEmpresa 			= in.p_IdEmpresa;		
		ValIn.p_IdUsuario 			= in.p_IdUsuario;
		ValIn.p_IdModulo 			= in.p_IdModulo;
		ValIn.p_IdPais 				= in.p_IdPais;
		ValIn.p_IdMonedaAdicional 	= "CLP";
		ValIn.p_OrigenProcesamiento = "MESA_UI";

		Long IdValorizacion = 0L;
		/*
		 * 				Construccion Primera valorizacion (de 2), utilizada para optener la tasa y el precio 
		 * 				se busca obtener precio y tasa por lo que se envian nulos
		 */		
		
		for (ObjInstrumento CarteraINGTIT : CarterasIngreso) {
			ObjEntradaInstrumentos TestV = LLenarObjEntradaInstrumento(CarteraINGTIT, IdValorizacion, null , 0 , "FIRST");
			IdValorizacion++;
			ListaAValorizar1.add(TestV);
		}ValIn.p_Instrumentos = ListaAValorizar1;

		ValorizarInstrumentoSalida ResultadoValorizador = new ValorizarInstrumentoSalida();
			
		System.out.println("llamada 1 a valorizador ------ ");
		ResultadoValorizador = valorizador.logica(con,ValIn);		// -- Primer llamado al valorizador donde se busca obtener la tasa -- //



		List<ObjResultadoValorizacion>	InstrumentosValorizados = ResultadoValorizador.P_OUT_INSTRUMENTOSVALORIZADOS;
		List<ObjResultadoValorizacion>	InstrumentosCOMP = new ArrayList<ObjResultadoValorizacion>();
		Integer ContadorCOMP = 0;
		 for (int icc = 0; icc<InstrumentosValorizados.size();icc++) {			// se filtra el listado de valorizaciones obtenidas para obtener solo las COMP
			if (InstrumentosValorizados.get(icc).DimensionTasa == "COMP") {
				InstrumentosCOMP.add(InstrumentosValorizados.get(icc));
				if ( InstrumentosValorizados.get(icc).Tasa != null){	
					ValorizacionesFinal.get(ContadorCOMP).Tasa = InstrumentosValorizados.get(icc).Tasa;
					ValorizacionesFinal.get(ContadorCOMP).PosValorizacion= icc;
					if (Debug) {	System.out.println("Tasa Valorizada: " +  InstrumentosValorizados.get(icc).Tasa  +" | Monto: "+InstrumentosValorizados.get(icc).MontoCompra);}
					ValorizacionesFinal.get(ContadorCOMP).ValorizacionOK = true;
				}else {
					System.out.println(InstrumentosValorizados.get(icc));
					ValorizacionesFinal.get(ContadorCOMP).ValorizacionOK = false;

					// 		Si falla la valorizacion y este no regresa una tasa, se guarda el id de la valorizacion fallida para descartarla mas adelante
				}
				ContadorCOMP++;
			}
		} 
		/*
		 * 	Segunda valorizacion para optener el Monto deacuerdo a la tasa dada
		 */
		IdValorizacion = 0L; int icv = 0;
		for (ObjInstrumento CarteraINGTIT : CarterasIngreso) {	
			if (ValorizacionesFinal.get(icv).ValorizacionOK && ValorizacionesFinal.get(icv).ValorizacionOK) {
				ObjEntradaInstrumentos TestV = LLenarObjEntradaInstrumento(CarteraINGTIT, IdValorizacion , InstrumentosCOMP , icv , "SECOND");
				IdValorizacion++;
				ListaAValorizar2.add(TestV);	
			}			  
			  icv++;				  
		}
		InstrumentosCOMP.clear();
		ValIn.p_Instrumentos = null;
		ValIn.p_Instrumentos = ListaAValorizar2;
		System.out.println("llamada 2 a valorizador ------ ");
		ResultadoValorizador = valorizador.logica(con,ValIn);			// -- Segundo llamado al valorizador donde se obtiene dada una tasa el precio y monto -- //

		InstrumentosValorizados = ResultadoValorizador.P_OUT_INSTRUMENTOSVALORIZADOS;		
		ContadorCOMP = 0;
		for (int icc = 0; icc<InstrumentosValorizados.size();icc++) {			// se filtra el listado de valorizaciones obtenidas para obtener solo las COMP
			if (InstrumentosValorizados.get(icc).DimensionTasa == "COMP") {
				InstrumentosCOMP.add(InstrumentosValorizados.get(icc));
				if (InstrumentosValorizados.get(icc).Precio == null && InstrumentosValorizados.get(icc).MontoMonedaInstrumento == null ) {
					ValorizacionesFinal.get(ContadorCOMP).ValorizacionOK = false;
				}else {
					ValorizacionesFinal.get(ContadorCOMP).Tasa = InstrumentosValorizados.get(icc).Tasa;
				}
				ContadorCOMP++;
			}
		} 
		ContadorCOMP = 0; int ContadorVal = 0;
		for ( int cc = 0 ; cc< CarterasIngreso.size() ; cc ++) {
			if ( ValorizacionesFinal.get(ContadorVal).ValorizacionOK  ) {
				CarterasIngreso.get(cc).Tasa = InstrumentosCOMP.get(ContadorCOMP).Tasa;
				CarterasIngreso.get(cc).Precio = InstrumentosCOMP.get(ContadorCOMP).Precio;
				CarterasIngreso.get(cc).Monto = InstrumentosCOMP.get(ContadorCOMP).MontoMonedaInstrumento;
				CarterasIngreso.get(cc).MontoEnMonedaInstrumento = InstrumentosCOMP.get(ContadorCOMP).MontoMonedaInstrumento;
				ContadorCOMP++;
			}else {
				for (ObjInstrumento CarteranoFusionada:CarterasRetiro.get(cc)) {
					String msg = "Cartera Folio: " + CarteranoFusionada.FolioCartera + " No pudo ser fusionada. Precio: " + CarteranoFusionada.Precio + ", MontoMonedaInst: " + CarteranoFusionada.MontoEnMonedaInstrumento + ", Tasa: " + CarteranoFusionada.Tasa;
					MensajeSalida("INFO",msg);
				}
				CarterasIngreso.remove(cc);
				CarterasRetiro.remove(cc);
				cc--;
			}
			ContadorVal++;
		}
		
		return ResultadoValorizador;			
	}
	
	private void LLenarHistorico(IngresoRetiroTitulosSalida resultado , List<List<Cartera>> ListadoCarterasIN, Connection con, FusionCarterasEntrada in) throws Exception {

		String Usuario = in.p_IdUsuario.toString();
		String Empresa = in.p_IdEmpresa.toString();
		List<AddHISFusion> QueryFusion2 = new ArrayList<AddHISFusion>();
		List<AddFusionAux> QueryFusion3 = new ArrayList<AddFusionAux>();

		int contadorValOK = 0;
		for ( int cont1 =0 ; cont1 < CarterasIngreso.size() ; cont1++) {
								
				AddHISFusion parametros1 	= new AddHISFusion();
				parametros1.FechaFusion 		= Utils.getFechaHoraSistema(con, "BSCL", in.p_IdEmpresa);
				parametros1.CantidadFusion 		= CarterasIngreso.get(cont1).Cantidad;
				parametros1.TasaFusion 			= CarterasIngreso.get(cont1).Tasa;
				parametros1.ValorRescateFusion	= CarterasIngreso.get(cont1).ValorRescate;
				parametros1.IdClasificacionTipoInstrumento=CarterasIngreso.get(cont1).IdClasificacionTipoInstrumento;
				//parametros1.MontoFusion		= Montos.get(cont1) ;		// El monto de la fusion se agrega en el siguiente ciclo for
				parametros1.MontoValorizado 	= CarterasIngreso.get(cont1).MontoEnMonedaInstrumento;
				parametros1.IdCarteraResultante	= null;
				parametros1.IdModulo			= in.p_IdModulo;
				parametros1.IdUsuario			= Usuario;
				parametros1.IdEmpresa			= Empresa;
				QueryFusion2.add(parametros1);	
				
				contadorValOK++;
		};
				
		SQL sql1=new SQL();
		if (resultado.P_OUT_ERRORESCONTADOR == 0) {	
			List<ObjResultado> MensajesResultado = resultado.P_OUT_RESULTADO;			
			contadorValOK = 0;	
			for (int i = 0 ; i< ValorizacionesFinal.size() ; i++  ) {	
				if ( ValorizacionesFinal.get(i).ValorizacionOK  ) {		
					QueryFusion2.get(contadorValOK).IdCarteraResultante  =  MensajesResultado.get(contadorValOK).IdCustodia;	
					QueryFusion2.get(contadorValOK).MontoFusion				=  ValorizacionesFinal.get(i).MontoSuma;
					for (int cci = 0 ; cci < ListadoCarterasIN.get(i).size(); cci++  ) {
						AddFusionAux parametros2 = new AddFusionAux();
						parametros2.IdCartera 			= 	ListadoCarterasIN.get(i).get(cci).IdCartera;
						parametros2.FolioCustodia 		= 	MensajesResultado.get(contadorValOK).IdCustodia;
						parametros2.Cantidad 			= 	ListadoCarterasIN.get(i).get(cci).Cantidad;
						parametros2.Tasa				=	ListadoCarterasIN.get(i).get(cci).TasaCompra;
						parametros2.ValorRescate		=	ListadoCarterasIN.get(i).get(cci).ValorRescate;
						parametros2.CorteMinimo 		=	ListadoCarterasIN.get(i).get(cci).CorteMinimo;
						parametros2.EsFungible 			=	ListadoCarterasIN.get(i).get(cci).EsFungible;
						QueryFusion3.add(parametros2);
						
						if (Debug) {System.out.println(" ||--||  Query para Detalle: " + parametros2 );}
					}
					if (Debug) {System.out.println(" ||--||--||  Query para Historico: " + QueryFusion2.get(contadorValOK) );}
					contadorValOK++;
				}
				
			}				

			if (QueryFusion2.size() > 0)
			{
				sql1.batch(con,SQL_AddHistorico,QueryFusion2,AddHISFusion.class);
			}
			
			if (QueryFusion3.size() > 0)
			{
				sql1.batch(con,SQL_AddFusionAux,QueryFusion3,AddFusionAux.class);
			}
		}
		

	}

	private ObjEntradaInstrumentos LLenarObjEntradaInstrumento(ObjInstrumento CarteraINGTIT , Long IdVal , List<ObjResultadoValorizacion> ResVal , int nval, String Nvalorizacion){
		  ObjEntradaInstrumentos ItemAValorizar = new ObjEntradaInstrumentos();
		  
		  ItemAValorizar.IdValorizacion 				= 	IdVal;
		  ItemAValorizar.OrigenProcesamiento 			= 	"MESA_UI";
		  ItemAValorizar.FechaValorizacion 				= 	fechaSistema;
		  ItemAValorizar.FechaOperacion 				= 	fechaSistema;                                 	//Solo para operaciones a plazo
		  ItemAValorizar.FechaVencimientoOperacion 		= 	CarteraINGTIT.FechaVencimiento;                	//Solo para operaciones a plazo     
		  ItemAValorizar.IdClasificacionTipoInstrumento	= 	CarteraINGTIT.IdClasificacionTipoInstrumento; 
		  ItemAValorizar.IdTipoInstrumento 				= 	CarteraINGTIT.IdTipoInstrumento; 
		  ItemAValorizar.IdInstrumento 					= 	CarteraINGTIT.IdInstrumento;
		  ItemAValorizar.Nemotecnico 					= 	CarteraINGTIT.Nemotecnico;
		  ItemAValorizar.FechaVencimiento				= 	CarteraINGTIT.FechaVencimiento;
		  ItemAValorizar.FechaEmision					= 	fechaSistema;
		  ItemAValorizar.IdMonedaInstrumento 			= 	CarteraINGTIT.IdMonedaInstrumento;
		  ItemAValorizar.IdMonedaEmisionInstrumento		= 	CarteraINGTIT.IdMonedaEmision; 					//CarteraINGTIT.IdTipoReajuste;
		  ItemAValorizar.IdTipoReajusteInstrumento 		= 	CarteraINGTIT.IdTipoReajuste;
		  ItemAValorizar.Emisor 						= 	CarteraINGTIT.IdEmisor;                         //valorizacionCL
		  ItemAValorizar.IdCondicionDeLiquidacion 		= 	CarteraINGTIT.IdCondicionLiquidacion;           //valorizacionCL
		  ItemAValorizar.FechaDeLiquidacion				= 	CarteraINGTIT.FechaLiquidacion;	
		  ItemAValorizar.Cantidad 						= 	CarteraINGTIT.Cantidad; 
		  ItemAValorizar.ValorRescate					= 	CarteraINGTIT.ValorRescate; 
		  ItemAValorizar.TablaOrigen					= 	"CARTERA";
		  
		  // Dependiendo de la valorizacion Nvalorizacion sera FIRST o SECOND al llamarse.
		  if ( Nvalorizacion.equals("FIRST")) {
			  ItemAValorizar.PrecioCompra	= null;				
			  ItemAValorizar.TasaCompra 	= null;	
			  ItemAValorizar.MontoCompra 	= CarteraINGTIT.Monto;  
		  }else  {
			  ItemAValorizar.PrecioCompra	= null; 				
			  ItemAValorizar.TasaCompra 	= ResVal.get(nval).Tasa;
			  ItemAValorizar.MontoCompra 	= null;
		  }
		if (Debug)  {System.out.println("----------------DatosValorizador-.---------" + Nvalorizacion);
		System.out.println("Precio: "+  ItemAValorizar.PrecioCompra + " Tasa: " + ItemAValorizar.TasaCompra + " Monto:" + ItemAValorizar.MontoCompra + " Cantidad: " + ItemAValorizar.Cantidad);}
		return ItemAValorizar;
	}
	
	public static void main(String[] args) { 
		Debug = true;
		FusionCarteras FC = new FusionCarteras();
		String json = 	"{p_IdEmpresa: 1, p_IdUsuario: 1646, p_IdPais: 44, p_IdModulo: \"CUS\"}";
		Gson gson = Utils.getGsonBuilder();
		FusionCarterasEntrada entrada = gson.fromJson(json, FusionCarterasEntrada.class);		

		// getBOLSA02Connection()  // para pruebas en master02    getDESAConnection()   // pruebas en desa
		
		try (Connection con = PlataformaUtils.getDESAConnection() ) 
		{
			FusionCarterasSalida salida = FC.logica(con,entrada);
			con.rollback();
		}catch (Exception e){	System.out.println(e);	}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		FusionCarterasEntrada in =gson.fromJson(jin.toString() , FusionCarterasEntrada.class);
		FusionCarteras FC = new FusionCarteras();
		FusionCarterasSalida out = FC.logica(conn,in);
		JsonParser jsonParser = new JsonParser();
		
		return jsonParser.parse(gson.toJson(out)).getAsJsonObject();
	}
}