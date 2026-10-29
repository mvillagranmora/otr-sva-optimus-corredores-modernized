package cl.bcs.socionegocio.soc;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import cl.bcs.socionegocio.pojo.Validacion;
import cl.bcs.utils.StringUtils;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.per.CrearDireccionElectronica;
import cl.bcs.socionegocio.per.CrearDireccionFisica;
import cl.bcs.socionegocio.per.CrearDocumentoIdentidad;
import cl.bcs.socionegocio.per.CrearTelefono;
import cl.bcs.socionegocio.per.EliminarEntidadPersona.TieneCantidadMinima;
import cl.bcs.socionegocio.pojo.AdvertenciaDetalle;
import cl.bcs.socionegocio.pojo.ErrorDetalle;
import cl.bcs.socionegocio.soc.CrearSocioNegocio.IdSecuencias;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;

public class ModificarSocioNegocio implements JavaCall
{
	static final Logger logger = LogManager.getLogger(ModificarSocioNegocio.class);
	//*****************************************************//
	// NOTAS :
	// 1.- p_IdUsuario ES STRING O LONG
	// 2.- EL Tipo DE LA DireccionElectronica NO SE MODIFICA
	
	//-------------------------------------------------
	// QUERIES
	//-------------------------------------------------
	private final String SQL_SELECT_PERSONA_CLIENTE						= loadResourceToString("cl/bcs/socionegocio/soc/sql/ModificarSocioNegocio_SelectPersonaCliente.sql");
	private final String SQL_CREAR_TABLA_TEMPORAL_DETALLE				= loadResourceToString("cl/bcs/socionegocio/soc/sql/ModificarSocioNegocio_CrearTablaTemporalDetalle.sql");
	private final String SQL_INSERTAR_TABLA_TEMPORAL_DETALLE			= loadResourceToString("cl/bcs/socionegocio/soc/sql/ModificarSocioNegocio_InsertarTablaTemporalDetalle.sql");
	private final String SQL_ELIMINAR_TABLA_TEMPORAL_DETALLE			= loadResourceToString("cl/bcs/socionegocio/soc/sql/ModificarSocioNegocio_EliminarTablaTemporalDetalle.sql");
	private final String SQL_SELECT_DOCUMENTO_IDENTIDAD					= loadResourceToString("cl/bcs/socionegocio/soc/sql/ModificarSocioNegocio_SelectDocumentoIdentidad.sql");
	private final String SQL_SELECT_INDICADORES							= loadResourceToString("cl/bcs/socionegocio/soc/sql/CrearSocioNegocio_SelectIndicadores.sql");
	private final String SQL_SELECT_INDICADORES_EXISTE					= loadResourceToString("cl/bcs/socionegocio/soc/sql/CrearSocioNegocio_SelectIndicadoresExiste.sql");
	private final String SQL_SELECT_INDICADORES_PERSONA					= loadResourceToString("cl/bcs/socionegocio/soc/sql/CrearSocioNegocio_SelectIndicadoresPersona.sql");

	private final String SQL_CONTADOR_DIRECCIONES_FISICAS				= loadResourceToString("cl/bcs/socionegocio/per/sql/EliminarRegistroTablasPersona_SelectContadorDireccionesFisicas.sql");
	private final String SQL_CONTADOR_DIRECCIONES_ELECTRONICAS			= loadResourceToString("cl/bcs/socionegocio/per/sql/EliminarRegistroTablasPersona_SelectContadorDireccionesElectronicas.sql");
	private final String SQL_CONTADOR_TELEFONOS							= loadResourceToString("cl/bcs/socionegocio/per/sql/EliminarRegistroTablasPersona_SelectContadorTelefonos.sql");
	private final String SQL_CONTADOR_CUENTAS_BANCARIAS					= loadResourceToString("cl/bcs/socionegocio/per/sql/EliminarRegistroTablasPersona_SelectContadorCuentasBancarias.sql");
	private final String SQL_CONTADOR_TARJETAS_BANCARIAS				= loadResourceToString("cl/bcs/socionegocio/per/sql/EliminarRegistroTablasPersona_SelectContadorTarjetasBancarias.sql");
	
	private static final Map<String, String> conceptosCodigoCampo = new HashMap<>(32);

	private final String SQL_SELECT_TIPO_ENROLAMIENTO_PERSONA = loadResourceToString("cl/bcs/socionegocio/per/sql/EliminarRegistroTablasPersona_SelectTiposEnrolamientoPersona.sql");;
	private final String SQL_SELECT_TIENE_MINIMO = loadResourceToString("cl/bcs/socionegocio/per/sql/EliminarRegistroTablasPersona_SelectTieneCantidadMinima.sql");;
	
	static {
		conceptosCodigoCampo.put("DOCUMENTOS_IDENTIDAD", "INGRESAR_DOCUMENTOS_IDENTIDAD_TABLA");
		conceptosCodigoCampo.put("CUENTAS_BANCARIAS", "INGRESAR_CUENTAS_BANCARIAS_TABLA");
		conceptosCodigoCampo.put("TARJETAS_BANCARIAS", "INGRESAR_TARJETAS_BANCARIAS_TABLA");
		conceptosCodigoCampo.put("TELEFONOS", "INGRESAR_TELEFONOS_TABLA");
		conceptosCodigoCampo.put("DIRECCIONES_FISICAS", "INGRESAR_DIRECCIONES_FISICAS_TABLA");
		conceptosCodigoCampo.put("DIRECCIONES_ELECTRONICAS", "INGRESAR_DIRECCIONES_ELECTRONICAS_TABLA");
		conceptosCodigoCampo.put("GRUPO_DIRECCIONES_ELECTRONICAS", "INGRESAR_GRUPOS_DIRECCIONES_ELECTRONICAS_TABLA");
	}
	//-------------------------------------------------
	// CLASES DE ENTRADA 
	//-------------------------------------------------
	public static class ModificarSocioNegocio_Entrada
	{
		public Long 				p_IdEmpresa;
		public Long					p_IdUsuario; // preguntar si es String o Long
		public String				p_ClaveUsuario;
		public List<SocioNegocio>	p_SocioNegocio;
		public String				p_IdTipoCanal;
		
		public ModificarSocioNegocio_Entrada()
		{
			p_SocioNegocio = new ArrayList<SocioNegocio>();
			p_IdTipoCanal = "";
		}
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	public static class SocioNegocio
	{
		public Long							IdEntidad					= 0L;
		public String						TipoEntidad					= "";
		public String						TipoDocumentoIdentidad		= "";
		public String						DocumentoIdentidad			= "";
		public String						Nombres						= "";
		public String						ApellidoPaterno				= "";
		public String						ApellidoMaterno				= "";
		public String						RazonSocial					= "";
		public Long							IdGiro						= 0L;
		public String						FechaNacimientoConstitucion	= "";
		public String						IdEstadoCivil				= "";
		public Long							IdPaisNacimiento			= 0L;
		public String						IdRegimenConyugal			= "";
		public Long							IdPaisNacionalidad			= 0L;
		public String						IdGenero					= "";
		public String						IdTipoPersona				= "";
		public Long							IdPaisResidencia			= 0L;
		public Long							IdProfesion					= 0L;
		public String						IdTipoRelacion				= "";	//para indicar si esta persona es una persona relacionada
		public Long							IdEntidadRelacionada		= 0L;	//en el caso que la relacion no sea con el cliente, sino con otra persona relacionada, aqui se puede indicar
		public String 						IdSituacionLaboral			= "";
		public List<DatosCliente>			DatosCliente;
		public List<TiposDocumento>			TiposDocumento;
        public List<DireccionFisica>		DireccionFisica;
        public List<Telefono>				Telefono;
        public List<DireccionElectronica>	DireccionElectronica;
        public List<IndicadorCliente>		IndicadorCliente;
        public List<IndicadorPersona>		IndicadorPersona;
        public List<IndicadorPortafolio>	IndicadorPortafolio;
        public List<CuentaBancaria>			CuentaBancaria;
        public List<TarjetasBancaria>		TarjetasBancaria;
        public List<DeclaracionOrigenFondo>	DeclaracionOrigenFondo;
        public List<NombreFantasia>			NombreFantasia;
        
		public SocioNegocio()
		{
			DatosCliente = new ArrayList<DatosCliente>();
			TiposDocumento = new ArrayList<TiposDocumento>();
	        DireccionFisica = new ArrayList<DireccionFisica>();
	        Telefono = new ArrayList<Telefono>();
	        DireccionElectronica = new ArrayList<DireccionElectronica>();
	        IndicadorCliente = new ArrayList<IndicadorCliente>();
	        IndicadorPersona = new ArrayList<IndicadorPersona>();
	        IndicadorPortafolio = new ArrayList<IndicadorPortafolio>();
	        CuentaBancaria = new ArrayList<CuentaBancaria>();
	        TarjetasBancaria = new ArrayList<TarjetasBancaria>();
	        DeclaracionOrigenFondo = new ArrayList<DeclaracionOrigenFondo>();
	        NombreFantasia = new ArrayList<NombreFantasia>();
		}
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}

