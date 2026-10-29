package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ActualizarCostoUF implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(ActualizarCostoUF.class);
	
	private final String SQL_SELECT_SOLICITUDES_RESCATES = loadResourceToString("cl/bcs/fondomutuo/sql/ActualizarCostoUF_SelectSolicitudesRescates.sql");
	private final String SQL_SELECT_SOLICITUDES_APORTES = loadResourceToString("cl/bcs/fondomutuo/sql/ActualizarCostoUF_SelectSolicitudesAportes.sql");
	private final String SQL_SELECT_SOLICITUDES_REINVERSIONES = loadResourceToString("cl/bcs/fondomutuo/sql/ActualizarCostoUF_SelectSolicitudesReinversiones.sql");
	private final String SQL_SELECT_CARTERAS = loadResourceToString("cl/bcs/fondomutuo/sql/ActualizarCostoUF_SelectCarteras.sql");
	private final String SQL_UPDATE_SOLICITUDES = loadResourceToString("cl/bcs/fondomutuo/sql/ActualizarCostoUF_UpdateSolicitudes.sql");
	private final String SQL_UPDATE_CARTERAS = loadResourceToString("cl/bcs/fondomutuo/sql/ActualizarCostoUF_UpdateCarteras.sql");
	private final String SQL_UPDATE_REGISTRO_MLT = loadResourceToString("cl/bcs/fondomutuo/sql/ActualizarCostoUF_UpdateRegistroMLT.sql");
	
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	public static class EntradaActualizarCostoUF{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_FechaDesde;
		public String p_FechaHasta;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaActualizarCostoUF{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaActualizarCostoUF() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SolicitudAporteRescate{
		public Long IdSolicitudAporteRescate;
		public String TipoSolicitud;
		public String IdTipoMovimientoFondoMutuo;
		public BigDecimal CostoUF;
		public BigDecimal NumeroCuotas;
		public BigDecimal MontoMonedaSistema;
		public BigDecimal ValorUFFechaInversion;
		public Long IdAgrupador;
		public String IdSerieFondo;
		public String IdRegimenTributario;
		
		public SolicitudAporteRescate() {
			this.CostoUF = BigDecimal.ZERO;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Cartera{
		public Long IdCartera;
		public Long FolioCartera;
		public Long IdOperacion;
		public Long EGRIdOperacion;
		public BigDecimal Cantidad;
		public BigDecimal EGRCantidad;
		public BigDecimal CostoInversionEnFactorAjuste;
		public BigDecimal ValorFactorAjusteaFechaCurse;
		public BigDecimal INGCostoInversionEnFactorAjuste;
		public BigDecimal INGValorFactorAjusteaFechaCurse;
		public BigDecimal EGRValorFactorAjusteaFechaCurse;
		public BigDecimal EGRCostoInversionEnFactorAjuste;
		public String TipoSolicitud;
		public String IdTipoMovimientoFondoMutuo;
		public Cartera(){
			EGRCostoInversionEnFactorAjuste = BigDecimal.ZERO;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Reinversion{
		public Long IdSolicitudAporteRescate;
		public String IdSerieFondoDestino;
		public BigDecimal Porcentaje;
		public String IdRegimenTributario;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaActualizarCostoUF jin = gson.fromJson(in.toString(), EntradaActualizarCostoUF.class);
		SalidaActualizarCostoUF out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaActualizarCostoUF logica (Connection con, EntradaActualizarCostoUF entrada) throws Exception {
		logger.debug("INICIO::ActualizarCostoUF");
		SalidaActualizarCostoUF out = new SalidaActualizarCostoUF();
		SQL sql=new SQL();
		
		System.out.println("FECHA DESDE::" + entrada.p_FechaDesde);
		System.out.println("FECHA HASTA::" + entrada.p_FechaHasta);
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("FechaDesde", entrada.p_FechaDesde);
		params.put("FechaHasta", entrada.p_FechaHasta);
		
		List<SolicitudAporteRescate> solicitudesRescates = sql.query(con, SQL_SELECT_SOLICITUDES_RESCATES, params, SolicitudAporteRescate.class);
		List<SolicitudAporteRescate> solicitudesAportes = sql.query(con, SQL_SELECT_SOLICITUDES_APORTES, params, SolicitudAporteRescate.class);
		List<Reinversion> reinversiones = sql.query(con, SQL_SELECT_SOLICITUDES_REINVERSIONES, params, Reinversion.class);
		List<Cartera> carteras = sql.query(con, SQL_SELECT_CARTERAS, params, Cartera.class);
		
		Map<Long, List<Cartera>> hashCarterasRescates = carteras.parallelStream().filter(cartera->_c.TipoSolicitudRescate.equals(cartera.TipoSolicitud)).collect(Collectors.groupingBy(cartera->cartera.EGRIdOperacion));
		Map<Long, List<Cartera>> hashCarterasAportes = carteras.parallelStream().collect(Collectors.groupingBy(cartera->cartera.IdOperacion));
		Map<String, List<SolicitudAporteRescate>> hashAportes = solicitudesAportes.parallelStream().filter(solicitud->!solicitud.IdAgrupador.equals(0L)).collect(Collectors.groupingBy(solicitud->solicitud.IdAgrupador + "_" + solicitud.IdRegimenTributario));
		Map<String, List<Reinversion>> hashReinversiones = reinversiones.parallelStream().collect(Collectors.groupingBy(item->{
			return item.IdSolicitudAporteRescate + "_" + item.IdSerieFondoDestino + "_" + item.IdRegimenTributario;
		}));
		System.out.println("Rescates::" + solicitudesRescates.size());
		for(SolicitudAporteRescate solicitud : solicitudesRescates) {
			List<Cartera> carterasSolicitud = hashCarterasRescates.get(solicitud.IdSolicitudAporteRescate) != null ? hashCarterasRescates.get(solicitud.IdSolicitudAporteRescate) : new ArrayList<Cartera>();//carteras.parallelStream().filter(cartera -> cartera.EGRIdOperacion.equals(solicitud.IdSolicitudAporteRescate) && _c.TipoSolicitudRescate.equals(cartera.TipoSolicitud)).collect(Collectors.toList());
			carterasSolicitud.parallelStream().forEach(cartera -> {
				cartera.EGRCostoInversionEnFactorAjuste = cartera.EGRCantidad.divide(cartera.Cantidad, MathContext.DECIMAL128).multiply(cartera.CostoInversionEnFactorAjuste, MathContext.DECIMAL128);
			});
			List<Cartera> carterasReinversion = carterasSolicitud.parallelStream().filter(cartera -> _c.Reinversion.equals(cartera.IdTipoMovimientoFondoMutuo)).collect(Collectors.toList());
			carterasReinversion.stream().forEach(cartera->{
				List<SolicitudAporteRescate> solicitudesAportesReinversion = hashAportes.get(solicitud.IdAgrupador + "_" + solicitud.IdRegimenTributario) != null ? hashAportes.get(solicitud.IdAgrupador + "_" + solicitud.IdRegimenTributario) : new ArrayList<SolicitudAporteRescate>();//solicitudesAportes.parallelStream().filter(solicitudRescate -> solicitudRescate.IdAgrupador.equals(solicitud.IdAgrupador) && !solicitudRescate.IdAgrupador.equals(0L)).collect(Collectors.toList());
				solicitudesAportesReinversion.parallelStream().forEach(solicitudAporteReinversion->{
					String id = solicitud.IdSolicitudAporteRescate + "_" + solicitudAporteReinversion.IdSerieFondo + "_" + solicitudAporteReinversion.IdRegimenTributario;
					List<Reinversion> reinversion = hashReinversiones.get(id) != null ? hashReinversiones.get(id) : new ArrayList<Reinversion>();//reinversiones.parallelStream().filter(item -> item.IdSolicitudAporteRescate.equals(solicitud.IdSolicitudAporteRescate) && item.IdSerieFondoDestino.equals(solicitudAporteReinversion.IdSerieFondo) && item.IdRegimenTributario.equals(solicitudAporteReinversion.IdRegimenTributario)).collect(Collectors.toList());
					BigDecimal porcentaje = reinversion.size() == 0 ? new BigDecimal("100") : reinversion.get(0).Porcentaje;
					porcentaje = porcentaje.divide(new BigDecimal("100"),MathContext.DECIMAL128);
					solicitudAporteReinversion.CostoUF = solicitudAporteReinversion.CostoUF.add(cartera.EGRCostoInversionEnFactorAjuste.multiply(porcentaje, MathContext.DECIMAL128));
					List<Cartera> carterasAporte = hashCarterasAportes.get(solicitudAporteReinversion.IdSolicitudAporteRescate) != null ? hashCarterasAportes.get(solicitudAporteReinversion.IdSolicitudAporteRescate) : new ArrayList<Cartera>();//carteras.parallelStream().filter(car -> car.IdOperacion.equals(solicitudAporteReinversion.IdSolicitudAporteRescate)).collect(Collectors.toList());
					carterasAporte.parallelStream().forEach(carteraAporte->{
						carteraAporte.CostoInversionEnFactorAjuste = solicitudAporteReinversion.CostoUF;
					});
				});
			});
			BigDecimal carteraSum = carterasSolicitud.stream().map(cartera->cartera.EGRCostoInversionEnFactorAjuste).reduce(BigDecimal.ZERO, (subtotal, cartera)-> subtotal.add(cartera, MathContext.DECIMAL128));
			
			if(carteraSum != null && carteraSum.compareTo(BigDecimal.ZERO) != 0) {
				solicitud.CostoUF = carteraSum;
			}
		}
		System.out.println("Salida::Loop");
		sql.batch(con, SQL_UPDATE_SOLICITUDES, solicitudesRescates, SolicitudAporteRescate.class);
		sql.batch(con, SQL_UPDATE_SOLICITUDES, solicitudesAportes, SolicitudAporteRescate.class);
		sql.batch(con, SQL_UPDATE_CARTERAS, carteras, Cartera.class);
		
		if (entrada.p_FechaDesde=="1900-01-01" && entrada.p_FechaHasta=="1900-01-01"){
			sql.batch(con, SQL_UPDATE_REGISTRO_MLT, solicitudesAportes, SolicitudAporteRescate.class);
		}
		
		
		
		logger.debug("FIN::ActualizarCostoUF");
		
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getUAT2Connection()) {
			
			//entradaActualizarCostoUF.p_FechaDesde = "2010-01-01";
			//entradaActualizarCostoUF.p_FechaHasta = "2010-12-31";
			
			//entradaActualizarCostoUF.p_FechaDesde = "2015-01-01";
			//entradaActualizarCostoUF.p_FechaHasta = "2015-06-30";
			
			//entradaActualizarCostoUF.p_FechaDesde = "2015-07-01";
			//entradaActualizarCostoUF.p_FechaHasta = "2015-12-31";
			
			String [] fechaDesde = {"2008-01-01","2009-01-01","2010-01-01","2011-01-01","2011-07-01","2012-01-01","2012-07-01","2013-01-01","2013-07-01","2014-01-01","2014-07-01","2015-01-01","2015-07-01","2016-01-01","2016-07-01","2017-01-01","2017-07-01","2018-01-01","2018-07-01","2019-01-01","2019-07-01","2020-01-01"};
			String [] fechaHasta = {"2008-12-31","2009-12-31","2010-12-31","2011-06-30","2011-12-31","2012-06-30","2012-12-31","2013-06-30","2013-12-31","2014-06-30","2014-12-31","2015-06-30","2015-12-31","2016-06-30","2016-12-31","2017-06-30","2017-12-31","2018-06-30","2018-12-31","2019-06-30","2019-12-31","2020-06-30"};
			try {
				for(int i= 0; i<fechaDesde.length; i++){
					ActualizarCostoUF actualiza = new ActualizarCostoUF();
					EntradaActualizarCostoUF entradaActualizarCostoUF = new EntradaActualizarCostoUF();
					entradaActualizarCostoUF.p_IdEmpresa = 1L;
					entradaActualizarCostoUF.p_IdUsuario = 21L;
					entradaActualizarCostoUF.p_IdModulo = "FMM";
					entradaActualizarCostoUF.p_FechaDesde = fechaDesde[i];
					entradaActualizarCostoUF.p_FechaHasta = fechaHasta[i];
					
					SalidaActualizarCostoUF salida = actualiza.logica(con, entradaActualizarCostoUF);
					System.out.println(salida);
					con.commit();
				}
				
			}
			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
		}
		System.out.println("== END ==");
		
	}
	
}
