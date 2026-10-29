package cl.bcs.facturacion;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.cuentaInversion.CrearCargoAbono;
import cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoEntrada;
import cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoSalida;
import cl.bcs.cuentaInversion.CrearCargoAbono.ObjCargoAbonoAGenerar;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;
import cl.bcs.utils.BcsPojo;

public class ModificarRetencionIgk implements JavaCall {

    private static final Logger logger = LogManager.getLogger(ModificarRetencionIgk.class);
    
    private final String SQL_BLOQUEA_MOVIMIENTOS 						= loadResourceToString("cl/bcs/facturacion/sql/ModificarRetencionIgk_BloqueaMovimiento.sql");
    private final String SQL_UPDATE_RETENCION 							= loadResourceToString("cl/bcs/facturacion/sql/ModificarRetencionIgk_UpdateRetencion.sql");
    private final String SQL_UPDATE_RETENCION_DETALLE 					= loadResourceToString("cl/bcs/facturacion/sql/ModificarRetencionIgk_UpdateRetencionDetalle.sql");
    private final String SQL_SELECT_COMPROBANTE 						= loadResourceToString("cl/bcs/facturacion/sql/ModificarRetencionIgk_SelectComprobante.sql");
    private final String SQL_SELECT_COMPROBANTE_DETALLE 				= loadResourceToString("cl/bcs/facturacion/sql/ModificarRetencionIgk_SelectComprobanteDetalle.sql");
    //cuenta inversion
  	private final String SQL_SELECT_COMPROBANTE_CARGO_ABONO 			= loadResourceToString("cl/bcs/facturacion/sql/ModificarRetencionIgk_SelectComprobanteCargoAbono.sql");    
  	
    //ENTRADAS
    public static class ModificarRetencionIgk_Entrada extends BcsPojo {
        public Long								p_IdEmpresa;		
        public Long								p_IdUsuario;   
        public String							p_IdModulo;
        public String							p_UsaReglasNegocio;
        public Long								p_IdComprobante;
        public List<Comprobante_Entrada>		P_COMPROBANTERECALCULAR;
        
        public ModificarRetencionIgk_Entrada (){
        	P_COMPROBANTERECALCULAR	= new ArrayList<Comprobante_Entrada>();
        }        
    };
    
    public static class Comprobante_Entrada extends BcsPojo{
    	
		public Long					IdComprobante;
		public Long					IdComprobanteDetalle;
		public BigDecimal			MayorMenorValor;
		public BigDecimal			MayorMenorValorActual;//trae el valor actual sin modificar para agregarlo al log.
		public BigDecimal 			MontoRetencionGananciaCap;
	};
	
	public static class ComprobanteDetalleUpdate extends BcsPojo
	{
		public Long IdComprobante;
		public Long IdComprobanteDetalle;
		public BigDecimal MayorMenorValor;
		public BigDecimal MayorMenorValorActual;
		public BigDecimal MontoRetencionGananciaCap;
		public BigDecimal MontoRetencionGananciaCapActual;
		public String AfectoArt107;
		public BigDecimal Monto;		
	}
	
	public static class InfoComprobante extends BcsPojo
	{
		public Long IdComprobante;
		public BigDecimal MayorMenorValor;
		public BigDecimal MontoRetencionGananciaCap;
		public String PorcentajeRetencionIgk;
		public String TipoCliente;
		public String Residente;
		public Long IdCliente;
		public String NombreLargoCliente;
		public String TipoDocumentoIdentidad;
		public String DocumentoIdentidad;
		public Long IdPortafolio;
		public String IdTipoOperacion;
		public BigDecimal CantidadTotal;
		public BigDecimal Precio;
		public BigDecimal MontoLiquidacionFinal;
		public String IdMoneda;
		public String IdMonedaLiquidacion;
		public BigDecimal ValorTipoCambio;
		public String NemotecnicoInstrumento;
		public String IdEstadoComprobante;
	}
	
	public static class ComprobanteUpdate extends BcsPojo
	{
		public Long IdComprobante;
		public BigDecimal MayorMenorValor;
		public BigDecimal MontoRetencionGananciaCap;		
	}
	
