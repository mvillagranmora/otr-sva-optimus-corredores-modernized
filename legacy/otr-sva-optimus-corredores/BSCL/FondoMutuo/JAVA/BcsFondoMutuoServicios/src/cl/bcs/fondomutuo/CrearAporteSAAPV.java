package cl.bcs.fondomutuo;
import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.APV.ConciliarDeposito;
// SAAPV
import cl.bcs.APV.CrearContratoSAAPV;
import cl.bcs.APV.CrearContratoSAAPV.Contrato;
import cl.bcs.APV.CrearContratoSAAPV.CrearContratoSAAPV_Entrada;
import cl.bcs.APV.CrearContratoSAAPV.CrearContratoSAAPV_Salida;
import cl.bcs.APV.CrearContratoSAAPV.Distribucion;
import cl.bcs.APV.CrearContratoSAAPV.Regimen;
// Recaudacion
import cl.bcs.APV.CrearRecaudacionCliente;
import cl.bcs.APV.CrearRecaudacionCliente.Cliente;
import cl.bcs.APV.CrearRecaudacionDeposito;
import cl.bcs.APV.CrearRecaudacionDeposito.Recaudacion;
import cl.bcs.APV.GrabarRecaudacionTraspaso.ParametrosSalidaServicio;
import cl.bcs.APV.GrabarRecaudacionTraspaso.Resultado;
import cl.bcs.APV.ProcesarDistribucion;
import cl.bcs.APV.ProcesarDistribucion.DistribucionContrato;
import cl.bcs.APV.ProcesarDistribucion.ParametrosRecaudacion;
import cl.bcs.fondomutuo.CalcularMontoNumeroCuota.EntradaCalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.CalcularMontoNumeroCuota.SalidaCalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal.EntradaCrearSolicitudAporteRescateNormal;
import cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal.InFormaPagoSpot;
import cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal.InSolicitudesAporteRescate;
import cl.bcs.fondomutuo.CrearSolicitudAporteRescateNormal.SalidaCrearSolicitudAporteRescateNormal;
import cl.bcs.fondomutuo.pojo.ClientePortafolioSolicitudes;
import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.fondomutuo.pojo.ResultadoSolicitud;
import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearAporteSAAPV  implements JavaCall
{
	private final Log log = new Log("FM.CrearAporteSAAPV ");
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	//-------------------------------------------------
	// QUERIES
	//-------------------------------------------------
	private final String SQL_SELECT_DATOS_APV					= loadResourceToString("cl/bcs/fondomutuo/sql/CrearAporteSAAPV_SelectDatosAPV.sql");
	private final String SQL_SELECT_ID_PORTAFOLIO				= loadResourceToString("cl/bcs/fondomutuo/sql/CrearAporteSAAPV_SelectIdPortafolio.sql");
	private final String SQL_SELECT_PRIMER_ID_PORTAFOLIO		= loadResourceToString("cl/bcs/fondomutuo/sql/CrearAporteSAAPV_SelectPrimerIdPortafolio.sql");
	private final String SQL_SELECT_DATOS_PERSONA				= loadResourceToString("cl/bcs/fondomutuo/sql/CrearAporteSAAPV_SelectDatosPersona.sql");
	private final String SQL_UPDATE_CONTRATO 					= loadResourceToString("cl/bcs/fondomutuo/sql/CrearAporteSAAPV_UpdateContratoSAAPV.sql");
	private final String SQL_CONSULTA_BANCO 					= loadResourceToString("cl/bcs/fondomutuo/sql/CrearAporteSAAPV_SelectConsultaBanco.sql");
	private final String SQL_SELECT_PORTAFOLIO					= loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectPortafolio.sql");
	private final String SQL_SELECT_DISTRIBUCION_CONTRATO		= loadResourceToString("cl/bcs/APV/sql/ConsultaDistribucionContrato.sql");

	//-------------------------------------------------
	// CLASES DE ENTRADA 
	//-------------------------------------------------
	// Entrada para crear Aportes APV
	public static class EntradaCrearAporteSAAPV{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_ClaveUsuario;
		public List<InEntradaRecaudacion> p_EntradaRecaudacion;
		
		public EntradaCrearAporteSAAPV() {
			p_EntradaRecaudacion = new ArrayList<InEntradaRecaudacion>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	/* ************************************************** */
	/* *********** Entrada para la Recaudacion ********** */
	/* ************************************************** */
	public static class InEntradaRecaudacion{
		public Long IdCliente;
		public String Simula;
		public Long IdInterno;
		public String FechaDeposito;
		public Long IdCuentaBanco;
		public Long IdBanco;
		public String CuentaCorriente;
		public BigDecimal MontoDeposito;
		public String Moneda;
		public String Observacion;
		public Long NumeroDocumento;
		public String ConceptoRecaudacion;
		public String IdTipoDeposito;
		public Long IdRecaudacionCliente;
		public Long IdRecaudacionEmpleador;
		public String DocumentoIdentidad;
		public String RegimenTributario;
		public String Origen;
		public String Producto;
		public Long IdPlan;
		public String Pensionado;
		public String Trabajador;
		public String Folio;
		public BigDecimal MontoAGF;
		public String Canal;
		public String GenerarAporteSAAPVsinAbonoCIN;
		public List<InDistribucionContrato> p_DistribucionContrato;
		
		public InEntradaRecaudacion() {
			Canal = "OPT";
			p_DistribucionContrato = new ArrayList<InDistribucionContrato>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	// Entrada Distribucion
	public static class InDistribucionContrato{
		public String IdTipoInstrumento;
		public Long IdInstrumento;
		public String Nemotecnico;
		public BigDecimal Monto;
		public String IdSerieFondo;
		public BigDecimal Porcentaje;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	//-------------------------------------------------
	// CLASES DE SALIDA 
	//-------------------------------------------------
	public static class SalidaCrearAporteSAAPV
	{
		public Long						P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		public List<ResultadoOperacion>	P_OUT_RESULTADO;
		
		public SalidaCrearAporteSAAPV(){
			this.P_OUT_ERRORESCONTADOR=0L;
			this.P_OUT_RESULTADO = new ArrayList<ResultadoOperacion>();
		}
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	public static class ResultadoOperacion
	{
		public String Origen;
		public String Identificador;
		public List<MensajesErrores> Mensajes;
		
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
	public static class ClaseUtilitaria
	{
        public Long		IdInterno;
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	public static class SelectDatosAPV
	{
        public Long		IdMatrizAgrupadorAPV;
        public Long		IdRegimenTributario;
        public String	Codigo;
        public String	NombrePlanTipoInversion;
        public String	CodigoAgrupador;
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	public static class SelectIdPortafolio
	{
        public Long		IdPortafolio;
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	public static class SelectContratoSAAPV
	{
        public Long		IdPersona;
        public Long		IdCliente;
        public Long		IdContratoSAAPV;
        public String	FolioContratoSAAPV;
        public Long		IdPortafolio;
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	public static class ContratoUpdateSAAPV
	{
		 public Long		IdContratoSAAPV;
		 public java.sql.Timestamp FechaCreacion;
		 public java.sql.Timestamp FechaSuscripcion;
		 
		 @Override
			public String toString() {
				return Utils.printJson(this, false);
			}
	}
	
	public static class SelectDatosPersona
	{
		public Long 	IdPersona;
		public String	DocumentoIdentidad;
		public String	Nombres;
		public String	ApellidoPaterno;
		public String	ApellidoMaterno;
		public String	FechaNacimiento;
		public String	Direccion;
		public String	NumeroDireccion;
        public String	Telefono;
        public Long		IdPais;
        public String	Pais;
        public Long		IdRegion;
        public String	Region;
        public Long		IdComuna;
        public String	Comuna;
        public Long		IdCiudad;
        public String	Ciudad;
        public String	EstadoCivil;
        public String	Genero;
        public String	DireccionElectronica;
        public String	IdCategoriaInversionista;
        public Long		IdEjecutivo;
        public String	IdPerfilComercial;
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	public static class CuentaBanco{
		public Long IdBanco;
		public String NumeroCuentaBancaria;
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	public static class PortafolioAPV extends BcsPojo {
		public Long IdPortafolio;
		
		public PortafolioAPV() {
			this.IdPortafolio = 0L;
		}
	}
	
	//-------------------------------------------------
	// LOGICA DEL SERVICIO 
	//-------------------------------------------------
	public SalidaCrearAporteSAAPV logica(Connection con, EntradaCrearAporteSAAPV entrada) throws Exception
	{
		log.write("INICIO::CrearAporteSAAPV");
		SQL sql = new SQL();
		
		SalidaCrearAporteSAAPV out = new SalidaCrearAporteSAAPV();
				
		Long IdInterno = 0L;
		Long ErroresContador = 0L;
		Long idBanco = 0L;
		String cuentaCorriente = "";
		
		List<ResultadoOperacion> listaResultadoOperacion = new ArrayList<ResultadoOperacion>();
		String FechaSistema = Utils.getFechaSistema(con, Utils.getSchema(), entrada.p_IdEmpresa);
				
		// Si la entrada viene con info
		if(entrada.p_EntradaRecaudacion.size() > 0)
		{
			// Recorro el encabezado
			for(InEntradaRecaudacion inEntradaRecaudacion : entrada.p_EntradaRecaudacion)
			{

				inEntradaRecaudacion.p_DistribucionContrato = inEntradaRecaudacion.p_DistribucionContrato
						.stream()
						.filter(distribucion->!(distribucion.IdTipoInstrumento == null && distribucion.IdInstrumento == null && distribucion.Nemotecnico == null && distribucion.Monto == null && distribucion.IdSerieFondo == null && distribucion.Porcentaje == null))
						.collect(Collectors.toList());
				
				if ( "S".equals(inEntradaRecaudacion.GenerarAporteSAAPVsinAbonoCIN) && inEntradaRecaudacion.p_DistribucionContrato.size() == 0 ) {
					
					MensajesErrores mensajesErrores = new MensajesErrores();
					ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();
					List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();

					objResultadoOperacion.Origen = "APV";
					objResultadoOperacion.Identificador = "0";
					
					mensajesErrores.IdInterno = inEntradaRecaudacion.IdInterno;
					mensajesErrores.TipoMensaje = "ERROR";
					mensajesErrores.Codigo = "ERROR_DISTRIBUCION_NECESARIA";
					mensajesErrores.Mensaje = "Se debe enviar distribucion";
				
					listaMensajesErrores.add(mensajesErrores);
					objResultadoOperacion.Mensajes = listaMensajesErrores;
					listaResultadoOperacion.add(objResultadoOperacion);
					ErroresContador++;
					
					out.P_OUT_ERRORESCONTADOR = ErroresContador;
					out.P_OUT_RESULTADO.addAll(listaResultadoOperacion);
					
					return out;
				}
				
				HashMap<String,Object> entradaSelectDatosAPV = new HashMap<String,Object>();
				entradaSelectDatosAPV.put("p_RegimenTriburario", inEntradaRecaudacion.RegimenTributario);
				entradaSelectDatosAPV.put("p_Producto", inEntradaRecaudacion.Producto);
				entradaSelectDatosAPV.put("p_IdPlan", inEntradaRecaudacion.IdPlan);
				List<SelectDatosAPV> SelectDatosAPV = sql.query(con, SQL_SELECT_DATOS_APV, entradaSelectDatosAPV, SelectDatosAPV.class);
				
				if (!SelectDatosAPV.isEmpty()) {
					HashMap<String, Object> entradaSelectIdPortafolio = new HashMap<String, Object>();
					entradaSelectIdPortafolio.put("p_IdCliente", inEntradaRecaudacion.IdCliente);
					entradaSelectIdPortafolio.put("p_IdMatrizAgrupadorAPV", SelectDatosAPV.get(0).IdMatrizAgrupadorAPV);
					entradaSelectIdPortafolio.put("p_IdRegimenTributario", SelectDatosAPV.get(0).IdRegimenTributario);
					List<SelectIdPortafolio> SelectIdPortafolio = sql.query(con, SQL_SELECT_ID_PORTAFOLIO,entradaSelectIdPortafolio, SelectIdPortafolio.class);

					Long IdPortafolioDefinitivo = 0L;

					// + Ingresa un SAAPV Instancia la clase principal
					CrearContratoSAAPV objInCrearContratoSAAPV = new CrearContratoSAAPV();

					// Se crea la lista para cargarla y luego pasarla al proceso para crear un contrato
					List<Contrato> listaInContrato = new ArrayList<Contrato>();

					// Instancia la entrada del contrato
					CrearContratoSAAPV_Entrada objInEntradaContratoSAAPV = new CrearContratoSAAPV_Entrada();

					// Ingresa los primeros parametros
					objInEntradaContratoSAAPV.p_IdEmpresa = entrada.p_IdEmpresa;
					objInEntradaContratoSAAPV.p_IdUsuario = entrada.p_IdUsuario;
					objInEntradaContratoSAAPV.p_ClaveUsuario = entrada.p_ClaveUsuario;

					// Instancia la lista entrada del contrato
					Contrato objInContratoSAAPV = new Contrato();

					objInContratoSAAPV.IdCliente = inEntradaRecaudacion.IdCliente;
					objInContratoSAAPV.Descripcion = inEntradaRecaudacion.Observacion;
					objInContratoSAAPV.Rut = inEntradaRecaudacion.DocumentoIdentidad;
					objInContratoSAAPV.IdPlan = inEntradaRecaudacion.IdPlan;
					objInContratoSAAPV.Pensionado = inEntradaRecaudacion.Pensionado;
					objInContratoSAAPV.Trabajador = "DEP".equalsIgnoreCase(inEntradaRecaudacion.Trabajador) ? "DEPENDIENTE" : ("IND".equalsIgnoreCase(inEntradaRecaudacion.Trabajador) ? "INDEPENDIENTE" : inEntradaRecaudacion.Trabajador);
					objInContratoSAAPV.FolioContratoSAAPV = inEntradaRecaudacion.Folio;
					objInContratoSAAPV.NombrePlan = SelectDatosAPV.get(0).NombrePlanTipoInversion;
					objInContratoSAAPV.CodigoAgrupador = SelectDatosAPV.get(0).CodigoAgrupador;
					objInContratoSAAPV.IdMatrizAgrupador = SelectDatosAPV.get(0).IdMatrizAgrupadorAPV;
					objInContratoSAAPV.IdPropositoPortafolio = "PROPOSITOAPV";
					objInContratoSAAPV.Canal = inEntradaRecaudacion.Canal;
					objInContratoSAAPV.EsAporteCuentaInversion = inEntradaRecaudacion.p_DistribucionContrato.isEmpty() ? "S" : "N";

					if (!SelectIdPortafolio.isEmpty()) {
						IdPortafolioDefinitivo = SelectIdPortafolio.get(0).IdPortafolio;
						objInContratoSAAPV.IdCliente = inEntradaRecaudacion.IdCliente;
						objInContratoSAAPV.Folio = "0";
						objInContratoSAAPV.IdContratoSAAPV = 0L;
						objInContratoSAAPV.CodigoTipoSolicitud = "AUTORIZACION";
					} else {
						// portafolio para crear
						HashMap<String, Object> entradaSelectPrimerIdPortafolio = new HashMap<String, Object>();
						entradaSelectPrimerIdPortafolio.put("p_IdCliente", inEntradaRecaudacion.IdCliente);
						List<SelectIdPortafolio> SelectPrimerIdPortafolio = sql.query(con,SQL_SELECT_PRIMER_ID_PORTAFOLIO, entradaSelectPrimerIdPortafolio,SelectIdPortafolio.class);
						IdPortafolioDefinitivo = SelectPrimerIdPortafolio.get(0).IdPortafolio;
						objInContratoSAAPV.CodigoTipoSolicitud = "INGRESO";
					}
					
					out = validaClienteBloqueado(con, IdInterno, IdPortafolioDefinitivo, entrada.p_IdEmpresa);
					if(out.P_OUT_ERRORESCONTADOR > 0) {
						if (inEntradaRecaudacion.Simula.trim().equals("S")) {
							// Se crea la lista para cargarla y luego pasarla al proceso
							List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();

							// Instancia los mensajes de errores
							MensajesErrores mensajesErrores = new MensajesErrores();

							// Instancia objeto de salida
							ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();

							objResultadoOperacion.Origen = "DIS";
							objResultadoOperacion.Identificador = inEntradaRecaudacion.IdCliente.toString();

							mensajesErrores.IdInterno = 0L;
							mensajesErrores.TipoMensaje = "OK";
							mensajesErrores.Codigo = "SIMULACION_EXITOSA";
							if (out.P_OUT_ERRORESCONTADOR > 0) {
								mensajesErrores.Mensaje = "SIMULACION CON OBSERVACIONES";
							} else {
								mensajesErrores.Mensaje = "SIMULACION SE HA EJECUTADO EXITOSAMENTE";
							}
							listaMensajesErrores.add(mensajesErrores);
							objResultadoOperacion.Mensajes = listaMensajesErrores;
							listaResultadoOperacion.add(objResultadoOperacion);

						}
						out.P_OUT_RESULTADO.addAll(listaResultadoOperacion);
						return out;
					}
										
					HashMap<String, Object> entradaSelectDatosPersona = new HashMap<String, Object>();
					entradaSelectDatosPersona.put("p_IdPortafolio", IdPortafolioDefinitivo);
					entradaSelectDatosPersona.put("p_IdCliente", inEntradaRecaudacion.IdCliente);
					List<SelectDatosPersona> SelectDatosPersona = sql.query(con, SQL_SELECT_DATOS_PERSONA,entradaSelectDatosPersona, SelectDatosPersona.class);

					if (!SelectDatosPersona.isEmpty()) {
						objInContratoSAAPV.IdPersona = SelectDatosPersona.get(0).IdPersona;
						objInContratoSAAPV.Nombres = SelectDatosPersona.get(0).Nombres;
						objInContratoSAAPV.ApellidoPaterno = SelectDatosPersona.get(0).ApellidoPaterno;
						objInContratoSAAPV.ApellidoMaterno = SelectDatosPersona.get(0).ApellidoMaterno;
						objInContratoSAAPV.FechaNacimiento = SelectDatosPersona.get(0).FechaNacimiento;
						objInContratoSAAPV.Direccion = SelectDatosPersona.get(0).Direccion;
						objInContratoSAAPV.NumeroDireccion = SelectDatosPersona.get(0).NumeroDireccion;
						objInContratoSAAPV.TelefonoMovil = SelectDatosPersona.get(0).Telefono;
						objInContratoSAAPV.IdPais = SelectDatosPersona.get(0).IdPais;
						objInContratoSAAPV.Pais = SelectDatosPersona.get(0).Pais;
						objInContratoSAAPV.IdRegion = SelectDatosPersona.get(0).IdRegion;
						objInContratoSAAPV.Region = SelectDatosPersona.get(0).Region;
						objInContratoSAAPV.IdComuna = SelectDatosPersona.get(0).IdComuna;
						objInContratoSAAPV.Comuna = SelectDatosPersona.get(0).Comuna;
						objInContratoSAAPV.IdCiudad = SelectDatosPersona.get(0).IdCiudad;
						objInContratoSAAPV.Ciudad = SelectDatosPersona.get(0).Ciudad;
						objInContratoSAAPV.IdEstadoCivil = SelectDatosPersona.get(0).EstadoCivil;
						objInContratoSAAPV.IdGenero = SelectDatosPersona.get(0).Genero;
						objInContratoSAAPV.Email = SelectDatosPersona.get(0).DireccionElectronica;
						objInContratoSAAPV.IdCategoriaInversionista = SelectDatosPersona.get(0).IdCategoriaInversionista;
						objInContratoSAAPV.IdEmpleador = SelectDatosPersona.get(0).IdEjecutivo;
						objInContratoSAAPV.IdPerfilComercial = SelectDatosPersona.get(0).IdPerfilComercial;
					}

					// Si la entrada distribucion viene con info
					if (inEntradaRecaudacion.p_DistribucionContrato.size() > 0) {
						// Se crea la lista para cargarla y luego pasarla al proceso
						List<Distribucion> listaInDistribucion;
						listaInDistribucion = new ArrayList<Distribucion>();

						// Por cada Rescate llamo al servicio de crear solicitudes FFMM
						for (InDistribucionContrato inDistribucionContrato : inEntradaRecaudacion.p_DistribucionContrato) {
							// Instancia el detalle del rescate
							Distribucion objInDistribucionContratoSAAPV = new Distribucion();

							objInDistribucionContratoSAAPV.FolioContratoSAAPV = inEntradaRecaudacion.Folio;
							objInDistribucionContratoSAAPV.IdSerieFondo = inDistribucionContrato.IdSerieFondo;
							objInDistribucionContratoSAAPV.MontoParcial = inDistribucionContrato.Monto;
							objInDistribucionContratoSAAPV.PorcentajeParcial = inDistribucionContrato.Porcentaje;

							// Se llena la lista
							listaInDistribucion.add(objInDistribucionContratoSAAPV);
						}
						// Fin for distribucion de contrato

						// Se pasa la lista al proceso
						objInContratoSAAPV.Distribucion = listaInDistribucion;
					}
					// Fin si hay distribucion

					// Se crea la lista regimen para cargarla y luego pasarla al proceso
					List<Regimen> listaInRegimen = new ArrayList<Regimen>();

					Long IdRecaudacion = 0L;
					Long IdContratoSAAPV = 0L; // OJO debo rescatar este Id para pasarlo

					// Instancia el detalle del rescate
					Regimen objInRegimenContratoSAAPV = new Regimen();

					SimpleDateFormat parseador = new SimpleDateFormat("yyyy-MM-dd");
					SimpleDateFormat formateador = new SimpleDateFormat("yyyy-MM");

					Date date = parseador.parse(inEntradaRecaudacion.FechaDeposito);

					objInRegimenContratoSAAPV.IdPortafolio = 0L;
					objInRegimenContratoSAAPV.IdPropositoPortafolio = "PROPOSITOAPV";
					objInRegimenContratoSAAPV.IdMatrizAgrupador = SelectDatosAPV.get(0).IdMatrizAgrupadorAPV;
					objInRegimenContratoSAAPV.CodigoAgrupador = SelectDatosAPV.get(0).CodigoAgrupador;
					objInRegimenContratoSAAPV.OrigenAhorro = "APV"; // OJO
					objInRegimenContratoSAAPV.Pago = "UNI"; // Unico
					objInRegimenContratoSAAPV.TipoRecaudacion = "DIRECTA"; // OJO
					objInRegimenContratoSAAPV.Producto = inEntradaRecaudacion.Producto;
					objInRegimenContratoSAAPV.IdRegimen = SelectDatosAPV.get(0).IdRegimenTributario;
					objInRegimenContratoSAAPV.Regimen = SelectDatosAPV.get(0).Codigo; // inEntradaRecaudacion.RegimenTriburario;
					objInRegimenContratoSAAPV.Cuenta = "";
					objInRegimenContratoSAAPV.MontoTotal =  MonedaFormato.formatear(con,inEntradaRecaudacion.MontoDeposito, MonedaFormato.MONTO, inEntradaRecaudacion.Moneda).valor;
					objInRegimenContratoSAAPV.Moneda = inEntradaRecaudacion.Moneda;
					objInRegimenContratoSAAPV.MesInicio = formateador.format(date);
					objInRegimenContratoSAAPV.MesTermino = formateador.format(date);

					// Se llena la lista
					listaInRegimen.add(objInRegimenContratoSAAPV);

					// Se pasa la lista regimen al proceso
					objInContratoSAAPV.Regimen = listaInRegimen;

					// Se llena la lista de los contratos
					listaInContrato.add(objInContratoSAAPV);
					// Se le pasa por parametro la lista recien agregada
					objInEntradaContratoSAAPV.p_Contrato = listaInContrato;

					// Llamar al proceso de creacion de contrato
					CrearContratoSAAPV_Salida objSalidaContratoSAAPV = objInCrearContratoSAAPV.logica(con,objInEntradaContratoSAAPV);

					if (objSalidaContratoSAAPV.P_OUT_ERRORESCONTADOR > 0) {
						for (CrearContratoSAAPV.MensajesErrores errorDetalle : objSalidaContratoSAAPV.p_Out_EntidadContrato.Mensajes) {
							List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
							MensajesErrores mensajesErrores = new MensajesErrores();
							ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();

							objResultadoOperacion.Origen = "APV";
							objResultadoOperacion.Identificador = objSalidaContratoSAAPV.p_Out_EntidadContrato.FolioContrato;

							mensajesErrores.IdInterno = errorDetalle.IdInterno;
							mensajesErrores.TipoMensaje = errorDetalle.TipoMensaje;
							mensajesErrores.Codigo = errorDetalle.Codigo;
							mensajesErrores.Mensaje = errorDetalle.Mensaje;

							listaMensajesErrores.add(mensajesErrores);
							objResultadoOperacion.Mensajes = listaMensajesErrores;
							listaResultadoOperacion.add(objResultadoOperacion);
							ErroresContador++;
						}
						out.P_OUT_ERRORESCONTADOR = ErroresContador;
						out.P_OUT_RESULTADO = listaResultadoOperacion;
						return out;
					} else {
						IdContratoSAAPV = objSalidaContratoSAAPV.p_Out_EntidadContrato.IdContratoSAAPV;
						String FolioContrato = objSalidaContratoSAAPV.p_Out_EntidadContrato.FolioContrato;
						Long IdPortafolioAPV = !SelectIdPortafolio.isEmpty() ? SelectIdPortafolio.get(0).IdPortafolio : objSalidaContratoSAAPV.P_REF_ID_PORTAFOLIO;
						// pinta mensajes del contrato
						for (CrearContratoSAAPV.MensajesErrores errorDetalle : objSalidaContratoSAAPV.p_Out_EntidadContrato.Mensajes) {
							List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
							MensajesErrores mensajesErrores = new MensajesErrores();
							ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();

							objResultadoOperacion.Origen = "APV";
							objResultadoOperacion.Identificador = objSalidaContratoSAAPV.p_Out_EntidadContrato.FolioContrato;

							mensajesErrores.IdInterno = errorDetalle.IdInterno;
							mensajesErrores.TipoMensaje = errorDetalle.TipoMensaje;
							mensajesErrores.Codigo = errorDetalle.Codigo;
							mensajesErrores.Mensaje = errorDetalle.Mensaje;

							listaMensajesErrores.add(mensajesErrores);
							objResultadoOperacion.Mensajes = listaMensajesErrores;
							listaResultadoOperacion.add(objResultadoOperacion);
						}
						
						{
							List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
							MensajesErrores mensajesErrores = new MensajesErrores();
							ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();

							objResultadoOperacion.Origen = "APV";
							objResultadoOperacion.Identificador = IdContratoSAAPV.toString();

							mensajesErrores.IdInterno = 0L;
							mensajesErrores.TipoMensaje = "OK";
							mensajesErrores.Codigo = "CREACION_CONTRATO_OK_ID_CONTRATO_SAAPV";
							mensajesErrores.Mensaje ="Creacion de contrato exitosa, id contrato saapv: " + IdContratoSAAPV.toString();

							listaMensajesErrores.add(mensajesErrores);
							objResultadoOperacion.Mensajes = listaMensajesErrores;
							listaResultadoOperacion.add(objResultadoOperacion);
					    }
						
						if("S".equals(inEntradaRecaudacion.GenerarAporteSAAPVsinAbonoCIN) && inEntradaRecaudacion.p_DistribucionContrato.size() > 0 ) {
							CrearSolicitudAporteRescateNormal crearSolicitudAporteRescateNormal = new CrearSolicitudAporteRescateNormal();
							EntradaCrearSolicitudAporteRescateNormal entradaCrearSolicitudAporteRescateNormal = new EntradaCrearSolicitudAporteRescateNormal();
							
							entradaCrearSolicitudAporteRescateNormal.p_IdEmpresa = entrada.p_IdEmpresa;
							entradaCrearSolicitudAporteRescateNormal.p_IdUsuario = entrada.p_IdUsuario;
							entradaCrearSolicitudAporteRescateNormal.p_IdModulo = _c.ModuloFondoMutuo;
							entradaCrearSolicitudAporteRescateNormal.p_NumeroDividendo = 0L;

							entradaCrearSolicitudAporteRescateNormal.p_UsaReglasNegocio = _c.SI;
							entradaCrearSolicitudAporteRescateNormal.p_IdTipoCanalIngreso = inEntradaRecaudacion.Canal;
							entradaCrearSolicitudAporteRescateNormal.p_SimularOperacion = inEntradaRecaudacion.Simula;
							entradaCrearSolicitudAporteRescateNormal.p_PermiteOperarProspectos = _c.SI;
							entradaCrearSolicitudAporteRescateNormal.p_IdOrigenProceso = "";
							entradaCrearSolicitudAporteRescateNormal = obtenerEntradaSolicitudesAportesFFMM(con,entradaCrearSolicitudAporteRescateNormal, inEntradaRecaudacion, entrada.p_IdEmpresa, FolioContrato,IdPortafolioAPV, IdContratoSAAPV);
							
							SalidaCrearSolicitudAporteRescateNormal salidaCrearSolicitudAporteRescateNormal = crearSolicitudAporteRescateNormal.logica(con, entradaCrearSolicitudAporteRescateNormal);
							
							if(salidaCrearSolicitudAporteRescateNormal.P_OUT_ERRORESCONTADOR > 0) {
								for(MensajeSalida mensajeError : salidaCrearSolicitudAporteRescateNormal.P_OUT_MENSAJES){
									List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
									MensajesErrores mensajesErrores = new MensajesErrores();
									ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();
									
									objResultadoOperacion.Origen = "DIS";
									objResultadoOperacion.Identificador = IdContratoSAAPV.toString();;
									
									mensajesErrores.IdInterno = mensajeError.id;
									mensajesErrores.TipoMensaje = mensajeError.tipo;
									mensajesErrores.Codigo = mensajeError.codigo;
									mensajesErrores.Mensaje = mensajeError.descripcion;
									
									listaMensajesErrores.add(mensajesErrores);
									objResultadoOperacion.Mensajes = listaMensajesErrores;
									listaResultadoOperacion.add(objResultadoOperacion);
									ErroresContador++;
								}
								
								out.P_OUT_RESULTADO = listaResultadoOperacion;
							} else {
								for(ResultadoSolicitud mensajesOK : salidaCrearSolicitudAporteRescateNormal.P_OUT_RESULTADO){
									List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
									MensajesErrores mensajesErrores = new MensajesErrores();
									ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();
									
									objResultadoOperacion.Origen = "DIS";
									objResultadoOperacion.Identificador = mensajesOK.FolioContrato.toString();
									
									mensajesErrores.IdInterno = mensajesOK.IdSolicitudAporteRescate;
									mensajesErrores.TipoMensaje = MensajeSalida.MENSAJE_OK;
									mensajesErrores.Codigo = "CREACION_CONTRATO_OK_FOLIO_CONTRATO";
									mensajesErrores.Mensaje = "Creacion de contrato exitosa, folio contrato: " + mensajesOK.FolioContrato.toString();
									
									listaMensajesErrores.add(mensajesErrores);
									objResultadoOperacion.Mensajes = listaMensajesErrores;
									listaResultadoOperacion.add(objResultadoOperacion);
								}
							}
						}else {
							idBanco = inEntradaRecaudacion.IdBanco;
							cuentaCorriente = inEntradaRecaudacion.CuentaCorriente;
							// se rescata idbanco y cuenta correinte en caso que no
							// declaren
							if (idBanco == null || cuentaCorriente == null) {
								HashMap<String, Object> ParamsCuentaBanco = new HashMap<String, Object>();
								ParamsCuentaBanco.put("IdCuentaBanco", inEntradaRecaudacion.IdCuentaBanco);
								List<CuentaBanco> _consultaBanco = sql.query(con, SQL_CONSULTA_BANCO, ParamsCuentaBanco,CuentaBanco.class);

								// si no trae el banco
								if (_consultaBanco.size() == 0) {
									// proceso
									List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
									ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();

									MensajesErrores mensajesErrores = new MensajesErrores();
									mensajesErrores.IdInterno = IdInterno;
									mensajesErrores.TipoMensaje = MensajeSalida.MENSAJE_ERROR;
									mensajesErrores.Codigo = "UI_BANCO_CORREDORA_Y_CUENTA_NO_VALIDOS";
									mensajesErrores.Mensaje = "UI_BANCO_CORREDORA_Y_CUENTA_NO_VALIDOS";

									objResultadoOperacion.Origen = "FFMM";
									objResultadoOperacion.Identificador = IdInterno.toString();

									listaMensajesErrores.add(mensajesErrores);
									objResultadoOperacion.Mensajes = listaMensajesErrores;
									listaResultadoOperacion.clear();
									listaResultadoOperacion.add(objResultadoOperacion);
									ErroresContador++;

									out.P_OUT_ERRORESCONTADOR = ErroresContador;
									out.P_OUT_RESULTADO = listaResultadoOperacion;
									return out;
								} else {
									idBanco = _consultaBanco.get(0).IdBanco;
									cuentaCorriente = _consultaBanco.get(0).NumeroCuentaBancaria;
								}
							}
							
							//Conversion de UF a Peso	
							if(inEntradaRecaudacion.Moneda.equalsIgnoreCase(_c.UF)){
								CalcularParidad calcularParidad = new CalcularParidad();
								CalcularParidadEntrada calcularParidadEntrada = new CalcularParidadEntrada();
								calcularParidadEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
								calcularParidadEntrada.p_IdTrabajo = 1L;
								CalculoEntrada calculoEntradaUF = new CalculoEntrada();
								calculoEntradaUF.IdTrabajo = 1L;
								calculoEntradaUF.IdInterno = inEntradaRecaudacion.IdInterno;
								calculoEntradaUF.Fecha = FechaSistema;
								calculoEntradaUF.IdMonedaOrigen = _c.UF;
								calculoEntradaUF.IdMonedaDestino = _c.Pesos;
								calculoEntradaUF.ValorCambioLocalModificado = BigDecimal.ZERO;
								calculoEntradaUF.MontoOrigen =  inEntradaRecaudacion.MontoDeposito;
								calculoEntradaUF.IdUso = "";
								calcularParidadEntrada.p_CalculosEntrada.add(calculoEntradaUF);
								
								CalcularParidadSalida calculoSalida = calcularParidad.logica(con, calcularParidadEntrada);
								HashMap<Long,Object> hashMontoMonedaPago = Utils.List2Hash(calculoSalida.p_out_CalculosSalida, "IdInterno");
								CalculoSalida montoPesos = (CalculoSalida) hashMontoMonedaPago.get(inEntradaRecaudacion.IdInterno);
								inEntradaRecaudacion.MontoDeposito = MonedaFormato.formatear(con, montoPesos.MontoDestino, MonedaFormato.MONTO, _c.Pesos).valor;
							}
							

							// Obtiene el Id Contrato SAAPV

							// + Ingresa un Deposito (usar de ejemplo el de
							// traspaso)
							ParametrosSalidaServicio salida = new ParametrosSalidaServicio();
							List<Recaudacion> lstParamsRecaudacion = new ArrayList<Recaudacion>();
							List<Cliente> lstParamsRecaudacionCliente = new ArrayList<Cliente>();

							Recaudacion recaudacionEntrada = new Recaudacion();
							recaudacionEntrada.IdRecaudacion = 0L;
							recaudacionEntrada.IdEmpresa = entrada.p_IdEmpresa;
							recaudacionEntrada.FolioCartola = 0L;
							recaudacionEntrada.LineaCartola = 0L;
							recaudacionEntrada.Sucursal = "";
							recaudacionEntrada.FechaDeposito = inEntradaRecaudacion.FechaDeposito;
							recaudacionEntrada.CuentaCorriente = cuentaCorriente;
							recaudacionEntrada.MontoDeposito = MonedaFormato.formatear(con,inEntradaRecaudacion.MontoDeposito, MonedaFormato.MONTO,_c.Pesos).valor;
							recaudacionEntrada.ConceptoRecaudacion = inEntradaRecaudacion.ConceptoRecaudacion;
							recaudacionEntrada.Origen = inEntradaRecaudacion.Origen;
							recaudacionEntrada.Observacion = "";
							recaudacionEntrada.NumeroDocumento = IdContratoSAAPV;
							recaudacionEntrada.IdTipoDeposito = inEntradaRecaudacion.IdTipoDeposito == "" ? "TRA" : inEntradaRecaudacion.IdTipoDeposito; // inEntradaRecaudacion.IdTipoDeposito;
							recaudacionEntrada.IdBanco = idBanco;
							recaudacionEntrada.IdRegimenTributario = SelectDatosAPV.get(0).IdRegimenTributario; // inEntradaRecaudacion.RegimenTriburario;
							recaudacionEntrada.IdCarga = 0L;
							recaudacionEntrada.IdEstado = "ING";
							lstParamsRecaudacion.add(recaudacionEntrada);

							CrearRecaudacionDeposito.ParametrosEntradaServicio depositoRecaudacion = new CrearRecaudacionDeposito.ParametrosEntradaServicio();
							depositoRecaudacion.p_IdEmpresa = entrada.p_IdEmpresa;
							depositoRecaudacion.p_IdUsuario = entrada.p_IdUsuario;
							depositoRecaudacion.p_IdModulo = "FMM";
							depositoRecaudacion.p_Tipo = "I";
							depositoRecaudacion.p_Origen = inEntradaRecaudacion.Origen;
							depositoRecaudacion.p_Parametros = lstParamsRecaudacion;

							CrearRecaudacionDeposito depositoRecaudacionLogica = new CrearRecaudacionDeposito();
							CrearRecaudacionDeposito.ParametrosSalidaServicio SalidaDeposito = depositoRecaudacionLogica.logica(con, depositoRecaudacion);							

							if (SalidaDeposito.P_OUT_ERRORESCONTADOR > 0) {
								// Si viene algun Error limpiamos mensajes para
								// mostrar solo los errores
								listaResultadoOperacion.clear();
								for (MensajeSalida errorDetalle : SalidaDeposito.P_OUT_MENSAJES) {
									List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
									MensajesErrores mensajesErrores = new MensajesErrores();
									ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();
									objResultadoOperacion.Origen = "REC";
									objResultadoOperacion.Identificador = "0";

									mensajesErrores.IdInterno = errorDetalle.id;
									mensajesErrores.TipoMensaje = errorDetalle.tipo;
									mensajesErrores.Codigo = errorDetalle.codigo;
									mensajesErrores.Mensaje = errorDetalle.descripcion;

									listaMensajesErrores.add(mensajesErrores);
									objResultadoOperacion.Mensajes = listaMensajesErrores;
									listaResultadoOperacion.add(objResultadoOperacion);
									ErroresContador++;
								}
								out.P_OUT_ERRORESCONTADOR = ErroresContador;
								out.P_OUT_RESULTADO = listaResultadoOperacion;
								return out;
							} else {
								// pinta mensajes de la recaudacion
								for (MensajeSalida errorDetalle : SalidaDeposito.P_OUT_MENSAJES) {
									List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
									MensajesErrores mensajesErrores = new MensajesErrores();
									ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();

									objResultadoOperacion.Origen = "REC";
									objResultadoOperacion.Identificador = "0";

									mensajesErrores.IdInterno = errorDetalle.id;
									mensajesErrores.TipoMensaje = errorDetalle.tipo;
									mensajesErrores.Codigo = errorDetalle.codigo;
									mensajesErrores.Mensaje = errorDetalle.descripcion;

									listaMensajesErrores.add(mensajesErrores);
									objResultadoOperacion.Mensajes = listaMensajesErrores;
									listaResultadoOperacion.add(objResultadoOperacion);
								}

								// + Llena la lista para ingresar una reaudacion
								// cliente (usar de ejemplo el de traspaso)
								if (SalidaDeposito.P_OUT_IDREC > 0) {
									IdRecaudacion = SalidaDeposito.P_OUT_IDREC;

									Resultado resultadoServicios = new Resultado();
									resultadoServicios.CodigoResultado = "REC";
									resultadoServicios.NombreResultado = "Recaudacion";
									resultadoServicios.IdResultado = IdRecaudacion;
									salida.P_OUT_IDRESULTADO.add(resultadoServicios);

									Cliente recaudacionCliente = new Cliente();
									recaudacionCliente.IdCliente = inEntradaRecaudacion.IdCliente;
									recaudacionCliente.IdRecaudacionCliente = 0L;
									recaudacionCliente.IdRecaudacion = IdRecaudacion;
									recaudacionCliente.IdRecaudacionEmpleador = 0L;
									recaudacionCliente.IdEmpresa = entrada.p_IdEmpresa;
									recaudacionCliente.FechaIngreso = inEntradaRecaudacion.FechaDeposito;
									recaudacionCliente.TipoAfiliado = inEntradaRecaudacion.Trabajador;
									recaudacionCliente.IdRegimenTriburario = SelectDatosAPV.get(0).Codigo; // inEntradaRecaudacion.RegimenTriburario;
									recaudacionCliente.MontoDepositoConvenido = BigDecimal.ZERO;
									recaudacionCliente.MontoDepositoAPV =  MonedaFormato.formatear(con,inEntradaRecaudacion.MontoDeposito, MonedaFormato.MONTO,_c.Pesos).valor;
									recaudacionCliente.Origen = inEntradaRecaudacion.Origen;
									recaudacionCliente.IdEstado = "ING";
									lstParamsRecaudacionCliente.add(recaudacionCliente);

									// Llama a ingresar un deposito
									CrearRecaudacionCliente.ParametrosEntradaServicio entradaRecaudacionCliente = new CrearRecaudacionCliente.ParametrosEntradaServicio();
									entradaRecaudacionCliente.p_IdEmpresa = entrada.p_IdEmpresa;
									entradaRecaudacionCliente.p_IdUsuario = entrada.p_IdUsuario;
									entradaRecaudacionCliente.p_IdModulo = "FMM";
									entradaRecaudacionCliente.p_Tipo = "";
									entradaRecaudacionCliente.p_Origen = inEntradaRecaudacion.Origen;
									entradaRecaudacionCliente.p_Parametros = lstParamsRecaudacionCliente;

									CrearRecaudacionCliente RecaudacionClienteLogica = new CrearRecaudacionCliente();
									CrearRecaudacionCliente.ParametrosSalidaServicio SalidaRecaudacionCliente = RecaudacionClienteLogica.logica(con, entradaRecaudacionCliente);

									if (SalidaRecaudacionCliente.P_OUT_ERRORESCONTADOR != null && SalidaRecaudacionCliente.P_OUT_ERRORESCONTADOR > 0) {
										// Se crea la lista para cargarla y luego
										// pasarla al proceso
										List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();

										// Instancia los mensajes de errores
										MensajesErrores mensajesErrores = new MensajesErrores();

										// Instancia objeto de salida
										ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();

										objResultadoOperacion.Origen = "REC";
										objResultadoOperacion.Identificador = "0";

										mensajesErrores.IdInterno = 0L;
										mensajesErrores.TipoMensaje = "ERROR";
										mensajesErrores.Codigo = "ERROR_RECAUDACION_CLIENTE";
										mensajesErrores.Mensaje = "Problema al crear registro Recaudacion";

										listaMensajesErrores.add(mensajesErrores);
										objResultadoOperacion.Mensajes = listaMensajesErrores;
										listaResultadoOperacion.add(objResultadoOperacion);

										ErroresContador++;
									} else {
										// Concilia (usar de ejemplo el de traspaso)Una vez que se envio a Recaudar se debe enviar a Conciliar
										ConciliarDeposito.Recaudacion IdRec = new ConciliarDeposito.Recaudacion();
										IdRec.IdRecaudacion = IdRecaudacion;

										List<ConciliarDeposito.Recaudacion> ParamsConci = new ArrayList<ConciliarDeposito.Recaudacion>();
										ParamsConci.add(IdRec);

										ConciliarDeposito.ParametrosEntradaServicio ObjinCon = new ConciliarDeposito.ParametrosEntradaServicio();
										ObjinCon.p_IdEmpresa = entrada.p_IdEmpresa;
										ObjinCon.p_IdUsuario = entrada.p_IdUsuario;
										ObjinCon.p_IdModulo = "APV";
										ObjinCon.p_Parametros = ParamsConci;

										ConciliarDeposito ConcilioDeposito = new ConciliarDeposito();
										ConciliarDeposito.ParametrosSalidaServicio ConciliarDeposito = ConcilioDeposito
												.logica(con, ObjinCon);

										if (ConciliarDeposito.P_OUT_ERRORESCONTADOR > 0) {
											listaResultadoOperacion.clear();
											for (MensajeSalida fila : ConciliarDeposito.P_OUT_MENSAJES) {
												// Se crea la lista para cargarla y luego pasarla al proceso
												List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();

												// Instancia los mensajes de errores
												MensajesErrores mensajesErrores = new MensajesErrores();

												// Instancia objeto de salida
												ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();

												objResultadoOperacion.Origen = "DIS";
												objResultadoOperacion.Identificador = "0";

												mensajesErrores.IdInterno = fila.id;
												mensajesErrores.TipoMensaje = "ERROR";
												mensajesErrores.Codigo = fila.codigo;
												mensajesErrores.Mensaje = fila.descripcion;

												listaMensajesErrores.add(mensajesErrores);
												objResultadoOperacion.Mensajes = listaMensajesErrores;
												listaResultadoOperacion.add(objResultadoOperacion);
												ErroresContador++;
											}

											out.P_OUT_ERRORESCONTADOR = ErroresContador;
											out.P_OUT_RESULTADO = listaResultadoOperacion;
											return out;
										} else {
											for (MensajeSalida fila : ConciliarDeposito.P_OUT_MENSAJES) {
												if (fila.codigo.equals("CONCILIADO_OK")) {
													
													// Una vez que se envio a Conciliar se debe enviar a Distribuir
													ParametrosRecaudacion pRec = new ParametrosRecaudacion();
													pRec.IdRecaudacion = IdRecaudacion;
													pRec.IdContratoSAAPV = IdContratoSAAPV;
													if (SelectIdPortafolio.isEmpty()) {
														pRec.IdPortafolio = objSalidaContratoSAAPV.P_REF_ID_PORTAFOLIO;
													} else {
														pRec.IdPortafolio = SelectIdPortafolio.get(0).IdPortafolio;
													}

													List<ParametrosRecaudacion> ListRec = new ArrayList<ParametrosRecaudacion>();
													ListRec.add(pRec);

													ProcesarDistribucion.ParametrosEntradaServicio paramDis = new ProcesarDistribucion.ParametrosEntradaServicio();
													paramDis.p_IdEmpresa = entrada.p_IdEmpresa;
													paramDis.p_IdModulo = "FMM";
													paramDis.p_IdUsuario = entrada.p_IdUsuario;
													paramDis.p_Opcion = "RECTRA";
													paramDis.p_Parametros = ListRec;
													paramDis.p_Simula = entrada.p_EntradaRecaudacion.get(0).Simula;
													paramDis.p_Canal = inEntradaRecaudacion.Canal;
													paramDis.p_SoloAporteCuentaInversion = inEntradaRecaudacion.p_DistribucionContrato.size() == 0 ? "S" : "N";
													paramDis.p_SoloAporteSAAPV = inEntradaRecaudacion.GenerarAporteSAAPVsinAbonoCIN;
													paramDis.p_PermiteOperarProspectos = _c.SI;
													paramDis.p_Origen = inEntradaRecaudacion.Origen;

													ProcesarDistribucion ProcesoDistribucion = new ProcesarDistribucion();
													ProcesarDistribucion.ParametrosSalidaServicio ProcesarDistibucion = ProcesoDistribucion.logica(con, paramDis);

													if (ProcesarDistibucion.P_OUT_ERRORESCONTADOR != null && ProcesarDistibucion.P_OUT_ERRORESCONTADOR > 0) {
														// si es un error limpiamos la lista de resultado para guardar solo error
														listaResultadoOperacion.clear();
														for (MensajeSalida row : ProcesarDistibucion.P_OUT_MENSAJES) {
															// Se crea la lista para cargarla y luego pasarla al proceso
															List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();

															// Instancia los mensajes de errores
															MensajesErrores mensajesErrores = new MensajesErrores();

															// Instancia objeto de salida
															ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();

															objResultadoOperacion.Origen = "DIS";
															objResultadoOperacion.Identificador = "0";

															mensajesErrores.IdInterno = row.id;
															mensajesErrores.TipoMensaje = row.tipo;
															mensajesErrores.Codigo = row.codigo;
															mensajesErrores.Mensaje = row.descripcion;
															
															listaMensajesErrores.add(mensajesErrores);
															objResultadoOperacion.Mensajes = listaMensajesErrores;
															listaResultadoOperacion.add(objResultadoOperacion);
															if(MensajeSalida.MENSAJE_ERROR.equals(row.tipo)) {
																ErroresContador++;
															}
														}

														out.P_OUT_ERRORESCONTADOR = ErroresContador;
														out.P_OUT_RESULTADO = listaResultadoOperacion;
														return out;

													} else {
														for (MensajeSalida row : ProcesarDistibucion.P_OUT_MENSAJES) {
															// Se crea la lista para cargarla y luego pasarla al proceso
															List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();

															// Instancia los mensajes de errores
															MensajesErrores mensajesErrores = new MensajesErrores();

															// Instancia objeto de salida
															ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();

															objResultadoOperacion.Origen = "DIS";
															objResultadoOperacion.Identificador = IdRecaudacion.toString();

															mensajesErrores.IdInterno = 0L;
															mensajesErrores.TipoMensaje = "OK";
															mensajesErrores.Codigo = row.codigo;
															mensajesErrores.Mensaje = row.descripcion;

															listaMensajesErrores.add(mensajesErrores);
															objResultadoOperacion.Mensajes = listaMensajesErrores;
															listaResultadoOperacion.add(objResultadoOperacion);
														}
														
														for (ProcesarDistribucion.SalidaSolicitudFFMM solicitudes : ProcesarDistibucion.P_OUT_SOLICITUDES_FFMM) {
															List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
		
															// Instancia los mensajes de errores
															MensajesErrores mensajesErrores = new MensajesErrores();
		
															// Instancia objeto de salida
															ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();
		
															objResultadoOperacion.Origen = "DIS";
															objResultadoOperacion.Identificador = solicitudes.FolioContrato.toString();
		
															mensajesErrores.IdInterno = 0L;
															mensajesErrores.TipoMensaje = "OK";
															mensajesErrores.Codigo = "CREACION_CONTRATO_OK_FOLIO_CONTRATO";
															mensajesErrores.Mensaje = "Creacion de contrato exitosa, folio contrato: " + solicitudes.FolioContrato.toString();
		
															listaMensajesErrores.add(mensajesErrores);
															objResultadoOperacion.Mensajes = listaMensajesErrores;
															listaResultadoOperacion.add(objResultadoOperacion);
														}
														
														// Fin mensaje salida procesar distribucion
													}
												} else {
													// Se crea la lista para cargarla y luego pasarla al proceso
													List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();

													// Instancia los mensajes de errores
													MensajesErrores mensajesErrores = new MensajesErrores();

													// Instancia objeto de salida
													ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();

													objResultadoOperacion.Origen = "DIS";
													objResultadoOperacion.Identificador = IdRecaudacion.toString();

													mensajesErrores.IdInterno = 0L;
													mensajesErrores.TipoMensaje = "OK";
													mensajesErrores.Codigo = fila.codigo;
													mensajesErrores.Mensaje = fila.descripcion;

													listaMensajesErrores.add(mensajesErrores);
													objResultadoOperacion.Mensajes = listaMensajesErrores;
													listaResultadoOperacion.add(objResultadoOperacion);
												}
												// fin si esta conciliado ok++-+-
											}
											// fin for conciliar deposito
										}
									}
								} else {
									// Se crea la lista para cargarla y luego pasarla al proceso
									List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();

									// Instancia los mensajes de errores
									MensajesErrores mensajesErrores = new MensajesErrores();

									// Instancia objeto de salida
									ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();

									objResultadoOperacion.Origen = "REC";
									objResultadoOperacion.Identificador = "0";
									mensajesErrores.IdInterno = 0L;
									mensajesErrores.TipoMensaje = "ERROR";
									mensajesErrores.Codigo = "ERROR_CREAR_RECAUDACION";
									mensajesErrores.Mensaje = "ERROR al crear registro Recaudacion";
									
									listaMensajesErrores.add(mensajesErrores);
									objResultadoOperacion.Mensajes = listaMensajesErrores;
									listaResultadoOperacion.add(objResultadoOperacion);
									ErroresContador++;
								}
							}
							
						}//
						
						// cierra el contrato cuando finaliza todo ok
						List<ContratoUpdateSAAPV> listaUpdate = new ArrayList<ContratoUpdateSAAPV>();
						ContratoUpdateSAAPV contrato = new ContratoUpdateSAAPV();
						contrato.IdContratoSAAPV = IdContratoSAAPV;
						
						contrato.FechaCreacion = Fecha.toDateTime(inEntradaRecaudacion.FechaDeposito);
						contrato.FechaSuscripcion = Fecha.toDateTime(inEntradaRecaudacion.FechaDeposito);

						listaUpdate.add(contrato);
						sql.batch(con, SQL_UPDATE_CONTRATO, listaUpdate, ContratoUpdateSAAPV.class); 

					}
					
					
					// fin else esta todo ok
					// SIMULACION
					if (inEntradaRecaudacion.Simula.trim().equals("S")) {
						// Se crea la lista para cargarla y luego pasarla al proceso
						List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();

						// Instancia los mensajes de errores
						MensajesErrores mensajesErrores = new MensajesErrores();

						// Instancia objeto de salida
						ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();

						objResultadoOperacion.Origen = "DIS";
						objResultadoOperacion.Identificador = IdRecaudacion.toString();

						mensajesErrores.IdInterno = 0L;
						mensajesErrores.TipoMensaje = "OK";
						mensajesErrores.Codigo = "SIMULACION_EXITOSA";
						if (ErroresContador != 0 || out.P_OUT_ERRORESCONTADOR > 0) {
							mensajesErrores.Mensaje = "SIMULACION CON OBSERVACIONES";
						} else {
							mensajesErrores.Mensaje = "SIMULACION SE HA EJECUTADO EXITOSAMENTE";
						}
						listaMensajesErrores.add(mensajesErrores);
						objResultadoOperacion.Mensajes = listaMensajesErrores;
						listaResultadoOperacion.add(objResultadoOperacion);

						con.rollback();
					}
					// Fin si es una operacion a firma
				} else {
					List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
					MensajesErrores mensajesErrores = new MensajesErrores();
					ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();

					objResultadoOperacion.Origen = "";
					objResultadoOperacion.Identificador = "0";

					mensajesErrores.IdInterno = 0L;
					mensajesErrores.TipoMensaje = "ERROR";
					mensajesErrores.Codigo = "DATOS_APV_INCORRECTOS";
					mensajesErrores.Mensaje = "Los Datos de APV ingresados, no son correctos";

					listaMensajesErrores.add(mensajesErrores);
					objResultadoOperacion.Mensajes = listaMensajesErrores;
					listaResultadoOperacion.add(objResultadoOperacion);
					ErroresContador++;
				}
			}
			// Fin for encabezado
		}
		// fin si encabezado viene con datos
		
		// Hace todo o no hace nada
		if (ErroresContador > 0) {
			con.rollback();
		}

		out.P_OUT_ERRORESCONTADOR = ErroresContador;
		out.P_OUT_RESULTADO = listaResultadoOperacion;
		
		log.write("FIN::CrearAporteSAAPV");
		return out;
	}
	
	private EntradaCrearSolicitudAporteRescateNormal obtenerEntradaSolicitudesAportesFFMM(Connection con, EntradaCrearSolicitudAporteRescateNormal entradaCrearSolicitudAporteRescate, InEntradaRecaudacion inEntradaRecaudacion, Long idEmpresa, String folioContrato, Long IdPortafolioAPV, Long IdContratoSAAPV) throws Exception {
		
		String idMonedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		String FechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		List<Long> secuencias = Utils.getSecuencia(con, "BSCL.FondoMutuo.BD.Secuencias::FolioContrato", 1L);
		
		AtomicLong contadorSolicitudes = new AtomicLong(0);
		AtomicLong contadorFormasPago = new AtomicLong(0);
		BigDecimal montoTotalDistribucion = BigDecimal.ZERO;
		
		SQL sql = new SQL();
		HashMap<String,Object> ParamsConsultaDistribucionContrato = new HashMap<String,Object>();
		ParamsConsultaDistribucionContrato.put("IdContrato", IdContratoSAAPV);
		List<DistribucionContrato> _DistribucionContrato = sql.query(con, SQL_SELECT_DISTRIBUCION_CONTRATO, ParamsConsultaDistribucionContrato, DistribucionContrato.class);

		for(InDistribucionContrato distribucion : inEntradaRecaudacion.p_DistribucionContrato){
			BigDecimal montoAporteCalculado = obtenerMontoAporteFFMM(con, distribucion, inEntradaRecaudacion);
			montoTotalDistribucion = montoTotalDistribucion.add(montoAporteCalculado);
			String idMoneda = _DistribucionContrato.stream().filter(dist -> distribucion.IdSerieFondo.equals(dist.Nemotecnico)).findFirst().orElse(new DistribucionContrato()).IdMoneda;

			InSolicitudesAporteRescate solicitud = new InSolicitudesAporteRescate();
			solicitud.IdInterno = contadorSolicitudes.incrementAndGet();
			solicitud.IdCliente = inEntradaRecaudacion.IdCliente;
			solicitud.IdPortafolio = IdPortafolioAPV;
			solicitud.TipoSolicitud = _c.TipoSolicitudAporte;
			solicitud.IdSerieFondo = distribucion.IdSerieFondo;
			solicitud.CalculoPorMontoCuota = _c.CalculoPorMonto;
			solicitud.EsPorMontoLiquido = _c.NO;
			solicitud.NumeroCuotas = BigDecimal.valueOf(0);
			solicitud.IdMoneda = idMoneda != null ? idMoneda : inEntradaRecaudacion.Moneda;
			solicitud.IdMonedaPago = idMonedaSistema;
			solicitud.IdMedioDePago = _c.CuentaInversion;
			solicitud.IdPreferenciaPagoPortafolio = 0L;
			solicitud.Visado = _c.NO;
			solicitud.SolicitudBatch = _c.NO;
			solicitud.OrigenOperacion = _c.OrigenManual;
			solicitud.IdRegimenTributario = inEntradaRecaudacion.RegimenTributario;
			solicitud.Glosa = "CREAR APORTE SAAPV";
			solicitud.IndicadorMLT = _c.NO;
			solicitud.IdSerieFondoDestino = "";
			solicitud.IdEmisorDestino = "";
			solicitud.IdAutorizadoOrigen = 0L;
			solicitud.Porcentaje = BigDecimal.ZERO;
			solicitud.TipoContratoOperacion = _c.SAAPV;
			solicitud.IdOrigenRecaCliente = 0L;
			solicitud.IngresoAutomatico = _c.NO;
			solicitud.FechaOperacion = FechaSistema.toString();
			solicitud.FolioContrato = secuencias.get(0);
			solicitud.Monto = montoAporteCalculado;
			solicitud.EsTraspaso = _c.NO;
			solicitud.Canal = inEntradaRecaudacion.Canal;
			solicitud.FolioContratoOperacion = folioContrato;
			solicitud.ForzarSpot = _c.Dolar.equals(solicitud.IdMoneda) ? _c.SI : _c.NO;
			
			entradaCrearSolicitudAporteRescate.p_SolicitudesAporteRescate.add(solicitud);
		}

		//Se resta diferencia en caso de haber decimales 
		BigDecimal diferencia = inEntradaRecaudacion.MontoDeposito.subtract(montoTotalDistribucion);
		InSolicitudesAporteRescate ultimoAporte = entradaCrearSolicitudAporteRescate.p_SolicitudesAporteRescate.get(entradaCrearSolicitudAporteRescate.p_SolicitudesAporteRescate.size() -1);
		if(ultimoAporte != null && diferencia.compareTo(BigDecimal.ZERO) != 0) {
			ultimoAporte.Monto = ultimoAporte.Monto.add(diferencia,MathContext.DECIMAL128);
			ultimoAporte.Monto = MonedaFormato.formatear(con,ultimoAporte.Monto,MonedaFormato.MONTO,_c.Pesos).valor;
		}
		
		contadorSolicitudes.set(0);
		for(InSolicitudesAporteRescate solicitud : entradaCrearSolicitudAporteRescate.p_SolicitudesAporteRescate) {
			contadorSolicitudes.incrementAndGet();
			if(_c.Dolar.equals(solicitud.IdMoneda)){
				List<InFormaPagoSpot> p_FormaPagoSpot = new ArrayList<InFormaPagoSpot>();
				contadorFormasPago.incrementAndGet();
				
				InFormaPagoSpot FormaPagoSpot = new InFormaPagoSpot();
				FormaPagoSpot.IdInternoPagoOperacion = contadorFormasPago.get();
				FormaPagoSpot.IdComprobantePagoOperacion= 0L;
				FormaPagoSpot.IdTipoMovimiento = "MOVEGR";
				FormaPagoSpot.IdFormaDePago    = "VENSPT_CIN_USD_0";
				FormaPagoSpot.IdOperacion      = 	contadorSolicitudes.get();
				FormaPagoSpot.RecibePagaPortafolio = "P";
				FormaPagoSpot.IdMoneda         =  solicitud.IdMoneda;
				FormaPagoSpot.IdMonedaLiquidacion  =  solicitud.IdMoneda;
				FormaPagoSpot.MontoFormaDePago = BigDecimal.ZERO;
				FormaPagoSpot.AfectarTesoreria =  "N";
				p_FormaPagoSpot.add(FormaPagoSpot);
				
				InFormaPagoSpot FormaPagoSpot2 = new InFormaPagoSpot();
				FormaPagoSpot2.IdInternoPagoOperacion = contadorFormasPago.get();
				FormaPagoSpot2.IdComprobantePagoOperacion= 0L;
				FormaPagoSpot2.IdTipoMovimiento = "MOVING";
				FormaPagoSpot2.IdFormaDePago    = "COMSPT_CIN_CLP_0";
				FormaPagoSpot2.IdOperacion      = 	contadorSolicitudes.get();
				FormaPagoSpot2.RecibePagaPortafolio = "R";
				FormaPagoSpot2.IdMoneda         = "CLP";
				FormaPagoSpot2.IdMonedaLiquidacion =  "CLP";
				FormaPagoSpot2.MontoFormaDePago = solicitud.Monto;
				FormaPagoSpot2.AfectarTesoreria =  "N";
				p_FormaPagoSpot.add(FormaPagoSpot2);
				
				entradaCrearSolicitudAporteRescate.p_FormaPagoSpot.addAll(p_FormaPagoSpot);
			}
		}
		
		if(entradaCrearSolicitudAporteRescate.p_SolicitudesAporteRescate.size()>0){
			List<CalcularMontoNumeroCuota.Serie> lstMontoNumeroCuotaSerie = new ArrayList<CalcularMontoNumeroCuota.Serie>();
			CalcularMontoNumeroCuota servicioCalcularMontoNumeroCuota = new CalcularMontoNumeroCuota();
			EntradaCalcularMontoNumeroCuota entradaCalcularMontoNumeroCuota = new EntradaCalcularMontoNumeroCuota();
			CalcularMontoNumeroCuota.Serie objSerie; 
			
			for(InSolicitudesAporteRescate sol : entradaCrearSolicitudAporteRescate.p_SolicitudesAporteRescate){
				objSerie = new CalcularMontoNumeroCuota.Serie(); 
				objSerie.Fecha = FechaSistema.toString();
				
				objSerie.IdInterno 		= sol.IdInterno;					
				objSerie.IdSerieFondo = sol.IdSerieFondo;
				objSerie.CalculoPorMontoCuota = sol.CalculoPorMontoCuota;
				objSerie.Monto = sol.Monto;
				objSerie.NumeroCuotas = sol.NumeroCuotas;
				objSerie.IdMonedaFondo = sol.IdMoneda;
				
				objSerie.IdMonedaInversion = (_c.Dolar.equals(sol.IdMoneda)) ?  "CLP" : sol.IdMoneda;
				objSerie.TipoSolicitud = sol.TipoSolicitud;
				objSerie.TipoFactorAjuste = _c.UF;
				objSerie.IdEmpresa = idEmpresa;
				lstMontoNumeroCuotaSerie.add(objSerie);
				
			}
			
			if(!lstMontoNumeroCuotaSerie.isEmpty()){
				entradaCalcularMontoNumeroCuota.p_IdEmpresa = idEmpresa;
				entradaCalcularMontoNumeroCuota.p_Series    = lstMontoNumeroCuotaSerie;
				
				log.write("Inicio Calcular Monto Numero Cuota");
				SalidaCalcularMontoNumeroCuota salidaCalculo = servicioCalcularMontoNumeroCuota.logica(con, entradaCalcularMontoNumeroCuota);	
				log.write("Fin Calcular Monto Numero Cuota");
				List<CalcularMontoNumeroCuota.Resultado> resultadoSeriesFFMM = salidaCalculo.P_OUT_RESULTADO;
				HashMap<Long,Object> hashResultadoSeriesFFMM = Utils.List2Hash(resultadoSeriesFFMM, "IdInterno");   
				
				for(InSolicitudesAporteRescate sol : entradaCrearSolicitudAporteRescate.p_SolicitudesAporteRescate) {
					CalcularMontoNumeroCuota.Resultado infoResultadoSeriesFFMM = (CalcularMontoNumeroCuota.Resultado) hashResultadoSeriesFFMM.get(sol.IdInterno);
					sol.NumeroCuotas = infoResultadoSeriesFFMM.NumeroCuotas;

					if (_c.Dolar.equals(sol.IdMoneda)){
						sol.Monto        = infoResultadoSeriesFFMM.Monto == null ? BigDecimal.ZERO: infoResultadoSeriesFFMM.Monto;
						sol.NumeroCuotas = infoResultadoSeriesFFMM.NumeroCuotas;	
					}
				}
				
				//Se repasa monto de forma de pago spot
				for(InFormaPagoSpot forPag : entradaCrearSolicitudAporteRescate.p_FormaPagoSpot){
					CalcularMontoNumeroCuota.Resultado infoResultadoSeriesFFMM = (CalcularMontoNumeroCuota.Resultado) hashResultadoSeriesFFMM.get(forPag.IdOperacion);	
					
					if(forPag.IdTipoMovimiento.equalsIgnoreCase("MOVEGR")){
						forPag.MontoFormaDePago = infoResultadoSeriesFFMM.Monto == null ? BigDecimal.ZERO: infoResultadoSeriesFFMM.Monto;
					}
				}
			}
		}
		
		return entradaCrearSolicitudAporteRescate;
	}

	private BigDecimal obtenerMontoAporteFFMM(Connection con, InDistribucionContrato distribucion, InEntradaRecaudacion inEntradaRecaudacion) throws Exception {		
		BigDecimal montoAporte =distribucion.Monto;
		if(distribucion.Porcentaje != null && distribucion.Monto == null) {
			montoAporte = inEntradaRecaudacion.MontoDeposito.multiply(distribucion.Porcentaje.divide(new BigDecimal("100"), MathContext.DECIMAL128));
			montoAporte = MonedaFormato.formatear(con, montoAporte, MonedaFormato.MONTO, inEntradaRecaudacion.Moneda).valor;
		}
		return montoAporte;
	}

	private SalidaCrearAporteSAAPV validaClienteBloqueado(Connection con, Long idInterno, Long idPortafolio, Long idEmpresa) throws Exception {
		SalidaCrearAporteSAAPV out = new SalidaCrearAporteSAAPV();

		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("IdPortafolio", idPortafolio);
		SQL sql = new SQL();

		ClientePortafolioSolicitudes cliente = sql.query(con, SQL_SELECT_PORTAFOLIO, params, ClientePortafolioSolicitudes.class).stream().findFirst().orElse(null);

		if (cliente != null && !_c.EstadoActivo.equalsIgnoreCase(cliente.IdEstadoCliente) && _c.NO.equals(cliente.EsClienteNuevoProspecto)) {
			List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
			MensajesErrores mensajesErrores = new MensajesErrores();
			ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();
			
			HashMap<String,String> parametros = new HashMap<String,String>();
			parametros.put("DocumentoIdentidad", cliente.DocumentoIdentidad);

			objResultadoOperacion.Origen = "";
			objResultadoOperacion.Identificador = "0";

			mensajesErrores.IdInterno = idInterno;
			mensajesErrores.TipoMensaje = MensajeSalida.MENSAJE_ERROR;
			mensajesErrores.Codigo = "CLIENTE_BLOQUEADO";
			mensajesErrores.Mensaje = Diccionario.getMensaje(con, idEmpresa, mensajesErrores.Codigo, parametros);

			listaMensajesErrores.add(mensajesErrores);
			objResultadoOperacion.Mensajes = listaMensajesErrores;
			out.P_OUT_RESULTADO.add(objResultadoOperacion);
			out.P_OUT_ERRORESCONTADOR++;
		}
		
		if(cliente != null && cliente.BloqueadoPorCumplimiento.equalsIgnoreCase("S")  && _c.NO.equals(cliente.EsClienteNuevoProspecto)){
			List<MensajesErrores> listaMensajesErrores = new ArrayList<MensajesErrores>();
			MensajesErrores mensajesErrores = new MensajesErrores();
			
			HashMap<String,String> parametros = new HashMap<String,String>();
			parametros.put("DocumentoIdentidad", cliente.DocumentoIdentidad);
			parametros.put("NombreCliente",cliente.NombreCompleto);
			
			ResultadoOperacion objResultadoOperacion = new ResultadoOperacion();

			objResultadoOperacion.Origen = "";
			objResultadoOperacion.Identificador = "0";

			mensajesErrores.IdInterno = idInterno;
			mensajesErrores.TipoMensaje = MensajeSalida.MENSAJE_ERROR;
			mensajesErrores.Codigo = "CLIENTE_BLOQUEADO_POR_CUMPLIMIENTO";
			mensajesErrores.Mensaje = Diccionario.getMensaje(con, idEmpresa, mensajesErrores.Codigo, parametros);

			listaMensajesErrores.add(mensajesErrores);
			objResultadoOperacion.Mensajes = listaMensajesErrores;
			out.P_OUT_RESULTADO.add(objResultadoOperacion);
			out.P_OUT_ERRORESCONTADOR++;
		}

		return out;
	}
	
	//-------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE - CALL
	//-------------------------------------------------------------------------------------------
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception
	{
		Gson gson = new Gson();		
		EntradaCrearAporteSAAPV jin = gson.fromJson(in.toString(), EntradaCrearAporteSAAPV.class);
		SalidaCrearAporteSAAPV out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	//-------------------------------------------------
	// METODO PARA PRUEBAS LOCALES 
	//-------------------------------------------------
	public static void main(String[] args) throws Exception {
		System.out.println("== INICIO PRUEBA LOCAL ==");

		try (Connection con = PlataformaUtils.getBOLSAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();

			String jsonUF = "{\"p_IdEmpresa\":\"1\",\"p_IdUsuario\":\"15\",\"p_ClaveUsuario\":\"\",\"p_EntradaRecaudacion\":[{\"Simula\":\"S\",\"FechaDeposito\":\"2020-01-16\",\"IdBanco\":21,\"CuentaCorriente\":\"1316400\",\"MontoDeposito\":2500,\"Moneda\":\"UF\",\"IdTipoDeposito\":\"TRA\",\"DocumentoIdentidad\":\"10003489-1\",\"RegimenTributario\":\"APV-A\",\"Origen\":\"WEB\",\"Producto\":\"APV Fondos Mutuos\",\"IdPlan\":2,\"Pensionado\":\"NO\",\"Trabajador\":\"DEP\",\"Folio\":\"W\",\"Canal\":\"WEB\",\"IdCliente\":58311,\"p_DistribucionContrato\":[{\"Porcentaje\":50,\"IdSerieFondo\":\"00136CCLP\"},{\"Porcentaje\":50,\"IdSerieFondo\":\"00102AUSD\"}]}]}";
//			String jsonPesos = "{\"p_IdEmpresa\":\"1\",\"p_IdUsuario\":\"70699\",\"p_ClaveUsuario\":\"\",\"p_EntradaRecaudacion\":[{\"Simula\":\"S\",\"FechaDeposito\":\"2023-03-16\",\"IdBanco\":21,\"CuentaCorriente\":\"1316400\",\"MontoDeposito\":100000,\"Moneda\":\"CLP\",\"IdTipoDeposito\":\"TRA\",\"DocumentoIdentidad\":\"27065064-3\",\"RegimenTributario\":\"APV-A\",\"Origen\":\"WEB\",\"Producto\":\"APV Acciones\",\"IdPlan\":2,\"Pensionado\":\"NO\",\"Trabajador\":\"DEP\",\"Folio\":\"W\",\"Canal\":\"WEB\",\"IdCliente\":83389,\"GenerarAporteSAAPVsinAbonoCIN\":\"N\",\"p_DistribucionContrato\":[]}]}";
			String jsonUSD = "{\"p_IdEmpresa\":\"1\",\"p_IdUsuario\":\"70699\",\"p_ClaveUsuario\":\"\",\"p_EntradaRecaudacion\":[{\"Simula\":\"S\",\"FechaDeposito\":\"2023-03-16\",\"IdBanco\":21,\"CuentaCorriente\":\"1316400\",\"MontoDeposito\":100001,\"Moneda\":\"CLP\",\"IdTipoDeposito\":\"TRA\",\"DocumentoIdentidad\":\"14135815-4\",\"RegimenTributario\":\"APV-B\",\"Origen\":\"WEB\",\"Producto\":\"APV Acciones\",\"IdPlan\":2,\"Pensionado\":\"NO\",\"Trabajador\":\"DEP\",\"Folio\":\"W\",\"Canal\":\"WEB\",\"IdCliente\":83423,\"GenerarAporteSAAPVsinAbonoCIN\":\"S\",\"p_DistribucionContrato\":[{\"Porcentaje\":50,\"IdSerieFondo\":\"00292AUSD\"},{\"Porcentaje\":50,\"IdSerieFondo\":\"00162AUSD\"}]}]}";
			String jsonPesos = "{\"p_IdEmpresa\":\"1\",\"p_IdUsuario\":\"70699\",\"p_ClaveUsuario\":\"\",\"p_EntradaRecaudacion\":[{\"Simula\":\"N\",\"FechaDeposito\":\"2023-03-16\",\"IdBanco\":21,\"CuentaCorriente\":\"1316400\",\"MontoDeposito\":100001,\"Moneda\":\"CLP\",\"IdTipoDeposito\":\"TRA\",\"DocumentoIdentidad\":\"14135815-4\",\"RegimenTributario\":\"APV-B\",\"Origen\":\"WEB\",\"Producto\":\"APV Acciones\",\"IdPlan\":2,\"Pensionado\":\"NO\",\"Trabajador\":\"DEP\",\"Folio\":\"W\",\"Canal\":\"WEB\",\"IdCliente\":83423,\"GenerarAporteSAAPVsinAbonoCIN\":\"S\",\"p_DistribucionContrato\":[{\"Porcentaje\":50,\"IdSerieFondo\":\"00167CCLP\"},{\"Porcentaje\":50,\"IdSerieFondo\":\"00041CCLP\"}]}]}";
			String QAPesos = "{\"p_IdEmpresa\":\"1\",\"p_IdUsuario\":\"50\",\"p_ClaveUsuario\":\"\",\"p_EntradaRecaudacion\":[{\"Simula\":\"N\",\"FechaDeposito\":\"2023-03-15\",\"IdBanco\":19,\"CuentaCorriente\":\"45362211\",\"MontoDeposito\":100000,\"Moneda\":\"CLP\",\"IdTipoDeposito\":\"TRA\",\"DocumentoIdentidad\":\"19314517-5\",\"RegimenTributario\":\"APV-B\",\"Origen\":\"WEB\",\"Producto\":\"APV Acciones\",\"IdPlan\":2,\"Pensionado\":\"NO\",\"Trabajador\":\"DEP\",\"Folio\":\"W\",\"Canal\":\"WEB\",\"IdCliente\":100833,\"GenerarAporteSAAPVsinAbonoCIN\":\"N\",\"p_DistribucionContrato\":[]}]}";
			
			EntradaCrearAporteSAAPV Objin = gson.fromJson(jsonUSD, EntradaCrearAporteSAAPV.class);
			CrearAporteSAAPV demo = new CrearAporteSAAPV();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());

			try {
				SalidaCrearAporteSAAPV ObjOut = demo.logica(con, Objin);
				con.rollback();
				System.out.println("SALIDA");
				System.out.println(ObjOut.toString());
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		System.out.println("== FIN PRUEBA LOCAL ==");
	}
}