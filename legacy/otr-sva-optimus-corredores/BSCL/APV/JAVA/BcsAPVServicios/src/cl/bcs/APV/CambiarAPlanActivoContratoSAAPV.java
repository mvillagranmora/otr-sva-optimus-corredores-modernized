package cl.bcs.APV;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.APV.GrabarContratoSAAPV.ParametrosContrato;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.portafolio.ModificarPortafolio;

public class CambiarAPlanActivoContratoSAAPV implements JavaCall {

	private static final Logger logger = LogManager.getLogger(CambiarAPlanActivoContratoSAAPV.class);

	private final String SQL_SELECT_PARAMETROS_CONTRATO = loadResourceToString("cl/bcs/APV/sql/CambiarAPlanActivoContratoSAAPV_SelectParametrosContrato.sql");
	private final String SQL_SELECT_DISTRIBUCION_INVERSION = loadResourceToString("cl/bcs/APV/sql/CambiarAPlanActivoContratoSAAPV_SelectDistribucionInversion.sql");
	private final String SQL_SELECT_PORTAFOLIOS_MODIFICADOS = loadResourceToString("cl/bcs/APV/sql/CambiarAPlanActivoContratoSAAPV_SelectPortafoliosModificados.sql");
	private final String SQL_SELECT_DETALLE_ORIGEN_TRASPASO = loadResourceToString("cl/bcs/APV/sql/CambiarAPlanActivoContratoSAAPV_SelectDetalleOrigenTraspaso.sql");
	private final String SQL_INSERTAR_GLOBAL_PORTAFOLIOS = loadResourceToString("cl/bcs/APV/sql/CambiarAPlanActivoContratoSAAPV_InsertGlobalPortafolios.sql");
	private final String SQL_MODIFICAR_PLAN_PORTAFOLIOS = loadResourceToString("cl/bcs/APV/sql/CambiarAPlanActivoContratoSAAPV_UpdatePlanPortafolios.sql");

	public static class ContratoCambioPlanIn {
		public Long IdPersona;
		public Long IdCliente;
		public Long IdPortafolio;
		public Long IdContratoSAAPV;
		public String FolioContratoSAAPV;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class CambiarAPlanActivoEntrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public List<ContratoCambioPlanIn> p_ContratoCambioPlanIn;

		public CambiarAPlanActivoEntrada() {
			this.p_ContratoCambioPlanIn = new ArrayList<ContratoCambioPlanIn>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	};

	public static class CambiarAPlanActivoSalida {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;

		public CambiarAPlanActivoSalida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	};