	public static class InfoComprobanteCargo_Proceso extends BcsPojo
	{		
		public BigDecimal			MontoRetencionGananciaCap;
		public Long					IdPortafolio;
		public String				IdMoneda;
		public String				IdMonedaComprobante;
		public BigDecimal			MontoComprobanteFinal; 
		public java.sql.Date		FechaLiquidacion;
		public Long					IdEjecutivo; 
		public Long					IdComprobante; 
		public String				IdTipoComprobante; 
		public Long				    FolioComprobante; 
		public java.sql.Timestamp	FechaComprobante; 
		public Long					IdInstrumento; 
		public String				IdTipoInstrumento;
		public String				Nemo; 
		public String				IdTipoProducto; 
		public String				IdTipoOperacion; 
		public Long					IdSucursal; 	
	}
    
  //SALIDAS
  	public static class ResultadoProceso_Salida {
  		public String Campo;
  		public Long Id;
  		public String Servicio;
  		public String Resultado;
  		
  		public String toString() {
  			return Utils.printJson(this, false);
  		}
  	}
  	
    public static class ModificarRetencionIgk_Salida extends BcsPojo{
		public List<MensajeSalida>					P_OUT_MENSAJES;
		public Long									P_OUT_ERRORESCONTADOR;
		public List<ResultadoProceso_Salida>		P_OUT_RESULTADOPROCESO;		
		
