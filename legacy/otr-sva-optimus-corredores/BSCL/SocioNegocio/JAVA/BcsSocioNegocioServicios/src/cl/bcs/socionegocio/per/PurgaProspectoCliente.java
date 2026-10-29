package cl.bcs.socionegocio.per;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.ftp.FileManager.Cabecera;
import cl.bcs.plataforma.ftp.FileManager.Detalle;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.utils.OfuscarDocumentoIdentidad;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class PurgaProspectoCliente implements JavaCall {
	static final Logger logger = LogManager.getLogger(PurgaProspectoCliente.class);
	
	private final String SQL_INSERT_CABECERA_ARCHIVO_SALIDA = loadResourceToString("cl/bcs/socionegocio/per/sql/PurgaProspectoCliente_InsertCabecera.sql");
	private final String SQL_INSERT_DETALLES_ARCHIVO_SALIDA = loadResourceToString("cl/bcs/socionegocio/per/sql/PurgaProspectoCliente_InsertDetalles.sql");
	private final String SQL_SELECT_CABECERA = loadResourceToString("cl/bcs/socionegocio/per/sql/PurgaProspectoCliente_SelectCabeceras.sql");
	private final String SQL_SELECT_DETALLES = loadResourceToString("cl/bcs/socionegocio/per/sql/PurgaProspectoCliente_SelectDetalles.sql");
	private final String SQL_UPDATE_CABECERA = loadResourceToString("cl/bcs/socionegocio/per/sql/PurgaProspectoCliente_UpdateCabecera.sql");
	private final String SQL_UPDATE_DETALLES = loadResourceToString("cl/bcs/socionegocio/per/sql/PurgaProspectoCliente_UpdateDetalles.sql");
	private final String SQL_SELECT_CONFIGURACION_TIPO_BORRADO = loadResourceToString("cl/bcs/socionegocio/per/sql/PurgaProspectoCliente_SelectConfiguracionTipoBorrado.sql");
	private final String SQL_INSERT_LOG_TIPO_BORRADO = loadResourceToString("cl/bcs/socionegocio/per/sql/PurgaProspectoCliente_InsertLogTipoBorrado.sql");
	private final String SQL_INSERT_DETALLE_LOG_TIPO_BORRADO = loadResourceToString("cl/bcs/socionegocio/per/sql/PurgaProspectoCliente_InsertDetalleLogTipoBorrado.sql");
	
	private final String SQL_OBTENER_PERSONA = loadResourceToString("cl/bcs/socionegocio/per/sql/PurgaProspectoCliente_SelectPersona.sql");
	private final String SQL_OBTENER_DOCUMENTO_IDENTIDAD = loadResourceToString("cl/bcs/socionegocio/per/sql/PurgaProspectoCliente_SelectDocumentoIdentidad.sql");
	private final String SQL_OBTENER_DIRECCION_FISICA = loadResourceToString("cl/bcs/socionegocio/per/sql/PurgaProspectoCliente_SelectDireccionFisica.sql");
	private final String SQL_OBTENER_DIRECCION_ELECTRONICA = loadResourceToString("cl/bcs/socionegocio/per/sql/PurgaProspectoCliente_SelectDireccionElectronica.sql");
	private final String SQL_OBTENER_TELEFONO = loadResourceToString("cl/bcs/socionegocio/per/sql/PurgaProspectoCliente_SelectTelefono.sql");
	private final String SQL_OBTENER_CUENTA_BANCARIA = loadResourceToString("cl/bcs/socionegocio/per/sql/PurgaProspectoCliente_SelectCuentaBancaria.sql");
	private final String SQL_OBTENER_TARJETA_BANCARIA = loadResourceToString("cl/bcs/socionegocio/per/sql/PurgaProspectoCliente_SelectTarjetaBancaria.sql");
	private final String SQL_OBTENER_CUENTA_INVERSION = loadResourceToString("cl/bcs/socionegocio/per/sql/PurgaProspectoCliente_SelectCuentaInversion.sql");
	private final String SQL_OBTENER_PORTAFOLIO = loadResourceToString("cl/bcs/socionegocio/per/sql/PurgaProspectoCliente_SelectPortafolio.sql");
	
	//ADICIONALES
	private final String SQL_OBTENER_CARTERA = loadResourceToString("cl/bcs/socionegocio/per/sql/PurgaProspectoCliente_SelectCartera.sql");
	private final String SQL_OBTENER_ORDEN = loadResourceToString("cl/bcs/socionegocio/per/sql/PurgaProspectoCliente_SelectOrden.sql");
	private final String SQL_OBTENER_OPERACION = loadResourceToString("cl/bcs/socionegocio/per/sql/PurgaProspectoCliente_SelectOperacion.sql");
	private final String SQL_OBTENER_INGRESO_EGRESO = loadResourceToString("cl/bcs/socionegocio/per/sql/PurgaProspectoCliente_SelectIngresoEgreso.sql");
	private final String SQL_OBTENER_EVENTO_CAPITAL = loadResourceToString("cl/bcs/socionegocio/per/sql/PurgaProspectoCliente_SelectEventoCapital.sql");
	private final String SQL_OBTENER_COMPROBANTE_TESORERIA = loadResourceToString("cl/bcs/socionegocio/per/sql/PurgaProspectoCliente_SelectComprobanteTesoreria.sql");
	private final String SQL_OBTENER_COMPROBANTE_FACTURACION = loadResourceToString("cl/bcs/socionegocio/per/sql/PurgaProspectoCliente_SelectComprobanteFacturacion.sql");
	private final String SQL_OBTENER_MOVIMIENTO_COMPROBANTE = loadResourceToString("cl/bcs/socionegocio/per/sql/PurgaProspectoCliente_SelectMovimientoComprobante.sql");
	private final String SQL_OBTENER_SOLICITUD_APOR_RES = loadResourceToString("cl/bcs/socionegocio/per/sql/PurgaProspectoCliente_SelectSolicitudAporteRescate.sql");

	private static final String CODIGO_TIPO_ENTRADA = "CARGA_BORRAR_DATOS_PERSONA";
	private static final String CODIGO_TIPO_SALIDA = "SALIDA_BORRAR_DATOS_PERSONA";
	private static final String NOMBRE_ARCHIVO_SALIDA = "RESPUESTA_PURGA_PROS";
	private static final String ORIGEN = "BCS";
	
	private static final String ACCION_UPDATE = "UPDATE";
	private static final String ACCION_DELETE = "DELETE";
	
	private static final Set<String> TABLAS_PRINCIPALES_LOG = new HashSet<>(Arrays.asList(
			"BSCL.SocioNegocio.BD::PER.Persona", 
			"BSCL.SocioNegocio.BD::PER.DocumentoIdentidad",
			"BSCL.SocioNegocio.BD::PER.DireccionFisica", 
			"BSCL.SocioNegocio.BD::PER.DireccionElectronica",
			"BSCL.SocioNegocio.BD::PER.Telefono", 
			"BSCL.SocioNegocio.BD::PER.CuentaBancaria",
			"BSCL.SocioNegocio.BD::PER.TarjetaBancaria"
	));
	
	public static class EntradaPurgaProspectoCliente extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_FechaCarga;
		public String p_IdModulo;
		
		public EntradaPurgaProspectoCliente () {
			this.p_IdEmpresa = 1L;
			this.p_IdUsuario = 0L;
			this.p_IdModulo = "SOC";
		}
	}

	public static class SalidaPurgaProspectoCliente extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public String P_OUT_RUTA_ARCHIVO;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_REGISTROSPROCESADOSCONTADOR;
		
		public SalidaPurgaProspectoCliente () {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_REGISTROSPROCESADOSCONTADOR = 0L;
			this.P_OUT_RUTA_ARCHIVO = "";
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	}
	
	public static class LineaEntrada extends BcsPojo {
		public Long	IdCabecera;
		public Long	IdDetalle;
	    public String IdSolicitud;
	    public String RutTitular;
	    public String TipoBorrado;
	    public String NombreDato;
	    public int CantidadColumnas;
	    public EstadoProceso EstadoProceso;

	    public LineaEntrada(Long idCabecera, Long idDetalle, String linea) {
	    	String[] campos = linea.split(",", -1);
	    	
	    	this.IdCabecera = idCabecera;
	    	this.IdDetalle = idDetalle;
	        this.CantidadColumnas = campos.length;
	        this.IdSolicitud = obtenerCampo(campos, PosicionCampo.ID_SOLICITUD);
	        this.RutTitular = obtenerCampo(campos, PosicionCampo.RUT_TITULAR);
	        this.TipoBorrado = obtenerCampo(campos, PosicionCampo.TIPO_BORRADO);
	        this.NombreDato = obtenerCampo(campos, PosicionCampo.NOMBRE_DATO);
	        this.EstadoProceso = new EstadoProceso(TipoEstado.SIN_ESTADO, TipoMotivo.NO_APLICA, "");
	    }
	    
	    private String obtenerCampo(String[] campos, PosicionCampo posicion) {
	    	int indice = posicion.Indice;
	        return indice < campos.length ? campos[indice].trim() : "";
	    }
	    
	    public boolean estaVacia() {
	        return IdSolicitud.isEmpty() && RutTitular.isEmpty() && TipoBorrado.isEmpty() && NombreDato.isEmpty();
	    }
	    
		public boolean esProcesable() {
			return EstadoProceso != null && EstadoProceso.TipoEstado == TipoEstado.SIN_ESTADO;
		}
	}
	
	public static class LineaSalida extends BcsPojo {
		public String IdSolicitud;
		public String Origen;
		public String FechaRespuesta;
		public int TipoEstado;
		public int TipoMotivo;
		public String Descripcion;
		
		public LineaSalida() {
	        this.Origen = ORIGEN;
	        this.Descripcion = "";
	    }
	}
	
	public static enum TipoEstado {
		SIN_ESTADO(9), RUT_NO_ENCONTRADO(0), ELIMINACION_EXITOSA(1), ERROR_GENERICO(2), ERROR_ELIMINAR(3), NO_ELIMINADO(4);
		public final int Codigo;
		
		TipoEstado(int codigo) {
			this.Codigo = codigo;
		}
	}
	
	public static enum TipoMotivo {
		NO_APLICA(0), CLIENTE_ACTIVO(1), PERSONA_RELACIONADA(2), TIEMPO_MINIMO_MENOR_90_DIAS(3), TIEMPO_MENOR_10_ANIOS(4), PERSONA_FALLECIDA(5),NO_ACTIVO(6);

		public final int Codigo;

		TipoMotivo(int codigo) {
			this.Codigo = codigo;
		}
	}
	

	public static enum PosicionCampo {
		ID_SOLICITUD(0), RUT_TITULAR(1), TIPO_BORRADO(2), NOMBRE_DATO(3);

		public final int Indice;

		PosicionCampo(int indice) {
			this.Indice = indice;
		}
	}

	public enum TipoBorrado {
		PROSPECTO("01"), CONSENTIMIENTO("02"), DEFINITIVA("03"), SUPRESION_DATO("11");

		public final String Codigo;

		TipoBorrado(String codigo) {
			this.Codigo = codigo;
		}
		
		public static boolean contieneCodigo(String codigo) {
			for (TipoBorrado tipo : values()) {
				if (tipo.Codigo.equals(codigo)) {
					return true;
				}
			}
			return false;
		}
	}
	
	public static class EstadoProceso extends BcsPojo {
	    public TipoEstado TipoEstado;
	    public TipoMotivo TipoMotivo; 
	    public String Mensaje;

	    public EstadoProceso(TipoEstado tipoEstado,TipoMotivo tipoMotivo, String mensaje) {
	        this.TipoEstado = tipoEstado;
	        this.TipoMotivo = tipoMotivo;
	        this.Mensaje = mensaje;
	    }
	}
	
	public static class Persona extends BcsPojo {
		public Long IdPersona;
		public Long IdCliente;
		public String RutTitular;
		public String Nombres;
		public String ApellidoPaterno;
		public String ApellidoMaterno;
		public Long IdRangoPatrimonio;
		public String Sexo;
		public Long IdPaisNacimiento;
		public String EstadoCivil;
		public Long IdProfesion;
		public Long IdPaisResidencia;
		public String FechaNacimiento;
		public String FechaDefuncion;
		public String CodigoRangoRenta;
		public Long IdDireccionFisicaPrincipal;
		public Long IdDireccionElectronicaPrincipal;
		public Long IdTelefonoPrincipal;
		public String EsClienteSebra;
		public List<DocumentoIdentidad> DocumentoIdentidad;
		public List<DireccionFisica> DireccionFisica;
		public List<DireccionElectronica> DireccionElectronica;
		public List<Telefono> Telefono;
		public List<CuentaBancaria> CuentaBancaria;
		public List<TarjetaBancaria> TarjetaBancaria;
		public List<CuentaInversion> CuentaInversion;
		public List<Portafolio> Portafolio;
		
		public Persona() {
			this.DocumentoIdentidad = new ArrayList<>();
			this.DireccionFisica = new ArrayList<>();
			this.DireccionElectronica = new ArrayList<>();
			this.Telefono = new ArrayList<>();
			this.CuentaBancaria = new ArrayList<>();
			this.TarjetaBancaria = new ArrayList<>();
			this.CuentaInversion = new ArrayList<>();
	        this.IdCliente = 0L;
	    }
	}
	
	public static class DocumentoIdentidad extends BcsPojo {
		public Long IdPersona;
		public Long IdCliente;
		public Long IdDocumentoIdentidad;
		public String DocumentoIdentidad;
		public String DocumentoIdentidadAsociado;
	}
	
	public static class DireccionFisica extends BcsPojo {
		public Long IdPersona;
		public Long IdDireccionFisica;
		public Long IdCiudad;
		public String CodigoPostal;
		public String Calle;
		public String Numero;
		public String Departamento;
	}	
	
	public static class DireccionElectronica extends BcsPojo {
		public Long IdPersona;
		public Long IdDireccionElectronica;
		public String Direccion;
	}
	
	public static class Telefono extends BcsPojo {
		public Long IdPersona;
		public Long IdTelefono;
		public String Numero;
	}
	
	public static class CuentaBancaria extends BcsPojo {
		public Long IdPersona;
		public Long IdCuentaBancaria;
		public String NumeroCuentaBancaria;
	}
	
	public static class TarjetaBancaria extends BcsPojo {
		public Long IdPersona;
		public Long IdTarjetaBancaria;
		public String NumeroTarjetaBancaria;
	}
	
	public static class CuentaInversion extends BcsPojo {
		public Long IdPersona;
		public Long IdCuentaInversion;
	}
	
	public static class Portafolio extends BcsPojo {
		public Long IdPersona;
		public Long IdCliente;
		public Long IdPortafolio;
		//ASOCIADOS
		public List<Cartera> Cartera;
		public List<Orden> Orden;
		public List<Operacion> Operacion;
		public List<IngresoEgreso> IngresoEgreso;
		public List<EventoCapital> EventoCapital;
		public List<ComprobanteTesoreria> ComprobanteTesoreria;
		public List<ComprobanteFacturacion> ComprobanteFacturacion;
		public List<MovimientoComprobante> MovimientoComprobante;
		public List<SolicitudAporteRescate> SolicitudAporteRescate;
		
		public Portafolio() {
			this.Cartera = new ArrayList<>();
			this.Orden = new ArrayList<>();
			this.Operacion = new ArrayList<>();
			this.IngresoEgreso = new ArrayList<>();
			this.EventoCapital = new ArrayList<>();
			this.ComprobanteTesoreria = new ArrayList<>();
			this.ComprobanteFacturacion = new ArrayList<>();
			this.MovimientoComprobante = new ArrayList<>();
			this.SolicitudAporteRescate = new ArrayList<>();
	    }
	}
	
	public static class Cartera extends BcsPojo {
	    public Long IdPortafolio;
	    public Long FolioCartera;
	}
	
	public static class Orden extends BcsPojo {
	    public Long IdPortafolio;
	    public Long IdTransaccion;
	    public Long IdOrden;
	}

	public static class Asignacion extends BcsPojo {
	    public Long IdOrden;
	    public Long IdTransaccion;
	}
	
	public static class Operacion extends BcsPojo {
		public Long IdPortafolio;
	    public Long IdOperacion;
	}
	
	public static class IngresoEgreso extends BcsPojo {
	    public Long IdPortafolio;
	    public Long IdIngresoEgreso;
	}
	
	public static class EventoCapital extends BcsPojo {
	    public Long IdPortafolio;
	    public Long IdEventoCapitalCliente;
	}
	
	public static class ComprobanteTesoreria extends BcsPojo {
	    public Long IdPortafolio;
	    public Long IdComprobante;
	}
	
	public static class ComprobanteFacturacion extends BcsPojo {
	    public Long IdPortafolio;
	    public Long IdComprobante;
	}
	
	public static class MovimientoComprobante extends BcsPojo {
	    public Long IdPortafolio;
	    public Long IdMovimientoComprobante;
	}
	
	public static class SolicitudAporteRescate extends BcsPojo {
	    public Long IdPortafolio;
	    public Long IdSolicitudAporteRescate;
	}
	
	public static class ConfiguracionTipoBorrado extends BcsPojo {
		public String CodigoTipoBorrado;
		public String Tabla;
		public String Campo;
		public String DescripcionCampo;
		public String CodigoDato;
		public String ValorOfuscado;
		public String Accion;
		public String NombreLlave;
		public String TipoLlave;
		public String ValorLlave; 
		public String OrigenLlave;
		public String IdEstado;
	    public Long OrdenDelete;
	}
	
	public static class UpdateTabla extends BcsPojo {
	    public String Tabla;
	    public String NombreLlave;
	    public String TipoLlave;
	    public String ValorLlave;
	    public List<CampoUpdate> Campos = new ArrayList<>();
	}
	
	public static class CampoUpdate extends BcsPojo {
	    public String Campo;
	    public String Valor;
	}

	public static class DeleteTabla extends BcsPojo {
		public String Tabla;
		public String NombreLlave;
		public String TipoLlave;
		public String ValorLlave;
		public Long OrdenDelete;
	}
	
	public static class LogTipoBorrado extends BcsPojo {
	    public Long IdLogTipoBorrado;
		public Long IdCabeceraFTP;
	    public Timestamp FechaEjecucion;
	    public Long IdUsuario;
	}
    
	public static class DetalleLogTipoBorrado extends BcsPojo {
    	public Long IdDetalleLogTipoBorrado;
	    public Long IdLogTipoBorrado;
	    public String CodigoTipoBorrado;
	    public Long IdPersona;
	    public String CodigoDato = "";
	    public String Accion;
	    public String TablaAfectada = "";
	    public String NombreLlaveTablaAfectada = "";
	    public String ValorLlaveTablaAfectada = "";
	    public String NombreCampoTablaAfectada = "";
	    public String ValorOriginal = "";
	    public String ValorOfuscado = "";
	}
	
	private static class TablaAfectada extends BcsPojo{
		public String NombreLlave;
		public String ValorLlave;

		public TablaAfectada(String nombreLLave, String valorLlave) {
			this.NombreLlave = nombreLLave;
			this.ValorLlave = valorLlave;
		}
	}

	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		
		EntradaPurgaProspectoCliente in = gson.fromJson(jin.toString(), EntradaPurgaProspectoCliente.class);
		SalidaPurgaProspectoCliente out = logica(con, in);
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public SalidaPurgaProspectoCliente logica(Connection con, EntradaPurgaProspectoCliente entrada) throws Exception {
		logger.info("INICIO::PurgaProspectoCliente.logica");
		long inicio = System.currentTimeMillis();
		SalidaPurgaProspectoCliente salida = new SalidaPurgaProspectoCliente();
		
		SQL sql = new SQL();
		List<Cabecera> cabeceras = getCabeceras(con, entrada.p_FechaCarga,"PEN");
		
		if(cabeceras.isEmpty()) {
			logger.info("INFO::PurgaProspectoCliente - No se encontraron archivos para cargar");
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR, "UI_CARGA_BORRAR_DATOS_PERSONA_SIN_ARCHIVO_PARA_CARGAR",new HashMap<>(0),con,entrada.p_IdEmpresa));
			salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
			return salida;
		}
		
		try {
			cabeceras.stream().forEach(cab -> cab.IdEstado = "ASI");
			sql.batch(con,SQL_UPDATE_CABECERA , cabeceras ,Cabecera.class);
			
			List<LineaSalida> lineasArchivoSalida = new ArrayList<>();
			List<Detalle> detalles = getDetalles(con, cabeceras);
			
			if(detalles.isEmpty()) {
				logger.info("INFO::PurgaProspectoCliente - No se encontraron detalles, el archivo se encuentra vacio");
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR, "UI_CARGA_BORRAR_DATOS_PERSONA_ARCHIVO_CARGA_VACIO",new HashMap<>(0),con,entrada.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
			}else {
				logger.info("Se obtuvieron {} detalles", detalles.size());
				List<LineaEntrada> lineasArchivoEntrada = detalles.stream().map(detalle -> new LineaEntrada(detalle.IdCabecera, detalle.IdDetalle, detalle.Linea)).collect(Collectors.toList());

				//obtener tipos de borrado
				List<ConfiguracionTipoBorrado> configuracionesTipoBorrado = sql.query(con, SQL_SELECT_CONFIGURACION_TIPO_BORRADO, new HashMap<>(0), ConfiguracionTipoBorrado.class);
				
				if (configuracionesTipoBorrado.isEmpty()) {
				    throw new Exception("No existen configuraciones de tipo de borrado");
				}
				
				Map<String, List<ConfiguracionTipoBorrado>> configuracionPorCodigoTipoBorrado = configuracionesTipoBorrado.stream().collect(Collectors.groupingBy(t -> t.CodigoTipoBorrado));
				
				List<Persona> personas = obtenerPersonas(con, lineasArchivoEntrada);
				HashMap<String, Object> personasPorRut = Utils.List2Hash(personas, "RutTitular");
				
				ValidadorPurgaProspectoCliente validador = new ValidadorPurgaProspectoCliente();
				ValidadorPurgaProspectoCliente.EntradaValidadorPurgaProspectoCliente entradaValidador = new ValidadorPurgaProspectoCliente.EntradaValidadorPurgaProspectoCliente();

				entradaValidador.IdEmpresa = entrada.p_IdEmpresa;
				entradaValidador.Lineas = lineasArchivoEntrada;
				entradaValidador.Personas = personas;
				entradaValidador.configuracionPorCodigoTipoBorrado = configuracionPorCodigoTipoBorrado;

				validador.logica(con, entradaValidador);

				Map<String, List<LineaEntrada>> mapLineasValidasTipoBorrado = agruparLineasValidasPorTipoBorrado(lineasArchivoEntrada);

				List<LineaEntrada> lineasEntradaTipoBorrado01 = mapLineasValidasTipoBorrado.getOrDefault(TipoBorrado.PROSPECTO.Codigo, Collections.emptyList());
				List<LineaEntrada> lineasEntradaTipoBorrado02 = mapLineasValidasTipoBorrado.getOrDefault(TipoBorrado.CONSENTIMIENTO.Codigo, Collections.emptyList());
				List<LineaEntrada> lineasEntradaTipoBorrado03 = mapLineasValidasTipoBorrado.getOrDefault(TipoBorrado.DEFINITIVA.Codigo, Collections.emptyList());
				List<LineaEntrada> lineasEntradaTipoBorrado11 = mapLineasValidasTipoBorrado.getOrDefault(TipoBorrado.SUPRESION_DATO.Codigo, Collections.emptyList());
				
				Long secuenciaLog = Utils.getSecuencia(con, "BSCL.SocioNegocio.BD.Secuencias::LogTipoBorrado", 1L).get(0);

				List<ConfiguracionTipoBorrado> configTipoBorradoProspecto = configuracionPorCodigoTipoBorrado.getOrDefault(TipoBorrado.PROSPECTO.Codigo, Collections.emptyList());

				if (!lineasEntradaTipoBorrado01.isEmpty() && !configTipoBorradoProspecto.isEmpty()) {
					List<MensajeSalida> resultado = suprimirDatosMasivos(con, lineasEntradaTipoBorrado01, personasPorRut, configTipoBorradoProspecto, entrada, secuenciaLog, false);
					salida.P_OUT_MENSAJES.addAll(resultado);
					
					List<MensajeSalida> resultadoEliminar = eliminacionDefinitiva(con, lineasEntradaTipoBorrado01, personasPorRut, configTipoBorradoProspecto, entrada, secuenciaLog, true);
					salida.P_OUT_MENSAJES.addAll(resultadoEliminar);
				}

				List<ConfiguracionTipoBorrado> configBorradoConsentimiento = configuracionPorCodigoTipoBorrado.getOrDefault(TipoBorrado.CONSENTIMIENTO.Codigo, Collections.emptyList());

				if (!lineasEntradaTipoBorrado02.isEmpty() && !configBorradoConsentimiento.isEmpty()) {
					List<MensajeSalida> resultado = suprimirDatosMasivos(con, lineasEntradaTipoBorrado02, personasPorRut, configBorradoConsentimiento, entrada, secuenciaLog, true);
					salida.P_OUT_MENSAJES.addAll(resultado);
				}

				List<ConfiguracionTipoBorrado> configBorradoDefinitiva = configuracionPorCodigoTipoBorrado.getOrDefault(TipoBorrado.DEFINITIVA.Codigo, Collections.emptyList());

				if (!lineasEntradaTipoBorrado03.isEmpty() && !configBorradoDefinitiva.isEmpty()) {
					List<MensajeSalida> resultadoSuprimir = suprimirDatosMasivos(con, lineasEntradaTipoBorrado03, personasPorRut, configBorradoDefinitiva, entrada, secuenciaLog, false);
					salida.P_OUT_MENSAJES.addAll(resultadoSuprimir);

					List<MensajeSalida> resultadoEliminar = eliminacionDefinitiva(con, lineasEntradaTipoBorrado03, personasPorRut, configBorradoDefinitiva, entrada, secuenciaLog, true);
					salida.P_OUT_MENSAJES.addAll(resultadoEliminar);
				}

				List<ConfiguracionTipoBorrado> configBorradoSupresionDato = configuracionPorCodigoTipoBorrado.getOrDefault(TipoBorrado.SUPRESION_DATO.Codigo, Collections.emptyList());

				if (!lineasEntradaTipoBorrado11.isEmpty() && !configBorradoSupresionDato.isEmpty()) {
					List<MensajeSalida> resultado = suprimirDatosEspecificos(con, lineasEntradaTipoBorrado11, personasPorRut, configBorradoSupresionDato, entrada, secuenciaLog, true);
					salida.P_OUT_MENSAJES.addAll(resultado);
				}
	
				lineasArchivoSalida.addAll(armarLineasArchivoSalida(con, entrada, lineasArchivoEntrada));
				
				if(!lineasArchivoSalida.isEmpty()) {
					String nombreArchivoSalida = NOMBRE_ARCHIVO_SALIDA + "_" + entrada.p_FechaCarga.replace("-", "");
					Long contadorRegistrosProcesados = lineasArchivoSalida.stream().filter(linea -> linea.TipoEstado == TipoEstado.ELIMINACION_EXITOSA.Codigo).count();
					salida.P_OUT_REGISTROSPROCESADOSCONTADOR = contadorRegistrosProcesados;
					
					actualizarDetallesArchivoEntradaSFTP(con, detalles, lineasArchivoEntrada, lineasArchivoSalida, personasPorRut);
					agregarArchivoSalidaSFTP(con, entrada, lineasArchivoSalida, nombreArchivoSalida);
				}

				salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
				logger.info("INFO::PurgaProspectoCliente - Se agrega el Control de Proceso de la Carga del Archivo para el dia "+ entrada.p_FechaCarga);
			}
			cabeceras.stream().forEach(cab -> cab.IdEstado = "PRO");
		} catch (Exception ex) {
		    logger.error("Error ejecutando proceso.", ex);
		    cabeceras.forEach(cab -> cab.IdEstado = "PEN");
		    throw ex;
		} finally {
			sql.batch(con,SQL_UPDATE_CABECERA , cabeceras ,Cabecera.class);		
		}
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::PurgaProspectoCliente.logica {} ms", fin - inicio);
		return salida;
	}

	private List<Persona> obtenerPersonas(Connection con, List<LineaEntrada> lineasEntrada) throws Exception{
		logger.info("INICIO::PurgaProspectoCliente.obtenerPersonas");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		List<Persona> personas = new ArrayList<>();
		List<String> listaRutTitular = lineasEntrada.stream().map(linea -> linea.RutTitular).distinct().collect(Collectors.toList());
		
		if(!listaRutTitular.isEmpty()) {
			HashMap<String, Object> params = new HashMap<>(1);
			params.put("DocumentosIdentidad", listaRutTitular);
			personas = sql.query(con, SQL_OBTENER_PERSONA, params, Persona.class);	
			
			List<Long> idsPersonas =  new ArrayList<>();
			for (Persona persona : personas) {
				idsPersonas.add(persona.IdPersona);
			}
			
			params.clear();
			params.put("IdsPersonas", idsPersonas);
			
			List<DocumentoIdentidad> documentosIdentidad = sql.query(con, SQL_OBTENER_DOCUMENTO_IDENTIDAD, params, DocumentoIdentidad.class);	
			List<DireccionFisica> direccionesFisicas = sql.query(con, SQL_OBTENER_DIRECCION_FISICA, params, DireccionFisica.class);	
			List<DireccionElectronica> direccionesElectronicas = sql.query(con, SQL_OBTENER_DIRECCION_ELECTRONICA, params, DireccionElectronica.class);	
			List<Telefono> telefonos = sql.query(con, SQL_OBTENER_TELEFONO, params, Telefono.class);	
			List<CuentaBancaria> cuentasBancarias = sql.query(con, SQL_OBTENER_CUENTA_BANCARIA, params, CuentaBancaria.class);	
			List<TarjetaBancaria> tarjerasBancarias = sql.query(con, SQL_OBTENER_TARJETA_BANCARIA, params, TarjetaBancaria.class);	
			List<CuentaInversion> cuentasInversion = sql.query(con, SQL_OBTENER_CUENTA_INVERSION, params, CuentaInversion.class);
			List<Portafolio> portafolios = sql.query(con, SQL_OBTENER_PORTAFOLIO, params, Portafolio.class);	
			
			Map<Long, List<DocumentoIdentidad>> documentosIdentidadPorPersona = documentosIdentidad.stream().collect(Collectors.groupingBy(d -> d.IdPersona));
			Map<Long, List<DireccionFisica>> direccionesFisicasPorPersona = direccionesFisicas.stream().collect(Collectors.groupingBy(d -> d.IdPersona));
			Map<Long, List<DireccionElectronica>> direccionesElectronicasPorPersona = direccionesElectronicas.stream().collect(Collectors.groupingBy(d -> d.IdPersona));
			Map<Long, List<Telefono>> telefonosPorPersona = telefonos.stream().collect(Collectors.groupingBy(t -> t.IdPersona));
			Map<Long, List<CuentaBancaria>> cuentasPorPersona = cuentasBancarias.stream().collect(Collectors.groupingBy(c -> c.IdPersona));
			Map<Long, List<TarjetaBancaria>> tarjetasPorPersona = tarjerasBancarias.stream().collect(Collectors.groupingBy(t -> t.IdPersona));
			Map<Long, List<CuentaInversion>> cuentasInversionPorPersona = cuentasInversion.stream().collect(Collectors.groupingBy(t -> t.IdPersona));
			Map<Long, List<Portafolio>> portafoliosPorPersona = portafolios.stream().collect(Collectors.groupingBy(t -> t.IdPersona));
			
			for (Persona persona : personas) {
				persona.DocumentoIdentidad = documentosIdentidadPorPersona.getOrDefault(persona.IdPersona, new ArrayList<>());
				persona.DireccionFisica = direccionesFisicasPorPersona.getOrDefault(persona.IdPersona, new ArrayList<>());
				persona.DireccionElectronica = direccionesElectronicasPorPersona.getOrDefault(persona.IdPersona, new ArrayList<>());
				persona.Telefono = telefonosPorPersona.getOrDefault(persona.IdPersona, new ArrayList<>());
				persona.CuentaBancaria = cuentasPorPersona.getOrDefault(persona.IdPersona, new ArrayList<>());
				persona.TarjetaBancaria = tarjetasPorPersona.getOrDefault(persona.IdPersona, new ArrayList<>());
				persona.CuentaInversion = cuentasInversionPorPersona.getOrDefault(persona.IdPersona, new ArrayList<>());
				persona.Portafolio = portafoliosPorPersona.getOrDefault(persona.IdPersona, new ArrayList<>());
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::PurgaProspectoCliente.obtenerPersonas {} ms", fin - inicio);
		return personas;
	}
	
	private void cargarEntidadesAdicionalesDefinitiva(Connection con, List<Persona> personas) throws Exception {
		logger.info("INICIO::PurgaProspectoCliente.cargarEntidadesAdicionalesDefinitiva");
		if (personas == null || personas.isEmpty()) {
			return;
		}

		SQL sql = new SQL();

		List<Portafolio> portafolios = personas.stream().flatMap(p -> p.Portafolio.stream()).collect(Collectors.toList());

		if (portafolios.isEmpty()) {
			return;
		}

		List<Long> idsPortafolio = portafolios.stream().map(p -> p.IdPortafolio).distinct().collect(Collectors.toList());

		HashMap<String, Object> params = new HashMap<>(1);
		params.put("IdsPortafolio", idsPortafolio);

		List<Cartera> carteras = sql.query(con, SQL_OBTENER_CARTERA, params, Cartera.class);
		List<Orden> transaccionesOrden = sql.query(con, SQL_OBTENER_ORDEN, params, Orden.class);
		List<Operacion> operaciones = sql.query(con, SQL_OBTENER_OPERACION, params, Operacion.class);
		List<IngresoEgreso> ingresosEgresos = sql.query(con, SQL_OBTENER_INGRESO_EGRESO, params, IngresoEgreso.class);
		List<EventoCapital> eventosCapital = sql.query(con, SQL_OBTENER_EVENTO_CAPITAL, params, EventoCapital.class);
		List<ComprobanteTesoreria> comprobantesTesoreria = sql.query(con, SQL_OBTENER_COMPROBANTE_TESORERIA, params, ComprobanteTesoreria.class);
		List<ComprobanteFacturacion> comprobantesFacturacion = sql.query(con, SQL_OBTENER_COMPROBANTE_FACTURACION,params, ComprobanteFacturacion.class);
		List<MovimientoComprobante> movimientosComprobante = sql.query(con, SQL_OBTENER_MOVIMIENTO_COMPROBANTE, params, MovimientoComprobante.class);
		List<SolicitudAporteRescate> solicitudesApoRes = sql.query(con, SQL_OBTENER_SOLICITUD_APOR_RES, params, SolicitudAporteRescate.class);
		
		Map<Long, List<Cartera>> carterasPorPortafolio = carteras.stream().collect(Collectors.groupingBy(o -> o.IdPortafolio));
		Map<Long, List<Orden>> transaccionesOrdenPorPortafolio = transaccionesOrden.stream().collect(Collectors.groupingBy(o -> o.IdPortafolio));
		Map<Long, List<Operacion>> operacionesPorPortafolio = operaciones.stream().collect(Collectors.groupingBy(o -> o.IdPortafolio));
		Map<Long, List<IngresoEgreso>> ingresoEgresoPorPortafolio = ingresosEgresos.stream().collect(Collectors.groupingBy(i -> i.IdPortafolio));
		Map<Long, List<EventoCapital>> eventoCapitalPorPortafolio = eventosCapital.stream().collect(Collectors.groupingBy(e -> e.IdPortafolio));
		Map<Long, List<ComprobanteTesoreria>> comprobanteTesPorPortafolio = comprobantesTesoreria.stream().collect(Collectors.groupingBy(c -> c.IdPortafolio));
		Map<Long, List<ComprobanteFacturacion>> comprobanteFacPorPortafolio = comprobantesFacturacion.stream().collect(Collectors.groupingBy(c -> c.IdPortafolio));
		Map<Long, List<MovimientoComprobante>> movimientoPorPortafolio = movimientosComprobante.stream().collect(Collectors.groupingBy(m -> m.IdPortafolio));
		Map<Long, List<SolicitudAporteRescate>> solicitudesApoResPorPortafolio = solicitudesApoRes.stream().collect(Collectors.groupingBy(m -> m.IdPortafolio));

		for (Portafolio portafolio : portafolios) {
			portafolio.Cartera = carterasPorPortafolio.getOrDefault(portafolio.IdPortafolio, new ArrayList<>());
			portafolio.Orden = transaccionesOrdenPorPortafolio.getOrDefault(portafolio.IdPortafolio, new ArrayList<>());
			portafolio.Operacion = operacionesPorPortafolio.getOrDefault(portafolio.IdPortafolio, new ArrayList<>());
			portafolio.IngresoEgreso = ingresoEgresoPorPortafolio.getOrDefault(portafolio.IdPortafolio,new ArrayList<>());
			portafolio.EventoCapital = eventoCapitalPorPortafolio.getOrDefault(portafolio.IdPortafolio,new ArrayList<>());
			portafolio.ComprobanteTesoreria = comprobanteTesPorPortafolio.getOrDefault(portafolio.IdPortafolio,new ArrayList<>());
			portafolio.ComprobanteFacturacion = comprobanteFacPorPortafolio.getOrDefault(portafolio.IdPortafolio,new ArrayList<>());
			portafolio.MovimientoComprobante = movimientoPorPortafolio.getOrDefault(portafolio.IdPortafolio,new ArrayList<>());
			portafolio.SolicitudAporteRescate = solicitudesApoResPorPortafolio.getOrDefault(portafolio.IdPortafolio,new ArrayList<>());
		}

		logger.info("FIN::PurgaProspectoCliente.cargarEntidadesAdicionalesDefinitiva");
	}

	private List<MensajeSalida> suprimirDatosMasivos(Connection con, List<LineaEntrada> lineasEntrada, HashMap<String, Object> personasPorRut, 
			List<ConfiguracionTipoBorrado> configuracion, EntradaPurgaProspectoCliente entrada, Long secuenciaLog, boolean registraLog) throws Exception {
		logger.info("INICIO::PurgaProspectoCliente.suprimirDatosMasivos");
		long inicio = System.currentTimeMillis();
		List<MensajeSalida> mensajes = new ArrayList<>();
		
		List<ConfiguracionTipoBorrado> configuracionTipoBorrado = configuracion.stream().filter(c -> ACCION_UPDATE.equals(c.Accion)).collect(Collectors.toList());

		List<DetalleLogTipoBorrado> listaDetalleLogTipoBorrado = new ArrayList<>();
		Map<String, UpdateTabla> mapUpdates = new HashMap<>();
		HashMap<String, String> hashValores = new HashMap<>();
		HashMap<String, String> documentosOfuscados = new HashMap<>();
		Set<LineaEntrada> lineasActualizadas = new HashSet<>();

		for (LineaEntrada linea : lineasEntrada) {
			Persona persona = (Persona) personasPorRut.get(linea.RutTitular);

			for (ConfiguracionTipoBorrado config : configuracionTipoBorrado) {
				List<?> registros = obtenerObjetosOrigen(persona, config);

				if (registros == null || registros.isEmpty()) {
					continue;
				}

				for (Object registro : registros) {
					String valorLlave = obtenerValorAtributo(registro, config.ValorLlave);

					if (valorLlave == null) {
						continue;
					}

					UpdateTabla update = obtenerUpdate(mapUpdates, config, valorLlave);

					boolean existeCampo = update.Campos.stream().anyMatch(c -> c.Campo.equals(config.Campo));

					String key = config.Tabla + "|" + valorLlave + "|" + config.Campo;

					String valorOfuscado = hashValores.computeIfAbsent(key, k -> obtenerValorUpdate(config, registro, documentosOfuscados));

					if (!existeCampo) {
						CampoUpdate campo = new CampoUpdate();
						campo.Campo = config.Campo;
						campo.Valor = valorOfuscado;
						update.Campos.add(campo);
						lineasActualizadas.add(linea);
					}

					if (registraLog && TABLAS_PRINCIPALES_LOG.contains(config.Tabla)) {
						DetalleLogTipoBorrado detalleLog = new DetalleLogTipoBorrado();

						detalleLog.IdLogTipoBorrado = secuenciaLog;
						detalleLog.IdPersona = persona.IdPersona;
						detalleLog.CodigoTipoBorrado = linea.TipoBorrado;
						detalleLog.CodigoDato = config.CodigoDato;
						detalleLog.Accion = config.Accion;
						detalleLog.TablaAfectada = config.Tabla;
						TablaAfectada tablaAfectada = obtenerDatosTablaAfectada(registro);
						detalleLog.NombreLlaveTablaAfectada = tablaAfectada.NombreLlave;
						detalleLog.ValorLlaveTablaAfectada = tablaAfectada.ValorLlave;
						detalleLog.NombreCampoTablaAfectada = config.Campo;
						String valorOriginal = obtenerValorAtributo(registro, config.Campo);
						detalleLog.ValorOriginal = valorOriginal == null ? "" : valorOriginal;
						detalleLog.ValorOfuscado = valorOfuscado == null ? "" : valorOfuscado;
						listaDetalleLogTipoBorrado.add(detalleLog);
					}
				}
			}
		}

		boolean ejecucionExitosa = ejecutarUpdate(con, mapUpdates, lineasActualizadas);

		if (ejecucionExitosa) {
			if (registraLog && !listaDetalleLogTipoBorrado.isEmpty()) {
				agregarLogTipoBorrado(con, lineasEntrada.get(0).IdCabecera, secuenciaLog, listaDetalleLogTipoBorrado, entrada);
			}
			mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "UI_SUPRESION_EXITOSA", new HashMap<>(0), con, entrada.p_IdEmpresa));
		} else {
			mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_SUPRESION_ERRONEA", new HashMap<>(0), con, entrada.p_IdEmpresa));
		}

		long fin = System.currentTimeMillis();
		logger.info("FIN::PurgaProspectoCliente.suprimirDatosMasivos {} ms", fin - inicio);
	    return mensajes;
	}
	
	private List<MensajeSalida> suprimirDatosEspecificos(Connection con, List<LineaEntrada> lineasEntrada, HashMap<String, Object> personasPorRut, List<ConfiguracionTipoBorrado> configuracion, 
			EntradaPurgaProspectoCliente entrada, Long secuenciaLog, boolean registraLog) throws Exception {
	    logger.info("INICIO::PurgaProspectoCliente.suprimirDatosEspecificos");
	    long inicio = System.currentTimeMillis();
		List<MensajeSalida> mensajes = new ArrayList<>();

	    Map<String, List<ConfiguracionTipoBorrado>> configuracionPorCodigoDato = configuracion.stream().filter(c -> ACCION_UPDATE.equals(c.Accion)).collect(Collectors.groupingBy(c -> c.CodigoDato));

	    List<DetalleLogTipoBorrado> listaDetalleLogTipoBorrado = new ArrayList<>();
	    Map<String, UpdateTabla> mapUpdates = new HashMap<>();
	    HashMap<String, String> hashValores = new HashMap<>();
	    HashMap<String, String> documentosOfuscados = new HashMap<>();
		Set<LineaEntrada> lineasActualizadas = new HashSet<>();
		
		for (LineaEntrada linea : lineasEntrada) {
			Persona persona = (Persona) personasPorRut.get(linea.RutTitular);

			List<ConfiguracionTipoBorrado> configuracionDato = configuracionPorCodigoDato.getOrDefault(linea.NombreDato, Collections.emptyList());

			for (ConfiguracionTipoBorrado config : configuracionDato) {
				List<?> registros = obtenerObjetosOrigen(persona, config);

				if (registros == null || registros.isEmpty()) {
					continue;
				}

				for (Object registro : registros) {
					String valorLlave = obtenerValorAtributo(registro, config.ValorLlave);

					if (valorLlave == null) {
						continue;
					}

					UpdateTabla update = obtenerUpdate(mapUpdates, config, valorLlave);

					boolean existeCampo = update.Campos.stream().anyMatch(c -> c.Campo.equals(config.Campo));

					String key = config.Tabla + "|" + valorLlave + "|" + config.Campo;

					String valorOfuscado = hashValores.computeIfAbsent(key, k -> obtenerValorUpdate(config, registro, documentosOfuscados));

					if (!existeCampo) {
						CampoUpdate campo = new CampoUpdate();
						campo.Campo = config.Campo;
						campo.Valor = valorOfuscado;
						update.Campos.add(campo);
						lineasActualizadas.add(linea);
					}

					if (registraLog && TABLAS_PRINCIPALES_LOG.contains(config.Tabla)) {
						DetalleLogTipoBorrado detalleLog = new DetalleLogTipoBorrado();

						detalleLog.IdLogTipoBorrado = secuenciaLog;
						detalleLog.IdPersona = persona.IdPersona;
						detalleLog.CodigoTipoBorrado = linea.TipoBorrado;
						detalleLog.CodigoDato = config.CodigoDato;
						detalleLog.Accion = config.Accion;
						detalleLog.TablaAfectada = config.Tabla;
						TablaAfectada tablaAfectada = obtenerDatosTablaAfectada(registro);
						detalleLog.NombreLlaveTablaAfectada = tablaAfectada.NombreLlave;
						detalleLog.ValorLlaveTablaAfectada = tablaAfectada.ValorLlave;
						detalleLog.NombreCampoTablaAfectada = config.Campo;
						String valorOriginal = obtenerValorAtributo(registro, config.Campo);
						detalleLog.ValorOriginal = valorOriginal == null ? "" : valorOriginal;
						detalleLog.ValorOfuscado = valorOfuscado == null ? "" : valorOfuscado;
						listaDetalleLogTipoBorrado.add(detalleLog);
					}
				}
			}
		}

		boolean ejecucionExitosa = ejecutarUpdate(con, mapUpdates, lineasActualizadas);
		
		if (ejecucionExitosa) {
			if (registraLog && !listaDetalleLogTipoBorrado.isEmpty()) {
				agregarLogTipoBorrado(con, lineasEntrada.get(0).IdCabecera, secuenciaLog, listaDetalleLogTipoBorrado, entrada);
			}
			mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "UI_SUPRESION_EXITOSA", new HashMap<>(0), con, entrada.p_IdEmpresa));
		} else {
			mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_SUPRESION_ERRONEA", new HashMap<>(0), con, entrada.p_IdEmpresa));
		}

	    long fin = System.currentTimeMillis();
	    logger.info("FIN::PurgaProspectoCliente.suprimirDatosEspecificos {} ms", fin - inicio);
	    return mensajes;
	}
	
	private List<MensajeSalida> eliminacionDefinitiva(Connection con, List<LineaEntrada> lineasEntrada, HashMap<String, Object> personasPorRut, List<ConfiguracionTipoBorrado> configuracionTipoBorrado, 
			EntradaPurgaProspectoCliente entrada, Long secuenciaLog, boolean registraLog) throws Exception {
		logger.info("INICIO::PurgaProspectoCliente.eliminacionDefinitiva");
		long inicio = System.currentTimeMillis();
		List<MensajeSalida> mensajes = new ArrayList<>();

		List<ConfiguracionTipoBorrado> configuracionesDelete = configuracionTipoBorrado.stream().filter(c -> ACCION_DELETE.equalsIgnoreCase(c.Accion))
	            .sorted(Comparator.comparing( d -> d.OrdenDelete == null ? 9999L : d.OrdenDelete)).collect(Collectors.toList());

		List<Persona> personas = personasPorRut.values().stream().map(p -> (Persona) p).collect(Collectors.toList());
		cargarEntidadesAdicionalesDefinitiva(con, personas);

		Map<String, DeleteTabla> deleteMap = new LinkedHashMap<>();
		List<LineaEntrada> lineasEliminadas = new ArrayList<>();
		
		for (LineaEntrada linea : lineasEntrada) {
			Persona persona = (Persona) personasPorRut.get(linea.RutTitular);
			
			boolean tieneDelete = false;

			for (ConfiguracionTipoBorrado config : configuracionesDelete) {
				List<?> registros = obtenerObjetosOrigen(persona, config);

				if (registros == null || registros.isEmpty()) {
					continue;
				}

				for (Object registro : registros) {
					String valorLlave = obtenerValorAtributo(registro, config.ValorLlave);

					if (valorLlave == null) {
						continue;
					}

					obtenerDelete(deleteMap, config, valorLlave);
					tieneDelete = true;
				}
			}
			
			if (tieneDelete) {
				lineasEliminadas.add(linea);
			}
		}

		boolean ejecucionExitosa = ejecutarDelete(con, new ArrayList<>(deleteMap.values()));
		
		if (ejecucionExitosa) {
			for (LineaEntrada linea : lineasEliminadas) {
	            if (linea.esProcesable()) {
	                linea.EstadoProceso = new EstadoProceso(TipoEstado.ELIMINACION_EXITOSA, TipoMotivo.NO_APLICA, "Procesado con éxito");
	            }
	        }
			
			List<DetalleLogTipoBorrado> detallesLog = new ArrayList<>();

			if (registraLog) {
				for (LineaEntrada linea : lineasEliminadas) {
					Persona persona = (Persona) personasPorRut.get(linea.RutTitular);

					DetalleLogTipoBorrado detalle = new DetalleLogTipoBorrado();
					detalle.IdLogTipoBorrado = secuenciaLog;
					detalle.IdPersona = persona.IdPersona;
					detalle.ValorOriginal = persona.RutTitular;
					detalle.CodigoTipoBorrado = linea.TipoBorrado;
					detalle.Accion = ACCION_DELETE;

					detallesLog.add(detalle);
				}	
				agregarLogTipoBorrado(con, lineasEntrada.get(0).IdCabecera, secuenciaLog, detallesLog, entrada);
			}
		     
			mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "UI_ELIMINACION_DEFINITIVA_EXITOSA", new HashMap<>(0), con, entrada.p_IdEmpresa));
		} else {
			for (LineaEntrada linea : lineasEntrada) {
				linea.EstadoProceso = new EstadoProceso(TipoEstado.ERROR_GENERICO, TipoMotivo.NO_APLICA,"Error asociado a la ejecución del proceso.");
			}
			
			mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ELIMINACION_DEFINITIVA_ERRONEA", new HashMap<>(0), con, entrada.p_IdEmpresa));
		}

		long fin = System.currentTimeMillis();
		logger.info("FIN::PurgaProspectoCliente.eliminacionDefinitiva {} ms", fin - inicio);
		return mensajes;
	}
	
	private DeleteTabla obtenerDelete(Map<String, DeleteTabla> deletesMap, ConfiguracionTipoBorrado config, String valorLlave) {
		String key = config.Tabla + "|" + config.NombreLlave + "|" + valorLlave;

		return deletesMap.computeIfAbsent(key, k -> {
	        DeleteTabla delete = new DeleteTabla();

	        delete.Tabla = config.Tabla;
	        delete.NombreLlave = config.NombreLlave;
	        delete.TipoLlave = config.TipoLlave;
	        delete.ValorLlave = valorLlave;
	        delete.OrdenDelete = config.OrdenDelete;

	        return delete;
	    });
	}
	
	private UpdateTabla obtenerUpdate(Map<String, UpdateTabla> updatesMap, ConfiguracionTipoBorrado config, String valorLlave) {
		String key = config.Tabla + "|" + config.NombreLlave + "|" + config.TipoLlave + "|" + valorLlave;

		return updatesMap.computeIfAbsent(key, k -> {
			UpdateTabla update = new UpdateTabla();

			update.Tabla = config.Tabla;
			update.NombreLlave = config.NombreLlave;
			update.TipoLlave = config.TipoLlave;
			update.ValorLlave = valorLlave;

			return update;
		});
	}

	private boolean ejecutarUpdate(Connection con, Map<String, UpdateTabla> mapUpdates, Set<LineaEntrada> lineasEntrada) {
		logger.info("INICIO::PurgaProspectoCliente.ejecutarUpdates");
		long inicio = System.currentTimeMillis();
		
		if (mapUpdates.isEmpty()) {
		    logger.info("No existen updates para ejecutar");
		    return true;
		}

		try {
			ejecutarBatch(con, new ArrayList<>(mapUpdates.values()));

			for (LineaEntrada linea : lineasEntrada) {
				if (linea.esProcesable()) {
					linea.EstadoProceso = new EstadoProceso(TipoEstado.ELIMINACION_EXITOSA, TipoMotivo.NO_APLICA, "Procesado con éxito");
				}
			}
			
			return true;
		} catch (Exception e) {
			logger.error("Error ejecutando actualizaciones.", e);

			for (LineaEntrada linea : lineasEntrada) {
				linea.EstadoProceso = new EstadoProceso(TipoEstado.ERROR_GENERICO, TipoMotivo.NO_APLICA, "Error asociado a la ejecución del proceso.");
			}
			
			return false;
		} finally {
			long fin = System.currentTimeMillis();
		    logger.info("FIN::PurgaProspectoCliente.ejecutarUpdates {} ms", fin - inicio);
		}
	}
	
	private boolean ejecutarDelete(Connection con, List<DeleteTabla> eliminacionesPendientes) {
		logger.info("INICIO::PurgaProspectoCliente.ejecutarDelete");
		long inicio = System.currentTimeMillis();

		try {
			if (eliminacionesPendientes == null || eliminacionesPendientes.isEmpty()) {
				return true;
			}

			for (DeleteTabla delete : eliminacionesPendientes) {
				if (delete.ValorLlave == null || "0".equals(delete.ValorLlave) || delete.ValorLlave.trim().isEmpty()) {
					logger.warn("Se omite delete tabla={} llave={} valor={}", delete.Tabla, delete.NombreLlave, delete.ValorLlave);
					continue;
				}

				String sql = "DELETE FROM \"" + delete.Tabla + "\" WHERE \"" + delete.NombreLlave + "\" = ?";
				
				//System.out.println(sql.replace("?", delete.ValorLlave));

				try (PreparedStatement ps = con.prepareStatement(sql)) {
					setValue(ps, 1, delete.ValorLlave);
					int filas = ps.executeUpdate();
					logger.info("DELETE Tabla={} Campo={} Valor={} Filas={}", delete.Tabla, delete.NombreLlave, delete.ValorLlave, filas);
				}
			}
			return true;
		} catch (Exception e) {
			logger.error("Error ejecutando deletes", e);
			return false;

		} finally {
			long fin = System.currentTimeMillis();
			logger.info("FIN::PurgaProspectoCliente.ejecutarDelete {} ms", fin - inicio);
		}
	}
	
	private void agregarLogTipoBorrado(Connection con,Long idCabecera, Long secuenciaLog, List<DetalleLogTipoBorrado> listaDetalleLogTipoBorrado, EntradaPurgaProspectoCliente entrada) throws Exception {
		logger.info("INICIO::PurgaProspectoCliente.agregarLogTipoBorrado");
		long inicio = System.currentTimeMillis();

		Timestamp fechaSistema = FechaDB.ObtenerFechaHoraTs(con, entrada.p_IdEmpresa);
		List<LogTipoBorrado> listaLogTipoBorrado = new ArrayList<>();

		LogTipoBorrado log = new LogTipoBorrado();
		log.IdLogTipoBorrado = secuenciaLog;
		log.IdCabeceraFTP = idCabecera;
		log.FechaEjecucion = fechaSistema;
		log.IdUsuario = entrada.p_IdUsuario;

		listaLogTipoBorrado.add(log);

		SQL sql = new SQL();
		sql.batch(con, SQL_INSERT_LOG_TIPO_BORRADO, listaLogTipoBorrado, LogTipoBorrado.class);
		sql.batch(con, SQL_INSERT_DETALLE_LOG_TIPO_BORRADO, listaDetalleLogTipoBorrado, DetalleLogTipoBorrado.class);

		long fin = System.currentTimeMillis();
		logger.info("FIN::PurgaProspectoCliente.agregarLogTipoBorrado {} ms", fin - inicio);
	}
	
	private List<Object> obtenerObjetosOrigen(Persona persona, ConfiguracionTipoBorrado config) {
		switch (config.OrigenLlave) {
		case "PERSONA":
			return Collections.singletonList(persona);
		case "DOCUMENTO_IDENTIDAD":
			return new ArrayList<>(persona.DocumentoIdentidad);
		case "DIRECCION_FISICA":
			return new ArrayList<>(persona.DireccionFisica);
		case "DIRECCION_ELECTRONICA":
			return new ArrayList<>(persona.DireccionElectronica);
		case "TELEFONO":
			return new ArrayList<>(persona.Telefono);
		case "CUENTA_BANCARIA":
			return new ArrayList<>(persona.CuentaBancaria);
		case "TARJETA_BANCARIA":
			return new ArrayList<>(persona.TarjetaBancaria);
		case "CUENTA_INVERSION":
			return new ArrayList<>(persona.CuentaInversion);
		case "PORTAFOLIO":
			return new ArrayList<>(persona.Portafolio);
		case "CARTERA":
		    return persona.Portafolio.stream().flatMap(p -> p.Cartera.stream()).collect(Collectors.toList());
		case "ORDEN":
		    return persona.Portafolio.stream().flatMap(p -> p.Orden.stream()).collect(Collectors.toList());
		case "OPERACION":
		    return persona.Portafolio.stream().flatMap(p -> p.Operacion.stream()).collect(Collectors.toList());
		case "INGRESOEGRESO":
		    return persona.Portafolio.stream().flatMap(p -> p.IngresoEgreso.stream()).collect(Collectors.toList());
		case "EVENTO_CAPITAL":
		    return persona.Portafolio.stream().flatMap(p -> p.EventoCapital.stream()).collect(Collectors.toList());
		case "COMPROBANTE_TES":
		    return persona.Portafolio.stream().flatMap(p -> p.ComprobanteTesoreria.stream()).collect(Collectors.toList());
		case "COMPROBANTE_FAC":
		    return persona.Portafolio.stream().flatMap(p -> p.ComprobanteFacturacion.stream()).collect(Collectors.toList());
		case "MOVIMIENTO_COMPROBANTE":
		    return persona.Portafolio.stream().flatMap(p -> p.MovimientoComprobante.stream()).collect(Collectors.toList());
		case "SOLICITUD_APORES":
		    return persona.Portafolio.stream().flatMap(p -> p.SolicitudAporteRescate.stream()).collect(Collectors.toList());
		default:
			return Collections.emptyList();
		}
	}
	
	private TablaAfectada obtenerDatosTablaAfectada(Object registro) {
		// Solo aplica para entidades que generan auditoría (TABLAS_PRINCIPALES_LOG)
		if (registro instanceof DocumentoIdentidad) {
			DocumentoIdentidad entidad = (DocumentoIdentidad) registro;
			return new TablaAfectada("IdDocumentoIdentidad", entidad.IdDocumentoIdentidad.toString());
		}

		if (registro instanceof DireccionFisica) {
			DireccionFisica entidad = (DireccionFisica) registro;
			return new TablaAfectada("IdDireccionFisica", entidad.IdDireccionFisica.toString());
		}

		if (registro instanceof DireccionElectronica) {
			DireccionElectronica entidad = (DireccionElectronica) registro;
			return new TablaAfectada("IdDireccionElectronica", entidad.IdDireccionElectronica.toString());
		}

		if (registro instanceof Telefono) {
			Telefono entidad = (Telefono) registro;
			return new TablaAfectada("IdTelefono", entidad.IdTelefono.toString());
		}

		if (registro instanceof CuentaBancaria) {
			CuentaBancaria entidad = (CuentaBancaria) registro;
			return new TablaAfectada("IdCuentaBancaria", entidad.IdCuentaBancaria.toString());
		}

		if (registro instanceof TarjetaBancaria) {
			TarjetaBancaria entidad = (TarjetaBancaria) registro;
			return new TablaAfectada("IdTarjetaBancaria", entidad.IdTarjetaBancaria.toString());
		}

		if (registro instanceof Persona) {
			Persona entidad = (Persona) registro;
			return new TablaAfectada("IdPersona", entidad.IdPersona.toString());
		}

		logger.warn("Entidad no soportada para obtener información de entidad afectada: {}", registro.getClass().getName());
		return new TablaAfectada("DESCONOCIDO", "DESCONOCIDO");
	}
	
	private String obtenerValorUpdate(ConfiguracionTipoBorrado configCampo, Object registro, Map<String, String> documentosOfuscados) {
		if (configCampo.ValorOfuscado == null) {
			return null;
		}

		switch (configCampo.ValorOfuscado) {

		case "CODIFICADO":
			if (registro instanceof DocumentoIdentidad) {
				DocumentoIdentidad doc = (DocumentoIdentidad) registro;
				String documentoBase = doc.DocumentoIdentidad;
				
				if (documentoBase == null) {
					return null;
				}

				String valorOfuscado = documentosOfuscados.computeIfAbsent(documentoBase, d -> OfuscarDocumentoIdentidad.ofuscar(d));

				return valorOfuscado;
			}
			return configCampo.ValorOfuscado;
		default:
			return configCampo.ValorOfuscado;
		}
	}

	private void ejecutarBatch(Connection con, List<UpdateTabla> updates) throws Exception {
		logger.info("INICIO::PurgaProspectoCliente.ejecutarBatch");
		long inicio = System.currentTimeMillis();
		
	    if (updates == null || updates.isEmpty())
	        return;

	    Map<String, List<UpdateTabla>> grupos = new HashMap<>();

	    for (UpdateTabla update : updates) {
	        String clausulaSet = update.Campos.stream().map(c -> "\"" + c.Campo + "\" = ?").collect(Collectors.joining(", "));
	        String sql = "UPDATE \"" + update.Tabla + "\" SET " + clausulaSet + " WHERE \"" + update.NombreLlave + "\" = ?";

	        grupos.computeIfAbsent(sql, k -> new ArrayList<>()).add(update);
	    }

	    for (Map.Entry<String, List<UpdateTabla>> entry : grupos.entrySet()) {
	        String sql = entry.getKey();
	        List<UpdateTabla> lista = entry.getValue();

	        try (PreparedStatement ps = con.prepareStatement(sql)) {
	            for (UpdateTabla update : lista) {

	                int index = 1;

	                for (CampoUpdate campo : update.Campos) {
	                    setValue(ps, index++, campo.Valor);
	                }

	                setValue(ps, index, update.ValorLlave);
	                
	                // Imprimir UPDATE completo
//					String sqlImpreso = sql;
//					for (CampoUpdate campo : update.Campos) {
//						String valor = campo.Valor == null || "NULL".equalsIgnoreCase(campo.Valor) ? "NULL" : "'" + campo.Valor + "'";
//						sqlImpreso = sqlImpreso.replaceFirst("\\?", valor);
//					}
//					sqlImpreso = sqlImpreso.replaceFirst("\\?", update.ValorLlave);
//					System.out.println(sqlImpreso);
	                
	                ps.addBatch();
	            }

	            ps.executeBatch();
	        }
	    }
	    
	    long fin = System.currentTimeMillis();
		logger.info("FIN::PurgaProspectoCliente.ejecutarBatch {} ms", fin - inicio);
	}
	
	private void setValue(PreparedStatement ps, int index, String value) throws Exception {
		 if (value == null || "NULL".equalsIgnoreCase(value)) {
	        ps.setNull(index, java.sql.Types.VARCHAR);
	        return;
	    }

	    ps.setString(index, value);
	}
	
	private void actualizarDetallesArchivoEntradaSFTP(Connection con,List<Detalle> detalles, List<LineaEntrada> lineasArchivoEntrada, List<LineaSalida> lineasArchivoSalida, HashMap<String, Object> personasPorRut) throws Exception {
		logger.info("INICIO::PurgaProspectoCliente.actualizarDetallesArchivoEntradaSFTP");
		long inicio = System.currentTimeMillis();
		
		Map<String, LineaSalida> hashLineasArchivoSalida = lineasArchivoSalida.stream().filter(linea -> linea.TipoEstado == TipoEstado.ELIMINACION_EXITOSA.Codigo).collect(Collectors.toMap(l -> l.IdSolicitud, l -> l, (a, b) -> a));

		HashMap<Long, Object> hashDetalles = Utils.List2Hash(detalles, "IdDetalle");
	    
		for (LineaEntrada lineaEntrada : lineasArchivoEntrada) {
	        Detalle detalle = (Detalle) hashDetalles.get(lineaEntrada.IdDetalle);
	        Persona persona = (Persona) personasPorRut.get(lineaEntrada.RutTitular);
			
	        if (detalle == null) {
				logger.error("Detalle no encontrado para IdDetalle {}", lineaEntrada.IdDetalle);
				continue;
			}
       
			if (hashLineasArchivoSalida.containsKey(lineaEntrada.IdSolicitud)) {
				if (persona != null) {
					detalle.IdTipoEntidad = "IdPersona";
					detalle.IdEntidad = persona.IdPersona;
				}
			}
		}

		SQL sql = new SQL();
		
		sql.batch(con,SQL_UPDATE_DETALLES , detalles ,Detalle.class);
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::PurgaProspectoCliente.actualizarDetallesArchivoEntradaSFTP {} ms", fin - inicio);
	}

	private Map<String, List<LineaEntrada>> agruparLineasValidasPorTipoBorrado(List<LineaEntrada> lineasEntrada) {
		logger.info("INICIO::PurgaProspectoCliente.agruparLineasValidasPorTipoBorrado");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, List<LineaEntrada>> detallesFiltrados = new HashMap<>();
		
		for (LineaEntrada lineaEntrada : lineasEntrada) {
	        if (lineaEntrada.esProcesable()) {
	            detallesFiltrados.computeIfAbsent(lineaEntrada.TipoBorrado, k -> new ArrayList<>()).add(lineaEntrada);
	        }
	    }
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::PurgaProspectoCliente.agruparLineasValidasPorTipoBorrado {} ms", fin - inicio);
		return detallesFiltrados;
	}

	private void agregarArchivoSalidaSFTP(Connection con, EntradaPurgaProspectoCliente entrada, List<LineaSalida> lineasArchivoSalida, String nombreArchivoSalida) throws Exception {
		logger.info("INICIO::PurgaProspectoCliente.agregarArchivoSalidaSFTP");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		
		List<Long> secuenciaCabecera = Utils.getSecuencia(con, "BSCL.Plataforma.BD.Secuencias::Cabecera", 1L);
		Long idCabecera = secuenciaCabecera.get(0);
		
		List<Cabecera> cabeceras = new ArrayList<Cabecera>();
		Cabecera cabecera = new Cabecera();
		cabecera.IdCabecera = idCabecera;
		cabecera.IdCodigoTipo = CODIGO_TIPO_SALIDA;
		cabecera.IdEstado = "PEN";
		cabecera.IdUsuario = entrada.p_IdUsuario;
		cabecera.NombreArchivo = nombreArchivoSalida + ".txt";
		cabecera.FechaCreacion = Fecha.toDateTime(entrada.p_FechaCarga);
		cabeceras.add(cabecera);
		
		sql.batch(con, SQL_INSERT_CABECERA_ARCHIVO_SALIDA, cabeceras, Cabecera.class);
		
		Detalle detalleEncabezado = new Detalle();
		detalleEncabezado.IdCabecera = idCabecera;
		detalleEncabezado.Linea = "id_solicitud,origen,fecha_respuesta,estado,motivo_Error,descripcion_Error";
		detalleEncabezado.IdTipoEntidad = "";
		detalleEncabezado.IdEntidad = 0L;

		List<Detalle> detalles = new ArrayList<>();
		detalles.add(detalleEncabezado);
		detalles.addAll(lineasArchivoSalida.stream().map( lineaSalida -> {
			Detalle detalle = new Detalle();
			detalle.IdCabecera = idCabecera;
			detalle.Linea = lineaSalida.IdSolicitud + "," + lineaSalida.Origen + "," + lineaSalida.FechaRespuesta + "," + lineaSalida.TipoEstado + "," + lineaSalida.TipoMotivo + "," + lineaSalida.Descripcion;
			detalle.IdTipoEntidad = "";
			detalle.IdEntidad = 0L;
			return detalle;
		}).collect(Collectors.toList()));

		sql.batch(con, SQL_INSERT_DETALLES_ARCHIVO_SALIDA, detalles, Detalle.class);
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::PurgaProspectoCliente.agregarArchivoSalidaSFTP {} ms", fin - inicio);
	}

	private List<LineaSalida> armarLineasArchivoSalida(Connection con, EntradaPurgaProspectoCliente entrada,  List<LineaEntrada> lineasEntrada) throws Exception {
		logger.info("INICIO::PurgaProspectoCliente.armarLineasArchivoSalida");
		long inicio = System.currentTimeMillis();
		List<LineaSalida> lineasArchivoSalida = new ArrayList<>();

		String fechaSistema = Fecha.toFormat(FechaDB.ObtenerFechaHoraTs(con, entrada.p_IdEmpresa),"yyyyMMddHHmmssSSS");
		for (LineaEntrada lineaEntrada : lineasEntrada) {
	
			EstadoProceso estadoProceso = lineaEntrada.EstadoProceso;

			if (estadoProceso == null || estadoProceso.TipoEstado == TipoEstado.SIN_ESTADO) {
				logger.error("Linea sin estado: IdDetalle={}, IdSolicitud={}", lineaEntrada.IdDetalle, lineaEntrada.IdSolicitud);
				estadoProceso = new EstadoProceso(TipoEstado.ERROR_GENERICO, TipoMotivo.NO_APLICA, "Error asociado a la ejecución del proceso.");
			}

			LineaSalida lineaSalida = new LineaSalida();
			lineaSalida.IdSolicitud = lineaEntrada.IdSolicitud;
			lineaSalida.Origen = ORIGEN;
			lineaSalida.FechaRespuesta = fechaSistema;
			lineaSalida.TipoEstado = estadoProceso.TipoEstado.Codigo;
			lineaSalida.TipoMotivo = estadoProceso.TipoMotivo.Codigo;
			lineaSalida.Descripcion = estadoProceso.Mensaje;

			lineasArchivoSalida.add(lineaSalida);
		}
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::PurgaProspectoCliente.armarLineasArchivoSalida {} ms", fin - inicio);
		return lineasArchivoSalida;
	}

	private List<Detalle> getDetalles(Connection con, List<Cabecera> cabeceras) throws Exception {
		logger.info("INICIO::PurgaProspectoCliente.getDetalles");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<>(1);
		List<Long> idCabeceras = cabeceras.stream().map(cabecera -> cabecera.IdCabecera).collect(Collectors.toList());
		params.put("IdCabecera", idCabeceras);
		List<Detalle> detalles = sql.query(con, SQL_SELECT_DETALLES, params, Detalle.class);

		long fin = System.currentTimeMillis();
		logger.info("FIN::PurgaProspectoCliente.getDetalles {} ms", fin - inicio);
		return detalles;
	}

	public List<Cabecera> getCabeceras(Connection con, String fechaEjecucion, String idEstado) throws Exception {
		logger.info("INICIO::PurgaProspectoCliente.getCabeceras");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<>(3);
		params.put("IdCodigoTipo", CODIGO_TIPO_ENTRADA);
		params.put("FechaEjecucion", fechaEjecucion);
		params.put("IdEstado", idEstado);
		List<Cabecera> cabeceras = sql.query(con, SQL_SELECT_CABECERA, params, Cabecera.class);
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::PurgaProspectoCliente.getCabeceras {} ms", fin - inicio);
		return cabeceras;
	}

	private String obtenerValorAtributo(Object objeto, String atributo) {
		try {
			Field field = objeto.getClass().getField(atributo);
			Object valor = field.get(objeto);
			return valor == null ? null : valor.toString();

		} catch (Exception e) {
			logger.error("No existe el atributo {} en {}", atributo, objeto.getClass().getSimpleName(), e);

			return null;
		}
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== START ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("master02","connections.properties")){
			Gson gson = Utils.getGsonBuilder();
			String test = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_FechaCarga\":\"2026-07-30\",\"p_IdModulo\":\"SOC\"}";
			
			PurgaProspectoCliente p1 = new PurgaProspectoCliente();
			EntradaPurgaProspectoCliente ObjIn = gson.fromJson(test, EntradaPurgaProspectoCliente.class);
			
			System.out.println("ENTRADA:");
			System.out.println(ObjIn.toString());
			try {
				SalidaPurgaProspectoCliente ObjOut = p1.logica(con, ObjIn);
				System.out.println(ObjOut.toString());
				
				con.rollback();				
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				con.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");

	}
}