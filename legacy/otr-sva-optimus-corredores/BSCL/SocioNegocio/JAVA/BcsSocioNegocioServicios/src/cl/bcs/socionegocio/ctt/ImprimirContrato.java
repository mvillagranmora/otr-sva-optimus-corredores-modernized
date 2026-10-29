package cl.bcs.socionegocio.ctt;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.plataforma.pla.utils.FechaDB;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.AcroFields.Item;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ImprimirContrato implements JavaCall {
	final static Logger logger = LogManager.getLogger(ImprimirContrato.class);

	private final String SQL_SELECT_TEMPLATE = loadResourceToString("cl/bcs/socionegocio/ctt/sql/ImprimirContratos_SelectTemplate.sql");
	private final String SQL_SELECT_DATOS_CORREDOR = loadResourceToString("cl/bcs/socionegocio/ctt/sql/ImprimirContratos_SelectDatosSocioNegocioCorredora.sql");
	private final String SQL_SELECT_IDENTIFICADOR_RELACION_PERSONA = loadResourceToString("cl/bcs/socionegocio/ctt/sql/ImprimirContratos_SelectIdentificadorRelacionPersona.sql");
	private final String SQL_SELECT_DATOS_PERSONA = loadResourceToString("cl/bcs/socionegocio/ctt/sql/ImprimirContratos_SelectDatosPersona.sql");
	private final String SQL_SELECT_DATOS_CLIENTE = loadResourceToString("cl/bcs/socionegocio/ctt/sql/ImprimirContratos_SelectDatosCliente.sql");
	private final String SQL_SELECT_DATOS_FATCA_PAIS_RESIDENCIA = loadResourceToString("cl/bcs/socionegocio/ctt/sql/ImprimirContratos_SelectFatcaPaisResidencia.sql");
	private final String SQL_SELECT_CUENTAS_BANCARIAS_PERSONA = loadResourceToString("cl/bcs/socionegocio/ctt/sql/ImprimirContratos_SelectCuentasBancariasPersona.sql");
	private final String SQL_SELECT_PERFIL_PRODUCTO_PORTAFOLIO = loadResourceToString("cl/bcs/socionegocio/ctt/sql/ImprimirContratos_SelectPerfilProductosPortafolio.sql");
	private final String SQL_SELECT_DETALLE_TIPO_INDICADOR = loadResourceToString("cl/bcs/socionegocio/ctt/sql/ImprimirContratos_SelectDetalleTipoIndicador.sql");
	private final String SQL_SELECT_APODERADOS_FIRMANTES = loadResourceToString("cl/bcs/socionegocio/ctt/sql/ImprimirContratos_SelectApoderadosFirmantes.sql");
	private final String SQL_SELECT_CONYUGE_APODERADOS_FIRMANTES = loadResourceToString("cl/bcs/socionegocio/ctt/sql/ImprimirContratos_SelectConyugeApoderadoFirmante.sql");

	public static class TipoIndicador {
		public final static String CODIGO_MODALIDAD_CAPTACION = "ORDMODALIDAD";
	}

	public static class ImprimirContrato_Entrada extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;

		public String p_DocumentoIdentidad;
		public String p_TipoDocumentoIdentidad;

		public String p_CodigoContrato;
	}

	public static class ImprimirContrato_Salida extends BcsPojo {
		public String p_out_ContratoB64;
		public String p_out_NombreContrato;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;

		public ImprimirContrato_Salida() {
			P_OUT_MENSAJES = new ArrayList<>();
			P_OUT_ERRORESCONTADOR = 0L;
		}
	}

	public static class ArchivoContrato extends BcsPojo {
		public Long IdEmpresa;
		public byte[] Binario;
		public String TipoBinario;
		public String EsEstatico;
		public String TipoPresentacion;
		public String CodigoArchivo;
		public String Descripcion;
	}

	public static class DetalleTipoIndicador extends BcsPojo {
		public Long IdDetalleIndicador;
		public String CodigoTipoIndicador;
		public String CodigoValor;
		public String DetalleValor;
	}

	public static class IdentificadorRelacionado extends BcsPojo {
		public Long OrdenRegistro;
		public Long IdPersonaOrigen;
		public Long IdPersonaRelacionada;
		public String TipoRelacion;
	}

	public static class CuentaBancaria extends BcsPojo {
		public Long IdPersona;
		public Long OrdenRegistro;
		public String IdMoneda;
		public String NombreBanco;
		public String DocumentoIdentidadBanco;
		public String NombreTipoCuentaBancaria;
		public String NumeroCuentaBancaria;
		public String NombreMoneda;
		public String SimboloMoneda;
		public String CodigoPais;
		public String NombrePais;
		public String Sucursal;
		public String EsFatca;
	}

	public static class DatosPersona extends BcsPojo {
		public Long IdPersona;
		public Long IdPersonaConyuge;
		public String NombreCompleto;
		public String RazonSocial;
		public String DocumentoIdentidad;
		public String DigitoVerificador;
		public String Profesion;
		public String NombreGiro;
		public String DireccionElectronicaPrincipal;
		public String DireccionElectronicaLaboral;
		public String DireccionElectronicaComercialPersona;
		public String ApellidoPaterno;
		public String ApellidoMaterno;
		public String Nombres;
		public String DireccionFisicaCalle;
		public String DireccionFisicaNumero;
		public String DireccionFisicaNombreCiudad;
		public String DireccionFisicaNombreComuna;
		public String DireccionFisicaNombreRegion;
		public String DireccionFisicaComercial;
		public String DireccionFisicaComercialNombreCiudad;
		public String DireccionFisicaComercialNombreComuna;
		public String DireccionFisicaComercialNombreRegion;
		public String DireccionFisicaComercialNombrePais;
		public String DireccionFisicaLaboral;
		public String DireccionFisicaLaboralNombreCiudad;
		public String DireccionFisicaLaboralNombreComuna;
		public String DireccionFisicaLaboralNombreRegion;
		public String DireccionFisicaLaboralNombrePais;
		public String NombrePaisNacionalidadPrincipal;
		public String TipoTelefonoPrincipal;
		public String NumeroTelefonoPrincipal;
		public String NumeroTelefonoFijo;
		public String NumeroTelefonoMovil;
		public String DireccionFisicaNombrePais;
		public String EstadoCivil;
		public String CodigoEstadoCivil;
		public String RegimenConyugal;
		public String IdEstado;
		public String FechaIngreso;
		public String FechaNacimiento;
		public String HoraIngreso;
		public String NombreRegimenConyugal;
		public String SexoCorto;
		public String Sexo;
		public String TipoPersona;
		public String NombrePaisNacimiento;
		public String NombrePaisResidencia;
		public String NombrePaisDireccionFisica;
		public String IdSituacionLaboral;
		public String NombreFantasia;
		public String GlobalLegalEntity;
		public String IndicadorNCG380;
		public String IndicadorKnowYourCustomer;
		public String Cargo;
		public String NombreGrupoEmpresarial;
		public String DireccionElectronicaComercial;
		public String RangoRenta;
		public String RangoNivelVenta;
		public String PropositoTransaccion;
		public String BCOCARGO1_1;
		public String NCTACARGO1 ;
		public String TCTACARGO1 ;
		public String BCOCARGO2_2;
		public String NCTACARGO2 ;
		public String TCTACARGO2 ;
		public String BCOCARGO3_3;
		public String NCTACARGO3 ;
		public String TCTACARGO3 ;
		public String BCOCARGO4_4;
		public String NCTACARGO4 ;
		public String TCTACARGO4 ;
		// No serializables
		public transient Map<Long, CuentaBancaria> CuentaBancariaPorOrden;

		public DatosPersona() {
			CuentaBancariaPorOrden = new HashMap<>(8);
		}

		boolean esPersonaNatural() {
			return "N".equalsIgnoreCase(TipoPersona);
		}
	}

	public static class DatosCliente extends BcsPojo {
		public Long IdPersona;
		public Long IdCliente;
		public String DocumentoIdentidadEjecutivo;
		public String CodigoEjecutivo;
		public String NombreEjecutivo;
		public String ApellidoPaternoEjecutivo;
		public String ApellidoMaternoEjecutivo;
		public Long IdSucursalPreferencia;
		public String CodigoSucursalPreferencia;
		public String NombreSucursalPreferencia;

		public Long IdPortafolio;
		public String CodigoPortafolio;
		public String EstadoModalidadPorCodigoJSONString;
		public String ModalidadesCaptacion;
		public String IdDireccionElectronicaNotificacionPorDefecto;
		public String DireccionElectronicaNotificacionPorDefecto;
		
		public String IndicadorAntecedentesLegales;
		public String IndicadorInversionistaCalificado;
		public String IndicadorPersonaPoliticamenteExpuesta;
		public String IndicadorCategoriaInversionista;
		public String IndicadorClienteFATCA;
		public String IndicadorClienteInfomacionPrivilegiada;
		public String IndicadorNGC380;
		public String IndicadorAdministracionCartera;
		public String IndicadorNeteaAbonosCargos;
		public String IndicadorCustodiaRF;
		public String IndicadorCustodiaRV;
		public String FolioContrato;

		public DatosCliente() {
			EstadoModalidadPorCodigoJSONString = "{}";
			ModalidadesCaptacion = "";
		}
	}
	
	public static class DatosPerfilProductosPortafolio extends BcsPojo {
		public String IdTipoProducto;
		public String NombreEstadoProductos;
		public String IdTipoOperacion;
		public String IdCanalIngreso;
		public String CobroComisionCodigoCobroClasificacion;
		public BigDecimal CobroComisionValorCobro;
		public String CobroComisionFormaCobro;
		public String CobroGastoCodigoCobroClasificacion;
		public BigDecimal CobroGastoValorCobro;
		public String CobroGastoFormaCobro;
		public String CobroDerechoCodigoCobroClasificacion;
		public BigDecimal CobroDerechoValorCobro;
		public String CobroDerechoFormaCobro;
	}
	
	public static class DatosCorredora extends BcsPojo {
		public Long IdPersona;
		public String DocumentoIdentidad;
		public String RazonSocial;
		public String GiroComercial;
		public String DireccionFisicaPrincipal;
		public String NombreComuna;
		public String NombreCiudad;
		public String NombrePais;
		public String NumeroTelefono;
		public String Fax;
		public String DireccionElectronicaPrincipal;
		public String NumeroDireccion;
		public String DocumentoIdentidadRepresentante;
		public String NombreRepresentante;
		public String DireccionFisicaRepresentante;
		public String NumeroDireccionRepresentante;
		public String DocumentoIdentidadApoderado;
		public String NombreApoderado;
		public String CodigoCorredora;
	}
	
	public static class DatosFatcaPaisResidencia extends BcsPojo {
		public String PaisResidencia;
		public String TIN;
		public String CausaNoTin;
	}
	
	public static class DatosApoderadoFirmante extends BcsPojo {
		public Long   IdPersona;
		public Long   IdPersonaConyuge;
		public String NombreCompleto;
		public String TipoDocumento;
		public String DocumentoIdentidad;
		public String NombrePaisNacionalidadPrincipal;
		public String DireccionElectronicaPrincipal;
		public String NumeroTelefonoPrincipal;
		public String RelacionCorredora;
		public String Cargo;
		public String Nombres;
		public String ApellidoPaterno;
		public String ApellidoMaterno;
		public String DireccionParticular;
		public String DireccionParticularCiudad;
		public String DireccionParticularComuna;
		public String EstadoCivil;
	}
	
	public static class DatosConyugeApoderadoFirmante extends BcsPojo {
		public Long   IdPersona;
		public Long   IdPersonaConyuge;
		public String DocumentoIdentidad;
		public String NombreCompleto;
		public String Nombres;
		public String ApellidoPaterno;
		public String ApellidoMaterno;
		public String NombrePaisNacionalidadPrincipal;
		public String RelacionCorredora;
	}
	
	public ImprimirContrato_Salida logica(Connection conn,ImprimirContrato_Entrada entrada) throws Exception {
		logger.info("INICIO::ImprimirContrato");
		long inicioServicio = System.currentTimeMillis();

		ImprimirContrato_Salida salida = new ImprimirContrato_Salida();
		
		String fechaSistema = Utils.getFechaSistema(conn, Utils.getSchema(), entrada.p_IdEmpresa);
		String horaSistema = FechaDB.ObtenerHoraSistemaLocal(conn, entrada.p_IdEmpresa);
		SimpleDateFormat d = new SimpleDateFormat("dd-MM-yyyy");
		Date date = Fecha.toDate(fechaSistema);
		fechaSistema = d.format(date);
		String[] separacionFecha = fechaSistema.split("-");
		String diaFechaSistema = separacionFecha[0];
		String mesFechaSistema = separacionFecha[1];
		String anioFechaSistema = separacionFecha[2];

		SQL _sql = new SQL();
		HashMap<String,Object> params = new HashMap<>();
		params.put("IdEmpresa",entrada.p_IdEmpresa);
		params.put("CodigoArchivo",entrada.p_CodigoContrato);
		List<ArchivoContrato> contrato = _sql.query(conn, SQL_SELECT_TEMPLATE, params, ArchivoContrato.class);
		
		//DATOS CORREDORA
		DatosCorredora corredora;
		{
			params.clear();
			params.put("IdEmpresa", entrada.p_IdEmpresa);
			corredora = _sql.query(conn, SQL_SELECT_DATOS_CORREDOR, params, DatosCorredora.class).stream().findFirst().orElse(new DatosCorredora());
		}


		// Personas relacionadas socio
		DatosPersona socioPersona = null;
		DatosCliente socioCliente;

		DatosPersona socioConyuge = new DatosPersona();
		DatosPersona socioEmpleador = new DatosPersona();
		DatosPersona socioRepresentante1 = new DatosPersona();
		DatosPersona socioRepresentante2 = new DatosPersona();
		DatosPersona socioRepresentante3 = new DatosPersona();
		DatosPersona socioRepresentante1Conyuge = new DatosPersona();
		DatosPersona socioRepresentante2Conyuge = new DatosPersona();
		DatosPersona socioRepresentante3Conyuge = new DatosPersona();
		DatosPersona socioApoderado1 = new DatosPersona();
		DatosPersona socioApoderado2 = new DatosPersona();
		DatosPersona socioApoderado3 = new DatosPersona();
		DatosPersona socioApoderado4 = new DatosPersona();
		DatosPersona socioApoderado5 = new DatosPersona();
		DatosPersona socioAutorizadoOrdenes1 = new DatosPersona();
		DatosPersona socioAutorizadoOrdenes2 = new DatosPersona();
		DatosPersona socioAutorizadoOrdenes3 = new DatosPersona();
		DatosPersona socioAutorizadoOrdenes4 = new DatosPersona();
		DatosPersona socioAutorizadoOrdenes5 = new DatosPersona();
		DatosPersona socioAutorizadoOrdenes6 = new DatosPersona();
		
		
		
		{
			logger.debug("INICIO::ImprimirContrato.ConsultarSocioContratoConRelacionados");
			long stopwatchStart = System.currentTimeMillis();

			params.clear();
			params.put("DocumentoIdentidad", entrada.p_DocumentoIdentidad);
			Map<Long, List<IdentificadorRelacionado>> identificadorRelacionesPorIdPersonaRelacionada = _sql
					.query(conn, SQL_SELECT_IDENTIFICADOR_RELACION_PERSONA, params, IdentificadorRelacionado.class)
					.stream()
					.collect(Collectors.groupingBy(
							u -> u.IdPersonaRelacionada,
							Collectors.toList()
					));

			params.clear();
			params.put("PersonaIds", new ArrayList<>(identificadorRelacionesPorIdPersonaRelacionada.keySet()));
			params.put("DocumentoIdentidad", entrada.p_DocumentoIdentidad);
			List<DatosPersona> relacionados = _sql.query(conn, SQL_SELECT_DATOS_PERSONA, params, DatosPersona.class);

			String identificadorCompuesto;
			for (DatosPersona relacionado : relacionados) {
				for (IdentificadorRelacionado identificadorRelacionado : identificadorRelacionesPorIdPersonaRelacionada.getOrDefault(relacionado.IdPersona, Collections.emptyList())) {
					identificadorCompuesto = identificadorRelacionado.TipoRelacion + "-" + identificadorRelacionado.OrdenRegistro;

					if ("SELF-1".equalsIgnoreCase(identificadorCompuesto)) {
						socioPersona = relacionado;
						continue;
					}
					if ("CONYUGESOCIO-1".equalsIgnoreCase(identificadorCompuesto)) {
						socioConyuge = relacionado;
						continue;
					}
					if ("REP-1".equalsIgnoreCase(identificadorCompuesto)) {
						socioRepresentante1 = relacionado;
						continue;
					}
					if ("REP-2".equalsIgnoreCase(identificadorCompuesto)) {
						socioRepresentante2 = relacionado;
						continue;
					}
					if ("REP-3".equalsIgnoreCase(identificadorCompuesto)) {
						socioRepresentante3 = relacionado;
						continue;
					}
					if ("CONYUGEREP-1".equalsIgnoreCase(identificadorCompuesto)) {
						socioRepresentante1Conyuge = relacionado;
						continue;
					}
					if ("CONYUGEREP-2".equalsIgnoreCase(identificadorCompuesto)) {
						socioRepresentante2Conyuge = relacionado;
						continue;
					}
					if ("CONYUGEREP-3".equalsIgnoreCase(identificadorCompuesto)) {
						socioRepresentante3Conyuge = relacionado;
						continue;
					}
					if ("APO-1".equalsIgnoreCase(identificadorCompuesto)) {
						socioApoderado1 = relacionado;
						continue;
					}
					if ("APO-2".equalsIgnoreCase(identificadorCompuesto)) {
						socioApoderado2 = relacionado;
						continue;
					}
					if ("APO-3".equalsIgnoreCase(identificadorCompuesto)) {
						socioApoderado3 = relacionado;
						continue;
					}
					if ("APO-4".equalsIgnoreCase(identificadorCompuesto)) {
						socioApoderado4 = relacionado;
						continue;
					}
					if ("APO-5".equalsIgnoreCase(identificadorCompuesto)) {
						socioApoderado5 = relacionado;
						continue;
					}
					if ("EMPLEADORSOCIO-1".equalsIgnoreCase(identificadorCompuesto)) {
						socioEmpleador = relacionado;
						continue;
					}
					if ("AUTZDO-1".equalsIgnoreCase(identificadorCompuesto)) {
						socioAutorizadoOrdenes1 = relacionado;
						continue;
					}
					if ("AUTZDO-2".equalsIgnoreCase(identificadorCompuesto)) {
						socioAutorizadoOrdenes2 = relacionado;
						continue;
					}
					if ("AUTZDO-3".equalsIgnoreCase(identificadorCompuesto)) {
						socioAutorizadoOrdenes3 = relacionado;
						continue;
					}
					if ("AUTZDO-4".equalsIgnoreCase(identificadorCompuesto)) {
						socioAutorizadoOrdenes4 = relacionado;
						continue;
					}	
					if ("AUTZDO-5".equalsIgnoreCase(identificadorCompuesto)) {
						socioAutorizadoOrdenes5 = relacionado;
						continue;
					}		
					if ("AUTZDO-6".equalsIgnoreCase(identificadorCompuesto)) {
						socioAutorizadoOrdenes6 = relacionado;
						continue;
					}						
					
				}
			}

			if (socioPersona == null) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "DOCUMENTO_IDENTIDAD_NO_EXISTE", new HashMap<>(), conn, entrada.p_IdEmpresa));
				return salida;
			}

			params.clear();
			params.put("IdPersona", socioPersona.IdPersona);
			params.put("IdEmpresa", entrada.p_IdEmpresa);
			params.put("DocumentoIdentidad", entrada.p_DocumentoIdentidad);
			socioCliente = _sql.query(conn, SQL_SELECT_DATOS_CLIENTE, params, DatosCliente.class).stream().findFirst().orElse(new DatosCliente());

			long stopwatchEnd = System.currentTimeMillis();
			logger.debug("FIN::ImprimirContrato.ConsultarSocioContratoConRelacionados {}", stopwatchEnd - stopwatchStart);
		}
		
		// Perfil productos y cobros
		DatosPerfilProductosPortafolio datosPerfilProductosPortafolioINAC = new DatosPerfilProductosPortafolio();
		DatosPerfilProductosPortafolio datosPerfilProductosPortafolioINAX = new DatosPerfilProductosPortafolio();
		DatosPerfilProductosPortafolio datosPerfilProductosPortafolioINCI = new DatosPerfilProductosPortafolio();
		DatosPerfilProductosPortafolio datosPerfilProductosPortafolioINCM = new DatosPerfilProductosPortafolio();
		DatosPerfilProductosPortafolio datosPerfilProductosPortafolioINEN = new DatosPerfilProductosPortafolio();
		DatosPerfilProductosPortafolio datosPerfilProductosPortafolioINFU = new DatosPerfilProductosPortafolio();
		DatosPerfilProductosPortafolio datosPerfilProductosPortafolioINFM = new DatosPerfilProductosPortafolio();
		DatosPerfilProductosPortafolio datosPerfilProductosPortafolioINIF = new DatosPerfilProductosPortafolio();
		DatosPerfilProductosPortafolio datosPerfilProductosPortafolioINMO = new DatosPerfilProductosPortafolio();
		DatosPerfilProductosPortafolio datosPerfilProductosPortafolioINRF = new DatosPerfilProductosPortafolio();
		DatosPerfilProductosPortafolio datosPerfilProductosPortafolioINSI = new DatosPerfilProductosPortafolio();
		DatosPerfilProductosPortafolio datosPerfilProductosPortafolioMEFW = new DatosPerfilProductosPortafolio();
		DatosPerfilProductosPortafolio datosPerfilProductosPortafolioMEME = new DatosPerfilProductosPortafolio();
		DatosPerfilProductosPortafolio datosPerfilProductosPortafolioMERF = new DatosPerfilProductosPortafolio();
		
		DatosPerfilProductosPortafolio datosPerfilProductosPortafolioIntINAC = new DatosPerfilProductosPortafolio();
		DatosPerfilProductosPortafolio datosPerfilProductosPortafolioIntINAX = new DatosPerfilProductosPortafolio();
		DatosPerfilProductosPortafolio datosPerfilProductosPortafolioIntINCI = new DatosPerfilProductosPortafolio();
		DatosPerfilProductosPortafolio datosPerfilProductosPortafolioIntINCM = new DatosPerfilProductosPortafolio();
		DatosPerfilProductosPortafolio datosPerfilProductosPortafolioIntINEN = new DatosPerfilProductosPortafolio();
		DatosPerfilProductosPortafolio datosPerfilProductosPortafolioIntINFU = new DatosPerfilProductosPortafolio();
		DatosPerfilProductosPortafolio datosPerfilProductosPortafolioIntINFM = new DatosPerfilProductosPortafolio();
		DatosPerfilProductosPortafolio datosPerfilProductosPortafolioIntINIF = new DatosPerfilProductosPortafolio();
		DatosPerfilProductosPortafolio datosPerfilProductosPortafolioIntINMO = new DatosPerfilProductosPortafolio();
		DatosPerfilProductosPortafolio datosPerfilProductosPortafolioIntINRF = new DatosPerfilProductosPortafolio();
		DatosPerfilProductosPortafolio datosPerfilProductosPortafolioIntINSI = new DatosPerfilProductosPortafolio();
		DatosPerfilProductosPortafolio datosPerfilProductosPortafolioIntMEFW = new DatosPerfilProductosPortafolio();
		DatosPerfilProductosPortafolio datosPerfilProductosPortafolioIntMEME = new DatosPerfilProductosPortafolio();
		DatosPerfilProductosPortafolio datosPerfilProductosPortafolioIntMERF = new DatosPerfilProductosPortafolio();
		
		params.clear();
		params.put("IdPortafolio", socioCliente.IdPortafolio);
		List<DatosPerfilProductosPortafolio> datosPerfilProductosPortafolio = _sql.query(conn, SQL_SELECT_PERFIL_PRODUCTO_PORTAFOLIO, params, DatosPerfilProductosPortafolio.class);
		
		for (DatosPerfilProductosPortafolio datosProducto : datosPerfilProductosPortafolio) {
			
			if(datosProducto.IdCanalIngreso != null && "CLI".equalsIgnoreCase(datosProducto.IdCanalIngreso)) { 
				//TAGS de perfil de productos con prefijo INT (Internet)
				switch (datosProducto.IdTipoProducto) {
					case "INAC":
						datosPerfilProductosPortafolioIntINAC = datosProducto;
						continue;
					case "INAX":
						datosPerfilProductosPortafolioIntINAX = datosProducto;
						continue;
					case "INCI":
						datosPerfilProductosPortafolioIntINCI = datosProducto;
						continue;
					case "INCM":
						datosPerfilProductosPortafolioIntINCM = datosProducto;
						continue;
					case "INEN":
						datosPerfilProductosPortafolioIntINEN = datosProducto;
						continue;
					case "INFU":
						datosPerfilProductosPortafolioIntINFU = datosProducto;
						continue;
					case "INFM":
						datosPerfilProductosPortafolioIntINFM = datosProducto;
						continue;
					case "INIF":
						datosPerfilProductosPortafolioIntINIF = datosProducto;
						continue;
					case "INMO":
						datosPerfilProductosPortafolioIntINMO = datosProducto;
						continue;
					case "INRF":
						datosPerfilProductosPortafolioIntINRF = datosProducto;
						continue;
					case "INSI":
						datosPerfilProductosPortafolioIntINSI = datosProducto;
						continue;
					case "MEFW":
						datosPerfilProductosPortafolioIntMEFW = datosProducto;
						continue;
					case "MEME":
						datosPerfilProductosPortafolioIntMEME = datosProducto;
						continue;
					case "MERF":
						datosPerfilProductosPortafolioIntMERF = datosProducto;
						continue;
				}
			}
			
			switch (datosProducto.IdTipoProducto) {
				case "INAC":
					datosPerfilProductosPortafolioINAC = datosProducto;
					continue;
				case "INAX":
					datosPerfilProductosPortafolioINAX = datosProducto;
					continue;
				case "INCI":
					datosPerfilProductosPortafolioINCI = datosProducto;
					continue;
				case "INCM":
					datosPerfilProductosPortafolioINCM = datosProducto;
					continue;
				case "INEN":
					datosPerfilProductosPortafolioINEN = datosProducto;
					continue;
				case "INFU":
					datosPerfilProductosPortafolioINFU = datosProducto;
					continue;
				case "INFM":
					datosPerfilProductosPortafolioINFM = datosProducto;
					continue;
				case "INIF":
					datosPerfilProductosPortafolioINIF = datosProducto;
					continue;
				case "INMO":
					datosPerfilProductosPortafolioINMO = datosProducto;
					continue;
				case "INRF":
					datosPerfilProductosPortafolioINRF = datosProducto;
					continue;
				case "INSI":
					datosPerfilProductosPortafolioINSI = datosProducto;
					continue;
				case "MEFW":
					datosPerfilProductosPortafolioMEFW = datosProducto;
					continue;
				case "MEME":
					datosPerfilProductosPortafolioMEME = datosProducto;
					continue;
				case "MERF":
					datosPerfilProductosPortafolioMERF = datosProducto;
					continue;
			}
		}
		
		//Fatca Paises de Residencia
		List<DatosFatcaPaisResidencia> paisesDeResidenciaFatca = new ArrayList<DatosFatcaPaisResidencia>();
		{
			params.clear();
			params.put("DocumentoIdentidad", entrada.p_DocumentoIdentidad);
			paisesDeResidenciaFatca = _sql.query(conn, SQL_SELECT_DATOS_FATCA_PAIS_RESIDENCIA, params, DatosFatcaPaisResidencia.class);
		}

		{
			params.clear();
			params.put("PersonaIds", new ArrayList<>(Arrays.asList(socioPersona.IdPersona)));
			Map<Long, Map<Long, CuentaBancaria>> cuentaBancariaPorIdOrdenPorIdPersona = _sql.query(conn, SQL_SELECT_CUENTAS_BANCARIAS_PERSONA, params, CuentaBancaria.class)
					.stream()
					.collect(Collectors.groupingBy(
							u -> u.IdPersona,
							Collectors.toMap(u -> u.OrdenRegistro, u -> u)
					));
			socioPersona.CuentaBancariaPorOrden = cuentaBancariaPorIdOrdenPorIdPersona.getOrDefault(socioPersona.IdPersona, new HashMap<>());
		}

		// Obtener detalles de tipo indicador
		Map<String, Map<String, String>> labelPorCodigoValorPorCodigoIndicador;
		{
			params.clear();
			params.put("IdEmpresa", entrada.p_IdEmpresa);
			params.put("TipoIndicadorCodigos", new ArrayList<>(Arrays.asList(TipoIndicador.CODIGO_MODALIDAD_CAPTACION)));
			labelPorCodigoValorPorCodigoIndicador = _sql.query(conn, SQL_SELECT_DETALLE_TIPO_INDICADOR, params, DetalleTipoIndicador.class)
					.stream()
					.collect(Collectors.groupingBy(
							u -> u.CodigoTipoIndicador,
							Collectors.toMap(u -> u.CodigoValor, u -> u.DetalleValor)
					));
		}
		
		//Datos de Apoderados Firmantes
		List<DatosApoderadoFirmante> datosApoderadoFirmante = new ArrayList<DatosApoderadoFirmante>();
		{
			params.clear();
			params.put("DocumentoIdentidad", entrada.p_DocumentoIdentidad);
			datosApoderadoFirmante = _sql.query(conn, SQL_SELECT_APODERADOS_FIRMANTES, params, DatosApoderadoFirmante.class);
		}
		
		List<DatosConyugeApoderadoFirmante> datosConyugeApoderadoFirmante = new ArrayList<DatosConyugeApoderadoFirmante>();
		{
			params.clear();
			List<Long> IdsPersonaConyuge = datosApoderadoFirmante.stream().map(p -> p.IdPersonaConyuge).filter(id -> id != null && id != 0L).collect(Collectors.toList());
			params.put("Idpersonas", IdsPersonaConyuge);
			datosConyugeApoderadoFirmante = _sql.query(conn, SQL_SELECT_CONYUGE_APODERADOS_FIRMANTES, params, DatosConyugeApoderadoFirmante.class);
		}

		// Mapear valores de indicadores tipo MULTI a texto con sus labels respectivos
		{
			final Gson gson = Utils.getGsonBuilder();
			final String delimiter = ", ";
			Map<String, Boolean> estadoModalidadPorCodigo = gson.fromJson(socioCliente.EstadoModalidadPorCodigoJSONString, new TypeToken<Map<String, Boolean>>() {}.getType());
			socioCliente.ModalidadesCaptacion = obtenerTextoValorMultiple(estadoModalidadPorCodigo, labelPorCodigoValorPorCodigoIndicador.getOrDefault(TipoIndicador.CODIGO_MODALIDAD_CAPTACION, Collections.emptyMap()), delimiter);
		}

		//Reader reader = new FileReader("d:\\ContratoMBI.pdf");
		//FileInputStream reader = new FileInputStream(new File("3.Contrato PJ_v07 20171226.pdf"));
		//byte[] asdf = IOUtils.toByteArray(reader);

		PdfReader pdfReader = new PdfReader(contrato.get(0).Binario);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		PdfStamper stamper = new PdfStamper(pdfReader, baos);
		AcroFields formx = stamper.getAcroFields();
		Map<String, Item> fields = formx.getFields();

		//Informacion de la Corredora
		formx.setField("CORREDORAADMINISTRADORAFONDOSMUTUOS",""); //Corredora Administradora Fondos Mutuos
		formx.setField("CORREDORACALLCENTER",""); // Corredora Call Center
		formx.setField("CORREDORACIUDADCORREDORA",(corredora.NombreCiudad!=null)? corredora.NombreCiudad:""); // Corredora Ciudad Corredora		
		formx.setField("CORREDORACODIGO",(corredora.CodigoCorredora!=null)? corredora.CodigoCorredora:""); //Corredora Codigo
		formx.setField("CORREDORACOMUNACORREDORA",(corredora.NombreComuna!=null)? corredora.NombreComuna:""); // Corredora Comuna Corredora
		formx.setField("CORREDORADIRECCION",((corredora.DireccionFisicaPrincipal!=null)? corredora.DireccionFisicaPrincipal:"") + " " + ((corredora.NumeroDireccion!=null)? corredora.NumeroDireccion:""));// Corredora Direccion
		formx.setField("CORREDORAEMAILCONTACTO",(corredora.DireccionElectronicaPrincipal!=null)? corredora.DireccionElectronicaPrincipal:""); // Corredora Email Contacto		
		formx.setField("CORREDORAFAX",(corredora.Fax!=null)? corredora.Fax:""); // Corredora Fax
		formx.setField("CORREDORAGIROCOMERCIAL",(corredora.GiroComercial!=null)? corredora.GiroComercial:""); // Corredora Giro Comercial		
		formx.setField("CORREDORAHOMEPAGE",""); // Corredora Pagina Web
		formx.setField("CORREDORAHORARIOATENCION", ""); // Corredora Horario Atencion
		formx.setField("CORREDORANOMBRE", (corredora.RazonSocial!=null)?  corredora.RazonSocial:""); // Corredora Nombre		
		formx.setField("CORREDORANOMBREGERENTEGENERAL", ""); // Corredora Nombre Gerente General
		formx.setField("CORREDORATELEFONO", (corredora.NumeroTelefono!=null)?  corredora.NumeroTelefono:""); // Corredora Telefono		
		formx.setField("CORREDORANOMBRELARGO", (corredora.RazonSocial!=null)?  corredora.RazonSocial:""); // Corredora Nombre Largo		
		formx.setField("CORREDORANOMBREREPRESENTANTELEGAL", (corredora.NombreRepresentante!=null)? corredora.NombreRepresentante:""); // Corredora Nombre Representante Legal		
		formx.setField("CORREDORARUT", (corredora.DocumentoIdentidad!=null)? corredora.DocumentoIdentidad:""); // Corredora Rut		
		formx.setField("CORREDORARUTREPRESENTANTELEGAL", (corredora.DocumentoIdentidadRepresentante!=null)? corredora.DocumentoIdentidadRepresentante:""); // Corredora Rut Representante Legal
		formx.setField("CORREDORADIRECCIONREPRESENTANTELEGAL",((corredora.DireccionFisicaRepresentante!=null)? corredora.DireccionFisicaRepresentante:"") + " " + ((corredora.NumeroDireccionRepresentante!=null)? corredora.NumeroDireccionRepresentante:""));// Corredora Direccion Representante Legal
		formx.setField("CORREDORANOMBREAPODERADO", (corredora.NombreApoderado!=null)? corredora.NombreApoderado:""); // Nombre Apoderado		
		formx.setField("CORREDORARUTAPODERADO", (corredora.DocumentoIdentidadApoderado!=null)? corredora.DocumentoIdentidadApoderado:""); // Rut Apoderado
		

		//Informacion Cuentas Bancarias Socio
		{
			CuentaBancaria cuentaBancaria = socioPersona.CuentaBancariaPorOrden.getOrDefault(1L, new CuentaBancaria());
			formx.setField("CUENTABAN_1BANCONOMBRE", (cuentaBancaria.NombreBanco!=null)? cuentaBancaria.NombreBanco:""); // Cuenta Bancaria Banco Nombre
			formx.setField("CUENTABAN_1BANCORUT", (cuentaBancaria.DocumentoIdentidadBanco!=null)? cuentaBancaria.DocumentoIdentidadBanco:""); // Cuenta Bancaria Banco Rut
			formx.setField("CUENTABAN_1BENEFICIARIO", ""); // Cuenta Bancaria Beneficiario
			formx.setField("CUENTABAN_1EMISOR", ""); // Cuenta Bancaria Emisor
			formx.setField("CUENTABAN_1MONEDA", (cuentaBancaria.IdMoneda!=null)? cuentaBancaria.IdMoneda:""); // Cuenta Bancaria Moneda
			formx.setField("CUENTABAN_1MONEDANEMO", (cuentaBancaria.IdMoneda!=null)? cuentaBancaria.IdMoneda:""); // Cuenta Bancaria Moneda Nemo
			formx.setField("CUENTABAN_1MONEDANOMBRE", (cuentaBancaria.NombreMoneda!=null)? cuentaBancaria.NombreMoneda:""); // Cuenta Bancaria Moneda Nombre
			formx.setField("CUENTABAN_1MONEDASIMBOLO", (cuentaBancaria.SimboloMoneda!=null)? cuentaBancaria.SimboloMoneda:""); // Cuenta Bancaria Moneda Simbolo
			formx.setField("CUENTABAN_1NUMERO", (cuentaBancaria.NumeroCuentaBancaria!=null)? cuentaBancaria.NumeroCuentaBancaria:""); // Cuenta Bancaria Numero
			formx.setField("CUENTABAN_1PAISISO", (cuentaBancaria.CodigoPais!=null)? cuentaBancaria.CodigoPais:""); // Cuenta Bancaria Pais Codigo ISO
			formx.setField("CUENTABAN_1PAISNOMBRE", (cuentaBancaria.NombrePais!=null)? cuentaBancaria.NombrePais:""); // Cuenta Bancaria Pais Nombre
			formx.setField("CUENTABAN_1SUCURSAL", (cuentaBancaria.Sucursal!=null)? cuentaBancaria.Sucursal:""); // Cuenta Bancaria Sucursal
			formx.setField("CUENTABAN_1TIPO", (cuentaBancaria.NombreTipoCuentaBancaria!=null)? cuentaBancaria.NombreTipoCuentaBancaria:""); // Cuenta Bancaria Tipo (Ahorro, Corriente, Vista, etc)
			formx.setField("CUENTABAN_1ESFACTA", (cuentaBancaria.EsFatca!=null)? cuentaBancaria.EsFatca:""); // Es Fatca
			if ("CUENTA CORRIENTE".equalsIgnoreCase(cuentaBancaria.NombreTipoCuentaBancaria)) {
				formx.setField("CUENTABANCORRI_1BANCONOMBRE",(cuentaBancaria.NombreBanco!=null)? cuentaBancaria.NombreBanco:"");// Cuenta Corriente Banco Nombre
				formx.setField("CUENTABANCORRI_1BANCORUT", (cuentaBancaria.DocumentoIdentidadBanco!=null)? cuentaBancaria.DocumentoIdentidadBanco:"");// Cuenta Corriente Banco Rut
				formx.setField("CUENTABANCORRI_1ENEFICIARIO","");// Cuenta Corriente Beneficiario
				formx.setField("CUENTABANCORRI_1EMISOR","");// Cuenta Corriente Emisor
				formx.setField("CUENTABANCORRI_1MONEDA",(cuentaBancaria.IdMoneda!=null)? cuentaBancaria.IdMoneda:"");// Cuenta Corriente Moneda
				formx.setField("CUENTABANCORRI_1MONEDANEMO",(cuentaBancaria.IdMoneda!=null)? cuentaBancaria.IdMoneda:"");// Cuenta Corriente Moneda Nemo
				formx.setField("CUENTABANCORRI_1MONEDANOMBRE",(cuentaBancaria.NombreMoneda!=null)? cuentaBancaria.NombreMoneda:"");// Cuenta Corriente Moneda Nombre
				formx.setField("CUENTABANCORRI_1MONEDASIMBOLO",(cuentaBancaria.SimboloMoneda!=null)? cuentaBancaria.SimboloMoneda:"");// Cuenta Corriente Moneda Simbolo
				formx.setField("CUENTABANCORRI_1NUMERO",(cuentaBancaria.NumeroCuentaBancaria!=null)? cuentaBancaria.NumeroCuentaBancaria:"");// Cuenta Corriente Numero
				formx.setField("CUENTABANCORRI_1PAISISO",(cuentaBancaria.CodigoPais!=null)? cuentaBancaria.CodigoPais:"");// Cuenta Corriente Pais Codigo ISO
				formx.setField("CUENTABANCORRI_1PAISNOMBRE",(cuentaBancaria.NombrePais!=null)? cuentaBancaria.NombrePais:"");// Cuenta Corriente Pais Nombre
				formx.setField("CUENTABANCORRI_1SUCURSAL",(cuentaBancaria.Sucursal!=null)? cuentaBancaria.Sucursal:"");// Cuenta Corriente Sucursal
				formx.setField("CUENTABANCORRI_1TIPO",(cuentaBancaria.NombreTipoCuentaBancaria!=null)? cuentaBancaria.NombreTipoCuentaBancaria:"");// Cuenta Corriente Tipo (Ahorro, Corriente, Vista, etc.)
				
			}
		}

		{
			CuentaBancaria cuentaBancaria = socioPersona.CuentaBancariaPorOrden.getOrDefault(2L, new CuentaBancaria());
			formx.setField("CUENTABAN_2BANCONOMBRE", (cuentaBancaria.NombreBanco!=null)? cuentaBancaria.NombreBanco:"");
			formx.setField("CUENTABAN_2BANCORUT", (cuentaBancaria.DocumentoIdentidadBanco!=null)? cuentaBancaria.DocumentoIdentidadBanco:"");// Cuenta Bancaria Banco Rut
			formx.setField("CUENTABAN_2BENEFICIARIO", "");// Cuenta Bancaria Beneficiario
			formx.setField("CUENTABAN_2EMISOR", "");// Cuenta Bancaria Emisor
			formx.setField("CUENTABAN_2MONEDA", (cuentaBancaria.IdMoneda!=null)? cuentaBancaria.IdMoneda:"");// Cuenta Bancaria Moneda
			formx.setField("CUENTABAN_2MONEDANEMO", (cuentaBancaria.IdMoneda!=null)? cuentaBancaria.IdMoneda:"");// Cuenta Bancaria Moneda Nemo
			formx.setField("CUENTABAN_2MONEDANOMBRE", (cuentaBancaria.NombreMoneda!=null)? cuentaBancaria.NombreMoneda:"");// Cuenta Bancaria Moneda Nombre
			formx.setField("CUENTABAN_2MONEDASIMBOLO", (cuentaBancaria.SimboloMoneda!=null)? cuentaBancaria.SimboloMoneda:"");// Cuenta Bancaria Moneda Simbolo
			formx.setField("CUENTABAN_2NUMERO", (cuentaBancaria.NumeroCuentaBancaria!=null)? cuentaBancaria.NumeroCuentaBancaria:"");// Cuenta Bancaria Numero
			formx.setField("CUENTABAN_2PAISISO", (cuentaBancaria.CodigoPais!=null)? cuentaBancaria.CodigoPais:"");// Cuenta Bancaria Pais Codigo ISO
			formx.setField("CUENTABAN_2PAISNOMBRE", (cuentaBancaria.NombrePais!=null)? cuentaBancaria.NombrePais:"");// Cuenta Bancaria Pais Nombre
			formx.setField("CUENTABAN_2SUCURSAL", (cuentaBancaria.Sucursal!=null)? cuentaBancaria.Sucursal:"");// Cuenta Bancaria Sucursal
			formx.setField("CUENTABAN_2TIPO", (cuentaBancaria.NombreTipoCuentaBancaria!=null)? cuentaBancaria.NombreTipoCuentaBancaria:"");// Cuenta Bancaria Tipo (Ahorro, Corriente, Vista, etc.)
			formx.setField("CUENTABAN_2ESFACTA", (cuentaBancaria.EsFatca!=null)? cuentaBancaria.EsFatca:""); // Es Fatca
			if ("CUENTA CORRIENTE".equalsIgnoreCase(cuentaBancaria.NombreTipoCuentaBancaria)) {
				formx.setField("CUENTABANCORRI_2BANCONOMBRE",(cuentaBancaria.NombreBanco!=null)? cuentaBancaria.NombreBanco:"");// Cuenta Corriente Banco Nombre
				formx.setField("CUENTABANCORRI_2BANCORUT",(cuentaBancaria.DocumentoIdentidadBanco!=null)? cuentaBancaria.DocumentoIdentidadBanco:"");// Cuenta Corriente Banco Rut
				formx.setField("CUENTABANCORRI_2ENEFICIARIO","");// Cuenta Corriente Beneficiario
				formx.setField("CUENTABANCORRI_2EMISOR","");// Cuenta Corriente Emisor
				formx.setField("CUENTABANCORRI_2MONEDA",(cuentaBancaria.IdMoneda!=null)? cuentaBancaria.IdMoneda:"");// Cuenta Corriente Moneda
				formx.setField("CUENTABANCORRI_2MONEDANEMO",(cuentaBancaria.IdMoneda!=null)? cuentaBancaria.IdMoneda:"");// Cuenta Corriente Moneda Nemo
				formx.setField("CUENTABANCORRI_2MONEDANOMBRE",(cuentaBancaria.NombreMoneda!=null)? cuentaBancaria.NombreMoneda:"");// Cuenta Corriente Moneda Nombre
				formx.setField("CUENTABANCORRI_2MONEDASIMBOLO",(cuentaBancaria.SimboloMoneda!=null)? cuentaBancaria.SimboloMoneda:"");// Cuenta Corriente Moneda Simbolo
				formx.setField("CUENTABANCORRI_2NUMERO",(cuentaBancaria.NumeroCuentaBancaria!=null)? cuentaBancaria.NumeroCuentaBancaria:"");// Cuenta Corriente Numero
				formx.setField("CUENTABANCORRI_2PAISISO",(cuentaBancaria.CodigoPais!=null)? cuentaBancaria.CodigoPais:"");// Cuenta Corriente Pais Codigo ISO
				formx.setField("CUENTABANCORRI_2PAISNOMBRE",(cuentaBancaria.NombrePais!=null)? cuentaBancaria.NombrePais:"");// Cuenta Corriente Pais Nombre
				formx.setField("CUENTABANCORRI_2SUCURSAL",(cuentaBancaria.Sucursal!=null)? cuentaBancaria.Sucursal:"");// Cuenta Corriente Sucursal
				formx.setField("CUENTABANCORRI_2TIPO",(cuentaBancaria.NombreTipoCuentaBancaria!=null)? cuentaBancaria.NombreTipoCuentaBancaria:"");// Cuenta Corriente Tipo (Ahorro, Corriente, Vista, etc.)
				
			}
		}

		{
			CuentaBancaria cuentaBancaria = socioPersona.CuentaBancariaPorOrden.getOrDefault(3L, new CuentaBancaria());
			formx.setField("CUENTABAN_3BANCONOMBRE", (cuentaBancaria.NombreBanco!=null)? cuentaBancaria.NombreBanco:"");// Cuenta Bancaria Banco Nombre
			formx.setField("CUENTABAN_3BANCORUT", (cuentaBancaria.DocumentoIdentidadBanco!=null)? cuentaBancaria.DocumentoIdentidadBanco:"");// Cuenta Bancaria Banco Rut
			formx.setField("CUENTABAN_3BENEFICIARIO", "");// Cuenta Bancaria Beneficiario
			formx.setField("CUENTABAN_3EMISOR", "");// Cuenta Bancaria Emisor
			formx.setField("CUENTABAN_3MONEDA", (cuentaBancaria.IdMoneda!=null)? cuentaBancaria.IdMoneda:"");// Cuenta Bancaria Moneda
			formx.setField("CUENTABAN_3MONEDANEMO", (cuentaBancaria.IdMoneda!=null)? cuentaBancaria.IdMoneda:"");// Cuenta Bancaria Moneda Nemo
			formx.setField("CUENTABAN_3MONEDANOMBRE", (cuentaBancaria.NombreMoneda!=null)? cuentaBancaria.NombreMoneda:"");// Cuenta Bancaria Moneda Nombre
			formx.setField("CUENTABAN_3MONEDASIMBOLO", (cuentaBancaria.SimboloMoneda!=null)? cuentaBancaria.SimboloMoneda:"");// Cuenta Bancaria Moneda Simbolo
			formx.setField("CUENTABAN_3NUMERO", (cuentaBancaria.NumeroCuentaBancaria!=null)? cuentaBancaria.NumeroCuentaBancaria:"");// Cuenta Bancaria Numero
			formx.setField("CUENTABAN_3PAISISO", (cuentaBancaria.CodigoPais!=null)? cuentaBancaria.CodigoPais:"");// Cuenta Bancaria Pais Codigo ISO
			formx.setField("CUENTABAN_3PAISNOMBRE", (cuentaBancaria.NombrePais!=null)? cuentaBancaria.NombrePais:"");// Cuenta Bancaria Pais Nombre
			formx.setField("CUENTABAN_3SUCURSAL", (cuentaBancaria.Sucursal!=null)? cuentaBancaria.Sucursal:"");// Cuenta Bancaria Sucursal
			formx.setField("CUENTABAN_3TIPO", (cuentaBancaria.NombreTipoCuentaBancaria!=null)? cuentaBancaria.NombreTipoCuentaBancaria:"");// Cuenta Bancaria Tipo (Ahorro, Corriente, Vista, etc.)
			formx.setField("CUENTABAN_3ESFACTA", (cuentaBancaria.EsFatca!=null)? cuentaBancaria.EsFatca:""); // Es Fatca			
			if ("CUENTA CORRIENTE".equalsIgnoreCase(cuentaBancaria.NombreTipoCuentaBancaria)) {
				formx.setField("CUENTABANCORRI_3BANCONOMBRE",(cuentaBancaria.NombreBanco!=null)? cuentaBancaria.NombreBanco:"");// Cuenta Corriente Banco Nombre
				formx.setField("CUENTABANCORRI_3BANCORUT",(cuentaBancaria.DocumentoIdentidadBanco!=null)? cuentaBancaria.DocumentoIdentidadBanco:"");// Cuenta Corriente Banco Rut
				formx.setField("CUENTABANCORRI_3ENEFICIARIO","");// Cuenta Corriente Beneficiario
				formx.setField("CUENTABANCORRI_3EMISOR","");// Cuenta Corriente Emisor
				formx.setField("CUENTABANCORRI_3MONEDA",(cuentaBancaria.IdMoneda!=null)? cuentaBancaria.IdMoneda:"");// Cuenta Corriente Moneda
				formx.setField("CUENTABANCORRI_3MONEDANEMO",(cuentaBancaria.IdMoneda!=null)? cuentaBancaria.IdMoneda:"");// Cuenta Corriente Moneda Nemo
				formx.setField("CUENTABANCORRI_3MONEDANOMBRE",(cuentaBancaria.NombreMoneda!=null)? cuentaBancaria.NombreMoneda:"");// Cuenta Corriente Moneda Nombre
				formx.setField("CUENTABANCORRI_3MONEDASIMBOLO",(cuentaBancaria.SimboloMoneda!=null)? cuentaBancaria.SimboloMoneda:"");// Cuenta Corriente Moneda Simbolo
				formx.setField("CUENTABANCORRI_3NUMERO",(cuentaBancaria.NumeroCuentaBancaria!=null)? cuentaBancaria.NumeroCuentaBancaria:"");// Cuenta Corriente Numero
				formx.setField("CUENTABANCORRI_3PAISISO",(cuentaBancaria.CodigoPais!=null)? cuentaBancaria.CodigoPais:"");// Cuenta Corriente Pais Codigo ISO
				formx.setField("CUENTABANCORRI_3PAISNOMBRE",(cuentaBancaria.NombrePais!=null)? cuentaBancaria.NombrePais:"");// Cuenta Corriente Pais Nombre
				formx.setField("CUENTABANCORRI_3SUCURSAL",(cuentaBancaria.Sucursal!=null)? cuentaBancaria.Sucursal:"");// Cuenta Corriente Sucursal
				formx.setField("CUENTABANCORRI_3TIPO",(cuentaBancaria.NombreTipoCuentaBancaria!=null)? cuentaBancaria.NombreTipoCuentaBancaria:"");// Cuenta Corriente Tipo (Ahorro, Corriente, Vista, etc.)
				
			}
		}
		
		{
			CuentaBancaria cuentaBancaria = socioPersona.CuentaBancariaPorOrden.getOrDefault(4L, new CuentaBancaria());
			formx.setField("CUENTABAN_4BANCONOMBRE", (cuentaBancaria.NombreBanco!=null)? cuentaBancaria.NombreBanco:"");// Cuenta Bancaria Banco Nombre
			formx.setField("CUENTABAN_4BANCORUT", (cuentaBancaria.DocumentoIdentidadBanco!=null)? cuentaBancaria.DocumentoIdentidadBanco:"");// Cuenta Bancaria Banco Rut
			formx.setField("CUENTABAN_4BENEFICIARIO", "");// Cuenta Bancaria Beneficiario
			formx.setField("CUENTABAN_4EMISOR", "");// Cuenta Bancaria Emisor
			formx.setField("CUENTABAN_4MONEDA", (cuentaBancaria.IdMoneda!=null)? cuentaBancaria.IdMoneda:"");// Cuenta Bancaria Moneda
			formx.setField("CUENTABAN_4MONEDANEMO", (cuentaBancaria.IdMoneda!=null)? cuentaBancaria.IdMoneda:"");// Cuenta Bancaria Moneda Nemo
			formx.setField("CUENTABAN_4MONEDANOMBRE", (cuentaBancaria.NombreMoneda!=null)? cuentaBancaria.NombreMoneda:"");// Cuenta Bancaria Moneda Nombre
			formx.setField("CUENTABAN_4MONEDASIMBOLO", (cuentaBancaria.SimboloMoneda!=null)? cuentaBancaria.SimboloMoneda:"");// Cuenta Bancaria Moneda Simbolo
			formx.setField("CUENTABAN_4NUMERO", (cuentaBancaria.NumeroCuentaBancaria!=null)? cuentaBancaria.NumeroCuentaBancaria:"");// Cuenta Bancaria Numero
			formx.setField("CUENTABAN_4PAISISO", (cuentaBancaria.CodigoPais!=null)? cuentaBancaria.CodigoPais:"");// Cuenta Bancaria Pais Codigo ISO
			formx.setField("CUENTABAN_4PAISNOMBRE", (cuentaBancaria.NombrePais!=null)? cuentaBancaria.NombrePais:"");// Cuenta Bancaria Pais Nombre
			formx.setField("CUENTABAN_4SUCURSAL", (cuentaBancaria.Sucursal!=null)? cuentaBancaria.Sucursal:"");// Cuenta Bancaria Sucursal
			formx.setField("CUENTABAN_4TIPO", (cuentaBancaria.NombreTipoCuentaBancaria!=null)? cuentaBancaria.NombreTipoCuentaBancaria:"");// Cuenta Bancaria Tipo (Ahorro, Corriente, Vista, etc.)
			formx.setField("CUENTABAN_4ESFACTA", (cuentaBancaria.EsFatca!=null)? cuentaBancaria.EsFatca:""); // Es Fatca			
			if ("CUENTA CORRIENTE".equalsIgnoreCase(cuentaBancaria.NombreTipoCuentaBancaria)) {
				formx.setField("CUENTABANCORRI_4BANCONOMBRE",(cuentaBancaria.NombreBanco!=null)? cuentaBancaria.NombreBanco:"");// Cuenta Corriente Banco Nombre
				formx.setField("CUENTABANCORRI_4BANCORUT",(cuentaBancaria.DocumentoIdentidadBanco!=null)? cuentaBancaria.DocumentoIdentidadBanco:"");// Cuenta Corriente Banco Rut
				formx.setField("CUENTABANCORRI_4BENEFICIARIO","");// Cuenta Corriente Beneficiario
				formx.setField("CUENTABANCORRI_4EMISOR","");// Cuenta Corriente Emisor
				formx.setField("CUENTABANCORRI_4MONEDA",(cuentaBancaria.IdMoneda!=null)? cuentaBancaria.IdMoneda:"");// Cuenta Corriente Moneda
				formx.setField("CUENTABANCORRI_4MONEDANEMO",(cuentaBancaria.IdMoneda!=null)? cuentaBancaria.IdMoneda:"");// Cuenta Corriente Moneda Nemo
				formx.setField("CUENTABANCORRI_4MONEDANOMBRE",(cuentaBancaria.NombreMoneda!=null)? cuentaBancaria.NombreMoneda:"");// Cuenta Corriente Moneda Nombre
				formx.setField("CUENTABANCORRI_4MONEDASIMBOLO",(cuentaBancaria.SimboloMoneda!=null)? cuentaBancaria.SimboloMoneda:"");// Cuenta Corriente Moneda Simbolo
				formx.setField("CUENTABANCORRI_4NUMERO",(cuentaBancaria.NumeroCuentaBancaria!=null)? cuentaBancaria.NumeroCuentaBancaria:"");// Cuenta Corriente Numero
				formx.setField("CUENTABANCORRI_4PAISISO",(cuentaBancaria.CodigoPais!=null)? cuentaBancaria.CodigoPais:"");// Cuenta Corriente Pais Codigo ISO
				formx.setField("CUENTABANCORRI_4PAISNOMBRE",(cuentaBancaria.NombrePais!=null)? cuentaBancaria.NombrePais:"");// Cuenta Corriente Pais Nombre
				formx.setField("CUENTABANCORRI_4SUCURSAL",(cuentaBancaria.Sucursal!=null)? cuentaBancaria.Sucursal:"");// Cuenta Corriente Sucursal
				formx.setField("CUENTABANCORRI_4TIPO",(cuentaBancaria.NombreTipoCuentaBancaria!=null)? cuentaBancaria.NombreTipoCuentaBancaria:"");// Cuenta Corriente Tipo (Ahorro, Corriente, Vista, etc.)
				
			}
		}
		
		{
			CuentaBancaria cuentaBancaria = socioPersona.CuentaBancariaPorOrden.getOrDefault(5L, new CuentaBancaria());
			formx.setField("CUENTABAN_5BANCONOMBRE", (cuentaBancaria.NombreBanco!=null)? cuentaBancaria.NombreBanco:"");// Cuenta Bancaria Banco Nombre
			formx.setField("CUENTABAN_5BANCORUT", (cuentaBancaria.DocumentoIdentidadBanco!=null)? cuentaBancaria.DocumentoIdentidadBanco:"");// Cuenta Bancaria Banco Rut
			formx.setField("CUENTABAN_5BENEFICIARIO", "");// Cuenta Bancaria Beneficiario
			formx.setField("CUENTABAN_5EMISOR", "");// Cuenta Bancaria Emisor
			formx.setField("CUENTABAN_5MONEDA", (cuentaBancaria.IdMoneda!=null)? cuentaBancaria.IdMoneda:"");// Cuenta Bancaria Moneda
			formx.setField("CUENTABAN_5MONEDANEMO", (cuentaBancaria.IdMoneda!=null)? cuentaBancaria.IdMoneda:"");// Cuenta Bancaria Moneda Nemo
			formx.setField("CUENTABAN_5MONEDANOMBRE", (cuentaBancaria.NombreMoneda!=null)? cuentaBancaria.NombreMoneda:"");// Cuenta Bancaria Moneda Nombre
			formx.setField("CUENTABAN_5MONEDASIMBOLO", (cuentaBancaria.SimboloMoneda!=null)? cuentaBancaria.SimboloMoneda:"");// Cuenta Bancaria Moneda Simbolo
			formx.setField("CUENTABAN_5NUMERO", (cuentaBancaria.NumeroCuentaBancaria!=null)? cuentaBancaria.NumeroCuentaBancaria:"");// Cuenta Bancaria Numero
			formx.setField("CUENTABAN_5PAISISO", (cuentaBancaria.CodigoPais!=null)? cuentaBancaria.CodigoPais:"");// Cuenta Bancaria Pais Codigo ISO
			formx.setField("CUENTABAN_5PAISNOMBRE", (cuentaBancaria.NombrePais!=null)? cuentaBancaria.NombrePais:"");// Cuenta Bancaria Pais Nombre
			formx.setField("CUENTABAN_5SUCURSAL", (cuentaBancaria.Sucursal!=null)? cuentaBancaria.Sucursal:"");// Cuenta Bancaria Sucursal
			formx.setField("CUENTABAN_5TIPO", (cuentaBancaria.NombreTipoCuentaBancaria!=null)? cuentaBancaria.NombreTipoCuentaBancaria:"");// Cuenta Bancaria Tipo (Ahorro, Corriente, Vista, etc.)
			formx.setField("CUENTABAN_5ESFACTA", (cuentaBancaria.EsFatca!=null)? cuentaBancaria.EsFatca:""); // Es Fatca	
			if ("CUENTA CORRIENTE".equalsIgnoreCase(cuentaBancaria.NombreTipoCuentaBancaria)) {
				formx.setField("CUENTABANCORRI_5BANCONOMBRE",(cuentaBancaria.NombreBanco!=null)? cuentaBancaria.NombreBanco:"");// Cuenta Corriente Banco Nombre
				formx.setField("CUENTABANCORRI_5BANCORUT",(cuentaBancaria.DocumentoIdentidadBanco!=null)? cuentaBancaria.DocumentoIdentidadBanco:"");// Cuenta Corriente Banco Rut
				formx.setField("CUENTABANCORRI_BENEFICIARIO","");// Cuenta Corriente Beneficiario
				formx.setField("CUENTABANCORRI_5EMISOR","");// Cuenta Corriente Emisor
				formx.setField("CUENTABANCORRI_5MONEDA",(cuentaBancaria.IdMoneda!=null)? cuentaBancaria.IdMoneda:"");// Cuenta Corriente Moneda
				formx.setField("CUENTABANCORRI_5MONEDANEMO",(cuentaBancaria.IdMoneda!=null)? cuentaBancaria.IdMoneda:"");// Cuenta Corriente Moneda Nemo
				formx.setField("CUENTABANCORRI_5MONEDANOMBRE",(cuentaBancaria.NombreMoneda!=null)? cuentaBancaria.NombreMoneda:"");// Cuenta Corriente Moneda Nombre
				formx.setField("CUENTABANCORRI_5MONEDASIMBOLO",(cuentaBancaria.SimboloMoneda!=null)? cuentaBancaria.SimboloMoneda:"");// Cuenta Corriente Moneda Simbolo
				formx.setField("CUENTABANCORRI_5NUMERO",(cuentaBancaria.NumeroCuentaBancaria!=null)? cuentaBancaria.NumeroCuentaBancaria:"");// Cuenta Corriente Numero
				formx.setField("CUENTABANCORRI_5PAISISO",(cuentaBancaria.CodigoPais!=null)? cuentaBancaria.CodigoPais:"");// Cuenta Corriente Pais Codigo ISO
				formx.setField("CUENTABANCORRI_5PAISNOMBRE",(cuentaBancaria.NombrePais!=null)? cuentaBancaria.NombrePais:"");// Cuenta Corriente Pais Nombre
				formx.setField("CUENTABANCORRI_5SUCURSAL",(cuentaBancaria.Sucursal!=null)? cuentaBancaria.Sucursal:"");// Cuenta Corriente Sucursal
				formx.setField("CUENTABANCORRI_5TIPO",(cuentaBancaria.NombreTipoCuentaBancaria!=null)? cuentaBancaria.NombreTipoCuentaBancaria:"");// Cuenta Corriente Tipo (Ahorro, Corriente, Vista, etc.)
				
			}
		}
	
		
		
		//Informacion Cuenta Cliente
		formx.setField("CUENTAEMPAPELLIDOMATERNO",(socioCliente.ApellidoMaternoEjecutivo!=null)?  socioCliente.ApellidoMaternoEjecutivo:"");// Agente Apellido Materno
		formx.setField("CUENTAEMPAPELLIDOPATERNO",(socioCliente.ApellidoPaternoEjecutivo!=null)?  socioCliente.ApellidoPaternoEjecutivo:"");// Agente Apellido Paterno
		formx.setField("CUENTAEMPCOD", (socioCliente.CodigoEjecutivo!=null)?  socioCliente.CodigoEjecutivo:"");// Agente Codigo		
		formx.setField("CUENTAEMPNOMBRES",(socioCliente.NombreEjecutivo!=null)? socioCliente.NombreEjecutivo:"");// Agente Nombres		
		formx.setField("CUENTAEMPNUMERODOCUMENTO",(socioCliente.DocumentoIdentidadEjecutivo!=null)?  socioCliente.DocumentoIdentidadEjecutivo:"");// Agente Numero Documento (Pasaporte, Rut)
		formx.setField("CUENTAEMPRUTDV",(socioCliente.DocumentoIdentidadEjecutivo!=null)?  socioCliente.DocumentoIdentidadEjecutivo:"");// Agente Rut + Digito Verificador
		
		// Perfil productos y cobros
		formx.setField("PPROD_INAC",(datosPerfilProductosPortafolioINAC.NombreEstadoProductos!=null)?  datosPerfilProductosPortafolioINAC.NombreEstadoProductos:"");
		formx.setField("CCOB_INAC_COMISION",(datosPerfilProductosPortafolioINAC.CobroComisionValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioINAC.CobroComisionValorCobro, datosPerfilProductosPortafolioINAC.CobroComisionFormaCobro):"");
		formx.setField("CCOB_INAC_GASTO",(datosPerfilProductosPortafolioINAC.CobroGastoValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioINAC.CobroGastoValorCobro, datosPerfilProductosPortafolioINAC.CobroGastoFormaCobro):"");
		formx.setField("CCOB_INAC_DERECHO",(datosPerfilProductosPortafolioINAC.CobroDerechoValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioINAC.CobroDerechoValorCobro, datosPerfilProductosPortafolioINAC.CobroDerechoFormaCobro):"");
		formx.setField("PPROD_INAX",(datosPerfilProductosPortafolioINAX.NombreEstadoProductos!=null)?  datosPerfilProductosPortafolioINAX.NombreEstadoProductos:"");
		formx.setField("CCOB_INAX_COMISION",(datosPerfilProductosPortafolioINAX.CobroComisionValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioINAX.CobroComisionValorCobro, datosPerfilProductosPortafolioINAX.CobroComisionFormaCobro):"");
		formx.setField("CCOB_INAX_GASTO",(datosPerfilProductosPortafolioINAX.CobroGastoValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioINAX.CobroGastoValorCobro, datosPerfilProductosPortafolioINAX.CobroGastoFormaCobro):"");
		formx.setField("CCOB_INAX_DERECHO",(datosPerfilProductosPortafolioINAX.CobroDerechoValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioINAX.CobroDerechoValorCobro, datosPerfilProductosPortafolioINAX.CobroDerechoFormaCobro):"");
		formx.setField("PPROD_INCI",(datosPerfilProductosPortafolioINCI.NombreEstadoProductos!=null)?  datosPerfilProductosPortafolioINCI.NombreEstadoProductos:"");
		formx.setField("CCOB_INCI_COMISION",(datosPerfilProductosPortafolioINCI.CobroComisionValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioINCI.CobroComisionValorCobro, datosPerfilProductosPortafolioINCI.CobroComisionFormaCobro):"");
		formx.setField("CCOB_INCI_GASTO",(datosPerfilProductosPortafolioINCI.CobroGastoValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioINCI.CobroGastoValorCobro, datosPerfilProductosPortafolioINCI.CobroGastoFormaCobro):"");
		formx.setField("CCOB_INCI_DERECHO",(datosPerfilProductosPortafolioINCI.CobroDerechoValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioINCI.CobroDerechoValorCobro, datosPerfilProductosPortafolioINCI.CobroDerechoFormaCobro):"");
		formx.setField("PPROD_INCM",(datosPerfilProductosPortafolioINCM.NombreEstadoProductos!=null)?  datosPerfilProductosPortafolioINCM.NombreEstadoProductos:"");
		formx.setField("CCOB_INCM_COMISION",(datosPerfilProductosPortafolioINCM.CobroComisionValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioINCM.CobroComisionValorCobro, datosPerfilProductosPortafolioINCM.CobroComisionFormaCobro):"");
		formx.setField("CCOB_INCM_GASTO",(datosPerfilProductosPortafolioINCM.CobroGastoValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioINCM.CobroGastoValorCobro, datosPerfilProductosPortafolioINCM.CobroGastoFormaCobro):"");
		formx.setField("CCOB_INCM_DERECHO",(datosPerfilProductosPortafolioINCM.CobroDerechoValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioINCM.CobroDerechoValorCobro, datosPerfilProductosPortafolioINCM.CobroDerechoFormaCobro):"");
		formx.setField("PPROD_INEN",(datosPerfilProductosPortafolioINEN.NombreEstadoProductos!=null)?  datosPerfilProductosPortafolioINEN.NombreEstadoProductos:"");
		formx.setField("CCOB_INEN_COMISION",(datosPerfilProductosPortafolioINEN.CobroComisionValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioINEN.CobroComisionValorCobro, datosPerfilProductosPortafolioINEN.CobroComisionFormaCobro):"");
		formx.setField("CCOB_INEN_GASTO",(datosPerfilProductosPortafolioINEN.CobroGastoValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioINEN.CobroGastoValorCobro, datosPerfilProductosPortafolioINEN.CobroGastoFormaCobro):"");
		formx.setField("CCOB_INEN_DERECHO",(datosPerfilProductosPortafolioINEN.CobroDerechoValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioINEN.CobroDerechoValorCobro, datosPerfilProductosPortafolioINEN.CobroDerechoFormaCobro):"");
		formx.setField("PPROD_INFU",(datosPerfilProductosPortafolioINFU.NombreEstadoProductos!=null)?  datosPerfilProductosPortafolioINFU.NombreEstadoProductos:"");
		formx.setField("CCOB_INFU_COMISION",(datosPerfilProductosPortafolioINFU.CobroComisionValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioINFU.CobroComisionValorCobro, datosPerfilProductosPortafolioINFU.CobroComisionFormaCobro):"");
		formx.setField("CCOB_INFU_GASTO",(datosPerfilProductosPortafolioINFU.CobroGastoValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioINFU.CobroGastoValorCobro, datosPerfilProductosPortafolioINFU.CobroGastoFormaCobro):"");
		formx.setField("CCOB_INFU_DERECHO",(datosPerfilProductosPortafolioINFU.CobroDerechoValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioINFU.CobroDerechoValorCobro, datosPerfilProductosPortafolioINFU.CobroDerechoFormaCobro):"");
		formx.setField("PPROD_INFM",(datosPerfilProductosPortafolioINFM.NombreEstadoProductos!=null)?  datosPerfilProductosPortafolioINFM.NombreEstadoProductos:"");
		formx.setField("CCOB_INFM_COMISION",(datosPerfilProductosPortafolioINFM.CobroComisionValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioINFM.CobroComisionValorCobro, datosPerfilProductosPortafolioINFM.CobroComisionFormaCobro):"");
		formx.setField("CCOB_INFM_GASTO",(datosPerfilProductosPortafolioINFM.CobroGastoValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioINFM.CobroGastoValorCobro, datosPerfilProductosPortafolioINFM.CobroGastoFormaCobro):"");
		formx.setField("CCOB_INFM_DERECHO",(datosPerfilProductosPortafolioINFM.CobroDerechoValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioINFM.CobroDerechoValorCobro, datosPerfilProductosPortafolioINFM.CobroDerechoFormaCobro):"");
		formx.setField("PPROD_INIF",(datosPerfilProductosPortafolioINIF.NombreEstadoProductos!=null)?  datosPerfilProductosPortafolioINIF.NombreEstadoProductos:"");
		formx.setField("CCOB_INIF_COMISION",(datosPerfilProductosPortafolioINIF.CobroComisionValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioINIF.CobroComisionValorCobro, datosPerfilProductosPortafolioINIF.CobroComisionFormaCobro):"");
		formx.setField("CCOB_INIF_GASTO",(datosPerfilProductosPortafolioINIF.CobroGastoValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioINIF.CobroGastoValorCobro, datosPerfilProductosPortafolioINIF.CobroGastoFormaCobro):"");
		formx.setField("CCOB_INIF_DERECHO",(datosPerfilProductosPortafolioINIF.CobroDerechoValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioINIF.CobroDerechoValorCobro, datosPerfilProductosPortafolioINIF.CobroDerechoFormaCobro):"");
		formx.setField("PPROD_INMO",(datosPerfilProductosPortafolioINMO.NombreEstadoProductos!=null)?  datosPerfilProductosPortafolioINMO.NombreEstadoProductos:"");
		formx.setField("CCOB_INMO_COMISION",(datosPerfilProductosPortafolioINMO.CobroComisionValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioINMO.CobroComisionValorCobro, datosPerfilProductosPortafolioINMO.CobroComisionFormaCobro):"");
		formx.setField("CCOB_INMO_GASTO",(datosPerfilProductosPortafolioINMO.CobroGastoValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioINMO.CobroGastoValorCobro, datosPerfilProductosPortafolioINMO.CobroGastoFormaCobro):"");
		formx.setField("CCOB_INMO_DERECHO",(datosPerfilProductosPortafolioINMO.CobroDerechoValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioINMO.CobroDerechoValorCobro, datosPerfilProductosPortafolioINMO.CobroDerechoFormaCobro):"");
		formx.setField("PPROD_INRF",(datosPerfilProductosPortafolioINRF.NombreEstadoProductos!=null)?  datosPerfilProductosPortafolioINRF.NombreEstadoProductos:"");
		formx.setField("CCOB_INRF_COMISION",(datosPerfilProductosPortafolioINRF.CobroComisionValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioINRF.CobroComisionValorCobro, datosPerfilProductosPortafolioINRF.CobroComisionFormaCobro):"");
		formx.setField("CCOB_INRF_GASTO",(datosPerfilProductosPortafolioINRF.CobroGastoValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioINRF.CobroGastoValorCobro, datosPerfilProductosPortafolioINRF.CobroGastoFormaCobro):"");
		formx.setField("CCOB_INRF_DERECHO",(datosPerfilProductosPortafolioINRF.CobroDerechoValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioINRF.CobroDerechoValorCobro, datosPerfilProductosPortafolioINRF.CobroDerechoFormaCobro):"");
		formx.setField("PPROD_INSI",(datosPerfilProductosPortafolioINSI.NombreEstadoProductos!=null)?  datosPerfilProductosPortafolioINSI.NombreEstadoProductos:"");
		formx.setField("CCOB_INSI_COMISION",(datosPerfilProductosPortafolioINSI.CobroComisionValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioINSI.CobroComisionValorCobro, datosPerfilProductosPortafolioINSI.CobroComisionFormaCobro):"");
		formx.setField("CCOB_INSI_GASTO",(datosPerfilProductosPortafolioINSI.CobroGastoValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioINSI.CobroGastoValorCobro, datosPerfilProductosPortafolioINSI.CobroGastoFormaCobro):"");
		formx.setField("CCOB_INSI_DERECHO",(datosPerfilProductosPortafolioINSI.CobroDerechoValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioINSI.CobroDerechoValorCobro, datosPerfilProductosPortafolioINSI.CobroDerechoFormaCobro):"");
		formx.setField("PPROD_MEFW",(datosPerfilProductosPortafolioMEFW.NombreEstadoProductos!=null)?  datosPerfilProductosPortafolioMEFW.NombreEstadoProductos:"");
		formx.setField("CCOB_MEFW_COMISION",(datosPerfilProductosPortafolioMEFW.CobroComisionValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioMEFW.CobroComisionValorCobro, datosPerfilProductosPortafolioMEFW.CobroComisionFormaCobro):"");
		formx.setField("CCOB_MEFW_GASTO",(datosPerfilProductosPortafolioMEFW.CobroGastoValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioMEFW.CobroGastoValorCobro, datosPerfilProductosPortafolioMEFW.CobroGastoFormaCobro):"");
		formx.setField("CCOB_MEFW_DERECHO",(datosPerfilProductosPortafolioMEFW.CobroDerechoValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioMEFW.CobroDerechoValorCobro, datosPerfilProductosPortafolioMEFW.CobroDerechoFormaCobro):"");
		formx.setField("PPROD_MEME",(datosPerfilProductosPortafolioMEME.NombreEstadoProductos!=null)?  datosPerfilProductosPortafolioMEME.NombreEstadoProductos:"");
		formx.setField("CCOB_MEME_COMISION",(datosPerfilProductosPortafolioMEME.CobroComisionValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioMEME.CobroComisionValorCobro, datosPerfilProductosPortafolioMEME.CobroComisionFormaCobro):"");
		formx.setField("CCOB_MEME_GASTO",(datosPerfilProductosPortafolioMEME.CobroGastoValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioMEME.CobroGastoValorCobro, datosPerfilProductosPortafolioMEME.CobroGastoFormaCobro):"");
		formx.setField("CCOB_MEME_DERECHO",(datosPerfilProductosPortafolioMEME.CobroDerechoValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioMEME.CobroDerechoValorCobro, datosPerfilProductosPortafolioMEME.CobroDerechoFormaCobro):"");
		formx.setField("PPROD_MERF",(datosPerfilProductosPortafolioMERF.NombreEstadoProductos!=null)?  datosPerfilProductosPortafolioMERF.NombreEstadoProductos:"");
		formx.setField("CCOB_MERF_COMISION",(datosPerfilProductosPortafolioMERF.CobroComisionValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioMERF.CobroComisionValorCobro, datosPerfilProductosPortafolioMERF.CobroComisionFormaCobro):"");
		formx.setField("CCOB_MERF_GASTO",(datosPerfilProductosPortafolioMERF.CobroGastoValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioMERF.CobroGastoValorCobro, datosPerfilProductosPortafolioMERF.CobroGastoFormaCobro):"");
		formx.setField("CCOB_MERF_DERECHO",(datosPerfilProductosPortafolioMERF.CobroDerechoValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioMERF.CobroDerechoValorCobro, datosPerfilProductosPortafolioMERF.CobroDerechoFormaCobro):"");
		
		// Perfil cobros internet
		formx.setField("CCOB_INT_INAC_COMISION",(datosPerfilProductosPortafolioIntINAC.CobroComisionValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioIntINAC.CobroComisionValorCobro, datosPerfilProductosPortafolioIntINAC.CobroComisionFormaCobro):"");
		formx.setField("CCOB_INT_INAC_GASTO",(datosPerfilProductosPortafolioIntINAC.CobroGastoValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioIntINAC.CobroGastoValorCobro, datosPerfilProductosPortafolioIntINAC.CobroGastoFormaCobro):"");
		formx.setField("CCOB_INT_INAC_DERECHO",(datosPerfilProductosPortafolioIntINAC.CobroDerechoValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioIntINAC.CobroDerechoValorCobro, datosPerfilProductosPortafolioIntINAC.CobroDerechoFormaCobro):"");
		formx.setField("CCOB_INT_INAX_COMISION",(datosPerfilProductosPortafolioIntINAX.CobroComisionValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioIntINAX.CobroComisionValorCobro, datosPerfilProductosPortafolioIntINAX.CobroComisionFormaCobro):"");
		formx.setField("CCOB_INT_INAX_GASTO",(datosPerfilProductosPortafolioIntINAX.CobroGastoValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioIntINAX.CobroGastoValorCobro, datosPerfilProductosPortafolioIntINAX.CobroGastoFormaCobro):"");
		formx.setField("CCOB_INT_INAX_DERECHO",(datosPerfilProductosPortafolioIntINAX.CobroDerechoValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioIntINAX.CobroDerechoValorCobro, datosPerfilProductosPortafolioIntINAX.CobroDerechoFormaCobro):"");
		formx.setField("CCOB_INT_INCI_COMISION",(datosPerfilProductosPortafolioIntINCI.CobroComisionValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioIntINCI.CobroComisionValorCobro, datosPerfilProductosPortafolioIntINCI.CobroComisionFormaCobro):"");
		formx.setField("CCOB_INT_INCI_GASTO",(datosPerfilProductosPortafolioIntINCI.CobroGastoValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioIntINCI.CobroGastoValorCobro, datosPerfilProductosPortafolioIntINCI.CobroGastoFormaCobro):"");
		formx.setField("CCOB_INT_INCI_DERECHO",(datosPerfilProductosPortafolioIntINCI.CobroDerechoValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioIntINCI.CobroDerechoValorCobro, datosPerfilProductosPortafolioIntINCI.CobroDerechoFormaCobro):"");
		formx.setField("CCOB_INT_INCM_COMISION",(datosPerfilProductosPortafolioIntINCM.CobroComisionValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioIntINCM.CobroComisionValorCobro, datosPerfilProductosPortafolioIntINCM.CobroComisionFormaCobro):"");
		formx.setField("CCOB_INT_INCM_GASTO",(datosPerfilProductosPortafolioIntINCM.CobroGastoValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioIntINCM.CobroGastoValorCobro, datosPerfilProductosPortafolioIntINCM.CobroGastoFormaCobro):"");
		formx.setField("CCOB_INT_INCM_DERECHO",(datosPerfilProductosPortafolioIntINCM.CobroDerechoValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioIntINCM.CobroDerechoValorCobro, datosPerfilProductosPortafolioIntINCM.CobroDerechoFormaCobro):"");
		formx.setField("CCOB_INT_INEN_COMISION",(datosPerfilProductosPortafolioIntINEN.CobroComisionValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioIntINEN.CobroComisionValorCobro, datosPerfilProductosPortafolioIntINEN.CobroComisionFormaCobro):"");
		formx.setField("CCOB_INT_INEN_GASTO",(datosPerfilProductosPortafolioIntINEN.CobroGastoValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioIntINEN.CobroGastoValorCobro, datosPerfilProductosPortafolioIntINEN.CobroGastoFormaCobro):"");
		formx.setField("CCOB_INT_INEN_DERECHO",(datosPerfilProductosPortafolioIntINEN.CobroDerechoValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioIntINEN.CobroDerechoValorCobro, datosPerfilProductosPortafolioIntINEN.CobroDerechoFormaCobro):"");
		formx.setField("CCOB_INT_INFU_COMISION",(datosPerfilProductosPortafolioIntINFU.CobroComisionValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioIntINFU.CobroComisionValorCobro, datosPerfilProductosPortafolioIntINFU.CobroComisionFormaCobro):"");
		formx.setField("CCOB_INT_INFU_GASTO",(datosPerfilProductosPortafolioIntINFU.CobroGastoValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioIntINFU.CobroGastoValorCobro, datosPerfilProductosPortafolioIntINFU.CobroGastoFormaCobro):"");
		formx.setField("CCOB_INT_INFU_DERECHO",(datosPerfilProductosPortafolioIntINFU.CobroDerechoValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioIntINFU.CobroDerechoValorCobro, datosPerfilProductosPortafolioIntINFU.CobroDerechoFormaCobro):"");
		formx.setField("CCOB_INT_INFM_COMISION",(datosPerfilProductosPortafolioIntINFM.CobroComisionValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioIntINFM.CobroComisionValorCobro, datosPerfilProductosPortafolioIntINFM.CobroComisionFormaCobro):"");
		formx.setField("CCOB_INT_INFM_GASTO",(datosPerfilProductosPortafolioIntINFM.CobroGastoValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioIntINFM.CobroGastoValorCobro, datosPerfilProductosPortafolioIntINFM.CobroGastoFormaCobro):"");
		formx.setField("CCOB_INT_INFM_DERECHO",(datosPerfilProductosPortafolioIntINFM.CobroDerechoValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioIntINFM.CobroDerechoValorCobro, datosPerfilProductosPortafolioIntINFM.CobroDerechoFormaCobro):"");
		formx.setField("CCOB_INT_INIF_COMISION",(datosPerfilProductosPortafolioIntINIF.CobroComisionValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioIntINIF.CobroComisionValorCobro, datosPerfilProductosPortafolioIntINIF.CobroComisionFormaCobro):"");
		formx.setField("CCOB_INT_INIF_GASTO",(datosPerfilProductosPortafolioIntINIF.CobroGastoValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioIntINIF.CobroGastoValorCobro, datosPerfilProductosPortafolioIntINIF.CobroGastoFormaCobro):"");
		formx.setField("CCOB_INT_INIF_DERECHO",(datosPerfilProductosPortafolioIntINIF.CobroDerechoValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioIntINIF.CobroDerechoValorCobro, datosPerfilProductosPortafolioIntINIF.CobroDerechoFormaCobro):"");
		formx.setField("CCOB_INT_INMO_COMISION",(datosPerfilProductosPortafolioIntINMO.CobroComisionValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioIntINMO.CobroComisionValorCobro, datosPerfilProductosPortafolioIntINMO.CobroComisionFormaCobro):"");
		formx.setField("CCOB_INT_INMO_GASTO",(datosPerfilProductosPortafolioIntINMO.CobroGastoValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioIntINMO.CobroGastoValorCobro, datosPerfilProductosPortafolioIntINMO.CobroGastoFormaCobro):"");
		formx.setField("CCOB_INT_INMO_DERECHO",(datosPerfilProductosPortafolioIntINMO.CobroDerechoValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioIntINMO.CobroDerechoValorCobro, datosPerfilProductosPortafolioIntINMO.CobroDerechoFormaCobro):"");
		formx.setField("CCOB_INT_INRF_COMISION",(datosPerfilProductosPortafolioIntINRF.CobroComisionValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioIntINRF.CobroComisionValorCobro, datosPerfilProductosPortafolioIntINRF.CobroComisionFormaCobro):"");
		formx.setField("CCOB_INT_INRF_GASTO",(datosPerfilProductosPortafolioIntINRF.CobroGastoValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioIntINRF.CobroGastoValorCobro, datosPerfilProductosPortafolioIntINRF.CobroGastoFormaCobro):"");
		formx.setField("CCOB_INT_INRF_DERECHO",(datosPerfilProductosPortafolioIntINRF.CobroDerechoValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioIntINRF.CobroDerechoValorCobro, datosPerfilProductosPortafolioIntINRF.CobroDerechoFormaCobro):"");
		formx.setField("CCOB_INT_INSI_COMISION",(datosPerfilProductosPortafolioIntINSI.CobroComisionValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioIntINSI.CobroComisionValorCobro, datosPerfilProductosPortafolioIntINSI.CobroComisionFormaCobro):"");
		formx.setField("CCOB_INT_INSI_GASTO",(datosPerfilProductosPortafolioIntINSI.CobroGastoValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioIntINSI.CobroGastoValorCobro, datosPerfilProductosPortafolioIntINSI.CobroGastoFormaCobro):"");
		formx.setField("CCOB_INT_INSI_DERECHO",(datosPerfilProductosPortafolioIntINSI.CobroDerechoValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioIntINSI.CobroDerechoValorCobro, datosPerfilProductosPortafolioIntINSI.CobroDerechoFormaCobro):"");
		formx.setField("CCOB_INT_MEFW_COMISION",(datosPerfilProductosPortafolioIntMEFW.CobroComisionValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioIntMEFW.CobroComisionValorCobro, datosPerfilProductosPortafolioIntMEFW.CobroComisionFormaCobro):"");
		formx.setField("CCOB_INT_MEFW_GASTO",(datosPerfilProductosPortafolioIntMEFW.CobroGastoValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioIntMEFW.CobroGastoValorCobro, datosPerfilProductosPortafolioIntMEFW.CobroGastoFormaCobro):"");
		formx.setField("CCOB_INT_MEFW_DERECHO",(datosPerfilProductosPortafolioIntMEFW.CobroDerechoValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioIntMEFW.CobroDerechoValorCobro, datosPerfilProductosPortafolioIntMEFW.CobroDerechoFormaCobro):"");
		formx.setField("CCOB_INT_MEME_COMISION",(datosPerfilProductosPortafolioIntMEME.CobroComisionValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioIntMEME.CobroComisionValorCobro, datosPerfilProductosPortafolioIntMEME.CobroComisionFormaCobro):"");
		formx.setField("CCOB_INT_MEME_GASTO",(datosPerfilProductosPortafolioIntMEME.CobroGastoValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioIntMEME.CobroGastoValorCobro, datosPerfilProductosPortafolioIntMEME.CobroGastoFormaCobro):"");
		formx.setField("CCOB_INT_MEME_DERECHO",(datosPerfilProductosPortafolioIntMEME.CobroDerechoValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioIntMEME.CobroDerechoValorCobro, datosPerfilProductosPortafolioIntMEME.CobroDerechoFormaCobro):"");
		formx.setField("CCOB_INT_MERF_COMISION",(datosPerfilProductosPortafolioIntMERF.CobroComisionValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioIntMERF.CobroComisionValorCobro, datosPerfilProductosPortafolioIntMERF.CobroComisionFormaCobro):"");
		formx.setField("CCOB_INT_MERF_GASTO",(datosPerfilProductosPortafolioIntMERF.CobroGastoValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioIntMERF.CobroGastoValorCobro, datosPerfilProductosPortafolioIntMERF.CobroGastoFormaCobro):"");
		formx.setField("CCOB_INT_MERF_DERECHO",(datosPerfilProductosPortafolioIntMERF.CobroDerechoValorCobro!=null)?  formatearDecimales(datosPerfilProductosPortafolioIntMERF.CobroDerechoValorCobro, datosPerfilProductosPortafolioIntMERF.CobroDerechoFormaCobro):"");
		
		//Informacion Direccion Fisica
		formx.setField("DIRECCIONCASILLA","");// Direccion Casilla
		formx.setField("DIRECCIONCASILLACIUDAD","");// Direccion Casilla Ciudad
		formx.setField("DIRECCIONCASILLACOMUNA","");// Direccion Casilla Comuna
		formx.setField("DIRECCIONCOMERCIAL",(socioPersona.DireccionFisicaComercial!=null)? socioPersona.DireccionFisicaComercial:"");// Direccion Comercial
		formx.setField("DIRECCIONCOMERCIALCIUDADCUENTA","");// Direccion Comercial Ciudad Cuenta
		formx.setField("DIRECCIONCOMERCIALCOMUNA",(socioPersona.DireccionFisicaComercialNombreComuna!=null)? socioPersona.DireccionFisicaComercialNombreComuna:"");// Direccion Comercial Comuna
		formx.setField("DIRECCIONCOMERCIALCOMUNACUENTA","");// Direccion Comercial Comuna Cuenta
		formx.setField("DIRECCIONCOMERCIALCUENTA","");// Direccion Comercial Cuenta
		formx.setField("DIRECCIONCOMERCIALCUIDAD",(socioPersona.DireccionFisicaComercialNombreCiudad!=null)? socioPersona.DireccionFisicaComercialNombreCiudad:"");// Direccion Comercial Cuidad
		formx.setField("DIRECCIONCOMERCIALREGION",(socioPersona.DireccionFisicaComercialNombreRegion!=null)? socioPersona.DireccionFisicaComercialNombreRegion:"");// Direccion Comercial Region
		formx.setField("DIRECCIONCOMERCIALPAIS",(socioPersona.DireccionFisicaComercialNombrePais!=null)? socioPersona.DireccionFisicaComercialNombrePais:"");// Direccion Comercial Pais
		formx.setField("DIRECCIONCORRESPONDENCIACIUDADCUENTA","");// Direccion Correspondencia Ciudad Cuenta
		formx.setField("DIRECCIONCORRESPONDENCIACOMUNACUENTA","");// Direccion Correspondencia Comuna Cuenta
		formx.setField("DIRECCIONCORRESPONDENCIACUENTA","");// Direccion Correspondencia Cuenta
		formx.setField("DIRECCIONLABORAL",(socioPersona.DireccionFisicaLaboral!=null)? socioPersona.DireccionFisicaLaboral:"");// Direccion Laboral
		formx.setField("DIRECCIONLABORALCIUDAD",(socioPersona.DireccionFisicaLaboralNombreCiudad!=null)? socioPersona.DireccionFisicaLaboralNombreCiudad:"");// Direccion Laboral Ciudad
		formx.setField("DIRECCIONLABORALCOMUNA",(socioPersona.DireccionFisicaLaboralNombreComuna!=null)? socioPersona.DireccionFisicaLaboralNombreComuna:"");// Direccion Laboral Comuna
		formx.setField("DIRECCIONLABORALREGION",(socioPersona.DireccionFisicaLaboralNombreRegion!=null)? socioPersona.DireccionFisicaLaboralNombreRegion:"");// Direccion Laboral Region
		formx.setField("DIRECCIONLABORALPAIS",(socioPersona.DireccionFisicaLaboralNombrePais!=null)? socioPersona.DireccionFisicaLaboralNombrePais:"");// Direccion Laboral Pais
		formx.setField("DIRECCIONLEGALCIUDADCUENTA","");// Direccion Legal Ciudad Cuenta
		formx.setField("DIRECCIONLEGALCOMUNACUENTA","");// Direccion Legal Comuna Cuenta
		formx.setField("DIRECCIONLEGALCUENTA","");// Direccion Legal Cuenta
		formx.setField("DIRECCIONPARTICULARCORREDORA",((corredora.DireccionFisicaPrincipal!=null)? corredora.DireccionFisicaPrincipal:"") + " " + ((corredora.NumeroDireccion!=null)? corredora.NumeroDireccion:""));// Direccion Particular  Corredora
		formx.setField("DIRECCIONPARTICULAR",socioPersona.DireccionFisicaCalle + " " + socioPersona.DireccionFisicaNumero);// Direccion Particular  CLiente		
		formx.setField("DIRECCIONPARTICULARCALLE", (socioPersona.DireccionFisicaCalle!=null)? socioPersona.DireccionFisicaCalle:"");// Direccion Particular Calle  CLiente		
		formx.setField("DIRECCIONPARTICULARNUMERO", (socioPersona.DireccionFisicaNumero!=null)? socioPersona.DireccionFisicaNumero:"");// Direccion Particular Numerro CLiente		
		formx.setField("DIRECCIONPARTICULARCIUDAD",(socioPersona.DireccionFisicaNombreCiudad!=null)? socioPersona.DireccionFisicaNombreCiudad:"");// Direccion Particular Ciudad		
		formx.setField("DIRECCIONPARTICULARCOMUNA",(socioPersona.DireccionFisicaNombreComuna!=null)? socioPersona.DireccionFisicaNombreComuna:"");// Direccion Particular Comuna
		formx.setField("DIRECCIONPARTICULARREGION",(socioPersona.DireccionFisicaNombreRegion!=null)? socioPersona.DireccionFisicaNombreRegion:"");// Direccion Particular Region
		formx.setField("DIRECCIONPARTICULARPAIS",(socioPersona.DireccionFisicaNombrePais!=null)? socioPersona.DireccionFisicaNombrePais:"");// Direccion Particular Pais

		//Hora y Fecha Actual (Impresion de Contrato)
		formx.setField("FECHAACTUAL",(fechaSistema!=null)?  fechaSistema:"");// Fecha Actual Firma		
		formx.setField("HORAACTUAL",(horaSistema!=null)?  horaSistema:"");// Hora Actual Firma
		formx.setField("FECHAACTUALAAAA",(anioFechaSistema!=null)?  anioFechaSistema:"");// Anio Actual Firma
		formx.setField("FECHAACTUALMM",(mesFechaSistema!=null)?  mesFechaSistema:"");// Mes Actual Firma
		formx.setField("FECHAACTUALDD",(diaFechaSistema!=null)?  diaFechaSistema:"");// Dia Actual Firma
		
		//Informacion Direccion correo electronico
		formx.setField("MAILPARTICULAR", (socioPersona.DireccionElectronicaPrincipal!=null)? socioPersona.DireccionElectronicaPrincipal:"");		
		formx.setField("MAILCOMERCIAL",(socioPersona.DireccionElectronicaComercialPersona!=null)? socioPersona.DireccionElectronicaComercialPersona:"");	// Correo Electronico Comercial
		formx.setField("MAILCORREDORACUENTA","");// Correo Electronico Corredora Cuenta
		formx.setField("MAILFACTURACUENTA","");// Correo Electronico Factura Cuenta
		formx.setField("MAILFUNCIONARIO","");// Correo Electronico Funcionario
		formx.setField("MAILLABORAL",(socioPersona.DireccionElectronicaLaboral!=null)? socioPersona.DireccionElectronicaLaboral:"");	// Correo Electronico Laboral
		
		//Informacion Persona
		formx.setField("PERSONARAZONSOCIAL",(socioPersona.NombreCompleto!=null)? socioPersona.NombreCompleto:"");
		formx.setField("PERSONARUTDV", (socioPersona.DocumentoIdentidad!=null)? socioPersona.DocumentoIdentidad:"");
		formx.setField("PERSONAGIRO", (socioPersona.NombreGiro!=null)? socioPersona.NombreGiro:"");
		formx.setField("PERSONAPAIS", (socioPersona.NombrePaisNacionalidadPrincipal!=null)? socioPersona.NombrePaisNacionalidadPrincipal:"");
		formx.setField("PERSONAGLEI", (socioPersona.GlobalLegalEntity!=null)? socioPersona.GlobalLegalEntity:"");
		formx.setField("PERSONAAPELLIDOMATERNO", (socioPersona.ApellidoMaterno!=null)? socioPersona.ApellidoMaterno:"");// Apellido Materno
		formx.setField("PERSONAAPELLIDOPATERNO", (socioPersona.ApellidoPaterno!=null)? socioPersona.ApellidoPaterno:"");// Apellido Paterno
		formx.setField("PERSONACODESTADOCIVIL", (socioPersona.CodigoEstadoCivil!=null)? socioPersona.CodigoEstadoCivil:"");// Codigo Estado Civil
		formx.setField("PERSONACODREGCONYUGAL", (socioPersona.RegimenConyugal!=null)? socioPersona.RegimenConyugal:"");// Codigo Regimen Conyugal
		formx.setField("PERSONAESTADO", (socioPersona.IdEstado!=null)? socioPersona.IdEstado:"");// Estado
		formx.setField("PERSONAESTADOCIVIL", (socioPersona.EstadoCivil!=null)? socioPersona.EstadoCivil:"");// Estado Civil
		formx.setField("PERSONAFECHAINGRESO", (socioPersona.FechaIngreso!=null)? socioPersona.FechaIngreso:"");// Fecha Ingreso
		formx.setField("PERSONAFECHANAC", (socioPersona.FechaNacimiento!=null)? socioPersona.FechaNacimiento:"");// Fecha Nacimiento
		formx.setField("PERSONAHORAINGRESO", (socioPersona.HoraIngreso!=null)? socioPersona.HoraIngreso:"");// Hora Ingreso
		formx.setField("PERSONANOMBREREGCONYUGAL", (socioPersona.NombreRegimenConyugal!=null)? socioPersona.NombreRegimenConyugal:"");// Nombre Regimen Conyugal
		formx.setField("PERSONANOMBRES", socioPersona.esPersonaNatural() ? ((socioPersona.Nombres!=null)? socioPersona.Nombres:"") : ((socioPersona.RazonSocial!=null)? socioPersona.RazonSocial:""));// Nombres (sin apellidos) o razon social
		formx.setField("PERSONANOMBRECOMPLETO", (socioPersona.NombreCompleto!=null)? socioPersona.NombreCompleto:"");// Nombres completo o razon social
		formx.setField("PERSONANUMERODOCUMENTO", (socioPersona.DocumentoIdentidad!=null)? socioPersona.DocumentoIdentidad:"");// Numero Documento
		formx.setField("PERSONASEXO", (socioPersona.Sexo!=null)? socioPersona.Sexo:"");// Sexo
		formx.setField("PERSONATIPO", (socioPersona.TipoPersona!=null)? socioPersona.TipoPersona:"");// Tipo (Juridica, Natural)
		formx.setField("PERSONACIUDAD", (socioPersona.DireccionFisicaNombreCiudad!=null)? socioPersona.DireccionFisicaNombreCiudad:"");// Tipo (Juridica, Natural)
		formx.setField("PERSONAPAIS", (socioPersona.NombrePaisNacionalidadPrincipal!=null)? socioPersona.NombrePaisNacionalidadPrincipal:"");// Tipo (Juridica, Natural)
		formx.setField("PERSONACOMUNA", (socioPersona.DireccionFisicaNombreComuna!=null)? socioPersona.DireccionFisicaNombreComuna:"");// Tipo (Juridica, Natural)
		formx.setField("PERSONANOMBREPAISNAC", (socioPersona.NombrePaisNacimiento!=null)? socioPersona.NombrePaisNacimiento:""); // Nombre del pais de nacimiento
		formx.setField("PERSONANOMBREPAISDIRFIS", (socioPersona.NombrePaisDireccionFisica!=null)? socioPersona.NombrePaisDireccionFisica:""); // Nombre del pais de direccion fisica
		formx.setField("PERSONASITUACIONLABORAL", (socioPersona.IdSituacionLaboral!=null)? socioPersona.IdSituacionLaboral:""); // Estado de situacion laboral
		formx.setField("PERSONANOMBREFANTASIA", (socioPersona.NombreFantasia!=null)? socioPersona.NombreFantasia:""); // Estado de Nombre Fantasia
		formx.setField("PERSONANOMBREPAISCONSTITUCION", (socioPersona.NombrePaisNacimiento!=null)? socioPersona.NombrePaisNacimiento:""); // Nombre del pais de constitucion
		formx.setField("PERSONACARGO", (socioPersona.Cargo!=null)? socioPersona.Cargo:""); // Nombre Cargo 
		formx.setField("PERSONARELCORREDORANCG380", (socioPersona.IndicadorNCG380!=null)? socioPersona.IndicadorNCG380:""); // Nombre Cargo 
		formx.setField("PERSONASEXOCORTO", (socioPersona.SexoCorto!=null)? socioPersona.SexoCorto:""); // Sexo Corto
		formx.setField("PERSONAPROFESION", (socioPersona.Profesion!=null)? socioPersona.Profesion:""); // Profesion
		formx.setField("PERSONARENTA", (socioPersona.RangoRenta!=null)? socioPersona.RangoRenta:""); //RangoRenta
		formx.setField("PERSONANIVELVENTA", (socioPersona.RangoNivelVenta!=null)? socioPersona.RangoNivelVenta:""); //RangoNivelVenta
		formx.setField("PERSONAPROPOSITOTRANSACCION", (socioPersona.PropositoTransaccion!=null)? socioPersona.PropositoTransaccion:""); //PropositoTransaccion
		
		//Indicadores Persona Mandato Mercantil
		formx.setField("BCOCARGO1_1",	 (socioPersona.BCOCARGO1_1	!=null)? socioPersona.BCOCARGO1_1	:""); 
		formx.setField("NCTACARGO1",	 (socioPersona.NCTACARGO1	!=null)? socioPersona.NCTACARGO1	:""); 
		formx.setField("TCTACARGO1",	 (socioPersona.TCTACARGO1	!=null)? socioPersona.TCTACARGO1	:""); 
		formx.setField("BCOCARGO2_2",	 (socioPersona.BCOCARGO2_2	!=null)? socioPersona.BCOCARGO2_2	:""); 
		formx.setField("NCTACARGO2",	 (socioPersona.NCTACARGO2	!=null)? socioPersona.NCTACARGO2	:""); 
		formx.setField("TCTACARGO2",	 (socioPersona.TCTACARGO2	!=null)? socioPersona.TCTACARGO2	:""); 
		formx.setField("BCOCARGO3_3",	 (socioPersona.BCOCARGO3_3	!=null)? socioPersona.BCOCARGO3_3	:""); 
		formx.setField("NCTACARGO3",	 (socioPersona.NCTACARGO3	!=null)? socioPersona.NCTACARGO3	:""); 
		formx.setField("TCTACARGO3",	 (socioPersona.TCTACARGO3	!=null)? socioPersona.TCTACARGO3	:""); 
		formx.setField("BCOCARGO4_4",	 (socioPersona.BCOCARGO4_4	!=null)? socioPersona.BCOCARGO4_4	:""); 
		formx.setField("NCTACARGO4",	 (socioPersona.NCTACARGO4	!=null)? socioPersona.NCTACARGO4	:""); 
		formx.setField("TCTACARGO4",	 (socioPersona.TCTACARGO4	!=null)? socioPersona.TCTACARGO4	:""); 
		
		// Informacion socio (como cliente)
		formx.setField("CLIENTE_NOMBRE_EJECUTIVO", (socioCliente.NombreEjecutivo!=null)? socioCliente.NombreEjecutivo:"");
		formx.setField("CLIENTE_NOMBRE_SUCURSAL", (socioCliente.NombreSucursalPreferencia!=null)? socioCliente.NombreSucursalPreferencia:"");
		formx.setField("CLIENTE_CODIGO_SUCURSAL", (socioCliente.CodigoSucursalPreferencia!=null)? socioCliente.CodigoSucursalPreferencia:"");
		formx.setField("CLIENTE_NGC380", (socioCliente.IndicadorNGC380!=null)? socioCliente.IndicadorNGC380:"");
		formx.setField("CLIENTE_FOLIO_CONTRATO", (socioCliente.FolioContrato!=null)? socioCliente.FolioContrato:"");
		formx.setField("CLIENTE_CODIGO_EJECUTIVO", (socioCliente.CodigoEjecutivo!=null)? socioCliente.CodigoEjecutivo:"");
		formx.setField("CLIENTEINFPRIVILEGIADA", (socioCliente.IndicadorClienteInfomacionPrivilegiada!=null)? socioCliente.IndicadorClienteInfomacionPrivilegiada:"");
		formx.setField("CLIENTERELCORREDORA", (socioCliente.IndicadorNGC380!=null)? socioCliente.IndicadorNGC380:"");
		
		//Indicadores Cliente
		formx.setField("INDCLIENTE_ANTLE", (socioCliente.IndicadorAntecedentesLegales!=null)? socioCliente.IndicadorAntecedentesLegales:"");
		formx.setField("INDCLIENTE_INICA", (socioCliente.IndicadorInversionistaCalificado!=null)? socioCliente.IndicadorInversionistaCalificado:"");
		formx.setField("INDCLIENTE_PPEXP", (socioCliente.IndicadorPersonaPoliticamenteExpuesta!=null)? socioCliente.IndicadorPersonaPoliticamenteExpuesta:"");
		formx.setField("INDCLIENTE_CATIG", (socioCliente.IndicadorCategoriaInversionista!=null)? socioCliente.IndicadorCategoriaInversionista:"");
		formx.setField("INDCLIENTE_FATCA", (socioCliente.IndicadorClienteFATCA!=null)? socioCliente.IndicadorClienteFATCA:"");
		formx.setField("INDCLIENTE_KYC", (socioPersona.IndicadorKnowYourCustomer!=null)? socioPersona.IndicadorKnowYourCustomer:"");
		
		formx.setField("INDCUENTA_ADMCA", (socioCliente.IndicadorAdministracionCartera!=null)? socioCliente.IndicadorAdministracionCartera:"");
		formx.setField("INDCUENTA_NETAC", (socioCliente.IndicadorNeteaAbonosCargos!=null)? socioCliente.IndicadorNeteaAbonosCargos:"");
		formx.setField("INDCUENTA_TICRF", (socioCliente.IndicadorCustodiaRF!=null)? socioCliente.IndicadorCustodiaRF:"");
		formx.setField("INDCUENTA_TICRV", (socioCliente.IndicadorCustodiaRV!=null)? socioCliente.IndicadorCustodiaRV:"");
		
		// Informacion primer portafolio activo cliente
		formx.setField("PORTAFOLIO_SOCIO_MODALIDAD_CAPTACION", (socioCliente.ModalidadesCaptacion!=null)? socioCliente.ModalidadesCaptacion:"");
		formx.setField("PORTAFOLIO_SOCIO_DIRECCION_ELECTRONICA_NOTIFICACION_DEFECTO", (socioCliente.DireccionElectronicaNotificacionPorDefecto!=null)? socioCliente.DireccionElectronicaNotificacionPorDefecto:"");

		//Informacion relacionado (apoderado)
		formx.setField("RELAPODE_1APELLIDOMATERNO", (socioApoderado1.ApellidoMaterno!=null)? socioApoderado1.ApellidoMaterno:"");// Apoderado Apellido Materno
		formx.setField("RELAPODE_1APELLIDOPATERNO", (socioApoderado1.ApellidoPaterno!=null)? socioApoderado1.ApellidoPaterno:"");// Apoderado Apellido Paterno
		formx.setField("RELAPODE_1DIRECCIONPARTICULAR",((socioApoderado1.DireccionFisicaCalle!=null)? socioApoderado1.DireccionFisicaCalle:"") + " " + ((socioApoderado1.DireccionFisicaNumero!=null)? socioApoderado1.DireccionFisicaNumero:""));// Apoderado Direccion Particular
		formx.setField("RELAPODE_1DIRECCIONPARTICULARCALLE", (socioApoderado1.DireccionFisicaCalle!=null)? socioApoderado1.DireccionFisicaCalle:"");// Apoderado Direccion Particular Numero
		formx.setField("RELAPODE_1DIRECCIONPARTICULARNUMERO", (socioApoderado1.DireccionFisicaNumero!=null)? socioApoderado1.DireccionFisicaNumero:"");// Apoderado Direccion Particular Numero
		formx.setField("RELAPODE_1DIRECCIONPARTICULARCIUDAD", (socioApoderado1.DireccionFisicaNombreCiudad!=null)? socioApoderado1.DireccionFisicaNombreCiudad:"");// Apoderado Direccion Particular Ciudad
		formx.setField("RELAPODE_1DIRECCIONPARTICULARCOMUNA", (socioApoderado1.DireccionFisicaNombreComuna!=null)? socioApoderado1.DireccionFisicaNombreComuna:"");// Apoderado Direccion Particular Comuna
		formx.setField("RELAPODE_1DIRECCIONPARTICULARREGION", (socioApoderado1.DireccionFisicaNombreRegion!=null)? socioApoderado1.DireccionFisicaNombreRegion:"");// Apoderado Direccion Particular Region
		formx.setField("RELAPODE_1DIRECCIONPARTICULARPAIS", (socioApoderado1.DireccionFisicaNombrePais!=null)? socioApoderado1.DireccionFisicaNombrePais:"");// Apoderado Direccion Particular Pais
		formx.setField("RELAPODE_1DV", (socioApoderado1.DigitoVerificador!=null)? socioApoderado1.DigitoVerificador:"");// Apoderado Digito Verificador
		formx.setField("RELAPODE_1GIRO", (socioApoderado1.NombreGiro!=null)? socioApoderado1.NombreGiro:"");// Apoderado Giro
		formx.setField("RELAPODE_1NOMBRES", socioApoderado1.esPersonaNatural() ? ((socioApoderado1.Nombres!=null)? socioApoderado1.Nombres:"") : ((socioApoderado1.RazonSocial!=null)? socioApoderado1.RazonSocial:""));// Apoderado Nombres
		formx.setField("RELAPODE_1NOMBRECOMPLETO", (socioApoderado1.NombreCompleto!=null)? socioApoderado1.NombreCompleto:"");// Apoderado Nombre completo
		formx.setField("RELAPODE_1NUMDOC", (socioApoderado1.DocumentoIdentidad!=null)? socioApoderado1.DocumentoIdentidad:"");// Apoderado Numero Documento (Pasaporte, Rut)
		formx.setField("RELAPODE_1PAIS", (socioApoderado1.NombrePaisNacionalidadPrincipal!=null)? socioApoderado1.NombrePaisNacionalidadPrincipal:"");// Apoderado Pais Nacionalidad
		formx.setField("RELAPODE_1PAIS_NACIMIENTO", (socioApoderado1.NombrePaisNacimiento!=null)? socioApoderado1.NombrePaisNacimiento:"");// Apoderado Pais Nacimiento
		formx.setField("RELAPODE_1PAIS_RESIDENCIA", (socioApoderado1.NombrePaisResidencia!=null)? socioApoderado1.NombrePaisResidencia:"");// Apoderado Pais Residencia
		formx.setField("RELAPODE_1RUT", (socioApoderado1.DocumentoIdentidad!=null)? socioApoderado1.DocumentoIdentidad:"");// Apoderado Rut
		formx.setField("RELAPODE_1RUTDV", (socioApoderado1.DocumentoIdentidad!=null)? socioApoderado1.DocumentoIdentidad:"");// Apoderado Rut + Digito Verificador
		formx.setField("RELAPODE_1SEXO", (socioApoderado1.Sexo!=null)? socioApoderado1.Sexo:"");// Apoderado Sexo
		formx.setField("RELAPODE_1TIPO", (socioApoderado1.TipoPersona!=null)? socioApoderado1.TipoPersona:"");// Apoderado Tipo (Juridica, Natural)
		formx.setField("RELAPODE_1TELEFONO_PRINCIPAL_TIPO", (socioApoderado1.TipoTelefonoPrincipal!=null)? socioApoderado1.TipoTelefonoPrincipal:"");
		formx.setField("RELAPODE_1TELEFONO_PRINCIPAL_NUMERO", (socioApoderado1.NumeroTelefonoPrincipal!=null)? socioApoderado1.NumeroTelefonoPrincipal:"");
		formx.setField("RELAPODE_1TELEFONO_MOVIL", (socioApoderado1.NumeroTelefonoMovil!=null)? socioApoderado1.NumeroTelefonoMovil:"");
		formx.setField("RELAPODE_1TELEFONO_FIJO", (socioApoderado1.NumeroTelefonoFijo!=null)? socioApoderado1.NumeroTelefonoFijo:"");
		formx.setField("RELAPODE_1DIRECCION_ELECTRONICA", (socioApoderado1.DireccionElectronicaPrincipal!=null)? socioApoderado1.DireccionElectronicaPrincipal:"");
		formx.setField("RELAPODE_1MAILCOMERCIAL", (socioApoderado1.DireccionElectronicaComercial!=null)? socioApoderado1.DireccionElectronicaComercial:"");
		formx.setField("RELAPODE_1MAILLABORAL", (socioApoderado1.DireccionElectronicaLaboral!=null)? socioApoderado1.DireccionElectronicaLaboral:"");
		formx.setField("RELAPODE_1RELACION_CORREDORA_NGC380", (socioApoderado1.IndicadorNCG380!=null)? socioApoderado1.IndicadorNCG380:"");
		formx.setField("RELAPODE_1PERID", (socioApoderado1.IdPersona!=null)? socioApoderado1.IdPersona.toString():"");

		formx.setField("RELAPODE_2APELLIDOMATERNO", (socioApoderado2.ApellidoMaterno!=null)? socioApoderado2.ApellidoMaterno:"");// Apoderado Apellido Materno
		formx.setField("RELAPODE_2APELLIDOPATERNO", (socioApoderado2.ApellidoPaterno!=null)? socioApoderado2.ApellidoPaterno:"");// Apoderado Apellido Paterno
		formx.setField("RELAPODE_2DIRECCIONPARTICULAR",((socioApoderado2.DireccionFisicaCalle!=null)? socioApoderado2.DireccionFisicaCalle:"") + " " + ((socioApoderado2.DireccionFisicaNumero!=null)? socioApoderado2.DireccionFisicaNumero:""));// Apoderado Direccion Particular
		formx.setField("RELAPODE_2DIRECCIONPARTICULARCALLE", (socioApoderado2.DireccionFisicaCalle!=null)? socioApoderado2.DireccionFisicaCalle:"");// Apoderado Direccion Particular Numero
		formx.setField("RELAPODE_2DIRECCIONPARTICULARNUMERO", (socioApoderado2.DireccionFisicaNumero!=null)? socioApoderado2.DireccionFisicaNumero:"");// Apoderado Direccion Particular Numero
		formx.setField("RELAPODE_2DIRECCIONPARTICULARCIUDAD", (socioApoderado2.DireccionFisicaNombreCiudad!=null)? socioApoderado2.DireccionFisicaNombreCiudad:"");// Apoderado Direccion Particular Ciudad
		formx.setField("RELAPODE_2DIRECCIONPARTICULARCOMUNA", (socioApoderado2.DireccionFisicaNombreComuna!=null)? socioApoderado2.DireccionFisicaNombreComuna:"");// Apoderado Direccion Particular Comuna
		formx.setField("RELAPODE_2DIRECCIONPARTICULARREGION", (socioApoderado2.DireccionFisicaNombreRegion!=null)? socioApoderado2.DireccionFisicaNombreRegion:"");// Apoderado Direccion Particular Region
		formx.setField("RELAPODE_2DIRECCIONPARTICULARPAIS", (socioApoderado2.DireccionFisicaNombrePais!=null)? socioApoderado2.DireccionFisicaNombrePais:"");// Apoderado Direccion Particular Pais
		formx.setField("RELAPODE_2DV", (socioApoderado2.DigitoVerificador!=null)? socioApoderado2.DigitoVerificador:"");// Apoderado Digito Verificador
		formx.setField("RELAPODE_2GIRO", (socioApoderado2.NombreGiro!=null)? socioApoderado2.NombreGiro:"");// Apoderado Giro
		formx.setField("RELAPODE_2NOMBRES", socioApoderado2.esPersonaNatural() ? ((socioApoderado2.Nombres!=null)? socioApoderado2.Nombres:"") : ((socioApoderado2.RazonSocial!=null)? socioApoderado2.RazonSocial:""));// Apoderado Nombres
		formx.setField("RELAPODE_2NOMBRECOMPLETO", (socioApoderado2.NombreCompleto!=null)? socioApoderado2.NombreCompleto:"");// Apoderado Nombre completo
		formx.setField("RELAPODE_2NUMDOC", (socioApoderado2.DocumentoIdentidad!=null)? socioApoderado2.DocumentoIdentidad:"");// Apoderado Numero Documento (Pasaporte, Rut)
		formx.setField("RELAPODE_2PAIS", (socioApoderado2.NombrePaisNacionalidadPrincipal!=null)? socioApoderado2.NombrePaisNacionalidadPrincipal:"");// Apoderado Pais Nacionalidad
		formx.setField("RELAPODE_2PAIS_NACIMIENTO", (socioApoderado2.NombrePaisNacimiento!=null)? socioApoderado2.NombrePaisNacimiento:"");// Apoderado Pais Nacimiento
		formx.setField("RELAPODE_2PAIS_RESIDENCIA", (socioApoderado2.NombrePaisResidencia!=null)? socioApoderado2.NombrePaisResidencia:"");// Apoderado Pais Residencia
		formx.setField("RELAPODE_2RUT", (socioApoderado2.DocumentoIdentidad!=null)? socioApoderado2.DocumentoIdentidad:"");// Apoderado Rut
		formx.setField("RELAPODE_2RUTDV", (socioApoderado2.DocumentoIdentidad!=null)? socioApoderado2.DocumentoIdentidad:"");// Apoderado Rut + Digito Verificador
		formx.setField("RELAPODE_2SEXO", (socioApoderado2.Sexo!=null)? socioApoderado2.Sexo:"");// Apoderado Sexo
		formx.setField("RELAPODE_2TIPO", (socioApoderado2.TipoPersona!=null)? socioApoderado2.TipoPersona:"");// Apoderado Tipo (Juridica, Natural)
		formx.setField("RELAPODE_2TELEFONO_PRINCIPAL_TIPO", (socioApoderado2.TipoTelefonoPrincipal!=null)? socioApoderado2.TipoTelefonoPrincipal:"");
		formx.setField("RELAPODE_2TELEFONO_PRINCIPAL_NUMERO", (socioApoderado2.NumeroTelefonoPrincipal!=null)? socioApoderado2.NumeroTelefonoPrincipal:"");
		formx.setField("RELAPODE_2TELEFONO_MOVIL", (socioApoderado2.NumeroTelefonoMovil!=null)? socioApoderado2.NumeroTelefonoMovil:"");
		formx.setField("RELAPODE_2TELEFONO_FIJO", (socioApoderado2.NumeroTelefonoFijo!=null)? socioApoderado2.NumeroTelefonoFijo:"");
		formx.setField("RELAPODE_2DIRECCION_ELECTRONICA", (socioApoderado2.DireccionElectronicaPrincipal!=null)? socioApoderado2.DireccionElectronicaPrincipal:"");
		formx.setField("RELAPODE_2MAILCOMERCIAL", (socioApoderado2.DireccionElectronicaComercial!=null)? socioApoderado2.DireccionElectronicaComercial:"");
		formx.setField("RELAPODE_2MAILLABORAL", (socioApoderado2.DireccionElectronicaLaboral!=null)? socioApoderado2.DireccionElectronicaLaboral:"");
		formx.setField("RELAPODE_2RELACION_CORREDORA_NGC380", (socioApoderado2.IndicadorNCG380!=null)? socioApoderado2.IndicadorNCG380:"");
		formx.setField("RELAPODE_2PERID", (socioApoderado2.IdPersona!=null)? socioApoderado2.IdPersona.toString():"");

		formx.setField("RELAPODE_3APELLIDOMATERNO", (socioApoderado3.ApellidoMaterno!=null)? socioApoderado3.ApellidoMaterno:"");// Apoderado Apellido Materno
		formx.setField("RELAPODE_3APELLIDOPATERNO", (socioApoderado3.ApellidoPaterno!=null)? socioApoderado3.ApellidoPaterno:"");// Apoderado Apellido Paterno
		formx.setField("RELAPODE_3DIRECCIONPARTICULAR",((socioApoderado3.DireccionFisicaCalle!=null)? socioApoderado3.DireccionFisicaCalle:"") + " " + ((socioApoderado3.DireccionFisicaNumero!=null)? socioApoderado3.DireccionFisicaNumero:""));// Apoderado Direccion Particular
		formx.setField("RELAPODE_3DIRECCIONPARTICULARCALLE", (socioApoderado3.DireccionFisicaCalle!=null)? socioApoderado3.DireccionFisicaCalle:"");// Apoderado Direccion Particular Numero
		formx.setField("RELAPODE_3DIRECCIONPARTICULARNUMERO", (socioApoderado3.DireccionFisicaNumero!=null)? socioApoderado3.DireccionFisicaNumero:"");// Apoderado Direccion Particular Numero
		formx.setField("RELAPODE_3DIRECCIONPARTICULARCIUDAD", (socioApoderado3.DireccionFisicaNombreCiudad!=null)? socioApoderado3.DireccionFisicaNombreCiudad:"");// Apoderado Direccion Particular Ciudad
		formx.setField("RELAPODE_3DIRECCIONPARTICULARCOMUNA", (socioApoderado3.DireccionFisicaNombreComuna!=null)? socioApoderado3.DireccionFisicaNombreComuna:"");// Apoderado Direccion Particular Comuna
		formx.setField("RELAPODE_3DIRECCIONPARTICULARREGION", (socioApoderado3.DireccionFisicaNombreRegion!=null)? socioApoderado3.DireccionFisicaNombreRegion:"");// Apoderado Direccion Particular Region
		formx.setField("RELAPODE_3DIRECCIONPARTICULARPAIS", (socioApoderado3.DireccionFisicaNombrePais!=null)? socioApoderado3.DireccionFisicaNombrePais:"");// Apoderado Direccion Particular Pais
		formx.setField("RELAPODE_3DV", (socioApoderado3.DigitoVerificador!=null)? socioApoderado3.DigitoVerificador:"");// Apoderado Digito Verificador
		formx.setField("RELAPODE_3GIRO", (socioApoderado3.NombreGiro!=null)? socioApoderado3.NombreGiro:"");// Apoderado Giro
		formx.setField("RELAPODE_3NOMBRES", socioApoderado3.esPersonaNatural() ? ((socioApoderado3.Nombres!=null)? socioApoderado3.Nombres:"") : ((socioApoderado3.RazonSocial!=null)? socioApoderado3.RazonSocial:""));// Apoderado Nombres
		formx.setField("RELAPODE_3NOMBRECOMPLETO", (socioApoderado3.NombreCompleto!=null)? socioApoderado3.NombreCompleto:"");// Apoderado Nombre completo
		formx.setField("RELAPODE_3NUMDOC", (socioApoderado3.DocumentoIdentidad!=null)? socioApoderado3.DocumentoIdentidad:"");// Apoderado Numero Documento (Pasaporte, Rut)
		formx.setField("RELAPODE_3PAIS", (socioApoderado3.NombrePaisNacionalidadPrincipal!=null)? socioApoderado3.NombrePaisNacionalidadPrincipal:"");// Apoderado Pais Nacionalidad
		formx.setField("RELAPODE_3PAIS_NACIMIENTO", (socioApoderado3.NombrePaisNacimiento!=null)? socioApoderado3.NombrePaisNacimiento:"");// Apoderado Pais Nacimiento
		formx.setField("RELAPODE_3PAIS_RESIDENCIA", (socioApoderado3.NombrePaisResidencia!=null)? socioApoderado3.NombrePaisResidencia:"");// Apoderado Pais Residencia
		formx.setField("RELAPODE_3RUT", (socioApoderado3.DocumentoIdentidad!=null)? socioApoderado3.DocumentoIdentidad:"");// Apoderado Rut
		formx.setField("RELAPODE_3RUTDV", (socioApoderado3.DocumentoIdentidad!=null)? socioApoderado3.DocumentoIdentidad:"");// Apoderado Rut + Digito Verificador
		formx.setField("RELAPODE_3SEXO", (socioApoderado3.Sexo!=null)? socioApoderado3.Sexo:"");// Apoderado Sexo
		formx.setField("RELAPODE_3TIPO", (socioApoderado3.TipoPersona!=null)? socioApoderado3.TipoPersona:"");// Apoderado Tipo (Juridica, Natural)
		formx.setField("RELAPODE_3TELEFONO_PRINCIPAL_TIPO", (socioApoderado3.TipoTelefonoPrincipal!=null)? socioApoderado3.TipoTelefonoPrincipal:"");
		formx.setField("RELAPODE_3TELEFONO_PRINCIPAL_NUMERO", (socioApoderado3.NumeroTelefonoPrincipal!=null)? socioApoderado3.NumeroTelefonoPrincipal:"");
		formx.setField("RELAPODE_3TELEFONO_MOVIL", (socioApoderado3.NumeroTelefonoMovil!=null)? socioApoderado3.NumeroTelefonoMovil:"");
		formx.setField("RELAPODE_3TELEFONO_FIJO", (socioApoderado3.NumeroTelefonoFijo!=null)? socioApoderado3.NumeroTelefonoFijo:"");
		formx.setField("RELAPODE_3DIRECCION_ELECTRONICA", (socioApoderado3.DireccionElectronicaPrincipal!=null)? socioApoderado3.DireccionElectronicaPrincipal:"");
		formx.setField("RELAPODE_3MAILCOMERCIAL", (socioApoderado3.DireccionElectronicaComercial!=null)? socioApoderado3.DireccionElectronicaComercial:"");
		formx.setField("RELAPODE_3MAILLABORAL", (socioApoderado3.DireccionElectronicaLaboral!=null)? socioApoderado3.DireccionElectronicaLaboral:"");
		formx.setField("RELAPODE_3RELACION_CORREDORA_NGC380", (socioApoderado3.IndicadorNCG380!=null)? socioApoderado3.IndicadorNCG380:"");
		formx.setField("RELAPODE_3PERID", (socioApoderado3.IdPersona!=null)? socioApoderado3.IdPersona.toString():"");

		formx.setField("RELAPODE_4APELLIDOMATERNO", (socioApoderado4.ApellidoMaterno!=null)? socioApoderado4.ApellidoMaterno:"");// Apoderado Apellido Materno
		formx.setField("RELAPODE_4APELLIDOPATERNO", (socioApoderado4.ApellidoPaterno!=null)? socioApoderado4.ApellidoPaterno:"");// Apoderado Apellido Paterno
		formx.setField("RELAPODE_4DIRECCIONPARTICULAR",((socioApoderado4.DireccionFisicaCalle!=null)? socioApoderado4.DireccionFisicaCalle:"") + " " + ((socioApoderado4.DireccionFisicaNumero!=null)? socioApoderado4.DireccionFisicaNumero:""));// Apoderado Direccion Particular
		formx.setField("RELAPODE_4DIRECCIONPARTICULARCALLE", (socioApoderado4.DireccionFisicaCalle!=null)? socioApoderado4.DireccionFisicaCalle:"");// Apoderado Direccion Particular Numero
		formx.setField("RELAPODE_4DIRECCIONPARTICULARNUMERO", (socioApoderado4.DireccionFisicaNumero!=null)? socioApoderado4.DireccionFisicaNumero:"");// Apoderado Direccion Particular Numero
		formx.setField("RELAPODE_4DIRECCIONPARTICULARCIUDAD", (socioApoderado4.DireccionFisicaNombreCiudad!=null)? socioApoderado4.DireccionFisicaNombreCiudad:"");// Apoderado Direccion Particular Ciudad
		formx.setField("RELAPODE_4DIRECCIONPARTICULARCOMUNA", (socioApoderado4.DireccionFisicaNombreComuna!=null)? socioApoderado4.DireccionFisicaNombreComuna:"");// Apoderado Direccion Particular Comuna
		formx.setField("RELAPODE_4DIRECCIONPARTICULARREGION", (socioApoderado4.DireccionFisicaNombreRegion!=null)? socioApoderado4.DireccionFisicaNombreRegion:"");// Apoderado Direccion Particular Region
		formx.setField("RELAPODE_4DIRECCIONPARTICULARPAIS", (socioApoderado4.DireccionFisicaNombrePais!=null)? socioApoderado4.DireccionFisicaNombrePais:"");// Apoderado Direccion Particular Pais
		formx.setField("RELAPODE_4DV", (socioApoderado4.DigitoVerificador!=null)? socioApoderado4.DigitoVerificador:"");// Apoderado Digito Verificador
		formx.setField("RELAPODE_4GIRO", (socioApoderado4.NombreGiro!=null)? socioApoderado4.NombreGiro:"");// Apoderado Giro
		formx.setField("RELAPODE_4NOMBRES", socioApoderado4.esPersonaNatural() ? ((socioApoderado4.Nombres!=null)? socioApoderado4.Nombres:"") : ((socioApoderado4.RazonSocial!=null)? socioApoderado4.RazonSocial:""));// Apoderado Nombres
		formx.setField("RELAPODE_4NOMBRECOMPLETO", (socioApoderado4.NombreCompleto!=null)? socioApoderado4.NombreCompleto:"");// Apoderado Nombre completo
		formx.setField("RELAPODE_4NUMDOC", (socioApoderado4.DocumentoIdentidad!=null)? socioApoderado4.DocumentoIdentidad:"");// Apoderado Numero Documento (Pasaporte, Rut)
		formx.setField("RELAPODE_4PAIS", (socioApoderado4.NombrePaisNacionalidadPrincipal!=null)? socioApoderado4.NombrePaisNacionalidadPrincipal:"");// Apoderado Pais Nacionalidad Principal
		formx.setField("RELAPODE_4PAIS_NACIMIENTO", (socioApoderado4.NombrePaisNacimiento!=null)? socioApoderado4.NombrePaisNacimiento:"");// Apoderado Pais Nacimiento
		formx.setField("RELAPODE_4PAIS_RESIDENCIA", (socioApoderado4.NombrePaisResidencia!=null)? socioApoderado4.NombrePaisResidencia:"");// Apoderado Pais Residencia
		formx.setField("RELAPODE_4RUT", (socioApoderado4.DocumentoIdentidad!=null)? socioApoderado4.DocumentoIdentidad:"");// Apoderado Rut
		formx.setField("RELAPODE_4RUTDV", (socioApoderado4.DocumentoIdentidad!=null)? socioApoderado4.DocumentoIdentidad:"");// Apoderado Rut + Digito Verificador
		formx.setField("RELAPODE_4SEXO", (socioApoderado4.Sexo!=null)? socioApoderado4.Sexo:"");// Apoderado Sexo
		formx.setField("RELAPODE_4TIPO", (socioApoderado4.TipoPersona!=null)? socioApoderado4.TipoPersona:"");// Apoderado Tipo (Juridica, Natural)
		formx.setField("RELAPODE_4TELEFONO_PRINCIPAL_TIPO", (socioApoderado4.TipoTelefonoPrincipal!=null)? socioApoderado4.TipoTelefonoPrincipal:"");
		formx.setField("RELAPODE_4TELEFONO_PRINCIPAL_NUMERO", (socioApoderado4.NumeroTelefonoPrincipal!=null)? socioApoderado4.NumeroTelefonoPrincipal:"");
		formx.setField("RELAPODE_4TELEFONO_MOVIL", (socioApoderado4.NumeroTelefonoMovil!=null)? socioApoderado4.NumeroTelefonoMovil:"");
		formx.setField("RELAPODE_4TELEFONO_FIJO", (socioApoderado4.NumeroTelefonoFijo!=null)? socioApoderado4.NumeroTelefonoFijo:"");
		formx.setField("RELAPODE_4DIRECCION_ELECTRONICA", (socioApoderado4.DireccionElectronicaPrincipal!=null)? socioApoderado4.DireccionElectronicaPrincipal:"");
		formx.setField("RELAPODE_4MAILCOMERCIAL", (socioApoderado4.DireccionElectronicaComercial!=null)? socioApoderado4.DireccionElectronicaComercial:"");
		formx.setField("RELAPODE_4MAILLABORAL", (socioApoderado4.DireccionElectronicaLaboral!=null)? socioApoderado4.DireccionElectronicaLaboral:"");
		formx.setField("RELAPODE_4RELACION_CORREDORA_NGC380", (socioApoderado4.IndicadorNCG380!=null)? socioApoderado4.IndicadorNCG380:"");
		formx.setField("RELAPODE_4PERID", (socioApoderado4.IdPersona!=null)? socioApoderado4.IdPersona.toString():"");
		
		//Informacion relacionado (apoderado)
		formx.setField("RELAPODE_5APELLIDOMATERNO", (socioApoderado5.ApellidoMaterno!=null)? socioApoderado5.ApellidoMaterno:"");// Apoderado Apellido Materno
		formx.setField("RELAPODE_5APELLIDOPATERNO", (socioApoderado5.ApellidoPaterno!=null)? socioApoderado5.ApellidoPaterno:"");// Apoderado Apellido Paterno
		formx.setField("RELAPODE_5DIRECCIONPARTICULAR",((socioApoderado5.DireccionFisicaCalle!=null)? socioApoderado5.DireccionFisicaCalle:"") + " " + ((socioApoderado5.DireccionFisicaNumero!=null)? socioApoderado5.DireccionFisicaNumero:""));// Apoderado Direccion Particular		
		formx.setField("RELAPODE_5DIRECCIONPARTICULARCALLE", (socioApoderado5.DireccionFisicaCalle!=null)? socioApoderado5.DireccionFisicaCalle:"");// Apoderado Direccion Particular Numero
		formx.setField("RELAPODE_5DIRECCIONPARTICULARNUMERO", (socioApoderado5.DireccionFisicaNumero!=null)? socioApoderado5.DireccionFisicaNumero:"");// Apoderado Direccion Particular Numero
		formx.setField("RELAPODE_5DIRECCIONPARTICULARCIUDAD", (socioApoderado5.DireccionFisicaNombreCiudad!=null)? socioApoderado5.DireccionFisicaNombreCiudad:"");// Apoderado Direccion Particular Ciudad
		formx.setField("RELAPODE_5DIRECCIONPARTICULARCOMUNA", (socioApoderado5.DireccionFisicaNombreComuna!=null)? socioApoderado5.DireccionFisicaNombreComuna:"");// Apoderado Direccion Particular Comuna
		formx.setField("RELAPODE_5DIRECCIONPARTICULARREGION", (socioApoderado5.DireccionFisicaNombreRegion!=null)? socioApoderado5.DireccionFisicaNombreRegion:"");// Apoderado Direccion Particular Region
		formx.setField("RELAPODE_5DIRECCIONPARTICULARPAIS", (socioApoderado5.DireccionFisicaNombrePais!=null)? socioApoderado5.DireccionFisicaNombrePais:"");// Apoderado Direccion Particular Pais
		formx.setField("RELAPODE_5DV", (socioApoderado5.DigitoVerificador!=null)? socioApoderado5.DigitoVerificador:"");// Apoderado Digito Verificador
		formx.setField("RELAPODE_5GIRO", (socioApoderado5.NombreGiro!=null)? socioApoderado5.NombreGiro:"");// Apoderado Giro
		formx.setField("RELAPODE_5NOMBRES", socioApoderado5.esPersonaNatural() ? ((socioApoderado5.Nombres!=null)? socioApoderado5.Nombres:"") : ((socioApoderado5.RazonSocial!=null)? socioApoderado5.RazonSocial:""));// Apoderado Nombres
		formx.setField("RELAPODE_5NOMBRECOMPLETO", (socioApoderado5.NombreCompleto!=null)? socioApoderado5.NombreCompleto:"");// Apoderado Nombre completo
		formx.setField("RELAPODE_5NUMDOC", (socioApoderado5.DocumentoIdentidad!=null)? socioApoderado5.DocumentoIdentidad:"");// Apoderado Numero Documento (Pasaporte, Rut)
		formx.setField("RELAPODE_5PAIS", (socioApoderado5.NombrePaisNacionalidadPrincipal!=null)? socioApoderado5.NombrePaisNacionalidadPrincipal:"");// Apoderado Pais Nacionalidad
		formx.setField("RELAPODE_5PAIS_NACIMIENTO", (socioApoderado5.NombrePaisNacimiento!=null)? socioApoderado5.NombrePaisNacimiento:"");// Apoderado Pais Nacimiento
		formx.setField("RELAPODE_5PAIS_RESIDENCIA", (socioApoderado5.NombrePaisResidencia!=null)? socioApoderado5.NombrePaisResidencia:"");// Apoderado Pais Residencia
		formx.setField("RELAPODE_5RUT", (socioApoderado5.DocumentoIdentidad!=null)? socioApoderado5.DocumentoIdentidad:"");// Apoderado Rut
		formx.setField("RELAPODE_5RUTDV", (socioApoderado5.DocumentoIdentidad!=null)? socioApoderado5.DocumentoIdentidad:"");// Apoderado Rut + Digito Verificador
		formx.setField("RELAPODE_5SEXO", (socioApoderado5.Sexo!=null)? socioApoderado5.Sexo:"");// Apoderado Sexo
		formx.setField("RELAPODE_5TIPO", (socioApoderado5.TipoPersona!=null)? socioApoderado5.TipoPersona:"");// Apoderado Tipo (Juridica, Natural)
		formx.setField("RELAPODE_5TELEFONO_PRINCIPAL_TIPO", (socioApoderado5.TipoTelefonoPrincipal!=null)? socioApoderado5.TipoTelefonoPrincipal:"");
		formx.setField("RELAPODE_5TELEFONO_PRINCIPAL_NUMERO", (socioApoderado5.NumeroTelefonoPrincipal!=null)? socioApoderado5.NumeroTelefonoPrincipal:"");
		formx.setField("RELAPODE_5TELEFONO_MOVIL", (socioApoderado5.NumeroTelefonoMovil!=null)? socioApoderado5.NumeroTelefonoMovil:"");
		formx.setField("RELAPODE_5TELEFONO_FIJO", (socioApoderado5.NumeroTelefonoFijo!=null)? socioApoderado5.NumeroTelefonoFijo:"");
		formx.setField("RELAPODE_5DIRECCION_ELECTRONICA", (socioApoderado5.DireccionElectronicaPrincipal!=null)? socioApoderado5.DireccionElectronicaPrincipal:"");
		formx.setField("RELAPODE_5MAILCOMERCIAL", (socioApoderado5.DireccionElectronicaComercial!=null)? socioApoderado5.DireccionElectronicaComercial:"");
		formx.setField("RELAPODE_5MAILLABORAL", (socioApoderado5.DireccionElectronicaLaboral!=null)? socioApoderado5.DireccionElectronicaLaboral:"");
		formx.setField("RELAPODE_5RELACION_CORREDORA_NGC380", (socioApoderado5.IndicadorNCG380!=null)? socioApoderado5.IndicadorNCG380:"");
		formx.setField("RELAPODE_5PERID", (socioApoderado5.IdPersona!=null)? socioApoderado5.IdPersona.toString():"");
		
		//Informacion relacionado (Autorizado a dar Ordenes)
		formx.setField("RELAUTORDARORDENES_1NOMBRECOMPLETO",(socioAutorizadoOrdenes1.NombreCompleto!=null)? socioAutorizadoOrdenes1.NombreCompleto: "");// Autorizado a dar Ordenes Nombre Completo
		formx.setField("RELAUTORDARORDENES_1APELLIDOMATERNO",(socioAutorizadoOrdenes1.ApellidoMaterno!=null)? socioAutorizadoOrdenes1.ApellidoMaterno: "");// Autorizado a dar Ordenes Apellido Materno
		formx.setField("RELAUTORDARORDENES_1APELLIDOPATERNO",(socioAutorizadoOrdenes1.ApellidoPaterno!=null)? socioAutorizadoOrdenes1.ApellidoPaterno:"");// Autorizado a dar Ordenes Apellido Paterno		
		formx.setField("RELAUTORDARORDENES_1DIRECCIONPARTICULAR",((socioAutorizadoOrdenes1.DireccionFisicaCalle!=null)? socioAutorizadoOrdenes1.DireccionFisicaCalle:"") + " " + ((socioAutorizadoOrdenes1.DireccionFisicaNumero!=null)? socioAutorizadoOrdenes1.DireccionFisicaNumero:""));// Autorizado a dar Ordenes Direccion Particular
		formx.setField("RELAUTORDARORDENES_1DIRECCIONPARTICULARCIUDAD",(socioAutorizadoOrdenes1.DireccionFisicaNombreCiudad!=null)? socioAutorizadoOrdenes1.DireccionFisicaNombreCiudad:"");// Autorizado a dar Ordenes Direccion Particular Ciudad
		formx.setField("RELAUTORDARORDENES_1DIRECCIONPARTICULARCOMUNA",(socioAutorizadoOrdenes1.DireccionFisicaNombreComuna!=null)? socioAutorizadoOrdenes1.DireccionFisicaNombreComuna:""); // Autorizado a dar Ordenes Direccion Particular Comuna
		formx.setField("RELAUTORDARORDENES_1DIRECCIONPARTICULARREGION",(socioAutorizadoOrdenes1.DireccionFisicaNombreRegion!=null)? socioAutorizadoOrdenes1.DireccionFisicaNombreRegion:""); // Autorizado a dar Ordenes Direccion Particular Region
		formx.setField("RELAUTORDARORDENES_1DIRECCIONPARTICULARPAIS",(socioAutorizadoOrdenes1.DireccionFisicaNombrePais!=null)? socioAutorizadoOrdenes1.DireccionFisicaNombrePais:""); // Autorizado a dar Ordenes Direccion Particular Pais
		formx.setField("RELAUTORDARORDENES_1DV",(socioAutorizadoOrdenes1.DocumentoIdentidad!=null)? (socioAutorizadoOrdenes1.DocumentoIdentidad.substring(socioAutorizadoOrdenes1.DocumentoIdentidad.length()-1,socioAutorizadoOrdenes1.DocumentoIdentidad.length())):"") ;// Autorizado a dar Ordenes Digito Verificador		
		formx.setField("RELAUTORDARORDENES_1GIRO",(socioAutorizadoOrdenes1.NombreGiro!=null)? socioAutorizadoOrdenes1.NombreGiro:"");// Autorizado a dar Ordenes Giro
		formx.setField("RELAUTORDARORDENES_1NOMBRES",(socioAutorizadoOrdenes1.Nombres!=null)? socioAutorizadoOrdenes1.Nombres:"");// Autorizado a dar Ordenes Nombres
		formx.setField("RELAUTORDARORDENES_1NUMDOC",(socioAutorizadoOrdenes1.DocumentoIdentidad!=null)? socioAutorizadoOrdenes1.DocumentoIdentidad:"");// Autorizado a dar Ordenes Numero Documento (Pasaporte, Rut)
		formx.setField("RELAUTORDARORDENES_1PAIS",(socioAutorizadoOrdenes1.NombrePaisNacionalidadPrincipal!=null)? socioAutorizadoOrdenes1.NombrePaisNacionalidadPrincipal:"");// Autorizado a dar Ordenes Pais
		formx.setField("RELAUTORDARORDENES_1RUT",(socioAutorizadoOrdenes1.DocumentoIdentidad!=null)? socioAutorizadoOrdenes1.DocumentoIdentidad:"");// Autorizado a dar Ordenes Rut
		formx.setField("RELAUTORDARORDENES_1RUTDV",(socioAutorizadoOrdenes1.DocumentoIdentidad!=null)? socioAutorizadoOrdenes1.DocumentoIdentidad:"");// Autorizado a dar Ordenes Rut + Digito Verificador
		formx.setField("RELAUTORDARORDENES_1SEXO",(socioAutorizadoOrdenes1.Sexo!=null)? socioAutorizadoOrdenes1.Sexo:"");// Autorizado a dar Ordenes Sexo
		formx.setField("RELAUTORDARORDENES_1TIPO",(socioAutorizadoOrdenes1.TipoPersona!=null)? socioAutorizadoOrdenes1.TipoPersona:"");// Autorizado a dar Ordenes Tipo (Juridica, Natural)
		formx.setField("RELAUTORDARORDENES_1TELEFONOMOVIL",(socioAutorizadoOrdenes1.NumeroTelefonoMovil!=null)? socioAutorizadoOrdenes1.NumeroTelefonoMovil:"");
		formx.setField("RELAUTORDARORDENES_1MAILPARTICULAR",(socioAutorizadoOrdenes1.DireccionElectronicaPrincipal!=null)? socioAutorizadoOrdenes1.DireccionElectronicaPrincipal:"");
		formx.setField("RELAUTORDARORDENES_1RELCORREDORANCG380",(socioAutorizadoOrdenes1.IndicadorNCG380!=null)? socioAutorizadoOrdenes1.IndicadorNCG380:"");
		formx.setField("RELAUTORDARORDENES_1ESTADOCIVIL",(socioAutorizadoOrdenes1.EstadoCivil!=null)? socioAutorizadoOrdenes1.EstadoCivil:"");		
		formx.setField("RELAUTORDARORDENES_1MAILCOMERCIAL",(socioAutorizadoOrdenes1.DireccionElectronicaComercial!=null)? socioAutorizadoOrdenes1.DireccionElectronicaComercial:"");
		formx.setField("RELAUTORDARORDENES_1MAILLABORAL",(socioAutorizadoOrdenes1.DireccionElectronicaLaboral!=null)? socioAutorizadoOrdenes1.DireccionElectronicaLaboral:"");
		formx.setField("RELAUTORDARORDENES_1TELEFONOFIJO",(socioAutorizadoOrdenes1.NumeroTelefonoFijo!=null)? socioAutorizadoOrdenes1.NumeroTelefonoFijo:"");
		formx.setField("RELAUTORDARORDENES_1CARGO",(socioAutorizadoOrdenes1.Cargo!=null)? socioAutorizadoOrdenes1.Cargo:"");
		formx.setField("RELAUTORDARORDENES_1PERID",(socioAutorizadoOrdenes1.IdPersona!=null)? socioAutorizadoOrdenes1.IdPersona.toString():"");
		
		formx.setField("RELAUTORDARORDENES_2NOMBRECOMPLETO",(socioAutorizadoOrdenes2.NombreCompleto!=null)? socioAutorizadoOrdenes2.NombreCompleto: "");// Autorizado a dar Ordenes Nombre Completo
		formx.setField("RELAUTORDARORDENES_2APELLIDOMATERNO",(socioAutorizadoOrdenes2.ApellidoMaterno!=null)? socioAutorizadoOrdenes2.ApellidoMaterno: "");// Autorizado a dar Ordenes Apellido Materno
		formx.setField("RELAUTORDARORDENES_2APELLIDOPATERNO",(socioAutorizadoOrdenes2.ApellidoPaterno!=null)? socioAutorizadoOrdenes2.ApellidoPaterno:"");// Autorizado a dar Ordenes Apellido Paterno		
		formx.setField("RELAUTORDARORDENES_2DIRECCIONPARTICULAR",((socioAutorizadoOrdenes2.DireccionFisicaCalle!=null)? socioAutorizadoOrdenes2.DireccionFisicaCalle:"") + " " + ((socioAutorizadoOrdenes2.DireccionFisicaNumero!=null)? socioAutorizadoOrdenes2.DireccionFisicaNumero:""));// Autorizado a dar Ordenes Direccion Particular
		formx.setField("RELAUTORDARORDENES_2DIRECCIONPARTICULARCIUDAD",(socioAutorizadoOrdenes2.DireccionFisicaNombreCiudad!=null)? socioAutorizadoOrdenes2.DireccionFisicaNombreCiudad:"");// Autorizado a dar Ordenes Direccion Particular Ciudad
		formx.setField("RELAUTORDARORDENES_2DIRECCIONPARTICULARCOMUNA",(socioAutorizadoOrdenes2.DireccionFisicaNombreComuna!=null)? socioAutorizadoOrdenes2.DireccionFisicaNombreComuna:""); // Autorizado a dar Ordenes Direccion Particular Comuna
		formx.setField("RELAUTORDARORDENES_2DIRECCIONPARTICULARREGION",(socioAutorizadoOrdenes2.DireccionFisicaNombreRegion!=null)? socioAutorizadoOrdenes2.DireccionFisicaNombreRegion:""); // Autorizado a dar Ordenes Direccion Particular Region
		formx.setField("RELAUTORDARORDENES_2DIRECCIONPARTICULARPAIS",(socioAutorizadoOrdenes2.DireccionFisicaNombrePais!=null)? socioAutorizadoOrdenes2.DireccionFisicaNombrePais:""); // Autorizado a dar Ordenes Direccion Particular Pais
		formx.setField("RELAUTORDARORDENES_2DV",(socioAutorizadoOrdenes2.DocumentoIdentidad!=null)? (socioAutorizadoOrdenes2.DocumentoIdentidad.substring(socioAutorizadoOrdenes2.DocumentoIdentidad.length()-2,socioAutorizadoOrdenes2.DocumentoIdentidad.length())):"") ;// Autorizado a dar Ordenes Digito Verificador		
		formx.setField("RELAUTORDARORDENES_2GIRO",(socioAutorizadoOrdenes2.NombreGiro!=null)? socioAutorizadoOrdenes2.NombreGiro:"");// Autorizado a dar Ordenes Giro
		formx.setField("RELAUTORDARORDENES_2NOMBRES",(socioAutorizadoOrdenes2.Nombres!=null)? socioAutorizadoOrdenes2.Nombres:"");// Autorizado a dar Ordenes Nombres
		formx.setField("RELAUTORDARORDENES_2NUMDOC",(socioAutorizadoOrdenes2.DocumentoIdentidad!=null)? socioAutorizadoOrdenes2.DocumentoIdentidad:"");// Autorizado a dar Ordenes Numero Documento (Pasaporte, Rut)
		formx.setField("RELAUTORDARORDENES_2PAIS",(socioAutorizadoOrdenes2.NombrePaisNacionalidadPrincipal!=null)? socioAutorizadoOrdenes2.NombrePaisNacionalidadPrincipal:"");// Autorizado a dar Ordenes Pais
		formx.setField("RELAUTORDARORDENES_2RUT",(socioAutorizadoOrdenes2.DocumentoIdentidad!=null)? socioAutorizadoOrdenes2.DocumentoIdentidad:"");// Autorizado a dar Ordenes Rut
		formx.setField("RELAUTORDARORDENES_2RUTDV",(socioAutorizadoOrdenes2.DocumentoIdentidad!=null)? socioAutorizadoOrdenes2.DocumentoIdentidad:"");// Autorizado a dar Ordenes Rut + Digito Verificador
		formx.setField("RELAUTORDARORDENES_2SEXO",(socioAutorizadoOrdenes2.Sexo!=null)? socioAutorizadoOrdenes2.Sexo:"");// Autorizado a dar Ordenes Sexo
		formx.setField("RELAUTORDARORDENES_2TIPO",(socioAutorizadoOrdenes2.TipoPersona!=null)? socioAutorizadoOrdenes2.TipoPersona:"");// Autorizado a dar Ordenes Tipo (Juridica, Natural)
		formx.setField("RELAUTORDARORDENES_2TELEFONOMOVIL",(socioAutorizadoOrdenes2.NumeroTelefonoMovil!=null)? socioAutorizadoOrdenes2.NumeroTelefonoMovil:"");
		formx.setField("RELAUTORDARORDENES_2MAILPARTICULAR",(socioAutorizadoOrdenes2.DireccionElectronicaPrincipal!=null)? socioAutorizadoOrdenes2.DireccionElectronicaPrincipal:"");
		formx.setField("RELAUTORDARORDENES_2RELCORREDORANCG380",(socioAutorizadoOrdenes2.IndicadorNCG380!=null)? socioAutorizadoOrdenes2.IndicadorNCG380:"");
		formx.setField("RELAUTORDARORDENES_2ESTADOCIVIL",(socioAutorizadoOrdenes2.EstadoCivil!=null)? socioAutorizadoOrdenes2.EstadoCivil:"");	
		formx.setField("RELAUTORDARORDENES_2MAILCOMERCIAL",(socioAutorizadoOrdenes2.DireccionElectronicaComercial!=null)? socioAutorizadoOrdenes2.DireccionElectronicaComercial:"");
		formx.setField("RELAUTORDARORDENES_2MAILLABORAL",(socioAutorizadoOrdenes2.DireccionElectronicaLaboral!=null)? socioAutorizadoOrdenes2.DireccionElectronicaLaboral:"");
		formx.setField("RELAUTORDARORDENES_2TELEFONOFIJO",(socioAutorizadoOrdenes2.NumeroTelefonoFijo!=null)? socioAutorizadoOrdenes2.NumeroTelefonoFijo:"");
		formx.setField("RELAUTORDARORDENES_2CARGO",(socioAutorizadoOrdenes2.Cargo!=null)? socioAutorizadoOrdenes2.Cargo:"");
		formx.setField("RELAUTORDARORDENES_2PERID",(socioAutorizadoOrdenes2.IdPersona!=null)? socioAutorizadoOrdenes2.IdPersona.toString():"");
		
		formx.setField("RELAUTORDARORDENES_3NOMBRECOMPLETO",(socioAutorizadoOrdenes3.NombreCompleto!=null)? socioAutorizadoOrdenes3.NombreCompleto: "");// Autorizado a dar Ordenes Nombre Completo
		formx.setField("RELAUTORDARORDENES_3APELLIDOMATERNO",(socioAutorizadoOrdenes3.ApellidoMaterno!=null)? socioAutorizadoOrdenes3.ApellidoMaterno: "");// Autorizado a dar Ordenes Apellido Materno
		formx.setField("RELAUTORDARORDENES_3APELLIDOPATERNO",(socioAutorizadoOrdenes3.ApellidoPaterno!=null)? socioAutorizadoOrdenes3.ApellidoPaterno:"");// Autorizado a dar Ordenes Apellido Paterno		
		formx.setField("RELAUTORDARORDENES_3DIRECCIONPARTICULAR",((socioAutorizadoOrdenes3.DireccionFisicaCalle!=null)? socioAutorizadoOrdenes3.DireccionFisicaCalle:"") + " " + ((socioAutorizadoOrdenes3.DireccionFisicaNumero!=null)? socioAutorizadoOrdenes3.DireccionFisicaNumero:""));// Autorizado a dar Ordenes Direccion Particular
		formx.setField("RELAUTORDARORDENES_3DIRECCIONPARTICULARCIUDAD",(socioAutorizadoOrdenes3.DireccionFisicaNombreCiudad!=null)? socioAutorizadoOrdenes3.DireccionFisicaNombreCiudad:"");// Autorizado a dar Ordenes Direccion Particular Ciudad
		formx.setField("RELAUTORDARORDENES_3DIRECCIONPARTICULARCOMUNA",(socioAutorizadoOrdenes3.DireccionFisicaNombreComuna!=null)? socioAutorizadoOrdenes3.DireccionFisicaNombreComuna:""); // Autorizado a dar Ordenes Direccion Particular Comuna
		formx.setField("RELAUTORDARORDENES_3DIRECCIONPARTICULARREGION",(socioAutorizadoOrdenes3.DireccionFisicaNombreRegion!=null)? socioAutorizadoOrdenes3.DireccionFisicaNombreRegion:""); // Autorizado a dar Ordenes Direccion Particular Region
		formx.setField("RELAUTORDARORDENES_3DIRECCIONPARTICULARPAIS",(socioAutorizadoOrdenes3.DireccionFisicaNombrePais!=null)? socioAutorizadoOrdenes3.DireccionFisicaNombrePais:""); // Autorizado a dar Ordenes Direccion Particular Pais
		formx.setField("RELAUTORDARORDENES_3DV",(socioAutorizadoOrdenes3.DocumentoIdentidad!=null)? (socioAutorizadoOrdenes3.DocumentoIdentidad.substring(socioAutorizadoOrdenes3.DocumentoIdentidad.length()-3,socioAutorizadoOrdenes3.DocumentoIdentidad.length())):"") ;// Autorizado a dar Ordenes Digito Verificador		
		formx.setField("RELAUTORDARORDENES_3GIRO",(socioAutorizadoOrdenes3.NombreGiro!=null)? socioAutorizadoOrdenes3.NombreGiro:"");// Autorizado a dar Ordenes Giro
		formx.setField("RELAUTORDARORDENES_3NOMBRES",(socioAutorizadoOrdenes3.Nombres!=null)? socioAutorizadoOrdenes3.Nombres:"");// Autorizado a dar Ordenes Nombres
		formx.setField("RELAUTORDARORDENES_3NUMDOC",(socioAutorizadoOrdenes3.DocumentoIdentidad!=null)? socioAutorizadoOrdenes3.DocumentoIdentidad:"");// Autorizado a dar Ordenes Numero Documento (Pasaporte, Rut)
		formx.setField("RELAUTORDARORDENES_3PAIS",(socioAutorizadoOrdenes3.NombrePaisNacionalidadPrincipal!=null)? socioAutorizadoOrdenes3.NombrePaisNacionalidadPrincipal:"");// Autorizado a dar Ordenes Pais
		formx.setField("RELAUTORDARORDENES_3RUT",(socioAutorizadoOrdenes3.DocumentoIdentidad!=null)? socioAutorizadoOrdenes3.DocumentoIdentidad:"");// Autorizado a dar Ordenes Rut
		formx.setField("RELAUTORDARORDENES_3RUTDV",(socioAutorizadoOrdenes3.DocumentoIdentidad!=null)? socioAutorizadoOrdenes3.DocumentoIdentidad:"");// Autorizado a dar Ordenes Rut + Digito Verificador
		formx.setField("RELAUTORDARORDENES_3SEXO",(socioAutorizadoOrdenes3.Sexo!=null)? socioAutorizadoOrdenes3.Sexo:"");// Autorizado a dar Ordenes Sexo
		formx.setField("RELAUTORDARORDENES_3TIPO",(socioAutorizadoOrdenes3.TipoPersona!=null)? socioAutorizadoOrdenes3.TipoPersona:"");// Autorizado a dar Ordenes Tipo (Juridica, Natural)
		formx.setField("RELAUTORDARORDENES_3TELEFONOMOVIL",(socioAutorizadoOrdenes3.NumeroTelefonoMovil!=null)? socioAutorizadoOrdenes3.NumeroTelefonoMovil:"");
		formx.setField("RELAUTORDARORDENES_3MAILPARTICULAR",(socioAutorizadoOrdenes3.DireccionElectronicaPrincipal!=null)? socioAutorizadoOrdenes3.DireccionElectronicaPrincipal:"");
		formx.setField("RELAUTORDARORDENES_3RELCORREDORANCG380",(socioAutorizadoOrdenes3.IndicadorNCG380!=null)? socioAutorizadoOrdenes3.IndicadorNCG380:"");
		formx.setField("RELAUTORDARORDENES_3ESTADOCIVIL",(socioAutorizadoOrdenes3.EstadoCivil!=null)? socioAutorizadoOrdenes3.EstadoCivil:"");
		formx.setField("RELAUTORDARORDENES_3MAILCOMERCIAL",(socioAutorizadoOrdenes3.DireccionElectronicaComercial!=null)? socioAutorizadoOrdenes3.DireccionElectronicaComercial:"");
		formx.setField("RELAUTORDARORDENES_3MAILLABORAL",(socioAutorizadoOrdenes3.DireccionElectronicaLaboral!=null)? socioAutorizadoOrdenes3.DireccionElectronicaLaboral:"");
		formx.setField("RELAUTORDARORDENES_3TELEFONOFIJO",(socioAutorizadoOrdenes3.NumeroTelefonoFijo!=null)? socioAutorizadoOrdenes3.NumeroTelefonoFijo:"");
		formx.setField("RELAUTORDARORDENES_3CARGO",(socioAutorizadoOrdenes3.Cargo!=null)? socioAutorizadoOrdenes3.Cargo:"");
		formx.setField("RELAUTORDARORDENES_3PERID",(socioAutorizadoOrdenes3.IdPersona!=null)? socioAutorizadoOrdenes3.IdPersona.toString():"");
		
		formx.setField("RELAUTORDARORDENES_4NOMBRECOMPLETO",(socioAutorizadoOrdenes4.NombreCompleto!=null)? socioAutorizadoOrdenes4.NombreCompleto: "");// Autorizado a dar Ordenes Nombre Completo
		formx.setField("RELAUTORDARORDENES_4APELLIDOMATERNO",(socioAutorizadoOrdenes4.ApellidoMaterno!=null)? socioAutorizadoOrdenes4.ApellidoMaterno: "");// Autorizado a dar Ordenes Apellido Materno
		formx.setField("RELAUTORDARORDENES_4APELLIDOPATERNO",(socioAutorizadoOrdenes4.ApellidoPaterno!=null)? socioAutorizadoOrdenes4.ApellidoPaterno:"");// Autorizado a dar Ordenes Apellido Paterno		
		formx.setField("RELAUTORDARORDENES_4DIRECCIONPARTICULAR",((socioAutorizadoOrdenes4.DireccionFisicaCalle!=null)? socioAutorizadoOrdenes4.DireccionFisicaCalle:"") + " " + ((socioAutorizadoOrdenes4.DireccionFisicaNumero!=null)? socioAutorizadoOrdenes4.DireccionFisicaNumero:""));// Autorizado a dar Ordenes Direccion Particular
		formx.setField("RELAUTORDARORDENES_4DIRECCIONPARTICULARCIUDAD",(socioAutorizadoOrdenes4.DireccionFisicaNombreCiudad!=null)? socioAutorizadoOrdenes4.DireccionFisicaNombreCiudad:"");// Autorizado a dar Ordenes Direccion Particular Ciudad
		formx.setField("RELAUTORDARORDENES_4DIRECCIONPARTICULARCOMUNA",(socioAutorizadoOrdenes4.DireccionFisicaNombreComuna!=null)? socioAutorizadoOrdenes4.DireccionFisicaNombreComuna:""); // Autorizado a dar Ordenes Direccion Particular Comuna
		formx.setField("RELAUTORDARORDENES_4DIRECCIONPARTICULARREGION",(socioAutorizadoOrdenes4.DireccionFisicaNombreRegion!=null)? socioAutorizadoOrdenes4.DireccionFisicaNombreRegion:""); // Autorizado a dar Ordenes Direccion Particular Region
		formx.setField("RELAUTORDARORDENES_4DIRECCIONPARTICULARPAIS",(socioAutorizadoOrdenes4.DireccionFisicaNombrePais!=null)? socioAutorizadoOrdenes4.DireccionFisicaNombrePais:""); // Autorizado a dar Ordenes Direccion Particular Pais
		formx.setField("RELAUTORDARORDENES_4DV",(socioAutorizadoOrdenes4.DocumentoIdentidad!=null)? (socioAutorizadoOrdenes4.DocumentoIdentidad.substring(socioAutorizadoOrdenes4.DocumentoIdentidad.length()-4,socioAutorizadoOrdenes4.DocumentoIdentidad.length())):"") ;// Autorizado a dar Ordenes Digito Verificador		
		formx.setField("RELAUTORDARORDENES_4GIRO",(socioAutorizadoOrdenes4.NombreGiro!=null)? socioAutorizadoOrdenes4.NombreGiro:"");// Autorizado a dar Ordenes Giro
		formx.setField("RELAUTORDARORDENES_4NOMBRES",(socioAutorizadoOrdenes4.Nombres!=null)? socioAutorizadoOrdenes4.Nombres:"");// Autorizado a dar Ordenes Nombres
		formx.setField("RELAUTORDARORDENES_4NUMDOC",(socioAutorizadoOrdenes4.DocumentoIdentidad!=null)? socioAutorizadoOrdenes4.DocumentoIdentidad:"");// Autorizado a dar Ordenes Numero Documento (Pasaporte, Rut)
		formx.setField("RELAUTORDARORDENES_4PAIS",(socioAutorizadoOrdenes4.NombrePaisNacionalidadPrincipal!=null)? socioAutorizadoOrdenes4.NombrePaisNacionalidadPrincipal:"");// Autorizado a dar Ordenes Pais
		formx.setField("RELAUTORDARORDENES_4RUT",(socioAutorizadoOrdenes4.DocumentoIdentidad!=null)? socioAutorizadoOrdenes4.DocumentoIdentidad:"");// Autorizado a dar Ordenes Rut
		formx.setField("RELAUTORDARORDENES_4RUTDV",(socioAutorizadoOrdenes4.DocumentoIdentidad!=null)? socioAutorizadoOrdenes4.DocumentoIdentidad:"");// Autorizado a dar Ordenes Rut + Digito Verificador
		formx.setField("RELAUTORDARORDENES_4SEXO",(socioAutorizadoOrdenes4.Sexo!=null)? socioAutorizadoOrdenes4.Sexo:"");// Autorizado a dar Ordenes Sexo
		formx.setField("RELAUTORDARORDENES_4TIPO",(socioAutorizadoOrdenes4.TipoPersona!=null)? socioAutorizadoOrdenes4.TipoPersona:"");// Autorizado a dar Ordenes Tipo (Juridica, Natural)
		formx.setField("RELAUTORDARORDENES_4TELEFONOMOVIL",(socioAutorizadoOrdenes4.NumeroTelefonoMovil!=null)? socioAutorizadoOrdenes4.NumeroTelefonoMovil:"");
		formx.setField("RELAUTORDARORDENES_4MAILPARTICULAR",(socioAutorizadoOrdenes4.DireccionElectronicaPrincipal!=null)? socioAutorizadoOrdenes4.DireccionElectronicaPrincipal:"");
		formx.setField("RELAUTORDARORDENES_4RELCORREDORANCG480",(socioAutorizadoOrdenes4.IndicadorNCG380!=null)? socioAutorizadoOrdenes4.IndicadorNCG380:"");
		formx.setField("RELAUTORDARORDENES_4ESTADOCIVIL",(socioAutorizadoOrdenes4.EstadoCivil!=null)? socioAutorizadoOrdenes4.EstadoCivil:"");	
		formx.setField("RELAUTORDARORDENES_4MAILCOMERCIAL",(socioAutorizadoOrdenes4.DireccionElectronicaComercial!=null)? socioAutorizadoOrdenes4.DireccionElectronicaComercial:"");
		formx.setField("RELAUTORDARORDENES_4MAILLABORAL",(socioAutorizadoOrdenes4.DireccionElectronicaLaboral!=null)? socioAutorizadoOrdenes4.DireccionElectronicaLaboral:"");
		formx.setField("RELAUTORDARORDENES_4TELEFONOFIJO",(socioAutorizadoOrdenes4.NumeroTelefonoFijo!=null)? socioAutorizadoOrdenes4.NumeroTelefonoFijo:"");
		formx.setField("RELAUTORDARORDENES_4CARGO",(socioAutorizadoOrdenes4.Cargo!=null)? socioAutorizadoOrdenes4.Cargo:"");
		formx.setField("RELAUTORDARORDENES_4PERID",(socioAutorizadoOrdenes4.IdPersona!=null)? socioAutorizadoOrdenes4.IdPersona.toString():"");
		
		formx.setField("RELAUTORDARORDENES_5NOMBRECOMPLETO",(socioAutorizadoOrdenes5.NombreCompleto!=null)? socioAutorizadoOrdenes5.NombreCompleto: "");// Autorizado a dar Ordenes Nombre Completo
		formx.setField("RELAUTORDARORDENES_5APELLIDOMATERNO",(socioAutorizadoOrdenes5.ApellidoMaterno!=null)? socioAutorizadoOrdenes5.ApellidoMaterno: "");// Autorizado a dar Ordenes Apellido Materno
		formx.setField("RELAUTORDARORDENES_5APELLIDOPATERNO",(socioAutorizadoOrdenes5.ApellidoPaterno!=null)? socioAutorizadoOrdenes5.ApellidoPaterno:"");// Autorizado a dar Ordenes Apellido Paterno		
		formx.setField("RELAUTORDARORDENES_5DIRECCIONPARTICULAR",((socioAutorizadoOrdenes5.DireccionFisicaCalle!=null)? socioAutorizadoOrdenes5.DireccionFisicaCalle:"") + " " + ((socioAutorizadoOrdenes5.DireccionFisicaNumero!=null)? socioAutorizadoOrdenes5.DireccionFisicaNumero:""));// Autorizado a dar Ordenes Direccion Particular
		formx.setField("RELAUTORDARORDENES_5DIRECCIONPARTICULARCIUDAD",(socioAutorizadoOrdenes5.DireccionFisicaNombreCiudad!=null)? socioAutorizadoOrdenes5.DireccionFisicaNombreCiudad:"");// Autorizado a dar Ordenes Direccion Particular Ciudad
		formx.setField("RELAUTORDARORDENES_5DIRECCIONPARTICULARCOMUNA",(socioAutorizadoOrdenes5.DireccionFisicaNombreComuna!=null)? socioAutorizadoOrdenes5.DireccionFisicaNombreComuna:""); // Autorizado a dar Ordenes Direccion Particular Comuna
		formx.setField("RELAUTORDARORDENES_5DIRECCIONPARTICULARREGION",(socioAutorizadoOrdenes5.DireccionFisicaNombreRegion!=null)? socioAutorizadoOrdenes5.DireccionFisicaNombreRegion:""); // Autorizado a dar Ordenes Direccion Particular Region
		formx.setField("RELAUTORDARORDENES_5DIRECCIONPARTICULARPAIS",(socioAutorizadoOrdenes5.DireccionFisicaNombrePais!=null)? socioAutorizadoOrdenes5.DireccionFisicaNombrePais:""); // Autorizado a dar Ordenes Direccion Particular Pais
		formx.setField("RELAUTORDARORDENES_5DV",(socioAutorizadoOrdenes5.DocumentoIdentidad!=null)? (socioAutorizadoOrdenes5.DocumentoIdentidad.substring(socioAutorizadoOrdenes5.DocumentoIdentidad.length()-5,socioAutorizadoOrdenes5.DocumentoIdentidad.length())):"") ;// Autorizado a dar Ordenes Digito Verificador		
		formx.setField("RELAUTORDARORDENES_5GIRO",(socioAutorizadoOrdenes5.NombreGiro!=null)? socioAutorizadoOrdenes5.NombreGiro:"");// Autorizado a dar Ordenes Giro
		formx.setField("RELAUTORDARORDENES_5NOMBRES",(socioAutorizadoOrdenes5.Nombres!=null)? socioAutorizadoOrdenes5.Nombres:"");// Autorizado a dar Ordenes Nombres
		formx.setField("RELAUTORDARORDENES_5NUMDOC",(socioAutorizadoOrdenes5.DocumentoIdentidad!=null)? socioAutorizadoOrdenes5.DocumentoIdentidad:"");// Autorizado a dar Ordenes Numero Documento (Pasaporte, Rut)
		formx.setField("RELAUTORDARORDENES_5PAIS",(socioAutorizadoOrdenes5.NombrePaisNacionalidadPrincipal!=null)? socioAutorizadoOrdenes5.NombrePaisNacionalidadPrincipal:"");// Autorizado a dar Ordenes Pais
		formx.setField("RELAUTORDARORDENES_5RUT",(socioAutorizadoOrdenes5.DocumentoIdentidad!=null)? socioAutorizadoOrdenes5.DocumentoIdentidad:"");// Autorizado a dar Ordenes Rut
		formx.setField("RELAUTORDARORDENES_5RUTDV",(socioAutorizadoOrdenes5.DocumentoIdentidad!=null)? socioAutorizadoOrdenes5.DocumentoIdentidad:"");// Autorizado a dar Ordenes Rut + Digito Verificador
		formx.setField("RELAUTORDARORDENES_5SEXO",(socioAutorizadoOrdenes5.Sexo!=null)? socioAutorizadoOrdenes5.Sexo:"");// Autorizado a dar Ordenes Sexo
		formx.setField("RELAUTORDARORDENES_5TIPO",(socioAutorizadoOrdenes5.TipoPersona!=null)? socioAutorizadoOrdenes5.TipoPersona:"");// Autorizado a dar Ordenes Tipo (Juridica, Natural)
		formx.setField("RELAUTORDARORDENES_5TELEFONOMOVIL",(socioAutorizadoOrdenes5.NumeroTelefonoMovil!=null)? socioAutorizadoOrdenes5.NumeroTelefonoMovil:"");
		formx.setField("RELAUTORDARORDENES_5MAILPARTICULAR",(socioAutorizadoOrdenes5.DireccionElectronicaPrincipal!=null)? socioAutorizadoOrdenes5.DireccionElectronicaPrincipal:"");
		formx.setField("RELAUTORDARORDENES_5RELCORREDORANCG580",(socioAutorizadoOrdenes5.IndicadorNCG380!=null)? socioAutorizadoOrdenes5.IndicadorNCG380:"");
		formx.setField("RELAUTORDARORDENES_5ESTADOCIVIL",(socioAutorizadoOrdenes5.EstadoCivil!=null)? socioAutorizadoOrdenes5.EstadoCivil:"");		
		formx.setField("RELAUTORDARORDENES_5MAILCOMERCIAL",(socioAutorizadoOrdenes5.DireccionElectronicaComercial!=null)? socioAutorizadoOrdenes5.DireccionElectronicaComercial:"");
		formx.setField("RELAUTORDARORDENES_5MAILLABORAL",(socioAutorizadoOrdenes5.DireccionElectronicaLaboral!=null)? socioAutorizadoOrdenes5.DireccionElectronicaLaboral:"");
		formx.setField("RELAUTORDARORDENES_5TELEFONOFIJO",(socioAutorizadoOrdenes5.NumeroTelefonoFijo!=null)? socioAutorizadoOrdenes5.NumeroTelefonoFijo:"");
		formx.setField("RELAUTORDARORDENES_5CARGO",(socioAutorizadoOrdenes5.Cargo!=null)? socioAutorizadoOrdenes5.Cargo:"");
		formx.setField("RELAUTORDARORDENES_5PERID",(socioAutorizadoOrdenes5.IdPersona!=null)? socioAutorizadoOrdenes5.IdPersona.toString():"");
		
		formx.setField("RELAUTORDARORDENES_6NOMBRECOMPLETO",(socioAutorizadoOrdenes6.NombreCompleto!=null)? socioAutorizadoOrdenes6.NombreCompleto: "");// Autorizado a dar Ordenes Nombre Completo
		formx.setField("RELAUTORDARORDENES_6APELLIDOMATERNO",(socioAutorizadoOrdenes6.ApellidoMaterno!=null)? socioAutorizadoOrdenes6.ApellidoMaterno: "");// Autorizado a dar Ordenes Apellido Materno
		formx.setField("RELAUTORDARORDENES_6APELLIDOPATERNO",(socioAutorizadoOrdenes6.ApellidoPaterno!=null)? socioAutorizadoOrdenes6.ApellidoPaterno:"");// Autorizado a dar Ordenes Apellido Paterno		
		formx.setField("RELAUTORDARORDENES_6DIRECCIONPARTICULAR",((socioAutorizadoOrdenes6.DireccionFisicaCalle!=null)? socioAutorizadoOrdenes6.DireccionFisicaCalle:"") + " " + ((socioAutorizadoOrdenes6.DireccionFisicaNumero!=null)? socioAutorizadoOrdenes6.DireccionFisicaNumero:""));// Autorizado a dar Ordenes Direccion Particular
		formx.setField("RELAUTORDARORDENES_6DIRECCIONPARTICULARCIUDAD",(socioAutorizadoOrdenes6.DireccionFisicaNombreCiudad!=null)? socioAutorizadoOrdenes6.DireccionFisicaNombreCiudad:"");// Autorizado a dar Ordenes Direccion Particular Ciudad
		formx.setField("RELAUTORDARORDENES_6DIRECCIONPARTICULARCOMUNA",(socioAutorizadoOrdenes6.DireccionFisicaNombreComuna!=null)? socioAutorizadoOrdenes6.DireccionFisicaNombreComuna:""); // Autorizado a dar Ordenes Direccion Particular Comuna
		formx.setField("RELAUTORDARORDENES_6DIRECCIONPARTICULARREGION",(socioAutorizadoOrdenes6.DireccionFisicaNombreRegion!=null)? socioAutorizadoOrdenes6.DireccionFisicaNombreRegion:""); // Autorizado a dar Ordenes Direccion Particular Region
		formx.setField("RELAUTORDARORDENES_6DIRECCIONPARTICULARPAIS",(socioAutorizadoOrdenes6.DireccionFisicaNombrePais!=null)? socioAutorizadoOrdenes6.DireccionFisicaNombrePais:""); // Autorizado a dar Ordenes Direccion Particular Pais
		formx.setField("RELAUTORDARORDENES_6DV",(socioAutorizadoOrdenes6.DocumentoIdentidad!=null)? (socioAutorizadoOrdenes6.DocumentoIdentidad.substring(socioAutorizadoOrdenes6.DocumentoIdentidad.length()-6,socioAutorizadoOrdenes6.DocumentoIdentidad.length())):"") ;// Autorizado a dar Ordenes Digito Verificador		
		formx.setField("RELAUTORDARORDENES_6GIRO",(socioAutorizadoOrdenes6.NombreGiro!=null)? socioAutorizadoOrdenes6.NombreGiro:"");// Autorizado a dar Ordenes Giro
		formx.setField("RELAUTORDARORDENES_6NOMBRES",(socioAutorizadoOrdenes6.Nombres!=null)? socioAutorizadoOrdenes6.Nombres:"");// Autorizado a dar Ordenes Nombres
		formx.setField("RELAUTORDARORDENES_6NUMDOC",(socioAutorizadoOrdenes6.DocumentoIdentidad!=null)? socioAutorizadoOrdenes6.DocumentoIdentidad:"");// Autorizado a dar Ordenes Numero Documento (Pasaporte, Rut)
		formx.setField("RELAUTORDARORDENES_6PAIS",(socioAutorizadoOrdenes6.NombrePaisNacionalidadPrincipal!=null)? socioAutorizadoOrdenes6.NombrePaisNacionalidadPrincipal:"");// Autorizado a dar Ordenes Pais
		formx.setField("RELAUTORDARORDENES_6RUT",(socioAutorizadoOrdenes6.DocumentoIdentidad!=null)? socioAutorizadoOrdenes6.DocumentoIdentidad:"");// Autorizado a dar Ordenes Rut
		formx.setField("RELAUTORDARORDENES_6RUTDV",(socioAutorizadoOrdenes6.DocumentoIdentidad!=null)? socioAutorizadoOrdenes6.DocumentoIdentidad:"");// Autorizado a dar Ordenes Rut + Digito Verificador
		formx.setField("RELAUTORDARORDENES_6SEXO",(socioAutorizadoOrdenes6.Sexo!=null)? socioAutorizadoOrdenes6.Sexo:"");// Autorizado a dar Ordenes Sexo
		formx.setField("RELAUTORDARORDENES_6TIPO",(socioAutorizadoOrdenes6.TipoPersona!=null)? socioAutorizadoOrdenes6.TipoPersona:"");// Autorizado a dar Ordenes Tipo (Juridica, Natural)
		formx.setField("RELAUTORDARORDENES_6TELEFONOMOVIL",(socioAutorizadoOrdenes6.NumeroTelefonoMovil!=null)? socioAutorizadoOrdenes6.NumeroTelefonoMovil:"");
		formx.setField("RELAUTORDARORDENES_6MAILPARTICULAR",(socioAutorizadoOrdenes6.DireccionElectronicaPrincipal!=null)? socioAutorizadoOrdenes6.DireccionElectronicaPrincipal:"");
		formx.setField("RELAUTORDARORDENES_6RELCORREDORANCG680",(socioAutorizadoOrdenes6.IndicadorNCG380!=null)? socioAutorizadoOrdenes6.IndicadorNCG380:"");
		formx.setField("RELAUTORDARORDENES_6ESTADOCIVIL",(socioAutorizadoOrdenes6.EstadoCivil!=null)? socioAutorizadoOrdenes6.EstadoCivil:"");			
		formx.setField("RELAUTORDARORDENES_6MAILCOMERCIAL",(socioAutorizadoOrdenes6.DireccionElectronicaComercial!=null)? socioAutorizadoOrdenes6.DireccionElectronicaComercial:"");
		formx.setField("RELAUTORDARORDENES_6MAILLABORAL",(socioAutorizadoOrdenes6.DireccionElectronicaLaboral!=null)? socioAutorizadoOrdenes6.DireccionElectronicaLaboral:"");
		formx.setField("RELAUTORDARORDENES_6TELEFONOFIJO",(socioAutorizadoOrdenes6.NumeroTelefonoFijo!=null)? socioAutorizadoOrdenes6.NumeroTelefonoFijo:"");
		formx.setField("RELAUTORDARORDENES_6CARGO",(socioAutorizadoOrdenes6.Cargo!=null)? socioAutorizadoOrdenes6.Cargo:"");
		formx.setField("RELAUTORDARORDENES_6PERID",(socioAutorizadoOrdenes6.IdPersona!=null)? socioAutorizadoOrdenes6.IdPersona.toString():"");
		

		// Informacion empleador del socio
		formx.setField("EMPLEADORSOCIO_RUT", (socioEmpleador.DocumentoIdentidad!=null)? socioEmpleador.DocumentoIdentidad:"");
		formx.setField("EMPLEADORSOCIO_NOMBRECOMPLETO", (socioEmpleador.NombreCompleto!=null)? socioEmpleador.NombreCompleto:"");
		formx.setField("EMPLEADORSOCIO_NOMBRES", socioEmpleador.esPersonaNatural() ? ((socioEmpleador.Nombres!=null)? socioEmpleador.Nombres:"") : ((socioEmpleador.RazonSocial!=null)? socioEmpleador.RazonSocial:""));
		formx.setField("EMPLEADORSOCIO_APELLIDOMATERNO", (socioEmpleador.ApellidoMaterno!=null)? socioEmpleador.ApellidoMaterno:"");
		formx.setField("EMPLEADORSOCIO_APELLIDOPATERNO", (socioEmpleador.ApellidoPaterno!=null)? socioEmpleador.ApellidoPaterno:"");
		formx.setField("EMPLEADORSOCIO_DIRECCION_FISICA",((socioEmpleador.DireccionFisicaCalle!=null)? socioEmpleador.DireccionFisicaCalle:"") + " " + ((socioEmpleador.DireccionFisicaNumero!=null)? socioEmpleador.DireccionFisicaNumero:""));		
		formx.setField("EMPLEADORSOCIO_DIRECCION_FISICA_CALLE", (socioEmpleador.DireccionFisicaCalle!=null)? socioEmpleador.DireccionFisicaCalle:"");
		formx.setField("EMPLEADORSOCIO_DIRECCION_FISICA_NUMERO", (socioEmpleador.DireccionFisicaNumero!=null)? socioEmpleador.DireccionFisicaNumero:"");
		formx.setField("EMPLEADORSOCIO_DIRECCION_FISICA_PAIS", (socioEmpleador.DireccionFisicaNombrePais!=null)? socioEmpleador.DireccionFisicaNombrePais:"");
		formx.setField("EMPLEADORSOCIO_DIRECCION_FISICA_CIUDAD", (socioEmpleador.DireccionFisicaNombreCiudad!=null)? socioEmpleador.DireccionFisicaNombreCiudad:"");
		formx.setField("EMPLEADORSOCIO_DIRECCION_FISICA_COMUNA", (socioEmpleador.DireccionFisicaNombreComuna!=null)? socioEmpleador.DireccionFisicaNombreComuna:"");
		formx.setField("EMPLEADORSOCIO_TELEFONO_PRINCIPAL_TIPO", (socioEmpleador.TipoTelefonoPrincipal!=null)? socioEmpleador.TipoTelefonoPrincipal:"");
		formx.setField("EMPLEADORSOCIO_TELEFONO_PRINCIPAL_NUMERO", (socioEmpleador.NumeroTelefonoPrincipal!=null)? socioEmpleador.NumeroTelefonoPrincipal:"");
		formx.setField("EMPLEADORSOCIO_TELEFONO_MOVIL", (socioEmpleador.NumeroTelefonoMovil!=null)? socioEmpleador.NumeroTelefonoMovil:"");
		formx.setField("EMPLEADORSOCIO_TELEFONO_FIJO", (socioEmpleador.NumeroTelefonoFijo!=null)? socioEmpleador.NumeroTelefonoFijo:"");
		formx.setField("EMPLEADORSOCIO_RELCORREDORANCG380", (socioEmpleador.IndicadorNCG380!=null)? socioEmpleador.IndicadorNCG380:"");
		
		// Informacion conyuge del socio
		if ("CA".equalsIgnoreCase(socioPersona.CodigoEstadoCivil)) {
			formx.setField("CONYUGESOCIO_NOMBRECOMPLETO", (socioConyuge.NombreCompleto!=null)? socioConyuge.NombreCompleto:"");
			formx.setField("CONYUGESOCIO_NOMBRES", (socioConyuge.Nombres!=null)? socioConyuge.Nombres:"");
			formx.setField("CONYUGESOCIO_APELLIDOPATERNO", (socioConyuge.ApellidoPaterno!=null)? socioConyuge.ApellidoPaterno:"");
			formx.setField("CONYUGESOCIO_APELLIDOMATERNO", (socioConyuge.ApellidoPaterno!=null)? socioConyuge.ApellidoPaterno:"");
			formx.setField("CONYUGESOCIO_DIRECCIONPARTICULAR",((socioConyuge.DireccionFisicaCalle!=null)? socioConyuge.DireccionFisicaCalle:"") + " " + ((socioConyuge.DireccionFisicaNumero!=null)? socioConyuge.DireccionFisicaNumero:""));			
			formx.setField("CONYUGESOCIO_DIRECCIONPARTICULARCALLE", (socioConyuge.DireccionFisicaCalle!=null)? socioConyuge.DireccionFisicaCalle:"");
			formx.setField("CONYUGESOCIO_DIRECCIONPARTICULARNUMERO", (socioConyuge.DireccionFisicaNumero!=null)? socioConyuge.DireccionFisicaNumero:"");
			formx.setField("CONYUGESOCIO_DIRECCIONPARTICULARCIUDAD", (socioConyuge.DireccionFisicaNombreCiudad!=null)? socioConyuge.DireccionFisicaNombreCiudad:"");
			formx.setField("CONYUGESOCIO_DIRECCIONPARTICULARCOMUNA", (socioConyuge.DireccionFisicaNombreComuna!=null)? socioConyuge.DireccionFisicaNombreComuna:"");
			formx.setField("CONYUGESOCIO_DIRECCIONPARTICULARPAIS", (socioConyuge.DireccionFisicaNombrePais!=null)? socioConyuge.DireccionFisicaNombrePais:"");
			formx.setField("CONYUGESOCIO_DV", (socioConyuge.DigitoVerificador!=null)? socioConyuge.DigitoVerificador:"");
			formx.setField("CONYUGESOCIO_GIRO", (socioConyuge.NombreGiro!=null)? socioConyuge.NombreGiro:"");
			formx.setField("CONYUGESOCIO_NUMDOC", (socioConyuge.DocumentoIdentidad!=null)? socioConyuge.DocumentoIdentidad:"");
			formx.setField("CONYUGESOCIO_PAIS", (socioConyuge.NombrePaisNacionalidadPrincipal!=null)? socioConyuge.NombrePaisNacionalidadPrincipal:"");
			formx.setField("CONYUGESOCIO_PAISNACIMIENTO", (socioConyuge.NombrePaisNacimiento!=null)? socioConyuge.NombrePaisNacimiento:"");
			formx.setField("CONYUGESOCIO_RUT", (socioConyuge.DocumentoIdentidad!=null)? socioConyuge.DocumentoIdentidad:"");
			formx.setField("CONYUGESOCIO_RUTDV", (socioConyuge.DocumentoIdentidad!=null)? socioConyuge.DocumentoIdentidad:"");
			formx.setField("CONYUGESOCIO_SEXO", (socioConyuge.Sexo!=null)? socioConyuge.Sexo:"");
			formx.setField("CONYUGESOCIO_TIPO", (socioConyuge.TipoPersona!=null)? socioConyuge.TipoPersona:"");
			formx.setField("CONYUGESOCIO_RELCORREDORANCG380", (socioConyuge.IndicadorNCG380!=null)? socioConyuge.IndicadorNCG380:"");
		}

		// Informacion conviviente civil del socio
		if ("CC".equalsIgnoreCase(socioPersona.CodigoEstadoCivil)) {
			formx.setField("CONVIVIENTECIVILSOCIO_NOMBRECOMPLETO", (socioConyuge.NombreCompleto!=null)? socioConyuge.NombreCompleto:"");
			formx.setField("CONVIVIENTECIVILSOCIO_NOMBRES", (socioConyuge.Nombres!=null)? socioConyuge.Nombres:"");
			formx.setField("CONVIVIENTECIVILSOCIO_APELLIDOPATERNO", (socioConyuge.ApellidoPaterno!=null)? socioConyuge.ApellidoPaterno:"");
			formx.setField("CONVIVIENTECIVILSOCIO_APELLIDOMATERNO", (socioConyuge.ApellidoPaterno!=null)? socioConyuge.ApellidoPaterno:"");
			formx.setField("CONVIVIENTECIVILSOCIO_DIRECCIONPARTICULAR",((socioConyuge.DireccionFisicaCalle!=null)? socioConyuge.DireccionFisicaCalle:"") + " " + ((socioConyuge.DireccionFisicaNumero!=null)? socioConyuge.DireccionFisicaNumero:""));			
			formx.setField("CONVIVIENTECIVILSOCIO_DIRECCIONPARTICULARCALLE", (socioConyuge.DireccionFisicaCalle!=null)? socioConyuge.DireccionFisicaCalle:"");
			formx.setField("CONVIVIENTECIVILSOCIO_DIRECCIONPARTICULARNUMERO", (socioConyuge.DireccionFisicaNumero!=null)? socioConyuge.DireccionFisicaNumero:"");
			formx.setField("CONVIVIENTECIVILSOCIO_DIRECCIONPARTICULARCIUDAD", (socioConyuge.DireccionFisicaNombreCiudad!=null)? socioConyuge.DireccionFisicaNombreCiudad:"");
			formx.setField("CONVIVIENTECIVILSOCIO_DIRECCIONPARTICULARCOMUNA", (socioConyuge.DireccionFisicaNombreComuna!=null)? socioConyuge.DireccionFisicaNombreComuna:"");
			formx.setField("CONVIVIENTECIVILSOCIO_DIRECCIONPARTICULARPAIS", (socioConyuge.DireccionFisicaNombrePais!=null)? socioConyuge.DireccionFisicaNombrePais:"");
			formx.setField("CONVIVIENTECIVILSOCIO_DV", (socioConyuge.DigitoVerificador!=null)? socioConyuge.DigitoVerificador:"");
			formx.setField("CONVIVIENTECIVILSOCIO_GIRO", (socioConyuge.NombreGiro!=null)? socioConyuge.NombreGiro:"");
			formx.setField("CONVIVIENTECIVILSOCIO_NUMDOC", (socioConyuge.DocumentoIdentidad!=null)? socioConyuge.DocumentoIdentidad:"");
			formx.setField("CONVIVIENTECIVILSOCIO_PAIS", (socioConyuge.NombrePaisNacionalidadPrincipal!=null)? socioConyuge.NombrePaisNacionalidadPrincipal:"");
			formx.setField("CONVIVIENTECIVILSOCIO_PAISNACIMIENTO", (socioConyuge.NombrePaisNacimiento!=null)? socioConyuge.NombrePaisNacimiento:"");
			formx.setField("CONVIVIENTECIVILSOCIO_RUT", (socioConyuge.DocumentoIdentidad!=null)? socioConyuge.DocumentoIdentidad:"");
			formx.setField("CONVIVIENTECIVILSOCIO_RUTDV", (socioConyuge.DocumentoIdentidad!=null)? socioConyuge.DocumentoIdentidad:"");
			formx.setField("CONVIVIENTECIVILSOCIO_SEXO", (socioConyuge.Sexo!=null)? socioConyuge.Sexo:"");
			formx.setField("CONVIVIENTECIVILSOCIO_TIPO", (socioConyuge.TipoPersona!=null)? socioConyuge.TipoPersona:"");
			formx.setField("CONVIVIENTECIVILSOCIO_RELCORREDORANCG380", (socioConyuge.IndicadorNCG380!=null)? socioConyuge.IndicadorNCG380:"");
		}

		// Informacion representante 1 (Conyuge)
		if ("CA".equalsIgnoreCase(socioRepresentante1.CodigoEstadoCivil)) {
			formx.setField("RELCONYU_1NOMBRECOMPLETO", (socioRepresentante1Conyuge.NombreCompleto!=null)? socioRepresentante1Conyuge.NombreCompleto:"");
			formx.setField("RELCONYU_1NOMBRES", (socioRepresentante1Conyuge.Nombres!=null)? socioRepresentante1Conyuge.Nombres:"");// Conyugue Nombres
			formx.setField("RELCONYU_1APELLIDOMATERNO", (socioRepresentante1Conyuge.ApellidoMaterno!=null)? socioRepresentante1Conyuge.ApellidoMaterno:"");// Conyugue Apellido Materno
			formx.setField("RELCONYU_1APELLIDOPATERNO", (socioRepresentante1Conyuge.ApellidoPaterno!=null)? socioRepresentante1Conyuge.ApellidoPaterno:"");// Conyugue Apellido Paterno
			formx.setField("RELCONYU_1DIRECCIONPARTICULAR",((socioRepresentante1Conyuge.DireccionFisicaCalle!=null)? socioRepresentante1Conyuge.DireccionFisicaCalle:"") + " " + ((socioRepresentante1Conyuge.DireccionFisicaNumero!=null)? socioRepresentante1Conyuge.DireccionFisicaNumero:""));// Conyugue Direccion Particular
			formx.setField("RELCONYU_1DIRECCIONPARTICULARCALLE", (socioRepresentante1Conyuge.DireccionFisicaCalle!=null)? socioRepresentante1Conyuge.DireccionFisicaCalle:"");
			formx.setField("RELCONYU_1DIRECCIONPARTICULARNUMERO", (socioRepresentante1Conyuge.DireccionFisicaNumero!=null)? socioRepresentante1Conyuge.DireccionFisicaNumero:"");
			formx.setField("RELCONYU_1DIRECCIONPARTICULARCIUDAD", (socioRepresentante1Conyuge.DireccionFisicaNombreCiudad!=null)? socioRepresentante1Conyuge.DireccionFisicaNombreCiudad:"");// Conyugue Direccion Particular Ciudad
			formx.setField("RELCONYU_1DIRECCIONPARTICULARREGION", (socioRepresentante1Conyuge.DireccionFisicaNombreRegion!=null)? socioRepresentante1Conyuge.DireccionFisicaNombreRegion:"");// Conyugue Direccion Particular Region
			formx.setField("RELCONYU_1DIRECCIONPARTICULARCOMUNA", (socioRepresentante1Conyuge.DireccionFisicaNombreComuna!=null)? socioRepresentante1Conyuge.DireccionFisicaNombreComuna:"");// Conyugue Direccion Particular Comuna
			formx.setField("RELCONYU_1DIRECCIONPARTICULARPAIS", (socioRepresentante1Conyuge.DireccionFisicaNombrePais!=null)? socioRepresentante1Conyuge.DireccionFisicaNombrePais:"");
			formx.setField("RELCONYU_1DV", (socioRepresentante1Conyuge.DigitoVerificador!=null)? socioRepresentante1Conyuge.DigitoVerificador:"");// Conyugue Digito Verificador
			formx.setField("RELCONYU_1GIRO", (socioRepresentante1Conyuge.NombreGiro!=null)? socioRepresentante1Conyuge.NombreGiro:"");// Conyugue Giro
			formx.setField("RELCONYU_1NUMDOC", (socioRepresentante1Conyuge.DocumentoIdentidad!=null)? socioRepresentante1Conyuge.DocumentoIdentidad:"");// Conyugue Numero Documento (Pasaporte, Rut)
			formx.setField("RELCONYU_1PAIS", (socioRepresentante1Conyuge.NombrePaisNacionalidadPrincipal!=null)? socioRepresentante1Conyuge.NombrePaisNacionalidadPrincipal:"");
			formx.setField("RELCONYU_1RUT", (socioRepresentante1Conyuge.DocumentoIdentidad!=null)? socioRepresentante1Conyuge.DocumentoIdentidad:"");// Conyugue Rut
			formx.setField("RELCONYU_1RUTDV", (socioRepresentante1Conyuge.DocumentoIdentidad!=null)? socioRepresentante1Conyuge.DocumentoIdentidad:"");// Conyugue Rut + Digito Verificador
			formx.setField("RELCONYU_1SEXO", (socioRepresentante1Conyuge.Sexo!=null)? socioRepresentante1Conyuge.Sexo:"");// Conyugue Sexo
			formx.setField("RELCONYU_1TIPO", (socioRepresentante1Conyuge.TipoPersona!=null)? socioRepresentante1Conyuge.TipoPersona:"");// Conyugue Tipo (Juridica, Natural)
			formx.setField("RELCONYU_1RELCORREDORANCG380", (socioRepresentante1Conyuge.IndicadorNCG380!=null)? socioRepresentante1Conyuge.IndicadorNCG380:"");
			formx.setField("RELCONYU_1PERID", (socioRepresentante1Conyuge.IdPersona!=null)? socioRepresentante1Conyuge.IdPersona.toString():"");
			formx.setField("RELCONYU_1MAILCOMERCIAL", (socioRepresentante1Conyuge.DireccionElectronicaComercial!=null)? socioRepresentante1Conyuge.DireccionElectronicaComercial:"");
			formx.setField("RELCONYU_1MAILLABORAL", (socioRepresentante1Conyuge.DireccionElectronicaLaboral!=null)? socioRepresentante1Conyuge.DireccionElectronicaLaboral:"");
			formx.setField("RELCONYU_1MAILPARTICULAR", (socioRepresentante1Conyuge.DireccionElectronicaPrincipal!=null)? socioRepresentante1Conyuge.DireccionElectronicaPrincipal:"");
			formx.setField("RELCONYU_1TELEFONOFIJO", (socioRepresentante1Conyuge.NumeroTelefonoFijo!=null)? socioRepresentante1Conyuge.NumeroTelefonoFijo:"");
			formx.setField("RELCONYU_1TELEFONOMOVIL", (socioRepresentante1Conyuge.NumeroTelefonoMovil!=null)? socioRepresentante1Conyuge.NumeroTelefonoMovil:"");
		}

		// Informacion representante 1 (convivivente civil)
		if ("CC".equalsIgnoreCase(socioRepresentante1.CodigoEstadoCivil)) {
			formx.setField("RELUNIONCIV_1NOMBRECOMPLETO", (socioRepresentante1Conyuge.NombreCompleto!=null)? socioRepresentante1Conyuge.NombreCompleto:"");
			formx.setField("RELUNIONCIV_1NOMBRES", (socioRepresentante1Conyuge.Nombres!=null)? socioRepresentante1Conyuge.Nombres:"");// Conyugue Nombres
			formx.setField("RELUNIONCIV_1APELLIDOMATERNO", (socioRepresentante1Conyuge.ApellidoMaterno!=null)? socioRepresentante1Conyuge.ApellidoMaterno:"");// Conyugue Apellido Materno
			formx.setField("RELUNIONCIV_1APELLIDOPATERNO", (socioRepresentante1Conyuge.ApellidoPaterno!=null)? socioRepresentante1Conyuge.ApellidoPaterno:"");// Conyugue Apellido Paterno
			formx.setField("RELUNIONCIV_1DIRECCIONPARTICULAR",((socioRepresentante1Conyuge.DireccionFisicaCalle!=null)? socioRepresentante1Conyuge.DireccionFisicaCalle:"") + " " + ((socioRepresentante1Conyuge.DireccionFisicaNumero!=null)? socioRepresentante1Conyuge.DireccionFisicaNumero:""));// Conyugue Direccion Particular
			formx.setField("RELUNIONCIV_1DIRECCIONPARTICULARCALLE", (socioRepresentante1Conyuge.DireccionFisicaCalle!=null)? socioRepresentante1Conyuge.DireccionFisicaCalle:"");
			formx.setField("RELUNIONCIV_1DIRECCIONPARTICULARNUMERO", (socioRepresentante1Conyuge.DireccionFisicaNumero!=null)? socioRepresentante1Conyuge.DireccionFisicaNumero:"");
			formx.setField("RELUNIONCIV_1DIRECCIONPARTICULARCIUDAD", (socioRepresentante1Conyuge.DireccionFisicaNombreCiudad!=null)? socioRepresentante1Conyuge.DireccionFisicaNombreCiudad:"");// Conyugue Direccion Particular Ciudad
			formx.setField("RELUNIONCIV_1DIRECCIONPARTICULARCOMUNA", (socioRepresentante1Conyuge.DireccionFisicaNombreComuna!=null)? socioRepresentante1Conyuge.DireccionFisicaNombreComuna:"");// Conyugue Direccion Particular Comuna
			formx.setField("RELUNIONCIV_1DIRECCIONPARTICULARREGION", (socioRepresentante1Conyuge.DireccionFisicaNombreRegion!=null)? socioRepresentante1Conyuge.DireccionFisicaNombreRegion:"");// Conyugue Direccion Particular Region
			formx.setField("RELUNIONCIV_1DIRECCIONPARTICULARPAIS", (socioRepresentante1Conyuge.DireccionFisicaNombrePais!=null)? socioRepresentante1Conyuge.DireccionFisicaNombrePais:"");
			formx.setField("RELUNIONCIV_1DV", (socioRepresentante1Conyuge.DigitoVerificador!=null)? socioRepresentante1Conyuge.DigitoVerificador:"");// Conyugue Digito Verificador
			formx.setField("RELUNIONCIV_1GIRO", (socioRepresentante1Conyuge.NombreGiro!=null)? socioRepresentante1Conyuge.NombreGiro:"");// Conyugue Giro
			formx.setField("RELUNIONCIV_1NUMDOC", (socioRepresentante1Conyuge.DocumentoIdentidad!=null)? socioRepresentante1Conyuge.DocumentoIdentidad:"");// Conyugue Numero Documento (Pasaporte, Rut)
			formx.setField("RELUNIONCIV_1PAIS", (socioRepresentante1Conyuge.NombrePaisNacionalidadPrincipal!=null)? socioRepresentante1Conyuge.NombrePaisNacionalidadPrincipal:"");
			formx.setField("RELUNIONCIV_1RUT", (socioRepresentante1Conyuge.DocumentoIdentidad!=null)? socioRepresentante1Conyuge.DocumentoIdentidad:"");// Conyugue Rut
			formx.setField("RELUNIONCIV_1RUTDV", (socioRepresentante1Conyuge.DocumentoIdentidad!=null)? socioRepresentante1Conyuge.DocumentoIdentidad:"");// Conyugue Rut + Digito Verificador
			formx.setField("RELUNIONCIV_1SEXO", (socioRepresentante1Conyuge.Sexo!=null)? socioRepresentante1Conyuge.Sexo:"");// Conyugue Sexo
			formx.setField("RELUNIONCIV_1TIPO", (socioRepresentante1Conyuge.TipoPersona!=null)? socioRepresentante1Conyuge.TipoPersona:"");// Conyugue Tipo (Juridica, Natural)
			formx.setField("RELUNIONCIV_1RELCORREDORANCG380", (socioRepresentante1Conyuge.IndicadorNCG380!=null)? socioRepresentante1Conyuge.IndicadorNCG380:"");
			formx.setField("RELUNIONCIV_1PERID", (socioRepresentante1Conyuge.IdPersona!=null)? socioRepresentante1Conyuge.IdPersona.toString():"");
			formx.setField("RELUNIONCIV_1MAILCOMERCIAL", (socioRepresentante1Conyuge.DireccionElectronicaComercial!=null)? socioRepresentante1Conyuge.DireccionElectronicaComercial:"");
			formx.setField("RELUNIONCIV_1MAILLABORAL", (socioRepresentante1Conyuge.DireccionElectronicaLaboral!=null)? socioRepresentante1Conyuge.DireccionElectronicaLaboral:"");
			formx.setField("RELUNIONCIV_1MAILPARTICULAR", (socioRepresentante1Conyuge.DireccionElectronicaPrincipal!=null)? socioRepresentante1Conyuge.DireccionElectronicaPrincipal:"");
			formx.setField("RELUNIONCIV_1TELEFONOFIJO", (socioRepresentante1Conyuge.NumeroTelefonoFijo!=null)? socioRepresentante1Conyuge.NumeroTelefonoFijo:"");
			formx.setField("RELUNIONCIV_1TELEFONOMOVIL", (socioRepresentante1Conyuge.NumeroTelefonoMovil!=null)? socioRepresentante1Conyuge.NumeroTelefonoMovil:"");
		}

		// Informacion representante 2 (Conyuge)
		if ("CA".equalsIgnoreCase(socioRepresentante2.CodigoEstadoCivil)) {
			formx.setField("RELCONYU_2NOMBRECOMPLETO", (socioRepresentante2Conyuge.NombreCompleto!=null)? socioRepresentante2Conyuge.NombreCompleto:"");
			formx.setField("RELCONYU_2NOMBRES", (socioRepresentante2Conyuge.Nombres!=null)? socioRepresentante2Conyuge.Nombres:"");// Conyugue Nombres
			formx.setField("RELCONYU_2APELLIDOMATERNO", (socioRepresentante2Conyuge.ApellidoMaterno!=null)? socioRepresentante2Conyuge.ApellidoMaterno:"");// Conyugue Apellido Materno
			formx.setField("RELCONYU_2APELLIDOPATERNO", (socioRepresentante2Conyuge.ApellidoPaterno!=null)? socioRepresentante2Conyuge.ApellidoPaterno:"");// Conyugue Apellido Paterno
			formx.setField("RELCONYU_2DIRECCIONPARTICULAR",((socioRepresentante2Conyuge.DireccionFisicaCalle!=null)? socioRepresentante2Conyuge.DireccionFisicaCalle:"") + " " + ((socioRepresentante2Conyuge.DireccionFisicaNumero!=null)? socioRepresentante2Conyuge.DireccionFisicaNumero:""));// Conyugue Direccion Particular
			formx.setField("RELCONYU_2DIRECCIONPARTICULARCALLE", (socioRepresentante2Conyuge.DireccionFisicaCalle!=null)? socioRepresentante2Conyuge.DireccionFisicaCalle:"");
			formx.setField("RELCONYU_2DIRECCIONPARTICULARNUMERO", (socioRepresentante2Conyuge.DireccionFisicaNumero!=null)? socioRepresentante2Conyuge.DireccionFisicaNumero:"");
			formx.setField("RELCONYU_2DIRECCIONPARTICULARCIUDAD", (socioRepresentante2Conyuge.DireccionFisicaNombreCiudad!=null)? socioRepresentante2Conyuge.DireccionFisicaNombreCiudad:"");// Conyugue Direccion Particular Ciudad
			formx.setField("RELCONYU_2DIRECCIONPARTICULARREGION", (socioRepresentante2Conyuge.DireccionFisicaNombreRegion!=null)? socioRepresentante2Conyuge.DireccionFisicaNombreRegion:"");// Conyugue Direccion Particular Region
			formx.setField("RELCONYU_2DIRECCIONPARTICULARCOMUNA", (socioRepresentante2Conyuge.DireccionFisicaNombreComuna!=null)? socioRepresentante2Conyuge.DireccionFisicaNombreComuna:"");// Conyugue Direccion Particular Comuna
			formx.setField("RELCONYU_2DIRECCIONPARTICULARPAIS", (socioRepresentante2Conyuge.DireccionFisicaNombrePais!=null)? socioRepresentante2Conyuge.DireccionFisicaNombrePais:"");
			formx.setField("RELCONYU_2DV", (socioRepresentante2Conyuge.DigitoVerificador!=null)? socioRepresentante2Conyuge.DigitoVerificador:"");// Conyugue Digito Verificador
			formx.setField("RELCONYU_2GIRO", (socioRepresentante2Conyuge.NombreGiro!=null)? socioRepresentante2Conyuge.NombreGiro:"");// Conyugue Giro
			formx.setField("RELCONYU_2NUMDOC", (socioRepresentante2Conyuge.DocumentoIdentidad!=null)? socioRepresentante2Conyuge.DocumentoIdentidad:"");// Conyugue Numero Documento (Pasaporte, Rut)
			formx.setField("RELCONYU_2PAIS", (socioRepresentante2Conyuge.NombrePaisNacionalidadPrincipal!=null)? socioRepresentante2Conyuge.NombrePaisNacionalidadPrincipal:"");
			formx.setField("RELCONYU_2RUT", (socioRepresentante2Conyuge.DocumentoIdentidad!=null)? socioRepresentante2Conyuge.DocumentoIdentidad:"");// Conyugue Rut
			formx.setField("RELCONYU_2RUTDV", (socioRepresentante2Conyuge.DocumentoIdentidad!=null)? socioRepresentante2Conyuge.DocumentoIdentidad:"");// Conyugue Rut + Digito Verificador
			formx.setField("RELCONYU_2SEXO", (socioRepresentante2Conyuge.Sexo!=null)? socioRepresentante2Conyuge.Sexo:"");// Conyugue Sexo
			formx.setField("RELCONYU_2TIPO", (socioRepresentante2Conyuge.TipoPersona!=null)? socioRepresentante2Conyuge.TipoPersona:"");// Conyugue Tipo (Juridica, Natural)
			formx.setField("RELCONYU_2RELCORREDORANCG380", (socioRepresentante2Conyuge.IndicadorNCG380!=null)? socioRepresentante2Conyuge.IndicadorNCG380:"");
			formx.setField("RELCONYU_2PERID", (socioRepresentante2Conyuge.IdPersona!=null)? socioRepresentante2Conyuge.IdPersona.toString():"");
			formx.setField("RELCONYU_2MAILCOMERCIAL", (socioRepresentante2Conyuge.DireccionElectronicaComercial!=null)? socioRepresentante2Conyuge.DireccionElectronicaComercial:"");
			formx.setField("RELCONYU_2MAILLABORAL", (socioRepresentante2Conyuge.DireccionElectronicaLaboral!=null)? socioRepresentante2Conyuge.DireccionElectronicaLaboral:"");
			formx.setField("RELCONYU_2MAILPARTICULAR", (socioRepresentante2Conyuge.DireccionElectronicaPrincipal!=null)? socioRepresentante2Conyuge.DireccionElectronicaPrincipal:"");
			formx.setField("RELCONYU_2TELEFONOFIJO", (socioRepresentante2Conyuge.NumeroTelefonoFijo!=null)? socioRepresentante2Conyuge.NumeroTelefonoFijo:"");
			formx.setField("RELCONYU_2TELEFONOMOVIL", (socioRepresentante2Conyuge.NumeroTelefonoMovil!=null)? socioRepresentante2Conyuge.NumeroTelefonoMovil:"");
		}

		// Informacion representante 2 (convivivente civil)
		if ("CC".equalsIgnoreCase(socioRepresentante2.CodigoEstadoCivil)) {
			formx.setField("RELUNIONCIV_2NOMBRECOMPLETO", (socioRepresentante2Conyuge.NombreCompleto!=null)? socioRepresentante2Conyuge.NombreCompleto:"");
			formx.setField("RELUNIONCIV_2NOMBRES", (socioRepresentante2Conyuge.Nombres!=null)? socioRepresentante2Conyuge.Nombres:"");// Conyugue Nombres
			formx.setField("RELUNIONCIV_2APELLIDOMATERNO", (socioRepresentante2Conyuge.ApellidoMaterno!=null)? socioRepresentante2Conyuge.ApellidoMaterno:"");// Conyugue Apellido Materno
			formx.setField("RELUNIONCIV_2APELLIDOPATERNO", (socioRepresentante2Conyuge.ApellidoPaterno!=null)? socioRepresentante2Conyuge.ApellidoPaterno:"");// Conyugue Apellido Paterno
			formx.setField("RELUNIONCIV_2DIRECCIONPARTICULAR",((socioRepresentante2Conyuge.DireccionFisicaCalle!=null)? socioRepresentante2Conyuge.DireccionFisicaCalle:"") + " " + ((socioRepresentante2Conyuge.DireccionFisicaNumero!=null)? socioRepresentante2Conyuge.DireccionFisicaNumero:""));// Conyugue Direccion Particular
			formx.setField("RELUNIONCIV_2DIRECCIONPARTICULARCALLE", (socioRepresentante2Conyuge.DireccionFisicaCalle!=null)? socioRepresentante2Conyuge.DireccionFisicaCalle:"");
			formx.setField("RELUNIONCIV_2DIRECCIONPARTICULARNUMERO", (socioRepresentante2Conyuge.DireccionFisicaNumero!=null)? socioRepresentante2Conyuge.DireccionFisicaNumero:"");
			formx.setField("RELUNIONCIV_2DIRECCIONPARTICULARCIUDAD", (socioRepresentante2Conyuge.DireccionFisicaNombreCiudad!=null)? socioRepresentante2Conyuge.DireccionFisicaNombreCiudad:"");// Conyugue Direccion Particular Ciudad
			formx.setField("RELUNIONCIV_2DIRECCIONPARTICULARCOMUNA", (socioRepresentante2Conyuge.DireccionFisicaNombreComuna!=null)? socioRepresentante2Conyuge.DireccionFisicaNombreComuna:"");// Conyugue Direccion Particular Comuna
			formx.setField("RELUNIONCIV_2DIRECCIONPARTICULARREGION", (socioRepresentante2Conyuge.DireccionFisicaNombreRegion!=null)? socioRepresentante2Conyuge.DireccionFisicaNombreRegion:"");// Conyugue Direccion Particular Region
			formx.setField("RELUNIONCIV_2DIRECCIONPARTICULARPAIS", (socioRepresentante2Conyuge.DireccionFisicaNombrePais!=null)? socioRepresentante2Conyuge.DireccionFisicaNombrePais:"");
			formx.setField("RELUNIONCIV_2DV", (socioRepresentante2Conyuge.DigitoVerificador!=null)? socioRepresentante2Conyuge.DigitoVerificador:"");// Conyugue Digito Verificador
			formx.setField("RELUNIONCIV_2GIRO", (socioRepresentante2Conyuge.NombreGiro!=null)? socioRepresentante2Conyuge.NombreGiro:"");// Conyugue Giro
			formx.setField("RELUNIONCIV_2NUMDOC", (socioRepresentante2Conyuge.DocumentoIdentidad!=null)? socioRepresentante2Conyuge.DocumentoIdentidad:"");// Conyugue Numero Documento (Pasaporte, Rut)
			formx.setField("RELUNIONCIV_2PAIS", (socioRepresentante2Conyuge.NombrePaisNacionalidadPrincipal!=null)? socioRepresentante2Conyuge.NombrePaisNacionalidadPrincipal:"");
			formx.setField("RELUNIONCIV_2RUT", (socioRepresentante2Conyuge.DocumentoIdentidad!=null)? socioRepresentante2Conyuge.DocumentoIdentidad:"");// Conyugue Rut
			formx.setField("RELUNIONCIV_2RUTDV", (socioRepresentante2Conyuge.DocumentoIdentidad!=null)? socioRepresentante2Conyuge.DocumentoIdentidad:"");// Conyugue Rut + Digito Verificador
			formx.setField("RELUNIONCIV_2SEXO", (socioRepresentante2Conyuge.Sexo!=null)? socioRepresentante2Conyuge.Sexo:"");// Conyugue Sexo
			formx.setField("RELUNIONCIV_2TIPO", (socioRepresentante2Conyuge.TipoPersona!=null)? socioRepresentante2Conyuge.TipoPersona:"");// Conyugue Tipo (Juridica, Natural)
			formx.setField("RELUNIONCIV_2RELCORREDORANCG380", (socioRepresentante2Conyuge.IndicadorNCG380!=null)? socioRepresentante2Conyuge.IndicadorNCG380:"");
			formx.setField("RELUNIONCIV_2PERID", (socioRepresentante2Conyuge.IdPersona!=null)? socioRepresentante2Conyuge.IdPersona.toString():"");
			formx.setField("RELUNIONCIV_2MAILCOMERCIAL", (socioRepresentante2Conyuge.DireccionElectronicaComercial!=null)? socioRepresentante2Conyuge.DireccionElectronicaComercial:"");
			formx.setField("RELUNIONCIV_2MAILLABORAL", (socioRepresentante2Conyuge.DireccionElectronicaLaboral!=null)? socioRepresentante2Conyuge.DireccionElectronicaLaboral:"");
			formx.setField("RELUNIONCIV_2MAILPARTICULAR", (socioRepresentante2Conyuge.DireccionElectronicaPrincipal!=null)? socioRepresentante2Conyuge.DireccionElectronicaPrincipal:"");
			formx.setField("RELUNIONCIV_2TELEFONOFIJO", (socioRepresentante2Conyuge.NumeroTelefonoFijo!=null)? socioRepresentante2Conyuge.NumeroTelefonoFijo:"");
			formx.setField("RELUNIONCIV_2TELEFONOMOVIL", (socioRepresentante2Conyuge.NumeroTelefonoMovil!=null)? socioRepresentante2Conyuge.NumeroTelefonoMovil:"");
		}

		// Informacion representante 3 (Conyuge)
		if ("CA".equalsIgnoreCase(socioRepresentante3.CodigoEstadoCivil)) {
			formx.setField("RELCONYU_3NOMBRECOMPLETO", (socioRepresentante3Conyuge.NombreCompleto!=null)? socioRepresentante3Conyuge.NombreCompleto:"");
			formx.setField("RELCONYU_3NOMBRES", (socioRepresentante3Conyuge.Nombres!=null)? socioRepresentante3Conyuge.Nombres:"");// Conyugue Nombres
			formx.setField("RELCONYU_3APELLIDOMATERNO", (socioRepresentante3Conyuge.ApellidoMaterno!=null)? socioRepresentante3Conyuge.ApellidoMaterno:"");// Conyugue Apellido Materno
			formx.setField("RELCONYU_3APELLIDOPATERNO", (socioRepresentante3Conyuge.ApellidoPaterno!=null)? socioRepresentante3Conyuge.ApellidoPaterno:"");// Conyugue Apellido Paterno
			formx.setField("RELCONYU_3DIRECCIONPARTICULAR",((socioRepresentante3Conyuge.DireccionFisicaCalle!=null)? socioRepresentante3Conyuge.DireccionFisicaCalle:"") + " " + ((socioRepresentante3Conyuge.DireccionFisicaNumero!=null)? socioRepresentante3Conyuge.DireccionFisicaNumero:""));// Conyugue Direccion Particular
			formx.setField("RELCONYU_3DIRECCIONPARTICULARCALLE", (socioRepresentante3Conyuge.DireccionFisicaCalle!=null)? socioRepresentante3Conyuge.DireccionFisicaCalle:"");
			formx.setField("RELCONYU_3DIRECCIONPARTICULARNUMERO", (socioRepresentante3Conyuge.DireccionFisicaNumero!=null)? socioRepresentante3Conyuge.DireccionFisicaNumero:"");
			formx.setField("RELCONYU_3DIRECCIONPARTICULARCIUDAD", (socioRepresentante3Conyuge.DireccionFisicaNombreCiudad!=null)? socioRepresentante3Conyuge.DireccionFisicaNombreCiudad:"");// Conyugue Direccion Particular Ciudad
			formx.setField("RELCONYU_3DIRECCIONPARTICULARREGION", (socioRepresentante3Conyuge.DireccionFisicaNombreRegion!=null)? socioRepresentante3Conyuge.DireccionFisicaNombreRegion:"");// Conyugue Direccion Particular Region
			formx.setField("RELCONYU_3DIRECCIONPARTICULARCOMUNA", (socioRepresentante3Conyuge.DireccionFisicaNombreComuna!=null)? socioRepresentante3Conyuge.DireccionFisicaNombreComuna:"");// Conyugue Direccion Particular Comuna
			formx.setField("RELCONYU_3DIRECCIONPARTICULARPAIS", (socioRepresentante3Conyuge.DireccionFisicaNombrePais!=null)? socioRepresentante3Conyuge.DireccionFisicaNombrePais:"");
			formx.setField("RELCONYU_3DV", (socioRepresentante3Conyuge.DigitoVerificador!=null)? socioRepresentante3Conyuge.DigitoVerificador:"");// Conyugue Digito Verificador
			formx.setField("RELCONYU_3GIRO", (socioRepresentante3Conyuge.NombreGiro!=null)? socioRepresentante3Conyuge.NombreGiro:"");// Conyugue Giro
			formx.setField("RELCONYU_3NUMDOC", (socioRepresentante3Conyuge.DocumentoIdentidad!=null)? socioRepresentante3Conyuge.DocumentoIdentidad:"");// Conyugue Numero Documento (Pasaporte, Rut)
			formx.setField("RELCONYU_3PAIS", (socioRepresentante3Conyuge.NombrePaisNacionalidadPrincipal!=null)? socioRepresentante3Conyuge.NombrePaisNacionalidadPrincipal:"");
			formx.setField("RELCONYU_3RUT", (socioRepresentante3Conyuge.DocumentoIdentidad!=null)? socioRepresentante3Conyuge.DocumentoIdentidad:"");// Conyugue Rut
			formx.setField("RELCONYU_3RUTDV", (socioRepresentante3Conyuge.DocumentoIdentidad!=null)? socioRepresentante3Conyuge.DocumentoIdentidad:"");// Conyugue Rut + Digito Verificador
			formx.setField("RELCONYU_3SEXO", (socioRepresentante3Conyuge.Sexo!=null)? socioRepresentante3Conyuge.Sexo:"");// Conyugue Sexo
			formx.setField("RELCONYU_3TIPO", (socioRepresentante3Conyuge.TipoPersona!=null)? socioRepresentante3Conyuge.TipoPersona:"");// Conyugue Tipo (Juridica, Natural)
			formx.setField("RELCONYU_3RELCORREDORANCG380", (socioRepresentante3Conyuge.IndicadorNCG380!=null)? socioRepresentante3Conyuge.IndicadorNCG380:"");
			formx.setField("RELCONYU_3PERID", (socioRepresentante3Conyuge.IdPersona!=null)? socioRepresentante3Conyuge.IdPersona.toString():"");
			formx.setField("RELCONYU_3MAILCOMERCIAL", (socioRepresentante3Conyuge.DireccionElectronicaComercial!=null)? socioRepresentante3Conyuge.DireccionElectronicaComercial:"");
			formx.setField("RELCONYU_3MAILLABORAL", (socioRepresentante3Conyuge.DireccionElectronicaLaboral!=null)? socioRepresentante3Conyuge.DireccionElectronicaLaboral:"");
			formx.setField("RELCONYU_3MAILPARTICULAR", (socioRepresentante3Conyuge.DireccionElectronicaPrincipal!=null)? socioRepresentante3Conyuge.DireccionElectronicaPrincipal:"");
			formx.setField("RELCONYU_3TELEFONOFIJO", (socioRepresentante3Conyuge.NumeroTelefonoFijo!=null)? socioRepresentante3Conyuge.NumeroTelefonoFijo:"");
			formx.setField("RELCONYU_3TELEFONOMOVIL", (socioRepresentante3Conyuge.NumeroTelefonoMovil!=null)? socioRepresentante3Conyuge.NumeroTelefonoMovil:"");
		}

		// Informacion representante 3 (convivivente civil)
		if ("CC".equalsIgnoreCase(socioRepresentante3.CodigoEstadoCivil)) {
			formx.setField("RELUNIONCIV_3NOMBRECOMPLETO", (socioRepresentante3Conyuge.NombreCompleto!=null)? socioRepresentante3Conyuge.NombreCompleto:"");
			formx.setField("RELUNIONCIV_3NOMBRES", (socioRepresentante3Conyuge.Nombres!=null)? socioRepresentante3Conyuge.Nombres:"");// Conyugue Nombres
			formx.setField("RELUNIONCIV_3APELLIDOMATERNO", (socioRepresentante3Conyuge.ApellidoMaterno!=null)? socioRepresentante3Conyuge.ApellidoMaterno:"");// Conyugue Apellido Materno
			formx.setField("RELUNIONCIV_3APELLIDOPATERNO", (socioRepresentante3Conyuge.ApellidoPaterno!=null)? socioRepresentante3Conyuge.ApellidoPaterno:"");// Conyugue Apellido Paterno
			formx.setField("RELUNIONCIV_3DIRECCIONPARTICULAR",((socioRepresentante3Conyuge.DireccionFisicaCalle!=null)? socioRepresentante3Conyuge.DireccionFisicaCalle:"") + " " + ((socioRepresentante3Conyuge.DireccionFisicaNumero!=null)? socioRepresentante3Conyuge.DireccionFisicaNumero:""));// Conyugue Direccion Particular
			formx.setField("RELUNIONCIV_3DIRECCIONPARTICULARCALLE", (socioRepresentante3Conyuge.DireccionFisicaCalle!=null)? socioRepresentante3Conyuge.DireccionFisicaCalle:"");
			formx.setField("RELUNIONCIV_3DIRECCIONPARTICULARNUMERO", (socioRepresentante3Conyuge.DireccionFisicaNumero!=null)? socioRepresentante3Conyuge.DireccionFisicaNumero:"");
			formx.setField("RELUNIONCIV_3DIRECCIONPARTICULARCIUDAD", (socioRepresentante3Conyuge.DireccionFisicaNombreCiudad!=null)? socioRepresentante3Conyuge.DireccionFisicaNombreCiudad:"");// Conyugue Direccion Particular Ciudad
			formx.setField("RELUNIONCIV_3DIRECCIONPARTICULARCOMUNA", (socioRepresentante3Conyuge.DireccionFisicaNombreComuna!=null)? socioRepresentante3Conyuge.DireccionFisicaNombreComuna:"");// Conyugue Direccion Particular Comuna
			formx.setField("RELUNIONCIV_3DIRECCIONPARTICULARREGION", (socioRepresentante3Conyuge.DireccionFisicaNombreRegion!=null)? socioRepresentante3Conyuge.DireccionFisicaNombreRegion:"");// Conyugue Direccion Particular Region
			formx.setField("RELUNIONCIV_3DIRECCIONPARTICULARPAIS", (socioRepresentante3Conyuge.DireccionFisicaNombrePais!=null)? socioRepresentante3Conyuge.DireccionFisicaNombrePais:"");
			formx.setField("RELUNIONCIV_3DV", (socioRepresentante3Conyuge.DigitoVerificador!=null)? socioRepresentante3Conyuge.DigitoVerificador:"");// Conyugue Digito Verificador
			formx.setField("RELUNIONCIV_3GIRO", (socioRepresentante3Conyuge.NombreGiro!=null)? socioRepresentante3Conyuge.NombreGiro:"");// Conyugue Giro
			formx.setField("RELUNIONCIV_3NUMDOC", (socioRepresentante3Conyuge.DocumentoIdentidad!=null)? socioRepresentante3Conyuge.DocumentoIdentidad:"");// Conyugue Numero Documento (Pasaporte, Rut)
			formx.setField("RELUNIONCIV_3PAIS", (socioRepresentante3Conyuge.NombrePaisNacionalidadPrincipal!=null)? socioRepresentante3Conyuge.NombrePaisNacionalidadPrincipal:"");
			formx.setField("RELUNIONCIV_3RUT", (socioRepresentante3Conyuge.DocumentoIdentidad!=null)? socioRepresentante3Conyuge.DocumentoIdentidad:"");// Conyugue Rut
			formx.setField("RELUNIONCIV_3RUTDV", (socioRepresentante3Conyuge.DocumentoIdentidad!=null)? socioRepresentante3Conyuge.DocumentoIdentidad:"");// Conyugue Rut + Digito Verificador
			formx.setField("RELUNIONCIV_3SEXO", (socioRepresentante3Conyuge.Sexo!=null)? socioRepresentante3Conyuge.Sexo:"");// Conyugue Sexo
			formx.setField("RELUNIONCIV_3TIPO", (socioRepresentante3Conyuge.TipoPersona!=null)? socioRepresentante3Conyuge.TipoPersona:"");// Conyugue Tipo (Juridica, Natural)
			formx.setField("RELUNIONCIV_3RELCORREDORANCG380", (socioRepresentante3Conyuge.IndicadorNCG380!=null)? socioRepresentante3Conyuge.IndicadorNCG380:"");
			formx.setField("RELUNIONCIV_3PERID", (socioRepresentante3Conyuge.IdPersona!=null)? socioRepresentante3Conyuge.IdPersona.toString():"");
			formx.setField("RELUNIONCIV_3MAILCOMERCIAL", (socioRepresentante3Conyuge.DireccionElectronicaComercial!=null)? socioRepresentante3Conyuge.DireccionElectronicaComercial:"");
			formx.setField("RELUNIONCIV_3MAILLABORAL", (socioRepresentante3Conyuge.DireccionElectronicaLaboral!=null)? socioRepresentante3Conyuge.DireccionElectronicaLaboral:"");
			formx.setField("RELUNIONCIV_3MAILPARTICULAR", (socioRepresentante3Conyuge.DireccionElectronicaPrincipal!=null)? socioRepresentante3Conyuge.DireccionElectronicaPrincipal:"");
			formx.setField("RELUNIONCIV_3TELEFONOFIJO", (socioRepresentante3Conyuge.NumeroTelefonoFijo!=null)? socioRepresentante3Conyuge.NumeroTelefonoFijo:"");
			formx.setField("RELUNIONCIV_3TELEFONOMOVIL", (socioRepresentante3Conyuge.NumeroTelefonoMovil!=null)? socioRepresentante3Conyuge.NumeroTelefonoMovil:"");
		}

		//Informacion relacionado (Cuenta Cliente)
		formx.setField("RELCUENTA_1APELLIDOMATERNO","");// Relacionado Cuenta Apellido Materno
		formx.setField("RELCUENTA_1APELLIDOPATERNO","");// Relacionado Cuenta Apellido Paterno
		formx.setField("RELCUENTA_1DV","");// Relacionado Cuenta Digito Verificador
		formx.setField("RELCUENTA_1NOMBRES","");// Relacionado Cuenta Nombres
		formx.setField("RELCUENTA_1NUMDOC","");// Relacionado Cuenta Numero Documento (Pasaporte, Rut)
		formx.setField("RELCUENTA_1RUT","");// Relacionado Cuenta Rut
		formx.setField("RELCUENTA_1RUTDV","");// Relacionado Cuenta Rut + Digito Verificador
		
		formx.setField("RELCUENTA_2APELLIDOMATERNO","");// Relacionado Cuenta Apellido Materno
		formx.setField("RELCUENTA_2APELLIDOPATERNO","");// Relacionado Cuenta Apellido Paterno
		formx.setField("RELCUENTA_2DV","");// Relacionado Cuenta Digito Verificador
		formx.setField("RELCUENTA_2NOMBRES","");// Relacionado Cuenta Nombres
		formx.setField("RELCUENTA_2NUMDOC","");// Relacionado Cuenta Numero Documento (Pasaporte, Rut)
		formx.setField("RELCUENTA_2RUT","");// Relacionado Cuenta Rut
		formx.setField("RELCUENTA_2RUTDV","");// Relacionado Cuenta Rut + Digito Verificador
		
		formx.setField("RELCUENTA_3APELLIDOMATERNO","");// Relacionado Cuenta Apellido Materno
		formx.setField("RELCUENTA_3APELLIDOPATERNO","");// Relacionado Cuenta Apellido Paterno
		formx.setField("RELCUENTA_3DV","");// Relacionado Cuenta Digito Verificador
		formx.setField("RELCUENTA_3NOMBRES","");// Relacionado Cuenta Nombres
		formx.setField("RELCUENTA_3NUMDOC","");// Relacionado Cuenta Numero Documento (Pasaporte, Rut)
		formx.setField("RELCUENTA_3RUT","");// Relacionado Cuenta Rut
		formx.setField("RELCUENTA_3RUTDV", "");// Relacionado Cuenta Rut + Digito Verificador"

		//Informacion relacionado (Representante Legal)
		formx.setField("RELCORREDORANCG380_MIEM", "Off"); //Off,Yes
		formx.setField("RELCORREDORANCG380_NING", "Off"); //Off,Yes
		formx.setField("RELCORREDORANCG380_TRA", "Off"); //Off,Yes
		formx.setField("RELCORREDORANCG380_DIR", "Off"); //Off,Yes
		formx.setField("RELCORREDORANCG380_SOC", "Off"); //Off,Yes
		formx.setField("RELCORREDORANCG380_OTRA", "Off"); //Off,Yes

		formx.setField("RELLEGAL_1NOMBRES", socioRepresentante1.esPersonaNatural() ? ((socioRepresentante1.Nombres!=null)? socioRepresentante1.Nombres:"") : ((socioRepresentante1.RazonSocial!=null)? socioRepresentante1.RazonSocial:""));
		formx.setField("RELLEGAL_1NOMBRECOMPLETO", (socioRepresentante1.NombreCompleto!=null)? socioRepresentante1.NombreCompleto:"");
		formx.setField("RELLEGAL_1NUMDOC", (socioRepresentante1.DocumentoIdentidad!=null)? socioRepresentante1.DocumentoIdentidad:"");
		formx.setField("RELLEGAL_1PAIS", (socioRepresentante1.NombrePaisNacionalidadPrincipal!=null)? socioRepresentante1.NombrePaisNacionalidadPrincipal:"");
		formx.setField("RELLEGAL_1APELLIDOPATERNO", (socioRepresentante1.ApellidoPaterno!=null)? socioRepresentante1.ApellidoPaterno:"");
		formx.setField("RELLEGAL_1APELLIDOMATERNO", (socioRepresentante1.ApellidoMaterno!=null)? socioRepresentante1.ApellidoMaterno:"");
		formx.setField("RELLEGAL_1DIRECCIONPARTICULAR",((socioRepresentante1.DireccionFisicaCalle!=null)? socioRepresentante1.DireccionFisicaCalle:"") + " " + ((socioRepresentante1.DireccionFisicaNumero!=null)? socioRepresentante1.DireccionFisicaNumero:""));
		formx.setField("RELLEGAL_1DIRECCIONPARTICULARCIUDAD", (socioRepresentante1.DireccionFisicaNombreCiudad!=null)? socioRepresentante1.DireccionFisicaNombreCiudad:"");
		formx.setField("RELLEGAL_1DIRECCIONPARTICULARCOMUNA", (socioRepresentante1.DireccionFisicaNombreComuna!=null)? socioRepresentante1.DireccionFisicaNombreComuna:"");
		formx.setField("RELLEGAL_1TELEFONOFIJO", (socioRepresentante1.NumeroTelefonoFijo!=null)? socioRepresentante1.NumeroTelefonoFijo:"");
		formx.setField("RELLEGAL_1TELEFONOMOVIL", (socioRepresentante1.NumeroTelefonoMovil!=null)? socioRepresentante1.NumeroTelefonoMovil:"");
		formx.setField("RELLEGAL_1DIRECCIONPARTICULARPAIS", (socioRepresentante1.DireccionFisicaNombrePais!=null)? socioRepresentante1.DireccionFisicaNombrePais:"");
		formx.setField("RELLEGAL_1MAILPARTICULAR", (socioRepresentante1.DireccionElectronicaPrincipal!=null)? socioRepresentante1.DireccionElectronicaPrincipal:"");
		formx.setField("RELLEGAL_1MAILCOMERCIAL", (socioRepresentante1.DireccionElectronicaComercial!=null)? socioRepresentante1.DireccionElectronicaComercial:"");
		formx.setField("RELLEGAL_1MAILLABORAL", (socioRepresentante1.DireccionElectronicaLaboral!=null)? socioRepresentante1.DireccionElectronicaLaboral:"");
		formx.setField("RELLEGAL_1CODESTADOCIVIL", (socioRepresentante1.CodigoEstadoCivil!=null)? socioRepresentante1.CodigoEstadoCivil:"");
		formx.setField("RELLEGAL_1ESTADOCIVIL", (socioRepresentante1.EstadoCivil!=null)? socioRepresentante1.EstadoCivil:"");
		formx.setField("RELLEGAL_1CONYUGERUT", (socioRepresentante1Conyuge.DocumentoIdentidad!=null)? socioRepresentante1Conyuge.DocumentoIdentidad:"");
		formx.setField("RELLEGAL_1CONYUGENOMBRE", (socioRepresentante1Conyuge.NombreCompleto!=null)? socioRepresentante1Conyuge.NombreCompleto:"");
		formx.setField("RELLEGAL_1CONYUGENOMBRES", (socioRepresentante1Conyuge.Nombres!=null)? socioRepresentante1Conyuge.Nombres:"");
		formx.setField("RELLEGAL_1CONYUGEAPELLIDOPATERNO", (socioRepresentante1Conyuge.ApellidoPaterno!=null)? socioRepresentante1Conyuge.ApellidoPaterno:"");
		formx.setField("RELLEGAL_1CONYUGEAPELLIDOMATERNO", (socioRepresentante1Conyuge.ApellidoMaterno!=null)? socioRepresentante1Conyuge.ApellidoMaterno:"");	
		formx.setField("RELLEGAL_1CONYUGENOMBREPASINAC", (socioRepresentante1Conyuge.NombrePaisNacimiento!=null)? socioRepresentante1Conyuge.NombrePaisNacimiento:"");//Nacionalidad Conyugue RL 		
		formx.setField("RELLEGAL_1CONYUGENGC380", (socioRepresentante1Conyuge.IndicadorNCG380!=null)? socioRepresentante1Conyuge.IndicadorNCG380:"");//NGC380  Conyugue RL 
		formx.setField("RELLEGAL_1CONYUGETELEFONOFIJO", (socioRepresentante1Conyuge.NumeroTelefonoFijo!=null)? socioRepresentante1Conyuge.NumeroTelefonoFijo:"");
		formx.setField("RELLEGAL_1CONYUGEDIRECCIONPARTICULAR",((socioRepresentante1Conyuge.DireccionFisicaCalle!=null)? socioRepresentante1Conyuge.DireccionFisicaCalle:"") + " " + ((socioRepresentante1Conyuge.DireccionFisicaNumero!=null)? socioRepresentante1Conyuge.DireccionFisicaNumero:""));
		formx.setField("RELLEGAL_1CONYUGEDIRECCIONCOMUNA", (socioRepresentante1Conyuge.DireccionFisicaNombreComuna!=null)? socioRepresentante1Conyuge.DireccionFisicaNombreComuna:"");
		formx.setField("RELLEGAL_1CONYUGEDIRECCIONCIUDAD", (socioRepresentante1Conyuge.DireccionFisicaNombreCiudad!=null)? socioRepresentante1Conyuge.DireccionFisicaNombreCiudad:"");
		formx.setField("RELLEGAL_1CONYUGETELEFONOMOVIL", (socioRepresentante1Conyuge.NumeroTelefonoMovil!=null)? socioRepresentante1Conyuge.NumeroTelefonoMovil:"");
		formx.setField("RELLEGAL_1CONYUGEMAILPARTICULAR", (socioRepresentante1Conyuge.DireccionElectronicaPrincipal!=null)? socioRepresentante1Conyuge.DireccionElectronicaPrincipal:"");
		formx.setField("RELLEGAL_1CONYUGEMAILCOMERCIAL", (socioRepresentante1Conyuge.DireccionElectronicaComercial!=null)? socioRepresentante1Conyuge.DireccionElectronicaComercial:"");
		formx.setField("RELLEGAL_1CONYUGEMAILLABORAL", (socioRepresentante1Conyuge.DireccionElectronicaLaboral!=null)? socioRepresentante1Conyuge.DireccionElectronicaLaboral:"");
		formx.setField("RELLEGAL_1CONCIVILRUT", (socioRepresentante1Conyuge.DocumentoIdentidad!=null)? socioRepresentante1Conyuge.DocumentoIdentidad:"");
		formx.setField("RELLEGAL_1CONCIVILNOMBRE", (socioRepresentante1Conyuge.NombreCompleto!=null)? socioRepresentante1Conyuge.NombreCompleto:"");			
		formx.setField("RELLEGAL_1CONCIVILTELEFONOFIJO", (socioRepresentante1Conyuge.NumeroTelefonoFijo!=null)? socioRepresentante1Conyuge.NumeroTelefonoFijo:"");
		formx.setField("RELLEGAL_1CONCIVILTELEFONOMOVIL", (socioRepresentante1Conyuge.NumeroTelefonoMovil!=null)? socioRepresentante1Conyuge.NumeroTelefonoMovil:"");
		formx.setField("RELLEGAL_1CONCIVILDIRECCIONPARTICULAR",((socioRepresentante1Conyuge.DireccionFisicaCalle!=null)? socioRepresentante1Conyuge.DireccionFisicaCalle:"") + " " + ((socioRepresentante1Conyuge.DireccionFisicaNumero!=null)? socioRepresentante1Conyuge.DireccionFisicaNumero:""));
		formx.setField("RELLEGAL_1CONCIVILMAILPARTICULAR", (socioRepresentante1Conyuge.DireccionElectronicaPrincipal!=null)? socioRepresentante1Conyuge.DireccionElectronicaPrincipal:"");
		formx.setField("RELLEGAL_1CONCIVILMAILCOMERCIAL", (socioRepresentante1Conyuge.DireccionElectronicaComercial!=null)? socioRepresentante1Conyuge.DireccionElectronicaComercial:"");
		formx.setField("RELLEGAL_1CONCIVILMAILLABORAL", (socioRepresentante1Conyuge.DireccionElectronicaLaboral!=null)? socioRepresentante1Conyuge.DireccionElectronicaLaboral:"");
		formx.setField("RELLEGAL_1DIRECCIONPARTICULARREGION", (socioRepresentante1.DireccionFisicaNombreRegion!=null)? socioRepresentante1.DireccionFisicaNombreRegion:"");
		formx.setField("RELLEGAL_1DV", (socioRepresentante1.DigitoVerificador!=null)? socioRepresentante1.DigitoVerificador:"");// Representante Legal Digito Verificador
		formx.setField("RELLEGAL_1GIRO", (socioRepresentante1.NombreGiro!=null)? socioRepresentante1.NombreGiro:"");// Representante Legal Giro
		formx.setField("RELLEGAL_1RUT", (socioRepresentante1.DocumentoIdentidad!=null)? socioRepresentante1.DocumentoIdentidad:"");// Representante Legal Rut
		formx.setField("RELLEGAL_1RUTDV", (socioRepresentante1.DocumentoIdentidad!=null)? socioRepresentante1.DocumentoIdentidad:"");// Representante Legal Rut + Digito Verificador
		formx.setField("RELLEGAL_1SEXO", (socioRepresentante1.Sexo!=null)? socioRepresentante1.Sexo:"");// Representante Legal Sexo
		formx.setField("RELLEGAL_1TIPO", (socioRepresentante1.TipoPersona!=null)? socioRepresentante1.TipoPersona:"");// Representante Legal Tipo (Juridica, Natural)
		formx.setField("RELLEGAL_1RELCORREDORANCG380", (socioRepresentante1.IndicadorNCG380!=null)? socioRepresentante1.IndicadorNCG380:"");
		formx.setField("RELLEGAL_1REGIMENPATRIMONIAL", (socioRepresentante1.NombreRegimenConyugal!=null)? socioRepresentante1.NombreRegimenConyugal:"");//Regimen Patrimonial = Regimen conyugal del representante legal		
		formx.setField("RELLEGAL_1NACIONALIDAD", (socioRepresentante1.NombrePaisNacimiento!=null)? socioRepresentante1.NombrePaisNacimiento:"");
		formx.setField("RELLEGAL_1PERID", (socioRepresentante1.IdPersona!=null)? socioRepresentante1.IdPersona.toString():"");

		formx.setField("RELLEGAL_2NOMBRES", socioRepresentante2.esPersonaNatural() ? ((socioRepresentante2.Nombres!=null)? socioRepresentante2.Nombres:"") : ((socioRepresentante2.RazonSocial!=null)? socioRepresentante2.RazonSocial:""));
		formx.setField("RELLEGAL_2NOMBRECOMPLETO", (socioRepresentante2.NombreCompleto!=null)? socioRepresentante2.NombreCompleto:"");
		formx.setField("RELLEGAL_2NUMDOC", (socioRepresentante2.DocumentoIdentidad!=null)? socioRepresentante2.DocumentoIdentidad:"");
		formx.setField("RELLEGAL_2PAIS", (socioRepresentante2.NombrePaisNacionalidadPrincipal!=null)? socioRepresentante2.NombrePaisNacionalidadPrincipal:"");
		formx.setField("RELLEGAL_2APELLIDOPATERNO", (socioRepresentante2.ApellidoPaterno!=null)? socioRepresentante2.ApellidoPaterno:"");
		formx.setField("RELLEGAL_2APELLIDOMATERNO", (socioRepresentante2.ApellidoMaterno!=null)? socioRepresentante2.ApellidoMaterno:"");
		formx.setField("RELLEGAL_2TELEFONOMOVIL", (socioRepresentante2.NumeroTelefonoMovil!=null)? socioRepresentante2.NumeroTelefonoMovil:"");
		formx.setField("RELLEGAL_2TELEFONOFIJO", (socioRepresentante2.NumeroTelefonoFijo!=null)? socioRepresentante2.NumeroTelefonoFijo:"");
		formx.setField("RELLEGAL_2MAILPARTICULAR", (socioRepresentante2.DireccionElectronicaPrincipal!=null)? socioRepresentante2.DireccionElectronicaPrincipal:"");
		formx.setField("RELLEGAL_2MAILCOMERCIAL", (socioRepresentante2.DireccionElectronicaComercial!=null)? socioRepresentante2.DireccionElectronicaComercial:"");
		formx.setField("RELLEGAL_2MAILLABORAL", (socioRepresentante2.DireccionElectronicaLaboral!=null)? socioRepresentante2.DireccionElectronicaLaboral:"");
		formx.setField("RELLEGAL_2DIRECCIONPARTICULAR",((socioRepresentante2.DireccionFisicaCalle!=null)? socioRepresentante2.DireccionFisicaCalle:"") + " " + ((socioRepresentante2.DireccionFisicaNumero!=null)? socioRepresentante2.DireccionFisicaNumero:""));
		formx.setField("RELLEGAL_2DIRECCIONPARTICULARCIUDAD", (socioRepresentante2.DireccionFisicaNombreCiudad!=null)? socioRepresentante2.DireccionFisicaNombreCiudad:"");
		formx.setField("RELLEGAL_2DIRECCIONPARTICULARCOMUNA", (socioRepresentante2.DireccionFisicaNombreComuna!=null)? socioRepresentante2.DireccionFisicaNombreComuna:"");
		formx.setField("RELLEGAL_2DIRECCIONPARTICULARREGION", (socioRepresentante2.DireccionFisicaNombreRegion!=null)? socioRepresentante2.DireccionFisicaNombreRegion:"");
		formx.setField("RELLEGAL_2CODESTADOCIVIL", (socioRepresentante2.CodigoEstadoCivil!=null)? socioRepresentante2.CodigoEstadoCivil:"");
		formx.setField("RELLEGAL_2ESTADOCIVIL", (socioRepresentante2.EstadoCivil!=null)? socioRepresentante2.EstadoCivil:"");
		formx.setField("RELLEGAL_2CONYUGERUT", (socioRepresentante2Conyuge.DocumentoIdentidad!=null)? socioRepresentante2Conyuge.DocumentoIdentidad:"");
		formx.setField("RELLEGAL_2CONYUGENOMBRE", (socioRepresentante2Conyuge.NombreCompleto!=null)? socioRepresentante2Conyuge.NombreCompleto:"");
		formx.setField("RELLEGAL_2CONYUGENOMBRES", (socioRepresentante2Conyuge.Nombres!=null)? socioRepresentante2Conyuge.Nombres:"");
		formx.setField("RELLEGAL_2CONYUGEAPELLIDOPATERNO", (socioRepresentante2Conyuge.ApellidoPaterno!=null)? socioRepresentante2Conyuge.ApellidoPaterno:"");
		formx.setField("RELLEGAL_2CONYUGEAPELLIDOMATERNO", (socioRepresentante2Conyuge.ApellidoMaterno!=null)? socioRepresentante2Conyuge.ApellidoMaterno:"");	
		formx.setField("RELLEGAL_2CONYUGENOMBREPASINAC", (socioRepresentante2Conyuge.NombrePaisNacimiento!=null)? socioRepresentante2Conyuge.NombrePaisNacimiento:"");//Nacionalidad Conyugue RL 
		formx.setField("RELLEGAL_2CONYUGENGC380", (socioRepresentante2Conyuge.IndicadorNCG380!=null)? socioRepresentante2Conyuge.IndicadorNCG380:"");//NGC380  Conyugue RL 
		formx.setField("RELLEGAL_2CONYUGETELEFONOFIJO", (socioRepresentante2Conyuge.NumeroTelefonoFijo!=null)? socioRepresentante2Conyuge.NumeroTelefonoFijo:"");
		formx.setField("RELLEGAL_2CONYUGEDIRECCIONPARTICULAR",((socioRepresentante2Conyuge.DireccionFisicaCalle!=null)? socioRepresentante2Conyuge.DireccionFisicaCalle:"") + " " + ((socioRepresentante2Conyuge.DireccionFisicaNumero!=null)? socioRepresentante2Conyuge.DireccionFisicaNumero:""));		
		formx.setField("RELLEGAL_2CONYUGEDIRECCIONCOMUNA", (socioRepresentante2Conyuge.DireccionFisicaNombreComuna!=null)? socioRepresentante2Conyuge.DireccionFisicaNombreComuna:"");
		formx.setField("RELLEGAL_2CONYUGEDIRECCIONCIUDAD", (socioRepresentante2Conyuge.DireccionFisicaNombreCiudad!=null)? socioRepresentante2Conyuge.DireccionFisicaNombreCiudad:"");
		formx.setField("RELLEGAL_2CONYUGETELEFONOMOVIL", (socioRepresentante2Conyuge.NumeroTelefonoMovil!=null)? socioRepresentante2Conyuge.NumeroTelefonoMovil:"");
		formx.setField("RELLEGAL_2CONYUGEMAILPARTICULAR", (socioRepresentante2Conyuge.DireccionElectronicaPrincipal!=null)? socioRepresentante2Conyuge.DireccionElectronicaPrincipal:"");
		formx.setField("RELLEGAL_2CONYUGEMAILCOMERCIAL", (socioRepresentante2Conyuge.DireccionElectronicaComercial!=null)? socioRepresentante2Conyuge.DireccionElectronicaComercial:"");
		formx.setField("RELLEGAL_2CONYUGEMAILLABORAL", (socioRepresentante2Conyuge.DireccionElectronicaLaboral!=null)? socioRepresentante2Conyuge.DireccionElectronicaLaboral:"");
		formx.setField("RELLEGAL_2CONCIVILRUT", (socioRepresentante2Conyuge.DocumentoIdentidad!=null)? socioRepresentante2Conyuge.DocumentoIdentidad:"");
		formx.setField("RELLEGAL_2CONCIVILNOMBRE", (socioRepresentante2Conyuge.NombreCompleto!=null)? socioRepresentante2Conyuge.NombreCompleto:"");			
		formx.setField("RELLEGAL_2CONCIVILTELEFONOFIJO", (socioRepresentante2Conyuge.NumeroTelefonoFijo!=null)? socioRepresentante2Conyuge.NumeroTelefonoFijo:"");
		formx.setField("RELLEGAL_2CONCIVILTELEFONOMOVIL", (socioRepresentante2Conyuge.NumeroTelefonoMovil!=null)? socioRepresentante2Conyuge.NumeroTelefonoMovil:"");
		formx.setField("RELLEGAL_2CONCIVILDIRECCIONPARTICULAR",((socioRepresentante2Conyuge.DireccionFisicaCalle!=null)? socioRepresentante2Conyuge.DireccionFisicaCalle:"") + " " + ((socioRepresentante2Conyuge.DireccionFisicaNumero!=null)? socioRepresentante2Conyuge.DireccionFisicaNumero:""));
		formx.setField("RELLEGAL_2CONCIVILMAILPARTICULAR", (socioRepresentante2Conyuge.DireccionElectronicaPrincipal!=null)? socioRepresentante2Conyuge.DireccionElectronicaPrincipal:"");
		formx.setField("RELLEGAL_2CONCIVILMAILCOMERCIAL", (socioRepresentante2Conyuge.DireccionElectronicaComercial!=null)? socioRepresentante2Conyuge.DireccionElectronicaComercial:"");
		formx.setField("RELLEGAL_2CONCIVILMAILLABORAL", (socioRepresentante2Conyuge.DireccionElectronicaLaboral!=null)? socioRepresentante2Conyuge.DireccionElectronicaLaboral:"");
		formx.setField("RELLEGAL_2DV", (socioRepresentante2.DigitoVerificador!=null)? socioRepresentante2.DigitoVerificador:"");// Representante Legal Digito Verificador
		formx.setField("RELLEGAL_2GIRO", (socioRepresentante2.NombreGiro!=null)? socioRepresentante2.NombreGiro:"");// Representante Legal Giro
		formx.setField("RELLEGAL_2RUT", (socioRepresentante2.DocumentoIdentidad!=null)? socioRepresentante2.DocumentoIdentidad:"");// Representante Legal Rut
		formx.setField("RELLEGAL_2RUTDV", (socioRepresentante2.DocumentoIdentidad!=null)? socioRepresentante2.DocumentoIdentidad:"");// Representante Legal Rut + Digito Verificador
		formx.setField("RELLEGAL_2SEXO", (socioRepresentante2.Sexo!=null)? socioRepresentante2.Sexo:"");// Representante Legal Sexo
		formx.setField("RELLEGAL_2TIPO", (socioRepresentante2.TipoPersona!=null)? socioRepresentante2.TipoPersona:"");// Representante Legal Tipo (Juridica, Natural)
		formx.setField("RELLEGAL_2RELCORREDORANCG380", (socioRepresentante2.IndicadorNCG380!=null)? socioRepresentante2.IndicadorNCG380:"");
		formx.setField("RELLEGAL_2REGIMENPATRIMONIAL", (socioRepresentante2.NombreRegimenConyugal!=null)? socioRepresentante2.NombreRegimenConyugal:"");//Regimen Patrimonial = Regimen conyugal del representante legal			
		formx.setField("RELLEGAL_2NACIONALIDAD", (socioRepresentante2.NombrePaisNacimiento!=null)? socioRepresentante2.NombrePaisNacimiento:"");
		formx.setField("RELLEGAL_2PERID", (socioRepresentante2.IdPersona!=null)? socioRepresentante2.IdPersona.toString():"");

		formx.setField("RELLEGAL_3NOMBRES", socioRepresentante3.esPersonaNatural() ? ((socioRepresentante3.Nombres!=null)? socioRepresentante3.Nombres:"") : ((socioRepresentante3.RazonSocial!=null)? socioRepresentante3.RazonSocial:""));
		formx.setField("RELLEGAL_3NOMBRECOMPLETO", (socioRepresentante3.NombreCompleto!=null)? socioRepresentante3.NombreCompleto:"");
		formx.setField("RELLEGAL_3NUMDOC", (socioRepresentante3.DocumentoIdentidad!=null)? socioRepresentante3.DocumentoIdentidad:"");
		formx.setField("RELLEGAL_3PAIS", (socioRepresentante3.NombrePaisNacionalidadPrincipal!=null)? socioRepresentante3.NombrePaisNacionalidadPrincipal:"");
		formx.setField("RELLEGAL_3APELLIDOPATERNO", (socioRepresentante3.ApellidoPaterno!=null)? socioRepresentante3.ApellidoPaterno:"");
		formx.setField("RELLEGAL_3APELLIDOMATERNO", (socioRepresentante3.ApellidoMaterno!=null)? socioRepresentante3.ApellidoMaterno:"");
		formx.setField("RELLEGAL_3DIRECCIONPARTICULAR",((socioRepresentante3.DireccionFisicaCalle!=null)? socioRepresentante3.DireccionFisicaCalle:"") + " " + ((socioRepresentante3.DireccionFisicaNumero!=null)? socioRepresentante3.DireccionFisicaNumero:""));
		formx.setField("RELLEGAL_3DIRECCIONPARTICULARCIUDAD", (socioRepresentante3.DireccionFisicaNombreCiudad!=null)? socioRepresentante3.DireccionFisicaNombreCiudad:"");
		formx.setField("RELLEGAL_3DIRECCIONPARTICULARCOMUNA", (socioRepresentante3.DireccionFisicaNombreComuna!=null)? socioRepresentante3.DireccionFisicaNombreComuna:"");
		formx.setField("RELLEGAL_3DIRECCIONPARTICULARREGION", (socioRepresentante3.DireccionFisicaNombreRegion!=null)? socioRepresentante3.DireccionFisicaNombreRegion:"");
		formx.setField("RELLEGAL_3TELEFONOMOVIL", (socioRepresentante3.NumeroTelefonoMovil!=null)? socioRepresentante3.NumeroTelefonoMovil:"");
		formx.setField("RELLEGAL_3TELEFONOFIJO", (socioRepresentante3.NumeroTelefonoFijo!=null)? socioRepresentante3.NumeroTelefonoFijo:"");
		formx.setField("RELLEGAL_3DIRECCIONPARTICULARPAIS", (socioRepresentante3.DireccionFisicaNombrePais!=null)? socioRepresentante3.DireccionFisicaNombrePais:"");
		formx.setField("RELLEGAL_3MAILPARTICULAR", (socioRepresentante3.DireccionElectronicaPrincipal!=null)? socioRepresentante3.DireccionElectronicaPrincipal:"");
		formx.setField("RELLEGAL_3MAILCOMERCIAL", (socioRepresentante3.DireccionElectronicaComercial!=null)? socioRepresentante3.DireccionElectronicaComercial:"");
		formx.setField("RELLEGAL_3MAILLABORAL", (socioRepresentante3.DireccionElectronicaLaboral!=null)? socioRepresentante3.DireccionElectronicaLaboral:"");
		formx.setField("RELLEGAL_3CODESTADOCIVIL", (socioRepresentante3.CodigoEstadoCivil!=null)? socioRepresentante3.CodigoEstadoCivil:"");
		formx.setField("RELLEGAL_3ESTADOCIVIL", (socioRepresentante3.EstadoCivil!=null)? socioRepresentante3.EstadoCivil:"");
		formx.setField("RELLEGAL_3CONYUGERUT", (socioRepresentante3Conyuge.DocumentoIdentidad!=null)? socioRepresentante3Conyuge.DocumentoIdentidad:"");
		formx.setField("RELLEGAL_3CONYUGENOMBRE", (socioRepresentante3Conyuge.NombreCompleto!=null)? socioRepresentante3Conyuge.NombreCompleto:"");
		formx.setField("RELLEGAL_3CONYUGENOMBRES", (socioRepresentante3Conyuge.Nombres!=null)? socioRepresentante3Conyuge.Nombres:"");
		formx.setField("RELLEGAL_3CONYUGEAPELLIDOPATERNO", (socioRepresentante3Conyuge.ApellidoPaterno!=null)? socioRepresentante3Conyuge.ApellidoPaterno:"");
		formx.setField("RELLEGAL_3CONYUGEAPELLIDOMATERNO", (socioRepresentante3Conyuge.ApellidoMaterno!=null)? socioRepresentante3Conyuge.ApellidoMaterno:"");	
		formx.setField("RELLEGAL_3CONYUGENOMBREPASINAC", (socioRepresentante3Conyuge.NombrePaisNacimiento!=null)? socioRepresentante3Conyuge.NombrePaisNacimiento:"");//Nacionalidad Conyugue RL 
		formx.setField("RELLEGAL_3CONYUGENGC380", (socioRepresentante3Conyuge.IndicadorNCG380!=null)? socioRepresentante3Conyuge.IndicadorNCG380:"");//NGC380  Conyugue RL 
		formx.setField("RELLEGAL_3CONYUGETELEFONOFIJO", (socioRepresentante3Conyuge.NumeroTelefonoFijo!=null)? socioRepresentante3Conyuge.NumeroTelefonoFijo:"");
		formx.setField("RELLEGAL_3CONYUGEDIRECCIONPARTICULAR",((socioRepresentante3Conyuge.DireccionFisicaCalle!=null)? socioRepresentante3Conyuge.DireccionFisicaCalle:"") + " " + ((socioRepresentante3Conyuge.DireccionFisicaNumero!=null)? socioRepresentante3Conyuge.DireccionFisicaNumero:""));
		formx.setField("RELLEGAL_3CONYUGEDIRECCIONCOMUNA", (socioRepresentante3Conyuge.DireccionFisicaNombreComuna!=null)? socioRepresentante3Conyuge.DireccionFisicaNombreComuna:"");
		formx.setField("RELLEGAL_3CONYUGEDIRECCIONCIUDAD", (socioRepresentante3Conyuge.DireccionFisicaNombreCiudad!=null)? socioRepresentante3Conyuge.DireccionFisicaNombreCiudad:"");
		formx.setField("RELLEGAL_3CONYUGETELEFONOMOVIL", (socioRepresentante3Conyuge.NumeroTelefonoMovil!=null)? socioRepresentante3Conyuge.NumeroTelefonoMovil:"");
		formx.setField("RELLEGAL_3CONYUGEMAILPARTICULAR", (socioRepresentante3Conyuge.DireccionElectronicaPrincipal!=null)? socioRepresentante3Conyuge.DireccionElectronicaPrincipal:"");
		formx.setField("RELLEGAL_3CONYUGEMAILCOMERCIAL", (socioRepresentante3Conyuge.DireccionElectronicaComercial!=null)? socioRepresentante3Conyuge.DireccionElectronicaComercial:"");
		formx.setField("RELLEGAL_3CONYUGEMAILLABORAL", (socioRepresentante3Conyuge.DireccionElectronicaLaboral!=null)? socioRepresentante3Conyuge.DireccionElectronicaLaboral:"");
		formx.setField("RELLEGAL_3CONCIVILRUT", (socioRepresentante3Conyuge.DocumentoIdentidad!=null)? socioRepresentante3Conyuge.DocumentoIdentidad:"");
		formx.setField("RELLEGAL_3CONCIVILNOMBRE", (socioRepresentante3Conyuge.NombreCompleto!=null)? socioRepresentante3Conyuge.NombreCompleto:"");			
		formx.setField("RELLEGAL_3CONCIVILTELEFONOFIJO", (socioRepresentante3Conyuge.NumeroTelefonoFijo!=null)? socioRepresentante3Conyuge.NumeroTelefonoFijo:"");
		formx.setField("RELLEGAL_3CONCIVILTELEFONOMOVIL", (socioRepresentante3Conyuge.NumeroTelefonoMovil!=null)? socioRepresentante3Conyuge.NumeroTelefonoMovil:"");
		formx.setField("RELLEGAL_3CONCIVILDIRECCIONPARTICULAR",((socioRepresentante3Conyuge.DireccionFisicaCalle!=null)? socioRepresentante3Conyuge.DireccionFisicaCalle:"") + " " + ((socioRepresentante3Conyuge.DireccionFisicaNumero!=null)? socioRepresentante3Conyuge.DireccionFisicaNumero:""));
		formx.setField("RELLEGAL_3CONCIVILMAILPARTICULAR", (socioRepresentante3Conyuge.DireccionElectronicaPrincipal!=null)? socioRepresentante3Conyuge.DireccionElectronicaPrincipal:"");
		formx.setField("RELLEGAL_3CONCIVILMAILCOMERCIAL", (socioRepresentante3Conyuge.DireccionElectronicaComercial!=null)? socioRepresentante3Conyuge.DireccionElectronicaComercial:"");
		formx.setField("RELLEGAL_3CONCIVILMAILLABORAL", (socioRepresentante3Conyuge.DireccionElectronicaLaboral!=null)? socioRepresentante3Conyuge.DireccionElectronicaLaboral:"");
		formx.setField("RELLEGAL_3DV", (socioRepresentante3.DigitoVerificador!=null)? socioRepresentante3.DigitoVerificador:"");// Representante Legal Digito Verificador
		formx.setField("RELLEGAL_3GIRO", (socioRepresentante3.NombreGiro!=null)? socioRepresentante3.NombreGiro:"");// Representante Legal Giro
		formx.setField("RELLEGAL_3RUT", (socioRepresentante3.DocumentoIdentidad!=null)? socioRepresentante3.DocumentoIdentidad:"");// Representante Legal Rut
		formx.setField("RELLEGAL_3RUTDV", (socioRepresentante3.DocumentoIdentidad!=null)? socioRepresentante3.DocumentoIdentidad:"");// Representante Legal Rut + Digito Verificador
		formx.setField("RELLEGAL_3SEXO", (socioRepresentante3.Sexo!=null)? socioRepresentante3.Sexo:"");// Representante Legal Sexo
		formx.setField("RELLEGAL_3TIPO", (socioRepresentante3.TipoPersona!=null)? socioRepresentante3.TipoPersona:"");// Representante Legal Tipo (Juridica, Natural)
		formx.setField("RELLEGAL_3RELCORREDORANCG380", (socioRepresentante3.IndicadorNCG380!=null)? socioRepresentante3.IndicadorNCG380:"");
		formx.setField("RELLEGAL_3REGIMENPATRIMONIAL", (socioRepresentante3.NombreRegimenConyugal!=null)? socioRepresentante3.NombreRegimenConyugal:"");//Regimen Patrimonial = Regimen conyugal del representante legal		
		formx.setField("RELLEGAL_3NACIONALIDAD", (socioRepresentante3.NombrePaisNacimiento!=null)? socioRepresentante3.NombrePaisNacimiento:"");
		formx.setField("RELLEGAL_3PERID", (socioRepresentante3.IdPersona!=null)? socioRepresentante3.IdPersona.toString():"");
		
		//Informacion Telefonos
		formx.setField("TELEFONOCOMERCIALCUENTA","");// Telefono Comercial Cuenta
		formx.setField("TELEFONOCORRESPONDENCIACUENTA","");// Telefono Correspondencia Cuenta
		formx.setField("TELEFONOFAXCLIENTE","");// Telefono Fax Cliente
		formx.setField("TELEFONOFIJOCLIENTE",(socioPersona.NumeroTelefonoFijo!=null)? socioPersona.NumeroTelefonoFijo:"");// Telefono Fijo Cliente
		formx.setField("TELEFONOLEGALCUENTA","");// Telefono Legal Cuenta
		formx.setField("TELEFONOMOVILCLIENTE",(socioPersona.NumeroTelefonoMovil!=null)? socioPersona.NumeroTelefonoMovil:"");// Telefono Movil Cliente
		formx.setField("TELEFONOPARTICULARCUENTA","");// Telefono Particular Cuenta
		formx.setField("TELEFONORECADOSCLIENTE","");// Telefono Recados Cliente		
		
		if(!paisesDeResidenciaFatca.isEmpty()) {
			int contador = 1;
			for (DatosFatcaPaisResidencia datosFatcaPaisResidencia : paisesDeResidenciaFatca) {
				formx.setField("FATCAPAISRESIDENCIA_PAISRESIDENCIA" + String.valueOf(contador), datosFatcaPaisResidencia.PaisResidencia); // Nombre Pais de Residencia
				formx.setField("FATCAPAISRESIDENCIA_TIN" + String.valueOf(contador), datosFatcaPaisResidencia.TIN); // TIN
				formx.setField("FATCAPAISRESIDENCIA_CAUSANOTIN" + String.valueOf(contador) ,datosFatcaPaisResidencia.CausaNoTin); // Causa de no tener TIN
				contador++;
			}	
		}
		
		for (int i = 0; i < 4; i++) {
		    int num = i + 1;
		    
		    DatosApoderadoFirmante firmante = (i < datosApoderadoFirmante.size()) ? datosApoderadoFirmante.get(i) : null;
		    DatosConyugeApoderadoFirmante conyuge = (firmante != null && firmante.IdPersonaConyuge != null) 
		    ? datosConyugeApoderadoFirmante.stream().filter(c -> c.IdPersona.equals(firmante.IdPersonaConyuge)).findFirst().orElse(null) : null;

		    //Dato Apoderado Firmante
		    formx.setField("REAPODE_FIRMANTE_" + num + "_NOMBRE_COMPLETO", (firmante != null && firmante.NombreCompleto != null) ? firmante.NombreCompleto : "");
		    formx.setField("REAPODE_FIRMANTE_" + num + "_TIPO_DOC", (firmante != null && firmante.TipoDocumento != null) ? firmante.TipoDocumento : "");
		    formx.setField("REAPODE_FIRMANTE_" + num + "_NUMDOC", (firmante != null && firmante.DocumentoIdentidad != null) ? firmante.DocumentoIdentidad : "");
		    formx.setField("REAPODE_FIRMANTE_" + num + "_NACIONALIDAD_PRINCIPAL", (firmante != null && firmante.NombrePaisNacionalidadPrincipal != null) ? firmante.NombrePaisNacionalidadPrincipal : "");
		    formx.setField("REAPODE_FIRMANTE_" + num + "_MAIL", (firmante != null && firmante.DireccionElectronicaPrincipal != null) ? firmante.DireccionElectronicaPrincipal : "");
		    formx.setField("REAPODE_FIRMANTE_" + num + "_TELEFONO_PRINCIPAL", (firmante != null && firmante.NumeroTelefonoPrincipal != null) ? firmante.NumeroTelefonoPrincipal : "");
		    formx.setField("REAPODE_FIRMANTE_" + num + "_RELACION_CORREDORA", (firmante != null && firmante.RelacionCorredora != null) ? firmante.RelacionCorredora : "");
		    formx.setField("REAPODE_FIRMANTE_" + num + "_CARGO", (firmante != null && firmante.Cargo != null) ? firmante.Cargo : "");
		    formx.setField("REAPODE_FIRMANTE_" + num + "NOMBRES", (firmante != null && firmante.Nombres != null) ? firmante.Nombres : "");
		    formx.setField("REAPODE_FIRMANTE_" + num + "PAIS", (firmante != null && firmante.NombrePaisNacionalidadPrincipal != null) ? firmante.NombrePaisNacionalidadPrincipal : "");
		    formx.setField("REAPODE_FIRMANTE_" + num + "APELLIDOPATERNO", (firmante != null && firmante.ApellidoPaterno != null) ? firmante.ApellidoPaterno : "");
		    formx.setField("REAPODE_FIRMANTE_" + num + "APELLIDOMATERNO", (firmante != null && firmante.ApellidoMaterno != null) ? firmante.ApellidoMaterno : "");
		    formx.setField("REAPODE_FIRMANTE_" + num + "DIRECCIONPARTICULAR", (firmante != null && firmante.DireccionParticular != null) ? firmante.DireccionParticular : "");
		    formx.setField("REAPODE_FIRMANTE_" + num + "DIRECCIONPARTICULARCIUDAD", (firmante != null && firmante.DireccionParticularCiudad != null) ? firmante.DireccionParticularCiudad : "");
		    formx.setField("REAPODE_FIRMANTE_" + num + "DIRECCIONPARTICULARCOMUNA", (firmante != null && firmante.DireccionParticularComuna != null) ? firmante.DireccionParticularComuna : "");
		    formx.setField("REAPODE_FIRMANTE_" + num + "ESTADOCIVIL", (firmante != null && firmante.EstadoCivil != null) ? firmante.EstadoCivil : "");

		  //Dato conyuge Apoderado Firmante
		    formx.setField("REAPODE_FIRMANTE_" + num + "CONYUGERUT", (conyuge != null && conyuge.DocumentoIdentidad != null) ? conyuge.DocumentoIdentidad : "");
		    formx.setField("REAPODE_FIRMANTE_" + num + "CONYUGENOMBRE", (conyuge != null && conyuge.NombreCompleto != null) ? conyuge.NombreCompleto : "");
		    formx.setField("REAPODE_FIRMANTE_" + num + "CONYUGENOMBRES", (conyuge != null && conyuge.Nombres != null) ? conyuge.Nombres : "");
		    formx.setField("REAPODE_FIRMANTE_" + num + "CONYUGEAPELLIDOPATERNO", (conyuge != null && conyuge.ApellidoPaterno != null) ? conyuge.ApellidoPaterno : "");
		    formx.setField("REAPODE_FIRMANTE_" + num + "CONYUGEAPELLIDOMATERNO", (conyuge != null && conyuge.ApellidoMaterno != null) ? conyuge.ApellidoMaterno : "");
		    formx.setField("REAPODE_FIRMANTE_" + num + "CONYUGENOMBREPASINAC", (conyuge != null && conyuge.NombrePaisNacionalidadPrincipal != null) ? conyuge.NombrePaisNacionalidadPrincipal : "");
		    formx.setField("REAPODE_FIRMANTE_" + num + "CONYUGENGC380", (conyuge != null && conyuge.RelacionCorredora != null) ? conyuge.RelacionCorredora : "");
		}

		//para imprimir nombres de campos del pdf
		/*for (Object key : fields.keySet()) {
			Object val = fields.get(key);
			System.out.println(key);
		}
		*/

		stamper.close();
		//reader.close();
		byte[] pdfBytes = baos.toByteArray();
		salida.p_out_ContratoB64 = new String(Base64.encodeBase64(pdfBytes));
		salida.p_out_NombreContrato = contrato.get(0).Descripcion + " - " + socioPersona.DocumentoIdentidad + ".pdf";
		//FileUtils.writeByteArrayToFile(new File(System.getenv("USERPROFILE") + "\\Desktop\\" +salida.p_out_NombreContrato), pdfBytes);

		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ImprimirContrato {}", finServicio - inicioServicio);

		return salida;
	}
	
	private String formatearDecimales(BigDecimal valorCobro, String formaCobro) {
		DecimalFormat formatoDecimales = new DecimalFormat("0.########"); 
		String valorFormateado = formatoDecimales.format(valorCobro);
		
		if (formaCobro != null && "V".equalsIgnoreCase(formaCobro)) {
			valorFormateado += '%';
		}
		
		return valorFormateado;
	}
	
	private String obtenerTextoValorMultiple(Map<String, Boolean> estadoSeleccionPorCodigoValor, Map<String, String> labelValorPorCodigoValor, String delimiter) {
		if (estadoSeleccionPorCodigoValor == null || estadoSeleccionPorCodigoValor.isEmpty()) {
			return "";
		}

		List<String> seleccionados = new ArrayList<>(estadoSeleccionPorCodigoValor.size());
		estadoSeleccionPorCodigoValor.forEach((codigoValor, seleccionado) -> {
			if (seleccionado) {
				seleccionados.add(labelValorPorCodigoValor.getOrDefault(codigoValor, codigoValor));
			}
		});

		return String.join(delimiter, seleccionados);
	}

	public static void main(String[] args) {
		System.out.println("== TEST ==");

		// Ejemplo persona natural - tags BE PN 380
		String json = "{\"p_IdEmpresa\":1,\"p_CodigoContrato\":\"SIMULPJ\",\"p_IdUsuario\":347,\"p_IdModulo\":\"PLA\",\"p_DocumentoIdentidad\":\"16369258-9\"}";
				ImprimirContrato_Entrada entrada = Utils.getGsonBuilder().fromJson(json, ImprimirContrato_Entrada.class);
		System.out.println(entrada);

		try (Connection conn = PlataformaUtils.getDESAConnection()) {
			try {
				ImprimirContrato_Salida salida = new ImprimirContrato().logica(conn, entrada);
				String b64 = salida.p_out_ContratoB64;
				salida.p_out_ContratoB64 = "ContenidoEnBase64"; // Mucho texto
				System.out.println(salida);
				if (salida.P_OUT_ERRORESCONTADOR > 0L) {
					conn.rollback();
				}

				String printedContractsDirPathString = System.getProperty("user.home") + "/Desktop/ImpresionesContratos";
				Path printedContractsDirPath = Paths.get(printedContractsDirPathString);
				if (Files.exists(printedContractsDirPath) == false) {
					Files.createDirectory(printedContractsDirPath);
				}

				String baseContractPath = printedContractsDirPathString + "/contrato-";
				long correlative = 0L;
				Path finalContractPath;
				do {
					finalContractPath = Paths.get(baseContractPath + correlative++ + ".pdf");
				} while (finalContractPath.toFile().exists());
				Files.write(finalContractPath, Base64.decodeBase64(b64));
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				conn.rollback();
			}
		} catch (SQLException throwables) {
			throwables.printStackTrace();
		}
		System.out.println("== END ==");
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		ImprimirContrato_Entrada jin = gson.fromJson(in.toString(), ImprimirContrato_Entrada.class);
		ImprimirContrato_Salida out = logica(conn, jin);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

}