	public static class DatosCliente
	{
        public String	IdCategoriaInversionista	= "";
        public Long		CodigoEjecutivo				= 0L;
        public String	IdTipoCliente				= "";
        public String	NombreSegmentoCliente		= "";
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	public static class TiposDocumento
	{
		public String	Accion					= "";
		public Long		IdDocumentoIdentidad	= 0L;
        public String	TipoDocumento			= "";
        public String	NumeroDocumento			= "";
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	public static class DireccionFisica
	{
		public String	Accion				= "";
		public Long		IdDireccionFisica	= 0L;
        public String	IdProposito			= "";
        public String	Sector				= "";
        public String	CodigoPostal		= "";
        public Long		IdComuna			= 0L;
        public String	Calle				= "";
        public String	Numero				= "";
        public String	Departamento		= "";
        public String	Complemento			= "";
        public Long		IdCiudad			= 0L;
        
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	public static class Telefono
	{
		public String	Accion		= "";
		public Long		IdTelefono	= 0L;
        public String	IdProposito	= "";
        public String	Numero		= "";
        public Long		IdPais		= 0L;
        public String	Tipo		= "";
        public String	Anexo		= "";
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	public static class DireccionElectronica
	{
		public String	Accion					= "";
		public Long		IdDireccionElectronica	= 0L;
        public String	Tipo					= "";
        public String	IdProposito				= "";
        public String	Direccion				= "";
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	public static class IndicadorCliente
	{
        public String	ValorIndicador		= "";
        public String	CodigoTipoIndicador	= "";
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	public static class IndicadorPersona
	{
        public String	ValorIndicador		= "";
        public String	CodigoTipoIndicador	= "";
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	public static class IndicadorPortafolio extends BcsPojo
	{
        public String	ValorIndicador;
        public String	CodigoTipoIndicador;
		public Long 	IdTipoIndicador;
		public Long 	IdIndicador;
		
		public IndicadorPortafolio() {
			this.CodigoTipoIndicador = "";
			this.ValorIndicador = "";
			this.IdTipoIndicador = 0L;
			this.IdIndicador = 0L;
		}
	}
	
	public static class CuentaBancaria
	{
		public String	Accion						= "";
		public Long		IdCuentaBancaria			= 0L;
        public String	NumeroCuentaBancaria		= "";
        public String	IdEstado					= "";
        public String	IdMoneda					= "";
        public String	CodigoTipoCuentaBancaria	= "";
        public Long		IdBanco						= 0L;
        public String	CodigoSucursal				= "";
        public Long		CodigoPlaza					= 0L;
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	public static class TarjetasBancaria
	{
		public String	Accion				= "";
		public Long		IdTarjetaBancaria	= 0L;
        public Long		IdBanco				= 0L;
        public String	IdTipo				= "";
        public String	NumeroTarjeta		= "";
        public String	CodigoVerificacion	= "";
        public String	FechaVencimiento	= "";
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	public static class DeclaracionOrigenFondo
	{
		public String					Accion						= "";
		public Long						IdDeclaracionOrigenFondos	= 0L;
        public String					IdTipoOperacionOrigenFondos	= "";
        public String					FechaDeclaracion			= "";
        public String					Comentario					= "";
        public String					OperacionInscripcion		= "";
        public String					IdEstado					= "";
        public List<DetalleOrigenFondo>	DetalleOrigenFondo;
        
        public DeclaracionOrigenFondo()
		{
			DetalleOrigenFondo = new ArrayList<DetalleOrigenFondo>();
		}
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	public static class DetalleOrigenFondo
	{
        public String	IdTipoOrigenFondo	= "";
        public Long		Valor				= 0L;
        public String	Otros				= "";
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	public static class NombreFantasia
	{
		public String	Accion				= "";
		public Long		IdNombreFantasia	= 0L;
        public String	Nombre				= "";
        public String	IdEstado			= "";
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	
	//-------------------------------------------------
	// CLASES DE SALIDA 
	//-------------------------------------------------
	public static class ModificarSocioNegocio_Salida extends BcsPojo
	{
		public Long						P_OUT_ERRORESCONTADOR;
		public Long						P_OUT_ADVERTENCIASCONTADOR;
		public Long						p_Out_ContadorEntidadModificada;
		public List<EntidadModificada>	p_Out_EntidadModificada;
		
		public ModificarSocioNegocio_Salida () {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_ADVERTENCIASCONTADOR = 0L;
			this.p_Out_ContadorEntidadModificada = 0L;
			this.p_Out_EntidadModificada = new ArrayList<EntidadModificada>();
			
		}
	}
	
	public static class EntidadModificada extends BcsPojo {
		public String					TipoEntidad;
		public Long						IdEntidad;
		public List<MensajesErrores>	Mensajes;
		
		public EntidadModificada() {
			this.IdEntidad = 0L;
			this.TipoEntidad = "";
			this.Mensajes = new ArrayList<MensajesErrores>();
		}
	}
	
	public static class MensajesErrores extends BcsPojo
	{
		public Long		IdInterno;
		public String	TipoMensaje;
		public String	Codigo;
		public String	Mensaje;
		
		public MensajesErrores() {
			this.IdInterno = 0L;
			this.TipoMensaje = "";
			this.Codigo = "";
			this.Mensaje = "";
		}
	}
	
	
	//-------------------------------------------------
	// CLASES UTILITARIAS 
	//-------------------------------------------------
	public static class SelectPersonaCliente
	{
		public String	DocumentoIdentidad	= "";
		public Long		IdPersona			= 0L;
		public Long		IdCliente			= 0L;
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	public static class SelectDocumentoIdentidad
	{
        public Long		IdDocumentoIdentidad		= 0L;
        public String	IdTipoDocumentoIdentidad	= "";
        public String	DocumentoIdentidad			= "";
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	public static class RegistroCambio
	{
        public String	CampoModificado	= "";
        public String	ValorNuevo		= "";
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	public static class IndicadorPersonaCliente
	{
		public Long		IdIndicador			= 0L;
		public Long		IdTipoIndicador		= 0L;
		public String	ValorIndicador		= "";
		public Date		FechaVencimiento	= Fecha.toDate("0001-01-01 00:00:00");
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	public static class SelectConsultarIndicadores
	{
		public Long		IdIndicador		= 0L;
        public Long		IdTipoIndicador	= 0L;
        public String	ValorIndicador	= "";
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
		
	public static class SelectConsultarIndicadoresExiste

	{
		public Long		IdTipoIndicador	= 0L;
        
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	public static class Tipos extends BcsPojo {
		public String TipoEnrolamiento;
		public String Tipo;
		
		public Tipos() {
			this.TipoEnrolamiento="";
			this.Tipo="";
		}
	}
	public static class ContadorSelect extends BcsPojo {
		public Long Contador;
		
		public ContadorSelect() {
			this.Contador = 0L;
		}
	}
	
	//-------------------------------------------------
	// LOGICA DEL SERVICIO 
	//-------------------------------------------------
	public ModificarSocioNegocio_Salida logica(Connection conn, ModificarSocioNegocio_Entrada in) throws Exception
	{
		SQL sql = new SQL();
		
		ModificarSocioNegocio_Salida salida = new ModificarSocioNegocio_Salida();
		
		String FechaSistema = Utils.getFechaSistema(conn, Utils.getSchema(), in.p_IdEmpresa);
		String FechaHoraSistema = Utils.getFechaHoraSistema(conn, Utils.getSchema(), in.p_IdEmpresa);

		final Map<String, SocioNegocioUtils.TipoSegmento> TipoSegmentoPorNombre = SocioNegocioUtils
				.obtenerTiposSegmentos(conn, in.p_IdEmpresa)
				.stream()
				.collect(Collectors.toMap(u -> u.NombreSegmentoCliente, u -> u));
		
		Long ErroresContador = 1L;
		Long ContadorEntidadModificada = 0L;
		
		List<EntidadModificada> listEntidadModificada = new ArrayList<EntidadModificada>();
		List<MensajesErrores> listMensajesErrores = new ArrayList<MensajesErrores>();

		List<Validacion> validaciones = new ArrayList<>();
		
		if(in.p_SocioNegocio.size() > 0)
		{
			for(SocioNegocio socioNegocio : in.p_SocioNegocio)
			{
				if(!socioNegocio.DocumentoIdentidad.isEmpty())
				{
					HashMap<String,Object> parametroEntrada = new HashMap<String,Object>();
					parametroEntrada.put("p_DocumentoIdentidad", socioNegocio.DocumentoIdentidad.trim());
					logger.debug("INICIO::SQL_SELECT_PERSONA_CLIENTE");
					List<SelectPersonaCliente> SelectPersonaCliente = sql.query(conn, SQL_SELECT_PERSONA_CLIENTE, parametroEntrada, SelectPersonaCliente.class);
					logger.debug("FIN::SQL_SELECT_PERSONA_CLIENTE");
					
					if(!SelectPersonaCliente.isEmpty())
					{
						Long IdPersona = SelectPersonaCliente.get(0).IdPersona;
						Long IdCliente = SelectPersonaCliente.get(0).IdCliente;
						
						String TipoEntidad = "";
						Long IdEntidad = 0L;
						
						if(IdCliente > 0)
						{
							TipoEntidad = "CLIENTE";
							IdEntidad = IdCliente;	
						}
						else
						{
							TipoEntidad = "PERSONA";
							IdEntidad = IdPersona;	
						}
						
						Long ContadorErrores = 0L;
						
						EntidadModificada entidadModificada = new EntidadModificada();
						MensajesErrores mensajesErrores = new MensajesErrores();
						
						List<RegistroCambio> listRegistroCambioPersona = new ArrayList<RegistroCambio>();
						List<RegistroCambio> listRegistroCambioCliente = new ArrayList<RegistroCambio>();
						
						// **********************
						// SE MODIFICA LA PERSONA
						// **********************
						if(!socioNegocio.IdTipoPersona.isEmpty())
						{
							RegistroCambio registroCambioPersona = new RegistroCambio();
							registroCambioPersona.CampoModificado	= "Tipo";
							registroCambioPersona.ValorNuevo		= socioNegocio.IdTipoPersona.trim();
							listRegistroCambioPersona.add(registroCambioPersona);	
						}
						
						if(!socioNegocio.Nombres.isEmpty())
						{
							RegistroCambio registroCambioPersona = new RegistroCambio();
							registroCambioPersona.CampoModificado	= "Nombres";
							registroCambioPersona.ValorNuevo		= socioNegocio.Nombres.trim();
							listRegistroCambioPersona.add(registroCambioPersona);
						}
						
						if(!socioNegocio.ApellidoPaterno.isEmpty())
						{
							RegistroCambio registroCambioPersona = new RegistroCambio();
							registroCambioPersona.CampoModificado	= "ApellidoPaterno";
							registroCambioPersona.ValorNuevo		= socioNegocio.ApellidoPaterno.trim();
							listRegistroCambioPersona.add(registroCambioPersona);	
						}
						
						if(!socioNegocio.ApellidoMaterno.isEmpty())
						{
							RegistroCambio registroCambioPersona = new RegistroCambio();
							registroCambioPersona.CampoModificado	= "ApellidoMaterno";
							registroCambioPersona.ValorNuevo		= socioNegocio.ApellidoMaterno.trim();
							listRegistroCambioPersona.add(registroCambioPersona);
						}
						
						if(!socioNegocio.RazonSocial.isEmpty())
						{
							RegistroCambio registroCambioPersona = new RegistroCambio();
							registroCambioPersona.CampoModificado	= "RazonSocial";
							registroCambioPersona.ValorNuevo		= socioNegocio.RazonSocial.trim();
							listRegistroCambioPersona.add(registroCambioPersona);
						}
						
						if(socioNegocio.IdGiro > 0L)
						{
							RegistroCambio registroCambioPersona = new RegistroCambio();
							registroCambioPersona.CampoModificado	= "IdGiroPrincipal";
							registroCambioPersona.ValorNuevo		= String.valueOf(socioNegocio.IdGiro);
							listRegistroCambioPersona.add(registroCambioPersona);
						}
						
						if(socioNegocio.IdPaisNacimiento > 0L)
						{
							RegistroCambio registroCambioPersona = new RegistroCambio();
							registroCambioPersona.CampoModificado	= "IdPaisNacimiento";
							registroCambioPersona.ValorNuevo		= String.valueOf(socioNegocio.IdPaisNacimiento);
							listRegistroCambioPersona.add(registroCambioPersona);
						}
						
						if(socioNegocio.IdPaisResidencia > 0L)
						{
							RegistroCambio registroCambioPersona = new RegistroCambio();
							registroCambioPersona.CampoModificado	= "IdPaisResidencia";
							registroCambioPersona.ValorNuevo		= String.valueOf(socioNegocio.IdPaisResidencia);
							listRegistroCambioPersona.add(registroCambioPersona);
						}
						
						if(!socioNegocio.FechaNacimientoConstitucion.isEmpty())
						{
							RegistroCambio registroCambioPersona = new RegistroCambio();
							registroCambioPersona.CampoModificado	= "FechaNacimiento";
							registroCambioPersona.ValorNuevo		= socioNegocio.FechaNacimientoConstitucion.trim();
							listRegistroCambioPersona.add(registroCambioPersona);
						}
						
						if(!socioNegocio.IdGenero.isEmpty())
						{
							RegistroCambio registroCambioPersona = new RegistroCambio();
							registroCambioPersona.CampoModificado	= "Sexo";
							registroCambioPersona.ValorNuevo		= socioNegocio.IdGenero.trim();
							listRegistroCambioPersona.add(registroCambioPersona);
						}
						
						if(!socioNegocio.IdEstadoCivil.isEmpty())
						{
							RegistroCambio registroCambioPersona = new RegistroCambio();
							registroCambioPersona.CampoModificado	= "EstadoCivil";
							registroCambioPersona.ValorNuevo		= socioNegocio.IdEstadoCivil.trim();
							listRegistroCambioPersona.add(registroCambioPersona);
						}
						
						if(!socioNegocio.IdRegimenConyugal.isEmpty())
						{
							RegistroCambio registroCambioPersona = new RegistroCambio();
							registroCambioPersona.CampoModificado	= "RegimenConyugal";
							registroCambioPersona.ValorNuevo		= socioNegocio.IdRegimenConyugal.trim();
							listRegistroCambioPersona.add(registroCambioPersona);
						}

						if(socioNegocio.IdProfesion > 0L)
						{
							RegistroCambio registroCambioPersona = new RegistroCambio();
							registroCambioPersona.CampoModificado	= "IdProfesion";
							registroCambioPersona.ValorNuevo		= String.valueOf(socioNegocio.IdProfesion);
							listRegistroCambioPersona.add(registroCambioPersona);
						}
						
						if(socioNegocio.IdPaisNacionalidad > 0L)
						{
							RegistroCambio registroCambioPersona = new RegistroCambio();
							registroCambioPersona.CampoModificado	= "IdPaisNacionalidadPrincipal";
							registroCambioPersona.ValorNuevo		= String.valueOf(socioNegocio.IdPaisNacionalidad);
							listRegistroCambioPersona.add(registroCambioPersona);
						}

						if(socioNegocio.IdSituacionLaboral.isEmpty())
						{
							RegistroCambio registroCambioPersona = new RegistroCambio();
							registroCambioPersona.CampoModificado	= "IdSituacionLaboral";
							registroCambioPersona.ValorNuevo		= socioNegocio.IdSituacionLaboral.trim();
							listRegistroCambioPersona.add(registroCambioPersona);
						}
						
						if(!listRegistroCambioPersona.isEmpty())
						{
							cl.bcs.socionegocio.per.ModificarPersonaDatosGenerales modificarPersona = new cl.bcs.socionegocio.per.ModificarPersonaDatosGenerales();
							cl.bcs.socionegocio.per.ModificarPersonaDatosGenerales.EntradaModificarPersonaDatosGenerales inModificarPersona = new cl.bcs.socionegocio.per.ModificarPersonaDatosGenerales.EntradaModificarPersonaDatosGenerales();
							inModificarPersona.p_IdEmpresa = in.p_IdEmpresa;
							inModificarPersona.p_IdUsuario = in.p_IdUsuario;
							inModificarPersona.p_IdModulo = "SOC";
							inModificarPersona.p_UsaReglasNegocio = "N";
							inModificarPersona.p_IdPersona = IdPersona;
							for(RegistroCambio r : listRegistroCambioPersona){
								cl.bcs.socionegocio.per.pojo.RegistroCambio campo = new cl.bcs.socionegocio.per.pojo.RegistroCambio();
								campo.CampoModificado = r.CampoModificado;
								campo.ValorNuevo = r.ValorNuevo;
								inModificarPersona.p_RegistroCambio.add(campo);
							}

							logger.debug("INICIO::ModificarPersonaDatosGenerales");
							cl.bcs.socionegocio.per.ModificarPersonaDatosGenerales.SalidaModificarPersonaDatosGenerales salidaModificarPersona = modificarPersona.logica(conn, inModificarPersona);
							logger.debug("FIN::ModificarPersonaDatosGenerales");
							if(salidaModificarPersona.P_OUT_ERRORESCONTADOR > 0)
							{
								entidadModificada.TipoEntidad = TipoEntidad;
								entidadModificada.IdEntidad = IdEntidad;

								for(MensajeSalida mensaje : salidaModificarPersona.P_OUT_MENSAJES)
								{
									if(!MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)) continue;
									mensajesErrores.IdInterno = 0L;
									mensajesErrores.TipoMensaje = "ERROR";
									mensajesErrores.Codigo = mensaje.codigo;
									mensajesErrores.Mensaje = mensaje.descripcion;

									listMensajesErrores.add(mensajesErrores);
									entidadModificada.Mensajes = listMensajesErrores;
									listEntidadModificada.add(entidadModificada);
									break;
								}

								for(MensajeSalida mensaje : salidaModificarPersona.P_OUT_MENSAJES)
								{
									if(MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)) continue;
									mensajesErrores.IdInterno = 0L;
									mensajesErrores.TipoMensaje = mensaje.tipo;
									mensajesErrores.Codigo = mensaje.codigo;
									mensajesErrores.Mensaje = mensaje.descripcion;

									listMensajesErrores.add(mensajesErrores);
									entidadModificada.Mensajes = listMensajesErrores;
									listEntidadModificada.add(entidadModificada);
									break;
								}

								break;
							}
						}
						
						// **********************
						// SE MODIFICA EL CLIENTE
						// **********************
						if(IdCliente > 0)
						{
							for(DatosCliente datosCliente : socioNegocio.DatosCliente)
							{
								if(!datosCliente.IdCategoriaInversionista.isEmpty())
								{
									RegistroCambio registroCambioCliente = new RegistroCambio();
									registroCambioCliente.CampoModificado	= "IdCategoriaInversionista";
									registroCambioCliente.ValorNuevo		= datosCliente.IdCategoriaInversionista.trim();
									listRegistroCambioCliente.add(registroCambioCliente);	
								}
								
								if(!datosCliente.IdTipoCliente.isEmpty())
								{
									RegistroCambio registroCambioCliente = new RegistroCambio();
									registroCambioCliente.CampoModificado	= "IdTipoCliente";
									registroCambioCliente.ValorNuevo		= datosCliente.IdTipoCliente.trim();
									listRegistroCambioCliente.add(registroCambioCliente);	
								}
								
								if(datosCliente.CodigoEjecutivo > 0L)
								{
									RegistroCambio registroCambioCliente = new RegistroCambio();
									registroCambioCliente.CampoModificado	= "IdPersonaEjecutivo";
									registroCambioCliente.ValorNuevo		= String.valueOf(datosCliente.CodigoEjecutivo);
									listRegistroCambioCliente.add(registroCambioCliente);	
								}

								if(StringUtils.isNullOrBlank(datosCliente.NombreSegmentoCliente) == false)
								{
									SocioNegocioUtils.TipoSegmento tipoSegmento = TipoSegmentoPorNombre.get(datosCliente.NombreSegmentoCliente);
									if(Objects.nonNull(tipoSegmento))
									{
										RegistroCambio registroCambioCliente = new RegistroCambio();
										registroCambioCliente.CampoModificado = "IdSegmentoCliente";
										registroCambioCliente.ValorNuevo = String.valueOf(tipoSegmento.IdSegmentoCliente);
										listRegistroCambioCliente.add(registroCambioCliente);
									}
									else
									{
										HashMap<String, String> parametrosMensaje = new HashMap<>(8);
										parametrosMensaje.put("NombreSegmentoCliente", datosCliente.NombreSegmentoCliente);
										PlataformaUtils.MensajeSalida ms = new PlataformaUtils.MensajeSalida(IdCliente, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, "UI_SEGMENTO_CLIENTE_INVALIDO", parametrosMensaje, conn, in.p_IdEmpresa);
										MensajesErrores me = new MensajesErrores();
										me.IdInterno = 0L;
										me.Codigo = ms.codigo;
										me.Mensaje = ms.descripcion;
										me.TipoMensaje = ms.tipo;
										listMensajesErrores.add(me);
										entidadModificada.Mensajes = listMensajesErrores;
										entidadModificada.IdEntidad = IdEntidad;
										entidadModificada.TipoEntidad = TipoEntidad;
										listEntidadModificada.add(entidadModificada);
										ContadorErrores++;
									}
								}
								
								if(!listRegistroCambioCliente.isEmpty())
								{
									cl.bcs.socionegocio.cli.ModificarClienteDatosGenerales modificarCliente = new cl.bcs.socionegocio.cli.ModificarClienteDatosGenerales();
									cl.bcs.socionegocio.cli.ModificarClienteDatosGenerales.EntradaModificarClienteDatosGenerales inModificarCliente = new cl.bcs.socionegocio.cli.ModificarClienteDatosGenerales.EntradaModificarClienteDatosGenerales();
									inModificarCliente.p_IdEmpresa = in.p_IdEmpresa;
									inModificarCliente.p_IdUsuario = in.p_IdUsuario;
									inModificarCliente.p_IdModulo = "SOC";
									inModificarCliente.p_UsaReglasNegocio = "N";
									inModificarCliente.p_IdCliente = IdCliente;
									for(RegistroCambio r : listRegistroCambioCliente){
										cl.bcs.socionegocio.cli.ModificarClienteDatosGenerales.RegistroCambio campo = new cl.bcs.socionegocio.cli.ModificarClienteDatosGenerales.RegistroCambio();
										campo.CampoModificado = r.CampoModificado;
										campo.ValorNuevo = r.ValorNuevo;
										inModificarCliente.p_RegistroCambio.add(campo);
									}

									logger.debug("INICIO::ModificarClienteDatosGenerales");
									cl.bcs.socionegocio.cli.ModificarClienteDatosGenerales.SalidaModificarClienteDatosGenerales salidaModificarCliente = modificarCliente.logica(conn, inModificarCliente);
									logger.debug("FIN::ModificarClienteDatosGenerales");
									if(salidaModificarCliente.P_OUT_ERRORESCONTADOR > 0)
									{
										entidadModificada.TipoEntidad = TipoEntidad;
										entidadModificada.IdEntidad = IdEntidad;

										for(MensajeSalida mensaje : salidaModificarCliente.P_OUT_MENSAJES)
										{
											if(!MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)) continue;
											mensajesErrores.IdInterno = 0L;
											mensajesErrores.TipoMensaje = "ERROR";
											mensajesErrores.Codigo = mensaje.codigo;
											mensajesErrores.Mensaje = mensaje.descripcion;

											listMensajesErrores.add(mensajesErrores);
											entidadModificada.Mensajes = listMensajesErrores;
											listEntidadModificada.add(entidadModificada);
											ContadorErrores++;
											break;
										}

										for(MensajeSalida mensaje : salidaModificarCliente.P_OUT_MENSAJES)
										{
											if(MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)) continue;
											mensajesErrores.IdInterno = 0L;
											mensajesErrores.TipoMensaje = mensaje.tipo;
											mensajesErrores.Codigo = mensaje.codigo;
											mensajesErrores.Mensaje = mensaje.descripcion;

											listMensajesErrores.add(mensajesErrores);
											entidadModificada.Mensajes = listMensajesErrores;
											listEntidadModificada.add(entidadModificada);
											ContadorErrores++;
											break;
										}

										break;
									}

									break;
								}
							}
							
							if(ContadorErrores > 0) break;
						}
						
						
						// *************************************************
						// AGREGAR, MODIFICAR Y ELIMINAR TIPOS DE DOCUMENTOS
						// *************************************************
						if(!socioNegocio.TiposDocumento.isEmpty())
						{
							for(TiposDocumento tiposDocumento : socioNegocio.TiposDocumento)
							{
								if(tiposDocumento.Accion.equals("AGREGAR"))
								{
									CrearDocumentoIdentidad crearDocumentoIdentidad = new CrearDocumentoIdentidad();
									CrearDocumentoIdentidad.EntradaCrearDocumentoIdentidad inDocumentoIdentidad = new CrearDocumentoIdentidad.EntradaCrearDocumentoIdentidad();
									inDocumentoIdentidad.p_IdEmpresa = in.p_IdEmpresa;
									inDocumentoIdentidad.p_IdUsuario = in.p_IdUsuario;
									inDocumentoIdentidad.p_IdModulo = "SOC";
									inDocumentoIdentidad.p_UsaReglasNegocio = "N";
									inDocumentoIdentidad.p_IdPersona = IdPersona;
									inDocumentoIdentidad.p_IdTipoDocumentoIdentidad = tiposDocumento.TipoDocumento.trim();
									inDocumentoIdentidad.p_IdTipoDocumentoIdentidadAsociado = "";
									inDocumentoIdentidad.p_DocumentoIdentidad = tiposDocumento.NumeroDocumento.trim();
									inDocumentoIdentidad.p_TipoPersona = socioNegocio.IdTipoPersona.trim();
									CrearDocumentoIdentidad.SalidaCrearDocumentoIdentidad salidaCrearDocumentoIdentidad = crearDocumentoIdentidad.logica(conn, inDocumentoIdentidad);
									
									if (salidaCrearDocumentoIdentidad.P_OUT_ERRORESCONTADOR > 0)
									{
										entidadModificada.TipoEntidad = TipoEntidad;
										entidadModificada.IdEntidad = IdEntidad;
										
										for (ErrorDetalle errorDetalle : salidaCrearDocumentoIdentidad.P_OUT_ERRORESDETALLE)
										{
											mensajesErrores.IdInterno = 0L;
											mensajesErrores.TipoMensaje = "ERROR";
											mensajesErrores.Codigo = errorDetalle.Codigo;
											mensajesErrores.Mensaje = errorDetalle.Descripcion;
											
											listMensajesErrores.add(mensajesErrores);
											entidadModificada.Mensajes = listMensajesErrores;
											listEntidadModificada.add(entidadModificada);
											ContadorErrores++;
											break;
										}
										
										break;
									}
								}
								else if(tiposDocumento.Accion.equals("MODIFICAR"))
								{
									List<RegistroCambio> listRegistroCambio = new ArrayList<RegistroCambio>();
									
									if(!tiposDocumento.TipoDocumento.isEmpty())
									{
										RegistroCambio registroCambio = new RegistroCambio();
										registroCambio.CampoModificado	= "IdTipoDocumentoIdentidad";
										registroCambio.ValorNuevo		= tiposDocumento.TipoDocumento.trim();
										listRegistroCambio.add(registroCambio);
									}
									
									if(!tiposDocumento.NumeroDocumento.isEmpty())
									{
										RegistroCambio registroCambio = new RegistroCambio();
										registroCambio.CampoModificado	= "DocumentoIdentidad";
										registroCambio.ValorNuevo		= tiposDocumento.NumeroDocumento.trim();
										listRegistroCambio.add(registroCambio);
									}
									
									if(!listRegistroCambio.isEmpty())
									{
										cl.bcs.socionegocio.per.ModificarPersonaDocumentosIdentidad modificarDocumentoIdentidad = new cl.bcs.socionegocio.per.ModificarPersonaDocumentosIdentidad();
										cl.bcs.socionegocio.per.ModificarPersonaDocumentosIdentidad.EntradaServicioModificarPersonaDocumentosIdentidad inModificarDocIdentidad = new cl.bcs.socionegocio.per.ModificarPersonaDocumentosIdentidad.EntradaServicioModificarPersonaDocumentosIdentidad();
										inModificarDocIdentidad.p_IdEmpresa = in.p_IdEmpresa;
										inModificarDocIdentidad.p_IdUsuario = in.p_IdUsuario;
										inModificarDocIdentidad.p_IdModulo = "SOC";
										inModificarDocIdentidad.p_UsaReglasNegocio = "N";
										inModificarDocIdentidad.p_IdPersona = IdPersona;
										inModificarDocIdentidad.p_IdDocumentoIdentidad = tiposDocumento.IdDocumentoIdentidad;
										for(RegistroCambio r : listRegistroCambio){
											cl.bcs.socionegocio.per.ModificarPersonaDocumentosIdentidad.RegistroCambioCampo campo = new cl.bcs.socionegocio.per.ModificarPersonaDocumentosIdentidad.RegistroCambioCampo();
											campo.CampoModificado = r.CampoModificado;
											campo.ValorNuevo = r.ValorNuevo;
											inModificarDocIdentidad.p_RegistroCambio.add(campo);
										}

										logger.debug("INICIO::ModificarPersonaDocumentosIdentidad");
										cl.bcs.socionegocio.per.ModificarPersonaDocumentosIdentidad.SalidaModificarPersonaDocumentosIdentidad salidaModificarDocumentoIdentidad = modificarDocumentoIdentidad.logica(conn, inModificarDocIdentidad);
										logger.debug("FIN::ModificarPersonaDocumentosIdentidad");
										if(salidaModificarDocumentoIdentidad.P_OUT_ERRORESCONTADOR > 0)
										{
											entidadModificada.TipoEntidad = TipoEntidad;
											entidadModificada.IdEntidad = IdEntidad;

											for(ErrorDetalle errorDetalle : salidaModificarDocumentoIdentidad.P_OUT_ERRORESDETALLE)
											{
												mensajesErrores.IdInterno = 0L;
												mensajesErrores.TipoMensaje = "ERROR";
												mensajesErrores.Codigo = errorDetalle.Codigo;
												mensajesErrores.Mensaje = errorDetalle.Descripcion;

												listMensajesErrores.add(mensajesErrores);
												entidadModificada.Mensajes = listMensajesErrores;
												listEntidadModificada.add(entidadModificada);
												ContadorErrores++;
												break;
											}

											for(AdvertenciaDetalle advertenciaDetalle : salidaModificarDocumentoIdentidad.P_OUT_ADVERTENCIASDETALLE)
											{
												mensajesErrores.IdInterno = 0L;
												mensajesErrores.TipoMensaje = "ADVERTENCIA";
												mensajesErrores.Codigo = advertenciaDetalle.Codigo;
												mensajesErrores.Mensaje = advertenciaDetalle.Descripcion;

												listMensajesErrores.add(mensajesErrores);
												entidadModificada.Mensajes = listMensajesErrores;
												listEntidadModificada.add(entidadModificada);
												ContadorErrores++;
												break;
											}

											break;
										}
									}
								}
								else if(tiposDocumento.Accion.equals("ELIMINAR"))
								{
									HashMap<String,Object> parametroSelectDocumentoIdentidad = new HashMap<String,Object>();
									parametroSelectDocumentoIdentidad.put("p_IdPersona", IdPersona);
									logger.debug("INICIO::SQL_SELECT_DOCUMENTO_IDENTIDAD");
									List<SelectDocumentoIdentidad> selectDocumentoIdentidad = sql.query(conn, SQL_SELECT_DOCUMENTO_IDENTIDAD, parametroSelectDocumentoIdentidad, SelectDocumentoIdentidad.class);
									logger.debug("FIN::SQL_SELECT_DOCUMENTO_IDENTIDAD");
									if(selectDocumentoIdentidad.size() > 1)
									{
										cl.bcs.socionegocio.per.EliminarPersonaDocumentoIdentidad eliminarDocumentoIdentidad = new cl.bcs.socionegocio.per.EliminarPersonaDocumentoIdentidad();
										cl.bcs.socionegocio.per.EliminarPersonaDocumentoIdentidad.EntradaServicioEliminarPersonaDocumentoIdentidad inEliminarDocIdentidad = new cl.bcs.socionegocio.per.EliminarPersonaDocumentoIdentidad.EntradaServicioEliminarPersonaDocumentoIdentidad();
										inEliminarDocIdentidad.p_IdEmpresa = in.p_IdEmpresa;
										inEliminarDocIdentidad.p_IdUsuario = in.p_IdUsuario;
										inEliminarDocIdentidad.p_IdModulo = "SOC";
										inEliminarDocIdentidad.p_UsaReglasNegocio = "N";
										inEliminarDocIdentidad.p_IdDocumentoIdentidad = tiposDocumento.IdDocumentoIdentidad;

										logger.debug("INICIO::EliminarPersonaDocumentoIdentidad");
										cl.bcs.socionegocio.per.EliminarPersonaDocumentoIdentidad.SalidaEliminarPersonaDocumentoIdentidad salidaEliminarDocumentoIdentidad = eliminarDocumentoIdentidad.logica(conn, inEliminarDocIdentidad);
										logger.debug("FIN::EliminarPersonaDocumentoIdentidad");
										if(salidaEliminarDocumentoIdentidad.P_OUT_ERRORESCONTADOR > 0)
										{
											entidadModificada.TipoEntidad = TipoEntidad;
											entidadModificada.IdEntidad = IdEntidad;

											for(ErrorDetalle errorDetalle : salidaEliminarDocumentoIdentidad.P_OUT_ERRORESDETALLE)
											{
												mensajesErrores.IdInterno = 0L;
												mensajesErrores.TipoMensaje = "ERROR";
												mensajesErrores.Codigo = errorDetalle.Codigo;
												mensajesErrores.Mensaje = errorDetalle.Descripcion;

												listMensajesErrores.add(mensajesErrores);
												entidadModificada.Mensajes = listMensajesErrores;
												listEntidadModificada.add(entidadModificada);
												ContadorErrores++;
												break;
											}

											for(AdvertenciaDetalle advertenciaDetalle : salidaEliminarDocumentoIdentidad.P_OUT_ADVERTENCIASDETALLE)
											{
												mensajesErrores.IdInterno = 0L;
												mensajesErrores.TipoMensaje = "ADVERTENCIA";
												mensajesErrores.Codigo = advertenciaDetalle.Codigo;
												mensajesErrores.Mensaje = advertenciaDetalle.Descripcion;

												listMensajesErrores.add(mensajesErrores);
												entidadModificada.Mensajes = listMensajesErrores;
												listEntidadModificada.add(entidadModificada);
												ContadorErrores++;
												break;
											}

											break;
										}
									}
									else if(selectDocumentoIdentidad.size() == 1) {
										
										String codigoCampo = conceptosCodigoCampo.get("DOCUMENTOS_IDENTIDAD");
										if( alMenosUnRegistro(conn,IdPersona, codigoCampo, in.p_IdTipoCanal)) {
											String descripcion = Diccionario.getMensaje(conn, in.p_IdEmpresa,"ERROR_DOCUMENTO_IDENTIDAD_REQUERIDO", new HashMap<>());  
											mensajesErrores.IdInterno = 0L;
											mensajesErrores.TipoMensaje = "ERROR";
											mensajesErrores.Codigo = "ERROR_DOCUMENTO_IDENTIDAD_REQUERIDO" ;
											mensajesErrores.Mensaje = descripcion;
											
											listMensajesErrores.add(mensajesErrores);
											entidadModificada.Mensajes = listMensajesErrores;
											listEntidadModificada.add(entidadModificada);
											ContadorErrores++;
											break;
										}
									}
								}
							}
							
							if(ContadorErrores > 0) break;
						}
						
						
						// **********************************************
						// AGREGAR, MODIFICAR Y ELIMINAR DIRECCION FISICA
						// **********************************************
						if(!socioNegocio.DireccionFisica.isEmpty())
						{
							for(DireccionFisica direccionFisica : socioNegocio.DireccionFisica)
							{
								if(direccionFisica.Accion.equals("AGREGAR"))
								{
									CrearDireccionFisica crearDireccionFisica = new CrearDireccionFisica();
									CrearDireccionFisica.EntradaCrearDireccionFisica inDireccionFisica = new CrearDireccionFisica.EntradaCrearDireccionFisica();
									inDireccionFisica.p_IdUsuario = in.p_IdUsuario;
									inDireccionFisica.p_IdEmpresa = in.p_IdEmpresa;
									inDireccionFisica.p_IdModulo = "SOC";
									inDireccionFisica.p_UsaReglasNegocio = "N";
									inDireccionFisica.p_Calle = direccionFisica.Calle;
									inDireccionFisica.p_Numero = direccionFisica.Numero;
									inDireccionFisica.p_Departamento = direccionFisica.Departamento;
									inDireccionFisica.p_Sector = direccionFisica.Sector;
									inDireccionFisica.p_CodigoPostal = direccionFisica.CodigoPostal;
									inDireccionFisica.p_IdCiudad = direccionFisica.IdCiudad;
									inDireccionFisica.p_IdDivisionPais = direccionFisica.IdComuna;
									inDireccionFisica.p_CodigoProposito = direccionFisica.IdProposito;
									inDireccionFisica.p_Comentario = direccionFisica.Complemento;
									inDireccionFisica.p_IdEstado = "ACT";
									inDireccionFisica.p_IdPersona = IdPersona;
									inDireccionFisica.p_Tipo = socioNegocio.IdTipoPersona.trim();
									CrearDireccionFisica.SalidaCrearDireccionFisica salidaCrearDireccionFisica = crearDireccionFisica.logica(conn, inDireccionFisica);
									
									if(salidaCrearDireccionFisica.P_OUT_ERRORESCONTADOR > 0)
									{
										entidadModificada.TipoEntidad = TipoEntidad;
										entidadModificada.IdEntidad = IdEntidad;
										
										for (ErrorDetalle errorDetalle : salidaCrearDireccionFisica.P_OUT_ERRORESDETALLE)
										{
											mensajesErrores.IdInterno = 0L;
											mensajesErrores.TipoMensaje = "ERROR";
											mensajesErrores.Codigo = errorDetalle.Codigo;
											mensajesErrores.Mensaje = errorDetalle.Descripcion;
											
											listMensajesErrores.add(mensajesErrores);
											entidadModificada.Mensajes = listMensajesErrores;
											listEntidadModificada.add(entidadModificada);
											ContadorErrores++;
											break;
										}
										
										break;
									}
								}
								else if(direccionFisica.Accion.equals("MODIFICAR"))
								{
									List<RegistroCambio> listRegistroCambio = new ArrayList<RegistroCambio>();
									
									if(!direccionFisica.IdProposito.isEmpty())
									{
										RegistroCambio registroCambio = new RegistroCambio();
										registroCambio.CampoModificado	= "CodigoProposito";
										registroCambio.ValorNuevo		= direccionFisica.IdProposito.trim();
										listRegistroCambio.add(registroCambio);
									}
									
									if(!direccionFisica.Sector.isEmpty())
									{
										RegistroCambio registroCambio = new RegistroCambio();
										registroCambio.CampoModificado	= "Sector";
										registroCambio.ValorNuevo		= direccionFisica.Sector.trim();
										listRegistroCambio.add(registroCambio);
									}
									
									if(!direccionFisica.CodigoPostal.isEmpty())
									{
										RegistroCambio registroCambio = new RegistroCambio();
										registroCambio.CampoModificado	= "CodigoPostal";
										registroCambio.ValorNuevo		= direccionFisica.CodigoPostal.trim();
										listRegistroCambio.add(registroCambio);
									}
									
									if(direccionFisica.IdComuna > 0L)
									{
										RegistroCambio registroCambio = new RegistroCambio();
										registroCambio.CampoModificado	= "IdDivisionPais";
										registroCambio.ValorNuevo		= String.valueOf(direccionFisica.IdComuna);
										listRegistroCambio.add(registroCambio);
									}
									
									if(!direccionFisica.Calle.isEmpty())
									{
										RegistroCambio registroCambio = new RegistroCambio();
										registroCambio.CampoModificado	= "Calle";
										registroCambio.ValorNuevo		= direccionFisica.Calle.trim();
										listRegistroCambio.add(registroCambio);
									}
									
									if(!direccionFisica.Numero.isEmpty())
									{
										RegistroCambio registroCambio = new RegistroCambio();
										registroCambio.CampoModificado	= "Numero";
										registroCambio.ValorNuevo		= direccionFisica.Numero.trim();
										listRegistroCambio.add(registroCambio);
									}
									
									if(!direccionFisica.Departamento.isEmpty())
									{
										RegistroCambio registroCambio = new RegistroCambio();
										registroCambio.CampoModificado	= "Departamento";
										registroCambio.ValorNuevo		= direccionFisica.Departamento.trim();
										listRegistroCambio.add(registroCambio);
									}
									
									if(!direccionFisica.Complemento.isEmpty())
									{
										RegistroCambio registroCambio = new RegistroCambio();
										registroCambio.CampoModificado	= "Comentario";
										registroCambio.ValorNuevo		= direccionFisica.Complemento.trim();
										listRegistroCambio.add(registroCambio);
									}
									
									if(direccionFisica.IdCiudad > 0L)
									{
										RegistroCambio registroCambio = new RegistroCambio();
										registroCambio.CampoModificado	= "IdCiudad";
										registroCambio.ValorNuevo		= String.valueOf(direccionFisica.IdCiudad);
										listRegistroCambio.add(registroCambio);
									}
									
									if(!listRegistroCambio.isEmpty())
									{
										cl.bcs.socionegocio.per.ModificarPersonaDireccionFisica modificarDireccionFisica = new cl.bcs.socionegocio.per.ModificarPersonaDireccionFisica();
										cl.bcs.socionegocio.per.ModificarPersonaDireccionFisica.EntradaServicioModificarPersonaDireccionFisica inModificarDireccionFisica = new cl.bcs.socionegocio.per.ModificarPersonaDireccionFisica.EntradaServicioModificarPersonaDireccionFisica();
										inModificarDireccionFisica.p_IdEmpresa = in.p_IdEmpresa;
										inModificarDireccionFisica.p_IdUsuario = in.p_IdUsuario;
										inModificarDireccionFisica.p_IdModulo = "SOC";
										inModificarDireccionFisica.p_UsaReglasNegocio = "N";
										inModificarDireccionFisica.p_IdDireccionFisica = direccionFisica.IdDireccionFisica;
										for(RegistroCambio r : listRegistroCambio){
											cl.bcs.socionegocio.per.ModificarPersonaDireccionFisica.RegistroCambioCampo campo = new cl.bcs.socionegocio.per.ModificarPersonaDireccionFisica.RegistroCambioCampo();
											campo.CampoModificado = r.CampoModificado;
											campo.ValorNuevo = r.ValorNuevo;
											inModificarDireccionFisica.p_RegistroCambio.add(campo);
										}

										logger.debug("INICIO::ModificarPersonaDireccionFisica");
										cl.bcs.socionegocio.per.ModificarPersonaDireccionFisica.SalidaModificarPersonaDireccionFisica salidaModificarDireccionFisica = modificarDireccionFisica.logica(conn, inModificarDireccionFisica);
										logger.debug("FIN::ModificarPersonaDireccionFisica");
										if(salidaModificarDireccionFisica.P_OUT_ERRORESCONTADOR > 0)
										{
											entidadModificada.TipoEntidad = TipoEntidad;
											entidadModificada.IdEntidad = IdEntidad;

											for(ErrorDetalle errorDetalle : salidaModificarDireccionFisica.P_OUT_ERRORESDETALLE)
											{
												mensajesErrores.IdInterno = 0L;
												mensajesErrores.TipoMensaje = "ERROR";
												mensajesErrores.Codigo = errorDetalle.Codigo;
												mensajesErrores.Mensaje = errorDetalle.Descripcion;

												listMensajesErrores.add(mensajesErrores);
												entidadModificada.Mensajes = listMensajesErrores;
												listEntidadModificada.add(entidadModificada);
												ContadorErrores++;
												break;
											}

											for(AdvertenciaDetalle advertenciaDetalle : salidaModificarDireccionFisica.P_OUT_ADVERTENCIASDETALLE)
											{
												mensajesErrores.IdInterno = 0L;
												mensajesErrores.TipoMensaje = "ADVERTENCIA";
												mensajesErrores.Codigo = advertenciaDetalle.Codigo;
												mensajesErrores.Mensaje = advertenciaDetalle.Descripcion;

												listMensajesErrores.add(mensajesErrores);
												entidadModificada.Mensajes = listMensajesErrores;
												listEntidadModificada.add(entidadModificada);
												ContadorErrores++;
												break;
											}

											break;
										}
									}
								}
								else if(direccionFisica.Accion.equals("ELIMINAR")) {

									HashMap<String,Object> parametroSelect = new HashMap<String,Object>();
									parametroSelect.put("IdPersona", IdPersona);
									Long contadorDireccionesFisicas = sql.query(conn, SQL_CONTADOR_DIRECCIONES_FISICAS, parametroSelect, ContadorSelect.class).stream().findFirst().orElse(new ContadorSelect()).Contador;
									if(contadorDireccionesFisicas == 1)
									{
										String codigoCampo = conceptosCodigoCampo.get("DIRECCIONES_FISICAS");
										if( alMenosUnRegistro(conn,IdPersona, codigoCampo, in.p_IdTipoCanal)) {
											String descripcion = Diccionario.getMensaje(conn, in.p_IdEmpresa,"ERROR_DIRECCION_FISICA_REQUERIDA", new HashMap<>());
											mensajesErrores.IdInterno = 0L;
											mensajesErrores.TipoMensaje = "ERROR";
											mensajesErrores.Codigo = "ERROR_DIRECCION_FISICA_REQUERIDA" ;
											mensajesErrores.Mensaje = descripcion;

											listMensajesErrores.add(mensajesErrores);
											entidadModificada.Mensajes = listMensajesErrores;
											listEntidadModificada.add(entidadModificada);
											ContadorErrores++;
											break;
										}

									}

									cl.bcs.socionegocio.per.EliminarPersonaDireccionFisica eliminarDireccionFisica = new cl.bcs.socionegocio.per.EliminarPersonaDireccionFisica();
									cl.bcs.socionegocio.per.EliminarPersonaDireccionFisica.EntradaServicioEliminarPersonaDireccionFisica inEliminarDireccionFisica = new cl.bcs.socionegocio.per.EliminarPersonaDireccionFisica.EntradaServicioEliminarPersonaDireccionFisica();
									inEliminarDireccionFisica.p_IdEmpresa = in.p_IdEmpresa;
									inEliminarDireccionFisica.p_IdUsuario = in.p_IdUsuario;
									inEliminarDireccionFisica.p_IdModulo = "SOC";
									inEliminarDireccionFisica.p_UsaReglasNegocio = "N";
									inEliminarDireccionFisica.p_IdDireccionFisica = direccionFisica.IdDireccionFisica;

									logger.debug("INICIO::EliminarPersonaDireccionFisica");
									cl.bcs.socionegocio.per.EliminarPersonaDireccionFisica.SalidaEliminarPersonaDireccionFisica salidaEliminarDireccionFisica = eliminarDireccionFisica.logica(conn, inEliminarDireccionFisica);
									logger.debug("FIN::EliminarPersonaDireccionFisica");
									if(salidaEliminarDireccionFisica.P_OUT_ERRORESCONTADOR > 0)
									{
										entidadModificada.TipoEntidad = TipoEntidad;
										entidadModificada.IdEntidad = IdEntidad;

										for(ErrorDetalle errorDetalle : salidaEliminarDireccionFisica.P_OUT_ERRORESDETALLE)
										{
											mensajesErrores.IdInterno = 0L;
											mensajesErrores.TipoMensaje = "ERROR";
											mensajesErrores.Codigo = errorDetalle.Codigo;
											mensajesErrores.Mensaje = errorDetalle.Descripcion;

											listMensajesErrores.add(mensajesErrores);
											entidadModificada.Mensajes = listMensajesErrores;
											listEntidadModificada.add(entidadModificada);
											ContadorErrores++;
											break;
										}

										for(AdvertenciaDetalle advertenciaDetalle : salidaEliminarDireccionFisica.P_OUT_ADVERTENCIASDETALLE)
										{
											mensajesErrores.IdInterno = 0L;
											mensajesErrores.TipoMensaje = "ADVERTENCIA";
											mensajesErrores.Codigo = advertenciaDetalle.Codigo;
											mensajesErrores.Mensaje = advertenciaDetalle.Descripcion;

											listMensajesErrores.add(mensajesErrores);
											entidadModificada.Mensajes = listMensajesErrores;
											listEntidadModificada.add(entidadModificada);
											ContadorErrores++;
											break;
										}

										break;
									}
								}
							}
							
							if(ContadorErrores > 0) break;
						}
						
						
						// **************************************
						// AGREGAR, MODIFICAR Y ELIMINAR TELEFONO
						// **************************************
						if(!socioNegocio.Telefono.isEmpty())
						{
							for(Telefono telefono : socioNegocio.Telefono)
							{
								if(telefono.Accion.equals("AGREGAR"))
								{
									CrearTelefono crearTelefono = new CrearTelefono();
									CrearTelefono.EntradaCrearTelefono inCrearTelefono = new CrearTelefono.EntradaCrearTelefono();
									inCrearTelefono.p_IdUsuario = in.p_IdUsuario;
									inCrearTelefono.p_IdEmpresa = in.p_IdEmpresa;
									inCrearTelefono.p_IdModulo = "SOC";
									inCrearTelefono.p_UsaReglasNegocio = "N";
									inCrearTelefono.p_IdPais = telefono.IdPais;
									inCrearTelefono.p_IdTipoTelefono = telefono.Tipo;
									inCrearTelefono.p_Numero = telefono.Numero;
									inCrearTelefono.p_Anexo = "";
									inCrearTelefono.p_CodigoProposito = telefono.IdProposito;
									inCrearTelefono.p_Comentario = "";
									inCrearTelefono.p_IdEstado = "ACT";
									inCrearTelefono.p_Tipo = socioNegocio.IdTipoPersona.trim();
									inCrearTelefono.p_IdPersona = IdPersona;
									CrearTelefono.SalidaCrearTelefono salidaCrearTelefono = crearTelefono.logica(conn, inCrearTelefono);
									
									if(salidaCrearTelefono.P_OUT_ERRORESCONTADOR > 0)
									{
										entidadModificada.TipoEntidad = TipoEntidad;
										entidadModificada.IdEntidad = IdEntidad;
										
										for (ErrorDetalle errorDetalle : salidaCrearTelefono.P_OUT_ERRORESDETALLE)
										{
											mensajesErrores.IdInterno = 0L;
											mensajesErrores.TipoMensaje = "ERROR";
											mensajesErrores.Codigo = errorDetalle.Codigo;
											mensajesErrores.Mensaje = errorDetalle.Descripcion;
											
											listMensajesErrores.add(mensajesErrores);
											entidadModificada.Mensajes = listMensajesErrores;
											listEntidadModificada.add(entidadModificada);
											ContadorErrores++;
											break;
										}
										
										break;
									}
								}
								else if(telefono.Accion.equals("MODIFICAR"))
								{
									List<RegistroCambio> listRegistroCambio = new ArrayList<RegistroCambio>();
									
									if(!telefono.IdProposito.isEmpty())
									{
										RegistroCambio registroCambio = new RegistroCambio();
										registroCambio.CampoModificado	= "CodigoProposito";
										registroCambio.ValorNuevo		= telefono.IdProposito.trim();
										listRegistroCambio.add(registroCambio);
									}
									
									if(!telefono.Numero.isEmpty())
									{
										RegistroCambio registroCambio = new RegistroCambio();
										registroCambio.CampoModificado	= "Numero";
										registroCambio.ValorNuevo		= telefono.Numero.trim();
										listRegistroCambio.add(registroCambio);
									}
									
									if(telefono.IdPais > 0L)
									{
										RegistroCambio registroCambio = new RegistroCambio();
										registroCambio.CampoModificado	= "IdPais";
										registroCambio.ValorNuevo		= String.valueOf(telefono.IdPais);
										listRegistroCambio.add(registroCambio);
									}
									
									if(!telefono.Tipo.isEmpty())
									{
										RegistroCambio registroCambio = new RegistroCambio();
										registroCambio.CampoModificado	= "IdTipoTelefono";
										registroCambio.ValorNuevo		= telefono.Tipo.trim();
										listRegistroCambio.add(registroCambio);
									}
									
									if(!telefono.Anexo.isEmpty())
									{
										RegistroCambio registroCambio = new RegistroCambio();
										registroCambio.CampoModificado	= "Anexo";
										registroCambio.ValorNuevo		= telefono.Anexo.trim();
										listRegistroCambio.add(registroCambio);
									}
									
									if(!listRegistroCambio.isEmpty())
									{
										cl.bcs.socionegocio.per.ModificarPersonaTelefono modificarTelefono = new cl.bcs.socionegocio.per.ModificarPersonaTelefono();
										cl.bcs.socionegocio.per.ModificarPersonaTelefono.EntradaModificarPersonaTelefono inModificarTelefono = new cl.bcs.socionegocio.per.ModificarPersonaTelefono.EntradaModificarPersonaTelefono();
										inModificarTelefono.p_IdEmpresa = in.p_IdEmpresa;
										inModificarTelefono.p_IdUsuario = in.p_IdUsuario;
										inModificarTelefono.p_IdModulo = "SOC";
										inModificarTelefono.p_UsaReglasNegocio = "N";
										inModificarTelefono.p_IdTelefono = telefono.IdTelefono;
										for(RegistroCambio r : listRegistroCambio){
											cl.bcs.socionegocio.per.ModificarPersonaTelefono.RegistroCambio campo = new cl.bcs.socionegocio.per.ModificarPersonaTelefono.RegistroCambio();
											campo.CampoModificado = r.CampoModificado;
											campo.ValorNuevo = r.ValorNuevo;
											inModificarTelefono.p_RegistroCambio.add(campo);
										}

										logger.debug("INICIO::ModificarPersonaTelefono");
										cl.bcs.socionegocio.per.ModificarPersonaTelefono.SalidaModificarPersonaTelefono salidaModificarTelefono = modificarTelefono.logica(conn, inModificarTelefono);
										logger.debug("FIN::ModificarPersonaTelefono");
										if(salidaModificarTelefono.P_OUT_ERRORESCONTADOR > 0)
										{
											entidadModificada.TipoEntidad = TipoEntidad;
											entidadModificada.IdEntidad = IdEntidad;

											for(MensajeSalida mensaje : salidaModificarTelefono.P_OUT_MENSAJES)
											{
												if(!MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)) continue;
												mensajesErrores.IdInterno = 0L;
												mensajesErrores.TipoMensaje = "ERROR";
												mensajesErrores.Codigo = mensaje.codigo;
												mensajesErrores.Mensaje = mensaje.descripcion;

												listMensajesErrores.add(mensajesErrores);
												entidadModificada.Mensajes = listMensajesErrores;
												listEntidadModificada.add(entidadModificada);
												ContadorErrores++;
												break;
											}

											for(MensajeSalida mensaje : salidaModificarTelefono.P_OUT_MENSAJES)
											{
												if(MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)) continue;
												mensajesErrores.IdInterno = 0L;
												mensajesErrores.TipoMensaje = mensaje.tipo;
												mensajesErrores.Codigo = mensaje.codigo;
												mensajesErrores.Mensaje = mensaje.descripcion;

												listMensajesErrores.add(mensajesErrores);
												entidadModificada.Mensajes = listMensajesErrores;
												listEntidadModificada.add(entidadModificada);
												ContadorErrores++;
												break;
											}

											break;
										}
									}
								}
								else if(telefono.Accion.equals("ELIMINAR")) {
									
									HashMap<String,Object> parametroSelect = new HashMap<String,Object>();
									parametroSelect.put("IdPersona", IdPersona);
									Long contadorTelefonos = sql.query(conn, SQL_CONTADOR_TELEFONOS, parametroSelect, ContadorSelect.class).stream().findFirst().orElse(new ContadorSelect()).Contador;
									if(contadorTelefonos == 1)
									{
										String codigoCampo = conceptosCodigoCampo.get("TELEFONOS");
										if( alMenosUnRegistro(conn,IdPersona, codigoCampo, in.p_IdTipoCanal)) {
											String descripcion = Diccionario.getMensaje(conn, in.p_IdEmpresa,"ERROR_TELEFONO_REQUERIDO", new HashMap<>());  
											mensajesErrores.IdInterno = 0L;
											mensajesErrores.TipoMensaje = "ERROR";
											mensajesErrores.Codigo = "ERROR_TELEFONO_REQUERIDO" ;
											mensajesErrores.Mensaje = descripcion;
											
											listMensajesErrores.add(mensajesErrores);
											entidadModificada.Mensajes = listMensajesErrores;
											listEntidadModificada.add(entidadModificada);
											ContadorErrores++;
											break;
										}
										
									}
									
									cl.bcs.socionegocio.per.EliminarPersonaTelefono eliminarTelefono = new cl.bcs.socionegocio.per.EliminarPersonaTelefono();
									cl.bcs.socionegocio.per.EliminarPersonaTelefono.EntradaServicioEliminarPersonaTelefono inEliminarTelefono = new cl.bcs.socionegocio.per.EliminarPersonaTelefono.EntradaServicioEliminarPersonaTelefono();
									inEliminarTelefono.p_IdEmpresa = in.p_IdEmpresa;
									inEliminarTelefono.p_IdUsuario = in.p_IdUsuario;
									inEliminarTelefono.p_IdModulo = "SOC";
									inEliminarTelefono.p_UsaReglasNegocio = "N";
									inEliminarTelefono.p_IdTelefono = telefono.IdTelefono;
									inEliminarTelefono.p_Comentario = "";

									logger.debug("INICIO::EliminarPersonaTelefono");
									cl.bcs.socionegocio.per.EliminarPersonaTelefono.SalidaEliminarPersonaTelefono salidaEliminarTelefono = eliminarTelefono.logica(conn, inEliminarTelefono);
									logger.debug("FIN::EliminarPersonaTelefono");
									if(salidaEliminarTelefono.P_OUT_ERRORESCONTADOR > 0)
									{
										entidadModificada.TipoEntidad = TipoEntidad;
										entidadModificada.IdEntidad = IdEntidad;

										for(ErrorDetalle errorDetalle : salidaEliminarTelefono.P_OUT_ERRORESDETALLE)
										{
											mensajesErrores.IdInterno = 0L;
											mensajesErrores.TipoMensaje = "ERROR";
											mensajesErrores.Codigo = errorDetalle.Codigo;
											mensajesErrores.Mensaje = errorDetalle.Descripcion;

											listMensajesErrores.add(mensajesErrores);
											entidadModificada.Mensajes = listMensajesErrores;
											listEntidadModificada.add(entidadModificada);
											ContadorErrores++;
											break;
										}

										for(AdvertenciaDetalle advertenciaDetalle : salidaEliminarTelefono.P_OUT_ADVERTENCIASDETALLE)
										{
											mensajesErrores.IdInterno = 0L;
											mensajesErrores.TipoMensaje = "ADVERTENCIA";
											mensajesErrores.Codigo = advertenciaDetalle.Codigo;
											mensajesErrores.Mensaje = advertenciaDetalle.Descripcion;

											listMensajesErrores.add(mensajesErrores);
											entidadModificada.Mensajes = listMensajesErrores;
											listEntidadModificada.add(entidadModificada);
											ContadorErrores++;
											break;
										}

										break;
									}
								}
							}
							
							if(ContadorErrores > 0) break;
						}
						
						
						// ***************************************************
						// AGREGAR, MODIFICAR Y ELIMINAR DIRECCION ELECTRONICA
						// ***************************************************
						if(!socioNegocio.DireccionElectronica.isEmpty())
						{
							for(DireccionElectronica direccionElectronica : socioNegocio.DireccionElectronica)
							{
								if(direccionElectronica.Accion.equals("AGREGAR"))
								{
									CrearDireccionElectronica crearDireccionelectronica = new CrearDireccionElectronica();
									CrearDireccionElectronica.EntradaCrearDireccionElectronica inDireccionElectronica = new CrearDireccionElectronica.EntradaCrearDireccionElectronica();
									inDireccionElectronica.p_IdUsuario = in.p_IdUsuario;
									inDireccionElectronica.p_IdEmpresa = in.p_IdEmpresa;
									inDireccionElectronica.p_IdModulo = "SOC";
									inDireccionElectronica.p_UsaReglasNegocio = "N";
									inDireccionElectronica.p_Direccion = direccionElectronica.Direccion;
									inDireccionElectronica.p_CodigoProposito = direccionElectronica.IdProposito;
									inDireccionElectronica.p_Comentario = "";
									inDireccionElectronica.p_IdEstado = "ACT";
									inDireccionElectronica.p_Tipo = direccionElectronica.Tipo;
									inDireccionElectronica.p_IdPersona = IdPersona;
									CrearDireccionElectronica.SalidaCrearDireccionElectronica salidaCrearDireccionElectronica = crearDireccionelectronica.logica(conn, inDireccionElectronica);
									
									if(salidaCrearDireccionElectronica.P_OUT_ERRORESCONTADOR > 0)
									{
										entidadModificada.TipoEntidad = TipoEntidad;
										entidadModificada.IdEntidad = IdEntidad;
										
										for (ErrorDetalle errorDetalle : salidaCrearDireccionElectronica.P_OUT_ERRORESDETALLE)
										{
											mensajesErrores.IdInterno = 0L;
											mensajesErrores.TipoMensaje = "ERROR";
											mensajesErrores.Codigo = errorDetalle.Codigo;
											mensajesErrores.Mensaje = errorDetalle.Descripcion;
											
											listMensajesErrores.add(mensajesErrores);
											entidadModificada.Mensajes = listMensajesErrores;
											listEntidadModificada.add(entidadModificada);
											ContadorErrores++;
											break;
										}
										
										break;
									}
								}
								else if(direccionElectronica.Accion.equals("MODIFICAR"))
								{
									List<RegistroCambio> listRegistroCambio = new ArrayList<RegistroCambio>();
							        
							        if(!direccionElectronica.IdProposito.isEmpty())
									{
							        	RegistroCambio registroCambio = new RegistroCambio();
										registroCambio.CampoModificado	= "CodigoProposito";
										registroCambio.ValorNuevo		= direccionElectronica.IdProposito.trim();
										listRegistroCambio.add(registroCambio);
									}
							        
							        if(!direccionElectronica.Direccion.isEmpty())
									{
							        	RegistroCambio registroCambio = new RegistroCambio();
										registroCambio.CampoModificado	= "Direccion";
										registroCambio.ValorNuevo		= direccionElectronica.Direccion.trim();
										listRegistroCambio.add(registroCambio);
									}
							        
									if(!listRegistroCambio.isEmpty())
									{
										cl.bcs.socionegocio.per.ModificarPersonaDireccionElectronica modificarDireccionElectronica = new cl.bcs.socionegocio.per.ModificarPersonaDireccionElectronica();
										cl.bcs.socionegocio.per.ModificarPersonaDireccionElectronica.EntradaModificarPersonaDireccionElectronica inModificarDireccionElectronica = new cl.bcs.socionegocio.per.ModificarPersonaDireccionElectronica.EntradaModificarPersonaDireccionElectronica();
										inModificarDireccionElectronica.p_IdEmpresa = in.p_IdEmpresa;
										inModificarDireccionElectronica.p_IdUsuario = in.p_IdUsuario;
										inModificarDireccionElectronica.p_IdModulo = "SOC";
										inModificarDireccionElectronica.p_UsaReglasNegocio = "N";
										inModificarDireccionElectronica.p_IdDireccionElectronica = direccionElectronica.IdDireccionElectronica;
										for(RegistroCambio r : listRegistroCambio){
											cl.bcs.socionegocio.per.ModificarPersonaDireccionElectronica.RegistroCambio campo = new cl.bcs.socionegocio.per.ModificarPersonaDireccionElectronica.RegistroCambio();
											campo.CampoModificado = r.CampoModificado;
											campo.ValorNuevo = r.ValorNuevo;
											inModificarDireccionElectronica.p_RegistroCambio.add(campo);
										}

										logger.debug("INICIO::ModificarPersonaDireccionElectronica");
										cl.bcs.socionegocio.per.ModificarPersonaDireccionElectronica.SalidaModificarPersonaDireccionElectronica salidaModificarDireccionElectronica = modificarDireccionElectronica.logica(conn, inModificarDireccionElectronica);
										logger.debug("FIN::ModificarPersonaDireccionElectronica");
										if(salidaModificarDireccionElectronica.P_OUT_ERRORESCONTADOR > 0)
										{
											entidadModificada.TipoEntidad = TipoEntidad;
											entidadModificada.IdEntidad = IdEntidad;

											for(MensajeSalida mensaje : salidaModificarDireccionElectronica.P_OUT_MENSAJES)
											{
												if(!MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)) continue;
												mensajesErrores.IdInterno = 0L;
												mensajesErrores.TipoMensaje = "ERROR";
												mensajesErrores.Codigo = mensaje.codigo;
												mensajesErrores.Mensaje = mensaje.descripcion;

												listMensajesErrores.add(mensajesErrores);
												entidadModificada.Mensajes = listMensajesErrores;
												listEntidadModificada.add(entidadModificada);
												ContadorErrores++;
												break;
											}

											for(MensajeSalida mensaje : salidaModificarDireccionElectronica.P_OUT_MENSAJES)
											{
												if(MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)) continue;
												mensajesErrores.IdInterno = 0L;
												mensajesErrores.TipoMensaje = mensaje.tipo;
												mensajesErrores.Codigo = mensaje.codigo;
												mensajesErrores.Mensaje = mensaje.descripcion;

												listMensajesErrores.add(mensajesErrores);
												entidadModificada.Mensajes = listMensajesErrores;
												listEntidadModificada.add(entidadModificada);
												ContadorErrores++;
												break;
											}

											break;
										}
									}
								}
								else if(direccionElectronica.Accion.equals("ELIMINAR")) {
									
									HashMap<String,Object> parametroSelect = new HashMap<String,Object>();
									parametroSelect.put("IdPersona", IdPersona);
									Long contadorDireccionesElectronicas = sql.query(conn, SQL_CONTADOR_DIRECCIONES_ELECTRONICAS, parametroSelect, ContadorSelect.class).stream().findFirst().orElse(new ContadorSelect()).Contador;
									if(contadorDireccionesElectronicas == 1)
									{
										String codigoCampo = conceptosCodigoCampo.get("DIRECCIONES_ELECTRONICAS");
										if( alMenosUnRegistro(conn,IdPersona, codigoCampo, in.p_IdTipoCanal)) {
											String descripcion = Diccionario.getMensaje(conn, in.p_IdEmpresa,"ERROR_DIRECCION_ELECTRONICA_REQUERIDA", new HashMap<>());  
											mensajesErrores.IdInterno = 0L;
											mensajesErrores.TipoMensaje = "ERROR";
											mensajesErrores.Codigo = "ERROR_DIRECCION_ELECTRONICA_REQUERIDA" ;
											mensajesErrores.Mensaje = descripcion;
											
											listMensajesErrores.add(mensajesErrores);
											entidadModificada.Mensajes = listMensajesErrores;
											listEntidadModificada.add(entidadModificada);
											ContadorErrores++;
											break;
										}
										
									}
									
									cl.bcs.socionegocio.per.EliminarPersonaDireccionElectronica eliminarDireccionElectronica = new cl.bcs.socionegocio.per.EliminarPersonaDireccionElectronica();
									cl.bcs.socionegocio.per.EliminarPersonaDireccionElectronica.EntradaServicioEliminarPersonaDireccionElectronica inEliminarDireccionElectronica = new cl.bcs.socionegocio.per.EliminarPersonaDireccionElectronica.EntradaServicioEliminarPersonaDireccionElectronica();
									inEliminarDireccionElectronica.p_IdEmpresa = in.p_IdEmpresa;
									inEliminarDireccionElectronica.p_IdUsuario = in.p_IdUsuario;
									inEliminarDireccionElectronica.p_IdModulo = "SOC";
									inEliminarDireccionElectronica.p_UsaReglasNegocio = "N";
									inEliminarDireccionElectronica.p_IdDireccionElectronica = direccionElectronica.IdDireccionElectronica;

									logger.debug("INICIO::EliminarPersonaDireccionElectronica");
									cl.bcs.socionegocio.per.EliminarPersonaDireccionElectronica.SalidaEliminarPersonaDireccionElectronica salidaEliminarDireccionElectronica = eliminarDireccionElectronica.logica(conn, inEliminarDireccionElectronica);
									logger.debug("FIN::EliminarPersonaDireccionElectronica");
									if(salidaEliminarDireccionElectronica.P_OUT_ERRORESCONTADOR > 0)
									{
										entidadModificada.TipoEntidad = TipoEntidad;
										entidadModificada.IdEntidad = IdEntidad;

										for(ErrorDetalle errorDetalle : salidaEliminarDireccionElectronica.P_OUT_ERRORESDETALLE)
										{
											mensajesErrores.IdInterno = 0L;
											mensajesErrores.TipoMensaje = "ERROR";
											mensajesErrores.Codigo = errorDetalle.Codigo;
											mensajesErrores.Mensaje = errorDetalle.Descripcion;

											listMensajesErrores.add(mensajesErrores);
											entidadModificada.Mensajes = listMensajesErrores;
											listEntidadModificada.add(entidadModificada);
											ContadorErrores++;
											break;
										}

										for(AdvertenciaDetalle advertenciaDetalle : salidaEliminarDireccionElectronica.P_OUT_ADVERTENCIASDETALLE)
										{
											mensajesErrores.IdInterno = 0L;
											mensajesErrores.TipoMensaje = "ADVERTENCIA";
											mensajesErrores.Codigo = advertenciaDetalle.Codigo;
											mensajesErrores.Mensaje = advertenciaDetalle.Descripcion;

											listMensajesErrores.add(mensajesErrores);
											entidadModificada.Mensajes = listMensajesErrores;
											listEntidadModificada.add(entidadModificada);
											ContadorErrores++;
											break;
										}

										break;
									}
								}
							}
							
							if(ContadorErrores > 0) break;
						}
						
						
						// **************************
						// AGREGAR INDICARDOR PERSONA
						// **************************
						if(!socioNegocio.IndicadorPersona.isEmpty())
							{
								List<IndicadorPersonaCliente> listaIndicadorPersonaCliente = new ArrayList<IndicadorPersonaCliente>();
								
								for(IndicadorPersona indicadorPersona : socioNegocio.IndicadorPersona)
								{
									//Consulta si existe indicador en el sistema
									HashMap<String,Object> parametrosconsultaIndicador = new HashMap<String,Object>();
									parametrosconsultaIndicador.put("p_CodigoTipoIndicador",indicadorPersona.CodigoTipoIndicador);
									logger.debug("INICIO::SQL_SELECT_INDICADORES_EXISTE");
									List<SelectConsultarIndicadoresExiste> SelectConsultaIndicadorExiste = sql.query(conn, SQL_SELECT_INDICADORES_EXISTE, parametrosconsultaIndicador, SelectConsultarIndicadoresExiste.class);
									logger.debug("FIN::SQL_SELECT_INDICADORES_EXISTE");
									if(SelectConsultaIndicadorExiste.isEmpty()) {
										return salida = mesajesDeError("PERSONA", IdEntidad, 0L, "ERROR", "INDICADOR_INVALIDO", "NO EXISTE INDICADOR EN EL SISTEMA");
									}
									
									//CONSULTA INDICADOR RELACIONADO A PERSONA
									HashMap<String,Object> parametrosconsultaIndicadorPersona = new HashMap<String,Object>();
									parametrosconsultaIndicadorPersona.put("p_CodigoTipoIndicador",indicadorPersona.CodigoTipoIndicador);
									parametrosconsultaIndicadorPersona.put("p_IdPersona", IdPersona);
									logger.debug("INICIO::SQL_SELECT_INDICADORES_PERSONA");
									List<SelectConsultarIndicadores> selectConsultarIndicadoresPersona = sql.query(conn, SQL_SELECT_INDICADORES_PERSONA, parametrosconsultaIndicadorPersona, SelectConsultarIndicadores.class);
									logger.debug("FIN::SQL_SELECT_INDICADORES_PERSONA");
									if(selectConsultarIndicadoresPersona.isEmpty()) {
										return salida = mesajesDeError("PERSONA", IdEntidad, 0L, "ERROR", "INDICADOR_ENTIDAD_PERSONA_NO_EXISTE", "NO EXISTE EL INDICADOR PARA LA ENTIDAD PERSONA");
										
									}
									
									IndicadorPersonaCliente indicadorPersonaCliente = new IndicadorPersonaCliente();
									indicadorPersonaCliente.IdIndicador = selectConsultarIndicadoresPersona.get(0).IdIndicador;
									indicadorPersonaCliente.IdTipoIndicador = selectConsultarIndicadoresPersona.get(0).IdTipoIndicador;
									indicadorPersonaCliente.ValorIndicador = indicadorPersona.ValorIndicador;
									indicadorPersonaCliente.FechaVencimiento = Fecha.toDate("0001-01-01 00:00:00");
									listaIndicadorPersonaCliente.add(indicadorPersonaCliente);
								}
								
								cl.bcs.plataforma.ind.ModificarIndicador modificarIndicadorPersona = new cl.bcs.plataforma.ind.ModificarIndicador();
								cl.bcs.plataforma.ind.ModificarIndicador.EntradaModificarIndicador inModificarIndicadorPersona = new cl.bcs.plataforma.ind.ModificarIndicador.EntradaModificarIndicador();
								inModificarIndicadorPersona.p_IdEmpresa = in.p_IdEmpresa;
								inModificarIndicadorPersona.p_IdUsuario = in.p_IdUsuario;
								inModificarIndicadorPersona.p_IdModulo = "SOC";
								inModificarIndicadorPersona.p_UsaReglasNegocio = "N";
								inModificarIndicadorPersona.p_IdTipoEntidad = "PERSONA";
								inModificarIndicadorPersona.p_IdEntidad = IdPersona;
								inModificarIndicadorPersona.p_ModeloCliente = "";
								for(IndicadorPersonaCliente ind : listaIndicadorPersonaCliente){
									cl.bcs.plataforma.ind.ModificarIndicador.RegistroAtributo atributo = new cl.bcs.plataforma.ind.ModificarIndicador.RegistroAtributo();
									atributo.IdIndicador = ind.IdIndicador;
									atributo.IdTipoIndicador = ind.IdTipoIndicador;
									atributo.ValorIndicador = ind.ValorIndicador;
									atributo.FechaVencimiento = new java.sql.Timestamp(ind.FechaVencimiento.getTime());
									inModificarIndicadorPersona.p_RegistroAtributo.add(atributo);
								}

								logger.debug("INICIO::ModificarIndicador");
								cl.bcs.plataforma.ind.ModificarIndicador.SalidaModificarIndicador salidaIndicadorPersona = modificarIndicadorPersona.logica(conn, inModificarIndicadorPersona);
								logger.debug("FIN::ModificarIndicador");
								if(salidaIndicadorPersona.P_OUT_ERRORESCONTADOR > 0)
								{
									entidadModificada.TipoEntidad = TipoEntidad;
									entidadModificada.IdEntidad = IdEntidad;

									for(MensajeSalida mensaje : salidaIndicadorPersona.P_OUT_MENSAJES)
									{
										if(!MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)) continue;
										mensajesErrores.IdInterno = 0L;
										mensajesErrores.TipoMensaje = "ERROR";
										mensajesErrores.Codigo = mensaje.codigo;
										mensajesErrores.Mensaje = mensaje.descripcion;

										listMensajesErrores.add(mensajesErrores);
										entidadModificada.Mensajes = listMensajesErrores;
										listEntidadModificada.add(entidadModificada);
										break;
									}

									for(MensajeSalida mensaje : salidaIndicadorPersona.P_OUT_MENSAJES)
									{
										if(MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)) continue;
										mensajesErrores.IdInterno = 0L;
										mensajesErrores.TipoMensaje = mensaje.tipo;
										mensajesErrores.Codigo = mensaje.codigo;
										mensajesErrores.Mensaje = mensaje.descripcion;

										listMensajesErrores.add(mensajesErrores);
										entidadModificada.Mensajes = listMensajesErrores;
										listEntidadModificada.add(entidadModificada);
										break;
									}

									break;
								}
							}
						
						
						
						// **************************
						// AGREGAR INDICARDOR CLIENTE
						// **************************
						if (TipoEntidad.trim().equals("CLIENTE"))
						{
							if(!socioNegocio.IndicadorCliente.isEmpty())
							{
								List<IndicadorPersonaCliente> listaIndicadorPersonaCliente = new ArrayList<IndicadorPersonaCliente>();
								
								for(IndicadorCliente indicadorCliente : socioNegocio.IndicadorCliente)
								{
									HashMap<String,Object> parametroEntradaIndicador = new HashMap<String,Object>();
									parametroEntradaIndicador.put("p_IdPersona", null);
									parametroEntradaIndicador.put("p_IdCliente", IdCliente);
									parametroEntradaIndicador.put("p_CodigoTipoIndicador", indicadorCliente.CodigoTipoIndicador);
									logger.debug("INICIO::SQL_SELECT_INDICADORES");
									List<SelectConsultarIndicadores> SelectConsultarIndicadores = sql.query(conn, SQL_SELECT_INDICADORES, parametroEntradaIndicador, SelectConsultarIndicadores.class);
									logger.debug("FIN::SQL_SELECT_INDICADORES");
									IndicadorPersonaCliente indicadorPersonaCliente = new IndicadorPersonaCliente();
									indicadorPersonaCliente.IdIndicador = SelectConsultarIndicadores.get(0).IdIndicador;
									indicadorPersonaCliente.IdTipoIndicador = SelectConsultarIndicadores.get(0).IdTipoIndicador;
									indicadorPersonaCliente.ValorIndicador = indicadorCliente.ValorIndicador;
									indicadorPersonaCliente.FechaVencimiento = Fecha.toDate("0001-01-01 00:00:00");
									listaIndicadorPersonaCliente.add(indicadorPersonaCliente);
								}
								
								cl.bcs.plataforma.ind.ModificarIndicador modificarIndicadorCliente = new cl.bcs.plataforma.ind.ModificarIndicador();
								cl.bcs.plataforma.ind.ModificarIndicador.EntradaModificarIndicador inModificarIndicadorCliente = new cl.bcs.plataforma.ind.ModificarIndicador.EntradaModificarIndicador();
								inModificarIndicadorCliente.p_IdEmpresa = in.p_IdEmpresa;
								inModificarIndicadorCliente.p_IdUsuario = in.p_IdUsuario;
								inModificarIndicadorCliente.p_IdModulo = "SOC";
								inModificarIndicadorCliente.p_UsaReglasNegocio = "N";
								inModificarIndicadorCliente.p_IdTipoEntidad = "CLIENTE";
								inModificarIndicadorCliente.p_IdEntidad = IdCliente;
								inModificarIndicadorCliente.p_ModeloCliente = "";
								for(IndicadorPersonaCliente ind : listaIndicadorPersonaCliente){
									cl.bcs.plataforma.ind.ModificarIndicador.RegistroAtributo atributo = new cl.bcs.plataforma.ind.ModificarIndicador.RegistroAtributo();
									atributo.IdIndicador = ind.IdIndicador;
									atributo.IdTipoIndicador = ind.IdTipoIndicador;
									atributo.ValorIndicador = ind.ValorIndicador;
									atributo.FechaVencimiento = new java.sql.Timestamp(ind.FechaVencimiento.getTime());
									inModificarIndicadorCliente.p_RegistroAtributo.add(atributo);
								}

								logger.debug("INICIO::ModificarIndicador");
								cl.bcs.plataforma.ind.ModificarIndicador.SalidaModificarIndicador salidaIndicadorCliente = modificarIndicadorCliente.logica(conn, inModificarIndicadorCliente);
								logger.debug("FIN::ModificarIndicador");
								if(salidaIndicadorCliente.P_OUT_ERRORESCONTADOR > 0)
								{
									entidadModificada.TipoEntidad = socioNegocio.TipoEntidad.trim();
									entidadModificada.IdEntidad = socioNegocio.IdEntidad;

									for(MensajeSalida mensaje : salidaIndicadorCliente.P_OUT_MENSAJES)
									{
										if(!MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)) continue;
										mensajesErrores.IdInterno = 0L;
										mensajesErrores.TipoMensaje = "ERROR";
										mensajesErrores.Codigo = mensaje.codigo;
										mensajesErrores.Mensaje = mensaje.descripcion;

										listMensajesErrores.add(mensajesErrores);
										entidadModificada.Mensajes = listMensajesErrores;
										listEntidadModificada.add(entidadModificada);
										break;
									}

									for(MensajeSalida mensaje : salidaIndicadorCliente.P_OUT_MENSAJES)
									{
										if(MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)) continue;
										mensajesErrores.IdInterno = 0L;
										mensajesErrores.TipoMensaje = mensaje.tipo;
										mensajesErrores.Codigo = mensaje.codigo;
										mensajesErrores.Mensaje = mensaje.descripcion;

										listMensajesErrores.add(mensajesErrores);
										entidadModificada.Mensajes = listMensajesErrores;
										listEntidadModificada.add(entidadModificada);
										break;
									}

									break;
								}
							}
						}
						
						
						// *********************************************
						// AGREGAR, MODIFICAR Y ELIMINAR CUENTA BANCARIA
						// *********************************************
						if(!socioNegocio.CuentaBancaria.isEmpty())
						{
							for(CuentaBancaria cuentaBancaria : socioNegocio.CuentaBancaria)
							{
								if(cuentaBancaria.Accion.equals("AGREGAR"))
								{
									cl.bcs.socionegocio.per.CrearPersonaCuentaBancaria crearCuentaBancaria = new cl.bcs.socionegocio.per.CrearPersonaCuentaBancaria();
									cl.bcs.socionegocio.per.CrearPersonaCuentaBancaria.EntradaCrearPersonaCuentaBancaria inCrearCuentaBancaria = new cl.bcs.socionegocio.per.CrearPersonaCuentaBancaria.EntradaCrearPersonaCuentaBancaria();
									inCrearCuentaBancaria.p_IdEmpresa = in.p_IdEmpresa;
									inCrearCuentaBancaria.p_IdUsuario = in.p_IdUsuario;
									inCrearCuentaBancaria.p_IdModulo = "SOC";
									inCrearCuentaBancaria.p_UsaReglasNegocio = "N";

									cl.bcs.socionegocio.per.CrearPersonaCuentaBancaria.InCuentaBancaria inCuentaBancaria = new cl.bcs.socionegocio.per.CrearPersonaCuentaBancaria.InCuentaBancaria();
									inCuentaBancaria.IdBanco = cuentaBancaria.IdBanco;
									inCuentaBancaria.IdTipoCuentaBancaria = cuentaBancaria.CodigoTipoCuentaBancaria;
									inCuentaBancaria.IdMoneda = cuentaBancaria.IdMoneda;
									inCuentaBancaria.NumeroCuentaBancaria = cuentaBancaria.NumeroCuentaBancaria;
									inCuentaBancaria.IdPersona = IdPersona;
									cuentaBancaria.IdEstado = cuentaBancaria.IdEstado != ""? cuentaBancaria.IdEstado : "ACT";
									inCuentaBancaria.IdEstado = cuentaBancaria.IdEstado;
									inCuentaBancaria.IdPlaza = cuentaBancaria.CodigoPlaza;
									inCuentaBancaria.CrearPreferenciaPago = "N";
									inCuentaBancaria.IdCliente = IdCliente;
									inCuentaBancaria.EsPac = "N";
									inCuentaBancaria.IdTipoIdentificadorCuenta = "";
									inCuentaBancaria.IdentificadorCuenta = "";
									inCuentaBancaria.IdPersonaRelacionada = 0L;
									inCuentaBancaria.CodigoCuentaInterbancario = "";
									inCuentaBancaria.CodigoAba = "";
									inCuentaBancaria.OtroCodigoBancoExtranjero = "";
									inCuentaBancaria.CodigoIban = "";
									inCuentaBancaria.OtroCodigoCuentaExtranjero = "";
									inCrearCuentaBancaria.p_InCuentaBancaria.add(inCuentaBancaria);

									logger.debug("INICIO::CrearPersonaCuentaBancaria");
									cl.bcs.socionegocio.per.CrearPersonaCuentaBancaria.SalidaCrearPersonaCuentaBancaria salidaCuentaBancaria = crearCuentaBancaria.logica(conn, inCrearCuentaBancaria);
									logger.debug("FIN::CrearPersonaCuentaBancaria");
									if(salidaCuentaBancaria.P_OUT_ERRORESCONTADOR > 0)
									{
										entidadModificada.TipoEntidad = TipoEntidad;
										entidadModificada.IdEntidad = IdEntidad;

										for(MensajeSalida mensaje : salidaCuentaBancaria.P_OUT_MENSAJES)
										{
											if(!MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)) continue;
											mensajesErrores.IdInterno = 0L;
											mensajesErrores.TipoMensaje = "ERROR";
											mensajesErrores.Codigo = mensaje.codigo;
											mensajesErrores.Mensaje = mensaje.descripcion;

											listMensajesErrores.add(mensajesErrores);
											entidadModificada.Mensajes = listMensajesErrores;
											listEntidadModificada.add(entidadModificada);
											ContadorErrores++;
											break;
										}

										for(MensajeSalida mensaje : salidaCuentaBancaria.P_OUT_MENSAJES)
										{
											if(MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)) continue;
											mensajesErrores.IdInterno = 0L;
											mensajesErrores.TipoMensaje = mensaje.tipo;
											mensajesErrores.Codigo = mensaje.codigo;
											mensajesErrores.Mensaje = mensaje.descripcion;

											listMensajesErrores.add(mensajesErrores);
											entidadModificada.Mensajes = listMensajesErrores;
											listEntidadModificada.add(entidadModificada);
											ContadorErrores++;
											break;
										}

										break;
									}
								}
								else if(cuentaBancaria.Accion.equals("MODIFICAR"))
								{
									List<RegistroCambio> listRegistroCambio = new ArrayList<RegistroCambio>();
							        
							        if(!cuentaBancaria.IdEstado.isEmpty())
									{
							        	RegistroCambio registroCambio = new RegistroCambio();
										registroCambio.CampoModificado	= "IdEstado";
										registroCambio.ValorNuevo		= cuentaBancaria.IdEstado.trim();
										listRegistroCambio.add(registroCambio);
									}
							        
							        if(!cuentaBancaria.NumeroCuentaBancaria.isEmpty())
									{
							        	RegistroCambio registroCambio = new RegistroCambio();
										registroCambio.CampoModificado	= "NumeroCuentaBancaria";
										registroCambio.ValorNuevo		= cuentaBancaria.NumeroCuentaBancaria.trim();
										listRegistroCambio.add(registroCambio);
									}
							        
							        if(!cuentaBancaria.IdMoneda.isEmpty())
									{
							        	RegistroCambio registroCambio = new RegistroCambio();
										registroCambio.CampoModificado	= "IdMoneda";
										registroCambio.ValorNuevo		= cuentaBancaria.IdMoneda.trim();
										listRegistroCambio.add(registroCambio);
									}
							        
							        if(!cuentaBancaria.CodigoTipoCuentaBancaria.isEmpty())
									{
							        	RegistroCambio registroCambio = new RegistroCambio();
										registroCambio.CampoModificado	= "IdTipoCuentaBancaria";
										registroCambio.ValorNuevo		= cuentaBancaria.CodigoTipoCuentaBancaria.trim();
										listRegistroCambio.add(registroCambio);
									}
							        
									if(cuentaBancaria.IdBanco > 0L)
									{
										RegistroCambio registroCambio = new RegistroCambio();
										registroCambio.CampoModificado	= "IdBanco";
										registroCambio.ValorNuevo		= String.valueOf(cuentaBancaria.IdBanco);
										listRegistroCambio.add(registroCambio);
									}
									
									/*
							        if(!cuentaBancaria.CodigoSucursal.isEmpty())
									{
										RegistroCambio registroCambio = new RegistroCambio();
										registroCambio.CampoModificado	= "";
										registroCambio.ValorNuevo		= cuentaBancaria.CodigoSucursal.trim();
										listRegistroCambio.add(registroCambio);
									}
									*/
									
									if(cuentaBancaria.CodigoPlaza > 0L)
									{
										RegistroCambio registroCambio = new RegistroCambio();
										registroCambio.CampoModificado	= "IdPlaza";
										registroCambio.ValorNuevo		= String.valueOf(cuentaBancaria.CodigoPlaza);
										listRegistroCambio.add(registroCambio);
									}
									
									if(!listRegistroCambio.isEmpty())
									{
										cl.bcs.socionegocio.per.ModificarPersonaCuentaBancaria modificarCuentaBancaria = new cl.bcs.socionegocio.per.ModificarPersonaCuentaBancaria();
										cl.bcs.socionegocio.per.ModificarPersonaCuentaBancaria.EntradaModificarPersonaCuentaBancaria inModificarCuentaBancaria = new cl.bcs.socionegocio.per.ModificarPersonaCuentaBancaria.EntradaModificarPersonaCuentaBancaria();
										inModificarCuentaBancaria.p_IdEmpresa = in.p_IdEmpresa;
										inModificarCuentaBancaria.p_IdUsuario = in.p_IdUsuario;
										inModificarCuentaBancaria.p_IdModulo = "SOC";
										inModificarCuentaBancaria.p_UsaReglasNegocio = "N";
										inModificarCuentaBancaria.p_IdCuentaBancaria = cuentaBancaria.IdCuentaBancaria;
										for(RegistroCambio r : listRegistroCambio){
											cl.bcs.socionegocio.per.ModificarPersonaCuentaBancaria.RegistroCambio campo = new cl.bcs.socionegocio.per.ModificarPersonaCuentaBancaria.RegistroCambio();
											campo.CampoModificado = r.CampoModificado;
											campo.ValorNuevo = r.ValorNuevo;
											inModificarCuentaBancaria.p_RegistroCambio.add(campo);
										}

										logger.debug("INICIO::ModificarPersonaCuentaBancaria");
										cl.bcs.socionegocio.per.ModificarPersonaCuentaBancaria.SalidaModificarPersonaCuentaBancaria salidaModificarCuentaBancaria = modificarCuentaBancaria.logica(conn, inModificarCuentaBancaria);
										logger.debug("FIN::ModificarPersonaCuentaBancaria");
										if(salidaModificarCuentaBancaria.P_OUT_ERRORESCONTADOR > 0)
										{
											entidadModificada.TipoEntidad = TipoEntidad;
											entidadModificada.IdEntidad = IdEntidad;

											for(MensajeSalida mensaje : salidaModificarCuentaBancaria.P_OUT_MENSAJES)
											{
												if(!MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)) continue;
												mensajesErrores.IdInterno = 0L;
												mensajesErrores.TipoMensaje = "ERROR";
												mensajesErrores.Codigo = mensaje.codigo;
												mensajesErrores.Mensaje = mensaje.descripcion;

												listMensajesErrores.add(mensajesErrores);
												entidadModificada.Mensajes = listMensajesErrores;
												listEntidadModificada.add(entidadModificada);
												ContadorErrores++;
												break;
											}

											for(MensajeSalida mensaje : salidaModificarCuentaBancaria.P_OUT_MENSAJES)
											{
												if(MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)) continue;
												mensajesErrores.IdInterno = 0L;
												mensajesErrores.TipoMensaje = mensaje.tipo;
												mensajesErrores.Codigo = mensaje.codigo;
												mensajesErrores.Mensaje = mensaje.descripcion;

												listMensajesErrores.add(mensajesErrores);
												entidadModificada.Mensajes = listMensajesErrores;
												listEntidadModificada.add(entidadModificada);
												ContadorErrores++;
												break;
											}

											break;
										}
									}
								}
								else if(cuentaBancaria.Accion.equals("ELIMINAR")) {
									
									HashMap<String,Object> parametroSelect = new HashMap<String,Object>();
									parametroSelect.put("IdPersona", IdPersona);
									Long contadorCuentasBancarias = sql.query(conn, SQL_CONTADOR_CUENTAS_BANCARIAS, parametroSelect, ContadorSelect.class).stream().findFirst().orElse(new ContadorSelect()).Contador;
									if(contadorCuentasBancarias == 1)
									{
										String codigoCampo = conceptosCodigoCampo.get("CUENTAS_BANCARIAS");
										if( alMenosUnRegistro(conn,IdPersona, codigoCampo, in.p_IdTipoCanal)) {
											String descripcion = Diccionario.getMensaje(conn, in.p_IdEmpresa,"ERROR_CUENTA_BANCARIA_REQUERIDA", new HashMap<>());  
											mensajesErrores.IdInterno = 0L;
											mensajesErrores.TipoMensaje = "ERROR";
											mensajesErrores.Codigo = "ERROR_CUENTA_BANCARIA_REQUERIDA" ;
											mensajesErrores.Mensaje = descripcion;
											
											listMensajesErrores.add(mensajesErrores);
											entidadModificada.Mensajes = listMensajesErrores;
											listEntidadModificada.add(entidadModificada);
											ContadorErrores++;
											break;
										}
										
									}
									
									cl.bcs.socionegocio.per.EliminarPersonaCuentaBancaria eliminarCuentaBancaria = new cl.bcs.socionegocio.per.EliminarPersonaCuentaBancaria();
									cl.bcs.socionegocio.per.EliminarPersonaCuentaBancaria.EntradaServicioEliminarPersonaCuentaBancaria inEliminarCuentaBancaria = new cl.bcs.socionegocio.per.EliminarPersonaCuentaBancaria.EntradaServicioEliminarPersonaCuentaBancaria();
									inEliminarCuentaBancaria.p_IdEmpresa = in.p_IdEmpresa;
									inEliminarCuentaBancaria.p_IdUsuario = in.p_IdUsuario;
									inEliminarCuentaBancaria.p_IdModulo = "SOC";
									inEliminarCuentaBancaria.p_UsaReglasNegocio = "N";
									inEliminarCuentaBancaria.p_IdCuentaBancaria = cuentaBancaria.IdCuentaBancaria;
									inEliminarCuentaBancaria.p_Comentario = "";

									logger.debug("INICIO::EliminarPersonaCuentaBancaria");
									cl.bcs.socionegocio.per.EliminarPersonaCuentaBancaria.SalidaEliminarPersonaCuentaBancaria salidaEliminarCuentaBancaria = eliminarCuentaBancaria.logica(conn, inEliminarCuentaBancaria);
									logger.debug("FIN::EliminarPersonaCuentaBancaria");
									if(salidaEliminarCuentaBancaria.P_OUT_ERRORESCONTADOR > 0)
									{
										entidadModificada.TipoEntidad = TipoEntidad;
										entidadModificada.IdEntidad = IdEntidad;

										for(ErrorDetalle errorDetalle : salidaEliminarCuentaBancaria.P_OUT_ERRORESDETALLE)
										{
											mensajesErrores.IdInterno = 0L;
											mensajesErrores.TipoMensaje = "ERROR";
											mensajesErrores.Codigo = errorDetalle.Codigo;
											mensajesErrores.Mensaje = errorDetalle.Descripcion;

											listMensajesErrores.add(mensajesErrores);
											entidadModificada.Mensajes = listMensajesErrores;
											listEntidadModificada.add(entidadModificada);
											ContadorErrores++;
											break;
										}

										for(AdvertenciaDetalle advertenciaDetalle : salidaEliminarCuentaBancaria.P_OUT_ADVERTENCIASDETALLE)
										{
											mensajesErrores.IdInterno = 0L;
											mensajesErrores.TipoMensaje = "ADVERTENCIA";
											mensajesErrores.Codigo = advertenciaDetalle.Codigo;
											mensajesErrores.Mensaje = advertenciaDetalle.Descripcion;

											listMensajesErrores.add(mensajesErrores);
											entidadModificada.Mensajes = listMensajesErrores;
											listEntidadModificada.add(entidadModificada);
											ContadorErrores++;
											break;
										}

										break;
									}
								}
							}
							
							if(ContadorErrores > 0) break;
						}
						
						
						// **********************************************
						// AGREGAR, MODIFICAR Y ELIMINAR TARJETA BANCARIA
						// **********************************************
						if(!socioNegocio.TarjetasBancaria.isEmpty())
						{
							for(TarjetasBancaria tarjetasBancaria : socioNegocio.TarjetasBancaria)
							{
								if(tarjetasBancaria.Accion.equals("AGREGAR"))
								{
									cl.bcs.socionegocio.per.CrearPersonaTarjetaBancaria crearTarjetaBancaria = new cl.bcs.socionegocio.per.CrearPersonaTarjetaBancaria();
									cl.bcs.socionegocio.per.CrearPersonaTarjetaBancaria.EntradaCrearPersonaTarjetaBancaria inCrearTarjetaBancaria = new cl.bcs.socionegocio.per.CrearPersonaTarjetaBancaria.EntradaCrearPersonaTarjetaBancaria();
									inCrearTarjetaBancaria.p_IdEmpresa = in.p_IdEmpresa;
									inCrearTarjetaBancaria.p_IdUsuario = in.p_IdUsuario;
									inCrearTarjetaBancaria.p_IdModulo = "SOC";
									inCrearTarjetaBancaria.p_UsaReglasNegocio = "N";
									inCrearTarjetaBancaria.p_IdBanco = tarjetasBancaria.IdBanco;
									inCrearTarjetaBancaria.p_IdTipoTarjetaBancaria = tarjetasBancaria.IdTipo;
									inCrearTarjetaBancaria.p_NumeroTarjetaBancaria = tarjetasBancaria.NumeroTarjeta;
									inCrearTarjetaBancaria.p_IdPersona = IdPersona;
									inCrearTarjetaBancaria.p_FechaVencimiento = tarjetasBancaria.FechaVencimiento;
									inCrearTarjetaBancaria.p_CodigoVerificacion = !tarjetasBancaria.CodigoVerificacion.isEmpty() ? Long.parseLong(tarjetasBancaria.CodigoVerificacion.trim()) : null;
									inCrearTarjetaBancaria.p_IdEstado = "ACT";

									logger.debug("INICIO::CrearPersonaTarjetaBancaria");
									cl.bcs.socionegocio.per.CrearPersonaTarjetaBancaria.SalidaCrearPersonaTarjetaBancaria salidaTarjetasBancaria = crearTarjetaBancaria.logica(conn, inCrearTarjetaBancaria);
									logger.debug("FIN::CrearPersonaTarjetaBancaria");
									if(salidaTarjetasBancaria.P_OUT_ERRORESCONTADOR > 0)
									{
										entidadModificada.TipoEntidad = TipoEntidad;
										entidadModificada.IdEntidad = IdEntidad;

										for(MensajeSalida mensaje : salidaTarjetasBancaria.P_OUT_MENSAJES)
										{
											if(!MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)) continue;
											mensajesErrores.IdInterno = 0L;
											mensajesErrores.TipoMensaje = "ERROR";
											mensajesErrores.Codigo = mensaje.codigo;
											mensajesErrores.Mensaje = mensaje.descripcion;

											listMensajesErrores.add(mensajesErrores);
											entidadModificada.Mensajes = listMensajesErrores;
											listEntidadModificada.add(entidadModificada);
											ContadorErrores++;
											break;
										}

										for(MensajeSalida mensaje : salidaTarjetasBancaria.P_OUT_MENSAJES)
										{
											if(MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)) continue;
											mensajesErrores.IdInterno = 0L;
											mensajesErrores.TipoMensaje = mensaje.tipo;
											mensajesErrores.Codigo = mensaje.codigo;
											mensajesErrores.Mensaje = mensaje.descripcion;

											listMensajesErrores.add(mensajesErrores);
											entidadModificada.Mensajes = listMensajesErrores;
											listEntidadModificada.add(entidadModificada);
											ContadorErrores++;
											break;
										}

										break;
									}
								}
								else if(tarjetasBancaria.Accion.equals("MODIFICAR"))
								{
									List<RegistroCambio> listRegistroCambio = new ArrayList<RegistroCambio>();
									
									if(tarjetasBancaria.IdBanco > 0L)
									{
										RegistroCambio registroCambio = new RegistroCambio();
										registroCambio.CampoModificado	= "IdBanco";
										registroCambio.ValorNuevo		= String.valueOf(tarjetasBancaria.IdBanco);
										listRegistroCambio.add(registroCambio);
									}
									
							        if(!tarjetasBancaria.NumeroTarjeta.isEmpty())
									{
							        	RegistroCambio registroCambio = new RegistroCambio();
										registroCambio.CampoModificado	= "NumeroTarjetaBancaria";
										registroCambio.ValorNuevo		= tarjetasBancaria.NumeroTarjeta.trim();
										listRegistroCambio.add(registroCambio);
									}
							        
							        if(!tarjetasBancaria.IdTipo.isEmpty())
									{
							        	RegistroCambio registroCambio = new RegistroCambio();
										registroCambio.CampoModificado	= "IdTipoTarjetaBancaria";
										registroCambio.ValorNuevo		= tarjetasBancaria.IdTipo.trim();
										listRegistroCambio.add(registroCambio);
									}
							        
							        if(!tarjetasBancaria.CodigoVerificacion.isEmpty())
									{
							        	RegistroCambio registroCambio = new RegistroCambio();
										registroCambio.CampoModificado	= "CodigoVerificacion";
										registroCambio.ValorNuevo		= tarjetasBancaria.CodigoVerificacion.trim();
										listRegistroCambio.add(registroCambio);
									}
							        
							        if(!tarjetasBancaria.FechaVencimiento.isEmpty())
									{
							        	RegistroCambio registroCambio = new RegistroCambio();
										registroCambio.CampoModificado	= "FechaVencimiento";
										registroCambio.ValorNuevo		= tarjetasBancaria.FechaVencimiento.trim();
										listRegistroCambio.add(registroCambio);
									}
							        
									if(!listRegistroCambio.isEmpty())
									{
										cl.bcs.socionegocio.per.ModificarPersonaTarjetaBancaria modificarTarjetaBancaria = new cl.bcs.socionegocio.per.ModificarPersonaTarjetaBancaria();
										cl.bcs.socionegocio.per.ModificarPersonaTarjetaBancaria.EntradaServicioModificarPersonaTarjetaBancaria inModificarTarjetaBancaria = new cl.bcs.socionegocio.per.ModificarPersonaTarjetaBancaria.EntradaServicioModificarPersonaTarjetaBancaria();
										inModificarTarjetaBancaria.p_IdEmpresa = in.p_IdEmpresa;
										inModificarTarjetaBancaria.p_IdUsuario = in.p_IdUsuario;
										inModificarTarjetaBancaria.p_IdModulo = "SOC";
										inModificarTarjetaBancaria.p_UsaReglasNegocio = "N";
										// El SP original no recibia p_IdPersona desde esta llamada (quedaba en su DEFAULT 0);
										// se replica ese mismo comportamiento para no alterar el IdPersona=0 grabado hoy en LogGenerarContratos.
										inModificarTarjetaBancaria.p_IdPersona = 0L;
										inModificarTarjetaBancaria.p_IdTarjetaBancaria = tarjetasBancaria.IdTarjetaBancaria;
										for(RegistroCambio r : listRegistroCambio){
											cl.bcs.socionegocio.per.ModificarPersonaTarjetaBancaria.RegistroCambioCampo campo = new cl.bcs.socionegocio.per.ModificarPersonaTarjetaBancaria.RegistroCambioCampo();
											campo.CampoModificado = r.CampoModificado;
											campo.ValorNuevo = r.ValorNuevo;
											inModificarTarjetaBancaria.p_RegistroCambio.add(campo);
										}

										logger.debug("INICIO::ModificarPersonaTarjetaBancaria");
										cl.bcs.socionegocio.per.ModificarPersonaTarjetaBancaria.SalidaModificarPersonaTarjetaBancaria salidaModificarTarjetaBancaria = modificarTarjetaBancaria.logica(conn, inModificarTarjetaBancaria);
										logger.debug("FIN::ModificarPersonaTarjetaBancaria");
										if(salidaModificarTarjetaBancaria.P_OUT_ERRORESCONTADOR > 0)
										{
											entidadModificada.TipoEntidad = TipoEntidad;
											entidadModificada.IdEntidad = IdEntidad;

											for(ErrorDetalle errorDetalle : salidaModificarTarjetaBancaria.P_OUT_ERRORESDETALLE)
											{
												mensajesErrores.IdInterno = 0L;
												mensajesErrores.TipoMensaje = "ERROR";
												mensajesErrores.Codigo = errorDetalle.Codigo;
												mensajesErrores.Mensaje = errorDetalle.Descripcion;

												listMensajesErrores.add(mensajesErrores);
												entidadModificada.Mensajes = listMensajesErrores;
												listEntidadModificada.add(entidadModificada);
												ContadorErrores++;
												break;
											}

											for(AdvertenciaDetalle advertenciaDetalle : salidaModificarTarjetaBancaria.P_OUT_ADVERTENCIASDETALLE)
											{
												mensajesErrores.IdInterno = 0L;
												mensajesErrores.TipoMensaje = "ADVERTENCIA";
												mensajesErrores.Codigo = advertenciaDetalle.Codigo;
												mensajesErrores.Mensaje = advertenciaDetalle.Descripcion;

												listMensajesErrores.add(mensajesErrores);
												entidadModificada.Mensajes = listMensajesErrores;
												listEntidadModificada.add(entidadModificada);
												ContadorErrores++;
												break;
											}

											break;
										}
									}
								}
								else if(tarjetasBancaria.Accion.equals("ELIMINAR")) {

									HashMap<String,Object> parametroSelect = new HashMap<String,Object>();
									parametroSelect.put("IdPersona", IdPersona);
									Long contadorTarjetasBancarias = sql.query(conn, SQL_CONTADOR_TARJETAS_BANCARIAS, parametroSelect, ContadorSelect.class).stream().findFirst().orElse(new ContadorSelect()).Contador;
									if(contadorTarjetasBancarias == 1)
									{
										String codigoCampo = conceptosCodigoCampo.get("TARJETAS_BANCARIAS");
										if( alMenosUnRegistro(conn,IdPersona, codigoCampo, in.p_IdTipoCanal)) {
											String descripcion = Diccionario.getMensaje(conn, in.p_IdEmpresa,"ERROR_TARJETA_BANCARIA_REQUERIDA", new HashMap<>());
											mensajesErrores.IdInterno = 0L;
											mensajesErrores.TipoMensaje = "ERROR";
											mensajesErrores.Codigo = "ERROR_TARJETA_BANCARIA_REQUERIDA" ;
											mensajesErrores.Mensaje = descripcion;

											listMensajesErrores.add(mensajesErrores);
											entidadModificada.Mensajes = listMensajesErrores;
											listEntidadModificada.add(entidadModificada);
											ContadorErrores++;
											break;
										}

									}

									cl.bcs.socionegocio.per.EliminarPersonaTarjetaBancaria eliminarTarjetaBancaria = new cl.bcs.socionegocio.per.EliminarPersonaTarjetaBancaria();
									cl.bcs.socionegocio.per.EliminarPersonaTarjetaBancaria.EntradaServicioEliminarPersonaTarjetaBancaria inEliminarTarjetaBancaria = new cl.bcs.socionegocio.per.EliminarPersonaTarjetaBancaria.EntradaServicioEliminarPersonaTarjetaBancaria();
									inEliminarTarjetaBancaria.p_IdEmpresa = in.p_IdEmpresa;
									inEliminarTarjetaBancaria.p_IdUsuario = in.p_IdUsuario;
									inEliminarTarjetaBancaria.p_IdModulo = "SOC";
									inEliminarTarjetaBancaria.p_UsaReglasNegocio = "N";
									inEliminarTarjetaBancaria.p_IdTarjetaBancaria = tarjetasBancaria.IdTarjetaBancaria;
									inEliminarTarjetaBancaria.p_Comentario = "";

									logger.debug("INICIO::EliminarPersonaTarjetaBancaria");
									cl.bcs.socionegocio.per.EliminarPersonaTarjetaBancaria.SalidaEliminarPersonaTarjetaBancaria salidaEliminarTarjetaBancaria = eliminarTarjetaBancaria.logica(conn, inEliminarTarjetaBancaria);
									logger.debug("FIN::EliminarPersonaTarjetaBancaria");
									if(salidaEliminarTarjetaBancaria.P_OUT_ERRORESCONTADOR > 0)
									{
										entidadModificada.TipoEntidad = TipoEntidad;
										entidadModificada.IdEntidad = IdEntidad;

										for(ErrorDetalle errorDetalle : salidaEliminarTarjetaBancaria.P_OUT_ERRORESDETALLE)
										{
											mensajesErrores.IdInterno = 0L;
											mensajesErrores.TipoMensaje = "ERROR";
											mensajesErrores.Codigo = errorDetalle.Codigo;
											mensajesErrores.Mensaje = errorDetalle.Descripcion;

											listMensajesErrores.add(mensajesErrores);
											entidadModificada.Mensajes = listMensajesErrores;
											listEntidadModificada.add(entidadModificada);
											ContadorErrores++;
											break;
										}

										for(AdvertenciaDetalle advertenciaDetalle : salidaEliminarTarjetaBancaria.P_OUT_ADVERTENCIASDETALLE)
										{
											mensajesErrores.IdInterno = 0L;
											mensajesErrores.TipoMensaje = "ADVERTENCIA";
											mensajesErrores.Codigo = advertenciaDetalle.Codigo;
											mensajesErrores.Mensaje = advertenciaDetalle.Descripcion;

											listMensajesErrores.add(mensajesErrores);
											entidadModificada.Mensajes = listMensajesErrores;
											listEntidadModificada.add(entidadModificada);
											ContadorErrores++;
											break;
										}

										break;
									}
								}
							}
							
							if(ContadorErrores > 0) break;
						}
						
						
						// *************************************************************
						// AGREGAR, MODIFICAR Y ELIMINAR DECLARACION DE ORIGEN DE FONDOS
						// *************************************************************
						if(!socioNegocio.DeclaracionOrigenFondo.isEmpty())
						{
							for(DeclaracionOrigenFondo declaracionOrigenFondo : socioNegocio.DeclaracionOrigenFondo)
							{
								if(declaracionOrigenFondo.Accion.equals("AGREGAR"))
								{
									List<DetalleOrigenFondo> listaDetalleOrigenFondo = new ArrayList<DetalleOrigenFondo>();
									
									for(DetalleOrigenFondo origenFondo : declaracionOrigenFondo.DetalleOrigenFondo)
									{
										DetalleOrigenFondo detalleOrigenFondo = new DetalleOrigenFondo();
										detalleOrigenFondo.IdTipoOrigenFondo = origenFondo.IdTipoOrigenFondo;
										detalleOrigenFondo.Valor = origenFondo.Valor;
										detalleOrigenFondo.Otros = origenFondo.Otros;
										listaDetalleOrigenFondo.add(detalleOrigenFondo);
									}
									
									logger.debug("INICIO::CrearDeclaracionOrigenFondos");
									cl.bcs.socionegocio.cli.CrearDeclaracionOrigenFondos crearDOF = new cl.bcs.socionegocio.cli.CrearDeclaracionOrigenFondos();
									cl.bcs.socionegocio.cli.CrearDeclaracionOrigenFondos.EntradaCrearDeclaracionOrigenFondos entradaDOF = new cl.bcs.socionegocio.cli.CrearDeclaracionOrigenFondos.EntradaCrearDeclaracionOrigenFondos();
									entradaDOF.p_IdEmpresa = in.p_IdEmpresa;
									entradaDOF.p_IdUsuario = in.p_IdUsuario;
									entradaDOF.p_IdModulo = "SOC";
									entradaDOF.p_IdCliente = IdCliente;
									entradaDOF.p_IdTipoOperacionOrigenFondos = declaracionOrigenFondo.IdTipoOperacionOrigenFondos;
									entradaDOF.p_FechaDeclaracion = declaracionOrigenFondo.FechaDeclaracion;
									entradaDOF.p_Comentario = declaracionOrigenFondo.Comentario;
									entradaDOF.p_IdEstado = declaracionOrigenFondo.IdEstado;
									entradaDOF.p_OperacionInscripcion = declaracionOrigenFondo.OperacionInscripcion;
									for (DetalleOrigenFondo detalle : listaDetalleOrigenFondo) {
									    cl.bcs.socionegocio.cli.CrearDeclaracionOrigenFondos.Detalle d = new cl.bcs.socionegocio.cli.CrearDeclaracionOrigenFondos.Detalle();
									    d.IdTipoOrigenFondo = detalle.IdTipoOrigenFondo;
									    d.Valor = new java.math.BigDecimal(detalle.Valor);
									    d.Otros = detalle.Otros;
									    entradaDOF.p_Detalle.add(d);
									}
									cl.bcs.socionegocio.cli.CrearDeclaracionOrigenFondos.SalidaCrearDeclaracionOrigenFondos salidaDeclaracionOrigenFondo = crearDOF.logica(conn, entradaDOF);
									logger.debug("FIN::CrearDeclaracionOrigenFondos");
																		 
									if(salidaDeclaracionOrigenFondo.P_OUT_ERRORESCONTADOR > 0)

									{

									    entidadModificada.TipoEntidad = TipoEntidad;

									    entidadModificada.IdEntidad = IdEntidad;
									 
									    for(MensajeSalida mensaje : salidaDeclaracionOrigenFondo.P_OUT_MENSAJES)

									    {

									        mensajesErrores.IdInterno = 0L;

									        mensajesErrores.TipoMensaje = mensaje.tipo;

									        mensajesErrores.Codigo = mensaje.codigo;

									        mensajesErrores.Mensaje = mensaje.descripcion;

									        listMensajesErrores.add(mensajesErrores);

									        entidadModificada.Mensajes = listMensajesErrores;

									        listEntidadModificada.add(entidadModificada);

									        ContadorErrores++;

									        break;

									    }

									    break;

									}
									 
								}
								else if(declaracionOrigenFondo.Accion.equals("MODIFICAR") || declaracionOrigenFondo.Accion.equals("ACTUALIZAR"))
								{
									List<RegistroCambio> listRegistroCambio = new ArrayList<RegistroCambio>();
									
									if(!declaracionOrigenFondo.IdTipoOperacionOrigenFondos.isEmpty())
									{
										RegistroCambio registroCambio = new RegistroCambio();
										registroCambio.CampoModificado	= "IdTipoOperacionOrigenFondos";
										registroCambio.ValorNuevo		= declaracionOrigenFondo.IdTipoOperacionOrigenFondos.trim();
										listRegistroCambio.add(registroCambio);
									}
									
									if(!declaracionOrigenFondo.FechaDeclaracion.isEmpty())
									{
										RegistroCambio registroCambio = new RegistroCambio();
										registroCambio.CampoModificado	= "FechaDeclaracion";
										registroCambio.ValorNuevo		= declaracionOrigenFondo.FechaDeclaracion.trim();
										listRegistroCambio.add(registroCambio);
									}
									
									if(!declaracionOrigenFondo.Comentario.isEmpty())
									{
										RegistroCambio registroCambio = new RegistroCambio();
										registroCambio.CampoModificado	= "Comentario";
										registroCambio.ValorNuevo		= declaracionOrigenFondo.Comentario.trim();
										listRegistroCambio.add(registroCambio);
									}
									
									if(!declaracionOrigenFondo.OperacionInscripcion.isEmpty())
									{
										RegistroCambio registroCambio = new RegistroCambio();
										registroCambio.CampoModificado	= "OperacionInscripcion";
										registroCambio.ValorNuevo		= declaracionOrigenFondo.OperacionInscripcion.trim();
										listRegistroCambio.add(registroCambio);
									}
									
									if(!declaracionOrigenFondo.IdEstado.isEmpty())
									{
										RegistroCambio registroCambio = new RegistroCambio();
										registroCambio.CampoModificado	= "IdEstado";
										registroCambio.ValorNuevo		= declaracionOrigenFondo.IdEstado.trim();
										listRegistroCambio.add(registroCambio);
									}
									
									for(DetalleOrigenFondo origenFondo : declaracionOrigenFondo.DetalleOrigenFondo)
									{
										RegistroCambio registroCambio = new RegistroCambio();
										registroCambio.CampoModificado	= origenFondo.IdTipoOrigenFondo.trim();
										registroCambio.ValorNuevo		= String.valueOf(origenFondo.Valor);
										listRegistroCambio.add(registroCambio);
									}
									
									if(!listRegistroCambio.isEmpty())
									{
										cl.bcs.socionegocio.cli.ModificarDeclaracionOrigenFondos modificarDeclaracionOrigenFondo = new cl.bcs.socionegocio.cli.ModificarDeclaracionOrigenFondos();
										cl.bcs.socionegocio.cli.ModificarDeclaracionOrigenFondos.EntradaModificarDeclaracionOrigenFondos inModificarDeclaracionOrigenFondo = new cl.bcs.socionegocio.cli.ModificarDeclaracionOrigenFondos.EntradaModificarDeclaracionOrigenFondos();
										inModificarDeclaracionOrigenFondo.p_IdEmpresa = in.p_IdEmpresa;
										inModificarDeclaracionOrigenFondo.p_IdUsuario = in.p_IdUsuario;
										inModificarDeclaracionOrigenFondo.p_IdModulo = "SOC";
										inModificarDeclaracionOrigenFondo.p_UsaReglasNegocio = "N";
										inModificarDeclaracionOrigenFondo.p_IdDeclaracionOrigenFondos = declaracionOrigenFondo.IdDeclaracionOrigenFondos;
										inModificarDeclaracionOrigenFondo.p_RegistroCambio = new ArrayList<cl.bcs.socionegocio.cli.ModificarDeclaracionOrigenFondos.RegistroCambio>();
										for(RegistroCambio r : listRegistroCambio){
											cl.bcs.socionegocio.cli.ModificarDeclaracionOrigenFondos.RegistroCambio campo = new cl.bcs.socionegocio.cli.ModificarDeclaracionOrigenFondos.RegistroCambio();
											campo.CampoModificado = r.CampoModificado;
											campo.ValorNuevo = r.ValorNuevo;
											inModificarDeclaracionOrigenFondo.p_RegistroCambio.add(campo);
										}

										logger.debug("INICIO::ModificarDeclaracionOrigenFondos");
										cl.bcs.socionegocio.cli.ModificarDeclaracionOrigenFondos.SalidaModificarDeclaracionOrigenFondos salidaModificarDeclaracionOrigenFondo = modificarDeclaracionOrigenFondo.logica(conn, inModificarDeclaracionOrigenFondo);
										logger.debug("FIN::ModificarDeclaracionOrigenFondos");
										if(salidaModificarDeclaracionOrigenFondo.P_OUT_ERRORESCONTADOR > 0)
										{
											entidadModificada.TipoEntidad = TipoEntidad;
											entidadModificada.IdEntidad = IdEntidad;

											for(MensajeSalida mensaje : salidaModificarDeclaracionOrigenFondo.P_OUT_MENSAJES)
											{
												if(!MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)) continue;
												mensajesErrores.IdInterno = 0L;
												mensajesErrores.TipoMensaje = "ERROR";
												mensajesErrores.Codigo = mensaje.codigo;
												mensajesErrores.Mensaje = mensaje.descripcion;

												listMensajesErrores.add(mensajesErrores);
												entidadModificada.Mensajes = listMensajesErrores;
												listEntidadModificada.add(entidadModificada);
												ContadorErrores++;
												break;
											}

											for(MensajeSalida mensaje : salidaModificarDeclaracionOrigenFondo.P_OUT_MENSAJES)
											{
												if(MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)) continue;
												mensajesErrores.IdInterno = 0L;
												mensajesErrores.TipoMensaje = mensaje.tipo;
												mensajesErrores.Codigo = mensaje.codigo;
												mensajesErrores.Mensaje = mensaje.descripcion;

												listMensajesErrores.add(mensajesErrores);
												entidadModificada.Mensajes = listMensajesErrores;
												listEntidadModificada.add(entidadModificada);
												ContadorErrores++;
												break;
											}

											break;
										}
									}
								}
							}
							
							if(ContadorErrores > 0) break;
						}
						
						
						// ************************************************
						// AGREGAR, MODIFICAR Y ELIMINAR NOMBRE DE FANTASIA
						// ************************************************
						if(!socioNegocio.NombreFantasia.isEmpty())
						{
							for(NombreFantasia nombreFantasia : socioNegocio.NombreFantasia)
							{
								if(nombreFantasia.Accion.equals("AGREGAR"))
								{
									cl.bcs.socionegocio.per.CrearPersonaNombreFantasia crearNombreFantasia = new cl.bcs.socionegocio.per.CrearPersonaNombreFantasia();
									cl.bcs.socionegocio.per.CrearPersonaNombreFantasia.EntradaCrearPersonaNombreFantasia inCrearNombreFantasia = new cl.bcs.socionegocio.per.CrearPersonaNombreFantasia.EntradaCrearPersonaNombreFantasia();
									inCrearNombreFantasia.p_IdEmpresa = in.p_IdEmpresa;
									inCrearNombreFantasia.p_IdUsuario = in.p_IdUsuario;
									inCrearNombreFantasia.p_IdModulo = "SOC";
									inCrearNombreFantasia.p_UsaReglasNegocio = "N";
									inCrearNombreFantasia.p_IdPersona = IdPersona;
									inCrearNombreFantasia.p_NombreFantasia = nombreFantasia.Nombre;
									inCrearNombreFantasia.p_IdEstado = "ACT";

									logger.debug("INICIO::CrearPersonaNombreFantasia");
									cl.bcs.socionegocio.per.CrearPersonaNombreFantasia.SalidaCrearPersonaNombreFantasia salidaNombreFantasia = crearNombreFantasia.logica(conn, inCrearNombreFantasia);
									logger.debug("FIN::CrearPersonaNombreFantasia");
									if(salidaNombreFantasia.P_OUT_ERRORESCONTADOR > 0)
									{
										entidadModificada.TipoEntidad = TipoEntidad;
										entidadModificada.IdEntidad = IdEntidad;

										for(MensajeSalida mensaje : salidaNombreFantasia.P_OUT_MENSAJES)
										{
											if(!MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)) continue;
											mensajesErrores.IdInterno = 0L;
											mensajesErrores.TipoMensaje = "ERROR";
											mensajesErrores.Codigo = mensaje.codigo;
											mensajesErrores.Mensaje = mensaje.descripcion;

											listMensajesErrores.add(mensajesErrores);
											entidadModificada.Mensajes = listMensajesErrores;
											listEntidadModificada.add(entidadModificada);
											ContadorErrores++;
											break;
										}

										for(MensajeSalida mensaje : salidaNombreFantasia.P_OUT_MENSAJES)
										{
											if(MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)) continue;
											mensajesErrores.IdInterno = 0L;
											mensajesErrores.TipoMensaje = mensaje.tipo;
											mensajesErrores.Codigo = mensaje.codigo;
											mensajesErrores.Mensaje = mensaje.descripcion;

											listMensajesErrores.add(mensajesErrores);
											entidadModificada.Mensajes = listMensajesErrores;
											listEntidadModificada.add(entidadModificada);
											ContadorErrores++;
											break;
										}

										break;
									}
								}
								else if(nombreFantasia.Accion.equals("MODIFICAR"))
								{
									cl.bcs.socionegocio.per.ModificarPersonaNombreFantasia modificarNombreFantasia = new cl.bcs.socionegocio.per.ModificarPersonaNombreFantasia();
									cl.bcs.socionegocio.per.ModificarPersonaNombreFantasia.EntradaServicioModificarPersonaNombreFantasia inModificarNombreFantasia = new cl.bcs.socionegocio.per.ModificarPersonaNombreFantasia.EntradaServicioModificarPersonaNombreFantasia();
									inModificarNombreFantasia.p_IdEmpresa = in.p_IdEmpresa;
									inModificarNombreFantasia.p_IdUsuario = in.p_IdUsuario;
									inModificarNombreFantasia.p_IdModulo = "SOC";
									inModificarNombreFantasia.p_UsaReglasNegocio = "N";
									inModificarNombreFantasia.p_IdPersona = IdPersona;
									inModificarNombreFantasia.p_IdNombreFantasia = nombreFantasia.IdNombreFantasia;
									inModificarNombreFantasia.p_NombreFantasia = nombreFantasia.Nombre;
									inModificarNombreFantasia.p_IdEstado = nombreFantasia.IdEstado;

									logger.debug("INICIO::ModificarPersonaNombreFantasia");
									cl.bcs.socionegocio.per.ModificarPersonaNombreFantasia.SalidaModificarPersonaNombreFantasia salidaModificarNombreFantasia = modificarNombreFantasia.logica(conn, inModificarNombreFantasia);
									logger.debug("FIN::ModificarPersonaNombreFantasia");
									if(salidaModificarNombreFantasia.P_OUT_ERRORESCONTADOR > 0)
									{
										entidadModificada.TipoEntidad = TipoEntidad;
										entidadModificada.IdEntidad = IdEntidad;

										for(ErrorDetalle errorDetalle : salidaModificarNombreFantasia.P_OUT_ERRORESDETALLE)
										{
											mensajesErrores.IdInterno = 0L;
											mensajesErrores.TipoMensaje = "ERROR";
											mensajesErrores.Codigo = errorDetalle.Codigo;
											mensajesErrores.Mensaje = errorDetalle.Descripcion;

											listMensajesErrores.add(mensajesErrores);
											entidadModificada.Mensajes = listMensajesErrores;
											listEntidadModificada.add(entidadModificada);
											ContadorErrores++;
											break;
										}

										for(AdvertenciaDetalle advertenciaDetalle : salidaModificarNombreFantasia.P_OUT_ADVERTENCIASDETALLE)
										{
											mensajesErrores.IdInterno = 0L;
											mensajesErrores.TipoMensaje = "ADVERTENCIA";
											mensajesErrores.Codigo = advertenciaDetalle.Codigo;
											mensajesErrores.Mensaje = advertenciaDetalle.Descripcion;

											listMensajesErrores.add(mensajesErrores);
											entidadModificada.Mensajes = listMensajesErrores;
											listEntidadModificada.add(entidadModificada);
											ContadorErrores++;
											break;
										}

										break;
									}
								}
							}
							
							if(ContadorErrores > 0) break;
						}
						
