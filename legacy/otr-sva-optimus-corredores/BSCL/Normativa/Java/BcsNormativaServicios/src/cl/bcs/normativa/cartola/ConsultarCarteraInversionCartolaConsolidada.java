package cl.bcs.normativa.cartola;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.normativa.cartola.BcsCartola.Dato;
import cl.bcs.normativa.cartola.BcsCartola.EntradaCartola;
import cl.bcs.normativa.cartola.BcsCartola.SalidaCartola;
import cl.bcs.normativa.cartola.ConsultarCartolaOnline.ConsultarCartolaOnline_Entrada;
import cl.bcs.normativa.cartola.ConsultarCartolaOnline.ConsultarCartolaOnline_Salida;
import cl.bcs.normativa.cartola.ConsultarCartolaOnline.DatosCliente;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;


public class ConsultarCarteraInversionCartolaConsolidada implements JavaCall{
	
	private static final boolean Excepcion = false;

	//SQLs
	private final String SQL_CLASIFICACION_INSTRUMENTOS = loadResourceToString(
			"cl/bcs/normativa/cartola/sql/ConsultarCartolaOnline_SelectClasificacionInstrumentos.sql");
	private final static Logger logger = LogManager.getLogger(ConsultarCarteraInversionCartolaConsolidada.class);
	
	
	public static class ClasificacionInstrumentos {
		public Long IdInstrumento;
		public String Nemotecnico;
		public String Mercado;
		public String SubMercado;
		public String ClasificacionActivo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public class MercadoSorter implements Comparator<HashMap<String, Object>> 
	{
		public int compare(HashMap<String, Object> cartera1, HashMap<String, Object> cartera2) {
			return ((String) cartera2.get("Mercado")).compareTo((String) cartera1.get("Mercado"));
		}
	}
	
	public class SubMercadoSorter implements Comparator<HashMap<String, Object>> 
	{
		public int compare(HashMap<String, Object> cartera1, HashMap<String, Object> cartera2) {
			return ((String) cartera2.get("SubMercado")).compareTo((String) cartera1.get("SubMercado"));
		}
	}
	
	public class ClasificacionSorter implements Comparator<HashMap<String, Object>> 
	{
		public int compare(HashMap<String, Object> cartera1, HashMap<String, Object> cartera2) {
			return ((String) cartera2.get("Clasificacion")).compareTo((String) cartera1.get("Clasificacion"));
		}
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = Utils.getGsonBuilder();	
		ConsultarCartolaOnline_Entrada in=gson.fromJson(jin.toString() ,ConsultarCartolaOnline_Entrada.class);
		ConsultarCartolaOnline_Salida out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	@Trace
	public ConsultarCartolaOnline_Salida logica(Connection con, ConsultarCartolaOnline_Entrada entrada) throws Exception {
		logger.info("<<INICIO::ConsultarCarteraInversionCartolaConsolidada>>");
		ConsultarCartolaOnline_Salida out = new ConsultarCartolaOnline_Salida();		
	
		
		EntradaCartola entradaCartola = new EntradaCartola();
		Dato dato = new Dato();
	
		//Datos iniciales para obtener informacion de la cartola
		entradaCartola.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCartola.p_FiltroSoloMonedaSistema = "N";
		entradaCartola.p_IdModulo = entrada.p_IdModulo;
		entradaCartola.p_IdUsuario = entrada.p_IdUsuario;
		entradaCartola.p_FechaDesde = Fecha.toDate(entrada.p_FechaDesde);
		entradaCartola.p_FechaHasta = Fecha.toDate(entrada.p_FechaHasta);
		
		for (DatosCliente cliente : entrada.p_Datos) {
			dato = new Dato();
			dato.IdCliente = cliente.IdCliente;
			dato.IdPortafolio = cliente.IdPortafolio;
			entradaCartola.p_Datos.add(dato);
		}
		out.p_Datos = entrada.p_Datos;
		out.P_OUT_SALIDA.put("carteraInversiones", getCarteraInversion(con, entradaCartola));
	    
		out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"CONSULTA_OK","Ok"));
		out.P_OUT_ERRORESCONTADOR = 0L;
			
		logger.info("<<FIN::ConsultarCarteraInversionCartolaConsolidada>>");
		return out;
	};
	
	
	/*Procesamiento de Cartera de inversiones*/
	
