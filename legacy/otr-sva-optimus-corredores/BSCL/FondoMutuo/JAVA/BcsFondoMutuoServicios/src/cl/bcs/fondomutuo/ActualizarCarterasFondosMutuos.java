package cl.bcs.fondomutuo;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cl.bcs.fondomutuo.ActualizaCarteraValorizadaFFMM.EntradaActulizaCarteraValorizadaFFMM;
import cl.bcs.fondomutuo.ActualizaCarteraValorizadaFFMM.SalidaActulizaCarteraValorizadaFFMM;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ActualizarCarterasFondosMutuos implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(ActualizarCarterasFondosMutuos.class);
	
	private static final Long CANTIDAD_MAXIMA_CARTERAS_VALORIZACION =300000L;
	private final String SQL_SELECT_CARTERAS = loadResourceToString("cl/bcs/fondomutuo/sql/ActualizarCarterasFondosMutuos_SelectCarteras.sql");
	
	public static class EntradaActualizarCarterasFondosMutuos {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class SalidaActualizarCarterasFondosMutuos {
		public Long P_OUT_ERRORESCONTADOR;
		public Long P_OUT_CARTERAS_VALORIZADAS;
		public List <MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaActualizarCarterasFondosMutuos() {
			this.P_OUT_ERRORESCONTADOR=0L;
			this.P_OUT_CARTERAS_VALORIZADAS=0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SerieFondo{
		public String IdSerieFondo;
		public Long IdInstrumento;
		public String IdFondo;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public String IdMoneda;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Cartera{
		public String IdSerieFondo;
		public Long IdCartera;
		public BigDecimal INGCantidad;
		public BigDecimal INGMontoMercadoActualMonedaInstrumento;
		public BigDecimal INGMontoATasaMercadoActual;
		public BigDecimal INGMontoMercadoActualMonedaAdicional;
		public String IdMoneda;
		public BigDecimal INGPrecioMercadoActual;
		public String INGFechaPrecioTasaMercadoActual;
		public String INGFuenteTasaPrecioMercadoActual;
		public BigDecimal INGMontoCompraActualMonedaInstrumento;
		public BigDecimal INGMontoATasaCompraActual;
		public BigDecimal INGPrecioCompra;
		public BigDecimal EGRCantidad;
		//public BigDecimal EGRMontoMonedaInstrumento;
		public BigDecimal EGRMontoATasaMercado;
		public BigDecimal EGRMontoMercadoMonedaInstrumento;
		public BigDecimal EGRMontoMercadoMonedaAdicional;
		public BigDecimal EGRPrecioMercado;
		public BigDecimal EGRPrecio;
		public BigDecimal EGRMonto;
		public BigDecimal EGRMontoMonedaInstrumento;
		public BigDecimal EGRMontoMonedaAdicional;
		public String EGRFechaPrecioTasaMercado;
		public String EGRFuenteTasaPrecioMercado;
				
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Instrumento {
		public Long IdInstrumento;
		public String IdTipoInstrumento;
		public String IdClasificacionTipoInstrumento;
		public String IdMonedaEmision;
		public String Nemotecnico;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaActualizarCarterasFondosMutuos jin = gson.fromJson(in.toString(), EntradaActualizarCarterasFondosMutuos.class);
		SalidaActualizarCarterasFondosMutuos out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	/**
	 *
	 * Retorna la salida del serviciocon los mensajes de salida, contador de errores y carteras valorizadas
	 * 
	 * Primero obtiene todas las carteras a valorizar
	 * Segundo valoriza por tramos  
	 * @param con Conexion a base de datos
	 * @param entrada Entrada del servicio con la empresa, usuario y modulo.
	 * @return Salida del servicio con los mensajes de salida, contador de errores y carteras valorizadas
	 * 
	 * */
	public SalidaActualizarCarterasFondosMutuos logica(Connection con, EntradaActualizarCarterasFondosMutuos entrada) throws Exception {
		logger.info("INICIO::ActualizarCarterasFondosMutuos.logica");
		Long init = System.currentTimeMillis();
		
		SalidaActualizarCarterasFondosMutuos out = new SalidaActualizarCarterasFondosMutuos();
		List<Cartera> carterasAvalorizar = obtenerCarteras(con);
		valorizarPorTramos(con,carterasAvalorizar,entrada,out);
		
		if(out.P_OUT_ERRORESCONTADOR.equals(0L)) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"VALORIZACION_EXITOSA","Valorizacion exitosa, carteras valorizadas:" + out.P_OUT_CARTERAS_VALORIZADAS));
		}
		
		logger.info("ActualizarCarterasFondosMutuos::Tiempo Ejecucion:" + (System.currentTimeMillis() - init));
		logger.info("FIN::ActualizarCarterasFondosMutuos.logica");
		return out;
	}
	
	/**
	 * Evalua si la cantidad de registros es superior a lo permitido y lo separa en partes para hacer la valorizacion mas rapida.
	 * @param con Conexion a base de datos
	 * @param List carteras vigente
	 * @param EntradaActualizarCarterasFondosMutuos valores entrada del servicio ActualizarCarterasFondosMutuos
	 * @param SalidaActualizarCarterasFondosMutuos valores salida del servicio ActualizarCarterasFondosMutuos
	 */
	private void valorizarPorTramos (Connection con,List<Cartera> carteras, EntradaActualizarCarterasFondosMutuos entrada , SalidaActualizarCarterasFondosMutuos out) throws Exception {		
		List<Cartera> carterasPorTramos = new ArrayList<Cartera>();
		Long cantidadCarteras = 0L;
		try {
			for(Cartera car : carteras) {				
				carterasPorTramos.add(car);
				cantidadCarteras++;				
				if(cantidadCarteras.compareTo(CANTIDAD_MAXIMA_CARTERAS_VALORIZACION)==0) {
					valorizar(con,carterasPorTramos,entrada, out); 
					con.commit();
					cantidadCarteras=0L;
					carterasPorTramos.clear();
				}
			}
			if(cantidadCarteras > 0) {
				valorizar(con,carterasPorTramos,entrada, out); 
				cantidadCarteras=0L;
				carterasPorTramos.clear();
			}			
		}catch(Exception e) {
			agregarError(out,e);
		}
	}
	
	/**
	 * llama al servicio de que valoriza y actualiza las carteras.
	 * @param con Conexion a base de datos
	 * @param List carteras vigente
	 * @param EntradaActualizarCarterasFondosMutuos valores entrada del servicio ActualizarCarterasFondosMutuos
	 * @param SalidaActualizarCarterasFondosMutuos valores salida del servicio ActualizarCarterasFondosMutuos
	 */
	private void valorizar(Connection con ,List<Cartera> carteras ,  EntradaActualizarCarterasFondosMutuos entrada,SalidaActualizarCarterasFondosMutuos out) throws Exception {		
		ActualizaCarteraValorizadaFFMM valorizarCarteras = new ActualizaCarteraValorizadaFFMM();
		EntradaActulizaCarteraValorizadaFFMM inValorizar = new EntradaActulizaCarteraValorizadaFFMM();
		
		inValorizar.p_IdEmpresa = entrada.p_IdEmpresa;
		inValorizar.p_IdModulo  = entrada.p_IdModulo;
		inValorizar.p_IdUsuario = entrada.p_IdUsuario;
		inValorizar.carteras=carteras;		
		SalidaActulizaCarteraValorizadaFFMM outValorizar = valorizarCarteras.logica(con, inValorizar);		
		if(outValorizar.P_OUT_ERRORESCONTADOR.compareTo(0L)>0) {
			out.P_OUT_ERRORESCONTADOR = outValorizar.P_OUT_ERRORESCONTADOR;
			out.P_OUT_MENSAJES.addAll(outValorizar.P_OUT_MENSAJES);
		}
		out.P_OUT_CARTERAS_VALORIZADAS+=outValorizar.P_OUT_CARTERAS_VALORIZADAS;
	}
	
	/**
	 * Retorna carteras FFMM vigentes a valorizar 
	 * @param con Conexion a base de datos
	 * @return List con las carteras vigentes
	 */
	private List<Cartera> obtenerCarteras(Connection con) throws Exception {
		logger.info("INICIO::ActualizarCarterasFondosMutuos.obtenerCarteras");
		SQL sql=new SQL();
		List<Cartera> carteras = sql.query(con, SQL_SELECT_CARTERAS, new HashMap<String,Object>(), Cartera.class);
		logger.info("FIN::ActualizarCarterasFondosMutuos.obtenerCarteras");
		return carteras;
	}	
	
	/**
	 * Suma en 1 el valor del contador errores, agrega a una lista el error obtenido del sistema.
	 * @param out Salida del servicio
	 * @param e Error del sistema
	 */
	private void agregarError(SalidaActualizarCarterasFondosMutuos out, Exception e) {
		logger.info("INICIO::ActualizarCarterasFondosMutuos.agregarError");
		out.P_OUT_ERRORESCONTADOR++;
		out.P_OUT_MENSAJES.add((new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ERROR_VALORIZACION",e.toString())));
		logger.info("FIN::ActualizarCarterasFondosMutuos.agregarError");
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getBolsaConnection()) { //-->FIJARSE EN QUE ENTORNO SE ESTA PROBANDO<--		
		EntradaActualizarCarterasFondosMutuos Objin = new EntradaActualizarCarterasFondosMutuos();
		ActualizarCarterasFondosMutuos  demo=new ActualizarCarterasFondosMutuos();
		Objin.p_IdEmpresa = 1L;
		Objin.p_IdModulo = "PLA";
		Objin.p_IdUsuario = 15L;
		try {
			SalidaActualizarCarterasFondosMutuos ObjOut=demo.logica(con, Objin);
			//con.commit();
			con.rollback();
			System.out.println(ObjOut.toString());
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}
			con.rollback(); 			
    	}
		System.out.println("== END ==");
	}
}
