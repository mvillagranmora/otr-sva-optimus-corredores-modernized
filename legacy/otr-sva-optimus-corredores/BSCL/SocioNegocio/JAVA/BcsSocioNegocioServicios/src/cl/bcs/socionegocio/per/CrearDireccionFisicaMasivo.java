package cl.bcs.socionegocio.per;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.per.ConfirmarModificarPersonaDireccionFisicaMasivo.Cliente;
import cl.bcs.socionegocio.per.ConfirmarModificarPersonaDireccionFisicaMasivo.Indicador;
import cl.bcs.socionegocio.per.ConfirmarModificarPersonaDireccionFisicaMasivo.Portafolio;
import cl.bcs.socionegocio.per.ModificarPersonaDireccionFisicaMasivo.DireccionFisica;
import cl.bcs.socionegocio.per.pojo.Entidad;
import cl.bcs.socionegocio.soc.SocioNegocioUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearDireccionFisicaMasivo implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(CrearDireccionFisicaMasivo.class);
	
	private final String SQL_INSERT_DIRECCION_FISICA = loadResourceToString("cl/bcs/socionegocio/per/sql/CrearDireccionFisicaMasivo_InsertDireccionFisica.sql");
	private String SQL_UPDATE_DIRECCION_FISICA_NO_PREDETERMINADA = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaDireccionFisicaMasivo_UpdateDireccionFisicaNoPredeterminada.sql");
	private String SQL_SELECT_CLIENTE = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaDireccionFisicaMasivo_SelectCliente.sql");
	private String SQL_UPDATE_DIRECCION_FISICA_PORTAFOLIO= loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaDireccionFisicaMasivo_UpdateDireccionFisicaPortafolio.sql");
	private String SQL_SELECT_INDICADOR_MODELO_TIPO_CLIENTE= loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaDireccionFisicaMasivo_SelectIndicadoresModeloTipoCliente.sql");
	private String SQL_UPDATE_INDICADORES_AUX= loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaDireccionFisicaMasivo_UpdateIndicadores.sql");
	private String SQL_UPDATE_INDICADORES= loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaDireccionFisicaMasivo_UpdateIndicadores.sql");
	private String SQL_SELECT_PORTAFOLIOS_CLIENTE= loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaDireccionFisicaMasivo_SelectPortafoliosCliente.sql");
	private String SQL_UPDATE_DIRECCION_FISICA_PRINCIPAL_TABLA_PERSONA = loadResourceToString("cl/bcs/socionegocio/per/sql/CrearDireccionFisicaMasivo_UpdateDireccionFisicaPrincipalTablaPersona.sql"); 


	
	private final String ID_CONCEPTO = "NUEDIF";
	
	public static class EntradaCrearDireccionFisicaMasivo extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		
		public List<DireccionFisica> p_DireccionesFisica;
		
		public EntradaCrearDireccionFisicaMasivo() {
			this.p_DireccionesFisica = new ArrayList<>();
		}
	}
	
	public static class SalidaCrearDireccionFisicaMasivo extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<Entidad> P_OUT_REFID;
		
		public SalidaCrearDireccionFisicaMasivo() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<>();
			this.P_OUT_REFID = new ArrayList<>();
		}
	}
	
	public static class DireccionFisica extends BcsPojo {
		public Long IdInterno;
		public Long IdDireccionFisica;
		public String Calle;
		public String Numero;
		public String Departamento;
		public String Sector;
		public String CodigoPostal;
		public Long IdCiudad;
		public Long IdDivisionPais;
		public String CodigoProposito;
		public String Comentario;
		public String IdEstado;
		public Long IdPersona;
		public String CodigoCompuesto;
		public String Predeterminado;
		public Long IdUsuarioCreador;
		public String IdEstadoAutorizacion;
		
		public DireccionFisica () {
			this.Predeterminado = "N";
		}
	}
	
	public static class Cliente {
		public Long IdCliente;
		public Long IdPersona;
		public String IdTipoCliente;
	}
	
	public static class Indicador{
		public Long IdIndicador;
		public Long IdTipoIndicador;
		public Long IdEntidad;
		public String ValorIndicador;
	}
	public static class Portafolio{
		public Long IdPortafolio;	
	}
	
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaCrearDireccionFisicaMasivo jin = gson.fromJson(in.toString(), EntradaCrearDireccionFisicaMasivo.class);
		SalidaCrearDireccionFisicaMasivo out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCrearDireccionFisicaMasivo logica(Connection con, EntradaCrearDireccionFisicaMasivo entrada) throws Exception {
		logger.info("INICIO::CrearDireccionFisicaMasivo");
		long inicioServicio = System.currentTimeMillis();
		
		SalidaCrearDireccionFisicaMasivo out = new SalidaCrearDireccionFisicaMasivo();
		Boolean esOperacionConAutorizacion = SocioNegocioUtils.esOperacionConAutorizacion(con, entrada.p_IdEmpresa, ID_CONCEPTO, entrada.p_UsaReglasNegocio);
		List<DireccionFisica> direccionesFisica = obtenerDireccionesFisica(con, entrada.p_DireccionesFisica, esOperacionConAutorizacion);
		insertarDireccionesFisica(con, direccionesFisica);
		predeterminaDireccionesFisica(con, direccionesFisica);
	
		if(esOperacionConAutorizacion) {
			out = crearEventoAutorizacion(con, entrada, direccionesFisica);
		} else {
			agregarLog(con, entrada, direccionesFisica);
		}
		
		out.P_OUT_REFID = obtenerEntidadesInsertados(direccionesFisica);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearDireccionFisicaMasivo", finServicio - inicioServicio);
		return out;
	}
		
	private List<DireccionFisica> obtenerDireccionesFisica(Connection con, List<DireccionFisica> entradaDireccionesFisica, Boolean esOperacionConAutorizacion) throws Exception {
		logger.trace("INICIO::CrearDireccionFisicaMasivo.obtenerDireccionesFisica");
		long inicio = System.currentTimeMillis();
		
		List<Long> idsDireccionesFisica = Utils.getSecuencia(con, "BSCL.SocioNegocio.BD.Secuencias::DireccionFisica", Long.valueOf(entradaDireccionesFisica.size()));
		AtomicInteger index = new AtomicInteger();
		
		List<DireccionFisica> direccionesFisica = entradaDireccionesFisica
				.stream()
				.map(entradaDireccionFisica->{
					Long idDireccionFisica = idsDireccionesFisica.get(index.getAndIncrement());
					DireccionFisica direccionFisica = obtenerEntidadDireccionFisica(entradaDireccionFisica, idDireccionFisica, esOperacionConAutorizacion);
					return direccionFisica;
				})
				.collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearDireccionFisicaMasivo.obtenerDireccionesFisica", fin - inicio);
		return direccionesFisica;
	}
	
	private DireccionFisica obtenerEntidadDireccionFisica(DireccionFisica entradaDireccionFisica, Long idDireccionFisica, Boolean esOperacionConAutorizacion) {
		logger.trace("INICIO::CrearDireccionFisicaMasivo.obtenerEntidadDireccionFisica");
		long inicio = System.currentTimeMillis();
		
		DireccionFisica direccionFisica = new DireccionFisica();
		direccionFisica.IdInterno = entradaDireccionFisica.IdInterno;
		direccionFisica.IdDireccionFisica = idDireccionFisica;
		direccionFisica.Calle = entradaDireccionFisica.Calle;
		direccionFisica.Numero = entradaDireccionFisica.Numero;
		direccionFisica.Departamento = entradaDireccionFisica.Departamento;
		direccionFisica.Sector = SocioNegocioUtils.obtenerValorNoNulo(entradaDireccionFisica.Sector, "");
		direccionFisica.CodigoPostal = SocioNegocioUtils.obtenerValorNoNulo(entradaDireccionFisica.CodigoPostal, "");
		direccionFisica.IdCiudad = entradaDireccionFisica.IdCiudad;
		direccionFisica.IdDivisionPais = entradaDireccionFisica.IdDivisionPais;
		direccionFisica.CodigoProposito = entradaDireccionFisica.CodigoProposito;
		direccionFisica.Comentario =  SocioNegocioUtils.obtenerValorNoNulo(entradaDireccionFisica.Comentario, "");
		direccionFisica.IdEstado = SocioNegocioUtils.obtenerValorNoNulo(entradaDireccionFisica.IdEstado, "ACT");
		direccionFisica.IdPersona = entradaDireccionFisica.IdPersona;
		direccionFisica.CodigoCompuesto = entradaDireccionFisica.CodigoCompuesto;
		direccionFisica.Predeterminado = entradaDireccionFisica.Predeterminado;
		direccionFisica.IdUsuarioCreador = entradaDireccionFisica.IdUsuarioCreador;
		direccionFisica.IdEstadoAutorizacion = SocioNegocioUtils.obtenerIdEstadoAutorizacion(esOperacionConAutorizacion);
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearDireccionFisicaMasivo.obtenerEntidadDireccionFisica", fin - inicio);
		
		return direccionFisica;
	}
	
	private void insertarDireccionesFisica(Connection con, List<DireccionFisica> direccionesFisica) throws Exception {
		logger.trace("INICIO::CrearDireccionFisicaMasivo.insertarDireccionesFisica");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		sql.batch(con, SQL_INSERT_DIRECCION_FISICA, direccionesFisica, DireccionFisica.class);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearDireccionFisicaMasivo.insertarDireccionesFisica", fin - inicio);
	}
	
	private void predeterminaDireccionesFisica(Connection con, List<DireccionFisica> direccionesFisica) throws Exception {
		logger.trace("INICIO::CrearDireccionFisicaMasivo.predeterminaDireccionesFisica");
		long inicio = System.currentTimeMillis();
		List<DireccionFisica> direccionFisicaPredeterminada = direccionesFisica.stream()
				.filter(tel->tel.Predeterminado.equals("S"))
				.collect(Collectors.toList());
		
				
		if(direccionesFisica.size() > 0 ) {
			SQL sql = new SQL();
			if(direccionFisicaPredeterminada.size()>0) {
				HashMap<String, Object> parametros = new HashMap<String, Object>();
				Long idDireccionFisicaPredeterminada = direccionFisicaPredeterminada.get(0).IdDireccionFisica;
				parametros.put("IdDireccionFisica", idDireccionFisicaPredeterminada);
				List<Cliente> cliente = sql.query(con, SQL_SELECT_CLIENTE, parametros, Cliente.class);
				if(cliente.size()>0) {
					Long idCliente = cliente.get(0).IdCliente;
					Long idPersona = cliente.get(0).IdPersona;
					String idTipoCliente = cliente.get(0).IdTipoCliente;
					SQL_UPDATE_DIRECCION_FISICA_NO_PREDETERMINADA = SQL_UPDATE_DIRECCION_FISICA_NO_PREDETERMINADA.replace("(:IdDireccionFisica)", idDireccionFisicaPredeterminada.toString()).replace("(:IdPersona)", idPersona.toString());
					SQL.exec(con, SQL_UPDATE_DIRECCION_FISICA_NO_PREDETERMINADA);
					SQL_UPDATE_DIRECCION_FISICA_PORTAFOLIO  = SQL_UPDATE_DIRECCION_FISICA_PORTAFOLIO.replace("(:IdDireccionEnvioCorrespondencia)", idDireccionFisicaPredeterminada.toString()).replace("(:IdCliente)", idCliente.toString());
					SQL.exec(con, SQL_UPDATE_DIRECCION_FISICA_PORTAFOLIO);
					
					//Actualizar direccion fisica principal en tabla PERSONA
					SQL_UPDATE_DIRECCION_FISICA_PRINCIPAL_TABLA_PERSONA = SQL_UPDATE_DIRECCION_FISICA_PRINCIPAL_TABLA_PERSONA.replace("(:IdDireccionFisica)", idDireccionFisicaPredeterminada.toString()).replace("(:IdPersona)", idPersona.toString());
					SQL.exec(con, SQL_UPDATE_DIRECCION_FISICA_PRINCIPAL_TABLA_PERSONA);
	
					parametros = new HashMap<String, Object>();
					parametros.put("ModeloCliente",idTipoCliente);
					Long idCheckBoxEnvioDireccionFisica = 10676L; //Si envia a direccion propia
					Long idCheckBoxEnvioDireccionCartolaFisica = 1020L; //Si envia cartola a direccion fisica
					Long idComboBoxDireccionFisicaCartola = 38L;
					Long idComboBoxDireccionFisicaGeneral = 10670L;
					List<Indicador> Indicadores = sql.query(con, SQL_SELECT_INDICADOR_MODELO_TIPO_CLIENTE, parametros, Indicador.class);
					parametros = new HashMap<String, Object>();
					parametros.put("IdCliente",idCliente);
					List<Portafolio> idPortafolios = sql.query(con,SQL_SELECT_PORTAFOLIOS_CLIENTE,parametros,Portafolio.class);
					if(Indicadores.size()>0) {
						List<Indicador> indicadorEnvioDireccionFisica = Indicadores.stream()
								.filter(ind->ind.IdTipoIndicador.equals(idCheckBoxEnvioDireccionFisica))
								.collect(Collectors.toList());
						List<Indicador> indicadorEnvioDireccionCartolaFisica = Indicadores.stream()
								.filter(ind->ind.IdTipoIndicador.equals(idCheckBoxEnvioDireccionCartolaFisica))
								.collect(Collectors.toList());
						List<Indicador> ComboDireccionFisicaGeneral = Indicadores.stream()
								.filter(ind->ind.IdTipoIndicador.equals(idComboBoxDireccionFisicaCartola))
								.collect(Collectors.toList());
						List<Indicador> ComboDireccionCartolaFisica = Indicadores.stream()
								.filter(ind->ind.IdTipoIndicador.equals(idComboBoxDireccionFisicaGeneral))
								.collect(Collectors.toList());
						
						List<Indicador> resultIndicadorPortafolioEnvioDireccion = new ArrayList<>();
						List<Indicador> resultIndicadorPortafolioEnvioDireccionCartola = new ArrayList<>();
						List<Indicador> resultindicadorComboPortafolioEnvioDireccionCartola = new ArrayList<>();
						List<Indicador> resultindicadorComboPortafolioEnvioDireccionGeneral = new ArrayList<>();
						
						for(Portafolio port :idPortafolios) {
							List<Indicador> buscarIndicador = indicadorEnvioDireccionFisica.stream()
									.filter(ind->ind.IdEntidad.equals(port.IdPortafolio))
									.collect(Collectors.toList());
							List<Indicador> buscarIndicadorCartola = indicadorEnvioDireccionCartolaFisica.stream()
									.filter(ind->ind.IdEntidad.equals(port.IdPortafolio))
									.collect(Collectors.toList());
							List<Indicador> ComboIndicadorCartola = ComboDireccionCartolaFisica.stream()
									.filter(ind->ind.IdEntidad.equals(port.IdPortafolio))
									.collect(Collectors.toList());
							List<Indicador> ComboIndicadorGeneral = ComboDireccionFisicaGeneral.stream()
									.filter(ind->ind.IdEntidad.equals(port.IdPortafolio))
									.collect(Collectors.toList());
							
							if(buscarIndicador.size()>0)resultIndicadorPortafolioEnvioDireccion.add(buscarIndicador.get(0));
							if(buscarIndicadorCartola.size()>0)resultIndicadorPortafolioEnvioDireccionCartola.add(buscarIndicadorCartola.get(0));
							if(ComboIndicadorCartola.size()>0)resultindicadorComboPortafolioEnvioDireccionCartola.add(ComboIndicadorCartola.get(0));
							if(ComboIndicadorGeneral.size()>0)resultindicadorComboPortafolioEnvioDireccionGeneral.add(ComboIndicadorGeneral.get(0));
							}
						if(indicadorEnvioDireccionFisica.size()>0 ) {
							if(indicadorEnvioDireccionFisica.get(0).ValorIndicador.equals("S") ){
								for(Indicador indCombo: resultindicadorComboPortafolioEnvioDireccionCartola) {
									SQL_UPDATE_INDICADORES_AUX = SQL_UPDATE_INDICADORES;
									SQL_UPDATE_INDICADORES_AUX = SQL_UPDATE_INDICADORES_AUX.replace("(:IdIndicador)",indCombo.IdIndicador.toString()).replace("(:ValorIndicador)", idDireccionFisicaPredeterminada.toString());
									SQL.exec(con, SQL_UPDATE_INDICADORES_AUX);
								}
							}
						}
						if(indicadorEnvioDireccionCartolaFisica.size()>0) {
							if( indicadorEnvioDireccionCartolaFisica.get(0).ValorIndicador.equals("S")) {
								for(Indicador indCombo: resultindicadorComboPortafolioEnvioDireccionGeneral) {
									SQL_UPDATE_INDICADORES_AUX = SQL_UPDATE_INDICADORES;
									SQL_UPDATE_INDICADORES_AUX = SQL_UPDATE_INDICADORES_AUX.replace("(:IdIndicador)",indCombo.IdIndicador.toString()).replace("(:ValorIndicador)", idDireccionFisicaPredeterminada.toString());
									SQL.exec(con, SQL_UPDATE_INDICADORES_AUX);
								}
							}
						}	
					}
				}
			}
		}
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearDireccionFisicaMasivo.predeterminaDireccionesFisica", fin - inicio);
	}
	
	private SalidaCrearDireccionFisicaMasivo crearEventoAutorizacion(Connection con, EntradaCrearDireccionFisicaMasivo entrada, List<DireccionFisica> direccionesFisica) throws Exception {
		logger.debug("INICIO::CrearDireccionFisicaMasivo.crearEventoAutorizacion");
		long inicio = System.currentTimeMillis();
		SalidaCrearDireccionFisicaMasivo salidaDireccionFisicaMasiva = new SalidaCrearDireccionFisicaMasivo();
		
		CrearEventoAutorizacionMasivo crearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		List<Long> idsPersona = direccionesFisica
				.parallelStream()
				.map(direccionFisica->direccionFisica.IdPersona)
				.collect(Collectors.toList());
		Map<Long, SocioNegocioUtils.Persona> personasPorIdPersona = SocioNegocioUtils.obtenerDatosPersonaPorIdPersona(con, idsPersona);
		
		CrearEventoAutorizacionMasivo.ServiceEntrada entradaCrearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo.ServiceEntrada();
		entradaCrearEventoAutorizacionMasivo.P_IDEMPRESA = entrada.p_IdEmpresa;
		entradaCrearEventoAutorizacionMasivo.P_IDMODULO = entrada.p_IdModulo;
		entradaCrearEventoAutorizacionMasivo.P_IDUSUARIO = entrada.p_IdUsuario;
		entradaCrearEventoAutorizacionMasivo.P_SIMULAROPERACION = "N";
		entradaCrearEventoAutorizacionMasivo.P_USAREGLASNEGOCIO = "N";
		
		entradaCrearEventoAutorizacionMasivo.P_TABLAEVENTOS = direccionesFisica.stream().map(direccionFisica->{
			SocioNegocioUtils.Persona persona = personasPorIdPersona.get(direccionFisica.IdPersona);
			CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS evento = new CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS();
			evento.IdTipoEvento = ID_CONCEPTO;
			evento.IdModuloSolicita = entrada.p_IdModulo;
			evento.IdUsuarioSolicita = entrada.p_IdUsuario;
			evento.Fecha = fechaSistema;
			evento.IdConcepto = ID_CONCEPTO;
			evento.NumeroOperacionConcepto = String.valueOf(direccionFisica.IdDireccionFisica);
			evento.FechaOperacionConcepto = fechaSistema;
			evento.IdCliente = direccionFisica.IdPersona;
			evento.Glosa = persona.DocumentoIdentidad + " / " + persona.NombreCompleto + " /CalleNro: " + direccionFisica.Calle + " / " + direccionFisica.Numero;
			return evento;
		}).collect(Collectors.toList());
		
		CrearEventoAutorizacionMasivo.ServiceSalida salidaCrearEventoAutorizacionMasivo = crearEventoAutorizacionMasivo.logica(con, entradaCrearEventoAutorizacionMasivo);
		
		salidaDireccionFisicaMasiva.P_OUT_ERRORESCONTADOR = salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESCONTADOR;
		salidaDireccionFisicaMasiva.P_OUT_MENSAJES.addAll(SocioNegocioUtils.obtenerMensajesAdvertenciaAutorizacion(salidaCrearEventoAutorizacionMasivo.P_OUT_ADVERTENCIASDETALLE));
		salidaDireccionFisicaMasiva.P_OUT_MENSAJES.addAll(SocioNegocioUtils.obtenerMensajesErroresAutorizacion(salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESDETALLE));
		
		if(salidaDireccionFisicaMasiva.P_OUT_ERRORESCONTADOR.equals(0L)) {
			salidaDireccionFisicaMasiva.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_AUTORIZACION,"REQUIERE_AUTORIZACION",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearDireccionFisicaMasivo.crearEventoAutorizacion", fin - inicio);
		return salidaDireccionFisicaMasiva;	
	}
	
	private void agregarLog(Connection con, EntradaCrearDireccionFisicaMasivo entrada, List<DireccionFisica> direccionesFisica) throws Exception {
		logger.trace("INICIO::CrearDireccionFisicaMasivo.agregarLog");
		long inicio = System.currentTimeMillis();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		
		for(DireccionFisica direccionFisica : direccionesFisica) {
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(direccionFisica.IdDireccionFisica, "Calle", "", direccionFisica.Calle));
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(direccionFisica.IdDireccionFisica, "Numero", "", direccionFisica.Numero));
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(direccionFisica.IdDireccionFisica, "Departamento", "", direccionFisica.Departamento));
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(direccionFisica.IdDireccionFisica, "CodigoPostal", "", direccionFisica.CodigoPostal));
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(direccionFisica.IdDireccionFisica, "IdCiudad", "", String.valueOf(direccionFisica.IdCiudad)));
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(direccionFisica.IdDireccionFisica, "IdDivisionPais", "", String.valueOf(direccionFisica.IdDivisionPais)));
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(direccionFisica.IdDireccionFisica, "CodigoProposito", "", direccionFisica.CodigoProposito));
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(direccionFisica.IdDireccionFisica, "Comentario", "", direccionFisica.Comentario));
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(direccionFisica.IdDireccionFisica, "IdEstado", "", direccionFisica.IdEstado));
			entradaAgregarLog.p_LogDetalle.add(SocioNegocioUtils.obtenerDetalleLog(direccionFisica.IdDireccionFisica, "CodigoCompuesto", "", direccionFisica.CodigoCompuesto));
			
			AgregarLog.InAgregarLog.Log log = SocioNegocioUtils.obtenerLogPorDefecto();
			log.IdCabeceraLog = direccionFisica.IdDireccionFisica;
			log.IdTipoLog = "CREACION";
			log.IdModulo = entrada.p_IdModulo;
			log.IdUsuario = entrada.p_IdUsuario;
			log.IdEmpresa = entrada.p_IdEmpresa;
			log.FechaSistemaLog = fechaSistema;
			log.IdUsuarioAtendido = entrada.p_IdUsuario;
			log.IdPersona = direccionFisica.IdPersona;
			log.IdTipoRegistro = "CREACION_DIRECCION_FISICA";
			log.EstadoRegistro = direccionFisica.IdEstadoAutorizacion;
			log.EstadoProceso = direccionFisica.IdEstadoAutorizacion;
			log.NombreProceso = "Creacion Direccion Fisica";
			log.IdRegistro = String.valueOf(direccionFisica.IdDireccionFisica);
			
			entradaAgregarLog.p_Log.add(log);
		}
				
		AgregarLog agregarLogService = new AgregarLog();
		agregarLogService.logica(con, entradaAgregarLog);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearDireccionFisicaMasivo.agregarLog", fin - inicio);
	}
	
	private List<Entidad> obtenerEntidadesInsertados(List<DireccionFisica> direccionesFisica){
		logger.trace("INICIO::CrearDireccionFisicaMasivo.obtenerIdsInsertados");
		long inicio = System.currentTimeMillis();
		
		List<Entidad> entidades = direccionesFisica
				.parallelStream()
				.map(direccionFisica->{
					Entidad entidad = new Entidad();
					entidad.IdInterno = direccionFisica.IdInterno;
					entidad.IdEntidad = direccionFisica.IdDireccionFisica;
					return entidad;
				})
				.collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearDireccionFisicaMasivo.obtenerIdsInsertados", fin - inicio);
		return entidades;
	}

}
