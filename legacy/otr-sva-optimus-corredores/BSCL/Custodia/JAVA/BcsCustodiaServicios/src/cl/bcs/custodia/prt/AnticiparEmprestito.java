package cl.bcs.custodia.prt;
//-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
//-- ACC: El 14/12/2018 se modifico esta clase para que llame inmediatamente a la clase VencerEmprestitos, ya q el proceso es identico y solo cambia Concepto, fecha, cantidad y montos
//-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------


import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.prt.AnticiparPrestamo.AnticiparPrestamo_Entrada_DatosPrestamo;
import cl.bcs.custodia.prt.AnticiparPrestamo.SelectEstadoAnticipo;
import cl.bcs.custodia.prt.VencerPrestamos.ObjRegistroPrestamo;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.bck.AnticiparPrestamistaBlockChain;
import cl.bcs.socionegocio.bck.AnticiparPrestamistaBlockChain.AnticiparPrestamistaBlockChain_Entrada;
import cl.bcs.socionegocio.bck.AnticiparPrestamistaBlockChain.AnticiparPrestamistaBlockChain_Salida;
import cl.bcs.socionegocio.bck.ContratoEsBlockChainFirmado.TipoDocumento;
import cl.bcs.socionegocio.pojo.ConstantesBCK;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class AnticiparEmprestito implements JavaCall {
	

	private final String SQL_SELECT_EMPRESTITO 				= loadResourceToString("cl/bcs/custodia/sql/AnticiparEmprestito_SelectEmprestito.sql");	
	private final String SQL_SELECT_PRESTAMOS_ASOCIADOS 	= loadResourceToString("cl/bcs/custodia/sql/AnticiparEmprestito_SelectPrestamosAsociados.sql");	
	private final String SQL_SELECT_INFORMACIONPRESTATARIO	= loadResourceToString("cl/bcs/socionegocio/bck/sql/ConsultarDatosPrestatario.sql");
	private final String SQL_UPDATE_ESTADO_ANTICIPO			= loadResourceToString("cl/bcs/custodia/sql/AnticiparPrestamo_UpdateEstadoAnticipo.sql");
	private final String SQL_SELECT_ESCONTRATOBLOCKCHAIN	= loadResourceToString("cl/bcs/socionegocio/bck/sql/ContratoEsBlockChainFirmado_SelectTipoDocumento.sql");

	public static final String TIPO_PRESTAMO_VENTA_CORTA 	 = "PRTVTC";
	
	//-------------------------------------------------
	// CLASES DE ENTRADA 
	//-------------------------------------------------
	public static class AnticiparEmprestito_Entrada extends BcsPojo {
		public Long       						p_IdEmpresa;
		public Long       						p_IdUsuario;
		public String     						p_IdModulo;	
		public String     						p_UsaReglasNegocio = "S";
		public Long       						p_IdPais;
		public String 							p_RebajaPrestamo = "N"; //Si viene en S, significa q es un anticipo manual del UN prestamista, por ende tengo q rebajar la cantidad que estoy anticipando tambien del prestamo.
		public String 							p_FechaAnticipo;
		public List<FolioPrestamoBlockChain>	p_FoliosPrestamoBlockChain	;
		public String 							p_AnticiparPrestamoAsociado = "N";   //S:  Anticipar tambien el prestamo asociado al emprestito
		public String							p_VieneDesdeBlockChain = "N"; 
		public List<AnticiparEmprestito_Entrada_DatosEmprestito>	p_DatosEmprestitos;
		public List<AnticiparPrestamo_Entrada_DatosPrestamo>		p_DatosPrestamos;
		
		public AnticiparEmprestito_Entrada() {
			this.p_DatosEmprestitos	=  new ArrayList<AnticiparEmprestito_Entrada_DatosEmprestito>();
			this.p_RebajaPrestamo 	= "N";
		}
	} 
	
 
	public static class AnticiparEmprestito_Entrada_DatosEmprestito extends BcsPojo {
		public Long       		FolioPrestamo;
		public Long       		FolioEmprestito;
		public BigDecimal      	PrecioInstrumento;
		public BigDecimal      	Cantidad;
		public BigDecimal      	MontoInstrumento;
		public BigDecimal      	TasaPremio;
		public BigDecimal      	MontoPremio;
		public BigDecimal      	MontoAPlazo;
		public String      		FechaAnticipo;
		public Integer      	PlazoAlVencimiento;
		public Long       		IdPais;
		public String      		IdMonedaLiquidacion;
		public String      		IdCondicionLiquidacion;
		public String      		FechaLiquidacion;
		public String      		DevolverCustodia;
		public Long       		IdEmpleadoDigitador;
		public String 			FechaEmprestito;
	}
	
	public static class SelectCorredora extends BcsPojo	{
		public String CodigoCorredora;
		public String DocumentoIdentidad;
	}
	
	//-------------------------------------------------
	// CLASES DE SALIDA 
	//-------------------------------------------------

	public static class AnticiparEmprestito_Salida extends BcsPojo {	
		public List<MensajeSalida>		P_OUT_MENSAJES;
		public Long						P_OUT_ERRORESCONTADOR;
		
		public AnticiparEmprestito_Salida() {
			this.P_OUT_ERRORESCONTADOR 	= 0L;
			this.P_OUT_MENSAJES	 		=  new ArrayList<MensajeSalida>();
		}
	}


	//-------------------------------------------------
	// CLASES COMPLEMENTARIAS
	//-------------------------------------------------
	public static class ObjRegistroEmprestito extends BcsPojo {
		public Long 			IdCartera;
		public Long 			IdSubyacente;
		public Long 			FolioSubyacente;
		public Long 			FolioEmprestito;
		public String 			FolioSistemaExterno;
		public Long 			IdCarteraRetirada;
		public java.sql.Date 	FechaVencimientoEmprestito;
		public java.sql.Date 	FechaEmprestito;
		public Long 			IdCliente;
		public Long 			IdPortafolio;
		public String 			IdTipoProducto;
		public Long 			FolioPrestamo;
		public Long 			IdClientePrestamo;
		public Long 			IdPortafolioPrestamo;
		public java.sql.Date 	FechaPrestamo;
		public Long 			IdInstrumentoEmprestito;
		public String 			IdTipoInstrumentoEmprestito;
		public String 			IdEmisorEmprestito;
		public String 			NemoSubyacente;
		public Long 			IdInstrumentoSubyacente;
		public String 			IdTipoInstrumentoSubyacente;
		public String 			IdClasificacionTipoInstrumentoSubyacente;
		public String 			IdTipoReajusteSubyacente;
		public String 			IdEmisorSubyacente;
		public String 			IdMonedaSubyacente;
		public BigDecimal 		INGCantidad;
		public BigDecimal 		INGTasaEmprestito;
		public BigDecimal 		INGValorRescate;
		public BigDecimal 		INGMontoInstrumento;
		public BigDecimal 		INGMontoTotalEmprestito;
		public BigDecimal 		INGMontoPremio;
		public String 			ConCustodiaPrestamista;
		public String 			ConCustodiaPrestatario;
		public String 			IdBolsa;
		public String 			OperaContraCuentaInversionPrestamista;
		public String 			OperaContraCuentaInversionPrestatario;
		public BigDecimal 		PrecioInstrumento;
		public Long 			PlazoAlVencimiento;
		public Long 			EmpleadoDigitador;
		public java.sql.Date 	FechaVencimientoSubyacente;
		public String 			EsFungibleSubyacente;
		public String 			IdMonedaEmisionSubyacente;
	}
	public static class FolioPrestamoBlockChain extends BcsPojo {
		public String	FolioPrestamoBlockChain = "";
	}
	
	public static class SelectDocumentoIdentidadPrestatario extends BcsPojo {
		public String DocumentoIdentidad;
	}
	
	public static class Cartera extends BcsPojo {
		public Long 	FolioCartera;
		public String 	FolioSistemaExterno;
		public Long		IdEmpresa;
	}
	
	//-------------------------------------------------
	// LOGICA DEL SERVICIO 
	//-------------------------------------------------	
	public AnticiparEmprestito_Salida logica(Connection con, AnticiparEmprestito_Entrada entrada) throws Exception {
		
		AnticiparEmprestito_Salida salida 	= new AnticiparEmprestito_Salida();
		SQL sql 							= new SQL();
		HashMap<String, Object> params 		= new HashMap<String, Object>();
		HashMap<String,String>  valores 	= new HashMap<String,String>();
		SimpleDateFormat dateFormat 		= new SimpleDateFormat("yyyy-MM-dd");
		String fechaSistema 				= ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		// Obtener datos del emprestitos
		List<Long> lstFolioEmprestito 				= entrada.p_DatosEmprestitos.parallelStream().map(linea->linea.FolioEmprestito).distinct().collect(Collectors.toList());
		List<Long> emprestitos 						= new ArrayList<>();
		List<ObjRegistroEmprestito> lstEmprestitos 	= new ArrayList<ObjRegistroEmprestito>();
		
		if ( lstFolioEmprestito.size() > 0 ) {
			params.clear();
			params.put("IdEmpresa"			, entrada.p_IdEmpresa);
			params.put("lstFolioEmprestito"	, lstFolioEmprestito);						
			lstEmprestitos = sql.query(con, SQL_SELECT_EMPRESTITO, params, ObjRegistroEmprestito.class);
			
			// Valida que el anticipo no corresponde al mismo dia del vencimiento
			for (ObjRegistroEmprestito emprestito : lstEmprestitos) {	
				Date fechaInicial   = dateFormat.parse(fechaSistema); 
				Date fechaFinal 	= emprestito.FechaVencimientoEmprestito;
				int diferenciaDias 	= (int) ((fechaFinal.getTime()-fechaInicial.getTime())/86400000);
				
				if ( diferenciaDias <= 0 ) {				
					salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"PRESTAMISTA_NO_SE_PUEDE_ANTICIPAR_POR_FECHA_VCTO",valores,con,entrada.p_IdEmpresa));
					salida.P_OUT_ERRORESCONTADOR++;
					return salida;
				}	
			}
			
			VencerEmprestitos ObjVencimiento 		= new VencerEmprestitos();
			VencerEmprestitos.Entrada paramsVenc 	= new VencerEmprestitos.Entrada();
			paramsVenc.p_Fecha 				= entrada.p_FechaAnticipo;
			paramsVenc.p_IdEmpresa 			= entrada.p_IdEmpresa;
			paramsVenc.p_IdModulo 			= entrada.p_IdModulo;
			paramsVenc.p_IdPais 			= entrada.p_IdPais;
			paramsVenc.p_IdUsuario 			= entrada.p_IdUsuario;
			paramsVenc.p_RebajaPrestamo 	= entrada.p_RebajaPrestamo;
			paramsVenc.p_OrigenLiquidacion 	= "ANTICIPO";
			
			for (AnticiparEmprestito_Entrada_DatosEmprestito entradaDatosEmprestito : entrada.p_DatosEmprestitos) {
				emprestitos.add(entradaDatosEmprestito.FolioEmprestito);				
				
				VencerEmprestitos.EmprestitoAnticipando regAnt = new VencerEmprestitos.EmprestitoAnticipando();
				regAnt.Cantidad = entradaDatosEmprestito.Cantidad;
				regAnt.FolioEmprestito = entradaDatosEmprestito.FolioEmprestito;
				regAnt.IdEmpleadoDigitador = entradaDatosEmprestito.IdEmpleadoDigitador;
				regAnt.MontoAPlazo = entradaDatosEmprestito.MontoAPlazo;
				regAnt.MontoInstrumento = entradaDatosEmprestito.MontoInstrumento;
				regAnt.MontoPremio = entradaDatosEmprestito.MontoPremio;
				regAnt.PlazoAlVencimiento = entradaDatosEmprestito.PlazoAlVencimiento;
				regAnt.PrecioInstrumento = entradaDatosEmprestito.PrecioInstrumento;
				regAnt.TasaPremio = entradaDatosEmprestito.TasaPremio;
				regAnt.FechaEmprestito = Fecha.toDate(entradaDatosEmprestito.FechaEmprestito);
				paramsVenc.p_lstEmprestitos.add(entradaDatosEmprestito.FolioEmprestito);
				paramsVenc.p_lstAnticipos.add(regAnt);
			}

			VencerEmprestitos.Salida resulAnticipos = ObjVencimiento.logica(con, paramsVenc);
			salida.P_OUT_ERRORESCONTADOR = resulAnticipos.P_OUT_ERRORESCONTADOR;
			salida.P_OUT_MENSAJES.addAll(resulAnticipos.P_OUT_MENSAJES);

			//Se deben anticipar tambien los prestamos asociados a los emprestitos
			//Este caso se da cuando se solicita anticipar directamente el emprestito(Ej UI de emprestitos)
			if ( entrada.p_AnticiparPrestamoAsociado.equalsIgnoreCase("S") ) {
				List<ObjRegistroPrestamo> lstPrestamo 		= new ArrayList<ObjRegistroPrestamo>();			
				HashMap<String,Object> prmsBuscaPrestamos 	= new HashMap<String,Object>();
				prmsBuscaPrestamos.put("p_IdEmpresa", entrada.p_IdEmpresa);
				for ( AnticiparEmprestito_Entrada_DatosEmprestito entradaDatosEmprestito : entrada.p_DatosEmprestitos ){
					prmsBuscaPrestamos.put("p_FolioPrestamo", entradaDatosEmprestito.FolioPrestamo);
				}
			
				lstPrestamo = sql.query(con, SQL_SELECT_PRESTAMOS_ASOCIADOS, prmsBuscaPrestamos, ObjRegistroPrestamo.class);
				
				if ( lstPrestamo.size() > 0 ) {
					VencerPrestamos ObjAnticipo 			= new VencerPrestamos();
					VencerPrestamos.Entrada paramsAnticipo 	= new VencerPrestamos.Entrada();
					VencerPrestamos.Salida resulAnticipo 	= new VencerPrestamos.Salida();
					
					paramsAnticipo.p_IdEmpresa 					= entrada.p_IdEmpresa;
					paramsAnticipo.p_Fecha 						= entrada.p_FechaAnticipo;
					paramsAnticipo.p_IdModulo 					= entrada.p_IdModulo;
					paramsAnticipo.p_IdPais 					= entrada.p_IdPais;
					paramsAnticipo.p_IdUsuario 					= entrada.p_IdUsuario;
					paramsAnticipo.p_OrigenLiquidacion 			= "ANTICIPO";
					paramsAnticipo.p_VieneDesdeVencerEmprestito = "S";
					paramsAnticipo.p_lstPrestamos = new ArrayList<ObjRegistroPrestamo>();
					
					for ( ObjRegistroPrestamo prestamoAsociado : lstPrestamo ) {
						for ( AnticiparEmprestito_Entrada_DatosEmprestito entradaDatosEmprestito : entrada.p_DatosEmprestitos ) {
							if ( prestamoAsociado.FolioCartera.equals(entradaDatosEmprestito.FolioPrestamo) ) {
								
								prestamoAsociado.FechaOperacion 						= Fecha.toDate(entradaDatosEmprestito.FechaAnticipo);
								prestamoAsociado.FechaLiquidacion 						= entradaDatosEmprestito.FechaAnticipo;
								prestamoAsociado.IdCondicionLiquidacion 				= entradaDatosEmprestito.IdCondicionLiquidacion;
								prestamoAsociado.IdUsuario 								= entradaDatosEmprestito.IdEmpleadoDigitador;
								prestamoAsociado.Cantidad 								= entradaDatosEmprestito.Cantidad;
								prestamoAsociado.INGPrecioCompra 						= entradaDatosEmprestito.PrecioInstrumento;
								prestamoAsociado.INGTasaCompra 							= prestamoAsociado.INGTasaCompra;
								prestamoAsociado.INGMontoPrima 							= prestamoAsociado.INGMontoPrima;
								prestamoAsociado.INGMontoPremio 						= prestamoAsociado.INGMontoPremio;
								prestamoAsociado.INGMontoCompraActualMonedaInstrumento 	= prestamoAsociado.INGMontoCompraActualMonedaInstrumento;
								prestamoAsociado.INGMontoATasaCompraActual 				= prestamoAsociado.INGMontoATasaCompraActual;
								prestamoAsociado.INGMontoAPlazo 						= entradaDatosEmprestito.MontoAPlazo;
								prestamoAsociado.FolioSistemaExterno 					= "0";
								
								paramsAnticipo.p_lstPrestamos.add(prestamoAsociado);
							}	
						}
						
					}
					
					resulAnticipo = ObjAnticipo.logica(con, paramsAnticipo);
					salida.P_OUT_ERRORESCONTADOR = resulAnticipo.P_OUT_ERRORESCONTADOR;
					salida.P_OUT_MENSAJES.addAll(resulAnticipo.P_OUT_MENSAJES);
				}
			}
		}
		
		if( salida.P_OUT_ERRORESCONTADOR == 0 ) {
			AnticiparEmprestito_Salida salidaInformarAnticipoEmprestitoBlockChain = informarAnticipoEmprestitoBlockChain(con, entrada, lstEmprestitos);
			salida.P_OUT_ERRORESCONTADOR += salidaInformarAnticipoEmprestitoBlockChain.P_OUT_ERRORESCONTADOR;
			salida.P_OUT_MENSAJES.addAll(salidaInformarAnticipoEmprestitoBlockChain.P_OUT_MENSAJES);
		}
		
		return salida;
	}
	
	public AnticiparEmprestito_Salida informarAnticipoEmprestitoBlockChain(Connection con, AnticiparEmprestito_Entrada entrada, List<ObjRegistroEmprestito> lstEmprestitos) throws Exception {
		
		AnticiparEmprestito_Salida salida 												= new AnticiparEmprestito_Salida();
		
		if( entrada.p_VieneDesdeBlockChain.equalsIgnoreCase("S") ) {
			return salida;
		}
		
		AnticiparPrestamistaBlockChain AnticiparPrestamistaBlockchain 					= new AnticiparPrestamistaBlockChain();
		AnticiparPrestamistaBlockChain_Entrada AnticiparPrestamistaBlockChainEntrada 	= new AnticiparPrestamistaBlockChain_Entrada();
		List<ObjRegistroPrestamo> lstPrestamo 											= new ArrayList<ObjRegistroPrestamo>();			
		HashMap<String,Object> parametros 												= new HashMap<String,Object>();
		SQL sql 																		= new SQL();
		List<ObjRegistroEmprestito> emprestitos 										= new ArrayList<ObjRegistroEmprestito>();
		
		parametros.put("p_IdEmpresa", entrada.p_IdEmpresa);
		for ( AnticiparEmprestito_Entrada_DatosEmprestito entradaDatosEmprestito : entrada.p_DatosEmprestitos ) {
			parametros.put("p_FolioPrestamo", entradaDatosEmprestito.FolioPrestamo);
		}	
		lstPrestamo = sql.query(con, SQL_SELECT_PRESTAMOS_ASOCIADOS, parametros, ObjRegistroPrestamo.class);
		parametros.clear();
		
		/*** TIPO DOCUMENTO BLOCKCHAIN HABILITADO ***/
		parametros.put("p_IdTipoDocumento", lstPrestamo.get(0).IdTipoPrestamo.equalsIgnoreCase(TIPO_PRESTAMO_VENTA_CORTA) ? ConstantesBCK.CONTRATOPRESTAMOANEXO42 : ConstantesBCK.CONTRATOPRESTAMOANEXO48);
		List<TipoDocumento> arrTipoDocumento = sql.query(con, SQL_SELECT_ESCONTRATOBLOCKCHAIN, parametros, TipoDocumento.class);
		parametros.clear();
		
		if ( arrTipoDocumento.size() == 0 ) {
			return salida;
		}
		
		for ( ObjRegistroEmprestito emprest : lstEmprestitos ) {
			if( !emprest.FolioSistemaExterno.isEmpty() || !emprest.FolioSistemaExterno.equals("0") ) {
				List<Long> lstFolioEmprestito = entrada.p_DatosEmprestitos.parallelStream()
						.map( linea -> linea.FolioEmprestito )
						.distinct()
						.collect( Collectors.toList() );
				parametros.put("IdEmpresa"			, entrada.p_IdEmpresa);
				parametros.put("lstFolioEmprestito"	, lstFolioEmprestito);						
				emprestitos = sql.query(con, SQL_SELECT_EMPRESTITO, parametros, ObjRegistroEmprestito.class);
				parametros.clear();
				
				parametros.put("p_IdClienteSolicita", emprest.IdCliente);
				List<SelectDocumentoIdentidadPrestatario> SelectInformacionPrestatario= sql.query(con, SQL_SELECT_INFORMACIONPRESTATARIO, parametros, SelectDocumentoIdentidadPrestatario.class);
				parametros.clear();
				
				AnticiparPrestamistaBlockChainEntrada.CantidadPrestamo				= lstPrestamo.get(0).INGCantidad;
				AnticiparPrestamistaBlockChainEntrada.p_IdEmpresa					= entrada.p_IdEmpresa;
				AnticiparPrestamistaBlockChainEntrada.p_Fecha						= entrada.p_DatosEmprestitos.get(0).FechaAnticipo;
				AnticiparPrestamistaBlockChainEntrada.p_rutPrestamista				= SelectInformacionPrestatario.get(0).DocumentoIdentidad;
				AnticiparPrestamistaBlockChainEntrada.p_cantidadPrestamista			= emprestitos.size() == 0 ? new BigDecimal("0") : emprestitos.get(0).INGCantidad;
				AnticiparPrestamistaBlockChainEntrada.p_FoliosPrestamoBlockChain.add(emprest.FolioSistemaExterno);	
				AnticiparPrestamistaBlockChain_Salida AnticiparPrestamistaBlockChainSalida = AnticiparPrestamistaBlockchain.logica(con, AnticiparPrestamistaBlockChainEntrada);	
				if( AnticiparPrestamistaBlockChainSalida.P_OUT_ERRORESCONTADOR > 0 ) {
					salida.P_OUT_MENSAJES = AnticiparPrestamistaBlockChainSalida.P_OUT_MENSAJES;
					salida.P_OUT_ERRORESCONTADOR++;
				} else {
					actualizaEstadoBlockchain(con, AnticiparPrestamistaBlockChainSalida.P_OUT_STATUS, lstPrestamo.get(0).IdCartera);
					salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "UI_BC_OK_ANTICIPO_PRESTAMISTA", new HashMap<String, String>(), con, entrada.p_IdEmpresa));
				}
			} else {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, "UI_BLOCKCHAIN_DEBE_ENVIAR_FOLIO_PRESTAMOBC", new HashMap<String, String>(), con, entrada.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR++;
				break;
			}
		}
		
		return salida;
	}
	
	public void actualizaEstadoBlockchain(Connection con, List<cl.bcs.socionegocio.bck.AnticiparPrestamistaBlockChain.Status> status, Long IdCartera) throws SQLException {
		SQL sql = new SQL();
		List<SelectEstadoAnticipo> selectEstadoAnticipo = new ArrayList<SelectEstadoAnticipo>();
		SelectEstadoAnticipo actualizarEstado = new SelectEstadoAnticipo();
		actualizarEstado.IdCartera = IdCartera;
		if(status.get(0).Status.isEmpty()) {
			actualizarEstado.EstadoAnticipo = "RZD";
		}
		else {
			if(status.get(0).Status.contentEquals("SUCCESS")) {
				actualizarEstado.EstadoAnticipo = "APD";
			}else if (status.get(0).Status.contentEquals("ERROR")){
				actualizarEstado.EstadoAnticipo = "RZD";
			}
		}
		selectEstadoAnticipo.add(actualizarEstado);
		sql.batch(con, SQL_UPDATE_ESTADO_ANTICIPO, selectEstadoAnticipo, SelectEstadoAnticipo.class);
	}
		
	//-------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE - CALL
	//-------------------------------------------------------------------------------------------
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();
		AnticiparEmprestito_Entrada in = gson.fromJson(jin.toString(), AnticiparEmprestito_Entrada.class);
		AnticiparEmprestito_Salida out = logica(con,in);
		
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
			

			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"CUS\",\"p_UsaReglasNegocio\":\"S\",\"p_IdPais\":44,\"p_DatosEmprestitos\":[{\"FolioEmprestito\":20706808,\"FechaEmprestito\":\"2023-03-20\",\"PrecioInstrumento\":1200,\"Cantidad\":50,\"MontoInstrumento\":60000,\"TasaPremio\":1.32,\"MontoPremio\":26.400000000001455,\"MontoAPlazo\":60026.4,\"FechaAnticipo\":\"2023-03-21\",\"PlazoAlVencimiento\":3,\"IdPais\":44,\"IdMonedaLiquidacion\":\"CLP\",\"IdCondicionLiquidacion\":\"T0\",\"FechaLiquidacion\":\"2023-03-21T03:00:00.000Z\",\"DevolverCustodia\":\"S\",\"IdEmpleadoDigitador\":7305,\"FolioPrestamo\":20706802}],\"p_RebajaPrestamo\":\"S\",\"p_AnticiparPrestamoAsociado\":\"N\",\"p_FechaAnticipo\":\"2023-03-21\"}";
			
			AnticiparEmprestito_Entrada Objin = gson.fromJson(json, AnticiparEmprestito_Entrada.class);
			AnticiparEmprestito demo = new AnticiparEmprestito();
 			
			System.out.println("ENTRADA:" + Objin.toString());
			
 			try {
 				AnticiparEmprestito_Salida ObjOut = demo.logica(con, Objin);
 				System.out.println("SALIDA:" + ObjOut);
 				con.commit();
 				
 			} catch(Exception er) {
				er.printStackTrace();
				con.rollback();
			}

	    }
		catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("== FIN PRUEBA LOCAL ==");
	}

}
