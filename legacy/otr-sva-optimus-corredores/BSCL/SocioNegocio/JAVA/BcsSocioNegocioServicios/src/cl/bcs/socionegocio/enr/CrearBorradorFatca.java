package cl.bcs.socionegocio.enr;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.enr.pojo.EntradaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.ResultadoAdministrarBorradorEntidad;
import cl.bcs.socionegocio.enr.pojo.SalidaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.Fatca;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearBorradorFatca implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(CrearBorradorFatca.class);
	
	private String SQL_INSERT_FATCA = loadResourceToString("cl/bcs/socionegocio/enr/sql/CrearBorradorFatca_InsertFatca.sql");
	
	public static class EntradaCrearBorradorFatca extends EntradaAdministrarBorrador{
		public List<Fatca> p_Fatcas;
		
		public EntradaCrearBorradorFatca(){
			this.p_Fatcas = new ArrayList<Fatca>();
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaCrearBorradorFatca jin = gson.fromJson(in.toString(), EntradaCrearBorradorFatca.class);
		SalidaAdministrarBorrador out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaAdministrarBorrador logica(Connection con, EntradaCrearBorradorFatca entrada) throws Exception {
		logger.info("INICIO::CrearBorradorFatca");
		long inicioServicio = System.currentTimeMillis();
		SalidaAdministrarBorrador out = new SalidaAdministrarBorrador();
		
		out = entrada.validarCamposObligatoriosEntrada(con, entrada.p_Fatcas, Fatca.class, out);
		out = entrada.validarGrillasObligatoriasEntrada(con, entrada.p_Fatcas, "Fatcas", out);
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		
		List<Fatca> fatcas = obtenerEntidadesFatca(con, entrada.p_Fatcas);
		
		insertarFatcas(con, fatcas);
		
		out.P_OUT_REFID = obtenerIdsInsertados(fatcas);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearBorradorFatca", finServicio - inicioServicio);
		return out;
	}

	private List<Fatca> obtenerEntidadesFatca(Connection con, List<Fatca> borradoresFatcas) {
		logger.trace("INICIO::CrearBorradorFatca.obtenerEntidadesFatca");
		long inicio = System.currentTimeMillis();
		
		List<Fatca> fatcas = borradoresFatcas.parallelStream().map(borradorFatca->{
			Fatca fatca = new Fatca();
			fatca.IdInterno = borradorFatca.IdInterno;
			fatca.IdEnrolamiento = borradorFatca.IdEnrolamiento;
			fatca.IdClasificacionFatca = borradorFatca.IdClasificacionFatca;
			fatca.IdClasificacionEmpresaFatca = borradorFatca.IdClasificacionEmpresaFatca;
			fatca.NumeroGIIN = borradorFatca.NumeroGIIN;
			fatca.FechaFirmaFormulario = borradorFatca.FechaFirmaFormulario;
			fatca.AutorizacionInformarOrganismo = borradorFatca.AutorizacionInformarOrganismo;
			fatca.ExcepcionResidente = borradorFatca.ExcepcionResidente;
			fatca.ExcepcionNacimiento = borradorFatca.ExcepcionNacimiento;
			fatca.ExcepcionDomicilio = borradorFatca.ExcepcionDomicilio;
			fatca.ExcepcionTelefono = borradorFatca.ExcepcionTelefono;
			fatca.ExcepcionOrdenPermanente = borradorFatca.ExcepcionOrdenPermanente;
			fatca.ExcepcionFirma = borradorFatca.ExcepcionFirma;
			fatca.ParticipacionSociedadesUSA = borradorFatca.ParticipacionSociedadesUSA;
			fatca.OrdenPermanente = borradorFatca.OrdenPermanente;
			fatca.NACEEUU = borradorFatca.NACEEUU;
			fatca.NACEEUUExplicacion = borradorFatca.NACEEUUExplicacion;
			fatca.EntidadFinanciera = borradorFatca.EntidadFinanciera;
			fatca.TipoInstitucionFinanciera = borradorFatca.TipoInstitucionFinanciera;
			fatca.EntidadGestionada = borradorFatca.EntidadGestionada;
			fatca.IngresosPasivos = borradorFatca.IngresosPasivos;
			fatca.GenerarIngresosPasivos = borradorFatca.GenerarIngresosPasivos;
			fatca.CategoriaEntidad = borradorFatca.CategoriaEntidad;
			fatca.GIINEntidad = borradorFatca.GIINEntidad;
			fatca.RazonSocial = borradorFatca.RazonSocial;
			fatca.FechaLiquidacion = borradorFatca.FechaLiquidacion;
			fatca.NombreMercado = borradorFatca.NombreMercado;
			fatca.FechaConstitucion = borradorFatca.FechaConstitucion;
			fatca.ObligacionesFiscales = borradorFatca.ObligacionesFiscales;
			fatca.ObligacionesFiscalesExplicacion = borradorFatca.ObligacionesFiscalesExplicacion;
			fatca.IndividuoControl = borradorFatca.IndividuoControl;
			return fatca;
		}).collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorFatca.obtenerEntidadesFatca", fin - inicio);
		return fatcas;
	}
	
	private void insertarFatcas(Connection con, List<Fatca> fatcas) throws Exception {
		logger.debug("INICIO::CrearBorradorFatca.insertarFatcas");
		long inicio = System.currentTimeMillis();		
		SQL sql = new SQL();
		sql.batch(con, SQL_INSERT_FATCA, fatcas, Fatca.class);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBorradorFatca.insertarFatcas", fin - inicio);
	}
	
	private List<ResultadoAdministrarBorradorEntidad> obtenerIdsInsertados(List<Fatca> fatcas){
		logger.trace("INICIO::CrearBorradorFatca.obtenerIdsInsertados");
		long inicio = System.currentTimeMillis();
		
		List<ResultadoAdministrarBorradorEntidad> idsInsertados = fatcas.parallelStream().map(fatca->{
			ResultadoAdministrarBorradorEntidad resultadoCreacionBorradorEntidad = new ResultadoAdministrarBorradorEntidad();
			resultadoCreacionBorradorEntidad.IdInterno = fatca.IdInterno;
			resultadoCreacionBorradorEntidad.IdEntidad = fatca.IdEnrolamiento;
			return resultadoCreacionBorradorEntidad;
		}).collect(Collectors.toList());
		long fin = System.currentTimeMillis();
		
		logger.trace("FIN::CrearBorradorFatca.obtenerIdsInsertados", fin - inicio);
		return idsInsertados;
	}
	
	 public static void main(String[] args) {
		 try (Connection con = PlataformaUtils.getDESAConnection()) {
			 String requestBody = "";

			 Gson gson = Utils.getGsonBuilder();
			 EntradaCrearBorradorFatca entrada = gson.fromJson(requestBody, EntradaCrearBorradorFatca.class);

			 CrearBorradorFatca modificarBorradorFatca = new CrearBorradorFatca();
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