	public List<HashMap<String,Object>> getCarteraInversion(Connection con, EntradaCartola entradaCartola) throws Exception{
		List<HashMap<String, Object>> carteraInversion = new ArrayList<HashMap<String, Object>>();
		SalidaCartola salidaCartola = new SalidaCartola();
		List<HashMap<String, Object>> result = new ArrayList<HashMap<String, Object>>();
		CarteraRV carteraRV = new CarteraRV();
		CarteraRF carteraRF = new CarteraRF();
		CarteraIF carteraIF = new CarteraIF();
		CarteraFM carteraFM = new CarteraFM();
		CarteraForward carteraFWD = new CarteraForward();
		
		SQL sql = new SQL();
		
		logger.debug("INICIO::Obtencion Clasificacion Instrumentos");
		//Clasificacion Instrumentos
		//HashMap<String,Object> params = new HashMap<String,Object>();
		List<ClasificacionInstrumentos> clasificacionInstrumentos = sql.query(con,SQL_CLASIFICACION_INSTRUMENTOS, new HashMap<>(), ClasificacionInstrumentos.class); 
		logger.debug("FIN::Obtencion Clasificacion Instrumentos");
		
		
		logger.debug("INICIO::Obtencion Carteras Cliente");
		
		salidaCartola = carteraRV.procesar(con, entradaCartola);
		if(salidaCartola != null && !salidaCartola.P_OUT_SALIDA.isEmpty()) {
			carteraInversion = (List<HashMap<String, Object>>) salidaCartola.P_OUT_SALIDA.get("carteraRV");
			carteraInversion = procesaCarteraInversionRV(carteraInversion,result);
		}
		
		
		salidaCartola = carteraRF.procesar(con, entradaCartola); 
		if(salidaCartola != null && !salidaCartola.P_OUT_SALIDA.isEmpty()) {
			carteraInversion = (List<HashMap<String, Object>>) salidaCartola.P_OUT_SALIDA.get("carteraRF");
			carteraInversion = procesaCarteraInversionRF(carteraInversion,result);
		}
		
		
		salidaCartola = carteraIF.procesar(con, entradaCartola); 
		if(salidaCartola != null && !salidaCartola.P_OUT_SALIDA.isEmpty()) {
			carteraInversion = (List<HashMap<String, Object>>) salidaCartola.P_OUT_SALIDA.get("carteraIF");
			carteraInversion = procesaCarteraInversionIIF(carteraInversion,result);
		}
		
		salidaCartola = carteraFM.procesar(con, entradaCartola);
		if(salidaCartola != null && !salidaCartola.P_OUT_SALIDA.isEmpty()) {
			carteraInversion = (List<HashMap<String, Object>>) salidaCartola.P_OUT_SALIDA.get("carteraFM");
			carteraInversion = procesaCarteraInversionFFMM(carteraInversion, result);
		}
		
		salidaCartola = carteraFWD.procesar(con, entradaCartola);
		if(salidaCartola != null && !salidaCartola.P_OUT_SALIDA.isEmpty()) {
			carteraInversion = (List<HashMap<String, Object>>) salidaCartola.P_OUT_SALIDA.get("carteraFM");
			carteraInversion = procesaCarteraInversionFWD(carteraInversion, result);
		}
		
		//Obtenemos clasificacion instrumentos cartera
		carteraInversion = procesaClasificacionInstrumentosCartera(carteraInversion, clasificacionInstrumentos);
		
		logger.debug("FIN::Obtencion Carteras Cliente");
		
		
		
		logger.debug("INICIO:: Ordenando cartera/clasificacion");
		
		//carteraInversion = ordenaCarteraPorClasificacion(carteraInversion,"Mercado");
		//carteraInversion = ordenaCarteraPorClasificacion(carteraInversion,"SubMercado");
		//carteraInversion = ordenaCarteraPorClasificacion(carteraInversion,"Clasificacion");
		Collections.sort(carteraInversion, new MercadoSorter()
				.thenComparing(new SubMercadoSorter())
				.thenComparing(new ClasificacionSorter()));

		logger.debug("FIN:: Ordenando cartera/clasificacion");
		
		return carteraInversion;
	}
	
	public List<HashMap<String, Object>> procesaClasificacionInstrumentosCartera(List<HashMap<String,Object>> cartera, List<ClasificacionInstrumentos> clasificacion){
		if(cartera != null) {
			
			for(HashMap<String, Object> item : cartera) {
				Long idInstrumento = Long.parseLong(item.get("IdInstrumento").toString());
				for(ClasificacionInstrumentos clasif : clasificacion) {
					if (clasif.IdInstrumento.equals(idInstrumento)) {
						item.replace("Mercado",clasif.Mercado);
						item.replace("SubMercado","",clasif.SubMercado);
						item.replace("Clasificacion","",clasif.ClasificacionActivo);
						break;
					}
				}
			}
		}		
		return cartera;
	}
	
