package cl.bcs.socionegocio.enr;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.enr.pojo.TipoRelacion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.socionegocio.enr.pojo.EntradaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.RelacionPersona;
import cl.bcs.socionegocio.enr.pojo.ResultadoAdministrarBorradorEntidad;
import cl.bcs.socionegocio.enr.pojo.SalidaAdministrarBorrador;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearBorradorRelacionPersona implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(CrearBorradorRelacionPersona.class);
	
	private String SQL_INSERT_RELACION_PERSONA = loadResourceToString("cl/bcs/socionegocio/enr/sql/CrearBorradorRelacionPersona_InsertRelacionPersona.sql");
	
	public static class EntradaCrearBorradorRelacionPersona extends EntradaAdministrarBorrador{
		public List<RelacionPersona> p_RelacionesPersona;
		
		public EntradaCrearBorradorRelacionPersona(){
			this.p_RelacionesPersona = new ArrayList<RelacionPersona>();
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaCrearBorradorRelacionPersona jin = gson.fromJson(in.toString(), EntradaCrearBorradorRelacionPersona.class);
		SalidaAdministrarBorrador out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaAdministrarBorrador logica(Connection con, EntradaCrearBorradorRelacionPersona entrada) throws Exception {
		logger.info("INICIO::CrearBorradorRelacionPersona");
		long inicioServicio = System.currentTimeMillis();
		SalidaAdministrarBorrador out = new SalidaAdministrarBorrador();

		prepararRelacionesEntrantes(con, entrada);
		
		out = entrada.validarCamposObligatoriosEntrada(con, entrada.p_RelacionesPersona, RelacionPersona.class, out);
		out = entrada.validarGrillasObligatoriasEntrada(con, entrada.p_RelacionesPersona, "RelacionesPersona", out);
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		
		List<RelacionPersona> relacionesPersona = obtenerRelacionesPersona(con, entrada.p_RelacionesPersona);
		
		insertarRelacionPersona(con, relacionesPersona);
		
		out.P_OUT_REFID = obtenerIdsInsertados(relacionesPersona);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearBorradorRelacionPersona", finServicio - inicioServicio);
		return out;
		
	}

	private void prepararRelacionesEntrantes(Connection con, EntradaCrearBorradorRelacionPersona entrada) throws SQLException {
		logger.trace("INICIO::CrearBorradorRelacionPersona.prepararRelacionesEntrantes");
		long inicio = System.currentTimeMillis();

		List<String> tipoRelacionIds = entrada.p_RelacionesPersona.stream().filter(u -> u.IdTipoRelacion != null).map(u -> u.IdTipoRelacion).collect(Collectors.toList());
		Map<String, TipoRelacion> tipoRelacionPorIdTipoRelacion = EnrolamientoUtils.obtenerTipoRelacionPorIdTipoRelacion(con, tipoRelacionIds);

		for (RelacionPersona relacionPersona : entrada.p_RelacionesPersona) {
			relacionPersona.EstaInsertada = "N";

			TipoRelacion tipoRelacion = tipoRelacionPorIdTipoRelacion.getOrDefault(relacionPersona.IdTipoRelacion, new TipoRelacion());
			relacionPersona.TipoRelacionUsaCondicionesRelacion = tipoRelacion.UsaCondicionesRelacion;
			relacionPersona.TipoRelacionEsIndefinida = tipoRelacion.EsIndefinida;
			relacionPersona.TipoRelacionUsaFinalidadRelacion = tipoRelacion.UsaFinalidadRelacion;
			relacionPersona.TipoRelacionUsaGradoVinculacionParental = tipoRelacion.UsaGradoVinculacionParental;

			boolean esAGYRLE = "AGYRLE".equals(tipoRelacion.IdTipoRelacion);
			boolean esPERDEC = "PERDEC".equals(tipoRelacion.IdTipoRelacion);
			boolean noUsaPorcentajeParticipacion = "N".equals(tipoRelacion.UsaPorcentajeParticipacion);
			relacionPersona.TipoRelacionUsaPorcentajeParticipacion = esAGYRLE || esPERDEC || noUsaPorcentajeParticipacion ? "N" : "S";
		}

		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorRelacionPersona.prepararRelacionesEntrantes {}", fin - inicio);
	}

	private List<RelacionPersona> obtenerRelacionesPersona(Connection con, List<RelacionPersona> borradoresRelacionesPersona) throws Exception{
		logger.trace("INICIO::CrearBorradorRelacionPersona.obtenerRelacionesPersona");
		long inicio = System.currentTimeMillis();
		
		List<Long> secuencias = Utils.getSecuencia(con, "BSCL.SocioNegocio.BD.Secuencias::BorradorRelacionPersona", Long.valueOf(borradoresRelacionesPersona.size()));
		
		AtomicInteger index = new AtomicInteger();
		List<RelacionPersona> relacionesPersona = borradoresRelacionesPersona.parallelStream().map(borradorRelacionPersona->{
			Long idRelacionPersona = secuencias.get(index.getAndIncrement());
			RelacionPersona relacionPersona = obtenerEntidadRelacionPersona(borradorRelacionPersona, idRelacionPersona);
			return relacionPersona;
		}).collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorRelacionPersona.obtenerRelacionesPersona", fin - inicio);
		return relacionesPersona;
	}
	
	private RelacionPersona obtenerEntidadRelacionPersona(RelacionPersona borradorRelacionPersona, Long idRelacionPersona) {
		logger.trace("INICIO::CrearBorradorRelacionPersona.obtenerEntidadRelacionPersona");
		long inicio = System.currentTimeMillis();
		
		RelacionPersona relacionPersona = new RelacionPersona();
		relacionPersona.IdRelacionPersona = idRelacionPersona;
		relacionPersona.IdInterno = borradorRelacionPersona.IdInterno;
		relacionPersona.IdEnrolamientoOrigen = borradorRelacionPersona.IdEnrolamientoOrigen;
		relacionPersona.IdTipoRelacion = borradorRelacionPersona.IdTipoRelacion;
		relacionPersona.IdPersonaDestino = borradorRelacionPersona.IdPersonaDestino;
		relacionPersona.FechaInicioRelacion = borradorRelacionPersona.FechaInicioRelacion;
		relacionPersona.FechaFinRelacion = borradorRelacionPersona.FechaFinRelacion;
		relacionPersona.PorcentajeParticipacion = borradorRelacionPersona.PorcentajeParticipacion;
		relacionPersona.RetiraDocumentos = borradorRelacionPersona.RetiraDocumentos;
		relacionPersona.FinalidadRelacion = borradorRelacionPersona.FinalidadRelacion;
		relacionPersona.CondicionesRelacion = borradorRelacionPersona.CondicionesRelacion;
		relacionPersona.IdGradoVinculacionParental = borradorRelacionPersona.IdGradoVinculacionParental;
		relacionPersona.IdCiudad = borradorRelacionPersona.IdCiudad;
		relacionPersona.Calle = borradorRelacionPersona.Calle;
		relacionPersona.Numero = borradorRelacionPersona.Numero;
		relacionPersona.Email = borradorRelacionPersona.Email;
		relacionPersona.Telefono = borradorRelacionPersona.Telefono;
		relacionPersona.Complemento = borradorRelacionPersona.Complemento;
		relacionPersona.Ciudad = borradorRelacionPersona.Ciudad;
		relacionPersona.IdRelacionPersonaPersona = borradorRelacionPersona.IdRelacionPersonaPersona;
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearBorradorRelacionPersona.obtenerEntidadRelacionPersona", fin - inicio);
		return relacionPersona;
	}
	
	private void insertarRelacionPersona(Connection con, List<RelacionPersona> relacionesPersona) throws Exception {
		logger.debug("INICIO::CrearBorradorRelacionPersona.insertarRelacionPersona");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		sql.batch(con, SQL_INSERT_RELACION_PERSONA, relacionesPersona, RelacionPersona.class);
		long fin = System.currentTimeMillis();

		logger.debug("FIN::CrearBorradorRelacionPersona.insertarRelacionPersona", fin - inicio);
	}
	
	private List<ResultadoAdministrarBorradorEntidad> obtenerIdsInsertados(List<RelacionPersona> relacionesPersonas){
		logger.trace("INICIO::CrearBorradorRelacionPersona.obtenerIdsInsertados");
		long inicio = System.currentTimeMillis();
		
		List<ResultadoAdministrarBorradorEntidad> idsInsertados = relacionesPersonas.parallelStream().map(relacionPersona->{
			ResultadoAdministrarBorradorEntidad resultadoCreacionBorradorEntidad = new ResultadoAdministrarBorradorEntidad();
			resultadoCreacionBorradorEntidad.IdInterno = relacionPersona.IdInterno;
			resultadoCreacionBorradorEntidad.IdEntidad = relacionPersona.IdRelacionPersona;
			return resultadoCreacionBorradorEntidad;
		}).collect(Collectors.toList());
		long fin = System.currentTimeMillis();
		
		logger.trace("FIN::CrearBorradorRelacionPersona.obtenerIdsInsertados", fin - inicio);
		return idsInsertados;
	}

	public static void main(String[] args) {
		System.out.println("== INICIO TEST ==");

		try (Connection conn = PlataformaUtils.getDESAConnection()) {
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"S\",\"p_CodigoFormulario\":\"RELACIONES_FULL_JURIDICA\",\"p_IdTipoCanal\":\"OPT\",\"p_RelacionesPersona\":[{\"IdInterno\":1,\"IdRelacionPersona\":273,\"IdEnrolamientoOrigen\":456,\"IdTipoRelacion\":\"\",\"IdPersonaDestino\":0,\"FechaInicioRelacion\":\"0001-01-01 00:00:00\",\"FechaFinRelacion\":\"0001-01-01 00:00:00\",\"PorcentajeParticipacion\":0,\"RetiraDocumentos\":\"N\",\"FinalidadRelacion\":\"\",\"CondicionesRelacion\":\"\",\"IdGradoVinculacionParental\":0,\"Ciudad\":\"ARICA\",\"Calle\":\"ASD\",\"Numero\":\"123\",\"Telefono\":\"+56 1 2345 6789\",\"Email\":\"ASD@ASD.COM\",\"Complemento\":\"ASDASD\"}]}";
			EntradaCrearBorradorRelacionPersona entrada = Utils.getGsonBuilder().fromJson(json, EntradaCrearBorradorRelacionPersona.class);
			System.out.println("ENTRADA");
			System.out.println(entrada);

			try {
				SalidaAdministrarBorrador salida = new CrearBorradorRelacionPersona().logica(conn, entrada);
				System.out.println("SALIDA");
				System.out.println(salida);
				if (salida.P_OUT_ERRORESCONTADOR > 0) {
					conn.rollback();
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				conn.rollback();
			}
		} catch (SQLException throwables) {
			throwables.printStackTrace();
		}
	}
}
