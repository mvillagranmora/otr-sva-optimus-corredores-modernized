package cl.bcs.socionegocio.cnt;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.per.CrearDocumentoIdentidad;
import cl.bcs.socionegocio.per.CrearDocumentoIdentidad.EntradaCrearDocumentoIdentidad;
import cl.bcs.socionegocio.per.CrearDocumentoIdentidad.SalidaCrearDocumentoIdentidad;
import cl.bcs.socionegocio.per.CrearPersona;
import cl.bcs.socionegocio.per.CrearPersona.EntradaServicioCrearPersona;
import cl.bcs.socionegocio.per.CrearPersona.SalidaServicioCrearPersona;
import cl.bcs.socionegocio.pojo.AdvertenciaDetalle;
import cl.bcs.socionegocio.pojo.Enrolamiento;
import cl.bcs.socionegocio.pojo.ErrorDetalle;
import cl.bcs.socionegocio.pojo.SalidaEstandar;
import cl.bcs.socionegocio.soc.ValidarDocumentoIdentidad;
import cl.bcs.socionegocio.soc.ValidarDocumentoIdentidad.SalidaValidarDocumentoIdentidad;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearContraparte implements JavaCall {
	
	private Logger logger = LogManager.getLogger(CrearContraparte.class);
	
	public String SQL_ConsultarIdPersonaUsuario= "SELECT EMP.\"IdPersona\" as \"Id\" FROM \"BSCL.SocioNegocio.BD::EMP.Empleado\" EMP WHERE EMP.\"IdEmpleado\" = :IdEmpleadoEjecutivo";
	
	public String SQL_CREAR_CONTRAPARTE = loadResourceToString("cl/bcs/socionegocio/cnt/sql/CrearContraparte_Insert.sql");
	public String SQL_CREAR_CONTRAPARTE_SOCIONEGOCIO_UPDATE = loadResourceToString("cl/bcs/socionegocio/cnt/sql/CrearContraparte_SocioNegocio_Update.sql");
	public String SQL_CREAR_CONTRAPARTE_ENROLAMIENTO_UPDATE = loadResourceToString("cl/bcs/socionegocio/cnt/sql/CrearContraparte_Enrolamiento_Update.sql");
	private final String SQL_UPDATE_PERSONA = PlataformaUtils.loadResourceToString(
			"cl/bcs/socionegocio/cnt/sql/CrearContraparte_Update_Persona.sql");

	public String SQL_ConsultarExistenciaDocumentoAdicional = "SELECT COUNT(1) AS \"Count\" FROM \"BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDocumentoIdentidad\" WHERE \"DocumentoIdentidad\" = :DocumentoIdentidadAdicional AND \"IdTipoDocumentoIdentidad\" = :TipoDocumentoIdentidadAdicional AND \"IdEmpresa\" = :IdEmpresa";
	public String SQL_ConsultarDocumentoAdicionalPropio = "SELECT COUNT(1) AS \"Count\" FROM \"BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona\" WHERE \"DocumentoIdentidadAdicional\" = :DocumentoIdentidadAdicional AND \"TipoDocumentoIdentidadAdicional\" = :TipoDocumentoIdentidadAdicional AND \"DocumentoIdentidad\" = :DocumentoIdentidad";
	public String SQL_CREAR_CONTRAPARTE_DOCUMENTOIDENTIDADADICIONAL_UPDATE = loadResourceToString("cl/bcs/socionegocio/cli/sql/CrearCliente_DocumentoIdentidadAdicional_Update.sql");
	
	private final String ID_CONCEPTO = "ENRCNT";
	
	public static class EntradaServicioCrearContraparte{
		public long p_IdEmpresa;
		public long p_IdUsuario;
		public String p_IdModulo;
		public List<Enrolamiento> p_ConsultarEnrolamiento;
		public String p_UsaReglasNegocio = "S";
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class SalidaServicioCrearContraparte extends SalidaEstandar{
		public SalidaServicioCrearContraparte(){
			this.P_OUT_REFIDCONTRAPARTE = 0;
			this.P_OUT_REFIDPORTAFOLIO=0;
			this.P_OUT_ADVERTENCIASCONTADOR=0;
			this.P_OUT_ERRORESCONTADOR=0;
			this.P_OUT_ADVERTENCIASDETALLE = new ArrayList<AdvertenciaDetalle>();
			this.P_OUT_ERRORESDETALLE = new ArrayList<ErrorDetalle>();
		}
		public long P_OUT_REFIDCONTRAPARTE;
		public long P_OUT_REFIDPORTAFOLIO;
		public long P_OUT_REFIDPERSONA;
		public long P_OUT_IDSOCIONEGOCIO;
		public String P_OUT_DOCUMENTOIDENTIDAD="";
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Contador{
		public long Count;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class EntidadContraparte extends Enrolamiento{
		public EntidadContraparte(){
			super();
		}
		public long IdContraparte;
		public long IdPersona;
		public String IdEstado;
		public String EsEnrolado;
		public long IdPersonaUsuario;
				
		public void setFromEnrolamiento(Enrolamiento enr){
			this.IdEnrolamiento  = enr.IdEnrolamiento ;
			this.IdEmpresa  = enr.IdEmpresa ;
			this.IdUsuario  = enr.IdUsuario ;
			this.IdTipoEntidad  = enr.IdTipoEntidad ;
			this.IdEntidadEnrolada  = enr.IdEntidadEnrolada ;
			this.IdTipoDocumentoIdentidad  = enr.IdTipoDocumentoIdentidad ;
			this.IdTipoDocumentoIdentidadAsociado  = enr.IdTipoDocumentoIdentidadAsociado ;
			this.DocumentoIdentidad  = enr.DocumentoIdentidad ;
			this.IdTipoDocumentoIdentidadAdicional  = enr.IdTipoDocumentoIdentidadAdicional ;
			this.DocumentoIdentidadAdicional  = enr.DocumentoIdentidadAdicional ;
			this.Tipo  = enr.Tipo ;
			this.Nombres  = enr.Nombres ;
			this.ApellidoPaterno  = enr.ApellidoPaterno ;
			this.ApellidoMaterno  = enr.ApellidoMaterno ;
			this.RazonSocial  = enr.RazonSocial ;
			this.IdPaisNacimiento  = enr.IdPaisNacimiento ;
			this.FechaNacimiento  = enr.FechaNacimiento ;
			this.Sexo  = enr.Sexo ;
			this.EstadoCivil  = enr.EstadoCivil ;
			this.IdCiudad  = enr.IdCiudad ;
			this.IdDivisionPais  = enr.IdDivisionPais ;
			this.IdTipoTelefono  = enr.IdTipoTelefono ;
			this.Telefono  = enr.Telefono ;
			this.Email  = enr.Email ;
			this.DireccionCalle  = enr.DireccionCalle ;
			this.DireccionNumero  = enr.DireccionNumero ;
			this.DireccionDepartamento  = enr.DireccionDepartamento ;
			this.FechaCreacion  = enr.FechaCreacion ;
			this.FechaCreacionStr  = enr.FechaCreacionStr ;
			this.FechaModificacion  = enr.FechaModificacion ;
			this.FechaModificacionStr  = enr.FechaModificacionStr ;
			this.IdPersonaConyuge  = enr.IdPersonaConyuge ;
			this.RegimenConyugal  = enr.RegimenConyugal ;
			this.CLI_IdCategoriaInversionista  = enr.CLI_IdCategoriaInversionista ;
			this.CLI_IdTipoCliente  = enr.CLI_IdTipoCliente ;
			this.CLI_IdEmpleadoEjecutivo  = enr.CLI_IdEmpleadoEjecutivo ;
			this.CLI_CodigoAdicionalHolding  = enr.CLI_CodigoAdicionalHolding ;
			this.CLI_IdClaseCliente  = enr.CLI_IdClaseCliente ;
			this.CLI_ClienteFinal = enr.CLI_ClienteFinal;
			this.POR_IdPerfilComercial  = enr.POR_IdPerfilComercial;
			this.POR_IdPersonaBrokerExtranjero = enr.POR_IdPersonaBrokerExtranjero;			
			this.EMP_CodigoEmpleado  = enr.EMP_CodigoEmpleado ;
			this.EMP_CodigoTipoCargoEmpleado  = enr.EMP_CodigoTipoCargoEmpleado ;
			this.EMP_IdEmpleadoSupervisor  = enr.EMP_IdEmpleadoSupervisor ;
			this.EMP_IdSucursal  = enr.EMP_IdSucursal ;
			this.EMP_FechaInicioCargo  = enr.EMP_FechaInicioCargo ;
			this.EMP_FechaTerminoCargo  = enr.EMP_FechaTerminoCargo ;
			this.CNT_CodigoInscripcion  = enr.CNT_CodigoInscripcion ;
			this.CNT_Emisor  = enr.CNT_Emisor ;
			this.CNT_Depositos  = enr.CNT_Depositos ;
			this.CNT_Bolsa  = enr.CNT_Bolsa ;
			this.CNT_Corredora  = enr.CNT_Corredora ;
			this.CNT_CamaraDeCompensacion  = enr.CNT_CamaraDeCompensacion ;
			this.CNT_Banco  = enr.CNT_Banco ;
			this.CNT_Boveda  = enr.CNT_Boveda ;
			this.CNT_Otros = enr.CNT_Otros;
			this.PRO_IdAreaProveedor  = enr.PRO_IdAreaProveedor ;
			this.EMP_CargoIndefinido = enr.EMP_CargoIndefinido;
			this.EsRutInterno = enr.EsRutInterno;
		}
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class EntidadCrearContraparteUpdate{
		public long p_IdDocumentoIdentidadAdicional;
		public long p_IdPersona;
	}
	
	public static class Ids{
		public long Id;
	}
	
	public static class ErrorDetalleCall{
		public String Codigo;
		public String Descripcion;
	}
	
	public static class AdvertenciaDetalleCall{
		public String Codigo;
		public String RequiereAutorizacion;
		public String Descripcion;
	}
	
	public static class EntidadCrearContraparteUpdateSocioNegocio{
		public long IdSocioDeNegocio;
		public long IdContraparte;
	}
	
	public static class EntidadCrearContraparteUpdateEnrolamiento{
		public long IdEnrolamiento;
		public long IdContraparte;
	}
		
	public static class EntidadUpdatePersona { 
		public long IdContraparte; 
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaServicioCrearContraparte in = gson.fromJson(jin.toString(), EntradaServicioCrearContraparte.class);
		SalidaServicioCrearContraparte out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaServicioCrearContraparte logica(Connection conn, EntradaServicioCrearContraparte in) throws Exception {
		logger.info("INICIO Crear contraparte");
		SalidaServicioCrearContraparte salida = new SalidaServicioCrearContraparte();
		SQL sql = new SQL();
		if(in.p_ConsultarEnrolamiento.size()>0){
			logger.debug("Hay entidades que enrolar");
			for(Enrolamiento contraparte : in.p_ConsultarEnrolamiento){
				if(contraparte.IdTipoEntidad!=null && contraparte.IdTipoEntidad.equals("CONTRAPARTE")){
					String fechaSistema = Utils.getFechaSistema(conn, Utils.getSchema(), in.p_IdEmpresa);
					
					ValidarDocumentoIdentidad validarDocumentoIdentidad = new ValidarDocumentoIdentidad();
					SalidaValidarDocumentoIdentidad documentoValido = validarDocumentoIdentidad.logica(conn, in.p_IdEmpresa, contraparte.IdTipoEntidad, contraparte.IdTipoDocumentoIdentidad, contraparte.DocumentoIdentidad, contraparte.Tipo);
					
					//Crear Persona
					CrearPersona crearPersona = new CrearPersona();
					EntradaServicioCrearPersona entradaCrearPersona = new EntradaServicioCrearPersona();
					entradaCrearPersona.p_IdEmpresa = in.p_IdEmpresa;
					entradaCrearPersona.p_IdModulo = in.p_IdModulo;
					entradaCrearPersona.p_IdUsuario = in.p_IdUsuario;
					entradaCrearPersona.p_ConsultarEnrolamiento = Arrays.asList(contraparte);
					logger.debug("llamar servicio crear persona");
					SalidaServicioCrearPersona salidaCrearPersona = crearPersona.logica(conn, entradaCrearPersona);
					//obtener advertencias de la creacion de persona
					for(AdvertenciaDetalle advertencia : salidaCrearPersona.P_OUT_ADVERTENCIASDETALLE){
						salida.addAdvertencia(advertencia.Codigo, advertencia.Descripcion);
					}
					
					if(salidaCrearPersona.P_OUT_ERRORESCONTADOR==0 || salidaCrearPersona.P_OUT_REFID != 0 ){
						logger.debug("Persona existe o se creo, crear contraparte");
						//ingresar contraparte
						
						long idPersona = salidaCrearPersona.P_OUT_REFID;
						long idSocioNegocio = salidaCrearPersona.P_OUT_IDSOCIONEGOCIO;												
						long idContraparte = Utils.getSecuencia(conn, "BSCL.SocioNegocio.BD.Secuencias::Contraparte", (long) 1).get(0);
						salida.P_OUT_REFIDPERSONA = idPersona;
						salida.P_OUT_IDSOCIONEGOCIO = idSocioNegocio;
						salida.P_OUT_DOCUMENTOIDENTIDAD = salidaCrearPersona.P_OUT_DOCUMENTOIDENTIDAD;
						
						String requiereAutorizacion = Utils.getRequiereEventoAutorizacion(conn, in.p_IdEmpresa, ID_CONCEPTO);
						requiereAutorizacion = (in.p_UsaReglasNegocio.equals("N")) ? "N" : requiereAutorizacion;
								
						HashMap<String, Object> params = new HashMap<String, Object>();
						params.put("IdEmpleadoEjecutivo", contraparte.CLI_IdEmpleadoEjecutivo);
						List<Ids> idPersonaUsuario = sql.query(conn, SQL_ConsultarIdPersonaUsuario, params, Ids.class);
						
						EntidadContraparte entidadContraparte = new EntidadContraparte();
						entidadContraparte.setFromEnrolamiento(contraparte);
						entidadContraparte.IdContraparte = idContraparte;
						entidadContraparte.IdPersona = idPersona;
						entidadContraparte.IdEmpresa= in.p_IdEmpresa;
						entidadContraparte.IdEstado = "ACT"; 
						entidadContraparte.EsEnrolado = (requiereAutorizacion.equals("S")) ? "P" : "S";
						entidadContraparte.IdPersonaUsuario = (idPersonaUsuario!=null && idPersonaUsuario.size()> 0)? idPersonaUsuario.get(0).Id :0;
						
						
						logger.debug("entidad contrapar: "+entidadContraparte);
						int[] resContraparte = sql.batch(conn, SQL_CREAR_CONTRAPARTE, Arrays.asList(entidadContraparte), EntidadContraparte.class);
						if(resContraparte!=null && resContraparte.length>0 && resContraparte[0]==1){
							logger.debug("contraparte creado correctamente.");
							salida.P_OUT_REFIDCONTRAPARTE = idContraparte;
							salida.P_OUT_DOCUMENTOIDENTIDAD = entidadContraparte.DocumentoIdentidad;
							System.out.println("RES CNT: " + idContraparte);
						}else{
							salida.addError("CONTRAPARTE_NO_PUEDE_SER_INGRESADO","CONTRAPARTE_NO_PUEDE_SER_INGRESADO");
							logger.debug(salida.P_OUT_ERRORESDETALLE.get(salida.P_OUT_ERRORESDETALLE.size()-1));
							return salida;
						}
												
						if(salida.P_OUT_ERRORESCONTADOR==0){
							logger.debug("Actualizar socio de negocio con IdContraparte");
							EntidadCrearContraparteUpdateSocioNegocio updateSocioNegocio = new EntidadCrearContraparteUpdateSocioNegocio();
							updateSocioNegocio.IdSocioDeNegocio = idSocioNegocio;
							updateSocioNegocio.IdContraparte = idContraparte;
							int[] resUpdateSocioNegocio = sql.batch(conn, SQL_CREAR_CONTRAPARTE_SOCIONEGOCIO_UPDATE, Arrays.asList(updateSocioNegocio), EntidadCrearContraparteUpdateSocioNegocio.class);									
							if(resUpdateSocioNegocio==null || (resUpdateSocioNegocio.length>0 && resUpdateSocioNegocio[0]==0)){
								salida.addError("UI_ERROR_ACTUALIZAR_SOCIONEGOCIO", "UI_ERROR_ACTUALIZAR_SOCIONEGOCIO");
								logger.debug(salida.P_OUT_ERRORESDETALLE.get(salida.P_OUT_ERRORESDETALLE.size()-1));
							}
							
													
							
							Long idDocumentoIdentidadAdicional = null;
							EntradaCrearDocumentoIdentidad entradaDocumentoIdentidad = new EntradaCrearDocumentoIdentidad();
							if(documentoValido.CodigoPais.equals("PE") && !contraparte.DocumentoIdentidadAdicional.isEmpty() && !contraparte.DocumentoIdentidadAdicional.equals("")){
								logger.debug("identificar si existe un documento de identidad adicional (PERU)");
								
								params = new HashMap<>();
								params.put("DocumentoIdentidadAdicional", contraparte.DocumentoIdentidadAdicional);
								params.put("TipoDocumentoIdentidadAdicional", contraparte.IdTipoDocumentoIdentidadAdicional);
								params.put("IdEmpresa", in.p_IdEmpresa);
								List<Contador> contadorDocumentoAdicioanl = sql.query(conn, SQL_ConsultarExistenciaDocumentoAdicional, params, Contador.class);
								
								if(!contadorDocumentoAdicioanl.isEmpty() && contadorDocumentoAdicioanl.get(0).Count==1){
									
									params = new HashMap<>();
									params.put("DocumentoIdentidad", contraparte.DocumentoIdentidad);
									params.put("DocumentoIdentidadAdicional", contraparte.DocumentoIdentidadAdicional);
									params.put("TipoDocumentoIdentidadAdicional", contraparte.IdTipoDocumentoIdentidadAdicional);
									//params.put("IdEmpresa", in.p_IdEmpresa);
									contadorDocumentoAdicioanl = sql.query(conn, SQL_ConsultarDocumentoAdicionalPropio, params, Contador.class);
									if(contadorDocumentoAdicioanl.isEmpty() || contadorDocumentoAdicioanl.get(0).Count!=1){
										salida.addError("UI_EXISTE_DOCUMENTO_ADICIONAL", Diccionario.getMensaje(conn, in.p_IdEmpresa, "UI_EXISTE_DOCUMENTO_ADICIONAL", new HashMap<>()));
										logger.debug(salida.P_OUT_ERRORESDETALLE.get(salida.P_OUT_ERRORESDETALLE.size()-1));
										return salida;
									}
								}else{
									
									//ingresar documento identidad adicional.
									idDocumentoIdentidadAdicional = Utils.getSecuencia(conn, "BSCL.SocioNegocio.BD.Secuencias::DocumentoIdentidad", (long) 1).get(0);
									CrearDocumentoIdentidad crearDocumentoIdentidad = new CrearDocumentoIdentidad();
									entradaDocumentoIdentidad = new EntradaCrearDocumentoIdentidad();
									entradaDocumentoIdentidad.p_IdUsuario = in.p_IdUsuario;
									entradaDocumentoIdentidad.p_IdEmpresa = in.p_IdEmpresa;
									entradaDocumentoIdentidad.p_IdModulo = in.p_IdModulo;
									entradaDocumentoIdentidad.p_UsaReglasNegocio = "N";
									
									entradaDocumentoIdentidad.p_IdPersona = idPersona;
									entradaDocumentoIdentidad.p_IdTipoDocumentoIdentidad = contraparte.IdTipoDocumentoIdentidadAdicional;
									entradaDocumentoIdentidad.p_IdTipoDocumentoIdentidadAsociado = contraparte.IdTipoDocumentoIdentidadAdicional;
									entradaDocumentoIdentidad.p_DocumentoIdentidad = contraparte.DocumentoIdentidadAdicional;
									entradaDocumentoIdentidad.p_TipoPersona = contraparte.Tipo;
									SalidaCrearDocumentoIdentidad salidaDocumentoIdentidad = crearDocumentoIdentidad.logica(conn, entradaDocumentoIdentidad);
									if(salidaDocumentoIdentidad.P_OUT_ERRORESCONTADOR==0){
										idDocumentoIdentidadAdicional = salidaDocumentoIdentidad.p_out_RefId;
										System.out.println("RES DOC ADI: " + idDocumentoIdentidadAdicional);
										
										//actualizar los ids de la persona con cada entidad que fue ingresada
										EntidadCrearContraparteUpdate updatePersona = new EntidadCrearContraparteUpdate();
										updatePersona.p_IdDocumentoIdentidadAdicional = (idDocumentoIdentidadAdicional!=null) ? idDocumentoIdentidadAdicional : 0;
										updatePersona.p_IdPersona = idPersona;
										sql.batch(conn, SQL_CREAR_CONTRAPARTE_DOCUMENTOIDENTIDADADICIONAL_UPDATE, Arrays.asList(updatePersona), EntidadCrearContraparteUpdate.class);
										
									}else{
										for (ErrorDetalle error : salidaDocumentoIdentidad.P_OUT_ERRORESDETALLE) {
											salida.addError(error.Codigo, error.Descripcion);
										}
										return salida;
									}
									
								}
							}

							if (requiereAutorizacion.equals("N")) {
								logger.debug("Actualizando persona a estado ACT por no requerir autorizacion");

								EntidadUpdatePersona updatePersona = new EntidadUpdatePersona();
								updatePersona.IdContraparte = idContraparte;

								int[] resUpdatePersona = sql.batch(conn, SQL_UPDATE_PERSONA,
										Arrays.asList(updatePersona), EntidadUpdatePersona.class);
										
								if (resUpdatePersona == null || resUpdatePersona.length == 0
										|| resUpdatePersona[0] == 0) {
									salida.addError("UI_ERROR_ACTUALIZAR_PERSONA", "UI_ERROR_ACTUALIZAR_PERSONA");
									logger.debug(
											salida.P_OUT_ERRORESDETALLE.get(salida.P_OUT_ERRORESDETALLE.size() - 1));
									return salida;
								}
							}

							if(requiereAutorizacion.equals("S")){
								logger.debug("Generar evento de autorizacion");
								
								CrearEventoAutorizacionMasivo evtAutorizacion = new CrearEventoAutorizacionMasivo();
								
								CrearEventoAutorizacionMasivo.ServiceEntrada entradaEvtAutorizacion = new CrearEventoAutorizacionMasivo.ServiceEntrada();
								entradaEvtAutorizacion.P_IDEMPRESA = in.p_IdEmpresa;
								entradaEvtAutorizacion.P_IDMODULO = in.p_IdModulo;
								entradaEvtAutorizacion.P_IDUSUARIO = in.p_IdUsuario;
								entradaEvtAutorizacion.P_SIMULAROPERACION = "N";
								entradaEvtAutorizacion.P_USAREGLASNEGOCIO = "N";
								
								CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS evento = new CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS();
								// Enrolamiento de contraparte debe rutear por ENRCNT
								evento.IdTipoEvento = ID_CONCEPTO;
								evento.IdModuloSolicita = in.p_IdModulo;
								evento.IdUsuarioSolicita = in.p_IdUsuario;
								evento.Fecha = fechaSistema;
								evento.IdConcepto = ID_CONCEPTO;
								evento.NumeroOperacionConcepto = String.valueOf(idContraparte);
								evento.FechaOperacionConcepto = fechaSistema;
								//evento.IdCliente = idContraparte;
								evento.Glosa = (contraparte.Tipo.equals("J")) ? contraparte.DocumentoIdentidad + " / " + contraparte.RazonSocial : contraparte.DocumentoIdentidad + " / " + contraparte.Nombres + " " + contraparte.ApellidoPaterno + " " + contraparte.ApellidoMaterno;
								
								entradaEvtAutorizacion.P_TABLAEVENTOS = Arrays.asList(evento);
								
								CrearEventoAutorizacionMasivo.ServiceSalida salidaAut = evtAutorizacion.logica(conn, entradaEvtAutorizacion);
								salida.P_OUT_ADVERTENCIASCONTADOR++;
								salida.P_OUT_ADVERTENCIASDETALLE.add(new AdvertenciaDetalle(
									"REQUIERE_AUTORIZACION",
									"S",
									Diccionario.getMensaje(conn, in.p_IdEmpresa, "REQUIERE_AUTORIZACION", new HashMap<>())
								));
								
								for(CrearEventoAutorizacionMasivo.ObjADVERTENCIASDETALLE advertencia : salidaAut.P_OUT_ADVERTENCIASDETALLE){
									salida.addAdvertencia(advertencia.Codigo, advertencia.Descripcion);
								}
								for (CrearEventoAutorizacionMasivo.ObjERRORESDETALLE error : salidaAut.P_OUT_ERRORESDETALLE) {
									salida.addError(error.Codigo, error.Descripcion);
								}
							}
						}else{
							salida.addError("CONTRAPARTE_NO_PUEDE_SER_ENROLADO","CONTRAPARTE_NO_PUEDE_SER_ENROLADO");
							logger.debug(salida.P_OUT_ERRORESDETALLE.get(salida.P_OUT_ERRORESDETALLE.size()-1));
							
						}
						
					}else{
						//error al crear la persona
						for(ErrorDetalle errorPersona : salidaCrearPersona.P_OUT_ERRORESDETALLE){
							salida.addError(errorPersona.Codigo, errorPersona.Descripcion);
							logger.debug(salida.P_OUT_ERRORESDETALLE.get(salida.P_OUT_ERRORESDETALLE.size()-1));
						}
					}
				}else{
					salida.addError("CONTRAPARTE_NO_PUEDE_SER_ENROLADO","CONTRAPARTE_NO_PUEDE_SER_ENROLADO");
					logger.debug(salida.P_OUT_ERRORESDETALLE.get(salida.P_OUT_ERRORESDETALLE.size()-1));
				}
			}
		}else{
			salida.addError("CONTRAPARTE_NO_PUEDE_SER_ENROLADO","CONTRAPARTE_NO_PUEDE_SER_ENROLADO");
			logger.debug(salida.P_OUT_ERRORESDETALLE.get(salida.P_OUT_ERRORESDETALLE.size()-1));
		}
		logger.info("Termino Crear contraparte");
		return salida;
	}

	public static void main(String[] args) {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json ="{\"p_ConsultarEnrolamiento\":[{\"IdEnrolamiento\":0,\"IdEmpresa\":1,\"IdUsuario\":9,\"IdTipoEntidad\":\"CONTRAPARTE\",\"IdEntidadEnrolada\":0,\"IdTipoDocumentoIdentidad\":\"R\",\"IdTipoDocumentoIdentidadAsociado\":\"R\",\"DocumentoIdentidad\":\"6664955-5\",\"IdTipoDocumentoIdentidadAdicional\":\"R\",\"DocumentoIdentidadAdicional\":\"\",\"Tipo\":\"J\",\"Nombres\":\"\",\"ApellidoPaterno\":\"\",\"ApellidoMaterno\":\"\",\"RazonSocial\":\"SZCL\",\"IdPaisNacimiento\":44,\"FechaNacimiento\":\"0001-01-01T03:29:20.000Z\",\"Sexo\":\"M\",\"EstadoCivil\":\"\",\"IdCiudad\":173,\"IdDivisionPais\":343,\"IdTipoTelefono\":\"FIJO\",\"Telefono\":\"+5621234567\",\"Email\":\"PROBANDOCONTRAPARTE@CONTRAPARTE.CL\",\"DireccionCalle\":\"CALLE DE PRUEBA\",\"DireccionNumero\":\"123456\",\"DireccionDepartamento\":\"\",\"FechaCreacion\":\"2021-05-14T19:10:50.424Z\",\"FechaModificacion\":\"2021-05-14T19:10:50.424Z\",\"IdPersonaConyuge\":0,\"RegimenConyugal\":\"\",\"CLI_IdCategoriaInversionista\":\"\",\"CLI_IdTipoCliente\":\"\",\"CLI_IdClaseCliente\":\"\",\"CLI_IdEmpleadoEjecutivo\":0,\"CLI_CodigoAdicionalHolding\":\"\",\"POR_IdPerfilComercial\":\"\",\"EMP_CodigoEmpleado\":\"\",\"EMP_CodigoTipoCargoEmpleado\":\"\",\"EMP_IdEmpleadoSupervisor\":0,\"EMP_IdSucursal\":0,\"EMP_FechaInicioCargo\":\"0001-01-01\",\"EMP_FechaTerminoCargo\":\"0001-01-01\",\"CNT_CodigoInscripcion\":\"4444\",\"CNT_Emisor\":\"N\",\"CNT_Depositos\":\"N\",\"CNT_Bolsa\":\"N\",\"CNT_Corredora\":\"N\",\"CNT_CamaraDeCompensacion\":\"N\",\"CNT_Banco\":\"N\",\"CNT_Boveda\":\"N\",\"CNT_Otros\":\"444\",\"PRO_IdAreaProveedor\":0,\"EMP_CargoIndefinido\":\"\",\"EsRutInterno\":\"N\"}],\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"SOC\"}";
			
			EntradaServicioCrearContraparte Objin = gson.fromJson(json, EntradaServicioCrearContraparte.class);

			CrearContraparte demo = new CrearContraparte();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			
			try {
				SalidaServicioCrearContraparte ObjOut = demo.logica(con, Objin);

				System.out.println(ObjOut.toString());
				System.out.println("SALIDA");
				System.out.println("IdContraparte creada: " + ObjOut.P_OUT_REFIDCONTRAPARTE);
				System.out.println("IdPersona creada/asociada: " + ObjOut.P_OUT_REFIDPERSONA);
				System.out.println("IdSocioNegocio: " + ObjOut.P_OUT_IDSOCIONEGOCIO);
				System.out.println("DocumentoIdentidad: " + ObjOut.P_OUT_DOCUMENTOIDENTIDAD);
				con.rollback();
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
			con.rollback();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}

}
