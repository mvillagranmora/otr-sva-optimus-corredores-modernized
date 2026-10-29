package cl.bcs.facturacion;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


import cl.bcs.facturacion.pojo.MovimientoComprobanteDetalle;

import cl.bcs.cuentaInversion.CrearCargoAbono;
import cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoEntrada;
import cl.bcs.cuentaInversion.CrearCargoAbono.CrearCargoAbonoSalida;
import cl.bcs.cuentaInversion.CrearCargoAbono.ObjCargoAbonoAGenerar;
import cl.bcs.facturacion.pojo.ComprobanteDetalle;
import cl.bcs.facturacion.CrearComprobante.CrearComprobante_Entrada;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;
import cl.bcs.tesoreria.ModificarComprobanteTesoreria;
import cl.bcs.tesoreria.ModificarComprobanteTesoreria.IngresoEgreso;
import cl.bcs.tesoreria.ModificarComprobanteTesoreria.Comprobante;
import cl.bcs.tesoreria.ModificarComprobanteTesoreria.ComprobanteFormaDePago;
import cl.bcs.tesoreria.ModificarComprobanteTesoreria.ComprobanteDetalles;
import cl.bcs.tesoreria.ModificarComprobanteTesoreria.IngresoEgresoFormaDePago;
import cl.bcs.tesoreria.ModificarComprobanteTesoreria.EntradaModificarComprobanteTesoreria;
import cl.bcs.tesoreria.ModificarComprobanteTesoreria.SalidaModificarComprobanteTesoreria;;





public class ProcesarGananciaCapital implements JavaCall{
	
		static final Logger logger = LogManager.getLogger(ProcesarGananciaCapital.class);
		private final String SQL_SELECT_MOVIMIENTO_COMPROBANTE_DETALLE 	= loadResourceToString("cl/bcs/facturacion/sql/ProcesarGananciaCapital_SelectMovimientoComprobanteDetalle.sql");
		private final String SQL_SELECT_PREPARA_UPDATE_MOVIMIENTO_COMPROBANTE_DETALLE 	= loadResourceToString("cl/bcs/facturacion/sql/ProcesarGananciaCapital_SelectPreparaDatos_UpdateMovimientoComprobanteDetalle.sql");
		private final String SQL_UPDATE_MOVIMIENTO_COMPROBANTE_DETALLE 	= loadResourceToString("cl/bcs/facturacion/sql/ProcesarGananciaCapital_UpdateMovimientoComprobanteDetalle.sql");
		private final String SQL_SELECT_PREPARA_UPDATE_MOVIMIENTO_COMPROBANTE_DETALLE_NOM 	= loadResourceToString("cl/bcs/facturacion/sql/ProcesarGananciaCapital_SelectPreparaDatos_UpdateMovimientoComprobanteDetalleNominal.sql");
		private final String SQL_UPDATE_MOVIMIENTO_COMPROBANTE_DETALLE_NOM 	= loadResourceToString("cl/bcs/facturacion/sql/ProcesarGananciaCapital_UpdateMovimientoComprobanteDetalleNominal.sql");
		private final String SQL_UPDATE_COMPROBANTE_DETALLE 			= loadResourceToString("cl/bcs/facturacion/sql/ProcesarGananciaCapital_UpdateComprobanteDetalle.sql");
		private final String SQL_SELECT_PREPARA_UPDATE_COMPROBANTE 	= loadResourceToString("cl/bcs/facturacion/sql/ProcesarGananciaCapital_SelectPreparaDatos_UpdateComprobante.sql");
		private final String SQL_UPDATE_COMPROBANTE 					= loadResourceToString("cl/bcs/facturacion/sql/ProcesarGananciaCapital_UpdateComprobante.sql");
		//cuenta inversion
		private final String SQL_SELECT_COMPROBANTE_CARGO 				= loadResourceToString("cl/bcs/facturacion/sql/ProcesarGananciaCapital_SelectComprobanteCargo.sql");
		//tesoreria
		private final String SQL_SELECT_TESORERIA_INGRESOEGRESO 			= loadResourceToString("cl/bcs/facturacion/sql/ProcesarGananciaCapital_SelectTesoreriaIngresoEgreso.sql");
		private final String SQL_SELECT_TESORERIA_INGRESOEGRESOFORMADEPAGO	= loadResourceToString("cl/bcs/facturacion/sql/ProcesarGananciaCapital_SelectTesoreriaIngresoEgresoFormaDePago.sql");
		private final String SQL_SELECT_TESORERIA_COMPROBANTE 				= loadResourceToString("cl/bcs/facturacion/sql/ProcesarGananciaCapital_SelectTesoreriaComprobante.sql");
		private final String SQL_SELECT_TESORERIA_COMPROBANTEDETALLE		= loadResourceToString("cl/bcs/facturacion/sql/ProcesarGananciaCapital_SelectTesoreriaComprobanteDetalle.sql");
		private final String SQL_SELECT_TESORERIA_COMPROBANTEFORMADEPAGO 	= loadResourceToString("cl/bcs/facturacion/sql/ProcesarGananciaCapital_SelectTesoreriaComprobanteFormaDePago.sql");
		
			
		
