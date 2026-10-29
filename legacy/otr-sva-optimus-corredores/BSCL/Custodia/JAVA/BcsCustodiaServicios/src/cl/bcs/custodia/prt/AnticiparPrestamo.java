package cl.bcs.custodia.prt;
//-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
//-- ACC: El 15/12/2018 se modifico esta clase para que llame inmediatamente a la clase VencerPrestamos, ya q el proceso es identico y solo cambia Concepto, fecha, cantidad y montos
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

import cl.bcs.custodia.prt.VencerPrestamos.ObjRegistroPrestamo;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.bck.AnticiparPrestamoBlockChain;
import cl.bcs.socionegocio.bck.AnticiparPrestamoBlockChain.AnticiparPrestamoBlockChain_Entrada;
import cl.bcs.socionegocio.bck.AnticiparPrestamoBlockChain.AnticiparPrestamoBlockChain_Salida;
import cl.bcs.socionegocio.bck.AnticiparPrestamoBlockChain.FolioPrestamoBlockChain;
import cl.bcs.socionegocio.bck.AnticiparPrestamoBlockChain.Status;
import cl.bcs.socionegocio.bck.ContratoEsBlockChainFirmado.TipoDocumento;
import cl.bcs.socionegocio.bck.ReintentoPrestamosBlockChain.Anticipos;
import cl.bcs.socionegocio.pojo.ConstantesBCK;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class AnticiparPrestamo implements JavaCall {
	
	private final String SQL_SELECT_PRESTAMOS_PROCESO 		= loadResourceToString("cl/bcs/custodia/sql/AnticiparPrestamo_SelectPrestamosProceso.sql");
	private final String SQL_SELECT_DATOS_EMPRESTITOS 		= loadResourceToString("cl/bcs/custodia/sql/AnticiparPrestamo_SelectDatosEmprestitos.sql");
	private final String SQL_UPDATE_ESTADO_ANTICIPO			= loadResourceToString("cl/bcs/custodia/sql/AnticiparPrestamo_UpdateEstadoAnticipo.sql");
	private final String SQL_SELECT_INGCANTIDAD 			= loadResourceToString("cl/bcs/socionegocio/bck/sql/ReintentoPrestamosBlockChain_SelectIngCantidad.sql");
	private final String SQL_SELECT_ESCONTRATOBLOCKCHAIN	= loadResourceToString("cl/bcs/socionegocio/bck/sql/ContratoEsBlockChainFirmado_SelectTipoDocumento.sql");
	
	public static final String TIPO_PRESTAMO_VENTA_CORTA 	 = "PRTVTC";
	
	//-------------------------------------------------
	// CLASES DE ENTRADA 
	//-------------------------------------------------

	public static class AnticiparPrestamo_Entrada extends BcsPojo {
		public Long       										p_IdEmpresa;
		public Long       										p_IdUsuario;
		public String     										p_IdModulo;	
		public String     										p_UsaReglasNegocio = "S";
		public String 											p_FechaAnticipo;
		public Long       										p_IdPais;
		public String     										p_SimularOperacion = "N";
		public String     										p_EsDemo = "N";
		public String     										p_UsaPrecioMedio = "N";      // S/N
		public List<AnticiparPrestamo_Entrada_DatosPrestamo>	p_DatosPrestamos;
		public String 											p_VieneDesdeBlockChain = "N"; 	// si esto esta en S
																								// significa que debe
																								// saltarse validar en
																								// BlockChain e ir a Anticipar en BlockChain
		public AnticiparPrestamo_Entrada() {
			this.p_DatosPrestamos	 =  new ArrayList<AnticiparPrestamo_Entrada_DatosPrestamo>();
		}
	}
	
	public static class AnticiparPrestamo_Entrada_DatosPrestamo extends BcsPojo {
		public Long       		FolioPrestamo;    // Es el folio de la cartera del prestamo 
		public String      		FechaAnticipo;
		public Long       		IdPais;
		public String      		IdMonedaLiquidacion;
		public String      		IdCondicionLiquidacion;
		public String			FechaLiquidacion;
		public String      		DevolverCustodia;
		public Long       		IdEmpleadoDigitador;
		public BigDecimal      	Cantidad;
		public BigDecimal      	PrecioInstrumento;
		public BigDecimal      	TasaAnticipo; 
		public BigDecimal      	MontoPrima;
		public BigDecimal 		MontoPremio;
		public BigDecimal      	Monto;
		public BigDecimal      	MontoAPlazo;
		public String			FolioSistemaExterno = ""; //valor de Folio BlockChain para anticipar el prestamo en BlockChain Tambien
		public String			EsAnticipoParcial;
		public BigDecimal		CantidadBlockChain;
		public String 			IdBolsa;
	}
	
	
	//-------------------------------------------------
	// CLASES DE SALIDA 
	//-------------------------------------------------

	public static class AnticiparPrestamo_Salida extends BcsPojo {	
		public List<MensajeSalida>					P_OUT_MENSAJES;
		public Long									P_OUT_ERRORESCONTADOR;
		public List<MovimientoComprobante_Salida>	P_OUT_MOVIMIENTOCOMPROBANTE;
		
		public AnticiparPrestamo_Salida() {
			this.P_OUT_MENSAJES	 				=  new ArrayList<MensajeSalida>();
			this.P_OUT_MOVIMIENTOCOMPROBANTE 	=  new ArrayList<MovimientoComprobante_Salida>();
			this.P_OUT_ERRORESCONTADOR 			= 0L;
		}
	}
	
	public static class MovimientoComprobante_Salida extends BcsPojo {			
		public String			Origen;
		public Long       		FolioPrestamo;
		public Long       		IdMovimientoComprobante;
		public String			IdTipoComprobante;
	}
	
	//-------------------------------------------------
	// CLASES UTILITARIAS
	//-------------------------------------------------
	
	public static class SelectEstadoAnticipo extends BcsPojo {
		public String 	EstadoAnticipo;
		public Long 	IdCartera;
	}
	
	public static class Emprestito extends BcsPojo {
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
	public AnticiparPrestamo_Salida logica(Connection con, AnticiparPrestamo_Entrada entrada) throws Exception {
		
		AnticiparPrestamo_Salida salida = new AnticiparPrestamo_Salida();
		SQL sql 						= new SQL();
		SimpleDateFormat dateFormat 	= new SimpleDateFormat("yyyy-MM-dd");
		String fechaSistema 			= ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		List<VencerPrestamos.ObjRegistroPrestamo> lstPrestamo 	= new ArrayList<VencerPrestamos.ObjRegistroPrestamo>();
		HashMap<String,Object> prmsBuscaPrestamos 				= new HashMap<String,Object>();
		
		List<Long> foliosPrestamo = entrada.p_DatosPrestamos.parallelStream()
				.map( linea -> linea.FolioPrestamo )
				.distinct()
				.collect( Collectors.toList() );
		prmsBuscaPrestamos.put("p_IdEmpresa", entrada.p_IdEmpresa);
		prmsBuscaPrestamos.put("p_FolioPrestamo", foliosPrestamo);
		lstPrestamo = sql.query(con, SQL_SELECT_PRESTAMOS_PROCESO, prmsBuscaPrestamos, VencerPrestamos.ObjRegistroPrestamo.class);
		
		HashMap<String,Object> parametrosBuscaEmprestitos = new HashMap<String,Object>();
		parametrosBuscaEmprestitos.put("p_FolioPrestamo", prmsBuscaPrestamos.get("p_FolioPrestamo"));
		parametrosBuscaEmprestitos.put("p_IdEmpresa"	, prmsBuscaPrestamos.get("p_IdEmpresa"));
		List<Emprestito> emprestitos = sql.query(con, SQL_SELECT_DATOS_EMPRESTITOS, parametrosBuscaEmprestitos, Emprestito.class);
		
		if( emprestitos.stream().count() > 1 ) {
			salida.P_OUT_ERRORESCONTADOR = 1L;
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_ANTICIPO_CANTIDAD_EMPRESTITOS", new HashMap<String, String>(), con, entrada.p_IdEmpresa));
			return salida;
		}
		
		for ( VencerPrestamos.ObjRegistroPrestamo prestamo : lstPrestamo ) {	
			Date fechaInicial   = dateFormat.parse(fechaSistema); 
			Date fechaFinal 	= prestamo.FechaVencimientoPrestamo;
			int diferenciaDias 	= (int) ((fechaFinal.getTime()-fechaInicial.getTime())/86400000);
			
			if ( diferenciaDias <= 0 ) {
				HashMap<String,String>  valores = new HashMap<String,String>();
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "FECHA_ANTICIPO_MENOR_VCTO", valores, con, entrada.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR++;
				return salida;
			}	
		}
		
		
		VencerPrestamos ObjAnticipo 			= new VencerPrestamos();
		VencerPrestamos.Entrada paramsAnticipo 	= new VencerPrestamos.Entrada();
		VencerPrestamos.Salida resulAnticipo 	= new VencerPrestamos.Salida();
		
		paramsAnticipo.p_IdEmpresa 				= entrada.p_IdEmpresa;
		paramsAnticipo.p_Fecha 					= entrada.p_FechaAnticipo;
		paramsAnticipo.p_IdModulo 				= entrada.p_IdModulo;
		paramsAnticipo.p_IdPais 				= entrada.p_IdPais;
		paramsAnticipo.p_IdUsuario 				= entrada.p_IdUsuario;
		paramsAnticipo.p_OrigenLiquidacion 		= "ANTICIPO";
		paramsAnticipo.p_lstPrestamos 			= new ArrayList<ObjRegistroPrestamo>();
		paramsAnticipo.p_VieneDesdeBlockChain 	= entrada.p_VieneDesdeBlockChain; 
		
		if ( lstPrestamo.size() > 0 ) {
			for ( AnticiparPrestamo.AnticiparPrestamo_Entrada_DatosPrestamo prtNew : entrada.p_DatosPrestamos ) {
				for ( VencerPrestamos.ObjRegistroPrestamo prtOrig : lstPrestamo ) {
					
					List<Anticipos> arrSumIng 				= new ArrayList<>();
					List<Long> foliosCartera				= new ArrayList<Long>();
					foliosCartera.add( prtOrig.FolioCartera );
					HashMap<String,Object> parametroEntrada = new HashMap<String,Object>();
					parametroEntrada 						= new HashMap<String,Object>();
					parametroEntrada.put("p_IdEmpresa"		, entrada.p_IdEmpresa);
					parametroEntrada.put("p_FolioPrestamo"	, foliosCartera);
					arrSumIng = sql.query(con, SQL_SELECT_INGCANTIDAD, parametroEntrada, Anticipos.class);
					
					if ( prtOrig.FolioCartera.equals(prtNew.FolioPrestamo) ) {						
						prtOrig.FechaOperacion 							= Fecha.toDate(prtNew.FechaAnticipo);
						prtOrig.FechaLiquidacion 						= prtNew.FechaAnticipo;
						prtOrig.IdCondicionLiquidacion 					= prtNew.IdCondicionLiquidacion;
						prtOrig.IdUsuario 								= prtNew.IdEmpleadoDigitador;
						prtOrig.Cantidad 								= prtNew.Cantidad;
						prtOrig.INGPrecioCompra 						= prtNew.PrecioInstrumento;
						prtOrig.INGTasaCompra 							= prtNew.TasaAnticipo;
						prtOrig.INGMontoPrima 							= prtNew.MontoPrima;
						prtOrig.INGMontoPremio 							= prtNew.MontoPremio;
						prtOrig.INGMontoCompraActualMonedaInstrumento 	= prtNew.Monto;
						prtOrig.INGMontoATasaCompraActual 				= prtNew.Monto;
						prtOrig.INGMontoAPlazo 							= prtNew.MontoAPlazo;
						prtOrig.FolioSistemaExterno 					= prtNew.FolioSistemaExterno;
						prtOrig.EsAnticipoParcial 						= prtNew.Cantidad.compareTo(arrSumIng.get(0).EGRCantidad.add(prtOrig.INGCantidad)) != 0 ? "S" : "N";
						prtOrig.PrestamoAsignadoTotal					= prtOrig.CantidadPorAsignar.compareTo(BigDecimal.ZERO) == 0 ? "S": "N";
						prtOrig.IdAgente								= entrada.p_IdUsuario;
						prtOrig.IdBolsa									= prtNew.IdBolsa;
						paramsAnticipo.p_lstPrestamos.add(prtOrig);			
						prtNew.EsAnticipoParcial						= prtNew.Cantidad.compareTo(arrSumIng.get(0).EGRCantidad.add(prtOrig.INGCantidad)) != 0 ? "S" : "N";
						prtNew.CantidadBlockChain						= prtOrig.INGCantidad.subtract(prtNew.Cantidad);
						break;
					}
				}
			}
			try {
				resulAnticipo = ObjAnticipo.logica(con, paramsAnticipo);
				salida.P_OUT_ERRORESCONTADOR = resulAnticipo.P_OUT_ERRORESCONTADOR;
				salida.P_OUT_MENSAJES.addAll(resulAnticipo.P_OUT_MENSAJES);
			}catch(Exception e) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, Diccionario.getMensaje(con, entrada.p_IdEmpresa, "UI_ERROR_ANTICIPARPRESTAMO").msg(), null, con, entrada.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR++;
				return salida;
			}
			
			if( salida.P_OUT_ERRORESCONTADOR == 0 ) {
				if(!emprestitos.isEmpty()) {
					AnticiparPrestamo_Salida salidaInformarAnticipoPrestamoBlockChain = informarAnticipoPrestamoBlockChain(con, entrada, emprestitos.get(0).DocumentoIdentidad, lstPrestamo);
					salida.P_OUT_ERRORESCONTADOR += salidaInformarAnticipoPrestamoBlockChain.P_OUT_ERRORESCONTADOR;
					salida.P_OUT_MENSAJES.addAll(salidaInformarAnticipoPrestamoBlockChain.P_OUT_MENSAJES);
				}
				
			}
		}
		
		return salida;
	}
	
	public AnticiparPrestamo_Salida informarAnticipoPrestamoBlockChain(Connection con, AnticiparPrestamo_Entrada entrada, String documentoIdentidadEmprestito, List<VencerPrestamos.ObjRegistroPrestamo> lstPrestamo) throws Exception {
		
		AnticiparPrestamo_Salida salida 			= new AnticiparPrestamo_Salida();
		SQL sql 									= new SQL();
		HashMap<String,Object> parametros 			= new HashMap<String,Object>();
		
		if( entrada.p_VieneDesdeBlockChain.equalsIgnoreCase("S") ) {
			return salida;
		}
		
		/*** TIPO DOCUMENTO BLOCKCHAIN HABILITADO ***/
		parametros.put("p_IdTipoDocumento", lstPrestamo.get(0).IdTipoPrestamo.equalsIgnoreCase(TIPO_PRESTAMO_VENTA_CORTA) ? ConstantesBCK.CONTRATOPRESTAMOANEXO42 : ConstantesBCK.CONTRATOPRESTAMOANEXO48);
		List<TipoDocumento> arrTipoDocumento = sql.query(con, SQL_SELECT_ESCONTRATOBLOCKCHAIN, parametros, TipoDocumento.class);
		parametros.clear();
		
		if ( arrTipoDocumento.size() == 0 ) {
			return salida;
		}
		
		AnticiparPrestamoBlockChain AnticiparPrestamoBlockchain 				= new AnticiparPrestamoBlockChain();
		AnticiparPrestamoBlockChain_Entrada AnticiparPrestamoBlockChainEntrada 	= new AnticiparPrestamoBlockChain_Entrada();
		AnticiparPrestamoBlockChain_Salida AnticiparPrestamoBlockChainSalida 	= new AnticiparPrestamoBlockChain_Salida();
		
		for ( AnticiparPrestamo.AnticiparPrestamo_Entrada_DatosPrestamo prtNew : entrada.p_DatosPrestamos ) {
			if( !prtNew.FolioSistemaExterno.isEmpty() || !prtNew.FolioSistemaExterno.equals("0") ) {
				if ( prtNew.EsAnticipoParcial.equalsIgnoreCase("S") ) {
					AnticiparPrestamoBlockChainEntrada.p_DocumentoIdentidadPrestamista 	= documentoIdentidadEmprestito;
					AnticiparPrestamoBlockChainEntrada.Cantidad							= prtNew.CantidadBlockChain;
				}
				else {
					AnticiparPrestamoBlockChainEntrada.Cantidad					= prtNew.Cantidad;
				}
				AnticiparPrestamoBlockChainEntrada.esAnticipoParcial		 	= prtNew.EsAnticipoParcial;
				AnticiparPrestamoBlockChainEntrada.p_IdEmpresa					= entrada.p_IdEmpresa;
				AnticiparPrestamoBlockChainEntrada.p_Fecha						= prtNew.FechaAnticipo;
				AnticiparPrestamoBlockChainEntrada.p_FechaLiquidacion			= prtNew.FechaLiquidacion;
				FolioPrestamoBlockChain folio = new FolioPrestamoBlockChain();
				folio.FolioPrestamoBlockChain = prtNew.FolioSistemaExterno;
				AnticiparPrestamoBlockChainEntrada.p_FoliosPrestamoBlockChain.add(folio);
				AnticiparPrestamoBlockChainSalida = AnticiparPrestamoBlockchain.logica(con, AnticiparPrestamoBlockChainEntrada);
				if( AnticiparPrestamoBlockChainSalida.P_OUT_ERRORESCONTADOR > 0 ) {
					salida.P_OUT_MENSAJES = AnticiparPrestamoBlockChainSalida.P_OUT_MENSAJES;
					salida.P_OUT_ERRORESCONTADOR++;
				} else {
					actualizarEstadoBlockchain(con, AnticiparPrestamoBlockChainSalida.P_OUT_STATUS, lstPrestamo.get(0).IdCartera);
					salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "UI_BC_OK_ANTICIPO_PRESTAMO", new HashMap<String, String>(), con, entrada.p_IdEmpresa));
				}
			} else {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, "UI_BLOCKCHAIN_DEBE_ENVIAR_FOLIO_PRESTAMOBC", new HashMap<String, String>(), con, entrada.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR++;
				break;
			}
		}
		
		return salida;
	}
	
	public void actualizarEstadoBlockchain(Connection con, List<Status> status, long IdCartera ) throws SQLException {
		SQL sql 										= new SQL();
		List<SelectEstadoAnticipo> selectEstadoAnticipo = new ArrayList<SelectEstadoAnticipo>();
		SelectEstadoAnticipo actualizarEstado 			= new SelectEstadoAnticipo();
		actualizarEstado.IdCartera = IdCartera;
		if( status.get(0).Status.isEmpty() ) {
			actualizarEstado.EstadoAnticipo = "RZD";
		}
		else {
			if( status.get(0).Status.contentEquals("SUCCESS") ) {
				actualizarEstado.EstadoAnticipo = "APD";
			} else if ( status.get(0).Status.contentEquals("ERROR") ) {
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
		AnticiparPrestamo_Entrada in = gson.fromJson(jin.toString(), AnticiparPrestamo_Entrada.class);
		AnticiparPrestamo_Salida out = logica(con,in);
		
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
		
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa02","connections.properties")) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();		

			String jsonSinEmprestito = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9524,\"p_IdModulo\":\"CUS\",\"p_UsaReglasNegocio\":\"S\",\"p_IdPais\":44,\"p_DatosPrestamos\":[{\"FolioPrestamo\":1693277,\"FechaAnticipo\":\"2026-06-10\",\"IdPais\":44,\"IdMonedaLiquidacion\":\"CLP\",\"IdCondicionLiquidacion\":\"T0\",\"FechaLiquidacion\":\"2026-06-10\",\"DevolverCustodia\":\"S\",\"IdEmpleadoDigitador\":108,\"Cantidad\":3000,\"PrecioInstrumento\":6420,\"TasaAnticipo\":2,\"IdBolsa\":\"BCS\",\"MontoPrima\":77040,\"MontoPremio\":0,\"Monto\":19260000,\"MontoAPlazo\":19337040,\"FolioSistemaExterno\":\"\"}],\"p_UsaPrecioMedio\":\"N\",\"p_FechaAnticipo\":\"2026-06-10\"}";
			String jsonAnticipo= "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9524,\"p_IdModulo\":\"CUS\",\"p_UsaReglasNegocio\":\"S\",\"p_IdPais\":44,\"p_DatosPrestamos\":[{\"FolioPrestamo\":1693802,\"FechaAnticipo\":\"2026-06-12\",\"IdPais\":44,\"IdMonedaLiquidacion\":\"CLP\",\"IdCondicionLiquidacion\":\"T0\",\"FechaLiquidacion\":\"2026-06-12\",\"DevolverCustodia\":\"S\",\"IdEmpleadoDigitador\":108,\"Cantidad\":5000,\"PrecioInstrumento\":26.08,\"TasaAnticipo\":2,\"IdBolsa\":\"BCS\",\"MontoPrima\":86.93333333331975,\"MontoPremio\":87,\"Monto\":130400,\"MontoAPlazo\":130486.93333333332,\"FolioSistemaExterno\":\"\"}],\"p_UsaPrecioMedio\":\"N\",\"p_FechaAnticipo\":\"2026-06-12\"}";
			
			
			AnticiparPrestamo_Entrada Objin = gson.fromJson(jsonSinEmprestito, AnticiparPrestamo_Entrada.class);
			AnticiparPrestamo demo = new AnticiparPrestamo();
 			
			System.out.println("ENTRADA:" + Objin.toString());
			
 			try {
 				AnticiparPrestamo_Salida ObjOut = demo.logica(con, Objin);
 				System.out.println("SALIDA:" + ObjOut);
 				con.rollback();
 				
 			} catch(Exception er) {
				er.printStackTrace();
				con.rollback();
			}

	    }

		System.out.println("== FIN PRUEBA LOCAL ==");
	}
	
	
}
