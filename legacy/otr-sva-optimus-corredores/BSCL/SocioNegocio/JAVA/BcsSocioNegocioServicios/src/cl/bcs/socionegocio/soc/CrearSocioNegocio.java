package cl.bcs.socionegocio.soc;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.ind.ModificarIndicador;
import cl.bcs.plataforma.ind.ModificarIndicador.EntradaModificarIndicador;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.per.CrearDireccionElectronicaMasivo;
import cl.bcs.socionegocio.per.CrearDireccionFisicaMasivo;
import cl.bcs.socionegocio.per.CrearDocumentoIdentidad;
import cl.bcs.socionegocio.per.CrearPersona;
import cl.bcs.socionegocio.per.CrearPersonaCuentaBancaria;
import cl.bcs.socionegocio.per.CrearPersonaCuentaBancaria.EntradaCrearPersonaCuentaBancaria;
import cl.bcs.socionegocio.per.CrearPersonaCuentaBancaria.SalidaCrearPersonaCuentaBancaria;
import cl.bcs.socionegocio.per.CrearPersonaNombreFantasia;
import cl.bcs.socionegocio.per.CrearPersonaNombreFantasia.EntradaCrearPersonaNombreFantasia;
import cl.bcs.socionegocio.per.CrearPersonaNombreFantasia.SalidaCrearPersonaNombreFantasia;
import cl.bcs.socionegocio.per.CrearPersonaTarjetaBancaria;
import cl.bcs.socionegocio.per.CrearPersonaTarjetaBancaria.EntradaCrearPersonaTarjetaBancaria;
import cl.bcs.socionegocio.per.CrearPersonaTarjetaBancaria.SalidaCrearPersonaTarjetaBancaria;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.EntidadPersonaEliminar;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.EntradaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.SalidaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.CrearTelefonoMasivo;
import cl.bcs.socionegocio.per.CrearTelefonoMasivo.EntradaCrearTelefonoMasivo;
import cl.bcs.socionegocio.per.CrearTelefonoMasivo.SalidaCrearTelefonoMasivo;
import cl.bcs.socionegocio.per.EliminarPersonaDireccionElectronicaMasivo;
import cl.bcs.socionegocio.per.EliminarPersonaDireccionFisicaMasivo;
import cl.bcs.socionegocio.per.EliminarPersonaTelefonoMasivo;
import cl.bcs.socionegocio.per.CrearDireccionElectronicaMasivo.EntradaCrearDireccionElectronicaMasivo;
import cl.bcs.socionegocio.per.CrearDireccionElectronicaMasivo.SalidaCrearDireccionElectronicaMasivo;
import cl.bcs.socionegocio.per.CrearDireccionFisicaMasivo.EntradaCrearDireccionFisicaMasivo;
import cl.bcs.socionegocio.per.CrearDireccionFisicaMasivo.SalidaCrearDireccionFisicaMasivo;
import cl.bcs.socionegocio.cli.CrearCliente;
import cl.bcs.socionegocio.cli.CrearDeclaracionOrigenFondos;
import cl.bcs.socionegocio.cli.CrearDeclaracionOrigenFondos.EntradaCrearDeclaracionOrigenFondos;
import cl.bcs.socionegocio.cli.CrearDeclaracionOrigenFondos.SalidaCrearDeclaracionOrigenFondos;
import cl.bcs.socionegocio.pojo.Enrolamiento;
import cl.bcs.socionegocio.pojo.ErrorDetalle;
import cl.bcs.socionegocio.rel.CrearRelacionPersona;
import cl.bcs.socionegocio.rel.CrearRelacionPersona.EntradaCrearRelacionPersona;
import cl.bcs.socionegocio.rel.CrearRelacionPersona.SalidaCrearRelacionPersona;
import cl.bcs.socionegocio.soc.ModificarSocioNegocio.ModificarSocioNegocio_Entrada;
import cl.bcs.socionegocio.soc.ModificarSocioNegocio.ModificarSocioNegocio_Salida;
import cl.bcs.socionegocio.wbs.CrearClienteExpress.ErroresDetalle;
import cl.bcs.socionegocio.wbs.CrearClienteExpress.MensajeAdvertencia;
import cl.bcs.socionegocio.wbs.CrearClienteFullWS;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.StringUtils;
import cl.bcs.utils.Utils;

public class CrearSocioNegocio implements JavaCall {
	static final Logger logger = LogManager.getLogger(CrearSocioNegocio.class);
	// *****************************************************//
	// NOTAS :
	// 1.- p_IdUsuario ES STRING O LONG
	// 2.- FECHA DE ENTREGA // 29-01-2019
	// 3.- EN Cuenta Bancaria SOBRA EL CAMPO CodigoSucursal

	// -------------------------------------------------
	// QUERIES
	// -------------------------------------------------
	private final String SQL_SELECT_IDPERSONA_RELACIONADA = loadResourceToString("cl/bcs/socionegocio/soc/sql/CrearSocioNegocio_SelectIdPersonaRelacionada.sql");
	private final String SQL_SELECT_IDPERSONA = loadResourceToString("cl/bcs/socionegocio/soc/sql/CrearSocioNegocio_SelectIdPersona.sql");
	private final String SQL_CONSULTAR_EXISTENCIA_PERSONA = loadResourceToString("cl/bcs/socionegocio/per/sql/ConsultaExistenciaPersona.sql");
	private final String SQL_UPDATE_RELACION_PERSONA = loadResourceToString("cl/bcs/socionegocio/soc/sql/CrearSocioNegocio_UpdateRelacionPersona.sql");
	private final String SQL_SELECT_INDICADORES_PERSONA = loadResourceToString("cl/bcs/socionegocio/soc/sql/CrearSocioNegocio_SelectIndicadoresPersonas.sql");
	private final String SQL_SELECT_INDICADORES_CLIENTE = loadResourceToString("cl/bcs/socionegocio/soc/sql/CrearSocioNegocio_SelectIndicadoresClientes.sql");
	private final String SQL_SELECT_DIRECCION_FISICA_PERSONA = loadResourceToString("cl/bcs/socionegocio/soc/sql/ConsultarSocioNegocio_SelectDireccionFisica.sql");
	private final String SQL_SELECT_DIRECCION_ELECTRONICA_PERSONA = loadResourceToString("cl/bcs/socionegocio/soc/sql/ConsultarSocioNegocio_SelectDireccionElectronica.sql");
	private final String SQL_SELECT_TELEFONO_PERSONA = loadResourceToString("cl/bcs/socionegocio/soc/sql/ConsultarSocioNegocio_SelectTelefono.sql");
	private final String SQL_PREDETERMINAR_PERSONA_DIRECCION_ELECTRONICA = loadResourceToString("cl/bcs/socionegocio/soc/sql/CrearSocioNegocio_Call_PredeterminarDireccionElectronica.sql");
	private final String SQL_PREDETERMINAR_PERSONA_DIRECCION_FISICA = loadResourceToString("cl/bcs/socionegocio/soc/sql/CrearSocioNegocio_Call_PredeterminarDireccionFisica.sql");
	private final String SQL_PREDETERMINAR_PERSONA_TELEFONO = loadResourceToString("cl/bcs/socionegocio/soc/sql/CrearSocioNegocio_Call_PredeterminarTelefono.sql");

	private final String CODIGO_FORMULARIO_CLIENTE_FULL_NATURAL = "INGRESAR_CLIENTE_FULL_NATURAL";
	private final String CODIGO_FORMULARIO_CLIENTE_FULL_JURIDICA = "INGRESAR_CLIENTE_FULL_JURIDICA";
	private static final String USA_REGLAS_NEGOCIO = "S";
	private static final String ID_MODULO = "SOC";
	// -------------------------------------------------
	// CLASES DE ENTRADA
	// -------------------------------------------------
	public static class CrearSocioNegocio_Entrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario; // preguntar si es String o Long
		public String p_ClaveUsuario;
		public List<SocioNegocio> p_SocioNegocio;

