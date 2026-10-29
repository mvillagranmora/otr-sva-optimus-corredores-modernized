package cl.bcs.ordenes;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.ordenes.PrevisualizarDesasignacion.Asignacion_Proceso;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

public class EjecutarDesasignacion implements JavaCall {
	private final Logger logger =  LogManager.getLogger(EjecutarDesasignacionSalida.class.getName());
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		long s1 = System.currentTimeMillis();
		Gson gson = Utils.getGsonBuilder();
		EjecutarDesasignacionEntrada in=gson.fromJson(jin.toString() ,EjecutarDesasignacionEntrada.class);
		EjecutarDesasignacionSalida out=logica(con,in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.p_out_Mensajes);
		for (MensajeSalida msje : out.p_out_Mensajes) {
			msje.descripcion = Diccionario.getMensaje(con,in.p_IdEmpresa, msje.codigo, new HashMap());
		}
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
        System.out.println("TIEMPO " + (System.currentTimeMillis() - s1));
		return dataout;
	}
	
	public static class EjecutarDesasignacionEntrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_simularOperacion;
		public String p_UsaReglasNegocio;
		public String p_FechaDesde;
		public String p_FechaHasta;
		public Long p_IdLibroOrdenes;
		public String p_IdClasificacionTipoInstrumento;
		public String p_IdTipoCanalIngreso;
		public String p_IdTipoInstrumento;
		public String p_Accion;
		public String p_IdBolsa;
		public Long p_IdInstrumento;
		public Long p_IdPortafolio;
		public Long p_IdAsignacionMasiva;
		public String p_IdMonedaOperacion;
		public String p_IdCondicionDeLiquidacion;
		public String p_MarcaAsignacion;
	}
	
	public static class EjecutarDesasignacionSalida {
		public List<MensajeSalida> p_out_Mensajes; 
		public Long P_OUT_ERRORESCONTADOR;
		
		public EjecutarDesasignacionSalida() {
			this.p_out_Mensajes = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
		}
	}
	
	public EjecutarDesasignacionSalida logica(Connection conn,EjecutarDesasignacionEntrada entrada) throws Exception {	
		logger.info("INICIO::EjecutarDesasignacion");
		long inicio = System.currentTimeMillis();

		EjecutarDesasignacionSalida salida = new EjecutarDesasignacionSalida();
		
		//CONSULTAR ASIGNACIONES
		PrevisualizarDesasignacion cons = new PrevisualizarDesasignacion();
		PrevisualizarDesasignacion.PrevisualizarDesasignacionEntrada ent = new PrevisualizarDesasignacion.PrevisualizarDesasignacionEntrada();
		
		ent.p_IdEmpresa = entrada.p_IdEmpresa;
		ent.p_IdUsuario = entrada.p_IdUsuario;
		ent.p_IdModulo = entrada.p_IdModulo;
		ent.p_FechaDesde = entrada.p_FechaDesde;
		ent.p_FechaHasta = entrada.p_FechaHasta;
		ent.p_AccionOrden = entrada.p_Accion;
		ent.p_IdBolsa = entrada.p_IdBolsa;
		ent.p_IdClasificacionTipoInstrumento = entrada.p_IdClasificacionTipoInstrumento;
		ent.p_IdInstrumento = entrada.p_IdInstrumento;
		ent.p_IdLibroOrdenes = entrada.p_IdLibroOrdenes;
		ent.p_IdTipoCanalIngreso = entrada.p_IdTipoCanalIngreso;
		ent.p_IdTipoInstrumento = entrada.p_IdTipoInstrumento;
		ent.p_IdPortafolio = entrada.p_IdPortafolio;
		ent.p_IdAsignacionMasiva = entrada.p_IdAsignacionMasiva;
		ent.p_IdMonedaOperacion = entrada.p_IdMonedaOperacion;
		ent.p_IdCondicionDeLiquidacion = entrada.p_IdCondicionDeLiquidacion;
		ent.p_MarcaAsignacion = entrada.p_MarcaAsignacion;

		PrevisualizarDesasignacion.PrevisualizarDesasignacionSalida sal = cons.logica(conn, ent);
		
		if (MensajeSalida.getNumeroErrores(sal.p_out_Mensajes) > 0) {
			salida.p_out_Mensajes.addAll(sal.p_out_Mensajes);
			return salida;
		}
		//ANULAR ASIGNACIONES
		AnularAsignaciones servicioAnularAsignaciones = new AnularAsignaciones();
		AnularAsignaciones.AnularAsignaciones_Entrada anularAsignacionesEntrada = new AnularAsignaciones.AnularAsignaciones_Entrada();
		anularAsignacionesEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
		anularAsignacionesEntrada.p_IdUsuario = entrada.p_IdUsuario;
		anularAsignacionesEntrada.p_IdModulo = entrada.p_IdModulo;
		
		AgregarLog agregarLogService = new AgregarLog();
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		entradaAgregarLog.p_IdTrabajo = 1L;
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, ent.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		for (Asignacion_Proceso a : sal.p_out_Asignaciones) {
			cl.bcs.ordenes.AnularAsignaciones.Asignacion_Entrada as = new  cl.bcs.ordenes.AnularAsignaciones.Asignacion_Entrada();
			as.IdAsignacion = a.IdAsignacion;
			anularAsignacionesEntrada.asignacionesParaAnular.add(as);
			
			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
			log.IdTrabajo = entradaAgregarLog.p_IdTrabajo;
			log.IdCabeceraLog = a.IdAsignacion;
			log.IdTipoLog = "DESASIGNACION";
			log.IdModulo = ent.p_IdModulo;
			log.IdUsuario = ent.p_IdUsuario;
			log.IdEmpresa = ent.p_IdEmpresa;
			log.FechaSistemaLog = fechaSistema;
			log.IdUsuarioAtendido = ent.p_IdUsuario;
			log.IdCliente = a.IdCliente;
			log.EstadoCliente = "";
			log.NombreCliente = a.NombreCompleto;
			log.IdTipoDocumentoIdentidad = "";
			log.IdDocumentoIdentidad = a.DocumentoIdentidad;
			log.IdPortafolio = a.IdPortafolio;
			log.EstadoPortafolio = "";
			log.IdRegistro = a.IdAsignacion.toString();
			log.IdTipoRegistro = "EJECUTAR_DESASIGNACION";
			log.EstadoRegistro = a.IdEstadoAsignacion;
			log.IdTipoOperacion = a.IdTipoOperacion;
			log.Folio = a.FolioCartera;
			log.Cantidad = a.CantidadAsignada;
			log.PrecioTasa = a.Precio;
			log.Monto = a.MontoAsignado;
			log.IdMonedaOrigen = a.IdMonedaOperacion;
			log.IdMonedaLiquidacion = a.IdMonedaLiquidacionOrden;
			log.TipoCambio = a.ValorTipoCambio.toString();
			log.Nemo = a.Nemotecnico;
			log.IdProceso = 0L;
			log.EstadoProceso = "ING";
			log.NombreProceso = "Ejecutar Desasignacion";						
			entradaAgregarLog.p_Log.add(log);
		}
		agregarLogService.logica(conn, entradaAgregarLog);
		
		AnularAsignaciones.AnularAsignaciones_Salida anularAsignacionesSalida = servicioAnularAsignaciones.logica(conn, anularAsignacionesEntrada);
		
		//PREPARAR SALIDA
		salida.p_out_Mensajes.addAll(anularAsignacionesSalida.p_out_Mensajes);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::.EjecutarDesasignacion tiempo: " + (fin - inicio));
		
		return salida;
	}
	
	public static void main(String[]args) throws Exception{
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa02", "connections.properties")){
		
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			/*
			EjecutarDesasignacionEntrada ent = new EjecutarDesasignacionEntrada();
			ent.p_IdEmpresa = 1L;
			ent.p_IdUsuario = 41L;
			ent.p_IdModulo = "ORD";
			ent.p_FechaDesde = "2017-04-12 14:30:00";
			ent.p_IdBolsa = "BCS";
			ent.p_IdTipoCanalIngreso = "TRA";
			ent.p_IdInstrumento = 418L;
			ent.p_FechaHasta = "2017-04-13 02:59:59";*/
			String json = "{\r\n"
					+ "    \"p_IdEmpresa\": 1,\r\n"
					+ "    \"p_IdUsuario\": 97,\r\n"
					+ "    \"p_IdModulo\": \"ORD\",\r\n"
					+ "    \"p_FechaHasta\": \"2026-07-04 02:59:59\",\r\n"
					+ "    \"p_FechaDesde\": \"2026-07-03 13:08:24\",\r\n"
					+ "    \"p_IdClasificacionTipoInstrumento\": \"RV\",\r\n"
					+ "    \"p_IdPortafolio\": 1124,\r\n"
					+ "    \"p_IdTipoCanalIngreso\": \"TRA\"\r\n"
					+ "}";
			EjecutarDesasignacionEntrada ent = gson.fromJson(json, EjecutarDesasignacionEntrada.class);
			
			EjecutarDesasignacion d = new EjecutarDesasignacion();
			EjecutarDesasignacionSalida salida = d.logica(conn, ent);
			for (MensajeSalida asig : salida.p_out_Mensajes) {
				System.out.println(asig.toString());
			}
			conn.rollback();
		}	
	}	
}