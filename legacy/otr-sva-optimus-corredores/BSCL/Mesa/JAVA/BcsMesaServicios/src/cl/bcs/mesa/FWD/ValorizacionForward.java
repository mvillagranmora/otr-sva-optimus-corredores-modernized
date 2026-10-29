package cl.bcs.mesa.FWD;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.mesa.utils.ConsultarConfiguracionMesa;
import cl.bcs.mesa.utils.ConsultarConfiguracionMesa.ConfiguracionMesa;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog.DetalleLog;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.InObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.OutObtenerDiaAnterior;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.plataforma.tas.EntregarPreciosTasas;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.Criterio;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Entrada;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Entrada_DatosInstrumentos;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Salida;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Salida_PreciosTasas;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ValorizacionForward implements JavaCall{
	private static final Logger logger = LogManager.getLogger(ValorizacionForward.class);
	
	private final String SQL_SELECT_REQUISITOS_VALORIZACION_FORWARD = loadResourceToString("cl/bcs/mesa/FWD/sql/ValorizacionForward_ConsultarRequisitosValorizacionForward.sql");
	private final String SQL_DELETE_CONTROL_PROCESO_FORWARD = loadResourceToString("cl/bcs/mesa/FWD/sql/ValorizacionForward_DeleteControlProceso.sql");
	private final String SQL_UPDATE_CARTERA_OPERACIONES_FORWARD_ESTADO_VCD = loadResourceToString("cl/bcs/mesa/FWD/sql/ValorizacionForward_UpdateCarteraOperacionesForward.sql");
	private final String SQL_SELECT_OPERACIONES_PROCESO_VALORIZACION_FORWARD = loadResourceToString("cl/bcs/mesa/FWD/sql/ValorizacionForward_ConsultarOperacionesProcesoValorizacionForward.sql");
	private final String SQL_SELECT_CRITERIOS_FORWARD = loadResourceToString("cl/bcs/mesa/FWD/sql/ValorizacionForward_ConsultarCriteriosForward.sql");
	private final String SQL_UPDATE_CARTERA_RESULTADO_VALORIZACION = loadResourceToString("cl/bcs/mesa/FWD/sql/ValorizacionForward_UpdateCarteraResultadoValorizacion.sql");
	private final String SQL_INSERT_CONTROL_PROCESO_FORWARD = loadResourceToString("cl/bcs/mesa/FWD/sql/ValorizacionForward_InsertControlProceso.sql");
	
	
	public static class ValorizacionForwardEntrada extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdPais;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_FechaProceso;
		public String p_IdTipoCanalIngreso;
		
		public ValorizacionForwardEntrada() {
			this.p_IdEmpresa = 1L;
			this.p_IdPais = 0L;
			this.p_IdUsuario = 0L;
			this.p_IdModulo = "FWD";
			this.p_UsaReglasNegocio = "S";
			this.p_FechaProceso = "0001-01-01";
			this.p_IdTipoCanalIngreso = "OPT";
		}
	}
	
	public static class ValorizacionForwardSalida extends BcsPojo {
		public List<MensajeSalida>		P_OUT_MENSAJES;
		public Long						P_OUT_ERRORESCONTADOR;
		public List<ResultadoValorizacion> P_OUT_RESULTADO;
		
		public ValorizacionForwardSalida() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_RESULTADO = new ArrayList<ResultadoValorizacion>();
		}
	}
	
	public static class OperacionProcesoValorizacionForward extends BcsPojo{
		public Long IdCartera;
		public Long FolioCartera;
		public Long IdOperacion;
		public String IdTipoOperacion;
		public Long PlazoEnDiasOperacion;
		public Date FechaOperacion;
		public BigDecimal MontoOperacion;
		public BigDecimal TipoCambioCierre;
		public BigDecimal ParidadCierre;
		public BigDecimal TipoCambioMercado;
		public BigDecimal PrecioSpotCompra;
		public BigDecimal PrecioSpotVenta;
		public BigDecimal PrecioSpotPromedio;
		public BigDecimal TCDolarObservado;
		public BigDecimal TipoCambioMercadoUF;
		public String MultiplicaODivide;
		public String IdMoneda;
		public BigDecimal PrecioUtilizarPTC;
		public BigDecimal FactorMultDiv;
		public Long PlazoResidual;
		
		//Servicio Entrega Tasas
		public Long IdRegistro;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public Long IdInstrumento;
		public String Nemotecnico;
		public String IdMonedaPrincipal;
		public String IdMonedaSecundaria;
		public Date FechaEfectiva;
		public String ValorParametroModalidadLiquidacion;
		public Date FechaVencimientoOperacion;
		public String MensajeError;
		
		//Cartera
		public String IdTipoTasa;
		public BigDecimal Tasa;
		public BigDecimal MontoValorRazonableActivo;
		public BigDecimal MontoValorRazonablePasivo;
		public BigDecimal MontoValorRazonableNeto;
		public BigDecimal TasaValorRazonableActivo;
		public BigDecimal TasaValorRazonablePasivo;
		
	}
	
	public static class ProcesosSqlObj extends BcsPojo{
		public Long IdEmpresa;
		public String FechaProceso;
		
	}
	
	public static class RequisitosValorizacionForward extends BcsPojo{
		public String IdEstadoMesa;
		public String NombreMesa;
		public String IdEstadoModulo;
		public String NombreModulo;
		public Long CantidadCriterios;
		public BigDecimal TipoCambioDO;
		public BigDecimal TipoCambioUF;
		public BigDecimal TipoCambioUSD;
		public BigDecimal PrecioSpotCompraUSD;
		public BigDecimal PrecioSpotVentaUSD;
		public BigDecimal PrecioSpotPromedioUSD;
		
	}
	
	public static class UpdateCartera extends BcsPojo{
		public Long IdCartera;
		public Long IdEmpresa;
		public java.sql.Timestamp FechaValorizacionCartera;
		public Date FechaValorizacionAnterior;
		public Date FechaValorizacionActual;
		public BigDecimal MontoValorRazonableActivo;
		public BigDecimal MontoValorRazonablePasivo;
		public BigDecimal MontoValorRazonableNeto;
		public BigDecimal TasaValorRazonableActivo;
		public BigDecimal TasaValorRazonablePasivo;
		
	}
	
	public static class ResultadoValorizacion extends BcsPojo{
		public Long IdCartera;
		public Long FolioCartera;
		public String IdTipoOperacion;
		public String Nemotecnico;
		public BigDecimal MontoOperacion;
		public String IdMonedaPrincipal;
		public String IdMonedaSecundaria;
		public BigDecimal MontoValorRazonableActivo;
		public BigDecimal TasaValorRazonableActivo;
		public BigDecimal MontoValorRazonablePasivo;
		public BigDecimal TasaValorRazonablePasivo;
		public String MensajeError;
		
	}
	
	public static class ControlProceso extends BcsPojo{	
		public Long IdEmpresa;
		public Long IdUsuario;
		public String FechaSistema; 
		public String ResultadoProceso;
		
	}
	
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		ValorizacionForwardEntrada in = gson.fromJson(jin.toString(), ValorizacionForwardEntrada.class);
		ValorizacionForwardSalida out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public ValorizacionForwardSalida logica(Connection conn, ValorizacionForwardEntrada in) throws Exception {
		Long init = System.currentTimeMillis();
		long startTime = System.nanoTime();
		logger.info("Inicio servicio ValorizacionForward");
		
		SQL _sql = new SQL();
		ValorizacionForwardSalida out = new ValorizacionForwardSalida();
		out.P_OUT_ERRORESCONTADOR = 0L;
		Long Id = 0L;
		HashMap<String,String> mapMsg = new HashMap<>();
		
		String resultadoProcesoTexto = "";
		
		try {
			/***** VALIDACIONES *****/
			HashMap<String,Object> params = new HashMap<>();
			params.put("IdEmpresa", in.p_IdEmpresa);
			params.put("FechaProceso", in.p_FechaProceso);
			
			logger.debug("Inicio SQL_SELECT_REQUISITOS_VALORIZACION_FORWARD");
			startTime = System.nanoTime();
			List<RequisitosValorizacionForward> requisitosValorizacionForward = _sql.query(conn, SQL_SELECT_REQUISITOS_VALORIZACION_FORWARD, params, RequisitosValorizacionForward.class);
			logger.debug("(" + (System.nanoTime() - startTime)/1000000 + ") Fin SQL_SELECT_REQUISITOS_VALORIZACION_FORWARD");
			
			String permiteValorizarConModuloCerrado = "";
			try {
				permiteValorizarConModuloCerrado = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "MESA", "WS_PERMITE_VALORIZAR_MODULO_FWD_CERRADO");
			}catch (Exception ex) {
				HashMap<String,String> msjParam = new HashMap<String,String>(1);
				msjParam.put("CodigoParametro", "PERMITE_VALORIZAR_MODULO_FWD_CERRADO");
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,"UI_INFO_PARAMETRO_NO_EXISTE",msjParam,conn,in.p_IdEmpresa));
				permiteValorizarConModuloCerrado = "N";
				msjParam.clear();
			}
			
			for (RequisitosValorizacionForward req: requisitosValorizacionForward){
				if("A".equals(req.IdEstadoMesa)){
					mapMsg.put("NombreMesa", req.NombreMesa);
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"MESA_FORWARD_ABIERTA",mapMsg,conn,in.p_IdEmpresa));
					Id++;
				}
				if("CER".equals(req.IdEstadoModulo) && !("S".equals(permiteValorizarConModuloCerrado) && "WBS".equals(in.p_IdTipoCanalIngreso))){
					mapMsg.put("NombreModulo", req.NombreModulo);
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"MODULO_FORWARD_CERRADO",mapMsg,conn,in.p_IdEmpresa));
					Id++;
				}
				if(req.CantidadCriterios.longValue() == 0){
					mapMsg.put("CantidadCriterios", req.CantidadCriterios.toString());
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"NO_EXISTE_CRITERIO_DE_TASAS",mapMsg,conn,in.p_IdEmpresa));
					Id++;
				}
				if(req.TipoCambioDO.compareTo(BigDecimal.ZERO) == 0){
					mapMsg.put("TipoCambioDO", req.TipoCambioDO.toString());
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"TIPO_CAMBIO_DO_CERO_MANTENEDOR_PYTC",mapMsg,conn,in.p_IdEmpresa));
					Id++;
				}
				if(req.TipoCambioUF.compareTo(BigDecimal.ZERO) == 0){
					mapMsg.put("TipoCambioUF", req.TipoCambioUF.toString());
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"TIPO_CAMBIO_UF_CERO_MANTENEDOR_PYTC",mapMsg,conn,in.p_IdEmpresa));
					Id++;
				}
				if(req.TipoCambioUSD.compareTo(BigDecimal.ZERO) == 0){
					mapMsg.put("TipoCambioUSD", req.TipoCambioUSD.toString());
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"TIPO_CAMBIO_USD_CERO_MANTENEDOR_PYTC",mapMsg,conn,in.p_IdEmpresa));
					Id++;
				}
				if(req.PrecioSpotCompraUSD.compareTo(BigDecimal.ZERO) == 0){
					mapMsg.put("PrecioSpotCompraUSD", req.PrecioSpotCompraUSD.toString());
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"PRECIO_SPOT_COMPRA_USD_CERO_MANTENEDOR_PYTC",mapMsg,conn,in.p_IdEmpresa));
					Id++;
				}
				if(req.PrecioSpotVentaUSD.compareTo(BigDecimal.ZERO) == 0){
					mapMsg.put("PrecioSpotVentaUSD", req.PrecioSpotVentaUSD.toString());
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"PRECIO_SPOT_VENTA_USD_CERO_MANTENEDOR_PYTC",mapMsg,conn,in.p_IdEmpresa));
					Id++;
				}
				if(req.PrecioSpotPromedioUSD.compareTo(BigDecimal.ZERO) == 0){
					mapMsg.put("PrecioSpotPromedioUSD", req.PrecioSpotPromedioUSD.toString());
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"PRECIO_SPOT_PROMEDIO_USD_CERO_MANTENEDOR_PYTC",mapMsg,conn,in.p_IdEmpresa));
					Id++;
				}
			}
			
			//Retorna si existen errores 
			if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0){
				return out;
			}
			
			
			/***** DELETE CONTROL PROCESO *****/
			List<ProcesosSqlObj> procesosSqlObj = new ArrayList<ProcesosSqlObj>();
			ProcesosSqlObj oasb = new ProcesosSqlObj();
			oasb.IdEmpresa = in.p_IdEmpresa;
			oasb.FechaProceso = in.p_FechaProceso;
			procesosSqlObj.add(oasb);
			_sql.batch(conn, SQL_DELETE_CONTROL_PROCESO_FORWARD, procesosSqlObj, ProcesosSqlObj.class);
			
			
			/***** UPDATE CARTERA OPERACIONES FORWARD *****/
			_sql.batch(conn, SQL_UPDATE_CARTERA_OPERACIONES_FORWARD_ESTADO_VCD, procesosSqlObj, ProcesosSqlObj.class);
			
			
			/***** OPERACIONES PARA PROCESO VALORIZACION FORWARD *****/
			ConfiguracionMesa configuracionMesa = new ConsultarConfiguracionMesa().logica(conn, in.p_IdEmpresa, "MEFW", "OCUPAFECHAEFECTIVA");
			String ocupaFechaEfectiva = configuracionMesa.Valor;
			params.clear();
			params.put("IdEmpresa", in.p_IdEmpresa);
			params.put("FechaProceso", Fecha.toDate(in.p_FechaProceso));
			params.put("OcupaFechaEfectiva", ocupaFechaEfectiva);
			//logger.debug("Inicio SQL_SELECT_OPERACIONES_PROCESO_VALORIZACION_FORWARD");
			startTime = System.nanoTime();
			List<OperacionProcesoValorizacionForward> operacionesForward = _sql.query(conn, SQL_SELECT_OPERACIONES_PROCESO_VALORIZACION_FORWARD, params, OperacionProcesoValorizacionForward.class);
			//logger.debug("(" + (System.nanoTime() - startTime)/1000000 + ") Fin SQL_SELECT_OPERACIONES_PROCESO_VALORIZACION_FORWARD");

			
			//Variables
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn,in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			
			//Valida si existen operaciones que apliquen al proceso de Valorizacion
			if(operacionesForward.size() > 0){

				/*
				mapMsg.put("FechaProceso", in.p_FechaProceso);
				out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"NO_EXISTEN_OPERACIONES_PARA_PROCESO_VALORIZACION",mapMsg,conn,in.p_IdEmpresa));
				Id++;*/
				//return out;

				//Se genera IdRegistro para entrada del Servicio EntregarPreciosTasas
				List<Long> secuenciaIdRegistro = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::TAS.IdRegistroEntregarPreciosTasas",Long.valueOf(operacionesForward.size() * 2));
				int i = 0;
				
				EntregarPreciosTasas_Entrada entregarPreciosTasasEntrada = new EntregarPreciosTasas_Entrada();
				entregarPreciosTasasEntrada.p_IdEmpresa = in.p_IdEmpresa;
				entregarPreciosTasasEntrada.p_IdModulo = in.p_IdModulo;
				entregarPreciosTasasEntrada.p_IdPais = in.p_IdPais;
				entregarPreciosTasasEntrada.p_IdUsuario = in.p_IdUsuario;
				
				List<OperacionProcesoValorizacionForward> operacionFwdProceso = new ArrayList<OperacionProcesoValorizacionForward>();
				for(OperacionProcesoValorizacionForward opeFwd: operacionesForward){
					//Obtiene el Precio a utilizar (PrecioUtilizarPTC). Obtiene Factor TCDolar si Multiplica o Divide (FactorMultDiv)
					if(opeFwd.IdTipoOperacion.equals("COMFWD") && !(opeFwd.PrecioSpotVenta.compareTo(BigDecimal.ZERO) == 0)){
						opeFwd.PrecioUtilizarPTC = opeFwd.PrecioSpotVenta;
					}
					else if(opeFwd.IdTipoOperacion.equals("VENFWD") && !(opeFwd.PrecioSpotCompra.compareTo(BigDecimal.ZERO) == 0)){
						opeFwd.PrecioUtilizarPTC = opeFwd.PrecioSpotCompra;
					}
					else if(!(opeFwd.PrecioSpotPromedio.compareTo(BigDecimal.ZERO) == 0)){
						opeFwd.PrecioUtilizarPTC = opeFwd.PrecioSpotPromedio;
					}
					else{
						opeFwd.PrecioUtilizarPTC = opeFwd.TipoCambioMercado;
					}
					
					if(opeFwd.MultiplicaODivide.equals("D") && !(opeFwd.ParidadCierre.compareTo(BigDecimal.ZERO) == 0)){
						opeFwd.FactorMultDiv = opeFwd.TCDolarObservado.divide(opeFwd.ParidadCierre,8,RoundingMode.HALF_UP);
					}
					else if(opeFwd.MultiplicaODivide.equals("D") && opeFwd.ParidadCierre.compareTo(BigDecimal.ZERO) == 0){
						opeFwd.FactorMultDiv = BigDecimal.ZERO;
					}
					else{
						opeFwd.FactorMultDiv = opeFwd.ParidadCierre.multiply(opeFwd.TCDolarObservado);
					}
										
					//Separa IdMonedaPrincipal de cada operacion FWD
					OperacionProcesoValorizacionForward operacion = copyPojoOperacionProcesoValorizacionForward(opeFwd);
					operacion.IdMoneda = opeFwd.IdMonedaPrincipal;
					operacion.IdRegistro = secuenciaIdRegistro.get(i);
					operacionFwdProceso.add(operacion);
					i++;
					
					EntregarPreciosTasas_Entrada_DatosInstrumentos entradaDatosInstrumentos = copyPojoEntradaDatosInstrumentos(operacion);
					entradaDatosInstrumentos.TipoCartera = "0";
					entradaDatosInstrumentos.IdUso = "CARTERA";
					entradaDatosInstrumentos.IdTipoCriterioTasa = "0";
					entradaDatosInstrumentos.FechaConsulta = Fecha.toDate(in.p_FechaProceso);
					entregarPreciosTasasEntrada.p_DatosInstrumentos.add(entradaDatosInstrumentos);
					
					//Separa IdMonedaSecundaria de cada operacion FWD
					operacion = copyPojoOperacionProcesoValorizacionForward(opeFwd);
					operacion.IdMoneda = opeFwd.IdMonedaSecundaria;
					operacion.IdRegistro = secuenciaIdRegistro.get(i);
					operacionFwdProceso.add(operacion);
					
					entradaDatosInstrumentos = copyPojoEntradaDatosInstrumentos(operacion);
					entradaDatosInstrumentos.TipoCartera = "0";
					entradaDatosInstrumentos.IdUso = "CARTERA";
					entradaDatosInstrumentos.IdTipoCriterioTasa = "0";
					entradaDatosInstrumentos.FechaConsulta = Fecha.toDate(in.p_FechaProceso);
					entregarPreciosTasasEntrada.p_DatosInstrumentos.add(entradaDatosInstrumentos);
					i++;
				}
				
				
				/***** VALIDACION ADICIONAL: existen Criterios de Seleccion de Tasas que aplican a las operaciones FORWARD del proceso en curso *****/
				//logger.debug("Inicio SQL_SELECT_CRITERIOS_FORWARD");
				//startTime = System.nanoTime();
				params.clear();
				params.put("IdEmpresa", in.p_IdEmpresa);
				List<Criterio> criteriosForward = _sql.query(conn, SQL_SELECT_CRITERIOS_FORWARD, params, Criterio.class);
				//logger.debug("(" + (System.nanoTime() - startTime)/1000000 + ") Fin SQL_SELECT_CRITERIOS_FORWARD");
				
				if (criteriosForward.size() == 0) {
					mapMsg.put("FechaProceso", in.p_FechaProceso);
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"NO_EXISTEN_CRITERIOS_PARA_ENTRADA_A_SERVICIO_ENTREGA_TASAS",mapMsg,conn,in.p_IdEmpresa));
					Id++;
					return out;
				}
				
				
				/***** SERVICIO EntregarPreciosTasas *****/
				List<OperacionProcesoValorizacionForward> operacionesCompraActivo = new ArrayList<OperacionProcesoValorizacionForward>();
				List<OperacionProcesoValorizacionForward> operacionesCompraPasivo = new ArrayList<OperacionProcesoValorizacionForward>();
				List<OperacionProcesoValorizacionForward> operacionesVentaActivo = new ArrayList<OperacionProcesoValorizacionForward>();
				List<OperacionProcesoValorizacionForward> operacionesVentaPasivo = new ArrayList<OperacionProcesoValorizacionForward>();
				
				if (entregarPreciosTasasEntrada.p_DatosInstrumentos.size() > 0) {
					EntregarPreciosTasas entregarPreciosTasasService = new EntregarPreciosTasas();
					EntregarPreciosTasas_Salida entregarPreciosTasasSalida = entregarPreciosTasasService.logica(conn, entregarPreciosTasasEntrada);
					HashMap<String, Object> tasasSalidaHash = Utils.List2Hash(entregarPreciosTasasSalida.P_OUT_PRECIOSTASAS, "IdRegistro");
					
					if (MensajeSalida.getNumeroErrores(entregarPreciosTasasSalida.P_OUT_MENSAJES) > 0){
						return out;
					}
					
					for (OperacionProcesoValorizacionForward operacion : operacionFwdProceso) {
						EntregarPreciosTasas_Salida_PreciosTasas tasaSalida = (EntregarPreciosTasas_Salida_PreciosTasas) tasasSalidaHash.get((Object) operacion.IdRegistro);
						if (tasaSalida != null) {
							operacion.MensajeError = tasaSalida.MensajeError;
							operacion.IdTipoTasa = tasaSalida.IdTipoTasa;
							operacion.Tasa = tasaSalida.Tasa;
						}
						
						//Operaciones Compra
						if ("COMFWD".equals(operacion.IdTipoOperacion)) {
							if (operacion.IdMonedaPrincipal.equals(operacion.IdMoneda)) {
								operacionesCompraActivo.add(operacion);
							}
							else if (operacion.IdMonedaSecundaria.equals(operacion.IdMoneda)) {
								operacionesCompraPasivo.add(operacion);
							}
						}
						//Operaciones Venta
						else if ("VENFWD".equals(operacion.IdTipoOperacion)) {
							if (operacion.IdMonedaSecundaria.equals(operacion.IdMoneda)) {
								operacionesVentaActivo.add(operacion);
							}
							else if (operacion.IdMonedaPrincipal.equals(operacion.IdMoneda)) {
								operacionesVentaPasivo.add(operacion);
							}
						}
					}
				}
				HashMap<String, Object> operacionesCompraPasivoHash = Utils.List2Hash(operacionesCompraPasivo, "IdCartera");
				HashMap<String, Object> operacionesVentaPasivoHash = Utils.List2Hash(operacionesVentaPasivo, "IdCartera");
				
				
				/***** CALCULOS VALOR RAZONABLE *****/
				configuracionMesa = new ConsultarConfiguracionMesa().logica(conn, in.p_IdEmpresa, "MEFW", "VALRAZONABLECOMPUETOSIMPLE");
				String valorRazonableCompuestoSimple = configuracionMesa.Valor;
				
				//Obtiene dia habil anterior para setear el valor de INGFechaValorizacionAnterior
				InObtenerDiaAnterior.FechasIn fecha = new InObtenerDiaAnterior.FechasIn();
				fecha.fechaInicial = in.p_FechaProceso;
				fecha.numeroDias = 1L;
				
				ObtenerDiaAnterior obtenerDiaAnterior = new ObtenerDiaAnterior();
				InObtenerDiaAnterior diaAnteriorIn = new InObtenerDiaAnterior();
				diaAnteriorIn.idPais = in.p_IdPais;
				diaAnteriorIn.diaHabil = "S";
				diaAnteriorIn.fechas.add(fecha);
				OutObtenerDiaAnterior diaAnteriorOut = obtenerDiaAnterior.logica(conn, diaAnteriorIn);
				String fechaAnterior = diaAnteriorOut.fechas.get(0).fechaCalculada;
				
				List<UpdateCartera> updateCarteraList = new ArrayList<UpdateCartera>();
				List<ResultadoValorizacion> resultadoList = new ArrayList<ResultadoValorizacion>();
				
				//Operaciones Compra
				for (OperacionProcesoValorizacionForward opeComAct : operacionesCompraActivo) {
					OperacionProcesoValorizacionForward opeComPas = (OperacionProcesoValorizacionForward) operacionesCompraPasivoHash.get((Object) opeComAct.IdCartera);
					if (opeComAct.Tasa != null && opeComPas.Tasa != null) {
						opeComAct.TasaValorRazonableActivo = opeComAct.Tasa;
						opeComAct.TasaValorRazonablePasivo = opeComPas.Tasa;
						
						//Valor Razonable Simple
						if ("N".equals(valorRazonableCompuestoSimple)) {
							if (Arrays.asList("SECANO", "SEINFL", "SECARE").contains(opeComAct.Nemotecnico)) {
								BigDecimal esSecare = "SECARE".equals(opeComAct.Nemotecnico) ? opeComPas.TipoCambioMercadoUF : BigDecimal.ONE;
								if ("PUNTO".equals(opeComAct.IdTipoTasa)) {
									opeComAct.MontoValorRazonableActivo = opeComAct.MontoOperacion.multiply(opeComAct.PrecioUtilizarPTC.add(opeComAct.Tasa)).setScale(0, RoundingMode.HALF_UP);
									opeComAct.MontoValorRazonablePasivo = opeComPas.MontoOperacion.multiply(opeComPas.TipoCambioCierre.multiply(esSecare)).setScale(0, RoundingMode.HALF_UP);
								}
								else {
									BigDecimal factor = (opeComAct.Tasa.divide(BigDecimal.valueOf(100),12,RoundingMode.HALF_UP)).divide(BigDecimal.valueOf(360),12,RoundingMode.HALF_UP);
									BigDecimal divisor = (factor.multiply(BigDecimal.valueOf(opeComAct.PlazoResidual))).add(BigDecimal.ONE);
									opeComAct.MontoValorRazonableActivo = opeComAct.MontoOperacion.multiply(opeComAct.PrecioUtilizarPTC.divide(divisor,12,RoundingMode.HALF_UP)).setScale(0, RoundingMode.HALF_UP);
									
									factor = (opeComPas.Tasa.divide(BigDecimal.valueOf(100),12,RoundingMode.HALF_UP)).divide(BigDecimal.valueOf(360),12,RoundingMode.HALF_UP);
									divisor = (factor.multiply(BigDecimal.valueOf(opeComPas.PlazoResidual))).add(BigDecimal.ONE);
									BigDecimal montoNocionalCLP = (opeComPas.MontoOperacion.multiply(opeComPas.TipoCambioCierre)).setScale(12, RoundingMode.DOWN);
									opeComAct.MontoValorRazonablePasivo = montoNocionalCLP.divide(divisor,12,RoundingMode.HALF_UP).multiply(esSecare).setScale(0, RoundingMode.HALF_UP);
								}
							}
							else if ("ARBITR".equals(opeComAct.Nemotecnico)) {
								if ("PUNTO".equals(opeComAct.IdTipoTasa)) {
									opeComAct.MontoValorRazonableActivo = opeComAct.MontoOperacion.multiply(opeComAct.FactorMultDiv.add(opeComAct.Tasa)).setScale(0, RoundingMode.HALF_UP);;
									opeComAct.MontoValorRazonablePasivo = opeComPas.MontoOperacion.multiply(opeComPas.FactorMultDiv.add(opeComPas.Tasa)).setScale(0, RoundingMode.HALF_UP);;
								}
								else {
									BigDecimal factor = (opeComAct.Tasa.divide(BigDecimal.valueOf(100),12,RoundingMode.HALF_UP)).divide(BigDecimal.valueOf(360),12,RoundingMode.HALF_UP);
									BigDecimal divisor = (factor.multiply(BigDecimal.valueOf(opeComAct.PlazoResidual))).add(BigDecimal.ONE);
									opeComAct.MontoValorRazonableActivo = opeComAct.MontoOperacion.multiply(opeComAct.FactorMultDiv.divide(divisor,12,RoundingMode.HALF_UP)).setScale(0, RoundingMode.HALF_UP);
									
									factor = (opeComPas.Tasa.divide(BigDecimal.valueOf(100),12,RoundingMode.HALF_UP)).divide(BigDecimal.valueOf(360),12,RoundingMode.HALF_UP);
									divisor = (factor.multiply(BigDecimal.valueOf(opeComPas.PlazoResidual))).add(BigDecimal.ONE);
									opeComAct.MontoValorRazonablePasivo = opeComPas.MontoOperacion.multiply(opeComPas.FactorMultDiv.divide(divisor,12,RoundingMode.HALF_UP)).setScale(0, RoundingMode.HALF_UP);
								}
							}
						}
						//Valor Razonable Compuesto
						else if ("S".equals(valorRazonableCompuestoSimple)) {
							if (Arrays.asList("SECANO", "SEINFL", "SECARE").contains(opeComAct.Nemotecnico)) {
								BigDecimal potencia = (BigDecimal.ONE.add(opeComAct.Tasa.divide(BigDecimal.valueOf(100),12,RoundingMode.HALF_UP))).pow(opeComAct.PlazoResidual.intValue() / 360);
								opeComAct.MontoValorRazonableActivo = opeComAct.MontoOperacion.multiply(opeComAct.PrecioUtilizarPTC.divide(potencia,12,RoundingMode.HALF_UP)).setScale(0, RoundingMode.HALF_UP);
								
								BigDecimal esSecare = "SECARE".equals(opeComAct.Nemotecnico) ? opeComPas.TipoCambioMercadoUF : BigDecimal.ONE;
								potencia = (BigDecimal.ONE.add(opeComPas.Tasa.divide(BigDecimal.valueOf(100),12,RoundingMode.HALF_UP))).pow(opeComPas.PlazoResidual.intValue() / 360);
								BigDecimal montoNocionalCLP = (opeComPas.MontoOperacion.multiply(opeComPas.TipoCambioCierre)).setScale(12, RoundingMode.HALF_UP);
								opeComAct.MontoValorRazonablePasivo = montoNocionalCLP.divide(potencia,12,RoundingMode.HALF_UP).multiply(esSecare).setScale(0, RoundingMode.HALF_UP);
							}
							else if ("ARBITR".equals(opeComAct.Nemotecnico)) {
								BigDecimal potencia = (BigDecimal.ONE.add(opeComAct.Tasa.divide(BigDecimal.valueOf(100),12,RoundingMode.HALF_UP))).pow(opeComAct.PlazoResidual.intValue() / 360);
								opeComAct.MontoValorRazonableActivo = opeComAct.MontoOperacion.multiply(opeComAct.FactorMultDiv.divide(potencia,12,RoundingMode.HALF_UP)).setScale(0, RoundingMode.HALF_UP);
								
								potencia = (BigDecimal.ONE.add(opeComPas.Tasa.divide(BigDecimal.valueOf(100),12,RoundingMode.HALF_UP))).pow(opeComPas.PlazoResidual.intValue() / 360);
								opeComAct.MontoValorRazonablePasivo = opeComPas.MontoOperacion.multiply(opeComPas.FactorMultDiv.divide(potencia,12,RoundingMode.HALF_UP)).setScale(0, RoundingMode.HALF_UP);
							}
						}
						opeComAct.MontoValorRazonableNeto = opeComAct.MontoValorRazonableActivo.subtract(opeComAct.MontoValorRazonablePasivo);
						
						UpdateCartera updateCartera = new UpdateCartera();
						updateCartera.IdCartera = opeComAct.IdCartera;
						updateCartera.IdEmpresa = in.p_IdEmpresa;
						updateCartera.FechaValorizacionCartera = Fecha.toDateTime(in.p_FechaProceso);
						updateCartera.FechaValorizacionAnterior = Fecha.toDate(fechaAnterior);
						updateCartera.FechaValorizacionActual = Fecha.toDate(fechaSistema);
						updateCartera.MontoValorRazonableActivo = opeComAct.MontoValorRazonableActivo != null ? opeComAct.MontoValorRazonableActivo : BigDecimal.ZERO;
						updateCartera.MontoValorRazonablePasivo = opeComAct.MontoValorRazonablePasivo != null ? opeComAct.MontoValorRazonablePasivo : BigDecimal.ZERO;
						updateCartera.MontoValorRazonableNeto = opeComAct.MontoValorRazonableNeto != null ? opeComAct.MontoValorRazonableNeto : BigDecimal.ZERO;
						updateCartera.TasaValorRazonableActivo = opeComAct.TasaValorRazonableActivo != null ? opeComAct.TasaValorRazonableActivo : BigDecimal.ZERO;
						updateCartera.TasaValorRazonablePasivo = opeComAct.TasaValorRazonablePasivo != null ? opeComAct.TasaValorRazonablePasivo : BigDecimal.ZERO;
						updateCarteraList.add(updateCartera);
					}
					
					//Salida
					ResultadoValorizacion resultado = copyPojoResultadoValorizacion(opeComAct);
					resultadoList.add(resultado);
				}
				
				//Operaciones Venta
				for (OperacionProcesoValorizacionForward opeVenAct : operacionesVentaActivo) {
					OperacionProcesoValorizacionForward opeVenPas = (OperacionProcesoValorizacionForward) operacionesVentaPasivoHash.get((Object) opeVenAct.IdCartera);
					if (opeVenAct.Tasa != null && opeVenPas.Tasa != null) {
						opeVenAct.TasaValorRazonableActivo = opeVenAct.Tasa;
						opeVenAct.TasaValorRazonablePasivo = opeVenPas.Tasa;
						
						//Valor Razonable Simple
						if ("N".equals(valorRazonableCompuestoSimple)) {
							if (Arrays.asList("SECANO", "SEINFL", "SECARE").contains(opeVenAct.Nemotecnico)) {
								BigDecimal esSecare = "SECARE".equals(opeVenAct.Nemotecnico) ? opeVenPas.TipoCambioMercadoUF : BigDecimal.ONE;
								if ("PUNTO".equals(opeVenAct.IdTipoTasa)) {
									opeVenAct.MontoValorRazonableActivo = opeVenAct.MontoOperacion.multiply(opeVenAct.PrecioUtilizarPTC.add(opeVenAct.Tasa)).setScale(0, RoundingMode.HALF_UP);
									opeVenAct.MontoValorRazonablePasivo = opeVenPas.MontoOperacion.multiply(opeVenPas.TipoCambioCierre.multiply(esSecare)).setScale(0, RoundingMode.HALF_UP);
								}
								else {
									BigDecimal factor = (opeVenAct.Tasa.divide(BigDecimal.valueOf(100),12,RoundingMode.HALF_UP)).divide(BigDecimal.valueOf(360),12,RoundingMode.HALF_UP);
									BigDecimal divisor = (factor.multiply(BigDecimal.valueOf(opeVenAct.PlazoResidual))).add(BigDecimal.ONE);
									BigDecimal montoNocionalCLP = (opeVenAct.MontoOperacion.multiply(opeVenAct.TipoCambioCierre)).setScale(12, RoundingMode.HALF_UP);
									opeVenAct.MontoValorRazonableActivo = montoNocionalCLP.divide(divisor,12,RoundingMode.HALF_UP).multiply(esSecare).setScale(0, RoundingMode.HALF_UP);
									
									factor = (opeVenPas.Tasa.divide(BigDecimal.valueOf(100),12,RoundingMode.HALF_UP)).divide(BigDecimal.valueOf(360),12,RoundingMode.HALF_UP);
									divisor = (factor.multiply(BigDecimal.valueOf(opeVenPas.PlazoResidual))).add(BigDecimal.ONE);
									opeVenAct.MontoValorRazonablePasivo = (opeVenPas.MontoOperacion.multiply(opeVenPas.PrecioUtilizarPTC)).divide(divisor,12,RoundingMode.HALF_UP).setScale(0, RoundingMode.HALF_UP);
								}
							}
							else if ("ARBITR".equals(opeVenAct.Nemotecnico)) {
								if ("PUNTO".equals(opeVenAct.IdTipoTasa)) {
									opeVenAct.MontoValorRazonableActivo = opeVenAct.MontoOperacion.multiply(opeVenAct.FactorMultDiv.add(opeVenAct.Tasa)).setScale(0, RoundingMode.HALF_UP);
									opeVenAct.MontoValorRazonablePasivo = opeVenPas.MontoOperacion.multiply(opeVenPas.FactorMultDiv.add(opeVenPas.Tasa)).setScale(0, RoundingMode.HALF_UP);
								}
								else {
									BigDecimal factor = (opeVenAct.Tasa.divide(BigDecimal.valueOf(100),12,RoundingMode.HALF_UP)).divide(BigDecimal.valueOf(360),12,RoundingMode.HALF_UP);
									BigDecimal divisor = (factor.multiply(BigDecimal.valueOf(opeVenAct.PlazoResidual))).add(BigDecimal.ONE);
									opeVenAct.MontoValorRazonableActivo = opeVenAct.MontoOperacion.multiply(opeVenAct.FactorMultDiv.divide(divisor,12,RoundingMode.HALF_UP)).setScale(0, RoundingMode.HALF_UP);
									
									factor = (opeVenPas.Tasa.divide(BigDecimal.valueOf(100),12,RoundingMode.HALF_UP)).divide(BigDecimal.valueOf(360),12,RoundingMode.HALF_UP);
									divisor = (factor.multiply(BigDecimal.valueOf(opeVenPas.PlazoResidual))).add(BigDecimal.ONE);
									opeVenAct.MontoValorRazonablePasivo = opeVenPas.MontoOperacion.multiply(opeVenPas.FactorMultDiv.divide(divisor,12,RoundingMode.HALF_UP)).setScale(0, RoundingMode.HALF_UP);
								}
							}
						}
						//Valor Razonable Compuesto
						else if ("S".equals(valorRazonableCompuestoSimple)) {
							if (Arrays.asList("SECANO", "SEINFL", "SECARE").contains(opeVenAct.Nemotecnico)) {
								BigDecimal potencia = (BigDecimal.ONE.add(opeVenAct.Tasa.divide(BigDecimal.valueOf(100),12,RoundingMode.HALF_UP))).pow(opeVenAct.PlazoResidual.intValue() / 360);
								opeVenAct.MontoValorRazonableActivo = opeVenAct.MontoOperacion.multiply(opeVenAct.PrecioUtilizarPTC.divide(potencia,12,RoundingMode.HALF_UP)).setScale(0, RoundingMode.HALF_UP);
								
								BigDecimal esSecare = "SECARE".equals(opeVenAct.Nemotecnico) ? opeVenPas.TipoCambioMercadoUF : BigDecimal.ONE;
								potencia = (BigDecimal.ONE.add(opeVenPas.Tasa.divide(BigDecimal.valueOf(100),12,RoundingMode.HALF_UP))).pow(opeVenPas.PlazoResidual.intValue() / 360);
								BigDecimal montoNocionalCLP = (opeVenPas.MontoOperacion.multiply(opeVenPas.TipoCambioCierre)).setScale(12, RoundingMode.HALF_UP);
								opeVenAct.MontoValorRazonablePasivo = montoNocionalCLP.divide(potencia,12,RoundingMode.HALF_UP).multiply(esSecare).setScale(0, RoundingMode.HALF_UP);
							}
							else if ("ARBITR".equals(opeVenAct.Nemotecnico)) {
								BigDecimal potencia = (BigDecimal.ONE.add(opeVenAct.Tasa.divide(BigDecimal.valueOf(100),12,RoundingMode.HALF_UP))).pow(opeVenAct.PlazoResidual.intValue() / 360);
								opeVenAct.MontoValorRazonableActivo = opeVenAct.MontoOperacion.multiply(opeVenAct.FactorMultDiv.divide(potencia,12,RoundingMode.HALF_UP)).setScale(0, RoundingMode.HALF_UP);
								
								potencia = (BigDecimal.ONE.add(opeVenPas.Tasa.divide(BigDecimal.valueOf(100),12,RoundingMode.HALF_UP))).pow(opeVenPas.PlazoResidual.intValue() / 360);
								opeVenAct.MontoValorRazonablePasivo = opeVenPas.MontoOperacion.multiply(opeVenPas.FactorMultDiv.divide(potencia,12,RoundingMode.HALF_UP)).setScale(0, RoundingMode.HALF_UP);
							}
						}
						opeVenAct.MontoValorRazonableNeto = opeVenAct.MontoValorRazonableActivo.subtract(opeVenAct.MontoValorRazonablePasivo);
						
						UpdateCartera updateCartera = new UpdateCartera();
						updateCartera.IdCartera = opeVenAct.IdCartera;
						updateCartera.IdEmpresa = in.p_IdEmpresa;
						updateCartera.FechaValorizacionCartera = Fecha.toDateTime(in.p_FechaProceso);
						updateCartera.FechaValorizacionAnterior = Fecha.toDate(fechaAnterior);
						updateCartera.FechaValorizacionActual = Fecha.toDate(fechaSistema);
						updateCartera.MontoValorRazonableActivo = opeVenAct.MontoValorRazonableActivo != null ? opeVenAct.MontoValorRazonableActivo : BigDecimal.ZERO;
						updateCartera.MontoValorRazonablePasivo = opeVenAct.MontoValorRazonablePasivo != null ? opeVenAct.MontoValorRazonablePasivo : BigDecimal.ZERO;
						updateCartera.MontoValorRazonableNeto = opeVenAct.MontoValorRazonableNeto != null ? opeVenAct.MontoValorRazonableNeto : BigDecimal.ZERO;
						updateCartera.TasaValorRazonableActivo = opeVenAct.TasaValorRazonableActivo != null ? opeVenAct.TasaValorRazonableActivo : BigDecimal.ZERO;
						updateCartera.TasaValorRazonablePasivo = opeVenAct.TasaValorRazonablePasivo != null ? opeVenAct.TasaValorRazonablePasivo : BigDecimal.ZERO;
						updateCarteraList.add(updateCartera);
					}
					
					//Salida
					ResultadoValorizacion resultado = copyPojoResultadoValorizacion(opeVenAct);
					resultadoList.add(resultado);
				}
				
				/***** UPDATE CARTERA *****/
				if (updateCarteraList.size() > 0) {
					_sql.batch(conn, SQL_UPDATE_CARTERA_RESULTADO_VALORIZACION, updateCarteraList, UpdateCartera.class);
					
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_INFORMACION,"OPERACIONES_FWD_VALORIZADAS","OPERACIONES_FWD_VALORIZADAS: " + updateCarteraList.size()));
					
					//out.P_OUT_RESULTADO = new ArrayList<ResultadoValorizacion>(resultadoList);//se comenta para no sumar a los tiempos de descarga del contenido.
				}
				/***** SALIDA *****/
				out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
				
				//Agregar log
				AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
				for (ResultadoValorizacion resultado : resultadoList) {
					DetalleLog detalleLog = new DetalleLog();
					//detalleLog.IdTrabajo = ;
					detalleLog.IdCabeceraLog = Long.parseLong(fechaSistema.replace("-", "").replace("/", ""));
					detalleLog.SubRegistro = resultado.FolioCartera.toString(); 
					detalleLog.CampoRegistro = "MontoValorRazonableActivo";
					detalleLog.ValorAntiguo = "0";
					detalleLog.ValorNuevo = resultado.MontoValorRazonableActivo != null ? resultado.MontoValorRazonableActivo.toString() : "VALOR TASA NO ENCONTRADO";
					entradaAgregarLog.p_LogDetalle.add(detalleLog);
					
					detalleLog = new DetalleLog();
					//detalleLog.IdTrabajo = ;
					detalleLog.IdCabeceraLog = Long.parseLong(fechaSistema.replace("-", "").replace("/", ""));
					detalleLog.SubRegistro = resultado.FolioCartera.toString(); 
					detalleLog.CampoRegistro = "MontoValorRazonablePasivo";
					detalleLog.ValorAntiguo = "0";
					detalleLog.ValorNuevo = resultado.MontoValorRazonablePasivo != null ? resultado.MontoValorRazonablePasivo.toString() : "VALOR TASA NO ENCONTRADO";
					entradaAgregarLog.p_LogDetalle.add(detalleLog);
				}

				AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
				log.IdCabeceraLog = Long.parseLong(fechaSistema.replace("-", "").replace("/", ""));
				log.IdTipoLog = "VALORIZACION";
				log.IdModulo = in.p_IdModulo;
				log.IdUsuario = in.p_IdUsuario;
				log.IdEmpresa = in.p_IdEmpresa;
				log.FechaSistemaLog = fechaSistema;
				log.IdUsuarioAtendido = in.p_IdUsuario;
				//log.IdCliente = opeFwd.IdCliente;
				//log.EstadoCliente = "";
				//log.NombreCliente = "";
				//log.IdTipoDocumentoIdentidad = "";
				//log.IdDocumentoIdentidad = "";
				//log.IdPortafolio = opeFwd.IdPortafolio;
				//log.EstadoPortafolio = "";
				log.IdTipoRegistro = "FORWARD";
				//log.EstadoRegistro = opeFwd.IdEstado;
				//log.IdTipoOperacion = operacion.IdTipoOperacion;
				//log.Folio = operacion.FolioCartera;
				//log.Cantidad = BigDecimal.ZERO;
				//log.PrecioTasa = BigDecimal.ZERO;
				//log.Monto = operacion.MontoOperacion;
				//log.IdMonedaOrigen = operacion.IdMonedaPrincipal;
				//log.IdMonedaLiquidacion = "";
				//log.TipoCambio = operacion.TipoCambioCierre.toString();
				//log.Nemo = operacion.Nemotecnico;
				//log.IdProceso = 0L;
				log.EstadoProceso = "VIG";
				log.NombreProceso = "ValorizacionForward";						
				entradaAgregarLog.p_Log.add(log);
				
				AgregarLog agregarLogService = new AgregarLog();
				agregarLogService.logica(conn, entradaAgregarLog);
				
			}else {
				mapMsg.put("FechaProceso", in.p_FechaProceso);
				out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_INFORMACION,"NO_EXISTEN_OPERACIONES_PARA_PROCESO_VALORIZACION",mapMsg,conn,in.p_IdEmpresa));
				Id++;
				//return out;
			}
			
			/***** LOG DE PROCESOS PARA EL CIERRE DE MODULO *****/
			//valida errores
			if (out.P_OUT_ERRORESCONTADOR.equals(0L)){
				resultadoProcesoTexto = "OK";
			}else{
				resultadoProcesoTexto = "UI_PRESENTA_ERRORES";
			}
			List<ControlProceso> lstControlProceso = new ArrayList<ControlProceso>();			
			ControlProceso controlProceso = new ControlProceso();
			controlProceso.FechaSistema  = fechaSistema;
			controlProceso.IdEmpresa     = in.p_IdEmpresa;
			controlProceso.IdUsuario     = in.p_IdUsuario;
			controlProceso.ResultadoProceso = resultadoProcesoTexto;
			lstControlProceso.add(controlProceso);
			_sql.batch(conn, SQL_INSERT_CONTROL_PROCESO_FORWARD, lstControlProceso, ControlProceso.class);
			
		}catch(Exception e){
			out.P_OUT_ERRORESCONTADOR = 1L;
			logger.error(e);
			e.printStackTrace();
			throw e;
		}
		
		logger.info("Termino del servicio ValorizacionForward, tiempo:" + (System.currentTimeMillis() - init));
		return out;
	}
	
	private OperacionProcesoValorizacionForward copyPojoOperacionProcesoValorizacionForward(OperacionProcesoValorizacionForward opeFwd) {
		OperacionProcesoValorizacionForward operacion = new OperacionProcesoValorizacionForward();
		operacion.IdCartera = opeFwd.IdCartera;
		operacion.FolioCartera = opeFwd.FolioCartera;
		operacion.IdOperacion = opeFwd.IdOperacion;
		operacion.IdTipoOperacion = opeFwd.IdTipoOperacion;
		operacion.PlazoEnDiasOperacion = opeFwd.PlazoEnDiasOperacion;
		operacion.FechaOperacion = opeFwd.FechaOperacion;
		operacion.MontoOperacion = opeFwd.MontoOperacion;
		operacion.TipoCambioCierre = opeFwd.TipoCambioCierre;
		operacion.ParidadCierre = opeFwd.ParidadCierre;
		operacion.TipoCambioMercado = opeFwd.TipoCambioMercado;
		operacion.PrecioSpotCompra = opeFwd.PrecioSpotCompra;
		operacion.PrecioSpotVenta = opeFwd.PrecioSpotVenta;
		operacion.PrecioSpotPromedio = opeFwd.PrecioSpotPromedio;
		operacion.TCDolarObservado = opeFwd.TCDolarObservado;
		operacion.TipoCambioMercadoUF = opeFwd.TipoCambioMercadoUF;
		operacion.MultiplicaODivide = opeFwd.MultiplicaODivide;
		operacion.IdMoneda = opeFwd.IdMoneda;
		operacion.PrecioUtilizarPTC = opeFwd.PrecioUtilizarPTC;
		operacion.FactorMultDiv = opeFwd.FactorMultDiv;
		operacion.PlazoResidual = opeFwd.PlazoResidual;
		operacion.IdRegistro = opeFwd.IdRegistro;
		operacion.IdClasificacionTipoInstrumento = opeFwd.IdClasificacionTipoInstrumento;
		operacion.IdTipoInstrumento = opeFwd.IdTipoInstrumento;
		operacion.IdInstrumento = opeFwd.IdInstrumento;
		operacion.Nemotecnico = opeFwd.Nemotecnico;
		operacion.IdMonedaPrincipal = opeFwd.IdMonedaPrincipal;
		operacion.IdMonedaSecundaria = opeFwd.IdMonedaSecundaria;
		operacion.FechaEfectiva = opeFwd.FechaEfectiva;
		operacion.ValorParametroModalidadLiquidacion = opeFwd.ValorParametroModalidadLiquidacion;
		operacion.FechaVencimientoOperacion = opeFwd.FechaVencimientoOperacion;
		operacion.MensajeError = opeFwd.MensajeError;
		operacion.IdTipoTasa = opeFwd.IdTipoTasa;
		operacion.Tasa = opeFwd.Tasa;
		operacion.MontoValorRazonableActivo = opeFwd.MontoValorRazonableActivo;
		operacion.MontoValorRazonablePasivo = opeFwd.MontoValorRazonablePasivo;
		operacion.MontoValorRazonableNeto = opeFwd.MontoValorRazonableNeto;
		operacion.TasaValorRazonableActivo = opeFwd.TasaValorRazonableActivo;
		operacion.TasaValorRazonablePasivo = opeFwd.TasaValorRazonablePasivo;
		return operacion;
	}
	
	private EntregarPreciosTasas_Entrada_DatosInstrumentos copyPojoEntradaDatosInstrumentos(OperacionProcesoValorizacionForward operacion) {
		EntregarPreciosTasas_Entrada_DatosInstrumentos entradaDatosInstrumentos = new EntregarPreciosTasas_Entrada_DatosInstrumentos();
		entradaDatosInstrumentos.IdRegistro = operacion.IdRegistro;
		//entradaDatosInstrumentos.TipoCartera = operacion.TipoCartera;
		//entradaDatosInstrumentos.IdUso = operacion.IdUso;
		//entradaDatosInstrumentos.IdTipoCriterioTasa = operacion.IdTipoCriterioTasa;
		entradaDatosInstrumentos.IdClasificacionTipoInstrumento = operacion.IdClasificacionTipoInstrumento;
		entradaDatosInstrumentos.IdTipoInstrumento = operacion.IdTipoInstrumento;
		entradaDatosInstrumentos.IdInstrumento = operacion.IdInstrumento;
		entradaDatosInstrumentos.Nemotecnico = operacion.Nemotecnico;
		entradaDatosInstrumentos.IdMoneda = operacion.IdMoneda;
		//entradaDatosInstrumentos.FechaConsulta = operacion.FechaConsulta;
		entradaDatosInstrumentos.PlazoResidual = operacion.PlazoResidual;
		//entradaDatosInstrumentos.NemoSVS = operacion.NemoSVS;
		return entradaDatosInstrumentos;
	}
	
	private ResultadoValorizacion copyPojoResultadoValorizacion(OperacionProcesoValorizacionForward operacion) {
		ResultadoValorizacion resultado = new ResultadoValorizacion();
		resultado.IdCartera = operacion.IdCartera;
		resultado.FolioCartera = operacion.FolioCartera;
		resultado.IdTipoOperacion = operacion.IdTipoOperacion;
		resultado.Nemotecnico = operacion.Nemotecnico;
		resultado.MontoOperacion = operacion.MontoOperacion;
		resultado.IdMonedaPrincipal = operacion.IdMonedaPrincipal;
		resultado.IdMonedaSecundaria = operacion.IdMonedaSecundaria;
		resultado.MensajeError = operacion.MensajeError;
		resultado.MontoValorRazonableActivo = operacion.MontoValorRazonableActivo;
		resultado.TasaValorRazonableActivo = operacion.TasaValorRazonableActivo;
		resultado.MontoValorRazonablePasivo = operacion.MontoValorRazonablePasivo;
		resultado.TasaValorRazonablePasivo = operacion.TasaValorRazonablePasivo;
		return resultado;
	}
	

	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con =  PlataformaUtils.getPropertiesConnection("qa02")){
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdPais\":44,\"p_IdUsuario\":9576,\"p_IdModulo\":\"MFW\",\"p_UsaReglasNegocio\":\"N\",\"p_FechaProceso\":\"2026-05-27\",\"p_IdTipoCanalIngreso\":\"WBS\"}";
			ValorizacionForwardEntrada Objin = gson.fromJson(json, ValorizacionForwardEntrada.class);
			
			ValorizacionForward demo = new ValorizacionForward();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			try {
 				ValorizacionForwardSalida ObjOut = demo.logica(con, Objin);
 				con.rollback();
 				System.out.println("SALIDA");
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				//er.printStackTrace();	
				con.rollback();
			}
			finally {
				con.rollback();
				con.close();
			}
	    }	
		System.out.println("== END ==");
	}
}