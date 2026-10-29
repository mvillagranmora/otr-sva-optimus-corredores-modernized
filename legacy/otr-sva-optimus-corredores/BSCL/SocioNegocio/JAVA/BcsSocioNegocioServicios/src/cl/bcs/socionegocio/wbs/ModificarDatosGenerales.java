package cl.bcs.socionegocio.wbs;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.frm.pojo.FormularioCanal;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.EntradaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.SalidaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.CrearPersona;
import cl.bcs.socionegocio.per.ModificarPersonaDatosGeneralesMasivo;
import cl.bcs.socionegocio.per.CrearPersona.EntradaServicioCrearPersona;
import cl.bcs.socionegocio.per.CrearPersona.SalidaServicioCrearPersona;
import cl.bcs.socionegocio.per.pojo.RegistroCambioMasivo;
import cl.bcs.socionegocio.pojo.Enrolamiento;
import cl.bcs.socionegocio.wbs.WebServiceUtils.EntidadManipulada;
import cl.bcs.socionegocio.wbs.WebServiceUtils.SalidaWBS;
import cl.bcs.socionegocio.wbs.pojo.DatosPersonaDatosGenerales;
import cl.bcs.socionegocio.wbs.pojo.Empleador;
import cl.bcs.socionegocio.wbs.pojo.EntradaAdministrarWS;
import cl.bcs.socionegocio.wbs.pojo.SalidaAdministrarWS;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarDatosGenerales implements JavaCall {
	static final Logger logger = LogManager.getLogger(ModificarDatosGenerales.class);
	
	private String SQL_SELECT_PERSONA = loadResourceToString("cl/bcs/socionegocio/wbs/sql/ModificarDatosGenerales_SelectPersonaDatosGenerales.sql");
	private String SQL_SELECT_PERSONA_DATOS_EMPLEADOR = loadResourceToString("cl/bcs/socionegocio/wbs/sql/ModificarDatosGenerales_SelectPersonaDatosEmpleador.sql");
	private String ID_MODULO = "SOC";
	
	public static class EntradaModificarDatosGenerales extends EntradaAdministrarWS {
		public List<DatosPersonaDatosGenerales> p_PersonaDatosGenerales;
		
		public EntradaModificarDatosGenerales(){
			this.p_PersonaDatosGenerales = new ArrayList<DatosPersonaDatosGenerales>();
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaModificarDatosGenerales jin = gson.fromJson(in.toString(), EntradaModificarDatosGenerales.class);
		SalidaWBS out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public SalidaWBS logica(Connection conn, EntradaModificarDatosGenerales entrada) throws Exception {
		logger.info("INICIO::ModificarDatosGenerales");
		long inicioServicio = System.currentTimeMillis();
		
		SalidaWBS salida =  new SalidaWBS();
		
		List<DatosPersonaDatosGenerales> personasDatosGenerales = obtenerDatosPersonaDatosGenerales(conn,entrada);

		for(DatosPersonaDatosGenerales datosPersona : personasDatosGenerales) {

		    SalidaAdministrarWS out = new SalidaAdministrarWS();
		    EntidadManipulada entidadModificada = new EntidadManipulada();
		    entidadModificada.IdEntidad = datosPersona.IdPersona;
		    entidadModificada.TipoEntidad = "DATOS GENERALES PERSONA";
		    datosPersona.EsBorrador = "N";
		    Long idPersonaEmpleador = 0L;
		   
		    if(datosPersona.TipoEnrolamiento == null) {
				entidadModificada = WebServiceUtils.noEsCliente(entidadModificada);
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadModificada);
				continue;
		    }
		    
		    Empleador datosEmpleador = entrada.p_PersonaDatosGenerales.parallelStream()
					.filter(persona -> persona.IdPersona.equals(datosPersona.IdPersona))
					.findFirst().orElse(new DatosPersonaDatosGenerales()).Empleador;
		    
		    if(!entidadEmpleadorEsVacia(datosEmpleador)) {
				SalidaWBS salidaCrearEmpleador = new SalidaWBS();
				
				salidaCrearEmpleador = crearEmpleador(conn, datosPersona, entrada);
			   	if(salidaCrearEmpleador.P_OUT_ERRORESCONTADOR > 0) {
			   		salida.P_OUT_ENTIDADES_MANIPULADAS.addAll(salidaCrearEmpleador.P_OUT_ENTIDADES_MANIPULADAS);
			   		salida.P_OUT_ERRORESCONTADOR += salidaCrearEmpleador.P_OUT_ERRORESCONTADOR;
			   		return salida;
			    } else {
			    	salida.P_OUT_ENTIDADES_MANIPULADAS.addAll(salidaCrearEmpleador.P_OUT_ENTIDADES_MANIPULADAS);
			    	idPersonaEmpleador = salidaCrearEmpleador.P_OUT_ENTIDADES_MANIPULADAS.stream().findFirst().orElse(new EntidadManipulada()).IdEntidad;
			   	}
			}
		    
		    entrada.p_PersonaDatosGenerales.parallelStream()
	    	.filter(per -> per.IdPersona.equals(datosPersona.IdPersona))
	    	.findFirst().orElse(new DatosPersonaDatosGenerales()).IdPersonaEmpleador = idPersonaEmpleador;
		    List<DatosPersonaDatosGenerales> mergeCambios = new ArrayList<>();
		    DatosPersonaDatosGenerales nuevosCampos = entrada.p_PersonaDatosGenerales.parallelStream()
			    	.filter(per -> per.IdPersona.equals(datosPersona.IdPersona))
			    	.findFirst().orElse(new DatosPersonaDatosGenerales());
		    if (nuevosCampos.IdSituacionLaboral!=null && nuevosCampos.IdSituacionLaboral.equals("") ) {
		    	nuevosCampos.IdSituacionLaboral = null;
		    }
		    WebServiceUtils.merge(datosPersona, nuevosCampos);
		    mergeCambios.add(datosPersona);
		    
		    datosPersona.CodigoTipoCargoEmpleado = (datosPersona.Cargo.equals(null) || datosPersona.Cargo.equals("")) ? datosPersona.CodigoTipoCargoEmpleado  : datosPersona.Cargo;
		    
		    FormularioCanal formulario = new FormularioCanal();
		    formulario.IdEmpresa = entrada.p_IdEmpresa;
		    formulario.CodigoFormulario = WebServiceUtils.obtenerCodigoFormulario(conn, datosPersona.Tipo, datosPersona.TipoEnrolamiento, "DATOS_GENERALES_PERSONA");
		    formulario.IdTipoCanal = entrada.p_IdTipoCanal;
		    
		    out = validarCampoSituacionLaboralEmpleador(conn, datosPersona,  entrada, out);
		    
		    if(out.P_OUT_ERRORESCONTADOR > 0) {
		    	entidadModificada.IdEntidad=datosPersona.IdPersona;
		    	entidadModificada.TipoEntidad = "DATOS GENERALES PERSONA";
				entidadModificada = WebServiceUtils.agregarMensajesCamposRequeridos(entidadModificada, out);
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadModificada);
				salida.P_OUT_ERRORESCONTADOR += out.P_OUT_ERRORESCONTADOR;
				return salida;
		    } 
		    
		    out = entrada.validarCamposObligatoriosEntrada(conn, mergeCambios, DatosPersonaDatosGenerales.class, formulario, out);
		  
		    if(out.P_OUT_ERRORESCONTADOR > 0) {
				entidadModificada = WebServiceUtils.agregarMensajesCamposRequeridos(entidadModificada, out);
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadModificada);
				salida.P_OUT_ERRORESCONTADOR += out.P_OUT_ERRORESCONTADOR;
				continue;
		    } 
		    
		    List<DatosPersonaDatosGenerales> masivoDeMentira =  new ArrayList<>();
		    masivoDeMentira.add(datosPersona);
		    ModificarPersonaDatosGeneralesMasivo modificarPersonaDatosGeneralesMasivo = new ModificarPersonaDatosGeneralesMasivo();
		    EntradaEjecutarCambiosPersona entradaEjecutarCambiosPersona = new EntradaEjecutarCambiosPersona();
		    entradaEjecutarCambiosPersona.p_IdEmpresa = entrada.p_IdEmpresa;
		    entradaEjecutarCambiosPersona.p_IdModulo = entrada.p_IdModulo;
		    entradaEjecutarCambiosPersona.p_IdUsuario = entrada.p_IdUsuario;
		    entradaEjecutarCambiosPersona.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		    entradaEjecutarCambiosPersona.p_RegistroCambio = crearRegistroCambio(masivoDeMentira);
		    
		    SalidaEjecutarCambiosPersona salidaEjecutarCambiosPersona = modificarPersonaDatosGeneralesMasivo.logica(conn, entradaEjecutarCambiosPersona);

		    if( !salidaEjecutarCambiosPersona.P_OUT_MENSAJES.isEmpty() ) {
				entidadModificada.Mensajes.addAll(salidaEjecutarCambiosPersona.P_OUT_MENSAJES);
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadModificada);
				continue;
		    }
		    
		    if(entidadModificada.Mensajes.isEmpty()) {
				entidadModificada = WebServiceUtils.agregarMensajeOk(entidadModificada,"MODIFICADA");
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadModificada);
		    }
		}
		salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ENTIDADES_MANIPULADAS.parallelStream().reduce(0L,
				(sum, ent) -> sum += ent.Mensajes.parallelStream().filter(mensaje ->PlataformaUtils.MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)).count(),
				(sum1, sum2) -> sum1 + sum2);
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarDatosGenerales", finServicio - inicioServicio);
		return salida;
		
	}

	private Empleador consultarDatosEmpleadorBD(Connection conn, String documentoIdentidad) throws Exception {
		logger.debug("INICIO::ModificarDatosGenerales.consultarDatosEmpleadorBD");
	    Long inicio = System.currentTimeMillis();
	    
		HashMap<String, Object> params = new HashMap<String, Object>(2);
		params.put("DocumentoIdentidad", documentoIdentidad );
				
		SQL sql = new SQL();
		Empleador empleador = (sql.query(conn, SQL_SELECT_PERSONA_DATOS_EMPLEADOR , params, Empleador.class)).parallelStream().findFirst().orElse(new Empleador());
		
	    long fin = System.currentTimeMillis();
		logger.info("FIN::ModificarDatosGenerales.consultarDatosEmpleadorBD", fin - inicio);
		return empleador;
	}

	private boolean entidadEmpleadorEsVacia(Empleador datosEmpleador) throws Exception{
		logger.debug("INICIO::ModificarDatosGenerales.entidadEmpleadorEsVacia");
	    Long inicio = System.currentTimeMillis();
	    
	    if(!datosEmpleador.DocumentoIdentidad.equals("") || !datosEmpleador.TipoDocumentoIdentidad.equals("") || !datosEmpleador.ApellidoMaterno.equals("") || !datosEmpleador.ApellidoPaterno.equals("")
	    		|| !datosEmpleador.Nombres.equals("") || !datosEmpleador.RazonSocial.equals("") || !datosEmpleador.IdTipoPersona.equals("")) {
	    	return false;
	    }
	    
	    long fin = System.currentTimeMillis();
		logger.info("FIN::ModificarDatosGenerales.entidadEmpleadorEsVacia", fin - inicio);
		return true;
	}
	
	
	private SalidaAdministrarWS validarCampoSituacionLaboralEmpleador(Connection con, DatosPersonaDatosGenerales datosPersona, EntradaModificarDatosGenerales entrada,SalidaAdministrarWS out) throws Exception {
		if (datosPersona.Empleador!=null){
			if(!datosPersona.Empleador.DocumentoIdentidad.isEmpty() && !datosPersona.Empleador.DocumentoIdentidad.equals("") ) {
				if (!datosPersona.IdSituacionLaboral.equals("EMPL") || datosPersona.IdSituacionLaboral.equals(null) || datosPersona.IdSituacionLaboral.equals("") ){
					HashMap<String, String> params = new HashMap<>(2);
					out.P_OUT_ERRORESCONTADOR ++;
					out.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, "UI_ERROR_CAMPO_SITUACION_LABORAL_EMPLEADOR", params, con, entrada.p_IdEmpresa));								
				}
			}
		}
		return out;
	}

	private SalidaWBS crearEmpleador(Connection conn, DatosPersonaDatosGenerales datosPersona, EntradaModificarDatosGenerales in) throws Exception {
		    logger.debug("INICIO::ModificarDatosGenerales.crearEmpleador");
		    Long inicio = System.currentTimeMillis();
		    
		    SalidaWBS salida = new SalidaWBS();
		    SalidaAdministrarWS out = new SalidaAdministrarWS();
		    EntidadManipulada entidadAValidar = new EntidadManipulada();
		    
		    
			CrearPersona crearPersona = new CrearPersona();
			CrearPersona.EntradaServicioCrearPersona entradaServicioCrearPersona = new EntradaServicioCrearPersona(); 
			entradaServicioCrearPersona.p_IdEmpresa = in.p_IdEmpresa;
			entradaServicioCrearPersona.p_IdModulo = ID_MODULO;
			entradaServicioCrearPersona.p_IdUsuario = in.p_IdUsuario;
			Enrolamiento enrolamiento = new Enrolamiento();
			
			String documentoIdentidadEmpleador = in.p_PersonaDatosGenerales.parallelStream()
					.filter(persona -> persona.IdPersona.equals(datosPersona.IdPersona))
					.findFirst().orElse(new DatosPersonaDatosGenerales()).Empleador.DocumentoIdentidad;
			
			Empleador datosEmpleadorDesdeEntrada = in.p_PersonaDatosGenerales.parallelStream()
					.filter(persona -> persona.IdPersona.equals(datosPersona.IdPersona))
					.findFirst().orElse(new DatosPersonaDatosGenerales()).Empleador;
			
	    	Empleador datosEmpleadorDesdeBD = consultarDatosEmpleadorBD(conn, documentoIdentidadEmpleador) ;
	    	
	    	if(entidadEmpleadorEsVacia(datosEmpleadorDesdeBD)) {
	    		enrolamiento.ApellidoMaterno = datosEmpleadorDesdeEntrada.ApellidoMaterno;
				enrolamiento.ApellidoPaterno = datosEmpleadorDesdeEntrada.ApellidoPaterno;
				enrolamiento.DocumentoIdentidad = datosEmpleadorDesdeEntrada.DocumentoIdentidad;
				enrolamiento.IdTipoDocumentoIdentidad = datosEmpleadorDesdeEntrada.TipoDocumentoIdentidad;
				enrolamiento.Nombres = datosEmpleadorDesdeEntrada.Nombres;
				enrolamiento.RazonSocial = datosEmpleadorDesdeEntrada.RazonSocial;
				enrolamiento.Tipo = datosEmpleadorDesdeEntrada.IdTipoPersona;
				enrolamiento.IdTipoEntidad = "PERSONA";
				enrolamiento.IdEmpresa = in.p_IdEmpresa;
				enrolamiento.IdTipoDocumentoIdentidadAsociado = "";
	    	}else {
	    		enrolamiento.ApellidoMaterno = datosEmpleadorDesdeBD.ApellidoMaterno;
				enrolamiento.ApellidoPaterno = datosEmpleadorDesdeBD.ApellidoPaterno;
				enrolamiento.DocumentoIdentidad = datosEmpleadorDesdeBD.DocumentoIdentidad;
				enrolamiento.IdTipoDocumentoIdentidad = datosEmpleadorDesdeBD.TipoDocumentoIdentidad;
				enrolamiento.Nombres = datosEmpleadorDesdeBD.Nombres;
				enrolamiento.RazonSocial = datosEmpleadorDesdeBD.RazonSocial;
				enrolamiento.Tipo = datosEmpleadorDesdeBD.IdTipoPersona;
				enrolamiento.IdTipoEntidad = "PERSONA";
				enrolamiento.IdEmpresa = in.p_IdEmpresa;
				enrolamiento.IdTipoDocumentoIdentidadAsociado = "";
	    	}
			
			List<Enrolamiento> datosAValidar = new ArrayList<Enrolamiento>();
			
			datosAValidar.add(enrolamiento);
			
			FormularioCanal formulario = new FormularioCanal();
		    formulario.IdEmpresa = in.p_IdEmpresa;
		    formulario.CodigoFormulario = WebServiceUtils.obtenerCodigoFormulario(conn, datosPersona.Tipo, datosPersona.TipoEnrolamiento, "DATOS_GENERALES_PERSONA_DATOS_EMPLEADOR");
		    formulario.IdTipoCanal = in.p_IdTipoCanal;
		    
			out = in.validarCamposObligatoriosEntrada(conn, datosAValidar, Enrolamiento.class, formulario, out);

		    if(out.P_OUT_ERRORESCONTADOR > 0) {
		    	entidadAValidar = WebServiceUtils.agregarMensajesCamposRequeridos(entidadAValidar, out);
		    	entidadAValidar.IdEntidad = 0L;
		    	entidadAValidar.TipoEntidad = "EMPLEADOR";
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(entidadAValidar);
				salida.P_OUT_ERRORESCONTADOR += out.P_OUT_ERRORESCONTADOR;
				return salida;
		    }
		    
		    if(!entidadEmpleadorEsVacia(datosEmpleadorDesdeBD)) {
		    	EntidadManipulada empleadorSeleccionado = new EntidadManipulada();
		    	empleadorSeleccionado.IdEntidad = datosEmpleadorDesdeBD.IdEmpleador;
		    	empleadorSeleccionado.TipoEntidad = "EMPLEADOR";
				WebServiceUtils.agregarMensajeOk(empleadorSeleccionado, "SELECCIONADA");
				salida.P_OUT_ENTIDADES_MANIPULADAS.add(empleadorSeleccionado);
		    }else {
		    	entradaServicioCrearPersona.p_ConsultarEnrolamiento = new ArrayList<Enrolamiento>(Collections.singletonList(enrolamiento)) ;
				
				SalidaServicioCrearPersona salidaServicioCrearPersona = crearPersona.logica(conn, entradaServicioCrearPersona);		
				
				if(salidaServicioCrearPersona.P_OUT_ERRORESCONTADOR > 0 ) {
					//TODO: si entidad existe pasar idEmpleador a salida -- revisar
					if(salidaServicioCrearPersona.P_OUT_REFID > 0) {
						EntidadManipulada empleadorCreado = new EntidadManipulada();
						empleadorCreado.IdEntidad = salidaServicioCrearPersona.P_OUT_REFID;
						empleadorCreado.TipoEntidad = "EMPLEADOR";
						WebServiceUtils.agregarMensajeOk(empleadorCreado, "SELECCIONADA");
						salida.P_OUT_ENTIDADES_MANIPULADAS.add(empleadorCreado);
					}else {
						salida.P_OUT_ERRORESCONTADOR += salidaServicioCrearPersona.P_OUT_ERRORESCONTADOR;
						salida.P_OUT_ENTIDADES_MANIPULADAS = salidaServicioCrearPersona.P_OUT_ERRORESDETALLE.parallelStream().map(err -> {
							EntidadManipulada entidadManipulada = new EntidadManipulada();
							MensajeSalida mensajeSalida = new MensajeSalida();
							mensajeSalida.codigo = err.Codigo;
							mensajeSalida.descripcion = err.Descripcion;
							mensajeSalida.id = 0L;
							mensajeSalida.tipo = "ERROR";
							entidadManipulada.Mensajes.add(mensajeSalida);
							return entidadManipulada;
						}).collect(Collectors.toList());					
					}
				}else {
					EntidadManipulada empleadorCreado = new EntidadManipulada();
					empleadorCreado.IdEntidad = salidaServicioCrearPersona.P_OUT_REFID;
					empleadorCreado.TipoEntidad = "EMPLEADOR";
					WebServiceUtils.agregarMensajeOk(empleadorCreado, "AGREGADA");
					salida.P_OUT_ENTIDADES_MANIPULADAS.add(empleadorCreado);
				}
		    }
			
			Long fin = System.currentTimeMillis();
			logger.debug("FIN::ModificarDatosGenerales.crearEmpleador {}", fin - inicio);
			
		return salida;
	}
	

	private List<RegistroCambioMasivo> crearRegistroCambio(List<DatosPersonaDatosGenerales> datosGenerales) {
		logger.trace("INICIO::ModificarDatosGenerales.crearRegistroCambio");
		long inicio = System.currentTimeMillis();
		
		List<RegistroCambioMasivo> registrosCambio = new ArrayList<>();
		
		for(DatosPersonaDatosGenerales persona : datosGenerales) {
			
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(persona.IdPersona, "Nombres", persona.Nombres));
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(persona.IdPersona, "ApellidoPaterno", persona.ApellidoPaterno));
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(persona.IdPersona, "ApellidoMaterno", persona.ApellidoMaterno));
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(persona.IdPersona, "IdPaisNacimiento", persona.IdPaisNacimiento));
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(persona.IdPersona, "IdPaisResidencia", persona.IdPaisResidencia));
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(persona.IdPersona, "IdTransparenciaFiscal", persona.IdTransparenciaFiscal));
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(persona.IdPersona, "ResidenciaTributariaExtranjera", persona.ResidenciaTributariaExtranjera));
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(persona.IdPersona, "FechaNacimiento", persona.FechaNacimiento));
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(persona.IdPersona, "FechaDefuncion", persona.FechaDefuncion));
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(persona.IdPersona, "Sexo", persona.Sexo));
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(persona.IdPersona, "EsExtranjero", persona.EsExtranjero));
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(persona.IdPersona, "EstadoCivil", persona.EstadoCivil));
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(persona.IdPersona, "IdPersonaConyuge", persona.IdPersonaConyuge));
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(persona.IdPersona, "RegimenConyugal", persona.RegimenConyugal));
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(persona.IdPersona, "Cargo", persona.Cargo));
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(persona.IdPersona, "IdProfesion", persona.IdProfesion));
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(persona.IdPersona, "IdPaisNacionalidadPrincipal", persona.IdPaisNacionalidadPrincipal));
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(persona.IdPersona, "IdPersonaEmpleador",  persona.IdPersonaEmpleador));
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(persona.IdPersona, "CodigoTipoCargoEmpleado",persona.CodigoTipoCargoEmpleado));
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(persona.IdPersona, "DetalleCargo", persona.DetalleCargo));
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(persona.IdPersona, "IdRangoPatrimonio", persona.IdRangoPatrimonio));
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(persona.IdPersona, "IdGiroPrincipal", persona.IdGiroPrincipal));
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(persona.IdPersona, "RazonSocial", persona.RazonSocial));
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(persona.IdPersona, "FechaInicioActividad", persona.FechaInicioActividad));
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(persona.IdPersona, "IdPersonaRepresentante", persona.IdPersonaRepresentante));
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(persona.IdPersona, "GlobalLegalEntity", persona.GlobalLegalEntity));
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(persona.IdPersona, "IdNombreFantasiaPrincipal", persona.IdNombreFantasiaPrincipal));
			registrosCambio.add(WebServiceUtils.crearRegistroCambio(persona.IdPersona, "IdSituacionLaboral", persona.IdSituacionLaboral));	
			if("N".equalsIgnoreCase(persona.Tipo)) {
				registrosCambio.add(WebServiceUtils.crearRegistroCambio(persona.IdPersona, "CodigoPropositoTransaccion", persona.CodigoPropositoTransaccion));
				registrosCambio.add(WebServiceUtils.crearRegistroCambio(persona.IdPersona, "CodigoRangoRenta", persona.CodigoRangoRenta));
			}
			if("J".equalsIgnoreCase(persona.Tipo)) {
				registrosCambio.add(WebServiceUtils.crearRegistroCambio(persona.IdPersona, "CodigoRangoNivelVenta", persona.CodigoRangoNivelVenta));
			}

		}
		
		registrosCambio = registrosCambio.stream().filter(registro ->  registro.ValorNuevo !=null && !registro.ValorNuevo.equals("")).collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarDatosGenerales.crearRegistroCambio", fin - inicio);
		return registrosCambio;
	}

	private List<DatosPersonaDatosGenerales> obtenerDatosPersonaDatosGenerales(Connection con, EntradaModificarDatosGenerales entrada) throws Exception {
		logger.debug("INICIO::ModificarDatosGenerales.obtenerDatosPersonaDatosGenerales");
		long inicio = System.currentTimeMillis();
		
		List<Long> idsPersonas = entrada.p_PersonaDatosGenerales.stream()
				  .map(persona -> persona.IdPersona)
				  .distinct()
				  .collect(Collectors.toList());
		
		HashMap<String, Object> params = new HashMap<String, Object>(2);
		params.put("IdPersonas", idsPersonas);
		
		SQL sql = new SQL();
		List<DatosPersonaDatosGenerales> datosPersonaDatosGenerales = sql.query(con, SQL_SELECT_PERSONA, params, DatosPersonaDatosGenerales.class);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarDatosGenerales.obtenerDatosPersonaDatosGenerales", fin - inicio);
		return datosPersonaDatosGenerales;
	}

	
	public static void main(String args[]) throws SQLException {
		System.out.println("== TEST ==");		                  
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();			
			
			String jsonEntrada = "{\r\n"
					+ "  \"p_PersonaDatosGenerales\": [\r\n"
					+ "    {\r\n"
					+ "      \"IdPersona\": 1752554,\r\n"
					+ "      \"ApellidoPaterno\": \"JARA\",\r\n"
					+ "      \"ApellidoMaterno\": \"JARA\",\r\n"
					+ "      \"FechaNacimiento\": \"1970-08-25\"\r\n"
					+ "    }\r\n"
					+ "  ],\r\n"
					+ "  \"p_IdEmpresa\": 1,\r\n"
					+ "  \"p_IdUsuario\": 4\r\n"
					+ "}";
			
			String jsonReq = "{\r\n"
					+ "  \"p_PersonaDatosGenerales\": [\r\n"
					+ "    {\r\n"
					+ "      \"IdPersona\": 1774617,\r\n"
					+ "      \"Empleador\": {\r\n"
//					+ "          \"IdTipoPersona\": \"N\",\r\n"
					+ "          \"TipoDocumentoIdentidad\": \"R\",\r\n"
					+ "          \"DocumentoIdentidad\": \"10000287-4\"\r\n"//24310715-6 
//					+ "          \"Nombres\": \"EMPLEA\",\r\n"
//					+ "          \"ApellidoPaterno\": \"WBS\",\r\n"
//					+ "          \"ApellidoMaterno\": \"QUEPASA\",\r\n"
//					+ "          \"RazonSocial\": \"\"\r\n"
					+ "       }\r\n"
					+ "    }\r\n"
					+ "  ],\r\n"
					+ "  \"p_IdEmpresa\": 1,\r\n"
					+ "  \"p_IdUsuario\": 4\r\n"
					+ "}";
			String jsonReqFullJur = "{\r\n"
					+ "  \"p_PersonaDatosGenerales\": [\r\n"
					+ "    {\r\n"
					+ "      \"IdPersona\": 1732307\r\n"
					+ "    }\r\n"
					+ "  ],\r\n"
					+ "  \"p_IdEmpresa\": 1,\r\n"
					+ "  \"p_IdUsuario\": 4\r\n"
					+ "}";
			EntradaModificarDatosGenerales entradaModificarDatosGenerales = gson.fromJson(jsonReq, EntradaModificarDatosGenerales.class);
			ModificarDatosGenerales modificarDatosGenerales = new ModificarDatosGenerales();				
			try {
				SalidaWBS salida= modificarDatosGenerales.logica(con, entradaModificarDatosGenerales);
//				con.commit();
				con.rollback();
				con.close();
				System.out.println(salida.toString());
			}
			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
				con.close();
			}			
    	}
		System.out.println("== END ==");
	}
	
}
