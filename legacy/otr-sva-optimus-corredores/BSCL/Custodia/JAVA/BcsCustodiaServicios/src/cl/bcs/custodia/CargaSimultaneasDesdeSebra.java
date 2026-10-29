package cl.bcs.custodia;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.IngresarGarantiaJava.IngresarGarantiaEntrada;
import cl.bcs.custodia.IngresarGarantiaJava.IngresarGarantiaSalida;
import cl.bcs.custodia.IngresarGarantiaJava.ObjGarantiaCustodia;
import cl.bcs.custodia.ProcesaCargaMasivaCustodia.ParametrosActualizacion;
import cl.bcs.custodia.ProcesaCargaMasivaCustodia.ParametrosActualizacionDeta;
import cl.bcs.custodia.utils.IngresoCustodia;
import cl.bcs.custodia.utils.RetiroCustodia;
import cl.bcs.custodia.utils.RetiroCustodia.RetiroCustodiaSalida.ObjDatosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

//
 
public class CargaSimultaneasDesdeSebra implements JavaCall {

	private final Log log = new Log("CUS.CargaSimultaneasDesdeSebra");

	private final String SQL_SELECT_OPERACIONESSIM = loadResourceToString("cl/bcs/custodia/sql/ProcesarCargaMasivaSimultaneaSebra_SelectCargaMasivaSimultaneaSebra.sql");
		
