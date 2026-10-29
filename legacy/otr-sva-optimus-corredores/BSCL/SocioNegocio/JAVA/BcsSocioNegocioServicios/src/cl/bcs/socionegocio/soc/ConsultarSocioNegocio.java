package cl.bcs.socionegocio.soc;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.ordenes.CrearOrdenes.OrdenProceso;
import cl.bcs.ordenes.CrearOrdenes.RequisitoOperativoCrearOrden;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.soc.CrearSocioNegocio.SelectIdPersona;
import cl.bcs.socionegocio.wbs.ConsultarFichaCliente.DatosPortafolio;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarSocioNegocio implements JavaCall {

	//*****************************************************//
	// NOTAS :
	// 1.- p_IdUsuario ES STRING O LONG
	// 2.- EL Tipo DE LA DireccionElectronica NO SE MODIFICA
	
	//-------------------------------------------------
	// QUERIES
	//-------------------------------------------------
	
	private final String SQL_SELECT_IDPERSONA						= loadResourceToString("cl/bcs/socionegocio/soc/sql/CrearSocioNegocio_SelectIdPersona.sql");
	private final String SQL_SELECT_DATOSPERSONA					= loadResourceToString("cl/bcs/socionegocio/soc/sql/ConsultarSocioNegocio_SelectPersonaPorID.sql");
	private final String SQL_SELECT_DATOSCLIENTE					= loadResourceToString("cl/bcs/socionegocio/soc/sql/ConsultarSocioNegocio_SelectClientePorIdPersona.sql");
	private final String SQL_SELECT_DOCUMENTOIDENTIDAD				= loadResourceToString("cl/bcs/socionegocio/soc/sql/ConsultarSocioNegocio_SelectDocumentoIdentidad.sql");
	private final String SQL_SELECT_DIRECCIONFISICA					= loadResourceToString("cl/bcs/socionegocio/soc/sql/ConsultarSocioNegocio_SelectDireccionFisica.sql");
	private final String SQL_SELECT_TELEFONO						= loadResourceToString("cl/bcs/socionegocio/soc/sql/ConsultarSocioNegocio_SelectTelefono.sql");
	private final String SQL_SELECT_DIRECCIONELECTRONICA			= loadResourceToString("cl/bcs/socionegocio/soc/sql/ConsultarSocioNegocio_SelectDireccionElectronica.sql");
	private final String SQL_SELECT_INDICADORCLIENTE				= loadResourceToString("cl/bcs/socionegocio/soc/sql/ConsultarSocioNegocio_SelectVistaIndicadorCliente.sql");
	private final String SQL_SELECT_INDICADORPERSONA				= loadResourceToString("cl/bcs/socionegocio/soc/sql/ConsultarSocioNegocio_SelectVistaIndicadorPersona.sql");
	private final String SQL_SELECT_CUENTABANCARIA					= loadResourceToString("cl/bcs/socionegocio/soc/sql/ConsultarSocioNegocio_SelectCuentaBancaria.sql");
	private final String SQL_SELECT_TARJETABANCARIA					= loadResourceToString("cl/bcs/socionegocio/soc/sql/ConsultarSocioNegocio_SelectTarjetaBancaria.sql");
	private final String SQL_SELECT_VISTADOF						= loadResourceToString("cl/bcs/socionegocio/soc/sql/ConsultarSocioNegocio_SelectVistaDOF.sql");
	private final String SQL_SELECT_VISTADETALLEDOF					= loadResourceToString("cl/bcs/socionegocio/soc/sql/ConsultarSocioNegocio_SelectVistaDetalleDOF.sql");
	private final String SQL_SELECT_NOMBREFANTASIA					= loadResourceToString("cl/bcs/socionegocio/soc/sql/ConsultarSocioNegocio_SelectNombreFantasia.sql");
	private final String SQL_SELECT_RELACION						= loadResourceToString("cl/bcs/socionegocio/soc/sql/ConsultarSocioNegocio_SelectRelacion.sql");
	private final String SQL_SELECT_PLAN_APV						= loadResourceToString("cl/bcs/socionegocio/soc/sql/ConsultarSocioNegocio_SelectPlanAPV.sql");
	private final String SQL_SELECT_DATOSPORTAFOLIO					= loadResourceToString("cl/bcs/socionegocio/soc/sql/ConsultarSocioNegocio_SelectPortafolioCliente.sql");
		
	//-------------------------------------------------
	// CLASES DE ENTRADA 
	//-------------------------------------------------
	public static class ConsultarSocioNegocio_Entrada
	{
		public Long 				p_IdEmpresa;
		public Long					p_IdUsuario; // preguntar si es String o Long
		public String				p_ClaveUsuario;
		public DatosEntidad			p_DatosEntidad;
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
   
	private static class DatosEntidad {
		public String TipoEntidad = ""; //"CLI" o "PER"
		public Long IdEntidad = 0L; //Id de Persona o Id de Cliente
	}
	
    //-------------------------------------------------------------------------------------------
	// CLASES UTILITARIAS QUE REPRESENTAN OBJETOS OUT	
	//-------------------------------------------------------------------------------------------
	
	public static class ConsultarSocioNegocio_Salida {

		public List<MensajeSalida>					P_OUT_MENSAJES;
		public Long									P_OUT_ERRORESCONTADOR = 0L;
		public List<SocioNegocio>		   			P_OUT_RESULTADOPROCESO;
		
		public ConsultarSocioNegocio_Salida()
		{	
			this.P_OUT_MENSAJES 			 = new ArrayList<MensajeSalida>();
			this.P_OUT_RESULTADOPROCESO		 = new ArrayList<SocioNegocio>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	

	/*
	public static class ResultadoProceso_Salida
	{
		public List<SocioNegocio> SocioNegocio;        
		
		public String toString(){
			return Utils.printJson(this, false);
		}
	}
	*/
	public static class SocioNegocio
	{
		public Long							IdPersona					= 0L;
		public Long							IdCliente					= 0L;
		public String						TipoEntidad					= "";
		public String						TipoDocumentoIdentidad		= "";
		public String						DocumentoIdentidad			= "";
		public String						Nombres						= "";
		public String						ApellidoPaterno				= "";
		public String						ApellidoMaterno				= "";
		public String						RazonSocial					= "";
		public Long							IdGiro						= 0L;
		public String						Giro						= "";
		public String						FechaNacimientoConstitucion = "";
		public String						IdEstadoCivil				= "";
		public String						EstadoCivil					= "";
		public Long							IdPaisNacimiento			= 0L;
		public String						PaisNacimiento				= "";
		public String						IdRegimenConyugal			= "";
		public String						RegimenConyugal				= "";
		public Long							IdPaisNacionalidad			= 0L;
		public String						PaisNacionalidad			= "";
		public String						IdGenero					= "";
		public String						Genero						= "";
		public String						IdTipoPersona				= "";
		public String						TipoPersona					= "";
		public Long							IdPaisResidencia			= 0L;
		public String						PaisResidencia				= "";
		public Long							IdProfesion					= 0L;
		public String						Profesion					= "";
		public String						IdTipoRelacion				= "";	//para indicar si esta persona es una persona relacionada
		public String						TipoRelacion				= "";	//para indicar si esta persona es una persona relacionada
		public Long							IdPersonaRelacionada		= 0L;	//en el caso que la relacion no sea con el cliente, sino con otra persona relacionada, aqui se puede indicar
		public Long							IdConyuge					= 0L;
		public Long							IdRepresentante				= 0L;
		public Long							IdPersonaEmpleador			= 0L;
		public String   					IdTipoCanalIngresoCorredor  = "";
		public List<DatosCliente>   		DatosCliente				= new ArrayList();
		public List<TiposDocumento>			TiposDocumento				= new ArrayList();
        public List<DireccionFisica>		DireccionFisica				= new ArrayList();
        public List<Telefono>				Telefono					= new ArrayList();
        public List<DireccionElectronica>	DireccionElectronica		= new ArrayList();
        public List<IndicadorCliente>		IndicadorCliente			= new ArrayList();
        public List<IndicadorPersona>		IndicadorPersona			= new ArrayList();
        public List<CuentaBancaria>			CuentaBancaria				= new ArrayList();
        public List<TarjetaBancaria>		TarjetaBancaria				= new ArrayList();
        public List<DeclaracionOrigenFondo>	DeclaracionOrigenFondo		= new ArrayList();
        public List<NombreFantasia>			NombreFantasia				= new ArrayList();
        public List<DatosPortafolio>		DatosPortafolio				= new ArrayList<>();
		
		@Override
		public String toString()
		{
			//this.FechaNacimientoConstitucion = Fecha.toDate(Fecha.toFormat(this.FechaNacimientoConstitucion, "yyyy-MM-dd")); 
			return Utils.printJson(this, false);
		}
	}
	public static class DatosCliente
	{
        public String	IdCategoriaInversionista	= "";
        public String 	CategoriaInversionista		= "";
        public Long		CodigoEjecutivo				= 0L;
        public String 	Ejecutivo					= "";
        public String	IdTipoCliente				= "";
        public String	TipoCliente					= "";
        public Long		IdPlanTipoInversion 		= 0L;
        public String	NombrePlanTipoInversion 	= "Sin Plan";
        public Long		IdPlanTipoInversionParaTransaccionar 		= 2L; //valor por defecto sura
        public String  SegmentoCliente 				= "";
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	public static class TiposDocumento
	{
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
		public Long		IdDireccionFisica	= 0L;
        public String	IdProposito			= "";
        public String 	Proposito			= "";
        public String	Sector				= "";
        public String	CodigoPostal		= "";
        public Long		IdComuna			= 0L;
        public String	Comuna				= "";
        public String	Calle				= "";
        public String	Numero				= "";
        public String	Departamento		= "";
        public String	Complemento			= "";
        public Long		IdCiudad			= 0L;
        public String	Ciudad				= "";
        
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	public static class Telefono
	{
		public Long		IdTelefono	= 0L;
        public String	IdProposito	= "";
        public String 	Proposito	= "";
        public String	Numero		= "";
        public Long		IdPais		= 0L;
        public String 	Pais		= "";
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
		public Long		IdDireccionElectronica	= 0L;
        public String	Tipo					= "";
        public String	IdProposito				= "";
        public String 	Proposito				= "";
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
	
	public static class CuentaBancaria
	{
		public Long		IdCuentaBancaria			= 0L;
        public String	NumeroCuentaBancaria		= "";
        public String	IdEstado					= "";
        public String	IdMoneda					= "";
        public String	CodigoTipoCuentaBancaria	= "";
        public String	TipoCuentaBancaria			= "";
        public Long		IdBanco						= 0L;
        public String	Banco						= "";
        public String	CodigoSucursal				= "";
        public Long		CodigoPlaza					= 0L;
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	public static class Relacion 
	{
		public Integer	Nivel							;
		public String	IdTipoRelacion				= "";
		public String	TipoRelacion				= "";
		public Long		IdPersonaOrigen				= 0L;
		public Long		IdPersonaDestino			= 0L;
	}
	
	public static class TarjetaBancaria
	{
		public Long		IdTarjetaBancaria	= 0L;
        public Long		IdBanco				= 0L;
        public String	Banco				= "";
        public String	IdTipo				= "";
        public String	Tipo				= "";
        public String	NumeroTarjeta		= "";
        public Integer	CodigoVerificacion	= 0;
        public String	FechaVencimiento	= "";
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	public static class DeclaracionOrigenFondo
	{
		public Long						IdDeclaracionOrigenFondos	= 0L;
        public String					IdTipoOperacionOrigenFondos	= "";
        public String					FechaDeclaracion			= "";
        public String					Comentario					= "";
        public String					OperacionInscripcion		= "";
        public String					IdEstado					= "";
        public List<DetalleOrigenFondo>	DetalleOrigenFondo;
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	public static class SelectIdsPersona
	{
        public Long IdPersona = 0L;
        public Long IdCliente = 0L;
        
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	public static class DetalleOrigenFondo
	{
        public String		IdTipoOrigenFondo	= "";
        public String		TipoOrigenFondo		= "";
        public String		Valor				= "0.0";
        public String		Otros				= "";
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	public static class NombreFantasia
	{
		public Long		IdNombreFantasia	= 0L;
        public String	Nombre				= "";
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	public static class PlanAPV
	{
		public Integer		IdPlanTipoInversion 		= 0;
        public String	NombrePlanTipoInversion 	= "";
        public Long		IdPlanTipoInversionParaTransaccionar 		= 0L;
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	public static class DatosPortafolio extends BcsPojo{
		public Long 	IdPortafolio	 	= 0L;
		public String 	CodigoPortafolio 	= "";
		public String 	IdFormaCobro	 	= "";
	    public String 	IdPerfilComercial	= "";
	    public String 	IdPerfilCobros 		= "";
	    public String 	IdPerfilProductos 	= "";
	    public Long 	IdCliente			= 0L;
	}

	//-------------------------------------------------
	// LOGICA DEL SERVICIO 
	//-------------------------------------------------
	public ConsultarSocioNegocio_Salida logica(Connection conn, ConsultarSocioNegocio_Entrada in) throws Exception
	{
		SQL sql = new SQL();
		
		ConsultarSocioNegocio_Salida salida = new ConsultarSocioNegocio_Salida(); 
		//ResultadoProceso_Salida salida = new ResultadoProceso_Salida();
		Long IdPersona = 0L;
		Long IdEmpresa = 0L; 
		Long IdCliente = 0L; 
		
		List<SocioNegocio> arrPersonas = new ArrayList<SocioNegocio>();
		List<DatosCliente> arrClientes = new ArrayList<DatosCliente>();
		List<TiposDocumento> arrDoctosIdentidad = new ArrayList<TiposDocumento>();
		List<DireccionFisica> arrDireccionesFisica = new ArrayList<DireccionFisica>();
		List<Telefono> arrTelefonos = new ArrayList<Telefono>();
		List<DireccionElectronica> arrDireccionesElectronica = new ArrayList<DireccionElectronica>();
		List<IndicadorCliente> arrIndicadoresCliente = new ArrayList<IndicadorCliente>();
		List<IndicadorPersona> arrIndicadoresPersona = new ArrayList<IndicadorPersona>();
		List<CuentaBancaria> arrCuentasBancaria = new ArrayList<CuentaBancaria>();
		List<TarjetaBancaria> arrTarjetasBancaria = new ArrayList<TarjetaBancaria>();
		List<DeclaracionOrigenFondo> arrDOF = new ArrayList<DeclaracionOrigenFondo>();
		List<DetalleOrigenFondo> arrDetalleDOF = new ArrayList<DetalleOrigenFondo>();
		List<NombreFantasia> arrNombresFantasia = new ArrayList<NombreFantasia>();
		List<Relacion> arrRelaciones = new ArrayList<Relacion>();
		List<Relacion> arrRelaciones1 = new ArrayList<Relacion>();
		List<Relacion> arrRelaciones2 = new ArrayList<Relacion>();
		List<Relacion> arrRelaciones3 = new ArrayList<Relacion>();
		List<Relacion> arrRelaciones4 = new ArrayList<Relacion>();
		List<Relacion> arrRelacionTMP = new ArrayList<Relacion>();
		List<SocioNegocio> arrPersonasTMP = new ArrayList<SocioNegocio>();
		List<PlanAPV> arrPlanAPV = new ArrayList<PlanAPV>();
		List<DatosPortafolio> datosPortafolios = new ArrayList<DatosPortafolio>();
		
		//Rescata de los datos de entrada el valor de IdEmpresa
		IdEmpresa = in.p_IdEmpresa;
		
		//si el Id de Entidad es de un cliente, como el proceso necesita si o si el Id de Persona, con el Id de Cliente
		//va en busca del Id persona a traves de la ejecucion de un query
		if (in.p_DatosEntidad.TipoEntidad.equalsIgnoreCase("CLI") || in.p_DatosEntidad.TipoEntidad.equalsIgnoreCase("CLIENTE") ) {

			HashMap<String,Object> parametroEntrada = new HashMap<String,Object>();
			parametroEntrada.put("p_IdCliente", in.p_DatosEntidad.IdEntidad);
			List<SelectIdsPersona> SelectIdsPersona = sql.query(conn, SQL_SELECT_IDPERSONA, parametroEntrada, SelectIdsPersona.class);
			
			if(SelectIdsPersona.size()>0) {
				
				IdPersona = SelectIdsPersona.get(0).IdPersona;
				//IdCliente = SelectIdsPersona.get(0).IdCliente;
			}
			else {
				//TODO crear mensaje de error por no encontrar el IdPersona
			}
			
		}
		else {
			//entonces se este pasando un Id Persona y con eso se puede trabajar para rescatar los datos de persona
			IdPersona = in.p_DatosEntidad.IdEntidad;
		}
		
		//existe un Id de Persona Valido
        if(IdPersona != 0L) {
        	
        	//entonces va a buscar datos de Persona en la tabla correspondiente
        	
			HashMap<String,Object> parametroEntrada = new HashMap<String,Object>();
			parametroEntrada.put("p_IdPersona", IdPersona);
			arrPersonas = sql.query(conn, SQL_SELECT_DATOSPERSONA, parametroEntrada, SocioNegocio.class);
        	
        }
        
		//encontro a la personaPER
        if(arrPersonas.size()>0) {

        	//le cambia el formato a la fecha (le quita la hora)
        	//arrPersonas.get(0).FechaNacimientoConstitucion = ConvierteFecha(arrPersonas.get(0).FechaNacimientoConstitucion);
        	
        	//entonces va por sus datos de cliente
			HashMap<String,Object> parametroEntrada = new HashMap<String,Object>();
			parametroEntrada.put("p_IdPersona", IdPersona);
			arrClientes = sql.query(conn, SQL_SELECT_DATOSCLIENTE, parametroEntrada, DatosCliente.class);
			parametroEntrada.clear();
			
			//TODO get plan
			parametroEntrada.put("p_IdCliente", arrPersonas.get(0).IdCliente);
			arrPlanAPV = sql.query(conn, SQL_SELECT_PLAN_APV, parametroEntrada, PlanAPV.class);
			parametroEntrada.clear();
			
			if (arrPlanAPV.size()>0) {
				arrClientes.get(0).IdPlanTipoInversion = Long.valueOf(arrPlanAPV.get(0).IdPlanTipoInversion);
				arrClientes.get(0).NombrePlanTipoInversion = arrPlanAPV.get(0).NombrePlanTipoInversion;
				arrClientes.get(0).IdPlanTipoInversionParaTransaccionar = Long.valueOf(arrPlanAPV.get(0).IdPlanTipoInversion);
			}
        	
			//devolvio datos de Cliente
			if(arrClientes.size()>0) {
				//entonces lo agrega a los datos de salida
				arrPersonas.get(0).DatosCliente = arrClientes;
				//IdCliente =  arrClientes.get(0).IdCliente;
			}
			
			//va por los datos de documento de identidad
			parametroEntrada = new HashMap<String,Object>();
			parametroEntrada.put("p_IdPersona", IdPersona);
			arrDoctosIdentidad = sql.query(conn, SQL_SELECT_DOCUMENTOIDENTIDAD, parametroEntrada, TiposDocumento.class);
        	
			//devolvio datos de Identidad
			if(arrDoctosIdentidad.size()>0) {
				//entonces lo agrega a los datos de salida
				arrPersonas.get(0).TiposDocumento = arrDoctosIdentidad;
				
			}

			//va por los datos de Direcciones Fisicas
			parametroEntrada = new HashMap<String,Object>();
			parametroEntrada.put("p_IdPersona", IdPersona);
			arrDireccionesFisica = sql.query(conn, SQL_SELECT_DIRECCIONFISICA, parametroEntrada, DireccionFisica.class);
			
			//devolvio direcciones fisicas
			if(arrDireccionesFisica.size()>0) {
				//entonces lo agrega a los datos de salida
				arrPersonas.get(0).DireccionFisica = arrDireccionesFisica;
			}

			//va por los datos de Telefono
			parametroEntrada = new HashMap<String,Object>();
			parametroEntrada.put("p_IdPersona", IdPersona);
			arrTelefonos = sql.query(conn, SQL_SELECT_TELEFONO, parametroEntrada, Telefono.class);
			
			//devolvio Telefonos
			if(arrTelefonos.size()>0) {
				//entonces lo agrega a los datos de salida
				arrPersonas.get(0).Telefono = arrTelefonos;
			}
			
			//va por los datos de Direccion Electronica
			parametroEntrada = new HashMap<String,Object>();
			parametroEntrada.put("p_IdPersona", IdPersona);
			arrDireccionesElectronica = sql.query(conn, SQL_SELECT_DIRECCIONELECTRONICA, parametroEntrada, DireccionElectronica.class);
			
			//devolvio Direcciones Electronica
			if(arrDireccionesElectronica.size()>0) {
				//entonces lo agrega a los datos de salida
				arrPersonas.get(0).DireccionElectronica = arrDireccionesElectronica;
			}

			//arrPersonas.get(0).TipoEntidad ="PER"; //se asume que es de tipo persona hasta que se demuestre que es cliente mas abajo
			
			//existe id de Cliente
			if("CLI".equalsIgnoreCase(arrPersonas.get(0).TipoEntidad)) {
				IdCliente =  arrPersonas.get(0).IdCliente;
				//va por los datos de Indicadores Cliente
				parametroEntrada = new HashMap<String,Object>();
				parametroEntrada.put("p_IdEmpresa", IdEmpresa);
				parametroEntrada.put("p_IdEntidad", IdCliente);
				arrIndicadoresCliente = sql.query(conn, SQL_SELECT_INDICADORCLIENTE, parametroEntrada, IndicadorCliente.class);
				
				//devolvio Indicadores
				if(arrIndicadoresCliente.size()>0) {
					//entonces lo agrega a los datos de salida
					arrPersonas.get(0).IndicadorCliente = arrIndicadoresCliente;
				}
				//arrPersonas.get(0).TipoEntidad = "CLI";	
			}

			//va por los datos de Indicadores Persona
			parametroEntrada = new HashMap<String,Object>();
			parametroEntrada.put("p_IdEmpresa", IdEmpresa);
			parametroEntrada.put("p_IdEntidad", IdPersona);
			arrIndicadoresPersona = sql.query(conn, SQL_SELECT_INDICADORPERSONA, parametroEntrada, IndicadorPersona.class);
			
			//devolvio Indicadores
			if(arrIndicadoresPersona.size()>0) {
				//entonces lo agrega a los datos de salida
				arrPersonas.get(0).IndicadorPersona = arrIndicadoresPersona;
			}

			//va por los datos de Cuentas Bancarias
			parametroEntrada = new HashMap<String,Object>();
			parametroEntrada.put("p_IdEmpresa", IdEmpresa);
			parametroEntrada.put("p_IdPersona", IdPersona);
			arrCuentasBancaria  = sql.query(conn, SQL_SELECT_CUENTABANCARIA, parametroEntrada, CuentaBancaria.class);
			
			//devolvio Cuentas Bancarias
			if(arrCuentasBancaria.size()>0) {
				//entonces lo agrega a los datos de salida
				arrPersonas.get(0).CuentaBancaria = arrCuentasBancaria;
			}

			//va por los datos de Tarjeta Bancarias
			parametroEntrada = new HashMap<String,Object>();
			parametroEntrada.put("p_IdEmpresa", IdEmpresa);
			parametroEntrada.put("p_IdPersona", IdPersona);
			arrTarjetasBancaria  = sql.query(conn, SQL_SELECT_TARJETABANCARIA, parametroEntrada, TarjetaBancaria.class);
			
			//devolvio Tarjeta Bancarias
			if(arrTarjetasBancaria.size()>0) {
				//entonces lo agrega a los datos de salida
				arrPersonas.get(0).TarjetaBancaria = arrTarjetasBancaria;
			}

			//existe id de Cliente
			if(IdCliente!=0L) {
				//va por los datos de Declaracion Origen Fondos
				parametroEntrada = new HashMap<String,Object>();
				parametroEntrada.put("p_IdEmpresa", IdEmpresa);
				parametroEntrada.put("p_IdCliente", IdCliente);
				arrDOF = sql.query(conn, SQL_SELECT_VISTADOF, parametroEntrada, DeclaracionOrigenFondo.class);
				
				//devolvio Declaraciones
				if(arrDOF.size()>0) {
					
					//recorre las declaraciones de Fondo para ir llenando los detalles de Declaraciones de cada uno
					
					for (DeclaracionOrigenFondo objDOF : arrDOF) {
						
						parametroEntrada = new HashMap<String,Object>();
						parametroEntrada.put("p_IdEmpresa", IdEmpresa);
						parametroEntrada.put("p_IdCliente", IdCliente);
						parametroEntrada.put("p_IdDeclaracionOrigenFondos", objDOF.IdDeclaracionOrigenFondos );
						arrDetalleDOF = sql.query(conn, SQL_SELECT_VISTADETALLEDOF, parametroEntrada, DetalleOrigenFondo.class);
						
						//encontro detalle de Origen de Fondo
						if(arrDetalleDOF.size()>0) {
						  //la agrega como detalle al registro de Declaracion encabezado correspondiente
							objDOF.DetalleOrigenFondo = arrDetalleDOF;
						}
						
						
					}
						
					//entonces lo agrega a los datos de salida
					arrPersonas.get(0).DeclaracionOrigenFondo = arrDOF;
					
				}
				
			}
			
			datosPortafolios = obtenerDatosPortafolio(conn, IdCliente, IdEmpresa);
			if( datosPortafolios != null && datosPortafolios.size() > 0) {
				arrPersonas.get(0).DatosPortafolio = datosPortafolios;
			}
			
			//va por los datos de NombreFantasia
			parametroEntrada = new HashMap<String,Object>();
			parametroEntrada.put("p_IdPersona", IdPersona);
			arrNombresFantasia = sql.query(conn, SQL_SELECT_NOMBREFANTASIA, parametroEntrada, NombreFantasia.class);
			
			//devolvio Nombres de Fantasia
			if(arrNombresFantasia.size()>0) {
				//entonces lo agrega a los datos de salida
				arrPersonas.get(0).NombreFantasia = arrNombresFantasia;
			}

			//inicia proceso de busqueda de personas relacionadas

			//para los casos especiales de la realcion Conyuge (CY) y Representante (REP) 
			//existe en la misma entidad Persona dos campos que apuntan a los Id de estas personas relacionadas
			//asi que los agrego como primeros datos de personas relacionadas solo si no son 0
			
			//existe conyuge
			if(arrPersonas.get(0).IdConyuge>0 && (arrPersonas.get(0).IdEstadoCivil.equalsIgnoreCase("CA") || arrPersonas.get(0).IdEstadoCivil.equalsIgnoreCase("CC"))) {
				
				Relacion objRel = new Relacion();
				objRel.Nivel=1;
				objRel.IdPersonaOrigen = IdPersona;
				objRel.IdPersonaDestino = arrPersonas.get(0).IdConyuge;
				objRel.IdTipoRelacion = "CY";
				objRel.TipoRelacion = "CONYUGE";
				
				arrRelaciones1.add(objRel);
			}

			//existe Representante
			if(arrPersonas.get(0).IdRepresentante >0) {
				
				Relacion objRel = new Relacion();
				objRel.Nivel=1;
				objRel.IdPersonaOrigen = IdPersona;
				objRel.IdPersonaDestino = arrPersonas.get(0).IdRepresentante;
				objRel.IdTipoRelacion = "REP";
				objRel.TipoRelacion = "REPRESENTANTE LEGAL";
				
				arrRelaciones1.add(objRel);
			}
			
			if(arrPersonas.get(0).IdPersonaEmpleador >0) {
				Relacion objRel = new Relacion();
				objRel.Nivel=1;
				objRel.IdPersonaOrigen = IdPersona;
				objRel.IdPersonaDestino = arrPersonas.get(0).IdPersonaEmpleador;
				objRel.IdTipoRelacion = "EMPDOR";
				objRel.TipoRelacion = "EMPLEADOR";
				
				arrRelaciones1.add(objRel);
			}
		
			//encontro relaciones de Conyuge y Representante
			if(arrRelaciones1.size()>0) {
				
				//entonces las agrega al arreglo principal
				AgregaRelaciones(arrRelaciones, arrRelaciones1);
				
			}
			
			//va por los datos de Relaciones
			parametroEntrada = new HashMap<String,Object>();
			parametroEntrada.put("p_IdEmpresa", IdEmpresa);
			parametroEntrada.put("p_IdPersona", IdPersona);
			arrRelacionTMP = sql.query(conn, SQL_SELECT_RELACION, parametroEntrada, Relacion.class);
			
			//encontro relaciones que en este caso serian de primer nivel
			if(arrRelacionTMP.size()>0) {
				//entonces las agrega al arreglo principal
				AgregaRelaciones(arrRelaciones, arrRelacionTMP);
				
			}
			
			//hay relaciones de Primer nivel?
			if(arrRelaciones.size()>0) {
				
				//Las que ya estaban las considero de primer nivel mas la de conyuge y representante que las 
				//agregue a mano cuando correspondian
				for (Relacion objRel : arrRelaciones) {
					objRel.Nivel=1;
					
					//y busco las relaciones de esos de esas entidades secundarias
					
					parametroEntrada = new HashMap<String,Object>();
					parametroEntrada.put("p_IdEmpresa", IdEmpresa);
					parametroEntrada.put("p_IdPersona", objRel.IdPersonaDestino);
					arrRelaciones2 = sql.query(conn, SQL_SELECT_RELACION, parametroEntrada, Relacion.class);
					
					//busca tambien relaciones que estan dadas a nivel del registro persona atributos IdConyuge y IdRepresentante
					arrRelacionTMP = AgregoConyugeyRepresentante(conn, objRel.IdPersonaDestino,2);
					
					if(arrRelacionTMP.size()>0) {
						AgregaRelaciones(arrRelaciones2, arrRelacionTMP);
					}
					
					//recorre entonces dichas relaciones secundarias para agregar un tercer nivel
					
					for (Relacion objRel2 : arrRelaciones2) {
						objRel2.Nivel=2;

						parametroEntrada = new HashMap<String,Object>();
						parametroEntrada.put("p_IdEmpresa", IdEmpresa);
						parametroEntrada.put("p_IdPersona", objRel2.IdPersonaDestino);
						arrRelaciones3 = sql.query(conn, SQL_SELECT_RELACION, parametroEntrada, Relacion.class);

						//busca tambien relaciones que estan dadas a nivel del registro persona atributos IdConyuge y IdRepresentante
						arrRelacionTMP = AgregoConyugeyRepresentante(conn, objRel2.IdPersonaDestino,3);
						
						if(arrRelacionTMP.size()>0) {
							AgregaRelaciones(arrRelaciones3, arrRelacionTMP);
						}

						//recorre entonces dichas relaciones terciarias para agregar un cuarto nivel
						
						for (Relacion objRel3 : arrRelaciones3) {
							objRel2.Nivel=3;

							parametroEntrada = new HashMap<String,Object>();
							parametroEntrada.put("p_IdEmpresa", IdEmpresa);
							parametroEntrada.put("p_IdPersona", objRel3.IdPersonaDestino);
							arrRelaciones4 = sql.query(conn, SQL_SELECT_RELACION, parametroEntrada, Relacion.class);
							
							//busca tambien relaciones que estan dadas a nivel del registro persona atributos IdConyuge y IdRepresentante
							arrRelacionTMP = AgregoConyugeyRepresentante(conn, objRel3.IdPersonaDestino,4);
							
							if(arrRelacionTMP.size()>0) {
								AgregaRelaciones(arrRelaciones4, arrRelacionTMP);
							}
							
							//recorro estas ultimas para asignarles el 4 nivel
							for (Relacion objRel4 : arrRelaciones4) {
								objRel2.Nivel=4;
								
							}							

						}
						
					}
					
				}
				
				//junto todas las relaciones en un solo arreglo 
				AgregaRelaciones(arrRelaciones, arrRelaciones2);
				AgregaRelaciones(arrRelaciones, arrRelaciones3);
				AgregaRelaciones(arrRelaciones, arrRelaciones4);
				
				//recorremos las relaciones para agregar a la lista de salida las Persona que se vayan encontrando
				
				//entonces se recorre todas las personas relacionadas entre si, solo hasta a un "cuarto" nivel
				//y llamamos a las rutinas necesaria para extraer sus datos de personas, direcciones y otros
				for (Relacion objRel : arrRelaciones) {
					
					if (!objRel.IdPersonaDestino.equals(IdPersona)) {
						//busca datos de persona
						parametroEntrada = new HashMap<String,Object>();
						parametroEntrada.put("p_IdPersona", objRel.IdPersonaDestino);
						arrPersonasTMP = sql.query(conn, SQL_SELECT_DATOSPERSONA, parametroEntrada, SocioNegocio.class);
						
						//encontro a la persona
						if(arrPersonasTMP.size()>0) {
							
							//lo agrego al arreglo principal
							arrPersonasTMP.get(0).TipoRelacion = objRel.TipoRelacion;
							arrPersonasTMP.get(0).IdTipoRelacion = objRel.IdTipoRelacion;
							arrPersonasTMP.get(0).IdPersonaRelacionada = objRel.IdPersonaOrigen;
							arrPersonasTMP.get(0).TipoEntidad = "PER"; //asume para las entidades que es una entidad Persona
							
							arrPersonas.add(arrPersonasTMP.get(0));
						
							//va por los datos adicionales de una entidad persona
							
							
							//va por los datos de documento de identidad
							parametroEntrada = new HashMap<String,Object>();
							parametroEntrada.put("p_IdPersona", objRel.IdPersonaDestino);
							arrDoctosIdentidad = sql.query(conn, SQL_SELECT_DOCUMENTOIDENTIDAD, parametroEntrada, TiposDocumento.class);
				        	
							//devolvio datos de Identidad
							if(arrDoctosIdentidad.size()>0) {
								//entonces lo agrega a los datos de salida
								arrPersonasTMP.get(0).TiposDocumento = arrDoctosIdentidad;
								
							}

							//va por los datos de Direcciones Fisicas
							parametroEntrada = new HashMap<String,Object>();
							parametroEntrada.put("p_IdPersona", objRel.IdPersonaDestino);
							arrDireccionesFisica = sql.query(conn, SQL_SELECT_DIRECCIONFISICA, parametroEntrada, DireccionFisica.class);
							
							//devolvio direcciones fisicas
							if(arrDireccionesFisica.size()>0) {
								//entonces lo agrega a los datos de salida
								arrPersonasTMP.get(0).DireccionFisica = arrDireccionesFisica;
							}

							//va por los datos de Telefono
							parametroEntrada = new HashMap<String,Object>();
							parametroEntrada.put("p_IdPersona", objRel.IdPersonaDestino);
							arrTelefonos = sql.query(conn, SQL_SELECT_TELEFONO, parametroEntrada, Telefono.class);
							
							//devolvio Telefonos
							if(arrTelefonos.size()>0) {
								//entonces lo agrega a los datos de salida
								arrPersonasTMP.get(0).Telefono = arrTelefonos;
							}
							
							//va por los datos de Direccion Electronica
							parametroEntrada = new HashMap<String,Object>();
							parametroEntrada.put("p_IdPersona", objRel.IdPersonaDestino);
							arrDireccionesElectronica = sql.query(conn, SQL_SELECT_DIRECCIONELECTRONICA, parametroEntrada, DireccionElectronica.class);
							
							//devolvio Direcciones Electronica
							if(arrDireccionesElectronica.size()>0) {
								//entonces lo agrega a los datos de salida
								arrPersonasTMP.get(0).DireccionElectronica = arrDireccionesElectronica;
							}

							//va por los datos de Indicadores Persona
							parametroEntrada = new HashMap<String,Object>();
							parametroEntrada.put("p_IdEmpresa", IdEmpresa);
							parametroEntrada.put("p_IdEntidad", objRel.IdPersonaDestino);
							arrIndicadoresPersona = sql.query(conn, SQL_SELECT_INDICADORPERSONA, parametroEntrada, IndicadorPersona.class);
							
							//devolvio Indicadores
							if(arrIndicadoresPersona.size()>0) {
								//entonces lo agrega a los datos de salida
								arrPersonasTMP.get(0).IndicadorPersona = arrIndicadoresPersona;
							}

							//va por los datos de Cuentas Bancarias
							parametroEntrada = new HashMap<String,Object>();
							parametroEntrada.put("p_IdEmpresa", IdEmpresa);
							parametroEntrada.put("p_IdPersona", objRel.IdPersonaDestino);
							arrCuentasBancaria  = sql.query(conn, SQL_SELECT_CUENTABANCARIA, parametroEntrada, CuentaBancaria.class);
							
							//devolvio Cuentas Bancarias
							if(arrCuentasBancaria.size()>0) {
								//entonces lo agrega a los datos de salida
								arrPersonasTMP.get(0).CuentaBancaria = arrCuentasBancaria;
							}

							//va por los datos de Tarjeta Bancarias
							parametroEntrada = new HashMap<String,Object>();
							parametroEntrada.put("p_IdEmpresa", IdEmpresa);
							parametroEntrada.put("p_IdPersona", objRel.IdPersonaDestino);
							arrTarjetasBancaria  = sql.query(conn, SQL_SELECT_TARJETABANCARIA, parametroEntrada, TarjetaBancaria.class);
							
							//devolvio Tarjeta Bancarias
							if(arrTarjetasBancaria.size()>0) {
								//entonces lo agrega a los datos de salida
								arrPersonasTMP.get(0).TarjetaBancaria = arrTarjetasBancaria;
							}

							//va por los datos de NombreFantasia
							parametroEntrada = new HashMap<String,Object>();
							parametroEntrada.put("p_IdPersona", objRel.IdPersonaDestino);
							arrNombresFantasia = sql.query(conn, SQL_SELECT_NOMBREFANTASIA, parametroEntrada, NombreFantasia.class);
							
							//devolvio Nombres de Fantasia
							if(arrNombresFantasia.size()>0) {
								//entonces lo agrega a los datos de salida
								arrPersonasTMP.get(0).NombreFantasia = arrNombresFantasia;
							}
							
							//fin datos adicionales de una entidad persona
				
							
						}
					}
				}
				
			}
			
			
        } 
        else {
        	//debe indicar que no encontro a la persona
        	
        	GeneraElementosError(conn, IdEmpresa, "PERSONA_NO_EXISTE", salida.P_OUT_MENSAJES );
        	++salida.P_OUT_ERRORESCONTADOR;
        }
        	
		salida.P_OUT_RESULTADOPROCESO = arrPersonas;
		return salida;
	}
	private void AgregaRelaciones(List<Relacion> arrRelacionesDestino, List<Relacion> arrRelacionesOrigen) {

		for (Relacion objRel : arrRelacionesOrigen) {
			
	        //lo agrega solo si ya no existe la relacion 		
			if(!BuscaSiRelacionExiste(objRel, arrRelacionesDestino )) {
				arrRelacionesDestino.add(objRel);
			}
			
		}
		
	}
	
	
	private  List<DatosPortafolio> obtenerDatosPortafolio(Connection conn, Long idClientes,Long idEmpresa) throws SQLException {
       
        SQL sql = new SQL();
        HashMap<String, Object> parametros = new HashMap<>(2);
        parametros.put("IdCliente", idClientes);
        Map<Long, List<DatosPortafolio>> portafoliosPorIdCliente = sql.query(conn, SQL_SELECT_DATOSPORTAFOLIO, parametros, DatosPortafolio.class)
        		.stream()
        		.collect(Collectors.groupingBy(portafolio -> portafolio.IdCliente));		
        
        List<DatosPortafolio> datosPortafolios = portafoliosPorIdCliente.get(idClientes);

        return  datosPortafolios;
	}
	
	/*
	 Agrega desde los atributos IdConyuge y IdRepresentante
	 las relaciones de una persona tal como se obtendria desde la tabla relaciones
	 */
	private List<Relacion> AgregoConyugeyRepresentante(Connection conn, Long IdPersona, Integer Nivel) throws Exception {
		List<Relacion> arrRelacionTmp = new ArrayList<Relacion>();
        List<SocioNegocio> arrPersonasTMP = new ArrayList<SocioNegocio>();

        SQL sql = new SQL();
        
		HashMap<String,Object> parametroEntrada = new HashMap<String,Object>();
		parametroEntrada.put("p_IdPersona", IdPersona);
		arrPersonasTMP = sql.query(conn, SQL_SELECT_DATOSPERSONA, parametroEntrada, SocioNegocio.class);
		
		//encontro a la persona
		if(arrPersonasTMP.size()>0) {

			//existe conyuge
			if(arrPersonasTMP.get(0).IdConyuge>0) {
				
				Relacion objRel = new Relacion();
				objRel.Nivel=Nivel;
				objRel.IdPersonaOrigen = IdPersona;
				objRel.IdPersonaDestino = arrPersonasTMP.get(0).IdConyuge;
				objRel.IdTipoRelacion = "CY";
				objRel.TipoRelacion = "CONYUGE";
				
				arrRelacionTmp.add(objRel);
			}

			//existe Representante
			if(arrPersonasTMP.get(0).IdRepresentante >0) {
				
				Relacion objRel = new Relacion();
				objRel.Nivel=Nivel;
				objRel.IdPersonaOrigen = IdPersona;
				objRel.IdPersonaDestino = arrPersonasTMP.get(0).IdRepresentante;
				objRel.IdTipoRelacion = "REP";
				objRel.TipoRelacion = "REPRESENTANTE LEGAL";
				
				arrRelacionTmp.add(objRel);
			}
			
		}
		
		return arrRelacionTmp;
	}
	private boolean BuscaSiRelacionExiste(Relacion objRelBuscada, List<Relacion> arrRelacionesDestino ) {
		boolean blnResult = false;
		
		for (Relacion objRel : arrRelacionesDestino) {

			//existe el registro de relacion en el arreglo o lista de destino?
			if(objRel.IdPersonaDestino.equals( objRelBuscada.IdPersonaDestino) && objRel.IdPersonaOrigen.equals(objRelBuscada.IdPersonaOrigen) && objRel.IdTipoRelacion.equalsIgnoreCase(objRelBuscada.IdTipoRelacion)) {
			
				//entonces
				blnResult = true;
				
				break;
					
				
			}
			
			
		}
		
		
		return blnResult;
	}
	//-------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE - CALL
	//-------------------------------------------------------------------------------------------
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception
	{
		Gson gson = Utils.getGsonBuilder();
		ConsultarSocioNegocio_Entrada in = gson.fromJson(jin.toString(), ConsultarSocioNegocio_Entrada.class);
		ConsultarSocioNegocio_Salida out = logica(conn, in);
		
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
		
		try (Connection con = PlataformaUtils.getBOLSAConnection())
		{
			//Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd").serializeNulls().create();
			
			//String jsonCL = "{\"p_ConsultarEnrolamiento\":[{\"IdEnrolamiento\":0,\"IdEmpresa\":1,\"IdUsuario\":3,\"IdTipoEntidad\":\"CLIENTE\",\"IdEntidadEnrolada\":0,\"IdTipoDocumentoIdentidad\":\"R\",\"IdTipoDocumentoIdentidadAsociado\":\"R\",\"DocumentoIdentidad\":\"68370463-7\",\"IdTipoDocumentoIdentidadAdicional\":\"R\",\"DocumentoIdentidadAdicional\":\"\",\"Tipo\":\"N\",\"Nombres\":\"TEST\",\"ApellidoPaterno\":\"ALTA\",\"ApellidoMaterno\":\"CLIENTE\",\"RazonSocial\":\"\",\"IdPaisNacimiento\":1,\"FechaNacimiento\":\"2010-01-01T03:00:00.000Z\",\"Sexo\":\"M\",\"EstadoCivil\":\"SO\",\"IdCiudad\":173,\"IdDivisionPais\":343,\"IdTipoTelefono\":\"FIJO\",\"Telefono\":\"+56225485952\",\"Email\":\"CHERNANDEZ@BOLSADESANTIAGO.COM\",\"DireccionCalle\":\"ASD\",\"DireccionNumero\":\"123\",\"DireccionDepartamento\":\"\",\"FechaCreacion\":\"2018-11-06T17:21:10.190Z\",\"FechaModificacion\":\"2018-11-06T17:21:10.190Z\",\"IdPersonaConyuge\":0,\"RegimenConyugal\":\"\",\"CLI_IdCategoriaInversionista\":\"ARRIESGADO\",\"CLI_IdTipoCliente\":\"FAMILYOFFICE\",\"CLI_IdClaseCliente\":\"\",\"CLI_IdEmpleadoEjecutivo\":7298,\"CLI_CodigoAdicionalHolding\":\"\",\"CLI_ClienteFinal\":\"N\",\"POR_IdPerfilComercial\":\"PERFIL ARRIESGADO BCS\",\"POR_IdPersonaBrokerExtranjero\":0,\"EMP_CodigoEmpleado\":\"\",\"EMP_CodigoTipoCargoEmpleado\":\"\",\"EMP_IdEmpleadoSupervisor\":0,\"EMP_IdSucursal\":0,\"EMP_FechaInicioCargo\":\"0001-01-01\",\"EMP_FechaTerminoCargo\":\"0001-01-01\",\"CNT_CodigoInscripcion\":\"\",\"CNT_Emisor\":\"\",\"CNT_Depositos\":\"\",\"CNT_Bolsa\":\"\",\"CNT_Corredora\":\"\",\"CNT_CamaraDeCompensacion\":\"\",\"CNT_Banco\":\"\",\"CNT_Boveda\":\"\",\"CNT_Otros\":\"\",\"PRO_IdAreaProveedor\":0,\"EMP_CargoIndefinido\":\"\",\"EsRutInterno\":\"N\"}],\"p_IdEmpresa\":1,\"p_IdUsuario\":3,\"p_IdModulo\":\"SOC\"}";
			
		//	String jsonCL = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":\"21\",\"p_ClaveUsuario\":\"string\",\"p_DatosEntidad\":{\"TipoEntidad\":\"PER\",\"IdEntidad\":29445}}";
			
			String jsonCL = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":\"21\",\"p_ClaveUsuario\":\"1\",\"p_DatosEntidad\":{\"TipoEntidad\":\"PER\",\"IdEntidad\":29445}}";
				
			ConsultarSocioNegocio_Entrada Objin = gson.fromJson(jsonCL, ConsultarSocioNegocio_Entrada.class);

			ConsultarSocioNegocio demo = new ConsultarSocioNegocio();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			
			try {
				ConsultarSocioNegocio_Salida ObjOut = demo.logica(con, Objin);
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
	private void GeneraElementosError(Connection con, Long idEmpresa, String codMsg, List<MensajeSalida> P_OUT_MENSAJES) 
			throws Exception {
		
		String msg	=	Diccionario.getMensaje(con,idEmpresa,codMsg)
						.msg();
		
		P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,codMsg,msg));

		
		
	}
/*	
   private java.sql.Date ConvierteFecha(java.sql.Date dtInput) {
	   java.sql.Date dtmRetorno = null;
	   if(dtInput!=null) {
		   dtmRetorno = Fecha.toDate(Fecha.toFormat(dtInput, "yyyy-MM-dd"));
	   }
	   return dtmRetorno;
   }
*/
}
