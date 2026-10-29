package cl.bcs.mesa.MES;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class EditarMonedaPorProducto implements JavaCall {
	static final Logger logger = LogManager.getLogger(EditarMonedaPorProducto.class);
	private final String SQL_CONSULTAR_MONEDA_POR_PRODUCTO_EXISTENTES = loadResourceToString("cl/bcs/mesa/MES/sql/EditarMonedaPorProducto_ConsultarMonedaPorProductoExistente.sql");
	private final String SQL_CONSULTAR_MONEDA_POR_PRODUCTO = loadResourceToString("cl/bcs/mesa/MES/sql/EditarMonedaPorProducto_ConsultarMonedaPorProducto.sql");
	private final String SQL_UPDATE_MONEDA_POR_PRODUCTO = loadResourceToString("cl/bcs/mesa/MES/sql/EditarMonedaPorProducto_UpdateMonedaPorProducto.sql");

	public static class EntradaEditarMonedaPorProducto extends BcsPojo {
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;
		public String 	p_UsaReglasNegocio;
		public List<InstrumentoMoneda> p_MonedasPorProducto;

		public EntradaEditarMonedaPorProducto() {
			this.p_IdEmpresa = 0L;
			this.p_IdUsuario = 0L;
			this.p_IdModulo = "";
			this.p_UsaReglasNegocio = "N";
			this.p_MonedasPorProducto = new ArrayList<InstrumentoMoneda>();
		}
	}
	
	public static class InstrumentoMoneda extends BcsPojo {
		public Long 	IdInstrumentoMoneda;
		public Long 	IdInstrumento;
		public String	Nemotecnico;
		public String 	IdTipoProducto;
		public String 	IdTipoInstrumento;
		public String 	IdMonedaPrincipal;
		public String 	IdMonedaSecundaria;
		public String 	IdEstado;
		public String	FeriadoMonedaPrincipal;
		
		public InstrumentoMoneda() {
			this.IdInstrumentoMoneda = 0L;
			this.IdInstrumento = 0L;
			this.Nemotecnico = "";
			this.IdTipoProducto = "";
			this.IdTipoInstrumento = "";
			this.IdMonedaPrincipal = "";
			this.IdMonedaSecundaria = "";
			this.IdEstado = "INA";
			this.FeriadoMonedaPrincipal = "N";
		}
	}
	
	public static class SalidaEditarMonedaPorProducto extends BcsPojo {
		public Long					P_OUT_ERRORESCONTADOR;
		public Long					P_OUT_ADVERTENCIASCONTADOR;
		public List<MensajeSalida> 	P_OUT_MENSAJES;
		
		public SalidaEditarMonedaPorProducto() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_ADVERTENCIASCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaEditarMonedaPorProducto jin = gson.fromJson(in.toString(), EntradaEditarMonedaPorProducto.class);
		SalidaEditarMonedaPorProducto out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	private SalidaEditarMonedaPorProducto logica(Connection conn, EntradaEditarMonedaPorProducto entrada) throws Exception {
		logger.info("INICIO::EditarMonedaPorProducto");
		SalidaEditarMonedaPorProducto salida = new SalidaEditarMonedaPorProducto();
		
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(conn, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			salida.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
			return salida;
		}

		if (entrada.p_MonedasPorProducto.size() == 0) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_NO_SE_INGRESARON_MONEDAS_POR_PRODUCTO",new HashMap<>(),conn,entrada.p_IdEmpresa));
			salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
			return salida;
		}
		
		//obtener entidades entrada
		List<InstrumentoMoneda> entidadesInstrumentoMoneda = obtenerEntidadesIntrumentoMoneda(entrada.p_MonedasPorProducto);
		
		for(InstrumentoMoneda instrumentoMoneda : entidadesInstrumentoMoneda) {
			List<InstrumentoMoneda> entidadesInstrumentoMonedaExistentes = consultarExistenciaInstrumentoMoneda(conn, instrumentoMoneda, entrada.p_IdEmpresa);
			if(entidadesInstrumentoMonedaExistentes.size() > 0) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_YA_EXISTE_MONEDA_POR_PRODUCTO",new HashMap<>(),conn,entrada.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
				return salida;
			}
		}
		
		List<InstrumentoMoneda> datosAntiguos = obtenerIntrumentosMoneda(conn, entrada.p_MonedasPorProducto, entrada.p_IdEmpresa);
		actualizarMonedaPorProducto(conn, entidadesInstrumentoMoneda);
		agregarLog(conn, datosAntiguos, entrada);
		
		salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"UI_MONEDA_POR_PRODUCTO_MODIFICADA",new HashMap<>(),conn,entrada.p_IdEmpresa));
		
		logger.info("FIN::EditarMonedaPorProducto");
		return salida;
	}

	private void actualizarMonedaPorProducto(Connection conn, List<InstrumentoMoneda> instrumentoMoneda) throws Exception {
		logger.info("INICIO::EditarMonedaPorProducto.actualizarMonedaPorProducto");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		sql.batch(conn,SQL_UPDATE_MONEDA_POR_PRODUCTO,instrumentoMoneda,InstrumentoMoneda.class);
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::EditarMonedaPorProducto.actualizarMonedaPorProducto", fin - inicio);
	}

	private List<InstrumentoMoneda> consultarExistenciaInstrumentoMoneda(Connection conn, InstrumentoMoneda instrumentoMoneda, Long idEmpresa) throws Exception {
		logger.info("INICIO::EditarMonedaPorProducto.consultarExistenciaInstrumentoMoneda");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();		
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("IdInstrumentoMoneda", instrumentoMoneda.IdInstrumentoMoneda);
		params.put("IdEmpresa", idEmpresa);
		params.put("Nemotecnico", instrumentoMoneda.Nemotecnico);
		params.put("IdInstrumento", instrumentoMoneda.IdInstrumento);
		params.put("IdTipoInstrumento", instrumentoMoneda.IdTipoInstrumento);
		params.put("IdTipoProducto", instrumentoMoneda.IdTipoProducto);
		params.put("IdMonedaPrincipal", instrumentoMoneda.IdMonedaPrincipal);
		params.put("IdMonedaSecundaria", instrumentoMoneda.IdMonedaSecundaria);
		
		List<InstrumentoMoneda> instrumentosMoneda = sql.query(conn, SQL_CONSULTAR_MONEDA_POR_PRODUCTO_EXISTENTES, params, InstrumentoMoneda.class);

		long fin = System.currentTimeMillis();
		logger.info("FIN::EditarMonedaPorProducto.consultarExistenciaInstrumentoMoneda", fin - inicio);
		return instrumentosMoneda;
	}

	private List<InstrumentoMoneda> obtenerEntidadesIntrumentoMoneda(List<InstrumentoMoneda> instrumentoMonedas) {
		logger.info("INICIO::EditarMonedaPorProducto.obtenerEntidadesIntrumentoMoneda");
		List<InstrumentoMoneda> entidadesInstrumentoMoneda = instrumentoMonedas.stream().map(instrumentoMoneda ->{
			return instrumentoMoneda;
		}).collect(Collectors.toList());
		
		logger.info("FIN::EditarMonedaPorProducto.obtenerEntidadesIntrumentoMoneda");
		return entidadesInstrumentoMoneda;
	}
	
	public void agregarLog(Connection con, List<InstrumentoMoneda> datosAntiguos, EntradaEditarMonedaPorProducto entrada) {
		try {
			logger.trace("INICIO::EditarMonedaPorProducto.agregarLog");
			long inicio = System.currentTimeMillis();
			
			HashMap<String, Object> hDatosA = Utils.List2Hash(datosAntiguos, "IdInstrumentoMoneda");
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
			
			for (InstrumentoMoneda ins : entrada.p_MonedasPorProducto) {
				AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
				Long idCabeceraLog = ins.IdInstrumentoMoneda;
				InstrumentoMoneda datoAntiguo = (InstrumentoMoneda) hDatosA.get(ins.IdInstrumentoMoneda);
				
				log.IdCabeceraLog = idCabeceraLog;
				log.IdTipoLog = "MODIFICACION";
				log.IdModulo = entrada.p_IdModulo;
				log.IdUsuario = entrada.p_IdUsuario;
				log.IdEmpresa = entrada.p_IdEmpresa;
				log.FechaSistemaLog = fechaSistema;
				log.IdUsuarioAtendido = entrada.p_IdUsuario;
				log.IdTipoRegistro = "MONEDA-PRODUCTO";
				log.IdRegistro = ins.IdInstrumentoMoneda.toString();
				log.Nemo = ins.Nemotecnico;
				log.NombreProceso = "Editar Moneda Por Producto";
				
				entradaAgregarLog.p_Log.add(log);
				
				if (!datoAntiguo.IdTipoProducto.equalsIgnoreCase(ins.IdTipoProducto))
					entradaAgregarLog.p_LogDetalle.add(agregarDetalleLog(idCabeceraLog, "Tipo Producto", datoAntiguo.IdTipoProducto, ins.IdTipoProducto));
				if (!datoAntiguo.IdTipoInstrumento.equalsIgnoreCase(ins.IdTipoInstrumento))
					entradaAgregarLog.p_LogDetalle.add(agregarDetalleLog(idCabeceraLog, "Tipo Instrumento", datoAntiguo.IdTipoInstrumento, ins.IdTipoInstrumento));
				if (!datoAntiguo.IdMonedaPrincipal.equalsIgnoreCase(ins.IdMonedaPrincipal))
					entradaAgregarLog.p_LogDetalle.add(agregarDetalleLog(idCabeceraLog, "Moneda Principal", datoAntiguo.IdMonedaPrincipal, ins.IdMonedaPrincipal));
				if (!datoAntiguo.IdMonedaSecundaria.equalsIgnoreCase(ins.IdMonedaSecundaria))
					entradaAgregarLog.p_LogDetalle.add(agregarDetalleLog(idCabeceraLog, "Moneda Secundaria", datoAntiguo.IdMonedaSecundaria, ins.IdMonedaSecundaria));
				if (!datoAntiguo.IdEstado.equalsIgnoreCase(ins.IdEstado))
					entradaAgregarLog.p_LogDetalle.add(agregarDetalleLog(idCabeceraLog, "Estado", datoAntiguo.IdEstado, ins.IdEstado));
				if (!datoAntiguo.FeriadoMonedaPrincipal.equalsIgnoreCase(ins.FeriadoMonedaPrincipal))
					entradaAgregarLog.p_LogDetalle.add(agregarDetalleLog(idCabeceraLog, "Feriado Moneda Principal", datoAntiguo.FeriadoMonedaPrincipal, ins.FeriadoMonedaPrincipal));				
			}
			
			AgregarLog agregarLogService = new AgregarLog();
			agregarLogService.logica(con, entradaAgregarLog);
			long fin = System.currentTimeMillis();
			logger.trace("FIN::EditarMonedaPorProducto.agregarLog", fin - inicio);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public  InAgregarLog.DetalleLog agregarDetalleLog(Long idCabeceraLog, String campoRegistro, String valorAntiguo, String valorNuevo) {
		InAgregarLog.DetalleLog detLog = new InAgregarLog.DetalleLog();
		detLog.IdCabeceraLog = idCabeceraLog; 
		detLog.IdTrabajo = 0L;
		detLog.SubRegistro = "MONEDA-PRODUCTO"; 
		detLog.CampoRegistro = campoRegistro; 
		detLog.ValorAntiguo = valorAntiguo; 
		detLog.ValorNuevo = valorNuevo;
		return detLog;
	}
	
	private List<InstrumentoMoneda> obtenerIntrumentosMoneda(Connection conn, List<InstrumentoMoneda> entradaInstrumentosMoneda, Long idEmpresa) throws Exception {
		logger.debug("INICIO::EditarMonedaPorProducto.obtenerIntrumentosMoneda");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		HashMap<String, Object> params = new HashMap<String, Object>();
		List<Long> idsInstrumentoMoneda = new ArrayList<Long>();
		idsInstrumentoMoneda = entradaInstrumentosMoneda.stream()
														.map(i -> i.IdInstrumentoMoneda)
													    .collect(Collectors.toList());
											
		params.put("IdsInstrumentoMoneda", idsInstrumentoMoneda);
		params.put("IdEmpresa", idEmpresa);
		List<InstrumentoMoneda> instrumentosMoneda = sql.query(conn, SQL_CONSULTAR_MONEDA_POR_PRODUCTO, params, InstrumentoMoneda.class);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::EditarMonedaPorProducto.obtenerIntrumentosMoneda", fin - inicio);
		return instrumentosMoneda;
	}
	
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":\"MFW\",\"p_UsaReglasNegocio\":\"S\",\"p_MonedasPorProducto\":[{\"IdInstrumentoMoneda\":5,\"IdInstrumento\":133254,\"Nemotecnico\":\"SEINFL\",\"IdTipoProducto\":\"MEFW\",\"IdTipoInstrumento\":\"FWD\",\"IdMonedaPrincipal\":\"UF\",\"IdMonedaSecundaria\":\"CLP\",\"IdEstado\":\"INA\",\"FeriadoMonedaPrincipal\":\"N\"}]}";
			
			EntradaEditarMonedaPorProducto Objin = gson.fromJson(json, EntradaEditarMonedaPorProducto.class);
			EditarMonedaPorProducto demo = new EditarMonedaPorProducto();

			System.out.println("ENTRADA");
			System.out.println(Objin.toString());

			try {
				SalidaEditarMonedaPorProducto ObjOut = demo.logica(con, Objin);
				con.commit();
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
