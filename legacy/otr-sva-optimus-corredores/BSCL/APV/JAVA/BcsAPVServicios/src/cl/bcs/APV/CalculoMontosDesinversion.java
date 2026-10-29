package cl.bcs.APV;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cl.bcs.fondomutuo.CalcularMontoNumeroCuota.ValorCuotaTipoParidad;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.plataforma.tas.EntregarPreciosTasas;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Entrada;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Entrada_DatosInstrumentos;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Salida;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Salida_PreciosTasas;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida;

public class CalculoMontosDesinversion implements JavaCall   {

	/*
	 * Queries que van a ser utilizadas por el servicio. 
	 */	
	public String SQL_CREAR_Recaudacion = loadResourceToString("cl/bcs/APV/sql/Deposito_Insert.sql");
	public String SQL_MODIFICAR_Recaudacion = loadResourceToString("cl/bcs/APV/sql/Deposito_Update.sql");
	private final String SQL_SELECT_VALOR_CUOTA_TIPO_CAMBIO = loadResourceToString("cl/bcs/fondomutuo/sql/CalcularMontoNumeroCuota_SelectValorCuotaTipoCambio.sql");
	public String SQL_SELECT_DETALLE_TRASPASO_EGRESO_DESINVERSION = loadResourceToString("cl/bcs/APV/sql/ObtenerDetalleTraspasoDesinversion.sql");

	
	/********************
	 * Clases IN/OUT	*
	 ********************/
	
	public static class Custodia{
		public Long			IdRegistro;
		public String 		NombreProductoAgrupador;
		public String 		Regimen;
		public String 		IdClasificacionTipoInstrumento;
		public String 		IdTipoInstrumento;
		public String 		Nemotecnico;
		public BigDecimal	CantidadCustodia;
		public BigDecimal	PPPCustodia;
		public BigDecimal	ValorPPPCustodia;
		public BigDecimal	MontoPPPCustodia;	
		public BigDecimal	CantidadADesinvertir;
		public BigDecimal	MontoADesinvertir;
		public BigDecimal 	MontoADesinvertirCLP;
		public Long			IdPortafolio;
		//public BigDecimal	PrecioMercadoCustodia;
		//public BigDecimal	MontoMercadoCustodia;	
		public String		IdEmisor;
		public Long			IdInstrumento;
		public String		IdMonedaInstrReajuste;		

		/* --- NUEVAS COLUMNAS --- */
		public String 		DocumentoIdentidad;
		public String 		DocumentoIdentidadRutCavali;
        public String 		NombreCliente;
        public String 		Relacion;
		public String 		ISIN;
		public String 		ClasificacionRiesgoInstrumento;
		public String 		EsFungible;
		public String 		EsPagaderoEn;
		public String 		IdBoveda;
		public String 		NumeroCuentaBoveda;
		public BigDecimal 	TotalPosicion;
		public BigDecimal 	TotalPosicionPresunta;
		public BigDecimal 	Rentabilidad;
		
		public BigDecimal PrecioMercadoCustodia;
		public BigDecimal ValorMercadoCustodia;
		public BigDecimal MontoMercadoCustodia;
		public BigDecimal DiferenciaMercadoCustodia;
		
		public BigDecimal CantidadGarantia;
		public BigDecimal PrecioMercadoGarantia;
		public BigDecimal ValorMercadoGarantia;
		public BigDecimal DiferenciaMercadoGarantia;
		
		public BigDecimal CantidadSimulCom;
		public BigDecimal PrecioMercadoSimulCom;
		public BigDecimal ValorMercadoSimulCom;
		public BigDecimal DiferenciaMercadoSimulCom;
		
		public BigDecimal CantidadSimulVen;
		public BigDecimal PrecioMercadoSimulVen;
		public BigDecimal ValorMercadoSimulVen; 
		public BigDecimal DiferenciaMercadoSimulVen;
		
		public BigDecimal CantidadPrestamo;
		public BigDecimal PrecioMercadoPrestamo;
		public BigDecimal ValorMercadoPrestamo;
		public BigDecimal DiferenciaMercadoPrestamo;
		
		public BigDecimal CantidadEmpresti;
		public BigDecimal PrecioMercadoEmpresti;
		public BigDecimal ValorMercadoEmpresti;
		public BigDecimal DiferenciaMercadoEmpresti;
		
		public BigDecimal CantidadPactosV;
		public BigDecimal PrecioMercadoPactosV;
		public BigDecimal ValorMercadoPactosV;
		public BigDecimal DiferenciaMercadoPactosV;
		
		public BigDecimal CantidadPactosC;
		public BigDecimal PrecioMercadoPactosC;
		public BigDecimal ValorMercadoPactosC;
		public BigDecimal DiferenciaMercadoPactosC;
		
		public BigDecimal CantidadReporteCompra;
		public BigDecimal PrecioMercadoReporteCompra;
		public BigDecimal ValorMercadoReporteCompra;
		public BigDecimal DiferenciaMercadoReporteCompra;
		
		public BigDecimal CantidadReporteVenta;
		public BigDecimal PrecioMercadoReporteVenta;
		public BigDecimal ValorMercadoReporteVenta;
		public BigDecimal DiferenciaMercadoReporteVenta;
		
