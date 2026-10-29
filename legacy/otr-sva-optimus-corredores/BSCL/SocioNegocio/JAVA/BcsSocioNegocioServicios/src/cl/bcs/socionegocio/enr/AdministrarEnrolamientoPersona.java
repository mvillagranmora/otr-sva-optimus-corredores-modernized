package cl.bcs.socionegocio.enr;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.enr.CrearClienteFull.EntradaCrearClienteFull;
import cl.bcs.socionegocio.enr.pojo.DatosClienteDatosGenerales;
import cl.bcs.socionegocio.enr.pojo.DireccionElectronicaEnrolamiento;
import cl.bcs.socionegocio.enr.pojo.DireccionFisica;
import cl.bcs.socionegocio.enr.pojo.DocumentoIdentidad;
import cl.bcs.socionegocio.enr.pojo.Enrolamiento;
import cl.bcs.socionegocio.enr.pojo.Telefono;
import cl.bcs.socionegocio.per.CrearPersona;
import cl.bcs.socionegocio.per.CrearPersona.EntradaServicioCrearPersona;
import cl.bcs.socionegocio.per.CrearPersona.SalidaServicioCrearPersona;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.EntradaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.SalidaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.ModificarPersonaDatosGeneralesMasivo;
import cl.bcs.socionegocio.per.pojo.RegistroCambioMasivo;
import cl.bcs.utils.Fecha;

public class AdministrarEnrolamientoPersona {
	
	static final Logger logger = LogManager.getLogger(AdministrarEnrolamientoPersona.class);
	