		public ModificarRetencionIgk_Salida (){
			P_OUT_MENSAJES	= new ArrayList<>();
			P_OUT_RESULTADOPROCESO = new ArrayList<>();
			P_OUT_ERRORESCONTADOR = 0L;
		}
	};
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		ModificarRetencionIgk_Entrada in = gson.fromJson(jin.toString(), ModificarRetencionIgk_Entrada.class);
		ModificarRetencionIgk_Salida out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}	

    @Trace
	public ModificarRetencionIgk_Salida logica(Connection conn, ModificarRetencionIgk_Entrada in) throws Exception {
    	logger.info("Inicio Modificar Retencion Igk: " + in.toString());
		ModificarRetencionIgk_Salida out = new ModificarRetencionIgk_Salida();
		SQL _sql = new SQL();
		
		//String localizacionSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.LocalizacionSistema");
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
				
		if (in.P_COMPROBANTERECALCULAR.size() == 0) {
			String codigo = "NO_VIENEN_MOVIMIENTOS";
			String desc = Diccionario.getMensaje(conn, in.p_IdEmpresa, codigo+" -Recaulculo IGK").msg();
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,codigo,desc));
			return out;
		}
		
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(conn, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			return out;
		}

		ArrayList<Long> idsComprobante = new ArrayList<Long>();
		for (Comprobante_Entrada mov : in.P_COMPROBANTERECALCULAR) {
			if (!idsComprobante.contains(mov.IdComprobante)) {
				idsComprobante.add(mov.IdComprobante);
			}
		}		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("idsComprobante", idsComprobante);
		
		try {
			_sql.query(conn, SQL_BLOQUEA_MOVIMIENTOS, params, ModificarRetencionIgk.class);
		}catch(SQLException sqle) {
			if(sqle.getErrorCode()== 131) {
				//error especifico porque el movimiento que se esta tratando de obtener ya esta siendo procesado.
				out.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR, "UI_TRANSACCION_BLOQUEADA_EN_PROCESO", "UI_TRANSACCION_BLOQUEADA_EN_PROCESO"));
				return out;
			}else {
				throw sqle;
			}
		}
		List<ComprobanteUpdate> compUpdate = new ArrayList<ComprobanteUpdate>();//Cabecera
		List<ComprobanteDetalleUpdate> compDetUpdate = new ArrayList<ComprobanteDetalleUpdate>();//Detalle
		
		//Obtener datos comprobante
		HashMap<String,Object> filtroComprobante = new HashMap<>(1);
		filtroComprobante.put("IdComprobante",in.p_IdComprobante);
		
		InfoComprobante comprobante = _sql.query(conn, SQL_SELECT_COMPROBANTE, filtroComprobante,InfoComprobante.class).get(0);
		BigDecimal montoRetencionAcumulable = new BigDecimal(0);
		BigDecimal mayorMenorValorAcumulable = new BigDecimal(0);
		for(Comprobante_Entrada mov : in.P_COMPROBANTERECALCULAR) {
			//Obtener datos comprobante detalle
			HashMap<String,Object> filtro = new HashMap<>();
			filtro.put("IdComprobanteDetalle",mov.IdComprobanteDetalle);
			ComprobanteDetalleUpdate comprobanteDetalle = _sql.query(conn, SQL_SELECT_COMPROBANTE_DETALLE, filtro,ComprobanteDetalleUpdate.class).get(0);	
			
			//Calcular monto de retencion por detalle
			BigDecimal montoRetencion = new BigDecimal(0);
			if("NO RESIDENTE".equals(comprobante.Residente.trim().toUpperCase())) {
				montoRetencion = mov.MontoRetencionGananciaCap;
			}else {
				montoRetencion = (mov.MayorMenorValor.multiply(new BigDecimal(comprobante.PorcentajeRetencionIgk), MathContext.DECIMAL128)).divide(new BigDecimal(100), MathContext.DECIMAL128);
			}
			
			ComprobanteDetalleUpdate mvto = new ComprobanteDetalleUpdate();
			mvto.IdComprobante = mov.IdComprobante;
			mvto.IdComprobanteDetalle = mov.IdComprobanteDetalle;
			mvto.MayorMenorValorActual = mov.MayorMenorValorActual;
			mvto.MayorMenorValor = mov.MayorMenorValor;
			mvto.MontoRetencionGananciaCapActual = comprobanteDetalle.MontoRetencionGananciaCap;
			mvto.MontoRetencionGananciaCap = montoRetencion;			
			compDetUpdate.add(mvto);
			montoRetencionAcumulable = montoRetencionAcumulable.add(montoRetencion, MathContext.DECIMAL128);
			mayorMenorValorAcumulable = mayorMenorValorAcumulable.add(mov.MayorMenorValor, MathContext.DECIMAL128);
		}
		//Llenar objeto para actualizar mayor menor valor del comprobante (se suman los detalles)
		ComprobanteUpdate cmbtUpdt = new ComprobanteUpdate();
		cmbtUpdt.IdComprobante = in.p_IdComprobante;
		cmbtUpdt.MayorMenorValor = mayorMenorValorAcumulable;
		cmbtUpdt.MontoRetencionGananciaCap = montoRetencionAcumulable;
		compUpdate.add(cmbtUpdt);
		BigDecimal montoDiferencia = comprobante.MontoRetencionGananciaCap.subtract(montoRetencionAcumulable,MathContext.DECIMAL128);
		//seleccionar comprobantes idonios para un cargo
		List<InfoComprobanteCargo_Proceso> selComprobantecargoAbono = _sql.query(conn, SQL_SELECT_COMPROBANTE_CARGO_ABONO, filtroComprobante, InfoComprobanteCargo_Proceso.class);
			
		CrearCargoAbonoSalida salidaCrearCargoAbono = new CrearCargoAbonoSalida();
		
		//MOVIMIENTOS DE CARGO O ABONO
		//cliente no residente si genera cargo/abono (Residente solo modifica MM valor y monto retencion)
			if((montoDiferencia.compareTo(new BigDecimal(0)) == 1) && "NO RESIDENTE".equals(comprobante.Residente.trim().toUpperCase())){
				salidaCrearCargoAbono = mueveCuentaInversion(conn, selComprobantecargoAbono,  in, fechaSistema,"MOVABN", montoDiferencia);
			};
			
			if((montoDiferencia.compareTo(new BigDecimal(0)) == -1) && "NO RESIDENTE".equals(comprobante.Residente.trim().toUpperCase())) {
				montoDiferencia = montoDiferencia.multiply(new BigDecimal(-1), MathContext.DECIMAL128);
				salidaCrearCargoAbono = mueveCuentaInversion(conn, selComprobantecargoAbono,  in, fechaSistema,"MOVCAR", montoDiferencia);
			};
			
			for(MensajeSalida info : salidaCrearCargoAbono.P_OUT_MENSAJES) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(info.id ,info.tipo,info.codigo,info.descripcion));				
			}
			//FIN MOVIMIENTO CARGO O ABONO	
			try {
				if(salidaCrearCargoAbono.P_OUT_ERRORESCONTADOR == null || salidaCrearCargoAbono.P_OUT_ERRORESCONTADOR == 0) {
					//Actualiza el detalle del comprobante(mayor menor valor y monto retencion)
					_sql.batch(conn, SQL_UPDATE_RETENCION_DETALLE, compDetUpdate, ComprobanteDetalleUpdate.class);
					
					//Actualiza cabecera comprobante(mayor menor valor y monto de retencion, suma del detalle)
					_sql.batch(conn, SQL_UPDATE_RETENCION, compUpdate, ComprobanteUpdate.class);
					
					//AGREGAR LOG		
					{
						AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
						
							InfoComprobante infoComprobante = comprobante;
							
							AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
							log.IdCabeceraLog = infoComprobante.IdComprobante;
							log.IdTipoLog = "MODIFICACION";
							log.IdModulo = "FAC";
							log.IdUsuario = in.p_IdUsuario;
							log.IdEmpresa = in.p_IdEmpresa;
							log.FechaSistemaLog = fechaSistema;
							log.IdUsuarioAtendido = in.p_IdUsuario;
							//log.IdCliente = infoComprobante.IdCliente;
							log.EstadoCliente = "";
							log.NombreCliente = infoComprobante.NombreLargoCliente;
							log.IdTipoDocumentoIdentidad = infoComprobante.TipoDocumentoIdentidad;
							log.IdDocumentoIdentidad = infoComprobante.DocumentoIdentidad;
							log.IdPortafolio = infoComprobante.IdPortafolio;
							log.EstadoPortafolio = "";
							log.IdTipoRegistro = "COMPROBANTEFACTURACION";
							log.EstadoRegistro = infoComprobante.IdEstadoComprobante;
							log.IdTipoOperacion = infoComprobante.IdTipoOperacion;
							log.Folio = infoComprobante.IdComprobante;
							log.Cantidad = infoComprobante.CantidadTotal;
							log.PrecioTasa = infoComprobante.Precio;
							log.Monto = infoComprobante.MontoLiquidacionFinal;
							log.IdMonedaOrigen = infoComprobante.IdMoneda;
							log.IdMonedaLiquidacion = infoComprobante.IdMonedaLiquidacion;
							log.TipoCambio = infoComprobante.ValorTipoCambio.doubleValue()+"";
							log.Nemo = infoComprobante.NemotecnicoInstrumento;
							log.IdProceso = 0L;
							log.EstadoProceso = "";
							log.NombreProceso = "";						
							entradaAgregarLog.p_Log.add(log);
							
							for (ComprobanteDetalleUpdate comprobanteDetalle : compDetUpdate) {					
								if(comprobanteDetalle.MayorMenorValorActual.compareTo(comprobanteDetalle.MayorMenorValor) == -1) {
									//Agrega cambios de mayor menor valor
									AgregarLog.InAgregarLog.DetalleLog d = new AgregarLog.InAgregarLog.DetalleLog();
									d.CampoRegistro = comprobanteDetalle.IdComprobanteDetalle+"";
									d.IdCabeceraLog = comprobanteDetalle.IdComprobante;
									d.SubRegistro = "MAYOR_MENOR_VALOR";
									d.ValorAntiguo = comprobanteDetalle.MayorMenorValorActual.setScale(2, RoundingMode.CEILING).toString();
									d.ValorNuevo = comprobanteDetalle.MayorMenorValor.setScale(2, RoundingMode.CEILING).toString();
									entradaAgregarLog.p_LogDetalle.add(d);
								}
								if(comprobanteDetalle.MontoRetencionGananciaCapActual.compareTo(comprobanteDetalle.MontoRetencionGananciaCap) == -1) {
									//Agrega cambios de monto retencion
									AgregarLog.InAgregarLog.DetalleLog retencion = new AgregarLog.InAgregarLog.DetalleLog();
									retencion.CampoRegistro = comprobanteDetalle.IdComprobanteDetalle+"";
									retencion.IdCabeceraLog = comprobanteDetalle.IdComprobante;
									retencion.SubRegistro = "MONTO_RETENCION";
									retencion.ValorAntiguo = comprobanteDetalle.MontoRetencionGananciaCapActual.setScale(2, RoundingMode.CEILING).toString();
									retencion.ValorNuevo = comprobanteDetalle.MontoRetencionGananciaCap.setScale(2, RoundingMode.CEILING).toString();
									entradaAgregarLog.p_LogDetalle.add(retencion);
								}
							}
						AgregarLog agregarLogService = new AgregarLog();
						agregarLogService.logica(conn, entradaAgregarLog);
						logger.info("Fin log");						
					}
					//FIN LOG
					String codigo = "UI_COMPROBANTE_REGULARIZADO";
					String desc = Diccionario.getMensaje(conn, in.p_IdEmpresa, codigo).add("IdComprobante", comprobante.IdComprobante).msg();
					out.P_OUT_MENSAJES.add(new MensajeSalida(comprobante.IdComprobante,MensajeSalida.MENSAJE_INFORMACION,codigo,desc));	
				}
							
			}catch(SQLException sqle) {
				if(sqle.getErrorCode()==131) {
					//error especifico porque el movimiento que se esta tratando de obtener ya esta siendo procesado.
					out.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.MENSAJE_ERROR, "UI_TRANSACCION_BLOQUEADA_EN_PROCESO", "UI_TRANSACCION_BLOQUEADA_EN_PROCESO"));				
					return out;
				}else {
					throw sqle;
				}
			}
		
		
		logger.info("Fin Logica");
		
		return out;
			
	};
	
	private CrearCargoAbonoSalida mueveCuentaInversion(Connection con, List<InfoComprobanteCargo_Proceso> comprobantes, ModificarRetencionIgk_Entrada EntradaComprobante,String fechaSistema, String Tipo, BigDecimal diferenciaRetencion) throws Exception{
		logger.info("INICIO::ModificarRetencionIgk.mueveCuentaInversion");
		String 	tFechaHoraSistema 	= FechaDB.ObtenerFechaHoraSistemaUTC(con, EntradaComprobante.p_IdEmpresa);
		CrearCargoAbonoSalida salidaCrearCargoAbono = new CrearCargoAbonoSalida();
		salidaCrearCargoAbono.P_OUT_ERRORESCONTADOR = 0L;
		salidaCrearCargoAbono.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		CrearCargoAbonoEntrada entradaCrearCargoAbono = new CrearCargoAbonoEntrada();
		CrearCargoAbono crearCargoAbono = new CrearCargoAbono();
		Long i = 0L;
		HashMap<Long,Long> ids = new HashMap<Long,Long>();

		for(InfoComprobanteCargo_Proceso sol : comprobantes){
		ObjCargoAbonoAGenerar obj = new ObjCargoAbonoAGenerar();
					obj.NumeroCargoAbono =  sol.IdComprobante;
					obj.IdPortafolio = sol.IdPortafolio;
					obj.IdMoneda = sol.IdMoneda;
					obj.IdTipoMovimiento = Tipo;// "MOVCAR";
					obj.Monto = diferenciaRetencion;
					obj.IdMonedaOperacion = sol.IdMonedaComprobante;
					obj.MontoOperacion = sol.MontoComprobanteFinal;
					if (Tipo=="MOVCAR"){obj.IdConcepto ="IGKCAR";}else {obj.IdConcepto ="IGKABN";} ;
					if (Tipo=="MOVCAR"){obj.Descripcion = "CARGO_IGK";}else {obj.Descripcion = "ABONO_IGK";} ;
					obj.IdCondicionDeLiquidacion = "T0"; //campo obligatorio para certificar				
					obj.IdEjecutivo = sol.IdEjecutivo;
					obj.IdSucursal = 0L;
					obj.IdTipoProducto = sol.IdTipoProducto;
					obj.IdTipoOperacion = sol.IdTipoOperacion;
					obj.IdOperacion = sol.IdComprobante;
					obj.FechaOperacion = Fecha.toStringDate(sol.FechaComprobante);
					obj.FechaLiquidacionOperacion = tFechaHoraSistema; //Fecha.toStringDate(sol.FechaLiquidacion);
					obj.FechaCondicionDeLiquidacion = fechaSistema;//Fecha.toStringDate(sol.FechaLiquidacion);
					obj.FechaComprobante = Fecha.toStringDate(sol.FechaComprobante);
					obj.TipoCambioOperacion = new BigDecimal("0");
					obj.IdTipoComprobante = sol.IdTipoComprobante;
					obj.IdComprobante = sol.IdComprobante;
					obj.FolioComprobante = sol.FolioComprobante.toString();
					//obj.IdComprobanteDetalle = sol.IdComprobanteDetalle;
					obj.IdTipoMovimientoAsociado = "";
					obj.IdMovimientoAsociado = 0L;					
					obj.IdInstrumento = sol.IdInstrumento;
					obj.IdTipoInstrumento = sol.IdTipoInstrumento;
					obj.Nemo = sol.Nemo;
					obj.EsCargoAbonoPorTraspaso = "N";
					obj.GeneroContraparteEnTesoreria = "N";
					obj.CertificadoAutomatico = "N";//Retencion IGK No genera certificado automatico.
					ids.put(i, sol.IdComprobante);
					i++;
					entradaCrearCargoAbono.p_CargoAbonoAGenerar.add(obj);			
		}
		if(entradaCrearCargoAbono.p_CargoAbonoAGenerar.size() > 0){
			entradaCrearCargoAbono.p_IdEmpresa = EntradaComprobante.p_IdEmpresa;
			entradaCrearCargoAbono.p_IdUsuario = EntradaComprobante.p_IdUsuario;
			entradaCrearCargoAbono.p_IdModulo = EntradaComprobante.p_IdModulo;
			entradaCrearCargoAbono.p_simularOperacion = "N";
			entradaCrearCargoAbono.p_UsaReglasNegocio = EntradaComprobante.p_UsaReglasNegocio;
			entradaCrearCargoAbono.p_FechaSistema = fechaSistema;
			entradaCrearCargoAbono.p_EsIngresoManual	= "N";
			salidaCrearCargoAbono = crearCargoAbono.logica(con, entradaCrearCargoAbono);
			
			//Cambiamos el id interno por el id de la solicitud
			for(MensajeSalida mje : salidaCrearCargoAbono.P_OUT_MENSAJES){
				if(MensajeSalida.MENSAJE_ERROR.equals(mje.tipo)){
					Long id = ids.get(mje.id);
					mje.id = id;
				}
			}
		}
		logger.info("FIN::ModificarRetencionIgk.mueveCuentaInversion");
		return salidaCrearCargoAbono;
	}
	
	
    public static void main(String[] args) throws Exception {		
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {
				Gson gson = Utils.getGsonBuilder();
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":6,\"p_IdModulo\":\"FAC\",\"p_UsaReglasNegocio\":\"N\",\"p_IdComprobante\":454424,\"P_COMPROBANTERECALCULAR\":[{\"IdComprobante\":454424,\"IdComprobanteDetalle\":652527,\"MayorMenorValor\":75000,\"MayorMenorValorActual\":65000,\"MontoRetencionGananciaCap\":7500}]}";
				//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":70818,\"p_IdModulo\":\"FAC\",\"p_UsaReglasNegocio\":\"S\",\"p_IdComprobante\":454476,\"P_COMPROBANTERECALCULAR\":[{\"IdComprobante\":454476,\"IdComprobanteDetalle\":652579,\"MayorMenorValor\":4000,\"MayorMenorValorActual\":4000,\"MontoRetencionGananciaCap\":13000},{\"IdComprobante\":454476,\"IdComprobanteDetalle\":652580,\"MayorMenorValor\":11000,\"MayorMenorValorActual\":12000,\"MontoRetencionGananciaCap\":1000}]}";
				ModificarRetencionIgk_Entrada Objin = gson.fromJson(json, ModificarRetencionIgk_Entrada.class);			
				ModificarRetencionIgk modificarMvtoComp = new ModificarRetencionIgk();
				ModificarRetencionIgk_Salida Objout = modificarMvtoComp.logica(con, Objin);
				System.out.println(Objout.toString());
				//con.rollback();
				con.commit();
				
		    	}catch(Exception er) {
					er.printStackTrace();	
					con.rollback();
				}
		} catch (Exception e) {
			e.printStackTrace();			
		}	
		System.out.println("== END ==");
	};

}
