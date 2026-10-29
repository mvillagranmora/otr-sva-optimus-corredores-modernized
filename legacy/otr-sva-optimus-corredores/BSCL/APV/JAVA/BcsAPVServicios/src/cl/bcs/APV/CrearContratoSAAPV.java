package cl.bcs.APV;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

import cl.bcs.APV.CambiarEstadoContratosSAAPV.ContratosSAAPV_Entrada;
import cl.bcs.fondomutuo.CrearAporteSAAPV.SelectDatosAPV;
import cl.bcs.fondomutuo.CrearAporteSAAPV.SelectIdPortafolio;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;

public class CrearContratoSAAPV implements JavaCall 
{
	static final Logger logger = LogManager.getLogger(CrearContratoSAAPV.class);
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	//*****************************************************//
	// NOTAS :
	// 1.- p_IdUsuario ES STRING O LONG
	
	//-------------------------------------------------
	// QUERIES
	//-------------------------------------------------
	private final String SQL_SELECT_CONTRATO_SAAPV					= loadResourceToString("cl/bcs/APV/sql/CrearContratoSAAPV_SelectContratoSAAPV.sql");
	private final String SQL_SELECT_FOLIO_CONTRATO_SAAPV			= loadResourceToString("cl/bcs/APV/sql/CrearContratoSAAPV_SelectFolioContratoSAAPV.sql");
	private final String SQL_SELECT_INSTRUMENTO						= loadResourceToString("cl/bcs/APV/sql/ContratoSAAPV_ObtenerInstrumentoByNemotecnico.sql");
	private final String SQL_VALIDA_CONTRATO_VIG					= "SELECT \"IdEstado\" FROM  \"BSCL.APV.BD::SCS.ContratoSAAPV\" WHERE \"IdContratoSAAPV\" = :IdContratoSAAPV";
	private final String SQL_UPDATE_CONTRATO 						= "UPDATE \"BSCL.APV.BD::SCS.ContratoSAAPV\"  SET \"IdEstado\" = 'CER' WHERE \"IdContratoSAAPV\" = :IdContratoSAAPV";
	private final String SQL_SELECT_PRIMER_ID_PORTAFOLIO			= loadResourceToString("cl/bcs/fondomutuo/sql/CrearAporteSAAPV_SelectPrimerIdPortafolio.sql");
	private final String SQL_SELECT_ID_PORTAFOLIO					= loadResourceToString("cl/bcs/fondomutuo/sql/CrearAporteSAAPV_SelectIdPortafolio.sql");
	private final String SQL_SELECT_DATOS_APV						= loadResourceToString("cl/bcs/fondomutuo/sql/CrearAporteSAAPV_SelectDatosAPV.sql");
	private final String SQL_SELECT_RESIDENCIA						= loadResourceToString("cl/bcs/APV/sql/ConsultarDatosClienteResidencia.sql");
	private final String SQL_SELECT_REGIMEN							= "SELECT \"Codigo\" FROM \"BSCL.APV.BD::REC.RegimenTributario\" WHERE \"IdRegimenTributario\" = :IdRegimen";
	private final String SQL_SELECT_FICHACLIENTE					= loadResourceToString("cl/bcs/APV/sql/ContratoSAAPV_SelectFichaClienteAPV.sql");
	private final String SQL_SELECT_ENTIDAD_RECAUDADORA				= loadResourceToString("cl/bcs/APV/sql/CrearContratoSAAPV_SelectEntidadRecaudadora.sql");
	private final String SQL_VALIDAR_PERFIL_COMERCIAL_APV 			= loadResourceToString("cl/bcs/APV/sql/CrearContratoSAAPV_ValidarPerfilComercialAPV.sql");

	public final List<String> fondos = Arrays.asList(new String[]{"A", "B", "C", "D", "E"});
	//-------------------------------------------------
	// CLASES DE ENTRADA 
	//-------------------------------------------------
	public static class CrearContratoSAAPV_Entrada
	{
		public Long 				p_IdEmpresa;
		public Long					p_IdUsuario; // preguntar si es String o Long
		public String				p_ClaveUsuario;
		public List<Contrato>		p_Contrato;
		
		public CrearContratoSAAPV_Entrada()
		{
			p_Contrato = new ArrayList<Contrato>();
		}
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	
	
