package cl.bcs.plataforma.com;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.com.CrearSubdetalleContratoComision.EntradaCrearSubdetalleContratoComision;
import cl.bcs.plataforma.com.EliminarSubdetalleContratoComision.EntradaEliminarSubdetalleContratoComision;
import cl.bcs.plataforma.com.ModificarSubdetalleContratoComision.EntradaModificarSubdetalleContratoComision;
import cl.bcs.plataforma.com.ValidarExistenciaDetalleContratoComisionComun.EntradaValidarExistenciaDetalleContratoComisionComun;
import cl.bcs.plataforma.com.ValidarValoresSubdetalleContratoComision.EntradaValidarValoresSubdetalleContratoComision;
import cl.bcs.plataforma.com.ValidarValoresSubdetalleContratoComision.SalidaValidarValoresSubdetalleContratoComision;
import cl.bcs.plataforma.com.pojo.DetalleContratoComision;
import cl.bcs.plataforma.com.pojo.ResultadoDetalleContratoComision;
import cl.bcs.plataforma.com.pojo.SubdetalleContratoComision;
import cl.bcs.plataforma.com.pojo.Validacion;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;

import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarDetalleContratoComision implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(ModificarDetalleContratoComision.class);
	
	private final String SQL_SELECT_DETALLE_CONTRATO_COMISION = loadResourceToString("cl/bcs/plataforma/com/sql/ModificarDetalleContratoComision_SelectDetalleContratoComision.sql");
	private final String SQL_UPDATE_DETALLE_CONTRATO_COMISION = loadResourceToString("cl/bcs/plataforma/com/sql/ModificarDetalleContratoComision_UpdateDetalleContratoComision.sql");

	public static class EntradaModificarDetalleContratoComision extends BcsPojo{
		
		public Long 							p_IdEmpresa;
		public Long 							p_IdUsuario;
		public String 							p_IdModulo;
		public String 							p_UsaReglasNegocio;
		public List<DetalleContratoComision> 	p_DetallesContratoComision;
		
		public EntradaModificarDetalleContratoComision() {
			this.p_DetallesContratoComision = new ArrayList<DetalleContratoComision>();
		}
	}
	
	public static class SalidaModificarDetalleContratoComision extends BcsPojo{
		
		public List<MensajeSalida>						P_OUT_MENSAJES;
		public Long										P_OUT_ERRORESCONTADOR;
		public List<ResultadoDetalleContratoComision> 	P_OUT_REFID;
		
		public SalidaModificarDetalleContratoComision() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_REFID = new ArrayList<ResultadoDetalleContratoComision>();
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
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		EntradaModificarDetalleContratoComision in = gson.fromJson(jin.toString(), EntradaModificarDetalleContratoComision.class);
		SalidaModificarDetalleContratoComision out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaModificarDetalleContratoComision logica(Connection con, EntradaModificarDetalleContratoComision in) throws Exception{
		logger.info("INICIO::ModificarDetalleContratoComision");
		Long inicioServicio = System.currentTimeMillis();
		
		SalidaModificarDetalleContratoComision out = new SalidaModificarDetalleContratoComision();
		
		if(out.P_OUT_ERRORESCONTADOR > 0) return out;
	
		//Se valida si existe el contrato
		List<Validacion> validaciones = validarExistenciaDetalleContratoComision(con, in.p_DetallesContratoComision, in.p_IdEmpresa);
		out.agregarValidacionesSalida(validaciones);
		if(out.P_OUT_ERRORESCONTADOR > 0) return out;
	
		
		//Se valida si ya existe un detalle con los parametros especificados
		ValidarExistenciaDetalleContratoComisionComun validarExistenciaDetalleContratoComisionComun = new ValidarExistenciaDetalleContratoComisionComun();
		EntradaValidarExistenciaDetalleContratoComisionComun entradaValidarExistenciaDetalleContratoComision = new EntradaValidarExistenciaDetalleContratoComisionComun();
		entradaValidarExistenciaDetalleContratoComision.p_IdEmpresa = in.p_IdEmpresa;
		entradaValidarExistenciaDetalleContratoComision.p_DetallesContratoComision = in.p_DetallesContratoComision;
		List<Validacion> validacionExistenciaDetalleContratoComisionComun = validarExistenciaDetalleContratoComisionComun.logica(con, entradaValidarExistenciaDetalleContratoComision).validaciones;
		out.agregarValidacionesSalida(validacionExistenciaDetalleContratoComisionComun);
		if(out.P_OUT_ERRORESCONTADOR > 0) return out;
		
		actualizarDetallesContratoComision(con, in.p_DetallesContratoComision);
		out.P_OUT_REFID = obtenerIdsActualizados(in.p_DetallesContratoComision);
		
		//Agrega/editar/elimina detalles de contratos
		DetalleContratoComision detalleContratoComision = in.p_DetallesContratoComision.parallelStream().findFirst().orElse(null);
		if(detalleContratoComision != null) {
			eliminarSubdetalleContratoComision(con, detalleContratoComision, in.p_IdEmpresa);
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
		
			insertarSubdetalleContratoComision(con, detalleContratoComision);
			modificarSubdetalleContratoComision(con, detalleContratoComision, in.p_IdEmpresa);
			

		}
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarDetalleContratoComision", finServicio - inicioServicio);
		return out;
	}
	
	public void actualizarDetallesContratoComision(Connection con, List<DetalleContratoComision> detallesContratoComision) throws Exception{
		logger.info("INICIO::ModificarDetalleContratoComision.insertarDetalleCotratoComision");
		Long inicioServicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		sql.batch(con, SQL_UPDATE_DETALLE_CONTRATO_COMISION, detallesContratoComision, DetalleContratoComision.class);
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarDetalleContratoComision.insertarDetalleContratoComision", finServicio - inicioServicio);
	}
	
	public List<ResultadoDetalleContratoComision> obtenerIdsActualizados(List<DetalleContratoComision> detallesContratoComision) {
		
		logger.info("INICIO::ModificarDetalleContratoComision.obtenerIdsActualizados");
		Long inicioServicio = System.currentTimeMillis();
		
		List<ResultadoDetalleContratoComision> resultadoIdsActualizados = new ArrayList<ResultadoDetalleContratoComision>();
		AtomicLong i = new AtomicLong(0L);
		detallesContratoComision.stream()
							.forEach( dcc -> {
								ResultadoDetalleContratoComision resultadoDetalleContratoComision = new ResultadoDetalleContratoComision();
								resultadoDetalleContratoComision.IdInterno = i.getAndIncrement();
								resultadoDetalleContratoComision.IdDetalleContratoComision = dcc.IdDetalleContratoComision;
								resultadoIdsActualizados.add(resultadoDetalleContratoComision);
									
							});
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarDetalleContratoComision.obtenerIdsActualizados", finServicio - inicioServicio);
		return resultadoIdsActualizados;
	}
	
	private List<Validacion> validarExistenciaDetalleContratoComision(Connection con, List<DetalleContratoComision> detallesContratoComision, Long idEmpresa) throws Exception {
        logger.debug("INICIO::ModificarDetalleContratoComision.validarExistenciaDetalleContratoComision");
        Long inicioServicio = System.currentTimeMillis();

        List<Long> idDetalleContratoComision = detallesContratoComision.parallelStream()
        										.map(detalleContratoComision -> detalleContratoComision.IdDetalleContratoComision)
        										.collect(Collectors.toList());
		
		HashMap<String,Object> params = new HashMap<String, Object>();
		params.put("IdDetalleContratoComision", idDetalleContratoComision);

        SQL sql = new SQL();
    	List<DetalleContratoComision> detallesContratoComisionExistentes = sql.query(con, SQL_SELECT_DETALLE_CONTRATO_COMISION, params, DetalleContratoComision.class);
        Map<Long, DetalleContratoComision> detallesContratoComisionPorId = detallesContratoComisionExistentes
        																	.parallelStream()
        																	.collect(Collectors.toMap(detalleContratoComision -> detalleContratoComision.IdDetalleContratoComision,
        																								detalleContratoComision -> detalleContratoComision));

        List<Validacion> validaciones = new ArrayList<>();

        for (DetalleContratoComision detalleContratoComision : detallesContratoComision) {
            if (detallesContratoComisionPorId.get(detalleContratoComision.IdDetalleContratoComision) == null) {
                Validacion validacion = new Validacion();
                validacion.EsError = true;
                String msg = Diccionario.getMensaje(con, idEmpresa, "UI_ERROR_DETALLE_CONTRATO_NO_EXISTE").msg();
                validacion.MensajeError = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_EDITAR_DETALLE_CONTRATO", msg);
                validaciones.add(validacion);
            }
        }

        Long finServicio = System.currentTimeMillis();
        logger.debug("FIN::ModificarDetalleContratoComision.validarExistenciaDetalleContratoComision", finServicio - inicioServicio);

        return validaciones;
    }
	
	
	private void insertarSubdetalleContratoComision(Connection con, DetalleContratoComision detallesContratoComision) throws Exception {
		logger.info("INICIO::ModificarDetalleContratoComision.insertarSubdetalleContratoComision");
		Long inicioServicio = System.currentTimeMillis();
		if(!detallesContratoComision.SubdetallesContratoComision.isEmpty()) {
			CrearSubdetalleContratoComision crearSubdetalleContratoComision = new CrearSubdetalleContratoComision();
			EntradaCrearSubdetalleContratoComision entradaCrearSubdetalle 	= new EntradaCrearSubdetalleContratoComision();
			entradaCrearSubdetalle.p_IdDetalleContratoComision 				= detallesContratoComision.IdDetalleContratoComision;
			entradaCrearSubdetalle.p_SubdetallesContratoComision			= detallesContratoComision.SubdetallesContratoComision;
			crearSubdetalleContratoComision.logica(con, entradaCrearSubdetalle);
		}
		Long finServicio = System.currentTimeMillis();
	    logger.debug("FIN::ModificarDetalleContratoComision.insertarSubdetalleContratoComision", finServicio - inicioServicio);
	}
	
	private void modificarSubdetalleContratoComision(Connection con, DetalleContratoComision detallesContratoComision, Long idEmpresa) throws Exception {
		logger.info("INICIO::ModificarDetalleContratoComision.modificarSubdetalleContratoComision");
		Long inicioServicio = System.currentTimeMillis();
		
		List<SubdetalleContratoComision> subDetallesContratoComision = detallesContratoComision.SubdetallesContratoComision.stream()
																		.filter( sdcc -> sdcc.IdSubdetalleContratoComision != null)
																		.collect(Collectors.toList());
		
		ModificarSubdetalleContratoComision modificarSubdetalleContratoComision = new ModificarSubdetalleContratoComision();
		EntradaModificarSubdetalleContratoComision entradaModificarSubdetalle 	= new EntradaModificarSubdetalleContratoComision();
		entradaModificarSubdetalle.p_IdEmpresa 									= idEmpresa;
		entradaModificarSubdetalle.p_SubdetallesContratoComision 				= subDetallesContratoComision;
		modificarSubdetalleContratoComision.logica(con, entradaModificarSubdetalle);
		
		Long finServicio = System.currentTimeMillis();
	    logger.debug("FIN::ModificarDetalleContratoComision.modificarSubdetalleContratoComision", finServicio - inicioServicio);
	}
	
	private void eliminarSubdetalleContratoComision(Connection con, DetalleContratoComision detallesContratoComision, Long idEmpresa) throws Exception {
		logger.info("INICIO::ModificarDetalleContratoComision.eliminarSubdetalleContratoComision");
		Long inicioServicio = System.currentTimeMillis();
			
		List<SubdetalleContratoComision> subDetallesContratoComision = detallesContratoComision.SubdetallesContratoComision.stream()
				.filter( sdcc -> sdcc.Estado.equalsIgnoreCase("eliminado"))
				.collect(Collectors.toList());

		EliminarSubdetalleContratoComision eliminarSubdetalleContratoComision 				= new EliminarSubdetalleContratoComision();
		EntradaEliminarSubdetalleContratoComision entradaEliminarSubdetalleContratoComision = new EntradaEliminarSubdetalleContratoComision();
		entradaEliminarSubdetalleContratoComision.p_IdEmpresa								= idEmpresa;
		entradaEliminarSubdetalleContratoComision.p_SubdetallesContratoComision 			= subDetallesContratoComision;
		eliminarSubdetalleContratoComision.logica(con, entradaEliminarSubdetalleContratoComision);
		
		Long finServicio = System.currentTimeMillis();
	    logger.debug("FIN::ModificarDetalleContratoComision.eliminarSubdetalleContratoComision", finServicio - inicioServicio);
		
	}
	
	public static void main(String[] args) throws Exception {
	
		System.out.println("== TEST ==");		                  
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();

			String jsonEntrada = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"PLA\",\"p_UsaReglasNegocio\":\"N\",\"p_DetallesContratoComision\":[{\"IdContratoComision\":81,\"IdDetalleContratoComision\":65,\"IdCobroClasificacion\":1,\"IdCobro\":\"COFI\",\"EsPorRangos\":\"S\",\"IdConcepto\":\"123444\",\"SubdetallesContratoComision\":[{\"IdDetalleContratoComision\":65,\"IdSubdetalleContratoComision\":102,\"Monto\":\"100\",\"Porcentaje\":null,\"MontoMaximo\":\"1000\",\"estado\":\"eliado\"},{\"IdDetalleContratoComision\":65,\"IdSubdetalleContratoComision\":null,\"Monto\":10,\"Porcentaje\":null,\"MontoMaximo\":4,\"estado\":\"eee\"},{\"IdDetalleContratoComision\":65,\"IdSubdetalleContratoComision\":103,\"Monto\":\"10\",\"Porcentaje\":null,\"MontoMaximo\":\"1\",\"estado\":\"eliminado\"}]}]}";
			EntradaModificarDetalleContratoComision ent = gson.fromJson(jsonEntrada, EntradaModificarDetalleContratoComision.class);
			ModificarDetalleContratoComision detalleContratoComision = new ModificarDetalleContratoComision();
			try {
				SalidaModificarDetalleContratoComision detalleContratoComisionSalida = detalleContratoComision.logica(con, ent);
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