		//cuenta inversion NC
		private final String SQL_SELECT_COMPROBANTE_ABONO_NC 				= loadResourceToString("cl/bcs/facturacion/sql/ProcesarGananciaCapital_SelectComprobanteAbonoNC.sql");
		
			
	public static class EntradaProcesarGananciaCapital{
		public Long p_IdEmpresa;
		public List<MovimientoComprobanteDetalle> p_ComprobanteDetalle;
		
	
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaProcesarGananciaCapital {
		public List<MensajeSalida>		P_OUT_MENSAJES;
		public Long						P_OUT_ERRORESCONTADOR;
		public Long						P_OUT_ADVERTENCIASCONTADOR;
		
		public SalidaProcesarGananciaCapital() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class InfoMovimientoComprobanteDetalle_Proceso extends BcsPojo{
		public Long IdMovimientoComprobanteDetalle;
		public Long IdComprobante;
	}
	public static class InfoMovimientoComprobanteDetalle_Update extends BcsPojo{
		public Long IdTrabajo;
		public Long IdMovimientoComprobanteDetalle;
		public String ValorIndicador;
		public BigDecimal Monto;
		public BigDecimal MontoRetencionGananciaCap;
		public BigDecimal MayorMenorValor;
		public String CargoGananciaCap;
		public Long IdComprobante;
	}
	
	public static class InfoComprobante_Proceso extends BcsPojo{
		public Long IdComprobante;
	}
	
	public static class InfoComprobanteCargo_Proceso extends BcsPojo{
		/*
		public Long					NumeroCargoAbono;
		public String				IdTipoMovimiento;
		public String				IdConcepto;
		public String				Descripcion;
		public String				IdCondicionDeLiquidacion;
		public Long					IdOperacion; 
		public String				FechaOperacion; 
		public String				FechaLiquidacionOperacion; 
		public String				FechaVencimientoOperacion; 
		public BigDecimal			MontoOperacion; 
		public BigDecimal			TipoCambioOperacion; 
		public Long					IdContraparte; 
		public Long					IdComprobanteDetalle; 
		public Long					IdComprobanteFormaDePago; 
		public String				IdTipoMovimientoAsociado; 
		public Long					IdMovimientoAsociado; 
		
		public Long					IdPersonaPagoCobro; 
		public BigDecimal			Cantidad; 
		public String				EsIndicadorBilateral; 
		public String				EsCargoAbonoPorTraspaso; 
		public Long					IdPortafolioTraspaso; 
		public String				GeneroContraparteEnTesoreria; 
		public String				CertificadoAutomatico;
		*/
		
		
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
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaProcesarGananciaCapital jin = gson.fromJson(in.toString(), EntradaProcesarGananciaCapital.class);
		CrearComprobante_Entrada jin2 = gson.fromJson(in.toString(), CrearComprobante_Entrada.class);
		SalidaProcesarGananciaCapital out = logica(conn,jin,jin2);

		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	@Trace
	public  SalidaProcesarGananciaCapital logica(Connection con, EntradaProcesarGananciaCapital entrada, CrearComprobante_Entrada EntradaComprobante) throws Exception{
		logger.info("INICIO::ProcesarGananciaCapital");
		SalidaProcesarGananciaCapital out = new SalidaProcesarGananciaCapital();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, EntradaComprobante.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		SQL _sql = new SQL();
		
		//Identifica comprobantes para el calculo de mmvalor y retencion (Atributo No Institucional + tipo operacion)
		ArrayList<Long> IdsMovimientoComprobanteDetalle = new ArrayList<Long>();
		for (MovimientoComprobanteDetalle mov : entrada.p_ComprobanteDetalle) {
			if (!IdsMovimientoComprobanteDetalle.contains(mov.IdMovimientoComprobanteDetalle)) {
				IdsMovimientoComprobanteDetalle.add(mov.IdMovimientoComprobanteDetalle);
			}
		}
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdMovimientoComprobanteDetalle", IdsMovimientoComprobanteDetalle);
		/*Seleccion de ventas para la retencion*/
		List<InfoMovimientoComprobanteDetalle_Proceso> selMovimientoComprobanteDetalle = _sql.query(con, SQL_SELECT_MOVIMIENTO_COMPROBANTE_DETALLE, params, InfoMovimientoComprobanteDetalle_Proceso.class);

		if (selMovimientoComprobanteDetalle.size() != 0) {
			
			
			List<InfoMovimientoComprobanteDetalle_Update> selMovimientoComprobanteDetalleNomPaso = _sql.query(con, SQL_SELECT_PREPARA_UPDATE_MOVIMIENTO_COMPROBANTE_DETALLE_NOM, params, InfoMovimientoComprobanteDetalle_Update.class);
			if(selMovimientoComprobanteDetalleNomPaso.size() != 0) {
				
				_sql.batch(con, SQL_UPDATE_MOVIMIENTO_COMPROBANTE_DETALLE_NOM, selMovimientoComprobanteDetalle, InfoMovimientoComprobanteDetalle_Proceso.class);
			}
			List<InfoMovimientoComprobanteDetalle_Update> selMovimientoComprobanteDetallePaso = _sql.query(con, SQL_SELECT_PREPARA_UPDATE_MOVIMIENTO_COMPROBANTE_DETALLE, params, InfoMovimientoComprobanteDetalle_Update.class);
			if(selMovimientoComprobanteDetallePaso.size() != 0) {
				
				_sql.batch(con, SQL_UPDATE_MOVIMIENTO_COMPROBANTE_DETALLE, selMovimientoComprobanteDetalle, InfoMovimientoComprobanteDetalle_Proceso.class);
			}

			
			_sql.batch(con, SQL_UPDATE_COMPROBANTE_DETALLE, selMovimientoComprobanteDetalle, InfoMovimientoComprobanteDetalle_Proceso.class);
			
			List<InfoMovimientoComprobanteDetalle_Update> selComprobantePaso = _sql.query(con, SQL_SELECT_PREPARA_UPDATE_COMPROBANTE, params, InfoMovimientoComprobanteDetalle_Update.class);
			if(selComprobantePaso.size() != 0) {
				_sql.batch(con, SQL_UPDATE_COMPROBANTE, selComprobantePaso, InfoMovimientoComprobanteDetalle_Update.class);//SQL_SELECT_PREPARA_UPDATE_COMPROBANTE
			}
			
			
			//seleccionar comprobantes idonios para un cargo
			List<InfoComprobanteCargo_Proceso> selComprobantecargo = _sql.query(con, SQL_SELECT_COMPROBANTE_CARGO, params, InfoComprobanteCargo_Proceso.class);
			//CUENTA INVERSION
			if (selComprobantecargo.size() != 0) {
				CrearCargoAbonoSalida salidaCrearCargoAbono = mueveCuentaInversion(con, selComprobantecargo,  EntradaComprobante, fechaSistema,"MOVCAR");
				
				//TESORERIA
				ArrayList<Long> IdsComprobante = new ArrayList<Long>();
				for (InfoComprobanteCargo_Proceso mov : selComprobantecargo) {
					if (!IdsComprobante.contains(mov.IdComprobante)) {
						IdsComprobante.add(mov.IdComprobante);
					}
				}
				
				HashMap<String,Object> parIdComprobante = new HashMap<String,Object>();
				parIdComprobante.put("IdComprobante", IdsComprobante);
				
				List<IngresoEgreso> selIngresoEgreso= _sql.query(con, SQL_SELECT_TESORERIA_INGRESOEGRESO, parIdComprobante, IngresoEgreso.class);
				if (selIngresoEgreso.size() != 0) {
				
					List<IngresoEgresoFormaDePago> selIngresoEgresoFormaDePago= _sql.query(con, SQL_SELECT_TESORERIA_INGRESOEGRESOFORMADEPAGO, parIdComprobante, IngresoEgresoFormaDePago.class);
					List<Comprobante> selComprobante= _sql.query(con, SQL_SELECT_TESORERIA_COMPROBANTE, parIdComprobante, Comprobante.class);
					List<ComprobanteDetalles> selComprobanteDetalle= _sql.query(con, SQL_SELECT_TESORERIA_COMPROBANTEDETALLE, parIdComprobante, ComprobanteDetalles.class);
					List<ComprobanteFormaDePago> selComprobanteFormaDePago= _sql.query(con, SQL_SELECT_TESORERIA_COMPROBANTEFORMADEPAGO, parIdComprobante, ComprobanteFormaDePago.class);
					
					EntradaModificarComprobanteTesoreria entradaModificarMovimiento = new EntradaModificarComprobanteTesoreria();
					SalidaModificarComprobanteTesoreria salidaModificarMovimiento = new SalidaModificarComprobanteTesoreria();
					ModificarComprobanteTesoreria  modificarMovimiento= new 	ModificarComprobanteTesoreria();
					entradaModificarMovimiento.p_IngresoEgreso=selIngresoEgreso;
					entradaModificarMovimiento.p_IngresoEgresoFormaDePago=selIngresoEgresoFormaDePago;
					entradaModificarMovimiento.p_Comprobante=selComprobante;
					entradaModificarMovimiento.p_ComprobanteDetalle=selComprobanteDetalle;
					entradaModificarMovimiento.p_ComprobanteFormaDePago=selComprobanteFormaDePago;
					entradaModificarMovimiento.p_IdEmpresa=EntradaComprobante.p_IdEmpresa;
					entradaModificarMovimiento.p_IdUsuario = EntradaComprobante.p_IdUsuario;
					entradaModificarMovimiento.p_IdModulo = EntradaComprobante.p_IdModulo;
					
					salidaModificarMovimiento = modificarMovimiento.logica(con, entradaModificarMovimiento);
					
				};
				
			};
			
		};
		/*Seleccion de Notas de Credito para la reversar cargos IGK*/
		List<InfoComprobanteCargo_Proceso> selComprobanteabono = _sql.query(con, SQL_SELECT_COMPROBANTE_ABONO_NC, params, InfoComprobanteCargo_Proceso.class);
		//CUENTA INVERSION
		if (selComprobanteabono.size() != 0) {
			CrearCargoAbonoSalida salidaCrearCargoAbono = mueveCuentaInversion(con, selComprobanteabono,  EntradaComprobante, fechaSistema,"MOVABN");
			
		}	
		
		logger.info("FIN::ProcesarGananciaCapital");
		return out;
	}
	

	private CrearCargoAbonoSalida mueveCuentaInversion(Connection con, List<InfoComprobanteCargo_Proceso> comprobantes, CrearComprobante_Entrada EntradaComprobante,String fechaSistema, String Tipo) throws Exception{
		logger.trace("INICIO::CertificarSolicitudAporteRescate.mueveCuentaInversion");
		CrearCargoAbonoSalida salidaCrearCargoAbono = new CrearCargoAbonoSalida();
		salidaCrearCargoAbono.P_OUT_ERRORESCONTADOR = 0L;
		salidaCrearCargoAbono.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		CrearCargoAbonoEntrada entradaCrearCargoAbono = new CrearCargoAbonoEntrada();
		CrearCargoAbono crearCargoAbono = new CrearCargoAbono();
		Long i = 0L;
		HashMap<Long,Long> ids = new HashMap<Long,Long>();

		for(InfoComprobanteCargo_Proceso sol : comprobantes){
		

					ObjCargoAbonoAGenerar obj = new ObjCargoAbonoAGenerar();
					//CARGO EN MONEDA DEL FONDO NORMALES Y DIRERIDOS CUENTA INVERSION('APONORM','APOREINV','APOOVER','APORESPRO','APODIV')
					obj.NumeroCargoAbono = i;
					obj.IdPortafolio = sol.IdPortafolio;
					obj.IdMoneda = sol.IdMoneda;
					obj.IdTipoMovimiento = Tipo;// "MOVCAR";
					obj.Monto = sol.MontoRetencionGananciaCap;
					obj.IdMonedaOperacion = sol.IdMonedaComprobante;
					obj.MontoOperacion = sol.MontoComprobanteFinal;
					if (Tipo=="MOVCAR"){obj.IdConcepto ="IGKCAR";}else {obj.IdConcepto ="IGKABN";} ;
					if (Tipo=="MOVCAR"){obj.Descripcion = "CARGO_IGK";}else {obj.Descripcion = "ABONO_IGK";} ;
					obj.IdCondicionDeLiquidacion = "";
					obj.FechaCondicionDeLiquidacion =Fecha.toStringDate(sol.FechaLiquidacion);
					obj.IdEjecutivo = sol.IdEjecutivo;
					obj.IdSucursal = 0L;
					obj.IdTipoProducto = sol.IdTipoProducto;
					obj.IdTipoOperacion = sol.IdTipoOperacion;
					obj.IdOperacion = sol.IdComprobante;
					obj.FechaOperacion = Fecha.toStringDate(sol.FechaComprobante);
					obj.FechaLiquidacionOperacion = Fecha.toStringDate(sol.FechaLiquidacion);
					obj.TipoCambioOperacion = new BigDecimal("0");
					obj.IdTipoComprobante = sol.IdTipoComprobante;
					obj.IdComprobante = sol.IdComprobante;
					obj.FolioComprobante = sol.FolioComprobante.toString();
					//obj.IdComprobanteDetalle = sol.IdComprobanteDetalle;
					obj.IdTipoMovimientoAsociado = "";
					obj.IdMovimientoAsociado = 0L;
					obj.FechaComprobante = Fecha.toStringDate(sol.FechaComprobante);
					obj.IdInstrumento = sol.IdInstrumento;
					obj.IdTipoInstrumento = sol.IdTipoInstrumento;
					obj.Nemo = sol.Nemo;
					obj.EsCargoAbonoPorTraspaso = "N";
					obj.GeneroContraparteEnTesoreria = "N";
					obj.CertificadoAutomatico = "S";
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
		logger.trace("FIN::ProcesarGananciaCapital.mueveCuentaInversion");
		return salidaCrearCargoAbono;
	}
	

	
	
}
