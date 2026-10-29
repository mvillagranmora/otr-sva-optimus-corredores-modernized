package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

public class AdministrarComisiones implements JavaCall{

	static final Logger logger = LogManager.getLogger(AdministrarComisiones.class);
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaAdministrarComision jin = gson.fromJson(in.toString(), EntradaAdministrarComision.class);
		SalidaAdministrarComision out = logica(conn,jin);

		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static class EntradaAdministrarComision extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public List<Comision> p_Comision;
		
		
		public EntradaAdministrarComision() {
			this.p_IdEmpresa = 0L;
			this.p_IdUsuario = 0L;
			this.p_IdModulo = "";
			this.p_UsaReglasNegocio = "N";
			this.p_Comision = new ArrayList<Comision>();
		}
	}
	
	public static class SalidaAdministrarComision extends BcsPojo{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_ERRORESDETALLE;
		public Long P_OUT_ADVERTENCIASCONTADOR;
		public List<MensajeSalida> P_OUT_ADVERTENCIASDETALLE;
		
		public SalidaAdministrarComision() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_ERRORESDETALLE = new ArrayList<MensajeSalida>();
			this.P_OUT_ADVERTENCIASCONTADOR = 0L;
			this.P_OUT_ADVERTENCIASDETALLE = new ArrayList<MensajeSalida>();
		}
	}
	
	public static class Comision extends BcsPojo {
		public Long IdInterno;
		public Long IdComision;
		public String IdSerieFondo;
		public String Descripcion;
		public String TipoComision;
		public String FechaVigenciaDesde;
		public String FechaVigenciaHasta;
		public BigDecimal PorcentajeLiberado;
		public String Estado;
		public String FechaSistema;
		public Long IdEmpresa;
		public List<TramoComision> TramosComision;
		public List<TramoComision> TramosQuitados;
		
	    public Comision () {
			this.IdInterno = 0L;
			this.IdComision = 0L;
			this.IdSerieFondo = "";
			this.Descripcion = "";
			this.TipoComision = "";
			this.FechaVigenciaDesde = "";
			this.FechaVigenciaHasta = "";
			this.PorcentajeLiberado = BigDecimal.ZERO;
			this.Estado = "INA";
			this.FechaSistema = "0001-01-01";
			this.IdEmpresa = 0L;
			this.TramosComision = new ArrayList<TramoComision>();
			this.TramosQuitados = new ArrayList<TramoComision>();
		}
	}
	
	public static class TramoComision extends BcsPojo {
		public Long IdInterno;
		public Long IdTramo;
		public Long IdComision;
		public Long DiasDesde;
		public Long DiasHasta;
		public BigDecimal PorcentajeComision;
		public String FechaSistema;
		public String Estado;
		
		public TramoComision() {
			this.IdInterno = 0L;
			this.IdTramo = 0L;
			this.IdComision = 0L;
			this.DiasDesde = 0L;
			this.DiasHasta = 0L;
			this.PorcentajeComision = BigDecimal.ZERO;
			this.FechaSistema = "0001-01-01";
			this.Estado = "INA";
		}
	}
	
	public SalidaAdministrarComision logica(Connection conn, EntradaAdministrarComision entrada)  throws Exception{
		logger.info("INICIO::AdministrarComisiones");
		long inicioServicio = System.currentTimeMillis();
		
		SalidaAdministrarComision salida = new SalidaAdministrarComision();
		
		HashMap<String, String> paramsMensaje = new HashMap<>(2);
		
		if(entrada.p_Comision.isEmpty()) {
			salida.P_OUT_ERRORESDETALLE.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_SOLICITUDES_APORTE_RESCATE", paramsMensaje, conn, entrada.p_IdEmpresa));
			salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.stream().count();
			return salida;
		}
		
		//modificar las comisiones existentes
		SalidaAdministrarComision salidaModificacion = modificarComisiones(conn, entrada);
		salida.P_OUT_ADVERTENCIASDETALLE.addAll(salidaModificacion.P_OUT_ADVERTENCIASDETALLE);
		salida.P_OUT_ERRORESDETALLE.addAll(salidaModificacion.P_OUT_ERRORESDETALLE);
		
		SalidaAdministrarComision salidaCreacion = crearComisiones(conn, entrada);
		salida.P_OUT_ADVERTENCIASDETALLE.addAll(salidaCreacion.P_OUT_ADVERTENCIASDETALLE);
		salida.P_OUT_ERRORESDETALLE.addAll(salidaCreacion.P_OUT_ERRORESDETALLE);

		salida.P_OUT_ADVERTENCIASCONTADOR = salida.P_OUT_ADVERTENCIASDETALLE.stream().count();
		salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.stream().count();
		
		//agregar Log
		agregarLog(conn,entrada);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::AdministrarComisiones", finServicio - inicioServicio);
		return salida;
	}
	
	private SalidaAdministrarComision modificarComisiones(Connection conn, EntradaAdministrarComision entrada) throws Exception {
		logger.info("INICIO::AdministrarComisiones.modificarComisiones");
		long inicioServicio = System.currentTimeMillis();
		
		SalidaAdministrarComision salidaModificar = new SalidaAdministrarComision();
		List<Comision> comisionesExistentes = entrada.p_Comision.stream().filter(com -> !com.IdComision.equals(0L)).collect(Collectors.toList());
		
		if(!comisionesExistentes.isEmpty()) {
			EntradaAdministrarComision entradaModificar = new EntradaAdministrarComision();
			entradaModificar.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaModificar.p_IdModulo = entrada.p_IdModulo;
			entradaModificar.p_IdUsuario = entrada.p_IdUsuario;
			entradaModificar.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
			entradaModificar.p_Comision.addAll(comisionesExistentes);
			
			ModificarComision servicioModificar = new ModificarComision();
			
			salidaModificar = servicioModificar.logica(conn, entradaModificar);	
		}
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::AdministrarComisiones.modificarComisiones", finServicio - inicioServicio);
		return salidaModificar;
	}

	private SalidaAdministrarComision crearComisiones(Connection conn, EntradaAdministrarComision entrada) throws Exception {
		logger.info("INICIO::AdministrarComisiones.crearComisiones");
		long inicioServicio = System.currentTimeMillis();
		
		SalidaAdministrarComision salidaCreacion = new SalidaAdministrarComision();
		List<Comision> comisionesNuevas = entrada.p_Comision.stream().filter(com -> com.IdComision.equals(0L)).collect(Collectors.toList());
		
		if(!comisionesNuevas.isEmpty()) {
			EntradaAdministrarComision entradaCrear = new EntradaAdministrarComision();
			entradaCrear.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaCrear.p_IdModulo = entrada.p_IdModulo;
			entradaCrear.p_IdUsuario = entrada.p_IdUsuario;
			entradaCrear.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
			entradaCrear.p_Comision.addAll(comisionesNuevas);
			
			CrearComision servicioCrear = new CrearComision();
			salidaCreacion = servicioCrear.logica(conn, entradaCrear);
			
		}
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::AdministrarComisiones.crearComisiones", finServicio - inicioServicio);
		return salidaCreacion;
	}

	private void agregarLog(Connection conn, EntradaAdministrarComision entrada) throws Exception { 
		logger.trace("INICIO::AdministrarComisiones.agregarLog");
		long inicio = System.currentTimeMillis();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		for(Comision com : entrada.p_Comision) {
			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
			log.IdCabeceraLog = com.IdComision;
			log.IdTipoLog = "INGRESO";
			log.IdModulo = _c.ModuloFondoMutuo;
			log.IdUsuario = entrada.p_IdUsuario;
			log.IdEmpresa = entrada.p_IdEmpresa;
			log.FechaSistemaLog = fechaSistema;
			log.IdUsuarioAtendido = entrada.p_IdUsuario;
			log.IdCliente = 0L;
			log.EstadoCliente = "";
			log.NombreCliente = "";
			log.IdTipoDocumentoIdentidad = "";
			log.IdDocumentoIdentidad = "";
			log.IdPortafolio = 0L;
			log.EstadoPortafolio = "";
			log.IdTipoRegistro = _c.TipoRegistroInstrumentoFFMM;
			log.IdRegistro = com.IdComision.toString();
			log.EstadoRegistro = com.Estado;
			log.IdTipoOperacion = "";
			log.Folio = 0L;
			log.Cantidad = BigDecimal.ZERO;
			log.PrecioTasa = BigDecimal.ZERO;
			log.Monto = BigDecimal.ZERO;
			log.IdMonedaOrigen = "";
			log.IdMonedaLiquidacion = "";
			log.TipoCambio = "";
			log.Nemo = com.IdSerieFondo;
			log.IdProceso = 0L;
			log.EstadoProceso = "";
			log.NombreProceso = "";
			entradaAgregarLog.p_Log.add(log);
		}
		AgregarLog agregarLogService = new AgregarLog();
		agregarLogService.logica(conn, entradaAgregarLog);
		long fin = System.currentTimeMillis();
		logger.trace("FIN::AdministrarComisiones.agregarLog", fin - inicio);
	}
	
	public static void main(String[] args) {
		System.out.println("== INICIO TEST =");

		try (Connection conn = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
					.serializeNulls().create();
			
			SalidaAdministrarComision salida = new SalidaAdministrarComision();
			AdministrarComisiones administrarComision = new AdministrarComisiones();

			String desa = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"FMM\",\"p_UsaReglasNegocio\":\"N\",\"p_Comision\":[{\"IdInterno\":0,\"IdComision\":962,\"IdSerieFondo\":\"FMOPTIMUS_A\",\"Descripcion\":\"AD\",\"TipoComision\":\"PER\",\"FechaVigenciaDesde\":\"2021-01-01\",\"FechaVigenciaHasta\":\"2999-12-31\",\"PorcentajeLiberado\":1.1,\"Estado\":\"ACT\",\"TramosComision\":[{\"IdInterno\":0,\"IdTramo\":121,\"DiasDesde\":0,\"DiasHasta\":300,\"PorcentajeComision\":0.89}]}]}";
			String quitar = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"FMM\",\"p_UsaReglasNegocio\":\"N\",\"p_Comision\":[{\"IdInterno\":0,\"IdComision\":962,\"IdSerieFondo\":\"FMOPTIMUS_A\",\"Descripcion\":\"AD\",\"TipoComision\":\"PER\",\"FechaVigenciaDesde\":\"2021-01-04\",\"FechaVigenciaHasta\":\"2999-12-31\",\"PorcentajeLiberado\":1.1,\"Estado\":\"ACT\",\"TramosComision\":[{\"IdTramo\":122,\"DiasDesde\":20,\"DiasHasta\":1,\"PorcentajeComision\":0.7,\"IdInterno\":0}],\"TramosQuitados\":[{\"IdTramo\":121,\"Estado\":\"INA\"}]}]}";
			EntradaAdministrarComision Objin = gson.fromJson(quitar, EntradaAdministrarComision.class);		
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			
			try {
				SalidaAdministrarComision ObjOut = administrarComision.logica(conn, Objin);

				if (salida.P_OUT_ERRORESCONTADOR > 0) {
					conn.rollback();
				}
//				conn.rollback();
			 	conn.commit();

				System.out.println("SALIDA");
				System.out.println(ObjOut);
			} catch (Exception er) {
				er.printStackTrace();
				conn.rollback();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("== FIN TEST ==");
	}
}
