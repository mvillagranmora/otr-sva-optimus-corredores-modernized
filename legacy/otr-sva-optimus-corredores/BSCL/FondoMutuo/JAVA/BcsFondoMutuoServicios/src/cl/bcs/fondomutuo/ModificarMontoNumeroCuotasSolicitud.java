package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.CalcularMontoNumeroCuota.EntradaCalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.CalcularMontoNumeroCuota.SalidaCalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarMontoNumeroCuotasSolicitud implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(ModificarMontoNumeroCuotasSolicitud.class);
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	private final String SQL_UPDATE_SOLICITUD_APORTE = loadResourceToString("cl/bcs/fondomutuo/sql/ModificarMontoNumeroCuotasSolicitud_UpdateSolicitudAporte.sql");
	private final String SQL_UPDATE_SOLICITUD_RESCATE = loadResourceToString("cl/bcs/fondomutuo/sql/ModificarMontoNumeroCuotasSolicitud_UpdateSolicitudRescate.sql");
	private final String SQL_UPDATE_SOLICITUD_REINVERSION = loadResourceToString("cl/bcs/fondomutuo/sql/ModificarMontoNumeroCuotasSolicitud_UpdateSolicitudReinversion.sql");
	private final String SQL_SELECT_SOLICITUD = loadResourceToString("cl/bcs/fondomutuo/sql/ModificarMontoNumeroCuotasSolicitud_SelectSolicitud.sql");
	private final String SQL_SELECT_SOLICITUD_REINVERSION = loadResourceToString("cl/bcs/fondomutuo/sql/ModificarMontoNumeroCuotasSolicitud_SelectSolicitudReinversion.sql");
	
	
	public static class EntradaModificarMontoNumeroCuotasSolicitud{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		
		public List<InSolicitud> p_Solicitudes;
		
		public EntradaModificarMontoNumeroCuotasSolicitud() {
			this.p_Solicitudes = new ArrayList<InSolicitud>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaModificarMontoNumeroCuotasSolicitud{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaModificarMontoNumeroCuotasSolicitud() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InSolicitud{
		public Long IdInterno;
		public Long IdSolicitudAporteRescate;
		public String CalculoPorMontoCuota;
		public BigDecimal MontoPrincipal;
		public BigDecimal MontoSecundario;
		public String IdMonedaPrincipal;
		public String IdMonedaSecundaria;
		public BigDecimal Cuota;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Solicitud{
		public Long IdSolicitudAporteRescate;
		public String CalculoPorMontoCuota;
		public String IdMoneda;
		public String IdMonedaFondo;
		public Date FechaCertificacion;
		public Date FechaValorCuota;
		public String IdSerieFondo;
		public String TipoSolicitud;
		public Long IdSolicitudAporteRescateRelacionado;
		public String IdTipoMovimientoFondoMutuo;
		public String IdMonedaPago;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class SolicitudActualizar{
		public Long IdInterno;
		public Long IdSolicitudAporteRescate;
		public BigDecimal CostoMonedaLocal;
		public BigDecimal Monto;
		public BigDecimal ValorCuota;
		public BigDecimal NumeroCuotas;
		public BigDecimal ParidadFechaInversion;
		public BigDecimal CostoUF;
		public BigDecimal ValorUFFechaInversion;
		public String IdMonedaSistema;
		public BigDecimal MontoMonedaSistema;
		public String TipoSolicitud;
		public Long IdSolicitudAporteRescateRelacionado;
		public String IdTipoMovimientoFondoMutuo;
		public String IdMoneda;
		public String IdMonedaPago;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SolicitudReinversion{
		public Long IdSolicitudAporteRescate;
		public BigDecimal MontoMonedaSistema;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaModificarMontoNumeroCuotasSolicitud jin = gson.fromJson(in.toString(), EntradaModificarMontoNumeroCuotasSolicitud.class);
		SalidaModificarMontoNumeroCuotasSolicitud out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaModificarMontoNumeroCuotasSolicitud logica (Connection con, EntradaModificarMontoNumeroCuotasSolicitud entrada) throws Exception {
		logger.info("INICIO::ModificarMontoNumeroCuotasSolicitud");
		SalidaModificarMontoNumeroCuotasSolicitud out = new SalidaModificarMontoNumeroCuotasSolicitud();
		
		SQL sql=new SQL();
		
		ArrayList<Long> correlativos = new ArrayList<Long>();
		for (InSolicitud mov: entrada.p_Solicitudes) {
			if(mov.IdInterno.longValue() == 0L){
				out.P_OUT_ERRORESCONTADOR = 1L;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ID_INTERNO_NO_CERO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				return out;
			}
			if (correlativos.contains(mov.IdInterno)) {
				out.P_OUT_ERRORESCONTADOR = 1L;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"CORRELATIVO_INCORRECTO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				return out;
			} else {
				correlativos.add(mov.IdInterno);
			}
		}
		
		String idMonedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		
		List<Long> idSolicitudes = entrada.p_Solicitudes.stream().map(solicitud-> { return solicitud.IdSolicitudAporteRescate; }).collect(Collectors.toList());
		params.put("IdSolicitudAporteRescate", idSolicitudes);
		
		List<Solicitud> solicitudes = sql.query(con, SQL_SELECT_SOLICITUD, params, Solicitud.class);
		HashMap<Long,Object> hSolicitud = Utils.List2Hash(solicitudes, "IdSolicitudAporteRescate");
		
		CalcularMontoNumeroCuota calculo = new CalcularMontoNumeroCuota();
		EntradaCalcularMontoNumeroCuota entradaCalculo = new EntradaCalcularMontoNumeroCuota();
		entradaCalculo.p_IdEmpresa = entrada.p_IdEmpresa;
		
		entradaCalculo.p_Series = entrada.p_Solicitudes.stream().map(inSolicitud -> {
			Solicitud solicitud = (Solicitud) hSolicitud.get(inSolicitud.IdSolicitudAporteRescate);
			CalcularMontoNumeroCuota.Serie serie = new CalcularMontoNumeroCuota.Serie();
			serie.IdInterno = inSolicitud.IdInterno;
			serie.Fecha =  Fecha.stringToSqlDate(solicitud.FechaValorCuota);
			serie.IdSerieFondo = solicitud.IdSerieFondo;
		 	serie.CalculoPorMontoCuota = solicitud.CalculoPorMontoCuota;
		 	if( _c.CalculoPorMonto.equals(inSolicitud.CalculoPorMontoCuota)) {
		 		serie.NumeroCuotas = BigDecimal.ZERO;
		 		if(solicitud.IdMoneda.equals(inSolicitud.IdMonedaPrincipal)) {
		 			serie.Monto = inSolicitud.MontoPrincipal;
		 		}else if(solicitud.IdMoneda.equals(inSolicitud.IdMonedaSecundaria)) {
		 			serie.Monto = inSolicitud.MontoSecundario;
		 		}
		 	}else {
		 		serie.Monto = BigDecimal.ZERO;
		 		serie.NumeroCuotas = inSolicitud.Cuota;
		 	}
		 	serie.IdMonedaFondo = solicitud.IdMonedaFondo;
		 	serie.IdMonedaInversion = solicitud.IdMoneda;
		 	serie.TipoSolicitud =  solicitud.TipoSolicitud;
		 	serie.TipoFactorAjuste = _c.UF;
		 	serie.IdEmpresa = entrada.p_IdEmpresa;
		 	return serie;
		}).collect(Collectors.toList());
		
		SalidaCalcularMontoNumeroCuota salidaCalculo = calculo.logica(con, entradaCalculo);
		HashMap<Long,Object> hSalidaCalculo = Utils.List2Hash(salidaCalculo.P_OUT_RESULTADO, "IdInterno");
		List<SolicitudActualizar> solicitudesActualizar = entrada.p_Solicitudes.parallelStream().map(inSolicitud -> {
			Solicitud solicitud = (Solicitud) hSolicitud.get(inSolicitud.IdSolicitudAporteRescate);
			CalcularMontoNumeroCuota.Resultado resultado = (CalcularMontoNumeroCuota.Resultado) hSalidaCalculo.get(inSolicitud.IdInterno);
			SolicitudActualizar solicitudActualizar = new SolicitudActualizar();
			if(resultado != null) {
				solicitudActualizar.IdInterno = inSolicitud.IdInterno;
				solicitudActualizar.IdSolicitudAporteRescate = inSolicitud.IdSolicitudAporteRescate;
				solicitudActualizar.TipoSolicitud = solicitud.TipoSolicitud;
				solicitudActualizar.IdSolicitudAporteRescateRelacionado = solicitud.IdSolicitudAporteRescateRelacionado;
				solicitudActualizar.IdTipoMovimientoFondoMutuo = solicitud.IdTipoMovimientoFondoMutuo;
				solicitudActualizar.IdMoneda = solicitud.IdMoneda;
				solicitudActualizar.IdMonedaPago = solicitud.IdMonedaPago;
				solicitudActualizar.IdMonedaSistema = resultado.IdMonedaSistema;
				
				if(_c.TipoSolicitudAporte.equals(solicitudActualizar.TipoSolicitud)) {
					solicitudActualizar.CostoMonedaLocal = resultado.MontoDestino;
					solicitudActualizar.Monto = resultado.Monto;
					solicitudActualizar.NumeroCuotas = resultado.NumeroCuotas;
					solicitudActualizar.ParidadFechaInversion = resultado.ValorParidad;
					solicitudActualizar.CostoUF = resultado.CostoEnFactorAjuste;
					solicitudActualizar.ValorUFFechaInversion = resultado.ValorFactorAjuste;
					solicitudActualizar.ValorCuota = resultado.ValorCuota;
				}
				
				if(idMonedaSistema.equals(inSolicitud.IdMonedaPrincipal)) {
					solicitudActualizar.MontoMonedaSistema = inSolicitud.MontoPrincipal;
				} else if (idMonedaSistema.equals(inSolicitud.IdMonedaSecundaria)) {
					solicitudActualizar.MontoMonedaSistema = inSolicitud.MontoSecundario;
				} else {
					solicitudActualizar.MontoMonedaSistema = resultado.MontoMonedaSistema;
				}
				
			}
			return solicitudActualizar;
		}).collect(Collectors.toList());
		
		HashMap<Long,Object> hSolicitudesActualizar = Utils.List2Hash(solicitudesActualizar, "IdInterno");
		
		List<SolicitudActualizar> solicitudesAporteActualizar = solicitudesActualizar.parallelStream().filter(solicitud->_c.TipoSolicitudAporte.equals(solicitud.TipoSolicitud)).collect(Collectors.toList());
		List<SolicitudActualizar> solicitudesRescateActualizar = solicitudesActualizar.parallelStream().filter(solicitud->_c.TipoSolicitudRescate.equals(solicitud.TipoSolicitud)).collect(Collectors.toList());
		
		sql.batch(con, SQL_UPDATE_SOLICITUD_APORTE, solicitudesAporteActualizar, SolicitudActualizar.class);
		sql.batch(con, SQL_UPDATE_SOLICITUD_RESCATE, solicitudesRescateActualizar, SolicitudActualizar.class);
		
		List<Long> idsSolicitudesReinversion = solicitudesAporteActualizar.parallelStream()
				.filter(solicitud->_c.AporteReinversion.equals(solicitud.IdTipoMovimientoFondoMutuo) && solicitud.IdSolicitudAporteRescateRelacionado > 0 && !solicitud.IdMonedaSistema.equals(solicitud.IdMonedaPago))
				.map(solicitud->solicitud.IdSolicitudAporteRescateRelacionado)
				.distinct()
				.collect(Collectors.toList());
		
		if(idsSolicitudesReinversion.size() > 0) {
			params.clear();
			params.put("IdSolicitudAporteRescateRelacionado", idsSolicitudesReinversion);
			List<SolicitudReinversion> solicitudesActualizarReinversion = sql.query(con, SQL_SELECT_SOLICITUD_REINVERSION, params, SolicitudReinversion.class);
			sql.batch(con, SQL_UPDATE_SOLICITUD_REINVERSION, solicitudesActualizarReinversion, SolicitudReinversion.class);
		}
		
		
		for(InSolicitud inSolicitud : entrada.p_Solicitudes) {
			SolicitudActualizar solicitudActualizar = (SolicitudActualizar) hSolicitudesActualizar.get(inSolicitud.IdInterno);
			HashMap<String,String> parametrosMensaje = new HashMap<String,String>();
			parametrosMensaje.put("IdSolicitudAporteRescate", String.valueOf(inSolicitud.IdSolicitudAporteRescate));
			if(solicitudActualizar == null) {
				out.P_OUT_ERRORESCONTADOR = 1L;
				out.P_OUT_MENSAJES.add(new MensajeSalida(inSolicitud.IdSolicitudAporteRescate,MensajeSalida.MENSAJE_ERROR,"NO_SE_ACTUALIZA_MONTO_SOLICITUD",parametrosMensaje,con,entrada.p_IdEmpresa));
			}else {
				out.P_OUT_MENSAJES.add(new MensajeSalida(inSolicitud.IdSolicitudAporteRescate,MensajeSalida.MENSAJE_OK,"MONTO_SOLICITUD_ACTUALIZADA",parametrosMensaje,con,entrada.p_IdEmpresa));
			}
		}
		
		logger.info("FIN::ModificarMontoNumeroCuotasSolicitud");
		
		return out;
	}
	
	
}
