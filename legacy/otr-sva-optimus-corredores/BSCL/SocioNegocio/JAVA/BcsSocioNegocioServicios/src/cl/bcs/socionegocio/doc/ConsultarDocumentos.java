package cl.bcs.socionegocio.doc;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.bck.ConsultarEstadoContratoBlockchain;
import cl.bcs.socionegocio.pojo.Validacion;
import cl.bcs.socionegocio.soc.SocioNegocioUtils;
import cl.bcs.socionegocio.soc.pojo.ResumenPersona;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.StringUtils;
import cl.bcs.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConsultarDocumentos implements JavaCall {
	static final Logger logger = LogManager.getLogger(ConsultarDocumentos.class);

	//-------------------------------------------------
	// QUERIES
	//-------------------------------------------------
	private final String SQL_SELECT_TIPO_PERSONA 			= loadResourceToString("cl/bcs/socionegocio/doc/sql/ConsultarDocumentos_SelectTipoPersona.sql");
	private final String SQL_SELECT_CONSULTA_DOCUMENTO 		= loadResourceToString("cl/bcs/socionegocio/doc/sql/ConsultarDocumentos_SelectConsultaDocumento.sql");
	private final String SQL_SELECT_NOMBRE_ESTADO 			= loadResourceToString("cl/bcs/socionegocio/doc/sql/ConsultarDocumentos_SelectNombreEstado.sql");
	private final String SQL_SELECT_FIRMAS_PERSONA 			= loadResourceToString("cl/bcs/socionegocio/doc/sql/ConsultarDocumentos_SelectFirmasPersona.sql");
	private final String SQL_SELECT_IDENTIFICADOR_CLIENTE 	= loadResourceToString("cl/bcs/socionegocio/doc/sql/ConsultarDocumentos_SelectIdentificadorCliente.sql");
	private final String SQL_SELECT_CanalesIngresoDocumento = loadResourceToString("cl/bcs/socionegocio/doc/sql/ConsultarDocumentos_SelectCanalesIngresoDoc.sql");
	private final String SQL_SELECT_CanalIngresoDocumento 	= loadResourceToString("cl/bcs/socionegocio/doc/sql/ConsultarDocumentos_SelectCanalFirmaDocumento.sql");
	private final String SQL_SELECT_ProgramaDocumento       = loadResourceToString("cl/bcs/socionegocio/doc/sql/ConsultarDocumentos_SelectProgramaClienteDocumento.sql");

	//-------------------------------------------------
	// CLASES DE ENTRADA 
	//-------------------------------------------------
	public static class ConsultarDocumentos_Entrada extends BcsPojo {
		public Long p_IdUsuario;
		public Long p_IdEmpresa;
		public Long p_IdCliente;
		public Long p_IdPersonaFirmante;
		public String p_DocumentoIdentidadCliente;
		public String p_DocumentoIdentidadFirmante;
		public String p_IdEstadoDocumento;

		public ConsultarDocumentos_Entrada() {
			this.p_IdEstadoDocumento = "";
			this.p_DocumentoIdentidadCliente = "";
			this.p_DocumentoIdentidadFirmante = "";
		}
	}


	//-------------------------------------------------
	// CLASES DE SALIDA 
	//-------------------------------------------------
	public static class ConsultarDocumentos_Salida extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<PlataformaUtils.MensajeSalida> P_OUT_MENSAJES;
		public List<Documentos> p_out_Documentos;  
		

		public ConsultarDocumentos_Salida() {
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_MENSAJES 			= new ArrayList<>();
			p_out_Documentos 		= new ArrayList<>();
		}

		public void agregarValidacion(Validacion validacion) {
			if (validacion.EsError) {
				P_OUT_ERRORESCONTADOR++;
			}

			P_OUT_MENSAJES.add(validacion.MensajeError);
		}
	}
	
	public static class Documentos extends BcsPojo
	{
		public String		IdTipoDocumento;
		public String		NombreDocumento;
		public String		Glosa;
		public Long			NumeroFirmasMinimas;
		public Long			NumeroFirmasMaximas;
		public String		SolicitarFechaFirma;
		public String		ValidaFechaVigencia;
		public Long			IdAnexo;
		public Long			IdCliente;
		public Long			IdDocumento;
		public String		IdEstado;
		public String		TieneAdjunto;
		public String		NombreEstado;
		public Timestamp	FechaVigencia;
		public String		PermiteFirmarSinDocumento;    
		public Long			IdClaseDocumento;
		public String		TipoDocumentoNormativa;
		public String		FechaFirma;
		public String       IdTipoCanalIngreso;
		public String       IdEstadoFirmaClienteConsulta;
		public Timestamp    FechaFirmaClienteConsulta;
		public String		IdTipoCanalIngresoFirmaClienteConsulta;
		public String       IdTipoCanalFirma;
		public Integer      CantidadMinimaFirmasRequeridas;
		public String       AgenteNombreCompleto;
		public Timestamp	FechaIngreso;
		public Timestamp    FechaActivacion;
		public String       CodigoArchivo;
		public String       NombrePlantillaContrato;
		public String 		ValidaClienteFull;
		public Date			FechaModificacion;
		public String		UsuarioFirmante;
		public List<CanalesIngreso> CanalesDeIngreso;
		public String		DocumentoSubido;
		public String 		SolicitadoPor;
		public String 		FirmadoPor;
		public String 		UsuarioSolicitante;
		
	}
	
	
	//-------------------------------------------------
	// CLASES UTILITARIAS 
	//-------------------------------------------------
	public static class SelectTipoPersona extends BcsPojo {
		public String Tipo = "";
	}
	public static class CanalesIngreso {
		public String	IdTipoCanalIngreso	= "";
		public String 	NombreTipoCanal		= "";
	}
	public static class SelectCanalesIngreso {
		public String	IdTipoCanalIngreso	= "";
		public String 	IdTipoDocumento 	= "";
		public String	Nombre				= "";
	}
	public static class SelectNombreEstado extends BcsPojo {
		public String IdEstado = "";
		public String Nombre = "";
	}
	public static class SelectCanalIngreso{
		public Long 	IdFirmaAnexo;
		public String 	IdTipoCanalIngreso;	
	}
	public static class FirmaAnexo extends BcsPojo {
		public Long IdAnexo;
		public String IdEstado;
		public String IdTipoCanalIngreso;
		public Long IdPersonaAgente;
		public java.sql.Timestamp FechaFirma;
		public String EsAutorizado;
	}

	public static class IdentificadorCliente extends BcsPojo {
		public Long IdCliente;
	}
	
	public static class ProgramaDocumentoCliente extends BcsPojo {
		public Long ProgramaDocumentoEstado;
	}

	//-------------------------------------------------
	// LOGICA DEL SERVICIO 
	//-------------------------------------------------
	public ConsultarDocumentos_Salida logica(Connection conn, ConsultarDocumentos_Entrada in) throws Exception {
		logger.info("INICIO::ConsultarDocumentos");
		long inicio = System.currentTimeMillis();

		SQL sql = new SQL();

		ConsultarDocumentos_Salida salida = new ConsultarDocumentos_Salida();

		
		HashMap<String, Object> parametroEntrada = new HashMap<String, Object>();
		List<Documentos> documentos = new ArrayList<Documentos>();

		Long idCliente = in.p_IdCliente;
		
	
		
		String documentoIdentidadCliente = in.p_DocumentoIdentidadCliente;

		if (idCliente == null || documentoIdentidadCliente.length() > 0) {
			parametroEntrada.put("DocumentoIdentidad", documentoIdentidadCliente);

			IdentificadorCliente socioCliente = sql.query(conn, SQL_SELECT_IDENTIFICADOR_CLIENTE, parametroEntrada, IdentificadorCliente.class)
					.stream()
					.findFirst()
					.orElse(new IdentificadorCliente());
			idCliente = socioCliente.IdCliente;

			parametroEntrada.clear();
		}
		

		if (idCliente == null) {
			HashMap<String, String> params = new HashMap<>(2);
			params.put("DocumentoIdentidad", documentoIdentidadCliente);

			Validacion validacion = new Validacion();
			validacion.EsError = true;
			validacion.MensajeError = new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, "UI_NO_EXISTE_CLIENTE_PARA_DOCUMENTO_IDENTIDAD", params, conn, in.p_IdEmpresa);

			salida.agregarValidacion(validacion);

			return salida;
		}

		parametroEntrada.put("p_IdCliente", idCliente);
		List<SelectTipoPersona> SelectTipoPersona = sql.query(conn, SQL_SELECT_TIPO_PERSONA, parametroEntrada, SelectTipoPersona.class);

		

		
		parametroEntrada.put("p_IdCliente", idCliente);
		parametroEntrada.put("p_IdEmpresa", in.p_IdEmpresa);
		parametroEntrada.put("p_IdTipoPersona", SelectTipoPersona.get(0).Tipo);
		parametroEntrada.put("p_IdEstadoDocumento", in.p_IdEstadoDocumento);
		

		System.out.println(parametroEntrada);
		List<Documentos> SelectDocumentos = sql.query(conn, SQL_SELECT_CONSULTA_DOCUMENTO, parametroEntrada, Documentos.class);
		int totalDocumentos = SelectDocumentos.size();
		
		parametroEntrada.clear();
		parametroEntrada.put("p_IdCliente", idCliente);
		List<ProgramaDocumentoCliente> SelectProgramaDocumento = sql.query(conn, SQL_SELECT_ProgramaDocumento , parametroEntrada, ProgramaDocumentoCliente.class);
				
		int existenDocumentosPendientes = SelectProgramaDocumento.size();
		
		if (existenDocumentosPendientes>0) {
			HashMap<String, String> params = new HashMap<>(2);
			params.put("DocumentoIdentidad", documentoIdentidadCliente);

			Validacion validacion = new Validacion();
			validacion.EsError = true;
			validacion.MensajeError = new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, "UI_DOCUMENTOS_EN_PROCESO", params, conn, in.p_IdEmpresa);

			salida.agregarValidacion(validacion);

			return salida;
		}

		calcularFechaVencimiento(conn, in.p_IdEmpresa, SelectDocumentos);

		Long idPersonaFirmante = obtenerIdPersonaFirmante(conn, in);
		Map<Long, FirmaAnexo> firmasPersonaPorIdAnexo = obtenerFirmasPersonaPorIdAnexo(conn, idPersonaFirmante);
		
		Map<String, SelectNombreEstado> SelectNombreEstado = sql.query(conn, SQL_SELECT_NOMBRE_ESTADO, new HashMap<String,Object>(), SelectNombreEstado.class).stream().collect(Collectors.toMap(estado -> estado.IdEstado, estado -> estado));
		
		
		for(Documentos selectDocumentos : SelectDocumentos)
		{
			String estadoDocumento = "";
			if (SelectDocumentos.size() > 0) {			
				HashMap<String,Object> parametroEntradaCanal = new HashMap<String,Object>();
				parametroEntradaCanal.put("IdAnexo", selectDocumentos.IdAnexo);
				List<SelectCanalIngreso> SelectCanalIngreso = sql.query(conn, SQL_SELECT_CanalIngresoDocumento, parametroEntradaCanal, SelectCanalIngreso.class);
				if (SelectCanalIngreso.size()>0) {
					selectDocumentos.IdTipoCanalFirma = SelectCanalIngreso.get(0).IdTipoCanalIngreso;
				}
				
				if(selectDocumentos.IdClaseDocumento == 2 || selectDocumentos.IdClaseDocumento == 3)
				{
					ConsultarEstadoContratoBlockchain consultarEstadoContratoBlockchain = new ConsultarEstadoContratoBlockchain();
					ConsultarEstadoContratoBlockchain.ConsultarEstadoContratoBlockchain_Entrada entradaConsultarEstadoContratoBlockchain = new ConsultarEstadoContratoBlockchain.ConsultarEstadoContratoBlockchain_Entrada();
					entradaConsultarEstadoContratoBlockchain.p_IdTipoDocumento = selectDocumentos.IdTipoDocumento;
					entradaConsultarEstadoContratoBlockchain.p_IdCliente      = idCliente.toString();
					entradaConsultarEstadoContratoBlockchain.p_IdEmpresa      = in.p_IdEmpresa;
					entradaConsultarEstadoContratoBlockchain.p_IdModulo       = "";
					
					ConsultarEstadoContratoBlockchain.ConsultarEstadoContratoBlockchain_Salida salidaConsultarEstadoContratoBlockchain = consultarEstadoContratoBlockchain.logica(conn, entradaConsultarEstadoContratoBlockchain);
					
				
					if(salidaConsultarEstadoContratoBlockchain.p_Out_ContadorError > 0)
					{
						selectDocumentos.IdEstado = "ERR";
						selectDocumentos.NombreEstado = "SIN CONEXION BC";
					}
					else
					{
						if(salidaConsultarEstadoContratoBlockchain.p_Out_Status.equals("APPROVED"))
						{
							estadoDocumento = "APD";
							selectDocumentos.TieneAdjunto = "S";
						}
						else if (salidaConsultarEstadoContratoBlockchain.p_Out_Status.equals("REJECTED"))
						{
							estadoDocumento = "RZD";
							selectDocumentos.TieneAdjunto = "S";
						}
						else if (salidaConsultarEstadoContratoBlockchain.p_Out_Status.equals("PENDING"))
						{
							estadoDocumento = "PAU";
							selectDocumentos.TieneAdjunto = "S";
						}
						else if (salidaConsultarEstadoContratoBlockchain.p_Out_Status.equals("NOFOUND"))
						{
							estadoDocumento = "SIC";
						}
					
						
						selectDocumentos.IdEstado = SelectNombreEstado.get(estadoDocumento).IdEstado;
						selectDocumentos.NombreEstado = SelectNombreEstado.get(estadoDocumento).Nombre;
					}
				}
			}
			FirmaAnexo firmaAnexo = firmasPersonaPorIdAnexo.computeIfAbsent(selectDocumentos.IdAnexo, idAnexo -> {
				FirmaAnexo fau = new FirmaAnexo();
				fau.IdAnexo = idAnexo;

				return fau;
			});
			selectDocumentos.FechaFirmaClienteConsulta = firmaAnexo.FechaFirma;
			selectDocumentos.IdEstadoFirmaClienteConsulta = firmaAnexo.IdEstado;
			selectDocumentos.IdTipoCanalIngresoFirmaClienteConsulta =  firmaAnexo.IdTipoCanalIngreso;

			documentos.add(selectDocumentos);

		}
		
		parametroEntrada.clear();
		ArrayList <String> IdTipoDocumentos = new ArrayList<String>();
		for ( Documentos documento	:	documentos) {
			IdTipoDocumentos.add(documento.IdTipoDocumento);			
		} 
		parametroEntrada.put("IdtipoDocumento", IdTipoDocumentos);

		
		if (parametroEntrada.size() >0) {
			List<SelectCanalesIngreso> SelectCanales= sql.query(conn, SQL_SELECT_CanalesIngresoDocumento ,parametroEntrada, SelectCanalesIngreso.class);
			
			int i =0;
			
			for (Documentos documento: documentos) {
				List<CanalesIngreso> CanalesDOC  = new ArrayList<CanalesIngreso>();
				for ( int j = i ; j<SelectCanales.size();j++) {			
					if (documento.IdTipoDocumento.equals(SelectCanales.get(i).IdTipoDocumento)) {
						CanalesIngreso canalIngreso = new CanalesIngreso();
						canalIngreso.IdTipoCanalIngreso = SelectCanales.get(i).IdTipoCanalIngreso;
						canalIngreso.NombreTipoCanal = SelectCanales.get(i).Nombre;
						CanalesDOC.add(canalIngreso);
						//CanalesDOC.add(SelectCanales.get(i).IdTipoCanalIngreso);
						i++;
					}else {
						break;
					}
				}
				documento.CanalesDeIngreso = CanalesDOC;
			}
		}
		

		
		salida.p_out_Documentos = documentos;

		
		long fin = System.currentTimeMillis();
		logger.info("FIN::ConsultarDocumentos", fin - inicio);

		return salida;
	}

	private void calcularFechaVencimiento(Connection con, Long idEmpresa, List<Documentos> documentos) throws Exception {
		logger.debug("INICIO::ConsultarDocumentos.calcularFechaVencimiento");
		long inicio = System.currentTimeMillis();
		
		String numeroDiasVencimiento = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA","PLA.NumeroDiasFechaVencimiento");
		
		int diasVencimiento = Integer.valueOf(numeroDiasVencimiento);
		documentos.stream().filter(documento -> documento.FechaModificacion != null).forEach(documento -> {
			if(!"PEN".equals(documento.IdEstado)) {
				documento.FechaVigencia = new Timestamp((Fecha.addDays(documento.FechaModificacion, diasVencimiento).getTime()));
			}
		});
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarDocumentos.calcularFechaVencimiento", fin - inicio);
	} 

	private Long obtenerIdPersonaFirmante(Connection conn, ConsultarDocumentos_Entrada entrada) throws SQLException { 
		logger.debug("INICIO::ConsultarDocumentos.obtenerIdPersonaFirmante");
		long inicio = System.currentTimeMillis();

		Long id = entrada.p_IdPersonaFirmante;
		if (id == null && !StringUtils.isNullOrBlank(entrada.p_DocumentoIdentidadFirmante)) {
			ResumenPersona resumenPersona = SocioNegocioUtils
					.obtenerResumenPersonaPorDocumentoIdentidad(conn, new ArrayList<>(Collections.singletonList(entrada.p_DocumentoIdentidadFirmante)))
					.getOrDefault(entrada.p_DocumentoIdentidadFirmante, new ResumenPersona());
			id = resumenPersona.IdPersona;
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarDocumentos.obtenerIdPersonaFirmante", fin - inicio);

		return id;
	}

	private Map<Long, FirmaAnexo> obtenerFirmasPersonaPorIdAnexo(Connection conn, Long idPesonaFirmante) throws SQLException {
		logger.debug("INICIO::ConsultarDocumentos.obtenerFirmasPersonaPorIdAnexo");
		long inicio = System.currentTimeMillis();

		if (idPesonaFirmante == null) {
			return new HashMap<>();
		}

		HashMap<String, Object> params = new HashMap<>(2);
		params.put("IdPersona", idPesonaFirmante);

		SQL sql = new SQL();
		Map<Long, FirmaAnexo> firmasPersonaPorIdAnexo = sql.query(conn, SQL_SELECT_FIRMAS_PERSONA, params, FirmaAnexo.class)
				.stream()
				.collect(Collectors.toMap(fau -> fau.IdAnexo, fau -> fau));

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarDocumentos.obtenerFirmasPersonaPorIdAnexo", fin - inicio);

		return firmasPersonaPorIdAnexo;
	}
	
	
	//-------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE - CALL
	//-------------------------------------------------------------------------------------------
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		ConsultarDocumentos_Entrada in = gson.fromJson(jin.toString(), ConsultarDocumentos_Entrada.class);
		ConsultarDocumentos_Salida out = logica(conn, in);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	//-------------------------------------------------
	// METODO PARA PRUEBAS LOCALES 
	//-------------------------------------------------
	
	public static void main(String[] args)
	{
		System.out.println("== INICIO PRUEBA LOCAL ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection())
		{
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();

			//String jsonCL = "{\"p_IdEmpresa\":1,\"p_IdCliente\":524890}";
			//String jsonCL = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdEstadoDocumento\":\"PDF\",\"p_DocumentoIdentidadCliente\":\"89090100-K\",\"p_DocumentoIdentidadFirmante\":\"336-0\"  }";
String jsonCL = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdEstadoDocumento\":\"PDF\",\"p_DocumentoIdentidadCliente\":\"89090100-K\",\"p_DocumentoIdentidadFirmante\":\"336-0\"  }";
			//jsonCL = "{\"p_IdEmpresa\":1, \"p_IdCliente\": 524893, \"p_IdUsuario\":9,\"p_IdEstadoDocumento\":\"PDF\",\"p_DocumentoIdentidadCliente\":\"89090100-K\",\"p_DocumentoIdentidadFirmante\":\"336-0\"  }";
			ConsultarDocumentos_Entrada Objin = gson.fromJson(jsonCL, ConsultarDocumentos_Entrada.class);

			ConsultarDocumentos demo = new ConsultarDocumentos();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			
			try {
				ConsultarDocumentos_Salida ObjOut = demo.logica(con, Objin);
				con.rollback();
				System.out.println("SALIDA");
				System.out.println(ObjOut.toString());
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
			con.rollback();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		System.out.println("== FIN PRUEBA LOCAL ==");
	}
}