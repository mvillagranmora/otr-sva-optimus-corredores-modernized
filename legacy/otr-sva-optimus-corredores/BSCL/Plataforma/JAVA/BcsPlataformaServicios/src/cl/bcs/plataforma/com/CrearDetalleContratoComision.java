package cl.bcs.plataforma.com;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.com.CrearSubdetalleContratoComision.EntradaCrearSubdetalleContratoComision;

import cl.bcs.plataforma.com.ValidarExistenciaDetalleContratoComisionComun.EntradaValidarExistenciaDetalleContratoComisionComun;
import cl.bcs.plataforma.com.ValidarValoresSubdetalleContratoComision.EntradaValidarValoresSubdetalleContratoComision;
import cl.bcs.plataforma.com.ValidarValoresSubdetalleContratoComision.SalidaValidarValoresSubdetalleContratoComision;
import cl.bcs.plataforma.com.pojo.DetalleContratoComision;
import cl.bcs.plataforma.com.pojo.ResultadoDetalleContratoComision;
import cl.bcs.plataforma.com.pojo.Validacion;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearDetalleContratoComision implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(CrearDetalleContratoComision.class);
	private final String SQL_INSERT_DETALLE_CONTRATO_COMISION = loadResourceToString("cl/bcs/plataforma/com/sql/CrearDetalleContratoComision_InsertDetalleContratoComision.sql");
	
	public static class EntradaCrearDetalleContratoComision extends BcsPojo{
		
		public Long 							p_IdEmpresa;
		public Long 							p_IdUsuario;
		public String 							p_IdModulo;
		public String 							p_UsaReglasNegocio;
		public List<DetalleContratoComision> 	p_DetallesContratoComision;
		
		public EntradaCrearDetalleContratoComision() {
			this.p_DetallesContratoComision = new ArrayList<DetalleContratoComision>();
		}
		
	}
	
	public static class SalidaCrearDetalleContratoComision extends BcsPojo{
		
		public List<MensajeSalida>						P_OUT_MENSAJES;
		public Long										P_OUT_ERRORESCONTADOR;
		public List<ResultadoDetalleContratoComision> 	P_OUT_REFID;
		
		public SalidaCrearDetalleContratoComision() {
			this.P_OUT_MENSAJES 		= new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR 	= 0L;
			this.P_OUT_REFID 			= new ArrayList<ResultadoDetalleContratoComision>();
			
		}
		
		public void agregarValidacionesSalida(List<Validacion> validaciones) {
			for(Validacion validacion : validaciones) {
				agregarValidacionSalida(validacion);
			}
		}
		
		public void agregarValidacionSalida(Validacion validacion) {
			if(validacion.EsError) {
				this.P_OUT_ERRORESCONTADOR += 1L;
				this.P_OUT_MENSAJES.add(validacion.MensajeError);
			}
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		
		Gson gson = getGsonBuilder();
		EntradaCrearDetalleContratoComision in = gson.fromJson(jin.toString(), EntradaCrearDetalleContratoComision.class);
		SalidaCrearDetalleContratoComision out = logica(conn, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
		
	}
	
	public SalidaCrearDetalleContratoComision logica(Connection con, EntradaCrearDetalleContratoComision in) throws Exception {
		logger.info("INICIO::CrearDetalleContratoComision");
		Long inicioServicio = System.currentTimeMillis();
		
		SalidaCrearDetalleContratoComision out = new SalidaCrearDetalleContratoComision();
		if(out.P_OUT_ERRORESCONTADOR > 0) return out;
		
		//Valida que el contrato no tenga un detalle repetido en base a los parametros
		ValidarExistenciaDetalleContratoComisionComun validarExistenciaDetalleContratoComisionComun = new ValidarExistenciaDetalleContratoComisionComun();
		EntradaValidarExistenciaDetalleContratoComisionComun entradaValidarExistenciaDetalleContratoComisionComun = new EntradaValidarExistenciaDetalleContratoComisionComun();
		entradaValidarExistenciaDetalleContratoComisionComun.p_IdEmpresa = in.p_IdEmpresa;
		entradaValidarExistenciaDetalleContratoComisionComun.p_DetallesContratoComision = in.p_DetallesContratoComision;
		List<Validacion> validacionExistenciaDetalleContratoComisionComun = validarExistenciaDetalleContratoComisionComun.logica(con, entradaValidarExistenciaDetalleContratoComisionComun).validaciones;
		
		out.agregarValidacionesSalida(validacionExistenciaDetalleContratoComisionComun);
		if(out.P_OUT_ERRORESCONTADOR > 0) return out;
		
		List<DetalleContratoComision> detallesContratoComision = obtenerDetallesContratoComision(con, in);
		int [] resultadoInsertarDetalles = insertarDetallesContratoComision(con, detallesContratoComision);
		
		if(resultadoInsertarDetalles[0] == 1) {
			DetalleContratoComision detalleContratoComision = detallesContratoComision.parallelStream().findFirst().orElse(null);
			ValidarValoresSubdetalleContratoComision validarValoresSubdetalleContratoComision = new ValidarValoresSubdetalleContratoComision();
			EntradaValidarValoresSubdetalleContratoComision entradaValidarMontoMaximoSubdetalleContratoComision = new EntradaValidarValoresSubdetalleContratoComision();
			entradaValidarMontoMaximoSubdetalleContratoComision.p_IdEmpresa 	= in.p_IdEmpresa;
			entradaValidarMontoMaximoSubdetalleContratoComision.p_IdCobro 		= detalleContratoComision.IdCobro;
			entradaValidarMontoMaximoSubdetalleContratoComision.p_EsPorRangos	= detalleContratoComision.EsPorRangos;
			entradaValidarMontoMaximoSubdetalleContratoComision.p_SubdetallesContratoComision = detalleContratoComision.SubdetallesContratoComision;
			SalidaValidarValoresSubdetalleContratoComision validarValores = validarValoresSubdetalleContratoComision.logica(con, entradaValidarMontoMaximoSubdetalleContratoComision);
			out.P_OUT_ERRORESCONTADOR = validarValores.P_OUT_ERRORESCONTADOR;
			out.P_OUT_MENSAJES.addAll(validarValores.P_OUT_MENSAJES);
			if(out.P_OUT_ERRORESCONTADOR > 0) return out;
			
			crearSubdetalles(con, detalleContratoComision);
			
			
		}
		out.P_OUT_REFID = obtenerIdsInsertados(detallesContratoComision);
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearDetalleContratoComision", finServicio - inicioServicio);
		
		return out;
	
	}
	
	private List<DetalleContratoComision> obtenerDetallesContratoComision(Connection con, EntradaCrearDetalleContratoComision in) throws Exception {
		
		logger.info("INICIO::CrearDetalleContratoComision.obtenerDetallesCotratoComision");
		Long inicioServicio = System.currentTimeMillis();
		
		List<Long> secuenciasDetalleContratoComision = Utils.getSecuencia(con, "BSCL.Plataforma.BD.Secuencias::COM.DetalleContratoComision", 1L);
		List<DetalleContratoComision> detallesContratoComision = new ArrayList<DetalleContratoComision>();
		Integer i = 0;
		for(DetalleContratoComision detalleContratoComision : in.p_DetallesContratoComision) {
			
			DetalleContratoComision detalle 	= new DetalleContratoComision();
			detalle.IdDetalleContratoComision 	= secuenciasDetalleContratoComision.get(i);
			detalle.IdContratoComision 			= detalleContratoComision.IdContratoComision;
			detalle.IdCobroClasificacion 		= detalleContratoComision.IdCobroClasificacion;
			detalle.IdCobro 					= detalleContratoComision.IdCobro;
			detalle.EsPorRangos 				= detalleContratoComision.EsPorRangos;
			detalle.IdConcepto 					= detalleContratoComision.IdConcepto;
			detalle.MontoMinimo					= detalleContratoComision.MontoMinimo;
			detalle.MontoMaximo					= detalleContratoComision.MontoMaximo;
			detalle.SubdetallesContratoComision = detalleContratoComision.SubdetallesContratoComision;
			detallesContratoComision.add(detalle);
			i++;
		}
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearDetalleContratoComision.obtenerDetallesCotratoComision", finServicio - inicioServicio);
		
		return detallesContratoComision;
	}
	
	public int [] insertarDetallesContratoComision(Connection con, List<DetalleContratoComision> detallesContratoComision) throws Exception {
		
		logger.info("INICIO::CrearDetalleContratoComision.insertarDetallesCotratoComision");
		Long inicioServicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		int [] resultado = sql.batch(con, SQL_INSERT_DETALLE_CONTRATO_COMISION, detallesContratoComision, DetalleContratoComision.class);
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearDetalleContratoComision.insertarDetallesCotratoComision", finServicio - inicioServicio);
		return resultado;
	}
	
	public List<ResultadoDetalleContratoComision> obtenerIdsInsertados(List<DetalleContratoComision> detallesContratoComision) {
		logger.info("INICIO::CrearDetalleContratoComision.obtenerIdsInsertados");
		Long inicioServicio = System.currentTimeMillis();
		
		List<ResultadoDetalleContratoComision> resultadoIdsInsertados = new ArrayList<ResultadoDetalleContratoComision>();
		AtomicLong i = new AtomicLong(0L);
		detallesContratoComision.stream()
								.forEach( dcc -> {
									ResultadoDetalleContratoComision resultado = new ResultadoDetalleContratoComision();
									resultado.IdInterno = i.getAndIncrement();
									resultado.IdDetalleContratoComision = dcc.IdDetalleContratoComision;
									resultadoIdsInsertados.add(resultado);
								});
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearDetalleContratoComision.obtenerIdsInsertados", finServicio - inicioServicio);
		return resultadoIdsInsertados;
	}
	
	private void crearSubdetalles(Connection con, DetalleContratoComision detalleContratoComision) throws Exception {
		
		logger.info("INICIO::CrearDetalleContratoComision.crearSubdetalles");
		Long inicioServicio = System.currentTimeMillis();
		
		if(detalleContratoComision != null) {
			CrearSubdetalleContratoComision crearSubdetalleContratoComision = new CrearSubdetalleContratoComision();
			EntradaCrearSubdetalleContratoComision entradaSubdetalle = new EntradaCrearSubdetalleContratoComision();
			entradaSubdetalle.p_IdDetalleContratoComision = detalleContratoComision.IdDetalleContratoComision;
			entradaSubdetalle.p_SubdetallesContratoComision = detalleContratoComision.SubdetallesContratoComision;
			crearSubdetalleContratoComision.logica(con, entradaSubdetalle);
		}
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearDetalleContratoComision.crearSubdetalles", finServicio - inicioServicio);
		
	}
	
	
	
	public static void main(String[] args) throws Exception {
		
		System.out.println("== TEST ==");		                  
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();

			String jsonEntrada = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"PLA\",\"p_UsaReglasNegocio\":\"N\",\"p_DetallesContratoComision\":[{\"IdContratoComision\":104,\"IdDetalleContratoComision\":null,\"IdCobroClasificacion\":2,\"IdCobro\":\"COFI\",\"EsPorRangos\":\"N\",\"IdConcepto\":\"123444\",\"SubdetallesContratoComision\":[{\"IdDetalleContratoComision\":null,\"IdSubdetalleContratoComision\":null,\"Monto\":100,\"Porcentaje\":null,\"MontoMaximo\":null,\"Estado\":\"add\"},{\"IdDetalleContratoComision\":null,\"IdSubdetalleContratoComision\":null,\"Monto\":1000,\"Porcentaje\":null,\"MontoMaximo\":null,\"Estado\":\"add\"}]}]}";
			EntradaCrearDetalleContratoComision in = gson.fromJson(jsonEntrada, EntradaCrearDetalleContratoComision.class);
			CrearDetalleContratoComision detalleContratoComision = new CrearDetalleContratoComision();
			try {
				SalidaCrearDetalleContratoComision detalleContratoComisionSalida = detalleContratoComision.logica(con, in);
				con.rollback();
				System.out.println(detalleContratoComisionSalida.toString());
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
						
    	}
		System.out.println("== END ==");
		
	}

	
	
	

}
