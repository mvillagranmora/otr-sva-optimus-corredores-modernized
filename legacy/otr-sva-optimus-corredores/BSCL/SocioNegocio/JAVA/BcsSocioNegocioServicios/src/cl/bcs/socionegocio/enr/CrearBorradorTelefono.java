package cl.bcs.socionegocio.enr;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
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
import cl.bcs.socionegocio.enr.pojo.Telefono;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearBorradorTelefono implements JavaCall {

	static final Logger logger = LogManager.getLogger(CrearBorradorTelefono.class);
	
	private String SQL_INSERT_TELEFONO = loadResourceToString("cl/bcs/socionegocio/enr/sql/CrearBorradorTelefono_InsertTelefono.sql");
	
	public static class EntradaCrearBorradorTelefono extends EntradaAdministrarBorrador {
		public List<Telefono> p_Telefonos;
		
		public EntradaCrearBorradorTelefono(){
			this.p_Telefonos = new ArrayList<Telefono>();
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaCrearBorradorTelefono jin = gson.fromJson(in.toString(), EntradaCrearBorradorTelefono.class);
		SalidaAdministrarBorrador out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaAdministrarBorrador logica(Connection con, EntradaCrearBorradorTelefono entrada) throws Exception {
		logger.info("INICIO::CrearBorradorTelefono");
		long inicioServicio = System.currentTimeMillis();
		SalidaAdministrarBorrador out = new SalidaAdministrarBorrador();
		
		out = entrada.validarCamposObligatoriosEntrada(con, entrada.p_Telefonos, Telefono.class, out);
		out = entrada.validarGrillasObligatoriasEntrada(con, entrada.p_Telefonos, "Telefonos", out);
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		
		List<Telefono> telefonos = obtenerTelefonos(con, entrada.p_Telefonos);
		
		insertarTelefonos(con, telefonos);
		
		out.P_OUT_REFID = obtenerIdsInsertados(telefonos);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearBorradorTelefono", finServicio - inicioServicio);
		return out;
	}
	
	private List<Telefono> obtenerTelefonos(Connection con, List<Telefono> borradoresTelefonos) throws Exception{
		logger.trace("INICIO::CrearBorradorTelefono.obtenerTelefonos");
		long inicio = System.currentTimeMillis();
		
		List<Long> secuencias = Utils.getSecuencia(con, "BSCL.SocioNegocio.BD.Secuencias::BorradorTelefono", Long.valueOf(borradoresTelefonos.size()));
		
		AtomicInteger index = new AtomicInteger();
		List<Telefono> telefonos = borradoresTelefonos.parallelStream().map(borradorDireccionElectronica->{
			Long idTelefono = secuencias.get(index.getAndIncrement());
			Telefono telefono = obtenerEntidadTelefono(borradorDireccionElectronica, idTelefono);
			return telefono;
		}).collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorTelefono.obtenerTelefonos", fin - inicio);
		return telefonos;
	}
	
	private Telefono obtenerEntidadTelefono(Telefono borradorTelefono, Long idTelefono) {
		logger.trace("INICIO::CrearBorradorTelefono.obtenerEntidadTelefono");
		long inicio = System.currentTimeMillis();
		Telefono telefono = new Telefono();
		telefono.IdTelefono = idTelefono;
		telefono.IdInterno = borradorTelefono.IdInterno;
		telefono.IdEnrolamiento = borradorTelefono.IdEnrolamiento;
		telefono.IdPais = borradorTelefono.IdPais;
		telefono.IdTipoTelefono = borradorTelefono.IdTipoTelefono;
		telefono.Numero = borradorTelefono.Numero;
		telefono.Anexo = borradorTelefono.Anexo;
		telefono.CodigoProposito = borradorTelefono.CodigoProposito;
		telefono.Comentario = borradorTelefono.Comentario;
		telefono.Predeterminado = borradorTelefono.Predeterminado;
		telefono.IdTelefonoPersona = borradorTelefono.IdTelefonoPersona;
		long fin = System.currentTimeMillis();
		logger.info("FIN::CrearBorradorTelefono.obtenerEntidadTelefono", fin - inicio);
		return telefono;
	}
	
	private void insertarTelefonos(Connection con, List<Telefono> telefonos) throws Exception {
		logger.debug("INICIO::CrearBorradorTelefono.insertarTelefonos");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		sql.batch(con, SQL_INSERT_TELEFONO, telefonos, Telefono.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearBorradorTelefono.insertarTelefonos", fin - inicio);
	}
	
	private List<ResultadoAdministrarBorradorEntidad> obtenerIdsInsertados(List<Telefono> telefonos){
		logger.trace("INICIO::CrearBorradorTelefono.obtenerIdsInsertados");
		long inicio = System.currentTimeMillis();
		
		List<ResultadoAdministrarBorradorEntidad> idsInsertados = telefonos.parallelStream().map(telefono->{
			ResultadoAdministrarBorradorEntidad resultadoCreacionBorradorEntidad = new ResultadoAdministrarBorradorEntidad();
			resultadoCreacionBorradorEntidad.IdInterno = telefono.IdInterno;
			resultadoCreacionBorradorEntidad.IdEntidad = telefono.IdTelefono;
			return resultadoCreacionBorradorEntidad;
		}).collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorTelefono.obtenerIdsInsertados", fin - inicio);
		return idsInsertados;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_Telefonos\":[{\"IdInterno\":1,\"IdEnrolamiento\":456,\"IdTelefono\":0,\"IdPais\":44,\"IdTipoTelefono\":\"FIJO\",\"Numero\":\"+56 1 2345 6789\",\"Anexo\":\"ASKDJSAKL\",\"CodigoProposito\":\"COME\",\"Comentario\":\"ADASD\",\"Predeterminado\":\"N\"}],\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"S\",\"p_CodigoFormulario\":\"TELEFONO_FULL_JURIDICA\",\"p_IdTipoCanal\":\"OPT\"}\n";
			EntradaCrearBorradorTelefono Objin = gson.fromJson(json, EntradaCrearBorradorTelefono.class);
			
			CrearBorradorTelefono demo = new CrearBorradorTelefono();
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