	public static class EntidadRecaudadora
	{
		public String DocumentoIdentidad;
		public Long IdEntidadRecaudadora;
		public String Nombre;
		public Long IdPersona;
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	public static class Contrato
	{
        public String						Estado							= "";
        public String						FolioContratoSAAPV				= "";
        public Long							IdContratoSAAPV					= 0L;
        public Long							IdCliente						= 0L;
        public Long							IdPersona						= 0L;
        public String						Rut								= "";
        public String						Nombres							= "";
		public String						ApellidoPaterno					= "";
		public String						ApellidoMaterno					= "";
        public String						Direccion						= "";
        public String						NumeroDireccion					= "";
        public String						Telefono						= "";
        public String						TelefonoFijo					= "";
        public String						TelefonoMovil					= "";
        public String						TelefonoLaboral					= "";
        public String						Email							= "";
        public Long							IdPais							= 0L;
        public String						Pais							= "";
        public Long							IdRegion						= 0L;
        public String						Region							= "";
        public Long							IdComuna						= 0L;
        public String						Comuna							= "";
        public Long							IdCiudad						= 0L;
        public String						Ciudad							= "";
        public String						FechaNacimiento					= "0001-01-01";
        public String						IdEstadoCivil					= "";
        public String						IdGenero						= "";
        public Long							IdCargoPersona					= 0L;
        public Long							IdClienteEmpleador				= 0L;
        public Long							IdPersonaEmpleador				= 0L;
        public String						RutEmpleador					= "";
        public String						RazonSocialEmpleador			= "";
        public String						NombreFantasiaEmpleador			= "";
        public String						DireccionEmpleador				= "";
        public String						NumeroDireccionEmpleador		= "";
        public String						TelefonoEmpleador				= "";
        public Long							IdPaisEmpleador					= 0L;
        public String						PaisEmpleador					= "";
        public Long							IdRegionEmpleador				= 0L;
        public String						RegionEmpleador					= "";
        public Long							IdComunaEmpleador				= 0L;
        public String						ComunaEmpleador					= "";
        public Long							IdCiudadEmpleador				= 0L;
        public String						CiudadEmpleador					= "";
        public String						NombreEncargado					= "";
        public String						EmailEncargado					= "";
        public String						TelefonoEncargado				= "";
        public List<Regimen>				Regimen;
        public List<Distribucion>			Distribucion;
        public Long							IdPlan							= 0L;
        public String						NombrePlan						= "";
        public String						FechaSuscripcion				= "0001-01-01";
        public String						FechaCreacion					= "0001-01-01";
        public String						FechaAprobacion					= "0001-01-01";
        public String						FechaNotificacion				= "0001-01-01";
        public String						FechaRecepcion					= "0001-01-01";
        public String						Folio							= "";
        public String						IdCategoriaInversionista		= "";
        public String						IdPerfilComercial				= "";
        public Long							IdMedioRecaudacionTrabajador	= 0L;
        public Long							IdEntidadRecaudadora			= 0L;
        public Long							CodigoPrevision					= 0L;
        public String						Pensionado						= "NO";
        public String						Trabajador						= "";
        public String						FormaDePago						= "";
        public Long							IdMedioRecaudacionFormaPago		= 0L;
        public Long							DiaDescuento					= 0L;
        public String						Institucion						= "";
        public double						ComisionTransferencia			= 0L;
        public String						CodigoTipoSolicitud				= "";
        public String						TipoMovimiento					= "";
        public String						RazonRetiro						= "";
        public Long							IdPersonaOrigen					= 0L;
        public String						IdEstadoEnvioNotificacion		= "";
        public String						IdEstadoSolicitud				= "";
        public Long							IdCargaMasiva					= 0L;
        public Long							IdEmpleador						= 0L;
        public String						IdPropositoPortafolio			= "";
        public Long							IdMatrizAgrupador				= 0L;
        public String						CodigoAgrupador					= "";
        public String						Descripcion						= "";
        public String 						Canal                           = "OPT";
        public String						UsarDatosFichaCliente			="";
        public String						TipoTraspaso					="";
        public String						EsAporteCuentaInversion;
        public List<DetalleOrigenTraspaso>	DetalleOrigenTraspaso;
        
		public Contrato()
		{
			Canal = "OPT";
			Regimen = new ArrayList<Regimen>();
			Distribucion = new ArrayList<Distribucion>();
			DetalleOrigenTraspaso = new ArrayList<DetalleOrigenTraspaso>();
			EsAporteCuentaInversion = "N";
		}
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	public static class Regimen
	{
        public Long			IdContratoSAAPV				= 0L;
        public Long			IdPortafolio				= 0L;
        public String		IdPropositoPortafolio		= "";
        public Long			IdMatrizAgrupador			= 0L;
        public String		CodigoAgrupador				= "";
        public String		OrigenAhorro				= "";
        public String		MesInicio					= "";
        public String		MesTermino					= "";
        public String		Pago						= "";
        public String		TipoRecaudacion				= "IND";
        public BigDecimal	PorcentajeRegimenImponible	= BigDecimal.ZERO;
        public String		Producto					= "";
        public Long			IdRegimen					= 0L;
        public String		Regimen						= "";
        public String		Cuenta						= "";
        public BigDecimal	MontoTotal					= BigDecimal.ZERO;
        public String		Moneda						= "";
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	public static class Distribucion
	{
        public Long			IdContratoSAAPV				= 0L;
        public String		FolioContratoSAAPV			= "";        
        public Long			CodigoFondo					= 0L;
        public String		IdSerieFondo				= "";
        public BigDecimal	MontoParcial				= BigDecimal.ZERO;
        public BigDecimal 	PorcentajeParcial			= BigDecimal.ZERO;
        public String		NombreFondo					= "";
        public String		IdEstado					= "";
        
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	public static class salidaParametrosPersona
	{
      
        public Long		IdPersona				= 0L;
        
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	public static class salidaParametrosFichaCliente
	{
		public Long IdPersona;
		public String DocumentoIdentidad;
		public String Nombres;
		public String ApellidoPaterno;
		public String ApellidoMaterno;
		public String FechaNacimiento;
		public String Direccion;
		public String NumeroDireccion;
		public String TelefonoFijo;
		public String TelefonoMovil;
		public String TelefonoLaboral;
		public Long IdPais;
		public String Pais;
		public Long IdRegion;
		public String Region;
		public Long IdComuna;
		public String Comuna;
		public Long IdCiudad;
		public String Ciudad;
		public String EstadoCivil;
		public String Genero;
		public String DireccionElectronica;
		public String IdCategoriaInversionista;
		public Long IdEjecutivo;
		public String IdPerfilComercial;
		public Long IdPersonaEmpleador;
		public String RazonSocialEmpleador;
		public String RutEmpleador;
		public Long IdPaisEmpleador;
		public String DireccionEmpleador;
		public String NumeroDireccionEmpleador;
		public Long IdCiudadEmpleador;
		public Long IdComunaEmpleador;
		public String TelefonoEmpleador;

		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	
	
	
	public static class DetalleOrigenTraspaso
	{
        public String		CodigoTipoAhorro			= "";
        public String		DocumentoEntidad			= "";
        public String		Fondo						= "";
        public BigDecimal	MontoTraspaso				= BigDecimal.ZERO;
        public BigDecimal	UFTraspaso					= BigDecimal.ZERO;
        public BigDecimal	PorcentajeTraspaso			= BigDecimal.ZERO;
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	public static class Instrumento{
		public Long		IdInstrumento;
		public String	Nemotecnico;
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	public static class ContratoSAAPVVigente{
		
		public String IdEstado;
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	
public static class salidaResidenciaCliente{
		
		public Long IdDivisionPais;
		public Long IdPais;
		public Long IdCiudad;
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
public static class SalidaRegimen{
	
	public String Codigo;
	@Override
	public String toString()
	{
		return Utils.printJson(this, false);
	}
}





	//-------------------------------------------------
	// CLASES DE SALIDA 
	//-------------------------------------------------
	public static class CrearContratoSAAPV_Salida
	{
		public Long 			P_REF_ID_PORTAFOLIO;
		public Long				P_OUT_ERRORESCONTADOR;
		public EntidadContrato	p_Out_EntidadContrato;
		
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	public static class EntidadContrato
	{
		public String					FolioContrato;
		public Long						IdContratoSAAPV;
		public List<MensajesErrores>	Mensajes;
        
		public EntidadContrato()
		{
			Mensajes = new ArrayList<MensajesErrores>();
		}
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	public static class MensajesErrores
	{
		public Long		IdInterno;
		public String	TipoMensaje;
		public String	Codigo;
		public String	Mensaje;
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	
	//-------------------------------------------------
	// CLASES UTILITARIAS 
	//-------------------------------------------------
	public static class SelectContratoSAAPV
	{
        public Long		IdPersona;
        public Long		IdCliente;
        public String	IdEstado;
        public String	IdEstadoSolicitud;
        public String	FolioContratoSAAPV;
        public Long		IdPlan;
        public Long		IdMedioRecaudacionFormaPago;
        public Long		IdMedioRecaudacionTrabajador;
        public String	Pago;
        public String	CodigoPortafolio;
        public Long		IdMatrizAgrupadorAPV;
        public Long		IdProductoAgrupador;
        public Long		IdRegimenTributario;
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	public static class SelectFolioContratoSAAPV
	{
        public Long		IdPersona;
        public Long		IdCliente;        
        public Long		IdContratoSAAPV;
        public String	FolioContratoSAAPV;
        public Long		DiaDescuento;
        public Long		IdMedioRecaudacionFormaPago;
        public Long   	IdPortafolio;
		public Long	  	IdPortafolioContrato;
        
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	private HashMap<String, Object> obtenerInstrumentos(List<Distribucion> distribuciones, Connection conn) throws Exception {
		SQL sql = new SQL();		
		List<String> nemostecnicos = new ArrayList<String>();
		List<Instrumento> instrumentos = new ArrayList<Instrumento>();		
		HashMap<String, Object> params = new HashMap<String, Object>();			
		for(Distribucion distribucion: distribuciones) {			
			if(!nemostecnicos.contains(distribucion.IdSerieFondo))			
				nemostecnicos.add(distribucion.IdSerieFondo);					
		}		
		params.put("Nemotecnico", nemostecnicos);
		instrumentos = sql.query(conn, SQL_SELECT_INSTRUMENTO, params, Instrumento.class);		
		return Utils.List2Hash(instrumentos, "Nemotecnico");	
	}
	
	public static class ParamsContrato_Update
	{
        public Long	IdContratoSAAPV;
      
	}
	
	public static class PerfilComercialAPV {

	    public String IdPerfilComercialBase;
	    public String IdPerfilCobros;

	    @Override
	    public String toString() {
	        return Utils.printJson(this, false);
	    }
	}
	
	//-------------------------------------------------
	// LOGICA DEL SERVICIO 
	//-------------------------------------------------
	public CrearContratoSAAPV_Salida logica(Connection conn, CrearContratoSAAPV_Entrada in) throws Exception
	{
		logger.info("Inicia CrearContratoSAAPV");
		SQL sql = new SQL();
		List<ContratoSAAPVVigente> validaContrato = new ArrayList<ContratoSAAPVVigente>();
		
		CrearContratoSAAPV_Salida salida = new CrearContratoSAAPV_Salida();
		
		Long ContadorErrores = 0L;
		String TipoOperacion = "I";
		
		if(in.p_Contrato.size() > 0)
		{
			EntidadContrato entidadContrato = new EntidadContrato();
			List<MensajesErrores> listMensajesErrores = new ArrayList<MensajesErrores>();
			
			for (Contrato contrato : in.p_Contrato)
			{	
				if(!contrato.FolioContratoSAAPV.isEmpty())
				{
			        String NuevoFolioContratoSAAPV = "";
			        String FolioPrevio = "";
			        
			        try
			        {
			            Long.parseLong(contrato.FolioContratoSAAPV.trim());
			            NuevoFolioContratoSAAPV = contrato.FolioContratoSAAPV.trim();
			        }
			        catch (NumberFormatException excepcion1)
			        {
			        		logger.error(excepcion1);
			        		if(esGeneracionFolioAutomatico(contrato.FolioContratoSAAPV)) {
			        			ObtenerFolioSuscripcion obtenerFolioSuscripcion = new ObtenerFolioSuscripcion();
								ObtenerFolioSuscripcion.ObtenerFolioSuscripcion_Entrada entradaObtenerFolioSuscripcion = new ObtenerFolioSuscripcion.ObtenerFolioSuscripcion_Entrada();
								entradaObtenerFolioSuscripcion.p_IdEmpresa = in.p_IdEmpresa;
								entradaObtenerFolioSuscripcion.p_IdUsuario = in.p_IdUsuario;
								entradaObtenerFolioSuscripcion.p_IdModulo = "APV";
								entradaObtenerFolioSuscripcion.p_Canal = contrato.FolioContratoSAAPV;
								ObtenerFolioSuscripcion.ObtenerFolioSuscripcion_Salida salidaObtenerFolioSuscripcion = obtenerFolioSuscripcion.logica(conn, entradaObtenerFolioSuscripcion);
								if(salidaObtenerFolioSuscripcion.p_Out_ContadorError > 0)
								{
									MensajesErrores mensajesErrores = new MensajesErrores();
									
									mensajesErrores.IdInterno = salidaObtenerFolioSuscripcion.p_Out_Mensajes.IdInterno;
									mensajesErrores.TipoMensaje = salidaObtenerFolioSuscripcion.p_Out_Mensajes.TipoMensaje;
									mensajesErrores.Codigo = salidaObtenerFolioSuscripcion.p_Out_Mensajes.Codigo;
									mensajesErrores.Mensaje = salidaObtenerFolioSuscripcion.p_Out_Mensajes.Mensaje;
									
									entidadContrato.FolioContrato = "";
									entidadContrato.IdContratoSAAPV = 0L;
									
									listMensajesErrores.add(mensajesErrores);
									entidadContrato.Mensajes = listMensajesErrores;
									ContadorErrores++;
									break;
								}
								else{
									NuevoFolioContratoSAAPV = salidaObtenerFolioSuscripcion.p_Out_FolioSuscripcion;
								}
			        		}else {
			        			NuevoFolioContratoSAAPV = in.p_Contrato.get(0).FolioContratoSAAPV;
			        		}
				        
			        }
					
					if(contrato.CodigoTipoSolicitud.equals("MODIFICACION") && contrato.IdContratoSAAPV > 0)
					{
						if (!contrato.Folio.isEmpty()) {
							FolioPrevio = contrato.Folio.trim();
							
							HashMap<String,Object> parametroEntrada = new HashMap<String,Object>();
							parametroEntrada.put("p_IdCliente", contrato.IdCliente);
							parametroEntrada.put("p_IdContratoSAAPV", contrato.IdContratoSAAPV);
							
							logger.debug("Select Folio Contrato SAAPV");
							List<SelectFolioContratoSAAPV> SelectFolioContratoSAAPV = sql.query(conn, SQL_SELECT_FOLIO_CONTRATO_SAAPV, parametroEntrada, SelectFolioContratoSAAPV.class);
							parametroEntrada.clear();
							
							if(SelectFolioContratoSAAPV.isEmpty()) {
								List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
								MensajesErrores mensajesErrores = new MensajesErrores();
								mensajesErrores.IdInterno = 0L;
								mensajesErrores.TipoMensaje = "ERROR";
								mensajesErrores.Codigo = "ERROR_CONTRATO_NO_EXISTE";
								mensajesErrores.Mensaje = "El valor de IdContratoSAAPV proporcionado no es valido";

								entidadContrato.FolioContrato = "";
								entidadContrato.IdContratoSAAPV = 0L;
								listMensajesErrores.add(mensajesErrores);
								entidadContrato.Mensajes = listMensajesErrores;

								listaMensajesErrores.add(mensajesErrores);
								ContadorErrores++;
								entidadContrato.Mensajes = listMensajesErrores;
								
								salida.P_OUT_ERRORESCONTADOR = ContadorErrores;
								salida.p_Out_EntidadContrato = entidadContrato;
								return salida;
							}
							
							if(!SelectFolioContratoSAAPV.get(0).FolioContratoSAAPV.equalsIgnoreCase(contrato.Folio)) {
								List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
								MensajesErrores mensajesErrores = new MensajesErrores();
								mensajesErrores.IdInterno = 0L;
								mensajesErrores.TipoMensaje = "ERROR";
								mensajesErrores.Codigo = "ERROR_FOLIO_NO_CORRESPONDE";
								mensajesErrores.Mensaje = "El valor de IdContratoSAAPV proporcionado no corresponde con el Folio";

								entidadContrato.FolioContrato = "";
								entidadContrato.IdContratoSAAPV = 0L;
								listMensajesErrores.add(mensajesErrores);
								entidadContrato.Mensajes = listMensajesErrores;

								listaMensajesErrores.add(mensajesErrores);
								ContadorErrores++;
								entidadContrato.Mensajes = listMensajesErrores;
								
								salida.P_OUT_ERRORESCONTADOR = ContadorErrores;
								salida.p_Out_EntidadContrato = entidadContrato;
								return salida;
							}
							
							TipoOperacion = "M";
						}else {
							
							HashMap<String,Object> entradaSelectDatosAPV = new HashMap<String,Object>();
							entradaSelectDatosAPV.put("p_RegimenTriburario", contrato.Regimen.get(0).Regimen);
							entradaSelectDatosAPV.put("p_Producto", contrato.Regimen.get(0).Producto);
							entradaSelectDatosAPV.put("p_IdPlan", contrato.IdPlan);
							List<SelectDatosAPV> SelectDatosAPV = sql.query(conn, SQL_SELECT_DATOS_APV, entradaSelectDatosAPV, SelectDatosAPV.class);
							contrato.NombrePlan =  SelectDatosAPV.get(0).NombrePlanTipoInversion;
							contrato.CodigoAgrupador = SelectDatosAPV.get(0).CodigoAgrupador;
							contrato.IdMatrizAgrupador= SelectDatosAPV.get(0).IdMatrizAgrupadorAPV;
							
							HashMap<String,Object> parametroEntrada = new HashMap<String,Object>();
							parametroEntrada.put("p_IdCliente", contrato.IdCliente);
							parametroEntrada.put("p_IdContratoSAAPV", contrato.IdContratoSAAPV);
							
							logger.debug("Select Folio Contrato SAAPV");
							List<SelectFolioContratoSAAPV> SelectFolioContratoSAAPV = sql.query(conn, SQL_SELECT_FOLIO_CONTRATO_SAAPV, parametroEntrada, SelectFolioContratoSAAPV.class);
							parametroEntrada.clear();
							
							//si el Idportafolio  es nulo usar IdPortafolioContrato como IdPortafolio
							if(SelectFolioContratoSAAPV.get(0).IdPortafolio == null) {
								contrato.Regimen.get(0).IdPortafolio  =  SelectFolioContratoSAAPV.get(0).IdPortafolioContrato;
							}else {
								contrato.Regimen.get(0).IdPortafolio  =  SelectFolioContratoSAAPV.get(0).IdPortafolio;
							}				
							
							if(!SelectFolioContratoSAAPV.isEmpty())
							{
								FolioPrevio = SelectFolioContratoSAAPV.get(0).FolioContratoSAAPV;
								TipoOperacion = "M";
							}
							else
							{
								TipoOperacion = "I";							
							}
							
							for(SelectFolioContratoSAAPV folioContratoSAAPV : SelectFolioContratoSAAPV) {
								if(folioContratoSAAPV.FolioContratoSAAPV.equalsIgnoreCase(FolioPrevio) && !"".equalsIgnoreCase(FolioPrevio)) {
									contrato.DiaDescuento = contrato.DiaDescuento.compareTo(0L) == 0 ? folioContratoSAAPV.DiaDescuento : contrato.DiaDescuento;
									contrato.IdMedioRecaudacionFormaPago = contrato.IdMedioRecaudacionFormaPago.compareTo(0L) == 0 ? folioContratoSAAPV.IdMedioRecaudacionFormaPago : contrato.IdMedioRecaudacionFormaPago;
								}
							}
							
						}
					}
					else
					{
						HashMap<String,Object> parametroEntrada = new HashMap<String,Object>();
						parametroEntrada.put("p_IdCliente", contrato.IdCliente);
						logger.debug("Select Contrato SAAPV");
						List<SelectContratoSAAPV> SelectContratoSAAPV = sql.query(conn, SQL_SELECT_CONTRATO_SAAPV, parametroEntrada, SelectContratoSAAPV.class);
						
						if(!SelectContratoSAAPV.isEmpty())
						{
							for(SelectContratoSAAPV contratoSAAPV : SelectContratoSAAPV) {
								if(contratoSAAPV.IdEstado.equals("VIG") && contratoSAAPV.IdEstadoSolicitud.equals("APR")) {
									List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
					    			 
									MensajesErrores mensajesErrores = new MensajesErrores();
									mensajesErrores.IdInterno = 0L;
									mensajesErrores.TipoMensaje = "ERROR";
									mensajesErrores.Codigo = "CONTRATO_VIGENTE_APROBADO";
									mensajesErrores.Mensaje = "Contrato Folio " + contratoSAAPV.FolioContratoSAAPV +" se encuentra con estado vigente y con estado solicitud aprobado, intente cerrar el contrato para continuar";
									entidadContrato.FolioContrato = contratoSAAPV.FolioContratoSAAPV;
									entidadContrato.IdContratoSAAPV = 0L;
									listMensajesErrores.add(mensajesErrores);
									entidadContrato.Mensajes = listMensajesErrores;

									listaMensajesErrores.add(mensajesErrores);
									ContadorErrores++;
									entidadContrato.Mensajes = listMensajesErrores;
									
									salida.P_OUT_ERRORESCONTADOR = ContadorErrores;
									salida.p_Out_EntidadContrato = entidadContrato;
									return salida; 
								}
							}
							
							Long idProductoAgrupador = Long.parseLong(contrato.CodigoAgrupador.replace("GRUPO ", ""), 10) ;
							
							for(SelectContratoSAAPV selectContratoSAAPV : SelectContratoSAAPV)
							{
								if(selectContratoSAAPV.IdPlan.equals(contrato.IdPlan) &&
									selectContratoSAAPV.IdMedioRecaudacionFormaPago.equals(contrato.IdMedioRecaudacionFormaPago) &&
									selectContratoSAAPV.IdMedioRecaudacionTrabajador.equals(contrato.IdMedioRecaudacionTrabajador) &&
									selectContratoSAAPV.IdMatrizAgrupadorAPV.equals(contrato.IdMatrizAgrupador) && 
									selectContratoSAAPV.IdProductoAgrupador.equals(idProductoAgrupador))
								{
									if(!contrato.Regimen.isEmpty())
									{
										for(Regimen regimen : contrato.Regimen)
										{
											if(selectContratoSAAPV.Pago.equals(regimen.Pago) &&
												selectContratoSAAPV.CodigoPortafolio.equals(regimen.Regimen) &&
												selectContratoSAAPV.IdRegimenTributario.equals(regimen.IdRegimen))
											{
												FolioPrevio = selectContratoSAAPV.FolioContratoSAAPV;
												TipoOperacion = "M";
												break;
											}
										}
									}
								}
							}
						}
					}
					
					if (TipoOperacion.equals("M")) {
						HashMap<String,Object> parametroEntrada = new HashMap<String,Object>();
						parametroEntrada.put("IdContratoSAAPV", in.p_Contrato.get(0).IdContratoSAAPV);
						logger.debug("Select valida contrato vigente");
						validaContrato = sql.query(conn, SQL_VALIDA_CONTRATO_VIG, parametroEntrada, ContratoSAAPVVigente.class);
						parametroEntrada.clear();
						
						if (validaContrato.isEmpty()) {
							List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
			    			 
							MensajesErrores mensajesErrores = new MensajesErrores();
							mensajesErrores.IdInterno = 0L;
							mensajesErrores.TipoMensaje = "ERROR";
							mensajesErrores.Codigo = "ERROR_CONTRATO_NO_EXISTE";
							mensajesErrores.Mensaje = "El valor de IdContratoSAAPV proporcionado no es valido";
							entidadContrato.FolioContrato = "";
							entidadContrato.IdContratoSAAPV = 0L;
							listMensajesErrores.add(mensajesErrores);
							entidadContrato.Mensajes = listMensajesErrores;

							listaMensajesErrores.add(mensajesErrores);
							ContadorErrores++;
							entidadContrato.Mensajes = listMensajesErrores;
							
							salida.P_OUT_ERRORESCONTADOR = ContadorErrores;
							salida.p_Out_EntidadContrato = entidadContrato;
							return salida;
						}
					}
			
					//DATOS RESIDENCIA
					HashMap<String,Object> parametrosResidencia = new HashMap<String,Object>();
					parametrosResidencia.put("DocumentoIdentidad", in.p_Contrato.get(0).Rut);
					List<salidaResidenciaCliente> salidaParametrosResidencia = sql.query(conn, SQL_SELECT_RESIDENCIA, parametrosResidencia, salidaResidenciaCliente.class);
					
					// *************************************************
					// PORTAFOLIO MODIFICADOS
					// *************************************************
					List<GrabarContratoSAAPV.PortafoliosModificados> listaPortafoliosModificados = new ArrayList<GrabarContratoSAAPV.PortafoliosModificados>();
					
					if(!contrato.Regimen.isEmpty())
					{
						for(Regimen regimen : contrato.Regimen)
						{
							GrabarContratoSAAPV.PortafoliosModificados portafoliosModificados = new GrabarContratoSAAPV.PortafoliosModificados();
							
							
							portafoliosModificados.OrigenAhorro = regimen.OrigenAhorro;
							
							//SI NO SE ENVIA REGIMEN buscamos codigo
							if(contrato.Regimen.get(0).Regimen.isEmpty()) {
								HashMap<String,Object> EntradaRegimen = new HashMap<String,Object>();
								EntradaRegimen.put("IdRegimen",regimen.IdRegimen);
								logger.debug("Select Regimen");
								List<SalidaRegimen> SalidaRegimen = sql.query(conn, SQL_SELECT_REGIMEN, EntradaRegimen, SalidaRegimen.class);
								regimen.Regimen = SalidaRegimen.get(0).Codigo;
							}
							
							//en caso que sea creacion Contrato
							if(contrato.CodigoTipoSolicitud.equalsIgnoreCase("AUTORIZACION")||contrato.CodigoTipoSolicitud.equalsIgnoreCase("INGRESO")) {
								HashMap<String,Object> entradaSelectDatosAPV = new HashMap<String,Object>();
								entradaSelectDatosAPV.put("p_RegimenTriburario", regimen.Regimen);
								entradaSelectDatosAPV.put("p_Producto", regimen.Producto);
								entradaSelectDatosAPV.put("p_IdPlan", contrato.IdPlan);
								logger.debug("Select Datos APV");
								List<SelectDatosAPV> SelectDatosAPV = sql.query(conn, SQL_SELECT_DATOS_APV, entradaSelectDatosAPV, SelectDatosAPV.class);
									
								if(SelectDatosAPV.isEmpty()) {
									 List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
					    			 
										MensajesErrores mensajesErrores = new MensajesErrores();
										mensajesErrores.IdInterno = 0L;
										mensajesErrores.TipoMensaje = "ERROR";
										mensajesErrores.Codigo = "DATOS_APV_INCORRECTOS";
										mensajesErrores.Mensaje = "Los Datos de APV ingresados, no son correctos(Matriz)";
										entidadContrato.FolioContrato = "";
										entidadContrato.IdContratoSAAPV = 0L;
										listMensajesErrores.add(mensajesErrores);
										entidadContrato.Mensajes = listMensajesErrores;

										listaMensajesErrores.add(mensajesErrores);
										ContadorErrores++;
										entidadContrato.Mensajes = listMensajesErrores;
										
										salida.P_OUT_ERRORESCONTADOR = ContadorErrores;
										salida.p_Out_EntidadContrato = entidadContrato;
										return salida; 
								}
								
								contrato.NombrePlan =  SelectDatosAPV.get(0).NombrePlanTipoInversion;
								
								if(regimen.CodigoAgrupador.isEmpty() || regimen.IdMatrizAgrupador.equals(0l)) {
									contrato.CodigoAgrupador = SelectDatosAPV.get(0).CodigoAgrupador;
									contrato.IdMatrizAgrupador = SelectDatosAPV.get(0).IdMatrizAgrupadorAPV; 
								}else {
									contrato.CodigoAgrupador = SelectDatosAPV.get(0).CodigoAgrupador;
									contrato.IdMatrizAgrupador = regimen.IdMatrizAgrupador; 
								}
								//buscamos IdPortafolio
								HashMap<String, Object> entradaSelectIdPortafolio = new HashMap<String, Object>();
								entradaSelectIdPortafolio.put("p_IdCliente", contrato.IdCliente);
								entradaSelectIdPortafolio.put("p_IdMatrizAgrupadorAPV", SelectDatosAPV.get(0).IdMatrizAgrupadorAPV);
								entradaSelectIdPortafolio.put("p_IdRegimenTributario", SelectDatosAPV.get(0).IdRegimenTributario);
								logger.debug("Select IdPortafolio");
								List<SelectIdPortafolio> SelectIdPortafolio = sql.query(conn, SQL_SELECT_ID_PORTAFOLIO,entradaSelectIdPortafolio, SelectIdPortafolio.class);
								
								//en caso que no encuentre Portafolio
								if(SelectIdPortafolio.isEmpty()) {
									// portafolio para crear
									HashMap<String, Object> entradaSelectPrimerIdPortafolio = new HashMap<String, Object>();
									entradaSelectPrimerIdPortafolio.put("p_IdCliente", contrato.IdCliente);
									logger.debug("Select primer IdPortafolio");
									List<SelectIdPortafolio> SelectPrimerIdPortafolio = sql.query(conn,SQL_SELECT_PRIMER_ID_PORTAFOLIO, entradaSelectPrimerIdPortafolio,SelectIdPortafolio.class);
									regimen.IdPortafolio = SelectPrimerIdPortafolio.get(0).IdPortafolio;
								}else {
									regimen.IdPortafolio  = SelectIdPortafolio.get(0).IdPortafolio;
								}
							}
							portafoliosModificados.IdPortafolio = regimen.IdPortafolio;
							portafoliosModificados.IdPropositoPortafolio = regimen.IdPropositoPortafolio.equalsIgnoreCase("")?"PROPOSITOAPV":regimen.IdPropositoPortafolio;
							portafoliosModificados.MesInicio = regimen.MesInicio;
							portafoliosModificados.MesTermino = !"".equalsIgnoreCase(regimen.MesTermino) ? regimen.MesTermino : "9999-01";
							portafoliosModificados.Pago = regimen.Pago;
							portafoliosModificados.PagoNombre = regimen.TipoRecaudacion;
							portafoliosModificados.PorcentajeRegimenImponible = regimen.PorcentajeRegimenImponible.doubleValue();
							portafoliosModificados.Producto = regimen.Producto;
							portafoliosModificados.Regimen = regimen.Regimen;
							portafoliosModificados.CodigoAgrupador  = contrato.CodigoAgrupador;
							portafoliosModificados.IdMatrizAgrupador = contrato.IdMatrizAgrupador;
							
							if(regimen.Moneda.equals(_c.Pesos))
							{
								portafoliosModificados.MontoPesos = regimen.MontoTotal.doubleValue();
							} 
							else 
							{
								portafoliosModificados.MontoUF = regimen.MontoTotal.doubleValue();
							}

							portafoliosModificados.Pago = regimen.Pago;
							portafoliosModificados.PagoNombre = regimen.TipoRecaudacion;
							portafoliosModificados.PorcentajeRegimenImponible = regimen.PorcentajeRegimenImponible.doubleValue();
							portafoliosModificados.Producto = regimen.Producto;
							portafoliosModificados.Regimen = regimen.Regimen;
							portafoliosModificados.CodigoAgrupador  = contrato.CodigoAgrupador;
							
							listaPortafoliosModificados.add(portafoliosModificados);
						}
 						if(("TOT").equals(contrato.TipoTraspaso)) {
 							contrato.DetalleOrigenTraspaso.get(0).MontoTraspaso = contrato.Regimen.get(0).MontoTotal.longValue() > 0L ? contrato.Regimen.get(0).MontoTotal : BigDecimal.ZERO;
 							contrato.DetalleOrigenTraspaso.get(0).PorcentajeTraspaso = new BigDecimal(100);
 						} else if(("PAR").equals(contrato.TipoTraspaso)) {
 							for(DetalleOrigenTraspaso detalle : contrato.DetalleOrigenTraspaso) {
 								if(detalle.PorcentajeTraspaso != null && detalle.PorcentajeTraspaso.compareTo(BigDecimal.ZERO) > 0) {
 									detalle.MontoTraspaso = contrato.Regimen.get(0).MontoTotal.longValue() > 0L ? contrato.Regimen.get(0).MontoTotal : BigDecimal.ZERO;
 								}
 							}
 						}
					}
					
					// RESCATE DATOS FICHA CLIENTE
					HashMap<String,Object> parametrosEntradaFichaCliente = new HashMap<String,Object>();
					parametrosEntradaFichaCliente.put("p_IdCliente", contrato.IdCliente);
					parametrosEntradaFichaCliente.put("p_IdPortafolio", listaPortafoliosModificados.get(0).IdPortafolio);
					logger.debug("Select FichaCliente");
					List<salidaParametrosFichaCliente> salidaParametrosFichaCliente = sql.query(conn, SQL_SELECT_FICHACLIENTE, parametrosEntradaFichaCliente, salidaParametrosFichaCliente.class);
					parametrosEntradaFichaCliente.clear();
					
					
					// rellenar o no con los datos de la ficha de cliente  - CASO BIZNET Y WEB 
					
				     String UsarDatosFichaCliente = contrato.UsarDatosFichaCliente;
				     
 				     if(UsarDatosFichaCliente.equalsIgnoreCase("N")){
				    	 salidaParametrosFichaCliente.get(0).IdPersona = contrato.IdPersona;
				    	 salidaParametrosFichaCliente.get(0).DocumentoIdentidad = contrato.Rut;
				    	 salidaParametrosFichaCliente.get(0).Nombres  =contrato.Nombres;
				    	 salidaParametrosFichaCliente.get(0).ApellidoPaterno= contrato.ApellidoPaterno;
				    	 salidaParametrosFichaCliente.get(0).ApellidoMaterno = contrato.ApellidoMaterno;
				    	 salidaParametrosFichaCliente.get(0).FechaNacimiento = contrato.FechaNacimiento;
				    	 salidaParametrosFichaCliente.get(0).Direccion = contrato.Direccion;
				    	 salidaParametrosFichaCliente.get(0).NumeroDireccion  =contrato.NumeroDireccion;
				    	 salidaParametrosFichaCliente.get(0).TelefonoFijo = contrato.TelefonoFijo;
				    	 salidaParametrosFichaCliente.get(0).TelefonoMovil= contrato.TelefonoMovil;
				    	 salidaParametrosFichaCliente.get(0).TelefonoLaboral = contrato.TelefonoLaboral;
				    	 salidaParametrosFichaCliente.get(0).IdPais= salidaParametrosFichaCliente.get(0).IdPais;
				    	 salidaParametrosFichaCliente.get(0).Pais = contrato.Pais;
				    	 salidaParametrosFichaCliente.get(0).IdRegion = salidaParametrosFichaCliente.get(0).IdRegion;
				    	 salidaParametrosFichaCliente.get(0).Region = contrato.Region;
				    	 salidaParametrosFichaCliente.get(0).IdComuna = salidaParametrosFichaCliente.get(0).IdComuna;
				    	 salidaParametrosFichaCliente.get(0).Comuna = contrato.Comuna;
				    	 salidaParametrosFichaCliente.get(0).IdCiudad = salidaParametrosFichaCliente.get(0).IdCiudad;
				    	 salidaParametrosFichaCliente.get(0).Ciudad = contrato.Ciudad;
				    	 salidaParametrosFichaCliente.get(0).EstadoCivil = contrato.IdEstadoCivil;
				    	 salidaParametrosFichaCliente.get(0).Genero = contrato.IdGenero;
				    	 salidaParametrosFichaCliente.get(0).DireccionElectronica = contrato.Email;
				    	 salidaParametrosFichaCliente.get(0).IdCategoriaInversionista = contrato.IdCategoriaInversionista;
				 		 salidaParametrosFichaCliente.get(0).IdPersonaEmpleador = contrato.IdPersonaEmpleador;
				 		salidaParametrosFichaCliente.get(0).RazonSocialEmpleador = contrato.RazonSocialEmpleador;
				 		salidaParametrosFichaCliente.get(0).RutEmpleador = contrato.RutEmpleador;
				 		salidaParametrosFichaCliente.get(0).IdPaisEmpleador = contrato.IdPaisEmpleador;
				 		salidaParametrosFichaCliente.get(0).DireccionEmpleador = contrato.DireccionEmpleador;
				 		salidaParametrosFichaCliente.get(0).NumeroDireccionEmpleador = "0";
				 		salidaParametrosFichaCliente.get(0).IdCiudadEmpleador = contrato.IdCiudadEmpleador;
				 		salidaParametrosFichaCliente.get(0).IdComunaEmpleador = contrato.IdComunaEmpleador;
				 		salidaParametrosFichaCliente.get(0).TelefonoEmpleador = contrato.TelefonoEmpleador;
				     }
					
					
					// *************************************************
					// PARAMETROS CONTRATO
					// *************************************************
					List<GrabarContratoSAAPV.ParametrosContrato> listaParametrosContrato = new ArrayList<GrabarContratoSAAPV.ParametrosContrato>();
					GrabarContratoSAAPV.ParametrosContrato parametrosContrato = new GrabarContratoSAAPV.ParametrosContrato();
					
					String APVHabilitado = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "APV.Habilitado");
					boolean validacionesAPV = "PROPOSITOAPV".equalsIgnoreCase(contrato.IdPropositoPortafolio) && "S".equalsIgnoreCase(APVHabilitado);
					String perfilComercialPorDefecto = "";
					if (validacionesAPV) {
					    try {
					        perfilComercialPorDefecto = obtenerPerfilComercialPorDefecto(conn, in.p_IdEmpresa, contrato.IdCategoriaInversionista);

					    } catch (Exception e) {
					        logger.error("Error obteniendo perfil comercial APV. "+ "categoría: {}", contrato.IdCategoriaInversionista);

					        MensajesErrores mensajesErrores = new MensajesErrores();

					        mensajesErrores.IdInterno = 0L;
					        mensajesErrores.TipoMensaje = "ERROR";
					        mensajesErrores.Codigo = "PERFIL_COMERCIAL_APV_EXENTO_INVALIDO";
					        mensajesErrores.Mensaje = Diccionario.getMensaje(conn, in.p_IdEmpresa, mensajesErrores.Codigo).msg();
					        entidadContrato.FolioContrato = "";
					        entidadContrato.IdContratoSAAPV = 0L;
					        listMensajesErrores.add(mensajesErrores);
					        entidadContrato.Mensajes = listMensajesErrores;
					        ContadorErrores++;
					        salida.P_OUT_ERRORESCONTADOR = ContadorErrores;
					        salida.p_Out_EntidadContrato = entidadContrato;

					        return salida;
					    }
					}
		
					
					boolean esDependiente = "DEPENDIENTE".equalsIgnoreCase(contrato.Trabajador);
					
					parametrosContrato.IdContratoSAAPV = contrato.IdContratoSAAPV;
					parametrosContrato.IdEmpresa = in.p_IdEmpresa;
					parametrosContrato.FolioContratoSAAPV = NuevoFolioContratoSAAPV;
					parametrosContrato.IdPlan = contrato.IdPlan;
					parametrosContrato.NombrePlan = contrato.NombrePlan;
					parametrosContrato.FechaSuscripcion = contrato.FechaSuscripcion;
					parametrosContrato.FechaCreacion = contrato.FechaCreacion;
					parametrosContrato.FechaAprobacion = contrato.FechaAprobacion; 
					parametrosContrato.FechaNotificacion = contrato.FechaNotificacion;
					parametrosContrato.FechaRecepcion = contrato.FechaRecepcion;
					parametrosContrato.Folio = FolioPrevio;
					parametrosContrato.IdPersona = salidaParametrosFichaCliente.get(0).IdPersona; 
					parametrosContrato.IdEmpleador = contrato.IdEmpleador;
					parametrosContrato.IdCliente = contrato.IdCliente;
					parametrosContrato.IdCategoriaInversionista = salidaParametrosFichaCliente.get(0).IdCategoriaInversionista;
					parametrosContrato.IdPerfilComercial = validacionesAPV ? perfilComercialPorDefecto : salidaParametrosFichaCliente.get(0).IdPerfilComercial == null ? "" : salidaParametrosFichaCliente.get(0).IdPerfilComercial;
					parametrosContrato.NombreCliente = salidaParametrosFichaCliente.get(0).Nombres;
					parametrosContrato.ApellidoPaternoCliente = salidaParametrosFichaCliente.get(0).ApellidoPaterno; 
					parametrosContrato.ApellidoMaternoCliente = salidaParametrosFichaCliente.get(0).ApellidoMaterno; 
					parametrosContrato.DocumentoIdentidadCliente = contrato.Rut;
					parametrosContrato.FechaNacimientoCliente = salidaParametrosFichaCliente.get(0).FechaNacimiento.toString();
					parametrosContrato.EstadoCivil = salidaParametrosFichaCliente.get(0).EstadoCivil;
					parametrosContrato.IdTipoGenero = salidaParametrosFichaCliente.get(0).Genero;
					parametrosContrato.IdPaisResidenciaCliente = salidaParametrosFichaCliente.get(0).IdPais;
					parametrosContrato.IdCiudadCliente = salidaParametrosFichaCliente.get(0).IdCiudad;
					parametrosContrato.IdComunaCliente = salidaParametrosFichaCliente.get(0).IdComuna;
					parametrosContrato.DireccionCliente = salidaParametrosFichaCliente.get(0).Direccion;
					parametrosContrato.NumeroDireccionCliente = salidaParametrosFichaCliente.get(0).NumeroDireccion;
					parametrosContrato.IdTipoCargoPersona = contrato.IdCargoPersona;
					parametrosContrato.TelefonoFijo = salidaParametrosFichaCliente.get(0).TelefonoFijo != null ? salidaParametrosFichaCliente.get(0).TelefonoFijo : "";
					parametrosContrato.TelefonoMovil = salidaParametrosFichaCliente.get(0).TelefonoMovil != null ? salidaParametrosFichaCliente.get(0).TelefonoMovil : "";
					parametrosContrato.TelefonoLaboral = salidaParametrosFichaCliente.get(0).TelefonoLaboral != null ? salidaParametrosFichaCliente.get(0).TelefonoLaboral : "";
					parametrosContrato.EmailCliente = salidaParametrosFichaCliente.get(0).DireccionElectronica; 
					parametrosContrato.RazonSocialEmpleador = contrato.RazonSocialEmpleador;
					parametrosContrato.IdMedioRecaudacionTrabajador = contrato.IdMedioRecaudacionTrabajador;
					parametrosContrato.IdEntidadRecaudadora = contrato.IdEntidadRecaudadora;
					parametrosContrato.DocumentoIdentidadEmpleador = contrato.RutEmpleador;
					parametrosContrato.IdPaisEmpleador = contrato.IdPaisEmpleador;
					parametrosContrato.DireccionComercialEmpleador = contrato.DireccionEmpleador;
					parametrosContrato.NumeroDireccionEmpleador = contrato.NumeroDireccionEmpleador;
					parametrosContrato.IdCiudadEmpleador = contrato.IdCiudadEmpleador;
					parametrosContrato.IdComunaEmpleador = contrato.IdComunaEmpleador;
					parametrosContrato.TelefonoEmpleador = contrato.TelefonoEmpleador;
					parametrosContrato.NombreEncargado = contrato.NombreEncargado;
					parametrosContrato.EmailEncargado = contrato.EmailEncargado;
					parametrosContrato.TelefonoEncargado = contrato.TelefonoEncargado;
					parametrosContrato.CodigoPrevision = contrato.CodigoPrevision;
					parametrosContrato.Pensionado = "SI".equalsIgnoreCase(contrato.Pensionado) ? "SI" : "NO";
					parametrosContrato.Trabajador = esDependiente ? "DEPENDIENTE" : "INDEPENDIENTE";
					parametrosContrato.FormaDePago = contrato.FormaDePago;
					parametrosContrato.IdMedioRecaudacionFormaPago = contrato.IdMedioRecaudacionFormaPago;
					parametrosContrato.DiaDescuento = contrato.DiaDescuento;
					parametrosContrato.Institucion = contrato.CodigoPrevision.toString();
					parametrosContrato.ComisionTransferencia = contrato.ComisionTransferencia;
				    //parametrosContrato.CodigoTipoSolicitud = (contrato.DetalleOrigenTraspaso ? "TRA" : "";
					//parametrosContrato.TipoMovimiento = contrato.TipoMovimiento;
					parametrosContrato.RazonRetiro = contrato.RazonRetiro;
					parametrosContrato.NombreEntidadOrigen = "";
					parametrosContrato.RutEntidadOrigen = "";
					//parametrosContrato.IdPersonaOrigen = 0L;
					parametrosContrato.Descripcion = contrato.Descripcion;
					parametrosContrato.TipoDeAhorro = "";
					parametrosContrato.FondoDeOrigen = "";
					parametrosContrato.TipoTraspaso = contrato.TipoTraspaso;
					parametrosContrato.MontoTraspaso = 0L;
					parametrosContrato.UFTraspaso = 0L;
					parametrosContrato.PorcentajeTraspaso = 0L;
					parametrosContrato.IdEstadoEnvioNotificacion = contrato.IdEstadoEnvioNotificacion;
					parametrosContrato.IdEstadoSolicitud = contrato.IdEstadoSolicitud;
					parametrosContrato.IdEstado = contrato.Estado;
					parametrosContrato.IdCargaMasiva = contrato.IdCargaMasiva;
					parametrosContrato.IdEmpleado = salidaParametrosFichaCliente.get(0).IdEjecutivo != null ? salidaParametrosFichaCliente.get(0).IdEjecutivo : contrato.IdEmpleador;
					parametrosContrato.IdPropositoPortafolio = contrato.IdPropositoPortafolio;
					parametrosContrato.IdMatrizAgrupadorAPV = contrato.IdMatrizAgrupador;
					parametrosContrato.CodigoAgrupador = contrato.CodigoAgrupador;
					parametrosContrato.Canal = contrato.Canal;
					parametrosContrato.EsAporteCuentaInversion = contrato.EsAporteCuentaInversion;
					//parametrosContrato.IdInterno = ;
					
					if(contrato.RutEmpleador.equals("") && esDependiente) {
						parametrosContrato.IdEmpleador = salidaParametrosFichaCliente.get(0).IdPersonaEmpleador;
						parametrosContrato.RazonSocialEmpleador = salidaParametrosFichaCliente.get(0).RazonSocialEmpleador;
						parametrosContrato.DocumentoIdentidadEmpleador = salidaParametrosFichaCliente.get(0).RutEmpleador;
						parametrosContrato.IdPaisEmpleador = salidaParametrosFichaCliente.get(0).IdPaisEmpleador;
						parametrosContrato.DireccionComercialEmpleador = salidaParametrosFichaCliente.get(0).DireccionEmpleador;
						parametrosContrato.NumeroDireccionEmpleador = salidaParametrosFichaCliente.get(0).NumeroDireccionEmpleador;
						parametrosContrato.IdCiudadEmpleador = salidaParametrosFichaCliente.get(0).IdCiudadEmpleador;
						parametrosContrato.IdComunaEmpleador = salidaParametrosFichaCliente.get(0).IdComunaEmpleador;
						parametrosContrato.TelefonoEmpleador = salidaParametrosFichaCliente.get(0).TelefonoEmpleador;
					}
					
					listaParametrosContrato.add(parametrosContrato);
					
					
					// *************************************************
					// DISTRIBUCION INVERSION
					// *************************************************
					
					
					
					
					// *************************************************
					
					List<GrabarContratoSAAPV.DistribucionInversion> listaDistribucionInversion = new ArrayList<GrabarContratoSAAPV.DistribucionInversion>();
					
					if (contrato.Regimen.isEmpty()) {
						List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
						MensajesErrores mensajesErrores = new MensajesErrores();
						mensajesErrores.IdInterno = 0L;
						mensajesErrores.TipoMensaje = "ERROR";
						mensajesErrores.Codigo = "ERROR_REGIMEN_VACIO";
						mensajesErrores.Mensaje = "Debe ingresar un regimen";
						entidadContrato.FolioContrato = "";
						entidadContrato.IdContratoSAAPV = 0L;
						listMensajesErrores.add(mensajesErrores);
						entidadContrato.Mensajes = listMensajesErrores;
					
						listaMensajesErrores.add(mensajesErrores);
						ContadorErrores++;
						entidadContrato.Mensajes = listMensajesErrores;
						
						salida.P_OUT_ERRORESCONTADOR = ContadorErrores;
						salida.p_Out_EntidadContrato = entidadContrato;
						return salida;
					}
					
					if(!contrato.Distribucion.isEmpty()) {
						int i = 0;	
						BigDecimal	montoDistribucionTotal	= BigDecimal.ZERO;
								
						for(Distribucion distribucion : contrato.Distribucion) {
							//validacion monto porcentaje					
							
						     if(contrato.Distribucion.get(i).MontoParcial == null && contrato.Distribucion.get(i).PorcentajeParcial == null) {
						    	 List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
						    	 
									MensajesErrores mensajesErrores = new MensajesErrores();

									mensajesErrores.IdInterno = 0L;
									mensajesErrores.TipoMensaje = "ERROR";
									mensajesErrores.Codigo = "ERROR_MONTO_O_PORCENTAJE_VACIO";
									mensajesErrores.Mensaje = "Debe ingresar monto o porcentaje a la distribucion " + String.valueOf(i+1);
									
									entidadContrato.FolioContrato = "";
									entidadContrato.IdContratoSAAPV = 0L;
									
									listMensajesErrores.add(mensajesErrores);
									entidadContrato.Mensajes = listMensajesErrores;

									listaMensajesErrores.add(mensajesErrores);
									ContadorErrores++;
								
									entidadContrato.Mensajes = listMensajesErrores;
									
									salida.P_OUT_ERRORESCONTADOR = ContadorErrores;
									salida.p_Out_EntidadContrato = entidadContrato;
									return salida;
						    	 			    	 
						     }
							// si solo viene porcentaje en la distribucion
							if (contrato.Distribucion.get(i).PorcentajeParcial != null && contrato.Distribucion.get(i).MontoParcial == null) {
								// si porcentaje es 0
								if (contrato.Distribucion.get(i).PorcentajeParcial.equals(BigDecimal.ZERO)) {
									List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
									MensajesErrores mensajesErrores = new MensajesErrores();
									mensajesErrores.IdInterno = 0L;
									mensajesErrores.TipoMensaje = "ERROR";
									mensajesErrores.Codigo = "ERROR_PORCENTAJE_CERO";
									mensajesErrores.Mensaje = "Porcentaje en cero, Debe ingresar un porcentaje en la distribucion";
									entidadContrato.FolioContrato = "";
									entidadContrato.IdContratoSAAPV = 0L;
									listMensajesErrores.add(mensajesErrores);
									entidadContrato.Mensajes = listMensajesErrores;

									listaMensajesErrores.add(mensajesErrores);
									ContadorErrores++;
									entidadContrato.Mensajes = listMensajesErrores;

									salida.P_OUT_ERRORESCONTADOR = ContadorErrores;
									salida.p_Out_EntidadContrato = entidadContrato;
									return salida;
								}
								// Se calcula el monto equivalente al porcentaje
								// de la distribucion
								if (!contrato.Regimen.get(0).MontoTotal.equals(BigDecimal.ZERO)) {
									contrato.Distribucion.get(i).MontoParcial = contrato.Regimen.get(0).MontoTotal.multiply(contrato.Distribucion.get(i).PorcentajeParcial).divide(new BigDecimal(100));
								} else {
									List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();

									MensajesErrores mensajesErrores = new MensajesErrores();
									mensajesErrores.IdInterno = 0L;
									mensajesErrores.TipoMensaje = "ERROR";
									mensajesErrores.Codigo = "ERROR_MONTO_DEPOSITO_CERO";
									mensajesErrores.Mensaje = "monto en cero, Debe ingresar monto de deposito para la creacion de la distribucion";
									listMensajesErrores.add(mensajesErrores);
									entidadContrato.Mensajes = listMensajesErrores;

									listaMensajesErrores.add(mensajesErrores);
									ContadorErrores++;
									entidadContrato.Mensajes = listMensajesErrores;

									salida.P_OUT_ERRORESCONTADOR = ContadorErrores;
									salida.p_Out_EntidadContrato = entidadContrato;
									return salida;
								}

							}

							// si solo viene monto en la distribucion
							if (contrato.Distribucion.get(i).PorcentajeParcial == null && contrato.Distribucion.get(i).MontoParcial != null) {
								// si monto es 0
								if (contrato.Distribucion.get(i).MontoParcial.equals(BigDecimal.ZERO)) {
									List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();

									MensajesErrores mensajesErrores = new MensajesErrores();
									mensajesErrores.IdInterno = 0L;
									mensajesErrores.TipoMensaje = "ERROR";
									mensajesErrores.Codigo = "ERROR_MONTO_CERO";
									mensajesErrores.Mensaje = "monto en cero, Debe ingresar un monto en la distribucion";
									entidadContrato.FolioContrato = "";
									entidadContrato.IdContratoSAAPV = 0L;
									listMensajesErrores.add(mensajesErrores);
									entidadContrato.Mensajes = listMensajesErrores;

									listaMensajesErrores.add(mensajesErrores);
									ContadorErrores++;
									entidadContrato.Mensajes = listMensajesErrores;

									salida.P_OUT_ERRORESCONTADOR = ContadorErrores;
									salida.p_Out_EntidadContrato = entidadContrato;
									return salida;
								}
								// si monto total es 0
								if (!contrato.Regimen.get(0).MontoTotal.equals(BigDecimal.ZERO)) {
									contrato.Distribucion.get(i).PorcentajeParcial = new BigDecimal(100).multiply(contrato.Distribucion.get(i).MontoParcial).divide(contrato.Regimen.get(0).MontoTotal, 8, RoundingMode.HALF_UP);
								} else {
									List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
									MensajesErrores mensajesErrores = new MensajesErrores();
									mensajesErrores.IdInterno = 0L;
									mensajesErrores.TipoMensaje = "ERROR";
									mensajesErrores.Codigo = "ERROR_MONTO_DEPOSITO_CERO";
									mensajesErrores.Mensaje = "monto en cero, Debe ingresar monto  deposito para la creacion de la distribucion";
									entidadContrato.FolioContrato = "";
									entidadContrato.IdContratoSAAPV = 0L;
									listMensajesErrores.add(mensajesErrores);
									entidadContrato.Mensajes = listMensajesErrores;

									listaMensajesErrores.add(mensajesErrores);
									ContadorErrores++;
									entidadContrato.Mensajes = listMensajesErrores;

									salida.P_OUT_ERRORESCONTADOR = ContadorErrores;
									salida.p_Out_EntidadContrato = entidadContrato;
									return salida;
								}
							}

							HashMap<String, Object> hashInstrumentos = obtenerInstrumentos(contrato.Distribucion, conn);
							GrabarContratoSAAPV.DistribucionInversion distribucionInversion = new GrabarContratoSAAPV.DistribucionInversion();
							Instrumento instrumento = (Instrumento) hashInstrumentos.get(distribucion.IdSerieFondo);
							distribucionInversion.IdContratoSAAPV = contrato.IdContratoSAAPV;
							distribucionInversion.FolioContratoSAAPV = NuevoFolioContratoSAAPV;
							distribucionInversion.IdInstrumento = instrumento.IdInstrumento;
							distribucionInversion.Nemotecnico = instrumento.Nemotecnico;
							distribucionInversion.MontoPesos = distribucion.MontoParcial.doubleValue();
							distribucionInversion.Porcentaje = distribucion.PorcentajeParcial.doubleValue();
							distribucionInversion.IdEstado = distribucion.IdEstado;

							listaDistribucionInversion.add(distribucionInversion);
							i++;

							montoDistribucionTotal = montoDistribucionTotal.add(distribucion.MontoParcial);

						}
						
						// se valida monto total distribucion con el monto deposito 
						if (montoDistribucionTotal.compareTo(contrato.Regimen.get(0).MontoTotal) != 0) {
							 List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
								MensajesErrores mensajesErrores = new MensajesErrores();
								mensajesErrores.IdInterno = 0L;
								mensajesErrores.TipoMensaje = "ERROR";
								mensajesErrores.Codigo = "UI_TOTAL_DISTRIBUCION_DIFERENTE_A_MONTO_DEPOSITO";
								mensajesErrores.Mensaje = "UI_TOTAL_DISTRIBUCION_DIFERENTE_A_MONTO_DEPOSITO";
								entidadContrato.FolioContrato = "";
								entidadContrato.IdContratoSAAPV = 0L;
								listMensajesErrores.add(mensajesErrores);
								entidadContrato.Mensajes = listMensajesErrores;

								listaMensajesErrores.add(mensajesErrores);
								ContadorErrores++;
								break;
						}
					}
					
					
					
					// *************************************************
					// DETALLE ORIGEN TRASPASO
					// *************************************************
					List<GrabarContratoSAAPV.DetalleOrigenTraspaso> listaDetalleOrigenTraspaso = new ArrayList<GrabarContratoSAAPV.DetalleOrigenTraspaso>();
					
					if(!contrato.DetalleOrigenTraspaso.isEmpty()) {
						HashMap<String,Object> parametrosEntidadRecaudadora = new HashMap<String,Object>();
						for(DetalleOrigenTraspaso detalleOrigenTraspasoContrato : contrato.DetalleOrigenTraspaso) {
							
							if (detalleOrigenTraspasoContrato.DocumentoEntidad != null && !detalleOrigenTraspasoContrato.DocumentoEntidad.equalsIgnoreCase("")) {
								parametrosEntidadRecaudadora.put("DocumentoIdentidad", detalleOrigenTraspasoContrato.DocumentoEntidad);
							} else {
								List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
				    			 
								MensajesErrores mensajesErrores = new MensajesErrores();
								mensajesErrores.IdInterno = 0L;
								mensajesErrores.TipoMensaje = "ERROR";
								mensajesErrores.Codigo = "ERROR_ENTIDAD_RECAUDADORA_TRASPASO";
								mensajesErrores.Mensaje = "Debe ingresar una entidad recaudadora valida";
								entidadContrato.FolioContrato = "";
								entidadContrato.IdContratoSAAPV = 0L;
								listMensajesErrores.add(mensajesErrores);
								entidadContrato.Mensajes = listMensajesErrores;

								listaMensajesErrores.add(mensajesErrores);
								ContadorErrores++;
								entidadContrato.Mensajes = listMensajesErrores;
									
								salida.P_OUT_ERRORESCONTADOR = ContadorErrores;
								salida.p_Out_EntidadContrato = entidadContrato;
								return salida; 
							}
							
							if (contrato.TipoTraspaso == null || contrato.TipoTraspaso.equalsIgnoreCase("")) {
								List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
				    			 
								MensajesErrores mensajesErrores = new MensajesErrores();
								mensajesErrores.IdInterno = 0L;
								mensajesErrores.TipoMensaje = "ERROR";
								mensajesErrores.Codigo = "ERROR_TIPO_TRASPASO";
								mensajesErrores.Mensaje = "Debe ingresar un tipo de traspaso valido";
								entidadContrato.FolioContrato = "";
								entidadContrato.IdContratoSAAPV = 0L;
								listMensajesErrores.add(mensajesErrores);
								entidadContrato.Mensajes = listMensajesErrores;

								listaMensajesErrores.add(mensajesErrores);
								ContadorErrores++;
								entidadContrato.Mensajes = listMensajesErrores;
									
								salida.P_OUT_ERRORESCONTADOR = ContadorErrores;
								salida.p_Out_EntidadContrato = entidadContrato;
								return salida; 
							}
							
							if (detalleOrigenTraspasoContrato.CodigoTipoAhorro != null && detalleOrigenTraspasoContrato.CodigoTipoAhorro.equalsIgnoreCase("APV")) {
								if (detalleOrigenTraspasoContrato.Fondo == null || !fondos.contains(detalleOrigenTraspasoContrato.Fondo)) {
									List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
					    			 
									MensajesErrores mensajesErrores = new MensajesErrores();
									mensajesErrores.IdInterno = 0L;
									mensajesErrores.TipoMensaje = "ERROR";
									mensajesErrores.Codigo = "ERROR_TIPO_AHORRO_FONDO";
									mensajesErrores.Mensaje = "Fondo no valido para Tipo Ahorro APV";
									entidadContrato.FolioContrato = "";
									entidadContrato.IdContratoSAAPV = 0L;
									listMensajesErrores.add(mensajesErrores);
									entidadContrato.Mensajes = listMensajesErrores;

									listaMensajesErrores.add(mensajesErrores);
									ContadorErrores++;
									entidadContrato.Mensajes = listMensajesErrores;
										
									salida.P_OUT_ERRORESCONTADOR = ContadorErrores;
									salida.p_Out_EntidadContrato = entidadContrato;
									return salida; 
								}
							}
							
							if (contrato.TipoTraspaso.equalsIgnoreCase("PAR") && !(detalleOrigenTraspasoContrato.MontoTraspaso.compareTo(BigDecimal.ZERO) > 0 || detalleOrigenTraspasoContrato.UFTraspaso.compareTo(BigDecimal.ZERO) > 0 || detalleOrigenTraspasoContrato.PorcentajeTraspaso.compareTo(BigDecimal.ZERO) > 0)) {
								List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
				    			 
								MensajesErrores mensajesErrores = new MensajesErrores();
								mensajesErrores.IdInterno = 0L;
								mensajesErrores.TipoMensaje = "ERROR";
								mensajesErrores.Codigo = "ERROR_MONTO_UF_PORCENTAJE_TRASPASO";
								mensajesErrores.Mensaje = "Si el traspaso es parcial, debe ingresar monto, uf o porcentaje";
								entidadContrato.FolioContrato = "";
								entidadContrato.IdContratoSAAPV = 0L;
								listMensajesErrores.add(mensajesErrores);
								entidadContrato.Mensajes = listMensajesErrores;

								listaMensajesErrores.add(mensajesErrores);
								ContadorErrores++;
								entidadContrato.Mensajes = listMensajesErrores;
									
								salida.P_OUT_ERRORESCONTADOR = ContadorErrores;
								salida.p_Out_EntidadContrato = entidadContrato;
								return salida; 
							}
							
						}
						List<EntidadRecaudadora> entidadRecaudadora = sql.query(conn, SQL_SELECT_ENTIDAD_RECAUDADORA, parametrosEntidadRecaudadora, EntidadRecaudadora.class);
					    HashMap<String,Object> entidadHash = Utils.List2Hash(entidadRecaudadora,"DocumentoIdentidad");
						for(DetalleOrigenTraspaso detalleOrigenTraspasoContrato : contrato.DetalleOrigenTraspaso)
						{
							GrabarContratoSAAPV.DetalleOrigenTraspaso detalleOrigenTraspaso = new GrabarContratoSAAPV.DetalleOrigenTraspaso();
							EntidadRecaudadora entidad = (EntidadRecaudadora) entidadHash.get(detalleOrigenTraspasoContrato.DocumentoEntidad);
							if (entidad == null) {
								List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
				    			 
								MensajesErrores mensajesErrores = new MensajesErrores();
								mensajesErrores.IdInterno = 0L;
								mensajesErrores.TipoMensaje = "ERROR";
								mensajesErrores.Codigo = "ERROR_ENTIDAD_RECAUDADORA_TRASPASO";
								mensajesErrores.Mensaje = "Debe ingresar una entidad recaudadora valida";
								entidadContrato.FolioContrato = "";
								entidadContrato.IdContratoSAAPV = 0L;
								listMensajesErrores.add(mensajesErrores);
								entidadContrato.Mensajes = listMensajesErrores;

								listaMensajesErrores.add(mensajesErrores);
								ContadorErrores++;
								entidadContrato.Mensajes = listMensajesErrores;
									
								salida.P_OUT_ERRORESCONTADOR = ContadorErrores;
								salida.p_Out_EntidadContrato = entidadContrato;
								return salida;  
							}
							detalleOrigenTraspaso.IdDetalleOrigenTraspaso = 0L; 
							detalleOrigenTraspaso.IdTraspaso = 0L;
							detalleOrigenTraspaso.CodigoTipoAhorro = detalleOrigenTraspasoContrato.CodigoTipoAhorro;
							detalleOrigenTraspaso.IdEntidadRecaudadora = entidad.IdEntidadRecaudadora;
							detalleOrigenTraspaso.NombreEntidad = entidad.Nombre;
							detalleOrigenTraspaso.DocumentoEntidad = entidad.DocumentoIdentidad;
							detalleOrigenTraspaso.Fondo = (detalleOrigenTraspasoContrato.CodigoTipoAhorro.equalsIgnoreCase("APV")) ? detalleOrigenTraspasoContrato.Fondo : "";
							detalleOrigenTraspaso.MontoTraspaso = detalleOrigenTraspasoContrato.MontoTraspaso.doubleValue();
							detalleOrigenTraspaso.UFTraspaso = (contrato.TipoTraspaso.equalsIgnoreCase("TOT")) ? 0 : detalleOrigenTraspasoContrato.UFTraspaso.doubleValue();
							detalleOrigenTraspaso.PorcentajeTraspaso = detalleOrigenTraspasoContrato.PorcentajeTraspaso.doubleValue();
							detalleOrigenTraspaso.IdEstado = "VIG";
							detalleOrigenTraspaso.IdInstrumento = 0L;
							detalleOrigenTraspaso.CodigoRegimenTributario = "0";
							detalleOrigenTraspaso.IdPortafolio = 0L;
							parametrosContrato.IdPersonaOrigen = entidad.IdPersona;
							parametrosContrato.RutEntidadOrigen = entidad.DocumentoIdentidad;
							parametrosContrato.NombreEntidadOrigen = entidad.Nombre;
							parametrosContrato.TipoDeAhorro = detalleOrigenTraspasoContrato.CodigoTipoAhorro;
							
							listaDetalleOrigenTraspaso.add(detalleOrigenTraspaso);
						}
						
						
					}
					parametrosContrato.CodigoTipoSolicitud = listaDetalleOrigenTraspaso.size() > 0 ? "TRA" : "";
					parametrosContrato.TipoMovimiento = listaDetalleOrigenTraspaso.size() > 0 ? "INGRESO" : contrato.TipoMovimiento;
					// *************************************************
					// GRABAR CONTRATO SAAPV
					// *************************************************
					GrabarContratoSAAPV grabarContratoSAAPV = new GrabarContratoSAAPV();
					GrabarContratoSAAPV.ParametrosEntradaServicio inGrabarContratoSAAPV = new GrabarContratoSAAPV.ParametrosEntradaServicio();
					
					inGrabarContratoSAAPV.p_IdUsuario = in.p_IdUsuario;
					inGrabarContratoSAAPV.p_IdEmpresa = in.p_IdEmpresa;
					inGrabarContratoSAAPV.p_IdModulo = "APV";
					inGrabarContratoSAAPV.p_Tipo = TipoOperacion;
					inGrabarContratoSAAPV.p_Parametros = listaParametrosContrato;
					inGrabarContratoSAAPV.p_DistribucionInversion = listaDistribucionInversion;
					inGrabarContratoSAAPV.p_PortafoliosModificados = listaPortafoliosModificados;
					inGrabarContratoSAAPV.p_DetalleOrigenTraspaso = listaDetalleOrigenTraspaso;
					
					GrabarContratoSAAPV.ParametrosSalidaServicio salidaGrabarContratoSAAPV = grabarContratoSAAPV.logica(conn, inGrabarContratoSAAPV);
					

					//Cerramos el contrato Antiguo en caso que el tipo de solicitud sea una modificacion
					if( TipoOperacion.equals("M")){
						
						/*
						HashMap<String,Object> parametroEntrada = new HashMap<String,Object>();
						parametroEntrada.put("IdContratoSAAPV", in.p_Contrato.get(0).IdContratoSAAPV);
						List<ContratoSAAPVVigente> validaContrato = sql.query(conn, SQL_VALIDA_CONTRATO_VIG, parametroEntrada, ContratoSAAPVVigente.class);
						*/
						
						// validamos si el contrato anterior es con estado vigente
						if(validaContrato.get(0).IdEstado.equals("VIG")) {
							
							if(in.p_Contrato.get(0).IdContratoSAAPV != null)
							{
							    
								List<ParamsContrato_Update> update_contrato = new ArrayList<ParamsContrato_Update>();
								ParamsContrato_Update param = new ParamsContrato_Update();
								param.IdContratoSAAPV = in.p_Contrato.get(0).IdContratoSAAPV;
								update_contrato.add(param);
								sql.batch(conn, SQL_UPDATE_CONTRATO, update_contrato,ParamsContrato_Update.class);
								AgregarLog(conn, in);
							}
						}else {
							 List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
			    			 
							 MensajesErrores mensajesErrores = new MensajesErrores();
							 mensajesErrores.IdInterno = 0L;
							 mensajesErrores.TipoMensaje = "ERROR";
							 mensajesErrores.Codigo = "ERROR_CONTRATO_NO_VIGENTE";
							 mensajesErrores.Mensaje = "El monto a modificar no es vigente";
							 entidadContrato.FolioContrato = "";
							 entidadContrato.IdContratoSAAPV = 0L;
							 listMensajesErrores.add(mensajesErrores);
							 entidadContrato.Mensajes = listMensajesErrores;

							 listaMensajesErrores.add(mensajesErrores);
							 ContadorErrores++;
							 entidadContrato.Mensajes = listMensajesErrores;
								
							 salida.P_OUT_ERRORESCONTADOR = ContadorErrores;
							 salida.p_Out_EntidadContrato = entidadContrato;
							 return salida; 
						}
					}
					
					salida.P_REF_ID_PORTAFOLIO = salidaGrabarContratoSAAPV.P_REF_ID_PORTAFOLIO;	
					
					
					for (MensajeSalida mensajeSalida : salidaGrabarContratoSAAPV.P_OUT_MENSAJES)
					{
						MensajesErrores mensajesErrores = new MensajesErrores();
						
						mensajesErrores.IdInterno = 0L;
						mensajesErrores.TipoMensaje = mensajeSalida.tipo;
						mensajesErrores.Codigo = mensajeSalida.codigo;
						mensajesErrores.Mensaje = mensajeSalida.descripcion;
						
						if(mensajeSalida.tipo.equals("ERROR"))
						{
							List<MensajesErrores> listErrores = new ArrayList<MensajesErrores>();
							
							listErrores.add(mensajesErrores);
							entidadContrato.Mensajes = listErrores;
							entidadContrato.IdContratoSAAPV = mensajeSalida.id;
							
							ContadorErrores++;
							break;
						}
						else
						{
							listMensajesErrores.add(mensajesErrores);
							entidadContrato.Mensajes = listMensajesErrores;
							entidadContrato.IdContratoSAAPV = mensajeSalida.id;
						}
					}
					
					entidadContrato.FolioContrato = NuevoFolioContratoSAAPV;
					break;
				}
				else
				{
					MensajesErrores mensajesErrores = new MensajesErrores();
					
					mensajesErrores.IdInterno = 0L;
					mensajesErrores.TipoMensaje = "ERROR";
					mensajesErrores.Codigo = "DEBE_INGRESAR_UN_FOLIO_CONTRATO";
					mensajesErrores.Mensaje = "DEBE_INGRESAR_UN_FOLIO_CONTRATO";
					
					entidadContrato.FolioContrato = "";
					entidadContrato.IdContratoSAAPV = 0L;
					
					listMensajesErrores.add(mensajesErrores);
					entidadContrato.Mensajes = listMensajesErrores;
					ContadorErrores++;
				}
			}
			
			salida.P_OUT_ERRORESCONTADOR = ContadorErrores;
			salida.p_Out_EntidadContrato = entidadContrato;
		}
		else
		{
			EntidadContrato entidadContrato = new EntidadContrato();
			MensajesErrores mensajesErrores = new MensajesErrores();
			
			List<MensajesErrores> listMensajesErrores = new ArrayList<MensajesErrores>();
			
			mensajesErrores.IdInterno = 0L;
			mensajesErrores.TipoMensaje = "ERROR";
			mensajesErrores.Codigo = "NO_HAY_CONTRATOS_PARA_INGRESAR";
			mensajesErrores.Mensaje = "NO_HAY_CONTRATOS_PARA_INGRESAR";
			
			entidadContrato.FolioContrato = "";
			entidadContrato.IdContratoSAAPV = 0L;
			
			listMensajesErrores.add(mensajesErrores);
			entidadContrato.Mensajes = listMensajesErrores;
			
			salida.P_OUT_ERRORESCONTADOR = 1L;
			salida.p_Out_EntidadContrato = entidadContrato;
		}
		logger.info("Termina CrearContratoSAAPV");
		return salida;
	}
	
	private Boolean esGeneracionFolioAutomatico(String folioEntrada) {
		Boolean esGeneracionFolioAutomatico = folioEntrada != null && folioEntrada.length() == 1 && Character.isLetter(folioEntrada.charAt(0));
		return esGeneracionFolioAutomatico;
	}
	
	private String obtenerPerfilComercialPorDefecto(Connection con, Long idEmpresa, String categoriaInversionista) throws Exception {
		try {
			String parametroPerfilComercial = "APV.PerfilComercial."+categoriaInversionista;
			String perfilComercial = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", parametroPerfilComercial);
			
			if(perfilComercial == null || "".equalsIgnoreCase(perfilComercial)) {
				logger.error("No existe configuración APV para el parámetro");
				throw new Exception("No existe configuración APV para el parámetro "+ parametroPerfilComercial);
			}
			PerfilComercialAPV perfilComercialAPV = obtenerPerfilComercialAPVValido(con, perfilComercial);
			return perfilComercialAPV.IdPerfilComercialBase;
			
		} catch (Exception e) {
			logger.error("Error obteniendo perfil comercial APV para categoría");
			throw e;
		}
		
	}
	private PerfilComercialAPV obtenerPerfilComercialAPVValido(Connection con, String perfilComercial) throws Exception {
		HashMap<String, Object> parametros = new HashMap<String, Object>();
	    parametros.put("p_IdPerfilComercialBase", perfilComercial);
	    SQL sql = new SQL();
	    List<PerfilComercialAPV> perfiles = sql.query(con, SQL_VALIDAR_PERFIL_COMERCIAL_APV, parametros, PerfilComercialAPV.class);
	    PerfilComercialAPV perfil = perfiles.stream().findFirst().orElse(null);
	    if (perfil == null) {
	    	throw new Exception("El perfil comercial configurado [" +perfilComercial+ "] no existe, está inactivo o no tiene asociado APV_EXENTO");	    	
    	}

	    return perfil;
	}
	
	
	public void AgregarLog(Connection con, CrearContratoSAAPV_Entrada in) {
		try {
			String FechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");	
			Long IdCliente = in.p_Contrato.get(0).IdCliente;
			String FolioContratoSAAPV = in.p_Contrato.get(0).FolioContratoSAAPV;
			Long IdPersona = in.p_Contrato.get(0).IdPersona;
			Long IdCabeceraLog = (IdCliente == 0) ? IdPersona : IdCliente;
			
			{
				//CABECERA
				AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
				
				AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
				
				log.IdCabeceraLog = IdCabeceraLog;
				log.IdTipoLog = "MODIFICACION";
				log.IdModulo = "APV";
				log.IdUsuario = in.p_IdUsuario;
				log.IdEmpresa = in.p_IdEmpresa;
				log.FechaSistemaLog = FechaSistema;
				log.IdUsuarioAtendido = 0L;
				log.IdCliente = IdCliente;
				log.EstadoCliente = "";
				log.NombreCliente = "";
				log.IdTipoDocumentoIdentidad = "";
				log.IdDocumentoIdentidad = "";
				log.IdPortafolio = 0L;
				log.EstadoPortafolio = "";
				log.IdTipoRegistro = "CONTRATO-SAAPV";
				log.EstadoRegistro = "";
				log.IdTipoOperacion = "";
				log.Folio = 0L;
				log.Cantidad = BigDecimal.ZERO;
				log.PrecioTasa = BigDecimal.ZERO;
				log.Monto = BigDecimal.ZERO;
				log.IdMonedaOrigen = "";
				log.IdMonedaLiquidacion = "";
				log.TipoCambio = "";
				log.Nemo = "";
				log.IdProceso = 0L;
				log.EstadoProceso = "ING";
				log.NombreProceso = "Cerrar Contrato SAAPV";
				
				if (IdCliente == 0) {
					log.IdPersona = IdPersona;
				}
				
				entradaAgregarLog.p_Log.add(log);
				
				//DETALLE
				entradaAgregarLog.p_LogDetalle.add(agregarDetalleLog(IdCabeceraLog, "FolioContratoSAAPV", FolioContratoSAAPV));
				entradaAgregarLog.p_LogDetalle.add(agregarDetalleLog(IdCabeceraLog, "Descripción", "Cierre de contrato antiguo por tipo de solicitud"));
				
				//REGISTRO
				AgregarLog agregarLogService = new AgregarLog();
				agregarLogService.logica(con, entradaAgregarLog);
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public  InAgregarLog.DetalleLog agregarDetalleLog(Long IdCabeceraLog, String CampoRegistro, String ValorNuevo) {
		InAgregarLog.DetalleLog detLog = new InAgregarLog.DetalleLog();
		detLog.IdCabeceraLog = IdCabeceraLog; 
		detLog.IdTrabajo = 0L;
		detLog.SubRegistro = "CONTRATO-SAAPV"; 
		detLog.CampoRegistro = CampoRegistro; 
		detLog.ValorAntiguo = ""; 
		detLog.ValorNuevo = ValorNuevo;
		return detLog;
	}
	
	//-------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE - CALL
	//-------------------------------------------------------------------------------------------
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception
	{
		Gson gson = Utils.getGsonBuilder();
		CrearContratoSAAPV_Entrada in = gson.fromJson(jin.toString(), CrearContratoSAAPV_Entrada.class);
		CrearContratoSAAPV_Salida out = logica(conn, in);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	//-------------------------------------------------
	// METODO PARA PRUEBAS LOCALES 
	//-------------------------------------------------
	public static void main(String[] args)
	{
		System.out.println("== INICIO PRUEBA LOCAL ==");
		
		try (Connection con = PlataformaUtils.getPRESURAConnection())
		{
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			


			String jsonCL = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":\"21\",\"p_ClaveUsuario\":\"e53bcfb69a40ff736ce8666432b7f5ec3dc61be7dbc392b4cfda8b7031c12c1f\",\"p_Contrato\":[{\"Canal\":\"WEB\",\"FolioContratoSAAPV\":\"0000404794\",\"Rut\":\"14552020-7\",\"Regimen\":[{\"OrigenAhorro\":\"APV\",\"MesInicio\":\"2020-05\",\"Pago\":\"DIRECTA\",\"Producto\":\"APV Fondos Mutuos\",\"IdRegimen\":2,\"MontoTotal\":15000,\"Moneda\":\"CLP\"}],\"Distribucion\":[{\"FolioContratoSAAPV\":\"0000404793\",\"IdSerieFondo\":\"00038CCLP\",\"MontoParcial\":15000,\"PorcentajeParcial\":100}],\"IdPlan\":1,\"FechaSuscripcion\":\"2020-03-25\",\"IdMedioRecaudacionTrabajador\":\"2\",\"Pensionado\":\"N\",\"Trabajador\":\"D\",\"FormaDePago\":\"DIRECTA\",\"CodigoTipoSolicitud\":\"AUTORIZACION\",\"UsarDatosFichaCliente\":\"S\",\"NumeroDireccionEmpleador\":\"9999\",\"IdCliente\":5937}]}";
	
			CrearContratoSAAPV_Entrada Objin = gson.fromJson(jsonCL, CrearContratoSAAPV_Entrada.class);

			CrearContratoSAAPV demo = new CrearContratoSAAPV();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			
			try {
				CrearContratoSAAPV_Salida ObjOut = demo.logica(con, Objin);
				//con.commit();
				con.rollback();
				System.out.println("SALIDA");
				System.out.println(ObjOut.toString());
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
			con.rollback();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		System.out.println("== FIN PRUEBA LOCAL ==");
	}
}