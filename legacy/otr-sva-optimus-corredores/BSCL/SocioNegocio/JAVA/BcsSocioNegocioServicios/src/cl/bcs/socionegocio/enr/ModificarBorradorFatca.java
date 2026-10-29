package cl.bcs.socionegocio.enr;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.enr.pojo.EntradaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.ResultadoAdministrarBorradorEntidad;
import cl.bcs.socionegocio.enr.pojo.SalidaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.Fatca;
import cl.bcs.socionegocio.enr.pojo.Validacion;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarBorradorFatca implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(ModificarBorradorFatca.class);
	
	private String SQL_SELECT_FATCA = loadResourceToString("cl/bcs/socionegocio/enr/sql/ModificarBorradorFatca_SelectFatca.sql");
	private String SQL_UPDATE_FATCA = loadResourceToString("cl/bcs/socionegocio/enr/sql/ModificarBorradorFatca_UpdateFatca.sql");
	
	public static class EntradaModificarBorradorFatca extends EntradaAdministrarBorrador{
		public List<Fatca> p_Fatcas;
		
		public EntradaModificarBorradorFatca(){
			this.p_Fatcas = new ArrayList<Fatca>();
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaModificarBorradorFatca jin = gson.fromJson(in.toString(), EntradaModificarBorradorFatca.class);
		SalidaAdministrarBorrador out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaAdministrarBorrador logica(Connection con, EntradaModificarBorradorFatca entrada) throws Exception {
		logger.info("INICIO::ModificarBorradorFatca");
		long inicioServicio = System.currentTimeMillis();
		SalidaAdministrarBorrador out = new SalidaAdministrarBorrador();
		
		out = entrada.validarCamposObligatoriosEntrada(con, entrada.p_Fatcas, Fatca.class, out);
		out = entrada.validarGrillasObligatoriasEntrada(con, entrada.p_Fatcas, "Fatcas", out);
		
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		
		List<Validacion> validaciones = validarExistenciaFatca(con, entrada.p_Fatcas, entrada.p_IdEmpresa);
		out.agregarValidacionesSalida(validaciones);
		
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		
		actualizarFatcas(con, entrada.p_Fatcas);
		
		out.P_OUT_REFID = obtenerIdsActualizados(entrada.p_Fatcas);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarBorradorFatca", finServicio - inicioServicio);
		return out;
	}
	
	private List<Validacion> validarExistenciaFatca(Connection con, List<Fatca> fatcas, Long idEmpresa) throws Exception {
		logger.debug("INICIO::ModificarBorradorFatca.validarExistenciaFatca");
		long inicio = System.currentTimeMillis();
		
		List<Long> idEnrolamiento = fatcas.parallelStream().map(fatca->{
			return fatca.IdEnrolamiento;
		}).collect(Collectors.toList());
		
		HashMap<String, Object> parametros = new HashMap<String, Object>(1);
		parametros.put("IdEnrolamiento", idEnrolamiento);
		
		SQL sql = new SQL();
		List<Fatca> fatcasExistentes = sql.query(con, SQL_SELECT_FATCA, parametros, Fatca.class);
		Map<Long, Fatca> fatcasPorIdEnrolamiento = fatcasExistentes.parallelStream().collect(Collectors.toMap(fatca->fatca.IdEnrolamiento, fatca->fatca));
		
		List<Validacion> validaciones = new ArrayList<Validacion>();
		
		for(Fatca fatca : fatcas) {
			if(fatcasPorIdEnrolamiento.get(fatca.IdEnrolamiento) == null) {
				Validacion validacion = new Validacion();
				validacion.EsError = true;
				HashMap<String,String> datosMensajes = new HashMap<String,String>(1);
				datosMensajes.put("EntidadGestionada", fatca.EntidadGestionada);
				validacion.MensajeError = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR, "UI_ERROR_BORRADOR_FATCA_NO_EXISTE", datosMensajes, con, idEmpresa);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarBorradorFatca.validarExistenciaFatca", fin - inicio);
		return validaciones;
	}
	
	private void actualizarFatcas(Connection con, List<Fatca> fatcas) throws Exception {
		logger.debug("INICIO::ModificarBorradorFatca.actualizarFatcas");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		sql.batch(con, SQL_UPDATE_FATCA, fatcas, Fatca.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarBorradorFatca.actualizarFatcas", fin - inicio);
	}
	
	private List<ResultadoAdministrarBorradorEntidad> obtenerIdsActualizados(List<Fatca> fatcas){
		logger.trace("INICIO::ModificarBorradorFatca.obtenerIdsActualizados");
		long inicio = System.currentTimeMillis();
		
		List<ResultadoAdministrarBorradorEntidad> idsInsertados = fatcas.parallelStream().map(fatca->{
			ResultadoAdministrarBorradorEntidad resultadoCreacionBorradorEntidad = new ResultadoAdministrarBorradorEntidad();
			resultadoCreacionBorradorEntidad.IdInterno = fatca.IdInterno;
			resultadoCreacionBorradorEntidad.IdEntidad = fatca.IdEnrolamiento;
			return resultadoCreacionBorradorEntidad;
		}).collect(Collectors.toList());
		long fin = System.currentTimeMillis();
		
		logger.trace("FIN::ModificarBorradorFatca.obtenerIdsActualizados", fin - inicio);
		return idsInsertados;
	}
	
	 public static void main(String[] args) {
		 try (Connection con = PlataformaUtils.getDESAConnection()) {
			 String requestBody = "{\"p_Fatcas\":[{\"IdInterno\":0,\"IdEnrolamiento\":30,\"IdClasificacionFatca\":0,\"IdClasificacionEmpresaFatca\":\"0\",\"NumeroGIIN\":\"\",\"FechaFirmaFormulario\":\"1900-01-01\",\"AutorizacionInformarOrganismo\":\"\",\"ExcepcionResidente\":\"\",\"ExcepcionNacimiento\":\"\",\"ExcepcionDomicilio\":\"\",\"ExcepcionTelefono\":\"\",\"ExcepcionOrdenPermanente\":\"\",\"ExcepcionFirma\":\"\",\"ParticipacionSociedadesUSA\":\"\",\"OrdenPermanente\":\"\",\"NACEEUU\":\"UI_CERTIFICADO_NAC_EEUU2\",\"NACEEUUExplicacion\":\"HOLA\",\"EntidadFinanciera\":\"\",\"TipoInstitucionFinanciera\":\"\",\"EntidadGestionada\":\"\",\"IngresosPasivos\":\"\",\"GenerarIngresosPasivos\":\"\",\"CategoriaEntidad\":\"\",\"GIINEntidad\":\"\",\"RazonSocial\":\"\",\"FechaLiquidacion\":\"1900-01-01 00:00:00.000\",\"NombreMercado\":\"\",\"FechaConstitucion\":\"1900-01-01 00:00:00.000\",\"ObligacionesFiscales\":\"\",\"ObligacionesFiscalesExplicacion\":\"\",\"IndividuoControl\":\"\"}]}";

			 Gson gson = Utils.getGsonBuilder();
			 EntradaModificarBorradorFatca entrada = gson.fromJson(requestBody, EntradaModificarBorradorFatca.class);

			 ModificarBorradorFatca modificarBorradorFatca = new ModificarBorradorFatca();
			 try {
				 SalidaAdministrarBorrador salida = modificarBorradorFatca.logica(con, entrada);

				 System.out.println(salida);
				 System.out.println("Fin metodo main");
			 } catch (Exception innerException) {
				 innerException.printStackTrace();
				 con.rollback();
			 }
	     } catch (SQLException e) {
	    	 e.printStackTrace();
	     }
	 }
}
