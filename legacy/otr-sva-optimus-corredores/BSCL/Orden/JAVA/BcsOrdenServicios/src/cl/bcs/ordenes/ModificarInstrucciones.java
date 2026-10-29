package cl.bcs.ordenes;

import cl.bcs.ordenes.pojo.Instruccion;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.per.pojo.Entidad;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class ModificarInstrucciones implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(ModificarInstrucciones.class);
	static class _CONST  {
		public final String IdEstadoInstruccionIngreso = "ING";
		public final String IdEstadoInstruccionModificar = "MOD";
		public final String IdTipoLogIngreso = "INGRESO";
		public final String IdTipoLogModificacion = "MODIFICACION";
		public final String IdModulo = "ORD";
		public final String IdTipoRegistro = "INSTRUCCION";
		public final String MensajeBitacoraAgente = "Instruccion modificada correctamente por el Agente.";
		public final String MensajeBitacoraCliente = "Instruccion modificada correctamente por el Cliente.";
		
	};
	
	private final _CONST constantes = new _CONST();
	
	private final String SQL_UPDATE_INSTRUCCIONES = loadResourceToString("cl/bcs/ordenes/sql/ModificarInstrucciones_UpdateInstrucciones.sql");
	private final String SQL_SELECT_INSTRUCCIONES = loadResourceToString("cl/bcs/ordenes/sql/ModificarInstrucciones_SelectInstrucciones.sql");
	private final String SQL_SELECT_TIPO_OPERACION = loadResourceToString("cl/bcs/ordenes/sql/CrearInstrucciones_SelectTipoOperacion.sql");
	private final String SQL_SELECT_MOTIVO_INSTRUCCION = loadResourceToString("cl/bcs/ordenes/sql/CrearInstrucciones_SelectMotivoInstruccion.sql");

	
	public static class EntradaModificarInstrucciones extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_IdTipoCanalIngreso;

		public List<Instruccion> p_Instrucciones;
		
		public EntradaModificarInstrucciones() {
			this.p_Instrucciones = new ArrayList<>();
		}
	}
	
	public static class SalidaModificarInstrucciones extends BcsPojo {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long	P_OUT_ERRORESCONTADOR;
		public List<Entidad> P_OUT_REFID;
		
		public SalidaModificarInstrucciones() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<>();
			this.P_OUT_REFID = new ArrayList<>();
		}

	}
	public static class TipoOperacion extends BcsPojo {
		public String IdTipoOperacion;
		public String Nombre;
		public String Accion;
		public String IdTipoProducto;
		
	}
	
	public static class InstruccionMotivo extends BcsPojo {
		public Long IdMotivo;
		public String IdEstado;
		public String Motivo;
		public String Tipo;
		public String IdTipoCanalIngreso;
		public String Activo;
		public String CodigoEstado;
		
	}
	
	
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaModificarInstrucciones in = gson.fromJson(jin.toString(), EntradaModificarInstrucciones.class);
		SalidaModificarInstrucciones out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaModificarInstrucciones logica(Connection conn, EntradaModificarInstrucciones in) throws Exception {
		logger.info("INICIO::CrearInstrucciones");
		long inicioServicio = System.currentTimeMillis();

		java.sql.Timestamp fechaHoraSistema = FechaDB.ObtenerFechaHoraSistemaTs(conn, in.p_IdEmpresa);
		SalidaModificarInstrucciones salida = new SalidaModificarInstrucciones();

		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(conn, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			salida.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			return salida;
		}
		
		List<Instruccion> instrucciones = obtenerInstrucciones(conn, in.p_Instrucciones, in.p_IdTipoCanalIngreso, in.p_IdEmpresa, fechaHoraSistema);
		obtenerTipoOperacion(conn, instrucciones);
		
		if (instrucciones.size() == 0) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_ENCONTRARON_INSTRUCCIONES",null,conn,in.p_IdEmpresa));
			return salida;
		}
		obtenerInstruccionMotivo(conn, instrucciones);
		
		actualizarInstrucciones(conn,instrucciones);
		
		List<Entidad> instruccionesActualizadas = obtenerEntidadesInsertados(instrucciones);
		salida.P_OUT_REFID = instruccionesActualizadas;
		
		agregarBitacoras(conn, in, instruccionesActualizadas);
		agregarLog(conn, in, instrucciones);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarInstrucciones", finServicio - inicioServicio);
		return salida;
	}
	
	private List<Instruccion> obtenerInstrucciones (Connection conn, List<Instruccion> instruccionesEntrada, String idCanalIngreso, Long idEmpresa, java.sql.Timestamp fechaHoraSistema) throws Exception{
		logger.trace("INICIO::CrearInstruccion.obtenerInstrucciones");
		long inicio = System.currentTimeMillis();
		
		SQL _sql = new SQL();
		
		List<Long> idInstrucciones = new ArrayList<>();
		instruccionesEntrada.forEach(inst -> {
			idInstrucciones.add(inst.IdInstruccion);
		});
		
		HashMap<String,Object> params = new HashMap<>();
		params.put("idsInstrucciones", idInstrucciones);
		List<Instruccion> instrucciones = _sql.query(conn, SQL_SELECT_INSTRUCCIONES, params, Instruccion.class);
		
		List<Instruccion> instruccionesActualizadas = instruccionesEntrada.parallelStream().map(instruccionEntrada -> {
			Instruccion instruccion = obtenerEntidadInstruccion(instruccionEntrada, instruccionEntrada.IdInstruccion, idCanalIngreso, idEmpresa, fechaHoraSistema);
			return instruccion;
		}).collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::CrearInstruccion.obtenerInstrucciones", fin - inicio);
		return instruccionesActualizadas;
	}

	private void obtenerTipoOperacion(Connection conn, List<Instruccion> instruccion) throws SQLException {
		logger.debug("INICIO::CrearInstruccion.obtenerTipoOperacion");
		long inicio = System.currentTimeMillis();

		SQL sql = new SQL();
		Map<String, TipoOperacion> tipoOperacionPorIdTipoOperacion = sql.query(conn, SQL_SELECT_TIPO_OPERACION, new HashMap<>(), TipoOperacion.class)
				.stream()
				.collect(Collectors.toMap(tipo -> tipo.IdTipoOperacion, tipo -> tipo));

		instruccion.forEach(inst -> {
			String tipo = inst.IdClasificacionTipoInstrumento;
			TipoOperacion tipoOperacion = tipoOperacionPorIdTipoOperacion.get(inst.IdTipoOperacionInstruccion);
			String accion = tipoOperacion.Accion;

			StringBuilder sb = new StringBuilder();
			sb.append(inst.IdTipoOperacion);

			if ("RV".equals(tipo) && ("COMPRA".equalsIgnoreCase(accion) || "VENTA".equalsIgnoreCase(accion))) {
				sb.setLength(0);
				sb.append(accion, 0, 3).append(inst.IdTipoInstrumento);

			} else if ("IF".equals(tipo)) {
				sb.setLength(0);
				sb.append(accion, 0, 3).append("IIF");

			} else if ("RF".equals(tipo)) {
				sb.setLength(0);
				sb.append(accion, 0, 3).append("RF");
			}

			inst.IdTipoOperacion = sb.toString();
		});

		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearInstruccion.obtenerTipoOperacion", fin - inicio);
	}
	
	private Instruccion obtenerEntidadInstruccion(Instruccion instruccionEntrada, Long idInstruccion, String idCanalIngreso, Long idEmpresa, java.sql.Timestamp fechaHoraSistema) {
		logger.trace("INICIO::CrearInstruccion.obtenerEntidadInstruccion");
		long inicio = System.currentTimeMillis();
		
		Instruccion entidadInstruccion = new Instruccion();
		entidadInstruccion.IdInterno = instruccionEntrada.IdInterno;
		entidadInstruccion.IdInstruccion = idInstruccion;
		if("CLI".equalsIgnoreCase(idCanalIngreso))
			entidadInstruccion.IdEstadoInstruccion = constantes.IdEstadoInstruccionModificar;
		else
			entidadInstruccion.IdEstadoInstruccion = constantes.IdEstadoInstruccionIngreso;
		entidadInstruccion.FechaTrade = instruccionEntrada.FechaTrade;
		entidadInstruccion.FechaLiquidacion = instruccionEntrada.FechaLiquidacion;
		entidadInstruccion.FechaVencimiento = instruccionEntrada.FechaVencimiento;
		entidadInstruccion.PlazoVencimiento = instruccionEntrada.PlazoVencimiento;
		entidadInstruccion.IdClasificacionTipoInstrumento = instruccionEntrada.IdClasificacionTipoInstrumento;
		entidadInstruccion.IdClasificacionTipoInstrumentoInstruccion = instruccionEntrada.IdClasificacionTipoInstrumentoInstruccion;
		entidadInstruccion.IdTipoInstrumentoInstruccion = instruccionEntrada.IdTipoInstrumentoInstruccion;
		entidadInstruccion.IdTipoInstrumento = instruccionEntrada.IdTipoInstrumento;
		entidadInstruccion.IdTipoOperacion = instruccionEntrada.IdTipoOperacion;
		entidadInstruccion.IdTipoOperacionInstruccion = instruccionEntrada.IdTipoOperacionInstruccion;
		entidadInstruccion.IdInstrumento = instruccionEntrada.IdInstrumento;
		entidadInstruccion.IdEmisor = instruccionEntrada.IdEmisor;
		entidadInstruccion.Nemotecnico = instruccionEntrada.Nemotecnico;
		entidadInstruccion.IdCliente = instruccionEntrada.IdCliente;
		entidadInstruccion.IdEstadoCliente =instruccionEntrada.IdEstadoCliente;
		entidadInstruccion.IdPortafolioCliente = instruccionEntrada.IdPortafolioCliente;
		entidadInstruccion.IdEstadoPortafolio = instruccionEntrada.IdEstadoPortafolio;
		entidadInstruccion.IdCuentaCustodioPortafolio = instruccionEntrada.IdCuentaCustodioPortafolio;
		entidadInstruccion.DocumentoIdentidad = instruccionEntrada.DocumentoIdentidad;
		entidadInstruccion.NombreCompleto = instruccionEntrada.NombreCompleto;
		entidadInstruccion.IdBolsa = instruccionEntrada.IdBolsa;
		entidadInstruccion.IdBoveda = instruccionEntrada.IdBoveda;
		entidadInstruccion.IdIntermediario = instruccionEntrada.IdIntermediario;
		entidadInstruccion.FolioFactura = instruccionEntrada.FolioFactura;
		entidadInstruccion.IdMonedaLiquidacion = instruccionEntrada.IdMonedaLiquidacion;
		entidadInstruccion.IdCondicionDeLiquidacion = instruccionEntrada.IdCondicionDeLiquidacion;
		entidadInstruccion.Cantidad = instruccionEntrada.Cantidad;
		entidadInstruccion.PrecioInstruccion = instruccionEntrada.PrecioInstruccion;
		entidadInstruccion.MontoNeto = instruccionEntrada.MontoNeto;
		entidadInstruccion.TotalCobros = instruccionEntrada.TotalCobros;
		entidadInstruccion.MontoLiquidacion = instruccionEntrada.MontoLiquidacion;
		entidadInstruccion.TirTasa = instruccionEntrada.TirTasa;
		entidadInstruccion.ValorRescate = instruccionEntrada.ValorRescate;
		entidadInstruccion.ValorNominal = instruccionEntrada.ValorNominal;
		entidadInstruccion.IdTipoReajuste = instruccionEntrada.IdTipoReajuste;
		entidadInstruccion.IdTipoCanalIngreso = idCanalIngreso;
		entidadInstruccion.IdTipoCanalDistribucion = instruccionEntrada.IdTipoCanalDistribucion;
		entidadInstruccion.IdEmpresa = idEmpresa;
		entidadInstruccion.FechaIngreso = fechaHoraSistema;
		entidadInstruccion.Glosa = instruccionEntrada.Glosa;
		entidadInstruccion.EsFungible = instruccionEntrada.EsFungible;
		entidadInstruccion.CorteMinimo = instruccionEntrada.CorteMinimo;
		entidadInstruccion.CodMotivoInstruccion = instruccionEntrada.CodMotivoInstruccion;
				
		long fin = System.currentTimeMillis();
		logger.info("FIN::CrearInstruccion.obtenerEntidadInstruccion", fin - inicio);
		return entidadInstruccion;
	}
	
	private void actualizarInstrucciones (Connection conn, List<Instruccion> instrucciones) throws Exception{
		logger.debug("INICIO::CrearInstruccion.insertarInstrucciones");
		long inicio = System.currentTimeMillis();
		
		
		
		SQL sql = new SQL();
		sql.batch(conn, SQL_UPDATE_INSTRUCCIONES, instrucciones, Instruccion.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearInstruccion.insertarInstrucciones", fin - inicio);
	}
	
	private List<Entidad> obtenerEntidadesInsertados(List<Instruccion> instrucciones){
		logger.trace("INICIO::CrearInstruccion.obtenerEntidadesInsertados");
		long inicio = System.currentTimeMillis();
		
		List<Entidad> entidades = instrucciones.parallelStream().map(instruccion->{
			Entidad entidad = new Entidad();
			entidad.IdInterno = instruccion.IdInterno;
			entidad.IdEntidad = instruccion.IdInstruccion;
			return entidad;
		}).collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearInstruccion.obtenerEntidadesInsertados", fin - inicio);
		return entidades;
	}
	
	private void agregarLog(Connection con, EntradaModificarInstrucciones entrada, List<Instruccion> instrucciones) throws Exception {
		logger.debug("INICIO::CrearInstruccion.agregarLog");
		long inicio = System.currentTimeMillis();
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		entradaAgregarLog.p_Log = instrucciones.parallelStream().map(instruccion->{
			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
			log.IdCabeceraLog = instruccion.IdInstruccion;
			if("CLI".equalsIgnoreCase(entrada.p_IdTipoCanalIngreso))
				log.IdTipoLog = constantes.IdTipoLogModificacion;
			else
				log.IdTipoLog = constantes.IdTipoLogIngreso;
			log.IdModulo = entrada.p_IdModulo;
			log.IdUsuario = entrada.p_IdUsuario;
			log.IdEmpresa = entrada.p_IdEmpresa;
			log.FechaSistemaLog = fechaSistema;
			log.IdUsuarioAtendido = entrada.p_IdUsuario;
			log.IdCliente = instruccion.IdCliente;
			log.EstadoCliente = instruccion.IdEstadoCliente;
			log.NombreCliente = instruccion.NombreCompleto;
			log.IdDocumentoIdentidad = instruccion.DocumentoIdentidad;
			log.IdPortafolio = instruccion.IdPortafolioCliente;
			log.EstadoPortafolio = instruccion.IdEstadoPortafolio;
			log.IdTipoRegistro = constantes.IdTipoRegistro;
			log.EstadoRegistro = instruccion.IdEstadoInstruccion;
			log.IdTipoOperacion = instruccion.IdTipoOperacion;
			log.PrecioTasa = instruccion.PrecioInstruccion;
			log.Cantidad = instruccion.Cantidad;
			log.Monto = instruccion.MontoLiquidacion;
			log.IdMonedaLiquidacion = instruccion.IdMonedaLiquidacion;
			log.Nemo = instruccion.Nemotecnico;
			log.IdProceso = 0L;
			log.EstadoProceso = "";
			if("CLI".equalsIgnoreCase(entrada.p_IdTipoCanalIngreso))
				log.NombreProceso = "Modificacion de Instruccion";
			else
				log.NombreProceso = "Ingreso de Instruccion";
			if(!instruccion.FolioFactura.isEmpty() ) {
				log.Folio = Long.valueOf(instruccion.FolioFactura);				
			}
			return log;
		}).collect(Collectors.toList());
		
		AgregarLog agregarLogService = new AgregarLog();
		agregarLogService.logica(con, entradaAgregarLog);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearInstruccion.agregarLog", fin - inicio);
	}

	private void agregarBitacoras(Connection conn, EntradaModificarInstrucciones entrada, List<Entidad> instruccionesIngresadas) throws Exception {
		logger.debug("INICIO::CrearInstruccion.agregarBitacoras");
		long inicio = System.currentTimeMillis();
		
		AgregarEventoBitacoraMasivo.EntradaAgregarEventoBitacoraMasivo entradaAgregarEventoBitacoraMasivo = new AgregarEventoBitacoraMasivo.EntradaAgregarEventoBitacoraMasivo();
		entradaAgregarEventoBitacoraMasivo.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaAgregarEventoBitacoraMasivo.p_IdUsuario = entrada.p_IdUsuario;
		entradaAgregarEventoBitacoraMasivo.p_IdModulo = entrada.p_IdModulo;
		entradaAgregarEventoBitacoraMasivo.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		entradaAgregarEventoBitacoraMasivo.p_IdTipoCanalIngreso = entrada.p_IdTipoCanalIngreso;
		entradaAgregarEventoBitacoraMasivo.p_eventos = instruccionesIngresadas.stream().map(instruccion -> {
			AgregarEventoBitacoraMasivo.EventoBitacora eventoBitacora = new AgregarEventoBitacoraMasivo.EventoBitacora();
			eventoBitacora.IdInterno = instruccion.IdEntidad;
			eventoBitacora.NombreEntidad = "INSTRUCCION";
			eventoBitacora.IdEntidad = instruccion.IdEntidad;
			eventoBitacora.IdOrden = 0L;
			eventoBitacora.IdAsignacion = 0L;
			eventoBitacora.IdTransaccion = 0L;
			eventoBitacora.MensajeEventoBitacora = ("CLI").equals(entrada.p_IdTipoCanalIngreso) ? constantes.MensajeBitacoraCliente : constantes.MensajeBitacoraAgente;
			eventoBitacora.IdTipoEventoBitacora = "INSTRUCCION_MODIFICADA";

			return eventoBitacora;
		}).collect(Collectors.toList());

		AgregarEventoBitacoraMasivo agregarEventoBitacoraMasivo = new AgregarEventoBitacoraMasivo();
		agregarEventoBitacoraMasivo.logica(conn, entradaAgregarEventoBitacoraMasivo);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearInstruccion.agregarBitacoras", fin - inicio);
	}

	private void obtenerInstruccionMotivo(Connection conn, List<Instruccion> instruccion) throws SQLException {
		logger.debug("INICIO::CrearInstruccion.obtenerTipoOperacion");
		long inicio = System.currentTimeMillis();

		SQL sql = new SQL();
		Map<String, InstruccionMotivo> mapInstruccionMotivo = sql.query(conn, SQL_SELECT_MOTIVO_INSTRUCCION, new HashMap<>(), InstruccionMotivo.class)
				.stream()
				.collect(Collectors.toMap(motivo -> motivo.CodigoEstado, motivo -> motivo));
		
		instruccion.forEach(inst -> {
			if(!("").equalsIgnoreCase(inst.CodMotivoInstruccion) && inst.CodMotivoInstruccion != null) {
				InstruccionMotivo insMotivo = mapInstruccionMotivo.get(inst.CodMotivoInstruccion);
				inst.IdMotivo = insMotivo.IdMotivo;
			}
			else
				inst.IdMotivo = null;
		});

		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearInstruccion.obtenerTipoOperacion", fin - inicio);
	}
	
	public static void main(String[]args) throws Exception{
		
		Gson gson = Utils.getGsonBuilder();

		String jsonM02 = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":106147,\"p_IdModulo\":\"ORD\",\"p_UsaReglasNegocio\":\"S\",\"p_IdTipoCanalIngreso\":\"CLI\",\"p_Instrucciones\":[{\"Cantidad\":5550,\"CorteMinimo\":1,\"DocumentoIdentidad\":\"18576992-5\",\"EsFungible\":\"N\",\"FechaLiquidacion\":\"2021-04-29\",\"FechaTrade\":\"2021-04-27\",\"FechaVencimiento\":\"2099-12-31\",\"FolioFactura\":\"918001\",\"Glosa\":\"\",\"IdBolsa\":\"BCS\",\"IdBoveda\":\"DCV\",\"IdClasificacionTipoInstrumento\":\"RV\",\"IdClasificacionTipoInstrumentoInstruccion\":\"IR\",\"IdCliente\":104512,\"IdCondicionDeLiquidacion\":\"T2\",\"IdEmisor\":\"\",\"IdEstadoCliente\":\"ACT\",\"IdEstadoPortafolio\":\"ACT\",\"IdInstruccion\":1,\"IdInstrumento\":21,\"IdIntermediario\":201,\"IdInterno\":0,\"IdMonedaLiquidacion\":\"CLP\",\"IdPortafolioCliente\":105209,\"IdTipoInstrumento\":\"ACN\",\"IdTipoInstrumentoInstruccion\":\"IRV\",\"IdTipoOperacionInstruccion\":\"COMIRV\",\"IdTipoReajuste\":\"\",\"MontoLiquidacion\":1180500,\"MontoNeto\":1165500,\"Nemotecnico\":\"AESGENER\",\"NombreCompleto\":\"SERGIO MEDRANO FLORES\",\"PlazoVencimiento\":28737,\"PrecioInstruccion\":210,\"TirTasa\":0,\"TotalCobros\":15000,\"ValorNominal\":1,\"ValorRescate\":0}]}";
		System.out.println("== TEST ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection())
		{
			EntradaModificarInstrucciones Objin = gson.fromJson(jsonM02, EntradaModificarInstrucciones.class);
			
			ModificarInstrucciones demo=new ModificarInstrucciones();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				SalidaModificarInstrucciones ObjOut=demo.logica(con, Objin);
 				con.commit();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
 			//con.rollback();
 			
    	}
		 	
		System.out.println("== END ==");
	}
}

	