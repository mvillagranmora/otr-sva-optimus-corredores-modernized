package cl.bcs.plataforma.pau;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.mesa.SPT.CrearOperacionSpot.OperacionObjOut;
import cl.bcs.plataforma.pau.CambiarEstadoModuloMesaSpot.Mesa;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CambiarEstadoModuloMesaSpot implements JavaCall {
	private static final Logger logger = LogManager.getLogger(CambiarEstadoModuloMesaSpot.class);
	
	private final String SQL_SELECT_CODIGO_USUARIO = loadResourceToString("cl/bcs/plataforma/pau/sql/CambiarEstadoModuloMesaSpot_SelectCodigoUsuario.sql"); 
	private final String SQL_DATOS_MESA = loadResourceToString("cl/bcs/plataforma/pau/sql/CambiarEstadoModuloMesaSpot_SelectDatosMesa.sql");
	private final String SQL_UPDATE_MODULO_SPOT = loadResourceToString("cl/bcs/plataforma/pau/sql/CambiarEstadoModuloMesaSpot_UpdateEstadoModuloSpot.sql");
	private final String SQL_UPDATE_MESA_SPOT = loadResourceToString("cl/bcs/plataforma/pau/sql/CambiarEstadoModuloMesaSpot_UpdateEstadoMesaSpot.sql");
	private final String SQL_CONSULTAR_PARIDAD_TIPO_CAMBIO = loadResourceToString("cl/bcs/plataforma/pau/sql/CambiarEstadoModuloMesaSpot_SelectParidadTipoCambio.sql");
	private final String SQL_UPDATE_PARIDAD_TIPO_CAMBIO = loadResourceToString("cl/bcs/plataforma/pau/sql/CambiarEstadoModuloMesaSpot_UpdateTipoCambio.sql");
	private final String SQL_CREATE_TEMPORARY_TABLE_ABRIR_MODULO = loadResourceToString("cl/bcs/plataforma/pau/sql/CambiarEstadoModuloMesaSpot_CreateTemporaryTableAbrirModulo.sql");
	private final String SQL_CREATE_TEMPORARY_TABLE_DATOS_MESA = loadResourceToString("cl/bcs/plataforma/pau/sql/CambiarEstadoModuloMesaSpot_CreateTemporaryTableDatosMesa.sql");
	private final String SQL_CREATE_TEMPORARY_TABLE_DATOS_TIPOCAMBIO = loadResourceToString("cl/bcs/plataforma/pau/sql/CambiarEstadoModuloMesaSpot_CreateTemporaryTableDatosTipoCambio.sql");
	private final String SQL_INSERT_TEMPORARY_TABLE_ABRIR_MODULO = loadResourceToString("cl/bcs/plataforma/pau/sql/CambiarEstadoModuloMesaSpot_InsertTemporaryTableAbrirModulo.sql");
	private final String SQL_INSERT_TEMPORARY_TABLE_DATOS_MESA = loadResourceToString("cl/bcs/plataforma/pau/sql/CambiarEstadoModuloMesaSpot_InsertTemporaryTableDatosMesa.sql");
	private final String SQL_INSERT_TEMPORARY_TABLE_DATOS_TIPOCAMBIO = loadResourceToString("cl/bcs/plataforma/pau/sql/CambiarEstadoModuloMesaSpot_InsertTemporaryTableDatosTipoCambio.sql");
	
	public static class Usuario{
		public String CodigoUsuario;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class Modulo {
		public String 	CodigoModulo; 
		public String 	Estado; 
		public Usuario 	CodigoUsuario;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class EntradaAbrirModuloMesaSpot {
		public Long 		p_IdEmpresa;
		public Long 		p_IdUsuario;
		public String 		p_IdModulo;
		public String		p_simularOperacion;
		public String		p_UsaReglasNegocio;
		public String		p_FechaSistema;
		public String		p_FechaSistemaAnterior;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class OutResultado {
		public String 	CodigoModulo; 
		public String 	IdEstado; 
		public String 	NombreModulo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class OutAdvertenciasDetalle {
		public Long 	IdInterno;
		public String	Codigo;
		public String	RequiereAutorizacion;
		public String	Descripcion;
		
		public OutAdvertenciasDetalle() {
			this.IdInterno = 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class OutErroresDetalle {
		public Long 	IdInterno;
		public String	Codigo;
		public String	Descripcion;
		
		public OutErroresDetalle() {
			this.IdInterno = 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaAbrirModuloMesaSpot {
		public Long					p_out_ErroresContador;
		public List<MensajeSalida>	p_out_Mensajes;
		public String				p_out_Mensaje;
		
		public SalidaAbrirModuloMesaSpot() {
			this.p_out_Mensajes = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Mesa {
		public Long 		IdMesa; 
		public String 		NombreMesa; 
		public String 		IdEstadoMesa;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class EntradaAbrirMesaSpot {
		public Long 		p_IdEmpresa;
		public Long 		p_IdUsuario;
		public String 		p_IdModulo;
		public String		p_simularOperacion;
		public String		p_UsaReglasNegocio;
		public List<Mesa>	p_DatosMesas;
	
		public EntradaAbrirMesaSpot() {
			this.p_DatosMesas = new ArrayList<Mesa>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class OutResultadoProceso {
		public Long 	IdMesa; 
		public String 	Resultado; 
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaAbrirMesaSpot {
		public OutResultadoProceso 		p_out_ResultadoProceso; 
		public Integer 					p_out_AdvertenciasContador;
		public OutAdvertenciasDetalle	p_out_AdvertenciasDetalle;
		public Integer					p_out_ErroresContador;
		public OutErroresDetalle		p_out_ErroresDetalle;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InModificarParidadesTipoCambio {
		public Long IdParidadYTipoCambio;
		public String IdMoneda;
		public String NombreMoneda;
		public BigDecimal ParidadMercado;
		public BigDecimal ParidadBID;
		public BigDecimal ParidadASK;
		public BigDecimal LimiteSuperiorParidad;
		public BigDecimal LimiteInferiorParidad;
		public BigDecimal ParidadSBIF;
		public BigDecimal TipoCambioMercado;
		public BigDecimal LimiteSuperiorTipoCambio;
		public BigDecimal LimiteInferiorTipoCambio;
		public BigDecimal PrecioSpotCompra;
		public BigDecimal PrecioSpotVenta;
		public BigDecimal PrecioSpotPromedio;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class OutParidadesTipoCambio {
		public Long IdParidadYTipoCambio;
		public String IdMoneda;
		public String NombreMoneda;
		public BigDecimal ParidadMercado;
		public BigDecimal ParidadBID;
		public BigDecimal ParidadASK;
		public BigDecimal LimiteSuperiorParidad;
		public BigDecimal LimiteInferiorParidad;
		public BigDecimal PorcentajeSuperiorParidad;
		public BigDecimal PorcentajeInferiorParidad;
		public BigDecimal ParidadSBIF;
		public BigDecimal TipoCambioMercado;
		public BigDecimal LimiteSuperiorTipoCambio;
		public BigDecimal LimiteInferiorTipoCambio;
		public BigDecimal PorcentajeSuperiorTipoCambio;
		public BigDecimal PorcentajeInferiorTipoCambio;
		public BigDecimal PrecioSpotCompra;
		public BigDecimal PrecioSpotVenta;
		public BigDecimal PrecioSpotPromedio;
		public String MultiplicaODivide;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaConsultarParidadesTipoCambio {
		public OutParidadesTipoCambio 	p_out_ParidadesYTipoCambio; 
		public Integer 					p_out_AdvertenciasContador;
		public OutAdvertenciasDetalle	p_out_AdvertenciasDetalle;
		public Integer					p_out_ErroresContador;
		public OutErroresDetalle		p_out_ErroresDetalle;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	/*******************
	 * LOGICA
	 *******************/
	
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = new Gson();		
		EntradaAbrirModuloMesaSpot in=gson.fromJson(jin.toString() ,EntradaAbrirModuloMesaSpot.class);
		SalidaAbrirModuloMesaSpot out = logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaAbrirModuloMesaSpot logica(Connection con, EntradaAbrirModuloMesaSpot entrada) throws Exception {
		Gson gson = new Gson();	
		SalidaAbrirModuloMesaSpot salida = new SalidaAbrirModuloMesaSpot();
		salida.p_out_ErroresContador = 0L;
		SQL sql=new SQL();
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdEmpresa",entrada.p_IdEmpresa);
		params.put("IdUsuario", entrada.p_IdUsuario);
		
		List<Usuario> codigoUsuario = sql.query(con, SQL_SELECT_CODIGO_USUARIO, params, Usuario.class);
		
		Modulo modulo = new Modulo();
		modulo.CodigoModulo = "MSP";
		modulo.Estado = "ABT";
		modulo.CodigoUsuario = codigoUsuario.get(0);
		
		List<Modulo> modulos = new ArrayList<Modulo>();
		modulos.add(modulo);
		
		params.clear();
		params.put("IdTipoProducto","MEME");
		List<Mesa> mesas = sql.query(con, SQL_DATOS_MESA, params, Mesa.class);
		
		// Se borran tablas temporales si ya existen
		try {
			SQL.exec(con, "DROP TABLE #AbrirModulo_p_Modulo");
		} catch(Exception e) {
			logger.info("Tabla #AbrirModulo_p_Modulo no existe");
		}
		try {
			SQL.exec(con, "DROP TABLE #CierreAperturaMesa_p_DatosMesas");
		} catch(Exception e) {
			logger.info("Tabla #CierreAperturaMesa_p_DatosMesas no existe");
		}
		
		// Crea las tablas temporales
		SQL.exec(con, SQL_CREATE_TEMPORARY_TABLE_ABRIR_MODULO);
		SQL.exec(con, SQL_CREATE_TEMPORARY_TABLE_DATOS_MESA);
		
		// Inserta tablas temporales globales
		sql.batch(con, SQL_INSERT_TEMPORARY_TABLE_ABRIR_MODULO, modulos, Modulo.class);
		sql.batch(con, SQL_INSERT_TEMPORARY_TABLE_DATOS_MESA, mesas, Mesa.class);		
		
		//Se abre el modulo Spot
		HashMap<String,Object> parmsInModuloSpot =new HashMap<String,Object>();
		parmsInModuloSpot.put("p_IdEmpresa", entrada.p_IdEmpresa);
		parmsInModuloSpot.put("p_IdUsuario", entrada.p_IdUsuario);
		parmsInModuloSpot.put("p_IdModulo", entrada.p_IdModulo);
		parmsInModuloSpot.put("p_Modulo", modulos);
		parmsInModuloSpot.put("p_MesaAbierta", 'N');
		
		HashMap<String,Class> parmsOutModuloSpot =new HashMap<String,Class>();
		parmsOutModuloSpot.put("p_out_Resultado", OutResultado.class);
		parmsOutModuloSpot.put("p_out_AdvertenciasContador", Long.class);
		parmsOutModuloSpot.put("p_out_ErroresContador", Long.class);
		parmsOutModuloSpot.put("p_out_AdvertenciasDetalle", OutAdvertenciasDetalle.class);
		parmsOutModuloSpot.put("p_out_ErroresDetalle", OutErroresDetalle.class);
		
		HashMap<String, Object> outAbrirModulo = sql.call(con, SQL_UPDATE_MODULO_SPOT, parmsInModuloSpot, parmsOutModuloSpot);
        
		salida.p_out_ErroresContador = (Long) outAbrirModulo.get("p_out_ErroresContador");
		SQL.exec(con, "DROP TABLE #AbrirModulo_p_Modulo");
		
		if(((Long) outAbrirModulo.get("p_out_AdvertenciasContador")) > 0) {
			MensajeSalida mensajeSalida = new MensajeSalida();
			for(OutAdvertenciasDetalle mensaje : (List<OutAdvertenciasDetalle>) outAbrirModulo.get("p_out_AdvertenciasDetalle")) {
				mensajeSalida.id = mensaje.IdInterno;
				mensajeSalida.tipo = "ADVERTENCIA";
				mensajeSalida.codigo = mensaje.Codigo;
				mensajeSalida.descripcion = mensaje.Descripcion;
				salida.p_out_Mensajes.add(mensajeSalida);
			}
		}
		
		if(((Long) outAbrirModulo.get("p_out_ErroresContador")) > 0) {
			MensajeSalida mensajeSalida = new MensajeSalida();
			for(OutErroresDetalle mensaje : (List<OutErroresDetalle>) outAbrirModulo.get("p_out_ErroresDetalle")) {
				mensajeSalida.id = mensaje.IdInterno;
				mensajeSalida.tipo = "ERROR";
				mensajeSalida.codigo = mensaje.Codigo;
				mensajeSalida.descripcion = mensaje.Descripcion;
				salida.p_out_Mensajes.add(mensajeSalida);
			}
			return salida;
		}
			
		//Se abre la mesa Spot
		HashMap<String,Object> parmsInMesaSpot =new HashMap<String,Object>();
		parmsInMesaSpot.put("p_IdEmpresa", entrada.p_IdEmpresa);
		parmsInMesaSpot.put("p_IdUsuario", entrada.p_IdUsuario);
		parmsInMesaSpot.put("p_IdModulo", entrada.p_IdModulo);
		parmsInMesaSpot.put("p_simularOperacion", entrada.p_simularOperacion);
		parmsInMesaSpot.put("p_UsaReglasNegocio", entrada.p_UsaReglasNegocio);
		parmsInMesaSpot.put("p_DatosMesas", mesas);
		
		HashMap<String,Class> parmsOutMesaSpot =new HashMap<String,Class>();
		parmsOutMesaSpot.put("p_out_ResultadoProceso", OutResultadoProceso.class);
		parmsOutMesaSpot.put("p_out_AdvertenciasContador", Long.class);
		parmsOutMesaSpot.put("p_out_ErroresContador", Long.class);
		parmsOutMesaSpot.put("p_out_AdvertenciasDetalle", OutAdvertenciasDetalle.class);
		parmsOutMesaSpot.put("p_out_ErroresDetalle", OutErroresDetalle.class);
		
		HashMap<String, Object> outAbrirMesa = sql.call(con, SQL_UPDATE_MESA_SPOT, parmsInMesaSpot, parmsOutMesaSpot);
		
		salida.p_out_ErroresContador += (Long) outAbrirMesa.get("p_out_ErroresContador");
		SQL.exec(con, "DROP TABLE #CierreAperturaMesa_p_DatosMesas");
		
		if(((Long) outAbrirMesa.get("p_out_AdvertenciasContador")) > 0) {
			MensajeSalida mensajeSalida = new MensajeSalida();
			for(OutAdvertenciasDetalle mensaje : (List<OutAdvertenciasDetalle>) outAbrirMesa.get("p_out_AdvertenciasDetalle")) {
				mensajeSalida.id = mensaje.IdInterno;
				mensajeSalida.tipo = "ADVERTENCIA";
				mensajeSalida.codigo = mensaje.Codigo;
				mensajeSalida.descripcion = mensaje.Descripcion;
				salida.p_out_Mensajes.add(mensajeSalida);
			}
		}
		
		if((Long) outAbrirMesa.get("p_out_ErroresContador") > 0) {
			MensajeSalida mensajeSalida = new MensajeSalida();
			for(OutErroresDetalle mensaje : (List<OutErroresDetalle>) outAbrirMesa.get("p_out_ErroresDetalle")) {
				mensajeSalida.id = mensaje.IdInterno;
				mensajeSalida.tipo = "ERROR";
				mensajeSalida.codigo = mensaje.Codigo;
				mensajeSalida.descripcion = mensaje.Descripcion;
				salida.p_out_Mensajes.add(mensajeSalida);
			}
			return salida;
		}
		
		//Se valida la existencia del Tipo Cambio para la fechaSistema nueva
		HashMap<String,Object> parmsInTipoCambio =new HashMap<String,Object>();
		parmsInTipoCambio.put("p_IdEmpresa", entrada.p_IdEmpresa);
		parmsInTipoCambio.put("p_IdUsuario", entrada.p_IdUsuario);
		parmsInTipoCambio.put("p_Fecha", Fecha.toDate(entrada.p_FechaSistema));
		
		HashMap<String,Class> parmsOutTipoCambio =new HashMap<String,Class>();
		parmsOutTipoCambio.put("p_out_ParidadesYTipoCambio", OutParidadesTipoCambio.class);
		parmsOutTipoCambio.put("p_out_AdvertenciasContador", Long.class);
		parmsOutTipoCambio.put("p_out_ErroresContador", Long.class);
		parmsOutTipoCambio.put("p_out_AdvertenciasDetalle", OutAdvertenciasDetalle.class);
		parmsOutTipoCambio.put("p_out_ErroresDetalle", OutErroresDetalle.class);
		
		HashMap<String, Object> outConsultarParidadTipoCambio = sql.call(con, SQL_CONSULTAR_PARIDAD_TIPO_CAMBIO, parmsInTipoCambio, parmsOutTipoCambio);
		
		List<OutParidadesTipoCambio> outParidadTipoCambio = (List<OutParidadesTipoCambio>) outConsultarParidadTipoCambio.get("p_out_ParidadesYTipoCambio");
		OutParidadesTipoCambio paridadTipoCambioDolar = new OutParidadesTipoCambio();
		
		Boolean existeTipoCambio = false;
		Boolean existeLimiteInferiorTipoCambio = false;
		Boolean existeLimiteSuperiorTipoCambio = false;
		for(OutParidadesTipoCambio tipoCambio : outParidadTipoCambio) {
			if("USD".equalsIgnoreCase(tipoCambio.IdMoneda)) {
				if(tipoCambio.TipoCambioMercado.compareTo(BigDecimal.ZERO) != 0)
					existeTipoCambio = true;
				if(tipoCambio.LimiteInferiorTipoCambio.compareTo(BigDecimal.ZERO) != 0)
					existeLimiteInferiorTipoCambio = true;
				if(tipoCambio.LimiteSuperiorTipoCambio.compareTo(BigDecimal.ZERO) != 0)
					existeLimiteSuperiorTipoCambio = true;
				paridadTipoCambioDolar = tipoCambio;
			}
		}
		
		salida.p_out_ErroresContador += (Long) outConsultarParidadTipoCambio.get("p_out_ErroresContador");
		
		String cargaDolarEnCero =  ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "MESA", "CARGADOLARENCEROPAUTA");
				
		if((!existeTipoCambio || !existeLimiteInferiorTipoCambio || !existeLimiteSuperiorTipoCambio) && ("N".equals(cargaDolarEnCero))) {					
				
			try {
				SQL.exec(con, "DROP TABLE #ModificarParidadYTipoCambio_p_DatosParidadYTipoCambio");
			} catch(Exception e) {
				logger.info("Tabla #ModificarParidadYTipoCambio_p_DatosParidadYTipoCambio no existe");
			}
			
			SQL.exec(con, SQL_CREATE_TEMPORARY_TABLE_DATOS_TIPOCAMBIO);
				
			parmsInTipoCambio.put("p_Fecha", Fecha.toDate(entrada.p_FechaSistemaAnterior));
			HashMap<String, Object> outConsultarParidadTipoCambioFechaAnterior = sql.call(con, SQL_CONSULTAR_PARIDAD_TIPO_CAMBIO, parmsInTipoCambio, parmsOutTipoCambio);
				
			BigDecimal tipoCambio = BigDecimal.ZERO;
			BigDecimal limiteInferiorTipoCambio = BigDecimal.ZERO;
			BigDecimal limiteSuperiorTipoCambio = BigDecimal.ZERO;
			for(OutParidadesTipoCambio tcAnterior : (List<OutParidadesTipoCambio>) outConsultarParidadTipoCambioFechaAnterior.get("p_out_ParidadesYTipoCambio")) {
				if("USD".equalsIgnoreCase(tcAnterior.IdMoneda)) {
					tipoCambio = tcAnterior.TipoCambioMercado;
					limiteInferiorTipoCambio = tcAnterior.LimiteInferiorTipoCambio;
					limiteSuperiorTipoCambio = tcAnterior.LimiteSuperiorTipoCambio;
				}
			}
				
			List<InModificarParidadesTipoCambio> inModificarParidadesTipoCambio = new ArrayList<InModificarParidadesTipoCambio>();
			InModificarParidadesTipoCambio paridadTipoCambioNueva = new InModificarParidadesTipoCambio(); 
			paridadTipoCambioNueva.IdParidadYTipoCambio = paridadTipoCambioDolar.IdParidadYTipoCambio;
			paridadTipoCambioNueva.IdMoneda = paridadTipoCambioDolar.IdMoneda;
			paridadTipoCambioNueva.NombreMoneda = paridadTipoCambioDolar.NombreMoneda;
			paridadTipoCambioNueva.ParidadMercado = paridadTipoCambioDolar.ParidadMercado;
			paridadTipoCambioNueva.ParidadBID = paridadTipoCambioDolar.ParidadBID;
			paridadTipoCambioNueva.ParidadASK = paridadTipoCambioDolar.ParidadASK;
			paridadTipoCambioNueva.LimiteSuperiorParidad = paridadTipoCambioDolar.LimiteSuperiorParidad;
			paridadTipoCambioNueva.LimiteInferiorParidad = paridadTipoCambioDolar.LimiteInferiorParidad;
			paridadTipoCambioNueva.ParidadSBIF = paridadTipoCambioDolar.ParidadSBIF;				
			paridadTipoCambioNueva.TipoCambioMercado = paridadTipoCambioDolar.TipoCambioMercado.compareTo(BigDecimal.ZERO) == 0 ? tipoCambio : paridadTipoCambioDolar.TipoCambioMercado;						
			paridadTipoCambioNueva.LimiteSuperiorTipoCambio = paridadTipoCambioDolar.LimiteSuperiorTipoCambio.compareTo(BigDecimal.ZERO) == 0 ? limiteSuperiorTipoCambio : paridadTipoCambioDolar.LimiteSuperiorTipoCambio;
			paridadTipoCambioNueva.LimiteInferiorTipoCambio = paridadTipoCambioDolar.LimiteInferiorTipoCambio.compareTo(BigDecimal.ZERO) == 0 ? limiteInferiorTipoCambio : paridadTipoCambioDolar.LimiteInferiorTipoCambio;
			paridadTipoCambioNueva.PrecioSpotCompra = paridadTipoCambioDolar.PrecioSpotCompra;
			paridadTipoCambioNueva.PrecioSpotVenta = paridadTipoCambioDolar.PrecioSpotVenta;
			paridadTipoCambioNueva.PrecioSpotPromedio = paridadTipoCambioDolar.PrecioSpotPromedio;
			inModificarParidadesTipoCambio.add(paridadTipoCambioNueva);
			
			sql.batch(con, SQL_INSERT_TEMPORARY_TABLE_DATOS_TIPOCAMBIO, inModificarParidadesTipoCambio, InModificarParidadesTipoCambio.class);
				
			HashMap<String,Object> parmsInModificarTipoCambio =new HashMap<String,Object>();
			parmsInModificarTipoCambio.put("p_IdEmpresa", entrada.p_IdEmpresa);
			parmsInModificarTipoCambio.put("p_IdUsuario", entrada.p_IdUsuario);
			parmsInModificarTipoCambio.put("p_IdModulo", entrada.p_IdModulo);
			parmsInModificarTipoCambio.put("p_Fecha", Fecha.toDate(entrada.p_FechaSistema));
			parmsInModificarTipoCambio.put("p_DatosParidadYTipoCambio", "#ModificarParidadYTipoCambio_p_DatosParidadYTipoCambio");
			
			HashMap<String,Class> parmsOutModificarTipoCambio =new HashMap<String,Class>();
			parmsOutModificarTipoCambio.put("p_out_ParidadesYTipoCambio", OutParidadesTipoCambio.class);
			parmsOutModificarTipoCambio.put("p_out_AdvertenciasContador", Long.class);
			parmsOutModificarTipoCambio.put("p_out_ErroresContador", Long.class);
			parmsOutModificarTipoCambio.put("p_out_AdvertenciasDetalle", OutAdvertenciasDetalle.class);
			parmsOutModificarTipoCambio.put("p_out_ErroresDetalle", OutErroresDetalle.class);
			
			HashMap<String, Object> outModificarParidadTipoCambio = sql.call(con, SQL_UPDATE_PARIDAD_TIPO_CAMBIO, parmsInModificarTipoCambio, parmsOutModificarTipoCambio);
		
			salida.p_out_ErroresContador += (Long) outModificarParidadTipoCambio.get("p_out_ErroresContador");
			salida.p_out_Mensaje = "Los datos de Tipo Cambio no fueron cargados previo a la ejecucion de la Pauta, se utilizan datos del dia anterior";
				
			SQL.exec(con, "DROP TABLE #ModificarParidadYTipoCambio_p_DatosParidadYTipoCambio");
		}else {
			salida.p_out_Mensaje = "PautaAbrirModuloMesaSpot finalizado correctamente";
		}		
		return salida;
	} 
	
	public static void main(String[] args) {
		String dbURL  = "jdbc:sap://161.238.15.24:30215/?";
        String dbprop = "currentschema=BSCL;";
        String dbdrv  = "com.sap.db.jdbc.Driver";
        String dbqry  = "select 1 from dummy";
        String user   = "SOPORTE";
        String pass   = "Bolsa123";		
        
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(dbdrv);
        ds.setUsername(user);
        ds.setPassword(pass);
        ds.setUrl(dbURL);
        ds.setValidationQuery(dbqry);
        ds.setConnectionProperties(dbprop);
        ds.setDefaultAutoCommit(false);
        
        try {
 			Connection con = ds.getConnection();
			EntradaAbrirModuloMesaSpot entrada = new EntradaAbrirModuloMesaSpot();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdModulo = "PLA";
			entrada.p_IdUsuario = 1L;
			entrada.p_FechaSistema = "2020-08-20";
			entrada.p_FechaSistemaAnterior = "2020-08-19";
			entrada.p_simularOperacion = "N";
			entrada.p_UsaReglasNegocio = "N";
			
			JsonParser parser 		= new JsonParser();
	        JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
	        CambiarEstadoModuloMesaSpot demo = new CambiarEstadoModuloMesaSpot();		
	    	System.out.println(demo.call(con, dataJsonIn).toString());	
	    	//con.commit();
	    	con.rollback();
	    	con.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
        System.out.println("== END ==");
	}
}
