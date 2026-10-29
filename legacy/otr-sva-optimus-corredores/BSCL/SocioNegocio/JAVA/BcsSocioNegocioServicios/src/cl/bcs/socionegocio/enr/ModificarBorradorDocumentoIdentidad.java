package cl.bcs.socionegocio.enr;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.enr.ValidarBorradorDocumentoIdentidad.EntradaValidarBorradorDocumentoIdentidad;
import cl.bcs.socionegocio.enr.pojo.DocumentoIdentidad;
import cl.bcs.socionegocio.enr.pojo.EntradaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.ResultadoAdministrarBorradorEntidad;
import cl.bcs.socionegocio.enr.pojo.SalidaAdministrarBorrador;
import cl.bcs.socionegocio.enr.pojo.Validacion;
import cl.bcs.socionegocio.enr.ValidarExistenciaBorradorDocumentoIdentidad.EntradaValidarExistenciaBorradorDocumentoIdentidad;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ModificarBorradorDocumentoIdentidad implements JavaCall {

	static final Logger logger = LogManager.getLogger(ModificarBorradorDocumentoIdentidad.class);

	private String SQL_UPDATE_DOCUMENTO_IDENTIDAD = loadResourceToString("cl/bcs/socionegocio/enr/sql/ModificarBorradorDocumentoIdentidad_UpdateDocumentoIdentidad.sql");
	private String SQL_UPDATE_DOCUMENTO_IDENTIDAD_NO_PREDETERMINADOS = loadResourceToString("cl/bcs/socionegocio/enr/sql/ModificarBorradorDocumentoIdentidad_UpdateDocumentoIdentidadNoPredeterminados.sql");
	private String SQL_UPDATE_ENROLAMIENTO_DOCUMENTO_IDENTIDAD_PRINCIPAL = loadResourceToString("cl/bcs/socionegocio/enr/sql/ModificarBorradorDocumentoIdentidad_UpdateEnrolamientoDocumentoIdentidadPrincipal.sql");

	public static class EntradaModificarBorradorDocumentoIdentidad extends EntradaAdministrarBorrador {
		public List<DocumentoIdentidad> p_DocumentosIdentidades;

		public EntradaModificarBorradorDocumentoIdentidad() {
			this.p_DocumentosIdentidades = new ArrayList<DocumentoIdentidad>();
		}
	}
		
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaModificarBorradorDocumentoIdentidad jin = gson.fromJson(in.toString(), EntradaModificarBorradorDocumentoIdentidad.class);
		SalidaAdministrarBorrador out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaAdministrarBorrador logica(Connection con, EntradaModificarBorradorDocumentoIdentidad entrada) throws Exception {
		logger.info("INICIO::ModificarBorradorDocumentoIdentidad");
		long inicioServicio = System.currentTimeMillis();
		SalidaAdministrarBorrador out = new SalidaAdministrarBorrador();
		
		out = entrada.validarCamposObligatoriosEntrada(con, entrada.p_DocumentosIdentidades, DocumentoIdentidad.class, out);
		out = entrada.validarGrillasObligatoriasEntrada(con, entrada.p_DocumentosIdentidades, "DocumentosIdentidades", out);
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}

		List<Validacion> validacionesDocumentosIdentidades = validarDocumentosIdentidades(con, entrada.p_DocumentosIdentidades, entrada.p_IdEmpresa);
		out.agregarValidacionesSalida(validacionesDocumentosIdentidades);

		if (out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}

		ValidarExistenciaBorradorDocumentoIdentidad validarExistenciaBorradorDocumentoIdentidad = new ValidarExistenciaBorradorDocumentoIdentidad();
		EntradaValidarExistenciaBorradorDocumentoIdentidad entradaValidarExistenciaBorradorDocumentoIdentidad = new EntradaValidarExistenciaBorradorDocumentoIdentidad();
		entradaValidarExistenciaBorradorDocumentoIdentidad.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaValidarExistenciaBorradorDocumentoIdentidad.p_DocumentosIdentidades = entrada.p_DocumentosIdentidades;

		List<Validacion> validacionesExistenciaDocumentosIdentidades = validarExistenciaBorradorDocumentoIdentidad.logica(con, entradaValidarExistenciaBorradorDocumentoIdentidad).validaciones;
		out.agregarValidacionesSalida(validacionesExistenciaDocumentosIdentidades);

		if (out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}

		actualizarDocumentosIdentidades(con, entrada.p_DocumentosIdentidades);
		predeterminarDocumentosIdentidades(con, entrada.p_DocumentosIdentidades);
		
		out.P_OUT_REFID = obtenerIdsActualizados(entrada.p_DocumentosIdentidades);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarBorradorDocumentoIdentidad", finServicio - inicioServicio);
		return out;
	}
	
	private List<Validacion> validarDocumentosIdentidades(Connection con, List<DocumentoIdentidad> borradoresDocumentoIdentidades, Long idEmpresa) throws Exception {
		logger.trace("INICIO::ModificarBorradorDocumentoIdentidad.validarDocumentoIdentidad");
		long inicio = System.currentTimeMillis();
		
		ValidarBorradorDocumentoIdentidad validarBorradorDocumentoIdentidad = new ValidarBorradorDocumentoIdentidad();
		EntradaValidarBorradorDocumentoIdentidad entradaValidarBorradorDocumentoIdentidad = new EntradaValidarBorradorDocumentoIdentidad();
		entradaValidarBorradorDocumentoIdentidad.IdEmpresa = idEmpresa;
		entradaValidarBorradorDocumentoIdentidad.p_DocumentosIdentidad = borradoresDocumentoIdentidades;
		
		List<Validacion> validaciones = validarBorradorDocumentoIdentidad.logica(con, entradaValidarBorradorDocumentoIdentidad).Validaciones;
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarBorradorDocumentoIdentidad.validarDocumentosIdentidades", fin - inicio);
		return validaciones;
	}
	
	private void predeterminarDocumentosIdentidades(Connection con, List<DocumentoIdentidad> documentosIdentidades) throws Exception {
		logger.debug("INICIO::ModificarBorradorTelefono.predeterminarTelefonos");
		long inicio = System.currentTimeMillis();
		
		List<DocumentoIdentidad> documentosIdentidadesPredeterminados = documentosIdentidades.parallelStream().filter(documentoIdentidad->"S".equals(documentoIdentidad.Predeterminado)).collect(Collectors.toList());
		
		SQL sql = new SQL();
		sql.batch(con, SQL_UPDATE_DOCUMENTO_IDENTIDAD_NO_PREDETERMINADOS, documentosIdentidadesPredeterminados, DocumentoIdentidad.class);
		sql.batch(con, SQL_UPDATE_ENROLAMIENTO_DOCUMENTO_IDENTIDAD_PRINCIPAL, documentosIdentidadesPredeterminados, DocumentoIdentidad.class);
		long fin = System.currentTimeMillis();

		logger.debug("FIN::ModificarBorradorTelefono.predeterminarTelefonos", fin - inicio);
	}
	
	private void actualizarDocumentosIdentidades(Connection con, List<DocumentoIdentidad> documentosIdentidades) throws Exception {
		logger.debug("INICIO::ModificarBorradorDocumentoIdentidad.actualizarDocumentosIdentidades");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		sql.batch(con, SQL_UPDATE_DOCUMENTO_IDENTIDAD, documentosIdentidades, DocumentoIdentidad.class);
		long fin = System.currentTimeMillis();

		logger.debug("FIN::ModificarBorradorDocumentoIdentidad.actualizarDocumentosIdentidades", fin - inicio);
	}
	
	private List<ResultadoAdministrarBorradorEntidad> obtenerIdsActualizados(List<DocumentoIdentidad> documentosIdentidades){
		logger.trace("INICIO::ModificarBorradorDocumentoIdentidad.obtenerIdsActualizados");
		long inicio = System.currentTimeMillis();
		
		List<ResultadoAdministrarBorradorEntidad> idsInsertados = documentosIdentidades.parallelStream().map(documentoIdentidad->{
			ResultadoAdministrarBorradorEntidad resultadoCreacionBorradorEntidad = new ResultadoAdministrarBorradorEntidad();
			resultadoCreacionBorradorEntidad.IdInterno = documentoIdentidad.IdInterno;
			resultadoCreacionBorradorEntidad.IdEntidad = documentoIdentidad.IdDocumentoIdentidad;
			return resultadoCreacionBorradorEntidad;
		}).collect(Collectors.toList());
		long fin = System.currentTimeMillis();
		
		logger.trace("FIN::ModificarBorradorDocumentoIdentidad.obtenerIdsActualizados", fin - inicio);
		return idsInsertados;
	}
	
	public static void main(String[] args) {
		System.out.println("== INICIO TEST ==");
		
		String jsonEntrada = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"S\",\"p_CodigoFormulario\":\"\",\"p_IdTipoCanal\":\"OPT\",\"p_DocumentosIdentidades\":[{\"IdDocumentoIdentidad\":44,\"IdEnrolamiento\":3,\"IdTipoDocumentoIdentidad\":\"P\",\"DocumentoIdentidad\":\"1111111111\",\"NumeroSerie\":\"11111111111111111111\",\"FechaEmision\":\"2020-08-21\",\"FechaVencimiento\":\"0001-01-01\"}]}";
		
		try (Connection conn = PlataformaUtils.getDESAConnection()) {
            Gson gson = Utils.getGsonBuilder();
            EntradaModificarBorradorDocumentoIdentidad entrada = gson.fromJson(jsonEntrada, EntradaModificarBorradorDocumentoIdentidad.class);

            try {
                ModificarBorradorDocumentoIdentidad modificarBorradorDocumentoIdentidad = new ModificarBorradorDocumentoIdentidad();
                SalidaAdministrarBorrador salida = modificarBorradorDocumentoIdentidad.logica(conn, entrada);

                System.out.println(salida);
            } catch (Exception innerException) {
                innerException.printStackTrace();
                conn.rollback();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
		
		System.out.println("== FIN TEST ==");
	}
}