	public static class DetalleCarga {
		public Long                 IdCargaSimultaneasSebra;
		public String 				CodigoPortafolio;
		public String 				IdCondicionLiquidacion;
		public String 				IdBoveda;
		public String 				IdConcepto;
		public Long 				IdInstrumento;
		public String 				Nemotecnico;
		public String 				IdTipoInstrumento;
		public String 				IdEmisor;
		public String 				FechaEmision;
		public String				FechaVencimientoInstrumento;
		public BigDecimal			CorteMinimo;
		public String 				IdTipoReajuste;
		public String 				FechaVencimiento;
		public BigDecimal			Cantidad;
		public BigDecimal 			Precio;
		public BigDecimal 			Tasa;
		public BigDecimal 			Monto;
		public BigDecimal 			ValorRescate;
		public String 				NemoGarantizado;
		public String 				IdMoneda;
		public String 				EsFungible;
		public String 				IdBolsa;
		public String 				Glosa;
		public String 				IdEstado;
		public String 				ObservacionValidacion;
		public String 				CuentaCustodio;
		public String 				IdTipoOperacion;
		public String 				ClasificacionInstrumento;
		public long					Plazo;
		public String 				FechaLiquidacion;
		public String 				FechaOperacion;
		public BigDecimal 			TipoCambio;
		public BigDecimal 			MontoContado;
		public BigDecimal 			MontoPlazo;
		public String 				EsNominal;
		public String 				DocumentoIdentidad;
		public Long 				IdCliente;
		public Long 				IdPersona;		
		public Long 				IdPortafolio;
		public Long 				IdCuentaCustodioPortafolio;
		public Long					IdInstrumentoSubyacente;
		public String 				NemotecnicoSubyacente;	
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DetalleCuentasCustodio {
		public Long IdCuentaCustodioPortafolio;
		public String IdBoveda;
		public String Codigo;
		public Long IdPortafolio;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ProcesarCargaMasivaCustodiaReporteEntrada {
		public Long 								p_IdUsuario;
		public Long 								p_IdEmpresa;
		public String 								p_IdModulo;
		public String 								p_UsaCarterasSistema;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ProcesarCargaMasivaCustodiaReporteSalida {
		public List<MensajeSalida>			P_OUT_MENSAJES;
		public Long							P_OUT_ERRORESCONTADOR;
		public String						P_OUT_DETALLE_CUADRATURA;
		
		public ProcesarCargaMasivaCustodiaReporteSalida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	/*
	 * Objeto de transporte
	 */
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		ProcesarCargaMasivaCustodiaReporteEntrada in = gson.fromJson(jin.toString(), ProcesarCargaMasivaCustodiaReporteEntrada.class);
		ProcesarCargaMasivaCustodiaReporteSalida out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public ProcesarCargaMasivaCustodiaReporteSalida logica(Connection conn, ProcesarCargaMasivaCustodiaReporteEntrada in) throws Exception {
		SQL _sql = new SQL();
		ProcesarCargaMasivaCustodiaReporteSalida out = new ProcesarCargaMasivaCustodiaReporteSalida();
		String monedaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		int contador = 1;
		Long idCarga = 76L;
		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresaService.logica(conn, in.p_IdEmpresa);
		Long idPaisEmpresa = empresa.IdPais;
		log.write("INICIO");
		try 
		//*************************************************************************	INICIO	***********************************************************************************		
		{
			//TODO : PRINCIPAL
			HashMap<String,Object> regDetalleCarga = new HashMap<>();
			regDetalleCarga.put("CodPerIni", "2018-04");
			regDetalleCarga.put("CodPerFin", "2018-04");
			regDetalleCarga.put("FechaVencimiento", "2018-04-30");
			List<DetalleCarga> rsDetalle = _sql.query(conn, SQL_SELECT_OPERACIONESSIM, regDetalleCarga, DetalleCarga.class);
			
			List<IngresoCustodia.ObjDatosEntrada> 		registroRepIngreso		= new ArrayList<IngresoCustodia.ObjDatosEntrada>();			
			List<RetiroCustodia.ObjDatosEntrada> 		registroRepRetiro 		= new ArrayList<RetiroCustodia.ObjDatosEntrada>();						
			List<IngresoCustodia.ObjDatosEntrada> 		registroRepIngresoEnc 	= new ArrayList<IngresoCustodia.ObjDatosEntrada>();			
			List<IngresoCustodia.ObjDatosEntrada> 		registroRepIngresoSub 	= new ArrayList<IngresoCustodia.ObjDatosEntrada>();
//			List<ObjGarantiaCustodia> 					detalleGarantiaCustodia = new ArrayList<ObjGarantiaCustodia>();
			List<IngresoCustodia.ObjDatosEntrada> 		detalleGarantiaCustodia	= new ArrayList<IngresoCustodia.ObjDatosEntrada>();	
		
			RetiroCustodia.RetiroCustodiaSalida 		resultadoRepRetiroRes	= new RetiroCustodia.RetiroCustodiaSalida();
			IngresoCustodia.IngresoCustodiaSalida 		resultadoRepIngresoRes	= new IngresoCustodia.IngresoCustodiaSalida();
			
//******************************************************************************************************************************************************************************				
//*********************************************************************************		VENTAS 		****************************************************************************
//******************************************************************************************************************************************************************************

//1.0 INGRESO DE CARTERA 	-- INICIO		
			 
			
			for (DetalleCarga recDetalle: rsDetalle)
			{
				if (recDetalle.IdTipoOperacion.equals("VENSIM") && (in.p_UsaCarterasSistema.equals("N")) ) 
				{					
					IngresoCustodia.ObjDatosEntrada	ObjEntrada = new IngresoCustodia.ObjDatosEntrada();
					ObjEntrada.IdGenerarMovimientoCustodia			= recDetalle.IdCargaSimultaneasSebra;
					ObjEntrada.TablaAfectada						= "CARTERA";
					ObjEntrada.AccionOperacion						= "INGRESO";
					ObjEntrada.EsRegistroNuevo						= "S";
					ObjEntrada.IdCliente							= recDetalle.IdCliente;										//		recDetalle.IdCliente; 
					ObjEntrada.IdPortafolio							= recDetalle.IdPortafolio;									//		mov2.IdPortafolio;
					ObjEntrada.FechaOperacion						= Fecha.toDate(recDetalle.FechaOperacion);					//		mov2.FechaOperacion;
					ObjEntrada.IdTipoOperacion						= recDetalle.IdTipoOperacion;								//		mov2.IdTipoOperacion;		
					ObjEntrada.IdConcepto							= recDetalle.IdConcepto;									//		mov2.IdConcepto;
					ObjEntrada.IdCondicionLiquidacion				= recDetalle.IdCondicionLiquidacion;						//		mov2.IdCondicionLiquidacion; 
					ObjEntrada.FechaLiquidacion						= Fecha.toDate(recDetalle.FechaLiquidacion);				//		mov2.FechaLiquidacion;
					ObjEntrada.IdUsuario							= in.p_IdUsuario;											//		mov2.IdUsuario;
					ObjEntrada.Glosa								= recDetalle.Glosa;											//		mov2.Glosa;
					ObjEntrada.IdInstrumento						= recDetalle.IdInstrumento;									//		mov2.IdInstrumento;
					ObjEntrada.IdMonedaInstrumento					= recDetalle.IdMoneda;										//		mov2.IdMonedaInstrumento;
					ObjEntrada.IdTipoReajusteInstrumento			= recDetalle.IdTipoReajuste;								//		mov2.IdTipoReajuste;
					ObjEntrada.IdEmisor								= recDetalle.IdEmisor;										//		mov2.IdEmisor;
					ObjEntrada.EsFungible							= "N";//recDetalle.EsFungible;								//		mov2.EsFungible;
					ObjEntrada.IdBoveda								= recDetalle.IdBoveda;										//		mov2.IdBoveda;
					ObjEntrada.EsNominal							= recDetalle.EsNominal;										//		mov2.EsNominal;			
					ObjEntrada.Cantidad								= recDetalle.Cantidad;										//		mov2.Cantidad; 
					ObjEntrada.CorteMinimo							= recDetalle.CorteMinimo;									//		mov2.CorteMinimo; 
					ObjEntrada.ValorRescate							= recDetalle.ValorRescate;									//		mov2.ValorRescate; 
					ObjEntrada.PrecioCompra							= recDetalle.Precio;										//	*	mov2.PrecioCompra; 
					ObjEntrada.TasaCompra							= recDetalle.Tasa;											//		mov2.TasaCompra;
					ObjEntrada.MontoCompraMonedaInstrumento			= recDetalle.Monto;											//	*	mov2.MontoCompraMonedaInstrumento;
					ObjEntrada.IdCarga                              = idCarga;
					registroRepIngreso.add(ObjEntrada);					
				} 				
			}
			
			System.out.println("Objeto INGRESO: ");
			System.out.println(registroRepIngreso);

		
			if (registroRepIngreso.size() > 0)
			{
				IngresoCustodia ingresoCustodia = new IngresoCustodia();
				IngresoCustodia.IngresoCustodiaEntrada paramInSPCabecera = new IngresoCustodia.IngresoCustodiaEntrada();
				paramInSPCabecera.p_IdEmpresa			= in.p_IdEmpresa;		
				paramInSPCabecera.p_IdModulo			= in.p_IdModulo;
				paramInSPCabecera.p_SimularOperacion	= "N";
				paramInSPCabecera.p_TicketAtencion 		= 0L;
				paramInSPCabecera.p_DatosEntrada		= registroRepIngreso;				
				IngresoCustodia.IngresoCustodiaSalida resultadoRepIngreso = ingresoCustodia.logica(conn, paramInSPCabecera);
				
				System.out.println("Resultado INGRESO: ");
				System.out.println(resultadoRepIngreso);
		
//1.0 INGRESO DE CARTERA 	-- FIN				
										
					
//2.0 RETIRO DE CARTERA 	-- INICIO
				for (DetalleCarga recDetalle: rsDetalle)
				{
					RetiroCustodia.ObjDatosEntrada	ObjEntrada = new RetiroCustodia.ObjDatosEntrada();
					ObjEntrada.IdGenerarMovimientoCustodia			= recDetalle.IdCargaSimultaneasSebra;
					ObjEntrada.TablaAfectada						= "CARTERA";
					ObjEntrada.AccionOperacion						= "RETIRO";
					ObjEntrada.IdCliente							= recDetalle.IdCliente;										//		recDetalle.IdCliente; 
					ObjEntrada.IdPortafolio							= recDetalle.IdPortafolio;									//		mov2.IdPortafolio;
					ObjEntrada.FechaOperacion						= Fecha.toDate(recDetalle.FechaOperacion);					//		mov2.FechaOperacion;
					ObjEntrada.IdTipoOperacion						= recDetalle.IdTipoOperacion;								//		mov2.IdTipoOperacion;		
					ObjEntrada.IdConcepto							= recDetalle.IdConcepto;									//		mov2.IdConcepto;
					ObjEntrada.IdCondicionLiquidacion				= recDetalle.IdCondicionLiquidacion;						//		mov2.IdCondicionLiquidacion; 
					ObjEntrada.FechaLiquidacion						= Fecha.toDate(recDetalle.FechaLiquidacion);				//		mov2.FechaLiquidacion;
					ObjEntrada.IdUsuario							= in.p_IdUsuario;											//		mov2.IdUsuario;
					ObjEntrada.Glosa								= recDetalle.Glosa;											//		mov2.Glosa;
					ObjEntrada.IdInstrumento						= recDetalle.IdInstrumento;									//		mov2.IdInstrumento;
					ObjEntrada.IdMonedaInstrumento					= recDetalle.IdMoneda;										//		mov2.IdMonedaInstrumento;
					ObjEntrada.IdTipoReajusteInstrumento			= recDetalle.IdTipoReajuste;								//		mov2.IdTipoReajuste;
					ObjEntrada.IdEmisor								= recDetalle.IdEmisor;										//		mov2.IdEmisor;
					ObjEntrada.EsFungible							= "N";//recDetalle.EsFungible;									//		mov2.EsFungible;
					ObjEntrada.IdBoveda								= recDetalle.IdBoveda;										//		mov2.IdBoveda;
					ObjEntrada.EsNominal							= recDetalle.EsNominal;										//		mov2.EsNominal;			
					ObjEntrada.Cantidad								= recDetalle.Cantidad;										//		mov2.Cantidad; 
					ObjEntrada.CorteMinimo							= recDetalle.CorteMinimo;									//		mov2.CorteMinimo; 
					ObjEntrada.ValorRescate							= recDetalle.ValorRescate;									//		mov2.ValorRescate; 
					ObjEntrada.PrecioCompra							= recDetalle.Precio;										//	*	mov2.PrecioCompra; 
					ObjEntrada.TasaCompra							= recDetalle.Tasa;											//		mov2.TasaCompra;
					ObjEntrada.MontoCompraMonedaInstrumento			= recDetalle.Monto;		
					ObjEntrada.IdCarga                              = idCarga;
					//	*	mov2.MontoCompraMonedaInstrumento;
					//ObjEntrada.IdMovimientoCarteraEnTransitoOrigen= mov.IdMovimientoCarteraEnTransito;			
					//ObjEntrada.IdTipoCanalIngreso					= mov2.IdTipoCanalIngreso; 		
					//ObjEntrada.IdTipoComprobante					= mov2.IdTipoComprobante; 
					//ObjEntrada.FolioComprobante					= mov2.FolioComprobante;
					//ObjEntrada.FolioExternoReferencia				= mov2.FolioExternoReferencia;
					//ObjEntrada.IdSucursal							= mov2.IdSucursal; 
					//ObjEntrada.IdAgente							= mov2.IdAgente;
					
							
					for (IngresoCustodia.IngresoCustodiaSalida.ObjDatosSalida recorreIngresos: resultadoRepIngreso.P_OUT_DATOSSALIDA)
						{						
							if (recDetalle.IdCargaSimultaneasSebra.equals(recorreIngresos.IdGenerarMovimientoCustodia))
							{
								ObjEntrada.FolioCustodia 	= recorreIngresos.FolioCustodia;
								ObjEntrada.IdCustodia		= recorreIngresos.IdCustodia;						
								break;
							}						
						}
						registroRepRetiro.add(ObjEntrada);					
				}
						
			}
			
			System.out.println("Objeto RETIRO: ");
			System.out.println(registroRepRetiro);

					
			if (registroRepRetiro.size() > 0)
			{
				RetiroCustodia retirosCustodia = new RetiroCustodia();
				RetiroCustodia.RetiroCustodiaEntrada paramInSP = new RetiroCustodia.RetiroCustodiaEntrada();
				paramInSP.p_IdEmpresa			= in.p_IdEmpresa;		
				paramInSP.p_SimularOperacion	= "N";
				paramInSP.p_TicketAtencion 		= 0L;
				paramInSP.p_DatosEntrada		= registroRepRetiro;				
				RetiroCustodia.RetiroCustodiaSalida resultadoRepRetiro = retirosCustodia.logica(conn, paramInSP);

				resultadoRepRetiroRes.P_OUT_DATOSSALIDA.addAll(resultadoRepRetiro.P_OUT_DATOSSALIDA);
			
						
				if (resultadoRepRetiro.P_OUT_MENSAJES.size() > 0)
				{
					if (!resultadoRepRetiro.P_OUT_ERRORESCONTADOR.equals(0L)){
						for (MensajeSalida recorreErr: resultadoRepRetiro.P_OUT_MENSAJES){
							out.P_OUT_MENSAJES.add(new MensajeSalida(recorreErr.id ,MensajeSalida.MENSAJE_ERROR, "UI_INGRESO_REPORTE_ERROR", new HashMap<>() ,conn,in.p_IdEmpresa));
							out.P_OUT_ERRORESCONTADOR = 1L;
							return out;
						}
					}
					out.P_OUT_MENSAJES.add(new MensajeSalida(1L ,MensajeSalida.MENSAJE_INFORMACION, "UI_INGRESO_REPORTE_MASIVO_OK", new HashMap<>() ,conn,in.p_IdEmpresa));
					
					out.P_OUT_ERRORESCONTADOR = 0L;
				}
						
				System.out.println("Resultado RETIRO: ");
				System.out.println(resultadoRepRetiro);
//2.0 RETIRO DE CARTERA 	-- FIN				
					
		
//3.0 INGRESO CABECERA 	-- INICIO				
				for (DetalleCarga recDetalle: rsDetalle)
				{					
					IngresoCustodia.ObjDatosEntrada	ObjEntrada = new IngresoCustodia.ObjDatosEntrada();
					ObjEntrada.IdGenerarMovimientoCustodia			= recDetalle.IdCargaSimultaneasSebra;
					ObjEntrada.TablaAfectada						= "CARTERA";
					ObjEntrada.AccionOperacion						= "INGRESO";
					ObjEntrada.EsRegistroNuevo						= "S";
					ObjEntrada.IdCliente							= recDetalle.IdCliente;						//		mov.IdCliente; 
					ObjEntrada.IdPortafolio							= recDetalle.IdPortafolio;					//		mov.IdPortafolio;
					ObjEntrada.IdCuentaCustodioPortafolio			= recDetalle.IdCuentaCustodioPortafolio;	//		mov.IdCuentaCustodioPortafolio;
					ObjEntrada.FechaOperacion						= Fecha.toDate(recDetalle.FechaOperacion);	//		mov.FechaOperacion;
					ObjEntrada.FechaVencimientoOperacion			= Fecha.toDate(recDetalle.FechaVencimiento);//	*	mov.FechaVencimientoOperacion;
					ObjEntrada.PlazoEnDiasOperacion					= recDetalle.Plazo;							//	*	mov.PlazoEnDiasOperacion;
					ObjEntrada.IdTipoOperacion						= recDetalle.IdTipoOperacion;				//		mov.IdTipoOperacion;		
					ObjEntrada.IdConcepto							= recDetalle.IdConcepto;					//		mov.IdConcepto;
					ObjEntrada.IdCondicionLiquidacion				= recDetalle.IdCondicionLiquidacion;		//		mov.IdCondicionLiquidacion; 
					ObjEntrada.FechaLiquidacion						= Fecha.toDate(recDetalle.FechaLiquidacion);//		mov.FechaLiquidacion;
					ObjEntrada.IdBolsa								= recDetalle.IdBolsa;						//		mov.IdBolsa;	
					ObjEntrada.IdUsuario							= in.p_IdUsuario;							//		mov.IdUsuario;
					ObjEntrada.Glosa								= recDetalle.Glosa;							//		mov.Glosa;
					ObjEntrada.IdInstrumento						= recDetalle.IdInstrumentoSubyacente;		//		mov.IdInstrumento;
					ObjEntrada.IdMonedaInstrumento					= recDetalle.IdMoneda;						//	*	mov.IdMonedaInstrumento;
					ObjEntrada.IdTipoReajusteInstrumento			= recDetalle.IdTipoReajuste;				//		mov.IdTipoReajuste;
					ObjEntrada.IdEmisor								= recDetalle.IdEmisor;						//		mov.IdEmisor;
					ObjEntrada.EsFungible							= recDetalle.EsFungible;					//		mov.EsFungible;
					ObjEntrada.IdBoveda								= recDetalle.IdBoveda;						//		mov.IdBoveda;
					ObjEntrada.EsNominal							= recDetalle.EsNominal;						//		mov.EsNominal;			
					ObjEntrada.Cantidad								= recDetalle.Cantidad;						//		mov.Cantidad; 
					ObjEntrada.CorteMinimo							= recDetalle.CorteMinimo; 
					ObjEntrada.ValorRescate							= recDetalle.ValorRescate;					//		mov.ValorRescate; 
					ObjEntrada.PrecioCompra							= recDetalle.Precio;						//		mov.PrecioCompra; 
					ObjEntrada.TasaCompra							= recDetalle.Tasa;							//	*	mov.TasaCompra;
					ObjEntrada.MontoCompra							= recDetalle.Monto;							// 	*	mov.MontoCompra;
					ObjEntrada.IdCarga                              = idCarga;
					//ObjEntrada.IdMovimientoCarteraEnTransitoOrigen= mov.IdMovimientoCarteraEnTransito;			
					//ObjEntrada.IdTipoCanalIngreso					= mov.IdTipoCanalIngreso; 		
					//ObjEntrada.CodigoIFRS							= mov.CodigoIFRS;
					//ObjEntrada.IdTipoComprobante					= mov.IdTipoComprobante; 
					//ObjEntrada.FolioComprobante						= mov.FolioComprobante;
					//ObjEntrada.FolioExternoReferencia				= mov.FolioExternoReferencia;
					//ObjEntrada.IdSucursal							= mov.IdSucursal; 
					//ObjEntrada.IdAgente								= mov.IdAgente;
		
					for (RetiroCustodia.RetiroCustodiaSalida.ObjDatosSalida recorreRetiros: resultadoRepRetiro.P_OUT_DATOSSALIDA)
					{						
						if (recDetalle.IdCargaSimultaneasSebra.equals(recorreRetiros.IdGenerarMovimientoCustodia))
						{
							ObjEntrada.FolioCarteraRelacionada = recorreRetiros.FolioCustodia;
							break;
						}
					}
					registroRepIngresoEnc.add(ObjEntrada);
				}
			}//mientras
		
				System.out.println("Objeto ENCABEZADO: ");
				System.out.println(registroRepIngresoEnc);

				
			if (registroRepIngresoEnc.size() > 0)
			{
				IngresoCustodia ingresoCustodia = new IngresoCustodia();
				IngresoCustodia.IngresoCustodiaEntrada paramInSPCabecera = new IngresoCustodia.IngresoCustodiaEntrada();
				paramInSPCabecera.p_IdEmpresa			= in.p_IdEmpresa;		
				paramInSPCabecera.p_IdModulo			= in.p_IdModulo;
				paramInSPCabecera.p_SimularOperacion	= "N";
				paramInSPCabecera.p_TicketAtencion 		= 0L;
				paramInSPCabecera.p_DatosEntrada		= registroRepIngresoEnc;				
				IngresoCustodia.IngresoCustodiaSalida resultRepIngresoEnc = ingresoCustodia.logica(conn, paramInSPCabecera);
				
				System.out.println("Resultado ENCABEZADO: ");
				System.out.println(resultRepIngresoEnc);
		
				if (resultRepIngresoEnc.P_OUT_MENSAJES.size() > 0)
				{
					if (!resultRepIngresoEnc.P_OUT_ERRORESCONTADOR.equals(0L)){
						for (MensajeSalida recorreErr: resultRepIngresoEnc.P_OUT_MENSAJES){
							out.P_OUT_MENSAJES.add(new MensajeSalida(recorreErr.id ,MensajeSalida.MENSAJE_ERROR, "UI_INGRESO_REPORTE_ERROR", new HashMap<>() ,conn,in.p_IdEmpresa));
							out.P_OUT_ERRORESCONTADOR = 1L;
							return out;
						}
					}
					out.P_OUT_MENSAJES.add(new MensajeSalida(1L ,MensajeSalida.MENSAJE_INFORMACION, "UI_INGRESO_REPORTE_MASIVO_OK", new HashMap<>() ,conn,in.p_IdEmpresa));
					
					out.P_OUT_ERRORESCONTADOR = 0L;
				}
//3.0 INGRESO CABECERA 	-- FIN
						
						
//4.0 INGRESO SUBYACENTE
				for (DetalleCarga recDetalle: rsDetalle)
				{	
					IngresoCustodia.ObjDatosEntrada	ObjEntrada = new IngresoCustodia.ObjDatosEntrada();
					ObjEntrada.IdGenerarMovimientoCustodia			= recDetalle.IdCargaSimultaneasSebra;
					ObjEntrada.TablaAfectada						= "SUBYACENTE";
					ObjEntrada.AccionOperacion						= "INGRESO";
					ObjEntrada.EsRegistroNuevo						= "S";	
					ObjEntrada.IdCliente							= recDetalle.IdCliente;						//		mov2.IdCliente; 
					ObjEntrada.IdPortafolio							= recDetalle.IdPortafolio;					//		mov2.IdPortafolio;
					ObjEntrada.IdCuentaCustodioPortafolio			= recDetalle.IdCuentaCustodioPortafolio;	//		mov2.IdCuentaCustodioPortafolio;
					ObjEntrada.FechaOperacion						= Fecha.toDate(recDetalle.FechaOperacion);	//		mov2.FechaOperacion;
					ObjEntrada.FechaVencimientoOperacion			= Fecha.toDate(recDetalle.FechaVencimiento);//	*	mov2.FechaVencimientoOperacion;
					ObjEntrada.PlazoEnDiasOperacion					= recDetalle.Plazo;							//		mov2.PlazoEnDiasOperacion;
					ObjEntrada.IdTipoOperacion						= recDetalle.IdTipoOperacion;				//		mov2.IdTipoOperacion;		
					ObjEntrada.IdConcepto							= recDetalle.IdConcepto;					//		mov2.IdConcepto;
					ObjEntrada.IdCondicionLiquidacion				= recDetalle.IdCondicionLiquidacion;		//		mov2.IdCondicionLiquidacion; 
					ObjEntrada.FechaLiquidacion						= Fecha.toDate(recDetalle.FechaLiquidacion);//		mov2.FechaLiquidacion;
					ObjEntrada.IdBolsa								= recDetalle.IdBolsa;						//		mov2.IdBolsa;	
					ObjEntrada.IdUsuario							= in.p_IdUsuario;							//		mov2.IdUsuario;
					ObjEntrada.Glosa								= recDetalle.Glosa;							//		mov2.Glosa;
					ObjEntrada.IdInstrumento						= recDetalle.IdInstrumento;					//		mov2.IdInstrumento;
					ObjEntrada.IdMonedaInstrumento					= recDetalle.IdMoneda;						//	*	mov2.IdMonedaInstrumento;
					ObjEntrada.IdTipoReajusteInstrumento			= recDetalle.IdTipoReajuste;				//		mov2.IdTipoReajuste;
					ObjEntrada.IdEmisor								= recDetalle.IdEmisor;						//		mov2.IdEmisor;
					ObjEntrada.EsFungible							= recDetalle.EsFungible;					//		mov2.EsFungible;
					ObjEntrada.IdBoveda								= recDetalle.IdBoveda;						//		mov2.IdBoveda;
					ObjEntrada.EsNominal							= recDetalle.EsNominal;						//		mov2.EsNominal;			
					ObjEntrada.Cantidad								= recDetalle.Cantidad;						//		mov2.Cantidad; 
					ObjEntrada.CorteMinimo							= recDetalle.CorteMinimo;					//		mov2.CorteMinimo; 
					ObjEntrada.ValorRescate							= recDetalle.ValorRescate;					//		mov2.ValorRescate; 
					ObjEntrada.PrecioCompra							= recDetalle.Precio;						//	*	mov2.PrecioCompra; 
					ObjEntrada.TasaCompra							= recDetalle.Tasa;							//	*	mov2.TasaCompra;
					ObjEntrada.MontoCompra							= recDetalle.Monto;							//	*	mov2.MontoCompra;
					ObjEntrada.IdCarga                              = idCarga;
					//ObjEntrada.MontoCompraMonedaInstrumento			= mov2.MontoCompraMonedaInstrumento;
					//ObjEntrada.IdMovimientoCarteraEnTransitoOrigen = mov.IdMovimientoCarteraEnTransito;					
					//ObjEntrada.IdTipoCanalIngreso					= mov2.IdTipoCanalIngreso; 		
					//ObjEntrada.CodigoIFRS							= mov2.CodigoIFRS;
					//ObjEntrada.IdTipoComprobante					= mov2.IdTipoComprobante; 
					//ObjEntrada.FolioComprobante					= mov2.FolioComprobante;
					//ObjEntrada.FolioExternoReferencia				= mov2.FolioExternoReferencia;
					//ObjEntrada.IdSucursal							= mov2.IdSucursal; 
					//ObjEntrada.IdAgente							= mov2.IdAgente;							
							
					for (IngresoCustodia.IngresoCustodiaSalida.ObjDatosSalida recorreIngresos: resultRepIngresoEnc.P_OUT_DATOSSALIDA)
					{						
						if (recDetalle.IdCargaSimultaneasSebra.equals(recorreIngresos.IdGenerarMovimientoCustodia))
						{
							ObjEntrada.FolioCarteraRelacionada	= recorreIngresos.FolioCustodia;
							break;
						}
					}
							
					for (RetiroCustodia.RetiroCustodiaSalida.ObjDatosSalida recorreRetiros: resultadoRepRetiroRes.P_OUT_DATOSSALIDA)
					{						
						if (recDetalle.IdCargaSimultaneasSebra.equals(recorreRetiros.IdGenerarMovimientoCustodia))
						{
							ObjEntrada.FolioCarteraOrigen = recorreRetiros.FolioCustodia;
							ObjEntrada.IdCustodia = recorreRetiros.IdCustodia;
							ObjEntrada.IdCarteraOrigen = recorreRetiros.IdCustodia;
							ObjEntrada.FolioCustodia = recorreRetiros.FolioCustodia;
							break;
						}
					}
		
					registroRepIngresoSub.add(ObjEntrada);
							
				}
				
				
			//}//mientras
			
			System.out.println("Objeto SUBYACENTE: ");
			System.out.println(registroRepIngresoSub);

			
				if (registroRepIngresoSub.size() > 0)
				{
					IngresoCustodia ingresoCustodiaSubyacente = new IngresoCustodia();
					IngresoCustodia.IngresoCustodiaEntrada paramInSPSubyacente = new IngresoCustodia.IngresoCustodiaEntrada();
					paramInSPSubyacente.p_IdEmpresa			= in.p_IdEmpresa;		
					paramInSPSubyacente.p_IdModulo			= in.p_IdModulo;
					paramInSPSubyacente.p_SimularOperacion	= "N";
					paramInSPSubyacente.p_TicketAtencion 		= 0L;
					paramInSPSubyacente.p_DatosEntrada		= registroRepIngresoSub;				
					IngresoCustodia.IngresoCustodiaSalida resultadoRepIngresoSub = ingresoCustodiaSubyacente.logica(conn, paramInSPSubyacente);
					
					System.out.println("Resultado INGRESO SUBYACENTE: ");
					System.out.println(resultadoRepIngresoSub);
						
					if (resultadoRepIngresoSub.P_OUT_MENSAJES.size() > 0)
					{
						if (!resultadoRepIngresoSub.P_OUT_ERRORESCONTADOR.equals(0L))
						{
							for (MensajeSalida recorreErr: resultadoRepIngresoSub.P_OUT_MENSAJES)
							{
								out.P_OUT_MENSAJES.add(new MensajeSalida(recorreErr.id ,MensajeSalida.MENSAJE_ERROR, "UI_INGRESO_REPORTE_ERROR", new HashMap<>() ,conn,in.p_IdEmpresa));
								out.P_OUT_ERRORESCONTADOR = 1L;
								return out;
							}
						}
						out.P_OUT_MENSAJES.add(new MensajeSalida(1L ,MensajeSalida.MENSAJE_INFORMACION, "UI_INGRESO_REPORTE_MASIVO_OK", new HashMap<>() ,conn,in.p_IdEmpresa));								
						out.P_OUT_ERRORESCONTADOR = 0L;
						
					}
				}
			}
			//	}				
			//}
			
//******************************************************************************************************************************************************************************				
//*********************************************************************************		COMPRAS 		****************************************************************************
//******************************************************************************************************************************************************************************

//1.0 INGRESO DE CARTERA 	-- INICIO		
			
			for (DetalleCarga recDetalle: rsDetalle)
			{
				if (recDetalle.IdTipoOperacion.equals("COMSIM")) 
				{
					IngresoCustodia.ObjDatosEntrada	ObjEntrada = new IngresoCustodia.ObjDatosEntrada();
					ObjEntrada.IdGenerarMovimientoCustodia			= recDetalle.IdCargaSimultaneasSebra;
					ObjEntrada.TablaAfectada						= "CARTERA";
					ObjEntrada.AccionOperacion						= "INGRESO";
					ObjEntrada.EsRegistroNuevo						= "S";
					ObjEntrada.IdCliente							= recDetalle.IdCliente;										//		recDetalle.IdCliente; 
					ObjEntrada.IdPortafolio							= recDetalle.IdPortafolio;									//		mov2.IdPortafolio;
					ObjEntrada.FechaOperacion						= Fecha.toDate(recDetalle.FechaOperacion);					//		mov2.FechaOperacion;
					ObjEntrada.IdTipoOperacion						= recDetalle.IdTipoOperacion;								//		mov2.IdTipoOperacion;		
					ObjEntrada.IdConcepto							= recDetalle.IdConcepto;									//		mov2.IdConcepto;
					ObjEntrada.IdCondicionLiquidacion				= recDetalle.IdCondicionLiquidacion;						//		mov2.IdCondicionLiquidacion; 
					ObjEntrada.FechaLiquidacion						= Fecha.toDate(recDetalle.FechaLiquidacion);				//		mov2.FechaLiquidacion;
					ObjEntrada.IdUsuario							= in.p_IdUsuario;											//		mov2.IdUsuario;
					ObjEntrada.Glosa								= recDetalle.Glosa;											//		mov2.Glosa;
					ObjEntrada.IdInstrumento						= recDetalle.IdInstrumento;									//		mov2.IdInstrumento;
					ObjEntrada.IdMonedaInstrumento					= recDetalle.IdMoneda;										//		mov2.IdMonedaInstrumento;
					ObjEntrada.IdTipoReajusteInstrumento			= recDetalle.IdTipoReajuste;								//		mov2.IdTipoReajuste;
					ObjEntrada.IdEmisor								= recDetalle.IdEmisor;										//		mov2.IdEmisor;
					ObjEntrada.EsFungible							= "N";//recDetalle.EsFungible;								//		mov2.EsFungible;
					ObjEntrada.IdBoveda								= recDetalle.IdBoveda;										//		mov2.IdBoveda;
					ObjEntrada.EsNominal							= recDetalle.EsNominal;										//		mov2.EsNominal;			
					ObjEntrada.Cantidad								= recDetalle.Cantidad;										//		mov2.Cantidad; 
					ObjEntrada.CorteMinimo							= recDetalle.CorteMinimo;									//		mov2.CorteMinimo; 
					ObjEntrada.ValorRescate							= recDetalle.ValorRescate;									//		mov2.ValorRescate; 
					ObjEntrada.PrecioCompra							= recDetalle.Precio;										//	*	mov2.PrecioCompra; 
					ObjEntrada.TasaCompra							= recDetalle.Tasa;											//		mov2.TasaCompra;
					ObjEntrada.MontoCompraMonedaInstrumento			= recDetalle.Monto;											//	*	mov2.MontoCompraMonedaInstrumento;
					ObjEntrada.IdCarga                              = idCarga;
					//ObjEntrada.IdMovimientoCarteraEnTransitoOrigen 	= mov.IdMovimientoCarteraEnTransito;					
					//ObjEntrada.IdTipoCanalIngreso					= mov2.IdTipoCanalIngreso; 		
					//ObjEntrada.IdTipoComprobante					= mov2.IdTipoComprobante; 
					//ObjEntrada.FolioComprobante					= mov2.FolioComprobante;
					//ObjEntrada.FolioExternoReferencia				= mov2.FolioExternoReferencia;
					//ObjEntrada.IdSucursal							= mov2.IdSucursal; 
					//ObjEntrada.IdAgente							= mov2.IdAgente;
			
					registroRepIngreso.add(ObjEntrada);								
				}
			}
			
			System.out.println("Objeto INGRESO: ");
			System.out.println(registroRepIngreso);

			
			if (registroRepIngreso.size() > 0)
			{
				IngresoCustodia ingresoCustodia = new IngresoCustodia();
				IngresoCustodia.IngresoCustodiaEntrada paramInSPCabecera = new IngresoCustodia.IngresoCustodiaEntrada();
				paramInSPCabecera.p_IdEmpresa			= in.p_IdEmpresa;		
				paramInSPCabecera.p_IdModulo			= in.p_IdModulo;
				paramInSPCabecera.p_SimularOperacion	= "N";
				paramInSPCabecera.p_TicketAtencion 		= 0L;
				paramInSPCabecera.p_DatosEntrada		= registroRepIngreso;				
				IngresoCustodia.IngresoCustodiaSalida resultadoRepIngreso = ingresoCustodia.logica(conn, paramInSPCabecera);
				
				resultadoRepIngresoRes.P_OUT_DATOSSALIDA.addAll(resultadoRepIngreso.P_OUT_DATOSSALIDA);
				
				
				System.out.println("Resultado INGRESO: ");
				System.out.println(resultadoRepIngreso);
			//}
//1.0 INGRESO DE CARTERA 	-- FIN
			
//2.0 INGRESO CABECERA 	-- INICIO				
				for (DetalleCarga recDetalle: rsDetalle)
				{					
					IngresoCustodia.ObjDatosEntrada	ObjEntrada = new IngresoCustodia.ObjDatosEntrada();
					ObjEntrada.IdGenerarMovimientoCustodia			= recDetalle.IdCargaSimultaneasSebra;
					ObjEntrada.TablaAfectada						= "CARTERA";
					ObjEntrada.AccionOperacion						= "INGRESO";
					ObjEntrada.EsRegistroNuevo						= "S";
					ObjEntrada.IdCliente							= recDetalle.IdCliente;						//		mov.IdCliente; 
					ObjEntrada.IdPortafolio							= recDetalle.IdPortafolio;					//		mov.IdPortafolio;
					ObjEntrada.IdCuentaCustodioPortafolio			= recDetalle.IdCuentaCustodioPortafolio;	//		mov.IdCuentaCustodioPortafolio;
					ObjEntrada.FechaOperacion						= Fecha.toDate(recDetalle.FechaOperacion);	//		mov.FechaOperacion;
					ObjEntrada.FechaVencimientoOperacion			= Fecha.toDate(recDetalle.FechaVencimiento);//	*	mov.FechaVencimientoOperacion;
					ObjEntrada.PlazoEnDiasOperacion					= recDetalle.Plazo;							//	*	mov.PlazoEnDiasOperacion;
					ObjEntrada.IdTipoOperacion						= recDetalle.IdTipoOperacion;				//		mov.IdTipoOperacion;		
					ObjEntrada.IdConcepto							= recDetalle.IdConcepto;					//		mov.IdConcepto;
					ObjEntrada.IdCondicionLiquidacion				= recDetalle.IdCondicionLiquidacion;		//		mov.IdCondicionLiquidacion; 
					ObjEntrada.FechaLiquidacion						= Fecha.toDate(recDetalle.FechaLiquidacion);//		mov.FechaLiquidacion;
					ObjEntrada.IdBolsa								= recDetalle.IdBolsa;						//		mov.IdBolsa;	
					ObjEntrada.IdUsuario							= in.p_IdUsuario;							//		mov.IdUsuario;
					ObjEntrada.Glosa								= recDetalle.Glosa;							//		mov.Glosa;
					ObjEntrada.IdInstrumento						= recDetalle.IdInstrumentoSubyacente;		//		mov.IdInstrumento;
					ObjEntrada.IdMonedaInstrumento					= recDetalle.IdMoneda;						//	*	mov.IdMonedaInstrumento;
					ObjEntrada.IdTipoReajusteInstrumento			= recDetalle.IdTipoReajuste;				//		mov.IdTipoReajuste;
					ObjEntrada.IdEmisor								= recDetalle.IdEmisor;						//		mov.IdEmisor;
					ObjEntrada.EsFungible							= recDetalle.EsFungible;					//		mov.EsFungible;
					ObjEntrada.IdBoveda								= recDetalle.IdBoveda;						//		mov.IdBoveda;
					ObjEntrada.EsNominal							= recDetalle.EsNominal;						//		mov.EsNominal;			
					ObjEntrada.Cantidad								= recDetalle.Cantidad;						//		mov.Cantidad; 
					ObjEntrada.CorteMinimo							= recDetalle.CorteMinimo; 
					ObjEntrada.ValorRescate							= recDetalle.ValorRescate;					//		mov.ValorRescate; 
					ObjEntrada.PrecioCompra							= recDetalle.Precio;						//		mov.PrecioCompra; 
					ObjEntrada.TasaCompra							= recDetalle.Tasa;							//	*	mov.TasaCompra;
					ObjEntrada.MontoCompra							= recDetalle.Monto;							// 	*	mov.MontoCompra;
					ObjEntrada.IdCarga                              = idCarga;
					//ObjEntrada.IdMovimientoCarteraEnTransitoOrigen= mov.IdMovimientoCarteraEnTransito;			
					//ObjEntrada.IdTipoCanalIngreso					= mov.IdTipoCanalIngreso; 		
					//ObjEntrada.CodigoIFRS							= mov.CodigoIFRS;
					//ObjEntrada.IdTipoComprobante					= mov.IdTipoComprobante; 
					//ObjEntrada.FolioComprobante						= mov.FolioComprobante;
					//ObjEntrada.FolioExternoReferencia				= mov.FolioExternoReferencia;
					//ObjEntrada.IdSucursal							= mov.IdSucursal; 
					//ObjEntrada.IdAgente								= mov.IdAgente;
		
					registroRepIngresoEnc.add(ObjEntrada);
				}
			}

			System.out.println("Objeto ENCABEZADO: ");
			System.out.println(registroRepIngresoEnc);
			
			if (registroRepIngresoEnc.size() > 0)
			{
				IngresoCustodia ingresoCustodia = new IngresoCustodia();
				IngresoCustodia.IngresoCustodiaEntrada paramInSPCabecera = new IngresoCustodia.IngresoCustodiaEntrada();
				paramInSPCabecera.p_IdEmpresa			= in.p_IdEmpresa;		
				paramInSPCabecera.p_IdModulo			= in.p_IdModulo;
				paramInSPCabecera.p_SimularOperacion	= "N";
				paramInSPCabecera.p_TicketAtencion 		= 0L;
				paramInSPCabecera.p_DatosEntrada		= registroRepIngresoEnc;				
				IngresoCustodia.IngresoCustodiaSalida resultadoRepIngresoEnc = ingresoCustodia.logica(conn, paramInSPCabecera);
				
				System.out.println("Resultado ENCABEZADO: ");
				System.out.println(resultadoRepIngresoEnc);
		
				if (resultadoRepIngresoEnc.P_OUT_MENSAJES.size() > 0)
				{
					if (!resultadoRepIngresoEnc.P_OUT_ERRORESCONTADOR.equals(0L)){
						for (MensajeSalida recorreErr: resultadoRepIngresoEnc.P_OUT_MENSAJES){
							out.P_OUT_MENSAJES.add(new MensajeSalida(recorreErr.id ,MensajeSalida.MENSAJE_ERROR, "UI_INGRESO_REPORTE_ERROR", new HashMap<>() ,conn,in.p_IdEmpresa));
							out.P_OUT_ERRORESCONTADOR = 1L;
							return out;
						}
					}
					out.P_OUT_MENSAJES.add(new MensajeSalida(1L ,MensajeSalida.MENSAJE_INFORMACION, "UI_INGRESO_REPORTE_MASIVO_OK", new HashMap<>() ,conn,in.p_IdEmpresa));
					
					out.P_OUT_ERRORESCONTADOR = 0L;
				}
//2.0 INGRESO CABECERA -- FIN
				
//3.0 INGRESO SUBYACENTE -- INICIO
				for (DetalleCarga recDetalle: rsDetalle)
				{	
					IngresoCustodia.ObjDatosEntrada	ObjEntrada = new IngresoCustodia.ObjDatosEntrada();
					ObjEntrada.IdGenerarMovimientoCustodia			= recDetalle.IdCargaSimultaneasSebra;
					ObjEntrada.TablaAfectada						= "SUBYACENTE";
					ObjEntrada.AccionOperacion						= "INGRESO";
					ObjEntrada.EsRegistroNuevo						= "S";	
					ObjEntrada.IdCliente							= recDetalle.IdCliente;						//		mov2.IdCliente; 
					ObjEntrada.IdPortafolio							= recDetalle.IdPortafolio;					//		mov2.IdPortafolio;
					ObjEntrada.IdCuentaCustodioPortafolio			= recDetalle.IdCuentaCustodioPortafolio;	//		mov2.IdCuentaCustodioPortafolio;
					ObjEntrada.FechaOperacion						= Fecha.toDate(recDetalle.FechaOperacion);	//		mov2.FechaOperacion;
					ObjEntrada.FechaVencimientoOperacion			= Fecha.toDate(recDetalle.FechaVencimiento);//	*	mov2.FechaVencimientoOperacion;
					ObjEntrada.PlazoEnDiasOperacion					= recDetalle.Plazo;							//		mov2.PlazoEnDiasOperacion;
					ObjEntrada.IdTipoOperacion						= recDetalle.IdTipoOperacion;				//		mov2.IdTipoOperacion;		
					ObjEntrada.IdConcepto							= recDetalle.IdConcepto;					//		mov2.IdConcepto;
					ObjEntrada.IdCondicionLiquidacion				= recDetalle.IdCondicionLiquidacion;		//		mov2.IdCondicionLiquidacion; 
					ObjEntrada.FechaLiquidacion						= Fecha.toDate(recDetalle.FechaLiquidacion);//		mov2.FechaLiquidacion;
					ObjEntrada.IdBolsa								= recDetalle.IdBolsa;						//		mov2.IdBolsa;	
					ObjEntrada.IdUsuario							= in.p_IdUsuario;							//		mov2.IdUsuario;
					ObjEntrada.Glosa								= recDetalle.Glosa;							//		mov2.Glosa;
					ObjEntrada.IdInstrumento						= recDetalle.IdInstrumento;					//		mov2.IdInstrumento;
					ObjEntrada.IdMonedaInstrumento					= recDetalle.IdMoneda;						//	*	mov2.IdMonedaInstrumento;
					ObjEntrada.IdTipoReajusteInstrumento			= recDetalle.IdTipoReajuste;				//		mov2.IdTipoReajuste;
					ObjEntrada.IdEmisor								= recDetalle.IdEmisor;						//		mov2.IdEmisor;
					ObjEntrada.EsFungible							= recDetalle.EsFungible;					//		mov2.EsFungible;
					ObjEntrada.IdBoveda								= recDetalle.IdBoveda;						//		mov2.IdBoveda;
					ObjEntrada.EsNominal							= recDetalle.EsNominal;						//		mov2.EsNominal;			
					ObjEntrada.Cantidad								= recDetalle.Cantidad;						//		mov2.Cantidad; 
					ObjEntrada.CorteMinimo							= recDetalle.CorteMinimo;					//		mov2.CorteMinimo; 
					ObjEntrada.ValorRescate							= recDetalle.ValorRescate;					//		mov2.ValorRescate; 
					ObjEntrada.PrecioCompra							= recDetalle.Precio;						//	*	mov2.PrecioCompra; 
					ObjEntrada.TasaCompra							= recDetalle.Tasa;							//	*	mov2.TasaCompra;
					ObjEntrada.MontoCompra							= recDetalle.Monto;							//	*	mov2.MontoCompra;
					ObjEntrada.IdCarga                              = idCarga;
					//ObjEntrada.MontoCompraMonedaInstrumento			= mov2.MontoCompraMonedaInstrumento;
					//ObjEntrada.IdMovimientoCarteraEnTransitoOrigen = mov.IdMovimientoCarteraEnTransito;					
					//ObjEntrada.IdTipoCanalIngreso					= mov2.IdTipoCanalIngreso; 		
					//ObjEntrada.CodigoIFRS							= mov2.CodigoIFRS;
					//ObjEntrada.IdTipoComprobante					= mov2.IdTipoComprobante; 
					//ObjEntrada.FolioComprobante					= mov2.FolioComprobante;
					//ObjEntrada.FolioExternoReferencia				= mov2.FolioExternoReferencia;
					//ObjEntrada.IdSucursal							= mov2.IdSucursal; 
					//ObjEntrada.IdAgente							= mov2.IdAgente;							
							
					for (IngresoCustodia.IngresoCustodiaSalida.ObjDatosSalida recorreIngresos: resultadoRepIngresoEnc.P_OUT_DATOSSALIDA)
					{						
						if (recDetalle.IdCargaSimultaneasSebra.equals(recorreIngresos.IdGenerarMovimientoCustodia))
						{
							ObjEntrada.FolioCarteraRelacionada	= recorreIngresos.FolioCustodia;
// CONSULTAR
							ObjEntrada.FolioCarteraOrigen = recorreIngresos.FolioCustodia;
							ObjEntrada.IdCustodia = recorreIngresos.IdCustodia;
							ObjEntrada.IdCarteraOrigen = recorreIngresos.IdCustodia;
							ObjEntrada.FolioCustodia = recorreIngresos.FolioCustodia;
							
							break;
						}
					}
							
		
					registroRepIngresoSub.add(ObjEntrada);
							
				}	
					
			}
			
			System.out.println("Objeto SUBYACENTE: ");
			System.out.println(registroRepIngresoSub);

//3.0 INGRESO SUBYACENTE -- FIN

//4.0 RETIRO CARTERA -- INICIO			
			if (registroRepIngresoSub.size() > 0)
			{
				IngresoCustodia ingresoCustodiaSubyacente = new IngresoCustodia();
				IngresoCustodia.IngresoCustodiaEntrada paramInSPSubyacente = new IngresoCustodia.IngresoCustodiaEntrada();
				paramInSPSubyacente.p_IdEmpresa			= in.p_IdEmpresa;		
				paramInSPSubyacente.p_IdModulo			= in.p_IdModulo;
				paramInSPSubyacente.p_SimularOperacion	= "N";
				paramInSPSubyacente.p_TicketAtencion 		= 0L;
				paramInSPSubyacente.p_DatosEntrada		= registroRepIngresoSub;				
				IngresoCustodia.IngresoCustodiaSalida resultadoRepIngresoSub = ingresoCustodiaSubyacente.logica(conn, paramInSPSubyacente);
				
				System.out.println("Resultado INGRESO SUBYACENTE: ");
				System.out.println(resultadoRepIngresoSub);
					
				if (resultadoRepIngresoSub.P_OUT_MENSAJES.size() > 0)
				{
					if (!resultadoRepIngresoSub.P_OUT_ERRORESCONTADOR.equals(0L))
					{
						for (MensajeSalida recorreErr: resultadoRepIngresoSub.P_OUT_MENSAJES)
						{
							out.P_OUT_MENSAJES.add(new MensajeSalida(recorreErr.id ,MensajeSalida.MENSAJE_ERROR, "UI_INGRESO_REPORTE_ERROR", new HashMap<>() ,conn,in.p_IdEmpresa));
							out.P_OUT_ERRORESCONTADOR = 1L;
							return out;
						}
					}
				out.P_OUT_MENSAJES.add(new MensajeSalida(1L ,MensajeSalida.MENSAJE_INFORMACION, "UI_INGRESO_REPORTE_MASIVO_OK", new HashMap<>() ,conn,in.p_IdEmpresa));								
				out.P_OUT_ERRORESCONTADOR = 0L;
			}//new		
//2.0 RETIRO DE CARTERA 	-- INICIO
				for (DetalleCarga recDetalleRet: rsDetalle)
				{
					RetiroCustodia.ObjDatosEntrada	ObjEntradaRet = new RetiroCustodia.ObjDatosEntrada();
					ObjEntradaRet.IdGenerarMovimientoCustodia		= recDetalleRet.IdCargaSimultaneasSebra;
					ObjEntradaRet.TablaAfectada						= "CARTERA";
					ObjEntradaRet.AccionOperacion					= "RETIRO";
					ObjEntradaRet.IdCliente							= recDetalleRet.IdCliente;									//		recDetalle.IdCliente; 
					ObjEntradaRet.IdPortafolio						= recDetalleRet.IdPortafolio;								//		mov2.IdPortafolio;
					ObjEntradaRet.FechaOperacion					= Fecha.toDate(recDetalleRet.FechaOperacion);				//		mov2.FechaOperacion;
					ObjEntradaRet.IdTipoOperacion					= recDetalleRet.IdTipoOperacion;							//		mov2.IdTipoOperacion;		
					ObjEntradaRet.IdConcepto						= recDetalleRet.IdConcepto;									//		mov2.IdConcepto;
					ObjEntradaRet.IdCondicionLiquidacion			= recDetalleRet.IdCondicionLiquidacion;						//		mov2.IdCondicionLiquidacion; 
					ObjEntradaRet.FechaLiquidacion					= Fecha.toDate(recDetalleRet.FechaLiquidacion);				//		mov2.FechaLiquidacion;
					ObjEntradaRet.IdUsuario							= in.p_IdUsuario;											//		mov2.IdUsuario;
					ObjEntradaRet.Glosa								= recDetalleRet.Glosa;										//		mov2.Glosa;
					ObjEntradaRet.IdInstrumento						= recDetalleRet.IdInstrumento;								//		mov2.IdInstrumento;
					ObjEntradaRet.IdMonedaInstrumento				= recDetalleRet.IdMoneda;										//		mov2.IdMonedaInstrumento;
					ObjEntradaRet.IdTipoReajusteInstrumento			= recDetalleRet.IdTipoReajuste;								//		mov2.IdTipoReajuste;
					ObjEntradaRet.IdEmisor							= recDetalleRet.IdEmisor;									//		mov2.IdEmisor;
					ObjEntradaRet.EsFungible						= "N";//recDetalle.EsFungible;								//		mov2.EsFungible;
					ObjEntradaRet.IdBoveda							= recDetalleRet.IdBoveda;									//		mov2.IdBoveda;
					ObjEntradaRet.EsNominal							= recDetalleRet.EsNominal;									//		mov2.EsNominal;			
					ObjEntradaRet.Cantidad							= recDetalleRet.Cantidad;									//		mov2.Cantidad; 
					ObjEntradaRet.CorteMinimo						= recDetalleRet.CorteMinimo;								//		mov2.CorteMinimo; 
					ObjEntradaRet.ValorRescate						= recDetalleRet.ValorRescate;								//		mov2.ValorRescate; 
					ObjEntradaRet.PrecioCompra						= recDetalleRet.Precio;										//	*	mov2.PrecioCompra; 
					ObjEntradaRet.TasaCompra						= recDetalleRet.Tasa;										//		mov2.TasaCompra;
					ObjEntradaRet.MontoCompraMonedaInstrumento		= recDetalleRet.Monto;		
					ObjEntradaRet.IdCarga  							= idCarga;
					//	*	mov2.MontoCompraMonedaInstrumento;
					//ObjEntrada.IdMovimientoCarteraEnTransitoOrigen= mov.IdMovimientoCarteraEnTransito;			
					//ObjEntrada.IdTipoCanalIngreso					= mov2.IdTipoCanalIngreso; 		
					//ObjEntrada.IdTipoComprobante					= mov2.IdTipoComprobante; 
					//ObjEntrada.FolioComprobante					= mov2.FolioComprobante;
					//ObjEntrada.FolioExternoReferencia				= mov2.FolioExternoReferencia;
					//ObjEntrada.IdSucursal							= mov2.IdSucursal; 
					//ObjEntrada.IdAgente							= mov2.IdAgente;
					
							
					for (IngresoCustodia.IngresoCustodiaSalida.ObjDatosSalida recorreIngresos: resultadoRepIngresoRes.P_OUT_DATOSSALIDA)
						{						
							if (recDetalleRet.IdCargaSimultaneasSebra.equals(recorreIngresos.IdGenerarMovimientoCustodia))
							{
								ObjEntradaRet.FolioCustodia 	= recorreIngresos.FolioCustodia;
								ObjEntradaRet.IdCustodia		= recorreIngresos.IdCustodia;						
								break;
							}						
						}
						registroRepRetiro.add(ObjEntradaRet);					
				}
						
						System.out.println("Objeto RETIRO: ");
						System.out.println(registroRepRetiro);
			}		
			if (registroRepRetiro.size() > 0)
			{
				RetiroCustodia retirosCustodia = new RetiroCustodia();
				RetiroCustodia.RetiroCustodiaEntrada paramInSP = new RetiroCustodia.RetiroCustodiaEntrada();
				paramInSP.p_IdEmpresa			= in.p_IdEmpresa;		
				paramInSP.p_SimularOperacion	= "N";
				paramInSP.p_TicketAtencion 		= 0L;
				paramInSP.p_DatosEntrada		= registroRepRetiro;				
				RetiroCustodia.RetiroCustodiaSalida resultadoRepRetiro = retirosCustodia.logica(conn, paramInSP);
				
				resultadoRepRetiroRes.P_OUT_DATOSSALIDA.addAll(resultadoRepRetiro.P_OUT_DATOSSALIDA);
						
				if (resultadoRepRetiro.P_OUT_MENSAJES.size() > 0)
				{
					if (!resultadoRepRetiro.P_OUT_ERRORESCONTADOR.equals(0L)){
						for (MensajeSalida recorreErr: resultadoRepRetiro.P_OUT_MENSAJES){
							out.P_OUT_MENSAJES.add(new MensajeSalida(recorreErr.id ,MensajeSalida.MENSAJE_ERROR, "UI_INGRESO_REPORTE_ERROR", new HashMap<>() ,conn,in.p_IdEmpresa));
							out.P_OUT_ERRORESCONTADOR = 1L;
							return out;
						}
					}
					out.P_OUT_MENSAJES.add(new MensajeSalida(1L ,MensajeSalida.MENSAJE_INFORMACION, "UI_INGRESO_REPORTE_MASIVO_OK", new HashMap<>() ,conn,in.p_IdEmpresa));
					
					out.P_OUT_ERRORESCONTADOR = 0L;
				}
						
				System.out.println("Resultado RETIRO: ");
				System.out.println(resultadoRepRetiro);

//5.0 INGRESO GARANTIA -- INICIO
				/*
				for (DetalleCarga recDetalle: rsDetalle)
				{					
					IngresoCustodia.ObjDatosEntrada	ObjEntrada = new IngresoCustodia.ObjDatosEntrada();
					ObjEntrada.TablaAfectada						= "GARANTIA";
					ObjEntrada.AccionOperacion						= "INGRESO";
					ObjEntrada.EsRegistroNuevo						= "S";
					ObjEntrada.IdCliente							= recDetalle.IdCliente;						
					ObjEntrada.IdPortafolio							= recDetalle.IdPortafolio;					
					ObjEntrada.IdCuentaCustodioPortafolio			= recDetalle.IdCuentaCustodioPortafolio;	
					ObjEntrada.FechaOperacion						= Fecha.toDate(recDetalle.FechaOperacion);	
					ObjEntrada.FechaVencimientoOperacion			= Fecha.toDate(recDetalle.FechaVencimiento);
					ObjEntrada.PlazoEnDiasOperacion					= recDetalle.Plazo;							
					ObjEntrada.IdTipoOperacion						= recDetalle.IdTipoOperacion;				
					ObjEntrada.IdCondicionLiquidacion				= recDetalle.IdCondicionLiquidacion;		
					ObjEntrada.FechaLiquidacion						= Fecha.toDate(recDetalle.FechaLiquidacion);
					ObjEntrada.IdBolsa								= recDetalle.IdBolsa;						
					ObjEntrada.IdUsuario							= in.p_IdUsuario;							
					ObjEntrada.Glosa								= recDetalle.Glosa;							
					ObjEntrada.IdInstrumento						= recDetalle.IdInstrumentoSubyacente;		
					ObjEntrada.IdMonedaInstrumento					= "CLP";						
					ObjEntrada.IdTipoReajusteInstrumento			= recDetalle.IdTipoReajuste;				
					ObjEntrada.IdEmisor								= recDetalle.IdEmisor;						
					ObjEntrada.EsFungible							= recDetalle.EsFungible;					
					ObjEntrada.IdBoveda								= recDetalle.IdBoveda;						
					ObjEntrada.EsNominal							= recDetalle.EsNominal;						
					ObjEntrada.Cantidad								= recDetalle.Cantidad;						
					ObjEntrada.CorteMinimo							= recDetalle.CorteMinimo; 
					ObjEntrada.ValorRescate							= recDetalle.ValorRescate;					
					ObjEntrada.PrecioCompra							= recDetalle.Precio;						
					ObjEntrada.TasaCompra							= recDetalle.Tasa;							
					ObjEntrada.MontoCompra							= recDetalle.Monto;
					ObjEntrada.IdConcepto 							= "GARREP";
					ObjEntrada.IdContraparteGarantia 				= 10059L;
					ObjEntrada.IdCarga                              = idCarga;
					
					for (RetiroCustodia.RetiroCustodiaSalida.ObjDatosSalida recorreIngresos: resultadoRepRetiroRes.P_OUT_DATOSSALIDA)
					{						
						if (recDetalle.IdCargaSimultaneasSebra.equals(recorreIngresos.IdGenerarMovimientoCustodia))
						{
							ObjEntrada.FolioCarteraGarantizada 		= recorreIngresos.FolioCustodia;
							ObjEntrada.FolioCarteraOrigen			= recorreIngresos.FolioCustodia;
							ObjEntrada.IdGarantiaOrigen				= recorreIngresos.IdCustodia;
							ObjEntrada.IdGenerarMovimientoCustodia 	= recorreIngresos.IdCustodia;
							ObjEntrada.IdCarteraOrigen				= recorreIngresos.IdCustodia;
							break;
						}						
					}
					detalleGarantiaCustodia.add(ObjEntrada);
				}
				System.out.println("Objeto Garantia: ");
				System.out.println(detalleGarantiaCustodia);
				
				if (detalleGarantiaCustodia.size() > 0)
				{
					IngresoCustodia ingresoCustodia = new IngresoCustodia();
					IngresoCustodia.IngresoCustodiaEntrada paramInSPCabecera = new IngresoCustodia.IngresoCustodiaEntrada();
					paramInSPCabecera.p_IdEmpresa			= in.p_IdEmpresa;		
					paramInSPCabecera.p_IdModulo			= in.p_IdModulo;
					paramInSPCabecera.p_SimularOperacion	= "N";
					paramInSPCabecera.p_TicketAtencion 		= 0L;
					paramInSPCabecera.p_DatosEntrada		= detalleGarantiaCustodia;				
					IngresoCustodia.IngresoCustodiaSalida resultadoGarantia = ingresoCustodia.logica(conn, paramInSPCabecera);
					
					resultadoRepIngresoRes.P_OUT_DATOSSALIDA.addAll(resultadoGarantia.P_OUT_DATOSSALIDA);
					
					
					System.out.println("Resultado INGRESO: ");
					System.out.println(resultadoGarantia);
				} 
*/
				
//5.0 INGRESO GARANTIA -- FIN				
				
				
		}	
			
			//4.0 RETIRO CARTERA -- FIN			
			
//*************************************************************************	FIN	***************************************************************************************						
	}
		
		
			
		catch(Exception e) {
			log.write(e);
			throw e;
		}
		finally {
			//conn.close();
		}
		log.write("TERMINO");
		return out;
		
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getUAT2Connection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":69,\"p_IdModulo\":\"CUS\",\"p_UsaCarterasSistema\":\"N\"}";
			ProcesarCargaMasivaCustodiaReporteEntrada Objin = gson.fromJson(json, ProcesarCargaMasivaCustodiaReporteEntrada.class);
			
			CargaSimultaneasDesdeSebra demo=new CargaSimultaneasDesdeSebra();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				ProcesarCargaMasivaCustodiaReporteSalida ObjOut=demo.logica(con, Objin);
 				con.commit();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
	    	}
		System.out.println("== END ==");
	}
}