	public List<HashMap<String, Object>> procesaCarteraInversionRV(List<HashMap<String,Object>> carteraRv, List<HashMap<String, Object>> result){
		
		if (carteraRv != null){
			
			for(HashMap<String, Object> item : carteraRv) {
				
				HashMap<String, Object> registro = crearRegistroCartera((String) item.get("RazonSocial"), (String) item.get("IdEmisor"), (String) item.get("Moneda"), (BigDecimal) item.get("DisponibleActivo"), (String) item.get("FechaVencimiento"), 
						 (BigDecimal) item.get("PlazoActivo"), (BigDecimal) item.get("PrecioPromedioCompra"),  (BigDecimal) item.get("PrecioMercado"), (BigDecimal) item.get("MontoMercadoMonedaSistemaActivo"), (BigDecimal) item.get("MontoMercadoMonedaAdicionalActivo"), 
						 BigDecimal.ZERO,  (BigDecimal) item.get("RentabilidadPasivo"), BigDecimal.ZERO, (Long) item.get("IdInstrumento"), "", "", "", "detalle",(Long) item.get("IdPortafolio"), (Long) item.get("IdCliente"), (String) item.get("IdTipoInstrumento"));
				result.add(registro);
			}
		}
		
		return result;
		
	}
	
	
	public List<HashMap<String, Object>> procesaCarteraInversionRF(List<HashMap<String,Object>> carteraRF, List<HashMap<String, Object>> result){
		
		if (carteraRF != null){
			
			for(HashMap<String, Object> item : carteraRF) {
				HashMap<String, Object> registro = crearRegistroCartera((String) item.get("RazonSocial"), (String) item.get("IdEmisor"), (String) item.get("Moneda"), (BigDecimal) item.get("CantidadContable"), (String) item.get("FechaVencimiento"),
						BigDecimal.ZERO, (BigDecimal) item.get("PrecioCompra"), (BigDecimal) item.get("PrecioMercado"),  (BigDecimal) item.get("MontoMercadoMonedaSistema"), (BigDecimal) item.get("MontoMercadoMonedaAdicional"),
						 (BigDecimal) item.get("CuponesPagadosMonedaSistema"), BigDecimal.ZERO, BigDecimal.ZERO, (Long) item.get("IdInstrumento"), (String) item.get("IdClasificacionTipoInstrumento"), "", "", "detalle",(Long) item.get("IdPortafolio"), (Long) item.get("IdCliente"), (String) item.get("IdTipoInstrumento"));
				result.add(registro);
			}
		}
		
		return result;
		
	}
	
	public List<HashMap<String, Object>> procesaCarteraInversionIIF(List<HashMap<String,Object>> carteraIIF, List<HashMap<String, Object>> result){
		
		if (carteraIIF != null){
			
			for(HashMap<String, Object> item : carteraIIF) {
				HashMap<String, Object> registro = crearRegistroCartera( (String) item.get("RazonSocial"), (String) item.get("IdEmisor"), (String) item.get("Moneda"), (BigDecimal) item.get("CantidadContable"), (String) item.get("FechaVencimiento"), 
						BigDecimal.ZERO, (BigDecimal) item.get("PrecioCompra"), (BigDecimal) item.get("PrecioMercado"),  (BigDecimal) item.get("MontoMercadoMonedaSistema"), (BigDecimal) item.get("MontoMercadoMonedaAdicional"), (BigDecimal) item.get("CuponesPagadosMonedaSistema"), 
						BigDecimal.ZERO, BigDecimal.ZERO, (Long) item.get("IdInstrumento"), (String) item.get("Mercado"), "", "","detalle",(Long) item.get("IdPortafolio"), (Long) item.get("IdCliente"), (String) item.get("IdTipoInstrumento"));
				result.add(registro);
			}
		}
		
		return result;
		
	}
	
