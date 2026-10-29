package cl.bcs.socionegocio.per;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import cl.bcs.plataforma.pla.utils.OpenEJBEncryption;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;


import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;


import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.socionegocio.per.PurgaProspectoCliente.EstadoProceso;
import cl.bcs.socionegocio.per.PurgaProspectoCliente.LineaEntrada;
import cl.bcs.socionegocio.per.PurgaProspectoCliente.Persona;
import cl.bcs.socionegocio.per.PurgaProspectoCliente.TipoBorrado;
import cl.bcs.socionegocio.per.PurgaProspectoCliente.ConfiguracionTipoBorrado;
import cl.bcs.socionegocio.per.PurgaProspectoCliente.TipoEstado;
import cl.bcs.socionegocio.per.PurgaProspectoCliente.TipoMotivo;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ValidadorPurgaProspectoCliente implements JavaCall {
	static final Logger logger = LogManager.getLogger(ValidadorPurgaProspectoCliente.class);

	private final String SQL_OBTENER_RELACIONES_PERSONA = loadResourceToString("cl/bcs/socionegocio/per/sql/PurgaProspectoCliente_ObtenerRelacionesPersona.sql");
	private final String SQL_OBTENER_PERSONA_FALLECIDA = loadResourceToString("cl/bcs/socionegocio/per/sql/PurgaProspectoCliente_ObtenerPersonaFallecida.sql");
	private final String SQL_OBTENER_TIEMPOMINIMO_90DIAS = loadResourceToString("cl/bcs/socionegocio/per/sql/PurgaProspectoCliente_ObtenerTiempoMinimo90dias.sql");
	private final String SQL_OBTENER_TIEMPOMINIMO_3650DIAS = loadResourceToString("cl/bcs/socionegocio/per/sql/PurgaProspectoCliente_ObtenerTiempoMinimo3650dias.sql");
	private final String SQL_OBTENER_MOVSCTAINVERSION = loadResourceToString("cl/bcs/socionegocio/per/sql/PurgaProspectoCliente_ObtenerMovsCtaInversion.sql");
	private final String SQL_OBTENER_ACTUALIZACIONESPER = loadResourceToString("cl/bcs/socionegocio/per/sql/PurgaProspectoCliente_ObtenerActualizacionesPER.sql");
	private final String SQL_OBTENER_CUSCARTERA = loadResourceToString("cl/bcs/socionegocio/per/sql/PurgaProspectoCliente_ObtenerCusCartera.sql");
	private final String SQL_OBTENER_HISCARTERA = loadResourceToString("cl/bcs/socionegocio/per/sql/PurgaProspectoCliente_ObtenerHisCartera.sql");
	private final String SQL_OBTENER_OPERACIONESRUEDA = loadResourceToString("cl/bcs/socionegocio/per/sql/PurgaProspectoCliente_ObtenerOperacionesRueda.sql");
	private final String SQL_OBTENER_FWDSPT = loadResourceToString("cl/bcs/socionegocio/per/sql/PurgaProspectoCliente_ObtenerFWDSPT.sql");
	// private final String SQL_OBTENER_FFMM = loadResourceToString("cl/bcs/socionegocio/per/sql/PurgaProspectoCliente_ObtenerFFMM.sql");
	private final String SQL_OBTENER_OPERACIONESTES = loadResourceToString("cl/bcs/socionegocio/per/sql/PurgaProspectoCliente_ObtenerOperacionesTES.sql");
	private final String SQL_OBTENER_OPERACIONESFAC = loadResourceToString("cl/bcs/socionegocio/per/sql/PurgaProspectoCliente_ObtenerOperacionesFAC.sql");
	private final String SQL_OBTENER_CUSMOVTRANSITO = loadResourceToString("cl/bcs/socionegocio/per/sql/PurgaProspectoCliente_ObtenerCusMovTransito.sql");
	// private final String SQL_OBTENER_CONTRATOSAAPV = loadResourceToString("cl/bcs/socionegocio/per/sql/PurgaProspectoCliente_ObtenerContratosAAPV.sql");
	private final String SQL_OBTENER_MOVIMIENTOSSEBRA = loadResourceToString("cl/bcs/socionegocio/per/sql/PurgaProspectoCliente_ObtenerMovimientosSebra.sql");
	
	private static final int MIN_COLUMNAS = 3;
	private static final int MAX_COLUMNAS = 4;

	public static class EntradaValidadorPurgaProspectoCliente extends BcsPojo {
		public Long IdEmpresa;
		public List<LineaEntrada> Lineas;
		public List<Persona> Personas;
		public Set<String> TiposBorrado;
		public Map<String, List<ConfiguracionTipoBorrado>> configuracionPorCodigoTipoBorrado;

		public EntradaValidadorPurgaProspectoCliente() {
			this.Lineas = new ArrayList<>();
			this.Personas = new ArrayList<>();
		}
	}
	public static class movsPCB extends BcsPojo{
		public static final String RutTitular = null;

	}
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();

		EntradaValidadorPurgaProspectoCliente in = gson.fromJson(jin.toString(), EntradaValidadorPurgaProspectoCliente.class);

		logica(con, in);
		return new JsonObject();
	}


	public void logica(Connection con, EntradaValidadorPurgaProspectoCliente entrada) throws Exception {
		logger.info("INICIO::ValidadorPurgaProspectoCliente.logica");
		long inicio = System.currentTimeMillis();
		
		validarLineasEntrada(con, entrada.Lineas, entrada.Personas, entrada.configuracionPorCodigoTipoBorrado);
		
		Map<String, List<LineaEntrada>> lineasPorTipo = entrada.Lineas.stream().filter(LineaEntrada::esProcesable).collect(Collectors.groupingBy(linea -> linea.TipoBorrado));
		validarPorTipoBorrado(con, entrada.IdEmpresa, lineasPorTipo, entrada.Personas);
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::ValidadorPurgaProspectoCliente.logica {} ms", fin - inicio);
	}

	private void validarPorTipoBorrado(Connection con, Long idEmpresa, Map<String, List<LineaEntrada>> lineasPorTipo, List<Persona> personas) throws Exception {
		List<LineaEntrada> listaProspecto = lineasPorTipo.getOrDefault(TipoBorrado.PROSPECTO.Codigo, Collections.emptyList());
		if (!listaProspecto.isEmpty()) {
			validarProspecto(con, idEmpresa, listaProspecto, personas);
		}
		
		List<LineaEntrada> listaConsentimiento = lineasPorTipo.getOrDefault(TipoBorrado.CONSENTIMIENTO.Codigo, Collections.emptyList());
		if (!listaConsentimiento.isEmpty()) {
			validarConsentimiento(con, idEmpresa, listaConsentimiento, personas);
		}
		
		List<LineaEntrada> listaDefinitiva = lineasPorTipo.getOrDefault(TipoBorrado.DEFINITIVA.Codigo, Collections.emptyList());
		if (!listaDefinitiva.isEmpty()) {
			validarDefinitiva(con, idEmpresa, listaDefinitiva, personas);
		}
		
		List<LineaEntrada> listaSupresionDato = lineasPorTipo.getOrDefault(TipoBorrado.SUPRESION_DATO.Codigo, Collections.emptyList());
		if (!listaSupresionDato.isEmpty()) {
			validarSupresionDato(con, idEmpresa, listaSupresionDato, personas);
		}
	}
	
	private void validarProspecto(Connection con, Long idEmpresa, List<LineaEntrada> lineas, List<Persona> personas) throws Exception {
		if (lineas.isEmpty()) {
			return;
		}

		validarPersonaRelacionada(con, lineas);
		validar90Dias(con, idEmpresa, lineas);
		validarClienteActivo(con, idEmpresa, lineas, personas, false);
		
	}

	private void validarConsentimiento(Connection con, Long idEmpresa, List<LineaEntrada> lineas, List<Persona> personas) throws Exception {
		if (lineas.isEmpty()) {
			return;
		}

		validarPersonaRelacionada(con, lineas);
		validar90Dias(con, idEmpresa, lineas);
		validarClienteActivo(con, idEmpresa, lineas, personas, false);
	}

	private void validarDefinitiva(Connection con, Long idEmpresa, List<LineaEntrada> lineas, List<Persona> personas) throws Exception {
		if (lineas.isEmpty()) {
			return;
		}
		
		validarPersonaFallecida(con, lineas);
		validarPersonaRelacionada(con, lineas);
		validarClienteActivo(con, idEmpresa, lineas, personas, true);
	}

	private void validarSupresionDato(Connection con, Long idEmpresa, List<LineaEntrada> lineas, List<Persona> personas) throws Exception {
		if (lineas.isEmpty()) {
			return;
		}

		validarClienteActivo(con, idEmpresa, lineas, personas, false);

		// Para supresión de datos (tipo 11), el cliente debe estar activo.
		// validarClienteActivo marca CLIENTE_ACTIVO como NO_ELIMINADO.
		// Aquí se revierte el estado para permitir que continúe el procesamiento.
		for (LineaEntrada linea : lineas) {
			if (linea.EstadoProceso.TipoMotivo == TipoMotivo.CLIENTE_ACTIVO) {
				linea.EstadoProceso = new EstadoProceso(TipoEstado.SIN_ESTADO, TipoMotivo.NO_APLICA, "");
			}
			else if(linea.EstadoProceso.TipoMotivo == TipoMotivo.NO_APLICA) {
					linea.EstadoProceso = new EstadoProceso(TipoEstado.NO_ELIMINADO, TipoMotivo.NO_ACTIVO, "Persona no es cliente activo");
			}
		}

	}
	
	private void validarLineasEntrada(Connection con, List<LineaEntrada> lineasEntrada, List<Persona> personas, Map<String, List<ConfiguracionTipoBorrado>> configuracionPorCodigoTipoBorrado) throws Exception {
		logger.info("INICIO::ValidadorPurgaProspectoCliente.validarLineasEntrada");
		long inicio = System.currentTimeMillis();
		
		Map<String, Integer> conteoIdSolicitud = new HashMap<>();

		//Buscar repetidos
		for (LineaEntrada lineaEntrada : lineasEntrada) {
			if (!lineaEntrada.IdSolicitud.isEmpty()) {
				conteoIdSolicitud.merge(lineaEntrada.IdSolicitud, 1, Integer::sum);
			}
		}
		
		Set<String> documentosExistentes = new HashSet<>();
		
		if (personas != null && !personas.isEmpty()) {
			documentosExistentes = personas.stream().map(p -> p.RutTitular).filter(Objects::nonNull).collect(Collectors.toSet());
		}
			
		for (LineaEntrada lineaEntrada : lineasEntrada) {
			String mensaje = "";

			if (lineaEntrada.estaVacia()) {
				continue;
			}

			if (lineaEntrada.CantidadColumnas < MIN_COLUMNAS) {
				mensaje = "El registro no contiene la cantidad mínima de campos requeridos (3). ";
				agregarEstadoProceso(lineaEntrada, TipoEstado.ERROR_ELIMINAR, TipoMotivo.NO_APLICA, mensaje);
				continue;
			}

			if (lineaEntrada.CantidadColumnas > MAX_COLUMNAS) {
				mensaje = "El registro supera la cantidad máxima de valores. ";
				agregarEstadoProceso(lineaEntrada, TipoEstado.ERROR_ELIMINAR, TipoMotivo.NO_APLICA, mensaje);
				continue;
			}

			// id_solicitud
			String idSolicitud = lineaEntrada.IdSolicitud;

			if (idSolicitud.isEmpty()) {
				mensaje = "El campo id_solicitud (en la posición 0) es obligatorio. ";
				agregarEstadoProceso(lineaEntrada, TipoEstado.ERROR_ELIMINAR, TipoMotivo.NO_APLICA, mensaje);
				continue;
			} else if (!esNumeroEntero(idSolicitud)) {
				mensaje = "El campo id_solicitud (en la posición 0) debe ser un número entero. ";
				agregarEstadoProceso(lineaEntrada, TipoEstado.ERROR_ELIMINAR, TipoMotivo.NO_APLICA, mensaje);
				continue;
			} else if (conteoIdSolicitud.getOrDefault(idSolicitud, 0) > 1) {
				mensaje = "El campo id_solicitud (en la posición 0) está repetido en otra línea. ";
				agregarEstadoProceso(lineaEntrada, TipoEstado.ERROR_ELIMINAR, TipoMotivo.NO_APLICA, mensaje);
				continue;
			}

			// rut_titular
			String rutTitular = lineaEntrada.RutTitular;

			if (rutTitular.isEmpty()) {
				mensaje = "El campo rut_titular (en la posición 1) es obligatorio. ";
				agregarEstadoProceso(lineaEntrada, TipoEstado.ERROR_ELIMINAR, TipoMotivo.NO_APLICA, mensaje);
				continue;
			} else if (!validarRut(rutTitular)) {
				mensaje = "El campo rut_titular (en la posición 1) no es valido o no tiene el formato correcto. ";
				agregarEstadoProceso(lineaEntrada, TipoEstado.ERROR_ELIMINAR, TipoMotivo.NO_APLICA, mensaje);
				continue;
			//} else if (conteoDocumentoIdentidad.getOrDefault(rutTitular, 0) > 1) {
			//	mensaje = "El campo rut_titular (en la posición 1) está repetido en otra línea. ";
			//	agregarEstadoProceso(lineaEntrada, TipoEstado.ERROR_ELIMINAR, TipoMotivo.NO_APLICA, mensaje);
			//	continue;
			} else if (!documentosExistentes.contains(rutTitular)) {
				mensaje = "El documento de identidad informado en el campo rut_titular (en la posición 1) no se encuentra registrado en el sistema. ";
				agregarEstadoProceso(lineaEntrada, TipoEstado.RUT_NO_ENCONTRADO, TipoMotivo.NO_APLICA, mensaje);
				continue;
			}

			// tipo_borrado
			String tipoBorrado = lineaEntrada.TipoBorrado;

			if (tipoBorrado.isEmpty()) {
				mensaje = "El campo tipo_borrado (en la posición 2) es obligatorio. ";
				agregarEstadoProceso(lineaEntrada, TipoEstado.ERROR_ELIMINAR, TipoMotivo.NO_APLICA, mensaje);
				continue;
			} else if (!TipoBorrado.contieneCodigo(tipoBorrado)) {
				mensaje = "El tipo_borrado '" + tipoBorrado + "' (en la posición 2) no es válido. ";
				agregarEstadoProceso(lineaEntrada, TipoEstado.ERROR_ELIMINAR, TipoMotivo.NO_APLICA, mensaje);
				continue;
			}

			// nombre_dato
			String nombreDato = lineaEntrada.NombreDato;

			if (TipoBorrado.SUPRESION_DATO.Codigo.equals(tipoBorrado)) {
				if (nombreDato == null || nombreDato.isEmpty()) {
					mensaje = "El campo nombre_dato (en la posición 3) viene vacío y es obligatorio para tipo_borrado 11. ";
					agregarEstadoProceso(lineaEntrada, TipoEstado.ERROR_ELIMINAR, TipoMotivo.NO_APLICA, mensaje);
					continue;
				}else {
					List<ConfiguracionTipoBorrado> configuracionTipoBorrado = configuracionPorCodigoTipoBorrado.getOrDefault(TipoBorrado.SUPRESION_DATO.Codigo, Collections.emptyList());
					
					boolean campoNoExiste = configuracionTipoBorrado.stream().noneMatch(c -> c.CodigoDato.equals(nombreDato));
					
					if(campoNoExiste) {
						mensaje = "El campo nombre_dato (en la posición 3) tiene un codigo no valido para tipo_borrado 11. ";
						agregarEstadoProceso(lineaEntrada, TipoEstado.ERROR_ELIMINAR, TipoMotivo.NO_APLICA, mensaje);
						continue;
					}
					
				}
			} else if (TipoBorrado.PROSPECTO.Codigo.equals(tipoBorrado) || TipoBorrado.CONSENTIMIENTO.Codigo.equals(tipoBorrado) || TipoBorrado.DEFINITIVA.Codigo.equals(tipoBorrado)) {
				if (nombreDato != null && !nombreDato.isEmpty()) {
					mensaje = "El campo nombre_dato (en la posición 3) no aplica para el tipo_borrado " + tipoBorrado + ". ";
					agregarEstadoProceso(lineaEntrada, TipoEstado.ERROR_ELIMINAR, TipoMotivo.NO_APLICA, mensaje);
					continue;
				}
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::ValidadorPurgaProspectoCliente.validarLineasEntrada {} ms", fin - inicio);
	}
	
	private void agregarEstadoProceso(LineaEntrada lineaEntrada, TipoEstado tipoEstado, TipoMotivo tipoMotivo, String mensaje) {
		lineaEntrada.EstadoProceso = new EstadoProceso(tipoEstado, tipoMotivo, mensaje);
	}

	private void validarClienteActivo(Connection con, Long idEmpresa, List<LineaEntrada> lineas, List<Persona> personas, boolean borradoDefinitivo) throws Exception {
		logger.info("INICIO::ValidadorPurgaProspectoCliente.validarClienteActivo");
		long inicio = System.currentTimeMillis();
		
		List<String> documentosIdentidad = lineas.stream().filter(LineaEntrada::esProcesable).map(l -> l.RutTitular).distinct().collect(Collectors.toList());

		if (documentosIdentidad.isEmpty()) {
			return;
		}

		Long diasBorradoDefinitivo = ConfiguracionSistema.getParametroConfiguracionLong(con, idEmpresa, "SOCIODENEGOCIO", "PER.DiasBorradoDefinitivo");
		String fechaSistema = cl.bcs.utils.Fecha.toFormat(FechaDB.ObtenerFechaHoraTs(con, idEmpresa), "yyyyMMddHHmmssSSS");

		SQL sql = new SQL();

		HashMap<String, Object> params = new HashMap<>();

		params.put("FechaSistema", fechaSistema);
		params.put("diasBorradoDefinitivo", diasBorradoDefinitivo);

		List<Persona> documentosClientesActivos = new ArrayList<>();

		List<String> porValidar = documentosIdentidad;

		if (!porValidar.isEmpty() && borradoDefinitivo) {
			params.put("DocumentosIdentidad", porValidar);

			List<Persona> resultado = sql.query(con, SQL_OBTENER_TIEMPOMINIMO_3650DIAS, params, Persona.class);
			acumularYReducir(documentosClientesActivos, porValidar, resultado);
		}

		// Para borrado no definitivo (PROSPECTO/CONSENTIMIENTO/SUPRESION_DATO), las consultas de
		// actividad usan la antigüedad real de la persona (fecha de ingreso) en vez de la
		// ventana fija de diasBorradoDefinitivo, que solo aplica para DEFINITIVA.
		params.put("esBorradoDefinitivo", borradoDefinitivo ? 1L : 0L);

		if (!porValidar.isEmpty()) {
			params.put("DocumentosIdentidad", porValidar);

			List<Persona> resultado = sql.query(con, SQL_OBTENER_ACTUALIZACIONESPER, params,Persona.class);
			acumularYReducir(documentosClientesActivos, porValidar, resultado);
		}

		if (!porValidar.isEmpty()) {
			params.put("DocumentosIdentidad", porValidar);

			List<Persona> resultado = sql.query(con, SQL_OBTENER_MOVSCTAINVERSION, params, Persona.class);
			acumularYReducir(documentosClientesActivos, porValidar, resultado);
		}

		if (!porValidar.isEmpty()) {

			params.put("DocumentosIdentidad", porValidar);

			List<Persona> resultado = sql.query(con, SQL_OBTENER_OPERACIONESRUEDA, params, Persona.class);
			acumularYReducir(documentosClientesActivos, porValidar, resultado);
		}

		if (!porValidar.isEmpty()) {

			params.put("DocumentosIdentidad", porValidar);

			List<Persona> resultado = sql.query(con, SQL_OBTENER_FWDSPT, params, Persona.class);
			acumularYReducir(documentosClientesActivos, porValidar, resultado);
		}

		if (!porValidar.isEmpty()) {
			params.put("DocumentosIdentidad", porValidar);

			List<Persona> resultado = sql.query(con, SQL_OBTENER_CUSMOVTRANSITO, params, Persona.class);
			acumularYReducir(documentosClientesActivos, porValidar, resultado);
		}

		if (!porValidar.isEmpty()) {
			params.put("DocumentosIdentidad", porValidar);

			List<Persona> resultado = sql.query(con, SQL_OBTENER_OPERACIONESTES, params, Persona.class);
			acumularYReducir(documentosClientesActivos, porValidar, resultado);
		}

		if (!porValidar.isEmpty()) {
			params.put("DocumentosIdentidad", porValidar);
			
			List<Persona> resultado = sql.query(con, SQL_OBTENER_OPERACIONESFAC, params, Persona.class);
			acumularYReducir(documentosClientesActivos, porValidar, resultado);
		}

		if (!porValidar.isEmpty()) {
			params.put("DocumentosIdentidad", porValidar);

			List<Persona> resultado = sql.query(con, SQL_OBTENER_CUSCARTERA, params, Persona.class);
			acumularYReducir(documentosClientesActivos, porValidar, resultado);
		}

		if (!porValidar.isEmpty()) {
			params.put("DocumentosIdentidad", porValidar);

			List<Persona> resultado = sql.query(con, SQL_OBTENER_HISCARTERA, params, Persona.class);
			acumularYReducir(documentosClientesActivos, porValidar, resultado);
		}
			
		if (!porValidar.isEmpty()) {
			params.put("DocumentosIdentidad", porValidar);

			List<Persona> resultado = sql.query(con, SQL_OBTENER_HISCARTERA, params, Persona.class);
			acumularYReducir(documentosClientesActivos, porValidar, resultado);
		}
		
		if (!porValidar.isEmpty()) {
		    Set<String> rutsSebra = personas.stream()
		        .filter(p -> "S".equals(p.EsClienteSebra))
		        .map(p -> p.RutTitular)
		        .collect(Collectors.toSet());

		    List<String> paraValidarSebra = porValidar.stream()
		        .filter(rutsSebra::contains)
		        .collect(Collectors.toList());

		    if (!paraValidarSebra.isEmpty()) {
		        List<Persona> resultado = validarMovimientosPCB(con, idEmpresa, paraValidarSebra, fechaSistema, diasBorradoDefinitivo);
		        acumularYReducir(documentosClientesActivos, porValidar, resultado);
		    }
		}
		
		Set<String> documentosNoCumplen = documentosClientesActivos.stream().map(p -> p.RutTitular).collect(Collectors.toSet());

		for (LineaEntrada linea : lineas) {
			
		    if (!linea.esProcesable()) {
		        continue;
		    }
		    
			if (documentosNoCumplen.contains(linea.RutTitular)) {
				agregarEstadoProceso(linea, TipoEstado.NO_ELIMINADO, TipoMotivo.CLIENTE_ACTIVO, "El documento informado corresponde a un cliente activo");
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::ValidadorPurgaProspectoCliente.validarClienteActivo {} ms", fin - inicio);
	}

	private void validarPersonaRelacionada(Connection con, List<LineaEntrada> lineas) throws Exception {
		logger.info("INICIO::ValidadorPurgaProspectoCliente.validarPersonaRelacionada");
		long inicio = System.currentTimeMillis();
		
		List<String> documentos = lineas.stream().filter(LineaEntrada::esProcesable).map(l -> l.RutTitular).distinct().collect(Collectors.toList());

		if (documentos.isEmpty()) {
			return;
		}

		SQL sql = new SQL();

		HashMap<String, Object> params = new HashMap<>();

		params.put("DocumentosIdentidad", documentos);

		List<Persona> relaciones = sql.query(con, SQL_OBTENER_RELACIONES_PERSONA, params, Persona.class);
		Set<String> relacionados = relaciones.stream().map(p -> p.RutTitular).collect(Collectors.toSet());

		for (LineaEntrada linea : lineas) {

		    if (!linea.esProcesable()) {
		        continue;
		    }

			if (relacionados.contains(linea.RutTitular)) {
				agregarEstadoProceso(linea, TipoEstado.NO_ELIMINADO, TipoMotivo.PERSONA_RELACIONADA, "Persona relacionada con otro cliente");
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::ValidadorPurgaProspectoCliente.validarPersonaRelacionada {} ms", fin - inicio);
	}

	private void validarPersonaFallecida(Connection con, List<LineaEntrada> lineas) throws Exception {
		logger.info("INICIO::ValidadorPurgaProspectoCliente.validarPersonaFallecida");
		long inicio = System.currentTimeMillis();
		
		List<String> documentos = lineas.stream().filter(LineaEntrada::esProcesable).map(l -> l.RutTitular).distinct().collect(Collectors.toList());

		if (documentos.isEmpty()) {
			return;
		}

		SQL sql = new SQL();

		HashMap<String, Object> params = new HashMap<>();

		params.put("DocumentosIdentidad", documentos);

		List<Persona> fallecidos = sql.query(con, SQL_OBTENER_PERSONA_FALLECIDA, params, Persona.class);
		Set<String> documentosFallecidos = fallecidos.stream().map(p -> p.RutTitular).collect(Collectors.toSet());

		for (LineaEntrada linea : lineas) {

		    if (!linea.esProcesable()) {
		        continue;
		    }

			if (documentosFallecidos.contains(linea.RutTitular)) {
				agregarEstadoProceso(linea, TipoEstado.NO_ELIMINADO, TipoMotivo.PERSONA_FALLECIDA, "Persona fallecida");
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::ValidadorPurgaProspectoCliente.validarPersonaFallecida {} ms", fin - inicio);
	}

	private void validar90Dias(Connection con, Long idEmpresa, List<LineaEntrada> lineas) throws Exception {
		logger.info("INICIO::ValidadorPurgaProspectoCliente.validar90Dias");
		long inicio = System.currentTimeMillis();
		
		List<String> documentos = lineas.stream().filter(LineaEntrada::esProcesable).map(l -> l.RutTitular).distinct().collect(Collectors.toList());

		if (documentos.isEmpty()) {
			return;
		}

		SQL sql = new SQL();

		Long dias = ConfiguracionSistema.getParametroConfiguracionLong(con, 1L, "SOCIODENEGOCIO", "PER.DiasBorradoProspectos");

		HashMap<String, Object> params = new HashMap<>(3);
		params.put("DocumentosIdentidad", documentos);
		params.put("fechaSistema", FechaDB.ObtenerFechaHoraTs(con, idEmpresa));
		params.put("diasBorradoProspectoConsentimiento", dias);

		List<Persona> recientes = sql.query(con, SQL_OBTENER_TIEMPOMINIMO_90DIAS, params, Persona.class);
		Set<String> documentosRecientes = recientes.stream().map(p -> p.RutTitular).collect(Collectors.toSet());

		for (LineaEntrada linea : lineas) {

		    if (!linea.esProcesable()) {
		        continue;
		    }

			if (documentosRecientes.contains(linea.RutTitular)) {
				agregarEstadoProceso(linea, TipoEstado.NO_ELIMINADO, TipoMotivo.TIEMPO_MINIMO_MENOR_90_DIAS, "No cumple antigüedad mínima de " + dias + " días");
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::ValidadorPurgaProspectoCliente.validar90Dias {} ms", fin - inicio);
	}
	
	private List<Persona> validarMovimientosPCB(Connection con,Long idEmpresa, List<String> documentos, String fechaSistema, Long diasBorradoDefinitivo) throws Exception {
	    logger.info("INICIO::ValidadorPurgaProspectoCliente.validarMovimientosPCB");
	    long inicio = System.currentTimeMillis();
	
	    if (documentos.isEmpty()) {
	        return new ArrayList<>();
	    }
	
	    Properties prop = new Properties();
	    String DBdriver, DBuser, DBpass, DBurl, DBprop, DBKey;
	    InputStream input;
        String rutaArchivoProperties = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.RutaArchivosConfiguracion");        	
	    try {
            input = new FileInputStream(rutaArchivoProperties+"optimus.properties");
	    	prop.load(input);
	        DBdriver = prop.getProperty("DBdriverPCB");
	        DBuser   = prop.getProperty("DBuserPCB");
	        DBpass   = prop.getProperty("DBpassPCB");
	        DBurl    = prop.getProperty("DBurlPCB");
	        DBprop   = prop.getProperty("DBpropPCB");
	        DBKey    = prop.getProperty("DBKeyPCB");
	        input.close();
	    } catch (IOException e) {
	        throw new InterruptedException("ERROR AL LEER "+rutaArchivoProperties+"optimus.properties" + e.getMessage());
	    }

	    SQL _sql = new SQL();

	    BasicDataSource dsTarget = new BasicDataSource();
	    try {
	        dsTarget.setDriverClassName(DBdriver);
	        dsTarget.setUsername(OpenEJBEncryption.decrypt(DBuser, DBKey));
	        dsTarget.setPassword(OpenEJBEncryption.decrypt(DBpass, DBKey));
	        dsTarget.setUrl(DBurl);
	        dsTarget.setConnectionProperties(DBprop);
	        dsTarget.setDefaultAutoCommit(true);
	    } catch (Exception e) {
	        throw new InterruptedException("ERROR NO SE PUEDE OBTENER CONEXION SEBRA DB");
	    }
	
	    HashMap<String, Object> params = new HashMap<>();
	    params.put("listaRuts", documentos);
	    params.put("fechaSistema", fechaSistema);
	    params.put("diasBorradoDefinitivo", diasBorradoDefinitivo);

	    List<Persona> movsPCB;
	    try (Connection conTarget = dsTarget.getConnection()) {
	        movsPCB = _sql.query(conTarget, SQL_OBTENER_MOVIMIENTOSSEBRA, params, Persona.class);
	    } finally {
	        dsTarget.close();
	    }
	
	    long fin = System.currentTimeMillis();
	    logger.info("FIN::ValidadorPurgaProspectoCliente.validarMovimientosPCB {} ms", fin - inicio);
	    return movsPCB;
	}	
	
	private void acumularYReducir(List<Persona> acumulador, List<String> porValidar, List<Persona> fallidos) {
		if (fallidos == null || fallidos.isEmpty()) {
			return;
		}

		acumulador.addAll(fallidos);
		Set<String> documentosFallidos = fallidos.stream().map(p -> p.RutTitular).collect(Collectors.toSet());

		porValidar.removeAll(documentosFallidos);
	}

	private boolean esNumeroEntero(String valor) {
	    try {
	        Long.parseLong(valor);
	        return true;
	    } catch (Exception e) {
	        return false;
	    }
	}
	
	public boolean validarRut(String rut) {
	    if (rut == null || rut.isEmpty()) {
	        return false;
	    }
	    
	    if (!rut.matches("^\\d+-[0-9kK]{1}$")) {
	        return false;
	    }

	    String[] partes = rut.split("-");

	    String numeroRut = partes[0];
	    String dvIngresado = partes[1].toUpperCase();

	    int suma = 0;
	    int multiplicador = 2;

	    for (int i = numeroRut.length() - 1; i >= 0; i--) {
	        suma += Character.getNumericValue(numeroRut.charAt(i)) * multiplicador;
	        multiplicador++;
	        if (multiplicador > 7) {
	            multiplicador = 2;
	        }
	    }

	    int dv = 11 - (suma % 11);
	    String dvCalculado;

	    switch (dv) {
	        case 11:
	            dvCalculado = "0";
	            break;
	        case 10:
	            dvCalculado = "K";
	            break;
	        default:
	            dvCalculado = String.valueOf(dv);
	    }
	    return dvCalculado.equals(dvIngresado);
	}

}
