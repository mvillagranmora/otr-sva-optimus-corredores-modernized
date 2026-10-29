package cl.bcs.socionegocio.soc;

import cl.bcs.plataforma.frm.CampoFormularioUtils;
import cl.bcs.plataforma.ind.pojo.DetalleTipoIndicador;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.per.pojo.Entidad;
import cl.bcs.socionegocio.pojo.RequisitoEnrolamientoFull;
import cl.bcs.socionegocio.pojo.Validacion;
import cl.bcs.socionegocio.soc.pojo.Cliente;
import cl.bcs.socionegocio.soc.pojo.Indicador;
import cl.bcs.socionegocio.soc.pojo.Portafolio;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ValidarRequisitosEnrolamientoFull implements JavaCall {
	static final Logger logger = LogManager.getLogger(ValidarRequisitosEnrolamientoFull.class);

	private final String SQL_SELECT_CONFIGURACION_CAMPO = loadResourceToString("cl/bcs/socionegocio/soc/sql/ValidarRequisitosEnrolamientoFull_SelectConfiguracionCampo.sql");
	private final String SQL_SELECT_CAMPO_EXCEPCION = loadResourceToString("cl/bcs/socionegocio/soc/sql/ValidarRequisitosEnrolamientoFull_SelectCampoExcepcion.sql");
	private final String SQL_SELECT_DETALLE_TIPO_INDICADOR = loadResourceToString("cl/bcs/socionegocio/soc/sql/ValidarRequisitosEnrolamientoFull_SelectDetalleTipoIndicador.sql");

	private static final Map<String, String> conceptosPorCodigoFormulario = new HashMap<>(32);
	private static final Map<String, String> tiposPersonaPorInicial = new HashMap<>(4);

	static {
		conceptosPorCodigoFormulario.put("DATOS_GENERALES_CLIENTE_FULL_JURIDICA", "DATOSGENERALES");
		conceptosPorCodigoFormulario.put("DATOS_GENERALES_CLIENTE_FULL_NATURAL", "DATOSGENERALES");
		conceptosPorCodigoFormulario.put("DATOS_GENERALES_PERSONA_FULL_JURIDICA", "DATOSGENERALES");
		conceptosPorCodigoFormulario.put("DATOS_GENERALES_PERSONA_FULL_NATURAL", "DATOSGENERALES");

        conceptosPorCodigoFormulario.put("FATCA_FULL_NATURAL", "FATCACRS");
        conceptosPorCodigoFormulario.put("FATCA_FULL_JURIDICA", "FATCACRS");

        conceptosPorCodigoFormulario.put("DATOS_GENERALES_PORTAFOLIO_CLIENTE_FULL_JURIDICA", "DATOSGENERALESPORTAFOLIO");
        conceptosPorCodigoFormulario.put("DATOS_GENERALES_PORTAFOLIO_CLIENTE_FULL_NATURAL", "DATOSGENERALESPORTAFOLIO");

        conceptosPorCodigoFormulario.put("GRILLA_FULL_JURIDICA", "GRILLA");
        conceptosPorCodigoFormulario.put("GRILLA_FULL_NATURAL", "GRILLA");

        conceptosPorCodigoFormulario.put("GRILLA_PORTAFOLIO_FULL_JURIDICA", "GRILLAPORTAFOLIO");
        conceptosPorCodigoFormulario.put("GRILLA_PORTAFOLIO_FULL_NATURAL", "GRILLAPORTAFOLIO");

        conceptosPorCodigoFormulario.put("INDICADORES_CLIENTE_FULL_JURIDICA", "INDICADORESCLIENTE");
        conceptosPorCodigoFormulario.put("INDICADORES_CLIENTE_FULL_NATURAL", "INDICADORESCLIENTE");
        conceptosPorCodigoFormulario.put("INDICADORES_PERSONA_FULL_JURIDICA", "INDICADORESPERSONA");
        conceptosPorCodigoFormulario.put("INDICADORES_PERSONA_FULL_NATURAL", "INDICADORESPERSONA");
        conceptosPorCodigoFormulario.put("PREFERENCIAS_NEGOCIOS_CLIENTE_FULL_JURIDICA", "INDICADORES");
        conceptosPorCodigoFormulario.put("PREFERENCIAS_NEGOCIOS_CLIENTE_FULL_NATURAL", "INDICADORES");

        conceptosPorCodigoFormulario.put("PORTAFOLIO_CLIENTE_INDICADORES_FULL_JURIDICA", "INDICADORESPORTAFOLIO");
        conceptosPorCodigoFormulario.put("PORTAFOLIO_CLIENTE_INDICADORES_FULL_NATURAL", "INDICADORESPORTAFOLIO");
        conceptosPorCodigoFormulario.put("PORTAFOLIO_CLIENTE_PREFERENCIA_NEGOCIO_FULL_JURIDICA", "INDICADORESPORTAFOLIO");
        conceptosPorCodigoFormulario.put("PORTAFOLIO_CLIENTE_PREFERENCIA_NEGOCIO_FULL_NATURAL", "INDICADORESPORTAFOLIO");

        tiposPersonaPorInicial.put("J", "JURIDICA");
        tiposPersonaPorInicial.put("N", "NATURAL");
    }

    public static class Tupla<T, K> {
        public T primero;
        public K segundo;

        public Tupla(T primero, K segundo) {
            this.primero = primero;
            this.segundo = segundo;
        }
    }

    public static class Campo extends BcsPojo {
        public String Concepto;
        public String NombreCampo;
        public String CodigoCampo;
        public String CodigoFormulario;
        public String CodigoCampoBackend;
        public String CodigoCampoFrontend;
        public String Requerido;
        public String TipoDato;
        public String IdUso;
        public String ValorVacioCampo;
        public String ValorDefectoCampoFormulario;
        public String UbicacionFormulario;

        public Long CantidadMinima;
    }

    public static class CampoExcepcion extends BcsPojo {
        public String CodigoFormulario;
        public String CodigoCampo;
        public String CodigoCampoExcepcion;
        public String CodigoCampoBackendExcepcion;
        public String TipoDato;
        public String ValorCampoExcepcion;
        public String IdTipoCanal;
    }

	public static class Configuracion extends BcsPojo {
		private final Map<String, List<Campo>> camposCache;
		private final List<Campo> campos;

		private final Map<String, List<CampoExcepcion>> excepcionesCache;
		private final List<CampoExcepcion> excepciones;

		private final Map<Long, Set<String>> detallesTipoIndicadorCache;
		private final List<DetalleTipoIndicador> tipoIndicadorDetalles;

		public Configuracion() {
			this.campos = new ArrayList<>();
			this.camposCache = new HashMap<>();
			this.excepciones = new ArrayList<>();
			this.excepcionesCache = new HashMap<>();
			this.tipoIndicadorDetalles = new ArrayList<>();
			this.detallesTipoIndicadorCache = new HashMap<>();
		}

        public List<Campo> obtenerCampos(Cliente cliente, String concepto) {
            logger.trace("INICIO::ValidarRequisitosEnrolamientoFull.Configuracion.obtenerCampos");
            long inicio = System.currentTimeMillis();

            String tipoPersona = tiposPersonaPorInicial.get(cliente.DatosGenerales.TipoPersona);
            String uso = cliente.tieneIdEnrolamiento() ? "MOD" : "CRE";

            String permutacion = uso + "-" + tipoPersona + "-" + concepto;

            List<Campo> camposFiltrados = camposCache.computeIfAbsent(permutacion, key -> campos
                    .stream()
                    .filter(campo -> uso.equals(campo.IdUso) || "AMB".equals(campo.IdUso))
                    .filter(campo -> campo.CodigoFormulario.endsWith(tipoPersona))
                    .filter(campo -> concepto.equals(campo.Concepto))
                    .filter(campo -> !("NOMBRE_FANTASIA").equals(campo.CodigoCampo))
                    .filter(campo -> !("NOMBRE_FANTASIA_ENR_FULL").equals(campo.CodigoCampo))
                    .collect(Collectors.toList())
            );

            long fin = System.currentTimeMillis();
            logger.trace("FIN::ValidarRequisitosEnrolamientoFull.Configuracion.obtenerCampos {}", fin - inicio);

            return camposFiltrados;
        }

        public List<CampoExcepcion> obtenerExcepciones(Campo campo) {
            logger.trace("INICIO::ValidarRequisitosEnrolamientoFull.Configuracion.obtenerExcepciones");
            long inicio = System.currentTimeMillis();

            String permutacion = campo.CodigoFormulario + "-" + campo.CodigoCampo;
            List<CampoExcepcion> excepcionesFiltradas = excepcionesCache.computeIfAbsent(permutacion, key -> excepciones
                    .stream()
					.filter(excepcion -> excepcion.CodigoFormulario.equals(campo.CodigoFormulario))
					.filter(excepcion -> excepcion.CodigoCampo.equals(campo.CodigoCampo))
					.collect(Collectors.toList())
			);

			long fin = System.currentTimeMillis();
			logger.trace("FIN::ValidarRequisitosEnrolamientoFull.Configuracion.obtenerExcepciones {}", fin - inicio);

			return excepcionesFiltradas;
		}

		public Set<String> obtenerValoresTipoIndicador(Long idTipoIndicador) {
			logger.trace("INICIO::ValidarRequisitosEnrolamientoFull.obtenerValoresTipoIndicador");
			long inicio = System.currentTimeMillis();

			Set<String> valores = detallesTipoIndicadorCache.computeIfAbsent(idTipoIndicador, key -> tipoIndicadorDetalles
					.stream()
					.filter(detalle -> idTipoIndicador.equals(detalle.IdTipoIndicador))
					.map(detalle -> String.valueOf(detalle.IdDetalleIndicador))
					.collect(Collectors.toSet())
			);

			long fin = System.currentTimeMillis();
			logger.trace("FIN::ValidarRequisitosEnrolamientoFull.obtenerValoresTipoIndicador {}", fin - inicio);

			return valores;
		}
	}

    public static class EntradaValidarRequisitosEnrolamientoFull extends BcsPojo {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;
        public String p_IdTipoCanalIngreso;
        public List<Cliente> clientes;
    }

    public static class SalidaValidarRequisitosEnrolamientoFull extends BcsPojo {
        public Long P_OUT_ERRORESCONTADOR;
        public List<Entidad> P_OUT_REFID;
        public List<MensajeSalida> P_OUT_MENSAJES;
        public List<Validacion> P_OUT_VALIDACIONES;
        public List<RequisitoEnrolamientoFull> P_OUT_REQUISITOS_ENR_FULL;


        public SalidaValidarRequisitosEnrolamientoFull() {
            this.P_OUT_ERRORESCONTADOR = 0L;
            this.P_OUT_REFID = new ArrayList<>();
            this.P_OUT_MENSAJES = new ArrayList<>();
            this.P_OUT_VALIDACIONES = new ArrayList<>();
            this.P_OUT_REQUISITOS_ENR_FULL = new ArrayList<>();
        }

        public void agregarValidacionesSalida(List<Validacion> validaciones) {
            for (Validacion validacion : validaciones) {
                agregarValidacionSalida(validacion);
            }
        }

        public void agregarValidacionSalida(Validacion validacion) {
            if (validacion.EsError) {
                this.P_OUT_ERRORESCONTADOR += 1L;
                this.P_OUT_MENSAJES.add(validacion.MensajeError);
                this.P_OUT_VALIDACIONES.add(validacion);
            }
        }
    }

    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
        Gson gson = Utils.getGsonBuilder();
        EntradaValidarRequisitosEnrolamientoFull entrada = gson.fromJson(in, EntradaValidarRequisitosEnrolamientoFull.class);
        ValidarRequisitosEnrolamientoFull validarRequisitosEnrolamientoFull = new ValidarRequisitosEnrolamientoFull();
        SalidaValidarRequisitosEnrolamientoFull salida = validarRequisitosEnrolamientoFull.logica(conn, entrada);

        JsonParser parser = new JsonParser();

        return parser.parse(gson.toJson(salida)).getAsJsonObject();


    }

    public SalidaValidarRequisitosEnrolamientoFull logica(Connection con, EntradaValidarRequisitosEnrolamientoFull entrada) throws Exception {
        logger.info("INICIO::ValidarRequisitosEnrolamientoFull");
        long inicio = System.currentTimeMillis();

        SalidaValidarRequisitosEnrolamientoFull salida = new SalidaValidarRequisitosEnrolamientoFull();

        if (entrada.p_IdTipoCanalIngreso == null || entrada.p_IdTipoCanalIngreso.isEmpty()) {
            Validacion validacion = new Validacion();
            validacion.EsError = true;
            validacion.MensajeError = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_FALTA_ESPECIFICAR_CANAL_INGRESO", new HashMap<>(), con, entrada.p_IdEmpresa);
            salida.agregarValidacionSalida(validacion);

            return salida;
        }

        Configuracion configuracion = obtenerConfiguracionCampos(con, entrada.p_IdEmpresa, entrada.p_IdTipoCanalIngreso);

        List<RequisitoEnrolamientoFull> requisitosFaltantesCliente = validarClientes(con, entrada, configuracion);
        salida.P_OUT_REQUISITOS_ENR_FULL.addAll(requisitosFaltantesCliente);

        if (salida.P_OUT_REQUISITOS_ENR_FULL.size() > 0) {
            Validacion validacion = new Validacion();
            validacion.EsError = true;
            validacion.MensajeError = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_FALTAN_REQUERIMIENTOS_ENROLAMIENTO_FULL", new HashMap<>(), con, entrada.p_IdEmpresa);
            salida.agregarValidacionSalida(validacion);
        }

        long fin = System.currentTimeMillis();
        logger.info("FIN::ValidarRequisitosEnrolamientoFull {}", fin - inicio);

        return salida;
    }

	private Configuracion obtenerConfiguracionCampos(Connection con, Long idEmpresa, String idTipoCanal) throws SQLException {
		logger.debug("INICIO::ValidarRequisitosEnrolamientoFull.obtenerConfiguracionCampos");
		long inicio = System.currentTimeMillis();

		SQL sql = new SQL();
		HashMap<String, Object> parametros = new HashMap<>(2);
		parametros.put("IdTipoCanalIngreso", idTipoCanal);

		List<Campo> campos = sql.query(con, SQL_SELECT_CONFIGURACION_CAMPO, parametros, Campo.class);
	
		
		for (Campo campo : campos) {
			campo.Concepto = conceptosPorCodigoFormulario.get(campo.CodigoFormulario);
			
		}

		parametros.clear();
		parametros.put("IdTipoCanal", idTipoCanal);
		List<CampoExcepcion> excepciones = sql.query(con, SQL_SELECT_CAMPO_EXCEPCION, parametros, CampoExcepcion.class);

		parametros.clear();
		parametros.put("IdEmpresa", idEmpresa);
		List<DetalleTipoIndicador> tipoIndicadorDetalles = sql.query(con, SQL_SELECT_DETALLE_TIPO_INDICADOR, parametros, DetalleTipoIndicador.class);

		Configuracion configuracion = new Configuracion();
		configuracion.campos.addAll(campos);
		configuracion.excepciones.addAll(excepciones);
		configuracion.tipoIndicadorDetalles.addAll(tipoIndicadorDetalles);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ValidarRequisitosEnrolamientoFull.obtenerConfiguracionCampos {}", fin - inicio);

		return configuracion;
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

	public String obtenerUbicacionFormulario(Connection conn, Long idEmpresa, String plantillaUbicacion) {
		logger.debug("INICIO::ValidarRequisitosEnrolamientoFull.obtenerUbicacionFormulario");
		long inicio = System.currentTimeMillis();

		String ubicacion;
		try {
			ubicacion = new MensajeSalida(0L, MensajeSalida.MENSAJE_INFORMACION, plantillaUbicacion, new HashMap<>(), conn, idEmpresa).descripcion;
		} catch (Exception e) {
			ubicacion = plantillaUbicacion + ", sin traduccion";
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ValidarRequisitosEnrolamientoFull.obtenerUbicacionFormulario {}", fin - inicio);

		return ubicacion;
	}

    public String obtenerUbicacionFormulario(Connection conn, Long idEmpresa, String plantillaUbicacion, Portafolio portafolio) {
		logger.debug("INICIO::ValidarRequisitosEnrolamientoFull.obtenerUbicacionFormulario");
		long inicio = System.currentTimeMillis();

		String codigoPortafolio = String.valueOf(portafolio.DatosGenerales.CodigoPortafolio);
		HashMap<String, String> parametrosMensajes = new HashMap<>(2);
		parametrosMensajes.put("CodigoPortafolio", codigoPortafolio);

		String ubicacion;
		try {
			ubicacion = new MensajeSalida(0L, MensajeSalida.MENSAJE_INFORMACION, plantillaUbicacion, parametrosMensajes, conn, idEmpresa).descripcion;
		} catch (Exception e) {
			ubicacion = plantillaUbicacion + " CodigoPortafolio: " + codigoPortafolio + ", sin traduccion";
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ValidarRequisitosEnrolamientoFull.obtenerUbicacionFormulario {}", fin - inicio);

		return ubicacion;
	}

    public RequisitoEnrolamientoFull obtenerRequisitoEnrolamientoFull(Connection conn, Long idEmpresa, Long idEntidad, String codigoMensaje, HashMap<String, String> parametrosMensaje, String idTipoEntidad, String ubicacion) {
        logger.debug("INICIO::ValidarRequisitosEnrolamientoFull.obtenerRequisitoEnrolamientoFull");
        long inicio = System.currentTimeMillis();

        RequisitoEnrolamientoFull requisito = new RequisitoEnrolamientoFull();
        requisito.IdEntidad = idEntidad;
        requisito.IdTipoEntidad = idTipoEntidad;
        requisito.Ubicacion = ubicacion;
        try {
            requisito.Descripcion = new MensajeSalida(idEntidad, MensajeSalida.MENSAJE_ERROR, codigoMensaje, parametrosMensaje, conn, idEmpresa).descripcion;
        } catch (Exception e) {
            requisito.Descripcion = new MensajeSalida(idEntidad, MensajeSalida.MENSAJE_ERROR, codigoMensaje, codigoMensaje + " no fue posible recuperar traduccion").descripcion;
        }

        long fin = System.currentTimeMillis();
        logger.debug("FIN::ValidarRequisitosEnrolamientoFull.obtenerRequisitoEnrolamientoFull {}", fin - inicio);

        return requisito;
    }

    private List<RequisitoEnrolamientoFull> validarClientes(Connection con, EntradaValidarRequisitosEnrolamientoFull entrada, Configuracion configuracion) {
        logger.debug("INICIO::ValidarRequisitosEnrolamientoFull.validarClientes");
        long inicio = System.currentTimeMillis();

        List<RequisitoEnrolamientoFull> requisitos = Collections.synchronizedList(new ArrayList<>());

        entrada.clientes.forEach(cliente -> {
            List<RequisitoEnrolamientoFull> validacionesDatosGenerales = validarDatosGenerales(con, entrada.p_IdEmpresa, cliente, configuracion);
            requisitos.addAll(validacionesDatosGenerales);

            List<RequisitoEnrolamientoFull> validacionesCantidadesMinimas = validarCantidadesMinimas(con, entrada.p_IdEmpresa, cliente, configuracion);
            requisitos.addAll(validacionesCantidadesMinimas);

            List<RequisitoEnrolamientoFull> validacionesIndicadoresCliente = validarIndicadoresCliente(con, entrada.p_IdEmpresa, cliente, configuracion);
            requisitos.addAll(validacionesIndicadoresCliente);

            List<RequisitoEnrolamientoFull> validacionesIndicadoresPersona = validarIndicadoresPersona(con, entrada.p_IdEmpresa, cliente, configuracion);
            requisitos.addAll(validacionesIndicadoresPersona);

            List<RequisitoEnrolamientoFull> validacionesFatcaCrs = validarFatcaCrs(con, entrada.p_IdEmpresa, cliente, configuracion);
            requisitos.addAll(validacionesFatcaCrs);

            List<RequisitoEnrolamientoFull> validacionesPortafolios = validarPortafolios(con, entrada.p_IdEmpresa, cliente, configuracion);
            requisitos.addAll(validacionesPortafolios);
        });

        long fin = System.currentTimeMillis();
        logger.debug("FIN::ValidarRequisitosEnrolamientoFull.validarClientes {}", fin - inicio);

        return requisitos;
    }

    private List<RequisitoEnrolamientoFull> validarIndicadoresCliente(Connection con, Long idEmpresa, Cliente cliente, Configuracion configuracion) {
        logger.debug("INICIO::ValidarRequisitosEnrolamientoFull.validarIndicadoresCliente");
        long inicio = System.currentTimeMillis();

        List<RequisitoEnrolamientoFull> requisitos = configuracion.obtenerCampos(cliente, "INDICADORESCLIENTE")
                .stream()
                .filter(campo -> {
                    Indicador indicador = cliente.IndicadoresClientePorCodigoTipoIndicador.get(campo.CodigoCampoFrontend);
					if (!"S".equals(campo.Requerido) && Objects.nonNull(indicador) && "N".equals(indicador.TieneValorAsignado)) {
						indicador.ValorIndicador = campo.ValorDefectoCampoFormulario;
					}

					return tieneValorInvalido(campo, indicador, configuracion);
                })
                .map(campo -> {
                    HashMap<String, String> parametros = new HashMap<>(2);
                    parametros.put("NombreCampo", campo.NombreCampo);

                    return obtenerRequisitoEnrolamientoFull(con, idEmpresa, cliente.getIdEntidad(), "UI_CAMPO_SIN_VALOR", parametros, "CLIENTE", obtenerUbicacionFormulario(con, idEmpresa, campo.UbicacionFormulario));
                })
                .collect(Collectors.toList());

        long fin = System.currentTimeMillis();
        logger.debug("FIN::ValidarRequisitosEnrolamientoFull.validarIndicadoresCliente {}", fin - inicio);

        return requisitos;
    }

    private List<RequisitoEnrolamientoFull> validarIndicadoresPersona(Connection con, Long idEmpresa, Cliente cliente, Configuracion configuracion) {
        logger.debug("INICIO::ValidarRequisitosEnrolamientoFull.validarIndicadoresPersona");
        long inicio = System.currentTimeMillis();

        List<RequisitoEnrolamientoFull> requisitos = configuracion.obtenerCampos(cliente, "INDICADORESPERSONA")
                .stream()
                .filter(campo -> {
                    Indicador indicador = cliente.IndicadoresPersonaPorCodigoTipoIndicador.get(campo.CodigoCampoFrontend);
					if (!"S".equals(campo.Requerido) && Objects.nonNull(indicador) && "N".equals(indicador.TieneValorAsignado)) {
						indicador.ValorIndicador = campo.ValorDefectoCampoFormulario;
					}

                    return tieneValorInvalido(campo, indicador, configuracion);
                })
                .map(campo -> {
                    HashMap<String, String> parametros = new HashMap<>(2);
                    parametros.put("NombreCampo", campo.NombreCampo);

                    return obtenerRequisitoEnrolamientoFull(con, idEmpresa, cliente.getIdEntidad(), "UI_CAMPO_SIN_VALOR", parametros, "PERSONA", obtenerUbicacionFormulario(con, idEmpresa, campo.UbicacionFormulario));
                })
                .collect(Collectors.toList());

        long fin = System.currentTimeMillis();
        logger.debug("FIN::ValidarRequisitosEnrolamientoFull.validarIndicadoresPersona {}", fin - inicio);

        return requisitos;
    }

    private boolean tieneValorInvalido(Campo campo, Object o, List<CampoExcepcion> excepciones) {
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
            return CampoFormularioUtils.noEsValorValido((String) rawValue, campo.ValorVacioCampo, campo.CodigoCampo);
        }
        if ("NUMBER".equals(campo.TipoDato) && rawValue instanceof Long) {
            return CampoFormularioUtils.noEsValorValido((Long) rawValue, campo.ValorVacioCampo, campo.CodigoCampo);
        }
        if ("DATE".equals(campo.TipoDato) && rawValue instanceof java.sql.Date) {
            return CampoFormularioUtils.noEsValorValido((java.sql.Date) rawValue, campo.ValorVacioCampo, campo.CodigoCampo);
        }
        if ("SECONDDATE".equals(campo.TipoDato) && rawValue instanceof java.sql.Timestamp) {
            return CampoFormularioUtils.noEsValorValido((java.sql.Timestamp) rawValue, campo.ValorVacioCampo, campo.CodigoCampo);
        }
        if ("GRID".equals(campo.TipoDato) && rawValue instanceof Long) {
        	return (Long) rawValue < campo.CantidadMinima;
		}

        long fin = System.currentTimeMillis();
        logger.trace("FIN::ValidarRequisitosEnrolamientoFull.tieneValorInvalido {}", fin - inicio);

        return true;
    }

    private boolean tieneValorInvalido(Campo campo, Indicador indicador, Configuracion configuracion) {
		logger.trace("INICIO::ValidarRequisitosEnrolamientoFull.tieneValorInvalido");
		long inicio = System.currentTimeMillis();

		boolean noCumpleCriterioCampoRequerido = tieneValorInvalido(campo, indicador, configuracion.obtenerExcepciones(campo));
		boolean noEsIndicadorInactivo = !"INA".equals(indicador.IdEstado);

		long fin = System.currentTimeMillis();
		logger.trace("FIN::ValidarRequisitosEnrolamientoFull.tieneValorInvalido {}", fin - inicio);

		return noEsIndicadorInactivo && noCumpleCriterioCampoRequerido;
	}

    private List<RequisitoEnrolamientoFull> validarDatosGenerales(Connection con, Long idEmpresa, Cliente cliente, Configuracion configuracion) {
        logger.debug("INICIO::ValidarRequisitosEnrolamientoFull.validarDatosGenerales");
        long inicio = System.currentTimeMillis();

        List<RequisitoEnrolamientoFull> requisitos = configuracion.obtenerCampos(cliente, "DATOSGENERALES")
                .stream()
                .filter(campo -> tieneValorInvalido(campo, cliente.DatosGenerales, configuracion.obtenerExcepciones(campo)))
                .map(campo -> {
                    HashMap<String, String> parametros = new HashMap<>(2);
                    parametros.put("NombreCampo", campo.NombreCampo);

                    return obtenerRequisitoEnrolamientoFull(con, idEmpresa, cliente.getIdEntidad(), "UI_CAMPO_SIN_VALOR", parametros, "PERSONA/CLIENTE", obtenerUbicacionFormulario(con, idEmpresa, campo.UbicacionFormulario));
                })
                .collect(Collectors.toList());

        long fin = System.currentTimeMillis();
        logger.debug("FIN::ValidarRequisitosEnrolamientoFull.validarDatosGenerales {}", fin - inicio);

        return requisitos;
    }

    private List<RequisitoEnrolamientoFull> validarCantidadesMinimas(Connection con, Long idEmpresa, Cliente cliente, Configuracion configuracion) {
        logger.debug("INICIO::ValidarRequisitosEnrolamientoFull.validarCantidadesMinimas");
        long inicio = System.currentTimeMillis();

        List<RequisitoEnrolamientoFull> requisitos = configuracion.obtenerCampos(cliente, "GRILLA")
                .stream()
				.filter(campo -> tieneValorInvalido(campo, cliente, configuracion.obtenerExcepciones(campo)))
                .map(campo -> {
                    HashMap<String, String> parametros = new HashMap<>(4);
                    parametros.put("NombreCampo", campo.NombreCampo);
                    parametros.put("CantidadMinima", String.valueOf(campo.CantidadMinima));

                    return obtenerRequisitoEnrolamientoFull(con, idEmpresa, cliente.getIdEntidad(), "UI_CAMPO_NO_CUMPLE_CANTIDAD_MINIMA", parametros, "PERSONA/CLIENTE", obtenerUbicacionFormulario(con, idEmpresa, campo.UbicacionFormulario));
                })
                .collect(Collectors.toList());

        long fin = System.currentTimeMillis();
        logger.debug("FIN::ValidarRequisitosEnrolamientoFull.validarCantidadesMinimas {}", fin - inicio);

        return requisitos;
    }

    private List<RequisitoEnrolamientoFull> validarFatcaCrs(Connection con, Long idEmpresa, Cliente cliente, Configuracion configuracion) {
        logger.debug("INICIO::ValidarRequisitosEnrolamientoFull.validarFatcaCrs");
        long inicio = System.currentTimeMillis();

        List<RequisitoEnrolamientoFull> requisitos = configuracion.obtenerCampos(cliente, "FATCACRS")
                .stream()
                .filter(campo -> tieneValorInvalido(campo, cliente.Fatca, configuracion.obtenerExcepciones(campo)))
                .map(campo -> {
                    HashMap<String, String> parametros = new HashMap<>(2);
                    parametros.put("NombreCampo", campo.NombreCampo);

                    return obtenerRequisitoEnrolamientoFull(con, idEmpresa, cliente.getIdEntidad(), "UI_CAMPO_SIN_VALOR", parametros, "PERSONA/FATCA", obtenerUbicacionFormulario(con, idEmpresa, campo.UbicacionFormulario));
                })
                .collect(Collectors.toList());

        long fin = System.currentTimeMillis();
        logger.debug("FIN::ValidarRequisitosEnrolamientoFull.validarFatcaCrs {}", fin - inicio);

        return requisitos;
    }

    private List<RequisitoEnrolamientoFull> validarPortafolios(Connection con, Long idEmpresa, Cliente cliente, Configuracion configuracion) {
        logger.debug("INICIO::ValidarRequisitosEnrolamientoFull.validarPortafolios");
        long inicio = System.currentTimeMillis();

        List<RequisitoEnrolamientoFull> requisitos = new ArrayList<>();

        Function<Campo, Stream<Tupla<Campo, Portafolio>>> obtenerPortafoliosNoCumplenRequeridos = campo -> cliente.Portafolios
                .stream()
                .filter(portafolio -> tieneValorInvalido(campo, portafolio.DatosGenerales, configuracion.obtenerExcepciones(campo)))
                .map(portafolio -> new Tupla<>(campo, portafolio));

        Function<Campo, Stream<Tupla<Campo, Portafolio>>> obtenerPortafoliosNoCumplenCantidadMinima = campo -> cliente.Portafolios
                .stream()
                .filter(portafolio -> {
                    Object cantidad = obtenerValorPropiedad(campo.CodigoCampoBackend, portafolio);

                    return cantidad instanceof Long && (Long) cantidad < campo.CantidadMinima;
                })
                .map(portafolio -> new Tupla<>(campo, portafolio));

        List<RequisitoEnrolamientoFull> validacionesCamposRequeridos = configuracion.obtenerCampos(cliente, "DATOSGENERALESPORTAFOLIO")
                .stream()
                .flatMap(obtenerPortafoliosNoCumplenRequeridos)
                .map(campoPortafolio -> {
                    HashMap<String, String> parametros = new HashMap<>(2);
                    parametros.put("NombreCampo", campoPortafolio.primero.NombreCampo);

                    return obtenerRequisitoEnrolamientoFull(con, idEmpresa, campoPortafolio.segundo.getIdEntidadOrDefault(cliente.getIdEntidad()), "UI_CAMPO_SIN_VALOR", parametros, "PORTAFOLIO", obtenerUbicacionFormulario(con, idEmpresa, campoPortafolio.primero.UbicacionFormulario, campoPortafolio.segundo));
                })
                .collect(Collectors.toList());
        requisitos.addAll(validacionesCamposRequeridos);

        List<RequisitoEnrolamientoFull> validacionCantidadesMinimas = configuracion.obtenerCampos(cliente, "GRILLAPORTAFOLIO")
                .stream()
                .flatMap(obtenerPortafoliosNoCumplenCantidadMinima)
                .map(campoPortafolio -> {
                    HashMap<String, String> parametros = new HashMap<>(4);
                    parametros.put("NombreCampo", campoPortafolio.primero.NombreCampo);
                    parametros.put("CantidadMinima", String.valueOf(campoPortafolio.primero.CantidadMinima));

                    return obtenerRequisitoEnrolamientoFull(con, idEmpresa, campoPortafolio.segundo.getIdEntidadOrDefault(cliente.getIdEntidad()), "UI_CAMPO_NO_CUMPLE_CANTIDAD_MINIMA", parametros, "PORTAFOLIO", obtenerUbicacionFormulario(con, idEmpresa, campoPortafolio.primero.UbicacionFormulario, campoPortafolio.segundo));
                })
                .collect(Collectors.toList());
        requisitos.addAll(validacionCantidadesMinimas);


        		
        List<RequisitoEnrolamientoFull> validacionesIndicadores = configuracion.obtenerCampos(cliente, "INDICADORESPORTAFOLIO")
                .stream()
                .flatMap(campo -> cliente.Portafolios.stream().map(portafolio -> new Tupla<>(campo, portafolio)))
                .filter(campoPortafolio -> {
                    Campo campo = campoPortafolio.primero;
                    Portafolio portafolio = campoPortafolio.segundo;
                    Indicador indicador = portafolio.IndicadoresPorCodigoTipoIndicador.get(campo.CodigoCampoFrontend);
					if (!"S".equals(campo.Requerido) && Objects.nonNull(indicador) && "N".equals(indicador.TieneValorAsignado)) {
						indicador.ValorIndicador = campo.ValorDefectoCampoFormulario;
					}

					if (Objects.nonNull(indicador)) 
						return tieneValorInvalido(campo, indicador, configuracion);
					else
						return false;
					
                })
                .map(campoPortafolio -> {
                    HashMap<String, String> parametros = new HashMap<>(2);
                    parametros.put("NombreCampo", campoPortafolio.primero.NombreCampo);

                    return obtenerRequisitoEnrolamientoFull(con, idEmpresa, campoPortafolio.segundo.getIdEntidadOrDefault(cliente.getIdEntidad()), "UI_CAMPO_SIN_VALOR", parametros, "PORTAFOLIO", obtenerUbicacionFormulario(con, idEmpresa, campoPortafolio.primero.UbicacionFormulario, campoPortafolio.segundo));
                })
                .collect(Collectors.toList());
        requisitos.addAll(validacionesIndicadores);

        long fin = System.currentTimeMillis();
        logger.debug("FIN::ValidarRequisitosEnrolamientoFull.validarPortafolios {}", fin - inicio);

        return requisitos;
    }

    public static void main(String[] args) {
        System.out.println("== INICIO TEST ==");

		String jsonRequest = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":\"21\",\"entidades\":[{\"IdInterno\":2,\"IdCliente\":1425188}]}";
        try (Connection conn = PlataformaUtils.getDESAConnection()) {
            Gson gson = Utils.getGsonBuilder();
            ConsultarFichaSocioNegocio.EntradaConsultarFichaSocioNegocio entradaConsultarFichaSocioNegocio = gson.fromJson(jsonRequest, ConsultarFichaSocioNegocio.EntradaConsultarFichaSocioNegocio.class);
            try {
                ConsultarFichaSocioNegocio consultarFichaSocioNegocio = new ConsultarFichaSocioNegocio();
                ConsultarFichaSocioNegocio.SalidaConsultarFichaSocioNegocio salidaConsultarFichaSocioNegocio = consultarFichaSocioNegocio.logica(conn, entradaConsultarFichaSocioNegocio);
                System.out.println("SalidaConsultarFichaSocioNegocio");
                System.out.println(salidaConsultarFichaSocioNegocio);
                if (salidaConsultarFichaSocioNegocio.P_OUT_ERRORESCONTADOR > 0) {
                    conn.rollback();
                } else {
                    EntradaValidarRequisitosEnrolamientoFull entrada = new EntradaValidarRequisitosEnrolamientoFull();
                    entrada.p_IdEmpresa = 1L;
                    entrada.p_IdUsuario = 4L;
                    entrada.p_UsaReglasNegocio = "S";
                    entrada.p_IdTipoCanalIngreso = "OPT";
                    entrada.p_IdModulo = "SOC";
                    entrada.clientes = salidaConsultarFichaSocioNegocio.P_OUT_RESULTADOPROCESO;
                    ValidarRequisitosEnrolamientoFull validarRequisitosEnrolamientoFull = new ValidarRequisitosEnrolamientoFull();
                    SalidaValidarRequisitosEnrolamientoFull salida = validarRequisitosEnrolamientoFull.logica(conn, entrada);
                    System.out.println("SalidaValidarRequisitosEnrolamientoFull");
                    System.out.println(salida);
                    if (salida.P_OUT_ERRORESCONTADOR > 0) {
                        conn.rollback();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                conn.rollback();
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        System.out.println("== FIN TEST ==");
    }
}