		public BigDecimal CantidadTransIng;
		public BigDecimal CantidadTransEgr;
		
		/*Datos para Obtener Precio Mercado*/
		public Long 		IdInstrumentoCustodia;
		public String		IdMonedaInstrumento;
		public String		FechaOperacion;
	}
	
	public static class DetalleTraspasoDesinversion{
		public Long   		IdTraspaso;
		public BigDecimal	MontoTraspaso;
		public BigDecimal 	PorcentajeTraspaso;
		public Long 		IdInstrumento;	
		public String 		CodigoRegimenTributario;
		public Long 		IdPortafolio;
		public BigDecimal   UFTraspaso;
		public String 		IdMoneda;
		public BigDecimal  	MontoCalculadoUF;
		public BigDecimal 	MontoCalculadoCLP;
		public DetalleTraspasoDesinversion() {
			IdTraspaso=0L;
			IdInstrumento=0L;
			CodigoRegimenTributario="";
			IdPortafolio=0L;
			UFTraspaso = BigDecimal.ZERO;
	    }
	};
	
	
	//  SERVICIO ENTRADA
	public static class ParametrosEntradaServicio {
		
		public 	Long		p_IdEmpresa;
		public	Long		p_IdUsuario;
		public 	String		p_IdModulo;
		public	Long		p_TotalADesinvertir;
		public	BigDecimal	p_PorcentajeADesinvertir;
		public  Long		p_CantidadADesinvertir;
		public  String		p_Redondeo; // ROUND = Arriba o Abajo, UP = Arriba, DOWN = Abajo.
		public  Long        p_IdTraspaso;
		
		public List<Custodia>	p_Custodia;
		
