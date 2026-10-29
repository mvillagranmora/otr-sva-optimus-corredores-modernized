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

import cl.bcs.plataforma.com.ValidarExistenciaContratoComisionComun.EntradaValidarExistenciaContratoComisionComun;
import cl.bcs.plataforma.com.ValidarExistenciaTipoContratoComision.EntradaValidarExistenciaTipoContratoComision;
import cl.bcs.plataforma.com.pojo.ContratoComision;
import cl.bcs.plataforma.com.pojo.ResultadoContratoComision;
import cl.bcs.plataforma.com.pojo.Validacion;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearContratoComision implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(CrearContratoComision.class);
	
	private final String SQL_INSERT_CONTRATO_COMISION = loadResourceToString("cl/bcs/plataforma/com/sql/CrearContratoComision_InsertContratoComision.sql");

	public static class EntradaCrearContratoComision extends BcsPojo{
		
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;
		public String 	p_UsaReglasNegocio;
		
		public List<ContratoComision> p_ContratosComisiones;
		
		public EntradaCrearContratoComision() {
			this.p_ContratosComisiones = new ArrayList<ContratoComision>();
		}
	}
	
	public static class SalidaCrearContratoComision extends BcsPojo{

		public List<MensajeSalida>				P_OUT_MENSAJES;
		public Long								P_OUT_ERRORESCONTADOR;
		public List<ResultadoContratoComision> 	P_OUT_REFID;
		
		public SalidaCrearContratoComision() {
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
		EntradaCrearContratoComision in = gson.fromJson(jin.toString(), EntradaCrearContratoComision.class);
		SalidaCrearContratoComision out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCrearContratoComision logica(Connection con, EntradaCrearContratoComision in) throws Exception{
		logger.info("INICIO::CrearContratoComision");
		Long inicioServicio = System.currentTimeMillis();
		
		SalidaCrearContratoComision out = new SalidaCrearContratoComision();
		
		if(out.P_OUT_ERRORESCONTADOR > 0) return out;
		
		//Valida si ya existe un registro con el mismo mercado, moneda, area cliente y estado.
		ValidarExistenciaTipoContratoComision validarExistenciaTipoContratoComision = new ValidarExistenciaTipoContratoComision();
		EntradaValidarExistenciaTipoContratoComision entradaValidarExistenciaTipoContratoComision = new EntradaValidarExistenciaTipoContratoComision();
		entradaValidarExistenciaTipoContratoComision.p_IdEmpresa = in.p_IdEmpresa;
		entradaValidarExistenciaTipoContratoComision.p_ContratosComisiones = in.p_ContratosComisiones;
		List<Validacion> validacionTipoContratoComision = validarExistenciaTipoContratoComision.logica(con, entradaValidarExistenciaTipoContratoComision).validaciones;
		out.agregarValidacionesSalida(validacionTipoContratoComision);
		if(out.P_OUT_ERRORESCONTADOR > 0) return out;
		
		//Valida si ya existe un contrato identico
		ValidarExistenciaContratoComisionComun validarExistenciaContratoComisionComun = new ValidarExistenciaContratoComisionComun();
		EntradaValidarExistenciaContratoComisionComun entradaValidarExistenciaContratoComision = new EntradaValidarExistenciaContratoComisionComun();
		entradaValidarExistenciaContratoComision.p_IdEmpresa = in.p_IdEmpresa;
		entradaValidarExistenciaContratoComision.p_ContratosComisiones = in.p_ContratosComisiones;
		List<Validacion> validacionContratoComisionComun = validarExistenciaContratoComisionComun.logica(con, entradaValidarExistenciaContratoComision).validaciones;
		out.agregarValidacionesSalida(validacionContratoComisionComun);
		if(out.P_OUT_ERRORESCONTADOR > 0) return out;
		
		List<ContratoComision> contratosComisiones = obtenerContratosComisiones(con, in);
		insertarContratosComisiones(con, contratosComisiones);
		out.P_OUT_REFID = obtenerIdsInsertados(contratosComisiones);
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearContratoComision", finServicio - inicioServicio);
		return out;
	}
	
	
	private List<ContratoComision> obtenerContratosComisiones(Connection con, EntradaCrearContratoComision in) throws Exception {
		
		logger.info("INICIO::CrearContratoComision.obtenerCotratosComisiones");
		Long inicioServicio = System.currentTimeMillis();
		
		List<Long> secuenciasContratoComision = Utils.getSecuencia(con, "BSCL.Plataforma.BD.Secuencias::COM.ContratoComision", 1L);
		
		List<ContratoComision> contratosComisiones = new ArrayList<ContratoComision>();
		Integer i = 0;
 
		for(ContratoComision entradaContratoComision: in.p_ContratosComisiones) {
			
			ContratoComision contratoComision 				= new ContratoComision();
			contratoComision.IdContratoComision				= secuenciasContratoComision.get(i);
			contratoComision.Nombre							= entradaContratoComision.Nombre;
			contratoComision.Descripcion					= entradaContratoComision.Descripcion;
			contratoComision.IdTipoContrato 				= entradaContratoComision.IdTipoContrato;
			contratoComision.CodigoArea 					= entradaContratoComision.CodigoArea;
			contratoComision.IdFormatoCobro 				= entradaContratoComision.IdFormatoCobro;
			contratoComision.IdClasificacionTipoInstrumento = entradaContratoComision.IdClasificacionTipoInstrumento;
			contratoComision.IdMoneda 						= entradaContratoComision.IdMoneda; 
			contratoComision.IdSegmentoCliente				= entradaContratoComision.IdSegmentoCliente;
			contratoComision.IdPortafolio					= entradaContratoComision.IdPortafolio;
			contratoComision.IdCliente						= entradaContratoComision.IdCliente;
			contratoComision.IdEmpresa						= in.p_IdEmpresa;
			contratoComision.IdEstado					 	= entradaContratoComision.IdEstado;
			contratoComision.FechaIngresoRegistro			= FechaDB.ObtenerFechaHoraSistemaTs(con, in.p_IdEmpresa);
			
			contratosComisiones.add(contratoComision);
			i++;
			
		}
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearContratoComision.obtenerCotratosComisiones", finServicio - inicioServicio);
		return contratosComisiones;
	}
	
	private void insertarContratosComisiones(Connection con, List<ContratoComision> contratosComisiones) throws Exception {
		
		logger.info("INICIO::CrearContratoComision.insertarCotratosComisiones");
		Long inicioServicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		sql.batch(con, SQL_INSERT_CONTRATO_COMISION, contratosComisiones, ContratoComision.class);
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearContratoComision.insertarCotratosComisiones", finServicio - inicioServicio);
	}
	
	public List<ResultadoContratoComision> obtenerIdsInsertados(List<ContratoComision> contratosComisiones) {
		
		logger.info("INICIO::CrearContratoComision.obtenerIdsInsertados");
		Long inicioServicio = System.currentTimeMillis();
		
		List<ResultadoContratoComision> resultadoIdsInsertados = new ArrayList<ResultadoContratoComision>();
		AtomicLong i = new AtomicLong(0L);
		contratosComisiones.stream()
							.forEach( cc -> {
								ResultadoContratoComision resultadoContratoComision = new ResultadoContratoComision();
								resultadoContratoComision.IdInterno = i.getAndIncrement();
								resultadoContratoComision.IdContratoComision = cc.IdContratoComision;
								resultadoIdsInsertados.add(resultadoContratoComision);
									
							});
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearContratoComision.obtenerIdsInsertados", finServicio - inicioServicio);
		return resultadoIdsInsertados;
	}
	
	
	public static void main(String[] args) throws Exception {
	
		System.out.println("== TEST ==");		                  
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();

			String jsonEntrada = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"PLA\",\"p_UsaReglasNegocio\":\"N\",\"p_ContratosComisiones\":[{\"IdEmpresa\":1,\"IdContratoComision\":null,\"IdTipoContrato\":\"CONV\",\"Nombre\":\"ABEL CONTRATO\",\"Descripcion\":\"\",\"CodigoArea\":\"ADMCAR\",\"IdFormatoCobro\":\"AGRRUT\",\"IdClasificacionTipoInstrumento\":\"FM\",\"IdMoneda\":\"USD\",\"IdSegmentoCliente\":3,\"IdPortafolio\":1449415,\"IdCliente\":1419659,\"IdEstado\":\"INA\"}]}";
			EntradaCrearContratoComision ent = gson.fromJson(jsonEntrada, EntradaCrearContratoComision.class);
			CrearContratoComision contratoComision = new CrearContratoComision();
			try {
				SalidaCrearContratoComision contratoComisionSalida = contratoComision.logica(con, ent);
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
