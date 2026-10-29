package cl.bcs.socionegocio.enr;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.socionegocio.enr.pojo.DireccionElectronica;
import cl.bcs.socionegocio.enr.pojo.EntradaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.ResultadoAdministrarBorradorEntidad;
import cl.bcs.socionegocio.enr.pojo.SalidaAdministrarBorrador;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearBorradorDireccionElectronica implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(CrearBorradorDireccionElectronica.class);
	
	private String SQL_INSERT_DIRECCION_ELECTRONICA = loadResourceToString("cl/bcs/socionegocio/enr/sql/CrearBorradorDireccionElectronica_InsertDireccionElectronica.sql");
	
	public static class EntradaCrearBorradorDireccionElectronica extends EntradaAdministrarBorrador {
		public List<DireccionElectronica> p_DireccionesElectronicas;
		
		public EntradaCrearBorradorDireccionElectronica(){
			this.p_DireccionesElectronicas = new ArrayList<DireccionElectronica>();
		}
	}
			
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaCrearBorradorDireccionElectronica jin = gson.fromJson(in.toString(), EntradaCrearBorradorDireccionElectronica.class);
		SalidaAdministrarBorrador out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaAdministrarBorrador logica(Connection con, EntradaCrearBorradorDireccionElectronica entrada) throws Exception {
		logger.info("INICIO::CrearBorradorDireccionElectronica");
		long inicioServicio = System.currentTimeMillis();
		SalidaAdministrarBorrador out = new SalidaAdministrarBorrador();
		
		out = entrada.validarCamposObligatoriosEntrada(con, entrada.p_DireccionesElectronicas, DireccionElectronica.class, out);
		out = entrada.validarGrillasObligatoriasEntrada(con, entrada.p_DireccionesElectronicas, "DireccionesElectronicas", out);
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		
		List<DireccionElectronica> direccionesElectronicas = obtenerDireccionesElectronicas(con, entrada.p_DireccionesElectronicas);
		
		insertarDireccionesElectronicas(con, direccionesElectronicas);
		
		out.P_OUT_REFID = obtenerIdsInsertados(direccionesElectronicas);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearBorradorDireccionElectronica", finServicio - inicioServicio);
		return out;
	}
	
	private List<DireccionElectronica> obtenerDireccionesElectronicas(Connection con, List<DireccionElectronica> borradoresDireccionesElectronicas) throws Exception{
		logger.trace("INICIO::CrearBorradorDireccionElectronica.obtenerDireccionesElectronicas");
		long inicio = System.currentTimeMillis();
		
		List<Long> secuencias = Utils.getSecuencia(con, "BSCL.SocioNegocio.BD.Secuencias::BorradorDireccionElectronica", Long.valueOf(borradoresDireccionesElectronicas.size()));
		
		AtomicInteger index = new AtomicInteger();
		List<DireccionElectronica> direccionesElectronicas = borradoresDireccionesElectronicas.parallelStream().map(borradorDireccionElectronica->{
			Long idDireccionElectronica = secuencias.get(index.getAndIncrement());
			DireccionElectronica direccionElectronica = obtenerEntidadDireccionElectronica(borradorDireccionElectronica, idDireccionElectronica);
			return direccionElectronica;
		}).collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorDireccionElectronica.obtenerDireccionesElectronicas", fin - inicio);
		return direccionesElectronicas;
	}
	
	private DireccionElectronica obtenerEntidadDireccionElectronica(DireccionElectronica borradorDireccionElectronica, Long idDireccionFisica) {
		logger.trace("INICIO::CrearBorradorDireccionElectronica.obtenerEntidadDireccionElectronica");
		long inicio = System.currentTimeMillis();
		
		DireccionElectronica direccionElectronica = new DireccionElectronica();
		direccionElectronica.IdDireccionElectronica = idDireccionFisica;
		direccionElectronica.IdInterno = borradorDireccionElectronica.IdInterno;
		direccionElectronica.IdEnrolamiento = borradorDireccionElectronica.IdEnrolamiento;
		direccionElectronica.Direccion = borradorDireccionElectronica.Direccion;
		direccionElectronica.CodigoProposito = borradorDireccionElectronica.CodigoProposito;
		direccionElectronica.Comentario = borradorDireccionElectronica.Comentario;
		direccionElectronica.Predeterminado = borradorDireccionElectronica.Predeterminado;
		direccionElectronica.IdDireccionElectronicaPersona = borradorDireccionElectronica.IdDireccionElectronicaPersona;
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::CrearBorradorDireccionElectronica.obtenerEntidadDireccionElectronica", fin - inicio);
		return direccionElectronica;
	}
	
	private void insertarDireccionesElectronicas(Connection con,  List<DireccionElectronica> direccionesElectronicas) throws Exception {
		logger.debug("INICIO::CrearBorradorDireccionElectronica.insertarDireccionesElectronicas");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		sql.batch(con, SQL_INSERT_DIRECCION_ELECTRONICA, direccionesElectronicas, DireccionElectronica.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBorradorDireccionElectronica.insertarDireccionesElectronicas", fin - inicio);
	}
	
	private List<ResultadoAdministrarBorradorEntidad> obtenerIdsInsertados(List<DireccionElectronica> direccionesElectronicas){
		logger.trace("INICIO::CrearBorradorDireccionElectronica.obtenerIdsInsertados");
		long inicio = System.currentTimeMillis();
		
		List<ResultadoAdministrarBorradorEntidad> idsInsertados = direccionesElectronicas.parallelStream().map(direccionElectronica->{
			ResultadoAdministrarBorradorEntidad resultadoCreacionBorradorEntidad = new ResultadoAdministrarBorradorEntidad();
			resultadoCreacionBorradorEntidad.IdInterno = direccionElectronica.IdInterno;
			resultadoCreacionBorradorEntidad.IdEntidad =  direccionElectronica.IdDireccionElectronica;
			return resultadoCreacionBorradorEntidad;
		}).collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorDireccionElectronica.obtenerIdsInsertados", fin - inicio);
		return idsInsertados;
	}

	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");

		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"S\",\"p_CodigoFormulario\":\"DIRECCION_ELECTRONICA_FULL_JURIDICA\",\"p_IdTipoCanal\":\"OPT\",\"p_DireccionesElectronicas\":[{\"IdInterno\":1,\"IdDireccionElectronica\":0,\"IdEnrolamiento\":456,\"Direccion\":\"HECTOR.VERA@GAT-BLAC.COM\",\"CodigoProposito\":\"PART\",\"Comentario\":\"ASDASDA\",\"Predeterminado\":\"N\"}]}\n";
			EntradaCrearBorradorDireccionElectronica entrada = gson.fromJson(json, EntradaCrearBorradorDireccionElectronica.class);

			CrearBorradorDireccionElectronica demo = new CrearBorradorDireccionElectronica();
			System.out.println("ENTRADA");
			System.out.println(entrada);
			System.out.println("SALIDA");

			try {
				SalidaAdministrarBorrador salida = demo.logica(con, entrada);
				if (salida.P_OUT_ERRORESCONTADOR > 0) {
					con.rollback();
				}
				System.out.println(salida);
			} catch (Exception er) {
				er.printStackTrace();
			} finally {
				con.rollback();
			}
		}
		System.out.println("== END ==");
	}
}
