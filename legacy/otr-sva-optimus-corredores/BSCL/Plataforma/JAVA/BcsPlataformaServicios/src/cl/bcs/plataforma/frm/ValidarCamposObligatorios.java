package cl.bcs.plataforma.frm;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.frm.pojo.FormularioCanal;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.wbs.pojo.Indicador;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class ValidarCamposObligatorios implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(ValidarCamposObligatorios.class);
	private String SQL_CAMPOS_FORMULARIO = loadResourceToString("cl/bcs/plataforma/frm/sql/ValidarCamposObligatorios_SelectCamposFormulario.sql");
	private String SQL_CAMPOS_FORMULARIO_EXCEPCIONES = loadResourceToString("cl/bcs/plataforma/frm/sql/ValidarCamposObligatorios_SelectCamposFormularioExcepciones.sql");
	private String SQL_CAMPOS_FORMULARIO_TIPO_GRID = loadResourceToString("cl/bcs/plataforma/frm/sql/ValidarCamposObligatorios_SelectCamposFormularioTipoGrid.sql");
	private String SQL_CAMPOS_FORMULARIO_CODIGO = loadResourceToString("cl/bcs/plataforma/frm/sql/ValidarCamposObligatorios_SelectCamposFormularioCodigo.sql");
	private String SQL_CAMPOS_VALORES_POSBILES = loadResourceToString("cl/bcs/plataforma/frm/sql/ValidarContenidoCampos_SelectValoresPosibles.sql");
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static class CampoGrid extends BcsPojo {
		public String CodigoCampoBackend;
		public Long Tamano;
	}
		
	public static class SalidaValidarCamposObligatorios extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaValidarCamposObligatorios() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	}
	
	public static class CampoObligatorio extends BcsPojo {
		public String CodigoCampoBackend;
		public String CodigoCampoFrontend;
		public String CodigoCampo;
		public String NombreCampo;
		public String TipoDato;
		public String ValorDefecto;
	}
	
	public static class CampoGridObligatorio extends BcsPojo {
		public String CodigoCampoBackend;
		public String NombreCampo;
		public Long CantidadMinima;
	}
	
	public static class CampoExcepcion extends BcsPojo {
		public String CodigoCampo;
		public String CodigoCampoExcepcion;
		public String ValorCampoExcepcion;
		public String CodigoCampoBackendExcepcion;
		public String TipoDato;
	}
	
	public static class CampoValidacion extends BcsPojo {
		public String Campo;
		public String ValoresPosibles;
		public String ValorDefecto;
		public String Prefijo;
	}
	
	public <T> SalidaValidarCamposObligatorios validarObjeto(Connection con, FormularioCanal formularioCanal, List<T> objetos, Class<T> tipo) throws Exception {
		logger.debug("INICIO::ValidarCamposObligatorios.validarObjeto");
		long inicio = System.currentTimeMillis();
		SalidaValidarCamposObligatorios resultado = new SalidaValidarCamposObligatorios();
		
		List<CampoObligatorio> camposObligatorios = obtenerCamposFormularioCanal(con, formularioCanal);
		Map<String, List<CampoExcepcion>> camposFormularioExcepcionPorCodigoCampo = obtenerCamposFormularioExcepcionPorCodigoCampo(con, formularioCanal, camposObligatorios);
		
		for(T objeto : objetos) {
			for(CampoObligatorio campoObligatorio : camposObligatorios) {
				Field campo = objeto.getClass().getField(campoObligatorio.CodigoCampoBackend);
				List<CampoExcepcion> camposExcepciones = camposFormularioExcepcionPorCodigoCampo.get(campoObligatorio.CodigoCampo);
				
 				if(esExcepcion(camposExcepciones, objeto)) {
					continue;
				}
				
				if(campo == null) {
					resultado = agregarErrorResultado(con, campoObligatorio.NombreCampo, formularioCanal.IdEmpresa, resultado);
					continue;
				}
				
				if("TEXT".equals(campoObligatorio.TipoDato)) {
					if(campo.getType().equals(String.class)) {
						String valor = (String) campo.get(objeto);
						if(CampoFormularioUtils.noEsValorValido(valor, campoObligatorio.ValorDefecto, campoObligatorio.CodigoCampo)) {
							resultado = agregarErrorResultado(con, campoObligatorio.NombreCampo, formularioCanal.IdEmpresa, resultado);
							continue;
						}
					} else {
						resultado = agregarErrorResultado(con, campoObligatorio.NombreCampo, formularioCanal.IdEmpresa, resultado);
						continue;
					}
				}
				
				if("NUMBER".equals(campoObligatorio.TipoDato)) {
					if(campo.getType().equals(Long.class)) {
						Long valor = (Long) campo.get(objeto);
						if(CampoFormularioUtils.noEsValorValido(valor, campoObligatorio.ValorDefecto, campoObligatorio.CodigoCampo)) {
							resultado = agregarErrorResultado(con, campoObligatorio.NombreCampo, formularioCanal.IdEmpresa, resultado);
							continue;
						}
					} else {
						resultado = agregarErrorResultado(con, campoObligatorio.NombreCampo, formularioCanal.IdEmpresa, resultado);
						continue;
					}
				}
				
				if("DATE".equals(campoObligatorio.TipoDato)) {
					if(campo.getType().equals(java.sql.Date.class)) {
						java.sql.Date valor = (java.sql.Date) campo.get(objeto);
						if(CampoFormularioUtils.noEsValorValido(valor, campoObligatorio.ValorDefecto, campoObligatorio.CodigoCampo)) {
							resultado = agregarErrorResultado(con, campoObligatorio.NombreCampo, formularioCanal.IdEmpresa, resultado);
							continue;
						}
					} else {
						resultado = agregarErrorResultado(con, campoObligatorio.NombreCampo, formularioCanal.IdEmpresa, resultado);
						continue;
					}
				}
				
				if("SECONDDATE".equals(campoObligatorio.TipoDato)) {
					if(campo.getType().equals(java.sql.Timestamp.class)) {
						java.sql.Timestamp valor = (java.sql.Timestamp) campo.get(objeto);
						if(CampoFormularioUtils.noEsValorValido(valor, campoObligatorio.ValorDefecto, campoObligatorio.CodigoCampo)) {
							resultado = agregarErrorResultado(con, campoObligatorio.NombreCampo, formularioCanal.IdEmpresa, resultado);
							continue;
						}
					} else {
						resultado = agregarErrorResultado(con, campoObligatorio.NombreCampo, formularioCanal.IdEmpresa, resultado);
						continue;
					}
				}
				
			}
		}
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ValidarCamposObligatorios.validarObjeto", fin - inicio);
		return resultado;
	}
	
	public SalidaValidarCamposObligatorios validarGrilla(Connection con, FormularioCanal formularioCanal, List<CampoGrid> grillas) throws Exception {
		logger.debug("INICIO::ValidarCamposObligatorios.validarObjeto");
		long inicio = System.currentTimeMillis();
		SalidaValidarCamposObligatorios resultado = new SalidaValidarCamposObligatorios();
		
		resultado = validarEntrada(con, grillas, formularioCanal.IdEmpresa);
		
		if(resultado.P_OUT_ERRORESCONTADOR > 0) {
			return resultado;
		}
		
		List<CampoGridObligatorio> camposObligatorios = obtenerCamposGrillaFormularioCanal(con, formularioCanal);
		
		Map<String, CampoGrid> grillasPorCodigoCampo = obtenerGrillasPorCodigoCampo(grillas);
		
		for(CampoGridObligatorio campoObligatorio : camposObligatorios) {
			CampoGrid grilla = grillasPorCodigoCampo.get(campoObligatorio.CodigoCampoBackend);
			
			if(grilla == null) {
				resultado = agregarErrorResultado(con, campoObligatorio.NombreCampo, formularioCanal.IdEmpresa, resultado);
				continue;
			}
			
			if(grilla.Tamano < campoObligatorio.CantidadMinima) {
				resultado = agregarErrorResultado(con, campoObligatorio.NombreCampo, formularioCanal.IdEmpresa, resultado);
				continue;
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ValidarCamposObligatorios.validarObjeto", fin - inicio);
		return resultado;
	}
	
	private List<CampoObligatorio> obtenerCamposFormularioCanal(Connection con, FormularioCanal formularioCanal) throws Exception {
		logger.debug("INICIO::ValidarCamposObligatorios.obtenerCamposFormularioCanal");
		long inicio = System.currentTimeMillis();
		
		HashMap<String,Object> parametros = new HashMap<String,Object>(2);
		parametros.put("CodigoFormulario", formularioCanal.CodigoFormulario);
		parametros.put("IdTipoCanal", formularioCanal.IdTipoCanal);
		
		SQL sql = new SQL();
		List<CampoObligatorio> camposObligatorios = sql.query(con, SQL_CAMPOS_FORMULARIO, parametros, CampoObligatorio.class);
		if(formularioCanal.CodigoFormulario.equals("DATOS_GENERALES_PERSONA_FULL_JURIDICA")) {
			camposObligatorios.removeIf(nombreFantasia -> nombreFantasia.CodigoCampoBackend.equals("IdNombreFantasiaPrincipal"));
		}
		
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ValidarCamposObligatorios.obtenerCamposFormularioCanal", fin - inicio);
		return camposObligatorios;
	}
	
	private <T> Boolean esExcepcion(List<CampoExcepcion> camposExcepcion, T objeto) throws Exception {
		logger.trace("INICIO::ValidarCamposObligatorios.esExcepcion");
		long inicio = System.currentTimeMillis();
		
		if(camposExcepcion == null) {
			return false;
		}
		
		for(CampoExcepcion campoExcepcion : camposExcepcion) {
			Field campo = objeto.getClass().getField(campoExcepcion.CodigoCampoBackendExcepcion);
			Boolean esExcepcion = false;
			if(campo.getType().equals(String.class)) {
				String valor = (String) campo.get(objeto);
				esExcepcion = campoExcepcion.ValorCampoExcepcion.equals(valor);
			}
			
			if(campo.getType().equals(Long.class)) {
				Long valor = (Long) campo.get(objeto);
				Long valorCampo = Long.valueOf(campoExcepcion.ValorCampoExcepcion);
				esExcepcion = valorCampo.equals(valor);
			}
			
			if(campo.getType().equals(java.sql.Date.class)) {
				java.sql.Date valor = (java.sql.Date) campo.get(objeto);
				String fecha = Fecha.toStringDate(valor);
				esExcepcion = campoExcepcion.ValorCampoExcepcion.equals(fecha);
			}
			
			if(campo.getType().equals(java.sql.Timestamp.class)) {
				java.sql.Timestamp valor = (java.sql.Timestamp) campo.get(objeto);
				String fecha = Fecha.toStringDate(valor);
				esExcepcion = campoExcepcion.ValorCampoExcepcion.equals(fecha);
			}
			if(esExcepcion) return true;
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ValidarCamposObligatorios.esExcepcion", fin - inicio);
		return false;
	}
	
	private Map<String, List<CampoExcepcion>> obtenerCamposFormularioExcepcionPorCodigoCampo(Connection con, FormularioCanal formularioCanal, List<CampoObligatorio> camposObligatorios) throws Exception {
		logger.debug("INICIO::ValidarCamposObligatorios.obtenerCamposFormularioExcepcionPorCodigoCampo");
		long inicio = System.currentTimeMillis();
		
		List<String> campos = camposObligatorios
				.parallelStream()
				.map(campoObligatorio->campoObligatorio.CodigoCampo)
				.distinct()
				.collect(Collectors.toList());
		HashMap<String,Object> parametros = new HashMap<String,Object>(4);
		parametros.put("CodigoFormulario", formularioCanal.CodigoFormulario);
		parametros.put("IdTipoCanal", formularioCanal.IdTipoCanal);
		parametros.put("CodigoCampo", campos);
		
		SQL sql = new SQL();
		List<CampoExcepcion> camposExcepcion = sql.query(con, SQL_CAMPOS_FORMULARIO_EXCEPCIONES, parametros, CampoExcepcion.class);
		
		Map<String, List<CampoExcepcion>> camposFormularioExcepcionPorCodigoCampo = camposExcepcion
				.parallelStream()
				.collect(Collectors.groupingBy(campoExcepcion->campoExcepcion.CodigoCampo));
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ValidarCamposObligatorios.obtenerCamposFormularioExcepcionPorCodigoCampo", fin - inicio);
		return camposFormularioExcepcionPorCodigoCampo;
	}
	
	private SalidaValidarCamposObligatorios agregarErrorResultado(Connection con, String nombreCampo, Long idEmpresa, SalidaValidarCamposObligatorios resultado) throws Exception {
		logger.trace("INICIO::ValidarCamposObligatorios.obtenerCamposFormularioCanal");
		long inicio = System.currentTimeMillis();
		resultado.P_OUT_ERRORESCONTADOR += 1;
		HashMap<String,String> parametros = new HashMap<String,String>(1);
		parametros.put("NombreCampo", nombreCampo);
		resultado.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR, "UI_ERROR_CAMPO_ES_OBLIGATORIO", parametros, con, idEmpresa));
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ValidarCamposObligatorios.obtenerCamposFormularioCanal", fin - inicio);
		return resultado;
	}
	
	private SalidaValidarCamposObligatorios validarEntrada(Connection con, List<CampoGrid> grillas, Long idEmpresa) throws Exception {
		logger.trace("INICIO::ValidarCamposObligatorios.esValorValido");
		long inicio = System.currentTimeMillis();
		SalidaValidarCamposObligatorios resultado = new SalidaValidarCamposObligatorios();
		
		List<String> campos = grillas.parallelStream().map(grilla->grilla.CodigoCampoBackend).collect(Collectors.toList());
		Set<String> camposDiferentes = new HashSet<String>(campos);
		
		if(camposDiferentes.size() < campos.size()) {
			resultado.P_OUT_ERRORESCONTADOR += 1;
			resultado.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR, "UI_ERROR_VALIDA_GRILLA_CAMPOS_ENTRADA_DUPLICADOS", new HashMap<String,String>(), con, idEmpresa));
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ValidarCamposObligatorios.esValorValido", fin - inicio);
		return resultado;
	}
	
	private List<CampoGridObligatorio> obtenerCamposGrillaFormularioCanal(Connection con, FormularioCanal formularioCanal) throws Exception {
		logger.debug("INICIO::ValidarCamposObligatorios.obtenerCamposGrillaFormularioCanal");
		long inicio = System.currentTimeMillis();
		
		HashMap<String,Object> parametros = new HashMap<String,Object>(2);
		parametros.put("CodigoFormulario", formularioCanal.CodigoFormulario);
		parametros.put("IdTipoCanal", formularioCanal.IdTipoCanal);
		
		SQL sql = new SQL();
		List<CampoGridObligatorio> camposGridObligatorios = sql.query(con, SQL_CAMPOS_FORMULARIO_TIPO_GRID, parametros, CampoGridObligatorio.class);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ValidarCamposObligatorios.obtenerCamposGrillaFormularioCanal", fin - inicio);
		return camposGridObligatorios;
	}
	
	private Map<String,CampoGrid> obtenerGrillasPorCodigoCampo(List<CampoGrid> grillas){
		logger.trace("INICIO::ValidarCamposObligatorios.obtenerGrillasPorCodigoCampo");
		long inicio = System.currentTimeMillis();
		Map<String, CampoGrid> grillasPorCodigoCampo = grillas.parallelStream().collect(Collectors.toMap(grilla->grilla.CodigoCampoBackend, grilla->grilla));
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ValidarCamposObligatorios.obtenerGrillasPorCodigoCampo", fin - inicio);
		return grillasPorCodigoCampo;
	}
	
	public <T> SalidaValidarCamposObligatorios validarIndicadores(Connection con, FormularioCanal formularioCanal, Map<String, Indicador> indicadores, Class<T> tipo) throws Exception {
		logger.debug("INICIO::ValidarCamposObligatorios.validarIndicadores");
		long inicio = System.currentTimeMillis();
		SalidaValidarCamposObligatorios resultado = new SalidaValidarCamposObligatorios();
		
		List<CampoObligatorio> camposObligatorios = obtenerCamposFormularioCanal(con, formularioCanal);
		Map<String, List<CampoExcepcion>> camposFormularioExcepcionPorCodigoCampo = obtenerCamposFormularioExcepcionPorCodigoCampo(con, formularioCanal, camposObligatorios);
		for(CampoObligatorio campoObligatorio : camposObligatorios) {
			Indicador indicador = indicadores.get(campoObligatorio.CodigoCampoFrontend);

			List<CampoExcepcion> camposExcepciones = new ArrayList<CampoExcepcion>();
			if(!camposFormularioExcepcionPorCodigoCampo.isEmpty()) {
				camposExcepciones = camposFormularioExcepcionPorCodigoCampo.get(campoObligatorio.CodigoCampo);	
			}
			
			if(esExcepcion(camposExcepciones, indicador)) {
				continue;
			}
			if(tieneValorInvalido(campoObligatorio, indicador, camposExcepciones)) {
				resultado = agregarErrorResultado(con, campoObligatorio.NombreCampo, formularioCanal.IdEmpresa, resultado);
				continue;
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ValidarCamposObligatorios.validarIndicadores", fin - inicio);
		return resultado;
	}
	
	public <T> SalidaValidarCamposObligatorios validarIndicadoresEnrolamiento(Connection con, FormularioCanal formularioCanal, Map<String, cl.bcs.socionegocio.enr.pojo.Indicador> indicadores, Class<T> tipo) throws Exception {
		logger.debug("INICIO::ValidarCamposObligatorios.validarIndicadores");
		long inicio = System.currentTimeMillis();
		SalidaValidarCamposObligatorios resultado = new SalidaValidarCamposObligatorios();
		
		List<CampoObligatorio> camposObligatorios = obtenerCamposFormularioCanal(con, formularioCanal);
		Map<String, List<CampoExcepcion>> camposFormularioExcepcionPorCodigoCampo = obtenerCamposFormularioExcepcionPorCodigoCampo(con, formularioCanal, camposObligatorios);
		for(CampoObligatorio campoObligatorio : camposObligatorios) {
			cl.bcs.socionegocio.enr.pojo.Indicador indicador = indicadores.get(campoObligatorio.CodigoCampoFrontend);

			List<CampoExcepcion> camposExcepciones = new ArrayList<CampoExcepcion>();
			if(!camposFormularioExcepcionPorCodigoCampo.isEmpty()) {
				camposExcepciones = camposFormularioExcepcionPorCodigoCampo.get(campoObligatorio.CodigoCampo);	
			}
			
			if(esExcepcion(camposExcepciones, indicador)) {
				continue;
			}
			if(tieneValorInvalido(campoObligatorio, indicador, camposExcepciones)) {
				resultado = agregarErrorResultado(con, campoObligatorio.NombreCampo, formularioCanal.IdEmpresa, resultado);
				continue;
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ValidarCamposObligatorios.validarIndicadores", fin - inicio);
		return resultado;
	}
	
	private boolean tieneValorInvalido(CampoObligatorio campo, Object o, List<CampoExcepcion> excepciones) {
        logger.trace("INICIO::ValidarRequisitosEnrolamientoFull.tieneValorInvalido");
        long inicio = System.currentTimeMillis();

        boolean esExcepcion = false;
        for (CampoExcepcion excepcion : excepciones) {
            if (esExcepcion) {
                break;
            }
            Object rawValueExcepcion = obtenerValorPropiedad(excepcion.CodigoCampoBackendExcepcion, o);
            if ("TEXT".equals(excepcion.TipoDato) && rawValueExcepcion instanceof String) {
                esExcepcion = excepcion.ValorCampoExcepcion.equals(rawValueExcepcion);
                continue;
            }
            if ("NUMBER".equals(excepcion.TipoDato) && rawValueExcepcion instanceof Long) {
                esExcepcion = Long.valueOf(excepcion.ValorCampoExcepcion).equals(rawValueExcepcion);
                continue;
            }
            if ("DATE".equals(excepcion.TipoDato) && rawValueExcepcion instanceof java.sql.Date) {
                esExcepcion = excepcion.ValorCampoExcepcion.equals(Fecha.toStringDate((java.sql.Date) rawValueExcepcion));
                continue;
            }
            if ("SECONDDATE".equals(excepcion.TipoDato) && rawValueExcepcion instanceof java.sql.Timestamp) {
                esExcepcion = excepcion.ValorCampoExcepcion.equals(Fecha.toStringDate((java.sql.Timestamp) rawValueExcepcion));
            }
        }
        if (esExcepcion) {
            return false;
        }

        Object rawValue = obtenerValorPropiedad(campo.CodigoCampoBackend, o);
        if ("TEXT".equals(campo.TipoDato) && rawValue instanceof String) {
            return CampoFormularioUtils.noEsValorValido((String) rawValue, campo.ValorDefecto, campo.CodigoCampo);
        }
        if ("NUMBER".equals(campo.TipoDato) && rawValue instanceof Long) {
            return CampoFormularioUtils.noEsValorValido((Long) rawValue, campo.ValorDefecto, campo.CodigoCampo);
        }
        if ("DATE".equals(campo.TipoDato) && rawValue instanceof java.sql.Date) {
            return CampoFormularioUtils.noEsValorValido((java.sql.Date) rawValue, campo.ValorDefecto, campo.CodigoCampo);
        }
        if ("SECONDDATE".equals(campo.TipoDato) && rawValue instanceof java.sql.Timestamp) {
            return CampoFormularioUtils.noEsValorValido((java.sql.Timestamp) rawValue, campo.ValorDefecto, campo.CodigoCampo);
        }

        long fin = System.currentTimeMillis();
        logger.trace("FIN::ValidarRequisitosEnrolamientoFull.tieneValorInvalido {}", fin - inicio);

        return true;
    }
	
    private Object obtenerValorPropiedad(String nombrePropiedad, Object o) {
        logger.trace("INICIO::ValidarRequisitosEnrolamientoFull.obtenerValorPropiedad");
        long inicio = System.currentTimeMillis();

        Class<?> clazz = null;
        Object valorPropiedad = null;

        if (o == null) {
            return null;
        }

        try {
            clazz = o.getClass();
            Field field = clazz.getDeclaredField(nombrePropiedad);
            field.setAccessible(true);
            valorPropiedad = field.get(o);

        } catch (NoSuchFieldException e) {
            logger.warn("La clase " + clazz.getSimpleName() + " NO tiene la propiedad [" + nombrePropiedad + "]");

        } catch (IllegalAccessException ignored) {
        }

        long fin = System.currentTimeMillis();
        logger.trace("FIN::ValidarRequisitosEnrolamientoFull.obtenerValorPropiedad {}", fin - inicio);

        return valorPropiedad;
    }
    
	public <T> SalidaValidarCamposObligatorios validarContenidoCampos(Connection con, List<T> objetos, String tipoCanal, Long idEmpresa) throws Exception {
		logger.trace("INICIO::ValidarCamposObligatorios.validarContenidoCampos");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		SalidaValidarCamposObligatorios resultado = new SalidaValidarCamposObligatorios();

		List<String> listaValidacion = objetos.stream()
				.flatMap(data -> Arrays.stream(data.getClass().getDeclaredFields())
						.filter(field -> field.isAnnotationPresent(ValidarContenidoCampos.class)).distinct()
						.map(campo -> campo.getAnnotation(ValidarContenidoCampos.class).idCampo()))
				.collect(Collectors.toList());

		HashMap<String, Object> params = new HashMap<>(1);
		params.put("FiltroValidacion", listaValidacion);

		List<CampoValidacion> valoresPosiblesCampo = sql.query(con, SQL_CAMPOS_VALORES_POSBILES, params, CampoValidacion.class);

		objetos.stream().forEach(objeto -> Arrays.stream(objeto.getClass().getDeclaredFields())
				.filter(field -> field.isAnnotationPresent(ValidarContenidoCampos.class)).forEach(campo -> {
					ValidarContenidoCampos validacion = campo.getAnnotation(ValidarContenidoCampos.class);
					String tipoValidacion = validacion.idCampo();
					String prefijo = validacion.prefijo();
					try {
						params.clear();
						params.put("CodigoCampoBackend", campo.getName());
						params.put("IdTipoCanal", tipoCanal);
						if (sql.query(con, SQL_CAMPOS_FORMULARIO_CODIGO, params, CampoObligatorio.class).isEmpty()) {
							return;
						}
						Object valorCampo = campo.get(objeto);
						String nombreCampo = validacion.idCampoMensaje();

						CampoValidacion campoValidacion = valoresPosiblesCampo.stream()
								.filter(data -> data.Campo.equalsIgnoreCase(tipoValidacion)
										&& data.Prefijo.equalsIgnoreCase(prefijo))
								.findFirst().orElse(null);

						if (campoValidacion == null) {
							resultado.P_OUT_ERRORESCONTADOR += 1L;
							HashMap<String, String> param = new HashMap<>(1);
							param.put("IdTipoValidacion", tipoValidacion);
							resultado.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
									"TIPO_VALIDACION_NO_DEFINIDA", param, con, idEmpresa));
						} else if (campoValidacion != null) {
							String valor = Arrays.stream(campoValidacion.ValoresPosibles.split(","))
									.filter(valores -> valores.equalsIgnoreCase(String.valueOf(valorCampo))).findFirst()
									.orElse(campoValidacion.ValorDefecto);

							if (!valor.equalsIgnoreCase(String.valueOf(valorCampo))
									|| !campoValidacion.Prefijo.equalsIgnoreCase(prefijo)) {
								resultado.P_OUT_ERRORESCONTADOR += 1L;
								HashMap<String, String> param = new HashMap<>(2);
								param.put("atributo", nombreCampo);
								param.put("valor", String.valueOf(valorCampo));
								resultado.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
										"VALOR_FUERA_RANGO", param, con, idEmpresa));//
							}
						}

					} catch (Exception e) {
						logger.warn("Error al obtener contenido campo");
					}
				}));
		
		long fin = System.currentTimeMillis();
        logger.trace("FIN::ValidarCamposObligatorios.validarContenidoCampos {}", fin - inicio);
		return resultado;
	}
}