	public List<HashMap<String, Object>> procesaCarteraInversionFFMM(List<HashMap<String,Object>> carteraFFMM, List<HashMap<String, Object>> result){
		
		if (carteraFFMM != null){
			
			for(HashMap<String, Object> item : carteraFFMM) {
				HashMap<String, Object> registro = crearRegistroCartera( (String) item.get("RAZON_SOCIAL"), (String) item.get("emisor"), (String) item.get("ID_MONEDA_INSTRUMENTO"), (BigDecimal) item.get("CAN_CUSTODIA"), "", 
						BigDecimal.ZERO, (BigDecimal) item.get("valorCuotaAporte"), (BigDecimal) item.get("valorCuotaActual"),  (BigDecimal) item.get("MONTO_VALORIZ"), BigDecimal.ZERO, BigDecimal.ZERO, 
						BigDecimal.ZERO, BigDecimal.ZERO, (Long) item.get("ID_INSTRUMENTO"), "", "", "","detalle",(Long) item.get("IdPortafolio"), (Long) item.get("IdCliente"), (String) item.get("IdTipoInstrumento"));
				result.add(registro);
			}
		}
		
		return result;
		
	}
	
	
	public List<HashMap<String, Object>> procesaCarteraInversionFWD(List<HashMap<String,Object>> carteraFWD, List<HashMap<String, Object>> result){
		
		if (carteraFWD != null){
			String monedaOperacion = "";
			String tipoOperacion = "";
			BigDecimal montoPesos = BigDecimal.ZERO;
			BigDecimal montoDolar = BigDecimal.ZERO;
			
			for(HashMap<String, Object> item : carteraFWD) {
				tipoOperacion = (String) item.get("tipo");
				
				if(tipoOperacion.equals("V")) {
					tipoOperacion = "Venta";
					monedaOperacion = (String) item.get("monedaRecibe");
				}else {
					tipoOperacion = "Compra";
					monedaOperacion = (String) item.get("monedaEntrega");
				}
				
				if (!monedaOperacion.equals("CLP")) {
					montoPesos = (BigDecimal) item.get("MontoMonedaSecundaria");
					montoDolar = (BigDecimal) item.get("monto");
				}else {
					montoPesos = (BigDecimal) item.get("monto");
					montoDolar = (BigDecimal) item.get("MontoMonedaSecundaria");
				}
				
				HashMap<String, Object> registro = crearRegistroCartera(tipoOperacion + " " + (String) item.get("Nemotecnico"), "", monedaOperacion, (BigDecimal) item.get("monto"),(String) item.get("vencimiento") , 
						BigDecimal.ZERO, (BigDecimal) item.get("TipoCambioCierre"), (BigDecimal) item.get("TipoCambioCosto"),  montoPesos, montoDolar, BigDecimal.ZERO, 
						BigDecimal.ZERO, BigDecimal.ZERO, (Long) item.get("ID_INSTRUMENTO"), "", "", "","detalle",(Long) item.get("IdPortafolio"), (Long) item.get("IdCliente"), (String) item.get("IdTipoInstrumento"));
				result.add(registro);
			}
		}
		
		return result;
		
	}
	
