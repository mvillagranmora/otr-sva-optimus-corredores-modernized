package cl.bcs.socionegocio.enr;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.ctt.ImprimirContrato;
import cl.bcs.socionegocio.ctt.ImprimirContrato.ImprimirContrato_Entrada;
import cl.bcs.socionegocio.ctt.ImprimirContrato.ImprimirContrato_Salida;

import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class GenerarDocumentosFirmados  implements JavaCall {
	AtomicBoolean running = new AtomicBoolean(false);
	public final Log log = new Log("SOC.GenerarDocumentosFirmados");

	public static final Logger logger = LogManager.getLogger(GenerarDocumentosFirmados.class);
	
	private final String SQL_SELECT_CLIENTES = loadResourceToString("cl/bcs/socionegocio/enr/sql/GenerarDocumentosFirmados_SelectClientes.sql");
	public final String SQL_SELECT_VALIDACION = loadResourceToString("cl/bcs/socionegocio/enr/sql/GenerarDocumentosFirmados_SelectValidacion.sql");
	private final String SQL_INSERT_INDICADOR = loadResourceToString("cl/bcs/socionegocio/enr/sql/GenerarDocumentosFirmados_InsertIndicadorContrato.sql");
	private final String SQL_SELECT_TIPO_DOCUMENTOS_FIRMA_AUTOMATICA = loadResourceToString("cl/bcs/socionegocio/enr/sql/GenerarDocumentosFirmados_SelectDocumentosConFirmaAutomatica.sql");
	private final String SQL_INSERT_SOLICITUD_FIRMA = loadResourceToString("cl/bcs/socionegocio/enr/sql/GenerarDocumentosFirmados_InsertSolicitudFirma.sql");
	private final String SQL_INSERT_FIRMA = loadResourceToString("cl/bcs/socionegocio/enr/sql/GenerarDocumentosFirmados_InsertFirma.sql");
	private final String SQL_TIPO_DOCUMENTO_INDICADOR_SELECT = loadResourceToString("cl/bcs/socionegocio/enr/sql/GenerarDocumentosFirmados_SelectTipoDocumentoIndicador.sql");
	public String SQL_DOCUMENTO_SELECT =  loadResourceToString("cl/bcs/socionegocio/enr/sql/GenerarDocumentosFirmados_SelectDocumentos.sql");
	public String SQL_DOCUMENTO_INSERT = loadResourceToString("cl/bcs/socionegocio/enr/sql/GenerarDocumentosFirmados_InsertDocumentos.sql");
	public String SQL_ANEXO_INSERT = loadResourceToString("cl/bcs/socionegocio/enr/sql/GenerarDocumentosFirmados_InsertAnexo.sql");
	public String SQL_ANEXO_UPDATE_ESTADO =  loadResourceToString("cl/bcs/socionegocio/enr/sql/GenerarDocumentosFirmados_UpdateAnexo.sql");
	public String SQL_SELECT_TIPO_DOCUMENTO = loadResourceToString("cl/bcs/socionegocio/enr/sql/GenerarDocumentosFirmados_SelectTipoDocumento.sql");
	
	public String SQL_ULTIMO_ANEXO_UPDATE =  loadResourceToString("cl/bcs/socionegocio/enr/sql/GenerarDocumentosFirmados_UpdateUltimoAnexo.sql");
	public String SQL_ARCHIVO_UPDATE =  loadResourceToString("cl/bcs/socionegocio/enr/sql/GenerarDocumentosFirmados_UpdateArchivo.sql");

	

	public static class ActualizarPrograma{
		public String RutaSalida;
		public Long IdPrograma;
		
		public ActualizarPrograma(String RutaSalida,Long IdPrograma){
			this.RutaSalida = RutaSalida;
			this.IdPrograma = IdPrograma;
		}
	}
	
	public static class EntidadEntradaFirmada {
		public Long v_IdEmpresa;
		public Long v_IdCliente;
		public String v_IdTipoCanal;
		public Long v_IdUsuario;
	}

	public static class EntidadCliente {
		public Long IdEmpresa;
		public Long IdCliente;
		public Long IdPersona;
		public Long IdPersonaEjecutivo;
		public String IdTipoDocumentoIdentidad;
		public String DocumentoIdentidad;
		public String IdTipoPersona;
	}

	public static class EntidadListaDocumento {
		public Long IdEmpresa;
		public String IdTipoDocumento;
		public String NombreDocumento;
		public String CodPlantilla;
		public Integer NumeroFirmantesEmpresa;
		public Integer NumeroFirmantesCliente;
		public String RequiereFirmaCliente;
		public String CodigoTipoIndicador;

	}

	public static class EntidadTipoDocumento {
		public String IdTipoDocumento;
		public Integer NumeroFirmasMinimas;
		public Integer NumeroFirmasMaximas;

	}

	public static class EntidadDocumento {
		public Long IdDocumento;
		public String IdTipoDocumento;
		public Long IdEmpresa;
		public Long IdCliente;
		public Long IdUltimoAnexo;
	}

	public static class EntidadAnexos {
		public Long IdAnexo;
		public Long IdDocumento;
		public String IdEstado;
		public Long NumeroFirmantesEmpresa;
		public Long NumeroFirmantesCliente;
		public Integer NumeroFirmasMinimas;
		public Integer NumeroFirmasMaximas;
		public String NombreArchivoAdjunto;
		public String FormatoArchivoAdjunto;
		public String IdTipoCanalIngreso;
	}
	
	public static class EntidadAnexo {
		public Long IdAnexo;
		public Long IdDocumento;
		public String IdEstado;
	}
	
	
	public static class EntidadIndicador {
		public Long IdEntidad;
		public String Codigo;
		public Long IdTipoIndicador;
		public String CodigoTipoIndicador;
		public String IdTipoPersona;
		public String IdTipoDocumento;
	}

	public static class EntidadArchivo{
		public InputStream FilePart;
		public String Estado;
		public String NombreArchivoAdjunto;
		public String FormatoArchivoAdjunto;
		public Long IdDocumento;
		public Long IdAnexo;
	}

	public static class EntidadValidaExistenDatos {
		public Long IdEmpresa;
		public Long IdCliente;
		public Long IdUltimoAnexo;
		public Long IdDocumento;
		public String IdTipoDocumento;

	}
	
	public static class EntidadSolicitudFirma{
		public Long IdSolicitudFirmaAnexo;
		public Long IdPersonaSolicitante;
		public Long IdPersonaFirmante;
		public String IdTipoCanalIngreso;
	}
	
	public static class EntidadFirma{
		public Long IdFirmaAnexo;
		public Long IdAnexo;
		public Long IdSolicitudFirmaAnexo;
		public String IdEstado;
		public String IdTipoCanalIngreso;
		public Long IdPersonaAgente;
		public Timestamp FechaFirma;
		public String EsAutorizado;
		public Long IdUsuario;
	}
	
	public static class EntidadDocumentoPlantilla{
		public String IdTipoDocumento;
		public Integer NumeroFirmantesCliente;
		public Integer NumeroFirmantesEmpresa;
		public String CodigoPlantilla;
	}
	
	public boolean logica(Connection conn, EntidadEntradaFirmada entrada) throws Exception {
		running.set(false);

		System.out.println("INICIO::GenerarDocumentosFirmados");
		long inicio = System.currentTimeMillis();
		
		//Creamos arrays para las entradas grupales de querys
		SQL sql = new SQL();
		List<String> IdTipoDocumentos = new ArrayList<String>();
		List<EntidadDocumentoPlantilla> AsocDocPlantilla= new ArrayList<EntidadDocumentoPlantilla>();
		List<Long> IdAnexos = new ArrayList<Long>();
		List<String> CodigoPlantilla = new ArrayList<String>();

		EntidadCliente Cliente = BuscaCliente(conn, entrada);
		List<EntidadListaDocumento> ListaDocumentos = BuscaDocumentos(conn, Cliente);
		
		//Agrupamos los documentos con firma automatica y asociacimos el tipo de documento a su plantilla
		for (EntidadListaDocumento acDoc :ListaDocumentos) {	
			if(acDoc.IdTipoDocumento != null && acDoc.CodPlantilla != null && !acDoc.CodPlantilla.toUpperCase().equalsIgnoreCase("FALSE")){
				EntidadDocumentoPlantilla docPlantilla = new EntidadDocumentoPlantilla();
				docPlantilla.IdTipoDocumento = acDoc.IdTipoDocumento;
				docPlantilla.NumeroFirmantesCliente = acDoc.NumeroFirmantesCliente;
				docPlantilla.NumeroFirmantesEmpresa = acDoc.NumeroFirmantesEmpresa;
				docPlantilla.CodigoPlantilla = acDoc.CodPlantilla;
				AsocDocPlantilla.add(docPlantilla);
				
				IdTipoDocumentos.add(acDoc.IdTipoDocumento);
				CodigoPlantilla.add(acDoc.CodPlantilla);
			}		
		}
		
		//Consultamos si el cliente ya poseia documentos en el sistema
		HashMap<String,Object> selectDocumentos_IN = new HashMap<String,Object>();
		selectDocumentos_IN.put("IdCliente", Cliente.IdCliente);
		selectDocumentos_IN.put("IdTipoDocumentos", IdTipoDocumentos);
		selectDocumentos_IN.put("IdTipoPersona", Cliente.IdTipoPersona);
		List<EntidadDocumento> listaDocsActuales = sql.query(conn, SQL_DOCUMENTO_SELECT, selectDocumentos_IN, EntidadDocumento.class);	
		
		//Si previamente ya se generaron los documentos solo los firma y activa indicadores
		if(listaDocsActuales.size() > 0) {
			List<EntidadAnexo> nuevosAnexos = new ArrayList<EntidadAnexo>();
			for(EntidadDocumento acDoc : listaDocsActuales) {
				EntidadAnexo anexoUpdate_IN = new EntidadAnexo();
				anexoUpdate_IN.IdEstado = "ACT";
				anexoUpdate_IN.IdAnexo = acDoc.IdUltimoAnexo;
				anexoUpdate_IN.IdDocumento = acDoc.IdDocumento;
				nuevosAnexos.add(anexoUpdate_IN);
				IdAnexos.add(acDoc.IdUltimoAnexo);
			}
			
			sql.batch(conn, SQL_ANEXO_UPDATE_ESTADO, nuevosAnexos, EntidadAnexo.class);
			
			//Rellenar datos de solicitudes y firmas
			List<EntidadFirma> FirmasAnexos = new ArrayList<EntidadFirma>();
			List<EntidadSolicitudFirma> SolicitudesFirmas = new ArrayList<EntidadSolicitudFirma>();
			for(Long acUltimoAnexo : IdAnexos) {
				EntidadSolicitudFirma solFirma = new EntidadSolicitudFirma();
				solFirma.IdSolicitudFirmaAnexo = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::SolicitudFirmaAnexo", (long) 1).get(0);;
				solFirma.IdPersonaFirmante = Cliente.IdPersona;
				solFirma.IdPersonaSolicitante = Cliente.IdPersona;
				solFirma.IdTipoCanalIngreso = entrada.v_IdTipoCanal;
				SolicitudesFirmas.add(solFirma);
				
				EntidadFirma firmaAnexo = new EntidadFirma();
				firmaAnexo.IdFirmaAnexo = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::FirmaAnexo", (long) 1).get(0);;
				firmaAnexo.FechaFirma = FechaDB.ObtenerFechaHoraSistemaTs(conn, entrada.v_IdEmpresa);
				firmaAnexo.IdAnexo = acUltimoAnexo;
				firmaAnexo.IdEstado = "ACT";
				firmaAnexo.IdPersonaAgente = Cliente.IdPersonaEjecutivo;
				firmaAnexo.IdSolicitudFirmaAnexo = solFirma.IdSolicitudFirmaAnexo;
				firmaAnexo.EsAutorizado = "S";
				firmaAnexo.IdTipoCanalIngreso = entrada.v_IdTipoCanal;
				firmaAnexo.IdUsuario = entrada.v_IdUsuario;
				FirmasAnexos.add(firmaAnexo);
			}
			
			//Insertar solicitudes y firmas
			sql.batch(conn, SQL_INSERT_SOLICITUD_FIRMA, SolicitudesFirmas, EntidadSolicitudFirma.class);
			
			sql.batch(conn, SQL_INSERT_FIRMA, FirmasAnexos, EntidadFirma.class);
			
		}else{
			for(EntidadDocumentoPlantilla acDocPlantilla : AsocDocPlantilla) {

				InputStream ArchivoBuffer = null;
				try {
					ArchivoBuffer = ImprimeContratosPDF(conn, Cliente.IdEmpresa.toString(),acDocPlantilla.CodigoPlantilla, "PLA", Cliente.IdTipoDocumentoIdentidad,Cliente.DocumentoIdentidad);

					Long IdAnexo = SubeDocumentoDB(conn, ArchivoBuffer, acDocPlantilla.IdTipoDocumento, entrada.v_IdEmpresa,entrada.v_IdCliente, String.valueOf(acDocPlantilla.NumeroFirmantesEmpresa),String.valueOf(acDocPlantilla.NumeroFirmantesCliente), entrada.v_IdTipoCanal);

					//Rellenar datos de solicitudes y firmas
					EntidadSolicitudFirma solFirma = new EntidadSolicitudFirma();
					solFirma.IdSolicitudFirmaAnexo = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::SolicitudFirmaAnexo", (long) 1).get(0);;
					solFirma.IdPersonaFirmante = Cliente.IdPersona;
					solFirma.IdPersonaSolicitante = Cliente.IdPersona;
					solFirma.IdTipoCanalIngreso = entrada.v_IdTipoCanal;
					sql.batch(conn, SQL_INSERT_SOLICITUD_FIRMA, Arrays.asList(solFirma), EntidadSolicitudFirma.class);
								
					EntidadFirma firmaAnexo = new EntidadFirma();
					firmaAnexo.IdFirmaAnexo = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::FirmaAnexo", (long) 1).get(0);;
					firmaAnexo.FechaFirma = FechaDB.ObtenerFechaHoraSistemaTs(conn, entrada.v_IdEmpresa);
					firmaAnexo.IdAnexo = IdAnexo;
					firmaAnexo.IdEstado = "ACT";
					firmaAnexo.IdPersonaAgente = Cliente.IdPersonaEjecutivo;
					firmaAnexo.IdSolicitudFirmaAnexo = solFirma.IdSolicitudFirmaAnexo;
					firmaAnexo.EsAutorizado = "S";
					firmaAnexo.IdTipoCanalIngreso = entrada.v_IdTipoCanal;
					firmaAnexo.IdUsuario = entrada.v_IdUsuario;
					sql.batch(conn, SQL_INSERT_FIRMA, Arrays.asList(firmaAnexo), EntidadFirma.class);
					
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println(e);
					return false;
				}

			}
			
		}
		//Seleccionar Indicador

		HashMap<String,Object> tipoDocIndicador_entrada = new HashMap<String,Object>();
		tipoDocIndicador_entrada.put("IdEmpresa", entrada.v_IdEmpresa);
		tipoDocIndicador_entrada.put("IdTipoDocumentos", IdTipoDocumentos);
		
		List<EntidadIndicador> ListaDocIndicador = sql.query(conn, SQL_TIPO_DOCUMENTO_INDICADOR_SELECT, tipoDocIndicador_entrada, EntidadIndicador.class);
		
		List<String> indicadores = new ArrayList<String>();
		
		for(EntidadIndicador acIndicador : ListaDocIndicador){
			indicadores.add(acIndicador.CodigoTipoIndicador);
		}
		crearIndicador(conn,entrada.v_IdCliente,indicadores);
				
		System.out.println("== END ==");

		long fin = System.currentTimeMillis();
		System.out.println("FIN::GenerarDocumentosAutomaticos" + (fin - inicio));

		return true;

	}

	public EntidadCliente BuscaCliente(Connection conn, EntidadEntradaFirmada param) throws Exception {
		HashMap<String, Object> params = new HashMap<>(2);
		params.put("IdEmpresa", param.v_IdEmpresa);
		params.put("IdCliente", param.v_IdCliente);
		SQL sql = new SQL();
		List<EntidadCliente> ClienteX = sql.query(conn, SQL_SELECT_CLIENTES, params, EntidadCliente.class);
		return ClienteX.get(0);
	}

	public List<EntidadListaDocumento> BuscaDocumentos(Connection conn, EntidadCliente param) throws Exception {
		HashMap<String, Object> params = new HashMap<>(1);
		params.put("IdEmpresa", param.IdEmpresa);
		params.put("IdTipoPersona", param.IdTipoPersona);
		SQL sql = new SQL();
		List<EntidadListaDocumento> ListaDocumentoX = sql.query(conn, SQL_SELECT_TIPO_DOCUMENTOS_FIRMA_AUTOMATICA, params,EntidadListaDocumento.class);
		return ListaDocumentoX;
	}

	private void crearIndicador(Connection conn, Long v_IdCliente, List<String> ListaDocIndicador) {
		SQL sql = new SQL();
		
		try {
			List<EntidadIndicador> nuevosIndicadores = new ArrayList<>();
			for(String ind: ListaDocIndicador) {
				EntidadIndicador nuevoIndicador = new EntidadIndicador();
				nuevoIndicador.IdEntidad = v_IdCliente;
				nuevoIndicador.Codigo = ind;
				nuevosIndicadores.add(nuevoIndicador);
			}
			sql.batch(conn, SQL_INSERT_INDICADOR, nuevosIndicadores, EntidadIndicador.class);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	
		
	}

	public InputStream ImprimeContratosPDF(Connection conn, String IdEmpresa, String IdPlantilla, String IdModulo,String TipoDocumentoIdentidad, String DocumentoIdentidad) {
		InputStream filePart = null;
		try {

			ImprimirContrato_Entrada entrada = new ImprimirContrato_Entrada();
			
			entrada.p_IdEmpresa = Long.valueOf(IdEmpresa) ;	
			entrada.p_CodigoContrato = IdPlantilla;
			entrada.p_IdModulo = IdModulo ;
			entrada.p_TipoDocumentoIdentidad =  TipoDocumentoIdentidad;
			entrada.p_DocumentoIdentidad =  DocumentoIdentidad ;
			

			ImprimirContrato_Salida salida = new ImprimirContrato().logica(conn, entrada);

			String b64 = salida.p_out_ContratoB64;

			salida.p_out_ContratoB64 = "ContenidoEnBase64";

			filePart = new ByteArrayInputStream(Base64.decodeBase64(b64));

		} catch (Exception e) {

			e.printStackTrace();
		} finally {

		}

		return filePart;
	}

	public Long SubeDocumentoDB(Connection conn, InputStream filePart, String IdTipoDocumento, Long IdEmpresa,Long IdCliente, String NumeroFirmantesEmpresa, String NumeroFirmantesCliente, String IdTipoCanalIngreso) throws Exception {
		
		String IdDocumento = "0";
		String IdAnexo ="";
		String FormatoArchivoAdjunto = "PDF";
		String NombreArchivoAdjunto = IdTipoDocumento + IdAnexo + ".pdf";
		Long IdAnexoEntrada = 0l;

		SQL sql = new SQL();
		
		HashMap<String, Object> params = new HashMap<>();
		params.put("IdTipoDocumento", IdTipoDocumento);
		EntidadTipoDocumento tipoDocumento = sql .query(conn, SQL_SELECT_TIPO_DOCUMENTO, params,EntidadTipoDocumento.class).stream().findFirst() .orElse(new EntidadTipoDocumento());
		 

		HashMap<String, Object> paramsValida = new HashMap<>();
		paramsValida.put("IdEmpresa", IdEmpresa);
		paramsValida.put("IdCliente", IdCliente);
		paramsValida.put("IdTipoDocumento", IdTipoDocumento);
		
		EntidadValidaExistenDatos ValidaExiste = sql.query(conn, SQL_SELECT_VALIDACION, paramsValida,EntidadValidaExistenDatos.class).stream().findFirst().orElse(new EntidadValidaExistenDatos());

		IdDocumento = (ValidaExiste.IdDocumento != null)?ValidaExiste.IdDocumento.toString():"0" ;
		
		EntidadDocumento entidadDocumento = new EntidadDocumento();
		
		if (IdDocumento != null && IdDocumento.equals("0")) {
			
			Long idDocumento = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::Documento", (long) 1).get(0);

			try {

				entidadDocumento.IdDocumento = idDocumento;
				entidadDocumento.IdTipoDocumento = IdTipoDocumento;
				entidadDocumento.IdEmpresa = IdEmpresa;
				entidadDocumento.IdCliente = IdCliente;
				entidadDocumento.IdUltimoAnexo = 0L;
				sql.batch(conn, SQL_DOCUMENTO_INSERT, Arrays.asList(entidadDocumento), EntidadDocumento.class);

				IdDocumento = idDocumento.toString();

			} catch (Exception e) {
				conn.rollback();
				throw new Exception(e);
			}

		} else {
			
			IdAnexo = ValidaExiste.IdUltimoAnexo.toString();
			IdAnexoEntrada =Long.parseLong(IdAnexo);
		}
		// crear Anexo
		EntidadAnexos entidadAnexo = new EntidadAnexos();
		Long idAnexo = 0L;
		if (IdAnexo != null) {
			idAnexo = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::Anexo", (long) 1).get(0);
			try {
				entidadAnexo.IdAnexo =idAnexo;
				entidadAnexo.IdDocumento = Long.valueOf(IdDocumento);
				entidadAnexo.IdEstado = "ACT";
				entidadAnexo.NumeroFirmantesEmpresa = Long.parseLong(NumeroFirmantesEmpresa);
				entidadAnexo.NumeroFirmantesCliente = Long.parseLong(NumeroFirmantesCliente);
				entidadAnexo.NumeroFirmasMinimas = tipoDocumento.NumeroFirmasMinimas;
				entidadAnexo.NumeroFirmasMaximas = tipoDocumento.NumeroFirmasMaximas;
				entidadAnexo.NombreArchivoAdjunto = NombreArchivoAdjunto;
				entidadAnexo.FormatoArchivoAdjunto = FormatoArchivoAdjunto;
				entidadAnexo.IdTipoCanalIngreso = IdTipoCanalIngreso;
				sql.batch(conn, SQL_ANEXO_INSERT, Arrays.asList(entidadAnexo), EntidadAnexos.class);

				IdAnexo = idAnexo.toString();

			} catch (Exception e) {
				conn.rollback();
				throw new Exception(e);
			}
		}
		// Se actualiza el El ultimo Anexo
		sql.batch(conn, SQL_ULTIMO_ANEXO_UPDATE, Arrays.asList(entidadAnexo), EntidadAnexos.class);

		// Se actualiza el Adjunto y el estado

		EntidadArchivo archivo = new EntidadArchivo();
		archivo.FilePart =  filePart;
		archivo.Estado =  "ACT";
		archivo.NombreArchivoAdjunto =  entidadAnexo.NombreArchivoAdjunto;
		archivo.FormatoArchivoAdjunto =  FormatoArchivoAdjunto;
		archivo.IdDocumento =  entidadAnexo.IdDocumento;
		archivo.IdAnexo =  entidadAnexo.IdAnexo;

		sql.batch(conn, SQL_ARCHIVO_UPDATE, Arrays.asList(archivo), EntidadArchivo.class);
		
		if (IdAnexoEntrada != null && IdAnexoEntrada != 0) { 
			try {  
				EntidadAnexo anexoInsert_IN = new EntidadAnexo();
				anexoInsert_IN.IdEstado = "VCD";
				anexoInsert_IN.IdAnexo = IdAnexoEntrada;
				anexoInsert_IN.IdDocumento = Long.parseLong(IdDocumento);
			  
				sql.batch(conn, SQL_ANEXO_UPDATE_ESTADO,Arrays.asList(anexoInsert_IN), EntidadAnexo.class); 
		    } catch (Exception e) {
		 		conn.rollback(); 
		  		throw new Exception(e); 
			}
		}
		   
		return idAnexo;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	// -------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	// -------------------------------------------------
	public static void main(String[] args) {
		System.out.println("== INICIO PRUEBA LOCAL ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {

			String jsonCL = "{\"v_IdEmpresa\":\"1\",\"v_IdCliente\":\"14185\",\"v_IdTipoCanal\":\"OWP\"}";
			Gson gson = new Gson();
			EntidadEntradaFirmada entrada = gson.fromJson(jsonCL,EntidadEntradaFirmada.class);
			try {

				GenerarDocumentosFirmados demo = new GenerarDocumentosFirmados();
				demo.logica(con, entrada);

			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
			// con.rollback();
		con.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		System.out.println("== FIN PRUEBA LOCAL ==");
	}

	
	
}