	public static class IdentificadorContratoSAAPV {
		public Long IdContratoSAAPV;
		public String FolioContratoSAAPV;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class DatosContratoSAAPV {
		public Long IdContratoSAAPV;
		public Long IdEmpresa;
		public String FolioContratoSAAPV;
		public Long IdPlan;
		public String NombrePlan;
		public java.sql.Date FechaSuscripcion;
		public java.sql.Date FechaCreacion;
		public java.sql.Date FechaAprobacion;
		public java.sql.Date FechaNotificacion;
		public java.sql.Date FechaRecepcion;
		public String Folio;
		public Long IdPersona;
		public Long IdEmpleador;
		public Long IdCliente;
		public String IdCategoriaInversionista;///
		public String IdPerfilComercial;///
		public String IdPerfilComercialAPV;
		public String NombreCliente;
		public String ApellidoPaternoCliente;
		public String ApellidoMaternoCliente;
		public String DocumentoIdentidadCliente;
		public java.sql.Date FechaNacimientoCliente;
		public String EstadoCivil;
		public String IdTipoGenero;
		public Long IdPaisResidenciaCliente;
		public Long IdCiudadCliente;
		public Long IdComunaCliente;
		public String DireccionCliente;
		public String NumeroDireccionCliente;///
		public Long IdTipoCargoPersona;
		public String TelefonoCliente;
		public String TelefonoComercialCliente;
		public String TelefonoCelularCliente;
		public String EmailCliente;
		public String RazonSocialEmpleador;
		public Long IdMedioRecaudacionTrabajador;
		public Long IdEntidadRecaudadora;
		public String DocumentoIdentidadEmpleador;
		public Long IdPaisEmpleador;///
		public String DireccionComercialEmpleador;
		public String NumeroDireccionEmpleador;///
		public Long IdCiudadEmpleador;///
		public Long IdComunaEmpleador;
		public String TelefonoEmpleador;
		public String NombreEncargado;
		public String EmailEncargado;
		public String TelefonoEncargado;
		public Long CodigoPrevision;
		public String Pensionado;
		public String Trabajador;
		public String FormaDePago;
		public Long IdMedioRecaudacionFormaPago;
		public Long DiaDescuento;
		public String Institucion;
		public BigDecimal ComisionTransferencia;
		public String CodigoTipoSolicitud;
		public String TipoMovimiento;///
		public String RazonRetiro;///
		public String NombreEntidadOrigen;
		public String RutEntidadOrigen;
		public Long IdPersonaOrigen;///
		public String Descripcion;///
		public String TipoDeAhorro;
		public String FondoDeOrigen;
		public String TipoTraspaso;
		public BigDecimal MontoTraspaso;
		public BigDecimal UFTraspaso;
		public BigDecimal PorcentajeTraspaso;
		public String IdEstadoEnvioNotificacion;
		public String IdEstadoSolicitud;
		public String IdEstado;
		public Long IdCargaMasiva;
		public Long IdEmpleado;///
		public Long IdInterno;///

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class DatosDistribucionInversion {
		public Long IdContratoSAAPV;
		public String FolioContratoSAAPV;
		public Long IdInstrumento;
		public String Nemotecnico;
		public BigDecimal MontoPesos;
		public BigDecimal Porcentaje;
		public String IdEstado;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class DatosPortafoliosModificados {
		public Long IdContratoSAAPV;
		public String CodigoAgrupador;
		public Long IdMatrizAgrupadorAPV;
		public String OrigenAhorro;
		public Long IdPortafolio;
		public String IdPropositoPortafolio;
		public String PeriodoInicio;
		public String PeriodoTermino;
		public BigDecimal MontoPesos;
		public BigDecimal MontoUF;
		public String Pago;
		public BigDecimal PorcentajeRegimenImponible;
		public String NombreProductoAgrupador;
		public String Codigo;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class DatosDetalleOrigenTraspaso {
		public Long IdContratoSAAPV;
		public Long IdDetalleOrigenTraspaso;
		public Long IdTraspaso;
		public String CodigoTipoAhorro;
		public Long IdEntidadRecaudadora;
		public String NombreEntidad;
		public String DocumentoEntidad;
		public String Fondo;
		public BigDecimal MontoTraspaso;
		public BigDecimal UFTraspaso;
		public BigDecimal PorcentajeTraspaso;
		public String IdEstado;
		public Long IdInstrumento;
		public String CodigoRegimenTributario;
		public Long IdPortafolio;
		public Long IdInterno;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class RegistroCambio {
		public String CampoModificado;
		public String ValorNuevo;

		public RegistroCambio(String campoModificado, String valorNuevo) {
			this.CampoModificado = campoModificado;
			this.ValorNuevo = valorNuevo;
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Portafolio{
		public Long IdPortafolio;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();
		CambiarAPlanActivoEntrada jin = gson.fromJson(in.toString(), CambiarAPlanActivoEntrada.class);
		CambiarAPlanActivoSalida out = logica(conn, jin);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};

			
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public CambiarAPlanActivoSalida logica(Connection con, CambiarAPlanActivoEntrada in) throws Exception {
		Long init = System.currentTimeMillis();
		logger.info("Inicio servicio CambiarAPlanActivoContratoSAAPV");

		SQL sql = new SQL();
		CambiarAPlanActivoSalida out = new CambiarAPlanActivoSalida();
		out.P_OUT_ERRORESCONTADOR = 0L;
		out.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ADVERTENCIA,"ACTUALIZAR_MODELOS_COMERCIALES",new HashMap<String, String>(),con,in.p_IdEmpresa));
		try {
			// Validacion entrada al servicio
			if (in.p_ContratoCambioPlanIn.size() == 0) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_CONTRATOS","NO_SE_INGRESARON_CONTRATOS"));
				return out;	
			}
			
			// Validacion FolioModificado ya existe en otro Contrato SAAPV
			HashMap<String, Object> params = new HashMap<String, Object>();

			// Filtra IdContratoSAAPV repetidos en la entrada
			List<Long> idsContrato = new ArrayList<Long>();
			List<Portafolio> lstPortafolios = new ArrayList<Portafolio>();
			
			HashMap<String, ContratoCambioPlanIn> contratoHash = new HashMap<String, ContratoCambioPlanIn>();
			for (ContratoCambioPlanIn contratoIn : in.p_ContratoCambioPlanIn) {
				String keyContrato = "";
				
				Portafolio portafolio = new Portafolio();
				portafolio.IdPortafolio = contratoIn.IdPortafolio;
				lstPortafolios.add(portafolio);

				keyContrato = in.p_IdEmpresa + "_" + contratoIn.IdContratoSAAPV;

				if (!contratoHash.containsKey(keyContrato)) {
					contratoHash.put(keyContrato, contratoIn);
					idsContrato.add(contratoIn.IdContratoSAAPV);
				}
			}
			
			if(idsContrato.size() > 0) {			
				// Parametros Contrato SAAPV
				String autorizaGestionContrato = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "APV.AutorizaGestionContrato");

				params.clear();
				params.put("IdEmpresa", in.p_IdEmpresa);
				params.put("IdPlan", 1);
				params.put("IdContratoSAAPV", idsContrato);
				List<DatosContratoSAAPV> datosContratoSAAPVList = sql.query(con, SQL_SELECT_PARAMETROS_CONTRATO, params, DatosContratoSAAPV.class);
				List<ParametrosContrato> parametrosContratoList = new ArrayList<ParametrosContrato>();
				for (DatosContratoSAAPV contrato : datosContratoSAAPVList) {
					ParametrosContrato parametrosContrato = copyPojoParametrosContrato(contrato);
					parametrosContrato.FolioContratoSAAPV = contrato.FolioContratoSAAPV;
					parametrosContrato.IdPlan = 2L;
					parametrosContrato.FechaSuscripcion = Fecha.toStringDate(contrato.FechaSuscripcion);
					parametrosContrato.FechaCreacion = Fecha.toStringDate(contrato.FechaCreacion);
					parametrosContrato.FechaAprobacion = Fecha.toStringDate(contrato.FechaAprobacion);
					parametrosContrato.FechaNotificacion = Fecha.toStringDate(contrato.FechaNotificacion);
					parametrosContrato.FechaRecepcion = Fecha.toStringDate(contrato.FechaRecepcion);
					parametrosContrato.Folio = contrato.FolioContratoSAAPV;
					parametrosContrato.IdPerfilComercial = contrato.IdPerfilComercialAPV == null || "".equals(contrato.IdPerfilComercialAPV) ? contrato.IdPerfilComercial : contrato.IdPerfilComercialAPV;
					parametrosContrato.FechaNacimientoCliente = Fecha.toStringDate(contrato.FechaNacimientoCliente);
					parametrosContrato.TelefonoFijo = contrato.TelefonoCliente;
					parametrosContrato.TelefonoMovil = contrato.TelefonoComercialCliente;
					parametrosContrato.TelefonoLaboral = contrato.TelefonoCelularCliente;
					parametrosContrato.ComisionTransferencia = contrato.ComisionTransferencia.doubleValue();
					parametrosContrato.CodigoTipoSolicitud = "MOD";
					parametrosContrato.MontoTraspaso = contrato.MontoTraspaso.doubleValue();
					parametrosContrato.UFTraspaso = contrato.UFTraspaso.doubleValue();
					parametrosContrato.PorcentajeTraspaso = contrato.PorcentajeTraspaso.doubleValue();
					parametrosContrato.IdEstadoEnvioNotificacion = "NEV";
					parametrosContrato.IdEstadoSolicitud = "S".equals(autorizaGestionContrato) ? "PDA" : "";
					parametrosContrato.IdEstado = contrato.IdEstado;
					parametrosContrato.IdCargaMasiva = null;
					parametrosContrato.IdPropositoPortafolio = "";
					parametrosContrato.IdMatrizAgrupadorAPV = 0L;
					parametrosContrato.CodigoAgrupador = "";
					parametrosContrato.IdPaisEmpleador=contrato.IdPaisEmpleador;
					parametrosContrato.IdPaisResidenciaCliente = contrato.IdPaisResidenciaCliente;
					parametrosContratoList.add(parametrosContrato);
				}
				
				HashMap<Long, Object> datosContratoSAAPVHash = Utils.List2Hash(datosContratoSAAPVList, "IdContratoSAAPV");

				// Distribucion Inversion
				List<DatosDistribucionInversion> datosDistribucionInversionList = sql.query(con, SQL_SELECT_DISTRIBUCION_INVERSION, params, DatosDistribucionInversion.class);
				List<ModificarContratoSAAPV.DistribucionInversion> distribucionInversionList = new ArrayList<ModificarContratoSAAPV.DistribucionInversion>();
				for (DatosDistribucionInversion datosDistribucionInversion : datosDistribucionInversionList) {
					ModificarContratoSAAPV.DistribucionInversion distribucionInversion = new ModificarContratoSAAPV.DistribucionInversion();
					distribucionInversion.IdContratoSAAPV = datosDistribucionInversion.IdContratoSAAPV;
					distribucionInversion.FolioContratoSAAPV = datosDistribucionInversion.FolioContratoSAAPV;
					distribucionInversion.IdInstrumento = datosDistribucionInversion.IdInstrumento;
					distribucionInversion.Nemotecnico = datosDistribucionInversion.Nemotecnico;
					distribucionInversion.MontoPesos = datosDistribucionInversion.MontoPesos.doubleValue();
					distribucionInversion.Porcentaje = datosDistribucionInversion.Porcentaje.doubleValue();
					distribucionInversion.IdEstado = datosDistribucionInversion.IdEstado;
					distribucionInversionList.add(distribucionInversion);
				}

				// Portafolios Modificados
				List <DatosPortafoliosModificados> datosPortafoliosModificadosList = sql.query(con, SQL_SELECT_PORTAFOLIOS_MODIFICADOS, params, DatosPortafoliosModificados.class);
				List<ModificarContratoSAAPV.PortafoliosModificados> portafoliosModificadosList = new ArrayList<ModificarContratoSAAPV.PortafoliosModificados>();
				for (DatosPortafoliosModificados datosPortafoliosModificados : datosPortafoliosModificadosList) {
					ModificarContratoSAAPV.PortafoliosModificados portafoliosModificados = new ModificarContratoSAAPV.PortafoliosModificados();
					portafoliosModificados.CodigoAgrupador = datosPortafoliosModificados.CodigoAgrupador;
					portafoliosModificados.IdMatrizAgrupador = datosPortafoliosModificados.IdMatrizAgrupadorAPV;
					portafoliosModificados.OrigenAhorro = datosPortafoliosModificados.OrigenAhorro;
					portafoliosModificados.IdPortafolio = datosPortafoliosModificados.IdPortafolio;
					portafoliosModificados.IdPropositoPortafolio = datosPortafoliosModificados.IdPropositoPortafolio;
					portafoliosModificados.MesInicio = datosPortafoliosModificados.PeriodoInicio;
					portafoliosModificados.MesTermino = datosPortafoliosModificados.PeriodoTermino;
					portafoliosModificados.MontoPesos = datosPortafoliosModificados.MontoPesos.doubleValue();
					portafoliosModificados.MontoUF = datosPortafoliosModificados.MontoUF.doubleValue();
					portafoliosModificados.Pago = datosPortafoliosModificados.Pago;
					portafoliosModificados.PagoNombre = "IND".equals(portafoliosModificados.Pago) ? "INDEFINIDO" : "TEM".equals(portafoliosModificados.Pago) ? "TEMPORAL" : "UNI".equals(portafoliosModificados.Pago) ? "UNICO" : "";
					//portafoliosModificados.PorcentajeRegimenImponible = datosPortafoliosModificados.PorcentajeRegimenImponible.doubleValue();
					portafoliosModificados.Producto = datosPortafoliosModificados.NombreProductoAgrupador;
					portafoliosModificados.Regimen = datosPortafoliosModificados.Codigo;
					portafoliosModificados.IdContratoSAAPVOrigen = datosPortafoliosModificados.IdContratoSAAPV;
					portafoliosModificadosList.add(portafoliosModificados);
				}

				// Detalle Origen Traspaso
				List <DatosDetalleOrigenTraspaso> datosDetalleOrigenTraspasoList = sql.query(con, SQL_SELECT_DETALLE_ORIGEN_TRASPASO, params, DatosDetalleOrigenTraspaso.class);
				List<ModificarContratoSAAPV.DetalleOrigenTraspaso> detalleOrigenTraspasoList = new ArrayList<ModificarContratoSAAPV.DetalleOrigenTraspaso>();
				for (DatosDetalleOrigenTraspaso datosDetalleOrigenTraspaso : datosDetalleOrigenTraspasoList) {
					ModificarContratoSAAPV.DetalleOrigenTraspaso detalleOrigenTraspaso = new ModificarContratoSAAPV.DetalleOrigenTraspaso();
					detalleOrigenTraspaso.IdDetalleOrigenTraspaso = 0L;
					detalleOrigenTraspaso.IdTraspaso = 0L;
					detalleOrigenTraspaso.CodigoTipoAhorro = datosDetalleOrigenTraspaso.CodigoTipoAhorro;
					detalleOrigenTraspaso.IdEntidadRecaudadora = datosDetalleOrigenTraspaso.IdEntidadRecaudadora;
					detalleOrigenTraspaso.NombreEntidad = datosDetalleOrigenTraspaso.NombreEntidad;
					detalleOrigenTraspaso.DocumentoEntidad = datosDetalleOrigenTraspaso.DocumentoEntidad;
					detalleOrigenTraspaso.Fondo = datosDetalleOrigenTraspaso.Fondo;
					detalleOrigenTraspaso.MontoTraspaso = datosDetalleOrigenTraspaso.MontoTraspaso.doubleValue();
					detalleOrigenTraspaso.UFTraspaso = datosDetalleOrigenTraspaso.UFTraspaso.doubleValue();
					detalleOrigenTraspaso.PorcentajeTraspaso = datosDetalleOrigenTraspaso.PorcentajeTraspaso.doubleValue();
					detalleOrigenTraspaso.IdEstado = datosDetalleOrigenTraspaso.IdEstado;
					detalleOrigenTraspaso.IdInstrumento = datosDetalleOrigenTraspaso.IdInstrumento;
					detalleOrigenTraspaso.CodigoRegimenTributario = datosDetalleOrigenTraspaso.CodigoRegimenTributario;
					detalleOrigenTraspaso.IdPortafolio = datosDetalleOrigenTraspaso.IdPortafolio;
					detalleOrigenTraspaso.IdContratoSAAPVOrigen = datosDetalleOrigenTraspaso.IdContratoSAAPV;
					detalleOrigenTraspasoList.add(detalleOrigenTraspaso);
				}

				// Crea los nuevos Contratos SAAPV con Plan Activo
				ModificarContratoSAAPV modificarContratoSAAPV = new ModificarContratoSAAPV();
				ModificarContratoSAAPV.ParametrosEntradaServicio parametrosEntrada = new ModificarContratoSAAPV.ParametrosEntradaServicio();
				parametrosEntrada.p_IdEmpresa = in.p_IdEmpresa;
				parametrosEntrada.p_IdModulo = "APV";
				parametrosEntrada.p_IdUsuario = in.p_IdUsuario;
				parametrosEntrada.p_Tipo = "M";
				parametrosEntrada.p_Parametros = parametrosContratoList;
				parametrosEntrada.p_DistribucionInversion = distribucionInversionList;
				parametrosEntrada.p_PortafoliosModificados = portafoliosModificadosList;
				parametrosEntrada.p_DetalleOrigenTraspaso = detalleOrigenTraspasoList;
				ModificarContratoSAAPV.ParametrosSalidaServicio parametrosSalida = modificarContratoSAAPV.logica(con, parametrosEntrada);

				out.P_OUT_MENSAJES.addAll(parametrosSalida.P_OUT_MENSAJES);

			
				// Modificar Portafolio

			try {
				ModificarPortafolio modificarPortafolio = new ModificarPortafolio();
				HashMap<Long, Boolean> portafolioModificadoFiltro = new HashMap<Long, Boolean>();

				for (DatosPortafoliosModificados datosPortafoliosModificados : datosPortafoliosModificadosList) {
					Long idPortafolio = datosPortafoliosModificados.IdPortafolio;

					// Evita modificar más de una vez el mismo portafolio
					if (portafolioModificadoFiltro.containsKey(idPortafolio)) {
						continue;
					}
					portafolioModificadoFiltro.put(idPortafolio, true);

					DatosContratoSAAPV datosContrato =
						(DatosContratoSAAPV) datosContratoSAAPVHash.get(datosPortafoliosModificados.IdContratoSAAPV);

					if (datosContrato == null) {
						continue;
					}

					String idPerfilComercialAPV = datosContrato.IdPerfilComercialAPV;

					// Solo se modifica el portafolio si el contrato posee perfil APV informado
					if (idPerfilComercialAPV == null || "".equals(idPerfilComercialAPV)) {
						continue;
					}

					ModificarPortafolio.Entrada entradaModificarPortafolio =
						new ModificarPortafolio.Entrada();

					entradaModificarPortafolio.p_IdEmpresa = in.p_IdEmpresa;
					entradaModificarPortafolio.p_IdUsuario = in.p_IdUsuario;
					entradaModificarPortafolio.p_IdModulo = in.p_IdModulo;
					entradaModificarPortafolio.p_UsaReglasNegocio = "N";
					entradaModificarPortafolio.p_IdPortafolio = idPortafolio;

					entradaModificarPortafolio.p_RegistroCambio.add(
						new ModificarPortafolio.RegistroCambio(
							"IdPerfilComercialBaseOrigen",
							idPerfilComercialAPV
						)
					);

					ModificarPortafolio.Salida salidaModificarPortafolio =
						modificarPortafolio.logica(con, entradaModificarPortafolio);

					String folio = datosContrato.FolioContratoSAAPV;

					if (salidaModificarPortafolio.P_OUT_ERRORESDETALLE != null) {
						for (ModificarPortafolio.ErrorDetalle errorDetalle
								: salidaModificarPortafolio.P_OUT_ERRORESDETALLE) {

							out.P_OUT_MENSAJES.add(
								new MensajeSalida(
									datosPortafoliosModificados.IdContratoSAAPV,
									MensajeSalida.MENSAJE_ERROR,
									errorDetalle.Codigo,
									"CONTRATO " + folio
										+ ": Modificar Portafolio: "
										+ errorDetalle.Descripcion
								)
							);
						}
					}

					if (salidaModificarPortafolio.P_OUT_ADVERTENCIASDETALLE != null) {
						for (ModificarPortafolio.AdvertenciaDetalle advertenciaDetalle
								: salidaModificarPortafolio.P_OUT_ADVERTENCIASDETALLE) {

							out.P_OUT_MENSAJES.add(
								new MensajeSalida(
									datosPortafoliosModificados.IdContratoSAAPV,
									MensajeSalida.MENSAJE_ADVERTENCIA,
									advertenciaDetalle.Codigo,
									"CONTRATO " + folio
										+ ": Modificar Portafolio: "
										+ advertenciaDetalle.Descripcion
								)
							);
						}
					}
				}

			} catch (Exception ex) {
				ex.printStackTrace();
				logger.error("Error al modificar portafolios durante cambio de plan", ex);

				out.P_OUT_MENSAJES.add(
					new MensajeSalida(
						0L,
						MensajeSalida.MENSAJE_ERROR,
						"ERROR_MODIFICAR_PORTAFOLIO",
						"Error al modificar portafolios: " + ex.getMessage()
					)
				);
			}
			}
			//Cambio de plan a portafolios
			if(lstPortafolios.size() > 0){
				try {					
					sql.batch(con, SQL_INSERTAR_GLOBAL_PORTAFOLIOS, lstPortafolios, Portafolio.class);
					
					int[] resultadoMod = sql.batch(con, SQL_MODIFICAR_PLAN_PORTAFOLIOS, lstPortafolios, Portafolio.class);				
					
					if(resultadoMod != null && resultadoMod.length > 0){
						out.P_OUT_MENSAJES.add(new MensajeSalida(
							(long)lstPortafolios.size(),
							MensajeSalida.MENSAJE_OK,
							"MODIFICA_PLAN_PORTAFOLIO_OK",
							"Cambio de Plan a " + resultadoMod.length + " Portafolios OK.")
						);
					}else{
						out.P_OUT_MENSAJES.add(new MensajeSalida(
							(long)lstPortafolios.size(),
							MensajeSalida.MENSAJE_ERROR,
							"ERROR_MODIFICA_PLAN_PORTAFOLIO",
							"Problema al modificar el plan de Portafolios.")
						);
					}	
				}catch(Exception ex) {
					ex.printStackTrace();
					logger.error(ex);
				}
			}
			
			// Parametros de salida
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		}
		catch(Exception e){
			out.P_OUT_ERRORESCONTADOR = 1L;
			e.printStackTrace();
			logger.error(e);
			throw e;
		}

		logger.info("Termino del servicio CambiarAPlanActivoContratoSAAPV, tiempo:" + (System.currentTimeMillis() - init));
		return out;
	}

	private ParametrosContrato copyPojoParametrosContrato(DatosContratoSAAPV contrato) {
		ParametrosContrato parametrosContrato = new ParametrosContrato();
		parametrosContrato.IdContratoSAAPV = contrato.IdContratoSAAPV;
		parametrosContrato.IdEmpresa = contrato.IdEmpresa;
		parametrosContrato.FolioContratoSAAPV = contrato.FolioContratoSAAPV;
		parametrosContrato.IdPlan = contrato.IdPlan;
		parametrosContrato.NombrePlan = contrato.NombrePlan;
		// parametrosContrato.FechaSuscripcion = contrato.FechaSuscripcion;
		// parametrosContrato.FechaCreacion = contrato.FechaCreacion;
		// parametrosContrato.FechaAprobacion = contrato.FechaAprobacion;
		// parametrosContrato.FechaNotificacion = contrato.FechaNotificacion;
		// parametrosContrato.FechaRecepcion = contrato.FechaRecepcion;
		parametrosContrato.Folio = contrato.Folio;
		parametrosContrato.IdPersona = contrato.IdPersona;
		parametrosContrato.IdEmpleador = contrato.IdEmpleador;
		parametrosContrato.IdCliente = contrato.IdCliente;
		parametrosContrato.IdCategoriaInversionista = contrato.IdCategoriaInversionista;
		parametrosContrato.IdPerfilComercial = contrato.IdPerfilComercial;
		parametrosContrato.NombreCliente = contrato.NombreCliente;
		parametrosContrato.ApellidoPaternoCliente = contrato.ApellidoPaternoCliente;
		parametrosContrato.ApellidoMaternoCliente = contrato.ApellidoMaternoCliente;
		parametrosContrato.DocumentoIdentidadCliente = contrato.DocumentoIdentidadCliente;
		// parametrosContrato.FechaNacimientoCliente = contrato.FechaNacimientoCliente;
		parametrosContrato.EstadoCivil = contrato.EstadoCivil;
		parametrosContrato.IdTipoGenero = contrato.IdTipoGenero;
		parametrosContrato.IdPaisResidenciaCliente = contrato.IdPaisResidenciaCliente;
		parametrosContrato.IdCiudadCliente = contrato.IdCiudadCliente;
		parametrosContrato.IdComunaCliente = contrato.IdComunaCliente;
		parametrosContrato.DireccionCliente = contrato.DireccionCliente;
		parametrosContrato.NumeroDireccionCliente = contrato.NumeroDireccionCliente;
		parametrosContrato.IdTipoCargoPersona = contrato.IdTipoCargoPersona;
		// parametrosContrato.TelefonoFijo = contrato.TelefonoFijo;
		// parametrosContrato.TelefonoMovil = contrato.TelefonoMovil;
		// parametrosContrato.TelefonoLaboral = contrato.TelefonoLaboral;
		parametrosContrato.EmailCliente = contrato.EmailCliente;
		parametrosContrato.RazonSocialEmpleador = contrato.RazonSocialEmpleador;
		parametrosContrato.IdMedioRecaudacionTrabajador = contrato.IdMedioRecaudacionTrabajador;
		parametrosContrato.IdEntidadRecaudadora = contrato.IdEntidadRecaudadora;
		parametrosContrato.DocumentoIdentidadEmpleador = contrato.DocumentoIdentidadEmpleador;
		parametrosContrato.IdPaisEmpleador = contrato.IdPaisEmpleador;
		parametrosContrato.DireccionComercialEmpleador = contrato.DireccionComercialEmpleador;
		parametrosContrato.NumeroDireccionEmpleador = contrato.NumeroDireccionEmpleador;
		parametrosContrato.IdCiudadEmpleador = contrato.IdCiudadEmpleador;
		parametrosContrato.IdComunaEmpleador = contrato.IdComunaEmpleador;
		parametrosContrato.TelefonoEmpleador = contrato.TelefonoEmpleador;
		parametrosContrato.NombreEncargado = contrato.NombreEncargado;
		parametrosContrato.EmailEncargado = contrato.EmailEncargado;
		parametrosContrato.TelefonoEncargado = contrato.TelefonoEncargado;
		parametrosContrato.CodigoPrevision = contrato.CodigoPrevision;
		parametrosContrato.Pensionado = contrato.Pensionado;
		parametrosContrato.Trabajador = contrato.Trabajador;
		parametrosContrato.FormaDePago = contrato.FormaDePago;
		parametrosContrato.IdMedioRecaudacionFormaPago = contrato.IdMedioRecaudacionFormaPago;
		parametrosContrato.DiaDescuento = contrato.DiaDescuento;
		parametrosContrato.Institucion = contrato.Institucion;
		// parametrosContrato.ComisionTransferencia = contrato.ComisionTransferencia;
		parametrosContrato.CodigoTipoSolicitud = contrato.CodigoTipoSolicitud;
		parametrosContrato.TipoMovimiento = contrato.TipoMovimiento;
		parametrosContrato.RazonRetiro = contrato.RazonRetiro;
		parametrosContrato.NombreEntidadOrigen = contrato.NombreEntidadOrigen;
		parametrosContrato.RutEntidadOrigen = contrato.RutEntidadOrigen;
		parametrosContrato.IdPersonaOrigen = contrato.IdPersonaOrigen;
		parametrosContrato.Descripcion = contrato.Descripcion;
		parametrosContrato.TipoDeAhorro = contrato.TipoDeAhorro;
		parametrosContrato.FondoDeOrigen = contrato.FondoDeOrigen;
		parametrosContrato.TipoTraspaso = contrato.TipoTraspaso;
		// parametrosContrato.MontoTraspaso = contrato.MontoTraspaso;
		// parametrosContrato.UFTraspaso = contrato.UFTraspaso;
		// parametrosContrato.PorcentajeTraspaso = contrato.PorcentajeTraspaso;
		parametrosContrato.IdEstadoEnvioNotificacion = contrato.IdEstadoEnvioNotificacion;
		parametrosContrato.IdEstadoSolicitud = contrato.IdEstadoSolicitud;
		parametrosContrato.IdEstado = contrato.IdEstado;
		parametrosContrato.IdCargaMasiva = contrato.IdCargaMasiva;
		parametrosContrato.IdEmpleado = contrato.IdEmpleado;
		parametrosContrato.IdInterno = contrato.IdInterno;
		return parametrosContrato;
	}

	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");

		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
		
			String jsonDESA = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"APV\",\"p_ContratoCambioPlanIn\":[{\"IdPersona\":1750936,\"IdCliente\":1419564,\"IdPortafolio\":1450489,\"IdContratoSAAPV\":167,\"FolioContratoSAAPV\":113433}]}";
			CambiarAPlanActivoEntrada Objin = gson.fromJson(jsonDESA, CambiarAPlanActivoEntrada.class);

			CambiarAPlanActivoContratoSAAPV demo = new CambiarAPlanActivoContratoSAAPV();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			System.out.println("SALIDA");

			try {
				CambiarAPlanActivoSalida ObjOut = demo.logica(con, Objin);
				// con.commit();
				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				// er.printStackTrace();
			}
			finally {
				con.rollback();
				con.close();
			}
		}
		System.out.println("== END ==");
	}
}
