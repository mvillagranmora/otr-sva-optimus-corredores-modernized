package cl.bcs.socionegocio.enr;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import cl.bcs.socionegocio.enr.pojo.Ciudad;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.enr.pojo.DireccionFisica;
import cl.bcs.socionegocio.enr.pojo.EntradaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.ResultadoAdministrarBorradorEntidad;
import cl.bcs.socionegocio.enr.pojo.SalidaAdministrarBorrador;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearBorradorDireccionFisica implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(CrearBorradorDireccionFisica.class);

	private String SQL_INSERT_DIRECCION_FISICA = loadResourceToString("cl/bcs/socionegocio/enr/sql/CrearBorradorDireccionFisica_InsertDireccionFisica.sql");
	
	public static class EntradaCrearBorradorDireccionFisica extends EntradaAdministrarBorrador{
		public List<DireccionFisica> p_DireccionesFisicas;
		
		public EntradaCrearBorradorDireccionFisica(){
			this.p_DireccionesFisicas = new ArrayList<DireccionFisica>();
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaCrearBorradorDireccionFisica jin = gson.fromJson(in.toString(), EntradaCrearBorradorDireccionFisica.class);
		SalidaAdministrarBorrador out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaAdministrarBorrador logica(Connection con, EntradaCrearBorradorDireccionFisica entrada) throws Exception {
		logger.info("INICIO::CrearBorradorDireccionFisica");
		long inicioServicio = System.currentTimeMillis();
		SalidaAdministrarBorrador out = new SalidaAdministrarBorrador();

		prepararDireccionesEntrantes(con, entrada);
		
		out = entrada.validarCamposObligatoriosEntrada(con, entrada.p_DireccionesFisicas, DireccionFisica.class, out);
		out = entrada.validarGrillasObligatoriasEntrada(con, entrada.p_DireccionesFisicas, "DireccionesFisicas", out);
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		
		List<DireccionFisica> direccionesFisicas = obtenerDireccionesFisicas(con, entrada.p_DireccionesFisicas);
		
		insertarDireccionesFisicas(con, direccionesFisicas);
		
		out.P_OUT_REFID = obtenerIdsInsertados(direccionesFisicas);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearBorradorDireccionFisica", finServicio - inicioServicio);
		return out;
		
	}

	private void prepararDireccionesEntrantes(Connection con, EntradaCrearBorradorDireccionFisica entrada) throws SQLException {
		logger.debug("INICIO::CrearBorradorDireccionFisica.prepararDireccionesEntrantes");
		long inicio = System.currentTimeMillis();

		List<Long> ciudadIds = entrada.p_DireccionesFisicas.stream().filter(u -> u.IdCiudad == null).map(u -> u.IdCiudad).collect(Collectors.toList());
		Map<Long, Ciudad> ciudadPorIdCiudad = EnrolamientoUtils.obtenerCiudadPorIdCiudad(con, ciudadIds);

		for (DireccionFisica direccionFisica : entrada.p_DireccionesFisicas) {
			direccionFisica.EsBorrador = "S";

			Ciudad ciudad = ciudadPorIdCiudad.get(direccionFisica.IdCiudad);
			if (ciudad != null) {
				direccionFisica.IdPais = ciudad.IdPais;
				direccionFisica.UsaDivisionPais = ciudad.UsaDivisionPais;
			}
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBorradorDireccionFisica.prepararDireccionesEntrantes {}", fin - inicio);
	}

	private List<DireccionFisica> obtenerDireccionesFisicas(Connection con, List<DireccionFisica> borradoresDireccionesFisicas) throws Exception{
		logger.trace("INICIO::CrearBorradorDireccionFisica.obtenerDireccionesFisicas");
		long inicio = System.currentTimeMillis();
		
		List<Long> secuencias = Utils.getSecuencia(con, "BSCL.SocioNegocio.BD.Secuencias::BorradorDireccionFisica", Long.valueOf(borradoresDireccionesFisicas.size()));
		
		AtomicInteger index = new AtomicInteger();
		List<DireccionFisica> direccionesFisicas = borradoresDireccionesFisicas.parallelStream().map(borradorDireccionFisica->{
			Long idDireccionFisica = secuencias.get(index.getAndIncrement());
			DireccionFisica direccionFisica = obtenerEntidadDireccionFisica(borradorDireccionFisica, idDireccionFisica);
			return direccionFisica;
		}).collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorDireccionFisica.obtenerDireccionesFisicas", fin - inicio);
		return direccionesFisicas;
	}
	
	private DireccionFisica obtenerEntidadDireccionFisica(DireccionFisica borradorDireccionFisica, Long idDireccionFisica) {
		logger.trace("INICIO::CrearBorradorDireccionFisica.obtenerEntidadDireccionFisica");
		long inicio = System.currentTimeMillis();
		
		DireccionFisica direccionFisica = new DireccionFisica();
		direccionFisica.IdDireccionFisica = idDireccionFisica;
		direccionFisica.IdInterno = borradorDireccionFisica.IdInterno;
		direccionFisica.IdEnrolamiento = borradorDireccionFisica.IdEnrolamiento;
		direccionFisica.Calle = borradorDireccionFisica.Calle;
		direccionFisica.Numero = borradorDireccionFisica.Numero;
		direccionFisica.Departamento = borradorDireccionFisica.Departamento;
		direccionFisica.Sector = borradorDireccionFisica.Sector;
		direccionFisica.CodigoPostal = borradorDireccionFisica.CodigoPostal;
		direccionFisica.IdCiudad = borradorDireccionFisica.IdCiudad;
		direccionFisica.IdDivisionPais = borradorDireccionFisica.IdDivisionPais;
		direccionFisica.CodigoProposito = borradorDireccionFisica.CodigoProposito;
		direccionFisica.Comentario = borradorDireccionFisica.Comentario;
		direccionFisica.CodigoCompuesto = borradorDireccionFisica.CodigoCompuesto;
		direccionFisica.Predeterminado = borradorDireccionFisica.Predeterminado;
		direccionFisica.IdDireccionFisicaPersona = borradorDireccionFisica.IdDireccionFisicaPersona;
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorDireccionFisica.obtenerEntidadDireccionFisica", fin - inicio);
		return direccionFisica;
	}
	
	private void insertarDireccionesFisicas(Connection con, List<DireccionFisica> direccionesFisicas) throws Exception {
		logger.debug("INICIO::CrearBorradorDireccionFisica.insertarDireccionesFisicas");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		sql.batch(con, SQL_INSERT_DIRECCION_FISICA, direccionesFisicas, DireccionFisica.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBorradorDireccionFisica.insertarDireccionesFisicas", fin - inicio);
	}
	
	private List<ResultadoAdministrarBorradorEntidad> obtenerIdsInsertados(List<DireccionFisica> direccionesFisicas){
		logger.trace("INICIO::CrearBorradorDireccionFisica.obtenerIdsInsertados");
		long inicio = System.currentTimeMillis();
		
		List<ResultadoAdministrarBorradorEntidad> idsInsertados = direccionesFisicas.parallelStream().map(direccionFisica->{
			ResultadoAdministrarBorradorEntidad resultadoCreacionBorradorEntidad = new ResultadoAdministrarBorradorEntidad();
			resultadoCreacionBorradorEntidad.IdInterno = direccionFisica.IdInterno;
			resultadoCreacionBorradorEntidad.IdEntidad = direccionFisica.IdDireccionFisica;
			return resultadoCreacionBorradorEntidad;
		}).collect(Collectors.toList());
		long fin = System.currentTimeMillis();
		
		logger.trace("FIN::CrearBorradorDireccionFisica.obtenerIdsInsertados", fin - inicio);
		return idsInsertados;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"S\",\"p_IdTipoCanal\":\"OPT\",\"p_CodigoFormulario\":\"DIRECCION_FISICA_FULL_JURIDICA\",\"p_DireccionesFisicas\":[{\"IdInterno\":1,\"IdEnrolamiento\":456,\"IdPais\":44,\"IdDireccionFisica\":1869,\"Calle\":\"ASDASD\",\"Numero\":\"123\",\"Departamento\":\"ASD\",\"Sector\":\"AASD\",\"CodigoPostal\":\"12312\",\"IdCiudad\":788,\"IdDivisionPais\":344,\"CodigoProposito\":\"PART\",\"Comentario\":\"ASDASD\",\"CodigoCompuesto\":\"\",\"Predeterminado\":\"N\",\"CodigoDivisionPais\":\"15102\"}]}\n";
			EntradaCrearBorradorDireccionFisica Objin = gson.fromJson(json, EntradaCrearBorradorDireccionFisica.class);
			
			CrearBorradorDireccionFisica demo = new CrearBorradorDireccionFisica();
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
	 			
 			try {
 				SalidaAdministrarBorrador ObjOut = demo.logica(con, Objin);
 				con.rollback();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				er.printStackTrace();	
			}
			finally {
				con.rollback();
				con.close();
			}
		}
		System.out.println("== END ==");
	}
}