	private BigDecimal getRentabilidad(BigDecimal valorMercadoCLP, BigDecimal dividendosCupones, BigDecimal cantidad, BigDecimal precioPromCompra) {
		
		//((Valor Mercado pesos + dividendos  - (cantidad * precio prom compra)) / (cantidad * precio promedio compra)) * 100
		//try {
		BigDecimal rentabilidad = BigDecimal.ZERO;
		precioPromCompra = precioPromCompra.setScale(2,BigDecimal.ROUND_UP);
		cantidad = cantidad.setScale(2,BigDecimal.ROUND_UP);
		BigDecimal suma = (valorMercadoCLP.add(dividendosCupones));
		//System.out.println("precioPromCompra: "+precioPromCompra);
		//System.out.println("cantidad: "+cantidad);
		BigDecimal valor = BigDecimal.ZERO.setScale(2);
		if(precioPromCompra != BigDecimal.ZERO.setScale(2)) {
		    valor = (cantidad.multiply(precioPromCompra).setScale(2,BigDecimal.ROUND_UP)).setScale(2,BigDecimal.ROUND_UP);		  
		}
		 
		if (valor != BigDecimal.ZERO.setScale(2))
		{
			rentabilidad = (suma.subtract(valor));
			rentabilidad = (rentabilidad.divide(valor,2,BigDecimal.ROUND_HALF_EVEN)); 
			rentabilidad = (rentabilidad.multiply(new BigDecimal(100)));
			return rentabilidad.setScale(2,BigDecimal.ROUND_UP);
		}else {
			return BigDecimal.ZERO.setScale(2);
		}

		//}
		/*catch (Exception c )
		{
			System.out.println(c);
			return null ;
		}*/
		
	}
	
	
	public HashMap<String, Object> crearRegistroCartera(String detalleInstrumentos, String emisor, String moneda,  BigDecimal cantidad, String fecha_Vcto,
			BigDecimal duracion, BigDecimal precioPromCompra,  BigDecimal precioMercado, BigDecimal valorMercadoCLP, BigDecimal valorMercadoUSD, BigDecimal dividendosCupones,
			BigDecimal rentabP, BigDecimal carteraP, Long idInstrumento, String mercado, String  subMercado, String clasificacion, String tipo, Long idPortafolio, Long idCliente,
			String idTipoInstrumento){
		
		//Calculo de rentabilidad

		String rentabilidad = "";
				
		if (tipo.equals("detalle")) {
			BigDecimal rentab = BigDecimal.ZERO.setScale(2);
			precioPromCompra = "RF".equalsIgnoreCase(mercado) || "IF".equalsIgnoreCase(mercado) ? precioPromCompra.setScale(6,BigDecimal.ROUND_UP) : precioPromCompra.setScale(2,BigDecimal.ROUND_UP);
			cantidad = cantidad.setScale(2,BigDecimal.ROUND_UP);
			rentab = getRentabilidad(valorMercadoCLP, dividendosCupones, cantidad, precioPromCompra);
			if(rentabilidad.equals(null)){
				rentab = BigDecimal.ZERO;
			}
			rentabilidad = rentab + "%";
		}
		

		HashMap<String, Object> registro = new HashMap<>();
		
		registro.put("idPortafolio",idPortafolio);
		registro.put("idCliente", idCliente);
		registro.put("detalleInstrumentos", detalleInstrumentos);
		registro.put("emisor", emisor); 
		registro.put("moneda", moneda);
		registro.put("cantidad",cantidad);
		if((fecha_Vcto).equalsIgnoreCase("0001-01-01"))
			registro.put("fecha_Vcto", "");
		else
			registro.put("fecha_Vcto", fecha_Vcto);
		
		registro.put("duracion", duracion); 
		
		registro.put("PrecioPromCompra", precioPromCompra);
		registro.put("precioMercado",  precioMercado);  
		registro.put("valorMercadoCLP", valorMercadoCLP);  
		registro.put("valorMercadoUSD", valorMercadoUSD);  
		registro.put("dividendosCupones", dividendosCupones); 
		registro.put("rentabP", rentabilidad); 
		registro.put("carteraP", carteraP); 
		registro.put("IdInstrumento", idInstrumento);
		registro.put("Mercado",mercado);
		registro.put("SubMercado", subMercado);
		registro.put("Clasificacion", clasificacion);
		registro.put("tipoFila",tipo);
		registro.put("idTipoInstrumento", idTipoInstrumento);
		
		return registro;
	}
	
	
	
	public List<HashMap<String, Object>> ordenaCarteraPorClasificacion(List<HashMap<String,Object>> cartera, String campoOrdenar){
		if(cartera != null) {
			
			Collections.sort(cartera, new Comparator<HashMap<String, Object>>() {
				@Override
				public int compare(HashMap<String, Object> cartera1, HashMap<String, Object> cartera2) {
					return ((String) cartera2.get(campoOrdenar)).compareTo((String) cartera1.get(campoOrdenar));
				}
			});
		}	
		return cartera;		
	}
	
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {
				Gson gson = Utils.getGsonBuilder();	
				String json = "{\"p_IdEmpresa\":1,\"p_IdModulo\":\"NOR\",\"p_IdUsuario\":4,\"p_FechaDesde\":\"2019-07-01\",\"p_FechaHasta\":\"2019-07-31\",\"p_Datos\":[{\"IdCliente\":524891,\"IdPortafolio\":1450068}]}";
				ConsultarCartolaOnline_Entrada Objin_json = gson.fromJson(json, ConsultarCartolaOnline_Entrada.class);
				ConsultarCarteraInversionCartolaConsolidada demo = new ConsultarCarteraInversionCartolaConsolidada();				
	 			System.out.println("ENTRADA");
	 			System.out.println(Objin_json.toString());
	 			System.out.println("SALIDA");

	 			try { 
	 				ConsultarCartolaOnline_Salida ObjOut = demo.logica(con, Objin_json);
	 				//con.commit();
	 				
	 				 
	 				System.out.println(ObjOut.toString() + "");
	 			} 
	 			catch(Exception er) {
					er.printStackTrace();
					con.rollback();
				}
	 			
			} catch (Exception e) {
	
				e.printStackTrace();			
			}finally {
				con.close();
				System.out.println("== END ==");
			}	
		
		
		};
	}
	
}
