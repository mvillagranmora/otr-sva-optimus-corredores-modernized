package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.CalcularMontoNumeroCuota.EntradaCalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.CalcularMontoNumeroCuota.SalidaCalcularMontoNumeroCuota;
import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.fondomutuo.pojo.SerieFondoSolicitudes;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarComisionSolicitudPorPerfeccionamiento implements JavaCall{
	
	private final Log log = new Log("FM.ModificarComisionSolicitudPorPerfeccionamiento");
	
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	private final String SQL_ACTUALIZAR_SOLICITUD = loadResourceToString("cl/bcs/fondomutuo/sql/ModificarComisionSolicitudPorPerfeccionamiento_ActualizarSolicitud.sql");
	private final String SQL_SELECT_SOLICITUDES = loadResourceToString("cl/bcs/fondomutuo/sql/ModificarComisionSolicitudPorPerfeccionamiento_SelectSolicitud.sql");
	private final String SQL_SELECT_SERIE_FONDO = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectSerieFondo.sql");
	
	public static class EntradaModificarComisionSolicitudPorPerfeccionamiento{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_SimularOperacion;
		public String p_UsaReglasNegocio;
		
		List<InSolicitudes> p_Solicitudes;
		
		public EntradaModificarComisionSolicitudPorPerfeccionamiento(){
			this.p_Solicitudes = new ArrayList<InSolicitudes>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InSolicitudes{
		public Long IdInterno;
		public Long IdSolicitudAporteRescate;
		public Long IdCliente;
    	public Long IdPortafolio;
    	public String IdSerieFondo;
    	public String CalculoPorMontoCuota;
    	public String TipoSolicitud;
    	public String IdMoneda;
    	public BigDecimal Monto;
    	public BigDecimal NumeroCuotas;
    	public BigDecimal MontoComision;
    	
    	public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Solicitud{
		public Long IdSolicitudAporteRescate;
		public Long IdCliente;
		public Long IdPortafolio;
		public String IdMoneda;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class OutSolicitudes{
		public Long IdInterno;
		public Long IdSolicitudAporteRescate;
		public Long IdCliente;
    	public Long IdPortafolio;
    	public String IdSerieFondo;
    	public String CalculoPorMontoCuota;
    	public String TipoSolicitud;
    	public String IdMoneda;
    	public BigDecimal Monto;
    	public BigDecimal NumeroCuotas;
    	public BigDecimal MontoComision;
    	
    	public String toString() {
			return Utils.printJson(this, false);
		}
    	
	}
	
	public static class SalidaModificarComisionSolicitudPorPerfeccionamiento{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		public List <OutSolicitudes> P_OUT_RESULTADO;
		
		
		public SalidaModificarComisionSolicitudPorPerfeccionamiento(){
			this.P_OUT_RESULTADO = new ArrayList<OutSolicitudes>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaModificarComisionSolicitudPorPerfeccionamiento jin = gson.fromJson(in.toString(), EntradaModificarComisionSolicitudPorPerfeccionamiento.class);
		SalidaModificarComisionSolicitudPorPerfeccionamiento out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaModificarComisionSolicitudPorPerfeccionamiento logica(Connection con, EntradaModificarComisionSolicitudPorPerfeccionamiento entrada) throws Exception{
		log.write("INICIO ProcesarCargaSolicitudesAporteRescate");
		SalidaModificarComisionSolicitudPorPerfeccionamiento out = new SalidaModificarComisionSolicitudPorPerfeccionamiento();
		SQL sql=new SQL();
		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo);
		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
			return out;
		}
		
		if(entrada.p_Solicitudes.size() == 0){
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"NO_SE_INGRESARON_SOLICITUDES",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
			return out;
		}
		
		ArrayList<Long> correlativos = new ArrayList<Long>();
		ArrayList<Long> ids = new ArrayList<Long>();
		ArrayList<String> series = new ArrayList<String>();
		for (InSolicitudes mov: entrada.p_Solicitudes) {
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
				ids.add(mov.IdSolicitudAporteRescate);
				if(!series.contains(mov.IdSerieFondo)) series.add(mov.IdSerieFondo);
			}
		}
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdSolicitudAporteRescate", ids);
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		
		List<Solicitud> solicitudesExistentes = sql.query(con, SQL_SELECT_SOLICITUDES, params, Solicitud.class);
		params.clear();
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("IdSerieFondo", series);
		List<SerieFondoSolicitudes> seriesSolicitudes = sql.query(con, SQL_SELECT_SERIE_FONDO, params, SerieFondoSolicitudes.class);
		params.clear();
		
		HashMap<Long,Object> hSolicitudesExistentes = Utils.List2Hash(solicitudesExistentes, "IdSolicitudAporteRescate");
		HashMap<String,Object> hseries = Utils.List2Hash(seriesSolicitudes, "IdSerieFondo");
		
		CalcularMontoNumeroCuota calculo = new CalcularMontoNumeroCuota();
		EntradaCalcularMontoNumeroCuota entradaCalculo = new EntradaCalcularMontoNumeroCuota();
		entradaCalculo.p_IdEmpresa = entrada.p_IdEmpresa;
		
		for(InSolicitudes sol : entrada.p_Solicitudes){
			CalcularMontoNumeroCuota.Serie serie = new CalcularMontoNumeroCuota.Serie();
			SerieFondoSolicitudes serieFondo = (SerieFondoSolicitudes) hseries.get(sol.IdSerieFondo);
			serie.IdInterno = sol.IdInterno;
			serie.Fecha = fechaSistema;
			serie.IdSerieFondo = sol.IdSerieFondo;
		 	serie.CalculoPorMontoCuota = sol.CalculoPorMontoCuota;
		 	serie.Monto = sol.Monto;
		 	serie.NumeroCuotas = sol.NumeroCuotas;
		 	serie.IdMonedaFondo = serieFondo != null ? serieFondo.IdMoneda : "";
		 	serie.IdMonedaInversion = sol.IdMoneda;
		 	serie.TipoSolicitud =  _c.TipoSolicitudAporte;
		 	serie.TipoFactorAjuste = _c.UF;
		 	serie.IdEmpresa = entrada.p_IdEmpresa;
		 	entradaCalculo.p_Series.add(serie);
		}
		
		SalidaCalcularMontoNumeroCuota salidaCalculo = calculo.logica(con, entradaCalculo);
		
		HashMap<Long,Object> valoresCuota = Utils.List2Hash(salidaCalculo.P_OUT_RESULTADO, "IdInterno");
		
		List<OutSolicitudes> solicitudesModificar = new ArrayList<OutSolicitudes>();  
		for(InSolicitudes sol : entrada.p_Solicitudes){
			Solicitud solicitud = (Solicitud) hSolicitudesExistentes.get(sol.IdSolicitudAporteRescate);
			CalcularMontoNumeroCuota.Resultado vc = (CalcularMontoNumeroCuota.Resultado) valoresCuota.get(sol.IdInterno);
			if(solicitud == null){
				out.P_OUT_ERRORESCONTADOR += 1L;
				out.P_OUT_MENSAJES.add(new MensajeSalida(sol.IdInterno,MensajeSalida.MENSAJE_ERROR,"SOLICITUD_NO_EXISTE",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
			}
			if(solicitud != null){
				if(solicitud.IdCliente.longValue() != sol.IdCliente.longValue()){
					out.P_OUT_ERRORESCONTADOR += 1L;
					out.P_OUT_MENSAJES.add(new MensajeSalida(sol.IdInterno,MensajeSalida.MENSAJE_ERROR,"CLIENTE_INVALIDO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				}
				if(solicitud.IdPortafolio.longValue() != sol.IdPortafolio.longValue()){
					out.P_OUT_ERRORESCONTADOR += 1L;
					out.P_OUT_MENSAJES.add(new MensajeSalida(sol.IdInterno,MensajeSalida.MENSAJE_ERROR,"PORTAFOLIO_INVALIDO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				}
				if(!solicitud.IdMoneda.equals(sol.IdMoneda)){
					out.P_OUT_ERRORESCONTADOR += 1L;
					out.P_OUT_MENSAJES.add(new MensajeSalida(sol.IdInterno,MensajeSalida.MENSAJE_ERROR,"MONEDA_INVALIDO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				}
			}
			if(vc == null){
				out.P_OUT_ERRORESCONTADOR += 1L;
				out.P_OUT_MENSAJES.add(new MensajeSalida(sol.IdInterno,MensajeSalida.MENSAJE_ERROR,"MONTO_INVALIDO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				out.P_OUT_MENSAJES.add(new MensajeSalida(sol.IdInterno,MensajeSalida.MENSAJE_ERROR,"NUMERO_CUOTAS_INVALIDO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
			}else{
				if(vc.Monto.compareTo(sol.Monto) != 0){
					out.P_OUT_ERRORESCONTADOR += 1L;
					out.P_OUT_MENSAJES.add(new MensajeSalida(sol.IdInterno,MensajeSalida.MENSAJE_ERROR,"MONTO_INVALIDO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				}
				if(vc.NumeroCuotas.compareTo(sol.NumeroCuotas) != 0){
					out.P_OUT_ERRORESCONTADOR += 1L;
					out.P_OUT_MENSAJES.add(new MensajeSalida(sol.IdInterno,MensajeSalida.MENSAJE_ERROR,"NUMERO_CUOTAS_INVALIDO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
				}
			}
			OutSolicitudes solOut = new OutSolicitudes();
			solOut = Utils.copyPojo(sol, solOut);
			solicitudesModificar.add(solOut);
		}
		
		sql.batch(con, SQL_ACTUALIZAR_SOLICITUD, solicitudesModificar, OutSolicitudes.class);
		
		log.write("FIN SalidaModificarComisionSolicitudPorPerfeccionamiento");
		return out;
	}
}
