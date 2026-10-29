package cl.bcs.mesa.FWD;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.APV.AsignarContratoARecaudacion.ParamsAsignacion_Update;
import cl.bcs.custodia.utils.IngresoCustodia;
import cl.bcs.custodia.utils.RetiroCustodia;
import cl.bcs.mesa.FWD.CrearOperacionForward.RequisitosCrearOperacionForward;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class IngresarForwardCartera implements JavaCall{
	
	private final Log log = new Log("FWD.IngresarForwardCartera");
	
	private final String SQL_INSERT_OPERACIONES_FORWARD_CARTERA_WRAPPER = loadResourceToString("cl/bcs/mesa/FWD/sql/IngresarForwardCartera_InsertOperacionesForwardWrapper.sql");
	private final String SQL_SELECT_REQUISITOS_OPERACIONES_FORWARD_CARTERA = loadResourceToString("cl/bcs/mesa/FWD/sql/IngresarForwardCartera_ConsultarRequisitosOperacionForwardCartera.sql");
	private final String SQL_SELECT_OPERACIONES_FORWARD_RETIRO_CUSTODIA = loadResourceToString("cl/bcs/mesa/FWD/sql/IngresarForwardCartera_ConsultarOperacionesForwardRetiroCustodia.sql");
	private final String SQL_SELECT_OPERACIONES_FORWARD_INGRESO_CUSTODIA = loadResourceToString("cl/bcs/mesa/FWD/sql/IngresarForwardCartera_ConsultarOperacionesForwardIngresoCustodia.sql");
	private final String SQL_UPDATE_OPERACION_FORWARD = loadResourceToString("cl/bcs/mesa/FWD/sql/IngresarForwardCartera_UpdateOperacionForward.sql");
	
	public static class TablaForwardObjIn{
		public Long FolioInterno;
		public Long IdOperacion;
		public Long FolioCartera;
		public String IdTipoOperacionOriginal;
		public String IdTipoOperacionNuevo;
		public String IdTipoInstrumento;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoProducto;
		public Long IdInstrumento;
		public String Nemotecnico;
		public Long IdClienteOriginal;
		public Long IdClienteNuevo;
		public Long IdPortafolioOriginal;
		public Long IdPortafolioNuevo;
		public Long IdPersonaEjecutivoOriginal;
		public Long IdPersonaEjecutivoNuevo;
		public String IdTipoCanalIngreso;
		public String IdMonedaPrincipal;	
		public String IdMonedaSecundaria;
		public BigDecimal PrecioVencimiento;
		public BigDecimal MontoOperacionOriginal;
		public BigDecimal MontoOperacionNuevo;
		public BigDecimal MontoInicialOriginal;
		public BigDecimal MontoInicialNuevo;
		public BigDecimal MontoFinalOriginal;
		public BigDecimal MontoFinalNuevo;
		public Date FechaOperacion;
		public BigDecimal TCCierreOriginal;
		public BigDecimal TCCierreNuevo;
		public BigDecimal ParidadCierreOriginal;
		public BigDecimal ParidadCierreNuevo;
		public Long PlazoOriginal;
		public Long PlazoNuevo;
		public Date FechaVencimientoOriginal;
		public Date FechaVencimientoNuevo;
		public Date FechaPagoOriginal;
		public Date FechaPagoNuevo;
		public Long IdModalidadLiquidacionOriginal;
		public Long IdModalidadLiquidacionNuevo;
		public Long IdReferenciaMercado;
		public Long IdPlaza;
		public BigDecimal TCCostoOriginal;
		public BigDecimal TCCostoNuevo;
		public String PuntosTasasForwardOriginal;
		public String PuntosTasasForwardNuevo;
		public BigDecimal PuntosForwardOriginal;
		public BigDecimal PuntosForwardNuevo;
		public BigDecimal TCForwardOriginal;
		public BigDecimal TCForwardNuevo;
		public BigDecimal TasasMonedaPrincipalOriginal;
		public BigDecimal TasasMonedaPrincipalNuevo;
		public BigDecimal TasasMonedaSecundariaOriginal;
		public BigDecimal TasasMonedaSecundariaNuevo;
		public BigDecimal MontoDiferidoOriginal;
		public BigDecimal MontoDiferidoNuevo;
		public BigDecimal MontoMargenOriginal;
		public BigDecimal MontoMargenNuevo;
		public Long IdFixingOriginal;
		public Long IdFixingNuevo;
		public Date FechaFixingOriginal;
		public Date FechaFixingNuevo;
		public String HoraFixingOriginal;
		public String HoraFixingNuevo;
		public Long IdMesa;
		public Long IdSpotAsociado;
		public String Observaciones;
		public String IdEstado;
		public BigDecimal TasaDescuento;
		public BigDecimal TipoCambioAnticipo;
		public BigDecimal MontoARecibirAnticipo;
		public BigDecimal TipoCambioObservado;
		/*public BigDecimal TasaDescuentoOriginal;
		public BigDecimal TasaDescuentoNuevo;
		public BigDecimal TipoCambioAnticipoOriginal;
		public BigDecimal TipoCambioAnticipoNuevo;
		public BigDecimal MontoARecibirAnticipoOriginal;
		public BigDecimal MontoARecibirAnticipoNuevo;
		public BigDecimal TipoCambioObservadoOriginal;
		public BigDecimal TipoCambioObservadoNuevo;*/
		public String TipoAnticipo = "";
		
		public Long IdInterno;
		public String CodigoOperador = "";

		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class IngresarForwardCarteraEntrada {
		public Long p_IdEmpresa;
		public Long p_IdPais;
		public Long p_IdUsuario;
		public String p_IdBolsa;
		public Long p_IdSucursal;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public List<TablaForwardObjIn> p_tablaForward;
		
		public IngresarForwardCarteraEntrada() {
			this.p_tablaForward = new ArrayList<TablaForwardObjIn>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class TablaForwardObjOut{
		public Long FolioInterno;
		public Long IdCartera;
		public Long IdOperacion;
		public Long FolioCartera;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class IngresarForwardCarteraSalida {
		public List<MensajeSalida>		P_OUT_MENSAJES;
		public Long						P_OUT_ERRORESCONTADOR;
		public Long						P_OUT_ADVERTENCIASCONTADOR;
		public List<TablaForwardObjOut>	P_OUT_TBLIDFORWARDINGRESOS;
		public List<TablaForwardObjOut>	P_OUT_TBLIDFORWARDRETIROS;
		
		public IngresarForwardCarteraSalida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_TBLIDFORWARDINGRESOS = new ArrayList<TablaForwardObjOut>();
			this.P_OUT_TBLIDFORWARDRETIROS = new ArrayList<TablaForwardObjOut>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DatosMovimientoCustodia{
		public Long FolioInterno;
		public Long IdOperacion;
		public Long FolioCartera;
		public String IdTipoOperacion;
		public String IdTipoInstrumento;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoProducto;
		public Long IdInstrumento;
		public String Nemotecnico;
		public Long IdCliente;
		public Long IdPortafolio;
		public Long IdPersonaEjecutivo;
		public String IdTipoCanalIngreso;
		public String IdMonedaPrincipal;	
		public String IdMonedaSecundaria;
		public BigDecimal PrecioVencimiento;
		public BigDecimal MontoOperacion;
		public BigDecimal MontoInicial;
		public BigDecimal MontoFinal;
		public Date FechaOperacion;
		public BigDecimal TCCierre;
		public BigDecimal ParidadCierre;
		public Long Plazo;
		public Date FechaVencimiento;
		public Date FechaPago;
		public Long IdModalidadLiquidacion;
		public Long IdReferenciaMercado;
		public Long IdPlaza;
		public BigDecimal TCCosto;
		public String PuntosTasasForward;
		public BigDecimal PuntosForward;
		public BigDecimal TCForward;
		public BigDecimal TasasMonedaPrincipal;
		public BigDecimal TasasMonedaSecundaria;
		public BigDecimal MontoDiferido;
		public BigDecimal MontoMargen;
		public Long IdFixing;
		public Date FechaFixing;
		public String HoraFixing;
		public Long IdMesa;
		public Long IdSpotAsociado;
		public String Observaciones;
		public BigDecimal TasaDescuento;
		public BigDecimal TipoCambioAnticipo;
		public BigDecimal MontoARecibirAnticipo;
		public BigDecimal TipoCambioObservado;

		public BigDecimal TipoCambioMonedaPrincipal;
		public String TipoMonedaAdicionalSistema;
		public BigDecimal ParidadMonedaAdicionalSistema;
		public String TipoMonedaPrincipal;
		public Long IdCartera;
		
		public String IdEstado;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		IngresarForwardCarteraEntrada in = gson.fromJson(jin.toString(), IngresarForwardCarteraEntrada.class);
		IngresarForwardCarteraSalida out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		long c = 0;
		for (MensajeSalida s : out.P_OUT_MENSAJES) {
			if (s.tipo.equals("AUTORIZACION"))
				c++;
		}
		out.P_OUT_ADVERTENCIASCONTADOR = c;
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public IngresarForwardCarteraSalida logica(Connection conn, IngresarForwardCarteraEntrada in) throws Exception {
		SQL _sql = new SQL();
		IngresarForwardCarteraSalida out = new IngresarForwardCarteraSalida();
		out.P_OUT_ERRORESCONTADOR = 0L;
		out.P_OUT_ADVERTENCIASCONTADOR = 0L;
		Long Id = 0L;
		Long errCont = 0L;
		HashMap<String,String> mapMsg = new HashMap<>();
		
		log.write("INICIO :: FWD.IngresarForwardCartera");
		
		try{
			if(in.p_tablaForward.size() == 0){
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_OPERACIONES",mapMsg,conn,in.p_IdEmpresa));
				Id++;
				return out;
			}
			
			RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
			RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(conn, in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
			if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
				out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
				return out;
			}
			
			/***** VARIABLES *****/
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn,in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			String fechaHoraSistemaSecondate = FechaDB.ObtenerFechaHoraSistemaUTC(conn,in.p_IdEmpresa);
			String monedaAdicionalSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaAdicional");
			
			Long cantOpeIn = 0L;
			
			List<Long> FolioInterno = new ArrayList<Long>();
			HashMap<Long,TablaForwardObjIn> operacionForwardCarteraHash = new HashMap<Long,TablaForwardObjIn>();
			for (TablaForwardObjIn ope: in.p_tablaForward){
				FolioInterno.add(ope.FolioInterno);
				cantOpeIn++;
				operacionForwardCarteraHash.put(ope.FolioInterno, ope);
			}
			
			//Insert tablas globales
			log.write("INSERT en tabla BSCL.Mesa.BD::FWD.IngresarForwardCartera_Proceso :: "+in.p_tablaForward);
			_sql.batch(conn, SQL_INSERT_OPERACIONES_FORWARD_CARTERA_WRAPPER, in.p_tablaForward, TablaForwardObjIn.class);
			
			
			/***** VALIDACIONES *****/
			HashMap<String,Object> params = new HashMap<>();
			params.put("IdEmpresa", in.p_IdEmpresa);
			params.put("FolioInterno", FolioInterno);
			
			if(in.p_UsaReglasNegocio.equals("S")){
				List<RequisitosCrearOperacionForward> requisitosCrearOperacionForward = _sql.query(conn, SQL_SELECT_REQUISITOS_OPERACIONES_FORWARD_CARTERA, params, RequisitosCrearOperacionForward.class);
				
				for (RequisitosCrearOperacionForward req: requisitosCrearOperacionForward){
					if(!req.IdEstadoMesa.equals("A")){
						mapMsg.put("FolioInterno", req.FolioInterno);
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"MESA_FORWARD_CERRADA",mapMsg,conn,in.p_IdEmpresa));
						Id++;
					}
					if(req.MontoOperacion.compareTo(BigDecimal.ZERO) <= 0){
						mapMsg.put("FolioInterno", req.FolioInterno);
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"MONTO_MAYOR_CERO",mapMsg,conn,in.p_IdEmpresa));
						Id++;
					}
					if(req.Nemotecnico.equals("SECARE") || req.Nemotecnico.equals("SEINFL")){
						if(req.MontoInicial.compareTo(BigDecimal.ZERO) <= 0){
							mapMsg.put("FolioInterno", req.FolioInterno);
							out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"MONTO_INICIAL_MAYOR_CERO",mapMsg,conn,in.p_IdEmpresa));
							Id++;
						}
						if(req.MontoFinal.compareTo(BigDecimal.ZERO) <= 0){
							mapMsg.put("FolioInterno", req.FolioInterno);
							out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"MONTO_FINAL_MAYOR_CERO",mapMsg,conn,in.p_IdEmpresa));
							Id++;
						}
					}
					if(!Fecha.toStringDate(req.FechaOperacion).equals(fechaSistema) && (req.IdTipoOperacion.equals("COMFWD") || req.IdTipoOperacion.equals("VENFWD"))){
						mapMsg.put("FolioInterno", req.FolioInterno);
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"FECHA_OPERACION_DISTINTA_FECHA_PROCESO",mapMsg,conn,in.p_IdEmpresa));
						Id++;
					}
					if(Fecha.toStringDate(req.FechaVencimiento).equals(fechaSistema) && !req.IdTipoOperacion.equals("VCDFWD")){
						mapMsg.put("FolioInterno", req.FolioInterno);
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"FECHA_VENCIMIENTO_DISTINTO_OPERACION",mapMsg,conn,in.p_IdEmpresa));
						Id++;
					}
					if(req.Plazo <= 0){
						mapMsg.put("FolioInterno", req.FolioInterno);
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"PLAZO_MAYOR_CERO",mapMsg,conn,in.p_IdEmpresa));
						Id++;
					}
					if(!req.IdEstadoCuentaInversionMonedaPrincipal.equals("ACT") && !req.Nemotecnico.equals("SEINFL")){
						mapMsg.put("FolioInterno", req.FolioInterno);
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"MONEDA_NO_HABILITADA_CUENTA_INVERSION",mapMsg,conn,in.p_IdEmpresa));
						Id++;
					}
					if(!req.ValorIndicadorFirmaCCGFWD.equals("S")){
						mapMsg.put("FolioInterno", req.FolioInterno);
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"FIRMA_CCG_FWD_CLIENTE",mapMsg,conn,in.p_IdEmpresa));
						Id++;
					}
					if(!(req.IdEstadoModulo.equals("ACT") && req.TipoEstadoModulo.equals("A"))){
						mapMsg.put("FolioInterno", req.FolioInterno);
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"UI_MODULO_CERRADO",mapMsg,conn,in.p_IdEmpresa));
						Id++;
					}
					if(req.ResultadoPerfilProducto.equals("NOK")){
						mapMsg.put("FolioInterno", req.FolioInterno);
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,req.MensajePerfilProducto,mapMsg,conn,in.p_IdEmpresa));
						Id++;
					}
					if(!(req.IdCliente > 0L && req.IdEstadoCliente.equals("ACT"))){
						String msjError = "";
						switch(req.IdEstadoCliente){
							case "BLO": msjError = "CLIENTE_BLOQUEADO"; break;
							case "PEN": msjError = "CLIENTE_PENDIENTE_AUTORIZACION"; break;
							default : msjError = "CLIENTE_NO_HABILITADO"; break;
						}
						mapMsg.put("FolioInterno", req.FolioInterno);
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,msjError,mapMsg,conn,in.p_IdEmpresa));
						Id++;
					}
					if(!(req.IdPortafolio > 0L && req.IdEstadoPortafolio.equals("ACT"))){
						String msjError = "";
						switch(req.IdEstadoCliente){
							case "BLO": msjError = "PORTAFOLIO_BLOQUEADO"; break;
							case "PEN": msjError = "PORTAFOLIO_PENDIENTE_AUTORIZACION"; break;
							default : msjError = "PORTAFOLIO_NO_HABILITADO"; break;
						}
						mapMsg.put("FolioInterno", req.FolioInterno);
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,msjError,mapMsg,conn,in.p_IdEmpresa));
						Id++;
					}
					if(req.TCCosto.compareTo(BigDecimal.ZERO) <= 0 && req.IdTipoInstrumento.equals("FWD") && !req.Nemotecnico.equals("ARBITR")){
						mapMsg.put("FolioInterno", req.FolioInterno);
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"TIPO_COSTO_MAYOR_CERO",mapMsg,conn,in.p_IdEmpresa));
						Id++;
					}
					if(req.TCCosto.compareTo(BigDecimal.ZERO) <= 0 && req.IdTipoInstrumento.equals("FWD") && !req.Nemotecnico.equals("ARBITR")){
						mapMsg.put("FolioInterno", req.FolioInterno);
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"TIPO_COSTO_CIERRE_DENTRO_RANGO_DEFINIDO",mapMsg,conn,in.p_IdEmpresa));
						Id++;
					}
					if(req.TCCierre.compareTo(BigDecimal.ZERO) <= 0 && !req.Nemotecnico.equals("ARBITR")){
						mapMsg.put("FolioInterno", req.FolioInterno);
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"TC_CIERRE_MAYOR_CERO",mapMsg,conn,in.p_IdEmpresa));
						Id++;
					}
					if(req.ParidadCierre.compareTo(BigDecimal.ZERO) <= 0 && req.Nemotecnico.equals("ARBITR")){
						mapMsg.put("FolioInterno", req.FolioInterno);
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"PARIDAD_CIERRE_MAYOR_CERO",mapMsg,conn,in.p_IdEmpresa));
						Id++;
					}
					if(req.TipoCambioUF.compareTo(BigDecimal.ZERO) <= 0 && req.Nemotecnico.equals("SECARE")){
						mapMsg.put("FolioInterno", req.FolioInterno);
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"SIN_VALOR_TIPO_CAMBIO_UF_SECARE",mapMsg,conn,in.p_IdEmpresa));
						Id++;
					}
				}
			}
			
			
			//Retorna si existen errores 
			errCont = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			if (!errCont.equals(0L) ){
				return out;
			}
			
			
			/***** FORWARD CUSTODIA *****///TODO
			params = new HashMap<>();
			params.put("IdEmpresa", in.p_IdEmpresa);
			params.put("FolioInterno", FolioInterno);
			params.put("MonedaAdicionalSistema", monedaAdicionalSistema);
			
			List<DatosMovimientoCustodia> datosMovimientoRetiroCustodia = _sql.query(conn, SQL_SELECT_OPERACIONES_FORWARD_RETIRO_CUSTODIA, params, DatosMovimientoCustodia.class);
			List<DatosMovimientoCustodia> datosMovimientoIngresoCustodia = _sql.query(conn, SQL_SELECT_OPERACIONES_FORWARD_INGRESO_CUSTODIA, params, DatosMovimientoCustodia.class);
			
			String idTipoOperacion = operacionForwardCarteraHash.get(FolioInterno.get(0)).IdTipoOperacionNuevo;

			// RETIRO CUSTODIA
			if (idTipoOperacion.equals("MODFWD") || idTipoOperacion.equals("ANUFWD") || idTipoOperacion.equals("ANTFWD") || idTipoOperacion.equals("VCDFWD") || (idTipoOperacion.equals("REVFWD"))) {
				log.write("MESA FORWARD :: Llamada Retiro Custodia");
				
				String accionOperacion = "";
				if(idTipoOperacion.equals("MODFWD")) accionOperacion = "MODIFICACION";
				else if(idTipoOperacion.equals("ANUFWD")) accionOperacion = "ANULACION";
				else if(idTipoOperacion.equals("ANTFWD")) accionOperacion = "ANTICIPO";
				else if(idTipoOperacion.equals("VCDFWD")) accionOperacion = "VENCIMIENTO";
				//else if(idTipoOperacion.equals("REVFWD")) accionOperacion = "REVERSA";
				else if(idTipoOperacion.equals("REVFWD")) accionOperacion = "ANULACION";
				
				List<RetiroCustodia.ObjDatosEntrada> datosEntradaCustodia = new ArrayList<RetiroCustodia.ObjDatosEntrada>();
				Long i = 1L;
				
				for(DatosMovimientoCustodia dmc: datosMovimientoRetiroCustodia){
					RetiroCustodia.ObjDatosEntrada cusIn = new RetiroCustodia.ObjDatosEntrada();
					cusIn.IdGenerarMovimientoCustodia = i;
					cusIn.TablaAfectada = "CARTERA";
					//cusIn.EsRetiroNegocio = "S";
					
					cusIn.IdCustodia = dmc.IdCartera;
					cusIn.FolioCustodia = dmc.FolioCartera;
					
					//cusIn.IdCarteraOrigen = dmc.IdCartera;
					
					// OPERACION
					cusIn.AccionOperacion = accionOperacion;
					cusIn.FechaRetiroRegistro = Fecha.toTimeStamp(fechaHoraSistemaSecondate);
					cusIn.IdCliente = dmc.IdCliente;
					cusIn.IdPortafolio = dmc.IdPortafolio;
					cusIn.FechaOperacion = dmc.FechaOperacion;
					cusIn.IdTipoOperacion = dmc.IdTipoOperacion;
					cusIn.IdOperacion = dmc.IdOperacion;
					cusIn.IdConcepto = dmc.IdTipoOperacion;
					cusIn.IdCondicionLiquidacion = "T0";
					cusIn.FechaLiquidacion = dmc.FechaVencimiento;
					cusIn.IdTipoCanalIngreso = dmc.IdTipoCanalIngreso;
					//cusIn.IdTipoComprobante = dmc.;
					//cusIn.FolioComprobante = dmc.IdOperacion; //TODO se comento para controlar problema reversa
					//cusIn.FechaComprobante = dmc.;
					//cusIn.FolioExternoReferencia = dmc.;
					cusIn.IdSucursal = in.p_IdSucursal;
					cusIn.IdAgente = dmc.IdPersonaEjecutivo;
					cusIn.IdUsuario = in.p_IdUsuario;
					cusIn.IdModulo = in.p_IdModulo;
					cusIn.Glosa = dmc.Observaciones;
					
					// INSTRUMENTO
					cusIn.IdInstrumento = dmc.IdInstrumento;
					cusIn.IdMonedaInstrumento = dmc.IdMonedaPrincipal;
					//cusIn.IdMonedaEmision;
					//cusIn.IdTipoReajusteInstrumento;
					//cusIn.IdEmisor;
					//cusIn.EsFungible;
					//cusIn.IdBoveda;
					//cusIn.CuentaBoveda;
					//cusIn.EsNominal;
					//cusIn.FechaVencimientoInstrumento;
					
					// CANTIDADES Y MONTOS
					cusIn.Cantidad = dmc.MontoOperacion; 
					//cusIn.CorteMinimo; 
					cusIn.ValorRescate = BigDecimal.ZERO;
					//cusIn.PrecioSucio;
					cusIn.PrecioCompra = dmc.PrecioVencimiento; 
					cusIn.TasaCompra = dmc.TCForward;
					//cusIn.MontoCompraMonedaInstrumento; 
					//cusIn.TasaAgente; 
					//cusIn.PrecioAPlazo;
					//cusIn.MontoAPlazo; 
					//cusIn.MontoAPlazoMonedaInstrumento;
					
					// FORWARD
					cusIn.IdSpotAsociado = dmc.IdSpotAsociado;
					cusIn.IdModalidadCumplimiento = dmc.IdModalidadLiquidacion;
					cusIn.MontoMonedaPrincipal = dmc.MontoOperacion;
					cusIn.TipoMonedaPrincipal = dmc.TipoMonedaPrincipal;
					if(dmc.Nemotecnico.equals("ARBITR")){
						cusIn.MontoMonedaSecundaria = BigDecimal.ZERO;
						if(dmc.TipoMonedaPrincipal.equals("D") && !(dmc.ParidadCierre.compareTo(BigDecimal.ZERO) == 0)){
							cusIn.MontoMonedaSecundaria = dmc.MontoOperacion.divide(dmc.ParidadCierre,8,RoundingMode.HALF_UP);
						}
						else if(dmc.TipoMonedaPrincipal.equals("M")){
							cusIn.MontoMonedaSecundaria = dmc.MontoOperacion.multiply(dmc.ParidadCierre);
						}
					}
					else{
						cusIn.MontoMonedaSecundaria = dmc.MontoOperacion.multiply(dmc.TCCierre);
					}
					//cusIn.IdMonedaLiquidacion = dmc.;
					cusIn.MontoLiquidacion = dmc.IdTipoOperacion.equals("VCDFWD") || dmc.IdTipoOperacion.equals("ANTFWD") ? dmc.MontoARecibirAnticipo : BigDecimal.ZERO;
					//cusIn.MontoLiquidacionMonedaPrincipal = dmc.;
					cusIn.MontoDiferido = dmc.MontoDiferido;
					//cusIn.MontoUtilidadDiferida = dmc.;
					//cusIn.MontoPerdidaDiferida = dmc.;
					cusIn.MontoMargen = dmc.MontoMargen;
					//cusIn.MontoValorRazonableActivo = dmc.;
					//cusIn.MontoValorRazonablePasivo = dmc.;
					//cusIn.MontoValorRazonableNeto = dmc.;
					//cusIn.TasaValorRazonableActivo = dmc.;
					//cusIn.TasaValorRazonablePasivo = dmc.;
					//cusIn.MontoDevengoTipoCambio = dmc.;
					//cusIn.MontoDevengoTipoCambioAnterior = dmc.;
					//cusIn.MontoDiferidoDiario = dmc.;
					//cusIn.MontoDiferidoAcumulado = dmc.;
					//cusIn.ValorTipoCambioSpot = dmc.;
					//cusIn.ValorTipoCambioCotizador = dmc.;
					cusIn.EsPuntos = dmc.PuntosTasasForward;
					cusIn.TasaPuntosMonedaPrincipal = dmc.PuntosTasasForward.equals("S") ? dmc.PuntosForward : dmc.TasasMonedaPrincipal;
					cusIn.TasaPuntosMonedaSecundaria = dmc.PuntosTasasForward.equals("S") ? dmc.TCForward : dmc.TasasMonedaSecundaria;
					cusIn.MontoMatriz = BigDecimal.ZERO;
					cusIn.TasaDescuentoAnticipo = dmc.TasaDescuento;
					cusIn.TipoCambioAnticipo = dmc.TipoCambioAnticipo;
					cusIn.MontoARecibirAnticipo = dmc.MontoARecibirAnticipo;
					cusIn.TipoCambioObservado = dmc.TipoCambioObservado;
					cusIn.FechaEfectiva = dmc.FechaOperacion;
					cusIn.IdPlaza = dmc.IdPlaza;
					cusIn.IdFixing = dmc.IdFixing;
					cusIn.FechaFixing = dmc.FechaFixing;
					cusIn.HoraFixing = Fecha.toTime(dmc.HoraFixing);
					cusIn.IdReferenciaMercado = dmc.IdReferenciaMercado;
					cusIn.IdMonedaPrincipal = dmc.IdMonedaPrincipal;
					cusIn.IdMonedaSecundaria = dmc.IdMonedaSecundaria;
					cusIn.ValorTipoCambioCierre = dmc.TCCierre;
					cusIn.ValorParidadCierre = dmc.ParidadCierre;
					cusIn.ValorTipoCambioCosto = !dmc.Nemotecnico.equals("ARBITR") ? dmc.TCCosto : BigDecimal.ZERO;
					cusIn.ValorParidadCosto = dmc.Nemotecnico.equals("ARBITR") ? dmc.TCCosto : BigDecimal.ZERO;
					cusIn.IdEstado = dmc.IdEstado;
					
					datosEntradaCustodia.add(cusIn);
					i++;
				}
				
				RetiroCustodia retiroCustodia = new RetiroCustodia();
				RetiroCustodia.RetiroCustodiaEntrada retiroCustodiaIn = new RetiroCustodia.RetiroCustodiaEntrada();
				retiroCustodiaIn.p_IdEmpresa = in.p_IdEmpresa;
				retiroCustodiaIn.p_IdUsuario = in.p_IdUsuario;
				retiroCustodiaIn.p_SimularOperacion = "N";
				retiroCustodiaIn.p_TicketAtencion = 1L;
				retiroCustodiaIn.p_DatosEntrada = datosEntradaCustodia;
				retiroCustodiaIn.p_ValorizacionAutomatica = "N";
				RetiroCustodia.RetiroCustodiaSalida retiroCustodiaOut = retiroCustodia.logica(conn, retiroCustodiaIn);
				
				//Procesa Salida del Retiro Custodia
				out.P_OUT_ERRORESCONTADOR = out.P_OUT_ERRORESCONTADOR + retiroCustodiaOut.P_OUT_ERRORESCONTADOR + MensajeSalida.getNumeroErrores(retiroCustodiaOut.P_OUT_MENSAJES);
				out.P_OUT_MENSAJES.addAll(retiroCustodiaOut.P_OUT_MENSAJES);
				if (out.P_OUT_ERRORESCONTADOR > 0){
					return out;
				}
				
				for(DatosMovimientoCustodia dmc: datosMovimientoRetiroCustodia){
					for(RetiroCustodia.RetiroCustodiaSalida.ObjDatosSalida cusOut: retiroCustodiaOut.P_OUT_DATOSSALIDA){
						if(dmc.IdCartera.longValue() == cusOut.IdCustodia.longValue()){
							TablaForwardObjOut opOut = new TablaForwardObjOut();
							opOut.FolioInterno = dmc.FolioInterno;
							opOut.IdCartera = cusOut.IdCustodia;
							opOut.IdOperacion = cusOut.IdOperacion;
							opOut.FolioCartera = cusOut.FolioCustodia;
							out.P_OUT_TBLIDFORWARDRETIROS.add(opOut);
						}
					}
				}
			}

			// INGRESO CUSTODIA
			if (idTipoOperacion.equals("COMFWD") || idTipoOperacion.equals("VENFWD") || idTipoOperacion.equals("MODFWD") || (idTipoOperacion.equals("REVFWD"))) {
				log.write("MESA FORWARD :: Llamada Ingreso Custodia");
				
				List<IngresoCustodia.ObjDatosEntrada> datosEnradaCustodia = new ArrayList<IngresoCustodia.ObjDatosEntrada>();
				Long i = 1L;
				
				for(DatosMovimientoCustodia dmc: datosMovimientoIngresoCustodia){
					IngresoCustodia.ObjDatosEntrada cusIn = new IngresoCustodia.ObjDatosEntrada();
					cusIn.IdGenerarMovimientoCustodia = i;
					cusIn.TablaAfectada = "CARTERA";

					if (idTipoOperacion.equals("MODFWD") || idTipoOperacion.equals("REVFWD")) {
						cusIn.GenerarNuevoFolioCustodia = "N";
						cusIn.MantieneFolioCustodia = "S";
						cusIn.IdCarteraOrigen = dmc.IdCartera;
						
						// SALDOS CARTERAS
						cusIn.IdCustodia = dmc.IdCartera;
						cusIn.FolioCustodia = dmc.FolioCartera;
					}
					cusIn.EsRegistroNuevo = "S";
					
					// OPERACION
					cusIn.AccionOperacion = "INGRESO";
					cusIn.IdCliente = dmc.IdCliente;
					cusIn.IdPortafolio = dmc.IdPortafolio;
					//cusIn.IdCuentaCustodioPortafolio = dmc.;
					cusIn.IdClienteRelacionado = dmc.IdCliente;
					cusIn.IdPortafolioRelacionado = dmc.IdPortafolio;
					//cusIn.IdCuentaCustodioPortafolioRelacionado = dmc.;
					cusIn.FechaOperacion = dmc.FechaOperacion;
					cusIn.FechaVencimientoOperacion = dmc.FechaVencimiento;
					cusIn.PlazoEnDiasOperacion = dmc.Plazo;
					cusIn.IdTipoOperacion = dmc.IdTipoOperacion;
					//cusIn.IdGrupoOperacion = dmc.;
					cusIn.IdOperacion = dmc.IdOperacion;
					cusIn.IdConcepto = dmc.IdTipoOperacion;
					//cusIn.FolioOperacionRelacionada = dmc.;
					cusIn.IdCondicionLiquidacion = "T0";
					cusIn.FechaLiquidacion = dmc.FechaOperacion;//dmc.FechaVencimiento;//IMPCREDOPT-3448: sp BSCL.Custodia.BD.Servicios::ConsultarCustodiaMovimientos busca por INGFechaLiquidacion
					cusIn.IdTipoProducto = dmc.IdTipoProducto;
					cusIn.IdTipoCanalIngreso = dmc.IdTipoCanalIngreso;
					//cusIn.CodigoIFRS = dmc.;
					//cusIn.IdTipoComprobante = dmc.;
					cusIn.FolioComprobante = dmc.IdOperacion;
					//cusIn.FechaComprobante = dmc.;
					//cusIn.FolioExternoReferencia = dmc.;
					cusIn.IdMesa = dmc.IdMesa;
					cusIn.IdBolsa = in.p_IdBolsa;
					//cusIn.EsIntraday = dmc.;
					//cusIn.TipoCarteraCorredor = dmc.;
					//cusIn.UsaGarantiaEspecifica = dmc.;
					//cusIn.CobraPrimaAlInicio = dmc.;
					//cusIn.IdTipoPrestamo = dmc.;
					cusIn.IdUsuario = in.p_IdUsuario;
					cusIn.IdSucursal = in.p_IdSucursal;
					cusIn.IdAgente = dmc.IdPersonaEjecutivo;
					cusIn.Glosa = dmc.Observaciones;
					//cusIn.PermiteCertificacionPorUsuario = dmc.;
					
					// INSTRUMENTO
					cusIn.IdInstrumento = dmc.IdInstrumento;
					//cusIn.IdInstrumentoAnterior = dmc.;
					cusIn.IdMonedaInstrumento = dmc.IdMonedaPrincipal;
					//cusIn.IdMonedaEmision;
					//cusIn.IdTipoReajusteInstrumento;
					//cusIn.IdEmisor;
					//cusIn.EsFungible;
					//cusIn.IdBoveda;
					//cusIn.CuentaBoveda;
					//cusIn.EsNominal;
					//cusIn.FechaVencimientoInstrumento;
					//cusIn.FechaEmisionInstrumento;
					
					// CANTIDADES Y MONTOS
					cusIn.Cantidad = dmc.MontoOperacion; 
					//cusIn.CorteMinimo; 
					cusIn.ValorRescate = BigDecimal.ZERO;
					//cusIn.PrecioSucio;
					cusIn.PrecioCompra = dmc.PrecioVencimiento; 
					cusIn.TasaCompra = dmc.TCForward;
					cusIn.MontoCompra = dmc.MontoOperacion.multiply(dmc.TipoCambioMonedaPrincipal); 
					//cusIn.MontoCompraMonedaInstrumento; 
					//cusIn.TasaAgente; 
					//cusIn.PrecioAPlazo;
					//cusIn.MontoAPlazo; 
					//cusIn.MontoAPlazoMonedaInstrumento;
					
					// FORWARD
					cusIn.IdSpotAsociado = dmc.IdSpotAsociado;
					cusIn.IdModalidadCumplimiento = dmc.IdModalidadLiquidacion;
					cusIn.MontoMonedaPrincipal = dmc.MontoOperacion;
					if(dmc.Nemotecnico.equals("ARBITR")){
						cusIn.MontoMonedaSecundaria = BigDecimal.ZERO;
						if(dmc.TipoMonedaPrincipal.equals("D") && !(dmc.ParidadCierre.compareTo(BigDecimal.ZERO) == 0)){
							cusIn.MontoMonedaSecundaria = dmc.MontoOperacion.divide(dmc.ParidadCierre,8,RoundingMode.HALF_UP);
						}
						else if(dmc.TipoMonedaPrincipal.equals("M")){
							cusIn.MontoMonedaSecundaria = dmc.MontoOperacion.multiply(dmc.ParidadCierre);
						}
					}
					else{
						if(dmc.IdMonedaSecundaria.equals("CLP")) {
							cusIn.MontoMonedaSecundaria = dmc.MontoOperacion.multiply(dmc.TCCierre).setScale(0, RoundingMode.HALF_UP);
						}else {
							cusIn.MontoMonedaSecundaria = dmc.MontoOperacion.multiply(dmc.TCCierre);
						}
					}
					//cusIn.IdMonedaLiquidacion = dmc.;
					cusIn.MontoLiquidacion = dmc.IdTipoOperacion.equals("VCDFWD") || dmc.IdTipoOperacion.equals("ANTFWD") ? dmc.MontoARecibirAnticipo : BigDecimal.ZERO;
					//cusIn.MontoLiquidacionMonedaPrincipal = dmc.;
					cusIn.MontoDiferido = dmc.MontoDiferido;
					//cusIn.MontoUtilidadDiferida = dmc.;
					//cusIn.MontoPerdidaDiferida = dmc.;
					cusIn.MontoMargen = dmc.MontoMargen;
					//cusIn.MontoValorRazonableActivo = dmc.;
					//cusIn.MontoValorRazonablePasivo = dmc.;
					//cusIn.MontoValorRazonableNeto = dmc.;
					//cusIn.TasaValorRazonableActivo = dmc.;
					//cusIn.TasaValorRazonablePasivo = dmc.;
					//cusIn.MontoDevengoTipoCambio = dmc.;
					//cusIn.MontoDevengoTipoCambioAnterior = dmc.;
					//cusIn.MontoDiferidoDiario = dmc.;
					//cusIn.MontoDiferidoAcumulado = dmc.;
					//cusIn.ValorTipoCambioSpot = dmc.;
					//cusIn.ValorTipoCambioCotizador = dmc.;
					cusIn.EsPuntos = dmc.PuntosTasasForward;
					cusIn.TasaPuntosMonedaPrincipal = dmc.PuntosTasasForward.equals("S") ? dmc.PuntosForward : dmc.TasasMonedaPrincipal;
					cusIn.TasaPuntosMonedaSecundaria = dmc.PuntosTasasForward.equals("S") ? dmc.TCForward : dmc.TasasMonedaSecundaria;
					cusIn.MontoMatriz = BigDecimal.ZERO;
					cusIn.TasaDescuentoAnticipo = dmc.TasaDescuento;
					cusIn.TipoCambioAnticipo = dmc.TipoCambioAnticipo;
					cusIn.MontoARecibirAnticipo = dmc.MontoARecibirAnticipo;
					cusIn.TipoCambioObservado = dmc.TipoCambioObservado;
					cusIn.FechaEfectiva = dmc.FechaOperacion;
					cusIn.IdPlaza = dmc.IdPlaza;
					cusIn.IdFixing = dmc.IdFixing;
					cusIn.FechaFixing = dmc.FechaFixing;
					cusIn.HoraFixing = Fecha.toTime(dmc.HoraFixing);
					cusIn.IdReferenciaMercado = dmc.IdReferenciaMercado;
					cusIn.IdMonedaPrincipal = dmc.IdMonedaPrincipal;
					cusIn.IdMonedaSecundaria = dmc.IdMonedaSecundaria;
					cusIn.ValorTipoCambioCierre = dmc.TCCierre;
					cusIn.ValorParidadCierre = dmc.ParidadCierre;
					cusIn.ValorTipoCambioCosto = !dmc.Nemotecnico.equals("ARBITR") ? dmc.TCCosto : BigDecimal.ZERO;
					cusIn.ValorParidadCosto = dmc.Nemotecnico.equals("ARBITR") ? dmc.TCCosto : BigDecimal.ZERO;
					if(dmc.IdEstado.equals("PEN")) {
						cusIn.EsPendienteAutorizacion = "S";
					}
					datosEnradaCustodia.add(cusIn);
					i++;
				}
				
				IngresoCustodia ingresoCustodia = new IngresoCustodia();
				IngresoCustodia.IngresoCustodiaEntrada ingresoCustodiaIn = new IngresoCustodia.IngresoCustodiaEntrada();
				ingresoCustodiaIn.p_IdEmpresa = in.p_IdEmpresa;
				ingresoCustodiaIn.p_IdUsuario = in.p_IdUsuario;
				ingresoCustodiaIn.p_IdModulo = in.p_IdModulo;
				ingresoCustodiaIn.p_SimularOperacion = "N";
				ingresoCustodiaIn.p_TicketAtencion = 1L;
				ingresoCustodiaIn.p_DatosEntrada = datosEnradaCustodia;
				ingresoCustodiaIn.p_ValorizacionAutomatica = "N";
				IngresoCustodia.IngresoCustodiaSalida ingresoCustodiaOut = ingresoCustodia.logica(conn, ingresoCustodiaIn);
				
				//Procesa Salida del Ingreso Custodia
				out.P_OUT_ERRORESCONTADOR = out.P_OUT_ERRORESCONTADOR + ingresoCustodiaOut.P_OUT_ERRORESCONTADOR;
				out.P_OUT_MENSAJES.addAll(ingresoCustodiaOut.P_OUT_MENSAJES);
				if (out.P_OUT_ERRORESCONTADOR > 0){
					return out;
				} else { /* Cambia estado del movimiento FWD a VIG y quita la fecha de liquidacion */
					/*
					 ParamsAsignacion_Update Param_Asignacion = new ParamsAsignacion_Update();
					Param_Asignacion.IdRecaudacionCliente = fila.IdRecaudacionCliente;
					Param_Asignacion.IdContratoSAAPV = fila.IdContratoSAAPV;
					Param_Asignacion.FechaModificacion = FechaHora;
					Update_Asignacion.add(Param_Asignacion);
					 */
					
					List<ParamsOperacionFWD_Update> Update_OperacionFWD = new ArrayList<ParamsOperacionFWD_Update>();
					ParamsOperacionFWD_Update Params_OperacionFWD = new ParamsOperacionFWD_Update();
					Params_OperacionFWD.FolioCartera = in.p_tablaForward.get(0).FolioCartera;
					Update_OperacionFWD.add(Params_OperacionFWD);

					// params.clear();
					// params.put("FolioCartera", in.p_tablaForward.get(0).FolioCartera);
					//_sql.batch(conn, SQL_UPDATE_OPERACION_FORWARD, Update_OperacionFWD,
					//		ParamsOperacionFWD_Update.class);
					// sql.batch(con,SQL_UPDATE_AsignacionContratoRecaudacion,Update_Asignacion,ParamsAsignacion_Update.class);
					// List<ParamsOperacionFWD_Update> Update_OperacionFWD = new
					// ArrayList<ParamsOperacionFWD_Update>();
				}
				
				for(DatosMovimientoCustodia dmc: datosMovimientoIngresoCustodia){
					for(IngresoCustodia.IngresoCustodiaSalida.ObjDatosSalida cusOut: ingresoCustodiaOut.P_OUT_DATOSSALIDA){
						if(dmc.IdOperacion.longValue() == cusOut.IdOperacion.longValue()){
							TablaForwardObjOut opOut = new TablaForwardObjOut();
							opOut.FolioInterno = dmc.FolioInterno;
							opOut.IdCartera = cusOut.IdCustodia;
							opOut.IdOperacion = cusOut.IdOperacion;
							opOut.FolioCartera = cusOut.FolioCustodia;
							out.P_OUT_TBLIDFORWARDINGRESOS.add(opOut);
						}
					}
				}
			}
			
			
			/***** SALIDA *****/
			out.P_OUT_ERRORESCONTADOR = out.P_OUT_ERRORESCONTADOR + MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			
		}catch(Exception e){
			out.P_OUT_ERRORESCONTADOR = 1L;
			log.write(e);
			throw e;
		}
		
		return out;
	}
	
	public static class ParamsOperacionFWD_Update{
		public Long FolioCartera;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String jsonDesaIngFwd = "{\"p_IdEmpresa\":1,\"p_IdPais\":44,\"p_IdUsuario\":33,\"p_IdBolsa\":\"BCS\",\"p_IdSucursal\":0,\"p_IdModulo\":\"MFW\",\"p_UsaReglasNegocio\":\"S\",\"p_tablaForward\":[{\"FolioInterno\":20687568,\"IdOperacion\":2142,\"FolioCartera\":20687568,\"IdTipoOperacionOriginal\":\"COMFWD\",\"IdTipoOperacionNuevo\":\"MODFWD\",\"IdTipoInstrumento\":\"FWD\",\"IdClasificacionTipoInstrumento\":\"DV\",\"IdTipoProducto\":\"MEFW\",\"IdInstrumento\":346370,\"Nemotecnico\":\"SECANO\",\"IdClienteOriginal\":130826,\"IdClienteNuevo\":130826,\"IdPortafolioOriginal\":34993,\"IdPortafolioNuevo\":34993,\"IdPersonaEjecutivoOriginal\":1335561,\"IdPersonaEjecutivoNuevo\":1335561,\"IdTipoCanalIngreso\":\"MES\",\"IdMonedaPrincipal\":\"USD\",\"IdMonedaSecundaria\":\"CLP\",\"PrecioVencimiento\":0,\"MontoOperacionOriginal\":1000.00000000,\"MontoOperacionNuevo\":1000,\"MontoInicialOriginal\":null,\"MontoInicialNuevo\":650000,\"MontoFinalOriginal\":null,\"MontoFinalNuevo\":0,\"FechaOperacion\":\"2018-10-09 00:00:00.000\",\"TCCierreOriginal\":680.00000000,\"TCCierreNuevo\":680,\"ParidadCierreOriginal\":0E-8,\"ParidadCierreNuevo\":0,\"PlazoOriginal\":1,\"PlazoNuevo\":1,\"FechaVencimientoOriginal\":\"2018-10-10 00:00:00.000\",\"FechaVencimientoNuevo\":\"2018-10-10 00:00:00.000\",\"FechaPagoOriginal\":\"2018-10-10 00:00:00.000\",\"FechaPagoNuevo\":\"2018-10-10 00:00:00.000\",\"IdModalidadLiquidacionOriginal\":6,\"IdModalidadLiquidacionNuevo\":7,\"IdReferenciaMercado\":0,\"IdPlaza\":2931,\"TCCostoOriginal\":650.00000000,\"TCCostoNuevo\":650,\"PuntosTasasForwardOriginal\":\"S\",\"PuntosTasasForwardNuevo\":\"S\",\"PuntosForwardOriginal\":0E-8,\"PuntosForwardNuevo\":0,\"TCForwardOriginal\":650.00000000,\"TCForwardNuevo\":650,\"TasasMonedaPrincipalOriginal\":0E-8,\"TasasMonedaPrincipalNuevo\":0,\"TasasMonedaSecundariaOriginal\":0E-8,\"TasasMonedaSecundariaNuevo\":0,\"MontoDiferidoOriginal\":30000.00000000,\"MontoDiferidoNuevo\":30000,\"MontoMargenOriginal\":-30000.00000000,\"MontoMargenNuevo\":-30000,\"IdFixingOriginal\":37,\"IdFixingNuevo\":37,\"FechaFixingOriginal\":\"0001-01-01 00:00:00.000\",\"FechaFixingNuevo\":\"0001-01-01 00:00:00.000\",\"HoraFixingOriginal\":\"00:00:00\",\"HoraFixingNuevo\":\"00:00:00\",\"IdMesa\":2,\"IdSpotAsociado\":0,\"Observaciones\":\"\",\"IdEstado\":\"MOD\",\"TasaDescuento\":0,\"TipoCambioAnticipo\":0,\"MontoARecibirAnticipo\":0,\"TipoCambioObservado\":0,\"TipoAnticipo\":\"\"}]}";
			IngresarForwardCarteraEntrada Objin = gson.fromJson(jsonDesaIngFwd, IngresarForwardCarteraEntrada.class);
			
			IngresarForwardCartera demo = new IngresarForwardCartera();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				IngresarForwardCarteraSalida ObjOut = demo.logica(con, Objin);
 				//con.commit();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				//er.printStackTrace();	
				con.rollback();
			}
 			//con.commit();
			finally {
				con.close();
			}
	    }	
		System.out.println("== END ==");
	}
}