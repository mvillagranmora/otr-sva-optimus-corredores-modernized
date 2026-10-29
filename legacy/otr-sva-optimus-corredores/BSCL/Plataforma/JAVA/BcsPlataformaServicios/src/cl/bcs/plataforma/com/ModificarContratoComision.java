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

import cl.bcs.plataforma.com.ValidarExistenciaContratoComisionComun.EntradaValidarExistenciaContratoComisionComun;
import cl.bcs.plataforma.com.ValidarExistenciaTipoContratoComision.EntradaValidarExistenciaTipoContratoComision;
import cl.bcs.plataforma.com.pojo.ContratoComision;
import cl.bcs.plataforma.com.pojo.ResultadoContratoComision;
import cl.bcs.plataforma.com.pojo.Validacion;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarContratoComision implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(CrearContratoComision.class);
	
	private final String SQL_SELECT_CONTRATO_COMISION = loadResourceToString("cl/bcs/plataforma/com/sql/ModificarContratoComision_SelectContratoComision.sql");
	private final String SQL_UPDATE_CONTRATO_COMISION = loadResourceToString("cl/bcs/plataforma/com/sql/ModificarContratoComision_UpdateContratoComision.sql");

	public static class EntradaModificarContratoComision extends BcsPojo{
		
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;
		public String 	p_UsaReglasNegocio;

		public List<ContratoComision> p_ContratosComisiones;
		
		public EntradaModificarContratoComision() {
			this.p_ContratosComisiones = new ArrayList<ContratoComision>();
		}

	}
	
	public static class SalidaModificarContratoComision extends BcsPojo{

		public List<MensajeSalida>				P_OUT_MENSAJES;
		public Long								P_OUT_ERRORESCONTADOR;
		public List<ResultadoContratoComision> 	P_OUT_REFID;
		
		public SalidaModificarContratoComision() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_REFID = new ArrayList<ResultadoContratoComision>();
			
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
		EntradaModificarContratoComision in = gson.fromJson(jin.toString(), EntradaModificarContratoComision.class);
		SalidaModificarContratoComision out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaModificarContratoComision logica(Connection con, EntradaModificarContratoComision in) throws Exception{
		logger.info("INICIO::ModificarContratoComision");
		Long inicioServicio = System.currentTimeMillis();
		
		SalidaModificarContratoComision out = new SalidaModificarContratoComision();
		if(out.P_OUT_ERRORESCONTADOR > 0) return out;
	
		List<Validacion> validaciones = validarExistenciaContratoComision(con, in.p_ContratosComisiones, in.p_IdEmpresa);
		out.agregarValidacionesSalida(validaciones);
		if(out.P_OUT_ERRORESCONTADOR > 0) return out;
		
		//Valida si ya existe un registro con el mismo mercado, moneda, area cliente y estado.
		ValidarExistenciaTipoContratoComision validarExistenciaTipoContratoComisionComun = new ValidarExistenciaTipoContratoComision();
		EntradaValidarExistenciaTipoContratoComision entradaValidarExistenciaTipoContratoComision = new EntradaValidarExistenciaTipoContratoComision();
		entradaValidarExistenciaTipoContratoComision.p_IdEmpresa = in.p_IdEmpresa;
		entradaValidarExistenciaTipoContratoComision.p_ContratosComisiones = in.p_ContratosComisiones;
		List<Validacion> validacionTipoContratoComision = validarExistenciaTipoContratoComisionComun.logica(con, entradaValidarExistenciaTipoContratoComision).validaciones;
		out.agregarValidacionesSalida(validacionTipoContratoComision);
		if(out.P_OUT_ERRORESCONTADOR > 0) return out;
		
		//Valida si ya existe un registro identico
		ValidarExistenciaContratoComisionComun validarExistenciaContratoComisionComun = new ValidarExistenciaContratoComisionComun();
		EntradaValidarExistenciaContratoComisionComun entradaValidarExistenciaContratoComisionComun = new EntradaValidarExistenciaContratoComisionComun();
		entradaValidarExistenciaContratoComisionComun.p_IdEmpresa = in.p_IdEmpresa;
		entradaValidarExistenciaContratoComisionComun.p_ContratosComisiones = in.p_ContratosComisiones;
		List<Validacion> validacionContratoComisionComun = validarExistenciaContratoComisionComun.logica(con, entradaValidarExistenciaContratoComisionComun).validaciones;
		out.agregarValidacionesSalida(validacionContratoComisionComun);
		if(out.P_OUT_ERRORESCONTADOR > 0) return out;
		
		actualizarContratosComisiones(con, in.p_ContratosComisiones);
		out.P_OUT_REFID = obtenerIdsActualizados(in.p_ContratosComisiones);
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarContratoComision", finServicio - inicioServicio);
		return out;
	}
	
	private List<Validacion> validarExistenciaContratoComision(Connection con, List<ContratoComision> contratosComisiones, Long idEmpresa) throws Exception {
        logger.debug("INICIO::ModificarContratoComision.validarExistenciaContratoComision");
        Long inicioServicio = System.currentTimeMillis();

        List<Long> idContratoComision = contratosComisiones.parallelStream().map(contratoComision -> contratoComision.IdContratoComision).collect(Collectors.toList());
		
		HashMap<String,Object> params = new HashMap<String, Object>(2);
		params.put("IdContratoComision", idContratoComision);
	

        SQL sql = new SQL();
    	List<ContratoComision> contratosComisionesExistentes = sql.query(con, SQL_SELECT_CONTRATO_COMISION, params, ContratoComision.class);
        Map<Long, ContratoComision> contratosComisionesPorId = contratosComisionesExistentes.parallelStream().collect(Collectors.toMap(contratoComision -> contratoComision.IdContratoComision, contratoComision -> contratoComision));

        List<Validacion> validaciones = new ArrayList<>();

        for (ContratoComision contratoComision : contratosComisiones) {
            if (contratosComisionesPorId.get(contratoComision.IdContratoComision) == null) {
                Validacion validacion = new Validacion();
                validacion.EsError = true;
                String msg = Diccionario.getMensaje(con, idEmpresa, "UI_CONTRATO_COMISION_NO_EXISTE").msg();
                validacion.MensajeError = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_EDITAR_CONTRATO", msg);
                validaciones.add(validacion);
            }
        }

        Long finServicio = System.currentTimeMillis();
        logger.debug("FIN::ModificarContratoComision.validarExistenciaContratoComision", finServicio - inicioServicio);

        return validaciones;
    }
	
	
	
	private void actualizarContratosComisiones(Connection con, List<ContratoComision> contratosComisiones) throws Exception {
		
		logger.info("INICIO::ModificarContratoComision.insertarContratosComisiones");
		Long inicioServicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		sql.batch(con, SQL_UPDATE_CONTRATO_COMISION, contratosComisiones, ContratoComision.class);
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarContratoComision.insertarnCotratosComisiones", finServicio - inicioServicio);
	}
	
	public List<ResultadoContratoComision> obtenerIdsActualizados(List<ContratoComision> contratosComisiones) {
		
		logger.info("INICIO::ModificarContratoComision.obtenerIdsActualizados");
		Long inicioServicio = System.currentTimeMillis();
		
		List<ResultadoContratoComision> resultadoIdsActualizados = new ArrayList<ResultadoContratoComision>();
		AtomicLong i = new AtomicLong(0L);
		contratosComisiones.stream()
							.forEach( cc -> {
								ResultadoContratoComision resultadoContratoComision = new ResultadoContratoComision();
								resultadoContratoComision.IdInterno = i.getAndIncrement();
								resultadoContratoComision.IdContratoComision = cc.IdContratoComision;
								resultadoIdsActualizados.add(resultadoContratoComision);
									
							});
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarContratoComision.obtenerIdsActualizados", finServicio - inicioServicio);
		return resultadoIdsActualizados;
	}
	
	
	public static void main(String[] args) throws Exception {
	
		System.out.println("== TEST ==");		                  
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();

			String jsonEntrada = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"PLA\",\"p_UsaReglasNegocio\":\"N\",\"p_ContratosComisiones\":[{\"IdEmpresa\":1,\"IdContratoComision\":75,\"IdTipoContrato\":\"CONT\",\"Nombre\":\"ASD\",\"Descripcion\":\"ASD\",\"CodigoArea\":\"ADMCAR\",\"IdFormatoCobro\":\"AGRRUT\",\"IdClasificacionTipoInstrumento\":\"DV\",\"IdMoneda\":\"USD\",\"IdSegmentoCliente\":3,\"IdPortafolio\":1451976,\"IdCliente\":1424217,\"IdEstado\":\"ACT\"}]}";
			EntradaModificarContratoComision ent = gson.fromJson(jsonEntrada, EntradaModificarContratoComision.class);
			ModificarContratoComision contratoComision = new ModificarContratoComision();
			try {
				SalidaModificarContratoComision contratoComisionSalida = contratoComision.logica(con, ent);
				con.rollback();
				System.out.println(contratoComisionSalida.toString());
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
						
    	}
		System.out.println("== END ==");
		
	}

}