		public ParametrosEntradaServicio(){
			p_Custodia = new ArrayList<Custodia>();	
			p_IdTraspaso= 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	

	// SERVICIO SALIDA
	public static class ParametrosSalidaServicio {		
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		public BigDecimal			P_OUT_MONTO_TOTAL;
		public List<Custodia>		P_OUT_CUSTODIA;
		public BigDecimal			P_OUT_PORCENTAJE;

		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public ParametrosSalidaServicio(){
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}		
	};

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		ParametrosEntradaServicio jin = gson.fromJson(in.toString() ,ParametrosEntradaServicio.class);
		ParametrosSalidaServicio out = logica(conn,jin);		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	/************************
	 * Métodos Básicos	*	
	 ************************/
	private List<Custodia> convertirMontos(Connection con, List<Custodia> custodia,BigDecimal monto, String idMonedaSistema, Long idEmpresa, String fecha) throws Exception{
		List<CalculoEntrada> entradaParidadMontoUF = custodia.stream()
				.filter(detalle -> !idMonedaSistema.equalsIgnoreCase(detalle.IdMonedaInstrumento))
				.map(detalle -> {
					CalculoEntrada calculoEntradaMontoUF = new CalculoEntrada();
					calculoEntradaMontoUF.IdTrabajo			= 0L;
					calculoEntradaMontoUF.Fecha				= fecha;
					calculoEntradaMontoUF.IdInterno			= detalle.IdRegistro;
					calculoEntradaMontoUF.IdMonedaOrigen	= idMonedaSistema;
					calculoEntradaMontoUF.IdMonedaDestino	= detalle.IdMonedaInstrumento;
					calculoEntradaMontoUF.MontoOrigen		= monto;
					return calculoEntradaMontoUF;
				})
				.distinct()
				.collect(Collectors.toList());
		
		List<CalculoSalida> salidaParidadMontoUF = obtenerCalculoParidad(con, idEmpresa, entradaParidadMontoUF);
		HashMap<Long, Object> hashParidadMontoUF = Utils.List2Hash(salidaParidadMontoUF,"IdInterno");
		custodia = custodia.stream().map(detalle ->{
			CalculoSalida calculoSalidaMontoUF = (CalculoSalida) hashParidadMontoUF.get(detalle.IdRegistro);
			BigDecimal montoDestino = calculoSalidaMontoUF != null ?  calculoSalidaMontoUF.MontoDestino: BigDecimal.ZERO;
			detalle.MontoADesinvertir=montoDestino;
			return detalle;
		}).collect(Collectors.toList());
		return custodia;
	}
	private Custodia convertirMontos(Connection con, Custodia custodia,BigDecimal monto, String idMonedaSistema, Long idEmpresa, String fecha,Boolean transformarCLP) throws Exception{
		List<CalculoEntrada> entradaParidadMontoUF = new ArrayList<CalculoEntrada>();
		CalculoEntrada calculoEntradaMontoUF = new CalculoEntrada();
		calculoEntradaMontoUF.IdTrabajo			= 0L;
		calculoEntradaMontoUF.Fecha				= fecha;
		calculoEntradaMontoUF.IdInterno			= custodia.IdRegistro;
		calculoEntradaMontoUF.IdMonedaOrigen	= transformarCLP?custodia.IdMonedaInstrumento:idMonedaSistema;
		calculoEntradaMontoUF.IdMonedaDestino	= transformarCLP?idMonedaSistema:custodia.IdMonedaInstrumento;
		calculoEntradaMontoUF.MontoOrigen		= monto;
		entradaParidadMontoUF.add(calculoEntradaMontoUF);
		
		List<CalculoSalida> salidaParidadMontoUF = obtenerCalculoParidad(con, idEmpresa, entradaParidadMontoUF);
		HashMap<Long, Object> hashParidadMontoUF = Utils.List2Hash(salidaParidadMontoUF,"IdInterno");
		
		CalculoSalida calculoSalidaMontoUF = (CalculoSalida) hashParidadMontoUF.get(custodia.IdRegistro);
		BigDecimal montoDestino = calculoSalidaMontoUF != null ? (transformarCLP ? calculoSalidaMontoUF.MontoDestino.setScale(0, RoundingMode.HALF_DOWN):calculoSalidaMontoUF.MontoDestino) : BigDecimal.ZERO;
		if(transformarCLP)
			custodia.MontoADesinvertirCLP=montoDestino;
		else
			custodia.MontoADesinvertir=montoDestino;
		return custodia;
	}
	
	private List<CalculoSalida> obtenerCalculoParidad (Connection con,Long idEmpresa, List<CalculoEntrada> paridades) throws Exception{
		List<CalculoSalida> paridadesConvertidas = new ArrayList<>();
		CalcularParidad calcularParidad = new CalcularParidad();
		CalcularParidadEntrada entradaCalcularParidad = new CalcularParidadEntrada();
		entradaCalcularParidad.p_IdEmpresa = idEmpresa;
		entradaCalcularParidad.p_IdTrabajo = 0L;
		entradaCalcularParidad.p_CalculosEntrada = paridades;
		CalcularParidadSalida salidaCalculoParidad = calcularParidad.logica(con, entradaCalcularParidad);
		if( salidaCalculoParidad.P_OUT_ERRORESCONTADOR.equals(0L) && !salidaCalculoParidad.p_out_CalculosSalida.isEmpty()) {
			paridadesConvertidas = salidaCalculoParidad.p_out_CalculosSalida;
		}
		return paridadesConvertidas;
	}
	
	public ParametrosSalidaServicio logica(Connection con,ParametrosEntradaServicio in)throws Exception {
				
		ParametrosSalidaServicio 	salida 			= new ParametrosSalidaServicio ();
		MensajeSalida               msg             = new MensajeSalida();
		
		BigDecimal CantidadTotal 		= new BigDecimal(0);
		BigDecimal MontoTotalPatrimonio = new BigDecimal(0);
		BigDecimal MontoTotal = new BigDecimal(0);
		List<Custodia> ListCustodia =  new ArrayList<Custodia>();
		List<String> listInstrumentos = new ArrayList<String>();
		String idMonedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresaService.logica(con, in.p_IdEmpresa);
		Long idPaisEmpresa = empresa.IdPais;
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa,
				"PLATAFORMA", "PLA.FechaSistema");
		//PRECIO MERCADO
		{	
			EntregarPreciosTasas_Entrada entregarPreciosTasasEntrada = new EntregarPreciosTasas_Entrada();
			
			entregarPreciosTasasEntrada.p_IdEmpresa = in.p_IdEmpresa;
			entregarPreciosTasasEntrada.p_IdModulo = in.p_IdModulo;
			entregarPreciosTasasEntrada.p_IdPais = idPaisEmpresa;		
			entregarPreciosTasasEntrada.p_IdUsuario = in.p_IdUsuario;
			
			
			for (Custodia cust : in.p_Custodia) {
				if ((cust.IdClasificacionTipoInstrumento.equalsIgnoreCase("RV") && !cust.IdTipoInstrumento.equalsIgnoreCase("SIM"))) {
					EntregarPreciosTasas_Entrada_DatosInstrumentos entradaDatosInstrumentos = new EntregarPreciosTasas_Entrada_DatosInstrumentos();
					entradaDatosInstrumentos.IdRegistro = cust.IdRegistro;
					entradaDatosInstrumentos.TipoCartera = "0";
					entradaDatosInstrumentos.IdUso = "CARTERA";
					entradaDatosInstrumentos.IdTipoCriterioTasa = "0";
					entradaDatosInstrumentos.IdClasificacionTipoInstrumento = "RV";
					entradaDatosInstrumentos.IdTipoInstrumento = "0";
					entradaDatosInstrumentos.IdInstrumento = cust.IdInstrumento;
					entradaDatosInstrumentos.Nemotecnico = cust.Nemotecnico;
					entradaDatosInstrumentos.IdMoneda = cust.IdMonedaInstrumento;
					entradaDatosInstrumentos.FechaConsulta = Fecha.toDate(cust.FechaOperacion);  
					entradaDatosInstrumentos.PlazoResidual = 0L;
					entregarPreciosTasasEntrada.p_DatosInstrumentos.add(entradaDatosInstrumentos);
				}
			}
			
			if (entregarPreciosTasasEntrada.p_DatosInstrumentos.size() > 0) {
				EntregarPreciosTasas entregarPreciosTasasService = new EntregarPreciosTasas();
				EntregarPreciosTasas_Salida entregarPreciosTasasSalida = entregarPreciosTasasService.logica(con, entregarPreciosTasasEntrada);
				HashMap<String, Object> precios = Utils.List2Hash(entregarPreciosTasasSalida.P_OUT_PRECIOSTASAS,"IdRegistro");
				
				for (Custodia CustodiaIn : in.p_Custodia) {
					if (!CustodiaIn.IdClasificacionTipoInstrumento.equalsIgnoreCase("RE")){
						EntregarPreciosTasas_Salida_PreciosTasas registroPrecio = (EntregarPreciosTasas_Salida_PreciosTasas) precios.get(CustodiaIn.IdRegistro);
						if (registroPrecio != null && registroPrecio.Precio != null && registroPrecio.Precio.doubleValue() > 0) {
							CustodiaIn.PrecioMercadoCustodia = registroPrecio.Precio;
						}
					}
				}
			}
		}
		
		
		if(in.p_Custodia.size() > 0){
			
			try{		
				
				String IndicadorRedondeo = ConfiguracionSistema.getParametroConfiguracionString(con,in.p_IdEmpresa, "PLATAFORMA","APV.IndicadorRedondeoDesinversion");
				SQL sql=new SQL();
				HashMap<String,Object> params = new HashMap<String,Object>();
				
				for(Custodia CustodiaIn : in.p_Custodia){
					listInstrumentos.add(CustodiaIn.Nemotecnico);
					if(CustodiaIn.MontoMercadoCustodia.compareTo(BigDecimal.ZERO) == 0){
						msg.id = 1L;
						msg.tipo = "CALCULO";
						msg.codigo = "ERROR";
						msg.descripcion = "Custodia con Producto " + CustodiaIn.NombreProductoAgrupador + " y Portafolio " + CustodiaIn.Regimen + " tiene el Monto Mercado Custodia en " + CustodiaIn.MontoMercadoCustodia;
						salida.P_OUT_MENSAJES.add(msg);
						salida.P_OUT_ERRORESCONTADOR = 1L;
						
						return salida;
					}
					CantidadTotal.add(CustodiaIn.CantidadCustodia);
					MontoTotalPatrimonio = MontoTotalPatrimonio.add(CustodiaIn.MontoMercadoCustodia);
					
				}
				BigDecimal monto = BigDecimal.ZERO;
				if(in.p_PorcentajeADesinvertir.longValue() == 0){
					salida.P_OUT_PORCENTAJE	= new BigDecimal((double)in.p_TotalADesinvertir/MontoTotalPatrimonio.longValue());
					monto = new BigDecimal(in.p_TotalADesinvertir).divide(new BigDecimal(in.p_Custodia.size()),MathContext.DECIMAL128);
				}else{
					salida.P_OUT_PORCENTAJE = in.p_PorcentajeADesinvertir.divide(new BigDecimal(100));
				}
				
				if(in.p_IdTraspaso > 0){
					params.clear();
					params.put("IdTraspaso", in.p_IdTraspaso);
					List<DetalleTraspasoDesinversion> DetalleTraspaso = sql.query(con, SQL_SELECT_DETALLE_TRASPASO_EGRESO_DESINVERSION, params,  DetalleTraspasoDesinversion.class);
					CalcularParidadEntrada calcularParidadEntrada = new  CalcularParidadEntrada();
					if (!DetalleTraspaso.isEmpty()) {
						
						calcularParidadEntrada.p_IdEmpresa = in.p_IdEmpresa;
						calcularParidadEntrada.p_IdTrabajo = 9999L;
						
						for (DetalleTraspasoDesinversion detalleTraspaso : DetalleTraspaso){
							CalculoEntrada c1 = new CalculoEntrada();
							if(BigDecimal.ZERO.compareTo(detalleTraspaso.UFTraspaso)==-1 ) {
								c1.Fecha = fechaSistema;
								c1.IdInterno = 1L;
								c1.IdMonedaDestino = detalleTraspaso.IdMoneda;
								c1.IdMonedaOrigen = "UF";
								
								c1.IdTrabajo = 2001L;
								c1.IdUso = "ORDENES";
								c1.MontoOrigen = detalleTraspaso.UFTraspaso;
								calcularParidadEntrada.p_CalculosEntrada.add(c1);
							}					
						}	
						CalcularParidad calcularParidad = new CalcularParidad();
						if(calcularParidadEntrada!=null &&calcularParidadEntrada.p_CalculosEntrada.size()>0) {
							CalcularParidadSalida calcularParidadSalida = calcularParidad.logica(con, calcularParidadEntrada);
							for(CalculoSalida regParidad : calcularParidadSalida.p_out_CalculosSalida) {
								DetalleTraspaso.forEach(traspaso -> {
									if(traspaso.UFTraspaso == regParidad.MontoOrigen && traspaso.IdMoneda == regParidad.IdMonedaDestino) {
										traspaso.MontoCalculadoUF = regParidad.MontoDestino;
										traspaso.MontoCalculadoCLP = regParidad.MontoLocal;
									}
					                
					            });
							}
						}
					}			
					for(Custodia CustodiaIn : in.p_Custodia){
						for(DetalleTraspasoDesinversion Detalle :DetalleTraspaso) {
							if(CustodiaIn.IdInstrumento.equals(Detalle.IdInstrumento) && CustodiaIn.Regimen.equals(Detalle.CodigoRegimenTributario)  && CustodiaIn.IdPortafolio.equals(Detalle.IdPortafolio)){
								params.clear();
								params.put("IdEmpresa", in.p_IdEmpresa);
								params.put("IdSerieFondo", CustodiaIn.Nemotecnico);
								List<ValorCuotaTipoParidad> valorCuotaTipoParidad = sql.query(con, SQL_SELECT_VALOR_CUOTA_TIPO_CAMBIO, params, ValorCuotaTipoParidad.class);
								
								if (BigDecimal.ZERO.compareTo(Detalle.MontoTraspaso)==-1) {
									if(CustodiaIn.IdClasificacionTipoInstrumento.equalsIgnoreCase("FM"))
									{
										CustodiaIn.MontoADesinvertirCLP = Detalle.MontoTraspaso;
										CustodiaIn = convertirMontos(con,CustodiaIn,CustodiaIn.MontoADesinvertirCLP,idMonedaSistema,in.p_IdEmpresa,fechaSistema,false);
										CustodiaIn.CantidadADesinvertir =CustodiaIn.MontoADesinvertir.divide(CustodiaIn.PrecioMercadoCustodia, MathContext.DECIMAL128).setScale(valorCuotaTipoParidad.get(0).DecimalesCuota.intValue(),  RoundingMode.HALF_DOWN);										
									}else{
										CustodiaIn.MontoADesinvertirCLP = Detalle.MontoTraspaso;	
										CustodiaIn = convertirMontos(con,CustodiaIn,CustodiaIn.MontoADesinvertirCLP,idMonedaSistema,in.p_IdEmpresa,fechaSistema,false);
										CustodiaIn.CantidadADesinvertir = CustodiaIn.MontoADesinvertir.divide(CustodiaIn.PrecioMercadoCustodia,0, RoundingMode.HALF_UP);
									}
									break;
								}else if(BigDecimal.ZERO.compareTo(Detalle.UFTraspaso)==-1) {
									if(CustodiaIn.IdClasificacionTipoInstrumento.equalsIgnoreCase("FM"))
									{
										CustodiaIn.MontoADesinvertir = Detalle.MontoCalculadoUF;
										CustodiaIn.MontoADesinvertirCLP = Detalle.MontoCalculadoCLP;
										CustodiaIn.CantidadADesinvertir = CustodiaIn.MontoADesinvertir.divide(CustodiaIn.PrecioMercadoCustodia, MathContext.DECIMAL128).setScale(valorCuotaTipoParidad.get(0).DecimalesCuota.intValue(),  RoundingMode.HALF_DOWN);
									}else{
										CustodiaIn.MontoADesinvertir = Detalle.MontoCalculadoUF;
										CustodiaIn.MontoADesinvertirCLP = Detalle.MontoCalculadoCLP;
										CustodiaIn.CantidadADesinvertir = CustodiaIn.MontoADesinvertir.divide(CustodiaIn.PrecioMercadoCustodia,0,RoundingMode.HALF_UP);
									}
									break;
								}else if (BigDecimal.ZERO.compareTo(Detalle.PorcentajeTraspaso)==-1) {
									if(CustodiaIn.IdClasificacionTipoInstrumento.equalsIgnoreCase("FM")) {
										CustodiaIn.CantidadADesinvertir = (Detalle.PorcentajeTraspaso.multiply(CustodiaIn.CantidadCustodia)).movePointLeft(2).setScale(valorCuotaTipoParidad.get(0).DecimalesCuota.intValue(),  RoundingMode.HALF_DOWN);
										CustodiaIn.MontoADesinvertir = CustodiaIn.CantidadADesinvertir.multiply(CustodiaIn.PrecioMercadoCustodia).setScale(valorCuotaTipoParidad.get(0).DecimalesCuota.intValue(),  RoundingMode.HALF_DOWN);
										CustodiaIn = convertirMontos(con,CustodiaIn,CustodiaIn.MontoADesinvertir,idMonedaSistema,in.p_IdEmpresa,fechaSistema,true);
										CustodiaIn.MontoADesinvertirCLP.setScale(0, RoundingMode.HALF_DOWN);
									}else{
										CustodiaIn.CantidadADesinvertir = (Detalle.PorcentajeTraspaso.multiply(CustodiaIn.CantidadCustodia)).divide(new BigDecimal(100),0, RoundingMode.HALF_UP);
										CustodiaIn.MontoADesinvertir = CustodiaIn.CantidadADesinvertir.multiply(CustodiaIn.PrecioMercadoCustodia).setScale(0, BigDecimal.ROUND_HALF_UP);
										CustodiaIn = convertirMontos(con,CustodiaIn,CustodiaIn.MontoADesinvertir,idMonedaSistema,in.p_IdEmpresa,fechaSistema,true);
										CustodiaIn.MontoADesinvertirCLP.setScale(0, RoundingMode.HALF_DOWN);
										
									}
									break;
								}
							}
						}
						ListCustodia.add(CustodiaIn);
						MontoTotal = MontoTotal.add(CustodiaIn.MontoADesinvertirCLP);
					}
					
				}else {
				
					if(in.p_TotalADesinvertir >= MontoTotalPatrimonio.longValue()){
			
						for(Custodia  CustodiaIn : in.p_Custodia){
						
							CustodiaIn.CantidadADesinvertir = CustodiaIn.CantidadCustodia;
							CustodiaIn.MontoADesinvertir = CustodiaIn.MontoMercadoCustodia;
							CustodiaIn.MontoADesinvertirCLP = CustodiaIn.MontoMercadoCustodia;					
							ListCustodia.add(CustodiaIn);
						}
					}else{
						if(in.p_PorcentajeADesinvertir.compareTo(BigDecimal.ZERO)==0) {
							in.p_Custodia= convertirMontos(con,in.p_Custodia,monto,idMonedaSistema,in.p_IdEmpresa,fechaSistema);
						}
							for(Custodia CustodiaIn : in.p_Custodia){
														
								in.p_TotalADesinvertir = MontoTotalPatrimonio.multiply(salida.P_OUT_PORCENTAJE).longValue();
								
								switch(IndicadorRedondeo)
								{
									case "ROUND_UP":
										if(CustodiaIn.IdClasificacionTipoInstrumento.equalsIgnoreCase("RV"))
										{
											CustodiaIn.CantidadADesinvertir = salida.P_OUT_PORCENTAJE.multiply( CustodiaIn.CantidadCustodia).setScale(0, BigDecimal.ROUND_UP);
											CustodiaIn.MontoADesinvertir = CustodiaIn.CantidadADesinvertir.multiply(CustodiaIn.PrecioMercadoCustodia).setScale(0, BigDecimal.ROUND_UP);
										}else{
											CustodiaIn.CantidadADesinvertir = salida.P_OUT_PORCENTAJE.multiply( CustodiaIn.CantidadCustodia);
											CustodiaIn.MontoADesinvertir = CustodiaIn.CantidadADesinvertir.multiply(CustodiaIn.PrecioMercadoCustodia);
										}
										break;
									case "ROUND_DOWN":
										if(CustodiaIn.IdClasificacionTipoInstrumento.equalsIgnoreCase("RV"))
										{
											CustodiaIn.CantidadADesinvertir = salida.P_OUT_PORCENTAJE.multiply( CustodiaIn.CantidadCustodia).setScale(0, BigDecimal.ROUND_DOWN);
											CustodiaIn.MontoADesinvertir = CustodiaIn.CantidadADesinvertir.multiply(CustodiaIn.PrecioMercadoCustodia).setScale(0, BigDecimal.ROUND_DOWN);
										}else{
											CustodiaIn.CantidadADesinvertir = salida.P_OUT_PORCENTAJE.multiply( CustodiaIn.CantidadCustodia);
											CustodiaIn.MontoADesinvertir = CustodiaIn.CantidadADesinvertir.multiply(CustodiaIn.PrecioMercadoCustodia);
										}
										break;
									case "ROUND_HALF_UP":
										if(CustodiaIn.IdClasificacionTipoInstrumento.equalsIgnoreCase("RV"))
										{
											CustodiaIn.CantidadADesinvertir = salida.P_OUT_PORCENTAJE.multiply( CustodiaIn.CantidadCustodia).setScale(0, BigDecimal.ROUND_HALF_UP);
											CustodiaIn.MontoADesinvertir = CustodiaIn.CantidadADesinvertir.multiply(CustodiaIn.PrecioMercadoCustodia).setScale(0, BigDecimal.ROUND_HALF_UP);
										}else{
											CustodiaIn.CantidadADesinvertir = salida.P_OUT_PORCENTAJE.multiply( CustodiaIn.CantidadCustodia);
											CustodiaIn.MontoADesinvertir = CustodiaIn.CantidadADesinvertir.multiply(CustodiaIn.PrecioMercadoCustodia);
										}
										break;
									case "HALF_DOWN":
										params.clear();
										params.put("IdEmpresa", in.p_IdEmpresa);
										params.put("IdSerieFondo", CustodiaIn.Nemotecnico);
										
										List<ValorCuotaTipoParidad> valorCuotaTipoParidad = sql.query(con, SQL_SELECT_VALOR_CUOTA_TIPO_CAMBIO, params, ValorCuotaTipoParidad.class);

											if(CustodiaIn.IdClasificacionTipoInstrumento.equalsIgnoreCase("FM"))
											{
												CustodiaIn.CantidadADesinvertir = (salida.P_OUT_PORCENTAJE.multiply( CustodiaIn.CantidadCustodia)).setScale(valorCuotaTipoParidad.get(0).DecimalesCuota.intValue(),  RoundingMode.HALF_DOWN);
												CustodiaIn.MontoADesinvertir = CustodiaIn.CantidadADesinvertir.multiply(CustodiaIn.PrecioMercadoCustodia).setScale(valorCuotaTipoParidad.get(0).DecimalesCuota.intValue(),  RoundingMode.HALF_DOWN);
												CustodiaIn = convertirMontos(con,CustodiaIn,CustodiaIn.MontoADesinvertir,idMonedaSistema,in.p_IdEmpresa,fechaSistema,true);
												CustodiaIn.MontoADesinvertirCLP=CustodiaIn.MontoADesinvertirCLP.setScale(0, BigDecimal.ROUND_HALF_DOWN);
											}else{
												CustodiaIn.CantidadADesinvertir = salida.P_OUT_PORCENTAJE.multiply( CustodiaIn.CantidadCustodia).setScale(0, RoundingMode.HALF_DOWN);
												CustodiaIn.MontoADesinvertir = CustodiaIn.CantidadADesinvertir.multiply(CustodiaIn.PrecioMercadoCustodia).setScale(0, RoundingMode.HALF_DOWN);
												CustodiaIn = convertirMontos(con,CustodiaIn,CustodiaIn.MontoADesinvertir,idMonedaSistema,in.p_IdEmpresa,fechaSistema,true);
												CustodiaIn.MontoADesinvertirCLP=CustodiaIn.MontoADesinvertirCLP.setScale(0, RoundingMode.HALF_DOWN);
											}
										break;
								}	
								
								ListCustodia.add(CustodiaIn);
								MontoTotal = MontoTotal.add(CustodiaIn.MontoADesinvertirCLP);
							}
						}
					}
					
				salida.P_OUT_MONTO_TOTAL = MontoTotal;
				salida.P_OUT_CUSTODIA = ListCustodia;
				salida.P_OUT_PORCENTAJE = salida.P_OUT_PORCENTAJE.multiply(new BigDecimal(100));
				
				msg.id = 1L;
				msg.tipo = "CALCULO";
				msg.codigo = "OK";
				msg.descripcion = "Calculo Realizado";
				salida.P_OUT_MENSAJES.add(msg);
				salida.P_OUT_ERRORESCONTADOR = 0L;
			}
			catch(Exception er) {
				msg.id = 1L;
				msg.tipo = "CALCULO";
				msg.codigo = "ERROR";
				msg.descripcion = er.toString();
				salida.P_OUT_MENSAJES.add(msg);
				salida.P_OUT_ERRORESCONTADOR = 1L;
			}
		}else{
			msg.id = 1L;
			msg.tipo = "CALCULO";
			msg.codigo = "ERROR";
			msg.descripcion = "No hay Custodias para calcular";
			salida.P_OUT_MENSAJES.add(msg);
			salida.P_OUT_ERRORESCONTADOR = 1L;
		}
		return salida;
	}
	