	public List<DatosClienteDatosGenerales> crearPersona(Connection con, EntradaCrearClienteFull entrada, List<DatosClienteDatosGenerales> borradoresCliente) throws Exception {
		logger.debug("INICIO::AdministrarEnrolamientoPersona.crearPersona");
		long inicio = System.currentTimeMillis();
		
		List<DatosClienteDatosGenerales> borradoresConPersonasNuevas = borradoresCliente
				.parallelStream()
				.filter(borradorCliente->"S".equals(borradorCliente.EsPersonaNueva))
				.collect(Collectors.toList());
		
		for(DatosClienteDatosGenerales borradorCliente : borradoresConPersonasNuevas) {
			cl.bcs.socionegocio.pojo.Enrolamiento persona = obtenerEntidadEnrolamientoPersona(borradorCliente);
			EntradaServicioCrearPersona entradaServicioCrearPersona = new EntradaServicioCrearPersona();
			entradaServicioCrearPersona.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaServicioCrearPersona.p_IdUsuario = entrada.p_IdUsuario;
			entradaServicioCrearPersona.p_IdModulo = entrada.p_IdModulo;
			entradaServicioCrearPersona.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
			entradaServicioCrearPersona.p_ConsultarEnrolamiento = Arrays.asList(persona);
			
			CrearPersona crearPersona = new CrearPersona();
			SalidaServicioCrearPersona salidaServicioCrearPersona = crearPersona.logica(con, entradaServicioCrearPersona); // Se deja dentro de un for dado que el servicio a pesar de ser masivo solo devuelve un id
			borradorCliente.IdPersona = salidaServicioCrearPersona.P_OUT_REFID;
			borradorCliente.IdSocioNegocio = salidaServicioCrearPersona.P_OUT_IDSOCIONEGOCIO;
			
			List<MensajeSalida> mensajesAdvertencia = EnrolamientoUtils.obtenerMensajesSalidaAdvertencia(salidaServicioCrearPersona.P_OUT_ADVERTENCIASDETALLE, borradorCliente.IdInterno);
			List<MensajeSalida> mensajesError = EnrolamientoUtils.obtenerMensajesSalidaError(salidaServicioCrearPersona.P_OUT_ERRORESDETALLE, borradorCliente.IdInterno);
			borradorCliente.MensajesSalida.addAll(mensajesAdvertencia);
			borradorCliente.MensajesSalida.addAll(mensajesError);
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientoPersona.crearPersona", fin - inicio);
		return borradoresCliente;
	}
	
	public List<DatosClienteDatosGenerales> modificarPersona(Connection con, EntradaCrearClienteFull entrada, List<DatosClienteDatosGenerales> borradoresCliente) throws Exception {
		logger.debug("INICIO::AdministrarEnrolamientoPersona.modificarPersona");
		long inicio = System.currentTimeMillis();
		
		EntradaEjecutarCambiosPersona entradaEjecutarCambiosPersona = new EntradaEjecutarCambiosPersona();
		entradaEjecutarCambiosPersona.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaEjecutarCambiosPersona.p_IdModulo = entrada.p_IdModulo;
		entradaEjecutarCambiosPersona.p_IdUsuario = entrada.p_IdUsuario;
		entradaEjecutarCambiosPersona.p_UsaReglasNegocio = "S";
		
		for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
			List<RegistroCambioMasivo> registrosCambios = obtenerRegistrosCambioModificarPersona(borradorCliente.IdPersona, borradorCliente.Enrolamiento);
			if("N".equals(borradorCliente.EsPersonaNueva)) {
				registrosCambios.addAll(obtenerRegistrosCambioModificarPersonaExistente(borradorCliente.IdPersona, borradorCliente.Enrolamiento));
			}
			
			registrosCambios = registrosCambios
					.parallelStream()
					.filter(registroCambio->registroCambio.ValorNuevo != null)
					.collect(Collectors.toList());
			
			entradaEjecutarCambiosPersona.p_RegistroCambio.addAll(registrosCambios);
		}
		
		if(entradaEjecutarCambiosPersona.p_RegistroCambio.size() > 0 ) {
			ModificarPersonaDatosGeneralesMasivo modificarPersonaDatosGeneralesMasivo = new ModificarPersonaDatosGeneralesMasivo();
			SalidaEjecutarCambiosPersona salidaEjecutarCambiosPersona = modificarPersonaDatosGeneralesMasivo.logica(con, entradaEjecutarCambiosPersona);
			
			for(DatosClienteDatosGenerales borradorCliente : borradoresCliente) {
				borradorCliente.MensajesSalida.addAll(salidaEjecutarCambiosPersona.P_OUT_MENSAJES);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientoPersona.modificarPersona", fin - inicio);
		return borradoresCliente;
	}
	
	private List<RegistroCambioMasivo> obtenerRegistrosCambioModificarPersona(Long idPersona, Enrolamiento enrolamiento){
		logger.trace("INICIO::AdministrarEnrolamientoPersona.obtenerRegistrosCambioModificarPersona");
		long inicio = System.currentTimeMillis();
		
		List<RegistroCambioMasivo> registrosCambio = new ArrayList<>();
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(idPersona, "IdGiroPrincipal", enrolamiento.IdGiroPrincipal));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(idPersona, "FechaInicioActividad", enrolamiento.FechaInicioActividad));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(idPersona, "EsExtranjero", enrolamiento.EsExtranjero));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(idPersona, "CodigoTipoCargoEmpleado", enrolamiento.CodigoTipoCargoEmpleado));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(idPersona, "Cargo", enrolamiento.CodigoTipoCargoEmpleado));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(idPersona, "DetalleCargo",enrolamiento.DetalleCargo));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(idPersona, "Empleador", enrolamiento.IdPersonaEmpleador));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(idPersona, "IdProfesion", enrolamiento.IdProfesion));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(idPersona, "IdPersonaRepresentante", enrolamiento.IdPersonaRepresentante));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(idPersona, "IdPaisNacionalidadPrincipal", enrolamiento.IdPaisNacionalidadPrincipal));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(idPersona, "GlobalLegalEntity", enrolamiento.GlobalLegalEntity));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(idPersona, "IdPersonaEmpleador", enrolamiento.IdPersonaEmpleador));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(idPersona, "IdRangoPatrimonio", enrolamiento.IdRangoPatrimonio));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(idPersona, "IdTransparenciaFiscal", enrolamiento.IdTransparenciaFiscal));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(idPersona, "ResidenciaTributariaExtranjera", enrolamiento.ResidenciaTributariaExtranjera));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(idPersona, "IdGiroCIIU", enrolamiento.IdGiroCIIU));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(idPersona, "IdSituacionLaboral", enrolamiento.IdSituacionLaboral));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(idPersona, "IdTipoCanalIngresoCorredor", enrolamiento.IdTipoCanalIngresoCorredor));

		long fin = System.currentTimeMillis();
		logger.trace("FIN::AdministrarEnrolamientoPersona.obtenerRegistrosCambioModificarPersona", fin - inicio);
		return registrosCambio;
	}
	
	private List<RegistroCambioMasivo> obtenerRegistrosCambioModificarPersonaExistente(Long idPersona, Enrolamiento enrolamiento){
		logger.trace("INICIO::AdministrarEnrolamientoPersona.obtenerRegistrosCambioModificarPersonaExistente");
		long inicio = System.currentTimeMillis();
		
		List<RegistroCambioMasivo> registrosCambio = new ArrayList<>();
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(idPersona, "Tipo", enrolamiento.Tipo));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(idPersona, "Nombres", enrolamiento.Nombres));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(idPersona, "ApellidoPaterno", enrolamiento.ApellidoPaterno));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(idPersona, "ApellidoMaterno", enrolamiento.ApellidoMaterno));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(idPersona, "RazonSocial", enrolamiento.RazonSocial));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(idPersona, "IdPaisNacimiento", enrolamiento.IdPaisNacimiento));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(idPersona, "FechaNacimiento", enrolamiento.FechaNacimiento));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(idPersona, "Sexo", enrolamiento.Sexo));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(idPersona, "EstadoCivil", enrolamiento.EstadoCivil));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(idPersona, "RegimenConyugal", enrolamiento.RegimenConyugal));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(idPersona, "IdPersonaConyuge", enrolamiento.IdPersonaConyuge));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(idPersona, "IdSituacionLaboral", enrolamiento.IdSituacionLaboral));
		registrosCambio.add(EnrolamientoUtils.crearRegistroCambio(idPersona, "IdTipoCanalIngresoCorredor", enrolamiento.IdTipoCanalIngresoCorredor));

		long fin = System.currentTimeMillis();
		logger.trace("FIN::AdministrarEnrolamientoPersona.obtenerRegistrosCambioModificarPersonaExistente", fin - inicio);
		return registrosCambio;
	}
	
	private cl.bcs.socionegocio.pojo.Enrolamiento obtenerEntidadEnrolamientoPersona(DatosClienteDatosGenerales borradorCliente){
		logger.trace("INICIO::AdministrarEnrolamientoPersona.obtenerEntidadEnrolamientoPersona");
		long inicio = System.currentTimeMillis();
		
		cl.bcs.socionegocio.pojo.Enrolamiento persona = new cl.bcs.socionegocio.pojo.Enrolamiento();
		persona.IdEnrolamiento  = borradorCliente.Enrolamiento.IdEnrolamiento;
		persona.IdEmpresa  = borradorCliente.Enrolamiento.IdEmpresa;
		persona.IdUsuario  = borradorCliente.Enrolamiento.IdUsuario;
		persona.IdTipoEntidad  = borradorCliente.Enrolamiento.IdTipoEntidad;
		persona.IdEntidadEnrolada  = borradorCliente.Enrolamiento.IdEntidadEnrolada;
		persona.CodigoRangoRenta = borradorCliente.Enrolamiento.CodigoRangoRenta;
		persona.CodigoRangoNivelVenta = borradorCliente.Enrolamiento.CodigoRangoNivelVenta;
		persona.CodigoPropositoTransaccion = borradorCliente.Enrolamiento.CodigoPropositoTransaccion;
		
		DocumentoIdentidad documentoIdentidad = EnrolamientoUtils.obtenerDocumentoIdentidadPorId(borradorCliente.DocumentosIdentidades, borradorCliente.Enrolamiento.IdDocumentoIdentidad);
		persona.IdTipoDocumentoIdentidad  = documentoIdentidad.IdTipoDocumentoIdentidad;
		persona.IdTipoDocumentoIdentidadAsociado  = documentoIdentidad.IdTipoDocumentoIdentidadAsociado;
		persona.DocumentoIdentidad  = documentoIdentidad.DocumentoIdentidad;
		persona.NumeroSerie = documentoIdentidad.NumeroSerie;
		persona.FechaEmision = documentoIdentidad.FechaEmision;
		persona.FechaVencimiento = documentoIdentidad.FechaVencimiento;

		DocumentoIdentidad documentoIdentidadAdicional = EnrolamientoUtils.obtenerDocumentoIdentidadPorId(borradorCliente.DocumentosIdentidades, borradorCliente.Enrolamiento.IdDocumentoIdentidadAdicional);
		if(documentoIdentidadAdicional != null) {
			persona.IdTipoDocumentoIdentidadAdicional  = documentoIdentidadAdicional.IdTipoDocumentoIdentidad;
			persona.DocumentoIdentidadAdicional  = documentoIdentidadAdicional.DocumentoIdentidad;
		}
		
		persona.Tipo  = borradorCliente.Enrolamiento.Tipo;
		persona.Nombres  = borradorCliente.Enrolamiento.Nombres;
		persona.ApellidoPaterno  = borradorCliente.Enrolamiento.ApellidoPaterno;
		persona.ApellidoMaterno  = borradorCliente.Enrolamiento.ApellidoMaterno;
		persona.RazonSocial  = borradorCliente.Enrolamiento.RazonSocial;
		persona.IdPaisNacimiento  = borradorCliente.Enrolamiento.IdPaisNacimiento;
		persona.IdPaisResidencia  = borradorCliente.Enrolamiento.IdPaisResidencia;
		persona.FechaNacimiento  = borradorCliente.Enrolamiento.FechaNacimiento;
		persona.Sexo  = borradorCliente.Enrolamiento.Sexo;
		persona.EstadoCivil  = borradorCliente.Enrolamiento.EstadoCivil;
		persona.IdSituacionLaboral = borradorCliente.Enrolamiento.IdSituacionLaboral;
		persona.IdTipoCanalIngresoCorredor = borradorCliente.Enrolamiento.IdTipoCanalIngresoCorredor;
		
		DireccionFisica direccionFisica = EnrolamientoUtils.obtenerDireccionFisicaPorId(borradorCliente.DireccionesFisica, borradorCliente.Enrolamiento.IdDireccionFisicaPrincipal);
		if(direccionFisica != null) {
			persona.IdCiudad  = direccionFisica.IdCiudad;
			persona.IdDivisionPais  = direccionFisica.IdDivisionPais;
			persona.DireccionCalle  = direccionFisica.Calle;
			persona.DireccionNumero  = direccionFisica.Numero;
			persona.DireccionDepartamento  = direccionFisica.Departamento;
		}
		
		Telefono telefono = EnrolamientoUtils.obtenerTelefonoPorId(borradorCliente.Telefonos, borradorCliente.Enrolamiento.IdTelefonoPrincipal);
		if(telefono != null) {
			persona.IdTipoTelefono  = telefono.IdTipoTelefono;
			persona.Telefono  = telefono.Numero;
		}
		
		DireccionElectronicaEnrolamiento direccionElectronica = EnrolamientoUtils.obtenerDireccionElectronicaPorId(borradorCliente.DireccionesElectronica, borradorCliente.Enrolamiento.IdDireccionElectronicaPrincipal);
		if(direccionElectronica != null) {
			persona.Email  = direccionElectronica.Direccion;
		}
		
		persona.FechaCreacion  = borradorCliente.Enrolamiento.FechaCreacion;
		persona.FechaCreacionStr  = Fecha.toStringDatetime(borradorCliente.Enrolamiento.FechaCreacion);
		persona.FechaModificacion  = borradorCliente.Enrolamiento.FechaModificacion;
		persona.FechaModificacionStr  =Fecha.toStringDatetime(borradorCliente.Enrolamiento.FechaModificacion);
		persona.IdPersonaConyuge  = borradorCliente.Enrolamiento.IdPersonaConyuge;
		persona.RegimenConyugal  = borradorCliente.Enrolamiento.RegimenConyugal;
		persona.IdProfesion = borradorCliente.Enrolamiento.IdProfesion;
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::AdministrarEnrolamientoPersona.obtenerEntidadEnrolamientoPersona", fin - inicio);
		return persona;
	}
	
	public List<DatosClienteDatosGenerales> enrolarPersona(Connection con, EntradaCrearClienteFull entrada, List<DatosClienteDatosGenerales> borradoresCliente) throws Exception{
		logger.debug("INICIO::AdministrarEnrolamientoPersona.enrolarPersona");
		long inicio = System.currentTimeMillis();
		
		borradoresCliente = crearPersona(con, entrada, borradoresCliente);
		borradoresCliente = modificarPersona(con, entrada, borradoresCliente);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::AdministrarEnrolamientoPersona.enrolarPersona", fin - inicio);
		return borradoresCliente;
	}
	
}
