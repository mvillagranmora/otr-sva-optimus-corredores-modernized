package cl.bcs.ordenes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.formula.functions.T;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.com.CalcularCobros;
import cl.bcs.plataforma.com.CalcularCobros.CalcularCobrosEntrada;
import cl.bcs.plataforma.com.CalcularCobros.CalcularCobrosSalida;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog.Log;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AjustarTipoCambioAsignacion implements JavaCall {

	private final String SQL_UPDATE_ASIGNACION 			= loadResourceToString("cl/bcs/ordenes/sql/AjustarTipoCambioAsignacion_UpdateAsignacion.sql");
	private final String SQL_SELECT_COBRO_ASIGNACION	= loadResourceToString("cl/bcs/ordenes/sql/AjustarTipoCambioAsignacion_SelectCobroAsignacion.sql");
	private final String SQL_UPDATE_COBRO_ASIGNACION 	= loadResourceToString("cl/bcs/ordenes/sql/AjustarTipoCambioAsignacion_UpdateCobroAsignacion.sql");
	private final String SQL_SELECT_ORDEN				= loadResourceToString("cl/bcs/ordenes/sql/AjustarTipoCambioAsignacion_SelectOrdenes.sql");
	private final String SQL_UPDATE_ORDEN				= loadResourceToString("cl/bcs/ordenes/sql/AjustarTipoCambioAsignacion_UpdateOrden.sql");
	private final String SQL_SELECT_COBRO_ORDEN			= loadResourceToString("cl/bcs/ordenes/sql/AjustarTipoCambioAsignacion_SelectCobroOrden.sql");
	private final String SQL_UPDATE_COBRO_ORDEN			= loadResourceToString("cl/bcs/ordenes/sql/AjustarTipoCambioAsignacion_UpdateCobroOrden.sql");
	private final String SQL_SELECT_DATOS_CLIENTE		= loadResourceToString("cl/bcs/ordenes/sql/AjustarTipoCambioAsignacion_SelectDatosClientes.sql");
	private final String SQL_SELECT_ASIGNACION  		= loadResourceToString("cl/bcs/ordenes/sql/AjustarTipoCambioAsignacion_SelectAsignacion.sql");
	
	
	private final String MONEDA_OPERACION_DOLAR 		= "USD";
	private final String ESTADO_ASIGNACION_NO_FACTURADO = "NEF";
	private final String CLASIFICACION_RENTA_VARIABLE	= "RV";

	private final String TIPO_LOG_MODIFICACION 			= "MODIFICACION";
	
	public static class AjustarTipoCambioAsignacion_Entrada extends BcsPojo{
		
		public Long 							p_IdEmpresa;
		public Long 							p_IdUsuario;
		public String							p_IdModulo;
		public List<Asignacion_Entrada>			p_Asignacion;
		
	}
	
	public static class AjustarTipoCambioAsignacion_Salida extends BcsPojo{
		
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		
		public AjustarTipoCambioAsignacion_Salida() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<>();
		}

	}
	
	public static class Asignacion_Entrada extends BcsPojo{
		
		public Long 		IdAsignacion;
		public Long			IdTransaccion;
		public Long			IdOrden;
		public BigDecimal	MontoAsignado;
		public String		IdEstadoAsignacion;
		public String		IdMonedaOperacion;
		public String		IdMonedaLiquidacionOrden;
		public String		IdClasificacionTipoInstrumento;
		public String		FechaLiquidacion;
		public String		FolioTransaccion;
		public Long 		IdCliente;
		public Long			IdPortafolio;
		public BigDecimal	TipoCambio;
		public BigDecimal	NuevoTipoCambio;
	}
	
	public static class DatosCliente extends BcsPojo {
		
		public Long     IdCliente;
		public String   NombreCliente;
		public String   EstadoCliente;
		public String   TipoDocumento;
		public String   DocumentoIdentidad;
		public Long     IdPortafolio;
		public String   CodigoPortafolio;
		public String   EstadoPortafolio;
		public Long     IdPersona;
				
	}
	
	
	public static class Asignacion extends BcsPojo {
		
		public Long 		IdAsignacion;
		public String		IdEstadoAsignacion;
		public String		IdMonedaOperacion;
		public String		IdMonedaLiquidacionOrden;
		public BigDecimal	MontoAsignado;
		public BigDecimal	TipoCambio;
	}
	
	public static class Orden extends BcsPojo {
		
		public Long			IdOrden;
		public Long			IdAsignacion;
		public String		IdMonedaOrden;
		public String		IdMonedaLiquidacionOrden;
		public BigDecimal	MontoOrden;
		public BigDecimal	MontoLiquidacion;
		public BigDecimal	MontoLocal;
		public BigDecimal	TipoCambio;
	}
	
	public static class CobroAsignacion extends BcsPojo{
		
		public Long			IdCobroAsignacion;
		public Long			IdAsignacion;
		public String		IdCobro;
		public String		IdMoneda;
		public String		FormaCobro;
		public BigDecimal	ValorCobro;
		public BigDecimal	ValorCobroAplicado;
		public BigDecimal	MontoCobroCalculado;
		public BigDecimal	MontoCobroCalculadoLocal;
		public BigDecimal	MontoCobroCalculadoLiquidacion;
		public String		CodigoCobroClasificacion;
		public String		CodigoCobroClasificacionBase;
		public BigDecimal 	MontoMinimo;
		
	}
	
	public static class CobroOrden extends BcsPojo {
		
		public Long			IdInternoCobro;
		public Long			IdCobroOrden;
		public Long			IdOrden;
		public String		IdCobro;
		public String		IdMoneda;
		public String		FormaCobro;
		public BigDecimal	ValorCobroAplicado;
		public BigDecimal	MontoCobroCalculado;
		public BigDecimal	MontoCobroCalculadoLocal;
		public BigDecimal	MontoCobroCalculadoLiquidacion;
		public String		CodigoCobroClasificacion;
		public BigDecimal	ValorCobroPerfil;
		public BigDecimal 	MontoMinimo;
		public String 		PermiteCambiarValor;
		public BigDecimal 	NuevoMontoCobroCalculado;
		public BigDecimal 	NuevoValorCobroFinal;
		
	}
	
	public static class CobroCalculo extends BcsPojo{
	    public Long       IdInternoOrigen;
	    public Long       IdPadre;
	    public String     IdCobro;
	    public BigDecimal ValorCobroPerfil;
	    public BigDecimal MontoMinimo;
	    public String     CodigoCobroClasificacion;
	    public String     PermiteCambiarValor;
	    public BigDecimal ValorCobroAplicado;
	    public String     IdMoneda;
	}

	public static class ResultadoCobroCalculo extends BcsPojo {
	    public Long       IdInternoOrigen;
	    public Long       IdPadre;
	    public BigDecimal MontoCobroCalculado;
	    public BigDecimal MontoCobroLiquidacion;
	    public BigDecimal ValorCobroFinal;
	    public String     IdMoneda;
	}
	
	public static class ClientePortafolio extends BcsPojo{
		
		public Long			IdCliente;
		public Long			IdPortafolio;
		public String		CodigoPortafolio;
		public String		NombreCliente;
	}
	
	public static class ActualizacionTipoCambioAsignacion extends BcsPojo {
		
		public Long 		IdAsignacion;
		public Long			IdOrden;
		public BigDecimal	MontoAsignadoLiquidacion;
		public BigDecimal	MontoAsignadoLocal;
		public BigDecimal	MontoAsignado;
		public BigDecimal	TipoCambio;
		
	}
	
	public static class ActualizacionTipoCambioOrden extends BcsPojo{
		
		public Long			IdOrden;
		public BigDecimal	MontoLiquidacion;
		public BigDecimal	MontoLocal;
		public BigDecimal	MontoOrden;
		public String		FechaModificacion;
	}
	
	public static class ActualizacionCobroAsignacion extends BcsPojo{
		
		public Long			IdCobroAsignacion;
		public Long			IdAsignacion;
		public BigDecimal	MontoCobroCalculado;
		public BigDecimal	MontoCobroCalculadoLocal;
		public BigDecimal	MontoCobroCalculadoLiquidacion;
	}
	
	public static class ActualizacionCobroOrden extends BcsPojo {
		
		public Long			IdCobroOrden;
		public Long			IdOrden;
		public BigDecimal	MontoCobroCalculado;
		public BigDecimal	MontoCobroCalculadoLocal;
		public BigDecimal	MontoCobroCalculadoLiquidacion;
	}
	
	@Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		AjustarTipoCambioAsignacion_Entrada jin=gson.fromJson(in.toString() ,AjustarTipoCambioAsignacion_Entrada.class);
		AjustarTipoCambioAsignacion_Salida out=logica(conn,jin);
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	private <T> boolean ValidarListaConElementos(List<T> lista) {
		return lista != null && !lista.isEmpty();
	}
	
	private boolean ValidarId(Long Id) {
		return Id != null && !Id.equals(0L);
	}
	
	private boolean ValidarNuevoTipoCambio(BigDecimal tipoCambio) {
	    return tipoCambio != null && tipoCambio.compareTo(BigDecimal.ZERO) > 0;
	}
	
	private boolean ValidarMonedas(String IdMoneda, String MonedaComparacion) {
		return IdMoneda.equals(MonedaComparacion);
	}	
	
	private boolean ValidarEstadoAsignacion(String IdEstado) {
		return this.ESTADO_ASIGNACION_NO_FACTURADO.equals(IdEstado);
	}
	
	private boolean ValidarMercadoRentaVariable(String clasificacionTipoInstrumento) {
		return this.CLASIFICACION_RENTA_VARIABLE.equals(clasificacionTipoInstrumento);
	}
	
	private boolean ValidarFacturacion(Long IdAsignacion , Connection conn) throws SQLException {
		SQL sql = new SQL();
			boolean valido= false;
			
			HashMap<String, Object> queryParams = new HashMap<String, Object>(1);
			queryParams.put("IdAsignacion", IdAsignacion);
			
			//aqui va los datos del log  			
			List<Asignacion> datosAsignacion = sql.query(conn, SQL_SELECT_ASIGNACION, queryParams, Asignacion.class);		
			Asignacion estadoAsignacion = datosAsignacion.isEmpty() ? new Asignacion():datosAsignacion.get(0);
			
			if("NEF".equals(estadoAsignacion.IdEstadoAsignacion)) {
				valido= true;
			}			
			
		return valido;
	}

	
	private void ValidarAsignaciones(List<Asignacion_Entrada> listaAsignaciones, AjustarTipoCambioAsignacion_Salida salida, Connection conn,Long idEmpresa) throws Exception {
		
		if(!this.ValidarListaConElementos(listaAsignaciones)) {
			
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_LISTA_ASIGNACIONES_INVALIDA", new HashMap<String,String>(0), conn, idEmpresa));
			salida.P_OUT_ERRORESCONTADOR++;
			return;
		}
		
		for(Asignacion_Entrada asignacion : listaAsignaciones) {
			
			if (!this.ValidarId(asignacion.IdAsignacion)) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_ASIGNACION_SIN_IDENTIFICADOR", new HashMap<String,String>(0), conn, idEmpresa));
				salida.P_OUT_ERRORESCONTADOR++;
			}
			
			HashMap<String,String> paramsMensaje = new HashMap<String,String>();
			paramsMensaje.put("SecuenciaAsignacion", asignacion.IdAsignacion.toString());
			
			if (!this.ValidarId(asignacion.IdOrden)) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_ASIGNACION_SIN_IDENTIFICAR_ORDEN", paramsMensaje, conn, idEmpresa));
				salida.P_OUT_ERRORESCONTADOR++;
			}
			
			if (!this.ValidarMercadoRentaVariable(asignacion.IdClasificacionTipoInstrumento)) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_ASIGNACION_MERCADO_INVALIDO", paramsMensaje, conn, idEmpresa));
				salida.P_OUT_ERRORESCONTADOR++;
			}
			
			if (!this.ValidarNuevoTipoCambio(asignacion.NuevoTipoCambio)) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_TIPO_CAMBIO_ASIGNACION_MAYOR_A_CERO", paramsMensaje, conn, idEmpresa));
				salida.P_OUT_ERRORESCONTADOR++;
			}
			
			if(asignacion.TipoCambio.equals(asignacion.NuevoTipoCambio)) {
				paramsMensaje.clear();
				paramsMensaje.put("SecuenciaAsignacion", asignacion.IdAsignacion.toString());
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_IGUALDAD_TIPO_CAMBIO", paramsMensaje, conn, idEmpresa));
				salida.P_OUT_ERRORESCONTADOR++;
			}
			
			if (!this.ValidarEstadoAsignacion(asignacion.IdEstadoAsignacion)) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_ESTADO_ASIGNACION", paramsMensaje, conn, idEmpresa));
				salida.P_OUT_ERRORESCONTADOR++;
			}
			
			if(!this.ValidarMonedas(asignacion.IdMonedaOperacion, this.MONEDA_OPERACION_DOLAR)) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_MONEDA_OPERACION", paramsMensaje, conn, idEmpresa));
				salida.P_OUT_ERRORESCONTADOR++;
			}
			
		   if(!this.ValidarFacturacion(asignacion.IdAsignacion, conn)) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_ESTADO_ASIGNACION", paramsMensaje, conn, idEmpresa));
				salida.P_OUT_ERRORESCONTADOR++;
			}
		}
		
		
	}	
	
	

	

	private BigDecimal CalcularNuevoMontoTipoCambio (BigDecimal tipoCambioNuevo, BigDecimal montoAsignado) {
		return tipoCambioNuevo.multiply(montoAsignado).setScale(0, RoundingMode.HALF_UP);
	}
	
	private ActualizacionTipoCambioAsignacion ConstruirActualizacionAsignacion(Asignacion_Entrada asignacion) {
		
		BigDecimal montoFinalActualizacion = this.CalcularNuevoMontoTipoCambio(asignacion.NuevoTipoCambio, asignacion.MontoAsignado);
		
		ActualizacionTipoCambioAsignacion actualizacionAsignacion = new ActualizacionTipoCambioAsignacion();
		
		actualizacionAsignacion.IdAsignacion 				= asignacion.IdAsignacion;
		actualizacionAsignacion.IdOrden						= asignacion.IdOrden;
		actualizacionAsignacion.MontoAsignadoLiquidacion 	= asignacion.MontoAsignado;
		actualizacionAsignacion.MontoAsignadoLocal			= montoFinalActualizacion;
		actualizacionAsignacion.MontoAsignado				= asignacion.MontoAsignado;
		actualizacionAsignacion.TipoCambio					= asignacion.NuevoTipoCambio;
		
		return actualizacionAsignacion;
		
	}
	
	private ActualizacionTipoCambioOrden ConstruirActualizacionOrden(Orden orden, String fechaModificacion) {		

		BigDecimal montoFinalActualizacion = this.CalcularNuevoMontoTipoCambio(orden.TipoCambio, orden.MontoOrden);
		
		ActualizacionTipoCambioOrden actualizacionOrden = new ActualizacionTipoCambioOrden();
		
		actualizacionOrden.IdOrden						= orden.IdOrden;
		actualizacionOrden.MontoLiquidacion 			= montoFinalActualizacion;
		actualizacionOrden.MontoLocal					= montoFinalActualizacion;
		actualizacionOrden.MontoOrden					= orden.MontoOrden;
		actualizacionOrden.FechaModificacion			= fechaModificacion;
		
		return actualizacionOrden;
	}
	
 	private void AgregarLogAjusteTipoCambio(Connection conn, AjustarTipoCambioAsignacion_Entrada entrada,String tipoLog, String fechaSistema) {
 		try {
 			InAgregarLog agregarLogEntrada = new InAgregarLog();
 			SQL sql = new SQL();
 			
 			HashMap<String, Object> queryParams = new HashMap<String, Object>(1);
 			queryParams.put("IdCliente", entrada.p_Asignacion.stream().map(x -> x.IdCliente).collect(Collectors.toList()));
 			queryParams.put("IdPortafolio", entrada.p_Asignacion.stream().map(x -> x.IdPortafolio).collect(Collectors.toList()));
 			
 			//aqui va los datos del log  			
 			List<DatosCliente> datosCliente = sql.query(conn, SQL_SELECT_DATOS_CLIENTE, queryParams, DatosCliente.class);		
 			DatosCliente DatosClientePortafolio = datosCliente.isEmpty()? new DatosCliente ():datosCliente.get(0);
 			
 			
	 		
 	 		for (Asignacion_Entrada asignacion: entrada.p_Asignacion) {
 		 		
 				agregarLogEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
 				agregarLogEntrada.p_IdUsuario = entrada.p_IdUsuario;
 				agregarLogEntrada.p_IdModulo = entrada.p_IdModulo;
 				agregarLogEntrada.p_UsaReglasNegocio = "Si";
 				agregarLogEntrada.p_IdEstadoAutorizacion = "S";
 				agregarLogEntrada.p_IdTrabajo = 0L;
 			    Log logAjusteTipoCambio = new Log();
 			    logAjusteTipoCambio.IdCabeceraLog = asignacion.IdAsignacion;
 			    logAjusteTipoCambio.IdTipoLog = tipoLog;
 			    logAjusteTipoCambio.IdModulo = entrada.p_IdModulo;
 			    logAjusteTipoCambio.IdUsuario = entrada.p_IdUsuario;
 			    logAjusteTipoCambio.IdEmpresa = entrada.p_IdEmpresa;
 			    logAjusteTipoCambio.FechaSistemaLog = fechaSistema;
 			    logAjusteTipoCambio.IdCliente = DatosClientePortafolio.IdCliente;
 			    logAjusteTipoCambio.IdPortafolio = DatosClientePortafolio.IdPortafolio;
 			    logAjusteTipoCambio.EstadoCliente = DatosClientePortafolio.EstadoCliente;
 			    logAjusteTipoCambio.EstadoPortafolio = DatosClientePortafolio.EstadoPortafolio;
 			    logAjusteTipoCambio.IdTipoDocumentoIdentidad = DatosClientePortafolio.TipoDocumento;
 			    logAjusteTipoCambio.IdDocumentoIdentidad = DatosClientePortafolio.DocumentoIdentidad;
 			    logAjusteTipoCambio.NombreCliente = DatosClientePortafolio.NombreCliente;
 			    logAjusteTipoCambio.IdRegistro    = asignacion.IdAsignacion.toString();
 			    logAjusteTipoCambio.IdPersona     = DatosClientePortafolio.IdPersona;
 			    
 			    logAjusteTipoCambio.IdTipoRegistro = "CAMBIO_USD_ASIGNACION";
 			    logAjusteTipoCambio.NombreProceso = "Cambio USD en Asignación";
 			    
 			    agregarLogEntrada.p_Log.add(logAjusteTipoCambio);

 				String montoCLPAntiguoStr = asignacion.MontoAsignado.multiply(asignacion.TipoCambio).setScale(0, RoundingMode.HALF_UP).toPlainString();
 				String montoCLPNuevoStr = asignacion.MontoAsignado.multiply(asignacion.NuevoTipoCambio).setScale(0, RoundingMode.HALF_UP).toPlainString();
 				if (asignacion.TipoCambio.compareTo(asignacion.NuevoTipoCambio) != 0) {
 					agregarLogEntrada.p_LogDetalle.add(agregarDetalleLog(asignacion.IdAsignacion, "MONTO_CLP", montoCLPAntiguoStr, montoCLPNuevoStr));
 	 				agregarLogEntrada.p_LogDetalle.add(agregarDetalleLog(asignacion.IdAsignacion, "TIPO_CAMBIO", asignacion.TipoCambio.toPlainString(), asignacion.NuevoTipoCambio.toPlainString()));	
 				}
 			}
 	 		AgregarLog agregarLogService = new AgregarLog();
			agregarLogService.logica(conn, agregarLogEntrada);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
 	
	public  InAgregarLog.DetalleLog agregarDetalleLog(Long idCabeceraLog, String campoRegistro, String valorAntiguo, String valorNuevo) {
		InAgregarLog.DetalleLog detLog = new InAgregarLog.DetalleLog();
		detLog.IdCabeceraLog = idCabeceraLog; 
		detLog.IdTrabajo = 0L;
		detLog.SubRegistro = "CAMBIO_USD_ASIGNACION"; 
		detLog.CampoRegistro = campoRegistro; 
		detLog.ValorAntiguo = valorAntiguo; 
		detLog.ValorNuevo = valorNuevo;
		return detLog;
	}
	
	private List<ResultadoCobroCalculo> ejecutarCalculoCobros(Connection conn, List<CobroCalculo> cobros, BigDecimal montoBase) throws Exception {

	    CalcularCobrosEntrada entrada = new CalcularCobrosEntrada();
	    Map<Long, CobroCalculo> porIdInterno = new HashMap<>();
	    Long idInterno = 0L;

	    for (CobroCalculo cob : cobros) {
	        CalcularCobros.CobroEntrada c = new CalcularCobros.CobroEntrada();
	        c.IdInternoCobro            = idInterno;
	        c.MontoOperacionMonedaCobro = montoBase;
	        c.IdCobro                   = cob.IdCobro;
	        c.ValorCobroPerfil          = cob.ValorCobroPerfil;
	        c.MontoMinimo               = cob.MontoMinimo;
	        c.CodigoCobroClasificacion  = cob.CodigoCobroClasificacion;
	        c.PermiteCambiarValor       = cob.PermiteCambiarValor;
	        c.ValorCobroAplicado        = cob.ValorCobroAplicado;
	        c.IdMonedaCobro             = cob.IdMoneda;
	        entrada.cobros.add(c);
	        porIdInterno.put(idInterno, cob);
	        idInterno++;
	    }

	    CalcularCobrosSalida salida = new CalcularCobros().logica(conn, entrada);

	    List<ResultadoCobroCalculo> resultados = new ArrayList<>();
	    for (CalcularCobros.CobroSalida cs : salida.p_out_Cobros) {
	        CobroCalculo original        = porIdInterno.get(cs.IdInternoCobro);
	        ResultadoCobroCalculo r      = new ResultadoCobroCalculo();
	        r.IdInternoOrigen            = original.IdInternoOrigen;
	        r.IdPadre                    = original.IdPadre;
	        r.MontoCobroCalculado        = cs.MontoCobroCalculado;
	        r.ValorCobroFinal            = cs.ValorCobroFinal;
	        r.IdMoneda                   = original.IdMoneda;
	        resultados.add(r);
	    }
	    return resultados;
	}

	private List<CobroCalculo> prepararEntradaCobrosOrdenCalculo(List<CobroOrden> cobros) {
	    List<CobroCalculo> resultado = new ArrayList<>();
	    for (CobroOrden cob : cobros) {
	        CobroCalculo c              = new CobroCalculo();
	        c.IdInternoOrigen           = cob.IdCobroOrden;
	        c.IdPadre                   = cob.IdOrden;
	        c.IdCobro                   = cob.IdCobro;
	        c.ValorCobroPerfil          = cob.ValorCobroPerfil;
	        c.MontoMinimo               = cob.MontoMinimo;
	        c.CodigoCobroClasificacion  = cob.CodigoCobroClasificacion;
	        c.PermiteCambiarValor       = cob.PermiteCambiarValor;
	        c.ValorCobroAplicado        = cob.ValorCobroAplicado;
	        c.IdMoneda                  = cob.IdMoneda;
	        resultado.add(c);
	    }
	    return resultado;
	}

	private List<CobroCalculo> prepararEntradaCobrosAsignacionCalculo(List<CobroAsignacion> cobros) {
	    List<CobroCalculo> resultado = new ArrayList<>();
	    for (CobroAsignacion cob : cobros) {
	        CobroCalculo c              = new CobroCalculo();
	        c.IdInternoOrigen           = cob.IdCobroAsignacion;
	        c.IdPadre                   = cob.IdAsignacion;
	        c.IdCobro                   = cob.IdCobro;
	        c.ValorCobroPerfil          = cob.ValorCobro;
	        c.MontoMinimo               = cob.MontoMinimo;
	        c.CodigoCobroClasificacion  = cob.CodigoCobroClasificacion;
	        c.PermiteCambiarValor       = "";
	        c.ValorCobroAplicado        = cob.ValorCobroAplicado;
	        c.IdMoneda                  = cob.IdMoneda;
	        resultado.add(c);
	    }
	    return resultado;
	}

	private List<ActualizacionCobroOrden> mapearResultadoActualizacionOrden(
	        List<ResultadoCobroCalculo> resultados) {
	    List<ActualizacionCobroOrden> actualizaciones = new ArrayList<>();
	    for (ResultadoCobroCalculo r : resultados) {
	        ActualizacionCobroOrden upd             = new ActualizacionCobroOrden();
	        upd.IdCobroOrden                        = r.IdInternoOrigen;
	        upd.IdOrden                             = r.IdPadre;
	        upd.MontoCobroCalculado                 = r.MontoCobroCalculado;
	        upd.MontoCobroCalculadoLocal            = r.MontoCobroCalculado;
	        upd.MontoCobroCalculadoLiquidacion      = r.MontoCobroLiquidacion;
	        actualizaciones.add(upd);
	    }
	    return actualizaciones;
	}

	private List<ActualizacionCobroAsignacion> mapearResultadoActualizacionAsignacion(
	        List<ResultadoCobroCalculo> resultados) {
	    List<ActualizacionCobroAsignacion> actualizaciones = new ArrayList<>();
	    for (ResultadoCobroCalculo r : resultados) {
	        ActualizacionCobroAsignacion upd        = new ActualizacionCobroAsignacion();
	        upd.IdCobroAsignacion                   = r.IdInternoOrigen;
	        upd.IdAsignacion                        = r.IdPadre;
	        upd.MontoCobroCalculado                 = r.MontoCobroCalculado;
	        upd.MontoCobroCalculadoLocal            = r.MontoCobroCalculado;
	        upd.MontoCobroCalculadoLiquidacion      = r.MontoCobroLiquidacion;
	        actualizaciones.add(upd);
	    }
	    return actualizaciones;
	}
	
	public AjustarTipoCambioAsignacion_Salida logica(Connection conn, AjustarTipoCambioAsignacion_Entrada entrada) throws Exception {
		
		AjustarTipoCambioAsignacion_Salida salida = new AjustarTipoCambioAsignacion_Salida();
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		String monedaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		
		this.ValidarAsignaciones(entrada.p_Asignacion, salida, conn,entrada.p_IdEmpresa);
		
		if(salida.P_OUT_ERRORESCONTADOR.compareTo(0L) > 0) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_AJUSTE_TIPO_CAMBIO_FALLIDO", new HashMap<String,String>(0), conn, entrada.p_IdEmpresa));
			return salida;
		}
		
		// ACTUALIZACION TIPO CAMBIO EN ASIGNACIONES
				
		List<ActualizacionTipoCambioAsignacion> actualizacionesTipoCambioAsignacion = new ArrayList<>();
		
		for (Asignacion_Entrada asignacion: entrada.p_Asignacion) {
			
			ActualizacionTipoCambioAsignacion actualizacionTipoCambio = this.ConstruirActualizacionAsignacion(asignacion);
			actualizacionesTipoCambioAsignacion.add(actualizacionTipoCambio);
		}
		
		SQL sql = new SQL();
		sql.batch(conn, SQL_UPDATE_ASIGNACION, actualizacionesTipoCambioAsignacion, ActualizacionTipoCambioAsignacion.class);		
		
		// ACTUALIZACION TIPO CAMBIO EN ORDENES

		HashMap<String, Object> queryParams = new HashMap<String, Object>(1);
		queryParams.put("IdsOrden", actualizacionesTipoCambioAsignacion.stream().map(x -> x.IdOrden).collect(Collectors.toList()));
		queryParams.put("IdsAsignacion", actualizacionesTipoCambioAsignacion.stream().map(x -> x.IdAsignacion).collect(Collectors.toList()));
		
		List<Orden> ordenesAsignacion = new ArrayList<>();		
		ordenesAsignacion.addAll(sql.query(conn, SQL_SELECT_ORDEN, queryParams, Orden.class));
		
		List<ActualizacionTipoCambioOrden> actualizacionesTipoCambioOrden = new ArrayList<>();
		
		for (Orden orden: ordenesAsignacion) {
			
			ActualizacionTipoCambioOrden actualizacionTipoCambioOrden = this.ConstruirActualizacionOrden(orden, fechaSistema);
			actualizacionesTipoCambioOrden.add(actualizacionTipoCambioOrden);		
		}
		
		queryParams.clear();
		queryParams.put("IdsOrden", actualizacionesTipoCambioAsignacion.stream().map(x -> x.IdOrden).collect(Collectors.toList()));
		sql.batch(conn, SQL_UPDATE_ORDEN, actualizacionesTipoCambioOrden, ActualizacionTipoCambioOrden.class);
		
		// ACTUALIZACION DE COBROS EN ASIGNACIONES Y ORDENES
		
		queryParams.clear();
		queryParams.put("IdsAsignacion", actualizacionesTipoCambioAsignacion.stream().map(x -> x.IdAsignacion).collect(Collectors.toList()));
		List<CobroAsignacion> cobrosAsignaciones = new ArrayList<>();		
		cobrosAsignaciones.addAll(sql.query(conn, SQL_SELECT_COBRO_ASIGNACION, queryParams, CobroAsignacion.class));
		
		queryParams.clear();
		List<CobroOrden> cobrosOrden = new ArrayList<>();		
		queryParams.put("IdsOrden", actualizacionesTipoCambioAsignacion.stream().map(x -> x.IdOrden).collect(Collectors.toList()));
		cobrosOrden.addAll(sql.query(conn, SQL_SELECT_COBRO_ORDEN, queryParams, CobroOrden.class));
		
		HashMap<String, CobroOrden> cobrosOrdenHash = new HashMap<>();
		
		for (CobroOrden cobro: cobrosOrden) {
			String key = cobro.IdCobro + cobro.IdMoneda + cobro.CodigoCobroClasificacion;
			
			cobrosOrdenHash.put(key, cobro);
		}
		
		for (CobroAsignacion cobro: cobrosAsignaciones) {
			String key = cobro.IdCobro + cobro.IdMoneda + cobro.CodigoCobroClasificacion;
			
			CobroOrden cobroEquivalente = cobrosOrdenHash.get(key);
			
			cobro.MontoMinimo = cobroEquivalente != null ? cobroEquivalente.MontoMinimo : BigDecimal.ZERO;
		}
		
		List<ActualizacionCobroOrden> actualizacionesCobroOrden = new ArrayList<>();
		List<ActualizacionCobroAsignacion> actualizacionesCobroAsignacion = new ArrayList<>();

		for (Orden ord : ordenesAsignacion) {

		    ActualizacionTipoCambioAsignacion asigOrden = actualizacionesTipoCambioAsignacion.stream()
		        .filter(a -> a.IdOrden.equals(ord.IdOrden))
		        .findFirst()
		        .orElseThrow(() -> new Exception("Asignación no encontrada para orden: " + ord.IdOrden));

		    BigDecimal montoBase = asigOrden.MontoAsignadoLocal;
		    boolean necesitaConversion = MONEDA_OPERACION_DOLAR.equals(ord.IdMonedaLiquidacionOrden);

		    List<CobroOrden> cobrosOrdenActual = cobrosOrden.stream()
		        .filter(c -> c.IdOrden.equals(ord.IdOrden))
		        .collect(Collectors.toList());

		    List<CobroAsignacion> cobrosAsignacionActual = cobrosAsignaciones.stream()
		        .filter(c -> c.IdAsignacion.equals(asigOrden.IdAsignacion))
		        .collect(Collectors.toList());

		    if (ValidarListaConElementos(cobrosOrdenActual)) {
		        List<ResultadoCobroCalculo> resultadoCobroCalculoOrden = this.ejecutarCalculoCobros(conn, this.prepararEntradaCobrosOrdenCalculo(cobrosOrdenActual), montoBase);
		        
		        if (necesitaConversion) {
		        	resultadoCobroCalculoOrden.forEach(x ->
				        x.MontoCobroLiquidacion = x.MontoCobroCalculado.divide(
				            ord.TipoCambio,
				            4,
				            RoundingMode.HALF_UP
				        )
				    );
		        } else {
					resultadoCobroCalculoOrden.forEach(x ->
			        	x.MontoCobroLiquidacion = x.MontoCobroCalculado
		        	);
				}		        
		        
		        actualizacionesCobroOrden.addAll(this.mapearResultadoActualizacionOrden(resultadoCobroCalculoOrden));
		    }

		    if (ValidarListaConElementos(cobrosAsignacionActual)) {
		        List<ResultadoCobroCalculo> resultadoCobroCalculoAsig = this.ejecutarCalculoCobros(conn, this.prepararEntradaCobrosAsignacionCalculo(cobrosAsignacionActual), montoBase);
		        
		        if (necesitaConversion) {
		        	resultadoCobroCalculoAsig.forEach(x ->
				        x.MontoCobroLiquidacion = x.MontoCobroCalculado.divide(
				            ord.TipoCambio,
				            4,
				            RoundingMode.HALF_UP
				        )
				    );
		        } else {
					resultadoCobroCalculoAsig.forEach(x ->
		        		x.MontoCobroLiquidacion = x.MontoCobroCalculado
		        	);
				}
		        
		        actualizacionesCobroAsignacion.addAll(this.mapearResultadoActualizacionAsignacion(resultadoCobroCalculoAsig));
		    }
		}
		
		if (ValidarListaConElementos(actualizacionesCobroOrden)) {
		    sql.batch(conn, SQL_UPDATE_COBRO_ORDEN, actualizacionesCobroOrden, ActualizacionCobroOrden.class);
		}
		if (ValidarListaConElementos(actualizacionesCobroAsignacion)) {
		    sql.batch(conn, SQL_UPDATE_COBRO_ASIGNACION, actualizacionesCobroAsignacion, ActualizacionCobroAsignacion.class);
		}		
		
		if (salida.P_OUT_ERRORESCONTADOR.equals(0L)) {
			AgregarLogAjusteTipoCambio(conn, entrada, TIPO_LOG_MODIFICACION, fechaSistema);
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "UI_AJUSTE_TIPO_CAMBIO_EXITOSO", new HashMap<String,String>(0), conn, entrada.p_IdEmpresa));
		}
		
		return salida;
	}
	
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getPropertiesConnection("qa02","connections.properties")) {
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9595,\"p_IdModulo\":\"ORD\",\"p_Asignacion\":[{\"IdAsignacion\":1113064,\"IdTransaccion\":3746460,\"IdOrden\":1608496,\"MontoAsignado\":168000,\"IdEstadoAsignacion\":\"NEF\",\"IdMonedaOperacion\":\"USD\",\"IdMonedaLiquidacionOrden\":\"USD\",\"IdClasificacionTipoInstrumento\":\"RV\",\"FechaLiquidacion\":\"2026-07-14T04:00:00.000Z\",\"FolioTransaccion\":\"691002\",\"IdCliente\":4030,\"IdPortafolio\":5660,\"TipoCambio\":900,\"NuevoTipoCambio\":950}]}";

			AjustarTipoCambioAsignacion_Entrada Objin = gson.fromJson(json, AjustarTipoCambioAsignacion_Entrada.class);
			
			AjustarTipoCambioAsignacion demo = new AjustarTipoCambioAsignacion();
			try {
				AjustarTipoCambioAsignacion_Salida ObjOut = demo.logica(con, Objin);
				//con.rollback();
				con.commit();
				System.out.println(ObjOut.toString());
			} catch(Exception er) {
				er.printStackTrace();
				con.rollback();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
	
}
