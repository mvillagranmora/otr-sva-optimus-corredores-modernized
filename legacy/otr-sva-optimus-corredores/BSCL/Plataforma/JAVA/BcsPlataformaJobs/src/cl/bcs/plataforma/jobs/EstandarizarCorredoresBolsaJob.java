package cl.bcs.plataforma.jobs;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.StringUtils;

import com.google.gson.JsonObject;

import cl.bcs.nuam.StoredProcedureException;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.socionegocio.cnt.CrearContraparte;
import cl.bcs.socionegocio.cnt.CrearContraparte.EntradaServicioCrearContraparte;
import cl.bcs.socionegocio.cnt.CrearContraparte.SalidaServicioCrearContraparte;
import cl.bcs.socionegocio.pojo.Enrolamiento;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class EstandarizarCorredoresBolsaJob extends BcsJob implements JavaCall{
	
	private final Logger LOGGER = LogManager.getLogger(EstandarizarCorredoresBolsaJob.class);
	
	private final String SQL_SELECT_CORREDORES_RUEBAS = loadResourceToString("cl/bcs/plataforma/jobs/sql/EstandarizarCorredoresBolsa_Select_CorredoresRuebas.sql"); 
	private final String SQL_SELECT_CONSULTAR_EXISTENCIA_CORREDOR = loadResourceToString("cl/bcs/plataforma/jobs/sql/EstandarizarCorredoresBolsa_Select_ConsultarExistenciaCorredor.sql");
	private final String SQL_SELECT_CORREDORAS_BCS = loadResourceToString("cl/bcs/plataforma/jobs/sql/EstandarizarCorredoresBolsa_Select_CorredorasBcs.sql");
	private final String SQL_SELECT_CORREDORES_SIN_PERSONA = loadResourceToString("cl/bcs/plataforma/jobs/sql/EstandarizarCorredoresBolsa_Select_CorredoresSinPersona.sql");
	private final String SQL_SELECT_CODIGO_CORREDORES = loadResourceToString("cl/bcs/plataforma/jobs/sql/EstandarizarCorredoresBolsa_Select_CodigoCorredores.sql");
	
	private final String SQL_UPSERT_CORREDORAS_BCS = loadResourceToString("cl/bcs/plataforma/jobs/sql/EstandarizarCorredoresBolsa_Upsert_CorredorasBcs.sql");
	private final String SQL_UPSERT_PERSONA_CORREDORES = loadResourceToString("cl/bcs/plataforma/jobs/sql/EstandarizarCorredoresBolsa_Upsert_PersonaCorredores.sql");
	private final String SQL_UPSERT_CODIGO_CORREDORES = loadResourceToString("cl/bcs/plataforma/jobs/sql/EstandarizarCorredoresBolsa_Upsert_CodigoCorredores.sql");
	private final String SQL_UPDATE_DESACTIVAR_CORREDOR_BCS = "UPDATE \"BSCL.Plataforma.BD::INS.Corredora\" SET \"IdEstado\" = 'INA'  WHERE \"IdCorredora\" = :IdCorredora;";
	private final String SQL_UPDATE_NOMBRE_CORTO_BCS = "UPDATE \"BSCL.Plataforma.BD::INS.Corredora\" SET \"NombreCorto\" = :NombreCorto  WHERE \"IdCorredora\" = :IdCorredora;";
	private final String SQL_UPDATE_RAZON_SOCIAL_BCS = "UPDATE \"BSCL.SocioNegocio.BD::PER.Persona\" SET \"RazonSocial\" = :RazonSocial  WHERE \"IdPersona\" = :IdPersona;";
	private final String SQL_INSERT_CORREDOR_NO_EXISTENTE = loadResourceToString("cl/bcs/plataforma/jobs/sql/EstandarizarCorredoresBolsa_Insert_Corredor_No_Existente.sql");
	
	private AtomicInteger idCabeceraLog = new AtomicInteger(1);
	
	private boolean DEBUG = false;
	
	public static class CorredorBcs{
		public Long IdPersona;
		public Long IdSocioDeNegocio;
		public String DocumentoIdentidad;
		public String TipoDocumentoIdentidad;
		public Long IdContraparte;
		public Long IdCorredora;
		public String CodigoInscripcionRegulador;
		public String NombreCorto;
		public String RazonSocial;
		//datos corredor ruebas
		public String NEMO;
		public String COD_CORR_SVS;
		public String COD_VALPO;
		public String COD_CORR_BEC;
		public String COD_CORR;
		
		public CorredorBcs() {}
		public CorredorBcs(Long idPersona, Long idSocioDeNegocio, String documentoIdentidad, Long idContraparte) {
			this.IdPersona = idPersona;
			this.IdSocioDeNegocio = idSocioDeNegocio;
			this.DocumentoIdentidad = documentoIdentidad;
			this.IdContraparte = idContraparte;
		}
		
		
		public Long getIdCorredora() {
			return IdCorredora;
		}
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CorredorRuebas{
		public String RUT_CORR;
		public String RAZON_SOCIAL;
		public String NEMO;
		public BigDecimal COD_CORR;
		public BigDecimal COD_CORR_SVS;
		public BigDecimal COD_CORR_VALPO;
		public BigDecimal COD_CORR_BEC;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class EntidadCorredora{
		public Long IdCorredora;
		public Long IdPersona;
		public String NombreCorto;
		public String CodigoInscripcionRegulador;
		public String IdEstado;
		public String Borrado;
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CodigoCorredora{
		public Long IdCorredora;
		public String IdBolsa;
		public String CodigoCorredora;

		public CodigoCorredora() {}
		public CodigoCorredora(Long idCorredora, String idBolsa, String codigoCorredora) {
			this.IdBolsa = idBolsa;
			this.IdCorredora = idCorredora;
			this.CodigoCorredora = codigoCorredora;
		}
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	private Connection getRuebasConnection(Connection conn, Long idEmpresa) throws SQLException, FileNotFoundException, IOException, StoredProcedureException {
		
		String rutaArchivosConfiguracion = Utils.getConfiguracion(conn, Utils.getSchema(), idEmpresa, "PLA.RutaArchivosConfiguracion");
		
		//Leer archivo properties
		if(DEBUG) {
			rutaArchivosConfiguracion = "C:\\BCS\\repoDES\\BSCL\\Properties\\";	
		}
		try (InputStream input = new FileInputStream(rutaArchivosConfiguracion + "ruebas.properties")) {
			Properties props = new Properties();
			props.load(input);
			LOGGER.debug(props.get("DBurl"));
			return DriverManager.getConnection(props.getProperty("DBurl"), props.getProperty("DBuser"), props.getProperty("DBpass"));
		} 
	}
	

	//ejemplo ActualizaEmisores
	@Override
	public boolean transaction(Connection conBCS) throws InterruptedException, Throwable {
		LOGGER.info("INICIO Estandarizar corredores de bolsa");
		
		//instanciar log bd.
		AgregarLog agregarLog = new AgregarLog();
		InAgregarLog inLog = new InAgregarLog();
		inLog.p_IdTrabajo =  1L;
		
		Long idEmpresaDefecto = Long.parseLong(Optional.ofNullable(this.getParam("IdEmpresa")).orElse("1"));
		Long idUsuarioDefecto = Long.parseLong(Optional.ofNullable(this.getParam("IdUsuario")).orElse("1"));
		String idModulo = Optional.ofNullable(this.getParam("IdModulo")).orElse("PLA");
		
		try(Connection connRuebas = getRuebasConnection(conBCS, idEmpresaDefecto)){
			//obtener datos desde el job.
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conBCS, idEmpresaDefecto, "PLATAFORMA", "PLA.FechaSistema");
			
			Long idCarga = 999L;
			
			SQL sql = new SQL();
			LOGGER.debug("1 - Obtener corredores desde la ruebas");
			List<CorredorRuebas> corredoresRuebas = sql.query(connRuebas, SQL_SELECT_CORREDORES_RUEBAS, new HashMap<String, Object>(), CorredorRuebas.class).parallelStream().map(corr -> {
				corr.RUT_CORR = Optional.ofNullable(corr.RUT_CORR).orElse("").trim();
				return corr;
			}).collect(Collectors.toList());
			HashMap<String, Object> hashCorredoresRuebas = Utils.List2Hash(corredoresRuebas, "RUT_CORR");
			
			LOGGER.debug("2 - Consultar corredores de la bcs");
			HashMap<String, Object> params = new HashMap<String, Object>();
			List<CorredorBcs> corredoresBcs = sql.query(conBCS, SQL_SELECT_CONSULTAR_EXISTENCIA_CORREDOR, params, CorredorBcs.class);
			Map<String, Object> hashCorredoresBcs = Utils.List2Hash(corredoresBcs, "DocumentoIdentidad");
			LOGGER.trace("Filtrar cuales corredores de optimus existen en ruebas");
			List<CorredorBcs> corredoresBcsExistenEnSebra = corredoresBcs.stream().filter(corrBcs -> hashCorredoresRuebas.containsKey(corrBcs.DocumentoIdentidad)).collect(Collectors.toList());
			
			LOGGER.debug("3 - Preparar creacion de personas/contrapartes");
			EntradaServicioCrearContraparte entradaCrearContraparte = new EntradaServicioCrearContraparte();
			entradaCrearContraparte.p_IdEmpresa = idEmpresaDefecto;
			entradaCrearContraparte.p_IdModulo = idModulo;
			entradaCrearContraparte.p_IdUsuario = idUsuarioDefecto;
			entradaCrearContraparte.p_UsaReglasNegocio = "N";
			entradaCrearContraparte.p_ConsultarEnrolamiento = new ArrayList<>();
			
			List<CorredorRuebas> corredoresSebraNoExistenEnOptimus = new ArrayList<>();
			for(CorredorRuebas corredorRuebas: corredoresRuebas) {
				CorredorBcs corredorBcs = (CorredorBcs) hashCorredoresBcs.get(corredorRuebas.RUT_CORR);
				
				if(corredorBcs==null || ( corredorBcs!=null && corredorBcs.IdContraparte == null )) {
					LOGGER.debug("Persona o contraparte no existe: " + corredorRuebas.RUT_CORR + "; " + corredorRuebas.NEMO);
					Enrolamiento enrolamiento = new Enrolamiento();
					enrolamiento.Tipo = "J";
					enrolamiento.Nombres = "";
					enrolamiento.ApellidoMaterno = "";
					enrolamiento.ApellidoPaterno = "";
					enrolamiento.RazonSocial = corredorRuebas.RAZON_SOCIAL;
					enrolamiento.IdTipoEntidad = "CONTRAPARTE";
					enrolamiento.IdTipoDocumentoIdentidad = "R";
					enrolamiento.IdTipoDocumentoIdentidadAsociado = enrolamiento.IdTipoDocumentoIdentidad; 
					enrolamiento.DocumentoIdentidad = corredorRuebas.RUT_CORR;
					//datos obligatorios.
					enrolamiento.IdEmpresa = idEmpresaDefecto;
					enrolamiento.IdUsuario = idUsuarioDefecto;
					enrolamiento.IdEntidadEnrolada = 0L;
					enrolamiento.IdTipoDocumentoIdentidadAdicional = "";
					enrolamiento.DocumentoIdentidadAdicional = "";
					enrolamiento.Nombres = "";
					enrolamiento.ApellidoMaterno="";
					enrolamiento.ApellidoPaterno="";
					enrolamiento.Sexo ="";
					enrolamiento.EstadoCivil = "";
					enrolamiento.IdTipoTelefono ="";
					enrolamiento.Telefono = "";
					enrolamiento.Email = "";
					enrolamiento.DireccionCalle = "";
					enrolamiento.DireccionNumero = "";
					enrolamiento.DireccionDepartamento = "";
					enrolamiento.IdPaisNacimiento = 0L;
					enrolamiento.IdCiudad = 0L;
					enrolamiento.CLI_IdCategoriaInversionista = "";
					enrolamiento.POR_IdPerfilComercial ="";
					enrolamiento.CLI_IdTipoCliente ="";
					enrolamiento.CLI_CodigoAdicionalHolding ="";
					enrolamiento.CLI_IdClaseCliente = "";
					enrolamiento.IdCarga = idCarga;
					//datos obligatorios contraparte
					enrolamiento.CNT_Otros ="";
					enrolamiento.CNT_Corredora="S";
					enrolamiento.CNT_CodigoInscripcion = String.valueOf(corredorRuebas.COD_CORR_SVS);
					
					entradaCrearContraparte.p_ConsultarEnrolamiento =  Arrays.asList(enrolamiento);
					LOGGER.trace("LLamar servicio crear contrapartes");
					
					CrearContraparte servicioCrearContraparte = new CrearContraparte();
					SalidaServicioCrearContraparte salidaCrearContraparte = servicioCrearContraparte.logica(conBCS, entradaCrearContraparte);
					if(salidaCrearContraparte != null && salidaCrearContraparte.P_OUT_REFIDPERSONA != 0 && salidaCrearContraparte.P_OUT_IDSOCIONEGOCIO != 0) {
						if(corredorBcs!=null) {
							LOGGER.debug("contraparte no existia, actualizar informacion en hashmap");
							inLog.agregarDetalleLogBasico(idCabeceraLog.longValue(), inLog.p_IdTrabajo, "Nuevo contraparte, documento:", "", String.valueOf(salidaCrearContraparte.P_OUT_DOCUMENTOIDENTIDAD));
							corredorBcs.IdPersona = salidaCrearContraparte.P_OUT_REFIDPERSONA;
							corredorBcs.IdSocioDeNegocio = salidaCrearContraparte.P_OUT_IDSOCIONEGOCIO;
							corredorBcs.IdContraparte = salidaCrearContraparte.P_OUT_REFIDCONTRAPARTE;
							corredorBcs.DocumentoIdentidad = salidaCrearContraparte.P_OUT_DOCUMENTOIDENTIDAD;
						}else {
							LOGGER.debug("persona nueva, actualizar hashmap y agregar a lista");
							inLog.agregarDetalleLogBasico(idCabeceraLog.longValue(), inLog.p_IdTrabajo, "Persona y contraparte nuevo, documento:", "", String.valueOf(salidaCrearContraparte.P_OUT_DOCUMENTOIDENTIDAD));
							hashCorredoresBcs.put(salidaCrearContraparte.P_OUT_DOCUMENTOIDENTIDAD, new CorredorBcs(salidaCrearContraparte.P_OUT_REFIDPERSONA, salidaCrearContraparte.P_OUT_IDSOCIONEGOCIO, salidaCrearContraparte.P_OUT_DOCUMENTOIDENTIDAD, salidaCrearContraparte.P_OUT_REFIDCONTRAPARTE));
							corredoresBcsExistenEnSebra.add((CorredorBcs) hashCorredoresBcs.get(salidaCrearContraparte.P_OUT_DOCUMENTOIDENTIDAD));	
						}
						
					}
				}else {
					LOGGER.debug("Persona y contrparte existen: " + corredorRuebas.RUT_CORR + "; " + corredorRuebas.NEMO);
					//inLog.agregarDetalleLogBasico(1L, inLog.p_IdTrabajo, "Contraparte existe, documento:", "", corredorRuebas.RUT_CORR );
				}
				
				LOGGER.trace("map corredor ruebas con contraparte bcs");
				corredorBcs = (CorredorBcs) hashCorredoresBcs.get(corredorRuebas.RUT_CORR);
				if(null!=corredorBcs) {
					corredorBcs.CodigoInscripcionRegulador = String.valueOf(corredorRuebas.COD_CORR_SVS);
					
					corredorBcs.COD_CORR = String.valueOf(corredorRuebas.COD_CORR);
					corredorBcs.COD_CORR_BEC = String.valueOf(corredorRuebas.COD_CORR_BEC);
					corredorBcs.COD_VALPO = String.valueOf(corredorRuebas.COD_CORR_VALPO);
					corredorBcs.NEMO = String.valueOf(corredorRuebas.NEMO);
				}else {
					LOGGER.debug("CORREDOR RUEBAS NO EXISTE EN BCS: " + corredorRuebas.NEMO);
					corredoresSebraNoExistenEnOptimus.add(corredorRuebas);
				}
			}
			
			if(!inLog.p_LogDetalle.isEmpty()) {
				//si se ingresaron detalles, entonces crear la cabecera para estos logs.
				inLog.agregarLogBasico(idEmpresaDefecto, idUsuarioDefecto, idModulo, idCabeceraLog.longValue(), inLog.p_IdTrabajo, "INGRESO", "CONTRAPARTE", fechaSistema, "Creacion personas y contrapartes para nuevas corredoras");
			}
			LOGGER.debug("4 - Ingresar Corredores Ruebas que existen optimus como socio pero no como corredor...");
			if(!corredoresSebraNoExistenEnOptimus.isEmpty()) {
				idCabeceraLog.incrementAndGet();
				inLog.agregarLogBasico(idEmpresaDefecto, idUsuarioDefecto, idModulo, idCabeceraLog.longValue(), inLog.p_IdTrabajo, "INGRESO", "CORREDOR", fechaSistema, "Ingresar Corredor sebra en tabla corredores optimus");
				corredoresSebraNoExistenEnOptimus.forEach(corr ->{
					corr.COD_CORR_SVS.setScale(0);
					inLog.agregarDetalleLogBasico(idCabeceraLog.longValue(), inLog.p_IdTrabajo, "Ingresar corredor:" +corr.NEMO, "", corr.COD_CORR_SVS.toString());
				});
				int res[] = sql.batch(conBCS, SQL_INSERT_CORREDOR_NO_EXISTENTE, corredoresSebraNoExistenEnOptimus, CorredorRuebas.class);
			}
			
			
			
			
			LOGGER.debug("4 - Desactivar corredores bcs que no estan en ruebas");
			idCabeceraLog.incrementAndGet();
			List<CorredorBcs> corredoresBcsDesactivar = corredoresBcs.stream().filter(corrBcs -> !hashCorredoresRuebas.containsKey(corrBcs.DocumentoIdentidad)).collect(Collectors.toList());
			if(!corredoresBcsDesactivar.isEmpty()) {
				inLog.agregarLogBasico(idEmpresaDefecto, idUsuarioDefecto, idModulo, idCabeceraLog.longValue(), inLog.p_IdTrabajo, "MODIFICACION", "CORREDOR", fechaSistema, "Desactivar corredores de bolsa Optimus que no estan en sebra");
				LOGGER.debug("coredores a desactivar: " + corredoresBcsDesactivar.stream().map(corrBcs -> {
					
					inLog.agregarDetalleLogBasico(idCabeceraLog.longValue(), inLog.p_IdTrabajo, "Desactivar corredor:" +corrBcs.NombreCorto, "ACT", "INA");
					
					return corrBcs.NombreCorto;
				}).collect(Collectors.toList()));
				sql.batch(conBCS, SQL_UPDATE_DESACTIVAR_CORREDOR_BCS, corredoresBcsDesactivar , CorredorBcs.class);
			}
			
			LOGGER.debug("5 - Actualizar NombreCorto Sebra -> Optimus");
			idCabeceraLog.incrementAndGet();
			List<CorredorBcs> corredorBcsNombreCortoIncorrecto = corredoresBcsExistenEnSebra.stream().filter(corredorBcs ->{
				boolean diferencias = false;
				CorredorRuebas corredorRuebas = (CorredorRuebas) hashCorredoresRuebas.get(corredorBcs.DocumentoIdentidad);
				String msg = String.format("COD_RUE: %s, COD_BCS: %s,\t NOMBRECORTO_RUE: %s, NOMBRECORTO_BCS: %s", corredorRuebas.COD_CORR_SVS, corredorBcs.CodigoInscripcionRegulador, corredorRuebas.NEMO, corredorBcs.NombreCorto);
				
				if(!StringUtils.trimAllWhitespace(corredorRuebas.NEMO).equalsIgnoreCase(StringUtils.trimAllWhitespace(corredorBcs.NombreCorto))) {
					inLog.agregarDetalleLogBasico(idCabeceraLog.longValue(), inLog.p_IdTrabajo, "Cambio NombreCorto :" , corredorBcs.NombreCorto, corredorRuebas.NEMO);
					LOGGER.debug("NombreCorto Diferente-> " + msg);
					diferencias = true;
				}
				
				return diferencias;
			}).map(corredorBcs -> {
				CorredorRuebas corredorRuebas = (CorredorRuebas) hashCorredoresRuebas.get(corredorBcs.DocumentoIdentidad);
				corredorBcs.NombreCorto = corredorRuebas.NEMO;
				return corredorBcs;
			}).collect(Collectors.toList());
			
			//ejecutar actualizacion nombres cortos o razones sociales
			if(!corredorBcsNombreCortoIncorrecto.isEmpty()) {
				inLog.agregarLogBasico(idEmpresaDefecto, idUsuarioDefecto, idModulo, idCabeceraLog.longValue(), inLog.p_IdTrabajo, "MODIFICACION", "CORREDOR", fechaSistema, "Modificacion Nombre Corto corredores de bolsa Optimus");
				//EJECUTAR UPDATE NEMO
				sql.batch(conBCS, SQL_UPDATE_NOMBRE_CORTO_BCS, corredorBcsNombreCortoIncorrecto, CorredorBcs.class);
			}
			
			LOGGER.debug("6 - Actualizar RazonSocial Sebra -> Optimus");
			idCabeceraLog.incrementAndGet();
			List<CorredorBcs> corredorBcsRazonSocialIncorrecto = corredoresBcsExistenEnSebra.stream().filter(corredorBcs ->{
				boolean diferencias = false;
				CorredorRuebas corredorRuebas = (CorredorRuebas) hashCorredoresRuebas.get(corredorBcs.DocumentoIdentidad);
				String msg = String.format("COD_RUE: %s, COD_BCS: %s, \t RAZON_RUE: %s, RAZON_BCS: %s", corredorRuebas.COD_CORR_SVS, corredorBcs.CodigoInscripcionRegulador, corredorRuebas.RAZON_SOCIAL, corredorBcs.RazonSocial);
				
				if(!StringUtils.trimAllWhitespace(corredorRuebas.RAZON_SOCIAL).equalsIgnoreCase(StringUtils.trimAllWhitespace(corredorBcs.RazonSocial))) {
					inLog.agregarDetalleLogBasico(idCabeceraLog.longValue(), inLog.p_IdTrabajo, "Cambio Razon Social:" , corredorBcs.RazonSocial, corredorRuebas.RAZON_SOCIAL);
					LOGGER.debug("RazonSocial Diferente-> " + msg);
					diferencias = true;
				}
				
				return diferencias;
			}).map(corredorBcs -> {
				CorredorRuebas corredorRuebas = (CorredorRuebas) hashCorredoresRuebas.get(corredorBcs.DocumentoIdentidad);
				corredorBcs.RazonSocial = corredorRuebas.RAZON_SOCIAL;
				return corredorBcs;
			}).collect(Collectors.toList());
			
			//ejecutar actualizacion Razon Social
			if(!corredorBcsRazonSocialIncorrecto.isEmpty()) {
				inLog.agregarLogBasico(idEmpresaDefecto, idUsuarioDefecto, idModulo, idCabeceraLog.longValue(), inLog.p_IdTrabajo, "MODIFICACION", "CORREDOR", fechaSistema, "Modificacion Razon social corredores de bolsa Optimus");
				//EJECUTAR UPDATE RAZON_SOCIAL
				sql.batch(conBCS, SQL_UPDATE_RAZON_SOCIAL_BCS, corredorBcsRazonSocialIncorrecto, CorredorBcs.class);
			}
			
					
			LOGGER.debug("7 - Consultar todas las corredoras actuales en bcs");
			params = new HashMap<>();
			HashMap<String, EntidadCorredora> hashEntidadCorredora = new HashMap<>();
			sql.query(conBCS, SQL_SELECT_CORREDORAS_BCS, params, EntidadCorredora.class).stream().forEach(ent -> {
				hashEntidadCorredora.put(ent.CodigoInscripcionRegulador, ent);
			});
			
			LOGGER.debug("8 - Asignar idCorredora a nuevas corredoras mediante mapeo de codigo inscripcion.");
			corredoresBcsExistenEnSebra.forEach(corredorBcs -> {
				EntidadCorredora entidadCorredora = hashEntidadCorredora.get(corredorBcs.CodigoInscripcionRegulador);
				if(entidadCorredora!=null) {
					//corredora ya existe en optimus
					corredorBcs.IdCorredora = entidadCorredora.IdCorredora;
				}
			});
			
			LOGGER.trace("ordernar lista de corredores por codigo corredora.");
			Collections.sort(corredoresBcsExistenEnSebra, (c1, c2) -> Long.valueOf(c1.COD_CORR).compareTo(Long.valueOf(c2.COD_CORR)));
			
			LOGGER.debug("9 - identificar cuantas corredoras no tienen idCorredora");
			Long cantidadNuevosCorredores = corredoresBcsExistenEnSebra.stream().filter(corredor -> corredor.IdCorredora==null).map(corr ->{
				LOGGER.info(String.format("Nuevo Corredor: %s, Documento identidad: %s, CodigoCorredor: %s", corr.NEMO,corr.DocumentoIdentidad,corr.COD_CORR));
				return corr;
			}).count();
			LOGGER.debug("Cantidad corredoras nuevas: " + cantidadNuevosCorredores);

			LOGGER.trace("obtener ids secuencia para nuevos corredores");
			LinkedList<Long> idsNuevosCorredores = new LinkedList<>();
			Utils.getSecuencia(conBCS, "BSCL.Plataforma.BD.Secuencias::Corredora", cantidadNuevosCorredores).forEach(sec -> idsNuevosCorredores.add(sec));
			
			LOGGER.debug("10 - Asignar id a nuevas corredoras");
			idCabeceraLog.incrementAndGet();
			if(!cantidadNuevosCorredores.equals(0L)) {
				inLog.agregarLogBasico(idEmpresaDefecto, idUsuarioDefecto, idModulo, idCabeceraLog.longValue(), inLog.p_IdTrabajo, "INGRESO", "CORREDOR", fechaSistema, "Creacion nuevos corredores de bolsa");
			}
			List<CorredorBcs> corredoresNuevos = corredoresBcsExistenEnSebra.stream().filter(corr -> corr.IdCorredora==null).map(corr -> {
				// logar nuevas corredoras.
				corr.IdCorredora = idsNuevosCorredores.pop();
				inLog.agregarDetalleLogBasico(idCabeceraLog.longValue(), inLog.p_IdTrabajo, "Corredor nuevo, codigo corredor:" , "", corr.COD_CORR);
				return corr;
			}).collect(Collectors.toList());
			
			
			LOGGER.debug("11 - Ingresar Corredores nuevos");
			sql.batch(conBCS, SQL_UPSERT_CORREDORAS_BCS, corredoresNuevos, CorredorBcs.class);
			LOGGER.trace(sql.query(conBCS, SQL_SELECT_CORREDORAS_BCS, params, EntidadCorredora.class));
			
			//obtener las corredoras a las que se les actualizara el idpersona
			idCabeceraLog.incrementAndGet();
			params.clear();
			params.put("CodigoInscripcionRegulador", corredoresNuevos.stream().map(corr -> corr.CodigoInscripcionRegulador).collect(Collectors.toList()));
			List<EntidadCorredora> corredoresSinPersona = sql.query(conBCS, SQL_SELECT_CORREDORES_SIN_PERSONA, params, EntidadCorredora.class);
			if(!corredoresSinPersona.isEmpty()) {
				inLog.agregarLogBasico(idEmpresaDefecto, idUsuarioDefecto, idModulo, idCabeceraLog.longValue(), inLog.p_IdTrabajo, "ACTUALIZACION", "CORREDOR", fechaSistema, "Actualizar ids persona a corredores de bolsa");
				corredoresSinPersona.forEach(corr->{
					inLog.agregarDetalleLogBasico(idCabeceraLog.longValue(), inLog.p_IdTrabajo, "Corredor", "", corr.NombreCorto);
				});
			}
			LOGGER.debug("12 - Actualizar ids persona a corredores existentes sin persona");
			params.clear();
			sql.batch(conBCS, SQL_UPSERT_PERSONA_CORREDORES, corredoresBcsExistenEnSebra, CorredorBcs.class);
			LOGGER.trace(sql.query(conBCS, SQL_SELECT_CORREDORAS_BCS, params, EntidadCorredora.class));
			
			LOGGER.debug("13 - Perparar actualizacion codigos corredora");
			List<CodigoCorredora> codigosCorredora = new ArrayList<>();
			corredoresBcsExistenEnSebra.forEach(corr ->{
				if(corr.COD_CORR!=null && !corr.COD_CORR.equals("") && !corr.COD_CORR.equals("0")) {
					codigosCorredora.add(new CodigoCorredora(corr.IdCorredora, "BCS", corr.COD_CORR));
				}
				if(corr.COD_CORR_BEC!=null && !corr.COD_CORR_BEC.equals("") && !corr.COD_CORR_BEC.equals("0")) {
					codigosCorredora.add(new CodigoCorredora(corr.IdCorredora, "BEC", corr.COD_CORR_BEC));
				}
				if(corr.COD_VALPO!=null && !corr.COD_VALPO.equals("") && !corr.COD_VALPO.equals("0")) {
					codigosCorredora.add(new CodigoCorredora(corr.IdCorredora, "BCV", corr.COD_VALPO));
				}
				
			});
			//obtener los codigos que se van a crear.
			HashMap<String, CodigoCorredora> hashCodigosActuales = new HashMap<>();
			sql.query(conBCS, SQL_SELECT_CODIGO_CORREDORES, params, CodigoCorredora.class).forEach(cod ->{
				hashCodigosActuales.put(String.format("%d_%s", cod.IdCorredora,cod.CodigoCorredora), cod);
			});
			
			boolean codigosNuevos = false;
			idCabeceraLog.incrementAndGet();
			for(CodigoCorredora cod: codigosCorredora){
				CodigoCorredora codAct = (CodigoCorredora) hashCodigosActuales.get(String.format("%d_%s", cod.IdCorredora,cod.CodigoCorredora));
				if(codAct==null) {
					codigosNuevos = true;
					inLog.agregarDetalleLogBasico(idCabeceraLog.longValue(), inLog.p_IdTrabajo, "Codigo corredor nuevo", "", String.format("Codigo: %s, bolsa: %s", cod.CodigoCorredora, cod.IdBolsa));
				}
			}
			if(codigosNuevos) {
				inLog.agregarLogBasico(idEmpresaDefecto, idUsuarioDefecto, idModulo, idCabeceraLog.longValue(), inLog.p_IdTrabajo, "INGRESO", "CORREDOR", fechaSistema, "Codigos corredora nuevos.");
			}
			
			LOGGER.debug("14 - Actualizar codigos corredoras");
			sql.batch(conBCS, SQL_UPSERT_CODIGO_CORREDORES, codigosCorredora, CodigoCorredora.class);
			
			//agregar logs
			if(!inLog.p_Log.isEmpty()) {
				agregarLog.logica(conBCS, inLog);
				LOGGER.debug(inLog);	
			}
		}
		LOGGER.info("TERMINO Estandarizar corredores de bolsa");
		return true;
	}

	@Override
	public void onError(Throwable t) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}
		
	public static void main(String[] args) {
		// Example
		System.out.println("== START ==");
		try (Connection con = PlataformaUtils.getBCOCHILEConnection()) {
			con.setAutoCommit(false);
			SQL.exec(con, "set schema BSCL");
			SQL.exec(con, "SET TRANSACTION AUTOCOMMIT DDL OFF");
			// ---------------------------------
			try {
				EstandarizarCorredoresBolsaJob apf = new EstandarizarCorredoresBolsaJob();
				apf.DEBUG = true;
				System.out.println(apf.transaction(con));
				con.commit();
			} catch (Exception er) {
				er.printStackTrace();
			} catch (Throwable e) {
				e.printStackTrace();
			}
			con.rollback();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}


	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		throw new RuntimeException("Not implemented");
	}

}
