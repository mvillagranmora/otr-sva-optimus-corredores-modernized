package cl.bcs.normativa;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class GenerarNCG380 implements JavaCall{

	private static final Logger log = LogManager.getLogger(GenerarNCG380.class);

    private String SQL_CONSULTA_PARAMETROS = loadResourceToString("cl/bcs/normativa/sql/GenerarNCG380_SelectParametros.sql");
    private String SQL_CONSULTA_PARAMETROS_SOLICITUD = loadResourceToString("cl/bcs/normativa/sql/GenerarNCG380_SelectParametrosSolicitud.sql");
    private String SQL_CONSULTA_USUARIO = loadResourceToString("cl/bcs/normativa/sql/GenerarNCG380_SelectUsuario.sql");
    private String SQL_CONSULTA_EMPRESA = loadResourceToString("cl/bcs/normativa/sql/GenerarNCG380_SelectEmpresa.sql");
    private String SQL_CONSULTA_MERCADOS = loadResourceToString("cl/bcs/normativa/sql/GenerarNCG380_SelectMercados.sql");
    private String SQL_INSERT_TEMP = loadResourceToString("cl/bcs/normativa/sql/GenerarNCG380_InsertTablaTemporal.sql");
    
    private String SQL_CONSULTA_NEMO = loadResourceToString("cl/bcs/normativa/sql/GenerarNCG380_SelectNemo.sql");
    private String SQL_CONSULTA_REL_NEMO_MERCADO = loadResourceToString("cl/bcs/normativa/sql/GenerarNCG380_SelectNemoMercado.sql");
    private String SQL_CONSULTA_RUT = loadResourceToString("cl/bcs/normativa/sql/GenerarNCG380_SelectRutCliente.sql");
    private String SQL_CONSULTA_REL = loadResourceToString("cl/bcs/normativa/sql/GenerarNCG380_SelectTipoRelacion.sql");
    private String SQL_CONSULTA_DIF_HORA = loadResourceToString("cl/bcs/normativa/sql/GenerarNCG380_SelectDiferenciaHoraria.sql");
    
    public GenerarNCG380_Salida logica(Connection conn, GenerarNCG380_Entrada in) throws Exception {
    	GenerarNCG380_Salida out = new GenerarNCG380_Salida();
		log.info("INICIO");
		
		// Filtros por documento basados en lo que están en mantenedor Circulares
		HashMap<String, String> filtros = new HashMap<>();
		filtros.put("NCG380ASIG", "FINI;FTER;CLAINS;NEMO");
		filtros.put("NCG380OPE", "FINI;FTER;CLAINS;NEMO");
		filtros.put("NCG380ORD", "FINI;FTER;OFI;RUT;CLAINS;NEMO");
		
		SQL _sql = new SQL();
		List<ParametroNCG380> _parametrosNCG380 = _sql.query(conn, SQL_CONSULTA_PARAMETROS, new HashMap<>(), ParametroNCG380.class);
		if (_parametrosNCG380.isEmpty()) {
			log.info("FIN");	
			return out;
		}
		
		List<Usuario> _usuario = _sql.query(conn, SQL_CONSULTA_USUARIO, new HashMap<>(), Usuario.class);
		if (_usuario.isEmpty()) {
			log.info("No se logra obtener IdUsuario");
			out.P_OUT_ERRORESCONTADOR += 1;
			return out;
		}
		
		List<Empresa> _empresa = _sql.query(conn, SQL_CONSULTA_EMPRESA, new HashMap<>(), Empresa.class);
		if (_empresa.isEmpty()) {
			log.info("No se logra obtener IdEmpresa");
			out.P_OUT_ERRORESCONTADOR += 1;	
			return out;
		}
		
		// Parametros de Entrada
		String p_NumeroArchivo;
		String p_IdTipoDocumento;
	
		String p_IdModulo = "NOR";
		Long p_IdUsuario = _usuario.get(0).IdUsuario;		
		Long p_IdEmpresa = _empresa.get(0).IdEmpresa;
		String p_FechaInicio = "0001-01-01";		
		String p_FechaTermino = "9999-01-01";
		Long p_IdCliente = 0L;
		
		String fechaHora = in.p_FechaEjecucion;
		
		String[] lista = ObtenerListaDocumentos(_parametrosNCG380.get(0).Valor);
		
		// Obtener Fecha
		if(fechaHora.isEmpty()) {
			CallableStatement callFecha = null;
			String callFechaString = String.format("{CALL %s.%s(?,?)}", getQuotedString("BSCL"),
	                getQuotedString("BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"));
			try {
				callFecha = conn.prepareCall(callFechaString);
				callFecha.setLong(1,p_IdEmpresa);
				callFecha.registerOutParameter(2, java.sql.Types.NVARCHAR);
				callFecha.execute();
				log.info("Se hace llamada a procedimiento BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema");			
				fechaHora = callFecha.getNString(2);
			} catch (Exception e) {
				out.P_OUT_ERRORESCONTADOR += 1;
				log.info("Error al llamar a BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema");
				e.printStackTrace();
				return out;
			} finally {
	            try {
	                if (callFecha != null) callFecha.close();
	            } catch (SQLException e) {
					out.P_OUT_ERRORESCONTADOR += 1;	
	                e.printStackTrace();
	            }
	        }
		}
		
		for(int i=0;i<lista.length;i++) {
			if (lista[i].length()>0) {
				p_IdTipoDocumento = lista[i].toUpperCase();
				p_NumeroArchivo = Integer.toString(i+1);
				
				log.info("Generando solicitud para documento "+ p_IdTipoDocumento);
				
				if(!p_IdTipoDocumento.contains("NCG380")) {
					out.P_OUT_ERRORESCONTADOR += 1;
					log.info("ERROR: Documento "+ p_IdTipoDocumento + " no es parte de la normativa NCG380");
					continue;
				}
				
				log.info("Se consultan los parametros para realizar la solicitud a la circular");
				HashMap<String, Object> entradaConsultaParams = new HashMap<>();
				entradaConsultaParams.put("p_IdTipoDocumento", p_IdTipoDocumento);
				List<ParametroNCG380> _parametrosSolicitud = _sql.query(conn, SQL_CONSULTA_PARAMETROS_SOLICITUD, entradaConsultaParams, ParametroNCG380.class);
				String valorString = null;
				if (_parametrosSolicitud.isEmpty()) {
					log.info("No se han obtenido parametros para lo solicitud");
					out.P_OUT_ERRORESCONTADOR += 1;	
				}else {					
					valorString = _parametrosSolicitud.get(0).Valor;
				}
				
				if(valorString == null) {
					valorString = "";
					log.info("Error: Parametro PARAMS_SOL_PAUTA_" + p_IdTipoDocumento + " no existente o sin valor definido, se usarán valores por defecto");
				}
				
				HashMap<String, String> parametrosMap = procesarParametrosSolicitud(valorString, p_IdTipoDocumento);
				
				log.info("Comienzo de validación de filtros");
				
				HashMap<String, Object> entradaParams = new HashMap<>();
				entradaParams.put("p_FechaSistema", fechaHora.substring(0,10));
				List<DiferenciaHoraria> diferenciahoraria = _sql.query(conn, SQL_CONSULTA_DIF_HORA, entradaParams, DiferenciaHoraria.class);
				String fini = "";
				String ffin = "";
				if (diferenciahoraria.isEmpty()) {
					log.info("No se ha obtenido diferencia horaria para la solicitud");
					out.P_OUT_ERRORESCONTADOR += 1;	
				}else {					
					fini = diferenciahoraria.get(0).Fecha_Inicio;
					ffin = diferenciahoraria.get(0).Fecha_Fin;
				}

				
				// FREA reemplaza FINI y FTER
				if (filtros.get(p_IdTipoDocumento) != null && filtros.get(p_IdTipoDocumento).contains("FREA")) {					
					boolean fechaRealizacionValida = validarFecha(parametrosMap.get("FREA"), "FREA");
					if (parametrosMap.get("FREA") != null && fechaRealizacionValida) {
						parametrosMap.put("FINI", getFechaDesde(parametrosMap.get("FREA")));
						parametrosMap.put("FTER", getFechaHasta(parametrosMap.get("FREA")));
					} else if (parametrosMap.get("FREA") != null && !fechaRealizacionValida) {
						log.info("No se genera documento: " + p_IdTipoDocumento);
						continue;
					}
				}
				
				// Aplicación de valores por defecto para fechas
				if (parametrosMap.get("FINI") == null) {
					if (p_IdTipoDocumento.equals("NCG380OPE")) {
						parametrosMap.put("FINI", fechaHora.substring(0,10));
					}
					else if (p_IdTipoDocumento.equals("NCG380ORD") || p_IdTipoDocumento.equals("NCG380ASIG")) {
						parametrosMap.put("FINI", fini);
					}
					else {
						parametrosMap.put("FINI", getFechaDesde(fechaHora));
					}
				}
				if (parametrosMap.get("FTER") == null) {
					if (p_IdTipoDocumento.equals("NCG380OPE")) {
						parametrosMap.put("FTER", fechaHora.substring(0,10));
					}
					else if (p_IdTipoDocumento.equals("NCG380ORD") || p_IdTipoDocumento.equals("NCG380ASIG")) {
						parametrosMap.put("FTER", ffin);
					}
					else {
						parametrosMap.put("FTER", getFechaHasta(fechaHora));
					}
				}
				
				// Validación de fechas
				if (!validarFecha(parametrosMap.get("FINI"), "FINI")) {
					log.info("No se genera documento: " + p_IdTipoDocumento);
					out.P_OUT_ERRORESCONTADOR += 1;	
					continue;
				}else{
					if (p_IdTipoDocumento.equals("NCG380OPE")) {
						parametrosMap.put("FINI", fechaHora.substring(0,10));
					}
					else if (p_IdTipoDocumento.equals("NCG380ORD") || p_IdTipoDocumento.equals("NCG380ASIG")) {
						parametrosMap.put("FINI", fini);
					}
					else {
						parametrosMap.put("FINI", getFechaDesde(parametrosMap.get("FINI")));
					}
				};
				if (!validarFter(parametrosMap.get("FINI"), parametrosMap.get("FTER"))) {
					log.info("No se genera documento: " + p_IdTipoDocumento);
					out.P_OUT_ERRORESCONTADOR += 1;	
					continue;
				}else if (parametrosMap.get("FTER").length() == 10){ // Esta condicion verifica que no se haya aplicado aún método getFechaHasta() a FTER
					if (p_IdTipoDocumento.equals("NCG380OPE")) {
						parametrosMap.put("FTER", fechaHora.substring(0,10));
					}
					else if (p_IdTipoDocumento.equals("NCG380ORD") || p_IdTipoDocumento.equals("NCG380ASIG")) {
						parametrosMap.put("FTER", ffin);
					}
					else {
						parametrosMap.put("FTER", getFechaHasta(parametrosMap.get("FTER")));
					}
					
				};
				
				// Validación mercado
				boolean mercadoExiste = false;
				if (parametrosMap.get("CLAINS") != null) {
					HashMap<String, Object> mercadoMap = new HashMap<String, Object>();
					mercadoMap.put("IdClasificacionTipoInstrumento", parametrosMap.get("CLAINS"));
					List<Busqueda> mercado = _sql.query(conn, SQL_CONSULTA_MERCADOS, mercadoMap, Busqueda.class);
					if (!mercado.isEmpty() && "S".equals(mercado.get(0).Existe)) {
						mercadoExiste = true;
					}
					if(!mercadoExiste) {
						log.info("Error: Mercado " + parametrosMap.get("CLAINS") + " es inválido");
						out.P_OUT_ERRORESCONTADOR += 1;
					}
					if (!mercadoExiste && filtros.get(p_IdTipoDocumento) != null && filtros.get(p_IdTipoDocumento).contains("CLAINS")) {
						log.info("No se genera documento: " + p_IdTipoDocumento);
						continue;
					}
				}
				
				// Validación nemo
				boolean nemoExiste = false;
				if (parametrosMap.get("NEMO") != null) {
					HashMap<String, Object> nemoMap = new HashMap<String, Object>();
					nemoMap.put("Nemotecnico", parametrosMap.get("NEMO"));
					List<Busqueda> nemo = _sql.query(conn, SQL_CONSULTA_NEMO, nemoMap, Busqueda.class);
					if (!nemo.isEmpty() && "S".equals(nemo.get(0).Existe)) {
						nemoExiste = true;
					}
					if(!nemoExiste) {
						log.info("Error: Nemo " + parametrosMap.get("NEMO") + " es inválido");
						out.P_OUT_ERRORESCONTADOR += 1;
					}
					if (!nemoExiste && filtros.get(p_IdTipoDocumento) != null && filtros.get(p_IdTipoDocumento).contains("NEMO")) {
						log.info("No se genera documento: " + p_IdTipoDocumento);
						continue;
					}
				}
				
				// Validación relación mercado - nemo
				boolean nemoEnMercado = false;
				if (nemoExiste && mercadoExiste) {
					HashMap<String, Object> nemoMercadoMap = new HashMap<String, Object>();
					nemoMercadoMap.put("Nemotecnico", parametrosMap.get("NEMO"));
					nemoMercadoMap.put("IdClasificacionTipoInstrumento", parametrosMap.get("CLAINS"));
					List<Busqueda> nemoMercado = _sql.query(conn, SQL_CONSULTA_REL_NEMO_MERCADO, nemoMercadoMap, Busqueda.class);
					if (!nemoMercado.isEmpty() && "S".equals(nemoMercado.get(0).Existe)) {
						nemoEnMercado = true;
					}
					if(!nemoEnMercado) {
						log.info("Error: Nemo " + parametrosMap.get("NEMO") + " no es de tipo " + parametrosMap.get("CLAINS"));
						out.P_OUT_ERRORESCONTADOR += 1;
					}
					if (!nemoEnMercado && filtros.get(p_IdTipoDocumento) != null && filtros.get(p_IdTipoDocumento).contains("NEMO") && filtros.get(p_IdTipoDocumento).contains("CLAINS")) {
						log.info("No se genera documento: " + p_IdTipoDocumento);
						continue;
					}
				}
				
				// Validación rut
				boolean rutExiste = false;
				if (parametrosMap.get("RUT") != null) {
					HashMap<String, Object> rutMap = new HashMap<String, Object>();
					rutMap.put("DocumentoIdentidad", parametrosMap.get("RUT"));
					List<Persona> rut = _sql.query(conn, SQL_CONSULTA_RUT, rutMap, Persona.class);
					if (!rut.isEmpty() && rut.get(0).IdPersona != null) {
						rutExiste = true;
					}
					if(!rutExiste) {
						log.info("Error: Rut " + parametrosMap.get("RUT") + " en campo RUT no encontrado");
						out.P_OUT_ERRORESCONTADOR += 1;
					}
					if (!rutExiste && filtros.get(p_IdTipoDocumento) != null && filtros.get(p_IdTipoDocumento).contains("RUT")) {
						log.info("No se genera documento: " + p_IdTipoDocumento);
						continue;
					}
				}
				
				// Validación Tipo Relación
				boolean tipoRelExiste = false;
				if (parametrosMap.get("RELA") != null) {
					HashMap<String, Object> relMap = new HashMap<String, Object>();
					relMap.put("IdTipoRelacion", parametrosMap.get("RELA"));
					List<Busqueda> relacion = _sql.query(conn, SQL_CONSULTA_REL, relMap, Busqueda.class);
					if (!relacion.isEmpty() && "S".equals(relacion.get(0).Existe)) {
						tipoRelExiste = true;
					}
					if(!tipoRelExiste) {
						log.info("Error: Tipo de relación " + parametrosMap.get("RELA") + " no existe");
						out.P_OUT_ERRORESCONTADOR += 1;
					}
					if (!tipoRelExiste && filtros.get(p_IdTipoDocumento) != null && filtros.get(p_IdTipoDocumento).contains("RELA")) {
						log.info("No se genera documento: " + p_IdTipoDocumento);
						continue;
					}
				}
				
				// Validación Empleado
				boolean empExiste = false;
				if (parametrosMap.get("RUTEMPL") != null) {
					HashMap<String, Object> empMap = new HashMap<String, Object>();
					empMap.put("DocumentoIdentidad", parametrosMap.get("RUTEMPL"));
					List<Persona> emp = _sql.query(conn, SQL_CONSULTA_RUT, empMap, Persona.class);
					if (!emp.isEmpty() && emp.get(0).IdPersona != null) {
						empExiste = true;
					}
					if(!empExiste) {
						log.info("Error: Rut " + parametrosMap.get("RUTEMPL") + " en campo RUTEMPL no encontrado");
						out.P_OUT_ERRORESCONTADOR += 1;
					}
					if (!empExiste && filtros.get(p_IdTipoDocumento) != null && filtros.get(p_IdTipoDocumento).contains("RUTEMPL")) {
						log.info("No se genera documento: " + p_IdTipoDocumento);
						continue;
					}
				}
				
				// Validación Autorizado
				boolean autExiste = false;
				if (parametrosMap.get("IDAUTORIZADO") != null) {
					HashMap<String, Object> autMap = new HashMap<String, Object>();
					autMap.put("DocumentoIdentidad", parametrosMap.get("IDAUTORIZADO"));
					List<Persona> aut = _sql.query(conn, SQL_CONSULTA_RUT, autMap, Persona.class);
					if (!aut.isEmpty() && aut.get(0).IdPersona != null) {
						autExiste = true;
						parametrosMap.put("IDAUTORIZADO", aut.get(0).IdPersona.toString());
					}
					if(!autExiste) {
						log.info("Error: Rut " + parametrosMap.get("IDAUTORIZADO") + " en campo IDAUTORIZADO no encontrado");
						out.P_OUT_ERRORESCONTADOR += 1;
					}
					if (!autExiste && filtros.get(p_IdTipoDocumento) != null && filtros.get(p_IdTipoDocumento).contains("IDAUTORIZADO")) {
						log.info("No se genera documento: " + p_IdTipoDocumento);
						continue;
					}
				}
				
				// Tabla de parametros de solicitud
				// Parametros BATCH
				List<ParametroSolicitud> parametrosSolicitud = new ArrayList<>();
				parametrosSolicitud.add(new ParametroSolicitud("BATCH", "idUsuario", p_IdUsuario.toString()));
				parametrosSolicitud.add(new ParametroSolicitud("BATCH", "schema", "BSCL"));
				parametrosSolicitud.add(new ParametroSolicitud("BATCH", "grabar", "false"));
				parametrosSolicitud.add(new ParametroSolicitud("BATCH", "reemplazar", "false"));
				parametrosSolicitud.add(new ParametroSolicitud("BATCH", "separadorListas", ";"));
				parametrosSolicitud.add(new ParametroSolicitud("BATCH", "simboloDecimal", ","));
				parametrosSolicitud.add(new ParametroSolicitud("BATCH", "simboloFecha", "dd/MM/yyyy"));
				parametrosSolicitud.add(new ParametroSolicitud("BATCH", "aceptarFechasNull", "true"));
				
				// Parametros de solicitud (recibidos de parametro de configuracion)
				
				parametrosSolicitud.add(new ParametroSolicitud("SOLICITUD", "DESCCOL", "TIT"));
				if (filtros.get(p_IdTipoDocumento) != null && filtros.get(p_IdTipoDocumento).contains("FINI")) {
					parametrosSolicitud.add(new ParametroSolicitud("SOLICITUD", "FINI", parametrosMap.get("FINI")));
				}
				if (filtros.get(p_IdTipoDocumento) != null && filtros.get(p_IdTipoDocumento).contains("FTER")) {
					parametrosSolicitud.add(new ParametroSolicitud("SOLICITUD", "FTER", parametrosMap.get("FTER")));
				}
				if (filtros.get(p_IdTipoDocumento) != null && filtros.get(p_IdTipoDocumento).contains("CLAINS")) {
					if (parametrosMap.get("CLAINS") != null) {						
						parametrosSolicitud.add(new ParametroSolicitud("SOLICITUD", "CLAINS", parametrosMap.get("CLAINS")));
					}else if (parametrosMap.get("CLAINS") == null && (p_IdTipoDocumento.equals("NCG380ASIG" )|| p_IdTipoDocumento.equals("NCG380ORD"))) {
						parametrosSolicitud.add(new ParametroSolicitud("SOLICITUD", "CLAINS", null));
					}
				}
				if (filtros.get(p_IdTipoDocumento) != null && filtros.get(p_IdTipoDocumento).contains("NEMO")) {
					if (parametrosMap.get("NEMO") != null) {						
						parametrosSolicitud.add(new ParametroSolicitud("SOLICITUD", "NEMO", parametrosMap.get("NEMO")));
					}else if (parametrosMap.get("NEMO") == null && (p_IdTipoDocumento.equals("NCG380ASIG" )|| p_IdTipoDocumento.equals("NCG380ORD"))) {
						parametrosSolicitud.add(new ParametroSolicitud("SOLICITUD", "NEMO", null));
					}
				}
				if (filtros.get(p_IdTipoDocumento) != null && filtros.get(p_IdTipoDocumento).contains("RUT")) {
					if (parametrosMap.get("RUT") != null) {						
						parametrosSolicitud.add(new ParametroSolicitud("SOLICITUD", "RUT", parametrosMap.get("RUT")));
					}else if (parametrosMap.get("RUT") == null && (p_IdTipoDocumento.equals("NCG380ASIG")|| p_IdTipoDocumento.equals("NCG380ORD"))) {
						parametrosSolicitud.add(new ParametroSolicitud("SOLICITUD", "RUT", null));
					}
				}else if (p_IdTipoDocumento.equals("NCG380ASIG")) {
					parametrosSolicitud.add(new ParametroSolicitud("SOLICITUD", "RUT", null));
				}
				if (filtros.get(p_IdTipoDocumento) != null && filtros.get(p_IdTipoDocumento).contains("OFI")) {
					parametrosSolicitud.add(new ParametroSolicitud("SOLICITUD", "OFI", parametrosMap.get("OFI")));
					if (parametrosMap.get("OFI") != null) {
						String nombreConOficio = p_IdTipoDocumento + fechaHora.substring(0,4) + "_" + parametrosMap.get("OFI");
						parametrosSolicitud.add(new ParametroSolicitud("SOLICITUD", "NOMSAL", nombreConOficio));
						parametrosSolicitud.add(new ParametroSolicitud("SOLICITUD", "EXTSAL", "csv"));
					}
				}

				if (p_IdTipoDocumento.equals("NCG380OPE")) {
					parametrosSolicitud.add(new ParametroSolicitud("SOLICITUD", "TODOS_CLI", "S"));
				}

				// Parametros de solicitud desde pauta
				parametrosSolicitud.add(new ParametroSolicitud("SOLICITUD", "EsPauta", "S"));
				parametrosSolicitud.add(new ParametroSolicitud("SOLICITUD", "PAUTA_NCG380_NUM", p_NumeroArchivo));
				
				if (parametrosMap.get("OFI") == null || (filtros.get(p_IdTipoDocumento) != null && !filtros.get(p_IdTipoDocumento).contains("OFI"))) {					
					parametrosSolicitud.add(new ParametroSolicitud("SOLICITUD", "PAUTA_NCG380", "S"));
				}
				
				// Crear tabla temporal
				PreparedStatement  pstmtCreate = conn.prepareStatement("CREATE LOCAL TEMPORARY TABLE \"#ParamSolicitudNcg380Pauta\" (\"Tipo\" NVARCHAR(40), \"NombreParametro\" NVARCHAR(100), \"Valor\" NVARCHAR(100))");
	            pstmtCreate.executeUpdate();
	            log.info("Se crea tabla temporal #ParamSolicitudNcg380Pauta");
	            
	            // Inserta en tabla temporal
	            _sql.batch(conn, SQL_INSERT_TEMP, parametrosSolicitud, ParametroSolicitud.class);
	            log.info("Se insertan parámetros en tabla #ParamSolicitudNcg380Pauta");
	            
				// Llamada para crear solicitud de circular
				String nombreTabla = String.format("%s.%s", getQuotedString("BSCL"), getQuotedString("#ParamSolicitudNcg380Pauta"));
				CallableStatement callCrearSolicitud = null;
				String callCrearSolicitudString = String.format("{CALL %s.%s(?,?,?,?,?,?,?,%s,?,?,?,?,?)}", getQuotedString("BSCL"),
		                getQuotedString("BSCL.Normativa.BD.Servicios::NOR.CrearSolicitudCircular"), nombreTabla);

				ResultSet resultSet = null;
				try {
					callCrearSolicitud = conn.prepareCall(callCrearSolicitudString);
					callCrearSolicitud.setNString(1, p_IdModulo);
					callCrearSolicitud.setLong(2, p_IdUsuario);
					callCrearSolicitud.setLong(3, p_IdEmpresa);
					callCrearSolicitud.setNString(4, p_IdTipoDocumento);
					callCrearSolicitud.setNString(5, p_FechaInicio);
					callCrearSolicitud.setNString(6, p_FechaTermino);
					callCrearSolicitud.setLong(7, p_IdCliente);
					
					boolean hasResults = callCrearSolicitud.execute();
					log.info("Se hace llamada a procedimiento BSCL.Normativa.BD.Servicios::NOR.CrearSolicitudCircular");
					
		            do {
		                if (hasResults) {
	                    	resultSet = callCrearSolicitud.getResultSet();
	                    	procesarResultado(resultSet);
		                }
		                hasResults = callCrearSolicitud.getMoreResults();
		            } while (hasResults || callCrearSolicitud.getUpdateCount() != -1);

				} catch (Exception e) {
					out.P_OUT_ERRORESCONTADOR += 1;
					log.info("Error al llamar a BSCL.Normativa.BD.Servicios::NOR.CrearSolicitudCircular");
					e.printStackTrace();
				} finally {
		            try {
		            	// Borra tabla temporal
						PreparedStatement  pstmtDrop = conn.prepareStatement("DROP TABLE \"#ParamSolicitudNcg380Pauta\"");
			            pstmtDrop.executeUpdate();
			            log.info("Se elimina tabla temporal #ParamSolicitudNcg380Pauta");
		                if (callCrearSolicitud != null) callCrearSolicitud.close();
		            } catch (SQLException e) {
						out.P_OUT_ERRORESCONTADOR += 1;	
		                e.printStackTrace();
		            }
		        }
			}
		}

		log.info("FIN");
		return out;
    }
	
    private static String getQuotedString(final String name) {
        return "\"" + name + "\"";
    }
    
    public static String[] ObtenerListaDocumentos(String param){
    	String[] lista = param.split(",",0);
    	return lista;
    }
    
	public static class ParametroNCG380 {
		public String Valor;
	}
	
	public static class Usuario {
		public Long IdUsuario;
	}
	
	public static class Busqueda {
		public String Existe;
	}
	
	public static class Persona {
		public Long IdPersona;
	}
	
	public static class Empresa {
		public Long IdEmpresa;
	}
	
	public static class DiferenciaHoraria {
		public String Fecha_Inicio;
		public String Fecha_Fin;
	}
	
	public static class ParametroSolicitud{
		public String Tipo;
		public String NombreParametro;
		public String Valor;
		
		public ParametroSolicitud(String Tipo, String NombreParametro, String Valor) {
			this.Tipo = Tipo;
			this.NombreParametro = NombreParametro;
			this.Valor = Valor;
		}
	}
	
	public static class GenerarNCG380_Entrada{
		public Long 		p_IdEmpresa;
		public String		p_FechaEjecucion = "";
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class GenerarNCG380_Salida{
		public Long 	P_OUT_ERRORESCONTADOR;
		public GenerarNCG380_Salida(){
			this.P_OUT_ERRORESCONTADOR = 0L;
		}
	}
    
	private static String getFechaDesde(String fecha) {
		fecha = fecha.substring(0,10);
		return fecha + " 03:00:00";
	}
	
	private static String getFechaHasta(String fecha) {
		fecha = fecha.substring(0,10);
		LocalDate date = LocalDate.parse(fecha).plusDays(1);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		fecha = date.format(formatter);
		String fechaHasta = fecha + " 02:59:00";
		return fechaHasta;
	}
	
	private static boolean validarFecha(String fechaStr, String campo) {
		if (fechaStr == null) return false;
		boolean res = true;
		fechaStr = (fechaStr != null && fechaStr.length() >= 10)? fechaStr.substring(0,10): fechaStr;
		try {
			LocalDate.parse(fechaStr);
		} catch (Exception e) {
			log.info("Error: Fecha "+campo +" inválida");
			e.printStackTrace();
			res = false;
		}
		return res;
	};
	
	private static boolean validarFter(String fini, String fter) {
		boolean res = true;
		String entradaFini = fini.length() >= 10? fini.substring(0,10): fini;
		String entradaFter = fter.length() >= 10? fter.substring(0,10): fter;
		try {
			LocalDate fechaIni = LocalDate.parse(entradaFini);
			LocalDate fechaTer = LocalDate.parse(entradaFter);
			if (fechaIni.isAfter(fechaTer)) {
				log.info("Error: FINI es superior a FTER");
				res = false;
	        }
		} catch (DateTimeParseException e) {
			log.info("Error: FINI y/o FTER son fechas inválidas");
			e.printStackTrace();
			res = false;
		}
		return res;
	};

	private static HashMap<String, String> procesarParametrosSolicitud(String parametros, String IdTipoDocumento) throws Exception {
		log.info("Procesando parametros de solicitud: " + parametros);
		String[] campos = parametros.split(";");
		HashMap<String, String> salida = new HashMap<String, String>();
		for (String campo: campos) {
			String[] par = campo.trim().split("=");
			if(par.length == 2) {
				if (par[1].trim().toUpperCase().equals("NULL")) {
					salida.put(par[0].trim(), null);					
				} else {
					salida.put(par[0].trim(), par[1].trim());	
				}
			}else if(par.length == 1) {
				salida.put(par[0].trim(), null);
			}
		}
		return salida;
	};
	
	private static void procesarResultado(ResultSet resultSet) throws SQLException {
		ResultSetMetaData metaData = resultSet.getMetaData();
		int columnas = metaData.getColumnCount();
		int numAdvertencias = 0;
		String advertencias = "";
		int numErrores = 0;
		String errores = "";
		if (columnas == 3) {
			while (resultSet.next()) {
				String codigo = resultSet.getNString("Codigo");
				String requiereAutorizacion = resultSet.getNString("RequiereAutorizacion");
				String descripcion = resultSet.getNString("Descripcion");
				
				String advertencia = "Codigo: " + codigo + ", RequiereAutorizacion: " + requiereAutorizacion + ", Descripcion: " + descripcion + "\n";
				advertencias = advertencias + advertencia;
				numAdvertencias += 1;
			}			
		} else if (columnas == 2) {
			while (resultSet.next()) {
				String codigo = resultSet.getNString("Codigo");
				String descripcion = resultSet.getNString("Descripcion");
				
				String error = "Codigo: " + codigo + ", Descripcion: " + descripcion + "\n";
				errores = errores + error;
				numErrores += 1;
			}			
		}
		if (numErrores > 0) {
			log.info("Errores ocurridos en BSCL.Normativa.BD.Servicios::NOR.CrearSolicitudCircular - "+numErrores);
			log.info(advertencias);
		}
		if (numAdvertencias > 0) {
			log.info("Advertencias obtenidas de BSCL.Normativa.BD.Servicios::NOR.CrearSolicitudCircular - "+numAdvertencias);
			log.info(errores);
		}
    }
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public static void main(String[] args) throws Exception {
		try (Connection con =  PlataformaUtils.getPropertiesConnection("scotiapre","connections.properties")) {
			try {
				Gson gson = Utils.getGsonBuilder();
				String json = "{}";
				GenerarNCG380_Entrada Objin = gson.fromJson(json, GenerarNCG380_Entrada.class);
	
				GenerarNCG380 gn = new GenerarNCG380();
				Objin.p_FechaEjecucion= "2026-04-28";
				Objin.p_IdEmpresa = 1L;
				
	
				GenerarNCG380_Salida resp = gn.logica(con, Objin);
				//con.rollback();
				con.commit();
			} catch (Exception e) {
				e.printStackTrace();
				con.rollback();
			}
		} catch (Exception e) {
			e.printStackTrace();			
		}	

	}
}