	/********************
	 * Método Principal *
	 ********************/

	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			try {
				
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();				
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"APV\",\"p_TotalADesinvertir\":190000,\"p_PorcentajeADesinvertir\":0,\"p_CantidadADesinvertir\":0,\"p_IdTraspaso\":0,\"p_Custodia\":[{\"IdRegistro\":1,\"NombreProductoAgrupador\":\"APV Acciones\",\"Regimen\":\"DC-R\",\"IdClasificacionTipoInstrumento\":\"FM\",\"IdTipoInstrumento\":\"FFM\",\"Nemotecnico\":\"00039DCLP\",\"CantidadCustodia\":334.8799,\"PPPCustodia\":1688.7176,\"ValorPPPCustodia\":565517.59641152,\"MontoPPPCustodia\":565518,\"CantidadADesinvertir\":95.8366,\"MontoADesinvertir\":196224.2789,\"IdPortafolio\":206348,\"IdEmisor\":\"SURA\",\"IdInstrumento\":910288,\"IdMonedaInstrReajuste\":\"CLP\",\"DocumentoIdentidad\":\"16657034-4\",\"DocumentoIdentidadRutCavali\":\"\",\"NombreCliente\":\"MACARENA SUSANA MANCILLA LOPEZ\",\"Relacion\":\"TER\",\"ISIN\":\"1\",\"ClasificacionRiesgoInstrumento\":\"\",\"EsFungible\":\"N\",\"EsPagaderoEn\":\"CLP\",\"IdBoveda\":\"COR\",\"NumeroCuentaBoveda\":\"\",\"TotalPosicion\":334.8799,\"TotalPosicionPresunta\":0,\"Rentabilidad\":21.24504316453411,\"PrecioMercadoCustodia\":2047.4879,\"ValorMercadoCustodia\":685662.54320321,\"MontoMercadoCustodia\":685662.54320321,\"DiferenciaMercadoCustodia\":117346.85605464,\"CantidadGarantia\":0,\"PrecioMercadoGarantia\":0,\"ValorMercadoGarantia\":0,\"DiferenciaMercadoGarantia\":0,\"CantidadSimulCom\":0,\"PrecioMercadoSimulCom\":0,\"ValorMercadoSimulCom\":0,\"DiferenciaMercadoSimulCom\":0,\"CantidadSimulVen\":0,\"PrecioMercadoSimulVen\":0,\"ValorMercadoSimulVen\":0,\"DiferenciaMercadoSimulVen\":0,\"CantidadPrestamo\":0,\"PrecioMercadoPrestamo\":0,\"ValorMercadoPrestamo\":0,\"DiferenciaMercadoPrestamo\":0,\"CantidadEmpresti\":0,\"PrecioMercadoEmpresti\":0,\"ValorMercadoEmpresti\":0,\"DiferenciaMercadoEmpresti\":0,\"CantidadPactosV\":0,\"PrecioMercadoPactosV\":0,\"ValorMercadoPactosV\":0,\"DiferenciaMercadoPactosV\":0,\"CantidadPactosC\":0,\"PrecioMercadoPactosC\":0,\"ValorMercadoPactosC\":0,\"DiferenciaMercadoPactosC\":0,\"CantidadReporteCompra\":0,\"PrecioMercadoReporteCompra\":0,\"ValorMercadoReporteCompra\":0,\"DiferenciaMercadoReporteCompra\":0,\"CantidadReporteVenta\":0,\"PrecioMercadoReporteVenta\":0,\"ValorMercadoReporteVenta\":0,\"DiferenciaMercadoReporteVenta\":0,\"CantidadTransIng\":0,\"CantidadTransEgr\":0,\"IdMonedaInstrumento\":\"CLP\",\"FechaOperacion\":\"2024-08-01\"},{\"IdRegistro\":2,\"NombreProductoAgrupador\":\"APV Acciones\",\"Regimen\":\"APV-B\",\"IdClasificacionTipoInstrumento\":\"FM\",\"IdTipoInstrumento\":\"FFM\",\"Nemotecnico\":\"00111AUSD\",\"CantidadCustodia\":289.035,\"PPPCustodia\":45.65,\"ValorPPPCustodia\":13194.44775,\"MontoPPPCustodia\":11848614.0795,\"CantidadADesinvertir\":82.717,\"MontoADesinvertir\":3776.031,\"IdPortafolio\":206346,\"IdEmisor\":\"BGF\",\"IdInstrumento\":910545,\"IdMonedaInstrReajuste\":\"USD\",\"DocumentoIdentidad\":\"16657034-4\",\"DocumentoIdentidadRutCavali\":\"\",\"NombreCliente\":\"MACARENA SUSANA MANCILLA LOPEZ\",\"Relacion\":\"TER\",\"ISIN\":\"LU0106831901\",\"ClasificacionRiesgoInstrumento\":\"\",\"EsFungible\":\"N\",\"EsPagaderoEn\":\"USD\",\"IdBoveda\":\"COR\",\"NumeroCuentaBoveda\":\"\",\"TotalPosicion\":289.035,\"TotalPosicionPresunta\":0,\"Rentabilidad\":0,\"PrecioMercadoCustodia\":45.65,\"ValorMercadoCustodia\":13194.44775,\"MontoMercadoCustodia\":11875002.975,\"DiferenciaMercadoCustodia\":12693.87775,\"CantidadGarantia\":0,\"PrecioMercadoGarantia\":0,\"ValorMercadoGarantia\":0,\"DiferenciaMercadoGarantia\":0,\"CantidadSimulCom\":0,\"PrecioMercadoSimulCom\":0,\"ValorMercadoSimulCom\":0,\"DiferenciaMercadoSimulCom\":0,\"CantidadSimulVen\":0,\"PrecioMercadoSimulVen\":0,\"ValorMercadoSimulVen\":0,\"DiferenciaMercadoSimulVen\":0,\"CantidadPrestamo\":0,\"PrecioMercadoPrestamo\":0,\"ValorMercadoPrestamo\":0,\"DiferenciaMercadoPrestamo\":0,\"CantidadEmpresti\":0,\"PrecioMercadoEmpresti\":0,\"ValorMercadoEmpresti\":0,\"DiferenciaMercadoEmpresti\":0,\"CantidadPactosV\":0,\"PrecioMercadoPactosV\":0,\"ValorMercadoPactosV\":0,\"DiferenciaMercadoPactosV\":0,\"CantidadPactosC\":0,\"PrecioMercadoPactosC\":0,\"ValorMercadoPactosC\":0,\"DiferenciaMercadoPactosC\":0,\"CantidadReporteCompra\":0,\"PrecioMercadoReporteCompra\":0,\"ValorMercadoReporteCompra\":0,\"DiferenciaMercadoReporteCompra\":0,\"CantidadReporteVenta\":0,\"PrecioMercadoReporteVenta\":0,\"ValorMercadoReporteVenta\":0,\"DiferenciaMercadoReporteVenta\":0,\"CantidadTransIng\":0,\"CantidadTransEgr\":0,\"IdMonedaInstrumento\":\"USD\",\"FechaOperacion\":\"2024-08-01\"}]}";
				ParametrosEntradaServicio Objin = gson.fromJson(json, ParametrosEntradaServicio.class);	
				
				CalculoMontosDesinversion demo = new CalculoMontosDesinversion();	
				
	 			System.out.println("ENTRADA");
	 			System.out.println(Objin.toString());
	 			System.out.println("SALIDA");
	 			
	 			try { 
	 				ParametrosSalidaServicio ObjOut = demo.logica(con, Objin);
	 				//con.commit();
	 				con.rollback();
	 				System.out.println(ObjOut.toString());
	 			} 
	 			catch(Exception er) {
					er.printStackTrace();	
					con.rollback();
				}
	 			
		} catch (Exception e) {
			e.printStackTrace();			
		}	
		System.out.println("== END ==");
		};
	}
	
}