						ErroresContador = 0L;
						ContadorEntidadModificada = 1L;
						
						mensajesErrores.IdInterno = 0L;
						mensajesErrores.TipoMensaje = "INFO";
						mensajesErrores.Codigo = "MODIFICACION_EXITOSA";
						mensajesErrores.Mensaje = "Modificacion Exitosa";
						
						listMensajesErrores.add(mensajesErrores);
						entidadModificada.Mensajes = listMensajesErrores;
						listEntidadModificada.add(entidadModificada);
						
						entidadModificada.TipoEntidad = TipoEntidad;
						entidadModificada.IdEntidad = IdEntidad;
						entidadModificada.Mensajes = listMensajesErrores;
						listEntidadModificada.add(entidadModificada);
					}
					else
					{
						EntidadModificada entidadModificada = new EntidadModificada();
						MensajesErrores mensajesErrores = new MensajesErrores();
						
						entidadModificada.TipoEntidad = "";
						entidadModificada.IdEntidad = 0L;
						
						mensajesErrores.IdInterno = 0L;
						mensajesErrores.TipoMensaje = "ERROR";
						mensajesErrores.Codigo = "PERSONA_NO_EXISTE";
						mensajesErrores.Mensaje = "PERSONA_NO_EXISTE";
						
						listMensajesErrores.add(mensajesErrores);
						entidadModificada.Mensajes = listMensajesErrores;
						listEntidadModificada.add(entidadModificada);
					}
				}
				else
				{
					EntidadModificada entidadModificada = new EntidadModificada();
					MensajesErrores mensajesErrores = new MensajesErrores();
					
					entidadModificada.TipoEntidad = "";
					entidadModificada.IdEntidad = 0L;
					
					mensajesErrores.IdInterno = 0L;
					mensajesErrores.TipoMensaje = "ERROR";
					mensajesErrores.Codigo = "DEBE_INGRESAR_UN_DOCUMENTO_IDENTIDAD";
					mensajesErrores.Mensaje = "DEBE_INGRESAR_UN_DOCUMENTO_IDENTIDAD";
					
					listMensajesErrores.add(mensajesErrores);
					entidadModificada.Mensajes = listMensajesErrores;
					listEntidadModificada.add(entidadModificada);
				}
			}
			
			salida.P_OUT_ERRORESCONTADOR = ErroresContador;
			salida.p_Out_ContadorEntidadModificada = ContadorEntidadModificada;
			salida.p_Out_EntidadModificada = listEntidadModificada;
		}
		else
		{
			EntidadModificada entidadModificada = new EntidadModificada();
			MensajesErrores mensajesErrores = new MensajesErrores();
			
			entidadModificada.TipoEntidad = "";
			entidadModificada.IdEntidad = 0L;
			
			mensajesErrores.IdInterno = 0L;
			mensajesErrores.TipoMensaje = "ERROR";
			mensajesErrores.Codigo = "NO_SE_PUEDE_ENROLAR";
			mensajesErrores.Mensaje = "NO_SE_PUEDE_ENROLAR";
			
			listMensajesErrores.add(mensajesErrores);
			entidadModificada.Mensajes = listMensajesErrores;
			listEntidadModificada.add(entidadModificada);
			
			salida.P_OUT_ERRORESCONTADOR = 1L;
			salida.p_Out_ContadorEntidadModificada = 0L;
			salida.p_Out_EntidadModificada = listEntidadModificada;
		}
		
		if(tablaTemporal(conn, false, new ArrayList<RegistroCambio>()))
		{
			return salida = mesajesDeError("", 0L, 0L, "ERROR", "ERROR_TABLA_TEMPORAL", "ERROR_TABLA_TEMPORAL");
		}
		
		if(tablaTemporalIndicador(conn, false, new ArrayList<IndicadorPersonaCliente>()))
		{
			return salida = mesajesDeError("", 0L, 0L, "ERROR", "ERROR_TABLA_TEMPORAL_INDICADOR", "ERROR_TABLA_TEMPORAL_INDICADOR");
		}
		
		return salida;
	}
	
	private boolean alMenosUnRegistro(Connection conn, Long idPersona, String codigoCampo , String idTipoCanal) throws Exception {
		logger.debug("INICIO::ModificarSocioNegocio.alMenosUnRegistro");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		
		String codigoFormulario = obtenerCodigoFormulario(conn,idPersona);
		
		HashMap<String, Object> prms = new HashMap<String, Object>();
		prms.put("CodigoFormulario", codigoFormulario);
		prms.put("CodigoCampo", codigoCampo);
		prms.put("IdTipoCanal", idTipoCanal);
		
		String alMenosUnRegistro = sql.query(conn, SQL_SELECT_TIENE_MINIMO, prms, TieneCantidadMinima.class).stream().findFirst().orElse(new TieneCantidadMinima()).Requerido;

		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarSocioNegocio.alMenosUnRegistro", finServicio - inicio);
		
		return alMenosUnRegistro.equals("S") ? true : false;
	}

	private String obtenerCodigoFormulario(Connection conn, Long idPersona) throws Exception {
		logger.debug("INICIO::ModificarSocioNegocio.obtenerCodigoFormulario");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("IdPersona", idPersona);
		
		Tipos tipos =sql.query(conn, SQL_SELECT_TIPO_ENROLAMIENTO_PERSONA, params, Tipos.class).stream().findFirst().orElse(new Tipos());
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarSocioNegocio.obtenerCodigoFormulario", finServicio - inicio);
		return "GRILLA_" + tipos.TipoEnrolamiento + "_" +tipos.Tipo;
	}

	public boolean tablaTemporal(Connection conn, boolean opcion, List<RegistroCambio> listaRegistroCambio)
	{
		SQL sql = new SQL();
		
		if(opcion)
		{
			if(!listaRegistroCambio.isEmpty())
			{
				try
				{
					try
					{
						SQL.exec(conn, "CREATE LOCAL TEMPORARY TABLE #ModificarSocioNegocio_RegistroCambio (\"CampoModificado\" NVARCHAR(255),\"ValorNuevo\" NVARCHAR(255))");
						
						sql.batch(conn, "DELETE FROM #ModificarSocioNegocio_RegistroCambio", listaRegistroCambio, RegistroCambio.class);
						
						sql.batch(conn, "INSERT INTO #ModificarSocioNegocio_RegistroCambio (\"CampoModificado\",\"ValorNuevo\") VALUES (:CampoModificado,:ValorNuevo)", listaRegistroCambio, RegistroCambio.class);
					}
					catch (Exception error2)
					{
						sql.batch(conn, "DELETE FROM #ModificarSocioNegocio_RegistroCambio", listaRegistroCambio, RegistroCambio.class);
						
						sql.batch(conn, "INSERT INTO #ModificarSocioNegocio_RegistroCambio (\"CampoModificado\",\"ValorNuevo\") VALUES (:CampoModificado,:ValorNuevo)", listaRegistroCambio, RegistroCambio.class);
					}
				}
				catch (Exception error1)
				{
					try
					{
						SQL.exec(conn, "DROP TABLE #ModificarSocioNegocio_RegistroCambio");
					}
					catch (Exception error3)
					{
						return true;
					}
				}
			}
		}
		else
		{
			try
			{
				SQL.exec(conn, "DROP TABLE #ModificarSocioNegocio_RegistroCambio");
			}
			catch (Exception error4)
			{
				//return true;
			}
		}

		return false;
	}

	public boolean tablaTemporalIndicador(Connection conn, boolean opcion, List<IndicadorPersonaCliente> listaIndicadorPersonaCliente)
	{
		SQL sql = new SQL();
		
		if(opcion)
		{
			if(!listaIndicadorPersonaCliente.isEmpty())
			{
				try
				{
					try
					{
						SQL.exec(conn, "CREATE LOCAL TEMPORARY TABLE #CrearSocioNegocio_IndicadorPersonaCliente (\"IdIndicador\" BIGINT,\"IdTipoIndicador\" BIGINT, \"ValorIndicador\" NVARCHAR(255), \"FechaVencimiento\" SECONDDATE)");
						
						sql.batch(conn, "DELETE FROM #CrearSocioNegocio_IndicadorPersonaCliente", listaIndicadorPersonaCliente, IndicadorPersonaCliente.class);
						
						sql.batch(conn, "INSERT INTO #CrearSocioNegocio_IndicadorPersonaCliente (\"IdIndicador\",\"IdTipoIndicador\",\"ValorIndicador\",\"FechaVencimiento\") VALUES (:IdIndicador,:IdTipoIndicador,:ValorIndicador,:FechaVencimiento)", listaIndicadorPersonaCliente, IndicadorPersonaCliente.class);
					}
					catch (Exception error2)
					{
						sql.batch(conn, "DELETE FROM #CrearSocioNegocio_IndicadorPersonaCliente", listaIndicadorPersonaCliente, IndicadorPersonaCliente.class);
						
						sql.batch(conn, "INSERT INTO #CrearSocioNegocio_IndicadorPersonaCliente (\"IdIndicador\",\"IdTipoIndicador\",\"ValorIndicador\",\"FechaVencimiento\") VALUES (:IdIndicador,:IdTipoIndicador,:ValorIndicador,:FechaVencimiento)", listaIndicadorPersonaCliente, IndicadorPersonaCliente.class);
					}
				}
				catch (Exception error1)
				{
					try
					{
						SQL.exec(conn, "DROP TABLE #CrearSocioNegocio_IndicadorPersonaCliente");
					}
					catch (Exception error3)
					{
						return true;
					}
				}
			}
		}
		else
		{
			try
			{
				SQL.exec(conn, "DROP TABLE #CrearSocioNegocio_IndicadorPersonaCliente");
			}
			catch (Exception error4)
			{
				//return true;
			}
		}

		return false;
	}
	
	public ModificarSocioNegocio_Salida mesajesDeError(String tipoEntidad, Long idEntidad, Long idInterno, String tipoMensaje, String codigo, String mensaje)
	{
		ModificarSocioNegocio_Salida salida = new ModificarSocioNegocio_Salida();
		
		List<EntidadModificada> listEntidadModificada = new ArrayList<EntidadModificada>();
		List<MensajesErrores> listMensajesErrores = new ArrayList<MensajesErrores>();
		
		EntidadModificada entidadModificada = new EntidadModificada();
		MensajesErrores mensajesErrores = new MensajesErrores();
		
		entidadModificada.TipoEntidad = tipoEntidad;
		entidadModificada.IdEntidad = idEntidad;
		
		mensajesErrores.IdInterno = idInterno;
		mensajesErrores.TipoMensaje = tipoMensaje;
		mensajesErrores.Codigo = codigo;
		mensajesErrores.Mensaje = mensaje;
		
		listMensajesErrores.add(mensajesErrores);
		entidadModificada.Mensajes = listMensajesErrores;
		listEntidadModificada.add(entidadModificada);
		
		salida.P_OUT_ERRORESCONTADOR = 1L;
		salida.p_Out_ContadorEntidadModificada = 0L;
		salida.p_Out_EntidadModificada = listEntidadModificada;
		
		return salida;
	}
	
	
	//-------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE - CALL
	//-------------------------------------------------------------------------------------------
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception
	{
		Gson gson = Utils.getGsonBuilder();
		ModificarSocioNegocio_Entrada in = gson.fromJson(jin.toString(), ModificarSocioNegocio_Entrada.class);
		ModificarSocioNegocio_Salida out = logica(conn, in);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	//-------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	//-------------------------------------------------
	public static void main(String[] args) throws ConfigurationException
	
	  {
	 
	    System.out.println("== INICIO PRUEBA LOCAL ==");    
	    try (Connection con = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties"))
	 
	    {
	 
	      Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();  
	      
	      String jsonCL = "{\"p_IdEmpresa\":\"1\",\"p_IdUsuario\":\"4\",\"p_ClaveUsuario\":\"\",\"p_SocioNegocio\":[{\"IdEntidad\":38269,\"TipoEntidad\":\"CLI\",\"TipoDocumentoIdentidad\":\"R\",\"DocumentoIdentidad\":\"21638498-9\",\"DatosCliente\":[],\"TiposDocumento\":[],\"DireccionFisica\":[],\"Telefono\":[],\"DireccionElectronica\":[],\"IndicadorCliente\":[],\"IndicadorPersona\":[],\"CuentaBancaria\":[{\"Accion\":\"AGREGAR\",\"IdBanco\":1,\"CodigoTipoCuentaBancaria\":\"CUCO\",\"IdMoneda\":\"CLP\",\"NumeroCuentaBancaria\":\"9900011\",\"IdEstado\":\"\",\"CodigoPlaza\":0}],\"TarjetasBancaria\":[],\"DeclaracionOrigenFondo\":[],\"NombreFantasia\":[]}]}";
	      
	      
	      ModificarSocioNegocio_Entrada Objin = gson.fromJson(jsonCL, ModificarSocioNegocio_Entrada.class);       
	      ModificarSocioNegocio demo = new ModificarSocioNegocio();       
	      System.out.println("ENTRADA");
	 
	      System.out.println(Objin.toString());         try {
	 
	        ModificarSocioNegocio_Salida ObjOut = demo.logica(con, Objin); 
	        if (ObjOut.P_OUT_ERRORESCONTADOR > 0) {
	 
	          con.rollback();
	 
	        }           System.out.println("SALIDA");
	 
	        System.out.println(ObjOut.toString());
	 
	      } catch (Exception er) {
	 
	        er.printStackTrace();
	 
	      } finally {
	 
	        con.rollback();
	 
	      }       } catch (SQLException e) {
	 
	      e.printStackTrace();
	 
	    }
	 
	    System.out.println("== FIN PRUEBA LOCAL ==");
	 
	  }
	 
}