		public CrearSocioNegocio_Entrada() {
			p_SocioNegocio = new ArrayList<SocioNegocio>();
		}

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class SocioNegocio {
		public Long IdEntidad = 0L;
		public String TipoEntidad = "";
		public String TipoDocumentoIdentidad = "";
		public String DocumentoIdentidad = "";
		public String Nombres = "";
		public String ApellidoPaterno = "";
		public String ApellidoMaterno = "";
		public String RazonSocial = "";
		public Long IdGiro = 0L;
		public String FechaNacimientoConstitucion = "0001-01-01";
		public String IdEstadoCivil = "";
		public Long IdPaisNacimiento = 0L;
		public String IdRegimenConyugal = "";
		public Long IdPaisNacionalidad = 0L;
		public String IdGenero = "";
		public String IdTipoPersona = "";
		public Long IdPaisResidencia = 0L;
		public Long IdProfesion = 0L;
		public String IdTipoRelacion = ""; // para indicar si esta persona es una persona relacionada
		public Long IdEntidadRelacionada = 0L; // en el caso que la relacion no sea con el cliente, sino con otra
												// persona relacionada, aqui se puede indicar
		public String IdSituacionLaboral = "";
		public String IdTipoCanalIngresoCorredor = "";
		public List<DatosCliente> DatosCliente;
		public List<TiposDocumento> TiposDocumento;
		public List<DireccionFisica> DireccionFisica;
		public List<Telefono> Telefono;
		public List<DireccionElectronica> DireccionElectronica;
		public List<IndicadorCliente> IndicadorCliente;
		public List<IndicadorPersona> IndicadorPersona;
		public List<Indicador> IndicadorPortafolio;
		public List<CuentaBancaria> CuentaBancaria;
		public List<TarjetasBancaria> TarjetasBancaria;
		public List<DeclaracionOrigenFondo> DeclaracionOrigenFondo;
		public List<NombreFantasia> NombreFantasia;

		public SocioNegocio() {
			DatosCliente = new ArrayList<DatosCliente>();
			TiposDocumento = new ArrayList<TiposDocumento>();
			DireccionFisica = new ArrayList<DireccionFisica>();
			Telefono = new ArrayList<Telefono>();
			DireccionElectronica = new ArrayList<DireccionElectronica>();
			IndicadorCliente = new ArrayList<IndicadorCliente>();
			IndicadorPersona = new ArrayList<IndicadorPersona>();
			IndicadorPortafolio = new ArrayList<Indicador>();
			CuentaBancaria = new ArrayList<CuentaBancaria>();
			TarjetasBancaria = new ArrayList<TarjetasBancaria>();
			DeclaracionOrigenFondo = new ArrayList<DeclaracionOrigenFondo>();
			NombreFantasia = new ArrayList<NombreFantasia>();
		}

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class DatosCliente {
		public String IdCategoriaInversionista = "";
		public String IdPerfilComercial = "";
		public Long CodigoEjecutivo = 0L;
		public String IdTipoCliente = "";
		public String NombreSegmentoCliente = "";

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class TiposDocumento {
		public String TipoDocumento = "";
		public String NumeroDocumento = "";

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class DireccionFisica {
		public String Accion = "";
		public Long IdDireccionFisica = 0L;
		public String IdProposito = "";
		public String Sector = "";
		public String CodigoPostal = "";
		public Long IdComuna = 0L;
		public String Calle = "";
		public String Numero = "";
		public String Departamento = "";
		public String Complemento = "";
		public Long IdCiudad = 0L;

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public String getDireccionFisicaCalleNumero() {
			return this.Calle +" "+ this.Numero;
		}
	}

	public static class Telefono {
		public String Accion = "";
		public Long IdTelefono = 0L;
		public String IdProposito = "";
		public String Numero = "";
		public Long IdPais = 0L;
		public String Tipo = "";
		public String Anexo = "";

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class DireccionElectronica {
		public String Accion = "";
		public Long IdDireccionElectronica = 0L;
		public String Tipo = "";
		public String IdProposito = "";
		public String Direccion = "";

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class IndicadorCliente {
		public String ValorIndicador = "";
		public String CodigoTipoIndicador = "";

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class IndicadorPersona {
		public String ValorIndicador = "";
		public String CodigoTipoIndicador = "";

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class Indicador extends BcsPojo {
		public String ValorIndicador;
		public String CodigoTipoIndicador;
		public Long IdTipoIndicador;
		public Long IdIndicador;

		public Indicador() {
			this.CodigoTipoIndicador = "";
			this.ValorIndicador = "";
			this.IdTipoIndicador = 0L;
			this.IdIndicador = 0L;
		}
	}

	public static class CuentaBancaria {
		public String NumeroCuentaBancaria = "";
		public String IdEstado = "";
		public String IdMoneda = "";
		public String CodigoTipoCuentaBancaria = "";
		public Long IdBanco = 0L;
		public String CodigoSucursal = "";
		public Long CodigoPlaza = 0L;

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class TarjetasBancaria {
		public Long IdBanco = 0L;
		public String IdTipo = "";
		public String NumeroTarjeta = "";
		public String CodigoVerificacion = "";
		public String FechaVencimiento = "";

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class DeclaracionOrigenFondo {
		public String IdTipoOperacionOrigenFondos = "";
		public String FechaDeclaracion = "";
		public String Comentario = "";
		public String OperacionInscripcion = "";
		public String IdEstado = "";
		public List<DetalleOrigenFondo> DetalleOrigenFondo;

		public DeclaracionOrigenFondo() {
			DetalleOrigenFondo = new ArrayList<DetalleOrigenFondo>();
		}

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class DetalleOrigenFondo {
		public String IdTipoOrigenFondo = "";
		public Long Valor = 0L;
		public String Otros = "";

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class NombreFantasia {
		public String Accion = "";
		public Long IdNombreFantasia = 0L;
		public String Nombre = "";
		public String IdEstado = "";

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// -------------------------------------------------
	// CLASES DE SALIDA
	// -------------------------------------------------
	public static class CrearSocioNegocio_Salida extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public Long p_Out_ContadorEntidadCreada;
		public List<EntidadCreada> p_Out_EntidadCreada;
		public List<MensajeSalida> P_OUT_ADVERTENCIAS;
		public Long P_OUT_ADVERTENCIASCONTADOR;

		public CrearSocioNegocio_Salida() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_ADVERTENCIASCONTADOR = 0L;
			this.p_Out_ContadorEntidadCreada = 0L;
			this.p_Out_EntidadCreada = new ArrayList<>();
			this.P_OUT_ADVERTENCIAS = new ArrayList<>();
		}
	}

	public static class EntidadCreada {
		public String TipoEntidad;
		public Long IdEntidad;
		public String Estado;
		public String Nuevo;
		public List<MensajesErrores> Mensajes;

		public EntidadCreada() {
			Mensajes = new ArrayList<MensajesErrores>();
		}

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class MensajesErrores {
		public Long IdInterno;
		public String TipoMensaje;
		public String Codigo;
		public String Mensaje;

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// -------------------------------------------------
	// CLASES UTILITARIAS
	// -------------------------------------------------
	public static class SelectIdPersona {
		public Long IdPersona = 0L;

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class SelectConsultarIndicadores {
		public Long IdIndicador = 0L;
		public Long IdTipoIndicador = 0L;
		public String ValorIndicador = "";
		public String CodigoTipoIndicador;

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class IndicadorPersonaCliente {
		public Long IdIndicador = 0L;
		public Long IdTipoIndicador = 0L;
		public String ValorIndicador = "";
		public Date FechaVencimiento = Fecha.toDate("0001-01-01 00:00:00");

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class Relaciones {
		public Long IdEntidad = 0L;
		public Long IdPersona = 0L;
		public Long IdCliente = 0L;

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class UpdateRelacionPersona {
		public Long p_IdPersonaConyuge = 0L;
		public Long p_IdPersonaEmpleador = 0L;
		public Long p_IdPersonaRepresentante = 0L;
		public Long p_IdPersona = 0L;

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class IdSecuencias {
		public Long IdSecuencia = 0L;

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class PersonaRelacionaExistente {
		public Long IdPersona;
		public Long IdSocioDeNegocio;
		public String DocumentoIdentidad;
		public String TipoDocumentoIdentidad;
		public String Tipo;

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ConsultaExistePersona extends BcsPojo {
		public Long IdPersona;
		public Long IdSocioDeNegocio;
		public Long IdCliente;

		public ConsultaExistePersona() {
			this.IdPersona = 0L;
			this.IdSocioDeNegocio = 0L;
			this.IdCliente = 0L;
		}
	}

	// -------------------------------------------------
	// LOGICA DEL SERVICIO
	// -------------------------------------------------
	public CrearSocioNegocio_Salida logica(Connection conn, CrearSocioNegocio_Entrada in) throws Exception {
		SQL sql = new SQL();

		CrearSocioNegocio_Salida salida = new CrearSocioNegocio_Salida();

		String agregaAdvertencia = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "SOCIODENEGOCIO", "SOC.MensajeAdvertenciaRelacionadoExiste");
		String EditarTelefonoYDirecciones = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "SOCIODENEGOCIO", "PER.EditarTelefonoYDirecciones");
		Boolean ExistenEdiciones = false, EdicionTelefonos = false, EdicionDireccionesFisicas = false, 	EdicionCorreos = false;
		
		String FechaSistema = Utils.getFechaSistema(conn, Utils.getSchema(), in.p_IdEmpresa);
		String FechaHoraSistema = Utils.getFechaHoraSistema(conn, Utils.getSchema(), in.p_IdEmpresa);

		Long ErroresContador = 1L;
		Long ContadorEntidadCreada = 0L;

		List<EntidadCreada> listEntidadCreada = new ArrayList<EntidadCreada>();
		List<MensajesErrores> listMensajesErrores = new ArrayList<MensajesErrores>();
		List<Relaciones> listRelaciones = new ArrayList<Relaciones>();

		final Map<String, SocioNegocioUtils.TipoSegmento> TipoSegmentoPorNombre = SocioNegocioUtils .obtenerTiposSegmentos(conn, in.p_IdEmpresa).stream()
				.collect(Collectors.toMap(u -> u.NombreSegmentoCliente, u -> u));

		if(in.p_SocioNegocio.size() > 0){
			/*SIMULACION PARA COMPROBAR QUE LOS DATOS BASICOS OBLIGATORIOS ESTEN PRESENTES EN EL JSON*/
			for (SocioNegocio socioNegocio : in.p_SocioNegocio){
				Long ContadorDireccionFisica = 0L;
				Long ContadorTelefono = 0L;
				Long ContadorDireccionElectronica = 0L;

				EntidadCreada entidadCreada = new EntidadCreada();
				MensajesErrores mensajesErrores = new MensajesErrores();

				entidadCreada.TipoEntidad = socioNegocio.TipoEntidad.trim();
				entidadCreada.IdEntidad = 0L;
				entidadCreada.Estado = "";
				entidadCreada.Nuevo = "";

				ConsultaExistePersona personaExiste = validarExistenciaSocioNegocio(conn, socioNegocio, in.p_IdEmpresa);
				
				if(!socioNegocio.DireccionFisica.isEmpty())
				{
					for(DireccionFisica direccionFisica : socioNegocio.DireccionFisica)
					{
						if((socioNegocio.IdTipoPersona.trim().equals("N") && direccionFisica.IdProposito.equals("PART")) || (socioNegocio.IdTipoPersona.trim().equals("J") && direccionFisica.IdProposito.equals("COME")))
						{
							ContadorDireccionFisica++;
							break;
						}
					}
				}

				if(!socioNegocio.Telefono.isEmpty())
				{
					for(Telefono telefono : socioNegocio.Telefono)
					{
						if((socioNegocio.IdTipoPersona.trim().equals("N") && telefono.IdProposito.equals("PART")) ||( socioNegocio.IdTipoPersona.trim().equals("J") && telefono.IdProposito.equals("COME")))
						{
							ContadorTelefono++;
							break;
						}
					}
				}
				
				if(!socioNegocio.DireccionElectronica.isEmpty())
				{
					for(DireccionElectronica direccionElectronica : socioNegocio.DireccionElectronica)
					{
						
						if((socioNegocio.IdTipoPersona.trim().equals("N") && direccionElectronica.IdProposito.equals("PART")) || (socioNegocio.IdTipoPersona.trim().equals("J") && direccionElectronica.IdProposito.equals("COME")))
						{
							ContadorDireccionElectronica++;
							break;
						}
					}
				}

				if (ContadorDireccionFisica == 0 && personaExiste.IdPersona.compareTo(0L) == 0) {
					entidadCreada.TipoEntidad = socioNegocio.TipoEntidad.trim();
					entidadCreada.IdEntidad = socioNegocio.IdEntidad;
					entidadCreada.Estado = "";
					entidadCreada.Nuevo = "";

					mensajesErrores.IdInterno = 0L;
					mensajesErrores.TipoMensaje = "ERROR";

					if (socioNegocio.IdTipoPersona.trim().equals("N")) {
						mensajesErrores.Codigo = "DEBE_TENER_COMO_MINIMO_UNA_DIRECCION_FISICA_PARTICULAR";
						mensajesErrores.Mensaje = "DEBE_TENER_COMO_MINIMO_UNA_DIRECCION_FISICA_PARTICULAR";
					} else if (socioNegocio.IdTipoPersona.trim().equals("J")) {
						mensajesErrores.Codigo = "DEBE_TENER_COMO_MINIMO_UNA_DIRECCION_FISICA_COMERCIAL";
						mensajesErrores.Mensaje = "DEBE_TENER_COMO_MINIMO_UNA_DIRECCION_FISICA_COMERCIAL";
					}

					listMensajesErrores.add(mensajesErrores);
					entidadCreada.Mensajes = listMensajesErrores;
					listEntidadCreada.add(entidadCreada);
					break;

				} else if (ContadorTelefono == 0 && personaExiste.IdPersona.compareTo(0L) == 0) {
					entidadCreada.TipoEntidad = socioNegocio.TipoEntidad.trim();
					entidadCreada.IdEntidad = socioNegocio.IdEntidad;
					entidadCreada.Estado = "";
					entidadCreada.Nuevo = "";

					mensajesErrores.IdInterno = 0L;
					mensajesErrores.TipoMensaje = "ERROR";

					if (socioNegocio.IdTipoPersona.trim().equals("N")) {
						mensajesErrores.Codigo = "DEBE_TENER_COMO_MINIMO_UN_TELEFONO_PARTICULAR";
						mensajesErrores.Mensaje = "DEBE_TENER_COMO_MINIMO_UN_TELEFONO_PARTICULAR";
					} else if (socioNegocio.IdTipoPersona.trim().equals("J")) {
						mensajesErrores.Codigo = "DEBE_TENER_COMO_MINIMO_UN_TELEFONO_COMERCIAL";
						mensajesErrores.Mensaje = "DEBE_TENER_COMO_MINIMO_UN_TELEFONO_COMERCIAL";
					}

					listMensajesErrores.add(mensajesErrores);
					entidadCreada.Mensajes = listMensajesErrores;
					listEntidadCreada.add(entidadCreada);
					break;
				} else if (ContadorDireccionElectronica == 0 && personaExiste.IdPersona.compareTo(0L) == 0) {
					entidadCreada.TipoEntidad = socioNegocio.TipoEntidad.trim();
					entidadCreada.IdEntidad = socioNegocio.IdEntidad;
					entidadCreada.Estado = "";
					entidadCreada.Nuevo = "";

					mensajesErrores.IdInterno = 0L;
					mensajesErrores.TipoMensaje = "ERROR";

					if (socioNegocio.IdTipoPersona.trim().equals("N")) {
						mensajesErrores.Codigo = "DEBE_TENER_COMO_MINIMO_UNA_DIRECCION_ELECTRONICA_PARTICULAR";
						mensajesErrores.Mensaje = "DEBE_TENER_COMO_MINIMO_UNA_DIRECCION_ELECTRONICA_PARTICULAR";
					} else if (socioNegocio.IdTipoPersona.trim().equals("J")) {
						mensajesErrores.Codigo = "DEBE_TENER_COMO_MINIMO_UNA_DIRECCION_ELECTRONICA_COMERCIAL";
						mensajesErrores.Mensaje = "DEBE_TENER_COMO_MINIMO_UNA_DIRECCION_ELECTRONICA_COMERCIAL";
					}

					listMensajesErrores.add(mensajesErrores);
					entidadCreada.Mensajes = listMensajesErrores;
					listEntidadCreada.add(entidadCreada);
					break;
				} 
				
				
				if (("S").equals(EditarTelefonoYDirecciones) && socioNegocio.IdTipoRelacion.equals("") ) { 
					// declarar que la persona existe, para activar la nueva funcionalidad
					
					if(personaExiste.IdPersona.compareTo(0L) != 0) {
						
						ExistenEdiciones = true;

						if (ContadorDireccionFisica != 0) {
							EdicionDireccionesFisicas = true;						
						}

						if (ContadorDireccionElectronica != 0) {
							EdicionCorreos = true;
						}

						if (ContadorTelefono != 0) {
							EdicionTelefonos = true;
						}
						
					}

				}
			}

			if (!listEntidadCreada.isEmpty()) {
				salida.P_OUT_ERRORESCONTADOR = ErroresContador;
				salida.p_Out_ContadorEntidadCreada = ContadorEntidadCreada;
				salida.p_Out_EntidadCreada = listEntidadCreada;

				return salida;
			}

			List<SocioNegocio> _socioNegocioPersonaRelacionada = in.p_SocioNegocio.parallelStream().filter(socioNegocio -> {
						return socioNegocio.IdEntidadRelacionada > 0 && !socioNegocio.IdTipoRelacion.trim().isEmpty();
					}).collect(Collectors.toList());
			List<String> documentosIdentidadPersonaRelacionada = new ArrayList<String>();
			List<String> tipoDocumentoPersonaRelacionada = new ArrayList<String>();
			for (SocioNegocio socioNegocio : _socioNegocioPersonaRelacionada) {
				documentosIdentidadPersonaRelacionada.add(socioNegocio.DocumentoIdentidad);
				tipoDocumentoPersonaRelacionada.add(socioNegocio.TipoDocumentoIdentidad);
			}
			HashMap<String, Object> parametros = new HashMap<String, Object>();
			parametros.put("DocumentoIdentidad", documentosIdentidadPersonaRelacionada);
			parametros.put("TDI", tipoDocumentoPersonaRelacionada);

			List<PersonaRelacionaExistente> personasRelacionadasExistentes = new ArrayList<PersonaRelacionaExistente>();
			List<PersonaRelacionaExistente> personasNoRelacionadasExistentes = new ArrayList<PersonaRelacionaExistente>();

			if (!documentosIdentidadPersonaRelacionada.isEmpty() && !tipoDocumentoPersonaRelacionada.isEmpty()) {
				logger.debug("INICIO::SQL_SELECT_IDPERSONA_RELACIONADA");
	          	personasRelacionadasExistentes = sql.query(conn, SQL_SELECT_IDPERSONA_RELACIONADA, parametros, PersonaRelacionaExistente.class);
	          	logger.debug("FIN::SQL_SELECT_IDPERSONA_RELACIONADA");
	        } else {
          		List<SocioNegocio> _socioNegocioPersonaNoRelacionada = in.p_SocioNegocio
          			.parallelStream()
          			.filter(socioNegocio->{
          				return socioNegocio.IdEntidadRelacionada == 0 && socioNegocio.IdTipoRelacion.trim().isEmpty();
          			})
          			.collect(Collectors.toList());
          		
          		List<String> documentosIdentidadPersonaNoRelacionada = new ArrayList<String>();
              	List<String> tipoDocumentoPersonaNoRelacionada = new ArrayList<String>();
              	for(SocioNegocio socioNegocio : _socioNegocioPersonaNoRelacionada) {
              		documentosIdentidadPersonaNoRelacionada.add(socioNegocio.DocumentoIdentidad);
              		tipoDocumentoPersonaNoRelacionada.add(socioNegocio.TipoDocumentoIdentidad);
              	}
          		parametros.clear();
          		parametros.put("DocumentoIdentidad",documentosIdentidadPersonaNoRelacionada);
              	parametros.put("TDI",tipoDocumentoPersonaNoRelacionada);
              	
              	logger.debug("INICIO::SQL_SELECT_IDPERSONA_RELACIONADA");
              	personasNoRelacionadasExistentes = sql.query(conn, SQL_SELECT_IDPERSONA_RELACIONADA, parametros, PersonaRelacionaExistente.class);
              	logger.debug("FIN::SQL_SELECT_IDPERSONA_RELACIONADA");
          	}
          	
			for (SocioNegocio socioNegocio : in.p_SocioNegocio) {
				Long IdPersona = 0L;
				Long IdCliente = 0L;
				Long ContadorErrores = 0L;
				Long ContadorEntidadModificada = 0L;

				EntidadCreada entidadCreada = new EntidadCreada();
				MensajesErrores mensajesErrores = new MensajesErrores();
				Relaciones relaciones = new Relaciones();

				List<Enrolamiento> listEnrolamiento = new ArrayList<Enrolamiento>();
				Enrolamiento enrolamiento = new Enrolamiento();

				enrolamiento.IdEnrolamiento = 0L;
				enrolamiento.IdEmpresa = 1L;
				enrolamiento.IdUsuario = 1L;
				enrolamiento.IdTipoEntidad = socioNegocio.TipoEntidad.trim();
				enrolamiento.IdEntidadEnrolada = 0L;
				enrolamiento.IdTipoDocumentoIdentidad = socioNegocio.TipoDocumentoIdentidad.trim();
				enrolamiento.IdTipoDocumentoIdentidadAsociado = "";
				enrolamiento.DocumentoIdentidad = socioNegocio.DocumentoIdentidad.trim().replace(".", "");
				enrolamiento.IdTipoDocumentoIdentidadAdicional = "";
				enrolamiento.DocumentoIdentidadAdicional = "";
				enrolamiento.Tipo = socioNegocio.IdTipoPersona.trim();
				enrolamiento.Nombres = socioNegocio.Nombres.trim();
				enrolamiento.ApellidoPaterno = socioNegocio.ApellidoPaterno.trim();
				enrolamiento.ApellidoMaterno = socioNegocio.ApellidoMaterno.trim();
				enrolamiento.RazonSocial = socioNegocio.RazonSocial.trim();
				enrolamiento.IdPaisNacimiento = socioNegocio.IdPaisNacimiento;
				enrolamiento.FechaNacimiento = java.sql.Date.valueOf(socioNegocio.FechaNacimientoConstitucion.trim());
				enrolamiento.Sexo = socioNegocio.IdGenero.trim();
				enrolamiento.EstadoCivil = socioNegocio.IdEstadoCivil.trim();
				enrolamiento.IdProfesion = socioNegocio.IdProfesion;
				enrolamiento.IdPaisNacionalidadPrincipal = socioNegocio.IdPaisNacionalidad;
				enrolamiento.IdSituacionLaboral = socioNegocio.IdSituacionLaboral;
				enrolamiento.IdCiudad = 0L;
				enrolamiento.IdDivisionPais = 0L;
				enrolamiento.DireccionCalle = "";
				enrolamiento.DireccionNumero = "";
				enrolamiento.DireccionDepartamento = "";
				enrolamiento.IdTipoTelefono = "";
				enrolamiento.Telefono = "";
				enrolamiento.Email = "";
				enrolamiento.IdGiro = socioNegocio.IdGiro;
				enrolamiento.IdPaisResidencia = socioNegocio.IdPaisResidencia;
				
				if (!socioNegocio.DireccionFisica.isEmpty()) {
					for (DireccionFisica direccionFisica : socioNegocio.DireccionFisica) {
						if ((socioNegocio.IdTipoPersona.trim().equals("N") && direccionFisica.IdProposito.equals("PART")) || (socioNegocio.IdTipoPersona.trim().equals("J") && direccionFisica.IdProposito.equals("COME"))) {
							enrolamiento.IdCiudad = direccionFisica.IdCiudad;
							enrolamiento.IdDivisionPais = direccionFisica.IdComuna;
							enrolamiento.DireccionCalle = direccionFisica.Calle.trim();
							enrolamiento.DireccionNumero = direccionFisica.Numero.trim();
							enrolamiento.DireccionDepartamento = direccionFisica.Departamento.trim();
							break;
						}
					}
				}

				if (!socioNegocio.Telefono.isEmpty()) {
					for (Telefono telefono : socioNegocio.Telefono) {
						if((socioNegocio.IdTipoPersona.trim().equals("N") && telefono.IdProposito.equals("PART")) || (socioNegocio.IdTipoPersona.trim().equals("J") && telefono.IdProposito.equals("COME"))) {
							enrolamiento.IdTipoTelefono			= telefono.Tipo.trim();
							enrolamiento.Telefono				= telefono.Numero.trim();
							break;
						}
					}
				}
				
				if(!socioNegocio.DireccionElectronica.isEmpty()){

					for(DireccionElectronica direccionElectronica : socioNegocio.DireccionElectronica){
						if (direccionElectronica.IdProposito.isEmpty() || "".equals(direccionElectronica.IdProposito)) {
							mensajesErrores.IdInterno = 0L;
							mensajesErrores.TipoMensaje = "ERROR";
							mensajesErrores.Codigo = "PROPOSITO_VACIO";
							mensajesErrores.Mensaje = "PROPOSITO_VACIO";
							listMensajesErrores.add(mensajesErrores);
							entidadCreada.TipoEntidad = socioNegocio.TipoEntidad.trim();
							entidadCreada.IdEntidad = socioNegocio.IdEntidad;
							entidadCreada.Estado = "";
							entidadCreada.Nuevo = "";
							entidadCreada.Mensajes = listMensajesErrores;
							listEntidadCreada.add(entidadCreada);
							ContadorErrores++;
							break;
						}
						
						if (direccionElectronica.Direccion.isEmpty() || "".equals(direccionElectronica.Direccion)) {
							mensajesErrores.IdInterno = 0L;
							mensajesErrores.TipoMensaje = "ERROR";
							mensajesErrores.Codigo = "DIRECCION_VACIO";
							mensajesErrores.Mensaje = "DIRECCION_VACIO";
							listMensajesErrores.add(mensajesErrores);
							entidadCreada.TipoEntidad = socioNegocio.TipoEntidad.trim();
							entidadCreada.IdEntidad = socioNegocio.IdEntidad;
							entidadCreada.Estado = "";
							entidadCreada.Nuevo = "";
							entidadCreada.Mensajes = listMensajesErrores;
							listEntidadCreada.add(entidadCreada);
							ContadorErrores++;
							break;
						}

						if((socioNegocio.IdTipoPersona.trim().equals("N") && direccionElectronica.IdProposito.equals("PART")) || (socioNegocio.IdTipoPersona.trim().equals("J") && direccionElectronica.IdProposito.equals("COME"))){
							enrolamiento.Email					= direccionElectronica.Direccion.trim();
						}
					}
				}

				enrolamiento.FechaCreacion						= java.sql.Timestamp.valueOf(FechaHoraSistema);
				enrolamiento.FechaCreacionStr					= FechaSistema;
				enrolamiento.FechaModificacion					= java.sql.Timestamp.valueOf(FechaHoraSistema);
				enrolamiento.FechaModificacionStr				= FechaSistema;
				enrolamiento.IdPersonaConyuge					= 0L;
				enrolamiento.RegimenConyugal					= socioNegocio.IdRegimenConyugal.trim();
				
				enrolamiento.CLI_IdCategoriaInversionista		= "";
				enrolamiento.CLI_IdTipoCliente					= "";
				enrolamiento.CLI_IdEmpleadoEjecutivo			= 0L;
				enrolamiento.CLI_CodigoAdicionalHolding			= "";
				enrolamiento.CLI_IdClaseCliente					= "";
				enrolamiento.CLI_ClienteFinal					= "";
				enrolamiento.POR_IdPerfilComercial				= "";
				enrolamiento.POR_IdPersonaBrokerExtranjero		= 0L;
				
				
				if(!socioNegocio.DatosCliente.isEmpty()){
					for(DatosCliente datosCliente : socioNegocio.DatosCliente){
						enrolamiento.CLI_IdCategoriaInversionista		= datosCliente.IdCategoriaInversionista.trim();
						enrolamiento.CLI_IdTipoCliente					= datosCliente.IdTipoCliente.trim();
						enrolamiento.CLI_IdEmpleadoEjecutivo			= datosCliente.CodigoEjecutivo;	
						enrolamiento.POR_IdPerfilComercial				= datosCliente.IdPerfilComercial.trim() != null && !datosCliente.IdPerfilComercial.trim().equals("") ? datosCliente.IdPerfilComercial.trim() : "";
						
						if(StringUtils.isNullOrBlank(datosCliente.NombreSegmentoCliente) == false) {
							SocioNegocioUtils.TipoSegmento tipoSegmento = TipoSegmentoPorNombre.get(datosCliente.NombreSegmentoCliente);
							if (Objects.nonNull(tipoSegmento)) {
								enrolamiento.CLI_IdSegmentoCliente = tipoSegmento.IdSegmentoCliente;
							} else {
								HashMap<String, String> parametrosMensaje = new HashMap<>(2);
								parametrosMensaje.put("NombreSegmentoCliente", datosCliente.NombreSegmentoCliente);
								PlataformaUtils.MensajeSalida ms = new PlataformaUtils.MensajeSalida(IdCliente, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, "UI_SEGMENTO_CLIENTE_INVALIDO", parametrosMensaje, conn, in.p_IdEmpresa);
								MensajesErrores me = new MensajesErrores();
								me.IdInterno = 0L;
								me.Codigo = ms.codigo;
								me.Mensaje = ms.descripcion;
								me.TipoMensaje = ms.tipo;
								listMensajesErrores.add(me);
								entidadCreada.Mensajes = listMensajesErrores;
								listEntidadCreada.add(entidadCreada);
								ContadorErrores++;
							}
						}

						break;
					}
				}
				
				enrolamiento.EMP_CodigoEmpleado					= "";
				enrolamiento.EMP_CodigoTipoCargoEmpleado		= "";
				enrolamiento.EMP_IdEmpleadoSupervisor			= 0L;
				enrolamiento.EMP_IdSucursal						= 0L;
				enrolamiento.EMP_FechaInicioCargo				= java.sql.Date.valueOf("0001-01-01");
				enrolamiento.EMP_FechaTerminoCargo				= java.sql.Date.valueOf("0001-01-01");
				enrolamiento.CNT_CodigoInscripcion				= "";
				enrolamiento.CNT_Emisor							= "";
				enrolamiento.CNT_Depositos						= "";
				enrolamiento.CNT_Bolsa							= "";
				enrolamiento.CNT_Corredora						= "";
				enrolamiento.CNT_CamaraDeCompensacion			= "";
				enrolamiento.CNT_Banco							= ""; 
				enrolamiento.CNT_Boveda							= "";
				enrolamiento.CNT_Otros							= "";
				enrolamiento.PRO_IdAreaProveedor				= 0L; 
				enrolamiento.EMP_CargoIndefinido				= "";
				enrolamiento.IdCarga							= 0L;
				enrolamiento.EsRutInterno						= "N";
				enrolamiento.IdTipoCanalIngresoCorredor					= socioNegocio.IdTipoCanalIngresoCorredor;
				
				Long paisSistema = ConfiguracionSistema.getParametroConfiguracionLong(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.PaisSistema");
				enrolamiento.ResidenciaTributariaExtranjera		= socioNegocio.IdPaisResidencia != null && socioNegocio.IdPaisResidencia != paisSistema ? "S" : "N";
				
				listEnrolamiento.add(enrolamiento);

				// SE CREA LA PERSONA Y EL CLIENTE
				if (socioNegocio.TipoEntidad.trim().equals("PERSONA")) {
					Boolean debeCrearPersona = true;
					Boolean debeModificarPersona = false;
					Boolean modificaTipoPersona = false;
					if(socioNegocio.IdEntidadRelacionada > 0 && !socioNegocio.IdTipoRelacion.trim().isEmpty()) {
						List<PersonaRelacionaExistente> personaRelacionada = personasRelacionadasExistentes.parallelStream().filter(persona->{
							return persona.DocumentoIdentidad.equals(socioNegocio.DocumentoIdentidad) && persona.TipoDocumentoIdentidad.equals(socioNegocio.TipoDocumentoIdentidad);
						}).collect(Collectors.toList());
						if(!personaRelacionada.isEmpty()) {
							debeCrearPersona = false;
							debeModificarPersona = true;
							IdPersona = personaRelacionada.get(0).IdPersona;
							relaciones.IdEntidad = socioNegocio.IdEntidad;
							relaciones.IdPersona = IdPersona;
							relaciones.IdCliente = IdCliente;
							listRelaciones.add(relaciones);

							if ("S".equalsIgnoreCase(agregaAdvertencia)) {
								debeModificarPersona = false;
								salida.P_OUT_ADVERTENCIAS.add(new MensajeSalida(relaciones.IdEntidad,
										MensajeSalida.MENSAJE_ADVERTENCIA, "UI_ADVERTENCIA_PERSONA_RELACIONADA_EXISTE",
										new HashMap<String, String>(0), conn, in.p_IdEmpresa));
								salida.P_OUT_ADVERTENCIASCONTADOR = salida.P_OUT_ADVERTENCIAS.stream().count();

							}

							if (!socioNegocio.IdTipoPersona.equalsIgnoreCase(personaRelacionada.get(0).Tipo))
								modificaTipoPersona = true;
						}
					} else {
						List<PersonaRelacionaExistente> personaNoRelacionada = personasNoRelacionadasExistentes.parallelStream().filter(persona->{
							return persona.DocumentoIdentidad.equals(socioNegocio.DocumentoIdentidad) && persona.TipoDocumentoIdentidad.equals(socioNegocio.TipoDocumentoIdentidad);
						}).collect(Collectors.toList());
						if(!personaNoRelacionada.isEmpty()) {
							debeCrearPersona = false;
							debeModificarPersona = true;
							IdPersona = personaNoRelacionada.get(0).IdPersona;
							relaciones.IdEntidad = socioNegocio.IdEntidad;
							relaciones.IdPersona = IdPersona;
							relaciones.IdCliente = IdCliente;
							listRelaciones.add(relaciones);
							if (!socioNegocio.IdTipoPersona.equalsIgnoreCase(personaNoRelacionada.get(0).Tipo))
								modificaTipoPersona = true;
						}
					}

					if (debeCrearPersona) {
						CrearPersona crearPersona = new CrearPersona();
						CrearPersona.EntradaServicioCrearPersona inPersona = new CrearPersona.EntradaServicioCrearPersona();
						inPersona.p_IdEmpresa = in.p_IdEmpresa;
						inPersona.p_IdUsuario = in.p_IdUsuario;
						inPersona.p_IdModulo = "SOC";
						inPersona.p_ConsultarEnrolamiento = listEnrolamiento;
						inPersona.p_IdTipoCanal = socioNegocio.IdTipoCanalIngresoCorredor;
						CrearPersona.SalidaServicioCrearPersona salidaCrearPersona = crearPersona.logica(conn, inPersona);

						if (salidaCrearPersona.P_OUT_ERRORESCONTADOR > 0) {
							entidadCreada.TipoEntidad = socioNegocio.TipoEntidad.trim();
							entidadCreada.IdEntidad = socioNegocio.IdEntidad;
							entidadCreada.Estado = "";
							entidadCreada.Nuevo = "";

							for (ErrorDetalle errorDetalle : salidaCrearPersona.P_OUT_ERRORESDETALLE) {
								mensajesErrores.IdInterno = 0L;
								mensajesErrores.TipoMensaje = "ERROR";
								mensajesErrores.Codigo = errorDetalle.Codigo;
								mensajesErrores.Mensaje = errorDetalle.Descripcion;

								listMensajesErrores.add(mensajesErrores);
								entidadCreada.Mensajes = listMensajesErrores;
								listEntidadCreada.add(entidadCreada);
								break;
							}

							break;
						} else {
							IdPersona = salidaCrearPersona.P_OUT_REFID;

							relaciones.IdEntidad = socioNegocio.IdEntidad;
							relaciones.IdPersona = IdPersona;
							relaciones.IdCliente = IdCliente;
							listRelaciones.add(relaciones);
						}
					} else if(debeModificarPersona) {
						if(modificaTipoPersona) {
							HashMap<String,String> valores = new HashMap<String,String>();
							MensajeSalida mensaje = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"TIPO_PERSONA_NO_SE_MODIFICA",valores,conn,in.p_IdEmpresa);
							
							mensajesErrores.IdInterno = mensaje.id;
							mensajesErrores.TipoMensaje = mensaje.tipo;
							mensajesErrores.Codigo = mensaje.codigo;
							mensajesErrores.Mensaje = mensaje.descripcion;

							listMensajesErrores.add(mensajesErrores);
							entidadCreada.Mensajes = listMensajesErrores;
							entidadCreada.IdEntidad = socioNegocio.IdEntidad;
							entidadCreada.TipoEntidad = socioNegocio.TipoEntidad;
							entidadCreada.Nuevo = "";
							entidadCreada.Estado = "";

							// Se limpia la lista de entidades creadas y se deja el contador en cero
							ContadorEntidadCreada = 0L;
							listEntidadCreada.clear();

							listEntidadCreada.add(entidadCreada);
							ErroresContador++;
							break;
						}

						List<ModificarSocioNegocio.SocioNegocio> listSocioNegocio = new ArrayList<>();
						ModificarSocioNegocio.SocioNegocio socioNegocioModificar = new ModificarSocioNegocio.SocioNegocio();

						socioNegocioModificar.IdEntidad = socioNegocio.IdEntidad;
						socioNegocioModificar.TipoEntidad = socioNegocio.TipoEntidad;
						socioNegocioModificar.TipoDocumentoIdentidad = socioNegocio.TipoDocumentoIdentidad;
						socioNegocioModificar.DocumentoIdentidad = socioNegocio.DocumentoIdentidad;
						socioNegocioModificar.Nombres = socioNegocio.Nombres;
						socioNegocioModificar.ApellidoPaterno = socioNegocio.ApellidoPaterno;
						socioNegocioModificar.ApellidoMaterno = socioNegocio.ApellidoMaterno;
						socioNegocioModificar.RazonSocial = socioNegocio.RazonSocial;
						socioNegocioModificar.IdGiro = socioNegocio.IdGiro;
						socioNegocioModificar.FechaNacimientoConstitucion = socioNegocio.FechaNacimientoConstitucion;
						socioNegocioModificar.IdEstadoCivil = socioNegocio.IdEstadoCivil;
						socioNegocioModificar.IdPaisNacimiento = socioNegocio.IdPaisNacimiento;
						socioNegocioModificar.IdRegimenConyugal = socioNegocio.IdRegimenConyugal;
						socioNegocioModificar.IdPaisNacionalidad = socioNegocio.IdPaisNacionalidad;
						socioNegocioModificar.IdGenero = socioNegocio.IdGenero;
						socioNegocioModificar.IdTipoPersona = socioNegocio.IdTipoPersona;
						socioNegocioModificar.IdPaisResidencia = socioNegocio.IdPaisResidencia;
						socioNegocioModificar.IdProfesion = socioNegocio.IdProfesion;
						socioNegocioModificar.IdTipoRelacion = socioNegocio.IdTipoRelacion;
						socioNegocioModificar.IdEntidadRelacionada = socioNegocio.IdEntidadRelacionada;
						socioNegocioModificar.IdSituacionLaboral = socioNegocio.IdSituacionLaboral;
						
						for (DireccionFisica direccionFisica : socioNegocio.DireccionFisica) {
							ModificarSocioNegocio.DireccionFisica direccionModificar = new ModificarSocioNegocio.DireccionFisica();
							direccionModificar.IdDireccionFisica = direccionFisica.IdDireccionFisica;
							direccionModificar.Accion = !"".equalsIgnoreCase(direccionFisica.Accion) ? direccionFisica.Accion : (direccionFisica.IdDireccionFisica.compareTo(0L) != 0 ? "MODIFICAR" : "AGREGAR");
							direccionModificar.IdProposito = direccionFisica.IdProposito;
							direccionModificar.Sector = direccionFisica.Sector;
							direccionModificar.CodigoPostal = direccionFisica.CodigoPostal;
							direccionModificar.IdComuna = direccionFisica.IdComuna;
							direccionModificar.Calle = direccionFisica.Calle;
							direccionModificar.Numero = direccionFisica.Numero;
							direccionModificar.Departamento = direccionFisica.Departamento;
							direccionModificar.Complemento = direccionFisica.Complemento;
							direccionModificar.IdCiudad = direccionFisica.IdCiudad;
							socioNegocioModificar.DireccionFisica.add(direccionModificar);
						}

						for (Telefono telefono : socioNegocio.Telefono) {
							ModificarSocioNegocio.Telefono telefonoModificar = new ModificarSocioNegocio.Telefono();
							telefonoModificar.IdTelefono = telefono.IdTelefono;
							telefonoModificar.Accion = !"".equalsIgnoreCase(telefono.Accion) ? telefono.Accion : (telefono.IdTelefono.compareTo(0L) != 0 ? "MODIFICAR" : "AGREGAR");
							telefonoModificar.IdProposito = telefono.IdProposito;
							telefonoModificar.Numero = telefono.Numero;
							telefonoModificar.IdPais = telefono.IdPais;
							telefonoModificar.Tipo = telefono.Tipo;
							telefonoModificar.Anexo = telefono.Anexo;
							socioNegocioModificar.Telefono.add(telefonoModificar);
						}

						for (DireccionElectronica direccionElectronica : socioNegocio.DireccionElectronica) {
							ModificarSocioNegocio.DireccionElectronica direccionElectronicaModificar = new ModificarSocioNegocio.DireccionElectronica();
							direccionElectronicaModificar.IdDireccionElectronica = direccionElectronica.IdDireccionElectronica;
							direccionElectronicaModificar.Accion = !"".equalsIgnoreCase(direccionElectronica.Accion) ? direccionElectronica.Accion : (direccionElectronica.IdDireccionElectronica.compareTo(0L) != 0 ? "MODIFICAR" : "AGREGAR");
							direccionElectronicaModificar.Tipo = direccionElectronica.Tipo;
							direccionElectronicaModificar.IdProposito = direccionElectronica.IdProposito;
							direccionElectronicaModificar.Direccion = direccionElectronica.Direccion;
							socioNegocioModificar.DireccionElectronica.add(direccionElectronicaModificar);
						}

						if ("J".equalsIgnoreCase(socioNegocio.IdTipoPersona)) {
							for (NombreFantasia nombreFantasia : socioNegocio.NombreFantasia) {
								ModificarSocioNegocio.NombreFantasia nombreFantasiaModificar = new ModificarSocioNegocio.NombreFantasia();
								nombreFantasiaModificar.IdNombreFantasia = nombreFantasia.IdNombreFantasia;
								nombreFantasiaModificar.Accion = !"".equalsIgnoreCase(nombreFantasia.Accion) ? nombreFantasia.Accion : (nombreFantasia.IdNombreFantasia.compareTo(0L) != 0 ? "MODIFICAR" : "AGREGAR");
								nombreFantasiaModificar.Nombre = nombreFantasia.Nombre;
								nombreFantasiaModificar.IdEstado = nombreFantasia.IdEstado;
								socioNegocioModificar.NombreFantasia.add(nombreFantasiaModificar);
							}
						}
						
						listSocioNegocio.add(socioNegocioModificar);
						
						ModificarSocioNegocio modificarSocioNegocio = new ModificarSocioNegocio();
						ModificarSocioNegocio_Entrada entradaModificarSocioNegocio = new ModificarSocioNegocio_Entrada();
						entradaModificarSocioNegocio.p_IdEmpresa = in.p_IdEmpresa;
						entradaModificarSocioNegocio.p_IdUsuario = in.p_IdUsuario;
						entradaModificarSocioNegocio.p_ClaveUsuario = in.p_ClaveUsuario;
						entradaModificarSocioNegocio.p_SocioNegocio = listSocioNegocio;
						ModificarSocioNegocio_Salida salidaModificarSocioNegocio = modificarSocioNegocio.logica(conn, entradaModificarSocioNegocio);

						ContadorEntidadModificada = salidaModificarSocioNegocio.p_Out_ContadorEntidadModificada;
						salida.p_Out_ContadorEntidadCreada = salidaModificarSocioNegocio.p_Out_ContadorEntidadModificada;
						salida.P_OUT_ERRORESCONTADOR = salidaModificarSocioNegocio.P_OUT_ERRORESCONTADOR;
					}

				} else if (socioNegocio.TipoEntidad.trim().equals("CLIENTE")) {
					CrearCliente crearCliente = new CrearCliente();
					CrearCliente.EntradaServicioCrearCliente inCliente = new CrearCliente.EntradaServicioCrearCliente();
					inCliente.p_IdEmpresa = in.p_IdEmpresa;
					inCliente.p_IdUsuario = in.p_IdUsuario;
					inCliente.p_IdModulo = "SOC";
					inCliente.p_UsaReglasNegocio = "S";
					inCliente.p_ConsultarEnrolamiento = listEnrolamiento;
					inCliente.p_CodigoFormulario = "N".equals(socioNegocio.IdTipoPersona) ? CODIGO_FORMULARIO_CLIENTE_FULL_NATURAL : CODIGO_FORMULARIO_CLIENTE_FULL_JURIDICA;
					inCliente.p_IdTipoCanal = socioNegocio.IdTipoCanalIngresoCorredor;
					CrearCliente.SalidaServicioCrearCliente salidaCrearCliente = crearCliente.logica(conn, inCliente); 

					if (salidaCrearCliente.P_OUT_ERRORESCONTADOR > 0) {
						entidadCreada.TipoEntidad = socioNegocio.TipoEntidad.trim();
						entidadCreada.IdEntidad = salidaCrearCliente.P_OUT_REFIDCLIENTE;
						entidadCreada.Estado = "";
						entidadCreada.Nuevo = "";

						for (ErrorDetalle errorDetalle : salidaCrearCliente.P_OUT_ERRORESDETALLE) {
							mensajesErrores.IdInterno = 0L;
							mensajesErrores.TipoMensaje = "ERROR";
							mensajesErrores.Codigo = errorDetalle.Codigo;
							mensajesErrores.Mensaje = errorDetalle.Descripcion;

							listMensajesErrores.add(mensajesErrores);
							entidadCreada.Mensajes = listMensajesErrores;
							listEntidadCreada.add(entidadCreada);
							break;
						}

						break;
					} else {
						HashMap<String, Object> parametroEntrada = new HashMap<String, Object>();
						parametroEntrada.put("p_IdCliente", salidaCrearCliente.P_OUT_REFIDCLIENTE);
						logger.debug("INICIO::SQL_SELECT_IDPERSONA");
						List<SelectIdPersona> SelectIdPersona = sql.query(conn, SQL_SELECT_IDPERSONA, parametroEntrada, SelectIdPersona.class);
						logger.debug("FIN::SQL_SELECT_IDPERSONA");
						IdPersona = SelectIdPersona.get(0).IdPersona;
						IdCliente = salidaCrearCliente.P_OUT_REFIDCLIENTE;

						relaciones.IdEntidad = socioNegocio.IdEntidad;
						relaciones.IdPersona = IdPersona;
						relaciones.IdCliente = IdCliente;
						listRelaciones.add(relaciones);
					}
				} else {
					entidadCreada.TipoEntidad = socioNegocio.TipoEntidad.trim();
					entidadCreada.IdEntidad = socioNegocio.IdEntidad;
					entidadCreada.Estado = "";
					entidadCreada.Nuevo = "";

					mensajesErrores.IdInterno = 0L;
					mensajesErrores.TipoMensaje = "ERROR";
					mensajesErrores.Codigo = "NO_INGRESO_PERSONA";
					mensajesErrores.Mensaje = "NO_INGRESO_PERSONA";

					listMensajesErrores.add(mensajesErrores);
					entidadCreada.Mensajes = listMensajesErrores;
					listEntidadCreada.add(entidadCreada);
					ContadorErrores++;
					break;
				}

				// SE CREAN LAS RELACIONES
				if(socioNegocio.IdEntidadRelacionada > 0 && !socioNegocio.IdTipoRelacion.trim().isEmpty()){
					for(Relaciones lista : listRelaciones) {
						if(lista.IdEntidad == socioNegocio.IdEntidadRelacionada){
							if(socioNegocio.IdTipoRelacion.trim().equals("CY") || socioNegocio.IdTipoRelacion.trim().equals("REP") || socioNegocio.IdTipoRelacion.trim().equals("PEMDOR")){
		    					List<UpdateRelacionPersona> listUpdateRelacionPersona = new ArrayList<UpdateRelacionPersona>();
		    					UpdateRelacionPersona updateRelacionPersona = new UpdateRelacionPersona();
		    					
    							if(socioNegocio.IdTipoRelacion.trim().equals("CY")){
    								UpdateRelacionPersona updateRelacionPersonaConyuge = new UpdateRelacionPersona();
    								updateRelacionPersonaConyuge.p_IdPersonaConyuge = lista.IdPersona;
    								updateRelacionPersonaConyuge.p_IdPersona = IdPersona;
    								List<UpdateRelacionPersona> listUpdateRelacionPersonaConyuge = new ArrayList<UpdateRelacionPersona>();
    								listUpdateRelacionPersonaConyuge.add(updateRelacionPersonaConyuge);
    								logger.debug("INICIO::SQL_UPDATE_RELACION_PERSONA");
    								sql.batch(conn, SQL_UPDATE_RELACION_PERSONA, listUpdateRelacionPersonaConyuge, UpdateRelacionPersona.class);
    								logger.debug("FIN::SQL_UPDATE_RELACION_PERSONA");
    								updateRelacionPersona.p_IdPersonaConyuge = IdPersona;
    							} else if(socioNegocio.IdTipoRelacion.trim().equals("REP")){
    								updateRelacionPersona.p_IdPersonaEmpleador = IdPersona;	
    							} else if(socioNegocio.IdTipoRelacion.trim().equals("PEMDOR")){
    								updateRelacionPersona.p_IdPersonaRepresentante = IdPersona;
    							}
		    					
		    					updateRelacionPersona.p_IdPersona = lista.IdPersona;
		    					listUpdateRelacionPersona.add(updateRelacionPersona);
		    					logger.debug("INICIO::SQL_UPDATE_RELACION_PERSONA");
								sql.batch(conn, SQL_UPDATE_RELACION_PERSONA, listUpdateRelacionPersona, UpdateRelacionPersona.class);
								logger.debug("FIN::SQL_UPDATE_RELACION_PERSONA");
							} else {
								EntradaCrearRelacionPersona entradaCrearRelacionPersona = new EntradaCrearRelacionPersona();
								entradaCrearRelacionPersona.p_IdEmpresa = in.p_IdEmpresa;
								entradaCrearRelacionPersona.p_IdUsuario = in.p_IdUsuario;
								entradaCrearRelacionPersona.p_IdModulo = "SOC";
								entradaCrearRelacionPersona.p_UsaReglasNegocio = "N";
								entradaCrearRelacionPersona.p_IdTipoRelacion = socioNegocio.IdTipoRelacion.trim();
								entradaCrearRelacionPersona.p_IdPersonaOrigen = lista.IdPersona;
								entradaCrearRelacionPersona.p_IdPersonaDestino = IdPersona;
								entradaCrearRelacionPersona.p_IdEstado = "ACT";
								entradaCrearRelacionPersona.p_FechaInicioRelacion = FechaHoraSistema;
								entradaCrearRelacionPersona.p_FechaFinRelacion = "0001-01-01 00:00:00";
								entradaCrearRelacionPersona.p_PorcentajeParticipacion = BigDecimal.ZERO;
								entradaCrearRelacionPersona.p_RetiraDocumentos = "";
								entradaCrearRelacionPersona.p_FinalidadRelacion = "";
								entradaCrearRelacionPersona.p_CondicionesRelacion = "";
								entradaCrearRelacionPersona.p_IdGradoVinculacionParental = 0L;
								entradaCrearRelacionPersona.p_Ciudad = "";
								entradaCrearRelacionPersona.p_Calle = "";
								entradaCrearRelacionPersona.p_Numero = "";
								entradaCrearRelacionPersona.p_Telefono = "";
								entradaCrearRelacionPersona.p_Email = "";
								entradaCrearRelacionPersona.p_Complemento = "";
								CrearRelacionPersona crearRelacionPersona = new CrearRelacionPersona();
								SalidaCrearRelacionPersona salidaCrearRelacionPersona = crearRelacionPersona.logica(conn, entradaCrearRelacionPersona);
								
								if(salidaCrearRelacionPersona.P_OUT_ERRORESCONTADOR > 0) {
									entidadCreada.TipoEntidad = socioNegocio.TipoEntidad.trim();
									entidadCreada.IdEntidad = socioNegocio.IdEntidad;
									entidadCreada.Estado = "";
									entidadCreada.Nuevo = "";

									for (MensajeSalida mensaje : salidaCrearRelacionPersona.P_OUT_MENSAJES) {
										if (MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)) {
											mensajesErrores.IdInterno = 0L;
											mensajesErrores.TipoMensaje = "ERROR";
											mensajesErrores.Codigo = mensaje.codigo;
											mensajesErrores.Mensaje = mensaje.descripcion;

											listMensajesErrores.add(mensajesErrores);
											entidadCreada.Mensajes = listMensajesErrores;
											listEntidadCreada.add(entidadCreada);
											ContadorErrores++;
											break;
										}
										
										if(MensajeSalida.MENSAJE_ADVERTENCIA.equals(mensaje.tipo) || MensajeSalida.MENSAJE_AUTORIZACION.equals(mensaje.tipo)) {
											mensajesErrores.IdInterno = 0L;
											mensajesErrores.TipoMensaje = "ADVERTENCIA";
											mensajesErrores.Codigo = mensaje.codigo;
											mensajesErrores.Mensaje = mensaje.descripcion;

											listMensajesErrores.add(mensajesErrores);
											entidadCreada.Mensajes = listMensajesErrores;
											listEntidadCreada.add(entidadCreada);
											ContadorErrores++;
											break;
										}

									}

									break;
								}
							}
						}
					}
					
					if(ContadorErrores > 0) break;
				}

				// SE CREA LOS INDICADORES DE PERSONA
				if (!socioNegocio.IndicadorPersona.isEmpty()) {
					List<IndicadorPersonaCliente> listaIndicadorPersonaCliente = new ArrayList<IndicadorPersonaCliente>();

					List<String> codigoIndicadores = socioNegocio.IndicadorPersona.parallelStream()
							.map(indicador -> indicador.CodigoTipoIndicador).distinct().collect(Collectors.toList());
					HashMap<String, Object> parametroEntrada = new HashMap<String, Object>(2);
					parametroEntrada.put("IdPersona", IdPersona);
					parametroEntrada.put("CodigoTipoIndicador", codigoIndicadores);
					logger.debug("INICIO::SQL_SELECT_INDICADORES");
					List<SelectConsultarIndicadores> selectConsultarIndicadores = sql.query(conn, SQL_SELECT_INDICADORES_PERSONA, parametroEntrada, SelectConsultarIndicadores.class);
					Map<String,SelectConsultarIndicadores> indicadoresPersona = selectConsultarIndicadores.parallelStream().collect(Collectors.toMap(indicador->indicador.CodigoTipoIndicador, indicador->indicador));
					logger.debug("INICIO::SQL_SELECT_INDICADORES");

					listaIndicadorPersonaCliente = socioNegocio.IndicadorPersona.parallelStream()
							.filter(indicador -> indicadoresPersona.get(indicador.CodigoTipoIndicador) != null)
							.map(indicador -> {
								SelectConsultarIndicadores indicadorPersona = indicadoresPersona.get(indicador.CodigoTipoIndicador);
								IndicadorPersonaCliente nuevoIndicadorPersona = new IndicadorPersonaCliente();
								nuevoIndicadorPersona.IdIndicador = indicadorPersona.IdIndicador;
								nuevoIndicadorPersona.IdTipoIndicador = indicadorPersona.IdTipoIndicador;
								nuevoIndicadorPersona.ValorIndicador = indicador.ValorIndicador;
								nuevoIndicadorPersona.FechaVencimiento = Fecha.toDate("0001-01-01 00:00:00");
								return nuevoIndicadorPersona;
							}).collect(Collectors.toList());

					ModificarIndicador.SalidaModificarIndicador resultadoModificarIndicador = modificarIndicador(conn, in, "PERSONA", IdPersona, listaIndicadorPersonaCliente);

					if (resultadoModificarIndicador.P_OUT_ERRORESCONTADOR > 0) {
						entidadCreada.TipoEntidad = socioNegocio.TipoEntidad.trim();
						entidadCreada.IdEntidad = socioNegocio.IdEntidad;
						entidadCreada.Estado = "";
						entidadCreada.Nuevo = "";
						for (MensajeSalida mensaje : resultadoModificarIndicador.P_OUT_MENSAJES) {
							if (MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)) {
								mensajesErrores.IdInterno = 0L;
								mensajesErrores.TipoMensaje = "ERROR";
								mensajesErrores.Codigo = mensaje.codigo;
								mensajesErrores.Mensaje = mensaje.descripcion;

								listMensajesErrores.add(mensajesErrores);
								entidadCreada.Mensajes = listMensajesErrores;
								listEntidadCreada.add(entidadCreada);
								ContadorErrores++;
								break;
							}
							
							if(MensajeSalida.MENSAJE_ADVERTENCIA.equals(mensaje.tipo) || MensajeSalida.MENSAJE_AUTORIZACION.equals(mensaje.tipo)) {
								mensajesErrores.IdInterno = 0L;
								mensajesErrores.TipoMensaje = "ADVERTENCIA";
								mensajesErrores.Codigo = mensaje.codigo;
								mensajesErrores.Mensaje = mensaje.descripcion;

								listMensajesErrores.add(mensajesErrores);
								entidadCreada.Mensajes = listMensajesErrores;
								listEntidadCreada.add(entidadCreada);
								ContadorErrores++;
								break;
							}

						}

						break;
					}
				}

				// SE CREA LOS INDICADORES DE CLIENTE
				if (socioNegocio.TipoEntidad.trim().equals("CLIENTE")) {
					if (!socioNegocio.IndicadorCliente.isEmpty()) {
						List<IndicadorPersonaCliente> listaIndicadorPersonaCliente = new ArrayList<IndicadorPersonaCliente>();
						
						List<String> codigosTipoIndicadores = socioNegocio.IndicadorCliente.parallelStream().map(indicador->indicador.CodigoTipoIndicador).distinct().collect(Collectors.toList());
						HashMap<String,Object> parametroEntrada = new HashMap<String,Object>(2);
						parametroEntrada.put("IdCliente", IdCliente);
						parametroEntrada.put("CodigoTipoIndicador", codigosTipoIndicadores);
						logger.debug("INICIO::SQL_SELECT_INDICADORES");
						List<SelectConsultarIndicadores> SelectConsultarIndicadores = sql.query(conn, SQL_SELECT_INDICADORES_CLIENTE, parametroEntrada, SelectConsultarIndicadores.class);
						Map<String, SelectConsultarIndicadores> indicadoresCliente = SelectConsultarIndicadores.parallelStream().collect(Collectors.toMap(indicador -> indicador.CodigoTipoIndicador, indicador -> indicador));
						logger.debug("FIN::SQL_SELECT_INDICADORES");

						listaIndicadorPersonaCliente = socioNegocio.IndicadorCliente.parallelStream()
								.filter(indicador -> indicadoresCliente.get(indicador.CodigoTipoIndicador) != null)
								.map(indicador -> {
									SelectConsultarIndicadores indicadorCliente = indicadoresCliente.get(indicador.CodigoTipoIndicador);
									IndicadorPersonaCliente nuevoIndicadorCliente = new IndicadorPersonaCliente();
									nuevoIndicadorCliente.IdIndicador = indicadorCliente.IdIndicador;
									nuevoIndicadorCliente.IdTipoIndicador = indicadorCliente.IdTipoIndicador;
									nuevoIndicadorCliente.ValorIndicador = indicador.ValorIndicador;
									nuevoIndicadorCliente.FechaVencimiento = Fecha.toDate("0001-01-01 00:00:00");
									return nuevoIndicadorCliente;
								}).collect(Collectors.toList());

						ModificarIndicador.SalidaModificarIndicador resultadoModificarIndicador = modificarIndicador( conn, in, "CLIENTE", IdCliente, listaIndicadorPersonaCliente);

						if (resultadoModificarIndicador.P_OUT_ERRORESCONTADOR > 0) {
							entidadCreada.TipoEntidad = socioNegocio.TipoEntidad.trim();
							entidadCreada.IdEntidad = socioNegocio.IdEntidad;
							entidadCreada.Estado = "";
							entidadCreada.Nuevo = "";

							for (MensajeSalida mensaje : resultadoModificarIndicador.P_OUT_MENSAJES) {
								if (MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)) {
									mensajesErrores.IdInterno = 0L;
									mensajesErrores.TipoMensaje = "ERROR";
									mensajesErrores.Codigo = mensaje.codigo;
									mensajesErrores.Mensaje = mensaje.descripcion;

									listMensajesErrores.add(mensajesErrores);
									entidadCreada.Mensajes = listMensajesErrores;
									listEntidadCreada.add(entidadCreada);
									ContadorErrores++;
									break;
								}
								
								if(MensajeSalida.MENSAJE_ADVERTENCIA.equals(mensaje.tipo) || MensajeSalida.MENSAJE_AUTORIZACION.equals(mensaje.tipo)) {
									mensajesErrores.IdInterno = 0L;
									mensajesErrores.TipoMensaje = "ADVERTENCIA";
									mensajesErrores.Codigo = mensaje.codigo;
									mensajesErrores.Mensaje = mensaje.descripcion;

									listMensajesErrores.add(mensajesErrores);
									entidadCreada.Mensajes = listMensajesErrores;
									listEntidadCreada.add(entidadCreada);
									ContadorErrores++;
									break;
								}

							}

							break;
						}
					}

					// SE CREA LA DECLARACION DE ORIGEN DE FONDOS
					if (!socioNegocio.DeclaracionOrigenFondo.isEmpty()) {

						Map<String, List<DeclaracionOrigenFondo>> declaracionesOrigenFondoAgrupadas = socioNegocio.DeclaracionOrigenFondo.parallelStream().collect(Collectors.groupingBy(declaracion -> declaracion.FechaDeclaracion));

						for (List<DeclaracionOrigenFondo> declaracionOrigenFondoAgrupada : declaracionesOrigenFondoAgrupadas.values()) {

							EntradaCrearDeclaracionOrigenFondos entradaCrearDeclaracionOrigenFondos = new EntradaCrearDeclaracionOrigenFondos();
							for (DeclaracionOrigenFondo declaracionOrigenFondo : declaracionOrigenFondoAgrupada) {
								for (DetalleOrigenFondo origenFondo : declaracionOrigenFondo.DetalleOrigenFondo) {
									CrearDeclaracionOrigenFondos.Detalle detalleOrigenFondo = new CrearDeclaracionOrigenFondos.Detalle();
									detalleOrigenFondo.IdTipoOrigenFondo = origenFondo.IdTipoOrigenFondo;
									detalleOrigenFondo.Valor = new BigDecimal(origenFondo.Valor);
									detalleOrigenFondo.Otros = origenFondo.Otros;
									entradaCrearDeclaracionOrigenFondos.p_Detalle.add(detalleOrigenFondo);
								}
							}

							DeclaracionOrigenFondo declaracionOrigenFondo = declaracionOrigenFondoAgrupada.get(0);
							entradaCrearDeclaracionOrigenFondos.p_IdEmpresa = in.p_IdEmpresa;
							entradaCrearDeclaracionOrigenFondos.p_IdUsuario = in.p_IdUsuario;
							entradaCrearDeclaracionOrigenFondos.p_IdModulo = "SOC";
							entradaCrearDeclaracionOrigenFondos.p_IdCliente = IdCliente;
							entradaCrearDeclaracionOrigenFondos.p_IdTipoOperacionOrigenFondos = declaracionOrigenFondo.IdTipoOperacionOrigenFondos;
							entradaCrearDeclaracionOrigenFondos.p_FechaDeclaracion = declaracionOrigenFondo.FechaDeclaracion;
							entradaCrearDeclaracionOrigenFondos.p_Comentario = declaracionOrigenFondo.Comentario;
							entradaCrearDeclaracionOrigenFondos.p_IdEstado = declaracionOrigenFondo.IdEstado;
							entradaCrearDeclaracionOrigenFondos.p_OperacionInscripcion = declaracionOrigenFondo.OperacionInscripcion;

							CrearDeclaracionOrigenFondos crearDeclaracionOrigenFondos = new CrearDeclaracionOrigenFondos();
							SalidaCrearDeclaracionOrigenFondos salidaCrearDeclaracionOrigenFondos = crearDeclaracionOrigenFondos.logica(conn, entradaCrearDeclaracionOrigenFondos);
			 				
							if (salidaCrearDeclaracionOrigenFondos.P_OUT_ERRORESCONTADOR > 0) {
								entidadCreada.TipoEntidad = socioNegocio.TipoEntidad.trim();
								entidadCreada.IdEntidad = socioNegocio.IdEntidad;
								entidadCreada.Estado = "";
								entidadCreada.Nuevo = "";

								for (MensajeSalida mensaje : salidaCrearDeclaracionOrigenFondos.P_OUT_MENSAJES) {
									if (MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)) {
										mensajesErrores.IdInterno = 0L;
										mensajesErrores.TipoMensaje = "ERROR";
										mensajesErrores.Codigo = mensaje.codigo;
										mensajesErrores.Mensaje = mensaje.descripcion;

										listMensajesErrores.add(mensajesErrores);
										entidadCreada.Mensajes = listMensajesErrores;
										listEntidadCreada.add(entidadCreada);
										ContadorErrores++;
										break;
									}
									
									if(MensajeSalida.MENSAJE_ADVERTENCIA.equals(mensaje.tipo) || MensajeSalida.MENSAJE_AUTORIZACION.equals(mensaje.tipo)) {
										mensajesErrores.IdInterno = 0L;
										mensajesErrores.TipoMensaje = "ADVERTENCIA";
										mensajesErrores.Codigo = mensaje.codigo;
										mensajesErrores.Mensaje = mensaje.descripcion;

										listMensajesErrores.add(mensajesErrores);
										entidadCreada.Mensajes = listMensajesErrores;
										listEntidadCreada.add(entidadCreada);
										ContadorErrores++;
										break;
									}
								}

								break;
							}
						}
						if(ContadorErrores > 0) break;	
					}
				}
				
				// SE CREA LOS OTROS TIPOS DE DOCUMENTOS
				if (!socioNegocio.TiposDocumento.isEmpty()) {
					for (TiposDocumento tiposDocumento : socioNegocio.TiposDocumento) {
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
						
						if (salidaCrearDocumentoIdentidad.P_OUT_ERRORESCONTADOR > 0) {
							entidadCreada.TipoEntidad = socioNegocio.TipoEntidad.trim();
							entidadCreada.IdEntidad = socioNegocio.IdEntidad;
							entidadCreada.Estado = "";
							entidadCreada.Nuevo = "";

							for (ErrorDetalle errorDetalle : salidaCrearDocumentoIdentidad.P_OUT_ERRORESDETALLE) {
								mensajesErrores.IdInterno = 0L;
								mensajesErrores.TipoMensaje = "ERROR";
								mensajesErrores.Codigo = errorDetalle.Codigo;
								mensajesErrores.Mensaje = errorDetalle.Descripcion;
								
								listMensajesErrores.add(mensajesErrores);
								entidadCreada.Mensajes = listMensajesErrores;
								listEntidadCreada.add(entidadCreada);
								ContadorErrores++;
								break;
							}
							
							break;
						}
					}
					
					if(ContadorErrores > 0) break;
				}
				
				// SE CREA LAS OTRAS DIRECCIONES FISICAS
				if (!socioNegocio.DireccionFisica.isEmpty() && ContadorEntidadModificada.compareTo(0L) == 0 && debeModificarDatos(agregaAdvertencia, socioNegocio.IdTipoRelacion) ) {
					List<CrearDireccionFisicaMasivo.DireccionFisica> p_inDireccionesFisicas = new ArrayList<CrearDireccionFisicaMasivo.DireccionFisica>();
					
					List<DireccionFisica> direccionesExistentes = obtenerDireccionesFisicas(conn, IdPersona);
					
					for(DireccionFisica dirF : socioNegocio.DireccionFisica){
						if(direccionFisicaNoExiste( dirF,  direccionesExistentes) && !("ELIMINAR").equals(dirF.Accion)) {
							CrearDireccionFisicaMasivo.DireccionFisica dirFis = new CrearDireccionFisicaMasivo.DireccionFisica();
							dirFis.Calle = dirF.Calle;
							dirFis.Numero = dirF.Numero;
							dirFis.Departamento = dirF.Departamento;
							dirFis.Sector = dirF.Sector;
							dirFis.CodigoPostal = dirF.CodigoPostal;
							dirFis.IdCiudad = dirF.IdCiudad;
							dirFis.IdDivisionPais = dirF.IdComuna;
							dirFis.CodigoProposito = dirF.IdProposito;
							dirFis.Comentario = dirF.Complemento;
							dirFis.IdEstado = "ACT";
							dirFis.IdPersona = IdPersona;
							dirFis.IdUsuarioCreador = in.p_IdUsuario;
							p_inDireccionesFisicas.add(dirFis);
						}
					}
					
					SalidaCrearDireccionFisicaMasivo salidaCrearDireccionFisicaMasivo = new SalidaCrearDireccionFisicaMasivo();
					
					if(!p_inDireccionesFisicas.isEmpty()) {
						salidaCrearDireccionFisicaMasivo = crearDireccionesFisicas(conn,p_inDireccionesFisicas, in.p_IdEmpresa, in.p_IdUsuario);						
					}

					if (salidaCrearDireccionFisicaMasivo.P_OUT_ERRORESCONTADOR > 0) {
						entidadCreada.TipoEntidad = socioNegocio.TipoEntidad.trim();
						entidadCreada.IdEntidad = socioNegocio.IdEntidad;
						entidadCreada.Estado = "";
						entidadCreada.Nuevo = "";

						listMensajesErrores.addAll( salidaCrearDireccionFisicaMasivo.P_OUT_MENSAJES.stream().filter(mensaje -> PlataformaUtils.MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)).map(error ->{
							MensajesErrores errores = new MensajesErrores();
							errores.IdInterno = error.id;
							errores.Codigo = error.codigo;
							errores.TipoMensaje = error.tipo;
							errores.Mensaje = error.descripcion;
							return errores;
						}).collect(Collectors.toList()) );
						entidadCreada.Mensajes = listMensajesErrores;
						listEntidadCreada.add(entidadCreada);
						ContadorErrores++;
					}
					
					if (ContadorErrores > 0)
						break;
				}

				// SE CREA LOS OTROS TELEFONOS
				if (!socioNegocio.Telefono.isEmpty() && ContadorEntidadModificada.compareTo(0L) == 0 && debeModificarDatos(agregaAdvertencia, socioNegocio.IdTipoRelacion)) {
					List<CrearTelefonoMasivo.Telefono> p_inTelefonos= new ArrayList<CrearTelefonoMasivo.Telefono>();
					
					List<Telefono> telefonosExistentes = obtenerTelefonos(conn, IdPersona);
					
					for (Telefono telefono : socioNegocio.Telefono) {
						if(telefonoNoExiste( telefono,  telefonosExistentes) && !("ELIMINAR").equals(telefono.Accion)) {
							CrearTelefonoMasivo.Telefono telef = new CrearTelefonoMasivo.Telefono();
							telef.IdPais = telefono.IdPais;
							telef.IdTipoTelefono = telefono.Tipo;
							telef.Numero = telefono.Numero;
							telef.Anexo = "";
							telef.CodigoProposito = telefono.IdProposito;
							telef.Comentario = "";
							telef.IdEstado = "ACT";
							telef.IdPersona = IdPersona;
							telef.Predeterminado = "N";
							telef.IdUsuarioCreador = in.p_IdUsuario;
							
							p_inTelefonos.add(telef);
						}
					}
					
					SalidaCrearTelefonoMasivo salidaCrearTelefonoMasivo = new SalidaCrearTelefonoMasivo();
					
					if(!p_inTelefonos.isEmpty()) {
						salidaCrearTelefonoMasivo = crearTelefonos(conn,p_inTelefonos, in.p_IdEmpresa, in.p_IdUsuario);
					}

					if (salidaCrearTelefonoMasivo.P_OUT_ERRORESCONTADOR > 0) {
						entidadCreada.TipoEntidad = socioNegocio.TipoEntidad.trim();
						entidadCreada.IdEntidad = socioNegocio.IdEntidad;
						entidadCreada.Estado = "";
						entidadCreada.Nuevo = "";

						listMensajesErrores.addAll( salidaCrearTelefonoMasivo.P_OUT_MENSAJES.stream().filter(mensaje -> PlataformaUtils.MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)).map(error ->{
							MensajesErrores errores = new MensajesErrores();
							errores.IdInterno = error.id;
							errores.Codigo = error.codigo;
							errores.TipoMensaje = error.tipo;
							errores.Mensaje = error.descripcion;
							return errores;
						}).collect(Collectors.toList()) );
						entidadCreada.Mensajes = listMensajesErrores;
						listEntidadCreada.add(entidadCreada);
						ContadorErrores++;
					}
					
					if(ContadorErrores > 0) break;
				}
				
				// SE CREA LAS OTRAS DIRECIONES ELECTRONICAS
				if (!socioNegocio.DireccionElectronica.isEmpty() && ContadorEntidadModificada.compareTo(0L) == 0 && debeModificarDatos(agregaAdvertencia, socioNegocio.IdTipoRelacion)) {
					List<CrearDireccionElectronicaMasivo.DireccionElectronica> p_inDireccionesElectronicas = new ArrayList<CrearDireccionElectronicaMasivo.DireccionElectronica>();
					
					List<DireccionElectronica> direccionesElectronicasExistentes = obtenerDireccionesElectronicas(conn, IdPersona);
					
					for (DireccionElectronica direccionElectronica : socioNegocio.DireccionElectronica) {
						if (direccionElectronicaNoExiste(direccionElectronica, direccionesElectronicasExistentes) && !("ELIMINAR").equals(direccionElectronica.Accion)) {
							CrearDireccionElectronicaMasivo.DireccionElectronica dirEle = new CrearDireccionElectronicaMasivo.DireccionElectronica();
							dirEle.Direccion = direccionElectronica.Direccion;
							dirEle.CodigoProposito = direccionElectronica.IdProposito;
							dirEle.Comentario = "";
							dirEle.IdEstado = "ACT";
							dirEle.IdPersona = IdPersona;
							dirEle.Predeterminado = "N";
							dirEle.IdUsuarioCreador = in.p_IdUsuario;
							
							p_inDireccionesElectronicas.add(dirEle);
						}
					}
					
					SalidaCrearDireccionElectronicaMasivo salidaCrearDireccionElectronicaMasivo = new SalidaCrearDireccionElectronicaMasivo();

					if(!p_inDireccionesElectronicas.isEmpty()) {
						salidaCrearDireccionElectronicaMasivo = crearDireccionesElectronicas(conn, p_inDireccionesElectronicas,in.p_IdEmpresa, in.p_IdUsuario);		
					}
					
					if (salidaCrearDireccionElectronicaMasivo.P_OUT_ERRORESCONTADOR > 0) {
						entidadCreada.TipoEntidad = socioNegocio.TipoEntidad.trim();
						entidadCreada.IdEntidad = socioNegocio.IdEntidad;
						entidadCreada.Estado = "";
						entidadCreada.Nuevo = "";

						listMensajesErrores.addAll( salidaCrearDireccionElectronicaMasivo.P_OUT_MENSAJES.stream().filter(mensaje -> PlataformaUtils.MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)).map(error ->{
							MensajesErrores errores = new MensajesErrores();
							errores.IdInterno = error.id;
							errores.Codigo = error.codigo;
							errores.TipoMensaje = error.tipo;
							errores.Mensaje = error.descripcion;
							return errores;
						}).collect(Collectors.toList()) );
						entidadCreada.Mensajes = listMensajesErrores;
						listEntidadCreada.add(entidadCreada);
						ContadorErrores++;
					}
					
					if(ContadorErrores > 0) break;
				}
				
				// SE CREA LAS CUENTAS BANCARIAS
				if (!socioNegocio.CuentaBancaria.isEmpty()) {
					EntradaCrearPersonaCuentaBancaria entradaCrearPersonaCuentaBancaria = new EntradaCrearPersonaCuentaBancaria();
					entradaCrearPersonaCuentaBancaria.p_IdEmpresa = in.p_IdEmpresa;
					entradaCrearPersonaCuentaBancaria.p_IdUsuario = in.p_IdUsuario;
					entradaCrearPersonaCuentaBancaria.p_IdModulo = "SOC";
					entradaCrearPersonaCuentaBancaria.p_UsaReglasNegocio = "N";
					AtomicLong index = new AtomicLong(1L);
					Long idPersonaCuentaBancaria = IdPersona;
					Long idClienteCuentaBancaria = IdCliente;
					entradaCrearPersonaCuentaBancaria.p_InCuentaBancaria = socioNegocio.CuentaBancaria.parallelStream().map(cuentaBancaria->{
						CrearPersonaCuentaBancaria.InCuentaBancaria cuentaBancariaACrear = new CrearPersonaCuentaBancaria.InCuentaBancaria();
						cuentaBancariaACrear.IdInterno = index.getAndIncrement();
						cuentaBancariaACrear.IdBanco = cuentaBancaria.IdBanco;
						cuentaBancariaACrear.IdTipoCuentaBancaria = cuentaBancaria.CodigoTipoCuentaBancaria;
						cuentaBancariaACrear.IdMoneda = cuentaBancaria.IdMoneda;
						cuentaBancariaACrear.NumeroCuentaBancaria = cuentaBancaria.NumeroCuentaBancaria;
						cuentaBancariaACrear.IdPersona = idPersonaCuentaBancaria;
						if(cuentaBancaria.IdEstado.equals("VIG") || cuentaBancaria.IdEstado.equals("")) {
							cuentaBancariaACrear.IdEstado = "ACT";
						}
						else{
							cuentaBancariaACrear.IdEstado = cuentaBancaria.IdEstado;
						}
						cuentaBancariaACrear.IdPlaza = cuentaBancaria.CodigoPlaza;
						cuentaBancariaACrear.CrearPreferenciaPago = "N";
						cuentaBancariaACrear.IdCliente = idClienteCuentaBancaria;
						cuentaBancariaACrear.EsPac = "N";
						cuentaBancariaACrear.IdTipoIdentificadorCuenta = "";
						cuentaBancariaACrear.IdentificadorCuenta = "";
						cuentaBancariaACrear.IdPersonaRelacionada = 0L;
						cuentaBancariaACrear.CodigoCuentaInterbancario = "";
						cuentaBancariaACrear.CodigoAba = "";
						cuentaBancariaACrear.OtroCodigoBancoExtranjero = "";
						cuentaBancariaACrear.CodigoIban = "";
						cuentaBancariaACrear.OtroCodigoCuentaExtranjero = "";
						return cuentaBancariaACrear;
					}).collect(Collectors.toList());
					
					CrearPersonaCuentaBancaria crearPersonaCuentaBancaria = new CrearPersonaCuentaBancaria();
					SalidaCrearPersonaCuentaBancaria salidaCrearPersonaCuentaBancaria = crearPersonaCuentaBancaria.logica(conn, entradaCrearPersonaCuentaBancaria);
					
					if(salidaCrearPersonaCuentaBancaria.P_OUT_ERRORESCONTADOR > 0) {
						entidadCreada.TipoEntidad = socioNegocio.TipoEntidad.trim();
						entidadCreada.IdEntidad = socioNegocio.IdEntidad;
						entidadCreada.Estado = "";
						entidadCreada.Nuevo = "";
						
						for(MensajeSalida mensaje : salidaCrearPersonaCuentaBancaria.P_OUT_MENSAJES) {
							if(MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)) {
								mensajesErrores.IdInterno = 0L;
								mensajesErrores.TipoMensaje = "ERROR";
								mensajesErrores.Codigo = mensaje.codigo;
								mensajesErrores.Mensaje = mensaje.descripcion;

								listMensajesErrores.add(mensajesErrores);
								entidadCreada.Mensajes = listMensajesErrores;
								listEntidadCreada.add(entidadCreada);
								ContadorErrores++;
								break;
							}
							
							if(MensajeSalida.MENSAJE_ADVERTENCIA.equals(mensaje.tipo) || MensajeSalida.MENSAJE_AUTORIZACION.equals(mensaje.tipo)) {
								mensajesErrores.IdInterno = 0L;
								mensajesErrores.TipoMensaje = "ADVERTENCIA";
								mensajesErrores.Codigo = mensaje.codigo;
								mensajesErrores.Mensaje = mensaje.descripcion;
								
								listMensajesErrores.add(mensajesErrores);
								entidadCreada.Mensajes = listMensajesErrores;
								listEntidadCreada.add(entidadCreada);
								ContadorErrores++;
								break;
							}
						}
					}
					
					if(ContadorErrores > 0) break;
					
				}
				
				// SE CREA LAS TARJETAS BANCARIAS
				if (!socioNegocio.TarjetasBancaria.isEmpty()) {
					for (TarjetasBancaria tarjetasBancaria : socioNegocio.TarjetasBancaria) {
						EntradaCrearPersonaTarjetaBancaria entradaCrearPersonaTarjetaBancaria = new EntradaCrearPersonaTarjetaBancaria();
						entradaCrearPersonaTarjetaBancaria.p_IdEmpresa = in.p_IdEmpresa;
						entradaCrearPersonaTarjetaBancaria.p_IdUsuario = in.p_IdUsuario;
						entradaCrearPersonaTarjetaBancaria.p_IdModulo = "SOC";
						entradaCrearPersonaTarjetaBancaria.p_UsaReglasNegocio = "N";
						entradaCrearPersonaTarjetaBancaria.p_IdBanco = tarjetasBancaria.IdBanco;
						entradaCrearPersonaTarjetaBancaria.p_IdTipoTarjetaBancaria = tarjetasBancaria.IdTipo;
						entradaCrearPersonaTarjetaBancaria.p_NumeroTarjetaBancaria = tarjetasBancaria.NumeroTarjeta;
						entradaCrearPersonaTarjetaBancaria.p_IdPersona = IdPersona;
						entradaCrearPersonaTarjetaBancaria.p_FechaVencimiento = tarjetasBancaria.FechaVencimiento;
						entradaCrearPersonaTarjetaBancaria.p_CodigoVerificacion = Long.valueOf(tarjetasBancaria.CodigoVerificacion);
						entradaCrearPersonaTarjetaBancaria.p_IdEstado = "ACT";
						
						CrearPersonaTarjetaBancaria crearPersonaTarjetaBancaria = new CrearPersonaTarjetaBancaria();
						SalidaCrearPersonaTarjetaBancaria salidaCrearPersonaTarjetaBancaria = crearPersonaTarjetaBancaria.logica(conn, entradaCrearPersonaTarjetaBancaria);
						
						if(salidaCrearPersonaTarjetaBancaria.P_OUT_ERRORESCONTADOR > 0)
						{
							entidadCreada.TipoEntidad = socioNegocio.TipoEntidad.trim();
							entidadCreada.IdEntidad = socioNegocio.IdEntidad;
							entidadCreada.Estado = "";
							entidadCreada.Nuevo = "";
							
							for(MensajeSalida mensaje : salidaCrearPersonaTarjetaBancaria.P_OUT_MENSAJES) {
								if(MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)) {
									mensajesErrores.IdInterno = 0L;
									mensajesErrores.TipoMensaje = "ERROR";
									mensajesErrores.Codigo = mensaje.codigo;
									mensajesErrores.Mensaje = mensaje.descripcion;
									
									listMensajesErrores.add(mensajesErrores);
									entidadCreada.Mensajes = listMensajesErrores;
									listEntidadCreada.add(entidadCreada);
									ContadorErrores++;
									break;
								}
								
								if(MensajeSalida.MENSAJE_ADVERTENCIA.equals(mensaje.tipo) || MensajeSalida.MENSAJE_AUTORIZACION.equals(mensaje.tipo)) {
									mensajesErrores.IdInterno = 0L;
									mensajesErrores.TipoMensaje = "ADVERTENCIA";
									mensajesErrores.Codigo = mensaje.codigo;
									mensajesErrores.Mensaje = mensaje.descripcion;
									
									listMensajesErrores.add(mensajesErrores);
									entidadCreada.Mensajes = listMensajesErrores;
									listEntidadCreada.add(entidadCreada);
									ContadorErrores++;
									break;
								}
								
							}
							
							break;
						}
					}
					
					if(ContadorErrores > 0) break;
				}
				
				// SE CREA LOS NOMBRE FANTASIA
				if (!socioNegocio.NombreFantasia.isEmpty() && ContadorEntidadModificada.compareTo(0L) == 0) {
					for (NombreFantasia nombreFantasia : socioNegocio.NombreFantasia) {
						EntradaCrearPersonaNombreFantasia entradaCrearPersonaNombreFantasia = new EntradaCrearPersonaNombreFantasia();
						entradaCrearPersonaNombreFantasia.p_IdEmpresa = in.p_IdEmpresa;
						entradaCrearPersonaNombreFantasia.p_IdUsuario = in.p_IdUsuario;
						entradaCrearPersonaNombreFantasia.p_IdModulo = "SOC";
						entradaCrearPersonaNombreFantasia.p_UsaReglasNegocio = "N";
						entradaCrearPersonaNombreFantasia.p_IdPersona = IdPersona;
						entradaCrearPersonaNombreFantasia.p_NombreFantasia = nombreFantasia.Nombre;
						entradaCrearPersonaNombreFantasia.p_IdEstado = "ACT";

						CrearPersonaNombreFantasia crearPersonaNombreFantasia = new CrearPersonaNombreFantasia();
						SalidaCrearPersonaNombreFantasia salidaCrearPersonaNombreFantasia = crearPersonaNombreFantasia.logica(conn, entradaCrearPersonaNombreFantasia);

						if (salidaCrearPersonaNombreFantasia.P_OUT_ERRORESCONTADOR > 0) {
							entidadCreada.TipoEntidad = socioNegocio.TipoEntidad.trim();
							entidadCreada.IdEntidad = socioNegocio.IdEntidad;
							entidadCreada.Estado = "";
							entidadCreada.Nuevo = "";
							
							for(MensajeSalida mensaje : salidaCrearPersonaNombreFantasia.P_OUT_MENSAJES) {
								if(MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo)) {
									mensajesErrores.IdInterno = 0L;
									mensajesErrores.TipoMensaje = "ERROR";
									mensajesErrores.Codigo = mensaje.codigo;
									mensajesErrores.Mensaje = mensaje.descripcion;
									
									listMensajesErrores.add(mensajesErrores);
									entidadCreada.Mensajes = listMensajesErrores;
									listEntidadCreada.add(entidadCreada);
									ContadorErrores++;
									break;
								}
								
								if(MensajeSalida.MENSAJE_ADVERTENCIA.equals(mensaje.tipo) || MensajeSalida.MENSAJE_AUTORIZACION.equals(mensaje.tipo)) {
									mensajesErrores.IdInterno = 0L;
									mensajesErrores.TipoMensaje = "ADVERTENCIA";
									mensajesErrores.Codigo = mensaje.codigo;
									mensajesErrores.Mensaje = mensaje.descripcion;
									
									listMensajesErrores.add(mensajesErrores);
									entidadCreada.Mensajes = listMensajesErrores;
									listEntidadCreada.add(entidadCreada);
									ContadorErrores++;
									break;
								}
								
							}
							
							break;
						}
					}
					
					if(ContadorErrores > 0) break;
				}
				
				ErroresContador = 0L;
				ContadorEntidadCreada = 1L;
				
				entidadCreada.TipoEntidad = socioNegocio.TipoEntidad.trim();
				entidadCreada.IdEntidad = socioNegocio.IdEntidad;
				entidadCreada.Estado = "ACT";
				entidadCreada.Nuevo = "S";
				listEntidadCreada.add(entidadCreada);
				if (("S").equals(EditarTelefonoYDirecciones) && socioNegocio.IdTipoRelacion.isEmpty() ) {

					if (ExistenEdiciones) {
						ConsultaExistePersona Persona = validarExistenciaSocioNegocio(conn, socioNegocio, in.p_IdEmpresa);
						List<MensajesErrores> mensajesPredeterminar = PredeterminarDireccionesYTelefonos( conn, in, Persona, socioNegocio, EdicionDireccionesFisicas, EdicionCorreos, EdicionTelefonos);

						entidadCreada.Mensajes.addAll(mensajesPredeterminar);
					}
				}
			}

			salida.P_OUT_ERRORESCONTADOR = ErroresContador;
			salida.p_Out_ContadorEntidadCreada = ContadorEntidadCreada;
			salida.p_Out_EntidadCreada = listEntidadCreada;

		}
		else {
			EntidadCreada entidadCreada = new EntidadCreada();
			MensajesErrores mensajesErrores = new MensajesErrores();

			entidadCreada.TipoEntidad = "";
			entidadCreada.IdEntidad = 0L;
			entidadCreada.Estado = "";
			entidadCreada.Nuevo = "";

			mensajesErrores.IdInterno = 0L;
			mensajesErrores.TipoMensaje = "ERROR";
			mensajesErrores.Codigo = "NO_SE_PUEDE_ENROLAR";
			mensajesErrores.Mensaje = "NO_SE_PUEDE_ENROLAR";

			listMensajesErrores.add(mensajesErrores);
			entidadCreada.Mensajes = listMensajesErrores;
			listEntidadCreada.add(entidadCreada);

			salida.P_OUT_ERRORESCONTADOR = 1L;
			salida.p_Out_ContadorEntidadCreada = 0L;
			salida.p_Out_EntidadCreada = listEntidadCreada;
		}

		return salida;
	}

	private boolean direccionFisicaNoExiste(DireccionFisica dir, List<DireccionFisica> direccionesExistentes) {
		logger.debug("INICIO::CrearSocioNegocio.direccionFisicaNoExiste");
		
		Boolean noExiste = false;
		noExiste = direccionesExistentes.stream().filter(dirEx -> dirEx.getDireccionFisicaCalleNumero().equalsIgnoreCase(dir.getDireccionFisicaCalleNumero())).count() == 0L;
		
		logger.debug("FIN::CrearSocioNegocio.direccionFisicaNoExiste");
		return noExiste;
	}

	private boolean direccionElectronicaNoExiste(DireccionElectronica dir, List<DireccionElectronica> direccionesExistentes) {
		logger.debug("INICIO::CrearSocioNegocio.direccionElectronicaNoExiste");
		
		Boolean noExiste = false;
		noExiste = direccionesExistentes.stream().filter(dirEx -> dirEx.Direccion.equalsIgnoreCase(dir.Direccion)).count() == 0L;
		
		logger.debug("FIN::CrearSocioNegocio.direccionElectronicaNoExiste");
		return noExiste;
	}

	private boolean telefonoNoExiste(Telefono tel, List<Telefono> telefonosExistentes) {
		logger.debug("INICIO::CrearSocioNegocio.telefonoNoExiste");
		
		Boolean noExiste = false;
		noExiste = telefonosExistentes.stream().filter(telEx -> telEx.Numero.equalsIgnoreCase(tel.Numero)).count() == 0L;
		
		logger.debug("FIN::CrearSocioNegocio.telefonoNoExiste");
		return noExiste;
	}
	
	private SalidaCrearDireccionFisicaMasivo crearDireccionesFisicas(Connection conn, List<CrearDireccionFisicaMasivo.DireccionFisica> direccionesFisicas, Long idEmpresa, Long idUsuario) throws Exception {
		logger.debug("INICIO::CrearSocioNegocio.crearDireccionesFisicas");
		long inicio = System.currentTimeMillis();
		
		EntradaCrearDireccionFisicaMasivo entradaCrearDireccionFisicaMasivo = new EntradaCrearDireccionFisicaMasivo();
		entradaCrearDireccionFisicaMasivo.p_IdEmpresa = idEmpresa;
		entradaCrearDireccionFisicaMasivo.p_IdModulo = ID_MODULO;
		entradaCrearDireccionFisicaMasivo.p_IdUsuario = idUsuario;
		entradaCrearDireccionFisicaMasivo.p_UsaReglasNegocio = USA_REGLAS_NEGOCIO;
		
		CrearDireccionFisicaMasivo crearDireccionFisicaMasivo = new CrearDireccionFisicaMasivo();
		
		entradaCrearDireccionFisicaMasivo.p_DireccionesFisica.addAll(direccionesFisicas);

		SalidaCrearDireccionFisicaMasivo salidaCrearDireccionFisicaMasivo = crearDireccionFisicaMasivo.logica(conn, entradaCrearDireccionFisicaMasivo);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearSocioNegocio.crearDireccionesFisicas", fin - inicio);
		return salidaCrearDireccionFisicaMasivo;
	}

	private SalidaCrearDireccionElectronicaMasivo crearDireccionesElectronicas(Connection conn, List<CrearDireccionElectronicaMasivo.DireccionElectronica> direccionesElectronicas, Long idEmpresa, Long idUsuario) throws Exception {
		logger.debug("INICIO::CrearClienteExpress.crearDireccionesElectronicas");
		long inicio = System.currentTimeMillis();
		
		EntradaCrearDireccionElectronicaMasivo entradaCrearDireccionElectronicaMasivo = new EntradaCrearDireccionElectronicaMasivo();
		entradaCrearDireccionElectronicaMasivo.p_IdEmpresa = idEmpresa;
		entradaCrearDireccionElectronicaMasivo.p_IdModulo = ID_MODULO;
		entradaCrearDireccionElectronicaMasivo.p_IdUsuario = idUsuario;
		entradaCrearDireccionElectronicaMasivo.p_UsaReglasNegocio = USA_REGLAS_NEGOCIO;
		
		CrearDireccionElectronicaMasivo crearDireccionElectronicaMasivo = new CrearDireccionElectronicaMasivo();

		entradaCrearDireccionElectronicaMasivo.p_DireccionesElectronica.addAll(direccionesElectronicas);

		SalidaCrearDireccionElectronicaMasivo salidaCrearDireccionElectronicaMasivo = crearDireccionElectronicaMasivo.logica(conn, entradaCrearDireccionElectronicaMasivo);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteExpress.crearDireccionesElectronicas", fin - inicio);
		return salidaCrearDireccionElectronicaMasivo;
	}
	
	private SalidaCrearTelefonoMasivo crearTelefonos(Connection conn, List<CrearTelefonoMasivo.Telefono> telefonos, Long idEmpresa, Long idUsuario) throws Exception {
		logger.debug("INICIO::CrearClienteExpress.crearTelefonos");
		long inicio = System.currentTimeMillis();
		
		EntradaCrearTelefonoMasivo entradaCrearTelefonoMasivo = new EntradaCrearTelefonoMasivo();
		entradaCrearTelefonoMasivo.p_IdEmpresa = idEmpresa;
		entradaCrearTelefonoMasivo.p_IdModulo = ID_MODULO;
		entradaCrearTelefonoMasivo.p_IdUsuario = idUsuario;
		entradaCrearTelefonoMasivo.p_UsaReglasNegocio = USA_REGLAS_NEGOCIO;
		
		CrearTelefonoMasivo crearTelefonoMasivo = new CrearTelefonoMasivo();

		entradaCrearTelefonoMasivo.p_Telefonos.addAll(telefonos);

		SalidaCrearTelefonoMasivo salidaCrearTelefonoMasivo = crearTelefonoMasivo.logica(conn, entradaCrearTelefonoMasivo);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearClienteExpress.crearTelefonos", fin - inicio);
		return salidaCrearTelefonoMasivo;
	}
	
	private List<MensajesErrores> PredeterminarDireccionesYTelefonos(Connection conn, CrearSocioNegocio_Entrada in, ConsultaExistePersona persona,SocioNegocio socioNegocio,  Boolean edicionDireccionesFisicas, Boolean edicionCorreos, Boolean edicionTelefonos) throws Exception {
		logger.trace("INICIO::CrearSocioNegocio.PredeterminarDireccionesYTelefonos");
		long inicio = System.currentTimeMillis();
		
		List<MensajesErrores> mensajesErrores = new ArrayList<MensajesErrores>();
		Long idPersona = persona.IdPersona;
		
		if(edicionDireccionesFisicas) {
			List<MensajesErrores> mensajesEdicionDireccionesFisicas = EdicionDireccionesFisicas(conn, in, idPersona, socioNegocio.IdTipoCanalIngresoCorredor, socioNegocio.DireccionFisica);
			
			if(!mensajesEdicionDireccionesFisicas.isEmpty()) {
				mensajesErrores.addAll(mensajesEdicionDireccionesFisicas);
			}
			
			if(mensajesErrores.stream().filter(mensaje -> PlataformaUtils.MensajeSalida.MENSAJE_ERROR.equals(mensaje.TipoMensaje)).count() > 0) {
				return mensajesErrores;
			}
		}
		
		if(edicionCorreos) {
			List<MensajesErrores> mensajesEdicionDireccionesElectronicas = EdicionDireccionesElectronicas(conn, in, idPersona, socioNegocio.IdTipoCanalIngresoCorredor, socioNegocio.DireccionElectronica);
			
			if(!mensajesEdicionDireccionesElectronicas.isEmpty()) {
				mensajesErrores.addAll(mensajesEdicionDireccionesElectronicas);
			}
			
			if(mensajesErrores.stream().filter(mensaje -> PlataformaUtils.MensajeSalida.MENSAJE_ERROR.equals(mensaje.TipoMensaje)).count() > 0) {
				return mensajesErrores;
			}
		}
		
		if(edicionTelefonos) {
			List<MensajesErrores> mensajesEdicionTelefonos = EdicionTelefonos(conn, in, idPersona, socioNegocio.IdTipoCanalIngresoCorredor, socioNegocio.Telefono);
			
			if(!mensajesEdicionTelefonos.isEmpty()) {
				mensajesErrores.addAll(mensajesEdicionTelefonos);
			}
			
			if(mensajesErrores.stream().filter(mensaje -> PlataformaUtils.MensajeSalida.MENSAJE_ERROR.equals(mensaje.TipoMensaje)).count() > 0) {
				return mensajesErrores;
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearSocioNegocio.PredeterminarDireccionesYTelefonos", fin - inicio);
		return mensajesErrores;
	}

	private List<MensajesErrores> EdicionDireccionesFisicas(Connection conn, CrearSocioNegocio_Entrada in, Long idPersona, String idTipoCanalIngresoCorredor, List<DireccionFisica> direccionesFisicas) throws Exception {
		logger.trace("INICIO::CrearSocioNegocio.EdicionDireccionesFisicas");
		long inicio = System.currentTimeMillis();
		
		List<MensajesErrores> mensajesErrores = new ArrayList<MensajesErrores>();
		
		List<EntidadPersonaEliminar> p_DireccionesPersona = new ArrayList<>();
		List<CrearClienteFullWS.DireccionFisica> p_DireccionesFisicasEliminar = new ArrayList<>();
		List<DireccionFisica> p_DireccionesAgregadas = new ArrayList<>();
		
		CrearClienteFullWS crearClienteFullWS = new CrearClienteFullWS();
		p_DireccionesPersona  = crearClienteFullWS.obtenerDireccionesFisicas(conn, idPersona);
		p_DireccionesAgregadas = direccionesFisicas.stream().filter(dirF -> dirF.IdDireccionFisica.equals(0L) && !("ELIMINAR").equals(dirF.Accion)).collect(Collectors.toList());
		
		Long idDireccionFisica = 0L;

		CrearDireccionFisicaMasivo crearDireccionFisica = new CrearDireccionFisicaMasivo();
		EntradaCrearDireccionFisicaMasivo entradaCrearDireccionFisicaMasivo = new EntradaCrearDireccionFisicaMasivo();
		entradaCrearDireccionFisicaMasivo.p_IdEmpresa = in.p_IdEmpresa;
		entradaCrearDireccionFisicaMasivo.p_IdModulo = ID_MODULO;
		entradaCrearDireccionFisicaMasivo.p_IdUsuario = in.p_IdUsuario;
		entradaCrearDireccionFisicaMasivo.p_UsaReglasNegocio = USA_REGLAS_NEGOCIO;
		
		for (DireccionFisica direccionFisica : p_DireccionesAgregadas) {
			AtomicLong index = new AtomicLong(0);
			CrearDireccionFisicaMasivo.DireccionFisica inDireccionFisica = new CrearDireccionFisicaMasivo.DireccionFisica();
			inDireccionFisica.IdInterno = index.getAndIncrement();
			inDireccionFisica.Calle = direccionFisica.Calle;
			inDireccionFisica.Numero = direccionFisica.Numero;
			inDireccionFisica.Departamento = direccionFisica.Departamento;
			inDireccionFisica.Sector = direccionFisica.Sector;
			inDireccionFisica.CodigoPostal = direccionFisica.CodigoPostal;
			inDireccionFisica.IdCiudad = direccionFisica.IdCiudad;
			inDireccionFisica.IdDivisionPais = direccionFisica.IdComuna;
			inDireccionFisica.CodigoProposito = direccionFisica.IdProposito;
			inDireccionFisica.Comentario = direccionFisica.Complemento;
			inDireccionFisica.IdEstado = "ACT";
			inDireccionFisica.IdPersona = idPersona;
			inDireccionFisica.Predeterminado = "N";
			entradaCrearDireccionFisicaMasivo.p_DireccionesFisica.add(inDireccionFisica);
		}
		
		if(!entradaCrearDireccionFisicaMasivo.p_DireccionesFisica.isEmpty()) {
			SalidaCrearDireccionFisicaMasivo salidaCrearDireccionFisicaMasivo = crearDireccionFisica.logica(conn, entradaCrearDireccionFisicaMasivo);
			idDireccionFisica = salidaCrearDireccionFisicaMasivo.P_OUT_REFID.get(0).IdEntidad;				
		}
		
		p_DireccionesFisicasEliminar = p_DireccionesPersona.stream().map(dirE -> {
			CrearClienteFullWS.DireccionFisica direccion = new CrearClienteFullWS.DireccionFisica();
			direccion.IdDireccionFisica = dirE.IdEntidadPersonaEliminar;
			direccion.IdPersona = dirE.IdPersona;
			return direccion;
		}).collect(Collectors.toList());
		
		if(!p_DireccionesFisicasEliminar.isEmpty()) {
			EliminarPersonaDireccionFisicaMasivo eliminarPersonaDireccionFisicaMasivo = new EliminarPersonaDireccionFisicaMasivo();
			EntradaEjecutarCambiosPersona entradaEjecutarCambiosPersona = new EntradaEjecutarCambiosPersona();
			entradaEjecutarCambiosPersona.p_IdEmpresa = in.p_IdEmpresa;
			entradaEjecutarCambiosPersona.p_IdPersona = idPersona;
			entradaEjecutarCambiosPersona.p_IdModulo = ID_MODULO;
			entradaEjecutarCambiosPersona.p_IdUsuario = in.p_IdUsuario;
			entradaEjecutarCambiosPersona.p_UsaReglasNegocio = USA_REGLAS_NEGOCIO;
			entradaEjecutarCambiosPersona.p_IdTipoCanal = idTipoCanalIngresoCorredor;
			entradaEjecutarCambiosPersona.p_EsCrearCliente = "S";
			entradaEjecutarCambiosPersona.p_RegistroCambio = crearClienteFullWS.obtenerRegistrosCambioEliminarDireccionesFisicas(p_DireccionesFisicasEliminar);
			
			SalidaEjecutarCambiosPersona salidaEliminarPersonaDireccionFisicaMasivo = eliminarPersonaDireccionFisicaMasivo.logica(conn, entradaEjecutarCambiosPersona);
			
			if (!salidaEliminarPersonaDireccionFisicaMasivo.P_OUT_MENSAJES.isEmpty()) {
				mensajesErrores.addAll(salidaEliminarPersonaDireccionFisicaMasivo.P_OUT_MENSAJES.stream().map(msj->{
					MensajesErrores mensajeError = new MensajesErrores();
					mensajeError.IdInterno = msj.id;
					mensajeError.Codigo = msj.codigo;
					mensajeError.TipoMensaje = msj.tipo;
					mensajeError.Mensaje = msj.descripcion;
					return mensajeError;
				}).collect(Collectors.toList()));
			}
			
			if(salidaEliminarPersonaDireccionFisicaMasivo.P_OUT_ERRORESCONTADOR > 0L) {
				return mensajesErrores;
			}
		}
		
		HashMap<String, Object> salidaDireccionFisica = predeterminarDireccionFisica(conn, idDireccionFisica, in);
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearSocioNegocio.EdicionDireccionesFisicas", fin - inicio);
		return mensajesErrores;
	}

	private List<MensajesErrores> EdicionDireccionesElectronicas(Connection conn, CrearSocioNegocio_Entrada in, Long idPersona, String idTipoCanalIngresoCorredor, List<DireccionElectronica> direccionesElectronicas) throws Exception {
		logger.trace("INICIO::CrearSocioNegocio.EdicionDireccionesElectronicas");
		long inicio = System.currentTimeMillis();
		
		List<MensajesErrores> mensajesErrores = new ArrayList<MensajesErrores>();
		
		List<EntidadPersonaEliminar> p_DireccionesPersona = new ArrayList<>();
		List<CrearClienteFullWS.DireccionElectronica> p_DireccionesElectronicasEliminar = new ArrayList<>();
		List<DireccionElectronica> p_DireccionesAgregadas = new ArrayList<>();
		
		CrearClienteFullWS crearClienteFullWS = new CrearClienteFullWS();
		p_DireccionesPersona  = crearClienteFullWS.obtenerDireccionesElectronicas(conn, idPersona);
		p_DireccionesAgregadas = direccionesElectronicas.stream().filter(dirE -> dirE.IdDireccionElectronica.equals(0L) && !("ELIMINAR").equals(dirE.Accion) ).collect(Collectors.toList());
		
		Long idDireccionElectronica = 0L;
		
		CrearDireccionElectronicaMasivo  crearDireccionElectronica = new CrearDireccionElectronicaMasivo ();
		EntradaCrearDireccionElectronicaMasivo  entradaCrearDireccionElectronicaMasivo = new EntradaCrearDireccionElectronicaMasivo();
		entradaCrearDireccionElectronicaMasivo.p_IdEmpresa = in.p_IdEmpresa;
		entradaCrearDireccionElectronicaMasivo.p_IdModulo = ID_MODULO;
		entradaCrearDireccionElectronicaMasivo.p_IdUsuario = in.p_IdUsuario;
		entradaCrearDireccionElectronicaMasivo.p_UsaReglasNegocio = USA_REGLAS_NEGOCIO;
		
		for(DireccionElectronica direccionElectronica : p_DireccionesAgregadas) {
			AtomicLong index = new AtomicLong(0);
			CrearDireccionElectronicaMasivo.DireccionElectronica inDireccionElectronica = new CrearDireccionElectronicaMasivo.DireccionElectronica();
			inDireccionElectronica.IdInterno = index.getAndIncrement();
			inDireccionElectronica.Direccion = direccionElectronica.Direccion;
			inDireccionElectronica.CodigoProposito = direccionElectronica.IdProposito;
			inDireccionElectronica.Comentario = "";
			inDireccionElectronica.IdEstado = "ACT";
			inDireccionElectronica.IdPersona = idPersona;
			inDireccionElectronica.Predeterminado = "N";
			inDireccionElectronica.IdUsuarioCreador = in.p_IdUsuario;
			
			entradaCrearDireccionElectronicaMasivo.p_DireccionesElectronica.add(inDireccionElectronica);
		}
		
		if(!entradaCrearDireccionElectronicaMasivo.p_DireccionesElectronica.isEmpty()) {
			SalidaCrearDireccionElectronicaMasivo salidaCrearDireccionElectronicaMasivo = crearDireccionElectronica.logica(conn, entradaCrearDireccionElectronicaMasivo);
			idDireccionElectronica = salidaCrearDireccionElectronicaMasivo.P_OUT_REFID.get(0).IdEntidad;				
		}
		
		p_DireccionesElectronicasEliminar = p_DireccionesPersona.stream().map(dirE -> {
			CrearClienteFullWS.DireccionElectronica direccion = new CrearClienteFullWS.DireccionElectronica();
			direccion.IdDireccionElectronica = dirE.IdEntidadPersonaEliminar;
			direccion.IdPersona = dirE.IdPersona;
			return direccion;
		}).collect(Collectors.toList());

			if(!p_DireccionesElectronicasEliminar.isEmpty()) {
			EliminarPersonaDireccionElectronicaMasivo eliminarPersonaDireccionElectronicaMasivo = new EliminarPersonaDireccionElectronicaMasivo();
			EntradaEjecutarCambiosPersona entradaEjecutarCambiosPersona = new EntradaEjecutarCambiosPersona();
			entradaEjecutarCambiosPersona.p_IdEmpresa = in.p_IdEmpresa;
			entradaEjecutarCambiosPersona.p_IdPersona = idPersona;
			entradaEjecutarCambiosPersona.p_IdModulo = ID_MODULO;
			entradaEjecutarCambiosPersona.p_IdUsuario = in.p_IdUsuario;
			entradaEjecutarCambiosPersona.p_UsaReglasNegocio = USA_REGLAS_NEGOCIO;
			entradaEjecutarCambiosPersona.p_IdTipoCanal = idTipoCanalIngresoCorredor;
			entradaEjecutarCambiosPersona.p_EsCrearCliente = "S";
			entradaEjecutarCambiosPersona.p_RegistroCambio = crearClienteFullWS.obtenerRegistrosCambioEliminarDireccionesElectronicas(p_DireccionesElectronicasEliminar);
			
			SalidaEjecutarCambiosPersona salidaEliminarPersonaDireccionElectronicaMasivo = eliminarPersonaDireccionElectronicaMasivo.logica(conn, entradaEjecutarCambiosPersona);
			
			if (!salidaEliminarPersonaDireccionElectronicaMasivo.P_OUT_MENSAJES.isEmpty()) {
				mensajesErrores.addAll(salidaEliminarPersonaDireccionElectronicaMasivo.P_OUT_MENSAJES.stream().map(msj->{
					MensajesErrores mensajeError = new MensajesErrores();
					mensajeError.IdInterno = msj.id;
					mensajeError.Codigo = msj.codigo;
					mensajeError.TipoMensaje = msj.tipo;
					mensajeError.Mensaje = msj.descripcion;
					return mensajeError;
				}).collect(Collectors.toList()));
			}
			
			if(salidaEliminarPersonaDireccionElectronicaMasivo.P_OUT_ERRORESCONTADOR > 0L) {
				return mensajesErrores;
			}
		}
			
		HashMap<String, Object> salidaDireccionElectronica = predeterminarDireccionElectronica(conn, idDireccionElectronica, in);					
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearSocioNegocio.EdicionDireccionesElectronicas", fin - inicio);
		return mensajesErrores;
	}

	private List<MensajesErrores> EdicionTelefonos(Connection conn, CrearSocioNegocio_Entrada in, Long idPersona, String idTipoCanalIngresoCorredor, List<Telefono> telefonos) throws Exception {
		logger.trace("INICIO::CrearSocioNegocio.EdicionTelefonos");
		long inicio = System.currentTimeMillis();
		
		List<MensajesErrores> mensajesErrores = new ArrayList<MensajesErrores>();
		
		List<EntidadPersonaEliminar> p_TelefonosPersona = new ArrayList<EntidadPersonaEliminar>();
		List<CrearClienteFullWS.Telefono> p_TelefonosEliminar = new ArrayList<CrearClienteFullWS.Telefono>();
		List<Telefono> p_TelefonosAgregados = new ArrayList<Telefono>();
		
		CrearClienteFullWS crearClienteFullWS = new CrearClienteFullWS();
		p_TelefonosPersona  = crearClienteFullWS.obtenerTelefonos(conn, idPersona);
		p_TelefonosAgregados = telefonos.stream().filter(tel -> tel.IdTelefono.equals(0L) && !("ELIMINAR").equals(tel.Accion) ).collect(Collectors.toList());
		
		Long idTelefono = 0L;
		
		CrearTelefonoMasivo  crearTelefono = new CrearTelefonoMasivo ();
		EntradaCrearTelefonoMasivo  entradaCrearTelefonoMasivo = new EntradaCrearTelefonoMasivo();
		entradaCrearTelefonoMasivo.p_IdEmpresa = in.p_IdEmpresa;
		entradaCrearTelefonoMasivo.p_IdModulo = ID_MODULO;
		entradaCrearTelefonoMasivo.p_IdUsuario = in.p_IdUsuario;
		entradaCrearTelefonoMasivo.p_UsaReglasNegocio = USA_REGLAS_NEGOCIO;
		
		for(Telefono telefono : p_TelefonosAgregados) {
			AtomicLong index = new AtomicLong(0);
			CrearTelefonoMasivo.Telefono inTelefono = new CrearTelefonoMasivo.Telefono();
			inTelefono.IdInterno = index.getAndIncrement();
			inTelefono.IdPais = telefono.IdPais;
			inTelefono.IdTipoTelefono = telefono.Tipo;
			inTelefono.Numero = telefono.Numero;
			inTelefono.Anexo = telefono.Anexo;
			inTelefono.CodigoProposito = telefono.IdProposito;
			inTelefono.Comentario = "";
			inTelefono.IdEstado = "ACT";
			inTelefono.IdPersona = idPersona;
			inTelefono.Predeterminado = "N";
			
			entradaCrearTelefonoMasivo.p_Telefonos.add(inTelefono);
		}
		
		if(!entradaCrearTelefonoMasivo.p_Telefonos.isEmpty()) {
			SalidaCrearTelefonoMasivo salidaCrearTelefonoMasivo = crearTelefono.logica(conn, entradaCrearTelefonoMasivo);
			idTelefono = salidaCrearTelefonoMasivo.P_OUT_REFID.get(0).IdEntidad;				
		}
		
		p_TelefonosEliminar = p_TelefonosPersona.stream().map(tel -> {
			CrearClienteFullWS.Telefono telefono = new CrearClienteFullWS.Telefono();
			telefono.IdTelefono = tel.IdEntidadPersonaEliminar;
			telefono.IdPersona = tel.IdPersona;
			return telefono;
		}).collect(Collectors.toList());

		if(!p_TelefonosEliminar.isEmpty()) {
			EliminarPersonaTelefonoMasivo eliminarPersonaTelefonoMasivo = new EliminarPersonaTelefonoMasivo();
			EntradaEjecutarCambiosPersona entradaEjecutarCambiosPersona = new EntradaEjecutarCambiosPersona();
			entradaEjecutarCambiosPersona.p_IdEmpresa = in.p_IdEmpresa;
			entradaEjecutarCambiosPersona.p_IdPersona = idPersona;
			entradaEjecutarCambiosPersona.p_IdModulo = ID_MODULO;
			entradaEjecutarCambiosPersona.p_IdUsuario = in.p_IdUsuario;
			entradaEjecutarCambiosPersona.p_UsaReglasNegocio = USA_REGLAS_NEGOCIO;
			entradaEjecutarCambiosPersona.p_IdTipoCanal = idTipoCanalIngresoCorredor;
			entradaEjecutarCambiosPersona.p_EsCrearCliente = "S";
			entradaEjecutarCambiosPersona.p_RegistroCambio = crearClienteFullWS.obtenerRegistrosCambioEliminarTelefonos(p_TelefonosEliminar);
			
			SalidaEjecutarCambiosPersona salidaEliminarPersonaTelefonoMasivo = eliminarPersonaTelefonoMasivo.logica(conn, entradaEjecutarCambiosPersona);
			
			if (!salidaEliminarPersonaTelefonoMasivo.P_OUT_MENSAJES.isEmpty()) {
				mensajesErrores.addAll(salidaEliminarPersonaTelefonoMasivo.P_OUT_MENSAJES.stream().map(msj->{
					MensajesErrores mensajeError = new MensajesErrores();
					mensajeError.IdInterno = msj.id;
					mensajeError.Codigo = msj.codigo;
					mensajeError.TipoMensaje = msj.tipo;
					mensajeError.Mensaje = msj.descripcion;
					return mensajeError;
				}).collect(Collectors.toList()));
			}
			
			if(salidaEliminarPersonaTelefonoMasivo.P_OUT_ERRORESCONTADOR > 0L) {
				return mensajesErrores;
			}
		}
		
		HashMap<String, Object> salidaTelefono = predeterminarTelefono(conn, idTelefono, in);					
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearSocioNegocio.EdicionTelefonos", fin - inicio);
		return mensajesErrores;
	}

	private boolean debeModificarDatos(String agregaAdvertencia, String idTipoRelacion) {
		logger.info("INICIO::CrearSocioNegocio.debeModificarDatos");
		
		if("N".equalsIgnoreCase(agregaAdvertencia)) {
			logger.info("FIN::CrearSocioNegocio.debeModificarDatos");
			return true;
		}
		
		Boolean debeModificar = true;
		
		if("S".equalsIgnoreCase(agregaAdvertencia) && !idTipoRelacion.isEmpty()) {
			debeModificar = false;
		}
		
		logger.info("FIN::CrearSocioNegocio.debeModificarDatos");
		return debeModificar;
	}

	private ConsultaExistePersona validarExistenciaSocioNegocio(Connection conn, SocioNegocio socio, Long idEmpresa) throws Exception {
		logger.trace("INICIO::CrearSocioNegocio.validarExistenciaSocioNegocio");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> parametros = new HashMap<String, Object>();
	    parametros.put("DocumentoIdentidad", socio.DocumentoIdentidad);
	    parametros.put("TipoDocumentoIdentidad", socio.TipoDocumentoIdentidad);
	    
	    SQL sql = new SQL();
		List<ConsultaExistePersona> contadorPersona = sql.query(conn, SQL_CONSULTAR_EXISTENCIA_PERSONA, parametros, ConsultaExistePersona.class);
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearSocioNegocio.validarExistenciaSocioNegocio", fin - inicio);
		
		
		return contadorPersona.parallelStream().findFirst().orElse(new ConsultaExistePersona());
	}
	
	public CrearSocioNegocio_Salida mesajesDeError(String tipoEntidad, Long idEntidad, Long idInterno, String tipoMensaje, String codigo, String mensaje)
	{
		CrearSocioNegocio_Salida salida = new CrearSocioNegocio_Salida();

		List<EntidadCreada> listEntidadCreada = new ArrayList<EntidadCreada>();
		List<MensajesErrores> listMensajesErrores = new ArrayList<MensajesErrores>();

		EntidadCreada entidadCreada = new EntidadCreada();
		MensajesErrores mensajesErrores = new MensajesErrores();

		entidadCreada.TipoEntidad = "";
		entidadCreada.IdEntidad = 0L;
		entidadCreada.Estado = "";
		entidadCreada.Nuevo = "";
		
		listMensajesErrores.add(mensajesErrores);
		entidadCreada.Mensajes = listMensajesErrores;
		listEntidadCreada.add(entidadCreada);
		
		mensajesErrores.IdInterno = idInterno;
		mensajesErrores.TipoMensaje = tipoMensaje;
		mensajesErrores.Codigo = codigo;
		mensajesErrores.Mensaje = mensaje;
		
		listMensajesErrores.add(mensajesErrores);
		entidadCreada.Mensajes = listMensajesErrores;
		listEntidadCreada.add(entidadCreada);
		
		salida.P_OUT_ERRORESCONTADOR = 1L;
		salida.p_Out_ContadorEntidadCreada = 0L;
		salida.p_Out_EntidadCreada = listEntidadCreada;
		
		return salida;
	}
	
	private ModificarIndicador.SalidaModificarIndicador modificarIndicador(Connection con, CrearSocioNegocio_Entrada entrada, String idTipoEntidad, Long idEntidad, List<IndicadorPersonaCliente> indicadores) throws Exception{
		EntradaModificarIndicador entradaModificarIndicador = new EntradaModificarIndicador();
		entradaModificarIndicador.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaModificarIndicador.p_IdUsuario = entrada.p_IdUsuario;
		entradaModificarIndicador.p_IdModulo = "SOC";
		entradaModificarIndicador.p_UsaReglasNegocio = "N";
		entradaModificarIndicador.p_IdTipoEntidad = idTipoEntidad;
		entradaModificarIndicador.p_IdEntidad = idEntidad;
		entradaModificarIndicador.p_ModeloCliente = "";
		entradaModificarIndicador.p_RegistroAtributo = indicadores.parallelStream().map(indicador -> {
			ModificarIndicador.RegistroAtributo registroAtributo = new ModificarIndicador.RegistroAtributo();
			registroAtributo.IdIndicador = indicador.IdIndicador; 
			registroAtributo.IdTipoIndicador = indicador.IdTipoIndicador;
			registroAtributo.ValorIndicador = indicador.ValorIndicador;
			registroAtributo.FechaVencimiento = Fecha.toDateTime(Fecha.stringToSqlDate(indicador.FechaVencimiento));
			return registroAtributo;
		}).collect(Collectors.toList());
		ModificarIndicador modificarIndicador = new ModificarIndicador();
		ModificarIndicador.SalidaModificarIndicador salidaModificarIndicador = modificarIndicador.logica(con, entradaModificarIndicador);
		return salidaModificarIndicador;
	}
	
	private List<DireccionFisica> obtenerDireccionesFisicas(Connection conn, Long idPersona) throws Exception {
		logger.debug("INICIO::CrearSocioNegocio.obtenerDireccionesFisicas");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		HashMap<String, Object> params = new HashMap<String, Object>();

		params.put("p_IdPersona", idPersona);
		List<DireccionFisica> lstDirFisicas = sql.query(conn, SQL_SELECT_DIRECCION_FISICA_PERSONA, params,  DireccionFisica.class);

		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearSocioNegocio.obtenerDireccionesFisicas", finServicio - inicio);
		return lstDirFisicas;
	}

	private List<DireccionElectronica> obtenerDireccionesElectronicas(Connection con, Long idPersona)
			throws Exception {
		logger.debug("INICIO::CrearSocioNegocio.obtenerDireccionesElectronicas");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("p_IdPersona", idPersona);
		
		List<DireccionElectronica> lstDirElectronicas = sql.query(con, SQL_SELECT_DIRECCION_ELECTRONICA_PERSONA, params, DireccionElectronica.class);

		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearSocioNegocio.obtenerDireccionesElectronicas", finServicio - inicio);
		return lstDirElectronicas;
	}

	private List<Telefono> obtenerTelefonos(Connection con, Long idPersona) throws Exception {
		logger.debug("INICIO::CrearSocioNegocio.obtenerTelefonos");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("p_IdPersona", idPersona);

		List<Telefono> lstTelefonos = sql.query(con, SQL_SELECT_TELEFONO_PERSONA, params, Telefono.class);

		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearSocioNegocio.obtenerTelefonos", finServicio - inicio);
		return lstTelefonos;
	}
	
	private HashMap<String, Object> predeterminarTelefono(Connection conn, Long idTelefono, CrearSocioNegocio_Entrada entrada) throws Exception {
		logger.debug("INICIO::Persona.predeterminarTelefono");
		long inicio = System.currentTimeMillis();

		SQL actualizar_telefono = new SQL();

		HashMap<String, Object> paramIN = new HashMap<>(6);
		paramIN.put("IdEmpresa", entrada.p_IdEmpresa);
		paramIN.put("IdUsuario", entrada.p_IdUsuario);
		paramIN.put("IdModulo", "SOC");
		paramIN.put("UsaReglasNegocio", "N");
		paramIN.put("IdTelefono", idTelefono);

		HashMap<String, Class> paramOUT = new HashMap<>(6);
		paramOUT.put("p_out_RefId", Integer.class);
		paramOUT.put("p_out_AdvertenciasContador", Integer.class);
		paramOUT.put("p_out_AdvertenciasDetalle", MensajeAdvertencia.class);
		paramOUT.put("p_out_ErroresContador", Integer.class);
		paramOUT.put("p_out_ErroresDetalle", ErroresDetalle.class);
		HashMap<String, Object> resultadoPT = actualizar_telefono.call(conn, SQL_PREDETERMINAR_PERSONA_TELEFONO, paramIN, paramOUT);

		long finServicio = System.currentTimeMillis();
		logger.info("FIN::Persona.predeterminarTelefono", finServicio - inicio);
		return resultadoPT;
	}

	private HashMap<String, Object> predeterminarDireccionFisica(Connection conn, Long idDireccionFisica, CrearSocioNegocio_Entrada entrada) throws Exception {
		logger.debug("INICIO::Persona.predeterminarDireccionFisica");
		long inicio = System.currentTimeMillis();

		SQL actualizar_direccion = new SQL();

		HashMap<String, Object> paramIN = new HashMap<>(6);
		paramIN.put("IdEmpresa", entrada.p_IdEmpresa);
		paramIN.put("IdUsuario", entrada.p_IdUsuario);
		paramIN.put("IdModulo", "SOC");
		paramIN.put("UsaReglasNegocio", "N");
		paramIN.put("IdDireccionFisica", idDireccionFisica);

		HashMap<String, Class> paramOUT = new HashMap<>(6);
		paramOUT.put("p_out_RefId", Integer.class);
		paramOUT.put("p_out_AdvertenciasContador", Integer.class);
		paramOUT.put("p_out_AdvertenciasDetalle", MensajeAdvertencia.class);
		paramOUT.put("p_out_ErroresContador", Integer.class);
		paramOUT.put("p_out_ErroresDetalle", ErroresDetalle.class);
		HashMap<String, Object> resultadoPF = actualizar_direccion.call(conn, SQL_PREDETERMINAR_PERSONA_DIRECCION_FISICA, paramIN, paramOUT);

		long finServicio = System.currentTimeMillis();
		logger.info("FIN::Persona.predeterminarDireccionFisica", finServicio - inicio);
		return resultadoPF;
	}

	private HashMap<String, Object> predeterminarDireccionElectronica(Connection conn, Long idDireccionElectronica, CrearSocioNegocio_Entrada entrada) throws Exception {
		logger.debug("INICIO::Persona.predeterminarDireccionElectronica");
		long inicio = System.currentTimeMillis();

		SQL actualizar_correo = new SQL();

		HashMap<String, Object> paramIN = new HashMap<>(6);
		paramIN.put("IdEmpresa", entrada.p_IdEmpresa);
		paramIN.put("IdUsuario", entrada.p_IdUsuario);
		paramIN.put("IdModulo", "SOC");
		paramIN.put("UsaReglasNegocio", "N");
		paramIN.put("IdDireccionElectronica", idDireccionElectronica);

		HashMap<String, Class> paramOUT = new HashMap<>(6);
		paramOUT.put("p_out_RefId", Integer.class);
		paramOUT.put("p_out_AdvertenciasContador", Integer.class);
		paramOUT.put("p_out_AdvertenciasDetalle", MensajeAdvertencia.class);
		paramOUT.put("p_out_ErroresContador", Integer.class);
		paramOUT.put("p_out_ErroresDetalle", ErroresDetalle.class);
		HashMap<String, Object> resultadoPC = actualizar_correo.call(conn, SQL_PREDETERMINAR_PERSONA_DIRECCION_ELECTRONICA, paramIN, paramOUT);

		long finServicio = System.currentTimeMillis();
		logger.info("FIN::Persona.predeterminarDireccionElectronica", finServicio - inicio);
		return resultadoPC;
	}
	//-------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE - CALL
	//-------------------------------------------------------------------------------------------
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		CrearSocioNegocio_Entrada in = gson.fromJson(jin.toString(), CrearSocioNegocio_Entrada.class);
		CrearSocioNegocio_Salida out = logica(conn, in);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	// -------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	// -------------------------------------------------
	public static void main(String[] args) {
		System.out.println("== INICIO PRUEBA LOCAL ==");

		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
					.serializeNulls().create();
			
			//String jsonCL = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_ClaveUsuario\":null,\"p_SocioNegocio\":[{\"IdEntidad\":1,\"TipoEntidad\":\"PERSONA\",\"IdTipoPersona\":\"N\",\"TipoDocumentoIdentidad\":\"R\",\"DocumentoIdentidad\":\"18576992-5\",\"Nombres\":\"SERGIO\",\"IdGenero\":\"M\",\"IdPaisNacimientoConstitucion\":44,\"FechaNacimientoConstitucion\":\"1993-08-05\",\"IdNacionalidadPrincipal\":44,\"IdEstadoCivil\":\"SO\",\"IdPaisNacimiento\":44,\"IdPaisNacionalidad\":44,\"IdPaisResidencia\":44,\"DireccionFisica\":[{\"IdDireccionFisica\":1307707,\"IdProposito\":\"PART\",\"Sector\":\"Centro\",\"CodigoPostal\":\"6162000\",\"Calle\":\"Baquedano\",\"Numero\":\"999\",\"IdCiudad\":2614}],\"Telefono\":[{\"IdTelefono\":1735014,\"IdProposito\":\"PART\",\"Numero\":\"612654321\",\"IdPais\":44,\"Tipo\":\"FIJO\"}],\"DireccionElectronica\":[{\"IdDireccionElectronica\":855876,\"IdProposito\":\"PART\",\"Direccion\":\"smedrano@email.com\"}]}]}";
			String jsonCL = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_ClaveUsuario\":null,\"p_SocioNegocio\":[{\"IdEntidad\":1,\"TipoEntidad\":\"PERSONA\",\"IdTipoPersona\":\"N\",\"TipoDocumentoIdentidad\":\"R\",\"DocumentoIdentidad\":\"18576992-5\",\"Nombres\":\"SERGIO\",\"IdGenero\":\"M\",\"IdPaisNacimientoConstitucion\":44,\"FechaNacimientoConstitucion\":\"1993-08-05\",\"IdNacionalidadPrincipal\":44,\"IdEstadoCivil\":\"SO\",\"IdPaisNacimiento\":44,\"IdPaisNacionalidad\":44,\"IdPaisResidencia\":44,\"DireccionFisica\":[{\"IdProposito\":\"PART\",\"Sector\":\"Centro\",\"CodigoPostal\":\"6160000\",\"Calle\":\"Manuel Bulnes\",\"Numero\":\"999\",\"IdCiudad\":2614}],\"Telefono\":[{\"IdProposito\": \"COME\",\"Numero\": \"61200251\",\"IdPais\": 44,\"Tipo\": \"FIJO\"}],\"DireccionElectronica\":[{\"IdProposito\":\"PART\",\"Direccion\":\"smedrano@bolsadesantiago.com\"}]}]}";
			
			CrearSocioNegocio_Entrada Objin = gson.fromJson(jsonCL, CrearSocioNegocio_Entrada.class);

			CrearSocioNegocio demo = new CrearSocioNegocio();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			
			try {
				CrearSocioNegocio_Salida ObjOut = demo.logica(con, Objin);
				con.rollback();
				//con.commit();
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