package cl.bcs.socionegocio.car;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.ind.ConfirmarModificarIndicador;
import cl.bcs.plataforma.ind.ModificarIndicador;
import cl.bcs.plataforma.ind.ConfirmarModificarIndicador.EntradaConfirmarModificarIndicador;
import cl.bcs.plataforma.ind.ConfirmarModificarIndicador.SalidaConfirmarModificarIndicador;
import cl.bcs.plataforma.ind.ModificarIndicador.Indicador;
import cl.bcs.plataforma.pla.CrearRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.CrearRegistroSolicitudCambio.EntradaCrearRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class ProcesarCargaMasiva implements JavaCall {

	static final Logger log = LogManager.getLogger(ProcesarCargaMasiva.class);
	
	private final String SQL_INSERT_CARGA_MASIVA_PROCESO	= loadResourceToString("cl/bcs/socionegocio/car/sql/ProcesarCargaMasiva_InsertCargaMasivaProceso.sql");
	private final String SQL_SELECT_CARGA_MASIVA_PROCESO	= loadResourceToString("cl/bcs/socionegocio/car/sql/ProcesarCargaMasiva_SelectCargaMasivaProceso.sql");
	private final String SQL_SELECT_CARGA_MASIVA_DETALLE	= loadResourceToString("cl/bcs/socionegocio/car/sql/ProcesarCargaMasiva_SelectCargaMasivaDetalle.sql");	
	private final String SQL_UPDATE_CARGA_MASIVA			= loadResourceToString("cl/bcs/socionegocio/car/sql/ProcesarCargaMasiva_UpdateCargaMasiva.sql");
	private final String SQL_UPDATE_CARGA_MASIVA_DETALLE	= loadResourceToString("cl/bcs/socionegocio/car/sql/ProcesarCargaMasiva_UpdateCargaMasivaDetalle.sql");
	private final String SQL_SELECT_INDICADOR_PROCESO		= loadResourceToString("cl/bcs/socionegocio/car/sql/ProcesarCargaMasiva_SelectIndicadorProceso.sql");
	private final String SQL_SELECT_INVALIDOS				= loadResourceToString("cl/bcs/socionegocio/car/sql/ProcesarCargaMasiva_SelectRegistrosInvalidos.sql");
	private final String SQL_INSERT_TABLA_PASO				= loadResourceToString("cl/bcs/plataforma/ind/sql/ModificarIndicador_InsertTablaPaso.sql");
	private final String SQL_INSERT_INDICADORES				= loadResourceToString("cl/bcs/plataforma/ind/sql/ModificarIndicador_InsertIndicadores.sql");
	private final String SQL_DELETE_TABLA_PASO				= loadResourceToString("cl/bcs/plataforma/ind/sql/ModificarIndicador_DeleteTablaPaso.sql");
	private final String SQL_SELECT_INDICADORES_NUEVOS		= loadResourceToString("cl/bcs/plataforma/ind/sql/ModificarIndicador_SelectIndicadoresNuevos.sql");
	private final String SQL_SELECT_INDICADORES_EXISTENTES	= loadResourceToString("cl/bcs/plataforma/ind/sql/ModificarIndicador_SelectIndicadoresExistentes.sql");
	private final String SQL_SELECT_INDICADORES_NUEVOS_NORMATIVOS_CLIENTE = loadResourceToString("cl/bcs/plataforma/ind/sql/ModificarIndicador_SelectIndicadoresNuevosNormativosCliente.sql");
	
	
    //-------------------------------------------------------------------------------------------
	// CLASES PARA ENTRADA
	//-------------------------------------------------------------------------------------------
	
	public static class ProcesarCargaMasiva_Entrada 
	{		
		public Long			p_IdEmpresa;
		public Long			p_IdUsuario;
		public String		p_IdModulo;
		public String		p_UsaReglasNegocio;
		public String		p_FechaProceso;
		public List<CargaMasivaProcesar> p_CargaMasivaProcesar;

		public ProcesarCargaMasiva_Entrada()
		{
			this.p_CargaMasivaProcesar = new ArrayList<CargaMasivaProcesar>();	
		}
			
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class CargaMasivaProcesar 
	{
		public Long			IdCargaMasiva;
		public String		CodigoArchivo; 	  
	 	  
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
    //-------------------------------------------------------------------------------------------
	// CLASES PARA SALIDA
	//-------------------------------------------------------------------------------------------
	
	public static class ProcesarCargaMasiva_Salida {

		public List<MensajeSalida>			P_OUT_MENSAJES;
		public Long							P_OUT_ERRORESCONTADOR;
		public Long							P_OUT_ADVERTENCIASCONTADOR;
		
		public ProcesarCargaMasiva_Salida()
		{	
			this.P_OUT_ERRORESCONTADOR 		= 0L;
			this.P_OUT_ADVERTENCIASCONTADOR = 0L;
			this.P_OUT_MENSAJES 			= new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	

	//-------------------------------------------------------------------------------------------
	// CLASES UTILITARIAS 
	//-------------------------------------------------------------------------------------------

	public static class CargaMasiva_Proceso
	{
		public Long			IdCargaMasiva;
		public String		CodigoTipoCargaMasiva;	
		
		public Long			IdCargaMasiva_Real;
		public String		CodigoTipoCargaMasiva_Real;
		public String		IdEstado;	
		public String		FechaProceso;
		
		public long 		errores = 0;
		public long 		advertencias = 0;
		public long 		autorizaciones = 0;
		public long 		ok = 0;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class CargaMasivaDetalle
	{
		public Long			IdCargaMasivaDetalle;
		public Long			IdCargaMasiva;
		public String		DocumentoIdentidad;
		public Long			IdPortafolio;
		public String		CodigoPortafolio;
		public String		Entidad;
		public String		CodigoIndicador;
		public String		ValorIndicador;
		public String		IdEstado;
		public String		CodigoArchivo;
		public Long			IdIngresoEgreso = 0L;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class RegistrosInvalidos extends BcsPojo{
		public Long			IdCargaMasivaDetalle;
	}

	public static class EntradaModificarIndicador extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_IdTipoEntidad;
		public Long p_IdEntidad;
		public String p_ModeloCliente;
		public List<RegistroAtributo> p_RegistroAtributo;
		
		public EntradaModificarIndicador() {
			this.p_ModeloCliente = "";
			this.p_RegistroAtributo = new ArrayList<RegistroAtributo>();
		}
	}
	
	public static class RegistroAtributo extends BcsPojo{
		public Long IdIndicador;
		public Long IdTipoIndicador;
		public String ValorIndicador;
		public java.sql.Timestamp FechaVencimiento;
	}
	
	public static class RegistroAtributoTablaPaso extends RegistroAtributo{
		public Long IdTrabajo;
		public Long IdEntidad;
		public String IdTipoEntidad;
	}
	
	public static class CargaModificarIndicador extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public List<RegistroAtributoIndicador> p_RegistroAtributoIndicador;
		
		public CargaModificarIndicador() {
			this.p_RegistroAtributoIndicador = new ArrayList<RegistroAtributoIndicador>();
		}
	}
	
	public static class RegistroAtributoIndicador extends BcsPojo{
		public Long IdIndicador;
		public Long IdTipoIndicador;
		public String ValorIndicador;
		public Long p_IdEntidad;
		public String p_IdTipoEntidad;
		public String p_ModeloCliente;
		public java.sql.Timestamp FechaVencimiento;
		
		public RegistroAtributoIndicador() {
			this.p_ModeloCliente = "";
		}
	}
	
	
	//-------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE
	//-------------------------------------------------------------------------------------------
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		
		ProcesarCargaMasiva_Entrada in = gson.fromJson(jin.toString(), ProcesarCargaMasiva_Entrada.class);
		ProcesarCargaMasiva_Salida out = logica(con, in);
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	//-------------------------------------------------------------------------------------------
	// METODOS COMPLEMANTARIOS
	//-------------------------------------------------------------------------------------------
	
	private void ProcesaRequisito(Connection con, Long idEmpresa, boolean esError, String codMsg, String CodRequisito, CargaMasiva_Proceso cargaProceso, List<MensajeSalida> P_OUT_MENSAJES) 
			throws Exception {
		if (esError) {
			String msg=Diccionario.getMensaje(con,idEmpresa,codMsg)
				  .add("IdCargaMasiva", cargaProceso.IdCargaMasiva)
				  .msg();
			
			if (CodRequisito.equals(MensajeSalida.MENSAJE_ERROR)) {
				P_OUT_MENSAJES.add(new MensajeSalida(cargaProceso.IdCargaMasiva,MensajeSalida.MENSAJE_ERROR,codMsg,msg));
				cargaProceso.errores++;
			}else if (CodRequisito.equals(MensajeSalida.MENSAJE_AUTORIZACION)) {
				P_OUT_MENSAJES.add(new MensajeSalida(cargaProceso.IdCargaMasiva,MensajeSalida.MENSAJE_AUTORIZACION,codMsg,msg));
				cargaProceso.autorizaciones++;						
			}
			else if (CodRequisito.equals(MensajeSalida.MENSAJE_ADVERTENCIA)) {
				P_OUT_MENSAJES.add(new MensajeSalida(cargaProceso.IdCargaMasiva,MensajeSalida.MENSAJE_ADVERTENCIA,codMsg,msg));
				cargaProceso.advertencias++;
			}
			else {
				cargaProceso.ok++;
			}
		}else {
			cargaProceso.ok++;
		}		
	}

	
	public void limpiarCargas(List<CargaMasiva_Proceso> cargaProceso, List<CargaMasiva_Proceso> cargaConErrores) {
		for (CargaMasiva_Proceso carga : cargaProceso) {
			if (carga.errores > 0) {
				cargaConErrores.add(carga);
			}
		}
		
		for (CargaMasiva_Proceso cargaErr : cargaConErrores) {
			if (cargaProceso.contains(cargaErr))
				cargaProceso.remove(cargaErr);
		}
	}
	
	private Long insertarTablaPaso(Connection con, EntradaModificarIndicador entrada) throws Exception {
		log.debug("INICIO::ProcesarCargaMasiva.insertarTablaPaso");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		Long idTrabajo = Double.valueOf(Math.random()*10000000000L).longValue();
		List<RegistroAtributoTablaPaso> registrosTablaPaso = entrada.p_RegistroAtributo
		.parallelStream()
		.map(registro->{
			RegistroAtributoTablaPaso registroTablaPaso = new RegistroAtributoTablaPaso();
			registroTablaPaso.IdTrabajo = idTrabajo;
			registroTablaPaso.IdEntidad = entrada.p_IdEntidad;
			registroTablaPaso.IdTipoEntidad = entrada.p_IdTipoEntidad;
			registroTablaPaso.IdIndicador = registro.IdIndicador;
			registroTablaPaso.IdTipoIndicador = registro.IdTipoIndicador;
			registroTablaPaso.ValorIndicador = registro.ValorIndicador;
			registroTablaPaso.FechaVencimiento = Fecha.toDateTime("0001-01-01 00:00:00");
			return registroTablaPaso;
		}).collect(Collectors.toList());
		sql.batch(con, SQL_INSERT_TABLA_PASO, registrosTablaPaso, RegistroAtributoTablaPaso.class);
		long fin = System.currentTimeMillis();
		log.debug("FIN::ProcesarCargaMasiva.insertarTablaPaso", fin - inicio);
		return idTrabajo;
	}
	
	private void insertarIndicadoresNuevos(Connection con, EntradaModificarIndicador entrada, List<Indicador> indicadoreNuevos) throws Exception {
		log.debug("INICIO::ProcesarCargaMasiva.insertarIndicadoresNuevos");
		long inicio = System.currentTimeMillis();
		String fechaHoraSistema = FechaDB.ObtenerFechaHoraSistemaLocal(con, entrada.p_IdEmpresa);
		List<Indicador> indicadores = indicadoreNuevos.parallelStream().map(indicador->{
			Indicador indicadorNuevo = new Indicador();
			indicadorNuevo.IdIndicador = indicador.IdIndicador;
			indicadorNuevo.IdEntidad = indicador.IdEntidad;
			indicadorNuevo.IdTipoEntidad = indicador.IdTipoEntidad;
			indicadorNuevo.IdTipoIndicador = indicador.IdTipoIndicador;
			indicadorNuevo.ModeloCliente = entrada.p_ModeloCliente;
			indicadorNuevo.IdEmpresa = entrada.p_IdEmpresa;
			indicadorNuevo.ValorIndicador = "";
			indicadorNuevo.FechaIngreso = Fecha.toDateTime(fechaHoraSistema);
			indicadorNuevo.IdEstadoAutorizacion = "PEN";
			return indicadorNuevo;
		}).collect(Collectors.toList());
		SQL sql = new SQL();
		sql.batch(con, SQL_INSERT_INDICADORES, indicadores, Indicador.class);
		long fin = System.currentTimeMillis();
		log.debug("FIN::ProcesarCargaMasiva.insertarIndicadoresNuevos", fin - inicio);
	}
	
	private void insertarIndicadoresNuevosNormativosCliente(Connection con, EntradaModificarIndicador entrada, Long idTrabajo) throws Exception {
		log.debug("INICIO::currentTimeMillis.insertarIndicadoresNuevosNormativosCliente");
		long inicio = System.currentTimeMillis();
		if("PERSONA".equals(entrada.p_IdTipoEntidad)) {
			HashMap<String,Object> parametros = new HashMap<String,Object>(1);
			parametros.put("IdTrabajo", idTrabajo);
			SQL sql = new SQL();
			List<Indicador> indicadoresClienteNuevos = sql.query(con, SQL_SELECT_INDICADORES_NUEVOS_NORMATIVOS_CLIENTE, parametros, Indicador.class);
			String fechaHoraSistema = FechaDB.ObtenerFechaHoraSistemaLocal(con, entrada.p_IdEmpresa);
			List<Indicador> indicadores = indicadoresClienteNuevos.parallelStream().map(indicador->{
				Indicador indicadorNuevo = new Indicador();
				indicadorNuevo.IdIndicador = indicador.IdIndicador;
				indicadorNuevo.IdEntidad = indicador.IdEntidad;
				indicadorNuevo.IdTipoEntidad = "CLIENTE";
				indicadorNuevo.IdTipoIndicador = indicador.IdTipoIndicador;
				indicadorNuevo.ModeloCliente = entrada.p_ModeloCliente;
				indicadorNuevo.IdEmpresa = entrada.p_IdEmpresa;
				indicadorNuevo.ValorIndicador = "";
				indicadorNuevo.FechaIngreso = Fecha.toDateTime(fechaHoraSistema);
				indicadorNuevo.IdEstadoAutorizacion = "ING";
				return indicadorNuevo;
			}).collect(Collectors.toList());
			sql.batch(con, SQL_INSERT_INDICADORES, indicadores, Indicador.class);
		}
		long fin = System.currentTimeMillis();
		log.debug("FIN::currentTimeMillis.insertarIndicadoresNuevosNormativosCliente", fin - inicio);
	}
	
	private List<Indicador> buscarIndicadoresNuevos(Connection con, Long idTrabajo) throws Exception {
		log.debug("INICIO::ProcesarCargaMasiva.buscarIndicadoresNuevos");
		long inicio = System.currentTimeMillis();
		HashMap<String,Object> parametros = new HashMap<String,Object>(1);
		parametros.put("IdTrabajo", idTrabajo);
		SQL sql = new SQL();
		List<Indicador> indicadoresNuevos = sql.query(con, SQL_SELECT_INDICADORES_NUEVOS, parametros, Indicador.class);
		long fin = System.currentTimeMillis();
		log.debug("FIN::ProcesarCargaMasiva.buscarIndicadoresNuevos", fin - inicio);
		return indicadoresNuevos;
	}
	
	private List<Indicador> buscarIndicadoresExistentes(Connection con, Long idTrabajo) throws Exception {
		log.debug("INICIO::ProcesarCargaMasiva.buscarIndicadoresExistentes");
		long inicio = System.currentTimeMillis();
		HashMap<String,Object> parametros = new HashMap<String,Object>(1);
		parametros.put("IdTrabajo", idTrabajo);
		SQL sql = new SQL();
		List<Indicador> indicadoresNuevos = sql.query(con, SQL_SELECT_INDICADORES_EXISTENTES, parametros, Indicador.class);
		long fin = System.currentTimeMillis();
		log.debug("FIN::ProcesarCargaMasiva.buscarIndicadoresExistentes", fin - inicio);
		return indicadoresNuevos;
	}
	
	private List<Indicador> obtenerIndicadoresSinAutorizacion(List<Indicador> indicadoresNuevos, List<Indicador> indicadoresExistentes){
		log.debug("INICIO::ProcesarCargaMasiva.obtenerIndicadoresSinAutorizacion");
		long inicio = System.currentTimeMillis();
		List<Indicador> indicadoresSinAutorizacion = new ArrayList<Indicador>();
		indicadoresSinAutorizacion.addAll(indicadoresNuevos.parallelStream().filter(indicador->"N".equals(indicador.RequiereAutorizacion)).collect(Collectors.toList()));
		indicadoresSinAutorizacion.addAll(indicadoresExistentes.parallelStream().filter(indicador->"N".equals(indicador.RequiereAutorizacion)).collect(Collectors.toList()));
		long fin = System.currentTimeMillis();
		log.debug("FIN::ProcesarCargaMasiva.obtenerIndicadoresSinAutorizacion", fin - inicio);
		return indicadoresSinAutorizacion;
	}
	
	private void borrarTablaPaso(Connection con, Long idTrabajo) throws Exception {
		log.debug("INICIO::ProcesarCargaMasiva.borrarTablaPaso");
		long inicio = System.currentTimeMillis();
		RegistroAtributoTablaPaso registroTablaPaso = new RegistroAtributoTablaPaso();
		registroTablaPaso.IdTrabajo = idTrabajo;
		List<RegistroAtributoTablaPaso> registrosTablaPaso = new ArrayList<RegistroAtributoTablaPaso>();
		registrosTablaPaso.add(registroTablaPaso);
		SQL sql = new SQL();
		sql.batch(con, SQL_DELETE_TABLA_PASO, registrosTablaPaso, RegistroAtributoTablaPaso.class);
		long fin = System.currentTimeMillis();
		log.debug("FIN::ProcesarCargaMasiva.borrarTablaPaso", fin - inicio);
	}
	
	private Long crearSolicitudCambio(Connection con, List<Indicador> indicadores) throws Exception {
		log.debug("INICIO::ProcesarCargaMasiva.crearSolicitudCambio");
		long inicio = System.currentTimeMillis();
		Long idSolicitudCambioSalida = null;
		if(indicadores.size() > 0) {
			List<Long> secuenciasSolicitudCambio = Utils.getSecuencia(con, "BSCL.Plataforma.BD.Secuencias::PLA.IdSolicitudCambio", 1L);
			Long idSolicitudCambio = secuenciasSolicitudCambio.get(0);
			EntradaCrearRegistroSolicitudCambio entradaCrearRegistroSolicitudCambio = new EntradaCrearRegistroSolicitudCambio();
			idSolicitudCambioSalida = idSolicitudCambio;
			
			List<CrearRegistroSolicitudCambio.RegistroSolicitudCambio> solicitudesValorIndicador = indicadores.parallelStream().map(indicador->{
				CrearRegistroSolicitudCambio.RegistroSolicitudCambio registroSolicitudCambio = new CrearRegistroSolicitudCambio.RegistroSolicitudCambio();
				registroSolicitudCambio.IdSolicitudCambio = idSolicitudCambio;
				registroSolicitudCambio.IdTipoEntidad = "INDICADOR";
				registroSolicitudCambio.IdEntidadOrigen = String.valueOf(indicador.IdIndicador);
				registroSolicitudCambio.CampoModificado = "ValorIndicador";
				registroSolicitudCambio.TipoDato = "NVARCHAR";
				registroSolicitudCambio.ValorAntiguo = indicador.ValorAntiguo;
				registroSolicitudCambio.ValorNuevo = indicador.ValorIndicador;
				registroSolicitudCambio.IdEstadoAutorizacion = "PEN";
				return registroSolicitudCambio;
			}).collect(Collectors.toList());
			
			List<CrearRegistroSolicitudCambio.RegistroSolicitudCambio> solicitudesFechaVencimiento = indicadores.parallelStream().map(indicador->{
				CrearRegistroSolicitudCambio.RegistroSolicitudCambio registroSolicitudCambio = new CrearRegistroSolicitudCambio.RegistroSolicitudCambio();
				registroSolicitudCambio.IdSolicitudCambio = idSolicitudCambio;
				registroSolicitudCambio.IdTipoEntidad = "INDICADOR";
				registroSolicitudCambio.IdEntidadOrigen = String.valueOf(indicador.IdIndicador);
				registroSolicitudCambio.CampoModificado = "FechaVencimiento";
				registroSolicitudCambio.TipoDato = "SECONDDATE";
				registroSolicitudCambio.ValorAntiguo = Fecha.toStringDatetime(indicador.FechaVencimientoAntiguo);
				registroSolicitudCambio.ValorNuevo = Fecha.toStringDatetime(indicador.FechaVencimiento);
				registroSolicitudCambio.IdEstadoAutorizacion = "PEN";
				return registroSolicitudCambio;
			}).collect(Collectors.toList());
			entradaCrearRegistroSolicitudCambio.p_RegistroSolicitudCambio.addAll(solicitudesValorIndicador);
			entradaCrearRegistroSolicitudCambio.p_RegistroSolicitudCambio.addAll(solicitudesFechaVencimiento);
			CrearRegistroSolicitudCambio crearRegistroSolicitudCambio = new CrearRegistroSolicitudCambio();
			crearRegistroSolicitudCambio.logica(con, entradaCrearRegistroSolicitudCambio);
		}
		long fin = System.currentTimeMillis();
		log.debug("FIN::ProcesarCargaMasiva.crearSolicitudCambio", fin - inicio);
		return idSolicitudCambioSalida;
	}
	
	private SalidaConfirmarModificarIndicador confirmarModificarIndicadorSinAutorizacion(Connection con, EntradaModificarIndicador entrada, Long idSolicitudCambio) throws Exception {
		log.debug("INICIO::ModificarIndicador.confirmarModificarIndicadorSinAutorizacion");
		long inicio = System.currentTimeMillis();
		SalidaConfirmarModificarIndicador salidaConfirmarModificarIndicador = new SalidaConfirmarModificarIndicador();
		
		if(idSolicitudCambio != null) {
			EntradaConfirmarModificarIndicador entradaConfirmarModificarIndicador = new EntradaConfirmarModificarIndicador();
			entradaConfirmarModificarIndicador.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaConfirmarModificarIndicador.p_IdUsuario = entrada.p_IdUsuario;
			entradaConfirmarModificarIndicador.p_IdModulo = entrada.p_IdModulo;
			entradaConfirmarModificarIndicador.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
			entradaConfirmarModificarIndicador.p_IdSolicitudCambio = idSolicitudCambio;
			entradaConfirmarModificarIndicador.p_IdEstadoAutorizacion = "ING";
			entradaConfirmarModificarIndicador.p_IdTipoEntidad = entrada.p_IdTipoEntidad;
			entradaConfirmarModificarIndicador.p_IdEntidad = 0L;
			ConfirmarModificarIndicador confirmarModificarIndicador = new ConfirmarModificarIndicador();
			salidaConfirmarModificarIndicador = confirmarModificarIndicador.logica(con, entradaConfirmarModificarIndicador);
		}
		
		long fin = System.currentTimeMillis();
		log.debug("FIN::ModificarIndicador.confirmarModificarIndicadorSinAutorizacion", fin - inicio);
		return salidaConfirmarModificarIndicador;
	}
	
	private List<EntradaModificarIndicador> buscarModificarIndicador(Connection con, ProcesarCargaMasiva_Entrada in) throws Exception {
		log.debug("INICIO::ProcesarCargaMasiva.buscarModificarIndicador");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		HashMap<String,Object> parametros = new HashMap<String,Object>(1);
		CargaModificarIndicador cargaModificarIndicador = new CargaModificarIndicador();
		in.p_CargaMasivaProcesar.forEach(e -> {
			parametros.clear();
			parametros.put("IdCargaMasiva", e.IdCargaMasiva);
			cargaModificarIndicador.p_IdEmpresa = in.p_IdEmpresa;
			cargaModificarIndicador.p_IdUsuario = in.p_IdUsuario;
			cargaModificarIndicador.p_IdModulo = in.p_IdModulo;
			cargaModificarIndicador.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
			try {
				cargaModificarIndicador.p_RegistroAtributoIndicador = sql.query(con, SQL_SELECT_INDICADOR_PROCESO, parametros, RegistroAtributoIndicador.class); // mas cargas masivas
			} catch (Exception er) {
				log.error("ERROR::ProcesarCargaMasiva.buscarModificarIndicador", er);
			}
		});
		ArrayList<Long> idsCargaMasiva = new ArrayList<Long>();
		cargaModificarIndicador.p_RegistroAtributoIndicador.forEach(e -> {
			if (!idsCargaMasiva.contains(e.p_IdEntidad)) {
				idsCargaMasiva.add(e.p_IdEntidad);
			}
		});
		List<EntradaModificarIndicador> entradasModificarIndicador = idsCargaMasiva.parallelStream().map(e -> {
			EntradaModificarIndicador entradaModificar = new EntradaModificarIndicador();
			entradaModificar.p_IdEmpresa = in.p_IdEmpresa;
			entradaModificar.p_IdUsuario = in.p_IdUsuario;
			entradaModificar.p_IdModulo = in.p_IdModulo;
			entradaModificar.p_UsaReglasNegocio = in.p_UsaReglasNegocio;
			entradaModificar.p_IdEntidad = e;
			cargaModificarIndicador.p_RegistroAtributoIndicador.forEach(i -> {
				if (i.p_IdEntidad.equals(e)) {
					RegistroAtributo registro = new RegistroAtributo();
					entradaModificar.p_IdTipoEntidad = i.p_IdTipoEntidad;
					registro.IdIndicador = i.IdIndicador;
					registro.IdTipoIndicador = i.IdTipoIndicador;
					registro.ValorIndicador = i.ValorIndicador;
					registro.FechaVencimiento = Fecha.toDateTime("0001-01-01 00:00:00");
					entradaModificar.p_RegistroAtributo.add(registro);
				}
			});
			return entradaModificar;
		}).collect(Collectors.toList());
		long fin = System.currentTimeMillis();
		log.debug("FIN::ProcesarCargaMasiva.buscarModificarIndicador", fin - inicio);
		return entradasModificarIndicador;
	}
	
    
	//-------------------------------------------------------------------------------------------
	// LOGICA DEL SERVICIO
	//-------------------------------------------------------------------------------------------
	
	public ProcesarCargaMasiva_Salida logica(Connection con, ProcesarCargaMasiva_Entrada in) throws Exception {
		
		final Logger log = LogManager.getLogger(ModificarIndicador.class);
		
		log.info("== INICIO PROCESAR CARGA MASIVA  ==");		
		log.info("ENTRADA > " + in.toString());
		
		SQL _sql = new SQL();
		ProcesarCargaMasiva_Salida out = new ProcesarCargaMasiva_Salida();
		
		List<CargaMasiva_Proceso> cargaMasiva_Proceso = new ArrayList<>();
		List<CargaMasiva_Proceso> cargaMasiva_ConErrores = new ArrayList<>();		
		HashMap<String,Object> params = new HashMap<>();
		
		
		//Si usa regla de negocio realiza validaciones basicas
		if ("S".equalsIgnoreCase(in.p_UsaReglasNegocio))
		{
			RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
			RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
			
			if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
				log.info("NO CUMPLE REQUISITOS BASICOS");
				out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
				return out;
			}
						
			if (in.p_CargaMasivaProcesar.isEmpty()) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_CARGAS",null,con, in.p_IdEmpresa));
				return out;
			}
		}
			
		//Inserta parametros de entrada a tabla global
		_sql.batch(con, SQL_INSERT_CARGA_MASIVA_PROCESO, in.p_CargaMasivaProcesar , CargaMasivaProcesar.class);
		
		
		//Obtiene datos de carga masiva para proceso
		params.clear();
		for (CargaMasivaProcesar carga : in.p_CargaMasivaProcesar) {
			params.put("IdCargaMasiva", carga.IdCargaMasiva);
		}	
			
		cargaMasiva_Proceso = _sql.query(con, SQL_SELECT_CARGA_MASIVA_PROCESO, params, CargaMasiva_Proceso.class);
		log.info("DATOS PROCESO > " + cargaMasiva_Proceso);
		
		
		//Revisa estado de validacion
		for (CargaMasiva_Proceso carga : cargaMasiva_Proceso) 
		{
			ProcesaRequisito(con, in.p_IdEmpresa,(carga.IdCargaMasiva_Real.compareTo(0L) == 0), "CARGA_MASIVA_NO_EXISTE", MensajeSalida.MENSAJE_ERROR, carga, out.P_OUT_MENSAJES);				
			ProcesaRequisito(con, in.p_IdEmpresa,(carga.CodigoTipoCargaMasiva.equalsIgnoreCase("")), "TIPO_CARGA_MASIVA_NO_EXISTE", MensajeSalida.MENSAJE_ERROR, carga, out.P_OUT_MENSAJES);				
			ProcesaRequisito(con, in.p_IdEmpresa,("PRO".equalsIgnoreCase(carga.IdEstado)), "CARGA_MASIVA_PROCESADA", MensajeSalida.MENSAJE_ERROR, carga, out.P_OUT_MENSAJES);			
		}
		limpiarCargas(cargaMasiva_Proceso, cargaMasiva_ConErrores);
		
		if (cargaMasiva_Proceso.isEmpty()) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR, "CARGAS_MASIVAS_CON_ERRORES", null, con, in.p_IdEmpresa));
			return out;
		}
		
		List<Long> idsCargaOk = new ArrayList<>();
		for (CargaMasiva_Proceso carga : cargaMasiva_Proceso) {			
			if (!idsCargaOk.contains(carga.IdCargaMasiva)) {
				idsCargaOk.add(carga.IdCargaMasiva);
			}
		}
		
		
		// --------------------------------------------------------------------------------
		// OBTIENE DETALLE DE CARGA MASIVA A PROCESAR
		
		params.clear();
		params.put("IdCargaMasiva", idsCargaOk);
		
		List<CargaMasivaDetalle> CargaMasivaDetalle = new ArrayList<CargaMasivaDetalle>();
		CargaMasivaDetalle = _sql.query(con, SQL_SELECT_CARGA_MASIVA_DETALLE, params, CargaMasivaDetalle.class);
		log.info("CARGA MASIVA DETALLE > " + CargaMasivaDetalle);
		
		
		// --------------------------------------------------------------------------------
		// OBTIENE LOS INDICADORES A PROCESAR

		List<EntradaModificarIndicador> objIn = buscarModificarIndicador(con,in);
		objIn.forEach (e ->{
			Long idTrabajo = 0L;
			try {
				idTrabajo = insertarTablaPaso(con, e);
				List<Indicador> indicadoresNuevos = buscarIndicadoresNuevos(con, idTrabajo);
				List<Indicador> indicadoresExistentes = buscarIndicadoresExistentes(con, idTrabajo);
				insertarIndicadoresNuevos(con,e,indicadoresNuevos);
				insertarIndicadoresNuevosNormativosCliente(con,e,idTrabajo);
				List<Indicador> indicadoresSinAutorizacion = obtenerIndicadoresSinAutorizacion(indicadoresNuevos, indicadoresExistentes);
				Long idSecuenciaCambiosSinAutorizacion = crearSolicitudCambio(con, indicadoresSinAutorizacion);
				SalidaConfirmarModificarIndicador salidaConfirmarModificarIndicadorSinAutorizacion = confirmarModificarIndicadorSinAutorizacion(con, e, idSecuenciaCambiosSinAutorizacion);
				out.P_OUT_ERRORESCONTADOR += salidaConfirmarModificarIndicadorSinAutorizacion.P_OUT_ERRORESCONTADOR;
				if (out.P_OUT_MENSAJES!=null)
					out.P_OUT_MENSAJES.addAll(salidaConfirmarModificarIndicadorSinAutorizacion.P_OUT_MENSAJES);
			} catch(Exception er) {
				log.error("ERROR::ModificarIndicador", er);
			} finally {
				try {
					borrarTablaPaso(con, idTrabajo);
				} catch (Exception er) {
					log.error("ERROR::borrarTablaPaso", er);
				}
			}
		});
		
		params.clear();
		params.put("IdCargaMasiva", idsCargaOk);
		List<RegistrosInvalidos> advertencias = _sql.query(con, SQL_SELECT_INVALIDOS, params, RegistrosInvalidos.class);	

		if (advertencias.size()>0){
			out.P_OUT_ADVERTENCIASCONTADOR += 1;
			out.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ADVERTENCIA, "UI_CARGA_MASIVA_PROCESAR_INV", "Los registros inválidos no fueron procesados"));
		}
		
		
		// --------------------------------------------------------------------------------
		// ACTUALIZA CARGA MASIVA		

		List<CargaMasiva_Proceso> lstCargaActualizar = new ArrayList<CargaMasiva_Proceso>();
		
		for (Long idCarga: idsCargaOk)
		{
			CargaMasiva_Proceso objCarga = new CargaMasiva_Proceso();
			objCarga.IdCargaMasiva 	= idCarga;
			objCarga.FechaProceso 	= in.p_FechaProceso;
			lstCargaActualizar.add(objCarga);
		}
		
		if (!lstCargaActualizar.isEmpty())
		{
			_sql.batch(con, SQL_UPDATE_CARGA_MASIVA, lstCargaActualizar , CargaMasiva_Proceso.class);	
		}
		
		
		// --------------------------------------------------------------------------------
		// ACTUALIZA CARGA MASIVA DETALLE
		
		if (!CargaMasivaDetalle.isEmpty())
		{
			_sql.batch(con, SQL_UPDATE_CARGA_MASIVA_DETALLE, CargaMasivaDetalle , CargaMasivaDetalle.class);
		}


		log.info("SALIDA > " + out);
		log.info("== TERMINO PROCESAR CARGA MASIVA ==");
		
		return out;
	}
	
	
	//-------------------------------------------------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	//-------------------------------------------------------------------------------------------	
	
	public static void main(String[] args) throws Exception 
	{
		System.out.println("== INICIO PRUEBA LOCAL ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection()) 
		{
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":6,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"S\",\"p_CargaMasivaProcesar\":[{\"IdCargaMasiva\":88,\"CodigoArchivo\":\"CARGA_MASIVA_INDICADORES_SOCIO_NEGOCIO\"}],\"p_FechaProceso\":\"2023-04-17\"}";
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":51,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"S\",\"p_CargaMasivaProcesar\":[{\"IdCargaMasiva\":38,\"CodigoArchivo\":\"CARGA_MASIVA_INDICADORES_SOCIO_NEGOCIO\"}],\"p_FechaProceso\":\"2023-03-27\"}";

			ProcesarCargaMasiva_Entrada Objin = gson.fromJson(json, ProcesarCargaMasiva_Entrada.class);
			ProcesarCargaMasiva demo = new ProcesarCargaMasiva();
			
 			System.out.println("ENTRADA: " + Objin.toString());
 			
 			try 
 			{
 				ProcesarCargaMasiva_Salida ObjOut = demo.logica(con, Objin);
 				System.out.println(ObjOut.toString());
// 				con.commit();
 				con.rollback();
 			}
 			catch(Exception er) 
 			{
				er.printStackTrace();
			}
 			con.close(); 			
	    }		 	
		System.out.println("== FIN PRUEBA LOCAL ==");
	}
	

}
