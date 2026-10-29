package cl.bcs.custodia;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pau.ValorizarCarteraRF.ObjCarteras;
import cl.bcs.plataforma.pau.ValorizarCarteraPactos;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.custodia.utils.CalcularPrecioPromedio;
import cl.bcs.custodia.utils.ValorizaTasaCompra;
import cl.bcs.custodia.utils.ValorizarCustodia;
import cl.bcs.custodia.utils.ValorizarCustodia.ObjCartera;
import cl.bcs.custodia.utils.ValorizarCustodia.ObjInstrumentoAValorizar;
import cl.bcs.custodia.utils.ValorizarCustodia.ObjUpdateTablas;
import cl.bcs.custodia.utils.ValorizarCustodia.ValorizarCustodiaEntrada;
import cl.bcs.custodia.utils.ValorizarInstrumento.ObjResultadoValorizacion;
import cl.bcs.custodia.utils.ValorizarInstrumento;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ValorizacionValoresAdicionalesBatch implements JavaCall{	
	private static final Logger logger = LogManager.getLogger(ValorizacionValoresAdicionalesBatch.class);

	private final String SQL_SelectCartera = loadResourceToString("cl/bcs/plataforma/pau/sql/PautaNocturna_SEL_Cartera_ValoresAdicionales.sql");
	private final String SQL_SelectCartera2 = loadResourceToString("cl/bcs/custodia/sql/ValorizarCustodia_SEL_Cartera.sql");
	private final String SQL_InsertCartera = loadResourceToString("cl/bcs/custodia/sql/ValorizarCustodia_INS_Cartera.sql");
	private final String SQL_DeleteCartera = loadResourceToString("cl/bcs/custodia/sql/ValorizarCustodia_DEL_Cartera.sql");
	private final String SQL_SelectTipoRegistro = loadResourceToString("cl/bcs/plataforma/pau/sql/ValoresAdicionales_SelectTipoRegistro.sql");
	private final String SQL_InsertValoresAdicionales = loadResourceToString("cl/bcs/plataforma/pau/sql/ValoresAdicionales_Insert.sql");
	private final String SQL_DropValoresAdicionales = loadResourceToString("cl/bcs/plataforma/pau/sql/ValoresAdicionales_DeleteContenidoActual.sql");
	private final String SQL_SelectSubyacente = loadResourceToString("cl/bcs/plataforma/pau/sql/PautaNocturna_SEL_CarteraPactos_Subyacente.sql");
	private final String SQL_DropValoresAdicionalesPactosSubyacentes = loadResourceToString("cl/bcs/plataforma/pau/sql/ValoresAdicionales_DeletePactosSubyacentes.sql");

	public static class InValorizacionValoresAdicionales{
		public Long		p_IdEmpresa;		
		public Long		p_IdUsuario;
		public String	p_IdModulo;
		public String	p_FechaSistema;
		public Long		p_IdPais = -1L;
		public String 	p_Simular = "N";
		public Long 	p_IdPortafolio = 0L;
		public Long		p_IdCliente = 0L;
		public String 	p_ValorizaCarteraParaInterfaces = "N";
		public String 	p_ValorizaPactosSubyacenteTasaCompra = "N";
	}
	public static class outValorizacionInserts{
		public Long		IdCartera;
		public String	Fecha;
		public String	Ejecucion;
		public String 	IdTipoRegistro;
		public String	Valor;
	}
	
	public static class TipoRegistro{
		public Long	IdTipoRegistro;
		public String Valor;
		public Long	IdEmpresa;
	}
	

	public static class dropData{
		public String	TipoRegistro;
		public String	Fecha;
	}
	
	public static class OutValorizacionValoresAdicionales {
		public Long P_OUT_ERRORESCONTADOR;
		public String P_OUT_MENSAJE;
		
		public OutValorizacionValoresAdicionales(){
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJE = "";
		}
	}	
	
	public OutValorizacionValoresAdicionales logica(Connection con, InValorizacionValoresAdicionales in) throws Exception {
		
		String FechaSistema	= in.p_FechaSistema;
		String FechaRealSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa,"PLATAFORMA", "PLA.FechaSistema");
		String Historica = "S";
		if(FechaSistema.equals("") || FechaSistema.equals("0000-00-00")) FechaSistema = FechaRealSistema;
		if (!FechaSistema.equals(FechaRealSistema)) {  Historica = "S" ;} else {Historica = "N" ;} ;
				
		 ValorizacionValoresAdicionalesBatch.OutValorizacionValoresAdicionales out = new  ValorizacionValoresAdicionalesBatch.OutValorizacionValoresAdicionales();
		out.P_OUT_ERRORESCONTADOR = new Long(0);
		out.P_OUT_MENSAJE = "";
		SQL sql=new SQL();
		logger.debug("INICIO::ValorizarCarteraRF");

		List<String> clasificacion = new ArrayList<String>();
		out = logicaValoresAdicionales(con, in, out, sql, clasificacion, Historica);


		return out;
		
	}


	private OutValorizacionValoresAdicionales logicaValoresAdicionales(Connection con, InValorizacionValoresAdicionales in,  ValorizacionValoresAdicionalesBatch.OutValorizacionValoresAdicionales out,
		SQL sql, List<String> clasificacion , String Historica) throws SQLException, Exception {
		logger.debug(" Entrada a logicaValoresAdicionales");
		HashMap<Long,BigDecimal> frecuenciasCarterasHash = new HashMap<>();

		if(in.p_ValorizaCarteraParaInterfaces.equals("S")) { // Valorizacion Interfaces P40
			List<ObjCarteras> carteras = new ArrayList<ObjCarteras>();
			clasificacion.add("RF");
			HashMap<String,Object> paramsCartera = new HashMap<>(4);
			paramsCartera.put("IdEmpresa"	,	in.p_IdEmpresa		);
			paramsCartera.put("Historica"	, 	Historica			);
			paramsCartera.put("Fecha"		, 	in.p_FechaSistema	);
			paramsCartera.put("IdCliente"	, 	in.p_IdCliente		);
			
			carteras = sql.query(con, SQL_SelectCartera, paramsCartera, ObjCarteras.class);			
			
			for(ObjCarteras cartera: carteras) {
				if(cartera.WS_TieneCorteDeCupon.equals("S")) {
					frecuenciasCarterasHash.put(cartera.IdTablaOrigen,new BigDecimal(cartera.WS_FrecuenciaCupon+""));
				}
			}

			logger.debug(carteras.size() + " carteras a valorizar");
			if(carteras.size()==0) {
				out.P_OUT_ERRORESCONTADOR = out.P_OUT_ERRORESCONTADOR  + 1L;
				out.P_OUT_MENSAJE=  out.P_OUT_MENSAJE.concat("\n No Existen Carteras a valorizar" ) ;
				return out;
			}
			ValorizarCustodia.ValorizarCustodiaEntrada ValorizarCust = new ValorizarCustodia.ValorizarCustodiaEntrada();
			ValorizarCust.p_IdEmpresa 					= in.p_IdEmpresa;
			ValorizarCust.p_IdUsuario					= in.p_IdUsuario;
			ValorizarCust.p_IdModulo 					= "PLA";			
			ValorizarCust.p_IdPais 						= in.p_IdPais;
			ValorizarCust.p_OrigenProcesamiento 		= "VALORIZACION_CARTERARFIF";

			for (int i = 0; i < carteras.size() ; i++ ){
				ObjCarteras carterasrfif = carteras.get(i);
				ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada ValorizarInstrumentos = new ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada();				
				ValorizarInstrumentos.FechaOperacion 					= java.sql.Date.valueOf(in.p_FechaSistema);
				ValorizarInstrumentos.IdClasificacionTipoInstrumento 	= carterasrfif.WS_IdClasificacionTipoInstrumento;
				ValorizarInstrumentos.IdTipoInstrumento 				= carterasrfif.WS_IdTipoInstrumento;
				ValorizarInstrumentos.IdInstrumento 					= carterasrfif.WS_IdInstrumento;
				ValorizarInstrumentos.Nemotecnico 						= carterasrfif.WS_Nemotecnico;
				ValorizarInstrumentos.IdTipoReajusteInstrumento 		= carterasrfif.WS_IdTipoReajuste;
				ValorizarInstrumentos.IdMonedaEmisionInstrumento 		= carterasrfif.WS_IdMonedaEmision;
				ValorizarInstrumentos.Cantidad 							= carterasrfif.WS_Cantidad;
				ValorizarInstrumentos.ValorRescate 						= carterasrfif.WS_ValorRescate;
				ValorizarInstrumentos.PrecioCompra 						= carterasrfif.WS_PrecioCompra;
				ValorizarInstrumentos.TasaCompra 						= carterasrfif.WS_TasaEmisionInstrumento;
				ValorizarInstrumentos.PrecioSubyacente 					= carterasrfif.WS_PrecioSubyacente;
				ValorizarInstrumentos.TasaSubyacente 					= carterasrfif.WS_TasaSubyacente;
				ValorizarInstrumentos.PrecioGarantia 					= carterasrfif.WS_PrecioGarantia;
				ValorizarInstrumentos.TasaGarantia 						= carterasrfif.WS_TasaGarantia;
				ValorizarInstrumentos.TieneCorteDeCupon 				= carterasrfif.WS_TieneCorteDeCupon;
				ValorizarInstrumentos.IdCliente 						= carterasrfif.WS_IdCliente;
				ValorizarInstrumentos.IdPortafolio 						= carterasrfif.WS_IdPortafolio;
				ValorizarInstrumentos.TablaOrigen 						= carterasrfif.TablaOrigen;		
				ValorizarInstrumentos.IdCustodia 						= carterasrfif.IdTablaOrigen;								
				ValorizarInstrumentos.TablaOrigen						= carterasrfif.TablaOrigen;
				ValorizarInstrumentos.FechaVencimientoInstrumento		= carterasrfif.WS_FechaVencimiento;
				ValorizarInstrumentos.IdTipoReajusteInstrumento			= carterasrfif.WS_IdTipoReajuste;
				ValorizarInstrumentos.IdMonedaInstrumento 				= carterasrfif.WS_IdMonedaInstrumento;
				ValorizarInstrumentos.EsRegistroNuevo 					= "N";
				ValorizarInstrumentos.MontoCompra						= carterasrfif.WS_MontoCompra;						
				ValorizarInstrumentos.MontoCompraMonedaSistema			= carterasrfif.WS_BK_INGMontoATasaCompraActual;			//preguntas que montos son NULLL 
				ValorizarInstrumentos.MontoMercadoMonedaSistema			= carterasrfif.WS_BK_INGMontoATasaMercadoActual;			
				ValorizarInstrumentos.PlazoEnDiasOperacion				= carterasrfif.WS_PlazoEnDias;
				ValorizarInstrumentos.IdMonedaEmisionInstrumento		= carterasrfif.WS_IdMonedaEmision;
				ValorizarInstrumentos.Emisor							= carterasrfif.WS_IdEmisor;
				
				ValorizarCust.p_InstrumentosAValorizar.add(ValorizarInstrumentos);
				
				if ((i!= 0 && i%5000 == 0) || i == carteras.size() - 1) { //AL FINAL O CADA 5000
					logger.debug("Valorizando hasta " + i);					

					logger.debug(" Entrada a LlamadaValorizarInstrumento");
					out = LlamadaValorizarInstrumento(con, ValorizarCust,  sql,out,frecuenciasCarterasHash , in.p_FechaSistema) ;
					logger.debug(" Salida a LlamadaValorizarInstrumento");

					ValorizarCust.p_InstrumentosAValorizar.clear();
				}
			}
			logger.debug(" Salida a logicaValoresAdicionales");
		} // IF (p_ValorizaCarteraParaInterfaces.equals("S"))
		
		if(in.p_ValorizaPactosSubyacenteTasaCompra.equals("S")) {
			logger.debug(" Entrada a logicaValoresAdicionales Pactos Subyacentes");
			
			String parametroTasaRecompraVentaConPacto = obtenerParametroUsoTasaRecompraVentaConPacto(con, in.p_IdEmpresa);
			
			HashMap<String,Object> paramsCarteraPacto = new HashMap<>(4);
			List<ValorizarCarteraPactos.ObjCarteras> subyacentes = new ArrayList<ValorizarCarteraPactos.ObjCarteras>();
			ValorizarCustodia.ValorizarCustodiaEntrada ValorizarCust = new ValorizarCustodia.ValorizarCustodiaEntrada();
			
			ValorizarCust.p_IdEmpresa 					= in.p_IdEmpresa;
			ValorizarCust.p_IdUsuario					= in.p_IdUsuario;
			ValorizarCust.p_IdModulo 					= "PLA";			
			ValorizarCust.p_IdPais 						= in.p_IdPais;
			ValorizarCust.p_OrigenProcesamiento 		= "VALORIZACION_CARTERARFIF";
	
			paramsCarteraPacto.put("IdEmpresa",in.p_IdEmpresa);
			paramsCarteraPacto.put("IdPortafolio", in.p_IdPortafolio);
			paramsCarteraPacto.put("IdCliente", in.p_IdCliente);
			paramsCarteraPacto.put("fechaSistema", in.p_FechaSistema);

			subyacentes = sql.query(con, SQL_SelectSubyacente, paramsCarteraPacto, ValorizarCarteraPactos.ObjCarteras.class);

			logger.debug(subyacentes.size() + " carteras a valorizar");
			if(subyacentes.size()==0) {
				out.P_OUT_ERRORESCONTADOR = out.P_OUT_ERRORESCONTADOR  + 1L;
				out.P_OUT_MENSAJE=  out.P_OUT_MENSAJE.concat("\n No Existen Carteras a valorizar" ) ;
				return out;
			}

			for (int i = 0; i < subyacentes.size() ; i++ ){
				ValorizarCarteraPactos.ObjCarteras carteraspac = subyacentes.get(i);
				ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada ValorizarInstrumentos = new ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada();
				
				ValorizarInstrumentos.FechaOperacion = carteraspac.WS_FechaOperacion;
				ValorizarInstrumentos.IdClasificacionTipoInstrumento = carteraspac.WS_IdClasificacionTipoInstrumento;
				ValorizarInstrumentos.IdTipoInstrumento = carteraspac.WS_IdTipoInstrumento;
				ValorizarInstrumentos.FechaVencimientoOperacion = carteraspac.WS_FechaVencimiento;
				ValorizarInstrumentos.FechaVencimientoInstrumento = carteraspac.WS_FechaVencimiento;
				ValorizarInstrumentos.IdInstrumento = carteraspac.WS_IdInstrumento;
				ValorizarInstrumentos.Nemotecnico = carteraspac.WS_Nemotecnico;
				
				ValorizarInstrumentos.IdMonedaInstrumento = carteraspac.WS_IdMonedaInstrumento;
				ValorizarInstrumentos.EsRegistroNuevo = "N";
				
				ValorizarInstrumentos.IdMonedaEmisionInstrumento= carteraspac.WS_IdMonedaEmision;
	
				ValorizarInstrumentos.Nemotecnico = carteraspac.WS_Nemotecnico;
				ValorizarInstrumentos.IdTipoReajusteInstrumento = carteraspac.WS_IdTipoReajuste;
				ValorizarInstrumentos.Cantidad = carteraspac.WS_Cantidad;
				ValorizarInstrumentos.ValorRescate = carteraspac.WS_MontoCompra;//INGMontoCompra
				ValorizarInstrumentos.PrecioCompra = carteraspac.WS_PrecioCompra;
				ValorizarInstrumentos.TasaCompra = ("S").equals(parametroTasaRecompraVentaConPacto) ? carteraspac.WS_TasaCompraCarteraPacto : carteraspac.WS_TasaCompraCarteraOrigen;
				ValorizarInstrumentos.PrecioSubyacente = carteraspac.WS_PrecioSubyacente;
				ValorizarInstrumentos.TasaSubyacente = carteraspac.WS_TasaCompra;
				ValorizarInstrumentos.PrecioGarantia = carteraspac.WS_PrecioGarantia;
				ValorizarInstrumentos.TasaGarantia = carteraspac.WS_TasaGarantia;
				ValorizarInstrumentos.TieneCorteDeCupon = carteraspac.WS_TieneCorteDeCupon;
				ValorizarInstrumentos.IdCliente = carteraspac.WS_IdCliente;
				ValorizarInstrumentos.IdPortafolio = carteraspac.WS_IdPortafolio;
				ValorizarInstrumentos.TablaOrigen = carteraspac.TablaOrigen;		
				ValorizarInstrumentos.IdCustodia = carteraspac.IdTablaOrigen;
				ValorizarInstrumentos.IdTipoOperacion = carteraspac.WS_IdTipoOperacion;
				ValorizarInstrumentos.MontoCompra ="SUBYACENTE".equals(carteraspac.TablaOrigen) ? carteraspac.WS_MontoInicialSubyacente:null;//siempre se valoriza con valor rescate/cantidad + precio/tasa
				ValorizarInstrumentos.MontoCompraMonedaSistema = "SUBYACENTE".equals(carteraspac.TablaOrigen) ? carteraspac.WS_BK_INGMontoATasaCompraActual:null;//siempre se valoriza con valor rescate/cantidad + precio/tasa
				ValorizarInstrumentos.IdTipoReajusteCabecera = carteraspac.WS_IdTipoReajusteCabecera;
				
				ValorizarCust.p_InstrumentosAValorizar.add(ValorizarInstrumentos);

				if ((i!= 0 && i%5000 == 0) || i == subyacentes.size() - 1) {
					logger.debug("Valorizando hasta " + i);
					
			       //------------------------------------------
					logger.debug(" Entrada a LlamadaValorizarInstrumento");
					out = LlamadaValorizarInstrumento(con, ValorizarCust, sql, out, frecuenciasCarterasHash ,  in.p_FechaSistema);
					logger.debug(" Salida a LlamadaValorizarInstrumento");
				   //------------------------------------------

			        ValorizarCust.p_InstrumentosAValorizar.clear();
				}
			}
		}
		
		return out;
	}


	private OutValorizacionValoresAdicionales LlamadaValorizarInstrumento(Connection con, ValorizarCustodiaEntrada valorizarCust, SQL sql,  ValorizacionValoresAdicionalesBatch.OutValorizacionValoresAdicionales out
			, HashMap<Long, BigDecimal> frecuenciasCarterasHash ,  String FechaValorizacion)
			throws Exception, SQLException {
		
		//00 - VARIABLES DE SISTEMA
		String tFechaHoraSistema	= FechaValorizacion;
		
		List<TipoRegistro> tipoRegistro = new ArrayList<TipoRegistro>();
		HashMap<String,Object> paramsTipoRegistro = new HashMap<>();
		paramsTipoRegistro.put("IdEmpresa",valorizarCust.p_IdEmpresa);//1
		tipoRegistro = sql.query(con, SQL_SelectTipoRegistro, paramsTipoRegistro, TipoRegistro.class);
	
		//01 - SECUENCIAS
		List<Long> SecValorizacionCustodia = Utils.getSecuencia(con, "BSCL.Custodia.BD.Secuencias::ValorizacionCustodia", new Long(valorizarCust.p_InstrumentosAValorizar.size()));
		logger.debug("Fin ObtenerSecuencias");
					
		//02 - PREPARAR INSTRUMENTOS A VALORIZAR
		int indxValorizacion = 0;
		ValorizarInstrumento.ValorizarInstrumentoEntrada VIE 			= new ValorizarInstrumento.ValorizarInstrumentoEntrada();
		List<ObjInstrumentoAValorizar> InstrumentosAValorizar 			= new ArrayList<ObjInstrumentoAValorizar>();
		List<CalcularPrecioPromedio.ObjEntradaEntrada> PPDatosEntrada 	= new ArrayList<CalcularPrecioPromedio.ObjEntradaEntrada>();

		logger.debug("Inicio Loop Entradas ValorizarInstrumento");
		for (ValorizarCustodiaEntrada.ObjDatoEntrada  DatoEntrada : valorizarCust.p_InstrumentosAValorizar){
			if (DatoEntrada.IdInstrumento != null && !DatoEntrada.IdInstrumento.equals(0L)){
				ObjInstrumentoAValorizar instrumento = new ObjInstrumentoAValorizar();
				
				instrumento.TablaOrigen = DatoEntrada.TablaOrigen;
				instrumento.IdCustodia = DatoEntrada.IdCustodia;
				instrumento.IdCliente = DatoEntrada.IdCliente;
				instrumento.IdPortafolio = DatoEntrada.IdPortafolio;
				instrumento.EsRegistroNuevo = DatoEntrada.EsRegistroNuevo;
				instrumento.GenerarNuevoFolioCustodia = DatoEntrada.GenerarNuevoFolioCustodia;
				instrumento.OrigenProcesamiento = DatoEntrada.OrigenProcesamiento;
				instrumento.FechaOperacion = Fecha.toStringDate(DatoEntrada.FechaOperacion);
				instrumento.PlazoEnDiasOperacion = DatoEntrada.	PlazoEnDiasOperacion;
				instrumento.IdClasificacionTipoInstrumento = DatoEntrada.IdClasificacionTipoInstrumento;
				instrumento.IdTipoInstrumento = DatoEntrada.IdTipoInstrumento;
				instrumento.IdInstrumento = DatoEntrada.IdInstrumento;
				instrumento.Nemotecnico = DatoEntrada.Nemotecnico;
				instrumento.TieneCorteDeCupon = DatoEntrada.TieneCorteDeCupon;
				instrumento.IdMonedaInstrumento = DatoEntrada.IdMonedaInstrumento;
				instrumento.IdMonedaEmisionInstrumento = DatoEntrada.IdMonedaEmisionInstrumento;
				instrumento.IdTipoReajusteInstrumento = DatoEntrada.IdTipoReajusteInstrumento;
				instrumento.Cantidad = DatoEntrada. Cantidad;
				instrumento.ValorRescate = DatoEntrada.	ValorRescate;
				if(DatoEntrada.MontoCompraSubyacente != null) {
					instrumento.MontoCompra = DatoEntrada.ValorRescate;
				}else {
					instrumento.MontoCompra = DatoEntrada.	MontoCompra;
				}
				
				instrumento.MontoCompraMonedaSistema = DatoEntrada.MontoCompraMonedaSistema;
				instrumento.MontoMercadoMonedaSistema = DatoEntrada.MontoMercadoMonedaSistema;
				instrumento.MontoCompraSubyacente = DatoEntrada.MontoCompraSubyacente;
				instrumento.MontoCompraGarantia = DatoEntrada.MontoCompraGarantia;
				instrumento.PrecioCompra = DatoEntrada. PrecioCompra;
				instrumento.TasaCompra = DatoEntrada.TasaCompra;
				instrumento.PrecioSubyacente = DatoEntrada.PrecioSubyacente;
				instrumento.TasaSubyacente = DatoEntrada.TasaSubyacente;
				instrumento.PrecioGarantia = DatoEntrada.PrecioGarantia;
				instrumento.TasaGarantia = DatoEntrada.TasaGarantia;
				instrumento.Emisor = DatoEntrada.Emisor;
				
				instrumento.IdValorizacion 				= SecValorizacionCustodia.get( indxValorizacion );
				instrumento.FechaValorizacion 			= tFechaHoraSistema;
				instrumento.FechaOperacion				= DatoEntrada.FechaOperacion.toString();
				instrumento.FechaVencimientoOperacion	= DatoEntrada.FechaVencimientoOperacion != null ? DatoEntrada.FechaVencimientoOperacion.toString() : null;
				instrumento.FechaVencimiento			= DatoEntrada.FechaVencimientoInstrumento != null ? DatoEntrada.FechaVencimientoInstrumento.toString() : null;
				instrumento.FechaEmision				= DatoEntrada.FechaEmisionInstrumento != null ? DatoEntrada.FechaEmisionInstrumento.toString() : "0001-01-01";
				instrumento.IdCondicionDeLiquidacion	= DatoEntrada.IdCondicionLiquidacion;
				instrumento.FechaDeLiquidacion			= DatoEntrada.FechaLiquidacion != null ? DatoEntrada.FechaLiquidacion.toString() : "0001-01-01";
				instrumento.OrigenProcesamiento 		= DatoEntrada.OrigenProcesamiento == null ? valorizarCust.p_OrigenProcesamiento : DatoEntrada.OrigenProcesamiento;
				DatoEntrada.OrigenProcesamiento 		= instrumento.OrigenProcesamiento;
				InstrumentosAValorizar.add(instrumento);
				indxValorizacion ++;
				
				ValorizarInstrumento.ObjEntradaInstrumentos EI = new ValorizarInstrumento.ObjEntradaInstrumentos();
				
				EI.FechaValorizacion = FechaValorizacion;
				EI.IdValorizacion = instrumento.IdValorizacion;
				EI.OrigenProcesamiento = instrumento.OrigenProcesamiento;
				EI.FechaValorizacion = instrumento.FechaValorizacion;
				EI.FechaOperacion = instrumento.FechaOperacion;						//Solo para operaciones a plazo
				EI.FechaVencimientoOperacion = instrumento.FechaVencimientoOperacion;			//Solo para operaciones a plazo
				EI.PlazoEnDiasOperacion = instrumento.PlazoEnDiasOperacion;				//Solo para operaciones a plazo		
				EI.IdClasificacionTipoInstrumento = instrumento.IdClasificacionTipoInstrumento; 
				EI.IdTipoInstrumento = instrumento.IdTipoInstrumento; 
				EI.IdInstrumento = instrumento.IdInstrumento;
				EI.Nemotecnico = instrumento.Nemotecnico;
				EI.FechaVencimiento = instrumento.FechaVencimiento;
				EI.FechaEmision = instrumento.FechaEmision;
				EI.IdMonedaInstrumento = instrumento.IdMonedaInstrumento;
				EI.IdMonedaEmisionInstrumento = instrumento.IdMonedaEmisionInstrumento;
				EI.IdTipoReajusteInstrumento = instrumento.IdTipoReajusteInstrumento;
				EI.Emisor = instrumento.Emisor;								//valorizacionCL
				EI.IdCondicionDeLiquidacion = instrumento.IdCondicionDeLiquidacion;			//valorizacionCL
				EI.FechaDeLiquidacion = instrumento.FechaDeLiquidacion;
				EI.Cantidad = instrumento.Cantidad; 
				EI.ValorRescate = instrumento.ValorRescate; 
				EI.PrecioCompra = instrumento.PrecioCompra; 
				EI.TasaCompra = instrumento.TasaCompra;
				EI.MontoCompra = instrumento.MontoCompra;						//en moneda del instrumento		
				EI.MontoCompraMonedaSistema = instrumento.MontoCompraMonedaSistema;			//en moneda del instrumento
				EI.PrecioSubyacente = instrumento.PrecioSubyacente; 
				EI.TasaSubyacente = instrumento.TasaSubyacente; 
				EI.PrecioGarantia = instrumento.PrecioGarantia; 
				EI.TasaGarantia = instrumento.TasaGarantia; 
				EI.TasaTIF = instrumento.TasaTIF;
				EI.IdPortafolio = instrumento.IdPortafolio;
				EI.TablaOrigen = instrumento.TablaOrigen;		
				
				EI.IdTipoOperacion = DatoEntrada.IdTipoOperacion;
				
				VIE.p_Instrumentos.add(EI);
				
				//CalculoPrecioPromedio
				if ( !DatoEntrada.IdTipoInstrumento.equals("SIM") ) {
					CalcularPrecioPromedio.ObjEntradaEntrada PrecioPromedio = new CalcularPrecioPromedio.ObjEntradaEntrada();
					
					PrecioPromedio.IdCliente = DatoEntrada.IdCliente;
					PrecioPromedio.IdPortafolio = DatoEntrada.IdPortafolio;
					PrecioPromedio.IdInstrumento = DatoEntrada.IdInstrumento;
					
					PPDatosEntrada.add(PrecioPromedio);
				}
			} 
			
		};
		logger.debug("Fin Loop Entradas ValorizarInstrumento");
		
		ValorizarInstrumento.ValorizarInstrumentoSalida resultValorizacion = new ValorizarInstrumento.ValorizarInstrumentoSalida();
		
		if (VIE.p_Instrumentos.size() > 0){
			ValorizarInstrumento VI = new ValorizarInstrumento();
			VIE.p_IdEmpresa	= valorizarCust.p_IdEmpresa;		
			VIE.p_IdUsuario	= valorizarCust.p_IdUsuario;
			VIE.p_IdModulo	= valorizarCust.p_IdModulo;
			VIE.p_IdPais	= valorizarCust.p_IdPais;
			VIE.p_OrigenProcesamiento = valorizarCust.p_OrigenProcesamiento;
			resultValorizacion = VI.logica(con, VIE);
			logger.debug("Salida ValorizarInstrumento");
			
			if (VIE != null && !resultValorizacion.P_OUT_ERRORESCONTADOR.equals(0L)){
				out.P_OUT_ERRORESCONTADOR = out.P_OUT_ERRORESCONTADOR  + 1L;
				out.P_OUT_MENSAJE= out.P_OUT_MENSAJE.concat("\n"+"Resultado de valorizacion con errores: "+resultValorizacion.P_OUT_ERRORESCONTADOR+" Carteras con errores se continua con proceso" ) ;
			};

						
			// obtengo informacion de emisores para poner fecha de cupones a los instrumentos CERO y ZERO
			List<ObjUpdateTablas> idsCartera = new ArrayList<ObjUpdateTablas>();
			HashMap<Long,ValorizarCustodiaEntrada.ObjDatoEntrada> hashDatosEntrada = new HashMap<>();
			for (ValorizarCustodiaEntrada.ObjDatoEntrada  DatoEntrada : valorizarCust.p_InstrumentosAValorizar){
				if (DatoEntrada.TablaOrigen.equals("CARTERA") && !hashDatosEntrada.containsKey(DatoEntrada.IdCustodia)){
					hashDatosEntrada.put(DatoEntrada.IdCustodia, DatoEntrada);
					ObjUpdateTablas t = new ObjUpdateTablas();
					t.IdCartera = DatoEntrada.IdCustodia;
					idsCartera.add(t);
				}
			}
			
			sql.batch(con, SQL_InsertCartera, idsCartera, ObjUpdateTablas.class);
			List<ObjCartera> CAR = sql.query(con, SQL_SelectCartera2, new HashMap<>(), ObjCartera.class);
			HashMap<Long,Object> hmCartera = Utils.List2Hash(CAR, "IdCartera");
			SQL.exec(con, SQL_DeleteCartera);
			
			//03 - ACTUALIZAR CUSTODIA
			List<ObjUpdateTablas> paramsUpdCartera = new ArrayList<ObjUpdateTablas>();
			List<ObjUpdateTablas> paramsUpdCarteraPactoSubyacente = new ArrayList<ObjUpdateTablas>();
						
			HashMap<Long,ArrayList<cl.bcs.custodia.utils.ValorizarInstrumento.ObjResultadoValorizacion>> hashResultadoValorizacion = new HashMap<Long,ArrayList<cl.bcs.custodia.utils.ValorizarInstrumento.ObjResultadoValorizacion>>();
			for (ObjResultadoValorizacion res : resultValorizacion.P_OUT_INSTRUMENTOSVALORIZADOS) {
				System.out.println(res.toString());
				ArrayList<cl.bcs.custodia.utils.ValorizarInstrumento.ObjResultadoValorizacion> ll = hashResultadoValorizacion.get(res.IdValorizacion);
				if (ll == null) {
					ll = new ArrayList<ValorizarInstrumento.ObjResultadoValorizacion>();
					hashResultadoValorizacion.put(res.IdValorizacion,ll);
				}
				
				ll.add(res);
			}
			
			for (ObjInstrumentoAValorizar InstrumentoAValorizar: InstrumentosAValorizar){
				ObjUpdateTablas Tabla = new ObjUpdateTablas();
				ObjCartera InfoCartera = new ObjCartera();
				if (InstrumentoAValorizar.TablaOrigen.equals("CARTERA")){
					InfoCartera = (ObjCartera) hmCartera.get(InstrumentoAValorizar.IdCustodia);
				}
				
				//ACTUALIZACION TITULOS
				//con el hash restringimos la lista solo a las valorizaciones que correspondan a la entrada
				ArrayList<ObjResultadoValorizacion> listaValorizaciones = hashResultadoValorizacion.get(InstrumentoAValorizar.IdValorizacion);
				for (ValorizarInstrumento.ObjResultadoValorizacion ResultadoValorizacion : listaValorizaciones){
					
					if ( !InstrumentoAValorizar.IdValorizacion.equals( ResultadoValorizacion.IdValorizacion) ) {
						continue;
					}
					
					Tabla.IdValorizacion				= ResultadoValorizacion.IdValorizacion;
					Tabla.TicketProcesamiento			= ResultadoValorizacion.TicketProcesamiento;
					Tabla.TablaOrigen					= InstrumentoAValorizar.TablaOrigen;
					Tabla.EsRegistroNuevo				= InstrumentoAValorizar.EsRegistroNuevo;
					Tabla.GenerarNuevoFolioCustodia		= InstrumentoAValorizar.GenerarNuevoFolioCustodia;
					Tabla.OrigenProcesamiento			= InstrumentoAValorizar.OrigenProcesamiento;
					Tabla.IdCartera 					= InstrumentoAValorizar.TablaOrigen.equalsIgnoreCase("CARTERA") ? InstrumentoAValorizar.IdCustodia : null;
					Tabla.IdSubyacente 					= InstrumentoAValorizar.TablaOrigen.equalsIgnoreCase("SUBYACENTE") ? InstrumentoAValorizar.IdCustodia : null;
					Tabla.IdGarantia 					= InstrumentoAValorizar.TablaOrigen.equalsIgnoreCase("GARANTIA") ? InstrumentoAValorizar.IdCustodia : null;
					Tabla.IdMovimientoCarteraEnTransito	= InstrumentoAValorizar.TablaOrigen.equalsIgnoreCase("TRANSITO") ? InstrumentoAValorizar.IdCustodia : null;	
					Tabla.ValorPar = ResultadoValorizacion.ValorPar != null ? ResultadoValorizacion.ValorPar : BigDecimal.ZERO;
					//*****************************************************************************************************

					if (Tabla.OrigenProcesamiento.length() < 12) {
						logger.debug("algo raro: origen procesamiento" + valorizarCust.p_OrigenProcesamiento);
						logger.debug("tabla " + Tabla.toString());
					}
					
					if (Tabla.OrigenProcesamiento.equalsIgnoreCase("INGRESO_CUSTODIA") || (Tabla.OrigenProcesamiento.length() >= 12 && Tabla.OrigenProcesamiento.substring(0,12).equalsIgnoreCase("VALORIZACION")) ){
						if (ResultadoValorizacion.ResultadoProceso == null) continue;							
						
						Tabla.INGTicketValorizacion 			= ResultadoValorizacion.TicketProcesamiento;
						Tabla.INGValorizacionConProblemas 		= ResultadoValorizacion.ResultadoProceso.equals("OK") ? "N" : "S";
						
						
						Tabla.TasaCompra = InstrumentoAValorizar.TasaCompra;

						
						if ( ResultadoValorizacion.DimensionTiempo.equals("AN") ){
							
							Tabla.INGFechaValorizacionAnterior 				= ResultadoValorizacion.FechaValorizacion;
							Tabla.INGFechaFactorMonedaInstrumentoAnterior	= ResultadoValorizacion.FechaFactorMonedaInstrumento;
							Tabla.INGValorFactorMonedaInstrumentoAnterior	= ResultadoValorizacion.FactorMonedaInstrumento;
							Tabla.INGFechaFactorMonedaAdicionalAnterior 	= ResultadoValorizacion.FechaFactorMonedaAdicional;
							Tabla.INGValorFactorMonedaAdicionalAnterior 	= ResultadoValorizacion.FactorMonedaAdicional;
							
							if (ResultadoValorizacion.DimensionTasa.equals("COMP")){
								
								Tabla.INGMontoATasaCompraAnterior 						= ResultadoValorizacion.MontoMonedaSistema ;
								Tabla.INGMontoInteresCompraAnterior 					= ResultadoValorizacion.MontoInteresMonedaSistema;
								Tabla.INGMontoReajusteCompraAnterior 					= ResultadoValorizacion.MontoReajusteMonedaSistema;
								Tabla.INGMontoCompraAnteriorMonedaInstrumento 			= ResultadoValorizacion.MontoMonedaInstrumento;
								Tabla.INGMontoInteresCompraAnteriorMonedaInstrumento 	= ResultadoValorizacion.MontoInteresMonedaInstrumento;
								Tabla.INGMontoReajusteCompraAnteriorMonedaInstrumento 	= ResultadoValorizacion.MontoReajusteMonedaInstrumento;
								Tabla.INGMontoCompraAnteriorMonedaAdicional 			= ResultadoValorizacion.MontoMonedaAdicional;
								Tabla.INGMontoInteresCompraAnteriorMonedaAdicional 		= ResultadoValorizacion.MontoInteresMonedaAdicional;
								Tabla.INGMontoReajusteCompraAnteriorMonedaAdicional 	= ResultadoValorizacion.MontoReajusteMonedaAdicional;
								Tabla.INGDurationCompraAnterior 						= ResultadoValorizacion.Duration;
								Tabla.INGConvexidadCompraAnterior 						= ResultadoValorizacion.Convexidad;								
								Tabla.INGMontoAPlazoMonedaAdicional 					= ResultadoValorizacion.MontoAPlazoMonedaAdicional;
							};
							if (ResultadoValorizacion.DimensionTasa.equals("MERC")){
								
								Tabla.INGFechaPrecioTasaMercadoAnterior 		= ResultadoValorizacion.FechaPrecioTasa;
								Tabla.INGFuenteTasaPrecioMercadoAnterior 		= ResultadoValorizacion.FuentePrecioTasa;
								Tabla.INGPrecioMercadoAnterior 					= ResultadoValorizacion.Precio;
								Tabla.INGTasaMercadoAnterior 					= ResultadoValorizacion.Tasa;
								Tabla.INGMontoATasaMercadoAnterior 				= ResultadoValorizacion.MontoMonedaSistema;
								Tabla.INGMontoInteresMercadoAnterior 			= ResultadoValorizacion.MontoInteresMonedaSistema;
								Tabla.INGMontoReajusteMercadoAnterior 			= ResultadoValorizacion.MontoReajusteMonedaSistema;
								Tabla.INGMontoMercadoAnteriorMonedaInstrumento 			= ResultadoValorizacion.MontoMonedaInstrumento;
								Tabla.INGMontoInteresMercadoAnteriorMonedaInstrumento 	= ResultadoValorizacion.MontoInteresMonedaInstrumento;
								Tabla.INGMontoReajusteMercadoAnteriorMonedaInstrumento 	= ResultadoValorizacion.MontoReajusteMonedaInstrumento;
								Tabla.INGMontoMercadoAnteriorMonedaAdicional 			= ResultadoValorizacion.MontoMonedaAdicional;
								Tabla.INGMontoInteresMercadoAnteriorMonedaAdicional 	= ResultadoValorizacion.MontoInteresMonedaAdicional;
								Tabla.INGMontoReajusteMercadoAnteriorMonedaAdicional 	= ResultadoValorizacion.MontoReajusteMonedaAdicional;
								Tabla.INGDurationMercadoAnterior 	= ResultadoValorizacion.Duration;
								Tabla.INGConvexidadMercadoAnterior 	= ResultadoValorizacion.Convexidad;
							};
							if (ResultadoValorizacion.DimensionTasa.equals("GTIA")){
								Tabla.INGMontoATasaGarantiaAnterior 				= ResultadoValorizacion.MontoMonedaSistema;
								//Tabla.INGMontoInteresGarantiaAnterior 				= ResultadoValorizacion.MontoInteresMonedaSistema;
								//Tabla.INGMontoReajusteGarantiaAnterior 				= ResultadoValorizacion.MontoReajusteMonedaSistema;
								Tabla.INGMontoGarantiaAnteriorMonedaInstrumento 	= ResultadoValorizacion.MontoMonedaInstrumento;
								//Tabla.INGMontoInteresGarantiaAnteriorMonedaInstrumento = ResultadoValorizacion.MontoInteresMonedaInstrumento;
								//Tabla.INGMontoReajusteGarantiaAnteriorMonedaInstrumento = ResultadoValorizacion.MontoReajusteMonedaInstrumento;
								Tabla.INGMontoGarantiaAnteriorMonedaAdicional 			= ResultadoValorizacion.MontoMonedaAdicional;
								//Tabla.INGMontoInteresGarantiaAnteriorMonedaAdicional 	= ResultadoValorizacion.MontoInteresMonedaAdicional;
								//Tabla.INGMontoReajusteGarantiaAnteriorMonedaAdicional 	= ResultadoValorizacion.MontoReajusteMonedaAdicional;
								//Tabla.INGDurationGarantiaAnterior 					= ResultadoValorizacion.Duration;
								//Tabla.INGConvexidadGarantiaAnterior 				= ResultadoValorizacion.Convexidad;									
							};
							if (ResultadoValorizacion.DimensionTasa.equals("SUBY")){
								Tabla.INGMontoATasaSubyacenteAnterior	 			= ResultadoValorizacion.MontoMonedaSistema;
								//Tabla.INGMontoInteresSubyacenteAnterior 			= ResultadoValorizacion.MontoInteresMonedaSistema;
								//Tabla.INGMontoReajusteSubyacenteAnterior 			= ResultadoValorizacion.MontoReajusteMonedaSistema;
								Tabla.INGMontoSubyacenteAnteriorMonedaInstrumento 	= ResultadoValorizacion.MontoMonedaInstrumento;
								//Tabla.INGMontoInteresSubyacenteAnteriorMonedaInstrumento	= ResultadoValorizacion.MontoInteresMonedaInstrumento;
								//Tabla.INGMontoReajusteSubyacenteAnteriorMonedaInstrumento	= ResultadoValorizacion.MontoReajusteMonedaInstrumento;
								Tabla.INGMontoSubyacenteAnteriorMonedaAdicional		= ResultadoValorizacion.MontoMonedaAdicional;
								//Tabla.INGMontoInteresSubyacenteAnteriorMonedaAdicional	= ResultadoValorizacion.MontoInteresMonedaAdicional;
								//Tabla.INGMontoReajusteSubyacenteAnteriorMonedaAdicional 	= ResultadoValorizacion.MontoReajusteMonedaAdicional;
								//Tabla.INGDurationSubyacenteAnterior 						= ResultadoValorizacion.Duration;
								//Tabla.INGConvexidadSubyacenteAnterior 					= ResultadoValorizacion.Convexidad;
							};
							if (ResultadoValorizacion.DimensionTasa.equals("P+CM")){
								
								Tabla.INGMontoCompraAnteriorPibMas 					= ResultadoValorizacion.MontoMonedaSistema;
								Tabla.INGMontoCompraAnteriorPibMasMonedaInstrumento = ResultadoValorizacion.MontoMonedaInstrumento;
								Tabla.INGMontoCompraAnteriorPibMasMonedaAdicional 	= ResultadoValorizacion.MontoMonedaAdicional;
							};
							if (ResultadoValorizacion.DimensionTasa.equals("P-CM")){
								
								Tabla.INGMontoCompraAnteriorPibMenos 					= ResultadoValorizacion.MontoMonedaSistema;
								Tabla.INGMontoCompraAnteriorPibMenosMonedaInstrumento 	= ResultadoValorizacion.MontoMonedaInstrumento;
								Tabla.INGMontoCompraAnteriorPibMenosMonedaAdicional 	= ResultadoValorizacion.MontoMonedaAdicional;
							};
							if (ResultadoValorizacion.DimensionTasa.equals("P+MC")){
								
								Tabla.INGMontoMercadoAnteriorPibMas 					= ResultadoValorizacion.MontoMonedaSistema;
								Tabla.INGMontoMercadoAnteriorPibMasMonedaInstrumento 	= ResultadoValorizacion.MontoMonedaInstrumento;
								Tabla.INGMontoMercadoAnteriorPibMasMonedaAdicional		= ResultadoValorizacion.MontoMonedaAdicional;
							};
							if (ResultadoValorizacion.DimensionTasa.equals("P-MC")){
								
								Tabla.INGMontoMercadoAnteriorPibMenos 					= ResultadoValorizacion.MontoMonedaSistema;
								Tabla.INGMontoMercadoAnteriorPibMenosMonedaInstrumento	= ResultadoValorizacion.MontoMonedaSistema;
								Tabla.INGMontoMercadoAnteriorPibMenosMonedaAdicional	= ResultadoValorizacion.MontoMonedaSistema;
							};
							if (ResultadoValorizacion.DimensionTasa.equals("P+SY")){
								
								Tabla.INGMontoSubyacenteAnteriorPibMas 					= ResultadoValorizacion.MontoMonedaSistema;
								Tabla.INGMontoSubyacenteAnteriorPibMasMonedaInstrumento = ResultadoValorizacion.MontoMonedaInstrumento;
								Tabla.INGMontoSubyacenteAnteriorPibMasMonedaAdicional	= ResultadoValorizacion.MontoMonedaAdicional;
							};
							if (ResultadoValorizacion.DimensionTasa.equals("P-SY")){
								
								Tabla.INGMontoSubyacenteAnteriorPibMenos 					= ResultadoValorizacion.MontoMonedaSistema;
								Tabla.INGMontoSubyacenteAnteriorPibMenosMonedaInstrumento	= ResultadoValorizacion.MontoMonedaSistema;
								Tabla.INGMontoSubyacenteAnteriorPibMenosMonedaAdicional		= ResultadoValorizacion.MontoMonedaSistema;
							};
							if (ResultadoValorizacion.DimensionTasa.equals("P+GT")){
								
								Tabla.INGMontoGarantiaAnteriorPibMas 					= ResultadoValorizacion.MontoMonedaSistema;
								Tabla.INGMontoGarantiaAnteriorPibMasMonedaInstrumento 	= ResultadoValorizacion.MontoMonedaInstrumento;
								Tabla.INGMontoGarantiaAnteriorPibMasMonedaAdicional		= ResultadoValorizacion.MontoMonedaAdicional;
							};
							if (ResultadoValorizacion.DimensionTasa.equals("P-GT")){
								
								Tabla.INGMontoGarantiaAnteriorPibMenos 					= ResultadoValorizacion.MontoMonedaSistema;
								Tabla.INGMontoGarantiaAnteriorPibMenosMonedaInstrumento	= ResultadoValorizacion.MontoMonedaSistema;
								Tabla.INGMontoGarantiaAnteriorPibMenosMonedaAdicional	= ResultadoValorizacion.MontoMonedaSistema;
							};
							
						}
						if ( ResultadoValorizacion.DimensionTiempo.equals("AC") ){
							Tabla.INGFechaValorizacionActual 			= ResultadoValorizacion.FechaValorizacion;
							Tabla.INGFechaFactorMonedaInstrumentoActual = ResultadoValorizacion.FechaFactorMonedaInstrumento;
							Tabla.INGValorFactorMonedaInstrumentoActual = ResultadoValorizacion.FactorMonedaInstrumento;
							Tabla.INGFechaFactorMonedaAdicionalActual 	= ResultadoValorizacion.FechaFactorMonedaAdicional;
							Tabla.INGValorFactorMonedaAdicionalActual 	= ResultadoValorizacion.FactorMonedaAdicional;
							
							if (ResultadoValorizacion.DimensionTasa.equals("COMP")){
								if (Tabla.EsRegistroNuevo.equalsIgnoreCase("S") || (Tabla.EsRegistroNuevo.equalsIgnoreCase("N") && Tabla.GenerarNuevoFolioCustodia != null && Tabla.GenerarNuevoFolioCustodia.equalsIgnoreCase("S"))){
									Tabla.FechaFactorMonedaInstrumento 	= ResultadoValorizacion.FechaFactorMonedaInstrumento;
									Tabla.ValorFactorMonedaInstrumento	= ResultadoValorizacion.FactorMonedaInstrumento;										
									Tabla.FechaFactorMonedaAdicional 	= ResultadoValorizacion.FechaFactorMonedaAdicional;
									Tabla.ValorFactorMonedaAdicional 	= ResultadoValorizacion.FactorMonedaAdicional;
									Tabla.MontoCompraMonedaAdicional 	= ResultadoValorizacion.MontoMonedaAdicional;
									Tabla.MontoAPlazoMonedaAdicional 	= ResultadoValorizacion.MontoAPlazoMonedaAdicional;
								};
								if (Tabla.EsRegistroNuevo.equalsIgnoreCase("N")){
									Tabla.INGMontoATasaCompraActual		= ResultadoValorizacion.MontoMonedaSistema;
									Tabla.INGMontoCompraActualMonedaInstrumento = ResultadoValorizacion.MontoMonedaInstrumento;
								};
								if (Tabla.EsRegistroNuevo.equalsIgnoreCase("N") && Tabla.GenerarNuevoFolioCustodia != null && Tabla.GenerarNuevoFolioCustodia.equalsIgnoreCase("S")){ //Para el caso de titulos que retornan a la cartera con nuevo folio 
									Tabla.MontoCompra					= ResultadoValorizacion.MontoMonedaSistema;
									Tabla.MontoCompraMonedaInstrumento 	= ResultadoValorizacion.MontoMonedaInstrumento;										
									Tabla.MontoAPlazo					= ResultadoValorizacion.MontoAPlazoMonedaSistema;
									Tabla.MontoAPlazoMonedaInstrumento	= ResultadoValorizacion.MontoAPlazoMonedaInstrumento;
								};
								
								Tabla.INGMontoCompraActualMonedaAdicional					= ResultadoValorizacion.MontoMonedaAdicional;
								Tabla.INGDurationCompraActual 								= ResultadoValorizacion.Duration;
								Tabla.INGConvexidadCompraActual 							= ResultadoValorizacion.Convexidad;
								Tabla.INGMontoInteresCorridoCompraActual 					= ResultadoValorizacion.MontoInteresAcumuladoMonedaSistema;
								Tabla.INGMontoReajusteCorridoCompraActual 					= ResultadoValorizacion.MontoReajusteAcumuladoMonedaSistema;
								Tabla.INGMontoInteresCorridoCompraActualMonedaInstrumento 	= ResultadoValorizacion.MontoInteresAcumuladoMonedaInstrumento;
								Tabla.INGMontoReajusteCorridoCompraActualMonedaInstrumento 	= ResultadoValorizacion.MontoReajusteAcumuladoMonedaInstrumento;
								Tabla.INGMontoInteresCorridoCompraActualMonedaAdicional 	= ResultadoValorizacion.MontoInteresAcumuladoMonedaAdicional;
								Tabla.INGMontoReajusteCorridoCompraActualMonedaAdicional 	= ResultadoValorizacion.MontoReajusteAcumuladoMonedaAdicional;
								
								// MSH
								// ------------------------------------------------------------------------------------------------
								// Tabla.INGMontoInteresCompraProximo 					= ResultadoValorizacion.MontoInteresMonedaSistema;
								// Tabla.INGMontoReajusteCompraProximo 				= ResultadoValorizacion.MontoReajusteMonedaSistema;
								
								// Tabla.INGMontoInteresCompraProximoMonedaInstrumento = ResultadoValorizacion.MontoInteresMonedaInstrumento;
								// Tabla.INGMontoReajusteCompraProximoMonedaInstrumento= ResultadoValorizacion.MontoReajusteMonedaInstrumento;
								
								// Tabla.INGMontoInteresCompraProximoMonedaAdicional 	= ResultadoValorizacion.MontoInteresMonedaAdicional;
								// Tabla.INGMontoReajusteCompraProximoMonedaAdicional 	= ResultadoValorizacion.MontoReajusteMonedaAdicional;
								// ------------------------------------------------------------------------------------------------								
								
								//MANEJO ESPECIAL PARA LOS CERO Y ZERO YA QUE EL VALORIZADOR ENTREGA LA FECHA DEL CUPON CORRIDO EN UN MES.
								//SE SOLICITA A ROBERTO CERDA LA CORRECCION, PERO SEGURAMENTE DEMORARAN MUCHO TIEMPO, ESO SI ES QUE LO ARREGLAN.
								//JAIME DUARTE SOLICITA QUE HICIERAMOS ESTE PARCHE. 2018-08
								//InfoEmisor.IdEmisor	= InfoEmisor.IdEmisor == null ? "" : InfoEmisor.IdEmisor;
								if (InstrumentoAValorizar.TablaOrigen.equals("CARTERA")){
									if (InfoCartera.IdEmisor.equals("CERO") || InfoCartera.IdEmisor.equals("ZERO")){
										Tabla.INGFechaCorteCupon		= Fecha.toDate("20"+ ResultadoValorizacion.Nemotecnico.substring(8,10) + "-" + ResultadoValorizacion.Nemotecnico.substring(6,8) +  "-" + ResultadoValorizacion.Nemotecnico.substring(4,6));
										Tabla.INGFechaPagoCorteCupon	= Fecha.toDate("20"+ ResultadoValorizacion.Nemotecnico.substring(8,10) + "-" + ResultadoValorizacion.Nemotecnico.substring(6,8) +  "-" + ResultadoValorizacion.Nemotecnico.substring(4,6));
									}else{
										Tabla.INGFechaCorteCupon 							= ResultadoValorizacion.FechaCorteCupon;
										Tabla.INGFechaPagoCorteCupon 						= ResultadoValorizacion.FechaPagoCorteCupon;
									}
								};
																
								//Tabla.INGFechaPagoCorteCupon 						= ResultadoValorizacion.FechaPagoCorteCupon;
								Tabla.INGInteresesPagoCorteCupon					= ResultadoValorizacion.MontoInteresesPagoCorteCupon;
								Tabla.INGNumeroCupon 								= ResultadoValorizacion.NumeroCupon;
								Tabla.INGIdMonedaPagoCupon 							= ResultadoValorizacion.IdMonedaPagoCupon;
								Tabla.INGMontoCorteCupon							= ResultadoValorizacion.MontoCorteCuponMonedaSistema;									
								Tabla.INGMontoCapitalCorteCupon 					= ResultadoValorizacion.MontoCapitalCorteCuponMonedaSistema;
								Tabla.INGMontoInteresCorteCupon 					= ResultadoValorizacion.MontoInteresCorteCuponMonedaSistema;
								Tabla.INGMontoReajusteCorteCupon 					= ResultadoValorizacion.MontoReajusteCorteCuponMonedaSistema;
								Tabla.INGMontoCorteCuponMonedaInstrumento 			= ResultadoValorizacion.MontoCorteCuponMonedaInstrumento;
								Tabla.INGMontoCapitalCorteCuponMonedaInstrumento 	= ResultadoValorizacion.MontoCapitalCorteCuponMonedaInstrumento;
								Tabla.INGMontoInteresCorteCuponMonedaInstrumento 	= ResultadoValorizacion.MontoInteresCorteCuponMonedaInstrumento;
								Tabla.INGMontoReajusteCorteCuponMonedaInstrumento 	= ResultadoValorizacion.MontoReajusteCorteCuponMonedaInstrumento;
								Tabla.INGMontoCorteCuponMonedaAdicional 			= ResultadoValorizacion.MontoCorteCuponMonedaAdicional;
								Tabla.INGMontoCapitalCorteCuponMonedaAdicional 		= ResultadoValorizacion.MontoCapitalCorteCuponMonedaAdicional;
								Tabla.INGMontoInteresCorteCuponMonedaAdicional 		= ResultadoValorizacion.MontoInteresCorteCuponMonedaAdicional;
								Tabla.INGMontoReajusteCorteCuponMonedaAdicional 	= ResultadoValorizacion.MontoReajusteCorteCuponMonedaAdicional;
							};
							if (ResultadoValorizacion.DimensionTasa.equals("MERC")){
								Tabla.INGFechaPrecioTasaMercadoActual 						= ResultadoValorizacion.FechaPrecioTasa;
								Tabla.INGFuenteTasaPrecioMercadoActual 						= ResultadoValorizacion.FuentePrecioTasa;
								Tabla.INGPrecioMercadoActual 								= ResultadoValorizacion.Precio;
								Tabla.INGTasaMercadoActual 									= ResultadoValorizacion.Tasa;
								Tabla.INGMontoATasaMercadoActual 							= ResultadoValorizacion.MontoMonedaSistema;
								Tabla.INGMontoMercadoActualMonedaInstrumento 				= ResultadoValorizacion.MontoMonedaInstrumento;
								Tabla.INGMontoMercadoActualMonedaAdicional 					= ResultadoValorizacion.MontoMonedaAdicional;
								Tabla.INGDurationMercadoActual 								= ResultadoValorizacion.Duration;
								Tabla.INGConvexidadMercadoActual 							= ResultadoValorizacion.Convexidad;
								Tabla.INGMontoInteresCorridoMercadoActual 					= ResultadoValorizacion.MontoInteresAcumuladoMonedaSistema;
								Tabla.INGMontoReajusteCorridoMercadoActual 					= ResultadoValorizacion.MontoReajusteAcumuladoMonedaSistema;
								Tabla.INGMontoInteresCorridoMercadoActualMonedaInstrumento 	= ResultadoValorizacion.MontoInteresAcumuladoMonedaInstrumento;
								Tabla.INGMontoReajusteCorridoMercadoActualMonedaInstrumento = ResultadoValorizacion.MontoReajusteAcumuladoMonedaInstrumento;
								Tabla.INGMontoInteresCorridoMercadoActualMonedaAdicional 	= ResultadoValorizacion.MontoInteresAcumuladoMonedaAdicional;
								Tabla.INGMontoReajusteCorridoMercadoActualMonedaAdicional 	= ResultadoValorizacion.MontoReajusteAcumuladoMonedaAdicional;
							};
							if (ResultadoValorizacion.DimensionTasa.equals("GTIA")){
								if (Tabla.EsRegistroNuevo.equalsIgnoreCase("S") || (Tabla.EsRegistroNuevo.equalsIgnoreCase("N") && Tabla.GenerarNuevoFolioCustodia != null && Tabla.GenerarNuevoFolioCustodia.equalsIgnoreCase("S"))){
									Tabla.MontoGarantiaMonedaAdicional 	= ResultadoValorizacion.MontoMonedaAdicional;										
								};
								if (Tabla.EsRegistroNuevo.equalsIgnoreCase("N")){
									Tabla.INGMontoATasaGarantiaActual 				= ResultadoValorizacion.MontoMonedaSistema;
									Tabla.INGMontoGarantiaActualMonedaInstrumento 	= ResultadoValorizacion.MontoMonedaInstrumento;
								};
																									
								Tabla.INGMontoGarantiaActualMonedaAdicional 					= ResultadoValorizacion.MontoMonedaAdicional;
								Tabla.INGDurationGarantiaActual 								= ResultadoValorizacion.Duration;
								Tabla.INGConvexidadGarantiaActual 								= ResultadoValorizacion.Convexidad;
								Tabla.INGMontoInteresCorridoGarantiaActual 						= ResultadoValorizacion.MontoInteresAcumuladoMonedaSistema;
								Tabla.INGMontoReajusteCorridoGarantiaActual 					= ResultadoValorizacion.MontoReajusteAcumuladoMonedaSistema;
								Tabla.INGMontoInteresCorridoGarantiaActualMonedaInstrumento 	= ResultadoValorizacion.MontoInteresAcumuladoMonedaInstrumento;
								Tabla.INGMontoReajusteCorridoGarantiaActualMonedaInstrumento 	= ResultadoValorizacion.MontoReajusteAcumuladoMonedaInstrumento;
								Tabla.INGMontoInteresCorridoGarantiaActualMonedaAdicional 		= ResultadoValorizacion.MontoInteresAcumuladoMonedaAdicional;
								Tabla.INGMontoReajusteCorridoGarantiaActualMonedaAdicional 		= ResultadoValorizacion.MontoReajusteAcumuladoMonedaAdicional;
							};
							if (ResultadoValorizacion.DimensionTasa.equals("SUBY")){
								if (Tabla.EsRegistroNuevo.equalsIgnoreCase("S") || (Tabla.EsRegistroNuevo.equalsIgnoreCase("N") && Tabla.GenerarNuevoFolioCustodia != null && Tabla.GenerarNuevoFolioCustodia.equalsIgnoreCase("S"))){
									Tabla.MontoSubyacenteMonedaAdicional	= ResultadoValorizacion.MontoMonedaAdicional;										
								};
								if (Tabla.EsRegistroNuevo.equalsIgnoreCase("N")){
									Tabla.INGMontoATasaSubyacenteActual				= ResultadoValorizacion.MontoMonedaSistema;
									Tabla.INGMontoSubyacenteActualMonedaInstrumento	= ResultadoValorizacion.MontoMonedaInstrumento;
								};
								
								Tabla.INGMontoSubyacenteActualMonedaAdicional					= ResultadoValorizacion.MontoMonedaAdicional;
								Tabla.INGDurationSubyacenteActual 								= ResultadoValorizacion.Duration;
								Tabla.INGConvexidadSubyacenteActual 							= ResultadoValorizacion.Convexidad;
								Tabla.INGMontoInteresCorridoSubyacenteActual 					= ResultadoValorizacion.MontoInteresAcumuladoMonedaSistema;
								Tabla.INGMontoReajusteCorridoSubyacenteActual 					= ResultadoValorizacion.MontoReajusteAcumuladoMonedaSistema;
								Tabla.INGMontoInteresCorridoSubyacenteActualMonedaInstrumento	= ResultadoValorizacion.MontoInteresAcumuladoMonedaInstrumento;
								Tabla.INGMontoReajusteCorridoSubyacenteActualMonedaInstrumento	= ResultadoValorizacion.MontoReajusteAcumuladoMonedaInstrumento;
								Tabla.INGMontoInteresCorridoSubyacenteActualMonedaAdicional		= ResultadoValorizacion.MontoInteresAcumuladoMonedaAdicional;
								Tabla.INGMontoReajusteCorridoSubyacenteActualMonedaAdicional	= ResultadoValorizacion.MontoReajusteAcumuladoMonedaAdicional;
								
							};
							if (ResultadoValorizacion.DimensionTasa.equals("P+CM")){
								
								Tabla.INGMontoCompraActualPibMas 					= ResultadoValorizacion.MontoMonedaSistema;
								Tabla.INGMontoCompraActualPibMasMonedaInstrumento 	= ResultadoValorizacion.MontoMonedaInstrumento;
								Tabla.INGMontoCompraActualPibMasMonedaAdicional 	= ResultadoValorizacion.MontoMonedaAdicional;
							};
							if (ResultadoValorizacion.DimensionTasa.equals("P-CM")){
								
								Tabla.INGMontoCompraActualPibMenos 					= ResultadoValorizacion.MontoMonedaSistema;
								Tabla.INGMontoCompraActualPibMenosMonedaInstrumento = ResultadoValorizacion.MontoMonedaInstrumento;
								Tabla.INGMontoCompraActualPibMenosMonedaAdicional 	= ResultadoValorizacion.MontoMonedaAdicional;
							};
							if (ResultadoValorizacion.DimensionTasa.equals("P+MC")){
								
								Tabla.INGMontoMercadoActualPibMas 					= ResultadoValorizacion.MontoMonedaSistema;
								Tabla.INGMontoMercadoActualPibMasMonedaInstrumento 	= ResultadoValorizacion.MontoMonedaInstrumento;
								Tabla.INGMontoMercadoActualPibMasMonedaAdicional 	= ResultadoValorizacion.MontoMonedaAdicional;
							};
							if (ResultadoValorizacion.DimensionTasa.equals("P-MC")){
								
								Tabla.INGMontoMercadoActualPibMenos 					= ResultadoValorizacion.MontoMonedaSistema;
								Tabla.INGMontoMercadoActualPibMenosMonedaInstrumento 	= ResultadoValorizacion.MontoMonedaInstrumento;
								Tabla.INGMontoMercadoActualPibMenosMonedaAdicional 		= ResultadoValorizacion.MontoMonedaAdicional;
							};								
							if (ResultadoValorizacion.DimensionTasa.equals("P+SY")){
								
								Tabla.INGMontoSubyacenteActualPibMas 					= ResultadoValorizacion.MontoMonedaSistema;
								Tabla.INGMontoSubyacenteActualPibMasMonedaInstrumento 	= ResultadoValorizacion.MontoMonedaInstrumento;
								Tabla.INGMontoSubyacenteActualPibMasMonedaAdicional 	= ResultadoValorizacion.MontoMonedaAdicional;
							};
							if (ResultadoValorizacion.DimensionTasa.equals("P-SY")){
								
								Tabla.INGMontoSubyacenteActualPibMenos 					= ResultadoValorizacion.MontoMonedaSistema;
								Tabla.INGMontoSubyacenteActualPibMenosMonedaInstrumento = ResultadoValorizacion.MontoMonedaInstrumento;
								Tabla.INGMontoSubyacenteActualPibMenosMonedaAdicional 	= ResultadoValorizacion.MontoMonedaAdicional;
							};
							if (ResultadoValorizacion.DimensionTasa.equals("P+GT")){
								
								Tabla.INGMontoGarantiaActualPibMas 					= ResultadoValorizacion.MontoMonedaSistema;
								Tabla.INGMontoGarantiaActualPibMasMonedaInstrumento = ResultadoValorizacion.MontoMonedaInstrumento;
								Tabla.INGMontoGarantiaActualPibMasMonedaAdicional 	= ResultadoValorizacion.MontoMonedaAdicional;
							};
							if (ResultadoValorizacion.DimensionTasa.equals("P-GT")){
								
								Tabla.INGMontoGarantiaActualPibMenos 					= ResultadoValorizacion.MontoMonedaSistema;
								Tabla.INGMontoGarantiaActualPibMenosMonedaInstrumento 	= ResultadoValorizacion.MontoMonedaInstrumento;
								Tabla.INGMontoGarantiaActualPibMenosMonedaAdicional 	= ResultadoValorizacion.MontoMonedaAdicional;
							};								
							if (ResultadoValorizacion.DimensionTasa.equals("TIFF")){
								if (Tabla.EsRegistroNuevo.equalsIgnoreCase("S")){
									Tabla.TIFAfectoBeneficio 	= "";
									Tabla.TIFTasa 				=  ResultadoValorizacion.Tasa;
								};
								
								Tabla.INGMontoPresenteTIF 				= ResultadoValorizacion.MontoMonedaSistema;
								Tabla.INGMontoInteresTIF 				= ResultadoValorizacion.MontoInteresMonedaSistema;
								Tabla.INGMontoReajusteTIF 				= ResultadoValorizacion.MontoReajusteMonedaSistema;
								Tabla.INGMontoInteresAcumTIF 			= ResultadoValorizacion.MontoInteresAcumuladoMonedaSistema;
								Tabla.INGMontoReajusteAcumTIF 			= ResultadoValorizacion.MontoReajusteAcumuladoMonedaSistema;
								Tabla.INGMontoPresenteTIFMonedaInstrumento 		= ResultadoValorizacion.MontoMonedaInstrumento;
								Tabla.INGMontoInteresTIFMonedaInstrumento 		= ResultadoValorizacion.MontoInteresMonedaInstrumento;
								Tabla.INGMontoReajusteTIFMonedaInstrumento 		= ResultadoValorizacion.MontoReajusteMonedaInstrumento;
								Tabla.INGMontoInteresAcumTIFMonedaInstrumento 	= ResultadoValorizacion.MontoInteresAcumuladoMonedaInstrumento;
								Tabla.INGMontoReajusteAcumTIFMonedaInstrumento 	= ResultadoValorizacion.MontoReajusteAcumuladoMonedaInstrumento;
								Tabla.INGMontoPresenteTIFMonedaAdicional 		= ResultadoValorizacion.MontoMonedaAdicional;
								Tabla.INGMontoInteresTIFMonedaAdicional 		= ResultadoValorizacion.MontoInteresMonedaAdicional;
								Tabla.INGMontoReajusteTIFMonedaAdicional 		= ResultadoValorizacion.MontoReajusteMonedaAdicional;
								Tabla.INGMontoInteresAcumTIFMonedaAdicional 	= ResultadoValorizacion.MontoInteresAcumuladoMonedaAdicional;
								Tabla.INGMontoReajusteAcumTIFMonedaAdicional 	= ResultadoValorizacion.MontoReajusteAcumuladoMonedaAdicional;
							};
							if (ResultadoValorizacion.DimensionTasa.equals("CORR")){
								Tabla.INGFechaTasaCorredora 	= ResultadoValorizacion.FechaPrecioTasa;
								Tabla.INGTasaCorredora 			= ResultadoValorizacion.Tasa;									
								Tabla.INGMontoATasaCorredora 	= ResultadoValorizacion.MontoMonedaSistema;
								Tabla.INGMontoATasaCorredoraMonedaInstrumento 	= ResultadoValorizacion.MontoMonedaInstrumento;
								Tabla.INGMontoATasaCorredoraMonedaAdicional 	= ResultadoValorizacion.MontoMonedaAdicional;
							};

						}
						if ( ResultadoValorizacion.DimensionTiempo.equals("PR") ){
							Tabla.INGFechaValorizacionProximo 				= ResultadoValorizacion.FechaValorizacion;
							Tabla.INGFechaFactorMonedaInstrumentoProximo 	= ResultadoValorizacion.FechaFactorMonedaInstrumento;
							Tabla.INGValorFactorMonedaInstrumentoProximo 	= ResultadoValorizacion.FactorMonedaInstrumento;
							Tabla.INGFechaFactorMonedaAdicionalProximo 		= ResultadoValorizacion.FechaFactorMonedaAdicional;
							Tabla.INGValorFactorMonedaAdicionalProximo 		= ResultadoValorizacion.FactorMonedaAdicional;

							if (ResultadoValorizacion.DimensionTasa.equals("COMP")){
								Tabla.INGMontoATasaCompraProximo 						= ResultadoValorizacion.MontoMonedaSistema;
								Tabla.INGMontoInteresCompraProximo 						= ResultadoValorizacion.MontoInteresMonedaSistema;
								Tabla.INGMontoReajusteCompraProximo 					= ResultadoValorizacion.MontoReajusteMonedaSistema;
								
								Tabla.INGMontoCompraProximoMonedaInstrumento 			= ResultadoValorizacion.MontoMonedaInstrumento;
								Tabla.INGMontoInteresCompraProximoMonedaInstrumento 	= ResultadoValorizacion.MontoInteresMonedaInstrumento;
								Tabla.INGMontoReajusteCompraProximoMonedaInstrumento 	= ResultadoValorizacion.MontoReajusteMonedaInstrumento;
								
								Tabla.INGMontoCompraProximoMonedaAdicional 				= ResultadoValorizacion.MontoMonedaAdicional;
								Tabla.INGMontoInteresCompraProximoMonedaAdicional 		= ResultadoValorizacion.MontoInteresMonedaAdicional;
								Tabla.INGMontoReajusteCompraProximoMonedaAdicional 		= ResultadoValorizacion.MontoReajusteMonedaAdicional;
								
								Tabla.INGDurationCompraProximo 							= ResultadoValorizacion.Duration;
								Tabla.INGConvexidadCompraProximo	 					= ResultadoValorizacion.Convexidad;
							};
							if (ResultadoValorizacion.DimensionTasa.equals("MERC")){
								Tabla.INGFechaPrecioTasaMercadoProximo 					= ResultadoValorizacion.FechaPrecioTasa;
								Tabla.INGFuenteTasaPrecioMercadoProximo 				= ResultadoValorizacion.FuentePrecioTasa;
								Tabla.INGPrecioMercadoProximo 							= ResultadoValorizacion.Precio;
								Tabla.INGTasaMercadoProximo 							= ResultadoValorizacion.Tasa;
								Tabla.INGMontoATasaMercadoProximo 						= ResultadoValorizacion.MontoMonedaSistema;
								Tabla.INGMontoInteresMercadoProximo 					= ResultadoValorizacion.MontoInteresMonedaSistema;
								Tabla.INGMontoReajusteMercadoProximo 					= ResultadoValorizacion.MontoReajusteMonedaSistema;
								Tabla.INGMontoMercadoProximoMonedaInstrumento 			= ResultadoValorizacion.MontoMonedaInstrumento;
								Tabla.INGMontoInteresMercadoProximoMonedaInstrumento 	= ResultadoValorizacion.MontoInteresMonedaInstrumento;
								Tabla.INGMontoReajusteMercadoProximoMonedaInstrumento 	= ResultadoValorizacion.MontoReajusteMonedaInstrumento;
								Tabla.INGMontoMercadoProximoMonedaAdicional 			= ResultadoValorizacion.MontoMonedaAdicional;
								Tabla.INGMontoInteresMercadoProximoMonedaAdicional 		= ResultadoValorizacion.MontoInteresMonedaAdicional;
								Tabla.INGMontoReajusteMercadoProximoMonedaAdicional 	= ResultadoValorizacion.MontoReajusteMonedaAdicional;
								Tabla.INGDurationMercadoProximo 						= ResultadoValorizacion.Duration;
								Tabla.INGConvexidadMercadoProximo 						= ResultadoValorizacion.Convexidad;
							};
							if (ResultadoValorizacion.DimensionTasa.equals("GTIA")){
								Tabla.INGMontoATasaGarantiaProximo 						= ResultadoValorizacion.MontoMonedaSistema;
								Tabla.INGMontoInteresGarantiaProximo 					= ResultadoValorizacion.MontoInteresMonedaSistema;
								Tabla.INGMontoReajusteGarantiaProximo 					= ResultadoValorizacion.MontoReajusteMonedaSistema;
								Tabla.INGMontoGarantiaProximoMonedaInstrumento 			= ResultadoValorizacion.MontoMonedaInstrumento;
								Tabla.INGMontoInteresGarantiaProximoMonedaInstrumento 	= ResultadoValorizacion.MontoInteresMonedaInstrumento;
								Tabla.INGMontoReajusteGarantiaProximoMonedaInstrumento	= ResultadoValorizacion.MontoReajusteMonedaInstrumento;
								Tabla.INGMontoGarantiaProximoMonedaAdicional 			= ResultadoValorizacion.MontoMonedaAdicional;
								Tabla.INGMontoInteresGarantiaProximoMonedaAdicional 	= ResultadoValorizacion.MontoInteresMonedaAdicional;
								Tabla.INGMontoReajusteGarantiaProximoMonedaAdicional	= ResultadoValorizacion.MontoReajusteMonedaAdicional;
								Tabla.INGDurationGarantiaProximo 						= ResultadoValorizacion.Duration;
								Tabla.INGConvexidadGarantiaProximo						= ResultadoValorizacion.Convexidad;
							};
							if (ResultadoValorizacion.DimensionTasa.equals("SUBY")){
								Tabla.INGMontoATasaSubyacenteProximo 						= ResultadoValorizacion.MontoMonedaSistema;
								Tabla.INGMontoInteresSubyacenteProximo 						= ResultadoValorizacion.MontoInteresMonedaSistema;
								Tabla.INGMontoReajusteSubyacenteProximo 					= ResultadoValorizacion.MontoReajusteMonedaSistema;
								Tabla.INGMontoSubyacenteProximoMonedaInstrumento			= ResultadoValorizacion.MontoMonedaInstrumento;
								Tabla.INGMontoInteresSubyacenteProximoMonedaInstrumento		= ResultadoValorizacion.MontoInteresMonedaInstrumento;
								Tabla.INGMontoReajusteSubyacenteProximoMonedaInstrumento 	= ResultadoValorizacion.MontoReajusteMonedaInstrumento;
								Tabla.INGMontoSubyacenteProximoMonedaAdicional 				= ResultadoValorizacion.MontoMonedaAdicional;
								Tabla.INGMontoInteresSubyacenteProximoMonedaAdicional 		= ResultadoValorizacion.MontoInteresMonedaAdicional;
								Tabla.INGMontoReajusteSubyacenteProximoMonedaAdicional		= ResultadoValorizacion.MontoReajusteMonedaAdicional;
								Tabla.INGDurationSubyacenteProximo 							= ResultadoValorizacion.Duration;
								Tabla.INGConvexidadSubyacenteProximo 						= ResultadoValorizacion.Convexidad;
							};
						}

						if ( ResultadoValorizacion.DimensionTiempo.equals("ACUM") ){
							if (ResultadoValorizacion.DimensionTasa.equals("COMP")){
								Tabla.INGMontoInteresCorridoCompraActual					= ResultadoValorizacion.MontoInteresAcumuladoAPlazoMonedaSistema;
								Tabla.INGMontoReajusteCorridoCompraActual					= ResultadoValorizacion.MontoReajusteMonedaSistema;  
								Tabla.INGMontoInteresCorridoCompraActualMonedaInstrumento	= ResultadoValorizacion.MontoInteresAcumuladoAPlazoMonedaInstrumento;
								Tabla.INGMontoReajusteCorridoCompraActualMonedaInstrumento	= ResultadoValorizacion.MontoReajusteMonedaInstrumento;
								Tabla.INGMontoInteresCorridoCompraActualMonedaAdicional 	= ResultadoValorizacion.MontoInteresAcumuladoAPlazoMonedaAdicional;
								Tabla.INGMontoReajusteCorridoCompraActualMonedaAdicional	= ResultadoValorizacion.MontoReajusteMonedaAdicional;
							};
							if (ResultadoValorizacion.DimensionTasa.equals("MERC")){								
								Tabla.INGMontoInteresCorridoMercadoActual 					= ResultadoValorizacion.MontoInteresAcumuladoAPlazoMonedaSistema;
								Tabla.INGMontoReajusteCorridoMercadoActual 					= ResultadoValorizacion.MontoReajusteMonedaSistema;
								Tabla.INGMontoInteresCorridoMercadoActualMonedaInstrumento 	= ResultadoValorizacion.MontoInteresAcumuladoAPlazoMonedaInstrumento;
								Tabla.INGMontoReajusteCorridoMercadoActualMonedaInstrumento = ResultadoValorizacion.MontoReajusteMonedaInstrumento;
								Tabla.INGMontoInteresCorridoMercadoActualMonedaAdicional 	= ResultadoValorizacion.MontoInteresAcumuladoAPlazoMonedaAdicional;
								Tabla.INGMontoReajusteCorridoMercadoActualMonedaAdicional 	= ResultadoValorizacion.MontoReajusteMonedaAdicional;
							};
						}
						
					};//////////if (Tabla.OrigenProcesamiento.equalsIgnoreCase("INGRESO_CUSTODIA")){
					
					if (Tabla.OrigenProcesamiento.equalsIgnoreCase("RETIRO_CUSTODIA")){
						if (ResultadoValorizacion.ResultadoProceso == null) continue;
						Tabla.EGRTicketValorizacion 		= ResultadoValorizacion.TicketProcesamiento;
						Tabla.EGRValorizacionConProblemas 	= ResultadoValorizacion.ResultadoProceso.equals("OK") ? "N" : "S";
						
						if ( ResultadoValorizacion.DimensionTiempo.equals("AC") ){
							
							if (ResultadoValorizacion.DimensionTasa.equals("COMP")){
								
								Tabla.EGRFechaFactorMonedaInstrumento 	= ResultadoValorizacion.FechaFactorMonedaInstrumento;
								Tabla.EGRValorFactorMonedaInstrumento 	= ResultadoValorizacion.FactorMonedaInstrumento;
								Tabla.EGRFechaFactorMonedaAdicional 	= ResultadoValorizacion.FechaFactorMonedaAdicional;
								Tabla.EGRValorFactorMonedaAdicional 	= ResultadoValorizacion.FactorMonedaAdicional;
								//AL SER GARANTIA, EL MONTO VALORIZADO ES CALCULADO RESPECTO A LOS PRECIOS Y TASAS DE LA GARANTIA CREADA
								//SE REQUIERE MANTENER LOS MONTOS RESPECTO AL PRECIO Y TASA DE COMPRA ORIGINAL DE LA GARANTIA
								if (CAR.size()>0) {
									if("GAPRFG".equalsIgnoreCase(CAR.get(0).EGRIdConcepto) && "INGGTA".equalsIgnoreCase(CAR.get(0).EGRIdTipoOperacion)) {
										Tabla.EGRMonto						= (CAR.get(0).INGMontoATasaCompraActual.multiply(ResultadoValorizacion.Cantidad)).divide(CAR.get(0).INGCantidad);
									}else {
										Tabla.EGRMonto 						= ResultadoValorizacion.MontoMonedaSistema;
									}
								}else {
									Tabla.EGRMonto 						= ResultadoValorizacion.MontoMonedaSistema;
								}
								Tabla.EGRMontoMonedaInstrumento 		= ResultadoValorizacion.MontoMonedaInstrumento;
								Tabla.EGRMontoMonedaAdicional 			= ResultadoValorizacion.MontoMonedaAdicional;
								Tabla.EGRPrecioAPlazo					= ResultadoValorizacion.PrecioAPlazo;
								Tabla.EGRMontoAPlazo					= ResultadoValorizacion.MontoAPlazoMonedaSistema;
								Tabla.EGRMontoAPlazoMonedaInstrumento	= ResultadoValorizacion.MontoAPlazoMonedaInstrumento; 									
								Tabla.EGRMontoAPlazoMonedaAdicional 	= ResultadoValorizacion.MontoAPlazoMonedaAdicional;
								/*
				                if (InstrumentoAValorizar.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF")){
				                	Tabla.EGRSpread 					= InstrumentoAValorizar.MontoCompraMonedaSistema.subtract(InfoCartera.INGMontoATasaCompraActual.multiply(InstrumentoAValorizar.ValorRescate).divide(InfoCartera.INGValorRescate,20,RoundingMode.HALF_UP));
				                }
				                else {
				                	Tabla.EGRSpread 					= InstrumentoAValorizar.MontoCompraMonedaSistema.subtract(InfoCartera.INGMontoATasaCompraActual.multiply(InstrumentoAValorizar.Cantidad).divide(InfoCartera.INGCantidad,20,RoundingMode.HALF_UP));
				                }*/
								Tabla.EGRSpread 						= BigDecimal.ZERO;
								if (InstrumentoAValorizar.TablaOrigen.equals("CARTERA")){
									if (InstrumentoAValorizar.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF")){
										// MSH - Pactos vienen con estos datos en null (cabecera)
										if (ResultadoValorizacion.MontoMonedaSistema != null ) {
											Tabla.EGRSpread 				= ResultadoValorizacion.MontoMonedaSistema.subtract(InfoCartera.INGMontoATasaCompraActual.multiply(InstrumentoAValorizar.ValorRescate).divide(InfoCartera.INGValorRescate,20,RoundingMode.HALF_UP));
										}
					                }
					                else {
										// MSH - Anticipos ??
										if (ResultadoValorizacion.MontoMonedaSistema != null ) {
											Tabla.EGRSpread 				= ResultadoValorizacion.MontoMonedaSistema.subtract(InfoCartera.INGMontoATasaCompraActual.multiply(InstrumentoAValorizar.Cantidad).divide(InfoCartera.INGCantidad,20,RoundingMode.HALF_UP));
										}
					                }
									// MSH - Para pactos este dato viene en blanco									
									if ( ResultadoValorizacion.IdMonedaInstrumento == null || ResultadoValorizacion.IdMonedaInstrumento.equals("") ) {
										ResultadoValorizacion.IdMonedaInstrumento = ResultadoValorizacion.IdMonedaSistema;
									}
									Tabla.EGRSpread = MonedaFormato.formatear(con, Tabla.EGRSpread, MonedaFormato.MONTO, ResultadoValorizacion.IdMonedaInstrumento).valor;
								}
							};
							if (ResultadoValorizacion.DimensionTasa.equals("MERC")){
								
								Tabla.EGRFechaPrecioTasaMercado 		= ResultadoValorizacion.FechaPrecioTasa;
								Tabla.EGRFuenteTasaPrecioMercado 		= ResultadoValorizacion.FuentePrecioTasa;
								Tabla.EGRPrecioMercado 					= ResultadoValorizacion.Precio;
								Tabla.EGRTasaMercado 					= ResultadoValorizacion.Tasa;
								Tabla.EGRMontoATasaMercado 				= ResultadoValorizacion.MontoMonedaSistema;
								Tabla.EGRMontoMercadoMonedaInstrumento 	= ResultadoValorizacion.MontoMonedaInstrumento;
								Tabla.EGRMontoMercadoMonedaAdicional 	= ResultadoValorizacion.MontoMonedaAdicional;
								//Tabla.EGRSpreadMercado 					= ResultadoValorizacion.MontoMonedaSistema != null ? InstrumentoAValorizar.MontoMercadoMonedaSistema.subtract(ResultadoValorizacion.MontoMonedaSistema) : null;
								/*
								if (InstrumentoAValorizar.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF")){
				                	Tabla.EGRSpreadMercado				= InstrumentoAValorizar.MontoMercadoMonedaSistema.subtract(InfoCartera.INGMontoATasaMercadoActual.multiply(InstrumentoAValorizar.ValorRescate).divide(InfoCartera.INGValorRescate,20,RoundingMode.HALF_UP));
				                }
				                else {
				                	Tabla.EGRSpreadMercado				= InstrumentoAValorizar.MontoMercadoMonedaSistema.subtract(InfoCartera.INGMontoATasaMercadoActual.multiply(InstrumentoAValorizar.Cantidad).divide(InfoCartera.INGCantidad,20,RoundingMode.HALF_UP));
				                }*/
								if (InstrumentoAValorizar.TablaOrigen.equals("CARTERA")){
									if (InstrumentoAValorizar.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF")){
										// MSH - Pactos vienen con estos datos en null (cabecera)
										Tabla.EGRSpreadMercado 				= BigDecimal.ZERO;
										if (ResultadoValorizacion.MontoMonedaSistema != null ) {
					                	Tabla.EGRSpreadMercado				= ResultadoValorizacion.MontoMonedaSistema.subtract(InfoCartera.INGMontoATasaMercadoActual.multiply(InstrumentoAValorizar.ValorRescate).divide(InfoCartera.INGValorRescate,20,RoundingMode.HALF_UP));
					                }
					                }
					                else {
					                	Tabla.EGRSpreadMercado				= ResultadoValorizacion.MontoMonedaSistema != null ? ResultadoValorizacion.MontoMonedaSistema.subtract(InfoCartera.INGMontoATasaMercadoActual.multiply(InstrumentoAValorizar.Cantidad).divide(InfoCartera.INGCantidad,20,RoundingMode.HALF_UP)): InfoCartera.INGMontoATasaMercadoActual.multiply(InstrumentoAValorizar.Cantidad).divide(InfoCartera.INGCantidad,20,RoundingMode.HALF_UP);
					                }
									
									// MSH - Para pactos este dato viene en blanco
									if ( ResultadoValorizacion.IdMonedaInstrumento == null || ResultadoValorizacion.IdMonedaInstrumento.equals("") ) {
										ResultadoValorizacion.IdMonedaInstrumento = ResultadoValorizacion.IdMonedaSistema;
									}
									Tabla.EGRSpreadMercado = MonedaFormato.formatear(con, Tabla.EGRSpreadMercado, MonedaFormato.MONTO, ResultadoValorizacion.IdMonedaInstrumento).valor;
								}
							};
							if (ResultadoValorizacion.DimensionTasa.equals("GTIA")){
								Tabla.EGRMonto 							= ResultadoValorizacion.MontoMonedaSistema;
								Tabla.EGRMontoMonedaInstrumento 		= ResultadoValorizacion.MontoMonedaInstrumento;
								Tabla.EGRMontoMonedaAdicional 			= ResultadoValorizacion.MontoMonedaAdicional;
							};
							if (ResultadoValorizacion.DimensionTasa.equals("SUBY")){									
							};
							if (ResultadoValorizacion.DimensionTasa.equals("TIFF")){

								Tabla.EGRMontoPresenteTIF 					= ResultadoValorizacion.MontoMonedaSistema;
								Tabla.EGRMontoPresenteTIFMonedaInstrumento 	= ResultadoValorizacion.MontoMonedaInstrumento;
								Tabla.EGRMontoPresenteTIFMonedaAdicional 	= ResultadoValorizacion.MontoMonedaAdicional;									
							};
						};					
						
					};//////////if (Tabla.OrigenProcesamiento.equalsIgnoreCase("RETIRO_CUSTODIA")){					
				}; //for (ValorizarInstrumento
				
				if (InstrumentoAValorizar.TablaOrigen.equalsIgnoreCase("CARTERA") ){
					paramsUpdCartera.add(Tabla);
				};
				
				if(InstrumentoAValorizar.TablaOrigen.equalsIgnoreCase("SUBYACENTE")) {
					paramsUpdCarteraPactoSubyacente.add(Tabla);
				}

				
			}
			logger.debug(" Armado Valores Adicionales");
			List<outValorizacionInserts> OutInserts = new ArrayList<outValorizacionInserts>();
			List<String> TipoRegistro = new ArrayList<String>();

			int opcion;
			for (ObjUpdateTablas element : paramsUpdCartera) {
				System.out.println(element.toString());
				for (TipoRegistro tipoRegistroObj : tipoRegistro) {
				opcion = tipoRegistroObj.IdTipoRegistro.intValue();
				
					switch(opcion) {
						case 1 :{
							outValorizacionInserts obj1 = new outValorizacionInserts();
							obj1.IdCartera = element.IdCartera;
							obj1.Fecha = tFechaHoraSistema;
							obj1.Ejecucion = new Date()+"";
							obj1.IdTipoRegistro = tipoRegistroObj.Valor;
							obj1.Valor = (element.INGMontoATasaCompraActual.divide((BigDecimal) element.INGValorFactorMonedaInstrumentoActual, 4, RoundingMode.HALF_UP)+"");
							OutInserts.add(obj1);
							
							if(!TipoRegistro.contains(tipoRegistroObj.Valor)) {
								TipoRegistro.add(tipoRegistroObj.Valor);
							}							
							break;
						}
						case 2 :{							
							if( element.INGFechaCorteCupon != null) { //comp act
								if(element.INGFechaCorteCupon.compareTo(Fecha.toDate("0001-01-01") ) > 0){ 
									outValorizacionInserts obj1 = new outValorizacionInserts();
									BigDecimal frecuencia =  frecuenciasCarterasHash.get(element.IdCartera);
									obj1.IdCartera = element.IdCartera;
									obj1.Fecha = tFechaHoraSistema;
									obj1.IdTipoRegistro = tipoRegistroObj.Valor;
									LocalDate fechaCuponSinResta = LocalDate.parse(element.INGFechaCorteCupon+"");
									fechaCuponSinResta =  fechaCuponSinResta.minusMonths(frecuencia!= null ?frecuencia.longValue() : 0L);
									obj1.Valor = fechaCuponSinResta+"";									
									OutInserts.add(obj1);									
									if(!TipoRegistro.contains(tipoRegistroObj.Valor)) {
										TipoRegistro.add(tipoRegistroObj.Valor);
									}									
								}
							}							
							break;
						}	
						default :{
							System.out.println("Caso no abordado verificar aplicativo "+tipoRegistro.toString());
							break;
						}
					}
				};//end for registro				
			}//end for tipo carteras
			
			for(ObjUpdateTablas element : paramsUpdCarteraPactoSubyacente) {
				outValorizacionInserts obj1 = new outValorizacionInserts();
				
				obj1.IdCartera = element.IdSubyacente;
				obj1.Fecha = tFechaHoraSistema;
				obj1.Ejecucion = new Date()+"";
				obj1.IdTipoRegistro = "ValorPactoSubyacenteTasaCompra";
				obj1.Valor = element.INGMontoATasaCompraActual+"";
				OutInserts.add(obj1);
				
				if(!TipoRegistro.contains("ValorPactoSubyacenteTasaCompra")) {
					TipoRegistro.add("ValorPactoSubyacenteTasaCompra");
				}
			}

			if(OutInserts.size()>0) {
				try {
					logger.debug(" Drop Valores Adicionales antiguos");
					for(String registro : TipoRegistro) {
						if(registro.equals("ValorPactoSubyacenteTasaCompra")) {
							SQL.exec(con, SQL_DropValoresAdicionalesPactosSubyacentes.replace(":Fecha","'"+tFechaHoraSistema+"'")
									.replace(":IdTipoRegistro","'"+registro+"'"));
						}else if(registro.equals("FechaAnteriorCupon") || registro.equals("ValorParATasaEmision")){
							SQL.exec(con, SQL_DropValoresAdicionales.replace(":IdTipoRegistro","'"+registro+"'"));
						}
					}
	
					logger.debug(" insert Valores Adicionales nuevos");
				
					out.P_OUT_MENSAJE= out.P_OUT_MENSAJE=  out.P_OUT_MENSAJE.concat("\n"+OutInserts.size()+ " Carteras respaldadas") ;	
					sql.batch(con, SQL_InsertValoresAdicionales, OutInserts, outValorizacionInserts.class);		
				}catch(Exception e) {
					out.P_OUT_ERRORESCONTADOR = out.P_OUT_ERRORESCONTADOR  + 1L;
					out.P_OUT_MENSAJE= out.P_OUT_MENSAJE=  out.P_OUT_MENSAJE.concat("\n Error al respaldar informacion") ;					
					return out;
				}	
			}

		}else{
			out.P_OUT_ERRORESCONTADOR = out.P_OUT_ERRORESCONTADOR  + 1L;
			out.P_OUT_MENSAJE=  out.P_OUT_MENSAJE = "No Existen Instrumentos a valorizar";
			return out;
		
		};
		return out;
	};

	private String obtenerParametroUsoTasaRecompraVentaConPacto(Connection con, Long idEmpresa) throws Exception {
		logger.trace("INICIO::ValorizacionValoresAdicionalesBatch.obtenerParametroUsoTasaRecompraVentaConPacto");
		String usaTasaReccompraVentaConPacto = "N";
		try {
			usaTasaReccompraVentaConPacto = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "MESA", "TASARECOMPRAVENTACONPACTO");
		}catch(Exception e) {
			logger.warn("ADVERTENCIA::ValorizacionValoresAdicionalesBatch.obtenerParametroUsoTasaRecompraVentaConPacto::no existe parametro TASARECOMPRAVENTACONPACTO, se deja valor por defecto.");
		}
		
		logger.trace("FIN::ValorizacionValoresAdicionalesBatch.obtenerParametroUsoTasaRecompraVentaConPacto");
		return usaTasaReccompraVentaConPacto;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		InValorizacionValoresAdicionales jin=gson.fromJson(in.toString() ,InValorizacionValoresAdicionales.class);
		OutValorizacionValoresAdicionales out=logica(conn,jin);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	public static void main(String[] args) throws Exception {
		try (Connection con =  PlataformaUtils.getDESAConnection()  ) {
			try {
				InValorizacionValoresAdicionales entrada = new InValorizacionValoresAdicionales();			
				entrada.p_IdEmpresa = 1L;		
				entrada.p_IdUsuario = 97L;
				entrada.p_IdModulo = "MRF";
				entrada.p_FechaSistema = "2025-11-30";
				entrada.p_IdPais = 44L;
				entrada.p_IdPortafolio = 0L; //512341 Juanjo portafolio 1
				entrada.p_ValorizaCarteraParaInterfaces = "S";
				entrada.p_ValorizaPactosSubyacenteTasaCompra = "N";
			    ValorizacionValoresAdicionalesBatch  kk = new ValorizacionValoresAdicionalesBatch();
			    ValorizacionValoresAdicionalesBatch.OutValorizacionValoresAdicionales salida= kk.logica(con, entrada);
			    System.out.println("TERMINADO");
			    con.commit();
	    	}catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
		} catch (Exception e) {
			e.printStackTrace();			
		}	
	};
}
