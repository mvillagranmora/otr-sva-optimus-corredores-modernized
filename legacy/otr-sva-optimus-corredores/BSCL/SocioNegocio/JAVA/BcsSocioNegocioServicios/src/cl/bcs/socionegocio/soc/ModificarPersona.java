package cl.bcs.socionegocio.soc;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.per.ModificarPersonaDatosGeneralesMasivo;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.EntradaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.SalidaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.pojo.RegistroCambioMasivo;
import cl.bcs.socionegocio.soc.pojo.SocioNegocio;

public class ModificarPersona implements EjecutarCambiosSocioNegocio{

	static final Logger logger = LogManager.getLogger(ModificarPersona.class);
	
	public static class EntradaModificarPersona {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public Long p_IdPersona;
		public SocioNegocio p_SocioNegocio;
		
	}
	
	public static class SalidaModificarPersona {
		public List<MensajeSalida>				P_OUT_MENSAJES;
		public Long								P_OUT_ERRORESCONTADOR;
		
		public SalidaModificarPersona() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;	
		}
	}
	
	public SalidaModificarPersona logica(Connection conn, EntradaModificarPersona entrada) throws Exception {
		
		logger.info("INICIO::ModificarPersona");
		Long inicioServicio = System.currentTimeMillis();
		
		SalidaModificarPersona out = new SalidaModificarPersona();
		SalidaEjecutarCambiosPersona salidaModificarPersona = new SalidaEjecutarCambiosPersona();
		
		salidaModificarPersona = modificarPersona(conn, entrada);
		if(!salidaModificarPersona.P_OUT_MENSAJES.isEmpty()) {
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salidaModificarPersona.P_OUT_MENSAJES);
			out.P_OUT_MENSAJES = salidaModificarPersona.P_OUT_MENSAJES;
			return out;
		}
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarPersona", finServicio - inicioServicio);
		
		return out;
	}
	
	private SalidaEjecutarCambiosPersona modificarPersona(Connection conn, EntradaModificarPersona in) throws Exception {
		
		logger.info("INICIO::ModificarPersona.modificarPersona");
		Long inicioServicio = System.currentTimeMillis();
		
		ModificarPersonaDatosGeneralesMasivo personaDatosGenerales = new ModificarPersonaDatosGeneralesMasivo();
		EntradaEjecutarCambiosPersona entrada = new EntradaEjecutarCambiosPersona();
		
		List<RegistroCambioMasivo> registroCambio = obtenerRegistroCambioPersona(in.p_SocioNegocio, in.p_IdPersona);
		SalidaEjecutarCambiosPersona salida = new SalidaEjecutarCambiosPersona();
		
		if(!registroCambio.isEmpty()) {
			entrada.p_IdEmpresa = in.p_IdEmpresa;
			entrada.p_IdModulo = "SOC";
			entrada.p_IdUsuario = in.p_IdUsuario;
			entrada.p_UsaReglasNegocio = "N";
			entrada.p_RegistroCambio = registroCambio;
			salida = personaDatosGenerales.logica(conn, entrada);
		}
	
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarPersona.modificarPersona", finServicio - inicioServicio);
		
		return salida;	
	}
	
	private List<RegistroCambioMasivo> obtenerRegistroCambioPersona(SocioNegocio socioNegocio, Long IdPersona) {
		
		logger.info("INICIO::ModificarPersona.obtenerRegistroCambioPersona");
		Long inicioServicio = System.currentTimeMillis();
		
		List<RegistroCambioMasivo> registroCambioPersona = new ArrayList<RegistroCambioMasivo>();
		
		if(socioNegocio.IdTipoPersona.equals("N")) {
			if(!socioNegocio.Nombres.isEmpty()){
				RegistroCambioMasivo cambioPersona = new RegistroCambioMasivo();
				cambioPersona.CampoModificado	= "Nombres";
				cambioPersona.ValorNuevo		= socioNegocio.Nombres.trim();
				cambioPersona.IdEntidad			= IdPersona;
				registroCambioPersona.add(cambioPersona);
			}
			
			if(!socioNegocio.ApellidoPaterno.isEmpty()){
				RegistroCambioMasivo cambioPersona = new RegistroCambioMasivo();
				cambioPersona.CampoModificado	= "ApellidoPaterno";
				cambioPersona.ValorNuevo		= socioNegocio.ApellidoPaterno.trim();
				cambioPersona.IdEntidad			= IdPersona;
				registroCambioPersona.add(cambioPersona);	
			}
			
			if(!socioNegocio.ApellidoMaterno.isEmpty()){
				RegistroCambioMasivo cambioPersona = new RegistroCambioMasivo();
				cambioPersona.CampoModificado	= "ApellidoMaterno";
				cambioPersona.ValorNuevo		= socioNegocio.ApellidoMaterno.trim();
				cambioPersona.IdEntidad			= IdPersona;
				registroCambioPersona.add(cambioPersona);
			}
		}
		
		if(socioNegocio.IdTipoPersona.equals("J")) {
			if(!socioNegocio.RazonSocial.isEmpty()){
				RegistroCambioMasivo cambioPersona = new RegistroCambioMasivo();
				cambioPersona.CampoModificado	= "RazonSocial";
				cambioPersona.ValorNuevo		= socioNegocio.RazonSocial.trim();
				cambioPersona.IdEntidad			= IdPersona;
				registroCambioPersona.add(cambioPersona);
			}
		}
		
		if(socioNegocio.IdGiro > 0L){
			RegistroCambioMasivo cambioPersona = new RegistroCambioMasivo();
			cambioPersona.CampoModificado	= "IdGiroPrincipal";
			cambioPersona.ValorNuevo		= String.valueOf(socioNegocio.IdGiro);
			cambioPersona.IdEntidad			= IdPersona;
			registroCambioPersona.add(cambioPersona);
		}
		
		if(socioNegocio.IdPaisNacimientoConstitucion > 0L){
			RegistroCambioMasivo cambioPersona = new RegistroCambioMasivo();
			cambioPersona.CampoModificado	= "IdPaisNacimiento";
			cambioPersona.ValorNuevo		= String.valueOf(socioNegocio.IdPaisNacimientoConstitucion);
			cambioPersona.IdEntidad			= IdPersona;
			registroCambioPersona.add(cambioPersona);
		}
		
		if(socioNegocio.IdPaisResidenciaCasaMatriz > 0L){
			RegistroCambioMasivo cambioPersona = new RegistroCambioMasivo();
			cambioPersona.CampoModificado	= "IdPaisResidencia";
			cambioPersona.ValorNuevo		= String.valueOf(socioNegocio.IdPaisResidenciaCasaMatriz);
			cambioPersona.IdEntidad			= socioNegocio.IdEntidad;
			registroCambioPersona.add(cambioPersona);
		}
		
		if(!socioNegocio.FechaNacimientoConstitucion.isEmpty()){
			RegistroCambioMasivo cambioPersona = new RegistroCambioMasivo();
			cambioPersona.CampoModificado	= "FechaNacimiento";
			cambioPersona.ValorNuevo		= socioNegocio.FechaNacimientoConstitucion.trim();
			cambioPersona.IdEntidad			= IdPersona;
			registroCambioPersona.add(cambioPersona);
		}
		
		if(!socioNegocio.FechaDefuncion.isEmpty()){
			RegistroCambioMasivo cambioPersona = new RegistroCambioMasivo();
			cambioPersona.CampoModificado	= "FechaDefuncion";
			cambioPersona.ValorNuevo		= socioNegocio.FechaDefuncion.trim();
			cambioPersona.IdEntidad			= IdPersona;
			registroCambioPersona.add(cambioPersona);
		}
		
		if(!socioNegocio.IdGenero.isEmpty()){
			RegistroCambioMasivo cambioPersona = new RegistroCambioMasivo();
			cambioPersona.CampoModificado	= "Sexo";
			cambioPersona.ValorNuevo		= socioNegocio.IdGenero.trim();
			cambioPersona.IdEntidad			= IdPersona;
			registroCambioPersona.add(cambioPersona);
		}
		
		if(!socioNegocio.EstadoCivil.isEmpty()){
			RegistroCambioMasivo cambioPersona = new RegistroCambioMasivo();
			cambioPersona.CampoModificado	= "EstadoCivil";
			cambioPersona.ValorNuevo		= socioNegocio.EstadoCivil.trim();
			cambioPersona.IdEntidad			= IdPersona;
			registroCambioPersona.add(cambioPersona);
		}
		
		if(socioNegocio.IdProfesion > 0L){
			RegistroCambioMasivo cambioPersona = new RegistroCambioMasivo();
			cambioPersona.CampoModificado	= "IdProfesion";
			cambioPersona.ValorNuevo		= String.valueOf(socioNegocio.IdProfesion);
			cambioPersona.IdEntidad			= IdPersona;
			registroCambioPersona.add(cambioPersona);
		}
		
		if(!socioNegocio.ResidenciaTributariaExtranjera.isEmpty()){
			RegistroCambioMasivo cambioPersona = new RegistroCambioMasivo();
			cambioPersona.CampoModificado	= "ResidenciaTributariaExtranjera";
			cambioPersona.ValorNuevo		= socioNegocio.ResidenciaTributariaExtranjera;
			cambioPersona.IdEntidad			= IdPersona;
			registroCambioPersona.add(cambioPersona);
		}
		
		if(!socioNegocio.FechaInicioActividad.isEmpty()){
			RegistroCambioMasivo cambioPersona = new RegistroCambioMasivo();
			cambioPersona.CampoModificado	= "FechaInicioActividad";
			cambioPersona.ValorNuevo		= socioNegocio.FechaInicioActividad;
			cambioPersona.IdEntidad			= IdPersona;
			registroCambioPersona.add(cambioPersona);
		}
		
		if(!socioNegocio.GlobalLegalEntity.isEmpty()){
			RegistroCambioMasivo cambioPersona = new RegistroCambioMasivo();
			cambioPersona.CampoModificado	= "GlobalLegalEntity";
			cambioPersona.ValorNuevo		= socioNegocio.GlobalLegalEntity;
			cambioPersona.IdEntidad			= IdPersona;
			registroCambioPersona.add(cambioPersona);
		}
		
		if(socioNegocio.IdPaisNacionalidadPrincipal > 0L){
			RegistroCambioMasivo cambioPersona = new RegistroCambioMasivo();
			cambioPersona.CampoModificado	= "IdPaisNacionalidadPrincipal";
			cambioPersona.ValorNuevo		= String.valueOf(socioNegocio.IdPaisNacionalidadPrincipal);
			cambioPersona.IdEntidad			= IdPersona;
			registroCambioPersona.add(cambioPersona);
		}
		
		if(!socioNegocio.Cargo.isEmpty()){
			RegistroCambioMasivo cambioPersona = new RegistroCambioMasivo();
			cambioPersona.CampoModificado	= "CodigoTipoCargoEmpleado";
			cambioPersona.ValorNuevo		= socioNegocio.Cargo;
			cambioPersona.IdEntidad			= IdPersona;
			registroCambioPersona.add(cambioPersona);
		}
		
		if(socioNegocio.Empleador > 0L){
			RegistroCambioMasivo cambioPersona = new RegistroCambioMasivo();
			cambioPersona.CampoModificado	= "IdPersonaEmpleador";
			cambioPersona.ValorNuevo		= String.valueOf(socioNegocio.Empleador);
			cambioPersona.IdEntidad			= IdPersona;
			registroCambioPersona.add(cambioPersona);
		}
		
		if(socioNegocio.IdRangoPatrimonio > 0L){
			RegistroCambioMasivo cambioPersona = new RegistroCambioMasivo();
			cambioPersona.CampoModificado	= "IdRangoPatrimonio";
			cambioPersona.ValorNuevo		= String.valueOf(socioNegocio.IdRangoPatrimonio);
			cambioPersona.IdEntidad			= IdPersona;
			registroCambioPersona.add(cambioPersona);
		}
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarPersona.obtenerRegistroCambioPersona", finServicio - inicioServicio);
		
		return registroCambioPersona;
	}
	
}
